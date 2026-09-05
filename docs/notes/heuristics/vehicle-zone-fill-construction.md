# `VehicleZoneFillConstruction` (H3) 코드 해설

> **성격**: 코드 해설 노트(비규범). 규범은 [heuristics 문서 §5 H3](../../implementation/stage-04-initial-solution-heuristics.md)이고,
> 쉬운 말 설명은 [초기해 24개 노트 §2 H3](../initial-solution-heuristics-h1-h24.md)다 (**H3에는 §6이 없다**).
> 둘과 이 노트가 어긋나면 **규범이 이긴다.** 이 노트는 §2 H3을 코드 수준으로 연장할 뿐, 대체하지 않는다.
>
> **대상 독자**: CVRPTW[^cvrptw]를 아는 Java 개발자. 이 저장소의 용어(`Request`·`Problem`·bank 등)는 처음 본다고 가정하고,
> 처음 나올 때마다 각주로 풀었다. 각주는 문서 끝 [용어 각주](#용어-각주)에 모여 있다.
>
> **대상 파일**: `solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java` (155줄, 2026-09-02 기준).

---

## 0. 한 줄 요약

**"차를 까다로운 것부터 한 대씩 잡고, 남은 요청을 존[^zone]으로 묶은 뒤, 존마다 이 차에만 what-if[^what-if]로 실어 본다. '전원 성공이면서 적재율[^load-ratio] > 0.85'인 존이 있으면 그중 적재율 최대, 없으면 적재율 최대 존 하나만 커밋한다. 그 존에서 못 실린 멤버는 pending에 남아 다음 차가 가져간다."**

| 이름 조각 | 뜻 |
|---|---|
| `vehicle` | 바깥 루프가 **차량**. 한 대를 잡고 존 하나를 커밋한 뒤 닫는다 — vehicle-outer[^vehicle-outer] |
| `zone-fill` | 넣는 단위는 요청 하나가 아니라 **존 하나**(what-if로 고른 그룹). 존 ID를 본다 |
| `Construction` | 초기해 구축 기법[^construction] — 빈 해에서 시작해 요청을 넣는다 |

같은 vehicle-outer인 H14는 존 ID를 보지 않고 요청을 하나씩 채운다. 존을 전역으로 나누는 쪽은 H23이다.

---

## 1. 프로젝트 안에서의 위치

### 1.1 초기해 포트폴리오의 한 칸

`InitialSolutionBuilder.build(problem, profile)`가 결정적 construction **24개**(H1~H24)를 전부 돌려 정식 평가에 넣고, 사전식 점수[^lexicographic-score]가 가장 좋은 하나를 고른다. H3는 3번째이고, 실물 fixture[^fixture]에서는 미배정 15로 **3위**다(§8). 같은 vehicle-outer는 H3·H14·H15. **H3는 차에 존을**(what-if 승자의 삽입 집합), **H14는 요청 하나씩**이며 H14는 `zoneId`를 읽지 않는다. 포트폴리오 축으로는 H3가 ① 전역 배분의 국소판(h1–h24 §4.2)이고, ④ 차량 단위 채우기의 대표는 구역이 없을 때의 H14다. H23은 같은 존 축의 전역판(실물 1위, 미배정 0).

규범 §5 H3(~543행)은 **"차량 수를 직접 공략하는 유일한 기법"**이라고 적는다. 이유는 route elimination이 범위 밖(D4)이기 때문이다. 그 문장은 **기본 8개(H1–H8)를 쓰던 시점**의 것이다. 뒤에 들어온 H14도 vehicle-outer다. 규범 문면을 고치는 노트가 아니라, 문서의 **연대**로만 읽는다.

### 1.2 `ConstructionHeuristic` (SPI[^spi])

```java
public interface ConstructionHeuristic {
    String id();                                   // "vehicle-zone-fill"
    boolean abstains(Problem problem);             // H3는 항상 false — 기권하지 않는다
    Solution construct(Problem problem, Profile profile);
}
```

1. **난수 없음, 결정적**[^determinism]. 정렬은 문자열 ID로 끝나고 `HashMap` 순회로 승자를 고르지 않는다.
2. **반환 해는 항상 유효.** 못 넣은 요청은 bank[^bank]. 삽입은 전부 `InsertionSearch`(§2)라 hard 제약[^hard-soft]을 통과한 것만 경로에 있다.
3. **기권[^abstain] 없음.** `abstains`는 항상 `false`. `zoneId`가 하나도 없어도 기권하지 않는다 — `"(none)"` 그룹으로 퇴화(규범 §4.4, 2026-09-02 개정).

### 1.3 다루는 자료형

| 타입 | 이 파일에서의 뜻 |
|---|---|
| `Problem`[^problem] | 동결된 입력 전체. 탐색은 읽기만 |
| `Request`[^request] | 배차의 원자 단위 (pair 통째) |
| `Vehicle` | `maxWeight`·`maxVolume`. 차량 순서의 2키는 **무게**이지 부피가 아니다 |
| `Solution` | `List<Route>` + bank. 불변 — 삽입마다 새 객체. what-if를 버려도 롤백 코드가 없는 이유 |
| `Route` | `VehicleId` + 방문 목록. 빈 경로는 생성자가 막음 (X9) |
| `Profile`[^profile] | 고객별 hard/soft. 이 파일은 `InsertionSearch`에 넘기기만 한다 |
| `Candidate` | "이 차의 이 위치에 넣으면 Δ비용이 이만큼" |
| `WhatIf` (private) | 존 하나·차량 하나의 가짜 삽입 결과 — `zoneId`·`solution`·`inserted`·`allInserted`·`loadRatio` |

---

## 2. 기대는 부품 — `InsertionSearch` (이 기법이 쓰는 메서드만)

"어느 차를 어떤 순서로, 그 차에 어느 존을"만 이 파일이 정하고, **"넣을 수 있는가·어느 위치가 싼가"는 전부 `InsertionSearch`에** 맡긴다. 아래가 호출 전부다.

| 메서드 | 하는 일 | H3 |
|---|---|---|
| `emptySolution` | 전 요청이 bank인 빈 해 | 시작점 |
| `candidatesFor(..., r, v)` | **차량 v 하나**의 삽입 위치, 비용 오름차순. 비호환·불가 = 빈 목록. 경로 없으면 새 경로 후보 | `tryZone`의 삽입 |
| `apply` | 후보를 적용한 새 `Solution`. 그 요청은 bank에서 빠짐 | `tryZone` 내부. 커밋은 승자 `WhatIf.solution` 대입 |
| `checkOuterLoop` | 바깥 루프 상한 초과 시 `IllegalStateException` | 차량 1대마다 1회, bound = `m` |
| `sortedRequestIds` | 전 요청 ID를 문자열 오름차순 | `pending` 초기값 |
| `anchor` | 첫 방문 side (pickup 있으면 pickup, 없으면 delivery) | `zoneGroups`의 `zoneId` |
| `BY_REQUEST_ID` · `BY_VEHICLE_ID` | ID 문자열 비교 | 동률 |

**부르지 않는 것.** `candidates()`(모든 경로 + 미사용 1대), `InsertionSearch.Cache`, `standaloneDistMeter`, `openRoute`, `remove`. leftover pass가 없고, 닫힌 앞차에 되돌아가지 않는다.

### 2.1 `candidatesFor` (한 대)

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

호환이 아니면 즉시 빈 목록. 호환은 차급[^vehicle-class]·capability·구역·차고 앵커이지 **무게·부피 용량이 아니다**. 용량·시간창·정차 한도·`ZONE_MIX`[^zone-mix]는 오라클[^oracle](`RoutePropagator`[^propagation] + profile hard)이 본다. `visitsOf`가 빈 목록이면 새 경로 후보 — 그 차를 처음 열 때다.

`cands.getFirst()`는 `(새 경로 여부, Δ거리, Δ운행시간)` 사전식 최선. H3는 그 위치만 쓰고, 2·3순위를 보지 않는다 (regret 없음).

`anchor`(330행) = 첫 방문 side (pickup이 있으면 pickup, 없으면 delivery). `zoneGroups`는 `anchor(request).zoneId().orElse("(none)")`.

`apply`는 원본을 고치지 않고 새 `Solution`을 돌려준다. `tryZone`은 `base`에서 쌓고 커밋 전에 `current`를 안 바꾼다. 진 존의 `WhatIf`는 버린다 — 규범 §5 H3: **"롤백이 필요 없다."**

---

## 3. 알고리즘 전체 흐름

```text
abstains?  항상 false
current = 빈 해
pending = RequestId 문자열 순 LinkedHashSet
vehicles = vehicleOrder(problem)          // coverage 전 요청 1회, 이후 고정
bound = m
for v in vehicles, pending이 빌 때까지:    // 규칙 (c) 카운트 1, ≤ m
  zones = zoneGroups(pending)             // zoneId, 부재 = "(none)", TreeMap
  zoneOrder = (commonVehicles 크기 ASC, 존 크기 DESC, zoneId ASC)
  for z in zoneOrder:                     // what-if — current를 안 바꿈
    tryZone: 멤버를 (weight DESC, RequestId ASC)로 v에만 candidatesFor+apply
             loadRatio = max(Σw/maxW, Σv/maxV)   // capacity==0 → 0.0
             WhatIf = (allInserted, loadRatio, solution, inserted)
  COMMIT_ORDER 최소 WhatIf가 inserted 비어 있지 않으면
    current = 그 solution                 // 승자만 채택, 나머지 폐기
    pending -= inserted                   // 못 실린 멤버는 pending 잔류
  inserted 0건 → v 미사용, 빈 Route 없음   // X9
차량 소진 후 pending 잔여 = bank
```

바깥 카운트는 **차량 시작 1회뿐**. `tryZone` 안의 삽입은 세지 않는다.

---

## 4. 단계별 상세

### 4.1 `construct` 골격

```38:65:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java
    public Solution construct(Problem problem, Profile profile) {
        Solution current = InsertionSearch.emptySolution(problem);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        List<Vehicle> vehicles = vehicleOrder(problem);
        int iterations = 0;
        for (Vehicle vehicle : vehicles) {
            if (pending.isEmpty()) {
                break;
            }
            InsertionSearch.checkOuterLoop(ID, ++iterations, vehicles.size());
            Map<String, List<RequestId>> zones = zoneGroups(problem, pending);
            List<String> zoneOrder = zoneOrder(problem, zones);
            WhatIf chosen = null;
            for (String zoneId : zoneOrder) {
                WhatIf whatIf = tryZone(problem, profile, current, vehicle, zoneId, zones.get(zoneId));
                if (chosen == null || COMMIT_ORDER.compare(whatIf, chosen) < 0) {
                    chosen = whatIf;
                }
            }
            if (chosen != null && !chosen.inserted().isEmpty()) {
                current = chosen.solution();                                    // 그 존의 what-if만 채택
                pending.removeAll(chosen.inserted());
            }
            // 아무것도 못 받은 차량은 사용하지 않는다 — 빈 Route를 만들지 않는다 (X9)
        }
        return current;
    }
```

`pending`은 bank의 작업용 복사. 승자 비교는 `< 0`일 때만 교체(동률은 `zoneOrder` 앞·`COMMIT_ORDER`의 zoneId ASC). leftover pass 없음 — 루프가 끝나면 남은 pending = bank.

### 4.2 차량 순서 — `vehicleOrder` (coverage는 전 요청, 1회)

```67:83:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java
    /** 차량 순서 = (담당 가능한 Request 수 ASC, maxWeight DESC, VehicleId ASC) — 까다로운 차부터. */
    static List<Vehicle> vehicleOrder(Problem problem) {
        Map<VehicleId, Integer> coverage = new LinkedHashMap<>();
        for (Vehicle vehicle : problem.vehicles()) {
            coverage.put(vehicle.id(), 0);
        }
        for (Request request : problem.requests()) {
            for (VehicleId vehicleId : problem.compatibleVehicles(request.id())) {
                coverage.merge(vehicleId, 1, Integer::sum);
            }
        }
        List<Vehicle> vehicles = new ArrayList<>(problem.vehicles());
        vehicles.sort(Comparator.<Vehicle>comparingInt(v -> coverage.get(v.id()))
                .thenComparing(Comparator.comparingLong(Vehicle::maxWeight).reversed())
                .thenComparing(Vehicle::id, InsertionSearch.BY_VEHICLE_ID));
        return vehicles;
    }
```

| 순서 | 키 | 방향 | 코드 |
|---:|---|---|---|
| 1 | coverage = 호환 Request **수** | ASC | **전 요청**, `construct` 들어가기 전 **한 번**. pending이 줄어도 다시 세지 않는다 |
| 2 | `maxWeight` | DESC | **`maxVolume`·정차 한도·근무창은 차량 순서에 없다** |
| 3 | `VehicleId` | ASC | `BY_VEHICLE_ID` 문자열 |

coverage는 **"지금 남은 요청"이 아니다.** 이미 배정된 요청도 센다 — 정적 값, 한 번.

H14의 차량 순서와 한 줄로:

| | H3 | H14 |
|---|---|---|
| 1키 | coverage ASC (호환 요청 수, 전 요청 1회) | `effectiveMaxStopCount` DESC (**부재 = `Long.MAX_VALUE`**) |
| 2키 | `maxWeight` DESC | `maxWeight` DESC |
| 3키 | `VehicleId` ASC | `workSpanSec` DESC, 그다음 `VehicleId` ASC |
| 존 | 본다 | 안 본다 |

### 4.3 존 그룹 — `zoneGroups`

```85:93:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java
    /** zoneId(anchor side 기준, 부재 = "(none)")로 그룹핑. 그룹 안은 RequestId 순. */
    static Map<String, List<RequestId>> zoneGroups(Problem problem, Set<RequestId> pending) {
        Map<String, List<RequestId>> zones = new TreeMap<>();
        for (RequestId requestId : pending) {
            String zoneId = InsertionSearch.anchor(problem.request(requestId)).zoneId().orElse(NO_ZONE);
            zones.computeIfAbsent(zoneId, z -> new ArrayList<>()).add(requestId);
        }
        return zones;
    }
```

- `NO_ZONE = "(none)"`. `zoneId` 부재는 기권이 아니다 (2026-09-02 개정, 규범 §4.4).
- `TreeMap`이라 키 순회는 zoneId 문자열 오름차순. 실제 시도 순서는 다음 절 `zoneOrder`가 다시 정한다.
- 멤버 추가 순서는 `pending` 순 = `sortedRequestIds` (RequestId ASC). **`tryZone`이 무게 내림차순으로 다시 정렬**하므로, 이 단계의 멤버 순서는 삽입 순서가 아니다.

매 차량마다 pending 기준으로 **다시** 묶는다. 앞 차가 존 B를 가져가면 다음 차의 `zones`에는 B가 없다. 앞 차가 존 A를 **일부만** 가져가면 A는 남은 멤버로 다시 나타난다 — 존이 차에 쪼개지는 지점(§4.8).

### 4.4 존 순서 — `zoneOrder` · `commonVehicles`

```95:114:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java
    private static List<String> zoneOrder(Problem problem, Map<String, List<RequestId>> zones) {
        List<String> order = new ArrayList<>(zones.keySet());
        order.sort(Comparator.<String>comparingInt(z -> commonVehicles(problem, zones.get(z)).size())
                .thenComparing(Comparator.<String>comparingInt(z -> zones.get(z).size()).reversed())
                .thenComparing(Comparator.naturalOrder()));
        return order;
    }

    static Set<VehicleId> commonVehicles(Problem problem, List<RequestId> requestIds) {
        Set<VehicleId> common = null;
        for (RequestId requestId : requestIds) {
            if (common == null) {
                common = new LinkedHashSet<>(problem.compatibleVehicles(requestId));
            } else {
                common.retainAll(problem.compatibleVehicles(requestId));
            }
        }
        return common == null ? Set.of() : common;
    }
```

| 순서 | 키 | 방향 | 뜻 |
|---:|---|---|---|
| 1 | `commonVehicles` 크기 | ASC | 멤버 **전원**이 동시에 탈 수 있는 차의 교집합. 작을수록 제약이 센 존 |
| 2 | 존 Request 수 | DESC | 큰 존 먼저 |
| 3 | zoneId | ASC | 문자열 |

`commonVehicles`는 이 차 하나가 아니라 멤버 호환 집합의 **교집합**. T23처럼 차가 1대면 크기가 전부 1이라 2키(존 크기)가 가른다. 이 순서는 what-if **시도** 순서이고, 승자는 `COMMIT_ORDER`다.

### 4.5 `tryZone` — what-if (이 차에만, `current`는 그대로)

```116:139:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java
    /** what-if — 커밋하지 않는다. (요구 weight DESC, RequestId ASC)로 v의 빈 경로에 최소 비용 삽입. */
    private static WhatIf tryZone(
            Problem problem, Profile profile, Solution base, Vehicle vehicle, String zoneId, List<RequestId> members) {
        List<RequestId> order = new ArrayList<>(members);
        order.sort(Comparator.<RequestId>comparingLong(id -> problem.request(id).totalWeight()).reversed()
                .thenComparing(InsertionSearch.BY_REQUEST_ID));
        Solution solution = base;
        List<RequestId> inserted = new ArrayList<>();
        long weight = 0L;
        long volume = 0L;
        for (RequestId requestId : order) {
            List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, solution, requestId, vehicle.id());
            if (cands.isEmpty()) {
                continue;
            }
            solution = InsertionSearch.apply(problem, solution, cands.getFirst());
            inserted.add(requestId);
            Request request = problem.request(requestId);
            weight = Math.addExact(weight, request.totalWeight());
            volume = Math.addExact(volume, request.totalVolume());
        }
        double loadRatio = Math.max(ratio(weight, vehicle.maxWeight()), ratio(volume, vehicle.maxVolume()));
        return new WhatIf(zoneId, solution, inserted, inserted.size() == members.size(), loadRatio);
    }
```

`base`는 지금 `current`(앞 차 경로는 있고 이 차는 아직 없음). 삽입은 `candidatesFor(..., vehicle.id())`뿐 — **이 차만**. 멤버 순서 = `totalWeight` DESC, `RequestId` ASC. 용량 실패는 `continue`(이 what-if의 `inserted`에만 안 넣음, pending은 커밋 전까지 그대로). `allInserted = inserted.size() == members.size()`. 주석의 "v의 빈 경로"는 **시작이 빈 경로**라는 뜻이고, 첫 삽입 이후에는 커지는 경로에 끼운다.

진 존의 `solution`은 `COMMIT_ORDER`가 고르지 않으면 `current`에 안 붙는다. 쉬운 말 노트(§2 H3)의 "가짜로 실어 보고 버린 뒤 진짜로 확정"은 코드에서 **재삽입이 없다** — 승자 `WhatIf.solution` 대입이 커밋이다.

### 4.6 적재율 — `loadRatio` · `ratio`

```141:143:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java
    private static double ratio(long used, long capacity) {
        return capacity == 0L ? 0.0 : (double) used / (double) capacity;
    }
```

```text
loadRatio = max( Σweight / maxWeight , Σvolume / maxVolume )
capacity == 0  →  그 축은 0.0
```

넣은 요청의 무게·부피 합만 본다. 거리·시간창·정차 수는 없다. `double`은 **정렬 키로만**, 동률은 `zoneId` 문자열(규범 §4.2).

### 4.7 `COMMIT_ORDER` — `LOAD_TARGET = 0.85`, 비교는 `>`

```25:26:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java
    /** 재량 상수 — 이 적재율을 넘기는 "전원 성공" 존이 있으면 그중에서 고른다 (Stage 8 조정). */
    static final double LOAD_TARGET = 0.85;
```

```145:152:solver-core/src/main/java/com/ronext/rpdptw/solve/VehicleZoneFillConstruction.java
    /**
     * 커밋 규칙: "전원 성공 ∧ 적재율 > 0.85"인 존이 있으면 그중 적재율 최대, 없으면 적재율 최대 존.
     * 부동소수는 정렬 키로만 — 동률은 zoneId ASC (§4.2).
     */
    private static final Comparator<WhatIf> COMMIT_ORDER = Comparator
            .comparing((WhatIf w) -> w.allInserted() && w.loadRatio() > LOAD_TARGET, Comparator.reverseOrder())
            .thenComparing(WhatIf::loadRatio, Comparator.reverseOrder())
            .thenComparing(WhatIf::zoneId);
```

| 순서 | 키 | 방향 |
|---:|---|---|
| 1 | `allInserted && loadRatio > 0.85` | DESC (`reverseOrder` → `true`가 앞) |
| 2 | `loadRatio` | DESC |
| 3 | `zoneId` | ASC |

비교는 **`>` 이지 `>=`가 아니다.** `loadRatio == 0.85`이고 전원이 들어갔어도 첫 키는 `false`. 0.85를 넘기지 못한 전원 성공 존은 부분 삽입 존과 첫 키가 같고 둘째 키로만 겨룬다. `Boolean` 자연 순서는 `false < true`라 `reverseOrder`가 `true`를 앞으로 보낸다 — big-M 없음.

### 4.8 커밋 · 잔여 · 존이 쪼개지는 방법

승자 `inserted`가 비어 있지 않으면 `current = chosen.solution()`, `pending.removeAll(chosen.inserted())` — **들어간 것만** 뺀다. 못 실린 멤버는 pending에 남아 다음 차가 같은 존 ID로 다시 묶는다. leftover pass 없음. 존이 차에 조각나는 경로이고, 실물 15건의 구조다(survey §2.5). `inserted`가 비면 `current`를 안 바꾸고 빈 `Route`도 없다 (X9).

### 4.9 숫자 워크스루 — T23 (차량 1 · 존 2)

`VehicleZoneFillConstructionTest.commitsOnlyBestZonePerVehicle`. 테스트 주석 그대로다.

**전제:** 차 1대 V1, `maxWeight = maxVolume = 30,000`. 요청 5건 전부 `DELIVERY_ONLY`, 호환은 V1뿐. fixture의 `Item`은 무게와 부피가 같은 값[^units]. 시간창은 전원 통과한다고 본다(테스트가 오라클을 통과한 결과를 고정한다).

| r | zone | weight = volume |
|---|---|---:|
| A1 | A | 10,000 |
| A2 | A | 10,000 |
| B1 | B | 9,000 |
| B2 | B | 9,000 |
| B3 | B | 9,000 |

coverage(V1) = 5. 차량 순서 = [V1].

`zoneGroups`: A = [A1, A2] (pending ID 순), B = [B1, B2, B3]. `"(none)"` 없음.

`commonVehicles`: A도 B도 {V1}, 크기 1. 존 크기 DESC → **B(3) 다음 A(2)**.

**what-if A** — 순서 A1, A2 (무게 동률, ID ASC). 둘 다 들어감. Σ=20,000. `allInserted=true`, `loadRatio=20,000/30,000=0.666…`(테스트 주석 0.67). 첫 키 `true && 0.67 > 0.85` → **false**.

**what-if B** — 순서 B1, B2, B3. 셋 다 들어감. Σ=27,000. `allInserted=true`, `loadRatio=0.90`. 첫 키 `true && 0.90 > 0.85` → **true**.

`COMMIT_ORDER`: B의 첫 키가 true, A는 false → **B 승**. A의 what-if `solution`(A1·A2가 실린 해)은 버린다. `current`는 B1·B2·B3만 방문하는 경로 1개. pending에서 B 셋만 제거.

차가 더 없으므로 A1·A2 = bank. 테스트: `routes.size()==1`, visits=`{delivery(B1), delivery(B2), delivery(B3)}`, bank=`{A1, A2}`. A를 커밋했다면 적재율 0.67로 V1을 덜 채우고 B 3건이 bank다. 첫 키가 막는다.

### 4.10 숫자 워크스루 — 차량 2 · 존 2, 존이 쪼개진다

T23은 차가 1대라 진 존이 곧 bank다. 차가 한 대 더 있으면 **같은 규칙이 존을 차에 나눠 실어** 미배정이 남는다. 아래 숫자는 설명용이다. 식·비교자·커밋은 코드 그대로.

**전제:** 전원 `DELIVERY_ONLY`·호환 전원·시간창 비제약. 무게 = 부피. V1 `maxWeight=maxVolume=30,000`, V2 = 20,000.

| r | zone | weight |
|---|---|---:|
| A1 | A | 10,000 |
| A2 | A | 10,000 |
| A3 | A | 10,000 |
| A4 | A | 8,000 |
| B1 | B | 9,000 |
| B2 | B | 9,000 |
| B3 | B | 9,000 |

coverage: V1=7, V2=7. 1키 동률 → `maxWeight` DESC → **V1, V2**.

존 합: A = 38,000 (차 한 대 30,000을 넘김), B = 27,000 (V1에 0.90으로 딱).

**V1.** `commonVehicles` 둘 다 {V1, V2} 크기 2. 존 크기 DESC → A 다음 B.

| what-if | 삽입 순서 | 들어간 것 | allInserted | loadRatio | 첫 키 (`all ∧ >0.85`) |
|---|---|---|---|---:|---|
| A | A1, A2, A3, A4 | A1+A2+A3 = 30,000 (A4는 용량) | **false** | **1.00** | false |
| B | B1, B2, B3 | 27,000 전원 | **true** | **0.90** | **true** |

B가 이긴다. A는 적재율 1.00으로 더 "가득"이지만 전원이 아니라서 첫 키에서 진다. `current` = V1이 B 셋. pending = {A1, A2, A3, A4}.

**V2.** 남은 존은 A뿐.

| what-if | 삽입 순서 | 들어간 것 | allInserted | loadRatio |
|---|---|---|---|---:|
| A | A1, A2, A3, A4 | A1+A2 = 20,000 (A3 10,000 불가, A4 8,000도 20+8>20) | false | 1.00 |

inserted 비어 있지 않으니 커밋. pending = {A3, A4} → 차량 소진 → **bank 2건**.

```text
V1  Route  = B1, B2, B3     (존 B, 27,000/30,000 = 0.90)
V2  Route  = A1, A2         (존 A의 조각, 20,000/20,000 = 1.00)
bank       = A3, A4         (존 A의 나머지 18,000)
```

존 A가 V2와 bank에 쪼개졌다. V1이 B를 "전원 성공 ∧ >0.85"로 가져가 A 38,000을 뒤차 20,000에 남겼고, V2는 가득인데 A3·A4를 못 받는다.

**가설:** 전역 배정(H23)이면 V1→A(A1+A2+A3=30,000), V2→B(18,000/20,000=0.90), bank=A4 1건. 같은 산수의 대조이지 실측이 아니다. 실물에서 이 형태가 반복된 결과가 미배정 15다(survey §2.5: 존이 조각나고 뒤 T1 4대가 부피 1.9~3.0/4.9로 반만 참). H23은 존에 차를 전역 DP로 나눠 미배정 0.

---

## 5. 결정성 · 종료 · 복잡도

### 5.1 결정성

| 지점 | 고정 방법 |
|---|---|
| 차량 | coverage ASC, `maxWeight` DESC, `VehicleId` 문자열 |
| pending | `LinkedHashSet(sortedRequestIds)` |
| 존 그룹 | `TreeMap` + pending 순 추가. 삽입 순서는 `tryZone`이 다시 정함 |
| 존 시도 순 | `commonVehicles` 크기, 존 크기 DESC, zoneId ASC |
| 멤버 삽입 순 | `totalWeight` DESC, `BY_REQUEST_ID` |
| 오라클 동률 | `InsertionSearch.sorted` (비용, `VehicleId`, 위치). 한 차만 보므로 ID는 동률 |
| 커밋 승자 | `COMMIT_ORDER`. `< 0`일 때만 교체. 적재율 동률은 zoneId ASC |

`HashMap` 순회로 승자를 고르지 않는다. `double` 적재율은 정렬 키.

### 5.2 종료 (규범 §4.3 · §5 H3)

바깥 루프는 차량이다. 매 반복이 차량 1대를 소비한다 — 규범 §4.3의 **(c) 차량 1개를 확정해 닫는다**. 삽입 0건(X9)이어도 그 차는 다시 안 나온다.

```text
checkOuterLoop(ID, ++iterations, vehicles.size())   // bound = m
```

`tryZone`의 삽입은 안 센다. pending이 비면 `break`. 넘으면 `IllegalStateException` — 품질이 아니라 버그. 규범 §5 표 H3 행은 **≤ m**. §4.3 괄호는 (c)를 H14·H16·H20만 적었지만 H3의 바깥도 차량이라 같은 (c)다 (H1–H8 시점 표기가 표·H3 절에 남아 있다).

### 5.3 복잡도 (규범 §5 표 H3 행)

```text
H3 | vehicle-zone-fill | vehicle-outer | 전 패턴 | ≤ m | O(존 수 · n · L²)
```

차 1대마다 존마다 pending ≤ n × `candidatesFor` 한 대(위치 L × 전파 L. PD는 위치 쌍이라 `L`이 한 번 더). 실물 137 ms — H14(519 ms, 차마다 pending 전원 재계산)보다 빠르고, 존 배정 DP가 없어 H23(550 ms)보다도 빠르다.

---

## 6. 코드를 읽으며 눈여겨볼 점

규범과 코드는 1:1이다. 쉬운 말(§2 H3)과 코드가 어긋나 보이는 곳, 인접 기법과의 차이만 적는다. **수정 제안이 아니라 읽기 보조.**

1. **커밋은 재삽입이 아니다.** 승자 `WhatIf.solution`을 `current`에 넣는다. "가짜 삽입을 버리고 진짜로 다시 넣는다"가 아니다.
2. **"통째로"는 목표이지 항상 결과가 아니다.** 첫 키가 `allInserted && >0.85`를 우대할 뿐, 그런 존이 없으면 부분 삽입 존도 커밋한다. 못 실린 멤버는 다음 차 — 존 조각남.
3. **coverage는 pending이 아니다.** 전 요청 호환 수, 한 번. 앞 차가 가져간 요청도 카운트에 남는다.
4. **`>` 이지 `>=`가 아니다.** 적재율 정확히 0.85인 전원 성공 존은 첫 키 부스트를 받지 못한다.
5. **닫힌 차는 재오픈·leftover pass가 없다.** `candidates()`를 한 번도 안 부른다. T23의 V1은 B를 실은 뒤 용량 3,000이 남아도 A를 안 붙인다 — 한 차 한 존, 그리고 `ZONE_MIX`.
6. **H3 vs H14 vs H23.** 둘 다 vehicle-outer인 H3·H14 중 H3는 존 커밋, H14는 요청 단위 remaining-regret(존 ID 없음, 차량 순서는 정차 한도·근무창). H23은 존에 차를 전역으로 나눈 뒤 존 안을 채운다(실물 15 vs 0). greedy[^greedy].
7. **규범의 "유일한 기법"은 H1–H8 시점의 문면**이다. H14도 차를 채워 대수를 공략한다. 연대 사실.
8. **`0.85`는 재량 상수**(Stage 8). `Problem`은 안 바뀌고 `Solution`도 불변. `pending.removeAll`만 가변.

---

## 7. 경계 상황 (규범 §7)

| 상황 | 동작 | 번호 |
|---|---|---|
| 이 차가 어떤 존에도 1건을 못 넣음 | 그 차량 미사용. 빈 `Route` 없음 | **X9** |
| `zoneId`가 하나도 없음 | `"(none)"` 그룹 하나. 기권 아님 (2026-09-02 개정) | §4.4 |
| 호환 차량 0대인 요청 | 매 차 `candidatesFor` 빈 목록 → pending 잔류 → bank | X2 |
| 커밋된 존의 일부만 삽입 | 잔여 멤버는 pending. 뒤 차가 같은 존으로 재그룹 | §4.8 |
| 전 차량 소진 후 pending 잔여 | bank. 유효한 해 | Domain §9.1 |
| `PICKUP_DELIVERY` / 다중 차고 / 용량 0 | 기권하지 않음. 용량 0이면 그 축 `ratio` = 0.0 | §5 표 · §4.6 |
| 적재율 정확히 0.85 · 전원 성공 | 첫 키 `false` (`>` 아님 `>=`) | §4.7 |
| profile hard가 강해 삽입 0건 | 전부 bank. Feasible이므로 정상 | X14 |

X9를 코드로: `chosen.inserted().isEmpty()`이면 `current`를 안 바꾼다. `Route` 생성자가 빈 방문을 거부한다 (Stage 3 E30).

---

## 8. 테스트와 실측

| 테스트 | 고정하는 것 |
|---|---|
| T23 `VehicleZoneFillConstructionTest.commitsOnlyBestZonePerVehicle` | §4.9 그대로 — 존 A 0.67(전원, ≯0.85) vs 존 B 0.90(전원, >0.85) → B만 커밋, A는 bank. A의 what-if가 `current`를 오염시키지 않음 |
| T19 `ConstructionHeuristicsTest.outerLoopBoundedByRequestCount` | 바깥 루프 상한. H3의 bound는 `m` |

실물 실측(452건·31대, 2026-09-04, 24개 노트 §1 — 한 번 실행 기록. 회귀로 고정된 것은 T44의 "H23이 H3보다 사전식 우위"):

| 순위 | 기법 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 |
|---:|---|---:|---:|---:|---:|---:|
| 1 | H23 `zone-quota-balanced-fill` | **0** | 31 | 4,198,408 (2026-09-05 값 함수 개정 후 4,194,052, T52 확정) | 1,002,069 (개정 후 1,004,144) | 550 ms |
| **3** | **H3 `vehicle-zone-fill`** | **15** | **31** | **4,515,433** | **1,003,093** | **137 ms** |
| 5 | H14 `vehicle-fill-remaining-regret` | 56 | 31 | 4,216,318 | 917,423 | 519 ms |

H3 = **미배정 15 · 3위 · 137 ms**. H23은 전역판이라 0건. H14는 존을 안 봐 56건.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env
mvn test -pl solver-core -Dtest=VehicleZoneFillConstructionTest
```

---

## 용어 각주

[^cvrptw]: **CVRPTW** — 용량·시간창이 있는 차량 경로 문제. 이 저장소의 **RPDPTW**는 거기에 pickup-and-delivery와 실무 제약(차급·구역·정차 한도·근무창)을 더한 "rich" 변형.
[^construction]: **construction** — 빈 해에서 요청을 넣어 첫 해를 만드는 기법. 뒤의 ALNS가 그 해를 부수고 고친다. 여기선 24개, 난수 없음.
[^vehicle-outer]: **vehicle-outer** — 바깥 루프가 차량인 계열(H3·H14·H15). 한 대를 채우고 닫은 뒤 다음 차. 반대는 parallel-regret(H1·H2).
[^zone]: **존** — 요청 `anchor` side의 `zoneId`. 한 경로는 구체 구역 1종만 담을 수 있다(`ZONE_MIX`). 없으면 `"(none)"` 한 그룹.
[^what-if]: **what-if** — 커밋하지 않은 가짜 삽입. `tryZone`이 `base`에서 이 차에만 멤버를 넣어 `WhatIf`를 만들고, 승자만 `current`에 남긴다. `Solution`이 불변이라 진 쪽을 버리는 것이 롤백이다.
[^load-ratio]: **적재율** — `max(Σweight/maxWeight, Σvolume/maxVolume)`. 용량 0이면 그 축 0.0. H3 커밋 1키의 숫자.
[^lexicographic-score]: **사전식 점수** — `long[]`을 앞부터 비교. 기본 축은 **(미배정 수, 사용 차량 수, 총 거리 m, 총 운행시간 s)**. 가중합으로 뭉개지 않는다. hard 위반은 점수 축이 아니다.
[^determinism]: **결정적** — 같은 입력에 항상 같은 출력. 재검증·벤치마크를 위해 초기해에 난수를 금지한다.
[^bank]: **bank** — 경로에 못 들어간 요청 ID 집합. 모든 요청은 경로 또는 bank 중 정확히 하나(XOR). bank가 남은 해도 유효 — 미배정 축이 나쁠 뿐. 이 파일의 `pending`은 그 작업용 집합.
[^problem]: **`Problem`** — 요청·차량·차고·이동표·호환표를 검증·정규화해 **동결**한 객체. 탐색은 읽기만.
[^request]: **`Request`** — 주문 하나. `PICKUP_DELIVERY`(방문 2)·`DELIVERY_ONLY`·`PICKUP_ONLY`. 배정·제거의 원자 단위라 pickup만 따로 옮기지 않는다. pair.
[^profile]: **`Profile`** — 고객별 hard/soft와 점수 축. core에 `if (customerId == …)`를 두지 않고 이 객체를 갈아 끼운다.
[^oracle]: **오라클** — "이 후보가 유효한가·비용이 얼마인가"를 정확히 답하는 검사기. 여기서는 `InsertionSearch.candidatesFor`. H3는 용량·시간창을 스스로 검사하지 않는다.
[^vehicle-class]: **차급** — 차량 톤수 등급. 주문의 `allowedVehicleFeatures`와 맞춰 호환을 가른다. 호환표는 차급·capability·구역·차고. **무게·부피 용량은 호환에 없다.**
[^spi]: **SPI** — 구현체를 여러 개 꽂는 인터페이스. `ConstructionHeuristic` 24개가 등록된다.
[^hard-soft]: **hard / soft** — hard는 어기면 무효(용량·시간창·정차 한도·구역·차급), soft는 점수가 나빠질 뿐. 삽입은 hard 통과 후보만.
[^abstain]: **기권** — 기법이 이 `Problem`에서 성립하지 않아 실행하지 않음. 실패가 아니다. H3는 항상 `false`.
[^zone-mix]: **`ZONE_MIX`** — 한 경로가 구체 구역 2종 이상을 방문하면 hard 위반. `zoneId` 부재·`"ALL"`은 안 센다. 전파가 판정하고, H3는 차당 존 하나 커밋이라 같은 차에 두 구체 존을 섞지 않는다.
[^greedy]: **greedy** — 매 단계에서 지금 가장 좋아 보이는 선택을 하고 되돌리지 않음. H3의 차→존 커밋이 이것이다. H23의 존 배정 DP는 전역.
[^units]: **단위** — 무게·부피는 ×1000 FLOOR한 `long`, 거리는 meter, 시간은 초. T23의 10,000은 "10 단위".
[^fixture]: **fixture** — 테스트·실측용 고정 입력. 실물 fixture는 `data/win_poc_case_floor.json` (주문 452·차량 31).
[^propagation]: **전파 (`RoutePropagator`)** — 방문 순서가 주어지면 시각·적재·정차 수를 계산하고 hard를 검사해 Feasible/Infeasible로 답한다. 후보마다 경로 전체를 처음부터 다시. H3는 이 결과를 `candidatesFor`로만 받는다.
