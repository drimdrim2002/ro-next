# RPDPTW 설계 문서 지도

이 폴더의 현재 최상위 진입점은 아래 두 문서다. 둘 다 상태가 `REVIEW`이므로 아직 승인된 구현 기준이나 conflict authority가 아니다.

| 순서 | 문서 | 상태 | 역할 |
|---:|---|---|---|
| 1 | [Master Design](master-design.md) | `REVIEW` | 향후 RPDPTW 구현의 전체 책임, 불변조건, 의존 순서와 검증 gate에 대한 검토 제안 |
| 2 | [Master Design question register](master-design-open-questions.md) | `REVIEW` | 정확한 28개 질문의 해결·실험 대기·deferred 상태와 evidence/gate의 중앙 등록부 |

`REVIEW` 중 source authority와 승인 뒤 hierarchy는 [Master Design §1](master-design.md#1-문서-상태와-규범)을 따른다. 26개 interview 대상의 권위 있는 사용자 답변은 [세션 29](master-design-sessions/29-open-question-interview.md), 문서 반영·검증 기록은 [세션 30](master-design-sessions/30-open-question-integration.md)에 있다. 두 실험 대기 질문은 등록부의 승인 절차 없이 구현 default로 닫지 않는다.

## 권장 읽기 순서

처음 검토하는 독자는 다음 순서로 읽는다.

```text
docs/README.md
→ master-design.md
→ master-design-open-questions.md
→ 필요한 master-design-sessions 상세 초안
→ arranged 연구 정리
→ orgin 원본 근거
```

특정 결정의 근거를 추적할 때는 [세션 29 사용자 인터뷰](master-design-sessions/29-open-question-interview.md), [세션 30 통합 기록](master-design-sessions/30-open-question-integration.md), Master §17의 traceability, 관련 역사 세션 순으로 확인한다. 세션 19는 원래 28개 질문과 통합 baseline을, 세션 18은 문서 상태와 변경 절차를 제공한다.

## 문서 계층과 규범 지위

| 경로 | 역할 | 현재 지위 |
|---|---|---|
| [master-design.md](master-design.md) | 전체 Master 검토안 | `REVIEW`; 승인 전 conflict resolver가 아님 |
| [master-design-open-questions.md](master-design-open-questions.md) | 중앙 질문 등록부 | `REVIEW`; 질문 답이나 승인 기록이 아님 |
| [master-design-sessions/](master-design-sessions/) | 세션별 검토, 통합 초안과 evidence | 비규범 review input; Master 또는 승인 기록을 대체하지 않음 |
| [domain-design.md](domain-design.md) | legacy 상세 inventory와 traceability evidence | 현재 Master가 명시한 범위에서 비규범 참고 자료 |
| [arranged/](arranged/) | 주제별로 정리한 연구·기존 문서 | 비규범 연구 evidence |
| [orgin/](orgin/) | 원본 요약 보존 경로 | 비규범 original evidence |

`master-design-sessions`, `arranged`, `orgin`의 예시·권장안·연구 설명은 그 자체로 목표 계약이 아니다. 승인된 문서와 충돌하면 Master §1의 상태별 절차를 사용한다.

## 기존 통합 연구 문서

아래 문서는 연구와 역사적 설계를 주제별로 정리한 참고 자료다.

| 문서 | 역할 |
|---|---|
| [01_problem_definition.md](arranged/01_problem_definition.md) | CVRPTW/CVRP/PDPTW 정의, 입력, 제약, 목적함수와 feasibility |
| [02_initial_solution_heuristics.md](arranged/02_initial_solution_heuristics.md) | 초기해 휴리스틱, 포트폴리오, route elimination과 route pool |
| [03_alns_metaheuristic.md](arranged/03_alns_metaheuristic.md) | ALNS, Simulated Annealing, destroy/repair와 adaptive weight |
| [04_hgs_metaheuristic.md](arranged/04_hgs_metaheuristic.md) | HGS 구조, giant tour, split, crossover와 population |
| [05_local_search_moves.md](arranged/05_local_search_moves.md) | Relocate, Swap, 2-opt 계열, Or-opt, Cross-exchange와 SWAP* |
| [06_practical_extensions.md](arranged/06_practical_extensions.md) | Zone, 차량 부족, optional customer, dummy vehicle와 외주/이월 |
| [07_papers_and_benchmarks.md](arranged/07_papers_and_benchmarks.md) | 논문, 실험 결과, operator 근거와 한계 |
| [coverage_map.md](arranged/coverage_map.md) | 원본 파일별 반영 위치와 누락 확인 |
| [08_gcp_architecture.md](arranged/08_gcp_architecture.md) | provider-specific 역사·current-state evidence와 deferred infrastructure 검토 입력 |

특히 GCP 관련 세션·arranged 자료는 역사적/current-state evidence 또는 향후 `Q-INFRA-01`을 검토할 때 사용할 deferred input이다. 특정 cloud, product, runtime, service 또는 deployment unit을 committed target topology로 정하지 않는다. 현재 목표 설계는 cloud/product-neutral logical port와 책임 경계만 소유하며, 물리 infrastructure는 별도 evidence와 승인 전까지 deferred다.

## 원본 보존

통합에 사용된 원본 요약은 [orgin/](orgin/)에 보존한다. 디렉터리의 현재 철자는 기존 링크와 이력을 보존하기 위한 repository path이며, 원본·연구 자료의 존재가 규범 승인을 뜻하지 않는다.
