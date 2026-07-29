# Phase 02 사람용 구현 가이드 — Prepared travel과 immutable problem

> 이 문서는 Java 기초와 CVRPTW 경험은 있지만 이 저장소를 처음 보는 사람이 Phase 02를 직접 구현하고 판정하기 위한 교육형 작업 지시서다. 완성 코드를 제공하거나 구현 완료를 주장하지 않는다.

## 1. 문서 metadata

| 항목 | 값 |
|---|---|
| Canonical Phase | `02 / 14`가 아니라 **Phase 00~14 중 Phase 02**, 즉 총 15개 canonical Phase의 세 번째 단계 |
| 주제 | 모든 방향 이동 자료의 사전 준비와 immutable `ProblemInstance` |
| 가이드 상태 | `CORRECTED_ROUND_02 / AWAITING_INDEPENDENT_RECHECK` |
| 실제 구현 상태 | `NOT_STARTED / NOT_ACCEPTED` |
| 현재 readiness | `BLOCKED_BY_ENTRY_GATES` |
| 작성 기준일 | 2026-07-29, Asia/Seoul |
| 조사 기준 commit | `7cc890ee1d0805df5ae14b633127fade4f978639` |
| 선행 Phase | [Phase 01 — canonical input과 normalization](../../phases/phase-01-canonical-input-normalization.md) |
| 다음 Phase | [Phase 03 — route propagation과 evaluation kernel](../../phases/phase-03-route-propagation-evaluation-kernel.md) |
| 예상 독자 | Java record/interface/sealed type과 Maven test를 이해하고 CVRPTW matrix·route 계산 경험이 있으나 RPDPTW identity와 이 repository의 gate를 모르는 구현자 |
| Accountable owner | RPDPTW Domain·Input·Matrix owner 역할 |
| 구현 owner | Phase 02 `rpdptw-core/domain`·`travel` 구현 역할 |
| 협의 역할 | Phase 01 input owner, Phase 03 propagation owner, Phase 07 verification owner, Architecture owner, Input·Matrix policy owner |
| 독립 review 역할 | 구현자와 분리된 Phase 02 reviewer |
| 상태 전이 권한 | 총괄 scheduler만 authoritative registry와 `ACCEPTED` 상태를 갱신 |
| 필수 evidence | `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM` |
| 공개 API 상태 | 제안 이름만 있음. Java public API, wire schema, fingerprint encoding은 승인 전 `PROPOSED/OPEN` |

문서 review 통과와 실제 Phase acceptance를 구별한다. Canonical [Phase 02 review](../../reviews/phase-02-review.md)는 문서에 대해 `PASS_AFTER_APPLIED_CORRECTIONS`를 판정했지만 실제 Phase에는 `BLOCKED_NOT_IMPLEMENTED`를 판정했다. 이 사람용 가이드가 작성되어도 그 상태는 바뀌지 않는다.

### 1.1 Source sections와 fingerprints

아래 fingerprint는 조사 시점 working bytes를 `git hash-object <path>`로 계산한 Git blob ID다. 전체 파일 hash를 문서끼리 서로 복제해 갱신하는 authority 체계가 아니라, 이 가이드가 어떤 bytes와 section을 읽었는지 재현하는 **검증용 조사 fingerprint**다. 구현 착수 시 값이 달라졌다면 관련 section을 다시 읽고 requirement·test·evidence 영향부터 review한다.

| Source | 직접 적용한 heading/section | Git blob |
|---|---|---|
| [Canonical Master](../../../master-design.md) | §1.5, §2.1~§2.4, §4.1~§4.7, §5~§8, §13~§17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| [Current Domain map](../../../domain-design.md) | §1, §3, §5, §8~§9, §16 | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| [Current Architecture map](../../../architecture-design.md) | §1~§2, §4~§6, §18~§20 | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| [Final Domain Design](../../../2026-07-26-domain-design.md) | §1~§7, §16~§18, 특히 §2 primer·§6 travel·§7 immutable model | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| [Final Architecture Design](../../../2026-07-26-architecture-design.md) | §1~§2, §5.5~§5.6, §6 | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| [Integrated implementation design](../../../architecture-domain-implementation-design.md) | §1~§3, §5~§7, §19~§25, §27 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| [Question register](../../../master-design-open-questions.md) | §1~§4; `Q-MTX-01~03`, `Q-NUM-01~03`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| [Implementation README](../../README.md) | §0~§7, authority·15 Phase index·ALNS-first DAG | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| [Master Realization Plan](../../master-realization-plan.md) | §1~§9, Phase 01~03, §10~§15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| [Execution Progress and Results](../../execution-progress-and-results.md) | §2, §5, §6.3, §8~§10.1 | `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d` |
| [Phase 01 원본](../../phases/phase-01-canonical-input-normalization.md) | §2.3, §5.1, §6.2, §11.3, §12 | `ca8bff7cf3a0b971dc39a726ec16b323e6673133` |
| [Phase 02 원본](../../phases/phase-02-prepared-travel-immutable-problem.md) | 전체, 특히 §3~§14 | `8b5f115369ca2189c80de12868fb3a63104d9228` |
| [Phase 02 독립 review](../../reviews/phase-02-review.md) | 전체, 특히 findings F-P02-01~06와 blocker ledger | `1b5dcbc0a10d1cd2c3b060a6fe874b736fe3fd65` |
| [Phase 03 원본](../../phases/phase-03-route-propagation-evaluation-kernel.md) | §1~§4, §7~§9, §13~§15 | `74098f7e15cf75bcc443ae009cc475a9b60d63a3` |
| [사람용 Phase 02 review](../reviews/phase-02-review.md) | 전체, `HG-P02-R01~R05` correction input | `c804f3c8e2ad9ec30166b7aca32af6abebaf5627` |
| [현재 root POM](../../../../pom.xml) | 전체 dependency/plugin/module inventory | `1dc675ba17b7f2202f34a22131f152cc2868b075` |
| [현재 SDKMAN 설정](../../../../.sdkmanrc) | Java/Maven version | `62506cdf7dbd884fce313f6bcc1bb65adc5c3b9d` |

[2026-07-26 Master 초안](../../../2026-07-26-master-design.md)과 `docs/codex/`는 historical cross-check일 뿐 현재 결정 authority가 아니다. 이 가이드의 문장이나 기본값을 그 자료에서 가져와 최신 gate를 되돌려서는 안 된다.

Source의 역할과 시점을 한 축으로 섞지 않는다.

- 현재 [Master](../../../master-design.md), [Domain map](../../../domain-design.md), [Architecture map](../../../architecture-design.md)은 최신 상위 의미·gate·module 지도를 제공하지만 모두 `REVIEW`이며 그 자체가 구현 acceptance receipt는 아니다.
- 사용자 고정 입력인 2026-07-26 Domain/Architecture는 Phase 02의 detailed domain·Java 경계 baseline이다. 현재 상위 지도에서 명시적으로 교체한 gate/status는 최신 지도를 따르되, 새 의미를 추론해 dated invariant를 조용히 덮지 않는다.
- Implementation README, master plan, canonical Phase와 review는 이 저장소의 Phase별 entry/exit/evidence baseline이다. 현재 checkout의 POM/source는 live implementation inventory이지 설계 authority나 acceptance authority가 아니다.
- 두 권위 입력 사이에서 semantic 선택이 필요하지만 명시적 replacement/approval을 찾을 수 없으면 `OPEN`, 사람 승인과 last safe point로 남긴다. 구현자가 Java signature나 Maven edge로 결정을 대신하지 않는다.

## 2. 큰 그림

### 2.1 이 Phase가 제품에 필요한 이유

경로 solver가 `차고지 → A → B`라는 순서를 평가하려면 각 실제 위치 사이의 거리와 이동 시간이 필요하다. 외부 입력은 sparse할 수 있고, 같은 위치를 가리키는 pickup·delivery·terminal node가 여러 개일 수 있으며, 제공 이동 시간과 차량 속도로 생성한 이동 시간의 authority도 다르다.

이를 탐색 도중 해소하면 다음 문제가 생긴다.

- 어떤 후보를 먼저 평가했는지에 따라 matrix가 달라질 수 있다.
- solver와 verifier가 서로 다른 fallback을 써서 같은 route를 다르게 판정할 수 있다.
- `A→B` 누락을 `B→A`로 대신해 directed 의미를 잃을 수 있다.
- provider 장애나 잘못된 값이 “missing”으로 낮아져 조용히 생성값으로 바뀔 수 있다.
- mutable table이나 lazy cache가 candidate 사이에 새어 재현성과 독립 검증을 깨뜨릴 수 있다.

Phase 02는 이 위험을 solve 시작 전에 끝낸다.

```text
Phase 01 accepted NormalizedInputArtifact
  → source·identity·absence 검증
  → complete directed PreparedTravel
  → dense identity와 reference 검증
  → immutable ProblemInstance
  → Phase 03 cache-free propagation
```

핵심은 “matrix 계산 utility를 하나 만든다”가 아니다. **어떤 값이 권위인지, 무엇이 의도적 부재인지, 모든 lookup이 언제부터 total인지, solver와 verifier가 같은 artifact를 어떻게 증명하는지**를 한 번에 봉인하는 단계다.

### 2.2 15개 canonical Phase에서의 위치

| Phase | 역할 | Phase 02와의 관계 |
|---:|---|---|
| 00 | Build/reactor와 architecture guard | Phase 02가 들어갈 module/package와 금지 dependency를 먼저 확정 |
| 01 | Canonical input와 normalization | 유일한 producer. Sparse travel declaration, location, speed, normalized facts를 봉인해 전달 |
| **02** | **Prepared travel과 immutable problem** | **현재 Phase. Physical-location directed authority와 dense immutable problem을 완성** |
| 03 | Propagation/evaluation kernel | 첫 직접 consumer. Raw coordinate/speed 없이 route를 앞에서 뒤로 계산 |
| 04 | Capability/customer profile | Problem facts와 Phase 03 SPI에 policy를 bind |
| 05 | Pair insertion/initial portfolio | Phase 02 authority를 Phase 03/04를 통해 사용 |
| 06 | COW ALNS/reproducibility | Immutable problem/travel을 모든 candidate가 공유 |
| 07 | Independent verification/final result | Solver cache가 아닌 동일 prepared authority로 다시 계산 |
| 08 | Application ports/local runtime | Phase 02 artifact를 저장·전달하지만 travel 의미를 재해석하지 않음 |
| 09 | No-DB object storage | Artifact bytes/digest와 pointer를 저장하는 단계 |
| 10 | Provider-neutral coordinator | Prepared snapshot identity를 worker manifest에 연결 |
| 11 | AWS reference distribution | S3/Step Functions/Lambda adapter; Phase 02 core에는 AWS SDK를 넣지 않음 |
| 12 | Provider substitution | Storage/workflow/compute를 바꿔도 prepared semantic identity는 유지 |
| 13 | Optional hybrid/MIP | **Phase 14A ALNS benchmark acceptance receipt와 `C-17` 승인이 있어야만** 시작하는 optional consumer |
| 14 | 14A ALNS benchmark qualification / 14B official cutover | 14A는 Phase 08 뒤 별도 evidence gate, 14B는 official values와 production authority gate |

숫자 순서만 보고 `06 → 13 → 14`를 필수 선형 경로로 만들면 안 된다. 현재 ALNS-first critical path는 `00 → 01 → 02 → ... → 08 → 14A`다. Phase 13은 유효한 14A receipt와 별도 `C-17` 승인이 있어야 열리며, Phase 14B ALNS-only cutover에는 필수 predecessor가 아니다.

### 2.3 Producer와 consumer 계약

Phase 01 producer가 넘기는 것은 raw JSON이나 provider response가 아니라 하나의 immutable `NormalizedInputArtifact`다.

```text
NormalizedInputArtifact
  exact schema/adapter/raw/semantic identities
  external requests/service declarations/vehicles/locations
  normalized integer units and seconds
  typed coordinates
  vehicle speed = PresentValid | Missing
  sparse directed D/U = PresentInteger | Absent
  source/policy provenance
```

Phase 02는 여기에 dense IDs, complete travel과 immutable references를 더한다. Phase 03 consumer에게는 다음만 넘긴다.

```text
PreparedTravelRef + ProblemInstanceRef + Phase02HandoffManifest
  exact fingerprints/digests
  location/vehicle mapping identities
  M² and used-vehicle-time coverage proof
  policy/source identities
  accepted evidence/review references
```

Coordinate, speed, raw travel cell, provider client, partial table와 cache handle은 Phase 03 handoff가 아니다.

## 3. RPDPTW domain primer와 용어집

### 3.1 CVRPTW에서 달라지는 identity

CVRPTW에서는 흔히 customer 하나, node 하나, matrix index 하나가 비슷하게 보인다. 이 프로젝트에서는 다음 identity를 반드시 분리한다.

| 용어 | 이 프로젝트의 의미 | 섞으면 생기는 결함 |
|---|---|---|
| External `Order` | 외부 business input 표현 | Solver pair identity와 alias/schema 책임 혼합 |
| `Request` | 정규화된 하나의 atomic 운송 업무 | Pickup 또는 delivery 하나만 남는 partial pair |
| `SolverNodeId` | Pickup, delivery, start/end terminal 같은 논리 service identity | 같은 건물의 서로 다른 역할을 한 node로 합침 |
| `PhysicalLocationId` | Directed travel table의 endpoint | Node 수만큼 matrix를 부풀리거나 같은 위치 lookup이 달라짐 |
| `Visit` | Route 안에서 node가 나타나는 occurrence | Immutable node 정의와 route 순서 mutation 혼합 |
| `VehicleId` | 구체 입력 vehicle identity | Vehicle class만으로 generated time/terminal/ownership을 섞음 |
| `Route` | 구체 vehicle과 ordered visits의 결합 | Request set만 같으면 같은 route라고 잘못 판단 |

예를 들어 한 창고 위치 `L7`을 start terminal, real pickup service와 실제 delivery service가 함께 참조할 수 있다. Physical location은 하나일 수 있지만 physical solver node 역할은 서로 다르다.

```text
StartTerminalNode(T0) ─┐
RealPickupNode(P17)    ├─→ PhysicalLocationId(L7)
DeliveryNode(D22)      ┘

DeliveryOnlyRequest(R9)
  └─ LogicalInitialLoad
     └─ request/pair ownership only
        └─ no SolverNodeId, PhysicalLocationId, travel, stop or service visit
```

Travel lookup key는 `T0`, `P17`, `D22`가 아니라 `L7`이다. 그렇다고 세 physical solver node identity를 하나로 합쳐서는 안 된다. Delivery-only logical pickup은 이 mapping 그림에 네 번째 node로 들어가지 않는다. 만약 이후 승인된 내부 표현이 logical token이나 prefix marker를 사용하더라도 그것은 physical node/location/travel collection의 원소가 아니다.

### 3.2 Delivery-only와 real pickup-delivery

- **Delivery-only:** 차량이 depot 출발 전에 이미 적재한 물량이다. Logical pickup은 request pair 소유권에는 참여하지만 solver node, physical location, travel, stop, service visit, zone visit 또는 zone-resource membership을 만들지 않는다. Delivery node에 정규화된 zone fact가 있다면 그대로 보존한다.
- **Real pickup-delivery:** 실제 pickup 위치·시간창·service가 있고 같은 route에서 delivery보다 먼저 수행한다.

Phase 02는 route load를 계산하지 않는다. 그러나 `ProblemInstance`가 두 service pattern을 구분하고 request가 정확히 `LogicalInitialLoad` 또는 `PhysicalService(pickupNodeId)` 하나와 delivery node reference를 갖도록 봉인해야 Phase 03이 initial load와 load delta를 올바르게 계산할 수 있다. Prefix node 대 explicit initial-load state라는 구체 내부 표현은 `P-02 PROPOSED/OPEN`이며 Phase 01·02·03 owner와 Architecture owner의 승인 전 public API로 고정하지 않는다.

Zone은 pickup 표현을 정하는 수단이 아니다. Logical pickup 자체에서는 zone fact를 읽거나 합성하지 않고 방문 zone set/resource membership에 원소를 추가하지 않는다. 반대로 delivery node와 real physical pickup node가 정상적으로 가진 `zoneId`와 zone-resource fact는 버리거나 `ALL`로 덮어쓰지 않는다. Phase 02는 이 fact를 immutable problem에 보존하고, 실제 route의 zone compatibility 계산은 후속 owner가 수행한다.

### 3.3 Project-specific lifecycle와 invariant

```text
External ID
  → Phase 01 validated opaque identity
  → Phase 02 type-specific dense ID
  → immutable array/table index
  → output에서 external ID로 round-trip
```

Phase 02가 직접 보장할 invariant는 다음과 같다.

1. External↔dense mapping은 hole 없는 `0..N-1`이고 양방향 round-trip이 된다.
2. Request, vehicle, solver node, physical location ID는 서로 바꿔 쓸 수 없다.
3. Request는 정확히 하나의 pickup semantics와 하나의 delivery node reference를 가지며 wrong-kind/dangling reference가 없다.
4. Real pickup과 모든 delivery/terminal physical node만 정확히 하나의 location mapping을 갖는다. Logical initial load는 physical node/location collection에 없다.
5. Logical initial load 자체는 zone visit 또는 zone-resource membership을 만들지 않는다. Delivery node와 real physical pickup의 정상 zone fact는 그대로 보존한다.
6. Physical location이 `M`개면 distance lookup은 정확히 `M²`개다. Delivery-only logical pickup의 수는 `M`, `M²`, travel generation call 수를 늘리지 않는다.
7. 사용 vehicle이 `V`개면 모든 `(vehicle, from, to)` time lookup이 total이다.
8. `A→B`와 `B→A`는 독립 cell이다.
9. Prepared artifact 생성 후 mutable alias가 없다.
10. `ProblemInstance`가 bind한 travel fingerprint와 전달된 `PreparedTravel` fingerprint가 같다.
11. 실패·취소·overflow·corruption에서는 partial prepared artifact나 partial problem을 발행하지 않는다.

Route-bank XOR, same-vehicle, precedence 같은 stable solution invariant는 Phase 05에서 실제 route state로 구현한다. Phase 02는 그 invariant를 가능하게 하는 request/node identity와 reference graph를 보존한다. 아직 route가 없는데 Phase 02에서 `ASSIGNED/UNASSIGNED`를 만들면 책임 침범이다.

### 3.4 Travel 용어

| 용어 | 정확한 의미 |
|---|---|
| `D` | Provided authoritative directed integer meter |
| `U` | Provided authoritative vehicle-independent directed integer second |
| `C` | Non-authoritative legacy field. Feasibility, generation, score의 입력이 아님 |
| Self arc | Raw 값과 무관하게 `D=0`, `U=0`; override provenance는 보존 |
| Missing `D` | 승인된 Great Circle function으로 계산하고 최종 meter에서 `HALF_UP` 한 번 |
| Missing `U` | Resolved `D`와 concrete vehicle speed로 `CEILING(D × 3.6 / speed)` |
| Missing speed | 정확히 `45 km/h` 사용 가능 |
| Present-invalid speed | Input failure. Missing으로 낮춰 45를 쓰면 안 됨 |
| Non-self zero | 서로 다른 logical location ID가 co-located일 수 있으므로 상위 source contract상 valid한 provided `D=0`/`U=0`은 그대로 보존. “반드시 양수” 규칙을 만들지 않음 |
| Complete directed coverage | 모든 physical-location pair와 모든 used-vehicle time lookup이 solve 전 해소됨 |
| Semantic fingerprint | Value뿐 아니라 mapping, source kind, policy/version, unit/rounding/coverage identity까지 결합 |

Non-self zero와 negative를 같은 것으로 취급하지 않는다. Valid provided zero는 generation 대상이 아니며, negative·overflow·invalid token만 typed rejection이다.

## 4. 시작 전 읽기 순서와 entry gate

### 4.1 읽기 순서

구현자는 다음 순서를 건너뛰지 않는다.

1. [사람용 가이드 README](../README.md)의 authority, 15 Phase 수량, 문서/구현 상태 분리를 읽는다.
2. [사람용 진행 현황](../execution-progress-and-results.md)에서 자신의 task, review 역할과 실제 구현 `NOT_STARTED`를 확인한다.
3. [Implementation README §3](../../README.md#3-source-authority)와 [§6](../../README.md#6-phase-작업-순서)에서 최신 `26/1/1`, ALNS-first, Phase 13/14 gate를 확인한다.
4. [Canonical Master §8](../../../master-design.md#8-directed-distancetime-matrix-계약)과 [§4.5~§4.6](../../../master-design.md#45-상태와-산출물의-생명주기)를 읽는다.
5. 현재 [Domain map §1](../../../domain-design.md#1-문서-지위와-읽기-규칙)·[§8](../../../domain-design.md#8-travel-matrix-preparation)과 [Architecture map §1](../../../architecture-design.md#1-문서-목적-독자와-결정-표기)·[§19](../../../architecture-design.md#19-maven-build-order와-implementation-phases)에서 최신 의미/module/gate 지도를 확인한다.
6. 사용자 고정 detailed baseline인 [2026-07-26 Domain §2](../../../2026-07-26-domain-design.md#2-cvrptw-개발자를-위한-rpdptw-입문), [§6](../../../2026-07-26-domain-design.md#6-travel-preparation), [§7](../../../2026-07-26-domain-design.md#7-immutable-solver-model)과 [2026-07-26 Architecture §2](../../../2026-07-26-architecture-design.md#2-module과-package-경계)를 읽는다.
7. [Integrated Design Phase 2](../../../architecture-domain-implementation-design.md#6-phase-2--travel-preparation과-immutable-probleminstance)를 읽는다.
8. [Master Realization Plan Phase 02](../../master-realization-plan.md#phase-02--이동-자료-준비와-immutable-problem), [evidence bundle 규칙](../../master-realization-plan.md#9-evidence-bundle-규칙), [Phase DoD](../../master-realization-plan.md#111-phase-dod)를 읽는다.
9. [Phase 01 §6.2](../../phases/phase-01-canonical-input-normalization.md#62-산출물)와 [§11.3](../../phases/phase-01-canonical-input-normalization.md#113-phase-02-handoff)에서 실제 producer 계약을 읽는다.
10. [Phase 02 원본](../../phases/phase-02-prepared-travel-immutable-problem.md)과 [Phase 02 review findings](../../reviews/phase-02-review.md#3-findings)을 함께 읽는다.
11. [Phase 03 §4](../../phases/phase-03-route-propagation-evaluation-kernel.md#4-entry-gate와-확인-방법)과 [§14.1](../../phases/phase-03-route-propagation-evaluation-kernel.md#141-previous--actual-document-unaccepted-phase-02)에서 consumer의 실제 acceptance check를 읽는다.
12. 현재 [root POM](../../../../pom.xml), [`.sdkmanrc`](../../../../.sdkmanrc), `src/main/java`, `src/test/java` inventory를 직접 확인한다.

### 4.2 Entry gate와 확인 방법

| Gate | 필요한 evidence/승인 | 현재 조사 결과 | 구현자 행동 |
|---|---|---|---|
| Phase 00 acceptance | `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`, accepted review/receipt와 확정 module path | HEAD는 single-JAR baseline. Live tree에는 미커밋 reactor skeleton이 있으나 progress §5/§10.1은 `CHANGES_REQUIRED_FIX_01_IN_PROGRESS / NOT_ACCEPTED`이며 accepted receipt가 없음 | **중지**. Live skeleton을 삭제·재작성·accepted로 승격하지 않고 Phase 00 owner의 correction/receipt를 기다림 |
| Phase 01 acceptance | `E-P01-NUMERIC/TIME/COMPAT/ERROR`, immutable handoff와 acceptance receipt | 문서만 actual, 구현/evidence 없음 | **중지**. Raw JSON을 임시 input으로 사용하지 않음 |
| Great Circle approval | Function/policy ID·version, Earth model, coordinate contract, precision, reference vectors, approval | 구체값 없음 | Missing-`D` production green과 Phase exit를 **중지** |
| Typed source policy | Allowed source kinds, provided/generated priority, declared absence, source identity, no-fallback review | 제안만 있고 승인 record 없음 | Source enum/default를 production contract로 고정하지 않음 |
| Task/roles | Scheduler task ID, implementer, independent reviewer | Canonical Phase metadata는 `TBD`; 사람용 task만 작성 중 | 실제 구현 전 scheduler 확인 |
| Integer fixture scope | Plan-final fixture와 official snapshot authority 분리 | FLOOR fixture는 존재, official production snapshot은 아님 | Generic tests는 hand oracle, plan E2E는 FLOOR fixture, official label은 금지 |

현재 gate 확인에 사용할 read-only 명령:

```bash
git status --short --untracked-files=all
find . -type f -name pom.xml -not -path './target/*' -not -path './node_modules/*' -print | sort
find rpdptw build legacy src -type f -name '*.java' -not -path '*/target/*' -print | sort
git hash-object pom.xml docs/implementation/execution-progress-and-results.md
mvn -version
git rev-parse HEAD
```

이 inventory command의 출력, timestamp와 digest를 구현 시작 직전에 다시 봉인한다. §5.1의 timestamp/blob/digest와 하나라도 다르면 WP-02.0에서 멈추고 POM DAG, source owner, required command와 evidence 영향을 다시 review한다.

현재 root build는 미커밋 Phase 00 remediation을 실행하는 것이므로 이 가이드 correction에서 실행하지 않는다. Phase 00 owner가 허용한 characterization을 나중에 실행하더라도 ignored/stale `target/`, 로컬 Maven repository 또는 root의 `failIfNoTests=false` 때문에 생긴 green은 Phase 00/01/02 acceptance evidence가 아니다.

### 4.3 마지막 안전 지점

Entry gate가 닫혀 있는 지금의 마지막 안전 지점은 다음뿐이다.

- 권위 문서와 inventory를 읽는다.
- Proposed type/package와 test 이름을 review한다.
- Test-only hand oracle과 corruption fixture를 설계한다.
- Great Circle/source policy 승인 질문을 owner에게 전달한다.
- Production Java, POM, provider adapter, public schema와 registry status는 바꾸지 않는다.

승인 전 “일단 임의 Earth radius로 구현하고 나중에 바꾸자”는 진행이 아니다. 그 선택은 generated distance와 모든 downstream fingerprint를 바꾸므로 Phase exit와 재현성을 무효화한다.

## 5. 현재 상태와 목표 상태

### 5.1 2026-07-29 inventory — HEAD baseline과 live drift

이 절은 두 시점을 분리한다. HEAD는 재현 가능한 committed baseline이고 live snapshot은 같은 checkout의 **미커밋·미승인 관찰값**이다. 어느 쪽도 Phase 00/01/02 acceptance를 대신하지 않는다.

#### 5.1.1 HEAD baseline

| 항목 | HEAD `7cc890ee1d0805df5ae14b633127fade4f978639` 사실 | 해석 |
|---|---|---|
| Commit | `2026-07-29T00:19:42+09:00`, subject `deprecated` | 조사 baseline일 뿐 implementation receipt가 아님 |
| Build | Root `pom.xml` 1개인 single-JAR project | 당시 parent/child reactor 없음 |
| Main/Test Java | Root `src/main/java` 6개, `src/test/java` 1개 | Synthetic placeholder characterization |
| Target Phase types/tests | `PreparedTravel`, `ProblemInstance`, Phase 02 production/test 0개 | Phase 02 `NOT_STARTED / NOT_ACCEPTED` |

#### 5.1.2 Live uncommitted snapshot

Snapshot timestamp는 `2026-07-29T02:03:39+09:00` (`Asia/Seoul`)이다. Root POM blob은 `1dc675ba17b7f2202f34a22131f152cc2868b075`, progress blob은 `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d`, core POM blob은 `827e3cd913c16da3cf0011b964f803b33d5db8ef`, test-fixtures POM blob은 `e033f9cda6de908a25431b6dfce5b0b0d8cd3ac1`이다. POM/source/status inventory manifest SHA-256은 `c3ad0ffb4e85a30d99e1606411a36e0d4ebd16582e827437d08c6a591098fdc9`다.

| 분류 | Live에서 관찰한 사실 | acceptance와 수정 규칙 |
|---|---|---|
| Reactor | Root `packaging=pom`; `rpdptw`, `build`, `legacy` aggregator와 합계 13개 POM | 미커밋 Phase 00 artifact. 이 Phase가 이름/edge를 재작성하지 않음 |
| Stable modules | `rpdptw/core`, `solver`, `verification`, `application`, `capabilities`, `profile-catalog` skeleton | 경로는 존재하지만 Phase 00 accepted contract는 아님 |
| Target main Java | RPDPTW namespace main Java 23개, 모두 `package-info.java` | Package skeleton만 존재. Phase 01/02 production type은 0개 |
| Build tests | `build/architecture-rules`와 `build/test-fixtures` test Java 합계 11개 | Phase 00 remediation test이며 Phase 02 required test가 아님 |
| Fixture DAG | `build/test-fixtures`가 `rpdptw-core`를 test-scope로 소비하고 test-jar를 생산; `architecture-rules`가 그 test-jar와 stable modules를 소비 | Core가 test-fixtures를 역으로 의존하면 cycle. 현재 edge를 임의로 양방향화하지 않음 |
| Legacy | `legacy` 아래 Java 15개; 기존 root `src/**` 6/1은 삭제 상태 | 사용자 소유 migration drift. Phase 02 rollback/delete 대상이 아님 |
| Surefire | Parent POM의 module-level `failIfNoTests=false`; ignored `target/` report가 존재할 수 있음 | Root green/0 tests/stale report를 Phase 02 green으로 판정 금지 |
| Progress | Phase 00 `CHANGES_REQUIRED_FIX_01_IN_PROGRESS / NOT_ACCEPTED`; Phase 02 `BLOCKED_BY_ENTRY_GATES / NOT_ACCEPTED` | Skeleton 존재와 acceptance를 분리 |
| Target types/evidence | `PreparedTravel`, `ProblemInstance`, dense ID, Phase 02 production/test/evidence/receipt 0개 | Phase 02 구현 완료 주장 금지 |
| Plan-final data | `win_poc_case_floor.json`: 452 orders, 31 vehicles, 205,209 matrix cells | Local final execution fixture. Official production snapshot은 아님 |

Live snapshot은 correction 뒤에도 바뀔 수 있다. 구현 시작 직전에 §4.2 명령으로 새 timestamp/blob/inventory digest를 만들고, Phase 00 accepted receipt가 참조하는 source snapshot과 exact match하지 않으면 중지한다. 기존 미커밋 artifact를 정리·rollback·accept할 권한은 이 가이드에 없다.

조사 시 확인한 fixture lineage:

| File | SHA-256 | 사용 |
|---|---|---|
| `data/win_poc_case.json` | `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7` | 변경하지 않는 원본 provenance와 decimal negative fixture |
| `scripts/floor_win_poc_matrix.py` | `0423e0cef4d93ed51525a8f237b48f0c680c7d6e134af1ea70ba3a48b5ed77d0` | 사용자 승인 migration |
| `data/win_poc_case_floor.json` | `c246abd375211877c4ec3467998651deb13fbd01768d2d1d84b8d234f5f56873` | Plan-final local execution input |

### 5.2 목표 inventory

아래는 Phase 00이 경로를 승인했을 때의 **proposed internal tree**다. 경로와 type 이름은 바뀔 수 있지만 책임과 dependency 방향은 유지한다.

```text
rpdptw/core/
├── src/main/java/com/ronext/rpdptw/domain/
│   ├── RequestId.java
│   ├── VehicleId.java
│   ├── SolverNodeId.java
│   ├── PhysicalLocationId.java
│   ├── SolverPickupSemantics.java
│   ├── DenseIdBijection.java
│   ├── ProblemInstance.java
│   └── ProblemFingerprint.java
├── src/main/java/com/ronext/rpdptw/travel/
│   ├── PreparedTravel.java
│   ├── PreparedTravelFingerprint.java
│   ├── TravelPreparer.java
│   ├── TravelPreparationInput.java
│   ├── TravelGenerationPolicy.java
│   ├── GreatCircleDistanceFunction.java
│   ├── TravelSourceKind.java
│   ├── TravelCellProvenance.java
│   └── TravelPreparationFailure.java
└── src/test/java/com/ronext/rpdptw/{domain,travel}/...
    └── ... core-local fixture builders / hand oracles / corruption helpers

build/test-fixtures/               # 현재 live edge: test-fixtures --test→ core
└── ... downstream/cross-module consumer fixtures only

build/architecture-rules/
└── ... Phase02ArchitectureTest    # core + fixture test-jar를 downstream에서 검사
```

Core unit test helper는 `rpdptw/core/src/test`에 두어 production artifact에 포함하지 않는다. 현재 `build/test-fixtures`는 core를 소비하는 downstream test-jar이므로 core test가 이를 다시 의존해서는 안 된다. Cross-module fixture는 solver/verifier/architecture 같은 downstream consumer만 사용한다. 별도 pure fixture module로 edge를 뒤집거나 분리하려면 Phase 00/Architecture review와 cycle-free DAG evidence가 먼저 필요하며, 이 가이드가 현재 POM에 양방향 dependency를 추가하도록 승인하지 않는다.

목표 완료 상태:

- Phase 01 accepted artifact 외의 source authority가 없다.
- 모든 directed distance와 used-vehicle time lookup이 total이다.
- Artifact가 immutable하고 canonical bytes/fingerprint를 갖는다.
- `ProblemInstance`와 `PreparedTravel`의 exact binding이 검증된다.
- Core에 provider/cloud/HTTP/OR-Tools dependency가 없다.
- Fault/corruption/reproducibility/security/structural-performance test가 실제 결함을 잡는다.
- 세 evidence key와 독립 review/acceptance receipt가 봉인된다.

## 6. Scope, non-scope와 결정 상태

### 6.1 이 Phase의 scope

- Phase 01 handoff identity·reference·source declaration 검증
- Type-specific dense ID와 external↔dense bijection
- Directed provided integer `D/U` 수용
- Self `0/0`
- 승인 policy에 따른 missing `D/U` generation
- Complete `M²` distance와 `V×M²` resolved time coverage
- Checked allocation/index/arithmetic
- Canonical ordering, fingerprint, digest와 provenance
- Immutable `PreparedTravel`과 `ProblemInstance`
- Whole-artifact cache의 integrity contract
- Safe aggregate report와 redacted failure evidence
- Phase 03/07가 사용할 read-only authority contract

### 6.2 Non-scope

- Route propagation, load/time/window/rest/stop/resource 계산
- Hard constraint, metric, score, objective와 profile binding
- Pair insertion, route/bank, portfolio와 ALNS
- Candidate/result verifier 구현과 final outcome
- Application/provider port, network/file/object storage acquisition
- AWS/GCP SDK, credential, retry와 cloud distribution
- Dynamic/time-dependent traffic, geocoding와 live refresh
- Multi-trip/rotation, optional variants
- Phase 13 route pool/MIP
- Phase 14 official calibration, official manifest와 production cutover
- Public Java/wire API 확정

### 6.3 확정, proposed/open, gated/deferred

| 항목 | 상태 | 구현 규칙 |
|---|---|---|
| Physical-location directed key | 확정 | Node ID를 matrix key로 쓰지 않음 |
| Provided `D/U` integer meter/second | 확정 | Decimal coercion 금지 |
| `C` non-authoritative | 확정 | Generation/feasibility/score에서 사용 금지 |
| Self `0/0` | 확정 | Raw diagonal이 달랐던 사실은 provenance에 남김 |
| Missing `D` Great Circle + `HALF_UP` | 의미 확정, exact function은 **OPEN/BLOCKER** | 승인 function/version/reference vector 전 production green 금지 |
| Missing `U` vehicle-specific `CEILING` | 확정 | Provided common `U`를 덮어쓰지 않음 |
| Missing speed `45 km/h` | 확정 | Missing에만 적용; invalid-present는 failure |
| Delivery-only logical pickup의 비물리·비-zone-visit 의미 | 확정; prefix node 대 explicit initial-load 표현은 `P-02 PROPOSED/OPEN` | Pair ownership은 유지하되 physical node/location/travel/stop/service visit/zone visit/zone-resource membership을 만들지 않음. Delivery와 real physical pickup의 정상 zone fact는 보존 |
| Weight/volume unit·scale·rounding identity | 확정 | Phase 01 unit-bearing value를 재사용하고 Phase 02 raw `long`/재정규화 금지 |
| Dense type/package/class 이름 | `PROPOSED INTERNAL` | Phase 00/architecture review가 이름을 바꿀 수 있음 |
| Prepared table 내부 배열/압축 | `P-03 OPEN` | Total lookup/source distinction/no-alias를 약화하지 않는 범위에서 선택 |
| Fingerprint algorithm/encoding | `PROPOSED/OPEN` | Versioned ADR와 migration rule 전 public contract 금지 |
| Official travel snapshot | External authority gate | Generic Phase 02 acceptance의 필수 조건은 아니나 official 사용에는 필수 |
| `Q-BENCH-02` numeric values | `OPEN — EXPERIMENT_REQUIRED` | Phase 14를 막지만 Phase 02 semantics를 막지 않음 |
| Phase 13 | `C-17 GATED TARGET` | 유효한 Phase 14A receipt와 별도 승인 전 dependency/API/default 추가 금지 |
| Phase 14B production | Authority gate | Phase 02 artifact가 있어도 production 권한이 생기지 않음 |
| `Q-VAR-01` | `DEFERRED` | 질문·구현·활성화하지 않음 |
| Multi-trip/rotation | Deferred feature | Current oneway/single-roundtrip 의미만 보존 |

### 6.4 사람 승인 항목

다음은 구현자가 코드로 결정할 항목이 아니다.

1. Phase 00 module/package/DAG와 architecture rule.
2. Phase 01 exact handoff schema/identity와 acceptance receipt.
3. Delivery-only의 prefix token 대 explicit initial-load 내부 표현과 exact Java visibility. 비물리·비-zone-visit 의미와 physical delivery/real-pickup zone fact 보존은 승인 대기 항목이 아님.
4. Great Circle function ID/version, Earth model·constant, coordinate range/order, intermediate precision, anti-meridian/pole behavior와 reference vectors.
5. Typed travel source allowlist, snapshot identity, declared `Absent`, duplicate/conflict와 no-fallback policy.
6. Canonical fingerprint algorithm/encoding/version과 schema migration.
7. Core-local 대 cross-module test fixture owner와 cycle-free test dependency edge.
8. 공식 fixture/snapshot의 scope와 label.
9. Public API/wire schema.
10. Wall-time, maximum `M/V`, memory threshold와 production sizing.

## 7. 학습 경로

### 7.1 단계 A — 개념 이해

읽을 것:

- `Request ≠ SolverNode ≠ PhysicalLocation`
- Provided/common time과 generated/vehicle-resolved time
- `Absent ≠ Invalid ≠ UpstreamUnavailable`
- Artifact fingerprint와 provider locator의 차이

작은 탐색:

```text
Location A, B
Vehicle V1, V2

Provided:
  D(A,B)=100
  U(A,B)=8
Missing:
  D(B,A)
  U(B,A)
```

종이에 다음을 적는다.

- Distance cell은 4개다.
- Time lookup은 vehicle마다 4개지만 provided common `U`는 두 vehicle에 같은 authority로 resolve된다.
- `B→A`는 `A→B`를 복사하지 않는다.
- 같은 location을 가리키는 pickup/delivery node가 여러 개여도 travel cell 수는 늘지 않는다.

완료 신호:

- “왜 node matrix가 아닌 location matrix인가?”를 코드 없이 설명할 수 있다.
- Provided 값, generated 값, self-normalized 값의 provenance가 다른 이유를 설명할 수 있다.

자문 질문:

- 이 값은 물리 fact인가, source authority인가, 단순 storage metadata인가?
- 같은 숫자라도 source policy가 다르면 fingerprint가 같아야 하는가?

### 7.2 단계 B — 작은 실습과 독립 oracle

Production preparer를 만들기 전에 test-only fixture로 다음을 손 계산한다.

1. `M=2`, `V=2` Cartesian coverage.
2. Asymmetric `D(A,B) != D(B,A)`.
3. Generated time `ceil(D × 3.6 / speed)`.
4. Missing speed와 invalid-present speed의 분기.
5. Non-zero raw diagonal을 `0/0`으로 바꾸고 override count 확인.
6. Missing `D`인데 coordinate가 없는 rejection과 generator/cache/reverse 호출 0 확인.
7. 서로 다른 co-located location의 valid non-self zero 보존과 negative rejection 분리.
8. Duplicate conflict, unknown reference, overflow와 mutation probe.

완료 신호:

- Expected key set과 expected value를 production code를 호출하지 않고 만들 수 있다.
- 한 줄 corruption이 어떤 typed failure가 되어야 하는지 설명할 수 있다.

자문 질문:

- Oracle이 production rounding helper나 canonical encoder를 재사용해 같은 bug를 공유하고 있지 않은가?
- Test fixture가 official snapshot처럼 보이는 이름이나 provenance를 갖고 있지 않은가?

### 7.3 단계 C — 실제 변경

순서는 identity/value/failure → source handoff → preparation → canonical immutable artifact → problem freeze다. 각 단계에서 red test를 먼저 만들고, public surface를 최소화하며, mutable draft는 method-local로 제한한다.

완료 신호:

- 각 work package가 독립적으로 green이고 다음 package가 소비할 immutable contract가 있다.
- 실패 시 마지막 accepted artifact가 변하지 않는다.
- Type 이름을 바꿔도 invariant와 test oracle이 남는다.

자문 질문:

- 이 변경이 Phase 03 계산이나 Phase 08 provider port를 몰래 당겨오고 있지 않은가?
- `record`를 썼다는 사실만으로 내부 array/map alias를 놓치고 있지 않은가?

### 7.4 단계 D — 통합과 acceptance

Phase 03 consumer equality test, architecture rule, full core/root verify, evidence DAG와 독립 review를 수행한다.

완료 신호:

- `ProblemInstance.preparedTravelFingerprint == PreparedTravel.fingerprint`.
- Target test의 fresh report에 required method가 모두 있고 failure/error/skipped가 0이다.
- Pre-review manifest → independent review report → acceptance receipt의 단방향 digest chain이 있다.
- Scheduler가 실제 registry를 `ACCEPTED`로 전이했다.

자문 질문:

- Root build가 green인 이유가 target test 0건이기 때문은 아닌가?
- Review verdict나 receipt를 pre-review manifest에 나중에 backfill하지 않았는가?

## 8. Java 설계 안내

이 절의 이름과 signature는 **skeletal proposed contract**다. 그대로 복사해 public API로 확정하지 않는다.

### 8.1 Package와 dependency direction

```text
com.ronext.rpdptw.domain
  ID/value/reference/ProblemInstance
        ↑
com.ronext.rpdptw.travel
  source policy/value → pure TravelPreparer → PreparedTravel
        ↑
Phase 03 propagation and Phase 07 verification read-only consumers
```

금지 dependency:

```text
rpdptw-core -X→ application / adapter / cloud SDK / HTTP / storage locator
rpdptw-core -X→ solver / verification implementation / OR-Tools
travel      -X→ provider response / clock / filesystem / network
Phase 03/07 -X→ coordinate/speed generation or preparation cache
```

`domain`과 `travel`의 정확한 compile 방향은 Phase 00 package rule로 확정한다. Cycle을 피하려면 immutable ID/value contract를 domain 쪽에 두고 travel이 소비하며, `ProblemInstanceFactory`가 두 artifact의 identity를 조립하는 방향이 자연스럽다.

### 8.2 Type-specific ID 후보

```java
record RequestId(int value) {}
record VehicleId(int value) {}
record SolverNodeId(int value) {}
record PhysicalLocationId(int value) {}
```

각 constructor는 `value >= 0`을 검사한다. 하나의 generic `DenseId`만 사용하면 method parameter를 잘못 바꾸어도 compile될 수 있으므로 public/internal boundary에는 type-specific ID가 더 안전하다.

외부 ID mapping 후보:

```java
interface DenseIdBijection<E, D> {
    int size();
    D denseIdOf(E externalId);
    E externalIdOf(D denseId);
}
```

선택 근거:

- Array index와 external opaque ID를 분리한다.
- Result 변환에 필요한 reverse mapping을 보존한다.
- Stable canonical order를 한 owner가 관리한다.

주의:

- External ID를 trim, case-fold 또는 locale sort하지 않는다.
- Duplicate를 `Map.put`으로 덮어쓴 뒤 mapping을 만들지 않는다.
- `M*M`, `V*M*M` allocation/index는 checked arithmetic을 사용한다.

### 8.3 Presence와 source 후보

```java
sealed interface TravelValueDeclaration
        permits AbsentTravelValue, PresentIntegerTravelValue {}

record AbsentTravelValue(DeclaredAbsenceReason reason)
        implements TravelValueDeclaration {}

record PresentIntegerTravelValue(
        long value,
        SourceAuthorityId authority,
        SourcePolicyVersion policyVersion
) implements TravelValueDeclaration {}
```

`Invalid`는 정상 domain value variant가 아니라 Phase 01/02 validation failure다. Upstream unavailable/partial/corrupt도 `AbsentTravelValue`로 바꾸지 않는다.

Source kind 후보:

```java
enum TravelSourceKind {
    PROVIDED_CANONICAL_INPUT,
    OFFICIAL_TRAVEL_SNAPSHOT,
    GENERATED_GREAT_CIRCLE,
    GENERATED_VEHICLE_TIME,
    SELF_NORMALIZED
}
```

`TEST_ONLY_HAND_ORACLE`나 `PLAN_EXECUTION_FLOOR_FIXTURE`는 test/evidence envelope label이지 production core가 분기할 source enum으로 넣지 않는 편이 안전하다. Official snapshot은 Phase 01 artifact가 exact authority와 digest를 보존해 넘긴 경우에만 나타난다.

### 8.4 Value와 preparation result 후보

```java
record DistanceMeters(long value) {}
record TravelTimeSeconds(long value) {}
record VehicleSpeedKilometersPerHour(BigDecimal exactValue) {}

interface GreatCircleDistanceFunction {
    GreatCircleFunctionId id();
    GreatCircleFunctionVersion version();
    BigDecimal unroundedMeters(Coordinate from, Coordinate to);
}

sealed interface TravelPreparationResult
        permits TravelPreparationSucceeded, TravelPreparationRejected {}

record TravelPreparationSucceeded(
        PreparedTravel preparedTravel,
        TravelPreparationReport report
) implements TravelPreparationResult {}

record TravelPreparationRejected(
        TravelPreparationFailure failure,
        SafeFailureEvidence evidence
) implements TravelPreparationResult {}
```

`GreatCircleDistanceFunction` 구현을 주입 가능하게 하는 이유는 임의 library default를 숨기기 위해서가 아니라, 승인 function identity와 reference vector를 contract로 확인하기 위해서다.

Failure hierarchy 후보:

```java
sealed interface TravelPreparationFailure
        permits SourceFailure, ValueFailure, CoverageFailure,
                IdentityFailure, ArithmeticFailure, IntegrityFailure {}
```

정상 input rejection과 implementation defect를 code/category로 구분한다. Exception message 전체를 evidence나 외부 diagnostic으로 쓰지 않는다.

### 8.5 `PreparedTravel` 후보

```java
interface PreparedTravel {
    int physicalLocationCount();
    int vehicleCount();

    DistanceMeters distance(
        PhysicalLocationId from,
        PhysicalLocationId to
    );

    TravelTimeSeconds travelTime(
        VehicleId vehicle,
        PhysicalLocationId from,
        PhysicalLocationId to
    );

    TravelCellProvenance distanceProvenance(
        PhysicalLocationId from,
        PhysicalLocationId to
    );

    TravelCellProvenance timeProvenance(
        VehicleId vehicle,
        PhysicalLocationId from,
        PhysicalLocationId to
    );

    PreparedTravelFingerprint fingerprint();
}
```

Internal representation은 다음 중 하나일 수 있으나 아직 `P-03 OPEN`이다.

- `M²` distance array + common provided time array + generated per-vehicle overlay
- Fully resolved `V×M²` time array
- Immutable compressed representation

어떤 선택도 다음을 바꾸면 안 된다.

- 모든 lookup은 total이다.
- Common provided `U`와 generated vehicle-specific `U` provenance가 구분된다.
- Backing array/map을 노출하지 않는다.
- Iteration/storage layout 차이가 canonical fingerprint를 바꾸지 않는다.

### 8.6 `ProblemInstance` 후보

```java
sealed interface SolverPickupSemantics
        permits LogicalInitialLoad, PhysicalService {}

record LogicalInitialLoad() implements SolverPickupSemantics {}

record PhysicalService(
    SolverNodeId pickupNodeId
) implements SolverPickupSemantics {}

record SolverRequest(
    RequestId id,
    SolverPickupSemantics pickup,
    SolverNodeId deliveryNodeId,
    ServicePattern servicePattern,
    MilliKilograms demandWeight,
    MilliCubicMeters demandVolume,
    ImmutableVehicleEligibility eligibility
) {}

interface ProblemInstance {
    ProblemFingerprint fingerprint();
    PreparedTravelFingerprint preparedTravelFingerprint();
    DenseIdBijections mappings();
    ImmutableRequests requests();
    ImmutableVehicles vehicles();
    ImmutableSolverNodes nodes();
    ImmutablePhysicalLocations locations();
}

interface ProblemInstanceFactory {
    ProblemFreezeResult freeze(
        NormalizedInputArtifact normalizedInput,
        PreparedTravel preparedTravel
    );
}
```

이 snippet은 실제 Java 문법에 맞는 **package-private proposed 후보**지만 최종 이름/API가 아니다. `LogicalInitialLoad`는 `SolverNodeId`, `PhysicalLocationId` 또는 pickup-side zone resource를 갖지 않는다. `PhysicalService`만 pickup node를 가지며 그 node가 physical location과 정규화된 zone fact에 mapping될 수 있다. 모든 request의 delivery node가 가진 정상 zone fact도 보존한다. `servicePattern`과 `pickup`의 조합은 서로 일치해야 하고, invalid 조합은 freeze 전에 거부한다.

`MilliKilograms`와 `MilliCubicMeters`는 Phase 01의 scale `3`/`FLOOR` 결과를 담는 동일 unit-bearing immutable type을 재사용한다. Phase 02는 raw kg/CBM을 다시 읽거나 scale/rounding을 다시 수행하지 않는다. Phase 00 visibility 때문에 exact type 이름을 바꿔야 한다면 의미상 동등한 reviewed type과 일대일 mapping을 사용하되 primitive `long` 두 개로 약화하지 않는다. Vehicle capacity도 같은 차원의 동일 type을 사용해 weight↔volume 교환이 compile contract에서 성립하지 않게 한다.

`ProblemInstance`가 `PreparedTravel` object를 직접 포함할지 reference/fingerprint만 bind할지는 internal design review 대상이다. 고정 계약은 다음이다.

1. 생성 시 exact fingerprint와 location/vehicle mapping을 대조한다.
2. Phase 03 handoff에서 problem과 travel이 다른 authority면 거부한다.
3. No-compatible-vehicle request는 structural error가 아니다.
4. Logical initial load는 node/location/travel/stop/service/zone-visit/zone-resource collection에 나타나지 않고 physical pickup만 node/location/정상 zone reference를 가질 수 있다. Delivery node의 정상 zone fact도 손실 없이 보존한다.
5. Delivery-only request 수만 바꾼 대조 fixture에서 logical pickup이 추가한 zone visit과 zone-resource membership은 각각 정확히 0이며, delivery/real-pickup zone fact 집합은 expected physical fact와 정확히 같다.
6. Demand/capacity는 Phase 01의 unit-bearing type과 exact value를 보존하며 Phase 02 renormalization이 0회다.
7. Request/node/location/vehicle reference corruption은 structural failure다.
8. Constructor input과 returned view가 mutable alias를 만들지 않는다.

### 8.7 State transition과 pseudocode

```text
Phase01AcceptedArtifact
  → INPUT_VALIDATED
  → SOURCE_DECLARATIONS_VALIDATED
  → DENSE_IDS_VALIDATED
  → method-local TRAVEL_DRAFT
  → COMPLETE_TRAVEL_CANDIDATE
  → CANONICALIZED_AND_FINGERPRINTED
  → immutable PREPARED_TRAVEL
  → method-local PROBLEM_DRAFT
  → REFERENCES_AND_RANGES_VALIDATED
  → immutable PROBLEM_INSTANCE
  → HANDOFF_READY

any validation/fault/cancel
  → typed REJECTED
  → all drafts discarded
```

개념 pseudocode:

```text
prepare(normalizedInput, approvedPolicy, approvedGreatCircle):
  verify exact Phase 01 identities and source declarations
  create stable type-specific dense mappings
  reject duplicate/conflict/unknown/invalid values before generation

  checked allocate method-local distance/time draft

  for each (from, to) in stable physical-location dense order:
    if from == to:
      store 0/0 with SELF_NORMALIZED provenance
      continue

    distance =
      provided integer D
      or require both coordinates
         or reject before generator/cache/reverse lookup
      then generate with approved function + HALF_UP

    for each used vehicle in stable vehicle order:
      time =
        provided common integer U
        or generate CEILING(distance × 3.6 / valid-or-missing-default speed)

  assert exact M² distance and V×M² time totality
  canonicalize values + mapping + source/policy identities
  defensive-copy freeze PreparedTravel

  validate request pickup semantics:
    LogicalInitialLoad has no solver node/location/travel/service/zone visit
      and contributes no zone-resource membership
    PhysicalService has one valid pickup node and preserves its normalized zone facts
    every request has one valid delivery node and preserves its normalized zone facts
  validate physical node→location, vehicle references and checked ranges
  preserve Phase 01 unit-bearing demand/capacity values without renormalization
  bind PreparedTravel fingerprint
  defensive-copy freeze ProblemInstance

  return success

on any failure:
  publish no partial artifact and no cache entry
  return one typed rejection + safe aggregate evidence
```

## 9. 순서 있는 work packages

모든 package는 `red → 최소 green → refactor → full verification` 순서로 실행한다. 현재 live tree에는 module skeleton이 있지만 Phase 00 acceptance와 Phase 02 production type/test가 없다. 따라서 아래 **FUTURE AFTER ACCEPTANCE** command를 지금 Phase evidence로 실행하면 안 되며, strict targeted command는 test class 부재로 non-zero여야 한다. `failIfNoTests=false`, stale `target/` 또는 local repository artifact 때문에 생긴 exit `0`은 false-green이다.

### WP-02.0 — Entry baseline과 contract freeze

| 항목 | 지시 |
|---|---|
| 목적/이유 | 닫힌 gate와 제안 API를 코드로 굳히기 전에 source·owner·approval·module 경계를 고정한다. |
| 사전조건 | Scheduler task/roles 식별, Phase 00/01 acceptance 여부 확인, policy owner 지정 |
| 예상 변경 | Production Java 없음. ADR proposal, test matrix, evidence plan만 준비 |
| 구체 행동 | Source commit/blob/section과 timestamp/inventory digest 기록 → current POM/source/test inventory 재확인 → task와 Phase 00/01 receipt, Great Circle/source policy, fingerprint ADR, evidence plan의 actual path/digest 확인 → blocker면 중지 |
| 선택 근거 | Generated distance policy와 source authority는 모든 downstream fingerprint에 영향을 주므로 나중에 숨은 변경으로 처리할 수 없다. |
| 금지 shortcut | 임의 Earth radius/library/version, current provider behavior, fixture 값을 production default로 선택 |
| 검증 명령 | 아래 actual artifact check. 문서 non-empty/whitespace만 검사하는 command로 대체 금지 |
| 기대 결과 | 모든 path가 실제 immutable artifact이고 digest가 일치하며 receipt/approval/evidence-plan 관계가 검증됨. 현재는 missing이므로 non-zero와 `BLOCKED`가 정답 |
| 실패 해석 | Gate 미충족은 code defect가 아니라 authority blocker. `BLOCKED`를 유지 |
| Rollback | 코드 변경 없이 read-only baseline으로 복귀 |
| 다음 handoff | 승인된 module/policy/Phase 01 identities를 WP-02.1에 전달 |

완료 신호: 구현자가 “지금 production code를 시작해도 되는가?”에 evidence reference로 답할 수 있다. 현재 조사 기준 답은 “아니다”다.

```bash
# CURRENT ENTRY CHECK.
# Scheduler/evidence owner가 각 *_PATH와 64-hex *_SHA256을 실제 값으로 제공해야 한다.
# 하나라도 unset/missing/mismatch이면 즉시 non-zero다.
set -eu

verify_p02_entry_file() {
  label=$1
  path=$2
  expected=$3
  test -n "$path"
  test -n "$expected"
  test -f "$path"
  actual=$(shasum -a 256 "$path" | awk '{print $1}')
  test "$actual" = "$expected"
  printf '%s %s %s\n' "$label" "$actual" "$path"
}

verify_p02_entry_file TASK "$P02_TASK_RECORD_PATH" "$P02_TASK_RECORD_SHA256"
verify_p02_entry_file P00_RECEIPT "$P02_PHASE00_RECEIPT_PATH" "$P02_PHASE00_RECEIPT_SHA256"
verify_p02_entry_file P01_RECEIPT "$P02_PHASE01_RECEIPT_PATH" "$P02_PHASE01_RECEIPT_SHA256"
verify_p02_entry_file GREAT_CIRCLE "$P02_GREAT_CIRCLE_APPROVAL_PATH" "$P02_GREAT_CIRCLE_APPROVAL_SHA256"
verify_p02_entry_file SOURCE_POLICY "$P02_SOURCE_POLICY_APPROVAL_PATH" "$P02_SOURCE_POLICY_APPROVAL_SHA256"
verify_p02_entry_file FINGERPRINT_ADR "$P02_FINGERPRINT_ADR_PATH" "$P02_FINGERPRINT_ADR_SHA256"
verify_p02_entry_file EVIDENCE_PLAN "$P02_EVIDENCE_PLAN_PATH" "$P02_EVIDENCE_PLAN_SHA256"
```

Path나 digest를 이 문서가 발명하지 않는다. Scheduler/evidence plan이 정한 actual artifact를 입력받고, accepted Phase 00 evidence verifier로 receipt가 source manifest와 independent review를 참조하는지, Phase 01 receipt가 exact `NormalizedInputArtifact` identity를 참조하는지까지 확인한다. 환경변수나 file이 없는 현재 상태에서 이 block을 성공시키기 위해 dummy file을 만들면 gate 우회다.

### WP-02.1 — Identity, value, failure와 red test

| 항목 | 지시 |
|---|---|
| 목적/이유 | Node/location/request/vehicle 혼용과 unchecked index를 compile/test 단계에서 막는다. |
| 사전조건 | Phase 00 accepted module path, WP-02.0 통과 |
| 예상 file/package/type | `domain/*Id`, `DenseIdBijection`, travel value/source/provenance/failure; `DenseIdBijection*Test`, immutability red test |
| 구체 행동 | Type-specific ID → canonical mapping → checked size/index → meter/second/speed value → sealed failure → mutation probes 순으로 test와 최소 구현 작성 |
| 선택 근거 | Preparation table보다 먼저 identity와 failure가 있어야 table이 잘못된 key나 sentinel을 숨기지 않는다. |
| 금지 shortcut | Generic int/string ID, `Map.put` duplicate overwrite, negative/sentinel 허용, record 내부 mutable array 노출 |
| 검증 명령 | 아래 future command. Test class가 없거나 0건이면 실패해야 함 |
| 기대 결과 | Duplicate/hole/out-of-range/alias가 모두 red에서 green으로 전환되고 shuffled equivalent input이 같은 canonical mapping을 만듦 |
| 실패 해석 | Mapping/alias failure는 downstream type 공개 금지. Travel 구현으로 우회하지 않음 |
| Rollback | Mutable builder/generated test output 폐기, WP-02.1 전 source state로 복귀 |
| 다음 handoff | Validated IDs/value/failure contract를 WP-02.2에 전달 |

```bash
# FUTURE AFTER Phase 00/01 ACCEPTANCE; accepted wrapper와 reactor에서만
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am clean test \
  -Dtest=DenseIdBijectionTest,DenseIdBijectionPropertyTest,PreparedTravelImmutabilityTest \
  -DfailIfNoTests=true \
  -Dsurefire.failIfNoSpecifiedTests=true
```

### WP-02.2 — Phase 01 handoff와 source policy

| 항목 | 지시 |
|---|---|
| 목적/이유 | Source authority를 Phase 01 artifact 하나로 제한하고 intentional absence와 upstream failure를 분리한다. |
| 사전조건 | Phase 01 accepted artifact/schema/receipt, approved source kinds와 declared sparse policy |
| 예상 file/package/type | `TravelPreparationInput`, source declarations/provenance validation, `TravelSourceHandoffTest`, redaction test |
| 구체 행동 | Handoff fingerprint 대조 → declared keys/reference 검증 → `PresentInteger/Absent` 분리 → duplicate/conflict/unknown rejection → detached provider/source path가 없는 API 확인 |
| 선택 근거 | Phase 02가 provider port나 detached batch를 받으면 Phase 01 artifact와 이중 authority가 된다. |
| 금지 shortcut | Provider failure를 `Absent`로 변환, fixture/cache/reverse/generator fallback, raw SDK DTO를 core에 전달 |
| 검증 명령 | 아래 future command |
| 기대 결과 | Accepted Phase 01 artifact 안의 sparse declaration만 읽고 invalid source identity는 generation 전에 typed reject |
| 실패 해석 | Corrected Phase 01 artifact가 필요한 handoff failure. Phase 02가 값을 고치지 않음 |
| Rollback | Draft 생성 전 거부. Last safe point는 accepted Phase 01 artifact |
| 다음 handoff | Canonical validated preparation input을 WP-02.3에 전달 |

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am clean test \
  -Dtest=TravelSourceHandoffTest,TravelProvenanceSecurityTest \
  -DfailIfNoTests=true \
  -Dsurefire.failIfNoSpecifiedTests=true
```

### WP-02.3 — Complete directed travel preparation

| 항목 | 지시 |
|---|---|
| 목적/이유 | Solve 전 모든 directed distance와 used-vehicle time을 해소한다. |
| 사전조건 | WP-02.1/2 green. Missing-coordinate/valid-zero/negative rejection contract는 test-only stub으로 먼저 검증 가능하지만 generated-distance numeric success와 Phase exit에는 Great Circle function/version/reference vectors 승인 필요 |
| 예상 file/package/type | `TravelPreparer`, `TravelGenerationPolicy`, approved function implementation/adapter, unit/property/fault/complexity tests |
| 구체 행동 | Self 처리 → provided `D/U` priority → missing `D` generation → missing `U` generation → coverage assertion → fault/cancel discard 순으로 구현 |
| 선택 근거 | Self/provided priority를 먼저 적용해야 generator가 권위 값을 덮어쓰지 않는다. |
| 금지 shortcut | Reverse copy, symmetric average, decimal cast/round, invalid speed→45, partial result 반환, search-time lookup |
| 검증 명령 | 아래 future command |
| 기대 결과 | `M²`, `V×M²`, asymmetry, missing-coordinate rejection, non-self zero 보존, negative rejection, exact rounding/default, call-count와 no-partial-commit 모두 green |
| 실패 해석 | Arithmetic/coverage/reference/fault는 하나의 typed rejection. “infeasible route”가 아님 |
| Rollback | Method-local drafts와 미완성 cache write 전부 폐기 |
| 다음 handoff | Complete candidate를 WP-02.4 canonical freeze에 전달 |

```bash
# Great Circle approval 전에도 닫을 수 있는 boundary subset.
# Phase 00/01 entry가 닫혀 있는 현재에는 실행하지 않는다.
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am clean test \
  -Dtest='TravelPreparerTest#rejectsMissingDistanceWhenCoordinateIsAbsent+preservesValidNonSelfZeroDistanceAndTimeForCoLocatedLocations+rejectsNegativeProvidedDistanceOrTime' \
  -DfailIfNoTests=true \
  -Dsurefire.failIfNoSpecifiedTests=true

# FUTURE AFTER Great Circle approval: full WP-02.3.
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am clean test \
  -Dtest=TravelPreparerTest,TravelPreparerPropertyTest,TravelPreparationFaultInjectionTest,TravelPreparationComplexityTest \
  -DfailIfNoTests=true \
  -Dsurefire.failIfNoSpecifiedTests=true
```

실패를 잡는 최소 sensitivity mutation:

- `HALF_UP`을 `FLOOR`로 바꾸면 boundary test가 실패해야 한다.
- `CEILING`을 truncation으로 바꾸면 generated-time test가 실패해야 한다.
- `A→B`를 `B→A`로 복사하면 asymmetry test가 실패해야 한다.
- Invalid speed를 missing으로 바꾸면 rejection test가 실패해야 한다.
- Missing coordinate에서 generator/cache/reverse lookup이 한 번이라도 호출되거나 partial artifact가 발행되면 실패해야 한다.
- Valid non-self zero를 positive-only validation으로 거부하거나 generation으로 덮으면 실패해야 한다.
- Fail-after-N을 주입하면 artifact/cache publication count가 0이어야 한다.

### WP-02.4 — Canonical `PreparedTravel`, provenance, report와 cache integrity

| 항목 | 지시 |
|---|---|
| 목적/이유 | Complete draft를 immutable, reproducible, corruption-detecting artifact로 바꾼다. |
| 사전조건 | WP-02.3 complete coverage, encoding/fingerprint ADR review |
| 예상 file/package/type | `PreparedTravel`, canonical encoder, fingerprint, provenance, safe report, optional whole-artifact cache fake |
| 구체 행동 | Stable tuple order → length-prefixed versioned encoding → mapping/value/source/policy fingerprint → defensive copy → total lookup → corruption/cache hit-miss tests |
| 선택 근거 | 값만 hash하면 같은 숫자지만 다른 authority인 artifact를 구분하지 못한다. |
| 금지 shortcut | `HashMap` iteration, timestamp/retry/provider locator/random UUID를 semantic fingerprint에 포함, partial arc cache, corrupt hit silent fallback |
| 검증 명령 | 아래 future command |
| 기대 결과 | Same authority 반복·permutation·cache mode는 same bytes; source/policy/value/mapping 변경은 fingerprint 변경 |
| 실패 해석 | Same identity/different bytes 또는 digest mismatch는 integrity incident |
| Rollback | Corrupt cache/object 격리. 같은 identity overwrite 금지; encoding 변경 시 새 version |
| 다음 handoff | Immutable travel과 exact fingerprint를 WP-02.5에 전달 |

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am clean test \
  -Dtest=PreparedTravelImmutabilityTest,PreparedTravelCorruptionTest,TravelFingerprintReproducibilityTest,TravelPreparationReportTest \
  -DfailIfNoTests=true \
  -Dsurefire.failIfNoSpecifiedTests=true
```

Safe report에는 `M`, `V`, expected/actual coverage, provided/generated/self/default count, policy IDs, artifact fingerprints와 typed outcome만 둔다. Raw address, full coordinate, provider locator/response, credential, full input와 모든 arc dump는 넣지 않는다.

### WP-02.5 — Immutable `ProblemInstance` freeze

| 항목 | 지시 |
|---|---|
| 목적/이유 | Phase 01 normalized facts, dense mapping과 exact travel authority를 하나의 immutable solver declaration으로 묶는다. |
| 사전조건 | Phase 01 identity green, WP-02.4 immutable travel green |
| 예상 file/package/type | `ProblemInstance`, `ProblemInstanceFactory`, fingerprint, reference/corruption/immutability tests |
| 구체 행동 | Array length/bijection → request pickup semantics와 delivery node → logical pickup physical/zone-visit/resource collection 제외 → physical node→location과 delivery/real-pickup zone fact 보존 → Phase 01 unit-bearing demand/capacity 보존 → vehicle→terminal/travel view → static compatibility facts → exact travel bind → defensive freeze |
| 선택 근거 | Travel만 complete해도 wrong node/location/vehicle reference가 있으면 Phase 03이 안전하게 계산할 수 없다. |
| 금지 shortcut | No-compatible vehicle request를 malformed input으로 거부, mutable bitset/map 보관, problem 생성 뒤 travel 교체 |
| 검증 명령 | 아래 future command |
| 기대 결과 | Valid freeze, logical pickup이 `M`/`M²`/stop/service/travel call count/zone visit/zone-resource membership을 늘리지 않음, delivery와 real physical pickup의 정상 zone fact 보존, unit-bearing demand 무재정규화/weight-volume type 분리, 모든 dangling/wrong-kind/mismatch/corruption rejection, builder mutation 뒤 bytes 불변 |
| 실패 해석 | Problem draft defect. 이미 immutable인 PreparedTravel 자체는 보존할 수 있으나 problem은 발행하지 않음 |
| Rollback | Problem draft 전체 폐기. 수정된 input/travel은 새 identity로 다시 prepare/freeze |
| 다음 handoff | Exact pair `ProblemInstance + PreparedTravel`을 WP-02.6에 전달 |

```bash
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am clean test \
  -Dtest=ProblemInstanceTest,ProblemInstanceCorruptionTest,DenseIdBijectionPropertyTest \
  -DfailIfNoTests=true \
  -Dsurefire.failIfNoSpecifiedTests=true
```

### WP-02.6 — Architecture, integration, evidence와 Phase 03 handoff

| 항목 | 지시 |
|---|---|
| 목적/이유 | Local class green을 실제 module/consumer/evidence gate로 확장한다. |
| 사전조건 | WP-02.1~5 green, reviewer 지정, Phase 00 accepted architecture command |
| 예상 변경 | Architecture rules/tests, consumer contract tests, immutable evidence artifacts. Registry는 scheduler만 변경 |
| 구체 행동 | Core-local test helper와 downstream fixture edge 확인 → core clean verify + core 53-row scope 판정 → architecture `-am` clean verify + full 58-row 판정 → full root clean verify + full 58-row 판정 → Phase 03 mismatch rejection → 세 evidence key → pre-review manifest → independent review → acceptance receipt |
| 선택 근거 | Live Phase 00/legacy test와 Phase 02 target test 0건을 구분해 false-green을 막고 authority chain을 봉인한다. |
| 금지 shortcut | `failIfNoSpecifiedTests=false`, skipped/disabled required test, stale `target/`, console 한 줄, reviewer 정보를 pre-review manifest에 backfill |
| 검증 명령 | 아래 future commands. Phase 00가 module slug를 바꾸면 semantic owner를 유지한 새 command를 review |
| 기대 결과 | OR-Tools-free root green, required method 전부 fresh, forbidden dependency/fallback 0, evidence digest chain 완전 |
| 실패 해석 | 최대 `IMPLEMENTED_PENDING_EVIDENCE` 또는 `REVIEW_PENDING`; `ACCEPTED`나 handoff authority 아님 |
| Rollback | Phase 03에 전달하지 않고 마지막 accepted Phase 01 artifact를 safe point로 유지 |
| 다음 handoff | Scheduler-accepted `Phase02HandoffManifest`를 Phase 03 owner가 검증 |

```bash
# FUTURE AFTER Phase 00 acceptance.
# Evidence owner가 세 unique immutable run ID와 toolchain record를 미리 제공한다.
set -eu
: "${P02_CORE_RUN_ID:?}"
: "${P02_ARCHITECTURE_RUN_ID:?}"
: "${P02_ROOT_RUN_ID:?}"
: "${P02_TOOLCHAIN_RECORD:?}"
: "${P02_SOURCE_MANIFEST:?}"
test -s "$P02_TOOLCHAIN_RECORD"
test -s "$P02_SOURCE_MANIFEST"

# Core unit test는 core/src/test만 사용하며 build/test-fixtures를 역의존하지 않는다.
P02_RUN_ID=$P02_CORE_RUN_ID
P02_RUN_STARTED_NS=$(python3 -c 'import time; print(time.time_ns())')
./mvnw -B -ntp -Dstyle.color=never \
  -pl rpdptw/core -am clean verify \
  -DfailIfNoTests=true
python3 build/architecture-rules/src/test/scripts/check-phase-02-required-tests.py \
  --manifest rpdptw/core/src/test/resources/phase-02-required-tests.psv \
  --run-kind core \
  --run-id "$P02_RUN_ID" \
  --run-started-ns "$P02_RUN_STARTED_NS" \
  --toolchain-record "$P02_TOOLCHAIN_RECORD" \
  --source-manifest "$P02_SOURCE_MANIFEST" \
  --evidence-root build/evidence/phase-02/test-runs

# Downstream architecture/consumer 검사는 current DAG 방향으로 수행하고,
# Architecture §19.1의 -am closure에서 core 53 + architecture 5를 함께 판정한다.
P02_RUN_ID=$P02_ARCHITECTURE_RUN_ID
P02_RUN_STARTED_NS=$(python3 -c 'import time; print(time.time_ns())')
./mvnw -B -ntp -Dstyle.color=never \
  -pl build/architecture-rules -am clean verify
python3 build/architecture-rules/src/test/scripts/check-phase-02-required-tests.py \
  --manifest rpdptw/core/src/test/resources/phase-02-required-tests.psv \
  --run-kind architecture \
  --run-id "$P02_RUN_ID" \
  --run-started-ns "$P02_RUN_STARTED_NS" \
  --toolchain-record "$P02_TOOLCHAIN_RECORD" \
  --source-manifest "$P02_SOURCE_MANIFEST" \
  --evidence-root build/evidence/phase-02/test-runs

# Final root exit도 full 58을 다시 판정한다. core scope로 낮추면 exit failure다.
P02_RUN_ID=$P02_ROOT_RUN_ID
P02_RUN_STARTED_NS=$(python3 -c 'import time; print(time.time_ns())')
./mvnw -B -ntp -Dstyle.color=never clean verify
python3 build/architecture-rules/src/test/scripts/check-phase-02-required-tests.py \
  --manifest rpdptw/core/src/test/resources/phase-02-required-tests.psv \
  --run-kind root \
  --run-id "$P02_RUN_ID" \
  --run-started-ns "$P02_RUN_STARTED_NS" \
  --toolchain-record "$P02_TOOLCHAIN_RECORD" \
  --source-manifest "$P02_SOURCE_MANIFEST" \
  --evidence-root build/evidence/phase-02/test-runs
```

현재 POM은 Surefire를 pin하지만 Failsafe를 구성하지 않았고 module-level `failIfNoTests=false`다. 따라서 현재 `*Test`는 Surefire 대상이고 `*IT`는 발견/실행된다고 가정할 수 없다. Phase 02가 `*IT`를 required manifest에 추가한다면 먼저 Phase 00/Architecture가 Failsafe plugin/version/execution과 module ownership을 승인하고, `verify` 뒤 `target/failsafe-reports/TEST-*.xml`까지 같은 oracle로 검사한다. 설정 없는 `*IT` file 존재는 evidence가 아니다.

각 clean run 직후 §10.2의 checker를 실행하고 그 다음 command를 시작한다. `core` run은
full PSV 중 module이 정확히 `rpdptw/core`인 53개만 선택한다. `architecture`와 `root`
run은 core 53개와 `build/architecture-rules` 5개를 합친 full 58개를 선택한다. 즉
`core53 ⊂ full58`, `full58 - core53 = architecture5`이며 final exit에서 core filter를
재사용할 수 없다.

`clean`이 report를 지우므로 core, architecture, root run의 XML을 섞지 않는다. Checker는
run 시작 timestamp보다 오래된 XML을 거부하고, 아직 존재하지 않는 unique `run-id`
evidence directory를 원자적으로 만들며 manifest/선택 scope/XML/command·toolchain
metadata digest를 한 run identity로 봉인해야 한다. `mvn clean verify`가 성공해도 exact
required class/method manifest가 없으면 evidence failure다. Test class가 없거나 test
count가 0이거나 required method가 missing/duplicate/skipped이면 Maven exit `0`을
무시하고 실패로 판정한다.

## 10. 테스트 안내

### 10.1 Red → green 순서

1. 독립 expected tuple/key/value를 먼저 작성한다.
2. 아직 type이 없어 compile 또는 assertion이 실패하는 future-red를 기록한다.
3. Identity/value/failure test를 green으로 만든다.
4. Source handoff negative test를 green으로 만든다.
5. Travel formula/coverage test를 green으로 만든다.
6. Immutability/canonicalization/corruption test를 green으로 만든다.
7. Problem freeze/reference test를 green으로 만든다.
8. Fault/security/reproducibility/architecture test를 green으로 만든다.
9. Full module/root verification과 consumer handoff test를 green으로 만든다.

Test 삭제, assertion 완화, broad exception catch, `@Disabled`, fixture authority label 변경, false-pass Maven option으로 green을 만들지 않는다.

### 10.2 Exit-required test class/method와 oracle

아래 목록은 “대표 후보”가 아니라 Phase 02 exit에 필요한 **완전한 required method set**이다. Java type 이름이 Architecture review에서 바뀌어 test 이름도 바꿔야 하면 구현 전 이 표, machine-readable manifest, traceability와 evidence plan을 같은 review 단위에서 갱신한다. Test를 조용히 삭제하거나 optional로 낮추지 않는다.

| Test class | Exit-required method | Fixture/oracle | 잡아야 할 결함 |
|---|---|---|---|
| `DenseIdBijectionTest` | `roundTripsEveryExternalAndDenseId()` | Small opaque ID list | Wrong reverse mapping |
|  | `rejectsDuplicateExternalIdsAndDenseHoles()` | Duplicate/hole builder | Silent overwrite |
| `DenseIdBijectionPropertyTest` | `shuffledEquivalentEntitiesProduceCanonicalMapping()` | Seeded permutations + shrink | Order nondeterminism |
|  | `checkedCellIndexNeverWraps()` | Boundary `M/V` generator | `M²`, `V×M²` overflow |
| `TravelSourceHandoffTest` | `acceptsOnlyTravelSealedInThePhase01Artifact()` | Detached batch negative | Multiple authority |
|  | `rejectsInvalidDeclaredSourceIdentityBeforeGeneration()` | One-field corruption | Invalid→generation fallback |
|  | `preservesApprovedFixtureAndOfficialSnapshotDistinction()` | Two typed identities | Authority label confusion |
| `TravelPreparerTest` | `usesProvidedDirectedIntegerDistanceAndCommonTimeFirst()` | Independent integer hand oracle | Provided overwritten |
|  | `rejectsDecimalProvidedDistanceOrTimeWithoutRounding()` | Decimal token negative | Coercion |
|  | `normalizesEverySelfArcToZeroAndRecordsOverride()` | Non-zero diagonal | Raw diagonal authority |
|  | `preservesAsymmetricDirectedArcs()` | A→B/B→A distinct | Reverse fallback |
|  | `generatesOnlyMissingDistanceWithApprovedFunctionAndHalfUp()` | Approved boundary vectors | Wrong function/rounding |
|  | `rejectsMissingDistanceWhenCoordinateIsAbsent()` | Missing coordinate + zero-call spies | Hidden fallback/partial publication |
|  | `preservesValidNonSelfZeroDistanceAndTimeForCoLocatedLocations()` | Distinct location IDs with provided zero | Invalid positive-only rule |
|  | `rejectsNegativeProvidedDistanceOrTime()` | Negative D/U variants | Zero/negative conflation |
|  | `generatesVehicleSpecificMissingTimeWithCeiling()` | Exact rational oracle | Double/truncation |
|  | `usesFortyFiveOnlyWhenSpeedIsMissing()` | Missing variant | Wrong default trigger |
|  | `rejectsPresentInvalidSpeedInsteadOfDefaulting()` | Zero/negative speed | Invalid→missing |
|  | `keepsProvidedCommonTimeWhenDistanceWasGenerated()` | Missing D/present U | U overwritten |
|  | `rejectsDuplicateConflictingDirectedCell()` | Conflicting declaration | Last-write-wins |
|  | `rejectsUnknownLocationOrVehicleReference()` | Unknown IDs | Dangling reference |
| `TravelPreparerPropertyTest` | `resolvesExactlyAllMByMDirectedDistanceCells()` | Cartesian key oracle | Incomplete distance |
|  | `resolvesEveryUsedVehicleTimeCell()` | Cartesian vehicle key oracle | Partial time |
|  | `permutedSourceOrderHasSameBytesAndFingerprint()` | Permutations | Iteration leak |
|  | `differentSourcePolicyOrValueChangesFingerprint()` | One-field mutation | Fingerprint under-binding |
| `PreparedTravelImmutabilityTest` | `mutatingConstructorInputsCannotChangeArtifact()` | Mutable arrays/maps | Constructor alias |
|  | `returnedViewsCannotMutateBackingState()` | Mutation probes | Accessor alias |
|  | `problemAndTravelDoNotShareMutableBuilderState()` | Shared builder | Cross-artifact alias |
| `TravelPreparationFaultInjectionTest` | `injectedFailurePublishesNoPartialArtifactOrCacheEntry()` | Fail-after-N fake | Partial commit |
|  | `upstreamFailureMarkerNeverFallsBackToFixtureCacheReverseOrGenerator()` | Call-count spies | Unauthorized fallback |
|  | `checkedDistanceTimeAndIndexOverflowAreTypedFailures()` | BigInteger/reference bounds | Wrap/saturation |
|  | `invalidSourceHandoffFailsBeforeGeneration()` | Invalid Phase 01 handoff | Invalid→generation |
|  | `cancelledPreparationDiscardsDraft()` | Cancellation fake | Partial artifact exposure |
| `PreparedTravelCorruptionTest` | `rejectsAlteredCellWithUnchangedDeclaredFingerprint()` | Direct byte/value mutation | Value corruption |
|  | `rejectsAlteredProvenanceOrMissingCoverage()` | One-field corruption | Authority/coverage corruption |
|  | `rejectsSameIdentityDifferentCanonicalBytes()` | Cache fake | Identity collision/overwrite |
| `TravelFingerprintReproducibilityTest` | `repeatPreparationProducesIdenticalCanonicalBytes()` | Fixed test manifest | Hidden nondeterminism |
|  | `cacheHitMissAndDisabledProduceSameArtifact()` | Whole-artifact fake | Cache authority |
|  | `volatileProviderMetadataDoesNotChangeSemanticFingerprint()` | Timestamp/order/attempt variants | Runtime metadata leakage |
| `ProblemInstanceTest` | `freezesValidPairNodeLocationVehicleReferences()` | Canonical builder | Invalid freeze |
|  | `bindsExactPreparedTravelFingerprint()` | Independent fingerprint expectation | Detached travel |
|  | `keepsNoCompatibleVehicleAsStaticFactNotStructuralError()` | No-eligible request | Wrong rejection |
|  | `keepsLogicalInitialLoadOutOfPhysicalAndZoneResourceSets()` | Delivery-only request-count delta; physical collections와 zone facts를 서로 재사용하지 않는 두 independent expected-set oracle | Fake node/location/travel/visit 또는 logical-pickup zone visit/membership; physical delivery/real-pickup zone fact loss |
|  | `preservesPhase01UnitBearingDemandValuesWithoutRenormalization()` | Distinct milli-kg/milli-CBM sentinels | Unit swap/re-rounding |
| `ProblemInstanceCorruptionTest` | `rejectsSplitDanglingOrWrongKindPairReferences()` | Reference corruption | Pair/reference defect |
|  | `rejectsTravelMappingOrFingerprintMismatch()` | Swapped mapping | Wrong authority |
|  | `rejectsArrayLengthAndDenseRangeCorruption()` | Truncated arrays | Partial snapshot |
| `TravelProvenanceSecurityTest` | `redactsCredentialRawPayloadAddressAndCoordinatesFromFailureEvidence()` | Canary markers | Sensitive-data leak |
|  | `providerLocatorAndAttemptDoNotBecomeDomainIdentity()` | Locator/attempt variants | Provider coupling |
| `TravelPreparationReportTest` | `aggregateCountsMatchPreparedCoverageAndSourceKinds()` | Independent count oracle | False observability |
|  | `safeReportDoesNotChangeSemanticFingerprint()` | Report metadata variants | Observability→identity |
| `TravelPreparationComplexityTest` | `callsGreatCircleExactlyOncePerMissingNonSelfDistance()` | Counting function | Repeated/reverse generation |
|  | `doesNotPerformRuntimeOrPostFreezeSourceCalls()` | Throw-after-freeze fake | Lazy fallback |
| `Phase02ArchitectureTest` | `coreHasNoProviderCloudHttpSdkDependency()` | Dependency/bytecode scan | SDK leakage |
|  | `solverAndVerifierCannotReprepareOrFallbackTravel()` | Package/symbol scan | Multiple authority paths |
|  | `productionScopeContainsNoTestFixtureClass()` | Scope inspection | Fixture leakage |
|  | `coreTestScopeDoesNotDependOnDownstreamTestFixtures()` | Reactor dependency graph | Fixture cycle/stale local artifact |
|  | `problemBoundaryUsesDistinctWeightAndVolumeValueTypesInsteadOfRawLongs()` | Compiled signature inspection | Raw primitive/unit swap |

승인된 implementation change에는 아래 내용과 정확히 같은
`rpdptw/core/src/test/resources/phase-02-required-tests.psv`를 둔다. 이 file은 test
contract라서 production artifact에 포함하지 않고 pre-review manifest가 exact digest를
봉인한다. 현재 모든 required test는 `surefire`다. Failsafe가 승인된 뒤 `failsafe` row를
추가할 때는 plugin execution과 report directory도 같은 change/review에서 추가한다.

Full PSV는 정확히 58개 unique row이며 `rpdptw/core` 53개와
`build/architecture-rules` 5개뿐이다. Core run filter는 full PSV에서 module이
`rpdptw/core`인 row를 선택한 exact 53-row proper subset이다. Architecture `-am` run과
root run은 filter 없이 full 58개를 요구한다. 위 logical-initial-load method는 기존
physical-collection oracle을 약화하지 않고 zone 전용 expected set을 독립 계산한다.
Delivery-only request 수만 바꾼 두 fixture에서 logical pickup이 추가한 zone visit과
zone-resource membership delta가 각각 정확히 0인지 확인하고, delivery node와 real
physical pickup의 expected zone fact 집합도 별도로 exact equality로 확인한다.

```text
# module|engine|fully-qualified-class|method
rpdptw/core|surefire|com.ronext.rpdptw.domain.DenseIdBijectionTest|roundTripsEveryExternalAndDenseId
rpdptw/core|surefire|com.ronext.rpdptw.domain.DenseIdBijectionTest|rejectsDuplicateExternalIdsAndDenseHoles
rpdptw/core|surefire|com.ronext.rpdptw.domain.DenseIdBijectionPropertyTest|shuffledEquivalentEntitiesProduceCanonicalMapping
rpdptw/core|surefire|com.ronext.rpdptw.domain.DenseIdBijectionPropertyTest|checkedCellIndexNeverWraps
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelSourceHandoffTest|acceptsOnlyTravelSealedInThePhase01Artifact
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelSourceHandoffTest|rejectsInvalidDeclaredSourceIdentityBeforeGeneration
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelSourceHandoffTest|preservesApprovedFixtureAndOfficialSnapshotDistinction
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|usesProvidedDirectedIntegerDistanceAndCommonTimeFirst
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|rejectsDecimalProvidedDistanceOrTimeWithoutRounding
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|normalizesEverySelfArcToZeroAndRecordsOverride
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|preservesAsymmetricDirectedArcs
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|generatesOnlyMissingDistanceWithApprovedFunctionAndHalfUp
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|rejectsMissingDistanceWhenCoordinateIsAbsent
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|preservesValidNonSelfZeroDistanceAndTimeForCoLocatedLocations
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|rejectsNegativeProvidedDistanceOrTime
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|generatesVehicleSpecificMissingTimeWithCeiling
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|usesFortyFiveOnlyWhenSpeedIsMissing
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|rejectsPresentInvalidSpeedInsteadOfDefaulting
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|keepsProvidedCommonTimeWhenDistanceWasGenerated
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|rejectsDuplicateConflictingDirectedCell
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerTest|rejectsUnknownLocationOrVehicleReference
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerPropertyTest|resolvesExactlyAllMByMDirectedDistanceCells
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerPropertyTest|resolvesEveryUsedVehicleTimeCell
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerPropertyTest|permutedSourceOrderHasSameBytesAndFingerprint
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparerPropertyTest|differentSourcePolicyOrValueChangesFingerprint
rpdptw/core|surefire|com.ronext.rpdptw.travel.PreparedTravelImmutabilityTest|mutatingConstructorInputsCannotChangeArtifact
rpdptw/core|surefire|com.ronext.rpdptw.travel.PreparedTravelImmutabilityTest|returnedViewsCannotMutateBackingState
rpdptw/core|surefire|com.ronext.rpdptw.travel.PreparedTravelImmutabilityTest|problemAndTravelDoNotShareMutableBuilderState
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationFaultInjectionTest|injectedFailurePublishesNoPartialArtifactOrCacheEntry
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationFaultInjectionTest|upstreamFailureMarkerNeverFallsBackToFixtureCacheReverseOrGenerator
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationFaultInjectionTest|checkedDistanceTimeAndIndexOverflowAreTypedFailures
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationFaultInjectionTest|invalidSourceHandoffFailsBeforeGeneration
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationFaultInjectionTest|cancelledPreparationDiscardsDraft
rpdptw/core|surefire|com.ronext.rpdptw.travel.PreparedTravelCorruptionTest|rejectsAlteredCellWithUnchangedDeclaredFingerprint
rpdptw/core|surefire|com.ronext.rpdptw.travel.PreparedTravelCorruptionTest|rejectsAlteredProvenanceOrMissingCoverage
rpdptw/core|surefire|com.ronext.rpdptw.travel.PreparedTravelCorruptionTest|rejectsSameIdentityDifferentCanonicalBytes
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelFingerprintReproducibilityTest|repeatPreparationProducesIdenticalCanonicalBytes
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelFingerprintReproducibilityTest|cacheHitMissAndDisabledProduceSameArtifact
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelFingerprintReproducibilityTest|volatileProviderMetadataDoesNotChangeSemanticFingerprint
rpdptw/core|surefire|com.ronext.rpdptw.domain.ProblemInstanceTest|freezesValidPairNodeLocationVehicleReferences
rpdptw/core|surefire|com.ronext.rpdptw.domain.ProblemInstanceTest|bindsExactPreparedTravelFingerprint
rpdptw/core|surefire|com.ronext.rpdptw.domain.ProblemInstanceTest|keepsNoCompatibleVehicleAsStaticFactNotStructuralError
rpdptw/core|surefire|com.ronext.rpdptw.domain.ProblemInstanceTest|keepsLogicalInitialLoadOutOfPhysicalAndZoneResourceSets
rpdptw/core|surefire|com.ronext.rpdptw.domain.ProblemInstanceTest|preservesPhase01UnitBearingDemandValuesWithoutRenormalization
rpdptw/core|surefire|com.ronext.rpdptw.domain.ProblemInstanceCorruptionTest|rejectsSplitDanglingOrWrongKindPairReferences
rpdptw/core|surefire|com.ronext.rpdptw.domain.ProblemInstanceCorruptionTest|rejectsTravelMappingOrFingerprintMismatch
rpdptw/core|surefire|com.ronext.rpdptw.domain.ProblemInstanceCorruptionTest|rejectsArrayLengthAndDenseRangeCorruption
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelProvenanceSecurityTest|redactsCredentialRawPayloadAddressAndCoordinatesFromFailureEvidence
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelProvenanceSecurityTest|providerLocatorAndAttemptDoNotBecomeDomainIdentity
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationReportTest|aggregateCountsMatchPreparedCoverageAndSourceKinds
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationReportTest|safeReportDoesNotChangeSemanticFingerprint
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationComplexityTest|callsGreatCircleExactlyOncePerMissingNonSelfDistance
rpdptw/core|surefire|com.ronext.rpdptw.travel.TravelPreparationComplexityTest|doesNotPerformRuntimeOrPostFreezeSourceCalls
build/architecture-rules|surefire|com.ronext.rpdptw.architecture.Phase02ArchitectureTest|coreHasNoProviderCloudHttpSdkDependency
build/architecture-rules|surefire|com.ronext.rpdptw.architecture.Phase02ArchitectureTest|solverAndVerifierCannotReprepareOrFallbackTravel
build/architecture-rules|surefire|com.ronext.rpdptw.architecture.Phase02ArchitectureTest|productionScopeContainsNoTestFixtureClass
build/architecture-rules|surefire|com.ronext.rpdptw.architecture.Phase02ArchitectureTest|coreTestScopeDoesNotDependOnDownstreamTestFixtures
build/architecture-rules|surefire|com.ronext.rpdptw.architecture.Phase02ArchitectureTest|problemBoundaryUsesDistinctWeightAndVolumeValueTypesInsteadOfRawLongs
```

승인된 implementation change에는 아래 exact checker source를
`build/architecture-rules/src/test/scripts/check-phase-02-required-tests.py`에 두고 각
`clean verify` 직후 WP-02.6 interface로 실행한다. 이 checker는 full manifest가 정확히
core 53 + architecture 5 = 58인지 먼저 닫고 run kind에 따라 `core=53`,
`architecture/root=58`만 선택한다. Report file 0개, suite test 0건, stale XML,
failure/error/skipped, manifest duplicate, missing 또는 duplicate testcase는 모두
non-zero다.

```python
#!/usr/bin/env python3
import argparse
from collections import Counter
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import tempfile
import xml.etree.ElementTree as ET

EXPECTED_MODULE_COUNTS = {
    "rpdptw/core": 53,
    "build/architecture-rules": 5,
}
RUN_MODULES = {
    "core": {"rpdptw/core"},
    "architecture": set(EXPECTED_MODULE_COUNTS),
    "root": set(EXPECTED_MODULE_COUNTS),
}
COMMAND_IDS = {
    "core": "phase02-core-am-clean-verify-v1",
    "architecture": "phase02-architecture-am-clean-verify-v1",
    "root": "phase02-root-clean-verify-v1",
}

parser = argparse.ArgumentParser()
parser.add_argument("--manifest", required=True, type=Path)
parser.add_argument("--run-kind", required=True, choices=sorted(RUN_MODULES))
parser.add_argument("--run-id", required=True)
parser.add_argument("--run-started-ns", required=True, type=int)
parser.add_argument("--toolchain-record", required=True, type=Path)
parser.add_argument("--source-manifest", required=True, type=Path)
parser.add_argument("--evidence-root", required=True, type=Path)
args = parser.parse_args()

if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._-]{0,127}", args.run_id):
    raise SystemExit("invalid immutable run id")
if args.run_started_ns <= 0:
    raise SystemExit("run-started-ns must be positive")
if not args.manifest.is_file() or args.manifest.stat().st_size == 0:
    raise SystemExit("required test manifest is missing or empty")
if not args.toolchain_record.is_file() or args.toolchain_record.stat().st_size == 0:
    raise SystemExit("toolchain record is missing or empty")
if not args.source_manifest.is_file() or args.source_manifest.stat().st_size == 0:
    raise SystemExit("source manifest is missing or empty")

rows = []
for number, raw in enumerate(
    args.manifest.read_text(encoding="utf-8").splitlines(), 1
):
    line = raw.strip()
    if not line or line.startswith("#"):
        continue
    parts = tuple(line.split("|"))
    if len(parts) != 4 or any(not part for part in parts):
        raise SystemExit(f"invalid manifest row {number}: {raw!r}")
    if parts[1] not in {"surefire", "failsafe"}:
        raise SystemExit(f"unsupported engine in row {number}: {parts[1]}")
    rows.append(parts)

duplicates = [row for row, count in Counter(rows).items() if count != 1]
if duplicates:
    raise SystemExit(f"duplicate manifest rows: {duplicates}")
module_counts = Counter(row[0] for row in rows)
if module_counts != Counter(EXPECTED_MODULE_COUNTS) or len(rows) != 58:
    raise SystemExit(
        f"full manifest must be exact core53+architecture5=58: {module_counts}"
    )

full_rows = set(rows)
core_rows = {row for row in rows if row[0] == "rpdptw/core"}
architecture_rows = full_rows - core_rows
if not (
    len(core_rows) == 53
    and len(architecture_rows) == 5
    and core_rows < full_rows
    and {row[0] for row in architecture_rows} == {"build/architecture-rules"}
):
    raise SystemExit("invalid core53 proper-subset/full58 relationship")

selected_modules = RUN_MODULES[args.run_kind]
selected_rows = {row for row in rows if row[0] in selected_modules}
expected_required = 53 if args.run_kind == "core" else 58
if len(selected_rows) != expected_required:
    raise SystemExit(
        f"{args.run_kind} requires {expected_required}, got {len(selected_rows)}"
    )
if args.run_kind != "core" and selected_rows != full_rows:
    raise SystemExit("architecture/root run may not weaken full 58-method exit")

observed = Counter()
report_counts = Counter()
reports_by_key = {}
bad = []
for module, engine in sorted({(row[0], row[1]) for row in selected_rows}):
    report_dir = Path(module) / "target" / f"{engine}-reports"
    reports = sorted(report_dir.glob("TEST-*.xml"))
    reports_by_key[(module, engine)] = reports
    report_counts[(module, engine)] += 0
    if not reports:
        bad.append(f"no fresh XML reports: {report_dir}")
        continue
    for report in reports:
        if report.stat().st_mtime_ns < args.run_started_ns:
            bad.append(f"stale XML report: {report}")
            continue
        root = ET.parse(report).getroot()
        for element in root.iter():
            tag = element.tag.rsplit("}", 1)[-1]
            if tag == "testsuite" and any(
                int(element.attrib.get(name, "0")) != 0
                for name in ("failures", "errors", "skipped")
            ):
                bad.append(f"non-zero suite summary: {report}")
            if tag != "testcase":
                continue
            report_counts[(module, engine)] += 1
            key = (
                module,
                engine,
                element.attrib.get("classname", ""),
                element.attrib.get("name", ""),
            )
            observed[key] += 1
            child_tags = {
                child.tag.rsplit("}", 1)[-1] for child in list(element)
            }
            if child_tags & {"failure", "error", "skipped"}:
                bad.append(f"non-pass testcase: {key}")

for report_key, count in report_counts.items():
    if count == 0:
        bad.append(f"zero testcases: {report_key}")
for row in selected_rows:
    count = observed[row]
    if count == 0:
        bad.append(f"missing required testcase: {row}")
    elif count != 1:
        bad.append(f"duplicate required testcase ({count}): {row}")
if bad:
    raise SystemExit("\n".join(bad))

def digest(path):
    value = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()

args.evidence_root.mkdir(parents=True, exist_ok=True)
final_dir = args.evidence_root / args.run_id
if final_dir.exists():
    raise SystemExit(f"immutable run identity already exists: {final_dir}")
temporary_dir = Path(
    tempfile.mkdtemp(prefix=f".{args.run_id}.", dir=args.evidence_root)
)
report_digests = {}
for (module, engine), reports in reports_by_key.items():
    destination = temporary_dir / "reports" / module / engine
    destination.mkdir(parents=True, exist_ok=True)
    for report in reports:
        copied = destination / report.name
        shutil.copy2(report, copied)
        report_digests[str(copied.relative_to(temporary_dir))] = digest(copied)
shutil.copy2(args.manifest, temporary_dir / "phase-02-required-tests.psv")
shutil.copy2(args.toolchain_record, temporary_dir / "toolchain-record")
shutil.copy2(args.source_manifest, temporary_dir / "source-manifest")
metadata = {
    "runId": args.run_id,
    "runKind": args.run_kind,
    "commandId": COMMAND_IDS[args.run_kind],
    "runStartedNs": args.run_started_ns,
    "requiredCount": expected_required,
    "fullManifestCount": 58,
    "coreSubsetCount": 53,
    "manifestSha256": digest(args.manifest),
    "toolchainRecordSha256": digest(args.toolchain_record),
    "sourceManifestSha256": digest(args.source_manifest),
    "reportSha256": dict(sorted(report_digests.items())),
}
(temporary_dir / "run.json").write_text(
    json.dumps(metadata, ensure_ascii=False, sort_keys=True, indent=2) + "\n",
    encoding="utf-8",
)
os.rename(temporary_dir, final_dir)
print(
    f"PASS run={args.run_id} kind={args.run_kind} "
    f"required={expected_required} evidence={final_dir} "
    f"run_json_sha256={digest(final_dir / 'run.json')}"
)
```

XML과 PSV, reviewed command ID, toolchain과 source digest는 checker가 새 run
directory에 묶는다. 호출 wrapper는 checker stdout/stderr/exit code를 별도 immutable
log로 capture하고 `run.json` digest와 함께 pre-review manifest에서 참조한다. Report
또는 manifest가 이전 run이면 timestamp/digest 검사가 실패하고, 같은 `run-id`
재사용·overwrite도 실패한다.

필수 negative control은 다음과 같다.

- Core clean run 직후 `--run-kind architecture` 또는 `root`를 주면 fresh architecture
  report가 없어 non-zero여야 한다.
- Full PSV에서 core row 하나를 삭제·추가하거나 architecture row를 core로 바꾸면
  `core53+architecture5=58`과 proper-subset 검사가 non-zero여야 한다.
- Fresh XML의 required testcase 하나를 삭제·복제·skip/fail/error로 바꾸거나 report
  mtime을 run 시작 전으로 바꾸면 각각 non-zero여야 한다.
- 같은 `run-id`를 다시 쓰면 non-zero여야 한다.
- Final evidence가 `runKind=root`, command ID
  `phase02-root-clean-verify-v1`, `requiredCount=58`이 아니면 §13.3 exit가 실패해야 한다.

Oracle 규칙:

- Production preparer, production canonical encoder와 production rounding helper를 expected 계산에 재사용하지 않는다.
- Generated `U`는 exact decimal/rational reference와 `CEILING`으로 계산한다.
- Great Circle numeric green은 승인된 function/version의 reference vector가 있을 때만 주장한다.
- Coverage expected set은 별도 Cartesian product로 만든다.
- Corruption builder는 정상 encoder가 손상을 다시 정규화하지 못하도록 한 필드/byte를 직접 바꾼다.
- Failure를 잡는 sensitivity mutation을 최소 하나씩 실행해 test가 실제로 red가 되는지 확인한다.

### 10.3 Test category별 적용 판정

| 종류 | Phase 02 적용 | 이유와 pass 판정 |
|---|---|---|
| Unit/boundary | 필수 | Integer/rounding/default/self/reference/overflow positive·negative·boundary 전부 pass |
| Property | 필수 | Dense bijection, ordering, coverage totality; seed와 shrink 가능한 counterexample 기록 |
| Contract | 필수 | Phase 01 단일 source, Phase 03 exact consumer equality와 no-fallback |
| Module integration | 필수 | Identity→travel→problem freeze를 한 core path에서 검증 |
| Local end-to-end | 이 Phase 단독으로는 해당 없음 | Submission부터 publication까지는 Phase 08/07 소유. Phase 02를 E2E 완료로 과장하지 않음 |
| Architecture | 필수 | Provider/cloud/HTTP/OR-Tools, solver/verifier 역의존과 test fixture leakage 0 |
| Fault/cancellation | 필수 | Draft/cache write 0, pre-state 보존, typed failure |
| Corruption | 필수 | Cell/provenance/coverage/mapping/digest 한 필드 손상 모두 reject |
| Reproducibility | 필수 | Same authority input의 canonical bytes/fingerprint 동일 |
| Security | Phase-local 필수 | Credential/PII/raw payload/provider locator leakage 0; authorization 자체는 Phase 08+ |
| Performance | Structural만 필수 | Great Circle call count와 no post-freeze source call. Wall-time/memory threshold는 승인 envelope 전 금지 |
| Official/provider integration | 조건부 | Approved snapshot이 별도 제공된 경우만 exact scope/digest로 실행. Generic Phase acceptance의 필수는 아님 |
| Benchmark quality | 해당 없음 | `Q-BENCH-02`와 Phase 14A/14B 소유 |

### 10.4 False-positive와 false-green 방지

- 현재 live reactor의 Phase 00 architecture/fixture test나 legacy characterization test 성공을 Phase 02 green으로 사용하지 않는다.
- Target module/test 이름이 없으면 targeted command는 실패해야 한다.
- `-Dsurefire.failIfNoSpecifiedTests=false`, `-DskipTests`, `maven.test.skip`을 evidence command에 사용하지 않는다.
- 각 accepted clean run의 fresh Surefire XML과, 승인되어 실제 구성된 경우에만 Failsafe XML을 machine-readable manifest에 대조한다.
- Failed/error/skipped뿐 아니라 missing/duplicate method도 실패다.
- Previous run report와 현재 run report를 섞지 않는다.
- Test-only oracle을 official snapshot이나 provider integration으로 이름 바꾸지 않는다.
- Production code와 같은 helper를 쓰는 expected calculation만으로 pass하지 않는다.
- One-field mutation으로 test sensitivity를 확인한다.
- Cache enabled 경로만 실행하지 않고 disabled/full recomputation과 exact equality를 비교한다.

## 11. 사람 checkpoint와 evidence

### 11.1 Checkpoint 시점과 질문

| Checkpoint | 멈추고 확인할 질문 | 승인/판정 역할 | Stop 조건 | Resume 조건 |
|---|---|---|---|---|
| CP-0 Entry | Phase 00/01가 실제 accepted인가? Module path와 task ID가 있는가? | Scheduler, Architecture, Phase 01 owner | Evidence/receipt 없음 | Exact accepted refs와 owner 배정 |
| CP-1 Identity/API | ID 종류, mapping owner, visibility, package direction이 맞는가? | Architecture + Phase 03/07 consumer | Generic ID/cycle/public API 불명 | Reviewed internal contract |
| CP-2 Travel policy | Great Circle/source/absence/rounding authority가 완전한가? | Input·Matrix owner | Function/version/reference vector 또는 source policy 없음 | Approval record와 negative fixtures |
| CP-3 Artifact identity | Canonical encoding, fingerprint, cache key와 schema migration이 닫혔는가? | Architecture/Data owner | Same identity/different bytes 처리 불명 | Versioned ADR/contract |
| CP-4 Pre-review | Required tests가 fresh이며 세 evidence key가 완전한가? | Implementer + evidence custodian | Missing/skipped/failed, mutable evidence | Digest-protected manifest |
| CP-5 Independent review | Reviewer가 봉인된 manifest만으로 exit gate를 확인했는가? | Independent reviewer | Finding/blocker 또는 manifest drift | Immutable review report |
| CP-6 Acceptance/handoff | Receipt가 manifest+review digest를 참조하고 Phase 03가 artifact를 검증했는가? | Acceptance authority + scheduler + Phase 03 owner | Receipt/equality/handoff 없음 | Valid receipt와 scheduler transition |

### 11.2 Evidence bundle

`E-P02-TRAVEL` 최소 내용:

- Great Circle/source policy approval reference
- Test-only oracle identity와, 조건부 official/approved snapshot의 별도 scope/digest
- Exact commands, toolchain, exit codes와 fresh test method manifest
- `M²`/`V×M²` coverage, formula/asymmetry/no-fallback 결과
- Provided/generated/self/default safe counts
- Fault/corruption/reproducibility/security/structural-performance 결과
- Prepared bytes/fingerprint/digest

`E-P02-DENSE-ID` 최소 내용:

- External↔dense canonical mapping digest
- Bijection/property/permutation/overflow 결과
- Logical-initial-load 제외를 포함한 physical node→location, vehicle mapping과 reference report
- Delivery-only request 수 변화가 `M`, `M²`, travel call count, logical-pickup zone visit과 zone-resource membership을 바꾸지 않는 independent oracle 결과
- Delivery node와 real physical pickup의 정상 zone fact가 expected physical fact set과 정확히 같은 보존 결과

`E-P02-PROBLEM` 최소 내용:

- Problem schema/fingerprint/digest
- Exact prepared fingerprint equality
- Pickup semantics/pair/node/location/vehicle/zone-fact reference와 no-alias/corruption 결과
- Phase 01 milli-kg/milli-CBM demand/capacity type mapping, 무재정규화와 primitive/unit-swap 방지 결과
- Phase 03 handoff manifest와 rollback point

세 evidence key는 §10.2 full 58-row PSV digest, core 53-row explicit filter와 proper-subset
판정, accepted clean-run별 Maven command/toolchain/exit, fresh Surefire/Failsafe XML
digest와 checker stdout/stderr/exit를 공통으로 참조한다. Core-local fixture와 downstream
`build/test-fixtures` dependency graph도 architecture evidence에 넣어 cycle이나 stale
local artifact 통과가 없음을 보인다. Final root evidence의 `runKind=root`,
`requiredCount=58`과 immutable run identity가 없으면 세 key가 있어도 exit evidence가
아니다.

Evidence provenance는 다음 단방향 DAG를 지킨다.

```text
implementation + test outputs
  → preReviewEvidenceManifest [digest M]
  → independentReviewReport [references M, digest R]
  → postReviewAcceptanceReceipt [references M + R]
  → scheduler ACCEPTED + handoff
```

Pre-review manifest에는 reviewer identity, verdict, review report ref/digest, acceptance status나 receipt를 넣지 않는다. Review 뒤 manifest bytes를 수정하거나 backfill하지 않는다.

Evidence에 secret, credential, raw address/full coordinate, full input, mutable `latest` path, stale `target/`만 있는 reference를 넣지 않는다. 사람이 읽는 summary와 machine-readable artifact 모두 content digest로 연결한다.

## 12. 흔한 오해와 anti-pattern

1. **“Node가 matrix index다.”** 아니다. Matrix endpoint는 physical location이고 solver node는 service/terminal identity다.

2. **“Provided value가 없으면 반대 방향을 복사하면 된다.”** Directed contract 위반이다. Missing `D`만 승인 Great Circle로 만들고 `A→B`, `B→A`를 독립 처리한다.

3. **“Speed가 이상하면 45를 쓰면 안전하다.”** 45는 missing에만 적용한다. 0, 음수, 파싱 불가처럼 present-invalid는 failure다.

4. **“`record`면 immutable이다.”** 내부 array/map/bitset과 element가 mutable하면 아니다. Constructor와 accessor 모두 방어해야 한다.

5. **“같은 값이면 같은 fingerprint다.”** Source kind/policy/version이 다르면 authority가 다르므로 relevant fingerprint도 달라져야 한다.

6. **“Cache가 깨지면 지우고 다른 provider/generator로 성공시키면 된다.”** Corrupt hit는 integrity incident다. Silent fallback은 evidence와 authority를 잃는다.

7. **“Phase 02에서 provider port를 만들면 나중에 편하다.”** Phase 08 책임을 당기고 Phase 01 artifact와 이중 source authority를 만든다.

8. **“Root Maven이 green이면 Phase 02도 green이다.”** 아니다. Live root는 Phase 00/legacy test를 실행할 수 있고 module-level zero-test도 허용한다. Phase 02 exact required manifest와 fresh XML/evidence가 없으면 red다.

9. **“FLOOR fixture가 있으니 official production travel도 승인됐다.”** `win_poc_case_floor.json`은 plan-final local execution input이다. Official snapshot, Phase 14B authority와 AWS cutover를 승인하지 않는다.

10. **“No-compatible vehicle request는 malformed input이다.”** 아니다. Structural reference는 유효할 수 있고 static unassignability fact로 남는다.

11. **“Phase 02가 route feasibility까지 확인해야 완전하다.”** Phase 02는 problem/travel authority를 완성한다. Route propagation/evaluation은 Phase 03이다.

12. **“Phase 13을 미리 고려해 OR-Tools type을 넣자.”** `C-17` gate 우회다. Generic core/default build에는 vendor API가 0이어야 한다.

추가 금지:

- Raw JSON, `Map<String,Object>`, provider DTO를 `ProblemInstance`에 보관
- Decimal `D/U`를 cast, `longValue`, `Math.round`로 수용
- `double` 누적과 epsilon으로 integer travel을 맞춤
- Diagonal raw `9999/0` 보존
- Unchecked `M*M`, `V*M*M`
- Hash iteration, clock, thread, provider completion order를 canonical identity에 포함
- Partial table/cache를 `PreparedTravel`로 publish
- Search/propagator/verifier에서 좌표·speed/provider를 재조회
- Same identity에 different bytes overwrite

## 13. 실제 구현 Phase exit checklist와 DoD

다음은 모두 AND 조건이다.

### 13.1 Entry와 scope

- [ ] Phase 00과 Phase 01이 실제 `ACCEPTED`이며 acceptance receipts가 있다.
- [ ] Scheduler task, implementer, independent reviewer와 acceptance authority가 지정됐다.
- [ ] Approved Great Circle function/version/reference vectors가 있다.
- [ ] Typed source/absence/no-fallback policy가 승인됐다.
- [ ] Module/package 경로가 Phase 00 contract와 일치한다.
- [ ] Propagation/evaluation/ALNS/provider SDK/MIP/public API를 scope로 당기지 않았다.

### 13.2 Travel과 problem

- [ ] Provided directed integer `D/U`가 generation보다 우선한다.
- [ ] Decimal/negative/invalid/reference conflict를 typed reject한다.
- [ ] 모든 self arc가 `0/0`이고 override provenance가 있다.
- [ ] Missing `D`만 approved Great Circle + `HALF_UP`으로 생성한다.
- [ ] Missing `D`에 필요한 coordinate가 없으면 generator/cache/reverse 호출과 partial publication 없이 typed reject한다.
- [ ] 상위 source contract상 valid한 non-self zero `D/U`는 보존하고 negative/overflow만 reject한다.
- [ ] Missing `U`만 vehicle-specific `CEILING`으로 생성한다.
- [ ] Missing speed에만 정확히 45를 쓴다.
- [ ] Distance `M²`와 every-used-vehicle time coverage가 total이다.
- [ ] Delivery-only logical pickup은 solver node/location/travel/stop/service/zone visit 또는 zone-resource membership을 만들거나 `M`/`M²`/generation call count를 늘리지 않는다.
- [ ] Delivery node와 real physical pickup의 정상 zone fact는 보존되며 delivery-only request 수 변화 전후 expected physical zone fact set과 정확히 같다.
- [ ] Request demand와 vehicle capacity는 Phase 01의 unit-bearing weight/volume type과 exact value를 보존하고 Phase 02 재정규화를 하지 않는다.
- [ ] External↔dense bijection과 모든 typed reference가 valid하다.
- [ ] Prepared travel/problem이 no-alias immutable이다.
- [ ] Problem과 travel fingerprint/mapping이 exact match한다.
- [ ] Runtime lazy/reverse/symmetric/provider fallback이 0이다.

### 13.3 Test와 architecture

- [ ] Unit/boundary/property/contract/fault/corruption/reproducibility/security/observability test가 실제 결함을 검출한다.
- [ ] Core clean run은 full PSV의 exact core 53-row filter, architecture `-am`과 final root clean run은 full 58-row manifest로 판정됐고 각 fresh Surefire/Failsafe XML에서 failed/error/skipped/missing/duplicate가 0이다.
- [ ] `core53 ⊂ full58`, `full58 - core53 = architecture5`가 checker에서 exact하며 final root run은 `runKind=root`, reviewed root command ID와 immutable unique run identity를 가진다.
- [ ] Core unit helper는 core `src/test`에 있고 core→`build/test-fixtures` 역의존/cycle과 production fixture leakage가 0이다.
- [ ] Cache hit/miss/disabled와 full recomputation bytes/fingerprint가 같다.
- [ ] Structural performance call-count가 통과한다.
- [ ] Wall-time/memory official threshold를 임의로 만들지 않았다.
- [ ] Core provider/cloud/HTTP/OR-Tools dependency와 customer/provider branch가 0이다.
- [ ] Test fixture가 production scope에 새지 않는다.
- [ ] OR-Tools-free full root build가 green이다.

### 13.4 Evidence, review와 handoff

- [ ] `E-P02-TRAVEL`, `E-P02-DENSE-ID`, `E-P02-PROBLEM`이 immutable digest를 가진다.
- [ ] Pre-review manifest가 reviewer/review/acceptance 정보를 포함하지 않는다.
- [ ] Independent review report가 exact manifest digest를 참조한다.
- [ ] Acceptance receipt가 manifest와 review digest를 모두 참조한다.
- [ ] Rollback point와 last-safe-point가 명시됐다.
- [ ] Phase 03 owner가 handoff equality/coverage를 검증했다.
- [ ] OPEN/GATED/deferred와 official authority를 숨은 default/완료 상태로 바꾸지 않았다.
- [ ] Scheduler가 authoritative registry를 실제 `ACCEPTED`로 전이했다.

하나라도 충족하지 않으면 `IMPLEMENTED_PENDING_EVIDENCE`, `REVIEW_PENDING`, `BLOCKED` 또는 `FAILED` 중 사실에 맞는 상태이지 `ACCEPTED`가 아니다.

## 14. 다음 Phase 인계

### 14.1 Producer artifact

```text
ProblemInstanceRef
  schemaVersion
  problemFingerprint
  canonicalInputFingerprint
  denseMappingFingerprints
  preparedTravelFingerprint
  contentDigest

PreparedTravelRef
  schemaVersion
  preparedTravelFingerprint
  locationMappingFingerprint
  vehicleMappingFingerprint
  source/generationPolicyFingerprints
  exact M² / usedVehicleTime coverage proof
  contentDigest

Phase02HandoffManifest
  Phase01 input identities
  policy/function identities
  E-P02-* references
  independent review + acceptance receipt references
  rollback point
```

### 14.2 Consumer 확인법

Phase 03는 propagation 전에 다음을 확인한다.

```text
problem.preparedTravelFingerprint == preparedTravel.fingerprint
problem/travel location mapping fingerprints agree
problem/travel vehicle mapping fingerprints agree
all route-reachable solver nodes map to one physical location
all vehicle/location directed lookups are total
numeric/time/service/travel policy identities match handoff
```

하나라도 실패하면 raw coordinate/speed, reverse arc, default나 provider 조회로 보완하지 않고 Phase 02 handoff를 거부한다.

### 14.3 Broken handoff 증상

| 증상 | 가능한 원인 | 올바른 대응 |
|---|---|---|
| Phase 03가 `Optional`/missing travel을 처리해야 함 | Coverage가 complete하지 않음 | Phase 02 artifact 거부·재생성 |
| Propagator가 coordinate/speed를 요구함 | Preparation responsibility 누락 | API/architecture finding으로 되돌림 |
| 같은 route가 solver/verifier에서 다른 time을 가짐 | Fingerprint/source authority drift | Both artifacts 격리, integrity investigation |
| Node 수와 matrix dimension이 같음 | Node/location identity 혼용 | Dense mapping과 travel key 재검토 |
| Unknown node/location이 runtime에 나타남 | Problem reference validation 누락 | Problem artifact reject |
| `A→B`만 있고 `B→A`가 lookup fallback | Directed totality 위반 | Prepared travel reject |
| Problem 생성 뒤 travel만 교체 가능 | Binding/immutability 위반 | 새 problem identity로 다시 freeze |
| Phase 03가 fixture/provider label을 해석함 | Source metadata가 semantic API에 누출 | Read-only value/provenance contract 축소 |

Phase 05와 Phase 07은 이후 consumer지만 Phase 02에서 직접 search/verifier API를 만들지 않는다. Phase 07은 동일 problem/travel authority로 cache-free 검증할 수 있어야 한다는 equality contract만 미리 보존한다.

## 15. Source → requirement → work package → test/evidence traceability

| Requirement | Source | Work package | Test/oracle | Evidence |
|---|---|---|---|---|
| `REQ-P02-AUTH` authority와 gate 보존 | [Implementation README §3](../../README.md#3-source-authority), [Plan §2](../../master-realization-plan.md#2-입력-권위와-충돌-규칙) | WP-02.0, WP-02.6 | Source/blob/section drift check | Bundle metadata |
| Physical-location directed key | [Master §5.1](../../../master-design.md#51-핵심-개념), [§8](../../../master-design.md#8-directed-distancetime-matrix-계약) | WP-02.1, WP-02.3 | Bijection + asymmetry tests | `E-P02-DENSE-ID`, `E-P02-TRAVEL` |
| Integer `D/U`, `C` non-authority | `Q-MTX-01~02`, [Domain §6](../../../2026-07-26-domain-design.md#6-travel-preparation) | WP-02.2~3 | Decimal rejection/provided priority | `E-P02-TRAVEL` |
| Self `0/0` | `Q-MTX-02`, Master §8 | WP-02.3 | `normalizesEverySelfArc...` | `E-P02-TRAVEL` |
| Missing `D` Great Circle + `HALF_UP`, coordinate required | `Q-MTX-03`, [Master §8](../../../master-design.md#8-directed-distancetime-matrix-계약), Domain §6, [Phase 02 §14 `REQ-P02-D-GEN`](../../phases/phase-02-prepared-travel-immutable-problem.md#14-source--requirement--test-traceability) | WP-02.0, WP-02.3 | `generatesOnlyMissingDistanceWithApprovedFunctionAndHalfUp`, `rejectsMissingDistanceWhenCoordinateIsAbsent` + zero-call/no-publication oracle | `E-P02-TRAVEL` |
| Non-self co-located zero 허용, negative/overflow reject | [Phase 02 §7.2](../../phases/phase-02-prepared-travel-immutable-problem.md#72-absence-semantics) | WP-02.1, WP-02.3 | `preservesValidNonSelfZeroDistanceAndTimeForCoLocatedLocations`, `rejectsNegativeProvidedDistanceOrTime` | `E-P02-TRAVEL` |
| Missing `U` vehicle `CEILING`, missing-only 45 | `Q-MTX-02~03`, [Integrated §6.2](../../../architecture-domain-implementation-design.md#62-complete-preparation) | WP-02.3 | Generated time/default/invalid speed | `E-P02-TRAVEL` |
| Complete `M²`/used-vehicle time | [Plan Phase 02](../../master-realization-plan.md#phase-02--이동-자료-준비와-immutable-problem) | WP-02.3 | Cartesian coverage property | `E-P02-TRAVEL` |
| No lazy/reverse/symmetric/provider fallback | Master §8, [Integrated §6.3](../../../architecture-domain-implementation-design.md#63-runtime-prohibition와-provenance) | WP-02.2~3, WP-02.6 | Fault spies + architecture symbols | `E-P02-TRAVEL`, architecture report |
| Phase 01 artifact only | [Phase 01 §6.2](../../phases/phase-01-canonical-input-normalization.md#62-산출물) | WP-02.2 | `TravelSourceHandoffTest` | `E-P02-TRAVEL` |
| `Absent`만 generation 대상 | [Phase 02 §7.2](../../phases/phase-02-prepared-travel-immutable-problem.md#72-absence-semantics) | WP-02.2~3 | Invalid/upstream failure no-fallback | `E-P02-TRAVEL` |
| Dense identity/bijection | [Domain §7](../../../2026-07-26-domain-design.md#7-immutable-solver-model) | WP-02.1, WP-02.5 | Bijection/permutation/index overflow | `E-P02-DENSE-ID` |
| Delivery-only logical pickup은 pair ownership만, physical node/location/travel/visit/zone-resource 0; delivery와 real pickup zone fact 보존 | [Master §5.2](../../../master-design.md#52-service-meaning), [Master §5.3](../../../master-design.md#53-vehicle-size와-capability), [Domain §2.3](../../../2026-07-26-domain-design.md#23-delivery-only와-real-pickup-delivery), [Phase 01 §7.2](../../phases/phase-01-canonical-input-normalization.md#72-canonical-request와-service-pattern), [Integrated §6.4](../../../architecture-domain-implementation-design.md#64-dense-identity와-immutable-problem) | WP-02.5 | `keepsLogicalInitialLoadOutOfPhysicalAndZoneResourceSets` + `M/M²`/call-count oracle + delivery-only count delta의 independent zone-set oracle | `E-P02-DENSE-ID`, `E-P02-PROBLEM` |
| Phase 01 unit-bearing demand/capacity 보존, Phase 02 renormalization 0 | [Master §7.2](../../../master-design.md#72-fixed-point와-checked-arithmetic), [Phase 01 §7.3](../../phases/phase-01-canonical-input-normalization.md#73-numeric-value), [Architecture §2.4](../../../2026-07-26-architecture-design.md#24-먼저-알아야-할-immutable-artifact) | WP-02.1, WP-02.5, WP-02.6 | `preservesPhase01UnitBearingDemandValuesWithoutRenormalization`, `problemBoundaryUsesDistinctWeightAndVolumeValueTypesInsteadOfRawLongs` | `E-P02-DENSE-ID`, `E-P02-PROBLEM`, architecture report |
| Immutable problem/reference bind | Domain §7, [Integrated §6.4](../../../architecture-domain-implementation-design.md#64-dense-identity와-immutable-problem) | WP-02.5 | Problem/reference/corruption tests | `E-P02-PROBLEM` |
| No mutable alias | [Master §4.5](../../../master-design.md#45-상태와-산출물의-생명주기) | WP-02.1, WP-02.4~5 | Constructor/accessor mutation probes | `E-P02-PROBLEM` |
| Stable authority fingerprint | Master §8·§13, Domain §17.4 | WP-02.4 | Repeat/permutation/cache/mismatch | `E-P02-TRAVEL`, `E-P02-PROBLEM` |
| Fault/cancel all-or-nothing | [Plan §8](../../master-realization-plan.md#8-공통-테스트-전략) | WP-02.3~4 | Fail-after-N/cancel/overflow | `E-P02-TRAVEL` |
| Corruption rejection | [Integrated §22.4](../../../architecture-domain-implementation-design.md#224-independent-corruption-fixtures) | WP-02.4~5 | One-field cell/source/mapping/digest corruption | 두 travel/problem evidence |
| Reproducibility | [Plan §13](../../master-realization-plan.md#13-위험-보안-운영-관측과-재현성) | WP-02.4, WP-02.6 | Same bytes/fingerprint, provider metadata variance | 세 evidence key |
| Security/redaction | [Integrated §20](../../../architecture-domain-implementation-design.md#20-security와-tenant-boundary) | WP-02.2, WP-02.4 | Canary secret/PII/report test | Security report |
| Safe observability | [Integrated §19.3](../../../architecture-domain-implementation-design.md#193-correlation-fields) | WP-02.4 | Count vs artifact independent check | `E-P02-TRAVEL` |
| Phase 03 exact handoff | [Phase 03 §4](../../phases/phase-03-route-propagation-evaluation-kernel.md#4-entry-gate와-확인-방법) | WP-02.5~6 | Fingerprint/mapping/coverage consumer contract | `E-P02-PROBLEM` |
| Reactor command/fixture edge와 exact method execution | [Architecture §19.1](../../../architecture-design.md#191-reactor-build-order), [Plan §8.2](../../master-realization-plan.md#82-필수-test-종류), [Plan §9.1](../../master-realization-plan.md#91-pre-review-evidence-manifest) | WP-02.0~6 | `-am clean`, cycle rule, core53 explicit filter, architecture/root full58, freshness·zero/failure/skipped/missing/duplicate와 immutable run checker | 세 evidence key + architecture report |
| Phase 13/14 gate 비우회 | [Implementation README §6](../../README.md#6-phase-작업-순서), [Plan §14](../../master-realization-plan.md#14-open-gated-deferred와-restart-condition) | WP-02.0, WP-02.6 | Hidden-default/vendor-dependency scan | Gate snapshot |

## 16. 구현자가 마지막으로 스스로 묻는 질문

1. 내가 읽는 travel key는 node인가 physical location인가?
2. 이 missing은 schema가 선언한 `Absent`인가, invalid/upstream failure인가?
3. Provided 값을 generator가 덮어쓸 경로가 정말 0개인가?
4. `A→B`와 `B→A`가 독립 test에서 다르게 유지되는가?
5. Missing coordinate가 typed reject되고 valid non-self zero는 그대로 보존되는가?
6. Delivery-only logical pickup이 physical node/location/travel/visit/zone-resource를 만들지 않고 delivery/real-pickup의 정상 zone fact는 보존하는가?
7. Weight와 volume이 Phase 01 unit type을 유지하며 Phase 02가 다시 반올림하지 않는가?
8. Speed 45가 missing이 아닌 오류에도 적용되고 있지 않은가?
9. 실패·취소·overflow 뒤 partial artifact/cache write가 0인가?
10. Mutable constructor input과 returned view를 바꿔도 artifact가 그대로인가?
11. Fingerprint가 value, mapping, source, policy를 충분히 bind하고 volatile provider metadata는 제외하는가?
12. Phase 03와 Phase 07가 같은 prepared authority를 증명할 수 있는가?
13. HEAD baseline, 미커밋 live reactor, accepted Phase 00 receipt를 서로 다른 상태로 보고 있는가?
14. Root/legacy test, FLOOR fixture, 문서 review를 실제 Phase 02 acceptance로 오인하지 않았는가?
15. Great Circle, performance threshold, public API 또는 official authority를 임의 값으로 닫지 않았는가?
16. Phase 13 optional gate와 Phase 14 official/production gate를 우회하는 dependency나 표현을 추가하지 않았는가?

이 질문에 evidence reference로 답할 수 없으면 아직 handoff할 때가 아니다.
