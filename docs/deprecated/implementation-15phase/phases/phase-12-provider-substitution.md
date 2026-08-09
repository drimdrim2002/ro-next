# Phase 12 — Provider substitution

```yaml
document_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
document_version: 1.3
document_workflow_status: INDEPENDENT_REVIEWED_WITH_CORRECTIONS
final_audit_cycle_status: RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2
manifest_reference_dag: STANDALONE_EVIDENCE_TO_FORWARD_MANIFEST_TO_REVIEW_TO_RECEIPT
phase: "12"
phase_name: provider-substitution
baseline_date: "2026-07-28"
implementation_status: NOT_STARTED
phase_acceptance_status: BLOCKED_NOT_IMPLEMENTED
candidate_provider_status: NOT_SELECTED
provider_adoption_status: NOT_APPROVED
deployment_status: NOT_DEPLOYED
production_default_status: NOT_AUTHORIZED
evidence_status: NOT_PRODUCED
review_status: COMPLETE_CHANGES_REQUIRED
review_document: ../reviews/phase-12-review.md
entry_gate_status: GATED_BY_UNACCEPTED_PREDECESSORS_AND_PROVIDER_ADOPTION
handoff_status: NOT_READY
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_revision_status: ALNS_FIRST_GATE_OVERLAY_APPLIED_DOCUMENTATION_ONLY
scheduler_task_id: TBD_NOT_SUPPLIED
owners:
  implementation: approved candidate-provider adapter owner role
  application_contract: Phase 08 Application/Local Runtime owner role
  storage_contract: Phase 09 Object Storage owner role
  coordinator_contract: Phase 10 Provider-neutral Coordinator owner role
  aws_reference_subject: Phase 11 AWS Reference Distribution owner role
  independent_semantic_oracle: provider-neutral conformance test owner role
  provider_adoption: Product + Platform + Operations owner roles
  security: Platform Security and data-governance owner roles
  reliability: SRE and disaster-recovery owner roles
  performance_cost: Performance + FinOps owner roles
  independent_conformance: provider-neutral conformance test owner role
  downstream_hybrid_decision: Phase 13 Optional Hybrid owner role
  review: independent Phase 12 reviewer role
prerequisites:
  - Phase 00 accepted module/package architecture and provider-leakage rules
  - Phase 07 accepted both-gate PublishableResult authority
  - Phase 08 accepted provider-neutral application, worker, workflow and telemetry ports
  - Phase 09 accepted exact-key, immutable artifact, digest, CAS and no-database contracts
  - Phase 10 accepted state/action, declared-completeness, retry, deadline and cancellation contracts
  - Phase 11 accepted review and manifest-independent immutable AWS reference evidence digest
  - explicit candidate-provider and substitution-axis adoption approval
  - approved isolated non-production integration environment for the candidate provider
planned_evidence:
  - E-P12-PROVIDER-CONTRACT
  - E-P12-PARITY
  - E-P12-MIGRATION
  - E-P12-SECURITY
  - E-P12-OPERATIONS
  - E-P12-PERFORMANCE
  - E-P12-ROLLBACK
source_sections:
  canonical_master: "§1~4, §13~17; especially §15.10 RM-8 and §16.2~16.3"
  final_domain: "§3, §7~10, §15~18; provider-neutral authority/result meaning only"
  final_architecture: "§2~3, §5~6; ports, identity, lifecycle, failure, security and evidence"
  integrated_design: "§16 and §19~28; substitution, migration, failure/test/invariants"
  open_questions: "Q-BENCH-02, Q-INFRA-01, Q-VAR-01 and §3~4"
  docs_readme: "all four named sections; canonical document map and historical boundaries"
  implementation_readme: "§1, §3~4, §6~7"
  master_realization_plan: "§2~4, §6, Phase 12, §8~15"
  phase_11_actual_predecessor: "v1.3 §6.1~6.3, §14.1~14.2, §17 and §18.2; stable citation, reciprocal cycle resolution introduced in v1.2"
  phase_13_actual_downstream: "v1.5 §4.1, §6.5 and §14.2; review COMPLETE/PASS_WITH_RESIDUAL_BLOCKERS, gated not started"
  root_readme_and_inventory: "README, pom.xml, Dockerfile, tracked Java/GCP source and tests"
canonical_source_fingerprints_sha256:
  docs/master-design.md: e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd
  docs/deprecated/2026-07-26-domain-design.md: 1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac
  docs/deprecated/2026-07-26-architecture-design.md: 1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed
  docs/deprecated/architecture-domain-implementation-design.md: 883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571
inventory_fingerprints_sha256:
  README.md: 22eff4f63607db29bd4049344986109c680aa970d0865a3b859598e6b3b96c06
  pom.xml: f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6
  Dockerfile: 2aae6615e6c3dba5184064593e29407f7cded4e63dd416719a0b2fb30846d985
  gcp/README.md: 8294e4c3bbb7b92b0d7f51136b18aaa342fe3c9f06ec822dc23ce5a75c915913
  gcp/cloudbuild.yaml: 284663c833bb3550497f412e507e25a4d12b64f4bcae0ad993c9bd576f76b209
  gcp/workflows/optimization.yaml: 65eeef9344a63b743af1684b915c9bdfa976454e560ebb8624d75ed00fd874c1
  AlnsBatchEngine.java: 4120203ded07267bd71179b3eecf251cc635f17b5378eeda038819b2a2ae7481
  OptimizationApiController.java: 3dcab11fcac78bb994b1f77bedaf425601684e8b9b3eb1560c305dcf671f358c
  OptimizationWorkerController.java: 846e64f1ad76386ac4da847d6e2b9585ed5d909841266c06c38915aed06afe3c
  AlnsBatchEngineTest.java: 947cf04529ffb45f8049b5b3cf64a06e00680e1657393d50ef7ca8e627a3f829
historical_cross_check:
  file: docs/deprecated/2026-07-26-master-design.md
  status: SUPERSEDED_NOT_AUTHORITY
docs_codex_status: HISTORICAL_NOT_AUTHORITY_NOT_MODIFIED
neighbor_phase_documents:
  phase_11: ACTUAL_V1_3_REVIEW_COMPLETE_CHANGES_REQUIRED_NOT_STARTED_HANDOFF_NOT_READY
  phase_12_review: ACTUAL_COMPLETE_CHANGES_REQUIRED
  phase_13: ACTUAL_V1_5_REVIEW_COMPLETE_PASS_WITH_RESIDUAL_BLOCKERS_GATED_NOT_STARTED
  phase_14: ACTUAL_V1_4_REVIEW_COMPLETE_CHANGES_REQUIRED_14A_NOT_RUN_14B_NOT_STARTED
live_document_inventory:
  phase_documents_present: "15/15"
  phase_reviews_present: "15/15"
  phase_reviews_complete: "15/15"
  observed_review_verdicts: "P00 PASS_WITH_RESIDUAL_BLOCKERS; P01 ACCEPTED_WITH_APPLIED_CORRECTIONS; P02 PASS_AFTER_APPLIED_CORRECTIONS; P03~P12 CHANGES_REQUIRED; P13 PASS_WITH_RESIDUAL_BLOCKERS; P14 CHANGES_REQUIRED"
authoring_snapshot:
  status: HISTORICAL_NOT_CURRENT
  phase_11_then: V1_1
  phase_13_then: READY_FOR_REVIEW_GATED_NOT_STARTED_APPEARED_DURING_AUTHORING
neighbor_fingerprint_policy: CANONICAL_FINGERPRINTS_AND_STABLE_SECTION_CITATIONS_ONLY
```

## 1. 문서 지위, 권위와 fingerprint 정책

이 문서 세트의 입력 권위는 **사용자 선언으로 고정**되었다. Source 문서의
`REVIEW` metadata는 provenance로 보존하지만 Phase 12 상세 문서 작성을 멈추는
조건이 아니다. 반대로 이 문서가 구체적인 Java type, test와 migration 절차를
제안한다는 사실은 candidate provider 선택, 구현, 배포, production default 또는
cutover authority가 생겼다는 뜻이 아니다.

상태 축을 분리한다.

| 축 | 현재 값 | 의미 |
|---|---|---|
| Source decision | `Q-INFRA-01 RESOLVED` | AWS S3 + Step Functions + Lambda가 선택된 target/reference다. |
| Candidate provider | `NOT_SELECTED` | ECS, GCP, Kubernetes, Azure 또는 다른 provider를 임의 선택하지 않는다. |
| 문서 | `INDEPENDENT_REVIEWED_WITH_CORRECTIONS` | Provider substitution 계약 review와 안전 교정이 완료됐다. |
| 구현/evidence | `NOT_STARTED` / `NOT_PRODUCED` | Adapter, integration run 또는 parity evidence를 주장하지 않는다. |
| Adoption | `NOT_APPROVED` | 어느 대체 provider도 운영 채택 대상으로 승인되지 않았다. |
| Production default | `NOT_AUTHORIZED` | Phase 12가 provider traffic/default를 바꾸지 않는다. |
| Phase review | `COMPLETE — CHANGES_REQUIRED` | [독립 리뷰](../reviews/phase-12-review.md)는 완료됐으나 cross-phase blocker와 implementation/evidence gate가 남는다. |
| Documentation inventory | `PHASE 15/15; REVIEW 15/15 COMPLETE` | 작성 중 관찰한 일부 부재/`READY_FOR_REVIEW` 표기는 historical snapshot이며 현재 inventory가 아니다. |
| Phase 11 | `v1.3 / COMPLETE — CHANGES_REQUIRED / NOT_STARTED / NOT_READY` | v1.2에서 reciprocal conformance-evidence cycle은 해소됐고 v1.3 current contract에도 보존되지만 실제 evidence와 accepted handoff는 없다. |
| Phase 13 | `v1.5 / COMPLETE — PASS_WITH_RESIDUAL_BLOCKERS / GATED_NOT_STARTED` | Phase 14A ALNS benchmark acceptance와 `C-17` 전에는 hybrid를 시작하거나 활성화하지 않는다. |
| Phase 14 | `v1.4 / COMPLETE — CHANGES_REQUIRED / 14A NOT_RUN / 14B NOT_STARTED` | 14A benchmark evidence가 없고 14B calibration/cutover authority도 없다. |

권위 적용 순서는 다음과 같다.

1. 사용자 선언과 [Canonical Master](../../2026-07-31-phase-b-master-design.md)
2. [질문 등록부](../../master-design-open-questions.md)의 exact `Q-*` 상태
3. [Final Domain Design](../../2026-07-26-domain-design.md)의 domain/result 의미
4. [Final Architecture Design](../../2026-07-26-architecture-design.md)의 module/DAG/port 배치
5. [Integrated implementation design](../../architecture-domain-implementation-design.md)의 Phase 12 substitution 계약
6. [Master Realization Plan](../master-realization-plan.md)과 [구현 문서 지도](../README.md)
7. [Actual Phase 11](phase-11-aws-reference-distribution.md)의 v1.3 §6.1~§6.3,
   §14.1~§14.2, §17과 §18.2 predecessor contract/blocker

[Actual Phase 13](phase-13-optional-hybrid-route-selection.md)은 downstream
consumer이며 이 문서의 authority 순서를 바꾸지 않는다. 현재 v1.5와
[완료된 review](../reviews/phase-13-review.md)의 §4.1 activation AND gate,
§6.5 skip/activated boundary와 §14.2 Phase 12 evidence receipt를 직접 대조했다.
“작성 중 파일이 나타남”과 당시 `READY_FOR_REVIEW` 관찰은 historical authoring
snapshot일 뿐 live status가 아니다.

[2026-07-26 Master Design — SUPERSEDED](../../2026-07-26-master-design.md)는
역사적 누락 cross-check에만 사용했다. `docs/codex/*`와 legacy GCP 자료는 현재
authority가 아니다. Final Architecture의 과거 `Q-INFRA-01 DEFERRED` 표기는 최신
Canonical Master/질문 등록부의 `RESOLVED`, AWS reference 선택으로 해소한다.

### 1.1 Canonical fingerprint와 stable section 재현

```bash
shasum -a 256 \
  docs/master-design.md \
  docs/deprecated/2026-07-26-domain-design.md \
  docs/deprecated/2026-07-26-architecture-design.md \
  docs/deprecated/architecture-domain-implementation-design.md
```

Metadata fingerprint는 사용자 지정 canonical 네 문서의 provenance/drift 확인에만
사용한다. 질문 등록부, Master Realization Plan, 구현 지도와 인접 Phase는 §1의
stable named section을 직접 읽고 semantic compatibility와 accepted artifact/evidence
identity를 확인한다.

Phase 11/12/13 whole-file 또는 section hash를 서로 저장하거나 acceptance 입력으로
사용하지 않는다. Phase 11 §18.2와 Phase 13 §4.1/§6.5/§14.2는 stable citation일 뿐
문서 존재·hash 일치가 accepted handoff를 증명하지 않는다. Inventory fingerprint도
current characterization drift 확인용이며 구현·evidence acceptance가 아니다.

## 2. 목표, 범위, 비범위와 불변조건

### 2.1 목표

Phase 12의 목표는 승인된 한 substitution axis에서 AWS reference subject와 candidate
provider subject가 동일한 provider-neutral storage/coordinator/worker/application
contract를 만족함을 한 conformance suite와 비교 가능한 evidence로 입증하는 것이다.

```text
same pre-sealed ProviderConformanceApplicabilityConfig
+ same logical ExecutionManifest
+ same canonical input/artifact bytes
→ AWS reference result
+ candidate-provider result
→ independent provider-neutral oracle가 각각을 재계산
→ same semantic artifact/result fingerprints
   or same provider-neutral typed exceptional termination
→ immutable AWS/candidate evidence를 서로 독립 seal
→ pre-review ProviderConformanceManifest가 두 evidence digest를 forward-reference
→ independent review 뒤 separate acceptance receipt
```

AWS는 선택된 reference execution/evidence baseline이지 semantic truth의 유일한
oracle이 아니다. AWS와 candidate가 같은 adapter/common bug로 같은 오답을 만들면
둘의 일치만으로 pass할 수 없다. Provider 차이는 capability/evidence로 드러낸다.
차이를 core/domain/identity/schema, result authority, completeness 또는 verifier 의미
변경으로 흡수하지 않는다.

### 2.2 포함 범위

- Storage, durable workflow, worker compute 축을 독립 식별하는 substitution scope
- 승인된 candidate provider용 최소 adapter/distribution/deployment mapping
- Phase 09 storage와 Phase 10 coordinator/worker contract의 공통 conformance suite
- AWS reference evidence와 candidate evidence의 기계 판독 가능한 comparison
- Capability, CAS/consistency, failure, retry/deadline, serialization/replay 차이 판정
- IAM-equivalent least privilege, encryption, tenant, logging/redaction 통제 비교
- Observability semantic field와 provider metadata 분리
- Artifact digest-preserving copy, shadow, recoverable rollback rehearsal 설계
- Provider별 workload performance/cost envelope evidence
- Unsupported/gated 결과와 adoption recommendation 또는 rejection evidence

### 2.3 명시적 비범위

- Candidate provider의 무승인 선택, 구현 시작, account/project/cluster 생성
- 어떤 provider도 production default 또는 AWS의 자동 대체로 확정
- AWS production cutover, candidate production cutover 또는 traffic switch
- Core/domain/solver/verifier/application port의 provider별 fork
- Artifact/result schema 또는 logical ID의 provider-specific version
- Database, queue 또는 provider listing을 새 authority로 도입
- Phase 13 route pool/MIP/hybrid 구현, OR-Tools version/config/native/distribution 선택
- Phase 14 official 수치, benchmark, official manifest와 cutover
- `Q-BENCH-02` 공식 수치, `Q-VAR-01` deferred 변형의 해소
- Legacy GCP placeholder를 target parity 또는 구현 evidence로 승격

### 2.4 고정 불변조건

1. `rpdptw-core`, solver, verification, application은 provider SDK/type/URI를 참조하지 않는다.
2. Provider 변경은 pair, route/bank partition, travel, evaluation, comparator와 two-gate publication을 바꾸지 않는다.
3. Provider locator/execution ID/version token은 semantic/result fingerprint에 들어가지 않는다.
4. Artifact kind/schema/canonical bytes/content digest/logical authority lineage는 provider 간 같다.
5. Immutable create, verified exact read, state CAS와 publication CAS 의미를 약화하지 않는다.
6. Prefix/list/event arrival/completion order는 declared completeness 또는 champion authority가 아니다.
7. Same logical work retry는 `AttemptId`와 provider observation만 바꾼다.
8. Platform timeout/deadline/resource failure는 정상 `MAX_*`/plateau 종료가 아니다.
9. 두 verifier `PASS` 전에는 어느 provider도 normal publication을 할 수 없다.
10. Unsupported capability는 `UNSUPPORTED`, 미승인은 `GATED`이며 emulation으로 `SUPPORTED`를 위조하지 않는다.
11. Provider-specific exception은 provider-neutral failure taxonomy로 손실 없이 mapping한다.
12. Provider config/secret/credential/resource name은 domain artifact가 아니다.
13. Serialization은 allow-listed versioned canonical codec만 사용하며 Java/native SDK object를 저장하지 않는다.
14. Encryption과 digest, IAM과 application string check, telemetry와 correctness는 서로 대체하지 않는다.
15. Candidate 성능 우위는 semantic parity나 security gap을 덮지 못한다.
16. AWS reference의 한계도 candidate에 유리하도록 숨기지 않고 동일 evidence schema에 기록한다.
17. 미확정 provider/API/수치/threshold는 `PROPOSED` 또는 `OPEN`이고 hidden default가 아니다.
18. Phase 12 evidence는 Phase 13에 hybrid 활성화가 아니라 인프라 적합성 판단 자료만 제공한다.
19. Run-state CAS token, current-pending action authorization과 publication-pointer precondition은 서로 대체하지 않는다.
20. Cancellation intent는 durable request/hint이며 `CANCEL_REQUESTED` 대 `PUBLISHING`의 same-run-state CAS만 terminal fence다.
21. Tenant authorization은 non-ambient approved binding으로 전달하고 denied/missing/corrupt/stale/indeterminate failure를 손실 없이 보존한다.
22. Durable crash/re-entry는 restart-safe deadline contract 없이는 deadline-enabled provider path를 활성화하지 않는다.

## 3. 결정 상태와 gate 보존

| 항목 | 상태 | Phase 12 처리 |
|---|---|---|
| `Q-INFRA-01` | `RESOLVED` | AWS를 reference execution/evidence subject로 사용한다. 독립 semantic oracle이나 구현/cutover 완료로 오인하지 않는다. |
| Candidate provider | `NOT_SELECTED` | 승인 record 전 module skeleton도 만들지 않는다. |
| Substitution axis | `NOT_SELECTED` | `STORAGE`, `WORKFLOW`, `COMPUTE`, `DISTRIBUTION` 중 승인된 최소 축만 변경한다. |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Test-only explicit 값만 허용하고 official 수치/성능 pass threshold를 만들지 않는다. |
| `C-17` / Actual Phase 13 | `GATED TARGET/GATED_NOT_STARTED` | Hybrid 구현·활성화를 당기지 않는다. |
| `Q-VAR-01` | `DEFERRED` | 질문·구현·contract 완화 금지 |
| Production authority | `NOT_GRANTED` | Evidence/recommendation과 실제 default/cutover를 분리한다. |

Candidate adoption record의 proposed schema는 다음과 같다.

```java
public record ProviderAdoptionDecision(
    DecisionId decisionId,
    CandidateProviderId providerId,
    Set<SubstitutionAxis> approvedAxes,
    DeploymentScope nonProductionScope,
    Set<RequiredCapabilityId> requiredCapabilities,
    SecurityControlProfileId securityProfile,
    PerformanceEnvelopePolicyRef performancePolicy,
    CostEvidencePolicyRef costPolicy,
    ExpirationOrReviewAt reviewBoundary,
    ApprovalEvidenceRef approvalEvidence
) {}

public enum SubstitutionAxis {
    STORAGE,
    DURABLE_WORKFLOW,
    WORKER_COMPUTE,
    DISTRIBUTION_MAPPING
}
```

이 schema와 이름은 **PROPOSED INTERNAL**이다. 실제 provider ID, resource shape,
threshold와 날짜는 owner 승인 전 open이다.

## 4. Entry gate와 evidence receipt

### 4.1 필수 entry

| Gate | 필요한 evidence | 미충족 시 last safe state |
|---|---|---|
| G12-E0 Source | §1 canonical fingerprint와 stable-section authority review | 문서/fixture review만 |
| G12-E1 Phase 08~10 | Accepted port/state/failure/evidence refs와 cross-phase blocker 해소 | Contract catalog/red tests만 |
| G12-E2 Phase 11 | Accepted review와 manifest-independent immutable AWS `ProviderEvidenceManifest` | AWS evidence schema review만 |
| G12-E3 Adoption | Provider/axis/scope/capability 명시 approval | Candidate module/config 생성 금지 |
| G12-E4 Environment | Isolated non-prod environment, scoped credentials, budget/cleanup owner | Offline adapter design만 |
| G12-E5 Security/Ops | Non-ambient access, lossless failure, IAM-equivalent, encryption, retention, retry, quota, telemetry review | Security/performance red tests만 |
| G12-E6 Linearization/deadline | Distinct publication precondition, same-state cancel fence, restart-safe durable deadline contract | Pure state/virtual-clock red tests만 |

Phase 11 문서의 현재 상태는
`INDEPENDENT_REVIEWED_WITH_CORRECTIONS/NOT_STARTED/HANDOFF_NOT_READY`이고 review
verdict는 `CHANGES_REQUIRED`다. 따라서 아래 receipt는 미래 구현 entry
requirement이며 현재 존재한다고 주장하지 않는다.

```text
Phase12EntryReceipt
  phase08AcceptedContractRef
  phase09AcceptedStorageContractRef
  phase10AcceptedCoordinatorContractRef
  phase11AcceptedReviewRef
  awsReferenceProviderEvidenceDigest
  providerConformanceApplicabilityConfigDigest
  candidateProviderAdoptionDecisionRef
  approvedAxes[]
  isolatedEnvironmentApprovalRef
  securityOperationsPerformancePolicyRefs[]
  crossPhaseContractResolutionRefs[]
  canonicalSourceFingerprints
  stableSourceSectionRefs[]
  productionAuthority = false
  contentDigest
```

### 4.2 Entry rejection

다음 중 하나면 implementation work package를 시작하지 않는다.

- Provider 이름만 있고 approved axis/scope가 없음
- Phase 11 console screenshot 또는 emulator 결과만 있고 immutable evidence manifest가 없음
- Candidate가 CAS/consistency를 제공하지 않는데 “eventually consistent enough”로 처리
- Run-state token을 publication pointer precondition으로 재사용하거나 cancel intent object를 terminal fence로 처리
- Ambient credential/thread-local tenant와 generic exception으로 authorization/failure contract를 채움
- Restart-safe durable deadline contract 없이 provider crash/re-entry를 정상 운영 가능으로 표시
- Security/performance threshold가 test code hidden constant로만 존재
- GCP legacy path를 target contract로 간주
- Phase 13 value hypothesis를 이유로 provider를 먼저 선택

## 5. Current inventory와 proposed change tree

### 5.1 2026-07-28 actual inventory

| 영역 | 현재 사실 | Phase 12 해석 |
|---|---|---|
| Build | 단일 `pom.xml`, Java 25, GCP Workflow/Storage SDK가 root dependency | Target multi-module/provider isolation이 아직 없다. |
| Core | `AlnsBatchEngine`이 seed/iterations로 합성 `objective` map 생성 | RPDPTW 또는 conformance oracle이 아니다. |
| API | `Map<String,Object>`, `gs://` 입력, random UUID와 hidden fallback 수치 | Canonical application/identity contract evidence가 아니다. |
| Worker/finalize | GCS prefix listing 후 raw objective 최소 candidate 선택 | Declared completeness, CAS, two-gate publication과 불일치한다. |
| GCP workflow | 병렬 HTTP batch 후 finalize 호출 | Durable provider-neutral coordinator semantics를 구현하지 않는다. |
| GCP IAM guide | Broad example roles, public API option, fixed timeout 예시 | Security/cutover 승인 evidence가 아니다. |
| Test | `AlnsBatchEngineTest` 한 개 | 합성 candidate만 확인하며 provider parity evidence가 아니다. |
| AWS | Phase 11 v1.3 review는 `CHANGES_REQUIRED`; v1.2에서 reciprocal evidence cycle은 해소됐지만 implementation/evidence는 `NOT_STARTED/NOT_PRODUCED` | Accepted reference evidence와 나머지 blocker 해소 전 Phase 12 entry는 닫혀 있다. |
| Phase 08~10 reviews | Access/failure carrier, worker commit, publication precondition, cancellation fence와 durable deadline blocker가 남음 | Phase 12가 임의 adapter signature/default로 봉합하지 않는다. |
| Documentation set | Phase 문서 15/15, review 15/15 존재하고 review 15/15 완료 | 작성 당시 부재/`READY_FOR_REVIEW` snapshot은 historical이며 live inventory가 아니다. |
| Phase 13 | v1.5 review `PASS_WITH_RESIDUAL_BLOCKERS`; `GATED_NOT_STARTED`, evidence 미생산 | §4.1/§14.2의 모든 Phase 12 evidence gate를 존중하고 hybrid artifact/backend를 만들지 않는다. |
| Phase 14 | v1.4 review `CHANGES_REQUIRED`; `14A NOT_RUN`, `14B NOT_STARTED/BLOCKED_NOT_READY` | ALNS benchmark evidence, official calibration/cutover evidence 또는 권한을 Phase 12가 만들지 않는다. |

Legacy GCP inventory는 candidate 선정 evidence가 아니라 characterization fixture다.
그 path가 현재 실행 가능하더라도 target contracts를 만족한다는 뜻이 아니다.

### 5.2 승인 뒤에만 생성할 change tree

```text
build/
└── provider-conformance-tests/             # provider-neutral suite
    ├── src/main/java/.../conformance/
    ├── src/testFixtures/.../provider/
    └── src/test/.../comparison/

adapters/
├── object-<approved-provider>/              # STORAGE axis일 때만
├── workflow-<approved-provider>/            # DURABLE_WORKFLOW axis일 때만
└── compute-<approved-provider>/             # WORKER_COMPUTE axis일 때만

distributions/
└── <approved-distribution>/                 # DISTRIBUTION_MAPPING 승인 시

deployment/
└── <approved-provider>/                     # reviewed source IaC only

evidence/
└── schema/                                  # source tree에 evidence instance를 위조해 넣지 않음
```

`<approved-provider>`는 문서 placeholder이며 실제 module 이름이 아니다. 승인 전 빈
GCS/ECS/Kubernetes/Azure module, dependency 또는 production config를 만들지 않는다.

### 5.3 Compile dependency direction

```text
core / solver / verification
          ↓
      application
          ↓
  provider-neutral ports
       ↙       ↘
object-common  provider-conformance-tests
       ↓             ↑
approved adapters ───┘
       ↓
distribution/deployment
```

금지 dependency:

```text
core|solver|verification|application → provider SDK
core|solver|verification             → provider-conformance-tests
AWS adapter                          → candidate adapter
candidate adapter                    → AWS adapter
conformance oracle                   → concrete provider exception/DTO
Phase 12 modules                     → Phase 13 hybrid/backend/vendor modules
```

## 6. Provider-neutral contracts

모든 이름은 **PROPOSED INTERNAL projection**이다. Phase 08~11 accepted signature를
하나로 맞추는 cross-phase receipt 뒤 exact package/type을 확정한다. `Object`,
`Map<String,Object>`, provider overload 또는 duplicate port로 signature drift를
봉합하지 않는다.

### 6.1 Conformance subject와 capability

```java
public interface ProviderConformanceSubject extends AutoCloseable {
    ProviderIdentity identity();
    ProviderCapabilityReport probeCapabilities();
    ArtifactStore artifactStore();
    RunStateRepository runStateRepository();
    ResultPublisher resultPublisher();
    WorkerDispatcher workerDispatcher();
    WorkflowExecutionPort workflowExecutionPort();
    CancellationPort cancellationPort();
    TelemetryPort telemetryPort();
    ProviderObservationReader observations();
}

public record ProviderIdentity(
    ProviderId providerId,
    ProviderRole role,                 // AWS_REFERENCE or CANDIDATE
    ProviderAdapterVersion adapterVersion,
    DistributionArtifactDigest distributionDigest,
    ProviderEnvironmentClass environmentClass
) {}

public sealed interface ProviderCapabilityDecision
    permits Supported, Unsupported, Gated, Indeterminate {

    record Supported(
        RequiredCapabilityId capability,
        CapabilityEvidenceRef evidence
    ) implements ProviderCapabilityDecision {}

    record Unsupported(
        RequiredCapabilityId capability,
        UnsupportedReasonCode reason
    ) implements ProviderCapabilityDecision {}

    record Gated(
        RequiredCapabilityId capability,
        ApprovalRequirementRef requirement
    ) implements ProviderCapabilityDecision {}

    record Indeterminate(
        RequiredCapabilityId capability,
        MissingEvidenceRef missingEvidence
    ) implements ProviderCapabilityDecision {}
}
```

AWS reference와 candidate는 같은 `ProviderIdentity` projection을 쓰되 role을 분리한다.
AWS를 `CandidateProviderId`로 가장하거나 candidate를 reference oracle로 승격하지
않는다. Subject construction은 approved non-ambient authorization binding과 explicit
environment scope를 받아야 하며 process ambient credential, thread-local tenant 또는
global default provider를 읽을 수 없다. Exact binding type은 Phase 08/09 cross-phase
승인 전 proposed blocker다.

Required capabilities:

```text
IMMUTABLE_PUT_IF_ABSENT
EXACT_VERIFIED_READ
ATOMIC_COMPARE_AND_SET
ATOMIC_PUBLICATION_FENCE
EXACT_READ_AFTER_COMMITTED_WRITE
DURABLE_WORKFLOW_REENTRY
IDEMPOTENT_WORKFLOW_START
IDEMPOTENT_WORKER_DISPATCH
ATTEMPT_PRESERVING_RETRY
COOPERATIVE_STOP_AND_CANCEL
DECLARED_COMPLETENESS
TENANT_SCOPED_AUTHORIZATION
NON_AMBIENT_TENANT_AUTHORIZATION
LOSSLESS_OPERATION_FAILURE_MAPPING
DISTINCT_RUN_STATE_AND_PUBLICATION_PRECONDITIONS
SAME_RUN_STATE_CANCEL_PUBLICATION_FENCE
RESTART_SAFE_DURABLE_DEADLINE
ENCRYPTION_IN_TRANSIT
ENCRYPTION_AT_REST
STRUCTURED_CORRELATED_TELEMETRY
BOUNDED_RETRY_AND_DEADLINE
CANONICAL_SERIALIZATION_REPLAY
```

`Unsupported`는 정상적인 Phase 12 결론이다. Required capability가 unsupported이면
그 axis는 adoption `REJECTED_UNSUPPORTED`; core/port를 완화하지 않는다.

### 6.2 Storage/application signatures

```java
public interface ArtifactStore {
    ArtifactPutResult putIfAbsent(
        ArtifactKey key,
        ArtifactContent content,
        ContentDigest expectedDigest
    );

    ReadableArtifact readVerified(ArtifactRef reference);
    ArtifactMetadata metadata(ArtifactRef reference);
}

public interface RunStateRepository {
    CreateStateResult createIfAbsent(SolveId solveId, RunState initialState);
    VersionedRunState get(SolveId solveId);
    StateUpdateResult compareAndSet(
        SolveId solveId,
        StateVersion expectedVersion,
        RunState nextState
    );
}

public interface ResultPublisher {
    VersionedPublishedResult get(SolveId solveId);
    PublicationResult compareAndSet(
        SolveId solveId,
        PublicationPrecondition expectedPublication,
        PublishableResultRef result
    );
}

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

`OpaqueLocator`와 provider version token은 adapter 내부다. Application은 parse,
concatenate, order, compare 또는 fingerprint하지 않는다. Candidate가 only-eventual
read를 제공하거나 stale read를 정상으로 반환할 수 있으면 `EXACT_VERIFIED_READ`
required capability를 만족하지 않는다. Read-retry로 해결할 수 있는지, 어떤 bounded
deadline과 indeterminate failure를 반환할지는 provider-specific evidence로 증명한다.

`ResultPublisher.get`과 `PublicationPrecondition`은 latest Phase 10/11의
response-loss reconciliation 및 distinct-pointer refinement를 반영한 **PROPOSED
CROSS-PHASE BLOCKER**다. `StateVersion`과 호환·대체할 수 없다. Phase 08/09/10/11
owner가 exact signature와 non-ambient authorized access/lossless failure carrier를
승인하기 전 Phase 12 adapter는 overload, ambient context 또는 generic exception으로
이를 구현하지 않는다.

### 6.3 Coordinator, workflow와 worker signatures

```java
public interface AdvanceSolve {
    AdvanceSolveResult advance(AdvanceSolveCommand command);
}

public interface WorkerDispatcher {
    DispatchReceipt dispatch(WorkerAssignment assignment);
    WorkerExecutionStatus getStatus(WorkerRunId workerRunId);
    StopReceipt requestStop(
        WorkerRunId workerRunId,
        CancellationId cancellationId
    );
}

public interface WorkflowExecutionPort {
    WorkflowStartReceipt start(LogicalWorkflowRequest request);
    WorkflowExecutionStatus getStatus(LogicalWorkflowRunId runId);
    WorkflowCancelReceipt requestCancel(
        LogicalWorkflowRunId runId,
        CancellationId cancellationId
    );
}

public sealed interface CoordinatorAction
    permits DispatchPhaseOneScreens,
            WaitForPhaseOneScreens,
            DispatchWorkers,
            WaitForWorkers,
            RequestWorkerStops,
            FinalizeResult,
            PublishResult,
            CompleteSolve,
            FailSolve {
    ActionId actionId();
    long authorizingTransitionOrdinal();
}
```

Candidate workflow는 action type/refs만 mapping한다. 점수, verifier report 내용,
worker completeness, next round 또는 publication eligibility를 provider workflow
definition/controller에서 계산하지 않는다.

`authorizingTransitionOrdinal`도 단독 authorization token이 아니다. Executor는 side
effect 직전에 exact current pending `ActionId`, payload fingerprint와 ordinal을 함께
대조한다. Opaque run-state version은 repository CAS에만 남고 action/event/fingerprint로
직렬화하지 않는다. Cancellation intent는 request/hint이며 same-run-state
`CANCEL_REQUESTED` 대 `PUBLISHING` CAS 뒤에만 publication pointer operation을
시도한다.

### 6.4 Config와 explicit semantic differences

```java
public record ProviderAdapterConfig(
    ProviderIdentity providerIdentity,
    Set<SubstitutionAxis> enabledAxes,
    ExplicitRetryPolicy retryPolicy,
    ExplicitDeadlinePolicy deadlinePolicy,
    ExplicitConsistencyPolicy consistencyPolicy,
    EncryptionPolicyRef encryptionPolicy,
    TenantAuthorizationPolicyRef authorizationPolicy,
    TelemetryPolicyRef telemetryPolicy,
    PerformanceEnvelopePolicyRef performancePolicy,
    ProviderOpaqueConfigRef opaqueProviderConfig
) {}

public record ProviderSemanticDifference(
    DifferenceId differenceId,
    ContractDimension dimension,
    SemanticDifferenceKind kind,
    SafeDescription description,
    ProviderCapabilityDecision decision,
    RequiredAction requiredAction
) {}

public enum SemanticDifferenceKind {
    METADATA_ONLY,
    EQUIVALENT_MAPPING,
    UNSUPPORTED,
    GATED_REQUIRES_APPROVAL,
    CONTRACT_VIOLATION
}
```

Provider API name, event format, version token, resource ID, timeout signal과 metric
namespace는 `METADATA_ONLY` 또는 `EQUIVALENT_MAPPING`일 수 있다. CAS 원자성,
visibility, completeness, retry identity, verifier/publication 의미 차이는 metadata가
아니며 `UNSUPPORTED/GATED/CONTRACT_VIOLATION`으로 판정한다.

### 6.5 Adapter execution과 comparison pseudocode

```text
evaluateCandidate(adoptionDecision):
  require adoptionDecision.providerId and approvedAxes are explicit
  require non-production environment and owner approvals
  seal ProviderConformanceApplicabilityConfig from approved axes before either provider run
  subject = createCandidateSubject(adoptionDecision)
  capabilities = subject.probeCapabilities()
  for each required capability in stable manifest order:
    record SUPPORTED | UNSUPPORTED | GATED | INDETERMINATE
  if any required applicable capability != SUPPORTED:
    return typed rejection/gate; do not weaken contract or deploy
  run identical conformance cases against AWS_REFERENCE and CANDIDATE
  compare provider-neutral projections
  record provider observations separately
```

```text
executeConformanceCase(case, subject):
  fixture = readVerified(case.fixtureRef)
  before = oracle.captureAuthoritativeState(subject)
  inject only the case-declared provider operation/fault
  actual = case.execute(subject, fixture)
  after = oracle.captureAuthoritativeState(subject)
  semantic = oracle.recomputeProviderNeutralProjection(before, actual, after)
  observations = subject.observations().readSafeProjection()
  return immutable result(semantic, observations, assertions)
```

```text
compareProviders(applicabilityConfig, awsEvidence, candidateEvidence):
  require awsEvidence and candidateEvidence contain no conformance/review/receipt back-reference
  require each evidence executedApplicabilityConfigDigest == applicabilityConfig.contentDigest
  require same pre-sealed applicable case set, case catalog and independent oracle version
  require applicable case skip count == 0
  for each caseId in stable order:
    require independent oracle assertion PASS for AWS and candidate separately
    compare semantic artifact/state/identity/termination projection
    compare neutral failure and retry disposition
    retain provider metadata differences outside semantic fingerprint
  evaluate security control objectives
  evaluate approved performance/cost envelope or return GATED
  seal immutable comparison and recommendation with productionAuthority=false
  seal pre-review ProviderConformanceManifest(
      applicabilityConfig.contentDigest,
      awsEvidence.contentDigest,
      candidateEvidence.contentDigest,
      comparison.contentDigest)
```

Applicability를 provider 결과를 본 뒤 바꾸거나 candidate가 어려운 case를
`NOT_APPLICABLE`로 재분류하지 않는다. Approved substitution axis와 accepted contract
version이 case applicability를 실행 전에 결정한다. AWS↔candidate equality는 필요한
comparison이지만 독립 oracle에 대한 각 subject의 pass를 대신하지 않는다.

```text
migrateShadowClosure(sourceRootRef, destination):
  closure = traverse exact declared refs; never prefix-list for authority
  for each ref in stable closure order:
    verify source kind/schema/length/digest/authority
    destination.putIfAbsent(exact logical key, canonical content, digest)
    read back and verify destination
    seal ArtifactCopyReceipt
  require every declared ref has equivalent receipt
  execute shadow without publication/state/traffic authority
  on any failure: stop, preserve source refs/pointers, quarantine destination conflict
```

## 7. Artifact, identity, serialization과 lifecycle

### 7.1 Semantic identity 대 provider observation

| Semantic identity — provider 간 동일 | Provider observation — 비교 기록만 |
|---|---|
| Tenant/Submission/Solve/Manifest fingerprint | Account/project/subscription/cluster |
| Round/worker logical ordinal와 `WorkerRunId` | Function/task/job/pod/execution ID |
| `AttemptId` rule | Provider delivery/redelivery ID |
| Artifact kind/schema/digest/length | Bucket/container/object URI, ETag/generation |
| Problem/travel/profile/config/build fingerprints | Region/zone/runtime revision |
| Candidate/result/payload fingerprints | Provider trace/log/metric locator |
| Coordinator action/state lineage | Provider workflow state/event name |

Provider metadata를 제외한 semantic projection은 canonical ordering과 explicit codec
version을 사용한다.

```java
public record SemanticReplayProjection(
    ExecutionManifestFingerprint manifest,
    List<CoordinatorTransitionDigest> transitions,
    List<LogicalWorkerReplayDigest> workersInStableOrder,
    List<CanonicalArtifactDigest> artifactsInStableOrder,
    Optional<PublishableResultDigest> result,
    ProviderNeutralTermination termination
) {}

public record ProviderObservationProjection(
    ProviderIdentity provider,
    List<SafeRuntimeObservation> runtimeObservations,
    List<SafeResourceObservation> resourceObservations,
    List<SafeFailureObservation> failureObservations
) {}
```

### 7.2 Canonical codec contract

```text
bytes receive
→ maximum length and schema allowlist
→ digest/length verify
→ canonical decoder
→ semantic validation
→ canonical re-encode
→ byte-exact or declared canonical-digest equality
```

- Arbitrary Java serialization, SDK DTO serialization, native object handle과 map key
  order 의존 JSON을 금지한다.
- Unknown schema/version은 typed `UNSUPPORTED_SCHEMA`, silent ignore가 아니다.
- Provider event envelope는 adapter schema이며 canonical domain/result schema를
  변경하지 않는다.
- Same semantic manifest를 AWS와 candidate에서 읽고 다시 쓰면 semantic bytes/digest가
  같다. Compression/encryption bytes가 달라도 protected canonical content digest는 같다.

### 7.3 Lifecycle와 migration

```text
source exact read
→ source canonical digest/schema/authority verify
→ destination put-if-absent
→ destination exact read-back
→ destination canonical digest/schema/authority verify
→ immutable ArtifactCopyReceipt
→ all referenced closure copied and verified
→ shadow execution only
→ optional pointer/state cutover proposal
```

```java
public record ArtifactCopyReceipt(
    ArtifactKey logicalKey,
    ArtifactRef sourceRef,
    ArtifactRef destinationRef,
    ContentDigest canonicalDigest,
    ArtifactSchemaVersion schemaVersion,
    CopyDisposition disposition,
    VerificationReportRef sourceVerification,
    VerificationReportRef destinationVerification
) {}

public sealed interface CopyDisposition
    permits Copied, AlreadyEquivalent, Conflict, Unsupported, Indeterminate {}
```

Same logical key/different digest는 overwrite/migration이 아니라 integrity conflict다.
Phase 12는 immutable closure copy와 rollback rehearsal을 설계·검증할 수 있지만
production pointer/traffic을 전환하지 않는다.

### 7.4 State transition comparison

```text
SUBMITTED
→ PREPARING
→ PREPARED
→ ROUND_DISPATCHING
→ ROUND_RUNNING
→ ROUND_VERIFYING
→ ROUND_AGGREGATING
→ ROUND_DISPATCHING | FINALIZING
→ PUBLISHING
→ SUCCEEDED
```

Exceptional branch:

```text
any nonterminal
→ CANCEL_REQUESTED | WATCHDOG_REACHED | RESOURCE_LIMIT_REACHED
  | PLATFORM_TIMEOUT | FAILED | INCOMPLETE | PUBLICATION_REJECTED
→ terminal CAS
→ late provider event is no-op/reconciliation evidence
```

AWS와 candidate 비교는 provider event state가 아니라 위 provider-neutral state,
`transitionOrdinal`, action identity와 artifact refs를 사용한다.

## 8. Failure taxonomy, retry, CAS와 deadline

### 8.1 Provider-neutral failure map

```java
public sealed interface ProviderOperationFailure
    permits TransientUnavailable,
            Throttled,
            AccessDenied,
            AuthenticationFailed,
            ConditionalConflict,
            VisibilityIndeterminate,
            IntegrityViolation,
            UnsupportedCapability,
            QuotaOrCapacityExceeded,
            PlatformTimeout,
            CancellationObserved,
            ProviderProtocolViolation {

    ProviderFailureCode code();
    RetryDisposition retryDisposition();
    SafeFailureDetail safeDetail();
    ProviderObservationRef observation();
}
```

| Provider observation | Neutral mapping | Retry rule | Publication |
|---|---|---|---|
| Transport unavailable | `TransientUnavailable` | Bounded same operation/logical identity | Full success 뒤만 |
| Throttle/rate limit | `Throttled` | Explicit backoff/budget | Partial success 금지 |
| Authn/authz deny | `AuthenticationFailed`/`AccessDenied` | Auto credential broadening 금지 | 차단 |
| Conditional precondition fail | `ConditionalConflict` | Exact reload/revalidate | Last-write-wins 금지 |
| Ack lost/unknown write | `VisibilityIndeterminate` | Exact key/status inspect | Blind duplicate 금지 |
| Digest/schema mismatch | `IntegrityViolation` | Same artifact retry 금지 | 차단/incident |
| Missing atomic CAS | `UnsupportedCapability` | Retry로 보완 금지 | Adoption reject |
| Runtime hard timeout | `PlatformTimeout` | Same logical work/new attempt if policy permits | 정상 termination 금지 |
| Stop/cancel signal | `CancellationObserved` | Intent/stop/actual termination 분리 | Success 금지 |
| Unknown SDK status | `ProviderProtocolViolation` | Safe fail closed | Success 금지 |

### 8.2 Idempotency와 CAS

```text
same SubmissionId + same canonical digests
→ converge to same SolveId/state

same SubmissionId + different canonical digests
→ IDEMPOTENCY_CONFLICT

same WorkerRunId + retry
→ same assignment/seed/warm start/requested steps + new AttemptId only

same logical success + same verified digest
→ converge

same logical success + different verified digest
→ REPRODUCIBILITY_INTEGRITY_FAILURE
```

CAS conformance:

- Concurrent state updates have exactly one winner for an expected version.
- Stale version never overwrites.
- Lost success response is reconciled by exact read, not list or blind retry.
- Action executor는 exact current pending action/payload/transition을 side effect 직전에 재검증한다.
- Opaque run-state token은 action identity나 publication precondition으로 재사용하지 않는다.
- `CANCEL_REQUESTED`와 `PUBLISHING`은 같은 run-state expected version에서 경쟁하며 intent object는 hint다.
- Publication pointer는 Phase 07 both-gate closure와 distinct `PublicationPrecondition`만 받는다.
- Same published digest converges; different digest conflicts.
- Provider version token equality is opaque and never ordered.

### 8.3 Retry와 deadline

세 layer를 분리한다.

```text
SDK/transport retry       # same provider operation, same AttemptId
provider delivery retry   # same logical event receipt reconciliation
application work retry    # same WorkerRunId, new AttemptId
```

`ExplicitRetryPolicy`의 attempts/backoff/jitter, `ExplicitDeadlinePolicy`의 operation,
workflow, worker platform reserve 수치는 adoption/workload evidence 전 **OPEN**이다.
Test fixture 값은 `TEST_ONLY`로 주입한다. Provider remaining time, lease TTL 또는
HTTP timeout을 algorithm steps/rounds/quality termination으로 변환하지 않는다.

Crash/re-entry가 가능한 workflow에서 process-local monotonic tick을 새 process가
직접 비교하거나 wall clock/provider remaining-time으로 조용히 복원하지 않는다.
Durable deadline representation, restart-origin reconciliation, platform reserve와
indeterminate-clock failure가 Phase 08/10/11에서 승인되고 virtual-clock crash oracle을
통과하기 전 deadline-enabled provider conformance/adoption은 `GATED`다.

Missing, denied, corrupt, stale, conflict, indeterminate와 protocol failure는 operation별
approved exhaustive carrier로 application까지 손실 없이 전달한다. Generic
`AdapterUnavailable`, empty/not-found 또는 success로 축소하면
`LOSSLESS_OPERATION_FAILURE_MAPPING`을 실패한다.

## 9. Conformance와 evidence comparison model

### 9.1 단일 suite

```java
public interface ProviderConformanceCase {
    ConformanceCaseId id();
    Set<RequiredCapabilityId> requiredCapabilities();
    ConformanceCaseResult execute(
        ProviderConformanceSubject subject,
        ConformanceFixture fixture,
        ConformanceOracle oracle
    );
}

public record ConformanceCaseResult(
    ConformanceCaseId caseId,
    ConformanceDisposition disposition,
    Optional<SemanticReplayProjection> semanticProjection,
    ProviderObservationProjection providerObservations,
    List<AssertionResult> assertions,
    EvidenceRef rawEvidence
) {}

public enum ConformanceDisposition {
    PASS,
    FAIL,
    UNSUPPORTED,
    GATED,
    NOT_RUN
}
```

같은 case implementation을 AWS reference와 candidate subject에 parameterized 실행한다.
Provider별 test copy에서 assertion을 삭제하거나 expected failure를 달리하지 않는다.
Provider-specific setup/observation extractor만 plugin fixture로 분리한다.

### 9.2 비교 dimensions

| Dimension | Equality/oracle |
|---|---|
| Contract/version | Accepted port/action/schema versions exact |
| Artifact | Kind/schema/canonical digest/length/authority refs exact |
| Identity | Provider metadata 제외 logical identity projection exact |
| Lifecycle | Legal state/action sequence and terminal exact |
| Completeness | Same declared set; missing one blocks aggregation/publication |
| Failure | Same neutral code/retry disposition/last safe point |
| CAS/consistency | Required atomic/visibility assertions all pass |
| Idempotency/retry | Attempt-only change and duplicate convergence exact |
| Deadline/cancel | No normal termination; same exceptional class/fence |
| Verification/result | Both-gate refs and publishable payload digest exact |
| Serialization/replay | Canonical bytes/digests and semantic replay exact |
| Security | Same control objective; provider mechanism evidence may differ |
| Observability | Required correlation/event semantics exact; namespace may differ |
| Performance/cost | Approved envelope comparison, not semantic equality |

### 9.3 Comparison record

```java
public record ProviderEvidenceComparison(
    ProviderConformanceApplicabilityConfig applicabilityConfig,
    EvidenceContentDigest awsReferenceEvidenceDigest,
    EvidenceContentDigest candidateProviderEvidenceDigest,
    List<ContractComparison> contractComparisons,
    List<ProviderSemanticDifference> differences,
    SecurityControlComparison security,
    ObservabilityComparison observability,
    PerformanceEnvelopeComparison performance,
    MigrationComparison migration,
    AdoptionRecommendation recommendation,
    boolean productionAuthority
) {
    // productionAuthority must remain false in Phase 12.
}

public sealed interface AdoptionRecommendation
    permits EligibleForSeparateAdoptionReview,
            RejectedUnsupported,
            RejectedContractViolation,
            GatedMissingEvidence,
            NoRecommendation {}
```

## 10. Ordered work packages

모든 WP의 target path/type 이름은 proposed다. Adoption 승인 전 WP12-0/1의
read-only review와 red test specification까지만 허용한다.

### WP12-0 — Entry receipt와 source drift freeze

- **Prerequisite:** 문서 baseline; candidate 구현 권한은 불필요.
- **Targets:** `Phase12EntryReceipt`, canonical fingerprint script, stable-section predecessor receipt.
- **Tasks:** §1 fingerprint 재현, Phase 08~11 accepted 상태/evidence 확인, candidate/axis
  approval 유무 확인, actual Phase 13 gated/not-started 상태 기록.
- **Verification:** §1.1 commands; `Phase12EntryReceiptTest.rejectsMissingPhase11EvidenceOrProviderApproval()`.
- **Verification command:** §1.1 canonical fingerprint와 future accepted wrapper의
  `./mvnw -f build/provider-conformance-tests/pom.xml clean test -Dtest=Phase12EntryReceiptTest -Dsurefire.failIfNoSpecifiedTests=true`.
- **Expected:** 미충족 entry가 `GATED`로 명시되고 implementation file 생성이 0이다.
- **Failure/rollback:** Drift/receipt 누락이면 이전 문서 snapshot과 red specs만 보존하고
  changed section을 재검토한다.
- **Handoff:** WP12-1에 immutable entry report; gate가 닫혀 있으면 WP12-2 이후 금지.

### WP12-1 — Contract snapshot와 conformance catalog

- **Prerequisite:** WP12-0 canonical fingerprint/stable-section review pass; Phase 08~11 contract refs available.
- **Targets:** Pre-run `ProviderConformanceApplicabilityConfig`, accepted signature
  projection, conformance case catalog와 provider-neutral fixtures.
- **Tasks:** Storage/coordinator/worker/application method, state, failure, artifact,
  identity, codec와 required capability를 versioned manifest로 고정한다. Distinct
  publication precondition, same-state cancel fence, non-ambient authorization,
  lossless failure와 durable deadline blocker 해소 ref도 포함한다.
- **Verification:** `ProviderConformanceCatalogTest.coversEveryAcceptedPortMethodStateAndFailureCode()`;
  `ProviderConformanceApplicabilityConfigTest.sealsAxesCasesOracleAndRuntimeConfigBeforeProviderRuns()`;
  canonical serialization/digest test.
- **Verification command:** `./mvnw -f build/provider-conformance-tests/pom.xml clean test -Dtest=ProviderConformanceCatalogTest,ProviderConformanceApplicabilityConfigTest,ProviderCanonicalCodecTest -Dsurefire.failIfNoSpecifiedTests=true`.
- **Expected:** AWS/candidate가 공유하는 case ID와 독립 oracle 한 벌; provider SDK import 0.
- **Failure/rollback:** Cross-phase signature drift면 bridge/overload를 만들지 않고 owner
  compatibility review로 돌아간다.
- **Handoff:** WP12-2~9가 소비하는 sealed applicability/config digest. 이것은
  pre-review `ProviderConformanceManifest`가 아니며 provider evidence를 참조하지 않는다.

### WP12-2 — Approved candidate capability probe와 module boundary

- **Prerequisite:** WP12-1; explicit adoption decision and approved axes/environment.
- **Targets:** 승인된 최소 adapter module, `ProviderConformanceSubject`,
  capability probe, provider config validator.
- **Tasks:** Approved axis만 scaffold; SDK를 adapter에 격리; required capability를
  actual provider docs/API probe/integration evidence로 판정한다.
- **Verification:** `ProviderSdkLeakageArchitectureTest`;
  `ProviderCapabilityProbeIT.reportsUnsupportedInsteadOfEmulatingMissingAtomicCas()`.
- **Verification command:** Approved module path가 receipt에서 resolve되고 full reactor
  install이 끝난 뒤 architecture unit은
  `./mvnw -f build/architecture-rules/pom.xml clean test -Dtest=ProviderSdkLeakageArchitectureTest -Dsurefire.failIfNoSpecifiedTests=true`,
  actual probe는
  `./mvnw -f adapters/<approved-axis-module>/pom.xml -Pprovider-integration clean verify -Dit.test=ProviderCapabilityProbeIT -Dfailsafe.failIfNoSpecifiedTests=true`.
- **Expected:** 각 capability가 `SUPPORTED/UNSUPPORTED/GATED/INDETERMINATE` 중 하나이며
  누락/unknown을 supported로 세지 않는다.
- **Failure/rollback:** Required unsupported이면 adapter candidate branch를 비활성화하고
  `RejectedUnsupported` evidence만 보존한다.
- **Handoff:** 승인 axis와 capability decision matrix를 WP12-3/4에 전달.

### WP12-3 — Storage/CAS/consistency conformance

- **Prerequisite:** WP12-2에서 `STORAGE` 승인; Phase 09 accepted suite.
- **Targets:** `object-<approved-provider>`, exact key codec, error mapper,
  atomic create/CAS/publication mapping.
- **Tasks:** Put-if-absent, verified exact read, metadata, state CAS, distinct
  publication-precondition CAS, lost-ack exact-read reconciliation과 no-list authority를
  구현한다.
- **Verification:** `ProviderStorageConformanceTest` 전체와 actual candidate integration;
  concurrent writers, stale read, corruption, cross-tenant negative cases.
- **Verification command:** Full reactor install 뒤
  `./mvnw -f build/provider-conformance-tests/pom.xml -Pprovider-integration clean verify -Dtest=ProviderStorageConformanceTest -Dit.test=CandidateObjectStorageProviderIT -Dsurefire.failIfNoSpecifiedTests=true -Dfailsafe.failIfNoSpecifiedTests=true`.
- **Expected:** AWS와 candidate 모두 same suite pass; semantic digest mismatch 0.
- **Failure/rollback:** Partial/indeterminate object는 quarantine; pointer/state를 전진시키지
  않고 exact previous ref를 유지한다.
- **Handoff:** `StorageConformanceEvidence`와 limitations를 WP12-5/7/9에 전달.

### WP12-4 — Workflow/coordinator/worker conformance

- **Prerequisite:** WP12-2의 approved workflow/compute axis; Phase 10 accepted suite.
- **Targets:** Approved workflow/compute adapters와 distribution mapping.
- **Tasks:** Start/reentry/wakeup/action mapping, exact-current-pending authorization,
  stable dispatch, status/stop, duplicate/lost/out-of-order reconcile, same-run-state
  cancel/publication fence, declared completeness와 both-gate publication을 구현한다.
- **Verification:** `ProviderWorkflowConformanceTest`, `ProviderWorkerConformanceTest`,
  `ProviderApplicationParityIT` with failure injection.
- **Verification command:** Full reactor install 뒤
  `./mvnw -f build/provider-conformance-tests/pom.xml -Pprovider-integration clean verify -Dtest=ProviderWorkflowConformanceTest,ProviderWorkerConformanceTest -Dit.test=ProviderApplicationParityIT -Dsurefire.failIfNoSpecifiedTests=true -Dfailsafe.failIfNoSpecifiedTests=true`.
- **Expected:** Provider workflow가 semantic decision을 소유하지 않고 동일 state/action
  trace 또는 typed exceptional terminal을 생성한다.
- **Failure/rollback:** New starts 중단, in-flight exact state reconcile, accepted
  AWS reference/local path를 유지한다. Candidate terminal을 success로 변환하지 않는다.
- **Handoff:** Workflow/compute evidence와 replay trace를 WP12-5/8/9에 전달.

### WP12-5 — Failure, retry, deadline, serialization과 replay parity

- **Prerequisite:** WP12-3/4 중 applicable axis green.
- **Targets:** Failure mapper, retry classifier, canonical envelope codecs,
  semantic/provider observation projections.
- **Tasks:** §8 matrix 모든 provider fault를 inject하고 same logical retry identity,
  exhaustive failure carrier, canonical re-encode, restart-safe deadline과 crash/reentry
  replay를 비교한다.
- **Verification:** `ProviderFailureTaxonomyContractTest`,
  `ProviderSerializationReplayIT`, `ProviderRetryIdentityIT`,
  `ProviderDeadlineRestartIT`, `ProviderCorruptionIT`.
- **Verification command:**
  `./mvnw -f build/provider-conformance-tests/pom.xml -Pprovider-integration clean verify -Dtest=ProviderFailureTaxonomyContractTest -Dit.test=ProviderSerializationReplayIT,ProviderRetryIdentityIT,ProviderDeadlineRestartIT,ProviderCorruptionIT -Dsurefire.failIfNoSpecifiedTests=true -Dfailsafe.failIfNoSpecifiedTests=true`.
- **Expected:** Unknown/indeterminate가 fail-closed; same envelope replay semantic digest exact.
- **Failure/rollback:** Bad artifact/attempt를 quarantine, last committed state/result ref 유지,
  automatic retry 중단.
- **Handoff:** Failure/replay evidence와 open mappings를 WP12-8/9에 전달.

### WP12-6 — Security, observability와 operations equivalence

- **Prerequisite:** Applicable adapter integration environment; approved control profile.
- **Targets:** Provider-specific IAM-equivalent policies, encryption config, audit/log/metric/
  trace mapping, retention/quota/runbook.
- **Tasks:** Non-ambient authorization binding, role×operation negative matrix, tenant
  boundary, lossless deny/missing mapping, secret/PII redaction, encryption in transit/at
  rest, required correlation/alarms와 cleanup을 검증한다.
- **Verification:** `ProviderSecurityControlIT`, `ProviderObservabilityIT`,
  `ProviderOperationalRecoveryIT`.
- **Verification command:**
  `./mvnw -f distributions/<approved-distribution>/pom.xml -Pprovider-integration clean verify -Dit.test=ProviderSecurityControlIT,ProviderObservabilityIT,ProviderOperationalRecoveryIT -Dfailsafe.failIfNoSpecifiedTests=true`.
- **Expected:** Mechanism 이름은 달라도 control objective와 negative oracle 모두 pass;
  broad wildcard data role 0, forbidden log match 0.
- **Failure/rollback:** Credentials/policy/revision revoke, new starts stop, immutable audit
  evidence preserve, incident owner에게 handoff.
- **Handoff:** Security/operations comparison을 WP12-8/9에 전달.

### WP12-7 — Artifact copy, shadow와 migration rehearsal

- **Prerequisite:** WP12-3 storage applicable and green; source/destination non-prod scope approved.
- **Targets:** Copy planner/executor, `ArtifactCopyReceipt`, closure verifier,
  shadow-only distribution selector.
- **Tasks:** Source verify → put-if-absent → read-back verify → closure compare;
  no-authority shadow run과 recoverable rollback을 rehearsal한다.
- **Verification:** `ProviderArtifactMigrationIT`, `ProviderShadowIsolationIT`,
  `ProviderRollbackIT`.
- **Verification command:** Full reactor install 뒤
  `./mvnw -f build/provider-conformance-tests/pom.xml -Pprovider-integration clean verify -Dit.test=ProviderArtifactMigrationIT,ProviderShadowIsolationIT,ProviderRollbackIT -Dfailsafe.failIfNoSpecifiedTests=true`.
- **Expected:** Canonical digest/identity preservation 100%; source pointer/traffic mutation 0.
- **Failure/rollback:** Destination conflict/quarantine, copied but unreferenced bytes 유지 또는
  approved cleanup plan; source immutable refs/pointers unchanged.
- **Handoff:** Migration/rollback receipts를 WP12-8/9에 전달.

### WP12-8 — Full parity와 performance envelope comparison

- **Prerequisite:** WP12-3~7 applicable gates pass.
- **Targets:** 서로 독립인 immutable AWS/candidate `ProviderEvidenceManifest`,
  `ProviderEvidenceComparison`, replay corpus runner, performance/cost recorder.
- **Tasks:** Same sealed applicability/config와 corpus를 local independent oracle,
  AWS와 candidate에서 실행한다. 각 evidence를 conformance manifest/review/receipt
  reference 없이 먼저 seal하고, 각 subject oracle pass 뒤 contract/fault/corruption/
  security/repro/performance dimension을 digest로 비교한다.
- **Verification:** `ProviderEndToEndParityIT`, `ProviderReproducibilityIT`,
  `ProviderPerformanceEnvelopeIT`; independent oracle recomputation;
  `ProviderEvidenceManifestTest.awsAndCandidateEvidenceHaveNoConformanceReviewOrReceiptBackReference()`.
- **Verification command:**
  `./mvnw -f build/provider-conformance-tests/pom.xml -Pprovider-parity clean verify -Dit.test=ProviderEndToEndParityIT,ProviderReproducibilityIT,ProviderPerformanceEnvelopeIT,ProviderCostEvidenceIT -Dfailsafe.failIfNoSpecifiedTests=true`.
- **Expected:** Semantic mismatch 0; approved performance/security envelope pass 또는 typed
  reject/gated recommendation.
- **Failure/rollback:** Candidate activation 없음; AWS reference role이나 production
  default도 자동 변경 없음;
  evidence에 exact mismatch와 last safe point 기록.
- **Handoff:** Standalone AWS/candidate evidence content digests, complete comparison과
  recommendation을 WP12-9에 전달.

### WP12-9 — Conformance manifest, independent review와 bounded handoff

- **Prerequisite:** WP12-0~8 applicable evidence complete; open/unsupported included.
- **Targets:** Review 전 두 provider evidence digest와 applicability/config를
  forward-reference하여 seal되는 `ProviderConformanceManifest`, 그 exact digest를
  검토하는 independent review package, 둘을 결합하는 `Phase12AcceptanceReceipt`,
  Phase 13 substitution evidence projection.
- **Tasks:** AWS/candidate evidence를 manifest 독립으로 먼저 seal한다. 다음으로
  applicability/config, 두 evidence content digest, comparison/limitations를
  `ProviderConformanceManifest`에 forward-reference하고 review 전에 seal한다.
  Review는 conformance manifest digest만 입력으로 받아 seal한다. Review 뒤 manifest나
  evidence를 고치지 않고 acceptance receipt가 conformance manifest digest와 independent
  review digest/verdict/restart를 결합해 `productionAuthority=false`를 다시 봉인한다.
- **Verification:** `ProviderConformanceManifestTest.forwardReferencesApplicabilityAndExactlyTwoProviderEvidenceDigests()`,
  `ProviderConformanceManifestTest.containsNoReviewOrAcceptanceReference()`,
  `Phase12AcceptanceReceiptTest.rejectsEvidenceManifestReviewOrReceiptCycle()`,
  `Phase12AcceptanceReceiptTest.rejectsMismatchedManifestOrReviewDigest()`;
  documentation/link/diff/traceability checks; independent review.
- **Verification command:**
  `./mvnw -f build/provider-conformance-tests/pom.xml clean test -Dtest=ProviderEvidenceManifestTest,ProviderConformanceManifestTest,Phase12AcceptanceReceiptTest -Dsurefire.failIfNoSpecifiedTests=true`
  후 §1.1 fingerprint, local-link, `git diff --check` 검증.
- **Expected:** Evidence→conformance/review/receipt back-reference 0,
  conformance→review/acceptance reference 0, review→evidence direct reference 0,
  receipt의 conformance/review digest 각각 1, exact digest 일치와
  `productionAuthority=false`; Phase 13에는 hybrid 구현 권한이 아닌 infra envelope만 전달.
- **Failure/rollback:** Manifest/review 불완전이면 `NOT_READY`; evidence instance를 수정
  overwrite하지 않고 새 version으로 재생성.
- **Handoff:** §18의 bounded manifests only.

## 11. Exact test specification

### 11.1 Fixture와 oracle

| Fixture ID | 내용 | Oracle |
|---|---|---|
| `provider/basic-success-v1` | One explicit manifest, declared Phase-1 screens/workers, both PASS refs | Canonical local/Phase 10 state/action/result projection |
| `provider/missing-worker-v1` | One declared worker outcome absent | `INCOMPLETE`/wait; publication absent |
| `provider/duplicate-out-of-order-v1` | Duplicate wakeup/completion in permuted order | Same terminal semantic digest |
| `provider/cas-race-v1` | Two writers, same expected version, different next states | Exactly one winner |
| `provider/publication-precondition-v1` | Run-state token, pointer precondition and lost response are independently permuted | Distinct token domains; exact-read convergence |
| `provider/lost-ack-v1` | Create/CAS success response lost | Exact read convergence; duplicate mutation 0 |
| `provider/retry-identity-v1` | Worker start failure then retry | Attempt changes only |
| `provider/platform-timeout-v1` | Runtime terminates before complete record | `PLATFORM_TIMEOUT`, never normal |
| `provider/cancel-publish-race-v1` | Cancel intent, `CANCEL_REQUESTED` and `PUBLISHING` race | Same-run-state CAS has one authoritative terminal fence |
| `provider/deadline-restart-v1` | Process loss and new monotonic origin before deadline decision | Approved durable reconciliation or typed gated/indeterminate |
| `provider/corrupt-artifact-v1` | Same ref with changed byte/schema/length | Integrity rejection before deserialize |
| `provider/serialization-v1` | Stable maps/lists/IDs in noncanonical input order | Canonical re-encode/digest exact |
| `provider/cross-tenant-v1` | Tenant A role probes tenant B refs/actions | All denied; no metadata disclosure |
| `provider/ambient-auth-v1` | Explicit access binding absent/mismatched while ambient credential exists | Backend call/existence disclosure 0 |
| `provider/shadow-copy-v1` | Complete immutable artifact closure | Destination digests exact; source pointer unchanged |
| `provider/performance-corpus-v1` | Approved workload classes and repeat count | Owner-approved envelope; values remain external |

Independent oracle는 provider log, event order, SDK success flag 또는 existing GCP
objective를 authority로 사용하지 않는다. Phase 07 verifier reports, exact refs,
canonical codec와 Phase 10 state/action model에서 재계산한다.

### 11.2 Contract and architecture tests

| Test class | Exact method | Fixture/oracle |
|---|---|---|
| `ProviderConformanceCatalogTest` | `coversEveryAcceptedPortMethodStateActionFailureAndCapability()` | Contract manifest coverage |
| same | `usesOneCaseIdAndOracleForAwsAndCandidateSubjects()` | Duplicate provider-specific assertion 0 |
| same | `sealsApplicableCaseSetBeforeEitherProviderRun()` | Post-result reclassification 0 |
| `ProviderConformanceApplicabilityConfigTest` | `sealsAxesCasesOracleAndRuntimeConfigBeforeProviderRuns()` | Immutable pre-run config digest |
| `ProviderEvidenceManifestTest` | `awsAndCandidateEvidenceHaveNoConformanceReviewOrReceiptBackReference()` | Forbidden evidence edge count 0 |
| same | `eachEvidenceBindsExecutedApplicabilityConfigAndHasDistinctContentDigest()` | Independent immutable evidence |
| `ProviderConformanceManifestTest` | `forwardReferencesApplicabilityAndExactlyTwoProviderEvidenceDigests()` | Config + AWS + candidate digest closure |
| same | `containsNoReviewOrAcceptanceReference()` | Forbidden conformance edge count 0 |
| `IndependentPhase12ReviewTest` | `referencesReviewedConformanceManifestDigestButNoProviderEvidence()` | Review direct-evidence edge count 0 |
| `Phase12AcceptanceReceiptTest` | `referencesExactConformanceManifestAndIndependentReviewDigests()` | Two exact terminal refs |
| same | `rejectsEvidenceManifestReviewOrReceiptCycle()` | Graph cycle detector |
| same | `rejectsMismatchedManifestOrReviewDigest()` | Exact digest mismatch rejected |
| `ProviderSdkLeakageArchitectureTest` | `providerTypesAppearOnlyInApprovedAdapterDistributionAndDeploymentModules()` | Bytecode/import dependency oracle |
| same | `coreDomainSolverVerificationApplicationDoNotDependOnConformanceOrProviderModules()` | DAG oracle |
| same | `phase12DoesNotDependOnPhase13HybridOrVendorModules()` | Dependency graph |
| `ProviderNeutralSchemaStabilityTest` | `candidateAdapterDoesNotChangeArtifactIdentityStateOrResultSchema()` | Accepted schema fingerprints |
| same | `providerLocatorAndExecutionIdAreExcludedFromSemanticFingerprint()` | Mutated metadata permutations |
| `ProviderConfigValidationTest` | `missingProviderAxisRetryDeadlineSecurityOrPerformancePolicyFailsClosed()` | Explicit config oracle |
| same | `unapprovedProviderOrAxisCannotConstructSubject()` | Adoption receipt oracle |
| same | `ambientCredentialOrDefaultProviderCannotConstructSubject()` | Non-ambient binding oracle |

### 11.3 Storage/CAS/consistency tests

`ProviderStorageConformanceTest`는 AWS와 candidate subject에 동일하게 parameterized 한다.

| Exact method | Oracle |
|---|---|
| `putIfAbsentCreatesOnceAndSameDigestConverges()` | One canonical ref |
| `sameKeyDifferentDigestIsIntegrityConflictWithoutOverwrite()` | Original bytes/ref unchanged |
| `readVerifiesKindSchemaLengthDigestBeforeDeserialization()` | Corruption rejected |
| `metadataAndReadDoNotDependOnPrefixListing()` | List call count 0 |
| `concurrentStateCasHasExactlyOneWinner()` | One new state/token |
| `staleStateVersionCannotOverwrite()` | Current state unchanged |
| `lostCasAcknowledgementConvergesByExactRead()` | No second transition |
| `runStateVersionCannotServeAsPublicationPrecondition()` | Type/domain separation |
| `publicationRequiresBothVerifierPassClosure()` | Missing/FAIL report rejected |
| `samePublicationDigestConvergesAndDifferentDigestConflicts()` | One pointer |
| `lostPublicationResponseConvergesByExactPointerRead()` | No second pointer transition |
| `committedExactReadMeetsDeclaredConsistencyCapability()` | No false missing/stale success |
| `providerVersionTokenRemainsOpaqueAndUnordered()` | Parse/order attempts absent |
| `crossTenantExactReadWriteAndCasAreDenied()` | All operations denied |

Actual integration class:

| Test class | Exact method |
|---|---|
| `CandidateObjectStorageProviderIT` | `actualBackendPassesCompleteStorageConformanceSuite()` |
| same | `providerNativeConditionalRaceMatchesAbstractCasOracle()` |
| same | `indeterminateWriteIsReconciledWithoutBlindOverwrite()` |

### 11.4 Workflow, worker and application integration tests

| Test class | Exact method | Oracle |
|---|---|---|
| `ProviderWorkflowConformanceTest` | `sameWorkflowKeySameManifestConverges()` | Same logical run |
| same | `sameWorkflowKeyDifferentManifestConflicts()` | Typed identity conflict |
| same | `crashAfterActionCasReplaysSameActionId()` | Same action receipt |
| same | `duplicateLostAndOutOfOrderWakeupsConverge()` | Same semantic trace |
| same | `executorRejectsActionNotMatchingExactCurrentPendingPayloadAndOrdinal()` | Side-effect count 0 |
| same | `missingDeclaredScreenBlocksWarmStartAndPhaseTwoDispatch()` | Dispatch count 0 |
| same | `missingDeclaredWorkerBlocksAggregationAndPublication()` | Pointer absent |
| same | `completionOrderNeverChangesChampionOrResult()` | Permutation equality |
| `ProviderWorkerConformanceTest` | `retryChangesAttemptOnlyAndPreservesAssignmentSeedWarmStartAndWork()` | Identity projection |
| same | `duplicateSameDigestCompletionConverges()` | One committed outcome |
| same | `duplicateDifferentDigestCompletionIsIntegrityFailure()` | Arbitrary winner 0 |
| same | `platformTimeoutResourceLimitAndThrottleAreNotNormalTermination()` | Failure taxonomy |
| same | `stopIntentAndActualTerminationRemainSeparate()` | Three observations |
| `ProviderApplicationParityIT` | `sameManifestProducesSameCanonicalArtifactsResultAndTermination()` | AWS vs candidate projection |
| same | `bothGatePublicationIsTheOnlySuccessPath()` | Fault matrix |
| `ProviderEndToEndParityIT` | `localAwsAndCandidateAgreeOnProviderNeutralExecutionProjection()` | Three-way comparison |

### 11.5 Fault, corruption and recovery tests

| Test class | Exact method | Oracle |
|---|---|---|
| `ProviderFailureTaxonomyContractTest` | `mapsEveryProviderFaultToOneNeutralCodeAndRetryDisposition()` | Exhaustive mapping table |
| same | `unknownProviderStatusFailsClosedAsProtocolViolation()` | Success false |
| `ProviderRetryIdentityIT` | `transportDeliveryAndApplicationRetriesRemainDistinct()` | Attempt/event/operation trace |
| same | `boundedRetryExhaustionEndsTypedWithoutPublication()` | Terminal and call bound |
| `ProviderCancellationDeadlineIT` | `cancelPublishRaceHasExactlyOneAuthoritativeTerminalIntent()` | CAS fence |
| same | `deadlineCannotChangeSeedComparatorStepsOrResultFingerprint()` | Semantic equality |
| `ProviderDeadlineRestartIT` | `restartUsesApprovedDurableOriginReconciliationOrFailsClosed()` | No wall-clock/provider-time fallback |
| `ProviderCorruptionIT` | `mutatedBytesLengthSchemaOrAuthorityRefAreRejectedBeforeUse()` | Independent digest/codec |
| same | `poisonedProviderMetadataCannotChangeSemanticArtifactIdentity()` | Semantic projection unchanged |
| `ProviderOperationalRecoveryIT` | `reentryAfterProcessLossResumesFromExactCommittedState()` | No duplicate logical work |
| same | `lateSuccessAfterExceptionalTerminalCannotPublish()` | Terminal/pointer unchanged |

### 11.6 Serialization, replay and reproducibility tests

| Test class | Exact method | Oracle |
|---|---|---|
| `ProviderCanonicalCodecTest` | `decodeValidateReencodeProducesCanonicalBytesAndDigest()` | Golden bytes |
| same | `unknownSchemaVersionIsUnsupportedNotSilentlyIgnored()` | Typed failure |
| same | `sdkDtoAndJavaSerializationAreRejectedArtifactFormats()` | Codec allowlist |
| `ProviderSerializationReplayIT` | `awsAndCandidateRoundTripSameArtifactsWithoutSemanticDrift()` | Digest/schema/authority exact |
| `ProviderReproducibilityIT` | `sameStrongEnvelopeProducesSameSemanticTraceAndResult()` | Provider metadata excluded |
| same | `differentProviderCompletionOrderDoesNotChangeReplayProjection()` | Permutation equality |
| same | `differentVerifiedDigestForSameStrongReplayIdentityIsIntegrityFailure()` | No winner |

### 11.7 Migration, shadow and rollback tests

| Test class | Exact method | Oracle |
|---|---|---|
| `ProviderArtifactMigrationIT` | `copyVerifiesSourcePutIfAbsentReadBackAndCompleteClosure()` | Receipt per ref |
| same | `sameLogicalKeyDifferentDigestStopsBeforePointerProposal()` | Source/destination unchanged |
| same | `providerLocatorChangeDoesNotChangeSemanticFingerprint()` | Exact projection |
| `ProviderShadowIsolationIT` | `shadowRunCannotPublishMutateSourceStateOrReceiveProductionTraffic()` | Call/pointer/route counters 0 |
| `ProviderRollbackIT` | `candidateFailureRoutesNewNonProductionStartsBackToAcceptedReference()` | Previous revision addressable |
| same | `rollbackPreservesImmutableArtifactsAndReconcilesInflightState()` | No delete/overwrite |

### 11.8 Security and observability tests

| Test class | Exact method | Oracle |
|---|---|---|
| `ProviderSecurityControlIT` | `workerCannotReadOtherTenantWriteChampionOrStartWorkflow()` | Provider-native deny |
| same | `missingOrMismatchedExplicitAccessBindingCannotUseAmbientCredential()` | Backend call/existence disclosure 0 |
| same | `apiCannotReadUnpublishedResultOrWriteWorkerOutcome()` | Provider-native deny |
| same | `workflowIdentityCannotReadBusinessArtifactsOrDecryptPayload()` | Provider-native deny |
| same | `encryptionInTransitAtRestAndContentDigestAreIndependentlyVerified()` | Three controls |
| same | `wildcardDataPlanePrivilegeAndApplicationOnlyTenantCheckAreRejected()` | Policy analyzer |
| same | `secretPiiPayloadAndProviderCredentialNeverAppearInEvidenceLogsOrTraces()` | Forbidden corpus zero match |
| `ProviderFailureCarrierIT` | `deniedMissingCorruptStaleAndIndeterminateRemainDistinctEndToEnd()` | Exhaustive no-collapse mapping |
| `ProviderObservabilityIT` | `requiredCorrelationStateFailureRetryAndVerificationEventsAreEmitted()` | Event catalog |
| same | `providerMetricNamespaceMapsWithoutChangingSemanticEventCode()` | Projection equality |
| same | `alarmsCoverIntegrityAccessRetryExhaustionTimeoutAndIncompleteWork()` | Alarm catalog |

### 11.9 Performance and cost tests

수치/threshold는 `PerformanceEnvelopePolicyRef`에서 explicit approval된 경우에만 pass/fail
oracle이 된다. 문서 또는 test에 임의 production threshold를 넣지 않는다.

| Test class | Exact method | Oracle |
|---|---|---|
| `ProviderPerformanceEnvelopeIT` | `recordsColdWarmLatencyThroughputMemoryConcurrencyAndErrorVector()` | Complete measurement schema |
| same | `comparesSameManifestWorkAndArtifactSizesAcrossProviders()` | Workload identity exact |
| same | `semanticParityAndSecurityRemainHardGatesEvenWhenCandidateIsFaster()` | Gate ordering |
| same | `missingApprovedThresholdReturnsGatedNotPass()` | `GATED_MISSING_POLICY` |
| same | `deadlineReserveCanCompleteFinalVerificationAndPublication()` | End-stage reserve evidence |
| `ProviderCostEvidenceIT` | `recordsRequestComputeStorageTransferObservabilityAndOperationalCostVector()` | Complete cost schema |
| same | `doesNotConvertExperimentEstimateIntoProductionDefault()` | Authority false |

Performance record:

```text
workloadClass / manifestFingerprint / corpusDigest
provider/distribution/build/runtime revision
warm/cold classification
requested/completed logical work
artifact bytes/operations
latency distribution and tail
throughput/concurrency/throttle/retry
memory/cpu/runtime resource
verification/publication reserve
error/timeout/incomplete rate
cost vector and pricing-source timestamp
approved envelope policy ref or GATED_MISSING_POLICY
```

## 12. Red → green sequence, commands와 pass criteria

### 12.1 Red → green

1. Contract catalog/architecture tests를 missing conformance module에서 red로 고정한다.
2. Provider-neutral hand oracle와 deterministic in-memory/file subject로 suite
   assertion을 먼저 green하고 AWS reference subject를 같은 assertion에 통과시킨다.
3. Adoption record 없이는 candidate subject construction이 red인 것을 확인한다.
4. 승인된 최소 axis module을 추가하고 capability probe를 actual environment에서 실행한다.
5. Required unsupported이면 green으로 위조하지 않고 typed adoption rejection으로 종료한다.
6. Applicable storage/workflow/compute contract를 unit → local fake → actual provider 순으로 green한다.
7. Fault/corruption/retry/deadline/cancel cases를 red→green한다.
8. IAM-equivalent/encryption/tenant/redaction negative cases를 actual provider에서 green한다.
9. Artifact copy/shadow/rollback을 source pointer 불변으로 green한다.
10. Local independent oracle↔AWS↔candidate full parity/replay와 approved performance envelope를 실행한다.
11. AWS/candidate evidence를 conformance/review/receipt back-reference 없이 각각 immutable seal한다.
12. Applicability/config와 두 evidence digest를 forward-reference하는
    `ProviderConformanceManifest`를 review 전에 seal한다.
13. 그 conformance manifest digest의 independent review와 manifest+review digest를
    갖는 별도 acceptance receipt를 통과한다.

Emulator/mock green을 actual provider evidence로 표시하지 않는다. AWS와 candidate 중 한쪽
case를 skip하면 parity는 pass가 아니다.

### 12.2 Future commands

현재 root에는 `./mvnw`, target reactor, Maven Failsafe plugin과 Phase 12 test가 없다.
따라서 아래는 accepted Phase 00 wrapper/plugin policy와 target tree가 구현된 뒤에만
실행 가능한 future contract다. 먼저 같은 source를 filter 없이 full install하고,
selected unit/integration run은 exact owner POM에서 `-am` 없이 zero-test fail-closed로
실행한다.

```bash
./mvnw clean install

./mvnw -f build/architecture-rules/pom.xml clean test \
  -Dtest=ProviderSdkLeakageArchitectureTest,ProviderNeutralSchemaStabilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true

./mvnw -f build/provider-conformance-tests/pom.xml clean test \
  -Dtest=ProviderConformanceCatalogTest,ProviderStorageConformanceTest,ProviderWorkflowConformanceTest,ProviderWorkerConformanceTest \
  -Dsurefire.failIfNoSpecifiedTests=true

./mvnw -f build/provider-conformance-tests/pom.xml clean test \
  -Dtest=ProviderFailureTaxonomyContractTest,ProviderCanonicalCodecTest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

Approved isolated candidate environment only:

```bash
./mvnw -f distributions/<approved-distribution>/pom.xml \
  -Pprovider-integration clean verify \
  -Dit.test=ProviderCapabilityProbeIT,ProviderSecurityControlIT,ProviderObservabilityIT,ProviderOperationalRecoveryIT \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dprovider.adoptionDecisionRef=<approved-ref> \
  -Dprovider.evidenceDir=target/phase12-evidence

./mvnw -f build/provider-conformance-tests/pom.xml \
  -Pprovider-parity clean verify \
  -Dit.test=ProviderApplicationParityIT,ProviderEndToEndParityIT,ProviderReproducibilityIT,ProviderDeadlineRestartIT,ProviderCorruptionIT,ProviderArtifactMigrationIT,ProviderShadowIsolationIT,ProviderRollbackIT,ProviderPerformanceEnvelopeIT,ProviderCostEvidenceIT \
  -Dfailsafe.failIfNoSpecifiedTests=true \
  -Dprovider.awsEvidenceRef=<accepted-phase11-ref> \
  -Dprovider.candidateEvidenceRef=<candidate-ref> \
  -Dprovider.evidenceDir=target/phase12-comparison
```

Angle-bracket values는 실행 전 승인 record에서 resolve해야 하며 literal/hidden default로
실행하지 않는다. 각 run 직후 fresh Surefire/Failsafe XML의 discovered
class/method와 passed/failed/error/skipped count를 expected manifest와 대조한다.
Integration test 0건, stale report, disabled profile, skipped required case와 provider
한쪽만의 report는 evidence가 아니다. Production deploy/cutover command는 이 문서에
포함하지 않는다.

### 12.3 Pass criteria

- Maven/architecture/schema validation exit 0
- Common case catalog의 applicable case skip 0
- 실행 전 sealed applicable case set 일치와 post-result reclassification 0
- AWS reference와 candidate에 동일 case ID/독립 oracle 적용률 100%
- 각 subject가 독립 oracle에 개별 pass한 뒤 provider semantic mismatch 0
- Core/domain/solver/verification/application provider SDK/type leakage 0
- Artifact/schema/identity/result semantic mismatch 0
- CAS lost update, last-write-wins, list-authority와 partial-worker publication 0
- Run-state/action/publication token-domain 혼용과 cancel-intent terminal 권위화 0
- Ambient credential/tenant fallback과 failure-code collapse 0
- Restart deadline의 wall-clock/provider-time hidden fallback 0
- Retry에서 logical identity/seed/warm start/requested work 변경 0
- Platform/deadline/cancel/resource failure의 normal termination 오분류 0
- Corrupt/unknown schema/provider protocol의 false success 0
- Required IAM-equivalent negative operation 허용 0
- Secret/PII/payload/credential forbidden log match 0
- Required telemetry/alarm field 누락 0
- Migration canonical digest mismatch와 source pointer mutation 0
- Approved performance/security/operations policy가 없으면 `GATED`, pass 0
- Provider evidence→conformance/review/receipt back-reference 0
- Conformance manifest→review/receipt reference와 review→evidence direct reference 0
- Conformance manifest의 distinct AWS/candidate evidence digest와 applicability/config 누락 0
- Acceptance receipt의 exact conformance manifest/review digest mismatch 0
- `productionAuthority=true` 또는 production-default mutation 0
- Independent Phase 12 review accepted

## 13. Security, IAM-equivalence와 encryption contract

Provider 간 “같은 role 이름”이 아니라 같은 control objective를 비교한다.

| Logical role | 최소 허용 | 금지 |
|---|---|---|
| API | Own-tenant submission/profile exact read, start, published result read | Worker/unpublished artifact write/read |
| Coordinator | Own solve state CAS, action/assignment write, dispatch/stop | Result verifier bypass, broad tenant list |
| Worker | Assigned refs read, own attempt/outcome/report write | Workflow start, champion/publication write |
| Workflow runtime | Named adapter invoke/wait/wakeup | Business artifact read/decrypt |
| Integration test | Named non-prod resources and negative probes | Production resource/policy mutation |
| Deploy/rollback | Reviewed non-prod revision/policy change | Business payload read absent separate audit |

Required evidence:

- Approved non-ambient authorization binding/session lifecycle and async propagation
- Provider-native identity/policy/role definition digest
- Tenant/resource scope and negative authorization result
- TLS/endpoint validation and at-rest encryption metadata
- Key/secret reference; secret value exclusion
- Rotation/revocation/deletion/recovery owner and open values
- Audit data access and retention
- Public ingress/egress/network boundary
- Policy analyzer or equivalent evidence

Caller-supplied tenant string, ambient credential, thread-local context 또는 application
string comparison만으로 tenant isolation을 주장하거나 broad shared credential에 모든
tenant 권한을 주지 않는다. Binding 누락/불일치는 provider call과 existence disclosure
전에 거부한다. Encryption metadata pass가 content digest를 대체하지 않고 digest pass가
confidentiality/authorization을 대체하지 않는다.

## 14. Observability, performance와 operational envelope

Provider-neutral required correlation:

```text
tenantHandle / solveId / manifestFingerprint
roundOrdinal / workerOrdinal / workerRunId / attemptId
actionId / transitionOrdinal
problem/travel/profile/config/build fingerprints
artifact kind/schema/digest
termination / failureCode / retryDisposition
candidateVerification / resultVerification / publicationDisposition
```

Provider-specific execution/resource/trace IDs는 별도 observation projection에 둔다.
Metric 이름이 달라도 semantic event code, cardinality boundary와 required alarm을
mapping한다. Raw input/result, address/PII, token, signed URL, credential, provider
secret와 unrestricted locator를 log/trace에 넣지 않는다.

Performance envelope는 semantic hard gate 뒤에 평가한다.

```text
contract + security + corruption + replay PASS
→ workload identity equality
→ measured performance/cost comparison
→ approved envelope PASS | GATED | REJECTED
```

Cold start, autoscaling, task/pod scheduling, concurrency, payload/artifact limit, retry/
throttle, regional consistency와 observability cost는 provider별 evidence다. Exact
threshold, repetitions, confidence rule, max cost와 resource sizing은 owner 승인 전
`PROPOSED/OPEN`이다.

## 15. Evidence manifest와 exit gates

### 15.1 Evidence manifests

`RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2`: Phase 11 v1.2 §14.1/§18.2는
`Phase11EvidenceManifest`와 post-review acceptance receipt가 Phase 12 manifest를
역참조하지 않게 고정했고, Phase 12 v1.2는 그 immutable digests를 downstream
`ProviderConformanceManifest`에서만 forward-reference한다. Reciprocal
conformance-evidence cycle은 더 이상 current residual blocker가 아니다.

Phase 12가 받을 수 있는 AWS handoff는 conformance manifest, independent review 또는
Phase 12 acceptance receipt를 역참조하지 않는 standalone immutable AWS evidence와
Phase 11 post-review receipt의 content digest뿐이다. Candidate evidence도 같은
독립 봉인 규칙을 따른다. 실제 Phase 11/12 evidence는 `NOT_PRODUCED`이고 accepted
handoff는 `NOT_READY`이므로 schema 해소를 구현/evidence gate 통과로 오인하지 않는다.

```text
ProviderConformanceApplicabilityConfig
  schemaVersion
  logicalPortContractVersion
  artifactAndStateSchemaVersions
  identityAndRetryProjectionVersion
  approvedAxes[]
  approvedEnvironmentAndRuntimeConfig
  requiredStorageContractCases[]
  requiredWorkflowComputeContractCases[]
  preSealedApplicableCaseIds[]
  localOracleRef
  independentOracleVersion
  providerMetadataExclusionProjection
  securityOperationsCostEvidenceSchema
  knownAwsSpecificLimits[]
  productionAuthority = false
  contentDigest
```

이 config는 provider run 전에 seal한다. Provider evidence는 이 config의 content
digest와 actual executed case/result를 기록할 수 있지만 아직 존재하지 않는
`ProviderConformanceManifest`, independent review 또는 acceptance receipt를
역참조하지 않는다.

```text
ProviderEvidenceManifest
  schemaVersion
  providerRole = AWS_REFERENCE | CANDIDATE
  providerIdentityProjection
  executedApplicabilityConfigDigest
  executedCaseEvidenceDigests[]
  independentOracleAssertionDigest
  artifactStateIdentityFailureEvidenceDigests[]
  securityObservabilityOperationsEvidenceDigests[]
  performanceCostEvidenceDigest
  unsupportedGatedOpenDifferences[]
  productionAuthority = false
  contentDigest
```

AWS와 candidate evidence가 각각 immutable seal된 뒤에만 pre-review conformance
manifest를 만든다. 이 manifest가 두 evidence **content digest**와 sealed
applicability/config를 content-addressed forward-reference한다. Evidence 쪽에는
conformance manifest/reference가 없으므로 역방향 edge가 없다.

```text
ProviderConformanceManifest
  schemaVersion
  phase = 12
  entryReceiptRef
  canonicalSourceFingerprints
  stableSourceSectionRefs[]
  providerAdoptionDecisionRef
  approvedAxes[]
  applicabilityConfigDigest
  awsReferenceEvidenceContentDigest
  candidateProviderEvidenceContentDigest
  capabilityDecisionMatrixRef
  storageContractEvidenceRef
  workflowComputeContractEvidenceRef
  applicationParityEvidenceRef
  failureRetryDeadlineEvidenceRef
  serializationReplayEvidenceRef
  corruptionFaultEvidenceRef
  securityControlComparisonRef
  observabilityOperationsEvidenceRef
  performanceCostEnvelopeEvidenceRef
  artifactMigrationShadowEvidenceRef
  rollbackRehearsalRef
  providerEvidenceComparisonRef
  nonAmbientAuthorizationEvidenceRef
  losslessFailureCarrierEvidenceRef
  distinctPublicationPreconditionEvidenceRef
  sameRunStateCancellationPublicationFenceEvidenceRef
  durableDeadlineRestartEvidenceRef
  unsupportedGatedOpenDifferences[]
  adoptionRecommendation
  productionAuthority = false
  contentDigest
```

`ProviderConformanceManifest`는 independent review 전에 seal하고 review/acceptance
reference를 포함하지 않는다. Independent review는 conformance manifest digest만
권위 입력으로 검토하며 evidence를 직접 역참조하지 않는다.

```text
IndependentPhase12Review
  reviewedProviderConformanceManifestDigest
  verdict
  findingsAndResidualBlockers[]
  productionAuthorityGranted = false
  contentDigest
```

Review가 끝난 뒤 별도 receipt만 manifest와 review의 exact content digest를 결합한다.

```text
Phase12AcceptanceReceipt
  schemaVersion
  providerConformanceManifestContentDigest
  independentReviewContentDigest
  independentReviewVerdict
  unresolvedBlockers[]
  handoffDisposition
  productionAuthority = false
  contentDigest
```

허용되는 content-reference DAG는 정확히 다음 순서를 따른다.

```text
ProviderConformanceApplicabilityConfig
  ├─seal→ standalone AWS evidence
  └─seal→ standalone candidate evidence

config + AWS evidence digest + candidate evidence digest
  → ProviderConformanceManifest
  → IndependentPhase12Review(manifest digest)

manifest digest + review digest
  → Phase12AcceptanceReceipt
```

화살표는 seal 시간 순서이며 AWS/candidate evidence branch는 서로를 참조하지 않는다.
Content reference는 evidence→config, manifest→config/evidence, review→manifest,
receipt→manifest/review만 허용한다. 다음 edge는 항상 거부한다.

```text
evidence → conformance manifest | review | acceptance receipt
conformance manifest → review | acceptance receipt
review → evidence | acceptance receipt
acceptance receipt → evidence
```

Exact invariant는 graph가 acyclic이고 모든 digest target이 이미 immutable seal됐으며,
AWS/candidate evidence 어느 쪽에도 conformance/review/receipt field가 0개인 것이다.
Review 결과 때문에 evidence나 manifest 내용이 바뀌면 기존 artifact를 overwrite하지
않고 affected evidence부터 새 content digest로 재생성해 manifest→review→receipt를
새 version chain으로 다시 봉인한다.

Console screenshot, provider deployment existence, one happy path, mock/emulator, legacy GCP
workflow와 generated IaC는 evidence reference를 채우지 않는다.

### 15.2 Exit gates

| Gate | Required evidence |
|---|---|
| G12-A Authority/scope | Accepted entry receipt, explicit provider/axis approval |
| G12-B Architecture | Provider leakage 0, no Phase 13/vendor dependency |
| G12-C Capability | Every required capability supported or axis rejected |
| G12-D Storage | Applicable Phase 09 suite, actual CAS/consistency/digest, distinct publication precondition |
| G12-E Workflow/compute | Applicable Phase 10 current-action/state/retry/same-state cancel/completeness |
| G12-F Contract parity | Both subjects independently pass same oracle/cases; semantic mismatch 0 |
| G12-G Failure/replay | Lossless taxonomy, retry/restart-safe deadline, codec/replay/corruption pass |
| G12-H Security | Non-ambient authorization, IAM-equivalent negative matrix, encryption, tenant, redaction |
| G12-I Operations/performance | Telemetry/alarm/recovery and approved envelope |
| G12-J Migration/rollback | Digest-preserving closure copy, shadow isolation, rollback |
| G12-K Evidence/review | Standalone provider evidence → forward-referencing conformance manifest → review → receipt DAG, forbidden back-reference 0과 exact digest |

모든 applicable gate는 AND다. Required capability unsupported는 구현 실패를 숨기는
것이 아니라 adoption rejection evidence다. `GATED`/`UNSUPPORTED`가 남은 provider를
production-eligible로 표시하지 않는다.

## 16. Definition of Done

Phase 12는 다음이 모두 참일 때만 `ACCEPTED` 후보가 된다.

1. Phase 08~11 predecessor accepted evidence와 exact entry receipt가 있다.
2. Candidate provider/axis/non-production scope가 명시적으로 승인됐다.
3. AWS와 candidate가 같은 pre-sealed applicability/config, case와 독립 oracle을
   사용하고 각각 oracle에 pass한다.
4. Core/domain/identity/artifact schema/result authority 변경이 0이다.
5. Applicable storage/coordinator/worker/application contracts가 actual provider에서 pass했다.
6. Unsupported/gated/semantic difference가 누락 없이 typed evidence에 있다.
7. CAS/consistency/idempotency/retry/restart-safe deadline/same-state cancel/
   completeness/two-gate 의미가 보존된다.
8. Failure taxonomy와 serialization/replay/corruption/fault cases가 pass한다.
9. Non-ambient authorization, lossless failure와 IAM-equivalent/encryption/tenant/
   network/redaction negative evidence가 완전하다.
10. Required observability/recovery와 approved performance/cost envelope가 평가됐다.
11. Artifact closure copy와 shadow/rollback rehearsal이 source authority를 바꾸지 않았다.
12. AWS/candidate evidence가 manifest/review/receipt back-reference 없이 먼저
    독립 seal되고, conformance manifest가 두 evidence digest와 applicability/config를
    forward-reference한 뒤 exact-digest review와 acceptance receipt가 별도로 sealed됐다.
13. `productionAuthority=false`, production default 변경 0이다.
14. Phase 13에는 substitution evidence만 전달되고 hybrid/cutover 구현이 0이다.
15. `Q-BENCH-02`, `C-17`, `Q-VAR-01` 상태가 보존됐다.

Adapter/source/test/IaC 존재, emulator green, provider resource 생성, 성능 우위 또는
happy path 한 번은 DoD가 아니다.

## 17. 금지 anti-pattern

| Anti-pattern | 왜 금지하는가 |
|---|---|
| 승인 없이 GCP/ECS/Kubernetes/Azure 중 하나 선택 | 사용자/owner adoption authority 위반 |
| Legacy GCP 코드를 candidate baseline으로 복사 | Placeholder를 target authority로 승격 |
| Candidate 제약에 맞춰 port/state/schema 변경 | Substitution이 semantic drift가 됨 |
| Provider URI/ETag/generation/task ID를 fingerprint에 포함 | Logical identity와 replay 붕괴 |
| CAS를 check-then-write, local lock, list로 흉내냄 | Distributed lost update |
| Run-state token을 action 또는 publication-pointer precondition으로 재사용 | 권위 key와 linearization point 혼합 |
| Cancel intent object와 publication pointer를 race winner로 취급 | 서로 다른 key의 CAS가 모두 성공 가능 |
| Event/list/completion order로 worker completeness 판정 | Partial success publication |
| Provider workflow에서 objective/comparator/verifier 판단 | Coordinator/domain authority 복제 |
| Retry 때 seed/warm start/work/WorkerRunId 변경 | Reproducibility 붕괴 |
| Timeout을 `MAX_STEPS_REACHED`로 변환 | Platform failure와 quality 종료 혼합 |
| Unknown SDK status를 generic success/empty result로 mapping | Failure taxonomy 손실 |
| SDK DTO/Java serialization을 artifact로 저장 | Schema/replay/provider neutrality 붕괴 |
| Encryption만 보고 digest를 생략 | Confidentiality와 integrity 혼동 |
| Digest만 보고 IAM/tenant 통제를 생략 | Integrity와 authorization 혼동 |
| Broad role + application string check | Provider-native tenant isolation 부재 |
| Ambient credential/thread-local tenant로 access binding 생략 | Caller identity와 authorization closure 부재 |
| Denied/corrupt/stale/indeterminate를 not-found/generic adapter error로 축소 | Retry/security/integrity 의미 손실 |
| Restart 때 wall clock/provider remaining-time으로 monotonic deadline 복원 | Provider별 watchdog/replay drift |
| Provider별 conformance test를 복사해 assertion 삭제 | Parity evidence 조작 |
| Emulator/mock pass를 actual provider evidence로 표기 | Provider semantics fake evidence |
| 빠르거나 싸다는 이유로 semantic/security failure 허용 | Gate 우선순위 위반 |
| Test-only threshold를 production performance default로 사용 | `Q-BENCH-02`/operations gate 임의 해소 |
| Shadow run에 publication/traffic/state mutation 허용 | Cutover를 Phase 12로 당김 |
| Phase 13 hybrid module/OR-Tools dependency/native distribution을 함께 구현 | `C-17` gate 위반 |
| Phase 11/12 whole-file reciprocal hash 저장 | Neighbor metadata cycle과 drift |
| Evidence manifest와 review가 서로 content digest를 포함 | 봉인 불가능한 reciprocal review cycle |
| Provider evidence가 conformance manifest/review/receipt를 역참조 | Forward manifest와 cycle을 만들어 immutable seal 순서 붕괴 |
| Conformance manifest가 review/acceptance receipt를 선참조 | Review 결과가 reviewed content를 바꾸는 자기참조 |
| `*IT`를 Surefire `-Dtest`로만 선택하고 0-test를 허용 | Integration false-green |

## 18. Blockers, last safe state, restart와 handoff

### 18.1 Blocker ledger

| Blocker | Owner | Last safe state | Restart condition |
|---|---|---|---|
| Phase 08~10 unaccepted | 각 Phase owner + scheduler | Contract catalog/red specs | Accepted evidence/review refs와 residual blocker 해소 |
| Non-ambient authorization + lossless failure carrier 없음 | Phase 08/09 Application/Security/Storage | Backend call/existence disclosure 0 | Approved binding/lifecycle와 exhaustive operation×failure tests |
| Worker committed-outcome authority 미승인 | Phase 08/09/10 Storage/Coordinator | Verified orphan outcome, committed authority 없음 | Approved exact create/CAS primitive와 concurrency/response-loss tests |
| Action/run-state/publication precondition 분리 미승인 | Phase 08/09/10/11 Application/Storage/Coordinator | Pure reducer + immutable result; publication off | Exact current-pending authorization, distinct token/read/CAS contract와 conformance |
| Same-run-state cancel/publication fence 미승인 | Phase 08/10/11 Application/Coordinator/AWS | Durable cancel request only; new side effect off | Same-version race/too-late/crash tests 공동 승인 |
| Durable monotonic deadline restart 미승인 | Phase 08/10/11 Application/Coordinator/Ops | `TEST_ONLY` virtual clock; deadline provider path off | Durable deadline ADR와 restart fault tests |
| Phase 11 handoff not ready | Phase 11 owner/reviewer | Standalone AWS evidence schema review | Accepted review + manifest-independent immutable AWS evidence digest |
| Candidate provider/axis 미승인 | Product/Platform/Ops | Provider-neutral suite only | Exact adoption decision |
| Candidate environment authority 없음 | Platform/Security | Offline design/fake only | Isolated scope/credential/budget/cleanup approval |
| Required CAS/consistency unsupported | Storage/Platform | AWS/local accepted path | Different provider/approved mechanism; port weakening 금지 |
| Security control evidence 없음 | Security/Data governance | No deploy/activation | Negative matrix/encryption/tenant evidence |
| Performance policy/수치 open | Performance/FinOps/Ops | Measurement schema, experiment-only run | Workload-based approved envelope |
| `Q-BENCH-02` open | Benchmark/Quality | Explicit test-only values | Calibration and approval |
| `C-17` Phase 13 gated | Product/Algorithm/Architecture | Substitution evidence only | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, separate C-17 scope + OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback approval |
| `Q-VAR-01` deferred | Product/Domain/Algorithm | Current pair/terminal/bank | Register restart evidence and approval |
| Production authority 없음 | Product/Platform/Security/Ops | Non-prod evidence/recommendation | Separate production cutover authority |

해소된 감사 항목은 current blocker 수에 포함하지 않는다.

| Audit item | Status | Current effect |
|---|---|---|
| Phase 11/12 reciprocal conformance-evidence cycle | `RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2` | Phase 11 evidence/receipt → Phase 12 manifest back-reference 0; regression test만 유지하며 implementation/evidence/handoff를 승인하지 않음 |

Blocker가 있어도 문서 review는 멈추지 않는다. Implementation은 last safe state까지만
진행하고 mock/legacy/default로 gate를 닫지 않는다.

### 18.2 Phase 11에서 받는 handoff

[Actual Phase 11](phase-11-aws-reference-distribution.md) v1.3의 §6.1~§6.3,
§14.1~§14.2, §17과 §18.2를 stable section으로 대조했다. Adjacent section digest는
기록하거나 acceptance에 사용하지 않는다. 구현 entry에서는 아래 contract와 residual
blocker 해소 evidence가 accepted인지 다시 확인한다.

```text
Phase11EvidenceManifest(provider = AWS_REFERENCE)
  # immutable standalone evidence
  contentDigest
  # no ProviderConformanceManifest/review/receipt reference
Phase11PostReviewAcceptanceReceipt
  phase11EvidenceManifestDigest
  independentReviewDigest
  contentDigest
  # no ProviderConformanceManifest reference
logical port/artifact/state schema versions
identity/retry projection
non-ambient authorization and lossless failure projection
exact-current-pending action authorization
distinct publication-pointer precondition
same-run-state cancellation/publication fence
durable deadline restart contract
storage/workflow/compute contract case catalog
local oracle and AWS actual evidence refs
provider metadata exclusion projection
security/operations/cost evidence schema
known AWS limitations/open gates
productionAuthority = false
```

`RESOLVED_BY_PHASE11_V1_2_AND_PHASE12_V1_2`: Phase 11 evidence/receipt는 Phase 12
manifest를 역참조하지 않고, Phase 12만 standalone AWS evidence digest와 별도
candidate evidence digest를 pre-review `ProviderConformanceManifest`에서
forward-reference한다. Schema cycle은 닫혔다.

그럼에도 Phase 11 review verdict가 `CHANGES_REQUIRED`이고 implementation/evidence가
`NOT_STARTED/NOT_PRODUCED`라 accepted evidence/receipt가 없다. 따라서 Phase 12
handoff는 계속 `NOT_READY`이며 이 이유는 reciprocal schema cycle이 아니라 실제
evidence와 나머지 entry gate 부재다.

Phase 12는 Phase 11에 자신의 whole-file/section fingerprint를 요구하지 않는다.

### 18.3 Phase 13으로 넘기는 bounded evidence

[Actual Phase 13](phase-13-optional-hybrid-route-selection.md) v1.5와
[완료된 review](../reviews/phase-13-review.md)는 현재
`PASS_WITH_RESIDUAL_BLOCKERS/GATED_NOT_STARTED_NOT_ACCEPTED`다. Baseline inspection
뒤 shared checkout에 나타났고 `READY_FOR_REVIEW`였다는 기록은 historical authoring
snapshot이다. Current 문서의 §4.1과 §14.2는 accepted Phase 12 review,
contract/migration/parity, security/cost/
operations, digest preservation, locator leakage 0과 rollback point를 모두 요구한다.
Phase 13 안의 “Phase 12 planned/absent” 표기는 그 문서 authoring baseline의
inventory다. 이 review가 현재 actual이어도 verdict는 `CHANGES_REQUIRED`이고 Phase 12
implementation/evidence/accepted handoff가 없으므로 Phase 13 entry를 충족하지 않는다.
Phase 12는 그 entry receipt에 다음 인프라 의사결정 자료만 더 구체화해 넘긴다.

```text
Phase12ToPhase13SubstitutionEvidence
  providerConformanceManifestContentDigest
  independentPhase12ReviewContentDigest
  phase12AcceptanceReceiptContentDigest
  selectedOrEvaluatedExecutionEnvelope
  workerRuntimeLimitsAndObservedHeadroom
  artifactSizeTransferLatencyAndCostEnvelope
  retryDeadlineCancellationSemanticsRef
  serializationReplayLimitsRef
  observabilityAndSecurityControlRef
  unsupportedGatedOpenDifferences[]
  evidenceApplicability = INFRASTRUCTURE_DECISION_INPUT_ONLY
  c17Approval = NOT_GRANTED_BY_THIS_HANDOFF
  ortoolsActivationOrDistributionApproval = NOT_GRANTED_BY_THIS_HANDOFF
  hybridImplementationAuthority = false
  productionCutoverAuthority = false
```

이 evidence는 optional hybrid의 worker runtime, artifact movement, memory/concurrency와
operational viability 판단에 사용할 수 있다. Route pool schema, MIP projection,
OR-Tools version/config/native distribution 선택, strictly-better adoption 또는 production activation을
승인하지 않는다. Phase 13 owner는 Phase 14A ALNS benchmark acceptance receipt를
먼저 확보하고 별도 `C-17` entry gate를 충족해야 한다.

### 18.4 Future adoption/cutover boundary

`EligibleForSeparateAdoptionReview`는 provider가 production default라는 뜻이 아니다.
별도 owner가 workload/security/operations/cost, versioned rollout, in-flight state,
traffic/pointer switch와 rollback authority를 승인해야 한다. Phase 12 evidence는 그
입력 중 하나일 뿐이다.

## 19. Source → requirement → test → evidence traceability

| Source | Phase 12 requirement | Work/test | Evidence |
|---|---|---|---|
| Master §4.4~4.6, §15.10 | Provider가 lifecycle/authority를 바꾸지 않음 | WP12-1/4/5; state/application parity | `E-P12-PROVIDER-CONTRACT` |
| Master §13 | Retry/deadline/replay identity | `ProviderRetryIdentityIT`, reproducibility tests | `E-P12-PARITY` |
| Master §14 | Both-gate result authority | Publication/fault/corruption tests | `E-P12-PROVIDER-CONTRACT` |
| Master §16.3 | AWS 이외 topology 별도 parity/approval | WP12-0/2/9 | Entry/adoption/review refs |
| Domain §7~10, §15 | Problem/profile/result provider-neutral identity | Schema/fingerprint/canonical codec tests | `E-P12-PARITY` |
| Domain §16~18 | Typed failure/open/gated/deferred | Failure taxonomy and manifest assertions | Evidence limitations |
| Architecture §2~3 | Module DAG, identity, retry/cancel | Architecture/workflow/worker tests | `E-P12-PROVIDER-CONTRACT` |
| Architecture §5 | Ports/artifact/failure/security/publication | Storage/application/security tests | Contract/security evidence |
| Integrated §16.1~16.6 | Orthogonal substitution/minimum axis | Adoption decision, WP12-2 | Capability/axis matrix |
| Integrated §16.7 | Digest-preserving migration | `ProviderArtifactMigrationIT` | `E-P12-MIGRATION` |
| Integrated §16.9~16.10 | Workflow/compute suite and gate | WP12-4/8 | `E-P12-PARITY` |
| Integrated §19~21 | Config/provenance/failure/retry | WP12-5/6 tests | Operations/replay evidence |
| Integrated §22~25 | Test/invariants/anti-pattern/deferred | Full catalog/review checklist | Review ref |
| Open questions `Q-INFRA-01` | AWS is reference, not production completion | AWS/candidate role assertions | Comparison manifest |
| `Q-BENCH-02` | Official values remain open | Missing-policy returns gated | Open value list |
| `Q-VAR-01` | Deferred variants stay inactive | No domain invariant relaxation | Architecture evidence |
| Phase 08~11 latest reviews/Phase 11 §6.1~§6.3, §17~§18.2 | Access/failure/action/publication/cancel/deadline blocker와 shared schemas | WP12-0/1/3~6/8 | Contract resolution + entry + comparison |
| Actual Phase 13 §4.1/§6.5/§14.2 | Infra evidence only, no hybrid authority | Handoff manifest assertions | Bounded handoff |
| Actual legacy GCP inventory | Historical gap characterization only | Legacy characterization negative fixture | Not parity evidence |

## 20. Reviewer checklist

- [ ] 사용자 고정 source authority와 최신 `Q-INFRA-01 RESOLVED`를 보존했다.
- [ ] Candidate provider/axis를 승인 없이 선택하거나 production default로 쓰지 않았다.
- [ ] AWS는 reference subject이며 각 provider를 독립 oracle로 검증하고 구현/cutover 완료로 과장하지 않았다.
- [ ] Legacy GCP는 inventory/evidence candidate일 뿐 authority가 아니다.
- [ ] Core/domain/identity/artifact schema/result authority 변경이 0이다.
- [ ] Storage/coordinator/worker/application conformance suite가 하나다.
- [ ] Capability/semantic difference가 unsupported/gated/violation으로 명시된다.
- [ ] Distinct publication precondition, exact pending action, CAS/consistency/idempotency/retry/restart deadline/same-state cancel/completeness가 모두 검증된다.
- [ ] Failure taxonomy, serialization/replay, fault/corruption test가 exact하다.
- [ ] Non-ambient authorization, lossless failure, IAM-equivalent/encryption/tenant/redaction/observability evidence가 있다.
- [ ] Performance/cost 수치와 threshold가 open/proposed/approved로 분리된다.
- [ ] 모든 WP에 prerequisite/target/task/verification/expected/failure rollback/handoff가 있다.
- [ ] Exact test class/method, fixture/oracle, red→green, commands/pass criteria가 있다.
- [ ] Standalone provider evidence → forward-reference conformance manifest → review → receipt DAG와 exact anti-cycle tests가 exit gate/DoD/resolved audit ledger에 연결된다.
- [ ] Canonical 네 문서 fingerprint만 provenance로 사용하고 Phase 11/13은 stable section citation으로만 대조했다.
- [ ] Actual Phase 13의 §4.1/§6.5/§14.2를 대조하고 substitution evidence만 넘기며 hybrid/cutover를 당기지 않았다.
- [ ] `Q-BENCH-02`, `C-17`, `Q-VAR-01` 상태를 임의로 닫지 않았다.
- [ ] Fake implementation/deployment/evidence 또는 console-only completion claim이 없다.
