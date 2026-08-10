---
title: Stage 3 — Solution·전파·평가 (상세 구현 설계)
stage: 3
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../domain-design.md (§6 Solution, §7 전파, §8 평가, §5 Problem, §12 오류 분류)
  - ../architecture-design.md (§2 모듈·패키지, §2.1 경계 규칙, §2.2 profile SPI 확정)
  - stage-00-cleanup-and-skeleton.md (§3.1 이름 기준)
  - stage-01-canonical-input-normalization.md (§2 canonical 모델, §9 미해결 질문)
  - stage-02-travel-and-problem-freeze.md (§2.3 Problem, §8 인계 사항)
revisions:
  - 2026-08-09 최초 작성 (`profile` 단일 패키지, `Comparator<Evaluation>`)
  - 2026-08-10 3계층 반영 — `profile` 패키지를 core `eval` + `solver-profile` 모듈로 분할,
    `Problem`에 profile 미보관, comparator SPI 제거 후 `long[] score` + `Scores.compare`
---

# Stage 3 — Solution·전파·평가

solver-core의 `solve` 패키지에 배차안(`Solution`)·구조 검사·전파·평가를, `eval` 패키지에
평가 계약(사실 값·metric·profile SPI·기본 구현)을, **`solver-profile` 모듈**에
`ProfileRegistry`를 만든다. 주 근거: [Domain §6–§8](../domain-design.md).
Stage 1의 canonical 모델(`Request`·`RequestSide`·`Vehicle`·`NodeId` 등)과 Stage 2의
`Problem`(`travel()`·`resolvedSpeedKmH()`·`compatibleVehicles()`)을 그대로 잇는다.

**DoD** ([Plan Stage 3](../implementation-plan.md)): Domain §7.2 숫자 예를 그대로 재현하는
테스트 · XOR 위반·hard 위반 검출 테스트 · 미등록 customerId → default profile 테스트.

핵심 구도 — Domain §6.5의 순서(구조 검사 → 정식 평가)를 타입으로 고정한다:

```text
[Problem (Stage 2 동결)] + [Profile (인자로 전달 — Domain §5 MUST NOT: 담지 않는다)]
        │
[Solution = routes + bank]  ──StructureCheck──▶  pair·XOR 위반 목록 (비면 통과, §6.3)
        │
        ▼ (구조 통과가 전제)
[Evaluator]
   ├─ RoutePropagator: 경로마다 §7.1 물리 계산 ──▶ RouteFacts (§7.3 기록 값)
   │     └ hard 위반 → Infeasible — 점수로 덮지 않음 (§7.5 MUST NOT)
   ├─ 호환성 검사 (§3.4 동결 사실) · profile hard (§8.4)
   ▼
[Evaluation (중립 metric, ③)]
        │
[profile.score(...) → long[] (④)] ── Scores.compare (사전식, ⑤ §8.3) ──▶ 두 해 중 승자
```

Domain §8.1의 층 ③④⑤가 각각 `Evaluation` · `Profile.score` · `Scores.compare`에 1:1 대응한다.
비교기 SPI는 없다 — 비교 방식은 사전식 하나뿐이고, **무엇을 비교할지만 profile이 정한다.**

---

## 1. 파일/클래스 목록

§1.1–§1.3은 `solver-core/src/main/java/com/ronext/rpdptw/` 아래,
§1.4는 `solver-profile/src/main/java/com/ronext/rpdptw/profile/` 아래다
(Architecture §2, Stage 0 §3.1). `package-info.java`(Stage 0 생성)는 유지한다.

### 1.1 `solve` — Solution·구조 검사·전파·평가

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `solve/Route.java` | 차량 하나의 방문 순서 (SoT, 고객 방문 `NodeId`만) | Domain §6.1·§6.4 |
| `solve/Solution.java` | 배차안: routes + bank (SoT 전체, 불변) | Domain §6 |
| `solve/StructureCheck.java` | pair·XOR·참조 규칙 검사 → 위반 목록 | Domain §1.4·§6.3 |
| `solve/StructureViolation.java` | 구조 위반 한 건 (종류 + 대상 ID) | Domain §6.3·§12 |
| `solve/RoutePropagator.java` | 경로 하나의 §7.1 전파 루프 (물리 계산 + hard 판정) | Domain §7 |
| `solve/PropagationResult.java` | 전파 결과: `RouteFacts` 또는 위반 | Domain §7 |
| `solve/Violation.java` | hard 위반 종류 enum | Domain §6.2·§7.1 |
| `solve/Evaluator.java` | 해 전체 정식 평가: 전파 → 호환·profile hard → `Evaluation`(③) → `score`(④) | Domain §8 |
| `solve/EvaluationResult.java` | 평가 결과: `Evaluation`+`score`+경로 사실, 또는 위반 | Domain §8.1 |

### 1.2 `eval` (core) — 평가 계약·SPI·기본 구현

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `eval/VisitFacts.java` | 방문 하나의 전파 사실 (도착·서비스·적재) | Domain §7.3 |
| `eval/RouteFacts.java` | 경로 하나의 전파 사실 묶음 + `routeOperationalTime` | Domain §7.3·§7.4 |
| `eval/Evaluation.java` | 해 하나의 중립 metric(층 ③) + 집계 팩토리 | Domain §8.1–§8.2 |
| `eval/HardConstraint.java` | profile이 추가하는 hard 제약 한 건 (SPI) | Domain §8.4 |
| `eval/Profile.java` | 고객별 구성 SPI: hard 목록 · score 축(층 ④) · id | Domain §8.4, Architecture §2.2 |
| `eval/DefaultProfile.java` | 기본 profile: 추가 hard 없음 + §8.3 예의 4축. **상속 대상 (final 아님)** | Domain §8.3–§8.4 |
| `eval/Scores.java` | `long[]` 사전식 비교(층 ⑤) — 유일한 비교 수단 | Domain §8.3 MUST |

**왜 `solve`가 아니라 `eval`인가** (설계 노트 N1): 재검증(Stage 5)이 `solve`를 참조할 수
없는데(ArchUnit) 같은 사실 값·같은 공식·같은 profile SPI를 써야 하기 때문이다. 임의 배치가 아니다.

**왜 core에 남는가**: 이들은 **계약과 기본 구현**이지 고객 정책이 아니다. `Problem`·`Solution`을
다루는 core 코드가 직접 쓰므로 core 밖으로 나갈 수 없다. 고객마다 늘어나는 것은 §1.4뿐이다.

### 1.3 `problem` — Stage 2 산출물 확장

| 파일 | 조치 | 근거 |
|---|---|---|
| `problem/Problem.java` | **수정**: `nodeRef()` 추가. `freeze` 시그니처는 **그대로** — profile을 담지 않는다 | Domain §5 MUST NOT |
| `problem/NodeRef.java` | **신규**: `NodeId` 역참조 (어느 Request의 어느 쪽인지) | Domain §1.2 |

### 1.4 `solver-profile` 모듈 — 고객 정책

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `profile/ProfileRegistry.java` | `customerId → Profile` 코드 맵, 미등록·부재 → default | Domain §8.4 MUST |
| (고객별 패키지) | 현재 **0개**. 첫 고객이 확정되면 `DefaultProfile` 상속 또는 `HardConstraint` 구현으로 추가 | Domain §8.4 |

- 이 모듈만 고객 전용 외부 라이브러리를 선언할 수 있다 (Architecture §2.1).
- `DefaultProfile`이 core에 있고 registry가 여기 있는 것은 의도적이다 —
  **core는 "표준 목적식"을, 이 모듈은 "그로부터의 이탈"을 소유한다.**

---

## 2. `Solution`과 구조 검사 (Domain §6)

### 2.1 SoT 시그니처

record는 불변이고 compact constructor에서 방어 복사(`List.copyOf`/`Set.copyOf`)만 한다.
pair·XOR 규칙 검사는 생성자가 아니라 `StructureCheck` 한 곳이 소유한다 (위반을 던지지 않고
**목록으로 보고** — DoD의 "검출"이 테스트 가능해야 하고, Stage 4 trial 루프가 결함을 버그
신호로 다뤄야 하기 때문).

```java
public record Route(VehicleId vehicleId, List<NodeId> visits) {
    // visits: 고객 방문 NodeId만, 순서 = 방문 순서. 비어 있으면 IllegalArgumentException —
    // 미사용 차량은 Route가 없는 것이다 (§8.2 usedVehicleCount의 근거).
}

public record Solution(List<Route> routes, Set<RequestId> bank) {
    // bank는 RequestId만 담는다 — 사유·비용 저장 금지 (Domain §6.3 MUST NOT).
}
```

- **depot는 `visits`에 없다.** 출발/도착 차고는 `Problem.vehicle(id)`의
  `startDepot`/`endDepot`에서 전파가 읽는다 (Stage 1이 trips 접기를 끝냈으므로 — Stage 1 §4
  절차 5). 경로 중간 depot 재방문(Domain §6.2 금지)은 **표현 자체가 불가능**하다 (노트 N4).
- `Solution`은 SoT만 담는다 (Domain §6.4). 도착 시각·load·점수 등 파생은 전파·평가의 출력이고
  `Solution`에 저장하지 않는다.
- record 불변성이 Domain §6.5의 의도(확정 해 제자리 수정 금지)를 타입으로 보장한다.
  draft 시도는 새 인스턴스 조립로만 가능하다 (조립 연산자는 Stage 4).

### 2.2 `StructureCheck` (Domain §1.4·§6.3)

```java
public final class StructureCheck {
    /** pair·XOR·참조 규칙 전수 검사. 비어 있으면 구조 정상. Problem은 읽기만 한다. */
    public static List<StructureViolation> check(Problem problem, Solution solution);
}

public record StructureViolation(Kind kind,
                                 Optional<RequestId> requestId,
                                 Optional<VehicleId> vehicleId) {
    public enum Kind {
        UNKNOWN_VEHICLE,          // 경로의 VehicleId가 Problem에 없음
        DUPLICATE_VEHICLE_ROUTE,  // 한 차량이 두 경로 소유 (§6.2 "차량 하나")
        UNKNOWN_NODE,             // 경로의 NodeId가 Problem에 없음 (DELIVERY_ONLY 가짜 픽업 포함)
        DUPLICATE_NODE,           // 같은 NodeId가 해 전체에 두 번
        PAIR_SPLIT,               // pickup·delivery가 서로 다른 경로 (§1.4)
        PAIR_INCOMPLETE,          // PICKUP_DELIVERY의 한쪽 방문만 존재 (§1.4)
        PICKUP_AFTER_DELIVERY,    // 같은 경로인데 pickup이 뒤 (§1.4)
        ASSIGNED_AND_BANKED,      // 경로 소유 ∧ bank 존재 (§6.3 XOR)
        NOT_ASSIGNED_NOT_BANKED,  // 어느 쪽에도 없음 (§6.3 XOR)
        UNKNOWN_REQUEST           // bank의 RequestId가 Problem에 없음
    }
}
```

검사 절차 (번호 순서, 전 위반 수집 후 반환):

```text
1. 경로   각 Route의 vehicleId가 Problem에 존재. 두 Route가 같은 차량이면 위반.
2. 방문   각 NodeId를 Problem.nodeRef로 역참조 — 실패 시 UNKNOWN_NODE.
         DELIVERY_ONLY의 pickup NodeId는 Problem에 등록돼 있지 않으므로 (Stage 1 §2.2 —
         pickup side 부재) 여기서 자동으로 걸린다 (§1.3 MUST NOT의 구조적 강제).
         해 전체에서 같은 NodeId 중복 → DUPLICATE_NODE.
3. pair   RequestId별로 등장 방문을 모은다:
         DELIVERY_ONLY: delivery 방문 1개 = 그 경로가 소유.
         PICKUP_DELIVERY: 두 방문이 다른 경로 → PAIR_SPLIT. 한쪽만 → PAIR_INCOMPLETE.
           같은 경로에서 pickup index > delivery index → PICKUP_AFTER_DELIVERY.
4. XOR   소유된 RequestId가 bank에도 있으면 ASSIGNED_AND_BANKED.
         Problem.requests() 전체에 대해 소유도 bank도 아니면 NOT_ASSIGNED_NOT_BANKED.
         bank의 ID가 Problem에 없으면 UNKNOWN_REQUEST.
```

- "bank에 정확히 한 번"(§6.3)은 `Set<RequestId>` 타입이 구조적으로 보장한다.
- "경로 종료 시 load = 0"(§6.2)은 별도 검사가 없다 — pair 완비(절차 3)와 §6.2 부호 규칙에서
  산술적으로 따라 나온다 (노트 N3). 재검증(Stage 5)은 그래도 재확인한다 (§10.2 방어).

---

## 3. 전파 (Domain §7)

### 3.1 시그니처

```java
public final class RoutePropagator {
    /**
     * §7.1 루프를 앞에서 뒤로 한 번. 물리 사실과 hard 판정만 — 점수·선호 없음 (§7.5).
     * 전제: visits는 구조 검사를 통과할 수 있는 형태 (미등록 NodeId → IllegalArgumentException).
     * pair 완비·XOR은 검사하지 않는다 — StructureCheck의 일 (노트 N3).
     */
    public static PropagationResult propagate(Problem problem, VehicleId vehicleId,
                                              List<NodeId> visits);
}

public sealed interface PropagationResult {
    record Feasible(RouteFacts facts) implements PropagationResult {}
    record Infeasible(Violation violation, Optional<NodeId> at) implements PropagationResult {}
        // at 부재 = 방문 이전(출발 적재)이나 경로 수준(근무창·한도) 위반
}

public enum Violation {
    TIME_WINDOW, REQ_DATE, WORK_WINDOW,
    CAPACITY_WEIGHT, CAPACITY_VOLUME,
    MAX_STOP_COUNT, MAX_DRIVE_TIME, MAX_DRIVE_DIST,
    INCOMPATIBLE_VEHICLE,   // Evaluator가 사용 (§4.2)
    PROFILE_HARD            // Evaluator가 사용 (§4.2)
}
```

### 3.2 사실 값 (Domain §7.3과 1:1)

```java
// eval 패키지 (배치 이유는 노트 N1)
public record VisitFacts(
    NodeId nodeId, RequestId requestId, boolean pickup, LocationId locationId,
    long arrivalSec, long serviceStartSec, long serviceEndSec,
    long waitingSec,                    // serviceStart − arrival (이 방문에서의 대기)
    long loadWeightAfterMilliKg,        // 방문 처리 후 적재 (§7.2 표의 load 열)
    long loadVolumeAfterMilliCbm) {}

public record RouteFacts(
    VehicleId vehicleId,
    long departureSec,                  // 차고 출발 시각
    long initialLoadWeightMilliKg, long initialLoadVolumeMilliCbm,   // §6.2 출발 적재
    List<VisitFacts> visits,
    Optional<Long> endDepotArrivalSec,  // endDepot 있을 때만
    long driveDistMeter, long driveTimeSec,                          // §7.4
    long customerWaitingTimeSec, long depotWaitingTimeSec,
    long serviceTimeSec,
    long interWorkWindowRestTimeSec,    // 현 모델 단일 근무창 → 항상 0 (§10 Q3)
    int stopCount) {
    /** §7.3 공식: driveTime + customerWaiting + depotWaiting + serviceTime + interWorkWindowRest */
    public long routeOperationalTimeSec();
}
```

§7.3 이름 대응: `arrival`·`serviceStartTime`·`serviceEndTime`·`loadWeight`·`loadVolume` →
`VisitFacts`, `distance`(=`driveDistMeter`)·`driveTime`·`customerWaitingTime`·
`depotWaitingTime`·`serviceTime`·`interWorkWindowRestTime`·`stopCount`·
`routeOperationalTime` → `RouteFacts`. 재검증(Stage 5)은 같은 record를 자기 계산으로 다시
채워 "같은 공식으로 재합산"(§7.3)을 값 비교로 확인할 수 있다.

### 3.3 전파 절차

번호 순서대로. 첫 hard 위반에서 `Infeasible`로 종료한다 (§7.1의 검사 순서 준수).
이동은 전부 `problem.travel()` 조회, speed는 `problem.resolvedSpeedKmH(vehicleId)` —
좌표 즉석 계산 금지 (Domain §4 MUST).

```text
0. 준비    v = problem.vehicle(vehicleId). 각 NodeId → problem.nodeRef (미등록이면 예외).
          initialLoad(W·V) = Σ 이 경로 방문이 속한 DELIVERY_ONLY request의 총수요 (§6.2).
          initialLoad > v.capacity → CAPACITY_* (at 부재 — 출발 전 위반).
1. 출발    waitInDepot=N (options): departure = v.workWindow.open  — "근무 시작 즉시 출발" (§2.5).
          waitInDepot=Y: 첫 방문 serviceStart에 정확히 도착하도록 늦춘다 —
            departure = max(workOpen, firstServiceStart − U[startDepot→loc₁])
            (firstServiceStart는 workOpen 출발 가정으로 먼저 계산. Y는 늦추기만 한다 —
             이르게 만들 수 없으므로 serviceStart 시각들은 N과 동일하고, 첫 방문 대기가
             customerWaiting → depotWaiting으로 옮겨질 뿐이다.)
          depotWaitingTime = departure − workOpen (N이면 0).
2. 방문 루프  각 방문 i에 대해 §7.1의 1–7 순서 그대로:
          arrival    = 직전 departure + U[직전 장소 → locᵢ]        // §7.1-1 (self arc = 0)
          serviceStart = max(arrival, side.window.open)            // §7.1-2 (이르면 대기)
          serviceStart > side.window.close → TIME_WINDOW           // 양끝 포함, §10 Q1
          serviceEnd  = serviceStart + side.serviceTimeSec         // §7.1-3 (§3.3 값은 정규화에서 확정)
          serviceStart > side.reqDateSec → REQ_DATE                // §7.1-4. serviceEnd는 조건 아님 (MUST NOT)
          load 갱신   : pickup 방문 +수요, delivery 방문 −수요       // §7.1-5·§6.2 부호 규칙
          0 ≤ load ≤ capacity 위반 → CAPACITY_*                    // §7.1-6
          departure   = serviceEnd                                 // §7.1-7
          집계: driveDist += D, driveTime += U, customerWaiting += serviceStart − arrival,
               serviceTime += side.serviceTimeSec,
               stopCount: locᵢ ≠ 직전 고객 서비스 장소이면 +1 (첫 방문은 항상 +1,
               depot는 비교 대상이 아니다 — §7.4)
3. 종료    v.endDepot 존재: endArrival = 마지막 serviceEnd + U[마지막 장소 → endDepot],
          driveDist·driveTime 가산 (§7.4 "(있으면) 마지막→endDepot").
          부재: 마지막 고객에서 종료 (§6.2).
4. 근무창  routeEnd = endArrival (endDepot 있으면) 또는 마지막 serviceEnd.
          routeEnd > v.workWindow.close → WORK_WINDOW.
          (departure ≥ workOpen은 절차 1이 구조적으로 보장. 단일 근무창에서는 시각이 단조
           증가하므로 양 끝점 검사가 §3.2 "이동 통째로 한 근무창 안"과 동치다 — 노트 N5.)
5. 한도    존재하는 축만 검사 (Optional 부재 = 제약 없음, §2.4·§2.6 — sentinel 비교 금지):
          stopCount > effectiveMaxStopCount → MAX_STOP_COUNT
          driveTime > maxDriveTimeSec → MAX_DRIVE_TIME / driveDist > maxDriveDistMeter → MAX_DRIVE_DIST
6. 사실    RouteFacts 조립 (interWorkWindowRestTime = 0 고정) 후 Feasible 반환.
```

### 3.4 §7.2 숫자 예 대응표 (DoD 재현 목표값)

Domain §7.2(분·kg)를 canonical 단위(planStart=당일 00:00 원점 초·milli-kg)로 옮긴 값이다.
테스트 T1은 정확히 이 값을 단언한다. 구성: V1 capacity 30000, workOpen 08:00(28800),
waitInDepot=N, U[차고→분당]=2400, U[분당→강남100]=3000, R2=PICKUP_DELIVERY(분당 픽업:
창 09:00–12:00, serviceTime 300, reqDate 36000) 10 kg, R1=DELIVERY_ONLY(강남100:
창 13:00–18:00, serviceTime 840, reqDate 54000) 10 kg. 경로 visits = [R2픽, R1배].

| Domain §7.2 | canonical 기대값 |
|---|---|
| 차고A 출발 08:00, load 10 | `departureSec=28800`, `initialLoadWeightMilliKg=10000` |
| R2픽 도착 08:40 → 대기 | `arrivalSec=31200`, `waitingSec=1200` |
| R2픽 서비스 09:00–09:05, reqDate 통과 | `serviceStartSec=32400`, `serviceEndSec=32700` (32400 ≤ 36000) |
| 픽업 후 load 20 | `loadWeightAfterMilliKg=20000` |
| R1배 도착 09:55 → 13:00까지 대기 | `arrivalSec=35700`, `waitingSec=11100` |
| R1배 서비스 13:00–13:14, reqDate 통과 | `serviceStartSec=46800`, `serviceEndSec=47640` (46800 ≤ 54000) |
| 배송 후 load 10 | `loadWeightAfterMilliKg=10000` |
| (집계) | `driveTimeSec=5400`, `customerWaitingTimeSec=12300`, `serviceTimeSec=1140`, `stopCount=2` |

§7.2 마지막 문단(serviceStart 16:00 > reqDate 15:00 → 불가)은 R1배 창을 16:00 도착이 되게
바꾼 변형으로 재현한다 → `Infeasible(REQ_DATE, at=R1배)`. 이 예제 경로는 R2배가 없는
pair 미완 상태다 — 전파가 pair를 검사하지 않기에 §7.2를 그대로 재현할 수 있다 (노트 N3).

---

## 4. 평가와 profile (Domain §8)

### 4.1 metric — `Evaluation` (Domain §8.1 층 ③, §8.2)

```java
// eval 패키지
public record Evaluation(int unassignedCount, int usedVehicleCount,
                         long totalDistanceMeter, long totalRouteOperationalTimeSec) {
    /** 중립 집계: usedVehicleCount = routes.size() (Route는 비어 있지 않음),
        합산은 Math.addExact (Domain §3.1 overflow 검사 정신). 해석(순위)은 profile.score의 일. */
    public static Evaluation aggregate(Collection<RouteFacts> routes, int unassignedCount);
}
```

- metric은 측정값일 뿐이다 — 선호를 섞지 않는다 (§8.1 MUST NOT). 경로별 `driveTime` 등
  나머지 §8.2 예시는 `RouteFacts`로 이미 노출되며, 별도 타입을 만들지 않는다.
- record 동등성이 재검증(Stage 5)의 "metric 재계산 대조"(§10.2)를 값 비교 한 줄로 만든다.
- **`Evaluation`은 고객이 늘어도 바뀌지 않는다.** 결과 JSON의 `metrics`(Domain §11.1)가
  이 record이고, 고객별 목적식은 이 값들 **위에서** `score`가 구성한다 (§4.3).
  요율표 비용처럼 여기 없는 축이 필요하면 `score`가 `RouteFacts`·`Problem`에서 직접 계산한다 —
  `Evaluation`에 고객 전용 필드를 추가하지 않는다 (MUST NOT).

### 4.2 정식 평가 — `Evaluator` (Domain §8.1 층 ②–④)

```java
public final class Evaluator {
    /** 전제: StructureCheck 위반 0 (Domain §6.5 순서 — 구조 검사 후 정식 평가).
        구조를 재검사하지 않는다. Problem·profile은 읽기만 한다.
        profile은 인자다 — Problem에 담기지 않는다 (Domain §5 MUST NOT). */
    public static EvaluationResult evaluate(Problem problem, Profile profile, Solution solution);
}

public sealed interface EvaluationResult {
    record Feasible(Evaluation evaluation,          // 층 ③ 중립 metric
                    long[] score,                   // 층 ④ 이 profile의 목적식 축
                    Map<VehicleId, RouteFacts> routes) implements EvaluationResult {}
    record Infeasible(VehicleId vehicleId, Violation violation,
                      Optional<NodeId> at,
                      Optional<String> hardConstraintId) implements EvaluationResult {}
}
```

절차:

```text
1. 경로마다 RoutePropagator.propagate — Infeasible이면 그대로 종료 (hard를 감점으로
   통과 금지, §8.1 MUST NOT).
2. 경로마다 호환성: 소유한 각 RequestId에 대해
   vehicleId ∈ problem.compatibleVehicles(requestId) (§3.4 동결 사실, Stage 2).
   위반 → Infeasible(INCOMPATIBLE_VEHICLE, at = 그 request의 delivery NodeId).
3. 경로마다 profile hard: profile.hardConstraints()의 각 h에 대해
   h.satisfied(problem, routeFacts) — 위반 → Infeasible(PROFILE_HARD, hardConstraintId = h.id()).
4. evaluation = Evaluation.aggregate(전 경로 facts, bank.size())     // 층 ③
5. score = profile.score(problem, evaluation, 전 경로 facts)          // 층 ④
   → Feasible(evaluation, score, routes) 반환.
```

- 비교 규약: `Scores.compare(a.score(), b.score()) < 0` ⟺ **a가 더 좋다**. Stage 4 acceptance와
  Stage 5의 best 판정이 이 규약 하나만 쓴다.
- 비교는 이미 계산된 `long[]`만 본다 — 전파 재실행 금지 (§8.1 MUST NOT).
- `Feasible`이 ③과 ④를 **둘 다** 들고 있는 이유: ③은 결과 JSON에 실리고(Domain §11.1),
  ④는 비교에만 쓰인다. 재검증(Stage 5)은 둘 다 재계산해 대조한다 (Domain §10.2).
- `long[]` 대조는 항상 `Arrays.equals`다 — §4.3의 공통 규칙 참조.

### 4.3 profile SPI (Domain §8.4, Architecture §2.2의 확정)

```java
// ---- core: eval 패키지 ----

public interface HardConstraint {
    String id();                              // 위반 보고용 식별자
    /** 전파를 통과한 경로 사실에 대한 추가 hard 판정. 위반이면 false.
        판정에 필요한 문제 사실(차급·구역·치수 등)은 problem에서 직접 읽는다. */
    boolean satisfied(Problem problem, RouteFacts route);
}

public interface Profile {
    String id();
    List<HardConstraint> hardConstraints();   // 물리 hard(§7)에 더해지는 고객 추가분
    /** 이 profile의 목적식 축(층 ④). 전부 "작을수록 좋다".
        같은 profile은 호출마다 항상 같은 길이를 반환한다 (다르면 버그). */
    long[] score(Problem problem, Evaluation metrics, Collection<RouteFacts> routes);
}

public class DefaultProfile implements Profile {          // final 아님 — 상속 대상
    public static final String ID = "default";
    // hardConstraints() = List.of() — 용량·시간창 등 물리 hard는 전파가 전담한다 (§7.1).
    // score() = §8.3 예의 4축:
    //   { unassignedCount, usedVehicleCount, totalDistanceMeter, totalRouteOperationalTimeSec }
}

public final class Scores {
    /** 사전식 비교. < 0 이면 a가 더 좋다. 길이가 다르면 IllegalArgumentException (버그 신호). */
    public static int compare(long[] a, long[] b);
}

// ---- solver-profile 모듈: profile 패키지 ----

public final class ProfileRegistry {
    public ProfileRegistry(Map<String, Profile> byCustomerId, Profile defaultProfile);
    public Profile resolve(Optional<String> customerId);  // 부재·미등록 → default (§8.4 MUST)
    public Profile defaultProfile();
    public static ProfileRegistry builtIn();              // 현재 등록 0건 — 전 고객 default
}
```

**`long[] score`의 동등 비교 규칙 (전 Stage 공통, MUST)**

`score`를 담는 record(`EvaluationResult.Feasible`·`AlnsResult`·`VerificationResult.Pass`)는
**자동 생성 `equals`를 쓰면 안 된다** — 배열 필드에서 record 동등성은 참조 비교라 값이 같아도
false가 나온다. 어디서든 score 대조는 `Arrays.equals`로 한다 (탐색↔재검증 대조, 테스트 단언,
두 구현 합치 확인 전부). 이 규칙이 지켜지지 않으면 Domain §6.4의 "캐시 = 재계산" 검사가
조용히 무의미해진다.

- `resolve`의 인자가 `Optional<String>`인 이유: Stage 1 `Plan.customerId`가 `Optional`이라
  null 키 `getOrDefault`를 피한다.
- **`HardConstraint`·`score`가 `Problem`을 받는다** (2026-08-10 변경). `Problem`이 `Profile`을
  담지 않게 되어 순환이 사라졌기 때문이다 (노트 N2). 이 인자 하나가 요율표(차급·구역별 단가)와
  3D 적재(차량 치수)를 **core 변경 없이** 가능하게 한다.
- **비교기 SPI는 없다.** 고정 가중치·Big-M 합산 금지(§8.3)가 관례가 아니라 구조로 지켜진다 —
  비교 수단이 `Scores.compare` 하나뿐이라 가중합 비교기를 만들 자리가 없다.
- 크게 만들고 싶은 축은 부호를 뒤집어 넣는다 (전 축 최소화 규칙을 깨지 않는다).
- `vhclOwnTyp` 부재 입력·default profile에서는 LEASE/DIRECT 축을 아예 쓰지 않는다 (§8.3).

#### 고객 구현 예 (Stage 3 범위 밖 — SPI가 충분한지 보이는 용도)

```java
// solver-profile — 요율표 고객: DefaultProfile 상속, 축 하나 교체
public final class AcmeProfile extends DefaultProfile {
    private static final Map<RateKey, Long> RATE = Map.of(/* 코드 상수 */);
    @Override public long[] score(Problem p, Evaluation m, Collection<RouteFacts> routes) {
        long cost = 0;
        for (RouteFacts r : routes) {
            String feature = p.vehicle(r.vehicleId()).vehicleFeature();   // ← Problem 인자 덕분
            cost += RATE.get(new RateKey(feature, zoneOf(p, r)));
        }
        return new long[] { m.unassignedCount(), cost, m.totalDistanceMeter() };
    }
}

// solver-profile — 3D 적재 고객: hard 제약 추가 (외부 라이브러리는 이 모듈에만)
public final class BoxFitConstraint implements HardConstraint {
    public String id() { return "BOX_FIT"; }
    public boolean satisfied(Problem p, RouteFacts route) {
        Vehicle v = p.vehicle(route.vehicleId());        // maxWidthMm·maxHeightMm·maxLengthMm
        return pack(v, itemsOf(p, route.visits()));      // item 치수는 canonical optional 필드
    }
}
```

### 4.4 `Problem` 확장 — `nodeRef` 색인만 (Stage 2 §8 인계 이행)

```java
public final class Problem {
    /** Stage 2의 freeze(Plan) 그대로. 인자를 추가하지 않는다 (Domain §5 MUST NOT). */
    public static Problem freeze(Plan plan);

    public Optional<NodeRef> nodeRef(NodeId id); // NodeId 역참조 (구조 검사·전파의 관문)
    // ... Stage 2의 기존 조회 메서드 전부 유지 (travel()·resolvedSpeedKmH()·compatibleVehicles() 등)
}

public record NodeRef(RequestId requestId, boolean pickup, RequestSide side) {}
```

- **`Problem.profile()`은 만들지 않는다** (2026-08-10 변경). profile은 `Evaluator`·탐색·재검증에
  인자로 전달되며, "탐색과 재검증이 같은 인스턴스"(§8.4 MUST)는 **호출자가 한 번 resolve해
  양쪽에 같은 값을 넘기는 것**으로 지킨다 (Stage 6 §조립). `Problem` 자체도 같은 방식으로
  공유되므로 보장 수준이 동일하다.
- `nodeRef` 색인은 freeze가 만든다 (차고 NodeId는 색인에 넣지 않는다 — 경로 visits에 올 수
  없는 값이므로 조회 실패가 곧 구조 신호다).
- 의존 최종 그림 (전부 한 방향, 순환 없음 — Architecture §2와 정합):

```text
모듈:  app → solver-profile → solver-core        (Maven이 강제)

solver-core 내부 패키지:
  domain  → (없음)
  problem → domain
  eval    → domain, problem                     ← Problem을 볼 수 있게 된 것이 이번 변경의 핵심
  solve   → domain, problem, eval
  verify  → domain, problem, eval               (Stage 5 — solve 참조 금지는 ArchUnit이 강제)

solver-profile:
  profile → domain, problem, eval               (core의 공개 타입만)
```

---

## 5. 설계 노트

| # | 내용 |
|---|---|
| N1 | **사실 값·Evaluation의 `eval` 배치**: 재검증은 "`domain`·`problem`·`eval`의 공개 타입만" 쓰고 `verify → solve` 참조는 ArchUnit이 막는다(Architecture §2.1). 그런데 재검증은 §7.3 값을 같은 공식으로 재합산하고 metric·score를 재계산·대조해야 하며(§10.2), profile SPI(`HardConstraint`·`score`)도 같은 타입을 소비한다. 따라서 `VisitFacts`·`RouteFacts`·`Evaluation`은 `solve`에 둘 수 없고, 평가 계약의 소유자인 `eval`에 둔다 |
| N2 | **`HardConstraint`·`score`가 `Problem`을 받는 이유** (2026-08-10 변경): 이전 설계는 `Problem`이 `Profile`을 보관해 `eval → problem` 참조가 순환이었고, 그래서 profile이 `RouteFacts`만 볼 수 있었다. 그 결과 요율표(차급·구역)·3D 적재(차량 치수)처럼 **문제 사실이 필요한 고객 정책을 표현할 수 없었다.** `Problem`에서 `Profile`을 빼자 순환이 사라졌고(Domain §5), 우회 장치(문제 뷰 인터페이스·컨텍스트 타입) 없이 `Problem`을 직접 넘기면 된다 |
| N3 | **전파는 pair·XOR·종료 load=0을 검사하지 않는다** — 그것은 구조 검사(§2.2)의 일이다(Domain §6.5의 단계 분리). §7.2 예제 경로 자체가 R2배 없는 pair 미완 상태이므로, 전파가 pair를 검사하면 DoD의 §7.2 재현이 불가능하다. 종료 load=0은 pair 완비에서 산술적으로 따라 나온다 |
| N4 | `Route.visits`에 depot가 없어 §6.2의 "경로 중간 depot 재방문 금지"가 표현 불가능하다. 출발/도착 차고는 `Vehicle`의 동결 필드가 유일한 근거다 |
| N5 | 근무창 검사는 양 끝점(departure ≥ open, routeEnd ≤ close)으로 충분하다: 단일 근무창 모델에서 전파 시각은 단조 증가하므로 모든 이동·서비스가 그 사이에 있다 (§3.2와 동치). 복수 근무창이 도입되면(§10 Q3) 이 동치가 깨지므로 그때 재설계한다 |
| N6 | **Stage 5 지침**: `Solution`은 `solve` 소유 타입이라 verify 진입 시그니처에 쓸 수 없다(ArchUnit). Architecture §2.1의 "값으로 전달받는다"는 문자 그대로 — verify는 `Map<VehicleId, List<NodeId>>` + `Set<RequestId>`(bank) 같은 domain 타입 분해값과 탐색이 보고한 `Evaluation`·`long[] score`를 받는다. `Solution`(record)이 정확히 그 분해값을 노출하므로 변환은 자명하다 |
| N7 | 근사·증분 없음: Stage 3의 전파·평가는 항상 전체 재계산이며 이것이 "정식 평가"의 권위다(§9.2). 증분 계산·shortlist는 Stage 4 재량이고, 그 결과가 정식 평가와 다르면 버그다(§6.4) |

---

## 6. Edge case 표

Domain §6–§8의 optional 규칙·경계값·오류 분류에서 뽑았다.

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | serviceStart == reqDate | 통과 (`≤`) | §2.3 |
| E2 | serviceEnd > reqDate (serviceStart는 이전) | 통과 — serviceEnd는 조건 아님 | §2.3 MUST NOT |
| E3 | serviceStart == window.close | 통과 (양끝 포함). +1초면 TIME_WINDOW | §2.3·§3.2 |
| E4 | arrival < window.open | 대기 후 open에 서비스 시작, `waitingSec` 기록 | §7.1-2 |
| E5 | 출발 전 initialLoad > capacity | CAPACITY_* (at 부재) — 방문 전 위반 | §6.2 |
| E6 | load == capacity | 통과 (`≤`) | §6.2 |
| E7 | DELIVERY_ONLY의 pickup NodeId가 경로에 등장 | UNKNOWN_NODE (Problem에 미등록 — 구조적 강제) | §1.3 MUST NOT |
| E8 | 같은 Request가 두 경로에 (PD 양쪽이 갈라짐) | PAIR_SPLIT | §1.4 |
| E9 | PD의 delivery만 경로에 | PAIR_INCOMPLETE | §1.4 |
| E10 | pickup이 delivery보다 뒤 | PICKUP_AFTER_DELIVERY | §1.4 |
| E11 | 경로 소유 ∧ bank | ASSIGNED_AND_BANKED | §6.3 XOR |
| E12 | 경로에도 bank에도 없음 | NOT_ASSIGNED_NOT_BANKED | §6.3 XOR |
| E13 | 전 Request가 bank, 경로 0개 | 유효한 해 — evaluate 가능 (unassigned=n, used=0, 거리 0) | §9.1 |
| E14 | 호환 차량 0대 Request가 bank에 | 정상 (오류·사유 기록 없음 — 사유는 Stage 5) | §3.4·§6.3 |
| E15 | 비호환 차량 경로에 배정 | Evaluator INCOMPATIBLE_VEHICLE (hard — 감점 아님) | §3.4·§8.1 |
| E16 | maxStop·maxDrive·치수 축 부재 | 검사 자체 없음 (sentinel 비교 금지) | §2.4·§2.6 |
| E17 | stopCount == effectiveMaxStopCount | 통과 (`≤`). +1이면 MAX_STOP_COUNT | §2.6 |
| E18 | 같은 장소 연속 방문 | 첫 진입만 stopCount +1, 사이 U=0 (self arc) | §7.4 |
| E19 | 첫 방문이 startDepot과 같은 장소 | stopCount +1 (depot는 비교 대상 아님), U=0 | §7.4 |
| E20 | waitInDepot=Y | 첫 방문 대기가 depotWaiting으로 이동, serviceStart·routeOperationalTime은 N과 동일 | §2.5·§7.3 |
| E21 | waitInDepot=Y인데 최속 출발도 창에 늦음 | TIME_WINDOW (Y는 늦추기만 — 이르게 못 함) | §2.5 |
| E22 | endDepot 존재 | 마지막→endDepot 이동을 driveDist·Time·근무창에 포함 | §7.4·§6.2 |
| E23 | routeEnd > workWindow.close | WORK_WINDOW (endDepot 이동 포함 시각 기준) | §3.2·§6.2 |
| E24 | 미등록 customerId | default profile 반환 | §8.4 MUST |
| E25 | customerId 부재 (Optional.empty) | default profile | §8.4·Stage 1 §2.2 |
| E26 | 등록된 customerId | 그 profile — core에 고객명 분기 없음 | §8.4 |
| E27 | 두 해의 score 축이 전부 동일 | `Scores.compare == 0` (동점 — 수락 정책은 Stage 4) | §8.3 |
| E28 | §8.3 예: A(미배정0,100km) vs B(미배정1,80km) | default score 축 → A 승 | §8.3 |
| E29 | metric 합산 overflow | `Math.addExact` → 예외 (조용한 오답 금지) | §3.1 |
| E30 | 빈 visits의 Route 생성 | IllegalArgumentException — 미사용 차량은 Route 부재로 표현 | §8.2 |
| E31 | `Scores.compare`에 길이 다른 배열 | IllegalArgumentException — 같은 profile이면 길이가 같아야 한다 (버그 신호) | §8.3 |
| E32 | profile이 빈 `long[]`을 반환 | 모든 해가 동점이 된다. 금지하지 않되 E31과 같은 방식으로 길이 일관성만 검사 | §8.3 |
| E33 | `HardConstraint`가 `problem`에서 optional 필드(치수 등)를 읽었는데 부재 | 그 제약의 판단이다 — core는 관여하지 않는다 (부재 = 그 축 미사용은 Domain §2.4의 core 규칙) | §8.4 |

---

## 7. 테스트 목록 — DoD 1:1 대응

위치: `solver-core/src/test/java/com/ronext/rpdptw/solve/`·`…/eval/`·`…/problem/`,
`ProfileRegistryTest`만 `solver-profile/src/test/java/com/ronext/rpdptw/profile/`.
의존은 JUnit만. Problem은 Stage 1·2의 정규화·freeze 경로로 손 조립한다 (fixture JSON 파싱은
Stage 6 — Stage 1 §7과 동일 원칙).

| # | 테스트 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| T1 | `RoutePropagatorTest.reproducesDomainSection72` | §3.4 대응표의 **전 셀** 단언 (출발·도착·대기·서비스 시각, load 곡선, 집계값) | "Domain §7.2 숫자 예 재현" |
| T2 | `RoutePropagatorTest.reqDateFailsOnLateServiceStart` | §7.2 마지막 문단 변형 → `Infeasible(REQ_DATE)`. E1·E2 경계 포함 (serviceEnd는 조건 아님) | 〃 + "hard 위반 검출" |
| T3 | `StructureCheckTest.detectsXorViolations` | E11·E12·E13(위반 0) + DUPLICATE_NODE·UNKNOWN_REQUEST | "XOR 위반 검출" |
| T4 | `StructureCheckTest.detectsPairViolations` | E7·E8·E9·E10 각각 정확한 Kind로 검출 | 〃 (§1.4 pair는 XOR의 전제) |
| T5 | `RoutePropagatorTest.detectsHardViolations` | E3·E5·E6·E17·E23: TIME_WINDOW·CAPACITY(출발 적재 포함)·WORK_WINDOW·MAX_* 각 1건 + 통과 경계 | "hard 위반 검출" |
| T6 | `EvaluatorTest.incompatibleVehicleAndProfileHardAreHard` | E15 + 항상-false HardConstraint를 가진 테스트 profile → PROFILE_HARD (감점 통과 없음) | 〃 |
| T7 | `ProfileRegistryTest.unregisteredResolvesToDefault` | E24·E25·E26: 미등록·부재 → default, 등록 → 해당 profile (solver-profile 모듈) | "미등록 customerId → default profile" |
| T8 | `EvaluatorTest.usesGivenProfileInstance` | `evaluate(problem, profile, solution)`이 인자로 받은 그 profile의 `hardConstraints`·`score`만 호출 (스파이 profile로 확인). `Problem`에는 profile이 없다 | 〃 (§8.4 — 탐색·재검증 같은 profile의 전제) |
| T9 | `EvaluatorTest.aggregatesMetricsAndScoresLexicographically` | E13·E27·E28·E29·E31: metric 집계값 + default 4축 score + "거리만 보는" 테스트 profile에서 승패 역전 (§8.3 예 재현) + 길이 불일치 예외 | (Plan 범위 문장 "metric, 사전식 비교") |
| T10 | `RoutePropagatorTest.waitInDepotRelocatesFirstWait` | E20·E21: Y/N에서 serviceStart 동일, 대기 귀속만 이동, routeOperationalTime 불변 | (Plan 범위 문장 "전파 루프·기록 값 §7.3") |
| T11 | `RoutePropagatorTest.stopCountAndDriveAggregation` | E18·E19·E22: 같은 장소 연속·depot 미산입·endDepot 포함 driveDist/Time | (Plan 범위 문장 "기록 값 §7.3") |
| T12 | `EvaluatorTest.hardConstraintReadsProblemFacts` | 차급(`vehicleFeature`)을 보고 판정하는 테스트 `HardConstraint` → `problem` 인자로 그 값에 도달함을 확인 (§4.3 고객 구현 예의 전제) | (노트 N2 — 이번 변경의 목적 자체) |

T9–T12는 DoD 세 문장 밖이지만 Plan Stage 3 범위 문장("적재 부호 규칙, 전파 루프, 기록 값(§7.3),
metric, 사전식 비교")의 직접 검증이다 — 보고에서 DoD 보강을 제안한다.

---

## 8. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| 초기해 생성·destroy/repair·acceptance·draft 조립 연산 (`Solution` 변형 연산자 일체) | Stage 4 | Domain §9·§6.5 |
| 증분 평가·삽입 shortlist 근사 (정식 평가만 존재) | Stage 4 재량 | Domain §9.2, 노트 N7 |
| `verify` 패키지의 독립 재검증 구현·점수 대조 실행 (여기서는 공유 값 타입만 제공) | Stage 5 | Domain §10, 노트 N1·N6 |
| 미배정 **사유** 산출 (`NO_COMPATIBLE_VEHICLE` 등 — bank는 ID만) | Stage 5 | Domain §6.3·§11 |
| 결과 JSON·run 메타 | Stage 5·6 | Domain §11 |
| 고객 특화 `Profile` 구현·레지스트리 등록 (현재 default 하나) | 필요 시 별도 | Domain §8.4 |
| depot open/close 창의 전파 적용 (`Depot.window`는 보관만 — §10 Q2) | Domain 보완 후 | Domain §2.5·§7 |
| `depot.taskTime` 시간 계산 적용 | 보류 (기존 유지) | Domain §2.5 |
| 복수 근무창·`interWorkWindowRestTime` 실계산 (0 고정) | Domain 보완 후 | Stage 1 §9 Q2, §10 Q3 |
| LEASE/DIRECT 비용 축·cost metric (현 입력·default profile 미사용) | 필요 시 profile 추가 | Domain §8.3 |
| ArchUnit 규칙 추가 (`verify ↛ solve`는 Stage 0에 이미 존재) | Stage 5 | Stage 0 §6 |

---

## 9. 미해결 질문

확정 문서로 답이 안 나오는 것만 남긴다. Stage 3 구현은 각 항목의 "잠정 처리"로 진행한다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | 시간창 close의 기준 — Domain §7.1은 serviceStart 공식과 "hard 검사: 시간창"만 정의하고, close 위반이 serviceStart 기준인지 serviceEnd 기준인지 명시하지 않았다 (reqDate는 serviceStart 기준을 명시) | **serviceStart ≤ close**로 판정 (reqDate 규칙과 동형·VRPTW 통례). serviceEnd는 close를 넘어도 통과. Domain §7에 한 줄 보완 요청 |
| Q2 | depot open/close 창의 전파 적용 — 규약 PDF는 "차량은 depot 창 안에 출발·도착"을 정의하지만 Domain §7 전파 루프·§6.2 hard 목록에는 depot 창이 없다 (depot.taskTime은 명시적으로 미적용) | 전파에서 **미적용** (필드는 Stage 1 `Depot.window`에 보관). fixture는 전일 창(00:00–23:59)이라 무영향. Domain 보완 후 절차 1·4에 추가 |
| Q3 | 복수 근무창 — §7.3의 `interWorkWindowRestTime`은 복수 근무창을 암시하지만 canonical `Vehicle.workWindow`는 하나다 (Stage 1 §9 Q2에서 이미 제기, 미해소) | 단일 근무창으로 전파하고 값은 0 고정. 복수 창 도입 시 노트 N5의 동치가 깨지므로 전파 절차 재설계 필요 |
