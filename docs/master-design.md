# RPDPTW 통합 솔버 Master Design

```yaml
status: REVIEW
version: 2.1-review
last_updated: 2026-07-23
owner: RPDPTW 설계 책임 역할
scope: 향후 RPDPTW 시스템 구현의 규범적 구조, 불변조건, 의존 순서와 검증 gate
supersedes: 이 파일의 기존 legacy 통합 초안
related_decisions:
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
```

## 1. 문서 상태와 규범

이 문서는 **향후 무엇을 어떤 경계와 순서로 구현하고 검증할지** 정하는 개발 설계다. 현재 코드, 테스트, 배포 또는 benchmark가 이 설계를 구현했다고 주장하지 않는다. 상태는 `REVIEW`이며, 열린 질문과 잠정 설계가 승인되기 전에는 `APPROVED` 기준으로 인용할 수 없다.

이 문서에서 규범어는 다음 의미다.

| 표기 | 의미 |
|---|---|
| **MUST / MUST NOT** | 구현과 검증이 반드시 지켜야 하는 계약 또는 불변조건 |
| **SHOULD / SHOULD NOT** | 특별한 근거와 검토가 있을 때만 이탈할 수 있는 잠정 또는 권장 방향 |
| **MAY** | 계약을 깨지 않는 선택 또는 확장점 |
| **TBD** | 중앙 질문의 결정 없이는 의미를 확정할 수 없음 |
| **DEFERRED** | 재개 조건과 별도 승인 전에는 현재 구현 범위가 아님 |

결정 상태는 다음처럼 해석한다.

- **확정**: `C-*` 결정과 그 결정에 필수인 계약이다.
- **질문 결정**: 중앙 [Master Design 질문 등록부](master-design-open-questions.md)의 `RESOLVED` 질문과 [세션 29 인터뷰](master-design-sessions/29-open-question-interview.md)에 기록된 사용자 답변이다.
- **잠정**: 사용자 결정으로 대체되지 않은 `P-*` 방향이다. 책임 경계는 검토 입력이지만 이름, API, 기본값 또는 활성 범위는 확정되지 않았다.
- **실험 대기**: 등록부의 `OPEN — EXPERIMENT_REQUIRED` 질문이다. 프로토콜은 결정되었지만 실제 공식 수치는 승인된 실험 결과 전까지 만들 수 없다.
- **보류**: 질문 등록부에서 `DEFERRED`로 관리하며 현재 roadmap을 막지 않는다.

현재 `REVIEW` 단계에서 이 문서는 conflict resolver가 아니라 **검토 제안**이다. 승인된 기준과 충돌할 때 이 문서의 문장만으로 그 기준을 대체할 수 없으며, 충돌은 review finding, 중앙 질문 또는 Decision Record의 incorporation/approval 절차로 해결해야 한다.

이 문서가 `APPROVED`가 된 뒤 같은 주제에 충돌이 있으면 다음 순서로 판단한다.

1. 채택된 외부 입력·출력 계약과 승인된 Decision Record
2. `APPROVED` 상태의 이 Master
3. `APPROVED` 상태의 상세 설계
4. review input과 `master-design-sessions` 문서
5. 연구·원본 자료

최신 사용자 결정은 문서와 Decision Record에 반영되기 전에도 적용 범위를 명시한 override다. 다만 그 결정은 관련 문서 전체를 자동 승인하지 않는다. 영향 범위를 식별하고, 필요한 질문·Decision Record와 Master/상세 문서에 반영한 뒤 review와 approval 절차를 거쳐 장기적인 이중 기준을 제거해야 한다. 예시와 논문 설명은 어느 상태에서도 규범이 아니다.

## 2. 목표, 범위와 성공 기준

### 2.1 목표

시스템은 **RPDPTW (Rich Pickup and Delivery Problem with Time Windows)** 문제를 정규화하고, 실행 가능한 해를 생성·개선·검증하여 추적 가능한 결과로 발행해야 한다.

가장 중요한 아키텍처 목표는 고객사마다 다른 목적과 제약을 **최소 코어 변경**으로 조립하는 것이다. 새 고객사의 단가, 제약, 중립 지표 또는 목적 순서가 공통 경로 상태나 ALNS에 고객사 분기를 추가하게 해서는 안 된다. 실제 물리 진행 의미가 기존 경계로 표현되지 않을 때만 좁고 검증 가능한 공통 seam을 확장한다.

### 2.2 현재 설계 범위

현재 roadmap은 다음을 포함한다.

- versioned adapter와 불변 정규화 문제
- 외부 sparse/omitted travel input을 solver 시작 전에 해소하는 명시적 Travel Matrix preparation
- request pair, vehicle, terminal, physical location, directed matrix와 hard feasibility
- 고객사별 constraint, 중립 metric, score, objective, `SolvePlan`과 profile binding
- 네 정책의 초기해 portfolio와 공통 atomic pair evaluator
- request-pair 기반 ALNS, bounded improvement와 fleet attempt
- copy-on-write 후보 상태, cache 무효화와 rollback
- step 기반 정상 종료, 안전 watchdog, cancellation/resource/failure 분리
- 검색 상태와 최종 결과의 분리, 구조화된 진단
- 서로 분리되고 cache-free인 candidate solution verifier와 post-finalization result-integrity verifier
- `data/win_poc_case.json` 기반 Win PoC benchmark 계약
- 고정 manifest의 round/worker fan-out·champion fan-in을 반복하는 논리 benchmark 실행 계약
- provider와 product에 독립적인 논리 실행 port와 migration gate

### 2.3 명시적 비범위

다음은 현재 구현 범위가 아니다.

- route pool과 set-covering/set-partitioning MIP 후처리
- MDVRP, OVRP, SDVRP 등 선택 변형의 구현
- 구체 cloud, provider, product, runtime, 실행·저장 service 또는 배포 단위
- 실시간 동적 routing, 교통정보 결합, geocoding과 주소 정제
- 승인되지 않은 multi-trip/rotation 의미
- 공식 숫자가 정해지지 않은 scorer, randomized start, diverse `K`, light-search budget, benchmark round/worker 수, `maxSteps`, watchdog과 성능 threshold
- 승인된 compliant integer `D/U`를 다시 받기 전 현재 Win PoC fixture의 official baseline 사용

### 2.4 성공 기준

향후 구현은 다음 조건을 모두 증거로 입증해야 한다.

1. 모든 안정 해가 request pair와 route/bank 불변조건을 만족한다.
2. 입력 의미와 정책은 version과 provenance가 고정된 snapshot으로 bind된다.
3. 고객 profile 교체가 공통 hard feasibility와 ALNS core 의미를 바꾸지 않는다.
4. cached/incremental 평가가 cache-free 전체 재계산과 정확히 같다.
5. Candidate solution verifier와 post-finalization result-integrity verifier를 모두 통과한 결과만 정상 publication과 benchmark 비교에 사용된다.
6. 고정된 재현성 envelope의 정상 step 종료가 같은 해와 결과 fingerprint를 만든다.
7. Win PoC baseline과 challenger는 같은 manifest의 모든 선언 round/worker가 정상 완료·검증된 실제 champion으로만 비교된다.
8. 각 roadmap phase는 산출물 존재가 아니라 명시된 검증 evidence로 완료된다.

## 3. 용어와 결정 요약

### 3.1 표준 용어

| 용어 | 규범 의미 |
|---|---|
| RPDPTW | 프로젝트의 표준 Rich Pickup and Delivery Problem with Time Windows 모델 |
| PDPTW | 논문과 학술 문제를 가리키는 원래 문제명 |
| `Request` | pickup 하나와 delivery 하나를 갖는 원자적 운송 의무 |
| `Feature` | 지역의 대형 차량 진입 제한에 쓰는 차량 크기 유형. 예: 1t, 3t, 5t |
| capability / qualification | 냉장, lift, 위험물 자격, 기사 자격 등 `Feature`와 분리된 확장 constraint 축 |
| hard feasibility | 위반 후보를 허용 가능한 해로 인정하지 않는 규칙 |
| neutral fact / metric | 발생한 물리·구조 사실이며 가격이나 선호를 포함하지 않음 |
| score policy | feasible 사실을 비용 또는 soft penalty로 변환하는 정책 |
| objective comparator | feasible 해의 ordered dimension을 의미적으로 비교하는 계약 |
| `SolvePlan` | stage 순서, warm-start, 선행 목표 보호와 budget reference를 조정하는 상위 계약 |
| `SearchRequestBank` | 탐색 중 정규 route에 없는 request ID membership |
| final outcome | 검증된 최종 request의 `ASSIGNED` 또는 `UNASSIGNED` partition |

### 3.2 확정 결정 색인

| ID | Master에 통합된 의미 |
|---|---|
| `C-01` | 표준 프로젝트 용어는 RPDPTW이며 학술 PDPTW와 구분한다. |
| `C-02` | Master는 구현 완료 보고가 아닌 미래 개발의 규범 설계다. |
| `C-03` | 고객사 변화에 대한 유지보수성과 최소 코어 변경이 최우선이다. |
| `C-04` | hard feasibility, 중립 측정, score, objective 비교와 stage 실행을 분리한다. |
| `C-05` | `Feature`는 vehicle size type이고 capability/qualification은 별도 축이다. |
| `C-06` | request는 same-vehicle, exactly-once, precedence, route/bank XOR를 갖는 atomic pair다. |
| `C-07` | delivery-only 출발 적재와 실제 pickup-delivery의 물리 의미를 구분한다. |
| `C-08` | 정상 품질 종료는 step limit이고 시간은 watchdog이다. |
| `C-09` | 현재 기본 candidate state는 changed-route copy-on-write와 독립 bank다. apply/undo는 측정된 COW 병목과 별도 변경 승인 전에는 기본 경로가 아니다. |
| `C-10` | 소수 물리량은 변환 경계에서 fixed-point integer로 정규화한다. |
| `C-11` | 무게·부피는 `n=3`, 비음수 `FLOOR`, item-first 정규화 후 `qty` 곱을 사용한다. 비용·거리·시간은 정수 입력이며 소수 입력을 거부한다. |
| `C-12` | 기본 volume은 999 CBM, 시간의 유한 상한은 plan end이며 실패는 별도 상태다. |
| `C-13` | 제공된 `D` meter/`U` second를 우선하고, 명시적 preparation이 누락 `D/U`를 승인 산식으로 생성한 뒤 solver/verifier에 complete travel data를 전달한다. |
| `C-14` | `ro_input_json_spec.pdf`는 legacy CVRPTW 사례이지 canonical RPDPTW schema가 아니다. |
| `C-15` | search bank의 membership과 final `ASSIGNED`/`UNASSIGNED` outcome·diagnostic을 분리한다. 차량의 직영/외주 소유 유형은 outcome status가 아니다. |
| `C-16` | 네 정책의 initial-solution portfolio에서 comparator상 best를 항상 warm-start에 포함하고, 실험으로 정할 검증된 diverse 후보를 추가할 수 있다. |
| `C-17` | route pool/MIP는 deferred다. |
| `C-18` | Win PoC 전용 네 성분 comparator는 미배정 수, 배차 차량 수, 전체 거리, 전체 운영시간 순이며, official run은 고정 round plan의 완결된 verified champion을 사용한다. |
| `C-19` | 선택 변형 문제는 구현이 아니라 deferred feasibility work다. |
| `C-20` | 구체 infrastructure/product/deployment topology는 deferred이고 Master는 논리 port만 소유한다. |
| `C-21` | 독립 verifier를 통과하지 않은 후보는 정상 publication 또는 benchmark 대상이 아니다. |
| `C-22` | 강한 재현성은 고정 fingerprint/seed/order/step의 정상 `MAX_STEPS_REACHED` 실행에 한정한다. |

### 3.3 잠정 설계 색인

아래 항목은 확정 결정으로 승격하지 않는다.

| ID | 잠정 방향 | 아직 확정하지 않는 것 |
|---|---|---|
| `P-01` | delivery-only와 real pickup-delivery의 service pattern을 구분 | 최종 타입명, prefix node 대 initial-load 표현 |
| `P-02` | delivery-only v1의 virtual pickup을 route 시작 prefix에 고정 | depot 작업시간 단위, 외부 노출, mixed-route 규칙 |
| `P-03` | preparation 완료 후 complete travel data를 표현하는 내부 구조 | 공통 `U`와 차량별 생성 `U`의 최종 타입/API 및 저장 표현 |
| `P-04` | `SolvePlan`은 orchestration, objective schema/score는 평가 vector | 최종 타입명과 별도 objective-plan 타입 필요 여부 |
| `P-05` | 장기 profile registry/factory와 solve별 immutable binding 분리 | 정확한 factory API, lifecycle 구현과 등록 방식 |
| `P-06` | propagation facts와 metric contributor/snapshot seam | 최종 표현과 공통 metric 승격 목록 |
| `P-07` | 선행 objective를 기본적으로 no-worse-than-best로 보호 | tolerance와 탐색 중 relaxation |
| `P-08` | **대체됨:** solver outcome은 `ASSIGNED`/`UNASSIGNED`이고 직영/외주는 vehicle ownership으로 구분 | 운영자의 후속 외주·이월은 solver result status가 아님 |
| `P-09` | diagnostic을 code, scope, confidence, source, evidence로 구조화 | 공개 code, 확장 code 노출과 audit 범위 |
| `P-10` | comparator상 best를 반드시 handoff하고 검증된 diverse 후보를 추가 | scorer, randomized start, `K`, light-search budget은 `Q-ALG-01` 실험 대기 |
| `P-11` | **기본 경로 아님:** 측정된 COW 병목이 있을 때만 apply/undo 재제안 가능 | 별도 실험·동등성 증거·변경 승인 없는 전환 금지 |
| `P-12` | versioned legacy adapter의 제한된 coercion과 alias 허용 | 허용 목록, canonical JSON과 unknown-field 정책 |
| `P-13` | benchmark manifest/card에 fixture, round/worker plan과 전 계약 fingerprint 보존 | round/worker 수, `maxSteps`, watchdog은 `Q-BENCH-02` 실험 대기 |
| `P-14` | 실행 경계를 논리 port로 분리 | transport, orchestrator, artifact store와 배포 단위 |

## 4. 논리 시스템 context와 책임 경계

### 4.1 End-to-end 책임 흐름

```text
submission/input
    ↓
normalization + profile binding
    ↓
portfolio + solve execution
    ↓
committed candidate
    ↓
candidate solution verifier
    ↓
preliminary request partition
    ↓
required final-solution insertion audit
    ↓
final request outcomes/diagnostics
    ↓
post-finalization result-integrity verifier
    ↓
status/artifact publication
    ↓
verified result retrieval
```

Cancellation은 실행 중 협력 signal로 전달되고 미완료 mutation의 rollback을 요구한다. 상태와 artifact lineage는 전 흐름에 걸쳐 input, profile, run, candidate, verification과 result identity를 연결한다.

### 4.2 논리 port

| Port | MUST 책임지는 것 | MUST NOT 결정하는 것 |
|---|---|---|
| submission/input | 접수 identity, idempotency key 의미, input lineage, 선택한 exact profile/config 전달 | raw field의 업무 의미, physical transport |
| normalization/binding | versioned adapter, numeric/time/matrix 정책과 profile dependency를 검증·동결 | algorithm 또는 결과 정책 |
| solve execution | portfolio, stage, seed, step budget, committed candidate와 termination provenance | external result status와 storage topology |
| finalization | candidate solution verifier 호출, 그 `PASS` report와 verified solution에서 preliminary partition 생성, 필요한 전수 삽입 audit 수행, final outcomes/diagnostics 생성, post-finalization result-integrity verifier 호출 | search bank에 final meaning 쓰기; audit가 찾은 feasible insertion을 자동 적용하거나 재탐색 loop를 시작하기; search cache나 solver summary를 두 verifier의 권위 입력으로 사용하기 |
| status/artifact | 두 verifier의 `PASS`, 상태 전이, immutable identity/fingerprint와 retention hook | verifier 하나라도 실패·미완료인 route/outcome 또는 benchmark payload 발행; storage product와 path 규칙 |
| result retrieval | verified result와 incomplete/invalid/recovery 의미 구분 | 미검증 candidate의 정상 공개 |
| cancellation | 의도 전달, 협력 종료, rollback 요구와 실제 종료 상태 보존 | watchdog 또는 failure의 이름 바꾸기 |

Finalization의 정확한 순서는 `committed candidate → candidate solution verifier → preliminary partition → required final-solution insertion audit → final outcomes/diagnostics → post-finalization result-integrity verifier → publication`이다. 정적 precheck로 이미 불가능성이 증명된 request를 제외한 모든 `UNASSIGNED` request를 audit한다. 첫 verifier gate는 candidate solution을, 둘째 verifier gate는 finalization이 만든 결과를 검증하며 어느 gate의 실패나 미완료도 publication을 막아야 한다.

의존 방향은 `external adapter → application orchestration → domain/evaluation/algorithm`이어야 한다. Core는 transport, storage, 인증 또는 runtime 타입을 참조해서는 안 된다. Adapter는 표현을 바꿀 수 있지만 core invariant, matrix 의미, comparator 또는 termination 의미를 바꿀 수 없다.

이 논리 경계는 cloud/product-neutral해야 한다. 물리 구성은 논리 책임을 구현할 뿐 core 의미를 역으로 바꿀 수 없다.

물리 topology는 [Q-INFRA-01](master-design-open-questions.md#q-infra-01)이 `DEFERRED`인 동안 결정하지 않는다. 논리 port의 설계와 test double은 진행할 수 있지만 특정 service나 배포 구성을 목표 설계로 고정할 수 없다.

## 5. Canonical RPDPTW 도메인 모델

### 5.1 핵심 개념

| 개념 | 규범 의미 |
|---|---|
| `Request` | immutable pickup node와 delivery node를 참조하는 atomic pair |
| pickup node | 물량이 차량 적재에 들어오는 논리 작업 |
| delivery node | 같은 물량이 차량 적재에서 빠지는 논리 작업 |
| vehicle | 용량, 하나의 vehicle size type, `vhclOwnTyp`, terminal 정책, 근무와 route-resource 데이터를 가진 자원 |
| terminal | route의 고정 start/end solver node |
| physical location | directed matrix endpoint. solver node와 별도 identity/index를 가짐 |
| route | 한 vehicle에 결합된 single-trip ordered sequence. `oneway`는 start terminal에서 마지막 고객까지, `roundtrip`은 start terminal에서 같은 depot 복귀까지다. |
| solution | 안정 route 집합과 독립 `SearchRequestBank`의 request partition |

외부 ID와 조밀한 core ID의 양방향 mapping은 정규화 결과에 포함하고 problem 생성 후 바꾸지 않는다. 여러 solver node가 같은 physical location을 참조할 수 있으며, 같은 위치라도 pickup, delivery와 terminal 역할은 합치지 않는다.

### 5.2 Service meaning

하나의 RPDPTW core를 사용하되 다음 의미를 보존해야 한다.

| Pattern 의미 | pickup | 규범 조건 |
|---|---|---|
| delivery-only | route 출발 전에 준비된 화물의 논리적 start loading | 같은 route의 모든 delivery-only demand 합이 initial load이며 첫 고객 뒤의 중간 재적재로 용량을 우회할 수 없음 |
| real pickup-delivery | 입력에 존재하는 실제 pickup 작업 | 같은 route에서 delivery보다 먼저 실제 위치·시간·service를 수행 |

Delivery-only와 real pickup-delivery는 같은 single-trip route에 섞을 수 있다. 시작 load는 모든 delivery-only demand의 합이고 delivery-only 고객에서 감소한다. 실제 pickup에서는 증가하고 그 delivery에서 감소한다. 모든 route prefix에서 각 load 차원이 `0 <= load <= capacity`를 만족해야 하며, delivery-only 논리 pickup은 travel, stop 또는 depot 재방문을 만들지 않는다.

Delivery-only의 prefix node 대 initial-load 내부 표현과 이름은 잠정이지만 어느 표현도 위 물리 의미를 바꿀 수 없다. `oneway`와 depot으로 한 번 복귀하는 `roundtrip + multiRotation=0`은 현재 single-trip 범위다. 같은 차량이 depot 복귀 뒤 재출발하는 multi-trip/rotation은 후속 범위이며, 활성화하더라도 pickup-delivery pair는 하나의 trip 안에서 완료해야 한다.

### 5.3 Vehicle size와 capability

입력의 `Feature`는 vehicle size type이다. Vehicle은 구체 코드 하나를 `vehicleFeature`로 받고, order는 허용 코드 배열을 `vehicleFeatureList`로 받는다.

```text
sizeCompatible
= vehicle.vehicleSizeType
  IN request.allowedVehicleSizeTypes
```

Request 측 size 값은 허용 대안의 집합이고 vehicle 측은 단일 유형이다. 코드는 비어 있지 않은 자유 형식 문자열이고 대소문자를 구분한 exact equality로 비교한다. 고정 allowlist를 두거나 문자열의 숫자·순서에서 톤급 관계를 추론하지 않는다. Order의 정확한 `["ALL"]`은 모든 vehicle을 허용하며 구체 코드와 섞을 수 없다.

일반 capability/qualification은 별도 subset 축이다.

```text
capabilityCompatible
= request.requiredCapabilities
  SUBSET OF vehicle.vehicleCapabilities
```

전용 차량과 zone도 별도 hard constraint다. Missing/null/empty zone은 `"ALL"`로 정규화한다. Route가 방문하는 `zoneId != "ALL"` 집합의 cardinality는 최대 1이어야 한다. 구체 zone vehicle은 같은 zone과 `ALL` order만, zone-neutral vehicle은 한 route에서 하나의 구체 zone과 `ALL` order만 방문할 수 있다. Size와 zone은 AND로 적용한다. 실제 pickup/delivery의 size 목록은 교집합을 사용하고 두 실제 작업의 구체 zone이 다르면 배정할 수 없다. Delivery-only의 depot 논리 pickup은 zone 방문이 아니다.

Vehicle ownership은 optional `vhclOwnTyp`으로 표현한다. Missing/null/empty는 `DIRECT`, 허용값은 대소문자를 구분한 `DIRECT`와 `LEASE`뿐이다. 이 축은 request assignment status와 분리한다.

## 6. 핵심 불변조건과 atomic mutation

### 6.1 Request partition

모든 안정 상태에서 각 request는 정확히 하나의 상태여야 한다.

```text
ASSIGNED_IN_SEARCH
= pickup과 delivery가 같은 vehicle route에 각각 정확히 한 번 존재
  AND pickup index < delivery index
  AND request가 SearchRequestBank에 없음

UNASSIGNED_IN_SEARCH
= pickup과 delivery가 모든 route에 없음
  AND request가 SearchRequestBank에 정확히 한 번 존재
```

두 상태는 XOR이다. 다음은 구현 결함이며 정상적인 infeasible 후보가 아니다.

- pickup 또는 delivery만 존재하는 partial pair
- 한 node 또는 pair의 중복
- pair가 서로 다른 vehicle route에 분리됨
- delivery가 pickup보다 앞섬
- route와 bank 동시 membership
- route와 bank 양쪽에서 누락

### 6.2 Route와 feasibility

각 안정 route는 다음을 MUST 만족한다.

1. 정확히 하나의 vehicle에 결합한다.
2. 첫 node는 그 vehicle의 start terminal이다. `oneway`의 마지막 node는 마지막 service node이고, `roundtrip`의 마지막 node는 같은 depot/end terminal이다.
3. 현재 single-trip에서는 내부 depot 재방문과 다른 vehicle의 terminal을 허용하지 않는다.
4. 승인된 service pattern을 보존한다.
5. 모든 load 차원에서 `0 <= load <= capacity`다.
6. 실제 directed matrix, 승인된 time contract와 route-resource rule로 hard-feasible하다.
7. 표준 single-trip의 완성 route는 request pair의 순변화가 0이어야 한다.
8. 고객 service location이 직전 service location과 다를 때만 stop count를 1 증가시키며 depot은 세지 않는다. `maxStopCnt`는 route 전체 누적이고 vehicle/global 한도가 모두 있으면 `min`을 포함 상한으로 사용한다.
9. 실제 통과 arc의 `D`와 vehicle-resolved `U`만 route 전체 `driveDist`/`driveTime`에 누적하며 대기·서비스·근무창 사이 휴식은 제외한다. 명시된 한도가 없으면 해당 추가 hard constraint가 없다.

### 6.3 Atomic mutation

Insert, remove, relocate, exchange, destroy/repair, route elimination과 rollback은 request pair를 최소 전이 단위로 사용해야 한다.

- 성공하면 pickup, delivery, route와 bank를 함께 갱신하고 영향받은 cache/aggregate/fingerprint를 무효화하거나 재계산한다.
- 실패, 거절, 중단 또는 예외면 route sequence, bank, fleet state, cache visibility, score/objective aggregate와 fingerprint가 호출 전과 같아야 한다.
- Evaluator, comparator, observer, best snapshot과 result conversion은 안정 상태만 볼 수 있다.
- 구조 불변조건 실패를 낮은 score, insertion infeasibility 또는 unassignment reason으로 숨겨서는 안 된다.

## 7. 입력, 정규화와 domain value 계약

### 7.1 Adapter 경계

외부 입력을 core 타입으로 직접 역직렬화하지 않는다.

```text
external bytes/reference
→ schema/version adapter
→ syntax/reference validation
→ business meaning
→ canonical business model
→ numeric/time/location/compatibility normalization
→ immutable ProblemInstance + provenance
```

Versioned legacy adapter는 승인된 제한적 coercion/alias만 처리할 수 있다. 적용한 해석을 provenance에 기록하고, 모호하거나 지원되지 않는 의미는 solve 전에 거부한다.

[`data/ro_input_json_spec.pdf`](../data/ro_input_json_spec.pdf)는 legacy CVRPTW 입력 사례다. 표와 예시가 충돌하고 RPDPTW의 matrix·pair 계약을 제공하지 않으므로 canonical schema로 복제해서는 안 된다. [`data/win_poc_case.json`](../data/win_poc_case.json)도 read-only fixture이지 canonical schema나 정답 해가 아니다.

### 7.2 Fixed-point와 checked arithmetic

소수 물리량은 변환 경계에서 정확한 10진수로 읽어 fixed-point integer로 정규화한다.

- 무게와 부피는 `n=3`이며 비음수 원문 값을 소수 셋째 자리까지 `FLOOR`한다.
- 각 item을 먼저 정규화한 뒤 그 정수값에 양의 정수 `qty`를 곱한다. Decimal line 합계를 먼저 만든 뒤 한 번만 내리지 않는다.
- 비용, 거리와 시간은 `n=0`의 정수 입력 계약이다. 소수 입력을 절삭·반올림하지 않고 입력 오류로 거부한다.
- Core feasibility와 누적에는 floating tolerance를 사용하지 않는다.
- 같은 차원의 demand와 capacity, travel과 resource limit는 같은 unit/scale/rounding policy를 사용한다.
- 정규화, quantity 곱, route/solution 합산은 checked arithmetic을 사용한다.
- Overflow, 비유한 값 또는 계산 실패를 큰 numeric value로 치환하지 않는다.
- Policy ID/version, 단위, scale, rounding과 adapter version을 fingerprint에 포함한다.

### 7.3 Planning period와 time

Frontend/backend는 timezone과 UTC offset 변환을 solver 밖에서 처리한다. Backend는 solver에 timezone/offset 없는 exact `yyyy-MM-dd HH:mm:ss` 문자열을 전달한다. Solver adapter는 이를 parsing하여 planning origin 기준 `long` second로 바꾸고, core는 timezone, DST와 문자열을 다루지 않는다. Solver에 전달되는 모든 planning/window 값은 이미 같은 전역 고정 시간 기준으로 정렬되어 있어야 한다. 그 전역 timezone의 이름은 solver default나 fingerprint 값으로 추정하지 않는다.

정규화된 problem은 정확한 plan start와 plan end를 가져야 한다. Planning period는 `planStart <= t < planEnd`인 반개구간이다. Node, terminal과 vehicle window는 같은 단조 `long` second 축을 사용한다.

- Plan end는 유한한 허용 상한이지 계산 실패 표현이 아니다.
- Plan end와 같은 event는 현재 plan에 포함하지 않는다.
- Time-window close는 포함 경계다.
- Feasible time 값과 `INFEASIBLE(reason)` 상태는 논리적으로 분리한다.
- 계산 실패에 numeric sentinel을 사용하거나 초과값을 plan end로 clamp하지 않는다.

고객 `openTime/closeTime`은 실제 영업시간이다. `serviceStart = max(arrival, openTime)`, `waitingTime = serviceStart - arrival`, `serviceEnd = departure = serviceStart + serviceTime`으로 계산한다. Waiting time은 중립 metric이다. 기본 `START_ONLY` profile은 `serviceStart <= closeTime`을 요구하므로 close와 같은 service start와 close 이후 departure를 허용한다. Profile은 `COMPLETE_WITHIN_WINDOW`를 선택하여 `serviceEnd <= closeTime`을 요구할 수 있다.

날짜 없는 고객/depot/vehicle window는 plan에 포함되는 각 날짜에 반복한다. `openTime > closeTime`은 당일 open부터 다음 날 close까지의 하나의 overnight window다. 전개 결과는 `[planStart, planEnd)`로 clip한다. 이동은 `departure + fullTravelTime <= currentWorkEnd`일 때만 시작한다. 현재 근무창에 전체 arc가 들어가지 않지만 plan 안에 다음 반복 근무창이 있으면 현재 위치에서 쉬고 다음 `workStart`에 전체 arc를 처음부터 시작한다. Arc 중간 pause/resume은 금지한다. 전체 arc가 어떤 단일 이용 가능 근무창에도 들어가지 않으면 infeasible이다.

Legacy `reqDate`와 `dueDate`는 같은 완료기한 별칭이며 `serviceStart <= serviceEnd <= reqDate(dueDate)`를 만족해야 한다. `duration`은 order/request 수준 진입 등 고정 서비스시간이고, `item.taskTime`은 item 한 단위 선적시간이다.

```text
serviceTime
= request.duration
  + Σ(item.taskTime × item.qty)
```

Order-level `taskTime`은 item에 배분하거나 `duration` 별칭으로 바꾸지 않고 입력 오류로 거부한다. Depot `taskTime`은 적용하지 않는다. Depot `duration`은 후속 rotation에서 한 trip 복귀 뒤 다음 trip 재출발 전에는 적용하지만 최초 출발과 마지막 복귀에는 적용하지 않는다.

`trips=oneway`이면 `multiRotation` 값은 비권위 입력으로 무시하고 depot에서 한 번 출발해 마지막 고객에서 끝난다. 그 외 `multiRotation != 0`은 rotation 기능 승인 전 `UNSUPPORTED_INPUT`이다. `roundtrip + multiRotation=0`은 같은 depot으로 한 번 복귀하는 현재 single-trip이다.

`waitInDepot=N`은 가능한 가장 이른 `max(vehicleWorkStart, depotOpen)` 출발을 사용한다. `waitInDepot=Y`는 `max(earliestDeparture, firstCustomerOpen - travelTime)`으로 동일한 조기 대기를 depot으로 옮긴다. Depot/customer waiting은 별도 metric이며 이 출발 정책은 feasibility를 완화하지 않는다.

`maxStopCnt`, `maxDriveTime`, `maxDriveDist`는 route 전체 누적이고 날짜·근무창 사이 휴식에서 reset하지 않는다. 차량별 stop 한도와 전역 한도가 함께 있으면 둘 다 hard constraint이며 `min`을 사용한다. 누락 한도는 명시적 “제약 없음”이며 큰 numeric sentinel로 바꾸지 않는다.

### 7.4 Constants와 유한 기본값

부피 차원을 사용하지 않는다고 adapter가 명시적으로 정규화한 경우 vehicle volume capacity의 기본값은 **999 CBM**이다.

- 999 CBM은 유한한 값이며 무제한 표현이 아니다.
- 실제 volume demand/capacity가 있으면 덮어쓰거나 clamp하지 않는다.
- 적용 출처와 policy version을 provenance에 남긴다.
- Core는 정규화 뒤 이 값을 일반 유한 capacity로 다룬다.

Tunable은 의미별 configuration이 소유해야 한다. 의미별 static default, solve별 immutable configuration snapshot과 instance value를 분리하고 출처·version을 보존한다. 범용 constants 공간에 고객 가격, plan 값, 탐색 상태 또는 platform 설정을 섞지 않는다.

### 7.5 Static compatibility

Adapter/transformer와 bound route constraint는 다음 규칙으로 `servableVehicles`와 route zone state를 만든다.

- `vehicle.vehicleFeature`는 missing/null/empty/`"ALL"`을 거부하고 구체 자유 형식 코드 하나를 요구한다.
- 신규 order는 하나 이상의 구체 코드 또는 정확히 `["ALL"]`인 `vehicleFeatureList`를 사용한다. Missing/null/empty와 `ALL` 혼합은 거부한다.
- Legacy 배열형 `order.vehicleFeature`는 alias로 읽되 신규 필드와 함께 있으면 exact list equality를 요구한다. Legacy 단일 문자열 order field는 거부한다.
- Size code는 registry에 없어도 유효하며 대소문자를 구분한 exact equality로 비교한다. Fleet에 일치 차량이 없으면 해당 request의 배정 가능 차량이 없는 것이다.
- Size와 zone은 독립 hard constraint로 AND한다. 실제 pickup/delivery size 목록은 교집합을 사용한다.
- Vehicle/order zone의 missing/null/empty는 `"ALL"`로 정규화하고, `ALL`을 제외한 route의 구체 zone 집합은 최대 하나다.
- 실제 pickup과 delivery의 구체 zone이 다르면 배정할 수 없다. Delivery-only의 depot 논리 pickup은 zone 검사에서 제외한다.

## 8. Directed distance/time matrix 계약

외부 production input은 sparse directed arc 또는 matrix 전체 생략을 허용한다. Solver 시작 전의 명시적 Travel Matrix preparation이 모든 physical location의 directed `M²` pair와 모든 사용 vehicle의 travel time을 해소한다.

```text
raw provided/generated travel sources
→ Travel Matrix preparation
→ complete physical-location directed distances
  + provided common or generated vehicle-resolved travel times
→ solver node location lookup
→ solver + verifier
```

다음을 MUST 지킨다.

1. Travel key는 request/node ID가 아니라 physical location ID다.
2. 모든 solver node는 유효한 location mapping을 갖고 preparation은 모든 directed physical-location pair를 열거한다.
3. 제공된 `D`는 authoritative directed meter, 제공된 `U`는 vehicle-independent authoritative directed second다. 둘 다 정수만 허용하고 소수값은 거부한다.
4. Legacy `C`는 비권위 필드이며 feasibility, score 또는 generation 분기에 사용하지 않는다.
5. 모든 self arc는 입력값과 관계없이 `D=0 meter`, `U=0 second`로 정규화한다.
6. 누락 `D`는 좌표 기반 Great Circle로 계산하고 `HALF_UP`으로 가장 가까운 정수 meter를 만든다. 필요한 좌표가 없으면 solve 전 입력 오류다.
7. 누락 `U`는 vehicle별 `CEILING(D_meter × 3.6 ÷ speed_km_h)` 정수 second로 만든다. Vehicle 속도가 없으면 `45 km/h`를 사용한다.
8. 제공된 common `U`와 vehicle별 생성 `U`를 구분하고 solver 시작 전 모든 사용 vehicle/pair의 시간이 해소되어야 한다.
9. Reverse arc 복사, 대칭 평균 또는 탐색 중 lazy generation을 금지한다.
10. Raw input, provided/generated source, generation policy, unit, coverage, diagonal policy와 raw/prepared fingerprint를 보존한다.
11. Solver core와 verifier는 같은 준비 완료 travel data만 사용하고 좌표·속도 계산을 반복하지 않는다.

현재 [`data/win_poc_case.json`](../data/win_poc_case.json)의 소수 `D/U`는 이 정수 계약에 비준수다. 정수 matrix를 다시 받거나 별도 명시적 계약 변경이 있기 전에는 canonical normalization, official baseline 또는 official benchmark에 사용하지 않는다.

## 9. Extensible policy, evaluation과 profile architecture

이 절의 `facts`, contributor/snapshot, objective schema/score, profile registry와 `SolvePlan`은 책임을 설명하는 잠정 라벨이다 (`P-04`~`P-06`). 구현은 아래 책임 분리를 지켜야 하지만 최종 타입명, API와 내부 표현은 아직 확정하지 않는다.

### 9.1 단방향 평가 구조

```text
normalized immutable facts
→ propagation + structural/static hard gates
→ policy-neutral facts and metrics
→ composed hard constraints
→ score components
→ objective schema/comparator
→ SolvePlan stages
```

각 책임은 다음을 MUST 지킨다.

| 책임 | 소유하는 의미 | 금지 |
|---|---|---|
| normalization | ID, unit, time, travel, service meaning, static compatibility | 고객 가격·목적 순서 |
| propagator | route 진행의 물리 사실과 explicit feasibility | 단가·최종 진단 |
| hard constraint | 후보 허용 가능성 | 유한 penalty로 위반 상쇄 |
| neutral metric | 발생량과 사실 | 좋고 나쁨, 가격, 최종 원인 |
| score policy | feasible 사실의 비용·soft penalty | route/raw input 재해석 |
| objective comparator | ordered dimension과 방향 | stage 실행, 물리 재계산 |
| `SolvePlan` | stage, warm-start, guard와 budget reference | hard rule 해제 |

Hard-infeasible 후보는 score, acceptance, temperature 또는 reward로 feasible이 될 수 없다. 같은 우선순위에서 고객이 교환 가능하다고 명시한 비용만 scalar로 합성한다. 양보할 수 없는 우선순위는 ordered dimensions로 비교하며 숨은 Big-M scalar로 평탄화하지 않는다.

### 9.2 Neutral metrics와 policy extension

공통 물리 집계만 common metrics에 둔다. 선택적인 고객사 지표는 잠정 contributor/snapshot seam으로 propagation facts에서 계산한다. Contributor는 가격이나 feasibility를 반환하지 않으며 raw route나 input을 재해석하지 않는다.

새 요구는 다음의 가장 좁은 경계에 둔다.

```text
input meaning
→ static compatibility
→ bound route/solution hard constraint
→ neutral metric contributor
→ score component
→ objective/comparator
→ SolvePlan/profile composition
```

기존 facts로 실제 물리 상태를 표현할 수 없고, 단가나 우선순위 문제가 아니며, 기존 profile과 verifier의 의미를 보존할 좁은 seam이 있을 때만 propagator/fact contract 확장을 검토한다.

### 9.3 Profile binding과 lifecycle

장기 profile definition/registry와 solve별 immutable bound profile을 분리하는 방향은 잠정이다. 어떤 구체 이름을 사용하든 다음 lifecycle은 MUST 지킨다.

- exact profile key/version/config를 resolve하고 unknown/latest fallback을 금지한다.
- 정규화된 dense ID, unit과 fact contract에 constraint/metric/score/objective/plan을 bind한다.
- Missing dependency, duplicate score key, unit/schema mismatch와 unknown reference를 solve 전에 거부한다.
- Bound profile은 생성 후 불변이고 request-indexed array는 한 problem에만 속한다.
- Evaluation scratch, route cache, adaptive state와 telemetry는 evaluation/run/seed 사이에 공유하지 않는다.
- Problem, profile config, constraint/metric/score/comparator/plan versions를 결과 fingerprint로 전달한다.

Solve 요청은 해당 고객사에 등록·승인된 objective preset만 선택할 수 있다. 요청이 objective 순서·가중치·수식을 직접 주입하거나 다른 고객사의 preset을 선택할 수 없다. Preset 생략 시 그 고객사 설정에 exact key/version으로 지정된 default를 사용한다. Unknown/unauthorized preset, 해당 고객사에 없는 objective 또는 unmet metric dependency는 bind 전에 거부하며 비슷한 preset으로 fallback하지 않는다.

특정 objective dimension은 일부 고객사에만 존재할 수 있다. Mandatory 의미를 사용하는 preset은 `mandatoryUnassignedCount`를 최상위 사전식 objective로 둔다. 이는 hard constraint나 유한 Big-M penalty가 아니므로 0이 불가능해도 최소 양수의 verified partial solution을 반환할 수 있다.

입력 fleet의 `DIRECT`와 `LEASE` vehicle은 모두 실제 배정 resource다. Solver가 fleet 밖의 공급자를 만들거나 미배정 request를 외주/이월로 추정하지 않는다. Customer objective의 기본/선택 구조는 다음 의미를 보존한다.

```text
optional mandatoryUnassignedCount
→ totalUnassignedCount
→ optional outsourcedVehicleVolumeCost
→ regularVehicleVolumeCost
→ remaining customer objectives
```

- `regularVehicleVolumeCost`는 사용한 `DIRECT` vehicle별 `maxVolume`을 차량당 한 번 합산해 최소화한다.
- `LEASE`를 허용하는 고객 preset은 사용한 `LEASE` vehicle별 `maxVolume` 합인 `outsourcedVehicleVolumeCost`를 더 앞선 차원으로 추가한다.
- `LEASE` vehicle이 입력되었는데 bound preset이 외주 objective를 지원하지 않으면 solve 전 binding 오류다.
- 고정 `1:100` scalar나 음수 score 예시로 strict priority를 흉내 내지 않는다.

## 10. Search solution과 final result

### 10.1 Search-time state

`SearchRequestBank`는 request ID membership만 보유한다.

- Pair insertion 성공 후 request를 제거한다.
- Pair destroy 성공 후 request를 추가한다.
- Candidate copy, acceptance, rejection과 rollback에 routes와 함께 참여한다.
- Pickup/delivery node membership, 비용, last failure, final status, diagnostic, deferred/outsourced 의미를 저장하지 않는다.

한 insertion failure나 반복된 rejection은 최종 reason이 아니다. Search telemetry가 필요하면 bank와 분리된 bounded record로 수집한다.

### 10.2 Finalization과 result

최종 결과는 검색 state의 직렬화가 아니다.

```text
committed candidate + immutable inputs + provenance
→ candidate solution verifier
→ verifier PASS report + verified solution
→ preliminary ASSIGNED/UNASSIGNED partition
→ required final-solution insertion audit
→ final outcomes/structured diagnostics
→ post-finalization result-integrity verifier
→ publication
```

Candidate solution verifier는 immutable problem/profile declaration, candidate route/node order, bank와 normalized matrix만으로 candidate를 cache-free 검증한다. `PASS` report와 verified solution이 없으면 finalization은 게시 가능한 outcome을 만들 수 없다.

모든 입력 request는 결과에 정확히 하나의 outcome을 가져야 한다. `ASSIGNED`는 exactly one verified route/vehicle/pair를 참조하고 `UNASSIGNED`는 route를 참조할 수 없다. `DIRECT`와 `LEASE` route에 배정된 request는 모두 `ASSIGNED`이며 ownership은 vehicle reference에서 구분한다. 입력 fleet 어디에도 배정되지 않은 request만 `UNASSIGNED`다. Solver는 운영자의 후속 외주·이월을 `OUTSOURCED` 또는 `DEFERRED` outcome으로 생성하지 않는다. Summary count는 outcome에서 파생한다.

Normalization/static precheck로 순서·탐색과 무관한 불가능성이 증명된 `UNASSIGNED` request는 `PROVEN` diagnostic을 사용할 수 있고 중복 insertion audit를 생략한다. 그 밖의 모든 `UNASSIGNED` request는 publication 전에 final routes를 고정한 채 모든 eligible vehicle과 합법 pickup/delivery position pair를 검사한다.

- 모든 option이 실패한 경우에만 `EXHAUSTIVE_FOR_FINAL_SOLUTION`과 `NO_FEASIBLE_INSERTION_IN_FINAL_SOLUTION` 계열 진단을 사용할 수 있다.
- 이 confidence는 현재 final routes 기준이며 전역 재배치 불가능성 증명이 아니다.
- Audit가 feasible insertion을 찾으면 자동 삽입, 재호출 또는 재탐색하지 않고 outcome은 `UNASSIGNED`로 게시할 수 있다.
- Feasible insertion 발견은 내부 audit record에만 남기고 외부 outcome에는 일반 `UNASSIGNED`와 별도로 성립하는 proven/search-observed/`UNKNOWN` 근거만 노출한다.
- Audit contract/version, 검사 vehicle/position 수, constraint별 rejection count, completion, work와 elapsed를 기록하고 ALNS step/quality vector와 분리한다.

Diagnostic code/scope/confidence/source/evidence의 정확한 wire 표현은 잠정이지만 실제 source 범위를 넘는 confidence를 만들 수 없다.

Post-finalization result-integrity verifier는 candidate verifier의 `PASS` report와 verified solution, final outcomes, diagnostic source/audit evidence, summary와 publishable payload를 입력으로 받아 outcome partition, vehicle ownership reference, audit completeness/confidence ceiling, summary와 payload fingerprint를 검증한다. 두 verifier 모두 search cache와 solver summary를 권위 입력으로 거부한다. Search telemetry가 diagnostic evidence로 허용되더라도 §10.1의 bounded record와 실제 source 범위를 넘는 confidence를 만들 수 없다. 어느 verifier든 `FAIL`이거나 미완료이면 정상 route/outcome payload와 benchmark vector publication을 거부한다.

### 10.3 Result provenance

게시 가능한 result는 최소한 다음 의미를 역추적할 수 있어야 한다.

- input digest, adapter와 normalized problem
- numeric/time/matrix policy
- profile, constraints, metrics, score, objective, comparator와 `SolvePlan`
- portfolio source, seeds, operators, algorithm/state strategy와 build/runtime compatibility
- stage별 requested/completed steps와 별도 inner-work counters
- termination과 상위 execution 상태
- canonical solution/result fingerprint
- 두 verifier의 contract/version, 판정과 bounded evidence

Run 종료와 정상 solution publication은 별도 상태다. 서로 다른 run의 route와 metric 최솟값을 합쳐 가상 결과를 만들지 않는다.

## 11. Initial portfolio와 ALNS pipeline

### 11.1 공통 pair evaluator

Construction, repair, paired move와 fleet reinsertion은 하나의 atomic pair insertion 의미를 사용해야 한다.

```text
stable solution + bank request + target route + pickup/delivery positions
→ structural/service-pattern gate
→ servableVehicles gate
→ hard-feasibility propagation
→ feasible option facts/metrics/ranking values
```

Evaluator는 committed state를 바꾸지 않는다. 선택된 move를 atomic 적용한 뒤 cache-free route 재계산과 같은 결과를 내야 한다. Feasible option만 ranking에 들어가며 마지막 total order는 stable request/vehicle/route/position identity로 결정한다.

### 11.2 현재 범위의 initial-solution portfolio

최소 portfolio는 다음 네 policy를 MUST 포함한다.

| Policy | 규범 역할 |
|---|---|
| `SEQ_FARTHEST` | bound farthest scorer의 seed로 route를 순차 확장 |
| `SEQ_EARLIEST_DEADLINE` | bound deadline scorer가 촉박하다고 판정한 request부터 순차 구성 |
| `PAR_REGRET_2` | 모든 active route의 best/second feasible option 손실을 비교 |
| `RAND_REGRET_3` | regret-3 구조와 versioned derived seed로 다양화 |

각 policy는 독립 route/bank/cache/random state를 사용한다. 후보별 bounded pair-safe improvement와 허용된 route-elimination attempt를 거친 뒤 cache-free validation, canonical fingerprint 중복 제거와 bound comparator 비교를 수행한다. Comparator상 가장 좋은 한 개를 `best_initial_solution`으로 반드시 선택해 ALNS warm-start set에 포함한다. Admission을 통과한 서로 다른 route 구조의 `top_k_diverse_solutions`는 best를 대체하지 않는 추가 시작점이다.

Farthest/deadline scorer, randomized start 수, diverse `K`와 후보별 light-search work budget의 의미와 승인 프로토콜은 [Q-ALG-01](master-design-open-questions.md#q-alg-01)에 정리되어 있다. 동일 corpus, recorded seed set와 downstream ALNS budget으로 versioned calibration을 수행하고, verifier·reproducibility 실패 configuration을 제외한 뒤 Algorithm·Benchmark owner가 exact configuration/version을 승인해야 한다. 실제 공식 수치는 아직 없으므로 `OPEN — EXPERIMENT_REQUIRED`이며, 그전에는 explicit experiment/test configuration만 사용하고 hidden default를 두지 않는다.

### 11.3 ALNS step

한 completed ALNS step은 다음 전체 전이다.

```text
committed current
→ request-pair destroy
→ bank 기반 pair repair
→ configured bounded in-step improvement
→ stable candidate validation/evaluation
→ hard-feasibility + stage guard
→ acceptance
→ commit 또는 discard
→ current/best 판정
→ adaptive/acceptance state 갱신
→ completed-step counter +1
```

Destroy와 repair의 대상 수는 node가 아니라 request 수다. Repair 뒤 삽입되지 않은 request는 완전한 pair가 route에 없는 채 bank에 남는다. Candidate가 hard-infeasible이면 acceptance에 도달하지 않는다.

지원 가능한 destroy/repair, local/fleet family는 bound registry/config가 명시하고 versioning한다. ALNS core는 고객사 ID, 가격 key 또는 Win PoC 순서를 열거하지 않는다. Non-improving feasible acceptance는 `current`만 바꿀 수 있고 verified `stageBest`/`solveBest`를 악화시킬 수 없다.

각 stage는 이전 verified best를 warm-start로 받는다. 선행 objective guard를 통과하지 못한 후보는 하위 목표 개선만으로 current/best가 될 수 없다. Stage 실패나 중단은 마지막 verified best를 보존한다.

### 11.4 Deferred algorithm boundary

Route pool, column pruning/persistence와 MIP model/reconstruction은 현재 범위가 아니다. 현재는 verified ordered route, source policy/run, metrics와 solution fingerprint를 export할 논리 경계만 보존한다. 별도 승인 전에 MIP dependency나 타입을 core에 넣지 않는다.

## 12. Candidate state, cache와 rollback

### 12.1 Initial copy-on-write

초기 안전 구현은 changed-route copy-on-write와 독립 bank를 사용한다.

```text
committed immutable snapshot
├─ immutable ProblemInstance/profile
├─ immutable/shared unchanged routes
├─ independent bank value
└─ validated aggregate/fingerprint

candidate
├─ base snapshot
├─ first write 전에 복사한 changed routes
├─ independent copied bank
└─ invalidated derived state
```

`current`, `stageBest`, `solveBest`는 immutable snapshot으로 취급한다. Candidate는 committed/best route나 cache를 직접 바꿀 수 없다. Accepted candidate만 freeze 후 새 current가 되고, rejected/interrupted/failed candidate는 전체 폐기한다.

### 12.2 Cache contract

Route sequence, request ownership, vehicle/terminal binding과 bank가 source of truth다. Arrival/load, feasibility, metrics, score, objective, aggregate, insertion table과 structural hash는 재계산 가능한 파생 상태다.

- Cache는 한 problem/profile/run/structure version에만 속한다.
- Route 또는 bank mutation은 영향받은 route/solution cache와 fingerprint를 무효화한다.
- Cache key는 route/request/positions뿐 아니라 evaluator/profile과 필요한 generation을 구분한다.
- 의심 cache는 복원하지 않고 버린 뒤 전체 재계산한다.
- Cached/incremental 결과는 cache-free full recomputation과 정확히 같아야 한다.

### 12.3 Later apply/undo gate

Copy-on-write는 현재 구현·운영 기본 경로다. Apply/undo는 roadmap의 자동 전환 대상이나 필수 산출물이 아니다. COW baseline profiling에서 route copy·allocation·GC가 실제 병목으로 입증되고 별도 변경 제안이 승인된 경우에만 다음 동등성 gate를 갖춘 실험 후보가 될 수 있다.

1. 모든 move의 apply/undo round-trip 뒤 route, bank, fleet state와 fingerprint가 같다.
2. 중간 예외, watchdog, cancellation과 resource signal fault injection 뒤 current/best가 보존된다.
3. 같은 initial state, seed와 operator trace에서 COW와 step별 candidate outcome, acceptance, current/best와 adaptive update가 같다.
4. Cache-free full verifier와 최종 canonical solution이 같다.
5. 실제 fixture와 microbenchmark에서 copy/allocation이 측정된 병목이다.
6. 승인된 성능 기준을 충족하고 정확성·재현성·관측 가능성 회귀가 없다.

임의의 자동 전환 threshold를 두지 않는다. 정확성·재현성·관측 가능성 동등성 중 하나라도 실패하거나 성능 이득이 충분하지 않으면 COW를 계속 유지한다. 향후 apply/undo 채택은 [Q-ALG-02](master-design-open-questions.md#q-alg-02)의 `RESOLVED — KEEP_COW`를 묵시적으로 뒤집을 수 없으며 새로운 profiling evidence와 별도 변경 승인이 필요하다.

## 13. Termination, reproducibility와 execution provenance

### 13.1 종료 의미

정상 품질 예산은 stage별 양의 `maxSteps`다. Wall-clock 시간은 monotonic watchdog으로만 사용한다.

| 의미 | 지위 |
|---|---|
| `MAX_STEPS_REACHED` | 모든 계획 stage의 step budget과 handoff를 완료한 정상 종료 |
| `WATCHDOG_REACHED` | 병리적 장기 실행을 중단한 예외적 안전 종료 |
| `CANCELLED` | 외부 취소 의도를 협력 처리한 종료 |
| `RESOURCE_LIMIT_REACHED` | solver가 처리 가능한 자원 안전 한계 |
| `PLATFORM_TIMEOUT` | 상위 실행 경계가 algorithm termination record 완성을 막은 상태 |
| `FAILED` | 실행, 구현 또는 platform failure |

입력/config binding 실패는 탐색 종료가 아니라 시작 전 오류다. 미완료 step은 completed count, acceptance/temperature와 adaptive state를 전진시키지 않고 candidate를 완전히 rollback/discard한다. Watchdog, cancellation, resource와 platform failure를 서로 또는 정상 종료로 다시 이름 붙여서는 안 된다.

예외 종료 뒤 마지막 committed best가 있더라도 candidate solution verifier와 post-finalization result-integrity verifier를 통과한 경우에만 recovery candidate가 될 수 있다. 정상 완료나 공식 benchmark run으로 표시할 수 없으며 외부 노출 여부는 별도 product 계약이다.

### 13.2 Strong reproducibility envelope

강한 재현성은 다음이 고정되고 `MAX_STEPS_REACHED`로 끝난 실행에만 적용한다.

```text
problem + normalized matrix + numeric/time/adapter fingerprints
+ exact profile/config/evaluation/objective/SolvePlan versions
+ portfolio/algorithm/operator/acceptance/state-strategy versions
+ build/runtime compatibility fingerprint
+ base/derived seeds and derivation version
+ round/run ordinal and warm-start lineage when using multi-round execution
+ stage maxSteps
+ stable iteration, reduction and tie-break order
```

이 envelope에서는 canonical step trace, verified solution, outcome partition, metrics/score/objective와 result fingerprint가 같아야 한다. Global random, unordered collection iteration, thread first-winner, clock-based tie-break와 cache hit/miss 의존을 금지한다.

### 13.3 Provenance

관측 elapsed time은 metadata일 뿐 quality budget이나 fingerprint의 hidden input이 아니다. Execution record는 stage별 requested/completed steps, separate construction/local/fleet/audit/verifier work, actual base/derived seeds, round/run ordinal, warm-start candidate, selected initial candidate, operator/config versions, exact termination, 마지막 completed stage/step과 rollback integrity를 보존해야 한다. Seed는 “좋은 값”을 선별하는 품질 대상이 아니라 고정 manifest 안에서 서로 다른 탐색 경로를 만드는 다양성 입력이다.

## 14. Independent verification과 Win PoC benchmark

### 14.1 Publication gate

Publication은 서로 다른 입력과 책임을 갖는 다음 두 gate를 정확히 이 순서로 통과해야 한다.

```text
committed candidate
→ candidate solution verifier
→ preliminary request partition
→ required final-solution insertion audit
→ final request outcomes/diagnostics
→ post-finalization result-integrity verifier
→ publication
```

#### Candidate solution verifier

Candidate solution verifier의 권위 입력은 다음 네 가지다.

- immutable problem/profile declaration
- candidate route/node order
- candidate의 `SearchRequestBank`
- authoritative prepared directed travel data

이 verifier는 위 입력에서 cache 없이 다음을 다시 계산해야 한다.

1. Problem/profile/matrix/solution identity와 fingerprint
2. 모든 request의 route/bank partition, exactly-once, same-vehicle와 precedence
3. Vehicle, oneway/roundtrip terminal 정책과 service-pattern 규칙
4. Size membership, capability subset과 `servableVehicles`
5. 모든 directed leg의 location mapping, distance와 time
6. Load, time, planning/vehicle windows와 route resources
7. 등록된 hard constraints
8. Neutral metrics, score breakdown과 objective vector

Solver feasibility flag, insertion 결과, cached arrival/load/metric/score, search cache, solver summary와 structural hash는 candidate solution verifier의 권위 입력이 아니다. Missing arc나 unresolved policy는 검증 실패다. `PASS` report와 verified solution이 없으면 finalization은 게시 가능한 outcome 생성으로 진행할 수 없다.

#### Post-finalization result-integrity verifier

Post-finalization result-integrity verifier의 권위 입력은 다음이다.

- candidate solution verifier의 `PASS` report와 verified solution
- final outcomes
- diagnostic source/audit evidence
- outcome-derived summary
- publishable payload

이 verifier는 모든 input request와 verified solution 사이의 exactly-one `ASSIGNED`/`UNASSIGNED` outcome partition, status와 route/vehicle ownership reference의 일치, required audit의 완결성과 diagnostic confidence ceiling, outcome에서 재계산한 summary, canonical solution/result identity와 게시할 payload fingerprint를 cache 없이 검증해야 한다.

Search cache와 solver summary는 post-finalization result-integrity verifier의 권위 입력도 아니다. Bounded search telemetry가 승인된 diagnostic source로 전달될 수는 있지만 그 자체가 solver summary를 권위화하거나 proven/exhaustive confidence를 만들지는 않는다. 이 gate의 `FAIL` 또는 미완료도 publication을 거부한다.

두 gate 중 하나라도 `FAIL`이거나 미완료이면 정상 route/outcome payload와 benchmark vector를 발행하지 않는다. Candidate verifier 실패는 정상 unassignment diagnostic으로 바꾸지 않고, result-integrity 실패는 candidate solution `PASS`로 덮어쓰지 않는다.

### 14.2 Primary fixture와 manifest

1차 end-to-end fixture는 [`data/win_poc_case.json`](../data/win_poc_case.json)이다.

- Read-only input fixture이며 정답 route나 baseline을 포함하지 않는다.
- 확인된 SHA-256은 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`이다.
- Fixture의 `trips=oneway`가 우선하므로 `multiRotation=1`은 route 의미에 영향을 주지 않는다. Depot에서 한 번 출발하고 중간·최종 depot 방문 없이 마지막 고객에서 끝난다.
- Raw `multiRotation` 값은 provenance에 남기되 feasibility, route 생성과 metric에 사용하지 않는다.
- 현재 fixture의 소수 `D/U`는 정수 meter/second 입력 계약에 비준수이므로 정수 matrix를 다시 받기 전 official run에 사용할 수 없다.
- Official comparison은 fixture, adapter, numeric/time/matrix, profile, algorithm/build, seeds/steps, verifier, metric formula와 gate를 하나의 비교 계약 snapshot으로 고정해야 한다.
- 그 snapshot을 round/worker plan과 함께 versioned manifest/card로 표현하는 방식은 잠정이다 (`P-13`). Exact schema와 official 수치는 아직 확정하지 않는다.
- Fingerprint가 다르면 quality regression을 판정하지 않고 비교 불가로 처리한다.
- Baseline은 실제 verified official run의 비교 record와 champion lineage로만 만들고 자동 덮어쓰지 않는다.

### 14.3 Win PoC comparator

Win PoC 전용 품질 vector는 다음 exact lexicographic order다.

```text
미배정 주문 수
→ 배차 차량 수
→ 전체 거리
→ 전체 시간
```

```text
(
  unassigned order count,
  dispatched vehicle count,
  total distance,
  total time
)
```

모든 성분은 작을수록 좋으며 첫 번째로 다른 성분만 승패를 결정한다.

- `unassigned order count`: verified input vehicle route에 배정되지 않은 input request/order 수
- `dispatched vehicle count`: request를 하나 이상 수행하는 고유 input vehicle 수
- `total distance`: verifier가 actual directed route arcs에서 재계산한 합
- `total time`: 모든 used route의 운영시간 합

네 성분이 모두 같으면 품질상 동률이다. Deterministic structural tie-break는 다섯 번째 품질 성분이 아니다. 이 comparator는 Win PoC benchmark 전용이며 모든 고객사의 solve objective가 아니다.

```text
totalRouteOperationalTimeSeconds
= Σ_used_routes(
    driveTime
  + customerWaitingTime
  + depotWaitingTime
  + serviceTime
  + interWorkWindowRestTime
)
```

Used vehicle의 route 시작 전 업무와 무관한 유휴시간, 미사용 vehicle 시간, solver/검증/직렬화 elapsed는 포함하지 않는다. Drive, customer/depot wait, service, inter-work-window rest와 total을 각각 재계산 가능한 breakdown으로 보존한다.

Quality, integrity, reproducibility와 performance 판정은 분리한다. Parsing, binding, construction, search, verification, finalization의 elapsed/memory 값은 성능 관측이며 네 성분 quality vector에 들어가지 않는다.

### 14.4 Multi-round official execution

Official benchmark는 manifest에 고정된 round plan을 정확히 실행한다.

1. 첫 round worker는 verified `best_initial_solution`과 admission된 diverse initial solutions를 manifest의 warm-start 규칙에 따라 사용한다.
2. 같은 round의 worker는 동일 execution contract와 서로 다른 derived seed를 사용한다.
3. 모든 worker candidate를 독립 검증한 뒤 stable comparator/tie-break로 단일 round champion을 고른다.
4. 다음 round의 모든 worker는 직전 round champion을 공통 warm start로 사용한다.
5. 마지막 round의 verified champion만 전체 official result다.

Manifest가 선언한 모든 worker는 exact `maxSteps`로 `MAX_STEPS_REACHED`하고 독립 검증을 통과해야 round가 완료된다. 실패 worker는 같은 round/run identity, seed와 warm start로 재시도할 수 있지만, 하나라도 끝내 완료되지 않으면 round와 전체 benchmark는 `INCOMPLETE`다. 성공한 일부 worker만으로 champion을 정하거나 다음 round를 시작할 수 없다.

Seed별 no-worse나 “좋은 seed” 선정은 official hard gate가 아니다. Worker 완료 순서와 물리 병렬 순서는 champion에 영향을 주지 않는다. Result-dependent 조기 종료와 전체 wall-clock quality deadline을 사용하지 않는다.

Round 수, round별 worker 수, warm-start 배정, worker별 `maxSteps`와 watchdog은 [Q-BENCH-02](master-design-open-questions.md#q-bench-02)의 확정된 calibration/approval protocol을 따라야 하지만 실제 공식 수치는 아직 없다. 인터뷰의 동작 설명용 수치나 기존 초안의 임시값을 공식값으로 사용하지 않는다. 논리 fan-out/fan-in은 provider-neutral하며 특정 cloud orchestration/worker product를 확정하지 않는다.

## 15. Implementation roadmap와 phase gates

아래 산출물은 모두 미래 개발 대상이다. 문서가 존재한다는 사실만으로 phase가 완료되지 않는다.

| Phase | 의존성과 향후 산출물 | 필수 exit evidence |
|---|---|---|
| `RM-0` 문서·결정 통제 | 이 Master, 중앙 질문, 확정/잠정/보류와 traceability baseline | `C-01`~`C-22` coverage, 질문 link, 문서 lint, completion/topology claim 없음 |
| `RM-1` normalized domain/input | immutable problem, versioned adapter, exact numeric/time/service/compatibility contracts, Travel Matrix preparation와 pair validation | hand-calculated normalization, boundary/overflow, provided/generated travel provenance, directed asymmetry와 invariant evidence |
| `RM-2` bound evaluation/profile | facts, hard constraints, metric contributors, score, comparator, `SolvePlan`, bound profile | layer isolation, dependency rejection, full evaluation, comparator/guard와 profile lifecycle evidence |
| `RM-3` pair evaluator/portfolio | atomic evaluator, 네 policy, bounded improvement, validation/dedup, mandatory best와 optional diverse set | policy deterministic traces, evaluator-vs-full equality, rollback, lineage와 explicit config; official defaults는 `Q-ALG-01` 실험 승인 필요 |
| `RM-4` ALNS/COW | pair destroy/repair, stages, guarded acceptance, adaptive state, COW/cache/termination | interruption fault injection, step counter, COW isolation, cache equivalence와 normal deterministic rerun |
| `RM-5` verifier/result | candidate solution verifier, two-state outcomes, required unassigned audit, diagnostics, post-finalization result-integrity verifier와 recovery path | 두 gate의 cache/solver-summary 비권위성, audit completeness/confidence ceiling, corrupted candidate·outcome/payload rejection, complete partition와 gate별 publication rejection |
| `RM-6` Win PoC baseline | compliant integer travel fixture, immutable multi-round manifest/card, actual final champion과 challenger workflow | `Q-ALG-01`/`Q-BENCH-02` 실험 승인, 모든 선언 worker의 verified normal completion, exact comparator, rerun와 approval |
| `RM-7` COW profiling과 선택적 후속 제안 | COW baseline profiling; 실제 병목일 때만 별도 apply/undo experiment proposal | COW 유지가 기본. 재제안 시 measured bottleneck, fault/trace/full-verifier equality와 별도 변경 승인 |
| `RM-8` compatibility migration | logical-port integration, versioned legacy adapter, shadow/cutover/rollback plan | lineage, semantic compatibility, idempotency/cancellation, verified end-to-end와 rollback evidence |
| `RM-9` separately approved follow-ups | route pool/MIP, variants, academic expansion와 physical topology의 개별 roadmap | 각 항목의 resume criteria와 별도 scope approval |

규범적 순서는 `RM-0 → RM-1 → RM-2 → RM-3 → RM-4 → RM-5 → RM-6 → RM-7 → RM-8`이다. `RM-8`의 logical port와 test-double 설계는 `RM-0` 뒤 병행할 수 있지만 verified cutover는 선행 gate 없이 완료할 수 없다. `RM-9`는 자동 착수 phase가 아니다.

해결된 질문은 [세션 29](master-design-sessions/29-open-question-interview.md)의 exact decision을 gate 입력으로 사용한다. 남은 두 실험 항목은 답을 소비하는 가장 이른 gate만 막는다. `Q-ALG-01` 전에도 explicit config로 portfolio contract를 구현할 수 있고, `Q-BENCH-02` 전에도 logical round/worker orchestration을 test double로 검증할 수 있지만 official defaults와 baseline은 발행할 수 없다. Test/experiment 값은 명시적으로 주입하며 production default로 승격하지 않는다.

## 16. Risks, migration, deferred work와 non-scope

### 16.1 주요 위험과 gate

| 위험 | 조기 신호 | Gate/대응 |
|---|---|---|
| semantic drift | adapter/preparation/evaluator/verifier가 같은 입력을 다르게 해석 | exact policy/source fingerprint, unresolved generation rejection, verifier identity check |
| customer branching | 고객사 이름이나 업무 문자열이 common evaluator/ALNS에 등장 | profile dependency closure와 new-profile change-impact review |
| pair/rollback 손상 | partial pair, route+bank 중복, 중단 뒤 fingerprint 변화 | atomic mutation, fault injection과 full partition verification |
| stale cache | hit/miss에 따라 feasibility/score가 달라짐 | cache-free equality at insertion, acceptance, best와 final |
| reproducibility erosion | global random, unordered merge, time-quality termination | namespaced seeds, stable order와 normal-step rerun |
| verifier coupling | solver cache/summary를 재사용 | independent path와 corrupted-cache fault injection |
| benchmark mismatch | 다른 manifest card나 incomplete round를 직접 비교 | immutable multi-round manifest와 compare-not-allowed/`INCOMPLETE` 판정 |
| premature optimization | baseline 전에 complex undo/cache 최적화 | COW first, `RM-7` measured decision와 no-switch 허용 |
| infrastructure coupling | core가 transport/runtime 타입을 참조 | logical ports와 dependency review |
| result overclaim | bank/last failure가 proven status/reason이 됨 | conservative finalization과 evidence-bounded confidence |

### 16.2 Migration

Legacy 문서와 현재 코드 구조는 replacement inventory와 characterization 대상일 뿐 목표 계약 준수 증거가 아니다. Migration은 다음 순서를 SHOULD 따른다.

1. 현재 input/output/state/error 의미를 read-only로 characterization한다.
2. 목표 계약과 semantic compatibility matrix를 만든다.
3. 새 normalized core, evaluation, algorithm과 verifier를 logical ports 뒤에서 격리한다.
4. 승인된 legacy meaning만 versioned adapter로 변환하고 provenance를 보존한다.
5. 동일 logical input에 대해 side-effect 없는 shadow comparison을 수행한다.
6. 두 publication verification gate와 동일 manifest를 통과한 결과만 publication/benchmark candidate로 사용한다.
7. Versioned cutover, retry/idempotency/cancellation과 rollback을 증거로 검증한다.
8. 구현이 설계를 바꿔야 하면 관련 결정 기록과 문서를 같은 변경 단위에서 갱신한다.

### 16.3 Deferred resume criteria

| 항목 | 현재 보존할 경계 | 재개 조건 |
|---|---|---|
| route pool/MIP | verified ordered route, lineage, metrics와 fingerprint export | Win baseline, verifier, reproducible portfolio/ALNS, measured value와 별도 solver/licensing/fallback 승인 |
| optional variants | 현재 atomic pair, fixed terminal, bank와 matrix contract | [Q-VAR-01](master-design-open-questions.md#q-var-01)의 선택·시점 결정, 대표 fixture, hand result와 core-impact feasibility 승인 |
| physical topology | 논리 ports, status/artifact/idempotency/cancellation 책임 | verified result, workload, security/access/retention/audit, retry/recovery, performance/cost evidence와 별도 승인 |
| academic benchmark expansion | Win manifest/card와 verifier 재사용 경계 | Win baseline, authoritative format/result, RPDPTW mapping과 separate manifest 승인 |
| multi-trip/rotation | 현재 `oneway`/single `roundtrip`; 후속 trip도 pair crossing 금지와 depot `duration` 경계 보존 | `multiRotation` 값·trip/reset/depot window/resource 계약, 예제와 domain/algorithm/verifier 영향의 별도 승인 |
| dynamic routing | immutable solve snapshot과 cancellation port | event/replanning, state continuity, conflict와 SLA 계약 승인 |

선택 변형을 현재 pair invariant 완화로 미리 구현하거나, route pool/MIP dependency를 core에 선반영해서는 안 된다. Physical topology는 안정된 logical contracts와 workload evidence 뒤에 가장 마지막으로 결정한다.

## 17. Open questions와 traceability

### 17.1 중앙 질문

28개 질문의 상태와 결정 단일 등록부는 [Master Design open questions](master-design-open-questions.md)다. 세션 29 인터뷰로 24개가 해결되었고, `Q-ALG-01`·`Q-BENCH-02`는 프로토콜만 확정된 `OPEN — EXPERIMENT_REQUIRED`, `Q-INFRA-01`·`Q-VAR-01`은 `DEFERRED`다. 등록부는 exact decision, evidence, gate와 이 문서 반영 절을 보존한다. 새 질문 ID를 이 문서에서 만들지 않는다.

### 17.2 상세 통합 입력

| 상세 문서 | Master가 소비한 경계 |
|---|---|
| [세션 20 — domain/input](master-design-sessions/20-domain-input-draft.md) | canonical model, pair invariant, normalization, fixed point, values, matrix와 search bank |
| [세션 21 — policy/objective](master-design-sessions/21-policy-objective-draft.md) | hard/metric/score/objective/plan 분리, profile binding과 customer extension |
| [세션 22 — algorithm](master-design-sessions/22-algorithm-draft.md) | portfolio, pair evaluator, ALNS, termination, COW/cache/rollback과 apply/undo gate |
| [세션 23 — result/benchmark](master-design-sessions/23-result-benchmark-draft.md) | result partition, diagnostics, verifier, recovery와 Win PoC comparison |
| [세션 24 — roadmap](master-design-sessions/24-roadmap-draft.md) | logical ports, phase order/evidence, risk, migration와 deferred resume criteria |
| [세션 18 — governance](master-design-sessions/18-document-governance.md) | REVIEW 상태, 규범어, source hierarchy와 변경 관리 |
| [세션 29 — 사용자 인터뷰](master-design-sessions/29-open-question-interview.md) | 26개 interview 대상의 authoritative 사용자 답변과 두 실험 대기 protocol |
| [세션 30 — 질문 통합](master-design-sessions/30-open-question-integration.md) | Master/register/영향 세션 반영 범위와 validation evidence |

[Domain Design](domain-design.md)는 legacy 상세 inventory와 traceability evidence다. 이 Master와 충돌하는 고정 numeric/time defaults, node-sized matrix, generic feature, multi-trip 기본 활성화, mixed bank/result 또는 구체 구현 형태는 규범으로 사용하지 않는다.

연구 근거는 [문제 정의](arranged/01_problem_definition.md), [초기해 휴리스틱](arranged/02_initial_solution_heuristics.md), [ALNS](arranged/03_alns_metaheuristic.md), [local search](arranged/05_local_search_moves.md), [실무 확장](arranged/06_practical_extensions.md)과 [논문·benchmark](arranged/07_papers_and_benchmarks.md)에 있다. 연구 예시는 승인된 업무 계약을 대신하지 않는다.

### 17.3 Requirement-to-evidence map

| 규범 영역 | 결정 | 상세 입력 | Roadmap | 필수 evidence |
|---|---|---|---|---|
| 표준 모델과 입력 | `C-01`, `C-05`~`C-14` | 세션 20 | `RM-1` | normalization, pair/property, boundary/overflow와 directed-matrix evidence |
| 확장 평가 | `C-03`~`C-05`, `C-15`, `C-18` | 세션 21 | `RM-2` | profile isolation, bind rejection, full evaluation와 comparator/guard |
| 탐색과 상태 | `C-06`, `C-08`~`C-09`, `C-13`, `C-16`~`C-17`, `C-22` | 세션 22 | `RM-3`, `RM-4`, `RM-7` | portfolio traces, rollback/cache equivalence, step rerun와 measured state decision |
| 결과와 benchmark | `C-15`, `C-18`, `C-21`~`C-22` | 세션 23 | `RM-5`, `RM-6` | verifier independence, result partition, manifest/cards와 deterministic champion |
| 시스템과 migration | `C-02`, `C-19`~`C-20` | 세션 24 | `RM-0`, `RM-8`, `RM-9` | document audit, logical-port integration, compatibility/rollback와 resume approval |

이 문서의 변경이 문제 의미, invariant, input/output, objective, numeric/time, roadmap gate 또는 논리 책임을 바꾸면 승인된 결정 기록과 영향을 받는 상세 문서를 같은 변경 단위에서 갱신해야 한다. 구현 진행 기록은 이 설계를 대체할 수 없다.
