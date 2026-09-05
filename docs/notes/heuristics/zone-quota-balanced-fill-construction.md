# `ZoneQuotaBalancedFillConstruction` (H23) 코드 해설

> **성격**: 코드 해설 노트(비규범). 규범은 [heuristics 문서 §5 H23](../../implementation/stage-04-initial-solution-heuristics.md)이고,
> 쉬운 말 설명은 [초기해 24개 노트 §2 H23](../initial-solution-heuristics-h1-h24.md)다.
> DP 내부(상태·전이·프론티어)는 [존 배정 DP 보강](../zone-quota-allocation-dp.md)이 맡는다 — 이 노트는 그 결과를 H23이 **어떻게 쓰는지**만 연장한다.
> 셋과 이 노트가 어긋나면 **규범이 이긴다.** 이 노트는 §2 H23을 코드 수준으로 연장할 뿐, 대체하지 않는다.
>
> 개정 근거: 기권 제거·희소 DP는 [scaling](../../implementation/stage-04-zone-quota-allocation-scaling.md),
> 값 함수 `(부족, 대수, 낭비, 결손)`은 [zone-value-function](../../implementation/stage-04-zone-value-function.md),
> 실물 구조는 [survey §2.5](../../implementation/stage-04-initial-solution-heuristics-survey.md).
>
> **대상 독자**: CVRPTW[^cvrptw]를 아는 Java 개발자. 이 저장소의 용어(`Request`·`Problem`·bank 등)는 처음 본다고 가정하고,
> 처음 나올 때마다 각주로 풀었다. 각주는 문서 끝 [용어 각주](#용어-각주)에 모여 있다.
>
> **대상 파일**: `solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java` (205줄, 2026-09-05 기준).
> 존 배정 결과는 `ZoneQuotaAllocation.java` (710줄)의 `allocate`가 낸다 — 그 710줄을 여기 풀지 않는다.

---

## 0. 한 줄 요약

**"구역마다 차를 몇 대 줄지를 먼저 표로 정하고(존 배정 DP[^dp]), 그 다음 존[^zone] 안에서 '남은 정차를 평균 크기 주문으로 다 채웠을 때 부피가 얼마나 남는가'가 가장 잘 맞는 차에 싣고, 못 실린 큰 주문은 이미 실린 더 작은 주문과 1:1로 맞바꾼다."**

이름은 세 조각이다.

| 이름 조각 | 뜻 |
|---|---|
| `zone-quota` | 존별 차량 **할당량**(quota[^quota])을 먼저 정한다 — `ZoneQuotaAllocation.allocate` |
| `balanced-fill` | 존 안은 부피·정차 **두 축이 동시에 딱 차도록** 채운다 — 이 파일의 `fill` |
| `Construction` | 초기해 구축 기법[^construction] — 빈 해에서 시작해 요청을 하나씩 넣는다 |

이 기법은 문헌이 아니라 **실물 데이터의 구조**에서 나왔다. 실물 fixture[^fixture](주문 452건·차량 31대)는 세 가지다
([survey §2.5](../../implementation/stage-04-initial-solution-heuristics-survey.md)): ① 주문 부피 합이 차량 부피 합의 95.8%라 **여유가 4.2%뿐**이고,
② 주문이 허용하는 차급[^vehicle-class]이 구역과 거의 1:1이며, ③ 한 경로는 구체 구역 하나만 돌 수 있다(`ZONE_MIX`[^zone-mix]).
여유가 4.2%면 앞 존이 좋은 차를 집어 가면 뒤 존이 굶는다. 그래서 조합을 전역 DP로 정한 뒤 존 안을 채운다.
결과는 실물에서 **미배정 0 · 31대 · 24개 중 1위**(종전 국소판 H3는 미배정 15).

---

## 1. 프로젝트 안에서의 위치

### 1.1 초기해 포트폴리오의 한 칸

이 저장소의 초기해[^initial-solution]는 하나가 아니다. `InitialSolutionBuilder.build(problem, profile)`가 결정적 construction
**24개**(H1~H24)를 전부 돌려 각 결과를 정식 평가(`Evaluator`)에 넣고, 사전식 점수[^lexicographic-score]가 가장 좋은 하나를 고른다.
H23은 23번째이고, 실물 fixture에서는 이 H23이 선택된다.

```text
InitialSolutionBuilder.build(problem, profile)
  ├─ H1 … H22
  ├─ H23 ZoneQuotaBalancedFillConstruction   ← 이 문서
  └─ H24 ZoneQuotaSubsetFillConstruction     (같은 allocate, 존 내부 적재는 자기 코드)
  → 각 Solution을 Evaluator로 평가 → 사전식 최선 1개
```

포트폴리오 축으로는 **"전역 배분"**의 대표다 ([h1–h24 §4.2 ①](../initial-solution-heuristics-h1-h24.md)).
같은 축의 국소판이 H3(`vehicle-zone-fill` — 차 한 대씩 존을 고른다, 실물 15건). H24는 1단계가 H23과 같고 2단계만 다르다
(미배정 8, 거리는 더 짧다 — 거리 축은 ALNS 몫이라 슬롯은 H23).

### 1.2 `ConstructionHeuristic` (SPI[^spi])

```java
public interface ConstructionHeuristic {
    String id();                                   // "zone-quota-balanced-fill"
    boolean abstains(Problem problem);             // H23은 항상 false — 기권하지 않는다
    Solution construct(Problem problem, Profile profile);
}
```

세 가지가 이 파일 전체를 지배한다.

1. **난수 없음, 결정적**[^determinism]. 정렬은 문자열 ID로 끝나고 `HashMap` 순회로 승자를 고르지 않는다.
2. **반환 해는 항상 유효.** 못 넣은 요청은 bank[^bank]. 삽입은 전부 `InsertionSearch`(§2.2)라 hard 제약[^hard-soft]을 통과한 것만 경로에 있다.
3. **기권[^abstain] 없음.** `abstains`는 `return false`. 유형 조합이 커도, 존이 없어도, 정차 한도가 없어도 실행한다.
   예전(2026-09-02)에는 Π(유형별 대수+1) > 65,536이면 기권했다. 2026-09-04 scaling이 기권을 없애고
   잘림은 `Allocation.truncated`로만 표시한다 (X20·X29). **기권과 잘림은 다른 일이다.**

### 1.3 다루는 자료형

| 타입 | 이 파일에서의 뜻 |
|---|---|
| `Problem`[^problem] | 동결된 입력 전체. 탐색은 읽기만 |
| `Request`[^request] | 배차의 원자 단위 (pair[^pair] 통째) |
| `Vehicle` | `maxVolume`·`maxWeight`·`effectiveMaxStopCount`(정차 한도[^stop-limit], optional) |
| `Solution` | `List<Route>` + bank. 불변 — 삽입마다 새 객체 |
| `Route` | `VehicleId` + 방문 목록. 빈 경로는 생성자가 막음 |
| `Profile`[^profile] | 고객별 hard/soft. 이 파일은 `InsertionSearch`에 넘기기만 한다 |
| `Candidate` | "이 차의 이 위치에 넣으면 Δ비용이 이만큼" |
| `Allocation` | 존 배정 DP의 결과 — 유형·존·존별 차량·`truncated` |
| `Load` (private) | bin마다 부피·방문 수의 **캐시**. 오라클[^oracle]이 아니다 (§4.5) |

---

## 2. 기대는 부품 두 개

이 파일은 **"어느 존의 어느 차에 어떤 순서로"**만 정한다.
**"존에 차를 몇 대 줄까"는 `ZoneQuotaAllocation`에, "넣을 수 있는가·어느 위치가 싼가"는 `InsertionSearch`에** 맡긴다.

### 2.1 `ZoneQuotaAllocation` — 존 배정 DP (H23·H24 공통)

**존 배정 DP가 하는 일.** 구역마다 차를 몇 대 줄지를, 앞 구역이 뒤를 굶기지 않게 **표로** 계산한다.
차 한 대씩 고르는 게 아니다. "아직 안 쓴 차종별 대수"를 상태로 두고 존을 하나씩 소비하며,
각 칸에 최선값 하나만 남긴다. 표를 다 채운 뒤 거꾸로 따라가면 존별 대수(quota)가 나온다.

`allocate(problem)` 한 번이 돌려주는 레코드:

```60:62:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaAllocation.java
    record Allocation(List<VehicleType> types, List<Zone> zones,
                      Map<String, List<VehicleId>> vehiclesByZone, boolean truncated) {}
```

| 필드 | 뜻 |
|---|---|
| `types` | 차종. (호환 요청 집합, `maxWeight`, `maxVolume`, 정차 한도)가 같은 차량들의 묶음. 근무창·차고·속도는 유형에 없다 — 그 차이는 오라클이 본다. 순서 = `maxVolume` ASC, `maxWeight` ASC, 첫 `VehicleId` ASC. 유형 안 차량 = `VehicleId` ASC |
| `zones` | 요청 `anchor` side의 `zoneId`로 묶은 목록. 부재 = `"(none)"`. **호환 차량 0대인 요청은 어느 존에도 안 들어간다** (처음부터 bank, X2). 존 순서 = (호환 유형 수 ASC, Σ부피 DESC, zoneId ASC) |
| `vehiclesByZone` | 존마다 실제 `VehicleId` 목록. **한 차량은 정확히 한 존에만** 들어간다 — 상태가 대수를 차감하므로. 배정 없는 존은 빈 목록 |
| `truncated` | 폭 제한이 상태를 잘랐으면 `true` (근사). H23은 이 플래그를 **읽지 않고** 2단계를 그대로 돈다 (X27) |

진입점:

```271:273:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaAllocation.java
    static Allocation allocate(Problem problem) {
        return allocate(problem, MAX_TOTAL_STATES);
    }
```

상한 두 개 — **기권이 아니라 근사**:

| 상수 | 값 | 넘치면 |
|---|---:|---|
| `MAX_TOTAL_STATES` | 262,144 | 층마다 값 순으로 잘라 `truncated = true` (X20, 2026-09-04) |
| `MAX_FRONTIER_LEAVES` | 4,194,304 | 그 존의 프론티어[^frontier] 열거를 접두만 남기고 `truncated = true` (X29, 2026-09-05). 이게 없으면 차종 31종에서 `-Xmx8g`도 OOM |

값 비교는 `(Σ부족, Σ대수, Σ낭비, Σ결손)` 사전식 — 앞 두 축이 정식 score(미배정, 차량 수)와 같다
(2026-09-05, [zone-value-function](../../implementation/stage-04-zone-value-function.md)). 종전은 `(부족, 낭비, 대수)`.

H23이 여기서 읽는 것은 `zones()`(순서 포함), `vehiclesByZone()`, 요청 정렬용 `types()` 셋이다. `truncated`는 안 읽는다.

> 상태 격자·전이·역추적은 [DP 보강 노트](../zone-quota-allocation-dp.md)와
> [scaling](../../implementation/stage-04-zone-quota-allocation-scaling.md)이 맡는다. 아래는 H23을 읽는 데 필요한 숫자만.

#### 숫자 표 — 2 존 × 2 차종, leftover 대수

**전제:** 설명용. 시간창·거리는 DP가 안 본다. 단위는 ×1000한 정수[^units].

| 차종 | 대수 | `maxVolume` | 탈 수 있는 존 |
|---|---:|---:|---|
| S | 2 (S1, S2) | 10,000 | A, B |
| L | 1 (L1) | 20,000 | B만 (A의 주문은 S만 허용) |

| 존 | 수요 부피 | 호환 |
|---|---:|---|
| A | 20,000 | S만 |
| B | 15,000 | S, L |

상태는 **"아직 안 쓴 대수"**다. 존을 하나 처리할 때마다 그 존에 준 대수만큼 뺀다.

**이긴 배정** (앞 존이 뒤를 굶기지 않음):

| 단계 | 이 존에 준 것 | leftover S | leftover L |
|---|---|---:|---:|
| 시작 | — | 2 | 1 |
| 존 A | S × 2 | **0** | 1 |
| 존 B | L × 1 | 0 | **0** |

A는 S 2대가 있어야 20,000을 덮는다. 남은 L 1대를 B가 가져가 15,000을 덮는다. 두 존 모두 덮임.

**진 배정** (A가 L을 집어 가면 B가 굶는다):

| 단계 | 이 존에 준 것 | leftover S | leftover L |
|---|---|---:|---|
| 시작 | — | 2 | 1 |
| 존 A | L × 1 | 2 | **0** |
| 존 B | (덮을 차 없음) | 2 | 0 |

L은 부피 20,000이라 A를 낭비 0으로 덮어 보인다. 그런데 A의 주문은 S만 타므로 이 전이는 `covers`가 거부한다 — 다음 절.
설령 거부하지 않아도 leftover L이 0이라 B(15,000)는 S 10,000에 안 들어간다. **앞이 뒤를 굶기는 숫자**가 이것이다.
DP는 두 미래를 표에 다 놓고, 부족 축이 0인 첫 배정을 고른다.

#### `covers` / Hall[^hall] — T38 한 줄 + 작은 표

"덮는다(`covers`)"는 부피·무게·방문 수 합계뿐 아니라 **호환 그룹 누적 검사(Hall 조건)** 를 본다.
한 줄: **작은 차만 되는 주문 묶음은, 그 묶음이 탈 수 있는 차만으로 용량을 채워야 한다 — 큰 차 용량을 빌려 쓰면 안 된다.**
이게 없으면 DP가 부피 합만 보고 큰 차를 큰 차 금지 존에 넣는다.

T38 입력 (`hallConditionKeepsBigVehiclesOffRestrictedZones`):

| | 부피 | 허용 |
|---|---:|---|
| 존 A 주문 2건 | 9,800 × 2 = 19,600 | SMALL만 |
| 존 B 주문 1건 | 15,000 | BIG, SMALL |
| 차량 BIG V1 | 20,000 × 1 | — |
| 차량 SMALL V2, V3 | 10,500 × 2 | — |

낭비만 보면 A ← BIG(낭비 400)이 A ← SMALL×2(낭비 1,400)보다 싸다. Hall이 그 전이를 지운다.
결과: A = `{V2, V3}`, B = `{V1}`. leftover는 위 표의 "이긴 배정"과 같은 모양이다.

#### 한 차량은 한 존, 기권은 없다

`allocate`가 `vehiclesByZone`을 만들 때 유형마다 커서(`cursor[t]`)를 앞으로만 민다 (295–305행).
같은 `VehicleId`가 두 존에 나타날 수 없다. 공급이 수요보다 작으면 그 존의 목록이 비거나 `maxNeed`까지만 타고,
못 실린 요청은 2단계 leftover pass가 맡는다 (X21). 조합이 커도 `abstains`는 `false` — 잘림은 `truncated`다.

### 2.2 `InsertionSearch` — 이 기법이 부르는 메서드만

H23이 부르는 것은 아래 표가 전부다. `Cache`·`standaloneDistMeter`·`openRoute`는 안 부른다.

| 메서드 | 하는 일 | H23 |
|---|---|---|
| `emptySolution` | 전 요청이 bank인 빈 해 | `fill` 시작 |
| `candidatesFor(..., r, v)` | **차량 v 하나**의 삽입 위치, 비용 오름차순. 비호환·불가 = 빈 목록. 경로 없으면 새 경로 후보 | 존 내부 적재, 1-1 교환 |
| `candidates(...)` | **모든 기존 경로 + 미사용 호환 1대** | leftover pass만 |
| `apply` | 후보를 적용한 새 `Solution`. 그 요청은 bank에서 빠짐 | 모든 삽입 |
| `remove(..., q)` | q를 경로에서 빼 bank로. 경로가 비면 `Route` 자체가 사라짐 | 1-1 교환 전용 |
| `visitsOf` · `requestOf` | 차량의 방문 목록 / 노드 → 요청 ID | 교환에서 경로 위 요청 나열 |
| `checkOuterLoop` | 바깥 루프 상한 초과 시 `IllegalStateException` | 세 루프 전부, bound = 3n |
| `sortedRequestIds` · `BY_REQUEST_ID` | ID 문자열 순 | leftover 수집, 동률 |

`candidatesFor`가 "넣을 수 있다"고 말하려면 세 관문을 통과해야 한다 — ① 호환(차급·capability·구역·차고),
② `RoutePropagator`[^propagation]가 시간창·용량·정차 한도·`ZONE_MIX`를 경로 전체에 다시 전파해 Feasible,
③ profile hard 전부 satisfied. **이 파일에 등장하는 `maxVolume`·정차 한도는 "어느 bin이 더 잘 맞는가"를 고르는 키이지, 유효성 판정이 아니다.**
`Load`도 그 키용 캐시다. 오라클은 `InsertionSearch`다.

```62:70:solver-core/src/main/java/com/ronext/rpdptw/solve/InsertionSearch.java
    static List<Candidate> candidatesFor(
            Problem problem, Profile profile, Solution current, RequestId requestId, VehicleId vehicleId) {
        if (!problem.compatibleVehicles(requestId).contains(vehicleId)) {
            return List.of();
        }
        List<Candidate> out = new ArrayList<>();
        collect(problem, profile, requestId, vehicleId, visitsOf(current, vehicleId), out);
        return sorted(out);
    }
```

`cands.getFirst()`는 `(새 경로 여부, Δ거리, Δ운행시간)` 사전식 최선. H23는 그 위치만 쓰고 2·3순위를 보지 않는다.

`candidates`(leftover pass)는 기존 경로를 다 본 뒤, 미사용 호환 중 `VehicleId` 문자열이 가장 앞선 **1대만** 새 경로 후보로 연다
(`firstUnusedCompatible`). leftover pass가 "존 bins만"이 아니라 **해 전체**를 보는 지점이다.

방문 수가 `L`인 경로에 단일 패턴(`DELIVERY_ONLY`·`PICKUP_ONLY`)을 넣으면 슬롯 `L + 1`, 슬롯마다 재전파 `O(L)` → 차량당 `O(L²)`.
`PICKUP_DELIVERY`는 위치 쌍 `(i ≤ j)`이라 슬롯 `O(L²)` × 전파 `O(L)` → `O(L³)`. 정차 한도 근사는
`visitCount`가 PD를 **2**로 센다.

```222:224:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaAllocation.java
    static int visitCount(Request request) {
        return (request.pickup().isPresent() ? 1 : 0) + (request.delivery().isPresent() ? 1 : 0);
    }
```

---

## 3. 알고리즘 전체 흐름

```text
abstains?  항상 false          // 조합 수 기권 분기 없음 (2026-09-04)

construct:
  fill(problem, profile, ZoneQuotaAllocation.allocate(problem), ID)

fill:                              // package-static. H24는 이것을 부르지 않는다
  current = 빈 해
  bound = 3n
  leftovers = []                   // leftover[^leftover] — 존에서 못 넣은 요청
  for z in allocation.zones():     // DP가 정한 존 순서
    bins = vehiclesByZone[z]       // 이 존 시작 때 bins의 경로는 전부 비어 있다
    order = requestOrder(z.members)
    loads = bin마다 Load(0, 0)     // 키용 캐시
    ②-a 존 내부 적재
      for r in order:              // 규칙 a/b, 카운트 1
        후보 bin = candidatesFor(r, v)가 비어 있지 않은 v
        선택 = key() 사전식 최소 (동률 = bins 앞)
        있으면 apply + loads.add, 없으면 zoneLeftovers
    ②-b 1-1 교환
      zoneLeftovers를 부피 DESC로:
        존 경로의 더 작은 q를 빼고 r을 그 차, q를 존의 다른 차에 — 첫 성공에서 멈춤
        실패 → leftovers
  존에 안 들어간 요청(호환 0대)도 leftovers에 합류
  ④ leftover pass
    leftovers를 requestOrder로:
      candidates() 최선(어느 경로든·새 차 1대든)에 삽입. 0개면 bank
  return current
```

바깥 카운트는 ②-a 요청마다 1 + ②-b leftover마다 1 + ④ leftover마다 1 → **≤ 3n**.
코드의 `bound = 3 * problem.requests().size()`가 그 상한이다.

H24는 `allocate`만 공유한다. 존 내부는 자기 `subsetSum`이고, leftover pass만 `requestOrder`를 빌려 쓴다.
`fill`을 부르지 않는다.

---

## 4. 단계별 상세

### 4.1 `abstains` · `construct`

```29:37:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java
    @Override
    public boolean abstains(Problem problem) {
        return false;
    }

    @Override
    public Solution construct(Problem problem, Profile profile) {
        return fill(problem, profile, ZoneQuotaAllocation.allocate(problem), ID);
    }
```

`abstains` 본체는 `return false` 한 줄이다. `combinations(...) > 65,536` 분기는 **없다.**
T45가 유형 17종·20종·3종×67대(Π 314,432)에서 `abstains == false`를 고정한다.

`construct`는 배정을 계산해 `fill`에 넘긴다. `id` 인자는 방어 카운터 예외 문자열용 — H23는 `"zone-quota-balanced-fill"`.

### 4.2 `fill` 골격

```43:54:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java
    static Solution fill(Problem problem, Profile profile, Allocation allocation, String id) {
        Solution current = InsertionSearch.emptySolution(problem);
        int bound = 3 * problem.requests().size();
        int iterations = 0;
        List<RequestId> leftovers = new ArrayList<>();
        for (Zone zone : allocation.zones()) {
            List<VehicleId> bins = allocation.vehiclesByZone().get(zone.zoneId());
            List<RequestId> order = requestOrder(problem, allocation, zone.members());
            Map<VehicleId, Load> loads = new LinkedHashMap<>();
            for (VehicleId vehicleId : bins) {
                loads.put(vehicleId, new Load());
            }
```

- **package-static.** 같은 패키지(테스트·H24)가 부를 수 있게 열려 있다. **H24는 부르지 않는다.**
- `bins` = DP가 이 존에 준 차량. 한 차량은 한 존이라, 이 존을 시작할 때 그 차의 경로는 아직 없다.
  이름이 bin인 이유는 이 단계가 요청을 통(bin)에 담는 일에 가깝기 때문이다.
- `loads` = bin마다 `{volume, visits}`. 경로를 매번 다시 훑지 않고 `key`를 계산하려고 둔다.
  삽입할 때만 `add`. 교환 단계에서는 갱신하지 않고, 교환은 `loads`를 읽지 않는다.

존 루프의 나머지(적재 → 교환 → leftover 수집 → leftover pass)는 아래 절.

### 4.3 요청 순서 — `requestOrder`

```108:115:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java
    static List<RequestId> requestOrder(Problem problem, Allocation allocation, List<RequestId> members) {
        List<RequestId> order = new ArrayList<>(members);
        order.sort(Comparator.<RequestId>comparingInt(id -> ZoneQuotaAllocation.compatibleTypeCount(allocation.types(), id))
                .thenComparing(Comparator.comparingLong((RequestId id) -> problem.request(id).totalVolume()).reversed())
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        return order;
    }
```

| 순서 | 키 | 방향 | 왜 |
|---:|---|---|---|
| 1 | 호환 유형 수 | ASC | 탈 수 있는 차종이 적은 요청을 먼저. 나중에 넣으면 자리가 없다 |
| 2 | `totalVolume` | DESC | 큰 것부터 — 자투리가 남지 않게 |
| 3 | `RequestId` | ASC | 동률을 문자열로 깨서 결정성 |

첫 키가 **필수**다. 부피순만 쓰면 여유가 0.12 CBM인 ZONE_21에서 13건이 미배정이고, 호환 수를 먼저 보면 2건이다
(survey §2.5 민감도). 같은 존 안에 "작은 차만 되는 주문"과 "아무 차나 되는 주문"이 섞여 있을 때,
아무 차나 되는 큰 주문이 먼저 작은 차의 자리를 차지하는 것을 막는다.

이 메서드는 leftover pass에서도 그대로 쓴다. H24 leftover pass도 여기를 빌린다
(`ZoneQuotaSubsetFillConstruction.java` 117행).

### 4.4 선택 키 — `key` (이 기법의 핵심)

존 내부 적재 루프(56–82행)의 역할 분담은 세 줄이다.

1. **"들어갈 수 있는가"** — `candidatesFor`. 빈 목록이면 이 bin은 후보가 아니다.
2. **"어느 bin이 좋은가"** — `key`. 오라클이 통과시킨 bin만 겨룬다.
3. **"bin 안 어느 위치인가"** — `cands.getFirst()`.

```135:147:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java
    private static double[] key(Vehicle vehicle, Load load, Request request, double mean) {
        long remainingVolume = vehicle.maxVolume() - load.volume - request.totalVolume();
        double predicted = remainingVolume;
        if (vehicle.effectiveMaxStopCount().isPresent()) {
            int stopsLeft = vehicle.effectiveMaxStopCount().getAsInt() - load.visits - ZoneQuotaAllocation.visitCount(request);
            predicted = remainingVolume - mean * stopsLeft;
        }
        return new double[] {predicted < 0 ? 1 : 0, Math.abs(predicted), remainingVolume};
    }
```

코드 그대로:

```text
remainingVolume = maxVolume − load.volume − volume(r)
stopsLeft       = 정차 한도 − load.visits − visitCount(r)     // PD면 2, 단일이면 1
predicted       = remainingVolume − mean × stopsLeft         // 한도 부재면 remainingVolume 그대로 (X23)
key             = ( predicted < 0 ? 1 : 0 ,  |predicted| ,  remainingVolume )
                  → 사전식 오름차순. 동률은 bins에서 먼저 나온 차
```

`mean`은 **지금 r을 제외하고, 이 존에서 앞으로 처리할 요청**의 평균 부피 (`meanVolume`, 124–133행). 마지막이면 0.
정렬이 부피 내림차순이라 이 평균은 뒤로 갈수록 작아진다.

| `predicted` 부호 | 뜻 | 첫 키 |
|---|---|---:|
| **음수** | 부피보다 **정차 칸이 먼저 바닥난다**. 정차가 다 차도 부피가 남으니 그 부피는 낭비 | **1** — 뒤로 |
| **0 이상** | 정차보다 **부피가 먼저 바닥난다**. 희소 자원인 부피(실물 여유 4.2%)를 다 쓴다 | **0** — 앞 |
| 0에 가까움 | 두 축이 동시에 딱 찬다 | 둘째 키 `\|predicted\|`가 작을수록 좋음 |

셋째 키 `remainingVolume`은 앞 둘이 같을 때 남는 부피가 가장 적은 bin(부피 best-fit[^best-fit])으로 깬다.

정차 한도가 없으면(`effectiveMaxStopCount` 부재) `predicted = remainingVolume`이 되어 키가
`(0, remainingVolume, remainingVolume)` — 순수 부피 best-fit으로 **퇴화**한다. 기권하지 않는다 (X23).

`double`은 **정렬 키로만** 쓴다. `compare`가 `< 0`일 때만 바꾸므로 동률이면 bins 앞이 남는다 (규범 §4.2).

규범 의사코드는 `B_v − 정차 수 − 1`이라 적는다. 코드는 `visitCount(r)`이라 PD(방문 2)를 뺀다.
의사코드가 단일 방문을 예로 쓴 것이고, 코드가 일반형이다.

#### 워크스루 — T40 (`signedBudgetAvoidsWastingStops`)

테스트 주석 그대로다. **전제:** 존 없음 → `"(none)"` 하나. 전원 `DELIVERY_ONLY`·호환 전원·시간창 비제약.
fixture의 무게 = 부피. 정차 한도 4.

| | 부피 | 정차 |
|---|---:|---:|
| V1 | 20,000 | 4 |
| V2 | 4,000 | 4 |
| R1–R4 | 4,000 | 방문 1 |
| R5–R8 | 1,000 | 방문 1 |

수요 부피 20,000·방문 8. V1만으로는 방문 4 < 8, V2만으로는 부피 4,000 < 20,000 → DP가 둘 다 준다.
유형 순서 = `maxVolume` ASC → type0 = V2, type1 = V1. bins = **[V2, V1]**.
요청 순서 = 호환 유형 수 전원 2 → 부피 DESC → **R1 R2 R3 R4 R5 R6 R7 R8**.

| 단계 | r | 평균(뒤) | V2 키 | V1 키 | 선택 |
|---:|---|---:|---|---|---|
| 1 | R1 4,000 | 16,000/7 ≈ 2,286 | rem 0, 정차 3 → pred −6,857 ⇒ **(1**, 6857, 0) | rem 16,000, 정차 3 → +9,143 ⇒ **(0**, 9143, 16000) | **V1** |
| 2 | R2 4,000 | 2,000 | (1, …) | rem 12,000, 정차 2 → +8,000 ⇒ (0, 8000, 12000) | V1 |
| 3 | R3 4,000 | 1,600 | (1, …) | rem 8,000, 정차 1 → +6,400 ⇒ (0, 6400, 8000) | V1 |
| 4 | R4 4,000 | 1,000 | (1, …) | rem 4,000, 정차 0 → +4,000 ⇒ (0, 4000, 4000) | V1 (정차 4 = 만석) |
| 5 | R5 1,000 | 1,000 | rem 3,000, 정차 3 → pred 0 ⇒ **(0, 0, 3000)** | 후보 없음(정차 한도) | **V2** |
| 6–8 | R6–R8 | … | pred 0 | 후보 없음 | V2 |

단계 1만 풀어 쓰면: 부피 best-fit이라면 V2가 "남은 부피 0"으로 완벽히 맞아 R1을 V2에 넣는다.
그러면 V1이 R2·R3·R4·R5로 정차 4를 다 쓰고, V2는 부피가 꽉 차서 R6·R7·R8 **3건이 bank**.
부호 예산 키는 V2를 "정차 3칸이 남는데 부피가 0이니 −6,857 낭비"로 읽어 첫 키 1로 뒤로 민다.
V1이 큰 주문 4건, V2가 작은 주문 4건. 테스트: bank 공집합, V1 = {R1..R4}, V2 = {R5..R8}.

### 4.5 `Load`는 캐시이지 오라클이 아니다

```196:204:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java
    private static final class Load {
        long volume;
        int visits;

        void add(Request request) {
            volume = Math.addExact(volume, request.totalVolume());
            visits += ZoneQuotaAllocation.visitCount(request);
        }
    }
```

삽입이 성공할 때만 `add`한다. 시간창·거리·실제 적재 궤적은 없다. `key`가 이 두 숫자만 읽는다.
"넣을 수 있는가"는 그 직전에 `candidatesFor`가 이미 답했다. `Load`를 믿어 삽입을 건너뛰는 분기는 없다.

### 4.6 1-1 교환 — `exchange`

존 적재가 끝나면 그 존에서 못 넣은 요청(`zoneLeftovers`)을 **부피 큰 것부터** 한 번 더 시도한다 (83–91행).

```163:194:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java
    private static Solution exchange(Problem problem, Profile profile, Solution current, List<VehicleId> bins, RequestId r) {
        long volume = problem.request(r).totalVolume();
        for (VehicleId vehicleId : bins) {
            List<RequestId> visitors = new ArrayList<>();
            for (var nodeId : InsertionSearch.visitsOf(current, vehicleId)) {
                RequestId q = InsertionSearch.requestOf(problem, nodeId);
                if (!visitors.contains(q) && problem.request(q).totalVolume() < volume) {
                    visitors.add(q);
                }
            }
            visitors.sort(Comparator.comparingLong((RequestId id) -> problem.request(id).totalVolume())
                    .thenComparing(InsertionSearch.BY_REQUEST_ID));
            for (RequestId q : visitors) {
                Solution without = InsertionSearch.remove(problem, current, q);
                List<Candidate> forR = InsertionSearch.candidatesFor(problem, profile, without, r, vehicleId);
                if (forR.isEmpty()) {
                    continue;
                }
                Solution withR = InsertionSearch.apply(problem, without, forR.getFirst());
                for (VehicleId other : bins) {
                    if (other.equals(vehicleId)) {
                        continue;
                    }
                    List<Candidate> forQ = InsertionSearch.candidatesFor(problem, profile, withR, q, other);
                    if (!forQ.isEmpty()) {
                        return InsertionSearch.apply(problem, withR, forQ.getFirst());
                    }
                }
            }
        }
        return null;
    }
```

순서: bins 순 × (`totalVolume` ASC, `RequestId` ASC) × bins의 다른 차. **첫 성공에서 즉시 반환.** 최적 교환을 찾지 않는다.

- q는 **`volume(q) < volume(r)`만**. r보다 큰 q를 빼면 r은 들어가도 q가 갈 곳이 없다. 작은 q부터 본다.
- r은 **q가 있던 그 차**에만 들어간다. q는 **같은 존의 다른 차**로만 옮긴다. 존 밖을 안 건드되니 `ZONE_MIX`와 DP 배정이 유지된다.
- `visitors.contains`로 중복을 거른다 — PD면 한 요청이 노드 2개로 나타난다.
- 실패의 `without`·`withR`은 버린다. `Solution`이 불변이라 롤백 코드가 없다.
- bins가 1대면 "다른 차" 루프가 비어 항상 `null` → leftover pass로.

#### 워크스루 — 숫자 3개

**전제:** 설명용. 한 존, 차 2대, 용량 10,000. 적재가 끝난 뒤 leftover에 r 하나.

| | 부피 |
|---|---:|
| r (못 실린 것) | **4,000** |
| q (V1에 실린 더 작은 것) | **3,000** |
| V2 잔여 부피 | **5,000** |

V1은 잔여 1,000이라 r 4,000이 안 들어갔고, q 3,000이 그 자리에 있다.

```text
① remove(q)     V1 잔여 1,000 + 3,000 = 4,000
② r을 V1에      4,000이 딱 맞음 → V1에 r
③ q를 V2에      3,000 < 잔여 5,000 → V2에 q
return 그 해     // 첫 성공. 다른 (q, 차) 쌍은 안 본다
```

세 숫자가 동시에 맞아야 한다: r이 q보다 크고, q를 뺀 자리에 r이 들어가고, q가 다른 존 경로의 잔여에 들어간다.
하나라도 어긋나면 `null`이고 r은 `leftovers`로 간다.

### 4.7 leftover 수집과 leftover pass

```93:105:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java
        for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {   // 존에 안 들어간 요청(호환 0대)도 bank 그대로
            if (current.bank().contains(requestId) && !leftovers.contains(requestId)) {
                leftovers.add(requestId);
            }
        }
        for (RequestId requestId : requestOrder(problem, allocation, leftovers)) {     // leftover pass
            InsertionSearch.checkOuterLoop(id, ++iterations, bound);
            List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, requestId);
            if (!cands.isEmpty()) {
                current = InsertionSearch.apply(problem, current, cands.getFirst());
            }
        }
        return current;
```

- **수집.** 존 처리가 끝난 뒤 bank에 남아 있으면서 아직 `leftovers`에 없는 요청 = **호환 차량 0대라 처음부터 존에 못 들어간 요청**.
  `candidates()`가 항상 빈 목록을 내니 결국 bank에 남지만, 코드는 그 사실을 미리 가정하지 않고 같은 경로로 흘린다.
- **leftover pass는 bins가 아니다.** `candidates()` — **모든 기존 경로 + 미사용 호환 1대**. DP가 배정하지 않은 차로 새 경로를 열 수 있다.
  다른 존의 경로에 넣으려 하면 `ZONE_MIX`가 거부한다. 여기서도 못 넣으면 bank — 유효한 해다.
- bound는 이 루프만의 n이 아니라 **fill 전체 3n**이다. 이미 ②-a·②-b가 카운트를 쓰고 들어온다.

---

## 5. 결정성 · 종료 · 복잡도

### 5.1 결정성

| 지점 | 고정 방법 |
|---|---|
| 존 순서 | DP 안 (호환 유형 수 ASC, Σ부피 DESC, zoneId ASC) |
| bins 순서 | DP가 소비한 순서 = 유형 순서(`maxVolume` ASC …) × 유형 안 `VehicleId` ASC |
| 요청 순서 | `requestOrder` — 끝이 `BY_REQUEST_ID` |
| bin 선택 동률 | `compare(key, chosenKey) < 0`일 때만 교체 → bins 앞 |
| 오라클 후보 동률 | `InsertionSearch.sorted` (비용, `VehicleId`, 위치) |
| 교환 순서 | bins × (부피 ASC, RequestId ASC) × bins |
| leftover 수집 | `sortedRequestIds` |
| leftover pass 새 차 | `firstUnusedCompatible` — `VehicleId` 문자열 최소 1대 |
| `loads` | `LinkedHashMap` — 삽입 순서 보존. 순회로 승자를 고르지 않음 |

`HashMap` 순회로 승자를 고르는 곳이 없다. `double` 키는 정렬에만 쓴다.

bins 순서는 **"전역 VehicleId ASC"가 아니다.** 70행 주석과 규범 교환 절의 "VehicleId ASC"는 bins 순처럼 읽히지만,
실제 bins는 **작은 차 유형이 먼저**다. T40에서 bins = `[V2, V1]`. 결정적이긴 마찬가지라 재현에는 영향이 없다.

### 5.2 종료 (규범 §4.3 · §5 H23)

- **DP:** 성분당 보관 상태 ≤ `MAX_TOTAL_STATES` + 존 수, 존당 프론티어 잎 ≤ `MAX_FRONTIER_LEAVES`. 기권으로 막지 않는다. 예산 소진은 근사 (X20·X29).
- **존 내부 적재:** 존 요청마다 규칙 (a) 삽입 또는 (b) 후보 0개 → 합 ≤ n.
- **교환:** zoneLeftover마다 1회 → ≤ n. 성공이면 bank −1, 실패면 leftovers로.
- **leftover pass:** ≤ n.
- 합 ≤ **3n** = `bound`. 넘으면 `IllegalStateException` — 품질이 아니라 버그 (X13).

시간 상한은 초기해에 없다. `AlnsConfig.timeLimitSec`은 ALNS에만 걸린다.

### 5.3 복잡도 (규범 §5 표 H23 행)

```text
H23 | zone-quota-balanced-fill | zone-quota | 전 패턴 | ≤ 3n
    | 배정 DP O(Z · S · F · G) (S ≤ Π(c_t+1), F ≤ S, G = 호환 그룹 수) + O(m_z · L²)
```

| 기호 | 뜻 |
|---|---|
| Z | 존 수 |
| S | DP 상태 수. 표는 `Π(유형별 대수+1)`을 상한으로 적는다. 구현은 도달한 상태만 보관하고, 성분당 `MAX_TOTAL_STATES`(262,144)로 자른다 |
| F | 존 하나에서 시도하는 프론티어 벡터 수. 잎은 `MAX_FRONTIER_LEAVES`로 잘린다 |
| G | 호환 그룹 수 — `covers`/Hall이 접두마다 도는 횟수 |
| m_z | 그 존의 bin 수 |
| L | 경로 방문 수. 단일 패턴이면 위치 `L+1` × 전파 `L` → `O(L²)`. PD면 위치가 `L²`이라 `L`이 한 번 더 |

2단계 적재는 존 요청마다 bin마다 `candidatesFor` 한 대. 교환은 leftover 1건에 최악 (bin 수 × 경로 길이 × bin 수)번의 `candidatesFor`.
실물에서 leftover가 몇 건 안 되니 전체 시간의 대부분은 1단계 DP다.

실측 소요: 2026-09-02 **507 ms** (scaling 전, 표·survey). scaling 뒤 재실측 **550 ms\*** ([h1–h24 §1](../initial-solution-heuristics-h1-h24.md)).
H3 137 ms보다 느리고, regret 계열 H1(830 ms)·H2(644 ms)보다 싸다.

---

## 6. 코드를 읽으며 눈여겨볼 점

규범과 코드는 1:1이다. 쉬운 말(§2 H23)과 코드가 어긋나 보이는 곳, 인접 기법과의 차이만 적는다. **수정 제안이 아니라 읽기 보조.**

1. **`abstains`는 항상 `false`다.** 옛 노트·옛 규범 문면의 "65,536이면 기권"은 2026-09-04에 죽었다. 잘림은 `truncated` (X20·X29).
2. **`fill`은 H23의 존 내부 적재다.** H24는 같은 `allocate` 위에서 자기 루프를 돌고, leftover pass만 `requestOrder`를 재사용한다.
3. **`Load`는 오라클이 아니다.** 부피·방문 수의 장부. 유효성은 `candidatesFor`가 이미 답한 뒤에만 `add`한다.
4. **키의 첫 축은 부호다.** 음수(정차 낭비)를 1로 밀어 뒤로 보낸다. 부피 best-fit만 쓰면 T40에서 3건이 남는다.
5. **`visitCount`는 PD=2.** 규범 의사코드의 `- 1`은 단일 방문 예. 정차 한도 부재면 이 항 자체가 없고 부피 best-fit으로 퇴화 (X23).
6. **leftover pass는 존 bins가 아니다.** `candidates()` = 전 경로 + 미사용 1대. DP가 안 준 차를 여기서 열 수 있다 (X21·X24).
7. **교환은 존 안에서만, 첫 성공에서 멈춘다.** r보다 작은 q만. 실패 시도의 `Solution`은 버린다.
8. **H3 vs H23.** H3는 차 한 대를 잡고 존 하나를 what-if로 커밋한다 — 존이 조각나고 실물 15건. H23는 존에 차를 전역으로 나눈 뒤 존 안을 채운다 — 0건. greedy[^greedy]의 범위만 넓힌 것이 아니다. 1단계가 DP다.
9. **`Problem`은 어디서도 안 바뀐다.** `Solution`도 불변이라 `current = apply(...)`로만 갱신한다.

---

## 7. 경계 상황 (규범 §7)

| 상황 | 동작 | 번호 |
|---|---|---|
| 유형 조합 수가 큼 | **기권하지 않는다.** 상태 총량 > 262,144 또는 프론티어 잎 > 4,194,304이면 `truncated = true`로 근사하고 2단계는 그대로 | **X20** · **X29** |
| 공급 < 수요라 어떤 존도 못 덮음 | 프론티어에 빈 집합(c)이 항상 있어 DP는 완주. 그 존 bins가 비면 적재 후보 0개 → leftover pass가 미사용 차로 시도 | **X21** |
| 정차 한도 부재 | 키가 부피 best-fit으로 퇴화. 기권 아님 | **X23** |
| 호환 차량 0대인 요청 | 존에 안 들어감 → §4.7 수집 → `candidates()` 빈 목록 → bank | X2 |
| `zoneId`가 하나도 없음 | `"(none)"` 존 하나. 기권 사유 아님 | §4.4 |
| 호환 존이 없는 유형 | 그 성분은 DP를 안 돌림. leftover pass가 그 차를 새 경로로 쓸 수 있음 | X24 |
| 근사(`truncated`)가 발생 | H23는 정상 실행. 포트폴리오 선택은 정식 평가 | X27 |
| 차종 ≥ 65 | 알려진 한계. 마스크가 `long`이라 비트가 접힌다. 가드·기권 없음 | X28 |
| 존에 bins 1대뿐 | 교환의 "다른 차"가 없어 항상 실패 → leftover pass | — |
| 바깥 루프 > 3n | `IllegalStateException` | X13 |

X20을 코드로: `abstains`에 조합 수 비교가 없고, `allocate`가 `truncated` 비트만 켠다. H23 `construct`는 그 비트를 읽지 않는다.

---

## 8. 테스트와 실측

| 테스트 | 고정하는 것 |
|---|---|
| T40 `ZoneQuotaBalancedFillConstructionTest.signedBudgetAvoidsWastingStops` | §4.4 그대로 — 부피 best-fit은 3건을 놓치지만 부호 예산은 전량. V1={R1..R4}, V2={R5..R8} |
| T38 `ZoneQuotaAllocationTest.hallConditionKeepsBigVehiclesOffRestrictedZones` | §2.1 Hall — A에 SMALL 2대, B에 BIG. 낭비만 보면 큰 차가 A로 가는 입력 |
| T44·T52 `WinPocFixtureTest.zoneQuotaBalancedFillAssignsEveryRequestOnRealFixture` (app) | 실물 452건에서 bank 0·31경로·Feasible·재실행 동일 score(X8)·H3보다 우위·경로 감사. **score 전체** `[0, 31, 4,194,052, 1,004,144]` · `truncated == false` |
| T45 `manyTypesNoLongerAbstain` | 17종·20종·3×67대에서 `abstains == false` · 완주 · `truncated == false` |

T39(`combinationsBoundAbstains`)는 **삭제**됐다 (2026-09-04, T45로 대체). 기권 조건이 사라졌기 때문이다.

실물 실측 (`data/win_poc_case_floor.json`, 주문 452 · 차량 31 · 정차 28).
점수 네 축은 **두 날짜**를 같이 적는다 — 2026-09-05 값 함수 개정이 거리·운행시간을 바꿨고, T52가 새 값을 고정한다.

| 시점 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 | 비고 |
|---|---:|---:|---:|---:|---:|---|
| 2026-09-02 (값 함수 개정 전, survey §2.5) | 0 | 31 | **4,198,408** | **1,002,069** | 507 ms | scaling 전 DP |
| 2026-09-04 뒤 재실측 ([h1–h24 §1](../initial-solution-heuristics-h1-h24.md)) | 0 | 31 | 4,198,408 | 1,002,069 | **550 ms\*** | scaling 뒤. 점수 축은 그때 그대로 |
| **2026-09-05 값 함수 개정 후 (T52 확정)** | **0** | **31** | **4,194,052** | **1,004,144** | (T52는 소요를 안 고정) | 축 `(부족, 대수, 낭비, 결손)` |

24개 중 **1위**. 같은 표의 H3는 미배정 **15** · 3위 · 137 ms. H24는 미배정 8.

```text
H23 score = [0, 31, 4,194,052, 1,004,144]     // T52
H3  score = [15, 31, 4,515,433, 1,003,093]    // §1 표 (한 번 실행 기록)
```

T44가 회귀로 고정하는 것은 "H23이 전량 배정하고 H3보다 사전식 우위"와 (T52로) score 전체 일치다.
550 ms\*는 한 번의 실행 기록이다.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env
mvn test -pl solver-core -Dtest=ZoneQuotaBalancedFillConstructionTest
mvn test -pl solver-core -Dtest=ZoneQuotaAllocationTest#hallConditionKeepsBigVehiclesOffRestrictedZones
mvn test -pl app -Dtest=WinPocFixtureTest
```

---

## 용어 각주

[^cvrptw]: **CVRPTW** — 용량·시간창이 있는 차량 경로 문제. 이 저장소의 **RPDPTW**는 거기에 pickup-and-delivery와 실무 제약(차급·구역·정차 한도·근무창)을 더한 "rich" 변형.

[^construction]: **construction** — 빈 해에서 요청을 넣어 첫 해를 만드는 기법. 뒤의 ALNS가 그 해를 부수고 고친다. 여기선 24개, 난수 없음.

[^dp]: **DP (동적 계획법)** — 문제를 "상태 → 다음 상태"로 쪼개고 상태마다 최선값 하나만 기억한다. 여기서 상태는 "아직 안 쓴 차종별 대수"이고, 존을 하나 처리할 때마다 한 층 내려간다. §2.1의 leftover 표가 그 상태다.

[^zone]: **존** — 요청 `anchor` side의 `zoneId`. 한 경로는 구체 구역 1종만 담을 수 있다(`ZONE_MIX`). 없으면 `"(none)"` 한 그룹.

[^quota]: **quota (할당량)** — 존에 주기로 정한 차종별 대수. DP의 출력. 실제 `VehicleId` 목록은 유형 순서 × 유형 안 ID 순으로 커서가 소비한다.

[^leftover]: **leftover** — 그 존의 bins에 못 넣은 요청. 교환에 한 번 더 시도되고, 실패하면 전 존 처리 뒤 leftover pass가 `candidates()`로 다시 본다. bank의 중간 단계이지, 아직 최종 미배정이 아니다.

[^zone-mix]: **`ZONE_MIX`** — 한 경로가 구체 구역 2종 이상을 방문하면 hard 위반. `zoneId` 부재·`"ALL"`은 안 센다. 전파가 판정한다. H23 2단계는 존 bins에만 넣으므로 같은 차에 두 구체 존을 섞지 않고, leftover pass가 다른 존 경로를 두드리면 전파가 거부한다.

[^vehicle-class]: **차급** — 차량 톤수 등급. 주문의 `allowedVehicleFeatures`와 맞춰 호환을 가른다. 호환표는 차급·capability·구역·차고. **무게·부피 용량은 호환에 없다.**

[^hall]: **Hall 조건** — 이분 매칭 이론의 조건("어떤 부분집합을 잡아도 그 이웃이 더 많다")을 빌린 이름. 여기서는 "호환 차종이 적은 요청 그룹부터 누적해 갔을 때, 각 누적 단계마다 그 그룹들이 탈 수 있는 차량만으로 부피·무게·방문 수를 감당하는가". T38이 없으면 큰 차가 큰 차 금지 존에 배정된다.

[^lexicographic-score]: **사전식 점수** — `long[]`을 앞부터 비교. 기본 축은 **(미배정 수, 사용 차량 수, 총 거리 m, 총 운행시간 s)**. 가중합으로 뭉개지 않는다. hard 위반은 점수 축이 아니다.

[^determinism]: **결정적** — 같은 입력에 항상 같은 출력. 재검증·벤치마크를 위해 초기해에 난수를 금지한다.

[^bank]: **bank** — 경로에 못 들어간 요청 ID 집합. 모든 요청은 경로 또는 bank 중 정확히 하나(XOR). bank가 남은 해도 유효 — 미배정 축이 나쁠 뿐.

[^problem]: **`Problem`** — 요청·차량·차고·이동표·호환표를 검증·정규화해 **동결**한 객체. 탐색은 읽기만.

[^request]: **`Request`** — 주문 하나. `PICKUP_DELIVERY`(방문 2)·`DELIVERY_ONLY`·`PICKUP_ONLY`. 배정·제거의 원자 단위라 pickup만 따로 옮기지 않는다.

[^pair]: **pair** — 한 `Request`를 구성하는 방문의 짝. PD는 pickup+delivery 두 방문이 항상 같이 움직인다. 단일 패턴은 방문 1개여도 같은 원자 단위로 다룬다.

[^profile]: **`Profile`** — 고객별 hard/soft와 점수 축. core에 `if (customerId == …)`를 두지 않고 이 객체를 갈아 끼운다.

[^oracle]: **오라클** — "이 후보가 유효한가·비용이 얼마인가"를 정확히 답하는 검사기. 여기서는 `InsertionSearch.candidatesFor` / `candidates`. H23의 `key`·`Load`는 근사 점수일 뿐 오라클이 아니다.

[^spi]: **SPI** — 구현체를 여러 개 꽂는 인터페이스. `ConstructionHeuristic` 24개가 등록된다.

[^hard-soft]: **hard / soft** — hard는 어기면 무효(용량·시간창·정차 한도·구역·차급), soft는 점수가 나빠질 뿐. 삽입은 hard 통과 후보만.

[^abstain]: **기권** — 기법이 이 `Problem`에서 성립하지 않아 실행하지 않음. 실패가 아니다. H23는 2026-09-04 이후 항상 `false`. 잘림(`truncated`)은 기권이 아니다.

[^stop-limit]: **정차 한도 (`effectiveMaxStopCount`)** — 한 경로가 방문할 수 있는 최대 횟수. 차량별 값이 있으면 그 값, 없으면 전역값, 둘 다 없으면 제약 없음. 소량 주문이 많은 존에서는 부피보다 이 한도가 먼저 찬다 — `key`가 이 축을 부피와 함께 보는 이유다.

[^frontier]: **프론티어** — 한 존에 대해 실제로 시도할 차량 조합만 추린 집합. (a) 딱 덮는 최소 (b) 한 대 모자란 최대 (c) 아무것도 안 줌. 잎 수가 `MAX_FRONTIER_LEAVES`를 넘으면 열거 순서 접두만 남긴다 (X29).

[^best-fit]: **best-fit** — 들어갈 수 있는 bin 중 남는 공간이 가장 적어지는 bin. H23의 키는 이 규칙을 "부피 − 평균 × 남은 정차" 축에 적용하고, 그 부호로 정차 낭비를 먼저 걸러낸다.

[^units]: **단위** — 무게·부피는 ×1000 FLOOR한 `long`, 거리는 meter, 시간은 초. T40의 4,000은 "4 단위".

[^fixture]: **fixture** — 테스트·실측용 고정 입력. 실물 fixture는 `data/win_poc_case_floor.json` (주문 452·차량 31).

[^propagation]: **전파 (`RoutePropagator`)** — 방문 순서가 주어지면 시각·적재·정차 수를 계산하고 hard를 검사해 Feasible/Infeasible로 답한다. 후보마다 경로 전체를 처음부터 다시. H23는 이 결과를 `candidatesFor`로만 받는다.

[^greedy]: **greedy** — 매 단계에서 지금 가장 좋아 보이는 선택을 하고 되돌리지 않음. H23의 존 내부 적재·1-1 교환(첫 성공)이 이것이다. 존 배정 DP는 전역.

[^initial-solution]: **초기해** — ALNS가 출발점으로 삼는 첫 `Solution`. 여기서는 24개 construction 결과 중 정식 평가 점수가 가장 좋은 것.
