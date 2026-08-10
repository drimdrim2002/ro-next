# 구현 상세 설계 (Stage 문서)

[Implementation Plan](../implementation-plan.md)의 Stage별 상세 구현 설계 문서다.
각 문서는 해당 Stage의 파일·클래스·시그니처·테스트를 구현 직전 수준으로 고정한다.
배차 규칙의 권위는 항상 [Domain Design](../domain-design.md), 배치의 권위는
[Architecture Design](../architecture-design.md)이며, 충돌 시 그 문서들이 이긴다.

이름 규칙: `stage-NN-<slug>.md`. 이전 Stage 문서가 정한 클래스명·패키지·시그니처는
다음 Stage가 그대로 이어받는다 (같은 개념에 새 이름 금지).

## Stage 인덱스

| Stage | 문서 | 범위 한 줄 | 상태 |
|---:|---|---|---|
| 0 | [stage-00-cleanup-and-skeleton.md](stage-00-cleanup-and-skeleton.md) | 구 코드·GCP 잔재 정리, 3모듈 뼈대, ArchUnit 경계 룰, README | 작성됨 |
| 1 | [stage-01-canonical-input-normalization.md](stage-01-canonical-input-normalization.md) | canonical 입력과 정규화 (solver-core) | 작성됨 |
| 2 | [stage-02-travel-and-problem-freeze.md](stage-02-travel-and-problem-freeze.md) | 이동표 준비(누락 보정·speed 체인)와 Problem 동결 (solver-core) | 작성됨 |
| 3 | [stage-03-solution-propagation-evaluation.md](stage-03-solution-propagation-evaluation.md) | Solution·구조 검사(pair·XOR)·전파 루프·평가(metric·score 축·사전식 비교)·profile SPI와 레지스트리 | 작성됨 |
| 4 | [stage-04-initial-solution-and-alns.md](stage-04-initial-solution-and-alns.md) | 초기해 생성(결정적 greedy)·ALNS 루프(destroy/repair 연산자 SPI·acceptance·시간 한도 종료) (solver-core) | 작성됨 |
| 5 | [stage-05-verification-and-result.md](stage-05-verification-and-result.md) | 독립 재검증(구조·재전파·점수 대조, verify 두 번째 구현)과 결과 모델(routes/unassigned+사유/metrics/run 메타) (solver-core) | 작성됨 |
| 6 | [stage-06-app-assembly.md](stage-06-app-assembly.md) | 앱 조립: 규약 JSON adapter(wire 매핑표)·SolveStore(S3/local)·접수/조회 API·SolveExecutor(상태 전이·heartbeat·STALE)·result.json wire 잠정안 (app) | 작성됨 |
| 7 | [stage-07-ecs-deployment.md](stage-07-ecs-deployment.md) | ECS Fargate 배포: Dockerfile 재작성(멀티스테이지·app jar)·태스크 정의/롤(S3 최소 권한)·환경변수→설정 키 매핑·CloudWatch·배포 절차·선택 LocalStack e2e | 작성됨 |
| 8 | [stage-08-benchmark-comparison.md](stage-08-benchmark-comparison.md) | Win 벤치마크 비교: 지표 정의·대응, 비교 실행 절차, 실행 조건 기록 양식(benchmark-results.md), 파라미터 조정 실험 방식 — multiRotation 해소가 선행 조건 (Win 결과는 data/alns_result.csv 입수됨) | 작성됨 |
