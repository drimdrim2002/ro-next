# `VehicleFillRemainingRegretConstruction` (H14) 코드 해설

> **성격**: 코드 해설 노트(비규범). 규범은 [heuristics 문서 §5 H14](../implementation/stage-04-initial-solution-heuristics.md)이고,
> 쉬운 말 설명은 [초기해 24개 노트 §6.3](initial-solution-heuristics-h1-h24.md)(비용 정의는 §6.0)이다.
> 셋과 이 노트가 어긋나면 **규범이 이긴다.** 이 노트는 §6.3을 코드 수준으로 연장할 뿐, 대체하지 않는다.
>
> **대상 독자**: CVRPTW[^cvrptw]를 아는 Java 개발자. 이 저장소의 용어(`Request`·`Problem`·bank 등)는 처음 본다고 가정하고,
> 처음 나올 때마다 각주로 풀었다. 각주는 문서 끝 [용어 각주](#용어-각주)에 모여 있다.
>
> **대상 파일**: `solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleFillRemainingRegretConstruction.java` (129줄, 2026-09-02 기준).

---

## 0. 한 줄 요약

**"차를 큰 것부터 한 대씩 잡아 채우되, 지금 이 차에 넣는 Δ거리보다 뒤차가 혼자 나르는 거리가 짧으면 그 요청은 미룬다. 뒤차가 없는 요청은 지금 넣는다. 더 못 넣으면 그 차를 닫고 다시 열지 않는다."**

| 이름 조각 | 뜻 |
|---|---|
| `vehicle-fill` | **차 한 대**를 잡고 그 차에만 넣고, 다 채우면 닫는다 — vehicle-outer[^vehicle-outer] |
| `remaining-regret` | 선택 키가 "지금 이 차의 Δ거리 − **아직 안 쓴 뒤차**의 단독 거리". 자리 간 차이가 아니다 |
| `Construction` | 초기해 구축 기법[^construction] — 빈 해에서 시작해 요청을 하나씩 넣는다 |

출처는 VROOM의 기본 초기해다(survey: `cost − λ·regret`, H14는 λ=1 고정. λ 격자·INIT 변종은 채택하지 않음).

---

## 1. 프로젝트 안에서의 위치

### 1.1 초기해 포트폴리오의 한 칸

`InitialSolutionBuilder.build(problem, profile)`가 결정적 construction **24개**(H1~H24)를 전부 돌려 정식 평가에 넣고, 사전식 점수[^lexicographic-score]가 가장 좋은 하나를 고른다. H14는 14번째이고, 실물 fixture[^fixture]에서는 미배정 56으로 **5위**다(§8).

```text
InitialSolutionBuilder.build(problem, profile)
  ├─ H1 … H13
  ├─ H14 VehicleFillRemainingRegretConstruction   ← 이 문서
  ├─ H15 … H24
  → 각 Solution을 Evaluator로 평가 → 사전식 최선 1개
```

같은 계열(vehicle-outer)은 H3·H14·H15. H3는 차에 **구역을 통째로**, H14는 **요청 하나씩**. 구역이 없으면 H3는 축이 꺾이고 H14는 그대로 돈다 — 24개 노트 §4.2 축 ④가 H14를 이종 fleet[^heterogeneous-fleet]·구역 없음의 대표로 남긴 이유다. 코드는 아직 24개 전부 실행한다.

### 1.2 `ConstructionHeuristic` (SPI[^spi])

```java
public interface ConstructionHeuristic {
    String id();                                   // "vehicle-fill-remaining-regret"
    boolean abstains(Problem problem);             // H14는 항상 false — 기권하지 않는다
    Solution construct(Problem problem, Profile profile);
}
```

1. **난수 없음, 결정적**[^determinism]. 정렬은 문자열 ID로 끝나고 `HashMap` 순회로 승자를 고르지 않는다.
2. **반환 해는 항상 유효.** 못 넣은 요청은 bank[^bank]. 삽입은 전부 `InsertionSearch`(§2)라 hard 제약[^hard-soft]을 통과한 것만 경로에 있다.
3. **기권 없음.** `abstains`는 항상 `false`. 차고 수·`PICKUP_DELIVERY`와 무관하게 실행한다.

### 1.3 다루는 자료형

| 타입 | 이 파일에서의 뜻 |
|---|---|
| `Problem`[^problem] | 동결된 입력 전체. 탐색은 읽기만 |
| `Request`[^request] | 배차의 원자 단위 (pair 통째) |
| `Vehicle` | `maxWeight`·`effectiveMaxStopCount`(정차 한도[^stop-limit], optional)·근무창·차고 |
| `Solution` | `List<Route>` + bank. 불변 — 삽입마다 새 객체 |
| `Route` | `VehicleId` + 방문 목록. 빈 경로는 생성자가 막음 |
| `Profile`[^profile] | 고객별 hard/soft. 이 파일은 `InsertionSearch`에 넘기기만 한다 |
| `Candidate` | "이 차의 이 위치에 넣으면 Δ비용이 이만큼" |
| `Pick` (private) | 채움 한 스텝의 후보 — `noAlt`·`netCost`·`deltaDist`·그 차의 최선 `Candidate` |
---

## 2. 기대는 부품 — `InsertionSearch` (이 기법이 쓰는 메서드만)

"어느 차를 어떤 순서로, 그 차에 어느 요청을"만 이 파일이 정하고, **"넣을 수 있는가·어느 위치가 싼가"는 전부
`InsertionSearch`에** 맡긴다. 아래가 호출 전부다. `candidates()`(모든 경로)는 **부르지 않는다.**

| 메서드 | 하는 일 | H14 |
|---|---|---|
| `emptySolution` | 전 요청이 bank인 빈 해 | 시작점 |
| `candidatesFor(..., r, v)` | **차량 v 하나**의 삽입 위치, 비용 오름차순. 비호환·불가 = 빈 목록. 경로 없으면 새 경로 후보 | 채움의 ①, `openRoute` 내부 |
| `openRoute(..., r, v)` | `candidatesFor` 최선 + `apply`. 못 열면 empty | seed[^seed] |
| `standaloneDistMeter(v, r)` | r을 v의 단독 경로로 둘 때의 이동표 거리 합. **오라클[^oracle]이 아님** — 표 조회 | ② |
| `apply` | 후보를 적용한 새 `Solution`. 그 요청은 bank에서 빠짐 | seed·채움 |
| `checkOuterLoop` | 바깥 루프 상한 초과 시 `IllegalStateException` | 차량 시작(c)·삽입(a) |
| `sortedRequestIds` · `BY_REQUEST_ID` · `BY_VEHICLE_ID` | ID 문자열 순 | pending·동률 |
| `workSpanSec` | 근무창 길이 합(초) | 차량 순서 3키 |

### 2.1 `candidatesFor` (한 대) vs `candidates` (전부)

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

호환이 아니면 즉시 빈 목록. 호환은 차급[^vehicle-class]·capability·구역·차고 앵커이지 **무게·부피 용량이 아니다**
(`Compatibility.compatible`). 용량은 오라클이 본다. `visitsOf`가 빈 목록이면 새 경로 후보 — seed의 `openRoute`가 이 경로다.

`candidates()`는 기존 경로 전부 + 미사용 호환 차량 1대(ID 가장 앞)를 본다. H1·H2가 쓴다. H14는 지금 채우는 v만 보므로
`candidatesFor(..., v)`. 닫힌 앞차·아직 안 연 뒤차는 이 호출에 안 나온다.

`candidatesFor`가 통과하려면 ① 호환, ② `RoutePropagator`[^propagation] Feasible, ③ profile hard 전부 satisfied.
H14는 용량·시간창을 스스로 검사하지 않는다. `cands.getFirst()`는 `(새 경로 여부, Δ거리, Δ운행시간)` 사전식 최선 —
채움은 이미 열린 경로라 실질 1키는 Δ거리(`deltaDriveDistMeter`).

### 2.2 `standaloneDistMeter` — ②가 실제로 호출하는 것

§6.3 문면의 ②는 "뒤차가 이 주문을 혼자 실어 나를 때의 최선 비용". 코드가 그 값을 만드는 경로는 `candidatesFor`가 아니다.

```99:112:solver-core/src/main/java/com/ronext/rpdptw/solve/InsertionSearch.java
    public static long standaloneDistMeter(Problem problem, VehicleId vehicleId, RequestId requestId) {
        Vehicle vehicle = problem.vehicle(vehicleId);
        Request request = problem.request(requestId);
        List<LocationId> stops = new ArrayList<>(4);
        vehicle.startDepot().ifPresent(stops::add);
        request.pickup().ifPresent(side -> stops.add(side.locationId()));
        request.delivery().ifPresent(side -> stops.add(side.locationId()));
        vehicle.endDepot().ifPresent(stops::add);
        long total = 0L;
        for (int i = 0; i + 1 < stops.size(); i++) {
            total = Math.addExact(total, problem.travel().distanceMeter(stops.get(i), stops.get(i + 1)));
        }
        return total;
    }
```

규범 §3.3: **"오라클이 아니라 표 조회다 (feasibility를 보장하지 않는다)."** 호출은
`remainingStandalone` → 호환인 뒤차 v′마다 이 합 → min. 호환 0대면 empty(`noAlt`).

하지 않는 일: 뒤차에 `candidatesFor`(빈 경로 오라클)를 돌리지 않는다. 뒤차가 시간창·용량으로 그 요청을 실제로 받을 수 있는지를 ②에서 보지 않는다. 차고가 같으면 ②는 요청마다 왕복(또는 편도) 하나다. `endDepot`이 없으면 복귀 구간이 합에 없다 — T31의 ②가 `d(차고, 배송지)` 한 칸인 이유(§8).

---

## 3. 알고리즘 전체 흐름

```text
abstains?  항상 false
current = 빈 해, pending = RequestId 문자열 순 LinkedHashSet
vehicles = vehicleOrder(problem)                 // §4.2 큰 차부터
bound = n + m
for v in vehicles, pending이 빌 때까지:           // 규칙 (c) 카운트 1
  seed = 호환·미처리 중 (무게 DESC, 부피 DESC, ID ASC)
         첫 openRoute 성공. 전부 실패 → v 미사용, 다음 차
  remaining = v 이후 차량
  loop:
    pending의 각 r:
      ① = candidatesFor(..., v) 최선.Δ거리        // 이 차에 못 넣으면 skip
      ② = remainingStandalone(..., remaining, r)  // 표 조회 min, 없으면 noAlt
      Pick = (noAlt, ①−②, ①, r, candidate)
    PICK_ORDER 최소를 v 최선 위치에 apply          // 규칙 (a)
    후보 0개 → v 닫기, 다음 차                     // 규칙 (c)
차량 소진 후 pending 잔여 = bank
```

세는 것은 차량 시작 1회 + 채움 삽입 1회 → **≤ n + m**. seed 삽입은 차량 시작(c)에 포함된다.

---

## 4. 단계별 상세

### 4.1 `construct` 골격

```33:41:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleFillRemainingRegretConstruction.java
    public Solution construct(Problem problem, Profile profile) {
        Solution current = InsertionSearch.emptySolution(problem);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        List<Vehicle> vehicles = vehicleOrder(problem);
        int bound = pending.size() + vehicles.size();
        int iterations = 0;
        for (int index = 0; index < vehicles.size() && !pending.isEmpty(); index++) {
            VehicleId v = vehicles.get(index).id();
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);           // 규칙 (c)
```

`pending`은 bank와 같은 집합의 작업용 복사다. `apply`가 `Solution.bank`에서 빼 주지만, 채움 루프가 O(1)로 지워야 해서
`LinkedHashSet`을 둔다. 순회는 ID 순, 승자는 `PICK_ORDER`(마지막 키가 이미 `RequestId` ASC).

### 4.2 차량 순서 — `vehicleOrder`

§6.3은 "정차 한도·적재량 큰 차부터". 코드의 키는 네 개다.

```111:120:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleFillRemainingRegretConstruction.java
    /** 차량 순서 = (effectiveMaxStopCount DESC — 부재 = 최우선, maxWeight DESC, Σ근무창 길이 DESC, VehicleId ASC). H16 공유. */
    static List<Vehicle> vehicleOrder(Problem problem) {
        List<Vehicle> vehicles = new ArrayList<>(problem.vehicles());
        vehicles.sort(Comparator.<Vehicle>comparingLong(v -> v.effectiveMaxStopCount().isPresent()
                        ? v.effectiveMaxStopCount().getAsInt() : Long.MAX_VALUE).reversed()
                .thenComparing(Comparator.comparingLong(Vehicle::maxWeight).reversed())
                .thenComparing(Comparator.comparingLong(InsertionSearch::workSpanSec).reversed())
                .thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
        return vehicles;
    }
```

| 순서 | 키 | 방향 | 코드 |
|---:|---|---|---|
| 1 | `effectiveMaxStopCount` | DESC | **부재 = `Long.MAX_VALUE`** → 한도 없는 차가 한도 있는 차보다 **앞** |
| 2 | `maxWeight` | DESC | **`maxVolume`은 차량 순서에 없다** |
| 3 | `workSpanSec` = Σ(close − open) | DESC | 근무창이 긴 차 먼저 |
| 4 | `VehicleId` | ASC | `BY_VEHICLE_ID` 문자열 |

H16이 이 메서드를 그대로 호출한다. H3의 차량 순서는 (담당 Request 수 ASC, `maxWeight` DESC, ID ASC)라 "큰 차"의 정의가 다르다.

| 차량 | 정차 | maxWeight | Σ근무창(s) | 정렬 키 |
|---|---:|---:|---:|---|
| V1 | 4 | 10,000 | 36,000 | (4, 10000, 36000, V1) |
| V2 | 3 | 5,000 | 36,000 | (3, 5000, 36000, V2) |
| V3 | 2 | 2,000 | 28,800 | (2, 2000, 28800, V3) |
| V0 | **부재** | 8,000 | 36,000 | (**2⁶³−1**, 8000, 36000, V0) → V1보다 앞 |

아래 워크스루는 V0 없이 V1→V2→V3만 쓴다.

### 4.3 seed — 경로를 여는 첫 요청은 regret이 고르지 않는다

```74:94:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleFillRemainingRegretConstruction.java
    /** seed = v와 호환·미처리 중 (totalWeight DESC, totalVolume DESC, RequestId ASC) 순 첫 새 경로 성공. */
    private static Optional<Solution> seed(
            Problem problem, Profile profile, Solution current, Set<RequestId> pending, VehicleId v) {
        List<RequestId> order = new ArrayList<>();
        for (RequestId requestId : pending) {
            if (problem.compatibleVehicles(requestId).contains(v)) {
                order.add(requestId);
            }
        }
        order.sort(Comparator.<RequestId>comparingLong(id -> problem.request(id).totalWeight()).reversed()
                .thenComparing(Comparator.<RequestId>comparingLong(id -> problem.request(id).totalVolume()).reversed())
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        for (RequestId requestId : order) {
            Optional<Solution> opened = InsertionSearch.openRoute(problem, profile, current, requestId, v);
            if (opened.isPresent()) {
                pending.remove(requestId);
                return opened;
            }
        }
        return Optional.empty();
    }
```

`openRoute` = `candidatesFor` + `apply`. v는 아직 경로가 없어 새 경로 후보. 호환·미처리 전부가 실패하면 빈 `Route` 없이
다음 차(`seeded.isEmpty() → continue`). seed 키는 채움의 ①−②와 무관하다. §6.3 표는 **seed 성공 뒤** 채움 스텝에만 해당한다.

### 4.4 ② — `remainingStandalone` (이 노트가 §6.3에 더하는 핵심)

```96:109:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleFillRemainingRegretConstruction.java
    /** regret(r) = min over 차량 순서상 v 이후·r과 호환인 v′ 의 standaloneDistMeter. 없으면 empty (noAlt). */
    static Optional<Long> remainingStandalone(Problem problem, List<Vehicle> remaining, RequestId requestId) {
        Long min = null;
        for (Vehicle vehicle : remaining) {
            if (!problem.compatibleVehicles(requestId).contains(vehicle.id())) {
                continue;
            }
            long standalone = InsertionSearch.standaloneDistMeter(problem, vehicle.id(), requestId);
            if (min == null || standalone < min) {
                min = standalone;
            }
        }
        return Optional.ofNullable(min);
    }
```

- `remaining` = `vehicles.subList(index + 1, size)`. **지나간 차·지금 차는 없다** (T31).
- 호환 v′가 0대면 `Optional.empty()` → `noAlt = true`.
- min은 `long` 미터. Δ운행시간·새 경로 여부는 ②에 없다.

규범 의사코드(heuristics §5 H14 ~768행):

```text
regret(r) = min_{v′: 차량 순서상 v 이후 · r과 호환} standaloneDistMeter(problem, v′, r)
그런 v′가 없으면 noAlt(r) = true   ← 지금 못 넣으면 끝 — 최우선
선택 키 = ( noAlt DESC, Δ거리(v) − regret(r) ASC, Δ거리 ASC, RequestId ASC )
```

### 4.5 채움 한 스텝 — ①, `Pick`, `PICK_ORDER`

```47:69:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleFillRemainingRegretConstruction.java
            remaining = vehicles.subList(index + 1, vehicles.size());
            while (true) {
                Pick best = null;
                for (RequestId requestId : pending) {
                    List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, v);
                    if (cands.isEmpty()) {
                        continue;
                    }
                    Candidate first = cands.getFirst();
                    Optional<Long> regret = remainingStandalone(problem, remaining, requestId);
                    Pick pick = new Pick(requestId, regret.isEmpty(),
                            first.deltaDriveDistMeter() - regret.orElse(0L), first.deltaDriveDistMeter(), first);
                    if (best == null || PICK_ORDER.compare(pick, best) < 0) {
                        best = pick;
                    }
                }
                if (best == null) {
                    break;                                                      // v 확정, 다음 차량
                }
                InsertionSearch.checkOuterLoop(ID, ++iterations, bound);       // 규칙 (a)
                current = InsertionSearch.apply(problem, current, best.candidate());
                pending.remove(best.requestId());
            }
```

| 기호 | 필드 | 계산 |
|---|---|---|
| ① | `deltaDist` | `candidatesFor(..., v).getFirst().deltaDriveDistMeter()` — **열린 경로**에 끼울 때 늘어나는 거리(m) |
| ② | `regret` | `remainingStandalone`의 min. 없으면 empty |
| `noAlt` | `boolean` | `regret.isEmpty()` |
| `netCost` | `long` | `① − regret.orElse(0L)` — `noAlt`면 `① − 0 = ①` |

```122:128:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleFillRemainingRegretConstruction.java
    /** 선택 키 = (noAlt DESC, Δ거리(v) − regret ASC, Δ거리 ASC, RequestId ASC) — big-M 없이 축 분리 (X19). */
    private static final Comparator<Pick> PICK_ORDER = Comparator.comparing(Pick::noAlt, Comparator.reverseOrder())
            .thenComparingLong(Pick::netCost)
            .thenComparingLong(Pick::deltaDist)
            .thenComparing(Pick::requestId, InsertionSearch.BY_REQUEST_ID);

    private record Pick(RequestId requestId, boolean noAlt, long netCost, long deltaDist, Candidate candidate) {}
```

`Boolean` 자연 순서는 `false < true`라 `reverseOrder`가 **`true`(noAlt)를 앞**으로 보낸다. ②에 큰 상수(big-M)를 넣지
않는다 — H1 `onlyCandidate`와 같은 축 분리(X19). `< 0`일 때만 교체.

H2의 regret과 한 줄로:

| | H2 `urgency-regret3` | H14 |
|---|---|---|
| 비교 대상 | **같은 요청**의 삽입 **자리** 1·2·3순위 Δ거리 차 | **같은 요청**의 (지금 차 ①) vs (**남은 차** 단독 ②) |
| 탐색 | `Cache.candidates()` — 열린 경로 전부 + 미사용 1대 | `candidatesFor(..., v)` — **지금 차만** |
| 식 | `Σ_k (c_k − c_1)` (k=2,3) | `① − min standalone(v′)` |
| 최우선 | 긴급 등급 → 후보 수 | `noAlt` |

ALNS repair의 regret 삽입은 H2 쪽이다.

### 4.6 차량을 닫는 조건

`best == null` ⇔ pending의 **어떤 요청도** `candidatesFor(..., v)`가 비어 있다(비호환이거나 오라클 거부: 용량·시간창·정차
한도·profile hard). 그때 `break`하고 다음 `index`.

- 닫힌 v는 이후 `candidatesFor` 대상이 아니다. 앞 차로 돌아가는 코드가 없다.
- leftover pass 없음. H23의 ④와 달리 뒤 차가 끝난 뒤 닫힌 차에 한 번 더 넣지 않는다. 남은 pending = bank.
- seed 실패 차도 다시 열리지 않는다.

규범 §4.3 (c): "차량(경로) 1개를 확정해 닫는다 → 남은 차량 수 −1". 카운터는 루프 **진입 때** 이미 1 올렸으므로
닫는 `break`에서는 다시 세지 않는다.

### 4.7 숫자 워크스루 — 요청 6 · 차량 3, 채움 한 스텝의 ①·②·`noAlt`

거리는 **설명용 미터**. 식·비교자·닫기 조건은 코드 그대로. 무게는 ×1000한 `long`[^units].

**차량** — 전부 start=end=같은 차고 D. V1·V2 근무 08:00–18:00 (36,000 s), V3 08:00–16:00 (28,800 s).

| 차량 | 정차 | maxWeight | Σ근무창 | 순서 |
|---|---:|---:|---:|---:|
| V1 | 4 | 10,000 | 36,000 | 1 |
| V2 | 3 | 5,000 | 36,000 | 2 |
| V3 | 2 | 2,000 | 28,800 | 3 |

**요청** — 전부 `DELIVERY_ONLY`. B만 차급 V1 전용. 차고가 같아 뒤차마다 standalone는 같다.

| r | totalWeight | 호환 | standalone = d(D,r)+d(r,D) |
|---|---:|---|---:|
| S | 6,000 | 전원 | 14,000 |
| A | 1,200 | 전원 | 20,000 |
| B | 1,200 | **V1만** | 8,000 (뒤차가 안 쓰므로 ②에 안 등장) |
| C | 1,200 | 전원 | 4,000 |
| D | 1,200 | 전원 | 12,000 |
| E | 1,200 | 전원 | 6,500 |

**V1 seed.** 무게 순 S(6,000)이 `openRoute` 성공. `pending = {A,B,C,D,E}`, `remaining = [V2, V3]`. 경로 `D → S → D`.

**V1 채움 1스텝** — ①은 오라클 `getFirst().deltaDriveDistMeter()`. ②는 코드가 도는 그대로:

```text
A: V2 호환 → standalone(V2,A)=20,000; V3 호환 → 20,000; min=20,000
B: V2 비호환 skip; V3 비호환 skip; min=없음 → noAlt
C: min(4,000, 4,000) = 4,000
D: 12,000
E: 6,500
```

| r | ① Δ거리(V1) | ② min standalone(뒤차) | noAlt | netCost = ①−② | PICK_ORDER |
|---|---:|---:|---|---:|---|
| **B** | 2,000 | *(empty)* | **true** | 2,000 (= ① − 0) | **1** — `noAlt`가 나머지보다 앞 (X19) |
| A | 1,200 | 20,000 | false | **−18,800** | 2 — 지금 싣는 게 이득 |
| D | 3,500 | 12,000 | false | −8,500 | 3 |
| E | 6,000 | 6,500 | false | −500 | 4 |
| C | 8,000 | 4,000 | false | **+4,000** | 5 — 뒤차 단독이 더 쌈 → 미룸 |

승자 = B. `apply`가 V1의 ① 자리에 B를 넣는다. `pending = {A,C,D,E}`.

§6.3 표의 A/C/D가 이 표의 A/C/B다. 코드가 실제로 두는 `noAlt`·`netCost`·`orElse(0L)`를 붙인 것이 이 절이다.

**다음 스텝.** 경로가 `S+B`로 바뀌었으니 ①을 다시 계산한다. `remaining`은 닫히기 전까지 [V2, V3] 그대로. 무게
6,000+1,200+1,200=8,400. D를 더하면 9,600. E(10,800)는 용량 10,000을 넘어 `candidatesFor` 빈 목록.
**어느 스텝이든 pending 전원의 `candidatesFor(..., V1)`가 비면 `best == null` → V1 닫기.**

**V2.** remaining = [V3]. ②는 V3 한 대의 standalone. C처럼 ②가 작았던 요청이 이제 ①의 대상. 정차 3·무게 5,000에서
더 못 넣으면 닫기.

**V3.** `remaining = []` → 넣을 수 있는 요청은 전부 `noAlt`. 키가 `(true, ①, ①, ID)`로 줄어 **가장 싼 삽입부터**.
더 못 넣으면 닫고 잔여 = bank.

---

## 5. 결정성 · 종료 · 복잡도

### 5.1 결정성

| 지점 | 고정 방법 |
|---|---|
| 차량 | `vehicleOrder` 네 키, 끝 `VehicleId` 문자열 |
| seed | 무게 DESC, 부피 DESC, `BY_REQUEST_ID` |
| pending 순회 | `LinkedHashSet(sortedRequestIds)` |
| 채움 승자 | `PICK_ORDER`. `< 0`일 때만 교체 |
| 오라클 동률 | `InsertionSearch.sorted` (비용, `VehicleId`, 위치). 한 차만 보므로 ID는 동률 |
| ② 동률 뒤차 | min이 `<`이라 먼저 나온 값. `long`이라 승자 요청에는 영향 없음 |

`HashMap` 순회로 승자를 고르는 곳이 없다. 부동소수도 없다.

### 5.2 종료 (규범 §4.3)

```text
(a) Request 1개를 삽입 → 미처리 −1
(c) 차량 1개를 확정해 닫는다 → 남은 차량 −1   [H14·H16·H20]
⇒ 바깥 루프 최대 |requests| + |vehicles|
```

H14는 (b)("후보 0개로 제외")를 쓰지 않는다. 이 차에 못 넣는 요청은 pending에 남겨 뒤차에 넘긴다. 뒤차에서도 못 넣으면
bank. `bound = n + m`. 넘으면 `IllegalStateException` — 품질이 아니라 버그.

### 5.3 복잡도 (규범 §5 표 H14 행)

```text
H14 | vehicle-fill-remaining-regret | vehicle-outer | 전 패턴 | ≤ n + m | O(n · L²) + 표 조회
```

바깥이 차량. 반복당 pending ≤ n × `candidatesFor` 한 대(위치 L × 전파 L; PD는 위치 쌍이라 `L`이 한 번 더) + 뒤차 ≤ m대 표 조회(구간 최대 3칸, 전파 없음). H1·H2는 반복당 `O(n · m · L²)`(전 차 오라클). H14는 오라클이 **한 대**라 싸다(실물 519 ms vs H2 644 · H1 830). H3(137 ms)보다 느린 것은 차마다 pending 전체를 다시 오라클하기 때문이다.

---

## 6. 코드를 읽으며 눈여겨볼 점

규범과 코드는 1:1이다. §6.3의 쉬운 말과 코드가 어긋나 보이는 곳만 적는다. **수정 제안이 아니라 읽기 보조.**

1. **②는 오라클 Δ거리가 아니다.** §6.3의 "혼자 실어 나를 때의 최선 비용"은 `standaloneDistMeter` 합이다. 뒤차가
   시간창·용량 때문에 그 요청을 못 받아도 ②는 숫자가 나오고 `noAlt`는 false다. 용량은 호환 필터에 없다. 미룬 요청이
   뒤차 seed/채움에서 오라클 거부되면 bank다.
2. **첫 요청은 regret이 고르지 않는다.** 경로를 여는 것은 seed(무게·부피). ①−② 표는 seed 이후만.
3. **정차 한도 부재 = 최우선.** `Long.MAX_VALUE`. 2키는 `maxWeight`이지 `maxVolume`이 아니다. 3키 Σ근무창은 §6.3 한 줄에 없다.
4. **`noAlt`일 때 `netCost = ① − 0`** (`orElse(0L)`). 첫째 축이 이미 갈라 놓아 이 0이 다른 요청을 앞지르지 못한다.
5. **닫힌 차는 재오픈·leftover pass가 없다.** `candidates()`를 한 번도 안 부른다. 앞차의 빈 칸을 뒤가 못 메운다.
6. **차고 부재는 ②의 항을 줄인다.** end 없으면 복귀 없음. 뒤차 start/end가 다르면 ②가 달라진다.
7. **`Problem`은 안 바뀐다.** `Solution`도 불변. `pending.remove`만 가변.

삼각부등식이 성립하는 표에서는 우회 ①이 왕복 ②보다 작아 `netCost`가 음수가 되기 쉽다. 코드가 그걸 가정하지는 않는다 — 이동표는 비삼각일 수 있다(N9).

---

## 7. 경계 상황 (규범 §7)

| 상황 | 동작 | 번호 |
|---|---|---|
| 남은 호환 차량이 없는 요청 | `remainingStandalone` empty → `noAlt=true` → `PICK_ORDER` 최우선. big-M 없음 | **X19** |
| 마지막 차량 | `remaining` 빈 리스트. 넣을 수 있는 요청은 전부 `noAlt`. 사실상 ① ASC | X19의 끝 |
| seed 전부 실패 | 그 차량 미사용. 빈 `Route` 없음 | §5 H14 |
| 이 차에 후보 0개인 요청 | pending에 남겨 뒤차에 넘김. (b)로 세지 않음 | §4.3 (c) vs (b) |
| 전 차량 소진 후 pending 잔여 | bank. 유효한 해 | Domain §9.1 |
| `PICKUP_DELIVERY` / 다중 차고 | 기권하지 않음. ②는 pickup→delivery 구간을 포함 | §5 표 |
| 정차 한도 부재 | 차량 순서 1키 = `Long.MAX_VALUE` | §4.2 |
| profile hard가 강해 삽입 0건 | 전부 bank. Feasible이므로 정상 | X14 |

X19를 코드로: ②를 무한대로 두지 않고 **비교 키 첫 칸을 boolean으로 둔다.** §4.7에서 ①=2,000인 B가 `netCost=−18,800`인 A보다 앞선다.

---

## 8. 테스트와 실측

| 테스트 | 고정하는 것 |
|---|---|
| T31 `VehicleFillRemainingRegretConstructionTest.regretUsesOnlyRemainingVehicles` | 지나간 차는 ②에서 제외 · `noAlt` 최우선 (X19) · bank 0 |
| T26 `InsertionSearchTest.standaloneDistUsesDirectedMatrix` | `d(i,j) ≠ d(j,i)`인 표에서 ②가 방향별 값을 합산 · 좌표 미사용 |
| T19 `ConstructionHeuristicsTest.outerLoopBoundedByRequestCount` | 바깥 루프 ≤ n+m |

T31 입력:

| | 값 |
|---|---|
| S | `DELIVERY_ONLY` 6,000, 허용 차급 {X, Y} — V1 seed |
| A | 4,000, {X, Y} — V2로 미룰 수 있다 |
| B | 4,000, {X} — V1뿐 → `noAlt` |
| V1 | 무게 10,000, feature X, 정차 한도 **부재**, start=D, **end 없음** |
| V2 | 10,000, Y, 한도 부재, start=D, end 없음 |
| 근무창 | 둘 다 28,800–64,800 → `workSpanSec` = 36,000 |

차량 순서: 한도 부재라 둘 다 `Long.MAX_VALUE`, 무게·근무 동률 → `VehicleId` ASC = **V1, V2**.

V1 채움 중 `remaining = [V2]`:

```text
remainingStandalone(B) = empty          // V2와 비호환
remainingStandalone(A) = Optional.of(standaloneDistMeter(V2, A))   // end 없음 → d(D, A) 한 칸
remainingStandalone(A, remaining=[]) = empty
```

seed = S(6,000). V1 용량 10,000이라 S 다음에 4,000짜리 **하나**. `noAlt`인 B가 A보다 먼저, A는 V2 seed.
bank 공집합, V1 방문에 B, V2 = `[delivery(A)]`.

실물 실측(452건·31대, 2026-09-04, 24개 노트 §1 — 한 번 실행 기록, T번호로 고정돼 있지 않음):

| 순위 | 기법 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 |
|---:|---|---:|---:|---:|---:|---:|
| 1 | H23 `zone-quota-balanced-fill` | 0 | 31 | 4,198,408 | 1,002,069 | 550 ms |
| 3 | H3 `vehicle-zone-fill` | **15** | 31 | 4,515,433 | 1,003,093 | 137 ms |
| **5** | **H14 `vehicle-fill-remaining-regret`** | **56** | **31** | **4,216,318** | **917,423** | **519 ms** |
| 6 | H2 `urgency-regret3` | 60 | 31 | 3,924,184 | 941,844 | 644 ms |

H14 = **미배정 56 · 5위 · 519 ms**. H3은 15로 더 적지만 구역을 차에 통째로 싣는다. 같은 vehicle-outer라도 단위가 존 vs 요청.

```bash
mvn test -pl solver-core -Dtest=VehicleFillRemainingRegretConstructionTest
```

---

## 용어 각주

[^cvrptw]: **CVRPTW** — 용량·시간창이 있는 차량 경로 문제. 이 저장소의 **RPDPTW**는 거기에 pickup-and-delivery와 실무 제약(차급·구역·정차 한도·근무창)을 더한 "rich" 변형.
[^construction]: **construction** — 빈 해에서 요청을 하나씩 넣어 첫 해를 만드는 기법. 뒤의 ALNS가 그 해를 부수고 고친다. 여기선 24개, 난수 없음.
[^vehicle-outer]: **vehicle-outer** — 바깥 루프가 차량인 계열(H3·H14·H15). 한 대를 채우고 닫은 뒤 다음 차. 반대는 parallel-regret(H1·H2).
[^heterogeneous-fleet]: **이종 fleet** — 차가 제각각(톤수·정차 한도·근무창·차급). H14의 차량 순서와 "뒤차가 더 잘 실을 요청은 미룬다"가 이 축을 겨눈다.
[^regret]: **regret** — 지금 안 넣고 미뤘을 때 나중에 얼마나 더 비싸지는가. H2는 같은 요청의 **자리** 1·2·3순위 Δ거리 차, H14는 지금 차 ① vs **남은 차량** 단독 ②.
[^seed]: **seed** — 빈 경로를 여는 첫 요청. H14는 호환·미처리 중 가장 무거운 요청이 오라클을 통과한 첫 번째. 채움의 ①−②와 다른 키.
[^lexicographic-score]: **사전식 점수** — `long[]`을 앞부터 비교. 기본 축은 **(미배정 수, 사용 차량 수, 총 거리 m, 총 운행시간 s)**. 가중합으로 뭉개지 않는다. hard 위반은 점수 축이 아니다.
[^determinism]: **결정적** — 같은 입력에 항상 같은 출력. 재검증·벤치마크를 위해 초기해에 난수를 금지한다.
[^bank]: **bank** — 경로에 못 들어간 요청 ID 집합. 모든 요청은 경로 또는 bank 중 정확히 하나(XOR). bank가 남은 해도 유효 — 미배정 축이 나쁠 뿐. 이 파일의 `pending`은 그 작업용 집합.
[^problem]: **`Problem`** — 요청·차량·차고·이동표·호환표를 검증·정규화해 **동결**한 객체. 탐색은 읽기만.
[^request]: **`Request`** — 주문 하나. `PICKUP_DELIVERY`(방문 2)·`DELIVERY_ONLY`·`PICKUP_ONLY`. 배정·제거의 원자 단위라 pickup만 따로 옮기지 않는다.
[^profile]: **`Profile`** — 고객별 hard/soft와 점수 축. core에 `if (customerId == …)`를 두지 않고 이 객체를 갈아 끼운다.
[^oracle]: **오라클** — "이 후보가 유효한가·비용이 얼마인가"를 정확히 답하는 검사기. 여기서는 `InsertionSearch.candidatesFor`. H14의 ①은 오라클, ②는 아님.
[^vehicle-class]: **차급** — 차량 톤수 등급. 주문의 `allowedVehicleFeatures`와 맞춰 호환을 가른다. 호환표는 차급·capability·구역·차고. **무게·부피 용량은 호환에 없다.**
[^spi]: **SPI** — 구현체를 여러 개 꽂는 인터페이스. `ConstructionHeuristic` 24개가 `InitialSolutionBuilder`에 등록된다.
[^hard-soft]: **hard / soft** — hard는 어기면 무효(용량·시간창·정차 한도·구역·차급), soft는 점수가 나빠질 뿐. construction 삽입은 hard 통과 후보만.
[^stop-limit]: **정차 한도 (`effectiveMaxStopCount`)** — 한 경로의 최대 정차 수. 없으면 optional 비어 있음. H14 차량 순서에서 부재는 `Long.MAX_VALUE`로 **가장 앞**.
[^units]: **단위** — 무게·부피는 ×1000 FLOOR한 `long`, 거리는 meter, 시간은 초. 워크스루의 6,000은 "6 단위".
[^fixture]: **fixture** — 테스트·실측용 고정 입력. 실물 fixture는 `data/win_poc_case_floor.json` (주문 452·차량 31).
[^propagation]: **전파 (`RoutePropagator`)** — 방문 순서가 주어지면 시각·적재·정차 수를 계산하고 hard를 검사해 Feasible/Infeasible로 답한다. 후보마다 경로 전체를 처음부터 다시. H14는 이 결과를 `candidatesFor`로만 받는다.
