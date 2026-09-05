# `UrgencyRegret3Construction` (H2) 코드 해설

> **성격**: 코드 해설 노트(비규범). 규범은 [heuristics 문서 §5 H2](../../implementation/stage-04-initial-solution-heuristics.md)이고,
> 쉬운 말 설명은 [h1-h24 §6.2](../initial-solution-heuristics-h1-h24.md)다 (비용 정의는 같은 문서 §6.0).
> 셋이 어긋나면 규범이 이긴다. 이 노트는 §6.2를 코드 산수까지 연장한다.
>
> **대상 독자**: CVRPTW[^cvrptw]를 아는 Java 개발자. 이 저장소의 용어(`Request`·`Problem`·bank 등)는 처음 본다고 가정하고,
> 처음 나올 때마다 각주로 풀었다. 각주는 문서 끝 [용어 각주](#용어-각주)에 모여 있다.
>
> **대상 파일**: `solver-core/src/main/java/com/ronext/rpdptw/solve/UrgencyRegret3Construction.java` (105줄, 2026-09-02 기준).

---

## 0. 한 줄 요약

**"시간창[^time-window]이 좁은 요청을 4등급으로 뭉갠 뒤, 같은 등급 안에서는 '지금 안 넣으면 Δ거리가 얼마나 커지는가'(regret-3[^regret])가 큰 것부터 넣는다."**

이름을 뜯어 보면 그대로다.

| 이름 조각 | 뜻 |
|---|---|
| `urgency` | 급한 정도 = **시간창 폭**. 폭이 좁을수록 끼울 시각이 적다 → 등급 0이 가장 급함 |
| `regret3` | 최선 자리(c1)를 놓쳤을 때 2위·3위가 얼마나 비싼지. `(c2−c1)+(c3−c1)` |
| `Construction` | 초기해[^initial-solution] 구축 기법[^construction] — 빈 해에서 시작해 요청을 하나씩 넣는다 |

regret은 "지금 최선을 놓치면 차선·3선 Δ거리가 얼마인가"를 한 숫자로 부른 이름이다. H1 `scarcity-regret2`와 같은 계열(parallel-regret[^parallel-regret])이되, 1순위 키가 **호환[^compat] 차량 수**가 아니라 **시간창 등급**이고, regret 깊이가 2가 아니라 3이라 다른 해를 낸다. 실물 fixture[^fixture]에서 H1 미배정 86 · H2 미배정 60.

---

## 1. 프로젝트 안에서의 위치

### 1.1 초기해 포트폴리오의 한 칸

`InitialSolutionBuilder.build(problem, profile)`가 결정적 construction **24개**(H1~H24)를 전부 돌려 각 결과를 정식 평가(`Evaluator`)에 넣고, 사전식 점수[^lexicographic-score]가 가장 좋은 하나를 고른다. H2는 그 24개 중 **2번째**(우선순위 앞쪽)이고, 실물 fixture 순위는 **6위**(미배정 60).

```text
InitialSolutionBuilder.build(problem, profile)
  ├─ H1 ScarcityRegret2Construction          (호환 차량 수 + regret-2)
  ├─ H2 UrgencyRegret3Construction           ← 이 문서
  ├─ H3 … H24
  → 각 Solution을 Evaluator로 평가 → 사전식 최선 1개
```

우선순위가 앞선다는 것은 실행 순서와, 점수 네 축이 **완전히 같은** 동률에서 이긴다는 뜻이다(규범 §6-4·X7). 실물에서는 동률이 없다.

H5처럼 씨앗[^seed]으로 경로를 하나 열고 채우는 기법이 아니다. 매 단계 **남은 요청 전부**를 다시 보고 하나만 고른다.

### 1.2 구현하는 인터페이스 — `ConstructionHeuristic` (SPI[^spi])

```java
public interface ConstructionHeuristic {
    String id();                                   // "urgency-regret3"
    boolean abstains(Problem problem);             // H2는 항상 false
    Solution construct(Problem problem, Profile profile);
}
```

세 가지 계약이 이 파일 전체를 지배한다.

1. **난수 없음, 결정적**[^determinism]. 모든 비교가 정수 키 + `RequestId` 문자열로 끝난다.
2. **반환 해는 항상 유효**하다. 못 넣은 요청은 bank[^bank]에 남기면 된다. 삽입은 전부 `InsertionSearch`를 통해서만 하므로 시간창·용량·정차 한도 등 hard 제약[^hard-soft]은 오라클[^oracle]이 보장한다.
3. **기권[^abstain]하지 않는다.** `abstains`는 `return false` 한 줄. 어떤 `Problem`에서도 돈다(규범 §4.4).

### 1.3 다루는 자료형 요약

| 타입 | 이 파일에서의 뜻 |
|---|---|
| `Problem`[^problem] | 동결된 입력 전체. 요청·차량·이동표·호환표. 탐색은 절대 고치지 않는다 |
| `Request`[^request] | 배차의 원자 단위(pair). pickup·delivery side 중 있는 것, 창 목록은 side에 있다 |
| `RequestSide` | 한 방문의 장소·창 목록(`List<TimeWindow>`)·서비스 시간. 등급 계산은 **마지막 side**만 본다 |
| `TimeWindow` | `(openSec, closeSec)` 초 정수[^units]. 날마다 반복된 절대 창 하나 |
| `Solution` | `List<Route>` + bank(`Set<RequestId>`). 불변 record — 삽입할 때마다 새 객체 |
| `Profile`[^profile] | 고객별 hard/soft 묶음. 이 파일은 그대로 `InsertionSearch`에 넘기기만 한다 |
| `InsertionSearch.Candidate` | "이 차량의 이 방문 순서에 넣으면 비용이 이만큼 는다"는 삽입 후보 |
| `Pick` (이 파일 private) | 한 요청의 선택 키 묶음 — `(requestId, grade, candidateCount, regret3, candidate)` |

---

## 2. 기대는 부품 — `InsertionSearch`

이 파일은 "지금 남은 것 중 누구를 넣을지"만 정한다. **"넣을 수 있는가·어느 위치가 싼가"는 전부 오라클**이다. H2가 **실제로 부르는** 메서드는 아래가 전부다. `candidatesFor`·`remove`·`visitsOf`는 안 부른다.

| 메서드 | 하는 일 | H2에서 쓰는 곳 |
|---|---|---|
| `emptySolution(problem)` | 전 요청이 bank인 빈 해 | 시작점 |
| `new InsertionSearch.Cache(problem, profile)` | 경로 버전 캐시. `candidates()`와 같은 순서의 상위 3개를 차량별로 기억 | 루프 밖 1회 |
| `cache.candidates(current, requestId)` | **모든 기존 경로 + 미사용 호환 차량 1대**의 후보를 `byCost` 오름차순으로 | 남은 요청마다, 매 반복 |
| `apply(problem, current, candidate)` | 후보를 적용한 **새** `Solution`. 그 요청은 bank에서 빠진다 | 선택된 1건 |
| `cache.forget(requestId)` | 방금 넣은 요청의 캐시 항목 삭제 | 삽입 직후 |
| `checkOuterLoop(id, n, bound)` | 바깥 루프 방어 카운터. 상한을 넘으면 `IllegalStateException` | 매 바깥 바퀴 |
| `sortedRequestIds(problem)` · `BY_REQUEST_ID` | ID 문자열 순 | pending 초기값 · 동률 |
| `lastSide(request)` · `windowSpanSec(side)` | 마지막 방문 side · 그 side 창 폭 합(초) | 등급을 **한 번** 계산할 때 |

정적 `InsertionSearch.candidates(...)`를 직접 부르지는 않는다. `Cache.candidates`가 같은 대상(기존 경로 전부 + 미사용 호환 1대)을 보고, 차량마다 상위 `Cache.TOP = 3`개만 남긴 뒤 다시 `byCost`로 합친다. 상위 3개가 전체 재계산과 같음은 T13b가 대조한다 — regret-3이 읽는 자리가 딱 그 3개라서 캐시 깊이가 3이다.

### 2.1 후보가 나오는 범위 — "모든 차"가 아니다

`candidates`가 보는 차량은 두 부류뿐이다(규범 §4.1).

1. **이미 경로가 있는** 호환 차량 — 그 경로의 모든 삽입 위치.
2. **아직 경로가 없는** 호환 차량 중 `VehicleId` 문자열 순 **첫 1대**만 새 경로로.

미사용 호환 차량이 3대여도 후보는 1대분만 열린다. 그래서 해가 비어 있는 첫 바퀴에는 요청마다 후보가 **새 경로 1개**(통과하면)가 전형값이다. 3대 전부의 Δ거리를 동시에 재는 기법이 아니다.

### 2.2 비용 `Candidate.byCost` — regret이 빼는 칸

```470:478:solver-core/src/main/java/com/ronext/rpdptw/solve/InsertionSearch.java
        /**
         * 사전식 비용: (새 경로 여부, Δ거리, Δ운행시간). 후보 '고르기' 전용 (§4.2).
         * deltaForwardSlackSec은 여기 들지 않는다 — H13만 자기 비교자로 쓴다 (§5 H13).
         */
        public static Comparator<Candidate> byCost() {
            return Comparator.comparing(Candidate::newRoute)
                    .thenComparingLong(Candidate::deltaDriveDistMeter)
                    .thenComparingLong(Candidate::deltaRouteOperationalTimeSec);
        }
```

- `newRoute false < true` — 기존 경로 자리가 새 차 열기보다 항상 앞선다. 차량 수가 점수 2번 축이라서다(h1-h24 §6.0).
- 그다음 `deltaDriveDistMeter` — 그 경로 총 주행거리가 늘어나는 미터. **regret-3이 더하고 빼는 칸은 이 값만**이다. 단위가 미터로 같은 것끼리만 뺀다.
- 동률은 Δ운행시간, 그다음 `sorted`가 `VehicleId` 문자열을 붙인다.

전제: 상위 3개 c1·c2·c3의 **순서**는 `byCost`이고, 규범 문면의 "Δ거리 기준"은 뺄셈에 쓰는 필드가 Δ거리라는 뜻이다. Δ거리로 다시 정렬하지 않는다. 기존 경로 후보가 3개 있으면 새 경로 후보는 상위 3에 못 들어온다(`newRoute`가 뒤이므로).

### 2.3 왜 H4의 8배가 걸리는가

H4 `deadline-sequential`은 요청을 한 줄로 세운 뒤 **요청당 `candidates` 1회**. H2는 바깥 바퀴마다 **남은 요청 전부**에 `cache.candidates`를 다시 부른다. 경로가 하나 바뀌면 그 자리가 남긴 요청의 2위·3위를 바꿔 놓기 때문이다. 이것이 parallel-regret의 정의고, 규범 표가 H1·H2를 `O(n · m · L²)`로 묶은 이유이며, 실측 644 ms vs H4 77 ms의 원인이다. 캐시는 안 바뀐 차량의 재전파만 건너뛸 뿐, 남은 요청을 매 바퀴 다시 훑는 루프 자체는 그대로다.

---

## 3. 알고리즘 전체 흐름

```text
abstains?  항상 false
     │
     ▼
① 긴급 등급   grade = urgencyGrades(problem)     ← Problem에서 한 번, 이후 동결
     │           창 폭 합 오름차순 → 절단값 3개 → 각 요청 0..3
     ▼
② 빈 해        current = emptySolution
               pending = 전 RequestId (문자열 순)
               Cache 1개, bound = n
     ▼
③ while pending ≠ ∅
     ├─ 남은 r마다 cache.candidates(current, r)
     │     후보 0개 → excluded (규칙 b, 한 바퀴에 여러 건 가능)
     │     그 외    → regret3 · candidateCount=min(size,3) → Pick
     ├─ PICK_ORDER 최소인 Pick이 best
     ├─ pending에서 excluded 제거
     └─ best 있으면 apply + forget + pending에서 제거 (규칙 a)
     ▼
   return current     ← 못 넣은 것은 bank 그대로
```

바깥 루프는 한 바퀴에 pending이 1 이상 줄어든다 → **≤ n**. 안쪽의 후보 재계산 횟수는 이 상한과 무관하다(규범 §4.3).

---

## 4. 단계별 상세

### 4.1 `abstains` — 기권 없음

```28:30:solver-core/src/main/java/com/ronext/rpdptw/solve/UrgencyRegret3Construction.java
    public boolean abstains(Problem problem) {
        return false;
    }
```

차고 수·패턴·존 유무를 보지 않는다. 기권 가능한 기법은 H5·H7·H8·H19·H20뿐(규범 §4.4).

### 4.2 긴급 등급 — `urgencyGrades` (이 기법의 정적 키)

규범 §5 H2:

```text
긴급 등급 = 문제 전체 Request의 창 폭 합을 오름차순 정렬해 4등분한 사분위 등급 (0..3).
  창 폭 합 = (delivery가 있으면 delivery, 없으면 pickup) side의 Σ(close − open)
  등급 경계는 Problem에서 한 번 계산하고 동결한다 — 삽입 중에 바뀌지 않는다 (결정적).
```

코드가 그 문면을 산수로 옮긴 곳이 여기다.

```75:96:solver-core/src/main/java/com/ronext/rpdptw/solve/UrgencyRegret3Construction.java
    static Map<RequestId, Integer> urgencyGrades(Problem problem) {
        List<Long> widths = new ArrayList<>();
        Map<RequestId, Long> widthById = new LinkedHashMap<>();
        for (Request request : problem.requests()) {
            long width = InsertionSearch.windowSpanSec(InsertionSearch.lastSide(request));
            widthById.put(request.id(), width);
            widths.add(width);
        }
        widths.sort(Comparator.naturalOrder());
        int n = widths.size();
        Map<RequestId, Integer> grades = new LinkedHashMap<>();
        for (Map.Entry<RequestId, Long> entry : widthById.entrySet()) {
            int grade = 0;
            for (int k = 1; k <= 3; k++) {
                if (entry.getValue() >= widths.get((int) ((long) k * n / 4))) {
                    grade++;
                }
            }
            grades.put(entry.getKey(), grade);
        }
        return grades;
    }
```

한 줄씩:

| 단계 | 코드가 하는 일 |
|---|---|
| side | `lastSide` = `delivery.orElseGet(pickup)`. **PICKUP_DELIVERY는 delivery 창만**. pickup 창은 등급에 안 들어간다 |
| 폭 | `windowSpanSec` = 그 side의 **모든** 창에 대해 `Σ(closeSec − openSec)`. 첫 open~마지막 close가 아니다. 점심 공백은 폭에 안 넣는다 |
| 정렬 | 폭 `n`개를 오름차순. 같은 폭이 여러 건이면 그 값이 목록에 그대로 반복된다 |
| 절단 인덱스 | `k = 1, 2, 3`에 대해 `⌊k · n / 4⌋` (Java `(long) k * n / 4`는 양수라 0 쪽 버림). 그 인덱스의 **값**이 절단값 |
| 등급 | 폭 `w`가 절단값 `≥`이면 1씩 올린다. 세 문을 모두 넘으면 3, 하나도 안 넘으면 0 |
| 동률 | 폭이 같으면 절단값과의 `≥` 결과가 같으므로 **같은 등급**. 순위(몇 번째 요청인가)가 아니라 값으로 가른다 |
| 동결 | `construct` 첫 줄에서 한 번. 삽입 중에 다시 계산하지 않는다 |

`k ≤ 3`의 3, 분모 4는 **재량 상수, Stage 8이 조정한다**(규범 §1 재량 표 "4분위 등급"). 축을 가중합으로 뭉개지 않으려고 폭 초 값을 1순위 키에 직접 넣지 않고 4칸으로 접은 것이다(규범 H2 불릿).

`lastSide`·`windowSpanSec`의 산수:

```334:353:solver-core/src/main/java/com/ronext/rpdptw/solve/InsertionSearch.java
    /** 마지막 방문 side (delivery가 있으면 delivery, 없으면 pickup). */
    static RequestSide lastSide(Request request) {
        return request.delivery().orElseGet(() -> request.pickup().orElseThrow());
    }
    ...
    static long windowSpanSec(RequestSide side) {
        long span = 0L;
        for (TimeWindow window : side.windows()) {
            span = Math.addExact(span, window.closeSec() - window.openSec());
        }
        return span;
    }
```

#### 워크스루 A — 요청 8건의 4분위 (코드와 같은 산수)

전부 `DELIVERY_ONLY`, 창은 자정 기준 초. 차량은 아직 안 본다 — 등급은 `Problem`만 보고 정한다.

| ID | delivery 창 | 폭 (초) |
|---|---|---:|
| A | 08:00–08:30 (`28800–30600`) | 1,800 |
| B | 08:00–09:00 | 3,600 |
| C | 08:00–10:00 | 7,200 |
| D | 08:00–12:00 | 14,400 |
| E | 08:00–14:00 | 21,600 |
| F | 08:00–16:00 | 28,800 |
| G | 08:00–18:00 | 36,000 |
| H | 08:00–20:00 | 43,200 |

정렬된 폭 `widths` (`n = 8`):

```text
index  0      1      2       3       4       5       6       7
값     1800   3600   7200    14400   21600   28800   36000   43200
ID     A      B      C       D       E       F       G       H
```

절단 인덱스 = `⌊k·8/4⌋`:

| k | `k*n/4` | 인덱스 | 절단값 `widths.get(…)` |
|---:|---:|---:|---:|
| 1 | 2 | 2 | **7,200** (C의 폭) |
| 2 | 4 | 4 | **21,600** (E의 폭) |
| 3 | 6 | 6 | **36,000** (G의 폭) |

각 요청: `w ≥ 7200`이면 +1, `w ≥ 21600`이면 +1, `w ≥ 36000`이면 +1. **절단값 자체는 높은 쪽 칸**에 들어간다(`>=`).

| ID | 폭 | ≥7200 | ≥21600 | ≥36000 | 등급 |
|---|---:|:---:|:---:|:---:|---:|
| A | 1,800 | | | | **0** |
| B | 3,600 | | | | **0** |
| C | 7,200 | ✓ | | | **1** |
| D | 14,400 | ✓ | | | **1** |
| E | 21,600 | ✓ | ✓ | | **2** |
| F | 28,800 | ✓ | ✓ | | **2** |
| G | 36,000 | ✓ | ✓ | ✓ | **3** |
| H | 43,200 | ✓ | ✓ | ✓ | **3** |

0이 가장 급하다. 8건이 값만 다르면 2·2·2·2로 나뉜다. 이 등급은 끝까지 안 변한다.

**두 창의 합.** H의 창이 `09:00–12:00`(10,800) + `13:00–18:00`(18,000)이면 폭은 28,800이지 09:00–18:00의 32,400이 아니다. 그 28,800은 F와 같아 **둘 다 등급 2**가 된다. 점심 1시간은 폭에 없다.

#### 워크스루 A의 가장자리 — 동률 · n&lt;4 · 전부 동일

같은 폭은 같은 등급. n=8이고 폭이 `100, 100, 100, 200, 200, 300, 400, 400`이면 절단값은 `widths[2]=100`, `widths[4]=200`, `widths[6]=400`. 100은 첫 절단값과 같아 **전원 최소값이 이미 등급 1**이고, 등급 0은 비다.

| 입력 | 절단 인덱스 | 결과 |
|---|---|---|
| n=1, 폭 아무 것 | `⌊k·1/4⌋ = 0` 세 번 → 자기 폭과 `≥` 3회 | 그 1건 **등급 3** |
| n=2, 폭 1,800 &lt; 7,200 | 인덱스 0, 1, 1 | 좁은 쪽 등급 **1**, 넓은 쪽 **3** |
| n=3, 엄밀 증가 | 0, 1, 2 | 등급 1 · 2 · 3 (0 없음) |
| n=4, 엄밀 증가 | 1, 2, 3 | 등급 0 · 1 · 2 · 3 각 1건 |
| n건 폭이 전부 같음 | 세 절단값이 그 같은 값 | **전원 등급 3** |

n=0이면 `widthById`가 비어 안쪽 `widths.get`에 들어가지 않는다. 빈 해 반환(X1).

전제: "4등분"은 인원 수가 아니라 **정렬된 폭 배열의 인덱스**로 자른다. 동률이 많으면 칸 인원이 2·2·2·2가 아니다. 실물 fixture의 05:45–13:30 169건은 폭이 같아 한 칸에 몰린다.

§6.2의 6건 예시(30분·1시간·2시간·4시간·6시간·8시간)를 같은 산수에 넣으면 n=6, 절단값은 `widths[1]=3600`, `widths[3]=14400`, `widths[4]=21600`이다. **B(1시간=3600)는 첫 절단값과 같아 등급 1**이지, §6.2가 묶은 "A·B 0등급"이 아니다. A만 0, B·C가 1, D가 2, E·F가 3. 쉬운 설명은 "뭉갠다"는 의도만 말하고, 경계의 `>=`는 코드가 정한다.

### 4.3 바깥 루프 — 규칙 (a)/(b)

```33:69:solver-core/src/main/java/com/ronext/rpdptw/solve/UrgencyRegret3Construction.java
    public Solution construct(Problem problem, Profile profile) {
        Map<RequestId, Integer> grade = urgencyGrades(problem);           // Problem에서 한 번 계산하고 동결
        Solution current = InsertionSearch.emptySolution(problem);
        InsertionSearch.Cache cache = new InsertionSearch.Cache(problem, profile);
        Set<RequestId> pending = new LinkedHashSet<>(InsertionSearch.sortedRequestIds(problem));
        int bound = pending.size();
        int iterations = 0;
        while (!pending.isEmpty()) {
            InsertionSearch.checkOuterLoop(ID, ++iterations, bound);
            Pick best = null;
            List<RequestId> excluded = new ArrayList<>();
            for (RequestId requestId : pending) {
                List<Candidate> cands = cache.candidates(current, requestId);
                if (cands.isEmpty()) {
                    excluded.add(requestId);                                   // 규칙 (b)
                    continue;
                }
                long c1 = cands.get(0).deltaDriveDistMeter();
                long regret3 = 0L;
                for (int k = 1; k < Math.min(3, cands.size()); k++) {          // 없는 항은 채우지 않는다
                    regret3 = Math.addExact(regret3, cands.get(k).deltaDriveDistMeter() - c1);
                }
                // 후보 수는 regret3의 빠진 항을 대신하는 축이라 3에서 자른다 — 그 이상은 항이 다 있다
                Pick pick = new Pick(requestId, grade.get(requestId), Math.min(cands.size(), 3), regret3, cands.getFirst());
                if (best == null || PICK_ORDER.compare(pick, best) < 0) {
                    best = pick;
                }
            }
            pending.removeAll(excluded);
            if (best != null) {
                current = InsertionSearch.apply(problem, current, best.candidate());   // 규칙 (a)
                pending.remove(best.requestId());
                cache.forget(best.requestId());
            }
        }
        return current;
    }
```

- `pending`은 아직 판단 안 한 요청. bank와 같다기보다 "이번 루프의 미처리". 후보 0개로 제외되면 그 바퀴에서 pending을 떠나 **bank에 남고 다시 안 본다**.
- 한 바퀴에서 excluded는 여러 건일 수 있다. 그 다음 한 건을 삽입한다. 남은 것이 전부 후보 0개면 `best == null`이고 pending이 비며 끝난다.
- `cands.getFirst()`가 삽입 위치 — `byCost` 최선. regret은 고르는 키일 뿐, 넣을 때는 항상 c1 자리다.

### 4.4 regret-3 — 있는 항만, 후보 수는 3에서 자른다

`cands`는 이미 `byCost` 오름차순. 인덱스 0·1·2만 본다.

```text
c1 = cands[0].deltaDriveDistMeter
regret3 = 0
k = 1 .. min(3, size)-1:
    regret3 += cands[k].deltaDriveDistMeter − c1     // Math.addExact
candidateCount = min(size, 3)
```

풀어 쓰면 규범 그대로다: **`(c2−c1)+(c3−c1)`**. 없는 항을 c1으로 채우지 **않는다**(그러면 그 항이 0이 되어 후보 1개와 후보 3개·격차 0이 구분이 안 된다). 대신 선택 키 2번 칸 `candidateCount ASC`가 "후보가 적을수록 급하다"를 맡는다.

| `cands.size()` | 루프 | `regret3` | `candidateCount` |
|---:|---|---|---:|
| 0 | 여기 안 옴 — excluded | — | — |
| 1 | k 없음 | **0** | **1** |
| 2 | k=1 한 항 | `cands[1]−cands[0]` | **2** |
| 3 | k=1,2 | `(c2−c1)+(c3−c1)` | **3** |
| 4 이상 | k=1,2 (cands[3+] 무시) | 상위 3개만 | **3** |

주석 원문(55–56행): *"후보 수는 regret3의 빠진 항을 대신하는 축이라 3에서 자른다 — 그 이상은 항이 다 있다."* 후보가 10개여도 regret 항은 2개(k=1,2)가 끝이라, 4개 이상인 요청들을 후보 수로 가릴 이유가 없다. 3에서 접어야 그 축이 "항이 빠졌는가"만 가리킨다.

H1은 같은 문제를 `boolean onlyCandidate`(후보 1개면 true)로 풀고 regret-2만 뺀다. H2는 깊이가 3이라 접는 지점이 3이다.

`Cache.TOP = 3`도 같은 3이다. 차량마다 4번째 이하는 regret-3이 안 읽으므로 캐시할 필요가 없다.

### 4.5 선택 키 — `PICK_ORDER`

```98:104:solver-core/src/main/java/com/ronext/rpdptw/solve/UrgencyRegret3Construction.java
    /** 선택 키 = (긴급 등급 ASC, 후보 수 ASC, regret3 DESC, RequestId ASC). */
    private static final Comparator<Pick> PICK_ORDER = Comparator.comparingInt(Pick::grade)
            .thenComparingInt(Pick::candidateCount)
            .thenComparing(Pick::regret3, Comparator.reverseOrder())
            .thenComparing(Pick::requestId, InsertionSearch.BY_REQUEST_ID);

    private record Pick(RequestId requestId, int grade, int candidateCount, long regret3, Candidate candidate) {}
```

| 순서 | 키 | 방향 | 왜 |
|---:|---|---|---|
| 1 | `grade` | ASC (0이 먼저) | 창이 좁은 등급을 먼저. 이 칸이 앞서면 regret은 안 본다 |
| 2 | `candidateCount` | ASC (1이 먼저) | 지금 자리가 적은 요청. regret 빠진 항의 대용. 후보 1개는 "지금 아니면 이 기회 끝" |
| 3 | `regret3` | DESC (큰 값이 먼저) | 최선을 놓치면 Δ거리가 많이 늘어나는 요청 |
| 4 | `RequestId` | ASC 문자열 | 총 순서. 여기까지 오면 승자는 하나 |

`best == null || PICK_ORDER.compare(pick, best) < 0`일 때만 교체한다. `RequestId`가 마지막이라 compare가 0이 되는 쌍은 없다. pending을 ID 순으로 훑는 것은 결정성 보조일 뿐, 승자는 비교기가 정한다.

H1 키와 나란히 보면 계열이 같고 1·2·3칸이 다르다.

| 칸 | H1 `scarcity-regret2` | H2 `urgency-regret3` |
|---|---|---|
| 1 | `compatibleVehicles(r).size()` ASC (정적) | 시간창 등급 ASC (정적, 4칸) |
| 2 | `onlyCandidate` DESC | `candidateCount` ASC (동적, 상한 3) |
| 3 | regret-2 = `c2−c1` DESC | regret-3 = `(c2−c1)+(c3−c1)` DESC |
| 4 | `RequestId` ASC | `RequestId` ASC |

H1의 희소도는 삽입이 진행돼도 안 줄어든다 — 이미 가득 찬 차도 분모에 남는다. H2의 등급도 안 줄어든다. 동적인 것은 둘 다 매 바퀴 다시 세는 후보 쪽이다. H9가 "지금 들어가는 경로 수"로 희소도를 동적으로 바꾼 변종이다.

### 4.6 워크스루 B — 한 바퀴의 선택

이어서 워크스루 A의 A~H, 차량 V1·V2·V3. 세 차 모두 전 요청과 호환. 단위 m.

**0바퀴 직후(빈 해).** 기존 경로 0, 미사용 첫 차 = V1. 통과하는 요청은 전부 후보 1개(새 경로), `regret3=0`, `candidateCount=1`. 키는 `(등급, 1, 0, ID)` → 등급 0인 A·B 중 문자열 앞선 **A**를 V1 새 경로에 넣는다. 이 바퀴에서 regret-3은 전원 0이라 쓰이지 않는다.

A 다음 바퀴도 경로가 V1 하나뿐이면, B는 기존 경로 자리 + V2 새 경로가 열린다. 등급 0이 B 혼자 남았으면 B가 이긴다. 아래는 **A·B가 V1에 실린 뒤**, pending = {C, D, E, F, G, H}인 바퀴다.

전제: 아래 Δ거리는 오라클이 그 상태에서 돌려줬다고 둔 숫자다. 이 파일이 거리를 계산하지 않는다.

V1 방문 2개 → `DELIVERY_ONLY` 삽입 위치 3곳 + 미사용 첫 차 V2의 새 경로. V3는 아직 안 본다.

| ID | 등급 | 오라클이 낸 후보 (byCost 순, Δm) | c1, c2, c3 | regret3 | count | 키 `(grade, count, regret3, id)` |
|---|---:|---|---|---:|---:|---|
| C | 1 | V1 400, V1 1,100, V1 2,500, (V2 새 8,000) | 400, 1100, 2500 | (1100−400)+(2500−400)=**2,800** | 3 | `(1, 3, 2800, C)` |
| D | 1 | V1 350, V2 새 8,000 | 350, 8000, — | 8000−350=**7,650** | **2** | `(1, 2, 7650, D)` |
| E | 2 | V1 200, V1 2,000, V1 3,000 | 200, 2000, 3000 | 1,800+2,800=**4,600** | 3 | `(2, 3, 4600, E)` |
| F | 2 | V1 500, V1 900, V1 2,000 | 500, 900, 2000 | 400+1,500=**1,900** | 3 | `(2, 3, 1900, F)` |
| G | 3 | V2 새 4,000  (V1 위치 전부 탈락) | 4000, —, — | **0** | **1** | `(3, 1, 0, G)` |
| H | 3 | V1 100, V1 800, V1 1,200 | 100, 800, 1200 | 700+1,100=**1,800** | 3 | `(3, 3, 1800, H)` |

비교 순서:

1. **등급 ASC** — C·D(1)가 E~H를 이긴다. G는 후보 1개라 "지금 아니면 끝"이지만 등급 3이라 1순위에서 진다.
2. **count ASC** — D(2) &lt; C(3). D가 이긴다. C의 regret 2,800은 이 칸에서 이미 안 본다.
3. regret3·ID는 이번 승자에게 필요 없다.

승자 D, 삽입 위치는 c1 = V1에 Δ 350 m인 자리. `apply` 후 pending에서 D 제거, `cache.forget(D)`. V1 방문이 바뀌었으니 다음 바퀴에서 남은 요청의 V1 캐시는 미스, V2 빈 경로 캐시는 히트.

D가 C를 이긴 이유가 워크스루의 핵심이다. 규범이 "없는 항은 채우지 않고 후보 수 ASC로 대신한다"고 한 그대로 — 후보 2개인 D는 3선 항이 없고, 그 사실이 regret 숫자보다 앞선다.

---

## 5. 결정성 · 종료 · 복잡도

### 5.1 결정성 — 같은 입력이면 같은 출력

| 지점 | 순서를 고정하는 방법 |
|---|---|
| 등급 | 폭 값 + `⌊k·n/4⌋` 인덱스의 값. 요청 순회 순서에 무관 |
| pending 초기값 | `sortedRequestIds` = `RequestId` 문자열 |
| 오라클 후보 | `byCost` 다음 `VehicleId`. 미사용 차는 문자열 최솟값 1대 |
| Pick 승자 | `PICK_ORDER` 총 순서. `HashMap` 순회로 고르지 않는다 |
| 캐시 | 조회 키가 `(requestId, vehicleId, visits)`. 히트·미스와 무관하게 같은 상위 3개 (T13b) |

난수 없음. `double`도 없음.

### 5.2 종료 — 시간이 아니라 구조로

- 바깥 `while`: 한 바퀴에 excluded ∪ {best} 만큼 pending 감소, 최소 1. `bound = n`.
- 넘으면 `checkOuterLoop`가 `IllegalStateException` — 품질이 아니라 버그(X13).
- 시간 상한 없음(규범 §4.3). `AlnsConfig.timeLimitSec`은 이 파일을 안 본다.

### 5.3 복잡도 (규범 문서 §5 표 H2 행)

```text
H2 | urgency-regret3 | parallel-regret | 전 패턴 | ≤ n | O(n · m · L²)
```

기호: `n` = 요청 수, `m` = 차량 수, `L` = 경로 평균 길이. "반복당 작업"은 후보 1개 검증 = 경로 전체 재전파 `O(L)`를 곱한 값(규범 §5 머리).

풀이:

- 바깥 ≤ n.
- 매 바깥 바퀴, **남은 요청마다** 기존 경로(≤ m) + 미사용 1대에 삽입 위치를 나열한다.
- 단일 방문(`DELIVERY_ONLY`·`PICKUP_ONLY`)은 위치 `L+1`, 위치마다 전파 `O(L)` → 경로당 `O(L²)`.
- `PICKUP_DELIVERY`는 위치 쌍 `(i ≤ j)`가 `O(L²)`이라 표가 `O(n · m · L³)`로 한 차수 올라간다(규범 §5 주석). 실물 fixture는 `DELIVERY_ONLY` 지배라 표의 `L²`가 맞다.
- 안쪽이 남은 요청 전회 스캔이라, 같은 표기 `O(n · m · L²)`를 쓰는 H4(요청당 1회, 77 ms)보다 비싸다. 실측 644 ms ≈ 8×. n=452배가 아닌 것은 초반 경로 수가 작고, `Cache`가 안 바뀐 차량의 전파를 건너뛰기 때문이다.

H1도 같은 칸 `O(n · m · L²)` · 830 ms. 둘 다 parallel-regret이라 비싸고, H2가 H1보다 짧은 것은 이 입력에서 바깥 바퀴가 조금 덜 돌거나 후보가 더 일찍 0이 된 관측이지 점근 표기의 차이는 아니다.

---

## 6. 코드를 읽으며 눈여겨볼 점

규범 문서와 코드는 1:1이지만, 처음 읽을 때 숫자가 다르게 보이는 지점이다. **수정 제안이 아니라 읽기 보조**다.

1. **첫 바퀴의 regret-3은 전원 0인 경우가 전형이다.** 빈 해에서는 미사용 1대뿐이라 후보 1개. 그때의 승자는 `(등급, RequestId)`다. regret이 살아나려면 기존 경로에 위치가 둘 이상 생겨야 한다.
2. **`candidateCount`는 차량 수가 아니다.** 호환 대수(H1 희소도)도 아니고, "지금 들어가는 경로 수"(H9)도 아니다. `candidates`가 돌려준 **삽입 자리 수**를 3에서 자른 값이다. 한 경로의 위치 3곳이 전부 상위면 count=3이다.
3. **상위 3개의 순서는 `byCost`다.** 기존 경로 Δ 5,000 m가 새 경로 Δ 50 m보다 앞선다(`newRoute`). regret은 그 순서를 유지한 채 Δ거리 필드만 뺀다. 삼각부등식을 깨는 이동표에서는 `(c_k − c1)`이 음수가 될 수 있다 — `addExact`는 음수를 막지 않는다.
4. **등급 0이 비는 입력이 있다.** 최소 폭이 첫 절단값과 같으면(`>=`) 전원 1 이상. 폭이 전부 같으면 전원 3. 1순위 키가 동률이 되어 count·regret3·ID로만 가른다. 가설: 실물의 "하루 종일" 창이 많으면 등급이 한 칸에 몰려, 남는 변별은 후보 수+regret-3이다. 그때도 H1(희소도+regret-2)과 **같은 정책은 아니다**.
5. **PICKUP_DELIVERY의 pickup 창은 등급에 없다.** 싣는 시각이 촉박하고 내리는 시각이 널널하면 등급은 널널 쪽을 따른다. pickup 촉박함은 오라클이 그 요청의 후보를 탈락시키거나 자리를 줄이는 쪽으로만 반영된다.
6. **제외는 재시도하지 않는다.** 후보 0개로 pending을 떠난 요청은 나중에 자리가 생겨도 다시 안 넣는다. parallel-regret의 공통 종료(규칙 b)다. leftover pass가 있는 H23과 다르다.
7. **`loads` 같은 근사 장부가 없다.** 용량·시간창을 이 파일이 세지 않는다. 보이는 숫자는 등급·Δ거리·후보 수뿐이다.
8. **`Problem`은 어디서도 바뀌지 않는다.** `current = apply(...)`로만 갱신한다.

---

## 7. 경계 상황 (규범 §7의 X번호)

이 기법 전용 X[^tx]는 없고 공통 종료 규칙 §4.3만 적용한다. 공통 항목 중 H2 경로에 실제로 닿는 것은 아래다.

| 상황 | 동작 | 번호 |
|---|---|---|
| requests 빈 목록 | `urgencyGrades`가 빈 맵, pending 빈 집합, 즉시 빈 해 | X1 |
| vehicles 빈 목록 / 전 요청 호환 0대 | 매 요청 후보 0개 → 한 바퀴에 전부 excluded → 전부 bank | X2 |
| 한 기법이 0건 삽입 | 정상. 전부 bank인 후보로 포트폴리오에 참여하고 1번 축에서 진다 | X4 |
| 바깥 루프가 n을 넘음 | `IllegalStateException` | X13 |
| profile hard가 강해 어떤 삽입도 통과 못 함 | 전부 bank, Feasible이므로 정상 | X14 |
| 폭이 전부 동일 / n&lt;4 | §4.2 가장자리. 기권하지 않음 | — |
| 후보 1개뿐인 요청과 3개인 요청이 같은 등급 | count ASC가 1개인 쪽을 고른다. regret3=0이어도 이긴다 | §5 H2 문면 |

H2는 기권하지 않으므로 X3(전원 기권)의 경로에 서지 않는다.

---

## 8. 테스트와 실측

이 기법 **전용 T번호·테스트 클래스는 없다.** 포트폴리오 공통 테스트와 캐시 대조가 커버한다.

| 테스트 | 무엇을 고정하나 |
|---|---|
| T13b `InsertionSearchTest.cachedCandidatesMatchFullRecomputation` | `Cache` 상위 3개 = 전체 재계산 상위 3개. H2가 읽는 c1·c2·c3의 전제 |
| T17 `InitialSolutionBuilderTest.everyConstructionResultIsFeasible` | 24개 전부 `Evaluator` Feasible (X5·X14) |
| T18 `InitialSolutionBuilderTest.deterministicAcrossRuns` | 같은 입력 두 번 → 같은 best·score (X8) |
| T19 `ConstructionHeuristicsTest.outerLoopBoundedByRequestCount` | 24개 바깥 루프 ≤ 기법별 상한. H2는 ≤ n (X13) |
| T20 `ConstructionHeuristicsTest.structureHoldsForEveryHeuristic` | pair 원자성·XOR. PD 포함 문제 포함 |
| T25 `InitialSolutionScaleTest.runsPortfolioOnFullScaleSyntheticProblem` | 규모 측정. 기법별 소요·미배정 출력 (한도 아님) |

실물 실측([h1-h24 §1](../initial-solution-heuristics-h1-h24.md), 2026-09-04, 정식 평가). 24개 중 **6위**.

| 기법 | 미배정 | 차량 | 거리(m) | 운행시간(s) | 소요 |
|---|---:|---:|---:|---:|---:|
| H4 `deadline-sequential` (순차 기준선) | 65 | 31 | 3,604,197 | 935,716 | 77 ms |
| **H2 `urgency-regret3`** | **60** | **31** | **3,924,184** | **941,844** | **644 ms** |
| H1 `scarcity-regret2` | 86 | 31 | 2,750,454 | 915,946 | 830 ms |
| H23 `zone-quota-balanced-fill` (실물 1위) | 0 | 31 | 4,198,408 | 1,002,069 | 550 ms |

H1 86 vs H2 60 — 이 fixture에서는 차종 호환보다 시간창 폭이 미배정을 더 잘 가렸다. 거리 축에서 H1이 더 짧은 것은 미배정 26건을 더 남긴 결과라 품질 신호가 아니다(h1-h24 §1 읽는 법).

실행 명령:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env
mvn test -pl solver-core -Dtest=ConstructionHeuristicsTest
mvn test -pl solver-core -Dtest=InsertionSearchTest#cachedCandidatesMatchFullRecomputation
```

---

## 용어 각주

[^cvrptw]: **CVRPTW** (Capacitated Vehicle Routing Problem with Time Windows) — 용량 제한과 시간창이 있는 차량 경로 문제.
    이 저장소가 푸는 **RPDPTW**는 거기에 pickup-and-delivery(한 주문이 싣는 곳과 내리는 곳 두 방문을 가짐)와 실무 제약
    (차급·구역·정차 한도·근무창 등)을 더한 "rich" 변형이다.

[^time-window]: **시간창** — 그 방문을 받아 주는 시각 구간 `(openSec, closeSec)`. 주문·차고·근무 창은 날마다 반복되고
    정규화가 절대 창 목록으로 펼친다. H2의 폭은 마지막 방문 side의 창들에 대해 `Σ(close−open)` 초다.

[^regret]: **regret (후회값)** — 지금 최선 자리의 Δ거리를 c1, 그다음을 c2·c3라 할 때 최선을 놓친 비용.
    H2는 `(c2−c1)+(c3−c1)`. 큰 요청을 먼저 넣는 키로만 쓰고, 실제로 삽입하는 위치는 항상 c1이다.

[^construction]: **construction (heuristic)** — 빈 해에서 시작해 요청을 하나씩 넣어 첫 해를 만드는 기법. 이 뒤에 오는 ALNS가
    그 해를 부수고 다시 고친다. 이 저장소에서는 24개이고 난수를 쓰지 않는다.

[^initial-solution]: **초기해** — ALNS가 출발점으로 삼는 첫 `Solution`. 24개 construction 결과 중 정식 평가 점수가 가장 좋은 것.

[^parallel-regret]: **parallel-regret** — 매 단계에서 남은 요청 **전부**의 최선·차선(·3선)을 다시 계산해 하나를 고르는 계열.
    규범 표의 H1·H2·H9·H10. 반대는 순서를 미리 고정하고 요청당 한 번만 넣는 sequential(H4 등).

[^lexicographic-score]: **사전식 점수** — `long[]`을 앞 원소부터 비교하는 순서. 기본 profile의 축은
    **(미배정 수, 사용 차량 수, 총 거리 m, 총 운행시간 s)** 네 개. 가중합으로 뭉개지 않는다.
    H2 안의 선택 키도 같은 태도 — 등급과 regret을 한 숫자로 섞지 않는다.

[^determinism]: **결정적(deterministic)** — 같은 입력에 항상 같은 출력. 초기해 단계에서 난수를 금지한다.

[^bank]: **bank** — 어떤 경로에도 들어가지 못한 요청 ID의 집합. 모든 요청은 "경로 안" 또는 "bank" 중 정확히 하나(XOR).
    bank에 요청이 남은 해도 유효하다 — 점수의 미배정 축이 나쁠 뿐이다. 문서의 "미배정"과 같은 집합이다.

[^problem]: **`Problem`** — 입력(요청·차량·차고·이동표)을 검증·정규화하고 호환표까지 계산해 **동결**한 객체. 탐색은 읽기만 한다.

[^request]: **`Request`** — 주문 하나. 패턴은 `PICKUP_DELIVERY`(방문 2개), `DELIVERY_ONLY`, `PICKUP_ONLY` 셋 중 하나.
    배정·제거의 원자 단위라 pickup만 따로 옮기는 일은 없다 (pair).

[^profile]: **`Profile`** — 고객별로 다른 hard/soft 제약과 점수 축. core에 `if (customerId == …)`를 두지 않고 이 객체를 갈아 끼운다.

[^compat]: **호환** — 그 요청을 그 차에 실을 수 있는지가 동결 시점에 `compatibleVehicles(requestId)`로 계산돼 있다
    (차급·capability·구역·차고). H1의 1순위 키가 이 집합의 크기이고, H2는 이 값을 선택 키에 넣지 않는다.
    오라클은 비호환 차를 후보에서 뺄 때만 이 표를 본다.

[^oracle]: **오라클** — "이 후보가 유효한가·비용이 얼마인가"를 정확히 답하는 검사기. 여기서는 `InsertionSearch`.
    호환 필터 + 경로 전체 전파[^propagation] + profile hard. H2는 유효성 판정을 전부 위임한다.

[^seed]: **씨앗 (seed)** — 빈 경로를 열 때 맨 처음 싣는 요청. H5·H10·H11이 이 방식으로 경로를 하나씩 완성한다.
    H2는 씨앗을 고르지 않고, 매 단계 남은 전부 중 하나를 기존 경로 또는 새 경로에 넣는다.

[^abstain]: **기권 (abstain)** — 이 기법이 이 `Problem` 구조에서 성립하지 않아 실행하지 않고 건너뛰는 것. 실패가 아니다.
    H2는 어떤 입력에도 기권하지 않는다. 기권 가능한 것은 H5·H7·H8·H19·H20뿐이다.

[^spi]: **SPI (Service Provider Interface)** — 구현체를 여러 개 꽂는 인터페이스. `ConstructionHeuristic` 24개가
    `InitialSolutionBuilder.defaults()` 목록에 우선순위 순으로 들어 있다.

[^hard-soft]: **hard / soft 제약** — hard는 어기면 해가 무효(용량·시간창·정차 한도·구역 단일성·차급), soft는 점수가 나빠질 뿐.
    construction의 삽입은 hard를 통과한 후보만 받는다.

[^units]: **단위** — 무게·부피는 ×1000 FLOOR한 `long`, 거리는 meter, 시간은 초 정수. 창 폭도 초의 차라 `double` 근사를 거치지 않는다.

[^fixture]: **fixture** — 테스트·실측에 쓰는 고정 입력. "실물 fixture"는 `data/win_poc_case_floor.json`
    (주문 452건·차량 31대). 1차 성공 기준의 실행 입력이다.

[^propagation]: **전파 (`RoutePropagator`)** — 방문 순서가 주어지면 차고 출발부터 도착·서비스·적재·정차를 계산하고
    hard를 검사해 Feasible 또는 Infeasible로 답한다. 후보 1개마다 그 경로를 처음부터 다시 전파한다.

[^tx]: **T번호 · X번호** — heuristics 문서의 표 번호. T는 테스트(§8), X는 경계 상황(§7). H2 전용 T/X는 없고
    공통 항목만 가리킨다.
