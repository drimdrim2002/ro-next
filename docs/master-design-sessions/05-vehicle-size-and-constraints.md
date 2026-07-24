# 세션 05 — 차량 크기 유형과 확장 제약 설계

이 문서는 `master-design.md`의 Feature 호환 규칙을 사용자 요구에 맞게 다시 정의하기 위한 결정 기록이다. 검토 기준은 `docs/master-design.md`, `docs/domain-design.md`, `docs/arranged/01_problem_definition.md`, `docs/arranged/06_practical_extensions.md`와 현재 기준 사례인 `data/win_poc_case.json`이다.

핵심 결정은 다음과 같다.

> 기존 입력의 `vehicleFeature`는 냉장·리프트 같은 기능 집합이 아니라 `T1`, `T1.4`, `T3.5`, `T5` 같은 **차량 크기 유형 코드**다. 주문의 값은 허용 가능한 유형들의 집합이고, 차량의 값은 그 차량에 해당하는 단일 유형이다. 장비·자격·특수 능력은 이 축에 섞지 않고 별도의 호환 제약으로 확장한다.

## 용어 정의

### 차량 크기 유형

`VehicleSizeType`은 차량의 크기 또는 톤급을 식별하는 **범주형 코드**다.

```text
예: T1, T1.4, T1.9, T2.5, T3.5, T5, T7, T8
```

- 차량 한 대는 원칙적으로 하나의 `VehicleSizeType`을 가진다.
- 주문 또는 방문지는 진입을 허용하는 차량 크기 유형의 집합인 `AllowedVehicleSizeTypes`를 가진다.
- 주문 쪽의 여러 값은 모두 갖추어야 하는 요구 조건이 아니라 허용 가능한 대안, 즉 OR 조건이다.
- 크기 유형은 적재 용량과 별개다. 크기 유형이 허용되어도 주문의 무게·부피가 차량 용량을 초과하면 배정할 수 없다.
- 크기 코드 사이의 대소 관계를 솔버가 임의로 추론하지 않는다. 예를 들어 `T1`이 허용되었다는 사실만으로 `T1.4`도 허용된다고 판단하지 않는다.

기존 JSON 필드명은 호환성을 위해 유지할 수 있지만, 설계 문서와 비즈니스 모델에서는 의미가 드러나는 이름을 사용한다.

| 외부 입력 | 비즈니스 의미 | 권장 내부 명칭 |
|---|---|---|
| 주문의 `vehicleFeature[]` | 방문지에 진입 가능한 차량 크기 유형 집합 | `allowedVehicleSizeTypes` |
| 차량의 `vehicleFeature` | 해당 차량의 단일 크기 유형 | `vehicleSizeType` |

`Feature`라는 포괄적인 이름은 신규 설계 용어로 사용하지 않는다. 기존 문서의 “차량 기능”, “주문 요구 기능”, “냉장 기능” 예시는 차량 크기 규칙에서 제거해야 한다.

### 차량 능력과 요구 능력

차량의 장비·운영 자격은 차량 크기와 다른 축이다.

```text
차량 능력 예: REFRIGERATED, LIFT, HAZMAT_CERTIFIED
주문 요구 예: REFRIGERATED + LIFT
```

권장 용어는 다음과 같다.

- `vehicleCapabilities`: 차량이 보유한 장비·자격의 집합
- `requiredCapabilities`: 주문 수행에 반드시 필요한 장비·자격의 집합

### 차량 호환성

차량 호환성은 크기 유형, 권역, 장비·자격, 전용 차량 같은 여러 정적 규칙을 모두 적용한 최종 결과다.

```text
servable(request, vehicle)
= sizeTypeCompatible
  AND zoneCompatible
  AND capabilityCompatible
  AND dedicatedVehicleCompatible
  AND 그 밖의 정적 호환 규칙
```

솔버 코어의 `Request.servableVehicles`는 이 결과를 차량 ID 기준 `BitSet`으로 보관한다. 코어는 어떤 업무 규칙이 이 비트를 제외했는지 알 필요가 없다.

## 차량 크기 호환 모델

### 기본 판정

주문 `r`과 차량 `v`의 차량 크기 호환 규칙은 다음과 같다.

```text
sizeTypeCompatible(r, v)
= vehicleSizeType(v) ∈ allowedVehicleSizeTypes(r)
```

집합 표현으로 쓰면 다음과 같으며, 차량은 단일 유형만 가진다는 점이 중요하다.

```text
allowedVehicleSizeTypes(r) ∩ {vehicleSizeType(v)} ≠ ∅
```

따라서 기존 `master-design.md`의 “공통 feature가 하나라도 있으면 호환”이라는 문장은 **차량 크기 유형에 한해서만** 결과적으로 맞다. 의미는 기능 두 집합의 교집합이 아니라, 주문이 나열한 허용 유형 중 차량의 단일 유형이 하나 존재하는지를 확인하는 것이다.

### 교집합과 부분집합의 구분

| 규칙 종류 | 주문 측 의미 | 차량 측 의미 | 판정 |
|---|---|---|---|
| 차량 크기 유형 | 허용 가능한 유형들의 대안 집합 | 단일 유형 | `vehicleSizeType ∈ allowedVehicleSizeTypes` |
| 장비·자격 | 모두 충족해야 하는 요구 집합 | 보유 능력 집합 | `requiredCapabilities ⊆ vehicleCapabilities` |
| 권역 | 방문지 또는 주문의 권역 | 차량이 담당 가능한 권역 | 권역 정책에 따른 포함 또는 사전 계산 결과 |
| 전용 차량 | 허용 차량 ID 집합 | 단일 차량 ID | `vehicleId ∈ allowedVehicleIds` |

예를 들어 주문이 차량 크기 `{T1, T1.4, T1.9}`와 장비 `{REFRIGERATED, LIFT}`를 요구한다면 다음 두 조건을 동시에 만족해야 한다.

```text
vehicle.sizeType ∈ {T1, T1.4, T1.9}
AND
{REFRIGERATED, LIFT} ⊆ vehicle.capabilities
```

장비 규칙에 교집합을 적용하면 냉장 기능만 가진 차량도 잘못 허용된다. 반대로 차량 크기에 부분집합 규칙을 적용하면 단일 차량이 여러 톤급을 동시에 가져야 하는 잘못된 모델이 된다.

### 명시적 허용 목록

차량 크기는 순서가 있어 보이더라도 정확한 코드 일치로 판정한다.

```text
주문 허용 유형 = {T1, T1.4, T1.9}

T1 차량   → 허용
T1.4 차량 → 허용
T1.9 차량 → 허용
T2.5 차량 → 불허
T5 차량   → 불허
```

고객사 입력이 “최대 T1.9까지”처럼 상한으로 제공된다면 `ProblemTransformer`가 고객사별 크기 유형 목록을 사용해 `{T1, T1.4, T1.9}`로 전개한다. 솔버 코어에는 상한 비교나 톤급 문자열 파싱 규칙을 넣지 않는다.

### `ALL`과 빈 값

`ALL`, `null`, 빈 배열의 정확한 외부 계약은 아직 확정하지 않는다. 다만 코어로 와일드카드 문자열을 전달하지 않는 원칙은 확정한다.

```text
외부의 ALL/빈 값 의미 해석
→ Transformer에서 명시적인 허용 차량 집합으로 전개
→ Request.servableVehicles에는 실제 차량 ID 비트만 저장
```

이 방식이면 입력 계약을 나중에 확정하더라도 ALNS와 코어 모델은 변경되지 않는다.

## 일반 제약 확장 구조

제약은 계산에 필요한 정보의 범위에 따라 세 종류로 구분한다. 새 요구가 생길 때 먼저 가장 단순한 확장 지점을 선택해야 한다.

| 구분 | 판단에 필요한 정보 | 표현 방식 | 예 |
|---|---|---|---|
| 정적 차량-주문 호환성 | 주문 하나와 차량 하나 | 변환 단계에서 `servableVehicles`로 컴파일 | 차량 크기, 냉장·리프트, 권역, 전용 차량 |
| 경로 상태 제약 | 차량과 경로 전체 또는 일부 순서 | `RouteConstraint` | 함께 실을 수 없는 주문 조합, 후입선출 하역, 경유 순서 제한 |
| 경로 진행 의미 변경 | 시간·적재 상태 전파 자체 | `RoutePropagator` | 멀티트립, 휴게·운전 중단과 재개, 특수 적재 상태 전파 |

비용 선호는 실행 가능성 제약과 섞지 않고 `ScorePolicy`가 담당한다. 예를 들어 큰 차량 이용을 금지하는 것은 호환성 제약이지만, 큰 차량 이용 비용만 높이는 것은 점수 정책이다.

### 정적 호환 규칙 합성

정적 규칙은 논리곱으로 합성하고 그 결과만 `BitSet`으로 만든다.

```text
VehicleCompatibilityRule
├─ VehicleSizeTypeRule
├─ ZoneRule
├─ CapabilityRule
├─ DedicatedVehicleRule
└─ 고객사별 추가 정적 규칙
```

각 규칙은 개념적으로 다음 계약을 가진다.

```text
isCompatible(order, vehicle) → true / false
```

변환기는 모든 차량을 후보로 시작한 뒤 등록된 규칙을 순서대로 적용한다.

```text
candidates = 모든 차량
candidates &= size-type 허용 차량
candidates &= zone 허용 차량
candidates &= capability 충족 차량
candidates &= 전용 차량 조건 충족 차량
```

최종 결과는 `Request.servableVehicles`에 저장한다. 규칙을 추가해도 `Request`, `Route`, 삽입·제거 연산자와 ALNS 엔진은 바꾸지 않는 것이 목표다.

규칙 구현이 문제 인스턴스별 배열이나 비트셋을 보유한다면 전역 singleton으로 공유하지 않는다. `SiteProfile` 또는 변환 과정이 solve 요청마다 불변 규칙 데이터와 최종 호환성 비트셋을 생성해야 병렬 실행 시 고객사·요청 간 상태 오염을 막을 수 있다.

### 실행형 제약의 경계

다음처럼 주문-차량 한 쌍만 보고 확정할 수 없는 조건은 `servableVehicles`로 억지로 표현하지 않는다.

- 주문 A와 B를 같은 차량에 실을 수 없음
- 배송 순서가 적재 순서의 역순이어야 함
- 특정 품목 조합의 동시 적재량 제한
- 한 경로에서 특정 유형 방문은 최대 N회
- 직전 방문이나 누적 상태에 따라 진입 가능 여부가 달라짐

이 규칙들은 `SiteProfile.extraConstraints`의 `RouteConstraint`로 합성한다. 경로 시간이나 적재량 전파의 정의 자체가 달라지는 경우에만 `RoutePropagator`를 교체한다. 단순한 고객사 제약 때문에 전파기를 교체하지 않는다.

현재 `domain-design.md`의 방향인 “가능한 제약은 데이터로 컴파일하고, 데이터만으로 표현할 수 없는 경로 전체 규칙만 실행형 인터페이스로 남긴다”는 유지한다. 다만 차량 크기와 일반 장비를 같은 `features` 집합에 넣은 예시는 위 구분에 맞게 개정해야 한다.

## 변환 단계와 탐색 단계 책임

### 변환 단계

`ProblemTransformer`와 고객사별 호환 규칙은 다음을 담당한다.

1. 외부 차량 크기 코드를 정규화하고 등록되지 않은 코드를 검증한다.
2. 차량마다 단일 `vehicleSizeType`을 확정한다.
3. 주문의 `allowedVehicleSizeTypes`를 정규화한다.
4. 상한·권역 정책처럼 간접적으로 주어진 조건을 명시적인 허용 유형 또는 허용 차량 집합으로 전개한다.
5. 차량 크기, 권역, 장비·자격, 전용 차량 등 모든 정적 규칙을 합성한다.
6. 주문마다 최종 `servableVehicles BitSet`을 생성한다.
7. 후보가 없는 경우 어느 정적 규칙에서 후보가 소진되었는지 진단 정보로 남긴다.

호환성 사유를 설명해야 한다면 탐색용 `BitSet`과 별도로 변환 결과에 진단 정보를 보관한다. ALNS의 핫 루프가 문자열 사유나 고객사 객체를 순회하게 만들지는 않는다.

### 탐색 단계

ALNS와 삽입 평가기는 정규화된 결과만 사용한다.

```text
if request.servableVehicles[vehicleId] == false
    해당 삽입 후보를 즉시 제외
```

그 후에 용량, 정차 수, 시간 전파, 경로 단위 추가 제약을 저렴한 순서대로 검사한다.

```text
1. servableVehicles
2. 최대 정차 수와 빠른 용량 상한
3. 시간·적재 전방 전파
4. RouteConstraint 목록
5. ScorePolicy 비용 평가
```

Destroy 연산자는 호환성 판단을 하지 않아도 되지만, Repair와 차량 간 Local Search는 이동 대상 차량에 대한 호환성을 반드시 다시 검사한다.

- Relocate: 이동 주문이 대상 차량과 호환되어야 한다.
- Swap: 두 주문이 각각 상대 차량과 호환되어야 한다.
- 2-opt* / Cross-exchange: 교환되는 모든 요청이 상대 차량과 호환되어야 한다.
- 픽업·배송 요청: 요청 쌍에 하나의 `servableVehicles`를 적용하고 같은 차량 배정 불변조건을 유지한다.

탐색 단계에서는 `T1`, `T5`, `REFRIGERATED` 같은 업무 문자열을 직접 비교하지 않는다.

## 검증 예

### 예 1. 좁은 지역의 대형 차량 진입 제한

```text
주문 O-1 허용 크기 = {T1, T1.4, T1.9}
차량 V-1 크기 = T1.4
차량 V-2 크기 = T2.5
```

결과:

```text
sizeTypeCompatible(O-1, V-1) = true
sizeTypeCompatible(O-1, V-2) = false
```

이는 무게 비교가 아니라 명시적 허용 코드의 포함 검사다. O-1이 매우 가벼워도 V-2는 진입할 수 없다.

### 예 2. 차량 크기와 용량은 독립

```text
주문 O-2 허용 크기 = {T1, T1.4}
주문 O-2 무게 = 1,800kg

차량 V-3 크기 = T1
차량 V-3 최대 무게 = 1,500kg
```

결과:

```text
차량 크기 호환 = true
용량 실행 가능 = false
최종 배정 가능 = false
```

### 예 3. 장비 요구는 부분집합

```text
주문 O-3 허용 크기 = {T1, T1.4, T1.9}
주문 O-3 요구 능력 = {REFRIGERATED, LIFT}

차량 V-4 크기 = T1.4
차량 V-4 보유 능력 = {REFRIGERATED}
```

결과:

```text
차량 크기 호환 = true
장비·자격 호환 = false
최종 servableVehicles에 V-4 미포함
```

공통 능력 하나가 있다는 이유로 허용하면 안 된다.

### 예 4. 모든 정적 규칙의 논리곱

```text
주문 O-4
  허용 크기 = {T1, T1.4}
  권역 = ZONE_16
  요구 능력 = {REFRIGERATED}

차량 V-5
  크기 = T1.4
  담당 권역 = {ZONE_16, ZONE_17}
  보유 능력 = {REFRIGERATED, LIFT}
```

모든 정적 규칙이 참이면 `V-5`를 O-4의 `servableVehicles`에 포함한다. 어느 하나라도 거짓이면 포함하지 않는다.

### 예 5. 차량 간 이동

현재 T1 차량에 배정된 주문의 경로상 시간과 용량이 모두 여유롭더라도, 해당 주문의 허용 크기 집합에 T5가 없다면 T5 차량으로의 Relocate 후보는 평가 전에 제외한다. 같은 검사를 초기해, Repair, Local Search 모두에서 일관되게 적용한다.

### 문서 및 구현 완료 기준

- `vehicleFeature`를 일반 기능 집합으로 설명하는 문구가 남아 있지 않다.
- 주문의 차량 크기 값은 대안 집합, 차량의 값은 단일 유형이라는 계약이 명시되어 있다.
- 차량 크기는 membership, 장비·자격은 subset이라는 차이가 테스트로 고정되어 있다.
- 정적 규칙을 하나 추가해도 ALNS 연산자와 코어 `Request` 구조를 수정하지 않는다.
- 차량 크기, 용량, 권역, 장비 규칙 각각의 실패를 독립적으로 검증한다.
- 모든 Repair 및 차량 간 Local Search가 `servableVehicles`를 우회하지 않는다.

## 남은 질문

아래 항목은 현재 자료만으로 확정하지 않고 후속 확인 대상으로 남긴다.

1. 주문의 `vehicleFeature`가 `null`, 빈 배열 또는 누락일 때 “모든 크기 허용”인가, 입력 오류인가?
2. 주문의 `vehicleFeature`에 `ALL`을 허용하는가? 허용한다면 모든 현재 차량 유형으로 전개하면 되는가?
3. 차량의 단일 `vehicleFeature`에 `ALL`이 올 수 있는가? 차량 크기라는 의미상 실제 유형 코드가 필수인 것으로 보는 편이 명확한데, 레거시 데이터에 `ALL`이 존재하는지 확인이 필요하다.
4. 차량 크기 코드는 전 고객사 공통 표준인가, 고객사별 코드 체계인가? `T1.4`, `T1.9`처럼 세분된 실제 데이터가 있으므로 고정 enum과 고객사별 코드 레지스트리 중 어느 쪽을 사용할지 결정해야 한다.
5. 주문의 허용 크기 목록은 상류 시스템이 이미 지역 진입 제한을 전개한 최종 결과인가, 솔버가 `zoneId`나 별도 지역 정책에서 계산해야 하는가?
6. `vehicleFeature`와 `zoneId`가 모두 제공되면 두 조건을 항상 AND로 적용하는가? 두 필드가 같은 진입 제한을 중복 표현하는 사례가 있는지도 확인이 필요하다.
7. 픽업지와 배송지의 차량 크기 제한이 서로 다를 수 있는가? 그렇다면 요청의 허용 집합은 두 방문지 허용 집합의 교집합으로 계산할지 확인해야 한다.
8. 등록되지 않은 차량 크기 코드가 들어오면 전체 입력을 거부할지, 해당 주문만 `NO_COMPATIBLE_VEHICLE`로 남길지 정책 결정이 필요하다.
9. 냉장·리프트·위험물 자격 같은 일반 능력이 실제 입력 계약에 추가될 예정인가? 예정이라면 필드명과 코드 사전을 별도로 정의해야 한다.
10. 시간대·요일·날씨에 따라 차량 진입 가능 여부가 달라지는 규칙이 현재 범위에 있는가? 있다면 단순 요청-차량 `BitSet`만으로는 부족하므로 시간창 전개 또는 경로 상태 제약으로 모델링해야 한다.
