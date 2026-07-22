# 08. GCP 배포 아키텍처

## 실행 흐름

```text
POST /optimizations (inputUri: gs://...)
  → Cloud Run API (scale-to-zero)
  → Workflows
      → ALNS batch 병렬 실행 (parallel, 최대 20)
      → Cloud Run worker가 후보를 Cloud Storage에 저장
      → Cloud Run worker가 최적 후보를 선택하고 결과 저장
  → GET /optimizations/{requestId}
```

인스턴스 본문은 Workflows 상태에 넣지 않고 Cloud Storage URI만 전달한다. 따라서 workflow 상태 크기 제한과 HTTP 요청 크기 제한을 피할 수 있다. 실제 입력 로딩과 CVRPTW 평가 로직은 다음 구현 단계에서 `AlnsBatchEngine` 뒤에 추가한다.

## Cloud Run에서 GKE로의 전환

`OptimizationWorkerController`는 HTTP 요청을 `AlnsBatchEngine`에 전달하는 얇은 어댑터다. ALNS/HGS, 초기해, local search는 `application`/`domain` 계층에 두고 GCP SDK나 HTTP 타입을 참조하지 않는다.

성능 측정 결과 한 batch가 Cloud Run 요청 제한, 메모리 또는 비용에 부딪히면 Workflows의 `runAlnsBatches` 단계만 Cloud Run Job 또는 GKE Job 호출로 바꾼다. HTTP API, 입력 포맷, 결과 포맷, 병렬 구조는 유지된다.

## 운영 원칙

- Workflows는 병렬 batch와 HTTP 오류 재시도를 관리한다.
- ALNS batch는 독립 seed로 fan-out하고 최종 선택 단계에서 objective가 가장 작은 후보를 고른다.
- 배포 전 input/result Cloud Storage bucket에 대한 최소 권한을 API와 worker 서비스 계정에 각각 부여한다.
- 운영 환경은 API 인증, Cloud Logging/Trace, 결과 객체 lifecycle rule을 추가한다.
