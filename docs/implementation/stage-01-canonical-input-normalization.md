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
  - 2026-08-10 미해결 질문 포인터 정리 — Q1→Plan D1, Q2→Plan D4(미해소 유지), Q5→Plan D2,
    E27에 D4 재검토 표시. 설계(파일·클래스·시그니처·테스트) 무변경
  - 2026-08-10 **D4 확정 반영 (Q2 해소)** — 시간창을 `List<TimeWindow>`로 (Vehicle·Depot·RequestSide
    셋 다, Domain §3.2) · `TimeBase.anchor` → `dailyWindows` · §4 절차 3·5·6을 "전개"로 ·
    E27 뒤집기(자정 넘는 창 수용) + E27b\~E31 신설 · T14\~T16 신설 · §4에 1일 fixture 무영향 실측 추가
  - 2026-08-10 **D1 확정 반영 (Q1 해소) + 차량별 `trips`** — ① `multiRotation` 판정 반전:
    §4 절차 2를 집합 판정(통과 = `{0,1}`, `-1`·`2 이상` = UNSUPPORTED_INPUT, `≤ -2` =
    INVALID_INPUT, `> 1` 비교식 금지)으로 · E6 뒤집고 E6b·E6c 신설 · T5 개명·재정의
    (`rejectsMultiRotationNonZero` → `acceptsOneRotationRejectsMultiTrip`) · DoD 문장 갱신.
    ② 차량별 `trips`: §2.3 `VehicleInput`에 `trips`(nullable) 추가 · §4 절차 5에
    `차량 trips ▷ options.trips` 체인과 오값 검증 추가 · E19b\~E19e 신설 · T13 확장.
    canonical `Vehicle`에 `trips`는 남지 않는다(접기 유지) — 타입 변경은 raw 운반체 한 곳뿐
  - 2026-08-11 정리 — §7 말미 문구를 Plan §1 DoD 편입으로 갱신(존재하지 않는 "§8 보고" 참조
    제거), §8의 해소된 multiRotation 행 삭제(Plan D1으로 종결된 사안의 잔재). 설계 무변경
  - 2026-08-11 **오버엔지니어링 검토 — 선제 구현 3묶음 유예**. 기준은 "지금 안 쓰는가"가
    아니라 **"나중에 넣는 비용이 지금 넣는 비용과 같은가"**다. 순수 add-only인 것만 뺐고,
    나중에 못 넣는 것(정수 단위 체계 · `List<TimeWindow>` 다일 시간창 · pair 원자성)은
    **하나도 건드리지 않았다**. 뺀 것: 차량별 `trips`(§2.3·§4 절차5·E19b\~E19e·T13) ·
    치수 3필드(§2.2·§2.3) · `VehicleOwnership`(§1.1·§2.2·§2.3·E13) ·
    `Depot.nodeId`(소비처 0 — Stage 3 §4.4가 차고 NodeId를 색인에서 제외) ·
    `Depot.taskTimeSec`(= 복귀 선적 시간, 1바퀴엔 미발생). 등재부는
    [Stage Extra](stage-extra-deferred-features.md), 작업 목록 등재는 Plan §1 말미.
    함께: `Units`의 field 인자를 lazy로(호출부 0개인 지금이 최저가) · T12를 도달 가능한
    축으로 축소 · §9에서 해소된 Q1·Q2·Q5 삭제
  - 2026-08-11 **소수 규칙의 비대칭을 명시** (읽는 사람이 "소수 거부"를 무게·부피에까지
    적용되는 것으로 읽었다 — 문서 결함). 규칙 변경은 **없다**: 무게·부피는 종전대로 소수를
    받아 ×1000 FLOOR하고, 거리·시간만 거부한다 (Domain §3.1). 손댄 곳은 DoD 문장 · §3
    `Units` 세 메서드 주석 · §2.3 `TravelEntryInput`의 `BigDecimal` 사유 셋뿐이다
---

# Stage 1 — canonical 입력과 정규화

solver-core의 `domain` 패키지에 canonical 모델(`Plan`·`Request`·`Vehicle` 등)과
정규화 코드(단위·시간·serviceTime·호환성·오류 분류)를 만든다. 주 근거: [Domain §1–§3](../domain-design.md).
패키지 좌표는 [Stage 0 §3.1](stage-00-cleanup-and-skeleton.md)의 이름 기준을 그대로 잇는다.

**DoD** ([Plan Stage 1](../implementation-plan.md)): 단위·경계값(무게·부피 FLOOR,
거리·시간 소수 거부, optional 부재 = 제약 없음) 단위 테스트 · `multiRotation`이 지원 범위
`{0, 1}` 밖이면 거부하는 테스트 · `options.trips` 접기 테스트.

> **소수 규칙은 차원마다 반대다 (Domain §3.1 — 혼동 주의).**
> **무게·부피는 소수를 받는다** — 규약이 `decimal kg`/`decimal CBM`으로 정의하고,
> 정규화가 `×1000` 후 3자리 FLOOR로 `long`을 만든다 (E1\~E3·T1).
> **거리·시간만 소수를 거부한다** (E5·T3).
> fixture 실측이 이 비대칭을 강제한다: item 452건 중 weight 436건·volume 449건이 소수이고
> (`"26.2"`·`"0.21"`), 차량 `maxVolume`도 `"5.95"`다 — 무게·부피까지 거부하면 fixture가
> 한 건도 통과하지 못한다.

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
| `domain/Depot.java` | 차고: 장소·창 | Domain §2.2·§2.5 |
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

**여기 없는 것 — 2026-08-11 유예.** `VehicleOwnership.java`(파일 전체) · `Vehicle`의 치수
3필드 · 차량별 `trips` · `Depot`의 `nodeId`·`taskTimeSec`은 **wire에 없거나 소비처가 없어서**
만들지 않는다. 전부 순수 add-only라 필요해지는 시점에 필드 하나씩 되살리면 되고, 근거·트리거·
되살릴 지점은 [Stage Extra](stage-extra-deferred-features.md)에 등재돼 있다.

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
}                                             // 차고용 발급은 없다 — Route.visits는 고객 방문만
                                              // 담고 전파는 차고를 LocationId로 조회한다 (Stage 3 §4.4)
```

- 같은 장소라도 pickup 역할과 delivery 역할은 다른 `NodeId`다 (Domain §1.2).
  발급 규칙이 `RequestId` 유일성에서 `NodeId` 유일성을 보장한다.

### 2.2 정규화 모델 (Domain §2·§3의 결과 형태)

```java
public enum ServicePattern { DELIVERY_ONLY, PICKUP_DELIVERY }        // Domain §1.3
public enum Trips { ONEWAY, ROUNDTRIP }                              // Domain §2.5

public record TimeWindow(long openSec, long closeSec) {}             // 양끝 포함, openSec ≤ closeSec (Domain §3.2)

public record Location(LocationId id, double latitude, double longitude) {}

public record Item(String itemId, long weightMilliKg, long volumeMilliCbm,
                   int qty, long taskTimeSec) {}                     // 전부 '개당' 값 (Domain §2.3)

public record RequestSide(
    NodeId nodeId,
    LocationId locationId,
    List<TimeWindow> windows,          // 기본 00:00:00 / 23:59:59. 날마다 반복 → 전개 목록 (Domain §3.2)
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
    List<TimeWindow> workWindows,      // workStart/workEnd (기본 00:00:00/23:59:59). 날마다 반복 →
                                       // 전개 목록. 빈 목록 = 계획 기간에 못 쓰는 차량 (Domain §3.2)
    OptionalInt speedKmH,              // Stage 2의 U 보정 체인 입력 (Domain §4)
    OptionalInt effectiveMaxStopCount, // min(차량 한도, 전역 한도) 접기 결과 (Domain §2.6)
    OptionalLong maxDriveTimeSec,
    OptionalLong maxDriveDistMeter,
    Set<String> capabilities,          // 빈 집합 = 능력 없음 (Domain §2.4)
    Optional<Set<String>> zoneIds,     // empty Optional = 전 구역 (Domain §2.4 MUST)
    LocationId startDepot,
    Optional<LocationId> endDepot) {}  // trips 접기 결과 — §4 절차 5

public record Depot(
    LocationId locationId,
    List<TimeWindow> windows,          // 날마다 반복 → 전개 목록. 출발·복귀 순간에 적용 (Domain §7.1)
    Optional<String> zoneId) {}        // 차고는 Route.visits에 오지 않아 NodeId가 없다 (Stage 3 §4.4)

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

- **시간창은 전부 `List<TimeWindow>`다** (2026-08-10 — Domain §3.2, Plan D4). 규약이 주는 값은
  날짜 없는 `HH:mm:ss` 한 쌍이고 그것이 계획 기간의 날마다 반복되므로, 정규화가 **절대 창 목록으로
  펼쳐서** 확정한다 (§3 `TimeBase.dailyWindows`, §4 절차). 목록 불변식 (MUST): **open 오름차순 정렬 ·
  서로 겹치지 않음 · 맞닿은 창 병합 완료.** 전파(Stage 3)와 재검증(Stage 5)이 이 순서를 믿고
  포인터를 앞으로만 밀며 훑으므로 성능이 아니라 정합성 조건이다. **빈 목록은 오류가 아니다** —
  그 차량·차고를 계획 기간에 쓸 수 없다는 뜻이고, 그것을 쓰는 경로가 불가가 될 뿐이다 (E30).
  하루짜리 fixture에서는 목록 길이가 1이라 종전 단일 창과 값이 같다.
- optional 축은 전부 `Optional*` 타입이다. **부재 = 그 축의 제약을 아예 적용하지 않음**이며,
  큰 수 sentinel로 채우지 않는다 (Domain §2.4 MUST·§2.6 MUST NOT).
- 전역 `Optimizer.VehicleMaxStopCount`는 정규화에서 `effectiveMaxStopCount`로 접혀 소멸한다 —
  `DeliveryPolicy`에 남기지 않는다 (Domain §2.6의 min 규칙을 한 곳에서 끝냄).
- **`Trips`는 남고 `multiRotation`은 사라지는 이유** (2026-08-11 명시): 둘 다 계산에는
  안 쓰인다 — `trips`는 절차 5에서 `endDepot` 유무로 접히고, `multiRotation`은 검증만 한다.
  갈리는 지점은 **담을 정보가 있느냐**다. `multiRotation`은 통과 값 `{0, 1}`이 **둘 다
  1바퀴**라 어느 쪽이었는지가 아무 의미도 없다. `trips`는 `oneway`/`roundtrip`이 **서로 다른
  답을 만든** 값이라, 결과 run 메타(`Result.Run.deliveryPolicy` — Stage 5 §4)가 "이 배차안을
  만든 정책이 무엇이었나"를 기록할 때 실제로 쓰인다. 그것이 유일한 소비처이고, 그래서
  `DeliveryPolicy`에만 남기고 `Vehicle`에는 남기지 않는다.

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
- `VehicleInput(String vehicleId, String vehicleFeature, BigDecimal maxWeightKg, BigDecimal maxVolumeCbm, LocalTime workStart, LocalTime workEnd, Integer speedKmH, Integer maxStopCnt, Long maxDriveTimeSec, Long maxDriveDistMeter, Set<String> capabilities, Set<String> zoneIds, String startDepotLocId, String endDepotLocId)`
  — 차량별 `trips`·`vhclOwnTyp`·치수 3필드는 **없다** (2026-08-11 유예). 현행 규약 차량 키는
  `vehicleId`·`vehicleFeature`·`maxWeight`·`maxVolume`·`workStartTime`·`workEndTime`·`speed`
  7개뿐이라(fixture 실측) 읽을 값 자체가 없다 — [Stage Extra E1·E2·E3](stage-extra-deferred-features.md).
- `DepotInput(String locId, String latText, String lonText, LocalTime openTime, LocalTime closeTime, String zoneId)`
  — `taskTime`은 받지 않는다. 그 값은 **복귀 선적 시간**이고(Domain §2.5, 2026-08-11 확정)
  1바퀴에는 복귀가 없어 발생하지 않는다 — multi-trip을 열 때 함께 되살린다 (Stage Extra E1).
- `OptionsInput(String trips, Integer multiRotation, String waitInDepot, Integer defaultSpeedKmH, Integer globalVehicleMaxStopCount)`
  — `distanceTimeCalculate`·`searchTimeLimitSec` 필드는 **없다** (§2.2). 탐색 예산이 canonical에
  들어올 자리를 타입에서부터 없앤다.
- `TravelEntryInput(String fromLocId, String toLocId, BigDecimal distance, BigDecimal time)`
  — fixture의 `F`/`T`/`D`/`U` 대응. `C` 등 기타 열은 필드 자체가 없다 (Domain §4 MUST NOT의 구조적 강제).
  **거부 대상인데도 `BigDecimal`인 이유**: 거부하려면 먼저 소수임을 **봐야** 한다.
  `Long`으로 받으면 파싱 단계에서 죽어(`field` 경로 없는 형식 오류) 또는 조용히 잘려
  (`"310708.03"` → `310708` 통과) 둘 다 나쁘다. 원시값은 정확히 옮기고 거부 판단은
  core가 한다 — "의미 변환은 전부 `PlanNormalizer`"(§서두).

---

## 3. 정규화 유틸 시그니처

```java
// Domain §3.2 — 시간 원점과 시간창 전개
public record TimeBase(LocalDateTime origin) {
    public long toSeconds(LocalDateTime t);      // t − origin 경과 초. 음수 허용 (§6 E17)
    /**
     * 날마다 반복되는 창(open~close)을 계획 기간에 맞춰 절대 창 목록으로 펼친다 (Domain §3.2 규칙 1–5).
     * 전날부터 planEnd 날짜까지 생성 → close < open이면 다음 날로 넘김 → [0, planEndSec − 1]로 자름
     * → 정렬 후 맞닿은 창 병합. close == open이면 InputException(INVALID_INPUT) — §6 E27b.
     * 결과가 빈 목록일 수 있다 (계획 기간에 못 쓰는 차량·차고 — 오류 아님, §6 E30).
     */
    public List<TimeWindow> dailyWindows(LocalTime open, LocalTime close, long planEndSec, String field);
    public LocalDateTime toWallClock(long sec);  // 결과 JSON 역변환용 (Domain §11, Stage 5·6 사용)
}

// Domain §3.1 — 단위. 실패 시 InputException(INVALID_INPUT)
// 세 메서드의 소수 정책이 서로 반대다 — 무게·부피는 받고(FLOOR), 거리·시간은 거부한다.
public final class Units {
    /** 무게·부피 전용. 소수를 **받아서** ×1000 후 3자리 FLOOR (fixture weight "26.2" → 26200). 음수 거부 */
    public static long toMilli(BigDecimal value, Supplier<String> field);
    /** 거리 전용. 소수는 **거부** — 규약이 integer meter다 (Domain §3.1·§4) */
    public static int  toWholeMeters(BigDecimal value, Supplier<String> field);
    /** 시간 전용. 소수는 **거부**. 음수도 거부 */
    public static int  toWholeSeconds(BigDecimal value, Supplier<String> field);
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

**`field`가 `Supplier<String>`인 이유** (2026-08-11): 오류 경로(`"orders[3].items[0].weight"`)는
**실패했을 때만** 필요한데, `String`으로 받으면 성공 경로에서도 호출부가 매번 문자열을 조립한다.
floor fixture의 이동표는 205,209줄이고 줄마다 D·U 두 번 호출하므로 **성공만 해도 41만 번**이다.
호출부가 아직 0개인 지금이 이 시그니처를 정할 수 있는 가장 싼 시점이다.

---

## 4. `PlanNormalizer.normalize` 절차

번호 순서대로 실행하고, 첫 위반에서 `InputException`으로 중단한다.
"규칙으로 정해진 기본값"만 채우고 그 외 애매함은 오류다 — 추측 금지 (Domain §2.1).

```text
1. 봉투    planId non-blank. planStart < planEnd (아니면 INVALID_INPUT).
          timeBase = TimeBase(planStart), planEndSec = toSeconds(planEnd).
2. 정책    규약 default 채움: trips→"oneway", multiRotation→0, waitInDepot→"N".
          multiRotation 판정 (Domain §2.5 MUST — 숫자는 차량이 도는 바퀴 수):
            통과는 집합 {0, 1} 둘뿐이다 (0 = 미설정 = 1바퀴로 취급).
            -1(무제한 복귀) 또는 2 이상  → UNSUPPORTED_INPUT.
            -2 이하 (규약이 "greater than -1"로 금지) → INVALID_INPUT.
            비교식 `> 1`로 쓰지 않는다 — -1이 게이트를 통과해 버린다 (MUST NOT).
          trips ∉ {oneway,roundtrip} / waitInDepot ∉ {Y,N} → INVALID_INPUT.
          결과: DeliveryPolicy(trips, waitInDepot, defaultSpeedKmH).
            trips는 **전체 설정 하나**다 (차량별 지정은 유예 — Stage Extra E1).
            multiRotation은 검증만 하고 보관하지 않는다 (통과한 0·1이 둘 다 "1바퀴"라
            담을 정보가 없다). Vehicle에 multiRotation 필드를 만들지 않는다 (Domain §2.5).
3. 차고    1개 이상. LocationId = locId 또는 LocationId.generated(좌표 원문).
          창 전개 = timeBase.dailyWindows(open, close, planEndSec) (기본 00:00:00/23:59:59,
            Domain §3.2 규칙 1–5 — 날마다 반복·자정 넘김·클리핑·병합).
          중복 LocationId → INVALID_INPUT.
4. 장소    차고·모든 request side의 (LocationId, 좌표) 수집 → locations 맵.
          같은 LocationId에 서로 다른 좌표 → INVALID_INPUT (추측 금지).
          좌표 파싱 실패·범위(±90/±180) 밖 → INVALID_INPUT.
5. 차량    중복 VehicleId → INVALID_INPUT.
          maxWeight/maxVolume → Units.toMilli.
          근무창 전개 = timeBase.dailyWindows(workStart, workEnd, planEndSec)
            (기본 00:00:00/23:59:59. 빈 목록도 정상 — 그 차량은 계획 기간에 못 쓴다, §6 E30).
          optional 축(maxStopCnt·maxDrive*·capabilities·zoneIds·speed):
            null → Optional.empty — sentinel 금지 (Domain §2.4 MUST·§2.6 MUST NOT).
            zoneIds가 '빈 집합'으로 오면 INVALID_INPUT (전 구역인지 금지인지 애매 — 추측 금지).
          effectiveMaxStopCount = min(존재하는 것만: maxStopCnt, optionsInput.globalVehicleMaxStopCount).
            둘 다 없으면 Optional.empty (Domain §2.6).
          startDepot: 명시 → depots에 존재 검증(없으면 INVALID_INPUT).
            부재 → 차고가 정확히 1개면 그 차고, 여러 개면 INVALID_INPUT (§6 E20).
          endDepot 접기 (Domain §2.5) — 절차 2의 options.trips로 판정한다
            (차량별 trips는 wire에 없어 유예 — Stage Extra E1):
            명시 → 존재 검증 후 그 값 (oneway여도 유지 — "있으면 그것 우선").
            부재 + roundtrip → startDepot. 부재 + oneway → Optional.empty.
            접은 뒤 canonical Vehicle에 trips 필드는 남지 않는다 (Domain §2.5 접기 유지 판단).
6. 주문    중복 RequestId → INVALID_INPUT. items 없음·빈 배열 → INVALID_INPUT (Domain §3.3).
          item별: qty null→1, qty < 1 → INVALID_INPUT (Domain §3.1 양의 정수).
            weight/volume → Units.toMilli (개당). taskTime null→0, 음수 → INVALID_INPUT.
          합산: total = Σ multiplyExact(개당 milli, qty) 를 addExact로 —
            overflow 시 INVALID_INPUT (Domain §3.1 overflow 검사).
          side별 (delivery는 필수, pickup은 있을 때만):
            NodeId 발급. 창 전개 = timeBase.dailyWindows(open, close, planEndSec)
              (기본 00:00:00/23:59:59 — 주문 창도 날마다 반복한다, Domain §3.2).
            duration null→0, 음수 → INVALID_INPUT.
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
반면 `multiRotation`은 절차 2를 **통과한다** — 규약 원본 값 `"1"`은 1바퀴라 지원 범위 안이다
([Plan §2.1 D1](../implementation-plan.md) 확정). 통과 집합이 `{0, 1}`이므로 이 필드로
거부되는 fixture는 없다.

**창 전개가 현행 fixture를 바꾸지 않는다는 근거** (2026-08-10 실측, `win_poc_case_floor.json`):
`dateRange` = `2023-09-13 00:00:00` \~ `2023-09-14 00:00:00`(정확히 24h·자정 정렬)이므로
`planEndSec = 86400`이고, 전개 규칙 1이 만드는 세 날짜(09-12·09-13·09-14) 중 앞뒤 둘은 규칙 4에서
잘려 사라진다.

| 대상 | 입력 (실측) | 전개 결과 |
|---|---|---|
| 차량 31대 **전원** | `workStartTime "00:00:00"` / `workEndTime "23:30:00"` | `[[0, 84600]]` — **창 1개** |
| 차고 1개 (`WIN_0`) | `openTime "00:00:00"` / `closeTime "23:59:59"` | `[[0, 86399]]` — **창 1개** |
| 주문 452건 | `05:45~10:30`(282건) · `05:45~13:30`(169건) · `05:45~17:30`(1건) | 각 **창 1개** |

창이 하나면 병합·미루기·`interWorkWindowRestTime`이 모두 발생하지 않으므로 전개 전과 값이 같다.
차고 창도 `[0, 86399]`라 출발 시각을 앞당기지도 늦추지도 않고, `trips: "oneway"` + 차량에
`endDepot` 필드가 없어 복귀 검사 자체가 없다 → **`DEPOT_WINDOW`는 이 fixture에서 발화하지 않는다.**
`planEnd`(86400)가 차량 `workEnd`(84600)보다 늦으므로 규칙 4의 클리핑도 무영향이다.

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
- 치수 축은 **만들지 않는다** — canonical `Item`에도 `Vehicle`에도 치수 필드가 없어 검사
  대상이 없다 (2026-08-11 유예, [Stage Extra E2](stage-extra-deferred-features.md)).
- `capability`·`zone` 두 축은 **현행 wire에서 항상 참이다** — 차량에 `capabilities`·`zoneIds`가
  없기 때문이다(fixture 차량 7키). 식은 Domain §3.4가 정의하므로 그대로 만들되, 테스트는
  도달 가능한 조합만 다룬다 (§7 T12).
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
| E6 | `multiRotation` = `0`·`1` (fixture 실값 `"1"` 포함) | **통과** — 둘 다 1바퀴 (D1 확정) | §2.5 MUST |
| E6b | `multiRotation` = `2` 이상, 또는 `-1`(무제한 복귀) | UNSUPPORTED_INPUT — 차고 재방문은 범위 밖 | §2.5 MUST·§12 |
| E6c | `multiRotation` = `-2` 이하 | INVALID_INPUT — 규약이 "greater than -1"로 금지한 값 | §2.5·§12 |
| E7 | qty 0·음수 | INVALID_INPUT. null → 1 | §3.1 |
| E8 | items 부재·빈 배열 (order 수준 taskTime만 있는 형태 포함) | INVALID_INPUT | §3.3 |
| E9 | 합산 overflow (Σ item×qty) | INVALID_INPUT (`multiplyExact`/`addExact`) | §3.1 |
| E10 | `maxStopCnt`·`maxDrive*` 부재 | `Optional.empty` — 제약 없음, sentinel 금지 | §2.4·§2.6 |
| E11 | 차량 한도 부재 + 전역 `VehicleMaxStopCount=28` (fixture 상황) | effective = 28 | §2.6 min |
| E12 | 차량 30 + 전역 28 | effective = 28 | §2.6 min |
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
| E27 | 시간창 close < open (예: 야간조 `22:00~06:00`) | **수용 — 자정을 넘는 창이다** (2026-08-10 D4 확정, 종전 INVALID_INPUT을 뒤집음). close를 다음 날짜의 그 시각으로 보고 전개한다. 계획 시작 '전날'에 시작한 창이 첫날 아침까지 이어지는 부분도 잘려 들어온다 | Domain §3.2 규칙 1·2 |
| E27b | 시간창 close == open (예: `09:00~09:00`) | **INVALID_INPUT (신규)** — 1초짜리 창인지 24시간인지 애매하다 (추측 금지). E27을 수용하면서 생긴 구멍을 닫는다 | Domain §3.2 규칙 3·§2.1 |
| E28 | 기본창 `00:00:00~23:59:59`, 3일 계획 | `[0,86399]`·`[86400,172799]`·`[172800,259199]`가 **병합돼 `[0,259199]` 하나**. 자정마다 1초 틈이 남으면 자정을 넘는 이동이 전부 막힌다 | Domain §3.2 규칙 5 |
| E29 | planStart가 자정이 아님 (1일차 10:00) + 근무 `08:00~17:00` | 1일차 창이 앞에서 잘려 `[0(=10:00), 25200(=17:00)]`. 날짜 기준은 timezone 없는 벽시계 날짜다 (Domain §2.2) | Domain §3.2 규칙 1·4 |
| E30 | 전개 결과가 **빈 목록**인 차량·차고 (예: 2시간 계획 `10:00~12:00`에 근무 `08:00~09:00`) | **오류 아님.** 그 차량·차고를 계획 기간에 쓸 수 없다는 뜻이고, 그것을 쓰는 경로가 불가가 될 뿐이다 (E24 "호환 차량 0대"와 같은 취급) | Domain §3.2·§3.4 |
| E31 | planEnd가 `workEnd`보다 이름 (계획 18:00 종료, `workEnd` 23:59:59) | 근무창이 `[…, planEndSec − 1]`로 잘린다 — **하루짜리 계획에서도 동작이 바뀌는 지점**이다 (종전에는 planEnd를 넘겨 끝나는 경로를 아무도 막지 않았다). 근거는 규약 `dateRange` 설명 "Solver will create a plan in date range" | Domain §3.2 규칙 4 |

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
| T4 | `PlanNormalizerTest.absentOptionalMeansNoConstraint` | E10·E15: 부재 축 전부 `Optional.empty`, sentinel 부재 확인 | "optional 부재 = 제약 없음" |
| T5 | `PlanNormalizerTest.acceptsOneRotationRejectsMultiTrip` | **통과 집합 고정.** E6: `0`·`1` 통과(fixture 실값 `"1"` 포함) · E6b: `2`·`5`·`-1` → UNSUPPORTED_INPUT · E6c: `-2` → INVALID_INPUT. **`-1` 케이스가 이 테스트의 핵심**이다 — 판정을 `> 1` 비교식으로 쓰면 여기서만 깨진다 | "`multiRotation` 지원 범위 `{0,1}` 밖 거부 테스트" |
| T6 | `TimeBaseTest.secondsFromPlanStart` | 원점 변환·`toWallClock` 왕복·E17 음수 | 경계값 (Domain §3.2) |
| T7 | `PlanNormalizerTest.serviceTimeFormula` | duration + Σ(taskTime×qty) — qty 곱 포함 (Domain §3.3) | Stage 범위 문장 "serviceTime 공식" |
| T8 | `PlanNormalizerTest.reqDateDefaultsToPlanEnd` | E16, side별 독립 reqDate (Domain §2.3 MUST) | Stage 범위 문장 |
| T9 | `PlanNormalizerTest.foldsGlobalStopCount` | E11·E12 (min 접기) | 경계값 (Domain §2.6) |
| T10 | `PlanNormalizerTest.rejectsAmbiguousInput` | E8·E14·E18·E21·E25 → INVALID_INPUT | 경계값·오류 분류 |
| T11 | `PlanNormalizerTest.deliveryOnlyHasNoPickupSide` | pattern 확정 + pickup `Optional.empty` (Domain §1.3 MUST NOT) | Stage 범위 문장 "canonical 모델" |
| T12 | `CompatibilityTest.axisTruthTable` | **size 축의 참/거짓 조합**(E26·E14의 `["ALL"]` 포함)과 `compatible`의 AND 결합 + E23·E24. `capability`·`zone`은 **현행 wire에서 항상 참**이라(차량에 해당 필드 없음) 미요구/부재 시 통과만 확인한다 — 도달 불가능한 조합의 진리표를 만들지 않는다 | Stage 범위 문장 "호환성 판정" |
| T13 | `PlanNormalizerTest.depotResolution` | E19·E20: `options.trips = roundtrip` + endDepot 부재 → `startDepot` · `oneway` + endDepot 명시 → 그 값 유지 · startDepot 부재 + 차고 1개 → 그 차고, 여러 개 → INVALID_INPUT | 경계값 (Domain §2.5) |
| T14 | `TimeBaseTest.expandsDailyWindows` | **창 전개 전수.** E28(기본창 3일 → 병합돼 1개) · E29(planStart 비자정 클리핑) · E31(planEnd 클리핑) · E30(빈 목록, 예외 없음) · Domain §3.2 예2(근무 08:00\~17:00 3일 → `[28800,61200]`·`[115200,147600]`·`[201600,234000]`) · 결과의 불변식(정렬·비겹침·병합 완료) | 경계값 (Domain §3.2) |
| T15 | `TimeBaseTest.acceptsOvernightWindowAndRejectsZeroLength` | **야간조 케이스.** E27: `22:00~06:00` 3일 계획 → 전날에서 넘어온 `[0, 첫날 06:00]` + 날마다 `[22:00, 다음날 06:00]` · E27b: `09:00~09:00` → INVALID_INPUT | 경계값 (Domain §3.2 규칙 2·3) |
| T16 | `PlanNormalizerTest.floorFixtureWindowsStayOneEach` | **1일 fixture 무영향 증명.** §4 말미 실측 표의 세 값(차량 `00:00:00~23:30:00` → `[[0,84600]]`, 차고 `00:00:00~23:59:59` → `[[0,86399]]`, 주문 `05:45~10:30` → 창 1개)을 `planEnd=86400`으로 손 조립해 **목록 길이가 전부 1**임을 단언 | 경계값 (Domain §3.2 — 회귀 방지) |

T7·T8·T11\~T16은 DoD 두 문장 밖이지만 Plan Stage 1 범위 문장("canonical 모델, serviceTime 공식,
호환성 판정, 오류 분류")과 Domain §3.2 시간창 전개의 직접 검증이다 — Plan §1의 편입(2026-08-11)에
따라 이 표 전부가 완료 기준이다.

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
| `depot.taskTime` — **읽지도 담지도 않는다** | [Stage Extra E1](stage-extra-deferred-features.md) (multi-trip) | 복귀 선적 시간이라 1바퀴엔 미발생 (Domain §2.5) |
| 비용(cost) 축 처리 | Stage 3 평가에서 필요 시 | Domain §3.1·§8 |
| 탐색 예산(`Termination.secondsSpentLimit`) 파싱·전달 | Stage 6 adapter → Stage 4 `AlnsConfig` | Domain §2.5.1, §2.2 |
| 치수 축 전부 (item 3필드 + 차량 한도 3필드) | [Stage Extra E2](stage-extra-deferred-features.md) (3D 적재 고객 확정 시) | Domain §2.1.1 |
| 차량별 `trips` | [Stage Extra E1](stage-extra-deferred-features.md) | wire에 필드 없음 (Domain §2.4 유예 표) |
| 차량 소유 구분(`VehicleOwnership`) | [Stage Extra E3](stage-extra-deferred-features.md) | 소비처 0 (Domain §2.4 유예 표) |

---

## 9. 미해결 질문

확정 문서로 답이 안 나오는 것만 남긴다. Stage 1 구현은 각 항목의 "잠정 처리"로 진행한다.

닫힌 질문은 여기 남기지 않는다 (2026-08-11 정리) — `multiRotation` 의미는 [Plan D1](../implementation-plan.md),
다일 시간창은 [Plan D4](../implementation-plan.md), 치수 축은 [Stage Extra E2](stage-extra-deferred-features.md),
`taskTime × qty`는 [Domain §3.1·§3.3](../domain-design.md)(2026-08-11 소유자 확정)이 정본이다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q4 | speed의 소수 입력 — 규약 PDF는 double, Domain §3.1 소수 거부 목록엔 거리·시간만 있다 | 정수만 수용(`Integer`), 소수 speed는 INVALID_INPUT (fixture는 정수 `"45"`). Domain 확인 후 완화 가능 |
| Q6 | plan `customerId`의 wire 원천 — Domain §2.2가 "`shprId`(또는 협의 필드)"로 열어 둠. fixture는 plan `shprId="S3853"`와 order별 `customerId="WINCOMMERCE"`가 공존 | canonical은 `Optional<String>` 하나만 보유. 어느 wire 필드를 쓸지는 Stage 6 adapter에서 협의 확정. 부재 시 default profile (Domain §8.4) |
