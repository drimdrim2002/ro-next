# 세션 07 — 종료 조건과 재현성

## 확정 정책

이 문서는 `master-design.md`의 D10과 27장을 개정할 때 적용할 종료·재현성 계약을 정의한다. ALNS의 정상 종료 기준은 **최대 step 수**이며, 실행시간은 품질을 결정하는 동등한 최적화 예산이 아니라 무한 루프나 비정상적인 장기 실행을 차단하는 **안전 watchdog**이다.

- 외부 API 명칭은 `iterations`와 `timeBudgetMillis`보다 의미가 분명한 `maxSteps`와 `watchdogTimeoutMillis`를 사용한다. 기존 `iterationsPerRun`은 전환 시 `maxStepsPerRun`으로 명칭을 통일한다.
- `maxSteps`는 필수 양의 정수다. 하나의 step은 destroy, repair, 후보 평가, 수용 여부 결정, 연산자 통계 갱신까지 완료하여 solver 상태가 일관된 시점을 뜻한다.
- `watchdogTimeoutMillis`는 운영 환경에서 필수로 설정하되, 정상적인 입력과 설정에서는 `maxSteps`가 먼저 끝나도록 정한다. watchdog이 반복적으로 먼저 동작하면 정상 종료가 아니라 용량 산정, 성능 회귀 또는 설정 오류로 취급한다.
- 시간 측정은 시스템 시각 변경의 영향을 받지 않는 monotonic clock으로 수행한다. wall clock은 제출·시작·종료 시각 같은 관측 메타데이터에만 사용한다.
- 종료 결과에는 최소한 `terminationReason`, `requestedSteps`, `completedSteps`, `seed`, `elapsedMillis`, 입력·설정 fingerprint, 알고리즘/빌드 버전을 남긴다.
- 종료 사유의 최소 집합은 `MAX_STEPS_REACHED`, `WATCHDOG_REACHED`, `PLATFORM_TIMEOUT`, `FAILED`다. `PLATFORM_TIMEOUT`과 `FAILED`는 solver가 정상 결과 객체를 반환하지 못한 실행 상태이며 후보의 종료 사유로 가장해서는 안 된다.

현재 `master-design.md`의 “최대 반복 수와 최대 실행시간 중 먼저 충족되는 시점에 종료”라는 표현은 구현상 조건식만 설명할 뿐 두 조건의 역할 차이를 드러내지 못한다. 개정 시 “step 제한은 정상 종료 조건, watchdog은 안전 차단 조건”으로 바꾼다.

## 정상 종료

정상 실행은 초기해 생성 후 ALNS step을 정확히 `maxSteps`회 완료하고 `MAX_STEPS_REACHED`로 끝난다.

```text
초기화 및 초기해 생성
→ completedSteps = 0
→ 다음 step을 원자적으로 수행
→ step을 완전히 commit한 뒤 completedSteps 증가
→ completedSteps == maxSteps이면 best 해 반환
```

다음 규칙을 적용한다.

- step 도중의 임시 후보는 `current`나 `best`로 간주하지 않는다. step의 모든 상태 변경이 commit되었을 때만 완료 step으로 센다.
- `best`는 마지막으로 commit된 실행 가능한 해다. 종료 처리와 결과 직렬화는 탐색 step 수에 포함하지 않는다.
- 초기해 생성, 전처리, 결과 검증도 watchdog의 보호 범위에는 포함되지만 ALNS `completedSteps`에는 포함하지 않는다. 단계별 소요시간은 별도 메트릭으로 남긴다.
- 정상 종료 결과를 반환하기 전에 독립적인 최종 실행 가능성 검증을 한 번 수행한다. 검증 실패는 정상 완료가 아니라 `FAILED`다.
- step 수는 머신 속도나 병렬 batch 완료 순서와 무관하므로 품질 비교, 회귀 테스트와 재현성 검증의 기준으로 사용한다.

## watchdog 시간 제한

watchdog은 품질 목표가 아니라 안전장치다. deadline에 도달하면 가능한 한 마지막으로 commit된 `best`를 보존하고 일관된 경계에서 종료한다.

```text
deadline = monotonicNow + watchdogTimeout

각 주요 단계 및 다음 step 시작 전:
    deadline 초과 여부 확인
    초과 시 진행 중 변경을 rollback
    마지막 committed best를 검증
    WATCHDOG_REACHED로 반환
```

watchdog 검사는 step 사이에만 두지 않는다. 초기해 생성, destroy/repair 후보 열거, 삽입 위치 평가, local search처럼 오래 걸릴 수 있는 내부 반복문도 동일한 cancellation/deadline 신호를 주기적으로 확인해야 한다. 신호를 확인한 연산자는 부분 변경을 commit하지 않고 상위 호출자에게 중단을 전달한다.

협력적 watchdog만으로는 신호 확인 자체에 도달하지 못하는 실제 무한 루프를 중단할 수 없다. 따라서 다음 두 계층을 함께 사용한다.

1. solver watchdog은 rollback, 최종 검증, 후보 저장을 수행할 시간을 남겨 정상적인 부분 결과를 반환한다.
2. GCP 요청 timeout은 solver가 응답하지 못할 때 프로세스 요청을 끊는 최후의 강제 차단이다.

`WATCHDOG_REACHED` 결과는 정상 품질 목표를 모두 수행한 결과와 구별한다. 다만 마지막 `best`가 독립 검증을 통과했다면 사용 가능한 부분 후보로 저장할 수 있다. watchdog이 초기해 완성 전에 동작하거나 유효한 `best`가 없다면 후보를 만들지 않고 해당 batch를 실패 처리한다.

## 재현성 보장과 예외

강한 재현성의 보장 범위는 다음 조건을 모두 고정하고 `MAX_STEPS_REACHED`로 정상 종료한 단일 batch다.

```text
동일한 정규화 입력과 입력 fingerprint
+ 동일한 전체 solver 설정과 정책 profile/version
+ 동일한 maxSteps
+ 동일한 seed 및 seed 파생 규칙 version
+ 동일한 알고리즘/연산자 집합과 구현 build
= 동일한 해, 목적함수, 완료 step 수
```

이를 위해 다음 구현 계약이 필요하다.

- 전역 난수와 암묵적인 난수 생성을 금지하고 모든 난수 stream을 저장된 seed에서 결정적으로 파생한다.
- API가 seed를 자동 생성한 경우에도 접수 시 한 번만 생성하여 요청 레코드에 저장하고 응답/결과에 노출한다. 재시도할 때 새 seed를 만들지 않는다.
- 병렬 run의 seed는 `baseSeed`와 `runNumber`로부터 버전이 고정된 함수로 파생한다. 파생된 실제 seed도 후보에 저장한다.
- 정렬의 동점 규칙, map/set 순회 순서, 부동소수점 사용 여부, 병렬 reduction 순서를 결정적으로 정의한다.
- 병렬 run의 완료 순서는 최종 결과에 영향을 주지 않아야 한다. 최종 후보 선택은 고객별 목적 비교 결과가 같을 때 `runNumber`, 실제 seed 또는 canonical solution fingerprint 같은 고정된 tie-breaker를 사용한다.
- 입력 데이터뿐 아니라 목적함수, 제약, route propagator, travel matrix의 식별자와 버전도 설정 fingerprint에 포함한다.

다음 경우에는 동일 seed만으로 동일한 최종 해를 보장하지 않는다.

- `WATCHDOG_REACHED`: 시스템 부하나 GC에 따라 deadline 전에 완료한 step 수가 달라질 수 있다.
- `PLATFORM_TIMEOUT` 또는 비정상 종료 후 재시도: 재시도 자체는 동일 입력·seed·step 설정을 사용하지만 다시 watchdog에 걸리면 완료 step 수가 달라질 수 있다.
- 알고리즘, 정책, 정규화 규칙, travel matrix 또는 실행 의미가 바뀐 다른 build/version 사이의 비교.
- 장래에 step 내부에서 비결정적인 병렬 탐색이나 경쟁 기반 first-winner 선택을 도입하는 경우. 이를 도입하려면 결정적 병합 규칙을 별도로 설계해야 한다.

따라서 benchmark와 회귀 테스트는 watchdog에 도달하지 않을 만큼 충분한 제한을 사용하고, 비교 대상은 `MAX_STEPS_REACHED` 결과로 한정한다. 실행시간은 품질 비교의 입력이 아니라 관측 메트릭으로만 기록한다.

## GCP 관계

현재 저장소의 GCP 실행 경로는 다음과 같다.

```text
Cloud Run API
→ Workflows 병렬 branch
→ Cloud Run worker `/internal/batches`
→ Cloud Storage 후보 저장
→ finalize
```

현재 설정은 다음과 같다.

- API 요청 timeout: 30초. 비동기 Workflows 실행을 접수하는 구간이므로 solver 종료 조건과 직접 관련이 없다.
- Workflows의 batch HTTP timeout: 900초.
- Cloud Run worker 요청 timeout: 900초.
- finalize HTTP timeout: 300초.

batch HTTP timeout과 worker 요청 timeout이 모두 900초인 현재 구성에는 solver가 watchdog 종료 후 rollback, 검증, 후보 저장과 응답을 마칠 여유가 없다. 목표 구성은 아래 순서를 만족해야 한다.

```text
solver watchdog deadline
< Workflows batch HTTP timeout
< Cloud Run worker request timeout
```

예를 들어 worker 제한을 900초로 유지한다면 `840초 / 870초 / 900초`처럼 각 계층에 정리 시간을 둔다. 이 숫자는 확정값이 아니라 운영 측정 전의 예시다. 입력 로딩, 최종 검증, Cloud Storage 저장 시간과 네트워크 편차를 측정하여 간격을 확정한다.

Workflows의 `http.default_retry`는 timeout이나 일시 오류에서 동일 batch를 다시 호출할 수 있다. 따라서 batch 실행은 `(requestId, runNumber)` 기준으로 멱등해야 한다.

- 재시도는 접수 시 저장된 동일 입력 fingerprint, 설정, seed와 `maxSteps`를 사용한다.
- 후보 객체에는 완전한 결과만 공개하고, 부분 쓰기와 완료 marker를 구분한다.
- 동일 run의 중복 성공 결과가 존재해도 최종 선택이 호출/완료 순서에 의존하지 않아야 한다.
- hard platform timeout으로 후보가 생성되지 않은 batch는 별도 실행 상태에 `PLATFORM_TIMEOUT`을 기록한다. 결과 파일 부재를 계속 `RUNNING`으로만 해석해서는 안 된다.
- 일부 병렬 batch가 실패한 경우의 전체 요청 성공 정책은 별도로 확정하되, 성공 후보가 하나라도 있을 때 사용할 수 있는 결과와 batch 실패 내역을 함께 보존하는 방식을 권장한다.

15분 안에 정상적으로 `maxSteps`를 완료하지 못하는 입력이 일반화되면 watchdog을 품질 예산처럼 늘리는 방식보다 Cloud Run Job 또는 GKE Job 전환을 검토한다. 실행 플랫폼이 바뀌어도 step 기반 정상 종료와 재현성 계약은 유지한다.

## 테스트

### 정상 종료 및 step 의미

- 작은 고정 fixture에서 정확히 `maxSteps`회 commit 후 `MAX_STEPS_REACHED`인지 검증한다.
- destroy/repair 도중 예외나 중단을 주입하여 미완료 step이 `completedSteps`에 포함되지 않고 `current`와 `best`가 손상되지 않는지 검증한다.
- 초기해, 탐색, 최종 검증 소요시간이 분리 기록되는지 검증한다.

### watchdog

- 실제 대기 대신 fake monotonic clock을 사용해 deadline 직전·동일·직후 경계를 결정적으로 검증한다.
- 초기해 생성, destroy, repair, local search 내부 각각에 중단을 주입해 rollback 후 마지막 committed best가 반환되는지 검증한다.
- 유효한 best가 없는 시점의 timeout은 후보 성공으로 기록되지 않는지 검증한다.
- watchdog 종료 결과가 `WATCHDOG_REACHED`, 요청/완료 step 수, elapsed time을 포함하는지 검증한다.

### 재현성

- 동일 입력 fingerprint, 전체 설정, build, seed와 `maxSteps`로 여러 번 실행해 해의 canonical 표현, 목적 벡터, `completedSteps`가 같은지 검증한다.
- 병렬 run 결과를 서로 다른 완료 순서로 finalize해도 동일 후보가 선택되는지 검증한다.
- 목적값이 같은 후보를 만들어 고정 tie-breaker가 적용되는지 검증한다.
- seed를 생략한 요청은 생성된 `baseSeed`가 저장되고 재시도에서도 동일하게 사용되는지 검증한다.
- watchdog이 발생한 실행은 강한 재현성 suite에서 제외되고 별도 안전성 suite로 분류되는지 검증한다.

### GCP 통합

- solver watchdog이 먼저 발생할 때 Cloud Run 응답과 후보 저장이 Workflows timeout 이전에 끝나는지 검증한다.
- HTTP timeout 후 retry를 모의하여 동일 `(requestId, runNumber)` 후보가 중복되거나 최종 결과가 순서에 따라 달라지지 않는지 검증한다.
- 강제 종료된 batch가 영구 `RUNNING`으로 남지 않고 최종 상태와 진단 정보로 전환되는지 검증한다.
- 여러 batch 중 일부만 실패했을 때 확정된 집계 정책대로 finalize되는지 검증한다.

## 남은 질문

1. 운영 입력의 예상 크기와 목표 step 수를 기준으로 solver watchdog, Workflows HTTP timeout, Cloud Run worker timeout을 각각 몇 초로 확정할 것인가? 현재 900초 worker를 유지한다면 `840/870/900초`를 시작값으로 사용할 수 있는가?
2. 유효한 best가 있는 `WATCHDOG_REACHED` batch를 최종 후보 경쟁에 포함할 것인가? 권장안은 포함하되 최종 요청 상태를 `COMPLETED_WITH_WARNING`으로 구분하고 종료 사유를 노출하는 것이다.
3. 병렬 batch 일부가 `PLATFORM_TIMEOUT` 또는 `FAILED`여도 하나 이상의 유효 후보가 있으면 전체 요청을 완료할 것인가? 권장안은 완료하되 실패 batch 목록과 성공 run 수를 결과에 포함하는 것이다.
4. 운영상 허용할 최소 성공 run 수 또는 비율이 필요한가? 없다면 성공 후보 1개를 최소 조건으로 볼 수 있다.
5. 재현성 보장을 동일 build/JVM 주버전 안으로 한정할 것인가, 서로 다른 build에서도 유지해야 하는가? 권장안은 동일 build의 bit-for-bit/canonical 결과를 보장하고 build 간에는 회귀 기준만 보장하는 것이다.
6. API의 seed 생략을 계속 허용할 것인가? 허용한다면 생성 seed를 접수 결과와 영속 메타데이터에 반드시 노출한다. 완전한 실행 재현이 중요한 호출에는 seed를 필수로 요구하는 별도 모드를 둘 수 있다.
