# Master Design question register

```yaml
status: REVIEW
version: 2.0-review
last_updated: 2026-07-23
owner: 질문별 owner boundary
scope: Master Design의 28개 질문에 대한 상태, 결정, evidence와 gate의 단일 등록부
supersedes: version 1.0-review의 26 OPEN + 2 DEFERRED 상태
related_decisions:
  - master-design-sessions/29-open-question-interview.md
  - master-design-sessions/30-open-question-integration.md
source:
  - master-design-sessions/19-integration-plan.md
  - master-design-sessions/29-open-question-interview.md
```

## 1. 사용 규칙

이 파일은 [Master Design](master-design.md)이 참조하는 정확한 28개 `Q-*`의 단일 등록부다. 질문 문장과 ID는 세션 19를 보존하고, 결정은 [세션 29 사용자 인터뷰](master-design-sessions/29-open-question-interview.md)만을 권위 있는 답변 근거로 사용한다. [세션 30](master-design-sessions/30-open-question-integration.md)은 그 답변의 문서 반영 기록이다.

아래 `Evidence / owner` 열의 “세션 29”는 해당 문서에서 같은 `Q-*` ID를 제목으로 가진 절의 사용자 답변·해석·상태를 가리킨다.

- `RESOLVED`는 표의 exact decision이 현재 Master에 통합되었음을 뜻한다.
- `RESOLVED — KEEP_COW`는 `RESOLVED`의 명시적 하위 판정이며 COW가 현재 기본임을 강조한다.
- `OPEN — EXPERIMENT_REQUIRED`는 의미와 실험·승인 protocol은 확정되었지만 공식 수치가 없다는 뜻이다. Explicit experiment/test 값만 허용하고 hidden production default나 official baseline을 만들지 않는다.
- `DEFERRED`는 질문하거나 활성화하지 않는다. Resume evidence와 별도 승인이 있어야 한다.
- 결정의 구체 타입/API/wire schema는 표의 의미를 보존해야 하며 새 기본값을 만들 수 없다.
- 상태 변경은 approval record와 영향 문서, `related_decisions`, version/fingerprint를 같은 변경 단위에서 갱신한다.

## 2. 질문·결정 등록부

| ID | 상태 | 질문 | 통합 결정 | Evidence / owner | Gate 결과 | Master 반영 |
|---|---|---|---|---|---|---|
| <a id="q-num-01"></a>`Q-NUM-01` | RESOLVED | 무게·부피·비용·거리·시간별 유지 소수 자릿수 `n`은 무엇인가? | 무게·부피 `n=3`; 비용·거리·시간은 정수 입력으로 `n=0` | 세션 29 사용자 답변 / Domain·Input | `RM-1` 의미 확정 | [§7.2](master-design.md#72-fixed-point와-checked-arithmetic) |
| <a id="q-num-02"></a>`Q-NUM-02` | RESOLVED | 각 물리량의 초과 자릿수 rounding mode는 무엇인가? | 비음수 무게·부피는 `FLOOR`; 비용·거리·시간 소수 입력은 변환 없이 거부 | 세션 29 사용자 답변 / Domain·Input | `RM-1` 경계값 검증 가능 | [§7.2](master-design.md#72-fixed-point와-checked-arithmetic) |
| <a id="q-num-03"></a>`Q-NUM-03` | RESOLVED | item 값을 먼저 정규화한 뒤 qty를 곱하는가, line 합계를 만든 뒤 정규화하는가? | Item을 먼저 `n=3/FLOOR` 정규화하고 정수 `qty`를 곱한 뒤 checked integer로 누적 | 세션 29 사용자 답변 / Domain·Input | `RM-1` demand 계약 확정 | [§7.2](master-design.md#72-fixed-point와-checked-arithmetic) |
| <a id="q-mtx-01"></a>`Q-MTX-01` | RESOLVED | legacy `D`와 `U`의 공식 의미·단위·허용 정밀도는 각각 무엇인가? | `D`는 directed meter, `U`는 directed second이며 둘 다 정수만 허용. 현재 소수 fixture는 비준수 | 세션 29 승인 권장안 / Input·Matrix | 정수 matrix 전에는 fixture official `RM-6` 차단 | [§8](master-design.md#8-directed-distancetime-matrix-계약) |
| <a id="q-mtx-02"></a>`Q-MTX-02` | RESOLVED | `C=O/G`와 diagonal `D=9999, U=0`의 공식 의미는 무엇인가? | `C`는 비권위; self arc는 모두 `0m/0s`; 누락 `D`는 Great Circle, 누락 `U`는 vehicle speed 또는 45km/h로 생성 | 세션 29 사용자 답변 / Input·Matrix | `RM-1` preparation 계약 활성 | [§8](master-design.md#8-directed-distancetime-matrix-계약) |
| <a id="q-mtx-03"></a>`Q-MTX-03` | RESOLVED | canonical production matrix는 항상 complete directed `M²`인가? | 외부 sparse/omitted 허용; preparation이 solver 전에 complete `M²`를 만든다. 생성 `D`는 integer meter `HALF_UP`, 생성 `U`는 vehicle별 `CEILING(D×3.6/speed)` second | 세션 29 승인 권장안 / Input·Matrix | `RM-1` provided/generated provenance·coverage 검증 | [§8](master-design.md#8-directed-distancetime-matrix-계약) |
| <a id="q-time-01"></a>`Q-TIME-01` | RESOLVED | 계획 시간대와 canonical date-time 형식은 무엇인가? | Backend가 timezone 처리 후 exact `yyyy-MM-dd HH:mm:ss` local string 전달; adapter가 planning-origin `long` second로 변환; core는 timezone/DST를 다루지 않음 | 세션 29 사용자 답변 / Backend·Input | `RM-1` solver time boundary 확정 | [§7.3](master-design.md#73-planning-period와-time) |
| <a id="q-time-02"></a>`Q-TIME-02` | RESOLVED | plan end와 각 time-window close는 포함 경계인가 제외 경계인가? | Plan은 `[start,end)`, window close는 포함 | 세션 29 사용자 답변 / Domain | `RM-1` boundary test 확정 | [§7.3](master-design.md#73-planning-period와-time) |
| <a id="q-time-03"></a>`Q-TIME-03` | RESOLVED | 고객 창은 서비스 시작만 제한하는가, 완료까지 제한하는가? 입력 profile별 정책을 허용하는가? | Early arrival 허용·waiting 기록; default `START_ONLY`; profile `COMPLETE_WITHIN_WINDOW` 가능; close 포함 | 세션 29 설명·예시 / Product·Domain | `RM-1` propagation/profile 검증 | [§7.3](master-design.md#73-planning-period와-time) |
| <a id="q-time-04"></a>`Q-TIME-04` | RESOLVED | 반복 일간 창, overnight 창, 차량 근무 종료를 넘는 이동의 의미는 무엇인가? | 날짜 없는 창은 일별 반복, `open>close`는 overnight. Arc가 근무창에 전부 들어갈 때만 출발하며 아니면 다음 work start에 처음부터 시작; arc 중간 pause/resume 금지 | 세션 29 승인·예시 / Product·Domain | `RM-1` multi-day propagation 확정 | [§7.3](master-design.md#73-planning-period와-time) |
| <a id="q-in-01"></a>`Q-IN-01` | RESOLVED | `reqDate`와 `dueDate`, `duration`과 주문 수준 `taskTime`의 정확한 의미·관계는 무엇인가? | `reqDate/dueDate`는 완료기한 별칭. `duration`은 request 고정시간, item `taskTime`은 단위당 시간이며 `serviceTime=duration+Σ(taskTime×qty)`. Order-level `taskTime`은 거부 | 세션 29 사용자 답변 / Input-contract | `RM-1` legacy mapping 확정 | [§7.3](master-design.md#73-planning-period와-time) |
| <a id="q-in-02"></a>`Q-IN-02` | RESOLVED | `depot.taskTime`, `multirotation`, `waitInDepot`, `maxStopCnt`, `maxDriveTime/Dist`의 적용 단위와 reset 경계는 무엇인가? | Depot `taskTime` 미적용; rotation 재출발에만 depot `duration`; oneway는 rotation 값 무시, 그 외 non-zero는 unsupported. Wait policy, location-transition stop, actual-arc drive resource는 route 전체 누적 | 세션 29 사용자 답변 / Input·Product | 현재 single-trip `RM-1` 확정; rotation은 후속 별도 승인 | [§6.2](master-design.md#62-route와-feasibility), [§7.3](master-design.md#73-planning-period와-time) |
| <a id="q-comp-01"></a>`Q-COMP-01` | RESOLVED | vehicle size 값의 null/empty/`ALL`, 코드 registry, unknown code 정책은 무엇인가? | Vehicle은 구체 `vehicleFeature`; order는 `vehicleFeatureList`. Order만 exact `["ALL"]` 허용. 자유 형식 case-sensitive exact code이며 fixed registry 없음; legacy order array alias만 허용 | 세션 29 사용자 답변 / Input·Product | `RM-1` schema/compatibility 확정 | [§5.3](master-design.md#53-vehicle-size와-capability), [§7.5](master-design.md#75-static-compatibility) |
| <a id="q-comp-02"></a>`Q-COMP-02` | RESOLVED | size restriction과 zone이 중복 입력될 때 항상 AND인가? pickup과 delivery 제한이 다르면 교집합인가? | Size와 zone은 AND; pickup/delivery size는 교집합. Missing zone은 `ALL`; route의 `ALL` 제외 구체 zone 집합은 최대 1 | 세션 29 사용자 답변·예시 / Product·Domain | `RM-1` static+route hard constraint 확정 | [§5.3](master-design.md#53-vehicle-size와-capability), [§7.5](master-design.md#75-static-compatibility) |
| <a id="q-req-01"></a>`Q-REQ-01` | RESOLVED | delivery-only와 실제 pickup-delivery를 같은 route에 섞을 수 있는가? | 허용. Delivery-only 합은 initial load, 실제 pickup/delivery는 방문 순서에서 증감하며 모든 prefix가 capacity를 만족. 중간 재상차 없음 | 세션 29 사용자 결정 / Domain·Product | Mixed single-trip `RM-1`·`RM-3` 활성 | [§5.2](master-design.md#52-service-meaning), [§6](master-design.md#6-핵심-불변조건과-atomic-mutation) |
| <a id="q-req-02"></a>`Q-REQ-02` | RESOLVED | 표준 범위에서 multi-trip을 지원하는가? 지원한다면 pickup-delivery 쌍이 trip 경계를 넘을 수 있는가? | Oneway와 single roundtrip은 현재 지원. Multi-trip/rotation은 후속이며 활성화해도 pair는 trip 경계를 넘지 않음 | 세션 29 사용자 확인 / Domain·Product | 현재 `RM-1` single-trip 확정; rotation은 deferred feature gate | [§5.2](master-design.md#52-service-meaning), [§16.3](master-design.md#163-deferred-resume-criteria) |
| <a id="q-obj-01"></a>`Q-OBJ-01` | RESOLVED | solve 요청이 고객사 내 objective preset을 선택할 수 있는가? | 고객사에 등록·승인된 preset만 선택. 생략 시 exact customer default; 다른 고객/unknown/missing dependency fallback 금지. 고객마다 objective availability가 다를 수 있음 | 세션 29 사용자 답변 / Product·Policy | `RM-2` binding 계약 확정 | [§9.3](master-design.md#93-profile-binding과-lifecycle) |
| <a id="q-obj-02"></a>`Q-OBJ-02` | RESOLVED | mandatory order는 hard rule인가, 유한 penalty인가? 상위 objective인가? | 사용하는 preset에서 `mandatoryUnassignedCount`가 최상위 lexicographic objective. Hard constraint/finite penalty가 아니며 0 불가능 시 best partial 허용 | 세션 29 사용자 결정 / Product·Policy | `RM-2` comparator 확정 | [§9.3](master-design.md#93-profile-binding과-lifecycle) |
| <a id="q-obj-03"></a>`Q-OBJ-03` | RESOLVED | 외주·이월을 결과 분류로만 둘지 최적화 선택지로 둘지? | 입력 `DIRECT/LEASE` vehicle만 solver 자원. Optional outsourced vehicle volume objective는 customer preset에만 추가; 미배정의 후속 외주·이월은 운영자 프로세스이며 solver status 아님 | 세션 29 사용자 답변 / Product·Policy·Result | `RM-2` ownership objective와 solver scope 확정 | [§9.3](master-design.md#93-profile-binding과-lifecycle), [§10.2](master-design.md#102-finalization과-result) |
| <a id="q-alg-01"></a>`Q-ALG-01` | OPEN — EXPERIMENT_REQUIRED | farthest/deadline scorer, randomized start 수, top-K, light-search step budget의 공식 기본값은 무엇인가? | Best initial solution은 항상 warm-start에 포함하고 diverse set은 추가다. Scorer, randomized starts, `K`, light-search budget은 동일 versioned calibration/approval protocol로 정한다. **수치는 아직 없음** | 세션 29 승인 protocol / Algorithm·Benchmark | Explicit experiment/test config만 허용; official `RM-3` defaults와 `RM-6` 차단 | [§11.2](master-design.md#112-현재-범위의-initial-solution-portfolio) |
| <a id="q-alg-02"></a>`Q-ALG-02` | RESOLVED — KEEP_COW | apply/undo를 기본 경로로 전환할 측정 기준은 무엇인가? | COW가 현재 기본. Apply/undo는 COW 병목 evidence와 별도 실험·동등성·변경 승인 전에는 기본 경로/필수 roadmap이 아님 | 세션 29 사용자 결정 / Algorithm·Performance | `RM-4` COW 진행; `RM-7`은 profiling과 선택적 재제안만 | [§12.3](master-design.md#123-later-applyundo-gate) |
| <a id="q-res-01"></a>`Q-RES-01` | RESOLVED | `OUTSOURCED`와 `DEFERRED`를 확정하는 최소 정보는 무엇인가? | Solver outcome은 `ASSIGNED`/`UNASSIGNED`만 생성. `LEASE` route도 `ASSIGNED`; optional `vhclOwnTyp`은 missing/null/empty=`DIRECT`, exact `DIRECT/LEASE`만 허용 | 세션 29 사용자 결정 / Product·Result | `RM-1` ownership, `RM-5` two-state outcome 확정 | [§5.3](master-design.md#53-vehicle-size와-capability), [§10.2](master-design.md#102-finalization과-result) |
| <a id="q-res-02"></a>`Q-RES-02` | RESOLVED | 모든 미배정 요청에 최종 해 기준 exhaustive insertion audit를 수행하는가? | Static `PROVEN` 제외 모든 `UNASSIGNED`를 전수 audit. Feasible insertion 발견 시 자동 수정/재탐색 없이 `UNASSIGNED` 게시 가능하며 발견은 내부 record에만 보존 | 세션 29 사용자 승인 / Result·Verification | `RM-5` audit completeness/confidence gate 확정 | [§10.2](master-design.md#102-finalization과-result), [§14.1](master-design.md#141-publication-gate) |
| <a id="q-bench-01"></a>`Q-BENCH-01` | RESOLVED | `전체 시간`은 순수 주행시간인가, 대기·서비스를 포함한 route elapsed time인가? | Used routes의 `drive + customer wait + depot wait + service + inter-work-window rest` 합 | 세션 29 사용자 결정 / Benchmark·Product | `RM-6` fourth metric formula 확정 | [§14.3](master-design.md#143-win-poc-comparator) |
| <a id="q-bench-02"></a>`Q-BENCH-02` | OPEN — EXPERIMENT_REQUIRED | 공식 seeds, maxSteps, watchdog, champion/seed별 regression gate는 무엇인가? | Fixed round plan, independent workers, verified round champion을 다음 round 공통 warm start로 사용. 모든 선언 worker 정상 완료·검증 필요; seed별 gate 없음. Round/worker 수, `maxSteps`, watchdog은 calibration 승인 후 결정. **수치는 아직 없음** | 세션 29 사용자 protocol / Benchmark·Quality | Logical fan-out/fan-in test 가능; official `RM-6` manifest/baseline 차단 | [§14.4](master-design.md#144-multi-round-official-execution) |
| <a id="q-bench-03"></a>`Q-BENCH-03` | RESOLVED | fixture의 `oneway + multiRotation=1`을 정확히 어떻게 해석하는가? | Oneway가 우선: depot 한 번 출발, 중간/최종 depot 복귀 없음, rotation 값은 비권위지만 raw provenance에는 보존 | 세션 29 사용자 결정 / Input·Product | `RM-1` adapter 의미 확정; integer matrix 전 `RM-6`는 별도 차단 | [§7.3](master-design.md#73-planning-period와-time), [§14.2](master-design.md#142-primary-fixture와-manifest) |
| <a id="q-infra-01"></a>`Q-INFRA-01` | DEFERRED | 실제 provider/product/deployment topology는 무엇인가? | 결정·질문·활성화하지 않음. 논리 port만 유지. 특정 orchestration/worker service는 fan-out/fan-in 예시일 뿐 | 세션 29에서 의도적 비질문 / Application·Platform·Product | `RM-9` resume evidence와 별도 승인 전 차단 없음 | [§4](master-design.md#4-논리-시스템-context와-책임-경계) |
| <a id="q-var-01"></a>`Q-VAR-01` | DEFERRED | MDVRP·OVRP·SDVRP 중 어떤 제한형을 언제 feasibility study할 것인가? | 결정·질문·활성화하지 않고 현재 pair/terminal/bank 계약 유지 | 세션 29에서 의도적 비질문 / Product·Domain·Algorithm | `RM-9` variant evidence와 별도 승인 전 차단 없음 | [§16.3](master-design.md#163-deferred-resume-criteria) |

## 3. 상태 요약

| 상태 | 수량 | 현재 의미 |
|---|---:|---|
| RESOLVED | 24 | `Q-ALG-02`의 `RESOLVED — KEEP_COW`를 포함하며 세션 29 exact decision을 Master에 통합 |
| OPEN — EXPERIMENT_REQUIRED | 2 | `Q-ALG-01`, `Q-BENCH-02`; protocol 확정, 공식 수치 미확정 |
| DEFERRED | 2 | `Q-INFRA-01`, `Q-VAR-01`; 질문·활성화 금지, resume evidence와 별도 승인 필요 |
| 합계 | 28 | 세션 19의 canonical 질문 전체 |

## 4. 남은 gate

- `Q-ALG-01`: scorer 공식, randomized start 수, diverse `K`, light-search work budget을 calibration corpus와 실제 측정 결과로 승인해야 한다.
- `Q-BENCH-02`: official round 수, round별 worker 수, warm-start 배정, worker별 `maxSteps`와 watchdog을 실험 결과로 승인해야 한다.
- 현재 Win fixture의 소수 `D/U`는 해결된 정수 계약에 비준수이므로 compliant integer matrix 없이는 official baseline을 만들 수 없다.
- `Q-INFRA-01`, `Q-VAR-01`은 계속 `DEFERRED`이며 위 실험 항목과 연결해 활성화하지 않는다.
