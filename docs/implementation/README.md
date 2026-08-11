# 구현 상세 설계 (Stage 문서)

[Implementation Plan](../implementation-plan.md)의 Stage별 상세 구현 설계 문서다.
각 문서는 해당 Stage의 파일·클래스·시그니처·테스트를 구현 직전 수준으로 고정한다.
배차 규칙의 권위는 항상 [Domain Design](../domain-design.md), 배치의 권위는
[Architecture Design](../architecture-design.md)이며, 충돌 시 그 문서들이 이긴다.

이름 규칙: `stage-NN-<slug>.md`. 이전 Stage 문서가 정한 클래스명·패키지·시그니처는
다음 Stage가 그대로 이어받는다 (같은 개념에 새 이름 금지).

**읽는 법**: Plan의 Stage N 본문(배차/시스템 관점·평문 DoD) → Domain 해당 절 → 이 폴더의
stage-NN 문서(파일·테스트 계약). Plan이 지도, 여기가 구현 직전 상세다.

## Stage 인덱스

| Stage | 문서 | 범위 한 줄 (VRPTW/시스템 관점) | 상태 |
|---:|---|---|---|
| 0 | [stage-00-cleanup-and-skeleton.md](stage-00-cleanup-and-skeleton.md) | 배차 로직 전: 3모듈 뼈대·경계 검사·앱 기동 통로 | 작성됨 |
| 1 | [stage-01-canonical-input-normalization.md](stage-01-canonical-input-normalization.md) | 입력 instance를 솔버 정본 단위·의미(`Plan`)로 정규화 | 작성됨 |
| 2 | [stage-02-travel-and-problem-freeze.md](stage-02-travel-and-problem-freeze.md) | 이동표 완비 + 탐색이 못 바꾸는 `Problem` 동결 | 작성됨 |
| 3 | [stage-03-solution-propagation-evaluation.md](stage-03-solution-propagation-evaluation.md) | 배차안 표현·경로 전파·점수 비교(feasibility + objective) | 작성됨 |
| 4 | [stage-04-initial-solution-and-alns.md](stage-04-initial-solution-and-alns.md) | 초기해 + ALNS(destroy/repair, pair 단위) | 작성됨 |
| 5 | [stage-05-verification-and-result.md](stage-05-verification-and-result.md) | 탐색과 독립 재검증(발행 게이트) + 결과 모델 | 작성됨 |
| 6 | [stage-06-app-assembly.md](stage-06-app-assembly.md) | 솔버를 HTTP 서비스로: 접수·저장·비동기 풀이·조회 | 작성됨 |
| 7 | [stage-07-ecs-deployment.md](stage-07-ecs-deployment.md) | 클라우드 상시 실행(ECS Fargate) + 실 S3 | 작성됨 |
| 8 | [stage-08-benchmark-comparison.md](stage-08-benchmark-comparison.md) | Win 대비 지표 비교·탐색 파라미터 조정 기록 | 작성됨 |
| Extra | [stage-extra-deferred-features.md](stage-extra-deferred-features.md) | 유예 항목 등재부 — 트리거가 발동하면 정식 Stage로 승격 | 등재 중 |

**Stage Extra는 순서에 없다.** 0–8과 달리 완료되는 단계가 아니라, "지금 만들지 않기로 했지만
조건이 되면 만든다"고 정한 항목의 등재부다. 다른 Stage 문서와 달리 **구현 직전 수준으로 쓰지
않는다** — 트리거가 발동하지 않은 항목을 미리 설계하는 것이 그 문서가 생긴 원인이기 때문이다
(깊이 기준은 그 문서 §0).
