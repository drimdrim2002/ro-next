# Phase 08 사람용 구현 가이드 — Application ports와 local reference runtime

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
guide_scope: Phase 08 only
canonical_phase_count: 15
phase: "08"
phase_name: application-ports-local-runtime
canonical_phase_document: docs/implementation/phases/phase-08-application-ports-local-runtime.md
canonical_phase_review: docs/implementation/reviews/phase-08-review.md
canonical_phase_document_status: REVIEWED_WITH_CORRECTIONS
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: BLOCKED_NOT_IMPLEMENTED
phase_acceptance_status_observed: NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status: BLOCKED_BY_UNACCEPTED_PREDECESSORS_AND_CROSS_PHASE_CONTRACTS
handoff_status_observed: NOT_READY
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_on_branch: codex-implementation
inventory_observed_date: 2026-07-29
worktree_revalidated_at: 2026-07-29T02:35:33+09:00
worktree_revalidation_status: PHASE00_REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS_NOT_ACCEPTED
worktree_revalidation_scope: modified root build/deployment/readme/progress plus untracked wrapper/build/legacy/rpdptw/human-guides paths
source_fingerprint_scheme: current_and_dated_input_git_objects_plus_head_baseline_and_timestamped_live_objects
phase00_live_status_observed: REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS
phase00_review_status_observed: INDEPENDENT_REVIEW_02_COMPLETED_CHANGES_REQUIRED
phase00_evidence_status_observed: REJECTED_PENDING_FIX_02_REGENERATION
phase00_acceptance_status_observed: NOT_ACCEPTED
prerequisite_phases:
  - "00"
  - "01"
  - "02"
  - "03"
  - "04"
  - "05"
  - "06"
  - "07"
source_sections_and_fingerprints:
  docs/README.md: "current top-level path map and normative-status notice | live 13f1b3b2dea038b8e0b466c138f5f59299413125"
  docs/master-design.md: "§1.5, §2.2~2.4, §4.1~4.7, §10, §13~14.1, §15.10, §16~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/domain-design.md: "current §1, §3, §10, §13~18 | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "current §5~7, §10, §12, §17~19 | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/architecture-domain-implementation-design.md: "§1.3~1.5, §2~3, §12, §13.1~13.4, §19~25, §27~29 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4 and exact Q-OBJ-01/Q-BENCH-02/Q-INFRA-01/Q-VAR-01 rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/2026-07-26-domain-design.md: "HISTORICAL_IMPLEMENTATION_BASELINE_ONLY | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "HISTORICAL_IMPLEMENTATION_BASELINE_ONLY | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/implementation/README.md: "§0~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~4, Phase 07~10, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "mutable status provenance, not design authority | HEAD 250aa90ae568a6b32ec905fa5ee456d430ff72cf; live-at-revalidation 0419f69199b3140dd44020f78278b1352e6517b8"
  pom.xml: "mutable live implementation inventory only | HEAD f8a411eadd4a5c01d8dd09fdea462738ca63d65f; live-at-revalidation 1dc675ba17b7f2202f34a22131f152cc2868b075"
  docs/implementation/phases/phase-07-independent-verification-final-result.md: "§7.1~8.5, §13.2~15.2 | 1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115"
  docs/implementation/phases/phase-08-application-ports-local-runtime.md: "§1~18 | 2aff093a6f2728470a7ccbb22b7e1a1a71f5b963"
  docs/implementation/phases/phase-09-object-storage-no-database.md: "§3~5, §7~8.8, §14~15.1 | 99a5b0df5531a65964272f423cc0ccca4d4f1430"
  docs/implementation/reviews/phase-08-review.md: "§1~9 | 7983d3b09e2f4c45b8cd132e3627c053c9e3e8ce"
  docs/implementation/human-guides/reviews/phase-08-review.md: "F-HG-P08-001~005 correction input | live 51374cb0930a25516e3c752631f317faefee2057"
expected_reader:
  - Java의 interface, record, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW의 route, time window, capacity propagation을 구현해 보았다
  - RPDPTW의 pair identity, application port, CAS publication과 two-gate result는 처음 접한다
owner_roles:
  implementation: RPDPTW Application/Local Runtime owner
  upstream_result: Phase 07 Verification/Result owner
  upstream_semantics: Phase 01~06 Domain/Profile/Algorithm owners
  local_adapters: Local CLI/Filesystem adapter owner
  security: Application Security owner
  downstream_storage: Phase 09 Object Storage owner
  downstream_coordination: Phase 10 Coordinator owner
  independent_oracle: Phase 08 independent test/oracle owner
  review: independent Phase 08 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P08-PORT
  - E-P08-LOCAL-E2E
  - E-P08-IDEMPOTENCY
```

> 이 문서는 사람이 Phase 08의 의미를 배우고, entry gate가 열린 뒤 실제로 구현하고, 독립 evidence로 완료를 판정하기 위한 교육형 작업 지시서다. 현재 shared checkout에는 동시 작업 중인 **미승인 Phase 00 reactor/wrapper/architecture-test/legacy-characterization 작업**이 보이지만 Phase 08의 concrete port, service, CLI, filesystem adapter, test 또는 evidence는 없다. 아래 Java 이름과 signature는 별도 표시가 없는 한 **제안 후보(proposed)**이며 존재하는 API, 승인된 public/wire contract 또는 구현 완료 evidence가 아니다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 08은 Phase 01~07의 provider-neutral semantic pipeline을 application use case 뒤에 한 번만 조립하고, 명시적 설정을 쓰는 local reference runtime에서 **검증 가능한 artifact·상태·취소·publication 의미를 보존한 채 request부터 verified result retrieval까지 실행하는 단계**다.

Phase 08은 solver를 새로 만드는 단계가 아니다. 앞 Phase의 해석을 controller나 filesystem에 맞춰 바꾸는 단계도 아니다. 핵심은 다음 세 가지다.

1. 외부 호출자는 inbound use case만 보고, application은 outbound port만 본다.
2. Local filesystem과 same-process worker는 port의 첫 reference implementation일 뿐 semantic authority가 아니다.
3. Phase 07의 두 verifier를 모두 통과한 `PublishableResult`만 immutable artifact와 publication CAS 뒤에 정상 결과가 된다.

## 2. 큰 그림과 필요한 이유

### 2.1 CVRPTW solver에 단순 CLI를 붙이는 일과 다른 이유

CVRPTW 프로그램에 `main()`을 하나 붙일 때는 입력 파일을 읽고 solver를 호출한 뒤 결과 JSON을 쓰는 것으로 충분해 보일 수 있다. RPDPTW에서는 그 shortcut이 다음 경계를 동시에 무너뜨린다.

- Raw input을 solver가 다시 해석하면 Phase 01 canonicalization이 사라진다.
- Pickup과 delivery 중 하나만 남은 candidate를 controller가 결과로 직렬화할 수 있다.
- Search cache나 `feasible=true`를 믿으면 Phase 07 독립 검증이 무력화된다.
- File 존재를 status로 쓰면 partial write와 정상 publication을 구분할 수 없다.
- Retry 때 UUID·seed·work가 바뀌면 같은 요청이 다른 solve가 된다.
- Timeout을 `MAX_STEPS_REACHED`로 바꾸면 platform failure가 정상 품질 종료로 보인다.
- `TenantId` 문자열만 믿으면 다른 tenant artifact의 존재 여부가 새어 나갈 수 있다.

Phase 08의 기준 흐름은 다음과 같다.

```text
explicit local request/config
→ versioned inbound adapter
→ canonicalization
→ normalization + prepared travel + exact profile binding
→ immutable SolveSnapshot + ExecutionManifest
→ deterministic ALNS solve
→ Phase 07 candidate verification
→ finalization/audit
→ Phase 07 result verification
→ PublishableResult
→ immutable put + verified read
→ publication precondition/CAS
→ exact status/result retrieval
```

화살표 사이의 산출물은 “비슷한 Java 객체”가 아니라 version, identity, content digest와 authority가 있는 계약이다. 뒤 단계는 앞 단계의 raw source나 hidden default로 돌아가지 않는다.

### 2.2 Local reference runtime의 역할

Local runtime은 cloud runtime의 축소판이 아니다. AWS가 없어도 application semantics를 검증할 수 있는 **기준 실행(reference execution)**이다.

```text
같아야 하는 것:
  canonical input/problem/travel/profile/manifest fingerprint
  candidate/replay fingerprint
  candidate PASS + result PASS identity
  final semantic result fingerprint
  canonical payload digest/length
  terminal meaning

달라도 되는 것:
  absolute workspace path
  local opaque locator
  process/thread ID
  observation timestamp와 elapsed
  temporary filename
  semantic 순서가 아닌 log delivery order
```

두 빈 workspace에서 같은 manifest를 실행했는데 result digest가 다르면 “local 차이”가 아니라 재현성 결함이다. 반대로 elapsed가 다르다고 result가 다르다고 판정해서도 안 된다.

### 2.3 Canonical 15 Phase 안의 위치

이 저장소의 canonical Phase는 **00~14, 총 15개**다. Phase 08은 ALNS-first correctness 경로의 마지막 local 실행 단계이며, Phase 09/10의 producer다.

| Phase | 주제 | 주요 producer → consumer 계약 |
|---:|---|---|
| 00 | Build와 architecture 뼈대 | Reactor, module/package boundary, wrapper와 architecture guard를 모든 Phase에 제공 |
| 01 | Canonical input와 정규화 | Versioned canonical facts, typed input error와 immutable normalized values를 Phase 02/08에 제공 |
| 02 | Prepared travel과 immutable problem | Complete directed `PreparedTravel`, `ProblemInstance`, dense identity를 Phase 03~08에 제공 |
| 03 | Propagation과 evaluation kernel | Cache-free route/solution evaluation과 comparator authority를 Phase 04~08에 제공 |
| 04 | Capability와 customer profile | Exact `BoundProfile`, dependency closure와 `SolvePlan`을 Phase 05~08에 제공 |
| 05 | Pair insertion과 initial portfolio | Atomic pair evaluator, stable route/bank, 최대 8개 initial candidate를 Phase 06/07에 제공 |
| 06 | COW ALNS와 reproducibility | `CommittedCandidate`, replay/work/termination lineage를 Phase 07에 제공 |
| 07 | Independent verification과 final result | Top-level `Publishable \| Rejected`와 nested `VerificationRejected \| GateIncomplete`를 **Phase 08**에 제공 |
| **08** | **Application ports와 local runtime** | **Use case, identities, lifecycle, local artifact/state/publication reference와 deterministic E2E를 Phase 09/10/14A에 제공** |
| 09 | No-DB object storage | Phase 08 storage 의미를 object-common/provider conditional semantics로 구현해 Phase 10/11에 제공 |
| 10 | Provider-neutral coordinator | Declared worker completeness, durable round state와 stable fan-in을 Phase 11/14에 제공 |
| 11 | AWS reference distribution | S3 + Step Functions + Lambda parity/security/rollback evidence를 Phase 14B에 제공 |
| 12 | Provider substitution | 승인된 provider 축만 same contract suite와 migration evidence로 교체하는 gated branch |
| 13 | Optional hybrid route selection | **Phase 14A receipt와 C-17 별도 승인 뒤에만** route pool/CP-SAT/fallback을 조건부 제공 |
| 14 | 14A benchmark / 14B official cutover | 14A는 ALNS benchmark acceptance, 14B는 official/production authority를 소유 |

ALNS-first critical path:

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → [08] → 14A
```

AWS ALNS-only production 후보 경로:

```text
08 → 09 → 10 → 11
14A + 11 → 14B
```

조건부 경로:

```text
10 → 12                              # provider adoption approval가 있을 때만
14A acceptance + C-17 approvals → 13 # optional
13 → 14B                             # official hybrid manifest를 선택했을 때만
```

Phase 13은 Phase 08의 선행조건이 아니다. Phase 08 local E2E 한 번이 성공해도 Phase 13 gate는 열리지 않는다. Phase 14A의 유효한 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`와 별도 `C-17` 승인이 필요하다. Phase 14A receipt도 Phase 14B production authority가 아니다.

### 2.4 Phase 08의 producer와 consumer

```text
Phase 01~04  canonical facts + prepared travel + bound profile
Phase 05~06  portfolio + deterministic committed candidate
Phase 07     top-level Publishable/Rejected + nested two-way rejection + both-gate evidence
       \         |         /
        \ exact identity/content equality
         ↓
Phase 08 application use case
  ├─ application-owned identity/lifecycle/failure
  ├─ local artifact/state/publication adapter
  ├─ same-process worker + cooperative cancellation
  └─ exact status/result retrieval
         ↓
Phase 09 storage semantics / Phase 10 execution seam / Phase 14A run evidence
```

| 경계 | Producer가 보장할 것 | Phase 08/consumer가 하면 안 되는 것 |
|---|---|---|
| Phase 01~04 → 08 | Canonical input, complete travel, exact profile/version/preset, immutable snapshot authority | Raw JSON, coordinate/speed, `latest` profile 또는 customer fallback 재해석 |
| Phase 05~06 → 08 | Accepted solver service, stable route/bank, replay/work/termination identity | Search cache, mutable COW state, raw objective를 application truth로 사용 |
| Phase 07 → 08 | Top-level `Publishable \| Rejected`와 `Rejected` 내부 `VerificationRejected \| GateIncomplete`의 손실 없는 2단계 output | 세 의미를 top-level 세 subtype처럼 switch, One-PASS success, incomplete disposition 합성, failure payload publication |
| Phase 08 → 09 | Logical key/ref/digest, artifact/state/publication semantics와 local contract evidence | Filesystem `Path`, S3 bucket/key, provider exception을 application contract로 넘김 |
| Phase 08 → 10 | `SolveId`, worker/attempt identity, cancellation/deadline, dispatcher/workflow seam | Multi-round completeness를 Phase 08 single worker가 미리 구현 |
| Phase 08 → 14A | Explicit manifest와 모든 run의 success/failure/resource/verifier evidence | 단일 성공 run을 benchmark acceptance로 승격하거나 실패 run을 누락 |

## 3. Source authority, fingerprint와 정확한 읽기 순서

### 3.1 충돌 해소 순서

구현 판단에는 다음 순서를 적용한다.

```text
사용자 고정 지시
→ docs/README.md가 지정한 current top-level path
→ canonical docs/master-design.md
→ question register의 exact Q-* 상태
→ current docs/domain-design.md의 의미
→ current docs/architecture-design.md의 배치
→ Integrated design의 15 Phase/no-DB/AWS 구조
→ implementation README/plan/Phase 08/review의 realization baseline
→ 날짜 고정 문서와 deprecated 자료의 historical cross-check
```

[현재 문서 지도](../../../README.md)는 `domain-design.md`와
`architecture-design.md`를 current top-level 진입점으로 지정한다. 이 문서 세트에 대한
사용자 고정 지시가 입력 권위를 부여하지만, current 두 문서 자체의 `REVIEW` metadata를
구현 acceptance나 production authority로 승격하지는 않는다.

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md), 날짜 고정 Domain/
Architecture, deprecated 문서와 `docs/codex/*`는 historical/realization-baseline
cross-check일 뿐 current path authority가 아니다. 날짜 고정 문서에 남은
`Q-INFRA-01 DEFERRED`, `25/1/2` 표기는 최신 [Canonical Master](../../../master-design.md)와
[question register](../../../master-design-open-questions.md)의
`Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`로 해소한다. 이것은
Phase 08에 AWS SDK를 넣거나 AWS 구현·배포·production을 완료했다는 뜻이 아니다.

### 3.2 검증 가능한 source fingerprint

Current/dated/implementation-baseline Git object는 metadata의
`inventory_observed_at_commit`에서 `git rev-parse HEAD:<path>` 또는 같은 bytes의
`git hash-object`로 얻었다. Mutable progress/POM은 correction timestamp의 live object를
별도로 병기한다. Source가 바뀌면 hash만 바꾸지 말고 인용 heading의 의미, 이 가이드의
requirement·WP·test·evidence 영향을 함께 review한다.

| Source | 시점/역할 | 직접 읽을 heading/section | Git object |
|---|---|---|---|
| [Current top-level map](../../../README.md) | `CURRENT_PATH_MAP`; path와 규범 지위 | 현재 최상위 진입점, 권장 읽기 순서, 문서 계층 | `13f1b3b2dea038b8e0b466c138f5f59299413125` |
| [Canonical Master](../../../master-design.md) | `CURRENT_CANONICAL_INPUT`; conflict order와 system meaning | §1.5, §2.2~2.4, [§4 구현 아키텍처](../../../master-design.md#4-구현-아키텍처와-책임-경계), §10, §13~14.1, §15.10, §16~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Current Domain](../../../domain-design.md) | `CURRENT_CANONICAL_INPUT`; domain/result/error 의미 | §1, §3, §10, §13~18 | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Current Architecture](../../../architecture-design.md) | `CURRENT_CANONICAL_INPUT`; module/app composition과 test 배치 | §5~7, §10, §12, §17~19 | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | `CURRENT_CANONICAL_INPUT`; 15 Phase realization | §1.3~1.5, §2~3, [§12 Phase 8](../../../architecture-domain-implementation-design.md#12-phase-8--application-ports와-local-reference-runtime), §13.1~13.4, §19~25, §27~29 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | `CURRENT_CANONICAL_INPUT`; exact gate state | §1~4, exact `Q-OBJ-01`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Dated Domain](../../../2026-07-26-domain-design.md) | `HISTORICAL_IMPLEMENTATION_BASELINE_ONLY` | 과거 section citation과 drift cross-check | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Dated Architecture](../../../2026-07-26-architecture-design.md) | `HISTORICAL_IMPLEMENTATION_BASELINE_ONLY` | 과거 module citation과 drift cross-check | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Implementation README](../../README.md) | `IMPLEMENTATION_BASELINE`; authority/DAG/status protocol | §0~7 | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | `IMPLEMENTATION_BASELINE`; Phase realization | §2~4, Phase 07~10, §8~15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress](../../execution-progress-and-results.md) | `LIVE_MUTABLE_STATUS_PROVENANCE`; semantic authority 아님 | §2, §5~10, scheduler authority와 Phase 00 remediation | HEAD `250aa90ae568a6b32ec905fa5ee456d430ff72cf`; live `0419f69199b3140dd44020f78278b1352e6517b8` |
| [Actual Phase 07](../../phases/phase-07-independent-verification-final-result.md) | `IMPLEMENTATION_BASELINE`; unaccepted producer contract | §7.1~8.5, §13.2~15.2 | `1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115` |
| [Canonical Phase 08](../../phases/phase-08-application-ports-local-runtime.md) | `IMPLEMENTATION_BASELINE`; target Phase design | 전체, 특히 §3~16과 §18 | `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` |
| [Actual Phase 09](../../phases/phase-09-object-storage-no-database.md) | `IMPLEMENTATION_BASELINE`; unaccepted consumer contract | §3~5, §7~8.8, §14~15.1 | `99a5b0df5531a65964272f423cc0ccca4d4f1430` |
| [Phase 08 review](../../reviews/phase-08-review.md) | `IMPLEMENTATION_BASELINE`; F-P08-001~013와 residual | §1~9 | `7983d3b09e2f4c45b8cd132e3627c053c9e3e8ce` |
| [Human-guide review](../reviews/phase-08-review.md) | `CORRECTION_INPUT`; F-HG-P08-001~005 | 전체 | live `51374cb0930a25516e3c752631f317faefee2057` |

Current와 dated input 사이의 semantic impact는 다음처럼 닫는다.

- Current Architecture §6.6/§7.1은 `apps/*`를 composition root로 지정하고
  local implementation을 `adapters/common` package로 둔다. Dated/Integrated/Canonical
  Phase 08의 더 세분화된 filesystem/distribution 제안은 current 배치를 override하지
  않는다. 이 가이드는 §8에서 `apps/cli`가 executable bootstrap/composition을 소유하고
  `adapters/common`의 local package를 주입하는 방향으로 고친다.
- Current Domain §13~16의 outcome/error/evidence 의미를 소비한다. 날짜 고정 section의
  이름이나 오래된 질문 상태를 새 result/failure/default로 구현하지 않는다.
- Implementation README/plan/Phase 문서는 realization baseline이다. Current map과
  충돌하는 module 배치나 상태를 현재 authority로 승격하지 않고 owner approval gate에
  남긴다.

`execution-progress-and-results.md`와 root POM은 concurrent scheduler/Phase 00 작업으로
live bytes가 바뀌는 관찰 대상이다. 2026-07-29T02:35:33+09:00 snapshot의 live Git
objects는 각각 `0419f69199b3140dd44020f78278b1352e6517b8`,
`1dc675ba17b7f2202f34a22131f152cc2868b075`다. Phase 00은
`REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS`, independent review 02는
`COMPLETED_CHANGES_REQUIRED`, replacement evidence는
`REJECTED_PENDING_FIX_02_REGENERATION`, acceptance receipt는 `NOT_PRODUCED`다.
빠르게 변하는 live object는 provenance일 뿐 acceptance criterion이 아니다. Phase 00
`NOT_ACCEPTED`이므로 Phase 08 entry는 계속 닫혀 있다.

재검증 예:

```bash
git rev-parse HEAD
git rev-parse HEAD:docs/master-design.md
git rev-parse HEAD:docs/implementation/phases/phase-08-application-ports-local-runtime.md
git rev-parse HEAD:docs/implementation/reviews/phase-08-review.md
git hash-object docs/implementation/execution-progress-and-results.md
```

인접 Phase 문서의 whole-file digest를 서로의 acceptance 조건으로 만들지 않는다. Entry에서는 인용 section의 semantic diff와 accepted implementation artifact/evidence identity를 확인한다.

### 3.3 구현 전 정확한 읽기 순서

| 순서 | 읽을 곳 | 답해야 할 질문 | 확인할 module/file/evidence |
|---:|---|---|---|
| 1 | [Current top-level map](../../../README.md) | Current path와 각 문서의 규범 지위는 무엇인가? | Current canonical 5 path, dated input의 historical 지위 |
| 2 | Master §4, §13~14.1, §15.10 | Application은 어떤 의미를 보존하고 무엇을 publish할 수 있는가? | Logical port, both-gate, `RM-8` |
| 3 | Question register exact rows | Exact preset, official 수치, AWS 선택, deferred variant 상태는 무엇인가? | `Q-OBJ-01`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` |
| 4 | Current Domain §1/§3/§10/§13~18 | Pair/route-bank/outcome/termination/incomplete의 정확한 의미는 무엇인가? | `ProblemInstance`, `PreparedTravel`, `BoundProfile`, `PublishableResult` 의미 |
| 5 | Current Architecture §5~7/§10/§12/§17~19 | Module DAG, app composition root, local reference, retry/failure/test 경계는 어디인가? | `rpdptw-application`, `adapters/common`, `apps/cli` |
| 6 | Integrated design §3, §12~13.4, §19~25 | Phase 08 realization baseline, no-DB storage seam, security/failure/test는 무엇인가? | Current 배치와 충돌하는 proposed split, exact-key/CAS 원칙 |
| 7 | [Implementation README §0~7](../../README.md) | 15 Phase, ALNS-first DAG와 status authority는 무엇인가? | Phase 08 canonical filename/review, scheduler rule |
| 8 | Master Realization Plan Phase 07~10, §8~15 | Entry/exit/evidence/DoD/rollback은 무엇인가? | `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY` |
| 9 | Actual Phase 07 §7.6~8.5/§15.2 | Top-level 2-variant와 nested rejection 2-variant를 어떻게 손실 없이 소비하는가? | Accepted `Phase07HandoffManifest`, `Publishable \| Rejected(VerificationRejected \| GateIncomplete)` |
| 10 | Canonical Phase 08 전체 | Exact proposed types, state, tests, WP, command와 blocker는 무엇인가? | §3 entry, §7 contracts, §11 tests, §12 WP |
| 11 | Phase 08 review 전체 | 어떤 safe correction이 적용됐고 무엇이 아직 막혀 있는가? | F-P08-001~013, residual cross-Phase decisions |
| 12 | Actual Phase 09 §7~8.8/§15.1 | Storage consumer가 요구하는 closure/failure/precondition은 무엇인가? | Phase 08/09 shared signature decision |
| 13 | Execution Progress §5~10 | 현재 누가 무엇을 구현 중이며 누가 status를 바꿀 수 있는가? | Phase 00 live remediation; Phase 08 여전히 blocked |
| 14 | 실제 repository inventory | Target path가 실제로 존재하고 concrete type/test가 있는가? | Root/app POM, package-info, adapters/apps/distributions, evidence |

문서, POM, package 디렉터리가 존재한다는 사실은 Phase 08 Java 구현이나 evidence가 존재한다는 뜻이 아니다.

## 4. RPDPTW primer, 용어집, identity와 lifecycle

### 4.1 CVRPTW의 customer와 RPDPTW의 request

CVRPTW에서는 customer visit 하나가 제거·삽입 단위인 경우가 많다. RPDPTW에서는 `Request`가 pickup과 delivery 의미를 함께 소유한다.

| 용어 | 의미 | 섞으면 생기는 결함 |
|---|---|---|
| `Order` | 외부 business 입력 표현 | Solver pair identity와 동일시 |
| `Request` | 원자 운송 업무와 pair ownership 단위 | Pickup 또는 delivery 하나로 축소 |
| `Node` | Terminal/pickup/delivery service 정의 | Physical location과 합침 |
| `PhysicalLocation` | Directed travel의 endpoint | 같은 장소의 서로 다른 service node를 합침 |
| `Visit` | Route에서 node가 나타나는 occurrence | Node 정의와 동일시 |
| `Route` | Concrete vehicle + terminal policy + ordered visits | Request 집합이나 vehicle class로 축소 |
| `SearchRequestBank` | Search 중 route 밖에 있는 request ID 집합 | Final `UNASSIGNED` outcome이나 reason 저장소로 사용 |

Real pickup-delivery는 pickup에서 load가 증가하고 delivery에서 감소한다. Delivery-only는 출발 전 initial load를 소유하며 logical pickup이 가짜 travel/service visit을 만들지 않는다.

```text
capacity = 10

가능:
P1(+6) → D1(-6) → P2(+5) → D2(-5)
load  6          0          5          0

불가능:
P1(+6) → P2(+5) → D1(-6) → D2(-5)
load  6         11          5          0
```

방문 집합이 같아도 순서가 다르면 feasibility가 다르다. Application은 route를 재계산하지 않지만, 이 의미를 가진 immutable snapshot과 verified result만 전달해야 한다.

### 4.2 Stable solution 불변조건

모든 stable search/result 경계에서 request마다 다음을 동시에 만족한다.

1. Real pickup과 delivery는 같은 concrete vehicle route에 있다.
2. 필요한 physical visit은 정확히 한 번 존재한다.
3. Pickup position은 delivery position보다 앞선다.
4. Request는 완전한 route pair 또는 bank 중 정확히 하나에 있다.

```text
routeOwnerCount(request)
+ bankMembershipCount(request)
= 1
```

Partial pair, route+bank duplicate, 둘 다 없음은 낮은 품질이나 `UNASSIGNED`가 아니라 implementation defect다. Phase 08은 그런 candidate를 safe error로 보존해야 하며 빈 정상 결과로 바꾸면 안 된다.

### 4.3 Application identity를 문자열 하나로 생각하지 않기

| Identity | 무엇을 고정하는가 | Retry에서 바뀌어도 되는 것 | 금지 입력 |
|---|---|---|---|
| `SubmissionId` + `IdempotencyKey` | 한 caller logical submission | 없음 | Random UUID fallback |
| `SubmissionCommandFingerprint` | Input/profile/manifest의 versioned semantic projection | Algorithm version migration이 승인된 새 contract만 | Path, mtime, env, clock |
| `SolveId` | Accepted submission에서 derivation된 solve | 승인된 derivation version migration만 | Retry마다 새 UUID |
| `ManifestFingerprint` | Snapshot/build/runtime/algorithm/seed/work/limits | 없음 | Hidden default |
| `WorkerRunId` | Solve/manifest/assignment/warm start | 없음 | Retry seed/work 변경 |
| `AttemptId` | 같은 logical worker의 platform attempt | Retry 때 새 값 허용 | Worker identity 대체 |
| `ArtifactKey` | Tenant/kind/logical artifact | 없음 | Provider locator |
| `ContentDigest` | Algorithm/version과 exact content bytes | 없음 | Untagged hash string |
| `StateVersion` | 한 state object의 optimistic CAS fence | Successful transition에서만 새 값 | Publication pointer token과 재사용 |
| `PublishedResultRef` | Both-gate result와 exact payload/artifact closure | Same-digest response-loss 수렴 | “가장 최신 파일” |

`SolveId` derivation algorithm과 fingerprint algorithm/version은 **PROPOSED/OPEN**이다. Review/ADR 전 production default를 고르지 않는다. Test-only 알고리즘은 fixture/config/evidence에 `TEST_ONLY`로 분류하고 production 정책으로 승격하지 않는다.

### 4.4 Artifact, state, observation의 lifecycle

```text
immutable semantic artifact:
  create once → digest/length/schema verify → exact ref로만 읽기

mutable authority:
  current version read → legal transition 검사 → compare-and-set

observation:
  elapsed/path/process/log metadata → semantic fingerprint 밖
```

Application lifecycle 후보:

```text
NEW
→ SUBMITTED
→ CANONICALIZING
→ PREPARING
→ PREPARED
→ SOLVING
→ VERIFYING_FINAL_RESULT
→ PUBLISHING
→ SUCCEEDED
```

예외 branch:

```text
CANONICALIZING → REJECTED_INPUT
PREPARING      → BINDING_FAILED
non-terminal   → CANCEL_REQUESTED → CANCELLED
execution      → WATCHDOG_REACHED | RESOURCE_LIMIT_REACHED
               | PLATFORM_TIMEOUT | FAILED
VERIFYING_FINAL_RESULT
               → PUBLICATION_REJECTED
               | VERIFICATION_INCOMPLETE
PUBLISHING     → PUBLICATION_REJECTED
```

Application은 Phase 07 내부의 candidate verify/finalize/result verify substage를 각각 outer state로 관찰한 척하지 않는다. 현재 handoff는 `Phase07Output` 하나이므로 outer state는 `VERIFYING_FINAL_RESULT` 하나다. Exact failure stage와 last safe identity는 Phase 07 output variant가 보존한다.

### 4.5 Phase 07 output을 손실 없이 소비하기

```text
Phase07Output
  ├─ Publishable(PublishableResult)
  └─ Rejected
       ├─ VerificationRejected(
       │    stage,
       │    VerificationDisposition,
       │    safe VerificationFailure,
       │    optional candidate PASS)
       └─ GateIncomplete(
            stage,
            SafeIncompleteFailure,
            optional candidate PASS,
            LastSafeIdentity)
```

`VerificationRejected`는 완료된 semantic rejection이다. `GateIncomplete`는 검증 과정이
authority/operation interruption 때문에 끝나지 못한 상태다. Java dispatch 경계는
의미상 세 결과를 top-level 세 subtype으로 평탄화하지 않는다. 첫 exhaustive switch는
`Phase07Output.Publishable | Phase07Output.Rejected`, 두 번째 switch는
`FinalResultRejection.VerificationRejected | FinalResultRejection.GateIncomplete`다.
Application 외부 outcome은 이 2단계 내부 구조를 success/rejected/interrupted라는 세
의미로 mapping할 수 있지만 upstream subtype ownership은 바꾸지 않는다.
`GateIncomplete`에 존재하지 않는 `VerificationDisposition`을 합성하거나 generic
rejection으로 축소하지 않는다. 두 rejection 모두 정상 route/outcome/payload를
publish하지 않는다.

### 4.6 핵심 application/local 불변조건

1. Application public contract에 provider SDK, HTTP exchange, `Path`, environment, mutable map이 없다.
2. CLI/future HTTP/local adapter는 같은 inbound use case를 호출한다.
3. Raw input은 versioned adapter와 normalization/preparation/binding을 우회하지 않는다.
4. Profile/version/preset/seed/work/limits/watchdog/workspace는 explicit하다.
5. Both-gate `PublishableResult`만 publication 후보가 된다.
6. Same identity/same digest는 수렴하고 same identity/different digest는 conflict다.
7. Artifact를 create-once하고 verified-read한 뒤 하나의 authoritative pointer만 CAS한다.
8. Status/result는 exact key/ref로 조회하며 listing, newest mtime, object existence를 authority로 쓰지 않는다.
9. Cancellation intent, cooperative observation, actual stop와 last safe point를 분리한다.
10. Algorithm step budget, application watchdog, transport/platform timeout을 분리한다.
11. Completion order, thread count, path와 elapsed는 result identity가 아니다.
12. Caller가 보낸 `TenantId`는 authorization proof가 아니다.
13. Failure taxonomy를 missing/not-found, denied, corrupt, stale, indeterminate 사이에서 손실시키지 않는다.
14. Phase 08은 Phase 09 object-common/S3, Phase 10 durable coordinator, Phase 11 AWS distribution을 선구현하지 않는다.

## 5. 실제 repository inventory — HEAD baseline, live drift와 목표

### 5.1 조사 방법과 해석

HEAD baseline은 commit `7cc890ee1d0805df5ae14b633127fade4f978639`의 tracked tree다. Live inventory는 같은 checkout의 concurrent uncommitted Phase 00 작업을 read-only로 관찰한 것이다.

```text
HEAD:
  single root JAR
  src/main Java 6 / test Java 1
  direct GCP/Jackson dependencies
  synthetic AlnsBatchEngine + HTTP/GCS placeholder

live drift:
  root pom → unaccepted reactor parent로 수정
  pinned wrapper, build rules/tests/scripts, rpdptw package scaffold 추가
  legacy source 이동 + provider seam/characterization test 추가
  README/Dockerfile/.dockerignore가 legacy module layout에 맞춰 수정 중

Phase 08 target:
  concrete application ports/services/identities/state
  adapters/common local filesystem package
  apps/cli executable composition root
  port contract/architecture/fault/E2E tests
  E-P08-* evidence
```

Live root [pom.xml](../../../../pom.xml)은 HEAD blob
`f8a411eadd4a5c01d8dd09fdea462738ca63d65f`, correction snapshot
2026-07-29T02:35:33+09:00의 live Git object는
`1dc675ba17b7f2202f34a22131f152cc2868b075`다. 이것은 concurrent Phase 00 work이며
Phase 08가 수정하거나 acceptance를 주장하지 않는다.

시간과 권한은 다음처럼 분리한다.

| Layer | 의미 | Phase 08 entry 사용 |
|---|---|---|
| HEAD baseline `7cc890e...` | Target 작성 checkout의 tracked historical baseline | Legacy/current-state 차이 설명만 |
| Original guide authoring snapshot | Phase 00 reactor가 진행 중이던 과거 live 관찰 | Historical provenance; current status 아님 |
| Correction live observation `2026-07-29T02:35:33+09:00` | Root POM/progress/source/test/evidence bytes의 시점 고정 관찰 | Restart inventory input; 빠르게 변하므로 acceptance 아님 |
| Accepted implementation evidence | Independent review PASS 뒤 acceptance receipt가 참조한 immutable bundle | Entry authority; 현재 Phase 00/08 모두 없음 |

Phase 00 replacement bundle의 존재와 fix 02 진행 상태는 “accepted evidence”가 아니다.
Fix 02 evidence regeneration, independent review PASS와 scheduler acceptance receipt 전에는
Phase 08 entry에 사용할 수 없다.

### 5.2 현재 vs 목표 inventory

| 영역 | HEAD baseline | Live inventory | Phase 08 목표/판정 |
|---|---|---|---|
| Root build | Single `com.ronext:ro-next` JAR | Modified parent/aggregator; `rpdptw`, `build`, `legacy` modules | Phase 00 acceptance 전 미승인 scaffold |
| Maven wrapper | 없음 | `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.mvn/toolchains.example.xml`이 작업 중 추가됨 | 파일은 존재하지만 Phase 00 acceptance가 아니며 Phase 08 target test가 없어 Phase 08 command는 여전히 future |
| Application module | 없음 | [rpdptw/application/pom.xml](../../../../rpdptw/application/pom.xml) 존재; core/solver/verification dependency | POM은 존재하지만 Phase 08 concrete implementation 0 |
| Application source | 없음 | `execution`, `port/in`, `port/out`, `service`의 `package-info.java` 4개 | Package ownership placeholder; interface/record/service 0 |
| Upstream target source | 없음 | `rpdptw/**` main Java 23개가 모두 `package-info.java` | Phase 01~07 implementation/evidence 0 |
| Application test | 없음 | `rpdptw/**` test Java 0 | Phase 08 unit/contract/integration test 0 |
| Build test | 없음 | `build/**/src/test/java` Java 11개: package-info 1, helper 1, executable test class 9개/`@Test` 25개 | Phase 00 work는 존재; `Phase08ApplicationArchitectureTest`/port-contract suite는 0 |
| Adapters | 없음 | `adapters/legacy-win-json`, `adapters/object-filesystem` 빈 directory만 존재 | POM/source/test 없음; filesystem adapter 구현 아님 |
| Apps | 없음 | `apps/cli` 빈 directory만 존재 | POM/source/test 없음; CLI 구현 아님 |
| Executable composition | 없음 | `apps/cli` 빈 directory, `distributions/` 부재 | Current Architecture에 맞는 `apps/cli` main/bootstrap/composition/runtime/fixture 0 |
| Port contract module | 없음 | `build/port-contract-tests` 없음 | Phase 08/09 abstract contract suite 0 |
| Legacy | Root `src/**` tracked | Root source는 deleted; `legacy/gcp-placeholder`에 이동본, provider seam과 characterization test가 작업 중 추가됨 | Concurrent migration 보존; target contract/evidence 아님 |
| Deployment/readme | Docker/GCP/README 존재 | `.dockerignore`, `Dockerfile`, `README.md`가 legacy module layout에 맞춰 수정 중 | Concurrent Phase 00 변경 보존; 수정·배포는 Phase 08 범위 밖 |
| Phase 00 evidence/status | 없음 | Review 02가 replacement bundle `a581e61b4fb9b565a55f7ff2925a1d5110477054c0731cc52407bae447c7dd07`와 pre-review manifest `fb3486f4af83164cc1b3c4a34b488145925a89547045518f4ff3ff1e49152e72`를 `CHANGES_REQUIRED`로 판정; fix 02 진행 중 | `REJECTED_PENDING_FIX_02_REGENERATION / NOT_ACCEPTED`; Phase 08 entry evidence가 아님 |
| Phase 08 evidence | 없음 | `E-P08-*` artifact/ref/digest 없음 | `NOT_PRODUCED` |

현재 [application inbound package-info](../../../../rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/in/package-info.java)는 provider-neutral use-case ownership만 선언한다. [outbound package-info](../../../../rpdptw/application/src/main/java/com/ronext/rpdptw/application/port/out/package-info.java)는 provider SDK 금지만 선언한다. 이것은 좋은 방향의 scaffold지만 method, failure, authorization, CAS semantics를 구현하지 않는다.

### 5.3 현재 없는 것을 검색하는 inspection command

다음은 현재 inventory를 읽기만 하는 명령이다.

```bash
git status --short
git diff -- pom.xml docs/implementation/execution-progress-and-results.md
find rpdptw build legacy adapters apps -type f -not -path '*/target/*' | sort
find rpdptw/application -type f -name '*.java' ! -name package-info.java
find rpdptw -type f -path '*/src/test/java/*' -name '*.java'
test -f mvnw
test -f .mvn/wrapper/maven-wrapper.properties
test ! -d distributions
test ! -d build/port-contract-tests
```

Wrapper 존재 검사는 concurrent Phase 00 작업의 live fact만 보여 준다. Application concrete source/test가 0이고 `distributions`, `build/port-contract-tests`가 없는 것은 Phase 08가 green이라는 뜻이 아니라 Phase 08 target이 없다는 evidence다.

### 5.4 Historical placeholder와 live relocation을 구분하기

Canonical Phase 08/review가 기록한 기존 placeholder facts는 HEAD source 기준으로 유효하다.

- Hidden `PORT=8080`, `SERVICE_MODE=api`
- Missing seed의 `System.nanoTime()`
- Random UUID request ID
- `parallelRuns=8`, `iterations=5000` fallback/clamp
- GCS object existence 기반 status
- Prefix listing 후 raw `double objective` 최소 후보 finalize
- Typed cancellation/idempotency/both-gate/CAS/input limit 부재

Live tree에서는 파일이 `legacy/gcp-placeholder`로 이동하고 provider seam/characterization test가 추가되는 중이다. Root의 tracked source 삭제, README/Dockerfile/.dockerignore 수정도 같은 concurrent Phase 00 변경이며 아직 committed/accepted baseline이 아니다. Phase 08 구현자는 root 삭제나 legacy 이동을 다시 수행하지 않고, accepted Phase 00 handoff 뒤의 exact legacy location을 재조회한다.

## 6. Scope, non-scope, 결정 상태와 사람 승인

### 6.1 포함 범위

- Provider-neutral inbound use case와 outbound port
- Submission/solve/manifest/run/attempt/artifact/state/publication identity
- Versioned input → solve → Phase 07 top-level 2 + nested rejection 2 output → publication/retrieval 흐름
- Explicit local config 기반 mandatory CLI reference
- Same-process single-worker dispatcher
- In-memory fake와 explicit-workspace local filesystem adapter
- Immutable artifact put/read/digest와 versioned state/publication CAS
- Idempotent submission, execution과 publication
- Cooperative cancellation, deadline/watchdog와 last safe point
- Typed application failure와 lossless adapter mapping
- Tenant/path/input/resource/redaction security
- Structured telemetry와 semantic/observation 분리
- Legacy characterization, isolated migration seam과 rollback rehearsal
- Phase 09 storage와 Phase 10 execution seam handoff

### 6.2 명시적 비범위

- S3/GCS/Azure object backend, provider SDK, bucket/key layout
- Step Functions/Lambda/ECS, GCP/Kubernetes workflow/compute
- Database, arbitrary query, listing 기반 recent/search/index
- Phase 10 multi-round durable coordinator와 declared-worker fan-in
- Public HTTP wire schema, exact public status/exit code와 product authentication
- GCP/AWS deployment, traffic cutover와 production authority
- Official worker/round/step/watchdog/limit/threshold 수치
- Route pool/MIP/OR-Tools dependency 또는 Phase 13 option
- `Q-VAR-01` optional variants와 multi-trip/rotation
- Phase 07 canonical payload 재인코딩 또는 result 의미 재계산

### 6.3 확정, proposed/open, gated, deferred

| 항목 | 상태 | 구현자가 할 수 있는 것 | 임의로 하면 안 되는 것 |
|---|---|---|---|
| Application-owned ports/use case | `CONTRACT` | Provider-neutral interface와 dependency rule 구현 | Adapter가 interface를 소유하게 함 |
| Exact Java type/method 이름 | `PROPOSED/OPEN` | Semantic projection과 test manifest 제안 | Wire/public compatibility로 선언 |
| `SolveId`/fingerprint algorithm | `PROPOSED/OPEN` | Versioned `TEST_ONLY` derivation 주입 | Hidden SHA/UUID/time default |
| Non-ambient tenant access binding | `CROSS-PHASE SECURITY BLOCKER` | 후보 비교와 no-call/no-leak red test | `TenantId`, `ThreadLocal`, env를 authority로 신뢰 |
| Storage operation failure carrier | `CROSS-PHASE DATA-INTEGRITY BLOCKER` | Operation × failure matrix와 exhaustive red test | Generic exception/`AdapterUnavailable`로 collapse |
| Run-state fence vs publication precondition | `CROSS-PHASE LINEARIZABILITY BLOCKER` | Distinct typed 후보와 race oracle 제안 | 같은 token 재사용/hidden precondition |
| Local filesystem atomicity | `PROPOSED/EXPERIMENT_REQUIRED` | Target FS capability/crash test | Unsupported FS에서 non-atomic fallback |
| `Q-BENCH-02` | `OPEN — EXPERIMENT_REQUIRED` | Explicit `TEST_ONLY`/experiment 값 | Legacy 8/5000/900/300을 official default로 사용 |
| `Q-INFRA-01` AWS target | `RESOLVED` | AWS가 미래 reference임을 보존 | Phase 08에 AWS 구현을 당겨옴 |
| Phase 09/11 S3 owner | `CROSS-PHASE BOUNDARY BLOCKER` | Provider-neutral/local last safe 유지 | S3 module/evidence owner를 임의 선택 |
| `C-17` route pool/MIP | `GATED TARGET` | Gate snapshot과 no-dependency test | Phase 13 착수/옵션/default 활성화 |
| Phase 13 | `OPTIONAL` | 14A receipt+C-17 승인 뒤 별도 진행 | Phase 08 성공으로 자동 활성화 |
| `Q-VAR-01` | `DEFERRED` | 현재 pair/terminal/bank 보존 | 질문·구현·config option 추가 |
| Phase 14A | `NOT_RUN/OPEN protocol values` | Phase 08 evidence를 입력으로 준비 | 단일 local success를 acceptance로 선언 |
| Phase 14B | `PRODUCTION AUTHORITY NOT GRANTED` | 없음 | Deploy/cutover/official claim |

### 6.4 사람 승인이 필요한 결정과 마지막 안전 지점

| 결정 | 승인 owner | 승인 전 마지막 안전 지점 | 승인 없을 때 stop 범위 |
|---|---|---|---|
| Phase 00~07 acceptance | Predecessor owners + scheduler | 문서, package scaffold, read-only inventory | Real Phase 08 pipeline/E2E/publication |
| Full-solution evaluation authority | Phase 03~06 owners | Ordered routes/bank와 route-level kernel | Phase 07/08 real result chain |
| Tenant authorization closure | Phase 08 Security + Phase 09 Storage | Backend call/existence disclosure 0 | Tenant-aware port/adapter |
| Lossless storage failure carrier | Phase 08/09 Data Integrity | Failure 때 state/pointer 불변 | Storage port implementation/fault evidence |
| Publication two-precondition contract | Phase 08/09/10 | Immutable publishable refs, published pointer 없음 | Publisher implementation/race evidence |
| Worker committed-outcome primitive | Phase 09/10 | Verified orphan outcome, committed authority 없음 | Multi-worker fan-in; Phase 08 single worker는 유지 가능 |
| Fingerprint/CLI/limit/FS policy | Architecture/API/Security/Platform | Versioned explicit `TEST_ONLY` values | External/production compatibility claim |
| Phase 09/11 S3 ownership | Architecture/Platform/Scheduler | Provider-neutral ports + local single-JVM | S3 implementation/evidence claim |
| Official values/production | Benchmark/Product/Operations/Security/Release | Local/reference evidence만 | Official manifest, deployment, cutover |

Stop은 실패가 아니다. 승인되지 않은 경계를 implementation convenience로 채우지 않는 것이 이 Phase의 안전성이다.

## 7. 사람을 위한 학습 경로

Phase 08은 한 번에 모든 port와 adapter를 작성하면 이해하기 어렵다. **개념 → 작은 탐색 → 실제 변경 → 통합**의 순서로 진행한다.

### 7.1 1단계 — 개념을 말로 설명하기

먼저 코드 없이 다음을 설명한다.

- Inbound use case와 outbound port의 owner가 왜 application인가?
- `ArtifactKey`, `ArtifactRef`, provider locator는 왜 다른가?
- Immutable artifact와 mutable pointer를 왜 분리하는가?
- Put-if-absent와 compare-and-set은 각각 어떤 race를 막는가?
- Phase 07 `VerificationRejected`와 `GateIncomplete`가 왜 다른가?
- Cancellation intent와 actual termination이 왜 다른가?
- Algorithm step budget과 wall-clock watchdog이 왜 다른가?
- `TenantId`가 왜 authorization proof가 아닌가?

완료 신호:

- 파일이나 provider 이름 없이 submission→publication 흐름을 그릴 수 있다.
- Success, rejection, incomplete, cancellation, timeout의 정상 payload 가능 여부를 구분한다.
- “파일이 있다”, “test가 green이다”, “verifier 하나가 PASS다”가 각각 왜 acceptance가 아닌지 설명한다.

자문 질문:

1. 이 field는 semantic identity인가 observation인가?
2. 이 값의 owner는 앞 Phase인가 application인가 adapter인가?
3. Retry가 이 값을 바꾸면 같은 logical operation인가?
4. Failure 뒤 어떤 pointer가 authority로 남는가?

### 7.2 2단계 — 작은 탐색으로 현재 구조 확인하기

아직 production type을 만들지 말고 다음을 조사한다.

1. `rpdptw-application`의 compile dependency를 손으로 DAG로 그린다.
2. 모든 `package-info.java`가 선언한 owner와 금지 경계를 표로 옮긴다.
3. Legacy placeholder의 UUID/time/default/listing/object-existence behavior를 source에서 찾는다.
4. Phase 07 top-level 두 branch와 `Rejected` 내부 두 branch의 field를 별도 표로 옮기고 각 branch의 publish call count를 적는다.
5. Phase 09가 요구하는 access/failure/precondition blocker를 operation별 matrix로 만든다.

작은 탐색 예:

```text
operation = publish

inputs:
  exact solve
  run-state fence
  publication-pointer precondition
  publishable closure
  authorized tenant scope

results:
  published
  already-same
  occupied-different
  stale state
  access denied
  referenced object missing/corrupt/indeterminate
```

완료 신호:

- “현재 존재”, “빈 placeholder”, “목표”, “승인 전 open”이 섞이지 않은 inventory가 있다.
- Operation × result matrix에서 generic exception 칸이 없다.
- Current POM/package-info를 Phase 08 implementation이라고 부르지 않는다.

### 7.3 3단계 — 실제 변경을 가장 작은 순서로 만들기

Entry gate가 열린 뒤에도 다음 순서를 지킨다.

```text
architecture red
→ identity/state/failure red
→ pure application types
→ fake port contract
→ Phase 07 exhaustive handling
→ local filesystem/security/crash
→ CLI/composition
→ cancellation/deadline/telemetry
→ real local E2E
→ evidence/review
```

한 단계의 최소 green이 되기 전에 다음 단계 mock으로 건너뛰지 않는다. 예를 들어 authorization contract가 open인데 filesystem adapter부터 만들거나, fake Phase 07 success만으로 `E-P08-LOCAL-E2E`를 만들지 않는다.

### 7.4 4단계 — 통합하며 의미가 보존되는지 확인하기

통합의 질문은 “모든 모듈이 compile되는가?”보다 강하다.

- CLI가 application use case를 우회하지 않는가?
- Application이 canonical payload를 다시 encode하지 않는가?
- Filesystem root가 달라도 semantic result가 같은가?
- Response loss 뒤 retry가 같은 published ref로 수렴하는가?
- Cancel/publish race에서 terminal meaning이 정확히 하나인가?
- Rejection/incomplete/limit/path attack에서 normal result pointer가 없는가?
- Telemetry가 실패하거나 순서가 달라도 seed/result가 같은가?

완료 신호:

- Real Phase 01~07 경로를 사용하는 local E2E가 있다.
- 두 fresh workspace와 same-workspace retry evidence가 있다.
- Success만이 아니라 failure/corruption/cancel/crash/recovery가 같은 contract를 증명한다.
- Independent review가 sealed pre-review evidence를 입력으로 `PASS`하고 acceptance receipt가 발행된다.

## 8. 목표 module/package/file과 dependency

### 8.1 예상 change tree

아래는 current Architecture §6.6/§7.1의 app composition-root 방향으로 canonical
Phase 08의 세분화 제안을 해소한 **proposed target**이다. Accepted Phase 00 naming과
Architecture/cross-Phase signature review에 따라 이름은 바뀔 수 있다. 이 tree 자체는
ADR이나 public compatibility가 아니며, 의미와 acyclic dependency 방향만 구현 시작 전에
승인되어야 한다.

```text
build/
├── architecture-rules/
│   └── src/test/java/.../Phase08ApplicationArchitectureTest.java
├── test-fixtures/
│   └── src/test/java/.../phase08/
│       ├── Phase08LocalFixtureBuilder.java
│       ├── ExplicitLocalConfigBuilder.java
│       ├── Phase07OutputStubBuilder.java
│       ├── ApplicationStateOracle.java
│       ├── LocalArtifactOracle.java
│       ├── TelemetryEventOracle.java
│       └── LocalPathAttackBuilder.java
└── port-contract-tests/
    ├── pom.xml                         # reusable tests-classifier Test JAR owner
    └── src/test/java/.../application/
        ├── ArtifactStoreContract.java  # abstract; Surefire test가 아님
        ├── RunStateRepositoryContract.java
        ├── ResultPublisherContract.java
        └── CancellationPortContract.java

rpdptw/application/
├── src/main/java/com/ronext/rpdptw/application/
│   ├── port/in/
│   │   ├── SubmitSolveUseCase.java
│   │   ├── ExecuteLocalSolveUseCase.java
│   │   ├── RequestCancellationUseCase.java
│   │   ├── GetSolveStatusQuery.java
│   │   └── GetVerifiedResultQuery.java
│   ├── port/out/
│   │   ├── ArtifactStore.java
│   │   ├── RunStateRepository.java
│   │   ├── ResultPublisher.java
│   │   ├── ProfileCatalogPort.java
│   │   ├── WorkerDispatcher.java
│   │   ├── WorkflowExecutionPort.java
│   │   ├── CancellationPort.java
│   │   ├── TelemetryPort.java
│   │   └── MonotonicClock.java
│   ├── api/
│   │   ├── SubmissionCommand.java
│   │   ├── ExecutionManifest.java
│   │   ├── ApplicationOutcome.java
│   │   ├── ApplicationFailure.java
│   │   └── identity/
│   ├── execution/
│   │   ├── SolveLifecycle.java
│   │   ├── SolveTransitionPolicy.java
│   │   ├── RunDeadline.java
│   │   └── LastSafePoint.java
│   └── service/
│       ├── DefaultSubmitSolveService.java
│       ├── DefaultLocalSolveService.java
│       ├── DefaultCancellationService.java
│       └── DefaultRetrievalService.java
└── src/test/java/...

adapters/common/
├── src/main/java/.../
│   ├── json/
│   │   ├── VersionedInputDocumentAdapter.java
│   │   ├── BoundedDocumentReader.java
│   │   └── SafeFailurePresenter.java
│   └── local/
│       ├── FileArtifactStore.java
│       ├── VersionedFileRunStateRepository.java
│       ├── AtomicFileResultPublisher.java
│       └── LocalWorkspaceLayout.java
└── src/test/java/.../
    ├── FileArtifactStoreContractTest.java
    ├── VersionedFileRunStateRepositoryContractTest.java
    ├── AtomicFileResultPublisherContractTest.java
    ├── LocalFilesystemCrashSafetyTest.java
    ├── LocalWorkspaceSecurityTest.java
    └── LocalInputLimitTest.java

apps/cli/
├── src/main/java/.../
│   ├── LocalCliMain.java
│   ├── LocalCliCommandParser.java
│   ├── LocalCliFailureMapper.java
│   ├── LocalCliOutput.java
│   ├── LocalCompositionRoot.java
│   ├── LocalRuntimeConfig.java
│   └── LocalRuntimeBootstrap.java
├── src/test/resources/phase08/
│   ├── P08_LOCAL_PD_3_TEST_ONLY.input.json
│   ├── P08_LOCAL_PD_3_TEST_ONLY.profile.json
│   └── P08_LOCAL_PD_3_TEST_ONLY.run.json
└── src/test/java/.../
    ├── LocalCliConfigurationTest.java
    ├── LocalCliLauncherSmokeTest.java
    ├── LocalCliInputLimitIntegrationTest.java
    ├── LocalCliWorkspaceSecurityIntegrationTest.java
    ├── TelemetryContractTest.java
    ├── LegacyMigrationBoundaryTest.java
    └── LocalCliEndToEndTest.java
```

Phase 08에서 만들지 않을 tree:

```text
adapters/object-common/                  # Phase 09
adapters/object-filesystem/              # current Architecture와 충돌하는 별도 split; 새 승인 전 금지
distributions/local/                     # downstream main/bootstrap을 만드는 split; 금지
adapters/object-s3/                      # Phase 09/11 ownership decision 뒤
apps/coordinator/ durable state machine  # Phase 10
adapters/workflow-aws-stepfunctions/     # Phase 11
adapters/compute-aws-lambda/             # Phase 11
deployment/aws/                          # Phase 11/14
adapters/route-selection-ortools-cpsat/  # Phase 13 gated
```

### 8.2 Compile dependency

```text
rpdptw-core
rpdptw-solver       → rpdptw-core
rpdptw-verification → rpdptw-core

rpdptw-application
  → rpdptw-core
  → rpdptw-solver
  → rpdptw-verification

adapters/common
  → rpdptw-core
  → rpdptw-application
  → rpdptw-verification

apps/cli
  → rpdptw-application
  → adapters/common
  → rpdptw-capabilities
  → rpdptw-profile-catalog
```

Application은 capabilities나 concrete profile implementation을 compile-depend하지 않는다.
`apps/cli`가 executable JAR/launcher, `main`, bootstrap과 composition root를 함께
소유하고 exact registry/catalog 및 `adapters/common`의 local implementation을 조립한다.
따라서 `apps/cli → ...`만 존재하고 application/adapter가 `apps/cli`를 import하거나
별도 downstream module의 bootstrap을 호출하는 reverse edge는 없다.

Executable ownership의 skeletal contract는 다음과 같다. 이는 완성 코드나 exit-code/
public schema default가 아니다.

```java
// apps/cli, PROPOSED skeleton only
public final class LocalCliMain {
    public static void main(String[] args) {
        LocalRuntimeBootstrap.launch(args);
    }
}

final class LocalRuntimeBootstrap {
    static void launch(String[] args) {
        // parse required config, then delegate object creation to the same module
        LocalCompositionRoot.create(/* explicit validated config */)
            .run(/* parsed command */);
    }
}
```

`LocalCompositionRoot`의 compile-time inputs는 application-owned inbound/outbound
contracts, `adapters/common` local implementations, accepted capability/profile providers뿐이다.
구체 constructor/return/exit signature는 `PROPOSED/OPEN`이며 Architecture/API owner가
승인하기 전 freeze하지 않는다. Packaging smoke는 `apps/cli` artifact의 manifest/launcher가
같은 artifact 안의 `LocalCliMain`을 가리키고 explicit test config로 bootstrap되는지만
검사한다. `java -jar`/launcher가 downstream module을 runtime classpath에서 우연히 찾아
성공하는 것은 pass가 아니다.

Test-only dependency는 compile DAG와 분리한다.

```text
build/port-contract-tests
  test-depends-on → rpdptw-application
  attaches        → type=test-jar, classifier=tests

adapters/common
  test-depends-on → build/port-contract-tests:tests
  production scope leakage = 0
```

`build/port-contract-tests`는 **reusable Test JAR owner**로 고정한다. Abstract contract는
standard `src/test/java`에 두되 이름이 `*Test`가 아니므로 자체 behavior pass로
discovery되지 않는다. Consumer의 concrete `*ContractTest`가 이를 상속/구성해 실행한다.
Owner POM은 live `build/test-fixtures` convention처럼 `maven-jar-plugin:test-jar`를
attach하고, consumer POM은 `type=test-jar`, `classifier=tests`, `scope=test`로만
의존한다. Architecture/dependency report는 이 artifact가 production compile/runtime
graph에 들어오지 않음을 검증한다.

### 8.3 책임 배치 자문표

| 요구 | 둬야 할 곳 | 두면 안 되는 곳 |
|---|---|---|
| Solve submission identity | `application.api/identity`, inbound use case | CLI parser, filesystem layout |
| Legal solve transition | `application.execution` | Adapter exception handler |
| Artifact logical semantics | `application.port.out` | S3/filesystem backend interface |
| Safe path encoding | Filesystem adapter internal | `ArtifactKey`, `SolveId` |
| Input JSON mapping | `adapters/common` | Core/application public command에 Jackson node |
| CLI exit/presentation | `apps/cli` | `ApplicationFailure` semantic code |
| CLI `main`/bootstrap/composition/packaging | `apps/cli` | `rpdptw-application`, `adapters/common`, 별도 downstream distribution |
| Concrete capability/profile selection | `apps/cli` composition root | Application POM |
| Local filesystem implementation | `adapters/common`의 `adapter.local` package | Application port 또는 CLI parser |
| Phase 07 result meaning | `rpdptw-verification` | Application re-encoding/finalization |
| Multi-round completeness | Phase 10 | Phase 08 single-worker state |
| AWS SDK/event mapping | Phase 11 adapter | Application/core/verification |

## 9. Proposed/Open Java 계약과 상태 전이

이 절은 완성 코드를 복사하라는 뜻이 아니다. Type이 강제해야 할 의미와 open decision을 드러내는 **skeletal contract**다.

### 9.1 Inbound use case 후보

```java
// PROPOSED internal Java projection; public wire API가 아니다.
public interface SubmitSolveUseCase {
    SubmissionReceipt submit(SubmissionCommand command);
}

public interface ExecuteLocalSolveUseCase {
    LocalExecutionOutcome execute(ExecuteLocalSolveCommand command);
}

public interface RequestCancellationUseCase {
    CancellationReceipt requestCancellation(CancelSolveCommand command);
}

public interface GetSolveStatusQuery {
    SolveStatusView getStatus(GetSolveStatus query);
}

public interface GetVerifiedResultQuery {
    VerifiedResultView getResult(GetVerifiedResult query);
}
```

```java
public record SubmissionCommand(
    TenantId tenantId,
    SubmissionId submissionId,
    IdempotencyKey idempotencyKey,
    SubmittedInputDocument input,
    ProfileSelection profileSelection,
    ExecutionManifest executionManifest
) {}
```

`SubmittedInputDocument`는 defensive immutable bytes abstraction, length, digest와 schema version을 가진다. `Path`, URL, bucket, stream, Jackson node, `HttpExchange`, mutable `byte[]`는 application command에 넣지 않는다.

### 9.2 Authorization binding — OPEN blocker

다음 두 shape는 **비교할 후보**이지 선택된 계약이 아니다.

```java
// Candidate A: operation argument가 explicit scope를 가진다.
OperationResult readVerified(
    AuthorizedTenantAccess access,
    ArtifactRef reference
);
```

```java
// Candidate B: 승인된 scope로 묶인 session/facade를 먼저 만든다.
TenantScopedArtifactSession authorize(AuthorizedTenantAccess access);
session.readVerified(reference);
```

결정 때 검토할 것:

- Caller authentication 결과와 tenant/purpose/policy version이 어떻게 묶이는가?
- Async dispatch/retry/retrieval에 scope가 어떻게 손실 없이 전달되는가?
- Reference/key tenant mismatch를 backend call 전에 막는가?
- Missing context와 wrong-tenant가 object 존재 여부를 누설하지 않는가?
- Global/static, `ThreadLocal`, environment, workspace owner를 쓰지 않는가?

이 계약이 승인되기 전 `TenantId`만 parameter에 추가해 tenant test를 green으로 만들지 않는다.

### 9.3 Submission과 execution outcome 후보

```java
public sealed interface SubmissionReceipt
    permits SubmissionReceipt.Created,
            SubmissionReceipt.Existing,
            SubmissionReceipt.Conflict {

    record Created(
        SolveId solveId,
        SubmissionCommandFingerprint fingerprint,
        StateVersion stateVersion
    ) implements SubmissionReceipt {}

    record Existing(
        SolveId solveId,
        SubmissionCommandFingerprint fingerprint,
        StateVersion stateVersion
    ) implements SubmissionReceipt {}

    record Conflict(
        SubmissionId submissionId,
        SafeConflictDetail detail
    ) implements SubmissionReceipt {}
}
```

```java
public sealed interface LocalExecutionOutcome
    permits LocalExecutionOutcome.Succeeded,
            LocalExecutionOutcome.Rejected,
            LocalExecutionOutcome.Cancelled,
            LocalExecutionOutcome.Interrupted {

    record Succeeded(
        SolveId solveId,
        PublishedResultRef result,
        SolveStatusView status
    ) implements LocalExecutionOutcome {}

    record Rejected(
        SolveId solveId,
        ApplicationFailure failure,
        SolveStatusView status
    ) implements LocalExecutionOutcome {}

    record Cancelled(
        SolveId solveId,
        CancellationReceipt receipt,
        LastSafePoint lastSafePoint
    ) implements LocalExecutionOutcome {}

    record Interrupted(
        SolveId solveId,
        ApplicationFailure failure,
        LastSafePoint lastSafePoint
    ) implements LocalExecutionOutcome {}
}
```

`Succeeded`에만 both-gate published ref가 있다. 나머지 variant에 정상 route/outcome/canonical payload/benchmark vector를 넣지 않는다.

### 9.4 Storage port skeleton과 OPEN failure carrier

```java
public interface ArtifactStore {
    ArtifactPutResult putIfAbsent(
        ArtifactKey key,
        ArtifactContent content,
        ContentDigest expectedDigest
    );

    // Return carrier is OPEN: it must represent missing/denied/corrupt/
    // stale/indeterminate without unchecked-exception collapse.
    ArtifactReadOperationResult readVerified(
        AuthorizedTenantAccess access,
        ArtifactRef reference
    );
}
```

```java
public record ArtifactKey(
    TenantId tenantId,
    ArtifactKind kind,
    ArtifactId artifactId
) {}

public record ContentDigest(
    DigestAlgorithmId algorithmId,
    DigestAlgorithmVersion algorithmVersion,
    DigestBytes bytes
) {}
```

`ArtifactRef`의 exact field와 tenant closure는 cross-Phase review 대상이다. `OpaqueLocator`가 있더라도 application은 parse/concatenate/compare/fingerprint하지 않는다.

Operation별 failure carrier가 최소 구분해야 할 의미:

```text
NOT_FOUND
ACCESS_DENIED
IDENTITY_CONFLICT
CORRUPT_OBJECT
STALE_VERSION
VISIBILITY_INDETERMINATE
PARTIAL_WRITE_ABORTED
UNSUPPORTED_CAPABILITY
TRANSIENT_UNAVAILABLE
```

이 taxonomy를 그대로 하나의 public enum으로 만들라는 뜻은 아니다. 각 operation이 가능한 결과를 sealed/checked하게 완전 표현하고 application failure로 total mapping해야 한다는 뜻이다.

### 9.5 State와 publication precondition — OPEN blocker

```java
public interface RunStateRepository {
    CreateStateResult createIfAbsent(SolveId solveId, RunState initialState);
    VersionedRunState get(SolveId solveId);
    StateUpdateResult compareAndSet(
        SolveId solveId,
        StateVersion expectedVersion,
        RunState nextState
    );
}
```

다음은 semantic 구분을 가르치기 위한 후보다.

```java
// PROPOSED names; exact shape requires Phase 08/09/10 approval.
record RunStateFence(SolveId solveId, StateVersion version) {}
record PublicationPrecondition(PublicationPointerVersion version) {}
```

```java
public interface ResultPublisher {
    PublicationOperationResult compareAndSet(
        AuthorizedTenantAccess access,
        RunStateFence runStateFence,
        PublicationPrecondition pointerPrecondition,
        PublishableResultRef desired
    );
}
```

Run-state fence는 “현재 run이 publish를 허가하는 상태인가?”를 확인한다. Publication precondition은 “published pointer가 비었거나 내가 아는 version인가?”를 확인한다. 서로 다른 authoritative object이므로 token을 재사용하지 않는다. 위 signature는 승인 전 구현 compatibility authority가 아니다.

Publication 순서:

```text
1. PublishableResult payload/reports/artifacts putIfAbsent
2. exact ref로 재독해하여 digest/length/schema 검증
3. Phase 07 candidate PASS + result PASS closure 일치
4. authorized run-state fence 재확인
5. published-result pointer create/CAS
6. pointer 재독해와 desired result equality
7. 그 뒤에만 SUCCEEDED
```

### 9.6 Cancellation, deadline와 observation

```java
public interface CancellationPort {
    CancellationWriteResult recordIfAbsent(CancellationIntent intent);
    Optional<CancellationIntent> find(SolveId solveId);
}

public interface CancellationProbe {
    CancellationObservation observe();
}

public interface MonotonicClock {
    MonotonicTick now();
}
```

```java
public record RunDeadline(
    MonotonicTick startedAt,
    WatchdogBudget explicitBudget
) {
    public DeadlineObservation observe(MonotonicClock clock);
}
```

Monotonic clock는 elapsed/watchdog observation에만 사용한다. Seed, comparator, stable ordering, semantic fingerprint, `MAX_STEPS_REACHED` 판단에 사용하지 않는다.

### 9.7 Application failure hierarchy 후보

```java
public sealed interface ApplicationFailure
    permits InputRejected,
            BindingRejected,
            IdentityConflict,
            ArtifactIntegrityViolation,
            IllegalStateTransition,
            VerificationRejected,
            VerificationGateIncomplete,
            CancellationCompleted,
            WatchdogReached,
            ResourceLimitReached,
            PlatformTimeout,
            AdapterUnavailable,
            InternalApplicationFailure {

    ApplicationFailureCode code();
    FailureStage stage();
    SafeFailureDetail safeDetail();
    RetryDisposition retryDisposition();
}
```

주의:

- `AdapterUnavailable` 하나가 storage taxonomy를 삼키면 안 된다.
- `VerificationRejected`는 Phase 07 stage/disposition/failure/optional candidate PASS를 보존한다.
- `VerificationGateIncomplete`는 stage/incomplete reason/optional candidate PASS/last-safe identity를 보존하고 disposition을 합성하지 않는다.
- Unexpected exception을 all-unassigned, empty route 또는 success로 바꾸지 않는다.

### 9.8 Pipeline pseudocode

```text
execute(command):
  require all schema/profile/version/preset/seed/work/limit/watchdog fields
  authorize explicit tenant access before existence lookup

  receipt = submit(command)
  if conflict:
    return Rejected(IdentityConflict)

  current = state.get(exact solve)
  if terminal:
    return same terminal view only after command/result identity equality

  CAS SUBMITTED → CANONICALIZING
  canonical = exact versioned adapter.parse(input)

  CAS CANONICALIZING → PREPARING
  problem, travel = preparation.prepare(canonical)
  profile = bind exact profile/version/preset
  snapshot = immutable(problem, travel, profile)
  persist + re-read snapshot/manifest
  CAS PREPARING → PREPARED

  check cancel/deadline at safe point
  CAS PREPARED → SOLVING
  candidate = solve(snapshot, manifest, probe, deadline)
  persist + re-read candidate/replay

  CAS SOLVING → VERIFYING_FINAL_RESULT
  output = phase07.finalizeResult(exact request)

  switch output:
    Publishable:
      require candidate PASS + result PASS closure
      persist + re-read result/payload/reports
      CAS VERIFYING_FINAL_RESULT → PUBLISHING
      publish using distinct state fence + pointer precondition
      if published or exact already-same:
        confirm pointer then SUCCEEDED
      else:
        typed conflict/failure; never expose normal payload

    Rejected:
      switch output.rejection:
        VerificationRejected:
          persist safe rejection
          CAS → PUBLICATION_REJECTED
          return Rejected(preserve exact fields)

        GateIncomplete:
          persist safe incomplete record
          CAS → VERIFICATION_INCOMPLETE
          return Interrupted(preserve exact fields, no disposition)
```

모든 integrity-critical operation은 반쯤 commit하지 않는다. Cancel/deadline을 관찰했더라도 이미 시작한 immutable write/digest verification은 complete 또는 discard한 뒤 actual termination을 기록한다.

두 switch 모두 catch-all/default branch를 두지 않는다. Accepted Phase 07 sealed hierarchy에
top-level subtype 또는 nested rejection subtype이 추가되면 compilation/signature
compatibility test가 fail closed해야 한다. Serialized handoff의 type tag나 variant별
required field가 누락되면 mapping 전에 contract decoder가 typed failure로 거부한다.
Reflection, `instanceof` catch-all 또는 generic exception은 exhaustiveness evidence가
아니다.

## 10. Test-first 전략, fixture, oracle와 false-green 방지

### 10.1 Test fixture와 builder

| Fixture/builder | 역할 | 금지 |
|---|---|---|
| `P08_LOCAL_PD_3_TEST_ONLY` | Explicit input/profile/run config를 쓰는 작은 real local E2E | Official benchmark/production default로 승격 |
| `P08_APPLICATION_PUBLISHABLE_TEST_ONLY` | Known Phase 07 publishable envelope unit fixture | Real Phase 07 E2E evidence로 제출 |
| `P08_APPLICATION_REJECTION_TEST_ONLY` | Candidate/result-stage rejection variants | Normal payload 포함 |
| `P08_APPLICATION_INCOMPLETE_TEST_ONLY` | Candidate/result-stage incomplete variants | Disposition 합성 |
| `Phase08LocalFixtureBuilder` | Fresh workspace-independent documents | CWD/home/env/temp fallback |
| `ExplicitLocalConfigBuilder` | 모든 required field와 omission/mutation cases | Automatic defaults |
| `Phase07OutputStubBuilder` | Top-level 2 + nested rejection 2 typed output | Unknown/missing branch를 generic exception으로 처리 |
| `LocalPathAttackBuilder` | Traversal/separator/control/Unicode/symlink cases | Test root 밖 실제 접근 |
| `FaultInjectingLocalIo` | Write/flush/move/pointer/response-loss one-shot fault | 여러 fault를 한 case에 섞기 |

`P08_LOCAL_PD_3_TEST_ONLY`의 expected semantic result는 accepted predecessor fixture/evidence에서 받아야 한다. 현재 그 literal이 없으므로 이 가이드가 route, objective 또는 digest를 발명하지 않는다.

### 10.2 Independent oracle

| Oracle | 독립적으로 계산/검사할 것 | Production helper 재사용 금지 |
|---|---|---|
| `ApplicationStateOracle` | Legal transition, expected version과 terminal XOR | `SolveTransitionPolicy` |
| `CommandFingerprintOracle` | Explicit inclusion/exclusion projection | Production fingerprint builder |
| `LocalArtifactOracle` | File bytes/digest/length, pointer target와 orphan | `FileArtifactStore` encoder/reader |
| `PublicationRaceOracle` | Barrier별 가능한 one-winner outcome | Publisher retry helper |
| `TelemetryEventOracle` | Event family, safe allowlist와 leak denylist | Production serializer/redactor |
| `ExactResultRetrievalOracle` | Published pointer가 가리키는 exact bytes | Directory scan/newest helper |
| `LegacyPlaceholderSourceOracle` | UUID/time/default/listing AST facts | Runtime/cloud success claim |

Oracle가 production code와 같은 bug를 공유하면 green은 evidence가 아니다. Architecture test로 test-oracle → production helper dependency도 검사한다.

### 10.3 Exact test class/method 후보

Architecture:

```text
Phase08ApplicationArchitectureTest
  applicationDependsOnCoreSolverAndVerificationButNoProviderSdk()
  applicationPortsExposeNoPathUriHttpExchangeEnvironmentOrMutableMap()
  localAdaptersDependInwardAndApplicationNeverDependsOnAdapters()
  verificationStillDoesNotDependOnApplicationSolverOrAdapters()
  cliOwnsExecutableCompositionRootAndHasNoReverseDependency()
  cliExecutableContainsNoCloudDatabaseOrRouteSelectionDependency()
  applicationContainsNoCustomerNameBranchOrProviderLocatorParsing()
```

Signature/identity/state:

```text
ApplicationPortSignatureTest
  phase07TopLevelOutputAndNestedRejectionAreExhaustive()
  phase07VariantRequiredFieldsFailClosedBeforeMapping()
  tenantScopedAccessBindingIsExplicitAndNonAmbient()
  storageOperationFailureCarrierIsExhaustiveAndLossless()
  runStateFenceAndPublicationPreconditionAreDistinct()

SubmissionIdempotencyTest
  newSubmissionCreatesOneSolveAndInitialState()
  sameKeyAndSameCommandReturnsExistingSolve()
  sameKeyAndDifferentInputDigestConflicts()
  sameKeyAndDifferentProfileOrManifestConflicts()
  absolutePathMtimeEnvironmentAndClockDoNotAffectCommandFingerprint()
  missingExplicitSeedWorkLimitOrWatchdogIsRejectedWithoutDefault()

ApplicationStateTransitionTest
  followsExactHappyPathToSucceeded()
  rejectsSkipFromSolvingDirectlyToPublishing()
  staleCasReloadsAndNeverFallsBackToLastWriteWins()
  terminalDifferentMeaningOrDigestConflicts()
```

Phase 07 consumption:

```text
Phase07ApplicationIntegrationTest
  publishableResultIsPersistedVerifiedThenPublished()
  candidateStageRejectionNeverPublishesNormalPayload()
  resultStageRejectionNeverPublishesNormalPayload()
  rejectionPreservesStageDispositionAndSafeFailure()
  gateIncompleteMapsToInterruptedWithoutPublication()
  gateIncompletePreservesReasonCandidatePassAndLastSafeWithoutDisposition()
  missingCandidateOrResultPassRejectsBeforePublication()
  sameEnvelopeIdentityDifferentPayloadBytesIsIntegrityViolation()
  exceptionIsNeverConvertedToAllUnassignedOrSucceeded()

ResultRetrievalTest
  returnsResultOnlyThroughPublishedPointerAndVerifiedRef()
  neverUsesDirectoryListingOrNewestFileAsResultAuthority()
```

Port contract:

```text
ArtifactStoreContract
  putIfAbsentCreatesAndReadVerifiedReturnsExactBytes()
  sameKeySameDigestIsIdempotent()
  sameKeyDifferentDigestConflicts()
  declaredDigestDifferentFromContentIsRejected()
  readVerifiedRejectsMetadataOrByteCorruption()
  tenantScopeCannotCrossReadOrWrite()
  missingOrMismatchedAccessBindingFailsBeforeExistenceDisclosure()

RunStateRepositoryContract
  createIfAbsentIsIdempotentOnlyForSameInitialState()
  compareAndSetAdvancesExactlyOneOpaqueVersion()
  staleVersionNeverOverwritesCurrentState()
  rejectsIllegalTransitionEvenWithCurrentVersion()

ResultPublisherContract
  publishesOnlyBothGateReferenceAtExpectedState()
  sameDigestDuplicateConverges()
  differentDigestDuplicateConflicts()
  runStateFenceAndPublicationPreconditionAreDistinct()
  cancellationPublicationRaceHasOneTerminalWinner()
```

Local fault/security:

```text
LocalFilesystemCrashSafetyTest
  failureBeforeAtomicMoveLeavesNoReferencedPartialArtifact()
  failureAfterArtifactBeforePointerLeavesUnreferencedArtifactOnly()
  retryAfterResponseLossConvergesToSamePublishedReference()
  bootstrapRejectsFilesystemWithoutRequiredAtomicSemantics()
  declaresLocalSingleJvmAndNeverClaimsCrossProcessOrDistributedCas()

LocalWorkspaceSecurityTest
  rejectsTraversalAbsoluteSeparatorControlAndUnicodeAmbiguity()
  rejectsSymlinkEscapeAtEveryPathComponent()
  tenantWithSameTextualArtifactIdCannotCrossRead()
  tenantIdWithoutAuthorizedBindingCannotReadWriteOrQuery()
  safeFailureContainsNoAbsolutePathInputSecretOrStackTrace()

LocalInputLimitTest
  rejectsDeclaredOrStreamingBytesBeyondExplicitLimit()
  rejectsDepthStringCollectionAndNumericTokenLimit()
  missingLimitIsRejectedRatherThanDefaulted()
```

Cancellation/deadline/retry/telemetry:

```text
CancellationSemanticsTest
  intentAndActualWorkerTerminationAreDistinct()
  cancellationDiscardsUncommittedCowTrialAndPreservesLastSafePoint()
  cancelAfterSucceededDoesNotDeleteOrRewriteResult()
  cancelAndPublishRaceProducesExactlyOneTerminalMeaning()

DeadlineSemanticsTest
  watchdogDoesNotBecomeMaxStepsReached()
  incompleteStepDoesNotAdvanceCompletedWorkOrAdaptiveState()
  transportTimeoutAfterPublishRetryReturnsExistingPublishedResult()
  clockElapsedNeverChangesSeedComparatorOrResultFingerprint()

WorkerRetryIdentityTest
  retryChangesAttemptOnly()
  sameStrongReplayIdentityDifferentVerifiedDigestIsIntegrityViolation()

TelemetryContractTest
  emitsRequiredHappyPathAndFailureEventsWithSafeCorrelation()
  redactsRawInputPiiSecretPathAndStackTrace()
  elapsedAndEventOrderDoNotChangeSemanticResult()
```

CLI/E2E/migration:

```text
LocalCliConfigurationTest
  requiresEveryExplicitSemanticRuntimeSecurityField()
  environmentCurrentDirectoryHomeLocaleTimezoneCannotSupplyMissingFields()

LocalCliLauncherSmokeTest
  packagedMainClassResolvesInsideCliArtifact()
  launcherBootstrapsCompositionWithExplicitTestConfig()
  launcherNeedsNoDownstreamDistributionClasspath()

LocalCliEndToEndTest
  explicitFixtureRunsCanonicalizeSolveBothGatePublishAndRetrieve()
  sameManifestInTwoFreshWorkspacesProducesSameCanonicalResultIdentity()
  sameWorkspaceRetryConvergesWithoutOverwrite()
  differentInputWithSameSubmissionConflictsBeforeSolve()
  phase07RejectedFixtureReturnsNoNormalResult()
  statusAndResultUseExactSolveIdWithoutDirectoryScan()
  ctrlCRequestsCooperativeCancellationAndReportsLastSafePoint()
  inputLimitFailureOccursBeforeCanonicalizationAndLeavesNoSolveResult()
  structuredLogsContainRequiredEventsAndNoSensitiveData()

LegacyMigrationBoundaryTest
  newLocalDistributionDoesNotModifyOrInvokeLegacyGcpControllers()
  shadowComparisonCannotPublishLegacyOrMismatchedResultAsTarget()
  rollbackDisablesNewLocalEntrypointWithoutChangingLegacyArtifact()
```

### 10.4 Red → green 순서

| 순서 | 먼저 red로 고정할 것 | 최소 green | Test layer |
|---:|---|---|---|
| 1 | Module DAG와 forbidden public type | Provider/transport/path edge 0 | Architecture |
| 2 | Identity/version/command projection | Same/same 수렴, same/different conflict | Unit/contract |
| 3 | Legal state와 top-level/nested Phase 07 failure | Illegal skip 0, 두 dispatch 경계 field loss 0 | Unit/property |
| 4 | Approved access/failure/precondition signature | Ambient trust/collapse/token reuse 0 | Signature contract |
| 5 | Artifact/state/publication fake contract | Digest/CAS/publish exact | Port contract |
| 6 | Phase 07 exhaustive handling | Publishable only after both PASS | Module integration |
| 7 | Filesystem/security/crash | Outside write 0, partial pointer 0 | Adapter/fault/security |
| 8 | Cancellation/deadline/retry/telemetry | Exceptional meaning 보존 | Application/fault |
| 9 | Explicit CLI/config/limits | Hidden fallback 0 | Adapter/security |
| 10 | Stubbed application E2E | Exact call/state sequence | Application integration |
| 11 | Real Phase 01~07 local E2E | Both-gate request→retrieval | Full integration/E2E |
| 12 | Fresh/same workspace replay | Semantic identity exact | Reproducibility |
| 13 | Legacy isolation/rollback | Cross-call/write 0 | Migration |
| 14 | Full reactor/evidence/review | Missing/fail/error/skip 0 | System/review |

### 10.5 False-green 방지

- `build/port-contract-tests`는 reusable Test JAR owner이고, concrete behavior test owner는
  `adapters/common`이다. Abstract contract source를 복사하거나 production JAR에 넣지 않는다.
- Clean checkout selected test는 §12.1의 **paired protocol**로 실행한다. 같은 explicit
  isolated local repository에 root reactor `-pl <owner> -am clean install -DskipTests`로
  exact sibling/test-JAR dependency를 먼저 설치한 뒤 owner POM `-f` selected test를
  `clean test`한다. 첫 단계의 `BUILD SUCCESS`와 skip된 test 수는 evidence가 아니다.
- Selected test는 owner POM에 `-f`, 같은 `-Dmaven.repo.local`과
  `-Dsurefire.failIfNoSpecifiedTests=true`를 사용해 fail closed한다. Selected filter와
  `-am`은 같이 쓰지 않는다.
- 현재 live parent는 Surefire 3.5.4만 관리하고 Failsafe plugin/binding은 없다. 이
  가이드의 `*Test`/`*ContractTest`/`LocalCliEndToEndTest`는 Surefire
  `test` phase owner다. 향후 `*IT`로 바꾸면 Failsafe를 `integration-test`/`verify`에
  명시적으로 bind하고 `-Dit.test`와 `-Dfailsafe.failIfNoSpecifiedTests=true`를
  추가하기 전에는 discovered로 계산하지 않는다.
- Full module/reactor suite는 filter 없이 별도 `clean verify`한다.
- Selected owner의 `clean` 뒤 생성된 expected class/method manifest와
  Surefire/Failsafe discovered XML만 exact 비교한다. Report path, source/build
  fingerprint와 generation timestamp를 봉인하고 이전 `target/` XML을 섞지 않는다.
- Missing, duplicate, failed, error, skipped, aborted 중 하나라도 있으면 fail이다.
- Stale `target/`, IDE green, console `BUILD SUCCESS`, 과거 commit report를 evidence로 쓰지 않는다.
- Stub Phase 07 test는 application integration으로만 분류하며 real local E2E로 제출하지 않는다.
- In-memory adapter success를 filesystem crash/durability evidence로 제출하지 않는다.
- Oracle이 production helper를 참조하면 해당 green과 evidence를 무효화한다.
- Test-only value에는 config, fixture name과 evidence에 `TEST_ONLY`를 남긴다.

### 10.6 Test 종류별 적용성과 pass 판정

| 종류 | 적용 | Phase 08 내용/미적용 이유 | Pass 판정 |
|---|---:|---|---|
| Unit/boundary | 필수 | Identity, failure mapping, limit-1/limit/limit+1, state | Exact positive/negative/boundary |
| Contract | 필수 | Inbound/outbound/Phase 07/storage signature | Unknown/missing branch fail-closed |
| Module integration | 필수 | Application service + fake ports + Phase 07 variants | Exact call/state/publish count |
| Property | 필수 | State transition, idempotency, path encoding, race terminal XOR | Shrinkable counterexample 0 |
| Architecture | 필수 | Provider/transport/path/vendor/reverse edge | Forbidden ref/edge 0 |
| Fault injection | 필수 | Write/flush/move/CAS/response loss/cancel/deadline | Last safe pointer/identity 보존 |
| Corruption | 필수 | Digest/length/schema/ref/PASS closure tamper | Deserialization/publication 0 |
| Reproducibility | 필수 | Fresh workspace, locale/timezone/clock/schedule permutations | Semantic result/payload exact |
| Security | 필수 | Tenant authorization, path/symlink/input limit/redaction | Outside call/write/leak 0 |
| Performance | 제한 적용 | Bounded input/artifact/workspace, event/work accounting | Correctness 불변; official threshold 없음 |
| Local E2E | 필수 | Real Phase 01~07 + CLI + filesystem + retrieval | Both PASS, exact lineage |
| Provider integration | 미적용 | Phase 08은 cloud SDK/provider adapter를 만들지 않음 | Dependency 0이 pass |
| Multi-round E2E | Phase 10 소유 | Phase 08은 single-worker reference | Fake reserved seam까지만 |
| Official benchmark | Phase 14A 소유 | Phase 08 evidence는 입력일 뿐 acceptance 아님 | Approved protocol/receipt 전 N/A |
| Production/security rehearsal | Phase 11/14B 소유 | Local scope는 production authority가 아님 | Local claim과 분리 |

## 11. 순서 있는 work packages

모든 Maven 명령은 **Phase 00 accepted reactor와 wrapper, 해당 module/type/test가 실제
존재한 뒤** 실행할 future command다. 현재 checkout에 wrapper/reactor와 replacement
evidence가 생겼지만 Phase 00은
`REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS / NOT_ACCEPTED`이고 Phase 08 test/type은
없으므로 지금 Phase 08 명령이 성공했다고 기록하지 않는다. 모든 selected command는
§12.1의 isolated-repository dependency preparation과 한 쌍이며 단독 실행하지 않는다.

### WP-08.0 — Entry receipt, live inventory와 legacy characterization

#### 목적과 필요한 이유

구현을 시작하기 전에 predecessor authority, 현재 repository 상태와 legacy rollback 지점을 고정한다. 이 단계가 없으면 concurrent Phase 00 scaffold를 Phase 08 기반으로 오인하거나, Phase 07 fake를 real handoff로 사용하거나, legacy hidden default를 target default로 올릴 수 있다.

#### 사전조건

- Scheduler task ID와 implementer/security/storage/reviewer 역할이 지정됐다.
- Phase 00 accepted reactor/module/package evidence를 읽을 수 있다.
- Phase 07 accepted handoff/evidence를 읽을 수 있다. 없으면 characterization까지만 하고 stop한다.
- Canonical source와 Phase 07/09 인용 section을 semantic diff할 수 있다.

#### 예상 file/package/type

- `build/architecture-rules/.../Phase08ApplicationArchitectureTest.java`
- `build/test-fixtures/.../LegacyPlaceholderSourceOracle.java`
- `LegacyPlaceholderSourceCharacterizationTest.java`
- Phase 08 entry receipt와 decision/open manifest
- Progress/README는 scheduler/owner만 수정하며 이 WP implementer가 직접 바꾸지 않는다.

#### 구체 행동

1. §3.2의 source Git blob과 live changed source hash를 다시 계산한다.
2. Phase 07 `Phase07HandoffManifest`, top-level 2 + nested rejection 2 variants, `E-P07-*`, accepted review/receipt를 field 단위로 대조한다.
3. Phase 09의 access binding, failure carrier, publication precondition과 S3 ownership blocker를 재검토한다.
4. Root/child POM, wrapper, target source/test, empty placeholder directory와 evidence inventory를 기록한다.
5. Accepted Phase 00 뒤 legacy source의 exact 위치를 다시 찾고 UUID/time/default/listing behavior를 AST/source로 고정한다.
6. Public schema, fingerprint, CLI exit, limit와 filesystem atomicity를 `PROPOSED/OPEN`으로 manifest에 남긴다.
7. Rollback owner와 “new local entrypoint만 disable, legacy artifact 불변” 경계를 기록한다.

#### 근거

- Master §15.10 migration
- Canonical Phase 08 §3~§5
- Phase 08 review F-P08-002, F-P08-006, F-P08-008, F-P08-013
- Master Plan evidence/status 규칙

#### 금지 shortcut

- Current POM/package-info 존재를 accepted Phase 00/08 artifact로 사용
- Legacy source가 이동 중인데 옛 path를 hard-code
- Root placeholder test 1건을 Phase 08 regression/evidence로 제출
- 인접 whole-file hash가 같다는 이유로 semantic contract를 승인
- `TBD` scheduler ID를 임의 생성

#### Future verification

```bash
git rev-parse HEAD
git status --short
git rev-parse HEAD:docs/implementation/phases/phase-08-application-ports-local-runtime.md
git hash-object docs/implementation/execution-progress-and-results.md

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f build/architecture-rules/pom.xml \
  -Dtest=LegacyPlaceholderSourceCharacterizationTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

#### 기대와 실패 해석

- Expected: Exact source/contract/version/evidence refs가 있고 characterization method가 모두 discovered/green이다.
- Missing accepted Phase 07 evidence: WP-08.1 pure scaffold 가능 여부를 scheduler가 판단하지만 real pipeline/E2E/publication은 blocked다.
- Source path drift: Oracle/test와 compatibility matrix를 새 accepted location에 맞춘다. Target default는 바꾸지 않는다.
- Test 0 discovered: Build success가 아니라 false green이다.

#### Rollback, 마지막 안전 지점과 handoff

- Rollback: Production code를 만들지 않았으므로 read-only receipt와 test skeleton만 폐기/수정한다.
- Last safe: Canonical/reviewed documents, accepted predecessor refs와 untouched legacy artifact.
- Handoff: Approved entry receipt, live inventory, legacy matrix와 proposed/open list를 WP-08.1에 넘긴다.

#### 사람 checkpoint

사람이 “actual/present”, “empty placeholder”, “future target”, “open/blocker” 네 상태를 표에서 구분하고 서명한다. 하나라도 섞이면 WP-08.1로 가지 않는다.

### WP-08.1 — Provider-neutral identity, port, failure와 state contract

#### 목적과 필요한 이유

Provider나 filesystem 코드를 쓰기 전에 application이 소유할 pure Java 의미를 type으로 고정한다. 이 순서를 지켜야 S3/GCS/filesystem behavior가 application contract를 역으로 정의하지 않는다.

#### 사전조건

- WP-08.0 receipt가 green이다.
- Phase 00 module/package convention이 accepted됐다.
- Phase 07 public output contract를 compile/read할 수 있다.
- Tenant access, storage failure carrier, publication precondition decision은 승인됐거나 해당 부분을 red-test/open으로 남기고 implementation stop 범위를 명확히 했다.

#### 예상 file/package/type

- `application.port.in`: submit/execute/cancel/status/result
- `application.port.out`: artifact/state/publisher/profile/dispatcher/workflow/cancel/telemetry/clock
- `application.api/identity`: submission/solve/manifest/run/attempt/artifact/digest
- `application.execution`: lifecycle/transition/deadline/last safe point
- `ApplicationFailure`, `SubmissionReceipt`, `LocalExecutionOutcome`
- Architecture/signature/idempotency/state tests

#### 구체 행동

1. Accepted predecessor type를 import할 public projection과 내부 DTO를 분리한다.
2. Submission/solve/worker/attempt/artifact/state/publication identity의 equality와 version을 정의한다.
3. Command fingerprint inclusion/exclusion manifest를 먼저 test literal로 만든다.
4. Legal state/event/guard table을 `SolveTransitionPolicy` 밖의 oracle과 함께 만든다.
5. Phase 07 top-level `Publishable | Rejected`와 nested `VerificationRejected | GateIncomplete`를 각각 exhaustive sealed switch로 강제한다.
6. Approved non-ambient tenant access binding을 모든 command/query/storage/state/publication operation에 적용한다.
7. Approved storage failure carrier를 operation별 total mapping으로 적용한다.
8. Run-state fence와 publication-pointer precondition을 서로 다른 type/identity로 표현한다.
9. Public signature에서 `Path`, URI, HTTP/Jackson, provider SDK, mutable map/bytes를 제거한다.
10. Phase 09 signature manifest와 Phase 10 reserved execution seam을 작성하되 backend/coordinator를 만들지 않는다.

#### 근거

- Current Architecture §6.4, §7, §12, §17
- Integrated design §3.4~3.6, §12
- Phase 08 review F-P08-003~005, F-P08-007

#### 금지 shortcut

- `TenantId` parameter만 추가하고 authorized라고 주장
- Global/static/`ThreadLocal`/environment에서 caller scope 추론
- Storage failure를 unchecked exception이나 `AdapterUnavailable` 하나로 축소
- State version을 publication pointer token으로 재사용
- Generic `Map<String,Object>` state/result
- Application이 adapter/provider interface를 구현
- Phase 07 내부 substage를 fake outer state로 추가

#### Future verification

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f build/architecture-rules/pom.xml \
  -Dtest=Phase08ApplicationArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f rpdptw/application/pom.xml \
  -Dtest=ApplicationPortSignatureTest,SubmissionIdempotencyTest,ApplicationStateTransitionTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

#### 기대와 실패 해석

- Expected: Forbidden public reference/edge 0, required version gap 0, same/same convergence와 same/different conflict, illegal transition write 0.
- Access-binding test가 red: Security blocker가 아직 닫히지 않은 것이다. Ambient fallback으로 green 처리하지 않는다.
- Failure carrier compile branch 누락: Operation taxonomy가 lossless하지 않다.
- Publication token reuse green: Oracle가 충분히 민감하지 않거나 signature가 linearization point를 숨긴 것이다.

#### Rollback, 마지막 안전 지점과 handoff

- Rollback: Pure application type/port change만 되돌리고 adapter/backend는 건드리지 않는다.
- Last safe: WP-08.0 receipt와 accepted Phase 07 contract.
- Handoff: Approved API/signature manifest, state/failure table, Phase 09/10 projection을 WP-08.2/3에 넘긴다.

#### 사람 checkpoint

Security/Storage/Coordinator owner가 access/failure/precondition signature를 각각 승인한다. 승인되지 않은 row가 있으면 관련 port implementation은 stop한다.

### WP-08.2 — Application pipeline과 Phase 07 output의 exhaustive handling

#### 목적과 필요한 이유

Phase 01~07 service를 하나의 application path로 조립하고, Phase 07 success/rejection/incomplete가 상태·artifact·외부 outcome에서 의미를 잃지 않게 한다.

#### 사전조건

- WP-08.1 architecture/identity/state tests가 green이다.
- Phase 01~06 accepted service contract가 있다.
- Unit 시작에는 typed Phase 07 stub을 사용할 수 있지만 real integration에는 accepted Phase 07 implementation/evidence가 필요하다.

#### 예상 file/package/type

- `DefaultSubmitSolveService`
- `DefaultLocalSolveService`
- `DefaultRetrievalService`
- Versioned canonical adapter/preparation/profile/solver/Phase 07 composition
- `Phase07ApplicationIntegrationTest`
- `ResultRetrievalTest`

#### 구체 행동

1. §9.8 pseudocode 순서대로 stage를 조립한다.
2. Snapshot/manifest/candidate/replay/result artifact는 immutable put 후 exact ref로 재독해한다.
3. 먼저 `Publishable | Rejected`를 exhaustively switch하고, `Rejected` 안에서 `VerificationRejected | GateIncomplete`를 두 번째 exhaustive switch로 처리한다.
4. Candidate/result 두 PASS와 exact authority identity가 없으면 publisher call 전에 reject한다.
5. `VerificationRejected`의 stage/disposition/failure/optional PASS를 보존한다.
6. `GateIncomplete`의 stage/reason/optional PASS/last-safe identity를 보존하고 disposition을 만들지 않는다.
7. Retrieval은 published pointer와 verified ref만 사용하고 orphan/decoy/newest artifact를 무시한다.
8. Canonical payload bytes는 Phase 07 소유 그대로 저장하며 application이 다시 encode하지 않는다.
9. 각 state transition/side effect 앞에 cancel/deadline safe point를 명시한다.

#### 근거

- Master §4.1~4.2, §14.1
- Current Domain §13~16
- Actual Phase 07 §7.6~8.5
- Phase 08 review F-P08-001, F-P08-006~007

#### 금지 shortcut

- Stubbed Phase 07을 real E2E로 이름 변경
- Candidate PASS 하나로 success/publish
- `GateIncomplete`를 completed rejection/generic exception으로 축소
- Failure output에 route/outcome/payload 포함
- Application이 final result를 재계산/재직렬화
- Directory listing, newest mtime, artifact existence로 result 추론
- Exception을 all-unassigned/empty result로 변환

#### Future verification

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f rpdptw/application/pom.xml \
  -Dtest=Phase07ApplicationIntegrationTest,ResultRetrievalTest,ApplicationStateTransitionTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

#### 기대와 실패 해석

- Publishable: Artifact re-read 뒤 publisher call exactly 1.
- Rejection/incomplete/missing PASS/tamper/exception: Publisher call 0, normal payload 0.
- Retrieval: Listing/newest call 0, exact pointer digest만 반환.
- Real Phase 07 compatibility가 compile되지 않음: Upstream handoff drift이며 stub adapter로 숨기지 않는다.
- Payload digest가 application encode에 따라 달라짐: Ownership 위반이다.

#### Rollback, 마지막 안전 지점과 handoff

- Rollback: Service integration만 되돌리고 WP-08.1 pure ports/types를 유지한다.
- Last safe: Accepted Phase 07 artifact와 unpublished immutable Phase 08 artifacts.
- Handoff: Exact call graph, typed outcome, state/artifact sequence와 `E-P08-PORT` draft를 WP-08.3/6에 넘긴다.

#### 사람 checkpoint

Phase 07 owner와 independent reviewer가 top-level/nested 두 dispatch 경계의 field coverage와 publish call oracle를 함께 확인한다. Application log/telemetry만 보고 PASS하지 않는다.

### WP-08.3 — Local artifact/state/publication adapter와 crash safety

#### 목적과 필요한 이유

Application port의 의미를 explicit workspace의 local filesystem에서 구현한다. Filesystem은 partial write, symlink, race와 unsupported atomicity가 있으므로 “파일을 쓸 수 있다”보다 강한 contract가 필요하다.

#### 사전조건

- WP-08.1 storage semantics가 승인됐다.
- Non-ambient access binding, lossless failure carrier와 distinct publication precondition이 승인됐다.
- Local platform/target filesystem owner가 capability test 범위를 승인했다.
- Phase 09 object-common/provider code는 시작하지 않는다.

#### 예상 file/package/type

- `FileArtifactStore`
- `VersionedFileRunStateRepository`
- `AtomicFileResultPublisher`
- `LocalWorkspaceLayout`
- `FaultInjectingLocalIo`
- Concrete abstract-contract suites
- Crash/path/security/limit tests

#### 구체 행동

1. Typed ID를 versioned safe path segment로 encode하고 normalize/containment를 재검사한다.
2. 모든 parent/target component에서 symlink를 거부한다.
3. Input/config/profile은 regular file, declared/actual streaming length와 parser limit를 검사한다.
4. Immutable content를 same-filesystem staging에 쓰고 flush/digest/length 검증 후 no-replace atomic publish한다.
5. Existing same digest/bytes는 idempotent, different content는 conflict로 끝낸다.
6. State version body를 immutable write한 뒤 current pointer를 single-JVM CAS한다.
7. Result artifacts/closure를 검증한 뒤 published pointer를 create/CAS한다.
8. Write/flush/move/state/publish/response-loss 각각 one-shot fault를 주입한다.
9. Startup capability probe가 atomic create/replace/fsync contract를 증명하지 못하면 fail closed한다.
10. `LOCAL_SINGLE_JVM` scope를 manifest/evidence에 명시하고 cross-process/distributed CAS를 주장하지 않는다.

Proposed local layout은 adapter detail일 뿐 application identity가 아니다.

```text
<workspace>/
└── tenants/<safe-tenant>/
    └── solves/<safe-solve>/
        ├── artifacts/<kind>/<artifact-id>/<digest>
        ├── state/versions/<version>-<digest>
        ├── state/current
        ├── cancellation/<cancellation-id>
        └── results/
            ├── payloads/<digest>
            └── published
```

#### 근거

- Current Architecture §6.6, §10, §12, §17
- Integrated design §12.4, §13.1~13.8
- Canonical Phase 08 §8.3
- Phase 08 review F-P08-003~005

#### 금지 shortcut

- Raw identifier를 path에 concatenate
- Symlink follow 또는 lexical normalize만으로 containment 주장
- Check-then-write, last-write-wins, non-atomic fallback
- File lock을 provider-neutral/distributed CAS로 승격
- Prefix/directory scan을 normal retrieval authority로 사용
- Missing/denied/corrupt를 empty/not-found/success로 축소
- Existing pointer를 blind overwrite/rollback

#### Future verification

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f adapters/common/pom.xml \
  -Dtest=FileArtifactStoreContractTest,VersionedFileRunStateRepositoryContractTest,AtomicFileResultPublisherContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f adapters/common/pom.xml \
  -Dtest=LocalFilesystemCrashSafetyTest,LocalWorkspaceSecurityTest,LocalInputLimitTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

#### 기대와 실패 해석

- Pointer-to-partial 0, outside-workspace read/write 0, stale overwrite 0.
- Same/same exact ref 수렴, same/different original bytes 불변.
- Unsupported filesystem에서 success fallback 0.
- Response loss 뒤 exact same published ref로 수렴.
- Symlink/race로 escape 가능: Adapter support 선언을 철회하고 evidence를 폐기한다.
- Cross-process test가 우연히 pass: Declared scope를 넓히는 evidence가 아니다.

#### Rollback, 마지막 안전 지점과 handoff

- Rollback: Filesystem adapter를 supported distribution에서 제거하고 in-memory fake/port contract로 돌아간다.
- Last safe: WP-08.1/2 ports/services, 기존 authoritative pointer, orphan으로만 남은 immutable artifact.
- Handoff: Local capability manifest, crash matrix, security assumptions와 Phase 09 extension gaps를 WP-08.4/7에 넘긴다.

#### 사람 checkpoint

Platform/Security owner가 실제 target filesystem에서 capability와 crash test를 확인한다. Java API 존재나 developer laptop 한 번 성공만으로 승인하지 않는다.

### WP-08.4 — Explicit CLI/config, input limits와 local composition

#### 목적과 필요한 이유

사람이 재현 가능한 방식으로 local reference를 실행하게 한다. CLI는 단순 편의 wrapper가 아니라 hidden environment/default를 차단하고 application use case 하나로 들어가는 mandatory inbound adapter다.

#### 사전조건

- WP-08.2 application service와 WP-08.3 local adapter가 green이다.
- Security/limit reviewer가 지정됐다.
- Test-only execution/limit/watchdog 값이 명시적으로 분류됐다.
- Separate-process cancel은 scope에 넣지 않는다.

#### 예상 file/package/type

- `adapters/common`: bounded reader/versioned mapper/safe failure presenter
- `apps/cli`: parser/main/failure mapper/output
- `apps/cli`: main/parser/presenter/config/bootstrap/composition root와 executable packaging
- `LocalRuntimeConfig`, `LocalInputLimits`, `LocalResourceLimits`
- CLI configuration/security tests

Mandatory CLI 후보:

```text
ro-next-local solve  --config <explicit-config-file>
ro-next-local status --workspace <explicit-workspace> --tenant <id> --solve <id>
ro-next-local result --workspace <explicit-workspace> --tenant <id> --solve <id>
```

Blocking `solve`의 interrupt/control channel은 `RequestCancellationUseCase`로 cooperative intent를 보낸다. 별도 process의 `cancel` subcommand는 cross-process state/lease/CAS evidence 전에는 만들지 않는다.

#### 구체 행동

1. Config file path만 CLI argument로 받고 semantic/runtime/security field는 config에 모두 명시한다.
2. Contract/schema, tenant/authorized binding, submission/idempotency, workspace/input/profile/version/preset, algorithm/version/seed/work, build/runtime, limits/watchdog, telemetry, `LOCAL_SINGLE_JVM` scope를 required로 검증한다.
3. CWD/home/temp/environment/locale/timezone/classpath-first/`latest`에서 누락값을 채우지 않는다.
4. Metadata length → bounded stream → parser token/depth/string/number → schema/domain → resource budget 순으로 검사한다.
5. `apps/cli` composition root는 `adapters/common` local package, capability/profile만 조립하고 cloud/DB/workflow/vendor dependency를 넣지 않는다.
6. Executable JAR/launcher의 main class를 `apps/cli` 내부 `LocalCliMain`으로 고정하고 explicit test config로 bootstrap smoke를 실행한다.
7. Stdout machine-readable result/ref와 stderr safe diagnostic을 분리한다.
8. Safe failure에는 correlation ID와 bounded code만 노출한다.
9. Optional HTTP는 별도 scope/schema/auth/TLS/threat review 없이는 만들지 않는다.

#### 근거

- Canonical Phase 08 §8.1~8.6, §10
- Phase 08 review F-P08-010
- Question register `Q-BENCH-02`
- Integrated design §19~21

#### 금지 shortcut

- Missing config에 legacy 8/5000/900/300, port 8080, current time/UUID 사용
- `tenantId`/OS user/workspace owner를 authorization binding으로 대체
- Unbounded `readAllBytes`, parser depth/string/number
- Limit 초과 truncate/clamp/partial parse
- Root legacy server를 local reference로 이름 변경
- CLI가 solver/verifier/filesystem을 직접 호출해 application use case 우회
- Unauthenticated/non-loopback HTTP를 local default로 제공

#### Future verification

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f apps/cli/pom.xml \
  -Dtest=LocalCliConfigurationTest,LocalCliLauncherSmokeTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f apps/cli/pom.xml \
  -Dtest=LocalCliInputLimitIntegrationTest,LocalCliWorkspaceSecurityIntegrationTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f build/architecture-rules/pom.xml \
  -Dtest=Phase08ApplicationArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

#### 기대와 실패 해석

- Required field omission 각각 bootstrap 전에 failure.
- Environment/CWD/home가 누락값을 보완한 case 0.
- Cloud/DB/route-selection dependency 0.
- Packaged main class는 `apps/cli` 안에서 resolve되고 downstream distribution classpath 없이 bootstrap된다.
- Input boundary는 limit-1/limit pass, limit+1 typed reject.
- Safe error/log leak count 0.
- Config omission이 통과: Hidden default bug이며 distribution release를 중단한다.

#### Rollback, 마지막 안전 지점과 handoff

- Rollback: Local distribution/CLI entrypoint를 disable하고 WP-08.2/3 module을 유지한다.
- Last safe: Application API, verified local adapter contract와 pointer 없는 workspace.
- Handoff: Config schema/version, composition manifest와 security/limit report를 WP-08.5/6에 넘긴다.

#### 사람 checkpoint

Product/API/Security owner가 “required field set”은 검토하되 exact public exit code나 production 수치를 승인되지 않은 상태에서 freeze하지 않는다.

### WP-08.5 — Cancellation, deadline, retry와 telemetry fault semantics

#### 목적과 필요한 이유

실행이 중단되는 이유를 정상 품질 종료와 분리하고, retry/race 뒤에도 같은 logical identity와 last safe state를 보존한다.

#### 사전조건

- WP-08.1 state/failure type, WP-08.2 service와 WP-08.4 composition이 green이다.
- Injected monotonic clock, cancellation probe와 deterministic barrier/fault hook가 있다.
- Solver safe-point/COW discard contract가 accepted됐다.

#### 예상 file/package/type

- `DefaultCancellationService`
- `CancellationPort` local implementation
- `RunDeadline`, `LastSafePoint`
- Retry/attempt mapper
- `TelemetryPort` test recorder와 safe event types
- Cancellation/deadline/retry/telemetry tests

#### 구체 행동

1. Cancel request → idempotent intent → legal state CAS → worker observation → uncommitted trial discard → actual stop → last safe point → `CANCELLED` 순서를 구현한다.
2. Cancel/publish race와 stale CAS를 deterministic barrier로 제어한다.
3. Completed semantic step과 incomplete trial의 accounting boundary를 고정한다.
4. Algorithm work budget, application watchdog, transport/platform timeout을 다른 type/state로 둔다.
5. Retry에서 `AttemptId`만 바뀌고 worker/seed/work/warm start/manifest는 유지되는 guard를 둔다.
6. Required event family, correlation field, safe allowlist와 sensitive denylist를 구현한다.
7. Telemetry failure policy를 explicit config로 둔다. Security audit event를 best-effort로 조용히 버리지 않는다.
8. Clock/event ordering을 바꿔 semantic result가 같은지 검사한다.

#### 근거

- Master §13
- Current Architecture §10, §12, §17
- Canonical Phase 08 §9~10
- Integrated design §19~21

#### 금지 shortcut

- Cancel intent 성공을 actual worker stop으로 간주
- Cancel/watchdog/platform timeout을 `MAX_STEPS_REACHED` 또는 `NO_STRICT_IMPROVEMENT`로 변환
- Incomplete step에서 completed count/adaptive/temperature 전진
- Retry에 새 seed/work/warm start/worker ID
- First-completed result를 champion/success로 선택
- Elapsed/event order를 semantic fingerprint나 objective에 포함
- Raw input/PII/secret/path/stack trace를 telemetry에 기록

#### Future verification

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f rpdptw/application/pom.xml \
  -Dtest=CancellationSemanticsTest,DeadlineSemanticsTest,WorkerRetryIdentityTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f apps/cli/pom.xml \
  -Dtest=TelemetryContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

#### 기대와 실패 해석

- Intent/observation/actual termination이 각각 관찰된다.
- Incomplete step mutation/count 0.
- Race terminal winner exactly 1, pointer-to-nonwinner 0.
- Response-loss retry는 same published ref.
- Retry identity에서 attempt 이외 변화 0.
- Sensitive telemetry leak 0, schedule/elapsed에 따른 semantic digest 변화 0.
- COW discard를 증명하지 못함: Cancellation support를 incomplete로 표시하고 E2E acceptance를 중단한다.

#### Rollback, 마지막 안전 지점과 handoff

- Rollback: Cancel/deadline feature를 supported surface에서 제거하고 last green application path로 돌아간다.
- Last safe: 마지막 committed candidate/state/pointer, unpublished immutable fault artifact.
- Handoff: Idempotency/cancel/deadline/retry fault matrix와 telemetry/redaction report를 WP-08.6에 넘긴다.

#### 사람 checkpoint

Algorithm owner는 incomplete step/accounting을, Security/Operations owner는 termination/telemetry를 각각 확인한다. 하나의 “timeout test”로 세 경계를 모두 승인하지 않는다.

### WP-08.6 — Real local E2E, deterministic rerun와 exact retrieval

#### 목적과 필요한 이유

Stub이 아닌 실제 Phase 01~07 경로로 request→both-gate→publication→retrieval이 완결되고, workspace/observation 차이가 결과 의미를 바꾸지 않음을 증명한다.

#### 사전조건

- WP-08.0~5 required tests가 green이다.
- Phase 01~07 implementation/evidence/review/acceptance receipt가 모두 유효하다.
- `P08_LOCAL_PD_3_TEST_ONLY`와 predecessor expected oracle이 test-only로 승인됐다.

#### 예상 file/package/type

- `LocalCliEndToEndTest`
- Fresh-workspace replay harness
- Exact result/status reader oracle
- E2E evidence recorder
- Explicit fixture/config/profile/input resources

#### 구체 행동

1. Empty explicit workspace에서 `solve`를 실행한다.
2. Input/canonical/problem/travel/profile/manifest/candidate/replay/verifier/result/pointer lineage를 exact ref/digest로 검사한다.
3. `status`와 `result`가 exact `SolveId`/published ref만 조회하는지 decoy files로 검사한다.
4. 두 fresh workspace에서 same manifest를 실행해 semantic result/payload identity를 비교한다.
5. Same workspace same-command retry가 overwrite 없이 exact existing refs로 수렴하는지 검사한다.
6. Same submission/different input conflict가 solver/Phase 07/publisher call 전에 나는지 검사한다.
7. Rejection, incomplete, cancel, limit, path attack과 response-loss case를 실행한다.
8. Stub Phase 07 report와 real Phase 07 report를 별도 classification으로 기록한다.
9. Observation/path/process differences inclusion/exclusion manifest를 evidence에 넣는다.

#### 근거

- Current Architecture §6.6, §10, §18~19
- Canonical Phase 08 §8.5, §11.9, WP-08.6
- Master reproducibility §13.2
- Phase 07 both-gate handoff

#### 금지 shortcut

- Fake/stub Phase 07을 full E2E로 제출
- In-memory adapter만 실행하고 local filesystem E2E라 주장
- Expected result file을 actual output으로 자동 갱신
- 한 workspace run만으로 reproducibility 주장
- Decoy/newest/listing retrieval을 허용
- Failed/incomplete run을 bundle에서 제외
- `win_poc_case_floor.json` 1회 성공을 benchmark acceptance로 승격

#### Future verification

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f apps/cli/pom.xml \
  -Dtest=LocalCliEndToEndTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean verify

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -pl rpdptw/application,adapters/common,apps/cli \
  -am clean verify
```

두 번째 명령은 module set의 full suite이며 selected-test evidence를 대신하지 않는다.

#### 기대와 실패 해석

- Success lineage에 candidate PASS와 result PASS가 모두 있다.
- Fresh workspace result/payload fingerprint/digest/length가 exact 같다.
- Same workspace retry overwrite 0, same refs.
- Rejection/incomplete/cancel/limit/conflict에 published pointer와 normal payload 0.
- Required failure/error/skipped 0.
- Result 차이의 declared semantic input이 없음: Reproducibility defect이며 bundle을 발행하지 않는다.
- Real Phase 07 bypass: Application integration은 green일 수 있어도 E2E는 fail이다.

#### Rollback, 마지막 안전 지점과 handoff

- Rollback: New local entrypoint를 disable한다. Failed workspace의 immutable artifact/pointer 상태는 evidence로 보존한다.
- Last safe: Last green module artifacts, Phase 07 accepted bundle와 이전 pointer.
- Handoff: `E-P08-LOCAL-E2E` candidate, config/fixture/oracle digest와 rerun comparison을 WP-08.7에 넘긴다.

#### 사람 checkpoint

Independent oracle owner가 expected result provenance와 two-workspace equality를 확인한다. Implementer가 만든 output을 implementer가 expected로 승인하지 않는다.

### WP-08.7 — Migration boundary, immutable evidence, independent review와 Phase 09/10 handoff

#### 목적과 필요한 이유

기능 green을 acceptance로 바꾸기 전에 legacy isolation, evidence provenance, downstream compatibility와 rollback을 독립 검토한다.

#### 사전조건

- WP-08.0~6 required tests가 모두 green이다.
- Legacy/new distributions가 물리적으로 격리됐다.
- Independent reviewer와 Phase 09/10 consumer가 지정됐다.
- Progress/status update는 scheduler가 수행한다.

#### 예상 file/package/type

- `LegacyMigrationBoundaryTest`
- Architecture/dependency/security reports
- `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY`
- `Phase08ApplicationStorageContractManifest`
- `Phase08ExecutionSeamManifest`
- Pre-review manifest, independent review, post-review receipt

#### 구체 행동

1. New `apps/cli` executable artifact가 legacy controller/GCP client를 invoke/modify하지 않는지 검사한다.
2. Shadow comparison은 side-effect-free namespace에서만 실행하고 mismatch/no-both-gate 결과의 publication을 금지한다.
3. New entrypoint disable/rollback 후 legacy source/artifact fingerprint가 같은지 검사한다.
4. Exact command/toolchain/source/build/test count/config/fixture/oracle/input/output/fault/rollback digest를 세 evidence bundle에 봉인한다.
5. Pre-review manifest를 review 전에 content-addressed immutable artifact로 고정한다.
6. Independent reviewer는 manifest digest만 입력으로 report를 만들고 findings/verdict를 별도 봉인한다.
7. Exit gate와 review가 모두 PASS일 때만 acceptance receipt가 manifest+review digest를 참조한다.
8. Phase 09/10 owner가 handoff signature/meaning을 소비자 입장에서 검토한다.
9. Scheduler가 receipt를 확인한 뒤에만 authoritative status를 갱신한다.

#### 근거

- Master Plan §9~§12 evidence DAG
- Canonical Phase 08 §13~§18
- Phase 08 review §6~§9
- Master §16.2 migration

#### 금지 shortcut

- Pre-review manifest에 reviewer/verdict/acceptance를 미리 넣거나 backfill
- Test report/console/path만 있고 bytes/digest 없는 evidence
- 서로 다른 commit/report의 좋은 결과 조합
- Independent review PASS 전 `ACCEPTED`
- Phase 09/10 consumer receipt 없이 handoff ready
- Legacy result rewrite, production deploy/traffic switch
- Orphan artifact를 삭제해 fault evidence를 숨김
- Phase 08에서 object-common/S3/coordinator/AWS code 추가

#### Future verification

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f build/architecture-rules/pom.xml \
  -Dtest=Phase08ApplicationArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f apps/cli/pom.xml \
  -Dtest=LegacyMigrationBoundaryTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -pl rpdptw/application,adapters/common,apps/cli \
  -am clean verify

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  clean verify
```

#### 기대와 실패 해석

- OR-Tools/cloud/DB 없는 ALNS-only default reactor green.
- Required missing/failure/error/skipped 0.
- Legacy/new cross-call/write 0, rollback fingerprint exact.
- 세 evidence bundle의 bytes/ref/digest가 있고 independent review `PASS`.
- Phase 09/10 compatibility receipt가 있다.
- 하나라도 없음: 최대 `IMPLEMENTED_PENDING_EVIDENCE` 또는 `REVIEW_PENDING`; `ACCEPTED` 아님.

#### Rollback, 마지막 안전 지점과 handoff

- Rollback: New local entrypoint/distribution을 disable하고 legacy source/artifact를 유지한다.
- Last safe: Accepted predecessor artifacts와 기존 authoritative pointer; new immutable artifacts는 published/orphan 구분을 보존한다.
- Handoff: §16의 exact manifests, 세 evidence bundle, review/receipt와 rollback point를 Phase 09/10/scheduler에 넘긴다.

#### 사람 checkpoint

Implementer, independent reviewer, Phase 09 consumer, Phase 10 consumer와 scheduler의 역할을 분리한다. 한 사람이 implementation green, review verdict와 authoritative status를 한 번에 self-approve하지 않는다.

## 12. 검증 명령, pass 판정과 evidence

### 12.1 현재 실행 가능한 검증과 future Maven 명령 구분

현재는 source/inventory/document 검증만 실행 가능하다.

```bash
git status --short
git rev-parse HEAD
git ls-tree -r --name-only HEAD
find rpdptw build legacy adapters apps -type f -not -path '*/target/*' | sort
test -f mvnw
test -f .mvn/wrapper/maven-wrapper.properties
find rpdptw/application -type f -name '*.java' ! -name package-info.java
find rpdptw -type f -path '*/src/test/java/*' -name '*.java'
test ! -d distributions
test ! -d build/port-contract-tests
```

현재 wrapper/reactor 파일과 Phase 00 replacement evidence는 존재하지만 application
concrete source/test, `apps/cli` executable composition과 Phase 08 port-contract module은
없다. Scheduler registry에서 Phase 00은
`REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS / NOT_ACCEPTED`다. 따라서 이 가이드 검증에서는
Maven을 실행하지 않았으며, 아래 Phase 08 명령은 accepted Phase 00 handoff와 target이
생긴 뒤의 future command다.

Phase 00 accepted 뒤 toolchain 확인:

```bash
java -version
./mvnw -version
```

Expected baseline:

```text
Java feature release: 25
Maven: accepted Phase 00 Enforcer range
```

Canonical Phase 08은 Maven 3.9.14를 관찰했지만 accepted Phase 00 toolchain/evidence가 최종 실행 authority다.

Clean-checkout selected-test paired protocol:

```bash
# PHASE08_M2는 증거 작업공간 밖의 임의 default가 아니라, receipt에 기록할
# 새롭고 비어 있는 explicit absolute directory여야 한다.
test -n "${PHASE08_M2}"
test -d "${PHASE08_M2}"
test -z "$(find "${PHASE08_M2}" -mindepth 1 -print -quit)"

# 예: owner=adapters/common. -am clean과 exact sibling/Test JAR 설치 단계.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -pl adapters/common -am clean install -DskipTests

# 위 install은 dependency preparation일 뿐 test evidence가 아니다.
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="${PHASE08_M2}" \
  -f adapters/common/pom.xml \
  -Dtest=FileArtifactStoreContractTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

각 WP는 첫 명령의 `-pl`과 둘째 명령의 `-f`를 같은 owner로 바꾼다. Architecture
owner처럼 reusable Test JAR까지 필요한 경우 test-scope dependency가 reactor `-am`
closure에 반드시 나타나야 한다. `build/port-contract-tests`의 attached
`tests` classifier, consumer dependency tree, selected owner의 새 Surefire XML과
expected class/method manifest를 같은 source/build fingerprint로 봉인한다.

### 12.2 Future command matrix

| Layer | Owner command | Required pass |
|---|---|---|
| Architecture/signature | WP-08.1 commands | Forbidden edge/type 0, all expected methods discovered |
| Identity/state | WP-08.1 application command | Same/same 수렴, same/different conflict, illegal write 0 |
| Phase 07 integration | WP-08.2 command | Top-level/nested 두 switch exhaustive, non-success publish 0 |
| Storage/local adapter | WP-08.3 commands | Abstract suite + crash/security/limit exact |
| CLI/config | WP-08.4 commands | Missing field fallback 0, leak 0 |
| Cancel/deadline/retry/telemetry | WP-08.5 commands | Exceptional semantics와 identity 보존 |
| Real local E2E | WP-08.6 commands | Real Phase 01~07, both-gate, exact retrieval |
| Reproducibility | WP-08.6 fresh/same workspace cases | Semantic result/payload exact |
| Migration | WP-08.7 migration command | Legacy/new cross-call/write 0 |
| Application/local module set | WP-08.7 module-set verify | Required suites 전부 green |
| Full reactor | Root `clean verify` | ALNS-only, cloud/DB/MIP-free required reactor green |

Selected class command 전에는 §12.1의 같은 explicit isolated repository와 exact source
state로 owner의 reactor dependency closure를 `-pl <owner> -am clean install
-DskipTests`한다. 이 준비 단계는 pass evidence가 아니다. 그 뒤 selected command는
owner POM만 `-f`로 실행해 class filter가 다른 module에 전달되지 않게 하고, owner의
fresh report를 exact-manifest validator로 검사한다. Full module-set/root
`clean verify`는 filter 없이 별도 evidence로 실행한다.

### 12.3 Evidence bundle

#### `E-P08-PORT`

```text
canonical source receipt + semantic impact record
accepted predecessor/Phase07 handoff receipts
module/package/dependency/signature manifest
identity/fingerprint inclusion-exclusion manifest
legal state/failure/retry table
approved authorization binding contract
approved operation별 lossless failure carrier
distinct run-state/publication precondition contract
architecture/bytecode/jdeps report
fake/local abstract port contract report
Phase 09/10 consumer compatibility refs
OPEN/GATED/deferred snapshot
```

#### `E-P08-LOCAL-E2E`

```text
exact source/build/Java/Maven/runtime identity
fixture/input/profile/config/oracle bytes and digests
full request→canonicalization→snapshot→solve lineage
candidate/replay/termination identities
candidate PASS + result PASS reports
publishable/result/payload fingerprints and lengths
artifact/state/publication refs and versions
exact status/result retrieval
fresh-workspace and same-workspace comparison
security/input-limit/path/redaction assertions
structured telemetry assertion
real-vs-stub Phase 07 classification
exact test report inventory
```

#### `E-P08-IDEMPOTENCY`

```text
submission same/same and same/different matrix
artifact same/same, same/different and corruption matrix
state CAS contenders/version history
publication stale/occupied/same/different/response-loss matrix
worker retry identity and attempt lineage
cancel/publish race
watchdog/platform timeout cases
filesystem write/flush/move/pointer fault matrix
last safe point and rollback result per fault
```

### 12.4 Exact pass criteria

Phase 08 exit는 다음 AND 조건이다.

1. Phase 00~07 accepted receipt/review/evidence가 유효하다.
2. Required test class/method가 모두 discovered/executed되고 failure/error/skipped/aborted가 0이다.
3. Application public contract의 provider/transport/path/DB/vendor forbidden ref가 0이다.
4. Local distribution의 cloud/DB/route-selection dependency가 0이다.
5. Same submission+same command는 exact same solve/ref로 수렴한다.
6. Same submission+different input/profile/manifest는 solve 전 conflict다.
7. Artifact same/same은 idempotent, same/different는 original 불변 conflict다.
8. Stale state/publication writer overwrite가 0이다.
9. Access context 없는 operation과 cross-tenant existence disclosure가 0이다.
10. Storage failure taxonomy의 unchecked/generic collapse가 0이다.
11. `VerificationRejected`/`GateIncomplete`/missing PASS/tamper/exception의 normal publication이 0이다.
12. `Publishable`은 verified artifact re-read와 pointer CAS 뒤에만 `SUCCEEDED`다.
13. Cancellation intent/observation/actual termination/last safe point가 분리된다.
14. Watchdog/platform/resource/cancellation의 normal termination 변환이 0이다.
15. Input limits, path containment, symlink와 sensitive leak failure가 0이다.
16. Real Phase 01~07 local E2E가 완결된다.
17. 두 fresh workspace의 semantic result/payload identity가 exact 같다.
18. Same workspace retry overwrite 0, exact existing ref다.
19. Legacy/new isolation과 rollback rehearsal이 green이다.
20. 세 evidence bundle이 immutable bytes/ref/digest를 가진다.
21. Phase 09/10 consumer receipt가 있다.
22. Independent Phase 08 review가 `PASS`다.
23. Post-review acceptance receipt가 pre-review manifest와 review report digest를 함께 참조한다.

### 12.5 Performance test의 한계

Phase 08 성능 검증은 correctness를 보호하는 resource/behavior evidence다.

- Bounded input read/parser/artifact/workspace memory
- Artifact bytes/read/write counts
- CAS conflict와 retry/cancel latency observation
- Same semantic result under thread/schedule variation
- No unbounded directory scan/listing

공식 latency, throughput, memory, file size, worker/step/watchdog threshold는 승인 전 `OPEN — EXPERIMENT_REQUIRED`다. 임시 test 수치가 빠르다고 production SLO나 Phase 14A acceptance를 선언하지 않는다.

## 13. 사람 checkpoint, stop·resume와 상태 권한

### 13.1 WP별 checkpoint

| Checkpoint | 사람이 직접 확인할 것 | Stop 조건 |
|---|---|---|
| C0 entry | Source semantic diff, accepted predecessor refs, live inventory/rollback | Accepted Phase 00~07/owner/task 없음 |
| C1 contract | Identity/state/failure/access/precondition totality | Ambient trust, generic collapse, reused token |
| C2 pipeline | Top-level 2 + nested rejection 2와 exact publish call count | 평탄화된 top-level 3-way switch, One-PASS/fake disposition/normal failure payload |
| C3 local adapter | Atomic capability, symlink/containment, fault last pointer | Unsupported FS fallback/outside access/partial pointer |
| C4 CLI/security | Required explicit field, limits, safe presentation | Hidden default/unbounded read/leak |
| C5 execution faults | Cancel/deadline/retry/telemetry 의미 | Normal termination conversion/identity drift |
| C6 E2E | Real Phase 01~07, both PASS, fresh/same workspace | Stub bypass/result mismatch/listing retrieval |
| C7 evidence | Exact reports/digests/consumer review/rollback | Missing/skip/stale/mixed source |
| C8 acceptance | Independent PASS + valid receipt + scheduler update | Self-approval 또는 production overclaim |

### 13.2 Stop 규칙

다음이면 즉시 해당 WP의 side effect를 멈춘다.

- Predecessor receipt와 implementation artifact가 일치하지 않는다.
- Authorization context가 없는데 object existence를 조회해야 한다.
- Failure result를 contract가 표현할 수 없다.
- CAS linearization point 또는 expected token owner가 모호하다.
- Phase 07 field를 합성/삭제해야 compile된다.
- Filesystem이 required atomicity를 증명하지 못한다.
- Same replay identity가 다른 verified digest를 만든다.
- Required test가 discover되지 않거나 skip됐다.
- Result pointer가 partial/unverified artifact를 가리킨다.
- OPEN/GATED/deferred 값을 default로 채워야만 진행된다.

### 13.3 Stop 때 기록할 것

```text
blocked requirement/decision
owner and required approval
source section/contract identity
last completed WP/test
last authoritative state/pointer
created immutable artifact refs and orphan/published classification
fault/command/toolchain/source fingerprint
safe rollback/disable action
resume first red test
```

### 13.4 Resume 절차

1. Owner/scheduler가 exact blocker receipt와 changed source/contract를 제공한다.
2. §3 source Git blob/live hash와 adjacent semantic diff를 다시 확인한다.
3. Requirement→contract→WP→test/evidence 영향을 갱신한다.
4. Last safe pointer와 artifact closure를 verified-read한다.
5. Orphan/unpublished artifact를 success로 재분류하지 않는다.
6. Affected WP의 첫 red test부터 재개한다.
7. Downstream consumer compatibility receipt를 다시 받는다.

### 13.5 상태 변경 권한

Implementer는 implementation/evidence proposal을, reviewer는 independent verdict를 제출한다. `execution-progress-and-results.md`의 authoritative registry/status/result는 total scheduler만 갱신한다.

```text
source exists        ≠ IMPLEMENTED
tests green          ≠ EVIDENCE COMPLETE
evidence sealed      ≠ REVIEW PASS
review PASS          ≠ ACCEPTED without receipt
Phase 08 ACCEPTED    ≠ benchmark accepted
benchmark accepted   ≠ production authority
```

## 14. 흔한 오해와 anti-pattern

- CLI/controller/Lambda가 solver/verifier/filesystem을 직접 호출해 use case 우회
- Adapter/provider가 port를 정의하고 application이 구현
- Application public contract에 `Path`, URI, HTTP context, Jackson node, mutable map/bytes
- Raw input을 solver가 parse하거나 prepared travel 뒤 coordinate/speed fallback
- Customer/profile `latest`, classpath first, other-tenant fallback
- `System.nanoTime()`, random UUID, CWD/home/temp/env로 identity/seed/config 생성
- Legacy 8/5000/900/300/8080 값을 official/local default로 승격
- Caller `TenantId`, global/static/`ThreadLocal`, environment를 authorization으로 신뢰
- Provider locator/path를 semantic artifact/result identity로 사용
- Missing/denied/corrupt/stale/indeterminate를 not-found/generic exception으로 collapse
- Same identity/different digest overwrite 또는 last-write-wins
- Check-then-put, CAS conflict 무시, stale retry without reload
- Run-state version과 publication pointer token 재사용
- Multi-object transaction, directory rename, file lock을 common contract로 가정
- Prefix/directory listing, newest mtime, event order, object existence로 status/result/completeness 추론
- Atomic operation 미지원 때 non-atomic fallback
- Raw identifier path concatenation, symlink follow, lexical-only containment
- Declared length만 믿고 unbounded stream/parser 실행
- Limit 초과를 truncate/clamp/empty/all-unassigned로 변환
- Candidate PASS 하나로 publication
- `GateIncomplete`에 disposition을 합성하거나 completed rejection으로 변경
- Verification exception을 all-unassigned/empty route/HTTP success로 변환
- Failure output에 normal route/outcome/payload/benchmark vector
- Phase 07 payload를 application/transport가 재인코딩해 digest 의미 변경
- Result bytes를 먼저 응답하고 publication CAS를 나중에 수행
- Cancellation intent를 actual stop으로 간주
- Watchdog/platform timeout을 normal ALNS termination으로 변환
- Retry에서 seed/work/warm start/logical worker identity 변경
- Completion order/thread first-winner를 result 선택에 사용
- Elapsed/path/process/event order를 semantic fingerprint나 objective에 포함
- Secret/raw address/PII/full input/path/stack trace를 log/error/metric에 노출
- Stubbed Phase 07이나 in-memory fake를 real local E2E로 제출
- Stale `target/`, zero-test, skipped test, console success를 evidence로 제출
- Phase 08에서 object-common/S3/coordinator/AWS deployment 구현
- Phase 13 MIP/OR-Tools 또는 `Q-VAR-01` variant를 config option으로 미리 추가
- Phase 08 local pass를 Phase 14A/14B authority로 승격

## 15. 실제 구현 exit checklist와 Definition of Done

### 15.1 Entry checklist

- [ ] Canonical source Git blob과 live drift의 semantic impact를 재검토했다.
- [ ] Phase 00~07 accepted receipt/evidence/review가 있다.
- [ ] Phase 07 exact top-level 2 + nested rejection 2 output와 handoff manifest가 있다.
- [ ] Scheduler task와 implementer/security/storage/reviewer/consumer owner가 정해졌다.
- [ ] Full-solution evaluation authority blocker가 닫혔다.
- [ ] Non-ambient tenant authorization binding이 승인됐다.
- [ ] Operation별 lossless storage failure carrier가 승인됐다.
- [ ] Run-state fence와 publication precondition identity가 승인됐다.
- [ ] Phase 09/10 worker committed-outcome boundary가 기록됐다.
- [ ] Phase 09/11 S3 ownership decision이 provider work 전에 승인됐다.
- [ ] Fingerprint/CLI/limit/FS proposed/open 항목이 manifest에 있다.
- [ ] First architecture/identity test가 의도한 이유로 red다.

### 15.2 구현 exit checklist

- [ ] Application module이 use case, identity, lifecycle, failure와 port를 소유한다.
- [ ] Provider/transport/path/DB/vendor forbidden dependency가 0이다.
- [ ] Request→canonicalization→solve→Phase 07→publication→retrieval이 한 path다.
- [ ] Phase 07 top-level 2 variant와 nested rejection 2 variant가 각각 field loss 없이 exhaustive하게 처리된다.
- [ ] Failure/incomplete output에 normal route/outcome/payload가 없다.
- [ ] Command/config에 semantic/runtime/security hidden default가 없다.
- [ ] Caller tenant claim은 explicit non-ambient authorization closure로 검증된다.
- [ ] Missing/wrong access가 existence/backend call 전에 fail한다.
- [ ] Storage failure taxonomy가 lossless하게 application으로 전달된다.
- [ ] Artifact same/same과 same/different semantics가 exact하다.
- [ ] State/publication CAS와 distinct precondition identity가 race에서 작동한다.
- [ ] Filesystem adapter가 `LOCAL_SINGLE_JVM` scope와 capability를 정직하게 선언한다.
- [ ] Cancellation/deadline/retry가 원래 의미와 last safe point를 보존한다.
- [ ] Input/path/symlink/tenant/redaction/security test가 green이다.
- [ ] Real Phase 01~07 local E2E가 both-gate 뒤에만 success다.
- [ ] Fresh-workspace result/payload identity가 exact 같다.
- [ ] Same-workspace retry가 overwrite 없이 same refs로 수렴한다.
- [ ] Corruption/fault/crash/race test가 last pointer를 보존한다.
- [ ] Legacy/new isolation과 rollback rehearsal이 green이다.
- [ ] Required test missing/failure/error/skipped/aborted가 0이다.
- [ ] `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY`가 immutable digest를 가진다.
- [ ] Phase 09/10 consumer compatibility receipt가 있다.
- [ ] Independent Phase 08 review가 PASS다.
- [ ] Valid post-review acceptance receipt가 있다.
- [ ] Scheduler가 authoritative status를 갱신했다.
- [ ] OPEN/GATED/deferred/optional/official/production 값을 발명하지 않았다.

### 15.3 Phase 08 Definition of Done

Phase 08 `ACCEPTED`는 다음 AND gate다.

1. Provider-neutral application contract가 compile dependency로 강제된다.
2. Phase 07의 both-gate 의미가 storage/CLI/retry/failure에서도 보존된다.
3. Local filesystem과 same-process runtime이 immutable artifact + one-pointer CAS semantics를 증명한다.
4. Identity, idempotency, cancellation, deadline, retry와 failure가 type/test로 구분된다.
5. Tenant/path/input/resource/redaction boundary가 fail closed한다.
6. Real local E2E와 deterministic rerun이 독립 oracle로 통과한다.
7. Legacy migration/rollback이 target semantics를 오염시키지 않는다.
8. Immutable evidence, independent review, consumer receipt와 acceptance receipt가 있다.

이 DoD는 object storage/S3, multi-round coordinator, AWS distribution, benchmark quality acceptance, optional Phase 13 또는 production cutover를 완료했다는 뜻이 아니다.

## 16. 이전·다음 Phase handoff와 broken 증상

### 16.1 Phase 07에서 받아야 할 것

```text
Phase07HandoffManifest
  contract/version/source/build/runtime
  problem/travel/profile/evaluation/SolvePlan/candidate/replay identities
  candidate pass or safe rejection
  verified solution/audit/result/result-pass identities when PASS
  canonical payload digest/length when PASS
  Phase07Output contract fingerprint
  E-P07-CANDIDATE-VERIFY / E-P07-AUDIT / E-P07-RESULT-VERIFY
  accepted review/receipt
  rollback point

Phase07Output
  Publishable(PublishableResult)
  or Rejected(VerificationRejected)
  or Rejected(GateIncomplete)
```

받으면 안 되는 것:

- Mutable builder/finalizer/auditor/search state
- Search/insertion/propagation/metric cache
- Solver feasible flag, summary나 raw objective만 있는 candidate
- One-PASS partial result
- Normal route/outcome/payload를 포함한 failure
- Provider locator/client/credential
- Missing official 값을 채운 default

### 16.2 Phase 09에 넘길 storage contract

```text
Phase08ApplicationStorageContractManifest
  contractVersion
  Phase08 accepted evidence/review/receipt refs
  TenantId / SolveId / ArtifactKey / ArtifactRef projections
  algorithm-tagged digest/length/schema/media semantics
  OpaqueLocator non-semantic rule
  approved tenant-scoped access binding/session
  operation별 lossless failure carrier
  ArtifactStore put/read/verify semantics
  RunStateRepository create/get/CAS semantics
  distinct run-state fence/publication precondition
  ResultPublisher both-gate CAS semantics
  exact retrieval/no-list rule
  local execution scope = LOCAL_SINGLE_JVM
  abstract port contract source/digest
  E-P08-PORT / E-P08-IDEMPOTENCY
  rollback point
```

Phase 09는 object-common/provider conditional mechanics를 자유롭게 구현할 수 있지만 Phase 08 logical meaning, same/different digest, both-gate publication, exact retrieval, tenant/failure/precondition 계약을 바꾸면 안 된다.

### 16.3 Phase 10에 넘길 execution seam

```text
Phase08ExecutionSeamManifest
  SolveId / ManifestFingerprint
  WorkerRunId / AttemptId
  WorkerAssignment and retry invariants
  WorkerDispatcher logical operations
  WorkflowExecutionPort reserved operations
  Cancellation intent semantics
  RunDeadline/termination separation
  RunStateRepository CAS
  Publishable/VerificationRejected/GateIncomplete outcomes
  telemetry correlation/event contract
  deterministic single-worker local oracle
```

Phase 10이 추가로 소유하는 것은 `ExecutionRound`, declared worker set/completeness, durable wakeup/reconcile, stable fan-in/champion과 multi-round transition이다. Phase 08 single-worker success를 round completeness로 사용하지 않는다.

### 16.4 Phase 14A handoff

Phase 08 acceptance 뒤 Phase 14A는 다음을 소비한다.

- Dataset/fixture/input/profile/config digest
- Build/runtime/hardware fingerprint
- Actual seed/repeat/run identity
- Requested/completed work와 termination
- Timeout/resource observation
- Objective vector와 result fingerprint
- Candidate/result verifier report
- Canonical payload/trace digest
- 모든 declared run의 success/failure/incomplete 상태

Phase 08은 corpus, repeat 수, threshold, budget, variance 기준을 정하지 않는다. 이것들은 `OPEN — EXPERIMENT_REQUIRED`이며 Phase 14A owner가 승인된 protocol로 닫는다.

### 16.5 Handoff가 깨졌다는 증상

| 증상 | 깨진 계약 | 되돌아갈 owner/WP |
|---|---|---|
| Application compile에 AWS/GCP/HTTP/`Path`가 필요 | Provider-neutral DAG 실패 | Phase 00/08, WP-08.1 |
| Same submission retry가 새 solve/seed를 만듦 | Idempotency/replay 실패 | WP-08.1/5 |
| Caller `TenantId`만으로 read가 됨 | Authorization closure 실패 | Security/Phase 09, WP-08.1/3 stop |
| Access denied와 corrupt가 같은 generic exception | Lossless failure 실패 | Phase 08/09, WP-08.1/3 |
| State version 하나로 publish pointer를 commit | Precondition identity 실패 | Phase 08/09/10, WP-08.1/3 |
| GateIncomplete에 disposition이 생김 | Phase 07 field synthesis | WP-08.2 |
| Result fail인데 payload/`SUCCEEDED`가 보임 | Both-gate 실패 | WP-08.2/3 |
| Status가 파일 존재/listing/newest에 따라 바뀜 | Exact retrieval 실패 | WP-08.2/3/6 |
| Crash 뒤 pointer가 partial bytes를 가리킴 | Immutable-before-pointer 실패 | WP-08.3 |
| 다른 workspace에서 result digest가 다름 | Reproducibility 실패 | WP-08.5/6 |
| Cancel 뒤 `MAX_STEPS_REACHED` | Termination taxonomy 실패 | WP-08.5 |
| Separate CLI process cancel이 shared authority 없이 동작한다고 주장 | Local scope 과장 | WP-08.4/5 |
| Stub Phase 07로 E2E evidence 생성 | Evidence classification 실패 | WP-08.6/7 |
| Phase 09가 `Path`/S3 key를 application에 요구 | Handoff dependency 역전 | Phase 08/09 consumer review |
| Phase 10이 single-worker result를 all-worker complete로 사용 | Completeness authority 실패 | Phase 10 |
| Local pass가 Phase 13/14B를 열었다고 표시 | Gate/authority overclaim | Scheduler/Phase 14 |

### 16.6 Local/legacy rollback

```text
rollback point:
  accepted legacy source/artifact fingerprint

new isolated unit:
  explicit apps/cli executable artifact + explicit workspace

rollback action:
  stop/disable new local entrypoint
  preserve published/orphan distinction
  keep legacy source/artifact unchanged
  record fault, refs and incompatibility

not authorized:
  GCP/AWS deploy/update/delete
  production traffic switch
  legacy result rewrite
  destructive evidence cleanup
```

## 17. Source → requirement → WP → test/evidence 추적성

| Requirement | Source | WP | Exact test/evidence |
|---|---|---|---|
| `REQ-P08-DAG` application은 provider-neutral이고 `apps/cli`가 composition root | Current Architecture §6.4/§6.6/§7, Integrated §3 | 08.0~1, 08.4, 08.7 | `Phase08ApplicationArchitectureTest.*`; CLI launcher smoke; `E-P08-PORT` |
| `REQ-P08-FLOW` canonicalization→solve→both-gate→publication 한 path | Master §4.1~4.2, Current Architecture §4.2 (Main flow) | 08.2, 08.6 | `LocalCliEndToEndTest.explicitFixtureRunsCanonicalizeSolveBothGatePublishAndRetrieve()`; local E2E |
| `REQ-P08-P07` Phase 07 top-level 2 + nested rejection 2 lossless | Actual Phase 07 §7.6~8.5, review F-P08-001 | 08.1~2 | `ApplicationPortSignatureTest.phase07TopLevelOutputAndNestedRejectionAreExhaustive()`; `Phase07ApplicationIntegrationTest.*`; port/E2E |
| `REQ-P08-NO-FAIL-PAYLOAD` non-success normal payload 0 | Master §14.1, Phase 07 contract | 08.2 | Rejection/incomplete/exception tests; `E-P08-PORT` |
| `REQ-P08-IDENTITY` stable submission/solve/run/attempt/version | Current Architecture §11.3 (Logical identity와 idempotency)/§16.3 (Provenance)/§17.2 (Retry/failure matrix) | 08.1, 08.5 | `SubmissionIdempotencyTest.*`, `WorkerRetryIdentityTest.*`; idempotency evidence |
| `REQ-P08-AUTHZ` explicit non-ambient tenant access | Current Architecture §17.3 (Security and data boundary), Integrated §20, review F-P08-003 | Decision, 08.1, 08.3 | `tenantScopedAccessBindingIsExplicitAndNonAmbient()`, security tests; approval receipt |
| `REQ-P08-FAILURE` lossless storage operation result | Current Architecture §12.1 (Port catalog)/§17.2 (Retry/failure matrix), Integrated §21, review F-P08-004 | Decision, 08.1, 08.3 | `storageOperationFailureCarrierIsExhaustiveAndLossless()`; fault matrix |
| `REQ-P08-ARTIFACT` immutable put/read/digest | Current Architecture §12.1 (Port catalog)/§16.2 (Artifact contract), Integrated §12~13 | 08.1, 08.3 | `ArtifactStoreContract.*`; port/idempotency evidence |
| `REQ-P08-CAS` state/publish precondition 분리 | Integrated §13.3~13.8, review F-P08-005 | Decision, 08.1, 08.3 | State/publisher contract와 race tests; idempotency evidence |
| `REQ-P08-LOCAL` explicit filesystem/same-process reference | Current Architecture §10.1~§10.3 (Local/single-run runtime), Integrated §12.4 | 08.3~6 | Filesystem/CLI/E2E suites; local E2E |
| `REQ-P08-NO-DEFAULT` 공식값/ambient fallback 0 | `Q-BENCH-02`, Phase 08 §8.1~8.2 | 08.1, 08.4 | Config omission/fingerprint tests; port/local evidence |
| `REQ-P08-REPRO` workspace/clock/order 비의존 | Master §13.2, Current Architecture §10.1~§10.3 (Local/single-run runtime)/§17.1 (Structured observability) | 08.5~6 | Fresh workspace/clock/event-order tests; local E2E |
| `REQ-P08-CANCEL` intent/actual/last-safe 분리 | Master §13.1, Current Architecture §12.1 (Port catalog)/§17.2 (Retry/failure matrix) | 08.5 | `CancellationSemanticsTest.*`; idempotency evidence |
| `REQ-P08-DEADLINE` algorithm/watchdog/platform 분리 | Master §13.1, Current Architecture §10.3 (Single-run과 local multi-worker)/§11.1 (Logical state machine)/§17.2 (Retry/failure matrix) | 08.5 | `DeadlineSemanticsTest.*`; idempotency evidence |
| `REQ-P08-RETRY` same logical worker, attempt-only 변화 | Current Architecture §11.3 (Logical identity와 idempotency)/§17.2 (Retry/failure matrix) | 08.5 | `WorkerRetryIdentityTest.*`; idempotency evidence |
| `REQ-P08-SECURITY` tenant/path/symlink/limit/redaction | Current Architecture §17.3 (Security and data boundary), Integrated §20 | 08.3~5 | Workspace/input/telemetry security tests; local E2E |
| `REQ-P08-OBS` telemetry는 semantic input이 아님 | Current Architecture §17.1 (Structured observability), Integrated §19.3 | 08.5 | `TelemetryContractTest.*`; local E2E |
| `REQ-P08-MIGRATION` legacy characterization/isolation/rollback | Master §16.2, repository inventory | 08.0, 08.7 | Legacy source/migration tests; all bundles |
| `REQ-P08-P09` accepted storage consumer handoff | Integrated §13, Actual Phase 09 §15.1 | 08.1, 08.3, 08.7 | Signature/port contract + Phase 09 consumer receipt |
| `REQ-P08-P10` execution seam only, coordinator 미선구현 | Current Architecture §11.1~§11.3 (Distributed multi-round runtime)/§12.1~§12.2 (Provider-neutral logical ports), Plan Phase 10 | 08.1, 08.5, 08.7 | Dispatcher/cancel/retry/state tests + Phase 10 receipt |
| `REQ-P08-Q-INFRA` AWS selected but not pulled forward | `Q-INFRA-01 RESOLVED` | 08.0~7 | Default local dependency/provider ref 0; port evidence |
| `REQ-P08-Q-BENCH` official numerical default 0 | `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED` | 전 WP | Config/source inspection; open snapshot |
| `REQ-P08-C17` Phase 13 dependency/option 0 | Master `C-17 GATED`, README DAG | 전 WP | Route-selection dependency 0; port evidence |
| `REQ-P08-Q-VAR` deferred variant 미활성 | `Q-VAR-01 DEFERRED` | 전 WP | Config/dependency manifest inspection |
| `REQ-P08-EVIDENCE` one-way immutable evidence DAG | Master Plan §9~§11 | 08.7 | Manifest M → review R → receipt M+R |
| `REQ-P08-AUTHORITY` Phase 08 pass는 benchmark/production 아님 | README §6.1, Plan §11/§14 | 08.7, handoff | Scheduler status/receipt and no-overclaim review |

새 field, port, state, failure, CLI surface, provider requirement 또는 public schema 요구가 나오면 source, owner, WP, exact test와 evidence를 이 표에 연결한다. 연결할 권위가 없으면 `PROPOSED/OPEN`으로 남기고 implementation default로 닫지 않는다.
