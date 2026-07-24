# 세션 01 — GCP 운영 아키텍처 전환

## 확정 사항

1. `master-design.md`의 AWS ECS/SQS/S3 기반 운영 가정은 폐기하고, 앞으로의 목표 운영 구조는 GCP를 기준으로 작성한다.
2. 현재 저장소에 이미 존재하는 GCP 실행 골격을 설계의 출발점으로 삼는다.
   - Cloud Run API
   - Google Cloud Workflows
   - 비공개 Cloud Run worker
   - Cloud Storage
3. API와 worker는 현재 하나의 Maven 산출물과 하나의 컨테이너 이미지를 공유하고, 배포 시 `SERVICE_MODE=api|worker`로 역할을 나눈다. 이 내용은 **현재 상태**로 문서화한다.
4. API는 최적화 본문을 Workflows 상태에 싣지 않고 `gs://` 입력 URI와 실행 파라미터만 전달한다.
5. Workflows는 독립 seed의 ALNS batch를 병렬 실행하고, worker의 finalize 단계가 저장된 후보 가운데 최종 후보를 선택하여 결과를 저장한다.
6. 최적화 알고리즘은 GCP 실행 기술과 분리한다. HTTP, Workflows SDK, Cloud Storage SDK, 인증 관련 타입은 어댑터에만 존재하고, 향후 구현할 `application`/`domain` 계층은 이를 직접 참조하지 않는다.
7. 현재 `AlnsBatchEngine`은 배포 흐름을 확인하기 위한 결정적 placeholder이다. 이를 실제 RPDPTW 솔버가 이미 구현된 것으로 기술해서는 안 된다.
8. Cloud Run 요청 시간·메모리·CPU 또는 비용 한계를 실제 측정으로 확인한 경우, Workflows의 batch 실행 대상만 Cloud Run Job 또는 GKE Job으로 교체할 수 있어야 한다. 외부 API, 입력·결과 계약, seed별 병렬 구조와 순수 솔버 경계는 유지한다.
9. GCP HTTP 재시도가 가능하므로 batch 실행과 finalize는 요청 단위로 멱등이어야 한다. 후보 객체 키에는 최소한 `requestId`와 `runNumber`가 포함되어야 하며, 동일 실행을 재시도해도 논리적으로 같은 결과가 되어야 한다.
10. GCP 리소스는 서비스 계정별 최소 권한으로 분리한다.
    - API 서비스 계정: Workflow 실행 권한, 결과 조회에 필요한 읽기 권한
    - Workflow 서비스 계정: 비공개 worker 호출 권한
    - worker 서비스 계정: 입력 읽기 및 후보·결과 쓰기에 필요한 권한

## 근거

- `README.md:3-11`은 이 저장소를 Java 25/GCP 기반 시작점으로 정의하고, Cloud Run API/worker, Workflows, Cloud Storage의 역할을 명시한다.
- `README.md:25-38`은 API가 `gs://` URI를 받아 Workflow 실행을 시작하며, `AlnsBatchEngine`이 아직 대체 구현임을 명시한다.
- `pom.xml:7-11`에는 하나의 `ro-next` artifact만 있고 `<modules>`가 없다. 현재 저장소는 Maven 멀티모듈이 아니다.
- `pom.xml:22-41`은 Google Cloud Workflows Executions와 Cloud Storage SDK를 사용하고 있으며, `pom.xml:87-103`은 하나의 shaded executable JAR를 생성한다.
- `OptimizationHttpServer.java:7-20`은 같은 진입점에서 `SERVICE_MODE`로 API와 worker를 선택한다.
- `OptimizationApiController.java:48-75`는 `gs://` 입력 URI 검증, `requestId` 생성, 실행 파라미터 정규화, Workflow 실행 및 `202 ACCEPTED` 응답을 담당한다.
- `OptimizationApiController.java:78-92`는 `results/{requestId}.json`을 조회하고, 결과가 없으면 현재 `RUNNING`으로 응답한다.
- `gcp/workflows/optimization.yaml:5-24`는 `runNumber`별 독립 seed를 사용해 worker의 `/internal/batches`를 OIDC로 병렬 호출하고 HTTP 기본 재시도를 적용한다.
- `gcp/workflows/optimization.yaml:25-38`은 병렬 batch 이후 `/internal/finalize`를 호출한다.
- `OptimizationWorkerController.java:28-52`는 batch 후보를 `candidates/{requestId}/{runNumber}.json`에 저장한다.
- `OptimizationWorkerController.java:55-86`은 후보를 읽어 최종 결과를 `results/{requestId}.json`에 저장한다. 현재의 단일 `double objective` 비교는 placeholder 구현이므로 최종 고객 정책 기반 비교 계약으로 교체되어야 한다.
- `AlnsBatchEngine.java:8-27`은 Cloud Run 어댑터와 향후 GKE worker가 함께 호출할 무상태 경계이며, 실제 ALNS 대신 임시 목적값을 반환한다고 명시한다.
- `docs/arranged/08_gcp_architecture.md:5-28`은 Cloud Run API → Workflows → Cloud Run worker → Cloud Storage 흐름, 최대 20개 병렬 실행, 코어의 클라우드 독립성, Cloud Run Job/GKE Job 전환 조건과 운영 원칙을 정의한다.
- `gcp/README.md:29-75`는 API, Workflow, worker 서비스 계정을 분리하고 필요한 IAM 역할과 두 Cloud Run 서비스의 배포 방식을 보여준다.
- `gcp/README.md:79-81`은 API와 worker를 scale-to-zero로 운영하고 입력과 결과를 같은 리전의 Cloud Storage에 두는 현재 운영 기준을 설명한다.
- 현재 `master-design.md`의 `5`, `20`, `28`, `217-218`, `262-265`, `303-337`, `378-381`, `534`, `1568-1578`, `1696`, `1723`, `2113-2114`, `2147`, `2186`, `2225` 부근에는 Maven 멀티모듈, `worker-app`, SQS, S3, Spring 또는 과거 worker 전환 가정이 남아 있어 GCP 기준과 충돌한다.

## 마스터 설계 반영안

### 1. 문서의 현재 상태와 목표 상태를 분리한다

문서 서두와 6장에는 다음 두 상태를 혼합하지 말고 명시적으로 분리한다.

**현재 상태**

- 단일 Maven artifact `com.ronext:ro-next`
- 하나의 shaded JAR와 컨테이너 이미지
- `SERVICE_MODE`로 분리 배포된 Cloud Run API와 worker
- Workflows 기반 seed별 병렬 실행
- Cloud Storage 기반 후보·결과 전달
- placeholder `AlnsBatchEngine`

**목표 상태**

- 위 GCP 실행 흐름은 유지
- placeholder 뒤에 실제 업무 입력 변환, 초기해 포트폴리오, RPDPTW ALNS와 결과 변환 구현
- GCP/HTTP/Jackson과 독립된 순수 `application`/`domain` 계층
- 고객별 목적함수 비교 계약을 사용한 후보 finalize
- 실패·재시도·결과 보존 정책이 명시된 비동기 실행 계약

### 2. 8장 상위 구조를 GCP 실행 흐름으로 교체한다

기존 `SQS → worker-app → solver-core → S3` 그림을 다음 구조로 바꾼다.

```text
Client
  │ POST /optimizations { inputUri, parameters }
  ▼
Cloud Run API (SERVICE_MODE=api, scale-to-zero)
  │ requestId 생성 + Workflow 실행
  ▼
Google Cloud Workflows
  │
  ├─ parallel run 0 ──OIDC──▶ Cloud Run worker /internal/batches
  ├─ parallel run 1 ──OIDC──▶ Cloud Run worker /internal/batches
  ├─ ...                         │
  │                             ├─ Cloud Storage에서 입력 읽기
  │                             ├─ application/domain 솔버 호출
  │                             └─ candidates/{requestId}/{runNumber}.json 저장
  │
  └─ finalize ───────OIDC──▶ Cloud Run worker /internal/finalize
                                ├─ 후보 조회 및 정책 기반 비교
                                └─ results/{requestId}.json 저장

Client ── GET /optimizations/{requestId} ──▶ Cloud Run API
```

이 그림에서 Cloud Run과 Workflows는 실행·오케스트레이션 계층이고, 솔버는 그 안에 종속되는 것이 아니라 worker 어댑터가 호출하는 독립 경계임을 함께 명시한다.

### 3. 논리 계층을 물리 Maven 모듈과 구분한다

8장과 9장은 현재 존재하지 않는 `solver-core`/`worker-app` 물리 모듈을 사실로 쓰지 않는다. 우선 다음 논리 계층으로 설명한다.

```text
adapter.in.http
  - 외부 API와 내부 worker HTTP 요청/응답

adapter.out.gcp
  - Workflow 실행
  - Cloud Storage 입력/후보/결과 저장소

application
  - batch 실행 유스케이스
  - finalize 유스케이스
  - 솔버 실행과 저장소 포트 조정

domain / solver
  - RPDPTW 모델, 변환 후 탐색, 평가, 결과
  - GCP SDK, HTTP 타입, 운영 JSON 타입을 참조하지 않음
```

물리적 멀티모듈 전환이 별도로 확정되기 전에는 “단일 Maven 프로젝트 안의 논리적 계층 분리”를 기준으로 쓴다. 추후 멀티모듈화하더라도 위 의존 방향과 외부 계약은 바뀌지 않아야 한다.

### 4. 6장 ‘현재 구현’과 ‘재사용 자산’을 실제 저장소에 맞춘다

`6.1 현재 구현되어 있는 기능`은 과거 SQS/S3 흐름 대신 다음을 기술한다.

- 비동기 최적화 제출·조회 HTTP API 골격
- Workflow 실행 생성
- `parallelRuns` 1~20, `iterationsPerRun` 100~250,000의 현재 어댑터 경계값
- 독립 seed 산출과 병렬 worker 호출
- 후보 및 최종 결과의 Cloud Storage 저장
- API/Workflow/worker 서비스 계정 분리
- 아직 실제 최적화가 아닌 placeholder batch engine

`6.3 재사용할 기존 자산`은 다음으로 바꾼다.

- Cloud Run HTTP 진입점과 API/worker 모드 분리
- Workflows fan-out/finalize 골격
- Cloud Storage URI 및 객체 경로 규칙
- GCP 배포·IAM 골격
- Maven/Java 25/JUnit 5 기반
- 클라우드와 분리된 `AlnsBatchEngine` 호출 경계

SQS listener, S3 publisher, Spring/AWS 구성, 존재하지 않는 Maven 멀티모듈은 재사용 자산에서 제거한다.

### 5. 핵심 설계 결정 표를 갱신한다

GCP 전환과 관련된 기존 결정을 다음 취지로 수정한다.

| 대상 | 교체할 결정 |
|---|---|
| D2 | Maven 빌드는 유지한다. 현재 물리 구조는 단일 모듈이며, 멀티모듈 여부는 별도 결정으로 남긴다. |
| D6 | Jackson과 GCP SDK는 어댑터/I/O 구현에만 둔다. 솔버 코어는 HTTP, Workflows, Cloud Storage에 의존하지 않는다. |
| D7 | Phase 13은 `worker-app` 교체가 아니라 기존 Cloud Run worker의 `AlnsBatchEngine` placeholder를 실제 application/domain 파이프라인으로 교체하는 통합 단계다. |
| 신규 결정 | 운영 오케스트레이션은 Workflows가 담당하며 seed별 batch는 독립·무상태·멱등으로 실행한다. |
| 신규 결정 | 대용량 입력은 Workflow 인자에 넣지 않고 Cloud Storage URI로 전달한다. |
| 신규 결정 | 후보와 최종 결과는 결정적 객체 경로 및 버전이 명시된 스키마로 저장한다. |
| 신규 결정 | 실행 환경을 Cloud Run Job/GKE Job으로 바꿔도 application/domain 호출 계약은 유지한다. |

설계 결정 번호는 18개 세션의 변경사항을 통합할 때 전체 표를 다시 정렬한다. 이 세션에서 임의로 새 번호를 확정하지 않는다.

### 6. Solver 전환 절과 Phase 13을 바꾼다

10장 마지막 문장과 31장 Phase 13은 다음 의미가 되어야 한다.

```text
현재:
Cloud Run worker HTTP adapter
→ placeholder AlnsBatchEngine
→ placeholder candidate

Phase 13 이후:
Cloud Run worker HTTP adapter
→ GCS input adapter / Business JSON mapping
→ application batch use case
→ RPDPTW solver core
→ candidate/result mapping
→ GCS candidate repository
```

Phase 13의 완료 기준에는 최소한 다음을 포함한다.

- 기존 `/internal/batches`, `/internal/finalize` 계약 유지 또는 버전 변경 명시
- 실제 입력 URI 로드 및 검증
- 실제 솔버 호출
- 고객별 비교 정책으로 최종 후보 선택
- 동일 `requestId + runNumber` 재시도 멱등성
- Workflow 실패가 영구 `RUNNING`으로 보이지 않는 상태 처리
- 후보/최종 결과 스키마 버전 기록
- 로컬 단위 테스트가 GCP 연결 없이 실행 가능

### 7. 29장 실행 진입점을 GCP 역할별로 다시 쓴다

기존 `29.4 worker-app`을 아래 항목으로 나눈다.

- `Cloud Run API`: 요청 수락, 파라미터 경계 검증, `requestId` 생성, Workflow 시작, 상태/결과 조회
- `Google Cloud Workflows`: batch fan-out, OIDC 호출, 재시도, 모든 batch 완료 후 finalize
- `Cloud Run worker — batch`: 입력 조회, 한 seed의 솔버 실행, 후보 저장
- `Cloud Run worker — finalize`: 기대한 후보 집합 검증, 고객별 비교 계약 적용, 최종 결과 저장
- `Cloud Storage`: 입력 URI, 실행 manifest 또는 상태, 후보, 최종 결과 저장
- `Local Benchmark/Experiment Runner`: GCP 없이 동일 application/domain 코어 호출

Cloud Storage 객체 경로는 문서 수준에서 최소 다음 namespace를 고정한다.

```text
inputs/...                              # 외부 입력 또는 정규화 입력
runs/{requestId}/manifest.json          # 권장: 요청·설정·상태·스키마 버전
runs/{requestId}/candidates/{run}.json  # seed별 후보
runs/{requestId}/result.json            # 최종 결과
```

현재 구현의 `candidates/{requestId}/{runNumber}.json`, `results/{requestId}.json`과 다른 경로를 채택한다면 API/Workflow/worker를 함께 변경해야 하므로, 위 경로는 목표안으로 표시하고 실제 전환 시 호환 또는 마이그레이션 방식을 결정한다.

### 8. 운영 계약과 실패 모델을 설계에 추가한다

현재 구현은 결과 객체가 없으면 항상 `RUNNING`을 반환한다. Master Design에는 최소 다음 상태 전이를 별도로 정의해야 한다.

```text
ACCEPTED → RUNNING → COMPLETED
                   ├→ FAILED
                   ├→ TIMED_OUT
                   └→ CANCELLED  # 취소 기능을 지원하는 경우에만
```

각 상태에는 `requestId`, Workflow execution 식별자, 제출/시작/완료 시각, 입력 URI, 설정과 seed 목록, solver/schema 버전, 오류 코드가 필요한지 검토한다. 오류 상세에 입력 데이터나 내부 예외를 그대로 노출하지 않는 원칙도 명시한다.

재시도 계약은 다음 수준까지 문서화한다.

- Workflow의 HTTP 재시도 대상과 최대 횟수
- 동일 batch 재호출 시 같은 seed·설정 사용
- 후보 쓰기 충돌 시 덮어쓰기, generation 조건부 쓰기 또는 기존 결과 재사용 중 하나 선택
- finalize는 기대한 `parallelRuns`개의 후보가 모두 있는지 확인
- 부분 실패를 전체 실패로 볼지, 성공 후보만으로 finalize할지 결정
- 최종 결과 저장 이후 finalize 재시도의 결과 일관성

### 9. IAM과 데이터 배치를 독립 절로 둔다

GCP 배포 절에는 서비스 계정별 책임을 서술하고 프로젝트 전체 권한보다 버킷·서비스 단위 권한을 우선한다.

입력 bucket과 결과 bucket이 같다는 가정을 암묵적으로 두지 않는다. 다음을 명시적으로 결정한 뒤 문서화한다.

- 허용되는 입력 bucket/project 목록
- worker의 cross-project 입력 읽기 권한
- 결과 조회 주체와 API의 결과 읽기 권한
- 후보와 결과의 암호화, 보존 기간, lifecycle 삭제 정책
- 리전 및 데이터 소재지

### 10. 36·37·38·39장의 AWS 표현을 일괄 교체한다

주문 처리 예시, 전체 흐름 요약, 종합 평가와 최종 요약의 마지막 구간은 모두 다음으로 끝나야 한다.

```text
RPDPTW solver result
→ application result mapping
→ Cloud Run worker adapter
→ Cloud Storage candidate/result
→ Cloud Run API status/result response
```

“신규 패키지에서 병렬 개발 후 worker-app 전환”이라는 표현은 “현재 GCP 실행 골격을 유지한 채 placeholder 경계 뒤의 코어를 단계적으로 구현하고 Phase 13에서 실제 파이프라인으로 연결”로 바꾼다.

## 남은 질문

1. 현재와 같이 **단일 Maven artifact/단일 이미지 + `SERVICE_MODE` 분리 배포**를 목표 구조로 유지할 것인가, 아니면 장래에 API·worker·solver를 물리 Maven 모듈 또는 별도 이미지로 분리할 계획인가?
2. Cloud Run API는 운영 환경에서도 외부 공개 API인가? 비공개라면 IAM, IAP, API Gateway 중 어느 인증 경계를 기준으로 설계할 것인가?
3. 입력과 결과를 하나의 bucket에 둘 것인가, 별도 bucket으로 분리할 것인가? 외부 프로젝트의 `gs://` 입력도 허용하는가?
4. 결과가 없을 때 객체 존재 여부만으로 상태를 판단하지 않으려면 실행 manifest를 Cloud Storage에 둘 것인가, Workflow execution 상태를 API가 직접 조회할 것인가, 별도 상태 저장소를 둘 것인가?
5. 병렬 batch 중 일부가 영구 실패하면 전체 요청을 `FAILED`로 종료할 것인가, 성공한 후보만으로 최종화하되 부분 실패를 결과에 기록할 것인가?
6. Workflows의 재시도에서 같은 후보 키가 이미 존재할 때 기존 결과 재사용, 무조건 덮어쓰기, generation 조건부 쓰기 중 어느 정책을 사용할 것인가?
7. Cloud Run request 한도를 넘는 것이 확인되었을 때 1차 대체 실행 환경은 Cloud Run Job인가, GKE Job인가? 전환 판단 기준으로 사용할 최대 실행시간·메모리·비용 임계치는 무엇인가?
8. 후보와 결과 객체의 보존 기간, 삭제 lifecycle, 감사·재현을 위한 최소 보존 메타데이터는 무엇인가?
9. 현재 컨테이너 runtime의 AWS Corretto 25는 GCP에서도 계속 사용할 것인가, 아니면 공급망을 단순화하기 위해 Temurin 등 다른 JDK 이미지로 통일할 것인가? Corretto 사용 자체는 ECS/SQS/S3 아키텍처 의존은 아니다.
10. API가 허용할 `parallelRuns`와 `iterationsPerRun`의 현재 범위(1~20, 100~250,000)를 제품 계약으로 고정할 것인가, 환경별 설정으로 이동할 것인가?
11. Workflows execution ID와 solver/schema/image 버전을 최종 결과에 기록해 운영 추적과 재현에 사용할 것인가?

## 범위 밖

- 이 세션에서는 코드, `pom.xml`, GCP YAML, 배포 스크립트와 `master-design.md`를 수정하지 않는다.
- 실제 GCP 프로젝트 생성, IAM 적용, Cloud Run/Workflows 배포는 수행하지 않는다.
- RPDPTW 명칭·패키지명의 최종 통일은 세션 2의 범위다.
- CVRPTW 입력을 pickup-delivery 모델로 변환하는 의미는 세션 3의 범위다.
- 고객별 목적함수 및 최종 후보 비교 구조의 상세 설계는 세션 4의 범위다. 이 문서에서는 GCP finalize가 그 비교 계약을 호출해야 한다는 경계만 정의한다.
- 차량 크기 유형과 추가 차량/주문 제약의 상세 모델은 세션 5의 범위다.
- ALNS, 초기해 포트폴리오, rollback, 수치·시간 모델, 미배정 사유, 벤치마크의 알고리즘 상세는 각 담당 세션의 범위다.
- 실제 부하 측정 전 Cloud Run Job 또는 GKE Job으로의 선제 전환, 인스턴스 크기와 동시성 수치 확정은 하지 않는다.
- AWS Corretto JDK 이미지 선택은 ECS/SQS/S3 운영 아키텍처와 별개이며, 위 질문이 결정되기 전 임의로 제거하지 않는다.
