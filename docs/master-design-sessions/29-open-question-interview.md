# 세션 29 — Master Design 열린 질문 인터뷰

```yaml
status: INTERVIEW_COMPLETE
version: 0.64-interview
last_updated: 2026-07-23
owner: RPDPTW 설계 인터뷰
scope: Master Design 질문 등록부의 OPEN 질문에 대한 사용자 결정 기록
supersedes: null
related_decisions: []
```

## 1. 기록 원칙

- 이 문서는 인터뷰 답변과 그 해석만 기록한다.
- 인터뷰 중에는 `master-design.md`와 `master-design-open-questions.md`를 수정하지 않는다.
- 사용자 답변을 넘어선 기본값이나 업무 의미를 만들지 않는다.
- 모든 질문이 끝난 뒤 별도 통합 세션이 승인 기록, Master, 질문 등록부와 영향 문서에 반영한다.
- `Q-INFRA-01`과 `Q-VAR-01`은 `DEFERRED` 상태를 유지하며 이 인터뷰에서 질문하지 않는다.

## 2. 결정 기록

### Q-NUM-01 — 차원별 유지 소수 자릿수

- **사용자 답변:** “무게, 부피는 전부 3입니다. 비용, 거리, 시간은 처음부터 정수로 입력됩니다.”
- **해석한 결정:**
  - 무게의 유지 소수 자릿수는 `n_weight = 3`이다.
  - 부피의 유지 소수 자릿수는 `n_volume = 3`이다.
  - 비용, 거리, 시간은 정수 입력 계약이므로 각각 `n_cost = 0`, `n_distance = 0`, `n_time = 0`이다.
  - 비용·거리·시간의 정수 계약은 소수 입력을 정수로 조용히 변환해도 된다는 뜻으로 확대 해석하지 않는다. 소수 입력의 오류 처리는 `Q-NUM-02`에서 확정한다.
- **근거:**
  - 사용자 직접 결정.
  - 기존 확정 사항 `C-10`에 따라 소수 물리량은 변환 경계에서 fixed-point integer로 정규화한다.
  - 기존 `n=3` 예시는 승인 근거가 아니었으나, 이번 답변으로 무게·부피에 한해 명시적으로 확정되었다.
- **영향 문서:**
  - `docs/master-design.md` §7.2 Fixed-point와 checked arithmetic
  - `docs/master-design-open-questions.md` `Q-NUM-01`
  - `docs/master-design-sessions/09-fixed-point-policy.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §8.1, §11.2
  - 향후 numeric normalization policy, provenance/fingerprint 계약과 `RM-1` 검증 기준
- **남은 모호성:**
  - 무게·부피의 셋째 자리 초과값 처리 방식은 `Q-NUM-02`에서 결정한다.
  - 비용·거리·시간에 소수 입력이 도착했을 때의 오류 정책도 `Q-NUM-02`에서 명시적으로 닫는다.
- **상태:** `RESOLVED`

### Q-NUM-02 — 차원별 초과 자릿수 처리

- **사용자 답변:** “무게, 부피는 내림이고, 비용, 거리, 시간은 소수 입력 거부”
- **해석한 결정:**
  - 무게와 부피는 정확한 원문 10진수를 읽은 뒤 소수 셋째 자리까지 유지하고, 그보다 작은 자릿수는 `FLOOR`로 처리한다.
  - 무게와 부피는 음수 입력이 허용되지 않으므로 허용 영역에서 `FLOOR`는 0 방향 절삭과 같은 결과를 낸다. 이 설명을 음수 허용 결정으로 확대하지 않는다.
  - 비용, 거리와 시간은 정수 입력 계약이다. 소수 입력은 반올림하거나 절삭하지 않고 입력 계약 위반으로 거부한다.
  - Legacy matrix의 `D/U`가 canonical 거리·시간 입력에 어떻게 대응하는지는 이 결정으로 추정하지 않고 `Q-MTX-01`과 `Q-MTX-02`에서 별도로 확정한다.
- **근거:**
  - 사용자 직접 결정.
  - `Q-NUM-01`에서 무게·부피는 `n=3`, 비용·거리·시간은 `n=0`으로 결정되었다.
  - 기존 `domain-design.md`의 내림 예시는 단독 승인 근거가 아니었으나, 이번 사용자 답변으로 무게·부피에 한해 동일한 방향이 명시적으로 승인되었다.
- **영향 문서:**
  - `docs/master-design.md` §7.2 Fixed-point와 checked arithmetic
  - `docs/master-design-open-questions.md` `Q-NUM-02`
  - `docs/master-design-sessions/09-fixed-point-policy.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §8.1, §11.2
  - 향후 numeric normalization policy, adapter validation, provenance/fingerprint와 `RM-1` 경계값 검증
- **남은 모호성:**
  - 무게·부피의 내림을 item 값에 먼저 적용할지, `item × qty` line 합계에 적용할지는 `Q-NUM-03`에서 결정한다.
  - Legacy `D/U`의 의미·단위·정밀도와 정수 canonical 계약의 관계는 `Q-MTX-01`에서 결정한다.
- **상태:** `RESOLVED`

### Q-NUM-03 — item 정규화와 quantity 곱 순서

- **사용자 답변:** “item 먼저 내림”
- **해석한 결정:**
  - 각 item의 원문 무게와 부피에 먼저 `n=3`, `FLOOR` 정규화를 적용한다.
  - 정규화된 item 정수값에 양의 정수 `qty`를 곱한다.
  - Request, route와 solution 합계는 그 결과를 checked integer arithmetic으로 누적한다.
  - Line의 원문 decimal 합계를 먼저 계산한 뒤 한 번만 내리는 대안은 사용하지 않는다.
- **근거:**
  - 사용자 직접 결정.
  - `Q-NUM-01`의 무게·부피 `n=3`과 `Q-NUM-02`의 `FLOOR` 결정을 순서 계약으로 완성한다.
  - 현재 Win PoC fixture의 item 452개는 모두 `qty=1`이어서 두 순서의 차이를 입증하지 못하며, fixture 관찰을 결정 근거로 사용하지 않았다.
- **영향 문서:**
  - `docs/master-design.md` §7.2 Fixed-point와 checked arithmetic
  - `docs/master-design-open-questions.md` `Q-NUM-03`
  - `docs/master-design-sessions/09-fixed-point-policy.md` 변환 시점과 순서
  - `docs/master-design-sessions/20-domain-input-draft.md` §8.1~8.2
  - 향후 adapter normalization, request demand 계산, overflow 검사와 `RM-1` 경계값 검증
- **남은 모호성:** 없음. 이 결정은 item 값이 개당 값이고 `qty`가 양의 정수라는 현재 입력 계약에 적용한다.
- **상태:** `RESOLVED`

### Q-MTX-01 — Legacy `D/U`의 의미·단위·허용 정밀도

- **사용자 답변:** “네 말한대로 진행”
- **질문에서 승인한 권장안:** `D = 정수 meter`, `U = 정수 second`, 허용 소수 자릿수 `0`; 현재 소수 fixture는 정수 행렬을 다시 받기 전까지 비준수 입력으로 처리.
- **해석한 결정:**
  - Legacy `D`의 공식 업무 의미는 directed 이동거리이며 단위는 meter다.
  - Legacy `U`의 공식 업무 의미는 directed 이동시간이며 단위는 second다.
  - `D`와 `U`는 모두 정수 입력만 허용하고 소수 자릿수는 `0`이다.
  - 소수 `D/U`를 포함한 현재 `data/win_poc_case.json`은 이 계약에 맞지 않는다. 생산자에게서 정수 행렬을 다시 받거나 별도의 명시적 결정이 있기 전에는 canonical normalization, 공식 baseline 또는 official benchmark에 사용하지 않는다.
  - 현재 fixture의 소수값을 adapter가 절삭·반올림하거나 값의 크기만으로 정수화하지 않는다.
- **근거:**
  - 사용자가 제시된 권장안을 직접 승인했다.
  - `Q-NUM-01`에서 거리·시간의 유지 자릿수는 `0`, `Q-NUM-02`에서 소수 입력 거부가 결정되었다.
  - Legacy PDF는 matrix 자체를 정의하지 않지만 `maxDriveDist`의 단위를 meter, `maxDriveTime`의 단위를 second로 설명하며, 사용자 승인이 그 추론을 공식 계약으로 확정했다.
- **영향 문서:**
  - `docs/master-design.md` §7.2, §8, §14.2~14.3
  - `docs/master-design-open-questions.md` `Q-MTX-01`
  - `docs/master-design-sessions/09-fixed-point-policy.md`
  - `docs/master-design-sessions/12-distance-time-input.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §5, §8
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 matrix adapter/schema, verifier, manifest와 `RM-1`·`RM-6` gate
- **남은 모호성:**
  - `C=O/G`와 diagonal `D=9999, U=0`의 의미는 `Q-MTX-02`에서 결정한다.
  - Canonical production matrix의 coverage 계약은 `Q-MTX-03`에서 결정한다.
- **상태:** `RESOLVED`

### Q-MTX-02 — `C=O/G`와 diagonal `D=9999, U=0`

- **사용자 답변 1:** “C의 값은 OSRM이든 GreatCircle이든 중요하지 않습니다. 오직 D (Distance, 단위는 meter), U(Duration, 단위는 seconds)가 중요합니다. D가 없으면 위경도 기준으로 GreateCircle을 계산 (이것은 별도 함수로 구현 예정). U가 없으면 차량 정보에 있는 속도를 이용해서 구하거나 속도가 없으면 45km/h를 적용해서 구현”
- **대각선 후속 답변:** “내 전부 0,0으로 정규화해주세요.”
- **해석한 결정:**
  - Legacy `C`는 solver의 거리·시간, feasibility, score 또는 fallback 분기에 영향을 주지 않는 비권위 필드다.
  - `C=O/G`를 OSRM/GreatCircle 등 특정 의미로 해석할 필요가 없으며, canonical core 계약에서 제거할 수 있다.
  - `D`만 authoritative distance이며 단위는 meter, `U`만 authoritative duration이며 단위는 second다.
  - 모든 self arc는 내부에서 `D=0 meter`, `U=0 second`로 정규화한다.
  - Legacy diagonal `D=9999, U=0`은 실제 이동거리로 사용하지 않고 `0/0`으로 정규화한다.
  - 신규 canonical 입력도 diagonal의 정규화 결과가 반드시 `0/0`이어야 한다.
  - `D`가 없을 때 위·경도로 Great Circle 거리를 계산하는 별도 기능을 제공한다.
  - `U`가 없을 때 차량 속도로 계산하며, 차량 속도도 없으면 `45 km/h`를 사용한다.
  - 위 누락값 생성 결정은 기존 `C-13`의 “좌표·속도 fallback 및 누락 arc 보충 금지”를 변경하는 최신 사용자 override다. 정확한 누락 범위, 생성 단계, 차량별 `U` 표현과 matrix completeness 영향은 `Q-MTX-03`에서 확정해야 한다.
- **근거:**
  - 사용자 직접 결정.
  - `Q-MTX-01`에서 `D=meter`, `U=second`가 이미 확정되었다.
- **영향 문서:**
  - `docs/master-design.md` `C-13`, §7.2, §8, §14
  - `docs/master-design-open-questions.md` `Q-MTX-02`, `Q-MTX-03`
  - `docs/master-design-sessions/09-fixed-point-policy.md`
  - `docs/master-design-sessions/12-distance-time-input.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §5
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 matrix preparation/adapter, provenance, verifier와 `RM-1`·`RM-6`
- **남은 모호성:**
  - `D/U가 없다`의 정확한 범위와 생성 결과의 matrix 계약은 `Q-MTX-03`에서 확인한다.
  - Great Circle 거리와 속도 기반 시간의 정수화 방식은 `Q-MTX-03`의 생성 계약에서 확인한다.
- **상태:** `RESOLVED`

### Q-MTX-03 — Canonical production matrix coverage

- **사용자 답변 1:** “네 맞습니다. 따라서 두 번째 방식이 맞습니다.”
- **질문에서 승인한 두 번째 방식:** 외부 입력은 sparse 또는 행렬 전체 생략을 허용하고, 별도 준비 단계에서 누락값을 생성하여 solver와 verifier에는 모든 필수 방향쌍이 해소된 complete matrix만 전달한다.
- **생성값 정수화 후속 답변:** “네 확정”
- **후속 질문에서 승인한 권장안:** Great Circle `D`는 `HALF_UP`으로 가장 가까운 정수 meter로 만들고, 누락 `U`는 차량별 `CEILING(D × 3.6 ÷ speed)` 정수 second로 계산한다.
- **해석한 결정:**
  - 외부 production input은 physical location의 모든 directed pair를 직접 제공할 의무가 없다. 일부 arc 또는 matrix 전체를 생략할 수 있다.
  - 명시적인 Travel Matrix preparation 단계가 모든 physical location의 directed `M²` pair를 열거하고 solver 시작 전에 완성한다.
  - 제공된 `D/U`는 우선 사용하고, 누락 `D`는 좌표 기반 Great Circle로, 누락 `U`는 차량 속도 또는 속도 누락 시 `45 km/h`로 생성한다.
  - Great Circle의 소수 meter 결과는 `HALF_UP`으로 가장 가까운 정수 meter로 변환한다.
  - 제공된 `U`는 차량과 무관한 authoritative 정수 second 값으로 사용한다.
  - `U`가 없는 directed pair는 차량별로 `CEILING(D_meter × 3.6 ÷ speed_km_h)`를 적용하여 정수 second를 생성한다.
  - 차량 속도가 없으면 `45 km/h`를 사용한다.
  - 누락 `U`의 생성 결과는 차량에 따라 다르므로 preparation 결과는 제공된 공통 `U`와 차량별 생성 travel time을 구분할 수 있어야 한다. Solver 시작 전 모든 사용 차량과 필수 directed pair의 시간이 해소되어야 한다.
  - `D` 생성에 필요한 좌표가 없어 필수 pair를 해소할 수 없으면 solve 전 입력 오류다.
  - Solver core와 verifier는 준비가 완료된 travel data만 사용하며 탐색 중 lazy fallback이나 좌표·속도 재계산을 하지 않는다.
  - Raw input, 제공값과 생성값의 source, 계산 정책과 결과 fingerprint를 provenance에 남긴다.
- **근거:**
  - 사용자 직접 결정.
  - `Q-MTX-02`에서 승인된 누락 `D/U` 생성 정책.
  - 현재 Win PoC fixture가 complete `M²`라는 사실은 관찰 evidence일 뿐 production 외부 입력 의무로 승격하지 않는다.
- **영향 문서:**
  - `docs/master-design.md` `C-13`, §7.2, §8, §14, `RM-1`
  - `docs/master-design-open-questions.md` `Q-MTX-03`
  - `docs/master-design-sessions/12-distance-time-input.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §5
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 Travel Matrix preparation, normalized matrix/time representation, provenance, verifier와 benchmark manifest
- **남은 모호성:** 없음. 구체 타입/API는 후속 상세 설계가 이 의미를 보존하여 정한다.
- **상태:** `RESOLVED`

### Q-TIME-01 — 계획 시간대와 canonical date-time 형식

- **사용자 답변 1:** “전역 고정 시간대이고, 시간대를 무시합니다. 시간대는 solver에서 다루는 것이 아닙니다. solver를 호출하는 backend, frontend에서 다뤄야 하는 것입니다.”
- **Solver 경계 후속 답변:** “아니요. backend 에서는 timezone 없는 문자열을 solver에 전달. solver는 timezone 없는 문자열을 받고 core 에서는 제안하대로 long 형태로 변경하여 진행 가능”
- **문자열 문법 후속 답변:** “yyyy-MM-dd HH:mm:ss입니다”
- **해석한 결정:**
  - 시스템의 시간대는 전역적으로 고정되어 있으나, 정확한 timezone 이름과 변환 규칙은 solver 계약의 책임이 아니다.
  - Frontend/backend가 외부 날짜·시각의 timezone, UTC offset과 필요한 변환을 처리한다.
  - Backend는 exact `yyyy-MM-dd HH:mm:ss` 형식의 timezone/offset 없는 date-time 문자열을 solver 입력 경계에 전달한다.
  - Fractional second, timezone suffix와 UTC offset은 이 solver 입력 형식에 포함하지 않는다.
  - Solver의 adapter/normalization 계층은 timezone 없는 문자열을 파싱하고, planning origin 기준 `long` second로 변환한다.
  - Solver core는 IANA `ZoneId`, UTC offset, DST 또는 date-time 문자열을 다루지 않고 정규화된 `long` 시간축만 사용한다.
  - Solver에 전달되는 planning period, node/terminal window와 vehicle work window 문자열은 이미 하나의 전역 고정 시간 기준으로 정렬되어 있어야 한다.
  - 전역 고정 timezone의 정확한 값을 현재 답변에서 추정하거나 solver fingerprint의 숨은 기본값으로 만들지 않는다.
- **근거:**
  - 사용자 직접 결정.
  - Legacy 자료에는 timezone/offset이 없으므로 기존 문자열만으로 특정 timezone을 추론하지 않는다.
- **영향 문서:**
  - `docs/master-design.md` §4 logical ports, §7.3 Planning period와 time, §13 reproducibility
  - `docs/master-design-open-questions.md` `Q-TIME-01`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §4.3
  - 향후 backend/frontend-to-solver input contract와 time normalization provenance
- **남은 모호성:** 없음. Plan end와 time-window close의 포함 여부는 별도 `Q-TIME-02`가 결정한다.
- **상태:** `RESOLVED`

### Q-TIME-02 — Plan end와 time-window close 경계

- **사용자 답변:** “1 plan end는 제외 time window close 포함”
- **해석한 결정:**
  - Planning period는 `start <= t < end`인 반개구간이다.
  - Plan end와 정확히 같은 시각의 event는 현재 plan 범위에 포함하지 않는다.
  - Time-window close는 포함 경계다. `Q-TIME-03`에서 정하는 관련 event가 `close`와 정확히 같으면 허용한다.
  - 정수 second 축에서 close를 묵시적으로 1초 줄이거나 plan end를 포함 경계로 바꾸지 않는다.
- **근거:**
  - 사용자 직접 결정.
  - Plan end 제외는 연속 planning period의 경계 중복을 방지하고, time-window close 포함은 정확한 업무 마감시각을 허용한다.
- **영향 문서:**
  - `docs/master-design.md` §7.3 Planning period와 time
  - `docs/master-design-open-questions.md` `Q-TIME-02`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §4.3
  - 향후 time normalization, propagator, verifier와 `RM-1` boundary tests
- **남은 모호성:** Time-window close와 비교할 event가 서비스 시작인지 완료인지는 `Q-TIME-03`에서 결정한다.
- **상태:** `RESOLVED`

### Q-TIME-03 — 고객 영업시간과 서비스 경계

- **사용자 답변 1:** “고객 시간 창이 의미하는 것은 가게의 open/close time입니다. 방금 예시는 9시에 열고 10시에 닫는다는 것입니다. 따라서 고객은 가장 빨리 9시에 도착이 가능하고 서비스를 시작해 9시 20분에 출발이 가능. 9시 도착, 9시 20분 서비스 종료, 9시 20분 출발”
- **영업시간 경계 후속 답변:**
  - “8시 50분에 도착하면 10분 대기 후 9시에 서비스를 시작합니다. 이 때 대기시간은 기록해야 합니다. 나중에 목적식에 대기 시간을 최소화하는 기준이 추가될 수 있음”
  - “가능. 현실적으로는 close 이후에서 departure 가능합니다. 이 부분은 policy로 적용해서 default가 가능이고, 경우에 따라 불가하게 처리했으면 합니다.”
- **해석한 결정:**
  - 고객 `openTime/closeTime`은 매장의 실제 영업 시작·종료 시각이다.
  - 고객 위치에 `openTime`보다 일찍 도착할 수 있다.
  - `serviceStart = max(arrival, openTime)`이며, `waitingTime = serviceStart - arrival`을 기록한다.
  - Waiting time은 가격이나 선호를 포함하지 않는 중립 metric이다. 향후 profile/objective가 이를 최소화할 수 있지만 현재 목적식에 자동 포함하지 않는다.
  - `openTime`과 정확히 같은 시각에 도착하거나 서비스를 시작할 수 있다.
  - 서비스 종료와 출발은 `serviceStart + serviceDuration`으로 계산한다.
  - 예시 `open=09:00`, `close=10:00`, `serviceDuration=20분`에서 `09:00 도착 → 09:00 서비스 시작 → 09:20 서비스 종료·출발`은 허용된다.
  - 기본 고객 창 정책은 `START_ONLY`다. `serviceStart <= closeTime`이면 서비스 종료와 출발이 close 이후여도 허용한다.
  - Profile은 `COMPLETE_WITHIN_WINDOW`를 선택하여 `serviceEnd/departure <= closeTime`을 요구할 수 있다.
  - `Q-TIME-02`의 close 포함 결정에 따라 기본 정책에서는 close와 정확히 같은 시각의 서비스 시작도 허용한다.
- **근거:**
  - 사용자 직접 설명과 hand-calculated 예시.
- **영향 문서:**
  - `docs/master-design.md` §7.3 Planning period와 time
  - `docs/master-design-open-questions.md` `Q-TIME-03`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §4.3
  - 향후 time-window normalization, waiting/service propagation, verifier와 `RM-1`
- **남은 모호성:** 없음. 반복 일간 창과 overnight 전개는 `Q-TIME-04`에서 결정한다.
- **상태:** `RESOLVED`

### Q-TIME-04 — 반복 일간 창, overnight 창과 차량 근무 종료 초과 이동

- **사용자 답변 1:** “네 맞습니다. 제안한대로 진행”
- **질문에서 승인한 권장안:** 날짜 없는 창은 plan 범위에서 매일 반복하고, `openTime > closeTime`은 익일 close까지 이어지는 overnight 창으로 해석한다.
- **차량 근무 종료 후속 답변:**
  - “만약 plan start time이 7월 1일 0시이고, plan end time이 7월 2일 0시입니다. (실제 운행 가능한 시간은 7월 1일 23시 59분 59초) 이런 경우 FORBID_CROSSING 입니다.”
  - “만약 plan start time이 7월 1일 0시이고, plan end time이 7월 3일 0시입니다. (실제 운행 가능한 시간은 7월 2일 23시 59분 59초, 즉 multi day로 이동) 이런 경우 PAUSE_AND_RESUME 입니다. 기사들은 실제로는 2,3일에 걸친 운송을 할 수 있으며 이런 계획이 있는 경우, 해당 지역에서 휴식 후 이동하게 됩니다.”
- **이동 시작 연기 후속 질의:** “이런 경우 첫날 10분 운전이 아니라 다음 날 9시부터 운전할 수 있게 할 수 있을까요?”
- **최종 확인 답변:** “네”
- **해석한 결정:**
  - 고객·차고지의 날짜 없는 `openTime/closeTime`과 차량의 날짜 없는 근무시간은 planning period에 포함되는 각 날짜에 반복한다.
  - 고객이 한 날짜의 close 이후 도착하면 다음 반복 창의 open까지 기다릴 수 있으며, 그 대기시간은 `Q-TIME-03`에서 정한 waiting metric에 포함한다.
  - `openTime > closeTime`이면 `D일 openTime`부터 `D+1일 closeTime`까지 이어지는 하나의 overnight 창이다.
  - Overnight 창을 당일의 빈 창이나 두 개의 독립된 창으로 해석하지 않는다.
  - 반복·overnight 전개 결과는 `Q-TIME-02`의 `[planStart, planEnd)` 경계로 clip한다.
  - 이동은 `departureTime + fullTravelTime <= currentWorkEnd`일 때만 현재 근무창에서 시작할 수 있다.
  - 전체 이동이 현재 근무창 안에 끝나지 않지만 plan end 전에 다음 반복 근무창이 있으면 현재 위치에서 휴식하고, 다음 `workStartTime`에 전체 이동을 처음부터 시작한다.
  - 이동 arc 중간에서 멈추고 남은 이동을 다음 날 이어가는 `PAUSE_AND_RESUME`은 사용하지 않는다.
  - 다음 반복 근무창이 plan 범위 안에 없거나, 전체 이동시간이 어떤 이용 가능한 단일 근무창에도 들어가지 않으면 해당 이동은 infeasible이다.
  - 근무창 사이의 휴식은 순수 drive time에서 제외하고 route elapsed/rest breakdown에 기록한다.
  - 이 출발 연기 규칙은 단일일 plan을 plan end 밖으로 연장하는 수단으로 사용하지 않는다.
- **근거:**
  - 사용자 직접 승인.
  - Legacy 입력의 영업·근무시간이 날짜 없는 `HH:mm:ss`이고 planning period는 여러 날짜를 포함할 수 있다.
- **영향 문서:**
  - `docs/master-design.md` §7.3 Planning period와 time
  - `docs/master-design-open-questions.md` `Q-TIME-04`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §4.3
  - 향후 window expansion, propagator, waiting metrics, verifier와 `RM-1`
- **남은 모호성:** 없음.
- **상태:** `RESOLVED`

### Q-IN-01 — Legacy 주문 날짜와 작업시간 필드

- **사용자 답변:**
  - “reqDate와 dueDate는 같은 의미의 레거시 별칭입니다.”
  - “duration과 taskTIme은 좀 다릅니다. duration은 차량이 해당 지점에 도착한 후 차량이 진입하는 시간 등을 나타냅니다. taskTime은 item을 선적하는데 걸리는 시간입니다. 따라서 duration은 order나 reuest에 정의를 하고, taskTime은 item에 정의함”
  - 날짜 의미 및 잘못된 계층의 `taskTime` 처리 후속 답변:
    - “네 맞습니다. 하지만 로직은 다시 한 번 확인”
    - “serviceStart = min(arrivalt time, opeTime)”
    - “serviceEnd = serviceStart + serviceTime”
    - “serviceStart <= reqDate(dueDate) <= serciceEnd”
    - Legacy order-level `taskTime` 거부 권장안에는 “네”
  - 시간 부등식 재확인 답변: “serviceStart <= serciceEnd <= reqDate(dueDate) 이거입니다. 다시 확인”
  - 서비스시간 합산 후속 답변: “곱해야 합니다. 150초가 아닌 420초가 되어야 합니다.”
- **해석한 결정:**
  - Legacy `reqDate`와 `dueDate`는 서로 다른 release/deadline 필드가 아니라 같은 업무 의미를 가진 별칭이다.
  - `duration`과 `taskTime`은 별칭이 아니며 서로 다른 활동의 시간이다.
  - `duration`은 차량이 해당 지점에 도착한 뒤 진입하는 시간 등을 나타내며 order 또는 request 수준에 정의한다.
  - `taskTime`은 item을 선적하는 데 걸리는 시간이며 item 수준에 정의한다.
  - Canonical 모델에서 order/request 수준 `taskTime`을 `duration`의 별칭으로 자동 변환하거나 두 값을 같은 필드로 덮어쓰지 않는다.
  - Legacy 입력에 order-level `taskTime`이 존재하면 item에 임의 배분하거나 `duration`으로 변환하지 않고 입력 오류로 거부한다.
  - `reqDate`/`dueDate`는 서비스 완료의 포함 기한이다. 시간 feasibility는 `serviceStart <= serviceEnd <= reqDate(dueDate)`를 만족해야 한다.
  - `serviceStart`는 `Q-TIME-03`에서 확정한 `max(arrivalTime, openTime)`으로 계산한다. 후속 답변에서 처음 제시된 `min`은 최종 수식으로 채택하지 않는다.
  - `reqDate`/`dueDate`가 plan end와 같으면 `Q-TIME-02`의 plan end 제외 경계가 여전히 적용되므로 plan 안의 event를 plan end까지 연장하지 않는다.
  - Item `taskTime`은 item 한 단위의 선적시간이므로 해당 item의 `qty`를 곱한다.
  - 주문/request의 전체 서비스시간은 `serviceTime = duration + Σ(item.taskTime × item.qty)`다.
  - `duration=120초`, `item.taskTime=30초`, `qty=10`이면 `serviceTime=420초`다.
  - `serviceEnd = serviceStart + serviceTime`이며 계산은 정수 second와 checked arithmetic을 사용한다.
  - Legacy PDF의 “item entry별 한 번 합산하고 qty를 곱하지 않는다”는 설명은 이번 사용자 직접 결정으로 대체한다.
- **근거:**
  - 사용자 직접 설명.
  - 현재 fixture의 모든 주문은 `reqDate = planEnd`이고 `dueDate`는 없으므로, fixture만으로 별칭 필드의 정확한 시간 제약을 판단할 수 없다.
  - Legacy 자료는 order-level `duration`과 일부 예시의 order-level `taskTime`을 혼용하므로 사용자 답변 없이 이를 별칭 또는 합산 대상으로 볼 수 없었다.
- **영향 문서:**
  - `docs/master-design.md` §5 canonical request, §7.3 입력·시간 정규화
  - `docs/master-design-open-questions.md` `Q-IN-01`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/20-domain-input-draft.md`
  - 향후 Legacy adapter, canonical request/item 모델, service-time propagation, verifier와 `RM-1`
- **남은 모호성:** 없음.
- **상태:** `RESOLVED`

### Q-IN-02 — Depot, rotation과 route resource 필드

- **사용자 답변 — depot 작업시간:**
  - “depot.taskTime은 적용하지 않습니다. depot에는 item이 없음”
  - “depot의 duration (serviceTime)은 회전 배차에 적용됩니다.”
  - “depot에서 출발하여 order1, order2, order3을 방문하고 다시 depot에 돌아옵니다. 이 때 depot의 duration이 적용됩니다.”
  - “처음에는 duration(serviceTime)이 적용 안됨”
  - “회전 배차는 추후 적용 예정”
- **사용자 답변 — 현재 `multiRotation` 처리:** non-zero 값을 `UNSUPPORTED_INPUT`으로 거부하는 두 번째 방식에 대해 “일단 두번째로 해주세요.”
- **사용자 답변 — `waitInDepot`:** 제안한 출발 지연 정책에 대해 “네 맞습니다”
- **사용자 답변 — `maxStopCnt` 계산 기준:** “최대 정차입니다. 중요한 것은 location 기준임. 예를 들어 order1, order2, order3의 location이 같으면 stop은 1임”
- **사용자 답변 — `maxStopCnt` 계산 기준 변경:** “다시 정하겠습니다. 이전 방문지와 위치를 확인하고 다르면 +1로 합니다.”
- **사용자 답변 — `maxStopCnt`의 depot 제외:** 제안한 고객 service location만 계산하는 기준에 대해 “네 맞습니다.”
- **사용자 답변 — `maxStopCnt` reset:** “차량 route 전체 누적”
- **사용자 답변 — `maxDriveTime/Dist` 합산 대상:** 실제 주행 arc만 합산하는 권장안에 대해 “네 맞습니다.”
- **사용자 답변 — `maxDriveTime/Dist` reset:** “route 전체 누적”
- **사용자 답변 — 한도 우선순위와 누락:** 제안한 전역·차량별 한도의 `min` 적용 및 누락 시 추가 제약 없음에 대해 “네 그렇게 진행”
- **해석한 결정:**
  - `depot.taskTime`은 solver의 시간 전파에 적용하지 않는다. Depot에는 item이 없으므로 `Q-IN-01`에서 확정한 item 단위 `taskTime × qty` 의미를 depot에 만들지 않는다.
  - Depot의 회차 간 작업시간은 `taskTime`이 아니라 `duration`, 즉 depot `serviceTime`으로 표현한다.
  - 최초 depot 출발에는 depot `duration`을 적용하지 않는다.
  - 차량이 한 회차의 주문 방문을 마치고 depot으로 돌아온 뒤 다음 회차로 다시 출발할 때 depot `duration`을 적용한다.
  - 마지막 depot 복귀 뒤 후속 회차가 없으면 재출발을 위한 depot `duration`을 적용하지 않는다.
  - 회전 배차 기능은 후속 범위이며 현재 표준 범위에서 구현된 것으로 간주하지 않는다.
  - Legacy 입력의 `depot.taskTime`은 현재 fixture처럼 존재하더라도 시간 전파에 적용하지 않는다.
  - `trips=oneway`이면 `multiRotation`은 route 의미에 영향을 주지 않으며 값과 관계없이 무시한다. 이때 route는 depot에서 한 번 출발하고 중간 depot 재방문 없이 마지막 고객에서 종료한다.
  - `trips=oneway`가 아닌 입력에서는 회전 배차가 지원되기 전까지 `multiRotation != 0`을 조용히 single-trip으로 바꾸지 않고 `UNSUPPORTED_INPUT`으로 거부한다.
  - 이 처리는 회전 배차가 별도 설계·승인을 거쳐 활성화되기 전까지의 정책이다.
  - 현재 Win fixture의 `trips=oneway, multiRotation=1`은 rotation 값 때문에 거부하지 않는다. Raw 값과 oneway 우선 해석을 provenance에 남긴다.
  - Legacy PDF는 `0=재방문 없음`, 양수 `n=최대 n회 재방문`, `-1=무제한`으로 설명하지만, 향후 회전 배차의 최종 값 계약은 이번 “두 번째” 선택만으로 승인된 것으로 확대 해석하지 않는다.
  - `waitInDepot=N`이면 차량은 `max(vehicleWorkStart, depotOpen)`인 가능한 가장 이른 시각에 depot을 출발한다. 첫 고객에 일찍 도착하면 `Q-TIME-03`에 따라 고객 위치에서 기다린다.
  - `waitInDepot=Y`이면 선택된 첫 고객 영업창의 open에 맞추어 `departure = max(earliestDeparture, firstCustomerOpen - travelTime)`으로 depot 출발을 늦춘다.
  - `waitInDepot=Y`는 고객 대기를 금지하거나 route feasibility를 바꾸는 hard constraint가 아니라, 첫 고객 앞의 동일한 조기 대기를 depot 대기로 옮기는 출발시각 정책이다.
  - Depot 대기와 고객 대기는 별도 metric으로 기록한다.
  - 현재 Win fixture의 `waitInDepot=N`은 가능한 가장 이른 depot 출발을 뜻한다.
  - `maxStopCnt`는 배정된 order/request 수가 아니라 물리 location 기준 정차 수의 상한이다.
  - Route 방문 순서에서 현재 방문지의 location이 직전 방문지의 location과 다를 때 stop count를 `+1`한다.
  - 같은 location의 order가 연속되면 첫 location 진입만 `+1`하고 뒤의 동일-location order는 추가하지 않는다.
  - Location A를 방문하고 다른 location B를 거쳐 다시 A를 방문하면 각 location 전환이 별도 정차이므로 A의 두 방문은 각각 계산한다.
  - 이 변경된 전환 기준은 앞서 기록한 “같은 location의 order는 stop 1”을 더 정확히 정의하며, route 전체의 고유 location 수만 세는 방식은 사용하지 않는다.
  - Stop count는 고객 service location에 진입할 때만 증가한다.
  - 시작·종료 depot, 향후 회전 배차의 중간 depot 방문과 공통 depot의 가상 pickup은 stop count에 포함하지 않는다.
  - `maxStopCnt`는 차량 route 전체에서 누적한다.
  - Planning date 변경, 반복 근무창 사이의 휴식 또는 다일 운행만으로 stop count를 reset하지 않는다.
  - 회전 배차 활성화 시 depot 회차 경계에서의 reset 여부는 해당 후속 기능 설계에서 별도로 결정한다.
  - `driveTime`은 실제로 주행한 모든 arc의 authoritative `U_seconds` 합이다.
  - `driveDist`는 실제로 주행한 모든 arc의 authoritative `D_meters` 합이다.
  - 출발 depot에서 첫 고객, 고객 사이, `roundtrip`의 마지막 고객에서 depot, 향후 회전 배차의 depot 출입처럼 실제 경로가 통과한 이동 arc를 포함한다.
  - 고객·depot 대기, order/depot `duration`, item `taskTime`과 반복 근무창 사이의 휴식은 drive time과 drive distance에 포함하지 않는다.
  - `driveTime <= maxDriveTime`, `driveDist <= maxDriveDist`인 포함 상한이며 한도와 정확히 같은 값은 허용한다.
  - `maxDriveTime`과 `maxDriveDist`는 차량 route 전체에서 누적한다.
  - Planning date 변경, 다음 반복 근무창 시작 또는 중간 휴식으로 두 누적값을 reset하지 않는다.
  - 일별 주행시간·거리 한도가 필요하면 기존 필드를 날짜별로 재해석하지 않고 별도 daily resource policy로 정의한다.
  - 차량별 `maxStopCnt`와 전역 `Optimizer.VehicleMaxStopCount`가 모두 있으면 둘 다 hard constraint이며 유효 한도는 두 값의 `min`이다.
  - 두 stop 한도 중 하나만 있으면 존재하는 값을 적용하고, 둘 다 없으면 stop 수에 추가 hard limit가 없다.
  - `maxDriveTime`과 `maxDriveDist`가 없으면 해당 주행 자원에 추가 hard limit가 없다.
  - 누락 한도를 임의의 큰 수 또는 `Long.MAX_VALUE` sentinel로 바꾸지 않고 명시적인 “제약 없음”으로 정규화한다.
- **근거:**
  - 사용자 직접 설명과 `depot → order1 → order2 → order3 → depot` 예시.
  - `Q-IN-01`에서 `taskTime`은 item 수준의 단위당 선적시간으로 확정되었다.
- **영향 문서:**
  - `docs/master-design.md` §5 canonical route/request, §7.3 입력·시간 정규화
  - `docs/master-design-open-questions.md` `Q-IN-02`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/20-domain-input-draft.md`
  - `docs/master-design-sessions/24-roadmap-draft.md`
  - 향후 Legacy adapter, depot service, multi-trip/rotation 모델, propagator와 `RM-1`
- **남은 모호성:**
  - 현재 표준 범위에는 없음.
  - 후속 회전 배차를 활성화하려면 `multiRotation`의 최종 값·reset 계약과 depot `duration`/window 경계를 별도로 승인해야 한다. 이는 현재 non-zero 입력 거부 계약을 모호하게 만들지 않는다.
- **상태:** `RESOLVED`

### Q-BENCH-03 — Win fixture의 `oneway + multiRotation=1`

- **사용자 답변:** “oneway이면 multiRoation이 무엇으로 입력되어있든 상관 없습니다.”
- **Oneway 형태 후속 답변:** depot에서 한 번 출발하고 중간·최종 depot 방문 없이 마지막 고객에서 끝나는 해석에 대해 “네”
- **해석한 결정:**
  - `trips=oneway`일 때 `multiRotation` 값은 route의 terminal, depot revisit 또는 회전 수에 영향을 주지 않는 비권위 입력이다.
  - Oneway route는 depot에서 한 번 출발하고 고객들을 방문한 뒤 마지막 고객에서 끝나는 single-trip이다.
  - Oneway route에는 중간 depot 재방문과 마지막 고객 이후 depot 복귀가 없다.
  - 따라서 현재 Win fixture의 `multiRotation=1`을 rotation 이유로 거부하거나 `0`으로 fixture를 수정하지 않는다.
  - Raw `multiRotation=1`은 fixture fingerprint와 provenance에 그대로 남기되, approved oneway interpretation에 따라 feasibility와 route 생성에는 영향을 주지 않는다.
  - 거리·시간 metric에는 시작 depot에서 첫 고객까지의 arc와 고객 사이 arc를 포함하고, 마지막 고객에서 depot으로 돌아가는 가상 arc는 포함하지 않는다.
  - 이 답변은 `Q-IN-02`의 일반적인 non-zero `multiRotation` 거부 정책에 대한 최신 조건부 override다.
  - `trips`가 `oneway`가 아닌 경우에는 회전 배차가 지원되기 전까지 non-zero `multiRotation`을 `UNSUPPORTED_INPUT`으로 거부한다.
  - Q-BENCH-03의 rotation 해석은 해결되었지만, 현재 fixture의 전체 official readiness는 numeric/matrix 등 다른 확정 계약도 모두 만족해야 한다.
- **근거:**
  - 사용자 직접 결정.
  - 현재 fixture는 `trips=oneway`, `multiRotation=1`이며 기존 문서만으로 이 조합의 우선순위를 확정할 수 없었다.
- **영향 문서:**
  - `docs/master-design.md` §7.3 입력·시간 정규화, §14 Win PoC benchmark
  - `docs/master-design-open-questions.md` `Q-BENCH-03`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/16-win-poc-benchmark.md`
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 Win adapter/manifest, terminal verifier, 거리·시간 metric과 `RM-1`·`RM-6`
- **남은 모호성:** 없음.
- **상태:** `RESOLVED`

### Q-COMP-01 — 차량 크기 필드와 코드 정책

- **사용자 답변:**
  - “vehicle에 있는 것은 차량 자체의 크기이므로 "T1.4"처럼 정확한 하나의 코드가 오고”
  - “order에 있는 vehicleFeature는 목록이므로 위와 같이 구체적인 목록이거나 ["T1", "T1.4", "T1.9"], 아니면 ["ALL"] 처럼 들어옵니다.”
  - “order는 vehicleFeature가 아니라 vehicleFeatureList로 변경해주세요.”
  - Legacy order field alias 권장안에 대해 “네”
  - Missing/null/empty/`ALL` 권장안에 대해 “네”
  - Size registry 제안에 대한 변경 답변: “코드는 얼마든지 자유롭게 들어올 수 있습니다.”
  - 자유 형식 코드 matching 권장안에 대해 “네”
- **해석한 결정:**
  - Vehicle에는 차량 자체의 크기를 나타내는 구체적인 코드 하나가 온다. Vehicle 측 필드명은 `vehicleFeature`다.
  - Order에는 해당 주문 위치에서 허용되는 차량 크기 코드의 목록이 온다.
  - Order 측 신규 필드명은 단수형 `vehicleFeature`가 아니라 배열임을 드러내는 `vehicleFeatureList`로 변경한다.
  - `vehicleFeatureList`는 `["T1", "T1.4", "T1.9"]` 같은 구체 코드 목록 또는 `["ALL"]` 형태를 가질 수 있다.
  - 이 인터뷰에서는 schema/code/fixture를 변경하지 않고, 후속 통합 세션이 문서 설계에 반영하도록 결정만 기록한다.
  - 신규 입력과 canonical 출력은 `order.vehicleFeatureList`를 사용한다.
  - Legacy 입력의 배열형 `order.vehicleFeature`는 구버전 alias로 읽고 내부 `vehicleFeatureList`로 정규화한다.
  - 두 order 필드가 함께 있으면 목록이 같을 때만 허용하고, 값이 다르면 입력 오류다.
  - Legacy order `vehicleFeature`가 배열이 아니라 단일 문자열이면 입력 오류다.
  - `vehicle.vehicleFeature`에는 구체적인 크기 코드 하나가 반드시 있어야 한다. Missing, null, 빈 문자열과 `"ALL"`은 입력 오류다.
  - `order.vehicleFeatureList`에는 하나 이상의 구체 코드 또는 정확히 `["ALL"]`이 있어야 한다. Missing, null과 빈 배열은 입력 오류다.
  - `["ALL", "T1"]`처럼 `ALL`과 구체 코드를 섞은 목록은 입력 오류다.
  - 모든 차량 크기를 허용한다는 의미는 order의 `["ALL"]`로만 표현한다.
  - Vehicle size code에는 고정된 사전 allowlist/registry를 두지 않으며 새로운 구체 코드는 자유롭게 입력할 수 있다.
  - 따라서 코드가 기존 fixture에서 관찰되지 않았다는 이유로 unknown-code 입력 오류를 만들지 않는다.
  - 현재 fixture에서 관찰된 `T1`, `T1.4`, `T1.9`, `T2.5`, `T3.5`, `T5`, `T7`, `T8`은 예시일 뿐 전체 registry가 아니다.
  - Size code는 비어 있지 않은 임의 문자열이며 대소문자를 구분하여 exact string equality로 비교한다.
  - `"T1"`과 `"t1"`은 서로 다른 코드다.
  - Order의 구체 코드와 같은 코드를 가진 vehicle이 있으면 size-compatible이다.
  - Order 목록에 현재 fleet에 없는 코드가 있어도 입력 오류가 아니다. 그 코드는 eligible vehicle을 만들지 않을 뿐이다.
  - Order 목록의 모든 구체 코드에 대응하는 vehicle이 없으면 해당 request는 “크기 호환 차량 없음” 근거로 배정 불가다.
  - `"ALL"`은 order `vehicleFeatureList`에서 모든 차량을 허용하는 예약값이며 앞서 확정한 대로 단독 원소로만 사용한다.
- **근거:**
  - 사용자 직접 설명.
  - 현재 Win fixture에서 vehicle `vehicleFeature`는 단일 문자열이고 order `vehicleFeature`는 배열인 실제 shape와 일치한다.
- **영향 문서:**
  - `docs/master-design.md` §5.3 Vehicle size와 capability, §7.5 Static compatibility
  - `docs/master-design-open-questions.md` `Q-COMP-01`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/20-domain-input-draft.md` §6
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 input schema/Legacy adapter, canonical order DTO, `servableVehicles`, verifier와 `RM-1`
- **남은 모호성:** 없음.
- **상태:** `RESOLVED`

### Q-COMP-02 — Size restriction과 zone 합성

- **사용자 답변:**
  - “차량은 기본적으로 모든 지역을 다 방문할 수 있습니다.”
  - “만일 명시적으로 zoneId가 지정되면 차량은 그 zoneId에 속한 order만 방문 가능”
  - “차량은 2개 이상의 zoneId를 방문할 수 없음”
  - “예를 들어 서울zone인 order와 경기도zone인 order 를 한 번에 방문할 수 없음”
  - Size와 zone의 AND 결합 확인에 대해 “네 맞습니다. and임”
  - Pickup/delivery 제한 합성 권장안에 대해 “네 맞습니다. 확정”
  - Zone 미지정 정책 정정:
    - “아닙니다. order도 zone 미지정이 가능합니다.”
    - “외부에서 입력시, 또는 값이 없는 경우에 "ALL"로 통일시켜 주세요.”
    - “order1(서울) -> order2(ALL) -> order3(서울)” 방문 가능
  - `"ALL"`을 제외한 구체 zone 집합 규칙의 최종 확인에 대해 “네 맞습니다.”
- **해석한 결정:**
  - Zone restriction이 명시되지 않은 vehicle은 어떤 단일 zone의 order에도 배정될 수 있다.
  - Vehicle에 특정 `zoneId`가 명시되면 그 vehicle은 같은 `zoneId`의 order만 방문할 수 있다.
  - 한 vehicle route가 방문하는 order의 서로 다른 `zoneId` cardinality는 최대 1이다.
  - 따라서 zone 미지정 vehicle도 서울 zone order와 경기도 zone order를 같은 route에 혼합할 수 없다.
  - Zone 미지정은 모든 zone을 한 route에서 섞어도 된다는 뜻이 아니라, route가 사용할 단일 zone을 사전에 고정하지 않았다는 뜻이다.
  - 이 규칙은 request별 고정 membership만으로 끝나지 않고 route의 첫 zone 배정 뒤 후속 삽입을 제한하는 route-level hard constraint다.
  - Size compatibility와 zone compatibility는 독립 hard constraint이며 차량은 두 조건을 모두 만족해야 한다.
  - Size 또는 zone 중 하나만 만족하는 OR, 또는 한 제약이 다른 제약을 덮어쓰는 방식은 사용하지 않는다.
  - 실제 pickup과 delivery가 서로 다른 size 허용 목록을 가지면 같은 차량이 두 작업을 모두 수행할 수 있도록 두 목록의 교집합을 사용한다.
  - Pickup/delivery size 허용 목록의 교집합이 비면 해당 request는 size-compatible vehicle이 없어 배정할 수 없다.
  - 실제 pickup과 delivery의 `zoneId`가 다르면 한 route가 두 zone을 방문하게 되므로 해당 request는 배정할 수 없다.
  - Delivery-only의 공통 depot 가상 pickup은 실제 고객 zone 방문이 아니므로 route zone cardinality와 pickup/delivery zone 일치 검사에서 제외한다. Delivery order의 zone만 적용한다.
  - Order도 zone 미지정이 가능하다.
  - 외부 zone 값이 없으면 normalization 단계에서 `"ALL"`로 통일한다.
  - `order.zoneId="ALL"`은 어떤 구체 zone에도 속하지 않는 zone-neutral/wildcard 방문이다.
  - `서울 → ALL → 서울` route는 서로 다른 구체 zone을 혼합하지 않으므로 허용한다.
  - Route의 구체 zone 집합은 방문 order 중 `zoneId != "ALL"`인 zone들의 집합이다.
  - Route feasibility는 구체 zone 집합의 cardinality가 최대 1일 것을 요구한다.
  - `서울 → ALL → 서울`과 `ALL → ALL`은 가능하고, `서울 → ALL → 경기도`는 불가능하다.
  - 구체 zone이 정해지지 않은 vehicle은 어떤 단일 구체 zone route 또는 ALL-only route에도 사용할 수 있다.
  - 서울처럼 구체 zone이 지정된 vehicle은 서울 order와 `ALL` order를 방문할 수 있지만 다른 구체 zone order는 방문할 수 없다.
  - Vehicle/order의 zone이 missing, null 또는 빈 문자열이면 normalization 단계에서 `"ALL"`로 통일한다.
  - `"ALL"` 이외의 zone code는 자유 형식이며 대소문자를 구분한 exact string equality로 비교한다.
- **근거:**
  - 사용자 직접 설명과 서울/경기도 route 예시.
  - 현재 Win fixture의 452개 order는 모두 하나의 `zoneId`를 가지며 11개 zone으로 분포하지만 vehicle에는 zone 필드가 없다.
- **영향 문서:**
  - `docs/master-design.md` §5.3 Vehicle size와 capability, §7.5 Static compatibility, §11.1 evaluator
  - `docs/master-design-open-questions.md` `Q-COMP-02`
  - `docs/master-design-sessions/20-domain-input-draft.md` §6.3
  - `docs/master-design-sessions/21-policy-objective-draft.md`
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 vehicle/order schema, static precheck, route zone state, pair evaluator, verifier와 `RM-1`
- **남은 모호성:** 없음.
- **상태:** `RESOLVED`

### Q-REQ-01 — Delivery-only와 실제 pickup-delivery의 혼합 route

- **사용자 답변:** “혼합을 허용합니다.”
- **질문에서 설명한 혼합 의미:**
  - 한 차량의 한 single-trip route에 depot 출발 전 적재된 delivery-only request와 route 중 실제 위치에서 상차하는 pickup-delivery request가 함께 존재한다.
  - Delivery-only의 pickup은 초기 적재를 나타내는 논리 작업이며 실제 depot 재방문, travel 또는 stop을 추가하지 않는다.
- **해석한 결정:**
  - Delivery-only request와 실제 pickup-delivery request를 같은 single-trip route에 혼합할 수 있다.
  - Route 시작 적재량은 그 route에 배정된 모든 delivery-only request demand의 합이다.
  - Delivery-only 고객 방문에서는 해당 demand가 load에서 감소한다.
  - 실제 pickup 방문에서는 해당 request demand가 load에 증가하고, 실제 delivery 방문에서는 감소한다.
  - 실제 pickup과 delivery는 같은 차량 route에 정확히 한 번씩 존재하며 pickup이 delivery보다 먼저다.
  - 혼합 route의 모든 prefix에서 모든 load 차원이 `0 <= load <= capacity`를 만족해야 한다.
  - Delivery-only 화물을 보충하기 위한 중간 depot 방문 또는 묵시적 재상차를 허용하지 않는다.
  - 혼합은 multi-trip/rotation을 뜻하지 않으며 현재 terminal과 single-trip 계약을 유지한다.
  - Size와 zone을 포함한 승인된 hard compatibility는 모든 실제 pickup/delivery 방문에 적용한다. 공통 depot의 delivery-only 논리 pickup은 `Q-COMP-02`에 따라 zone 방문으로 세지 않는다.
- **근거:**
  - 사용자 직접 결정.
  - 사용자에게 차량 용량 5, delivery-only 초기 load 4, 실제 pickup load 3의 순서별 예시를 설명한 뒤 승인받았다.
- **영향 문서:**
  - `docs/master-design.md` §5.2 Service meaning, §6 핵심 불변조건, §11.1 pair evaluator
  - `docs/master-design-open-questions.md` `Q-REQ-01`
  - `docs/master-design-sessions/20-domain-input-draft.md`
  - `docs/master-design-sessions/22-algorithm-draft.md`
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 canonical service-pattern binding, load propagator, pair operators, verifier와 `RM-1`·`RM-3`~`RM-5`
- **남은 모호성:** 없음.
- **상태:** `RESOLVED`

### Q-REQ-02 — Single round trip과 multi-trip 경계

- **사용자 질문:** “일반적인 round trip 까지는 가능하지 않나요?”
- **구분 설명 후 사용자 답변:** single round trip은 현재 지원하고 multi-trip/회전 배차만 후속 범위이며, 향후에도 pair가 trip 경계를 넘지 않는다는 정리에 대해 “네 맞습니다.”
- **해석한 결정:**
  - 일반적인 single round trip은 현재 표준 범위에서 지원한다.
  - `oneway` single-trip은 depot에서 출발하여 마지막 고객에서 끝난다. `Q-BENCH-03`에 따라 `multiRotation` 값은 무시한다.
  - `roundtrip + multiRotation=0`인 single-trip은 depot에서 출발하여 주문들을 방문한 뒤 같은 terminal/depot으로 복귀한다.
  - 현재 미지원인 것은 같은 차량이 depot 복귀 후 다시 출발하는 multi-trip/회전 배차다.
  - `roundtrip + multiRotation != 0`은 회전 배차가 활성화되기 전까지 `UNSUPPORTED_INPUT`이다.
  - 향후 multi-trip을 활성화해도 하나의 pickup-delivery pair는 같은 trip 안에서 완료해야 한다.
  - Pickup을 trip 1에서 수행하고 depot에 복귀한 뒤 해당 delivery를 trip 2에서 수행하는 pair crossing은 금지한다.
  - Delivery-only request도 하나의 trip에 완전히 속하며, 각 trip 종료 시 해당 trip의 미완료 화물이 없어야 한다.
  - 후속 trip은 `Q-IN-02`에서 정한 depot `duration`을 적용한 뒤 새로운 trip load로 시작한다.
- **근거:**
  - 사용자 직접 확인.
  - Round trip은 terminal 복귀 정책이고 multi-trip은 같은 차량의 복수 depot-to-depot 운행이라는 구분.
- **영향 문서:**
  - `docs/master-design.md` §5.2 Service meaning, §6 route invariant, §16 deferred work
  - `docs/master-design-open-questions.md` `Q-REQ-02`
  - `docs/master-design-sessions/11-input-schema-and-time-contract.md`
  - `docs/master-design-sessions/20-domain-input-draft.md`
  - `docs/master-design-sessions/22-algorithm-draft.md`
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - `docs/master-design-sessions/24-roadmap-draft.md`
  - 향후 terminal policy, trip model, load reset, pair evaluator, verifier와 `RM-1`
- **남은 모호성:** 현재 표준 범위에는 없음. Multi-trip 활성화의 상세 trip/resource 계약은 후속 기능 설계에서 별도로 승인한다.
- **상태:** `RESOLVED`

### Q-OBJ-01 — 고객사 내 objective preset 선택

- **사용자 답변:**
  - 제안한 고객사별 승인 preset 선택 및 기본값 정책에 대해 “네”
  - “중요한 것은 특정 목적 함수는 다른 고객사에 없을 수 있습니다. 이런 점도 기억할 것”
- **해석한 결정:**
  - Solve 요청은 해당 고객사에 미리 등록·승인된 objective preset을 선택할 수 있다.
  - 요청이 objective 순서, 가중치 또는 수식을 임의로 직접 주입할 수는 없다.
  - Preset 선택 권한과 가용 목록은 고객사별로 격리한다. 다른 고객사의 preset을 선택할 수 없다.
  - 특정 objective dimension, metric, score component 또는 목적함수 자체가 일부 고객사에만 존재할 수 있다.
  - 모든 고객사가 같은 objective catalog를 가져야 한다고 가정하거나, 한 고객사에 없는 목적함수를 다른 고객사의 구성으로 fallback하지 않는다.
  - 미등록·미허용 preset, 해당 고객사에 없는 objective 또는 충족되지 않은 metric/fact 의존성은 solve 시작 전 binding 오류다.
  - Unknown preset을 고객 기본값이나 비슷한 preset으로 조용히 치환하지 않는다.
  - 요청에서 preset을 생략하면 그 고객사 설정에 exact key/version으로 지정된 기본 preset을 사용한다.
  - 실제 bound preset key/version, config hash, objective/comparator와 `SolvePlan` lineage를 result와 execution fingerprint에 기록한다.
  - Win PoC comparator를 고객사 공통 기본값으로 만들지 않으며, benchmark에서 사용할 때도 별도 승인된 benchmark profile로 bind한다.
- **근거:**
  - 사용자 직접 승인과 고객사별 목적함수 가용성 강조.
  - Master의 고객사별 최소 코어 변경 목표 및 세션 21의 immutable bound profile 계약.
- **영향 문서:**
  - `docs/master-design.md` §9 policy/profile architecture, §10.3 result provenance, §13 reproducibility
  - `docs/master-design-open-questions.md` `Q-OBJ-01`
  - `docs/master-design-sessions/21-policy-objective-draft.md` §8, §11
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 submission contract, customer profile registry/binding, result metadata와 `RM-2`
- **남은 모호성:** 없음. 최종 API/type 이름은 후속 상세 설계가 이 권한·격리·lineage 의미를 보존하여 정한다.
- **상태:** `RESOLVED`

### Q-OBJ-02 — Mandatory order의 최적화 의미

- **사용자 답변:** “최상위 사전식 objectvie 입니다.”
- **해석한 결정:**
  - Mandatory order는 hard feasibility rule이 아니다.
  - Mandatory 미배정을 임의의 큰 유한 penalty로 다른 비용과 같은 scalar 차원에 합치지 않는다.
  - Mandatory 의미를 사용하는 고객 preset에서는 `mandatoryUnassignedCount`를 모든 다른 목적보다 앞선 최상위 사전식 objective로 둔다.
  - Mandatory request를 하나라도 더 배정할 수 있으면 차량 수·거리·시간·비용 등 하위 objective가 좋아도 mandatory 미배정 수가 더 많은 해를 선택하지 않는다.
  - `mandatoryUnassignedCount=0`이 불가능해도 전체 solve를 자동 `INFEASIBLE`로 만들지 않는다. 가능한 최소 양수 값을 가진 verified partial solution과 해당 미배정 outcome/diagnostic을 반환할 수 있다.
  - Mandatory objective의 실제 포함 여부와 나머지 objective 순서는 `Q-OBJ-01`에서 확정한 고객사별 승인 preset이 소유한다. Mandatory를 지원하지 않는 고객사에 이 차원을 전역 주입하지 않는다.
  - Objective 값은 search bank/final outcome에서 재계산 가능해야 하며 result에 exact preset/comparator lineage를 남긴다.
- **근거:**
  - 사용자 직접 결정.
  - Hard, finite soft preference, top-level lexicographic 세 대안과 불가능한 mandatory 사례의 영향을 설명한 뒤 선택받았다.
- **영향 문서:**
  - `docs/master-design.md` §9 policy/profile architecture, §10 search/result
  - `docs/master-design-open-questions.md` `Q-OBJ-02`
  - `docs/master-design-sessions/21-policy-objective-draft.md` §10.2
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 mandatory input normalization, objective schema/comparator, `SolvePlan`, result/verifier와 `RM-2`·`RM-5`
- **남은 모호성:** 없음. Mandatory 입력 필드의 구체 이름은 후속 schema 설계가 이 의미를 보존하여 정한다.
- **상태:** `RESOLVED`

### Q-OBJ-03 — 외주 차량 최적화와 미배정 후속 처리

- **사용자 답변 1:** “이건 solver가 처리하는 것이 아닙니다. 괸리자, 운영자의 영역입니다.”
- **추가 설명:**
  - “일반적으로 차량 유형에 정규 차량, 외주차량 등의 정보가 포함되어 있습니다.”
  - “score를 계산할 때 정규 차량을 우선시하고, 외주 차량을 다음으로 생각합니다”
  - “이렇게 해도 미배정되는 것은 배정을 할 수 없는 것입니다. 이것은 이 프로젝트의 영역이 아닙니다. 운영자, 관리자가 다른 프로세스로 처리할 일입니다.”
- **정규/외주 우선 score 제안:**
  - “3 은 이렇게 할 수 있지 않을까요? 3. 차량 비용 최소화”
  - “정규 차량은 -1, 외주 차량은 -100”
  - “차량 부피를 위 비용에 곱합”
  - “만일 똑같은 10이면, 정규 차량은 -10, 외주 차량은 -1,000”
- **정규 차량 절대 우선 후속 답변:**
  - “네 정규차량이 무조건 먼저 사용해야 합니다. 대수가 더 많아지더라도 먼저 사용해야 합니다.”
  - “왜냐하면 정규차량은 이미 비용을 전부 지불한 것입니다. 외주차량은 추가 비용입니다.”
- **동일 유형 차량 선택 후속 답변:** “같은 정규차량이라 하더라도 부피가 작은 차량을 사용하는 것이 좋습니다.”
- **고객사별 외주 objective 제안:**
  - “일반적으로 전부 정규 차량을 사용합니다. 경우에 따라 외주 차량을 사용합니다.”
  - “따라서 정규 차량 부피 비용 최소화는 기본으로 하고, 고객에 따라 외주 차량 부피 비용 최소화를 올릴 수 있을까요?”
- **고객사별 objective 구성 최종 답변:** “네 맞습니다.”
- **해석한 결정:**
  - 입력 fleet에 실제 vehicle resource로 포함된 정규 차량과 외주 차량은 모두 solver의 배정 대상이다.
  - Vehicle은 정규/외주를 구분할 수 있는 유형 정보를 가진다.
  - Solver의 score/objective는 정규 차량 배정을 외주 차량 배정보다 우선한다.
  - 입력 fleet에 없는 외부 공급자를 solver가 임의로 생성하거나, 미배정 request를 공급자 정보 없이 `OUTSOURCED`로 추정하지 않는다.
  - 정규·외주 vehicle resource를 모두 고려한 뒤에도 route에 배정되지 않은 request는 solver search state에서 `UNASSIGNED`로 남는다.
  - 그 미배정 request를 추가 외주하거나 후속 계획으로 이월하는 결정은 이 프로젝트의 solver 범위가 아니라 관리자·운영자의 별도 프로세스다.
  - “배정할 수 없는 것”이라는 업무 표현을 모든 미배정 request에 대한 수학적 불가능성 증명으로 확대하지 않는다. 진단 confidence는 후속 `Q-RES-02`의 audit 계약을 따른다.
  - 사용자는 외주 배정 주문 수 대신 차량 유형별 계수와 차량 부피를 곱한 vehicle cost/score를 제안했다.
  - 제안된 예시는 같은 부피 10에서 정규 차량 `-10`, 외주 차량 `-1,000`이다.
  - “비용 최소화”를 수학적 minimization으로 적용하면 `-1,000 < -10`이므로 외주 차량이 더 선호되어 사용자의 “정규 차량 우선”과 반대가 된다.
  - 음수 값을 유지하고 score를 maximize할지, 양수 비용으로 바꾸고 minimize할지 후속 확인 전 추정하지 않는다.
  - 차량 부피가 capacity volume인지 실제 assigned volume인지, 사용 차량당 한 번 계산하는지도 후속 확인 전 추정하지 않는다.
  - 정규 차량 우선은 거리, 일반 차량 수 또는 다른 하위 비용과 교환되는 유한 선호가 아니라 strict priority다.
  - 같은 mandatory/total assignment 수준에서 외주 차량을 쓰지 않는 feasible solution이 있으면, 정규 차량 대수가 더 많아져도 그 해를 선택한다.
  - 정규 차량 비용은 이미 지불된 sunk cost이고, 외주 차량만 현재 solve의 추가 비용을 발생시킨다.
  - 따라서 단순 `1:100` scalar만 하위 비용과 합산하여 strict priority를 흉내 내서는 안 된다. 외주 추가비용은 하위 목적보다 앞선 별도 사전식 차원이어야 한다.
  - 동시에 같은 정규 차량 유형 안에서는 부피 용량이 작은 차량을 선호해야 한다.
  - 정규 차량 기여값을 모두 0으로 두면 작은 정규 차량 선호를 표현할 수 없으므로, 외주 strict priority와 사용 차량 부피 선호를 분리해야 한다.
  - 사용자는 정규 차량의 사용 부피 비용 최소화를 기본 objective로 두고, 외주 차량을 사용하는 고객사에만 외주 차량 사용 부피 비용 최소화를 더 높은 objective로 추가하는 구성을 제안했다.
  - 이 구성은 `Q-OBJ-01`에서 확정한 고객사별 objective 가용성과 일치하며, 외주 objective가 없는 고객사에 해당 차원을 전역 주입하지 않는다.
  - 기본 `regularVehicleVolumeCost`는 사용한 정규 차량별 `maxVolume` 합을 최소화한다.
  - 외주 차량을 사용하는 고객사 profile은 `outsourcedVehicleVolumeCost`, 즉 사용한 외주 차량별 `maxVolume` 합을 최소화하는 차원을 추가한다.
  - 외주 차량 부피 비용 차원은 정규 차량 부피 비용 차원보다 앞선 사전식 objective다.
  - Mandatory objective를 사용하는 고객사에서는 `mandatoryUnassignedCount`가 최상위이며, 전체 미배정 수 뒤에 선택적 외주 차량 부피 비용, 기본 정규 차량 부피 비용과 나머지 고객 목적이 이어진다.
  - 외주 차량 부피 비용이 같을 때 정규 차량 부피 비용을 비교하므로 같은 정규 유형 내에서 더 작은 사용 차량 조합을 선호한다.
  - 각 비용은 주문을 하나 이상 수행한 used vehicle의 `maxVolume`을 차량당 한 번 합산하며 미사용 vehicle은 세지 않는다.
  - 외주 vehicle이 입력되었는데 선택된 고객 profile/preset이 외주 objective를 지원하지 않으면 의도치 않은 외주 사용을 허용하지 않고 solve 전 binding 오류로 처리한다.
  - 별도 사전식 차원으로 의미를 표현하므로 고정 1:100 가중치나 instance-derived Big-M을 사용하지 않는다.
- **근거:**
  - 사용자 직접 설명.
  - 외주 vehicle resource를 solver가 고려하는 것과, 미배정 결과에 대한 운영자의 후속 disposition을 구분한 업무 경계.
- **영향 문서:**
  - `docs/master-design.md` §9 policy/profile architecture, §10 search/result
  - `docs/master-design-open-questions.md` `Q-OBJ-03`
  - `docs/master-design-sessions/13-unassigned-status-and-diagnostics.md`
  - `docs/master-design-sessions/21-policy-objective-draft.md` §10.3
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 vehicle schema, score/objective preset, result outcome, verifier와 `RM-2`·`RM-5`
- **남은 모호성:**
  - 외주 vehicle에 배정된 request의 최종 status 표현은 `Q-RES-01`과 함께 확인해야 한다.
- **상태:** `RESOLVED`

### Q-RES-01 — 외주 차량 배정 상태와 소유 유형

- **사용자 답변:**
  - 외주 차량 route에 배정된 request도 `ASSIGNED`로 두고 vehicle type으로 구분하는 첫 번째 방식 선택.
  - “외주 여부는 차량에 아래 컬럼으로 관리합니다”
  - `vhclOwnTyp`: `DIRECT`는 직영이며 기본값, `LEASE`는 외주.
  - “이 값은 optional입니다.”
  - 입력 정규화 후속 확인: missing·`null`·빈 문자열은 `DIRECT`, 허용값은 대소문자를 구분하는 `DIRECT`·`LEASE`, 그 밖의 값은 입력 오류로 처리하는 제안에 “네 맞습니다.”
- **해석한 결정:**
  - 정규 차량과 외주 차량 모두 solver 입력의 실제 vehicle resource다.
  - 어떤 입력 vehicle의 verified route에 배정된 request는 vehicle 소유 유형과 관계없이 최종 status가 `ASSIGNED`다.
  - 정규/외주 여부는 request outcome status가 아니라 vehicle의 optional `vhclOwnTyp`으로 표현한다.
  - `vhclOwnTyp=DIRECT`는 직영 차량, `vhclOwnTyp=LEASE`는 외주 차량이다.
  - `vhclOwnTyp`의 missing·`null`·빈 문자열은 기본값 `DIRECT`로 정규화한다.
  - 명시할 수 있는 값은 대소문자를 구분하는 정확한 `DIRECT` 또는 `LEASE`다.
  - 그 밖의 non-empty 값은 `DIRECT`나 `LEASE`로 추측하지 않고 입력 오류로 처리한다.
  - `LEASE` vehicle 사용은 `Q-OBJ-03`에서 정한 고객사 선택적 외주 차량 부피 objective와 비용 breakdown에 반영한다.
  - 입력 fleet 어디에도 배정되지 않은 request는 solver result에서 `UNASSIGNED`다.
  - 관리자·운영자가 별도 프로세스에서 수행하는 추가 외주나 이월은 이 프로젝트가 `OUTSOURCED`/`DEFERRED` status로 생성하지 않는다.
- **근거:**
  - 사용자 직접 결정과 vehicle ownership field 정의.
  - Route assignment 여부와 vehicle ownership을 서로 다른 축으로 분리한다.
- **영향 문서:**
  - `docs/master-design.md` §5 vehicle, §9 objective/profile, §10 final result
  - `docs/master-design-open-questions.md` `Q-RES-01`
  - `docs/master-design-sessions/13-unassigned-status-and-diagnostics.md`
  - `docs/master-design-sessions/21-policy-objective-draft.md`
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 vehicle input schema, ownership normalization, objective breakdown, result/verifier와 `RM-1`·`RM-2`·`RM-5`
- **남은 모호성:** 없음. 향후 schema 명칭 변경은 `vhclOwnTyp`의 확정된 의미와 정규화 규칙을 보존해야 한다.
- **상태:** `RESOLVED`

### Q-RES-02 — 미배정 request의 최종 해 삽입 감사

- **사용자 답변:**
  - 정적 검사로 이미 `PROVEN`인 주문을 제외하고 나머지 모든 `UNASSIGNED` 주문에 전수 삽입 검사를 수행하는 권장안에 “네”.
  - Audit가 feasible insertion을 발견했을 때의 처리로 “1번으로 하려고 합니다. 왜냐하면 검사기가 잘못되었다면 무한 루프가 발생할 수 있습니다.”
- **해석한 결정:**
  - Normalization/precheck만으로 route 순서나 탐색 품질과 무관한 배정 불가능성이 증명된 request는 `PROVEN` 진단을 사용하며 중복된 final-solution insertion audit를 요구하지 않는다.
  - 그 밖의 모든 최종 `UNASSIGNED` request에는 결과 게시 전 final-solution exhaustive insertion audit를 수행한다.
  - Audit는 확정된 다른 route와 request 배치를 고정하고, 해당 request의 모든 eligible vehicle과 모든 합법 pickup/delivery insertion position pair를 검사한다.
  - 모든 경우가 실패한 request만 `EXHAUSTIVE_FOR_FINAL_SOLUTION` confidence와 `NO_FEASIBLE_INSERTION_IN_FINAL_SOLUTION` 계열 진단을 가질 수 있다.
  - 이 confidence는 현재 final route 기준의 삽입 불가만 뜻한다. 다른 request를 옮기거나 여러 route를 재구성해도 불가능하다는 전역 증명으로 표현하지 않는다.
  - Audit contract/version, 검사한 vehicle·position 수, constraint별 rejection count와 완결성을 evidence로 남긴다.
  - Audit work count와 elapsed time은 ALNS `completedSteps` 및 solution quality vector와 분리해 finalization/verification 비용으로 측정한다.
  - Audit가 feasible insertion을 하나 이상 발견해도 현재 result의 publication을 거부하지 않고 해당 request는 `UNASSIGNED`로 게시할 수 있다.
  - Finalization/audit 단계는 발견된 위치에 request를 자동 삽입하지 않는다.
  - 이 audit 결과만으로 solver를 자동 재호출하거나 수정·재탐색 루프에 넣지 않는다. 검사기 결함이 반복 solve를 유발할 수 있다는 운영 위험을 피한다.
  - Feasible insertion을 발견한 request에는 `EXHAUSTIVE_FOR_FINAL_SOLUTION` 또는 `NO_FEASIBLE_INSERTION_IN_FINAL_SOLUTION`을 부여할 수 없다.
  - Feasible insertion 발견 사실과 그 evidence는 내부 audit record에만 보존한다.
  - 외부 request outcome에는 그 발견 사실을 노출하지 않고 일반 `UNASSIGNED`로 게시한다. 공개 diagnostic은 별도로 성립하는 proven precheck, search-observed 또는 `UNKNOWN` 근거만 사용할 수 있다.
- **근거:**
  - 사용자 직접 승인.
  - 모든 미배정 request에 대해 가능한 한 일관된 설명 수준을 제공하되, 독립적인 static proof가 이미 있는 request에는 같은 결론을 위한 중복 감사를 요구하지 않는 권장안.
- **영향 문서:**
  - `docs/master-design.md` §10 search solution과 final result, §14 independent verification
  - `docs/master-design-open-questions.md` `Q-RES-02`
  - `docs/master-design-sessions/13-unassigned-status-and-diagnostics.md`
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 finalization audit, diagnostic evidence/result schema, result-integrity verifier와 `RM-5`
- **남은 모호성:** 없음. 내부 audit record의 보존·접근 권한은 후속 result storage 설계가 정하되 외부 payload 비노출 의미를 바꾸지 않는다.
- **상태:** `RESOLVED`

### Q-BENCH-01 — Win PoC `전체 시간` 공식

- **사용자 답변:**
  - “대기, 서비스, 휴식 시간을 포함하는 것이 나을까요? 왜냐하면 순수 주행시간이 같다고 하더라도, 가게가 늦게 열면 대기 시간이 길어지고 이건 좋은 해가 아닙니다”
  - 주행·고객 대기·depot 대기·서비스·근무창 사이 휴식의 합을 사용하는 제안에 “네”.
- **해석한 결정:**
  - Win PoC comparator의 네 번째 성분 `전체 시간`은 순수 주행시간 합이 아니라 모든 used vehicle route의 운영 경과시간 합이다.
  - Canonical 공식은 `totalRouteOperationalTimeSeconds = Σ_used_routes(driveTime + customerWaitingTime + depotWaitingTime + serviceTime + interWorkWindowRestTime)`이다.
  - `driveTime`은 실제 route가 통과한 authoritative `U_seconds` arc 합이다.
  - `customerWaitingTime`과 `depotWaitingTime`을 모두 포함한다. `waitInDepot`이 같은 대기를 고객 위치에서 depot으로 옮겨도 전체 시간에서 사라지지 않는다.
  - `serviceTime`은 방문별 확정 서비스시간을 합한다. Order 방문은 `duration + Σ(item.taskTime × qty)`를 사용하며, depot duration은 실제 적용되는 향후 rotation 경계에서만 포함한다.
  - 반복 근무창 사이에 route가 다음 날까지 이어질 때의 휴식시간을 포함한다.
  - 사용하지 않은 vehicle의 유휴시간, route가 시작되기 전 운영과 무관한 시간, solver·검증·직렬화 실행시간은 포함하지 않는다.
  - Pure drive, customer/depot wait, service, rest와 total operational time은 결과와 benchmark card에서 각각 재계산 가능한 breakdown으로 보존한다.
  - 네 번째 성분도 앞선 `미배정 주문 수 → 배차 차량 수 → 전체 거리`가 모두 같을 때만 비교하는 Win PoC 전용 사전식 성분이다.
- **근거:**
  - 사용자 직접 결정.
  - 같은 순수 주행시간이라도 영업 시작이 늦어 대기가 긴 해는 좋은 해가 아니라는 업무 판단.
- **영향 문서:**
  - `docs/master-design.md` §10 metrics/result, §14 Win PoC comparator
  - `docs/master-design-open-questions.md` `Q-BENCH-01`
  - `docs/master-design-sessions/16-win-poc-benchmark.md`
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - 향후 metric formula registry/manifest, propagator, verifier, benchmark card와 `RM-5`·`RM-6`
- **남은 모호성:** 없음. 향후 multi-rotation이 활성화되면 실제 적용된 depot duration과 trip-boundary wait/rest를 같은 additive breakdown에 포함한다.
- **상태:** `RESOLVED`

### Q-ALG-01 — 초기해 포트폴리오 공식 기본값

- **사용자 확인 질문:** “초기 해는 여러 가능성 중 가장 좋은 것으로 하기로 하지 않았나요? `docs/arranged/02_initial_solution_heuristics.md` 문서를 참고해주세요”
- **사용자 답변:** 여러 후보 중 최선해를 항상 선택하면서 검증된 다양해를 추가 ALNS 시작점으로 제공하고, 다양해 개수 `K`는 실험으로 정한다는 설명에 “네”.
- **실험·승인 후속 답변:** Farthest/deadline scorer, randomized start 수, `K`, 후보별 light-search work budget을 동일한 실험·승인 절차로 정하고 실험 전까지 `OPEN — EXPERIMENT_REQUIRED`로 유지하는 제안에 “네”.
- **해석한 결정:**
  - 여러 construction policy와 randomized run이 만든 candidate는 light improvement와 독립 검증을 거친 뒤 고객사에 bind된 comparator로 비교한다.
  - Comparator상 가장 좋은 candidate 한 개를 반드시 `best_initial_solution`으로 선택한다.
  - `top_k_diverse_solutions`는 best를 대신하거나 best보다 우선하는 후보가 아니라, admission을 통과한 서로 다른 route 구조를 ALNS의 추가 시작점으로 제공하는 별도 집합이다.
  - `best_initial_solution`은 항상 ALNS warm-start set에 포함되며 diverse candidate 때문에 제외되지 않는다.
  - Diverse candidate 수 `K`는 근거 없이 지금 확정하지 않고 실험으로 정한다.
  - “초기해는 여러 가능성 중 가장 좋은 것”과 “best 외의 검증된 다양해를 추가 보존”은 서로 충돌하지 않는다.
  - Farthest/deadline scorer 공식, randomized start 수, `K`와 후보별 light-search work budget은 하나의 versioned calibration experiment에서 비교한다.
  - 후보 configuration은 동일한 승인 corpus, recorded seed set과 downstream ALNS step budget을 사용한다.
  - 각 run은 independent verifier를 통과해야 하며 initial/final objective quality, candidate diversity와 duplicate rate, deterministic rerun 일치, work counter, elapsed time과 memory를 함께 측정한다.
  - 검증 또는 재현성에 실패한 configuration은 공식 기본값 후보에서 제외한다.
  - Algorithm·Benchmark owner가 측정 결과와 resource envelope를 검토하여 exact configuration/version을 명시적으로 승인한 뒤에만 공식 기본값으로 발행한다.
  - 승인 전에는 explicit experiment/test configuration만 허용하며 hidden code default를 두지 않는다.
- **근거:**
  - 사용자 직접 확인.
  - `docs/arranged/02_initial_solution_heuristics.md` §2는 후보별 light improvement 후 best와 diverse solution을 모두 보관하고, §13은 best 한 개와 route 구조가 다른 solution pool을 ALNS에 전달하도록 권고한다.
  - 현재 Master와 세션 15·22도 best + limited diverse initial candidates를 사용하되 정확한 `K`를 `Q-ALG-01`에 남긴다.
- **영향 문서:**
  - `docs/master-design.md` §11.2 initial-solution portfolio
  - `docs/master-design-open-questions.md` `Q-ALG-01`
  - `docs/master-design-sessions/15-initial-solution-portfolio.md`
  - `docs/master-design-sessions/22-algorithm-draft.md`
  - 향후 portfolio configuration/manifest, `InitialSolutionSet`, ALNS warm-start assignment와 `RM-3`
- **남은 모호성:**
  - 실제 scorer 공식과 수치는 승인된 실험 corpus와 실행 결과가 아직 없으므로 미확정이다.
- **상태:** `OPEN — EXPERIMENT_REQUIRED`

### Q-ALG-02 — Candidate state의 COW/apply-undo 기본 경로

- **사용자 답변:** “cow 로 제안한대로 진행”
- **해석한 결정:**
  - 현재 구현·운영 기본 candidate state strategy는 copy-on-write(`COW`)다.
  - Candidate는 committed `current`, `stageBest`, `solveBest`를 직접 변경하지 않고 변경 route와 독립 bank를 사용한다.
  - Accepted candidate만 immutable current로 교체하고 rejected·interrupted·failed candidate는 전체 discard한다.
  - Apply/undo는 현재 기본 경로 또는 필수 roadmap 산출물이 아니다.
  - Apply/undo로 자동 전환하기 위한 임의의 성능 threshold를 정하지 않는다.
  - 향후 COW baseline profiling에서 route copy·allocation·GC가 실제 병목으로 입증된 경우에만 별도 apply/undo 실험과 변경 승인을 제안할 수 있다.
  - 재검토 시에는 apply/undo round-trip, fault injection, 동일 seed/operator trace의 step별 outcome·acceptance·current/best·adaptive state, cache-free verifier와 final canonical solution이 COW와 정확히 같아야 한다.
  - 정확성·재현성·관측 가능성 동등성 중 하나라도 실패하거나 성능 이득이 충분하지 않으면 COW를 계속 유지한다.
- **근거:**
  - 사용자 직접 결정.
  - COW는 committed/best 상태를 candidate mutation에서 격리하는 현재 설계의 안전 기준이며, 측정되지 않은 rollback 복잡성을 미리 기본 경로로 만들 필요가 없다.
- **영향 문서:**
  - `docs/master-design.md` §12 candidate state/cache/rollback, §15 `RM-4`·`RM-7`
  - `docs/master-design-open-questions.md` `Q-ALG-02`
  - `docs/master-design-sessions/22-algorithm-draft.md` §6
  - `docs/master-design-sessions/24-roadmap-draft.md` `RM-4`, `RM-7`
  - 향후 state strategy configuration, rollback/fault suite, profiling decision record와 `RM-4`·`RM-7`
- **남은 모호성:** 없음. 향후 apply/undo 제안은 이 결정을 묵시적으로 뒤집지 않고 새로운 profiling evidence와 별도 변경 승인을 요구한다.
- **상태:** `RESOLVED — KEEP_COW`

### Q-BENCH-02 — 반복형 병렬 탐색과 공식 benchmark 실행 계약

- **사용자 답변:**
  - “seed비교는 의미가 없어요.”
  - AWS Step Functions와 Lambda를 예로 들어, 여러 초기해를 사용하고 서로 다른 seed의 여러 worker를 병렬 실행한 뒤 가장 좋은 결과를 best solution으로 선택하고, 그 best solution을 다음 round의 공통 초기해로 하여 다시 여러 seed worker를 실행하는 방식을 요청.
  - Worker별 `maxSteps=1000`, 시간 `60초`는 동작 설명을 위한 예시.
  - “좋은 seed는 의미가 없습니다.”
  - 공식 benchmark 종료를 manifest에 고정된 round plan으로 두는 제안에 “네”.
  - 모든 선언 worker가 정상 종료·검증되어야 round를 완료하는 두 번째 완결성 방식에 “두번째로 진행합니다.”
- **해석한 결정:**
  - Seed는 장기적으로 좋은 값을 선별하거나 seed별 품질 회귀를 판정하는 대상이 아니라, 같은 warm start에서 서로 다른 탐색 경로를 만드는 run별 다양성 입력이다.
  - 첫 round는 `Q-ALG-01`의 verified `best_initial_solution + admitted diverse initial solutions`를 worker warm start로 배정할 수 있다.
  - 한 round는 복수의 독립 worker run으로 구성되며 각 worker는 서로 다른 derived seed와 같은 round execution contract를 사용한다.
  - 각 worker candidate는 집계 전에 독립 검증을 통과해야 한다.
  - Round 집계기는 검증된 실제 candidate들을 고객사에 bind된 exact comparator로 비교하고 단일 round champion, 즉 당시의 best solution을 선택한다.
  - 다음 round의 모든 worker는 이전 round champion을 공통 warm start로 사용하고 서로 다른 derived seed로 다시 탐색한다.
  - Seed별 no-worse 또는 seed별 품질 분포를 공식 regression hard gate로 사용하지 않는다. 공식 품질 비교 대상은 전체 multi-round 실행이 산출한 최종 verified champion이다.
  - 재현성을 위해 “좋은 seed”를 선택하지는 않지만, 각 worker의 실제 base/derived seed, derivation version, warm-start candidate, round/run ordinal과 결과 lineage는 기록한다.
  - Worker 완료 순서나 병렬 실행 순서는 champion 선택에 영향을 주지 않으며 stable comparator/tie-break를 사용한다.
  - 공식 benchmark는 manifest에 명시된 고정 round plan을 정확히 실행하고 마지막 round의 verified champion을 최종 best solution으로 사용한다.
  - 공식 품질 종료를 전체 wall-clock deadline 또는 “개선 없는 몇 round” 같은 result-dependent 조기 종료로 결정하지 않는다.
  - Manifest의 round 수, round별 worker 수, warm-start 배정, worker별 `maxSteps`와 watchdog은 calibration experiment 후 승인·versioning한다.
  - 공식 benchmark에서 manifest가 선언한 모든 worker는 정확한 `maxSteps`로 정상 종료하고 독립 검증을 통과해야 해당 round가 완료된다.
  - 실패한 worker는 같은 round/run identity, seed와 warm start로 재시도할 수 있지만, 선언된 worker 결과가 모두 완성되기 전에는 round champion을 확정하지 않는다.
  - Worker 하나라도 끝내 정상 완료·검증되지 않으면 해당 round와 전체 official benchmark는 `INCOMPLETE`다. 성공한 worker 일부만으로 다음 round를 시작하거나 공식 champion/baseline을 만들지 않는다.
  - 예시의 worker별 `1000 steps`, `60초`를 공식값으로 확정하지 않는다. 정상 품질 예산은 `maxSteps`, 시간은 worker의 병리적 실행을 중단하는 watchdog이라는 기존 계약을 유지한다.
  - Step Functions/Lambda는 논리 orchestration과 worker fan-out/fan-in을 설명하는 예시다. 특정 provider/product/deployment topology를 확정하지 않으며 `Q-INFRA-01`은 `DEFERRED`를 유지한다.
- **근거:**
  - 사용자 직접 설명.
  - 여러 seed의 목적은 seed 자체 평가가 아니라 탐색 다양성 확보이고, 각 round의 best를 다음 탐색의 warm start로 사용하는 업무 요구.
- **영향 문서:**
  - `docs/master-design.md` §11 portfolio/ALNS, §13 termination/reproducibility, §14 Win PoC benchmark
  - `docs/master-design-open-questions.md` `Q-BENCH-02`
  - `docs/master-design-sessions/16-win-poc-benchmark.md`
  - `docs/master-design-sessions/22-algorithm-draft.md`
  - `docs/master-design-sessions/23-result-benchmark-draft.md`
  - `docs/master-design-sessions/24-roadmap-draft.md`의 logical execution ports
  - 향후 multi-round execution manifest, worker/round lineage, aggregate card와 `RM-4`·`RM-6`
- **남은 모호성:**
  - Round 수, worker 수, worker별 `maxSteps`와 watchdog의 실제 수치는 실험 evidence가 필요하다.
  - 물리 worker 재시도 횟수·backoff·orchestration 제품은 논리 benchmark 의미와 분리하며 현재 인프라 범위에서 확정하지 않는다.
- **상태:** `OPEN — EXPERIMENT_REQUIRED`

## 3. 진행 상태

- 완료: `Q-NUM-01`, `Q-NUM-02`, `Q-NUM-03`, `Q-MTX-01`, `Q-MTX-02`, `Q-MTX-03`, `Q-TIME-01`, `Q-TIME-02`, `Q-TIME-03`, `Q-TIME-04`, `Q-IN-01`, `Q-IN-02`, `Q-BENCH-03`, `Q-COMP-01`, `Q-COMP-02`, `Q-REQ-01`, `Q-REQ-02`, `Q-OBJ-01`, `Q-OBJ-02`, `Q-OBJ-03`, `Q-RES-01`, `Q-RES-02`, `Q-BENCH-01`
- 추가 완료: `Q-ALG-02`
- 프로토콜 확정·실험 대기: `Q-ALG-01`, `Q-BENCH-02`
- 인터뷰 대상 26개 질문의 사용자 확인 완료. 실제 측정값이 없는 `Q-ALG-01`, `Q-BENCH-02`만 의도적으로 `OPEN — EXPERIMENT_REQUIRED`를 유지한다.
- 다음 작업: 별도 통합 세션에서 이 기록을 `master-design.md`, `master-design-open-questions.md`와 영향 문서에 반영한다. 이 인터뷰 세션에서는 해당 문서를 수정하지 않는다.
- `DEFERRED` 유지: `Q-INFRA-01`, `Q-VAR-01`
