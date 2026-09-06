# `ZoneQuotaSubsetFillConstruction` (H24) 코드 해설

> **성격**: 코드 해설 노트(비규범). 규범은 [heuristics 문서 §5 H24](../../implementation/stage-04-initial-solution-heuristics.md)이고,
> 쉬운 말 설명은 [초기해 24개 노트 §2 H24](../initial-solution-heuristics-h1-h24.md)다. 둘과 이 노트가 어긋나면 **규범이 이긴다.**
> 이 노트는 §2 H24를 코드 수준으로 연장할 뿐, 대체하지 않는다.
>
> **대상 독자**: CVRPTW[^cvrptw]를 아는 Java 개발자. 이 저장소의 용어(`Request`·pair·bank·profile 등)는 처음 본다고 가정하고,
> 처음 나올 때마다 각주로 풀었다. 각주는 문서 끝 [용어 각주](#용어-각주)에 모여 있다.
>
> **대상 파일**: `solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java` (**284줄, 2026-09-02** 기준 — 이후 1단계 개정 세 건에도 이 파일은 바뀌지 않았다).
> 공유 1단계 `ZoneQuotaAllocation.allocate`는 `ZoneQuotaAllocation.java` **817줄**(2026-09-06)이다 — **이 문서는 호출과 반환 필드만** 다루고,
> 표를 어떻게 채우는지는 [H23 노트](zone-quota-balanced-fill-construction.md)와 [존 배정 DP 노트](../zone-quota-allocation-dp.md)로 보낸다.
> 1단계 개정 근거: [scaling](../../implementation/stage-04-zone-quota-allocation-scaling.md) ·
> [zone-value-function](../../implementation/stage-04-zone-value-function.md) ·
> [zone-quota-frontier-budget](../../implementation/stage-04-zone-quota-frontier-budget.md).

---

## 0. 한 줄 요약

**"존[^zone]마다 차를 몇 대 줄지는 H23과 같은 전역 DP[^dp]가 정한다. 그 다음 존 안을 채울 때만 다르다 — 차 한 대마다 씨앗[^seed] 근처 풀[^pool]을 자르고, 용량 C에 들어가는 주문 조합 중 부피 합이 가장 큰 것을 표를 칸마다 채워 찾은 뒤(subset-sum[^subset-sum]), 오라클[^oracle]이 시간창에서 거절한 주문은 제외하고 다시 찾는다."**

| 이름 조각 | 뜻 |
|---|---|
| `zone-quota` | 1단계. 존에 **차종(유형) 대수 몫**을 전역으로 나눈다. H23과 **같은** `allocate` |
| `subset-fill` | 2단계. 차(bin)마다 주문 **부분집합**을 부피 DP로 골라 한꺼번에 싣는다. H23의 균형 greedy가 아님 |
| `Construction` | 초기해 구축 기법[^construction] — 빈 해에서 시작해 요청을 넣는다 |

실물 fixture[^fixture]에서 **2위**. 미배정 8, 차량 31. H23(미배정 0)에 지고 H3(15)보다 앞선다. 거리는 H23보다 약 6% 짧다(§8 — 개정 전후 숫자를 둘 다 적는다).

---

## 1. 프로젝트 안에서의 위치

### 1.1 초기해 포트폴리오의 한 칸 — H23과 1단계는 같고 2단계만 다르다

`InitialSolutionBuilder.build(problem, profile)`가 결정적 construction **24개**(H1~H24)를 전부 돌려 정식 평가에 넣고, 사전식 점수[^lexicographic-score]가 가장 좋은 하나를 고른다. H24는 24번째이고, 실물에서는 **2위**(§8).

```text
InitialSolutionBuilder.build(problem, profile)
  ├─ H1 … H22
  ├─ H23 ZoneQuotaBalancedFillConstruction     ← 같은 allocate, 존 안은 정차 예산 best-fit + 1-1 교환
  └─ H24 ZoneQuotaSubsetFillConstruction       ← 이 문서. 같은 allocate, 존 안은 seed·pool·subsetSum·오라클 피드백
  → 각 Solution을 Evaluator로 평가 → 사전식 최선 1개
```

포트폴리오 축으로는 H23과 같은 **① 전역 배분**이다 ([h1–h24 §4.2](../initial-solution-heuristics-h1-h24.md)). 1단계가 같아서 병렬 슬롯에는 H23만 남기는 쪽이고, H24의 "거리 6% 단축"은 ALNS가 줄이는 축이라 초기해 슬롯의 이유가 못 된다 — 그 판단은 쉬운 말 노트의 몫이고, 여기선 코드가 그 거리를 어떻게 만드는지만 본다.

### 1.2 `ConstructionHeuristic` (SPI[^spi])

```java
public interface ConstructionHeuristic {
    String id();                                   // "zone-quota-subset-fill"
    boolean abstains(Problem problem);             // H24는 항상 false — 기권하지 않는다
    Solution construct(Problem problem, Profile profile);
}
```

1. **난수 없음, 결정적**[^determinism]. 정렬은 문자열 ID로 끝나고 `HashMap` 순회로 승자를 고르지 않는다.
2. **반환 해는 항상 유효.** 못 넣은 요청은 bank[^bank]. 삽입은 전부 `InsertionSearch`라 hard 제약[^hard-soft]을 통과한 것만 경로에 있다.
3. **기권[^abstain] 없음.** `abstains`는 항상 `false`(42–44행). 차종 조합이 커도, 존이 없어도, 정차 한도가 없어도 기권하지 않는다(X20·X23). 근사(`truncated`)가 나와도 2단계는 그대로 돈다(X27).

### 1.3 다루는 자료형

| 타입 | 이 파일에서의 뜻 |
|---|---|
| `Problem`[^problem] | 동결된 입력 전체. 탐색은 읽기만 |
| `Request`[^request] | 배차의 원자 단위 (pair[^pair] 통째) |
| `Vehicle` | `maxVolume`·`maxWeight`·`effectiveMaxStopCount`. bin 순서 2키는 **부피** |
| `Solution` | `List<Route>` + bank. 불변 — 삽입마다 새 객체. 오라클이 실패하면 `trial`만 버리고 `current`는 그대로 |
| `Route` | `VehicleId` + 방문 목록. 빈 경로는 생성자가 막음 |
| `Profile`[^profile] | 고객별 hard/soft. 이 파일은 `InsertionSearch`에 넘기기만 한다 |
| `Allocation` | `allocate`의 반환. H24가 읽는 것은 `zones()`와 `vehiclesByZone()` (leftover 순서는 `types()`도) |
| `Zone` | `zoneId` + `members`(그 존의 `RequestId` 목록) |
| `Candidate` | "이 차의 이 위치에 넣으면 Δ비용이 이만큼" |

---

## 2. 기대는 부품

### 2.1 `ZoneQuotaAllocation.allocate` — 호출만, 표 채우기는 여기 없음

```47:48:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
    public Solution construct(Problem problem, Profile profile) {
        Allocation allocation = ZoneQuotaAllocation.allocate(problem);
```

H24는 H23과 **같은** `ZoneQuotaAllocation.allocate(problem)`을 부른다. 받는 것은 존 목록 `zones`와 존별 차량 `vehiclesByZone`이다. **한 차량은 정확히 한 존에만** 나타난다(대수를 차감하며 소비). 배정 없는 존은 빈 목록. `truncated`는 읽지 않는다 — 근사여도 2단계는 그대로(X27). 근사가 되는 조건은 상한 셋(상태 총량 `MAX_TOTAL_STATES` 262,144 · 존당 열거 잎 `MAX_FRONTIER_LEAVES` 4,194,304 · 상태당 후보 `MAX_FRONTIER_CANDIDATES` 1,024)이고 실물 fixture는 어느 것에도 닿지 않는다.

표를 어떻게 채우는지(유형 묶기·존당 1회 프론티어 열거·값 `(부족, 대수, 낭비, 결손)`·역추적)는 이 문서의 소유가 아니다. [H23 노트](zone-quota-balanced-fill-construction.md)와 [존 배정 DP 노트](../zone-quota-allocation-dp.md).

그 다음 H24는 **자기 루프**로 존을 채운다. `ZoneQuotaBalancedFillConstruction.fill`을 **부르지 않는다.** H23의 1-1 교환도 없다. leftover[^leftover] pass의 **요청 순서만** `ZoneQuotaBalancedFillConstruction.requestOrder`를 빌려 쓴다(117행).

### 2.2 `InsertionSearch` — 이 기법이 실제로 부르는 메서드만

"어느 차에 어느 부분집합을"만 이 파일이 정하고, **"넣을 수 있는가·어느 위치가 싼가"는 전부 `InsertionSearch`에** 맡긴다.

| 메서드 | 하는 일 | H24 |
|---|---|---|
| `emptySolution` | 전 요청이 bank인 빈 해 | 시작점 (49행) |
| `candidatesFor(..., r, v)` | **차량 v 하나**의 삽입 위치, 비용 오름차순. 비호환·불가 = 빈 목록. 경로 없으면 새 경로 후보 | 오라클 순서화(85행) · 존 잔여 첫 성공(106행) |
| `apply` | 후보를 적용한 새 `Solution`. 그 요청은 bank에서 빠짐 | 위 두 곳 + leftover (121행) |
| `candidates()` | 기존 경로 전부 + 미사용 호환 1대 | leftover pass만 (119행) |
| `checkOuterLoop` | 바깥 루프 상한 초과 시 `IllegalStateException` | bin 피드백 `pool.size()+1`(72행) · leftover `n`(118행) |
| `travel().timeSec(from, to, speed)` | 이동표 시간(초). 좌표가 아님 | seed·pool (157·163행) |

**부르지 않는 것.** `InsertionSearch.Cache`, `standaloneDistMeter`, `openRoute`, `remove`. H23 1-1 교환의 `remove`는 여기 없다.

`candidatesFor` (InsertionSearch 62–70행): 호환이 아니면 즉시 빈 목록. 호환은 차급[^vehicle-class]·capability·구역·차고 앵커이지 **무게·부피 용량이 아니다**. 용량·시간창·정차 한도·`ZONE_MIX`[^zone-mix]는 오라클(`RoutePropagator`[^propagation] + profile hard)이 본다.

`cands.getFirst()`는 `(새 경로 여부, Δ거리, Δ운행시간)` 사전식 최선(`Candidate.byCost`). H24는 그 위치만 쓰고 2·3순위를 보지 않는다 (regret 없음).

`anchor`(InsertionSearch 330행) = 첫 방문 side (pickup이 있으면 pickup, 없으면 delivery). seed·pool의 위치는 `anchor(...).locationId()` (182–184행) — **위도·경도가 아니다.**

---

## 3. 알고리즘 전체 흐름

```text
abstains?  항상 false
allocation = ZoneQuotaAllocation.allocate(problem)     // H23과 동일. 표 채우기는 다른 문서
current = 빈 해
leftovers = []
for z in allocation.zones():
  unassigned = z.members 복사
  bins = binOrder(z에 배정된 차)                       // (존 멤버 호환 수 ASC, maxVolume DESC, VehicleId ASC)
  for b (i번째 bin):
    candidates = unassigned 중 b 호환, unassigned 순서 유지. 없으면 다음 bin
    pool = seed 근처 접두 (정차 한도 없으면 후보 전부)
    nmax = min(B_b, |pool|)  (B 부재 = |pool|)
    nmin = min(nmax, lowerBound(...))
    excluded = ∅
    반복 (≤ |pool|+1, 매 반복 제외 +1 또는 nmax −1):
      S = subsetSum(pool − excluded, nmin, nmax)
      S = ∅ → bin 종료
      trial = current 에서 S를 부피 DESC로 candidatesFor(b) 삽입. 실패 집합 F
      F = ∅ → current = trial, unassigned −= S, 다음 bin
      F ≠ ∅ → trial 폐기(current 불변), excluded ∪= F, nmax = |S| − |F|
              nmax < max(1, nmin) 이면 nmin = 0
  unassigned 잔여 → bins 순서로 candidatesFor 첫 성공. 남으면 leftovers
leftover pass: requestOrder(H23과 같음)로 candidates() 첫 원소에 삽입. 0개면 bank
```

바깥에서 `fill`을 부르는 경로는 없다.

---

## 4. 단계별 상세

### 4.1 `construct` 골격 — allocate 다음이 자기 루프

```47:125:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
    public Solution construct(Problem problem, Profile profile) {
        Allocation allocation = ZoneQuotaAllocation.allocate(problem);
        Solution current = InsertionSearch.emptySolution(problem);
        List<RequestId> leftovers = new ArrayList<>();
        for (Zone zone : allocation.zones()) {
            List<RequestId> unassigned = new ArrayList<>(zone.members());
            List<Vehicle> bins = binOrder(problem, allocation.vehiclesByZone().get(zone.zoneId()), zone.members());
            // … bin 루프 · 존 잔여 · leftovers.addAll(unassigned)
        }
        for (RequestId id : ZoneQuotaBalancedFillConstruction.requestOrder(problem, allocation, leftovers)) {
            InsertionSearch.checkOuterLoop(ID, ++iterations, problem.requests().size());
            List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, id);
            if (!cands.isEmpty()) {
                current = InsertionSearch.apply(problem, current, cands.getFirst());
            }
        }
        return current;
    }
```

`unassigned` 초기값 = `zone.members()`. 멤버 순서는 `allocate` 쪽 `zones()`가 `sortedRequestIds`(RequestId ASC)로 모은 뒤 존 순서만 다시 정렬한 것. 호환 차량 0대인 요청은 존에 안 들어간다(X2) — H24 leftover는 그 요청을 다시 훑지 않는다. 빈 해 bank에 처음부터 남아 끝난다.

### 4.2 bin 순서 — 까다로운 차부터

```127:145:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
    /** bins 순서 = (존 요청 중 호환 수 ASC, maxVolume DESC, VehicleId ASC) — 까다로운 차부터. */
    private static List<Vehicle> binOrder(Problem problem, List<VehicleId> assigned, List<RequestId> members) {
        ...
        bins.sort(Comparator.<Vehicle>comparingInt(v -> { /* members 중 호환 수 */ })
                .thenComparing(Comparator.comparingLong(Vehicle::maxVolume).reversed())
                .thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
        return bins;
    }
```

| 순서 | 키 | 방향 | 코드 |
|---:|---|---|---|
| 1 | 존 **전체** 멤버 중 이 차와 호환인 수 | ASC | `zone.members()` — unassigned가 줄어도 **다시 세지 않는다** |
| 2 | `maxVolume` | DESC | **`maxWeight`·정차 한도·근무창은 bin 순서에 없다** (H3 차량 순서의 2키가 무게인 것과 다름) |
| 3 | `VehicleId` | ASC | `BY_VEHICLE_ID` 문자열 |

규범 §5 H24: "까다로운 차부터". 호환 수가 적은 차가 먼저 풀에서 고른다.

### 4.3 후보 목록 — 정렬하지 않는다

```56:64:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
                List<RequestId> candidates = new ArrayList<>();
                for (RequestId id : unassigned) {
                    if (problem.compatibleVehicles(id).contains(vehicle.id())) {
                        candidates.add(id);
                    }
                }
                if (candidates.isEmpty()) {
                    continue;
                }
```

`candidates`는 unassigned를 필터만 한 목록이다. **여기서 정렬하지 않는다.** 차고가 없을 때 seed = `candidates.getFirst()`(154행)가 가리키는 것은 "필터 후 첫 원소"이지, 이 시점에 RequestId로 다시 줄 세운 결과가 아니다. (존 멤버가 RequestId ASC로 들어와 unassigned가 그 상대 순서를 지키므로, 실무적으로는 ASC 첫 잔여와 같아진다. 코드가 한 번 더 정렬하는 것은 아니다.)

### 4.4 씨앗(seed) — 이동표 시간, 좌표 아님

```151:160:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
    private static List<RequestId> pool(Problem problem, Vehicle vehicle, List<RequestId> candidates) {
        int speed = problem.resolvedSpeedKmH(vehicle.id());
        Optional<LocationId> reference = vehicle.startDepot().or(vehicle::endDepot);
        RequestId seed = candidates.getFirst();
        if (reference.isPresent()) {
            seed = candidates.stream()
                    .max(Comparator.<RequestId>comparingInt(id -> problem.travel().timeSec(reference.get(), anchorLocation(problem, id), speed))
                            .thenComparing(InsertionSearch.BY_REQUEST_ID.reversed()))
                    .orElseThrow();
        }
```

비교자 그대로:

1. `travel.timeSec(startDepot 없으면 endDepot → anchor 위치, resolvedSpeedKmH)` — **`.max`이므로 시간 DESC.**
2. 동률은 `BY_REQUEST_ID.reversed()`를 `.max`에 붙인다 → 문자열 ID **ASC**(작은 ID가 reversed에서 "더 크다"). 규범의 `(timeSec DESC, RequestId ASC)`와 같다.

차고가 **둘 다 없으면** 154행의 `getFirst()`가 그대로 씨앗이다. 위도·경도로 거리를 재지 않는다. ZONE_29가 70 km인 이유(규범 §5 H24)는 이 줄이 좌표가 아니라 이동표 시간이라서다.

### 4.5 풀(pool) — ⌈1.5 × maxVolume⌉ 과 정차 한도

```161:179:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
        LocationId seedLocation = anchorLocation(problem, seed);
        List<RequestId> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator.<RequestId>comparingInt(id -> problem.travel().timeSec(seedLocation, anchorLocation(problem, id), speed))
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        if (vehicle.effectiveMaxStopCount().isEmpty()) {
            return ordered;
        }
        long target = (POOL_NUMERATOR * vehicle.maxVolume() + POOL_DENOMINATOR - 1) / POOL_DENOMINATOR;
        int minimum = vehicle.effectiveMaxStopCount().getAsInt();
        ...
            if (volume >= target && pool.size() >= minimum) {
                break;
            }
```

- 정렬: `timeSec(seedAnchor → 각 anchor)` ASC, RequestId ASC. 씨앗 자신은 시간 0이라 맨 앞.
- **정차 한도 부재 → 후보 전부** (165–167행). 부피 목표로 자르지 않는다.
- 한도가 있으면 접두를 쌓아 `Σvolume ≥ ⌈1.5 × maxVolume⌉` **그리고** `size ≥ B`가 되는 순간 끊는다. 한쪽만 만족하면 계속.
- `⌈1.5 × V⌉` = `(3V + 1) / 2` (정수, 28–31행 `POOL_NUMERATOR=3`, `POOL_DENOMINATOR=2`). **재량 상수, Stage 8이 조정한다.**

부피만 보고 존 폭 70 km의 소량 주문을 한 차에 몰면 시간창을 어긴다(규범). pool이 그 절단이다.

### 4.6 nmax · nmin — T42 숫자로

```66:68:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
                int nmax = vehicle.effectiveMaxStopCount().isPresent()
                        ? Math.min(vehicle.effectiveMaxStopCount().getAsInt(), pool.size()) : pool.size();
                int nmin = Math.min(nmax, lowerBound(bins, i, unassigned.size()));
```

```186:205:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
    /** nmin = max(|미적재| − Σ_{뒤 bin} B, ⌊0.8 × |미적재| × maxVolume_b / Σ_{i 이후 bin} maxVolume⌋). 뒤 bin에 한도 부재가 있으면 첫 항은 0. */
    private static int lowerBound(List<Vehicle> bins, int index, int remaining) {
        ...
        long byStops = unlimited ? 0L : Math.max(0L, remaining - laterStops);
        long proportional = capacitySum == 0L ? 0L
                : (LOWER_BOUND_NUMERATOR * remaining * bins.get(index).maxVolume()) / (LOWER_BOUND_DENOMINATOR * capacitySum);
        return (int) Math.max(byStops, proportional);
    }
```

- `nmax = min(B, |pool|)`. B 없으면 `|pool|`.
- `laterStops`는 **뒤** bin만 (`j > index`). 뒤 bin 하나에라도 정차 한도가 없으면 첫 항은 **0**(규범·주석과 같음).
- `capacitySum`은 **자기 포함 i 이후** 부피 합. 정수 나눗셈 = 내림 = `⌊ ⌋`.
- `0.8 = 8/10` (`LOWER_BOUND_NUMERATOR=8`, `LOWER_BOUND_DENOMINATOR=10`). **재량 상수, Stage 8이 조정한다.**

**T42 숫자 (용량 8, 정차 3, bin 2, 요청 6건).** remaining=6, 두 차 모두 B=3, maxVolume=8.

```text
뒤 bin 정차 합 laterStops = 3
byStops = max(0, 6 − 3) = 3
capacitySum = 8 + 8 = 16
proportional = ⌊8 × 6 × 8 / (10 × 16)⌋ = ⌊384 / 160⌋ = ⌊2.4⌋ = 2
lowerBound = max(3, 2) = 3
nmax = min(3, |pool|)
nmin = min(nmax, 3) = 3     (pool이 3 이상이면)
```

하한이 하는 일 — 다음 절의 표.

### 4.7 subset-sum — 칸마다 채워 가장 큰 부피 조합을 찾는다

용량 C에 들어가는 주문 조합 중 부피 합이 가장 큰 것을, 표를 칸마다 채워 찾는다. 은유가 아니라 T41 숫자다.

**T41 입력.** 용량 10, 요청 부피 `5, 4, 4, 3, 2, 2`, bin 2개. (코드 단위는 ×1000이라 5,000 등. 아래 표는 테스트 주석과 같은 10 단위.)

**탐욕 best-fit decreasing** — 큰 것부터, 남는 칸이 가장 빠듯한 bin에 넣는다.

| 단계 | 넣는 것 | bin1 잔여 | bin2 잔여 |
|---|---:|---:|---:|
| 5 | bin1 | 5 | 10 |
| 4 | bin1 (잔여 5가 더 빠듯) | **1** | 10 |
| 4 | bin2 | 1 | 6 |
| 3 | bin2 | 1 | 3 |
| 2 | bin2 | 1 | **1** |
| 2 | 어느 쪽도 잔여 2 미만 → **1건 미배정** | 1 | 1 |

결과: bin1 `{5,4}`=9, bin2 `{4,3,2}`=9, 남은 `2`. 두 bin 모두 1이 남아 마지막 2를 못 넣는다.

**표로 찾는 조합.** 한 bin 용량 10. 칸 `f[v]` = 부피 합이 v인 부분집합이 있으면 그 최소 무게(이 fixture는 무게=부피라 도달 여부만 보면 된다). 물건을 하나씩, 칸을 **뒤에서부터** 갱신하는 0/1 knapsack[^knapsack] — 같은 물건을 두 번 쓰지 않는다.

T41 단위 테스트는 6건을 RequestId ASC(`5,4,4,3,2,2`)로 넣고 n 축 없이(`nmin=0, nmax=0`, 정차 한도 부재) 돌린다.

| 넣은 물건 | 새로 도달하는 v (≤10) |
|---|---|
| 5 | 0, 5 |
| 4 | 0, 4, 5, 9 |
| 4 | 0, 4, 5, 8, 9 |
| 3 | 0, 3, 4, 5, 7, 8, 9 |
| 2 | 0, 2, …, **10** (`8+2`, `{4,4,2}`) |
| 2 | 10은 이미 있음 (같은 무게라 덮어쓰지 않음, `candidate < f[index]`) |

선택 = **v DESC 첫 도달 칸** → v=10. 역추적 한 예: `{4,4,2}`. 남은 `{5,3,2}`도 합 10. **두 bin이 정확히 10·10, 미배정 0.** T41 `construct`가 이것을 고정한다.

왜 탐욕이 졌는가. `{5,4}`는 한 칸을 9로 채워 짝 `{3,2,2}`와 `{4}`를 갈라 놓는다. 표는 `{5,3,2}`와 `{4,4,2}`처럼 **합이 10인 분할**을 칸 10에서 바로 고른다.

#### 코드가 채우는 표

```214:272:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
     * subsetSum — 상태 f[v][n] = 양자화 부피 합 v·방문 수 합 n인 부분집합의 최소 weight (maxWeight 초과는 버린다).
     * 부피는 올림 양자화라 절대 넘치지 않는다. 선택 = (v DESC, n ASC) 첫 상태, nmin ≤ n ≤ nmax — 없으면 nmin = 0으로 다시.
     * 정차 한도 부재면 n 축이 없다 (X23). items는 RequestId ASC로 받는다.
```

1차원으로 펼친 배열: `index = v * width + c`, `width = nmax+1`(한도 있으면). `f[0]=0`, 나머지 `Long.MAX_VALUE`.

**양자화[^quantization]는 올림(CEILING)** 이라 실제 부피 합이 용량을 넘기지 않는다.

```text
u = max(1, ⌈maxVolume / 20,000⌉)     // VOLUME_BUCKETS = 20_000, 재량 상수, Stage 8이 조정한다
C = ⌊maxVolume / u⌋
vol_i = max(1, ⌈totalVolume_i / u⌉)   // 혼자서 C를 넘으면 capacity+1 → 선택 불가
```

실제 합 ≤ (양자화 합)×u ≤ C×u ≤ maxVolume. T41 둘째 단언: 용량 30,001 → u=2, C=15,000. `15,000+15,001`의 양자화 합 7,500+7,501=15,001 > 15,000이라 둘을 같이 고르지 않는다. 고른 합은 `15,001+1=15,002` ≤ 30,001 (v DESC가 더 큰 칸 7,502를 선호).

**선택** (`select`, 274–282행):

```text
for v = C .. 1:
  for c = nmin .. nmax:          // n ASC — 같은 부피면 방문 적은 쪽
    f[v][c] < ∞  → 그 (v, c)
없으면 nmin>0 일 때 nmin=0으로 한 번 더
```

빈 집합(v=0)은 고르지 않는다. 한도 부재면 `counted=false`, n 축 폭 1, `visits[i]=0`, 넘어온 nmin·nmax를 무시(X23).

**역추적**: items를 **뒤에서 앞으로**. `taken[i]` 비트면 포함하고 `v −= vol_i`, `c −= visits[i]`. 그다음 `chosen.sort(BY_REQUEST_ID)` (270행).

방문 수 `c_i` = `ZoneQuotaAllocation.visitCount` — 단일 1, `PICKUP_DELIVERY` 2.

주석·규범은 "items는 RequestId ASC로 받는다"고 한다. **호출부는 그렇게 정렬하지 않는다** — `pool` 순서(이동시간 ASC)에서 `excluded`만 뺀 목록을 그대로 넘긴다(73–78행). 0/1 knapsack의 최소 무게 값 자체는 순서에 불변이지만, 같은 무게의 다른 부분집합이 있으면 `candidate < f`라 **먼저 도달한 쪽**이 taken에 남고 역추적이 그 쪽을 따른다. T41 단위 테스트는 `sortedRequestIds`를 직접 넣어 ASC를 강제한다.

무게 한도: `candidate <= maxWeight && candidate < f[index]`일 때만 갱신. 용량을 넘는 무게 조합은 칸에 없다.

### 4.8 오라클 피드백 (X22) — T43 한 바퀴

```82:102:solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaSubsetFillConstruction.java
                    Solution trial = current;
                    List<RequestId> failed = new ArrayList<>();
                    for (RequestId id : sortedByVolumeDesc(problem, chosen)) {
                        List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, trial, id, vehicle.id());
                        if (cands.isEmpty()) {
                            failed.add(id);
                        } else {
                            trial = InsertionSearch.apply(problem, trial, cands.getFirst());
                        }
                    }
                    if (failed.isEmpty()) {
                        current = trial;                                                // bin 확정
                        unassigned.removeAll(chosen);
                        break;
                    }
                    excluded.addAll(failed);                                            // 이 반복의 삽입은 버린다 (해는 불변)
                    nmax = chosen.size() - failed.size();
                    if (nmax < Math.max(1, nmin)) {
                        nmin = 0;
                    }
```

삽입 순서 = `totalVolume` DESC, RequestId ASC (`sortedByVolumeDesc`, 207–211행). 실패해도 **나머지를 계속** 넣어 F를 모은다. F가 하나라도 있으면 `trial` 전체를 버린다 — `current`는 불변. `Solution`이 불변 객체라 롤백 코드가 없다.

**T43 한 반복.** 차 1대 용량 10, 정차 3. R1=4(근처), R2=3(근처), R3=3(111 km, 창 08:00–08:01). DP는 셋 다 고른다 — 시간창은 표에 없다.

| 단계 | 상태 |
|---|---|
| 시작 | nmin=3, nmax=3, excluded=∅, pool={R1,R2,R3} |
| subsetSum | S={R1,R2,R3} (부피 10, n=3) |
| 순서화 | 부피 DESC: R1 성공 → R2 성공 → R3 `candidatesFor` 빈 목록 (창). F={R3} |
| 피드백 | `current` 그대로. excluded={R3}. nmax=3−1=2. 2 < max(1,3) → **nmin=0** |
| 다음 | items={R1,R2}, S={R1,R2}, F=∅ → 확정. unassigned에서 R1·R2 제거 |
| 존 잔여 | R3를 V1에 재시도 → 또 실패 → leftover → leftover pass도 실패 → **bank={R3}** |

루프 상한은 `checkOuterLoop(..., pool.size() + 1)` (72행). 마지막에 items가 비어 break하는 한 번을 포함하므로 `+1`이다. 매 실패마다 excluded가 ≥1 늘거나 nmax가 ≥1 준다 → 무한 루프 없음(T43, X22).

### 4.9 존 잔여 · leftover pass

bin이 다 끝나면 남은 `unassigned`를 **bins 순서**(binOrder)로 `candidatesFor` 첫 성공에 넣는다(104–113행). 어느 bin에도 안 들어가면 leftover.

그 leftover를 H23과 같은 `requestOrder` — `(호환 유형 수 ASC, totalVolume DESC, RequestId ASC)` — 로 줄 세워 `candidates()`에 넣는다. 기존 경로든 아직 안 쓴 차의 새 경로든. 후보 0개면 bank.

H23 leftover와의 차이(코드 사실): H23은 1-1 교환 실패분 + 호환 0대(X2)를 bank에서 한 번 더 모은다. H24 leftover는 **존 루프가 남긴 unassigned뿐**이다. X2는 존에 없으므로 빈 해 bank에 처음부터 남아, leftover가 다시 시도하지 않는다. 결과는 같다(오라클이 어차피 빈 목록).

존 잔여 루프에는 `checkOuterLoop`가 없다. leftover만 `n`으로 센다.

---

## 5. 결정성 · 종료 · 복잡도

### 5.1 결정성

| 지점 | 고정 방법 |
|---|---|
| 존·배정 | `allocate` (결정적. DP 노트) |
| bin | 호환 수 ASC, `maxVolume` DESC, `VehicleId` 문자열 |
| seed | `timeSec` DESC, RequestId ASC (`.max` + `BY_REQUEST_ID.reversed`) |
| pool | `timeSec(seed→)` ASC, RequestId ASC, 접두 절단 |
| subsetSum 갱신 | items 순회 + `candidate < f` (동률은 기존 칸) |
| 선택 | v DESC, n ASC. 역추적 후 RequestId ASC |
| 오라클 삽입 | 부피 DESC, RequestId ASC, `cands.getFirst()` = `byCost` |
| leftover | H23 `requestOrder` |
| 동률 | 전부 문자열 ID. `HashMap` 순회 없음 |

### 5.2 종료 (규범 §4.3 · §5 H24)

- 존 배정 DP: 구조 상한 (`MAX_TOTAL_STATES` · 존당 1회 열거의 `MAX_FRONTIER_LEAVES` · 상태당 `MAX_FRONTIER_CANDIDATES`). 기권 없음.
- bin당 재DP: 매 반복 excluded +1 또는 nmax −1 → **≤ |pool|** (규범). 방어 카운터는 `pool.size()+1`(마지막 빈 반복).
- leftover: ≤ n.
- 존 잔여: unassigned 한 바퀴, 카운터 없음. 삽입 실패는 leftover로만 간다.

넘으면 `IllegalStateException` — 품질이 아니라 버그. 조용히 자르지 않는다.

### 5.3 복잡도 (규범 §5 표 H24 행)

```text
H24 | zone-quota-subset-fill | zone-quota | 전 패턴 | ≤ n + m, bin당 재DP ≤ P
    | 배정 DP + bin당 O(P · C · B) × 반복 (P = pool, C ≤ 20,000, B = 정차 한도) + O(P · L²)
```

subsetSum 1회 = O(|pool| · C · B) 셀. 규범 불릿: fixture 최대 87 × 17,640 × 29 ≈ 44M, pool은 α로 잘려 실제 ≈ 40건. T25 합성(용량 10⁶)은 u=50이라 C=20,000.

실측 소요는 기록이 여럿이다. 고르지 않고 적는다: 규범 §5 H24 **773 ms**(2026-09-02) · 쉬운 말 §1 **836 ms\***(스케일링 개정 후 재실측, 개정 전 786 ms).

---

## 6. 코드를 읽으며 눈여겨볼 점

규범과 코드의 1:1, 그리고 쉬운 말(§2 H24)과 어긋나 보이는 곳. **수정 제안이 아니라 읽기 보조.**

1. **`fill`을 부르지 않는다.** allocate 다음이 H24 자기 루프다. leftover 순서만 H23 `requestOrder`.
2. **오라클이 이 기법의 유일한 되돌림이다.** 경로 간 relocate·1-1 교환은 없다. 실패하면 그 bin의 이번 조합만 버리고 같은 차에서 다시 고른다.
3. **subsetSum은 시간창을 모른다.** T43이 셋 다를 고르는 이유. 거절은 `candidatesFor`가 한다.
4. **양자화는 올림**이라 실제 합이 용량을 넘기지 않는다. 내림이면 칸은 되고 실물은 넘칠 수 있다.
5. **선택 키는 (부피 DESC, 방문 ASC)** 이지 무게 최소가 아니다. `f`의 최소 무게는 "무게 한도 안에서 그 칸에 도달했는가"의 필터다.
6. **items 순서.** 주석·규범은 RequestId ASC, 호출부는 pool 순서. 단위 테스트 T41·T42의 `subsetSum(...)` 직접 호출은 ASC를 넣는다.
7. **seed는 먼저 `getFirst()` 한 뒤 차고가 있으면 덮어쓴다.** 좌표가 아니다.
8. **`0.8`·`1.5`·`20,000`은 재량 상수**(Stage 8). `Problem`은 안 바뀌고 `Solution`도 불변.
9. **거리는 약 6% 짧다**는 쉬운 말 §2 H24의 주장이다. 숫자는 §8. 미배정 축이 앞서므로 포트폴리오에서는 H23이 이긴다.

---

## 7. 경계 상황 (규범 §7 — X21 / X22 / X23)

| 상황 | 동작 | 번호 |
|---|---|---|
| 존 배정 DP가 어떤 존도 못 덮음 (공급 < 수요) | DP는 완주(빈 집합 전이). 그 존 `vehiclesByZone`는 빈 목록 → bins 0 → 멤버 전부 leftover → leftover pass가 기존 경로·미사용 차에 시도 | **X21** |
| subsetSum이 고른 요청을 오라클이 거절 | 그 요청을 excluded에 넣고 nmax 축소 후 재DP. bin당 ≤ \|pool\|. `current`는 그대로 | **X22** |
| 정차 한도 부재 | pool = 후보 전부. subsetSum은 n 축 없이 부피만 (nmin·nmax 무시). 기권 아님 | **X23** |
| 이 bin의 candidates 0건 | `continue` — 그 차는 이 단계에서 경로를 안 연다 | 62–64행 |
| subsetSum이 빈 목록 | bin 종료 | 79–80행 |
| nmax=0 또는 items 소진 | bin 종료 | 75–76행 |
| 호환 차량 0대인 요청 | 존에 안 들어감. 빈 해 bank에 잔류 | X2 |
| `truncated == true` (상한 셋 중 하나 작동 — X20·X29·X42) | H24는 필드를 안 읽는다. 2단계·leftover·평가는 그대로 | X27 |
| 차종 ≥ 65 | 알려진 한계(마스크 `long`). `abstains`는 계속 false | X28 |
| profile hard가 강해 삽입 0건 | 전부 bank. Feasible이므로 정상 | X14 |

X22를 코드로: `failed`가 비지 않으면 `current = trial`을 하지 않고 `excluded.addAll(failed)`. T43이 bank={R3}·경로={R1,R2}·`StructureCheck` 0·`Evaluator` Feasible을 고정한다.

---

## 8. 테스트와 실측

| 테스트 | 고정하는 것 |
|---|---|
| T41 `subsetSumFillsExactly` | §4.7 그대로 — 용량 10 bin 2개·`5,4,4,3,2,2`. 단독 `subsetSum` 합 10. `construct`는 두 경로 각 10, bank 빈 집합. 올림 양자화는 30,001에서 15,000+15,001을 같이 고르지 않음 |
| T42 `countLowerBoundSpreadsSmallRequests` | §4.6 — 용량 8·정차 3 bin 2개·`4,4,3,3,1,1`. nmin=0이면 `{4,4}`(2건). nmin=3이면 3건 합 8. `construct`는 전량 배정, 경로마다 방문 3 |
| T43 `oracleFeedbackExcludesInfeasibleAndTerminates` | §4.8 — DP는 3건을 고르고 오라클이 R3를 거절. 재DP가 \|pool\| 안에 끝, bank={R3}, Feasible (X22) |

T42의 **단위** `subsetSum`은 6건 전부를 넣는다. `{4,4}`가 첫 bin을 채우면 잔여 `{3,3,1,1}`=8이지만 정차 4 > 3이라 둘째 bin이 1건을 남긴다. nmin=3이 첫 bin에 작은 주문을 섞는다. `construct`는 이 하한에 **pool 절단이 겹친다**(씨앗이 가장 먼 1이라 접두가 다른 4를 잘라 낼 수 있음). 테스트는 둘을 분리해 단언한다.

실물 실측 — **두 날짜의 숫자를 같이 적는다. 하나를 고르지 않는다.**

쉬운 말 §1 표 (2026-09-04 단일 실행, 스케일링 개정 후 소요 \*). 값 함수 개정(2026-09-05) 뒤 H24 점수 네 축은 그 표에 안 들어 있고, 규범 §5 H23 불릿·[zone-value-function §5.5](../../implementation/stage-04-zone-value-function.md)에 따로 있다.

| 출처 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 |
|---|---:|---:|---:|---:|---:|
| §1 표 · survey §2.5 · 규범 §5 H24 (2026-09-02) | **8** | **31** | **3,942,905** | **974,992** | 773 ms / 836 ms\* |
| 값 함수 개정 후 (2026-09-05, 규범 §5 H23 불릿 · zone-value-function §5.5) | **8** | **31** | **3,914,015** | **975,885** | (같은 배정이라 2단계 거리만 변동) |
| 존당 1회 열거 후 (2026-09-06, frontier-budget §5.4) | 8 | 31 | 3,914,015 | 975,885 | 배정이 그대로라 점수도 그대로. 차종 31종 변형에서만 5 → 4로 좋아졌다 |

순위는 그대로 **2위**. 대조:

| 순위 | 기법 | 미배정 | 거리(m) 2026-09-02 | 거리(m) 2026-09-05 |
|---:|---|---:|---:|---:|
| 1 | H23 `zone-quota-balanced-fill` | **0** | 4,198,408 | 4,194,052 (T52) |
| **2** | **H24 `zone-quota-subset-fill`** | **8** | **3,942,905** | **3,914,015** |
| 3 | H3 `vehicle-zone-fill` | 15 | 4,515,433 | (값 함수와 무관) |

쉬운 말 §2 H24의 "거리 6% 더 짧다":

- 2026-09-02: (4,198,408 − 3,942,905) / 4,198,408 ≈ **6.1%**
- 2026-09-05: (4,194,052 − 3,914,015) / 4,194,052 ≈ **6.7%**

둘 다 "약 6%"와 맞다. 사전식 1축(미배정)에서 H23이 이긴다.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env
mvn test -pl solver-core -Dtest=ZoneQuotaSubsetFillConstructionTest
```

---

## 용어 각주

[^cvrptw]: **CVRPTW** — 용량·시간창이 있는 차량 경로 문제. 이 저장소의 **RPDPTW**는 거기에 pickup-and-delivery와 실무 제약(차급·구역·정차 한도·근무창)을 더한 "rich" 변형.
[^construction]: **construction** — 빈 해에서 요청을 넣어 첫 해를 만드는 기법. 뒤의 ALNS가 그 해를 부수고 고친다. 여기선 24개, 난수 없음.
[^zone]: **존** — 요청 `anchor` side의 `zoneId`. 한 경로는 구체 구역 1종만 담을 수 있다(`ZONE_MIX`). 없으면 `"(none)"` 한 그룹.
[^dp]: **DP (동적 계획법)** — "앞에서 i까지 처리했을 때의 최선"을 표로 채워 답을 조립한다. 존 배정 DP는 유형별 대수 벡터가 상태. 이 파일의 subset-sum DP는 양자화 부피×방문 수가 상태.
[^seed]: **씨앗** — 빈 경로를 열 때 기준으로 삼는 주문. H24는 차고에서 이동표 시간이 가장 긴 주문. 풀의 중심이다.
[^pool]: **풀** — subsetSum이 고를 수 있는 후보 접두. 씨앗에서 가까운 순으로, 부피 합이 ⌈1.5×용량⌉이 되고 개수가 정차 한도 이상이 될 때까지.
[^subset-sum]: **subset-sum** — 용량 C에 들어가는 주문 조합 중 부피 합이 가장 큰 것을 칸마다 채워 찾는 것. 코드의 `subsetSum`.
[^oracle]: **오라클** — "이 후보가 유효한가·비용이 얼마인가"를 정확히 답하는 검사기. 여기서는 `InsertionSearch.candidatesFor`. subsetSum은 부피·무게·정차 수만 보고, 시간창은 오라클이 본다.
[^lexicographic-score]: **사전식 점수** — `long[]`을 앞부터 비교. 기본 축은 **(미배정 수, 사용 차량 수, 총 거리 m, 총 운행시간 s)**. 가중합으로 뭉개지 않는다. hard 위반은 점수 축이 아니다.
[^determinism]: **결정적** — 같은 입력에 항상 같은 출력. 재검증·벤치마크를 위해 초기해에 난수를 금지한다.
[^bank]: **bank** — 경로에 못 들어간 요청 ID 집합. 모든 요청은 경로 또는 bank 중 정확히 하나(XOR). bank가 남은 해도 유효 — 미배정 축이 나쁠 뿐.
[^problem]: **`Problem`** — 요청·차량·차고·이동표·호환표를 검증·정규화해 **동결**한 객체. 탐색은 읽기만.
[^request]: **`Request`** — 주문 하나. `PICKUP_DELIVERY`(방문 2)·`DELIVERY_ONLY`·`PICKUP_ONLY`. 배정·제거의 원자 단위라 pickup만 따로 옮기지 않는다.
[^pair]: **pair** — pickup과 delivery가 짝인 주문을 **통째로** 하나. 픽업만 빼는 연산은 없다.
[^profile]: **`Profile`** — 고객별 hard/soft와 점수 축. core에 `if (customerId == …)`를 두지 않고 이 객체를 갈아 끼운다.
[^vehicle-class]: **차급** — 차량 톤수 등급. 주문의 `allowedVehicleFeatures`와 맞춰 호환을 가른다. 호환표는 차급·capability·구역·차고. **무게·부피 용량은 호환에 없다.**
[^spi]: **SPI** — 구현체를 여러 개 꽂는 인터페이스. `ConstructionHeuristic` 24개가 등록된다.
[^hard-soft]: **hard / soft** — hard는 어기면 무효(용량·시간창·정차 한도·구역·차급), soft는 점수가 나빠질 뿐. 삽입은 hard 통과 후보만.
[^abstain]: **기권** — 기법이 이 `Problem`에서 성립하지 않아 실행하지 않음. 실패가 아니다. H24는 항상 `false`.
[^zone-mix]: **`ZONE_MIX`** — 한 경로가 구체 구역 2종 이상을 방문하면 hard 위반. `zoneId` 부재·`"ALL"`은 안 센다. 전파가 판정한다. H24는 존 배정이 차를 존에 묶고, leftover만 존을 넘어 갈 수 있다.
[^quantization]: **양자화** — 부피를 칸 단위 u로 나누어 정수 칸에 올리는 것. H24는 **올림**이라 칸 합이 C 이하면 실제 합도 용량 이하.
[^knapsack]: **0/1 knapsack** — 각 물건을 넣거나 안 넣거나 둘 중 하나. 칸을 뒤에서 채우면 같은 물건을 두 번 쓰지 않는다. H24는 무게를 최소화하며 부피·방문 칸을 채운다.
[^propagation]: **전파 (`RoutePropagator`)** — 방문 순서가 주어지면 시각·적재·정차 수를 계산하고 hard를 검사해 Feasible/Infeasible로 답한다. 후보마다 경로 전체를 처음부터 다시.
[^fixture]: **fixture** — 테스트·실측용 고정 입력. 실물 fixture는 `data/win_poc_case_floor.json` (주문 452·차량 31).
[^leftover]: **leftover** — 존 내부 적재가 남긴 요청. leftover pass가 존을 넘어 기존 경로·미사용 차에 한 번 더 넣는다. 그래도 안 되면 bank.
