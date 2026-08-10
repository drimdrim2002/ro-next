---
title: Stage 1 — canonical 입력과 정규화 (상세 구현 설계)
stage: 1
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../domain-design.md (§1 용어·식별자, §2 입력 계약, §2.1.1 확장 기준, §2.5 배송정책,
    §2.5.1 탐색설정, §3 정규화, §12 오류 분류)
  - ../architecture-design.md (§2 모듈·패키지, §2.3 계층 기준)
  - stage-00-cleanup-and-skeleton.md (§3.1 이름 기준)
revisions:
  - 2026-08-09 최초 작성
  - 2026-08-10 `SolveOptions` → `DeliveryPolicy` (탐색 예산·TravelCalcMode 분리, Domain §2.5·§2.5.1)
---

# Stage 1 — canonical 입력과 정규화

solver-core의 `domain` 패키지에 canonical 모델(`Plan`·`Request`·`Vehicle` 등)과
정규화 코드(단위·시간·serviceTime·호환성·오류 분류)를 만든다. 주 근거: [Domain §1–§3](../domain-design.md).
패키지 좌표는 [Stage 0 §3.1](stage-00-cleanup-and-skeleton.md)의 이름 기준을 그대로 잇는다.

**DoD** ([Plan Stage 1](../implementation-plan.md)): 단위·경계값(FLOOR, 소수 거부,
optional 부재 = 제약 없음) 단위 테스트 · `multiRotation != 0` 거부 테스트.

핵심 구도 — 두 층과 한 관문:

```text
[app adapter (Stage 6)] ──raw 운반체(*Input, 정확한 원시값)──▶ [PlanNormalizer] ──▶ [정규화된 canonical 모델 = Plan]
                                                               실패 시 InputException          (Stage 2가 Problem으로 동결)
```

- **raw 운반체** (`domain.input`): adapter가 채우는 정확한 원시값 그릇. `BigDecimal`·`LocalDateTime`·
  `LocalTime` — double 근사 금지(Domain §3.1)를 타입으로 지킨다.
- **정규화 모델** (`domain`): 내부 단위(long milli, 원점 기준 초)로 확정된 유일한 의미(Domain §3).
  솔버·재검증이 공유한다.
- 정규화 규칙(§3)은 solver-core 소유다. adapter(Stage 6)는 wire 형식 변환만 하고 의미 변환은
  전부 `PlanNormalizer`가 한다 — 같은 입력이면 같은 정규화 결과(Domain §3 서두).

---

## 1. 파일/클래스 목록

전부 `solver-core/src/main/java/com/ronext/rpdptw/` 아래 (Architecture §2, Stage 0 §3.1).

### 1.1 `domain` — 정규화된 canonical 모델과 규칙

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `domain/RequestId.java` | 일감 한 건 ID (record) | Domain §1.2 |
| `domain/VehicleId.java` | 차량 ID (record) | Domain §1.2 |
| `domain/NodeId.java` | "장소에서 하는 역할 한 칸" ID + 발급 팩토리 | Domain §1.2 |
| `domain/LocationId.java` | 지도상 장소 ID + locId 부재 시 좌표 기반 생성 | Domain §1.2·§2.3 |
| `domain/ServicePattern.java` | enum `DELIVERY_ONLY` / `PICKUP_DELIVERY` | Domain §1.3 |
| `domain/TimeWindow.java` | 양끝 포함 시간창 (원점 기준 초) | Domain §2.3·§3.2 |
| `domain/Location.java` | 장소 = LocationId + 좌표 (Stage 2 Great Circle 보정용) | Domain §4 |
| `domain/Item.java` | 개당 무게·부피(milli)·qty·taskTime | Domain §2.3·§3.1 |
| `domain/RequestSide.java` | 방문 쪽 하나: 창·duration·serviceTime·reqDate·zone | Domain §2.3·§3.3 |
| `domain/Request.java` | 운송 의무 한 건 (pair 원자 단위) | Domain §1.3·§2.3 |
| `domain/Vehicle.java` | 차량: 용량·근무창·optional 한도 축 | Domain §2.4·§2.6 |
| `domain/VehicleOwnership.java` | enum `DIRECT` / `LEASE` | Domain §2.4 |
| `domain/Depot.java` | 차고: 장소·창·(미적용) taskTime | Domain §2.2·§2.5 |
| `domain/Trips.java` | enum `ONEWAY` / `ROUNDTRIP` | Domain §2.5 |
| `domain/DeliveryPolicy.java` | 정규화된 **배송정책** (trips·waitInDepot·기본 속도) | Domain §2.5 |
| `domain/TravelEntry.java` | 단위 검증이 끝난 이동 arc 원시값 한 줄 | Domain §2.2·§4 |
| `domain/Plan.java` | 정규화 완료된 한 번 풀이의 봉투 | Domain §2.2 |
| `domain/TimeBase.java` | 시간 원점(planStart)·경과 초 변환 | Domain §3.2 |
| `domain/Units.java` | 단위 환산 유틸: milli FLOOR·정수성 검사 | Domain §3.1 |
| `domain/Compatibility.java` | 호환성 판정 predicate (size·capability·zone) | Domain §3.4 |
| `domain/InputException.java` | 입력 오류·미지원 입력 분류 예외 | Domain §12 |

### 1.2 `domain.input` — raw 운반체와 정규화 관문

| 파일 | 책임 한 줄 |
|---|---|
| `domain/input/PlanInput.java` | raw 봉투: 기간·차고·주문·차량·이동표·옵션 |
| `domain/input/RequestInput.java` | raw 주문 (side 운반체 + items + feature/capability) |
| `domain/input/SideInput.java` | raw 방문 쪽: locId/좌표·창·duration·reqDate·zone |
| `domain/input/ItemInput.java` | raw item: BigDecimal weight/volume·qty·taskTime |
| `domain/input/VehicleInput.java` | raw 차량: 전 필드 (optional은 null 허용) |
| `domain/input/DepotInput.java` | raw 차고 |
| `domain/input/OptionsInput.java` | raw 배송정책 옵션 (multiRotation 포함). **탐색 예산은 없다** (§2.2) |
| `domain/input/TravelEntryInput.java` | raw 이동표 한 줄 (from·to·D·U — `C` 등 기타 열은 안 받음, Domain §4) |
| `domain/input/PlanNormalizer.java` | **유일한 정규화 관문**: `PlanInput → Plan`, 실패 시 `InputException` |

app 모듈의 `app.input`(규약 JSON adapter, Stage 6)과 다른 패키지다 — adapter는 wire JSON을
파싱해 이 `*Input` 운반체를 채우는 것까지만 한다.

테스트 파일은 §7. `package-info.java`(Stage 0 생성)는 유지한다.

---

## 2. 식별자와 canonical 모델 시그니처

record는 전부 불변이다. 생성자(compact constructor)에서 null·blank 검증만 한다.
전체 구현 본문은 쓰지 않는다 — 여기 시그니처가 계약이다.

### 2.1 ID 4종 (Domain §1.2)

```java
public record RequestId(String value) {}      // 배정·bank·결과의 단위
public record VehicleId(String value) {}      // 경로 소유
public record LocationId(String value) {      // 이동표 조회 키
    /** locId 부재 시 좌표 '원문 문자열'로 생성 — double 왕복 없이 결정적 (Domain §2.3) */
    public static LocationId generated(String latText, String lonText);  // "@{lat},{lon}"
}
public record NodeId(String value) {          // 방문 순서·시간창의 칸
    public static NodeId pickup(RequestId id);     // "{id}:P"
    public static NodeId delivery(RequestId id);   // "{id}:D"
    public static NodeId depot(LocationId loc);    // "DEPOT:{loc}"
}
```

- 같은 장소라도 pickup 역할과 delivery 역할은 다른 `NodeId`다 (Domain §1.2).
  발급 규칙이 `RequestId` 유일성에서 `NodeId` 유일성을 보장한다.

### 2.2 정규화 모델 (Domain §2·§3의 결과 형태)

```java
public enum ServicePattern { DELIVERY_ONLY, PICKUP_DELIVERY }        // Domain §1.3
public enum VehicleOwnership { DIRECT, LEASE }                       // Domain §2.4
public enum Trips { ONEWAY, ROUNDTRIP }                              // Domain §2.5

public record TimeWindow(long openSec, long closeSec) {}             // 양끝 포함 (Domain §3.2)

public record Location(LocationId id, double latitude, double longitude) {}

public record Item(String itemId, long weightMilliKg, long volumeMilliCbm,
                   int qty, long taskTimeSec) {}                     // 전부 '개당' 값 (Domain §2.3)

public record RequestSide(
    NodeId nodeId,
    LocationId locationId,
    TimeWindow window,                 // 기본 00:00:00 / 23:59:59 앵커링 (Domain §2.3)
    long durationSec,                  // 방문 자체 시간 (item 작업시간과 별개)
    long serviceTimeSec,               // duration + Σ(taskTime×qty) — 정규화 시 확정 (Domain §3.3)
    long reqDateSec,                   // 기본 planEnd. serviceStart ≤ reqDate 전용 (Domain §2.3 MUST)
    Optional<String> zoneId) {}

public record Request(
    RequestId id,
    ServicePattern pattern,
    Optional<RequestSide> pickup,      // PICKUP_DELIVERY만 존재 — DELIVERY_ONLY의 pickup 쪽은
                                       // 방문을 만들지 않으므로 모델에서 부재로 강제 (Domain §1.3 MUST NOT)
    RequestSide delivery,
    List<Item> items,
    long totalWeightMilliKg,           // Σ(item 환산값 × qty) — item 단위 환산 후 합산 (Domain §3.1)
    long totalVolumeMilliCbm,
    Optional<Set<String>> allowedVehicleFeatures,  // empty = 전 차급 (["ALL"] 정규화, Domain §3.4)
    Set<String> requiredCapabilities) {}           // 빈 집합 = 미요구 (Domain §3.4)

public record Vehicle(
    VehicleId id,
    String vehicleFeature,             // 차급 코드 하나 (Domain §2.4)
    long maxWeightMilliKg,
    long maxVolumeMilliCbm,
    TimeWindow workWindow,             // workStart/workEnd (기본 00:00:00/23:59:59)
    OptionalInt speedKmH,              // Stage 2의 U 보정 체인 입력 (Domain §4)
    OptionalInt effectiveMaxStopCount, // min(차량 한도, 전역 한도) 접기 결과 (Domain §2.6)
    OptionalLong maxDriveTimeSec,
    OptionalLong maxDriveDistMeter,
    OptionalLong maxWidthMm, OptionalLong maxHeightMm, OptionalLong maxLengthMm,  // §9 Q3
    Set<String> capabilities,          // 빈 집합 = 능력 없음 (Domain §2.4)
    Optional<Set<String>> zoneIds,     // empty Optional = 전 구역 (Domain §2.4 MUST)
    Optional<VehicleOwnership> ownership,   // 부재 = 소유 축 미사용 (Domain §2.4)
    LocationId startDepot,
    Optional<LocationId> endDepot) {}  // trips 접기 결과 — §4.4 절차 5

public record Depot(
    NodeId nodeId, LocationId locationId, TimeWindow window,
    long taskTimeSec,                  // 보관만. 시간 계산 미적용 (Domain §2.5)
    Optional<String> zoneId) {}

public record DeliveryPolicy(
    Trips trips,
    boolean waitInDepot,               // Y/N (Domain §2.5)
    OptionalInt defaultSpeedKmH) {}    // U 누락 보정 체인 (Domain §4)

public record TravelEntry(LocationId from, LocationId to,
                          int distanceMeter, int timeSec) {}   // 단위 검증만 끝난 arc (Domain §4)

public record Plan(
    String planId,
    Optional<String> customerId,       // profile 선택용 — 부재 시 default profile (Domain §8.4)
    TimeBase timeBase,                 // origin = planStart (Domain §3.2)
    long planEndSec,                   // 계획 기간 [0, planEndSec) — 끝 미포함 (Domain §2.2)
    List<Depot> depots,
    List<Request> requests,
    List<Vehicle> vehicles,
    Map<LocationId, Location> locations,
    List<TravelEntry> travelEntries,   // 이동표 '재료' — 키 구성·완전성·누락 보정은 Stage 2 (Domain §4)
    DeliveryPolicy deliveryPolicy) {}
```

- optional 축은 전부 `Optional*` 타입이다. **부재 = 그 축의 제약을 아예 적용하지 않음**이며,
  큰 수 sentinel로 채우지 않는다 (Domain §2.4 MUST·§2.6 MUST NOT).
- 전역 `Optimizer.VehicleMaxStopCount`는 정규화에서 `effectiveMaxStopCount`로 접혀 소멸한다 —
  `DeliveryPolicy`에 남기지 않는다 (Domain §2.6의 min 규칙을 한 곳에서 끝냄).

**`Plan`에 담지 않는 것** (2026-08-10 — Domain §2.5.1):

| 값 | 이유 | 어디로 |
|---|---|---|
| `searchTimeLimitSec` (`Termination.secondsSpentLimit`) | 탐색 예산이다. 유효한 답의 집합을 바꾸지 않으므로 `Problem`에 동결되면 안 된다 | Stage 6 adapter가 wire에서 직접 읽어 `AlnsConfig`(Stage 4)로 넘긴다 |
| `distanceTimeCalculate` (`TravelCalcMode`) | 이동표 준비 동작을 분기시키지 않는 죽은 값이었다 (Stage 2 §9 Q3) | canonical에서 제거. 결과 run 메타에 남길지는 Stage 6 재량 |

두 값 모두 `domain`에서 사라진다 — `TravelCalcMode` enum도 만들지 않는다.

### 2.3 raw 운반체 (`domain.input`)

adapter가 wire 값을 **정확한 타입 그대로** 옮겨 담는 그릇이다. 필드는 canonical §2 표와 1:1이고,
optional·기본값 있는 필드는 null 허용이다. 대표 두 개만 명시하고 나머지는 §2.2의 raw 대응형이다:

```java
public record PlanInput(
    String planId,
    String customerId,                     // nullable — 규약 shprId 또는 협의 필드 (Domain §2.2)
    LocalDateTime planStart, LocalDateTime planEnd,   // adapter가 RFC3339도 이 형으로 통일 (Domain §2.2)
    List<DepotInput> depots, List<RequestInput> requests, List<VehicleInput> vehicles,
    List<TravelEntryInput> travelEntries, OptionsInput options) {}

public record ItemInput(
    String itemId,
    BigDecimal weightKg, BigDecimal volumeCbm,   // 정확값 — double 경유 금지 (Domain §3.1 MUST NOT)
    Integer qty,                                 // null → 1 (규약 default)
    Long taskTimeSec) {}                         // null → 0 (규약 default)
```

- `RequestInput(String orderId, SideInput pickup /*nullable*/, SideInput delivery, List<ItemInput> items, List<String> vehicleFeatures /*nullable*/, Set<String> requiredCapabilities /*nullable*/)`
  — pickup side가 있으면 `PICKUP_DELIVERY`, 없으면 `DELIVERY_ONLY`로 정규화된다 (Domain §1.3).
- `SideInput(String locId /*nullable*/, String latText, String lonText, LocalTime openTime, LocalTime closeTime, Long durationSec, LocalDateTime reqDate, String zoneId)` — 시각류 전부 nullable.
- `VehicleInput(String vehicleId, String vehicleFeature, BigDecimal maxWeightKg, BigDecimal maxVolumeCbm, LocalTime workStart, LocalTime workEnd, Integer speedKmH, Integer maxStopCnt, Long maxDriveTimeSec, Long maxDriveDistMeter, Long maxWidthMm, Long maxHeightMm, Long maxLengthMm, Set<String> capabilities, Set<String> zoneIds, String vhclOwnTyp, String startDepotLocId, String endDepotLocId)`
- `DepotInput(String locId, String latText, String lonText, LocalTime openTime, LocalTime closeTime, Long taskTimeSec, String zoneId)`
- `OptionsInput(String trips, Integer multiRotation, String waitInDepot, Integer defaultSpeedKmH, Integer globalVehicleMaxStopCount)`
  — `distanceTimeCalculate`·`searchTimeLimitSec` 필드는 **없다** (§2.2). 탐색 예산이 canonical에
  들어올 자리를 타입에서부터 없앤다.
- `TravelEntryInput(String fromLocId, String toLocId, BigDecimal distance, BigDecimal time)`
  — fixture의 `F`/`T`/`D`/`U` 대응. `C` 등 기타 열은 필드 자체가 없다 (Domain §4 MUST NOT의 구조적 강제).

---

## 3. 정규화 유틸 시그니처

```java
// Domain §3.2 — 시간 원점
public record TimeBase(LocalDateTime origin) {
    public long toSeconds(LocalDateTime t);      // t − origin 경과 초. 음수 허용 (§6 E17)
    public long anchor(LocalTime partial);       // origin '날짜'의 그 시각으로 고정한 경과 초 (§9 Q2)
    public LocalDateTime toWallClock(long sec);  // 결과 JSON 역변환용 (Domain §11, Stage 5·6 사용)
}

// Domain §3.1 — 단위. 실패 시 InputException(INVALID_INPUT)
public final class Units {
    public static long toMilli(BigDecimal value, String field);       // ×1000, 소수 3자리 FLOOR. 음수 거부
    public static int  toWholeMeters(BigDecimal value, String field); // 소수 거부 (Domain §3.1·§4)
    public static int  toWholeSeconds(BigDecimal value, String field);// 소수 거부. 음수 거부
}

// Domain §12 1·2행의 코드화 — app(Stage 6)이 4xx로 매핑
public final class InputException extends RuntimeException {
    public enum Kind { INVALID_INPUT, UNSUPPORTED_INPUT }
    public InputException(Kind kind, String field, String message);
    public Kind kind();
    public String field();                       // 예: "orders[3].items[0].weight"
}

// domain.input — 유일한 정규화 관문
public final class PlanNormalizer {
    public Plan normalize(PlanInput input);      // 실패 시 InputException. 상태 없음(stateless)
}
```

`Units.toMilli`의 FLOOR: `value.movePointRight(3).setScale(0, RoundingMode.FLOOR)` —
`BigDecimal` 경로만 사용하고 double을 경유하지 않는다 (Domain §3.1 MUST NOT).

---

## 4. `PlanNormalizer.normalize` 절차

번호 순서대로 실행하고, 첫 위반에서 `InputException`으로 중단한다.
"규칙으로 정해진 기본값"만 채우고 그 외 애매함은 오류다 — 추측 금지 (Domain §2.1).

```text
1. 봉투    planId non-blank. planStart < planEnd (아니면 INVALID_INPUT).
          timeBase = TimeBase(planStart), planEndSec = toSeconds(planEnd).
2. 정책    규약 default 채움: trips→"oneway", multiRotation→0, waitInDepot→"N".
          multiRotation이 core 지원 범위(trip 1개) 초과 → UNSUPPORTED_INPUT (Domain §2.5 MUST).
            판정식은 §9 Q1 확정 후 확정한다 — 값의 의미가 미확정이므로 지금은
            "0만 통과, 그 외 거부"로 구현하고 T5가 그 동작을 고정한다.
          trips ∉ {oneway,roundtrip} / waitInDepot ∉ {Y,N} → INVALID_INPUT.
          결과: DeliveryPolicy(trips, waitInDepot, defaultSpeedKmH).
            multiRotation은 검증만 하고 보관하지 않는다 (지원 범위가 1뿐이라 담을 정보가 없다).
3. 차고    1개 이상. LocationId = locId 또는 LocationId.generated(좌표 원문).
          창 앵커링(기본 00:00:00/23:59:59), taskTime ≥ 0 (기본 0).
          NodeId.depot 발급. 중복 LocationId → INVALID_INPUT.
4. 장소    차고·모든 request side의 (LocationId, 좌표) 수집 → locations 맵.
          같은 LocationId에 서로 다른 좌표 → INVALID_INPUT (추측 금지).
          좌표 파싱 실패·범위(±90/±180) 밖 → INVALID_INPUT.
5. 차량    중복 VehicleId → INVALID_INPUT.
          maxWeight/maxVolume → Units.toMilli. 근무창 앵커링(기본 00:00:00/23:59:59).
          optional 축(maxStopCnt·maxDrive*·치수·capabilities·zoneIds·speed):
            null → Optional.empty — sentinel 금지 (Domain §2.4 MUST·§2.6 MUST NOT).
            zoneIds가 '빈 집합'으로 오면 INVALID_INPUT (전 구역인지 금지인지 애매 — 추측 금지).
          vhclOwnTyp: null·"" → 부재. "DIRECT"|"LEASE" → enum. 그 외 non-empty → INVALID_INPUT (Domain §2.4).
          effectiveMaxStopCount = min(존재하는 것만: maxStopCnt, optionsInput.globalVehicleMaxStopCount).
            둘 다 없으면 Optional.empty (Domain §2.6).
          startDepot: 명시 → depots에 존재 검증(없으면 INVALID_INPUT).
            부재 → 차고가 정확히 1개면 그 차고, 여러 개면 INVALID_INPUT (§6 E20).
          endDepot 접기 (Domain §2.5): 명시 → 존재 검증 후 그 값 (oneway여도 유지 — "있으면 그것 우선").
            부재 + roundtrip → startDepot. 부재 + oneway → Optional.empty.
6. 주문    중복 RequestId → INVALID_INPUT. items 없음·빈 배열 → INVALID_INPUT (Domain §3.3).
          item별: qty null→1, qty < 1 → INVALID_INPUT (Domain §3.1 양의 정수).
            weight/volume → Units.toMilli (개당). taskTime null→0, 음수 → INVALID_INPUT.
          합산: total = Σ multiplyExact(개당 milli, qty) 를 addExact로 —
            overflow 시 INVALID_INPUT (Domain §3.1 overflow 검사).
          side별 (delivery는 필수, pickup은 있을 때만):
            NodeId 발급. 창 앵커링(기본 00:00:00/23:59:59). duration null→0, 음수 → INVALID_INPUT.
            serviceTime = duration + Σ(taskTime × qty)   (Domain §3.3 — 개당 taskTime × 수량)
            reqDate null → planEnd (Domain §2.3 기본값). 각 side가 자기 reqDate만 갖는다 (대체 금지 MUST).
          vehicleFeatures: null 또는 정확히 ["ALL"] → Optional.empty (전 차급).
            "ALL"이 다른 값과 섞임 / 빈 배열 → INVALID_INPUT (§6 E14).
          requiredCapabilities: null → 빈 집합 (미요구, Domain §3.4).
          pattern: pickup side 존재 여부로 확정 (Domain §1.3).
7. 이동표  entry별: from/to non-blank.
          D → Units.toWholeMeters, U → Units.toWholeSeconds — 소수·음수 거부 (Domain §3.1·§4).
          여기서는 단위 검증만 한다. self arc = 0 강제·완전성·누락 보정(Great Circle,
          ceil(D×3.6/speed))·조회 키 구성은 전부 Stage 2 (Domain §4·§5).
8. 조립    Plan 반환. 호환성 사실의 사전 계산·ID 참조 그래프 검증은 하지 않는다 —
          그것은 Problem 생성(Stage 2, Domain §5) 몫이다.
```

fixture 근거: `win_poc_case.json`의 `D: "310708.03"` 같은 소수 문자열은 절차 7에서 거부된다 —
그래서 최종 실행 fixture가 정수화된 [win_poc_case_floor.json](../../data/win_poc_case_floor.json)이다.
반면 두 fixture 모두 `multiRotation: "1"`이라 절차 2에서 거부된다 — Stage 0 §11 Q2로 이관된
미결 사항이며 Stage 1은 Domain §2.5 MUST대로 구현한다 (§9 Q1).
`"1"`이 "trip 1개"를 뜻한다고 확인되면 판정식 한 줄(`> 1`)만 바뀌고 나머지는 그대로다.

---

## 5. 호환성 판정 (Domain §3.4)

독립 hard 축의 순수 predicate다. Stage 1은 판정 **함수**만 만들고, 차량×주문 호환성 사실을
미리 계산해 저장하는 것은 Problem 동결(Stage 2, Domain §5)이 한다.

```java
public final class Compatibility {
    public static boolean size(Vehicle v, Request r);        // feature ∈ 목록 OR 목록 무제한
    public static boolean capability(Vehicle v, Request r);  // 미요구 OR 요구 ⊆ v.capabilities
    public static boolean zone(Vehicle v, RequestSide s);    // v.zoneIds 부재(전 구역) OR s.zoneId ∈ v.zoneIds
    public static boolean compatible(Vehicle v, Request r);  // 위 전부 AND (zone은 존재하는 모든 side)
}
```

- side의 `zoneId`가 부재이면 zone 축은 그 side에 대해 통과다 (검사할 값이 없으면 축 미사용 —
  Domain §2.4 optional 원칙과 동일).
- 치수 축은 **만들지 않는다** — canonical item에 치수 필드가 없어 검사 대상이 없다 (§9 Q3).
- 호환 차량이 0대인 Request는 오류가 아니다. 정규화·판정 어디서도 던지지 않고, 풀이 후
  미배정+사유(`NO_COMPATIBLE_VEHICLE`)로 남는다 (Domain §3.4, 사유 기록은 Stage 5).

---

## 6. Edge case 표

Domain의 optional 규칙·경계값·오류 분류(§2·§3·§12)에서 뽑았다. "거부"는 전부 `InputException`.

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | weight `26.2` kg | `26200` milli (정확 변환 — fixture 실값) | §3.1 |
| E2 | weight `1.23456` kg | FLOOR → `1234` milli | §3.1 FLOOR |
| E3 | weight `0.0009` kg | FLOOR → `0` (합법 — 0 허용) | §3.1 |
| E4 | weight/volume 음수 | INVALID_INPUT | §3.1 |
| E5 | 거리 `310708.03` / 시간 `17265.5` | INVALID_INPUT (소수 거부) | §3.1·§4 |
| E6 | `multiRotation`이 core 지원 범위 초과 — 잠정 판정 `!= 0` (fixture의 `"1"` 포함) | UNSUPPORTED_INPUT | §2.5 MUST·§12, §9 Q1 |
| E7 | qty 0·음수 | INVALID_INPUT. null → 1 | §3.1 |
| E8 | items 부재·빈 배열 (order 수준 taskTime만 있는 형태 포함) | INVALID_INPUT | §3.3 |
| E9 | 합산 overflow (Σ item×qty) | INVALID_INPUT (`multiplyExact`/`addExact`) | §3.1 |
| E10 | `maxStopCnt`·`maxDrive*`·치수 부재 | `Optional.empty` — 제약 없음, sentinel 금지 | §2.4·§2.6 |
| E11 | 차량 한도 부재 + 전역 `VehicleMaxStopCount=28` (fixture 상황) | effective = 28 | §2.6 min |
| E12 | 차량 30 + 전역 28 | effective = 28 | §2.6 min |
| E13 | `vhclOwnTyp` = `"OWN"` | INVALID_INPUT. `""`·null → 축 미사용 (DIRECT 채움 금지) | §2.4 |
| E14 | vehicleFeature `["ALL"]` | 무제한. `["ALL","T1"]`·`[]` → INVALID_INPUT (애매 — 추측 금지) | §3.4·§2.1 |
| E15 | 차량 zoneIds 부재 (fixture 전 차량) | 전 구역 가능 | §2.4 MUST |
| E16 | reqDate 부재 (fixture는 전건 planEnd 값 명시) | planEnd로 기본값 | §2.3 |
| E17 | reqDate < planStart | 수용 (음수 초로 정규화) — 실행 불가능성은 탐색·미배정의 일 | §2.3·§3.4 유추 |
| E18 | planStart ≥ planEnd | INVALID_INPUT | §2.2 |
| E19 | roundtrip + endDepot 부재 | endDepot := startDepot. oneway + endDepot 명시 → 그 값 유지 | §2.5 |
| E20 | 차량 startDepot 부재 (fixture 전 차량) + 차고 1개 | 그 차고로 확정. 차고 여러 개면 INVALID_INPUT | §2.5·§2.1 |
| E21 | 같은 LocationId에 다른 좌표 | INVALID_INPUT | §2.1 추측 금지 |
| E22 | locId 부재 | `LocationId.generated(좌표 원문)` — 같은 좌표 원문 = 같은 장소 | §2.3 |
| E23 | capability 미요구 (빈 집합) | 모든 차량과 capability 축 통과 | §3.4 |
| E24 | 호환 차량 0대인 Request | 오류 아님 — 정규화 통과, 풀이 진행 | §3.4 |
| E25 | 중복 orderId / vehicleId | INVALID_INPUT | §5의 ID 참조 무결성 전제 |
| E26 | 차량 vehicleFeature가 `"ALL"` | 문자 그대로 비교 (목록에 `"ALL"`이 있어야 매칭) — §3.4 식을 벗어난 해석 금지 | §3.4 |
| E27 | 시간창 close < open (앵커링 후) | INVALID_INPUT (잠정 — §9 Q2 확정 전까지) | §3.2 |

---

## 7. 테스트 목록 — DoD 1:1 대응

위치: `solver-core/src/test/java/com/ronext/rpdptw/domain/` (+ `domain/input/`).
의존은 JUnit만 — fixture JSON 파싱은 Jackson **3**(app, Stage 0 §4.4)이 필요하므로 Stage 6 통합 테스트의 일이고,
여기서는 fixture의 실값(26.2 kg, 310708.03 m, multiRotation "1", 전역 28)을 손으로 옮겨 쓴다.

| # | 테스트 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| T1 | `UnitsTest.floorsAtThirdDecimal` | E1·E2·E3 값 확인. `BigDecimal` 문자열 경로로 double 오차 없음 확인 | "FLOOR 단위 테스트" |
| T2 | `UnitsTest.rejectsNegative` | E4 → INVALID_INPUT | 단위·경계값 |
| T3 | `UnitsTest.rejectsFractionalDistanceAndTime` | E5 (fixture 실값) → INVALID_INPUT | "소수 거부" |
| T4 | `PlanNormalizerTest.absentOptionalMeansNoConstraint` | E10·E13·E15: 부재 축 전부 `Optional.empty`, sentinel 부재 확인 | "optional 부재 = 제약 없음" |
| T5 | `PlanNormalizerTest.rejectsMultiRotationNonZero` | E6: kind = UNSUPPORTED_INPUT. `0`은 통과 | "`multiRotation != 0` 거부 테스트" |
| T6 | `TimeBaseTest.secondsFromPlanStart` | 원점 변환·`anchor`·`toWallClock` 왕복·E17 음수 | 경계값 (Domain §3.2) |
| T7 | `PlanNormalizerTest.serviceTimeFormula` | duration + Σ(taskTime×qty) — qty 곱 포함 (Domain §3.3) | Stage 범위 문장 "serviceTime 공식" |
| T8 | `PlanNormalizerTest.reqDateDefaultsToPlanEnd` | E16, side별 독립 reqDate (Domain §2.3 MUST) | Stage 범위 문장 |
| T9 | `PlanNormalizerTest.foldsGlobalStopCount` | E11·E12 (min 접기) | 경계값 (Domain §2.6) |
| T10 | `PlanNormalizerTest.rejectsAmbiguousInput` | E8·E13·E14·E18·E21·E25 → INVALID_INPUT | 경계값·오류 분류 |
| T11 | `PlanNormalizerTest.deliveryOnlyHasNoPickupSide` | pattern 확정 + pickup `Optional.empty` (Domain §1.3 MUST NOT) | Stage 범위 문장 "canonical 모델" |
| T12 | `CompatibilityTest.axisTruthTable` | §3.4 네 식의 참/거짓 조합 + E23·E24·E26 | Stage 범위 문장 "호환성 판정" |
| T13 | `PlanNormalizerTest.depotResolution` | E19·E20 (endDepot 접기·startDepot 단일 차고 확정) | 경계값 (Domain §2.5) |

T7·T8·T11·T12·T13은 DoD 두 문장 밖이지만 Plan Stage 1 범위 문장("canonical 모델, serviceTime 공식,
호환성 판정, 오류 분류")의 직접 검증이다 — §8 보고에서 DoD 보강을 제안한다.

---

## 8. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| 이동표 키 구성·완전성 검사·self arc = 0 강제·누락 보정(Great Circle, `ceil(D×3.6/speed)`) | Stage 2 | Domain §4 |
| `Problem` 동결·ID 참조 그래프 검증·호환성 사실 사전 계산 | Stage 2 | Domain §5 |
| `Solution`·전파·평가·`Profile`(core `eval`)·`ProfileRegistry`(solver-profile 모듈) | Stage 3 | Domain §6–§8 |
| 미배정 사유 산출 (`NO_COMPATIBLE_VEHICLE` 등) | Stage 5 | Domain §11 |
| wire JSON 파싱, 규약↔canonical 필드 매핑 (RFC3339 통일, `shprId`→customerId, partial-time 문자열 파싱, `weightUnitCd`=KG 검증, `continent`·`lssId`·`district`·`DEPOT`·`locTcd`·`prodId` 등 canonical 밖 필드의 무시/검증) | Stage 6 adapter | Domain §2.1·Architecture §2 |
| `InputException` → HTTP 4xx 매핑, 접수 시 S3 미저장 | Stage 6 | Domain §12·Architecture §3.1 |
| fixture JSON을 직접 읽는 테스트 (Jackson 3 / app) | Stage 6 | Architecture §2.1 (core 의존 0), Stage 0 §4.4 |
| `depot.taskTime`의 시간 계산 적용 | 보류 (필드만 보관) | Domain §2.5 |
| 비용(cost) 축 처리 | Stage 3 평가에서 필요 시 | Domain §3.1·§8 |
| multiRotation 값 의미 확인 (fixture `"1"`) | Stage 6 전 결정 | Stage 0 §11 Q2 |
| 탐색 예산(`Termination.secondsSpentLimit`) 파싱·전달 | Stage 6 adapter → Stage 4 `AlnsConfig` | Domain §2.5.1, §2.2 |
| item 치수 필드 추가 | 3D 적재 고객 확정 시 (canonical에 optional 3필드) | Domain §2.1.1, §9 Q3 |

---

## 9. 미해결 질문

확정 문서로 답이 안 나오는 것만 남긴다. Stage 1 구현은 각 항목의 "잠정 처리"로 진행한다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | fixture 두 개 모두 `multiRotation: "1"` — 이 값이 "차량이 도는 횟수"(1 = trip 1개 = 지원)인지 "추가 회차 수"(1 = trip 2개 = 미지원)인지 미확정 (Stage 0 §11 Q2와 동일 사안) | Stage 1은 보수적으로 "0만 통과"를 구현·테스트(T5). 호출 시스템 확인 후 판정식 한 줄(`> 1`)만 바꾸면 되고, 타입·절차·다른 테스트는 영향 없음 |
| Q2 | partial-time(openTime·closeTime·workStart·workEnd)의 다일(多日) 기간 의미 — Domain §3.2는 원점·근무창 규칙만 정의하고, 계획 기간이 24h를 넘거나 planStart가 자정이 아닐 때의 앵커링·자정 넘는 창(close < open)·일 반복 여부를 정하지 않았다 (§7.3 `interWorkWindowRestTime`은 복수 근무창을 암시) | `TimeBase.anchor`는 origin 날짜 고정. close < open은 INVALID_INPUT (E27). 현행 fixture(자정 시작 24h)는 영향 없음. Stage 3(전파) 전 Domain 보완 필요 |
| Q3 | 차량 치수 한도(maxWidth 등, Domain §2.4)는 있는데 canonical item(§2.3)에 치수 필드가 없어 §3.4 치수 축의 검사 대상이 없다 | Vehicle에 필드만 보관(adapter 값 유실 방지), Compatibility에 치수 축 없음. **처리 경로는 확정됐다** — 3D 적재 고객이 확정되면 Domain §2.1.1에 따라 `Item`에 optional 치수 3필드를 추가하고, 판정은 그 고객 profile의 `HardConstraint`가 한다. 고객별 canonical·`Problem` 분기는 하지 않는다 |
| Q4 | speed의 소수 입력 — 규약 PDF는 double, Domain §3.1 소수 거부 목록엔 거리·시간만 있다 | 정수만 수용(`Integer`), 소수 speed는 INVALID_INPUT (fixture는 정수 `"45"`). Domain 확인 후 완화 가능 |
| Q5 | `item.taskTime` 해석 — Domain §3.3은 `× qty`로 확정(MUST 준수)인데, 규약 PDF는 "taskTime is calculated by item type not quantity"라고 반대로 적음 | Domain이 권위 — ×qty로 구현(T7). fixture는 taskTime=0이라 당장 무영향. Stage 8 Win 비교 때 taskTime≠0 케이스면 지표 차이 요인으로 기억 |
| Q6 | plan `customerId`의 wire 원천 — Domain §2.2가 "`shprId`(또는 협의 필드)"로 열어 둠. fixture는 plan `shprId="S3853"`와 order별 `customerId="WINCOMMERCE"`가 공존 | canonical은 `Optional<String>` 하나만 보유. 어느 wire 필드를 쓸지는 Stage 6 adapter에서 협의 확정. 부재 시 default profile (Domain §8.4) |
