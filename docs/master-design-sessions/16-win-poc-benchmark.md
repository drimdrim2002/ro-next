# 세션 16 — WinCommerce PoC 기준 벤치마크

> **세션 30 통합 상태 (2026-07-23):** 본문의 순수 주행시간 공식, 고정 seed `[1..5]`, `100,000` steps, `600초`, seed별 gate와 rotation/diagonal 권장안은 현재 계약이 아니다. 세션 29에서 전체 시간은 `drive + customer/depot wait + service + inter-work-window rest`, oneway는 `multiRotation`을 무시하고 마지막 고객에서 종료, official run은 fixed multi-round plan의 모든 worker가 정상 완료·검증된 final champion으로 확정되었다. Round/worker 수, `maxSteps`, watchdog은 `OPEN — EXPERIMENT_REQUIRED`이며 수치를 만들 수 없다. 현재 fixture의 소수 `D/U`는 정수 matrix 계약에 비준수라 official baseline에 사용할 수 없다. [Master §14](../master-design.md#14-independent-verification과-win-poc-benchmark)와 [등록부](../master-design-open-questions.md)를 따른다.

이 문서는 `data/win_poc_case.json`을 1차 품질 벤치마크 fixture로 사용하는 계약을 정의한다. 구현 코드를 작성하는 문서가 아니라, 이후 `master-design.md`의 벤치마크·완료 기준을 개정할 때 반영할 설계 결정과 미결 사항을 기록한다.

이번 1차 목표는 학술 BKS(best known solution)에 곧바로 근접하는 것이 아니다. 실제 업무 입력 한 건을 끝까지 변환하고, 실행 가능한 해를 만들고, 동일 조건에서 재현하며, 변경 전후 품질을 일관된 순서로 비교할 수 있는 기준선을 먼저 확보하는 것이다.

이 벤치마크의 비교 규칙은 고객사별 목적함수를 대체하는 전역 규칙이 아니다. `WIN_POC_V1`이라는 별도 벤치마크 프로파일에 속한다. 다른 고객사나 학술 인스턴스는 동일한 솔버 결과 측정값을 사용하되 다른 목적 비교 정책을 선택할 수 있어야 한다.

## Fixture 개요

### 식별과 변경 통제

| 항목 | 확인값 |
|---|---:|
| fixture 경로 | `data/win_poc_case.json` |
| 파일 크기 | 14,157,512 byte |
| SHA-256 | `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7` |
| 최상위 형식 | JSON object |
| 계획 범위 | `2023-09-13 00:00:00` ~ `2023-09-14 00:00:00` |
| 주문 요청일 종류 | 1개 (`2023-09-14`) |
| 기존 결과 경로 | `routes`가 빈 배열이므로 정답 또는 baseline 결과는 포함하지 않음 |

공식 baseline 카드는 파일 경로가 아니라 SHA-256을 기준으로 fixture를 식별한다. 같은 이름의 파일이라도 digest가 바뀌면 새 fixture 버전이며 기존 baseline과 직접 비교하지 않는다. 원본 fixture는 수정하지 않고, 정규화 규칙이나 해석이 바뀌면 별도의 정책 버전을 올린다.

### 크기와 구조

| 구성 | 개수·범위 |
|---|---|
| depot | 1개 |
| 주문 | 452건, `orderId` 452개 모두 고유 |
| 주문 위치 | 452개, `locId` 452개 모두 고유 |
| item | 452개, 모든 주문에 item 1개 |
| 차량 | 31대, `vehicleId` 31개 모두 고유 |
| 초기 route | 0개 |
| 거리·시간 행렬 위치 | depot 1개 + 주문 위치 452개 = 453개 |
| 행렬 원소 | 205,209개 = `453 × 453` |
| 행렬 방향쌍 | `(T,F)` 고유 쌍 205,209개로 완전한 방향 행렬 |

행렬의 `T`와 `F`에 나타난 위치 집합은 depot 및 모든 주문의 `locId` 집합과 정확히 일치한다. 행렬은 방향쌍을 제공하므로 대칭을 가정하지 않고 입력된 방향별 값을 사용한다.

행렬 필드의 관찰 결과는 다음과 같다.

| 필드 | 관찰 결과 |
|---|---|
| `C` | 문자열이며 `O` 204,805건, `G` 404건 |
| `T`, `F` | 문자열 위치 ID, 각각 453개 고유값 |
| `D` | 204,756건은 숫자 문자열, 대각선 453건은 JSON number |
| `U` | 205,209건 모두 숫자 문자열 |
| `D` 범위 | 0.0 ~ 453,010.72 |
| `U` 범위 | 0.0 ~ 27,258.1 |

동일 위치 ID의 대각선 453건은 모두 `D=9999`, `U=0`이다. 서로 다른 위치 ID 사이에는 `D=0`, `U=0`, `C=G`인 원소가 404건 있다. 이 404건은 모두 좌표가 정확히 같은 위치끼리의 방향쌍이다. 전체 453개 위치에는 중복 좌표 그룹 25개가 있고, 여기에 속한 위치는 77개이며 가장 큰 그룹은 위치 18개다. 따라서 서로 다른 주문 위치 사이의 0 이동값을 일괄 오류로 판단해서는 안 된다. 반대로 대각선 `D=9999`를 실제 이동거리로 합산해서도 안 되며, 필드 의미를 확정하기 전까지 self arc는 경로에 생성하지 않는다.

대형 행렬을 조사할 때 원문 전체나 위치 ID 목록을 출력하지 않고, top-level key, 배열 길이, 고유 개수, 숫자 범위, 유형 분포와 digest만 집계했다. 이후 fixture 검사 도구도 같은 원칙으로 요약 정보를 남겨 로그와 메모리 사용을 제한한다.

### 업무 제약을 드러내는 분포

| 항목 | 확인값 |
|---|---|
| 주문 무게 | 1.12 ~ 1,451.48 kg, 합계 58,799.335 kg |
| 주문 부피 | 0.07 ~ 6.53 cbm, 합계 278.28 cbm |
| 차량 무게 용량 | 1,500 ~ 7,500 kg, fleet 합계 108,600 kg |
| 차량 부피 용량 | 4.9 ~ 17.64 cbm, fleet 합계 290.57 cbm |
| 주문 서비스시간 | 모든 주문 300초 |
| 주문 시작 시각 | 모든 주문 `05:45:00` |
| 주문 종료 시각 | `10:30:00` 282건, `13:30:00` 169건, `17:30:00` 1건 |
| zone | 11개 |
| 차량 근무시간 | 모든 차량 `00:00:00` ~ `23:30:00` |
| 차량 속도 | 모든 차량 45 km/h |
| 최대 정차 수 | plan option 기준 28건 |
| 운행 형태 | `trips=oneway`, `multiRotation=1` |

부피 기준으로 단순 fleet 총용량과 총수요의 차이는 약 12.29 cbm에 불과하다. 이 합계만으로 실행 가능성을 판정할 수는 없지만, 부피·차량 크기 호환·시간창을 함께 만족하면서 미배정과 차량 수를 줄여야 하는 유의미한 fixture다.

주문 `vehicleFeature`는 차량에 필요한 장비 집합이 아니라 해당 위치에 진입 가능한 차량 크기 유형 목록이다. 차량 유형은 `T1`, `T1.4`, `T1.9`, `T2.5`, `T3.5`, `T5`가 있고 주문별 허용 목록을 적용하면 한 주문이 선택할 수 있는 실제 차량은 최소 13대, 최대 31대다. 분포는 13대 허용 60건, 24대 허용 176건, 29대 허용 126건, 31대 허용 90건이다. 이 fixture에는 크기 유형상 허용 차량이 0대인 주문은 없다.

legacy fixture에는 같은 물리량이 JSON number와 숫자 문자열로 혼재한다. 공식 benchmark reader는 이 fixture에 한해 버전이 명시된 legacy 입력 어댑터를 거쳐야 한다. 조용한 범용 coercion으로 취급하지 않으며, 변환 후에는 세션 09의 고정소수점·단위 정책에 따른 canonical 정수만 솔버와 검증기에 전달한다.

## 정확한 지표 정의

공식 비교 벡터의 이름은 `WIN_POC_V1`이며 다음 네 성분을 가진다.

```text
WinPocMetricVector = (
  regularFleetUnassignedCount,
  usedRegularVehicleCount,
  totalDriveDistanceMeters,
  totalDriveTimeSeconds
)
```

모든 성분은 작을수록 좋다. 점수 계산과 최종 검증은 동일한 정규화 정책을 공유하되, 최종 값은 독립 검증기가 해에서 다시 계산한다.

### 1. 미배정 주문 수

`regularFleetUnassignedCount`는 입력 주문 452건 중 정규 차량 경로에 `ASSIGNED`되지 않은 주문의 수다.

```text
regularFleetUnassignedCount
= count(status in {UNASSIGNED, DEFERRED, OUTSOURCED})
```

현재 fixture에는 이월·외주 옵션이 없으므로 실제로는 최종 `SearchRequestBank` 크기 및 `UNASSIGNED` 결과 수와 같아야 한다. 향후 외주·이월을 추가해도 정규 fleet의 배차 능력을 같은 기준으로 회귀 비교하기 위해 두 상태를 자동으로 `ASSIGNED`에 포함하지 않는다. 외주를 서비스 완료로 볼지 여부가 필요한 고객사는 별도의 목적 프로파일을 사용한다.

다음은 미배정 수에 포함하지 않는다.

- 탐색 도중 destroy되어 일시적으로 bank에 들어간 주문
- 같은 물리 위치의 별도 주문을 하나로 합친 수
- 픽업 노드와 배송 노드를 각각 센 값

입력 요청 하나를 정확히 한 단위로 센다. 최종 결과 파티션 검증이 실패하면 미배정 수를 계산하여 비교하지 않고 전체 실행을 무효로 한다.

### 2. 배차 차량 수

`usedRegularVehicleCount`는 최종 정규 경로에 주문을 하나 이상 가진 고유 실제 `vehicleId` 수다.

- 한 차량이 depot를 재방문해 여러 rotation을 수행해도 1대로 센다.
- 빈 route와 주문이 없는 예비 차량은 세지 않는다.
- 외주·dummy·가상 차량은 정규 배차 차량 수에 포함하지 않는다.
- 같은 차량 ID가 여러 route 조각으로 표현되더라도 한 번만 센다. 단, 서로 겹치는 시각에 두 route를 수행한다면 feasibility 위반이다.

따라서 이 지표는 route 객체 수가 아니라 실제 사용 차량의 cardinality다.

### 3. 전체 거리

`totalDriveDistanceMeters`는 사용된 모든 정규 차량 경로의 실제 이동 arc 거리를 합한 값이다.

```text
routeDistance = Σ distance(fromNode, toNode)
totalDriveDistanceMeters = Σ routeDistance
```

합산 범위는 다음과 같다.

- 시작 depot에서 첫 방문지로 가는 arc를 포함한다.
- 경로 중간의 모든 고객 간 arc와 depot 재방문 arc를 포함한다.
- `trips=oneway`이므로 마지막 방문지에서 depot로 돌아오는 가상의 arc는 포함하지 않는다.
- 같은 좌표의 서로 다른 위치 사이에 입력 행렬이 0을 제공하면 정상적인 0 거리 arc로 인정한다.
- 경로에 없는 차량, 미배정 주문, 출력 직렬화 순서는 거리에 영향을 주지 않는다.

공식 비교값은 세션 09의 거리 canonical 단위인 정수 meter로 만든다. 현재 fixture의 `D`가 거리이고 `U`가 시간이라는 해석은 값 범위상 유력하지만 입력 자체에 필드 설명이 없다. 이 매핑이 확인되기 전까지 baseline 숫자를 확정하지 않는다. 소수 meter를 정수 meter로 바꾸는 정확한 rounding 정책도 정책 ID에 포함한다.

### 4. 전체 시간

`WIN_POC_V1`의 현재 권고 정의는 `totalDriveTimeSeconds`, 즉 모든 정규 차량의 순수 주행시간 합이다.

```text
routeDriveTime = Σ travelTime(fromNode, toNode)
totalDriveTimeSeconds = Σ routeDriveTime
```

전체 거리에 적용한 것과 같은 arc 집합을 사용한다. 고객 대기시간, 고객·depot 서비스시간, 휴식시간, 차량이 출발 전 depot에서 기다린 시간, solver 실행시간은 포함하지 않는다. 다음 값은 분석용 breakdown으로 별도 보고하되 4번째 비교 성분에는 섞지 않는다.

```text
totalWaitTimeSeconds
totalServiceTimeSeconds
totalRouteElapsedTimeSeconds
solverElapsedMillis
```

사용자가 말한 “전체 시간”이 순수 주행시간이 아니라 `주행 + 대기 + 서비스`의 경로 경과시간 합을 뜻한다면 이 성분의 이름과 공식을 바꿔야 한다. 이 부분은 남은 질문에 명시한다. 답변 전 임시 문서 기준은 기존 `RouteMetrics.driveTime` 의미와 일치하는 순수 주행시간이다.

## 비교 순서

`WIN_POC_V1`은 가중합이 아니라 사전식 비교를 사용한다.

```text
A가 B보다 좋음
⇔
1. A.unassigned < B.unassigned, 또는
2. 미배정이 같고 A.vehicles < B.vehicles, 또는
3. 앞의 두 값이 같고 A.distance < B.distance, 또는
4. 앞의 세 값이 같고 A.driveTime < B.driveTime
```

예를 들어 차량 1대를 줄이기 위해 거리가 크게 늘어나더라도 미배정 수가 같다면 차량이 적은 해가 우선한다. 거리가 짧다는 이유로 주문을 더 미배정하거나 차량을 더 사용할 수 없다. 이 우선순위 자체를 하나의 거대한 패널티 값으로 변환하지 않는다.

비교 규칙은 다음을 추가로 보장한다.

- feasibility를 통과한 완전한 결과끼리만 비교한다.
- 네 값은 canonical 정수이므로 `EPS`나 비율 허용오차를 두지 않는다.
- 여러 seed의 “best”는 실제 한 실행의 완전한 벡터 중 사전식 최솟값이다. 각 성분의 독립 최솟값을 조합해 존재하지 않는 가상 결과를 만들지 않는다.
- 네 성분이 모두 같으면 품질상 동률이다. 최종 후보를 하나 선택해야 할 때만 canonical solution fingerprint, `runNumber`, seed 순서의 안정된 tie-breaker를 적용한다.
- solver wall-clock 실행시간은 품질 tie-breaker가 아니다. 성능 회귀 항목으로 별도 취급한다.

이 비교기는 `WIN_POC_V1` 벤치마크 프로파일에 주입되는 정책이다. ALNS 코어, 일반 `Solution`, 공통 `RouteMetrics`에 WinCommerce 전용 순서를 하드코딩하지 않는다.

## Feasibility 검증

좋은 목적값보다 먼저 독립적인 실행 가능성 검증을 통과해야 한다. 솔버 내부 캐시나 출력된 합계를 신뢰해 그대로 비교하지 않고, immutable 문제 인스턴스와 최종 route 순서에서 별도 `SolutionVerifier`가 다시 계산한다.

### 입력 fixture 검증

공식 실행 전 다음을 확인한다.

1. fixture SHA-256과 benchmark manifest의 digest가 일치한다.
2. depot 1개, 주문 452건, 차량 31대, 행렬 위치 453개와 같은 구조 fingerprint가 일치한다.
3. 모든 주문·차량·위치 ID가 기대한 범위에서 고유하다.
4. 행렬의 `(from,to)` 방향쌍이 중복 없이 `453 × 453` 전체를 덮고 노드 집합이 입력 위치와 같다.
5. 숫자 문자열과 JSON number를 legacy 정책에 따라 정확한 10진수로 파싱하고, 단위·scale·rounding 정책을 적용한다.
6. 음수 수요·용량·시간, overflow, 알 수 없는 feature, 잘못된 날짜·시각을 거부한다.
7. 입력 option인 `oneway`, `multiRotation=1`, 최대 정차 수 28의 해석을 benchmark manifest에 명시한다.
8. 행렬의 `C`, `D`, `U`, `T`, `F` 의미와 대각선 처리 규칙이 확정된 matrix adapter 버전을 사용한다.

입력 구조가 달라지면 “알고리즘 회귀”로 판정하지 않고 fixture/adapter 불일치로 중단한다.

### 최종 해 검증

독립 검증기는 최소한 다음을 검사한다.

1. 입력 주문 452건 각각이 정규 경로 한 곳 또는 최종 비배정 상태 중 정확히 한 곳에 존재한다.
2. 중복 배정, 누락, 알 수 없는 주문·차량 ID가 없다.
3. RPDPTW 표현을 사용한다면 한 요청의 픽업·배송이 같은 차량에 각각 한 번 있고 픽업이 먼저다. CVRPTW delivery-only를 변환한 가상 pickup 규칙은 세션 03의 확정 모델을 따른다.
4. 주문의 허용 차량 크기 목록에 실제 차량 유형이 포함된다. 장비·필수 feature의 부분집합 검사는 별도의 hard constraint로 적용한다.
5. 각 route의 무게와 부피가 모든 시점에서 차량 용량 범위 안에 있다.
6. 주문 시간창, 차량 근무시간, 계획 기간, 서비스 완료 규칙을 만족한다.
7. route당 배송 주문 수가 최대 정차 수 28을 넘지 않는다.
8. `oneway` 종료 규칙과 허용된 depot 재방문 횟수를 만족한다.
9. 사용한 모든 방향 arc가 matrix adapter에서 유효하다. 같은 좌표의 0 거리·0 시간 arc는 허용하되 잘못된 self arc나 sentinel을 합산하지 않는다.
10. 거리·주행시간·대기시간·서비스시간을 노드 순서에서 다시 전파해 overflow 없이 계산한다.
11. 고객사 hard constraint가 추가되면 등록된 constraint ID와 version의 독립 검증 규칙도 모두 통과한다.
12. 출력에 기록된 네 지표가 독립 재계산값과 정확히 일치한다.

검증 결과는 단순 boolean이 아니라 `validatorVersion`, 적용한 constraint 목록, 오류 코드와 근거를 남긴다. 하나라도 실패하면 해당 run은 `INVALID_SOLUTION`이며 목적 벡터가 좋아도 baseline이나 후보 경쟁에 포함하지 않는다.

## Seed·step 실행 조건

세션 07의 종료·재현성 계약을 그대로 적용한다. 정상 품질 예산은 step 수이고 시간 제한은 무한 루프와 비정상 장기 실행을 막는 watchdog이다.

### 초기 `WIN_POC_V1` 실행 프로파일

| 항목 | 초기 기준 |
|---|---|
| 공식 seed 집합 | `[1, 2, 3, 4, 5]` |
| 로컬 smoke seed | `1` |
| 공식 `maxSteps` | seed당 100,000 step의 잠정값 |
| watchdog | seed당 600초의 잠정값; 입력의 기존 `Termination.secondsSpentLimit=600`은 품질 예산이 아니라 안전장치로만 해석 |
| 정상 종료 | `MAX_STEPS_REACHED`, `completedSteps=100000` |
| 강한 재현성 검사 | seed 1을 같은 build·설정으로 2회 실행하여 목적 벡터와 canonical solution fingerprint가 동일해야 함 |

100,000 step과 600초는 아직 실측 baseline이 없는 상태에서 시작하기 위한 값이다. 최초 tuning run에서 모든 seed가 watchdog보다 충분히 먼저 끝나는지와 품질 개선 곡선을 측정한다. 값이 부적절하면 baseline 카드 발행 전에 변경할 수 있다. 카드가 발행된 뒤에는 `maxSteps`나 seed 집합을 바꾼 실행을 같은 benchmark version으로 비교하지 않고 `WIN_POC_V2`처럼 새 프로파일로 버전화한다.

공식 quality run은 다섯 seed 모두 다음 조건을 만족해야 한다.

```text
입력·설정 fingerprint 일치
+ 동일 algorithm/build 및 operator set
+ MAX_STEPS_REACHED
+ completedSteps == maxSteps
+ 독립 feasibility 통과
= 비교 가능한 run
```

`WATCHDOG_REACHED`, `PLATFORM_TIMEOUT`, `FAILED`, `INVALID_SOLUTION` 결과는 공식 품질 baseline에서 제외한다. 유효한 부분 해가 있어도 step 예산이 달라 재현 가능한 품질 비교가 아니므로 공식 run을 성공으로 대체하지 않는다. 운영 후보로 보존할 수 있는지 여부는 세션 07의 운영 정책과 별개다.

seed 실행은 병렬화할 수 있지만 각 run 내부의 난수 흐름과 결과는 다른 run의 완료 순서에 의존하지 않아야 한다. 최종 champion은 다섯 결과가 모두 수집된 뒤 `WIN_POC_V1` 비교기로 선택한다.

실행시간은 다음처럼 관측한다.

- 입력 파싱·정규화 시간
- 초기해 포트폴리오 생성 시간
- ALNS step 실행시간
- 독립 검증시간
- 결과 직렬화·저장시간
- 전체 elapsed time

이 값은 watchdog 여유와 성능 회귀를 판단하는 자료이며 네 품질 지표에는 포함하지 않는다.

## Baseline·회귀 카드

`routes`가 비어 있으므로 현재 fixture만으로 네 baseline 숫자를 알 수 없다. matrix 의미와 총시간 정의를 확정하고 공식 seed 실행을 완료한 뒤 최초 baseline 카드를 발행한다. 기대값을 추측해 문서에 넣지 않는다.

### 카드 필수 항목

```text
benchmarkId: WIN_POC_V1
fixture:
  path
  sha256
  structuralFingerprint
inputAdapter:
  id
  version
  matrixFieldMapping
normalizationPolicy:
  id
  version
objectiveProfile:
  id: WIN_POC_V1
  metricOrder
constraintProfile:
  idsAndVersions
execution:
  algorithmBuild
  operatorSetVersion
  initialSolutionPortfolioVersion
  seeds
  maxSteps
  watchdogTimeoutMillis
  seedDerivationVersion
environment:
  jdk
  osArch
  cpuClass
perSeedResults:
  terminationReason
  completedSteps
  feasibilityStatus
  metricVector
  canonicalSolutionFingerprint
  elapsedBreakdown
aggregate:
  championRun
  championMetricVector
  perMetricDistribution
  runtimeDistribution
```

환경 정보는 품질 결정의 입력이 아니라 성능 분석과 재현 조사에 사용한다. 강한 결과 재현은 동일 build와 설정 범위에서 보장하고, build가 바뀐 경우에는 회귀 카드 비교로 품질 변화를 판단한다.

### 회귀 판정

회귀 검사는 다음 순서로 수행한다.

1. fixture·adapter·정규화·목적·제약·seed·step fingerprint가 다르면 `NOT_COMPARABLE`로 중단한다.
2. 입력 검증 또는 독립 feasibility가 한 건이라도 실패하면 hard failure다.
3. 공식 seed 중 하나라도 `MAX_STEPS_REACHED`가 아니면 품질 판정을 내리지 않고 `INCOMPLETE_BENCHMARK`로 실패한다.
4. 재현성 확인 run의 벡터 또는 canonical fingerprint가 달라지면 deterministic regression이다.
5. 후보의 실제 champion 벡터를 baseline champion과 사전식으로 비교한다.
6. 후보 champion이 더 나쁘면 `QUALITY_REGRESSION`, 같거나 더 좋으면 품질 gate를 통과한다.
7. seed별 벡터와 분포는 함께 표시한다. 일부 seed가 나빠졌지만 champion이 유지된 경우 경고로 남기며, 모든 seed no-worse를 hard gate로 할지는 남은 질문에서 확정한다.
8. 실행시간과 메모리는 품질과 분리한다. 초기에는 경고로 기록하고 안정된 측정 환경과 임계치가 생긴 뒤 별도 performance gate를 둔다.

baseline 갱신은 테스트가 자동으로 덮어쓰지 않는다. 알고리즘·목적·제약·정규화 변경 이유, 이전 카드, 새 카드, 지표별 변화, feasibility 증거를 검토한 뒤 명시적으로 승인한다. 지표가 좋아졌더라도 잘못된 제약 완화나 matrix 해석 변경으로 생긴 개선이 아닌지 먼저 확인한다.

회귀 카드의 요약 표는 최소한 다음 형식을 갖는다.

| 구분 | 미배정 | 차량 | 거리(m) | 주행시간(s) | 종료 | 실행 가능 | 실행시간 |
|---|---:|---:|---:|---:|---|---|---:|
| baseline champion | 최초 공식 실행 후 기록 |  |  |  | `MAX_STEPS_REACHED` | PASS | 관측값 |
| candidate champion | 실행값 |  |  |  | 실행값 | PASS/FAIL | 관측값 |
| 판정 | 첫 번째로 달라진 품질 성분을 명시 |  |  |  |  |  | 별도 성능 판정 |

## 후속 학술 benchmark

WinCommerce PoC fixture의 end-to-end 정합성, 재현성, 회귀 카드가 안정된 다음 학술 benchmark를 추가한다. 현재 `master-design.md`의 “Solomon 100개 인스턴스, 논문 Table 1 수준”이라는 표현은 1차 완료 기준에서 제거하고 후속 milestone로 옮긴다.

후속 단계에서는 다음을 별도 manifest로 고정한다.

- 정확한 Solomon 인스턴스 파일 목록과 checksum
- 25/50/100-customer 중 사용할 규모
- 거리 계산과 반올림 규칙
- depot 복귀, 서비스시간, 차량 동질성 등 변환 규칙
- BKS 출처와 버전
- seed와 `maxSteps`
- 차량 수 우선인지 거리 우선인지에 대한 학술 비교 프로파일
- BKS gap 계산식과 허용 임계치

표준 Solomon 인스턴스에서 모든 고객 배정이 문제 정의상 필수라면 미배정 주문은 최적화 성분이 아니라 invalid 판정으로 다루고, `(차량 수, 거리)`를 학술 프로파일의 사전식 지표로 사용할 수 있다. 이는 `(미배정, 차량, 거리, 시간)`인 `WIN_POC_V1`을 변경하는 것이 아니라 별도 프로파일을 추가하는 것이다.

진행 순서는 다음과 같다.

```text
WIN_POC_V1
→ 업무 입력·행렬·제약의 end-to-end 검증
→ seed·step 재현성과 회귀 카드 안정화
→ 소수의 명시된 Solomon 인스턴스 smoke benchmark
→ 정확한 manifest의 전체 Solomon suite
→ BKS gap 및 논문 결과와 비교
→ route pool/MIP hybrid는 그 다음 단계
```

학술 benchmark 확장은 WinCommerce baseline을 덮어쓰거나 동일 집계표에 무리하게 섞지 않는다. 업무 적합성과 학술 해 품질은 서로 다른 카드로 보고한다.

## 남은 질문

1. **행렬 필드 의미:** `D`를 meter 거리, `U`를 second 이동시간, `T/F`를 방향쌍의 양 끝으로 해석하면 되는가? `C=O/G`의 정확한 의미도 확인이 필요하다. 권장안은 기존 생성 시스템의 계약을 찾아 matrix adapter 문서에 명시하는 것이다.
2. **대각선 값:** 모든 self pair의 `D=9999`, `U=0`은 의도된 sentinel인가? 권장안은 self arc를 실제 경로에 생성하지 않고, 독립 검증에서도 연속 동일 node ID를 구조 오류로 처리하는 것이다. 같은 좌표지만 ID가 다른 주문 간 `0/0` arc는 계속 허용한다.
3. **전체 시간의 의미:** 네 번째 지표가 순수 주행시간 합인가, 아니면 주행·대기·서비스를 포함한 route elapsed time 합인가? 이 문서의 임시 권고안은 순수 주행시간이며 나머지는 breakdown으로 보고하는 것이다.
4. **초기 실행 예산:** 공식 seed `[1,2,3,4,5]`, seed당 `100,000` step, watchdog 600초를 최초 calibration 값으로 사용해도 되는가? 실제 측정 후 최초 baseline 발행 전에만 조정하는 것을 권장한다.
5. **기존 비교 결과:** 이전 ECS/CVRPTW 엔진이 이 fixture에 대해 만든 미배정 수, 차량 수, 거리, 시간 또는 route 결과 파일이 있는가? 있다면 새 baseline의 정답으로 맹신하지 않고 `legacy reference` 열에 나란히 기록하여 변환 차이를 조사할 수 있다.
6. **multi-rotation:** `oneway`와 `multiRotation=1` 조합에서 차량당 depot 재방문을 최대 1회 허용하는 것이 맞는가? 재방문 시 depot `taskTime=60초`를 매번 적용하는지도 확인해야 한다.
7. **회귀 강도:** 공식 gate를 champion 벡터 no-worse로 둘 것인가, 다섯 seed 각각의 no-worse까지 요구할 것인가? 권장안은 champion을 hard gate로 하고 seed별 악화는 처음에는 경고로 수집한 뒤 변동성을 확인하여 강화하는 것이다.
8. **성능 gate:** 품질과 별도로 elapsed time과 peak memory의 허용 회귀율을 얼마로 둘 것인가? 안정된 전용 실행 환경이 마련되기 전에는 수치 hard gate보다 추세 경고가 안전하다.
