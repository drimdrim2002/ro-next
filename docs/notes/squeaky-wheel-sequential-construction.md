# `SqueakyWheelSequentialConstruction` (H22) 코드 해설

> **성격**: 코드 해설 노트(비규범). 쉬운 말 설명은 [h1–h24 노트 §6.1](initial-solution-heuristics-h1-h24.md)이고,
> 규범은 [heuristics 문서 §5 H22](../implementation/stage-04-initial-solution-heuristics.md)다. 둘과 이 노트가 어긋나면 그쪽이 이긴다.
>
> **대상 독자**: CVRPTW[^cvrptw]를 아는 Java 개발자. 이 저장소의 용어(`Request`·`Problem`·bank 등)는 처음 본다고 가정하고,
> 처음 나올 때마다 각주로 풀었다. 각주는 문서 끝 [용어 각주](#용어-각주)에 모여 있다.
>
> **대상 파일**: `solver-core/src/main/java/com/ronext/rpdptw/solve/SqueakyWheelSequentialConstruction.java` (94줄, 2026-09-02 기준).
> 한 라운드의 삽입은 `DeadlineSequentialConstruction.java` (66줄, 같은 날짜)의
> `construct(problem, profile, order, Candidate.byCost(), ID)` 를 그대로 부른다.

---

## 0. 한 줄 요약

**"마감 임박 순으로 한 판 짜고, 못 실린 요청과 방문이 적은 경로의 요청에 숫자를 올려 순서를 바꾼 뒤 최대 5판을 다시 짜서, 사전식 점수[^lexicographic-score]가 가장 좋은 판을 남긴다."**

이름은 "squeaky wheel gets the grease"(삐걱대는 바퀴에 기름을 친다)에서 왔다. 아래는 그 속담이 아니라 **코드가 실제로 더하는 숫자**다.

| 이름 조각 | 뜻 |
|---|---|
| `squeaky-wheel` | 직전 해에서 문제가 된 요청의 `priority`를 올린다 — `blame`이 담당 |
| `sequential` | 올려 둔 순서로 **앞에서부터 하나씩** 가장 싼 자리에 넣는다 — H4와 같은 순차 삽입 |
| `Construction` | 초기해 구축 기법[^construction] — 빈 해에서 시작해 요청을 하나씩 넣는다 |

H1의 희소도(정적 호환 차량 수)와 regret[^regret] 삽입은 입력을 읽거나 현재 후보 차를 계산해 순서를 정한다. 이 기법은 둘 다 쓰지 않는다. **한 번 구성한 `Solution`을 보고** `priority` 벡터를 바꾼 다음, 그 벡터로 처음부터 다시 구성한다. 씨앗[^seed]을 따로 고르지도 않는다 — 새 경로는 `byCost`가 기존 자리에 못 넣을 때 미사용 호환 1대를 열 뿐이다. 난수는 없고 라운드 수 `R = 5`는 자바 상수라 종료가 구조로 보장된다.

---

## 1. 프로젝트 안에서의 위치

### 1.1 초기해 포트폴리오의 한 칸

이 저장소의 초기해[^initial-solution]는 하나가 아니다. `InitialSolutionBuilder.build(problem, profile)`가 결정적 construction
**24개**(H1~H24)를 전부 돌려 각 결과를 정식 평가(`Evaluator`)에 넣고, 사전식 점수가 가장 좋은 하나를 고른다.
H22는 그 24개 중 22번째이고, 실물 fixture[^fixture]에서는 **4위**(미배정 47, §8).

```text
InitialSolutionBuilder.build(problem, profile)
  ├─ H1 … H21
  ├─ H22 SqueakyWheelSequentialConstruction   ← 이 문서
  ├─ H23 ZoneQuotaBalancedFillConstruction
  └─ H24 ZoneQuotaSubsetFillConstruction
  → 각 Solution을 Evaluator로 평가 → 사전식 최선 1개
```

포트폴리오 축으로는 **"무가정 자기 교정"**의 대표다 ([h1–h24 §4.2](initial-solution-heuristics-h1-h24.md)).
구역·좌표·차고 수·주문 유형을 전제로 두지 않고, 못 실린 요청에 숫자를 올리는 규칙만으로 순서를 고친다.
1라운드는 H4와 같은 실행이라 H4를 슬롯에 따로 넣을 필요가 없다(§4.2).

### 1.2 구현하는 인터페이스 — `ConstructionHeuristic` (SPI[^spi])

```java
public interface ConstructionHeuristic {
    String id();                                   // "squeaky-wheel-sequential"
    boolean abstains(Problem problem);             // 이 기법은 항상 false
    Solution construct(Problem problem, Profile profile);   // 빈 해에서 시작해 Solution 하나
}
```

세 가지 계약이 이 파일 전체를 지배한다.

1. **난수 없음, 결정적**[^determinism]. 같은 `Problem`이면 언제 어디서 돌려도 같은 `Solution`이 나와야 한다.
   `priority`의 키 순서는 `InsertionSearch.sortedRequestIds`(문자열 ID 오름차순)이고, 삽입 동률은 `Candidate.byCost` 다음
   `VehicleId` 문자열이다. `HashMap` 순회로 승자를 고르지 않는다.
2. **반환 해는 항상 유효**하다. 넣지 못한 요청은 bank[^bank]에 남기면 되고, 경로에 들어간 요청은 전부 시간창·용량·정차 한도 등
   hard 제약[^hard-soft]을 통과한 상태다. 삽입을 `InsertionSearch`를 통해서만 하기 때문에 자동으로 따라온다.
3. **기권(abstain)은 없다.** `abstains`는 `false`를 반환한다 — 규범 §4.4, 기본 24개 중 H1·H2·H3·H4·H6와 확장 12개(H9~H18·H21·H22)가
   어떤 `Problem`에서도 기권하지 않는다.

### 1.3 다루는 자료형 요약

| 타입 | 이 파일에서의 뜻 |
|---|---|
| `Problem`[^problem] | 동결된 입력 전체. 요청·차량·이동표·호환표. 탐색은 절대 고치지 않는다 |
| `Request`[^request] | 배차의 원자 단위(pair). pickup·delivery side 중 있는 것들, `totalVolume`·`totalWeight` |
| `RequestId` | 요청의 문자열 ID. 정렬·`priority` 맵의 키 |
| `Solution` | `List<Route>` + bank(`Set<RequestId>`). 불변 record — 삽입할 때마다 새 객체 |
| `Route` | `VehicleId` + 방문 노드 목록. **빈 경로는 존재하지 않는다**(생성자가 막음) |
| `Profile`[^profile] | 고객별 hard/soft 제약 묶음. 이 파일은 그대로 삽입·평가에 넘기기만 한다 |
| `InsertionSearch.Candidate` | "이 차량의 이 위치에 넣으면 이런 방문 순서가 되고 비용이 이만큼 는다"는 삽입 후보 |
| `Map<RequestId, Integer> priority` | 요청마다 누적된 정수. 0에서 시작, 라운드마다 `blame`이 더한다 |

---

## 2. 기대는 부품

이 파일은 **"어떤 순서로 넣을지"와 "방금 짠 해를 보고 숫자를 얼마 올릴지"**만 정한다.
한 라운드의 삽입은 `DeadlineSequentialConstruction.construct`에, "넣을 수 있는가·어느 위치가 싼가"는
`InsertionSearch`에, 라운드 점수는 `Evaluator`에 맡긴다.

### 2.1 `DeadlineSequentialConstruction.construct` — 한 라운드 = H4

H4 본체의 진입점은 마감 순서를 직접 만들어 아래 오버로드를 부른다.

```java
// DeadlineSequentialConstruction.java:28-31
public Solution construct(Problem problem, Profile profile) {
    return construct(problem, profile, deadlineOrder(problem), Candidate.byCost(), ID);
}
```

H22가 부르는 것은 **같은 오버로드**다. 순서 목록만 `priority` 내림차순으로 갈아 끼운다.

```java
// SqueakyWheelSequentialConstruction.java:52
Solution solution = DeadlineSequentialConstruction.construct(problem, profile, order, Candidate.byCost(), ID);
```

공유 메서드 본체 (`DeadlineSequentialConstruction.java` 34–48행):

```java
static Solution construct(
        Problem problem, Profile profile, List<RequestId> order, Comparator<Candidate> position, String id) {
    Solution current = InsertionSearch.emptySolution(problem);
    int iterations = 0;
    for (RequestId requestId : order) {
        InsertionSearch.checkOuterLoop(id, ++iterations, order.size());
        List<Candidate> cands = InsertionSearch.candidates(problem, profile, current, requestId);
        if (cands.isEmpty()) {
            continue;                                                       // 규칙 (b) — bank에 남는다
        }
        List<Candidate> ranked = new ArrayList<>(cands);
        ranked.sort(position);                                              // 안정 정렬 — 동률은 §4.1 순서 유지
        current = InsertionSearch.apply(problem, current, ranked.getFirst()); // 규칙 (a)
    }
    return current;
}
```

읽는 요령:

- 시작점은 **전 요청이 bank인 빈 해**. 라운드마다 직전 해를 고치지 않고 **처음부터** 다시 넣는다.
- 요청 하나마다 후보가 있으면 비용 최선 위치에 넣고(규칙 a), 없으면 그 요청을 건너뛴다(규칙 b, bank 잔류).
  어느 쪽이든 `order`를 한 칸 소비하므로 루프는 정확히 `n`회에서 끝난다.
- `position`에 H22는 `Candidate.byCost()`를 넘긴다. H4도 같다. H13만 다른 비교자(`POSITION_ORDER`, 여유 손실)를 넘긴다.
- `id`는 방어 카운터 메시지용이다. H22는 `"squeaky-wheel-sequential"`을 넘기므로, 상한을 넘기면 예외 문자열만 H4와 다르고
  **해의 내용은 같다**.

**1라운드가 H4와 같은 실행인 이유.** 모든 `priority`가 0이면 정렬 키의 첫째 칸이 전원 동률이고, 둘째 칸이
`DeadlineSequentialConstruction.deadlineComparator`다. 그 비교자는 H4의 `deadlineOrder`와 동일하다
(있는 delivery의 마지막 창 `closeSec` 오름차순, 없으면 pickup의, 그다음 `RequestId` 오름차순).
이어서 `Candidate.byCost()`로 같은 오버로드를 부르므로, 1라운드 `Solution`은 H4의 `Solution`과 같다.

마감 시각은 이 한 줄이다.

```java
// DeadlineSequentialConstruction.java:63-65
static long deadlineSec(Request request) {
    return InsertionSearch.lastCloseSec(InsertionSearch.lastSide(request));
}
```

`lastSide` = delivery가 있으면 delivery, 없으면 pickup. `lastCloseSec` = 그 side 창 목록의 **마지막** 창 닫힘.
창이 하나면 그 값이 곧 마감이다.

### 2.2 `InsertionSearch` — 삽입 오라클[^oracle]

H22(와 그것이 부르는 H4 오버로드, 그리고 `blame`)가 쓰는 메서드는 아래 표가 전부다.
`candidatesFor`·`remove`·`Cache`는 이 기법이 부르지 않는다.

| 메서드 | 하는 일 | H22에서 쓰는 곳 |
|---|---|---|
| `emptySolution(problem)` | 전 요청이 bank인 빈 해 | 라운드 시작 (`construct` 36행) |
| `candidates(problem, profile, current, r)` | **모든 기존 경로 + 미사용 호환 차량 1대**에 대해 넣을 수 있는 위치를 비용 오름차순으로. 없으면 빈 목록 | 요청 하나마다 |
| `apply(problem, current, candidate)` | 후보를 적용한 **새** `Solution`. 그 요청은 bank에서 빠진다 | 후보가 있을 때 |
| `Candidate.byCost()` | `(새 경로 여부, Δ거리 m, Δ운행시간 s)` 사전식 오름차순 | 위치 고르기 — H4와 동일 |
| `checkOuterLoop(id, n, bound)` | 바깥 루프 방어 카운터. 상한을 넘으면 `IllegalStateException` | 요청마다 1회, `bound = order.size()` |
| `sortedRequestIds(problem)` | 전 요청 ID를 문자열 오름차순 | `priority` 맵 초기화 |
| `requestOf(problem, nodeId)` | 방문 노드 → 요청 ID | `blame`의 +1 |

`candidates`가 "넣을 수 있다"고 말하려면 세 관문을 다 통과해야 한다 — ① 호환 필터(차급·capability·구역·차고),
② `RoutePropagator`[^propagation]가 시간창·용량·정차 한도·구역 단일성 등을 경로 전체에 다시 전파해 Feasible,
③ profile의 hard 제약 전부 satisfied. H22는 용량이나 시간창을 스스로 검사하지 않는다.

기존 경로가 있으면 그 경로들을 먼저 보고, 미사용 호환 차량은 **문자열 ID가 가장 앞선 1대만** 새 경로 후보로 연다
(`firstUnusedCompatible`). 새 경로 후보는 `byCost`의 첫째 칸 `newRoute = true`라, 기존 경로에 들어가는 자리보다
항상 뒤다 — 차량 수가 점수 2번 축이기 때문이다 ([h1–h24 §6.0](initial-solution-heuristics-h1-h24.md)).

단일 방문 패턴(`DELIVERY_ONLY`·`PICKUP_ONLY`)에서 경로 방문 수가 `L`이면 삽입 슬롯은 `i = 0 … L`로 **`L + 1`곳**이다
(`InsertionSearch.java` 246–252행). 각 슬롯마다 경로 전체를 다시 전파하므로 그 차량 하나당 `O(L²)`.
`PICKUP_DELIVERY`는 `(i ≤ j)` 쌍이라 슬롯이 `O(L²)`, 전파까지 곱하면 `O(L³)`가 된다. 실물 fixture는 `DELIVERY_ONLY` 지배라
아래 숫자는 `L + 1` 기준이다.

### 2.3 `Evaluator` — 라운드마다 정식 평가

```java
// Evaluator.java:30-64 (축약)
public static EvaluationResult evaluate(Problem problem, Profile profile, Solution solution) {
    // 경로마다 RoutePropagator + profile hard
    Evaluation evaluation = Evaluation.aggregate(facts, solution.bank().size());
    long[] score = profile.score(problem, evaluation, facts);
    return new EvaluationResult.Feasible(evaluation, score, factsByVehicle);
}
```

기본 profile(`DefaultProfile`)의 축은 **(미배정 수, 사용 차량 수, 총 거리 m, 총 운행시간 s)** 네 칸이다.
비교는 `Scores.compare` — 앞 칸부터, 작은 쪽이 이기고, 앞 칸이 같을 때만 뒤 칸을 본다. 가중합은 없다.

H22는 매 라운드 이 점수로 `best`를 갱신한다. 동률(`compare == 0`)이면 **앞 라운드를 유지**한다
(`< 0`일 때만 교체, 57행). 1라운드가 H4와 같으므로, 이후 라운드가 엄격히 나아지지 않으면 반환 해는 H4와 같다.
H4보다 나빠질 수는 없다.

라운드 해가 `Feasible`이 아니면 `IllegalStateException`이다. 삽입 오라클과 평가기가 같은 세 관문을 쓰므로
논리적으로 도달하지 않아야 하고, 도달하면 품질 문제가 아니라 버그다(규범 X5).

---

## 3. 알고리즘 전체 흐름

```text
abstains?  항상 false

priority(r) = 0  (키 순서 = RequestId 문자열 오름차순)
best = null

round = 0 .. ROUNDS-1:          // ROUNDS = 5
  ① 순서   (priority DESC, 마감 ASC, RequestId ASC) 로 order를 만든다
  ② 구성   DeadlineSequentialConstruction.construct(..., order, byCost(), ID)
           = H4와 동일한 순차 최소 비용 삽입, 순서만 다름
  ③ 평가   Evaluator.evaluate → score
           Scores.compare(score, bestScore) < 0 이면 best 교체 (동률 = 앞 라운드)
  ④ blame  bank의 r           → priority(r) += 2
           방문 수 < 중앙값인 경로의 각 방문 노드 → 그 요청 += 1
  ⑤ 불변   next.equals(priority) 이면 break   // X18 — 이후 라운드도 같은 해
           아니면 priority = next, 다음 라운드

return best
```

바깥 루프는 라운드당 요청 `n`회 × 최대 5라운드 = `R · n`이다. 시간 상한은 없다(규범 §4.3).

---

## 4. 단계별 상세

### 4.1 상수와 진입점

```java
// SqueakyWheelSequentialConstruction.java:19-36
static final String ID = "squeaky-wheel-sequential";
/** 재량 상수 — 라운드 수. */
static final int ROUNDS = 5;

public boolean abstains(Problem problem) {
    return false;
}

public Solution construct(Problem problem, Profile profile) {
    return construct(problem, profile, new ArrayList<>());
}
```

`ROUNDS = 5`는 설정 파일이 아니라 **자바 상수**다. 규범 §1 표의 재량 항목("H22 라운드 5")과 같고,
H3의 적재율 0.85·H17의 seed 비율 0.25와 한 부류다. Stage 8 실험이 조정한다.

`priorityTrace` 오버로드는 테스트 T37이 라운드마다 스냅샷을 받기 위한 것이다. 본 진입점은 빈 목록을 넘긴다.

### 4.2 `priority` 초기화와 라운드 루프

```java
// 39-66행
Solution construct(Problem problem, Profile profile, List<Map<RequestId, Integer>> priorityTrace) {
    Map<RequestId, Integer> priority = new LinkedHashMap<>();
    for (RequestId requestId : InsertionSearch.sortedRequestIds(problem)) {
        priority.put(requestId, 0);
    }
    Comparator<RequestId> deadline = DeadlineSequentialConstruction.deadlineComparator(problem);
    Solution best = null;
    long[] bestScore = null;
    for (int round = 0; round < ROUNDS; round++) {
        priorityTrace.add(new LinkedHashMap<>(priority));
        List<RequestId> order = new ArrayList<>(priority.keySet());
        Map<RequestId, Integer> snapshot = priority;
        order.sort(Comparator.<RequestId>comparingInt(snapshot::get).reversed().thenComparing(deadline));
        Solution solution = DeadlineSequentialConstruction.construct(problem, profile, order, Candidate.byCost(), ID);
        EvaluationResult evaluated = Evaluator.evaluate(problem, profile, solution);
        if (!(evaluated instanceof EvaluationResult.Feasible feasible)) {
            throw new IllegalStateException(ID + " produced infeasible round: " + evaluated);
        }
        if (best == null || Scores.compare(feasible.score(), bestScore) < 0) {  // 동률이면 앞 라운드 유지
            best = solution;
            bestScore = feasible.score();
        }
        Map<RequestId, Integer> next = blame(problem, solution, priority);
        if (next.equals(priority)) {
            break;                                                          // 불변점 — 이후 라운드도 같은 해 (X18)
        }
        priority = next;
    }
    return best;
}
```

줄마다:

| 행 | 계산 |
|---:|---|
| 40–43 | `priority`를 `LinkedHashMap`으로 만들고 전 요청에 0. 키 삽입 순서 = `sortedRequestIds` = `RequestId` 문자열 오름차순 |
| 44 | 마감 비교자를 한 번만 만든다. `deadlineSec` ASC → `RequestId` ASC |
| 47 | `round`는 0부터 `ROUNDS - 1`까지. 최대 5회. `break`가 더 일찍 끝낼 수 있다 |
| 48 | T37용 스냅샷. 본 진입점의 빈 목록에는 부작용이 없다 |
| 49–51 | `order = 키 목록`을 **제자리 정렬**. 키 1 = `priority` 내림차순(`reversed`), 키 2 = 마감 비교자. 맵 자체는 정렬하지 않는다 |
| 52 | §2.1의 공유 `construct`. 이 한 줄이 한 라운드 |
| 53–56 | 정식 평가. Infeasible이면 예외 |
| 57–60 | `best == null`(1라운드)이거나 새 점수가 **엄격히 작으면** 교체. `Scores.compare`가 0이면 앞 라운드가 남는다 |
| 61–65 | `blame` 결과 맵이 직전과 `equals`(키·값 전부)이면 즉시 `break`. 그렇지 않으면 `priority`를 갈아 끼우고 다음 라운드 |
| 67 | `best`. 1라운드는 항상 들어가므로 `null`이 아니다 |

조기 종료가 비교하는 것은 **해가 아니라 `priority` 벡터**다. 해가 같아도 벡터가 바뀌면 다음 라운드를 돈다(§4.4 워크스루, §6 2번).

### 4.3 `blame` — +2 / +1 과 중앙값

```java
// 70-93행
static Map<RequestId, Integer> blame(Problem problem, Solution solution, Map<RequestId, Integer> priority) {
    Map<RequestId, Integer> next = new LinkedHashMap<>(priority);
    for (RequestId requestId : solution.bank()) {
        next.merge(requestId, 2, Integer::sum);
    }
    List<Integer> sizes = new ArrayList<>();
    for (Route route : solution.routes()) {
        sizes.add(route.visits().size());
    }
    if (sizes.isEmpty()) {
        return next;
    }
    sizes.sort(Comparator.naturalOrder());
    int median = sizes.get(sizes.size() / 2);
    for (Route route : solution.routes()) {
        if (route.visits().size() < median) {
            for (NodeId nodeId : route.visits()) {
                next.merge(InsertionSearch.requestOf(problem, nodeId), 1, Integer::sum);
            }
        }
    }
    return next;
}
```

**+2 (bank).** `solution.bank()`의 각 ID에 `merge(..., 2, Integer::sum)` — 있던 값에 2를 더한다.
bank의 요청은 경로에 없으므로 +1 루프와 겹치지 않는다. 한 라운드에 bank 요청이 받는 증분은 정확히 +2.

**중앙값.** 경로마다 `visits().size()`를 `sizes`에 모은다. `Route` 생성자가 빈 방문을 막으므로 여기 들어가는 값은
전부 1 이상이다. 경로가 0개면(`sizes.isEmpty()` — 전 요청이 bank) +1을 적용하지 않고 +2만 적용한 `next`를 반환한다.

경로가 1개 이상이면 `sizes`를 오름차순 정렬한 뒤

```text
median = sizes.get(sizes.size() / 2)     // 정수 나눗셈, 0-based
```

짝수 개 경로에서는 **위쪽 중앙값**(두 가운데 중 큰 쪽)이다. 평균을 내지 않는다.

| 경로 수 | `size / 2` | 예 `sizes` (정렬 후) | `median` |
|---:|---:|---|---:|
| 1 | 0 | `[4]` | 4 |
| 2 | 1 | `[1, 3]` | **3** (위쪽) |
| 3 | 1 | `[1, 2, 4]` | 2 |
| 4 | 2 | `[1, 2, 4, 8]` | 4 |

`visits().size() < median`인 경로만 +1 대상이다. `== median`은 올리지 않는다. 경로가 하나뿐이면 `L < L`이 거짓이라
+1은 0건이다 — T37의 첫 fixture가 이 경우다.

**+1 (방문 노드마다).** 대상 경로의 `visits()`를 순회하며 `requestOf`로 요청 ID를 찾아 `merge(..., 1, Integer::sum)`.
`DELIVERY_ONLY`·`PICKUP_ONLY`는 방문 1개라 요청당 +1이다. `PICKUP_DELIVERY`는 같은 요청이 노드 2개로 나타나므로
그 경로에 실려 있으면 `merge`가 **두 번** 불린다. 워크스루(§4.4)는 실물과 같이 단일 방문으로 숫자를 맞춘다.

`LinkedHashMap`의 키 순서는 초기화 때 고정되고 `merge`는 값을 고칠 뿐 키를 다시 넣지 않는다. 다음 라운드 `order`를
만들 때 `keySet()`을 새 리스트에 복사한 뒤 정렬하므로, 맵 순회 순서가 삽입 순서를 바꾸지는 않는다.

### 4.4 워크스루 — 요청 5건 · 차량 2대

아래는 코드 경로를 숫자로 따라가기 위한 **가상 입력**이다. T37 fixture가 아니고, 거리·운행시간은 전제 값이다.
단위는 ×1000한 정수[^units]라 부피 3,000 = 3 단위. 패턴은 전부 `DELIVERY_ONLY`(방문 1). 정차 한도 없음.
시간창은 넓고 마감만 다르다. 차량 V1·V2의 `maxVolume = 10,000`. 호환은 전원.

| 요청 | `lastCloseSec` | 부피 |
|---|---:|---:|
| A | 100 | 3,000 |
| B | 200 | 3,000 |
| C | 300 | 3,000 |
| D | 400 | 4,000 |
| E | 500 | 7,000 |

총 부피 20,000 = 차량 용량 합. 조합 `(E, A)` + `(D, B, C)` = 10,000 + 10,000으로 전량 배정이 가능하지만,
마감순으로 넣으면 E가 자투리에 안 들어간다.

#### 라운드 1 — `priority` 전부 0 = H4

정렬 키 `(priority DESC, 마감 ASC, RequestId ASC)`:

```text
priority = {A:0, B:0, C:0, D:0, E:0}
order    = A, B, C, D, E
```

순차 최소 비용 삽입 (`byCost`: 기존 경로가 새 경로보다 앞):

| 단계 | 넣는 요청 | 후보 | 선택 | 경로 상태 |
|---:|---|---|---|---|
| 1 | A 3,000 | 새 경로 V1 | V1 | V1={A} 부피 3,000 방문 1 |
| 2 | B 3,000 | V1 기존 (슬롯 2곳) / 새 V2 | V1 (`newRoute=false`) | V1={A,B} 6,000 방문 2 |
| 3 | C 3,000 | V1 잔여 4,000에 들어감 | V1 | V1={A,B,C} 9,000 방문 3 |
| 4 | D 4,000 | V1 잔여 1,000 < 4,000 → 새 V2 | V2 | V2={D} 4,000 방문 1 |
| 5 | E 7,000 | V1 잔여 1,000, V2 잔여 6,000, 둘 다 < 7,000 | 후보 0개 → bank | bank={E} |

해: V1 방문 3, V2 방문 1, bank 1건.

**평가** (`DefaultProfile` 네 축). 전제: V1 거리 4,000 m · 운행 1,200 s, V2 거리 1,500 m · 운행 400 s.

```text
score₁ = (미배정 1, 차량 2, 거리 5,500, 운행 1,600)
best   = 이 해
```

**중앙값.** `sizes = [3, 1]` (routes 순회 순서). 정렬 `[1, 3]`. `size = 2`, `2 / 2 = 1`, `median = sizes.get(1) = 3`.
V2의 방문 1 < 3 → D에 +1. V1의 3 < 3 은 거짓. bank E에 +2.

```text
blame 후 priority = {A:0, B:0, C:0, D:1, E:2}
next.equals(priority)?  0벡터와 다름 → 계속
```

#### 라운드 2 — E를 앞으로

```text
order = E(2), D(1), A(0), B(0), C(0)
```

| 단계 | 넣는 요청 | 선택 | 경로 상태 |
|---:|---|---|---|
| 1 | E 7,000 | 새 V1 | V1={E} 7,000 방문 1 |
| 2 | D 4,000 | V1 잔여 3,000 < 4,000 → 새 V2 | V2={D} 4,000 방문 1 |
| 3 | A 3,000 | V1 잔여 3,000에 딱 맞음 | V1={E,A} 10,000 방문 2 |
| 4 | B 3,000 | V2 잔여 6,000 | V2={D,B} 7,000 방문 2 |
| 5 | C 3,000 | V2 잔여 3,000 | V2={D,B,C} 10,000 방문 3 |

해: bank 0건. 전제: V1 거리 3,200 m · 운행 900 s, V2 거리 4,800 m · 운행 1,400 s.

```text
score₂ = (0, 2, 8,000, 2,300)
Scores.compare(score₂, score₁) = −1   // 미배정 0 < 1 → best 교체
```

**중앙값.** `sizes` 정렬 `[2, 3]`, `median = sizes.get(1) = 3`. V1 방문 2 < 3 → E, A에 +1씩. bank 없음(+2 없음).

```text
blame 후 priority = {A:1, B:0, C:0, D:1, E:3}
벡터가 또 변함 → 계속
```

#### 라운드 3 — 해는 같고 숫자만 오른다

```text
order = E(3), A(1), D(1), B(0), C(0)
```

E → 새 V1, A → V1 잔여 3,000, D → 새 V2, B·C → V2. 라운드 2와 같은 분할 `{E,A}` / `{D,B,C}`.
`score₃ = score₂` → `compare == 0` → **best는 라운드 2 유지**.

중앙값이 또 3이라 V1에 +1: E, A.

```text
priority = {A:2, B:0, C:0, D:1, E:4}
```

라운드 4·5도 같은 분할·같은 점수·같은 +1이 반복된다. `priority`가 매 라운드 바뀌므로 X18의 `equals`는
한 번도 참이 아니고, `ROUNDS = 5`를 다 돈 뒤 `best`(라운드 2 해)를 반환한다.

한 줄로 정리하면: **1라운드(H4)는 E를 자투리에 넣으려다 실패하고, 2라운드가 E를 맨 앞에 둬 전량 배정한다.
이후 라운드는 같은 해를 다시 만들 뿐 점수를 바꾸지 않는다.**

### 4.5 한 라운드의 연산 수 — 방문 수로

경로 방문 수가 `L`인 차량에 단일 방문 요청 하나를 넣으려면 슬롯이 `L + 1`곳이고, 슬롯마다 그 경로를 처음부터
다시 전파한다(`O(L)`). 그 차량의 비용은 `O(L²)`. 모든 기존 경로와 미사용 1대에 대해 합하고, 남은 요청마다 반복한다.

숫자 예: 한 차가 지금 20곳을 돌고 있으면 슬롯 21 × 전파 길이 21 ≈ 441번의 방문 계산. 차량 `m`대가 비슷한 길이를
갖고 요청 `n`건을 순차로 보면 한 라운드 ≈ `n · m · L²` 전파.

실물 fixture(주문 452 · 차량 31 · 정차 한도 28)에서 H4 한 판이 77 ms다. H22는 그 위에 최대 ×5가 붙는다.
실측 399 ms. **399 ≈ 77 × 5 는 추정일 뿐** — 5라운드를 실제로 다 도는지는 실물에서 재지 않았다(§6.1과 같은 문장).

---

## 5. 결정성 · 종료 · 복잡도

### 5.1 결정성 — 같은 입력이면 같은 출력

| 지점 | 순서를 고정하는 방법 |
|---|---|
| `priority` 키 | `sortedRequestIds` = `RequestId` 문자열 오름차순, `LinkedHashMap` |
| 라운드 삽입 순서 | `(priority DESC, deadlineSec ASC, RequestId ASC)` |
| 마감 동률 | `deadlineComparator`의 둘째 키 `BY_REQUEST_ID` |
| 새 경로를 열 차량 | `firstUnusedCompatible` — 미사용 호환 중 `VehicleId` 문자열 최소 1대 |
| 오라클 후보 동률 | `candidates`가 `(byCost, VehicleId)`로 정렬한 뒤, `construct`가 `byCost`로 안정 정렬 |
| `byCost` | `(newRoute, Δ거리, Δ운행시간)` — `newRoute=false`가 앞 |
| best 동률 | `Scores.compare < 0`일 때만 교체 → 앞 라운드 |
| `blame` 순회 | bank·routes는 `Solution`이 들고 있는 순서. `merge`는 값만 변경 |
| 조기 종료 | `Map.equals` — 키와 값. 순회 순서에 의존하지 않음 |

`HashSet`/`HashMap` 순회로 승자를 고르는 곳이 없다. 난수도 없다.

### 5.2 종료 — 시간이 아니라 구조로

- 라운드 수 ≤ `ROUNDS`(5). `break`는 더 줄일 수만 있다.
- 라운드 안쪽: 요청마다 규칙 (a) 삽입 또는 (b) 후보 0개. `bound = order.size()` = `n`.
  넘으면 `checkOuterLoop`가 `IllegalStateException` — 품질 문제가 아니라 버그(규범 §4.3).
- 시간 상한은 초기해에 없다. `AlnsConfig.timeLimitSec`은 ALNS에만 걸린다.

X18: `next.equals(priority)`이면 이후 라운드도 같은 `order` → 같은 해이므로 즉시 종료.
R회를 다 돌지 않는 것은 정상이다. T37 첫 fixture가 1라운드 만에 끝난다.

### 5.3 복잡도 (규범 문서 §5 표)

```text
H22  squeaky-wheel-sequential  sequential(반복)  전 패턴  바깥 루프 R·n (R = 5)  반복당 작업 R × O(m · L²)
```

같은 표의 H4 행은 바깥 루프 `= n`, 반복당 `O(m · L²)`다. H22의 "반복당 `R × O(m · L²)`"는
**H4 한 판 `O(m · L²)`에 라운드 배수 R을 붙인 표기**다. 바깥 루프가 이미 `R · n`이므로, 풀어 쓰면 전체는
최대 `R × n × O(m · L²)` — 한 요청을 기존 `m`개 경로(평균 길이 `L`)에 넣어 보는 비용 × 요청 수 × 최대 5판.

`L`은 경로 평균 길이. 후보 1개 검증 = 경로 전체 재전파 `O(L)`(규범 §4.1). 단일 방문이면 슬롯 `L + 1`이라
차량당 `O(L²)`. `PICKUP_DELIVERY`는 위치 쌍 `O(L²)` × 전파 `O(L)` → 그 요청 비중만큼 `L`이 한 번 더 곱해진다.

실물 한 판은 H4 77 ms가 실측이고, H22 전체 399 ms가 실측이다. 5판을 다 돌았는지는 미측정(§4.5).

---

## 6. 코드를 읽으며 눈여겨볼 점

규범 문서와 코드는 1:1이지만, 처음 읽을 때 헷갈리기 쉬운 곳을 적어 둔다. **수정 제안이 아니라 읽기 보조**다.

1. **한 라운드는 H4와 글자 그대로 같은 메서드다.** `DeadlineSequentialConstruction.construct(..., Candidate.byCost(), ID)`
   호출이 그 증거다. 다른 점은 `order`와 방어 카운터 문자열 `ID`뿐이다. 1라운드 `order`는 전원 `priority = 0`이라
   `deadlineOrder`와 같다.
2. **조기 종료는 해 비교가 아니다.** `next.equals(priority)`는 정수 벡터 비교다. §4.4처럼 해가 2라운드 이후 고정돼도
   방문 수 < 중앙값인 경로가 있으면 그 요청의 값이 매 라운드 +1 되어 벡터가 바뀌고, 5라운드를 다 돈다.
   반대로 경로가 하나이고 bank가 비면 중앙값 미만이 0건·+2가 0건 → 벡터 불변 → 1라운드 종료(T37).
3. **중앙값은 위쪽 값이다.** 경로 2개의 방문 수가 1과 3이면 중앙값 3. 평균 2가 아니다. `1 < 3`인 경로만 +1을 받는다.
4. **+1은 방문 노드 루프다.** 요청 루프가 아니다. 단일 방문이면 요청당 +1과 같고, pair(방문 2)면 같은 요청에 `merge`가
   두 번 적용된다.
5. **`best` 동률은 앞 라운드.** `Scores.compare < 0`만 교체. 1라운드 = H4이므로 이후가 나아지지 않으면 반환 값은 H4다.
6. **라운드마다 빈 해에서 다시 시작한다.** 직전 `Solution`을 고치지 않는다. `priority`만 다음 판의 입력이다.
   규범이 "multi-start가 아니다"라고 적은 이유 — 난수로 여러 시작점을 뽑는 게 아니라, 결정적 벡터가 이어진다.
7. **`Problem`은 어디서도 바뀌지 않는다.** `Solution`도 불변이라 `current = apply(...)`로만 갱신한다.
8. **기권 분기가 없다.** `abstains`는 상수 `false`. 구역이 없어도, 차고가 여러 개여도, pair가 섞여도 실행한다.

---

## 7. 경계 상황 (규범 §7의 X번호)

| 상황 | 동작 | 번호 |
|---|---|---|
| `priority`가 라운드 간 불변 | 이후 라운드도 같은 해 → `break`. R회를 다 돌지 않는 것은 정상 | **X18** |
| 전원 한 경로에 들어감 (경로 1개, bank 0) | `median = L`, `L < L` 거짓, +2 없음 → 벡터 불변 → 1라운드 종료. T37 첫 fixture | X18 |
| 매 라운드 bank가 남음 | bank 요청에 +2가 쌓여 벡터가 계속 변함 → R = 5를 전부 수행. T37 둘째 fixture | — |
| 경로 0개 (전 요청 bank) | `sizes.isEmpty()` → +1 생략, bank에만 +2. 다음 라운드도 같은 빈 해지만 벡터는 전원 +2라 `equals`는 거짓 → 5라운드 | — |
| construction 결과가 Evaluator Infeasible | `IllegalStateException`. 오라클과 평가기가 같은 관문이라 논리적으로 불가 | X5 |
| 한 라운드가 0건 삽입 | 전부 bank인 후보로 참여. 유효한 해. 점수 1번 축에서 대개 패배 | X4 |
| 기권 | 없음. `abstains == false` | §4.4 |
| 같은 `Problem`·`Profile` 재실행 | 같은 라운드 전개·같은 best (T37 `first.equals(second)`, `a.equals(b)`) | X8 |

---

## 8. 테스트와 실측

| 테스트 | 무엇을 고정하나 |
|---|---|
| T37 `SqueakyWheelSequentialConstructionTest.fixedRoundsDeterministicBlame` | 같은 입력 두 번 → 같은 라운드 전개·같은 best. priority 불변 시 조기 종료 (X18). blame은 단조(값이 줄지 않음) |

T37이 돌리는 두 fixture는 `ConstructionFixtures.ring`이다. 전부 `DELIVERY_ONLY`, 창은 전원 `WORK`(08:00–18:00)라
마감이 같고 삽입 순서는 `RequestId` (`R1` … `Rn`). 무게 = 부피 = 10,000, 차량 용량 30,000.

**fixture 1 — 조기 종료.** `ring(3, 1, 30_000, 10_000)`: 요청 3 · 차량 1 · 용량이 3건분. 전량이 한 경로에 들어간다.
`trace.size() == 1`, `bank` 공집합. 경로 1개의 방문 3, `median = 3`, +1 없음 · +2 없음 → 벡터 불변 → X18.

**fixture 2 — 5라운드 전부.** `ring(5, 1, 30_000, 10_000)`: 요청 5 · 차량 1 · 용량 3건분. 매 라운드 bank 2건.
`trace.size() == ROUNDS`(5). 두 번 실행한 `trace`와 `Solution`이 같다. bank 2건이 매 라운드 +2를 받아 벡터가
계속 변하므로 조기 종료가 없고, 어느 라운드의 값이든 직전보다 `>=` (단조).

실행 명령:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env
mvn test -pl solver-core -Dtest=SqueakyWheelSequentialConstructionTest
```

실물 실측 (`data/win_poc_case_floor.json`, 주문 452 · 차량 31, 2026-09-04 단일 실행,
[h1–h24 §1 표](initial-solution-heuristics-h1-h24.md)):

| 기법 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 | 24개 중 |
|---|---:|---:|---:|---:|---:|---:|
| H4 `deadline-sequential` (1라운드와 동일) | 65 | 31 | 3,604,197 | 935,716 | 77 ms | 7위 |
| **H22 `squeaky-wheel-sequential`** | **47** | 31 | 4,666,316 | 967,429 | **399 ms** | **4위** |

이 표는 한 번의 실행 기록이고 T번호로 고정돼 있지 않다. 회귀로 고정된 H22 동작은 T37이다.

가설: 실물에서 5라운드를 다 돌았는지는 재지 않았다. 399 ms ≈ 77 ms × 5 는 그 추정일 뿐이다.
측정된 것은 전체 399 ms와 H4 77 ms 두 값이다.

---

## 용어 각주

[^cvrptw]: **CVRPTW** (Capacitated Vehicle Routing Problem with Time Windows) — 용량 제한과 시간창이 있는 차량 경로 문제.
    이 저장소가 푸는 **RPDPTW**는 거기에 pickup-and-delivery(한 주문이 싣는 곳과 내리는 곳 두 방문을 가짐)와 실무 제약
    (차급·구역·정차 한도·근무창 등)을 더한 "rich" 변형이다.

[^construction]: **construction (heuristic)** — 빈 해에서 시작해 요청을 하나씩 넣어 첫 해를 만드는 기법. 이 뒤에 오는 ALNS
    (Adaptive Large Neighborhood Search)가 그 해를 부수고 다시 고치며 개선한다. 이 저장소에서는 construction이 24개이고
    난수를 쓰지 않는다.

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
    (싣기만 하고 차고 도착에서 하차) 셋 중 하나. 배정·제거의 원자 단위라 pickup만 따로 옮기는 일은 없다. pair = 이 원자 단위.

[^profile]: **`Profile`** — 고객별로 다른 hard/soft 제약과 점수 축을 담은 객체. core 코드에 `if (customerId == …)`를 두지 않고
    이 객체를 갈아 끼우는 방식으로 고객 차이를 다룬다. 탐색과 재검증에 같은 인스턴스를 쓴다.

[^oracle]: **오라클** — 최적화 문헌에서 "이 후보가 유효한가·비용이 얼마인가"를 정확히 답해 주는 검사기를 부르는 말. 여기서는
    `InsertionSearch`가 그 역할이다 — 삽입 후보를 경로 전체 재전파 + profile hard로 검증해 통과한 것만 돌려준다. H22는
    유효성 판정을 여기에 전부 위임하고 스스로는 "어떤 순서로 넣을지"만 정한다.

[^spi]: **SPI (Service Provider Interface)** — 구현체를 여러 개 꽂아 넣을 수 있게 만든 인터페이스. `ConstructionHeuristic`을
    구현한 24개 클래스가 `InitialSolutionBuilder`에 목록으로 등록된다.

[^hard-soft]: **hard / soft 제약** — hard는 어기면 해가 무효인 제약(용량·시간창·정차 한도·구역 단일성·차급 등), soft는 어겨도
    되지만 점수가 나빠지는 것. 이 저장소에서 construction과 ALNS의 삽입은 hard를 전부 통과한 후보만 받는다.

[^fixture]: **fixture** — 테스트·실측에 쓰는 고정 입력 데이터. 이 저장소의 "실물 fixture"는 `data/win_poc_case_floor.json`
    (실제 고객 데이터, 주문 452건·차량 31대)이다. T37이 쓰는 `ring`은 테스트 전용 합성 입력이다.

[^propagation]: **전파 (`RoutePropagator`)** — 경로의 방문 순서가 주어졌을 때 차고 출발부터 순서대로 도착·서비스 시작·출발 시각,
    적재량, 정차 수 등을 계산하고 hard 제약을 검사해 `Feasible`(사실값 포함) 또는 `Infeasible`(위반 코드)로 답하는 부품.
    삽입 후보 하나를 검증할 때마다 그 경로 전체를 처음부터 다시 전파한다 — 캐시 없이.

[^units]: **단위** — 무게·부피는 원래 값에 ×1000을 곱해 내림(FLOOR)한 `long`, 거리는 meter, 시간은 초 정수다. `double` 근사를
    거치지 않는다. 워크스루의 부피 3,000은 "3 단위"를 뜻한다.

[^regret]: **regret 삽입** — 요청마다 "최선 위치와 차선 위치의 비용 차(regret)"를 계산해 그 차가 큰 요청(지금 안 넣으면 나중에
    크게 손해 보는 요청)부터 넣는 기법. H1·H2가 이 계열이다. H22는 regret을 쓰지 않는다 — 순서는 `priority`와 마감뿐이다.

[^seed]: **씨앗 (seed)** — 빈 경로를 열 때 맨 처음 싣는 주문. H5·H10·H17이 이 개념을 쓴다. H22의 새 경로는
    `firstUnusedCompatible`이 고른 차에 `byCost` 최선(그 차의 유일한 새 경로 후보)으로 열릴 뿐, 씨앗을 따로 고르지 않는다.
