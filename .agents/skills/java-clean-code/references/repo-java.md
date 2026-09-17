# 이 저장소 Java 관습

`java-clean-code` 스킬이 지시할 때만 읽는다. 툴체인은 **Java 25** (`.sdkmanrc` `25.0.3-amzn`, `maven.compiler.release` 25). Java 21이 아니다.

## 모듈별 허용

| 모듈 | 허용 | 금지 |
|---|---|---|
| `solver-core` | 순수 Java. 테스트 스코프만 JUnit·ArchUnit | Spring, Jackson, AWS, Lombok, 고객 라이브러리. compile 의존 0 |
| `solver-profile` | core의 `Profile` 구현. 고객 전용 외부 라이브러리는 여기만 | core를 역참조하지 않음. `if (customerId == …)` 를 core에 두지 않음 |
| `app` | Spring Boot 4.1, Jackson 3 (`JsonMapper` / `tools.jackson`) | Jackson 2. 컨트롤러·executor에서 S3 SDK 직접 호출 (→ `storage`) |

패키지 경계·어디에 둘지는 `project-map` 스킬과 Architecture §2.3.

## 값과 타입

- 도메인 값은 `record` + compact constructor. 컬렉션은 `List.copyOf` / `Set.copyOf` / `Map.copyOf`.
- 선택 필드는 `Optional` / `OptionalInt` / `OptionalLong`. `null`로 optional을 표현하지 않는다. compact ctor에서 `Objects.requireNonNull`.
- 유틸·엔트리는 `public final class` + private 생성자 (`Units`, `InsertionSearch`, `ProfileRegistry`).
- 헬퍼는 패키지-private. 테스트가 같은 패키지에 있어 공개 API를 늘리지 않고 검사한다.
- Lombok 없음. getter 생성기 없음. record 접근자 (`id()`, `routes()`)를 쓴다.
- 무게·부피는 `Units.toMilli` (×1000 FLOOR `long`). 거리 meter·시간 초는 정수. `double`로 근사한 뒤 변환하지 않는다. 단위 불변식은 `AGENTS.md`·Domain.

## 오류와 분기

- 입력 거부는 `InputException` + `Kind.INVALID_INPUT` / `UNSUPPORTED_INPUT`. 새 예외 계층을 만들지 않는다.
- core에 고객명 분기 금지. 고객 차이는 `solver-profile`의 `Profile` 구현만.

## 테스트

- JUnit 5, 클래스 이름은 `*Test`, 메서드는 동작 (`detectsXorViolations`).
- 테스트 클래스는 public이 아니다.
- 공용 픽스처는 같은 패키지 `*Fixtures`. 설계 절을 재현하는 이름은 절 좌표를 쓴다 (`section72Problem`).
- 단일 테스트는 `-pl <모듈>`. 명령 함정은 `AGENTS.md`.

## 주석

- `package-info.java`는 패키지 책임 + Domain 절.
- 본문 주석은 비명백한 제약·설계 절만 (`heuristics 문서 §3.3`, `XOR`).
- `ArchitectureRulesTest`처럼 긴 설명 주석을 새로 늘리지 않는다. 주변 파일 밀도를 따른다.
