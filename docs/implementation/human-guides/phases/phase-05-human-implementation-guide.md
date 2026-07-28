# Phase 05 사람용 구현 가이드 — Pair insertion과 initial portfolio

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
human_guide_correction_status: ROUND_01_APPLIED_PENDING_INDEPENDENT_RECHECK
guide_scope: Phase 05 only
canonical_phase_count: 15
phase: "05"
phase_name: pair-insertion-initial-portfolio
canonical_phase_document: docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
canonical_phase_document_version: "1.3 + ALNS_FIRST_1.0 overlay"
canonical_phase_document_status: REVIEWED_CHANGES_REQUIRED
canonical_phase_review: docs/implementation/reviews/phase-05-review.md
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: BLOCKED
phase_acceptance_status_observed: NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status: BLOCKED
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_branch: codex-implementation
inventory_observed_date: 2026-07-29
head_baseline_tree: a63fa3b93ca206298ac6d467e02e1f3503cee30e
live_inventory_snapshot_at: "2026-07-29T02:17:56+09:00"
live_inventory_status_sha256: 93ee89d679029252067311d3641dd41e4c33cb9a75780a7efbfccd21c2238a48
live_implementation_manifest_sha256: f8a7fd90f71bcc4459f1d3ea17de62ba7a703459bd3bcea932db9555dd1ff25c
live_inventory_recheck_at: "2026-07-29T02:23:50+09:00"
live_progress_recheck_git_blob: f875edbdeb79fb18710421b56e126462a4d6d38c
live_inventory_final_recheck_at: "2026-07-29T02:30:21+09:00"
live_project_pom_count: 13
live_rpdptw_main_java_count: 23
live_phase00_build_test_class_count: 9
live_legacy_test_class_count: 5
live_phase05_named_type_or_test_count: 0
live_inventory_drift_detected_during_correction: true
scheduler_implementation_task_id: TBD_NOT_SUPPLIED
guide_delegation_source_thread: 019fa957-eadc-74d1-ae44-6d2957482856
prerequisite_phases:
  - "00"
  - "01"
  - "02"
  - "03"
  - "04"
direct_consumer_phase: "06"
verification_consumer_phase: "07"
source_fingerprint_scheme: pinned_head_git_blobs_plus_live_worktree_content_and_status_snapshot
source_sections_and_fingerprints:
  docs/master-design.md: "§4.1~4.7, §6, §9~13, §15.4~15.6, §16~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/2026-07-26-domain-design.md: "§2~3, §7~11, §17.5~18 | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "§1.2~1.4, §2, §5.6, §6 | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/architecture-domain-implementation-design.md: "§2~3, §7~10, §19~25 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~4 and exact Q-* rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/implementation/README.md: "§1, §3~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§2~4, Phase 03~06, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "§2, §5~9 | 250aa90ae568a6b32ec905fa5ee456d430ff72cf"
  docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md: "§1~16 | 0ea8046142a9e53cc1b9cedb198a1bdae550378b"
  docs/implementation/reviews/phase-05-review.md: "§1~8 | 88ecf56c57556a0ae8d3578ec158561602222c14"
expected_reader:
  - Java record, interface, sealed hierarchy와 Maven 기본을 안다
  - CVRPTW route, time window와 capacity propagation을 구현해 보았다
  - RPDPTW request identity, stable state, lifecycle과 repository target architecture는 처음 접한다
owner_roles:
  implementation: RPDPTW Pair/Insertion/Portfolio owner
  upstream_kernel: Phase 03 Core/Evaluation owner
  upstream_binding: Phase 04 Capability/Profile owner
  downstream_search: Phase 06 COW ALNS owner
  architecture: Phase 00 Architecture owner
  review: independent Phase 05 reviewer
  acceptance: total scheduler
planned_evidence:
  - E-P05-PAIR
  - E-P05-INSERTION
  - E-P05-PORTFOLIO
```

> 이 문서는 Java/CVRPTW 경험자가 Phase 05를 이해하고, entry gate가 열린 뒤 사람의 판단으로 구현하며, 증거를 남기도록 돕는 교육형 작업 지시서다. 현재 live checkout에는 미커밋·미승인 Phase 00 reactor/wrapper와 목표 module/package skeleton이 있지만 Phase 05 production type, Phase 05 test 또는 evidence는 없다. Skeleton 존재는 Phase 00 acceptance가 아니다. 아래 Java 이름과 signature는 별도 표시가 없는 한 **제안 후보(`PROPOSED`)**이며, 존재하는 API나 승인된 public contract가 아니다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 05는 모든 request가 “한 concrete vehicle route에 완전한 pair로 배정됨” 또는 “request bank에 정확히 한 번 남음” 중 하나인 immutable stable solution을 만들고, 한 pair의 모든 합법 삽입 위치를 부작용 없이 평가하여, 네 성장 정책과 두 vehicle 순서를 조합한 최대 8개의 독립 초기 후보를 Phase 06에 넘기는 단계다.

여기서 `exact insertion`은 MIP나 exact optimizer를 부른다는 뜻이 아니다. 선언한 위치를 실제 Phase 03 propagation과 Phase 04 bound policy로 빠짐없이 재평가한다는 뜻이다.

## 2. 큰 그림과 이 단계가 필요한 이유

### 2.1 CVRPTW의 한 위치 삽입만으로는 부족하다

CVRPTW에서는 흔히 customer 한 명을 route의 위치 하나에 넣는다. RPDPTW의 원자 단위는 `Request`이고, real pickup-delivery request는 두 개의 service visit을 소유한다.

```text
CVRPTW
  route + customer + one position

RPDPTW
  route + Request + pickup position + delivery position
  with pickup position < delivery position
```

두 visit은 같은 concrete vehicle route에 정확히 한 번씩 있어야 한다. Pickup에서 load가 증가하고 delivery에서 감소하므로 같은 방문 집합도 순서에 따라 capacity와 time feasibility가 달라진다. Pickup만 넣은 중간 경로나 서로 다른 차량에 나눈 pair를 stable candidate로 노출하면 이후 ALNS, verifier와 result가 모두 잘못된 자료를 소비한다.

Phase 05가 필요한 이유는 다음 세 책임을 한 경계에서 확실히 하기 위해서다.

1. Search가 소비할 stable route/bank partition을 정의한다.
2. Pair option을 만들되 feasibility 공식은 Phase 03/04에 위임한다.
3. Phase 06이 한 candidate에 과적합되지 않도록 서로 독립적인 초기 후보군을 만든다.

### 2.2 Canonical 15 Phase 중 위치

이 저장소의 canonical 구현 단계는 **Phase 00~14, 총 15개**다. Phase 파일이 존재한다는 사실과 구현이 acceptance되었다는 사실은 다르다. 현재 accepted Phase는 0개다.

| Phase | 주제 | Producer/consumer 관점에서 Phase 05와의 관계 |
|---:|---|---|
| 00 | Build와 architecture 뼈대 | `rpdptw-core`, `rpdptw-solver`, test-fixtures, architecture-rules와 wrapper/reactor를 제공해야 한다 |
| 01 | Canonical input와 normalization | Request, vehicle, numeric/time/service/compatibility 의미를 동결한다 |
| 02 | Prepared travel과 immutable problem | `ProblemInstance`, complete directed `PreparedTravel`, dense identity를 생산한다 |
| 03 | Route propagation과 evaluation kernel | Cache-free route facts, typed feasible/infeasible/invalid와 comparator 기반을 생산한다 |
| 04 | Capability와 customer profile | Exact `BoundProfile`/`SolvePlan`, constraint/metric/objective dependency closure를 생산한다 |
| **05** | **Pair insertion과 initial portfolio** | **이 가이드가 소유하는 stable construction과 최대 8개 seed 후보** |
| 06 | COW ALNS와 reproducibility | Phase 05 seed를 screen하고 destroy/repair/acceptance/adaptive search를 소유한다 |
| 07 | Independent verification과 final result | Search cache를 신뢰하지 않고 Phase 05/06 candidate 구조와 계산을 재검증한다 |
| 08 | Application port와 local runtime | 검증된 ALNS-only local end-to-end를 조립한다 |
| 09 | DB 없는 object storage | Immutable artifact와 CAS 상태를 저장한다. Phase 05 core 책임은 아니다 |
| 10 | Provider-neutral coordinator | Declared worker completeness와 round를 관리한다. Phase 05 policy를 재구현하지 않는다 |
| 11 | AWS reference distribution | S3/Step Functions/Lambda adapter다. Phase 05 core에 AWS SDK를 넣지 않는다 |
| 12 | Provider substitution | 승인된 provider 축의 조건부 대체다 |
| 13 | Optional hybrid route selection | **Phase 14A acceptance 뒤에만** 열 수 있는 `C-17` gated branch이며 Phase 05 exit 조건이 아니다 |
| 14 | 14A benchmark qualification / 14B official cutover | 14A ALNS evidence와 14B production authority는 서로 다른 gate다 |

현재 ALNS-first critical path는 다음과 같다.

```text
00 → 01 → 02 → 03 → 04 → [05] → 06 → 07 → 08 → 14A
```

Phase 13은 이 경로의 선행조건이 아니다. Phase 14A의 유효한 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`와 `C-17`의 별도 scope/backend/법무/보안/운영/비용 승인이 모두 있어야 검토할 수 있다. Phase 14B의 production authority가 없다는 이유로 Phase 05 또는 14A를 막아서는 안 되고, 14A receipt가 Phase 13이나 14B를 자동 승인하지도 않는다.

### 2.3 Producer와 consumer 계약

```text
Phase 01 normalized meaning
        ↓
Phase 02 ProblemInstance + PreparedTravel
        ↓ exact identity equality
Phase 03 route propagation/evaluation declarations
        ↓
Phase 04 BoundProfile + SolvePlan + comparator
        ↓
Phase 05 stable all-bank snapshot
        → pure pair option enumeration/evaluation
        → functional construction transition
        → 4 × 2 independent initial portfolio
        ↓ immutable handoff
Phase 06 screen + COW ALNS
        ↓
Phase 07 independent verification
```

| 경계 | Producer가 보장해야 할 것 | Consumer가 해서는 안 되는 것 |
|---|---|---|
| 02 → 05 | Immutable problem/travel, complete directed lookup, concrete vehicle와 terminal identity | Raw `D/U`, 좌표, speed, reverse arc 또는 provider를 다시 읽기 |
| 03 → 05 | Pure route propagation/evaluation, typed rejection/invalid, exact fingerprints | Capacity/time/resource 공식을 insertion package에 복제하기 |
| 04 → 05 | Exact profile/preset/config, bound hard/metric/objective/comparator closure | Customer-name 분기, unknown/latest fallback, 임의 objective scalar 만들기 |
| 05 → 06 | Immutable available seed solutions, unavailable member record, pure evaluator, exact lineage/evidence | Seed를 직접 mutate하거나 Phase 05 transition을 destroy editor로 cast하기 |
| 05/06 → 07 | Route/bank source of truth와 authority identity | Search cache, feasibility flag 또는 summary를 verifier authority로 쓰기 |

### 2.4 완료 모습

Phase 05의 정상 출력은 “가장 좋은 해 하나”가 아니다.

```text
SeedPortfolio
  ordered 4×2 PortfolioMember
    Available(ConstructionCandidate)
      immutable SearchSnapshot
      construction lineage
    or
    Unavailable(CLOCK_COORDINATE_MISSING)
```

각 available candidate는 품질이 낮거나 bank가 비어 있지 않아도 stable할 수 있다. Phase 05는 screen/champion을 고르지 않는다. 그것은 Phase 06의 책임이다.

## 3. Source authority, 읽기 순서와 fingerprint

### 3.1 충돌 해소 순서

Phase 05 판단에는 “권위”와 “관찰 시점”을 섞지 않는다.

```text
사용자 고정 지시와 이 문서 세트의 고정 입력
  → Canonical Master
  → 질문 등록부의 exact Q-* 상태
  → 2026-07-26 Final Domain 의미
  → 2026-07-26 Final Architecture 배치
  → Integrated 15-Phase design

현재 top-level Domain/Architecture REVIEW 지도
  → 최신 전체-product cross-check와 충돌 탐지
  → 날짜가 더 늦다는 이유만으로 고정 입력을 자동 대체하지 않음

implementation README/plan/Phase/review의 pinned HEAD baseline
  → 실행 계약과 재현 가능한 과거 기준선

timestamp가 있는 live worktree snapshot
  → 현재 파일·POM·status 관찰
  → acceptance 또는 설계 authority로 승격하지 않음
```

[현재 Domain 지도](../../../domain-design.md)와 [현재 Architecture 지도](../../../architecture-design.md)는 각각 `REVIEW` 상태의 최신 의미·배치 cross-check다. 사용자 고정 [2026-07-26 Domain](../../../2026-07-26-domain-design.md)과 [Architecture](../../../2026-07-26-architecture-design.md), implementation baseline과 다른 owner/package/API를 제시하면 더 최신 timestamp나 live file 존재를 근거로 하나를 선택하지 않는다. 충돌 path/heading, 두 후보, downstream 영향과 승인 owner를 기록하고 마지막 안전 지점에서 멈춘다.

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md), `master-design-sessions`, `arranged`, `orgin`, legacy GCP 자료는 current contract authority가 아니다. 특히 dated Final Domain과 Final Architecture 일부의 `Q-INFRA-01 DEFERRED`, 상태 `25/1/2`는 최신 등록부보다 오래됐다. 이 알려진 상태 충돌만 Canonical Master와 질문 등록부의 `Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`로 해소한다. 그 사실을 다른 미승인 API/default를 만드는 포괄 권한으로 사용하지 않는다.

### 3.2 구현자가 실제로 읽을 순서

링크를 위에서 아래로 읽고, 각 단계의 “읽은 흔적”에 source path, heading, Git blob과 자신이 이해한 한 문장을 남긴다.

1. [구현 문서 지도 §1·§3·§6](../../README.md#1-읽기-순서): authority와 ALNS-first DAG를 확인한다.
2. [Canonical Master §4](../../../master-design.md#4-구현-아키텍처와-책임-경계): 전체 pipeline, artifact lifecycle과 responsibility를 읽는다.
3. [Canonical Master §6](../../../master-design.md#6-핵심-불변조건과-atomic-mutation): route-bank XOR와 atomic mutation을 읽는다.
4. [Canonical Master §11.1~11.2](../../../master-design.md#11-initial-portfolio-alns와-route-selection-pipeline): 공통 pair evaluator와 4×2 portfolio 역할을 읽는다.
5. [Canonical Master §12](../../../master-design.md#12-candidate-state-cache와-rollback): cache가 authority가 아닌 이유와 COW 경계를 읽는다.
6. [Final Domain §2](../../../2026-07-26-domain-design.md#2-cvrptw-개발자를-위한-rpdptw-입문): request, service pattern과 pair invariant를 읽는다.
7. [Final Domain §8](../../../2026-07-26-domain-design.md#8-stable-solution-request-bank와-cow): stable state와 lifecycle을 읽는다.
8. [Final Domain §11](../../../2026-07-26-domain-design.md#11-alns-state와-operator): insertion option과 Phase 06 ownership을 대조한다.
9. [Final Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계): module/package/DAG와 금지 dependency를 읽는다.
10. [Integrated design §9](../../../architecture-domain-implementation-design.md#9-phase-5--stable-solution-insertion과-initial-portfolio): 15-Phase 배치의 Phase 5 의미를 읽는다.
11. [Master Realization Plan Phase 05](../../master-realization-plan.md#phase-05--pickup-delivery-pair-삽입과-초기-후보군): entry/input/output/exit/evidence를 확인한다.
12. [Execution Progress §5](../../execution-progress-and-results.md#5-구현-task-registry)와 [§8](../../execution-progress-and-results.md#8-현재-blockers-open-gates와-남은-이슈): 현재 authoritative status와 cross-Phase blocker를 확인한다.
13. [Canonical Phase 05](../../phases/phase-05-pair-insertion-initial-portfolio.md)의 전체, 특히 §3~§12와 §14~§16을 읽는다.
14. [Phase 05 review](../../reviews/phase-05-review.md)의 §1, §4, §6~§8을 읽고 residual blocker 세 개를 자신의 작업표에 복사한다.
15. [Phase 03](../../phases/phase-03-route-propagation-evaluation-kernel.md), [Phase 04](../../phases/phase-04-capabilities-customer-profiles.md), [Phase 06](../../phases/phase-06-cow-alns-reproducibility.md), [Phase 07](../../phases/phase-07-independent-verification-final-result.md)의 handoff 절을 대조한다.

### 3.3 HEAD baseline과 live drift의 검증 가능한 fingerprint

첫 번째 층은 `inventory_observed_at_commit`에서 `git rev-parse HEAD:<path>`로 얻은 재현 가능한 Git blob이다. Source가 바뀌면 hash만 갈아 끼우지 말고 바뀐 heading의 의미, contract, test와 gate 영향을 다시 검토한다.

| Source | 직접 소비한 heading/section | Git blob |
|---|---|---|
| [Canonical Master](../../../master-design.md) | §4.1~4.7, §6, §9~13, §15.4~15.6, §16~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Final Domain](../../../2026-07-26-domain-design.md) | §2~3, §7~11, §17.5~18 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Final Architecture](../../../2026-07-26-architecture-design.md) | §1.2~1.4, §2, §5.6, §6 | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | §2~3, §7~10, §19~25 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | §1~4, `Q-REQ-*`, `Q-OBJ-*`, `Q-ALG-*`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README](../../README.md) | §1, §3~7 | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | §2~4, Phase 03~06, §8~15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress](../../execution-progress-and-results.md) | §2, §5~9 | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` |
| [Canonical Phase 05](../../phases/phase-05-pair-insertion-initial-portfolio.md) | §1~16 | `0ea8046142a9e53cc1b9cedb198a1bdae550378b` |
| [Phase 05 review](../../reviews/phase-05-review.md) | §1~8 | `88ecf56c57556a0ae8d3578ec158561602222c14` |
| [Phase 03](../../phases/phase-03-route-propagation-evaluation-kernel.md) | §7, §9, §13~15 | `74098f7e15cf75bcc443ae009cc475a9b60d63a3` |
| [Phase 04](../../phases/phase-04-capabilities-customer-profiles.md) | §3~4, §7~8, §12~15 | `e0a68fd442a7db383e4a650e4a561234c323b1fe` |
| [Phase 06](../../phases/phase-06-cow-alns-reproducibility.md) | §2~4, §7~9, §14~17 | `984b6978981fffa662bcf4cf5b4f8a14c2287c09` |
| [Phase 07](../../phases/phase-07-independent-verification-final-result.md) | §4, §7~10, §15~16 | `1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115` |

현재 top-level cross-check도 별도 역할로 pin한다.

| Current map | 역할 | HEAD Git blob | 2026-07-29 live SHA-256 |
|---|---|---|---|
| [Current Domain](../../../domain-design.md) | 최신 `REVIEW` 의미/owner 지도, 고정 dated input 자동 대체 금지 | `ace117c380466b733994a1fbb2a95d31e41b3959` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` |
| [Current Architecture](../../../architecture-design.md) | 최신 `REVIEW` module/package 지도, accepted reactor 주장 금지 | `81495ff448d0e618ab3563e8ff80614fb1028acf` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` |

두 번째 층은 `live_inventory_snapshot_at`의 공유 working-tree 관찰이다.

| Live fingerprint | 값 | 판정 |
|---|---|---|
| HEAD commit/tree | `7cc890ee1d0805df5ae14b633127fade4f978639` / `a63fa3b93ca206298ac6d467e02e1f3503cee30e` | 재현 가능한 tracked baseline |
| `git status --porcelain=v1 -z --untracked-files=all` SHA-256 | `93ee89d679029252067311d3641dd41e4c33cb9a75780a7efbfccd21c2238a48` | Path/state snapshot, acceptance 아님 |
| `git diff --binary HEAD` SHA-256 | `500c4ef4e09165f685d0b446b97b4a5352e863dd1c08c6de82b89a1aab212836` | Tracked dirty bytes |
| Sorted untracked `path + SHA-256` manifest | `9f65029bf10aca7dc785ec2dcb2bbee7a746ba26829627afea1eb1a20d395d69` | Untracked content snapshot |
| Root POM / execution progress live Git blob | `1dc675ba17b7f2202f34a22131f152cc2868b075` / `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d` | HEAD baseline과 다른 uncommitted bytes |
| Non-`target` Phase 00 implementation manifest SHA-256 | `f8a7fd90f71bcc4459f1d3ea17de62ba7a703459bd3bcea932db9555dd1ff25c` | Wrapper/POM/build/rpdptw/legacy candidate, unapproved |

Correction 중 `2026-07-29T02:23:50+09:00` 재확인에서 progress blob은
`f875edbdeb79fb18710421b56e126462a4d6d38c`로 바뀌고 scheduler state는
`FIX_01_IMPLEMENTED_REVIEW_02_PENDING`이 됐다. 같은 non-`target` implementation
manifest는 `f8a7fd...25c`로 유지됐다. 이 drift는 Fix 01 제출을 Phase 00 acceptance로
승격하지 않으며, 오히려 timestamp 없는 “live current”를 command/evidence authority로
사용하지 말아야 한다는 fail-closed 사례다.

마지막 `2026-07-29T02:30:21+09:00` inventory recheck는 project POM 13개,
`rpdptw` main Java 23개, Phase 00 build test class 9개와 legacy test class 5개를
관찰했다. 초기 9개와 최종 전체 14개는 범위가 다른 수치이며 증가로 추론하지 않는다.
Phase 05 이름을 가진 production type/test는 계속 0개다. 이 최종 수치도 accepted
receipt가 아니라 공유 checkout drift를 재검출하기 위한 관찰값이다.

재확인 명령은 HEAD와 live를 각각 남긴다.

```bash
git rev-parse HEAD
git rev-parse 'HEAD^{tree}'
git rev-parse HEAD:docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
git rev-parse HEAD:docs/implementation/reviews/phase-05-review.md

git status --short --untracked-files=all -- \
  docs/master-design.md docs/domain-design.md docs/architecture-design.md \
  docs/2026-07-26-domain-design.md docs/2026-07-26-architecture-design.md \
  docs/architecture-domain-implementation-design.md \
  docs/master-design-open-questions.md docs/implementation pom.xml mvnw .mvn \
  rpdptw build legacy
git diff -- \
  docs/master-design.md docs/domain-design.md docs/architecture-design.md \
  docs/2026-07-26-domain-design.md docs/2026-07-26-architecture-design.md \
  docs/architecture-domain-implementation-design.md \
  docs/master-design-open-questions.md docs/implementation pom.xml
git hash-object pom.xml docs/implementation/execution-progress-and-results.md \
  rpdptw/pom.xml rpdptw/core/pom.xml rpdptw/solver/pom.xml \
  build/pom.xml build/test-fixtures/pom.xml build/architecture-rules/pom.xml
```

실제 구현 시작 시에는 위 live snapshot을 그대로 “현재”라고 재사용하지 않는다. 동일 범위의 timestamp, status digest, relevant content hash와 source/test count를 다시 봉인하고 Phase 00 accepted receipt가 가리키는 source/build manifest와 대조한다. 다음 중 하나면 fail-closed한다.

1. HEAD/source heading hash가 바뀌었는데 requirement 영향 review가 없다.
2. Live POM/module/test/report bytes가 accepted manifest와 다르다.
3. Scheduler status와 acceptance receipt가 불일치한다.
4. Snapshot 뒤 source/POM/test가 다시 변했거나 hash를 계산하지 않은 untracked file이 소비된다.

중지 기록에는 바뀐 path/heading, before/after hash, 요구·상태 변화, Java contract/test/evidence/handoff 영향, owner와 restart 승인을 남긴다. Skeleton이 존재하더라도 acceptance receipt가 없으면 entry verdict는 계속 `BLOCKED`다.

Whole-file hash를 인접 Phase metadata에 서로 복제해 순환 drift를 만들지 않는다. Accepted implementation artifact와 evidence digest는 실제 handoff manifest에서 별도로 pin한다.

## 4. RPDPTW primer와 용어집

### 4.1 Identity를 분리한다

| 용어 | 이 프로젝트의 의미 | 흔한 오해 |
|---|---|---|
| `Order` | 외부 business 입력 | Solver의 atomic identity와 같다고 보기 |
| `Request` | Pickup/delivery 의미를 소유한 원자 운송 업무 | Pickup node 하나로 보기 |
| `SolverNodeId` | Terminal/pickup/delivery service 정의 | Physical location과 합치기 |
| `PhysicalLocationId` | Directed travel table endpoint | 같은 장소의 두 service node를 합치기 |
| `Visit` | Route 안에서 node가 나타난 한 occurrence | Node 정의 자체로 보기 |
| `VehicleId` | 입력의 concrete vehicle | Vehicle type/count/sentinel로 대체하기 |
| `RoutePlan` | Concrete vehicle, terminal policy와 ordered service sequence | Mutable search draft로 보기 |
| `SearchRequestBank` | 현재 어느 route에도 없는 request ID membership | DB, final `UNASSIGNED`, last failure 저장소로 보기 |
| `SearchSnapshot` | Immutable stable routes/bank/evaluation/fingerprint | ALNS의 mutable current object로 보기 |

같은 physical location에서 pickup과 delivery를 해도 service node는 두 개다. Travel self arc가 `0/0`이어도 두 service와 두 load delta의 의미를 합치면 안 된다.

### 4.2 두 service pattern

**Real pickup-delivery**

- Pickup과 delivery가 모두 physical service visit이다.
- Pickup에서 load가 증가하고 delivery에서 감소한다.
- 같은 concrete vehicle route, exactly once, pickup-before-delivery가 필수다.

**Delivery-only**

- 화물은 route 출발 전에 이미 적재되어 있다.
- Logical pickup은 request 소유권에는 참여하지만 physical visit이 아니다.
- Delivery-only demand 합은 route initial load다.
- Delivery service에서 load가 감소한다.

```text
initial load: delivery-only 4
depot → real pickup(+3) → delivery-only delivery(-4) → real delivery(-3)
load:              7                            3                    0
```

Delivery-only logical pickup을 가짜 depot visit으로 만들면 travel, service time, stop과 resource가 오염된다.

### 4.3 Stable의 의미

여기서 stable은 “더 개선할 수 없는 고품질 해”가 아니다. 자료 구조가 완전하고 일관되어 다음 단계의 안전한 입력이 될 수 있다는 뜻이다.

모든 request는 다음 두 상태 중 정확히 하나다.

```text
ASSIGNED_IN_SEARCH
  complete required service
  same concrete vehicle route
  exactly once
  precedence satisfied
  not in bank

UNASSIGNED_IN_SEARCH
  no physical service on any route
  in SearchRequestBank exactly once
```

Partial, duplicate, split, reverse, route+bank overlap와 양쪽 누락은 정상 infeasible이나 낮은 quality가 아니라 implementation defect다.

### 4.4 Lifecycle vocabulary

| Artifact/state | Mutable인가 | Stable/authority인가 | Owner |
|---|---:|---:|---|
| `ProblemInstance`/`PreparedTravel` | 아니오 | 예 | Phase 02 |
| `BoundProfile`/`SolvePlan` | 아니오 | 예 | Phase 04 |
| `PairInsertionRequest` | 아니오 | 한 평가 호출의 입력 | Phase 05 core contract 후보 |
| `FeasibleInsertionOption` | 아니오 | 선택 전 option이며 committed solution은 아님 | Phase 05 |
| Construction builder scratch | 예, 내부 한정 | 아니오 | Phase 05 |
| `SearchSnapshot` | 아니오 | 구조/full evaluation 통과 뒤 예 | Phase 05/06 shared contract |
| `ConstructionCandidate` | 아니오 | Cache-free validation 뒤 seed로 사용 가능 | Phase 05 |
| `SeedPortfolio` | 아니오 | Phase 06의 immutable seed set | Phase 05 |
| `TrialDraft`/`CompletedTrial` | Step-local | stable snapshot이 아님 | Phase 06 |
| `VerifiedSolution` | 아니오 | Candidate verifier `PASS` 뒤 예 | Phase 07 |

### 4.5 반드시 지킬 invariant

1. Real pair의 pickup/delivery는 한 request 단위로 생성·적용한다.
2. 두 physical visit은 같은 concrete vehicle route에 정확히 한 번씩 있고 pickup index가 더 작다.
3. Assigned request와 bank membership은 XOR다.
4. Delivery-only logical pickup은 node, arc, stop, service position을 만들지 않는다.
5. 한 concrete vehicle은 stable snapshot에서 route 최대 하나만 소유한다.
6. `NEW_ROUTE`는 입력 fleet의 unused `VehicleId`만 소비한다.
7. Terminal/oneway/single-roundtrip 의미와 내부 depot revisit 금지를 유지한다.
8. Enumeration/materialization/evaluation은 base route/snapshot을 바꾸지 않는다.
9. Compatibility, capacity, time/window, travel, stop/resource와 extension hard rule은 Phase 03/04 authority가 판정한다.
10. 정상 `Rejected`와 authority/corruption `Invalid`를 섞지 않는다.
11. 검사하지 않은 위치가 있으면 exhaustive infeasibility를 주장하지 않는다.
12. Business comparator가 먼저이고 완전 동률일 때만 stable identity tie를 쓴다.
13. Candidate/member는 mutable route, bank, cache, trace 또는 builder scratch를 공유하지 않는다.
14. Selected option과 완성 candidate는 cache-free full recomputation과 exact identity가 같아야 한다.
15. Phase 05는 seed 생성에서 멈추고 Phase 06 screen/champion/ALNS와 Phase 07 verification을 실행하지 않는다.

## 5. Entry gate, 현재 판정과 사람 승인

### 5.1 Entry gate 표

문서와 독립 oracle 설계를 학습할 수는 있지만 production 구현과 Phase evidence 착수는 아래 AND gate가 모두 열릴 때까지 `BLOCKED`다.

| Gate | 필요한 artifact/evidence | 2026-07-29 관찰 | 판정 |
|---|---|---|---|
| Phase 00 accepted | Accepted review, `E-P00-ARCH`, target reactor/wrapper/module/package rule | Live에 reactor/wrapper/module skeleton은 있고 Fix 01이 제출됐으나 scheduler는 `FIX_01_IMPLEMENTED_REVIEW_02_PENDING`; acceptance receipt `NOT_PRODUCED` | BLOCKED |
| Phase 01 accepted | `E-P01-NUMERIC`, `E-P01-TIME`, `E-P01-COMPAT`, exact normalized facts | Accepted implementation/evidence 없음 | BLOCKED |
| Phase 02 accepted | `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM`, immutable equality | Implementation/evidence 없음 | BLOCKED |
| Phase 03 accepted | `E-P03-PROPAGATION`, `E-P03-EVALUATION`, `E-P03-COMPARATOR` | Review `CHANGES_REQUIRED`, implementation/evidence 없음 | BLOCKED |
| Phase 04 accepted | `E-P04-BINDING`, `E-P04-ISOLATION`, applicable facet evidence | Review `CHANGES_REQUIRED`, implementation/evidence 없음 | BLOCKED |
| Full-solution evaluator | Problem/travel/profile + ordered routes + bank 전체 평가, artifact reuse/invalidation, failure/fingerprint/comparator API | Route kernel/declaration은 설계됐지만 solution-level owner/API 미승인 | CROSS_PHASE_BLOCKER |
| Comparator/tie | Business equality, solution tie와 insertion-context tie owner, target-key order/version | Objective-first 의미만 확정 | CROSS_PHASE_BLOCKER |
| Portfolio policy detail | Exact traversal, CLOCK axis/orientation/function/version, missing/zero capacity rule | Policy 역할만 확정, API 세부 미승인 | AUTHORITY_BLOCKER |
| Phase 05/06 ownership | Construction insertion transition과 central pair-removal editor의 owner/package | Master plan/Phase 상세 간 소유 표현 충돌 | CROSS_PHASE_BLOCKER |
| Scheduler/owner | 실제 implementation task ID, implementer, independent reviewer | 제공되지 않음 | OWNER_GATE |

Entry에서 최소 다음 equality를 검증한다.

```text
ProblemInstance.preparedTravelFingerprint
  == PreparedTravel.fingerprint

Phase04Binding.problemFingerprint
  == ProblemInstance.fingerprint

Phase04Binding.propagationDeclarationFingerprint
  == Phase03 PropagationDeclaration.fingerprint

Phase04Binding.evaluationPlanFingerprint
  == Phase03 EvaluationPlan.fingerprint

Phase04 profile/config/comparator/SolvePlan fingerprints
  == handoff manifest declarations
```

Mismatch, missing component, unknown/latest fallback, duplicate identity 또는 incomplete travel이면 empty portfolio나 all-bank “성공”으로 낮추지 않고 시작 자체를 거부한다.

### 5.2 네 개의 사람 checkpoint

**Checkpoint A — 선행 Phase acceptance**

- Phase 00~04의 pre-review manifest, independent review report와 post-review acceptance receipt를 확인한다.
- Source file이나 test class 존재만으로 통과시키지 않는다.
- 통과하지 않으면 허용되는 마지막 작업은 문서 읽기, hand fixture 계산과 contract 질문 정리다.

**Checkpoint B — solution evaluation/comparator 승인**

- Route-level evaluation과 solution-level objective/ranking의 입력/출력/identity/failure owner를 문서화한다.
- Ordered routes와 bank가 바뀔 때 route artifact 재사용/무효화 규칙을 승인한다.
- Business equality와 insertion tie의 owner/order/version을 승인한다.
- 승인 없이는 `CandidateEvaluationArtifact`, `CandidateRankingArtifact`, `ConstructionTransition`의 compile freeze를 해제하지 않는다.

**Checkpoint C — portfolio policy 승인**

- `(request,target,position)` traversal의 golden trace를 승인한다.
- CLOCK 0도 축, orientation, angle 함수/version과 reference vector를 승인한다.
- Missing/zero capacity, utilization tie를 typed Phase 04 policy로 승인한다.
- 승인 없이는 hidden input order, `double`/epsilon 또는 fallback을 사용하지 않는다.

**Checkpoint D — Phase 05/06 handoff 승인**

- Phase 05가 소유할 것은 pure insertion과 construction-only functional transition이다.
- Destroy/removal, changed-route COW, `TrialDraft`, acceptance/adaptive/screen은 Phase 06 소유다.
- Master plan의 “central atomic editor/COW primitive” 표현과 detailed Phase의 소유 경계가 충돌하는 부분은 Architecture + Phase 05/06 공동 결정으로 닫는다.
- Exact type 이름, visibility와 evidence handoff를 cross-compile test로 확인한다.

### 5.3 마지막 안전 지점

| 막힌 항목 | 마지막으로 안전하게 할 수 있는 일 | 금지되는 다음 일 | Resume 조건 |
|---|---|---|---|
| 선행 acceptance 없음 | Source inventory, primer, literal oracle, proposed package 검토 | Production source/test/evidence 주장 | Phase 00~04 acceptance receipt |
| Full-solution evaluator 없음 | Immutable route/bank source of truth와 route-level kernel 계약 유지 | Route delta 합이나 placeholder `double` objective 생성 | Joint API + equality/corruption evidence |
| Comparator/tie 없음 | Objective-first, stable semantic field 목록과 non-reversal 법칙 유지 | Target kind byte order나 public API 임의 freeze | Comparator law와 exact order/version 승인 |
| Traversal/CLOCK/utilization 없음 | Policy 역할, CLOCK missing → unavailable, positive-capacity test-only rational fixture | Hidden order/fallback/zero rule | Golden trace, coordinate vectors, typed edge policy |
| Phase 05/06 owner 충돌 | Functional construction insertion만 설계 | Destroy/removal/COW API를 Phase 05에 선점 | Architecture ownership decision |

Stop할 때는 source commit, last green WP, 실패 test/seed, last safe snapshot fingerprint, open owner와 정확한 restart condition을 남긴다.

## 6. 실제 repository inventory: 현재와 목표

### 6.1 재현 명령

```bash
git rev-parse HEAD
git branch --show-current
git status --short --untracked-files=all
find . -path './.git' -prune -o -path './node_modules' -prune -o \
  \( -name 'pom.xml' -o -name '*.java' -o -name '*.kt' \) -print
find . -maxdepth 2 \( -name mvnw -o -path './.mvn' \) -print
```

이 inventory는 두 층으로 읽는다.

1. `inventory_observed_at_commit`의 tracked baseline은 다시 재현할 수 있는 Git 기준선이다.
2. Shared checkout의 live worktree는 다른 작업자가 동시에 바꿀 수 있으므로 timestamp, status/content hash와 함께 별도로 기록하고, acceptance된 artifact로 간주하지 않는다.

`live_inventory_snapshot_at`에는 root parent/aggregator, executable wrapper, stable/build/legacy module POM과 package skeleton이 존재한다. 기존 root Java는 삭제 표시이고 legacy boundary가 untracked로 존재한다. 공식 progress는 Phase 00 implementation 제출 뒤 review 01이 다섯 finding으로 `CHANGES_REQUIRED`를 판정했고, correction 중 Fix 01 제출이 완료되어 review 02가 진행 중이라고 기록한다. 이 가이드는 그 파일을 만들거나 이동·수정·복구하지 않았다. 어느 live 배치도 Phase 00 또는 Phase 05 accepted implementation이 아니며, 실제 구현 task는 시작 시 inventory를 다시 수집하고 accepted receipt와 비교해야 한다.

| 항목 | HEAD baseline | Live snapshot + correction recheck | Phase 05 판정 |
|---|---|---|---|
| Git | `codex-implementation`, commit `7cc890ee...`, tree `a63fa3b...` | Dirty tracked/untracked state; §3.3 status/content fingerprints | Unrelated 변경 보존, snapshot drift 시 fail-closed |
| Scheduler | Progress HEAD blob `250aa90...`의 과거 `0/15` baseline | Initial blob `36afdfd...` → recheck `f875edb...`; Phase 00 `FIX_01_IMPLEMENTED_REVIEW_02_PENDING`, receipt `NOT_PRODUCED` | Live drift fail-closed; Phase 05 entry `BLOCKED` |
| Maven | Root 단일 JAR placeholder POM blob `f8a411e...` | Root reactor POM blob `1dc675b...`, project POM 13개 | `PRESENT_BUT_UNACCEPTED`; accepted coordinate/plugin/report contract 아님 |
| Wrapper | `mvnw`, `.mvn` 없음 | `mvnw` blob `bd8896b...`, wrapper properties `a642114...` | `PRESENT_BUT_UNACCEPTED`; 현재 실행 결과를 Phase 05 evidence로 봉인 금지 |
| Stable modules | 없음 | `rpdptw/{core,solver,verification,application,capabilities,profile-catalog}`와 aggregators | POM/package skeleton만 존재 |
| Build modules | 없음 | `build/test-fixtures`, `build/architecture-rules`; initial/final Phase 00 `*Test.java` 9개 | Phase 05 test가 아니며 accepted classifier/DAG evidence 아님 |
| Legacy | Root main 6개/test 1개 | Root source 삭제 표시, `legacy/gcp-placeholder` untracked; final test class 5개 | 보존 대상 concurrent migration; Phase 05 dependency 금지 |
| Phase 05 source | 없음 | `evaluation/insertion`, `solver/state`, `solver/portfolio`에는 `package-info.java`만 존재 | Phase 05 production type 0 |
| Phase 05 test/evidence | 없음 | Pair/insertion/portfolio test class 0, `E-P05-*` 0 | `ABSENT`; ignored/stale `target/`은 evidence 아님 |
| Canonical docs | Phase 00~14 상세/review actual | 동일 + human-guide concurrent docs | 문서/review와 implementation acceptance 분리 |

Inventory fingerprint 보조값:

| File | 역할 | Git blob |
|---|---|---|
| `HEAD:pom.xml` | Pinned single-project placeholder baseline | `f8a411eadd4a5c01d8dd09fdea462738ca63d65f` |
| `src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java` | Synthetic objective placeholder | `18bfb344faaefe9373e02b007446e7bf15c3443c` |

### 6.2 존재·placeholder·부재를 구분한다

```text
HEAD BASELINE, CURRENT PLACEHOLDER
  HEAD:pom.xml = single JAR
  HEAD:src/**/com.ronext.optimizer.*
  HEAD:AlnsBatchEngineTest

LIVE, PRESENT_BUT_UNACCEPTED
  root/rpdptw/build/legacy reactor POMs
  mvnw + .mvn/wrapper
  rpdptw package-info.java skeleton
  Phase 00 architecture/test-fixture tests
  legacy/gcp-placeholder migration
  ignored target/* reports and JARs

ABSENT, PHASE 05 FUTURE TARGET
  rpdptw/core
    com.ronext.rpdptw.evaluation.insertion production types
  rpdptw/solver
    com.ronext.rpdptw.solver.state production types
    com.ronext.rpdptw.solver.portfolio production types
  build/test-fixtures Phase 05 oracle classes
  build/architecture-rules Phase05ArchitectureTest
  all E-P05 production/test/evidence artifacts
```

### 6.3 Proposed future tree

아래는 Phase 00~04 accepted layout에 맞춰 다시 확인할 future target이다. 이 가이드 작성 자체가 이 tree를 생성하거나 승인하지 않는다.

```text
rpdptw/core/
  src/main/java/com/ronext/rpdptw/evaluation/insertion/
    PairPositionKey.java
    PairPositionEnumeration.java
    InsertionTarget.java
    PairInsertionRequest.java
    PairInsertionEvaluator.java
    PairInsertionEvaluation.java
    InsertionAttempt.java
    FeasibleInsertionOption.java
    EnumerationReceipt.java
    BoundInsertionAuthority.java              # PROPOSED cross-Phase view
    internal/
      ExhaustivePairPositionEnumerator.java
      OrderedBoundedPairPositionEnumerator.java
      ImmutableRouteMaterializer.java
      DefaultPairInsertionEvaluator.java
  src/test/java/com/ronext/rpdptw/evaluation/insertion/
    PairPositionEnumeratorTest.java
    PairInsertionEvaluatorTest.java
    PairInsertionDelegationTest.java
    PairInsertionCorruptionTest.java
    PairInsertionReproducibilityTest.java

rpdptw/solver/
  src/main/java/com/ronext/rpdptw/solver/state/
    SearchRequestBank.java
    EvaluatedRoute.java
    SearchSnapshot.java
    SolutionFingerprint.java
    StableSolutionValidator.java
    ConstructionTransition.java
    internal/FunctionalConstructionTransition.java
  src/main/java/com/ronext/rpdptw/solver/portfolio/
    GrowthPolicy.java
    VehicleOrderPolicy.java
    PortfolioConstructionConfig.java
    PortfolioMember.java
    ConstructionCandidate.java
    ConstructionTrace.java
    SeedPortfolio.java
    InitialPortfolioBuilder.java
    internal/
      ClockGrowthOrder.java
      SequentialGrowthOrder.java
      DeterministicVehicleOrder.java
      DeterministicInitialPortfolioBuilder.java
  src/test/java/com/ronext/rpdptw/solver/
    state/*
    portfolio/*

build/test-fixtures/
  src/test/java/com/ronext/rpdptw/testing/phase05/
    PairInsertionFixtureBuilder.java
    ExhaustivePairPermutationOracle.java
    PairInsertionHandOracleTest.java
    Phase05OracleSensitivityTest.java

build/architecture-rules/
  src/test/java/com/ronext/rpdptw/architecture/
    Phase05ArchitectureTest.java
```

`rpdptw-core` test가 `build/test-fixtures`를 역으로 소비하지 않는다. Independent oracle module이 test scope로 core를 소비한다. 다음 Phase 06 책임 type을 Phase 05 tree에 만들지 않는다.

```text
TrialDraft
CompletedTrial
DestroyProposal / DestroyResult
RepairOperator / RepairResult
AcceptanceState
AdaptiveState
Phase1Champion
AlnsStep
```

### 6.4 현재와 future Maven 명령을 섞지 않는다

현재 live wrapper/reactor가 실행되더라도 Phase 00은 `FIX_01_IMPLEMENTED_REVIEW_02_PENDING / NOT_ACCEPTED`이고 Phase 05 test는 0개다. 따라서 아래 명령은 **`BLOCKED TEMPLATE`**이며 Phase 00 accepted receipt가 exact wrapper, reactor selector, plugin inheritance, test-jar classifier와 report contract를 가리킨 뒤에만 실행·조정할 수 있다.

각 selected-test gate는 같은 새 isolated local repository에서 두 invocation으로 실행한다. 첫 invocation의 `-am install`은 sibling reactor artifact와 test-jar를 공급하는 dependency preparation이다. `-DskipTests`이므로 그 결과나 report는 test evidence가 아니다. 두 번째 invocation은 `-am` 없이 target module만 `clean test`하여 upstream test-name mismatch를 피하고 target report를 fresh하게 만든다.

```bash
PHASE05_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-selected.XXXXXX")"
PHASE05_M2="$PHASE05_RUN_ROOT/repository"
mkdir -p "$PHASE05_M2"

# Example target: rpdptw/core
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_M2" \
  -pl rpdptw/core -am \
  -DskipTests \
  install

touch "$PHASE05_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_M2" \
  -pl rpdptw/core \
  -Dtest=PairPositionEnumeratorTest,PairInsertionEvaluatorTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test
```

Target별 exact substitution은 다음과 같다.

| Target report owner | Dependency preparation selector | Selected test selector | Fresh report owner |
|---|---|---|---|
| `rpdptw/core` | `-pl rpdptw/core -am -DskipTests install` | `-pl rpdptw/core -Dtest=<core classes> ... clean test` | `rpdptw/core/target/surefire-reports` |
| `rpdptw/solver` | `-pl rpdptw/solver -am -DskipTests install` | `-pl rpdptw/solver -Dtest=<solver classes> ... clean test` | `rpdptw/solver/target/surefire-reports` |
| `build/test-fixtures` | `-pl build/test-fixtures -am -DskipTests install` | `-pl build/test-fixtures -Dtest=<oracle classes> ... clean test` | `build/test-fixtures/target/surefire-reports` |
| `build/architecture-rules` | `-pl build/architecture-rules -am -DskipTests install` | `-pl build/architecture-rules -Dtest=Phase05ArchitectureTest ... clean test` | `build/architecture-rules/target/surefire-reports` |

모든 row는 별도 fresh `PHASE05_RUN_ROOT` 또는 provenance가 분리된 동일 run root를 사용한다. Evidence에는 wrapper/distribution hash, absolute `PHASE05_M2`, preparation/selected exact command와 exit code, start marker, target module/report path, report SHA-256와 sealed expected `Class#method` manifest를 남긴다.

Selected invocation이 exit `0`이어도 다음 AND gate를 모두 통과해야 green이다.

1. Selected invocation에 target `clean`과 `surefire.failIfNoSpecifiedTests=true`가 있다.
2. XML은 위 target report owner에만 있고 모두 `selected-test.started`보다 새롭다.
3. 실제 class 집합과 `Class#method` 집합이 WP-05.0에서 봉인한 non-empty expected manifest와 정확히 같다. Missing/extra/duplicate와 실행 0건은 실패다.
4. XML 합계 `tests > 0`, `failures = 0`, `errors = 0`, `skipped = 0`이다.
5. Dependency-preparation, 다른 module, 이전 run, IDE, console 요약과 ignored `target/` report를 합치지 않는다.
6. Selected report digest를 계산한 뒤 source/POM/test/status fingerprint가 바뀌면 report를 폐기하고 fresh run부터 다시 시작한다.

Phase 05 전체 exit에서는 accepted reactor snapshot에서 fresh isolated repository를 다시 만들고 root `clean verify`를 별도 실행한다. Selected green을 root reactor green으로 대체하거나 반대로 root green에서 expected Phase 05 method가 실행됐다고 추정하지 않는다.

## 7. Scope, non-scope와 결정 상태

### 7.1 Phase 05 scope

- Stable request-level route/bank XOR와 route/vehicle/terminal invariant validation
- Real pair와 delivery-only ownership의 원자적 insertion option
- Existing route와 unused concrete vehicle `NEW_ROUTE`
- Exhaustive real/delivery-only service-position enumeration
- Explicit bounded enumeration seam과 completeness honesty
- Phase 03/04 bound authority에 대한 단일 delegation
- `Feasible`, normal `Rejected`, contract/corruption `Invalid` 분리
- Base를 바꾸지 않는 pure materialization/evaluation
- Construction-only functional immutable transition
- Immutable `SearchRequestBank`, `SearchSnapshot`, candidate와 portfolio identity
- 4 growth × 2 vehicle-order 최대 8개 independent members
- CLOCK unavailable, stable tie, no-alias, corruption과 reproducibility evidence
- Phase 06 seed-only handoff

### 7.2 명시적 non-scope

- Phase 03 load/time/window/travel/stop/resource propagation 재구현
- Phase 04 profile/capability/score/objective/comparator binding 재구현
- Phase 06 destroy/removal editor, `TrialDraft`, COW apply, repair orchestration, acceptance, adaptive state, screen/champion/RNG/termination
- Phase 07 candidate verifier, final exhaustive insertion audit, diagnostic, outcome, publication
- Phase 13 route pool/MIP/OR-Tools/hybrid
- Apply/undo, mutable best/current, rollback log와 incremental authoritative cache
- Raw input, coordinate, speed, matrix, provider 또는 environment 재해석
- Multi-trip/rotation, MDVRP/OVRP/SDVRP, cross-trip pair
- Public/wire schema, official numeric budget, production calibration/cutover
- AWS/GCP/storage/workflow/HTTP DTO, SDK와 deployment

### 7.3 FIXED, PROPOSED, OPEN, GATED와 deferred

| 항목 | 상태 | 구현자가 할 일 |
|---|---|---|
| Stable pair/bank partition | FIXED | Partial/duplicate/split/reverse/overlap/missing을 defect로 거부 |
| Delegated feasibility | FIXED | Phase 03/04 result만 소비, 공식 복제 금지 |
| Phase 05 portfolio enumeration | FIXED | Exit evidence는 exhaustive 사용 |
| Bounded enumeration seam | PROPOSED | Explicit ordered keys/version/max 검증, runtime truncate 금지 |
| Immutable construction transition | FIXED semantics / PROPOSED API | 성공만 next snapshot, 실패 시 base exact 보존 |
| 4×2 portfolio | FIXED | 최대 8개 independent member, champion 없음 |
| Stable tie type/encoding | PROPOSED/OPEN | Business equality/API/target-key order 승인 전 freeze 금지 |
| Full-solution evaluator/ranking | OPEN CROSS-PHASE | Ad hoc route sum이나 placeholder scalar 금지 |
| Exact portfolio traversal | PROPOSED_REVIEW_REQUIRED | Golden trace와 owner 승인 전 hidden order 금지 |
| CLOCK coordinate convention | OPEN/PROPOSED | Missing → unavailable; 다른 policy fallback 금지 |
| Utilization missing/zero/tie | OPEN/PROPOSED | Phase 04 typed policy 없이는 production 구현 중지 |
| Identity encoding/hash | OPEN/PROPOSED | Semantic fields만 고정, public encoding은 architecture review |
| `Q-BENCH-02` official 수치 | OPEN — EXPERIMENT_REQUIRED | Test-only explicit 값만, production default 금지 |
| `C-17` route pool/MIP | GATED TARGET | Phase 14A receipt와 별도 승인 전 착수/의존성 추가 금지 |
| Phase 13 | OPTIONAL/GATED | Phase 05 DoD에 넣지 않는다 |
| `Q-VAR-01` | DEFERRED | 질문·활성화하지 않는다 |
| Multi-trip/rotation | DEFERRED FEATURE | Single-trip 계약을 유지한다 |
| Phase 14A corpus/threshold/repeat/budget | OPEN — EXPERIMENT_REQUIRED | 승인된 protocol 전 공식값 금지 |
| Phase 14B production authority | NOT_GRANTED/GATED | Phase 05 acceptance와 분리한다 |
| Public API/schema | OPEN | Internal proposed type을 compatibility promise로 만들지 않는다 |

`Q-INFRA-01`은 AWS target으로 `RESOLVED`다. 그러나 Phase 05 core에 AWS SDK나 AWS topology 의미를 추가할 권한은 아니다.

## 8. 개념에서 통합까지의 학습 경로

### 8.1 1단계 — 개념

종이에 request 세 개와 vehicle 두 개를 그리고 다음 질문에 답한다.

1. `Request`, pickup node, delivery node와 location은 어떻게 다른가?
2. Delivery-only logical pickup은 route에서 어디에 있는가?
3. 왜 bank에 last failure나 final outcome을 저장하면 안 되는가?
4. 왜 stable이 quality 최적을 뜻하지 않는가?
5. 왜 infeasible과 invalid를 다른 type으로 가져야 하는가?

완료 신호:

- Partial pair를 “낮은 점수”가 아니라 defect라고 설명한다.
- Bank membership과 final `UNASSIGNED`를 구분한다.
- `exact insertion`이 MIP를 뜻하지 않는다고 설명한다.

### 8.2 2단계 — 작은 탐색과 손 계산

Base service가 `[P1,D1]`이고 새 real pair가 `[P2,D2]`라고 하자. Terminal을 제외한 base service count `m=2`이므로 최종 두 위치 조합은 여섯 개다.

```text
(0,1) (0,2) (0,3) (1,2) (1,3) (2,3)
```

일반식:

```text
real pair legal count
  = C(m + 2, 2)
  = (m + 1)(m + 2) / 2

delivery-only legal count
  = m + 1
```

다음을 손으로 만든다.

- 기존 service 상대 순서가 유지되는 여섯 final sequence
- 각 sequence의 load prefix
- Feasible/rejected 위치 literal 표
- Position canonical order
- Delivery-only에서 logical pickup node/position/arc/stop이 0개라는 표

완료 신호:

- Production enumerator 없이 expected 표를 만들었다.
- Position을 terminal 포함 raw node index가 아니라 terminal 밖 final service index로 설명한다.
- Bounded miss가 exhaustive infeasibility가 아님을 설명한다.

### 8.3 3단계 — 실제 변경

Gate가 열린 뒤 다음 순서로 구현한다.

```text
value/validator
→ exhaustive enumerator
→ immutable materializer
→ bound authority delegation
→ functional construction transition
→ policy order
→ 4×2 portfolio
→ corruption/reproducibility
```

각 production type을 만들기 전에 exact future test가 올바른 이유로 red인지 기록한다. Type이 없어서 compile 실패한 것과 assertion이 계약 위반을 잡은 red를 구분한다.

완료 신호:

- 한 WP의 green을 다음 WP가 소비한다.
- Constructor가 defensive copy/validation을 실제 수행한다.
- Base bytes/fingerprint와 cross-member alias를 검사한다.

### 8.4 4단계 — 통합

최종 학습은 Phase 06과 Phase 07이 무엇을 받으며 무엇을 다시 계산하는지 설명하는 것이다.

자문 질문:

- Phase 06이 seed member를 그대로 mutate하면 어떤 invariant가 깨지는가?
- Phase 07이 search cache를 신뢰하면 왜 독립 verifier가 아닌가?
- CLOCK 둘이 unavailable이어도 portfolio 전체가 왜 실패가 아닌가?
- 한 request가 삽입되지 않았을 때 왜 final diagnostic을 만들 수 없는가?
- 같은 solution fingerprint를 가진 두 policy member의 lineage는 왜 둘 다 보존하는가?

완료 신호:

- Phase 05 handoff manifest의 필드를 빈 종이에 쓸 수 있다.
- Phase 05가 넘기지 않는 Phase 06/07 필드를 구분한다.
- Phase 13/14 gate를 Phase 05 success와 분리한다.

## 9. Pair position, policy와 state transition

### 9.1 Service position

Terminal을 제외한 base service sequence 길이를 `m`이라 한다.

Real pair:

```text
0 <= pickupPosition < deliveryPosition <= m + 1
```

Delivery-only:

```text
0 <= deliveryPosition <= m
logical pickup = route initial-load ownership
```

Real pair exhaustive order는 pickup position 오름차순, 같은 pickup 안에서 delivery position 오름차순이다. Delivery-only는 delivery position 오름차순이다. Oneway/roundtrip terminal은 position 수에 포함하지 않는다.

### 9.2 Exhaustive와 bounded

```text
EXHAUSTIVE
  all legal keys exactly once
  inspected == legal total
  feasible == 0이면 target에 대해 exhaustive miss 표현 가능

BOUNDED_EXPLICIT
  explicit ordered unique legal keys
  ordered key count <= maxEvaluations
  declaration bind 때 duplicate/out-of-range/overflow/excess 거부
  runtime truncate 없음
  feasible == 0이어도 NO_FEASIBLE_WITHIN_BOUND만 가능
```

Phase 05 correctness/portfolio는 `EXHAUSTIVE`다. Bounded seam은 향후 Phase 06 repair config가 별도로 승인해 소비할 수 있는 proposed contract다.

### 9.3 4×2 policy 의미

Canonical member order:

```text
CLOCK × DIRECT_FIRST_LARGE
CLOCK × DIRECT_FIRST_SMALL
SEQ_FARTHEST × DIRECT_FIRST_LARGE
SEQ_FARTHEST × DIRECT_FIRST_SMALL
SEQ_LARGE_DEMAND × DIRECT_FIRST_LARGE
SEQ_LARGE_DEMAND × DIRECT_FIRST_SMALL
SEQ_EARLIEST_DEADLINE × DIRECT_FIRST_LARGE
SEQ_EARLIEST_DEADLINE × DIRECT_FIRST_SMALL
```

| Policy | 고정된 역할 | Authority/tie | 금지 |
|---|---|---|---|
| `CLOCK` | Vehicle start terminal을 원점으로 request entry location을 clockwise 순회 | 승인된 coordinate convention/version, stable RequestId | Missing 좌표를 다른 정책/ID order로 fallback |
| `SEQ_FARTHEST` | Depot에서 먼 request seed, 이후 마지막 service에서 가까운 request | Prepared directed `D[current][entry]`, stable RequestId | Reverse/symmetric/raw coordinate distance |
| `SEQ_LARGE_DEMAND` | Vehicle 대비 utilization 큰 request seed, 이후 가까운 request | Exact rational utilization, deadline, RequestId | `double`, epsilon, Feature string |
| `SEQ_EARLIEST_DEADLINE` | 이른 normalized deadline seed, 이후 가까운 request | Deadline, utilization, RequestId | Raw date/string/input order |

Request entry location:

```text
REAL_PICKUP_DELIVERY → pickup location
DELIVERY_ONLY        → delivery location
```

Vehicle order는 모두 `DIRECT` concrete vehicles를 `LEASE`보다 먼저 둔다.

```text
utilization(request, vehicle)
= max(weight / weightCapacity, volume / volumeCapacity)
```

- `DIRECT_FIRST_LARGE`: 낮은 utilization, 즉 상대적으로 큰 vehicle 우선
- `DIRECT_FIRST_SMALL`: 높은 utilization, 즉 상대적으로 작은 vehicle 우선
- Ratio는 checked cross-product 또는 동등한 exact rational 비교를 사용
- Missing/zero capacity는 Phase 04 typed policy가 정함
- Enum ordinal, input order와 hash iteration을 canonical order로 쓰지 않음

Exact multi-route traversal은 아직 `PROPOSED_REVIEW_REQUIRED`다. 승인된 golden trace가 없으면 구현을 시작하지 않는다.

### 9.4 Functional lifecycle

```text
accepted Phase 02/03/04 immutable authorities
→ empty stable SearchSnapshot
    routes = []
    bank = all requests in canonical order
→ enumerate/materialize/evaluate one complete request
→ select feasible option by business comparator, then stable tie
→ functional ConstructionTransition
    replace/create route + remove bank request + fresh solution evaluation
→ next immutable SearchSnapshot
→ complete member attempts
→ cache-free final candidate validation
→ ConstructionCandidate
→ canonical ordered SeedPortfolio
→ Phase 06 seed input
```

### 9.5 Skeletal pseudocode

이 pseudocode는 control flow 계약이지 복사 가능한 완성 구현이 아니다.

```text
evaluatePairInsertion(command):
  require exact authority identities
  validate request, concrete target and enumeration declaration
  positions = declared canonical position keys

  for position in positions:
    route = materialize the complete request on a fresh immutable value
    result = bound authority evaluates route and whole candidate context

    Feasible → keep immutable option
    Rejected → keep typed attempt evidence, never rank
    Invalid  → discard partial output and fail closed

  verify count/completeness receipt
  order feasible options by business comparator
  apply insertion-context stable tie only on exact business equality
  return immutable completed evaluation
```

```text
applyConstruction(base, selected):
  require request is bank-only in base
  require exact base route/new unused vehicle and authority identities
  make fresh ordered routes and fresh bank
  obtain approved cache-free whole-solution evaluation
  validate pair/bank/vehicle/terminal/fingerprint
  publish next immutable snapshot only after every check

on stale/rejection/invalid/exception:
  publish no next snapshot
  require canonicalBytes(base) and fingerprint(base) unchanged
```

`approved cache-free whole-solution evaluation`은 현재 unresolved cross-Phase contract다. Placeholder route delta나 합성 objective로 채우지 않는다.

```text
buildPortfolio(authorities, config):
  validate exact policy matrix and total orders

  for member in canonical 4×2 order:
    if CLOCK and required coordinate missing:
      append Unavailable with exact evidence
      continue

    start independent all-bank empty snapshot and member-local scratch
    follow only approved deterministic traversal
    evaluate each request/target with EXHAUSTIVE pair insertion
    Invalid → fail whole portfolio
    no feasible option → keep request in bank, record normal rejection
    feasible → functional apply into a new snapshot
    finish with cache-free candidate validation
    append Available candidate

  derive available candidates only from member list
  validate no mutable cross-member aliases
  seal immutable SeedPortfolio
```

## 10. Proposed Java contract와 dependency

### 10.1 상태 표기 규칙

- `PROPOSED`: 내부 의미를 설명하는 후보. Review 전 public API가 아니다.
- `OPEN`: Owner/shape/encoding이 미확정이므로 compile freeze 금지.
- `FIXED SEMANTICS`: 이름은 바뀔 수 있지만 의미와 invariant는 바뀌면 안 된다.
- `TEST_ONLY`: Production default로 승격할 수 없는 fixture/config.

### 10.2 Core insertion 후보

```java
// PROPOSED internal value; constructor validation is required.
public sealed interface PairPositionKey
        permits PairPositionKey.RealPair, PairPositionKey.DeliveryOnly {

    record RealPair(int pickupPosition, int deliveryPosition)
            implements PairPositionKey {}

    record DeliveryOnly(int deliveryPosition)
            implements PairPositionKey {}
}

// PROPOSED. ExplicitBounded must defensively copy and reject bad declarations.
public sealed interface PairPositionEnumeration
        permits PairPositionEnumeration.Exhaustive,
                PairPositionEnumeration.ExplicitBounded {

    EnumerationPolicyFingerprint fingerprint();

    record Exhaustive(
            EnumerationPolicyVersion version,
            EnumerationPolicyFingerprint fingerprint)
            implements PairPositionEnumeration {}

    record ExplicitBounded(
            EnumerationPolicyKey key,
            EnumerationPolicyVersion version,
            List<PairPositionKey> orderedUniquePositions,
            int maxEvaluations,
            EnumerationPolicyFingerprint fingerprint)
            implements PairPositionEnumeration {}
}
```

Java record는 자동 defensive copy를 하지 않는다. Canonical constructor에서 null, range, duplicate, count/overflow, identity를 검증하고 `List.copyOf`를 사용한다. Nested element도 transitively immutable인지 확인한다.

```java
// PROPOSED; NEW_ROUTE always names a concrete input vehicle.
public sealed interface InsertionTarget
        permits InsertionTarget.ExistingRoute,
                InsertionTarget.NewRoute {

    VehicleId vehicleId();

    record ExistingRoute(
            VehicleId vehicleId,
            RoutePlan baseRoute,
            RouteEvaluationArtifact baseEvaluation)
            implements InsertionTarget {}

    record NewRoute(
            VehicleId vehicleId,
            NewRouteTerminalDeclaration terminalDeclaration)
            implements InsertionTarget {}
}

public record PairInsertionRequest(
        ProblemInstance problem,
        PreparedTravel preparedTravel,
        RequestId requestId,
        InsertionTarget target,
        PairPositionEnumeration enumeration,
        BoundInsertionAuthority authority,
        PairInsertionRequestFingerprint fingerprint) {}
```

`NewRouteTerminalDeclaration`은 Phase 02 vehicle/terminal authority를 가리키는 immutable reference 후보다. 임의 terminal이나 synthetic vehicle을 만들지 않는다.

```java
// OPEN cross-Phase consumer view. Final owner/name/signature is not approved.
public interface BoundInsertionAuthority {
    BoundInsertionResult evaluate(
            RequestId requestId,
            InsertionTarget target,
            PairPositionKey position,
            RoutePlan materializedRoute);

    BoundAuthorityFingerprint fingerprint();
}

public sealed interface BoundInsertionResult
        permits BoundInsertionResult.Feasible,
                BoundInsertionResult.Rejected,
                BoundInsertionResult.Invalid {

    // CandidateRankingArtifact is OPEN until whole-solution evaluator approval.
    record Feasible(
            RouteEvaluationArtifact routeEvaluation,
            CandidateRankingArtifact candidateRanking)
            implements BoundInsertionResult {}

    record Rejected(ConstraintRejection rejection)
            implements BoundInsertionResult {}

    record Invalid(EvaluationFailure failure)
            implements BoundInsertionResult {}
}
```

이 interface는 새로운 compatibility/capacity/time evaluator가 아니다. Phase 04가 bind한 declaration을 Phase 03 kernel과 승인된 solution evaluator에 전달하는 Phase 05 consumer view다.

```java
public interface PairInsertionEvaluator {
    PairInsertionEvaluation evaluate(PairInsertionRequest request);
}

public sealed interface PairInsertionEvaluation
        permits PairInsertionEvaluation.Completed,
                PairInsertionEvaluation.Invalid {

    record Completed(
            List<InsertionAttempt> orderedAttempts,
            List<FeasibleInsertionOption> orderedFeasibleOptions,
            EnumerationReceipt receipt)
            implements PairInsertionEvaluation {}

    record Invalid(
            EvaluationFailure failure,
            Optional<PairPositionKey> safePosition)
            implements PairInsertionEvaluation {}
}

public record EnumerationReceipt(
        EnumerationCompleteness completeness,
        long legalPositionCount,
        long evaluatedPositionCount,
        long feasiblePositionCount,
        long rejectedPositionCount,
        EnumerationPolicyFingerprint policyFingerprint) {}
```

### 10.3 Solver state와 portfolio 후보

```java
// PROPOSED internal immutable values.
public record SearchRequestBank(
        ProblemFingerprint problemFingerprint,
        List<RequestId> orderedUniqueRequestIds,
        SearchRequestBankFingerprint fingerprint) {}

public record SearchSnapshot(
        SolveAuthorityRef authority,
        List<EvaluatedRoute> routesInStableVehicleOrder,
        SearchRequestBank bank,
        CandidateEvaluationArtifact evaluation, // OPEN owner/shape
        SolutionFingerprint fingerprint) {}

public interface ConstructionTransition {
    ConstructionTransitionResult apply(
            SearchSnapshot base,
            FeasibleInsertionOption option);
}

public sealed interface ConstructionTransitionResult
        permits ConstructionTransitionResult.Applied,
                ConstructionTransitionResult.StaleOrMismatched,
                ConstructionTransitionResult.Invalid {

    record Applied(SearchSnapshot next)
            implements ConstructionTransitionResult {}

    record StaleOrMismatched(TransitionRejection rejection)
            implements ConstructionTransitionResult {}

    record Invalid(EvaluationFailure failure)
            implements ConstructionTransitionResult {}
}
```

이 `ConstructionTransition`은 empty/all-bank seed를 성장시키는 함수형 insertion transition 후보다. Phase 06 destroy/removal editor나 apply/undo가 아니다.

```java
public enum GrowthPolicy {
    CLOCK,
    SEQ_FARTHEST,
    SEQ_LARGE_DEMAND,
    SEQ_EARLIEST_DEADLINE
}

public enum VehicleOrderPolicy {
    DIRECT_FIRST_LARGE,
    DIRECT_FIRST_SMALL
}

public sealed interface PortfolioMember
        permits PortfolioMember.Available,
                PortfolioMember.Unavailable {

    PortfolioMemberId id();

    record Available(
            PortfolioMemberId id,
            ConstructionCandidate candidate)
            implements PortfolioMember {}

    record Unavailable(
            PortfolioMemberId id,
            PortfolioUnavailableReason reason,
            UnavailableEvidence evidence)
            implements PortfolioMember {}
}

public record SeedPortfolio(
        SolveAuthorityRef authority,
        List<PortfolioMember> membersInCanonicalMatrixOrder,
        SeedPortfolioFingerprint fingerprint) {
    // Available candidates are a derived immutable projection, not second storage.
}
```

`SeedPortfolio`가 member list와 별도의 independently supplied available candidate list를 저장하면 두 authority가 drift한다. Member list만 source of truth로 둔다.

### 10.4 Stable tie 후보

Business comparator가 다르다고 판정하면 그 결과가 최종이다. 완전 동률일 때만 다음 semantic tuple 후보를 사용한다.

```text
RequestId stable key
→ DIRECT before LEASE ownership rank
→ VehicleId stable key
→ existing route or NEW_ROUTE target key
→ service pattern
→ pickup position, delivery position
→ result RoutePlanFingerprint
```

이 field 집합과 non-tied reversal 금지는 고정 의미다. Exact target-key byte/order, business equality API와 fingerprint encoding은 `OPEN`이므로 승인 전 production type으로 freeze하지 않는다.

### 10.5 Dependency direction

```text
rpdptw-core:
  domain + travel
    → propagation + evaluation.api/runtime
      → evaluation.insertion

rpdptw-solver:
  core immutable/evaluation/insertion contracts
    → solver.state construction values
      → solver.portfolio

Phase 06:
  solver.state + solver.portfolio seed contracts
    → COW ALNS implementation
```

금지:

- `core/evaluation.insertion → solver`
- `core → profile implementation`, cloud/provider/vendor SDK
- `solver.portfolio → Phase 06 destroy/repair/acceptance/adaptive/termination`
- `solver.state → verification`, application, adapter 또는 final result
- Phase 06이 Phase 05 `.internal` 또는 builder scratch를 소비
- Placeholder `com.ronext.optimizer.*` 의존

## 11. Ordered work packages

모든 WP의 실제 구현은 entry gate가 열린 뒤에만 시작한다. 아래 file/type/command는 future target이며 Phase 00 accepted reactor가 다르면 architecture review 후 갱신한다.

### WP-05.0 — Entry, authority와 contract freeze

**목적과 이유**

구현자가 missing authority를 임의 default로 메우기 전에 source, owner, identity와 red-test 목록을 고정한다. 이 WP를 건너뛰면 이후 green test가 잘못된 계약을 검증할 수 있다.

**사전조건**

- Phase 00~04 accepted receipts
- Scheduler task/implementer/reviewer
- Source fingerprints 재검증
- Checkpoint B~D 승인 회의 가능

**예상 file/package/type**

- Production source 없음
- Contract decision record, red-test manifest, handoff/equality table
- Final internal package/visibility 후보

**구체 행동**

1. §3의 path/heading/blob을 재검증한다.
2. `ProblemInstance`→`BoundProfile` equality closure를 실제 artifact로 확인한다.
3. Full-solution evaluator와 comparator/tie owner를 결정한다.
4. Traversal/CLOCK/utilization과 Phase 05/06 editor ownership을 승인한다.
5. Proposed/open/test-only/public 항목을 분리한다.
6. Exact red test list와 expected failure reason을 봉인한다.
7. HEAD/source baseline과 timestamp가 있는 live status/content manifest를 각각 봉인하고 accepted Phase 00 receipt와 비교한다.
8. Module별 non-empty expected `Class#method` manifest, dependency-preparation/selected command와 report owner를 봉인한다.

**근거**

- Phase 05 review F-P05-001, F-P05-005, F-P05-011
- Execution Progress residual cross-Phase blocker

**금지 shortcut**

- Route objective 합으로 solution evaluator 대체
- Input order를 traversal로 채택
- Wrapper/module/test가 없는데 compile 성공 주장
- Scheduler task ID 임의 생성

**검증**

```bash
git rev-parse HEAD
git status --short --untracked-files=all -- docs pom.xml mvnw .mvn rpdptw build legacy
git rev-parse HEAD:docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md

# BLOCKED TEMPLATE: run only against the Phase 00 accepted reactor snapshot.
PHASE05_COMPILE_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-compile.XXXXXX")"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_COMPILE_ROOT/repository" \
  -pl rpdptw/core,rpdptw/solver -am \
  clean compile
```

예상 결과는 HEAD와 live snapshot의 분리, source/section/status 정확성, missing/default 0, approved internal contract compile이다. 현재 live wrapper/module skeleton의 성공이나 실패는 Phase 00 receipt가 없으므로 WP green이 아니라 `BLOCKED_TEMPLATE_PROBE`일 뿐이다.

**실패 해석, rollback과 handoff**

- Drift/owner 미결정이면 source/test 생성 금지
- Last safe point는 이 가이드, literal fixture와 decision question
- 승인된 contract/equality/red-test manifest를 WP-05.1에 전달

### WP-05.1 — Stable solution, bank, identity와 immutability

**목적과 이유**

Pair evaluator보다 먼저 안전한 source of truth를 만든다. 잘못된 snapshot을 허용하면 option 계산이 맞아도 portfolio가 손상된다.

**사전조건**

- WP-05.0 approved
- Phase 02 request/vehicle/terminal identity
- Phase 03/04 authority refs

**예상 file/package/type**

- `solver.state.SearchRequestBank`
- `solver.state.EvaluatedRoute`
- `solver.state.SearchSnapshot`
- `solver.state.StableSolutionValidator`
- `StableSolutionValidatorTest`
- `SearchSnapshotImmutabilityTest`

**구체 행동**

1. Ordered unique bank와 defensive immutable constructor를 만든다.
2. Complete pair/bank XOR, vehicle uniqueness, terminal/service pattern을 검증한다.
3. Stable vehicle/request order와 semantic fingerprint 입력을 명시한다.
4. Constructor input mutation과 accessor mutation probe를 작성한다.
5. Partial/duplicate/split/reverse/overlap/missing corruption builder를 만든다.

**근거**

- Master §6, Domain §2.4/§8, Integrated §9.1~9.4

**금지 shortcut**

- `Set` iteration을 canonical order로 사용
- Bank에 node/cost/failure/final outcome 저장
- Java record가 자동 immutable하다고 가정
- Partial pair를 `Rejected`로 낮춤

**검증**

```bash
PHASE05_SOLVER_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-solver.XXXXXX")"
PHASE05_SOLVER_M2="$PHASE05_SOLVER_RUN_ROOT/repository"
mkdir -p "$PHASE05_SOLVER_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_SOLVER_M2" \
  -pl rpdptw/solver -am \
  -DskipTests \
  install

touch "$PHASE05_SOLVER_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_SOLVER_M2" \
  -pl rpdptw/solver \
  -Dtest=StableSolutionValidatorTest,SearchSnapshotImmutabilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test
```

Expected:

- assigned/all-bank/mixed stable fixture pass
- partial/duplicate/split/reverse/overlap/missing/vehicle duplicate 모두 typed defect
- Mutable alias intersection 0
- Expected class/method 실행 수 > 0, skipped 0

실패는 “삽입 품질이 나쁨”이 아니라 state contract 결함이다. Invalid snapshot factory를 publish하지 않고 immutable upstream authorities로 rollback한다. Validated all-bank snapshot factory와 identity contract를 WP-05.2/05.3에 넘긴다.

### WP-05.2 — Exhaustive pair positions와 pure bound evaluation

**목적과 이유**

Pair option을 complete하게 만들고 feasibility를 한 authority에 위임한다. Enumeration과 feasibility를 분리해야 누락, 공식 복제와 false exhaustiveness를 잡을 수 있다.

**사전조건**

- WP-05.1 green
- Approved route/solution evaluation contract
- Comparator equality/tie contract

**예상 file/package/type**

- `evaluation.insertion.PairPositionKey`
- `PairPositionEnumeration`
- `InsertionTarget`
- `PairInsertionEvaluator`
- `BoundInsertionAuthority` 또는 승인된 대체 이름
- Enumerator/materializer internal types
- Core tests와 independent test-fixtures oracle

**구체 행동**

1. Real/delivery-only count와 canonical order를 checked arithmetic으로 구현한다.
2. Bounded declaration을 bind할 때 duplicate/range/max/overflow를 거부한다.
3. Complete pair를 fresh route value에 한 번에 materialize한다.
4. 각 position을 Phase 03/04/solution authority에 정확히 한 번 전달한다.
5. Rejected는 ranking에서 제외하고 Invalid는 즉시 fail-closed한다.
6. Receipt legal/evaluated/feasible/rejected count를 독립 계산과 비교한다.
7. Business comparator 후 stable tie를 적용한다.

**근거**

- Master §11.1, Domain §11, Canonical Phase 05 §6~8

**금지 shortcut**

- Pickup/delivery 별도 callback
- First feasible break
- Runtime `maxEvaluations` truncate
- Capacity/time/compatibility 조건문 복제
- `Invalid`를 `Rejected`로 변환
- Candidate ranking artifact를 route delta로 합성

**검증**

```bash
PHASE05_CORE_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-core.XXXXXX")"
PHASE05_CORE_M2="$PHASE05_CORE_RUN_ROOT/repository"
mkdir -p "$PHASE05_CORE_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_CORE_M2" \
  -pl rpdptw/core -am \
  -DskipTests \
  install

touch "$PHASE05_CORE_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_CORE_M2" \
  -pl rpdptw/core \
  -Dtest=PairPositionEnumeratorTest,PairInsertionEvaluatorTest,PairInsertionDelegationTest,PairInsertionCorruptionTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test

PHASE05_FIXTURE_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-fixture.XXXXXX")"
PHASE05_FIXTURE_M2="$PHASE05_FIXTURE_RUN_ROOT/repository"
mkdir -p "$PHASE05_FIXTURE_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_FIXTURE_M2" \
  -pl build/test-fixtures -am \
  -DskipTests \
  install

touch "$PHASE05_FIXTURE_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_FIXTURE_M2" \
  -pl build/test-fixtures \
  -Dtest=PairInsertionHandOracleTest,Phase05OracleSensitivityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test
```

Expected:

- `m=0..6` count/order/uniqueness exact
- §12 fixture 6 attempts, 2 feasible, 4 rejected
- Authority call position당 1회, duplicated formula 0
- Invalid 이후 later call/partial publish 0
- Base route bytes/fingerprint unchanged

Count/delegation/full-recompute identity 중 하나라도 실패하면 evaluator artifact를 폐기하고 WP-05.1 state만 유지한다. Pure option list, receipt와 `E-P05-INSERTION` 후보 record를 WP-05.3에 넘긴다.

### WP-05.3 — Functional atomic construction transition

**목적과 이유**

선택된 complete option을 route와 bank에 동시에 반영하되 base를 건드리지 않는다. 이 단계는 initial construction 전용이며 ALNS destroy/removal/COW가 아니다.

**사전조건**

- WP-05.1 stable snapshot
- WP-05.2 immutable feasible option
- Approved cache-free whole-solution evaluator

**예상 file/package/type**

- `solver.state.ConstructionTransition`
- `solver.state.internal.FunctionalConstructionTransition`
- `ConstructionTransitionTest`

**구체 행동**

1. Request가 base bank에 정확히 한 번 있고 route에 없음을 확인한다.
2. Existing target base fingerprint 또는 unused concrete new vehicle을 확인한다.
3. Route replace/create와 bank removal을 fresh values로 만든다.
4. Fresh full-solution evaluation과 fingerprint를 계산한다.
5. Stable invariant를 모두 확인한 뒤에만 `Applied(next)`를 노출한다.
6. Rejected/stale/invalid/exception에서 base canonical bytes를 비교한다.

**근거**

- Master §6.3/§12, Canonical Phase 05 §8.2

**금지 shortcut**

- In-place mutate 후 undo
- Destroy/remove API를 같이 구현
- Stale option을 다시 bind
- New route에 vehicle type/count sentinel 사용
- Evaluation 실패 후 partially built next 노출

**검증**

```bash
PHASE05_SOLVER_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-solver.XXXXXX")"
PHASE05_SOLVER_M2="$PHASE05_SOLVER_RUN_ROOT/repository"
mkdir -p "$PHASE05_SOLVER_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_SOLVER_M2" \
  -pl rpdptw/solver -am \
  -DskipTests \
  install

touch "$PHASE05_SOLVER_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_SOLVER_M2" \
  -pl rpdptw/solver \
  -Dtest=ConstructionTransitionTest,StableSolutionValidatorTest,SearchSnapshotImmutabilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test
```

Expected:

- Existing route replace와 new route create 모두 atomic
- Request bank에서 정확히 한 번 제거
- Same route/precedence/vehicle/terminal/fresh evaluation exact
- 모든 실패에서 base bytes/fingerprint before == after

Partial update나 base 오염이 있으면 transition 전체를 폐기한다. Apply/undo로 우회하지 않는다. Green construction transition과 `E-P05-PAIR` 후보 record를 WP-05.4에 넘긴다.

### WP-05.4 — Growth/vehicle policy와 최대 8개 portfolio

**목적과 이유**

서로 다른 deterministic construction lineage를 만들어 Phase 06이 한 초기해에 갇히지 않게 한다.

**사전조건**

- WP-05.2/05.3 green
- Approved traversal
- Approved CLOCK convention/reference vectors
- Approved utilization edge policy

**예상 file/package/type**

- `solver.portfolio.GrowthPolicy`
- `VehicleOrderPolicy`
- `PortfolioConstructionConfig`
- `PortfolioMember`
- `ConstructionCandidate`
- `SeedPortfolio`
- `InitialPortfolioBuilder`
- Policy internal types와 portfolio tests

**구체 행동**

1. 4×2 matrix key/version/order를 config에 명시한다.
2. CLOCK coordinate preflight를 member별로 수행한다.
3. Directed `D[current][entry]`와 exact utilization으로 policy order를 만든다.
4. Member마다 독립 all-bank snapshot과 scratch를 만든다.
5. 승인된 traversal로 exhaustive evaluator/functional transition을 호출한다.
6. Normal rejection이면 request를 bank에 남기고 trace만 기록한다.
7. Cache-free final candidate evaluation을 수행한다.
8. Members list에서 available candidates projection을 유일하게 파생한다.

**근거**

- `Q-ALG-01`, Master §11.2, Phase 05 review F-P05-006/F-P05-011

**금지 shortcut**

- Enum ordinal/input/hash order
- CLOCK fallback
- Reverse/symmetric distance
- `double` utilization/epsilon
- Member 간 mutable route/bank/cache/trace 공유
- Screen/champion/ALNS 실행

**검증**

```bash
PHASE05_SOLVER_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-solver.XXXXXX")"
PHASE05_SOLVER_M2="$PHASE05_SOLVER_RUN_ROOT/repository"
mkdir -p "$PHASE05_SOLVER_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_SOLVER_M2" \
  -pl rpdptw/solver -am \
  -DskipTests \
  install

touch "$PHASE05_SOLVER_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_SOLVER_M2" \
  -pl rpdptw/solver \
  -Dtest=PortfolioPolicyOrderTest,InitialPortfolioBuilderTest,InitialPortfolioIsolationTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test
```

Expected:

- Complete coordinate fixture는 canonical order 8 available
- Missing coordinate fixture는 2 CLOCK unavailable + 6 available
- Policy/request/vehicle/target/position lineage 재구성 가능
- Cross-member mutable alias 0
- Phase 06 screen/champion call 0

한 member의 mutation이 다른 member를 바꾸거나 hidden fallback/order가 있으면 portfolio 전체를 invalid로 처리한다. Green evaluator/transition은 보존할 수 있다. Immutable members와 trace를 WP-05.5에 넘긴다.

### WP-05.5 — Oracle, property, corruption, security와 reproducibility

**목적과 이유**

Production code와 같은 실수를 반복하지 않는 독립 expected path가 실제 결함을 잡는지 증명한다.

**사전조건**

- WP-05.1~05.4 functional green
- Test-only config와 repeat/parallel count가 명시됨

**예상 file/package/type**

- `PairInsertionHandOracleTest`
- `Phase05OracleSensitivityTest`
- `PortfolioCorruptionTest`
- `PairInsertionReproducibilityTest`
- `PortfolioReproducibilityTest`
- Test-local `BigInteger`/permutation/corruption helpers

**구체 행동**

1. `m=0..6` independent exhaustive final-permutation oracle를 실행한다.
2. Prefix load/utilization expected를 literal/`BigInteger`로 계산한다.
3. Missing position, reversed pair, fake pickup, reverse distance, duplicate delegation, `Invalid→Rejected`, comparator inversion과 alias defect double을 주입한다.
4. Pair/bank/artifact/fingerprint를 한 필드씩 손상한다.
5. Repeated/parallel/input-permuted build의 canonical bytes/fingerprint를 비교한다.
6. Safe aggregate만 telemetry projection에 포함하고 raw address/input/coordinate/secret/provider locator를 거부한다.
7. Capacity `6/6/2/4` oracle과 option selection을 분리하고, exact business equality assertion 뒤 stable `(0,1)`을 판정한다.
8. Non-tied control에서 stable-first `(0,1)`이 strictly better `(2,3)`을 뒤집지 못하는지 확인한다.

**근거**

- Integrated §19~24, Phase 05 review F-P05-007/F-P05-008

**금지 shortcut**

- Production enumerator로 expected 생성
- Production `toString()`/hash map으로 expected fingerprint 생성
- 승인 전 serialization bytes corruption을 canonical evidence로 주장
- Parallel completion order/elapsed를 identity로 사용
- Test repeat 수를 official production default로 승격

**검증**

```bash
PHASE05_CORE_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-core.XXXXXX")"
PHASE05_CORE_M2="$PHASE05_CORE_RUN_ROOT/repository"
mkdir -p "$PHASE05_CORE_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_CORE_M2" \
  -pl rpdptw/core -am \
  -DskipTests \
  install

touch "$PHASE05_CORE_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_CORE_M2" \
  -pl rpdptw/core \
  -Dtest=PairInsertionReproducibilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test

PHASE05_SOLVER_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-solver.XXXXXX")"
PHASE05_SOLVER_M2="$PHASE05_SOLVER_RUN_ROOT/repository"
mkdir -p "$PHASE05_SOLVER_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_SOLVER_M2" \
  -pl rpdptw/solver -am \
  -DskipTests \
  install

touch "$PHASE05_SOLVER_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_SOLVER_M2" \
  -pl rpdptw/solver \
  -Dtest=PortfolioCorruptionTest,PortfolioReproducibilityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test

PHASE05_FIXTURE_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-fixture.XXXXXX")"
PHASE05_FIXTURE_M2="$PHASE05_FIXTURE_RUN_ROOT/repository"
mkdir -p "$PHASE05_FIXTURE_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_FIXTURE_M2" \
  -pl build/test-fixtures -am \
  -DskipTests \
  install

touch "$PHASE05_FIXTURE_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_FIXTURE_M2" \
  -pl build/test-fixtures \
  -Dtest=PairInsertionHandOracleTest,Phase05OracleSensitivityTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test
```

Expected:

- Seeded faulty double마다 정확한 assertion red와 minimal counterexample
- Capacity receipt는 comparator blocker와 독립적으로 `6/6/2/4`
- Approved equality API가 있을 때 equality-first stable tie와 non-tied control 모두 green
- Surviving defect 0
- 모든 corruption typed invalid
- Repeat/parallel/input permutation의 member order, trace, solution/portfolio bytes와 fingerprints exact equality
- Sensitive field leakage 0

실패하면 portfolio를 Phase 06에 넘기지 않는다. Failure seed와 last safe candidate fingerprint를 보존한다. 세 planned evidence 후보를 WP-05.6에 전달한다.

### WP-05.6 — Architecture, evidence, review와 Phase 06 handoff

**목적과 이유**

개별 test green을 module boundary, immutable evidence와 독립 review를 통과한 accepted handoff로 바꾼다.

**사전조건**

- WP-05.1~05.5 green
- Fresh reports와 complete evidence candidates
- Independent reviewer

**예상 file/artifact**

- `Phase05ArchitectureTest`
- `E-P05-PAIR`
- `E-P05-INSERTION`
- `E-P05-PORTFOLIO`
- Pre-review evidence manifest
- Independent review report
- Post-review acceptance receipt
- `Phase05SeedPortfolioHandoff`

**구체 행동**

1. Full reactor clean verify와 architecture/bytecode rule을 실행한다.
2. Provider/customer/vendor/placeholder와 Phase 06/07 responsibility leakage를 검사한다.
3. Exact command, toolchain, test counts, source/build/config fingerprints와 failure seeds를 봉인한다.
4. Pre-review manifest를 봉인한 뒤 수정하지 않는다.
5. Independent review가 그 digest만 참조하게 한다.
6. Exit gate 통과 뒤 두 선행 digest를 참조하는 acceptance receipt를 발행한다.
7. Seed handoff의 bytes/digest/fingerprint와 rollback point를 Phase 06이 대조한다.

**근거**

- Master Realization Plan §9~11, Phase 05 §12~15

**금지 shortcut**

- `target/`이나 console 한 줄만 evidence로 사용
- Review verdict를 pre-review manifest에 backfill
- Receipt 없이 `ACCEPTED`
- OR-Tools/MIP를 default build에 추가
- Phase 06/07 동작을 Phase 05 E2E라고 부르기

**검증**

```bash
PHASE05_ROOT_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-root.XXXXXX")"
PHASE05_ROOT_M2="$PHASE05_ROOT_RUN_ROOT/repository"
mkdir -p "$PHASE05_ROOT_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_ROOT_M2" \
  clean verify

PHASE05_ARCH_RUN_ROOT="$(mktemp -d "${TMPDIR:-/tmp}/phase05-architecture.XXXXXX")"
PHASE05_ARCH_M2="$PHASE05_ARCH_RUN_ROOT/repository"
mkdir -p "$PHASE05_ARCH_M2"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_ARCH_M2" \
  -pl build/architecture-rules -am \
  -DskipTests \
  install

touch "$PHASE05_ARCH_RUN_ROOT/selected-test.started"
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE05_ARCH_M2" \
  -pl build/architecture-rules \
  -Dtest=Phase05ArchitectureTest \
  -Dsurefire.failIfNoSpecifiedTests=true \
  clean test
```

Expected:

- Optimizer-free ALNS-only default build pass
- Forbidden dependency/type/reference 0
- Expected tests non-zero, failed/error/skipped 0
- Evidence DAG digest 일치
- Independent review pass와 valid acceptance receipt

Bundle/review가 불완전하면 최대 `IMPLEMENTED_PENDING_EVIDENCE` 또는 `REVIEW_PENDING`이다. `ACCEPTED`나 handoff authority를 주장하지 않는다.

## 12. 테스트 구현 안내

### 12.1 Independent fixture와 oracle

`capacitySeparatesSixRealPairPositions()` test-only fixture:

```text
vehicle V1
  roundtrip
  weight/volume capacity = 5
  wide time/resource windows

physical locations
  terminal T, P1, D1, P2, D2 are pairwise distinct
  D(x,x)/U(x,x) = 0/0
  D(x,y)/U(x,y) = 1 meter/1 second for every x != y

existing R1
  real pickup P1 = +3
  real delivery D1 = -3
  base services = [P1, D1]

bank R2
  real pickup P2 = +4
  real delivery D2 = -4

all service seconds = 0
```

| Position | Final services | Prefix weight | Expected |
|---|---|---|---|
| `(0,1)` | `P2,D2,P1,D1` | `4,0,3,0` | FEASIBLE |
| `(0,2)` | `P2,P1,D2,D1` | `4,7,3,0` | REJECTED capacity |
| `(0,3)` | `P2,P1,D1,D2` | `4,7,4,0` | REJECTED capacity |
| `(1,2)` | `P1,P2,D2,D1` | `3,7,3,0` | REJECTED capacity |
| `(1,3)` | `P1,P2,D1,D2` | `3,7,4,0` | REJECTED capacity |
| `(2,3)` | `P1,D1,P2,D2` | `3,0,4,0` | FEASIBLE |

Pairwise-distinct location과 complete `1/1` off-diagonal travel은 두 feasible 순서의 roundtrip arc count·distance·travel time을 같게 만든다. 그러나 이것만으로 승인될 full-solution objective 전체의 equality를 추론하지 않는다. Capacity receipt와 selection oracle을 다음처럼 분리한다.

```text
capacity oracle
  assert legal/evaluated/feasible/rejected = 6/6/2/4
  assert feasible keys in canonical enumeration = [(0,1), (2,3)]
  do not assert selected key

test-only business authority spy
  receive the two complete cache-free candidate snapshots
  return the same exact ordered business-vector artifact B_EQ
  expose comparison == 0, equality fingerprint == exact same bytes

stable-tie oracle
  first assert businessCompare((0,1),(2,3)) == 0
  then and only then assert stable position key selects (0,1)

non-tied control
  authority spy returns B_23 strictly better than B_01
  assert selected == (2,3) even though stable key (0,1) sorts first
```

`B_EQ`, `B_23`과 `B_01`은 test-only exact artifacts이며 production objective dimension이나 default가 아니다. Approved comparator/business-equality API가 아직 `OPEN CROSS-PHASE`이면 capacity test만 independent green이고 두 selection tests는 `BLOCKED_UNTIL_COMPARATOR_CONTRACT`다. Stub method 이름을 production API로 freeze하거나 route distance equality를 business equality로 승격하지 않는다. Capacity 공식은 Phase 05 production code에 넣지 않고 Phase 03/04 authority와 독립 literal/`BigInteger` oracle이 검증한다.

Delivery-only fixture는 base `m=2`에서 delivery position `0,1,2`만 만들고 logical pickup physical effect가 0개임을 확인한다.

### 12.2 Exact test class/method matrix

| Class | Exact method 후보 | Fixture/oracle | Green 판정 |
|---|---|---|---|
| `PairPositionEnumeratorTest` | `enumeratesEveryLegalRealPairPositionExactlyOnce()` | Independent `m=0..6` expected | `C(m+2,2)`, all `p<d`, missing/duplicate 0, order exact |
|  | `enumeratesDeliveryOnlyDeliveryPositionsWithoutLogicalPickupVisit()` | Delivery-only `m=0..6` | `m+1`, logical pickup node/arc/stop 0 |
|  | `rejectsDuplicateOutOfRangeAndOverflowedBoundDeclarations()` | Corrupt config | Partial output 없이 typed invalid |
|  | `boundedMissNeverClaimsExhaustiveInfeasibility()` | Known feasible key를 제외한 bound | `NO_FEASIBLE_WITHIN_BOUND`만 |
| `PairInsertionHandOracleTest` | `enumeratesSixOptionsWithIndependentCapacityReceipt()` | §12.1 literal + independent oracle | Receipt `6/6/2/4`, feasible keys `(0,1),(2,3)`; selection 주장 없음 |
|  | `selectsStableFirstOnlyAfterExactBusinessEquality()` | Pairwise-distinct fixture + exact test-only equality authority | Equality assertion/fingerprint 먼저, 그 뒤 selected `(0,1)`; API 미승인 시 `BLOCKED_UNTIL_COMPARATOR_CONTRACT` |
|  | `doesNotLetStableKeyReverseNonTiedBusinessOrder()` | Authority spy: `(2,3)` strictly better, `(0,1)` stable-first | Selected `(2,3)`; non-tied reversal 0; API 미승인 시 blocked |
| `PairInsertionEvaluatorTest` | `materializesCompleteSameRoutePairForEveryOption()` | Real pair builder | Every route complete/same vehicle/precedence, base unchanged |
|  | `createsDeliveryOnlyInitialLoadOwnershipWithoutFakeVisit()` | Mixed service builder | Delivery + ownership only |
|  | `evaluatesNewRouteAgainstUnusedConcreteVehicleAndTerminal()` | Two-vehicle fixture | Exact concrete vehicle/terminal, sentinel 0 |
|  | `ordersFeasibleOptionsByBusinessComparatorThenStableTie()` | Tied/non-tied stub | Non-tied reversal 0, tied total order |
| `PairInsertionDelegationTest` | `delegatesCompatibilityCapacityTimeAndResourceWithoutReimplementation()` | Authority spy | Position당 authority call 1, local formula call 0 |
|  | `neverRanksRejectedOptions()` | Mixed results | Comparator input에 feasible만 |
|  | `abortsOnInvalidInsteadOfCallingItInfeasible()` | Middle invalid | Partial publish 0, later call 0 |
|  | `selectedOptionEqualsCacheFreeFullRecomputation()` | Approved joint fixture | Route/solution bytes와 fingerprints exact |
| `PairInsertionCorruptionTest` | `rejectsBaseRouteAndEvaluationFingerprintMismatch()` | One-field corrupt | Materialization 전에 invalid |
|  | `rejectsResultRouteChangedUnderOldOptionFingerprint()` | Corrupt option projection | Apply/rank 금지 |
| `StableSolutionValidatorTest` | `acceptsAssignedOrBankXorForEveryRequest()` | assigned/all-bank/mixed | Exact partition |
|  | `rejectsPartialDuplicateSplitReverseOverlapAndMissingAsDefect()` | Table corruption | 각 defect typed, infeasible 변환 0 |
|  | `rejectsSameConcreteVehicleOwnedByTwoRoutes()` | Two routes same vehicle | Candidate 생성 전 invalid |
|  | `acceptsDeliveryOnlyOwnershipWithoutPhysicalPickup()` | Mixed pattern | Logical pickup 부재가 defect 아님 |
| `ConstructionTransitionTest` | `atomicallyReplacesRouteAndRemovesRequestFromBank()` | Existing option | New snapshot only, fresh evaluation exact |
|  | `atomicallyCreatesNewRouteAndConsumesUnusedVehicle()` | `NEW_ROUTE` | Vehicle duplicate 0, bank remove one |
|  | `leavesBaseBytesUnchangedAfterRejectedStaleInvalidOrException()` | Fault injection | Before == after, next 미노출 |
| `SearchSnapshotImmutabilityTest` | `defensivelyCopiesRoutesBankArtifactsAndTraceInputs()` | Mutable constructor inputs | Post-mutation identity 불변 |
|  | `snapshotsAndPortfolioMembersHaveNoMutableCrossAlias()` | Identity graph inspector | Allowed immutable authority 외 intersection 0 |
| `PortfolioPolicyOrderTest` | `clockUsesExplicitZeroAxisAndClockwiseConvention()` | Approved vectors | Expected order와 convention fingerprint |
|  | `clockMissingCoordinateReturnsUnavailableWithoutFallback()` | Missing coordinate | Exact unavailable, fallback call 0 |
|  | `farthestSeedsFromDepotThenUsesDirectedDistanceFromLastService()` | Asymmetric `D` | Expected order, reverse lookup 0 |
|  | `largeDemandUsesExactUtilizationThenDeadlineAndRequestId()` | Rational-tie oracle | Floating/Feature inference 0 |
|  | `earliestDeadlineUsesDeadlineThenUtilizationAndRequestId()` | Normalized deadline | Exact order |
|  | `directVehiclesAlwaysPrecedeLeaseAndLargeSmallReverseUtilization()` | Ownership/capacity | DIRECT block first, non-tied reversal |
| `InitialPortfolioBuilderTest` | `buildsEightMembersInCanonicalPolicyMatrixOrder()` | Full coordinate fixture | 8 available, canonical IDs/order |
|  | `keepsSixNonClockMembersWhenBothClockMembersUnavailable()` | Missing coordinate | 2 unavailable + 6 available |
|  | `keepsNormallyInfeasibleRequestsInBankWithoutFinalDiagnostic()` | All rejected | Stable bank only, final reason 없음 |
|  | `recordsPolicyVehicleRequestTargetPositionAndEvaluationLineage()` | Trace oracle | Decisions reconstructable |
|  | `doesNotRunScreenChooseChampionOrCreateAlnsState()` | Forbidden-call spy | Phase 06 call/type 0 |
| `InitialPortfolioIsolationTest` | `membersDoNotShareMutableRouteBankCacheOrTraceState()` | Eight members | Mutable alias 0 |
|  | `equalSolutionsRetainDistinctMemberLineageWithoutChangingSolutionIdentity()` | Symmetric fixture | Same solution allowed, member IDs distinct |
| `PortfolioCorruptionTest` | `rejectsPairSplitBankOverlapCandidateAliasingAndStaleEvaluation()` | Field-projection corrupt builder | All typed invalid |
| `PairInsertionReproducibilityTest` | `sameInputsProduceSameAttemptsOptionsBytesAndFingerprints()` | Repeat/parallel | Exact order/bytes/fingerprint |
| `PortfolioReproducibilityTest` | `sameAuthorityAndConfigProduceIdenticalPortfolioAcrossRepeatedAndParallelBuilds()` | Explicit test-only repeats | Exact member/trace/identity |
|  | `inputCollectionOrderDoesNotAffectCandidateOrPortfolioIdentity()` | Permuted inputs | Canonical equality |
| `Phase05ArchitectureTest` | `coreInsertionDoesNotDependOnSolverCustomerProviderVendorOrVerifier()` | Bytecode/dependency | Forbidden reference 0 |
|  | `portfolioDoesNotContainAlnsDestroyRepairAcceptanceOrFinalVerification()` | Package/type rule | Phase 06/07 responsibility 0 |
|  | `placeholderOptimizerIsNotASeedPortfolioDependency()` | Dependency rule | `com.ronext.optimizer.*` 0 |
| `Phase05OracleSensitivityTest` | `detectsMissingReverseFakePickupWrongDelegationComparatorAndAliasFaults()` | Deliberately faulty doubles | 모든 seeded defect가 red |

### 12.3 Red → green 순서

| 순서 | Red group | 올바른 red | Green 조건 |
|---:|---|---|---|
| 1 | Stable partition/immutability | Invalid state나 alias가 통과 | 모든 corruption invalid, no-alias |
| 2 | Position enumeration | Count/order/completeness mismatch | Oracle와 `m=0..6` exact |
| 3 | Hand insertion/delegation | 6-option mismatch, formula duplication, invalid 은폐 | Capacity `6/6/2/4`, one-call, fail-closed; selection은 equality assertion 뒤 stable tie와 non-tied control 둘 다 green일 때만 |
| 4 | Functional transition | Bank/route partial update, base 오염 | Atomic next, failure pre-state exact |
| 5 | Policy/4×2 | Hidden order/fallback/floating tie | Approved golden trace |
| 6 | Corruption/isolation | Split/alias/stale artifact 통과 | 모든 corruption invalid |
| 7 | Repro/architecture/handoff | Parallel 차이 또는 forbidden dependency | Canonical equality, forbidden 0 |

Type이 없어서 compile failure인 최초 red도 기록할 수 있지만, contract red로 인정하려면 해당 assertion이 deliberately faulty implementation을 실제 검출해야 한다. Test disable/skip, production code로 expected 생성, assertion 삭제와 tolerance 확대는 green이 아니다.

### 12.4 Test layer별 적용성

| Test 종류 | Phase 05 적용 | Pass 기준 또는 미적용 이유 |
|---|---|---|
| Unit/boundary | 적용 | Position/count/constructor/range/overflow exact |
| Contract | 적용 | Phase 03/04/solution authority delegation 1경로, fallback 0 |
| Integration | 적용 | Core insertion → solver transition → portfolio를 in-process exact authority fixture로 연결 |
| E2E | Phase-local 제한 적용 | All-bank snapshot → seed portfolio까지. HTTP/cloud/result publication E2E는 Phase 08/11/14 소유 |
| Architecture | 적용 | Module/package/vendor/customer/provider/Phase ownership forbidden reference 0 |
| Fault injection | 적용 | Invalid/exception/stale/cancellation probe에서 partial publish 0, base 보존 |
| Corruption | 적용 | Pair/bank/route/artifact/fingerprint/alias 한 필드 손상 모두 거부 |
| Reproducibility | 적용 | Repeat/parallel/input permutation의 canonical bytes/fingerprints/trace exact |
| Security | Core 범위 적용 | Environment/provider/secret을 의미 입력으로 읽지 않음, sensitive log/trace leakage 0 |
| Performance | 정량 acceptance 미적용 | Legal/evaluated count와 allocation/alias counters는 기록하되 승인된 threshold가 없어 pass/fail 성능값을 발명하지 않음 |
| Provider integration | 미적용 | Phase 05는 provider SDK/port를 소유하지 않음 |
| Production/cutover | 미적용 | Phase 14B authority gate 소유 |

Performance smoke는 exhaustive count가 receipt와 맞고 hidden time truncation이 없음을 확인하는 correctness guard다. Microbenchmark 결과는 정보가 될 수 있지만 승인된 corpus/threshold/budget 전에는 Phase 05 acceptance gate가 아니다.

### 12.5 False-green 방지

1. Phase 00 accepted source/wrapper/reactor fingerprint에서만 시작하고, 새 `mktemp` run root와 새 `maven.repo.local`을 만든다.
2. Sibling dependency는 같은 isolated repository에서 `-pl <target> -am -DskipTests install`로 준비한다. 이 invocation은 test evidence가 아니다.
3. Selected invocation은 `-am` 없이 target module만 지정하고 반드시 `clean test`, exact `-Dtest=...`, `-Dsurefire.failIfNoSpecifiedTests=true`를 사용한다.
4. Selected 시작 marker 뒤 target-owned `target/surefire-reports/TEST-*.xml`만 읽는다. Dependency-preparation, 다른 module과 이전 run report를 합치지 않는다.
5. WP-05.0에서 봉인한 expected `Class#method` manifest는 non-empty여야 하고 actual XML과 양방향 차집합, duplicate가 모두 0이어야 한다.
6. XML의 `tests > 0`, `failures = 0`, `errors = 0`, `skipped = 0`을 별도로 합산한다. Maven exit `0`이나 `BUILD SUCCESS`만으로 green 처리하지 않는다.
7. Root `clean verify`도 별도 fresh isolated repository에서 실행하고 모든 required Phase 05 manifest가 실제 report에 포함됐는지 대조한다. Root green과 selected green은 서로를 대체하지 않는다.
8. Stale/ignored `target/`, IDE 한 건 green, console 요약, current Phase 00/legacy test와 Phase 05 test를 섞지 않는다.
9. Exact command, wrapper/distribution, absolute local-repository path, report owner/digest, source/POM/test/config fingerprint와 start/end 시각을 evidence에 남긴다.
10. Report 생성 뒤 source/POM/test/status hash가 바뀌면 stale로 폐기한다. Test-only config와 official/production config는 artifact identity에서 구분한다.

## 13. 사람 checkpoint, evidence와 stop/resume

### 13.1 WP별 checkpoint 질문

| 시점 | 사람이 답해야 할 질문 | 계속 조건 |
|---|---|---|
| WP-05.0 종료 | Full-solution evaluator/tie/traversal/editor owner가 모두 승인됐는가? | 문서화된 approval + compile/red-test manifest |
| WP-05.1 종료 | Invalid stable state를 만들 수 있는 public/internal factory가 남았는가? | Corruption 전부 typed invalid, no-alias |
| WP-05.2 종료 | 모든 legal position을 정말 평가했는가? Invalid를 숨기지 않았는가? | Independent count/receipt/delegation evidence |
| WP-05.3 종료 | 실패 뒤 base canonical bytes가 정말 같은가? | Before/after exact |
| WP-05.4 종료 | Policy 역할과 golden trace가 맞고 member가 독립적인가? | 8 또는 2 unavailable+6, alias 0 |
| WP-05.5 종료 | Oracle가 deliberately faulty doubles를 잡는가? | Surviving defect 0 |
| WP-05.6 종료 | Evidence/review/receipt DAG가 단방향인가? | Digest closure + scheduler acceptance |

### 13.2 Planned evidence 내용

| Evidence | 최소 내용 | 현재 상태 |
|---|---|---|
| `E-P05-PAIR` | Source/build/authority fingerprints, pair/bank/vehicle/terminal property, transition before/after bytes, failure unchanged, no-alias, safe telemetry | NOT_PRODUCED |
| `E-P05-INSERTION` | Position formula/receipt, literal six-option table, permutation/BigInteger oracle, defect sensitivity, delegation trace, feasible/rejected/invalid와 cache-free equality | NOT_PRODUCED |
| `E-P05-PORTFOLIO` | 4×2 config/version, 8-member/CLOCK unavailable traces, tie cases, derived available projection, isolation, repeat/parallel identity와 redaction | NOT_PRODUCED |

각 evidence artifact에는 source commit/sections, implementation build, toolchain, exact command/exit code, test count, fixture/config digest, failure seed, known limitation, OPEN/GATED/deferred snapshot과 rollback identity를 넣는다.

### 13.3 단방향 evidence DAG

```text
immutable implementation/test evidence
  → preReviewEvidenceManifest [digest M]
  → independentReviewReport [references M, digest R]
  → postReviewAcceptanceReceipt [references M + R]
  → ACCEPTED / Phase 06 handoff
```

Pre-review manifest에 reviewer, review verdict/reference/digest, acceptance 상태/receipt를 넣거나 나중에 backfill하지 않는다.

### 13.4 Safe observability

허용 가능한 aggregate:

```text
problem/travel/profile/config/build fingerprints
portfolio member policy key/version
enumeration completeness and legal/evaluated/feasible/rejected counts
transition result category and stable failure code
candidate/portfolio fingerprint
no-alias/corruption counters
requested/completed test work
reproducibility envelope ID
```

금지:

- Raw address/coordinate/full input
- External order/customer free text
- Secret, credential, signed URL, provider locator
- Full exception payload
- Thread ID, object identity와 arbitrary descriptor content
- Elapsed/completion order를 tie, quality, identity 또는 pass/fail 입력으로 사용

### 13.5 Stop record

중단할 때 다음을 남긴다.

```text
phase: 05
last_completed_wp:
source_commit:
authority_fingerprints:
last_safe_snapshot_or_artifact_fingerprint:
failing_test_and_seed:
failure_category:
partial_artifact_discarded: true|false
open_owner:
restart_condition:
rollback_reference:
```

`Invalid`, exception, cancellation 또는 evidence corruption 뒤 partial attempt/member/portfolio를 정상 artifact로 seal하지 않는다.

## 14. Anti-pattern과 실패 해석

### 14.1 Domain/algorithm anti-pattern

- Pickup과 delivery를 별도 evaluator/apply call로 삽입
- Partial pair를 낮은 score/normal infeasible/bank로 숨김
- Delivery-only logical pickup을 depot/customer visit으로 생성
- Pair를 route/vehicle에 split하거나 delivery-first option 생성
- Compatibility/capacity/time/resource 공식을 Phase 05에 복제
- Route delta/placeholder scalar로 solution ranking 합성
- `Invalid`를 rejection으로 낮추고 다음 option 계속
- First-feasible break 후 exhaustive라고 기록
- Bounded miss를 final `NO_FEASIBLE_INSERTION`이라고 표현
- `NEW_ROUTE`에 vehicle type/count/sentinel 사용
- Reverse/symmetric/raw distance fallback
- `double`, epsilon, Feature string으로 utilization/tie 계산
- CLOCK missing을 다른 policy로 fallback
- Input/hash/enum ordinal로 stable order 결정

### 14.2 State/identity anti-pattern

- Mutable route/bank/cache/trace의 candidate 간 공유
- Member list와 별도 independently supplied candidate list
- `toString()`, object address, timestamp, thread order로 fingerprint
- Constructor에서 mutable list를 그대로 저장
- Search cache/summary를 cache-free authority로 사용
- In-place mutate 후 undo를 Phase 05에 도입
- Accepted identity bytes overwrite

### 14.3 Scope/architecture anti-pattern

- Placeholder `AlnsBatchEngine` candidate를 seed contract로 감싸기
- Phase 05에서 destroy/repair/acceptance/adaptive/screen/champion 구현
- Phase 07 verifier/final audit/diagnostic/publication 흉내 내기
- Core/solver에 AWS/GCP/HTTP/Jackson/provider/OR-Tools type 도입
- Phase 13을 Phase 05 선행조건으로 만들기
- Phase 14A/14B gate를 하나로 합치기
- Test-only 수치/coordinate/capacity rule을 production default로 승격

### 14.4 실패 category 해석

| 관찰 | 해석 | 행동 |
|---|---|---|
| 한 option capacity/time violation | 정상 `Rejected` | Trace/count에 기록, ranking 제외 |
| 모든 exhaustive option rejected | 이 target의 normal miss | Request는 bank에 남을 수 있음 |
| Bounded set에서 feasible 없음 | Bound 내부 miss | Exhaustive/final diagnostic 주장 금지 |
| Fingerprint mismatch/overflow/partial pair | `Invalid` defect | 즉시 fail-closed, partial output 폐기 |
| CLOCK coordinate missing | 해당 CLOCK member `Unavailable` | 나머지 6 member 계속 가능 |
| Non-CLOCK policy config missing | Build invalid | Fallback 금지 |
| Same solution from two policies | 정상 가능 | Solution identity 공유 가능, lineage 분리 |
| Root placeholder test pass | Characterization | Phase 05 evidence 아님 |
| Selected Maven test 0건 | False green | 실패로 판정 |

## 15. Exit checklist와 Definition of Done

### 15.1 구현 완료 checklist

- [ ] Phase 00~04 acceptance receipts와 authority fingerprints가 exact다.
- [ ] Full-solution evaluator/ranking, comparator/tie와 traversal policy가 승인됐다.
- [ ] Phase 05/06 editor ownership과 signatures가 승인됐다.
- [ ] Pair/bank XOR, same vehicle, exactly once, precedence와 vehicle uniqueness를 검증한다.
- [ ] Delivery-only logical pickup의 physical effect가 0이다.
- [ ] Real/delivery-only exhaustive position count/order가 independent oracle와 같다.
- [ ] Explicit bounded declaration은 fail-closed이고 runtime truncate가 없다.
- [ ] Phase 03/04 delegation은 한 경로이며 공식 복제가 없다.
- [ ] Rejected는 rank되지 않고 Invalid는 partial output 없이 중단한다.
- [ ] Existing route와 concrete unused `NEW_ROUTE` transition이 atomic이다.
- [ ] 모든 실패에서 base canonical bytes/fingerprint가 보존된다.
- [ ] 4×2 matrix 또는 2 CLOCK unavailable + 6 available이 canonical order로 나온다.
- [ ] Candidate/member mutable alias가 0이다.
- [ ] Business comparator가 stable tie보다 우선한다.
- [ ] Order sensitivity, corruption과 seeded-defect sensitivity를 통과한다.
- [ ] Repeat/parallel/input permutation에서 canonical bytes/fingerprint/trace가 같다.
- [ ] Provider/customer/vendor/placeholder와 Phase 06/07 leakage가 0이다.
- [ ] Safe telemetry/redaction test가 통과한다.
- [ ] `E-P05-PAIR`, `E-P05-INSERTION`, `E-P05-PORTFOLIO`가 봉인됐다.
- [ ] Pre-review manifest → independent review → acceptance receipt digest가 닫혔다.
- [ ] Phase 06 handoff와 rollback point가 exact하다.
- [ ] OPEN/GATED/deferred/EXPERIMENT_REQUIRED 상태를 임의 값으로 닫지 않았다.

### 15.2 Definition of Done

Phase 05는 다음이 모두 참일 때만 `ACCEPTED`다.

1. Entry/owner/authority gate가 통과했다.
2. Canonical Phase/review와 proposed/open 결정이 승인됐다.
3. Applicable unit, contract, integration, architecture, fault, corruption, reproducibility와 security test가 통과했다.
4. 세 evidence가 immutable identity/digest를 가진다.
5. Independent review와 post-review acceptance receipt가 유효하다.
6. Available seeds는 cache-free full evaluation과 exact 일치하고 no-alias다.
7. Normal rejection, bounded miss와 invalid defect가 분리된다.
8. 최대 8개 lineage와 CLOCK unavailable 의미가 보존된다.
9. Phase 06은 seed만 소비하며 Phase 05가 downstream 책임을 끌어오지 않는다.
10. `Q-BENCH-02`, `C-17`, `Q-VAR-01`, multi-trip, public schema와 Phase 14 production authority를 닫지 않는다.

Production source와 test가 있어도 evidence가 미완성이면 `IMPLEMENTED_PENDING_EVIDENCE`, pre-review manifest만 봉인됐으면 `REVIEW_PENDING`이다. Receipt 없이 `ACCEPTED`가 아니다.

## 16. 다음 Phase handoff와 broken 증상

### 16.1 Phase 03/04에서 받아야 하는 것

Phase 03:

- Immutable `RoutePlan`
- Pure `RouteEvaluationKernel`
- `Feasible/Infeasible/Invalid`
- `RouteEvaluationArtifact`, `ConstraintRejection`, `EvaluationFailure`
- Business comparator/equality와 exact identity

Phase 04:

- Exact customer/profile/version/preset
- Immutable `BoundProfile`/`SolvePlan`
- Bound hard/metric/score/objective/comparator closure
- Phase 05 consumer evaluation view
- Approved solution-level evaluator 또는 공동 owner contract
- Typed utilization edge policy
- Accepted evidence/review/receipt

현재 둘은 accepted implementation handoff가 아니다. Placeholder evaluator, permissive profile, customer branch 또는 default comparator로 빈틈을 메우지 않는다.

### 16.2 Phase 06에 넘길 것

```text
Phase05SeedPortfolioHandoff
  source commit and accepted Phase 05 document/review/evidence digests
  Problem/PreparedTravel/BoundProfile/SolvePlan fingerprints
  portfolio/enumeration/tie config fingerprints
  canonical ordered PortfolioMember refs
  each available ConstructionCandidate
  each immutable SearchSnapshot/SolutionFingerprint
  unavailable member records
  cache-free validation refs
  E-P05-PAIR / E-P05-INSERTION / E-P05-PORTFOLIO refs
  acceptance receipt ref
  rollback point
```

Phase 06 acceptance check:

- Candidate bytes/digest/fingerprint와 manifest가 exact 일치
- 모든 candidate가 stable pair/bank/vehicle/terminal invariant 만족
- Candidate full evaluation이 approved cache-free evaluator와 exact 일치
- Mutable cross-candidate alias 0
- Seed를 직접 mutate하지 않고 Phase 06 changed-route COW로 가져감

넘기지 않는 것:

```text
phase-1 champion
screenMaxSteps or official numeric value
destroy/repair proposal
TrialDraft/current/stageBest/solveBest
acceptance/adaptive/RNG state
termination/completed-step
candidate verifier PASS
final outcome/diagnostic/publication
route pool/MIP/backend artifact
```

### 16.3 Handoff가 깨졌을 때의 증상

| Broken 증상 | 가능한 원인 | 돌아갈 지점 |
|---|---|---|
| Phase 06이 seed를 읽자 fingerprint mismatch | Encoding/config/authority drift | Accepted Phase 05 handoff digest |
| Candidate마다 같은 route가 같이 변함 | Mutable cross-alias | WP-05.4 이전 green state/evaluator |
| Screen order에 따라 champion이 달라짐 | Portfolio order/tie/Phase 06 fan-in defect | Phase 05 canonical members + Phase 06 comparator |
| Phase 07이 pair split을 발견 | Stable validator/transition 또는 handoff corruption | `E-P05-PAIR`과 last accepted seed |
| Phase 07 objective가 다름 | Full-solution evaluator/cache identity drift | WP-05.0 joint evaluator contract |
| CLOCK missing인데 8 available | Hidden fallback | WP-05.4 policy implementation |
| Bank failure가 final diagnostic으로 노출 | Phase 05/07 responsibility 혼합 | Search bank source of truth |
| OR-Tools/native가 Phase 05 build에 필요 | Phase 13 gate leakage | Phase 00 architecture + Phase 05 dependency DAG |

### 16.4 Rollback

Phase 06 handoff 전 실패하면 incomplete portfolio를 폐기하고 accepted Phase 03/04 immutable authorities와 green한 lower WP artifact만 보존한다. 일부 member, 가장 좋은 route 또는 console output을 새 portfolio로 합성하지 않는다.

Accepted Phase 05 이후 회귀면 이전 accepted `Phase05SeedPortfolioHandoff` digest로 돌아간다. 같은 identity의 bytes를 overwrite하지 않는다.

## 17. Source → requirement → WP → test/evidence 추적성

| Requirement | Source | Work package/contract | Exact test/evidence |
|---|---|---|---|
| `REQ-PAIR` same vehicle/exactly once/precedence/XOR | [Master §6](../../../master-design.md#6-핵심-불변조건과-atomic-mutation), Domain §2.4/§8 | WP-05.1/05.3 stable validator와 atomic transition | `StableSolutionValidatorTest`, `ConstructionTransitionTest`, `E-P05-PAIR` |
| `REQ-SERVICE-PATTERN` real + delivery-only | `Q-REQ-01`, [Domain §2.3](../../../2026-07-26-domain-design.md#23-delivery-only와-real-pickup-delivery) | WP-05.1/05.2 ownership vs physical visit | Delivery-only enumerator/materializer tests, `E-P05-PAIR/INSERTION` |
| `REQ-POSITION` all legal pair positions | [Master §11.1](../../../master-design.md#111-공통-pair-evaluator), Domain §11 | WP-05.2 exhaustive/bounded receipt | `PairPositionEnumeratorTest`, hand six-option oracle, `E-P05-INSERTION` |
| `REQ-DELEGATION` feasibility authority | Master §4.3/§11.1, Phase 03 handoff | WP-05.0/05.2 one bound authority path | `PairInsertionDelegationTest`, invocation trace |
| `REQ-FULL-SOLUTION-EVAL` routes + bank objective | Master §4.2/§15.4~15.5, review F-P05-001 | WP-05.0 blocker, WP-05.2/05.3 approved evaluator | Reciprocal compile/full-equality/corruption test, `E-P05-INSERTION/PORTFOLIO` |
| `REQ-FAILURE-TRIAGE` rejected vs invalid | Master §6.3, Phase 03 | WP-05.2 fail-closed, WP-05.3 unchanged base | Delegation invalid/rejected methods, transition fault, `E-P05-PAIR/INSERTION` |
| `REQ-NEW-ROUTE` concrete unused vehicle | Master §11.6, Integrated §9.8 | WP-05.2/05.3 `NewRoute(VehicleId)` | New route evaluator/transition methods, `E-P05-PAIR/INSERTION` |
| `REQ-PORTFOLIO` 4×2 | `C-16`, `Q-ALG-01`, [Master §11.2](../../../master-design.md#112-현재-범위의-initial-solution-portfolio) | WP-05.4 canonical matrix | `buildsEightMembersInCanonicalPolicyMatrixOrder()`, `E-P05-PORTFOLIO` |
| `REQ-CLOCK` coordinate/missing meaning | Master §11.2 | WP-05.0 approval + WP-05.4 unavailable | CLOCK vector/missing tests, `E-P05-PORTFOLIO` |
| `REQ-SEQUENTIAL` directed closeness/demand/deadline | Master §11.2, `Q-ALG-01` | WP-05.4 exact policy order | `PortfolioPolicyOrderTest`, `E-P05-PORTFOLIO` |
| `REQ-TIE` objective first + stable identities | Master §11.1~11.2, review F-P05-005 | WP-05.0 approval, WP-05.2/05.4 | Comparator-law/non-reversal/policy tie tests |
| `REQ-IMMUTABLE` candidate/portfolio no-alias | Master §4.5~4.6/§12, Domain §8 | WP-05.1/05.3/05.4 | Immutability/isolation/corruption tests, `E-P05-PAIR/PORTFOLIO` |
| `REQ-ORACLE` independent defect sensitivity | Integrated §22.2~22.4, review F-P05-007 | WP-05.5 literal/permutation/BigInteger/faulty doubles | Hand oracle, `Phase05OracleSensitivityTest`, all evidence |
| `REQ-REPRO` stable order/identity | [Master §13](../../../master-design.md#13-termination-reproducibility와-execution-provenance) | WP-05.5 repeat/parallel/permutation | Repro tests, `E-P05-PORTFOLIO` |
| `REQ-SECURITY-OBS` safe fields/redaction | Integrated §19~21, Plan §13 | WP-05.5 safe telemetry | Redaction/failure identity tests, all evidence |
| `REQ-ARCH-DAG` core/solver/Phase boundary | [Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계), Integrated §23~24 | WP-05.6 architecture rules | `Phase05ArchitectureTest`, architecture report |
| `REQ-HANDOFF-P06` seed only | Plan Phase 05~06, Phase 06 §16.1 | WP-05.6 immutable handoff | No-screen/no-ALNS test, handoff digest |
| `REQ-ALNS-FIRST` MIP-independent path | Implementation README §3/§6 | WP-05.6 optimizer-free build | Root clean verify, forbidden OR-Tools ref 0 |
| `REQ-GATES` open/gated/deferred preservation | Question register, Plan §14 | 모든 WP stop/resume | Gate snapshot digest, review checklist |

새 requirement, hard rule, ranking dimension, numeric default, public schema 또는 traversal 의미를 발견하면 source, owner, WP, test와 evidence를 연결한다. 구현 편의를 위해 미확정 값을 hidden default로 채우지 않는다.

## 18. 구현자가 마지막으로 확인할 짧은 판정표

| 질문 | 예라면 | 아니오라면 |
|---|---|---|
| Phase 00~04 acceptance receipts가 있는가? | 다음 gate 확인 | 구현하지 말고 학습/fixture 설계에서 중지 |
| Full-solution evaluator와 comparator/tie가 승인됐는가? | Position/evaluator contract 진행 | Route delta/objective를 발명하지 말고 중지 |
| Traversal/CLOCK/utilization이 승인됐는가? | Portfolio 구현 진행 | Hidden order/default 없이 중지 |
| Pair option은 항상 complete한가? | Delegation 검증 | Materializer부터 수정 |
| Rejected와 Invalid가 type으로 분리되는가? | Ranking 진행 | Fail-closed contract 수정 |
| Base와 members가 immutable/no-alias인가? | Portfolio seal | Handoff 금지 |
| 4×2 또는 2 unavailable+6의 lineage가 있는가? | Evidence 준비 | Policy/fallback 수정 |
| Expected tests가 실제 non-zero 실행됐는가? | Review 준비 | False green으로 실패 처리 |
| Evidence DAG와 acceptance receipt가 유효한가? | Phase 06 handoff 가능 | `ACCEPTED` 주장 금지 |
| Phase 13/14B를 Phase 05 gate로 끌어오지 않았는가? | Scope 정상 | Optional/production gate 분리 |

현재 checkout에서 이 표의 첫 질문 답은 **아니오**다. 따라서 현재의 정확한 상태는 `BLOCKED / NOT_ACCEPTED / NOT_PRODUCED`이며, 이 가이드는 그 상태를 숨기지 않는다.
