# 세션 13 — 미배정 탐색 상태와 최종 상태·진단 분리

> **세션 30 통합 상태 (2026-07-23):** 세션 29의 `Q-OBJ-03`, `Q-RES-01~02` 결정이 본문의 외주·이월 대안과 audit 미결정을 대체한다. Solver outcome은 `ASSIGNED`/`UNASSIGNED`만 사용하고 `LEASE` vehicle 배정도 `ASSIGNED`다. 운영자의 후속 외주·이월은 solver status가 아니다. Static `PROVEN`을 제외한 모든 `UNASSIGNED`를 final-solution insertion audit하되 feasible insertion 발견 시 자동 수정/재탐색 없이 내부 record만 남긴다. 현재 계약은 [Master §10](../master-design.md#10-search-solution과-final-result)과 [등록부](../master-design-open-questions.md)를 따른다.

## 확정 구조

현재 설계 문서에는 다음 두 정의가 충돌한다.

- `master-design.md`는 `RequestBank`가 미배정 주문과 미배정 사유를 함께 보관하는 것으로 설명한다.
- `domain-design.md`는 `RequestBank`를 탐색 중 미배정 요청 ID만 관리하는 `BitSet` 래퍼로 정의한다.

마스터 설계 개정 시 두 책임을 다음과 같이 분리한다.

```text
탐색 중 Solution
├─ Route[]
└─ SearchRequestBank
   └─ 현재 어떤 경로에도 들어 있지 않은 requestId 집합

최종 SolverResult
└─ RequestOutcome[]
   ├─ FinalAssignmentStatus
   ├─ 배정 또는 후속 처리 정보
   └─ UnassignmentDiagnostic[]
```

| 구성요소 | 책임 | 포함하지 않는 것 |
|---|---|---|
| `SearchRequestBank` | 탐색 중 어떤 정규 경로에도 속하지 않은 요청의 멤버십 관리 | 최종 업무 상태, 미배정 원인, 사용자 메시지, 외주·이월 결정 |
| `FinalAssignmentStatus` | 솔버 종료 후 각 주문의 최종 업무 처리 상태 표현 | 탐색 중 일시 상태, 실패 원인 |
| `UnassignmentDiagnostic` | 왜 정규 차량 배정에 실패했거나 어려웠는지 근거와 신뢰 수준을 표현 | 최종 처리 상태를 결정하는 권한 |
| 결과 변환기 | 최종 해와 고객사별 후속 처리 정책을 결과 DTO로 변환 | 탐색 해 변경 |

`RequestBank`라는 기존 명칭은 향후 문서에서 `SearchRequestBank`로 바꾼다. 이름 자체로 탐색 전용 상태임을 드러내고 최종 `UNASSIGNED` 결과와 혼동하지 않기 위함이다.

`SearchRequestBank`는 성능을 위해 `BitSet` 또는 동등한 정수 ID 집합으로 표현할 수 있다. 구체 자료구조와 무관하게 다음 원칙을 지켜야 한다.

- 요청 ID의 포함 여부만 저장한다.
- 픽업 노드와 배송 노드를 별도 원소로 저장하지 않고 요청 단위로 관리한다.
- 진단 목록이나 마지막 삽입 실패 이유를 저장하지 않는다.
- 고객사별 정책 타입을 참조하지 않는다.
- 해 복사, 후보 수용, 롤백의 대상에는 반드시 포함된다.

`UnassignmentDiagnostic`은 가변 탐색 해의 일부가 아니다. 다음 두 종류의 근거를 최종 결과 생성 단계에서 합성한 읽기 전용 결과다.

1. 변환·사전검사에서 얻은 정적 진단
2. 최종 해 감사와 탐색 통계에서 얻은 동적 진단

`호환 차량 없음`은 입력 형식 오류가 아니라 정상적인 배차 불가 결과로 취급한다. 존재하지 않는 차량 ID 참조, 잘못된 제약 설정 등 **호환성 정의 자체가 잘못된 경우**만 입력 검증 오류다.

## 탐색 상태

탐색 중 요청은 반드시 다음 둘 중 정확히 한 곳에 존재한다.

```text
정규 Route 한 곳
XOR
SearchRequestBank
```

RPDPTW 요청의 픽업·배송 쌍은 원자적으로 이동한다. 픽업만 경로에 있고 배송은 bank에 있는 중간 상태는 외부에 관찰되어서는 안 된다.

| 전환 | 경로 변경 | `SearchRequestBank` 변경 | 완료 조건 |
|---|---|---|---|
| 초기화 | 모든 경로를 빈 터미널 경로로 생성 | 모든 요청 추가 | 전체 요청이 bank에 정확히 한 번 존재 |
| 삽입 성공 | 같은 차량 경로에 픽업·배송을 함께 삽입 | 해당 요청 제거 | 경로 실행 가능성과 쌍 불변조건 확인 후 한 번에 반영 |
| 삽입 실패 | 변경 없음 | 변경 없음 | 실패 이유를 bank에 기록하지 않음 |
| Destroy | 경로에서 픽업·배송을 함께 제거 | 해당 요청 추가 | 제거와 추가가 하나의 move로 처리됨 |
| 배정 요청 교환 | 기존 요청 제거, 새 요청 삽입 | 기존 요청 추가, 새 요청 제거 | 전체 변경이 성공하거나 전체 롤백 |
| 후보 거절 | 현재 해 변경 없음 | 현재 bank 복원 | route와 bank가 동일 후보 스냅샷으로 함께 롤백 |
| 후보 수용 | 후보 route 채택 | 후보 bank 채택 | 구조 불변조건 검증 후 현재 해 교체 |

탐색 중 어떤 삽입 시도가 실패했다는 사실은 그 주문의 최종 원인이 아니다. 예를 들어 현재 경로에 공간이 없다는 실패는 다른 주문을 재배치하면 해소될 수 있다. 따라서 연산자는 필요할 경우 진단용 **집계 통계**만 별도 수집한다.

```text
requestId
constraintCode
rejectionCount
firstObservedStep
lastObservedStep
```

모든 삽입 후보와 전체 경로를 장기간 그대로 보존하지 않는다. 결과 설명에 필요한 최소 집계만 남겨 탐색 메모리와 진단 메모리를 분리한다.

최종 후보를 결과로 변환하기 전에 다음 파티션 검증을 수행한다.

- 모든 요청은 정규 경로 한 곳 또는 `SearchRequestBank` 중 정확히 한 곳에 존재한다.
- 하나의 요청이 둘 이상의 경로에 존재하지 않는다.
- bank에 있는 요청의 픽업·배송 노드는 어느 경로에도 존재하지 않는다.
- 경로에 있는 요청의 픽업·배송 노드는 같은 차량에 각각 한 번 존재하며 픽업이 먼저다.
- 전체 요청 수는 경로 배정 요청 수와 bank 크기의 합과 같다.

## 최종 상태

최종 결과의 상태는 다음 네 값으로 시작한다.

| `FinalAssignmentStatus` | 의미 | 필수 부가 정보 |
|---|---|---|
| `ASSIGNED` | 현재 계획의 정규 차량 경로에 실제 배정됨 | route ID, vehicle ID, 방문 및 서비스 시각 |
| `UNASSIGNED` | 현재 계획에서 정규 배정되지 않았고 확정된 대체 처리도 없음 | 하나 이상의 진단 또는 `UNDETERMINED` 진단 |
| `DEFERRED` | 현재 계획에서는 제외하고 후속 계획으로 이월하기로 명시적으로 결정됨 | 이월 대상 날짜·계획 ID 또는 이월 정책 식별자 |
| `OUTSOURCED` | 외부 운송 수단으로 처리하기로 명시적으로 결정됨 | 외주 옵션·공급자·가상 차량 식별자 중 적용 가능한 값 |

다음 규칙을 적용한다.

- 정규 경로에 있는 요청은 항상 `ASSIGNED`다.
- 현재 기본 기능에서 최종 `SearchRequestBank`에 남은 요청은 기본적으로 `UNASSIGNED`다.
- 진단만으로 `DEFERRED` 또는 `OUTSOURCED`를 추론하지 않는다. 두 상태는 입력 지시, 고객사 정책 또는 탐색이 선택한 명시적 대체 옵션이 있어야 한다.
- “외주가 필요할 수 있음”은 `OUTSOURCED`가 아니다. 상태는 `UNASSIGNED`로 두고 권고 행동에 `CONSIDER_OUTSOURCING`을 담는다.
- “다음 계획에서 다시 시도할 수 있음”도 `DEFERRED`가 아니다. 이월이 실제로 확정되었을 때만 `DEFERRED`로 바꾼다.
- `DUMMY`는 최종 업무 상태로 노출하지 않는다. 내부 dummy가 있다면 목적이 명확한 상태로 변환한다.
- 모든 입력 주문은 최종 결과에 정확히 하나의 `RequestOutcome`을 가져야 한다.

최종 상태와 진단은 독립적이다. 예를 들어 `OUTSOURCED` 주문도 정규 차량에 배정되지 못한 이유를 진단으로 가질 수 있고, `UNASSIGNED` 주문의 진단이 `SEARCH_BUDGET_EXHAUSTED`뿐일 수도 있다.

## 사유의 신뢰 수준

단일 문자열 `reason`으로 원인을 확정하지 않는다. 한 요청에는 여러 후보 사유가 있을 수 있으며 각 진단은 코드, 적용 범위, 근거 출처, 신뢰 수준을 가진다.

```text
UnassignmentDiagnostic
├─ code
├─ scope
├─ confidence
├─ source
├─ constraintId (선택)
└─ evidence (선택)
```

### 신뢰 수준

| `confidence` | 의미 | 사용 예 |
|---|---|---|
| `PROVEN` | 경로 순서나 휴리스틱 탐색 품질과 무관하게 해당 요청의 정규 배정 불가능성이 논리적으로 확정됨 | 허용 차량 집합이 비어 있음, 요청 수요가 모든 허용 차량의 단일 차량 용량을 초과함, 요청 자체의 시간창이 계획기간 밖에 있음 |
| `EXHAUSTIVE_FOR_FINAL_SOLUTION` | 확정된 최종 해를 고정한 상태에서 모든 허용 차량과 모든 합법 삽입 위치를 검사했으나 실패함 | 최종 경로에 대한 전체 삽입 감사 결과 용량·시간창·근무시간 제약에 모두 막힘 |
| `SEARCH_OBSERVED` | 휴리스틱 탐색 과정에서 반복 관찰되었으나 다른 해 구성에서도 불가능하다고 증명하지 못함 | 시간창 거절이 가장 자주 관찰됨, 최대 거리 거절이 반복됨 |
| `UNKNOWN` | bank에 남았다는 사실 외에 신뢰할 만한 직접 원인을 정하지 못함 | step 또는 안전 시간 제한 종료 시 충분한 삽입 근거가 없음 |

`EXHAUSTIVE_FOR_FINAL_SOLUTION`은 전역 불가능 증명이 아니다. 다른 주문을 다른 차량으로 옮기면 해당 주문이 배정될 수 있으므로 사용자 메시지도 “현재 최종 경로 기준 삽입 불가”로 표현한다.

### 적용 범위

| `scope` | 의미 |
|---|---|
| `REQUEST` | 주문 하나에 대해 성립하는 진단 |
| `COMPATIBILITY_GROUP` | 같은 허용 차량 집합을 공유하는 주문군의 수요·공급 압력 |
| `ZONE` | 지역 단위 수요·차량 부족 |
| `FLEET` | 전체 차량군 부족 또는 계획 전반의 제약 |

“zone 수요가 zone 차량 용량보다 큼”은 주문군에 대한 확정 진단일 수는 있지만, 그 zone의 특정 주문이 반드시 미배정이어야 한다는 증명은 아니다. 따라서 이를 각 주문의 `PROVEN CAPACITY_SHORTAGE`로 복제하지 않고 그룹 진단을 참조하게 한다.

### 기본 진단 코드

다음 코드는 마스터 설계의 기본 vocabulary로 사용한다.

| 코드 | 일반적인 최고 신뢰 수준 | 설명 |
|---|---|---|
| `NO_ELIGIBLE_VEHICLE` | `PROVEN` | 크기 유형, zone 또는 다른 hard compatibility 제약을 모두 적용한 허용 차량 집합이 비어 있음 |
| `DEMAND_EXCEEDS_EVERY_ELIGIBLE_VEHICLE` | `PROVEN` | 주문 하나의 무게·부피가 모든 허용 차량의 용량을 초과함 |
| `OUTSIDE_PLANNING_HORIZON` | `PROVEN` | 요청의 허용 서비스 시각이 계획기간과 교차하지 않음 |
| `NO_VEHICLE_TIME_OVERLAP` | `PROVEN` 또는 `EXHAUSTIVE_FOR_FINAL_SOLUTION` | 요청 시간창과 허용 차량의 가용시간이 교차하지 않음. 이동을 고려해야 확정되는 경우 신뢰 수준을 낮춤 |
| `NO_FEASIBLE_INSERTION_IN_FINAL_SOLUTION` | `EXHAUSTIVE_FOR_FINAL_SOLUTION` | 최종 해의 모든 합법 삽입 위치를 감사했으나 성공하지 못함 |
| `CAPACITY_PRESSURE` | `SEARCH_OBSERVED` | 탐색 중 용량 거절이 반복 관찰됨 |
| `TIME_WINDOW_PRESSURE` | `SEARCH_OBSERVED` | 탐색 중 시간창 거절이 반복 관찰됨 |
| `WORKING_TIME_PRESSURE` | `SEARCH_OBSERVED` | 근무시간 제약 거절이 반복 관찰됨 |
| `MAX_DISTANCE_PRESSURE` | `SEARCH_OBSERVED` | 최대 거리 제약 거절이 반복 관찰됨 |
| `MAX_DRIVE_TIME_PRESSURE` | `SEARCH_OBSERVED` | 최대 주행시간 제약 거절이 반복 관찰됨 |
| `MAX_STOPS_PRESSURE` | `SEARCH_OBSERVED` | 최대 정차 수 제약 거절이 반복 관찰됨 |
| `CUSTOM_CONSTRAINT_REJECTION` | 근거에 따라 다름 | 고객사 확장 제약이 배정을 거절함 |
| `SEARCH_BUDGET_EXHAUSTED` | `UNKNOWN` | step 제한 또는 보조 시간 제한까지 배정되지 못함 |
| `UNDETERMINED` | `UNKNOWN` | 더 구체적인 근거가 없음 |

고객사별 제약은 코어 enum을 매번 수정하지 않도록 안정된 namespaced code를 제공할 수 있어야 한다. 예를 들면 `customerA:restricted_road_window`와 같다. 결과 계약은 알 수 없는 확장 코드를 보존하며, 사용자 메시지는 코드와 분리해 현지화한다.

여러 진단 중 대표 사유가 필요한 화면에서는 다음 순서로 선택한다.

1. 높은 신뢰 수준
2. 더 좁고 직접적인 적용 범위(`REQUEST` 우선)
3. 고객사별 표시 우선순위
4. 안정된 진단 코드 순서

대표 사유는 표현 편의를 위한 파생값이며 전체 진단 목록을 대체하지 않는다.

## 외주·이월 확장

외주와 이월은 단순한 실패 사유가 아니라 비용과 운영 결과가 다른 **대체 처리 결정**이다. 따라서 `UnassignmentDiagnostic`의 reason 값으로 표현하지 않고 `FinalAssignmentStatus`로 표현한다.

확장은 두 모드를 구분한다.

### 결과 분류 모드

솔버는 정규 차량만 최적화하고, 종료 후 고객사별 `FinalDispositionPolicy`가 bank 요청을 `UNASSIGNED`, `DEFERRED`, `OUTSOURCED` 중 하나로 분류한다.

- 외주·이월이 정규 배차의 목적함수나 탐색 선택에 영향을 주지 않을 때 사용한다.
- 정책 추가로 코어 탐색 구조를 바꾸지 않아도 된다.
- 정책은 진단을 참고할 수 있지만 실제 처리에 필요한 정보가 없으면 `UNASSIGNED`를 반환해야 한다.

### 최적화 결정 모드

외주 비용, 이월 패널티, 서비스 수준을 정규 배차와 함께 최적화하려면 외주·이월을 명시적인 대체 옵션으로 탐색 상태와 점수 정책에 포함한다.

```text
요청의 탐색 위치
= 정규 Route
  또는 명시적 FallbackAssignment(OUTSOURCE/DEFER)
  또는 SearchRequestBank
```

이 경우에도 `SearchRequestBank`의 책임은 바뀌지 않는다. 별도 fallback 상태가 대체 처리 옵션, 비용, 공급자 또는 목표 날짜를 보관하며, 고객사별 `ScorePolicy`가 상태별 비용을 계산한다.

내부 dummy vehicle을 사용할 때는 다음을 지킨다.

- dummy 하나를 미배송·외주·이월·추가차의 공통 의미로 사용하지 않는다.
- dummy 또는 가상 차량에는 `OUTSOURCE`, `DEFER`, `EXTRA_VEHICLE_SIMULATION`처럼 용도가 명시되어야 한다.
- 추가 차량 시뮬레이션은 실제 외주 확정과 구분한다.
- 최종 DTO에는 dummy vehicle 자체가 아니라 업무 상태와 실제 근거를 노출한다.

이월 주문은 현재 계획에서 `ASSIGNED`로 계산하지 않는다. 후속 계획을 만들 때는 원 주문 ID 또는 연계 ID를 유지한 새 입력으로 들어가며 중복 계획을 방지할 수 있어야 한다.

## 결과 DTO 요구

결과는 배정 성공 목록과 `unassignedOrders`만 별도로 제공하기보다 모든 입력 주문에 대한 단일 `requestOutcomes` 목록을 기준 계약으로 삼는다.

```json
{
  "schemaVersion": "1",
  "requestOutcomes": [
    {
      "orderId": "ORDER-001",
      "status": "UNASSIGNED",
      "assignment": null,
      "fallback": null,
      "diagnostics": [
        {
          "code": "NO_ELIGIBLE_VEHICLE",
          "scope": "REQUEST",
          "confidence": "PROVEN",
          "source": "PRECHECK",
          "constraintId": "vehicle-size-zone-compatibility",
          "evidence": {
            "eligibleVehicleCount": 0
          }
        }
      ],
      "recommendedActions": ["CONSIDER_OUTSOURCING"]
    }
  ],
  "summary": {
    "total": 100,
    "assigned": 82,
    "unassigned": 12,
    "deferred": 4,
    "outsourced": 2
  },
  "diagnosticGroups": []
}
```

필드 요구사항은 다음과 같다.

- `orderId`: 외부 입력 주문 ID. 내부 request ID만 노출하지 않는다.
- `status`: 네 가지 `FinalAssignmentStatus` 중 하나다.
- `assignment`: `ASSIGNED`일 때 route·vehicle·방문시각 정보를 제공한다.
- `fallback`: `DEFERRED` 또는 `OUTSOURCED`일 때 확정된 대체 처리 정보를 제공한다.
- `diagnostics`: 복수 허용. `UNASSIGNED`인데 진단을 만들지 못했으면 `UNDETERMINED`를 포함한다.
- `recommendedActions`: 상태가 아니라 후속 조치 제안이다.
- `diagnosticGroups`: zone·호환 차량군·fleet 수준 진단을 한 번만 기록하고 요청 결과에서 해당 group ID를 참조할 수 있게 한다.
- `summary`: 상태별 건수 합이 `total`과 같아야 한다.
- 실행 메타데이터에는 seed, 수행 step 수, 종료 사유(`STEP_LIMIT`, `SAFETY_TIME_LIMIT`, `COMPLETED`)를 포함해 `UNKNOWN` 진단을 해석할 수 있게 한다.

기존 소비자가 `unassignedOrders` 형식을 요구한다면 `requestOutcomes`에서 `status == UNASSIGNED`인 항목만 투영한 호환 필드로 제공한다. 두 목록을 독립적으로 생성해서 불일치할 수 있게 해서는 안 된다.

진단 `message`는 API의 판정 근거가 아니다. 안정된 `code`를 계약으로 사용하고 한국어·영어 문구는 별도 메시지 변환 계층에서 생성한다. `evidence`에는 전체 탐색 이력이나 과도한 내부 상태를 넣지 않고 설명에 필요한 제한된 수치만 넣는다.

## 테스트

### 탐색 상태 불변조건

- 초기화 직후 모든 요청이 `SearchRequestBank`에 존재한다.
- 성공한 픽업·배송 쌍 삽입 후 해당 요청만 bank에서 제거된다.
- Destroy 후 픽업·배송 두 노드가 함께 제거되고 요청이 bank에 복귀한다.
- 삽입 실패, 후보 거절, undo 후 route와 bank가 원래 상태와 일치한다.
- 임의 move 연속 수행 후에도 모든 요청에 대해 `route XOR bank`가 성립한다.
- 동일 요청의 중복 경로 배정, 픽업·배송 분리, 경로와 bank 동시 포함을 탐지한다.

### 최종 상태 변환

- 정규 경로 요청은 모두 `ASSIGNED`로 변환된다.
- 기본 정책에서 최종 bank 요청은 모두 `UNASSIGNED`로 변환된다.
- 명시적 외주·이월 결정 없이 `OUTSOURCED`나 `DEFERRED`가 생성되지 않는다.
- 모든 입력 주문에 정확히 하나의 결과가 있으며 상태별 합계가 전체 주문 수와 같다.
- `OUTSOURCED`와 `DEFERRED`에 필요한 부가 정보가 없으면 결과 검증이 실패한다.

### 진단 정확성

- 허용 차량 집합이 빈 요청은 `NO_ELIGIBLE_VEHICLE / PROVEN`이 된다.
- 단일 주문 수요가 모든 허용 차량 용량을 넘을 때만 요청 수준 `DEMAND_EXCEEDS_EVERY_ELIGIBLE_VEHICLE / PROVEN`을 생성한다.
- zone 또는 호환 그룹 총수요 초과를 특정 주문의 확정 원인으로 잘못 변환하지 않는다.
- 최종 해 삽입 감사 없이 `EXHAUSTIVE_FOR_FINAL_SOLUTION`을 생성하지 않는다.
- 탐색 중 거절 횟수만으로 `PROVEN`을 생성하지 않는다.
- 구체 원인이 없을 때 `UNDETERMINED` 또는 `SEARCH_BUDGET_EXHAUSTED / UNKNOWN`을 생성한다.
- 고객사 확장 constraint code가 코어 변경 없이 결과에 보존된다.

### DTO와 회귀

- `requestOutcomes`와 호환용 `unassignedOrders` 투영 결과가 일치한다.
- 상태별 summary가 실제 목록과 일치한다.
- 동일 입력·설정·시드·step 제한에서 진단 code와 대표 사유 선택도 결정적이다.
- 보조 시간 제한 종료 결과에는 종료 사유가 기록되며 과도한 확정 진단을 만들지 않는다.
- 알 수 없는 확장 진단 코드가 역직렬화 과정에서 유실되지 않는다.

## 남은 질문

1. `OUTSOURCED`는 실제 공급자 또는 외주 차량까지 확정된 상태만 뜻하는가, 아니면 “외주 필요” 판정도 포함하는가? 이 문서는 전자를 권장하며 후자는 `UNASSIGNED + CONSIDER_OUTSOURCING`으로 분리했다.
2. `DEFERRED`의 최소 확정 정보는 무엇인가? 구체 날짜, 다음 계획 ID, 또는 고객사 이월 정책 ID 중 어느 수준까지 있어야 이월 확정으로 볼지 정해야 한다.
3. 외주·이월을 초기에는 결과 분류로만 처리할지, 첫 구현부터 비용을 포함한 최적화 선택지로 넣을지 정해야 한다. 선택에 따라 fallback 탐색 상태의 구현 Phase가 달라진다.
4. 기존 외부 결과 계약에 `unassignedOrders[{orderId, reason}]`가 이미 사용되고 있는가? 사용 중이면 호환 투영 필드의 유지 기간과 schema version 정책이 필요하다.
5. 고객사별 진단 코드를 외부 API에 그대로 노출할 수 있는가? 보안 또는 운영 정보 노출 우려가 있다면 외부 공개 code와 내부 constraint ID를 분리해야 한다.
6. 최종 해 전체 삽입 감사는 모든 미배정 요청에 항상 수행할지, 설명이 필요한 요청 또는 상위 우선순위 요청에만 수행할지 정해야 한다. 전자는 설명 품질이 높지만 종료 후 계산 비용이 증가한다.
7. zone·호환 그룹·fleet 수준 진단을 결과 DTO에 정식 포함할지, 운영 분석 보고서의 별도 계약으로 둘지 정해야 한다.
8. 한 주문의 대표 사유가 여러 개일 때 고객사별 표시 우선순위를 설정으로 둘지, 전체 진단 목록만 제공하고 UI에서 선택하게 할지 정해야 한다.
