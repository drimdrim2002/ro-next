# 세션 30 — Master Design 열린 질문 통합

```yaml
status: COMPLETE
version: 1.0-integration
last_updated: 2026-07-23
owner: RPDPTW 설계 통합
scope: 세션 29 사용자 결정의 Master, 질문 등록부와 영향 review input 반영 및 정합성 검증
supersedes: null
related_decisions:
  - 29-open-question-interview.md
```

## 1. 목적과 권위

이 세션은 [세션 29 열린 질문 인터뷰](29-open-question-interview.md)를 끝까지 읽고, 그 문서의 사용자 답변과 명시적 해석만을 권위 있는 결정으로 사용하여 문서를 통합했다. 세션 19의 원래 28개 질문 ID·문장은 보존하되 세션 29와 충돌하는 세션 09~24의 임시값·권장안·TBD를 새 결정으로 사용하지 않았다.

이 작업은 문서 통합만 수행한다.

- 코드, 테스트 코드, build/deploy 파일, fixture, PDF와 다른 data를 수정하지 않는다.
- 공식 수치가 없는 scorer, randomized starts, diverse `K`, light-search budget, round/worker 수, `maxSteps`, watchdog을 만들지 않는다.
- `Q-INFRA-01`, `Q-VAR-01`을 질문하거나 활성화하지 않는다.
- 특정 cloud/service는 logical fan-out/fan-in의 구현 예시일 뿐 목표 topology가 아니다.
- Commit과 push를 수행하지 않는다.

## 2. 질문 상태 결과

| 상태 | 수량 | ID |
|---|---:|---|
| `RESOLVED` | 24 | `Q-NUM-01~03`, `Q-MTX-01~03`, `Q-TIME-01~04`, `Q-IN-01~02`, `Q-COMP-01~02`, `Q-REQ-01~02`, `Q-OBJ-01~03`, `Q-ALG-02`, `Q-RES-01~02`, `Q-BENCH-01`, `Q-BENCH-03` |
| `OPEN — EXPERIMENT_REQUIRED` | 2 | `Q-ALG-01`, `Q-BENCH-02` |
| `DEFERRED` | 2 | `Q-INFRA-01`, `Q-VAR-01` |
| 합계 | 28 | 세션 19의 canonical 질문 전체 |

`Q-ALG-02`의 exact 표기는 `RESOLVED — KEEP_COW`이며 위 `RESOLVED` 수량 24에 포함한다.

## 3. Master 통합 내용

### 3.1 Numeric와 checked arithmetic

- 무게·부피는 `n=3`, 비음수 `FLOOR`다.
- Item을 먼저 정규화하고 정수 `qty`를 곱한다.
- 비용·거리·시간은 정수 입력이며 소수 입력을 거부한다.
- Overflow와 실패를 큰 numeric sentinel로 바꾸지 않는다.

### 3.2 Travel preparation

- 제공된 `D`는 directed integer meter, `U`는 directed integer second다.
- `C`는 비권위이고 self arc는 `0/0`으로 정규화한다.
- 외부 sparse/omitted travel input을 허용하되 solver 전 preparation이 complete directed `M²`를 만든다.
- 누락 `D`는 Great Circle 결과를 integer meter `HALF_UP`, 누락 `U`는 vehicle별 `CEILING(D × 3.6 ÷ speed)` second로 만든다.
- Vehicle speed 누락 시 `45 km/h`를 사용한다.
- Solver/verifier 내부의 lazy fallback은 금지하고 provided/generated source와 policy를 provenance에 남긴다.
- 현재 Win fixture의 소수 `D/U`는 새 정수 계약에 비준수이므로 official baseline을 만들 수 없다.

이 결정은 기존 `C-13`의 “누락 arc 생성 금지”를 대체하는 최신 사용자 override다. Master의 `C-13`, §8, roadmap evidence와 관련 역사 세션 overlay를 함께 갱신했다.

### 3.3 Time, service와 route resource

- Backend가 timezone을 처리하고 solver에는 timezone-less `yyyy-MM-dd HH:mm:ss`를 전달한다.
- Plan은 `[start,end)`, window close는 포함이다.
- Early arrival와 waiting을 기록하고 기본 `START_ONLY`, profile별 `COMPLETE_WITHIN_WINDOW`를 지원한다.
- 날짜 없는 창은 일별 반복하고 `open>close`는 overnight다.
- Arc는 한 work window에 전부 들어갈 때만 출발하며, 아니면 다음 work start에 arc 전체를 처음부터 시작한다.
- `reqDate/dueDate`는 완료기한 별칭이고 `serviceTime=duration+Σ(item.taskTime×qty)`다.
- Depot `taskTime`은 적용하지 않고 후속 rotation의 재출발 경계에서만 depot `duration`을 적용한다.
- `oneway`는 `multiRotation` 값을 무시하고 마지막 고객에서 끝난다. Single `roundtrip + multiRotation=0`은 현재 범위이고 multi-trip은 후속이다.
- Stop은 연속 physical location 전환 기준, drive distance/time은 실제 arc 기준이며 route 전체 누적이다.

### 3.4 Compatibility와 request/route

- Vehicle은 구체 `vehicleFeature` 하나, order는 `vehicleFeatureList`를 사용한다.
- Order의 exact `["ALL"]`만 size wildcard이며 free-form case-sensitive exact code를 사용한다.
- Size와 zone은 AND다. Missing zone은 `"ALL"`이고 route의 `ALL` 제외 구체 zone 집합은 최대 하나다.
- Delivery-only와 real pickup-delivery를 같은 single-trip route에 혼합할 수 있다.
- Oneway와 single roundtrip을 지원하고, 향후 multi-trip에서도 pair가 trip 경계를 넘을 수 없다.

### 3.5 Objective, ownership와 final result

- 고객사에 등록·승인된 preset만 선택하며 고객마다 objective availability가 다를 수 있다.
- Mandatory는 사용하는 preset의 최상위 `mandatoryUnassignedCount` 사전식 차원이다.
- `vhclOwnTyp`은 exact `DIRECT/LEASE`이며 missing/null/empty는 `DIRECT`다.
- `DIRECT/LEASE` vehicle 배정은 모두 `ASSIGNED`다.
- Customer profile은 `regularVehicleVolumeCost`와 필요 시 더 앞선 `outsourcedVehicleVolumeCost`를 used vehicle의 `maxVolume` 합으로 구성한다.
- Solver outcome은 `ASSIGNED/UNASSIGNED`만 사용하고 운영자의 후속 외주·이월은 solver status가 아니다.
- Static `PROVEN`을 제외한 모든 `UNASSIGNED`를 final-solution exhaustive insertion audit한다.
- Audit가 feasible insertion을 찾아도 자동 수정·재탐색하지 않고 내부 audit record만 보존한다.

### 3.6 Initial portfolio, COW와 benchmark

- Comparator상 `best_initial_solution`을 항상 ALNS warm-start에 포함하고 diverse candidates는 추가 집합이다.
- `Q-ALG-01`의 scorer, randomized starts, `K`, light-search budget은 calibration protocol만 확정되었고 실제 공식 수치는 없다.
- `Q-ALG-02`는 `RESOLVED — KEEP_COW`다. Apply/undo는 COW 병목 evidence와 별도 승인 전 기본 경로가 아니다.
- Win 네 번째 metric은 used routes의 `drive + customer/depot wait + service + inter-work-window rest`다.
- Official benchmark는 fixed round plan에서 독립 worker를 검증하고 round champion을 다음 round 공통 warm start로 사용한다.
- 선언 worker가 하나라도 끝내 정상 완료·검증되지 않으면 round와 전체 benchmark는 `INCOMPLETE`다.
- `Q-BENCH-02`의 round/worker 수, `maxSteps`, watchdog은 실험 evidence 전까지 미확정이다.

## 4. 문서 반영 범위

### 4.1 규범·진입 문서

| 문서 | 반영 |
|---|---|
| [Master Design](../master-design.md) | Version 2.1 review로 질문 결정의 의미, override, gate와 traceability 통합 |
| [질문 등록부](../master-design-open-questions.md) | 28개 질문의 status, exact decision, evidence/owner, gate, Master link와 수량 갱신 |
| [문서 지도](../README.md) | 질문 등록부의 현재 역할과 세션 29→30 추적 경로 추가 |
| [세션 인덱스](README.md) | 세션 19~30의 역할과 역사 문서 overlay 설명 추가 |

### 4.2 최소 역사 overlay를 추가한 영향 세션

본문을 현재형으로 전면 재작성하지 않고 문서 상단에 세션 30 overlay를 추가했다.

| 세션 | 이유 |
|---|---|
| 09 | Numeric `n`/rounding/order 및 matrix 추정이 해결된 결정과 충돌 |
| 11 | Time/input/rotation/size schema의 과거 질문·권장안이 해결됨 |
| 12 | No-fallback과 matrix 의미가 최신 preparation 결정으로 대체됨 |
| 13 | Outsourced/deferred status와 audit 대안이 two-state result/audit 결정으로 대체됨 |
| 14 | Mandatory finite penalty, 외주/이월 status 관련 임시 기본 가정이 최신 objective/result 결정과 충돌 |
| 15 | Scorer, starts, `K`, light-search 임시값이 실험 대기 규칙에 어긋남 |
| 16 | Pure drive time, 고정 seed/step/watchdog, rotation/diagonal 임시값이 최신 benchmark 계약과 충돌 |
| 20 | Domain/input draft의 모든 관련 TBD 및 no-fallback/mixed 금지가 해결됨 |
| 21 | Objective/result/total-time TBD와 four-state 방향이 해결됨 |
| 22 | Mixed route, COW/apply-undo, algorithm/benchmark 수치 상태가 변경됨 |
| 23 | Result/audit/matrix/benchmark TBD가 해결되거나 실험 대기로 축소됨 |
| 24 | `OPEN 26` gate map과 apply/undo roadmap 의미가 현재 상태와 충돌 |

### 4.3 조사했으나 수정하지 않은 세션

| 세션 | 판정 |
|---|---|
| 10 | 999 CBM, plan end와 explicit failure 분리는 세션 29 결정과 충돌하지 않음 |
| 17 | Optional variants feasibility는 계속 `DEFERRED`; 활성화·재질문하지 않음 |
| 19 | 원래 C/P/Q baseline과 history를 보존해야 하므로 수정하지 않음 |
| 28 | 세션 29 이전 corpus의 read-only re-review 기록이므로 수정하지 않음 |
| 29 | 권위 있는 사용자 답변 원문이므로 수정하지 않음 |

## 5. 충돌 해결 원칙

1. 세션 29의 exact 답변이 세션 09~24의 임시값보다 우선한다.
2. 역사 문서의 과거 결정을 지우지 않고 상단 overlay로 현재 효력을 제한한다.
3. Master와 질문 등록부에만 현재 규범 의미와 상태를 중복 없이 직접 쓴다.
4. `Q-ALG-01`, `Q-BENCH-02`는 protocol 결정을 “수치 해결”로 확대하지 않는다.
5. Travel preparation의 명시적 generation과 solver/verifier 내부 lazy fallback 금지를 구분한다.
6. Oneway의 rotation 무시와 일반 non-oneway `multiRotation != 0` 거부를 구분한다.
7. `LEASE` vehicle assignment와 운영자의 후속 outsourcing disposition을 구분한다.
8. Logical multi-round fan-out/fan-in과 physical infrastructure topology를 구분한다.

## 6. Validation 결과

| 검사 | 결과 |
|---|---|
| Canonical question identity | 세션 19과 등록부에서 28개 ID·순서·질문 문장 exact match, difference 0 |
| 상태 parser | `RESOLVED 24`, `OPEN — EXPERIMENT_REQUIRED 2`, `DEFERRED 2`, 합계 28 |
| `Q-ALG-02` subtype | `RESOLVED — KEEP_COW`이며 resolved count에 1회 포함 |
| 영향 session overlay | 09, 11~16, 20~24의 12개 대상 모두 상단 overlay 존재 |
| Local link/anchor | 변경·영향 문서 17개에서 199개 검사, 오류 0 |
| Markdown fence | 검사 대상 전체 balanced |
| Trailing whitespace | 0건 |
| `git diff --check` | 오류 0 |
| 금지 약어 | 현재 규범 문서에서 `RPDPDTW`, `rpdpdtw`, `Rpdpdtw` 0건 |
| 숨은 공식 수치 | Master/register/session 30에 scorer, `K`, randomized starts, light-search, round/worker, `maxSteps`, watchdog의 official numeric default 0건 |
| Infrastructure scope | Named provider/product target 0건; logical fan-out/fan-in만 기록 |
| Edit boundary | 위 §4의 17개 문서만 수정·생성; 코드, test, build/deploy, data/PDF/fixture 수정 0건 |

검사 시점의 규범 문서 크기와 SHA-256은 다음과 같다. 이 통합 기록은 자체 내용을 포함하므로 self-hash를 기록하지 않는다.

| 문서 | 줄 수 | SHA-256 |
|---|---:|---|
| `docs/master-design.md` | 943 | `a131a1afae9562faca6127baab4ef5ce48ed78de6e1729d03433641452e55ba6` |
| `docs/master-design-open-questions.md` | 78 | `4e211d27639b24aede4d661f37c7d339a0a05a00eef047e844a9ea29cc8846fc` |

## 7. 남은 항목

- `Q-ALG-01`: scorer 공식, randomized start 수, diverse `K`, light-search work budget의 실제 calibration과 승인.
- `Q-BENCH-02`: round 수, round별 worker 수, warm-start 배정, worker별 `maxSteps`, watchdog의 실제 calibration과 승인.
- 현재 Win fixture의 compliant integer `D/U` 재수령 또는 별도 명시적 계약 변경.
- `Q-INFRA-01`, `Q-VAR-01`은 계속 `DEFERRED`이며 재질문·활성화하지 않는다.

이 세션은 Master와 질문 등록부를 계속 `REVIEW`로 둔다. 통합 완료는 구현, benchmark 실행, official baseline, infrastructure 또는 optional variant 승인을 뜻하지 않는다.
