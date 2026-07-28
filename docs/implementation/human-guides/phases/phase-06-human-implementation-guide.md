# Phase 06 사람용 구현 가이드 — COW ALNS와 reproducibility

```yaml
guide_status: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
guide_scope: Phase 06 only
canonical_phase_count: 15
phase: "06"
phase_name: cow-alns-reproducibility
canonical_phase_document: docs/implementation/phases/phase-06-cow-alns-reproducibility.md
canonical_phase_review: docs/implementation/reviews/phase-06-review.md
canonical_phase_document_status: CHANGES_REQUIRED
canonical_phase_review_verdict: CHANGES_REQUIRED
implementation_status_observed: NOT_STARTED
phase_acceptance_status_observed: NOT_ACCEPTED
evidence_status_observed: NOT_PRODUCED
entry_gate_status: BLOCKED
source_authority_status: SOURCE_AUTHORITY_CONFLICT_BLOCKED
inventory_observed_at_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_observed_date: 2026-07-29
inventory_branch_observed: codex-implementation
working_tree_inventory_note: PHASE_00_PREREQUISITE_REMEDIATION_IN_PROGRESS_PRESERVED_UNMODIFIED
source_fingerprint_scheme: git_blob_at_inventory_observed_commit
live_inventory_observed_at: 2026-07-29T02:18:57+09:00
live_inventory_evidence_status: OBSERVATION_ONLY_NOT_ACCEPTANCE_EVIDENCE
canonical_test_manifest_id: PHASE06-CANONICAL-REQUIRED-METHODS-V1
canonical_test_manifest_entry_count: 89
canonical_test_manifest_sha256_lf: 1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86
source_sections_and_fingerprints:
  docs/README.md: "current top-level map | 13f1b3b2dea038b8e0b466c138f5f59299413125"
  docs/master-design.md: "§1~4, §10~13, §15.5~15.7, §16~17 | b507a5e7ba0b7e76475bc2d755493e814f4d053a"
  docs/domain-design.md: "current top-level detail §1~3, §10, §12, §15~18 | ace117c380466b733994a1fbb2a95d31e41b3959"
  docs/architecture-design.md: "current top-level placement §1~7, §18~20 | 81495ff448d0e618ab3563e8ff80614fb1028acf"
  docs/2026-07-26-domain-design.md: "user-fixed dated implementation input §2~3, §7~11, §16~18 | 0a02ba4c77a402455e3d80b76969dca28831b1e6"
  docs/2026-07-26-architecture-design.md: "user-fixed dated implementation input §1~3, §5~6 | d51339e251dee1e032e711144dc63d6d07d7323b"
  docs/architecture-domain-implementation-design.md: "§1~3, §9~10, §19~25 | 1199abf2cd52c801ec412bfbcf4729e2b5b29cf0"
  docs/master-design-open-questions.md: "§1~5 and exact Q-* rows | 3fff4c583a54f02dea667e78c8e5187d65ec0e18"
  docs/implementation/README.md: "§0~7 | 8a9cb4a29685a2540bd605c3ac63bb459052b2a1"
  docs/implementation/master-realization-plan.md: "§1~6, Phase 05~07, §8~15 | d7f6be4fff0089204fbdb52f731b2348407f36eb"
  docs/implementation/execution-progress-and-results.md: "§2, §5~9 | 250aa90ae568a6b32ec905fa5ee456d430ff72cf"
  docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md: "§7, §14~16 | 0ea8046142a9e53cc1b9cedb198a1bdae550378b"
  docs/implementation/phases/phase-06-cow-alns-reproducibility.md: "§1~17 | 984b6978981fffa662bcf4cf5b4f8a14c2287c09"
  docs/implementation/reviews/phase-06-review.md: "§1~8 | c68ed93aec2dee9f376096c70e9a8c0b1fa66480"
  docs/implementation/phases/phase-07-independent-verification-final-result.md: "§4, §7, §14~15 | 1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115"
expected_reader:
  - Java의 interface, record, sealed hierarchy와 Maven dependency를 이해한다
  - CVRPTW의 route, time window, capacity와 neighborhood search를 경험했다
  - ro-next의 RPDPTW pair, COW lifecycle, evidence gate와 replay 계약은 처음 접한다
owner_roles:
  implementation: RPDPTW Solver/Search owner
  upstream_state_and_portfolio: Phase 05 Pair/Insertion/Portfolio owner
  upstream_evaluation: Phase 03 Core/Evaluation and Phase 04 Profile/Binding owners
  downstream_verification: Phase 07 Independent Verification owner
  downstream_coordination: Phase 10 Coordinator owner
  architecture: Phase 00 Architecture owner
  review: independent Phase 06 reviewer
  status_authority: total scheduler
planned_evidence:
  - E-P06-COW
  - E-P06-ALNS
  - E-P06-REPLAY
```

> 이 문서는 사람이 Phase 06을 배우고, entry gate가 열린 뒤 구현하고, evidence를
> 만들기 위한 지시서다. 현재 live checkout에는 미승인 `rpdptw-solver` scaffold와
> wrapper가 있지만 Phase 06 production type/test 또는 accepted evidence는 없다.
> Scaffold와 live inventory는 acceptance evidence가 아니다. 따라서 아래 Java
> 이름과 signature는 별도 표시가 없는 한 **제안 후보(PROPOSED INTERNAL)**다.
> 문서에 이름이 있다는 사실은 API 승인, 구현, test pass 또는 Phase acceptance가
> 아니다.

## 1. 이 Phase를 한 문장으로 이해하기

Phase 06은 Phase 05가 만든 불변 seed solution을 직접 훼손하지 않고, 바뀌는
route만 처음 쓸 때 복사하는 COW trial에서 request-pair destroy와 repair를
반복하며, 한 step의 모든 효과를 한 번에 publish하고, 같은 실행 envelope에서
결정 trace와 candidate를 정확히 재현하는 ALNS 실행기를 만드는 단계다.

“더 좋은 route를 찾는다”만으로는 부족하다. 다음 네 가지가 동시에 참이어야 한다.

1. 실패·거절·취소된 trial이 `current`, `stageBest`, `solveBest`를 한 바이트도
   오염시키지 않는다.
2. `completedStep`은 destroy부터 adaptive/acceptance update까지 전부 끝난
   step만 센다.
3. 같은 입력·config·seed·순서·정상 종료에서는 중간 decision trace까지 같다.
4. Phase 07이 search cache나 solver 자기주장을 믿지 않고 후보를 독립 검증할 수
   있는 immutable handoff를 남긴다.

## 2. 큰 그림과 Phase 06이 필요한 이유

### 2.1 CVRPTW solver에서 흔히 하던 방식이 왜 충분하지 않은가

CVRPTW 구현에서는 customer 하나를 route에서 빼고 다른 위치에 넣는 연산,
mutable route list, 한 개의 전역 난수 발생기와 시간 제한을 조합해도 작은
문제에서는 그럴듯한 결과를 얻을 수 있다. 이 프로젝트의 RPDPTW에서는 그 방식이
다음 결함을 숨길 수 있다.

- 실제 pickup과 delivery 중 하나만 제거되거나 서로 다른 차량에 남는다.
- Delivery-only의 logical pickup을 실제 depot visit처럼 다뤄 load, stop,
  service time을 오염시킨다.
- 거절한 후보가 공유 list나 cache를 통해 현재 해 또는 best를 바꾼다.
- Hard-infeasible 후보가 큰 penalty나 simulated annealing으로 살아남는다.
- Thread scheduling, hash iteration 또는 acceptance draw 수 변화가 이후의 모든
  random 선택을 밀어 버린다.
- Watchdog로 잘린 미완료 step을 정상 step이나 `MAX_STEPS_REACHED`로 기록한다.
- 같은 최종 점수만 보고 중간 결정의 비결정성을 놓친다.

Phase 06의 중심은 operator 종류의 화려함보다 **상태 전이의 안전성, 권위 계산의
재사용, 정확한 work accounting과 replay 가능성**이다.

### 2.2 Canonical 15 Phase 중 위치

이 저장소의 canonical 구현 단계는 **Phase 00~14, 총 15개**다.

| Phase | 주제 | Phase 06과의 관계 |
|---:|---|---|
| 00 | Build와 architecture 뼈대 | `rpdptw-solver`, test fixture, architecture rule과 wrapper를 제공해야 한다 |
| 01 | 내부 표준 입력과 정규화 | 숫자·시간·service·compatibility 의미를 확정한다 |
| 02 | 이동 자료 준비와 immutable problem | `ProblemInstance`와 complete directed `PreparedTravel`을 만든다 |
| 03 | Route propagation과 evaluation kernel | Full route/solution 평가와 business comparator 권위를 제공해야 한다 |
| 04 | 재사용 capability와 customer profile | Immutable `BoundProfile`과 `SolvePlan`을 bind한다 |
| 05 | Pair insertion과 초기 후보군 | Pure insertion evaluator와 최대 8개 `SeedPortfolio` member를 생산한다 |
| **06** | **COW ALNS와 reproducibility** | **이 가이드의 소유 범위** |
| 07 | 독립 검증과 최종 결과 | Phase 06 candidate claim을 cache 없이 재검산한다 |
| 08 | Application port와 local 실행 | 검증된 ALNS-only local end-to-end를 조립한다 |
| 09 | DB 없는 object storage | Immutable artifact와 CAS 상태를 저장한다 |
| 10 | Provider-neutral coordinator | Round/worker completeness, retry와 outer termination을 소유한다 |
| 11 | AWS reference distribution | S3, Step Functions, Lambda mapping을 붙인다 |
| 12 | Provider substitution | 승인된 storage/workflow/compute 축만 교체한다 |
| 13 | Optional hybrid route selection | Phase 14A receipt와 `C-17` 승인 뒤에만 열리는 optional branch다 |
| 14 | 14A ALNS benchmark / 14B official cutover | 14A evidence gate와 14B production authority gate를 분리한다 |

현재 ALNS-first critical path는 다음과 같다.

```text
00 → 01 → 02 → 03 → 04 → 05 → [06] → 07 → 08 → 14A
```

Phase 13은 Phase 06의 선행조건도 필수 consumer도 아니다. Phase 14A가 유효한
`ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`를 발행하고 `C-17`의 scope, backend,
native, license/SBOM, security, operations, cost, fallback과 rollback 승인이
별도로 끝난 뒤에만 optional로 시작한다. Phase 14B production authority가 없다는
이유로 Phase 06이나 14A를 막지 않는다. 반대로 Phase 06 통과나 14A receipt가
Phase 13 또는 14B를 자동 승인하지 않는다.

### 2.3 Producer와 consumer 계약

```text
Phase 03/04
  ProblemInstance + PreparedTravel
  BoundProfile + SolvePlan
  full evaluation + business comparator
                 ↓
Phase 05
  immutable SearchSnapshot
  pure PairInsertionEvaluator
  ordered SeedPortfolio
                 ↓
Phase 06
  COW trial → completed ALNS step
  phase-1 stable champion
  worker committed candidate
  exact termination + replay/evidence
                 ↓
Phase 07
  independent cache-free candidate verification
                 ↓
Phase 08 / 10 / 14A
  local execution / outer coordination / benchmark qualification
```

| 경계 | Producer가 보장할 것 | Consumer가 하면 안 되는 것 |
|---|---|---|
| Phase 03/04 → 06 | 같은 authority에서 full solution evaluation, business equality, stage guard와 comparator 의미 | Route delta를 임의 합산하거나 Big-M scalar로 대체 |
| Phase 05 → 06 | Stable pair/bank snapshot, pure insertion evaluator, canonical member order, cache-free validation evidence | Seed를 직접 mutate하거나 missing member를 임시 해로 채움 |
| Phase 06 → 07 | Immutable candidate payload, authority content digest, declared claims, exact termination, replay/evidence와 accepted review receipt | Verifier `PASS`, final outcome 또는 publication eligibility를 미리 생성 |
| Phase 06 → 10 | Logical worker/run identity, requested/completed work, exact termination, stable candidate/replay | 일부 worker 결과나 completion-first winner를 round champion으로 간주 |
| Phase 06 → 14A | Accepted ALNS candidate/replay와 측정 schema | Test-only 수치를 official threshold로 승격하거나 MIP를 correctness oracle로 사용 |

## 3. Source authority, 읽기 순서와 fingerprint

### 3.1 충돌 해소 순서

현재는 한 줄짜리 authority 순서를 바로 적용할 수 없다. 다음 두 선언이 서로
다른 Domain/Architecture path를 가리킨다.

- [현재 최상위 문서 지도](../../../README.md)는 `docs/domain-design.md`와
  `docs/architecture-design.md`를 current top-level detail로 지목한다.
- [Implementation README §1·§3](../../README.md#3-source-authority)와
  [Master Realization Plan §2](../../master-realization-plan.md#2-입력-권위와-충돌-규칙)은
  사용자 고정 implementation input으로
  `docs/2026-07-26-domain-design.md`와
  `docs/2026-07-26-architecture-design.md`를 지목한다. 두 날짜 문서는 다시 plain
  path를 supersede한다고 선언한다.

따라서 Phase 06 entry에는 다음 gate가 추가된다.

```text
SOURCE_AUTHORITY_CONFLICT — BLOCKED

Master/Domain/Architecture owner approval
  + chosen canonical path pair
  + supersedes direction
  + current/plain ↔ dated semantic/package/test diff
  + affected WP/test/evidence list
  + approval record digest
→ SOURCE_AUTHORITY_DECISION_RECEIPT
```

Target 단독으로 plain 또는 dated path를 조용히 선택하지 않는다.

- Plain pair를 승인하면 metadata, 읽기 순서, traceability와 구현 evidence의
  canonical path를 같은 변경 단위에서 plain pair로 통일한다.
- Dated pair를 승인하면 `docs/README.md`, Canonical Master의 related-document map과
  같은 변경 단위의 supersedes/authority record를 요구한다.
- Receipt가 없으면 semantic fixture/oracle 설계까지만 가능하고 production source,
  accepted POM identity, Phase 06 evidence 생성은 시작하지 않는다.

`Q-*` 상태 자체는 [질문 등록부](../../../master-design-open-questions.md)의
`Q-INFRA-01 RESOLVED`, `RESOLVED 26 / OPEN 1 / DEFERRED 1`을 적용한다. 이는 source
path conflict를 해결하지도, Phase 06 solver에 AWS dependency를 넣으라는 뜻도
아니다. [2026-07-26 Master 초안](../../../2026-07-26-master-design.md)은
`SUPERSEDED` historical cross-check로만 쓴다.

### 3.2 처음 구현하는 사람의 정확한 읽기 순서

다음 순서는 “문서를 많이 읽기”가 아니라, 앞 문서의 의미를 뒤 문서의 구현
결정과 연결하기 위한 순서다.

1. [현재 최상위 문서 지도](../../../README.md)의 current entrypoint와 `REVIEW`
   지위를 읽고, plain Domain/Architecture가 current map에 있다는 사실을 기록한다.
2. [Canonical Master §1.4~1.5](../../../master-design.md#14-관련-문서의-역할)에서
   plain Domain/Architecture 관계와 승인 전 conflict rule을 읽는다.
3. [Canonical Master §4.5~4.7](../../../master-design.md#45-상태와-산출물의-생명주기)에서
   lifecycle, invariant와 roadmap의 전체 위치를 읽는다.
4. [Canonical Master §10~13](../../../master-design.md#10-search-solution과-final-result)에서
   search/result 분리, portfolio, ALNS step, COW, termination과 strong
   reproducibility를 읽는다.
5. [Current Domain §10](../../../domain-design.md#10-stable-solution-request-bank와-cow)과
   [dated Domain §8](../../../2026-07-26-domain-design.md#8-stable-solution-request-bank와-cow)를
   함께 읽고 pair/COW/search type의 semantic diff를 만든다.
6. [Current Architecture §5~§7](../../../architecture-design.md#5-recommended-maven-multi-module-tree)과
   [dated Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계)를
   함께 읽고 module/package/POM/test lifecycle diff를 만든다.
7. `SOURCE_AUTHORITY_DECISION_RECEIPT`가 있으면 선택된 pair의 exact section을
   implementation source로 pin한다. 없으면 계속 `BLOCKED`다.
8. [Integrated design §9~10](../../../architecture-domain-implementation-design.md#9-phase-5--stable-solution-insertion과-initial-portfolio)에서
   Phase 05와 Phase 06의 소유 경계를 읽는다.
9. [Question register의 `Q-ALG-01`](../../../master-design-open-questions.md#q-alg-01),
   [`Q-ALG-02`](../../../master-design-open-questions.md#q-alg-02),
   [`Q-BENCH-02`](../../../master-design-open-questions.md#q-bench-02),
   [`Q-VAR-01`](../../../master-design-open-questions.md#q-var-01)을 읽는다.
10. [Master Realization Plan의 Phase 06](../../master-realization-plan.md#phase-06--복사-후-변경-방식의-alns)과
   [evidence 규칙](../../master-realization-plan.md#9-evidence-bundle-규칙)을 읽는다.
11. [Execution Progress §8](../../execution-progress-and-results.md#8-현재-blockers-open-gates와-남은-이슈)과
    [live registry §10.1](../../execution-progress-and-results.md#101-phase-00-prerequisite-remediation)에서
    evidence 제출, review verdict와 acceptance receipt를 구분한다.
12. [Canonical Phase 06](../../phases/phase-06-cow-alns-reproducibility.md)의 전체
   §1~17을 읽는다.
13. [Phase 06 review](../../reviews/phase-06-review.md)의 finding 11개, 특히 residual
    `F-P06-003`과 `F-P06-004`를 읽는다.
14. [Phase 05 §15.2](../../phases/phase-05-pair-insertion-initial-portfolio.md#152-next--actual-but-unaccepted-phase-06)와
    [Phase 07 §15.1](../../phases/phase-07-independent-verification-final-result.md#151-previous--actual-but-unaccepted-phase-06)의
    handoff 양 끝을 대조한다.
15. §5의 implementation-entry re-snapshot을 실행하고 HEAD baseline, live
    observation과 accepted implementation receipt를 별도 identity로 봉인한다.

읽은 뒤 다음 네 문장을 자신의 말로 설명하지 못하면 구현을 시작하지 않는다.

- Stable state와 좋은 품질의 해는 같은 말이 아니다.
- `REJECTED`와 `INVALID_CANDIDATE`는 completed-step 의미가 다르다.
- Seed가 같은 것만으로 strong reproducibility가 되지 않는다.
- Candidate가 stable하다는 것과 Phase 07 verifier가 `PASS`했다는 것은 다르다.

### 3.3 검증 가능한 source fingerprint

Fingerprint는 서로 다른 네 identity layer를 섞지 않는다.

| Layer | 무엇을 고정하는가 | Phase 06에서의 지위 |
|---|---|---|
| Current top-level map | `docs/README.md`가 지목한 plain Domain/Architecture | Authority conflict의 한쪽 입력 |
| User-fixed dated input | Implementation README/Plan이 지목한 dated Domain/Architecture와 canonical 5문서 | Authority conflict의 다른 입력 |
| Implementation baseline | `inventory_observed_at_commit`의 tracked Git blob | 재현 가능한 역사 baseline, acceptance 아님 |
| Live snapshot | Timestamped worktree POM/source/progress SHA-256 | Drift 관찰뿐, acceptance evidence 아님 |

아래 Git blob은 commit `7cc890ee1d0805df5ae14b633127fade4f978639`에서
고정했다.

| Source | 역할/직접 읽을 section | Git blob |
|---|---|---|
| [Current docs map](../../../README.md) | Current top-level entry와 hierarchy | `13f1b3b2dea038b8e0b466c138f5f59299413125` |
| [Canonical Master](../../../master-design.md) | §1~4, §10~13, §15.5~15.7, §16~17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Current Domain](../../../domain-design.md) | §1~3, §10, §12, §15~18 | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Current Architecture](../../../architecture-design.md) | §1~7, §18~20 | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Dated Domain input](../../../2026-07-26-domain-design.md) | §2~3, §7~11, §16~18 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Dated Architecture input](../../../2026-07-26-architecture-design.md) | §1~3, §5~6 | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | §1~3, §9~10, §19~25 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | §1~5, exact `Q-*` rows | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README](../../README.md) | §0~7 | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | §1~6, Phase 05~07, §8~15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress baseline](../../execution-progress-and-results.md) | §2, §5~9 | `250aa90ae568a6b32ec905fa5ee456d430ff72cf` |
| [Phase 05](../../phases/phase-05-pair-insertion-initial-portfolio.md) | §7, §14~16 | `0ea8046142a9e53cc1b9cedb198a1bdae550378b` |
| [Canonical Phase 06](../../phases/phase-06-cow-alns-reproducibility.md) | 전체 §1~17 | `984b6978981fffa662bcf4cf5b4f8a14c2287c09` |
| [Phase 06 review](../../reviews/phase-06-review.md) | 전체 §1~§8 | `c68ed93aec2dee9f376096c70e9a8c0b1fa66480` |
| [Phase 07](../../phases/phase-07-independent-verification-final-result.md) | §4, §7, §14~15 | `1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115` |

재검증 예:

```bash
git rev-parse HEAD
git rev-parse HEAD:docs/README.md
git rev-parse HEAD:docs/master-design.md
git rev-parse HEAD:docs/domain-design.md
git rev-parse HEAD:docs/architecture-design.md
git rev-parse HEAD:docs/2026-07-26-domain-design.md
git rev-parse HEAD:docs/2026-07-26-architecture-design.md
git rev-parse HEAD:docs/implementation/phases/phase-06-cow-alns-reproducibility.md
git rev-parse HEAD:docs/implementation/reviews/phase-06-review.md
```

Hash가 다르면 다음을 기록하고 사람 checkpoint로 돌아간다.

1. 바뀐 source path와 heading
2. 이전/새 요구의 semantic diff
3. 영향받는 WP, type, state transition과 test
4. OPEN/GATED/deferred 또는 owner 상태 변화
5. 새 approval/evidence가 필요한지

Source path가 같더라도 live bytes는 별도 SHA-256으로 봉인한다. 인접 Phase
문서끼리 whole-file hash를 서로 metadata에 넣어 순환 drift를 만들지 않는다.
실제 implementation entry에서는 선택된 source decision receipt와 accepted
artifact/evidence/receipt content digest를 pin한다.

## 4. RPDPTW와 ALNS primer

### 4.1 핵심 용어

| 용어 | 이 Phase에서의 정확한 의미 | 흔한 오해 |
|---|---|---|
| `Request` | Pickup/delivery 의미를 함께 소유한 원자 운송 업무 | Node 또는 visit 하나 |
| `Visit` | Route 순서에 나타난 physical service occurrence | Request 전체 |
| `SearchRequestBank` | 현재 route에 배정되지 않은 request ID membership | Final `UNASSIGNED`, DB 또는 failure log |
| `SearchSnapshot` | Immutable routes, bank, authority, evaluation과 fingerprint를 가진 stable search state | Mutable working solution |
| `TrialDraft` | 한 step 안에서만 mutable한 changed-route COW 작업 공간 | Consumer에게 넘겨도 되는 candidate |
| `CompletedTrial` | Structure와 full evaluation을 마친 commit 직전 값 | 이미 published current |
| `current` | 다음 neighborhood의 기준이 되는 immutable snapshot slot | 항상 가장 좋은 해 |
| `stageBest` | 현재 SolvePlan stage에서 가장 좋은 eligible snapshot | Acceptance로 나빠질 수 있는 current |
| `solveBest` | Solve 전체에서 가장 좋은 eligible snapshot | 같은 Java reference를 세 slot이 공유한 것 |
| Destroy | Ordered unique request 제거 proposal | Route를 직접 mutate하는 operator |
| Repair | Bank request를 exact insertion evaluator로 다시 넣는 절차 | 모든 request를 반드시 넣는 절차 |
| Acceptance | Hard/stage guard를 통과한 candidate를 current로 받을지 결정 | Infeasible 해를 penalty로 허용하는 장치 |
| Adaptive learning | Completed outcome에 따라 operator 선택 state를 새 immutable 값으로 만드는 과정 | 전역 mutable counter |
| Completed step | Destroy부터 learning/acceptance state update까지 모두 끝나 한 번 publish된 전이 | Loop 진입 횟수 또는 operator 호출 수 |
| Strong replay | 고정 envelope에서 step별 결정과 candidate bytes/digest가 같은 것 | Seed나 최종 점수만 같은 것 |
| Evidence | 명령·환경·fixture·oracle·result·digest가 봉인된 검증 산출물 | Console의 `BUILD SUCCESS` 한 줄 |

### 4.2 Delivery-only와 real pickup-delivery

Real pickup-delivery request `R1`은 두 physical visit을 가진다.

```text
R1.pickup(+demand) → ... → R1.delivery(-demand)
```

Delivery-only request `R2`는 출발 전 initial load를 소유하고 delivery visit만
physical propagation에 나타난다.

```text
initialLoad += R2.demand
... → R2.delivery(-demand)
```

Destroy의 단위는 두 경우 모두 `RequestId`다. Delivery-only의 logical pickup을
가짜 depot visit으로 만들지 않고, real pair의 pickup만 제거한 뒤 delivery를 남기지
않는다.

### 4.3 Stable identity와 route-bank XOR

모든 stable snapshot에서 input request `r`은 다음 둘 중 정확히 하나다.

```text
routeOwnerCount(r) + bankMembershipCount(r) == 1
```

Route owner가 있으면 필요한 physical visit이 정확히 한 번 있고, real pair는 같은
concrete vehicle에서 pickup이 delivery보다 앞선다. Bank에 있으면 어느 route에도
그 request의 physical visit이 없어야 한다.

`PARTIAL_REINSERTION`과 `NO_FEASIBLE_INSERTION`은 결함이 아니다. 넣지 못한
request가 bank에 완전하게 남고 나머지 route가 hard-feasible하면 full-evaluate할
수 있는 정상 trial이다. 반면 partial pair, duplicate, split vehicle, route+bank
중복은 낮은 점수가 아니라 implementation defect다.

### 4.4 Identity와 equality를 분리한다

세 snapshot slot은 같은 solution content를 가질 수 있지만 top-level handle은
서로 달라야 한다.

```text
currentRef    != stageBestRef != solveBestRef   // reference/slot identity
current.bytes == stageBest.bytes                // content equality는 가능
```

Business objective equality와 deterministic tie도 분리한다.

- Business comparator가 `EQUAL`이면 개선이 아니다.
- Stable tie는 후보 선택이나 serialization 순서를 결정할 수 있다.
- Tie key가 business equality를 `STRICTLY_BETTER`로 바꾸면 안 된다.
- Completion order, clock, object address와 raw external ID는 tie 입력이 아니다.

### 4.5 COW lifecycle

```text
immutable SearchSnapshot F0
  ├─ immutable route R1
  ├─ immutable route R2
  ├─ immutable route R3
  └─ immutable bank B0

open TrialDraft
  ├─ R1/R2/R3를 read-only로 참조
  └─ independent trial-owned bank B1

first write to R2
  ├─ R2 → R2' exactly once
  ├─ copiedRouteIds += R2
  └─ affected cache/fingerprint invalid

full structure/evaluation
  └─ CompletedTrial C1

accept + every next-state component succeeds
  └─ immutable next AlnsSearchState publish exactly once

reject/invalid/fault/cancel/watchdog
  └─ discard draft; F0/current/stageBest/solveBest unchanged
```

“Copy-on-write”는 route list를 얕게 복사하는 이름이 아니다. 공유되는 모든 값은
transitively immutable이어야 하고, 쓰기 가능한 list/map, cache와 bank backing은
parent와 공유할 수 없다.

### 4.6 반드시 유지할 불변조건

1. 모든 stable snapshot은 complete pair와 route-bank XOR을 만족한다.
2. `current`, `stageBest`, `solveBest`의 top-level handle은 pairwise distinct다.
3. Unchanged route는 immutable일 때만 공유한다.
4. 한 trial의 한 route는 첫 write 때 정확히 한 번 복사한다.
5. Trial bank는 base bank와 mutable alias를 공유하지 않는다.
6. Reject, invalid, fault와 pre-publication interruption은 parent와 세 slot을
   byte/fingerprint 기준으로 보존한다.
7. 모든 next value를 local에서 완성한 후 `AlnsSearchState`를 한 번만 publish한다.
8. `REJECTED` feasible trial은 completed step이며 configured reject update를
   한 번 적용한다.
9. `INVALID_CANDIDATE`, `INTERRUPTED`와 실패한 step은 progress, learning,
   temperature와 RNG lineage를 전진시키지 않는다.
10. Structural/hard feasibility와 SolvePlan stage guard가 acceptance보다 먼저다.
11. Guard 실패 candidate는 current, stageBest, solveBest 어느 slot도 바꾸지 않는다.
12. Non-improving acceptance는 current만 바꿀 수 있고 best를 악화시키지 않는다.
13. Full evaluation과 comparator는 Phase 03/04 authority를 재사용한다.
14. Global/static/thread-local RNG와 unordered iteration을 사용하지 않는다.
15. `AttemptId`, thread, clock, elapsed와 completion order는 semantic seed/trace에
    들어가지 않는다.
16. Watchdog/cancel/resource/platform/failure를 정상 종료로 이름 바꾸지 않는다.
17. Phase 06 candidate는 Phase 07 `PASS`나 publishable result가 아니다.
18. Test-only, legacy와 experiment 값은 official default가 아니다.

## 5. 실제 inventory: 현재와 목표

### 5.1 재현 가능한 read-only inventory 명령

```bash
git rev-parse HEAD
git branch --show-current
git status --short
rg --files -g 'pom.xml' -g 'mvnw' -g 'mvnw.cmd' -g '.mvn/**' | sort
rg --files -g '*.java' | sort
rg --files rpdptw/solver -g '*.java' | sort
rg --files rpdptw/solver -g '*.java' | rg -v 'package-info\.java$'
rg --files -g '*Test.java' -g '*IT.java' | sort
sed -n '1,240p' pom.xml
sed -n '1,240p' rpdptw/solver/pom.xml
sed -n '1,120p' .mvn/wrapper/maven-wrapper.properties
sed -n '1,470p' docs/implementation/execution-progress-and-results.md
shasum -a 256 pom.xml rpdptw/solver/pom.xml \
  .mvn/wrapper/maven-wrapper.properties \
  docs/implementation/execution-progress-and-results.md
```

이 명령은 root legacy path만 보던 과거 `find src/main/java src/test/java`와 달리
`rpdptw/**`, `build/**`, `legacy/**`를 모두 열거한다. 출력은 세 시점을 분리한다.

1. **HEAD snapshot:** commit
   `7cc890ee1d0805df5ae14b633127fade4f978639`, branch
   `codex-implementation`. 재현 가능한 역사 baseline이며 acceptance commit이 아니다.
2. **Human-review observation:** `2026-07-29T01:40:21+09:00`. Phase 00 scaffold가
   나타났지만 Phase 06 source/test와 acceptance receipt는 없었다.
3. **Correction live observation:** `2026-07-29T02:18:57+09:00`. 아래 표의
   worktree bytes를 관찰했다. 공유 worktree이므로 영구 사실이나 acceptance
   evidence로 승격하지 않는다.

실제 implementation entry에서는 위 명령을 다시 실행해 timestamp, HEAD, branch,
status, POM/wrapper/progress SHA-256과 source/test count를 새
`IMPLEMENTATION_ENTRY_LIVE_SNAPSHOT`으로 봉인한다. 이 snapshot은 accepted
predecessor receipt를 대신하지 않는다.

### 5.2 존재·placeholder·부재 구분

| 항목 | HEAD snapshot | Correction live observation | Phase 06 판정 |
|---|---|---|---|
| Root/POM inventory | 단일 root project | Executable wrapper와 13개 `pom.xml`; root `ro-next-parent` → `rpdptw/build/legacy` reactor | 미승인 Phase 00 worktree, Phase 06 entry evidence 아님 |
| Wrapper | 없음 | `./mvnw` executable; pinned Maven 3.9.14 URL/SHA | 존재는 확인했지만 accepted wrapper receipt 없음 |
| Solver POM | 없음 | Compile `rpdptw-core` dependency만 있음 | JUnit/test-fixture/Failsafe closure가 없어 WP-06.0 BLOCKED |
| Root test plugins | Root legacy Surefire | Surefire 3.5.4와 `failIfNoTests=false`; Failsafe property/plugin/execution 없음 | `*IT`가 실행된다는 보장 없음 |
| Target Java | 0 | `rpdptw/**` Java 23개, 모두 `package-info.java`; solver 4개, non-`package-info` 0 | Phase 06 production/test 0 |
| Test source | Legacy 1개 | 전체 `*Test.java`/`*IT.java` 14개; Phase 00 architecture 8 + fixture 1, legacy 5, Phase 06 0 | Live test count를 Phase 06 evidence로 재사용 금지 |
| Legacy engine | Root synthetic engine | `legacy/gcp-placeholder` 아래 synthetic `AlnsBatchEngine` 보존 | RPDPTW ALNS evidence 아님 |
| Progress | Phase 00 not started baseline | `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`; prior bundle rejected, acceptance receipt `NOT_PRODUCED`, Phase 01 handoff `BLOCKED` | Evidence 제출/34 tests를 acceptance로 승격 금지 |
| Phase 06 evidence | 없음 | `E-P06-COW/ALNS/REPLAY` 없음 | `NOT_PRODUCED` |
| Accepted implementation | `0/15` | `0/15`; Phase 00 receipt 없음 | Phase 06 entry `BLOCKED` |

Correction live observation의 byte identity:

| File | SHA-256 | 해석 |
|---|---|---|
| `pom.xml` | `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c` | Live unaccepted parent/reactor |
| `rpdptw/solver/pom.xml` | `ac67af2ac73ab6a5d9f9418f4a5b54ed538c775fbad75a627f348885e3b0ad2e` | Compile-only solver scaffold |
| `.mvn/wrapper/maven-wrapper.properties` | `7613cd8a2f64216deb6c8f8443f2b339f2305d9b30fc3a4ea3088820a4788871` | Live wrapper policy, acceptance 아님 |
| `execution-progress-and-results.md` | `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` | Live scheduler registry bytes |

### 5.3 현재 실행 가능한 Maven 명령과 한계

HEAD baseline의 legacy characterization과 correction live reactor는 서로 다른
build다. 어느 쪽의 success도 Phase 06 evidence가 아니다. Correction 시점에는
wrapper가 실제 존재하지만 solver POM에 JUnit test dependency가 없고 parent에
Failsafe execution도 없으며 Phase 06 test source가 0개다. 따라서 다음은 지금
실행해 green을 주장할 명령이 아니라 WP-06.0에서 닫아야 할 future gate다.

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/solver help:effective-pom

./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/solver -am clean verify
```

Effective POM과 selected command의 승인 조건은 §11.5에 정의한다. 특히 wrapper가
있다는 사실, root/solver `verify` exit 0, `failIfNoTests=false`, console summary,
ignored/stale XML 또는 현재 `AlnsBatchEngine`의 deterministic 출력은
`E-P06-COW`, `E-P06-ALNS`, `E-P06-REPLAY`로 재사용하지 않는다.

## 6. Scope, 결정 상태와 사람 승인

### 6.1 Phase 06 scope

- Phase 05 immutable seed를 Phase-1 screen과 ALNS warm start로 소비
- Changed-route first-write COW, independent bank, invalidation, freeze/discard
- Approved central pair-removal editor를 통한 atomic destroy 적용
- Phase 05 pure `PairInsertionEvaluator`를 통한 repair
- Explicit bounded improvement 또는 explicit `NONE`
- Ordered operator registry, adaptive selection, reward와 immutable learning state
- Explicit Hill Climbing/SA acceptance와 stage guard
- Completed-step transaction과 exact normal/exceptional termination
- Phase-1 all-candidate screen과 completion-order-independent champion
- Purpose/operator namespaced seed stream과 stable reduction
- Canonical decision trace, replay manifest와 independent full-copy oracle
- Trace redaction, copy/work counter, architecture/fault/corruption/replay evidence
- Candidate/evidence/replay의 Phase 07/10/14A handoff

### 6.2 Non-scope

- Phase 03 propagation/evaluation/comparator 재구현
- Phase 04 customer profile/preset 재해석
- Phase 05 insertion 위치 의미와 initial construction policy 재구현
- Phase 07 candidate/result verifier, final outcome, audit와 publication
- Phase 10 declared-worker fan-out/fan-in, outer round champion과 retry/CAS
- Phase 13 route pool/MIP/OR-Tools/vendor dependency
- Apply/undo, automatic state switch와 근거 없는 micro-optimization
- Multi-trip/rotation, MDVRP/OVRP/SDVRP와 dynamic routing
- Official step/worker/round/watchdog/quality/performance 수치 결정
- Public/wire schema, canonical encoding과 hash algorithm의 최종 승인
- Cloud SDK, HTTP, storage, credential와 tenant authorization 구현

### 6.3 확정, proposed, open, gated와 deferred

| 항목 | 상태 | 구현자 행동 |
|---|---|---|
| Pair, route-bank XOR, hard-before-acceptance | FIXED | 약화하지 않는다 |
| COW 기본 전략 | `RESOLVED — KEEP_COW` | Apply/undo를 구현하지 않는다 |
| 최대 8개 portfolio와 Phase-1 screen | `Q-ALG-01 RESOLVED` | Available member 전부 exact explicit budget으로 screen한다 |
| Solver→core dependency | FIXED ARCHITECTURE | Verification/application/provider/vendor를 참조하지 않는다 |
| Plain vs dated Domain/Architecture path | `SOURCE_AUTHORITY_CONFLICT — BLOCKED` | Owner decision receipt와 semantic/package/test diff 전 한쪽을 canonical이라 주장하지 않는다 |
| Java type/package 이름 | PROPOSED INTERNAL | 의미를 보존하며 cross-Phase review에서 확정한다 |
| State publication 방식 | PROPOSED INTERNAL CONTRACT, REVIEW REQUIRED | Pure step derivation + engine-owned one-shot state slot 후보를 §8.3/§9에 따라 review하고 승인 전 구현하지 않는다 |
| Phase 06→07 handoff schema owner | CROSS-PHASE CONTRACT GATE | Core-owned value 또는 versioned artifact 중 하나를 owner receipt로 승인한다. Default 없음 |
| Seed mixing/canonical encoding | PROPOSED/REVIEW_REQUIRED | Version 승인과 independent oracle 전 external compatibility를 주장하지 않는다 |
| Invalid continuation | PROPOSED SAFE BASELINE | Fail-closed; hidden retry 금지 |
| Central pair-removal editor owner | RESIDUAL CROSS-PHASE BLOCKER | Phase 05/06/Architecture 공동 승인 전 구현하지 않는다 |
| Full solution evaluator/comparator/tie seam | RESIDUAL CROSS-PHASE BLOCKER | Phase 03~06 승인 전 ad hoc 합산/API를 만들지 않는다 |
| Official ALNS numeric values | OPEN — EXPERIMENT_REQUIRED | Test/experiment 값만 explicit ID로 사용한다 |
| Phase 13 route pool/MIP | `C-17 GATED TARGET` | 14A receipt와 별도 승인 전 source/dependency를 추가하지 않는다 |
| `Q-VAR-01` | DEFERRED | 질문·구현·선반영하지 않는다 |
| Multi-trip/rotation | DEFERRED FEATURE | Single-trip invariant를 유지한다 |
| Phase 14B production authority | NOT GRANTED | 14A benchmark와 구분하고 cutover를 주장하지 않는다 |

### 6.4 Entry gate와 마지막 안전 지점

현재 implementation entry는 `BLOCKED`다.

| Gate | 필요한 evidence | 현재 판정 |
|---|---|---|
| Source authority | Canonical path/supersedes approval, plain↔dated semantic/package/test diff와 receipt | SOURCE AUTHORITY CONFLICT |
| Phase 00 accepted | Target reactor, wrapper, architecture rule, `E-P00-ARCH`, receipt | BLOCKED |
| Phase 03 accepted | Full evaluation/comparator contract와 `E-P03-*` | BLOCKED |
| Phase 04 accepted | Immutable `BoundProfile`/`SolvePlan`, `E-P04-*` | BLOCKED |
| Phase 05 accepted | `Phase05SeedPortfolioHandoff`, pure evaluator, `E-P05-*` | BLOCKED |
| Accepted effective POM | Solver JUnit/test-fixture test scope, pinned Surefire/Failsafe와 `*IT`, selected-test fail-closed behavior | BLOCKED |
| Central pair-removal editor owner | Module/API/test/evidence owner 공동 승인 | CONTRACT GATE |
| Solution evaluator/comparator seam | Objective equality/context tie/API/fingerprint 승인 | CONTRACT GATE |
| State publication contract | Pure derivation, engine state-slot owner, atomic publish/failure oracle와 memory boundary 승인 | CONTRACT GATE |
| Phase 06→07 handoff schema | Owner/path, producer mapper, independent consumer decoder, version/order/digest/unknown-field policy 승인 | CONTRACT GATE |
| Explicit Phase 06 config | 모든 operator/acceptance/termination/seed/tie/numeric 값 | REVIEW REQUIRED |
| Seed/replay schema | Derivation/canonicalization version과 independent oracle | REVIEW REQUIRED |
| Scheduler/owners | Exact task ID, implementer와 independent reviewer | OWNER GATE |

**현재 마지막 안전 지점**은 이 가이드, canonical Phase 06의 semantic contract,
독립 fixture/oracle 설계와 read-only inventory다. 위 gate가 열리기 전에 Java
scaffold, placeholder evaluator, 임시 seed, default temperature 또는 중앙 editor를
추가하지 않는다.

사람 checkpoint에서 최소 다음 질문에 “예”라고 답해야 WP-06.1로 간다.

- `SOURCE_AUTHORITY_DECISION_RECEIPT`와 선택 source의 exact bytes가 같은가?
- Accepted Phase 05 handoff의 candidate bytes/digest와 manifest가 같은가?
- Accepted effective POM이 JUnit, Surefire/Failsafe와 isolated-repository command를 닫는가?
- Central removal editor의 소유 module과 evidence key가 한 곳으로 승인됐는가?
- Full solution equality와 stable tie가 다른 API 의미로 분리됐는가?
- Engine-owned publication slot과 Phase 06→07 handoff schema owner가 승인됐는가?
- Omitted config가 실제로 bind failure가 되는가?
- Scheduler가 implementer/reviewer와 rollback point를 기록했는가?

## 7. 학습 경로: 개념에서 통합까지

### 7.1 1단계 — 개념 이해

먼저 종이에 다음 상태를 그린다.

```text
current     = immutable S0
stageBest   = distinct handle to content S0
solveBest   = distinct handle to content S0

trial(S0)
  destroy R2
  repair R2 into another position
  full evaluate
  guard
  accept or reject
```

완료 신호:

- `current`가 best와 다를 수 있는 이유를 설명한다.
- Reject가 completed step인 이유를 설명한다.
- Invalid가 reject와 달리 learning/temperature를 전진시키지 않는 이유를 설명한다.
- 같은 content와 같은 handle이 다른 개념임을 설명한다.

자문 질문:

- Candidate가 stage guard를 실패했지만 더 짧은 distance라면 best가 될 수 있는가?
- Acceptance updater가 예외를 던졌다면 candidate만 먼저 commit해도 되는가?
- Watchdog가 publication 직후 도착했다면 이미 완료된 step을 취소해야 하는가?

정답은 각각 “아니오”, “아니오”, “아니오—다음 boundary에서 exceptional
termination으로 처리”다.

### 7.2 2단계 — 작은 손 탐색

Test-only 문제를 손으로 만든다.

```text
Vehicle: V01
Requests: R01, R02, R03, R04
Initial route: R01 pair, R02 pair
Initial bank: R03, R04
Stable order: R01 < R02 < R03 < R04
Comparator:
  totalUnassignedCount
  → totalDistance
  → stable tie only after business equality
```

세 completed step 각각에 대해 다음 표를 손으로 채운다.

| 필드 | Step 0 | Step 1 | Step 2 |
|---|---|---|---|
| Derived purpose seed | literal expected | literal expected | literal expected |
| Selected destroy/repair | hand value | hand value | hand value |
| Ordered removal proposal | hand list | hand list | hand list |
| Repair attempts | ordered positions | ordered positions | ordered positions |
| Guard relation | pass/fail | pass/fail | pass/fail |
| Acceptance relation/draw | exact | exact | exact |
| Outcome | exact enum | exact enum | exact enum |
| current/stageBest/solveBest fingerprint | literal | literal | literal |
| Learning/acceptance fingerprint | literal | literal | literal |
| Trace prefix digest | literal | literal | literal |

완료 신호:

- Production 코드를 호출하지 않고 expected row를 만들 수 있다.
- COW 대신 full copy를 사용한 reference state transition을 설명할 수 있다.
- 목적별 draw 수 하나를 늘려도 다른 목적 stream이 바뀌지 않아야 함을 확인한다.

### 7.3 3단계 — 실제 변경

Gate가 열린 뒤 다음 순서로만 변경한다.

```text
COW primitive
→ destroy/repair composition
→ transactional completed step
→ adaptive/acceptance/RNG
→ phase-1 screen/termination
→ replay/corruption
→ architecture/security/performance evidence
```

각 단계는 바로 다음 단계가 요구하는 최소 immutable contract만 노출한다. 처음부터
모든 operator family, SA, parallel screen과 artifact serialization을 한 class에
넣지 않는다.

완료 신호:

- 각 WP의 selected test가 fail-closed로 green이다.
- Production method를 expected oracle로 재사용하지 않는다.
- 하나의 WP를 rollback해도 이전 green artifact가 남는다.

### 7.4 4단계 — 통합과 handoff

마지막에는 Phase 05 accepted seed에서 시작해 Phase 03/04 evaluator를 호출하고,
Phase 07이 solver dependency 없이 소비할 수 있는 projection을 만든다. 단,
“solver-independent”라는 형용사만으로 owner가 생기지 않는다. WP-06.0의
`CROSS_PHASE_HANDOFF_SCHEMA_DECISION_RECEIPT`가 다음 둘 중 정확히 하나를
승인해야 한다.

1. `rpdptw-core`가 최소 immutable candidate vocabulary를 소유하고, Phase 06
   solver mapper가 이를 만들며 Phase 07 verifier가 소비한다.
2. Java module dependency와 독립된 versioned immutable artifact schema를 Phase 06
   producer가 encode하고 Phase 07이 별도 decoder/reference fixture로 읽는다.

어느 쪽도 기본값이 아니다. Owner/package 또는 artifact path, producer mapper,
consumer decoder, canonical order/encoding version/content digest,
unknown/missing/duplicate field rejection, exact termination/last-safe mapping과
non-authoritative claim 경계를 receipt가 모두 지정해야 한다.

완료 신호:

- `rpdptw-solver`는 core만 semantic dependency로 가진다.
- Phase 06 producer와 Phase 07 consumer가 solver internal class를 공유하지 않는다.
- 독립 fixture/decoder compatibility test가 schema one-field corruption을 거부한다.
- Same envelope의 repeated/parallel/cache/order permutation이 같은 trace/candidate를
  만든다.
- Phase 07 handoff에는 mutable trial/cache/verifier claim이 없다.
- `E-P06-*` → independent review → acceptance receipt의 단방향 DAG가 완성된다.

## 8. 목표 module, package와 Java 설계

### 8.1 Proposed file 배치

아래 tree는 **future target**이다. 현재 존재를 주장하지 않는다.

```text
rpdptw/solver/
├── pom.xml
├── src/main/java/com/ronext/rpdptw/solver/
│   ├── state/
│   │   ├── SearchSnapshotRef.java
│   │   ├── TrialDraft.java
│   │   ├── CowTrialFactory.java
│   │   ├── CowRouteStore.java
│   │   ├── CentralPairRemovalEditor.java   # owner 승인 시에만
│   │   ├── TrialCommitter.java
│   │   └── StateIsolationFailure.java
│   ├── search/
│   │   ├── AlnsEngine.java
│   │   ├── AlnsSearchState.java
│   │   ├── AlnsStateSlot.java
│   │   ├── VisibleAlnsState.java
│   │   ├── StateIdentity.java
│   │   ├── StatePublishResult.java
│   │   ├── AlnsEngineStepOutcome.java
│   │   ├── AlnsRunOutcome.java
│   │   ├── AlnsStepExecutor.java
│   │   ├── AlnsStepDerivation.java
│   │   ├── IterationOutcome.java
│   │   ├── StageGuard.java
│   │   ├── Phase1Screener.java
│   │   └── StableChampionReducer.java
│   ├── search/destroy/
│   ├── search/repair/
│   ├── search/improvement/
│   ├── search/adaptive/
│   ├── search/acceptance/
│   ├── random/
│   ├── termination/
│   └── replay/
└── src/test/java/com/ronext/rpdptw/solver/
    ├── fixture/
    ├── oracle/
    ├── state/
    ├── search/
    ├── random/
    ├── termination/
    ├── replay/
    ├── security/
    └── performance/

build/architecture-rules/
└── src/test/java/com/ronext/rpdptw/architecture/
    └── Phase06SolverArchitectureTest.java

handoff contract target after WP-06.0 approval:
  option A: core-owned minimal immutable candidate vocabulary
  option B: versioned immutable artifact schema + independent decoder
```

`CentralPairRemovalEditor.java`는 이름이 적혀 있어도 생성 권한이 아니다. Residual
owner gate가 Phase 05로 결정되면 solver는 approved core/Phase 05 contract를
소비하고 중복 editor를 만들지 않는다.

### 8.2 Dependency direction

```text
rpdptw-solver
  → rpdptw-core

rpdptw-solver MUST NOT
  → rpdptw-verification
  → rpdptw-application
  → customer/profile implementation
  → HTTP/storage/cloud/provider SDK
  → optimizer vendor API
```

Phase 06이 소비하는 upstream 의미:

```text
Phase 03/04:
  ProblemInstance / PreparedTravel
  BoundProfile / SolvePlan
  full solution evaluator / business comparator
  authority fingerprints

Phase 05:
  SearchSnapshot / SearchRequestBank
  PairInsertionEvaluator / FeasibleInsertionOption
  ConstructionCandidate / SeedPortfolio
  Phase05SeedPortfolioHandoff
```

Phase 07은 solver를 compile-depend하지 않는다. 따라서 `CommittedCandidate`의
handoff projection은 §7.4/§15.2의 owner gate를 통과한 core-owned immutable 값
또는 Java dependency와 독립된 versioned artifact schema여야 한다. Solver internal
class를 serialization해 넘기거나 Phase 07의 proposed `CandidateSnapshot`을 solver가
역으로 import하는 shortcut을 쓰지 않는다.

### 8.3 Proposed state types

아래 코드는 완성 구현이 아니라 type boundary를 검토하기 위한 skeletal contract다.

```java
package com.ronext.rpdptw.solver.search;

public record AlnsSearchState(
    SearchSnapshotRef current,
    SearchSnapshotRef stageBest,
    SearchSnapshotRef solveBest,
    SearchProgress progress,
    OperatorLearningSnapshot operatorLearning,
    AcceptanceStateSnapshot acceptanceState,
    RngLineage rngLineage,
    DecisionTraceRef trace
) {
    // PROPOSED:
    // - null rejection
    // - authority equality
    // - pairwise-distinct top-level handles
    // - defensive immutable components
}

public record SearchProgress(
    int stageOrdinal,
    long completedStep,
    long requestedCompletedSteps,
    AlnsRunIdentity runIdentity
) {}
```

`SearchSnapshotRef`는 provider URI나 mutable object pointer가 아니다. Slot role과
immutable content identity를 함께 가진 solver-local handle 후보다.

```java
// PROPOSED INTERNAL — cross-Phase API review 전 승인된 API가 아니다.
public interface AlnsStepExecutor {
    AlnsStepDerivation execute(
        AlnsSearchState before,
        AlnsExecutionConfig config,
        StepRandomStreams random,
        InterruptionProbe interruption
    );
}

public sealed interface AlnsStepDerivation
        permits AlnsStepDerivation.Completed,
                AlnsStepDerivation.Invalid,
                AlnsStepDerivation.Interrupted,
                AlnsStepDerivation.Failed {

    record Completed(
        IterationOutcome outcome,
        AlnsSearchState unpublishedAfter,
        StepEvidence evidence
    ) implements AlnsStepDerivation {}

    record Invalid(
        InvalidCandidateEvidence evidence,
        AlnsSearchState unchanged
    ) implements AlnsStepDerivation {}

    record Interrupted(
        ExceptionalTermination termination,
        InterruptionEvidence evidence,
        AlnsSearchState unchanged
    ) implements AlnsStepDerivation {}

    record Failed(
        SearchFailure failure,
        AlnsSearchState unchanged
    ) implements AlnsStepDerivation {}
}
```

이 contract에서 유일한 executor method 이름은 `execute`이고 반환 type은
`AlnsStepDerivation`이다. `Completed.unpublishedAfter`는 아직 외부에 publish되지
않은 immutable local value다. `Completed`의 세 component는 항상
`(outcome, unpublishedAfter, evidence)`이며 별도 `unpublished` boolean이나
생략된 component가 없다. Executor는 state holder, `AtomicReference`, application
store, CAS port와 durable artifact를 소유하지 않는다. `unchanged`는 “같은 점수”가
아니라 canonical bytes, fingerprints, progress, learning, acceptance와 RNG
lineage가 input과 같은 상태다.

State visibility는 `AlnsEngine`의 단일 owner loop만 소유한다.

```java
// PROPOSED INTERNAL skeletal ownership contract.
public interface AlnsEngine {
    AlnsRunOutcome run(
        AlnsStateSlot stateSlot,
        AlnsExecutionConfig config,
        AlnsRunIdentity runIdentity,
        long baseSeed,
        InterruptionProbe interruption
    );

    AlnsEngineStepOutcome runOneStep(
        AlnsStateSlot stateSlot,
        AlnsExecutionConfig config,
        long baseSeed,
        InterruptionProbe interruption
    );
}

public record VisibleAlnsState(
    AlnsSearchState state,
    StateIdentity identity,
    long publicationCount
) {
    // PROPOSED: non-null, non-negative count, identity matches state bytes.
}

public interface AlnsStateSlot {
    VisibleAlnsState readVisible();

    StatePublishResult publishOnce(
        StateIdentity expectedBefore,
        AlnsSearchState immutableAfter
    );
}

public sealed interface StatePublishResult
        permits StatePublishResult.Published,
                StatePublishResult.Conflict,
                StatePublishResult.Failed {

    record Published(VisibleAlnsState visibleAfter)
        implements StatePublishResult {}

    record Conflict(VisibleAlnsState actualVisible)
        implements StatePublishResult {}

    record Failed(
        VisibleAlnsState actualVisible,
        StatePublicationFailure failure
    ) implements StatePublishResult {}
}

public sealed interface AlnsEngineStepOutcome
        permits AlnsEngineStepOutcome.Published,
                AlnsEngineStepOutcome.Exceptional,
                AlnsEngineStepOutcome.Failed {

    record Published(
        IterationOutcome outcome,
        VisibleAlnsState committed,
        StepEvidence evidence
    ) implements AlnsEngineStepOutcome {}

    record Exceptional(
        ExceptionalTermination termination,
        InterruptionEvidence evidence,
        VisibleAlnsState lastCommitted
    ) implements AlnsEngineStepOutcome {}

    record Failed(
        SearchFailure failure,
        VisibleAlnsState lastCommitted
    ) implements AlnsEngineStepOutcome {}
}

public sealed interface AlnsRunOutcome
        permits AlnsRunOutcome.Normal,
                AlnsRunOutcome.Exceptional,
                AlnsRunOutcome.Failed {

    record Normal(
        NormalTermination termination,
        VisibleAlnsState lastCommitted
    ) implements AlnsRunOutcome {}

    record Exceptional(
        ExceptionalTermination termination,
        VisibleAlnsState lastCommitted
    ) implements AlnsRunOutcome {}

    record Failed(
        SearchFailure failure,
        VisibleAlnsState lastCommitted
    ) implements AlnsRunOutcome {}
}
```

`AlnsStepDerivation.Completed`와 `AlnsEngineStepOutcome.Published`는 서로 다른
variant다. 전자는 executor가 만든 unpublished value이고 후자만 state-slot
linearization을 확인한 engine outcome이다. `AlnsRunOutcome`은 여러
`AlnsEngineStepOutcome`을 접은 run 경계 결과다. 이 세 type 사이에 상속,
cast 또는 같은 constructor 재사용은 없다.

`AlnsStateSlot`의 semantic contract:

1. `readVisible()`은 state bytes, `StateIdentity`와 누적 publication count를 한
   atomic visibility snapshot으로 읽는다. 세 getter를 따로 읽어 조합하지 않는다.
2. `expectedBefore`가 visible state identity와 같을 때만 `immutableAfter`를 한 번
   선형화한다.
3. `Published.visibleAfter` 반환은 `immutableAfter` 전체가 visible하고
   `publicationCount == before.publicationCount + 1`이라는 뜻이다. Candidate,
   best, progress, learning, acceptance, RNG와 trace의 부분 visibility는 없다.
4. `Conflict.actualVisible`과 `Failed.actualVisible`은 각 결과가 선형화된 시점의
   실제 atomic visibility snapshot이다. 이 call이 derived state를 publish하지
   않았고 이 call에 귀속되는 publication-count 증가가 0이라는
   **unchanged-by-this-call** 의미다. 다른 publisher가 owner invariant를 이미
   깨뜨렸다면 actual count는 stale `before`와 다를 수 있으므로 같다고 거짓
   주장하지 않는다.
5. Valid composition은 publish-capable `AlnsStateSlot` reference를 한
   `AlnsEngine` owner loop에만 준다. Observer/executor/operator에는
   `VisibleAlnsState` 또는 더 좁은 read-only projection만 준다. 따라서
   `Conflict`는 정상 경쟁 결과가 아니라
   `PUBLICATION_OWNERSHIP_INVARIANT_VIOLATION`으로 map하는 fail-closed defect다.
   Fault double은 이 path를 만들 수 있지만 production owner는 retry하지 않는다.
6. Engine은 final pre-publication probe 뒤 `publishOnce`를 정확히 한 번 호출한다.
   `Published` 뒤 signal은 이미 완료된 step을 되돌리지 않고 다음 boundary에서
   exceptional termination으로 기록한다.
7. 이 slot은 **solver-local search state publication**이다. Phase 08/09의 artifact,
   storage 또는 public result commit과 같은 transaction으로 합치지 않는다.

`runOneStep`과 `run`의 exact mapping:

| Source result/boundary | `runOneStep` return | `run` return/continuation |
|---|---|---|
| Derivation `Completed` + slot `Published` | `AlnsEngineStepOutcome.Published(outcome, visibleAfter, evidence)` | Requested count 전이면 다음 step; exact count면 `AlnsRunOutcome.Normal(MAX_STEPS_REACHED, visibleAfter)` |
| Derivation `Interrupted` 또는 final probe signal | `AlnsEngineStepOutcome.Exceptional(exactTermination, evidence, before)` | `AlnsRunOutcome.Exceptional(exactTermination, before)` |
| Derivation `Invalid`/`Failed` | `AlnsEngineStepOutcome.Failed(mappedFailure, before)` | `AlnsRunOutcome.Failed(mappedFailure, before)` |
| Slot `Conflict(actualVisible)` | `AlnsEngineStepOutcome.Failed(PUBLICATION_OWNERSHIP_INVARIANT_VIOLATION, actualVisible)` | 같은 failure와 truthful `actualVisible`; retry 없음 |
| Slot `Failed(actualVisible, cause)` | `AlnsEngineStepOutcome.Failed(mappedPublicationFailure, actualVisible)` | 같은 failure와 truthful `actualVisible`; retry 없음 |
| Signal after slot `Published` | 현재 call은 `Published` 유지 | 다음 loop boundary에서 `Exceptional(exactTermination, visibleAfter)` |

Phase 06 single-worker `run`이 직접 만드는 유일한 normal termination은
`MAX_STEPS_REACHED`다. `NO_STRICT_IMPROVEMENT`와 `MAX_ROUNDS_REACHED`는 계속
Phase 10 owner다. Derivation의 `Invalid`와 `Failed`, publication conflict/failure를
normal termination이나 published completion으로 바꾸지 않는다.

구체 `AtomicReference` 사용 여부와 Java memory primitive는 implementation
decision이지만 위 atomicity/visibility/failure oracle은 바꿀 수 없다. 이 proposed
shape가 cross-Phase review에서 승인되지 않으면 다른 shape를 조용히 구현하지 않고
WP-06.0으로 돌아간다.

### 8.4 Proposed operator, repair와 acceptance contracts

```java
public interface DestroyOperator {
    OperatorVersion version();

    DestroyProposal propose(
        SearchSnapshot current,
        DestroyConfig config,
        RandomStream operatorOwnedRandom
    );
}

public record DestroyProposal(
    OperatorVersion operator,
    int requestedRemovalCount,
    List<RequestId> orderedUniqueRequests,
    Set<RouteId> sourceRoutes,
    ScoreAndTieEvidence evidence,
    Digest decisionTraceDigest
) {}
```

Destroy operator는 proposal만 만든다. Route/bank mutation은 approved central
editor만 수행한다.

```java
public interface RepairOperator {
    OperatorVersion version();

    RepairResult repair(
        TrialDraft postDestroy,
        RepairPlan plan,
        PairInsertionEvaluator phase05Evaluator,
        RandomStream shortlistRandom
    );
}

public record RepairPlan(
    RepairScope scope,
    ShortlistPolicy shortlist,
    NewRoutePolicy newRoute,
    StableTiePolicy tie,
    RepairBudget budget
) {}
```

Repair는 cheap shortlist를 만들 수 있지만 shortlist score를 feasibility나
objective authority로 사용하지 않는다. Every shortlisted option과 모든 legal
unused concrete vehicle `NEW_ROUTE` option은 Phase 05 exact evaluator로 보낸다.

```java
public interface AcceptancePolicy {
    AcceptanceDecision decide(
        EvaluatedCandidate candidate,
        SearchSnapshot current,
        BoundComparator comparator,
        AcceptanceStateSnapshot state,
        RandomStream acceptanceRandom
    );

    AcceptanceStateSnapshot advanceAfterCompletedStep(
        AcceptanceStateSnapshot before,
        IterationOutcome outcome
    );
}

public sealed interface AcceptanceStateSnapshot
        permits HillClimbingState, SimulatedAnnealingState {}
```

Hill Climbing은 equal policy를 explicit하게 가져야 한다. SA는 승인된 scalar energy
projection ID/version, initial temperature, completed-step cooling과 scale이 모두
있어야 한다. 일부가 빠졌을 때 “보수적으로 reject”하는 fallback은 금지한다.

### 8.5 Proposed termination contract

```java
public sealed interface Termination
        permits NormalTermination, ExceptionalTermination {}

public enum NormalTermination implements Termination {
    MAX_STEPS_REACHED,
    NO_STRICT_IMPROVEMENT,
    MAX_ROUNDS_REACHED
}

public enum ExceptionalTermination implements Termination {
    WATCHDOG_REACHED,
    CANCELLED,
    RESOURCE_LIMIT_REACHED,
    PLATFORM_TIMEOUT,
    FAILED,
    INCOMPLETE
}
```

Phase 06의 single worker run은 `MAX_STEPS_REACHED`만 정상 종료로 직접 낼 수
있다. `NO_STRICT_IMPROVEMENT`와 `MAX_ROUNDS_REACHED`는 Phase 10 outer
coordinator가 all-declared-worker completeness 뒤에 판정한다. Enum에 미래 값을
둘지 별도 type으로 분리할지는 proposed지만 ownership은 바꾸지 않는다.

### 8.6 Explicit config와 숫자 상태

`AlnsExecutionConfig` 후보는 최소 다음을 빠짐없이 묶는다.

```text
algorithmVersion
stateStrategy = CHANGED_ROUTE_COW_V1
ordered destroy registry + each config
ordered repair registry + each config
bounded improvement registry + explicit NONE or budgets
adaptive selection/reward/update config
acceptance policy + lifecycle + every parameter
seed derivation version + namespace mapping
stable request/vehicle/route/position/operator/candidate tie policies
requested completed steps
watchdog/resource/interruption policy
trace schema/canonicalization version
```

다음 omission은 bind error다.

- Removal lower/upper/count/rounding
- Repair scope, shortlist/top-N/sample/exact-evaluation budget
- Bounded improvement family별 attempt/work budget
- Initial weight, reward mapping, update period, reaction, exploration floor
- Acceptance policy와 SA energy/temperature/cooling/scale
- Screen/worker requested steps와 watchdog policy
- Seed derivation, namespace와 stable tie policy

| Config identity | 허용 목적 | 지위 |
|---|---|---|
| `TEST_ONLY_COW_THREE_STEP_V1` | Small fixture와 red→green | Test-only |
| `TEST_ONLY_SA_BOUNDARY_V1` | SA boundary 손 계산 | Test-only |
| `OGC2024_LEGACY_REPLAY` | Differential compatibility | Legacy test/experiment only |
| `EXPERIMENT_<id/version>` | Calibration 측정 | Approval 전 official 금지 |
| Official manifest | Phase 14 official execution | `Q-BENCH-02` 승인 전 생성 금지 |

Legacy의 `5%..15%`, `maxDestroy=1000`, Shaw `9/3/2`, reward
`20/10/2/0`, update `100`, reaction `0.5`, floor `0.01` 같은 값은
versioned differential fixture일 뿐 general production default가 아니다.

### 8.7 Namespaced RNG

```text
baseSeed
+ seedDerivationVersion
+ seedContextFingerprint
+ phaseKind
+ portfolioSlot?
+ roundOrdinal?
+ workerOrdinal?
+ alnsRunOrdinal
+ completedStepOrdinal
+ purpose
+ operatorId/version?
→ derived stream
```

최소 purpose:

```text
DESTROY_OPERATOR_SELECTION
REPAIR_OPERATOR_SELECTION
DESTROY_OPERATOR_INTERNAL
REPAIR_SHORTLIST_SAMPLING
IN_STEP_IMPROVEMENT_SELECTION
ACCEPTANCE_DRAW
```

규칙:

1. 한 purpose의 draw count 변화가 다른 purpose를 움직이지 않는다.
2. Operator internal stream은 operator ID/version으로도 namespace한다.
3. Logical retry는 같은 stream에서 시작한다.
4. `AttemptId`, thread, executor, current time, elapsed, cache hit/miss와 completion
   order를 derivation에 넣지 않는다.
5. Random subset의 universe를 stable ID order로 먼저 정렬한다.
6. Weighted selection은 ordered registry의 stable scan을 사용한다.
7. Derived seed/draw digest는 trace에 남기되 좋은 seed를 골라 cherry-pick하지 않는다.
8. Mixing algorithm과 canonical byte encoding은 version approval 전 외부 호환
   약속이 아니다.

## 9. State transition과 skeletal pseudocode

### 9.1 Completed-step state machine

```text
STEP_READY
→ OPERATOR_SELECTED
→ TRIAL_OPEN
→ DESTROY_PROPOSED
→ PAIR_DESTROY_APPLIED
→ REPAIR_COMPLETED
→ BOUNDED_IMPROVEMENT_COMPLETED
→ STRUCTURE_VALIDATED
→ FULL_EVALUATED
→ STAGE_GUARD_EVALUATED
→ ACCEPTED | REJECTED
→ NEXT_CURRENT_AND_BEST_DERIVED
→ ADAPTIVE_AND_ACCEPTANCE_DERIVED
→ NEXT_STATE_DERIVED_WITH_COMPLETED_STEP_RECORDED
→ PRE_PUBLICATION_PROBE
→ ENGINE_PUBLISH_ONCE_REQUESTED(expectedBefore, immutableAfter)
→ NEXT_STATE_PUBLISHED_ONCE → ENGINE_STEP_PUBLISHED → COMPLETED
  | PUBLICATION_CONFLICT → OWNERSHIP_INVARIANT_FAILED(actualVisible)
  | PUBLICATION_FAILURE → FAILED(actualVisible)
```

`completedStep`, trace, learning과 acceptance state는 publication 뒤에 따로 쓰지
않는다. Executor가 전부 immutable `nextState` 안에서 먼저 만들고, engine owner
loop가 `expectedBefore` identity와 함께 state slot에 한 번 전달한다.

Side transition:

```text
malformed proposal / editor defect / repair DEFECT
  → discard → FAILED

hard-infeasible candidate
  → discard → INVALID_CANDIDATE → fail-closed

cancel / watchdog / resource / platform signal before publication
  → discard → exact exceptional termination

signal after single publication
  → completed step remains completed
  → next boundary records exact exceptional termination

publication conflict / atomic publication failure
  → derived after is not made visible by this call
  → this call's successful-publication delta is 0
  → atomically returned actual visible state/identity/count is truthful lastCommitted
  → conflict maps to ownership-invariant defect; no internal retry
```

`StatePublishResult.Published`만 completed step을 visible하게 만든다. Executor가
`AlnsStepDerivation.Completed(unpublishedAfter)`를 파생했다는 사실만으로 completed
work를 외부에 보고하지 않는다. Valid exclusive-owner 실행에서는 conflict가
성립하지 않으며, fault double이나 owner 위반으로 관찰되면 normal 경쟁으로
처리하지 않고 actual visibility snapshot을 가진 invariant failure로 종료한다.

### 9.2 ALNS step pseudocode

아래는 구현 순서와 실패 지점을 보여 주는 skeleton이다. Copy-paste 가능한 완성
코드가 아니다.

```text
executor.execute(before, config, random, interruption):
  require accepted authority identities
  require before-state invariants
  require explicit config closure

  probe or return new AlnsStepDerivation.Interrupted(
    exactTermination, interruptionEvidence, before)
  select destroy and repair from ordered registries
  probe

  trial := open COW from before.current
  try:
    proposal := destroy.propose(current, config, destroy-internal stream)
    probe
    validate ordered unique assigned RequestIds
    apply with approved central pair editor
    assert exact route-bank partition
    probe

    repair via Phase 05 evaluator
    if DEFECT:
      discard
      return new AlnsStepDerivation.Failed(mappedFailure, before)
    probe

    run explicit bounded improvement or explicit NONE
    probe before/after each work unit

    evaluated := Phase 03/04 authoritative full evaluation
    probe
    if structural invalid or hard-infeasible:
      discard
      return new AlnsStepDerivation.Invalid(invalidEvidence, before)

    guardPassed := SolvePlan stage guard
    probe
    decision := guardPassed
      ? configured acceptance
      : REJECT without acceptance draw
    probe

    candidateContent := freeze local immutable content
    nextCurrentContent := decision.isAccepted()
      ? candidateContent
      : content(before.current)
    nextStageBestContent := guardPassed
      ? strictlyBestBusinessContent(before.stageBest, candidateContent)
      : content(before.stageBest)
    nextSolveBestContent := guardPassed
      ? strictlyBestBusinessContent(before.solveBest, candidateContent)
      : content(before.solveBest)

    create distinct slot handles for all three contents
    classify outcome with business comparator
    probe

    nextLearning := completed-outcome update
    nextAcceptance := completed-step advance
    nextTrace := append canonical logical events
    nextState := immutable aggregate with completedStep + 1
    stepEvidence := immutable evidence for outcome and nextState
    return new AlnsStepDerivation.Completed(
      outcome, nextState, stepEvidence)

  catch typed operator/config/arithmetic/identity failure:
    discard trial and unpublished values
    return new AlnsStepDerivation.Failed(searchFailure, before)

engineOwner.runOneStep(stateSlot, config, baseSeed, interruption):
  before := stateSlot.readVisible()
  random := derive purpose streams from
    baseSeed + before.state().progress() logical completed-step identity
  derived := executor.execute(
    before.state(), config, random, interruption)

  if derived is AlnsStepDerivation.Invalid:
    return new AlnsEngineStepOutcome.Failed(
      mapInvalidToFailure(derived.evidence()), before)
  if derived is AlnsStepDerivation.Interrupted:
    return new AlnsEngineStepOutcome.Exceptional(
      derived.termination(), derived.evidence(), before)
  if derived is AlnsStepDerivation.Failed:
    return new AlnsEngineStepOutcome.Failed(
      derived.failure(), before)

  final pre-publication probe
  if interrupted:
    return new AlnsEngineStepOutcome.Exceptional(
      exactTermination, interruptionEvidence, before)

  publication := stateSlot.publishOnce(
    before.identity(),
    derived.unpublishedAfter())

  if publication is StatePublishResult.Published:
    committed := publication.visibleAfter()
    require committed.state() == derived.unpublishedAfter()
    require committed.publicationCount() == before.publicationCount() + 1
    return new AlnsEngineStepOutcome.Published(
      derived.outcome(), committed, derived.evidence())

  if publication is StatePublishResult.Conflict:
    actual := publication.actualVisible()
    require actual is one atomic state/identity/count snapshot
    require this call did not make derived.unpublishedAfter() visible
    require this call's successful-publication delta == 0
    return new AlnsEngineStepOutcome.Failed(
      PUBLICATION_OWNERSHIP_INVARIANT_VIOLATION, actual)

  if publication is StatePublishResult.Failed:
    actual := publication.actualVisible()
    require actual is one atomic state/identity/count snapshot
    require this call did not make derived.unpublishedAfter() visible
    require this call's successful-publication delta == 0
    return new AlnsEngineStepOutcome.Failed(
      mapPublicationFailure(publication.failure()), actual)

engineOwner.run(stateSlot, config, runIdentity, baseSeed, interruption):
  require stateSlot.readVisible().state().progress().runIdentity() == runIdentity

  loop:
    visible := stateSlot.readVisible()
    if visible.state().progress().completedStep()
        == config.requestedCompletedSteps():
      return new AlnsRunOutcome.Normal(
        MAX_STEPS_REACHED, visible)

    step := engineOwner.runOneStep(
      stateSlot, config, baseSeed, interruption)

    if step is AlnsEngineStepOutcome.Published:
      probe next run boundary
      if interrupted:
        return new AlnsRunOutcome.Exceptional(
          exactTermination, step.committed())
      if step.committed().state().progress().completedStep()
          == config.requestedCompletedSteps():
        return new AlnsRunOutcome.Normal(
          MAX_STEPS_REACHED, step.committed())
      continue

    if step is AlnsEngineStepOutcome.Exceptional:
      return new AlnsRunOutcome.Exceptional(
        step.termination(), step.lastCommitted())

    if step is AlnsEngineStepOutcome.Failed:
      return new AlnsRunOutcome.Failed(
        step.failure(), step.lastCommitted())
```

중요한 review 교정:

- Guard 실패 candidate는 세 solution slot 모두를 보존한다.
- Acceptance 결과의 유일한 boolean projection은 `decision.isAccepted()`다.
  정의되지 않은 local `accepted`나 candidate quality를 대신 사용하지 않는다.
- Same content가 세 slot을 이겨도 slot handle은 각각 새로 만든다.
- Publication과 completed-step 기록을 두 번의 state write로 나누지 않는다.
- Named lifecycle boundary마다 cooperative interruption을 확인한다.
- Executor의 유일한 method는 `execute`이며
  `AlnsStepDerivation.Completed(outcome, unpublishedAfter, evidence)`만 파생한다.
  Engine owner만 final probe와 `publishOnce(expected, unpublishedAfter)`를
  호출하고 성공 뒤 별도 `AlnsEngineStepOutcome.Published`를 만든다.
- Linearization point는 `StatePublishResult.Published`가 반환되는 단일 atomic
  state-slot write다. `Conflict`/`Failed`는 derived state를 이 call로 publish하지
  않고 call 귀속 publication 증가가 0이며, atomic `actualVisible`을 truthful
  `lastCommitted`로 사용한다. Conflict는 exclusive-owner invariant defect이고
  내부 retry가 없다.
- `runOneStep`은 `AlnsEngineStepOutcome`, `run`은 `AlnsRunOutcome`만 반환하며
  §8.3 mapping 외의 cast, constructor 생략 또는 termination rename은 없다.

### 9.3 Phase-1 screen

```text
available Phase 05 members in canonical 4×2 order
→ derive independent screen namespace per slot
→ run exact explicit test/experiment screenMaxSteps
→ require MAX_STEPS_REACHED
→ require cache-free equality
→ collect by member order, never completion order
→ business comparator
→ if EQUAL:
     growthPolicyOrdinal
     vehicleOrderPolicyOrdinal
     candidateContentFingerprint
→ phase-1 champion
```

`CLOCK_COORDINATE_MISSING`으로 unavailable인 member는 failed screen이 아니다.
Available screen 하나라도 fault, cancel, invalid 또는 incomplete이면 별도 승인된
정책 없이 나머지만으로 champion을 만들지 않는다.

### 9.4 Replay comparison

`ReplayManifest` 후보의 최소 semantic field:

```text
schema/version
problem/travel/numeric/time/adapter authority
profile/evaluation/objective/SolvePlan authority
algorithm/operator/repair/improvement/adaptive/acceptance/state versions
build/runtime compatibility
base seed + derivation version + derived seed identities
logical phase/member/round/worker/run ordinals
warm-start fingerprint
requested/completed work
stable order/tie versions
exact termination + last committed boundary
canonical trace digest
current/stageBest/solveBest/candidate fingerprints
```

| 관찰 | 판정 |
|---|---|
| Authority/config identity가 다름 | `NOT_COMPARABLE`, replay 시작 금지 |
| Same envelope에서 seed/draw/event가 다름 | `REPLAY_MISMATCH` |
| Trace 같고 candidate bytes가 다름 | COW/state/canonicalization defect |
| Candidate 같고 trace가 다름 | Hidden nondeterminism 또는 trace defect |
| Requested work/termination이 다름 | Strong equality 주장 금지 |
| Parallel schedule만 다름 | Trace와 candidate exact equality 필요 |

## 10. Ordered work packages

각 WP는 앞 WP의 green artifact를 last safe point로 가진다. Gate가 닫혔는데 다음
WP로 넘어가거나, 실패한 WP의 일부 출력을 섞어 새 candidate를 만들지 않는다.
아래 모든 Future verification은 §11.5의 accepted effective-POM과 fresh
`PHASE06_M2` upstream `-am clean install` bootstrap receipt를 전제로 하며,
명령 일부만 떼어 현재 checkout의 acceptance evidence로 사용할 수 없다.

### WP-06.0 — Entry, authority와 config freeze

- **목적:** 구현 전에 source, upstream artifact, owner, config와 evidence 기준을
  하나의 reviewable contract로 고정한다.
- **왜 먼저 하는가:** 현재 두 residual cross-Phase blocker를 우회하면 이후 모든
  COW/replay test가 잘못된 evaluator 또는 중복 editor를 검증하는 false-green이 된다.
- **사전조건:** Scheduler task/owner 배정 제안, Phase 03~05 accepted bundle을
  받을 준비, Phase 06 review finding 이해.
- **예상 file/package/type:** Production Java 없음. Contract review record,
  `AlnsExecutionConfig` schema 후보, evidence manifest skeleton만 검토한다.
- **구체 행동:**
  1. §3의 plain current source와 user-fixed dated source semantic diff를 검토하고
     `SOURCE_AUTHORITY_DECISION_RECEIPT`를 받는다.
  2. Phase 05 handoff의 authority/content/evidence digest를 대조한다.
  3. Central pair-removal editor owner/module/API/test/evidence key를 공동 승인받는다.
  4. Full solution evaluator, business equality와 context tie seam을 승인받는다.
  5. Effective POM에서 solver test dependency, Surefire/Failsafe version·execution과
     zero-test policy를 승인받는다.
  6. `AlnsEngine`/`AlnsStateSlot` publication owner와 Phase 06→07 schema owner,
     mapping 및 compatibility evidence를 승인받는다.
  7. 모든 operator/repair/improvement/acceptance/termination/seed/tie 숫자의 상태를
     `fixed/test-only/experiment/open`으로 분류한다.
  8. Scheduler task, implementer, reviewer와 rollback identity를 기록한다.
- **근거:** Phase 06 review `F-P06-003`, `F-P06-004`; canonical Phase 06 §4/§15.
- **금지 shortcut:** Placeholder scalar evaluator, default temperature, 임시 editor,
  가장 좋은 Phase 05 member만 선택, 임의 task ID.
- **검증:**

  ```bash
  test -s docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md
  test -s docs/implementation/phases/phase-06-cow-alns-reproducibility.md
  test -s docs/implementation/phases/phase-07-independent-verification-final-result.md
  git rev-parse HEAD:docs/implementation/phases/phase-06-cow-alns-reproducibility.md
  ```

- **기대:** 위 명령은 source 존재만 확인한다. Source-authority, predecessor,
  effective-POM, publication-owner, handoff-schema decision receipt 중 하나라도
  없으면 entry는 계속 BLOCKED인 것이 정상이다.
- **실패 해석:** Source drift, owner conflict 또는 missing config는 구현 결함이
  아니라 시작 금지 조건이다.
- **Rollback/last safe:** 이 가이드와 approved semantic review 전 상태.
- **Handoff:** Frozen source/authority/config/test/evidence contract를 WP-06.1로 넘긴다.

### WP-06.1 — COW state, identity와 discard isolation

- **목적:** Parent를 오염시키지 않는 최소 COW primitive를 만든다.
- **왜 필요한가:** 모든 operator와 acceptance가 이 안전성에 의존한다. State가
  안전하지 않으면 더 높은 test는 의미가 없다.
- **사전조건:** WP-06.0 green, accepted immutable `SearchSnapshot`과 bank contract.
- **예상 file/package/type:** `solver.state`의 `SearchSnapshotRef`, `TrialDraft`,
  `CowTrialFactory`, route store, committer와 isolation failure; test fixture/counter.
- **구체 행동:**
  1. 세 top-level slot handle을 distinct하게 생성한다.
  2. Trial bank를 방어 복사하고 immutable route만 공유한다.
  3. Route first write에 trial-owned copy를 한 번 만든다.
  4. Mutation마다 영향 cache/fingerprint generation을 무효화한다.
  5. Freeze/commit/discard 뒤 mutable trial reference가 밖에 남지 않게 한다.
  6. Pre/post canonical bytes, copy set과 owner graph instrumentation을 남긴다.
- **근거:** Master §12, Domain §8, Phase 06 review `F-P06-010`.
- **금지 shortcut:** Whole parent mutation 후 undo, shallow mutable collection 공유,
  GC를 reachability oracle로 사용, same handle을 세 slot에 대입.
- **Future verification:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
    -Dtest=CowTrialIsolationTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **기대:** Exact required methods가 fresh report에 모두 있고 failed/error/skipped가
  0이며 `uniqueMutatedRouteIds == copiedRouteIds`, mutable alias가 0이다.
- **실패 해석:** Parent fingerprint 변화는 test flakiness가 아니라 state corruption이다.
- **Rollback:** WP-06.1 변경을 폐기하고 accepted Phase 05 snapshot digest로 돌아간다.
- **Handoff:** Audited open/write/freeze/discard primitive를 WP-06.2로 넘긴다.

### WP-06.2 — Destroy, central pair edit, repair와 bounded improvement

- **목적:** Proposal-only operator와 upstream exact evaluator를 COW trial 위에서
  합성한다.
- **왜 필요한가:** Request atomicity와 evaluation authority가 operator마다
  갈라지는 것을 막는다.
- **사전조건:** WP-06.1 green, central editor owner 승인, accepted
  `PairInsertionEvaluator`.
- **예상 file/package/type:** `solver.search.destroy`, `repair`, `improvement`;
  `CentralPairRemovalEditor`는 승인된 owner에만 둔다.
- **구체 행동:**
  1. Random, related/Shaw, route, historical, worst/semi-worst,
     location-oriented family를 versioned proposal로 구현한다.
  2. Ordered unique assigned request만 central editor에 전달한다.
  3. Repair scope와 cheap shortlist를 explicit config로 만든다.
  4. Shortlist와 legal `NEW_ROUTE` options를 Phase 05 evaluator로 평가한다.
  5. Complete/partial/no-feasible 결과에서 route-bank XOR을 보존한다.
  6. Bounded improvement family마다 work budget 또는 explicit `NONE`을 둔다.
- **근거:** Master §11.4~11.6, Domain §11, Integrated §9.7~9.8.
- **금지 shortcut:** Operator 직접 route mutation, node 단위 제거, feasibility 복제,
  promising score 권위화, concrete vehicle 대신 type/count sentinel, 무제한 local loop.
- **Future verification:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
    -Dtest=DestroyOperatorContractTest,RepairPipelineContractTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **기대:** Duplicate/unknown/unassigned proposal 거부, exact evaluator call set,
  partial/no-feasible partition과 legacy differential trace가 green이다.
- **실패 해석:** Evaluator가 missing이라 local 공식을 추가해야 하는 상황은 upstream
  contract gate가 닫혔다는 뜻이다.
- **Rollback:** Operator registry/composition만 제거하고 WP-06.1 COW를 보존한다.
- **Handoff:** Versioned proposal/result와 exact call trace를 WP-06.3으로 넘긴다.

### WP-06.3 — Transactional completed-step와 best monotonicity

- **목적:** Accept/reject/invalid/interrupted/fault를 원자적인 한 step으로 만든다.
- **왜 필요한가:** Candidate, progress, best, learning과 temperature의 부분 commit을
  막아야 정확한 종료와 replay가 가능하다.
- **사전조건:** WP-06.2 green, accepted full evaluation/comparator/stage guard.
- **예상 file/package/type:** `AlnsStepExecutor`, `AlnsStepDerivation`,
  `AlnsEngineStepOutcome`, `AlnsRunOutcome`, `IterationOutcome`, `StageGuard`,
  `AlnsEngine`, `AlnsStateSlot`, `VisibleAlnsState`, `StatePublishResult`,
  publication recorder.
- **구체 행동:**
  1. §9.1 lifecycle과 named probe를 그대로 구현한다.
  2. Hard/stage guard 뒤에만 acceptance를 호출한다.
  3. Guard 실패 시 세 slot content를 보존한다.
  4. Same candidate content라도 slot별 distinct handle을 만든다.
  5. Learning/acceptance/trace/progress까지 local next state에 포함한다.
  6. Executor는
     `AlnsStepDerivation.Completed(outcome, unpublishedAfter, evidence)`만 파생하고
     engine owner가 final probe 뒤
     `publishOnce(expectedBefore, unpublishedAfter)`를 정확히 한 번 호출한다.
  7. `runOneStep`은 publish 성공만 별도 `AlnsEngineStepOutcome.Published`로 만들고
     `run`은 §8.3의 exact step→run termination mapping만 사용한다.
  8. Publication conflict는 atomic `actualVisible`을
     `PUBLICATION_OWNERSHIP_INVARIANT_VIOLATION`의 truthful `lastCommitted`로
     사용한다. Publication failure도 atomic `actualVisible`을 사용하며 둘 다
     derived state unchanged-by-this-call, call 귀속 publication 증가 0,
     no-retry다.
- **근거:** Phase 06 review `F-P06-001`, `006`, `010`, `011`.
- **금지 shortcut:** Candidate 먼저 commit 후 counter update, invalid를 reject로
  바꾸기, guard 실패 candidate의 best 승격, publication 후 rollback.
- **Future verification:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
    -Dtest=AlnsStepStateMachineTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **기대:** Publication count 1, pre-publication interruption은 entire state unchanged,
  post-publication signal은 step을 보존하며 best는 comparator 기준 monotonic이다.
  Conflict/failure fault double에서는 derived aggregate가 이 call로 보이지 않고
  call 귀속 publication 증가가 0이다. Conflict의 unauthorized actual snapshot은
  before와 달라도 atomic state/identity/count 그대로 `lastCommitted`에 보고되며
  invariant failure이고, 어떤 failure에서도 published outcome이나 retry가 없다.
- **실패 해석:** Adaptive updater 예외 뒤 candidate가 보이면 transaction boundary가
  잘못된 것이다.
- **Rollback:** WP-06.3 executor를 제거하고 pure WP-06.2 composition과 WP-06.1
  state primitive를 보존한다.
- **Handoff:** Completed-step state transition을 WP-06.4로 넘긴다.

### WP-06.4 — Adaptive selection, acceptance와 namespaced RNG

- **목적:** Learning, acceptance와 randomness를 explicit immutable state로 만든다.
- **왜 필요한가:** 전역 RNG, hidden default와 registry order는 replay를 조용히
  깨뜨리는 가장 흔한 원인이다.
- **사전조건:** WP-06.3 green, seed derivation/canonical encoding review 승인.
- **예상 file/package/type:** `solver.search.adaptive`, `acceptance`, `solver.random`;
  independent reference calculation.
- **구체 행동:**
  1. Operator registry를 canonical ID/version order로 고정한다.
  2. Finite·positive·sum-one probability와 zero-use case를 처리한다.
  3. Completed outcome만 count/reward/weight를 전진시킨다.
  4. HC/SA의 모든 parameter와 lifecycle을 bind한다.
  5. Purpose/operator substream을 stateless logical context에서 유도한다.
  6. Attempt/thread/order/draw perturbation metamorphic test를 만든다.
- **근거:** Master §11.5/§13, Phase 06 §7.4~7.5.
- **금지 shortcut:** `Random` singleton, `ThreadLocalRandom`, unordered registry,
  omitted temperature, lexicographic Big-M, draw를 모든 목적이 공유.
- **Future verification:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
    -Dtest=AdaptiveOperatorSelectorTest,AcceptancePolicyTest,SeedStreamOwnershipTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **기대:** NaN/Infinity/zero-active가 0, sum-one 표현 계약 일치, invalid/interrupted
  non-advance, independent seed reference exact equality.
- **실패 해석:** 한 purpose draw 변화가 다른 purpose를 바꾸면 derivation/ownership
  결함이다.
- **Rollback:** Versioned adaptive/acceptance/RNG layer를 제거하고 deterministic
  explicit single-operator/HC test seam으로 돌아가 재검토한다.
- **Handoff:** Immutable learning/acceptance/RNG lineage를 WP-06.5로 넘긴다.

### WP-06.5 — Phase-1 screen과 exact termination/fault

- **목적:** Portfolio 전체를 공정하게 screen하고 single worker의 정확한 work와
  종료를 기록한다.
- **왜 필요한가:** 일부 성공 후보, completion-first winner와 wall-clock stop은
  quality와 reproducibility를 왜곡한다.
- **사전조건:** WP-06.4 green, Phase 05 canonical member order.
- **예상 file/package/type:** `Phase1Screener`, `StableChampionReducer`,
  `termination`, manual interruption probe, `AlnsWorkerRunIT`.
- **구체 행동:**
  1. Every available member에 exact explicit screen step을 할당한다.
  2. 모든 screen의 normal completion/cache-free equality를 요구한다.
  3. Member order로 collect하고 business comparator→stable tie로 reduce한다.
  4. Single worker의 requested/completed step equality를 기록한다.
  5. Cancel/watchdog/resource/platform/operator fault를 exact enum으로 보존한다.
  6. 각 lifecycle boundary의 last committed safe point를 기록한다.
- **근거:** Master §11.3/§13.1, Architecture §3.3~3.6.
- **금지 shortcut:** 일부 screen champion, real sleep 기반 flaky timeout,
  `PLATFORM_TIMEOUT→MAX_STEPS`, screen elapsed를 tie/quality에 사용.
- **Future verification:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
    -Dtest=Phase1ScreenerTest,TerminationFaultTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
    -Dit.test=AlnsWorkerRunIT \
    -Dfailsafe.failIfNoSpecifiedTests=true clean verify
  ```

- **기대:** Schedule permutation마다 same champion, requested=completed,
  every fault boundary에서 incomplete draft가 count되지 않는다.
- **실패 해석:** One screen fault 뒤 champion이 존재하면 completeness defect다.
- **Rollback:** Screener/termination layer만 제거하고 WP-06.4 single-step engine을
  보존한다.
- **Handoff:** Champion, worker candidate와 exact termination을 WP-06.6으로 넘긴다.

### WP-06.6 — Independent replay, corruption와 parallel determinism

- **목적:** Production 구현과 독립된 reference로 step별 trace와 state를 검증한다.
- **왜 필요한가:** Production helper를 두 번 호출하면 같은 bug를 두 번 재생할 뿐
  독립 evidence가 아니다.
- **사전조건:** WP-06.5 green, trace/manifest canonical schema review.
- **예상 file/package/type:** `solver.replay`; test-only
  `FullCopyStepReference`, `IndependentSeedDerivationReference`,
  `IndependentTraceCanonicalizer`, `IndependentReplayOracle`.
- **구체 행동:**
  1. Strong envelope와 canonical event inclusion/exclusion을 고정한다.
  2. Reference는 COW가 아닌 full copy를 사용한다.
  3. Seed derivation과 canonicalization을 production helper 없이 구현한다.
  4. Three-step fixture를 row-by-row literal expected로 검증한다.
  5. Repeated/parallel/cache/order/AttemptId permutations을 실행한다.
  6. Manifest/seed/event/candidate one-field corruption을 주입한다.
- **근거:** Master §13.2~13.3, Phase 06 review `F-P06-007`~`009`.
- **금지 shortcut:** Production trace를 expected로 저장, production seed helper
  import, 최종 fingerprint만 비교, mismatch 중 더 좋은 candidate 선택.
- **Future verification:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
    -Dtest=ReplayDeterminismTest,ReplayCorruptionTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test
  ```

- **기대:** 모든 row와 final fingerprint exact equality, corruption 전부 typed
  rejection, AttemptId/parallel schedule은 canonical result에 무영향.
- **실패 해석:** Oracle가 production helper를 import하면 `E-P06-REPLAY` 전체가
  무효다.
- **Rollback:** Replay layer와 evidence claim을 폐기하고 WP-06.5 trace를
  unapproved 상태로 보존한다.
- **Handoff:** Replay manifest, oracle report와 candidate fingerprint를 WP-06.7로 넘긴다.

### WP-06.7 — Architecture, security, performance, evidence와 review

- **목적:** Module 경계와 비기능 계약을 검증하고 immutable acceptance DAG를
  완성한다.
- **왜 필요한가:** Unit test가 green이어도 provider/vendor leakage, raw ID/PII
  trace와 false-green Maven report가 Phase를 실패시킬 수 있다.
- **사전조건:** WP-06.1~06.6 green, required test skipped 0.
- **예상 file/package/type:** Architecture test, trace redaction test, copy/work
  accounting test, pre-review manifest와 handoff projection.
- **구체 행동:**
  1. Solver→core only, global RNG와 customer/provider/verifier/vendor reference 0을
     자동 검사한다.
  2. Canonical trace allowlist와 observation metadata 분리를 검증한다.
  3. Copy, cheap/exact/feasible/completed work counter를 exact하게 검증한다.
  4. Fresh report의 class/method 수, command/toolchain/exit code와 digest를 봉인한다.
  5. `E-P06-COW`, `E-P06-ALNS`, `E-P06-REPLAY`를 pre-review manifest에 연결한다.
  6. Independent review report와 별도 acceptance receipt를 기다린다.
- **근거:** Architecture §2/§5.5~5.6, Plan §8~§13.
- **금지 shortcut:** `-DskipTests`, stale `target/`, console summary만 인용,
  reviewer verdict를 pre-review manifest에 backfill, raw input/secret trace.
- **Future verification:**

  ```bash
  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
    -Dtest=SolverTraceRedactionTest,CowCopyWorkAccountingTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" \
    -pl rpdptw/solver -am clean install

  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" \
    -pl build/architecture-rules \
    -Dtest=Phase06SolverArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true clean test

  ./mvnw -B -ntp -Dstyle.color=never \
    -Dmaven.repo.local="$PHASE06_M2/repository" clean verify
  ```

- **기대:** OR-Tools-free ALNS-only reactor green, forbidden reference 0, sensitive
  raw value 0, exact work counters, §11.2의 89-method manifest exact equality,
  immutable evidence와 independent review `PASS`.
- **실패 해석:** Code가 있어도 bundle/review/receipt가 불완전하면 최대
  `IMPLEMENTED_PENDING_EVIDENCE`다.
- **Rollback:** Last accepted predecessor와 last green WP artifact/digest를
  보존하고 incomplete evidence를 acceptance에 사용하지 않는다.
- **Handoff:** Candidate/evidence/replay 세 부류만 Phase 07/10/14A에 넘긴다.

## 11. 테스트 구현 안내

### 11.1 Fixture, builder와 oracle 분리

추천 test-only 역할:

| Type | 책임 | Production import |
|---|---|---:|
| `AlnsFixtureBuilder` | Immutable small problem/snapshot fixture 조립 | Core semantic type만 |
| `TestAlnsConfigBuilder` | 모든 값을 explicit한 test-only config 생성 | Config API만 |
| `ReplayFixtureBuilder` | Canonical literal rows/resource 제공 | Production replay helper 금지 |
| `ManualInterruptionProbe` | Named boundary에서 deterministic signal | Clock/sleep 금지 |
| `FullCopyStepReference` | COW와 독립된 deep-copy state transition | Solver state implementation 금지 |
| `IndependentSeedDerivationReference` | 승인된 seed spec 별도 계산 | Production `SeedDeriver` 금지 |
| `IndependentTraceCanonicalizer` | Literal schema로 bytes/digest 계산 | Production canonicalizer 금지 |
| `IndependentReplayOracle` | Step별 expected row와 mismatch 판정 | Production engine 호출 금지 |

Builder가 validation을 대신하면 안 된다. Corruption test는 정상 builder 결과를
받아 정확히 한 field를 바꾼 fixture를 명시적으로 만들어야 한다.

### 11.2 핵심 test class와 exact method 후보

모든 이름은 future/proposed다. 실제 cross-Phase API가 승인되면 의미를 보존하며
이름을 조정하고 required-method manifest도 함께 갱신한다.

| Class | Exact method 후보 | Fixture/oracle | Pass |
|---|---|---|---|
| `CowTrialIsolationTest` | `copiesChangedRouteOnlyOnFirstWrite()` | `twoRoutesOneMovedPair`, copy counter | Copied IDs = unique mutated IDs |
|  | `sharesOnlyImmutableUnchangedRoutes()` | Mutability probe | Writable alias 0 |
|  | `copiesBankIndependentlyBeforeMutation()` | Pre/post bytes | Parent bank exact unchanged |
|  | `keepsCurrentStageBestAndSolveBestTopLevelRefsDistinct()` | Same-content three slots | Handles distinct, content may equal |
|  | `rejectDiscardLeavesParentAndBestFingerprintsUnchanged()` | Rejectable trial | All bytes/fingerprints equal |
|  | `exceptionDiscardLeavesNoReachableTrialMutation()` | Throwing editor + owner graph | Reachable mutable ref 0 |
|  | `cancelDiscardLeavesParentUnchanged()` | Manual cancellation probe | Candidate/bank/cache/fingerprint unchanged |
| `AlnsStepStateMachineTest` | `acceptedImprovementPublishesOneImmutableNextState()` | Published/conflict/failure state-slot doubles | Published exactly once; derivation/published types distinct; conflict reports atomic actual state/identity/count as invariant failure; failed calls publish no derived bytes and add no call-owned count |
|  | `acceptedNonImprovingChangesCurrentButNeverBest()` | SA accepted worse | Only current changes |
|  | `rejectedFeasibleTrialCountsOneCompletedStep()` | HC reject | Step/reward/cooling once |
|  | `invalidCandidateAdvancesNoStateAndFailsClosed()` | Hard invalid trial | Entire state unchanged |
|  | `interruptedStepAdvancesNoState()` | Probe at each lifecycle boundary | Exact unchanged |
|  | `interruptionRaceUsesPublicationAsSingleLinearizationPoint()` | Before/after signal | Before unchanged; after completed once |
|  | `adaptiveFailureBeforePublicationRollsBackWholeStep()` | Throwing updater | Candidate not visible |
|  | `stageGuardRunsBeforeAcceptanceDraw()` | Guard fail + draw counter | Acceptance/draw count 0 |
|  | `stageGuardFailureCannotUpdateCurrentStageBestOrSolveBest()` | Attractive lower-stage candidate | All slots unchanged |
|  | `globalBestImprovementPublishesDistinctSlotHandlesForSameContent()` | Global best candidate | Three handles distinct |
|  | `hardInfeasibleCandidateNeverReachesAcceptance()` | Phase 03 rejected evaluation | Acceptance calls 0 |
| `DestroyOperatorContractTest` | `randomRemovalUsesStableUniverseAndDedicatedStream()` | Shuffled source maps | Same proposal |
|  | `relatedRemovalUsesPreparedAndBoundFactsOnly()` | Hand relatedness matrix | Exact score/order |
|  | `routeRemovalHonorsExplicitNonZeroBoundsAndRounding()` | Boundary routes | Exact nonzero count |
|  | `historicalRemovalUsesStableServiceIdentityNotRouteIndex()` | Reindexed routes | Same RequestIds |
|  | `worstRemovalOnlyProposesAndNeverMutates()` | Removal delta table | Proposal exact; parent bytes unchanged |
|  | `locationRemovalDistinguishesLogicalAndPhysicalPickup()` | Mixed service patterns | No fake physical pickup |
|  | `rejectsDuplicateUnknownOrUnassignedProposal()` | One-field corrupt proposals | Typed defect |
|  | `legacyReplayPresetMatchesVersionedDifferentialTrace()` | `OGC2024_LEGACY_REPLAY` fixture | Exact slot/config/trace; no default leakage |
| `RepairPipelineContractTest` | `usesCheapScoreOnlyForShortlist()` | Misleading score | Exact evaluator decides |
|  | `delegatesEveryShortlistedOptionToPhase05ExactEvaluator()` | Spy evaluator | Exact legal call set |
|  | `includesEveryLegalUnusedVehicleNewRouteOption()` | Two unused vehicles | Both in stable order |
|  | `partialReinsertionPreservesRouteBankExactPartition()` | Two removed, one feasible | XOR preserved |
|  | `noFeasibleInsertionPreservesHardFeasibleSnapshot()` | No-option fixture | Stable hard-feasible candidate; exact partition |
|  | `defectNeverReachesAcceptance()` | Corrupt result | Acceptance count 0 |
|  | `doesNotReimplementPropagationOrComparator()` | Architecture/package inspection | Duplicate evaluator implementation 0 |
| `AdaptiveOperatorSelectorTest` | `probabilitiesAreFinitePositiveAndSumToOne()` | Rational/decimal hand table | Exact declared representation |
|  | `zeroUseDenominatorNeverProducesNaNOrInfinity()` | Zero counts | Finite positive vector |
|  | `sameSnapshotOutcomeAndConfigProduceSameNextFingerprint()` | Fixed outcome classes | Reference updater byte/fingerprint equality |
|  | `invalidAndInterruptedDoNotAdvanceLearning()` | Non-completed outcomes | Bytes unchanged |
|  | `selectionIsIndependentOfRegistryInsertionOrder()` | Permuted registry | Same operator/trace |
|  | `updatePeriodUsesCompletedStepsOnly()` | Reject/invalid/interrupted mix | Update only at expected completed boundary |
| `AcceptancePolicyTest` | `hillClimbingRequiresExplicitEqualPolicy()` | Missing/equal config | Missing bind fails |
|  | `saRejectsMissingEnergyTemperatureCoolingOrScale()` | One-field omissions | Every omission rejected |
|  | `saUsesApprovedEnergyProjectionNotLexicographicBigM()` | Conflicting vector/scalar | Only declared projection |
|  | `saUsesAcceptancePurposeStreamOnly()` | Draw-count perturbation | Other streams unchanged |
|  | `temperatureAdvancesOnceAfterCompletedRejectedStep()` | SA reject fixture | Exact next state |
|  | `temperatureDoesNotAdvanceAfterInvalidOrInterruptedStep()` | Non-completed outcomes | State unchanged |
|  | `sameStateCandidateAndDrawProduceSameDecision()` | Fixed draw boundary | Same decision/fingerprint |
| `SeedStreamOwnershipTest` | `derivesDistinctSeedsForEveryPurposeNamespace()` | Independent seed reference | Exact expected values |
|  | `retryAttemptIdDoesNotChangeLogicalStreams()` | Two attempt IDs | Same derived seeds |
|  | `unrelatedDestroyDrawsDoNotShiftAcceptanceStream()` | Draw-count perturbation | Acceptance stream same |
|  | `unusedAcceptanceDrawDoesNotShiftNextStepOperators()` | Better/worse prior candidate | Next-step operator streams equal |
|  | `threadAndCompletionOrderDoNotEnterDerivation()` | Executor permutations | Same streams |
|  | `detectsSeedDerivationVersionMismatch()` | Corrupted manifest | `NOT_COMPARABLE` |
| `Phase1ScreenerTest` | `screensEveryAvailablePortfolioCandidateToExactStepBudget()` | 7 available + CLOCK unavailable | Each available exact |
|  | `requiresNormalCompletionForEveryAvailableScreen()` | One watchdog screen | No champion |
|  | `selectsChampionIndependentOfCompletionOrder()` | Delayed fake runs | Same champion/trace |
|  | `usesPolicyOrderThenFingerprintForEqualObjectiveTie()` | Equal objective | Exact stable winner |
|  | `doesNotTreatClockUnavailableAsFailedCandidate()` | Missing-coordinate Phase 05 manifest | Slot remains unavailable |
| `TerminationFaultTest` | `returnsMaxStepsOnlyAtExactCompletedCount()` | Requested 3 + rejects | Exact equality |
|  | `watchdogDuringEveryStepBoundaryDiscardsDraft()` | Parameterized manual probe | Exact unchanged |
|  | `cancelDuringEveryStepBoundaryDiscardsDraft()` | Parameterized manual probe | `CANCELLED`; exact unchanged |
|  | `resourceSignalIsNotRenamedWatchdogOrMaxSteps()` | Resource fake | Exact enum |
|  | `platformTimeoutIsNotAlgorithmCompletion()` | Platform signal fake | `PLATFORM_TIMEOUT`; no synthetic record |
|  | `operatorExceptionReturnsFailedAndPreservesLastCommittedBest()` | Throwing operator | Best unchanged |
|  | `malformedProposalFailsClosedWithoutInternalRetryLoop()` | Duplicate proposal + call counter | One failure; hidden retry 0 |
| `ReplayDeterminismTest` | `matchesIndependentThreeStepReferenceRowByRow()` | Three-step builder + independent oracle | Every row exact |
|  | `sameEnvelopeProducesSameTraceAndCandidateOnRepeatedRuns()` | Repeated run | Exact bytes/digest |
|  | `retryAttemptIdDoesNotChangeCanonicalTraceOrCandidate()` | Two attempts | Semantic equality |
|  | `sameEnvelopeProducesSameTraceUnderParallelScreenSchedules()` | Schedule permutations | Exact trace/champion |
|  | `cacheHitMissPatternDoesNotChangeReplay()` | Cache toggle | Exact trace/candidate |
|  | `unorderedSourceCollectionsDoNotChangeReplay()` | Permuted map/set creation | Same trace/candidate |
| `ReplayCorruptionTest` | `detectsChangedWarmStartFingerprint()` | One-field manifest change | `NOT_COMPARABLE` |
|  | `detectsChangedOperatorVersionOrTiePolicy()` | One-field corruption | `NOT_COMPARABLE` |
|  | `detectsDerivedSeedOrDrawDigestCorruption()` | One-field trace change | `REPLAY_MISMATCH` |
|  | `detectsStepEventReorderOmissionAndDuplication()` | Three corrupt traces | All rejected |
|  | `detectsSameTraceDifferentCandidateFingerprint()` | Candidate corruption | Mismatch |
|  | `detectsSameCandidateDifferentTraceDigest()` | Trace corruption | `REPLAY_MISMATCH` |
| `AlnsWorkerRunIT` | `runsPhase05WarmStartThroughPhase03EvaluationWithoutDuplicateEvaluator()` | Cross-module fixture | One evaluator authority |
|  | `handsOffCandidateEvidenceAndReplayManifestOnly()` | Completed worker | No verifier/final fields |
|  | `exceptionalRunPreservesExactTerminationAndLastSafePoint()` | Fault matrix | No normal overclaim |
| `SolverTraceRedactionTest` | `traceContainsIdsDigestsAndVersionsButNoRawAddressInputOrSecret()` | Sensitive strings | Sensitive values 0 |
|  | `traceUsesSafeInternalIdsAndExcludesRawExternalIdentifiers()` | Raw ID fixture | Safe IDs only |
|  | `solverModuleHasNoTenantAuthorizationOrProviderCredentialLogic()` | Architecture inspection | Forbidden references 0 |
| `CowCopyWorkAccountingTest` | `copiesExactlyDistinctMutatedRoutesAcrossRepeatedWrites()` | Repeated writes | Copy count exact |
|  | `recordsCheapExactFeasibleAndCompletedWorkSeparately()` | Work hand table | Every counter exact |
|  | `doesNotUseWallClockAsQualityOrTieInput()` | Vary elapsed | Candidate unchanged |
| `Phase06SolverArchitectureTest` | `solverDependsOnlyOnCoreAmongSemanticModules()` | Compiled graph | Forbidden edge 0 |
|  | `solverHasNoCloudHttpCustomerVerifierOrVendorReferences()` | Source/bytecode denylist | Reference 0 |
|  | `searchDoesNotImplementPropagationTravelOrFinalization()` | Package ownership rule | Duplicate semantic owner 0 |
|  | `productionHasNoStaticGlobalOrThreadLocalRandom()` | Bytecode/source rule | Violation 0 |

#### Canonical required-method manifest

위 표의 89개 행은 선택 예시가 아니라 canonical Phase 06 exact matrix다. 표 순서로
각 행을 UTF-8/LF `Class#method()` 한 줄로 normalize한 manifest identity는 다음과
같다.

```text
manifest: PHASE06-CANONICAL-REQUIRED-METHODS-V1
count: 89
sha256: 1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86
```

| Class | Count | Target WP | Required evidence |
|---|---:|---|---|
| `CowTrialIsolationTest` | 7 | 06.1 | `E-P06-COW` |
| `AlnsStepStateMachineTest` | 11 | 06.3 | `E-P06-COW`, `E-P06-ALNS` |
| `DestroyOperatorContractTest` | 8 | 06.2 | `E-P06-ALNS` |
| `RepairPipelineContractTest` | 7 | 06.2 | `E-P06-ALNS` |
| `AdaptiveOperatorSelectorTest` | 6 | 06.4 | `E-P06-ALNS` |
| `AcceptancePolicyTest` | 7 | 06.4 | `E-P06-ALNS` |
| `SeedStreamOwnershipTest` | 6 | 06.4, 06.6 | `E-P06-REPLAY` |
| `Phase1ScreenerTest` | 5 | 06.5 | `E-P06-ALNS` |
| `TerminationFaultTest` | 7 | 06.5 | `E-P06-ALNS` |
| `ReplayDeterminismTest` | 6 | 06.6 | `E-P06-REPLAY` |
| `ReplayCorruptionTest` | 6 | 06.6 | `E-P06-REPLAY` |
| `AlnsWorkerRunIT` | 3 | 06.5, 06.7 | `E-P06-ALNS`, `E-P06-REPLAY` |
| `SolverTraceRedactionTest` | 3 | 06.7 | `E-P06-ALNS` |
| `CowCopyWorkAccountingTest` | 3 | 06.7 | `E-P06-COW`, `E-P06-ALNS` |
| `Phase06SolverArchitectureTest` | 4 | 06.7 | `E-P06-ALNS` |
| **Total** | **89** | **06.1~06.7** | **세 bundle 모두** |

WP별 selected run은 해당 row를 조기에 검증할 뿐 canonical closure를 대체하지
않는다. WP-06.7에서 fresh Surefire solver XML, fresh Failsafe solver XML, fresh
architecture-rules XML을 test case의 declared class와 method로 합쳐 같은
normalization을 수행한다. Canonical manifest와 discovered multiset의
`missing`, `extra`, `duplicate`가 모두 0, total 89, failed/error/skipped가 모두
0이어야 한다. XML file/testcase가 run 시작 이전 timestamp이거나 이번 command와
toolchain/effective-POM digest에 연결되지 않으면 stale로 거부한다.

Method rename/add/delete는 의미가 같아 보여도 canonical Phase 06 source,
manifest version/hash, WP/evidence mapping과 independent review를 한 번에
갱신하기 전에는 허용하지 않는다. Corruption/fault method는 정상 case의 green만
남기지 않고 정확히 한 defect를 주입했을 때 oracle이 red가 되는 sensitivity
receipt도 남긴다.

### 11.3 Red → green 순서

1. **Red A:** COW copy/bank/no-alias/discard tests를 먼저 실패시킨다.
2. **Green A:** 최소 state primitive만 구현한다.
3. **Red B:** Destroy/repair/central editor delegation tests를 추가한다.
4. **Green B:** Proposal과 exact evaluator composition만 구현한다.
5. **Red C:** Accept/reject/invalid/interrupted/publication race를 실패시킨다.
6. **Green C:** Immutable aggregate single publication을 구현한다.
7. **Red D:** Adaptive/acceptance/purpose RNG boundary tests를 추가한다.
8. **Green D:** Explicit config와 versioned namespaced streams를 구현한다.
9. **Red E:** All-screen completeness와 fault termination tests를 추가한다.
10. **Green E:** Stable reducer와 typed termination을 구현한다.
11. **Red F:** Production import가 없는 replay/corruption oracle tests를 추가한다.
12. **Green F:** Row-by-row replay와 mismatch classifier를 구현한다.
13. **Green G:** Architecture, redaction, work accounting과 full reactor를 통과한다.

Green을 만들기 위해 expected 값을 production output으로 덮어쓰거나 required
test를 삭제·skip하지 않는다. Oracle 변경은 source/spec diff와 independent review가
필요하다.

### 11.4 Test category별 적용성과 pass 판정

| 종류 | Phase 06 적용 | Pass 기준 | 미적용/후속 이유 |
|---|---:|---|---|
| Unit/boundary | Yes | COW, outcome, probability, acceptance, seed, termination exact | 없음 |
| Contract | Yes | Phase 03/04/05 approved interface를 중복 없이 소비 | 없음 |
| Integration | Yes | Phase 05 seed→Phase 03/04 full eval→Phase 06 handoff | Provider/publication은 후속 |
| Local E2E | Limited | Solver worker boundary까지만 | Both-verifier/publication local E2E는 Phase 07/08 |
| Full product E2E | No | Phase 06 exit에 요구하지 않음 | Phase 08과 user final DoD |
| Architecture | Yes | Forbidden module/package/random/vendor edge 0 | 없음 |
| Fault injection | Yes | Every named boundary에서 discard/last safe exact | 없음 |
| Corruption | Yes | Proposal, state, manifest, trace, seed, candidate mutation 거부 | 없음 |
| Reproducibility | Yes | Repeated/parallel/cache/order/AttemptId permutations exact | 없음 |
| Security | Limited but required | Trace minimization과 dependency boundary | IAM/tenant/provider rehearsal은 Phase 11/14 |
| Performance | Work-accounting only | Copy/evaluation/work count exact, bounded config | Wall-clock SLA는 Phase 14A; apply/undo profiling은 RM-7 |
| Provider integration | No | Phase 06에 cloud SDK 0 | Phase 11/12 |
| MIP/backend | No | OR-Tools-free default build | Phase 13 gated |

### 11.5 False-green 방지

현재 live parent POM의 Surefire `failIfNoTests=false`, solver POM의 test dependency
부재와 Failsafe execution 부재는 Phase 06 구현 시작 전에 닫아야 할
`EFFECTIVE_POM_CONTRACT — BLOCKED`다. 승인된 effective-POM receipt에는 최소한
다음이 digest와 함께 있어야 한다.

- Wrapper distribution URL/SHA와 실제 Maven/JDK identity
- Solver의 approved JUnit Jupiter test dependency
- Cross-module fixture를 쓰는 경우 approved test-fixture artifact/test-jar dependency
- Surefire와 Failsafe의 명시적 승인 version 및 execution
- Surefire의 `*Test`, Failsafe의 `*IT` ownership과 Failsafe
  `integration-test`/`verify` goals
- Solver/architecture required test가 0개이면 실패하는 policy

Fresh isolated-repository procedure는 다음 순서를 따른다. `PHASE06_M2`는 evidence
run마다 새 directory이며, wrapper가 아닌 system `mvn`을 섞지 않는다.

```bash
PHASE06_M2="$(mktemp -d "${TMPDIR:-/tmp}/phase06-m2.XXXXXX")"
RUN_STARTED_UTC="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE06_M2/repository" \
  -pl rpdptw/core,build/test-fixtures -am clean install

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE06_M2/repository" \
  -pl rpdptw/solver help:effective-pom \
  -Doutput="$PHASE06_M2/solver-effective-pom.xml"

./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local="$PHASE06_M2/repository" -pl rpdptw/solver \
  -Dtest=CowTrialIsolationTest \
  -Dsurefire.failIfNoSpecifiedTests=true clean test
```

첫 command의 reactor `-am`은 target-only selected run이 필요로 하는 upstream
artifact를 같은 clean isolated repository에 설치한다. Selected pattern을 `-am`
reactor 전체에 전파하면 upstream module의 nonmatching test가 failure 또는
false-positive source가 되므로, 두 번째 단계부터는 target module만 고른다.
Integration test는 같은 repository에서 target-only `clean verify`와
`-Dit.test=AlnsWorkerRunIT -Dfailsafe.failIfNoSpecifiedTests=true`를 사용한다.
Architecture test는 solver를 `-pl rpdptw/solver -am clean install`한 뒤
`build/architecture-rules` target-only selected run으로 수행한다. 마지막으로
root `clean verify`를 별도로 실행해 reactor 전체를 검증한다.

금지:

- `-DskipTests`
- `-Dmaven.test.skip=true`
- `failIfNoSpecifiedTests=false`
- Accepted effective POM 없이 command-line property만으로 POM 결함을 가리기
- `-am`이 upstream nonmatching test를 만나도록 selected pattern을 전파
- Stale `target/surefire-reports`
- Failsafe execution 없이 `*IT` source 존재만 확인
- 이전 run의 XML과 이번 run의 console을 혼합
- Flaky test 중 성공한 retry 하나만 선택

Maven exit `0`은 필요조건일 뿐이다. §11.2 manifest checker가 fresh
Surefire/Failsafe/architecture XML을 합쳐 exact 89-method multiset과
`1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86`
hash를 재계산하고, missing/extra/duplicate 및
failed/error/skipped 0을 확인해야 한다. Console-only success, zero-test module,
stale XML 또는 이번 run에 연결되지 않은 report는 모두 evidence failure다.

## 12. 사람 checkpoint, evidence와 stop/resume

### 12.1 Checkpoint 표

| 시점 | 사람이 승인할 것 | Stop 조건 | Resume 조건 |
|---|---|---|---|
| CP-0 entry | Source authority, predecessor, editor/evaluator, effective-POM, publication와 handoff-schema receipts | 하나라도 missing/ambiguous | Exact owner/mapping/config identities가 모두 accepted |
| CP-1 COW | No-alias/copy/discard evidence | Parent mutation 또는 ambiguous ownership | COW tests와 owner graph green |
| CP-2 pair pipeline | Central editor single owner, exact evaluator reuse | Duplicate editor/evaluator | Cross-Phase compile/contract evidence |
| CP-3 step transaction | Executor derivation→engine step→run mapping, engine-owned single publication과 interruption linearization | Constructor/type mismatch, stale conflict `lastCommitted`, executor publication, partial commit 또는 retry | Compile-semantic mapping과 Published/conflict/failure state-slot matrix green |
| CP-4 RNG/acceptance | Versioned config/seed schema | Hidden default/global RNG | Independent reference equality |
| CP-5 screen/termination | All-screen completeness, exact work | Partial champion/renamed timeout | Unit+integration fresh reports |
| CP-6 replay | Oracle independence/corruption sensitivity | Production helper import | Row-by-row and seeded-defect evidence |
| CP-7 evidence | Exact 89 manifest, POM/architecture/security/work reports, immutable DAG | Missing/extra/duplicate/skipped/stale result | Manifest hash match + independent review + acceptance receipt |

### 12.2 Evidence bundle 내용

세 bundle 공통 provenance에는
`PHASE06-CANONICAL-REQUIRED-METHODS-V1`,
`1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86`,
discovered count 89, missing/extra/duplicate/failed/error/skipped 0과 fresh XML
digests를 넣는다. Wrapper/JDK/Maven identity, accepted effective-POM digest,
isolated repository identity, exact command/exit와 run start/end도 같은 run
receipt로 연결한다. Live inventory나 이전 XML은 이 provenance를 대신하지 못한다.

`E-P06-COW`:

- Accepted upstream/state/config identities
- Changed-route copy set와 independent bank audit
- Top-level slot handle identity audit
- Accept/reject/fault/cancel pre/post canonical bytes/fingerprints
- Exact command, toolchain, exit code와 fresh required-method manifest

`E-P06-ALNS`:

- Operator/repair/improvement/adaptive/acceptance version과 config
- Requested/completed work와 all-screen champion trace
- Guard, acceptance, best monotonicity와 fault/termination matrix
- Executor `execute` declaration↔pseudocode와
  `AlnsStepDerivation.Completed` component/constructor equality
- `AlnsStepDerivation`→`AlnsEngineStepOutcome`→`AlnsRunOutcome` mapping과 exact termination
- Engine/state-slot publication owner, atomic actual visibility와
  Published/conflict/failure unchanged-by-this-call/count matrix
- Cache-free evaluation/comparator equality
- Exact selected/integration/architecture test manifest

`E-P06-REPLAY`:

- Strong envelope와 base/derived seed lineage
- Independent oracle source/digest와 import audit
- Step별 literal/reference row와 canonical trace
- Repeated/parallel/cache/order/AttemptId-separation 결과
- One-field corruption rejection과 mismatch classification
- Phase 06→07 accepted schema receipt와 producer/consumer compatibility evidence

Evidence provenance는 단방향이다.

```text
immutable implementation/test evidence
→ preReviewEvidenceManifest M
→ independentReviewReport R references M
→ postReviewAcceptanceReceipt references M + R
→ ACCEPTED handoff
```

Pre-review manifest에 reviewer, verdict, review reference 또는 acceptance receipt를
넣거나 나중에 backfill하지 않는다.

### 12.3 Safe observability와 security

Canonical decision trace에 허용:

```text
dense/internal safe IDs
logical phase/member/round/worker/run/step ordinals
config/operator/version
requested/completed work
termination
artifact/authority/trace digests
```

Semantic trace/candidate fingerprint에서 제외:

```text
raw external request/vehicle/customer ID
address/coordinate/full input
credential/secret
provider locator/execution ID
platform AttemptId
thread/executor ID
elapsed/current time
arbitrary exception payload
```

`AttemptId`, elapsed와 provider execution ID는 typed observation record에 둘 수
있지만 semantic replay identity를 바꾸지 않는다. Digest는 authorization이나
encryption을 대신하지 않는다.

### 12.4 Stop과 rollback 원칙

- Entry gate 미충족: 문서/fixture 설계까지만 하고 production code를 시작하지 않는다.
- WP test failure: 다음 WP로 넘어가지 않고 마지막 green artifact를 보존한다.
- Integrity/corruption failure: 해당 candidate/evidence를 폐기하며 부분 채택하지 않는다.
- Replay oracle independence 실패: `E-P06-REPLAY` 전체를 무효로 한다.
- Review failure: `ACCEPTED`로 올리지 않고 `IMPLEMENTED_PENDING_EVIDENCE`,
  `REVIEW_PENDING`, `BLOCKED` 또는 `FAILED` 중 실제 상태를 쓴다.
- Rollback은 같은 identity의 bytes를 overwrite하지 않고 이전 accepted digest를
  다시 가리킨다.

## 13. 흔한 anti-pattern

- `AlnsBatchEngine` placeholder에 type을 덧붙여 실제 Phase 06으로 간주
- `Map<String,Object>`와 `double objective`를 candidate authority로 사용
- `current`, `stageBest`, `solveBest` 또는 parent route/bank/cache 직접 mutation
- Same immutable content라는 이유로 세 slot이 같은 top-level reference 공유
- Whole solution을 무조건 복사하고 changed-route COW evidence를 주장
- Mutable list/map을 defensive copy 없이 record에 저장
- Apply/undo skeleton 또는 automatic threshold 선반영
- Destroy operator가 route를 수정한 뒤 RequestId 목록도 반환
- Pickup/delivery node를 독립 removal 단위로 사용
- Phase 03 evaluator/comparator나 Phase 05 insertion evaluator 복제
- Cheap promising score를 feasibility/objective/final diagnostic으로 사용
- Partial/no-feasible repair를 defect로 처리하거나 bank를 final outcome으로 직렬화
- Hard violation을 penalty/SA/Big-M으로 통과
- Stage guard 실패 candidate를 stageBest/solveBest로 승격
- Candidate를 먼저 commit하고 learning/progress를 나중에 쓰기
- Invalid/interrupted를 rejected로 바꿔 reward/temperature/step 전진
- Rejected feasible step을 completed count에서 빼기
- Global `Random`, static RNG, `ThreadLocalRandom`, unordered collection iteration
- Attempt/thread/time/elapsed/completion order를 seed나 trace에 넣기
- Completion-first screen champion과 random/clock tie-break
- Wall-clock 1분을 phase-1 quality 종료로 사용
- Watchdog/cancel/resource/platform timeout을 `MAX_STEPS_REACHED`로 변환
- Same replay identity의 다른 결과 중 점수가 좋은 것을 선택
- Production helper로 expected replay output 생성
- Trace에 raw external ID, address, PII, secret 또는 full exception 저장
- Executor가 state를 publish하거나 publication conflict를 retry
- Conflict에서 stale expected `before`를 actual `lastCommitted`라고 보고
- Source-authority conflict에서 plain/dates path 중 하나를 무기록 선택
- Phase 07 handoff의 missing/unknown field를 invented default로 보완
- Phase 07 `PASS`, final outcome/result/publication eligibility를 Phase 06에서 생성
- Phase 13 gated type, OR-Tools dependency 또는 vendor status를 미리 추가
- Test-only/legacy 숫자를 general official default로 사용
- System `mvn`, zero-test `BUILD SUCCESS` 또는 stale report만 evidence로 제출

## 14. Phase exit checklist와 Definition of Done

### 14.1 실제 구현 exit checklist

- [ ] `SOURCE_AUTHORITY_DECISION_RECEIPT`가 current plain과 dated input 충돌을 닫았다.
- [ ] Phase 00, 03, 04, 05 accepted receipt와 exact input digest를 확인했다.
- [ ] Central pair-removal editor owner/API/evidence가 한 곳으로 승인됐다.
- [ ] Full solution evaluator, business equality/context tie seam이 승인됐다.
- [ ] Accepted effective POM에 JUnit/test-fixture, Surefire/Failsafe와 zero-test policy가 있다.
- [ ] 모든 config 값이 explicit하고 omission이 bind failure다.
- [ ] Changed route만 first-write copy하고 bank는 독립이다.
- [ ] `current`, `stageBest`, `solveBest` handle이 pairwise distinct다.
- [ ] Reject/invalid/fault/cancel에서 parent/best bytes와 fingerprint가 같다.
- [ ] Destroy는 proposal-only이며 central editor가 pair 전체를 이동한다.
- [ ] Repair가 Phase 05 exact evaluator를 재사용한다.
- [ ] Complete/partial/no-feasible repair가 route-bank XOR을 지킨다.
- [ ] Hard/stage guard가 acceptance보다 먼저다.
- [ ] Guard 실패는 세 slot을 바꾸지 않는다.
- [ ] Rejected는 completed, invalid/interrupted는 non-advance다.
- [ ] Executor `execute`는 선언과 같은 `AlnsStepDerivation`만 만들고
  `Completed(outcome, unpublishedAfter, evidence)` constructor가 component와 같다.
- [ ] `runOneStep`의 `AlnsEngineStepOutcome`과 `run`의 `AlnsRunOutcome`/termination
  mapping이 §8.3과 exact equality다.
- [ ] Publication conflict/failure에서 derived state는 unchanged-by-this-call,
  call 귀속 publication 증가는 0, retry는 0이며 atomic actual visibility가
  truthful `lastCommitted`다.
- [ ] Every named boundary interruption과 pre/post publication race가 검증됐다.
- [ ] Adaptive probability가 finite·positive·sum-one이다.
- [ ] Acceptance/temperature가 explicit하고 completed step에만 전진한다.
- [ ] Namespaced streams가 independent reference와 일치한다.
- [ ] Attempt/thread/completion order가 semantic RNG/trace에 없다.
- [ ] Every available screen이 exact normal budget을 완료한다.
- [ ] Champion이 completion order와 무관하다.
- [ ] Normal/exceptional termination과 requested/completed work가 정확하다.
- [ ] Independent full-copy oracle가 모든 step row에 일치한다.
- [ ] Replay corruption을 모두 거부한다.
- [ ] Solver의 verification/application/provider/customer/vendor dependency가 0이다.
- [ ] Static/global/thread-local random reference가 0이다.
- [ ] Trace에서 raw external ID/input/PII/secret이 0이다.
- [ ] Copy/work counter가 exact하다.
- [ ] Canonical 89-method manifest hash가 일치하고 missing/extra/duplicate/skipped가 0이다.
- [ ] OR-Tools-free ALNS-only module/root build가 fresh report로 green이다.
- [ ] `E-P06-COW`, `E-P06-ALNS`, `E-P06-REPLAY`가 immutable하다.
- [ ] Independent review와 post-review acceptance receipt가 있다.
- [ ] Phase 06→07 schema owner와 양쪽 mapper가 accepted receipt로 고정됐다.
- [ ] Phase 07 handoff에 candidate/evidence/replay만 있고 solver dependency가 0이다.
- [ ] OPEN/GATED/deferred를 임의 값이나 완료 상태로 닫지 않았다.

### 14.2 Definition of Done

Phase 06 `ACCEPTED`는 다음 AND 조건을 뜻한다.

1. Accepted Phase 05 seed에서 parent/best를 오염시키지 않고 COW ALNS를 반복한다.
2. Request-level operator, central mutation, exact evaluator, acceptance와 learning의
   소유권이 분리돼 있다.
3. Completed-step과 normal/exceptional termination이 exact trace로 남는다.
4. Fixed strong envelope에서 decision trace와 candidate가 정확히 재현된다.
5. Production과 코드를 공유하지 않는 independent reference가 COW/RNG/trace
   claim을 검증한다.
6. Official 수치 없이도 explicit test/experiment config로 논리 계약을 검증하되
   official quality/performance를 주장하지 않는다.
7. Phase 07과 Phase 10이 Phase 06 의미를 재구현하지 않고 handoff를 소비할 수 있다.
8. Evidence, review, receipt와 rollback point가 immutable identity로 연결된다.
9. Accepted effective POM과 exact 89-method fresh XML manifest가 zero-test/stale
   report 성공을 차단한다.
10. Accepted handoff schema receipt가 module owner, producer/consumer mapping과
    incompatibility failure를 고정한다.

다음은 Phase 06 DoD가 아니다.

- Placeholder `mvn test` 성공
- Candidate 하나가 feasible하거나 objective가 좋아진 것
- Same seed의 최종 점수만 같은 것
- Java source/test 파일 존재
- Phase 06 document review 완료
- Phase 07 verifier `PASS` 없이 “verified candidate”라고 부르는 것
- Phase 14A benchmark acceptance 또는 Phase 14B production authority

## 15. 다음 Phase와 downstream handoff

### 15.1 Phase 05에서 받아야 할 것

```text
Phase05SeedPortfolioHandoff
  source commit + accepted detail/review/evidence digests
  problem/travel/profile/evaluation/SolvePlan identities
  portfolio/enumeration/tie config fingerprints
  ordered available ConstructionCandidate refs
  each immutable SearchSnapshot/content fingerprint
  each member policy/trace/fingerprint
  unavailable member records
  cache-free validation reports
  E-P05-PAIR / E-P05-INSERTION / E-P05-PORTFOLIO
  rollback point
```

받으면 안 되는 것:

- Phase-1 champion
- Screen/worker official 숫자
- Destroy/repair proposal
- `TrialDraft`, current/best slots
- Acceptance/adaptive/RNG/termination state
- Verifier `PASS`, final outcome 또는 publication status

### 15.2 Phase 07에 넘길 것

현재 module DAG만으로 Phase 06 internal `SearchSnapshot`을 Phase 07에 직접 넘기면
verification이 solver에 의존한다. 반대로 Phase 07의 proposed
`CandidateSnapshot`을 solver가 import하면 Phase 07 type을 Phase 06으로
pull-forward한다. 둘 다 금지하며 다음 gate가 먼저 닫혀야 한다.

```text
PHASE06_07_HANDOFF_SCHEMA_DECISION — BLOCKED

one explicit owner/module/package
  + versioned immutable schema
  + solver producer mapping
  + Phase 07 consumer mapping
  + canonical encoding/digest rules
  + compatibility/corruption tests
  + independent review
→ PHASE06_07_HANDOFF_SCHEMA_RECEIPT
```

사람 review가 선택할 수 있는 허용 대안은 다음 둘뿐이며 이 가이드는 어느 쪽도
default로 결정하지 않는다.

| 대안 | Schema owner | Phase 06 producer boundary | Phase 07 consumer boundary |
|---|---|---|---|
| A — core-owned minimal vocabulary | `rpdptw/core`의 승인된 handoff package/type | Solver mapper가 internal state를 core immutable handoff로 투영; core만 의존 | Verification이 core handoff를 읽어 verifier-internal model로 변환; solver 의존 0 |
| B — independent versioned artifact | 승인된 neutral schema artifact/module owner | Solver exporter가 versioned canonical bytes와 digest 생성 | Verification importer가 schema version을 검증한 뒤 internal model 생성; 새 module edge는 architecture review 필수 |

Receipt는 적어도 다음 mapping을 field별 required/optional, canonical order,
encoding과 failure status까지 기록한다.

| Handoff field group | Producer source | Consumer use | Missing/unknown/mismatch |
|---|---|---|---|
| Schema name/version/owner digest | Accepted schema receipt | Decoder/compatibility gate | Unsupported version → consume 금지 |
| Canonical immutable routes와 request bank | Published `solveBest` content only | Phase 07 cache-free candidate rebuild | Missing/duplicate/XOR mismatch → `CORRUPT` |
| Problem/travel/profile/evaluation/SolvePlan identities | Accepted authority refs | Same-authority verifier bind | Identity/content digest mismatch → `NOT_COMPARABLE` |
| Candidate content digest | Canonical payload bytes | Recomputed digest comparison | Byte/digest mismatch → `CORRUPT` |
| Exact termination/last committed boundary | Published engine outcome | Incomplete/exceptional eligibility check | Renamed/missing status → handoff reject |
| Metric/score/objective/fingerprint claims | Published state/evaluation projection | **Non-authoritative** comparison hint only | Recomputed disagreement → `CORRUPT`; claim을 authority로 사용 금지 |
| `E-P06-COW`/`ALNS` digests와 accepted review receipt | Immutable evidence DAG | Provenance gate | Missing/unaccepted receipt → verification 시작 금지 |
| Replay envelope/seed/trace/slot/candidate digests와 `E-P06-REPLAY` | Accepted replay projection | Independent reproduction/corruption check | Version/digest mismatch → `NOT_COMPARABLE` 또는 `REPLAY_MISMATCH` |
| Requested/completed work, build/runtime/fault/rollback lineage | Worker/run evidence | Completeness와 last-safe interpretation | Partial/unknown mapping → normal completion 주장 금지 |

Producer는 `Published` state와 accepted evidence만 읽고 mutable trial/cache/operator
object를 보지 않는다. Consumer는 schema를 decode하고 authority/digest를
검증한 뒤에만 Phase 07 internal type을 만든다. Compatibility test는
supported-version round trip, unknown version, missing required field, unknown
field policy, route-bank corruption, authority mismatch와 digest mismatch를 모두
포함하고 receipt에 exact method/evidence identity를 남긴다.

넘기지 않는 것:

- Mutable trial, cache handle와 internal operator object
- Candidate verifier `PASS`/`VerifiedSolution`
- Final `ASSIGNED/UNASSIGNED`, audit, diagnostic와 summary
- Result verifier report, publishable result와 publication eligibility
- Exceptional termination을 정상 종료로 바꾼 상태

### 15.3 Handoff가 깨졌을 때 나타나는 증상

| 증상 | 의심할 경계 | 올바른 대응 |
|---|---|---|
| Phase 07이 solver dependency를 요구 | Owner/schema 없이 internal type에 결합 | Schema gate로 돌아가 owner와 producer mapping 승인 |
| Solver가 Phase 07 proposed type을 import | Consumer type을 upstream으로 pull-forward | Core-owned 또는 neutral versioned boundary 승인 전 중단 |
| 같은 bytes가 schema version별로 다르게 해석 | Version/encoding mapping 누락 | Unsupported version reject와 canonical encoding receipt 복원 |
| Required field가 없는데 default로 채움 | Producer/consumer requiredness 불일치 | Handoff reject; invented default 제거 |
| Unknown field/version을 조용히 무시 | Compatibility policy 누락 | Receipt의 explicit forward-compatibility 판정 적용 |
| Payload digest는 같지만 route-bank XOR이 깨짐 | Producer mapping 또는 consumer decode 결함 | `CORRUPT`; 양쪽 mapping test 재실행 |
| Candidate claim과 cache-free value가 다름 | Full evaluator/cache invalidation defect | `CORRUPT`, candidate 사용 금지 |
| Same envelope인데 AttemptId별 trace가 다름 | Platform identity가 semantic trace에 침투 | Seed/trace context에서 제거 |
| Phase-1 champion이 executor schedule마다 다름 | Completion-order reduction | Canonical member order로 collect/reduce |
| Watchdog run이 `MAX_STEPS_REACHED` | Termination mapping defect | Exact exceptional status와 last safe point 복원 |
| Best가 stage guard 실패 candidate로 바뀜 | Eligibility update 순서 결함 | Guarded best derivation으로 수정 |
| Reject 뒤 다음 step 결과가 달라짐 | Parent alias/RNG non-advance 결함 | COW/stream owner graph부터 재검증 |
| Phase 07이 review receipt를 찾지 못함 | Evidence DAG incomplete | Acceptance 전이 금지 |
| Phase 13 type가 Phase 06 artifact에 필수 | Gated scope pull-forward | ALNS-only handoff로 제거 |

### 15.4 Other consumers

| Consumer | 소비할 것 | 소비하면 안 되는 것 |
|---|---|---|
| Phase 10 | Worker/run identity, requested/completed work, exact termination, candidate/replay | Operator internal, partial worker champion |
| Phase 14A | Accepted ALNS result/replay와 measurement schema | Test-only 값을 official threshold로 승격 |
| RM-7 | Copy/allocation/GC/work counter | Automatic apply/undo threshold |
| Phase 13 gated | Accepted ALNS baseline after 14A receipt | Vendor API, raw selector incumbent, Phase 06 gate bypass |

## 16. Source → requirement → WP → test/evidence 추적성

| Requirement | Source heading | WP | Exact test/evidence |
|---|---|---|---|
| `REQ-SOURCE-AUTHORITY` current/plain vs user-fixed dated input | [Current docs map](../../../README.md), [current Domain](../../../domain-design.md), [current Architecture](../../../architecture-design.md), [dated Domain](../../../2026-07-26-domain-design.md), [dated Architecture](../../../2026-07-26-architecture-design.md) | 06.0 | `SOURCE_AUTHORITY_DECISION_RECEIPT`, semantic diff |
| `REQ-EFFECTIVE-POM` wrapper/reactor/test lifecycle | [Current Architecture §5~7](../../../architecture-design.md#5-recommended-maven-multi-module-tree), [dated Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계) | 06.0, 06.7 | Effective-POM digest, isolated `-am` bootstrap, Surefire/Failsafe XML |
| `REQ-EXACT-TEST-MANIFEST` canonical 89 methods | [Canonical Phase 06 §11](../../phases/phase-06-cow-alns-reproducibility.md#11-exact-test-plan) | 06.1~06.7 | `PHASE06-CANONICAL-REQUIRED-METHODS-V1`, exact hash/multiset receipt |
| `REQ-COW-ALNS` changed-route COW/independent bank | [Master §12](../../../master-design.md#12-candidate-state-cache와-rollback), `Q-ALG-02` | 06.1, 06.3 | `CowTrialIsolationTest.*`, `E-P06-COW` |
| `REQ-PAIR` request-level destroy/repair | [Master §11.4~11.6](../../../master-design.md#114-alns-step), [Domain §11](../../../2026-07-26-domain-design.md#11-alns-state와-operator) | 06.2 | Destroy/repair contract tests, `E-P06-ALNS` |
| `REQ-EVALUATOR-OWNER` full solution authority | Phase 06 review `F-P06-004`, Phase 05 blocker | 06.0, 06.3 | Cross-Phase compile/full-equality/corruption evidence |
| `REQ-EDITOR-OWNER` central pair edit | Phase 06 review `F-P06-003` | 06.0, 06.2 | Single-owner contract and pair edit tests |
| `REQ-STEP` completed-step transaction | [Architecture §3.4](../../../2026-07-26-architecture-design.md#34-alns-step-budget-계약) | 06.3 | State-machine/fault tests, `E-P06-ALNS` |
| `REQ-GUARD` guard before acceptance/best | [Master §11.4](../../../master-design.md#114-alns-step), review `F-P06-001` | 06.3 | Guard call-order/non-promotion methods |
| `REQ-SINGLE-PUBLISH` engine-owned atomic next state | Review `F-P06-006/011`, §8.3/§9.2 | 06.3 | Declaration↔constructor↔return mapping, Published/conflict/failure atomic actual-visibility matrix, publication count/race methods |
| `REQ-DISTINCT-SLOTS` same content, distinct handles | Domain §11, review `F-P06-010` | 06.1, 06.3 | Slot identity methods, `E-P06-COW` |
| `REQ-ADAPTIVE` finite deterministic learning | [Master §11.5](../../../master-design.md#115-destroy-repair와-adaptive-selection) | 06.4 | `AdaptiveOperatorSelectorTest.*` |
| `REQ-ACCEPTANCE` explicit HC/SA | Master §11.5, Phase 06 §9.4 | 06.4 | `AcceptancePolicyTest.*` |
| `REQ-RNG` namespaced logical streams | [Master §13.2](../../../master-design.md#132-strong-reproducibility-envelope) | 06.4, 06.6 | `SeedStreamOwnershipTest.*`, `E-P06-REPLAY` |
| `REQ-ATTEMPT-SEPARATION` platform attempt outside canonical identity | Architecture §3.6, review `F-P06-007` | 06.4, 06.6 | Retry AttemptId equality methods |
| `REQ-PORTFOLIO` every available 4×2 screen | `Q-ALG-01`, [Master §11.2~11.3](../../../master-design.md#112-현재-범위의-initial-solution-portfolio) | 06.5 | `Phase1ScreenerTest.*` |
| `REQ-TERMINATION` normal/exception separation | [Master §13.1](../../../master-design.md#131-종료-의미) | 06.3, 06.5 | `TerminationFaultTest.*` |
| `REQ-REPLAY-INDEPENDENCE` separate oracle | Phase 06 §10, Plan §8 | 06.6 | Row-by-row oracle + corruption tests |
| `REQ-NUMERIC-GATE` no hidden official values | `Q-BENCH-02`, Plan §14 | 06.0, 06.4, 06.5 | Omission binding tests, all manifests |
| `REQ-ARCH-DAG` solver→core only | [Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계) | 06.7 | `Phase06SolverArchitectureTest.*` |
| `REQ-SECURITY` safe trace identifiers | [Integrated §20](../../../architecture-domain-implementation-design.md#20-security와-tenant-boundary), review `F-P06-008` | 06.7 | `SolverTraceRedactionTest.*` |
| `REQ-PERFORMANCE` evidence before apply/undo | Master §12.3/§15.9 | 06.7 | `CowCopyWorkAccountingTest.*`, `E-P06-COW` |
| `REQ-HANDOFF` owned versioned schema, no verifier pull-forward | Master §14.1/§15.7, [Phase 07 §7](../../phases/phase-07-independent-verification-final-result.md#7-io-artifact-contract-identity와-lifecycle) | 06.0, 06.7 | Schema receipt, producer/consumer compatibility tests, `handsOffCandidateEvidenceAndReplayManifestOnly()` |
| `REQ-EVIDENCE-DAG` immutable review/receipt chain | [Plan §9](../../master-realization-plan.md#9-evidence-bundle-규칙) | 06.7 | Manifest/review/receipt digest validation |
| `REQ-C17-GATE` optional MIP after 14A | [README §6](../../README.md#6-phase-작업-순서) | 06.0, 06.7 | OR-Tools-free build, gate snapshot |

새 operator, state policy, random source, acceptance projection, numeric default,
termination 또는 public artifact field를 추가하려면 source, owner, WP, test와
evidence를 이 표에 먼저 연결한다.

## 17. 구현자가 마지막으로 확인할 판정표

| 질문 | Yes면 | No면 |
|---|---|---|
| Source authority decision receipt가 plain/dated 충돌을 닫았는가? | 선택된 source pin | Production 구현 금지 |
| Accepted predecessor와 두 residual owner 계약이 있는가? | WP-06.1 가능 | 문서/fixture 설계에서 정지 |
| Effective POM이 JUnit/Surefire/Failsafe와 zero-test failure를 닫았는가? | Fresh run 가능 | Test evidence 생성 금지 |
| Candidate 수정이 changed-route COW 안에만 있는가? | 계속 | State design rollback |
| Hard/stage guard가 acceptance보다 먼저인가? | 계속 | Step executor 수정 |
| Executor derivation, engine step와 run outcome이 exact type/constructor/termination mapping을 가지며 owner만 한 번 publish하고 conflict actual visibility를 truthful하게 fail-closed하는가? | 계속 | Transaction 경계 재설계 |
| Rejected만 completed이고 invalid/interrupted는 non-advance인가? | 계속 | Work accounting 수정 |
| Randomness가 logical purpose namespace에 소유되는가? | 계속 | Replay claim 금지 |
| Same envelope이 row-by-row independent oracle와 같은가? | 계속 | `E-P06-REPLAY` 무효 |
| Fresh exact 89-method hash와 architecture/security test가 green인가? | Evidence 봉인 | Acceptance 금지 |
| Phase 06→07 schema owner와 양쪽 mapping receipt가 있는가? | Handoff 검증 | Phase 07 전달 금지 |
| Independent review와 receipt가 있는가? | Phase 07 handoff 가능 | `ACCEPTED` 금지 |
| Official/open/gated/deferred 값을 발명하지 않았는가? | 완료 후보 | 해당 변경 제거 후 재검토 |

현재 checkout에서는 source-authority, predecessor/owner, effective-POM,
publication-owner와 handoff-schema receipt가 모두 없고 canonical 89 test source도
0개다. 따라서 이 가이드를 작성한 시점의 정확한 상태는
`IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES`,
`implementation NOT_STARTED`, `evidence NOT_PRODUCED`, Phase 06
`NOT_ACCEPTED`다.
