# RPDPTW 설계 문서 지도

이 폴더의 현재 최상위 진입점은 아래 네 문서다. 모두 상태가 `REVIEW`이므로 아직 승인된 구현 기준이나 conflict authority가 아니다.

| 순서 | 문서 | 상태 | 역할 |
|---:|---|---|---|
| 1 | [Master Design](master-design.md) | `REVIEW` | 향후 RPDPTW 구현의 전체 책임, 불변조건, 의존 순서와 검증 gate에 대한 검토 제안 |
| 2 | [Master Design question register](master-design-open-questions.md) | `REVIEW` | 정확한 28개 질문의 해결·실험 대기·deferred 상태와 evidence/gate의 중앙 등록부; `Q-INFRA-01`은 AWS target 선택으로 해결 |
| 3 | [Domain Design](domain-design.md) | `REVIEW` | Master의 domain/input/normalization/travel/state/evaluation/result 계약을 상세화한 검토 제안 |
| 4 | [Architecture Design](architecture-design.md) | `REVIEW` | Java 25/Maven module, dependency, 고객 확장, AWS S3 + Step Functions + Lambda target/reference runtime와 future provider substitution boundary |

`REVIEW` 중 source authority와 승인 뒤 hierarchy는 [Master Design §1](master-design.md#1-이-문서의-목적-독자와-사용법)을 따른다. 26개 interview 대상의 권위 있는 사용자 답변은 [세션 29](master-design-sessions/29-open-question-interview.md), Master·등록부 반영 기록은 [세션 30](master-design-sessions/30-open-question-integration.md), Domain Design 반영 기록은 [세션 31](master-design-sessions/31-domain-design-integration.md)에 있다. `Q-INFRA-01`은 2026-07-26 사용자 승인으로 AWS S3 + Step Functions + Lambda target/reference runtime을 선택했다. `Q-BENCH-02`의 수치와 `Q-VAR-01`은 등록부 절차 없이 구현 default로 닫지 않는다.

## 권장 읽기 순서

처음 검토하는 독자는 다음 순서로 읽는다.

```text
docs/README.md
→ master-design.md
→ master-design-open-questions.md
→ domain-design.md
→ architecture-design.md
→ 필요한 master-design-sessions 상세 초안
→ arranged 연구 정리
→ orgin 원본 근거
```

특정 결정의 근거를 추적할 때는 [세션 29 사용자 인터뷰](master-design-sessions/29-open-question-interview.md), [세션 30 Master 통합 기록](master-design-sessions/30-open-question-integration.md), [세션 31 Domain 통합 기록](master-design-sessions/31-domain-design-integration.md), Master §17과 Domain §18의 traceability, 관련 역사 세션 순으로 확인한다. 세션 19는 원래 28개 질문과 통합 baseline을, 세션 18은 문서 상태와 변경 절차를 제공한다.

## 문서 계층과 규범 지위

| 경로 | 역할 | 현재 지위 |
|---|---|---|
| [master-design.md](master-design.md) | 전체 Master 검토안 | `REVIEW`; 승인 전 conflict resolver가 아님 |
| [master-design-open-questions.md](master-design-open-questions.md) | 중앙 질문 등록부 | `REVIEW`; 2026-07-26 사용자 승인 `Q-INFRA-01`의 evidence·영향 문서를 기록 |
| [master-design-sessions/](master-design-sessions/) | 세션별 검토, 통합 초안과 evidence | 비규범 review input; Master 또는 승인 기록을 대체하지 않음 |
| [domain-design.md](domain-design.md) | Domain/input/normalization/travel/state/evaluation/result 상세 설계 | `REVIEW`; Master보다 높은 conflict authority가 아님 |
| [architecture-design.md](architecture-design.md) | Java/Maven project 구조, 확장 seam, runtime와 provider mapping | `REVIEW`; AWS S3 + Step Functions + Lambda는 선택된 target/reference runtime. SDK는 adapter/deployment 경계에만 위치하며 production cutover는 별도 evidence 필요 |
| [architecture-domain-implementation-design.md](architecture-domain-implementation-design.md) | Domain·architecture 구현 순서와 AWS target adapter boundary | `REVIEW`; Master/등록부의 AWS 선택과 future substitution 조건을 구현 phase로 구체화 |
| [2026-07-26-master-design.md](2026-07-26-master-design.md) | 2026-07-26 통합 초안 | `SUPERSEDED`; 현재 결정은 canonical `master-design.md`와 질문 등록부를 따름 |
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
| [08_gcp_architecture.md](arranged/08_gcp_architecture.md) | provider-specific historical/legacy current-state evidence |

특히 GCP 관련 세션·arranged 자료와 repository의 Java/GCP path는 historical/legacy current-state evidence다. 이들은 실제로 존재하는 경로를 보존하지만 target topology를 뜻하지 않으며 `AlnsBatchEngine`의 synthetic objective placeholder 성격도 바꾸지 않는다. 현재 선택된 기본 target/reference runtime은 AWS S3 + Step Functions + Lambda다. Logical port와 domain 의미는 AWS 구현에 종속되지 않으며 ECS, GCP, Kubernetes 등은 parity evidence와 별도 승인 뒤의 미래 대체 후보로 남는다.

## 원본 보존

통합에 사용된 원본 요약은 [orgin/](orgin/)에 보존한다. 디렉터리의 현재 철자는 기존 링크와 이력을 보존하기 위한 repository path이며, 원본·연구 자료의 존재가 규범 승인을 뜻하지 않는다.
