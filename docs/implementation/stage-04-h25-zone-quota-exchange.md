---
title: Stage 4 — H25 `zone-quota-exchange-fill` 설계안 (greedy 존 배정 + 구역 간 교환, H3 비교용)
stage: 4
date: 2026-09-04
status: 설계안 — 구현 전. 계약 문서(stage-04-initial-solution-heuristics.md)는 §9의 목록대로 **구현 직전에** 개정한다
plan: ../implementation-plan.md
sources:
  - stage-04-initial-solution-heuristics.md (§3.2 SPI · §3.4 존 배정 · §4.1~§4.4 공통 기반 · §5 H3/H23/H24 의사코드 · §7 X · §8 T)
  - stage-04-initial-solution-heuristics-survey.md (§2.5 근사 잣대 실측: 존 순서 greedy 32 · H3 15 · DP 2 · §3 "새로 여는 축" · §5 비교 프로토콜)
  - ../notes/initial-solution-heuristics-h1-h24.md (§4 병렬 포트폴리오 선정 검토 — H3 제외 사유 · §4.3 검증 실행 (a)(b))
  - solver-core `ZoneQuotaAllocation`·`VehicleZoneFillConstruction`·`ZoneQuotaBalancedFillConstruction`·`InitialSolutionBuilder` (2026-09-04 디스크)
  - stage-04-zone-quota-allocation-scaling.md (§2.1 ① 값 정의 · §3 `value`·`Allocation.truncated` — 이 설계가 공유하는 부품. **구현 순서: scaling 먼저, H25 다음**)
revisions:
  - 2026-09-04 최초 작성 — 사용자 확정("greedy + 교환은 직접 구현해 보고 H3과 비교")의 설계. 코드·계약 무변경
  - 2026-09-04 사용자 결정 반영 — §10 Q1~Q5 확정(MAX_SCANS 50 · **best-improvement**, 품질 같으면 first · T25 합성 변형 · T56b는 실측 후 · 25번째).
    §4.3 의사코드를 best-improvement로, §3 `value` 시그니처·값 정의를 scaling 설계 §3·§2.1 ①과 같은 문장으로. T45~T48 → T53~T56, X24~X29 → X28~X33 (scaling 설계가 T45~T52·X24~X27을 씀)
---

# Stage 4 — H25 `zone-quota-exchange-fill` 설계안

## 0. 한 장 요약

- **무엇.** 초기해 기법 하나를 더한다 — 구역(존)마다 차종을 몇 대 줄지를 **greedy로 정한 뒤, 구역끼리 차를
  옮기거나 맞바꿔 개선**하고, 구역 안은 H23의 채우기를 **그대로 재사용**한다. id `zone-quota-exchange-fill`, 번호 H25.
- **왜.** 두 질문에 실측으로 답하기 위해서다. ① H3(차 한 대씩 구역을 확정)의 손실 15건은 "구역 조각남 · 마지막
  T1 4대 반만 참"이라는 형태였다 — 구역 간 교환이 정확히 그것을 겨눈다. 얼마나 메우는지는 **모른다**. ② H23의
  DP는 차종 조합 수 Π(대수+1) > 65,536이면 기권한다 — 그때 대체가 H3(15건급)인지 이 기법인지는 실측이 정한다.
- **위치.** H3는 손대지 않는다. H25는 별도 기법으로 25번째(우선순위 마지막)에 두어 같은 실행에서 H3·H25·H23이
  나란히 나오게 한다. **기권하지 않는다** — Π와 무관하게 돈다.
- **정직한 전제.** H23의 기권을 없애는 개정(희소 DP + 폭 제한)이 별도로 설계되고 있다. 그 개정이 끝나면 H25가
  포트폴리오에 남을 이유는 **§8 비교 결과뿐**이다. 이 문서는 "H25가 필요하다"를 전제하지 않는다.

---

## 1. 왜 필요한가

### 1.1 H3의 손실 형태

survey §2.5의 실측이 H3의 15건이 어디서 왔는지를 보여 준다: 차 한 대를 잡고 그 차에 가장 잘 맞는 구역을
**그 자리에서 확정**하니, 앞차가 구역을 반쯤 가져가고 뒤차가 나머지를 받는 식으로 **구역이 조각나고**,
마지막에 남은 T1 4대가 반만 찼다(부피 1.9~3.0 / 4.9). 같은 데이터에서 "구역 → 차종 대수"를 한꺼번에 정하는
H23은 0건이다. 즉 H3의 손실은 "채우기"가 아니라 **"어느 구역에 어떤 차를 주는가"의 결정 범위**에서 났다.

### 1.2 교환이 그것을 겨눈다는 논리

반만 찬 T1을 다른 구역의 차와 맞바꾸거나, 남는 구역의 차 한 대를 모자란 구역으로 옮기는 것이 "구역 간 교환"이다.
H3에는 이 단계가 없고, H23의 1-1 교환은 **한 구역 안에서 주문 둘을 맞바꾸는** 것이라 한 층 아래다.
24개 어디에도 구역 단위로 차를 옮기는 단계는 없다.

### 1.3 "훨씬 나은지는 모른다 → 그래서 잰다"

논리로 말할 수 있는 건 여기까지다.

- 실물의 실패 형태가 단순해서(반 찬 차 몇 대) 1:1 교환이 잡을 가능성이 높다 → 15보다는 좋아질 것 같다.
- 그러나 여유 4.2%에서 0을 만들려면 ZONE_21처럼 여유 0.12 CBM짜리 조합(T2.5×2 + T3.5)을 **정확히** 맞춰야 하고,
  차 한 대씩 옮기는 이웃으로는 못 닿는 국소 최적이 있을 수 있다. DP는 전역이라 0이 나왔다.
- survey §2.5 근사 잣대: 존 순서 greedy 32 · H3 15 · DP 2. 교환이 이 사이 어디에 떨어질지는 재 봐야 안다.

이 저장소는 이런 질문에 논리가 아니라 실측으로 답한다(survey §5). **그래서 만든다** — 남길지는 §8이 정한다.

### 1.4 H3와의 차이 (한눈에)

| | H3 `vehicle-zone-fill` | H25 `zone-quota-exchange-fill` |
|---|---|---|
| 바깥 루프 | 차량 (까다로운 차부터) | 구역 (H23의 존 순서) |
| 구역 → 차 배정 | 차 한 대마다 what-if로 최선 구역 1개 확정. 되돌리지 않음 | 구역마다 greedy 덮개 → **구역 간 이동·맞바꿈으로 개선** |
| 배정의 값 | 적재율(부동소수, 정렬 키) | `(Σ부족, Σ낭비, Σ사용 대수)` 사전식 — **H23 DP와 같은 값** |
| 구역 안 채우기 | what-if 삽입 결과를 그대로 (weight DESC 순 최소 비용) | **H23 2단계 재사용** — 정차 예산 best-fit + 1-1 교환 + leftover pass |
| 기권 | 없음 | 없음 |
| 존 부재 | 단일 그룹으로 퇴화 | 단일 존 `(none)`으로 퇴화 (H23과 동일) |

---

## 2. 파일/클래스 목록

전부 `solver-core/src/main/java/com/ronext/rpdptw/solve/`. 하위 패키지 없음(계약 §2).

| 파일 | 상태 | 책임 한 줄 |
|---|---|---|
| `solve/ZoneQuotaExchangeFillConstruction.java` | **신규** | H25 — greedy 존 배정 + 구역 간 교환(§4.1~§4.3) → H23 채우기 호출 |
| `solve/ZoneQuotaAllocation.java` | 기존 · **추출만** | DP 전이 안의 값 계산을 `value(demand, types, s)`로 꺼낸다(§3). 이 추출은 scaling 설계가 먼저 하고(§3 ①의 "호환 유형만 합산" 포함), H25는 그것을 부른다 — DP와 H25가 **같은 값 함수**를 쓰기 위함 |
| `solve/ZoneQuotaBalancedFillConstruction.java` | 기존 · **추출만** | `construct`의 "배정 뒤" 부분을 `fill(problem, profile, allocation)`로 꺼낸다(§3). 동작 무변경 |
| `solve/InitialSolutionBuilder.java` | 기존 · 1줄 | `defaults()` 끝에 H25 추가 (25번째) |

새 파일은 1개다. `Solution`·`Route`·`InsertionSearch`·`Evaluator`·`Problem`·`Profile`은 수정하지 않는다.
`ZoneQuotaAllocation.Demand`는 **재사용한다** — `Demand.of(problem, types, zone)`·`covers(s)`·`totalVolume`이
package-private이라 같은 패키지의 H25가 그대로 부를 수 있다(확인: `static final class Demand`, `static Demand of(...)`,
`boolean covers(int[] s)`). 단 **`Demand.cached(radix)`는 부르지 않는다** — 그것이 `byte[Π]`를 잡는 곳이라
H25가 부르면 기권 없는 설계가 무너진다(§6 X30).

---

## 3. 시그니처

```java
public final class ZoneQuotaExchangeFillConstruction implements ConstructionHeuristic {
    static final String ID = "zone-quota-exchange-fill";
    /** 재량 상수 — 교환 스캔 라운드 상한. 개선 없는 스캔 1회면 그 전에 끝난다 (§5). */
    static final int MAX_SCANS = 50;

    public String id();
    public boolean abstains(Problem problem);            // 항상 false
    public Solution construct(Problem problem, Profile profile);

    /** greedy 덮개 + 구역 간 교환으로 만든 배정 — 반환형은 DP와 같은 Allocation (§4.1~§4.3). */
    static ZoneQuotaAllocation.Allocation allocate(Problem problem);

    /** 스캔 기록·비교용 오버로드 — 스캔마다 (Σ부족, Σ낭비, Σ사용 대수)를 남기고(T54) 스캔 규칙을 고른다(§8 재료 ④). H22의 priorityTrace와 같은 수법. */
    static ZoneQuotaAllocation.Allocation allocate(Problem problem, List<long[]> valueTrace, ScanRule rule);
    /** BEST = 스캔마다 최선 이웃 하나 적용 (기본, §4.3) · FIRST = 처음 개선되는 이웃 적용 후 재스캔 (비교 전용). */
    enum ScanRule { BEST, FIRST }
}

final class ZoneQuotaAllocation {
    // 기존 — 무변경 (vehicleTypes·zones·allocate 진입점). Allocation은 scaling 설계가 truncated 필드를 더한다 — H25는 false로 만든다
    static List<VehicleType> vehicleTypes(Problem problem);
    static List<Zone> zones(Problem problem, List<VehicleType> types);
    static Allocation allocate(Problem problem);

    /**
     * 추출 — 존 하나에 유형별 대수 s를 줬을 때의 값 {부족, 낭비, 사용 대수}.
     * 용량 합 = s에 든 유형 중 **존 호환 유형만** Σ_t s_t·maxVolume_t (scaling 설계 §2.1 ①과 같은 문장).
     * 부족 = covers면 0, 아니면 max(1, Σvolume_z − 용량 합) · 낭비 = covers면 용량 합 − Σvolume_z, 아니면 0.
     * DP 전이(기존 allocate 안의 인라인 식)와 H25 교환이 같은 함수를 쓴다. 시그니처는 scaling 설계 §3과 동일.
     */
    static long[] value(Demand demand, List<VehicleType> types, int[] s);
}

public final class ZoneQuotaBalancedFillConstruction implements ConstructionHeuristic {
    // 기존 construct(problem, profile) = fill(problem, profile, ZoneQuotaAllocation.allocate(problem))
    /** 추출 — 배정이 주어졌을 때의 존 내부 적재 + 1-1 교환 + leftover pass (계약 §5 H23 의사코드의 "배정 =" 아래 전부). */
    static Solution fill(Problem problem, Profile profile, ZoneQuotaAllocation.Allocation allocation);
}
```

- `RandomGenerator` 인자는 어디에도 없다(계약 §3 결정성 보증 그대로).
- `Allocation`을 그대로 반환하는 이유: `fill`이 `allocation.zones()`·`vehiclesByZone()`·`types()`만 보므로
  배정을 만든 쪽이 DP든 greedy+교환이든 구분하지 않는다. **H23 2단계가 두 기법에서 한 코드**라는 것이 §8 비교의 전제다.
- `fill` 추출 시 `checkOuterLoop`의 id 인자는 호출한 기법의 id를 넘긴다(예외 메시지에 H23/H25가 구분되게) —
  시그니처에 `String id`를 하나 더 두거나, H25가 자기 id로 감싸는 것 중 구현 재량. **전제:** 추출은 동작 무변경이고
  T40·T44가 그것을 잡는다.

---

## 4. 의사코드

기호는 계약 §5와 같다. `types`·`zones`·`Demand`·`covers`·존 순서·유형 순서는 **H23 공통 부품을 그대로** 쓴다 —
H25가 새로 정의하는 것은 (1) greedy 덮개 (2) 교환 이웃과 순회 순서 (3) 종료뿐이다.

### 4.1 준비 (H23 공통)

```text
types = ZoneQuotaAllocation.vehicleTypes(problem)      // 유형 순서 (maxVolume ASC, maxWeight ASC, 첫 VehicleId ASC)
zones = ZoneQuotaAllocation.zones(problem, types)      // 존 순서 (호환 유형 수 ASC, Σvolume DESC, zoneId ASC)
demand_z = Demand.of(problem, types, z)  for z in zones   // cached()는 부르지 않는다 (§2)
remaining[t] = |types[t].vehicles|
s_z = 0 벡터  for z in zones                            // 존별 유형 대수 — 이것이 "배정"이다
pool = 미배정 차량 — s에 안 든 대수. 값 기여 0. 교환에서 존처럼 다룬다 (§4.3)
compat_z = { t : types[t].compatible ∩ z.members ≠ ∅ }   // 그 존에 한 건이라도 실을 수 있는 유형
```

### 4.2 greedy 덮개 (존 순서대로 1회)

survey §2.5의 "존 순서 greedy(제약 많은 존 우선·낭비 최소)"를 **프론티어 나열 없이** 선형으로 다시 쓴 것이다.
DP의 프론티어 나열은 Π에 비례할 수 있어 기권 없는 기법이 쓸 수 없다(§6 X30).

```text
for z in zones:                                                      (존 순서 고정)
  s = 0
  add = compat_z를 유형 순서 DESC로 (큰 차부터)                        ← 빨리 덮는다
  while !covers_z(s):
    added = false
    for t in add:                                                     (라운드 로빈 — 호환 그룹마다 언젠가 호환 유형이 든다: Hall 조건)
      if remaining[t] > s[t]: s[t] += 1 ; added = true
      if covers_z(s): break
    if !added: break                                                  (공급 부족)
  if !covers_z(s): s = 0                                              ← DP의 (c)와 같은 규칙: 못 덮는 존에 차를 쏟아붓지 않는다.
                                                                        부분 배정은 교환(§4.3)이 pool에서 채운다
  trim: for t in 유형 순서 ASC (작은 차부터):
          while s[t] > 0 && covers_z(s − e_t): s[t] −= 1              ← 최소 덮개로 깎는다 = DP 프론티어 (a)
  s_z = s ; remaining −= s
```

- 라운드 로빈으로 큰 차부터 한 대씩 더하는 이유: 유형 하나만 몰아 넣으면 그 유형과 호환이 없는 그룹(예: ≤T1.9
  전용 그룹)을 영영 못 덮는다. 호환 유형을 돌아가며 넣으면 `covers`의 Hall 조건이 언젠가 만족되거나 공급이 바닥난다.
- trim은 작은 차부터 뺀다 — 큰 차를 남겨 두는 편이 덮개를 유지하기 쉽고, 낭비는 교환이 줄인다.
- **전제:** 이 greedy가 survey의 근사 잣대 greedy(32)와 같은 답을 내지는 않는다 — 그 잣대는 저장소 밖 스크래치였고
  프론티어 나열을 썼다. 여기서는 "교환의 출발점"만 맡는다. 출발점의 품질은 T54이 스캔 기록으로 보이고, 최종 품질은 §8이 잰다.

### 4.3 구역 간 교환 (best-improvement, 순회 순서 고정)

```text
V(s) = Σ_z value(demand_z, types, s_z)      // {Σ부족, Σ낭비, Σ사용 대수} — DP 값과 같은 함수 (§3)
      pool의 값 기여는 0
nodes = zones ++ [pool]                       // 순회 대상. pool은 마지막
이웃 두 종류 (모두 유형 t·t′는 유형 순서 ASC로 순회):
  MOVE(A → B, t):   s_A[t] −= 1, s_B[t] += 1        조건 s_A[t] > 0 · t ∈ compat_B (B = pool이면 항상)
  SWAP(A ↔ B, t, t′): s_A[t] −= 1, s_A[t′] += 1, s_B[t′] −= 1, s_B[t] += 1
                       조건 s_A[t] > 0 · s_B[t′] > 0 · t ≠ t′ · t′ ∈ compat_A · t ∈ compat_B (pool 쪽 조건은 항상)
Δ는 A·B 두 존의 value만 다시 계산한다 — covers 호출 2번(SWAP은 2번, MOVE도 2번).

scans = 0
loop:
  scans += 1 ; if scans > MAX_SCANS: break                            (§5 종료 — 정상, 예외 아님)
  best = null                                                         (이번 스캔의 최선 이웃)
  for A in nodes: for B in nodes, B ≠ A:                              (순서 고정)
    for t in 유형 순서:  MOVE(A→B, t) 시도 → V′ 계산
    for t, t′ in 유형 순서: SWAP(A↔B, t, t′) 시도 → V′ 계산
      각 시도에서: V′가 V보다 사전식으로 **엄격히** 작고 (best == null 또는 V′ < V_best 엄격) 이면 best = 그 이웃
                  ← 동률(V′ == V_best)은 먼저 만난 이웃 유지 — 순회 순서가 곧 동률 규칙 (결정성)
  if best == null: break                                              (국소 최적 — 스캔 1회에 개선 0)
  best 적용 ; valueTrace += V(s)
배정 = 존별 s_z → 차량 목록 (유형 순서 × 유형 안 VehicleId ASC로 소비 — DP의 역추적 뒤와 같은 규칙)
return Allocation(types, zones, vehiclesByZone, truncated = false)
```

- **best-improvement**(스캔마다 이웃 전체를 보고 가장 좋은 하나만 적용)를 고른 이유 — 사용자 결정(2026-09-04):
  "일반적으로 해 품질이 낫고 시간 차이는 크지 않다. 품질 차이가 없으면 first-improvement로." 스캔당 이웃 수는
  실물 규모에서 ≈ 7,100(§5)이라 전체를 보는 비용이 작고, 결정성은 순회 순서로 동률을 깨면 first와 똑같이 고정된다.
  **first-improvement와의 품질 차이는 §8 재료 ④가 잰다** — 같으면 first로 바꾼다(§10 Q2).
- 비교를 위해 스캔 규칙을 인자로 받는 package-private 오버로드를 둔다(§3 `allocate(problem, valueTrace, rule)`).
  기본 진입점은 best 하나이고, first는 그 비교 실행에서만 돈다.
- pool을 존처럼 다루면 "남는 구역의 차를 pool로 돌려 낭비를 줄이기"(MOVE A→pool)와 "모자란 구역에 pool의 차를 주기"
  (MOVE pool→A), "큰 차를 pool의 작은 차로 갈아태우기"(SWAP A↔pool)가 이웃 정의 하나로 다 나온다.
- Σ사용 대수가 값의 3번 축이라 MOVE A→pool은 낭비가 같아도 대수가 줄면 채택된다 — DP와 같은 축 순서다.

### 4.4 2단계 — H23 채우기 재사용

```text
construct(problem, profile):
  allocation = allocate(problem)                                       (§4.1~§4.3)
  return ZoneQuotaBalancedFillConstruction.fill(problem, profile, allocation)
```

**이 선택의 장단.** 장: H25 vs H23의 차이가 "greedy+교환 vs DP" **하나로 고립**되고, 두 기법의 배정 값 `V`가 같은
함수라 배정 자체도 수치로 비교된다(§8 재료 ①). 단: H25 vs H3의 차이에는 "교환의 가치"에 **"채우기 차이"(H23 2단계 vs
H3 what-if 삽입)가 섞인다.** 그래서 H25 > H3 결과만으로 "교환 덕"이라고 말할 수 없다. §8은 배정 값 `V`를 따로 기록해
그 둘을 분리한다 — 배정 값이 좋아졌으면 교환 덕, 배정 값은 같은데 최종 점수만 좋으면 채우기 덕이다.
"H3 2단계 재사용" 변형(H3의 what-if 삽입 위에 H25 배정)은 만들지 않고 §10 미결로 남긴다.

---

## 5. 종료·상한 (계약 §4.3 형식)

```text
예외 상한 — 루프가 아니라 구조가 유한한 기법:
  H25 greedy: 존마다 덧셈 ≤ m (매 덧셈이 remaining 1 소비) + trim ≤ m ⇒ ≤ 2·m·Z
  H25 교환: 스캔 ≤ MAX_SCANS (재량 50) · 스캔당 이웃 ≤ (Z+1)²·(T + T²) · 이웃당 covers 2회
           채택은 V의 사전식 엄격 감소만 — 같은 배정으로 돌아오지 않는다 (순환 없음)
  H25 2단계: H23과 동일 ≤ 3n (fill 재사용)
```

- 교환의 참 상한은 "V가 유한 격자 위에서 엄격 감소"이지만 그 격자는 부피 단위(×1000 long)라 카운터로 못 쓴다.
  그래서 H22의 R=5와 같은 수법으로 **스캔 수를 재량 상수로 자른다.** 상한 도달은 X28대로 정상 종료다 — 개선 도중에
  잘린 근사이고, 그 사실은 `valueTrace` 길이 = MAX_SCANS로 드러난다.
- 방어 카운터: greedy 덧셈이 `2·m·Z`를 넘으면 IllegalStateException(계약 §4.3 X13과 같은 취급). 교환은 스캔 상한이
  그 자체로 카운터다.
- 시간 상한 없음 (계약 D2·Q3).

복잡도 표 행 (계약 §5 표에 들어갈 것):

| # | id | 계열 | 지원 패턴 | 바깥 루프 | 반복당 작업 |
|---:|---|---|---|---|---|
| H25 | `zone-quota-exchange-fill` | zone-quota | 전 패턴 | greedy ≤ 2mZ · 스캔 ≤ 50 · fill ≤ 3n | greedy `O(Z · m · T · G)` + 교환 `O(스캔 · Z² · T² · G)` (G = 호환 그룹 수) + fill `O(m_z · L²)` |

실물 규모(Z ≈ 12, T = 6, G ≤ 6)에서 스캔당 이웃 ≈ 13² × 42 ≈ 7,100 · covers 2회 → 스캔 50회여도 DP의 전이 3.3M보다
작다. **전제:** H25의 배정 단계는 H23의 ~500 ms보다 빠를 것이다 — §8 재료 ③이 확인한다.

---

## 6. Edge case (계약 §7에 이어 붙일 것)

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| X28 | H25 교환이 MAX_SCANS에 도달 | 정상 종료 — 그때까지의 최선 배정으로 2단계 진행. 예외 아님. `valueTrace` 길이로 관측 | §5 |
| X29 | H25 greedy에서 어떤 존을 공급 부족으로 못 덮음 | 그 존은 s = 0 (DP (c)와 동일). 교환이 pool에서 MOVE로 부분 배정을 만들 수 있고, 그래도 남으면 fill의 leftover pass가 기존 경로·미사용 차량에 삽입 (X21과 같은 경로) | §4.2 |
| X30 | 차량 유형 조합 수 Π(대수+1) > 65,536 (H23·H24 기권 입력) | H25는 **기권하지 않고** 돈다. `Demand.cached`·프론티어 나열을 쓰지 않으므로 Π 크기 배열이 없다 — 이것이 기권 없음의 근거 | §2·§4.2 |
| X31 | 존이 하나도 없음 (`zoneId` 전부 부재) | 단일 존 `(none)` — greedy가 전 차량 중 최소 덮개를 고르고 나머지는 pool. H23과 같은 퇴화 | 계약 §4.4 |
| X32 | 정차 한도 부재 | `covers`의 방문 수 축이 +∞로 처리됨(기존 `Demand.compute`) · fill은 X23대로 부피 best-fit 퇴화. 기권 없음 | 계약 X23 |
| X33 | MOVE/SWAP 이웃이 하나도 조건을 만족하지 않음 (예: 차량 1대) | 스캔 1회에 개선 0 → 즉시 종료. greedy 배정 그대로 | §4.3 |

X3(전원 기권)의 기권 가능 목록은 바뀌지 않는다 — H25는 거기 들지 않는다.

---

## 7. 테스트 (계약 §8에 이어 붙일 것 — 현재 최대 T44)

위치·의존 규칙은 계약 §8 그대로(JUnit만 · 손 조립 `Problem` · 실물 fixture는 app 모듈).

| # | 테스트 | 내용 | 대응 |
|---|---|---|---|
| T53 | `ZoneQuotaExchangeFillConstructionTest.exchangeReachesDpValueOnHallFixture` | T38의 입력(큰 차 1·작은 차 2 · 큰 차 금지 존 A·자유 존 B)에서 H25 `allocate`의 최종 `V`가 DP `allocate`의 `V`와 **같다** — greedy가 큰 차를 잘못 두더라도 SWAP이 바로잡는다. `value()` 추출이 DP 결과를 바꾸지 않았음도 같은 테스트가 잡는다(DP `V`가 추출 전 기대값과 동일) | §4.3·§3 |
| T54 | `ZoneQuotaExchangeFillConstructionTest.scansStrictlyImproveAndTerminate` | greedy가 부족을 남기고 MOVE 한 번으로 풀리는 입력(존 A에 작은 차 2대 필요·B에 1대 필요·작은 차 3대, greedy 순서상 B가 2대를 먼저 가져가는 수치): `valueTrace`가 사전식 **엄격 감소**하고 길이 ≤ MAX_SCANS · 두 번 실행해 trace 동일 (X8) · 이웃이 없는 입력(차량 1대)에서 trace 길이 1 (X33) | §4.3·§5 |
| T55 | `ZoneQuotaExchangeFillConstructionTest.neverAbstainsBeyondCombinationLimit` | T39의 입력(유형이 서로 다른 17대 → Π = 2¹⁷)에서 H23 `abstains` true · H25 `abstains` false · H25 `construct` 정상 종료·`StructureCheck` 통과·Feasible (X30) | §6 X30 |
| T56 | `WinPocFixtureTest.zoneQuotaExchangeFillComparedOnRealFixture` (**app 모듈**) | 실물 fixture에서 **같은 실행 안에** H3·H25·H23을 돌려 기법마다 (미배정, 차량, 거리, 운행시간, 소요)와 H25·H23의 배정 값 `V`를 **표로 출력**한다. 단언은 H25의 `StructureCheck`·Feasible·재실행 동일 score(X8)·경로 감사(T44의 `audit` 재사용)뿐. **`H25 ≤ H3`(사전식) 단언은 넣지 않는다** — 실측 후 §8이 정한다(넣으면 T56b) | §8 |

T17·T19·T20(24개 전부 순회)은 목록이 `defaults()`이면 자동으로 25개를 돈다 — 문구만 "25개"로.
T15의 기권 목록·T16은 무변경(H25는 기권하지 않음).

---

## 8. 비교 프로토콜 (survey §5 형식)

**재료** — 세 입력에서 각각 H3·H25·H23을 같은 실행으로:

| 입력 | 출처 | 보는 것 |
|---|---|---|
| ① 실물 fixture | T56 | 본 비교 |
| ② 실물에서 `zoneId`를 지운 변형 | notes §4.3 (a) | 존 없을 때 세 기법의 퇴화형 |
| ③ H23이 기권하는 입력 | T55의 17대 입력을 실물 규모로 늘린 합성(전제: T25 합성 문제의 차량을 전부 다른 유형으로) | **H25가 존재하는 이유** — 이때 H3 vs H25만 남는다 |

기록 열: 기법 · 미배정 · 차량 · 거리 · 운행시간 · 소요 · (H25·H23) 배정 값 `V = (Σ부족, Σ낭비, Σ대수)` · H25 스캔 수.

**결과별 결정** (①이 기준, ②③은 보조):

| ① 결과 (사전식) | 결정 |
|---|---|
| H25 = H23 (미배정·차량 동률) | H25가 **H23 기권 시 대체**로 확정. 기권 없는 DP 개정(별도 설계)의 우선순위를 낮춘다 — 대체가 이미 DP급이므로. H3는 notes §4.2대로 제외 유지 |
| H3 < H25 < H23 | H25가 H23 기권 시 대체(H3 대신). DP 개정은 그대로 진행 — 기권 자체를 없애는 편이 낫다. 병렬 포트폴리오 5개에는 **들지 않는다** (H23이 있는 한 같은 축) |
| H25 ≤ H3 | **H25 폐기** — 이 문서에 폐기 표시, 코드 삭제, 계약 §2·§5·§7·§8 되돌림. H3가 기권 시 대체로 남는다 |
| H25 배정 값 `V` = H23 `V`인데 최종 점수는 H23이 좋음 | 배정은 같으나 fill 안의 동률 처리·순서가 다르다는 뜻 — **버그 후보**(fill이 같은 코드이면 같은 결과여야 한다). 원인을 찾고 나서 위 표를 적용 |

- "훨씬 낫다"의 기준은 두지 않는다 — 사전식 비교 하나뿐이다(계약 §6).
- ②에서 순위가 ①과 뒤집히면 notes §4.2 축 ① 표에 그 사실을 적는다. ②는 H25의 채택 여부를 바꾸지 않는다.
- ③에서 H25 ≤ H3이면 ①의 결과와 무관하게 "기권 시 대체" 역할은 H3에 남는다.
- 시간(소요)은 채택 여부를 바꾸지 않는다.
- **재료 ④ — 스캔 규칙 비교.** 같은 세 입력에서 `ScanRule.BEST`와 `FIRST`의 최종 점수·배정 값 `V`·스캔 수·소요를
  나란히 잰다. 사용자 결정(2026-09-04): 품질(점수 사전식)이 같으면 FIRST로 바꾸고 BEST와 오버로드를 지운다.
  BEST가 한 입력에서라도 좋으면 BEST 유지.

---

## 9. 계약 문서 개정 목록 (구현 직전에 — 이 순서로)

이 문서는 설계안이라 계약을 바꾸지 않는다. 구현 세션은 아래를 **먼저** 개정하고 코드를 쓴다(CLAUDE.md 규칙).

| 문서 | 어디 | 어떻게 |
|---|---|---|
| `stage-04-initial-solution-heuristics.md` | frontmatter `revisions` | 한 줄: "2026-09-XX H25 `zone-quota-exchange-fill` 편입 — greedy 존 배정 + 구역 간 교환, H23 2단계 재사용, 기권 없음. 포트폴리오 24 → **25개**, 축소 목표 25 → n. 근거 [stage-04-h25](stage-04-h25-zone-quota-exchange.md)" |
| 〃 | 서두 문단 · §3.1 주석 "24개" · §6 "24개" | "24개(기본 8 + 확장 14 + 실물 맞춤 2)" → "25개(… + 실물 맞춤 3)" |
| 〃 | §2 파일 표 | 행 추가: `solve/ZoneQuotaExchangeFillConstruction.java` — H25. `ZoneQuotaAllocation`·`ZoneQuotaBalancedFillConstruction` 행의 책임 줄에 "(`value`/`fill` 추출 — H25 공용)" |
| 〃 | §3.4 | 이 문서 §3의 `value`·`fill`·H25 시그니처 추가 |
| 〃 | §4.3 예외 상한 | "H25 greedy ≤ 2mZ · 교환 스캔 ≤ 50(재량) · fill ≤ 3n" 한 줄 |
| 〃 | §4.4 기권 절 마지막 불릿 | "실물 맞춤 H23·H24는 … 기권한다" 뒤에 "H25는 Π와 무관하게 기권하지 않는다 (X30)". "24개 전부 실행된다" → "25개" |
| 〃 | §5 표·계열 분포·의사코드 | 표에 이 문서 §5의 행 · 계열 분포 "zone-quota 2 (H23·H24)" → "zone-quota 3 (H23·H24·H25)" · H24 의사코드 뒤에 "### H25" 절 = 이 문서 §4 |
| 〃 | §7 | X28~X33 추가 (이 문서 §6) |
| 〃 | §8 | T53~T56 추가 (이 문서 §7) · T17·T19·T20 문구 "24개" → "25개" |
| 〃 | §10 Q1 | "24개 중" → "25개 중". 처리 문구에 "H25의 잔류 여부는 [stage-04-h25 §8](stage-04-h25-zone-quota-exchange.md) 비교가 정한다" |
| `stage-04-initial-solution-heuristics-survey.md` | §2.5 끝 | 문단 추가: H3 손실 형태(구역 조각남) → 구역 간 교환 = H25, 근거는 이 문서 §1. "24개 전부 실행" → "25개" |
| 〃 | §5 제목·본문 | "24 → 4" → "25 → n"(n은 notes §4.4의 5 + 조건부) · 재료 표 "24개 중 순위" → "25개" |
| `stage-04-alns.md` | revisions·§3.4 주석·§4 절차 1·그림 | "24개" → "25개" (4곳: 17·132·257·327·356행 부근) |
| `domain-design.md` | §9.3 (1188행 부근)·revisions | "24개(기본 8 + 확장 14 + 실물 맞춤 2)" → "25개(… 3)" — 규칙 무변경 한 줄 |
| `implementation-plan.md` | revisions·Stage 4 DoD (324·349·353·361행 부근) | "24개" → "25개", "24 → 4" → "25 → n" |
| `CLAUDE.md` | 12행·21행 | "construction 24개 포트폴리오(… 실물 맞춤 2)" → "25개(… 실물 맞춤 3)" · 테스트 수(solver-core 38클래스 102개 → +1클래스 +3개, app 3클래스 4개 → +1개) |
| `docs/notes/initial-solution-heuristics-h1-h24.md` | §2 끝·§4.2 표 ①·§4.4 | H25 항목(쉬운 말 설명) 추가 · 축 ① 후보에 H25, "H23 기권 시 대체 = §8 결과대로" · §4.4에 "H23 기권 입력에서는 [stage-04-h25 §8] 결과가 슬롯을 정한다" |
| `docs/notes/zone-quota-balanced-fill-construction.md` | 38~39행·483행·556행·563행·623행 | "24개" → "25개". `fill` 추출은 H23 동작 무변경이므로 설명 무변경 |
| `docs/implementation/README.md` | 문서 표 | 이 문서 행 추가 (4-초기해의 설계안) |

---

## 10. 하지 않는 것 · 미결

| 안 하는 것 | 이유 |
|---|---|
| H3 수정 (H3에 교환 단계 추가) | 비교 대상이 사라진다. H3는 그대로 두고 H25를 옆에 둔다 (사용자 확정) |
| "H3 2단계 재사용" 변형 (H25 배정 + H3 what-if 삽입) | §4.4 — H25 vs H3에서 채우기 효과를 완전히 분리하려면 필요하지만, 배정 값 `V` 기록으로 대신 분리한다. §8 결과가 "채우기 덕"으로 나오면 그때 만든다 |
| 2-대 이동(2-opt식) 이웃 | 1-대 이웃(MOVE·SWAP)으로 시작. §8에서 H25 < H23이면 이웃을 넓힐지 결정 |
| 희소 DP + 폭 제한 (H23 기권 제거) | **별도 설계** — 이 문서와 독립. 그 개정이 끝난 뒤 H25가 남을 이유는 §8뿐 |
| 존 순서·유형 순서의 재정의 | H23 공통 부품 그대로 — 바꾸면 비교가 흐려진다 |
| 시간 상한 | 계약 D2·Q3 |

| # | 질문 | 결정 (2026-09-04 사용자) |
|---|---|---|
| Q1 | `MAX_SCANS` 초기값 | **50 확정.** T54 trace로 실물에서 몇 스캔에 끝나는지 보고 Stage 8에서 조정 |
| Q2 | best-improvement vs first-improvement | **best-improvement 확정**(§4.3). 단 §8 재료 ④에서 품질 차이가 없으면 first로 바꾼다 |
| Q3 | §8 ③ "H23 기권 입력"의 합성 방법 | **T25 합성 변형 확정**(차량 전부 다른 유형, Π = 2³¹). scaling 설계 적용 후에는 이 입력에서 H23도 기권하지 않으므로 "H25 완주" 확인(T55)과 H23 비교 재료가 된다 |
| Q4 | T56에 `H25 ≤ H3` 단언(T56b)을 넣을 시점 | **§8 ① 실측 후 확정.** 실측 전에는 출력만 |
| Q5 | H25의 우선순위 위치 | **25번째 확정**(동률 시 H23·H24가 이김) |
