# 세션 18 — 설계 문서 거버넌스와 기준 문서 계층

## 문서 목적

이 세션의 목적은 `master-design.md`를 **앞으로 무엇을, 어떤 구조와 순서로 구현할지 합의하는 개발 마스터 설계**로 재정의하고, 상세 문서와 근거 자료가 서로 다른 역할을 갖도록 문서 체계를 정하는 것이다.

현재 진행 범위는 문서 작업뿐이다. 이 세션의 결정은 소스 코드 작성, 패키지 이동, 빌드 변경, 배포 설정 변경 또는 실제 구현 착수를 의미하지 않는다. 마스터 설계의 Phase는 향후 구현 계획과 완료 조건을 정의하지만, 구현 완료 상태를 주장하지 않는다.

현재 `master-design.md`에는 다음 성격의 내용이 함께 들어 있다.

- PDF를 Markdown으로 옮긴 과정에 대한 설명
- 시스템 전체의 규범적 설계 결정
- 도메인·시간·수치 계산의 상세 설계
- ALNS 연산자와 단계별 구현 계획
- 과거 AWS/ECS 기반 운영 구조
- 문서 작성자의 종합 평가와 반복 요약

개정 후에는 마스터 문서를 의사결정과 구현 계획의 중심으로 유지하되, 상세 계산과 근거는 하위 문서로 위임한다. 독자가 마스터 문서만 읽어도 범위, 목표 구조, 확장 지점, 구현 순서, 검증 기준을 이해할 수 있어야 하며, 구체적인 계약이 필요할 때 해당 상세 문서로 이동할 수 있어야 한다.

이 세션 문서는 `master-design.md`를 직접 개정하지 않는다. 18개 세션의 결과를 통합할 때 적용할 문서 관리 규칙과 개정 구조만 제안한다.

## 독자

| 독자 | 마스터 문서에서 확인할 내용 | 상세 문서에서 확인할 내용 |
|---|---|---|
| 제품·업무 책임자 | 해결 범위, 고객사별 변형을 수용하는 방식, 미배정과 목적함수의 정책 지점 | 입력 계약, 결과 의미, 고객사별 정책 명세 |
| 기술 책임자·아키텍트 | 시스템 경계, 목표 GCP 구조, source-of-truth, 확장 원칙, Phase 의존성 | 도메인·알고리즘·배포 상세 계약과 결정 기록 |
| 솔버 개발자 | 코어 불변조건, 변환·정책·제약 seam, 구현 순서와 완료 조건 | 타입 불변조건, 평가 계약, ALNS 상태 전이와 연산자 계약 |
| 플랫폼 개발자 | Cloud Run·Workflows·Cloud Storage의 책임 경계, 재시도·멱등성 원칙 | 요청 처리, 병렬 실행, 저장 경로, 장애 처리 상세 |
| QA·성능 담당자 | 품질 속성, Phase 게이트, 기준 벤치마크와 비교 순서 | 검증기 계약, fixture, seed, 측정 방식, 회귀 기준 |
| 미래 유지보수자 | 결정의 이유, 폐기된 대안, 변경 영향 범위 | Decision Record와 구현 진행 기록 |

마스터 문서는 특정 구현 클래스의 모든 필드나 알고리즘 수식을 나열하는 API 레퍼런스가 아니다. 반대로 비전 문서에 머물러서도 안 된다. 향후 구현자가 임의로 중요한 의미를 결정하지 않아도 될 정도의 규범적 경계와 완료 조건을 제공해야 한다.

## 규범 수준

### 문서 상태

설계 문서는 다음 상태 중 하나를 명시한다.

| 상태 | 의미 |
|---|---|
| `DRAFT` | 논의 중이며 구현 기준으로 사용할 수 없음 |
| `REVIEW` | 주요 결정이 작성되어 검토 중이지만 미확정 질문이 남을 수 있음 |
| `APPROVED` | 해당 버전이 향후 구현의 기준임 |
| `SUPERSEDED` | 더 새로운 문서 또는 결정으로 대체됨 |
| `ARCHIVED` | 근거·이력 보존용이며 현재 설계 효력이 없음 |

`master-design.md`는 18개 세션의 확정 사항과 미확정 질문을 반영한 뒤 `REVIEW` 상태로 올리고, 남은 필수 질문이 해결된 시점에만 `APPROVED`로 변경한다.

### 규범 문구

개정 문서에서는 문장의 강도를 구분한다.

- **필수(MUST)**: 구현과 검증이 반드시 따라야 하는 계약 또는 불변조건
- **권장(SHOULD)**: 특별한 근거가 있을 때만 이탈할 수 있는 기본 설계
- **선택(MAY)**: 호환 가능한 선택지 또는 후속 확장점
- **미결정(TBD, `Q-xxx`)**: 답을 임의로 가정하지 않고 질문 기록으로 연결한 사항

설명, 예시, 논문 요약은 규범 문구로 해석하지 않는다. 예시가 규칙과 충돌하면 규칙이 우선하며, 예시 자체도 수정 대상에 올린다.

### source-of-truth 우선순위

같은 주제에 충돌이 있을 때 다음 순서로 판단한다.

1. 외부 입력·출력 계약으로 채택된 명세와 승인된 Decision Record
2. `APPROVED` 상태의 `master-design.md`
3. `APPROVED` 상태의 Domain·Algorithm·Deployment·Benchmark 상세 설계
4. 18개 `master-design-sessions` 검토 문서와 중앙 질문 기록
5. `docs/arranged`의 통합 연구 문서
6. `docs/orgin`의 원본 요약과 PDF 스캔 해설·이관 메모
7. 구현 진행 기록과 개인 메모

다만 승인된 Decision Record가 기존 마스터 조항을 변경하는 경우에는 해당 Record가 그 조항만 한시적으로 대체한다. 같은 문서 변경 단위에서 마스터와 관련 상세 문서에도 반영하고 Decision Record를 `INCORPORATED`로 표시하여 장기간 이중 기준이 생기지 않게 한다.

현재 사용자가 직접 확정한 사항은 세션 문서에 보존하지만, 세션 문서 전체가 자동으로 규범 문서가 되는 것은 아니다. 통합 시 확정 사항을 마스터 또는 Decision Record에 옮기고, 모호한 사항은 질문으로 유지한다.

## 문서 계층

권장하는 목표 문서 구조는 다음과 같다.

```text
docs/
├─ README.md                         # 전체 문서 지도와 읽기 순서
├─ master-design.md                  # 시스템 전체의 규범적 개발 마스터 설계
├─ domain-design.md                  # 모델, 불변조건, 시간·수치·평가 계약
├─ algorithm-design.md               # 초기해 포트폴리오, ALNS, 로컬서치, 종료·롤백
├─ deployment-design.md              # GCP 실행·저장·재시도·멱등성·관측성
├─ benchmark-design.md               # win_poc_case 기준 지표·검증·회귀 방식
├─ input-contract.md                 # 채택한 JSON 입력 의미와 변환 계약
├─ open-questions.md                 # 아직 답하지 않은 질문의 단일 목록
├─ decisions/
│  └─ DR-xxx-*.md                    # 결정, 대안, 근거, 영향, 대체 관계
├─ implementation/
│  └─ progress.md                    # 향후 구현 착수 후 Phase별 실제 진행 증거
├─ master-design-sessions/
│  └─ *.md                           # 이번 18개 주제별 검토·개정 제안
├─ arranged/                         # 연구·기존 문서를 주제별로 통합한 참고 자료
├─ orgin/                            # 원본 요약 보존; 현재 철자는 별도 결정 전 유지
└─ archive/
   └─ import-notes/                  # PDF 스캔 순서·변환 과정 등 이관 기록
```

### `master-design.md`

시스템 전체의 가장 높은 수준의 규범적 설계 문서다. 다음 항목만 직접 소유한다.

- 프로젝트 목표, 범위와 명시적 비범위
- 현재 상태와 목표 상태의 구분
- RPDPTW 표준 모델을 선택한 이유와 시스템 경계
- 고객사별 요구를 최소 변경으로 수용하는 확장 원칙
- GCP 기준 전체 실행 구조와 구성요소 책임
- 모든 상세 설계가 지켜야 하는 핵심 불변조건
- Phase, 의존성, 산출물, 검증 가능한 완료 조건
- 주요 위험, 열린 질문과 상세 문서 링크

마스터에는 상세 타입 전체, 긴 의사코드, 연산자별 수식, PDF 페이지 설명을 중복해서 두지 않는다. 중요한 결정은 짧게 선언하고 상세 문서의 정확한 절을 참조한다.

### `domain-design.md`

비즈니스 입력에서 솔버 모델과 결과로 이어지는 의미를 소유한다.

- 비즈니스 모델과 계산 모델의 경계
- Request·Node·Vehicle·Route·Solution·RequestBank 불변조건
- 단위, 정밀도, 상한, 시간축과 Planning Horizon
- `RouteMetrics`, `ScorePolicy`, `RouteConstraint`, `RoutePropagator` 계약
- 고객사 요구의 확장 위치와 코어 변경 판단 기준
- 입력 검증, 결과 복원, 미배정 상태와 진단 의미

도메인 문서는 마스터 결정을 조용히 변경할 수 없다. 변경이 필요하면 먼저 Decision Record를 만들고 마스터의 영향을 명시한다. 현재 `domain-design.md`의 “기존 마스터 설계와의 차이”는 이 규칙에 따라 결정 기록으로 분해하거나 마스터에 흡수한다.

### `algorithm-design.md`

최적화 탐색의 상태와 절차를 소유한다.

- 초기해 포트폴리오와 후보 선택 방식
- ALNS destroy·repair·acceptance·adaptive selection 계약
- 요청 쌍의 원자적 제거·삽입과 해 불변조건 보존
- 고객사별 목적함수 조립과 단계 실행 방식
- step 중심 종료, 보호용 시간 제한, 난수와 재현성
- 부분 복사에서 apply/undo로 발전하는 롤백 전략
- local search, route elimination, 향후 route pool/MIP 경계

`docs/arranged/02_initial_solution_heuristics.md`, `03_alns_metaheuristic.md`, `05_local_search_moves.md`는 근거 자료로 유지하고, 실제 구현 계약은 이 문서에 선별하여 확정한다.

### `deployment-design.md`

현재 저장소와 목표 운영 환경에 맞춘 GCP 구조를 소유한다.

- Cloud Run API와 worker, Workflows 병렬 실행, Cloud Storage 산출물
- job·seed·candidate·final result의 식별과 상태 전이
- 재시도, 중복 실행, 멱등성, timeout, 실패 복구
- 설정, 비밀, IAM, 관측성, 배포와 롤백 경계
- 향후 GKE 전환 조건

기존 AWS/ECS·SQS·S3 설명은 역사적 배경으로만 보존하고 목표 구조에서는 제거한다. `docs/arranged/08_gcp_architecture.md`는 현재 근거이지만, 규범적 운영 계약은 새 Deployment Design으로 승격한다.

### `input-contract.md`와 `benchmark-design.md`

`data/ro_input_json_spec.pdf`는 기존 CVRPTW 입력 사례를 제공하는 외부 자료이고, 그 자체가 RPDPTW 내부 모델 설계서는 아니다. 채택한 필드 의미, 필수 여부, 단위, 행렬 규칙, 검증 오류는 `input-contract.md`에 명문화하고 PDF는 근거로 연결한다.

첫 벤치마크 기준은 `data/win_poc_case.json`으로 고정하고, 결과 비교 순서인 미배정 주문 수, 배차 차량 수, 전체 거리, 전체 시간을 `benchmark-design.md`에서 정확한 산식과 함께 정의한다. 이후 벤치마크를 추가해도 최초 회귀 기준의 의미가 바뀌지 않게 한다.

### Decision Record와 구현 진행 기록

Decision Record는 중요한 선택의 이유와 대안을 보존한다. 각 Record에는 ID, 상태, 날짜, 결정, 배경, 대안, 근거, 영향 문서, 마이그레이션 영향, 대체 관계를 둔다. 설계 문서 본문은 “무엇”을 말하고, Record는 “왜”를 보존한다.

`implementation/progress.md`는 향후 코드 구현이 실제로 시작된 뒤에만 사용한다. 계획을 완료 사실로 기록하지 않고, 각 Phase별로 다음 증거를 기록한다.

- 상태: `NOT_STARTED`, `IN_PROGRESS`, `BLOCKED`, `DONE`
- 적용된 설계 문서 버전과 Decision Record
- 실제 산출물과 검증 결과
- 설계와 달라진 점 및 후속 문서 변경 링크

진행 기록은 설계를 변경하는 문서가 아니며 source-of-truth가 될 수 없다.

### 기존 자료 정리 원칙

현재 문서의 PDF 스캔 순서 설명은 설계 내용이 아니므로 `archive/import-notes`로 이동한다. 실제 저장소에 존재하지 않는 `Scanned_20260722-2342.pdf`, `Scanned_20260722-2345.pdf`를 현재 근거처럼 참조하지 않는다. 원본을 보존해야 한다면 파일을 저장소에 추가하고 정확한 상대 링크와 보존 목적을 기록해야 한다.

`master-design.md`의 “전체 설계 흐름 요약”, “종합 평가”, “최종 요약”처럼 같은 결론을 반복하는 절은 하나의 “결정 요약과 다음 단계”로 합친다. 긴 주문 처리 예시는 Domain Design 또는 별도 예제로 이동하고, 마스터에는 경계 간 흐름만 남긴다. `domain-design.md`의 “최종 정리”도 새 결정을 만들지 않는 요약으로 축소한다.

현재 확인된 미존재·구식 참조는 개정 시 다음과 같이 처리한다.

| 현재 참조 | 처리 원칙 |
|---|---|
| `Scanned_20260722-2342.pdf`, `Scanned_20260722-2345.pdf` | 실재 원본이 추가되지 않으면 제거; 변환 이력은 archive에 기록 |
| `01_domain_design.md` | 실제 문서명인 `domain-design.md`로 연결 |
| `data/sample.json`, `data/cvrptw_sample.json` | 실제 기준인 `data/win_poc_case.json` 및 입력 명세로 교체; 미래 파일은 생성 전 참조하지 않음 |
| 루트 `PROGRESS.md` | 실제 구현 착수 시 `docs/implementation/progress.md`를 생성한 뒤 링크 |
| `worker-app`, SQS, S3 중심 설명 | GCP 목표 구조로 재작성하고 과거 구조는 필요 시 Decision Record 또는 archive에 보존 |

`docs/README.md`는 현재 arranged 문서만 소개하므로, 개정 완료 시 전체 문서 계층, 상태, 권장 읽기 순서와 source-of-truth 규칙을 보여 주는 인덱스로 다시 작성한다.

## 변경 관리

### 문서 메타데이터

모든 규범 문서 상단에 다음 정보를 둔다.

```yaml
status: DRAFT | REVIEW | APPROVED | SUPERSEDED | ARCHIVED
version: 문서 버전
last_updated: YYYY-MM-DD
owner: 문서 책임 역할
scope: 문서가 소유하는 결정 범위
supersedes: 대체한 문서 또는 버전
related_decisions: [DR-...]
```

Git 이력이 변경 내용을 보존하므로 본문에 긴 수정 이력을 반복하지 않는다. 호환성이나 의사결정 이유가 중요한 변경만 Decision Record로 남긴다.

### 변경 절차

1. 변경 요청이 어느 문서의 소유 범위인지 식별한다.
2. 기존 규범과 충돌하거나 여러 문서에 영향을 주는 선택이면 Decision Record 초안을 만든다.
3. 답이 필요한 모호성은 `open-questions.md`에 등록하고 관련 본문에 `TBD(Q-xxx)`를 표시한다.
4. 결정 후 같은 문서 변경 단위에서 Master, 관련 상세 문서, Decision Record, README 링크를 함께 갱신한다.
5. 용어, 단위, 타입 의미, Phase 번호, 링크, 상태가 서로 일치하는지 문서 검증을 수행한다.
6. 구현이 시작된 이후라면 진행 기록에 적용 문서 버전과 검증 증거를 추가한다.

단순 오탈자나 표현 개선은 Decision Record가 필요 없다. 다음 변경은 반드시 Decision Record를 남긴다.

- 문제 의미나 핵심 불변조건 변경
- 외부 입력·출력 계약 변경
- 목적함수·비교 순서 또는 고객사 확장 seam 변경
- 시간·수치 단위와 반올림 의미 변경
- GCP 구성요소 책임, 재시도, 멱등성 계약 변경
- Phase 범위나 완료 기준에 영향을 주는 구조 변경
- 기존 승인 결정을 폐기하거나 대체하는 변경

### 추적성

마스터의 핵심 요구와 결정에는 안정적인 ID를 부여한다.

- `G-xxx`: 목표
- `REQ-xxx`: 기능 요구
- `NFR-xxx`: 품질 속성
- `INV-xxx`: 반드시 유지할 불변조건
- `DR-xxx`: 결정 기록
- `Q-xxx`: 열린 질문

Phase 표에는 관련 요구 ID, 상세 설계 절, 산출물, 문서 검증 기준을 연결한다. 코드 파일이나 테스트 클래스는 향후 구현 문서에서만 연결하며, 현재 설계 단계에서는 존재하지 않는 산출물을 완료된 것처럼 링크하지 않는다.

## 질문 기록 방식

모호한 부분은 본문에서 임의로 확정하지 않고 중앙 `docs/open-questions.md`에 남긴다. 각 질문은 다음 형식을 따른다.

| 필드 | 의미 |
|---|---|
| ID | 안정적인 `Q-xxx` 식별자 |
| 상태 | `OPEN`, `ANSWERED`, `DEFERRED`, `SUPERSEDED` |
| 질문 | 한 가지 결정을 요구하는 구체적인 문장 |
| 배경 | 왜 지금 질문이 생겼는지 |
| 영향 | 답에 따라 달라지는 문서·Phase·외부 계약 |
| 선택지 | 이미 확인된 가능한 대안과 차이 |
| 임시 처리 | 질문을 건너뛰는 동안 문서가 취하는 처리; 기본값 확정이 아님 |
| 답변·근거 | 사용자의 답과 관련 자료 |
| 반영 위치 | 결정이 반영될 Master·상세 문서·Decision Record |

질문을 남긴 상태에서도 독립적인 문서 작업은 계속한다. 해당 결정 없이는 의미가 정해지지 않는 본문은 `TBD(Q-xxx)`로 표시하고 서로 다른 가정을 섞지 않는다. 향후 구현 Phase의 착수 또는 완료가 그 답에 의존하면 Phase 게이트에 질문 ID를 명시한다.

18개 세션 문서의 “남은 질문”은 다음 통합 순서를 따른다.

1. 중복 질문을 하나의 중앙 질문 ID로 합친다.
2. 이미 사용자가 답한 내용은 답변 원문과 해석을 분리해 기록한다.
3. 답변이 명확하면 Decision Record 또는 해당 규범 문서에 반영한다.
4. 일부만 답했거나 사례별 예외가 남으면 질문을 닫지 않고 범위를 좁힌 후 유지한다.
5. 보류한 질문은 `DEFERRED`로 두고 재검토할 Phase 또는 조건을 기록한다.

## master 개정 목차

개정 `master-design.md`는 다음 목차를 권장한다.

1. **문서 상태와 사용법**
   - 목적, 독자, 규범 수준, 버전, 관련 상세 문서
   - 현재 문서는 구현 결과가 아니라 향후 구현의 승인 기준임을 명시
2. **목표와 범위**
   - 프로젝트 목표, 성공 조건, 명시적 비범위
   - RPDPTW와 학술 PDPTW의 관계
3. **근거와 입력 자료**
   - 업무 요구, 입력 JSON 명세, `win_poc_case.json`, 논문·arranged 문서의 역할
4. **현재 상태와 목표 상태**
   - 현재 저장소 구조
   - 과거 AWS/ECS 구조는 배경으로만 요약
   - 목표 GCP 구조와 전환 범위
5. **전체 시스템 컨텍스트와 책임 경계**
   - Cloud Run API/worker, Workflows, Cloud Storage, 솔버 코어
   - 외부 I/O와 순수 계산 코어의 의존성 방향
6. **표준 문제 모델과 핵심 불변조건**
   - RPDPTW 공통 모델의 범위
   - 요청 쌍, 배정, 경로, 미배정 상태의 불변조건
   - 상세 Domain Design 참조
7. **변화 수용 아키텍처**
   - Transformer, compatibility rule, RouteConstraint, RoutePropagator
   - RouteMetrics, ScorePolicy, Objective/SolvePlan의 역할
   - 새 요구를 어느 seam에 추가할지 판단 규칙
8. **입력·정규화·이동 행렬 계약**
   - `ro_input_json_spec.pdf`에서 채택한 계약
   - 단위·시간·정밀도·상한의 개요
   - 실제 거리·시간 행렬 입력 원칙
9. **목적함수와 해 비교 구조**
   - 고객사별 조립 가능 구조
   - 결과 비교와 단계 실행 계약
   - 정책과 물리적 측정의 분리
10. **알고리즘 구조**
    - 초기해 포트폴리오
    - ALNS, local search, fleet reduction, 롤백
    - step 기본 종료, 보호용 시간 제한, 재현성
11. **미배정·외주·이월 결과 모델**
    - 탐색 상태와 최종 업무 상태 분리
    - 사유와 진단의 신뢰 수준
12. **GCP 실행·운영 구조**
    - 병렬 seed 실행, 후보 집계, 결과 저장
    - 재시도, 멱등성, timeout, 관측성
    - 상세 Deployment Design 참조
13. **검증과 벤치마크**
    - 독립 Solution Verifier
    - `win_poc_case.json` 기준 비교 순서와 산식
    - 결정성, 캐시 일관성, 정책 격리, 회귀 기준
14. **구현 Phase와 의존성**
    - 문서 산출물과 향후 코드 산출물 구분
    - 각 Phase의 입력, 산출물, 완료 조건, 선행 질문
    - route pool/MIP와 변형 문제의 후속 단계
15. **위험, 마이그레이션과 롤백**
    - 설계·구현 drift, 성능, 시간 경계, GCP 전환 위험
16. **열린 질문과 결정 기록 색인**
    - 중앙 질문과 Decision Record 링크
17. **용어집과 추적성 표**
    - RPDPTW·PDPTW, Feature·Capability 등 표준 용어
    - 요구 → 상세 설계 → Phase → 검증 기준 연결

PDF 스캔 설명, 긴 예제, 논문별 해설, 반복적인 종합 평가와 최종 요약은 이 목차의 본문에 두지 않는다. 필요한 자료는 archive, arranged, 상세 설계 또는 예제 문서로 이동하고 마스터에서 링크한다.

## 남은 질문

1. `docs/orgin` 디렉터리명은 원본 보존을 위해 그대로 유지할 것인가, 아니면 링크를 일괄 갱신하면서 `docs/original`로 바로잡을 것인가? 내용의 규범 수준에는 영향이 없지만 문서 구조의 장기 가독성에 영향을 준다.
2. 외부 입력 계약의 최종 기준은 `data/ro_input_json_spec.pdf` 자체인가, PDF를 해석해 작성할 `docs/input-contract.md`인가? 권장안은 PDF를 근거 자료로 보존하고 승인된 Markdown 계약을 구현 기준으로 사용하는 것이다.
3. `master-design.md`와 상세 문서의 승인 책임자는 개인 이름으로 둘 것인가, 역할(예: Product Owner, Solver Architect, Platform Owner)로 둘 것인가?
4. 18개 세션 통합이 끝난 뒤 기존 `master-design.md`와 `domain-design.md`를 같은 변경에서 전면 개정할 것인가, 먼저 Master를 승인한 다음 상세 문서를 순차 개정할 것인가? 권장안은 충돌을 막기 위해 Master와 직접 충돌하는 Domain 절, README, 질문 색인을 한 변경 단위에서 함께 정합화하는 것이다.
5. PDF 스캔 이관 설명을 별도 archive 파일로 보존해야 할 감사·추적 요구가 있는가? 없다면 Git 이력에만 남기고 현재 설계 문서에서는 제거해도 된다.
6. 향후 구현 진행 기록을 저장소 안의 `docs/implementation/progress.md`로 관리할 것인가, 아니면 GitHub Issues/Projects 같은 외부 작업 관리 도구를 기준으로 하고 문서에는 링크만 둘 것인가? 어느 경우에도 진행 기록은 설계의 source-of-truth로 사용하지 않는다.
