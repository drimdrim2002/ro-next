# 세션 12 - 입력 거리·시간 행렬 계약

> **세션 30 통합 상태 (2026-07-23):** 본문의 no-fallback 권장과 `D/U/C`·diagonal 미결정은 세션 29의 `Q-MTX-01~03` 사용자 결정으로 대체되었다. 외부 sparse/omitted input을 허용하고 명시적 preparation에서 `D=meter`, `U=second`, self `0/0`, 누락 `D` Great Circle `HALF_UP`, 누락 `U` vehicle별 `CEILING(D×3.6/speed)`와 속도 누락 시 `45km/h`를 적용한 뒤 complete travel data만 solver/verifier에 전달한다. `C`는 비권위다. 현재 계약은 [Master §8](../master-design.md#8-directed-distancetime-matrix-계약), 반영 검증은 [세션 30](30-open-question-integration.md)을 따른다.

> 범위: `data/ro_input_json_spec.pdf`, `data/win_poc_case.json`, `docs/master-design.md`의 거리·시간 생성 및 조회 설계
> 작업 성격: 향후 구현을 위한 설계 결정 기록
> 상태: 조건부 결정. 입력 행렬 우선 원칙과 방향성 보존은 확정할 수 있으나, 단위와 legacy 필드의 일부 의미는 확인이 필요하다.
> 제외 범위: 코드 변경, `master-design.md` 직접 변경, 외부 라우팅 API 구현

## 확인된 입력 구조

### 1. PDF 명세와 실제 기준 데이터의 차이

`ro_input_json_spec.pdf` 10쪽을 모두 확인한 결과, 이 PDF는 기존 CVRPTW 입력의 계획, 옵션, 주문, 차량 필드를 설명한다. 거리 계산과 관련해서는 다음만 정의한다.

- `options.distanceCalculate`: 기본 `OSRM`, 다른 값인 `GreatCircle` 사용 가능
- `options.defaultSpeed`: 모든 차량에 적용할 기본 속도
- 차량 `speed`: `km/h`
- 차량 `maxDriveDistc`: `meter`
- 차량 `maxDriveTime`: `second`

PDF 본문과 JSON 예시에는 `distanceMatrix` 필드 및 그 하위 필드 `C`, `T`, `D`, `U`, `F`의 정의가 없다. 따라서 PDF만 근거로 행렬 필드의 이름, 단위, diagonal 표현을 확정할 수는 없다. PDF는 엔진이 OSRM 또는 GreatCircle로 거리를 계산하던 이전 계약을 설명하고, 실제 기준 데이터는 계산된 거리·시간을 입력에 포함하도록 확장된 것으로 보인다.

`win_poc_case.json`에는 루트에 다음 필드가 존재한다.

```text
distanceMatrix: Array<{
  C,
  T,
  D,
  U,
  F
}>
```

관찰된 필드 역할은 다음과 같다. `F`, `T`, `D`, `U`의 의미는 데이터 형태와 값으로 강하게 추론되지만 PDF에 명시되지 않았으므로 최종 입력 계약에서 정식 확인해야 한다.

| legacy 필드 | 관찰된 역할 | 기준 데이터의 형식 |
|---|---|---|
| `F` | 출발 위치 ID, `fromLocationId` | 문자열 |
| `T` | 도착 위치 ID, `toLocationId` | 문자열 |
| `D` | `F → T` 거리 | 비대각은 숫자 문자열, 대각은 숫자 `9999` |
| `U` | `F → T` 이동시간 | 숫자 문자열 |
| `C` | 계산 방식 또는 arc 분류로 추정되는 메타데이터 | `O` 또는 `G`; 정확한 의미 미확정 |

새로운 공통 도메인이나 설계 문서에서는 `F`, `T`, `D`, `U`, `C`를 그대로 전파하지 않는다. 입력 adapter에서 의미가 드러나는 이름으로 바꾸고, `C`는 의미가 확인되기 전까지 비용 계산이 아닌 원본 메타데이터로만 보존한다.

```text
InputTravelArc
├─ fromLocationId
├─ toLocationId
├─ distance
├─ travelTime
└─ sourceMetadata       // legacy C, 의미 확인 전에는 선택적 메타데이터
```

### 2. `win_poc_case.json`의 실제 크기와 완전성

전체 JSON을 출력하지 않고 키, 개수, 타입, 집계값만 조사했다.

| 항목 | 확인값 |
|---|---:|
| depot 수 | 1 |
| 주문 수 | 452 |
| 입력에서 참조한 고유 `locId` 수 | 453 |
| 행렬의 고유 `F` 수 | 453 |
| 행렬의 고유 `T` 수 | 453 |
| 행렬 arc 수 | 205,209 |
| 기대 정방행렬 크기 | `453 × 453 = 205,209` |
| 중복 `(F, T)` 쌍 | 0 |
| 입력 위치 집합과 행렬 위치 집합의 차이 | 없음 |

즉 기준 데이터는 depot과 모든 주문 위치 사이의 **완전한 directed dense matrix**를 제공한다. 각 방향은 별도 arc다.

파일 안에서는 동일한 `F`가 453개씩 연속되고 각 row의 `T` 순서도 동일한 row-major 형태다. 그러나 이 순서는 `depot + orders` 배열 순서와 다르다. 따라서 row-major 배치는 최적화 가능한 관찰 특성일 뿐, 위치 매핑 계약으로 사용해서는 안 된다.

### 3. 값의 형식과 범위

기준 데이터의 비대각 arc에서 확인된 값은 다음과 같다.

| 항목 | 최솟값 | 최댓값 | 소수 자릿수 |
|---|---:|---:|---|
| `D` | 0.0 | 453,010.72 | 1~2자리 |
| `U` | 0.0 | 27,258.1 | 0~1자리 |

- 비대각 `D`는 문자열이고, 대각 `D` 453개만 JSON number `9999`다.
- `U`는 모두 문자열이며 대각은 문자열 `"0"`이다.
- 음수 또는 숫자로 해석할 수 없는 `D`, `U`는 없었다.
- 서로 다른 위치 ID 사이에 거리와 시간이 모두 0인 arc가 404개 있다.
- 이 404개는 모두 `C = "G"`이며, 양 끝 위치의 위도·경도가 동일하다.
- 나머지는 `C = "O"`다. `C`의 업무적 의미는 자료만으로 확정하지 않는다.

현재 기준 데이터는 거리와 시간을 정수로 제공하지 않는다. 따라서 다른 세션에서 정한 고정소수점 정책을 적용하더라도, 입력 행렬을 먼저 정수 meter/second라고 가정해서는 안 된다. 원문 10진수를 정확히 파싱한 뒤 거리와 시간 각각의 합의된 scale과 rounding으로 정규화해야 한다.

## 단위·방향성

### 1. 단위 판정

다음 근거를 종합하면 `D`는 meter, `U`는 second로 해석하는 것이 가장 일관된다.

- PDF는 차량 최대 주행거리를 `meter`, 최대 주행시간을 `second`로 정의한다.
- `WIN_0 → WIN_5221`의 값은 `D = 310708.03`, `U = 17265.5`다. 두 위치의 좌표 간 규모와 비교하면 약 310.7 km 및 약 4.8시간으로 해석할 때 현실적인 범위다.
- 행렬 최댓값도 거리 약 453 km, 시간 약 7.6시간으로 해석할 때 데이터의 지리적 범위와 일치한다.

다만 PDF의 첫 단위표에는 일반 `Length = mm`라고 적혀 있고, `distanceMatrix` 자체는 PDF에 없다. 따라서 **현재 설계 가정은 `D = meter`, `U = second`로 두되, 최종 계약 확정 질문을 남긴다.** `Length = mm`는 차량·화물 치수에 대한 단위로 보고 이동거리와 구분하는 것이 타당하다.

권장 외부 계약은 다음과 같다.

```text
distanceMeters: decimal meter
travelTimeSeconds: decimal second
```

내부 정수 scale은 세션 09의 `NumericNormalizationPolicy`가 정한다. 기준 데이터가 거리 0.01 m, 시간 0.1 s 정밀도를 실제로 포함하므로, 이를 정수 meter/second로 조용히 절삭하거나 반올림하는 규칙을 공통 계약으로 가정하지 않는다.

### 2. 방향성

행렬은 명백히 비대칭이다. 453개 위치의 무순서 위치쌍 102,378개를 역방향과 비교한 결과는 다음과 같다.

| 비교 | 비대칭 쌍 수 |
|---|---:|
| 거리 `D`가 다름 | 101,159 |
| 시간 `U`가 다름 | 102,122 |
| 둘 중 하나 이상 다름 | 102,149 |

예를 들면 다음과 같다.

```text
WIN_0    → WIN_5221: D = 310708.03, U = 17265.5
WIN_5221 → WIN_0:    D = 310302.25, U = 21078
```

그러므로 다음 규칙이 필요하다.

- `matrix[from][to]`와 `matrix[to][from]`은 독립 값이다.
- 한쪽 값으로 반대 방향을 채우거나 평균하지 않는다.
- 대칭행렬을 전제하는 저장 최적화와 알고리즘 최적화를 사용하지 않는다.
- 거리와 이동시간도 서로 독립적인 입력값이다. `time = distance ÷ speed`로 다시 계산하지 않는다.
- 차량별 시간 보정이 필요하더라도 입력 `U`를 기준값으로 사용하는 별도 정책이어야 한다.

## 노드·위치 매핑

### 1. matrix key는 주문 ID가 아니라 `locId`

`distanceMatrix.F`와 `distanceMatrix.T`는 `orders[].orderId`가 아니라 `depot[].locId` 및 `orders[].locId`를 참조한다. 또한 행렬의 첫 위치 순서는 다음처럼 입력 주문 순서와 다르다.

```text
행렬 순서: WIN_0, WIN_5221, WIN_2A99, ...
입력 순서: WIN_0, WIN_2AD0, WIN_1608, ...
```

따라서 `orders[i]`를 행렬의 `i + 1`행으로 대응시키는 구현은 잘못된 결과를 만든다. 변환 단계에서 반드시 다음 매핑을 만든다.

```text
locationIndexById: Map<LocationId, int>
locationIdsByIndex: LocationId[]
```

입력 arc는 `(F, T)`를 각각 `locationIndexById`로 변환해 dense matrix에 채운다. 파일 순서는 의미가 없고, `(F, T)` ID 쌍이 유일한 식별자다.

### 2. solver node와 physical location을 분리

RPDPTW 코어의 노드 수와 물리 위치 수는 같지 않다. 여러 요청의 가상 pickup이 같은 depot에 있을 수 있고, 여러 실제 요청이 같은 물리 위치를 공유할 수도 있다. 따라서 `2 × requestCount + terminals` 크기의 행렬을 노드 복제 수만큼 만들 필요가 없다.

권장 구조는 다음과 같다.

```text
SolverNode
├─ nodeId
├─ requestId / role
└─ locationIndex ───────────┐
                            ▼
LocationTravelMatrix[physicalLocation][physicalLocation]
├─ distance
└─ travelTime
```

경로 전파는 인접한 solver node의 `locationIndex`를 조회해 물리 위치 행렬을 사용한다. 이 구조는 다음 이점이 있다.

- 동일 depot의 가상 pickup을 주문 수만큼 복제해도 행렬은 한 번만 저장한다.
- delivery-only와 실제 pickup-delivery 요청이 같은 travel lookup을 사용한다.
- 주문 수가 아니라 고유 물리 위치 수 `M`에 대해 `M²`만 저장한다.
- 현재 453개 위치의 경우 거리·시간 primitive dense array는 arc 객체 205,209개를 유지하는 것보다 훨씬 작다.

`locId`가 같은 여러 입력 참조는 같은 물리 위치로 취급할 수 있다. 이 경우 위도·경도 등 위치 속성이 서로 다르면 입력 오류다. 반대로 `locId`는 다르지만 좌표가 같은 현재 데이터의 `C = G` arc는 별도 위치 ID를 유지하고 입력의 0 거리·0 시간을 그대로 보존한다.

### 3. diagonal과 가상 pickup

현재 데이터는 모든 `F == T` arc에서 `U = 0`이지만 `D = 9999`다. 이는 일반적인 자기 위치 이동거리와 맞지 않으며, legacy sentinel일 가능성이 높다. 이 값을 그대로 사용하면 같은 depot에 있는 연속 가상 pickup 사이에 주문마다 9,999의 거리가 누적된다.

권장 계약은 다음과 같다.

- 새로운 정식 행렬 계약의 diagonal은 거리 0, 시간 0이어야 한다.
- `win_poc_case.json`용 legacy adapter는 모든 diagonal이 동일한 `D = 9999`, `U = 0`인지 확인한 뒤 내부에서는 0, 0으로 정규화한다.
- 이 호환 변환은 입력 진단과 matrix provenance에 기록한다.
- legacy `9999`의 의미를 확인하기 전에는 다른 diagonal 값을 일반적으로 허용하거나 특별값 목록을 늘리지 않는다.

## 검증 규칙

입력 adapter가 `ProblemInstance`를 만들기 전에 다음을 검증한다.

### 1. 위치 참조

1. depot, terminal, pickup, delivery가 참조하는 모든 `locId`는 비어 있지 않아야 한다.
2. 동일 `locId`를 여러 요청이 공유할 수 있지만 좌표·zone 등 물리 위치 속성은 일관되어야 한다.
3. 필요한 고유 위치 집합과 행렬의 `F` 집합 및 `T` 집합이 일치해야 한다.
4. 알 수 없는 matrix 위치와 matrix에 없는 요청 위치를 각각 구분해 보고한다.
5. 실제 RPDPTW 입력에서는 pickup과 delivery의 위치 ID가 모두 검증 대상이다.

### 2. matrix 구조

고유 물리 위치가 `M`개라면 현재 production 계약은 다음을 요구한다.

1. 정확히 `M²`개의 directed arc가 있어야 한다.
2. 모든 `(fromLocationId, toLocationId)` 쌍이 정확히 한 번 있어야 한다.
3. row 순서와 column 순서는 자유이며 입력 배열 순서에 의존하지 않는다.
4. 비대칭을 허용하고 반대 방향 값을 별도로 보존한다.
5. 누락 arc, 중복 arc, 알 수 없는 endpoint가 하나라도 있으면 solve를 시작하지 않는다.

### 3. 수치

1. `D`, `U`는 원문 10진수로 정확히 파싱하며 빈 문자열, `NaN`, 무한대, 음수를 거부한다.
2. 현재 benchmark 호환 adapter는 JSON number와 numeric string을 모두 받을 수 있어야 한다. 일반 입력 스키마에서는 한 형식으로 통일하는 것이 바람직하다.
3. 거리·시간의 단위, scale, rounding 정책이 등록되어 있어야 한다.
4. `D`와 `maxDriveDist`, `U`와 `maxDriveTime`은 각각 같은 내부 단위와 scale을 사용해야 한다.
5. 정규화된 개별 arc, 경로 누적, 목적함수 집계가 자료형 범위를 넘지 않는지 checked arithmetic으로 검사한다.
6. 서로 다른 위치의 0 거리·0 시간은 허용한다. 현재 데이터에는 동일 좌표의 별도 `locId`가 실제로 존재한다.
7. diagonal 호환 규칙은 위 절대로 적용하며, 일반 이동 비용에 sentinel을 더하지 않는다.

### 4. 의미와 provenance

1. 입력 `U`는 기본적으로 authoritative travel time이다. `D ÷ vehicle.speed`로 덮어쓰지 않는다.
2. 차량 속도 보정은 명시적인 `TravelTimeAdjustmentPolicy`가 있을 때만 적용한다.
3. 보정 정책을 쓰려면 입력 `U`의 기준 속도 또는 기준 차량 의미, scale, rounding을 함께 정의해야 한다.
4. matrix 입력 해시, 단위 정책, 정규화 정책 버전, diagonal 호환 변환, 선택적 시간 보정 정책을 결과 메타데이터와 재현성 fingerprint에 포함한다.
5. `C`는 의미가 확정되기 전까지 경로 비용이나 feasibility를 바꾸는 분기로 사용하지 않는다.

독립 `SolutionVerifier`도 solver와 같은 정규화된 matrix를 사용해 최종 거리와 시간을 다시 계산해야 한다. 검증기가 좌표로 Haversine을 재계산하면 서로 다른 문제를 검증하게 된다.

## fallback 정책

### 1. 기본 정책

현재 요구사항의 기본값은 다음으로 정한다.

```text
TravelDataPolicy = REQUIRE_COMPLETE_INPUT_MATRIX
```

- 거리와 시간은 입력 행렬을 그대로 사용한다.
- 행렬 또는 directed arc가 누락되면 입력 오류로 종료한다.
- 좌표는 위치 식별, 데이터 진단, 지도 표시 용도이며 solver가 비용을 다시 만드는 근거가 아니다.
- Haversine, GreatCircle, 고정속도 계산, OSRM 호출은 solver core의 묵시적 fallback이 아니다.

누락 arc만 Haversine으로 채우면 같은 해 안에 도로 거리·시간과 직선 거리·고정속도 시간이 섞인다. 이는 실행 가능성, 목적함수, benchmark를 조용히 바꾸므로 허용하지 않는다.

### 2. 향후 생성 기능이 필요할 때

향후 matrix 생성이 필요하다면 solver core 밖의 명시적 전처리 단계로 둔다.

```text
좌표 입력
→ TravelMatrixPreparation
   ├─ RoadRoutingProvider
   └─ GreatCircleEstimateProvider
→ 단위와 provenance가 완성된 input matrix
→ InputTransformer
→ RPDPTW solver core
```

이 단계는 다음 조건을 지켜야 한다.

- 사용자가 명시적으로 생성 정책을 선택해야 한다.
- provider, 버전, 생성 시각, 교통 기준 시각, 단위, rounding을 기록한다.
- 가능하면 한 인스턴스 전체를 동일한 provider와 정책으로 생성한다.
- 혼합 출처를 허용해야 한다면 arc마다 provenance를 명시하고 이를 결과에 남긴다.
- 생성 실패를 직선거리로 조용히 대체하지 않는다.

PDF의 `distanceCalculate` 또는 현재 JSON의 `distanceTimeCalculate`는 이러한 전처리 정책의 legacy 입력으로만 해석할 수 있다. 완전한 `distanceMatrix`가 함께 들어오면 matrix가 우선하며, solver 내부에서 다시 계산하지 않는다. 장기적으로는 이 옵션을 matrix 생성 요청과 이미 생성된 matrix 입력에서 분리된 필드로 개정하는 것이 좋다.

## 마스터 반영안

향후 `master-design.md`에는 다음 변경을 반영한다.

### 1. 20.4 거리 및 시간 행렬 생성 절 교체

현재의 다음 설명은 삭제한다.

```text
좌표 간 거리는 haversine 거리로 사전 계산한다.
time(i, j) = distance(i, j) ÷ referenceSpeed
```

대신 다음 취지로 바꾼다.

> production 입력은 모든 관련 물리 위치 사이의 complete directed distance/time matrix를 제공한다. 변환기는 위치 ID로 arc를 매핑하고, 입력 거리와 시간을 정규화하여 RPDPTW 코어에 전달한다. 좌표 기반 거리·시간 생성은 solver core의 fallback이 아니며, 필요할 경우 별도 matrix preparation 단계가 완성된 입력 행렬을 생성한다.

### 2. `TravelMatrix`의 의미 변경

- solver node 수 기준 행렬이 아니라 고유 물리 위치 수 기준 행렬로 정의한다.
- 각 `SolverNode`는 `locationIndex`를 가진다.
- `distance[fromLocation][toLocation]`, `time[fromLocation][toLocation]`은 방향성 값이다.
- 가상 pickup과 terminal이 같은 위치를 공유해도 행렬을 복제하지 않는다.
- 외부 decimal은 입력 경계에서 정규화하고 core에는 primitive 정수 배열만 전달한다.

### 3. 차량별 속도 절 수정

입력 `U`가 authoritative travel time이면 기본적으로 차량 속도 배율을 적용하지 않는다. 고객이 차량 크기나 속도에 따른 시간 차이를 요구할 경우에만 다음 seam을 둔다.

```text
TravelTimeAdjustmentPolicy
├─ AS_INPUT
└─ SCALE_FROM_REFERENCE
```

`SCALE_FROM_REFERENCE`는 기준 속도·차량 속도·scale·rounding이 모두 정의된 경우에만 선택한다. 이 선택은 고객별 정책 확장 지점이지만 ALNS와 route propagation 자체에는 고객 조건문을 추가하지 않는다.

### 4. 변환·검증 Phase 추가

초기 Domain Core/Transformer Phase의 완료 조건에 다음을 포함한다.

- legacy `F/T/D/U/C`를 명시적 arc 계약으로 변환
- 위치 ID 집합 및 `M²` directed completeness 검증
- node-to-location 매핑
- 비대칭 distance/time 보존
- decimal 정규화 정책 연계
- diagonal `9999` benchmark 호환 처리
- matrix provenance 및 fingerprint
- 입력 matrix를 사용하는 독립 경로 거리·시간 검증

### 5. `win_poc_case.json` 회귀 기준

구현 전 문서 테스트 조건을 다음처럼 고정한다.

1. 453개 고유 위치와 205,209개 directed arc가 모두 로드된다.
2. 입력 위치 집합과 matrix endpoint 집합이 정확히 일치한다.
3. `WIN_0 → WIN_5221`과 역방향의 서로 다른 거리·시간이 보존된다.
4. 동일 좌표의 서로 다른 위치 간 0 거리·0 시간이 보존된다.
5. self arc legacy 값은 합의한 호환 규칙에 따라 0으로 정규화된다.
6. 주문 배열 순서를 바꾸어도 위치 ID 기반 행렬 조회 결과가 같다.
7. 하나의 directed arc를 삭제하거나 중복시키면 solve 전에 검증 실패한다.
8. Haversine 또는 차량 속도로 입력 `D`, `U`를 재계산하지 않는다.

## 남은 질문

다음은 자료만으로 확정할 수 없으므로 답변 전까지 임의로 구현 계약에 고정하지 않는다.

1. `distanceMatrix.D`의 공식 단위가 meter이고 `U`의 공식 단위가 second가 맞는가? 두 값의 소수 입력을 정식으로 허용하는가?
2. `C`의 `O`, `G`는 각각 무엇을 의미하는가? 라우팅 provider 또는 계산 방식이라면 결과 provenance로 보존해야 하는가?
3. 모든 self arc의 `D = 9999`, `U = 0`은 self 이동 금지를 나타내는 sentinel인가? 내부 0, 0 정규화가 맞는가?
4. 입력 `U`는 차량과 무관한 최종 도로 이동시간인가, 특정 기준 속도에 대한 시간인가? 후자라면 기준 속도는 어디에 있으며 차량 `speed`로 어떤 공식과 rounding을 적용해야 하는가?
5. `options.distanceTimeCalculate = "GreatCircle"`인데 대부분의 arc가 `C = "O"`인 이유는 무엇인가? 완전한 matrix가 있을 때 이 옵션은 무시해도 되는가?
6. 정식 스키마에서 `D`, `U`는 JSON number인가 numeric string인가? 기준 파일 호환 adapter는 둘 다 처리하되 신규 계약은 한 형식으로 통일해도 되는가?
7. production 입력은 항상 모든 방향의 `M²` arc를 제공하는가? sparse matrix 또는 일부 누락 arc를 지원해야 한다면 누락의 공식 의미와 보충 절차가 필요하다.
8. 실제 pickup-delivery 입력에서 pickup과 delivery의 위치 ID는 어떤 필드로 제공되는가? depot, 차량별 terminal, 주문별 pickup, delivery를 합친 물리 위치 집합으로 같은 matrix를 제공하는가?
9. 여러 요청이 동일한 `locId`를 공유할 수 있는가? 가능하다면 위치 속성 일치 검사를 어느 필드까지 적용해야 하는가?
10. 거리·시간 정규화에서 유지할 소수 자릿수와 초과 자릿수 처리 방식은 무엇인가? 기준 데이터의 실제 정밀도는 거리 2자리, 시간 1자리까지다.

질문과 무관하게 확정 가능한 핵심 원칙은 다음과 같다. **현재 production/benchmark 설계에서는 입력된 complete directed distance/time matrix가 유일한 이동 비용의 기준이며, solver core는 Haversine이나 속도로 이를 다시 생성하거나 누락 arc를 조용히 보충하지 않는다.**
