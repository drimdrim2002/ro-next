# `ZoneQuotaBalancedFillConstruction` (H23) 코드 해설

> **성격**: 코드 해설 노트(비규범). 규범은 [heuristics 문서 §5 H23](../implementation/stage-04-initial-solution-heuristics.md)이고,
> 근거 실측은 [survey §2.5](../implementation/stage-04-initial-solution-heuristics-survey.md)다. 둘과 이 노트가 어긋나면 그쪽이 이긴다.
>
> **대상 독자**: CVRPTW[^cvrptw]를 아는 Java 개발자. 이 저장소의 용어(`Request`·`Problem`·bank 등)는 처음 본다고 가정하고,
> 처음 나올 때마다 각주로 풀었다. 각주는 문서 끝 [용어 각주](#용어-각주)에 모여 있다.
>
> **대상 파일**: `solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaBalancedFillConstruction.java` (200줄, 2026-09-02 기준).

---

## 0. 한 줄 요약

**"구역(존)마다 어떤 차량을 몇 대 줄지를 먼저 전역으로 정하고(DP[^dp]), 그 다음 존 안에서 '부피와 정차 횟수가 동시에 딱 맞게 차는' 차량부터 채운다."**

이름을 뜯어 보면 그대로다.

| 이름 조각 | 뜻 |
|---|---|
| `zone-quota` | 존별 차량 **할당량**(quota)을 먼저 정한다 — `ZoneQuotaAllocation`이 담당 |
| `balanced-fill` | 존 내부 적재는 부피·정차 **두 축의 균형**을 보는 규칙으로 채운다 — 이 파일의 본체 |
| `Construction` | 초기해 구축 기법[^construction] — 빈 해에서 시작해 요청을 하나씩 넣는다 |

이 기법은 문헌이 아니라 **실물 데이터의 구조**에서 나왔다. 실물 fixture[^fixture](주문 452건·차량 31대)는 세 가지 특징이 있다
(survey §2.5): ① 주문 부피 합이 차량 부피 합의 95.8%라 **여유가 4.2%뿐**이고, ② 주문이 허용하는 차급[^vehicle-class]이
구역과 거의 1:1이며, ③ 한 경로는 구체 구역 하나만 돌 수 있다(Domain §3.4 `ZONE_MIX`[^zone-mix]). 이런 구조에서는
"어느 존에 어떤 차급 조합을 주는가"가 곧 미배정 수를 결정하고, 여유가 4.2%라 그 조합을 존 하나씩 greedy[^greedy]로 고르면 뒤 존이
굶는다. 그래서 조합을 전역 DP로 정한 뒤 존 안을 채운다. 결과는 실물 fixture에서 **미배정 0·31대**(종전 1위 H3[^h3]는 미배정 15)다.

---

## 1. 프로젝트 안에서의 위치

### 1.1 초기해 포트폴리오의 한 칸

이 저장소의 초기해[^initial-solution]는 하나가 아니다. `InitialSolutionBuilder.build(problem, profile)`가 결정적 construction
**24개**(H1~H24)를 전부 돌려 각 결과를 정식 평가(`Evaluator`)에 넣고, 사전식 점수[^lexicographic-score]가 가장 좋은 하나를 고른다.
H23은 그 24개 중 23번째이고, 실물 fixture에서는 이 H23이 선택된다.

```text
InitialSolutionBuilder.build(problem, profile)
  ├─ H1 … H22   (기본 8 + 확장 14)
  ├─ H23 ZoneQuotaBalancedFillConstruction   ← 이 문서
  └─ H24 ZoneQuotaSubsetFillConstruction     (같은 존 배정 DP, 존 내부 적재만 다름)
  → 각 Solution을 Evaluator로 평가 → 사전식 최선 1개
```

### 1.2 구현하는 인터페이스 — `ConstructionHeuristic` (SPI[^spi])

```java
public interface ConstructionHeuristic {
    String id();                                   // "zone-quota-balanced-fill"
    boolean abstains(Problem problem);             // 이 Problem에서 성립하지 않으면 true → 실행하지 않고 건너뜀
    Solution construct(Problem problem, Profile profile);   // 빈 해에서 시작해 Solution 하나
}
```

세 가지 계약이 이 파일 전체를 지배한다.

1. **난수 없음, 결정적**[^determinism]. 같은 `Problem`이면 언제 어디서 돌려도 같은 `Solution`이 나와야 한다. 그래서 모든 정렬이
   문자열 ID 비교로 끝나고, `HashMap` 순회로 승자를 고르지 않는다.
2. **반환 해는 항상 유효**하다. 넣지 못한 요청은 bank[^bank]에 남기면 되고, 경로에 들어간 요청은 전부 시간창·용량·정차 한도 등
   hard 제약[^hard-soft]을 통과한 상태다. 이건 이 파일이 직접 보장하는 게 아니라, 삽입을 전부 `InsertionSearch`(§2.2)를 통해서만 하기 때문에
   자동으로 따라온다.
3. **기권(abstain)은 실패가 아니다**. DP 상태 수가 너무 커지는 입력이면 안 돌리고 넘어간다.

### 1.3 다루는 자료형 요약

| 타입 | 이 파일에서의 뜻 |
|---|---|
| `Problem`[^problem] | 동결된 입력 전체. 요청·차량·이동표·호환표. 탐색은 절대 고치지 않는다 |
| `Request`[^request] | 배차의 원자 단위. pickup·delivery side 중 있는 것들, `totalVolume`·`totalWeight` |
| `Vehicle` | `maxVolume`·`maxWeight`·`effectiveMaxStopCount`(정차 한도[^stop-limit], optional)·근무창(일할 수 있는 시간대)·차고 |
| `Solution` | `List<Route>` + bank(`Set<RequestId>`). 불변 record — 삽입할 때마다 새 객체 |
| `Route` | `VehicleId` + 방문 노드 목록. **빈 경로는 존재하지 않는다**(생성자가 막음) |
| `Profile`[^profile] | 고객별 hard/soft 제약 묶음. 이 파일은 그대로 `InsertionSearch`에 넘기기만 한다 |
| `InsertionSearch.Candidate` | "이 차량의 이 위치에 넣으면 이런 방문 순서가 되고 비용이 이만큼 는다"는 삽입 후보 |
| `ZoneQuotaAllocation.Allocation` | 존 배정 DP의 결과 — 유형 목록·존 목록·존별 차량 목록 |

---

## 2. 기대는 부품 두 개

이 파일은 "무엇을 어디에 넣을지"만 정하고, **"넣을 수 있는가·어느 위치가 싼가"는 전부 `InsertionSearch`에, "존에 어떤 차량을
줄까"는 전부 `ZoneQuotaAllocation`에** 맡긴다. 두 부품의 계약을 먼저 알아야 본체가 짧게 읽힌다.

### 2.1 `ZoneQuotaAllocation` — 존 배정 DP (H23·H24 공통)

`allocate(problem)` 한 번으로 아래 세 가지를 돌려준다.

```java
record VehicleType(Set<RequestId> compatible, long maxWeight, long maxVolume,
                   OptionalInt maxStopCount, List<VehicleId> vehicles) {}
record Zone(String zoneId, List<RequestId> members) {}
record Allocation(List<VehicleType> types, List<Zone> zones, Map<String, List<VehicleId>> vehiclesByZone) {}
```

- **차량 유형**(`VehicleType`) = (호환 요청 집합, 최대 무게, 최대 부피, 정차 한도)가 완전히 같은 차량들의 묶음.
  근무창·차고·속도는 유형에 넣지 않는다 — 그 차이는 나중에 삽입 오라클이 판정한다. 유형 순서는 `maxVolume` 오름차순
  (작은 차부터), 유형 안 차량은 `VehicleId` 오름차순.
- **존**(`Zone`) = 요청의 anchor side[^anchor](pickup이 있으면 pickup, 없으면 delivery)의 `zoneId`로 묶은 요청 목록.
  `zoneId`가 없는 요청은 `"(none)"`이라는 가상 존 하나로 모인다. **호환 차량이 0대인 요청은 어느 존에도 들어가지 않는다**
  (어차피 어디에도 못 넣으니 처음부터 bank).
- **존 순서** = (호환 유형 수 오름차순, 총 부피 내림차순, zoneId 오름차순). "고를 수 있는 차량 종류가 적은 존", 즉 제약이
  센 존부터 처리한다.
- **DP**는 "남은 유형별 대수 벡터"를 상태로 두고 존을 하나씩 소비하며, 존마다 (a) 그 존을 딱 덮는 최소 조합 (b) 한 대 모자란
  최대 조합 (c) 아무것도 안 줌, 이 세 갈래(프론티어[^frontier])만 전이한다. 값은 **(총 부족, 총 낭비, 총 사용 대수)** 사전식 최소.
  "덮는다(covers)"의 판정에는 부피·무게·방문 수 합계뿐 아니라 **호환 그룹 누적 검사(Hall 조건)**[^hall]가 들어 있어,
  큰 차가 큰 차 금지 존에 배정되는 일을 막는다.
- **결과**: `vehiclesByZone`은 존마다 실제 `VehicleId` 목록. 한 차량은 정확히 한 존에만 들어간다(상태가 대수를 차감하므로).
  배정을 못 받은 존은 빈 목록이다.

H23이 여기서 읽는 것은 딱 셋이다: `zones()`(순서 포함), `vehiclesByZone()`, 그리고 요청 정렬 키를 만들기 위한 `types()`.

> DP의 상태·전이·`covers`·프론티어·역추적을 예제 하나로 끝까지 따라가는 보강 자료가 따로 있다 —
> [`ZoneQuotaAllocation` — 존 배정 DP는 어떻게 최적화하는가](zone-quota-allocation-dp.md). greedy가 실패하는 반례와 층별 표·상태 격자 그림을 담았다.

### 2.2 `InsertionSearch` — 삽입 오라클[^oracle]

H23이 부르는 메서드는 아래 표가 전부다.

| 메서드 | 하는 일 | H23에서 쓰는 곳 |
|---|---|---|
| `emptySolution(problem)` | 전 요청이 bank인 빈 해 | 시작점 |
| `candidatesFor(problem, profile, current, r, v)` | **차량 v 하나**에 요청 r을 넣을 수 있는 모든 위치를 비용 오름차순으로. 비호환·불가면 빈 목록. v에 경로가 없으면 "새 경로" 후보 | 존 내부 적재, 1-1 교환 |
| `candidates(problem, profile, current, r)` | **모든 기존 경로 + 미사용 호환 차량 1대**에 대해 같은 것 | leftover pass |
| `apply(problem, current, candidate)` | 후보를 적용한 **새** `Solution`. 그 요청은 bank에서 빠진다 | 모든 삽입 |
| `remove(problem, current, q)` | 요청 q를 경로에서 빼 bank로 돌린 새 `Solution`. 경로가 비면 `Route` 자체가 사라진다 | 1-1 교환 |
| `visitsOf(current, v)` · `requestOf(problem, node)` | 차량 v의 방문 노드 목록 / 노드 → 요청 ID | 1-1 교환에서 경로 위 요청 나열 |
| `checkOuterLoop(id, n, bound)` | 바깥 루프 방어 카운터. 상한을 넘으면 `IllegalStateException` | 세 루프 전부 |
| `sortedRequestIds(problem)` · `BY_REQUEST_ID` | ID 문자열 순 정렬 도구 | 결정성 |

`candidatesFor`가 "넣을 수 있다"고 말하려면 세 관문을 다 통과해야 한다 — ① 호환 필터(차급·capability[^capability]·구역·차고),
② `RoutePropagator`[^propagation]가 시간창·용량·정차 한도·구역 단일성 등을 경로 전체에 다시 전파해 Feasible, ③ profile의 hard 제약 전부
satisfied. 그래서 **H23은 용량이나 시간창을 스스로 검사하지 않는다**. 이 파일에 등장하는 `maxVolume`·정차 한도 계산은
전부 "어느 차량이 더 잘 맞는가"를 고르는 **점수용 근사**이지, 유효성 판정이 아니다. 이 구분이 이 파일을 읽는 핵심이다.

후보의 비용은 `(새 경로 여부, Δ거리, Δ운행시간)` 사전식이라, `cands.getFirst()`는 "그 차량 안에서 가장 싼 위치"다.

---

## 3. 알고리즘 전체 흐름

```text
abstains?  ─ Π(유형별 대수+1) > 65,536 이면 기권
     │
     ▼
① 존 배정 DP   allocation = ZoneQuotaAllocation.allocate(problem)
     │           → 존 순서, 존별 요청, 존별 차량(bins)
     ▼
② 존마다 반복 (존 순서대로)
     ├─ ②-a 존 내부 적재   요청을 (호환 유형 수↑, 부피↓, ID↑) 순으로 돌며
     │                     "부호 있는 정차 예산 키"가 가장 좋은 bin에 오라클 최선 위치로 삽입
     │                     후보 bin이 하나도 없으면 → zoneLeftovers
     └─ ②-b 1-1 교환       zoneLeftovers를 부피 큰 것부터: 존 경로의 더 작은 요청 q를 빼고
                           r을 그 자리에, q를 존의 다른 경로에 — 첫 성공에서 멈춤. 실패 → leftovers
     ▼
③ leftover 수집   존에 못 들어간(호환 0대) 요청도 leftovers에 합류
     ▼
④ leftover pass   leftovers를 같은 정렬로 돌며 candidates() 최선(어느 경로든·새 차량이든)에 삽입
                  후보 0개면 bank 그대로
     ▼
   return current
```

바깥 루프는 ②-a에서 요청마다 1회, ②-b에서 leftover마다 1회, ④에서 leftover마다 1회라 합쳐서 **≤ 3n**(n = 요청 수)이고,
코드의 `bound = 3 * problem.requests().size()`가 그 상한이다.

---

## 4. 단계별 상세

### 4.1 `abstains` — 기권 판정

```java
public boolean abstains(Problem problem) {
    return ZoneQuotaAllocation.combinations(ZoneQuotaAllocation.vehicleTypes(problem))
            > ZoneQuotaAllocation.MAX_COMBINATIONS;   // 65,536
}
```

DP의 상태 수는 유형별 대수를 c₁…c_k라 할 때 Π(c_t + 1)이다. 31대가 전부 서로 다른 유형이면 2³¹이라 배열로 들 수 없으니,
65,536을 넘으면 기권한다. `combinations`는 한계를 넘는 순간 곱하기를 멈추므로 오버플로 걱정은 없다.

- 실물 fixture: 유형 6종, 조합 25,920 → 실행.
- 규모 테스트(T25) 합성 입력: 32 → 실행.
- 유형이 제각각인 17대(2¹⁷ = 131,072) → 기권 (T39[^tx]).

`abstains`는 `Problem`만 보고 부작용이 없어야 한다는 SPI 계약을 지킨다. `vehicleTypes`를 여기서 한 번, `allocate` 안에서 또 한 번
계산하는데 O(요청 × 차량)이라 무시할 수준이다.

### 4.2 존 배정 — DP 결과 받기

```java
Allocation allocation = ZoneQuotaAllocation.allocate(problem);
Solution current = InsertionSearch.emptySolution(problem);
int bound = 3 * problem.requests().size();
int iterations = 0;
List<RequestId> leftovers = new ArrayList<>();
```

이 시점의 `current`는 경로 0개, bank = 전 요청이다. `leftovers`는 존 처리가 끝난 뒤 ④에서 다시 시도할 요청을 존 순서대로
모으는 목록이다.

### 4.3 존 내부 적재 — 이 파일의 본체

```java
for (Zone zone : allocation.zones()) {
    List<VehicleId> bins = allocation.vehiclesByZone().get(zone.zoneId());
    List<RequestId> order = requestOrder(problem, allocation, zone.members());
    Map<VehicleId, Load> loads = new LinkedHashMap<>();
    for (VehicleId vehicleId : bins) {
        loads.put(vehicleId, new Load());
    }
    ...
```

- **bins** = DP가 이 존에 준 차량들. 한 차량은 한 존에만 속하므로 **이 존을 시작할 때 bins의 경로는 전부 비어 있다**.
  이름이 bin인 이유는 이 단계가 사실상 bin packing[^bin-packing]이기 때문이다 — 요청 = 아이템, 차량 = bin.
- **loads** = bin마다 "지금까지 얼마나 실었나"의 장부(`Load { long volume; int visits; }`). 경로를 매번 다시 훑지 않고
  키를 계산하려고 둔 캐시이고, 이 존의 적재 루프 안에서만 정확하다(§6 참고).

#### 요청 순서 — `requestOrder`

```java
static List<RequestId> requestOrder(Problem problem, Allocation allocation, List<RequestId> members) {
    order.sort(Comparator.<RequestId>comparingInt(id -> ZoneQuotaAllocation.compatibleTypeCount(allocation.types(), id))
            .thenComparing(Comparator.comparingLong((RequestId id) -> problem.request(id).totalVolume()).reversed())
            .thenComparing(InsertionSearch.BY_REQUEST_ID));
    return order;
}
```

세 키의 뜻:

| 순서 | 키 | 왜 |
|---:|---|---|
| 1 | 호환 유형 수 **오름차순** | 갈 수 있는 차량 종류가 적은 요청(까다로운 요청)을 먼저. 나중에 넣으면 자리가 없다 |
| 2 | 총 부피 **내림차순** | bin packing의 First-Fit-Decreasing 원리 — 큰 것부터 넣어야 자투리가 남지 않는다 |
| 3 | `RequestId` 오름차순 | 동률을 문자열로 깨서 결정성 확보 |

첫 키가 **필수**라는 것이 실측으로 확인됐다. 부피순만 쓰면 여유가 0.12 CBM[^cbm]뿐인 ZONE_21에서 13건이 미배정이고,
호환 수를 먼저 보면 2건이다(survey §2.5 민감도). 같은 존 안에 "작은 차만 되는 주문"과 "아무 차나 되는 주문"이 섞여 있을 때,
아무 차나 되는 큰 주문이 먼저 작은 차의 자리를 차지해 버리는 것을 막는 키다. 이 메서드는 ④ leftover pass에서도 그대로 쓴다.

#### 요청 하나를 넣는 루프

```java
for (int i = 0; i < order.size(); i++) {
    InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
    RequestId requestId = order.get(i);
    Request request = problem.request(requestId);
    double mean = meanVolume(problem, order, i + 1);            // ← 아직 처리 안 한 요청들의 평균 부피
    VehicleId chosen = null;
    Candidate chosenCandidate = null;
    double[] chosenKey = null;
    for (VehicleId vehicleId : bins) {
        List<Candidate> cands = InsertionSearch.candidatesFor(problem, profile, current, requestId, vehicleId);
        if (cands.isEmpty()) {
            continue;                                             // 이 bin엔 못 넣는다 (오라클 판정)
        }
        double[] key = key(problem.vehicle(vehicleId), loads.get(vehicleId), request, mean);
        if (chosen == null || compare(key, chosenKey) < 0) {      // 동률은 bins 순서의 앞
            chosen = vehicleId;
            chosenCandidate = cands.getFirst();                   // 그 bin 안의 최선 위치
            chosenKey = key;
        }
    }
    if (chosen == null) {
        zoneLeftovers.add(requestId);                             // 규칙 b — 후보 0개
        continue;
    }
    current = InsertionSearch.apply(problem, current, chosenCandidate);   // 규칙 a — 삽입
    loads.get(chosen).add(request);
}
```

읽는 요령: **"들어갈 수 있는가"는 오라클, "어느 bin이 좋은가"는 키, "bin 안 어느 위치인가"는 오라클의 최선 후보**.
세 역할이 분리돼 있다.

주석의 "규칙 a/b"는 heuristics 문서 §4.3의 종료 규칙이다 — 바깥 루프 한 바퀴마다 (a) 요청 1개를 삽입하거나 (b) 요청 1개를
"후보 0개"로 제외한다. 어느 쪽이든 미처리 집합이 1 줄어드니 루프는 반드시 끝난다. `checkOuterLoop`는 그 약속이 깨졌을 때
조용히 자르지 않고 예외를 던지는 안전장치다.

#### 평균 부피 — `meanVolume`

```java
private static double meanVolume(Problem problem, List<RequestId> order, int from) {
    if (from >= order.size()) return 0.0;
    long total = 0L;
    for (int i = from; i < order.size(); i++) {
        total = Math.addExact(total, problem.request(order.get(i)).totalVolume());
    }
    return (double) total / (double) (order.size() - from);
}
```

"지금 넣는 요청 r을 제외하고, 이 존에서 **앞으로 처리할** 요청들의 평균 부피". 마지막 요청이면 0. 이 값이 다음 절의 예산
계산에서 "남은 정차 한 칸에 평균 얼마짜리 주문이 들어올까"의 추정치로 쓰인다. 정렬이 부피 내림차순이라 이 평균은 뒤로 갈수록
작아진다 — 즉 "앞으로 올 주문은 지금 것보다 작다"가 자연스럽게 반영된다.

#### 선택 키 — `key`  (이 기법의 핵심 아이디어)

```java
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

수식으로 쓰면:

```text
남은 부피      = maxVolume − 실린 부피 − volume(r)
남은 정차      = 정차 한도 − 실린 방문 수 − visitCount(r)          (visitCount: 단일 패턴 1, PICKUP_DELIVERY 2)
남을 부피(예측) = 남은 부피 − 평균 × 남은 정차
키             = ( 남을 부피 < 0 ?1:0 ,  |남을 부피| ,  남은 부피 )   → 사전식 오름차순, 동률은 bins 순서
```

**직관**: "r을 이 bin에 넣은 뒤, 남은 정차 칸을 전부 평균 크기 주문으로 채운다면 부피가 얼마나 남을까?"

| 남을 부피의 부호 | 뜻 | 취급 |
|---|---|---|
| **음수** | 부피보다 **정차 칸이 먼저 바닥난다**. 정차가 다 차도 부피가 남으니 그 부피는 **낭비**된다 | 첫 키가 1 → 뒤로 미룬다 |
| **양수** | 정차 칸보다 **부피가 먼저 바닥난다**. 남는 건 정차 칸뿐이고, 희소 자원인 부피(실물 여유 4.2%)는 다 쓴다 | 첫 키가 0 → 우선 |
| 0에 가까움 | 두 축이 **동시에** 딱 찬다 — 가장 잘 맞는 bin | 둘째 키 \|남을 부피\|가 작을수록 좋음 |

셋째 키 `남은 부피`는 앞 둘이 같을 때 고전적인 best-fit[^best-fit](남는 공간이 가장 적은 bin)으로 깬다.

왜 이게 필요한가? **부피만 보는 best-fit은 정차 한도가 지배하는 존에서 실패한다.** 소량·다건 존(실물의 ZONE_24·29)에서
큰 차에 작은 주문을 먼저 넣으면 큰 차의 정차 칸이 소량 주문으로 다 차 버리고, 부피는 절반도 못 채운 채 닫힌다. 이 규칙은
그런 bin에 음수 부호를 붙여 뒤로 미룬다. 실측에서 존 내부를 부피 best-fit(BFD)으로 하면 미배정 60, 이 규칙이면 11이었다
(survey §2.5, 근사 잣대 기준).

**정차 한도가 없는 차량**(`effectiveMaxStopCount` 부재)이면 `predicted = 남은 부피`가 되어 키가 `(0, 남은 부피, 남은 부피)`,
즉 순수 부피 best-fit으로 **퇴화**한다. 기권하지 않는다(경계 상황 X23).

부동소수(`double`)는 여기서 **정렬 키로만** 쓰고 동률 판정에는 쓰지 않는다 — `compare`가 `<0`일 때만 바꾸므로 동률이면 bins에서
먼저 나온 차량이 남는다(heuristics §4.2 결정성 규칙).

#### 워크스루 — 테스트 T40의 숫자로

T40(`signedBudgetAvoidsWastingStops`) 입력: 차량 V1(부피 20,000·정차 4)·V2(부피 4,000·정차 4), 요청 R1~R4는 부피 4,000,
R5~R8은 1,000. 전부 `DELIVERY_ONLY`, 존 없음(`"(none)"` 존 하나). 단위는 ×1000한 정수[^units]라 20,000 = 20 단위다.

DP: 유형 순서는 `maxVolume` 오름차순이라 유형0 = V2, 유형1 = V1. 존 수요는 부피 20,000·방문 8. V1만으로는 방문 4 < 8, V2만으로는
부피 4,000 < 20,000이라 둘 다 줘야 덮인다 → bins = [V2, V1] (유형 순서 × 유형 안 ID 순). 요청 순서 = R1 R2 R3 R4 R5 R6 R7 R8.

| 단계 | r | 평균(뒤 요청) | V2 키 (남은부피, 남은정차 → 예측) | V1 키 | 선택 |
|---:|---|---:|---|---|---|
| 1 | R1 4,000 | 16,000/7 ≈ 2,286 | 0, 3 → −6,857 ⇒ **(1**, 6857, 0) | 16,000, 3 → +9,143 ⇒ (0, 9143, 16000) | **V1** |
| 2 | R2 4,000 | 2,000 | 0, 3 → −6,000 ⇒ (1, …) | 12,000, 2 → +8,000 ⇒ (0, 8000, 12000) | V1 |
| 3 | R3 4,000 | 1,600 | 0, 3 → −4,800 ⇒ (1, …) | 8,000, 1 → +6,400 ⇒ (0, 6400, 8000) | V1 |
| 4 | R4 4,000 | 1,000 | 0, 3 → −3,000 ⇒ (1, …) | 4,000, 0 → +4,000 ⇒ (0, 4000, 4000) | V1 (정차 4 = 만석) |
| 5 | R5 1,000 | 1,000 | 3,000, 3 → 0 ⇒ (0, 0, 3000) | 후보 없음(정차 한도) | V2 |
| 6~8 | R6~R8 | … | 예측 0 | 후보 없음 | V2 |

단계 1을 보자. 부피만 보는 best-fit이라면 V2가 "남은 부피 0"으로 완벽히 맞아 R1을 V2에 넣는다. 그러면 V1이 R2·R3·R4·R5로
정차 4를 다 쓰고 V2는 부피가 꽉 차서 R6·R7·R8 3건이 남는다. 부호 예산 키는 V2를 "정차가 3칸 남는데 부피가 0이니 −6,857
낭비"로 읽어 뒤로 미루고, 결국 8건 전량이 들어간다. 테스트가 확인하는 결과가 정확히 V1 = {R1..R4}, V2 = {R5..R8}이다.

### 4.4 1-1 교환 — `exchange`

존 내부 적재가 끝나면 그 존에서 못 넣은 요청(`zoneLeftovers`)을 **부피 큰 것부터** 한 번 더 시도한다.

```java
for (RequestId requestId : sortedByVolumeDesc(problem, zoneLeftovers)) {
    InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
    Solution exchanged = exchange(problem, profile, current, bins, requestId);
    if (exchanged != null) {
        current = exchanged;              // 성공 = bank −1
    } else {
        leftovers.add(requestId);         // 이 존에서는 포기 → 나중에 ④에서 재시도
    }
}
```

`exchange`의 탐색 순서:

```java
private static Solution exchange(Problem problem, Profile profile, Solution current, List<VehicleId> bins, RequestId r) {
    long volume = problem.request(r).totalVolume();
    for (VehicleId vehicleId : bins) {                                   // ① 존의 경로(bins 순서)마다
        List<RequestId> visitors = ...;                                  //    그 경로에 실린 요청 중 volume(q) < volume(r)인 q,
        visitors.sort(부피 ASC, RequestId ASC);                          //    작은 것부터
        for (RequestId q : visitors) {
            Solution without = InsertionSearch.remove(problem, current, q);          // ② q를 뺀다
            List<Candidate> forR = InsertionSearch.candidatesFor(problem, profile, without, r, vehicleId);
            if (forR.isEmpty()) continue;                                            // ③ r이 q 자리(같은 차)에 들어가나?
            Solution withR = InsertionSearch.apply(problem, without, forR.getFirst());
            for (VehicleId other : bins) {                                           // ④ q가 존의 다른 차에 들어가나?
                if (other.equals(vehicleId)) continue;
                List<Candidate> forQ = InsertionSearch.candidatesFor(problem, profile, withR, q, other);
                if (!forQ.isEmpty()) {
                    return InsertionSearch.apply(problem, withR, forQ.getFirst());   // ⑤ 첫 성공에서 즉시 반환
                }
            }
        }
    }
    return null;                                                                     // 어떤 조합도 안 됨
}
```

읽는 요령:

- **왜 "더 작은" q만 빼는가**: r이 못 들어간 이유는 대개 부피·정차가 아슬아슬하게 모자라서다. r보다 큰 q를 빼면 r은 들어가지만
  q가 갈 곳이 더 없다. 작은 q를 빼면 q는 다른 경로의 자투리에 들어갈 가능성이 높다. 그래서 후보 q를 **부피 오름차순**으로
  본다 — 가장 옮기기 쉬운 것부터.
- **r은 반드시 q가 있던 차량에** 들어간다(③). q는 **같은 존의 다른 차량**으로만 옮긴다(④). 존 밖으로 나가지 않으니 `ZONE_MIX`를
  건드리지 않고, DP가 정한 존 배정도 유지된다.
- **첫 성공에서 멈춘다**. 최적 교환을 찾지 않는다. 성공하면 bank가 1 줄어들고, 실패한 r은 `leftovers`로 넘어가서 존 밖의
  차량(미사용 차량, 다른 존의 경로 중 호환되는 것)에 마지막으로 시도된다.
- `visitors`는 `List.contains`로 중복을 거른다 — `PICKUP_DELIVERY`면 한 요청이 노드 2개로 나타나기 때문이다.
- 이 단계는 오라클 호출이 많다: leftover 1건에 최악 (존 차량 수 × 경로 길이 × 존 차량 수)번의 `candidatesFor`. 실물에서
  leftover가 몇 건 안 되니 감당된다(전체 507 ms).

### 4.5 leftover 수집과 leftover pass

```java
for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {
    if (current.bank().contains(requestId) && !leftovers.contains(requestId)) {
        leftovers.add(requestId);                 // 존에 안 들어간 요청(호환 0대)도 합류
    }
}
for (RequestId requestId : requestOrder(problem, allocation, leftovers)) {
    InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
    List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, requestId);
    if (!cands.isEmpty()) {
        current = InsertionSearch.apply(problem, current, cands.getFirst());
    }
}
return current;
```

- **수집**: 존 처리가 끝난 뒤 bank에 남아 있으면서 아직 `leftovers`에 없는 요청은 딱 한 부류다 — **호환 차량이 0대라 처음부터
  존에 들어가지 못한 요청**. 이들은 `candidates()`가 항상 빈 목록을 내니 결국 bank에 남지만, 코드는 그 사실을 미리 가정하지
  않고 같은 경로로 흘려보낸다(추측하지 않는 스타일). 정렬은 어차피 다음 줄 `requestOrder`가 다시 하므로 여기서의 순서는
  무의미하다.
- **leftover pass**: 이제는 존 제한을 풀고 `candidates()`를 쓴다 — **기존 경로 전부(호환되면) + 미사용 호환 차량 중 ID가 가장
  앞선 1대**의 새 경로. 후보 중 비용 최선에 넣는다. 다른 존의 경로에 넣으려 하면 `ZONE_MIX` 때문에 전파가 거부하니 실제로 열리는
  문은 대개 "DP가 배정하지 않은 차량으로 새 경로"와 "`(none)` 존 요청을 아무 경로에나"다. 여기서도 못 넣으면 bank 그대로다
  — 유효한 해다.

---

## 5. 결정성 · 종료 · 복잡도

### 5.1 결정성 — 같은 입력이면 같은 출력

| 지점 | 순서를 고정하는 방법 |
|---|---|
| 존 순서 | DP 안에서 (호환 유형 수, Σ부피 DESC, zoneId ASC) |
| bins 순서 | DP가 소비한 순서 = 유형 순서(maxVolume ASC…) × 유형 안 VehicleId ASC |
| 요청 순서 | `requestOrder` — 끝이 `BY_REQUEST_ID` |
| bin 선택 동률 | `compare(key, chosenKey) < 0`일 때만 교체 → bins 순서의 앞이 남음 |
| 오라클 후보 동률 | `InsertionSearch.sorted`가 (비용, VehicleId, 위치) 순 |
| 교환 순서 | bins 순서 × (부피 ASC, RequestId ASC) × bins 순서 |
| `loads` | `LinkedHashMap` — 순회에 쓰지는 않지만 삽입 순서 보존 |

`HashSet`/`HashMap` 순회로 승자를 고르는 곳이 없다. `double` 키는 §4.2 규칙대로 정렬에만 쓴다.

### 5.2 종료 — 시간이 아니라 구조로

- DP: 상태 수 ≤ Π(대수+1) ≤ 65,536 고정. 기권이 이를 보장한다.
- 존 내부 적재: 존 요청 수만큼 정확히 반복 → 합쳐서 ≤ n.
- 교환: zoneLeftover마다 1회 → ≤ n.
- leftover pass: ≤ n.
- 합 ≤ 3n = `bound`. 넘으면 `IllegalStateException` — 품질 문제가 아니라 버그로 취급한다.

### 5.3 복잡도 (규범 문서 §5 표)

```text
배정 DP   O(Z · S · F · G)    Z = 존 수, S ≤ Π(c_t+1) 상태, F ≤ S 프론티어, G = 호환 그룹 수
존 적재   O(m_z · L²)         존 요청마다 bin 수 m_z × (위치 L × 전파 L)  — PICKUP_DELIVERY는 위치가 L²
```

실물 fixture(452건·31대·유형 6종·조합 25,920)에서 전체 507 ms. 초기해 24개 중 무거운 축이지만(H3 136 ms),
regret 계열[^regret](H1·H2)보다는 싸다.

---

## 6. 코드를 읽으며 눈여겨볼 점

규범 문서와 코드는 1:1이지만, 처음 읽을 때 헷갈리기 쉬운 곳과 사소한 어긋남을 적어 둔다. **수정 제안이 아니라 읽기 보조**다.

1. **`loads`는 근사 장부다.** 오라클이 판정한 실제 경로 상태와 같은 값을 유지하지만(삽입할 때마다 `add`), 교환 단계에서는
   갱신하지 않는다. 교환은 `loads`를 읽지 않으므로 문제없다 — 다만 "존 적재 루프 밖에서는 믿지 말 것".
2. **`stopsLeft`는 `visitCount(r)`을 뺀다.** 규범 의사코드는 `B_v − 정차 수 − 1`이라 적었지만, 코드는 `PICKUP_DELIVERY`(방문 2)를
   고려해 1 또는 2를 뺀다. 의사코드가 단일 방문 패턴을 예로 쓴 것이고 코드가 일반형이다.
3. **bins 순서는 "전역 VehicleId ASC"가 아니다.** 코드 주석(64행)과 규범 의사코드의 교환 절("VehicleId ASC")은 `VehicleId` 순처럼
   읽히지만, 실제 `bins`는 DP가 소비한 순서 — **유형 순서(작은 차부터) 안에서** VehicleId ASC — 다. 예: T40에서 bins = [V2, V1].
   결정적이긴 마찬가지라 결과 재현에는 영향이 없고, "동률이면 작은 차 유형이 먼저"라는 뜻이 된다.
4. **평균에는 앞으로 leftover가 될 요청도 들어간다.** `meanVolume`은 "아직 처리 안 한 존 요청 전부"의 평균이라, 결국 못 들어갈
   요청까지 포함한다. 추정치이므로 감수한다.
5. **`leftovers.contains`는 O(n)이라 수집 루프가 O(n²)**이지만 n = 452에서 무시할 수준이다.
6. **`Route`는 비어 있을 수 없다.** 교환의 `remove`가 마지막 요청을 빼면 그 `Route`가 사라진다. 그 뒤 `candidatesFor(…, vehicleId)`는
   `visitsOf`가 빈 목록을 돌려주니 "새 경로" 후보로 취급된다 — 자연스럽게 이어진다.
7. **`Problem`은 어디서도 바뀌지 않는다.** `Solution`도 불변이라 `current = apply(...)`로 갱신한다. 교환에서 실패한 시도의
   `without`·`withR`은 그냥 버려진다 — 되돌리기 코드가 없는 이유다.

---

## 7. 경계 상황 (규범 §7의 X번호)

| 상황 | 동작 | 번호 |
|---|---|---|
| 유형 조합 수 > 65,536 | 기권. 예외 아님 | X20 |
| 공급 < 수요라 어떤 존도 못 덮음 | DP는 빈 집합(c) 전이가 항상 있어 완주. 그 존의 bins가 비면 적재 루프에서 후보 0개 → 전부 zoneLeftover → 교환 대상 없음 → leftover pass가 미사용 차량으로 시도 | X21 |
| 정차 한도 부재 | 키가 부피 best-fit으로 퇴화. 기권하지 않음 | X23 |
| 호환 차량 0대인 요청 | 존에 안 들어감 → §4.5 수집 → `candidates()` 빈 목록 → bank | X2 |
| `zoneId`가 하나도 없음 | `"(none)"` 존 하나로 전체를 다룬다. 기권 사유가 아님 | §4.4 |
| 존에 bins가 1대뿐 | 교환은 "다른 경로"가 없어 항상 실패(④ 루프가 빈 루프) → leftover pass로 | — |

---

## 8. 테스트와 실측

| 테스트 | 무엇을 고정하나 |
|---|---|
| T40 `ZoneQuotaBalancedFillConstructionTest.signedBudgetAvoidsWastingStops` | §4.3 워크스루 그대로 — 부피 best-fit은 3건을 놓치지만 부호 예산 규칙은 전량 배정 |
| T38 `ZoneQuotaAllocationTest.hallConditionKeepsBigVehiclesOffRestrictedZones` | DP의 Hall 조건 — 큰 차가 큰 차 금지 존에 배정되지 않음 |
| T39 `ZoneQuotaAllocationTest.combinationsBoundAbstains` | 17종 제각각 → 기권, 같은 유형 31대 → 실행 |
| T44 `WinPocFixtureTest` (app 모듈) | 실물 fixture 452건에서 H23이 bank 0·31경로·Feasible·재실행 동일 score·H3보다 우위. 경로마다 구역 1종·차급·부피·무게·정차 28·시간창 감사 |

실물 실측(2026-09-02, 정식 평가):

| 기법 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 |
|---|---:|---:|---:|---:|---:|
| H3 `vehicle-zone-fill` (종전 1위) | 15 | 31 | 4,515,433 | 1,003,093 | 136 ms |
| **H23 `zone-quota-balanced-fill`** | **0** | 31 | 4,198,408 | 1,002,069 | 507 ms |
| H24 `zone-quota-subset-fill` | 8 | 31 | 3,942,905 | 974,992 | 773 ms |

실행 명령:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env
mvn test -pl solver-core -Dtest='ZoneQuotaBalancedFillConstructionTest,ZoneQuotaAllocationTest'
mvn test -pl app -Dtest=WinPocFixtureTest
```

---

## 용어 각주

[^cvrptw]: **CVRPTW** (Capacitated Vehicle Routing Problem with Time Windows) — 용량 제한과 시간창이 있는 차량 경로 문제.
    이 저장소가 푸는 **RPDPTW**는 거기에 pickup-and-delivery(한 주문이 싣는 곳과 내리는 곳 두 방문을 가짐)와 실무 제약
    (차급·구역·정차 한도·근무창 등)을 더한 "rich" 변형이다.

[^construction]: **construction (heuristic)** — 빈 해에서 시작해 요청을 하나씩 넣어 첫 해를 만드는 기법. 이 뒤에 오는 ALNS
    (Adaptive Large Neighborhood Search)가 그 해를 부수고 다시 고치며 개선한다. 이 저장소에서는 construction이 24개이고
    난수를 쓰지 않는다.

[^vehicle-class]: **차급** — 차량의 톤수 등급(T1, T1.9, T2.5, T3.5, T5 등). 주문마다 "이 차급까지만 진입 가능"이 지정될 수 있어
    (`allowedVehicleFeatures`), 그 주문과 차량의 **호환** 여부를 가른다. 호환은 `Problem`이 동결 시점에 미리 계산해
    `compatibleVehicles(requestId)`로 제공한다 — 차급·capability·구역·차고 조건을 모두 통과한 차량 집합.

[^initial-solution]: **초기해** — ALNS가 출발점으로 삼는 첫 `Solution`. 여기서는 24개 construction 결과 중 정식 평가 점수가 가장 좋은 것.

[^lexicographic-score]: **사전식 점수** — `long[]`을 앞 원소부터 비교하는 순서. 기본 profile(`DefaultProfile`)의 축은
    **(미배정 수, 사용 차량 수, 총 거리 m, 총 운행시간 s)** 네 개이고, 앞 축이 같을 때만 뒤 축을 본다. 가중합으로 뭉개지 않는다.
    그래서 "미배정 0"이 거리보다 항상 우선한다. hard 위반은 점수 축이 아니다 — 위반이 있는 해는 평가가 Infeasible로 따로 표시하고,
    construction 결과는 오라클 덕에 애초에 Infeasible일 수 없다.

[^determinism]: **결정적(deterministic)** — 같은 입력에 항상 같은 출력. 이 저장소는 재검증(별도 코드가 해를 처음부터 다시 계산해
    확인)과 재현 가능한 벤치마크를 위해 초기해 단계에서 난수를 금지한다.

[^bank]: **bank** — 어떤 경로에도 들어가지 못한 요청 ID의 집합. 모든 요청은 "경로 안" 또는 "bank" 중 정확히 한 곳에 있다(XOR).
    bank에 요청이 남은 해도 유효한 해다 — 다만 점수의 미배정 축이 나쁠 뿐이다.

[^problem]: **`Problem`** — 입력(요청·차량·차고·이동표)을 검증·정규화하고 호환표까지 계산해 **동결**한 객체. 탐색은 읽기만 한다.

[^request]: **`Request`** — 주문 하나. 패턴은 `PICKUP_DELIVERY`(방문 2개), `DELIVERY_ONLY`(차고에서 싣고 내리기만), `PICKUP_ONLY`
    (싣기만 하고 차고 도착에서 하차) 셋 중 하나. 배정·제거의 원자 단위라 pickup만 따로 옮기는 일은 없다.

[^profile]: **`Profile`** — 고객별로 다른 hard/soft 제약과 점수 축을 담은 객체. core 코드에 `if (customerId == …)`를 두지 않고
    이 객체를 갈아 끼우는 방식으로 고객 차이를 다룬다. 탐색과 재검증에 같은 인스턴스를 쓴다.

[^anchor]: **anchor side** — 요청의 첫 방문 쪽. pickup이 있으면 pickup, 없으면 delivery. 요청을 존에 배치할 때 "어느 구역 주문인가"의
    대표 좌표로 쓴다.

[^hall]: **Hall 조건** — 이분 매칭 이론의 조건("어떤 부분집합을 잡아도 그 이웃이 더 많다")을 빌린 이름. 여기서는 "호환 차량 종류가
    적은 요청 그룹부터 누적해 갔을 때, 각 누적 단계마다 그 그룹들이 탈 수 있는 차량만으로 부피·무게·방문 수를 감당하는가"를
    검사한다. 이게 없으면 DP가 부피 합만 보고 큰 차를 큰 차 금지 존에 배정한다(T38).

[^oracle]: **오라클** — 최적화 문헌에서 "이 후보가 유효한가·비용이 얼마인가"를 정확히 답해 주는 검사기를 부르는 말. 여기서는
    `InsertionSearch`가 그 역할이다 — 삽입 후보를 경로 전체 재전파 + profile hard로 검증해 통과한 것만 돌려준다. H23은
    유효성 판정을 여기에 전부 위임하고 스스로는 "어느 bin이 좋은가"만 정한다.

[^bin-packing]: **bin packing** — 크기가 다른 아이템들을 용량이 정해진 통(bin)에 최소 개수로 담는 문제. 차량 = bin, 주문 = 아이템으로
    보면 존 내부 적재가 곧 이 문제다. 유명한 근사법이 First-Fit-Decreasing(큰 것부터 들어가는 첫 bin에)과
    Best-Fit-Decreasing(큰 것부터 가장 꽉 차는 bin에)이다.

[^best-fit]: **best-fit** — 아이템이 들어갈 수 있는 bin 중 **남는 공간이 가장 적어지는** bin을 고르는 규칙. H23의 키는 이 규칙을
    부피 하나가 아니라 "부피 − 평균 × 남은 정차"라는 합성 축에 적용하고, 그 부호로 정차 낭비를 먼저 걸러낸다.

[^units]: **단위** — 무게·부피는 원래 값에 ×1000을 곱해 내림(FLOOR)한 `long`, 거리는 meter, 시간은 초 정수다. `double` 근사를
    거치지 않는다. 그래서 테스트의 부피 4,000은 "4 단위(CBM 등)"를 뜻한다.

[^dp]: **DP (dynamic programming, 동적 계획법)** — 문제를 "상태 → 다음 상태" 전이로 쪼개고, 상태마다 최선값 하나만 기억해
    같은 계산을 반복하지 않는 기법. 여기서 상태는 "아직 안 쓴 차량이 유형별로 몇 대 남았나"이고, 존을 하나 처리할 때마다 한 층
    내려간다. greedy와 달리 앞 존의 선택이 뒤 존을 굶기는지 전역으로 따진다.

[^fixture]: **fixture** — 테스트·실측에 쓰는 고정 입력 데이터. 이 저장소의 "실물 fixture"는 `data/win_poc_case_floor.json`
    (실제 고객 데이터, 주문 452건·차량 31대)이다. 기존 엔진(Win)의 결과 `data/alns_result.csv`와 비교하는 기준이다.

[^zone-mix]: **`ZONE_MIX`** — "한 경로가 두 종류 이상의 구체 구역을 방문했다"는 hard 위반 코드. 구역 `zoneId`가 없거나 `"ALL"`인
    방문은 세지 않는다. 예: 서울 → ALL → 서울은 가능, 서울 → ALL → 경기는 불가. 전파(`RoutePropagator`)가 판정하므로 H23은
    따로 검사하지 않는다.

[^greedy]: **greedy(탐욕법)** — 매 단계에서 지금 당장 가장 좋아 보이는 선택을 하고 되돌리지 않는 방식. 빠르지만 앞 선택이 뒤를
    망칠 수 있다. H23은 존 배정에는 DP를, 존 내부 적재에는 greedy를 쓴다.

[^h3]: **H3 `vehicle-zone-fill`** — 차량을 한 대씩 잡고 "이 차에 가장 잘 맞는 존"을 골라 채우는 기법(H23 이전의 실물 1위).
    차량 단위로 존을 고르다 보니 존이 여러 차에 조각나고, 뒤에 남은 작은 차들이 반만 찬다 — 실물 15 미배정의 원인이다.

[^spi]: **SPI (Service Provider Interface)** — 구현체를 여러 개 꽂아 넣을 수 있게 만든 인터페이스. `ConstructionHeuristic`을
    구현한 24개 클래스가 `InitialSolutionBuilder`에 목록으로 등록된다.

[^hard-soft]: **hard / soft 제약** — hard는 어기면 해가 무효인 제약(용량·시간창·정차 한도·구역 단일성·차급 등), soft는 어겨도
    되지만 점수가 나빠지는 것. 이 저장소에서 construction과 ALNS의 삽입은 hard를 전부 통과한 후보만 받는다.

[^stop-limit]: **정차 한도 (`effectiveMaxStopCount`)** — 한 경로가 방문할 수 있는 최대 정차 수. 차량별 값이 있으면 그 값, 없으면
    전역값, 둘 다 없으면 제약 없음(optional 비어 있음). 소량 주문이 많은 존에서는 부피보다 이 한도가 먼저 찬다 —
    H23의 키가 이 축을 부피와 함께 보는 이유다.

[^frontier]: **프론티어** — DP에서 한 존에 대해 실제로 시도해 볼 만한 차량 조합만 추린 집합. 모든 조합을 다 보지 않고 (a) 딱 덮는
    최소 조합 (b) 한 대만 더 있으면 덮는 최대 조합 (c) 아무것도 안 줌, 세 부류만 본다. (a)와 (c) 사이의 조합은 낭비만 늘리거나
    부족만 늘리므로 볼 이유가 없다.

[^capability]: **capability** — 주문이 요구하고 차량이 갖추는 기능 태그(냉장·리프트 등). 주문의 `requiredCapabilities`를 차량의
    `capabilities`가 전부 포함해야 호환이다.

[^propagation]: **전파 (`RoutePropagator`)** — 경로의 방문 순서가 주어졌을 때 차고 출발부터 순서대로 도착·서비스 시작·출발 시각,
    적재량, 정차 수 등을 계산하고 hard 제약을 검사해 `Feasible`(사실값 포함) 또는 `Infeasible`(위반 코드)로 답하는 부품.
    삽입 후보 하나를 검증할 때마다 그 경로 전체를 처음부터 다시 전파한다 — 캐시 없이.

[^tx]: **T번호 · X번호** — heuristics 문서의 표 번호. T는 테스트(§8 표, `T40` = `signedBudgetAvoidsWastingStops`), X는 경계
    상황(§7 표, `X23` = 정차 한도 부재). 코드 주석과 이 노트가 같은 번호로 가리킨다.

[^cbm]: **CBM (cubic meter)** — 부피 단위 m³. 실물 데이터의 부피가 이 단위이고, 코드에서는 ×1000한 정수로 든다(각주 [^units]).

[^regret]: **regret 삽입** — 요청마다 "최선 위치와 차선 위치의 비용 차(regret)"를 계산해 그 차가 큰 요청(지금 안 넣으면 나중에
    크게 손해 보는 요청)부터 넣는 기법. H1·H2가 이 계열이다. 매 단계 bank 전체를 다시 계산하므로 비싸다.
