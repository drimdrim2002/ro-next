# 세션 09 - 고정소수점·단위 변환 정책

> **세션 30 통합 상태 (2026-07-23):** 이 문서는 역사적 review input이다. 본문의 미확정 `n`·rounding·합산 순서는 세션 29의 `Q-NUM-01~03` 사용자 결정으로 대체되었다: 무게·부피 `n=3/FLOOR`, 비용·거리·시간 정수 입력, item-first 정규화 후 `qty` 곱. 현재 계약은 [Master §7.2](../master-design.md#72-fixed-point와-checked-arithmetic), 질문별 evidence/gate는 [등록부](../deprecated/master-design-open-questions.md), 반영 범위는 [세션 30](30-open-question-integration.md)을 따른다. `D/U`와 누락값 생성은 세션 29의 `Q-MTX-01~03` 및 Master §8이 본문의 과거 추정보다 우선한다.

이 문서는 `master-design.md` 개정 시 반영할 수치 정규화 계약을 정리한다. 구현 코드를 정의하는 문서가 아니라, 앞으로 구현할 변환 계층과 솔버 코어 사이의 계약을 확정하기 위한 작업 문서다.

검토 근거:

- `docs/master-design.md` 13장, 15장, 20장
- `docs/domain-design.md` 2.2절, 6.2절, 13장, 14장
- `data/ro_input_json_spec.pdf` 1쪽, 6~8쪽
- `data/win_poc_case.json`의 `distanceMatrix` 실제 값 형식

## 확정 사항

1. 외부 입력의 소수 물리량은 변환 경계에서 고정소수점 정수로 바꾸고, 솔버 코어에서는 `double`과 `EPS` 비교를 사용하지 않는다.
2. 입력 정밀도는 업무에서 합의한 소수 셋째 자리 또는 소수 `n`자리 정책을 적용한다. 다만 정확한 `n`과 초과 자릿수 처리 방식은 아직 문구만으로 확정할 수 없으므로 이 문서의 마지막 질문에 남긴다.
3. 한 물리량의 수요와 용량에는 반드시 같은 입력 단위, scale, 초과 자릿수 처리 방식을 적용한다. 예를 들어 `item.weight`와 `vehicle.maxWeight`는 하나의 무게 규칙을 공유하고, `item.volume`과 `vehicle.maxVolume`은 하나의 부피 규칙을 공유한다.
4. PDF 입력 명세는 무게 `kg`, 부피 `cbm`, `maxDriveDist`의 거리 `meter`, 서비스시간·`maxDriveTime`의 시간 `second`, 속도 `km/h`를 정의한다. 반면 PDF에는 `distanceMatrix` 계약이 없다. `win_poc_case.json`의 행렬 `D`, `U`는 주로 소수 문자열로 들어오므로 그 의미·단위·scale을 위 단위와 같다고 확정하지 않는다. `master-design.md`의 부피 `L` 예시는 PDF 계약에 맞춰 `cbm` 기준으로 수정해야 한다.
5. 고객사 또는 사이트별 차이는 변환 정책 설정으로 흡수한다. ALNS, 경로 적재량 계산, 용량 비교 코드는 고객별 scale이나 rounding을 알지 않는다.
6. 하나의 `ProblemInstance` 안에서는 물리량별 정규화 규칙이 하나로 고정된다. 서로 다른 사이트 규칙을 가진 입력을 한 인스턴스에 혼합하려면 변환 단계에서 먼저 인스턴스의 공통 내부 단위로 통일해야 한다.
7. 적용한 정책은 `policyId`, `policyVersion`, 물리량별 입력 단위, `fractionalDigits`, `roundingMode`와 함께 실행 결과 메타데이터 및 재현성 fingerprint에 포함한다. 거리·시간 행렬은 확정된 `D`·`U` 의미, 단위와 scale도 포함한다. 설정이 달라진 실행은 같은 입력·시드라도 동일 실행으로 간주하지 않는다.
8. 정규화는 비즈니스 입력을 RPDPTW 코어 모델로 바꾸는 Transformer의 책임이다. 이후의 누적, 비교, 캐시 키, 목적함수 입력 및 결과 검증은 모두 정규화된 정수값을 기준으로 한다.

권장 설계 경계는 다음과 같다.

```text
SiteProfile
└─ NumericNormalizationPolicy
   ├─ WEIGHT 규칙
   ├─ VOLUME 규칙
   ├─ 필요 시 COST 규칙
   └─ 차량 속도 보정 규칙

Business input
→ 정확한 10진수 파싱
→ 입력 검증
→ NumericNormalizationPolicy 적용
→ 정수 RPDPTW ProblemInstance
→ 고객사와 무관한 Solver Core
```

`NumericNormalizationPolicy`는 사이트 확장을 위한 seam이지만, 임의의 필드마다 제각각 규칙을 허용하는 구조는 피한다. 기본 단위는 물리량별로 정의하고, 필드는 그 물리량에 연결한다. 예외가 정말 필요한 경우에만 사이트 프로파일에서 명시적으로 override하며, 같은 비교식에 들어가는 필드끼리 scale이 달라지는 override는 허용하지 않는다.

## 단위표

| 물리량 | PDF 외부 단위·형식 | 대상 필드 예 | 코어 내부 표현 | 정규화 규칙 소유자 |
|---|---|---|---|---|
| 무게 | `kg`, decimal number | `items[].weight`, `vehicles[].maxWeight` | `long`, `kg × 10^n_weight` | 사이트의 `WEIGHT` 규칙 |
| 부피 | `cbm`, decimal number | `items[].volume`, `vehicles[].maxVolume` | `long`, `cbm × 10^n_volume` | 사이트의 `VOLUME` 규칙 |
| 수량 | 정수 | `items[].qty` | 정수 | 공통 입력 계약 |
| 거리 제한 | PDF: `meter`, 정수 | `maxDriveDist` | `long` meter | 공통 입력 계약 |
| 행렬 `D` | `win_poc_case.json`: 주로 decimal string이며 숫자형 값도 관찰됨; PDF 계약 없음 | `distanceMatrix[].D` | 단위·scale 확정 후 `long`으로 정규화 | 행렬 입력 정책, 미확정 |
| 서비스·운전 제한 시간 | PDF: `second`, 정수 | `depot.taskTime`, `order.duration`, `item.taskTime`, `maxDriveTime` | `long` second | 공통 입력 계약 |
| 행렬 `U` | `win_poc_case.json`: decimal string; PDF 계약 없음 | `distanceMatrix[].U` | 단위·scale 확정 후 `long`으로 정규화 | 행렬 입력 정책, 미확정 |
| 날짜·시각 | PDF의 date-time 또는 `HH:mm:ss` | plan 시작·종료, 영업시간, 근무시간 | 계획 기준시각 이후 `long` second | 시간 정규화 정책 |
| 속도 | `km/h`, decimal number | `defaultSpeed`, `vehicle.speed` | 직접 경로 계산에 남기지 않고 정수 시간 보정계수로 변환 | 속도 보정 규칙 |
| 길이·규격 | `mm` | 향후 차량·화물 치수 필드 | 정수 mm | 해당 제약을 도입할 때 확정 |
| 비용 | PDF에서 정의하지 않음 | 거리비, 대기비, 외주비 등 | 정책별 정수 또는 정수 목적 벡터 | 고객별 `ScorePolicy` |
| 위도·경도 | decimal number | depot/order latitude, longitude | 솔버 물리량과 분리 | 입력 위치 식별·행렬 조회 계층 |

부피의 내부값은 `L`가 아니라 `cbm × 10^n_volume`으로 설명한다. 예를 들어 `n_volume = 3`으로 최종 확정될 경우 `1.2 cbm`은 내부값 `1200`이 된다. 이 값의 한 단위는 `0.001 cbm`, 즉 1 L와 우연히 같지만, 외부 계약과 필드 의미는 끝까지 `cbm`으로 유지한다.

위도·경도에는 용량의 고정소수점 규칙을 재사용하지 않는다. 실제 거리·시간이 입력되는 설계에서는 좌표는 위치 매칭 또는 진단용일 수 있으며, 확정된 거리·시간 행렬 계약이 우선한다. 현재 `D`를 meter, `U`를 second로 해석하는 것은 값의 크기에 근거한 추정일 뿐 문서로 확인된 계약이 아니다.

## scale·rounding 계약

### 1. 정책의 최소 표현

각 소수 물리량 규칙은 최소한 다음 값을 가져야 한다.

| 속성 | 의미 |
|---|---|
| `dimension` | `WEIGHT`, `VOLUME`, `COST` 등 물리량 |
| `inputUnit` | `kg`, `cbm` 등 외부 단위 |
| `fractionalDigits` | 유지할 소수 자릿수 `n` |
| `scale` | `10^n`; 중복 설정하지 않고 `fractionalDigits`에서 유도 |
| `roundingMode` | 합의된 초과 자릿수 처리 방식 |
| `minValue`, `maxValue` | 정규화 전 허용 범위 |
| `policyId`, `policyVersion` | 고객별 정책 식별 및 재현성 관리 |

`fractionalDigits`와 `scale`을 둘 다 독립 설정으로 두면 `n=3`, `scale=100` 같은 모순이 생길 수 있다. 문서와 설정에서는 `n`을 기준값으로 삼고 `scale=10^n`을 유도한다.

### 2. 변환 시점과 순서

정규화 순서는 다음으로 고정하는 것을 권장한다.

```text
JSON 원문 10진수
→ 유한한 값인지·범위 내인지 검사
→ 필드에 연결된 물리량 규칙 조회
→ 해당 입력 필드 하나를 정수화
→ 정수화된 item 값 × 정수 qty
→ 주문 합계와 경로 합계를 정수로 누적
```

즉 `item.weight`와 `item.volume`을 입력 경계에서 먼저 정수화한 후 `qty`를 곱한다. 모든 후속 계산은 정수이므로 변환 위치에 따라 결과가 바뀌지 않는다. 만약 업무 합의가 “라인 합계 또는 주문 합계를 계산한 뒤 한 번만 자릿수 처리”라는 뜻이었다면 이 순서를 변경해야 하므로 남은 질문으로 기록한다.

JSON decimal은 IEEE-754 `double`로 먼저 근사한 뒤 scale을 곱하는 방식으로 정의하지 않는다. `1.005` 같은 값이 이진 부동소수점 근사 때문에 의도와 다르게 변환될 수 있으므로, 계약상 원문 10진 값을 정확하게 해석한 뒤 명시적 rounding을 적용해야 한다.

### 3. 같은 규칙을 적용해야 하는 범위

다음 필드 그룹은 각각 하나의 규칙을 공유한다.

- 무게 그룹: 모든 `item.weight`, 모든 `vehicle.maxWeight`, 무게 관련 사전검사·미배정 사유 계산·결과 검증
- 부피 그룹: 모든 `item.volume`, 모든 `vehicle.maxVolume`, 부피 관련 사전검사·미배정 사유 계산·결과 검증
- 거리 그룹: `maxDriveDist`, 경로 거리, 거리 기반 점수와 벤치마크 출력. `D`의 의미·단위가 거리로 확정되면 같은 내부 거리 단위로 먼저 정규화한 뒤 이 그룹에 편입
- 시간 그룹: 서비스시간, `maxDriveTime`, 경로 운전시간·대기시간·서비스시간·전체시간. `U`의 의미·단위가 이동시간으로 확정되면 같은 내부 시간 단위로 먼저 정규화한 뒤 이 그룹에 편입
- 비용 그룹: 같은 목적함수 성분에 더해지는 모든 값. 다른 scale의 비용은 합산 전에 해당 고객 `ScorePolicy`의 공통 비용 단위로 변환

입력 검증, 초기해, ALNS 연산자, 최종 `SolutionVerifier`, 결과 출력이 서로 다른 변환을 중복 구현하면 안 된다. 변환된 `ProblemInstance`가 유일한 계산 기준이며, 원본 decimal은 감사·오류 메시지·결과 표시를 위한 원본 데이터로만 보존한다.

### 4. 사이트별 유연성

고객별 차이는 다음 순서로 결정한다.

```text
공통 기본 정책
→ SiteProfile의 물리량별 override
→ ProblemInstance 생성 시 불변 정책 snapshot
```

새 고객이 무게 3자리, 부피 4자리, 비용 2자리를 요구하는 경우 코어 클래스를 수정하지 않고 프로파일 설정과 변환 정책만 추가한다. 단, 한 고객의 `item.volume`과 `vehicle.maxVolume`을 서로 다른 자릿수나 rounding으로 설정하는 것은 구성 오류다.

실행 중 정책 변경도 허용하지 않는다. 정책 버전이 바뀌면 새 `ProblemInstance`를 생성하고 캐시를 분리해야 한다.

### 5. 속도 보정과 이동시간

현재 문서는 속도를 `timeScalePermille`로 바꾸고 다음 정수 산식을 사용한다.

```text
correctedTime = matrixTime × timeScalePermille ÷ 1000
```

여기에는 서로 다른 두 rounding 결정이 있다.

1. `기준속도 ÷ 차량속도`를 정수 보정계수로 바꿀 때의 자릿수와 rounding
2. `matrixTime × 보정계수 ÷ scale`의 나머지를 초 단위로 바꿀 때의 rounding

두 결정을 용량의 `n`자리 정책과 암묵적으로 공유하지 않는다. `U`가 차량별 실제 이동시간을 제공하는 값으로 확정된다면 속도 보정 자체를 적용하지 않을 수도 있다. 현재는 `U`의 의미와 단위가 문서로 확인되지 않았으므로 먼저 행렬 계약을 확정해야 한다. 속도 보정을 적용한다면 별도 설정으로 명시하고, Java 정수 나눗셈에 우연히 맡기지 않는다. 정확한 방식은 남은 질문에 기록한다.

## 입력 검증

입력을 `ProblemInstance`로 만들기 전에 다음을 검증한다.

1. 필수 소수 필드는 누락되지 않아야 하고, 빈 문자열·`NaN`·무한대는 거부한다.
2. 무게, 부피, 속도와 차량 용량은 음수가 될 수 없다. `qty`는 양의 정수여야 한다. 0 허용 여부는 필드별 업무 규칙으로 명시한다.
3. 각 필드는 PDF 단위와 일치해야 한다. 단위 없는 값은 필드 계약의 단위로 해석하며, `L`를 `cbm` 값처럼 암묵적으로 받지 않는다.
4. 입력의 소수 자릿수가 `n`을 초과하면 합의된 rounding을 적용한다. 적용 전 원본값과 적용 후 정수값을 진단 가능하게 남긴다. 정책이 없으면 임의의 기본 rounding을 선택하지 않고 입력 또는 구성 오류로 처리한다.
5. 같은 비교 차원에 속한 필드들의 `dimension`, `inputUnit`, `fractionalDigits`, `roundingMode`가 같아야 한다.
6. scale 적용 결과가 `long` 범위에 들어오는지 변환 전에 검사한다.
7. `item 값 × qty`, 한 주문의 모든 item 합, 한 차량 경로의 누적 적재량이 `long` 범위에 들어오는지 검사한다.
8. `maxDriveDist`는 PDF 계약상 정수 meter이고, 서비스시간과 `maxDriveTime`은 정수 second다. 반면 `distanceMatrix.D/U`는 실제 POC 파일에서 decimal string이므로 별도 행렬 단위·scale·rounding 정책 없이는 정수로 강제 변환하지 않는다.
9. `D`와 `U`의 의미·단위가 확정되면 각각 `maxDriveDist`와 `maxDriveTime` 등 비교 대상의 내부 단위로 명시적으로 변환한다. 원시 문자열을 정수로 절삭하거나, 값의 크기만 보고 meter/second라고 간주하는 것은 금지한다.
10. 좌표값은 위도·경도 범위를 검증하되, 용량 scale로 변환하지 않는다.
11. PDF 표에서는 무게·부피·속도 같은 decimal 필드가 JSON `number`이지만 예제 JSON에는 숫자 문자열도 보인다. `win_poc_case.json`의 `D/U`도 decimal string이며, 일부 `D`에는 숫자형 값이 관찰된다. 정식 입력이 문자열, 숫자 또는 제한된 혼합형 중 무엇인지 필드별로 확정하기 전까지 reader가 임의로 혼용 수용한다고 문서화하지 않는다.
12. 정규화 정책 ID 또는 버전이 누락되었거나 사이트에 등록되지 않았으면 실행을 시작하지 않는다. 이를 기본값으로 조용히 대체하면 재현성과 고객별 계약을 확인할 수 없다.

자릿수 초과는 합의된 변환 대상이지 곧바로 validation error는 아니다. 반대로 값의 단위, 유한성, 부호, 범위, 정책 존재 여부는 자릿수 변환 전에 검증해야 한다.

## overflow

`long` 사용만으로 overflow가 방지되지는 않는다. 문서에는 다음 연산별 상한 검증을 명시한다.

```text
scale = 10^n
normalized field
item normalized value × qty
request item 합계
route load 누적
matrixTime × timeScale
distance × 단가, time × 단가
route cost 합계
unassigned penalty 합계
전체 목적함수 성분 합계
```

설계 규칙:

- `n`은 `10^n`이 표현 가능한 범위와 고객별 최대 입력값을 함께 고려해 상한을 둔다.
- 변환 전 `abs(input) × scale <= Long.MAX_VALUE`를 만족하는지 확인한다.
- 곱셈과 덧셈은 결과가 overflow한 뒤 검사하지 않고, 연산 전 범위 검사 또는 checked arithmetic를 사용한다.
- 목적함수의 각 성분은 독립적인 상한을 가진다. 고객별 가중치가 바뀌면 최악값 검증도 다시 수행한다.
- 무제한·불가능을 나타내는 sentinel은 정상 물리량과 합산하거나 scale 변환하지 않는다. sentinel의 구체적 값과 시간 상한은 세션 10의 결정에 따른다.
- overflow 시 포화값으로 조용히 치환하거나 음수로 wrap하지 않는다. 입력/정책 오류로 중단하고 어떤 필드와 연산이 범위를 넘었는지 보고한다.

인스턴스 생성 시 다음 최악값을 계산해 실행 전에 거부할 수 있어야 한다.

```text
모든 주문의 정규화 수요 합
최대 차량 경로 거리·시간
최대 차량 수 × 최대 경로 비용
최대 주문 수 × 최대 미배정 페널티
각 ObjectiveVector 성분의 최대값
```

## 테스트

### 1. 단위·정규화 예제 테스트

`n=3`을 가정한 아래 값은 설명용 예시이며, 최종 기본값을 확정하는 문구가 아니다.

| 입력 | 초과 자릿수 처리 | 예상 내부값 |
|---:|---|---:|
| `12.345 kg` | 어느 방식이든 정확히 표현 | `12345` |
| `1.2 cbm` | 어느 방식이든 정확히 표현 | `1200` |
| `12.3456 kg` | 내림/절삭 | `12345` |
| `12.3456 kg` | HALF_UP | `12346` |

정책 확정 후 표에서 선택되지 않은 행은 제거하지 말고, 다른 rounding과 결과가 다름을 보여 주는 회귀 테스트로 유지한다.

### 2. 비교 일관성 테스트

- `item.weight`와 `maxWeight`가 같은 입력값이면 항상 같은 정수값이 된다.
- `item.volume`과 `maxVolume`이 같은 입력값이면 항상 같은 정수값이 된다.
- 변환된 수요 합이 용량과 같으면 용량 비교는 실행 가능, 1 내부단위 크면 불가능이다.
- 서로 다른 사이트 정책으로 같은 JSON을 변환하면 정책에 따라 다른 내부값이 될 수 있지만, 동일 정책 ID·버전에서는 항상 같은 값이어야 한다.
- 입력 검증, 솔버 feasibility, 독립 `SolutionVerifier`가 동일한 정규화 결과를 사용해야 한다.

### 3. 경계·오류 테스트

- `0`, 허용 최소값·최대값, 정확히 `n`자리인 값
- `n+1`자리에서 마지막 숫자가 0, 4, 5, 9인 값
- 음수, 빈 값, `NaN`, 무한대, 지수 표기 입력
- 정규화 직전 `long` 최댓값과 이를 1 내부단위 초과하는 값
- 단일 item은 안전하지만 `× qty`에서 overflow하는 값
- 개별 item은 안전하지만 주문 합 또는 경로 누적에서 overflow하는 값
- `kg`/`cbm`, meter/second가 뒤바뀐 구성
- 수요와 용량에 서로 다른 scale 또는 rounding을 지정한 잘못된 사이트 정책

### 4. 변환 순서 테스트

`item 값 → 정수화 → qty 곱`의 기대 결과를 명시하고, `item 값 × qty → 정수화`와 다른 경계 사례를 회귀 테스트로 둔다. 이 테스트는 업무 합의가 어느 순서를 택했는지를 문서와 구현이 함께 고정하도록 한다.

### 5. 오차 범위 속성 테스트

정책이 내림이고 입력이 비음수라면 다음 성질을 검사한다.

```text
0 <= 원본값 - 복원값 < 10^-n
```

정책이 최근접 반올림이라면 그 방식에 맞는 최대 오차를 별도로 정의하고 검사한다. 임의의 decimal과 다양한 `n`에 대해 동일 결과와 오차 경계를 property test로 검증한다.

### 6. 시간 보정 테스트

- 원시 `D/U` decimal string이 이진 부동소수점 손실 없이 파싱되는지 확인한다.
- 확정된 행렬 단위·scale·rounding에 따라 `D/U`가 내부 정수로 변환되는지 확인한다.
- `D/U`의 소수 자릿수 경계, 숫자 문자열과 허용된 숫자형 값, 잘못된 문자열을 검사한다.
- 행렬 정책 확정 후 `U`와 서비스시간·`maxDriveTime`을 같은 내부 단위로 비교할 수 있는지 확인한다.
- 속도 보정계수 생성 시 선택한 scale과 rounding의 경계값을 검사한다.
- `matrixTime × timeScale` overflow를 검사한다.
- 보정 결과에 나머지가 있을 때 선택한 초 단위 rounding이 적용되는지 확인한다.
- 동일 입력·정책 버전에서 플랫폼과 실행 순서에 관계없이 동일 시간이 나오는지 확인한다.

### 7. 재현성·직렬화 테스트

- 결과에 정책 ID·버전·단위·`n`·rounding이 포함되는지 확인한다.
- 정책이 바뀌면 instance fingerprint와 캐시 namespace가 바뀌는지 확인한다.
- 내부 정수를 외부 단위로 표시한 뒤 다시 읽었을 때, 정의된 출력 정밀도 안에서 같은 내부값을 얻는지 확인한다.

## 남은 질문

1. “소수 셋째 자리 또는 `n`번째 자리 이하를 바꾼다”는 표현은 **소수 `n`자리까지 유지하고 `n+1`자리부터 처리한다**는 뜻인가요? 예를 들어 `n=3`일 때 `12.3456`을 `12.345` 또는 `12.346`으로 만드는 규칙인지 확인이 필요합니다.
2. 초과 자릿수의 정확한 처리 방식은 무엇인가요? 현재 `domain-design.md`는 비음수 값에 대해 `FLOOR`(사실상 절삭)을 예시로 들지만, 사용자 답변의 “바꾼다”만으로는 절삭, `HALF_UP`, `HALF_EVEN` 중 하나를 확정할 수 없습니다.
3. `n`은 모든 고객·모든 물리량에 공통으로 3인가요, 아니면 사이트별 또는 무게·부피·비용별로 다를 수 있나요? 최소 변경 구조를 위해 설계상 물리량별 사이트 설정을 지원하되, 실제 허용 범위는 확정해야 합니다.
4. `item.weight × qty`와 `item.volume × qty`는 개별 item 값을 먼저 `n`자리로 변환한 뒤 수량을 곱하나요, 아니면 decimal 라인 합계를 만든 뒤 한 번만 변환하나요? 이 문서는 전자를 권장안으로 기록했습니다.
5. 속도 보정계수와 보정된 이동시간도 용량과 같은 자릿수·rounding을 사용하나요? 별도 규칙이라면 보정계수 생성과 최종 second 변환 각각에 대해 절삭·반올림·올림 중 무엇을 적용할지 정해야 합니다.
6. PDF의 형식 표는 무게·부피·속도를 JSON `number`로 정의하지만 예제는 숫자 문자열을 사용합니다. 신규 GCP 입력 계약에서 숫자만 허용할지, 기존 호환 어댑터에서 숫자 문자열도 허용할지 확인이 필요합니다. `distanceMatrix.D/U`는 별도 계약으로 다뤄야 합니다.
7. 고정소수점 적용 후 결과 JSON은 원래 decimal 단위만 출력하면 되나요, 아니면 감사와 재현성을 위해 정규화된 정수값 및 정책 메타데이터도 함께 출력해야 하나요? 후자를 권장합니다.
8. `win_poc_case.json`의 `distanceMatrix.D`와 `U`는 각각 정확히 무엇을 의미하며 외부 단위는 무엇인가요? 값의 크기로는 거리와 시간을 추정할 수 있지만, PDF에는 이 행렬 계약이 없어 `D=meter`, `U=second`로 확정할 근거가 없습니다.
9. `D/U`의 허용 소수 자릿수, 내부 scale, 초과 자릿수 rounding은 무엇인가요? 또한 일부 `D`에서 보이는 숫자형 `9999` 같은 값이 정상 거리인지 sentinel인지, 행렬 필드가 decimal string과 JSON number의 혼합형을 공식 지원하는지도 확인이 필요합니다.
