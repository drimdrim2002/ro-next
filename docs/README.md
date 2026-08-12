# RO-Next 설계 문서 지도

2026-08-09 재설계로 확정된 현행 문서와 아카이브를 구분하는 진입점이다.

## 현행 문서 (이것만 효력 있음)

| 순서 | 문서 | 역할 |
|---:|---|---|
| 1 | [Master Design](master-design.md) | 목표 · 전체 흐름 · 핵심 결정 · 범위 · 완료 기준 |
| 2 | [Domain Design](domain-design.md) | 입력·정규화·Problem/Solution·전파·평가·ALNS·재검증·결과의 **의미와 규칙** |
| 3 | [Architecture Design](architecture-design.md) | 모듈(3개: solver-core·solver-profile·app)·경계 규칙·앱 구조·S3 배치·ECS Fargate 배포·로컬 환경 |
| 4 | [Implementation Plan](implementation-plan.md) | 구현 단계(Stage 0–8)와 단계별 완료 기준 |
| 5 | [implementation/](implementation/) Stage 문서 (stage-00~08 · stage-extra) | Stage별 구현 직전 상세 — 파일·클래스·시그니처·테스트 표 (**구현 계약**) |

읽는 순서 = 표 순서. 충돌 시 Master → Domain → Architecture → Implementation Plan(→ Stage 상세) 순으로 우선한다.

참고 자료: 입력 규약 [data/ro_input_json_spec.pdf](../data/ro_input_json_spec.pdf) ·
실행 fixture [data/win_poc_case_floor.json](../data/win_poc_case_floor.json)

## 아카이브 (효력 없음, 삭제하지 않음)

| 경로 | 내용 |
|---|---|
| [deprecated/](deprecated/) | 구 설계 전부. 2026-07-31 Phase B 3문서(`2026-07-31-phase-b-*.md`), 15-phase 구현 세트([implementation-15phase/](deprecated/implementation-15phase/)), Phase A 인터뷰, 그 이전 문서들 |
| [master-design-sessions/](master-design-sessions/) | 과거 설계 세션 기록 (비규범) |
| [arranged/](arranged/) · [orgin/](orgin/) | ALNS/HGS 연구 정리·원본 요약 (비규범) |

아카이브 문서의 결정 등록부(A\*/D\*/O\*)·gate 체계·GATED/C-17 표기는 모두 폐기된 체계다.
현행 문서와 충돌하면 **항상 현행 문서가 이긴다**.
