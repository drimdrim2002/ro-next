# 세션 19 — Master Design 통합 계획

> 상태: `REVIEW INPUT`  
> 성격: 세션 20~26의 문서 작성·통합·검토를 통제하는 계획  
> 변경 범위: 이 문서만 작성한다. `docs/master-design.md`는 이 세션에서 수정하지 않는다.  
> 기준일: 2026-07-23

## 1. 목적과 적용 원칙

이 문서는 세션 01~18의 결과와 현재 `docs/master-design.md`를 하나의 규범적 개발 설계로 통합하기 위한 작업 기준이다. 이 문서 자체가 새로운 Master Design은 아니며, 코드·배포·제품 상태를 변경하거나 구현 완료를 주장하지 않는다.

통합 시 근거의 우선순위는 다음과 같다.

1. 세션 19에 전달된 최신 사용자 결정
2. 사용자 결정과 충돌하지 않는 세션 01~18의 명시적 확정 사항
3. 현재 입력 자료에서 직접 확인한 사실
4. 세션 문서의 권장안과 의사 인터페이스
5. `docs/arranged`와 `docs/orgin`의 연구·원본 자료
6. 현재 `docs/master-design.md`의 기존 서술

세션 문서에 `권장`, `임시안`, `예시`, `후속 검토`로 적힌 내용은 확정 결정으로 승격하지 않는다. 상충하는 제안에 충분한 근거가 없으면 열린 질문으로 유지한다.

이 계획에서 사용하는 상태는 다음 세 가지다.

| 상태 | 의미 | Master 반영 방식 |
|---|---|---|
| **확정** | 최신 사용자 결정 또는 명시적 확정 사항이며 상충하는 더 높은 근거가 없음 | 규범 문장으로 작성 |
| **잠정** | 구조상 유력한 권장안이지만 이름·기본값·세부 계약이 승인되지 않음 | `SHOULD` 또는 대안이 보이는 초안으로 작성 |
| **열린 질문** | 답에 따라 외부 의미나 구현 계약이 달라짐 | 임의 기본값 없이 `TBD(Q-...)`로 기록 |

`DEFERRED`는 네 번째 결정 상태가 아니라 열린 질문 또는 범위 항목의 처리 방식이다. 재검토 조건이 올 때까지 현재 Master의 구현 범위에서 제외한다.

## 2. 검토한 기준 자료

다음 자료를 끝까지 읽고 통합 기준으로 사용했다.

- [세션 인덱스](README.md)
- [세션 01](01-gcp-architecture.md)부터 [세션 18](18-document-governance.md)까지의 전 문서
- [현재 Master Design](../master-design.md)
- [세션 19~26 스케줄러 로그](19-26-scheduler-log.md)
- [문서 인덱스](../README.md)와 [원본 반영 커버리지 맵](../arranged/coverage_map.md)

추적성 확인에는 다음 원본·정리 자료를 사용했다.

- [문제 정의](../arranged/01_problem_definition.md): CVRPTW, PDPTW, feasibility, 목적 비교
- [초기해 휴리스틱](../arranged/02_initial_solution_heuristics.md): 포트폴리오, 공통 evaluator, route elimination, route pool/MIP
- [ALNS](../arranged/03_alns_metaheuristic.md): destroy/repair, SA, adaptive weight, 종료
- [Local Search](../arranged/05_local_search_moves.md): 요청 이동에 적용할 move 근거
- [실무 확장](../arranged/06_practical_extensions.md): compatibility, optional customer, 외주·이월
- [논문·벤치마크](../arranged/07_papers_and_benchmarks.md): PDPTW ALNS 근거와 해석 한계
- [PDPTW 원본 요약](../orgin/alns_pdptw_paper_summary_ko.md): pickup-delivery pairing, same vehicle, precedence, request 단위 제거·삽입
- [기존 provider-specific 배포 정리](../arranged/08_gcp_architecture.md): 현재는 역사적 근거로만 사용

입력 자료는 수정하지 않고 존재와 기준값만 확인했다.

- `data/ro_input_json_spec.pdf`: 기존 CVRPTW 입력 사례, 92,458 bytes
- `data/win_poc_case.json`: 14,157,512 bytes, SHA-256 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`

PDF는 RPDPTW의 신규 규범 명세가 아니다. `win_poc_case.json`도 정답 해를 포함하지 않으며, 입력 fixture와 최초 회귀 기준을 만드는 데이터다.

## 3. 결정 분류

### 3.1 확정 사항

| ID | 확정 결정 | 규범적 의미 | 근거 |
|---|---|---|---|
| `C-01` | 표준 용어는 **RPDPTW**다. | 최초 등장에는 `Rich Pickup and Delivery Problem with Time Windows`를 병기한다. `RPDPDTW`, `rpdpdtw`, `Rpdpdtw`는 신규 설계에서 사용하지 않는다. 학술 문제 `PDPTW`는 그대로 보존한다. | 세션 02 |
| `C-02` | Master는 향후 구현 방법을 정하는 규범적 개발 설계다. | 현재 구현 완료 보고서, PDF 이관 기록, 논문 강의 노트로 쓰지 않는다. 확정·잠정·미결정과 현재·목표 상태를 구분한다. | 세션 18, 최신 사용자 결정 |
| `C-03` | 고객사마다 목적함수와 제약이 다르며, 최소 코어 변경이 최우선이다. | 고객사 추가가 ALNS, 경로 상태, 공통 평가 흐름의 고객사 분기 추가를 요구해서는 안 된다. 새 물리 의미가 필요한 경우에만 좁은 공통 seam을 확장한다. | 세션 04, 05, 14 |
| `C-04` | 실행 가능성, 중립 측정, 비용, 목적 비교, 단계 실행을 분리한다. | 정적 호환성은 변환 데이터, 경로 hard rule은 제약, 진행 의미는 propagator, 가격은 score policy, 비교 순서는 objective/solve plan의 책임이다. | 세션 04, 05, 14 |
| `C-05` | 입력의 `Feature`는 차량 크기 유형이다. | 1t·3t·5t 등 차량 크기 유형을 지역의 대형 차량 진입 제한에 사용한다. 주문 값은 허용 유형 집합, 차량 값은 단일 유형이다. 냉장·리프트·자격 등 capability/qualification은 별도 확장 제약이다. | 세션 05, 세션 16 |
| `C-06` | 요청은 원자적인 pickup-delivery 쌍이다. | 배정 요청은 같은 차량에 pickup과 delivery가 각각 정확히 한 번 존재하고 pickup이 먼저다. 미배정 요청은 두 노드가 어느 경로에도 없고 검색 bank에 요청 ID가 정확히 한 번 있다. 삽입·제거·이동·교환·실패 복구도 요청 단위다. | 세션 03, 06, PDPTW 원본 |
| `C-07` | Delivery-only CVRPTW와 실제 pickup-delivery의 의미를 구분한다. | 하나의 RPDPTW 코어를 사용하되, delivery-only의 출발 적재를 고객 방문 사이의 재적재로 해석하지 않는다. 표준 단일 회차에서는 중간 depot 재적재를 허용하지 않는다. | 세션 03, 11 |
| `C-08` | 정상 종료는 step 제한, 시간 제한은 watchdog이다. | 정상 품질 예산과 재현성 기준은 `maxSteps`다. 시간은 루프·병리적 장기 실행을 막는 보조 안전장치이며 정상 결과 비교 예산으로 사용하지 않는다. | 세션 07 |
| `C-09` | 후보 상태 전략은 단계적으로 진화한다. | 초기 안전 구현은 변경 경로 copy-on-write와 독립 SearchRequestBank를 사용한다. 후속 apply/undo는 같은 외부 계약과 검증 동등성을 유지하며 도입한다. 커밋된 current/best를 임의로 직접 변경하지 않는다. | 세션 08, 최신 사용자 결정 |
| `C-10` | 소수 물리량은 변환 경계에서 고정소수점 정수로 정규화한다. | 코어 feasibility와 누적에는 `double`/`EPS`를 사용하지 않는다. 같은 차원의 수요와 용량은 같은 단위·scale·rounding 정책을 공유한다. | 세션 09 |
| `C-11` | 정확한 소수 자릿수와 rounding은 아직 확정하지 않았다. | `n=3`, 절삭, `HALF_UP` 등을 예시에서 규범으로 승격하지 않는다. 정확한 `n` 또는 rounding mode가 명확하지 않으면 열린 질문으로 남긴다. | 세션 09, 최신 사용자 결정 |
| `C-12` | 부피 기본값은 999 CBM이고 time sentinel/상한은 plan end time이다. | 최신 지시의 “time sentinel”은 계획에서 허용되는 유한한 최댓값을 뜻하며 infeasible 표시가 아니다. 실패는 별도 상태로 표현한다. tunable 값은 의미별 Constants/configuration에서 관리하고 정책 버전·출처를 보존한다. | 세션 10, 최신 사용자 결정 |
| `C-13` | 실제 거리·시간 행렬이 이동 계산의 입력이다. | 입력된 방향별 거리와 시간을 권위 값으로 사용한다. 좌표·Haversine·속도로 조용히 재생성하거나 누락 arc를 묵시적으로 채우지 않는다. solver node와 물리 location을 분리한다. | 세션 09, 12, 최신 사용자 결정 |
| `C-14` | `ro_input_json_spec.pdf`는 선행 CVRPTW 입력 사례다. | 표와 예시가 충돌하므로 신규 canonical RPDPTW 계약으로 그대로 복제하지 않는다. legacy adapter 근거와 미결정 의미를 구분한다. | 세션 11, 12, 최신 사용자 결정 |
| `C-15` | 탐색 bank와 최종 결과·진단을 분리한다. | `SearchRequestBank`는 검색 중 멤버십만 가진다. 최종 assignment status와 unassignment diagnostic은 종료 후 결과 계층이 만든다. 삽입 실패 한 번을 확정 사유로 기록하지 않는다. | 세션 13 |
| `C-16` | 초기해 포트폴리오는 현재 구현 범위다. | 공통 pair insertion evaluator 위에 `SEQ_FARTHEST`, `SEQ_EARLIEST_DEADLINE`, `PAR_REGRET_2`, `RAND_REGRET_3`의 최소 포트폴리오, bounded light improvement, 검증·중복 제거·best/diverse 선택을 둔다. | 세션 15, arranged/02 |
| `C-17` | route pool과 MIP 후처리는 현재 범위가 아니다. | 현재 설계에는 확장 경계와 진입 조건만 남기고 구현 Phase와 완료 조건은 후속 승인으로 미룬다. | 세션 15, 최신 사용자 결정 |
| `C-18` | 1차 benchmark fixture는 `data/win_poc_case.json`이다. | `WIN_POC` 전용 비교 순서는 **미배정 주문 수 → 배차 차량 수 → 전체 거리 → 전체 시간**의 사전식 순서다. 이 순서는 모든 고객사의 전역 목적이 아니다. | 세션 16, 최신 사용자 결정 |
| `C-19` | 선택 변형 문제 구현은 보류한다. | MDVRP·OVRP·SDVRP는 현재 구현 항목이 아니라 후속 feasibility study다. 요청 원자성이나 terminal 의미를 Transformer 하나로 바꿀 수 있다고 미리 단정하지 않는다. | 세션 17, 최신 사용자 결정 |
| `C-20` | 구체 infrastructure/product/deployment topology는 보류한다. | Master는 submission, execution, artifact/status storage, result retrieval 등 논리 포트와 책임 경계만 정의한다. 특정 cloud, product, runtime topology, IAM, bucket, queue, job 종류를 목표 설계로 고정하지 않는다. | 최신 사용자 결정; 세션 01·18의 provider-specific 안을 대체 |
| `C-21` | 검증되지 않은 후보는 비교 대상이 아니다. | 독립 verifier가 요청 파티션, pair 불변조건, 호환성, 시간·용량·경로 자원, 거리·시간 재계산을 통과한 해만 최종 후보와 benchmark에 사용한다. | 세션 06, 12, 13, 16 |
| `C-22` | 강한 재현성은 조건이 고정된 정상 step 종료에 한정한다. | 입력·정책·행렬·설정·build·seed·step과 tie-break가 같고 `MAX_STEPS_REACHED`인 실행을 기준으로 한다. watchdog/platform timeout 결과는 같은 품질 예산으로 간주하지 않는다. | 세션 07, 16 |

### 3.2 잠정 사항

다음은 설계 방향을 설명하는 유력한 안이지만, 정확한 타입명·기본값·외부 스키마로 확정하지 않는다.

| ID | 잠정안 | 확정하지 않는 부분 | 후속 소유 세션 |
|---|---|---|---|
| `P-01` | `DELIVERY_ONLY`와 `PICKUP_DELIVERY` service pattern을 둔다. | enum 이름, 가상 pickup을 route prefix 노드로 둘지 초기 적재 상태로 둘지 | 20 |
| `P-02` | delivery-only v1은 가상 pickup을 route 시작 prefix에 고정한다. | depot 작업시간의 적용 단위, 외부 결과 노출 방식, mixed request route 세부 규칙 | 20 |
| `P-03` | 물리 location 기준 directed dense matrix와 node-to-location mapping을 사용한다. | production이 항상 `M²` complete matrix인지, sparse 계약을 별도 지원할지 | 20 |
| `P-04` | `SolvePlan`을 단계 orchestration의 상위 이름으로 사용하고 `ObjectiveSchema/Score`를 평가 벡터로 둔다. | 최종 Java 이름과 별도 `ObjectivePlan` 타입 필요 여부 | 21 |
| `P-05` | profile registry/provider가 설정 팩토리 역할을 하고 solve별 bound policy를 만든다. | 정확한 provider API, lifecycle 구현, 등록 방식 | 21 |
| `P-06` | `PropagationFacts`, `MetricContributor`, `MetricSnapshot`으로 고객사별 중립 측정값을 확장한다. | 객체/배열 표현과 공통 `RouteMetrics`에 승격할 지표 목록 | 21 |
| `P-07` | 선행 objective는 기본적으로 `NO_WORSE_THAN_BEST`로 보호한다. | 고객사별 tolerance 또는 탐색 중 relaxation 허용 여부 | 21 |
| `P-08` | 최종 상태는 `ASSIGNED`, `UNASSIGNED`, `DEFERRED`, `OUTSOURCED`를 구분한다. | 외주·이월의 최소 확정 정보와 첫 구현 활성 범위 | 23 |
| `P-09` | 진단은 code, scope, confidence, source, evidence로 구조화한다. | 공개 code 목록, 고객사 확장 코드 노출, 최종 삽입 감사 범위 | 23 |
| `P-10` | 초기해 후보는 best와 제한된 diverse top-K를 제공한다. | K, quality band, randomized start 수, diverse 후보의 ALNS 배정 방식 | 22 |
| `P-11` | apply/undo 전환에는 프로파일링과 copy-on-write trace 동등성 gate를 둔다. | 전환 임계치, 캐시 복구 방식, 기본 경로 전환 시점 | 22·24 |
| `P-12` | legacy reader는 제한된 숫자 문자열·별칭을 버전된 adapter에서 처리한다. | 허용 별칭, canonical JSON 형태, unknown field 정책 | 20 |
| `P-13` | benchmark는 fixture checksum과 adapter/policy/solver fingerprint를 가진 manifest/card를 사용한다. | 공식 seed, maxSteps, watchdog, 성능 gate 값 | 23 |
| `P-14` | 실행 경계는 논리 포트로 분리한다. | 실제 transport, orchestrator, artifact store, deployment unit | 24 이후 별도 인프라 결정 |

### 3.3 열린 질문

아래 질문은 중복을 합친 중앙 질문 후보다. 세션 20~24는 같은 질문을 새 ID로 복제하지 않고 이 ID를 사용해야 한다. 질문이 답해지지 않아도 Master 작성은 계속하되 관련 본문은 `TBD(Q-...)`로 남긴다.

| ID | 질문 | 영향 | 처리 |
|---|---|---|---|
| `Q-NUM-01` | 무게·부피·비용·거리·시간별 유지 소수 자릿수 `n`은 무엇인가? | 입력 정규화, overflow, 결과 표시, benchmark | OPEN |
| `Q-NUM-02` | 각 물리량의 초과 자릿수 rounding mode는 무엇인가? | 용량 경계, 행렬 합계, 재현성 | OPEN |
| `Q-NUM-03` | item 값을 먼저 정규화한 뒤 qty를 곱하는가, line 합계를 만든 뒤 정규화하는가? | 주문 수요와 용량 feasibility | OPEN |
| `Q-MTX-01` | legacy `D`와 `U`의 공식 의미·단위·허용 정밀도는 각각 무엇인가? | 거리·시간 비교, benchmark 숫자 | OPEN |
| `Q-MTX-02` | `C=O/G`와 diagonal `D=9999, U=0`의 공식 의미는 무엇인가? | provenance, self arc 정규화 | OPEN |
| `Q-MTX-03` | canonical production matrix는 항상 complete directed `M²`인가? | 입력 validation과 저장 구조 | OPEN |
| `Q-TIME-01` | 계획 시간대와 canonical date-time 형식은 무엇인가? | planning period, DST, fingerprint | OPEN |
| `Q-TIME-02` | plan end와 각 time-window close는 포함 경계인가 제외 경계인가? | 모든 시간 feasibility | OPEN |
| `Q-TIME-03` | 고객 창은 서비스 시작만 제한하는가, 완료까지 제한하는가? 입력 profile별 정책을 허용하는가? | transformer와 time-window 정규화 | OPEN |
| `Q-TIME-04` | 반복 일간 창, overnight 창, 차량 근무 종료를 넘는 이동의 의미는 무엇인가? | RoutePropagator | OPEN |
| `Q-IN-01` | `reqDate`와 `dueDate`, `duration`과 주문 수준 `taskTime`의 정확한 의미·관계는 무엇인가? | legacy 입력 변환 | OPEN |
| `Q-IN-02` | `depot.taskTime`, `multirotation`, `waitInDepot`, `maxStopCnt`, `maxDriveTime/Dist`의 적용 단위와 reset 경계는 무엇인가? | delivery-only, multi-trip, route resource | OPEN |
| `Q-COMP-01` | vehicle size 값의 null/empty/`ALL`, 코드 registry, unknown code 정책은 무엇인가? | static compatibility | OPEN |
| `Q-COMP-02` | size restriction과 zone이 중복 입력될 때 항상 AND인가? pickup과 delivery 제한이 다르면 교집합인가? | `servableVehicles` 생성 | OPEN |
| `Q-REQ-01` | delivery-only와 실제 pickup-delivery를 같은 route에 섞을 수 있는가? | 적재 전파와 prefix 의미 | OPEN |
| `Q-REQ-02` | 표준 범위에서 multi-trip을 지원하는가? 지원한다면 pickup-delivery 쌍이 trip 경계를 넘을 수 있는가? | 요청 불변조건, route model | OPEN 또는 DEFERRED |
| `Q-OBJ-01` | solve 요청이 고객사 내 objective preset을 선택할 수 있는가? | profile selection, API, result metadata | OPEN |
| `Q-OBJ-02` | mandatory order는 hard rule인가, 유한 penalty인가? 상위 objective인가? | feasibility와 scoring | OPEN |
| `Q-OBJ-03` | 외주·이월을 결과 분류로만 둘지 최적화 선택지로 둘지? | solution state와 objective | OPEN |
| `Q-ALG-01` | farthest/deadline scorer, randomized start 수, top-K, light-search step budget의 공식 기본값은 무엇인가? | 초기해 재현성 | OPEN |
| `Q-ALG-02` | apply/undo를 기본 경로로 전환할 측정 기준은 무엇인가? | 성능 Phase gate | OPEN |
| `Q-RES-01` | `OUTSOURCED`와 `DEFERRED`를 확정하는 최소 정보는 무엇인가? | 최종 결과 validation | OPEN |
| `Q-RES-02` | 모든 미배정 요청에 최종 해 기준 exhaustive insertion audit를 수행하는가? | 진단 신뢰도와 종료 후 비용 | OPEN |
| `Q-BENCH-01` | `전체 시간`은 순수 주행시간인가, 대기·서비스를 포함한 route elapsed time인가? | `WIN_POC` 네 번째 지표 | OPEN |
| `Q-BENCH-02` | 공식 seeds, maxSteps, watchdog, champion/seed별 regression gate는 무엇인가? | baseline card | OPEN |
| `Q-BENCH-03` | fixture의 `oneway + multiRotation=1`을 정확히 어떻게 해석하는가? | feasibility와 지표 | OPEN |
| `Q-INFRA-01` | 실제 provider/product/deployment topology는 무엇인가? | 배포 상세 문서 | **DEFERRED**; Master 구현 범위를 막지 않음 |
| `Q-VAR-01` | MDVRP·OVRP·SDVRP 중 어떤 제한형을 언제 feasibility study할 것인가? | 후속 roadmap | **DEFERRED** |

## 4. 중복·충돌 조정

### 4.1 조정 결과

| 주제 | 중복 또는 충돌 | 통합 판정 | 상태·근거 |
|---|---|---|---|
| 약어 | 현재 Master의 `RPDPDTW` 대 세션 02의 `RPDPTW` | 전부 `RPDPTW`로 교체하고 학술 `PDPTW`는 유지 | 확정 `C-01` |
| 문서 성격 | PDF 상세 해설·현행 설명·구현 계획이 혼재 | 규범적 미래 개발 설계로 재작성하고 이관 과정·반복 요약은 제거 | 확정 `C-02` |
| 목표 topology | 현재 Master의 AWS 경로, 세션 01·18의 GCP 목표안, 최신 cloud-neutral 결정 | 특정 provider 목표안은 모두 비규범적 역사로 내린다. 논리 포트와 책임만 Master에 둔다. | 확정 `C-20`; 최신 결정이 대체 |
| 물리 모듈 | 현재 Master의 `solver-core/worker-app` 멀티모듈 확정 | 논리적 core/application/adapter 의존 방향만 규범화한다. 실제 module/image 분리는 고정하지 않는다. | `C-20`, `P-14` |
| 표준 모델 | “모든 입력을 같은 pickup 의미로 변환”하는 현재 표현 | RPDPTW 코어는 하나지만 delivery-only와 실제 pickup 의미를 구분한다. | `C-07`, 세션 03 |
| delivery-only | 주문별 depot pickup이 route 중간에 자유 삽입될 수 있는 현재 예시 | 출발 적재 이후 고객 사이 재적재를 금지한다. 표현 방식은 잠정이다. | `C-07`, `P-01~02` |
| 요청 구조 | precedence만 검사하는 현재 삽입 표 | same vehicle, exactly once, pair completeness, route/bank XOR, atomic success/failure를 선행 불변조건으로 둔다. | `C-06` |
| `Feature` | generic vehicle capability 교집합과 size code가 혼재 | vehicle size membership으로 확정하고 capability subset은 별도 축으로 분리 | `C-05` |
| compatibility | zone·feature·전용 차량 로직이 코어와 profile에 혼재 | 정적 규칙은 변환 시 `servableVehicles`로 합성하고, 경로 상태가 필요한 규칙만 `RouteConstraint`에 둔다. | `C-03~05` |
| 숫자 단위 | 현재 Master의 g/L 고정 예시와 세션 09의 kg/cbm 외부 계약 | 외부 의미는 kg/cbm을 유지하고 내부는 정책 기반 정수다. 정확한 `n`·rounding은 질문이다. | `C-10~11`, `Q-NUM-*` |
| 부피 무제약 | `Long.MAX_VALUE`와 999 CBM | 부피 차원 미사용 시 999 CBM 유한 기본값을 적용하고 provenance를 남긴다. | `C-12` |
| 시간 실패 | `Long.MAX_VALUE` sentinel과 plan end 상한 | `planEndTime`은 실제 상한, 실패는 구조화된 infeasible 상태다. | `C-12` |
| travel | 좌표/Haversine·reference speed 생성과 입력 matrix가 충돌 | 실제 입력 matrix가 권위 값이다. 좌표 기반 생성은 명시적 외부 전처리 없이는 사용하지 않는다. | `C-13` |
| matrix layout | solver node 수 정방행렬과 물리 location 행렬 | node는 `locationIndex`를 참조하고 matrix는 물리 location을 기준으로 한다. | `C-13`, `P-03` |
| time contract | 현재 Master의 start-only, daily repeat, pause/resume, `reqDate` release 해석과 PDF 문구가 충돌 | 어느 쪽도 전역 기본값으로 확정하지 않는다. profile/input contract 질문으로 분리한다. | `Q-TIME-*`, `Q-IN-*` |
| 목적 순서 | 현재 Master의 고정 `미배정→차량→총비용`과 고객사별 목표 요구 | 코어는 목적 순서를 고정하지 않는다. 고객별 `SolvePlan`이 정하고 `WIN_POC`만 확정된 전용 순서를 가진다. | `C-03~04`, `C-18` |
| Big-M | arranged 자료의 scalar 예와 Master의 사전식 방식 | hard priority는 사전식 stage로 표현한다. 같은 우선순위 내 교환 가능한 비용만 합성한다. | 세션 04 |
| `SolvePlan`/`ObjectivePlan` | 세션 04와 14의 이름·책임이 겹침 | 상위 실행 orchestration은 `SolvePlan`, 평가 vector는 `ObjectiveSchema/Score`로 설명한다. 최종 타입명은 잠정이다. | `P-04` |
| metrics 확장 | `RouteMetrics`에 모든 필드를 추가하는 방식과 contributor 방식 | 공통 물리 사실만 `RouteMetrics`; 선택적 중립 사실은 contributor/snapshot seam으로 둔다. | 세션 14, `P-06` |
| RequestBank | 현재 Master의 검색 상태+최종 사유 혼합, 세션 03·06의 일반 이름, 세션 13의 분리 | 검색 상태는 `SearchRequestBank`, 최종 status/diagnostic은 결과 계층으로 분리 | `C-15` |
| dummy 의미 | arranged 자료의 dummy 하나가 미배송·외주·이월을 겸함 | 최종 업무 상태를 명시적으로 구분하고 dummy는 외부 상태로 노출하지 않는다. | 세션 13, `P-08~09` |
| 종료 | iteration과 wall time 중 먼저 도달하는 동등 조건 | step은 정상 종료, time은 watchdog으로 역할을 분리 | `C-08` |
| 후보 복사 | 현재 Master의 deep-copy 금지+초기 undo-log와 세션 08의 COW 우선 | 초기 COW, 후속 apply/undo의 단계적 경로로 통합 | `C-09`, `P-11` |
| 초기해 | 현재 Master의 single greedy와 arranged/세션 15의 portfolio | 최소 4개 portfolio와 공통 evaluator를 현재 범위로 확정 | `C-16` |
| route pool/MIP | arranged/02의 초기해 산출물 및 MVP 순서와 세션 15의 defer | 현재 범위에서는 `InitialSolutionSet`까지만 구현하고 route pool/MIP는 후속으로 이관 | `C-17` |
| benchmark | Solomon 우선·존재하지 않는 sample 대 `win_poc_case.json` | Win PoC를 1차 end-to-end 기준으로 사용하고 학술 benchmark는 후속 milestone로 둔다. | `C-18`, 세션 16 |
| benchmark 시간 | 세션 16의 잠정 drive time과 최신 “전체 시간” 표현 | 네 번째 성분의 우선순위·이름은 확정하되 산식은 `Q-BENCH-01`로 유지 | `C-18`, 열린 질문 |
| variants | 현재 Master의 Phase 16 구현과 세션 17의 study gate | 구현이 아니라 deferred feasibility study로만 기록 | `C-19` |
| 검증 | solver cache·출력 집계를 신뢰하는 흐름과 독립 verifier 요구 | 최종 후보와 benchmark는 독립 전량 검증을 통과해야 함 | `C-21` |

### 4.2 삭제·축소·보존 규칙

최종 Master 통합 시 다음 규칙을 적용한다.

- 삭제: PDF 스캔 페이지 순서 설명, 존재하지 않는 파일을 현재 근거로 든 부분, 반복적인 “종합 평가/최종 요약”.
- 교체: 구 약어, generic Feature 설명, 자유 depot pickup, 동등한 time budget, `Long.MAX_VALUE` sentinel, Haversine 생성, single greedy, 혼합 RequestBank, provider-specific 목표 topology.
- 축소: 클래스 목록과 긴 의사코드는 책임·불변조건·완료 기준만 남긴다.
- 보존: ALNS의 기본 흐름, feasible-first 정확성 원칙, 중립 metrics와 policy 분리, seed 기반 결정성은 최신 계약에 맞게 다시 쓴다.
- 링크: 상세 근거는 세션 문서와 이후 Domain/Policy/Algorithm/Benchmark 상세 초안으로 연결한다.

## 5. 최종 `docs/master-design.md` 목표 목차

세션 25는 다음 목차를 기준으로 Master를 전면 통합한다. 장 번호는 세션 25에서 최종 고정하되 아래 순서와 소유 경계는 유지한다.

1. **문서 상태와 규범**
   - 목적, 독자, 상태, 버전, `MUST/SHOULD/MAY/TBD`
   - 미래 구현 설계이며 구현 완료 주장이 아님을 명시
2. **목표, 범위, 성공 기준**
   - RPDPTW 목표
   - 고객사별 변화에 대한 최소 코어 변경
   - 현재 범위와 명시적 비범위
3. **용어와 설계 결정 요약**
   - RPDPTW/PDPTW
   - vehicle size type 대 capability
   - 확정·잠정·deferred 결정 색인
4. **논리 시스템 컨텍스트와 책임 경계**
   - submit/input, solve execution, candidate/finalization, artifact/status, result retrieval의 논리 포트
   - adapter/application/domain 의존 방향
   - provider/product/deployment topology는 `TBD/DEFERRED`
5. **RPDPTW 표준 문제 모델**
   - request, pickup/delivery, vehicle, terminal, route, physical location
   - delivery-only와 실제 pickup-delivery의 의미
6. **핵심 불변조건**
   - same vehicle, exactly once, precedence, route/bank XOR, atomic mutation
   - terminal, 용량, 시간, 경로 자원
7. **입력·정규화 계약**
   - legacy PDF의 지위
   - canonical adapter 경계, planning period, fixed point, 999 CBM, plan end
   - 명확하지 않은 시간·rounding 규칙은 질문 링크
8. **거리·시간 데이터 계약**
   - authoritative directed matrix input
   - physical location mapping, validation, provenance
   - 묵시적 fallback 금지
9. **변화 수용 아키텍처**
   - Transformer/static compatibility
   - RouteConstraint/RoutePropagator
   - neutral metrics/contributors
   - ScorePolicy, Objective, SolvePlan, profile binding
10. **해 상태와 결과 모델**
    - `SearchRequestBank`
    - final assignment status
    - unassignment diagnostics와 confidence
11. **알고리즘 파이프라인**
    - initial-solution portfolio
    - ALNS destroy/repair/SA/adaptive selection
    - light local search와 fleet reduction
    - route pool/MIP 후속 경계
12. **후보 상태, 캐시, 롤백**
    - initial copy-on-write
    - apply/undo 전환 계약
    - cache invalidation과 full recomputation equivalence
13. **종료, 결정성, 실행 메타데이터**
    - step 정상 종료
    - watchdog
    - seed derivation, tie-break, fingerprints
    - 논리적 실행·재시도·멱등 책임; 제품 topology 없음
14. **검증과 `WIN_POC` benchmark**
    - independent verifier
    - fixture identity
    - 사전식 metric order
    - baseline/regression card와 미결정 산식
15. **구현 roadmap과 Phase gates**
    - 문서 → domain/evaluation → portfolio/ALNS → result/verifier → benchmark → 성능
    - route pool/MIP와 variants는 deferred gate
16. **위험, 마이그레이션, 비범위**
    - 의미 오해, 수치 경계, 캐시, 재현성, 입력 호환
    - provider-specific 설계와 optional variants의 재개 조건
17. **열린 질문과 추적성**
    - `docs/master-design-open-questions.md` 링크
    - requirement/decision → 상세 초안 → Phase → verification map

## 6. 현재 Master 컨텍스트 맵

세션 20~25는 현재 Master를 다음과 같이 채굴하되, 기존 문장을 권위 있는 결정으로 간주하지 않는다.

| 현재 Master 범위 | 주요 내용 | 처리 | 새 목차 | 초안 소유 |
|---|---|---|---|---|
| §1~2 | 문서 소개와 PDF 스캔 설명 | §1은 규범 문서 소개로 재작성, §2 삭제 | 1~2 | 24·25 |
| §3~5 | 구 약어, 문제 정의, 목표, 통합 원칙 | 용어 수정, delivery-only 의미와 범위 보강 | 2~6 | 20·24 |
| §6~10 | 현재 구현, AWS/모듈/패키지/API | 구체 topology 제거, 논리 경계만 남김 | 4 | 24 |
| §11~17 | node/request/vehicle, 수치, 시간, 전파 | 최신 불변조건·입력 질문으로 전면 재작성 | 5~8 | 20 |
| §18~23 | metrics, 삽입, 변환, RequestBank, policy/profile | domain, policy, 결과 책임으로 분리 | 6~10 | 20·21·23 |
| §24~25 | Big-M, 고정 다단계 목적 | 고객별 evaluation/SolvePlan과 benchmark profile 분리 | 9 | 21 |
| §26~28 | ALNS, 종료, 성능 | portfolio·step/watchdog·COW→apply/undo로 교체 | 11~13 | 22 |
| §29~30 | I/O, worker, 테스트 | 논리 포트와 독립 verifier 중심으로 재작성 | 4, 13~14 | 23·24 |
| §31~35 | 16 Phase, milestone, 위험, 비범위, Q&A | 새 의존 roadmap·중앙 질문·deferred gate로 재작성 | 15~17 | 24 |
| §36~39 | 긴 예시와 반복 요약 | 삭제하거나 짧은 boundary flow로 흡수 | 필요 시 4 또는 부록 | 25 |

## 7. 세션 20~26의 정확한 컨텍스트와 편집 경계

### 7.1 공통 불변 경계

1. 세션 20~24는 `docs/master-design.md`를 **절대로 수정하지 않는다**.
2. 세션 25만 `docs/master-design.md`를 작성·수정할 수 있다.
3. 세션 26은 완성된 Master를 **검토만** 하며 `docs/master-design.md`를 수정하지 않는다.
4. 모든 세션은 문서 작업만 수행한다. source, build, deployment, data, fixture, PDF, test code를 수정하지 않는다.
5. 각 draft 세션은 자기 출력 파일만 수정하고, 앞선 세션 파일을 고치지 않는다.
6. 미결정을 임시 기본값으로 닫지 않는다. 동일한 질문은 이 문서의 `Q-*` ID를 재사용한다.

### 7.2 세션별 작업 지도

| 세션 | 유일한 기본 출력·편집 허용 | 반드시 읽을 컨텍스트 | 소유하는 새 Master 내용 | 명시적 비범위 | 완료 기준 |
|---:|---|---|---|---|---|
| **20** | `docs/master-design-sessions/20-domain-input-draft.md` | 이 계획; 세션 02, 03, 05, 06, 09~13, 17; 현재 Master §3, §11~23; arranged/01; PDPTW 원본; PDF와 fixture는 읽기 전용 | 용어, 표준 request/vehicle/location 모델, delivery-only 의미, pair 불변조건, planning/numeric/constants, matrix input, SearchRequestBank의 검색 측 | 목적 조립, ALNS 절차, benchmark gate, topology | `C-01`, `C-05~15`를 모두 다루고 `Q-NUM/MTX/TIME/IN/COMP/REQ`를 임의로 닫지 않음 |
| **21** | `docs/master-design-sessions/21-policy-objective-draft.md` | 이 계획; 세션 04, 05, 13, 14, 16; 세션 20 draft; 현재 Master §18, §22~25; arranged/01·06 | extensibility decision tree, neutral facts, metrics contributor, constraint/score/objective 분리, profile binding, SolvePlan, benchmark comparator와 고객 comparator의 분리 | input parser 세부, ALNS operator, 결과 DTO 전체, topology | 새 고객 정책 추가 시 예상 변경 범위를 표로 보이고 `SolvePlan/ObjectivePlan` 중복을 `P-04`대로 조정 |
| **22** | `docs/master-design-sessions/22-algorithm-draft.md` | 이 계획; 세션 06~08, 15; 세션 20~21 drafts; 현재 Master §26~28; arranged/02·03·05·07 | portfolio 4종, common pair insertion evaluator, light improvement, ALNS, local/fleet moves, step/watchdog, COW→apply/undo, cache/rollback, route pool/MIP 경계 | 외부 result schema, benchmark 수치, provider 실행 | 요청 원자성·재현성·rollback 후조건과 모든 algorithm Phase gate가 연결되고 route pool/MIP는 deferred |
| **23** | `docs/master-design-sessions/23-result-benchmark-draft.md` | 이 계획; 세션 07, 10, 12, 13, 16; 세션 20~22 drafts; current Master §21, §29~30, §32; fixture 읽기 전용 | final assignment status, diagnostics, independent verifier, result metadata, `WIN_POC` fixture/metric order/baseline card/regression | objective engine 내부, algorithm 구현, topology | `C-15`, `C-18`, `C-21~22` 반영; `Q-BENCH-01` 산식을 발명하지 않음 |
| **24** | `docs/master-design-sessions/24-roadmap-draft.md` | 이 계획; 세션 17~18과 20~23 drafts; current Master §1~10, §31~39; scheduler contract | Master 문서 메타·범위, 논리 시스템 경계, 전체 Phase 의존성·산출물·완료 gate, 위험, migration, deferred resume criteria, 통합 traceability table | provider/product topology, 코드 구현 상태 주장, 새 domain/algorithm 결정 | roadmap가 20~23의 산출물을 순서대로 소비하고 portfolio는 current, route pool/MIP·variants·topology는 deferred로 표시 |
| **25** | `docs/master-design.md`, `docs/master-design-open-questions.md`; 인덱스 정합에 꼭 필요할 때만 `docs/README.md` | 이 계획; 세션 20~24의 모든 draft; 세션 01~18은 충돌·추적 확인용; 현재 Master 전체 | §5 목표 목차에 따라 확정 사항 통합, 잠정 표지, 중앙 질문 파일, source/session 링크, 중복 제거 | 코드·설정·data 변경; 열린 질문의 임의 결정; 상세 문서의 무단 생성 | 금지 약어 0건, provider-specific 목표 topology 0건, `C-*` 누락 0건, 모든 미결정에 `Q-*` 연결, 문서 상태 `REVIEW` |
| **26** | `docs/master-design-sessions/26-master-review.md`만 | 이 계획; 세션 20~25 산출물; 완성 Master와 질문 파일; 01~18은 표본 추적 검증 | 수정 없는 독립 review report: 누락·모순·근거·링크·범위·용어·질문 상태·readiness 판정 | `docs/master-design.md`, 질문 파일, README 또는 다른 파일 수정 | 발견 사항을 severity와 정확한 절로 기록한다. 문제가 있어도 Master를 고치지 않고 후속 수정 세션을 요구한다. |

### 7.3 세션 간 handoff 계약

```text
19 integration plan
  → 20 domain/input draft
  → 21 policy/objective draft
  → 22 algorithm draft
  → 23 result/benchmark draft
  → 24 roadmap/context draft
  → 25 Master + open questions integration
  → 26 review-only report
```

- 세션 20은 의미와 불변조건의 용어를 고정하되 열린 입력 의미를 닫지 않는다.
- 세션 21은 세션 20의 중립 facts를 소비하고 물리 의미를 다시 계산하지 않는다.
- 세션 22는 세션 20의 pair mutation과 세션 21의 comparator 계약을 소비한다.
- 세션 23은 세션 20의 최종 변환 경계, 세션 21의 metric vector, 세션 22의 termination metadata를 소비한다.
- 세션 24는 20~23의 결정을 재해석하지 않고 Phase·경계·gate로 배열한다.
- 세션 25는 draft 간 표현을 통일하되 새로운 결정을 만들지 않는다.
- 세션 26은 수정 권한 없는 reviewer다.

## 8. 세션·근거 추적성 지도

| 세션 | Master에 전달할 핵심 | 원자료 연결 | 최종 draft 소비자 |
|---:|---|---|---|
| 01 | algorithm core와 external execution adapter의 분리, 논리적 병렬 run·idempotency 관심사 | arranged/08 및 당시 저장소 조사 | 24; provider-specific 내용은 superseded |
| 02 | RPDPTW/PDPTW 명명 | 현재 Master 금지 표기 조사 | 20, 25, 26 |
| 03 | delivery-only 대 실제 pickup-delivery, 시작 적재 의미 | arranged/01, PDPTW 원본 | 20, 22 |
| 04 | customer-specific score/objective/SolvePlan, Big-M 회피 | arranged/01·06 | 21 |
| 05 | size type membership, capability subset, compatibility seam | fixture, arranged/01·06 | 20, 21 |
| 06 | pair 불변조건과 atomic operator contract | PDPTW 원본, arranged/01 | 20, 22, 23 |
| 07 | maxSteps 정상 종료, watchdog, reproducibility | current Master §27, 실행 경계 조사 | 22, 23, 24 |
| 08 | COW 격리, commit/discard, apply/undo, cache | current Master §28 | 22 |
| 09 | fixed point, units, overflow, unresolved rounding | PDF, fixture | 20, 23 |
| 10 | 999 CBM, plan end, no numeric failure sentinel | current/domain 설계 | 20, 23 |
| 11 | legacy PDF 필드·시간 불일치와 canonical adapter 경계 | `ro_input_json_spec.pdf` | 20 |
| 12 | authoritative directed matrix, location mapping, no fallback | PDF, fixture | 20, 23 |
| 13 | SearchRequestBank 대 final status/diagnostics | arranged/06 | 20, 23 |
| 14 | neutral facts, metric contributors, composite policy, bound profile | current/domain 설계 | 21 |
| 15 | 4-policy initial portfolio, common evaluator, light improvement, MIP defer | arranged/02·03·05 | 22, 24 |
| 16 | fixture identity, lexicographic metric order, verifier, card | `win_poc_case.json` | 21, 23 |
| 17 | variants feasibility gate와 core-change 판단 | 세션 03·06·12 기반 | 20, 24 |
| 18 | normative document governance, source hierarchy, target outline | 전체 docs 조사 | 24, 25, 26 |

## 9. 통합 품질 gate

### 9.1 세션 25 작성 전

- 20~24의 다섯 draft가 모두 존재하고 자기 범위 밖 결정을 만들지 않았는지 확인한다.
- 같은 질문이 서로 다른 기본값으로 작성되지 않았는지 `Q-*` 기준으로 확인한다.
- 각 draft가 확정·잠정·열린 질문을 구분했는지 확인한다.
- `docs/master-design.md`의 변경이 아직 0건인지 확인한다.

### 9.2 세션 25 작성 후

다음 검사를 문서 수준에서 수행한다.

```text
금지 표기: RPDPDTW | rpdpdtw | Rpdpdtw
provider-specific 목표 topology 또는 배포 완료 주장
정확한 근거 없는 n, rounding mode, matrix unit, time boundary
Long.MAX_VALUE를 도메인 값 또는 실패 sentinel로 사용
Haversine/속도 기반 묵시적 travel fallback
RequestBank에 최종 사유 저장
single greedy만을 초기해 범위로 기술
route pool/MIP 또는 optional variants를 현재 구현 범위로 기술
time limit를 정상 품질 예산으로 기술
```

추가로 다음 긍정 검사를 수행한다.

- `RPDPTW`와 학술 `PDPTW`가 구분되어 있다.
- size type과 capability가 구분되어 있다.
- 요청 pair 불변조건 네 가지와 atomic handling이 명시되어 있다.
- step/watchdog 역할이 명시되어 있다.
- initial COW와 후속 apply/undo가 연결되어 있다.
- 999 CBM, plan-end time sentinel/상한, 별도 explicit infeasible state가 명시되어 있다.
- 실제 input matrix와 physical location mapping이 명시되어 있다.
- SearchRequestBank와 final outcome/diagnostic이 분리되어 있다.
- portfolio가 current scope이고 route pool/MIP가 deferred다.
- Win PoC 비교 순서가 정확히 네 성분의 사전식 순서로 적혀 있다.
- optional variants와 provider topology가 deferred다.
- 각 열린 질문이 `docs/master-design-open-questions.md`의 안정적 ID와 연결된다.

### 9.3 세션 26 review 판정

세션 26은 다음 중 하나로만 판정한다.

| 판정 | 의미 |
|---|---|
| `READY_FOR_REVIEW` | 확정 결정과 범위가 모두 반영되고 열린 질문이 명시적으로 남아 있음 |
| `READY_WITH_NONBLOCKING_FINDINGS` | 규범 의미는 맞고 링크·표현 등 비차단 결함만 있음 |
| `REVISION_REQUIRED` | 확정 결정 누락, 숨은 가정, 범위 위반, 충돌 또는 추적성 결손이 있음 |

`REVISION_REQUIRED`여도 세션 26은 Master를 직접 수정하지 않는다.

## 10. 세션 19 완료 선언

이 계획이 완료되면 다음이 참이어야 한다.

- 확정·잠정·열린 질문이 근거와 함께 분리되어 있다.
- 세션 01~18과 현재 Master의 주요 중복·충돌에 해결 또는 열린 상태가 있다.
- 최종 Master의 목표 목차가 cloud/product neutral하게 정의되어 있다.
- 세션 20~26 각각의 입력, 출력, 소유 내용, 금지 내용, 완료 기준이 명시되어 있다.
- Master를 수정할 유일한 세션은 25이며, 26은 review-only임이 명시되어 있다.
- 이 세션에서는 `docs/master-design.md`와 다른 어떤 파일도 수정하지 않는다.
