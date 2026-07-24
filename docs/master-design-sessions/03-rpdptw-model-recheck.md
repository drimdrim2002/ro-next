# 세션 03 — CVRPTW의 RPDPTW 통합 모델 재검토

> 범위: `docs/orgin/alns_pdptw_paper_summary_ko.md`, `docs/arranged/01_problem_definition.md`, `docs/master-design.md`의 문제 모델과 변환 규칙 검토  
> 작업 성격: 향후 구현을 위한 설계 결정 기록  
> 상태: 조건부 결정. 마지막의 질문은 미확정 사항이다.  
> 제외 범위: 코드 변경, `master-design.md` 변경, 목적함수·배포 구조 검토

## 자료 결론

### 1. 논문 요약 문서가 전제하는 PDPTW 요청

`alns_pdptw_paper_summary_ko.md`의 요청은 실제 픽업 지점과 실제 배송 지점을 가진 하나의 운송 단위다. 문서는 PDPTW의 필수 요소로 픽업, 배송, 시간창, 용량, 픽업 선행, 동일 차량 처리를 명시한다([123~146행](../orgin/alns_pdptw_paper_summary_ko.md#L123)). 수학적 모델도 요청마다 픽업 노드 하나와 배송 노드 하나를 둔다([274~320행](../orgin/alns_pdptw_paper_summary_ko.md#L274)).

논문 요약에서 직접 확인되는 핵심 제약은 다음과 같다([373~386행](../orgin/alns_pdptw_paper_summary_ko.md#L373)).

- 요청은 차량에 배정되거나 request bank에 있어야 한다.
- 한 요청의 픽업과 배송은 같은 차량이 수행한다.
- 픽업은 배송보다 먼저 수행한다.
- 차량은 정해진 시작 터미널에서 출발해 종료 터미널로 간다.
- 모든 방문에서 시간창과 용량을 만족한다.
- 차량의 시작·종료 적재량은 0이다.

ALNS의 제거·삽입 단위도 개별 노드가 아니라 요청이다. 특히 삽입은 한 요청의 픽업 위치와 배송 위치를 함께 정하는 연산이다([646~656행](../orgin/alns_pdptw_paper_summary_ko.md#L646)). 따라서 실제 PDP에서는 픽업만 남거나 배송만 남는 중간 상태를 완성된 후보해로 인정해서는 안 된다.

### 2. 문제 정의 문서가 전제하는 CVRPTW 주문

`01_problem_definition.md`는 현재 문서들의 중심 문제를 CVRPTW로 정의한다. 이 모델에서 차량은 depot에서 출발해 고객을 한 번씩 방문하고 depot으로 복귀하며, 배정된 고객 수요의 합이 차량 용량 이하여야 한다([3~24행](../arranged/01_problem_definition.md#L3)). 해의 경로에도 고객 노드만 들어가고 별도의 픽업 방문은 없다([63~93행](../arranged/01_problem_definition.md#L63)).

또한 같은 문서는 PDP/PDPTW를 CVRPTW에 픽업–배송 pairing과 precedence가 추가된 더 강한 문제로 구분한다([15~24행](../arranged/01_problem_definition.md#L15)). 즉, 검토 대상 두 문서만으로는 “일반 CVRPTW 주문을 아무 제약 없이 PDPTW 요청으로 바꾸어도 같은 문제다”라는 결론이 성립하지 않는다.

### 3. 두 자료를 함께 읽은 결론

하나의 RPDPTW 요청 모델과 하나의 ALNS 프레임워크를 사용하는 목표는 타당하다. 다만 다음 두 문장은 구분해야 한다.

- **타당한 문장:** CVRPTW를 의미 보존 변환하여 RPDPTW 코어에서 풀 수 있다.
- **타당하지 않은 문장:** depot에 픽업 노드를 하나 추가하고 일반 PDP와 동일하게 아무 위치에나 삽입하면 자동으로 CVRPTW와 동등해진다.

통합은 “모든 입력을 똑같은 방문 의미로 취급”하는 것이 아니라, “서로 다른 요청 의미를 표준 요청 계약으로 정규화하고 같은 탐색 프레임워크가 그 계약을 지키게 하는 것”이어야 한다.

## 재검토 결과

### 1. 현재 마스터 설계의 위험

`master-design.md`는 모든 문제를 하나의 RPDPTW 모델로 변환한다는 원칙을 세우고([24~26행](../master-design.md#L24), [148~177행](../master-design.md#L148)), 모든 주문에 픽업 노드와 배송 노드를 고정 배치한다([546~585행](../master-design.md#L546)). 업무용 CVRPTW에서는 depot을 픽업 좌표와 시간창으로 사용하고 `depotTaskTime`을 픽업 서비스 시간으로 매핑한다([1033~1050행](../master-design.md#L1033)).

이 표현 자체는 사용할 수 있지만, 현재 적힌 제약만으로는 원래 CVRPTW와 동등하지 않다. 현재의 삽입 검사는 같은 요청의 픽업이 배송보다 앞서는지만 요구하며([1006~1021행](../master-design.md#L1006)), 시간 전파 예시는 다음 경로까지 자연스럽게 허용하는 모양이다([849~860행](../master-design.md#L849)).

```text
출발 depot
→ 주문 A 가상 픽업(depot)
→ 주문 A 배송
→ 주문 B 가상 픽업(depot)
→ 주문 B 배송
→ 종료 depot
```

이는 차량이 고객 A를 배송한 뒤 depot으로 돌아와 B를 다시 싣는 **다중 회차 또는 중간 재적재 문제**다. 반면 표준 CVRPTW는 한 route에 배정한 주문을 출발 전에 적재하고 고객만 순회하는 단일 회차 모델이다. 위 경로를 허용하면 원래 CVRPTW에서는 용량 때문에 불가능한 주문 집합도 여러 번 나누어 처리할 수 있어 탐색 공간과 실행 가능성의 의미가 달라진다.

### 2. `DELIVERY_ONLY`와 실제 PDP는 구분해야 하는가

**구분해야 한다.** 다만 별도 솔버나 별도 ALNS를 만들 필요는 없다. 표준 `Request` 안에서 운송 의미를 구분하고, transformer가 입력 문제에 맞는 의미를 지정하며, 공통 feasibility 계층과 operator가 그 의미에 맞는 위치 제약을 적용하면 된다.

| 요청 의미 | 픽업의 업무 의미 | 허용되는 픽업 위치 | 용량 의미 |
|---|---|---|---|
| `DELIVERY_ONLY` | 출발 depot에서 이미 준비된 화물의 가상 적재 | route 시작부에 고정 | 출발 전에 배정 주문 전체가 적재됨 |
| `PICKUP_DELIVERY` | 입력에 존재하는 실제 픽업 작업 | 같은 route에서 배송 전의 실행 가능한 위치 | 픽업 시 증가, 배송 시 감소 |

이 구분은 고객사별 예외가 아니라 문제 의미를 보존하는 코어 계약이다. 따라서 임의의 `RouteConstraint` 플러그인에만 맡기기보다 표준 요청 모델과 공통 해 검증 규칙에 포함하는 편이 안전하다.

### 3. depot pickup 가상화는 가능한가

**가능하지만 조건부다.** 다음 조건을 모두 지키면 `DELIVERY_ONLY` 주문을 가상 pickup–delivery 쌍으로 표현해도 표준 CVRPTW의 용량 의미를 보존할 수 있다.

1. 가상 픽업은 실제 경유 후보가 아니라 해당 차량의 출발 적재를 표현한다.
2. 한 route에 배정된 모든 가상 픽업은 출발 터미널 바로 뒤의 연속된 prefix에 있어야 한다.
3. 첫 고객 방문 후에는 다른 `DELIVERY_ONLY` 가상 픽업을 삽입할 수 없다.
4. 가상 픽업 prefix가 끝난 시점의 적재량은 그 route에 배정된 delivery-only 주문 수요의 합과 같아야 한다.
5. 이 적재량이 차량 용량 이하여야 한다. 그러면 기존 CVRPTW의 `sum(demand) <= capacity`와 동등해진다.
6. 가상 픽업과 배송은 같은 차량에 정확히 한 번 존재하고, destroy/repair에서 한 요청으로 함께 이동한다.
7. 가상 픽업의 위치·시간·서비스 비용을 어떻게 계산하는지 별도 계약으로 고정한다.

예를 들어 다음은 허용되는 정규형이다.

```text
출발 터미널
→ A 가상 픽업(depot)
→ B 가상 픽업(depot)
→ A 배송
→ B 배송
→ 종료 터미널
```

다음은 표준 CVRPTW 변환에서는 금지해야 한다.

```text
출발 터미널
→ A 가상 픽업(depot)
→ A 배송
→ B 가상 픽업(depot)
→ B 배송
→ 종료 터미널
```

가상 픽업 노드를 경로에 실제로 나열하는 대신 route의 초기 적재량으로만 집계하는 방법도 의미상 더 직접적이다. 그러나 이는 현재의 고정 `2n + 2m` 노드 구조와 요청 쌍 삽입 연산을 더 많이 바꾸게 된다. **최소 변경이라는 목표를 고려하면 v1에서는 가상 pickup–delivery 쌍을 유지하되, 가상 픽업을 route 시작 prefix에 고정하는 방식**을 권장한다.

## 권장 모델

### 1. 표준 요청 계약

RPDPTW의 표준 요청은 픽업 노드와 배송 노드를 계속 가질 수 있다. 여기에 요청의 서비스 패턴을 명시적으로 추가한다.

```text
Request
├─ pickupNode
├─ deliveryNode
├─ servicePattern
│  ├─ DELIVERY_ONLY
│  └─ PICKUP_DELIVERY
└─ servableVehicles
```

이름은 구현 시 달라질 수 있지만, enum/string 값보다 중요한 것은 다음 의미 계약이다.

- `DELIVERY_ONLY`: pickup은 출발 적재를 나타내는 가상 노드이며 시작 prefix에 고정된다.
- `PICKUP_DELIVERY`: pickup과 delivery가 모두 실제 방문이며, 같은 차량·픽업 선행 조건 안에서 각각 위치를 탐색한다.

이 구조라면 별도 문제별 solver 분기 없이 같은 ALNS 흐름을 재사용할 수 있다.

```text
입력 스키마
→ ProblemTransformer가 servicePattern을 결정
→ 공통 Request 단위 destroy
→ pattern-aware insertion position 생성
→ 공통 route propagation 및 feasibility 검사
→ 공통 ALNS 수용·가중치 갱신
```

분기는 엔진 전체가 아니라 **삽입 가능 위치 생성과 요청 불변조건 검사**에 한정된다. 향후 실제 pickup-delivery 입력이 추가되어도 기존 고객사 transformer와 목적함수는 건드리지 않고 새 입력 매핑만 추가할 수 있다.

### 2. 삽입 연산의 계약

- `DELIVERY_ONLY` 요청은 배송 위치만 일반 삽입 후보로 탐색한다.
- 해당 요청의 가상 픽업은 선택한 route의 가상 픽업 prefix에 함께 배치한다.
- `PICKUP_DELIVERY` 요청은 `(pickupPosition, deliveryPosition)` 쌍을 탐색하며 `pickupPosition < deliveryPosition`을 만족해야 한다.
- 어떤 패턴이든 차량 호환성, 시간창, 근무시간, 다차원 용량, 거리·시간 한도 검사는 최종 route 전체에 대해 동일하게 수행한다.
- destroy는 두 노드를 개별적으로 고르지 않고 요청 ID를 제거 단위로 사용한다.

### 3. 경로·비용·출력의 해석

- 가상 픽업은 내부 정규화 장치이며 고객 방문 stop으로 세지 않는다.
- 외부 배차 결과에는 가상 픽업을 실제 고객 stop처럼 노출하지 않거나, `VIRTUAL_LOADING`으로 명확히 구분한다.
- 동일 depot의 연속 가상 픽업 사이 이동거리와 이동시간은 0이어야 한다.
- depot 적재 서비스 시간이 주문별인지 route별인지 확정한 뒤 한 번만 적용되는 값과 누적되는 값을 구분한다.
- 실제 PDP pickup의 거리·시간·서비스 시간은 입력에 있는 실제 방문으로 계산한다.
- 중간 depot 재적재 또는 여러 회차가 필요하면 `DELIVERY_ONLY`의 묵시적 부작용으로 허용하지 말고 후속 `MULTI_TRIP` 모델로 명시한다.

### 4. 마스터 설계에 반영할 결정 문장

향후 `master-design.md` 개정 시 다음 취지가 들어가야 한다.

> CVRPTW와 실제 PDP는 하나의 RPDPTW 요청 모델 및 ALNS 엔진으로 통합한다. 실제 PDP 요청은 자유 위치의 실제 픽업과 배송 쌍으로 표현한다. Delivery-only CVRPTW 주문은 출발 depot의 가상 픽업과 고객 배송 쌍으로 변환하되, 가상 픽업은 해당 route의 시작 prefix에 고정하여 중간 재적재를 금지한다. 따라서 변환 전후의 고객 방문, 총 적재 요구량, 용량 실행 가능성, 거리·시간 의미가 동일해야 한다.

## 필요한 불변조건

### 1. 요청 소속과 원자성

1. 모든 요청은 정확히 하나의 상태만 가진다: 한 route에 배정되거나 request bank에 존재한다.
2. 배정 요청의 pickup과 delivery는 같은 route에 각각 정확히 한 번 존재한다.
3. 미배정 요청의 pickup과 delivery는 어떤 route에도 존재하지 않는다.
4. pickup만 있거나 delivery만 있는 orphan request를 허용하지 않는다.
5. destroy, repair, rollback, route 제거는 모두 요청 단위로 원자적으로 처리한다.
6. request ID와 pickup/delivery node ID의 대응은 일대일이며 중복되지 않는다.

### 2. 경로와 선행 관계

1. 각 route는 해당 차량의 start terminal로 시작하고 end terminal로 끝난다.
2. 다른 차량의 terminal은 route 내부에 들어갈 수 없다.
3. 모든 배정 요청은 `pickupPosition < deliveryPosition`을 만족한다.
4. `PICKUP_DELIVERY`의 실제 pickup은 입력의 좌표·시간창·서비스 시간을 그대로 사용한다.
5. `DELIVERY_ONLY`의 가상 pickup들은 start terminal 직후 하나의 연속된 prefix를 이룬다.
6. 가상 pickup prefix 이후에는 delivery-only 가상 pickup을 다시 방문할 수 없다.
7. standard CVRPTW 모드에서는 고객 방문 후 depot 재적재를 허용하지 않는다.

### 3. 적재량

1. 시작 terminal 직전 적재량은 0이다.
2. pickup에서 해당 요청의 무게·부피가 증가하고 delivery에서 동일량이 감소한다.
3. 모든 위치에서 각 적재 차원은 `0 <= load <= vehicleCapacity`를 만족한다.
4. `DELIVERY_ONLY` route의 가상 pickup prefix 종료 적재량은 그 route의 delivery-only 주문 총수요와 같다.
5. 모든 요청을 완료한 end terminal의 적재량은 0이다.
6. 같은 요청의 pickup과 delivery 수량은 정확히 상쇄된다.

### 4. 가상 노드의 거리·시간·통계

1. 가상 pickup은 선택된 차량의 출발 적재 지점과 공간적으로 일치해야 한다.
2. 같은 적재 지점의 연속 가상 pickup 사이 거리·이동시간은 0이다.
3. 가상 pickup이 실제 배송 stop 수, 고객 방문 수, 배송 완료 수를 증가시키지 않는다.
4. depot 서비스 시간은 확정된 집계 규칙에 따라 한 번만 또는 주문별로 정확히 계산한다.
5. 입력의 원래 고객 간·depot 간 거리와 시간이 가상 노드 복제 때문에 왜곡되지 않아야 한다.
6. 외부 결과에서 가상 pickup과 실제 pickup을 구별할 수 있어야 한다.

### 5. 검증 시나리오

향후 속성 기반 테스트와 작은 고정 예제에는 최소한 다음 반례를 포함해야 한다.

- `pickup A → delivery A → pickup B → delivery B`가 delivery-only 단일 회차에서는 거부되는가.
- 수요 6인 주문 두 개를 용량 10 차량에 넣었을 때 중간 재적재로 잘못 허용되지 않는가.
- 실제 PDP에서는 `pickup A → pickup B → delivery A → delivery B`처럼 정상적인 교차 순서가 허용되는가.
- pickup과 delivery를 서로 다른 차량에 두면 거부되는가.
- 요청 제거 후 두 노드 모두 route에서 사라지고 request bank에 요청이 정확히 한 번 들어가는가.
- 동일 depot의 가상 pickup 복제로 이동거리·시간이 늘어나지 않는가.
- delivery-only 변환 전의 `sum(demand) <= capacity` 판정과 변환 후 prefix 최대 적재량 판정이 일치하는가.

## 남은 질문

아래 항목은 검토한 두 문서만으로 확정할 수 없다. 답을 받기 전에는 임의로 마스터 설계에 고정하지 않는다.

1. `depotTaskTime`은 주문 하나를 상차할 때마다 드는 시간인가, 차량 한 대가 출발 준비를 할 때 한 번만 드는 시간인가, 아니면 물량에 비례하는 시간인가?
2. 업무 CVRPTW에서 차량이 배송 중 depot으로 돌아와 재적재하는 다중 회차를 허용하는가? 허용한다면 한 route 내부의 `MULTI_TRIP`인가, 별도 route인가?
3. 모든 주문의 출발 적재 지점은 하나의 공통 depot인가? 차량별 start terminal 또는 주문별 출고 depot이 다를 수 있는가?
4. 실제 pickup-delivery 주문과 delivery-only 주문이 하나의 차량 route에 혼합될 수 있는가? 가능하다면 출발 적재 후 실제 pickup으로 적재량이 다시 증가하는 경우를 그대로 허용하면 되는가?
5. 가상 pickup을 외부 결과에서 완전히 숨길 것인가, depot loading 작업으로 표시할 것인가?
6. `maxStopCnt`는 고객 배송 stop만 세는가, 실제 PDP pickup도 세는가, depot loading 작업도 세는가?
7. 동일 depot에서 여러 주문을 싣는 경우 pickup 시간창은 차량 출발 가능 시간만 제한하면 되는가, 주문별 상차 가능 시간창도 존재하는가?

이 질문들과 무관하게 확정 가능한 결론은 다음과 같다. **하나의 RPDPTW 코어와 ALNS를 유지하되, `DELIVERY_ONLY`와 실제 `PICKUP_DELIVERY`의 의미를 명시적으로 구분하고 delivery-only 가상 픽업을 route 시작부에 고정해야 한다.**
