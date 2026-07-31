# 세션 11 - 기존 CVRPTW 입력 스키마와 시간 계약

> **세션 30 통합 상태 (2026-07-23):** 이 문서는 legacy 조사 기록으로 보존한다. 본문의 `Q-TIME-01~04`, `Q-IN-01~02`, `Q-COMP-01`, `Q-BENCH-03` 관련 질문·권장안은 세션 29 사용자 결정으로 해결되었다. Timezone-less `yyyy-MM-dd HH:mm:ss`, `[planStart,planEnd)`, close 포함, `START_ONLY` 기본/profile override, 반복·overnight와 full-arc 출발 연기, `reqDate/dueDate` 완료기한 별칭, service-time 합산, oneway/rotation·wait/stop/drive 계약, `vehicleFeatureList`와 oneway 우선 의미는 [Master §5~§8](../master-design.md)과 [등록부](../deprecated/master-design-open-questions.md)를 따른다. 과거 `남은 질문`은 현재 미결정 목록이 아니다.

이 문서는 기존 CVRPTW 입력 사례인 `data/ro_input_json_spec.pdf` 10쪽 전체를 렌더링 이미지와 텍스트 추출 결과로 함께 확인하고, `master-design.md`와 `domain-design.md`의 입력 및 시간 설계에 반영할 내용을 정리한 작업 기록이다. PDF는 과거 계약을 이해하기 위한 근거 자료이며, PDF 표와 예시가 서로 다른 부분은 임의로 하나를 사실로 확정하지 않는다.

이 세션의 범위는 문서 설계뿐이다. 코드와 기존 설계 문서는 변경하지 않는다.

## PDF에서 확인된 사실

### 공통 단위와 시간 표기

PDF 1쪽은 다음 단위를 정의한다.

| 항목 | 단위 |
|---|---|
| 길이 | mm |
| 시간 및 서비스시간 | sec |
| 무게 | kg |
| 부피 | cbm |
| 속도 | km/h |

시간대가 없는 시각은 `HH:MM:SS`, 날짜와 시각은 `YYYY-MM-DD HH:MM:SS` 예시로 설명한다. `dateRange.from/to` 표에는 RFC3339의 `full-date`라고 적혀 있으나, PDF의 자료형 설명과 예시 JSON은 공백으로 날짜와 시간을 구분하고 시간대 또는 UTC offset을 포함하지 않는다. 따라서 PDF만으로는 실제 허용 포맷과 시간대를 확정할 수 없다.

### Plan과 `dateRange`

PDF 2-4쪽에서 확인되는 Plan 계약은 다음과 같다.

| 필드 | 필수 여부 | PDF의 의미 |
|---|---|---|
| `planId` | 필수 | 시뮬레이션의 유일 ID |
| `continent` | 필수 | 대륙별 도로 거리 계산에 필요한 값 |
| `dateRange` | 필수 | 솔버가 계획을 생성할 기간 |
| `dateRange.from` | 필수 | 계획 시작 일시 |
| `dateRange.to` | 필수 | 계획 종료 일시 |
| `options` | 선택 | 실행 세부 옵션 |
| `depot` | 필수 | 모든 물품을 보관하고 차량이 물품을 상차하여 출발하는 차고지 |

PDF 본문은 Plan마다 차고지 정책이 하나라고 명시한다. 그러나 필드 표에서는 `depot`을 object로 설명하고, 예시 JSON에서는 한 건을 담은 배열로 표현한다. 복수 차고지를 지원한다는 근거는 없으며, object와 1원소 array 중 어느 직렬화 형태가 실제 계약인지 PDF만으로 확정할 수 없다.

차고지의 시간 관련 필드는 다음과 같다.

| 필드 | 필수 여부 | 기본값 | PDF의 의미 |
|---|---|---:|---|
| `openTime` | 선택 | `00:00:00` | 차고지 개장 시각 |
| `closeTime` | 선택 | `23:59:59` | 차고지 폐장 시각 |
| `taskTime` | 선택 | `0` | 상차 및 물품 이동 등에 드는 차고지 작업시간, 초 |

PDF는 차량의 차고지 출발과 도착이 모두 개장 및 폐장 시각 사이여야 한다고 설명한다. 개장 전 또는 폐장 후라면 다음 개장 시각까지 기다린다고도 설명한다. 다만 `taskTime`이 계획당 한 번인지, 차량 출발당 한 번인지, 회차별 한 번인지, 주문별로 합산되는지는 설명하지 않는다.

### Plan Option

PDF 4-5쪽의 옵션은 다음과 같다.

| 옵션 | 기본값 | PDF의 의미 |
|---|---:|---|
| `trips` | `oneway` | `oneway`는 차고지 미복귀, `roundtrip`은 차고지 복귀 |
| `multirotation` | `0` | `-1`은 무제한 차고지 재방문, `0`은 재방문 불가, 양수는 지정 횟수만큼 재방문 |
| `distanceCalculate` | `OSRM` | `OSRM` 또는 `GreatCircle` 거리 계산 |
| `defaultSpeed` | 문서에 없음 | 설정하면 모든 차량에 적용하는 기본 속도 |
| `waitInDepot` | `N` | 첫 방문지 개장시각에 맞추어 차고지 출발을 늦출 수 있는 정책 |

`waitInDepot`는 주문의 가능 여부를 바꾸는 시간창이라기보다, 불필요한 차고지 밖 대기를 줄이기 위한 출발시각 선택 정책으로 설명되어 있다.

옵션 표와 예시 JSON 사이에는 다음 차이가 있다.

- 표는 `multirotation`, 예시는 `multiRotation`을 사용한다.
- 표는 정수 타입을 정의하지만 예시는 문자열 `"0"`을 사용한다.
- 예시는 `driverRestTimeRatio: "0.15"`를 포함하지만 옵션 표에는 이 필드의 의미, 단위, 기본값과 범위가 없다.
- `defaultSpeed`는 설명만 있고 타입, 단위, 기본값과 허용 범위가 표에 채워져 있지 않다. PDF 1쪽의 공통 단위와 차량별 `speed` 설명을 통해 속도 단위가 km/h임은 확인할 수 있지만, 우선순위와 적용 방식은 확정할 수 없다.

### Order

PDF 5-7쪽에서 확인되는 주문의 시간 관련 필드는 다음과 같다.

| 필드 | 필수 여부 | 기본값 | PDF의 의미 |
|---|---|---:|---|
| `reqDate` | 선택 | Plan 종료 일시 | 주문의 request date |
| `duration` | 선택 | 문서에 없음 | 주차, 하역 이동 등 방문지 작업시간, 초 |
| `openTime` | 선택 | `00:00:00` | 고객 개장 시각 |
| `closeTime` | 선택 | `23:59:59` | 고객 폐장 시각 |
| `items[].taskTime` | 선택 | `0` | 품목 유형별 설치 작업시간, 초 |

PDF는 고객 위치에서도 차량의 도착과 출발이 모두 `openTime`과 `closeTime` 사이여야 한다고 설명한다. 따라서 이 문구를 그대로 적용하면 서비스 시작만 아니라 서비스 완료 후 출발도 폐장시각 안이어야 한다. 개장 전 또는 폐장 후에는 다음 개장 시각까지 기다린다고 설명한다.

`items[].taskTime`은 수량을 곱하지 않고 품목 유형별로 한 번씩 합산하는 예시가 명시되어 있다. 세 품목의 `qty=10`, `taskTime=10`일 때 합계는 300초가 아니라 30초라고 설명한다. 반면 `order.duration`과 품목별 `taskTime`을 함께 입력했을 때 두 값을 더하는지는 PDF에 없다.

주문 표와 예시 JSON 사이에는 다음 차이가 있다.

- 표는 `reqDate`를 정의하지만 예시는 `dueDate`를 사용한다.
- 표는 `duration`을 정의하지만 일부 예시는 주문 수준의 `taskTime`을 사용한다.
- 예시에 있는 `locId`, `customerId`는 주문 속성 표에 없다.
- 숫자 필드는 표에서 number/integer지만 예시에서는 대부분 문자열로 직렬화되어 있다.

특히 `reqDate`의 설명은 단순히 request date이고 기본값은 Plan 종료 일시인 반면, 예시 필드명은 `dueDate`다. 이 자료만으로 `reqDate`가 주문 해제시각, 희망일, 마감시각 중 무엇인지 확정할 수 없다.

### Vehicle

PDF 7-8쪽에서 확인되는 차량 시간 및 경로 제한은 다음과 같다.

| 필드 | 필수 여부 | 기본값 | PDF의 의미 |
|---|---|---:|---|
| `workStartTime` | 선택 | `00:00:00` | 차량이 이동할 수 있는 근무 시작시각 |
| `workEndTime` | 선택 | `23:59:59` | 차량이 이동할 수 있는 근무 종료시각 |
| `speed` | 선택 | 문서에 없음 | 차량 속도, km/h |
| `maxStopCnt` | 선택 | 문서에 없음 | 최대 정차 또는 주문 적재 횟수 |
| `maxDriveDistc` | 선택 | 문서에 없음 | 최대 주행거리, meter |
| `maxDriveTime` | 선택 | 문서에 없음 | 최대 운전시간, seconds |

PDF는 차량이 `workStartTime`과 `workEndTime` 사이에 이동할 수 있다고만 설명한다. 근무 종료 전에 시작한 이동을 종료 후에도 계속할 수 있는지, 그 자리에서 중단하고 다음 근무일에 재개하는지, 경로 자체를 불가능하게 하는지는 설명하지 않는다. 최대 정차 수, 거리와 운전시간이 회차, 일, 차량 경로 또는 전체 계획 중 어느 범위에 적용되는지도 설명하지 않는다.

차량 표는 `vehicleFeature`를 string으로 정의하지만 허용값 설명에는 복수 지정이 가능하다고 적혀 있다. 예시는 단일 문자열을 사용한다. 예시의 `driverSkill`은 차량 속성 표에 없다.

## RPDPTW 변환 매핑

기존 CVRPTW JSON을 곧바로 솔버 코어 타입으로 역직렬화하지 않고, 다음 경계를 둔다.

```text
기존 CVRPTW JSON
-> Legacy CVRPTW 입력 DTO와 호환성 파서
-> 의미 검증 및 정규화
-> 고객사 독립 비즈니스 모델
-> RPDPTW 변환기
-> 정수 기반 ProblemInstance
```

이 경계를 두면 과거 JSON의 필드명, object/array, 숫자 문자열 같은 직렬화 특이사항을 코어에 전파하지 않고도 호환할 수 있다. 반대로 의미가 다른 필드를 단순 alias로 처리해 잘못된 제약을 만드는 것도 막을 수 있다.

### 필드별 목표 매핑

| 기존 CVRPTW 필드 | 정규화 대상 | RPDPTW에서의 역할 | 상태 |
|---|---|---|---|
| `planId` | 계획 ID | `ProblemInstance.name` 및 추적 ID | 바로 매핑 가능 |
| `dateRange.from/to` | 정확한 계획 시작 및 종료 일시 | `PlanningPeriod`와 내부 horizon 경계 | 종료 경계와 시간대 확인 필요 |
| `depot` 위치 | 공통 차고지 | 차량 출발 위치, roundtrip 도착 위치, CVRPTW 주문의 공통 상차 위치 | 바로 매핑 가능하나 직렬화 형태 확인 필요 |
| `depot.openTime/closeTime` | 차고지 반복 운영시간 | 출발, 복귀 및 회차 상차 가능 창 | 일별 반복과 완료 규칙 확인 필요 |
| `depot.taskTime` | 차고지 서비스시간 | 최초 상차 또는 회차 재상차 작업시간 | 적용 단위 확인 필요 |
| `options.trips` | 경로 종료 정책 | open route 또는 depot-return route | 명시적으로 모델링 필요 |
| `options.multirotation` | 회차 정책 | 차고지 재방문과 재상차 허용 횟수 | v1 범위 확인 필요 |
| `options.waitInDepot` | 출발시각 정책 | 첫 방문 창에 맞춘 지연 출발 전략 | RoutePropagator 또는 departure policy로 분리 |
| `order.openTime/closeTime` | 고객 반복 운영시간 | delivery node 시간창 | 완료 기준과 반복 규칙 확인 필요 |
| `order.duration` | 방문 기본 서비스시간 | delivery node 서비스시간 | 바로 매핑 가능 |
| `items[].taskTime` | 품목 유형별 추가 서비스시간 | delivery node 추가 서비스시간 | 수량을 곱하지 않음. `duration`과의 합산은 확인 필요 |
| `reqDate` 또는 `dueDate` | 주문의 절대 시각 제약 | release time, deadline 또는 지정일 필터 | 의미 확인 전 매핑 금지 |
| `vehicle.workStartTime/workEndTime` | 차량 반복 근무시간 | `SolverVehicle.workWindows` | 근무 종료 경계 정책 확인 필요 |
| `vehicle.maxDriveTime` | 운전시간 한도 | hard route constraint | 적용 범위 확인 필요 |
| `vehicle.maxDriveDistc` | 운행거리 한도 | hard route constraint | 적용 범위 확인 필요 |
| `vehicle.maxStopCnt` | 방문 수 한도 | hard route constraint | pickup node를 포함하는지 확인 필요 |
| `vehicle.speed`, `defaultSpeed` | 시간 계산 파라미터 | 이동시간이 입력되지 않는 경우의 보조값 | 실제 이동시간 입력 계약과 우선순위는 별도 확정 |

### CVRPTW 주문을 RPDPTW 요청으로 바꿀 때의 주의점

PDF의 기존 모델은 모든 물품이 단일 차고지에 있고 차량이 그곳에서 상차한 뒤 고객에게 배송하는 형태다. 따라서 주문마다 실제로 독립된 픽업 위치를 방문하는 일반 PDPTW 입력이라고 볼 근거는 없다.

RPDPTW로 정규화할 때는 다음 의미를 보존해야 한다.

- 각 주문의 픽업 원천은 공통 차고지다.
- 한 회차에 배정된 여러 주문은 회차 시작의 상차 작업으로 함께 적재될 수 있다.
- 주문마다 가상 pickup node를 만들더라도 그것이 고객 배송 사이의 실제 차고지 재방문을 뜻해서는 안 된다.
- `multirotation=0`이면 최초 출발 뒤 차고지 재방문 및 재상차가 없어야 한다.
- `multirotation>0` 또는 `-1`이면 허용된 차고지 재방문이 새 회차의 경계를 만든다.
- `oneway`는 마지막 고객 뒤 차고지 복귀 이동을 강제하지 않고, `roundtrip`은 복귀 이동과 차고지 도착시간 검사를 포함한다.

따라서 현재 마스터 설계처럼 모든 주문에 차고지 좌표의 pickup node를 단순 생성하는 것만으로는 부족하다. 최소한 동일 회차의 모든 pickup 작업이 첫 delivery보다 앞선다는 제약이 필요하다. 다른 대안은 CVRPTW 입력에 한해 회차 시작의 묶음 적재 상태를 만들고, RPDPTW의 요청 쌍 의미와 물리 방문 의미를 분리하는 것이다. 어느 표현을 표준으로 삼을지는 문제 모델 세션의 결정에 따르되, 이 입력 계약은 주문별 차고지 재방문을 요구하지 않는다는 사실을 마스터에 명시해야 한다.

## 시간·계획기간 규칙

### 외부 계획기간과 내부 시간축

PDF의 필수 `dateRange.from/to`를 외부 계약의 기준으로 유지하고, `baseDate + 계획 일수`를 별도 외부 입력으로 만들지 않는다. 내부에서는 다음처럼 파생한다.

```text
dateRange.from/to
-> 형식과 시간대 정규화
-> PlanningPeriod(start, end)
-> TimeContext origin과 horizonStartSec/horizonEndSec
-> 반복 운영시간 및 근무시간 전개
-> 계획기간 경계로 clip
```

`TimeContext.origin`은 산술 기준일의 자정으로 둘 수 있지만, 실제 실행 가능 범위는 반드시 정확한 `dateRange.from/to` 초 경계로 별도 보존해야 한다. 예를 들어 `from=2026-07-23 08:00:00`인 입력에서 00:00부터 08:00까지를 계획 가능 시간으로 열어서는 안 된다. `domain-design.md`의 `Plan.baseDate`만으로는 이 정보를 보존할 수 없다.

`PlanningHorizon`은 필수라고 서술되어 있지만 현재 두 설계 문서에는 구체적인 값 타입 정의가 없고, `domain-design.md`의 `Plan`에도 포함되어 있지 않다. 목표 설계에서는 정확한 시작과 종료를 갖는 `PlanningPeriod`를 비즈니스 모델의 필수 필드로 두고, `PlanningHorizon`은 그 정규화 결과로 정의한다.

### 반복 시간창

PDF의 차고지, 주문 및 차량 시간은 날짜 없는 `HH:MM:SS`이므로 하나의 날짜에 붙여 절대 시간창으로 바꿔야 한다. 현재 설계의 `BusinessTimeSpec -> expand -> filter -> normalize -> TimeWindowSet` 흐름은 이 변환에 적합하다.

다만 PDF는 이 시간이 `dateRange`의 매일 반복된다고 직접 명시하지 않고, 요일별 휴무, 공휴일, 날짜별 예외와 하루 여러 구간도 정의하지 않는다. 기존 호환 프로필에서 매일 반복으로 해석할지는 확인이 필요하다. 매일 반복으로 확정되면 다음 순서를 사용한다.

1. 계획기간과 사이트 시간대에 포함되는 각 local date에 `openTime/closeTime`을 전개한다.
2. `closeTime < openTime`의 야간 운영 의미는 별도 확인 후 적용한다.
3. 절대 창을 정확한 `dateRange.from/to` 경계로 자른다.
4. 빈 창을 제거하고 정렬 및 병합한다.
5. 가능한 창이 없으면 해당 노드 또는 차량을 시간상 불가능한 것으로 진단한다.

### 고객 서비스시간과 시간창

PDF의 기존 계약은 고객 위치의 도착과 출발이 모두 운영시간 안이어야 한다. 반면 현재 마스터와 도메인 설계의 기본값은 서비스 시작만 창 안이면 완료가 폐장 이후여도 허용한다. 두 요구를 하나의 전역 규칙으로 고정하지 않고 다음 정책을 정규화 단계에 둔다.

| 정책 | 시작 가능 창 변환 | 용도 |
|---|---|---|
| `START_ONLY` | 원래 close 유지 | 현재 마스터의 일반 기본 정책 |
| `COMPLETE_WITHIN_WINDOW` | `latestStart = close - serviceTime` | PDF의 기존 CVRPTW 호환 프로필 |

이 정책은 코어 시간 전파를 두 벌로 만들 필요가 없다. 변환기가 서비스 시작 가능 창을 만들면 코어는 동일하게 `serviceStart` 포함 여부만 검사할 수 있다. 서비스시간이 창 길이보다 길면 해당 창은 제거한다.

서비스시간의 후보 계산식은 아래와 같지만, PDF에 합산 규칙이 없으므로 확정하지 않는다.

```text
deliveryServiceTime
= order.duration
+ sum(items[].taskTime by item entry, without multiplying qty)
```

예시의 주문 수준 `taskTime`을 `duration`의 구버전 alias로 볼지도 확인이 필요하다.

### 차고지 시간

차고지 창은 고객 창과 다른 사건을 제한한다.

- 최초 회차: 상차 시작, 상차 완료 및 차량 출발
- 추가 회차: 차고지 도착, 재상차 및 다시 출발
- roundtrip 종료: 최종 차고지 도착
- oneway 종료: 최종 고객 이후 차고지 도착 제약 없음

PDF의 “도착과 출발이 모두 창 안” 규칙을 적용하면 출발 회차의 `latestLoadStart`는 최소한 `closeTime - depotTaskTime`보다 늦을 수 없다. 다만 최종 roundtrip 도착 뒤 추가 작업시간을 적용하는지는 PDF에 없다.

`depot.taskTime`은 주문별 pickup service time으로 복제하면 한 차량에 주문 수만큼 반복 합산될 수 있다. PDF가 이를 요구한다는 근거가 없으므로 현재 마스터의 `depotTaskTime -> 각 pickup 서비스시간` 매핑은 보류하고, 최초 상차 또는 회차별 상차 서비스시간으로 모델링하는 안을 우선 검토한다.

### 차량 근무시간

차량은 `workStartTime/workEndTime` 사이에만 이동할 수 있다는 PDF 규칙을 hard constraint로 보존한다. 그러나 경계를 넘는 이동의 처리 방식은 PDF에 없으므로 현재 마스터의 “현재 위치에서 운전을 중단하고 다음 근무 시작에 재개”를 기존 입력 계약에서 도출된 사실로 쓰면 안 된다.

설계는 최소한 다음 두 정책을 구분할 수 있어야 한다.

| 정책 | 의미 |
|---|---|
| `FORBID_CROSSING` | 한 이동 arc가 현재 근무창 안에 끝나지 않으면 해당 스케줄 불가 |
| `PAUSE_AND_RESUME` | 근무 종료에 운전을 중단하고 다음 근무창에 남은 시간을 계속 운전 |

현재 마스터의 정책은 `PAUSE_AND_RESUME`다. 기존 CVRPTW 프로필이 어느 쪽을 사용했는지는 확인 질문으로 남긴다.

`maxDriveTime`은 휴식과 고객 대기를 제외한 순수 운전시간으로 계산하는 것이 필드명과 단위에 부합하지만, 회차별, 일별 또는 전체 경로별 한도인지는 PDF에 없다. `routeDuration`과 혼용하지 않도록 별도 측정값으로 유지한다.

### `reqDate`와 `dueDate`

현재 마스터는 `reqDate` 이전 시간창을 제거하여 earliest service 또는 release time으로 해석한다. PDF는 이 의미를 명시하지 않으며 기본값을 Plan 종료 일시로 적고, 예시에서는 이름이 다른 `dueDate`를 사용한다.

따라서 다음을 확정하기 전에는 `reqDate`를 시간창 필터로 사용하지 않는다.

- `reqDate`가 이 시각 이전 배송 금지인지
- 이 시각까지 배송해야 하는 마감인지
- 특정 영업일을 선택하기 위한 요청일인지
- `dueDate`가 같은 필드의 과거 이름인지 별도 deadline인지

의미가 확정되면 `releaseAt`, `dueAt` 또는 `requestedServiceDate`처럼 역할이 드러나는 정규화 필드로 바꾸고, legacy JSON 이름은 adapter에만 남긴다.

## 호환성 또는 불일치

| 항목 | PDF | 현재 master/domain 설계 | 판정 및 조치 |
|---|---|---|---|
| 계획기간 | 정확한 `dateRange.from/to` 필수 | 마스터는 시작일과 일수 예시, domain `Plan`은 `baseDate`만 보유 | 불일치. 정확한 시작/종료를 `Plan` 필수값으로 추가 |
| `PlanningHorizon` | 별도 이름 없음 | 필수라고 서술되지만 값 타입 정의와 `Plan` 필드가 없음 | 설계 누락. `PlanningPeriod`에서 파생되는 구체 타입 정의 |
| 시간대 | 명시 없음 | `LocalDateTime` 사용, `ZoneId` 없음 | 결정 필요. 사이트 기준 시간대 없이는 DST 및 offset 입력 정규화 불가 |
| 날짜 형식 | 표의 RFC3339 표현과 예시의 공백 형식이 충돌 | 하나의 명시 계약 없음 | 호환 parser와 canonical 출력 포맷을 분리 |
| horizon 종료 경계 | 포함/미포함 미정 | 시간창은 close 포함 방식 | 결정 필요. 동일 시각 경계 테스트 추가 |
| 차고지 cardinality | 표는 object, 예시는 1원소 array, 정책은 단일 차고지 | 마스터는 평면 `depotLatitude` 필드처럼 서술 | 불일치. legacy 입력 형태와 정규화된 단일 `Depot` 분리 |
| 차고지 작업시간 | 상차 및 물품 이동 시간이나 적용 단위 미정 | 주문별 pickup node 서비스시간으로 매핑 | 고위험 불일치. 회차별 상차시간 우선 검토 |
| 고객 창 완료 기준 | 도착과 출발 모두 창 안 | 서비스 시작만 창 안이면 허용 | 불일치. 고객/프로필별 completion policy 도입 |
| 폐점 후 처리 | 다음 개장까지 대기 | 다음 날짜 창을 탐색 | 대체로 호환. 반복 및 horizon 경계 명시 필요 |
| 야간 `close < open` | 설명 없음 | 다음 날까지 이어지는 야간 창으로 확정 | 확장 기능이며 legacy 의미 확인 필요 |
| 다중 시간창 및 휴일 | PDF에 없음 | 확장 seam을 제안 | 하위 호환 가능한 확장. 기본 legacy 동작과 분리 |
| `reqDate` | request date, 기본 Plan 종료 | earliest service로 해석해 이전 창 제거 | 의미 불일치 가능성이 큼. 확인 전 적용 금지 |
| 예시의 `dueDate` | 표에 없고 예시에 존재 | 별도 모델 없음 | deadline인지 alias인지 확인 필요 |
| 서비스시간 합산 | 품목 task time은 수량 미곱셈 | `duration + items.taskTime` 합산 | 일부 호환. 두 종류를 더하는 근거는 추가 확인 필요 |
| 주문 수준 `taskTime` | 예시에 존재, 표에는 `duration` | 매핑 없음 | legacy alias 여부 확인 필요 |
| 차량 근무창 | 창 안에서만 이동 | 근무 종료 시 도중 정지 및 익일 재개 | PDF보다 강한 가정. 정책 선택으로 분리 |
| `trips` | oneway/roundtrip | 입력 매핑과 도메인 필드 없음 | 누락. route terminal policy 추가 |
| `multirotation` | 차고지 재방문 횟수 | 완전한 multi-trip은 초기 범위 밖 | 범위 충돌. 지원, 명시적 거부 또는 제약된 지원 결정 필요 |
| `waitInDepot` | 지연 출발 정책 | 매핑 없음 | 누락. departure policy 추가 |
| `driverRestTimeRatio` | 예시에만 존재 | 매핑 없음 | 의미 불명. 무시 여부도 계약으로 정해야 함 |
| `maxStopCnt` | 최대 stop이나 설명이 모호함 | `maxStops` | RPDPTW의 가상 pickup까지 세면 의미가 달라짐. 업무 stop 정의 필요 |
| `maxDriveTime` 범위 | 미정 | route 수준처럼 사용 | 일/회차/전체 경로 범위 확인 필요 |
| 숫자 타입 | 표는 number/integer, 예시는 문자열 | 강제 타입 정책 없음 | legacy 관용 입력과 canonical strict 출력 분리 권장 |

## 마스터 반영안

### 1. PDF의 지위를 명시한다

마스터의 입력 장에 이 PDF를 “기존 CVRPTW 호환 입력의 참고 계약”으로 등록한다. PDF 표와 예시가 충돌하므로 그대로 복제한 신규 규범 명세로 취급하지 않고, 신규 canonical 계약과 legacy 허용 형식을 별도 표로 관리한다.

```text
Legacy accepted forms
-> 과거 입력을 읽기 위한 호환 규칙

Canonical input contract
-> 신규 생성자와 문서가 따라야 할 단일 규칙
```

### 2. `Plan`에 정확한 계획기간을 추가한다

`domain-design.md`의 `Plan.baseDate`를 유일한 기간 정보로 사용하지 않는다. 개념적으로 다음 값을 필수화한다.

```text
PlanningPeriod
- start date-time
- end date-time
- planning zone
- 명시된 경계 포함 규칙
```

`baseDate`가 필요하면 planning zone에서 start가 속한 local date로 파생한다. `ProblemInstance.horizon`에는 정규화된 시작 및 종료 초가 들어가며, 모든 노드 창과 근무창은 이 범위를 벗어날 수 없다.

### 3. Plan Option을 도메인에 승격한다

현재 매핑에서 빠진 다음 항목을 각각 의미 있는 정책으로 만든다.

- route terminal policy: `ONE_WAY`, `ROUND_TRIP`
- rotation policy: 허용 차고지 재방문 횟수 및 무제한 여부
- departure policy: 즉시 출발 또는 첫 방문지에 맞춘 지연 출발
- travel source policy: 실제 입력 행렬, 외부 계산 결과 또는 fallback의 우선순위

문자열 option map을 코어까지 전달하지 않는다. 입력 adapter가 enum 및 명시적 값으로 정규화하며, 지원하지 않는 값은 조용히 무시하지 않고 검증 결과에 남긴다.

### 4. 시간 정책을 데이터와 변환기로 분리한다

공통 코어는 절대 `TimeWindowSet`과 서비스시간만 처리하고, 다음 고객사 또는 입력 프로필 차이는 변환기 정책으로 둔다.

- `START_ONLY` 대 `COMPLETE_WITHIN_WINDOW`
- `FORBID_CROSSING` 대 `PAUSE_AND_RESUME`
- daily repeat, overnight window, 휴일 및 날짜 예외 생성 규칙
- `reqDate`/`dueDate`의 release 또는 deadline 필터
- `waitInDepot` 출발시각 선택

정책 식별자와 버전을 입력 fingerprint 및 결과 메타데이터에 포함해야 같은 입력의 시간 의미를 재현할 수 있다.

### 5. Legacy adapter의 허용 범위를 명시한다

호환 adapter에서는 다음 후보를 검토하되, 의미를 확인한 뒤 확정한다.

- `depot` object와 1원소 array를 읽고 내부에서는 단일 `Depot`으로 정규화
- 숫자 및 숫자 문자열을 읽고 canonical 출력은 JSON number로 통일
- `multirotation`과 `multiRotation`을 같은 정규화 필드로 수용
- 날짜 입력은 과거 공백 형식과 확정된 canonical 형식을 읽되, 출력은 하나로 통일
- 표에 없는 필드는 무시하지 말고 unknown 또는 unsupported diagnostic으로 남김

`reqDate`와 `dueDate`, `duration`과 주문 수준 `taskTime`은 이름만 보고 alias로 합치지 않는다. 의미가 확인된 뒤에만 변환한다.

### 6. CVRPTW-to-RPDPTW 상차 의미를 고친다

현재의 `depotTaskTime -> 주문별 pickup serviceTime` 매핑을 확정 규칙에서 제거한다. 차고지 상차는 회차 시작의 작업이고, 주문별 pickup은 요청의 선행관계를 표현하는 논리 노드일 수 있음을 분리해 설명한다.

마스터에는 다음 불변조건을 추가한다.

- 같은 회차의 모든 배송 수요는 해당 회차 상차 이후에만 배송된다.
- 가상 pickup은 불필요한 차고지 재방문을 만들지 않는다.
- 차고지 재방문은 rotation policy가 허용한 명시적 회차 경계에서만 발생한다.
- `ONE_WAY`와 `ROUND_TRIP`은 종료 terminal 및 시간 검사를 다르게 한다.

### 7. 시간 검증 완료 기준을 추가한다

최소 문서 테스트 행렬은 다음을 포함한다.

- `dateRange.from == to`, `from > to`, 자정과 정확한 종료 경계
- 계획 시작이 00:00이 아닌 경우의 clip
- 일반 일간 창, 폐점 후 다음 날 창, 계획 마지막 날
- `COMPLETE_WITHIN_WINDOW`에서 서비스시간이 창보다 짧거나 같은 경우와 긴 경우
- depot 상차 완료가 close와 정확히 같은 경우
- oneway와 roundtrip의 최종 depot 도착 검사 차이
- `waitInDepot=Y/N`의 출발시각 차이와 해의 실행 가능성 동일 여부
- 근무 종료 직전 이동의 `FORBID_CROSSING` 및 `PAUSE_AND_RESUME` 차이
- `reqDate`와 `dueDate` 의미가 확정된 뒤 각각의 경계
- `maxDriveTime`의 확정 scope별 누적 및 reset 경계
- 숫자와 숫자 문자열, object와 1원소 array legacy 입력 정규화

## 남은 질문

1. `dateRange.from/to`는 어느 시간대를 기준으로 하는가? Plan 또는 site에 IANA `ZoneId`를 추가할 수 있는가, 아니면 모든 값이 특정 고정 지역시간인가?
2. `dateRange.to`는 포함 경계인가 제외 경계인가? 예를 들어 `to=2023-03-25 00:00:00`일 때 정확히 00:00에 시작하는 서비스가 허용되는가?
3. 신규 canonical 날짜 형식은 RFC3339 offset date-time으로 정할 것인가, 기존 `YYYY-MM-DD HH:MM:SS`를 유지할 것인가? 기존 형식도 계속 받아야 하는가?
4. 차고지, 고객 및 차량의 `openTime/closeTime`, `workStartTime/workEndTime`은 `dateRange`의 매일 반복되는가? `close < open`은 익일 종료 야간 창으로 해석해도 되는가?
5. 기존 CVRPTW 프로필은 PDF 문구대로 서비스 완료 및 출발까지 고객 close 안에 있어야 하는가? 그렇다면 일반 RPDPTW 기본은 `START_ONLY`로 두고 legacy 프로필만 `COMPLETE_WITHIN_WINDOW`를 사용해도 되는가?
6. `reqDate`는 earliest delivery/release time, due deadline, requested service date 중 무엇인가? 예시의 `dueDate`는 같은 의미의 구 필드명인가, 별도 마감시각인가?
7. 배송 서비스시간은 `order.duration + sum(items[].taskTime)`인가? 품목 task time은 PDF대로 `qty`를 곱하지 않는 것이 맞는가? 예시의 주문 수준 `taskTime`은 `duration`의 alias인가?
8. `depot.taskTime`은 차량 최초 출발당 한 번, 각 회차당 한 번, 주문당 한 번 중 어느 방식으로 적용하는가? roundtrip 최종 도착 뒤에도 적용하는가?
9. `multirotation`의 숫자는 “추가 차고지 방문 횟수”, “추가 회차 수” 또는 “총 회차 수” 중 무엇인가? `-1` 무제한을 포함해 초기 버전에서 지원해야 하는가, 지원 전까지 입력 검증으로 명시적 거부해야 하는가?
10. 차량 이동이 `workEndTime`을 넘으면 해당 경로를 불가능하게 해야 하는가, 현재 마스터처럼 이동 중단 후 다음 근무 시작에 재개해야 하는가?
11. `maxDriveTime`, `maxDriveDistc`, `maxStopCnt`는 회차별, 하루별, 차량의 전체 다일 경로별 중 어느 범위에서 reset되는가? `maxStopCnt`에서 공통 차고지의 논리 pickup과 회차 방문도 stop으로 세는가?
12. `waitInDepot=Y`는 첫 고객 도착에 맞추어 가능한 가장 늦게 출발하는 최적화 정책인가, 단순히 depot 밖 대기를 depot 대기로 재분류하는 정책인가? roundtrip 또는 multi-rotation의 각 회차에도 적용하는가?
13. PDF 예시의 `driverRestTimeRatio`는 여전히 유효한 입력인가? 유효하다면 휴식 산정식, 적용 범위와 `workStartTime/workEndTime`의 관계는 무엇인가?
14. Legacy adapter는 `depot` object와 1원소 array, JSON number와 숫자 문자열을 모두 허용해야 하는가? 신규 canonical 출력은 object와 JSON number로 정리해도 되는가?
