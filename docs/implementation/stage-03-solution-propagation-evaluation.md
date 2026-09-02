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
  - 2026-08-10 미해결 질문 정리 — Q1 해소(Domain §7.1 시간창 close = serviceStart 확정),
    Q2·Q3는 Plan §2.1 D4로 이관(차고 창은 "적용"으로 방향 반전, 다일 근무창 지원).
    §8의 두 행을 "D4 설계 결정 후"로 갱신하고 자기 참조 오타(§10 Q2·Q3 → §9) 정정. 설계 무변경
  - 2026-08-10 **D4 확정 반영 (Q2·Q3 해소) — 설계 변경**: `Violation`에 `DEPOT_WINDOW` 추가 ·
    `VisitFacts`에 `departureSec`, `RouteFacts`에 `spanStartSec`·`routeEndSec()` 추가 ·
    §3.3 전파 절차 재작성(창 목록 위 배치·미루기·차고 창·휴식 계산, 근무창 양 끝점 검사 삭제) ·
    §3.5 다일 대응표 신설 · N5 폐기·N8 신설 · E23 개정, E34~E40 신설 · T13~T16 신설
  - 2026-08-11 정정 — §3.5·T13의 미룬 시간 귀속을 Domain §7.3 공식과 일치시킴(55800 전액 휴식
    → 대기 1800 + 휴식 54000. §3.3 절차 2의 집계 규칙과도 모순이었다 — Domain §7.2.1 동시 정정) ·
    E37 문구 동기 · §4.4의 사실과 다른 "Stage 2 §8 인계" 참조 정정 · §7 말미 문구를 Plan §1
    DoD 편입으로 갱신
  - 2026-08-11 Stage 1 유예 반영 — §8 `depot.taskTime`(복귀 선적 시간이라 1바퀴엔 미발생) ·
    §4.3 소유 비용축 · §4.4 3D profile 예시 주석. 전파 절차·차고 창 적용은 무변경
  - 2026-08-12 Domain 2026-08-12 개정(self arc sentinel·startDepot 부재 규칙·수치 number
    인코딩) 정합 — §3.3 주석·E18·E19의 "self arc = 0"을 sentinel U=86,400으로 (Domain §4)
  - 2026-08-13 감사 후속 인터뷰 정합 — §4.4의 차고 NodeId 문구를 "NodeId 자체가 없다
    (설계 확정 삭제)"로 명확화
  - 2026-08-13 감사 결함 정정 (분할 6 #1·#2·#6·#7·#9, 통합 §2) — §3.3 절차 1·2에
    Domain §7.1 **위반 귀속** 규약(두 창 축 동시 소진 시 절차 문장에 먼저 적힌 쪽) 전파 ·
    E35에 "Ds 목록이 먼저 소진" 데이터 조건 명시(과일반화 교정, T14 문구 동기) ·
    §2.1 "방어 복사만"에 `Route` 빈 visits 거부 병기 · §7 말미 Plan 인용을 의역 표기로 ·
    §9 서두를 README 공통 규칙(표시하고 남김)으로. 전파 절차·판정 자체는 무변경
  - 2026-08-14 Domain `startDepot` optional + `PICKUP_ONLY` — `RouteFacts.departureSec`를
    Optional로. 절차 0/1을 start 유무로 분기(없으면 앞 arc 없음, 첫 방문 arrival = spanStart).
    절차 3: end 도착 시 PICKUP_ONLY만 −수요. 구조 검사에 PICKUP_ONLY. E41~E43·T15 조합 확장
  - 2026-08-15 Evaluator 절차 2 `at` — PICKUP_ONLY는 delivery side가 없으므로 있는 방문 NodeId
    (DELIVERY_ONLY → delivery, PICKUP_ONLY → pickup, PICKUP_DELIVERY → delivery).
    §3.1 `DEPOT_WINDOW` 설명을 Domain §7.1에 맞춤 (있는 출·도착만)
  - 2026-08-16 적재 식별자에서 단위·스케일 접미 제거 — `loadWeightAfter`/`loadVolumeAfter`,
    `initialLoadWeight`/`initialLoadVolume`. 내부 단위는 Domain §3.1. 규칙 변경 없음
  - 2026-08-17 Domain §4 self arc 원복 정합 — §3.3 의사코드 주석·E18·E19의 U=86,400을
    **D=0·U=0**으로. 같은 장소 연속 방문은 정상 입력이고, 길이 0 arc는 기존 `fitArc` 정의가
    확정하므로 규칙 신설 없음. 노드 중복 금지는 §2.2 `DUPLICATE_NODE`가 그대로 소유
  - 2026-08-22 테스트 계약 정정 — §7 T13 이름 오타(`deferSToNextWorkWindow` →
    `defersToNextWorkWindow`) · T5의 "통과 경계"에 용량 경계 목표값 명시(적재 == `maxWeight`를
    실제로 전파. 종전 문구로는 경계용 차량을 만들고 버려도 통과처럼 보였다 — 실제로 그랬다) ·
    §7 위치 문단에 공용 fixture 보관소 `SolveFixtures` 등재. 전파 절차·판정 무변경
  - 2026-08-25 테스트 계약 확장 — §7 T5·T14에 두 창 축 **동시 소진** 목표값 명시(차고 출발 →
    `WORK_WINDOW` · 서비스 시작 → `TIME_WINDOW`, §3.3 절차 1·2의 2026-08-13 위반 귀속 규약) ·
    T5에 **방문지** 창 목록이 빈 경우(→ `TIME_WINDOW`) 추가 · §3.3 보조 함수 문단에 `fitArc`의
    실제 가시성(package-private) 기재. 종전 표는 "먼저 소진"만 덮어 tie 분기가, 빈 목록은
    근무창 쪽만 덮여 방문지 쪽이 무검증이었다. 전파 절차·판정 무변경
  - 2026-09-02 테스트 계약 정정 — §7 fixture 문단을 실제 구성(`TimeFixtures`·`SolveFixtures`·
    `RoutePropagatorFixtures` 세 곳)에 맞춰 개정. 종전 "`SolveFixtures` 한 곳" 문면은 구현과
    어긋났다(`TimeFixtures`를 나머지 둘이 함께 쓴다) · T5에 운전 두 축(`MAX_DRIVE_TIME`·
    `MAX_DRIVE_DIST`)과 `CAPACITY_VOLUME`의 목표값 명시. 종전 "MAX_* 각 1건"은
    `MAX_STOP_COUNT`만 검증돼 `checkRouteLimits`의 두 분기와 부피 분기가 미실행이었다.
    전파 절차·판정 무변경
  - 2026-09-02 **`ZONE_MIX` 추가** — Domain §3.4 경로 구역 단일성 복원(세션 29 Q-COMP-02)에 따라
    `Violation.ZONE_MIX`와 §3.3 절차 2의 판정(이 방문의 구체 zoneId ≠ 경로의 앞선 구체 zoneId →
    `Infeasible(ZONE_MIX, at = 이 방문)`) 추가. E44·T17 추가. 다른 절차·판정 무변경
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

record는 불변이고 compact constructor에서는 방어 복사(`List.copyOf`/`Set.copyOf`)와
`Route`의 빈 visits 거부(E30)만 한다.
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
  `startDepot`/`endDepot`(**있으면**)에서 전파가 읽는다 (Stage 1이 trips 접기를 끝냈으므로 —
  Stage 1 §4 절차 5). 없으면 첫 고객에서 시작 / 마지막 고객에서 종료. 경로 중간 depot
  재방문(Domain §6.2 금지)은 **표현 자체가 불가능**하다 (노트 N4).
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
        UNKNOWN_NODE,             // 경로의 NodeId가 Problem에 없음 (DELIVERY_ONLY 가짜 픽업·PICKUP_ONLY 가짜 하차 포함)
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
         DELIVERY_ONLY의 pickup NodeId·PICKUP_ONLY의 delivery NodeId는 Problem에 등록돼
         있지 않으므로 (Stage 1 §2.2 — 없는 side) 여기서 자동으로 걸린다 (§1.3 MUST NOT의
         구조적 강제).
         해 전체에서 같은 NodeId 중복 → DUPLICATE_NODE.
3. pair   RequestId별로 등장 방문을 모은다:
         DELIVERY_ONLY: delivery 방문 1개 = 그 경로가 소유.
         PICKUP_ONLY: pickup 방문 1개 = 그 경로가 소유.
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
        // at 부재 = 방문 이전(출발 적재·차고 출발)이나 경로 수준(한도) 위반
}

public enum Violation {
    TIME_WINDOW, REQ_DATE, WORK_WINDOW,
    DEPOT_WINDOW,           // 차고 창 밖 있는 출·도착 (Domain §7.1, 2026-08-10 D4 신설)
    CAPACITY_WEIGHT, CAPACITY_VOLUME,
    MAX_STOP_COUNT, MAX_DRIVE_TIME, MAX_DRIVE_DIST,
    ZONE_MIX,               // 경로 구역 단일성 (Domain §3.4·§7.1-6, 2026-09-02 복원)
    INCOMPATIBLE_VEHICLE,   // Evaluator가 사용 (§4.2)
    PROFILE_HARD            // Evaluator가 사용 (§4.2)
}
```

- `WORK_WINDOW`는 "**남은 근무창이 없다**"는 뜻이다 (창 끝을 넘는 이동·서비스는 위반이 아니라
  다음 창으로 미루는 대상이다 — Domain §3.2·§7.1). `DEPOT_WINDOW`는 **있는** 차고 출·도착
  **순간**이 차고 창 밖일 때다 (없는 쪽은 검사하지 않는다). 둘은 `verify` 쪽
  `VerifyViolation.Kind`에 같은 이름으로 하나씩 대응한다
  (의미를 맞추되 별도 정의 — Stage 5 노트 N2).

### 3.2 사실 값 (Domain §7.3과 1:1)

```java
// eval 패키지 (배치 이유는 노트 N1)
public record VisitFacts(
    NodeId nodeId, RequestId requestId, boolean pickup, LocationId locationId,
    long arrivalSec, long serviceStartSec, long serviceEndSec,
    long departureSec,                  // 이 방문에서의 출발 (§7.1 절차 7). 근무창이 하나면
                                        // 항상 serviceEnd와 같다 — 그래서 종전엔 없던 필드다
    long waitingSec,                    // serviceStart − arrival (이 방문에서의 대기)
    long loadWeightAfter,               // 방문 처리 후 적재 (§7.2 표의 load 열). Domain §3.1 내부 단위
    long loadVolumeAfter) {}

public record RouteFacts(
    VehicleId vehicleId,
    long spanStartSec,                  // 첫 근무창의 open — 경로 시간의 기준점 (§7.3)
    Optional<Long> departureSec,        // 차고 출발 시각. startDepot 있을 때만 (Domain §7.1 절차 0)
    long initialLoadWeight, long initialLoadVolume,                  // §6.2 출발 적재. Domain §3.1 내부 단위
    List<VisitFacts> visits,
    Optional<Long> endDepotArrivalSec,  // endDepot 있을 때만
    long driveDistMeter, long driveTimeSec,                          // §7.4
    long customerWaitingTimeSec, long depotWaitingTimeSec,
    long serviceTimeSec,
    long interWorkWindowRestTimeSec,    // 근무창 사이의 틈에서 보낸 시간 (§7.3 정의)
    int stopCount) {
    /** §7.3 공식: driveTime + customerWaiting + depotWaiting + serviceTime + interWorkWindowRest */
    public long routeOperationalTimeSec();
    /** §7.3 항등식: routeOperationalTimeSec() == routeEnd − spanStart. 두 구현 대조용 */
    public long routeEndSec();          // endDepotArrivalSec 또는 마지막 visit의 serviceEnd
}
```

§7.3 이름 대응: `arrival`·`serviceStartTime`·`serviceEndTime`·`departure`·`loadWeight`·`loadVolume` →
`VisitFacts`, `distance`(=`driveDistMeter`)·`driveTime`·`customerWaitingTime`·
`depotWaitingTime`·`serviceTime`·`interWorkWindowRestTime`·`stopCount`·
`routeOperationalTime` → `RouteFacts`. 재검증(Stage 5)은 같은 record를 자기 계산으로 다시
채워 "같은 공식으로 재합산"(§7.3)을 값 비교로 확인할 수 있다.

**시간 측정 규약 (MUST — Domain §3.2)**: 모든 소요 시간은 **두 시각의 차**로 계산한다. 창이
양끝 포함이라 "구간 안의 초 개수"를 세면 창마다 1초가 어긋나고, 그 오차는 이 record의
`interWorkWindowRestTimeSec`에 먼저 나타나 Stage 5 T5(두 독립 구현의 합치)에서 원인 불명의
off-by-one으로 보인다.

### 3.3 전파 절차

번호 순서대로. 첫 hard 위반에서 `Infeasible`로 종료한다 (§7.1의 검사 순서 준수).
이동은 전부 `problem.travel()` 조회, speed는 `problem.resolvedSpeedKmH(vehicleId)` —
좌표 즉석 계산 금지 (Domain §4 MUST).

창 목록(`v.workWindows`·차고 `windows`·`side.windows`)은 전개·병합이 끝난 정렬된 목록이다
(Stage 1 §2.2 불변식). 시각이 줄지 않으므로 각 목록은 **포인터를 앞으로만 밀며** 훑는다 —
경로당 O(방문 수 + 창 수)다. 창이 하나뿐인 입력(현행 fixture)에서는 아래 미루기·휴식이
한 번도 일어나지 않아 종전 절차와 값이 같다.

내부 보조 함수 두 개 (private — **`verify`와 공유하지 않는다**, 노트 N8):

```text
fitArc(창목록, from, U)      = [t, t+U]가 한 창 안에 통째로 들어가는 가장 이른 t ≥ from (없으면 부재)
fitService(창목록, from, S)  = [t, t+S]가 한 창 안에 통째로 들어가는 가장 이른 t ≥ from (없으면 부재)
```

가시성은 `fitArc`(및 별개 보조인 시간 겹침 합산)만 **package-private**이다 — T13이 미루기·대기
귀속을 직접 단언하기 때문이다. 나머지는 private이고, 이 완화는 `solve` 패키지 안에서만 유효하므로
N8("창을 걷는 코드는 `verify`와 공유하지 않는다")에 영향이 없다 (`verify ↛ solve`는 ArchUnit이 강제).

```text
0. 준비    v = problem.vehicle(vehicleId). 각 NodeId → problem.nodeRef (미등록이면 예외).
          initialLoad(W·V) = Σ 이 경로 방문이 속한 DELIVERY_ONLY request의 총수요 (§6.2).
          initialLoad > v.capacity → CAPACITY_* (at 부재 — 출발 전 위반).
          W = v.workWindows. 비어 있으면 WORK_WINDOW (계획 기간에 못 쓰는 차량 — Stage 1 E30).
          spanStart = W[0].open.
1. 출발    v.startDepot 없음 (Domain §7.1 절차 0'):
            앞 arc 없음. 차고 창 검사 없음. waitInDepot 미적용.
            첫 방문 arrival = spanStart. departureSec = empty. depotWaitingTime = 0.
            이어서 절차 2 (첫 방문은 직전 departure가 없으므로 arrival을 spanStart로 둔다).
          v.startDepot 있음:
          Ds = problem.depotAt(v.startDepot).windows(). 비어 있으면 DEPOT_WINDOW.
          waitInDepot=N (options): departure = 아래를 모두 만족하는 가장 이른 시각 (Domain §7.1 절차 0)
            · 어느 근무창 안 · 어느 Ds 창 안 · 첫 이동이 그 근무창 안에 통째로
            = fitArc(W, t, U[startDepot→loc₁])와 Ds를 함께 앞으로 밀며 찾는다.
            근무창이 먼저 소진되면 WORK_WINDOW, Ds가 먼저 소진되면 DEPOT_WINDOW (at 부재).
            두 창 축이 **같은 시각에 함께 소진**되면 절차 문장에 먼저 적힌 쪽 = WORK_WINDOW를
            기록한다 (Domain §7.1 위반 귀속, 2026-08-11 — 가능/불가 판정은 안 바뀌지만
            탐색과 재검증이 FAILED 원인 종류까지 같게 내기 위한 규약).
            → **"근무 시작 즉시 출발"은 "근무 시작과 차고 개장 중 늦은 쪽에 즉시"다** (Domain §2.5).
          waitInDepot=Y: 위 조건을 지키면서 첫 방문 serviceStart에 맞춰 늦춘다 —
            departure = 위 조건을 만족하면서 arrival ≤ firstServiceStart인 **가장 늦은** 시각
            (firstServiceStart는 N 출발 가정으로 먼저 계산. N의 출발 시각도 이 조건을 만족하므로
             후보 집합은 비지 않는다. Y는 늦추기만 한다 — serviceStart 시각들은 N과 동일하고
             첫 방문 대기가 customerWaiting → depotWaiting으로 옮겨질 뿐이다.)
          depotWaitingTime = [spanStart, departure] 중 **근무창에 걸친 부분** (Domain §7.3).
            차고가 근무 시작보다 늦게 열면 **N에서도 0이 아니다**.
2. 방문 루프  각 방문 i에 대해 Domain §7.1의 1–7 순서 그대로:
          arrival    = 첫 방문 ∧ startDepot 없음: spanStart (절차 1).
                       그 외: 직전 departure + U[직전 장소 → locᵢ]  // §7.1-1 (self arc는 D=0·U=0 — 같은 장소면 이동 없음, §4)
                       (직전 출발이 이동을 담는 창을 골랐으므로 arrival은 근무창 안이다)
          serviceStart = arrival 이상이면서 side.windows 중 하나 안이고
                       fitService(W, ·, serviceTime)도 만족하는 가장 이른 시각   // §7.1-2
            side.windows가 먼저 소진 → TIME_WINDOW / W가 먼저 소진 → WORK_WINDOW
            (두 축이 같은 시각에 함께 소진되면 먼저 적힌 쪽 = TIME_WINDOW — Domain §7.1 위반 귀속)
            (창이 하나면 max(arrival, open) — 종전 식과 같다. serviceStart > close 판정도
             그대로 살아 있다: 창을 넘어선 순간 그 창은 후보에서 빠진다. Domain §7.1 MUST)
          serviceEnd  = serviceStart + side.serviceTimeSec         // §7.1-3
          serviceStart > side.reqDateSec → REQ_DATE                // §7.1-4. serviceEnd는 조건 아님 (MUST NOT)
          load 갱신   : pickup 방문 +수요, delivery 방문 −수요       // §7.1-5·§6.2 (PICKUP_ONLY는 pickup만)
          0 ≤ load ≤ capacity 위반 → CAPACITY_*                    // §7.1-6
          side.zoneId 존재 ∧ 경로의 앞선 구체 zoneId 존재 ∧ 둘이 다름 → ZONE_MIX (at = 이 방문)
                        // §7.1-6·Domain §3.4 — ALL·부재는 정규화가 비웠으므로 "존재"만 본다
          departure   = fitArc(W, serviceEnd, U[locᵢ → 다음 장소])  // §7.1-7 — 창 끝을 넘으면
                        부재이면 WORK_WINDOW                        //          다음 창으로 미룬다
                        (마지막 방문이면 다음 장소 = endDepot, 없으면 departure = serviceEnd)
          집계: driveDist += D, driveTime += U,
               customerWaiting += ([arrival, serviceStart] + [serviceEnd, departure]) 중
                                  근무창에 걸친 부분 (Domain §7.3),
               serviceTime += side.serviceTimeSec,
               stopCount: locᵢ ≠ 직전 고객 서비스 장소이면 +1 (첫 방문은 항상 +1,
               depot는 비교 대상이 아니다 — §7.4)
3. 종료    v.endDepot 존재: endArrival = 마지막 departure + U[마지막 장소 → endDepot],
          driveDist·driveTime 가산 (§7.4 "(있으면) 마지막→endDepot").
          endArrival이 problem.depotAt(v.endDepot).windows() **어느 것에도** 들어가지 않으면 DEPOT_WINDOW —
            기다렸다 들어가는 것으로 미루지 않는다 (Domain §7.1 절차 8 확정).
          도착 사건에서 그 경로에 배정된 PICKUP_ONLY 수요만 뺀다 (Domain §6.2).
            잔량을 0으로 대입하지 않는다 (MUST NOT). 그 다음 0 ≤ load ≤ capacity.
          부재: 마지막 고객에서 종료 (§6.2). PICKUP_ONLY가 이 경로에 있으면 하차 싱크가
            없어 종료 load ≠ 0 — 호환이 막았어야 하는 2차 방어.
4. 휴식    routeEnd = endArrival (endDepot 있으면) 또는 마지막 serviceEnd.
          interWorkWindowRestTime = (routeEnd − spanStart) − ([spanStart, routeEnd] 중 근무창에
            걸친 시간)  — 근무창 사이의 틈에서 보낸 시간 전부 (Domain §7.3).
          **근무창 hard의 별도 양 끝점 검사는 없다** — 절차 1·2·3이 이미 창 안에 배치했고,
          배치할 창이 없을 때 WORK_WINDOW로 끝났다 (노트 N5 대체).
5. 한도    존재하는 축만 검사 (Optional 부재 = 제약 없음, §2.4·§2.6 — sentinel 비교 금지):
          stopCount > effectiveMaxStopCount → MAX_STOP_COUNT
          driveTime > maxDriveTimeSec → MAX_DRIVE_TIME / driveDist > maxDriveDistMeter → MAX_DRIVE_DIST
          (세 한도 모두 **경로 전체 합계**다 — 다일이어도 하루치로 나누지 않는다, Domain §2.4)
6. 사실    RouteFacts 조립 후 Feasible 반환.
          조립 직후 항등식을 확인할 수 있다: routeOperationalTimeSec() == routeEnd − spanStart
          (Domain §7.3 — 대기 3종의 정의가 맞물렸는지 보는 가장 싼 검사. T15)
```

### 3.4 §7.2 숫자 예 대응표 (DoD 재현 목표값)

Domain §7.2(분·kg)를 canonical 단위(planStart=당일 00:00 원점 초·milli-kg)로 옮긴 값이다.
테스트 T1은 정확히 이 값을 단언한다. 구성: V1 capacity 30000, **`workWindows=[[28800, 64800]]`
(08:00\~18:00, 하루짜리 계획이라 창 1개)**, **startDepot `windows=[[0, 86399]]`(전일)**,
waitInDepot=N, U[차고→분당]=2400, U[분당→강남100]=3000, R2=PICKUP_DELIVERY(분당 픽업:
창 09:00–12:00, serviceTime 300, reqDate 36000) 10 kg, R1=DELIVERY_ONLY(강남100:
창 13:00–18:00, serviceTime 840, reqDate 54000) 10 kg. 경로 visits = [R2픽, R1배].

| Domain §7.2 | canonical 기대값 |
|---|---|
| 차고A 출발 08:00, load 10 | `spanStartSec=28800`, `departureSec=28800`, `initialLoadWeight=10000` |
| R2픽 도착 08:40 → 대기 | `arrivalSec=31200`, `waitingSec=1200` |
| R2픽 서비스 09:00–09:05, reqDate 통과 | `serviceStartSec=32400`, `serviceEndSec=32700` (32400 ≤ 36000), `departureSec=32700` |
| 픽업 후 load 20 | `loadWeightAfter=20000` |
| R1배 도착 09:55 → 13:00까지 대기 | `arrivalSec=35700`, `waitingSec=11100` |
| R1배 서비스 13:00–13:14, reqDate 통과 | `serviceStartSec=46800`, `serviceEndSec=47640` (46800 ≤ 54000), `departureSec=47640` |
| 배송 후 load 10 | `loadWeightAfter=10000` |
| (집계) | `driveTimeSec=5400`, `customerWaitingTimeSec=12300`, `serviceTimeSec=1140`, `stopCount=2`, **`depotWaitingTimeSec=0`, `interWorkWindowRestTimeSec=0`** |

**창이 하나이므로 이 표의 값은 D4(다일 근무창·차고 창) 전과 완전히 같다.** 방문의
`departureSec`는 전부 `serviceEndSec`와 같고, 미루기·휴식이 일어나지 않는다. 항등식도 성립한다:
`routeOperationalTime = 5400 + 12300 + 0 + 1140 + 0 = 18840 = routeEnd(47640) − spanStart(28800)`.

§7.2 마지막 문단(serviceStart 16:00 > reqDate 15:00 → 불가)은 R1배 창을 16:00 도착이 되게
바꾼 변형으로 재현한다 → `Infeasible(REQ_DATE, at=R1배)`. 이 예제 경로는 R2배가 없는
pair 미완 상태다 — 전파가 pair를 검사하지 않기에 §7.2를 그대로 재현할 수 있다 (노트 N3).

### 3.5 Domain §7.2.1 다일 예 대응표 (T13 목표값)

Domain §7.2.1(3일 계획, 매일 08:00\~17:00)을 canonical 초로 옮긴 값이다.
`workWindows = [[28800,61200], [115200,147600], [201600,234000]]`.

| Domain §7.2.1 | canonical 기대값 |
|---|---|
| 2일차 16:30에 90분 이동 시작 시도 | `fitArc(W, 145800, 5400)` → 2일차 창에 안 들어감(`151200 > 147600`) |
| 3일차 08:00으로 미룸 | `departureSec = 201600` (그 방문의 `VisitFacts.departureSec`) |
| 미룬 15시간 30분의 귀속 (Domain §7.3·§3.3 절차 2) | `customerWaitingTimeSec += 1800` (근무창에 걸친 16:30\~17:00) · `interWorkWindowRestTimeSec += 54000` (창 사이 틈. 2026-08-11 정정 — 전액 휴식은 §7.3 공식과 모순) |
| 도착 3일차 09:30 | `arrivalSec = 207000` |
| 2일차 16:00에 서비스가 끝난 변형 | `serviceEndSec=144000`, `departureSec=201600`, 그중 근무 시간 3600초는 `customerWaitingTimeSec += 3600`, 창 사이 54000초는 `interWorkWindowRestTimeSec += 54000` |
| 9시간 창에 12시간 이동 | `fitArc` 전부 실패 → `Infeasible(WORK_WINDOW, at=그 방문)` |

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
   위반 → Infeasible(INCOMPATIBLE_VEHICLE, at = 그 request의 있는 방문 NodeId).
           DELIVERY_ONLY → delivery, PICKUP_ONLY → pickup,
           PICKUP_DELIVERY → delivery (기존과 같음).
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
- 소유(LEASE/DIRECT) 축은 **지금 없다** — canonical에 소유 필드를 담지 않기 때문이다
  (2026-08-11 유예, Domain §2.4 유예 표 · Stage Extra E3).

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
        Vehicle v = p.vehicle(route.vehicleId());        // 치수 한도 3필드 (Stage Extra E2로 유예 중)
        return pack(v, itemsOf(p, route.visits()));      // item 치수도 같이 되살린다 (Domain §2.1.1)
    }
}
```

### 4.4 `Problem` 확장 — `nodeRef` 색인만 (Stage 2 §2.3의 `Problem`에 조회 메서드 1개 추가)

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
- `nodeRef` 색인은 freeze가 만든다. 차고는 **NodeId 자체가 없다** (유예가 아니라 설계 확정
  삭제 — 2026-08-13 분류 확정) — 경로 visits에 올 수 없는 값이므로 조회 실패가 곧 구조 신호다.
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
| N5 | **폐기 (2026-08-10, D4).** 옛 내용: "근무창 검사는 양 끝점(departure ≥ open, routeEnd ≤ close)으로 충분하다 — 단일 근무창에서는 시각이 단조 증가하므로 §3.2와 동치". 근무창이 여럿이면 그 동치가 깨진다 (예: 1일차 17:00에 끝난 이동이 양 끝점만 보면 통과지만 실제로는 창 밖이다) — N5 자신이 예고한 상황이다. **대체 규칙**: 검사는 별도 비교가 아니라 **배치 자체**다. 절차 1·2·3이 이동·서비스를 창 안에 넣고, 넣을 창이 없을 때만 `WORK_WINDOW`다 (§3.3). 재검증은 배치 결과가 실제로 창 안에 있는지 한 번 더 훑어 확인한다 (Domain §10.2, Stage 5 §3 절차 5) |
| N6 | **Stage 5 지침**: `Solution`은 `solve` 소유 타입이라 verify 진입 시그니처에 쓸 수 없다(ArchUnit). Architecture §2.1의 "값으로 전달받는다"는 문자 그대로 — verify는 `Map<VehicleId, List<NodeId>>` + `Set<RequestId>`(bank) 같은 domain 타입 분해값과 탐색이 보고한 `Evaluation`·`long[] score`를 받는다. `Solution`(record)이 정확히 그 분해값을 노출하므로 변환은 자명하다 |
| N7 | 근사·증분 없음: Stage 3의 전파·평가는 항상 전체 재계산이며 이것이 "정식 평가"의 권위다(§9.2). 증분 계산·shortlist는 Stage 4 재량이고, 그 결과가 정식 평가와 다르면 버그다(§6.4) |
| N8 | **공유하는 것은 창 '목록'이고, 창을 '걷는 코드'가 아니다 (MUST — 리뷰에서 가장 틀리기 쉬운 지점).** 전개·병합이 끝난 `List<TimeWindow>`는 `Problem`의 동결 사실이라 탐색과 재검증이 같은 값을 읽는다 (이동표와 같은 지위). 그러나 `fitArc`/`fitService`처럼 **그 목록을 해석해 시각을 정하는 코드**는 규칙이지 사실이 아니므로, `RoutePropagator`와 `RouteReplay`가 각자 구현한다 — 공유하면 같은 버그가 양쪽에 숨어 재검증이 무의미해진다 (Domain §10.1의 이중 기입). 같은 문장이 Stage 5 §5 N1의 "공유 허용 목록"에도 있다 |

---

## 6. Edge case 표

Domain §6–§8의 optional 규칙·경계값·오류 분류에서 뽑았다.

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | serviceStart == reqDate | 통과 (`≤`) | §2.3 |
| E2 | serviceEnd > reqDate (serviceStart는 이전) | 통과 — serviceEnd는 조건 아님 | §2.3 MUST NOT |
| E3 | serviceStart == 그 창의 close | 통과 (양끝 포함). +1초면 **그 창은 후보에서 빠지고 다음 창으로 미룬다** — 남은 창이 없을 때만 TIME_WINDOW (E38과 같은 규칙. 마지막 창의 close 하나만 보면 창 사이 틈에서 시작하는 서비스가 통과해 버린다 — Domain §7.1 MUST) | §2.3·Domain §3.2·§7.1 |
| E4 | arrival < 다음 창의 open | 대기 후 open에 서비스 시작, `waitingSec` 기록 (`side.windows`에서 arrival 이후 첫 창) | §7.1-2 |
| E5 | 출발 전 initialLoad > capacity | CAPACITY_* (at 부재) — 방문 전 위반 | §6.2 |
| E6 | load == capacity | 통과 (`≤`) | §6.2 |
| E7 | DELIVERY_ONLY의 pickup NodeId·PICKUP_ONLY의 delivery NodeId가 경로에 등장 | UNKNOWN_NODE (Problem에 미등록 — 구조적 강제) | §1.3 MUST NOT |
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
| E18 | 같은 장소 연속 방문 | 첫 진입만 stopCount +1, 사이 **D = 0 · U = 0** (self arc — Domain §4, 2026-08-17 원복: 같은 장소면 이동이 없다. 서로 다른 두 Request가 같은 `LocationId`를 쓰는 것은 정상 입력이고, 노드 중복은 §2.2 `DUPLICATE_NODE`가 잡는다. 2026-08-12의 sentinel U=86,400은 폐기 — 정상 케이스에 24시간을 매겼다). 길이 0 arc는 §3.3 `fitArc` 정의가 그대로 확정한다: `[t, t]`라 `t`가 창 안이면 그 자리에서 성립하고 미루기가 일어나지 않는다 | §7.4·§4·§3.3 |
| E19 | 첫 방문이 startDepot과 같은 장소 | stopCount +1 (depot는 비교 대상 아님), **D = 0 · U = 0** (self arc — §4). 차고에서 같은 장소의 첫 고객으로 나가는 것은 **이동 0**이지 위반이 아니다 — 차고는 `visits`에 없으므로(§2.1) 노드 중복도 아니다 | §7.4·§4 |
| E20 | waitInDepot=Y | 첫 방문 대기가 depotWaiting으로 이동, serviceStart·routeOperationalTime은 N과 동일. **다일이면 그 대기 중 창 사이의 틈은 rest로 간다** (근무 시간 부분만 depotWaiting) | §2.5·§7.3 |
| E21 | waitInDepot=Y인데 최속 출발도 창에 늦음 | TIME_WINDOW (Y는 늦추기만 — 이르게 못 함) | §2.5 |
| E22 | endDepot 존재 | 마지막→endDepot 이동을 driveDist·Time·근무창에 포함 | §7.4·§6.2 |
| E23 | 마지막 이동이 남은 근무창 어디에도 안 들어감 | WORK_WINDOW (종전의 "routeEnd > close" 검사를 대체 — 배치 실패가 곧 위반, N5) | Domain §3.2·§7.1 |
| E34 | 차고가 근무 시작보다 늦게 엶 (근무 06:00\~, 차고 08:00\~) | waitInDepot=**N**이어도 출발 = 08:00, `depotWaitingTimeSec = 7200` (종전 "N이면 0"은 거짓이 된다) | Domain §2.5·§7.1 절차 0 |
| E35 | 차고 창 밖에만 출발 가능한 상황 (근무창 ∩ 차고 창 = ∅) 중 **Ds(차고 창) 목록이 먼저 소진되는 구성** — 예: 차고 창이 근무창보다 먼저 끝남 | DEPOT_WINDOW (at 부재 — 출발 전 위반). 결과는 데이터에 따라 갈린다: 근무창 목록이 먼저 소진되는 구성이면 WORK_WINDOW, 동시 소진이면 WORK_WINDOW (§3.3 절차 1 — Domain §7.1 위반 귀속) | Domain §7.1 절차 0 |
| E36 | endDepot 도착이 차고 창 사이의 틈 | DEPOT_WINDOW — 문 열 때까지 기다렸다 들어가는 것으로 **미루지 않는다** | Domain §7.1 절차 8 |
| E37 | 이동이 현재 창 끝을 넘음, 다음 창 있음 | 위반 아님 — 다음 창으로 미룬다. 미룬 시간 중 창 사이 틈은 `interWorkWindowRestTimeSec`, 근무창에 걸친 부분은 `customerWaitingTimeSec` (§3.5, Domain §7.3) | Domain §3.2·§7.1-7 |
| E38 | 서비스가 창 끝을 넘음 (`serviceStart + serviceTime > close`) | serviceStart를 다음 창으로 미룬다. 그 때문에 방문 시간창을 넘기면 TIME_WINDOW, 근무창이 소진되면 WORK_WINDOW | Domain §7.1-2 |
| E39 | `workWindows`가 빈 목록인 차량 | WORK_WINDOW (그 차량을 쓰는 경로는 전부 불가. 정규화에서는 오류가 아니었다 — Stage 1 E30) | Domain §3.2 |
| E40 | 근무창이 1개뿐인 입력 (현행 fixture) | 미루기·휴식이 한 번도 일어나지 않아 **D4 전과 값이 동일**. `departureSec == serviceEndSec`, `interWorkWindowRestTimeSec == 0` | §3.4 |
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
| E41 | startDepot 부재 | 앞 arc 없음. 첫 방문 arrival = spanStart. departureSec empty. depotWaitingTime = 0. waitInDepot 미적용 | Domain §7.1 절차 0' |
| E42 | PICKUP_ONLY pickup 방문 | load +수요. endDepot 도착에서 같은 수요만 −. 잔량 일괄 0 대입 금지 | Domain §6.2 |
| E44 | 경로의 구체 zoneId가 둘 이상 (A → ALL → B) | ZONE_MIX (at = 두 번째 구체 구역의 첫 방문). A → ALL → A → A → ALL·ALL → ALL은 통과. PD 양단이 다른 구체 구역이면 어느 경로에서도 ZONE_MIX | Domain §3.4·§7.1-6 |
| E43 | startDepot 부재 + waitInDepot=Y | Y는 무시. 오류 아님. 시각은 N과 동일 | Domain §2.5·§7.1 |

---

## 7. 테스트 목록 — DoD 1:1 대응

위치: `solver-core/src/test/java/com/ronext/rpdptw/solve/`·`…/eval/`·`…/problem/`,
`ProfileRegistryTest`만 `solver-profile/src/test/java/com/ronext/rpdptw/profile/`.
의존은 JUnit만. Problem은 Stage 1·2의 정규화·freeze 경로로 손 조립한다 (fixture JSON 파싱은
Stage 6 — Stage 1 §7과 동일 원칙).
`solve` 패키지 테스트의 fixture는 책임별로 세 곳이다 (Stage 4·5가 그대로 이어받는다):
**`TimeFixtures`**(시각·시간창 리터럴 — 나머지 둘도 이것을 쓴다) ·
**`SolveFixtures`**(`Problem`·`Request`·`Vehicle` 조립과 §7.2 구성 — `solve` 전 테스트 공용) ·
**`RoutePropagatorFixtures`**(전파 테스트 전용 `Problem` 변형). 새 fixture는 쓰는 테스트가
둘 이상이면 `SolveFixtures`에, 전파 한 곳뿐이면 `RoutePropagatorFixtures`에 둔다 —
**같은 fixture를 두 곳에 복제하지 않는다**(2026-09-02 개정: 종전 "`SolveFixtures` 한 곳"은
구현과 어긋난 문면이었다). **일정 번호를 클래스·메서드 이름에 넣지 않는다** —
문서 좌표(`section72…`)는 권위 문서를 가리키므로 그대로 쓴다
([README](README.md) 이름 규칙, 2026-08-22).

| # | 테스트 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| T1 | `RoutePropagatorTest.reproducesDomainSection72` | §3.4 대응표의 **전 셀** 단언 (출발·도착·대기·서비스 시각, load 곡선, 집계값) | "Domain §7.2 숫자 예 재현" |
| T2 | `RoutePropagatorTest.reqDateFailsOnLateServiceStart` | §7.2 마지막 문단 변형 → `Infeasible(REQ_DATE)`. E1·E2 경계 포함 (serviceEnd는 조건 아님) | 〃 + "hard 위반 검출" |
| T3 | `StructureCheckTest.detectsXorViolations` | E11·E12·E13(위반 0) + DUPLICATE_NODE·UNKNOWN_REQUEST | "XOR 위반 검출" |
| T4 | `StructureCheckTest.detectsPairViolations` | E7·E8·E9·E10 각각 정확한 Kind로 검출 | 〃 (§1.4 pair는 XOR의 전제) |
| T5 | `RoutePropagatorTest.detectsHardViolations` | E3·E5·E6·E17·E23·E39: TIME_WINDOW·CAPACITY(출발 적재 포함)·WORK_WINDOW(배치 실패·빈 창 목록)·MAX_* 각 1건 + 통과 경계 — 용량 경계는 **출발 적재 10,000 == `maxWeight` 10,000 → 통과**(`≤`)를 실제로 전파해 확인한다. 경계용 차량을 만들고 쓰지 않으면 이 칸은 비어 있는 것이다. **운전 두 축**: 이 경로의 합계
5,400초·25,000m에 한도를 맞춰 `==`는 통과하고 1 낮추면 `MAX_DRIVE_TIME`·`MAX_DRIVE_DIST`(at 부재).
**부피**: 무게와 별개 분기이므로 출발 적재 부피만 넘긴 구성으로 `CAPACITY_VOLUME`(at 부재). **서비스 시작의 두 창 축**: 근무창과 방문지 창이 **같은 시각에 함께 소진**되면 `TIME_WINDOW`(at = 그 방문 — §3.3 절차 2의 "먼저 적힌 쪽") · **방문지** 창 목록이 비면 `TIME_WINDOW`(그 축이 즉시 소진 — E6의 "빈 창 목록"은 근무창 쪽 `WORK_WINDOW`를 가리킨다) | "hard 위반 검출" |
| T6 | `EvaluatorTest.incompatibleVehicleAndProfileHardAreHard` | E15 + 항상-false HardConstraint를 가진 테스트 profile → PROFILE_HARD (감점 통과 없음) | 〃 |
| T7 | `ProfileRegistryTest.unregisteredResolvesToDefault` | E24·E25·E26: 미등록·부재 → default, 등록 → 해당 profile (solver-profile 모듈) | "미등록 customerId → default profile" |
| T8 | `EvaluatorTest.usesGivenProfileInstance` | `evaluate(problem, profile, solution)`이 인자로 받은 그 profile의 `hardConstraints`·`score`만 호출 (스파이 profile로 확인). `Problem`에는 profile이 없다 | 〃 (§8.4 — 탐색·재검증 같은 profile의 전제) |
| T9 | `EvaluatorTest.aggregatesMetricsAndScoresLexicographically` | E13·E27·E28·E29·E31: metric 집계값 + default 4축 score + "거리만 보는" 테스트 profile에서 승패 역전 (§8.3 예 재현) + 길이 불일치 예외 | (Plan 범위 문장 "metric, 사전식 비교") |
| T10 | `RoutePropagatorTest.waitInDepotRelocatesFirstWait` | E20·E21·**E34**: Y/N에서 serviceStart 동일, 대기 귀속만 이동, routeOperationalTime 불변. 차고가 늦게 여는 케이스에서 **N인데도 depotWaiting > 0** | (Plan 범위 문장 "전파 루프·기록 값 §7.3") |
| T11 | `RoutePropagatorTest.stopCountAndDriveAggregation` | E18·E19·E22: 같은 장소 연속·depot 미산입·endDepot 포함 driveDist/Time | (Plan 범위 문장 "기록 값 §7.3") |
| T12 | `EvaluatorTest.hardConstraintReadsProblemFacts` | 차급(`vehicleFeature`)을 보고 판정하는 테스트 `HardConstraint` → `problem` 인자로 그 값에 도달함을 확인 (§4.3 고객 구현 예의 전제) | (노트 N2 — 이번 변경의 목적 자체) |
| T13 | `RoutePropagatorTest.defersToNextWorkWindow` | **다일 전파.** §3.5 대응표의 전 셀 — 2일차 16:30 90분 이동이 3일차 08:00으로 미뤄지고 `customerWaitingTimeSec += 1800`·`interWorkWindowRestTimeSec += 54000`(2026-08-11 정정 — 종전 목표값 "rest 55800"은 §7.3 공식과 모순), 도착 207000 (E37) · 서비스가 창을 넘는 변형 (E38) · 대기가 근무 시간/틈으로 갈리는 변형 (customerWaiting 3600 + rest 54000) | (Domain §7.2.1 재현 — D4 확정분) |
| T14 | `RoutePropagatorTest.depotWindowAppliesToDepartureAndReturn` | **차고 창.** E35(출발 불가, **Ds 목록이 먼저 소진되는 구성** → DEPOT_WINDOW, at 부재 — 창 구성은 E35의 데이터 조건 그대로) · E36(복귀가 창 틈 → DEPOT_WINDOW, 미루지 않음) · 차고 창이 전일이면 종전과 동일 · **두 축 동시 소진**(근무창이 첫 이동을 못 담고 차고 창도 함께 닫히는 구성) → `WORK_WINDOW`, at 부재 — E35의 "Ds가 **먼저** 소진"과 구분되는 별개 데이터 조건이다 (§3.3 절차 1의 2026-08-13 위반 귀속 규약) | (Domain §7.1 — D4 확정분) |
| T15 | `RoutePropagatorTest.routeOperationalTimeIdentityHolds` | **항등식.** 단일 창·다일·waitInDepot Y/N·endDepot 유무·**startDepot 유무** 조합에서 `routeOperationalTimeSec() == routeEndSec() − spanStartSec()` (Domain §7.3). E41·E42·E43 포함. 성분 정의가 어긋나면 여기서 먼저 깨진다 | (Domain §7.3 — 두 구현 대조의 전제) |
| T16 | `RoutePropagatorTest.singleWindowMatchesPreD4Values` | **회귀 방지.** 현행 fixture 모양(창 1개·차고 전일창·endDepot 없음)에서 `departureSec == serviceEndSec`(전 방문)·`interWorkWindowRestTimeSec == 0`·`depotWaitingTimeSec == 0` (E40) — D4가 1일 입력의 값을 바꾸지 않았다는 증명 | (Domain §3.2 — D4 무영향 근거) |
| T17 | `RoutePropagatorTest.zoneMixIsHard` | E44: A→ALL→A→A→ALL 통과 · A→ALL→B → `Infeasible(ZONE_MIX, at = B 방문)` · PD 양단 상이 → ZONE_MIX · ALL→ALL 통과 | Domain §3.4 경로 구역 단일성 |

T9–T16은 DoD 세 문장 밖이지만 Plan Stage 3 범위 문장(취지 — 적재 부호 규칙, 전파 절차·기록
값(§7.3), metric, 사전식 비교)과 Domain §3.2·§7.1(D4 확정분)의 직접 검증이다 — Plan §1의
편입(2026-08-11)에 따라 이 표 전부가 완료 기준이다.

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
| `depot.taskTime` 시간 계산 적용 | **canonical에 없다** — 복귀 선적 시간이고 1바퀴엔 발생하지 않는다 (Domain §2.5, 2026-08-11 확정). 차고 **창**은 적용한다 | Stage Extra E1 |
| 하루 단위 운전·정차 한도 (`maxDriveTime` 등을 날마다 나누기) | 안 함 — 경로 전체 합계 (§9 Q4) | Domain §2.4 |
| LEASE/DIRECT 비용 축·cost metric (현 입력·default profile 미사용) | 필요 시 profile 추가 | Domain §8.3 |
| ArchUnit 규칙 추가 (`verify ↛ solve`는 Stage 0에 이미 존재) | Stage 5 | Stage 0 §6 |

---

## 9. 미해결 질문

닫힌 질문은 해소 표시를 달아 남긴다 ([README](README.md) 공통 규칙, 2026-08-13).
**Q1·Q2·Q3는 전부 해소됐다** (Q2·Q3는 2026-08-10 D4 확정) — 이 절에 남은 미결은 Q4 하나다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | 시간창 close의 기준 — Domain §7.1은 serviceStart 공식과 "hard 검사: 시간창"만 정의하고, close 위반이 serviceStart 기준인지 serviceEnd 기준인지 명시하지 않았다 (reqDate는 serviceStart 기준을 명시) | **해소 (2026-08-10).** Domain §7.1이 `serviceStart ≤ closeTime`을 MUST로 확정했다 — `serviceEnd`는 close를 넘어도 통과(조건 아님). 잠정안 그대로이며, 이제 탐색(Stage 3)과 재검증(Stage 5)이 같은 문장을 보고 구현한다 |
| Q2 | depot open/close 창의 전파 적용 — 규약 PDF는 "차량은 depot 창 안에 출발·도착"을 정의하지만 Domain §7 전파 루프·§6.2 hard 목록에는 depot 창이 없었다 | **해소 (2026-08-10, [Plan §2.1 D4](../implementation-plan.md) 확정).** **적용한다**(잠정안의 "미적용"과 반대다). 차고 창도 날마다 반복되므로(Domain §3.2) 판정은 "출발·복귀 **순간**이 어느 차고 창 안인가"다 — 규약 원문 *"All vehicles must depart and arrive between opening time and closing time of depot."* 위반은 `DEPOT_WINDOW`(§3.1). 복귀는 미루지 않는다(E36). 반영분: §3.1 enum · §3.3 절차 1·3 · E34\~E36 · T14. `depot.taskTime`은 여전히 미적용(§8) |
| Q3 | 복수 근무창 — §7.3의 `interWorkWindowRestTime`은 복수 근무창을 암시하지만 canonical `Vehicle.workWindow`는 하나였다 | **해소 (2026-08-10, [Plan §2.1 D4](../implementation-plan.md) 확정).** Domain §3.2가 시간창 전개를 정본화해 근무창은 `List<TimeWindow>`가 됐다(Stage 1 §2.2). 창 끝을 넘는 이동·서비스는 **다음 창으로 미루고**, 미룬 시간이 `interWorkWindowRestTime`이다(정의는 Domain §7.3). 노트 N5의 동치는 깨졌으므로 N5를 폐기하고 "배치가 곧 검사"로 대체했다. 반영분: §3.2 record · §3.3 절차 · §3.5 · N5·N8 · E37\~E40 · T13·T15·T16 |
| Q4 | `maxDriveTime`·`maxDriveDist`·`maxStopCnt`가 다일 계획에서 **경로 전체 합계**인지 **하루치**인지 — 규약은 "Vehicle can't travel more than max driving time"만 적고 기간을 말하지 않는다 | 경로 전체 합계로 구현한다 (§3.3 절차 5 — 종전과 같다). 현행 fixture에는 세 필드가 아예 없어 무영향이다. 하루 한도가 필요해지면 canonical에 optional 축을 더하는 **확장**이지 이 절차의 변경이 아니다 (Domain §2.1.1) |
