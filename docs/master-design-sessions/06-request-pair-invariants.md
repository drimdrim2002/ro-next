# 세션 06 — 요청 쌍 불변조건과 연산자 계약

> 범위: `master-design.md`의 RPDPTW 요청 쌍, `RequestBank`, Destroy/Repair 계약 보강  
> 작업 종류: 향후 구현을 위한 문서 설계만 수행하며 코드는 변경하지 않는다.  
> 참고 문서: `docs/master-design.md`, `docs/orgin/alns_pdptw_paper_summary_ko.md`, `docs/arranged/01_problem_definition.md`, `docs/domain-design.md`

## 1. 문제의 정확한 의미

사용자가 확인한 조건들은 모두 맞다. 이전 검토에서 지적한 문제는 조건의 내용이 틀렸다는 뜻이 아니라, 현재 마스터 설계가 그 조건들을 **모든 해 상태와 모든 연산자에서 지켜야 하는 불변조건으로 충분히 명시하지 않았다**는 뜻이다.

PDPTW의 한 `Request`는 서로 독립적인 두 방문이 아니라 하나의 운송 작업이다.

```text
Request r = (pickupNode(r), deliveryNode(r))
```

배정된 요청은 다음을 동시에 만족해야 한다.

1. 픽업과 배송이 모두 존재한다.
2. 두 노드는 같은 차량 경로에 존재한다.
3. 각 노드는 전체 해에서 정확히 한 번 존재한다.
4. 그 경로에서 픽업이 배송보다 앞선다.

배정되지 않은 요청은 다음을 동시에 만족해야 한다.

1. 픽업과 배송이 어떤 차량 경로에도 존재하지 않는다.
2. 요청 ID가 `RequestBank`에 정확히 한 번 존재한다. `BitSet`을 사용하면 멤버십은 자연스럽게 0 또는 1이다.

논문 요약도 이를 별개의 핵심 제약으로 설명한다.

- 요청 처리 또는 보류: 각 요청은 차량이 처리하거나 `RequestBank`에 들어간다.
- 같은 차량 조건: 한 요청의 픽업과 배송은 같은 차량이 수행한다.
- 선후관계: 픽업은 배송보다 먼저 수행된다.
- 제거와 삽입의 단위: 노드가 아니라 요청이다.

`01_problem_definition.md` 역시 PDP를 CVRPTW와 구분하는 핵심으로 pickup-delivery pairing, precedence, same-vehicle 제약을 든다. 따라서 이 규칙들은 고객사별 선택 제약이 아니라 RPDPTW 코어의 공통 구조 제약이다.

현재 마스터 설계에는 이미 다음 내용이 있다.

- 주문 삽입 검사에서 픽업 위치가 배송 위치보다 앞인지 검사한다.
- 초기해에서 모든 주문은 경로 또는 `RequestBank` 중 한 곳에 있어야 한다고 적혀 있다.
- 속성 기반 테스트에 같은 취지의 항목이 있다.

그러나 이 문구만으로는 아래의 잘못된 해를 명시적으로 배제하지 못한다.

| 잘못된 상태 | 기존 문구만으로 부족한 이유 |
|---|---|
| 차량 A에 픽업, 차량 B에 배송 | 두 노드 모두 “경로”에 있으므로 단순 경로/Bank 구분만으로 탐지되지 않을 수 있다. |
| 픽업만 경로에 있고 배송은 없음 | “주문이 경로에 있다”의 판정 기준이 정의되어 있지 않다. |
| 같은 픽업 노드가 두 경로에 중복 | 정확히 한 번이라는 계수 조건이 없다. |
| 경로에 픽업·배송이 있고 `RequestBank`에도 요청이 있음 | 경로 집합과 Bank의 배타성이 형식화되어 있지 않다. |
| Destroy 중 픽업만 먼저 제거된 상태를 점수 계산기가 관찰 | 제거의 원자성과 관찰 가능한 상태 경계가 정의되어 있지 않다. |
| Repair가 픽업 삽입 후 배송 삽입에 실패하고 픽업을 남김 | 부분 실패 시 롤백 계약이 없다. |

따라서 마스터 설계에는 “검사 항목 목록”뿐 아니라 요청 상태의 허용 형태와 상태 전이 계약을 규범적으로 추가해야 한다.

## 2. 확정 불변조건

### 2.1 요청 상태의 배타적 두 형태

요청 집합을 `R`, 차량 경로 집합을 `Routes`, `RequestBank`의 요청 집합을 `B`라고 한다. 요청 `r`에 대해 전체 경로에서 픽업과 배송 노드의 출현 횟수를 각각 `pickupCount(r)`, `deliveryCount(r)`로 정의한다.

모든 요청은 다음 두 상태 중 정확히 하나여야 한다.

```text
ASSIGNED(r)
= pickupCount(r) = 1
  AND deliveryCount(r) = 1
  AND pickup과 delivery가 동일한 vehicleId의 Route에 존재
  AND pickupIndex(r) < deliveryIndex(r)
  AND r NOT IN B

UNASSIGNED(r)
= pickupCount(r) = 0
  AND deliveryCount(r) = 0
  AND r IN B

모든 r에 대해 ASSIGNED(r) XOR UNASSIGNED(r)
```

이 정의로 다음 조건이 함께 확정된다.

| 불변조건 ID | 규칙 | 의미 |
|---|---|---|
| `REQ-INV-01` | 완전성 | 모든 요청은 `ASSIGNED` 또는 `UNASSIGNED` 중 하나다. 어느 쪽에도 없는 요청은 허용하지 않는다. |
| `REQ-INV-02` | 배타성 | 경로에 배정된 요청은 `RequestBank`에 있을 수 없다. Bank의 요청 노드는 경로에 있을 수 없다. |
| `REQ-INV-03` | 쌍 완전성 | 픽업만 또는 배송만 배정된 반쪽 요청은 허용하지 않는다. |
| `REQ-INV-04` | 정확히 한 번 | 배정된 요청의 픽업과 배송은 전체 해에서 각각 정확히 한 번 나타난다. |
| `REQ-INV-05` | 같은 차량 | 한 요청의 픽업과 배송은 동일한 `vehicleId`의 경로에 존재한다. |
| `REQ-INV-06` | 선행관계 | 같은 경로의 최종 노드 순서에서 픽업 인덱스가 배송 인덱스보다 작다. |
| `REQ-INV-07` | 요청 단위 상태 전이 | 삽입, 제거, 이동, 교환, 롤백은 요청 쌍 전체를 한 단위로 처리한다. |
| `REQ-INV-08` | 안정 상태의 관찰 가능성 | 평가기, 점수 정책, acceptance, observer, 결과 변환기는 위 불변조건을 만족한 해만 관찰한다. |

### 2.2 문제 인스턴스의 정적 불변조건

탐색을 시작하기 전에 `ProblemInstance`도 다음을 만족해야 한다.

| 불변조건 ID | 규칙 |
|---|---|
| `REQ-MODEL-01` | 모든 `Request`는 유효하고 서로 다른 `pickupNodeId`, `deliveryNodeId`를 가진다. 같은 물리 좌표를 사용하더라도 내부 노드 ID는 구분한다. |
| `REQ-MODEL-02` | `pickupNodeId`는 `PICKUP`, `deliveryNodeId`는 `DELIVERY` 타입이다. |
| `REQ-MODEL-03` | 하나의 pickup/delivery 노드는 정확히 하나의 요청에만 소속된다. 터미널 노드는 어떤 요청에도 소속되지 않는다. |
| `REQ-MODEL-04` | 픽업 적재 변화량은 요청 수량과 같고 배송 적재 변화량은 그 반대여서, 요청 쌍의 무게·부피 순변화량은 0이다. |
| `REQ-MODEL-05` | `Request.id`와 내부 인덱스의 대응은 고정되며, 요청-노드 매핑은 `ProblemInstance` 생성 후 변경하지 않는다. |

`Node.precedence` 하나만으로는 same-vehicle, exactly-once, Bank 배타성을 표현할 수 없다. 쌍 관계의 기준 정보는 `Request.pickupNodeId`와 `Request.deliveryNodeId`이며, `precedence`는 전방 계산 또는 일반 선행관계 확장에 쓰더라도 위 요청 불변조건을 대체하지 않는다.

### 2.3 마스터 설계에 넣을 규범 문구

다음 문구를 `Request`, `Solution`, `RequestBank` 설명과 주문 삽입 실행 가능성 검사 앞부분에 공통 규칙으로 반영한다.

> **요청 쌍 불변조건**  
> RPDPTW에서 배정과 미배정의 단위는 개별 노드가 아니라 `Request`다. 배정된 요청의 pickup과 delivery는 동일 차량 경로에 각각 정확히 한 번 존재하며 pickup이 delivery보다 앞선다. 미배정 요청의 두 노드는 어떤 경로에도 존재하지 않고 요청 ID만 `RequestBank`에 존재한다. 모든 요청은 이 두 상태 중 정확히 하나여야 한다. 코어 연산자는 부분 요청 상태를 외부에 노출해서는 안 된다.

## 3. 연산자 계약

### 3.1 공통 계약

모든 Construction, Destroy, Repair, Local Search, Fleet Minimizer 연산자는 노드가 아니라 `requestId`를 입력 단위로 사용한다. 연산자의 공개 계약은 다음과 같다.

```text
사전조건:
- 입력 Solution은 요청 쌍 불변조건을 만족한다.

성공 후조건:
- 출력 Solution은 요청 쌍 불변조건을 만족한다.
- 변경 대상이 아닌 요청의 배정 상태와 노드 순서는 보존된다.
- 변경된 Route의 캐시, Solution 점수, structuralHash는 무효화 또는 재계산된다.

실패 후조건:
- 입력 Solution의 Route, RequestBank, 캐시, 점수, structuralHash가 호출 전과 동일하다.
- 반쪽 요청이나 중복 요청을 남기지 않는다.
```

여기서 “원자적”은 병렬 처리나 데이터베이스 transaction을 뜻하지 않는다. 구현 내부에서 두 배열 원소를 순차적으로 바꿀 수는 있지만, 호출자와 평가 계층이 관찰하는 시점에는 요청 쌍 전체의 변경이 성공했거나 변경 전 상태로 완전히 복구되어 있어야 한다는 뜻이다.

후보 위치의 `pickupPosition`, `deliveryPosition`은 **두 노드를 모두 삽입한 최종 경로에서의 인덱스**로 정의하고 항상 다음을 만족시킨다.

```text
pickupPosition < deliveryPosition
```

이 정의를 사용하면 첫 노드 삽입으로 뒤쪽 인덱스가 이동하는 경우의 구현별 해석 차이를 없앨 수 있다.

### 3.2 Construction 계약

초기해 생성기는 모든 요청을 우선 `RequestBank`에 둔 유효한 해에서 시작한다. 요청을 배정할 때 pair insertion을 사용한다.

```text
초기 상태:
- 모든 Route는 시작/종료 터미널만 포함
- RequestBank = 전체 요청 집합

요청 삽입 성공:
- 같은 Route에 pickup과 delivery를 함께 삽입
- pickupPosition < deliveryPosition
- 실행 가능성 검사 통과
- 그 후에만 RequestBank에서 requestId 제거

요청 삽입 실패:
- Route 변경 없음
- requestId는 RequestBank에 유지
```

초기해 생성 완료 시 각 요청은 배정 또는 미배정 상태 중 정확히 하나여야 한다.

### 3.3 Destroy 계약

Destroy 연산자는 배정된 **요청**을 선택하며, `q`는 제거할 노드 수가 아니라 제거할 요청 수다.

```text
removeRequest(requestId):
1. 요청의 pickup과 delivery가 같은 Route에 각각 한 번 있는지 확인
2. 두 노드를 해당 Route에서 함께 제거
3. requestId를 RequestBank에 추가
4. Route 캐시와 Solution 집계를 무효화
5. 요청 쌍 불변조건을 확인한 뒤 변경을 공개
```

다음 동작은 금지한다.

- pickup 또는 delivery 하나만 선택하거나 제거
- 두 노드 중 하나를 찾지 못했는데 나머지만 제거
- 노드는 제거했지만 `RequestBank`에 요청을 추가하지 않음
- 이미 Bank에 있는 요청을 다시 제거 대상으로 선택
- 동일 요청을 한 Destroy 호출에서 중복 선택

`WorstRemoval`, `ShawRelatedRemoval`, `ClusterRemoval`, `TimeOrientedRemoval`, historical removal 모두 요청 ID 집합을 반환해야 한다. 관련성이나 비용 기여도는 pickup과 delivery를 포함한 요청 전체를 기준으로 계산한다.

### 3.4 Repair 계약

Repair 연산자는 `RequestBank`의 요청을 선택하고, 한 차량 경로 안에서 pickup 위치와 delivery 위치의 조합을 평가한다.

```text
insertRequest(requestId, vehicleId, pickupPosition, deliveryPosition):
1. requestId가 RequestBank에 있고 어떤 Route에도 노드가 없는지 확인
2. 동일 Route의 pickupPosition < deliveryPosition 후보를 구성
3. 시간, 용량, 차량 호환성, 경로 자원, 추가 제약을 전체 검사
4. 가능한 경우 두 노드를 함께 반영
5. 반영 성공 후에만 RequestBank에서 requestId 제거
```

삽입 평가 자체는 원본 해를 변경하지 않거나, 임시 변경을 사용한다면 반드시 완전히 undo해야 한다. pickup 삽입은 성공했지만 delivery 삽입 또는 실행 가능성 검사가 실패한 경우 pickup도 제거하고 Bank 멤버십을 유지한다.

Greedy와 Regret 연산자의 “삽입 후보” 하나는 다음 전체 튜플을 의미한다.

```text
InsertionCandidate(
    requestId,
    vehicleId,
    pickupPosition,
    deliveryPosition,
    feasibility,
    objectiveDelta
)
```

pickup 후보와 delivery 후보를 서로 독립적인 주문처럼 순위화하지 않는다.

### 3.5 이동·교환·차량 제거·롤백 계약

- 요청을 다른 차량으로 이동할 때는 원래 경로에서 두 노드를 제거하고 대상 경로에 두 노드를 삽입하는 하나의 request move로 처리한다.
- 두 요청 교환도 두 요청 쌍 전체를 대상으로 하며, 한쪽만 성공한 상태를 남기지 않는다.
- `FleetMinimizer`는 제거할 차량 경로의 모든 `requestId`를 Bank로 옮긴 뒤 재삽입한다. 하나라도 목표 조건을 충족하지 못해 차량 제거를 취소하면 Route와 Bank 전체를 직전 성공 해로 복구한다.
- candidate가 Simulated Annealing에서 거절되면 Route뿐 아니라 Bank 멤버십, 캐시, 점수, hash도 이전 current solution과 같아야 한다.
- 향후 SDVRP처럼 분할 배송을 지원할 때는 별도 요청/수량 상태 모델을 설계한다. 현재 RPDPTW 불변조건을 느슨하게 만들어 부분 요청을 허용하지 않는다.

### 3.6 마스터 설계의 삽입 검사표 개정안

현재 주문 삽입 실행 가능성 검사표 앞에 구조 검사를 배치한다.

| 순서 | 분류 | 검사 항목 |
|---:|---|---|
| 1 | 요청 상태 | 삽입 대상 요청이 `RequestBank`에 있고 두 노드가 어느 경로에도 없는가 |
| 2 | 쌍 완전성 | pickup과 delivery를 함께 삽입하는 후보인가 |
| 3 | 같은 차량 | 두 노드가 동일한 `vehicleId`의 Route에 삽입되는가 |
| 4 | 정확히 한 번 | 삽입 후 pickup과 delivery가 전체 해에서 각각 한 번만 존재하는가 |
| 5 | 선행관계 | 최종 Route에서 `pickupPosition < deliveryPosition`인가 |
| 6 | 차량 호환성 | 차량이 해당 요청을 처리할 수 있는가 |
| 7 | 시간 | 노드 시간창과 차량 근무시간을 만족하는가 |
| 8 | 용량 | 전 구간에서 무게·부피 용량을 만족하는가 |
| 9 | 경로 자원 | 최대 정차 수, 주행거리, 주행시간을 만족하는가 |
| 10 | 확장 제약 | 고객사별 `RouteConstraint`를 모두 만족하는가 |

## 4. 검증 시점

요청 쌍 검증은 단일 위치의 사후 검사에만 의존하지 않는다. 입력 구조, 연산자 국소 후조건, 후보해 전체 검증을 계층적으로 사용한다.

| 검증 시점 | 범위 | 필수 검사 | 실패 처리 |
|---|---|---|---|
| `ProblemInstance` 생성 직후 | 전체 요청·노드 | `REQ-MODEL-01`~`05` | 입력/변환 오류로 즉시 중단 |
| 초기 빈 해 생성 직후 | 전체 해 | 모든 요청이 Bank에 있고 경로에는 터미널만 존재 | 구현 오류로 즉시 중단 |
| pair insertion/removal 평가 전 | 대상 요청·대상 경로 | 연산자 사전조건 | 해당 move를 invalid로 처리하거나 구현 오류로 중단 |
| pair insertion/removal 반영 직후 | 대상 요청·변경 경로·Bank | 연산자 후조건, 구조 불변조건 | 즉시 rollback 후 구현 오류 기록 |
| Destroy 단계 완료 | 전체 해 | 제거 요청이 모두 Bank에 있고 경로에 노드가 없음 | candidate 폐기 |
| Repair 단계 완료 | 전체 해 | 모든 요청에 대한 `ASSIGNED XOR UNASSIGNED` | candidate 폐기 |
| 점수 계산 및 acceptance 직전 | candidate 전체 해 | 구조 불변조건과 경로 실행 가능성 | 점수 계산/수용 금지 |
| current/best 교체 직전 | candidate 전체 해 | 구조 불변조건, 점수 일관성 | 교체 금지 |
| Fleet Minimizer 성공/실패 처리 후 | 전체 해 | 성공 해 또는 rollback 해의 구조 일치 | 단계 실패 처리 |
| 결과 DTO 변환 직전 | 최종 전체 해 | 모든 요청의 배정 상태, 중복/누락 없음 | 결과 출력 금지 |

검증 비용을 고려한 실행 원칙은 다음과 같이 확정한다.

- 테스트와 개발 모드: 모든 연산자 반영 후 전체 `Solution` 불변조건을 검사한다.
- 운영 모드: 각 move에서 변경 요청·경로의 국소 후조건을 항상 검사하고, Destroy 완료, Repair 완료, candidate 수용 전, best 갱신 전, 결과 출력 전에는 전체 검증을 수행한다.
- 성능 최적화로 검증 빈도를 낮추더라도 best 갱신 전과 결과 출력 전의 전체 검증은 생략하지 않는다.
- `SolutionObserver`와 로그 hook은 pair mutation 도중 호출하지 않고 안정 상태에서만 호출한다.

### 4.1 검증 결과 코드

문서와 테스트에서 동일한 실패 의미를 사용하도록 최소한 다음 구조 오류 코드를 둔다.

| 오류 코드 | 의미 |
|---|---|
| `REQUEST_MISSING` | 요청이 경로에도 Bank에도 없다. |
| `REQUEST_BANK_CONFLICT` | 요청이 경로와 Bank에 동시에 있다. |
| `REQUEST_PARTIALLY_ASSIGNED` | pickup 또는 delivery 하나만 경로에 있다. |
| `REQUEST_DUPLICATED` | pickup 또는 delivery가 전체 해에 둘 이상 있다. |
| `REQUEST_SPLIT_ACROSS_ROUTES` | pickup과 delivery가 서로 다른 차량 경로에 있다. |
| `PICKUP_AFTER_DELIVERY` | 같은 경로에서 pickup이 delivery보다 뒤에 있다. |
| `REQUEST_NODE_MAPPING_INVALID` | 요청과 노드 타입·소유 매핑이 잘못됐다. |

이 코드는 최종 고객 미배정 사유와 구분한다. 위 코드는 솔버의 구조적 구현 오류이며, `NO_COMPATIBLE_VEHICLE` 같은 미배정 사유는 정상적인 최적화 결과다.

## 5. 테스트 항목

### 5.1 결정적 단위 테스트

| 테스트 ID | 구성 | 기대 결과 |
|---|---|---|
| `PAIR-001` | 같은 경로에 pickup 1회, delivery 1회, pickup이 먼저이고 Bank에 없음 | 유효한 `ASSIGNED` |
| `PAIR-002` | 두 노드 모두 경로에 없고 요청이 Bank에 있음 | 유효한 `UNASSIGNED` |
| `PAIR-003` | pickup만 경로에 있음 | `REQUEST_PARTIALLY_ASSIGNED` |
| `PAIR-004` | delivery만 경로에 있음 | `REQUEST_PARTIALLY_ASSIGNED` |
| `PAIR-005` | pickup은 차량 A, delivery는 차량 B | `REQUEST_SPLIT_ACROSS_ROUTES` |
| `PAIR-006` | 같은 경로에서 delivery가 pickup보다 먼저 | `PICKUP_AFTER_DELIVERY` |
| `PAIR-007` | pickup 또는 delivery가 두 번 존재 | `REQUEST_DUPLICATED` |
| `PAIR-008` | 두 노드가 경로에 있고 요청도 Bank에 있음 | `REQUEST_BANK_CONFLICT` |
| `PAIR-009` | 두 노드가 없고 Bank에도 없음 | `REQUEST_MISSING` |
| `PAIR-010` | 두 요청이 같은 pickup/delivery 노드 ID를 공유 | `REQUEST_NODE_MAPPING_INVALID` |

### 5.2 연산자 계약 테스트

| 테스트 ID | 시나리오 | 기대 결과 |
|---|---|---|
| `MOVE-001` | 배정 요청 하나를 Destroy | 두 노드가 함께 제거되고 요청이 Bank에 추가됨 |
| `MOVE-002` | `q=3` Destroy | 정확히 요청 3개, 즉 노드 6개가 제거됨 |
| `MOVE-003` | 반쪽 요청을 Destroy에 전달 | 변경 없이 사전조건 실패 |
| `MOVE-004` | Bank 요청의 가능한 pair insertion | 같은 경로에 두 노드가 삽입되고 Bank에서 제거됨 |
| `MOVE-005` | pickup 후보는 가능하지만 delivery까지 넣으면 불가능 | Route와 Bank가 호출 전과 완전히 동일함 |
| `MOVE-006` | pickup 위치와 delivery 위치가 같거나 역순 | 후보 생성 단계에서 거절 |
| `MOVE-007` | 대상 차량이 요청과 호환되지 않음 | Route 변경 없이 요청이 Bank에 유지됨 |
| `MOVE-008` | candidate가 SA에서 거절됨 | current Route, Bank, 캐시, 점수, hash가 모두 복원됨 |
| `MOVE-009` | 차량 제거 후 일부 요청 재삽입 실패 | Fleet Minimizer 시작 전 해로 전체 rollback |
| `MOVE-010` | 요청을 다른 차량으로 relocate | 원래 경로에서 쌍이 모두 사라지고 대상 경로에 쌍이 모두 존재 |

### 5.3 속성 기반 및 상태 전이 테스트

작은 임의 인스턴스에서 유효한 초기해를 만든 뒤 다음 연산을 임의 순서로 반복한다.

```text
pair insert
pair remove
request relocate
request exchange
destroy + repair
candidate accept/reject
fleet removal success/failure
```

각 안정 상태마다 다음 속성을 검증한다.

```text
모든 요청 r에 대해:
  ASSIGNED(r) XOR UNASSIGNED(r)

sum(pickupCount(r)) == 배정 요청 수
sum(deliveryCount(r)) == 배정 요청 수
RequestBank.size + 배정 요청 수 == 전체 요청 수
```

추가로 다음을 검증한다.

- 모든 배정 요청의 두 노드는 같은 vehicleId를 가진다.
- 모든 배정 요청에서 pickup index가 delivery index보다 작다.
- 연산자 실패와 candidate 거절 전후의 구조 해시 및 전체 노드 열이 같다.
- 전체 재검증 결과와 연산자의 국소 검증 결과가 같다.
- 점수 계산기, observer, 결과 변환기가 부분 요청 상태를 한 번도 관찰하지 않는다.
- 같은 seed의 결정적 실행에서 불변조건 오류의 유무와 최종 요청 배정 상태가 재현된다.

### 5.4 마스터 설계 테스트 절에 추가할 최소 문구

> 속성 기반 테스트는 “모든 주문이 경로 또는 RequestBank 중 하나에 존재한다”를 요청 쌍 단위로 검증한다. 배정 요청의 pickup과 delivery는 동일 차량 경로에 각각 정확히 한 번 존재하고 pickup이 먼저여야 하며, 미배정 요청의 두 노드는 어떤 경로에도 없어야 한다. 모든 Construction, Destroy, Repair, Local Search, Fleet Minimizer 연산자의 성공·실패·rollback 이후에 이 불변조건을 검증한다.

## 6. 남은 질문

핵심 pair 불변조건에는 더 이상 결정이 필요하지 않다. 아래 항목은 향후 변형 문제나 관찰 정책을 명확히 하기 위한 질문이며, 현재 RPDPTW 1차 구현을 막지는 않는다.

1. **멀티트립에서 한 요청이 중간 depot 경계를 넘을 수 있는가?**  
   예를 들어 첫 번째 trip에서 pickup하고 depot 재방문 뒤 두 번째 trip에서 delivery하는 것을 허용할지 정해야 한다. 권장 기본값은 `허용하지 않음`이다. 재적재 시점의 적재량 초기화와 화물 보관 의미가 충돌할 수 있으므로 pickup과 delivery는 같은 vehicle뿐 아니라 같은 trip segment에 두는 것이 안전하다.

2. **운영 모드의 전체 검증 주기를 추가로 설정할 것인가?**  
   이 문서는 Destroy/Repair 경계, 수용 전, best 갱신 전, 출력 전 검증을 필수로 확정했다. 장시간 탐색 중 방어적 검증을 매 `N` step마다 추가할지는 성능 측정 후 정할 수 있다. 권장 기본값은 개발·벤치마크에서는 매 step, 운영에서는 설정 가능한 `N`과 필수 경계 검증을 함께 사용하는 것이다.

3. **구조 오류 발생 시 운영 정책은 즉시 중단인가, 해당 candidate만 폐기하고 계속 탐색인가?**  
   구조 오류는 정상적인 infeasible 후보가 아니라 구현 결함을 뜻한다. 권장 기본값은 테스트·개발에서는 즉시 중단하고, 운영에서는 해당 candidate를 폐기하되 오류 메트릭과 진단 정보를 남긴 후 안전하게 복구된 current solution에서만 계속하는 것이다. 복구 검증에 실패하면 solve 전체를 중단해야 한다.

4. **향후 split delivery를 별도 문제 유형으로 도입할 시점은 언제인가?**  
   현재 확정 불변조건은 한 요청을 원자적 pickup-delivery 쌍으로 본다. 분할 배송은 수량별 부분 배정과 다중 방문을 요구하므로 별도 상태 모델과 연산자 계약이 필요하다. 현재 마스터 설계에서는 후속 feasibility study로만 기록하고 1차 RPDPTW 계약에는 포함하지 않는다.

마스터 설계에 병합할 때는 이 문서의 규칙을 최소한 다음 절에 반영한다.

- `Request` 및 `Solution`/`RequestBank`: 요청 상태의 배타적 두 형태
- 주문 삽입 실행 가능성 검사: 구조 검사를 업무·시간·용량 검사보다 먼저 수행
- ALNS Construction/Destroy/Repair/FleetMinimizer: 원자적 요청 이동 및 rollback 계약
- 테스트 전략: 정적 매핑, 상태 전이, 실패 복구, 속성 기반 검증

