# `HilbertSplitConstruction` (H18) 코드 해설

> **성격**: 코드 해설 노트(비규범). 쉬운 말 설명은 [h1–h24 노트 §6.4·§6.5](../initial-solution-heuristics-h1-h24.md)이고,
> 규범은 [heuristics 문서 §5 H18](../../implementation/stage-04-initial-solution-heuristics.md)이다. 출처·채택 근거는
> [survey H18 행](../../implementation/stage-04-initial-solution-heuristics-survey.md)이다. 셋과 이 노트가 어긋나면 그쪽이 이긴다.
> 이 문서는 §6.4–§6.5를 **코드 수준으로 연장**한다 — 대체하지 않는다.
>
> **대상 독자**: CVRPTW[^cvrptw]를 아는 Java 개발자. 이 저장소의 용어(`Request`·`Problem`·bank 등)는 처음 본다고 가정하고,
> 처음 나올 때마다 각주로 풀었다. 각주는 문서 끝 [용어 각주](#용어-각주)에 모여 있다.
>
> **대상 파일** (둘 다 2026-09-02 기준, `git log` 동일 커밋):
> - `solver-core/src/main/java/com/ronext/rpdptw/solve/HilbertSplitConstruction.java` (92줄)
> - `solver-core/src/main/java/com/ronext/rpdptw/solve/GiantTourSplit.java` (113줄)

---

## 0. 한 줄 요약

**"요청을 좌표 격자의 Hilbert[^hilbert] 인덱스 순으로 한 줄(giant tour[^giant-tour] σ)로 세운 뒤, 그 줄을 어디서 끊어 차 한 대씩 줄지를 DP[^dp]로 고른다."**

이름을 뜯어 보면 그대로다.

| 이름 조각 | 뜻 |
|---|---|
| `hilbert` | 대표 좌표 → 경계 상자 정규화 → Hilbert 인덱스. **순열만** 정한다 |
| `split` | 그 순열을 `GiantTourSplit`이 경로 조각으로 자른다 (Beasley/Prins) |
| `Construction` | 초기해 구축 기법[^construction] — 빈 해에서 시작해 `Solution` 하나 |

같은 순열이라면 어떤 탐욕[^greedy] 분할(next-fit[^next-fit] 포함)보다 나쁠 수 없다 — T34가 그 부등식이다. 순열 자체가 나쁘면 절단을 아무리 잘해도 소용없다. 좌표는 줄 세우기에만 쓰고, 조각의 실현성·거리는 전파[^propagation] + profile[^profile] hard와 이동표가 판정한다 (사전식[^lexicographic-score] 키는 미배정·차량 수·Σ거리).

---

## 1. 프로젝트 안에서의 위치

### 1.1 초기해 포트폴리오의 한 칸

이 저장소의 초기해[^initial-solution]는 하나가 아니다. `InitialSolutionBuilder.build(problem, profile)`가 결정적 construction
**24개**(H1~H24)를 전부 돌려 각 결과를 정식 평가(`Evaluator`)에 넣고, 사전식 점수가 가장 좋은 하나를 고른다.
H18은 그 24개 중 18번째이고, 계열은 **route-first(split)**[^route-first] 2개(H18·H19) 중 하나다.

```text
InitialSolutionBuilder.build(problem, profile)
  ├─ H1 … H17
  ├─ H18 HilbertSplitConstruction     ← 이 문서 (순열 = Hilbert)
  ├─ H19 NearestNeighborSplitConstruction  (순열 = 이동표 nearest-neighbor, 같은 GiantTourSplit)
  └─ H20 … H24
  → 각 Solution을 Evaluator로 평가 → 사전식 최선 1개
```

H19는 **자르는 코드가 H18과 완전히 같고 줄 세우는 방법만 다르다.** H19는 `problem.depots().size() != 1`이면 기권[^abstain]하고, H18은 차고 수와 무관하게 `abstains`가 항상 `false`다. 실물 fixture[^fixture]에서는 24개 전부 실행되며 H18은 20위(미배정 165, §8).

### 1.2 구현하는 인터페이스 — `ConstructionHeuristic` (SPI[^spi])

```java
public interface ConstructionHeuristic {
    String id();                                   // "hilbert-split"
    boolean abstains(Problem problem);             // H18은 항상 false
    Solution construct(Problem problem, Profile profile);
}
```

세 가지 계약이 이 파일 전체를 지배한다.

1. **난수 없음, 결정적**[^determinism]. 같은 `Problem`이면 같은 `Solution`. 정렬은 Hilbert 인덱스 다음에 `RequestId` 문자열로 끝나고, DP 동률은 "먼저 계산된 값 유지"다.
2. **반환 해는 항상 유효**하다. 넣지 못한 요청은 bank[^bank]에 남기면 되고, 경로에 들어간 요청은 전파 + profile hard[^hard-soft]를 통과한 상태다. H18은 삽입 오라클[^oracle]의 위치 나열을 쓰지 않고, 조각 방문 목록 전체를 `InsertionSearch.validate`에 한 번에 넘긴다.
3. **기권은 없다.** `abstains`가 상수 `false`라 어떤 `Problem`에서도 실행한다 (규범 §4.4 — H1~H18·H21·H22는 전원 기권 없음).

`construct` 본체는 한 줄이다.

```32:34:solver-core/src/main/java/com/ronext/rpdptw/solve/HilbertSplitConstruction.java
    public Solution construct(Problem problem, Profile profile) {
        return GiantTourSplit.split(problem, profile, hilbertOrder(problem), InsertionSearch.vehiclesByCapacityDesc(problem));
    }
```

순열은 `hilbertOrder`, 차량 순서는 적재량 내림차순, 절단은 `GiantTourSplit`. 이 파일이 스스로 경로를 만들거나 삽입하지 않는다.

### 1.3 다루는 자료형 요약

| 타입 | 이 파일에서의 뜻 |
|---|---|
| `Problem`[^problem] | 동결된 입력 전체. 요청·차량·이동표·호환표. 탐색은 절대 고치지 않는다 |
| `Request`[^request] | 배차의 원자 단위(pair). pickup·delivery side 중 있는 것들. 순열의 한 칸 |
| `Vehicle` | `maxWeight`로 차량 순서를 정하고, `effectiveMaxStopCount`가 조각 길이 상한 B |
| `Solution` | `List<Route>` + bank. DP 역추적이 직접 조립한다 (`emptySolution`/`apply`를 안 씀) |
| `Route` | `VehicleId` + 방문 노드 목록. **빈 경로는 존재하지 않는다**(생성자가 막음) |
| `Profile` | 고객별 hard/soft 제약 묶음. `validate`에 그대로 넘긴다 |
| `RouteFacts` | 전파가 성공했을 때의 사실값. DP가 읽는 것은 `driveDistMeter`뿐 |

---

## 2. 기대는 부품 — `GiantTourSplit` + 쓰는 `InsertionSearch`만

H18은 route-first라, 남은 bank 항목을 하나씩 최선 위치에 끼워 넣는 루프가 **없다.** `candidates` / `candidatesFor` / `apply` / `remove` / `emptySolution` / `checkOuterLoop`는 호출하지 않는다. 실제로 쓰는 메서드는 아래가 전부다.

| 메서드 | 하는 일 | 쓰는 곳 |
|---|---|---|
| `InsertionSearch.anchor(request)` | 첫 방문 side (pickup 있으면 pickup, 없으면 delivery) | `hilbertOrder` 대표 좌표 |
| `InsertionSearch.lastSide(request)` | 마지막 방문 side (delivery 있으면 delivery, 없으면 pickup) | 〃 |
| `InsertionSearch.sortedRequestIds(problem)` | 전 요청 ID를 문자열 순 | 순열 정렬의 출발 목록 · σ에 없는 요청을 bank에 합류 |
| `InsertionSearch.BY_REQUEST_ID` | `RequestId.value` 문자열 비교 | Hilbert 인덱스 동률 |
| `InsertionSearch.vehiclesByCapacityDesc(problem)` | `(maxWeight DESC, VehicleId ASC)` | Split에 넘기는 차량 순서 V |
| `InsertionSearch.validate(problem, profile, vehicleId, visits)` | 호환 필터 + 전파 Feasible + profile hard. 통과하면 `RouteFacts`, 아니면 empty | 조각 후보 ③의 **유일한 실현성 판정** |

`validate`가 "이 방문 목록이 이 차로 되는 경로인가"를 답한다. 세 관문을 다 통과해야 facts가 나온다 — ① `compatibleVehicles`에 그 차가 있는가(차급·capability·구역·차고), ② `RoutePropagator.propagate`가 시간창·용량·정차 한도[^stop-limit]·`ZONE_MIX`[^zone-mix] 등을 경로 전체에 다시 계산해 Feasible, ③ profile hard 전부 satisfied. **H18은 용량이나 시간창을 스스로 검사하지 않는다.** 거리도 좌표로 다시 재지 않는다 — `facts.driveDistMeter()`는 이동표 합이다.

```193:211:solver-core/src/main/java/com/ronext/rpdptw/solve/InsertionSearch.java
    /** 호환 필터 + 전파 + profile hard — 통과하면 facts, 아니면 empty. 후보 검증의 단일 지점 (§4.1). */
    static Optional<RouteFacts> validate(Problem problem, Profile profile, VehicleId vehicleId, List<NodeId> visits) {
        for (NodeId nodeId : visits) {
            if (!problem.compatibleVehicles(requestOf(problem, nodeId)).contains(vehicleId)) {
                return Optional.empty();
            }
        }
        PropagationResult propagated = RoutePropagator.propagate(problem, vehicleId, visits);
        if (!(propagated instanceof PropagationResult.Feasible feasible)) {
            return Optional.empty();
        }
        RouteFacts facts = feasible.facts();
        for (HardConstraint constraint : profile.hardConstraints()) {
            if (!constraint.satisfied(problem, facts)) {
                return Optional.empty();
            }
        }
        return Optional.of(facts);
    }
```

`GiantTourSplit`은 H18·H19가 순열만 바꿔 공유하는 패키지 전용 클래스다. 입력은 `(Problem, Profile, σ, V)`이고 출력은 `Solution` 하나. 상태 수 `(n+1)×(m+1)`이 고정이라 바깥 루프 방어 카운터가 없다 (규범 §4.3).

---

## 3. 알고리즘 전체 흐름

```text
abstains?  항상 false
     │
     ▼
① 순열 σ = hilbertOrder(problem)
     ├─ 요청마다 대표 좌표 (첫 side와 마지막 side의 중점)
     ├─ 전 요청의 (위도, 경도) 최소~최대로 경계 상자
     ├─ 상자 → side×side 격자 칸 (x = 경도 칸, y = 위도 칸)
     ├─ hilbertIndex(side, x, y) → d
     └─ d ASC, 동률 RequestId ASC
     │
     ▼
② 차량 순서 V = vehiclesByCapacityDesc
     └─ maxWeight DESC, VehicleId ASC
     │
     ▼
③ GiantTourSplit.split(problem, profile, σ, V)
     ├─ DP: f(i, k) = σ[1..i] 처리 · V[1..k] 소비의 최소 키
     │       키 = (bank 수, 사용 차량 수, ΣdriveDistMeter)
     │       전이 ① 차량 건너뜀 · ② 요청 bank · ③ 조각 → 경로 (validate 통과만)
     ├─ 역추적: 절단 위치 · bank 집합 · Route 목록 (끝에서부터 쌓아 reversed)
     └─ σ에 없는 요청도 bank (H18의 σ는 전 요청이라 이 분기는 비어 있다)
     │
     ▼
   return Solution
```

h1–h24 §6.4의 1단계(줄 세우기) = 여기 ①, 2단계(어디서 끊을까) = ③, 3단계(점수) = DP 키. 조각 안 방문 순서는 σ 그대로이고 2-opt가 없다.

---

## 4. 단계별 상세

### 4.1 순열 `hilbertOrder` — 네 단계

규범 §5 H18: *anchor 좌표(PD는 픽업·배송의 중점)를 경계 상자로 정규화해 Hilbert 곡선 (차수 16) 인덱스 ASC, 동률 RequestId.* 코드가 그 네 단계를 그대로 한다.

```36:63:solver-core/src/main/java/com/ronext/rpdptw/solve/HilbertSplitConstruction.java
    /** anchor 좌표(PD는 픽업·배송의 중점)를 경계 상자로 정규화해 Hilbert 인덱스 ASC, 동률 RequestId. */
    static List<RequestId> hilbertOrder(Problem problem) {
        Map<RequestId, double[]> points = new LinkedHashMap<>();
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        for (Request request : problem.requests()) {
            Location a = problem.locations().get(InsertionSearch.anchor(request).locationId());
            Location b = problem.locations().get(InsertionSearch.lastSide(request).locationId());
            double[] point = {(a.latitude() + b.latitude()) / 2.0, (a.longitude() + b.longitude()) / 2.0};
            points.put(request.id(), point);
            minLat = Math.min(minLat, point[0]);
            maxLat = Math.max(maxLat, point[0]);
            minLon = Math.min(minLon, point[1]);
            maxLon = Math.max(maxLon, point[1]);
        }
        long side = 1L << ORDER;
        Map<RequestId, Long> index = new LinkedHashMap<>();
        for (Map.Entry<RequestId, double[]> entry : points.entrySet()) {
            long x = normalize(entry.getValue()[1], minLon, maxLon, side);
            long y = normalize(entry.getValue()[0], minLat, maxLat, side);
            index.put(entry.getKey(), hilbertIndex(side, x, y));
        }
        List<RequestId> order = new ArrayList<>(InsertionSearch.sortedRequestIds(problem));
        order.sort(Comparator.<RequestId>comparingLong(index::get).thenComparing(InsertionSearch.BY_REQUEST_ID));
        return order;
    }
```

#### ① 대표 좌표

코드는 패턴 분기가 없다. **항상** `anchor` 위치와 `lastSide` 위치의 산술 중점 `( (lat_a+lat_b)/2 , (lon_a+lon_b)/2 )`이다.

| 패턴 | `anchor` | `lastSide` | 중점의 결과 |
|---|---|---|---|
| `DELIVERY_ONLY` | delivery | delivery | 그 delivery 좌표 (같은 점의 중점) |
| `PICKUP_ONLY` | pickup | pickup | 그 pickup 좌표 |
| `PICKUP_DELIVERY` | pickup | delivery | 픽업·배송의 중점 |

규범 문면 "PD는 픽업·배송의 중점, 나머지는 있는 쪽"과 결과가 같다. 중점은 **순서 키를 만들기 위한 숫자**일 뿐, 가짜 방문이 아니다. pair[^request]는 여전히 통째로 한 칸이다.

#### ② 경계 상자 정규화

전 요청 중점의 `minLat`·`maxLat`·`minLon`·`maxLon`이 상자다. 격자 한 변은

```text
ORDER = 16          // HilbertSplitConstruction 19행. 규범 §5 H18이 "차수 16"으로 적시.
side  = 1L << 16    // = 65,536
```

차수 16은 Stage 8 재량 상수 목록에 없다. 격자를 65,536 × 65,536으로 고정한 해상도 선택이다. 인덱스 범위는 `0 … side²−1 = 4,294,967,295` (약 43억)이고 `long`에 들어간다.

칸 번호:

```65:71:solver-core/src/main/java/com/ronext/rpdptw/solve/HilbertSplitConstruction.java
    private static long normalize(double value, double min, double max, long side) {
        if (max <= min) {
            return 0L;
        }
        long cell = (long) Math.floor((value - min) / (max - min) * side);
        return Math.min(cell, side - 1);
    }
```

- `max <= min` (모든 점이 그 축에서 같음) → 칸 0. 직선 또는 한 점에 몰리면 그 축은 전부 0이다.
- 그 외 `floor( (value−min)/(max−min) × side )`, 상한을 `side−1`로 자른다. `value == max`이면 비율 1 × side = side가 나와서, 자르지 않으면 격자 밖이다.
- **x = 경도 칸, y = 위도 칸** (56–57행: `normalize`의 첫 인자가 lon이면 x, lat이면 y).

#### ③ `hilbertIndex` — §4.2. ④ 정렬 — `sortedRequestIds`를 출발 목록으로 `index ASC`, 동률 `BY_REQUEST_ID`. `LinkedHashMap` 순회로 승자를 고르지 않는다.

### 4.2 `hilbertIndex` — 좌표 변환으로 인덱스 쌓기

쉬운 말 쪽([h1–h24 §6.5](../initial-solution-heuristics-h1-h24.md))은 이 곡선을 "한 붓으로 훑는 길"로 직관을 준다. **여기서부터는 코드가 하는 대입만** 본다.

```73:91:solver-core/src/main/java/com/ronext/rpdptw/solve/HilbertSplitConstruction.java
    /** 표준 xy → d (Hilbert curve, side = 2^ORDER). */
    static long hilbertIndex(long side, long x, long y) {
        long d = 0L;
        for (long s = side / 2; s > 0; s /= 2) {
            long rx = (x & s) > 0 ? 1 : 0;
            long ry = (y & s) > 0 ? 1 : 0;
            d += s * s * ((3 * rx) ^ ry);
            if (ry == 0) {
                if (rx == 1) {
                    x = side - 1 - x;
                    y = side - 1 - y;
                }
                long t = x;
                x = y;
                y = t;
            }
        }
        return d;
    }
```

입력 `(x, y)`는 `0 … side−1` 격자의 칸. 출력 `d`는 그 칸의 인덱스. 루프는 `s = side/2, side/4, …, 1`로 **스케일을 반씩 줄이며** 같은 일을 반복한다. `ORDER=16`이면 16바퀴, 아래 숫자는 `side=4`(2바퀴)와 `side=8`(3바퀴)로 따라간다 — 대입 규칙은 같다.

매 바퀴에서 하는 일:

1. **사분면 비트.** `rx = (x & s) > 0 ? 1 : 0`, `ry`도 y에 대해 같다. `s`는 2의 거듭제곱이므로 `x & s`는 x의 그 비트만 본다. 첫 바퀴 `s = side/2`이면: `rx=1` ↔ x가 오른쪽 반 `[side/2, side)`, `ry=1` ↔ y가 위쪽 반.
2. **블록 번호만큼 d에 더한다.** `q = (3 * rx) xor ry` ∈ {0,1,2,3}. 한 블록의 크기는 `s²`이므로 `d += s² × q`.
3. **`ry == 0`일 때만 (x, y)를 다시 쓴다.** (이 대입은 다음 바퀴의 비트 판정용 지역 변수다. 원래 칸을 바꾸지 않는다.)
   - `rx == 1`이면 격자 중심에 대한 반사: `x ← side−1−x`, `y ← side−1−y`.
   - 이어서 교환: `(x, y) ← (y, x)`.
   - `ry == 1`이면 x, y를 그대로 둔다.

`q = (3 rx) xor ry`의 값:

| rx | ry | `(3*rx) ^ ry` | 이 스케일에서 더해지는 블록 |
|---:|---:|---:|---|
| 0 | 0 | 0 | 블록 0 (d에 `0·s²`) |
| 0 | 1 | 1 | 블록 1 |
| 1 | 1 | 2 | 블록 2 |
| 1 | 0 | 3 | 블록 3 |

#### 한 점의 대입을 숫자로 — `(x, y) = (3, 1)`, `side = 4`

시작: `x=3`, `y=1`, `d=0`.

| 바퀴 | s | `x & s` → rx | `y & s` → ry | q | `d +=` | 대입 |
|---:|---:|---|---|---:|---:|---|
| 1 | 2 | `3&2=2` → 1 | `1&2=0` → 0 | 3 | 4×3 = 12, d=12 | ry=0·rx=1 → 반사 `x=4−1−3=0`, `y=4−1−1=2` → 교환 `(x,y)=(2,0)` |
| 2 | 1 | `2&1=0` → 0 | `0&1=0` → 0 | 0 | 1×0 = 0, d=12 | ry=0·rx=0 → 반사 없음, 교환 `(x,y)=(0,2)` |

결과 **d = 12**. 아래 4×4 표의 `(3,1) → 12`와 같다.

#### 세 바퀴 — `(x, y) = (1, 0)`, `side = 8`

시작: `x=1`, `y=0`, `d=0`.

| 바퀴 | s | rx | ry | q | d | 대입 후 (x, y) |
|---:|---:|---:|---:|---:|---:|---|
| 1 | 4 | 0 | 0 | 0 | 0 | 교환 → (0, 1) |
| 2 | 2 | 0 | 0 | 0 | 0 | 교환 → (1, 0) |
| 3 | 1 | 1 | 0 | 3 | 3 | 반사 `x=8−1−1=6`, `y=8−1−0=7` → 교환 (7, 6) |

결과 **d = 3**.

#### 4×4 표 — 실제 `hilbertIndex(4, x, y)`의 반환값

행은 y (위도 칸, 위로 증가), 열은 x (경도 칸, 오른쪽으로 증가). 함수가 내는 0-based 인덱스 그대로다.

| y＼x | 0 | 1 | 2 | 3 |
|---:|---:|---:|---:|---:|
| 3 | 5 | 6 | 9 | 10 |
| 2 | 4 | 7 | 8 | 11 |
| 1 | 3 | 2 | 13 | 12 |
| 0 | 0 | 1 | 14 | 15 |

인접한 d는 상하좌우로 한 칸 옆이다 (0–1–2–3–4–5–6–7–8–9–10–11–12–13–14–15). 사분면 경계의 점프는 **번호**에서 일어난다 — `(1,0)=1`과 `(2,0)=14`는 격자에서 한 칸 옆인데 d 차이는 13. 2×2 (`side=2`)는 왼쪽 아래 네 칸과 같고, 첫 바퀴의 q 그 자체다: `(0,0)=0`, `(0,1)=1`, `(1,1)=2`, `(1,0)=3`.

### 4.3 워크스루 — 요청 6건, 차량 2대 (순열)

실제 `ORDER=16`이면 칸 번호가 수만 대라, **같은 공식으로 `side=4`인 축소 격자**를 쓴다. 단계(중점 → 상자 → 칸 → d → 정렬)는 코드와 같다.

여섯 요청은 전부 `DELIVERY_ONLY`라 대표 좌표 = 배송지. 구역은 조각 실현성(§4.6)에서만 쓴다.

| RequestId | lat | lon | 구역 |
|---|---:|---:|---|
| A | 0 | 0 | WEST |
| B | 2 | 0 | WEST |
| C | 3 | 0 | WEST |
| D | 3 | 3 | EAST |
| E | 2 | 3 | EAST |
| F | 0 | 3 | EAST |

경계 상자: lat 0–3, lon 0–3.

```text
cell = floor( (value − 0) / (3 − 0) × 4 ) , 상한 3
```

| id | lon → x | lat → y | (x, y) | d |
|---|---|---|---|---:|
| A | 0/3×4 = 0 → 0 | 0 → 0 | (0, 0) | 0 |
| B | 0 → 0 | 2/3×4 = 2.66 → 2 | (0, 2) | 4 |
| C | 0 → 0 | 3/3×4 = 4 → **3** (clamp) | (0, 3) | 5 |
| D | 4 → **3** (clamp) | 4 → **3** | (3, 3) | 10 |
| E | 3 | 2 | (3, 2) | 11 |
| F | 3 | 0 | (3, 0) | 15 |

C·D·E·F는 상자 최댓값이라 비율 1 × side = 4가 나와 `side−1`로 잘린다 — `normalize` 71행이 하는 일이다. d ASC, 동률 없음 → **σ = [A, B, C, D, E, F]**.

서쪽을 남→북으로 이은 다음 동쪽을 북→남으로 잇는다. **C(WEST)와 D(EAST)가 줄에서 이웃**이다. 격자의 왼쪽 위 `(0,3)`와 오른쪽 위 `(3,3)` — 같은 변 양 끝이고 구역이 다르다. 이 한 쌍이 아래 DP에서 조각을 가른다.

PD 한 건이 끼면 대표 좌표만 중점이 된다. 예: pickup (lat,lon)=(3,0), delivery (3,3) → 중점 (3, 1.5) → 칸 하나. 순열에서의 위치는 그 칸의 d이고, **방문 두 개는 조각 안에서 붙어 다닌다** (§4.5).

### 4.4 차량 순서 — 적재량 DESC

```363:369:solver-core/src/main/java/com/ronext/rpdptw/solve/InsertionSearch.java
    /** 차량 순서 (maxWeight DESC, VehicleId ASC) — H6·H8·H17·H18·H19·H20·H21 공통. */
    static List<Vehicle> vehiclesByCapacityDesc(Problem problem) {
        List<Vehicle> vehicles = new ArrayList<>(problem.vehicles());
        vehicles.sort(Comparator.comparingLong(Vehicle::maxWeight).reversed()
                .thenComparing(Vehicle::id, BY_VEHICLE_ID));
        return vehicles;
    }
```

워크스루 차량:

| VehicleId | maxWeight | 정차 한도 B |
|---|---:|---:|
| V1 | 30,000 | 2 |
| V2 | 20,000 | 2 |

V = [V1, V2]. **조각을 만든 뒤에 차에 배정하는 것이 아니다.** V는 DP의 입력 순서이고, 전이 ③은 "지금 남은 차 중 맨 앞"에 조각을 준다. 전이 ①이 그 차를 **안 쓰고 건너뛸** 수 있다. 남은 차는 경로를 만들지 않는다(빈 `Route` 금지). 조각이 안 된 요청은 전이 ②로 bank. "남은 조각 → bank" 분기는 없다 — validate를 통과한 연속 구간만 조각이다.

한계(§6.4와 같음): 순열 앞쪽 조각이 무거운 차를 먼저 가져간다. "이 조각엔 5톤이 필요하다"는 판단을 순서 단계가 하지 않는다.

### 4.5 `appendPair` — 조각 안 순서는 σ 그대로, PD는 붙어서

```107:112:solver-core/src/main/java/com/ronext/rpdptw/solve/GiantTourSplit.java
    /** 순열 순으로, PICKUP_DELIVERY는 p 바로 뒤에 d. */
    static void appendPair(Problem problem, RequestId requestId, List<NodeId> visits) {
        Request request = problem.request(requestId);
        request.pickup().ifPresent(side -> visits.add(side.nodeId()));
        request.delivery().ifPresent(side -> visits.add(side.nodeId()));
    }
```

- `DELIVERY_ONLY` → delivery 노드 1개. `PICKUP_ONLY` → pickup 노드 1개. `PICKUP_DELIVERY` → pickup 다음에 delivery, **항상 인접**.
- 절단의 원자 단위는 `Request`라 pickup과 delivery가 **다른 조각으로 갈 수 없다.**
- 조각 `[A, B, C]`의 방문 목록은 σ 순 `… A의 side, B의 side, C의 side …`다. A→C→B가 이동표상 더 짧아도 바꾸지 않는다. 2-opt 없음. 순서 재배치는 ALNS 몫이다.

DP가 조각 σ[i+1..j]를 만들 때 `j`를 늘리며 `appendPair`를 **누적**한다. 앞 조각을 버리고 다시 짜지 않는다.

### 4.6 `GiantTourSplit` DP — 상태 · 전이 · 역추적

규범 §5 의사코드와 배열 첨자가 1:1이다. 코드는 0-based: `sigma.get(0)` = σ[1], `vehicles.get(0)` = V[1].

```26:61:solver-core/src/main/java/com/ronext/rpdptw/solve/GiantTourSplit.java
    static Solution split(Problem problem, Profile profile, List<RequestId> sigma, List<Vehicle> vehicles) {
        int n = sigma.size();
        int m = vehicles.size();
        long[][][] best = new long[n + 1][m + 1][];
        int[][][] parent = new int[n + 1][m + 1][];                            // {pi, pk, kind}: 1 skip · 2 bank · 3 route
        best[0][0] = new long[] {0L, 0L, 0L};

        for (int i = 0; i <= n; i++) {
            for (int k = 0; k <= m; k++) {
                long[] from = best[i][k];
                if (from == null) {
                    continue;
                }
                if (k < m) {                                                    // ① 차량 V[k+1]을 건너뜀
                    relax(best, parent, i, k + 1, from, i, k, 1);
                }
                if (i < n) {                                                    // ② σ[i+1]을 bank
                    relax(best, parent, i + 1, k, new long[] {from[0] + 1, from[1], from[2]}, i, k, 2);
                }
                if (i < n && k < m) {                                           // ③ σ[i+1..j]를 V[k+1]의 경로로
                    Vehicle vehicle = vehicles.get(k);
                    int limit = vehicle.effectiveMaxStopCount().isPresent()
                            ? Math.min(n, i + vehicle.effectiveMaxStopCount().getAsInt()) : n;
                    List<NodeId> visits = new ArrayList<>();
                    for (int j = i + 1; j <= limit; j++) {
                        appendPair(problem, sigma.get(j - 1), visits);
                        Optional<RouteFacts> facts = InsertionSearch.validate(problem, profile, vehicle.id(), visits);
                        if (facts.isEmpty()) {
                            continue;                                           // 불가하면 전이 없음
                        }
                        relax(best, parent, j, k + 1,
                                new long[] {from[0], from[1] + 1, from[2] + facts.get().driveDistMeter()}, i, k, 3);
                    }
                }
            }
        }
```

#### 상태

| 배열 | 뜻 |
|---|---|
| `best[i][k]` | σ의 앞 i개 처리, V의 앞 k대 소비. 값 `long[3] = (bank 수, 사용 차량 수, Σ거리 m)`. 미도달은 `null` |
| `parent[i][k]` | `{pi, pk, kind}`. kind 1=차량 건너뜀, 2=bank, 3=경로. 역추적용 |
| `best[0][0]` | `(0,0,0)` 만 초기화 |

키는 정식 점수 4축이 아니라 **3축**이다. 운행시간은 DP가 보지 않는다. 3축이 같으면 먼저 쓴 값이 남는다.

#### 전이 — 세 갈래, 이 순서

바깥 루프는 `i ASC`, 안쪽 `k ASC`. 목적지는 항상 i 또는 k가 늘어나므로 DAG다.

| kind | 조건 | 목적지 | 키 변화 | 의미 |
|---:|---|---|---|---|
| ① | `k < m` | `(i, k+1)` | 그대로 | V[k+1]을 **안 씀**. 남은 차를 소진해 `f(n,m)`에 도달하는 통로 (X16) |
| ② | `i < n` | `(i+1, k)` | bank +1 | σ[i+1]을 bank. **항상 가능** — 어떤 경로 전이도 못 드는 요청의 출구 |
| ③ | `i < n` ∧ `k < m` | `(j, k+1)` (`i < j ≤ limit`) | 차량 +1, 거리 += `driveDistMeter` | σ[i+1..j]를 V[k+1]의 경로로. **validate 통과만** |

`limit` = `min(n, i + B)`, `B = effectiveMaxStopCount` (없으면 n). 첨자 `j`는 **요청 수**라, `PICKUP_DELIVERY`(방문 2)면 방문 수가 B를 넘을 수 있다. 그 후보는 전파가 정차 한도로 거절해 `continue` — 결과는 맞고, 거절된 검증만 더 한다.

③의 실현성은 추정치가 아니다. `appendPair`로 만든 방문 목록을 그때그때 `validate`에 넣는다. 실패하면 그 `j`의 전이만 없고, `visits`에는 이미 붙어 있으므로 다음 `j`는 더 긴 목록을 검증한다. 짧은 조각이 실패했다고 긴 조각을 건너뛰지 않는다.

#### `relax` — 엄격히 작을 때만

```90:105:solver-core/src/main/java/com/ronext/rpdptw/solve/GiantTourSplit.java
    /** 동률 = 먼저 계산된 값 유지 — 엄격히 작을 때만 갱신 (결정적). */
    private static void relax(long[][][] best, int[][][] parent, int i, int k, long[] key, int pi, int pk, int kind) {
        if (best[i][k] == null || compare(key, best[i][k]) < 0) {
            best[i][k] = key;
            parent[i][k] = new int[] {pi, pk, kind};
        }
    }

    static int compare(long[] a, long[] b) {
        for (int t = 0; t < 3; t++) {
            if (a[t] != b[t]) {
                return a[t] < b[t] ? -1 : 1;
            }
        }
        return 0;
    }
```

동률에서 ①→②→③ 중 먼저 도착한 kind가 남는다. `HashMap` 순회가 없다.

#### 워크스루 DP — σ = [A,B,C,D,E,F], V = [V1,V2], B = 2

가정 (validate가 이렇게 답한다고 둔다. H18은 이 표를 만들지 않고 매번 전파한다):

- 길이 1 조각: 항상 가능, 거리 1,000 m
- 같은 구역 연속 2건 (`[A,B]`, `[B,C]`, `[D,E]`, `[E,F]`): 가능, 거리 1,600 m
- 구역이 섞인 `[C,D]`: `ZONE_MIX` → 불가
- 길이 3: `j ≤ i+2`라 시도 자체가 없음

next-fit은 V1에 A, B를 담고 C에서 한도 2로 닫은 뒤, V2에 C를 담고 D에서 `ZONE_MIX`로 닫아 D·E·F가 bank — **미배정 3**. Split은 C를 bank에 두고 D·E를 V2에 실어 **미배정 2**가 된다. 그 경로를 상태가 따라가는 방식만 본다.

키는 `(bank, 차량, 거리)`. 승자에 이르는 칸만.

| 전이 | from | to | 키 |
|---|---|---|---|
| 시작 | — | (i=0, k=0) | (0, 0, 0) |
| ③ [A,B] on V1 | (0, 0) | (2, 1) | **(0, 1, 1600)** |
| ② bank C | (2, 1) | (3, 1) | **(1, 1, 1600)** |
| ③ [D,E] on V2 | (3, 1) | (5, 2) | **(1, 2, 3200)** |
| ② bank F | (5, 2) | (6, 2) | **(2, 2, 3200)** = f(n, m) |

`f(6,2) = (2, 2, 3200)`: bank {C, F}, 경로 V1=`[A,B]`, V2=`[D,E]`.

비교용 next-fit 경로: V1에 A·B를 담고 한도 2로 닫음 → V2에 C를 담고 D에서 `ZONE_MIX`로 닫음 → D·E·F bank. 키 `(3, 2, …)`. 1축 3 > 2라 Split이 이긴다. 이것이 T34가 일반화한 부등식이다.

같은 3축 `(2,2,3200)`을 내는 다른 절단(A를 bank, V1=`[B,C]`, V2=`[D,E]`, F bank)도 있다. `relax`가 먼저 도착한 parent를 유지하므로 결과는 하나고 재현된다.

#### 역추적

```63:88:solver-core/src/main/java/com/ronext/rpdptw/solve/GiantTourSplit.java
        // 최종 상태 = f(n, m) — ①이 남은 차량을 전부 건너뛰므로 항상 도달한다 (X16)
        List<Route> routes = new ArrayList<>();
        Set<RequestId> bank = new LinkedHashSet<>();
        int i = n;
        int k = m;
        while (i != 0 || k != 0) {
            int[] p = parent[i][k];
            if (p[2] == 2) {
                bank.add(sigma.get(i - 1));
            } else if (p[2] == 3) {
                List<NodeId> visits = new ArrayList<>();
                for (int t = p[0] + 1; t <= i; t++) {
                    appendPair(problem, sigma.get(t - 1), visits);
                }
                routes.add(new Route(vehicles.get(p[1]).id(), visits));
            }
            i = p[0];
            k = p[1];
        }
        for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {
            if (!sigma.contains(requestId)) {
                bank.add(requestId);
            }
        }
        return new Solution(routes.reversed(), bank);
    }
```

`f(n,m)`에서 시작해 parent를 탄다.

| 현재 (i,k) | kind | 하는 일 | 다음 |
|---|---|---|---|
| (6,2) | ② | bank ← F | (5,2) |
| (5,2) | ③ | Route(V2, visits of D,E) — `vehicles.get(p[1])` = V2 | (3,1) |
| (3,1) | ② | bank ← C | (2,1) |
| (2,1) | ③ | Route(V1, visits of A,B) | (0,0) |
| (0,0) | 종료 | | |

경로는 **뒤에서부터** 쌓이므로 `[Route(V2), Route(V1)]`이 된다. `routes.reversed()`가 `[Route(V1), Route(V2)]`로 뒤집는다. kind ①(skip)은 집합을 건드리지 않고 첨자만 옮긴다 — 그 차는 해에 나타나지 않는다.

σ에 없는 요청을 bank에 더하는 루프는 방어다. H18의 `hilbertOrder`는 `problem.requests()` 전부라 이 집합은 공집합이다.

---

## 5. 결정성 · 종료 · 복잡도

### 5.1 결정성 — 같은 입력이면 같은 출력

| 지점 | 순서를 고정하는 방법 |
|---|---|
| 대표 좌표 | 중점 공식, 분기 없음 |
| 격자 칸 | `floor` + `min(cell, side−1)`. 동률 칸은 다음 행 |
| 순열 | `comparingLong(index).thenComparing(BY_REQUEST_ID)` |
| 차량 순서 V | `maxWeight DESC`, `VehicleId ASC` |
| DP 동률 | `compare < 0`일 때만 갱신. 루프 `i ASC, k ASC`, 전이 ①→②→③ |
| 오라클 | `validate`는 성공/실패만 — 후보 목록을 정렬할 일이 없음 |
| 경로 목록 | 역추적 후 `reversed()` |
| bank | `LinkedHashSet` — 역추적이 넣은 순 + (있다면) ID 순 합류 |

`HashSet`/`HashMap` 순회로 승자를 고르는 곳이 없다. 위도·경도 `double`은 칸 번호를 만드는 입력일 뿐, 동률 판정에 쓰지 않는다.

### 5.2 종료 — 시간이 아니라 구조로

규범 §4.3: *H18/H19 DP 상태 수 (`|requests|+1)×(|vehicles|+1)` 고정 — 루프 상한이 아니라 구조로 종료.*

- 상태 `(n+1)(m+1)`개. 각 칸은 최대 한 번 최선이 정해진다.
- 전이 ②가 모든 `(i<n)`에서 가능하므로 요청을 전부 bank만 해도 `f(n,·)`에 도달한다.
- 전이 ①이 남은 차를 건너뛰어 `f(n,m)`에 도달한다 (주석 63행, X16).
- `checkOuterLoop` 없음. 방어 카운터를 넘기는 경로가 구조상 없다.

### 5.3 복잡도 (규범 문서 §5 표 H18 행)

```text
H18 | hilbert-split | route-first(split) | 전 패턴 | DP 고정 | O(n · B · m) 전파 (B = 경로 방문 상한)
```

풀이:

- 순열: 요청 n개의 좌표 + 정렬 `O(n log n)`. 무시할 수준.
- 경로 전이를 시도하는 칸은 `i < n`, `k < m` — 대략 n·m개. 칸마다 `j`를 최대 B번 늘리며 `validate` 1회 = 그 조각의 전파 1회. 전파 횟수 **O(n · B · m)**.
- B는 `effectiveMaxStopCount` (없으면 n). 실물 fixture는 정차 한도 28이라 B=28, n=452, m=31 → 전파 상한이 대략 452×28×31 ≈ 4·10⁵회. 실측 175 ms (§8).
- 표의 "전 패턴": PD도 σ의 한 칸이고 `appendPair`가 방문 2개를 붙인다. 삽입 계열처럼 위치 쌍 `O(L²)`를 나열하지 않는다.

---

## 6. 코드를 읽으며 눈여겨볼 점

규범 문서와 코드는 1:1이지만, 처음 읽을 때 헷갈리기 쉬운 곳이다. **수정 제안이 아니라 읽기 보조**다.

1. **삽입 루프가 없다.** §6.0의 끼워넣기 비용 `(새 경로, Δ거리, Δ운행시간)`은 H18이 쓰지 않는다. 비용은 DP 키 3축뿐이다.
2. **x는 경도, y는 위도.** `normalize` 인자 순서가 그렇게 고정돼 있다. 위도 1도와 경도 1도의 실제 길이가 다른 왜곡은 §6.5가 이미 적었다.
3. **`side` 반사는 `s`가 아니라 원래 `side`로 한다.** `x = side−1−x`. Wikipedia/Butz 표준과 같다.
4. **조각 안 순서는 σ 그대로.** "최적 분할"은 경계만 최적이다. 방문 순서를 최적화하지 않는다.
5. **차량 순서 V는 DP 이전.** 전이 ①이 건너뛴 차는 해에 없고, 못 실은 요청은 ②로 bank. 남은 조각을 다른 차에 재배정하는 단계가 없다.
6. **`routes.reversed()`는 역추적이 뒤에서부터 쌓기 때문**이다. 점수와는 무관하고 `Solution.equals`·결정성용이다.
7. **DP 키에 운행시간이 없다.** 정식 평가 4축의 마지막은 여기서 동률 깨는 값이 못 된다. 3축이 같으면 루프 순서가 승자를 정한다.
8. **`Problem`은 어디서도 바뀌지 않는다.** `Solution`은 역추적이 끝날 때 한 번 만든다. 실패한 조각 후보는 `continue`로 버릴 뿐 되돌릴 상태가 없다.
9. **H19와의 차이 한 줄.** `NearestNeighborSplitConstruction`은 `nearestNeighborOrder` + 같은 `split` + 같은 `vehiclesByCapacityDesc`. 기권만 `depots.size() != 1`.

---

## 7. 경계 상황 (규범 §7의 X번호)

| 상황 | 동작 | 번호 |
|---|---|---|
| 어떤 경로 전이에도 못 드는 요청 | 전이 ②(bank)가 항상 있어 DP는 완주. 사전식 1축이 그 비용을 진다. `f(n,m)`은 ①이 남은 차를 건너뛰어 도달 | **X16** |
| requests 빈 목록 | n=0. skip만으로 `f(0,m)`. 빈 해 | X1 |
| vehicles 빈 목록 / 전 요청 호환 0대 | m=0이면 ③이 없고 ②만 → 전부 bank. 예외 아님 | X2 |
| 차고 2개 이상 | H18은 실행(좌표만 쓰므로 출발 차고가 필요 없다). H19는 기권 | §4.4 · §5 H18 "depot 무관이라 기권 없음" |
| 정차 한도 부재 | `limit = n`. 한 차가 전 요청을 한 조각으로 시도할 수 있고, 전파가 거절하면 더 짧은 j만 남는다 | §5 H18 B 정의 |
| 모든 요청의 위도 또는 경도가 같음 | 그 축 칸=0. 순열이 사실상 다른 축 + RequestId | `normalize` |
| σ에 없는 요청 | bank 합류. H18 경로에서는 발생하지 않음 | 82–86행 |
| profile hard가 강해 ③이 전부 실패 | ②만으로 전부 bank인 해. Feasible이므로 정상 | X14 |

X16을 코드로 다시: 요청 r이 단독으로도 validate에 실패하면, 그것을 포함하는 모든 ③이 `continue`다. 그래도 `(i, k)`에서 `(i+1, k)`로 가는 ②가 키 `(bank+1, …)`를 남긴다. DP가 중간에 멈추거나 예외를 던지지 않는다.

---

## 8. 테스트와 실측

H18 전용 테스트 클래스는 없다. 절단의 부등식은 T34가, 포트폴리오 일원으로서의 유효성은 T19·T20이 본다.

| 테스트 | 무엇을 고정하나 |
|---|---|
| T34 `GiantTourSplitTest.splitNeverWorseThanNextFitOnSameTour` | **같은 순열·같은 차량 순서**에서 Split의 `(bank, 차량 수, 거리)` 사전식 키 ≤ next-fit 분할 (X16). fixture 셋: `ring(8,3)`, `ring(7,2)`(차가 모자라 bank가 생김), `mixed()`(PD + 차고 2개). 순열은 Hilbert가 아니라 `sortedRequestIds` — **분할 부등식만** 고정한다 |
| `GiantTourSplitTest.splitIsDeterministic` | 같은 σ·V를 두 번 → `key` 배열 동등, `Solution.equals` |
| T19 `ConstructionHeuristicsTest.outerLoopBoundedByRequestCount` | 24개 각각(H18 포함) 예외 없이 끝나 `StructureCheck` 0. 빈 요청·빈 차량 포함 |
| T20 `ConstructionHeuristicsTest.structureHoldsForEveryHeuristic` | pair 원자성 · XOR. PD가 한 경로에 들어가면 pickup 인덱스가 delivery 이하 (`appendPair`가 p 다음에 d) |
| T15 (다중 depot) | H19는 `ABSTAINED`, H18은 목록에 남아 실행 |

T34의 next-fit은 "현재 차에 `appendPair`로 붙여 `validate`가 되면 확정, 안 되면 그 차를 닫고 다음 차. 차 없으면 bank"다. 앞 요청을 bank에 두고 뒤 묶음을 살리는 선택이 없다. Split의 ②가 그 선택이라, §4.6 워크스루처럼 1축이 더 좋을 수 있고, 나쁠 수는 없다.

실물 실측 ([h1–h24 §1](../initial-solution-heuristics-h1-h24.md), 2026-09-04, 정식 평가, 24개 중 **20위**):

| 기법 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 |
|---|---:|---:|---:|---:|---:|
| H23 `zone-quota-balanced-fill` (1위) | 0 | 31 | 4,198,408 | 1,002,069 | 550 ms |
| H19 `nearest-neighbor-split` (19위) | 146 | 27 | 4,117,051 | 764,837 | 182 ms |
| **H18 `hilbert-split`** | **165** | **30** | **5,353,989** | **840,954** | **175 ms** |

> 이 표는 한 번의 실행 기록이고 T번호로 고정돼 있지 않다. 회귀로 고정된 실물 결과는 T44(H23)뿐이다.

**가설:** 실물에서 약한 이유(미배정 165)는 측정된 사실이 아니다. [h1–h24 §6.4](../initial-solution-heuristics-h1-h24.md)의 가설을 코드 용어로 옮기면: 실물은 한 경로가 구체 구역 1종만 담는다(`ZONE_MIX`). Hilbert 순열은 `zoneId`를 읽지 않으므로 σ에서 이웃한 두 요청이 다른 구역일 수 있고, 그 경계(`§4.3`의 C\|D)를 넘는 ③은 전파가 거절한다. 조각이 구역 경계마다 잘리고, B=2가 아니어도 짧은 조각이 차를 소비해 뒤 요청이 bank로 밀린다. 이 가설은 §4.3 (a) "구역을 지운 변형"이 Stage 8에서 검증한다 — 거기서 H18이 크게 오르지 않으면 축 ⑤의 대표를 바꾼다.

실행 명령:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env
mvn test -pl solver-core -Dtest=GiantTourSplitTest
mvn test -pl solver-core -Dtest=ConstructionHeuristicsTest
```

---

## 용어 각주

[^cvrptw]: **CVRPTW** (Capacitated Vehicle Routing Problem with Time Windows) — 용량 제한과 시간창이 있는 차량 경로 문제.
    이 저장소가 푸는 **RPDPTW**는 거기에 pickup-and-delivery와 실무 제약(차급·구역·정차 한도·근무창 등)을 더한 "rich" 변형이다.

[^construction]: **construction (heuristic)** — 빈 해에서 시작해 요청을 넣어 첫 해를 만드는 기법. 이 뒤에 오는 ALNS가 그 해를 부수고 다시 고친다. 이 저장소에서는 construction이 24개이고 난수를 쓰지 않는다.

[^hilbert]: **Hilbert 곡선** — 정사각 격자의 칸 `(x, y)`를 정수 `d`로 보내는 매핑. 코드의 `hilbertIndex`가 그 매핑이다. 배차에서는 순열 키로만 쓰고 거리 계산에는 쓰지 않는다.

[^giant-tour]: **giant tour (거대 경로)** — 차 구분 없이 모든 요청을 하나의 긴 순열 σ로 세운 것. Split은 이 줄을 잘라 조각마다 차 한 대를 준다.

[^dp]: **DP (dynamic programming, 동적 계획법)** — 문제를 "상태 → 다음 상태" 전이로 쪼개고, 상태마다 최선값 하나만 기억해 같은 계산을 반복하지 않는 기법. 여기서 상태는 "σ의 앞 i개, 차 k대"이고, 값은 (bank, 차량 수, Σ거리) 사전식 최소다.

[^lexicographic-score]: **사전식 점수** — `long[]`을 앞 원소부터 비교하는 순서. 기본 profile의 축은 **(미배정 수, 사용 차량 수, 총 거리 m, 총 운행시간 s)** 네 개. H18 DP의 키는 앞 세 개만. 가중합으로 뭉개지 않는다. hard 위반은 점수 축이 아니다 — 위반이 있는 해는 평가가 Infeasible이고, construction 결과는 `validate` 덕에 Infeasible일 수 없다.

[^next-fit]: **next-fit** — 한 줄을 앞에서부터 현재 통(차)에 담다가 안 들어가면 그 통을 닫고 다음 통으로만 간다. 앞 통으로 되돌아가지 않고, 한 칸을 비워 뒤를 살리는 선택도 없다. T34의 비교 대상.

[^route-first]: **route-first (cluster-first/route-first의 반대쪽)** — 방문 순서(거대 경로, giant tour)를 먼저 정하고 어디서 자를지로 차를 나눈다. 반대는 "누구를 같은 차에"를 먼저 정하고 차 안에서 순서를 정하는 cluster-first (H6·H21 등).

[^propagation]: **전파 (`RoutePropagator`)** — 경로의 방문 순서가 주어졌을 때 차고 출발부터 도착·서비스·적재·정차 수를 계산하고 hard 제약을 검사해 Feasible 또는 Infeasible로 답하는 부품. 조각 후보 하나마다 그 경로를 처음부터 다시 전파한다.

[^profile]: **`Profile`** — 고객별 hard/soft 제약과 점수 축. core에 `if (customerId == …)`를 두지 않고 이 객체를 갈아 끼운다. 탐색과 재검증에 같은 인스턴스를 쓴다.

[^initial-solution]: **초기해** — ALNS가 출발점으로 삼는 첫 `Solution`. 24개 construction 결과 중 정식 평가 점수가 가장 좋은 것.

[^determinism]: **결정적(deterministic)** — 같은 입력에 항상 같은 출력. 재검증과 벤치마크 재현을 위해 초기해에서 난수를 금지한다.

[^bank]: **bank** — 어떤 경로에도 들어가지 못한 요청 ID의 집합. 모든 요청은 "경로 안" 또는 "bank" 중 정확히 한 곳에 있다(XOR). bank가 남은 해도 유효하다 — 점수의 미배정 축이 나쁠 뿐이다.

[^problem]: **`Problem`** — 입력(요청·차량·차고·이동표)을 검증·정규화하고 호환표까지 계산해 **동결**한 객체. 탐색은 읽기만 한다.

[^request]: **`Request`** (pair) — 주문 하나. 패턴은 `PICKUP_DELIVERY`(방문 2), `DELIVERY_ONLY`, `PICKUP_ONLY`. 배정·제거의 원자 단위라 pickup만 따로 옮기는 일은 없다.

[^spi]: **SPI (Service Provider Interface)** — 구현체를 여러 개 꽂는 인터페이스. `ConstructionHeuristic`을 구현한 24개 클래스가 `InitialSolutionBuilder.defaults()`에 우선순위 순으로 등록된다.

[^abstain]: **기권 (abstain)** — 이 기법이 이 `Problem`에서 성립하지 않아 실행하지 않고 건너뜀. 실패가 아니다. H18은 기권하지 않고, H19는 차고가 하나가 아니면 기권한다.

[^oracle]: **오라클** — "이 후보가 유효한가·비용이 얼마인가"를 정확히 답하는 검사기. 여기서는 `InsertionSearch.validate` — 호환 + 전파 + profile hard. H18은 위치 탐색을 위임하지 않고 **실현성만** 위임한다.

[^hard-soft]: **hard / soft 제약** — hard는 어기면 해가 무효(용량·시간창·정차 한도·구역 단일성·차급 등), soft는 어겨도 점수가 나빠질 뿐. construction의 경로는 hard를 통과한 것만 받는다.

[^stop-limit]: **정차 한도 (`effectiveMaxStopCount`)** — 한 경로의 최대 정차 수. 있으면 DP의 조각 길이 상한 B, 없으면 n. 실물 fixture는 28.

[^zone-mix]: **`ZONE_MIX`** — "한 경로가 두 종류 이상의 구체 구역을 방문했다"는 hard 위반. `zoneId`가 없거나 `"ALL"`인 방문은 세지 않는다. 전파가 판정하므로 H18은 따로 검사하지 않는다. 순열이 구역을 모르면 이 위반 때문에 ③이 거절된다 — §8 가설의 메커니즘.

[^fixture]: **fixture** — 테스트·실측에 쓰는 고정 입력. "실물 fixture"는 `data/win_poc_case_floor.json` (주문 452건·차량 31대). T34가 쓰는 `ring`·`mixed`는 손 조립 합성 입력이다.

[^greedy]: **greedy(탐욕법)** — 매 단계에서 지금 가장 좋아 보이는 선택을 하고 되돌리지 않음. next-fit이 그 예. H18의 순열은 greedy가 아니고(좌표 규칙), 절단은 DP라 같은 순열 안에서는 greedy가 아니다.
