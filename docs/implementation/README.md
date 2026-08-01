# RPDPTW 구현 문서 지도

```yaml
document_set_status: DOCUMENTATION_COMPLETE_WITH_RESIDUAL_BLOCKERS
baseline_date: 2026-07-28
authority_alignment_date: 2026-07-31
plan_version_alignment: 1.3
source_authority: >
  USER_LOCKED for scope/map/win_poc/15-phase numbering;
  LIVE design meaning = APPROVED Master/Domain/Architecture
phase_c_status: COMPLETE
semantic_rebase_core3: AUTHORITY_ALIGNED_2026-07-31
core3_residual_phrasing_pass: 2026-08-01
inventory_platform_reframe: 2026-08-01  # AWS SFN+Lambda|ECS; GCP=legacy
semantic_rebase_phases: NOT_DONE
phase_count: 15
phase_documents: 15
phase_reviews: 15
implementation_accepted_phases: 0
implementation_direction_decision: ALNS_FIRST_BENCHMARK_BEFORE_OPTIONAL_MIP
direction_revision_task_id: 019fa901-8776-7f61-b467-a8c6595b970d
direction_overlay_contract_version: ALNS_FIRST_1.0
filename_slug_policy: KEEP_DISPLAY_SEPARATION  # §5.1; rename only with phase body rebase
execution_success_fixture: data/win_poc_case_floor.json
execution_success_status: NOT_RUN
current_design:
  master: docs/master-design.md  # APPROVED v1.1 (2026-07-31)
  domain: docs/domain-design.md  # APPROVED v1.2 (2026-07-31)
  architecture: docs/architecture-design.md  # APPROVED v3.4 (2026-07-31)
  normative_input: docs/deprecated/2026-07-30-design-interview-phase-a.md
```

## 0. 이 구현 작업의 최종 성공 기준

이 문서 세트에 따라 수행하는 **현재 구현 작업**의 사용자 고정 성공은
[win_poc_case_floor.json](../../data/win_poc_case_floor.json)을 실제 solver로 실행하고,
candidate solution verifier와 result-integrity verifier가 모두 `PASS`한 결과를 생성하여
사용자에게 보여주는 것이다.

세부 AND gate, 결과 필드와 재현 조건은
[Master Realization Plan §11.3](master-realization-plan.md#113-사용자-고정-실행-성공-dod)을
따른다. 현재 상태는 fixture migration만 완료된 `NOT_RUN`이며 solver 구현 성공을
주장하지 않는다.

**Master A7 다층 gate와 혼동하지 않는다.** win_poc 성공은 이 작업의 local e2e gate일 뿐,
다음을 자동 충족하지 않는다.

| Gate 종류 (Master §8.3 개요) | 이 세트에서의 위치 |
|---|---|
| 일반 발행 (verifier 2단 + 발행 계약) | Phase 07 + 발행 경로 |
| ALNS 기능·품질 / benchmark | Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT` |
| official Win 비교 | Phase 14B / `Q-BENCH-02` (OPEN) |
| application cutover | Phase 14B production authority |
| hybrid 활성화 | C-17 GATED + Phase 13 |

## 1. 읽기 순서

### 1.1 Current design authority (Phase B APPROVED)

1. [문서 지도](../README.md)
2. [Phase A 인터뷰 정리](../deprecated/2026-07-30-design-interview-phase-a.md) — 규범 입력 (A1–A12, D1·D2)
3. [Canonical Master](../master-design.md) (`APPROVED` v1.1) — 목표·범위·완료·결정·e2e·gate 개요
4. [Domain Design](../domain-design.md) (`APPROVED` v1.2) — 값·수식·정규화·travel·전파·평가·해·검증·결과 **의미**
5. [Architecture Design](../architecture-design.md) (`APPROVED` v3.4) — module/package/port/runtime **배치**

**용어 규칙 (Domain §1.2):** 주요 개념은 English first + 괄호 한국어  
예: `immutable solve snapshot (풀이용 문제 고정본)`, `SearchRequestBank (탐색 중 미배정 바구니)`.

### 1.2 이 구현 문서 세트

6. [Master Realization Plan](master-realization-plan.md) — Phase DAG, 계약 요약, DoD, gate
7. [Execution Progress and Results](execution-progress-and-results.md) — registry, status, residual blockers
8. 해당 Phase 상세·review 문서 (`phases/*`, `reviews/*`)

### 1.3 Historical frozen inputs (SUPERSEDED — 작성 스냅샷 only)

> **Phase C (O5) 완료:** 구 문서 경로를 `docs/deprecated/` 로 remap 했다.  
> **Core 3 authority alignment (2026-07-31):** 이 README / master plan / progress 의
> 권위·충돌·핵심 결정 서술을 APPROVED 3문서에 맞췄다.  
> **`phases/*` · `reviews/*` 본문 의미 rebase는 아직 하지 않았다.**  
> Phase 본문과 current 설계가 충돌하면 **항상 current APPROVED가 이긴다.**

9. [Historical Final Domain (2026-07-26)](../deprecated/2026-07-26-domain-design.md)
10. [Historical Final Architecture (2026-07-26)](../deprecated/2026-07-26-architecture-design.md)
11. [Historical integrated implementation design](../deprecated/architecture-domain-implementation-design.md)
12. [Historical open-questions registry](../deprecated/master-design-open-questions.md)
13. [2026-07-26 Master — historical cross-check only](../deprecated/2026-07-26-master-design.md)

`docs/codex/*` 는 repo에 없거나 역사 전용이다. 현재 authority·API 이름·완료 evidence로 사용하지 않는다.

## 2. 문서 지도

| 문서 | 소유하는 내용 | 소유하지 않는 내용 |
|---|---|---|
| [master-realization-plan.md](master-realization-plan.md) | Current inventory, target structure, Phase 00~14 DAG/critical path, phase contract 요약, test/evidence/DoD, gate와 traceability | 구현 완료 상태의 일상 갱신 |
| [execution-progress-and-results.md](execution-progress-and-results.md) | 문서 workflow, scheduler task registry, phase status, result, review와 remaining issue | Source 설계 의미 변경 |
| [README.md](README.md) | 읽기 순서, source authority, canonical phase/review link index와 naming rule | Phase 상세 구현 계약 |
| [session-prompts/phase-rebase/PROMPT.md](session-prompts/phase-rebase/PROMPT.md) | Phase 본문 rebase **공통 세션 프롬프트** (`PHASE=NN` 파라미터, 한 Phase씩) | 자동 구현 완료·status 승격 |
| `phases/phase-00-*.md`~`phase-14-*.md` | 해당 Phase의 구체 실행 unit (작성 당시 frozen 계약 본문) | 다른 Phase status; current design authority 재정의 |
| `reviews/phase-00-review.md`~`phase-14-review.md` | 해당 Phase entry/exit/evidence의 독립 review verdict | 구현자 self-claim만으로 status 승격 |

## 3. Source authority

### 3.1 충돌 순서 (MUST)

```text
1. 사용자 선언 (이 세트의 scope: 15 Phase map, 파일 규칙, win_poc 최종 e2e)
2. Phase A 인터뷰 정리 (A1–A12, D1·D2) — docs/deprecated/2026-07-30-design-interview-phase-a.md
3. current APPROVED: docs/master-design.md (v1.1)
4. current APPROVED: docs/domain-design.md (v1.2)
5. current APPROVED: docs/architecture-design.md (v3.4)
6. (historical only) deprecated open-questions / 2026-07-26 domain·architecture / integrated design
7. (cross-check only) deprecated 2026-07-26 master
```

**current ≠ frozen:** `phases/*` 본문이 아직 Phase B APPROVED 의미로 재작성되지 않았다면,  
**새 구현 착수·의미 판정 시 current APPROVED 3문서가 이긴다.**  
frozen 세트는 작성 당시 계약·evidence 추적용이며, D1·D2와 충돌하는 문장은 폐기한다.

### 3.2 핵심 결정 미러 (APPROVED Master)

| ID | 요지 | 구현 함의 |
|---|---|---|
| **D1** | 단일 고정 입력 계약. multi-version 스키마 병행 운영 없음. 레거시 → **adapter 하나** | Phase 01: versioned multi-schema 운영 금지 |
| **D2 / O1** | Worker/API compute = **Lambda \| ECS — OPEN**. 한쪽 단정 금지 | Phase 11: AWS **reference**; **Lambda only** 단정 금지 |
| **A7** | 완료 = gate + evidence AND | 문서 작성 ≠ 솔버 완료 |
| **A8** | ALNS-first; route pool/MIP = **C-17 GATED TARGET** | 14A receipt 전 Phase 13 착수 금지 |
| **A9** | current ≠ target; placeholder ≠ 완료 evidence | 구현 0/15, win_poc NOT_RUN; **GCP code ≠ AWS target** |
| **A10** | object storage + durable orchestration + worker **논리** 분리. 클라우드 조립 ≠ 알고리즘 완료 ≠ cutover | 저장 S3; orchestration reference = **Step Functions**; compute = Lambda\|ECS |
| **A11** | profile 격리; core 고객명 분기 금지 | Phase 04 |

### 3.3 저장·런타임 (Architecture v3.4)

| 축 | 규범 |
|---|---|
| 클라우드 reference | **AWS** (GCP Cloud Run/Workflows/GCS 경로 유지·확장 아님) |
| 저장 | **S3 only**. 관계형 DB **MUST NOT**. Redis **MUST NOT** |
| 로컬 통합 | **LocalStack S3** + 동일 `adapters/s3` (단위 테스트 인메모리 fake 허용) |
| durable orchestration | **AWS Step Functions** (application 각본은 provider-neutral; Domain 점수 소유 아님) |
| REST 접수 | validation → input S3 저장 → **HTTP 200 + s3 key** (동기). 풀이 본체는 worker (비동기) |
| compute | **Lambda \| ECS — O1 OPEN** (reference 플랫폼 안에서의 제품 선택) |
| OR-Tools / MIP | **`backends/*` only** (adapters 자리 아님). C-17 GATED, default off |
| production | reference 선택 ≠ cutover 승인 (A9, A10) |
| current code | `src/` · `gcp/` = **GCP legacy placeholder**. inventory → [plan §3](master-realization-plan.md#3-2026-07-28-current-state-inventory) |

> **Historical note:** 구 질문 등록부의 `Q-INFRA-01 RESOLVED = S3 + Step Functions + Lambda`
> 집계와 “Lambda only RESOLVED” 문구는 **deprecated 스냅샷**이다. Master에 그 RESOLVED
> 문구는 없다.  
> **현재 규범:** 저장 = S3 only (MUST). reference platform = **AWS S3 + Step Functions +
> (Lambda \| ECS)**. **OPEN은 Lambda vs ECS 제품 선택(O1/D2)뿐**이다. GCP를 target으로
> 읽지 않는다. production cutover는 Phase 14 gate.

### 3.4 기타 유지 항목

- `win_poc_case_floor.json`은 사용자 승인 `D/U FLOOR` migration을 거친 이 구현 작업의 최종 실행 fixture다.
- `Q-BENCH-02` 공식 수치와 production authority는 별도 Phase 14B gate로 남는다.
- 구현 방향 **ALNS-first**: Phase 05 준비 → 06 ALNS → 07 검증 → 08 local → 14A benchmark.
- Route pool/MIP는 `C-17 GATED TARGET`. 유효한 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`와
  나머지 승인 전 Phase 13 착수·기본 활성화 금지.
- Gate 열릴 경우 implementation **proposed** backend = Google OR-Tools direct Java CP-SAT
  (Master/Domain이 backend 제품을 확정한 것이 아님. 별도 승인·version/SBOM 등 필요).
- Config `hybridMip.enabled` (등) default **false** 이며, config true 만으로 C-17 승인을 우회하지 않는다.
- `Q-VAR-01`은 `DEFERRED`이며 restart evidence 전 질문·활성화하지 않는다.
- Initial portfolio 개수·4×2 구성·ALNS step 수치 등은 Domain **OPEN/예시**다.
  Phase 본문의 수치는 experiment/test-only로 취급하며 Domain MUST로 승격하지 않는다.

### 3.5 Implementation-direction overlay version

현재 구현 계약의 합성 identity는 다음과 같다.

```text
phase/review의 base document_version 또는 UNVERSIONED_BASE
+ direction_overlay_contract_version = ALNS_FIRST_1.0
+ direction_revision_task_id = 019fa901-8776-7f61-b467-a8c6595b970d
```

위 task ID를 가진 Phase/review는 base version만으로 ALNS-first revision 전 계약을
가리키지 않는다. Base `document_version`이 없는 Phase 00~02도
`UNVERSIONED_BASE + ALNS_FIRST_1.0`으로 식별한다. Phase 12는 C-17 restart gate가
직접 변경됐으므로 base version도 v1.3으로 올렸다.

Overlay version과 base version은 문서 계약의 provenance일 뿐 implementation,
evidence, acceptance 또는 production authority가 아니다.

## 4. Canonical Phase와 review index

상태 표기:

- **actual**: 파일이 실제 존재함.
- **reviewed**: 독립 review 문서가 존재하고 대상 문서에 안전 교정이 반영됨. 구현 완료를 뜻하지 않음.
- **gated**: entry approval 전 작업 시작 금지.
- **deferred**: restart condition 전 질문·활성화 금지.
- **frozen body**: Phase 상세 본문은 2026-07-26 계열 작성 스냅샷. 착수 시 APPROVED 의미 우선.

현재 Phase/review 링크는 모두 **actual/reviewed**다. Review verdict와 구현 entry gate는
[Execution Progress and Results](execution-progress-and-results.md)의 registry를 따른다.

| Phase | 구현 주제 (English first) | 상세 문서 | Review |
|---:|---|---|---|
| 00 | Build / architecture skeleton | [actual](phases/phase-00-build-architecture-skeleton.md) | [reviewed](reviews/phase-00-review.md) |
| 01 | Fixed input contract + normalization (D1) | [actual](phases/phase-01-canonical-input-normalization.md) | [reviewed](reviews/phase-01-review.md) |
| 02 | Prepared travel + immutable solve snapshot | [actual](phases/phase-02-prepared-travel-immutable-problem.md) | [reviewed](reviews/phase-02-review.md) |
| 03 | Route propagation + evaluation kernel | [actual](phases/phase-03-route-propagation-evaluation-kernel.md) | [reviewed](reviews/phase-03-review.md) |
| 04 | Profiles / capabilities (A11) | [actual](phases/phase-04-capabilities-customer-profiles.md) | [reviewed](reviews/phase-04-review.md) |
| 05 | Pair insertion + initial portfolio | [actual](phases/phase-05-pair-insertion-initial-portfolio.md) | [reviewed](reviews/phase-05-review.md) |
| 06 | COW ALNS + reproducibility | [actual](phases/phase-06-cow-alns-reproducibility.md) | [reviewed](reviews/phase-06-review.md) |
| 07 | Independent verification + publishable result | [actual](phases/phase-07-independent-verification-final-result.md) | [reviewed](reviews/phase-07-review.md) |
| 08 | Application ports + local runtime | [actual](phases/phase-08-application-ports-local-runtime.md) | [reviewed](reviews/phase-08-review.md) |
| 09 | Object storage (S3 only, no DB/Redis) | [actual](phases/phase-09-object-storage-no-database.md) | [reviewed](reviews/phase-09-review.md) |
| 10 | Provider-neutral coordinator | [actual](phases/phase-10-provider-neutral-coordinator.md) | [reviewed](reviews/phase-10-review.md) |
| 11 | AWS reference distribution (compute OPEN) | [actual](phases/phase-11-aws-reference-distribution.md) | [reviewed](reviews/phase-11-review.md) |
| 12 | Provider substitution | [actual/gated](phases/phase-12-provider-substitution.md) | [reviewed/gated](reviews/phase-12-review.md) |
| 13 | Optional hybrid — `C-17 gated` | [actual/gated](phases/phase-13-optional-hybrid-route-selection.md) | [reviewed/gated](reviews/phase-13-review.md) |
| 14 | 14A ALNS benchmark / 14B official cutover | [actual/gated](phases/phase-14-official-calibration-cutover.md) | [reviewed/gated](reviews/phase-14-review.md) |

Phase 14는 하나의 상세/review 파일을 사용하지만
[Execution Progress and Results §2.2](execution-progress-and-results.md#22-구현-phase-상태)와
registry에서는 14A benchmark 상태/receipt와 14B production/cutover 상태를 별도로
기록한다. 14B production authority가 없다는 이유로 14A를 차단하지 않는다.

## 5. Filename 규칙

상세 문서:

```text
docs/implementation/phases/phase-00-build-architecture-skeleton.md
...
docs/implementation/phases/phase-14-official-calibration-cutover.md
```

Review 문서:

```text
docs/implementation/reviews/phase-00-review.md
...
docs/implementation/reviews/phase-14-review.md
```

규칙:

1. Phase 번호는 두 자리 `00`~`14`를 사용한다.
2. 상세 slug는 §4 표에 적힌 canonical 이름을 그대로 사용한다.
3. Review filename은 `phase-NN-review.md`로 고정한다.
4. 같은 Phase의 `-v2`, `-final`, 날짜 복제 파일을 만들지 않는다. Version/status는 문서 metadata와 git history로 관리한다.
5. 상세/review 파일은 모두 actual이며 filename은 바꾸지 않는다 (이번 alignment도 본문 미수정).
6. 새 file을 만들기 전 [Execution Progress and Results](execution-progress-and-results.md)의 scheduler task ID와 entry gate를 확인한다.
7. `current`, `latest`, `actual`로 인용하는 Phase version은 대상 문서 metadata와
   일치시킨다. 과거 version을 해소 증거나 authoring snapshot으로 인용할 때는
   `historical`, `introduced in` 또는 동등한 문맥을 명시한다.

### 5.1 Filename slug vs Domain 공식 용어 (정책 — KEEP + 표시 분리)

**결정 (채택, 2026-08-01):** Phase 상세 **filename slug는 안정 식별자**다.  
Domain 공식 용어와 철자가 달라도 **일상 rename하지 않는다.**

| 층 | 역할 | 예 (Phase 02) |
|---|---|---|
| **slug / path** | 링크·registry·fingerprint용 고정 ID | `phase-02-prepared-travel-immutable-problem.md` |
| **표시·계약 용어** | 읽기·구현·리뷰 시 쓰는 의미 이름 | `immutable solve snapshot` (Domain §7) |
| **의미 권위** | 충돌 시 승자 | APPROVED Domain/Master — **slug가 의미를 정의하지 않음** |

규칙 보충:

1. §4 index **「구현 주제」열**은 Domain English-first 용어를 쓴다. 링크 href는 canonical slug를 유지한다.
2. slug에 남은 옛 단어(예: `immutable-problem`)는 **historical path label**이다.  
   `ProblemInstance` 단독 freeze나 multi-version 입력 등 구 의미를 부활시키지 않는다.
3. 사람·에이전트는 파일명만으로 Phase 범위를 축소 해석하지 않는다.  
   해당 Phase 계약 + Domain/Architecture 절을 본다.
4. **Rename은 예외**다. 허용 조건은 동시에 다음을 만족할 때만이다.  
   - 해당 `phases/*` (및 필요 시 `reviews/*`) **의미 rebase와 같은 변경 단위**  
   - README §4 canonical 표·plan §5·progress registry 링크 **일괄 갱신**  
   - 구 slug는 최소 그 변경의 문서 note에 alias로 남김  
5. 지금(core 3 only / phase body stale) **rename하지 않는다.** 오독 완화는 본 절·Phase overlay·표시 용어로 한다.

검색 alias (Phase 02):

```text
immutable problem          → 파일 slug / 과거 표기
immutable solve snapshot   → Domain 공식 용어 (의미)
ProblemInstance            → snapshot 구성 요소(문제 본체) proposed 이름일 수 있음; freeze 단위 전체 아님
```

## 6. Phase 작업 순서

Master 파이프라인 (의미 개요):

```text
adapter? → canonical → normalize → travel → profile bind
  → immutable solve snapshot
  → initial portfolio → phase-1 screen ALNS → phase-2 ALNS
  → [optional gated] HybridPhase
  → candidate solution verifier → finalization
  → result-integrity verifier → publishable result
```

ALNS 구현·검증·benchmark qualification critical path:

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 14A
```

AWS ALNS-only production 후보:

```text
08 → 09 → 10 → 11
14A + 11 → 14B
```

조건부 branch:

```text
10 → 12  # 승인된 provider substitution
14A ALNS benchmark acceptance + C-17 approvals → 13  # optional hybrid/MIP
13 → 14B  # official hybrid manifest를 명시적으로 선택한 경우에만
```

### 6.1 ALNS benchmark acceptance gate

Phase 14A의 immutable evidence에는 dataset/fixture fingerprint, seed/repeat policy,
hardware/runtime fingerprint, correctness oracle, candidate/result verifier 결과,
objective/quality 비교, timeout/resource budget, variance/reproducibility, independent
review와 post-review acceptance receipt가 모두 있어야 한다. Corpus, threshold,
repeat 수, budget, 허용 variance와 provider/backend version은 승인 전
`OPEN — EXPERIMENT_REQUIRED` 또는 `GATED`다.

`win_poc_case_floor.json` 실행 성공은 중요한 end-to-end correctness/replay evidence지만,
승인된 corpus/protocol/quality·performance 기준이 없는 상태에서 그 자체만으로
`ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`를 만들지는 않는다.

### 6.2 Residual blockers (문서 alignment 관점)

이번 core 3 authority alignment **이후에도** 남은 blocker:

1. Phase 00~08 actual implementation acceptance (0/15)
2. `phases/*` / `reviews/*` 본문의 Phase B 의미 rebase (미실시)
3. ALNS benchmark corpus/protocol/criteria + immutable acceptance receipt
4. `C-17` 및 backend/license/native/security/operations/cost approvals
5. Phase 09~11 runtime evidence; LocalStack 통합 타깃 반영 (phase 본문)
6. Official values (`Q-BENCH-02`)와 production authority
7. compute O1 (Lambda \| ECS) 제품 결정 — **OPEN 유지**
8. Phase filename: **KEEP + 표시 분리** 채택 (§5.1). rename은 phase 본문 rebase 때 예외적으로만.

각 Phase 작업자는 다음 순서로 읽는다.

1. 이 README의 authority와 canonical filename.
2. [Master Realization Plan](master-realization-plan.md)의 해당 Phase 계약 요약.
3. 해당 actual Phase 상세 문서 — **충돌 시 APPROVED Domain/Architecture 우선**.
4. 관련 source authority section (Master → Domain → Architecture).
5. [Execution Progress and Results](execution-progress-and-results.md)의 scheduler task/entry 상태.
6. 구현과 evidence bundle.
7. 해당 Phase review 문서와 scheduler verdict.

## 7. Progress와 review 규칙

- 문서 작성률과 구현 완료율을 섞지 않는다.
- Source/test/deployment file 존재는 Phase completion이 아니다.
- 구현 Phase는 exit evidence와 review가 모두 통과해야 `ACCEPTED`다.
- 구현자와 reviewer는 evidence/verdict를 제출하고, authoritative task registry/status/result summary는 총괄 스케줄러만 갱신한다.
- Scheduler task ID를 전달받지 못했으면 `TBD`로 남기며 임의 ID를 만들지 않는다.
- OPEN/EXPERIMENT_REQUIRED/GATED/deferred를 임의 수치·default·완료 상태로 바꾸지 않는다.
- 모든 Phase/review 상대 링크는 target과 fragment anchor를 정적으로 검사한다.
- Authority alignment(문서 권위 정렬)는 구현 acceptance를 승격하지 않는다.
