# Phase 00 사람용 구현 가이드 — Build와 architecture skeleton

```yaml
phase: "00 / 14"
canonical_phase_count: 15
canonical_phase_range: "00..14"
document_kind: HUMAN_IMPLEMENTATION_GUIDE
document_status: CORRECTED_ROUND_01_PENDING_INDEPENDENT_REVIEW
head_baseline_implementation_status: NOT_STARTED
live_scheduler_status_at_correction: CHANGES_REQUIRED_FIX_01_IN_PROGRESS
phase_acceptance_status_at_correction: REJECTED_PENDING_FIX_01_REGENERATION
evidence_status_at_correction: DEFECTIVE_BUNDLE_SUPERSEDED
snapshot_date: 2026-07-29
snapshot_checkout:
  repository: /Users/brown/workspace/ro-next
  branch: codex-implementation
  commit: 7cc890ee1d0805df5ae14b633127fade4f978639
  head_tree: a63fa3b93ca206298ac6d467e02e1f3503cee30e
live_drift_snapshot_before_correction:
  observed_at: 2026-07-29T02:02:30+09:00
  approval: UNCOMMITTED_UNAPPROVED
  status_porcelain_sha256: 7087b4b7ce5505dc6e8c2774f39af185b859c9651d0d9541ba88b6dd5ff54f84
  tracked_diff_sha256: 500c4ef4e09165f685d0b446b97b4a5352e863dd1c08c6de82b89a1aab212836
  untracked_content_manifest_sha256: a02ef644b8ecb791a2663811ee1151305cb7f18d5f2bc7aaf0ffc5699e9be2fa
  implementation_inventory_sha256: 66e6529a5eb5276f01d137718b3950c18eb89664da5dc000563b04e94f3c0292
previous_phase: NONE
next_phase: "01 — canonical input와 normalization"
expected_reader:
  - Java 기초가 있는 구현자
  - CVRPTW를 구현해 보았지만 이 RPDPTW 설계와 repository는 처음인 구현자
owners:
  implementation: Build·Architecture owner
  legacy_characterization: Application·Platform owner
  evidence_packaging: Build·Quality owner
  independent_review: Architecture reviewer + Build reviewer
  acceptance_and_status: Scheduler/phase acceptance owner
  consumer: Phase 01 Domain·Input owner
public_api_status: NOT_DEFINED
optional_backend_status: GATED
official_benchmark_values: OPEN_EXPERIMENT_REQUIRED
production_authority: GATED
```

> 이 문서는 사람이 Phase 00을 구현하기 위한 교육형 작업 지시서다. 구현 완료 보고서나
> 승인 영수증이 아니다. Metadata는 같은 `HEAD`의 두 층을 분리한다. `head_baseline_*`은
> commit/tree의 과거 baseline이고, `live_*`는 교정 직전 dirty checkout과 공식 scheduler의
> 미커밋·미승인 관측값이다. Live 파일 존재나 local green은 acceptance가 아니다.
> [공식 실행 현황](../../execution-progress-and-results.md)이 이후 바뀌면 exact status와
> `updated_at`을 다시 읽고 scheduler owner에게 합류하거나 작업을 멈춘다.

## 1. 이 가이드의 읽는 법과 안전 표기

Phase 00의 산출물은 기능이 아니라 **기능을 안전하게 담을 빌드 경계**다. 빈 class를 많이
만들거나 현재 placeholder를 새 package로 옮기는 것만으로는 완료가 아니다. 성공은 다음 세
질문에 모두 “예”라고 답할 때 성립한다.

1. 현재 동작을 잃지 않고 legacy 경계 안에 격리했는가?
2. 잘못된 dependency가 들어오면 기본 `verify`가 실제로 실패하는가?
3. Phase 01이 domain 의미를 스스로 설계할 공간을 남기면서도 build와 package 경계는
   모호하지 않은가?

이 문서에서 다음 표기를 사용한다.

| 표기 | 의미 | 사람이 취할 행동 |
|---|---|---|
| **CONTRACT** | 권위 문서에서 이미 확정된 불변조건 | 구현과 review에서 변경하지 않는다 |
| **PROPOSED** | 내부 이름·signature 후보이며 아직 public contract가 아님 | review에서 바꿀 수 있고 확정 전 외부에 공개하지 않는다 |
| **TEST-ONLY** | test source나 test resource에서만 허용 | production default와 main artifact에 포함하지 않는다 |
| **CURRENT** | 이 문서 snapshot에서 실제로 관측한 사실 | checkout이 바뀌면 재조사한다 |
| **FUTURE** | 해당 파일/module을 만든 뒤에만 실행 가능한 명령 | 지금 실패한다고 repository 결함으로 판정하지 않는다 |
| **OPEN** | owner와 근거가 없으면 닫을 수 없는 질문 | 숫자·정책·API를 임의로 정하지 않는다 |
| **GATED** | 선행 evidence와 사람 승인 전 구현 또는 활성화 금지 | gate가 닫힌 동안 dependency와 fake provider도 만들지 않는다 |
| **DEFERRED** | restart condition 전 논의·구현 범위 밖 | “미리 준비”한다는 이유로 skeleton을 만들지 않는다 |

## 2. Source authority와 fingerprint

### 2.1 충돌 해석 규칙

구현자가 여러 설계 문서의 날짜나 표현 차이를 발견하면 문서의 역할부터 구분한다.

1. 최신 [canonical Master](../../../master-design.md)의 결정과 불변조건을 우선한다.
   질문 상태와 owner는
   [open-question register](../../../master-design-open-questions.md)의 exact `Q-*` 행을 따른다.
2. [현재 Domain 지도](../../../domain-design.md)와
   [현재 Architecture 지도](../../../architecture-design.md)는 최신 전체-product 책임과
   package/module 방향을 보여 주는 상위 `REVIEW` 지도다. 구현 완료나 Phase 00 acceptance를
   뜻하지 않으며, 그 안의 `RECOMMENDED` Java 이름을 승인된 public API로 바꾸지 않는다.
3. [2026-07-26 Domain 고정 입력](../../../2026-07-26-domain-design.md)과
   [2026-07-26 Architecture 고정 입력](../../../2026-07-26-architecture-design.md)은
   사용자가 고정한 dated source다. 현재 지도와 달라진 부분을 숨기지 않고 drift로 기록한다.
4. [통합 구현 설계](../../../architecture-domain-implementation-design.md),
   [Implementation README](../../README.md), [Master Realization Plan](../../master-realization-plan.md),
   [Phase 00 원본](../../phases/phase-00-build-architecture-skeleton.md)과
   [원본 review](../../reviews/phase-00-review.md)는 Phase 00의 실행·evidence·교정 baseline이다.
   특히 concrete Phase 00 tree는 reviewed core Phase의 최소 skeleton을 따른다.
5. 현재 상위 지도의 profile별 module 추천과 implementation baseline의
   `rpdptw-capabilities` + 단일 `rpdptw-profile-catalog`처럼 구체 배치가 충돌하면 이
   guide가 임의로 결정하지 않는다. H3에서 차이를 승인하고, 최종 **accepted Phase 00
   coordinate/package manifest**를 source of truth로 삼는다. 승인 전 이름은 **PROPOSED**다.
6. [2026-07-26 Master Design](../../../2026-07-26-master-design.md)은 historical
   cross-check일 뿐 현재 결정의 authority가 아니다. `docs/codex/`도 현재 계약의 출처로
   사용하지 않는다.

Final Domain/Architecture의 날짜가 있는 일부 질문 집계와 `Q-INFRA-01` 표시는 최신
canonical Master와 register에 의해 대체되었다. 이 snapshot의 등록부 상태는
**26 RESOLVED / 1 OPEN / 1 DEFERRED**다. `Q-INFRA-01`은 AWS target/reference 선택으로
해결되었지만, AWS 구현과 production cutover가 승인되었다는 뜻은 아니다.

### 2.2 읽은 source section과 검증 가능한 fingerprint

아래 SHA-256은 이 문서 작성 시 읽은 file bytes의 fingerprint다. 구현 착수 시 다시 계산해
다르면 관련 section을 재검토한다.

| 권위 입력 | 직접 적용한 heading/section | SHA-256 |
|---|---|---|
| [Master Design](../../../master-design.md) | §1.1~§1.5, §2.4, §3, §4.1~§4.7, §5~§8, §13~§17 | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` |
| [현재 Domain 지도](../../../domain-design.md) | metadata/§1 authority, §3/§3.1 current responsibility/package map | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` |
| [현재 Architecture 지도](../../../architecture-design.md) | metadata/§1 authority, §5~§6 current recommended tree/DAG/package map | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` |
| [Final Domain](../../../2026-07-26-domain-design.md) | §1, §2 RPDPTW primer, §3, §7~§8, §17~§18 | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` |
| [Final Architecture](../../../2026-07-26-architecture-design.md) | §1.2~§1.5, §2.1~§2.7, §5.6, §6.1~§6.5 | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` |
| [Architecture-domain implementation design](../../../architecture-domain-implementation-design.md) | §2 전체 순서, §3 module/DAG, §4 Phase 0, §22~§25, §27~§28 | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` |
| [Open questions](../../../master-design-open-questions.md) | §1~§4; `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` exact rows | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` |
| [Implementation README](../../README.md) | §1~§7, canonical Phase/review index와 critical path | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` |
| [Master Realization Plan](../../master-realization-plan.md) | §1~§4, §6~§7 Phase 00/01, §8~§15 evidence/DoD | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` |
| [Execution progress](../../execution-progress-and-results.md) | `HEAD` baseline과 live §10.1 scheduler row를 분리 | `HEAD`: `37f1a8a0ffad1e9614bd54d2b2444739fb83d0951bff73f2a2465ab54a3e8895`; live `2026-07-29T02:02:30+09:00`: `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` |
| [Phase 00 원본](../../phases/phase-00-build-architecture-skeleton.md) | metadata, §2~§6, WP00-0~5, test/evidence/exit/rollback | `0ad01e21a94ac543486a53c0ed0a256b4137e673bbae1be4d959ebc46dcefd27` |
| [Phase 00 독립 review](../../reviews/phase-00-review.md) | verdict와 P00-R-001~006, residual blocker | `db4f1f8c4d178c99d82597085155a1fbfac223659944f3b092e66ae5b7f6fed8` |
| [이 guide의 독립 review](../reviews/phase-00-review.md) | 전체; `P00-HG-R-001`~`P00-HG-R-006` | `63f733a5b6f61e41ea3befa4c8340dbd43f9755f4dc6fbb9a17377543e99d332` |
| [Phase 01 원본](../../phases/phase-01-canonical-input-normalization.md) | entry gate, Phase 00 소비 artifact, evidence contract | `67e078a058753335ae823bbec815b3628f212d4593dfce9ee0db39cc02000324` |
| [Phase 01 review](../../reviews/phase-01-review.md) | Phase 00 blocked entry와 document-only review 의미 | `4644e7a7ef30f82368eea167e4f70197f4a1380a808c8066f37036608561f27f` |

Source fingerprint 재계산 예:

```bash
shasum -a 256 \
  docs/master-design.md \
  docs/domain-design.md \
  docs/architecture-design.md \
  docs/2026-07-26-domain-design.md \
  docs/2026-07-26-architecture-design.md \
  docs/architecture-domain-implementation-design.md \
  docs/master-design-open-questions.md \
  docs/implementation/README.md \
  docs/implementation/master-realization-plan.md \
  docs/implementation/execution-progress-and-results.md \
  docs/implementation/phases/phase-00-build-architecture-skeleton.md \
  docs/implementation/reviews/phase-00-review.md \
  docs/implementation/human-guides/reviews/phase-00-review.md \
  docs/implementation/phases/phase-01-canonical-input-normalization.md \
  docs/implementation/reviews/phase-01-review.md
```

## 3. 큰 그림: 왜 Phase 00이 먼저인가

### 3.1 제품과 domain 배경

이 프로젝트는 단순히 “차량마다 고객을 배치한다”는 CVRPTW solver가 아니다. 주문이
pickup-delivery request로 해석되고, 두 visit의 동일 차량·선후 관계·원자성이 route 편집의
기본 단위가 되는 RPDPTW를 다룬다. Solver가 만든 candidate는 독립 verifier가 다시 계산해
통과해야 하고, 최종 결과도 별도 gate를 통과해야 publish할 수 있다. 고객별 차이는
customer-specific JAR이나 `if (customerName)` 분기가 아니라 재사용 capability와
versioned profile data로 조립한다.

이 구조에서 module dependency는 코드 정리 취향이 아니다. 예를 들어 verifier가 solver의
cache나 search class를 읽을 수 있으면 “독립 재검증”이 사실상 같은 계산을 두 번 호출하는
것이 된다. Cloud SDK가 core에 들어오면 local ALNS와 provider substitution이 provider
runtime에 종속된다. Phase 00은 이런 오류를 사람이 review에서 발견하기 전에 build가 막도록
architecture skeleton을 만든다.

### 3.2 canonical 15 Phase와 현재 위치

Phase 번호는 **00부터 14까지 총 15개**다. Phase 14를 두 gate로 나누어 설명해도 canonical
Phase 수가 16개가 되는 것은 아니다.

| Phase | 핵심 producer artifact | 주요 consumer |
|---|---|---|
| **00 — Build와 architecture skeleton** | Reactor, stable module 경계, architecture guard, legacy evidence | Phase 01 및 모든 후속 Phase |
| 01 — Canonical input와 normalization | Exact normalized input와 typed error | Phase 02 |
| 02 — Prepared travel과 immutable problem | Complete directed travel, immutable problem | Phase 03/05/07 |
| 03 — Route propagation/evaluation kernel | Cache-free facts, hard/metric/score 경계 | Phase 04/05/07 |
| 04 — Capabilities와 customer profiles | Immutable bound profile와 registry closure | Phase 05/07 |
| 05 — Pair insertion과 initial portfolio | Stable snapshot/bank, exact insertion, independent candidates | Phase 06 |
| 06 — COW ALNS와 reproducibility | Verified-form candidate 전 단계, replay trace | Phase 07/08/10/14A |
| 07 — Independent verification과 final result | Candidate/result 두 verifier gate | Phase 08/10/11/14A |
| 08 — Application ports와 local runtime | Provider-neutral ports, local E2E | Phase 09/10 |
| 09 — DB 없는 object storage | Immutable artifact + authoritative CAS | Phase 10/11 |
| 10 — Provider-neutral coordinator | Declared completeness와 deterministic fan-in | Phase 11/12/14B |
| 11 — AWS reference distribution | S3/Step Functions/Lambda parity evidence | Phase 14B |
| 12 — Provider substitution | 동등 port contract의 대체 provider 절차 | 필요 시 migration/cutover |
| 13 — Optional hybrid route selection | 별도 gated route pool/exact projection/backend evidence | Official hybrid를 선택한 Phase 14B |
| 14 — Official calibration/cutover | 14A ALNS benchmark receipt, 14B official manifest/cutover authority | Production |

기본 critical path는 `00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 14A`다.
AWS production path는 `08 → 09 → 10 → 11`을 더 거쳐 `14B`로 합류한다.

Phase 13은 일반 후속 단계가 아니다. 다음 둘을 모두 얻기 전에는 시작하지 않는다.

- Phase 14A의 유효한 `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`
- `C-17`의 scope/trigger/backend/licensing/acceptance 승인

Phase 13은 ALNS-only benchmark acceptance나 ALNS-only cutover의 선행조건이 아니다.
Phase 14B는 Phase 11 acceptance, 유효한 14A receipt, 공식 `Q-BENCH-02` 값, workload·security·
retention·retry·recovery·performance·cost와 **명시적 production authority** 없이 열리지
않는다. Phase 00에서 provider skeleton이나 임의 benchmark default를 만들어 이 gate를
우회하지 않는다.

### 3.3 Phase 00 앞뒤 producer-consumer 계약

Phase 00에는 이전 구현 Phase가 없다. 대신 다음 세 입력군을 producer로 취급한다.

| Producer | Phase 00이 소비하는 것 | 소비하면 안 되는 것 |
|---|---|---|
| Canonical 문서 | 확정된 domain/architecture 불변조건, module DAG, gate | historical 문서의 오래된 질문 상태 |
| 현재 repository | 실제 POM, source/test/deployment behavior, resolved dependency와 경고 | 비어 있는 directory를 구현 완료로 해석한 주장 |
| 사람 승인 | implementation scope, legacy convergence/collision 정책, reviewer assignment | 침묵을 승인으로 간주한 default |

Phase 00이 Phase 01에 주는 것은 domain class가 아니라 다음 세 evidence와 build contract다.

- `E-P00-BUILD`: optional-backend-free reactor와 재현 가능한 build provenance
- `E-P00-ARCH`: 허용/금지 dependency가 positive·negative fixture에서 동작한 증거
- `E-P00-LEGACY`: 현재 placeholder의 success·failure behavior와 격리 증거

문서를 잘 썼거나 root `mvn verify`가 한 번 통과한 것만으로 이 artifact가 생기지 않는다.
구현 evidence manifest, 독립 review report, post-review acceptance receipt가 같은 immutable
source snapshot을 가리켜야 Phase 01 entry가 열린다.

## 4. RPDPTW domain primer와 용어집

### 4.1 CVRPTW 경험자가 먼저 버려야 할 가정

CVRPTW에서 customer 하나를 route의 visit 하나로 생각하고 relocate/swap하는 습관은 이
프로젝트에서 충분하지 않다. RPDPTW의 real pickup-delivery request는 두 visit가 하나의
원자 단위다. pickup만 옮기거나 delivery만 bank에 남기는 intermediate draft는 만들 수
있지만 stable state로 공개하거나 검증 대상으로 넘길 수 없다.

또한 “delivery-only”는 pickup node가 하나 더 있다는 뜻이 아니다. 시작 시 차량에 이미
적재된 물량으로 load를 초기화하고 실제 delivery visit만 travel/stop/service에 기여한다.
논리 pickup을 물리 visit로 만들어 거리와 정차를 더하면 잘못된 모델이다.

### 4.2 identity와 lifecycle

| 용어 | 이 프로젝트에서의 identity | 자주 생기는 오류 |
|---|---|---|
| External order | 외부 schema의 주문 표현 | Domain request와 같은 class로 사용 |
| Request | Solver가 원자적으로 배치하는 pickup-delivery 쌍 또는 delivery-only 작업 | 두 node를 독립 작업으로 취급 |
| Node definition | Visit가 참조하는 immutable 의미/위치 정의 | Route 안의 occurrence와 동일시 |
| Visit occurrence | 특정 route sequence에 실제 놓인 node occurrence | 같은 node ID가 어디에 있는지 authority로 사용 |
| Route | 하나의 **구체 vehicle**이 소유하는 ordered visit sequence | 차량 type만 연결하거나 vehicle을 나중에 고름 |
| Request bank | 현재 어느 route에도 complete하게 배치되지 않은 request의 authority | 부분 pair를 stable state로 보존 |
| TrialDraft | remove/insert를 시험하는 동안에만 허용되는 불안정 상태 | solver/verifier/public API로 노출 |
| SearchSnapshot | route와 bank의 XOR, pair complete 등 stable invariant를 만족하는 immutable snapshot | mutable cache를 authority로 사용 |
| PreparedTravel | solve 전에 완성·동결된 directed location pair와 vehicle별 time | runtime symmetric/reverse/lazy fallback |
| BoundProfile | exact customer/profile/version과 capability closure를 immutable하게 bind한 결과 | customer name 분기나 latest fallback |
| Candidate verifier | Solver candidate를 authority data에서 독립 재계산하는 첫 gate | solver cache를 신뢰 |
| Result verifier | audit/outcome/summary/payload까지 다시 검사하는 둘째 gate | 첫 gate만 통과하고 publish |

### 4.3 stable invariant

후속 Phase가 구현해야 하지만 Phase 00의 module 방향이 보호해야 하는 핵심 불변조건은 다음과
같다.

```text
for every request r in a stable state:
    exactlyOne(
        completeOnExactlyOneConcreteVehicleRoute(r),
        presentExactlyOnceInRequestBank(r)
    )

if r is a real pickup-delivery pair and is routed:
    vehicle(pickup(r)) == vehicle(delivery(r))
    position(pickup(r)) < position(delivery(r))
    count(pickup(r)) == 1
    count(delivery(r)) == 1

if r is delivery-only and is routed:
    initialLoad(vehicle(r)) includes demand(r)
    physicalVisitCount(r) == 1
```

Route sequence·route ownership·concrete vehicle·request bank가 authority다. Load/time/metric/search
cache는 언제든 authority에서 재계산할 수 있는 derived state다. 그러므로 verifier는 solver
module과 cache package를 의존하지 않아야 한다.

## 5. 시작 전 읽기와 entry 확인 순서

### 5.1 권장 읽기 순서

읽는 순서는 “제품 의미 → module 경계 → 실행 gate → 현재 현실 → 다음 consumer”다.

1. [Master Design §1.1~§1.5와 §4](../../../master-design.md)
   제품 방향, non-negotiable invariant, `C-02`~`C-22`의 상태를 읽는다.
2. [Final Domain §2와 §7~§8](../../../2026-07-26-domain-design.md)
   위 glossary의 identity, stable/draft lifecycle, immutable/COW 경계를 확인한다.
3. [Final Architecture §2](../../../2026-07-26-architecture-design.md)
   target Maven tree, module responsibility와 dependency 방향을 그려 본다.
4. [통합 구현 설계 §3~§4](../../../architecture-domain-implementation-design.md)
   15 Phase 중 Phase 00의 실제 구현 범위와 금지 항목을 대조한다.
5. [Open questions §2~§4](../../../master-design-open-questions.md)
   `Q-BENCH-02=OPEN/EXPERIMENT_REQUIRED`, `Q-VAR-01=DEFERRED`,
   `Q-INFRA-01=RESOLVED target selection`의 차이를 확인한다.
6. [Implementation README §3~§6](../../README.md)와
   [Master Realization Plan §6~§7](../../master-realization-plan.md)
   critical path, Phase 13/14 gate, evidence DAG와 Phase 00 exit를 읽는다.
7. [Phase 00 원본](../../phases/phase-00-build-architecture-skeleton.md)과
   [Phase 00 review](../../reviews/phase-00-review.md)
   WP 순서와 `P00-R-001`~`P00-R-006` residual blocker를 체크리스트로 옮긴다.
8. [Phase 01 원본](../../phases/phase-01-canonical-input-normalization.md)의 entry/handoff와
   [Phase 01 review](../../reviews/phase-01-review.md)를 읽는다.
   Phase 00이 만들지 말아야 할 future domain API와 Phase 01이 실제로 필요한 artifact를
   구분한다.
9. Baseline과 live bytes를 다른 방법으로 읽는다.
   - `HEAD` pre-move source는 현재 worktree link로 대체하지 말고
     `git show 7cc890ee1d0805df5ae14b633127fade4f978639:<path>`로 읽는다. 대상 path는
     `pom.xml`, `Dockerfile`, `src/main/java/com/ronext/optimizer/...`와
     `src/test/java/com/ronext/optimizer/...`다.
   - Live candidate는 [root POM](../../../../pom.xml), [.sdkmanrc](../../../../.sdkmanrc),
     [Dockerfile](../../../../Dockerfile),
     [moved AlnsBatchEngine](../../../../legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/application/AlnsBatchEngine.java),
     [moved test](../../../../legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/application/AlnsBatchEngineTest.java),
     [moved HTTP server](../../../../legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationHttpServer.java),
     [moved API controller](../../../../legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationApiController.java),
     [moved worker controller](../../../../legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/adapter/in/http/OptimizationWorkerController.java)를
     별도 열에 기록한다.
   - Live moved source는 미커밋·미승인 candidate이므로 pre-move golden의 source of truth가
     아니다. `HEAD` bytes와 live bytes를 각각 hash하고 diff한다.

### 5.2 구현 착수 entry gate

다음 표에서 하나라도 `STOP`이면 POM/source 이동을 시작하지 않는다.

| Entry 질문 | 확인 방법 | PASS 기준 |
|---|---|---|
| 권위 source가 같은가? | §2.2의 SHA-256 재계산 | 모든 byte fingerprint 일치, 또는 변경 영향 review 기록 |
| Phase 00 구현 권한과 owner가 있는가? | scheduler/task scope 확인 | 변경 파일 범위와 owner/reviewer가 명시됨 |
| checkout이 기록되었는가? | branch/`HEAD`/tree와 status·tracked diff·untracked content manifest 계산 | evidence manifest에 immutable source identity와 dirty drift가 서로 다른 필드로 기록 |
| 공식 scheduler와 겹치지 않는가? | progress live hash와 §10.1 `current_status`, task/owner 확인 | 같은 Phase가 진행 중이면 owner에게 합류하거나 STOP; 중복 생성 금지 |
| 다른 사람 변경과 겹치지 않는가? | `git status --short --untracked-files=all` | overlap 분석 완료, unrelated change 보존 |
| baseline behavior가 재현되는가? | immutable `HEAD` materialization과 live candidate를 별도 build | pre-move와 live test/report/artifact가 별도 identity로 기록되고 차이가 판정됨 |
| legacy dependency 정책이 승인되었는가? | Checkpoint H1 | Jackson convergence와 Shade collision을 침묵 없이 처리할 정책이 기록됨 |
| build archive 재현 정책이 승인되었는가? | Checkpoint H2 | timestamp의 source와 pinning 정책이 명시됨 |
| Phase 00 원본 review blocker가 owner를 가졌는가? | review finding별 owner/status 표 | P00-R-001~006 각각 해결 또는 명시적 residual acceptance |

Phase 00에는 이전 Phase evidence가 없지만, “선행 evidence 없음”은 “entry gate 없음”을 뜻하지
않는다. 승인된 baseline과 current inventory가 Phase 00의 entry evidence다.

## 6. 실제 repository inventory: HEAD, live drift와 목표를 섞지 않기

### 6.1 HEAD baseline과 correction-time live drift

#### 6.1.1 `HEAD` baseline — `7cc890e...` / tree `a63fa3...`

다음 표는 현재 dirty worktree가 아니라 commit tree의 baseline이다. `HEAD`의 tracked build는
multi-module project가 아니다.

| 구분 | 실제 존재 | 의미 |
|---|---|---|
| Root build | `pom.xml` 한 개, implicit `jar` packaging | 현재 buildable placeholder application |
| Coordinates | `com.ronext:ro-next:0.1.0-SNAPSHOT` | target aggregator/module 좌표는 아직 없음 |
| Toolchain hints | `.sdkmanrc`: Java `25.0.3-amzn`, Maven `3.9.14` | Maven Wrapper는 아님 |
| Enforcement | Enforcer `3.6.1`: Java `[25,26)`, Maven `[3.9.14,)` | module/DAG architecture enforcement는 없음 |
| Build plugins | Compiler `3.14.1`, Surefire `3.5.4`, Shade `3.6.1` | target parent policy로 중앙화되지 않음 |
| Root dependencies | Google Workflow Executions `2.94.0`, GCS `2.70.0`, Jackson `2.19.2`, JUnit `5.13.1` | provider/business dependency가 root jar classpath에 있음 |
| Production Java | `src/main/java/com/ronext/optimizer/...` 6 files | `com.ronext.rpdptw` production source는 0 files |
| Test Java | `AlnsBatchEngineTest` 1 file | synthetic engine test 1개뿐 |
| Packaging | Shaded main class `OptimizationHttpServer` | legacy placeholder distribution |
| Target modules | tracked child `pom.xml` 0개, root `<modules>` 0개 | Reactor/DAG/evidence가 존재하지 않음 |
| Wrapper | `mvnw`, `mvnw.cmd`, `.mvn/` 모두 부재 | 이 `HEAD` tree만 materialize하면 `./mvnw` 사용 불가 |
| Phase evidence | `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY` 부재 | Phase 00 NOT_ACCEPTED |

현재 `AlnsBatchEngine`은 input bytes에서 RPDPTW를 풀지 않는다. seed/run/iteration으로
합성 `double` objective를 만들 뿐이다. HTTP/GCS/Workflow controller도 raw map, prefix listing,
환경/provider 접근과 합성 objective minimum을 사용한다. 이는 characterization할 legacy
placeholder이지 RPDPTW domain evidence가 아니다.

`rpdptw/*`, `adapters/*`, `apps/cli`, `legacy/gcp-placeholder`처럼 filesystem에 빈 directory가
보이더라도 tracked POM/source/resource가 없다면 **부재**다. 빈 directory는 Git artifact도,
module도, Phase acceptance evidence도 아니다.

`HEAD` file fingerprint:

| 파일 | SHA-256 |
|---|---|
| `pom.xml` | `f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6` |
| `.sdkmanrc` | `25c276822911b813a58c317ee47b86608e51c79ba921c65706df9f9f74793be5` |
| `Dockerfile` | `2aae6615e6c3dba5184064593e29407f7cded4e63dd416719a0b2fb30846d985` |

`HEAD` baseline 명령은 wrapper가 아니라 installed Maven을 사용한다. 이 명령을 dirty
worktree root에서 실행하면 live candidate를 build하므로 baseline 검증이 아니다. Exact
commit archive 또는 별도 immutable materialization에서만 실행한다.

```bash
mvn -B -ntp -Dstyle.color=never verify
```

이 명령의 통과는 과거 단일 JAR baseline을 확인할 뿐 Phase 00 exit가 아니다.

#### 6.1.2 Live drift — `2026-07-29T02:02:30+09:00`

아래는 교정 직전 같은 `HEAD` 위의 **미커밋·미승인 snapshot**이다. 문서 교정으로
human-guide file bytes가 바뀌므로 whole-worktree digest가 이후 달라지는 것은 정상이다.
이 값은 acceptance identity가 아니라 해당 시점의 overlap/reclassification evidence다.

| 항목 | Live 관측 | 판정 |
|---|---|---|
| Git identity | branch `codex-implementation`, `HEAD 7cc890e...`, tree `a63fa3...` | Commit만으로 dirty bytes 재현 불가 |
| Tracked drift | modified 5, deleted 7 | 다른 구현 작업의 미승인 patch |
| Untracked | file 110 | module/wrapper/test/human-guide가 섞인 미승인 content |
| Status manifest | porcelain-v1 `-z` SHA-256 `7087b4b7...54f84` | Path/state inventory이며 bytes digest와 별도 |
| Tracked bytes | `git diff --binary HEAD` SHA-256 `500c4ef4...12836` | Tracked patch fingerprint |
| Untracked bytes | sorted untracked `path + SHA-256` manifest SHA-256 `a02ef644...e2fa` | Untracked content fingerprint |
| Phase 00 implementation inventory | `pom.xml`, `.mvn`, wrapper, `build/`, `rpdptw/`, `legacy/`의 non-`target` file manifest SHA-256 `66e6529a...0292` | 이 범위도 미승인 candidate |
| Reactor candidate | project POM 13개, wrapper files 존재 | `HEAD` baseline과 다르며 accepted reactor가 아님 |
| Java candidate | main 33개, test 16개; core/stable package-info와 legacy/architecture tests | 파일 존재와 test green은 evidence acceptance가 아님 |
| Official scheduler | `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`; defective bundle superseded; acceptance `REJECTED_PENDING_FIX_01_REGENERATION` | 같은 Phase를 중복 구현하지 말고 task owner와 합류/STOP |
| Tool observation | Amazon Corretto `25.0.3`, Maven `3.9.14` | Tool provenance일 뿐 candidate 승인 아님 |

Fingerprint 계산 계약:

```bash
# path/state only
git status --porcelain=v1 -z --untracked-files=all | shasum -a 256

# tracked bytes
git diff --binary HEAD | shasum -a 256

# untracked path + bytes; stable bytewise path order
git ls-files --others --exclude-standard -z |
  sort -z |
  xargs -0 shasum -a 256 |
  shasum -a 256
```

실행 직전에는 다음 순서로 재분류한다.

1. Branch/`HEAD`/tree와 세 live digest를 다시 계산한다.
2. [공식 progress §10.1](../../execution-progress-and-results.md)의 live bytes, `current_status`,
   task ID, owner, `updated_at`을 읽는다. Fingerprint가 다르면 단순히 “stale이니 STOP”에서
   끝내지 않고, 같은 task면 owner에게 합류하고 다른/불명 task면 overlap이 해소될 때까지
   중단한다.
3. Baseline source는 `git show <commit>:<path>`, live source는 새 inventory의 actual path로
   읽는다. 어느 쪽도 다른 쪽의 link나 expected byte를 대신하지 않는다.
4. Dirty implementation은 승인된 commit 또는 content-addressed source archive로 완전히
   봉인되기 전 build/review/acceptance input으로 사용하지 않는다.
5. Live `target/`이나 기존 report는 stale일 수 있으므로 새 clean lifecycle의 expected test
   report와 count가 생성되었는지 확인한다.

### 6.2 REVIEWED Phase 00 target tree — H3 전에는 PROPOSED

아래는 current 전체-product 지도를 그대로 복사한 것이 아니라, reviewed core Phase 00의
최소 skeleton이다. 세 aggregator POM과 exact package ownership을 포함한다. Module/package
방향과 격리는 CONTRACT지만 concrete coordinates와 이름은 H3/architecture review 전
**PROPOSED**다. Current 상위 Architecture의 profile별 module 추천과 충돌하는 부분은 H3에서
해결하고, accepted Phase 00 coordinate/package manifest가 이 예를 대체한다. Manifest와
다르면 이 tree를 복붙해 새 package를 만들지 않는다.

```text
pom.xml                                  # parent + aggregator, packaging=pom
mvnw
mvnw.cmd
.mvn/
  wrapper/...

build/
  pom.xml                                # build-only aggregator
  test-fixtures/
    pom.xml
    src/test/java/com/ronext/rpdptw/fixture/...
  architecture-rules/
    pom.xml
    src/test/java/com/ronext/rpdptw/architecture/...
    src/test/resources/architecture-fixtures/...

rpdptw/
  pom.xml                                # stable semantic aggregator
  core/
    pom.xml
    src/main/java/com/ronext/rpdptw/
      input/package-info.java
      domain/package-info.java
      normalization/package-info.java
      travel/package-info.java
      propagation/package-info.java
      evaluation/
        api/package-info.java
        runtime/package-info.java
        insertion/package-info.java
  solver/
    pom.xml
    src/main/java/com/ronext/rpdptw/solver/
      portfolio/package-info.java
      search/package-info.java
      state/package-info.java
      termination/package-info.java
  verification/
    pom.xml
    src/main/java/com/ronext/rpdptw/
      verification/
        api/package-info.java
        candidate/package-info.java
        result/package-info.java
      result/
        api/package-info.java
        finalization/package-info.java
  application/
    pom.xml
    src/main/java/com/ronext/rpdptw/application/
      port/in/package-info.java
      port/out/package-info.java
      service/package-info.java
      execution/package-info.java
  capabilities/
    pom.xml
    src/main/java/com/ronext/rpdptw/capability/package-info.java
  profile-catalog/
    pom.xml
    src/main/java/com/ronext/rpdptw/profile/catalog/package-info.java

legacy/
  pom.xml                                # legacy-only aggregator
  gcp-placeholder/
    pom.xml
    src/main/java/com/ronext/optimizer/...
    src/test/java/com/ronext/optimizer/...
```

Production skeleton은 package ownership을 설명하는 `package-info.java`까지만 둔다. 기능이
없는 `Marker`, 항상 성공하는 service, 빈 public interface, fake provider를 만들지 않는다.
`module-info.java`/JPMS는 이 Phase의 확정 계약이 아니며 별도 ADR 없이는 도입하지 않는다.

Phase 08 이후의 adapter/app skeleton이나 Phase 13의 route-pool/selection/backend package를
“미리” 만들지 않는다. 존재하지 않는 future module을 root reactor에 넣으면 gate가 흐려지고
empty success가 기능 완료로 오인된다.

## 7. Scope, 확정 결정, 미결정과 사람 승인

### 7.1 In scope

- Current legacy endpoint, payload, storage key, workflow, success·failure behavior characterization
- Root parent/aggregator, Maven Wrapper, Java/Maven/plugin/dependency policy
- 여섯 stable target module과 test-fixture/architecture-rules build module
- `com.ronext.rpdptw` package ownership과 dependency direction
- Legacy GCP placeholder의 명시적 isolation
- Provider/vendor/customer/optional-backend/verifier 역의존 guard
- Default root `verify`, isolated-cache offline verify, two-clean-build artifact digest
- Immutable pre-review evidence manifest, independent review, post-review acceptance receipt

### 7.2 Non-scope

- RPDPTW parsing, normalization, domain record, solver 또는 verifier 구현
- Public API/schema 승인
- AWS/GCP/Azure/Kubernetes target adapter와 deployment
- OR-Tools/MIP/route pool/hybrid selection
- Official benchmark 수치와 production runtime default
- Production cutover, migration, shadow, rollback 실행
- `Q-VAR-01` variant와 multi-trip

### 7.3 확정된 것과 닫히지 않은 것

| 항목 | 상태 | Phase 00 행동 |
|---|---|---|
| Java 25 baseline과 Maven 3.9.14 requirement | **CONTRACT** | Wrapper/enforcer/tool provenance로 강제 |
| Root는 business dependency 없는 aggregator | **CONTRACT** | `packaging=pom`, child 관리만 수행 |
| Stable module DAG와 verifier 독립성 | **CONTRACT** | positive/negative architecture test |
| Base namespace `com.ronext.rpdptw` | **CONTRACT** | package-info와 guard로 예약 |
| Test fixture test-jar 연결 | **CONTRACT after review correction** | `type=test-jar`, `classifier=tests`, `scope=test` 모두 확인 |
| AWS S3 + Step Functions + Lambda | target/reference **RESOLVED**, implementation **GATED** | Phase 00 stable module에는 AWS SDK/API 0개 |
| `Q-BENCH-02` official 값 | **OPEN / EXPERIMENT_REQUIRED** | step/worker/round/watchdog 숫자를 default로 만들지 않음 |
| Phase 13 optional route selection | **GATED** | OR-Tools dependency, package, capability 광고 0개 |
| `Q-VAR-01` optional variant | **DEFERRED** | restart condition 전 skeleton도 만들지 않음 |
| Public Java/API/schema | **OPEN** | Phase 00에는 package-private test helper 후보만 사용 |
| Legacy Jackson convergence 정책 | **사람 승인 필요** | target strictness를 약화하지 말고 legacy-only 해결/격리 정책 결정 |
| Legacy Shade collision 정책 | **사람 승인 필요** | collision inventory와 필요한 service/resource merge semantics 승인 |
| ArchUnit/새 plugin exact version | **PROPOSED/사람 승인** | 중앙 pin, provenance와 compatibility test 후 결정 |
| Reproducible archive timestamp source | **PROPOSED/사람 승인** | source snapshot에서 결정적으로 파생되는 규칙 승인 |
| Phase 14B production authority | **GATED** | Phase 00 build 성공과 혼동하지 않음 |

### 7.4 사람 checkpoint와 마지막 안전 지점

| Checkpoint | 물어야 할 질문 | 승인 전 마지막 안전 지점 |
|---|---|---|
| H0 — baseline freeze | 이 source snapshot과 legacy behavior set이 characterization 기준인가? | POM/source 이동 전 |
| H1 — dependency/collision policy | Legacy Jackson mixed graph와 Shade collision을 어느 legacy-only 정책으로 처리할 것인가? | Parent/reactor 전환 전 |
| H2 — build policy | Wrapper checksum, plugin version, archive timestamp source와 offline cache 범위가 승인되었는가? | Wrapper/parent를 authoritative하게 만들기 전 |
| H3 — module/API boundary | 여섯 module 이름, DAG, `package-info` only 원칙과 test-jar 소비 방식이 승인되었는가? | Production class를 만들기 전 |
| H4 — guard coverage | Source/bytecode/dependency scan이 잡는 것과 잡지 못하는 것을 문서화했는가? | Negative fixture를 exit evidence로 봉인하기 전 |
| H5 — acceptance | Build·arch·legacy bundle이 같은 manifest/source digest인가? Independent reviewer가 재실행했는가? | Phase 01 status를 READY로 바꾸기 전 |

승인이 없으면 **그 지점에서 멈춘다**. 가장 안전한 rollback은 승인 전 작은 변경 단위와
명시적 file inventory를 유지하는 것이다. 사용자 작업이 섞인 checkout에서
`git reset --hard`, broad `git clean`, directory 전체 삭제로 되돌리지 않는다.

## 8. 학습 경로: 개념에서 통합까지

### 8.1 1단계 — 개념 지도 그리기

목표는 “어느 module이 무엇을 알아도 되는가”를 말로 설명하는 것이다.

작은 실습:

1. §4의 `Request`, `Route`, `SearchSnapshot`, verifier를 카드에 적는다.
2. 각 카드를 `core`, `solver`, `verification`, `application` 중 하나에 배치한다.
3. verifier 카드에서 solver/cache 카드로 화살표가 생기면 왜 독립 검증이 깨지는지 설명한다.
4. AWS SDK를 core에 배치했을 때 local ALNS와 provider substitution에 생기는 결합을 설명한다.

완료 신호:

- 구현자가 DAG를 보지 않고도 `verification → core`는 허용되고
  `verification → solver`는 금지인 이유를 설명한다.
- delivery-only logical pickup이 physical visit가 아닌 이유를 설명한다.

자문 질문:

- “편해서 의존한다”는 선택이 어떤 미래 gate의 독립성을 없애는가?
- 이 type은 domain authority인가, derived cache인가, provider detail인가?

### 8.2 2단계 — 작은 탐색과 baseline 재현

목표는 문서가 아니라 현재 checkout의 사실을 직접 확인하는 것이다.

작은 실습:

- Root POM에서 직접 dependency와 plugin을 각각 목록화한다.
- `jdeps` 또는 dependency tree에서 Google/Jackson classpath를 찾는다.
- 기존 test가 input bytes의 내용 변화를 검증하는지 확인한다.
- HTTP controller의 400/404/405/500과 202 경로를 표로 만든다.

완료 신호:

- “현재 존재”, “빈 directory”, “target”, “future placeholder”를 다른 열로 기록한다.
- 현재 test 통과를 RPDPTW solver 통과라고 부르지 않는다.

자문 질문:

- 이 관찰은 source에서 읽은 것인가, build output에서 읽은 것인가, 추측인가?
- 실패 경로와 redaction을 포함하지 않은 characterization이 어떤 regression을 놓치는가?

### 8.3 3단계 — 실제 변경

목표는 한 번에 하나의 계약을 red→green으로 만드는 것이다.

권장 순서:

1. Legacy golden characterization을 먼저 red/green으로 고정한다.
2. Parent/wrapper를 추가하되 current legacy module만 reactor에서 먼저 green으로 만든다.
3. Stable empty module을 한 개씩 추가하고 DAG를 확인한다.
4. 허용 dependency positive fixture를 먼저 통과시킨다.
5. 금지 dependency negative fixture가 실제로 build를 깨는지 확인한다.
6. Test-fixture leakage와 optional-backend-free build를 별도로 확인한다.

완료 신호:

- 각 commit-sized 작업 단위에서 root `verify`의 실패 이유가 하나로 좁혀진다.
- 실패를 skip/allowlist 확대가 아니라 계약에 맞는 boundary 수정으로 해결한다.

자문 질문:

- 이 green은 guard가 제대로 작동해서 green인가, test가 실행되지 않아서 green인가?
- Negative fixture가 없을 때 architecture rule이 항상 성공하는 false-green은 아닌가?

### 8.4 4단계 — 통합과 handoff

목표는 동일 source snapshot에서 build/architecture/legacy evidence를 봉인하는 것이다.

완료 신호:

- Clean online verify, prefetched offline verify, two-clean-build digest, positive/negative
  architecture suite, legacy golden suite가 한 manifest에 연결된다.
- Independent reviewer가 manifest 명령으로 재현한다.
- Phase 01 owner가 root/module 명령과 fixture 소비 방법을 그대로 실행해 본다.

자문 질문:

- Evidence log가 다시 생성될 때 자기 자신을 hash하는 순환 digest를 만들지 않는가?
- Phase 01이 Phase 00 내부 test helper나 legacy DTO에 의존해야만 시작할 수 있는가?

## 9. Java/Maven skeletal 설계 안내

### 9.1 Package ownership

| Module | 허용 책임 | Phase 00 production source |
|---|---|---|
| `rpdptw-core` | Provider-neutral input/domain/normalization/travel/propagation/evaluation의 미래 소유자 | `input`, `domain`, `normalization`, `travel`, `propagation`, `evaluation.api`, `evaluation.runtime`, `evaluation.insertion`의 `package-info.java`만 |
| `rpdptw-solver` | Portfolio/search/state/termination의 미래 소유자 | `solver.portfolio`, `solver.search`, `solver.state`, `solver.termination`의 `package-info.java`만 |
| `rpdptw-verification` | Solver 독립 candidate/result 검증과 finalization의 미래 소유자 | `verification.api`, `verification.candidate`, `verification.result`, `result.api`, `result.finalization`의 `package-info.java`만 |
| `rpdptw-capabilities` | 재사용 capability의 미래 소유자 | 단수 `com.ronext.rpdptw.capability.package-info`만 |
| `rpdptw-profile-catalog` | Versioned profile data/catalog의 미래 소유자 | `com.ronext.rpdptw.profile.catalog.package-info`만 |
| `rpdptw-application` | Use case orchestration과 provider-neutral port의 미래 소유자 | `application.port.in`, `application.port.out`, `application.service`, `application.execution`의 `package-info.java`만 |
| `legacy-gcp-placeholder` | 현재 `com.ronext.optimizer` behavior 보존 | 현재 source/test 이동, 의미 변경 금지 |
| `rpdptw-test-fixtures` | 후속 Phase가 test scope에서 공유할 builder/corpus | 필요한 최소 TEST-ONLY helper |
| `rpdptw-architecture-rules` | Stable module bytecode/DAG/namespace 검사 | Test source와 negative resource fixture |

`package-info.java` 주석은 책임과 금지 dependency를 설명한다. “향후 API를 예상”한 빈
interface나 record를 production source에 두지 않는다. 위 module/package 이름은 reviewed
Phase 00 baseline에 맞춘 **PROPOSED implementation manifest**다. H3에서 current 상위 지도와
차이를 승인한 뒤 accepted coordinate/package manifest가 생기면 그 manifest가 이 표보다
우선한다.

### 9.2 PROPOSED test-only contract type

실제 public API가 미정이므로 다음 이름은 architecture test harness의 후보일 뿐이다.

```java
package com.ronext.rpdptw.architecture;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

// PROPOSED, package-private, TEST-ONLY
interface ArchitectureRule {
    ArchitectureCheckResult check(ArchitectureFixture fixture);
}

// PROPOSED, package-private, TEST-ONLY
record ArchitectureFixture(
        String id,
        Path classesDirectory,
        Set<String> expectedViolationCodes) {
}

// PROPOSED, package-private, TEST-ONLY
record ArchitectureCheckResult(
        String ruleId,
        List<ArchitectureViolation> violations) {
    boolean passed() {
        return violations.isEmpty();
    }
}

// PROPOSED, TEST-ONLY; exhaustive assertion을 위한 hierarchy 후보
sealed interface ArchitectureViolation
        permits ForbiddenDependency, InternalPackageAccess,
                TestFixtureLeakage, ForbiddenSymbolReference {
    String code();
    String evidence();
}

record ForbiddenDependency(String code, String evidence)
        implements ArchitectureViolation {}
record InternalPackageAccess(String code, String evidence)
        implements ArchitectureViolation {}
record TestFixtureLeakage(String code, String evidence)
        implements ArchitectureViolation {}
record ForbiddenSymbolReference(String code, String evidence)
        implements ArchitectureViolation {}
```

이 compilation-unit 후보는 Java 25 문법상 package-private top-level type을 한 test source에
둘 수 있게 작성했지만 완성 implementation 계약은 아니다. 채택하더라도
`build/architecture-rules/src/test/java` 또는 test utility source에 둔다. `rpdptw-core`에
넣어 public domain API처럼 만들지 않는다. 실제 ArchUnit API와 Maven plugin version이
승인되면 더 단순한 test class만으로 대체해도 된다.

### 9.3 Dependency evaluation 의사코드

Architecture test는 artifact name만 보고 판정하지 말고 resolved edge와 bytecode symbol을
함께 본다.

```text
allowedProductionEdges = {
  solver -> core,
  verification -> core,
  capabilities -> core,
  profileCatalog -> core,
  application -> core,
  application -> solver,
  application -> verification
}

for each resolved production dependency edge:
    if edge is not in allowedProductionEdges
       and target is another stable module:
        fail(FORBIDDEN_MODULE_EDGE)

for each stable production class:
    fail if symbol references provider SDK, optimizer backend, legacy namespace
    fail if symbol references another module's ".internal" package

for verification production classes:
    fail if symbol references solver/search/cache

for generic core/solver/verification and approved application scope:
    fail if AST/bytecode/package evidence shows a customer-name
            conditional, switch, executable implementation or namespace

for profile catalog and approved adapter authorization boundary:
    allow exact customer/profile/version and authorization identity data
    fail if it owns arbitrary executable business rule or solver branch

for core production classes:
    fail on ambient semantic configuration reads such as
            System.getenv/System.getProperty/System.getProperties
    fail on ambient clock reads such as
            System.currentTimeMillis/System.nanoTime/Clock.system*/now()
    fail on unseeded or global random such as
            Math.random/ThreadLocalRandom/unseeded shared Random
    fail on mutable static registry or other mutable static authority

for every main artifact and production classpath:
    fail if shared test-fixture classifier or fixture package appears
```

Customer rule은 “모든 stable module에서 identity 문자열 0”이 아니다.

1. Generic core/solver/verification과 승인된 application 범위에는 customer-name
   conditional, executable implementation, customer package가 없어야 한다.
2. Profile catalog와 향후 승인된 adapter authorization은 exact identity/version data를
   소유할 수 있지만 arbitrary executable rule이나 solver branch를 소유하지 않는다.
3. Approved token manifest와 허용 위치를 AST/bytecode/package rule에 결합하고,
   conditional/string/switch bad fixture를 각각 둔다.

문자열 scan은 임의 customer synonym을 완전히 검출할 수 없다. Core ambient rule도
reflection/dynamic loading을 완전히 증명하지 못한다. 각 rule의 검출 범위, allowlist
source/owner/expiry, known-negative fixture와 residual limitation을 evidence에 명시한다.
후속 Phase에서 clock/random/config가 필요하면 owner가 주입하는 명시적 contract로 추가하며
Phase 00에서 fake production default를 만들지 않는다.

### 9.4 Build state transition

```text
DISCOVERED_CURRENT
  -- H0 baseline 승인 -->
CHARACTERIZED_LEGACY
  -- H1/H2 정책 승인 -->
REACTOR_BOOTSTRAPPED
  -- module positive build -->
SKELETON_GREEN
  -- negative fixture가 expected rule로 실패 -->
GUARDS_PROVEN
  -- clean/offline/reproducibility + immutable manifest -->
PRE_REVIEW_SEALED
  -- independent review -->
REVIEWED
  -- correction 재실행 + acceptance receipt -->
ACCEPTED_FOR_PHASE_01
```

`SKELETON_GREEN`에서 바로 `ACCEPTED_FOR_PHASE_01`로 건너뛰지 않는다. Review finding을
고친 뒤에는 이전 review log를 덮어쓰지 않고 새 source/evidence digest로 post-review
acceptance를 발행한다.

## 10. 순서 있는 work package

모든 WP는 앞 WP의 evidence를 소비한다. 병렬로 작업하더라도 shared root POM이나 source
이동을 두 owner가 동시에 수정하지 않는다.

### WP00-0 — Current baseline과 source snapshot 봉인

**목적과 이유**

Reactor 전환 전 현재 동작·dependency·file inventory를 변경 불가능한 입력으로 만든다.
Baseline이 없으면 legacy 이동 중 발생한 regression과 의도한 architecture 변화가 섞인다.

**사전조건**

- §5의 source를 읽었고 H0 reviewer/owner가 지정됨
- working tree의 unrelated 변경 owner를 파악함
- Build·Quality/Release owner가 `clean` 영향 밖의 evidence root와 retention/ACL을 승인했거나,
  승인 전 disposable staging에서 멈추기로 함
- evidence root가 source tree/build input과 분리되고 repository에 secret을 저장하지 않음

**예상 file/package/type**

- Source 변경 없음
- `target/phase-00-evidence/...`는 **disposable staging 후보**일 뿐이다. `mvn clean`이 지우며
  acceptance input으로 직접 인용하지 않는다.
- 승인된 영구 위치는 **OPEN / 사람 승인 필요**다. `clean` lifecycle 밖의
  content-addressed 또는 digest-protected root여야 한다.
- Manifest schema는 **PROPOSED internal**이며 public API가 아님

**구체 행동**

1. Branch, commit, tree, status/diff/untracked content digest, OS/JDK/Maven,
   locale/timezone을 저장한다.
2. `HEAD` baseline과 live POM/Java/test/Docker inventory를 별도 manifest로 저장한다.
3. `HEAD`의 `pom.xml`, `.sdkmanrc`, Dockerfile, source/test는 `git show` bytes를 hash하고,
   live bytes는 actual path에서 별도 hash한다.
4. Dirty implementation을 승인된 commit 또는 content-addressed source archive로 완전히
   봉인한다. 두 clean workspace는 이 동일 source identity만 materialize한다.
5. Immutable `HEAD` baseline에서 installed Maven verify를 실행하고, live candidate command와
   report를 섞지 않는다.
6. Effective POM, dependency tree, plugin resolution, shaded JAR entry/collision warning을 저장한다.
7. Existing test가 합성 objective만 확인한다는 limitation을 manifest에 명시한다.
8. Disposable `target/`에서 모든 leaf를 완성한 뒤 승인된 evidence root로 한 번 promotion하고,
   promotion 뒤 leaf를 수정하지 않는다. 위치/retention 승인이 없으면 seal/acceptance 전에 STOP한다.

**선택 근거**

Clean build log만 저장하면 source와 dependency graph가 무엇이었는지 복원할 수 없다.
반대로 source hash만 저장하면 tool/runtime 차이를 놓친다. 둘을 하나의 manifest로 묶는다.

**금지 shortcut**

- Baseline을 green으로 만들기 위해 dependency version을 먼저 변경
- 기존 warning 삭제 또는 log 일부만 복사
- untracked directory를 tracked module로 기록
- current test를 “solver correctness test”로 이름 바꾸기

**검증 명령**

```bash
git rev-parse --abbrev-ref HEAD
git rev-parse HEAD
git rev-parse HEAD^{tree}
git status --short --untracked-files=all
java -version
mvn -version
git ls-files | sort
# 아래 build는 immutable HEAD materialization에서만 실행
mvn -B -ntp -Dstyle.color=never verify
mvn -B -ntp -Dstyle.color=never help:effective-pom
mvn -B -ntp -Dstyle.color=never dependency:tree -Dverbose
jar tf target/ro-next-0.1.0-SNAPSHOT.jar
```

마지막 `jar` 명령의 artifact 이름은 `HEAD` POM에만 해당한다. Shade output 이름이 바뀌면
`find target -maxdepth 1 -type f`로 실제 artifact를 확인한 뒤 exact path를 기록한다.

**기대 결과와 pass 판정**

- Immutable `HEAD` single-JAR `verify`가 실행되고 기존 test count와 warning이 원문으로 남음
- Baseline과 live source/inventory/toolchain/dependency/build log가 서로 다른 identity로
  기록되고, 승인된 implementation source만 pre-review manifest에 연결됨
- `target/` file을 acceptance evidence로 직접 참조하지 않음
- Secret, access token, signed URL, raw credential이 evidence에 없음

**실패 해석**

- `HEAD` build 실패: Phase 00 변경을 시작하지 말고 baseline failure로 분류
- Dependency download 실패: source 결함과 network/cache 결함을 구분
- Dirty overlap: 다른 owner와 범위를 정리할 때까지 STOP

**Rollback**

Read-only 단계다. 생성한 local build/evidence output만 explicit path로 치우고 source는
건드리지 않는다. broad clean을 사용하지 않는다.

**다음 handoff**

H0에서 baseline manifest를 승인한 뒤 WP00-1에 exact source digest와 expected behavior 목록을
넘긴다.

### WP00-1 — Legacy success·failure characterization과 isolation 설계

**목적과 이유**

현재 placeholder를 target domain으로 오인하지 않으면서도 reactor 전환이 기존 behavior를
조용히 바꾸지 않게 한다.

**사전조건**

- WP00-0 baseline manifest
- Existing controller/server/engine test seam 조사
- 실패 응답에 credential/object key 내부 정보가 노출되지 않는지 review할 사람 지정

**예상 file/package/type**

- 미래 module: `legacy/gcp-placeholder/pom.xml`
- 기존 `com.ronext.optimizer...` source/test를 같은 package 이름으로 이동
- TEST-ONLY fixture/golden resource 후보
- Production package rename이나 `com.ronext.rpdptw`로의 흡수는 하지 않음

**구체 행동**

1. Existing success path의 method/status/content-type/payload shape를 golden으로 기록한다.
2. 이동 전 exact `HEAD` bytes에서 endpoint × method × path × failure-stage matrix를 먼저
   실행·기록한다. 최소 source-derived 분리는 다음과 같으며, status/content-type/body
   expected bytes는 pre-move 실제 응답을 capture한 뒤에만 golden으로 확정한다.

   | Endpoint/경계 | Method/path와 failure stage | `HEAD` source-derived status branch |
   |---|---|---|
   | Public API submit | `POST /optimizations`, JSON parse 실패 | generic `Exception` → redacted `500` |
   | Public API submit | JSON parse 성공 뒤 missing/invalid `inputUri` | `IllegalArgumentException` → `400` |
   | Public API context | unsupported method 또는 unsupported path | controller fallback → `404` |
   | Public API result | valid `GET /optimizations/{id}`, result blob 없음 | `202` + current `RUNNING` body |
   | Public API provider | storage/workflow/client/engine failure | redacted `500` |
   | Worker batch/finalize | non-`POST` on either registered context | explicit `405` |
   | Worker batch/finalize | JSON parse 실패 | generic `Exception` → endpoint-specific redacted `500` |
   | Worker batch/finalize | parsed body의 required field/type invalid | `IllegalArgumentException` → `400` |
   | Worker finalize | candidate list empty | `IllegalStateException` → redacted `500` |
   | Worker provider | storage list/read/write 또는 workflow failure | endpoint-specific redacted `500` |

   HTTP server/context가 matrix 밖에서 응답을 바꾸면 실제 pre-move byte capture를 우선하고
   source-derived 가정과 차이를 evidence에 남긴다.
3. HTTP/GCS/Workflow를 실제 cloud credential 없이 deterministic fake로 주입할 seam을 만든다.
   Fake는 TEST-ONLY이며 production default가 아니다.
4. Current storage key/prefix/listing 의존을 기록한다. 이를 target object-storage semantics로
   승인하지 않는다.
5. Legacy source/test를 explicit legacy module로 옮긴 뒤 package와 behavior가 같은지 확인한다.
6. Stable target module에서 legacy module로 향하는 dependency가 0인지 검사한다.

**선택 근거**

Success-only golden은 가장 위험한 regression인 error mapping, retry, redaction 변화를 잡지
못한다. Provider call을 test seam으로 격리해야 credential 없이도 기본 build가 통과한다.

**금지 shortcut**

- 합성 `AlnsBatchEngine`을 `rpdptw-solver`로 이동
- Raw map/Jackson DTO를 future canonical input로 선언
- `202`/empty/error behavior를 더 예뻐 보이게 바꾸기
- Malformed JSON과 parsed-invalid request, API fallback과 worker method guard를 한 status로 합치기
- Cloud가 없을 때 production code가 자동 success하도록 만들기
- Legacy GCP dependency allowlist를 stable module까지 넓히기

**검증 command/test 후보**

```bash
# FUTURE: legacy module이 생성된 뒤
./mvnw -B -ntp -Dstyle.color=never -pl legacy/gcp-placeholder -am test
./mvnw -B -ntp -Dstyle.color=never -pl legacy/gcp-placeholder -am verify
./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am \
  -Darchitecture.includes=legacy-isolation verify
```

Test class/method 후보:

- `OptimizationHttpGoldenContractTest`
  - `apiReturnsObservedRedacted500ForMalformedJson()`
  - `apiReturns400ForParsedInvalidRequest()`
  - `apiReturns404ForUnsupportedMethodOrPath()`
  - `workerReturns405ForUnsupportedMethod()`
  - `workerReturnsObservedRedacted500ForMalformedJson()`
  - `returns202WhenResultIsNotYetAvailable()`
  - `returnsRedacted500ForStorageFailure()`
  - `returnsRedacted500ForWorkflowFailure()`
- `LegacyEmptyCandidateBehaviorTest#preservesObservedEmptyCandidateOutcome()`
- `LegacyIsolationArchitectureTest#stableModulesDoNotDependOnLegacy()`

이 이름은 **PROPOSED**다. Java method 이름은 parameter/fixture를 생략한 후보이며 production
API가 아니다. Pre-move exact response bytes를 먼저 관측하고 expected value를 golden에
기록한다. 더 나은 HTTP semantics로 바꾸는 작업은 Phase 00 characterization과 분리된 승인
변경이어야 한다.

**기대 결과와 pass 판정**

- Endpoint/method/path/failure-stage별 이동 전후 같은 fixture에서 같은 status,
  content-type와 response bytes
- Failure body/log에 secret이 없음
- Default test에 cloud credential/network가 필요 없음
- Stable module → legacy edge 0

**실패 해석**

- Golden mismatch: source 이동/assembly/resource merge 또는 field-initialization timing regression 가능
- Cloud connection 시도: test seam 또는 default activation이 잘못됨
- Stable target이 legacy를 의존: migration shortcut이 architecture를 오염시킴

**Rollback**

한 번에 source tree 전체를 삭제하지 않는다. 이동 전/후 manifest를 비교하고, 새 module
registration과 source move를 분리된 변경 단위로 되돌린다. unrelated file은 보존한다.

**다음 handoff**

Legacy module coordinates, golden test list, known warning과 H1에 필요한 Jackson/Shade inventory를
WP00-2에 넘긴다.

### WP00-2 — Parent/reactor, Wrapper와 reproducible build policy

**목적과 이유**

모든 module이 같은 Java/Maven/plugin/dependency/reproducibility 정책을 사용하게 하되 root
artifact가 business/provider dependency를 운반하지 않게 한다.

**사전조건**

- WP00-1의 legacy dependency/collision inventory
- H1: Jackson convergence와 Shade resource collision 정책 승인
- H2: Wrapper checksum, exact plugin version, archive timestamp source 승인
- Network warm-up과 offline test를 분리해 기록할 환경

**예상 file/package/type**

- `pom.xml`: `packaging=pom`, parent/aggregator
- `mvnw`, `mvnw.cmd`, `.mvn/wrapper/...`
- Child module POM
- 선택 시 `.mvn/jvm.config` 또는 toolchain config; 내용은 review된 정책만

**구체 행동**

1. CURRENT root application coordinates/build를 legacy module로 옮긴다.
2. Root를 `packaging=pom`으로 바꾸고 root `<dependencies>`를 0으로 만든다.
3. Java release 25, Maven minimum/exact wrapper, encoding, dependency/plugin management를
   중앙화한다.
4. Wrapper distribution URL/version/checksum을 source-controlled policy로 고정한다.
5. Plugin version이 effective POM에서 모두 해석되는지 확인한다.
6. Dependency convergence/upper-bound rule을 target stable modules에 strict 적용한다.
7. Legacy mixed Jackson graph는 H1에서 승인한 explicit legacy-only 처리만 적용한다.
   Target 전체 strictness를 끄거나 global skip하지 않는다.
8. Shade collision inventory에서 필요한 service/resource merge를 승인된 transformer로
   명시한다. Warning을 무조건 숨기지 않는다.
9. Archive entry time을 승인된 source-derived timestamp로 고정한다.
10. Empty local repository에서 online warm-up 후 같은 repository로 offline build한다.
11. 서로 다른 두 clean workspace에서 artifact 목록과 digest를 비교한다.

**선택 근거**

SDKMAN 파일은 개발자 힌트이지 CI와 독립 review가 사용하는 self-contained launcher가 아니다.
Wrapper와 checksum이 동일 Maven binary를 고정한다. Reproducibility는 단순히
`project.build.sourceEncoding`을 설정하는 것이 아니라 archive timestamp, order, plugin과
tool provenance까지 포함한다.

**금지 shortcut**

- Root에 Jackson/Google/ArchUnit/JUnit 같은 business/test dependency 직접 추가
- `dependencyConvergence`를 global skip
- Legacy 충돌을 target dependencyManagement로 조용히 맞춰 behavior 변경
- `LATEST`, range, floating plugin version 사용
- 현재 시간을 archive timestamp로 사용
- Local warmed cache에서만 성공한 것을 offline reproducibility로 주장

**검증 command/test**

`HEAD`에는 wrapper가 없고 live candidate에는 untracked wrapper가 있다. 아래는 live candidate
owner가 scope를 확인한 뒤 실행할 수 있지만, accepted command가 되는 시점은 H2/H3와
source seal 뒤다.

```bash
# LIVE CANDIDATE / FUTURE ACCEPTED: source identity를 먼저 기록
./mvnw --version
./mvnw -B -ntp -Dstyle.color=never help:effective-pom
./mvnw -B -ntp -Dstyle.color=never clean verify

# 사전에 전용 local repository를 online으로 채운 뒤 같은 경로를 offline 사용
./mvnw -B -ntp -Dstyle.color=never \
  -Dmaven.repo.local=/absolute/path/to/phase00-m2 \
  dependency:go-offline
./mvnw -B -ntp -Dstyle.color=never -o \
  -Dmaven.repo.local=/absolute/path/to/phase00-m2 \
  verify

# aggregator에 dependency를 둘 수 없음을 정적으로 확인
./mvnw -B -ntp -Dstyle.color=never help:effective-pom
./mvnw -B -ntp -Dstyle.color=never dependency:tree
```

`dependency:go-offline`의 exit `0`만으로 offline 준비를 승인하지 않는다. 실제 `-o clean
verify`가 새 isolated local repository와 sealed source에서 통과해야 한다. Root `verify`가
Surefire의 `*Test`를 발견하는지, Failsafe를 쓰면 `*IT`가
`integration-test`/`verify`에 bind되는지 effective POM과 fresh XML report로 확인한다.
`clean` 뒤 expected report가 새로 생기고 suite/test count가 0보다 크며 expected class가
포함되어야 한다. 이전 `target/*-reports`가 남은 상태의 green은 evidence에서 거부한다.

Two-clean-build 비교의 각 workspace와 artifact 목록은 manifest에 exact path/digest로 기록한다.
Evidence output directory 자체를 build input에 포함하거나 자기 자신의 digest를 다시 hash하지
않는다.

**기대 결과와 pass 판정**

- `./mvnw --version`이 승인된 Maven/JDK 정책과 일치
- Root effective POM은 `packaging=pom`, business/provider dependency 0
- Stable module strict convergence 통과
- Prefetched isolated repository의 offline root `verify` 통과
- 두 clean build의 비교 대상 artifact set과 SHA-256 일치

**실패 해석**

- Offline plugin miss: `go-offline` 범위 또는 lifecycle plugin pin 누락
- Digest mismatch: timestamp, file ordering, generated metadata, tool/runtime 차이 조사
- Legacy convergence fail: strictness 제거가 아니라 H1 정책을 재검토
- Shade golden mismatch: service/resource merge가 runtime behavior를 바꿈

**Rollback**

Wrapper/parent 전환과 legacy child registration을 작은 단위로 유지한다. CURRENT root baseline
manifest로 돌아갈 수 있어야 하며 unrelated checkout을 reset하지 않는다.

**다음 handoff**

승인된 parent coordinates, wrapper command, plugin/dependency policy와 reproducibility
manifest를 WP00-3에 넘긴다.

### WP00-3 — Stable module와 package skeleton

**목적과 이유**

Phase 01 이후 각 책임의 compile boundary를 실제 reactor artifact로 만든다. Domain API는 아직
만들지 않는다.

**사전조건**

- WP00-2 parent/wrapper green
- H3 module names/DAG/package-info-only 원칙 승인
- Phase 01 owner가 expected `core` coordinates를 검토

**예상 file/package/type**

- §6.2의 여섯 stable module POM과 `package-info.java`
- `build/test-fixtures/pom.xml`
- Production marker/service/record/interface 없음

**구체 행동**

1. `rpdptw-core`를 dependency 0인 stable base module로 추가한다.
2. `solver`, `verification`, `capabilities`, `profile-catalog`이 core만 compile-depend하도록
   한 개씩 추가한다.
3. `application`이 core/solver/verification만 compile-depend하도록 추가한다.
4. 각 module의 `package-info.java`에 소유 책임과 금지 outward dependency를 서술한다.
5. Shared test fixture를 `src/test/java`에 두고 `maven-jar-plugin:test-jar`로
   `classifier=tests` artifact를 만든다.
6. Consumer sample test가 fixture에
   `type=test-jar`, `classifier=tests`, `scope=test`로만 의존하게 한다.
7. Main artifact와 production compile/runtime classpath에서 fixture class가 보이지 않음을
   확인한다.
8. Root reactor order와 resolved DAG를 저장한다.

**선택 근거**

`package-info.java`는 package ownership을 선언하지만 존재하지 않는 domain behavior를
약속하지 않는다. Test fixture를 main jar로 내보내지 않아야 후속 module production code가
편의상 fixture builder를 사용하는 leakage를 막을 수 있다.

**금지 shortcut**

- Compile 순서를 맞추려고 circular dependency 허용
- `verification → solver`
- `profile-catalog → capabilities implementation`
- Test fixture를 main source/main JAR로 publish
- `scope=compile` 또는 classifier 없는 fixture dependency
- Future Phase class를 empty shell로 추가
- `com.ronext.optimizer`를 새 core 아래에 그대로 복사

**검증 command/test**

```bash
# LIVE CANDIDATE / FUTURE ACCEPTED: module identity와 H3 scope 확인 뒤
./mvnw -B -ntp -Dstyle.color=never verify
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core -am verify
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/verification -am verify
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/application -am verify
./mvnw -B -ntp -Dstyle.color=never -pl build/test-fixtures -am verify
./mvnw -B -ntp -Dstyle.color=never dependency:tree -Dscope=compile

# 실제 artifact 이름을 먼저 확인한 뒤 main/test jar contents 비교
jar tf build/test-fixtures/target/*-tests.jar
jar tf build/test-fixtures/target/*.jar
```

`-pl <module> -am`은 선택 module과 reactor dependency만 build하며 전체 reactor acceptance를
증명하지 않는다. Root clean `verify`를 별도로 실행한다. Test-fixtures module 자체의 test
green은 consumer wiring을 증명하지 않는다. Consumer POM의 resolved test dependency가
`type=test-jar`, `classifier=tests`, `scope=test`인지와 consumer test compile/runtime
classpath에서만 fixture가 보이는지를 해당 consumer test와 dependency tree로 확인한다.

Shell glob 결과가 여러 artifact와 섞이면 manifest script에서 exact file list를 정렬해 각각
검사한다. `*-tests.jar`가 main jar 검사에 다시 포함되는 false-positive를 피한다.

**기대 결과와 pass 판정**

- Reactor cycle 0
- Resolved production edge가 §9.3 allowlist의 subset
- Stable production package는 `com.ronext.rpdptw` 아래
- Main jar/production classpath에서 test fixture class 0
- Domain behavior class와 fake implementation 0

**실패 해석**

- `-pl` 경로/좌표 실패: aggregator 등록 또는 artifactId/path 불일치
- Fixture가 main jar에 보임: source root나 jar plugin configuration 오류
- Application transitively provider SDK를 봄: legacy/provider dependency leakage

**Rollback**

Module을 root에 등록하는 변경과 module file 추가를 한 단위씩 되돌릴 수 있게 유지한다.
다른 사람이 만든 빈/future directory를 삭제하지 않는다.

**다음 handoff**

Exact reactor module list, coordinates, package ownership과 resolved DAG digest를 WP00-4에 넘긴다.

### WP00-4 — Architecture guard의 red→green 증명

**목적과 이유**

Architecture 규칙이 문서에만 있지 않고, 실제 위반이 생기면 default `verify`를 실패시키는지
증명한다.

**사전조건**

- WP00-3 reactor/DAG green
- H4 rule coverage와 한계 승인
- Negative fixture가 production reactor에 설치/publish되지 않는 격리 방식 승인

**예상 file/package/type**

- `build/architecture-rules/pom.xml`
- `src/test/java/com/ronext/rpdptw/architecture/*Test.java`
- `src/test/resources/architecture-fixtures/<rule-id>/...`
- §9.2 TEST-ONLY result type 후보

**구체 행동**

1. Positive fixture로 모든 허용 edge가 통과하는지 먼저 확인한다.
2. 아래 각 rule마다 최소 하나의 known-negative fixture를 만든다.
   - `core → solver`
   - `verification → solver/search/cache`
   - stable module → legacy
   - stable module → AWS/GCP/Azure/Kubernetes SDK
   - generic module → `com.google.ortools`
   - generic core/solver/verification 및 승인된 application 범위 → customer-specific
     executable namespace/name branch
   - profile catalog exact identity/version data는 허용하되 catalog의 arbitrary executable
     rule/solver branch는 거부
   - core → environment/system-property semantic read
   - core → system/ambient clock
   - core → unseeded/global random
   - core → mutable static registry
   - 다른 module `.internal` package 접근
   - test fixture의 production compile/runtime leakage
   - root aggregator business dependency
3. Negative fixture build가 “어떤 이유로든” 실패하는 것이 아니라 expected
   violation code/message 때문에 실패하는지 oracle로 확인한다.
4. Source scan, resolved dependency tree, bytecode/`jdeps` 검사를 조합한다.
5. Allowlist는 module/rule/reason/owner/expiry를 갖고 default broad wildcard를 금지한다.
6. Rule test 자체가 실행되었는지 test count와 report file을 evidence에 기록한다.
7. Negative fixture artifact가 reactor install/deploy 대상이 아닌지 확인한다.
8. Surefire 후보는 `*Test`로, Failsafe 후보는 `*IT`로 명명하고 effective POM의 실제
   include/binding과 맞춘다. `clean` 뒤 새 report가 없거나 expected class/test count가 0이면
   root verify exit `0`이어도 FAIL이다.

**선택 근거**

Source import scan만으로 reflection, transitive dependency, bytecode symbol을 놓칠 수 있고,
dependency tree만으로 customer name branch나 `.internal` access를 놓칠 수 있다. 여러 검사를
겹치고 rule의 한계를 공개해야 false-green을 줄인다.

**금지 shortcut**

- Negative fixture 없이 “현재 위반 0”만 확인
- Compile error를 expected architecture rejection으로 오인
- `src/test`만 scan하고 main artifact leakage를 놓침
- 전체 legacy/provider package wildcard allow
- Customer 문자열을 완전히 증명한다고 주장
- OR-Tools가 runtime에서만 비활성이라는 이유로 compile dependency 허용

**검증 command/test**

```bash
# LIVE CANDIDATE / FUTURE ACCEPTED: H4 scope와 sealed source 확인 뒤
./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am verify

./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am \
  -Darchitecture.fixture=verification-depends-on-solver verify

./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am \
  -Darchitecture.fixture=provider-sdk-in-core verify

./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am \
  -Darchitecture.fixture=test-fixture-production-leak verify

./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am \
  -Darchitecture.fixture=core-ambient-clock verify

./mvnw -B -ntp -Dstyle.color=never dependency:tree -Dverbose
jdeps --recursive --multi-release 25 path/to/exact/stable-module.jar
```

Negative fixture command는 non-zero가 기대 결과다. Harness는 subprocess exit만 보지 말고
expected rule ID와 offending edge/symbol을 함께 assert해야 한다.

Test class/method 후보:

- `ModuleDependencyArchitectureTest#allowsOnlyDeclaredStableEdges()`
- `VerifierIndependenceArchitectureTest#rejectsSolverSearchAndCacheReferences()`
- `ProviderIsolationArchitectureTest#rejectsProviderSdkFromStableModules()`
- `OptionalBackendArchitectureTest#rejectsOrToolsFromDefaultReactor()`
- `CustomerIsolationArchitectureTest#rejectsCustomerSpecificProductionBranch()`
- `CustomerIsolationArchitectureTest#allowsExactIdentityDataOnlyInApprovedCatalogBoundary()`
- `CustomerIsolationArchitectureTest#rejectsExecutableRuleFromProfileCatalog()`
- `CoreAmbientAccessArchitectureTest#rejectsEnvironmentAndSystemPropertyReads()`
- `CoreAmbientAccessArchitectureTest#rejectsSystemClockReads()`
- `CoreAmbientAccessArchitectureTest#rejectsUnseededOrGlobalRandom()`
- `CoreAmbientAccessArchitectureTest#rejectsMutableStaticRegistry()`
- `InternalPackageArchitectureTest#rejectsCrossModuleInternalAccess()`
- `TestFixtureLeakageArchitectureTest#rejectsFixtureOnProductionClasspath()`
- `NegativeFixtureHarnessTest#failsForExpectedRuleRatherThanIncidentalCompileError()`

**기대 결과와 pass 판정**

- Positive DAG fixture PASS
- 모든 known-negative fixture가 expected rule ID로 FAIL
- Default root `verify`에서 architecture suite가 자동 실행
- Provider/backend/verifier forbidden edge 0, declared generic scope의 unauthorized customer
  executable branch 0, approved catalog identity positive fixture PASS
- Architecture report/test count가 manifest에 존재

**실패 해석**

- Negative fixture green: rule coverage 또는 test discovery가 깨짐
- 다른 compile error로 red: fixture가 검사 대상 bytecode까지 도달하지 못함
- Local만 pass: plugin/test ordering 또는 filesystem-dependent scan 가능

**Rollback**

Rule 추가는 rule ID별로 독립 유지한다. False-positive가 생겨도 global skip하지 않고 해당
rule/fixture/allowlist entry만 근거와 함께 되돌리거나 수정한다.

**다음 handoff**

Rule inventory, positive/negative report, 검출 한계와 allowlist digest를 WP00-5에 넘긴다.

### WP00-5 — 통합 verify, evidence sealing, 독립 review와 Phase 01 handoff

**목적과 이유**

서로 다른 시점의 green log를 조합하지 않고 한 source snapshot에서 Phase exit를 판정한다.

**사전조건**

- WP00-0~4 evidence
- H0~H4 승인 기록
- Reviewer가 implementation owner와 독립적
- Evidence manifest format과 digest boundary 승인

**예상 evidence bundle**

```text
<approved-content-addressed-evidence-root>/phase-00/<manifest-id>/
  pre-review/
    manifest.json
    source/
      source-tree.digest
      tracked-files.txt
      dirty-state.txt
    build/
      toolchain.txt
      reactor.txt
      effective-pom.xml
      dependency-tree.txt
      online-verify.log
      offline-verify.log
      artifact-digests.sha256
      reproducibility-comparison.txt
    architecture/
      rules.json
      positive-report.xml
      negative-fixture-results.json
      jdeps/
    legacy/
      golden-report.xml
      behavior-matrix.json
      collision-inventory.txt
      redaction-report.txt
    manifest.sha256
  review/
    review-report.md
    reviewed-manifest.sha256
  acceptance/
    acceptance-receipt.json
```

경로와 schema는 **PROPOSED**다. 실제 evidence storage 정책을 따르되 세 계층
`pre-review → review → acceptance`를 합치지 않는다. 이 root는 Maven `target/` 또는 다른
`clean` 삭제 대상이면 안 된다. Owner가 위치/retention/ACL을 승인하지 않았으면 이 예를
실제 path로 확정하지 않고 H5 전 STOP한다.

**구체 행동**

1. Clean source snapshot에서 wrapper root `verify`를 실행한다.
2. 같은 snapshot에서 module list, dependency tree, positive/negative architecture,
   legacy golden, offline build, reproducibility 비교를 다시 실행한다.
3. `target/` staging leaf가 모두 완성되면 승인된 evidence root로 한 번 promotion한다.
   Acceptance는 staging path를 직접 참조하지 않는다.
4. `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`가 참조하는 nested leaf를 재귀적으로 열거하고
   canonical relative path의 bytewise stable order로 정렬한다.
5. 각 leaf에 canonical relative path, byte length와 SHA-256을 기록한다. Manifest는 자기
   digest나 이후 review/acceptance 파일을 input으로 hash하지 않는다.
6. Seal/verify tool은 nested omission, symlink, missing file, duplicate/path collision,
   same path/different bytes와 one-byte mutation을 non-zero로 거부한다. Leaf/manifest를
   쓰거나 자동 재봉인하는 verifier는 사용하지 않는다.
7. Independent reviewer는 sealed manifest와 source snapshot으로 명령을 재실행한다.
8. Finding이 있으면 원 pre-review/review를 수정하지 않는다. Source를 고친 뒤 새 manifest
   ID로 WP00-5를 다시 수행한다.
9. Review PASS와 correction closure 뒤 acceptance owner가 post-review receipt를 발행한다.
10. Scheduler registry를 업데이트할 권한이 있는 owner만 Phase 00 상태를 바꾼다.
11. Phase 01 owner에게 §15의 artifact와 확인 명령을 전달하고 소비 확인을 받는다.

**선택 근거**

Evidence가 source와 toolchain을 가리키지 않으면 재현할 수 없고, review 뒤 source가 바뀌면
review가 더 이상 그 build를 승인하지 않는다. Immutable 세 단계가 “review된 것”과 “현재
코드”를 연결한다.

**금지 shortcut**

- 이전 WP의 서로 다른 commit log를 한 manifest로 조합
- Failure log 삭제 후 재실행 없이 PASS 표시
- Review report를 implementation owner가 acceptance로 대체
- Source 변경 뒤 기존 review/receipt 재사용
- Phase 01 READY를 문서 파일만 보고 표시
- Phase 14A/14B 또는 Phase 13 gate를 Phase 00 build로 열기

**통합 검증 command**

```bash
# LIVE CANDIDATE / FUTURE ACCEPTED: sealed source와 H5 scope 확인 뒤
./mvnw --version
./mvnw -B -ntp -Dstyle.color=never clean verify
./mvnw -B -ntp -Dstyle.color=never dependency:tree -Dverbose
./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am verify
./mvnw -B -ntp -Dstyle.color=never \
  -pl legacy/gcp-placeholder -am verify

# Evidence 파일을 승인된 root로 promotion한 뒤
<seal-tool> <approved-evidence-root>/<manifest-id>/pre-review
<verify-tool> <approved-evidence-root>/<manifest-id>/pre-review
```

`<seal-tool>`/`<verify-tool>` 이름과 path는 **PROPOSED/OPEN**이다. H2/H5 승인 전 임의 script
이름을 계약으로 만들지 않는다. 필수 oracle은 위 canonical-path/length/SHA-256과 corruption
rejection이며 단순 `shasum <files...>` 성공으로 대체하지 않는다.

**기대 결과와 pass 판정**

- Default root build는 optional backend, native library, cloud credential 없이 PASS
- Reactor cycle/forbidden edge/test leakage 0
- Known-negative fixture는 expected architecture rule로만 FAIL
- Fresh Surefire/Failsafe report에 expected class와 non-zero test count 존재
- Legacy success·failure·redaction golden PASS
- Offline prefetched build PASS
- 두 clean build artifact digest PASS
- Independent review와 post-review acceptance receipt가 같은 manifest digest 참조

**실패 해석**

- Build/arch/legacy 중 하나라도 fail이면 Phase 전체는 NOT_ACCEPTED
- Review가 다른 digest를 참조하면 stale review
- Receipt가 pre-review manifest를 직접 참조하고 review digest를 생략하면 evidence chain 불완전

**Rollback**

Acceptance 전에는 마지막 accepted production state를 건드리지 않는다. Phase 00 자체가 첫
Phase이므로 current legacy baseline이 rollback 기준이다. Manifest/review/receipt는 삭제하거나
덮어쓰지 않고 superseded 관계를 남긴다.

**다음 handoff**

Phase 01 owner가 accepted coordinates, wrapper 명령, fixture test-jar consumption, architecture
rules와 세 evidence key를 재확인한 뒤에만 Phase 01 entry를 연다.

## 11. 테스트 전략과 false-green 방지

### 11.1 Red→green 원칙

Architecture test의 red는 “원래 build가 망가졌다”가 아니라 **의도한 위반을 guard가 잡았다**는
증거여야 한다.

1. Positive fixture가 먼저 compile/test되는지 확인한다.
2. Negative fixture에서 한 가지 위반만 추가한다.
3. Guard 도입 전 또는 guard disabled harness에서 fixture가 검사 가능한 상태까지 build되는지
   확인한다.
4. Guard enabled에서 expected rule ID, offending module/symbol과 non-zero exit를 assert한다.
5. 위반을 제거하면 같은 fixture가 green이 되는지 확인한다.
6. Root default lifecycle에서 suite가 실제 실행되고 test count가 0이 아닌지 확인한다.

“명령이 실패했다”만 보는 oracle은 dependency download 실패나 syntax error를 architecture
success로 오인한다. “명령이 통과했다”만 보는 oracle은 test discovery 0을 guard success로
오인한다.

### 11.2 Fixture, builder와 oracle 후보

| Test support | 역할 | 안전 조건 |
|---|---|---|
| `ArchitectureFixtureBuilder` **PROPOSED/TEST-ONLY** | 최소 module/POM/source fixture 구성 | Production artifact에 포함 금지 |
| `ExpectedViolation` record **PROPOSED/TEST-ONLY** | rule ID, edge/symbol, message fragment 표현 | 전체 compiler text에 취약하게 결합하지 않음 |
| `LegacyRequestFixture` **PROPOSED/TEST-ONLY** | Existing HTTP payload bytes 보존 | Future canonical DTO로 재사용 금지 |
| `FakeObjectStore`/`FakeWorkflowGateway` **PROPOSED/TEST-ONLY** | deterministic failure/success 주입 | Production fallback 등록 금지 |
| Golden HTTP oracle | status/content-type/body shape/redaction 비교 | Secret/object internals를 fixture에 저장 금지 |
| Artifact digest oracle | 정렬된 artifact set과 SHA-256 비교 | Evidence/log 자체를 build input에 넣지 않음 |

### 11.3 Test category별 적용 범위

| Test 종류 | Phase 00 적용 | 구체 판정 |
|---|---|---|
| Unit | **적용** | Rule parser/matcher, manifest leaf selection, collision inventory helper의 boundary test |
| Contract | **적용** | Module DAG, test-jar 소비, wrapper/toolchain, legacy HTTP behavior contract |
| Integration | **적용** | Root reactor와 child module lifecycle, legacy assembly/resource merge, ArchUnit/Enforcer/`jdeps` 결합 |
| E2E | **제한 적용** | Source snapshot → clean root verify → artifact/evidence까지의 build E2E. RPDPTW solve E2E는 domain 코드가 없어 해당 없음 |
| Architecture | **핵심 적용** | Positive DAG와 rule별 known-negative fixture, provider/customer/backend/verifier 격리 |
| Fault | **적용** | Legacy storage/workflow failure, offline missing artifact, stale/corrupt evidence reference |
| Corruption | **적용** | Fixture class를 main jar에 넣기, manifest leaf digest 변경, wrong module edge 삽입 시 fail |
| Reproducibility | **핵심 적용** | Isolated cache offline build와 두 clean workspace artifact digest equality |
| Security | **제한 적용** | Legacy 500/log redaction, credential-free default build, evidence secret scan. IAM/network는 Phase 11/14 대상 |
| Performance | **관측만** | Build time/artifact size를 기록해 gross regression을 알림. 공식 threshold는 `Q-BENCH-02`와 무관하며 임의 fail 기준을 만들지 않음 |

다음은 이 Phase에서 **해당하지 않는다**.

- Solver solution quality, route feasibility, normalization numeric oracle: Phase 01~07 소유
- Production cloud load/latency/cost test: Phase 11/14B 소유
- ALNS steps/workers/rounds/watchdog acceptance: Phase 14A의 승인된 protocol 소유
- OR-Tools correctness/performance: Phase 13 gate가 열렸을 때만 소유

### 11.4 Class와 method 후보

| 후보 test class | 핵심 method 후보 | 잡아야 하는 false-green |
|---|---|---|
| `RootAggregatorContractTest` | `rootHasPomPackagingAndNoBusinessDependencies()` | Parent가 jar이거나 root dependency가 남음 |
| `ModuleDagContractTest` | `resolvedEdgesEqualApprovedSubset()` | 선언 POM만 보고 transitive edge를 놓침 |
| `VerificationIndependenceTest` | `verificationReferencesCoreOnly()` | Verifier가 solver/cache bytecode 사용 |
| `ProviderIsolationTest` | `stableArtifactsReferenceNoProviderSdk()` | Dependency scope 조작으로 bytecode reference를 숨김 |
| `OptionalBackendClosedGateTest` | `defaultReactorContainsNoOrToolsSymbolOrArtifact()` | Dependency는 없지만 copied backend class/resource가 남음 |
| `CustomerIsolationTest` | `genericCodeRejectsKnownCustomerBranchButCatalogAllowsIdentityData()` | Profile identity data까지 막거나 catalog executable rule을 허용 |
| `CoreAmbientAccessTest` | `rejectsEnvironmentClockRandomAndMutableStaticRegistry()` | Empty core가 green인 동안 후속 ambient access guard가 실제로 없음 |
| `TestFixturePublicationTest` | `fixtureExistsOnlyInTestsClassifier()` | Main jar와 production classpath leakage |
| `LegacyGoldenContractTest` | `preservesSuccessAndFailureMatrix()` | Success path만 비교 |
| `ReproducibleArchiveTest` | `twoCleanBuildsHaveSameArtifactSetAndDigest()` | 한 workspace에서 두 번 package하여 cache effect를 놓침 |
| `EvidenceManifestIntegrityTest` | `rejectsNestedOmissionSymlinkMissingPathCollisionAndOneByteMutation()` | 얕은 glob, 자동 재봉인, 자기 참조 digest나 stale review 허용 |

모든 이름은 **PROPOSED**다. Method signature를 public API로 내보내지 않는다.

### 11.5 Pass를 선언하기 전 확인

- Surefire/Failsafe report에서 expected test class와 test count가 실제 존재하는가?
- `clean` 이후 생성된 report인가, 이전 run의 stale XML이 아닌가?
- Surefire `*Test`와 Failsafe `*IT` naming/include/lifecycle binding이 effective POM과 맞는가?
- Negative test가 expected rule ID 때문에 실패했는가?
- `-DskipTests`, `maven.test.skip`, profile auto-disable가 숨어 있지 않은가?
- `dependency:tree`의 omitted/conflict 항목까지 보았는가?
- `jar tf`가 main jar와 `tests` classifier를 정확히 구분했는가?
- `jdeps` 입력이 현재 build의 exact stable artifact인가?
- Offline build가 online build와 같은 isolated repository만 사용했는가?
- 두 build가 서로 다른 clean workspace인가?
- Legacy fake가 production service loader/resource에 등록되지 않았는가?

## 12. 사람 checkpoint, evidence bundle과 stop/resume

### 12.1 Review 질문

**H0 baseline review**

- Current synthetic engine을 target solver로 오인하지 않았는가?
- API/worker의 endpoint × method × path × parse/validation/provider failure stage를 나누고
  그 결과인 400/404/405/500/202, empty candidate, storage/workflow failure를 기록했는가?
- Malformed JSON과 parsed-invalid request의 status를 한 값으로 일반화하지 않았는가?
- Golden이 behavior를 보존할 뿐 future API를 승인하지 않는다고 적었는가?

**H1/H2 build review**

- Legacy Jackson version convergence를 target 전체 strictness 완화 없이 처리했는가?
- Shade service/resource collision 중 runtime에 필요한 merge와 불필요 중복을 구분했는가?
- Wrapper checksum과 plugin version source가 검증 가능한가?
- Archive timestamp가 source snapshot에서 결정적으로 파생되는가?

**H3/H4 architecture review**

- Test fixture consumer가 `type=test-jar`, `classifier=tests`, `scope=test`를 모두 갖는가?
- Verification이 solver/search/cache와 compile/runtime에서 독립인가?
- Profile catalog identity data의 허용과 generic customer executable branch 금지를 구분했는가?
- Core ambient config/clock/random/static-registry rule마다 negative fixture가 있는가?
- Source/dependency/bytecode scan의 coverage limitation이 기록되었는가?
- Rule마다 known-negative fixture가 있고 incidental compile failure를 배제했는가?

**H5 acceptance review**

- Build, architecture, legacy evidence가 같은 source/manifest digest인가?
- Independent reviewer가 실제 명령을 재실행했는가?
- Correction 후 새 manifest와 review를 만들었는가?
- Acceptance receipt 없이 Phase 01 READY를 주장하지 않았는가?

### 12.2 Evidence bundle 최소 내용

| Evidence key | 반드시 포함할 파일/로그 | digest 연결 |
|---|---|---|
| `E-P00-BUILD` | source/toolchain, wrapper, reactor, effective POM, dependency tree, clean/online/offline logs, artifact digest 비교 | pre-review manifest leaf |
| `E-P00-ARCH` | rule inventory, positive report, rule별 negative result, `jdeps`/bytecode/source scan, allowlist와 limitation | 같은 manifest |
| `E-P00-LEGACY` | endpoint/behavior matrix, success/failure report, collision inventory, redaction/credential-free result | 같은 manifest |
| Review | finding/decision/residual risk, reviewed manifest digest | immutable review digest |
| Acceptance | accepted review digest, source digest, owner/time/decision | post-review receipt digest |

Log에는 raw credential, access token, signed URL, authorization header를 넣지 않는다. Redaction
test는 “특정 secret 문자열이 없음”뿐 아니라 exception object, provider response, full object
key가 body로 직렬화되지 않는지도 본다.

모든 pre-review leaf는 `target/` 밖의 승인된 content-addressed/digest-protected evidence
root로 promotion된 뒤 참조한다. Manifest는 nested leaf의 canonical relative path, byte
length와 SHA-256을 기록하고 corruption oracle을 통과해야 한다.

### 12.3 Stop/resume 조건

즉시 멈추는 조건:

- Source fingerprint 또는 Phase scope가 바뀌었는데 영향 review가 없음
- 다른 작업의 file과 overlap되어 owner가 불분명함
- Legacy Jackson/collision 정책을 global skip이나 silent upgrade로만 해결 가능해 보임
- Root build에 provider/customer/optional backend dependency를 넣어야 한다는 제안
- Negative fixture가 expected rule이 아니라 다른 compile/network 실패로 red
- Default verify가 cloud credential/native backend를 요구
- Review가 sealed manifest와 다른 source digest를 참조

Resume 조건:

- 변경된 source section과 fingerprint를 문서화하고 owner가 재승인
- Overlap owner와 file boundary 합의
- H1/H2/H3/H4의 decision record 확보
- 실패의 최소 재현 fixture와 expected oracle 확보
- 새 source snapshot에서 pre-review bundle을 처음부터 재생성

## 13. 흔한 오해와 anti-pattern

1. **“폴더가 있으니 module도 있다.”**
   POM, aggregator registration, source/artifact와 verify evidence가 없으면 module이 아니다.

2. **“빈 interface를 만들면 다음 Phase가 빨라진다.”**
   불변조건 없이 먼저 굳힌 API는 Phase 01의 domain 설계를 왜곡한다. `package-info`로 경계만
   예약한다.

3. **“현재 AlnsBatchEngine 이름에 ALNS가 있으니 solver baseline이다.”**
   현재 코드는 input을 풀지 않는 합성 placeholder다. Legacy characterization 대상일 뿐이다.

4. **“Verifier가 solver helper를 읽어도 계산만 같으면 된다.”**
   같은 bug/cache를 공유하면 independent verification이 아니다.

5. **“Test fixture는 편하니 main JAR로 공유한다.”**
   Production code가 fixture builder에 결합되고 test-only default가 런타임으로 샌다.

6. **“GCP는 legacy니까 모든 provider package를 allowlist한다.”**
   예외는 exact legacy module과 reason으로 좁힌다. Stable module로 전파하지 않는다.

7. **“AWS가 선택되었으니 SDK skeleton을 지금 넣는다.”**
   Target 선택과 implementation/cutover authority는 다르다. AWS는 Phase 11/14 gate다.

8. **“OR-Tools를 dependency에 넣고 사용만 안 하면 gate를 지킨다.”**
   Phase 13 gate는 dependency, capability 광고, package와 activation 모두 닫는다.

9. **“아직 숫자가 없으니 적당한 worker/step default를 둔다.”**
   `Q-BENCH-02`는 OPEN/EXPERIMENT_REQUIRED다. TEST-ONLY 값도 explicit provenance를 갖고
   production default가 되지 않아야 한다.

10. **“Root verify가 green이면 Phase accepted다.”**
    Legacy/negative/reproducibility evidence, independent review와 acceptance receipt가 필요하다.

11. **“Negative fixture가 compile 실패했으니 guard가 동작한다.”**
    Syntax/dependency download 실패일 수 있다. Expected rule ID와 offending edge를 assert한다.

12. **“Warning을 없애기 위해 dependencyManagement로 전부 맞춘다.”**
    Legacy behavior를 바꿀 수 있다. Mixed Jackson graph와 Shade collision은 H1에서 별도
    승인한다.

13. **“Evidence에 모든 output을 hash하면 강하다.”**
    Manifest가 자기 자신이나 이후 review를 hash하면 순환/재생성 불가능 구조가 된다.

14. **“문서 review PASS는 구현 승인이다.”**
    현재 Phase 00 review의 `PASS_WITH_RESIDUAL_BLOCKERS`는 문서 계약 review다. 구현 evidence와
    acceptance가 아니다.

15. **“Phase 13을 먼저 하면 quality가 좋아지니 안전하다.”**
    Phase 13은 14A ALNS acceptance receipt와 C-17 승인 전 시작 금지다.

## 14. Phase 00 exit checklist와 Definition of Done

아래는 AND gate다. 하나라도 비어 있으면 Phase 00은 `NOT_ACCEPTED`다.

### 14.1 Entry와 scope

- [ ] Source fingerprint와 exact section을 재검증했다.
- [ ] Implementation owner, reviewer, acceptance owner와 변경 file scope가 승인되었다.
- [ ] Dirty/untracked unrelated 작업을 기록하고 보존했다.
- [ ] H0~H4 decision과 residual risk owner가 있다.

### 14.2 Legacy

- [ ] Current success behavior golden이 이동 전후 같다.
- [ ] Endpoint × method × path × parse/validation/provider failure-stage matrix의 exact
      status/content-type/body bytes가 이동 전후 같다.
- [ ] API malformed JSON의 observed `500`, parsed-invalid request의 `400`, API fallback
      `404`, worker non-POST `405`, missing result `202`를 한 generic rule로 합치지 않았다.
- [ ] Empty candidate, storage/workflow failure가 deterministic하게 test된다.
- [ ] Failure body/log redaction이 통과한다.
- [ ] Cloud credential/network 없이 legacy tests가 통과한다.
- [ ] Legacy dependency와 source가 explicit legacy module 밖으로 새지 않는다.
- [ ] Jackson convergence와 Shade collision 정책이 승인되고 evidence에 기록되었다.

### 14.3 Build와 reactor

- [ ] Root는 `packaging=pom`이고 business/provider/optimizer dependency가 0이다.
- [ ] Wrapper version과 checksum, Java 25/Maven policy가 고정되었다.
- [ ] Lifecycle plugin/dependency version이 effective POM에서 pin되었다.
- [ ] 여섯 stable module과 두 build/test module, legacy module이 reactor에 정확히 등록되었다.
- [ ] Reactor cycle이 0이고 allowed production edge의 subset이다.
- [ ] Default clean root `verify`가 optional backend/native/cloud credential 없이 통과한다.
- [ ] Prefetched isolated repository의 offline verify가 통과한다.
- [ ] 두 clean workspace의 비교 대상 artifact set과 SHA-256이 같다.

### 14.4 Skeleton과 architecture

- [ ] Stable namespace는 `com.ronext.rpdptw`다.
- [ ] Phase 00 production source는 책임을 설명하는 package skeleton이며 fake behavior가 없다.
- [ ] Core의 `input/domain/normalization/travel/propagation/evaluation.*`, solver,
      verification/result, application `port.in/out`/service/execution과 단수 `capability`
      package가 accepted manifest와 일치한다.
- [ ] Core outward dependency 0이다.
- [ ] Verification → solver/search/cache dependency/reference 0이다.
- [ ] Stable generic code의 provider SDK/legacy/OR-Tools reference와 customer-name
      executable conditional/package가 0이다.
- [ ] Profile catalog와 승인된 adapter authorization의 exact identity/version data만 허용되고
      arbitrary executable rule/solver branch는 0이다.
- [ ] Core의 ambient environment/system-property, system clock, unseeded/global random,
      mutable static registry access가 각각 negative fixture로 거부된다.
- [ ] Cross-module `.internal` access가 0이다.
- [ ] Test fixture는 tests classifier에 있고 main artifact/production classpath leakage가 0이다.
- [ ] Positive architecture fixture가 통과한다.
- [ ] 각 forbidden rule의 negative fixture가 expected rule ID로 실패한다.
- [ ] Architecture rule test가 root default lifecycle에서 실제 실행된다.
- [ ] Scan coverage, limitation, allowlist owner/expiry가 evidence에 있다.

### 14.5 Evidence와 acceptance

- [ ] `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`가 한 immutable source manifest를 참조한다.
- [ ] Acceptance input은 `target/`이 아니라 `clean` 영향 밖의 승인된 evidence root에 있다.
- [ ] Leaf → manifest digest 경계가 비순환이고 canonical path/byte length/SHA-256으로
      재검증 가능하다.
- [ ] Nested omission, symlink, missing file, path collision, same path/different bytes와
      one-byte mutation이 non-zero로 거부된다.
- [ ] Independent reviewer가 sealed bundle에서 명령을 재실행했다.
- [ ] Finding correction 뒤 새 manifest/review를 발행했다.
- [ ] Post-review acceptance receipt가 accepted review와 source digest를 참조한다.
- [ ] Scheduler/acceptance owner만 official status를 갱신했다.
- [ ] Phase 01 owner가 wrapper/module/test-fixture 계약을 소비 검증했다.
- [ ] OPEN/GATED/DEFERRED 항목을 임의로 닫거나 default로 만들지 않았다.

### 14.6 DoD 문장

Phase 00은 다음 문장이 사실일 때만 Done이다.

> 같은 immutable source snapshot에서, current legacy의 success와 failure behavior가
> 명시적 legacy module 안에서 보존되고, Java 25/Maven Wrapper 기반의 business-dependency-free
> root reactor가 optional backend·native library·cloud credential 없이 clean/online/offline
> verify를 통과하며, approved stable DAG·verifier 독립성·provider/customer/backend 격리·
> profile identity 허용 경계·core ambient nondeterminism 금지·test-fixture 비누출이
> positive/known-negative architecture test로 증명되고, 두 clean build의
> artifact digest가 일치하며, `E-P00-BUILD`/`E-P00-ARCH`/`E-P00-LEGACY`가 독립 review와
> post-review acceptance receipt로 봉인되어 Phase 01 consumer가 재현했다.

## 15. Phase 01 인계 계약

### 15.1 Producer/consumer artifact

| Phase 00 producer artifact | Phase 01이 사용하는 방법 | 확인 명령/방법 | Broken handoff 증상 |
|---|---|---|---|
| Accepted root parent/Wrapper | 같은 Java/Maven/plugin policy로 build | `./mvnw --version`; root `verify` | Local Maven에서만 build되거나 plugin version drift |
| Accepted `rpdptw-core` coordinates | Canonical/normalized immutable type의 소유 module | `-pl rpdptw/core -am verify` | Core가 provider/legacy를 transitively 요구 |
| Stable DAG | Phase 01 type을 core 안에만 두고 outward dependency 금지 | dependency tree + architecture suite | Input DTO 구현 때문에 core→adapter/Jackson/provider edge 필요 |
| Test fixture tests classifier | Exact numeric/time/error fixture를 test scope에서 공유 | consumer POM의 `type=test-jar`, `classifier=tests`, `scope=test` | Fixture가 main compile에서만 보이거나 production jar에 포함 |
| `E-P00-ARCH` | 새 Phase 01 source가 forbidden edge를 만들면 root verify fail | architecture-rules module + negative control | Rule test가 0건 실행되거나 Phase 01에서 skip해야 green |
| `E-P00-LEGACY` | External legacy behavior와 future canonical input를 구분 | golden report와 adapter boundary review | Raw legacy map/DTO를 core public API로 재사용 |
| `E-P00-BUILD` | Same source/toolchain/evidence convention을 이어 사용 | manifest digest 재검증 | Phase 01 evidence가 다른 unrecorded build/toolchain 사용 |
| Acceptance receipt | Phase 01 entry authorization | receipt→review→manifest→source digest chain | 문서 PASS만 있고 accepted receipt가 없음 |

Phase 01은 이 표의 package 예시를 독자적으로 확정하지 않는다. H3 이후 accepted Phase 00
coordinate/package manifest가 §6.2/§9.1 예시와 다르면 accepted manifest를 사용하고 차이의
approval record를 함께 소비한다.

### 15.2 Phase 01에 넘기지 않는 것

- `CanonicalInput`, `NormalizedInput`, `ProblemInstance`의 빈 Phase 00 class
- Legacy raw map/Jackson DTO를 canonical schema로 승인한 주장
- Numeric/time/service/alias policy의 임의 default
- Public API/schema compatibility promise
- Phase 13 backend 또는 Phase 14 official number

Phase 01 entry 확인:

```bash
# FUTURE: Phase 00 acceptance 뒤
./mvnw --version
./mvnw -B -ntp -Dstyle.color=never verify
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core -am verify
./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am verify
```

그리고 Phase 01 owner는 `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`의 manifest digest와
post-review acceptance receipt를 직접 확인한다. 파일이 있다는 사실만으로 accepted라
판정하지 않는다.

## 16. Source → requirement → WP → test/evidence traceability

| Source/section | Phase 00 requirement | Work package | Test/evidence |
|---|---|---|---|
| Master §1.1~§1.5, C-02/C-03/C-04 | Future-ready, maintainable, layered boundary; customer branch 금지 | WP00-3, WP00-4 | Module DAG, customer isolation negative fixture, `E-P00-ARCH` |
| Master C-17, C-19 | Optional hybrid와 variant gate 보존 | WP00-3, WP00-4, WP00-5 | OR-Tools artifact/symbol 0, gate checklist |
| Master C-20 | AWS target 선택과 구현/cutover authority 분리 | WP00-3, WP00-4 | Stable provider SDK reference 0 |
| Master C-21 | Independent verifier gate | WP00-3, WP00-4 | Verification→solver/search/cache negative fixture |
| Master C-22 | Strong reproducibility | WP00-2, WP00-5 | Offline build, two-clean-build digest, `E-P00-BUILD` |
| Final Domain §2, §7~§8 | Request pair/stable authority와 solver/verifier 분리의 이유 | WP00-3, WP00-4 | Package ownership review, verifier independence |
| Final Architecture §2.1~§2.7 | Target Maven tree와 dependency DAG | WP00-2, WP00-3 | Reactor/dependency tree/cycle report |
| Final Architecture §5.6 | Build enforcement | WP00-4 | Enforcer/ArchUnit/`jdeps`/negative fixtures |
| Integrated design §3~§4 | Root aggregator, module skeleton, Java 25, legacy isolation | WP00-1~WP00-4 | Root contract, wrapper, legacy golden, architecture suite |
| Integrated design §22~§25 | Test matrix, invariants, anti-pattern, ADR/deferred | WP00-0~WP00-5 | Category matrix, decision/checkpoint records |
| Open questions `Q-BENCH-02` | Official value OPEN/EXPERIMENT_REQUIRED | 모든 WP | Production numeric default absence review |
| Open questions `Q-INFRA-01` | AWS target resolved, implementation still phased | WP00-3, WP00-4 | Provider-free stable/default build |
| Open questions `Q-VAR-01` | Optional variant DEFERRED | WP00-3, WP00-5 | Variant package/capability absence |
| Realization Plan Phase 00 | Current preserve + reactor/guard + `E-P00-*` | WP00-0~WP00-5 | Build/arch/legacy evidence triad |
| Realization Plan evidence DAG | Manifest→review→acceptance 단방향 | WP00-5 | Digest chain integrity test |
| Phase 00 review P00-R-001 | Test fixture classifier/type/scope correction | WP00-3, WP00-4 | Main jar/classpath leakage test |
| Phase 00 review P00-R-002 | Jackson convergence와 Shade collision gate | WP00-1, WP00-2 | Dependency/collision inventory와 H1 approval |
| Phase 00 review P00-R-003 | Immutable source snapshot과 non-recursive sealing | WP00-0, WP00-5 | Manifest integrity/corruption test |
| Phase 00 review P00-R-004 | Architecture scan coverage/negative fixtures 명시 | WP00-4 | Rule limitation + known-negative reports |
| Phase 00 review P00-R-005 | Legacy failure-path golden 확대 | WP00-1 | 400/404/405/500/202, empty/provider failure suite |
| Phase 00 review P00-R-006 | Adjacent text와 actual status drift 방지 | WP00-0, WP00-5 | Phase 01/status registry 재확인 |
| Human guide review P00-HG-R-001 | Reviewed Phase 00 package tree와 aggregator | WP00-3 | Accepted coordinate/package manifest와 exact package-info inventory |
| Human guide review P00-HG-R-002 | Endpoint/failure-stage별 pre-move oracle | WP00-1 | Exact response-byte behavior matrix |
| Human guide review P00-HG-R-003 | Clean-safe evidence promotion과 corruption-resistant seal | WP00-0, WP00-5 | Canonical path/length/hash manifest와 corruption suite |
| Human guide review P00-HG-R-004 | Generic customer branch 금지와 profile identity 허용 분리 | WP00-4 | Approved token/location manifest와 positive/negative fixture |
| Human guide review P00-HG-R-005 | HEAD baseline과 unapproved live drift 분리 | WP00-0, WP00-5 | Timestamped status/diff/untracked/source digests와 scheduler join/STOP |
| Human guide review P00-HG-R-006 | Core ambient nondeterminism guard | WP00-4 | Env/property/clock/random/static registry negative fixtures |
| Phase 01 entry contract | Accepted core/build/fixture/evidence 소비 | WP00-3, WP00-5 | Consumer smoke commands와 handoff receipt |

## 17. 이 guide 자체의 정적 검증 방법

이 절은 guide 작성 품질을 확인하는 것이며 Phase 00 구현 evidence가 아니다.

```bash
# 파일 범위 확인
git status --short --untracked-files=all
git diff -- \
  docs/implementation/human-guides/phases/phase-00-human-implementation-guide.md

# Markdown whitespace
# Tracked 변경 전체
git diff --check
# 이 guide가 아직 untracked라면 별도로 추가 파일의 whitespace 검사
git diff --no-index --check /dev/null \
  docs/implementation/human-guides/phases/phase-00-human-implementation-guide.md

# 상대 link target과 fragment/heading 확인
# Markdown link를 추출해 상대 target이 존재하는지 검사하고,
# fragment가 있으면 target의 explicit id 또는 GFM heading slug와 대조한다.

# OPEN/GATED/DEFERRED와 Phase 수 표현 확인
rg -n 'OPEN|GATED|DEFERRED|EXPERIMENT_REQUIRED|00부터 14|총 15' \
  docs/implementation/human-guides/phases/phase-00-human-implementation-guide.md

# HEAD에는 없고 live에는 미승인인 wrapper/module 명령의 상태 label 확인
rg -n '\\./mvnw|-pl ' \
  docs/implementation/human-guides/phases/phase-00-human-implementation-guide.md
```

Guide 검증이 green이어도 `HEAD` baseline의 과거 `NOT_STARTED`와 live scheduler의
`CHANGES_REQUIRED_FIX_01_IN_PROGRESS`를 합치지 않는다. 교정 시 live acceptance는
`REJECTED_PENDING_FIX_01_REGENERATION`이며, 새 immutable evidence·독립 review·receipt 없이
Phase 00/01 상태를 올리지 않는다.

## 18. 남아 있는 OPEN/GATED/DEFERRED 요약

- **OPEN / EXPERIMENT_REQUIRED**: `Q-BENCH-02`의 official steps/workers/rounds/watchdog,
  quality/performance/cost acceptance 값
- **OPEN**: Public Java API와 wire/schema, Phase 00 architecture helper의 exact internal 이름
- **사람 승인 필요**: Legacy Jackson convergence, Shade collision 처리, ArchUnit/새 plugin
  exact version, source-derived archive timestamp 규칙
- **사람 승인 필요**: Current 상위 Architecture의 profile별 module 추천과 reviewed
  implementation baseline의 capabilities/profile-catalog 배치 차이; H3 accepted manifest 전
  module/package 이름은 PROPOSED
- **OPEN / 사람 승인 필요**: `clean` 영향 밖의 evidence root, retention과 ACL; 승인 전
  `target/`은 disposable staging일 뿐 acceptance input이 아님
- **GATED**: Phase 13 optional hybrid/OR-Tools; 14A acceptance receipt와 C-17 승인 전 금지
- **GATED**: AWS adapter/distribution은 Phase 11, official production calibration/cutover와
  authority는 Phase 14B
- **DEFERRED**: `Q-VAR-01` optional variant와 restart condition 전 관련 skeleton

이 목록을 숫자, provider fallback, fake success 또는 production default로 임의로 닫지 않는다.
