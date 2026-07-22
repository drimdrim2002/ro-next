# CVRPTW 문서 통합 정리

이 폴더는 루트 폴더에 있던 CVRPTW, ALNS, HGS, 초기해 휴리스틱, 로컬서치, 실무 확장, 논문 리뷰 문서를 보존한 상태에서 새로 재구성한 통합 정리본이다.

원본 문서는 수정하지 않았다. 중복 설명은 카테고리별 대표 문서에 합쳤고, 원본 주제가 어느 통합 문서에 반영됐는지는 `coverage_map.md`에서 확인할 수 있다.

## 문서 구성

| 순서 | 문서 | 역할 |
|---:|---|---|
| 1 | `01_problem_definition.md` | CVRPTW/CVRP/PDPTW 정의, 입력, 제약, 목적함수, feasibility |
| 2 | `02_initial_solution_heuristics.md` | 초기해 생성 휴리스틱, 포트폴리오, route elimination, route pool |
| 3 | `03_alns_metaheuristic.md` | ALNS + Simulated Annealing, destroy/repair, adaptive weight |
| 4 | `04_hgs_metaheuristic.md` | HGS 구조, giant tour, split, crossover, population, penalty |
| 5 | `05_local_search_moves.md` | Relocate, Swap, 2-opt, 2-opt*, Or-opt, Cross-exchange, SWAP* |
| 6 | `06_practical_extensions.md` | Zone 제약, 차량 부족, optional customer, dummy vehicle, 외주/이월 |
| 7 | `07_papers_and_benchmarks.md` | 관련 논문 핵심, 실험 결과, operator ranking, 한계 |
| 8 | `coverage_map.md` | 원본 파일별 반영 위치와 누락 확인표 |
| 9 | `08_gcp_architecture.md` | Java 25, Cloud Run, Workflows 기반 실행/확장 구조 |

## 권장 읽기 순서

처음 보는 독자는 다음 순서가 자연스럽다.

```text
01_problem_definition
→ 02_initial_solution_heuristics
→ 03_alns_metaheuristic
→ 05_local_search_moves
→ 04_hgs_metaheuristic
→ 06_practical_extensions
→ 07_papers_and_benchmarks
```

구현 관점에서는 다음 순서가 더 적합하다.

```text
01_problem_definition
→ 02_initial_solution_heuristics
→ 03_alns_metaheuristic
→ 05_local_search_moves
→ 06_practical_extensions
```

벤치마크나 논문 근거를 확인하려면 `07_papers_and_benchmarks.md`를 함께 보면 된다.

## 중복 정리 원칙

- CVRPTW/CVRP의 기본 정의는 `01_problem_definition.md`로 통합했다.
- ALNS의 destroy/repair, SA, adaptive weight 설명은 `03_alns_metaheuristic.md`로 통합했다.
- HGS-CVRP 문서 두 개의 중복 내용은 `04_hgs_metaheuristic.md`와 `05_local_search_moves.md`로 나눴다.
- SWAP*는 HGS 문맥과 local search 문맥에 모두 등장하지만, 상세 설명은 `05_local_search_moves.md`에 두고 HGS 문서에서는 역할만 설명했다.
- Zone, 차량 부족, optional customer, dummy vehicle 내용은 `06_practical_extensions.md`로 모았다.
- 논문별 실험 결과, 기여, 한계는 `07_papers_and_benchmarks.md`로 모았다.

## 원본 파일

통합에 사용한 원본 파일은 다음 8개다.

- `cvrptw_alns_sa_summary.md`
- `cvrptw_heuristic_strategy_summary.md`
- `cvrptw_hgs_alns_zone_summary.md`
- `hgs_local_search_moves_summary.md`
- `hgs_cvrp_swapstar_qa_summary.md`
- `HGS_CVRP_QA_정리.md`
- `alns_pdptw_paper_summary_ko.md`
- `alns_vrp_paper_lecture_summary.md`
