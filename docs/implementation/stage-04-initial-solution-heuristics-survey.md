---
title: Stage 4 — 초기해 construction 문헌 조사와 확장 후보 (근거 문서)
stage: 4
date: 2026-09-02
plan: ../implementation-plan.md
sources:
  - stage-04-initial-solution-heuristics.md (구현 계약 — 이 문서가 근거를 대는 대상)
  - ../domain-design.md (§4 이동표 MUST NOT, §8.3 사전식 비교, §9.2 shortlist, §9.3 재량)
  - ../orgin/cvrptw_heuristic_strategy_summary.md (종전 참고 자료 — 기본 8개의 출처)
  - stage-08-benchmark-comparison.md (§5 초기해 축소 판단의 재료)
revisions:
  - 2026-09-02 최초 작성 — 기본 8개(H1~H8)보다 정교한 construction을 찾기 위한 문헌·실전 솔버
    조사(4갈래, 2026-09-02 실시)와 그 결과의 채택/기각 판정. 채택 14개(H9~H22)는
    [heuristics 문서](stage-04-initial-solution-heuristics.md) §5에 의사코드로 편입했다 —
    **구현 계약은 그 문서에만 있고, 이 문서는 근거·출처·비교 프로토콜만 소유한다**
  - 2026-09-04 **§2.5에 H25 근거 추가** — H3 손실 형태(구역 조각남)를 겨누는 구역 간 교환.
    포트폴리오 24 → 25, §5 프로토콜의 수치를 25 → n으로. 설계·비교 프로토콜은
    [stage-04-h25](stage-04-h25-zone-quota-exchange.md)가 소유한다
  - 2026-09-02 **§2.5 실물 맞춤 2개(H23·H24) 근거 추가** — 실물 fixture 구조 실측(존×차급×부피)과
    설계 후보 3개의 같은 잣대 비교(현행 H3 15 재현 · C 27 · B 2 · A 3)를 기록. 포트폴리오 22 → 24,
    §5 프로토콜의 수치를 24로 갱신
---

# Stage 4 — 초기해 construction 문헌 조사와 확장 후보

**이 문서는 `4-초기해`의 근거 문서다 (2026-09-02).** 구현 계약(파일·시그니처·의사코드·테스트)은
[stage-04-initial-solution-heuristics.md](stage-04-initial-solution-heuristics.md)(이하 *heuristics 문서*)가
소유하고, 이 문서는 **왜 그 기법들인가**를 소유한다 — 조사 범위, 필터 기준, 후보별 출처와 판정,
기각 사유, 그리고 25 → n 축소를 위한 비교 프로토콜. 같은 규정을 두 곳에 적지 않는다: 의사코드·기권
규칙·복잡도 표는 heuristics 문서에만 있고, 여기서는 H번호로 가리킨다.

```text
[조사 4갈래 · 후보 ~45개]
   A 삽입·regret 정교화  B cluster/route-first·Split  C 이종 차대·차량 수·호환  D 최신 연구·실전 솔버
        │ §1 필터 7개 (결정성 · 방향성 표 · 오라클 · MIP/ML 없음 · route elimination 없음 · 종료 · pair)
        ▼
[채택 14개 → heuristics 문서 §5 H9~H22]      [기각·보류 ~30개 → §4 사유]
        │
        ▼
[T25 실측 + Stage 8 → 25개 중 몇 개만 남긴다 (§5 프로토콜, 사용자 결정)]
```

**결론 한 줄.** 문헌이 초기해 규칙 자체에서 근거 있는 개선을 보여 주는 곳은 (a) regret을 **현재
부분해에서의 삽입 가능 경로 수**와 결합하는 것, (b) seed·차량 순서로 **경로 수를 위에서 누르는** 것,
(c) 고정된 순열 위에서 경계를 **DP로 최적 분할**하는 것, (d) **시간창 호환성을 거리에 넣은**
클러스터링, (e) 경로 측이 요청을 고르는 **매칭 모드** — 다섯 축이고, 기본 8개에는 이 다섯이 전부
없다. 채택 14개는 이 다섯 축을 하나 이상씩 덮도록 골랐다.

---

## 0. 조사 범위와 방법

| 갈래 | 대상 | 확인 방식 |
|---|---|---|
| A | 삽입·regret 계열 정교화 (Solomon I1 계보, regret 변종, PDPTW SOTA 솔버의 초기해) | 원문 PDF 직접 확인: Bräysy & Gendreau 2005 리뷰, Ropke & Pisinger 2006 TechRep, Diana & Dessouky 2004, Sartori 학위논문 2019, Curtois 2018, Hiermann & Schiffer 2024 |
| B | cluster-first / route-first(Split) / sweep·petal / 시공간 클러스터링 | 원문 확인: Vidal 2016 Split, Hertrich 2019 sweep, Kerscher & Minner 2024/25, Dondo & Cerdá 2007, Renaud & Boctor 2000, Kim 2023 |
| C | 이종 차대·fleet mix·차량 수 최소화·site-dependent·다중 depot·DARP | 원문 확인: Koç 2016 survey, Renaud & Boctor 2000, Diana 2004, Belhaiza 2014, Detti 2016, Giosa/Tansini 2002·04, CIRRELT-2010-04 |
| D | 2018–2026 연구(대회 우승 솔버·학습 기반)와 실전 솔버 소스(OR-Tools·VROOM·jsprit·PyVRP·Timefold·LKH-3) | 소스·문서 직접 확인 (URL은 §6) |

**paywall로 초록·2차 문헌까지만 확인한 항목은 본문에 "(2차 문헌 기준)"으로 표시했다.**
그런 항목의 세부 수식은 채택 시 heuristics 문서가 우리 오라클에 맞춰 다시 정의했고, 원문의
수치를 우리 문제에 옮겨 주장하지 않는다.

## 1. 필터 — 이 저장소가 이미 고정한 것

후보를 거른 기준이다. 전부 기존 문서가 정한 것이고 이 조사가 새로 정한 것은 없다.

| # | 기준 | 근거 | 이 기준에 걸린 대표 후보 |
|---|---|---|---|
| F1 | **결정적** — 난수·멀티스타트·racing·noise 없음 | heuristics §1 고정 | randomized regret, GRASP, HGS/PyVRP의 random giant tour, jsprit noise, SISR blink, Nagata–Bräysy 원형 |
| F2 | 거리·시간은 **방향성 `TravelMatrix`만**. 좌표는 정렬·클러스터링에만, 대칭화 금지 | Domain §4 MUST NOT | Christofides(대칭 metric), Lu–Dessouky의 crossing 항, 유클리드 cone(FJ)의 거리 부분 |
| F3 | 후보 검증 = 호환 필터 + `RoutePropagator` + profile hard (`InsertionSearch`) — 그 오라클 위에 정의될 수 있어야 함 | heuristics §4.1 | HGS-VRPTW construction(위반 허용 후 penalty로 교정) |
| F4 | **MIP·column generation·ML 없음** | Master §4 · CLAUDE.md 범위 밖 | Fisher–Jaikumar GAP(MIP), Toth–Vigo Lagrangian 클러스터, Hiermann 2024 matching ILP, Kim 2023 RTV-ILP, 학습 기반 construction 전부 |
| F5 | construction 안에 **local search·route elimination 없음** (ALNS 몫) | 사용자 확정 D4 · heuristics §9 | Nagata–Bräysy ejection pool, Lim & Zhang EP, Russell 1995의 주기적 개선, Curtois 2018의 LS 초기해 |
| F6 | 종료는 **시간이 아니라 구조**로 보장 | heuristics §4.3 (D2·D3) | 반복 횟수가 문제 크기로 안 잡히는 것 (SWO는 고정 라운드 수로 통과) |
| F7 | 배정 단위는 `Request`(pair) 전체, `PICKUP_DELIVERY`는 픽업 선행 | Domain §1.4 | 단일 방문 전제 기법은 "요청 단위 순열"로 재정의 가능할 때만 통과 |

축 하나를 더 적어 둔다 — **삽입 규칙 안에서도 가중합을 쓰지 않는다**는 태도(heuristics §5 H2).
Domain §8.3은 평가·비교에만 적용되지만 heuristics 문서가 삽입 규칙까지 같은 태도를 택했으므로,
Solomon I1의 `α1·Δ거리 + α2·밀림`, jsprit의 `TW폭·계수 + depot거리·계수`, VROOM의 `λ`
격자는 **그대로 옮기지 않았다.** 대신 같은 단위(meter)끼리의 차(regret·savings)는 축을 뭉개는 것이
아니므로 허용했고(H11·H14), 서로 다른 단위는 사전식 키로 쌓았다(H13).

## 2. 조사 결과 총람

기호: ✔ 채택(H번호), △ 보류(트리거 있음), ✘ 기각. "근거"는 원문이 보고한 수치이고 우리 문제에서의
성능을 뜻하지 않는다 — 그것은 T25와 Stage 8이 잰다.

### 2.1 갈래 A — 삽입·regret 정교화

| 후보 | 출처 | 핵심 | 근거 | 판정 |
|---|---|---|---|---|
| regret-k + **삽입 가능 경로 수 최소 우선**, regret-m | Ropke & Pisinger 2006, *Transp. Sci.* 40(4); TechRep §3.2.2·§4.2 | 삽입 가능한 경로가 적은 요청부터; regret-m은 전 경로 합 | 50-request 16문제 단독 실행: regret-2/3/4/m gap 30.3/26.3/26.0/27.7%, **미배정 발생 3/3/2/0** — regret-m만 전량 배정 | ✔ **H9** |
| 시공간 seed(EPT 정렬 + 연쇄 배제) + parallel regret | Diana & Dessouky 2004, *TR-B* 38(6) | `LDT_k + TT(d_k, p_{k+1}) ≤ EPT_{k+1}`이면 k+1은 seed 제외; 나머지 regret-m | LA 파라트랜짓 500/1000요청: regret만으로 차량 −6~7%, seed 결합 **−8.17%** | ✔ **H10** (탈중심 가중 swap α는 가중치라 제외) |
| Solomon I1 c1/c2, Li & Lim·Sartori PDPTW 이식 | Solomon 1987; Sartori 학위논문 2019 §4.1; Bräysy & Gendreau 2005 식(1)–(6) | seed = 가장 이른 시작 허용 / 가장 먼 것; 선택 = `λ·d_0u − c1` 최대(단독 경로 대비 절감) | Solomon 56문제 CNV 453; Sartori & Buriol 2020 matheuristic의 실제 초기해 | ✔ **H11** (α 가중은 버리고 절감 항만 — 같은 단위) |
| Potvin & Rousseau 1993 parallel regret-m | *EJOR* 66(3) | I1 순차 실행으로 경로 수·seed 확정 후 병렬 regret-m | CNV 453 / CTD 78834, R·RC에서 I1보다 차량 적음 | ✘ H9·H10과 중복 (경로 수 사전 확정은 H10 seed가 담당) |
| Ioannou–Kritikos–Prastacos 2001 greedy look-ahead impact | *JORS* 52(5) (2차 문헌 기준) | 삽입이 **미배정 요청 전체**에 끼치는 영향(IU)까지 비용에 | Solomon **CNV 429 / CTD 67891** — construction 중 최고 | △ 비용 — IU 정확 계산은 반복당 `O(n²·m·L²)`. 근사(상위 k) 없이는 452건에서 부적합. Stage 8에서 시간 여유가 확인되면 재검토 |
| Lu & Dessouky 2006 slack-preserving insertion (+crossing) | *EJOR* 175(2) (2차 문헌 기준) | 거리 증분 + **시간창 여유 손실**을 비용에 | 초록: sequential/parallel insertion 대비 우수 | ✔ **H13** (crossing 항은 F2로 제외; slack은 forward slack으로 정의) |
| Antes & Derigs 1995 negotiation(bidding) | Univ. Köln TR; Bräysy & Gendreau 2005 Table 2 | 요청이 경로에 입찰 → **경로가 요청을 고른다**; 경로 수 −1 재실행 | Solomon **CNV 429 / CTD 71158** | ✔ **H12** (−1 재실행 루프는 제외 — F6·비용) |
| Van Landeghem 1988 bi-criteria savings | *EJOR* 36 | savings + 시간창 유연성 손실 | 수치 없음 | ✘ H7 비용식 변형에 불과 |
| Russell 1995 sector seed + 3 순서 규칙 | *Transp. Sci.* 29(2) | FJ seed + 주기적 개선 삽입 | CNV 424(개선 내장) | ✘ F5 |
| Hong & Park 1999 incompatibility seed | *IJPE* 62 | 상호 비호환 요청 집합을 seed | Potvin–Rousseau보다 약간 우수 | ✘ H10 seed와 축 중복 |
| Qi et al. 2012 spatiotemporal distance | *TR-E* 48(1) | 거리 + 최소 대기 시간의 가중합으로 k-medoids | Solomon 56 "promise" | △ → 정의는 H21이 Kerscher–Minner 식으로 흡수 (GA 클러스터링은 F1) |
| Jaw et al. 1986 ADARTW (EPT 순 순차) | *TR-B* 20(3) | earliest pickup 순으로 전 차량 최소 증분 삽입 | 2,600고객 실데이터 | ✘ H4의 정렬 키 변형 — H10 seed 정렬에 흡수 |
| SISR recreate 정렬 greedy | Christiaens & Vanden Berghe 2020; Hiermann & Schiffer 2024 §3 | 정렬 기준(random/far/close/tw-…) + blink | SOTA(ALNS 전체) | ✘ 정렬 greedy는 H4/H5와 같은 계열; blink는 F1 |
| Barbosa–Tiwari–Melo 2025 창 폭 순 greedy | arXiv:2511.07681 | pickup 창 폭 오름차순 순차 삽입 | 자체 인스턴스 | ✘ H2 + H4 조합, 새 축 아님 |
| Liu et al. 2019 convex-hull seed | IET CIM 1(3) (초록만) | hull 정점 seed | 미확인 | ✘ 근거 부족 |

### 2.2 갈래 B — cluster-first · route-first(Split) · sweep · 시공간

| 후보 | 출처 | 핵심 | 근거 | 판정 |
|---|---|---|---|---|
| **Split** (giant tour + DP 최적 분할) | Beasley 1983; Prins 2004 *C&OR* 31; Vidal 2016 *C&OR* 69 (`O(n)`); Prins 2009 제한 fleet; Gibbons 2026 arXiv:2601.17572 (SPD+TW) | 순열 σ 고정 → 보조 DAG 최단 경로 = 최적 경계 | Prins 2004: Christofides 14문제에서 당시 tabu 동급; 이후 HGS의 표준 부품 | ✔ **H18·H19** (순열 규칙 2종: Hilbert / 방향성 NN) |
| 공간채움곡선(Hilbert) 순서 + Optimal Partitioning | Bartholdi & Platzman 1988 *Mgmt Sci* 34(3); Bowerman–Calamai–Hall 1994 *EJOR* 76(1) | Hilbert 값 정렬 = TSP 근사 `O(N log N)`; 여기에 Split | 최적 대비 ~25% 이내, 속도 압도 | ✔ **H18** 의 순열 |
| Request 단위 Split (PDP) | Velasco et al. 2009 (초록); Prins–Lacomme–Prodhon 2014 리뷰 | 순열 원소 = pair | PDP 단독 수치 부족 | ✔ H18·H19의 pair 규칙 |
| 시간 지향 sweep + Window-wise / Corrective sweep | Solomon 1987; Hertrich–Hungerländer–Truden 2019 arXiv:1901.02771 | 제로각 = 최대 각도 간극, **양방향**, 부채꼴은 실현성으로 확장, 목표 사전식(차량, 소요, 이동) | n=2000: TW 병목에서 **35.0대 vs 42.1대** | ✔ **H20** (양방향·간극 제로각·오라클 확장; window-wise 각도는 재량으로 미루고 1차는 simple sweep) |
| Petal (1-/2-petal + column-circular 최단경로 선택) | Foster & Ryan 1976; Renaud–Boctor–Laporte 1996 *JORS* 47; Renaud & Boctor 2002 *EJOR* 140 | sweep 순서 위 모든 연속 구간을 후보로, 다항식 선택 | Golden 20(이종 fleet) best-known **+0.49%** (CW +15.13%, giant-tour +1.92%) | △ 1-petal은 H19(sweep 순열 Split)와 동치. 2-petal 열거는 비용 대비 근거가 fixture(단일 depot·동질 용량)에 약해 보류 |
| 시공간·수요 거리 + **agglomerative** 클러스터링 | Kerscher & Minner 2024 arXiv:2402.00041 = 2025 *TR-E* 204 (식 11–14) | `S = D·(2 − (f−h)/(l_w−e_w) + (q_i+q_j)/Q)`, 방향성 min; agglomerative는 초기화 무관 → 결정적 | GH 600–1000: "deterministic clustering이 우월" 명시, HGS-TW 대비 단시간 우위 | ✔ **H21** |
| Dondo & Cerdá 2007 TW 호환 hyper-node | *EJOR* 176(3) | 거리 임계 + 시간창 교집합 갱신으로 클러스터 | Solomon C·RC 우수 (MILP 결합) | △ H21과 축 중복(시간창 호환 클러스터). H21이 약하면 교체 후보 |
| Fisher–Jaikumar seed cone + 비MIP 배정 | FJ 1981; Koskosidis 1992; Bramel & Simchi-Levi 1995 | 누적 수요 cone으로 K seed, regret 배정 | FJ가 CW·sweep보다 우수 | ✘ 좌표 cone은 유클리드 전제, K 사전 고정은 H10 seed로 대체 |
| Linehaul/backhaul 매칭 (PICKUP_ONLY × DELIVERY_ONLY) | Toth & Vigo 1999 *EJOR* 113(3); Deif & Bodin 1984; Nagy & Salhi 2005 | 배달-only 경로와 집하-only 경로를 Hungarian 매칭으로 결합 | VRPB 당시 최선 | △ **트리거: 혼합 패턴 입력** — 실물 fixture는 DELIVERY_ONLY뿐이라 지금은 전부 기권해 비교에 기여하지 못한다 |
| PD midpoint subtractive 클러스터링 | Chiu 1994; Martins 2020 (Li & Lim) | pair midpoint 밀도로 자동 K | 초기해 자체는 NN 대비 개선 없음 | ✘ 근거 부족(개선 후에만 우위) |
| 다중 depot 배정 (urgency = 차선 depot − 최근접) | Cordeau–Gendreau–Laporte 1997; Giosa–Tansini–Viera 2002 *JORS* 53 | 요청→depot 선배정 | SPA가 6개 중 최선 | △ **트리거: `depots.size() > 1` 입력** |
| 시간 분해(rolling horizon) | Kim et al. AAAI 2023 arXiv:2303.03475; Bent & Van Hentenryck 2010 | 시간창 순 슬라이스, 겹침 재고 | 2,500요청에서 OR-Tools GLS가 못 찾는 해 산출 | ✘ 부분 solver가 ILP(F4); 삽입으로 바꾸면 H2의 정렬 키 변형 |
| Santini et al. 2023 route-based 분해 | *INFORMS JoC* 35(3) | 기존 해의 경로 barycenter를 k-means++ | ALNS/HGS 모두 최선 | ✘ construction 아님(기존 해 입력)·k-means++는 F1 — ALNS 대규모 분해 후보로 stage-04-alns에 넘길 가치 |

### 2.3 갈래 C — 이종 차대 · 차량 수 · 호환성 · 다중 depot

| 후보 | 출처 | 핵심 | 근거 | 판정 |
|---|---|---|---|---|
| Golden 1984 CS/OOS/**ROS** savings | *C&OR* 11(1); Renaud & Boctor 2000 §2; Koç 2016 survey | 차량 승급 고정비를 merge 기준에 | Golden 20에서 savings 계열 최선(ROS-γ) | ✘ 우리 목적에 차량 고정비 축이 없다 — 병합은 항상 경로 −1이고 "차량 크기"는 점수 축이 아니다. profile이 차량 비용 축을 갖게 되면 H7 비용식 변형으로 재검토 |
| Giant tour + 차량형 라벨 Split | Golden MGT; Prins 2009; Duhamel DFS-Split arXiv:2211.11816 | 제한 fleet Split | Golden 20에서 (MGT+Or-opt) 전체 최선 | ✔ H18·H19의 DP가 **차량 순서 상태**로 제한 fleet을 다룬다 |
| Dullaert 2002 I1 + vehicle insertion savings | *JORS* 53(11) (2차 문헌 기준) | I1 c1에 차량 승급 고정비 항 | Liu & Shen 168 인스턴스 | ✘ 고정비 축 없음; 순차 I1은 H11 |
| Chao–Golden–Wasil 1999 site-dependent 형별 partition-first | *INFOR* 37(3) (2차 문헌 기준) | 허용 차량형 집합으로 군집 후 형별 VRP | Nag 인스턴스에서 우수(2차) | ✘ H1 scarcity·H15 difficulty와 같은 신호(호환 집합)를 쓰는 다른 포장 |
| 하한으로 경로 수 선결정 + 병렬 삽입 | Gheysens 1986; Potvin & Rousseau 1993; MPFIH 2009/2016 | FFD 하한만큼 seed를 미리 열고 채움 | Potvin–Rousseau R1 13.33대 vs I1 13.58대 | ✘ H10 seed(연쇄 배제)와 역할 중복; 두 seed 규칙을 다 넣으면 §7 다양성 원칙 위반 |
| EPT 순차 + schedule block slack (Jaw 1986; Detti 2016; REBUS 1995) | *TR-B* 20(3); arXiv:1611.05187; *Ann. OR* 60 | EPT 정렬·호환 필터·최소 증분 | Detti: 결정적 초기해 + VNS gap 3.59% vs 랜덤 1000회 3.33% — **결정적 초기해로 충분** | ✘ H4 정렬 키 변형 (근거는 §5의 "결정적으로 충분하다"에 인용) |
| Nagata & Bräysy 2009 ejection pool (결정적 변형) | *Oper. Res. Lett.* 37(5); Lim & Zhang 2007 *IJoC* 19(3) | 경로 하나 제거 → EP → insertion-ejection | GH 1000: 60분에 best-known 합 근접 — route elimination 최강 | ✘ **F5 (D4)**. 단, 가장 강한 차량 수 축소 수단이므로 stage-04-alns의 destroy/repair 후보로 기록할 가치가 있다 |
| Squeaky Wheel Optimization (구성→blame→재정렬) | Lim–Lim–Rodrigues 2002 (PDPTW); Lim & Zhang 2007; Joslin & Clements 1999 | 이전 구성 결과로 우선순위를 갱신해 재구성 | Lim & Zhang이 SWO 초기해로 차량 수 최상위 | ✔ **H22** (라운드 수 고정 — F6 통과; 난수 없음 — F1 통과) |
| 다중 depot urgency 배정 | Cordeau 1997; Giosa 2002 | 위 2.2와 동일 | — | △ (2.2와 동일 트리거) |
| Type-route-cover (Makansi & Savla 2024) | arXiv:2406.07719 | 형별 경로 중복 생성 후 greedy cover | 시뮬레이션만 | ✘ 근거 약함 |
| Belhaiza 2014 다중 TW 최소 backward slack | *C&OR* 52 | 창 선택 DP | Favaretto 대비 −15.63% | ✘ 전파(Stage 3)가 이미 창 미룸을 담당 — construction 사항 아님 |
| Bent & Van Hentenryck 2004 사전식 (경로 수, delay, −Σ\|r\|²) | *Transp. Sci.* 38(4) | 작은 경로를 더 작게 몰아 비우기 | SA 자체는 랜덤 | ✘ 동률 규칙 아이디어로만 기록 |

### 2.4 갈래 D — 최신 연구와 실전 솔버

| 후보 | 출처 | 핵심 | 근거 | 판정 |
|---|---|---|---|---|
| **VROOM** 차량 순서 sequential fill + "남은 차량 대비" regret | VROOM `src/algorithms/heuristics/heuristics.cpp`, `vrptw.cpp` | 차량을 (max_tasks, capacity, TW 길이, range) 내림차순으로 하나씩 채움; `cost − λ·regret`, regret = 뒤 차량들 중 단독 비용 최소; INIT seed 4종; (INIT×λ) **32조합 결정적 병렬 → best** | 전 문제군 기본 전략. 이종 fleet은 dynamic vehicle choice | ✔ **H14** (λ=1 고정 — 같은 단위의 차; INIT=HIGHER_AMOUNT. λ 격자·다른 INIT은 Stage 8 변종) |
| **Timefold** difficulty × strength (WEAKEST_FIT_DECREASING) | Timefold docs `construction-heuristics` | 엔티티를 difficulty 내림차순, 값을 strength 오름차순 — **강한 차량을 어려운 요청에 남긴다** | 기본 construction; "정적 속성만으로 비교" | ✔ **H15** |
| **OR-Tools** `PATH_MOST_CONSTRAINED_ARC` | `routing_enums.proto`, `search.cc`, `routing.h` comparator | 경로 끝에서 arc를 **확장**; comparator = mandatory → 허용 차량 수(도메인 크기) → arc cost → index | `AUTOMATIC`이 "허용 차량 1대인 노드가 있으면" 고르는 전략 | ✔ **H16** (path-extension 계열이 포트폴리오에 없다) |
| OR-Tools `PARALLEL_CHEAPEST_INSERTION` (+ farthest seeds ratio) | `search.cc` `GlobalCheapestInsertionFilteredHeuristic` | 전역 최저 (요청, 위치)부터; pair는 동시 삽입 비용; `cheapest_insertion_farthest_seeds_ratio` | PD 문제의 `AUTOMATIC` 기본 | ✔ **H17** (regret-1 = 전역 최저 — H1/H2와 다른 해) |
| OR-Tools `LOCAL_CHEAPEST_INSERTION` (farthest-first 병렬) | `search.h` | depot에서 먼 노드부터 전 경로 최저 위치 | — | ✘ H4의 정렬 키 변형(farthest) — H17 seed에 흡수 |
| OR-Tools SAVINGS/SWEEP/CHRISTOFIDES | proto | — | — | ✘ H7·H8과 동치 / Christofides는 F2 |
| jsprit RegretInsertion 점수 | `RegretInsertion.java`, `DefaultScorer.java` | `(11−priority)·(2nd−1st) + tw계수·minTimeToOperate + depot계수·거리` | jsprit 기본 construction | ✘ 가중합(§1 태도); TW·depot 항의 취지는 H2·H17이 사전식으로 덮는다 |
| HGS-VRPTW(ORTEC, EURO-NeurIPS baseline) sweep + 60% fill + TW-split | `Population.cpp` | 짧은 TW는 EDD, 긴 TW는 cheapest; 위반 허용 | population의 5% | ✘ F3 (위반 허용 후 penalty 교정) |
| PyVRP / HGS-CVRP 초기해 | `solve.py`, `Solution.cpp` | random + LS 하나 | — | ✘ F1 |
| LKH-3 | `ChooseInitialTour.c`, `SOP_InitialTour.c` | WALK(pseudo-random); precedence는 topological fringe | — | ✘ F1 (fringe 개념만 H16의 pair 처리에 차용) |
| 대회 우승 솔버 (DIMACS 2022 ORTEC · EURO-NeurIPS 2022 Kleopatra/OptiML · Amazon 2021 LKH-AMZ) | Kool et al. 2023 PMLR 220; arXiv:2112.15192 | construction에 투자 없음 — random + 강한 LS. OptiML은 baseline의 nearest/furthest/sweep construction을 **제거하고도** static 1위 | 차량 무제한·동질 fleet·분 단위 예산 | ✘ 전제가 우리와 다르다 (§5에 인용) |
| 학습 기반 construction (AM 2019, POMO 2020, LEHD 2023, GLOP 2024, RouteFinder 2024, Li 2022 PDP-HAM) | arXiv 다수 | — | — | ✘ F4 — 런타임 모델·GPU 필요, rich 제약 일반화 안 됨, feasibility는 결국 손으로 짠 오라클 |

### 2.5 갈래 E — 실물 구조 맞춤 (2026-09-02 실측, H23·H24)

문헌이 아니라 **실물 fixture(`data/win_poc_case_floor.json`, 주문 452·차량 31)의 구조 실측**에서 나온
갈래다. 실측은 저장소 밖 python 스크래치로 했고(규약 JSON 어댑터는 Stage 6), 수치는 여기에만 남는다.

**구조 실측.**

| 항목 | 값 |
|---|---|
| 주문 부피 합 / 차량 부피 합 | 278.28 / 290.57 CBM — **여유 12.29 CBM (4.2%)**. 무게는 54% |
| 허용 차급 | 존과 거의 1:1 — ZONE_19(60건)는 ≤T1.9만(13대), ZONE_15·17은 ≤T2.5, ZONE_22·23·24는 ≤T3.5, ZONE_29(87건·60.4 CBM·depot 250~360 km)와 ZONE_25(2건)만 T5 가능 |
| 시간창 | 05:45–10:30 282건(도심 존) · 05:45–13:30 169건(원거리 존) · duration 300초 전건 |
| 존 폭 | ZONE_29 존 내부 이동 중앙값 41 km·52분 — 부피만 보면 시간창을 어긴다 |
| Win 엔진 결과(`data/alns_result.csv`)의 존→차급 | 미사용 부피 12.29 = 여유 전부. ZONE_21 = T2.5×2+T3.5(34.30 vs 34.18) · ZONE_19 = T1×3+T1.4+T1.9 · ZONE_29 = T3.5×2+T5×2 · ZONE_25 = T1 |
| H3(`vehicle-zone-fill`, 실측 15 미배정)의 손실 | 차량 하나씩 존을 고르다 존이 조각나고 뒤에 남은 T1 4대가 반만 찬다(부피 1.9~3.0/4.9) |

큰 차의 갈 곳이 정해져 있으니 "존에 어떤 차급 조합을 주는가"가 곧 미배정 수이고, 여유가 4.2%라
조합은 전역으로 맞춰야 한다 — 이것이 heuristics §5 H23·H24 공통 부품(존 배정 DP)의 근거다.

**설계 후보 비교 (같은 잣대: 부피·무게·정차 28·최근접 순회로 시간창 근사).** 현행 H3 규칙을 같은
잣대로 돌리면 실측과 같은 15가 나와 잣대가 실물과 맞는 것을 확인했다.

| 후보 | 핵심 | 미배정 | 존별 | 판정 |
|---|---|---:|---|---|
| 현행 H3 | 차량 외곽 루프 + 최선 존 커밋 | 15 | ZONE_15 8 · 19 5 · 23 1 · 24 1 | 잣대 보정 기준 |
| C: H3 개선판 | what-if 정렬을 지배 비율(max(vol, weight 비))로 | 27 | ZONE_15 14 · 19 11 · 24 2 | ✘ 정렬 키만으로는 존 조각남이 안 풀린다 |
| B: 존 배정 DP + 정차 예산 best-fit + 1-1 교환 | 전역 DP로 존→유형 대수, 존 내부는 부호 있는 "남을 부피" 규칙 | **2** | ZONE_29 2 | ✔ **H23** |
| A: 존 배정 DP + bin별 subset-sum DP + 오라클 피드백 | 같은 DP, 존 내부는 seed pool 위 부분집합 부피 DP | **3** | ZONE_29 3 | ✔ **H24** |

민감도: B는 요청 순서 첫 키가 "호환 유형 수 ASC"여야 한다(부피순만이면 13 — 여유 0.12 CBM인 ZONE_21이
안 풀림). A는 정차 하한 비례 상수(0.9/0.8/없음 → 3/5/8)와 pool 배율(1.5 → 3, 2.0 → 11)에 민감하다.
중간 단계 근거: 존 배정을 존 순서 greedy로 하면 32, 호환 그룹 검사(Hall 조건)를 뺀 DP는 T5를 ≤T1.9
전용 존에 보내 180+, 존 내부를 부피 BFD로 하면 60, 정차 예산 규칙 11, subset-sum(정차 하한) 0.

**Java 정식 평가 실측 (2026-09-02, 구현 후 · 저장소 밖 스크래치 실행기 · 24개 전부 실행).**

| 기법 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 |
|---|---:|---:|---:|---:|---:|
| H3 `vehicle-zone-fill` (종전 1위) | 15 | 31 | 4,515,433 | 1,003,093 | 136 ms |
| **H23 `zone-quota-balanced-fill`** | **0** | 31 | 4,198,408 | 1,002,069 | 507 ms |
| H24 `zone-quota-subset-fill` | 8 | 31 | 3,942,905 | 974,992 | 773 ms |
| 24개 합계 | | | | | 14.3 s (H21 7.0 s) |

H23이 best로 선정됐고 경로별 감사(구역 1종·차급·부피·무게·정차 28·시간창·reqDate) 31/31 PASS — Win 엔진
결과와 같은 452건 전량·31대다. 근사 잣대의 예측(B 2 · A 3)과 순위가 같고, 오라클이 최근접 순회보다 좋은
경로를 만들어 B는 근사보다 좋아졌다. A는 원거리 분산 존(ZONE_29·24)에서 8건이 남았다.

둘 다 넣는 이유는 §7 원칙 그대로다 — 공통 부품은 하나(`ZoneQuotaAllocation`)지만 존 내부 축이
다르고(2차원 균형 greedy vs 정확한 부분집합 DP), 어느 쪽이 실물·다른 입력에서 남을지는 T25·Stage 8
실측이 정한다. 존 배정 DP의 규모 한계(차종 조합 수 > 65,536에서의 기권)는 2026-09-04에 제거됐다 —
[stage-04-zone-quota-allocation-scaling](stage-04-zone-quota-allocation-scaling.md).
**판정은 Java 정식 평가뿐이다** — 위 표는 근사이고 heuristics §8 T38~T43과 실물 실행이
계약이다.

**H25 `zone-quota-exchange-fill` (2026-09-04 편입).** 위 표의 H3 손실 15건은 "채우기"가 아니라
**"어느 구역에 어떤 차를 주는가"의 결정 범위**에서 났다 — 차 한 대씩 존을 그 자리에서 확정하니
존이 조각나고 마지막 T1 4대가 반만 찬다. 반만 찬 차를 다른 구역의 차와 맞바꾸거나 남는 구역의 차를
모자란 구역으로 옮기는 단계는 24개 어디에도 없었다(H23의 1-1 교환은 **한 구역 안에서 주문 둘**을
맞바꾸는 것이라 한 층 아래다). H25는 그 층을 연다 — 존 배정을 greedy로 덮은 뒤 구역 간 MOVE·SWAP으로
고치고, 구역 안 채우기는 H23의 2단계를 그대로 부른다. 근사 잣대(위 "중간 단계 근거")로는 존 순서
greedy 32 · H3 15 · DP 2였고, **교환이 이 사이 어디에 떨어질지는 재 봐야 안다** — 그래서 만들고 잰다.
설계·근거·비교 프로토콜은 [stage-04-h25](stage-04-h25-zone-quota-exchange.md)가 소유하고, 잔류·폐기는
그 §8 결정 표를 보고 사용자가 정한다. 25개 전부 실행하는 것은 그대로다.

## 3. 채택 14개 — 축과 우선순위

heuristics 문서 §5의 H9~H22다. **어느 축을 새로 여는가**가 채택 기준이었고 (§7 원칙: 비슷한 정책을
여럿 넣으면 결과가 갈리지 않는다), 같은 축의 후보는 하나만 남겼다.

| H | id | 새로 여는 축 | 출처 (§2) | 구현 순서 |
|---:|---|---|---|:---:|
| H9 | `feasible-routes-regret-m` | regret 폭 m + **동적** 삽입 가능 경로 수 우선 (H1의 정적 희소도와 대비) | Ropke & Pisinger 2006 | 1차 |
| H10 | `chain-seed-regret-m` | **seed로 경로 수 하한**을 박는 시간 연쇄 배제 | Diana & Dessouky 2004 | 1차 |
| H11 | `i1-savings-sequential` | 선택 기준 = **단독 경로 대비 절감** (H5의 최소 Δ거리와 대비) | Solomon 1987 · Sartori & Buriol 2020 | 1차 |
| H12 | `route-bidding` | **경로 측이 요청을 고르는** 매칭 모드 | Antes & Derigs 1995 | 1차 |
| H13 | `slack-preserving-sequential` | 비용축 = **시간창 여유 손실** | Lu & Dessouky 2006 | 2차 |
| H14 | `vehicle-fill-remaining-regret` | 차량 순서 sequential + **남은 차량 대비** regret | VROOM | 1차 |
| H15 | `weakest-fit-decreasing` | **difficulty × strength** — 강한 차량을 아낀다 | Timefold | 1차 |
| H16 | `constrained-path-extension` | **path extension**(끝에 잇기) 계열 | OR-Tools PATH_MOST_CONSTRAINED_ARC | 2차 |
| H17 | `farthest-seed-global-cheapest` | **regret-1(전역 최저)** + farthest seed 비율 | OR-Tools PCI | 2차 |
| H18 | `hilbert-split` | 순열 고정 → **DP 최적 경계**; 순열 = Hilbert(국소 밀집 보존, depot 무관) | Bartholdi & Platzman 1988 · Beasley/Prins Split | 1차 |
| H19 | `nearest-neighbor-split` | 같은 DP, 순열 = **방향성 표 위 NN** (좌표 불필요) | Prins 2004 | 2차 (H18의 순열만 교체) |
| H20 | `gap-sweep-bidirectional` | sweep을 **전 패턴·양방향·오라클 확장**으로 (H8은 DELIVERY_ONLY·용량 next-fit) | Hertrich et al. 2019 | 2차 |
| H21 | `spatiotemporal-cluster` | **시간창 호환성이 거리에 든** 결정적 클러스터링 (H6의 zone 경계와 대비) | Kerscher & Minner 2024/25 | 1차 |
| H22 | `squeaky-wheel-sequential` | 우선순위를 **이전 구성 결과로 갱신** (H1의 정적 희소도와 대비) | Lim–Lim–Rodrigues 2002 | 2차 |

"1차/2차"는 구현 순서의 권고일 뿐 계약이 아니다 — 1차 8개는 (근거 강함 · 축 신규 · 기존 부품 재사용)
셋을 다 만족하고, 2차 6개는 그중 하나가 약하다(H13은 `Candidate` 필드 추가가 필요하고, H16·H17은
값싸지만 근거가 "솔버 기본값"뿐이며, H19는 H18의 변종, H20은 H8과 겹치고, H22는 비용이 H4의 R배다).
**둘 다 T25의 실측 대상이다** — 2차라서 비교에서 빠지지 않는다.

기존 8개와 합쳐 22개의 계열 분포: parallel-regret 4 (H1·H2·H9·H10) · sequential 5 (H4·H5·H11·H13·H22) ·
vehicle-outer 3 (H3·H14·H15) · matching 1 (H12) · extension 1 (H16) · global-cheapest 1 (H17) ·
split 2 (H18·H19) · cluster-first 3 (H6·H20·H21) · merge 1 (H7) · sweep 1 (H8).

## 4. 기각·보류 요약

§2 표의 ✘·△를 사유별로 모은 것이다. 보류(△)는 **트리거**가 있고, 발동하면
[Stage Extra](stage-extra-deferred-features.md)에 등재하는 것이 아니라 이 문서를 개정해 채택으로 바꾼다
(초기해 기법은 Stage 4 안의 재량이라 Extra 등재 대상이 아니다).

| 사유 | 후보 |
|---|---|
| F1 결정성 | randomized/GRASP 계열, HGS·PyVRP random 초기해, SISR blink, jsprit BestInsertion(shuffle), Nagata–Bräysy 원형, Santini k-means++ |
| F2 방향성 표 | Christofides, Lu–Dessouky crossing 항, FJ cone |
| F3 오라클 위 정의 불가 | HGS-VRPTW construction(위반 허용) |
| F4 MIP/ML | FJ GAP, Toth–Vigo Lagrangian, Hiermann 2024 matching ILP, Kim 2023 RTV-ILP, 학습 기반 전부 |
| F5 route elimination/LS | Nagata–Bräysy EP, Lim & Zhang EP, Russell 1995, Curtois LS 초기해 → **stage-04-alns 연산자 후보로 기록** |
| 축 중복 | Potvin–Rousseau(H9·H10), Hong & Park(H10), Jaw/Detti/Barbosa/LCI(H4·H17 정렬 키), Chao SDVRP(H1·H15), Gheysens 하한 seed(H10), Van Landeghem(H7), Dullaert(H11) |
| 목적 축 없음 | Golden ROS·Dullaert의 차량 고정비 항 — profile이 차량 비용 축을 갖게 되면 재검토 |
| 근거 부족 | Liu 2019 convex hull, Martins 2020 midpoint, Makansi 2024 cover |
| **보류 (트리거)** | Ioannou impact(**Stage 8 시간 여유**), Petal 2-petal(**H19가 약할 때**), Dondo hyper-node(**H21이 약할 때**), Linehaul/backhaul 매칭(**혼합 패턴 입력**), 다중 depot urgency(**`depots.size() > 1` 입력**), VROOM λ 격자·INIT 변종(**Stage 8 파라미터 실험**) |

## 5. 25 → n 비교 프로토콜

[Stage 8 §5](stage-08-benchmark-comparison.md)가 정한 원칙(재료는 T25, 자르는 것은 사용자)을 그대로
쓰되, 기법이 25개가 되어 재료의 형식을 정한다. 이 절은 **재료를 정하는 것이지 결과를 예단하지 않는다.**

**문헌이 이 비교에 대해 말해 주는 것 두 가지.**

1. 강한 LS와 분 단위 예산·차량 무제한 환경에서는 construction이 무의미하다 (EURO-NeurIPS 2022 OptiML —
   construction 제거 후 1위; PyVRP 0.13+ random 시작). 반대로 **유한·이종 fleet + hard feasibility +
   짧은 예산**의 production 솔버(VROOM 32조합, OR-Tools 17전략, jsprit)는 결정적 포트폴리오를 쓴다.
   우리는 후자다 — 미배정이 점수 1번 축, 차량 수가 2번 축이라 초기해가 그 둘을 잡지 못하면 ALNS가
   지우기 어려운 차이가 남는다. 그래서 25개를 다 재 보는 것이 맞고, 그 다음에 자른다.
2. 결정적 초기해 하나로 충분하다는 직접 증거가 있다 — Detti & Zabalo 2016: 결정적 초기해 + VNS gap
   3.59% vs 랜덤 초기해 1000회 3.33%. 난수 멀티스타트를 거부한 §1 고정이 근거 없는 선택이 아니다.

**재료 (T25가 기법마다 출력).**

| 열 | 뜻 | 출처 |
|---|---|---|
| `elapsedMillis` | 기법 소요 | `ConstructionOutcome` |
| `unassignedCount` · `score[]` | 정식 평가 결과 | 〃 |
| 순위 | 25개 중 `Scores.compare` 순위 | T25 계산 |
| best 선정 여부 | 실물 규모 실행에서 `heuristicId`로 뽑힌 적이 있는가 | Stage 8 §5 |
| 총 소요 / `timeLimitSec` | 포트폴리오 전체가 ALNS 예산의 몇 배인가 | T25 |

**자르는 순서 (권고).** ① 어느 실행에서도 상위 8위에 못 든 기법 → 제외 후보. ② 남은 것 중 같은 계열
(§3 분포)에서 둘 이상이면 순위 낮은 쪽 제외 — 계열 다양성을 남긴다. ③ 소요가 총 소요의 30%를 넘는데
best가 아니면 제외. ④ 4개가 남을 때까지 반복하되, **H4(baseline)는 순위와 무관하게 마지막까지 표에
남겨** 다른 기법이 baseline보다 나쁜지 보는 기준으로 쓴다(단 최종 4개에 들 필요는 없다).
이 순서는 사용자가 바꿀 수 있고, 최종 결정은 Stage 8 §5대로 사용자가 한다.

## 6. 출처 (직접 확인한 것)

논문·리뷰:
Bräysy & Gendreau 2005 *Transp. Sci.* 39(1) Part I (https://cepac.cheme.cmu.edu/pasi2011/library/cerda/braysy-gendreau-vrp-review.pdf) ·
Ropke & Pisinger 2006 TechRep (https://backend.orbit.dtu.dk/ws/portalfiles/portal/3154899/) ·
Diana & Dessouky 2004 *TR-B* 38(6) (https://sites.usc.edu/maged/) ·
Sartori 2019 학위논문 (https://lume.ufrgs.br/bitstream/handle/10183/194380/001093046.pdf) ·
Sartori & Buriol 2020 *C&OR* (https://doi.org/10.1016/j.cor.2020.105065) ·
Vidal 2016 Split (https://arxiv.org/abs/1508.02759) · Prins 2004 *C&OR* 31 (https://www.sciencedirect.com/science/article/pii/S0305054803001588) ·
Bartholdi & Platzman 1988 (https://pubsonline.informs.org/doi/10.1287/mnsc.34.3.291) · Bowerman et al. 1994 (https://www.sciencedirect.com/science/article/abs/pii/0377221794900116) ·
Hertrich et al. 2019 (https://arxiv.org/abs/1901.02771) · Kerscher & Minner 2024 (https://arxiv.org/abs/2402.00041) ·
Renaud & Boctor 2000/2002 (https://www.cirrelt.ca/documentstravail/2000/2000-015.pdf) · Dondo & Cerdá 2007 (https://doi.org/10.1016/j.ejor.2005.06.047) ·
Koç et al. 2016 survey (https://eprints.soton.ac.uk/378863/1/ThirtyYears.pdf) · Detti & Zabalo 2016 (https://arxiv.org/pdf/1611.05187) ·
Giosa–Tansini–Viera 2002 (https://link.springer.com/article/10.1057/palgrave.jors.2601426) · Kim et al. 2023 (https://arxiv.org/abs/2303.03475) ·
Hiermann & Schiffer 2024 (https://arxiv.org/pdf/2405.00230) · Curtois et al. 2018 (https://link.springer.com/article/10.1007/s13676-017-0115-6) ·
Nagata & Bräysy 2009 (https://www.sciencedirect.com/science/article/abs/pii/S0167637709000662) · Lim & Zhang 2007 (https://pubsonline.informs.org/doi/abs/10.1287/ijoc.1060.0186) ·
Joslin & Clements 1999 SWO (https://arxiv.org/abs/1105.5454) · Kool et al. 2023 EURO-NeurIPS 보고 (https://proceedings.mlr.press/v220/kool23a/kool23a.pdf) ·
Amazon 2021 LKH-AMZ (https://arxiv.org/pdf/2112.15192) · Santini et al. 2023 (https://pubsonline.informs.org/doi/10.1287/ijoc.2023.1288).

솔버 소스·문서:
OR-Tools `routing_enums.proto` · `ortools/routing/search.cc` · `search.h` · `routing.h` (https://github.com/google/or-tools) ·
VROOM `src/algorithms/heuristics/heuristics.cpp` · `src/problems/vrptw/vrptw.cpp` · `src/structures/vroom/vehicle.h` (https://github.com/VROOM-Project/vroom) ·
jsprit `algorithm/recreate/RegretInsertion.java` · `DefaultScorer.java` · `box/Jsprit.java` (https://github.com/graphhopper/jsprit) ·
PyVRP `pyvrp/solve.py` · `Solution.cpp` (https://github.com/PyVRP/PyVRP) · HGS-VRPTW baseline `Population.cpp` (https://github.com/ortec/euro-neurips-vrp-2022-quickstart) ·
Timefold construction heuristics (https://docs.timefold.ai/timefold-solver/latest/optimization-algorithms/construction-heuristics) ·
LKH-3 `SRC/ChooseInitialTour.c` · `SOP_InitialTour.c` (https://github.com/cerebis/LKH3) · SINTEF Li & Lim 벤치마크 (https://www.sintef.no/projectweb/top/pdptw/).

초록·2차 문헌까지만 확인: Ioannou et al. 2001 *JORS* 52(5) · Lu & Dessouky 2006 *EJOR* 175(2) · Antes & Derigs 1995 ·
Solomon 1987 *OR* 35(2) · Potvin & Rousseau 1993 *EJOR* 66(3) · Lim–Lim–Rodrigues 2002 AMCIS · Chao–Golden–Wasil 1999 ·
Dullaert et al. 2002 · Toth & Vigo 1999 · Velasco et al. 2009 · Christiaens & Vanden Berghe 2020 (SISR 본문).
