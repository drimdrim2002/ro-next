# 원본 문서 반영 커버리지 맵

이 문서는 원본 Markdown 파일의 주요 내용이 새 `docs/` 통합 문서 어디에 반영됐는지 확인하기 위한 추적표다.

## 1. 원본 파일별 반영 위치

| 원본 파일 | 주요 내용 | 반영 문서 |
|---|---|---|
| `cvrptw_alns_sa_summary.md` | CVRPTW 정의, 해 표현, 목적함수, feasibility, ALNS 구조, destroy/repair, SA, adaptive weight, pseudo code | `01_problem_definition.md`, `03_alns_metaheuristic.md`, `05_local_search_moves.md` |
| `cvrptw_heuristic_strategy_summary.md` | 초기해 생성 전략, 포트폴리오, sequential/regret/savings/sweep, route elimination, route pool, MIP 연결 | `02_initial_solution_heuristics.md` |
| `cvrptw_hgs_alns_zone_summary.md` | CVRPTW 개요, HGS/ALNS 비교, zone 제약, 차량 부족, optional customer, dummy vehicle, 권장 설계 | `01_problem_definition.md`, `03_alns_metaheuristic.md`, `04_hgs_metaheuristic.md`, `06_practical_extensions.md` |
| `hgs_local_search_moves_summary.md` | Relocate, Swap, 2-opt, 2-opt*, Or-opt, Cross-exchange, SWAP*, delta evaluation, granular search | `05_local_search_moves.md` |
| `hgs_cvrp_swapstar_qa_summary.md` | HGS-CVRP 논문, giant tour, split, OX, population, penalty, SWAP*, 차량 부족, 미배정 확장, 실험 결과 | `04_hgs_metaheuristic.md`, `05_local_search_moves.md`, `06_practical_extensions.md`, `07_papers_and_benchmarks.md` |
| `HGS_CVRP_QA_정리.md` | HGS-CVRP 설명, local search, SWAP*, 차량 부족, 실무 확장 | `04_hgs_metaheuristic.md`, `05_local_search_moves.md`, `06_practical_extensions.md` |
| `alns_pdptw_paper_summary_ko.md` | PDPTW 정의, LNS/ALNS, Shaw/Random/Worst removal, Greedy/Regret insertion, SA, noise, 차량 수 최소화, 실험 결과 | `01_problem_definition.md`, `03_alns_metaheuristic.md`, `07_papers_and_benchmarks.md` |
| `alns_vrp_paper_lecture_summary.md` | ALNS operator review, removal/insertion ranking, frequency vs ablation, 설계 권장, 한계 | `03_alns_metaheuristic.md`, `07_papers_and_benchmarks.md` |

## 2. 주제별 반영 확인

| 주제 | 반영 상태 | 위치 |
|---|---|---|
| CVRPTW 정의 | 반영 | `01_problem_definition.md` |
| CVRP / VRPTW / PDPTW 관계 | 반영 | `01_problem_definition.md` |
| 입력 데이터 구조 | 반영 | `01_problem_definition.md` |
| Route list 표현 | 반영 | `01_problem_definition.md` |
| Giant tour 표현 | 반영 | `04_hgs_metaheuristic.md` |
| Split | 반영 | `04_hgs_metaheuristic.md` |
| 시간 계산과 waiting | 반영 | `01_problem_definition.md` |
| Capacity feasibility | 반영 | `01_problem_definition.md` |
| Time window feasibility | 반영 | `01_problem_definition.md` |
| Vehicle compatibility | 반영 | `01_problem_definition.md`, `06_practical_extensions.md` |
| 차량 수 우선 목적함수 | 반영 | `01_problem_definition.md`, `03_alns_metaheuristic.md` |
| Penalty objective | 반영 | `01_problem_definition.md`, `04_hgs_metaheuristic.md`, `06_practical_extensions.md` |
| Feasible-only vs infeasible 허용 | 반영 | `01_problem_definition.md`, `03_alns_metaheuristic.md`, `04_hgs_metaheuristic.md` |
| 초기해 역할 | 반영 | `02_initial_solution_heuristics.md` |
| Sequential insertion | 반영 | `02_initial_solution_heuristics.md` |
| Parallel regret insertion | 반영 | `02_initial_solution_heuristics.md` |
| Savings heuristic | 반영 | `02_initial_solution_heuristics.md` |
| Sweep / cluster-first | 반영 | `02_initial_solution_heuristics.md` |
| Nearest feasible / urgency-first | 반영 | `02_initial_solution_heuristics.md` |
| Portfolio / racing | 반영 | `02_initial_solution_heuristics.md` |
| Route elimination | 반영 | `02_initial_solution_heuristics.md` |
| Route pool / MIP 연결 | 반영 | `02_initial_solution_heuristics.md` |
| ALNS 구조 | 반영 | `03_alns_metaheuristic.md` |
| Random removal | 반영 | `03_alns_metaheuristic.md` |
| Worst removal | 반영 | `03_alns_metaheuristic.md` |
| Shaw / related removal | 반영 | `03_alns_metaheuristic.md` |
| Route removal | 반영 | `03_alns_metaheuristic.md` |
| Time window removal | 반영 | `03_alns_metaheuristic.md` |
| Sequence-based removal | 반영 | `03_alns_metaheuristic.md`, `07_papers_and_benchmarks.md` |
| Greedy insertion | 반영 | `03_alns_metaheuristic.md` |
| Regret-k insertion | 반영 | `02_initial_solution_heuristics.md`, `03_alns_metaheuristic.md` |
| Randomized / noise insertion | 반영 | `03_alns_metaheuristic.md` |
| Simulated Annealing acceptance | 반영 | `03_alns_metaheuristic.md` |
| Adaptive operator weight | 반영 | `03_alns_metaheuristic.md` |
| Removal size | 반영 | `03_alns_metaheuristic.md`, `07_papers_and_benchmarks.md` |
| ALNS pseudo code | 반영 | `03_alns_metaheuristic.md` |
| HGS 전체 구조 | 반영 | `04_hgs_metaheuristic.md` |
| OX crossover | 반영 | `04_hgs_metaheuristic.md` |
| Feasible / infeasible population | 반영 | `04_hgs_metaheuristic.md` |
| Dynamic penalty | 반영 | `04_hgs_metaheuristic.md` |
| Repair | 반영 | `04_hgs_metaheuristic.md`, `06_practical_extensions.md` |
| Diversity / clone 제거 | 반영 | `04_hgs_metaheuristic.md` |
| HGS vs ALNS 비교 | 반영 | `04_hgs_metaheuristic.md` |
| Relocate | 반영 | `05_local_search_moves.md` |
| Swap | 반영 | `05_local_search_moves.md` |
| 2-opt | 반영 | `05_local_search_moves.md` |
| 2-opt* | 반영 | `05_local_search_moves.md` |
| Or-opt | 반영 | `05_local_search_moves.md` |
| Cross-exchange | 반영 | `05_local_search_moves.md` |
| SWAP* | 반영 | `05_local_search_moves.md`, `04_hgs_metaheuristic.md` |
| Delta evaluation | 반영 | `05_local_search_moves.md` |
| Granular search | 반영 | `05_local_search_moves.md`, `07_papers_and_benchmarks.md` |
| Zone 제약 | 반영 | `06_practical_extensions.md` |
| Compatibility matrix | 반영 | `06_practical_extensions.md` |
| Zone-aware destroy/repair | 반영 | `06_practical_extensions.md`, `03_alns_metaheuristic.md` |
| 차량 부족 | 반영 | `06_practical_extensions.md`, `04_hgs_metaheuristic.md` |
| Optional customer | 반영 | `06_practical_extensions.md` |
| Dummy / outsourcing vehicle | 반영 | `06_practical_extensions.md` |
| Selective / Prize-Collecting VRPTW | 반영 | `06_practical_extensions.md` |
| Pre-check | 반영 | `06_practical_extensions.md` |
| ALNS PDPTW 논문 | 반영 | `07_papers_and_benchmarks.md` |
| HGS-CVRP 논문 | 반영 | `07_papers_and_benchmarks.md` |
| ALNS operator ranking 논문 | 반영 | `07_papers_and_benchmarks.md` |
| 논문 한계와 해석 주의점 | 반영 | `07_papers_and_benchmarks.md` |

## 3. 중복 정리 내역

| 중복 내용 | 정리 방식 |
|---|---|
| CVRPTW/CVRP 기본 정의가 여러 파일에 반복 | `01_problem_definition.md`로 통합 |
| ALNS 기본 흐름과 destroy/repair 설명 반복 | `03_alns_metaheuristic.md`로 통합 |
| Regret insertion 설명 반복 | 초기해 관점은 `02`, ALNS repair 관점은 `03`으로 분리 |
| HGS-CVRP 문서 2개의 giant tour, split, OX 설명 반복 | `04_hgs_metaheuristic.md`로 통합 |
| SWAP* 설명 반복 | 상세 move 설명은 `05`, HGS 내 역할은 `04`에 요약 |
| 차량 부족과 미배정 주문 설명 반복 | `06_practical_extensions.md`로 통합 |
| 논문별 기여/한계/실험 결과 반복 | `07_papers_and_benchmarks.md`로 통합 |

## 4. 누락 점검 결과

원본의 주요 주제는 모두 위 통합 문서 중 하나 이상에 반영했다.

의도적으로 축약한 항목:

- 원본의 긴 강의식 비유와 예시는 핵심 의미만 남겼다.
- 논문 실험표의 모든 숫자를 상세 재현하지 않고, 설계 판단에 필요한 결론 위주로 정리했다.
- 원본 pseudo code는 중복이 많은 부분을 통합 의사코드로 축약했다.

축약했지만 개념적으로 유지한 항목:

- PDPTW의 pickup-delivery precedence와 same vehicle 제약
- ALNS의 noise와 simulated annealing
- operator weight update와 segment 단위 갱신
- HGS의 infeasible population, repair, penalty 조정
- SWAP*의 top-k insertion position, route pair preprocessing, polar sector filtering
- optional customer, dummy vehicle, outsourcing, deferral 관점

