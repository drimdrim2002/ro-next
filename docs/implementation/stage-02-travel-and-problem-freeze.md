---
title: Stage 2 — 이동표와 Problem 동결 (상세 구현 설계)
stage: 2
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../domain-design.md (§4 이동표 준비, §5 Problem, §3.4 호환성, §12 오류 분류)
  - ../architecture-design.md (§2 모듈·패키지)
  - stage-00-cleanup-and-skeleton.md (§3.1 이름 기준)
  - stage-01-canonical-input-normalization.md (§2 canonical 모델, §3 유틸, §9 미해결 질문)
revisions:
  - 2026-08-09 최초 작성
  - 2026-08-10 `SolveOptions` → `DeliveryPolicy`, `Problem`에 profile 미보관 확정 (Domain §5)
  - 2026-08-10 §7에 규모 테스트 T12 추가 (Plan Stage 2 규모 DoD 대응 — 453² 전 쌍 합성 입력의
    Problem 생성 1회, 시간·메모리 기록). 다른 설계 무변경
  - 2026-08-10 D1 확정 반영 — §8 "multiRotation fixture 충돌 해소" 행을 해소 표시로
    (바퀴 수 확정, fixture 원본 통과). 이 Stage의 설계·파일·테스트는 무변경
  - 2026-08-11 정리 — N4에 비대각 D=9999 1건 기록(실거리/결측 여부 확인은 Plan D2 안건),
    §7 말미 문구를 Plan §1 DoD 편입으로 갱신. 설계 무변경
  - 2026-08-11 Stage 1 유예 반영 — §4 절차 2에서 차고 NodeId 중복 검사 제거(차고는 NodeId를
    갖지 않는다) · §8 `depot.taskTime` 행을 "보류"에서 "canonical에 없음"으로. 설계 무변경
  - 2026-08-12 Domain 2026-08-12 개정(self arc sentinel·startDepot 부재 규칙·수치 number
    인코딩) 정합 — "self arc = 0"을 sentinel D=999,000·U=86,400 강제로 (§2.2 주석·§3 절차
    2·6·E3·E4·T4 개명 `selfArcAlwaysSentinel`), N4·§8의 fixture 수치 인용을 number 표기로
  - 2026-08-13 감사 결함 정정 (분할 6 #3·#5·#8·#9, C-8) — 상단 DoD 요약에 규모 측정(T12)
    추가 · E8 "(fixture 상황)" 오기 교정(fixture는 전 차량 speed 45 — 부재는 합성 케이스) ·
    E19 "(또는 빈 경로)"를 "(또는 경로 0개)"로(빈 visits Route는 Stage 3이 금지) ·
    §7 말미 Plan 인용을 의역 표기로 · §9 서두를 README 공통 규칙(표시하고 남김)으로. 설계 무변경
  - 2026-08-14 Domain `startDepot` optional + `PICKUP_ONLY` — freeze 절차 3을 3패턴 ↔ side로.
    start/end는 **있으면** 차고 집합. E16 확장. start 부재는 실패가 아님
---

# Stage 2 — 이동표와 Problem 동결

solver-core의 `domain` 패키지에 준비 완료된 이동표(`TravelMatrix`, 누락 보정 포함)를,
`problem` 패키지에 동결된 문제(`Problem`)를 만든다. 주 근거: [Domain §4–§5](../domain-design.md).
Stage 1이 확정한 `Plan`·`TravelEntry`·`Location`·`Vehicle`·`DeliveryPolicy`·`Compatibility` 등을
그대로 잇는다 — 같은 개념에 새 이름을 짓지 않는다.

**`Problem`은 profile도 탐색 예산도 담지 않는다** (Domain §5, 2026-08-10 확정). 그래서
`freeze`의 시그니처는 `freeze(Plan)` 하나이고 이후 Stage가 인자를 추가하지 않는다.

**DoD** ([Plan Stage 2](../implementation-plan.md)): 이동표 보정 규칙 테스트 ·
Problem 생성 후 불변성(문제 쪽 mutator 부재) 확인 · 규모 측정(T12 — 453² 합성 입력 1회,
소요 시간·메모리 기록).

핵심 구도 — Stage 1의 출력이 여기서 "풀 수 있는 문제"로 확정된다:

```text
[Plan (Stage 1 정규화 완료)] ──▶ Problem.freeze ──▶ [Problem (읽기 전용)]
                                  │                    ├ requests/vehicles/depots/locations
                                  ├ 참조·pair 검증       ├ TravelMatrix (모든 쌍 D·U 완비)
                                  ├ TravelMatrix.prepare ├ 호환성 사실 (RequestId→호환 VehicleId)
                                  └ 호환성 사전 계산      └ DeliveryPolicy (Domain §2.5)
                                  실패 시 ProblemCreationException → 상태 FAILED (Domain §12)
```

- 탐색(Stage 4)·재검증(Stage 5)은 이 `Problem`을 **읽기만** 한다 (Domain §5 MUST).
- 이동표 조회 키는 `LocationId` 쌍(방향 있음)이고, 준비된 표만 사용한다 —
  탐색 중 좌표 즉석 계산·대칭화 금지 (Domain §4 MUST/MUST NOT).

---

## 1. 파일/클래스 목록

전부 `solver-core/src/main/java/com/ronext/rpdptw/` 아래 (Architecture §2, Stage 0 §3.1).
`package-info.java`(Stage 0 생성)는 유지한다.

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `domain/GreatCircle.java` | 두 `Location` 사이 Great Circle 거리 → 정수 meter (HALF_UP) | Domain §4 |
| `domain/TravelMatrix.java` | 준비 완료된 불변 이동표: 검증·누락 보정·동결 + O(1) 조회 | Domain §4 |
| `problem/Problem.java` | 동결된 문제 묶음: `freeze` 팩토리 + 읽기 전용 조회 | Domain §5 |
| `problem/ProblemCreationException.java` | Problem 생성 실패 신호 (FAILED 원인, 4xx 아님) | Domain §12 |

테스트 파일은 §7. 이동표 준비가 `domain`에 있는 이유: Stage 0 package-info가
"domain = canonical 입력·정규화·이동표 (Domain §1–4)"로 고정했다.

---

## 2. 시그니처

전체 구현 본문은 쓰지 않는다 — 여기 시그니처가 계약이다.

### 2.1 `GreatCircle` (Domain §4)

```java
public final class GreatCircle {
    /** haversine, 지구 반지름 6_371_000 m. meter 값을 HALF_UP으로 정수화 (Domain §4). */
    public static int distanceMeter(Location a, Location b);
}
```

- 좌표는 `Location`의 double이다. Domain §3.1의 double 금지는 **단위 환산(kg·CBM) 경로**의
  규칙이고, 좌표 기하 계산은 그 대상이 아니다. 결과는 정수 meter로만 밖에 나간다.
- 반지름·공식은 "기존 유지"(Domain §4)이나 구 코드가 Stage 0에서 삭제되어 원본 상수를
  확인할 수 없다 → 잠정 확정값 (§9 Q2). 보정값은 표에 동결되므로 탐색↔재검증 불일치는
  구조적으로 불가능하다 — 위험은 Stage 8 Win 지표 비교뿐이다.

### 2.2 `TravelMatrix` (Domain §4)

```java
public final class TravelMatrix {

    /**
     * 준비 = 검증 + 누락 보정 + 동결. locations의 모든 장소 쌍에 대해 D·U를 확정한다.
     * 실패(중복 arc, overflow, speed ≤ 0 등)는 IllegalArgumentException — Problem.freeze가
     * ProblemCreationException으로 번역한다 (§5 설계 노트 N2).
     */
    public static TravelMatrix prepare(Map<LocationId, Location> locations,
                                       List<TravelEntry> entries,
                                       Set<Integer> resolvedSpeedsKmH);

    public int distanceMeter(LocationId from, LocationId to);              // 방향 있음. self = sentinel 999,000
    public int timeSec(LocationId from, LocationId to, int resolvedSpeedKmH);
    public Set<LocationId> locationIds();                                  // 표가 아는 장소 전체
}
```

- `timeSec`의 `resolvedSpeedKmH`: **보정된 arc에만** 영향 있다. 입력으로 주어진 arc의 U는
  차량과 무관하게 같다 (측정값이므로). 근거와 잠정성은 §3 절차 5·§9 Q1.
- 조회 규칙: 준비에 없던 `LocationId`·speed로 조회하면 `IllegalArgumentException`
  (조용한 오답 대신 조기 실패). `prepare` 이후 내부 상태 변경 수단은 없다.
- 내부 표현은 구현 재량 (예: `LocationId`→index 맵 + dense `int[n*n]` D·U, 보정 arc의
  speed별 값만 별도 보관). 요구는 "준비 후 불변 + O(1) 조회"뿐이다.
  참고 규모: floor fixture가 453 장소 = 205,209 쌍 → dense int 배열 2개 ≈ 1.6 MB.

### 2.3 `Problem` (Domain §5)

```java
public final class Problem {

    /** 검증 + 이동표 준비 + 호환성 사전 계산 + 동결. 실패 시 ProblemCreationException. */
    public static Problem freeze(Plan plan);

    // ---- 이하 전부 읽기 전용. mutator는 존재하지 않는다 (Domain §5 MUST) ----
    public String planId();
    public Optional<String> customerId();          // app이 profile resolve에 사용 (Domain §8.4)
    public TimeBase timeBase();
    public long planEndSec();
    public DeliveryPolicy deliveryPolicy();        // Domain §2.5 — 유효한 답을 바꾸는 값만
    public List<Depot> depots();                   // unmodifiable
    public List<Request> requests();               // unmodifiable
    public List<Vehicle> vehicles();               // unmodifiable
    public Map<LocationId, Location> locations();  // unmodifiable
    public TravelMatrix travel();

    public Request request(RequestId id);          // 미존재 → IllegalArgumentException
    public Vehicle vehicle(VehicleId id);          //   〃
    public Depot depotAt(LocationId id);           //   〃
    public int resolvedSpeedKmH(VehicleId id);     // §3 절차 5의 체인 결과 (freeze 시 확정)
    public Set<VehicleId> compatibleVehicles(RequestId id);  // §3.4 사실 — unmodifiable, ∅ 가능
}
```

- record가 아니라 class다: 파생 색인(ID 맵·호환성 맵)을 숨기고, 생성 경로를 `freeze` 하나로
  강제하기 위해서다. 모든 필드는 final, 컬렉션은 `freeze`에서 방어 복사해 unmodifiable로 보관.
- 새 입력 = 새 `freeze` 호출 = 새 `Problem` (Domain §5 규칙 3). 기존 인스턴스 수정 경로 없음.

### 2.4 `ProblemCreationException` (Domain §12)

```java
public final class ProblemCreationException extends RuntimeException {
    public ProblemCreationException(String message);
    public ProblemCreationException(String message, Throwable cause);
}
```

- `InputException`(접수 4xx 축, Stage 1)과 다른 분류다: Problem 생성 실패는 이미 접수된
  풀이를 **FAILED 상태 + 원인**으로 남긴다 (Domain §12 3행). 상태 기록은 Stage 6 executor 몫.

---

## 3. 이동표 준비 절차 (`TravelMatrix.prepare`)

번호 순서대로. Domain §4의 규칙을 준비 시점에 전부 끝낸다 — 이후는 조회만.

```text
1. 색인   locations 키 집합에 index 부여 (n개). 표의 장소 전체집합은 locations 맵이
         권위다 (Domain §5 — LocationId 목록과 이동표가 함께 동결).
2. 입력   entries 순회:
         - from 또는 to가 locations 밖 → 그 항목은 버린다 (§6 E6 — 문제의 장소가 아니면
           어떤 방문도 그 arc를 쓸 수 없으므로 오류가 아니다).
         - from == to (self arc) → 값을 읽지 않고 버린다. self는 절차 6의 sentinel이 이긴다 (§6 E3).
         - 같은 (from,to)가 두 번 → 실패 (값이 달라도 같아도 — 추측 금지, Domain §2.1).
         - 채택된 항목의 D·U는 그대로 표에 놓는다. 역방향은 별개 arc다 —
           대칭화 금지 (Domain §4 MUST NOT).
3. D 보정 모든 쌍 (i,j), i≠j 중 D가 없는 곳:
         D := GreatCircle.distanceMeter(loc_i, loc_j)   // HALF_UP (Domain §4)
4. U 보정 U가 없는 곳 (입력 항목은 D·U를 항상 함께 가지므로 = 절차 3에서 보정된 쌍):
         U := ceil(D × 3.6 / speed)                     // Domain §4
         정수 연산으로: Math.ceilDiv(18L * D, 5L * speed)   // 3.6/speed = 18/(5·speed), double 경유 없음
         resolvedSpeedsKmH의 speed마다 각각 준비한다 (절차 5). int 범위 초과 → 실패 (§6 E13).
5. speed  체인(Domain §4 주석 "speed 없으면 defaultSpeed, 그것도 없으면 45")의 해석:
         resolvedSpeed(차량) = vehicle.speedKmH ▷ deliveryPolicy.defaultSpeedKmH ▷ 45
         이 계산은 Problem.freeze가 하고(§4 절차 4), prepare는 distinct 집합만 받는다.
         speed ≤ 0이 섞여 있으면 실패 (÷0 방지 — §6 E10). 주어진 arc의 U는 speed 무관 공유.
6. self   모든 (i,i): D = 999,000, U = 86,400 (전 speed — sentinel, Domain §4 2026-08-12
         확정. 0이면 자기 순환이 공짜, Long.MAX_VALUE류는 addExact overflow). 입력이 준
         self 값(floor fixture는 453건 전부 D=9999·U=0)은 읽지 않고 버린다 — 근거는 §6 E3.
         §5 규칙 4의 완전성 검사에서 self arc는 누락으로 세지 않는다 (여기서 항상 채워진다).
7. 동결   불변 구조로 확정. 이후 어떤 경로로도 값이 바뀌지 않는다.
```

**`distanceTimeCalculate`는 canonical에 존재하지 않는다 (2026-08-10 확정).** 이 값은 준비
규칙을 바꾸지 않는다 — §4의 규칙 자체가 mode 무관이고, 규약 PDF의 의미("GreatCircle =
직선거리로 계산")는 위 보정 규칙이 이미 포섭한다 (행렬이 없으면 전 arc가 누락이라 전부
Great Circle로 채워진다). 아무도 읽지 않는 값을 canonical에 보관하지 않기로 했으므로
`TravelCalcMode` enum과 `SolveOptions`의 해당 필드는 만들지 않는다 (Domain §2.5.1,
Stage 1 §2.2). 결과 run 메타에 남길지는 Stage 6 재량이며, 그 경우 wire 원문에서 직접 읽는다.

---

## 4. Problem 동결 절차 (`Problem.freeze`)

번호 순서대로. 첫 위반에서 `ProblemCreationException`으로 중단한다 (Domain §5 규칙 4 —
실패하면 풀이를 시작하지 않는다). Stage 1의 `PlanNormalizer`가 이미 거른 것도 일부 다시
검증한다 — `Plan`은 public 타입이라 테스트·다른 경로로 직접 조립될 수 있기 때문이다 (방어).

```text
1. 복사   depots/requests/vehicles/locations를 방어 복사 (List.copyOf 등).
         이후 원본 Plan을 고쳐도 Problem은 변하지 않는다 (§7 T8).
2. ID    RequestId·VehicleId 중복 없음. 전 NodeId(모든 side) 중복 없음.
         (차고는 NodeId를 갖지 않는다 — Stage 1 §2.2, Stage 3 §4.4)
         위반 → 실패 (Domain §5 "ID 참조").
3. 참조   pair 참조 (Domain §5·§1.3):
         - DELIVERY_ONLY   ⇔ pickup 부재 ∧ delivery 존재
         - PICKUP_ONLY     ⇔ pickup 존재 ∧ delivery 부재
         - PICKUP_DELIVERY ⇔ pickup 존재 ∧ delivery 존재
         불일치 → 실패.
         모든 side·depot의 locationId ∈ locations 맵. 아니면 → 실패.
         vehicle.startDepot·endDepot(**있으면**) ∈ 차고 LocationId 집합. 아니면 → 실패.
         부재는 실패가 아니다 (첫 고객 시작 / 마지막 고객 종료 — Domain §2.4).
4. speed  차량별 resolvedSpeedKmH 확정 (§3 절차 5 체인). ≤ 0 → 실패.
         distinct 집합을 만들어 5로 넘긴다. VehicleId→speed 맵은 Problem이 보관.
5. 이동표 TravelMatrix.prepare(locations, plan.travelEntries, speeds).
         IllegalArgumentException → ProblemCreationException으로 번역(원인 보존, 노트 N2).
         완전성(Domain §5)은 이 시점에 성립한다: (a) 참조 장소가 전부 locations에 있고(절차 3),
         (b) prepare가 모든 쌍을 채웠다(§3). 이후 조회 실패는 곧 버그다.
6. 호환   호환성 사실 사전 계산 (Domain §5 "호환성 사실 (§3.4)"):
         requests × vehicles에 Compatibility.compatible(v, r) (Stage 1 §5) 적용 →
         Map<RequestId, Set<VehicleId>> (unmodifiable). ∅도 정상 — 오류 아님 (§6 E18).
         fixture 규모 452×31 = 14,012회 — 사전 계산 비용 무시 가능.
7. 조립   Problem 인스턴스 반환. profile은 `Problem`에 담지 않는다 (Domain §5 MUST NOT) —
         `customerId`만 보관하고, resolve와 전달은 app(Stage 6)이 한다.
         **이후 어느 Stage도 `freeze`에 인자를 추가하지 않는다.**
```

---

## 5. 설계 노트

| # | 내용 |
|---|---|
| N1 | `distanceTimeCalculate` 비분기 — §3 끝 문단. Domain §2.5.1이 canonical 제거로 확정 (§9 Q3 해소) |
| N2 | 예외 번역 seam: `domain`(TravelMatrix)이 `problem`의 예외를 던지면 역방향 패키지 참조가 생긴다. 그래서 prepare는 `IllegalArgumentException`, freeze가 `ProblemCreationException`으로 감싼다. 패키지 의존은 `problem → domain` 한 방향 유지 |
| N3 | 부분 항목(D만 있고 U 없음)은 Stage 2에 도달하지 않는다 — Stage 1 `TravelEntry`가 두 필드 필수 int라서다. wire가 부분 항목을 주면 Stage 6 adapter의 일 (Stage 1 절차 7 단위 검증에서 거부 권고) |
| N4 | fixture 사실 (2026-08-09 확인): floor fixture 행렬은 453 장소 완전 정방(453² = 205,209행, 중복 0)이라 보정이 한 번도 안 돈다. self arc 453건 전부 D=9999·U=0 (읽지 않고 버린다 — 절차 2·6). 전 차량 speed 45, `Optimizer.DefaultSpeed` 45 (수치 표기는 2026-08-12 number 정정 반영 — Domain §3.1), `distanceTimeCalculate` "GreatCircle". `C` 열(O/G)은 Stage 1이 구조적으로 배제. **비대각 arc 1건이 D=9999다** (`WIN_2306→WIN_3225`, U=991 — 2026-08-11 실측): wire의 self arc 대각 값(9999)과 같아 결측 표시의 누출이 의심되나, 규칙상 실거리 9999 m로 그대로 쓰인다. 실거리/결측 확인은 Plan D2 안건 (Stage 8 W16) |

---

## 6. Edge case 표

Domain §4·§5의 규칙·경계값·오류 분류에서 뽑았다. "실패" = `ProblemCreationException`
(prepare 내부에서는 `IllegalArgumentException` → 번역, 노트 N2).

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | (from,to) 항목 있음, 역방향 없음 | 정방향은 그대로, 역방향은 보정 — 대칭화 금지 | §4 MUST NOT |
| E2 | 쌍 자체가 행렬에 없음 | D = Great Circle(HALF_UP), U = ceil 체인 | §4 |
| E3 | self arc 입력 D=9999·U=0 (floor fixture 453건 전부) | 값을 읽지 않고 버린다 — 표의 self는 항상 sentinel D=999,000·U=86,400 (입력값 무관. 소수 거부의 검사 대상도 아니다). 종전 "self arc = 0" 규정은 2026-08-12 폐기 — 0이면 자기 순환이 공짜, 극단값은 addExact overflow | §4 (2026-08-12 확정) |
| E4 | self arc 입력 없음 | sentinel 999,000/86,400 (규칙값 — GC 보정 호출 없음. 있어도 없어도 된다) | §4 |
| E5 | 같은 (from,to) 항목 중복 | 실패 (값 충돌이면 추측 금지, 동일 값이어도 거부해 결정성 단순화) | §2.1 |
| E6 | locations 밖 장소의 항목 (여분 행) | 버림 — 표의 장소 전체집합은 locations 맵이 권위 | §5 |
| E7 | locId 오타로 행렬 전체가 밖 → 전부 보정 | 수용 (규칙상 관측 불가) — GC로 채워져 풀이는 진행. 조용한 품질 저하 가능성은 Stage 8 지표 비교에서 드러남 | §4 |
| E8 | 차량 speed 부재 + defaultSpeed=45 (합성 케이스 — fixture는 전 차량 `speed: 45`라 부재가 없다, N4. defaultSpeed 45는 fixture 값) | resolved 45 | §4 체인 |
| E9 | 차량 speed·defaultSpeed 둘 다 부재 | resolved 45 (고정 fallback) | §4 체인 |
| E10 | resolved speed ≤ 0 | 실패 (÷0 방지 — §4 식의 전제) | §4 |
| E11 | 서로 다른 speed 차량 혼재 | speed별로 보정 U 준비, 주어진 arc U는 공유 | §4·Stage 1 §2.2 `Vehicle.speedKmH` 주석 (§9 Q1) |
| E12 | ceil 경계: D=1000·s=45 → 80초, D=1001·s=45 → 81초 | `ceilDiv(18L·D, 5L·s)` 정수식 (double 경유 없음) | §4·§3.1 |
| E13 | 보정 U가 int 범위 초과 | 실패 (overflow 검사) | §3.1 |
| E14 | 서로 다른 두 장소인데 입력 D=0 | 허용 (금지 규칙 없음 — 동일 건물 별개 locId 등) | §4 |
| E15 | 같은 좌표의 두 장소를 GC 보정 | D=0 → U=0 | §4 |
| E16 | 패턴 ↔ side 불일치 (Plan 직접 조립): DELIVERY_ONLY인데 pickup 있음, PICKUP_ONLY인데 delivery 있음, PICKUP_DELIVERY인데 한쪽 없음, 둘 다 없음 | 실패 (pair 참조) | §1.3·§5 |
| E17 | startDepot/endDepot이 **있는데** 차고 목록 밖 | 실패 (ID 참조) — 정상 경로에선 Stage 1이 선차단, freeze는 방어 재검증. 부재는 통과 | §5 |
| E18 | 호환 차량 0대인 Request | 통과 — `compatibleVehicles` = ∅로 동결. 미배정+사유는 Stage 5 | §3.4 |
| E19 | vehicles 또는 requests가 빈 목록 | 통과 — 전부 bank(또는 **경로 0개**)인 해로 풀이 진행. 빈 visits의 `Route`가 아니다 — 그건 Stage 3이 금지한다 (미사용 차량 = Route 부재, Stage 3 E30) | §3.4 유추 (금지 규칙 없음) |
| E20 | RequestId 중복 등으로 NodeId 충돌 | 실패 | §5 |

---

## 7. 테스트 목록 — DoD 1:1 대응

위치: `solver-core/src/test/java/com/ronext/rpdptw/domain/`(T1–T7)·`…/problem/`(T8–T11).
의존은 JUnit만. fixture 실값(453², self 9999, speed 45)은 손으로 옮겨 쓴다 (Stage 1 §7과 동일 원칙).

| # | 테스트 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| T1 | `GreatCircleTest.knownDistances` | 같은 좌표 → 0. 같은 경도에서 위도 1° 차 → 111,195 m (R=6,371,000·HALF_UP 검증값) | "이동표 보정 규칙 테스트" |
| T2 | `TravelMatrixTest.correctsMissingDistanceByGreatCircle` | E2: 누락 쌍 D = GC. 주어진 쌍은 GC와 달라도 그대로 (§3 절차 2) | 〃 |
| T3 | `TravelMatrixTest.correctsMissingTimeByCeilFormula` | E12 경계값 두 건 + U는 보정 D 기반 | 〃 |
| T4 | `TravelMatrixTest.selfArcAlwaysSentinel` | E3·E4: self 입력 D=9999·U=0(fixture 값)이어도, 없어도 조회는 999,000/86,400 | 〃 |
| T5 | `TravelMatrixTest.keepsGivenArcsAsymmetric` | E1: D[a→b] ≠ D[b→a] 유지, 역방향만 보정 | 〃 |
| T6 | `TravelMatrixTest.rejectsDuplicateAndDropsForeignEntries` | E5 실패 / E6 버림 (버린 뒤 그 쌍은 보정값) | 〃 |
| T7 | `TravelMatrixTest.perSpeedCorrectedTimes` | E8·E9·E11: 체인 결과별 U, 주어진 arc는 speed 무관 동일. 미준비 speed·장소 조회 → 예외 | 〃 |
| T8 | `ProblemFreezeTest.immutableAfterFreeze` | 반환 컬렉션 전부 unmodifiable(수정 시 예외) + 원본 Plan 리스트 변조가 Problem에 안 비침(방어 복사) + reflection으로 `Problem`·`TravelMatrix` public 메서드에 set*/add*/remove*/put* 부재 확인 | "Problem 생성 후 불변성(문제 쪽 mutator 부재) 확인" |
| T9 | `ProblemFreezeTest.validatesReferencesAndPairs` | E10·E16·E17·E20 전부 `ProblemCreationException` | (Plan 범위 문장 "생성 시 참조 검증") |
| T10 | `ProblemFreezeTest.freezesCompatibilityFacts` | E18: ∅ 포함, 결과가 `Compatibility.compatible` 전수 대조와 일치 | (Plan 범위 문장 — Domain §5 호환성 사실) |
| T11 | `ProblemFreezeTest.travelCompleteAfterFreeze` | 희소 입력 후 n² 전 쌍 조회 성공 (완전성) | (Plan 범위 문장 "완전성 검증") |
| T12 | `ProblemScaleTest.freezesFullScaleSyntheticProblem` | **규모 측정.** 실물과 같은 규모의 합성 입력(장소 453 = 주문 452 + 차고 1, 차량 31, 이동표 **453² = 205,209쌍을 전부 채워서**)으로 `TravelMatrix.prepare` + `Problem.freeze` 1회 → 예외 없이 완료. **소요 시간·힙 사용량을 출력해 기록한다.** 쌍을 비우면 Great Circle 보정이 대신 채워 측정이 무의미해지므로 전 쌍을 준다. 입력은 프로그램으로 조립한다 (fixture JSON 파싱 없음 — 위 서두 원칙, app 모듈 불필요) | Plan Stage 2 "**규모**" 문장 |

T9–T11은 Plan DoD 요약 문장 밖이지만 Plan Stage 2 범위 문장(취지 — `Problem` 생성 시
참조 일관성·완전성 검사 후 동결)의 직접 검증이다 — Plan §1의 편입(2026-08-11)에 따라
이 표 전부가 완료 기준이다.

---

## 8. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| `customerId` → profile resolve·전달 — Stage 2는 `customerId` 보관까지 | Stage 6 (조립) | Domain §5·§8.4 — `Problem`에 담지 않는다 |
| 이동표를 실제로 소비하는 전파·시간 계산 | Stage 3 | Domain §7 |
| `Solution`·bank·XOR | Stage 3 | Domain §6 |
| 미배정 사유 산출 (`NO_COMPATIBLE_VEHICLE` 등 — 호환성 ∅ 사실은 여기서 동결만) | Stage 5 | Domain §11 |
| wire `distanceMatrix`(F/T/D/U/C, 문자열·정수 혼재) 파싱 → `TravelEntryInput` 매핑 | Stage 6 adapter | Architecture §2, Stage 1 §8 |
| `ProblemCreationException` → status FAILED 기록 | Stage 6 executor | Architecture §3.2 |
| ~~multiRotation fixture 충돌 해소~~ | **해소 — 결정 불필요** | Plan §2.1 D1 (2026-08-10): 바퀴 수 확정으로 fixture `1`이 그대로 통과. 판정은 Stage 1이 소유 |
| depot.taskTime | **canonical에 없다** — 복귀 선적 시간이라 1바퀴엔 미발생 (Domain §2.5, 2026-08-11) | Stage Extra E1 |
| 이동표 메모리 최적화(압축·공유 표현)의 확정 | 구현 재량 | §2.2 (요구는 불변+O(1)뿐) |
| 시간창 전개(다일 근무창·차고 창)의 재계산·파생 색인 | **안 함 — 이 Stage는 무변경** | Domain §3.2 |

**D4(다일 근무창 + 차고 시간창, 2026-08-10 확정)는 이 Stage를 바꾸지 않는다** — 음성 결과를
남겨 둔다. 시간창은 정규화(Stage 1)가 이미 절대 창 목록으로 펼쳐 `Vehicle`·`Depot`·`RequestSide`
안에 담아 오므로, `freeze(Plan)` 시그니처·검증 절차·`TravelMatrix`·조회 메서드가 모두 그대로다.
창 목록은 그 record들과 함께 동결돼 탐색·재검증이 같은 값을 읽는다 (Domain §5). 창을 해석하는
코드는 여기가 아니라 전파(Stage 3)·재검증(Stage 5)에 있고, 둘은 그것을 각자 구현한다
(Stage 3 노트 N8).

---

## 9. 미해결 질문

닫힌 질문은 해소 표시를 달아 남긴다 ([README](README.md) 공통 규칙, 2026-08-13) —
이 절의 미결은 Q1·Q2다. Stage 2 구현은 각 항목의 "잠정 처리"로 진행한다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | U 보정의 speed 차원 — Domain §4는 "키 = `LocationId` 쌍"(차량 무관)이라면서 보정식의 speed는 `speedKmH`(차량 필드, Stage 1 §2.2 주석도 "Stage 2의 U 보정 체인 입력")를 가리킨다. 차량마다 speed가 다르면 보정 U가 한 값일 수 없다 | 본 문서 설계대로 resolved speed별 사전 준비(§3 절차 5, `timeSec`의 speed 인자). 현행 fixture는 전 차량 45라 실질 무영향. Domain §4에 speed 차원 한 줄 보완 요청 |
| Q2 | Great Circle "기존 유지"의 기존 공식·반지름 — 구 코드가 Stage 0에서 삭제되어 확인 불가 | haversine, R=6,371,000 m, HALF_UP으로 잠정 확정 (§2.1). 탐색↔재검증 불일치는 표 동결로 원천 차단되므로 위험은 Stage 8 Win 지표 차이뿐 — 그때 필요하면 상수만 교정 |
| ~~Q3~~ | `TravelCalcMode`(`distanceTimeCalculate`)의 역할 | **해소됨 (2026-08-10).** 준비 동작 비분기가 확인됐고(노트 N1), 아무도 읽지 않는 값이므로 canonical에서 제거했다 (Domain §2.5.1). enum·필드 모두 만들지 않는다 |
