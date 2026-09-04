# 구현 상세 설계 (Stage 문서)

[Implementation Plan](../implementation-plan.md)의 Stage별 상세 구현 설계 문서다.
각 문서는 해당 Stage의 파일·클래스·시그니처·테스트를 구현 직전 수준으로 고정한다.
배차 규칙의 권위는 항상 [Domain Design](../domain-design.md), 배치의 권위는
[Architecture Design](../architecture-design.md)이며, 충돌 시 그 문서들이 이긴다.

이름 규칙: `stage-NN-<slug>.md`. 이전 Stage 문서가 정한 클래스명·패키지·시그니처는
다음 Stage가 그대로 이어받는다 (같은 개념에 새 이름 금지).

**한 Stage에 문서가 둘일 수 있다 (2026-09-02).** Stage 4가 그렇다 —
`stage-04-initial-solution-heuristics.md`가 초기해 기법 25개를,
`stage-04-alns.md`가 ALNS 본체를 소유한다. 한 문서에 넣으면 두 배로
길어져 ALNS 계약이 묻히기 때문이고, 쪼갤 때는 **소유 범위를 서로의 서두에 명시**해 같은
규정이 두 곳에 생기지 않게 한다. 번호는 같은 Stage 번호를 쓰고 slug로 구분한다.

**그리고 Stage 4는 두 단계로 나눠 구현한다 — `4-초기해` → `4-ALNS`** (Plan §2.2. 5-재검증/
5-결과와 같은 형식이되, 문서까지 갈라진 경우다). 각 문서가 자기 DoD와 테스트 표를 소유하고
**따로 green이 된다** — 초기해 쪽은 ALNS 타입을 하나도 참조하지 않는다. 아래 인덱스의
Stage 칸이 `4-초기해`·`4-ALNS`로 적힌 이유다.

**일정 번호는 식별자에 넣지 않는다 (2026-08-22).** `Stage3Fixtures` 같은 이름은 Stage가 끝나면
가리킬 대상이 없는데도 다음 Stage가 그대로 물려받는다(Stage 4 테스트가 `Stage3Fixtures`를
import하게 된다). 반대로 설계 문서의 **절 좌표**는 권위 문서를 가리키므로 그대로 쓴다 —
`section72Problem`·`reproducesDomainSection72`는 "Domain §7.2를 재현한다"는 뜻이라 정당하다.

**읽는 법**: Plan의 Stage N 본문(배차/시스템 관점·평문 DoD) → Domain 해당 절 → 이 폴더의
stage-NN 문서(파일·테스트 계약). Plan이 지도, 여기가 구현 직전 상세다.

**미해결 질문 절의 규칙 (2026-08-13 공통화)**: 각 Stage 문서의 미해결 질문 절에서 닫힌 질문은
**지우지 않고 해소 표시(날짜·근거·본문 반영 지점)를 달아 남긴다.** 해소 내용은 반드시 본문과
frontmatter `revisions`에 반영돼 있어야 하고, 본문에 "해소 선행" 같은 옛 전제 문구가 남아 있으면
안 된다. 표시 없이 남는 것은 진짜 미해결뿐이다. (종전에는 이 규칙이 stage-01에만 반대 방향으로
적혀 있었다 — 해소 서술과 교차 참조를 보존하는 다수 관행을 공통 규칙으로 확정.)

## Stage 인덱스

| Stage | 문서 | 범위 한 줄 (VRPTW/시스템 관점) | 상태 |
|---:|---|---|---|
| 0 | [stage-00-cleanup-and-skeleton.md](stage-00-cleanup-and-skeleton.md) | 배차 로직 전: 3모듈 뼈대·경계 검사·앱 기동 통로 | 작성됨 |
| 1 | [stage-01-canonical-input-normalization.md](stage-01-canonical-input-normalization.md) | 입력 instance를 솔버 정본 단위·의미(`Plan`)로 정규화 | 작성됨 |
| 2 | [stage-02-travel-and-problem-freeze.md](stage-02-travel-and-problem-freeze.md) | 이동표 완비 + 탐색이 못 바꾸는 `Problem` 동결 | 작성됨 |
| 3 | [stage-03-solution-propagation-evaluation.md](stage-03-solution-propagation-evaluation.md) | 배차안 표현·경로 전파·점수 비교(feasibility + objective) | 작성됨 |
| 4-초기해 | [stage-04-initial-solution-heuristics.md](stage-04-initial-solution-heuristics.md) | 초기해 construction 25개 포트폴리오 (기본 8 + 확장 14 + 실물 맞춤 3, 결정적·rule 기반) — **먼저** | 작성됨 |
| 4-초기해 | [stage-04-initial-solution-heuristics-survey.md](stage-04-initial-solution-heuristics-survey.md) | ↑의 **근거 문서** — 문헌·솔버 조사, 확장 14개의 출처·기각 사유·25 → n 비교 프로토콜 (계약 아님) | 작성됨 |
| 4-초기해 | [stage-04-h25-zone-quota-exchange.md](stage-04-h25-zone-quota-exchange.md) | ↑의 **설계안** — H25 `zone-quota-exchange-fill`(greedy 존 배정 + 구역 간 교환)의 설계와 H3·H23 비교 프로토콜. 계약 개정은 heuristics 문서에 반영됨 | 구현·실측 완료 |
| 4-ALNS | [stage-04-alns.md](stage-04-alns.md) | ALNS(destroy/repair, pair 단위)·acceptance·종료 — 초기해 green 이후 | 작성됨 |
| 5 | [stage-05-verification-and-result.md](stage-05-verification-and-result.md) | 탐색과 독립 재검증(발행 게이트) + 결과 모델 | 작성됨 |
| 6 | [stage-06-app-assembly.md](stage-06-app-assembly.md) | 솔버를 HTTP 서비스로: 접수·저장·비동기 풀이·조회 | 작성됨 |
| 7 | [stage-07-ecs-deployment.md](stage-07-ecs-deployment.md) | 클라우드 상시 실행(ECS Fargate) + 실 S3 | 작성됨 |
| 8 | [stage-08-benchmark-comparison.md](stage-08-benchmark-comparison.md) | Win 대비 지표 비교·탐색 파라미터 조정 기록 | 작성됨 |
| Extra | [stage-extra-deferred-features.md](stage-extra-deferred-features.md) | 유예 항목 등재부 — 트리거가 발동하면 정식 Stage로 승격 | 등재 중 |

**Stage Extra는 순서에 없다.** 0–8과 달리 완료되는 단계가 아니라, "지금 만들지 않기로 했지만
조건이 되면 만든다"고 정한 항목의 등재부다. 다른 Stage 문서와 달리 **구현 직전 수준으로 쓰지
않는다** — 트리거가 발동하지 않은 항목을 미리 설계하는 것이 그 문서가 생긴 원인이기 때문이다
(깊이 기준은 그 문서 §0).
