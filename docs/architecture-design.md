---
title: RO-Next Architecture Design
status: CONFIRMED
date: 2026-08-10
supersedes: docs/deprecated/2026-07-31-phase-b-architecture-design.md
master: docs/master-design.md
domain: docs/domain-design.md
revisions:
  - 2026-08-09 최초 확정 (ECS Fargate·2모듈)
  - 2026-08-10 3계층 확정 — 모듈 3개(core·profile·app), Profile을 Problem에서 분리,
    comparator → long[] score, 배송정책/탐색설정 분리
  - 2026-08-10 app 툴체인 — Spring Boot 4.1 + Jackson 3 (`JsonMapper` / `tools.jackson`)
  - 2026-08-10 D1 확정 반영 — §3.1-2 접수 게이트 문면을 `multiRotation != 0`에서
    `{0,1} 밖`으로 (Domain §2.5). 흐름·모듈·경계 규칙 무변경
  - 2026-08-11 접수 깊이 확정 — §3.1을 "파싱 + 정규화 검증까지 접수에서 실행, 의미 오류도 4xx"로
    개정. Domain §12와 충돌하던 "형식 수준만 검증" 문면 폐기 (Domain이 정본 — Stage 6 §10 Q3 결정)
  - 2026-08-11 §3.1 접수 4xx 예시에서 `vhclOwnTyp` 제거 — 그 필드를 읽지 않기로 해
    (Domain §2.4 유예 표) 예시로 성립하지 않는다. 접수 검증 범위는 무변경
---

# RO-Next Architecture Design

코드·모듈·인프라 **배치**를 정의하는 문서다. 배차 문제의 의미는 [Domain Design](domain-design.md)이
소유하며, 이 문서는 그 의미를 바꾸지 않는다.

확정 인프라: **AWS ECS Fargate 위의 단일 Spring Boot 서비스, 저장은 S3만** (2026-08-09).
이전 문서의 Lambda/ECS 미결정, Step Functions 오케스트레이션, 분산 라운드(champion) 구조,
YAML 2층 카탈로그, 십수 개 Maven 모듈은 모두 폐기했다.

확정 코드 구조: **3계층 = core · profile · app** (2026-08-10). 계층마다 변하는 속도와
허용 의존이 다르다 — core는 거의 안 변하고 외부 의존이 0, profile은 고객이 늘 때마다
늘어나며 외부 라이브러리를 쓸 수 있고, app은 인프라 설정만 안다.

## 1. 한 장 그림

```text
                        ┌──────────────────────────────────────────────┐
[호출 시스템]            │  ro-next 앱 (Spring Boot, ECS Fargate 컨테이너) │
   │ POST /solves ────▶ │  [api]  검증 → S3에 input 저장 → 200 + solveKey │
   │                    │            │                                  │
   │                    │            ▼ (비동기, 같은 프로세스)             │
   │                    │  [run]  executor: input 로드 → adapter →       │
   │                    │         Problem 동결 → 초기해 → ALNS →         │
   │                    │         재검증 → 결과/상태를 S3에 저장           │
   │ GET /solves/… ───▶ │  [api]  S3에서 상태/결과 읽어 반환               │
                        └──────────────┬───────────────────────────────┘
                                       │ (유일한 외부 의존)
                                     [ S3 ]
                                입력 · 상태 · 결과
```

- 서비스 하나, 저장소 하나. DB·Redis·SQS·Step Functions·Lambda 없음 (MUST NOT).
- 접수 HTTP 요청은 **검증 + S3 저장 + solveKey 반환까지만** 동기다.
  ALNS 완주를 HTTP 요청 안에서 기다리는 설계 금지 (MUST NOT).
- 호출 시스템은 응답의 `solveKey`로 진행·결과를 확인한다 — 조회 API가 기본이고,
  S3 권한이 있으면 직접 읽어도 된다.

## 2. 모듈 구조 — 3개 (확정)

```text
ro-next/
├── pom.xml                        # parent (modules: solver-core, solver-profile, app)
│
├── solver-core/                   # 순수 Java — 외부 라이브러리 의존 0. 거의 안 변함
│   └── com.ronext.rpdptw
│       ├── domain/                # canonical 입력 모델·정규화·배송정책 (Domain §1–4)
│       ├── problem/               # Problem 동결·검증 (Domain §5)
│       ├── eval/                  # 평가 계약 + profile SPI + 기본 구현 (Domain §7.3·§8)
│       ├── solve/                 # Solution·전파·ALNS·탐색설정 (Domain §6–9)
│       └── verify/                # 독립 재검증 (Domain §10)
│
├── solver-profile/                # 고객 정책만. 외부 라이브러리는 여기서만 허용
│   └── com.ronext.rpdptw.profile
│       ├── ProfileRegistry        # customerId → Profile (미등록 → default)
│       └── (고객별 패키지)          # 현재 0개
│
└── app/                           # Spring Boot — 배포 단위 (ECS 이미지 1개)
    └── com.ronext.rpdptw.app
        ├── api/                   # REST 컨트롤러 (접수·조회)
        ├── run/                   # SolveExecutor: 비동기 풀이 실행·상태 갱신
        ├── input/                 # 규약 JSON ↔ canonical adapter (Jackson 3 / JsonMapper)
        └── storage/               # SolveStore 인터페이스 + S3 구현 + 로컬 fake
```

**모듈 의존** — `app → solver-profile → solver-core` (한 방향, 역방향은 컴파일 불가)

**core 내부 패키지 의존** — 순환 없음

```text
domain  ──▶ (없음)
problem ──▶ domain
eval    ──▶ domain, problem
solve   ──▶ domain, problem, eval
verify  ──▶ domain, problem, eval        # solve 금지 (ArchUnit)
```

`eval`이 `problem`을 볼 수 있는 이유는 `Problem`이 `Profile`을 담지 않기 때문이다 (§2.2).
그래서 profile hard 제약·score가 차급·구역·치수 같은 **문제 사실을 직접 읽을 수 있다.**

### 2.1 경계 규칙과 강제 수단

| 규칙 | 강제 수단 |
|---|---|
| `solver-core`에 Spring/AWS SDK/Jackson 유입 금지 | **컴파일 차단** — solver-core pom에 해당 의존성이 없음 (test scope 제외) |
| `solver-core`가 고객 구현을 참조 금지 | **컴파일 차단** — Maven 의존이 `solver-profile → solver-core` 한 방향 |
| 고객 전용 외부 라이브러리(3D packing 등)가 core로 유입 금지 | 〃 — 그 의존은 `solver-profile` pom에만 선언 |
| `verify`가 `solve` 내부(캐시·탐색 상태)를 참조 금지 | **ArchUnit 테스트** — `verify.. → solve..` 참조 시 빌드 실패 |
| 재검증이 **탐색 예산**(시간·step·idle 한도)을 읽는 것 금지 | 〃 — `AlnsConfig`가 `solve`에 있으므로 위 규칙이 그대로 막는다 (§2.3) |
| core에 고객명 분기 금지 | profile SPI (Domain §8.4) + 코드 리뷰 |

- `verify`는 `domain`·`problem`·`eval`의 공개 타입만 사용해 처음부터 재계산한다.
  `solve`의 결과 객체(최종 `Solution`)는 값으로 전달받는다 — 구현체 내부를 들여다보지 않는다.
- 나중에 컴파일 수준 차단이 정말 필요해지면 `verify` 패키지를 모듈로 승격한다 (지금은 하지 않음).
- solver-core의 테스트 의존성(JUnit, ArchUnit)은 test scope로만 허용.

### 2.2 profile 연결 (코드 레지스트리)

```java
// solver-core/eval — 계약과 기본 구현 (core 소유)
public interface HardConstraint {
    String id();
    boolean satisfied(Problem problem, RouteFacts route);
}

public interface Profile {
    String id();
    List<HardConstraint> hardConstraints();
    /** 이 profile의 목적식 축. 전부 "작을수록 좋다". 같은 profile은 항상 같은 길이 */
    long[] score(Problem problem, Evaluation metrics, Collection<RouteFacts> routes);
}

public class DefaultProfile implements Profile { /* 상속 대상 — final 아님 */ }

public final class Scores {
    /** 사전식 비교. < 0 이면 a가 더 좋다 */
    public static int compare(long[] a, long[] b);
}
```

```java
// solver-profile — 고객 정책 (모듈 분리)
public final class ProfileRegistry {
    private final Map<String, Profile> byCustomerId;  // 코드에서 구성
    private final Profile defaultProfile;
    public Profile resolve(Optional<String> customerId) {   // 미등록·부재 → default (MUST)
        ...
    }
}
```

- YAML/설정 파일 없음. 신규 고객 특화는 `Profile` 구현 클래스 추가 + 맵 한 줄 등록.
- **`Profile`은 `Problem`에 담기지 않는다** (2026-08-10 변경). 담으면 `eval → problem`이
  순환이 되어 profile이 문제 사실을 영영 못 읽는다. 대신 executor가 한 번 resolve해
  **탐색과 재검증에 같은 인스턴스를 인자로 넘긴다** (Domain §8.4 MUST를 이 방식으로 지킨다).
- 비교기 SPI는 없다. 비교는 `Scores.compare` 하나뿐이고, 무엇을 비교할지는 `score()`가 정한다.
  고정 가중치·Big-M으로 축을 한 숫자에 뭉개는 구현은 **만들 수 없다** (Domain §8.3이 구조로 강제).

### 2.3 계층이 나뉘는 기준

값이나 코드를 어디에 둘지 헷갈리면 이 두 질문으로 정한다.

| 질문 | 예 → 배치 | 아니오 → 배치 |
|---|---|---|
| 이 값을 바꾸면 **유효한 답의 집합**이 바뀌는가? | `domain`의 배송정책 → `Problem`에 동결, 재검증이 읽음 | `solve`의 `AlnsConfig` → `Problem` 밖, 재검증이 못 봄 |
| 이 개념이 **다른 고객에게도 의미**가 있는가? | canonical에 optional 필드 (Domain §2.1) | 그 고객 전용 데이터 경로 — 실제로 필요해질 때 결정 |

- 배송정책(창고 복귀·회전 수·`waitInDepot`·기본 속도)은 유효한 답을 바꾸므로 `Problem` 쪽이다.
- 탐색 예산(시간·step·idle 한도·seed)은 답이 **달라질 뿐 무효가 되지 않으므로** `Problem` 밖이다.
  "언제 멈출지"가 "무엇이 옳은 답인지"에 섞이면 재검증의 의미가 무너진다.
- 두 값을 하나의 `Rule` 타입으로 묶지 않는다 (묶는 순간 이 구분이 사라진다).

## 3. 실행 흐름과 상태

### 3.1 접수 (동기)

```text
POST /solves  (body = 규약 JSON)
  1. 규약 파싱 + 정규화 검증 (canonical 변환을 실행하고 결과는 버림)
                                     실패 → 4xx (S3에 아무것도 남기지 않음 — Domain §12)
  2. multiRotation이 {0,1} 밖 등 미지원 → 4xx UNSUPPORTED_INPUT
  3. solveKey 생성 (§3.3)
  4. S3 put: {solveKey}/input.json + status.json(state=RECEIVED)
  5. executor 큐에 등록 (in-process)
  6. 200 + { solveKey }
```

- Problem 동결·이동표 준비는 접수에서 하지 않는다 — 무거운 작업은 executor에서.
  접수 검증은 **정규화까지**다 (2026-08-11 확정): 소수 거리·빈 `zoneIds`·
  `close == open` 같은 의미 오류도 접수에서 4xx로 돌려주고 S3에 남기지 않는다 (Domain §12의
  분류 그대로 — 종전의 "형식 수준만" 문면은 Domain과 충돌해 폐기). 정규화 1회는 sub-second라
  동기 응답에 문제없고, executor는 저장본을 같은 코드로 다시 파싱·정규화한다 (최종 방어).

### 3.2 풀이 (비동기, in-process executor)

```text
SolveExecutor (고정 크기 스레드풀, 동시 실행 수 = 설정값, 기본 1~2)
  RECEIVED → RUNNING (status.json 갱신)
  input.json 로드 → adapter → canonical → Problem 동결
  → 초기해 → ALNS (시간 한도 = 입력 옵션 또는 설정)
  → 재검증 (Domain §10)
  → PASS: result.json 저장 → status DONE
  → FAIL/예외: status FAILED + 원인 (결과 저장 없음)
```

- 진행 중 status.json에 주기적 heartbeat(마지막 갱신 시각)를 기록한다.
- **재시작 한계 (정직한 제약):** 앱이 재시작하면 진행 중이던 풀이는 사라진다.
  RUNNING인데 heartbeat가 오래된 solve는 조회 시 `STALE`로 응답하고, 호출 측이 재접수한다.
  (자동 재개·큐 이관은 현재 범위 밖 — 필요해지면 SQS 도입을 그때 결정.)
- 수평 확장 한계: 태스크를 여러 개 띄우면 접수한 태스크가 그 solve를 끝까지 담당한다.
  현재 규모(내부 서비스, 낮은 동시성)에서는 태스크 1개 운영이 기본.

### 3.3 S3 배치

```text
버킷 하나 (환경별: ro-next-solves-{env})
solves/{customerId}/{planId}/{runId}/     ← 이 prefix가 solveKey
  input.json      접수한 규약 입력 (검증 통과본)
  status.json     { state: RECEIVED|RUNNING|DONE|FAILED, heartbeatAt, error? }
  result.json     결과 JSON (Domain §11) — DONE일 때만 존재
```

- `runId` = 접수 시각 기반 식별자. 같은 `planId` 재접수는 새 `runId`로 별도 기록 (덮어쓰기 없음).
- key 조립 규칙은 `storage` 한 곳에만 둔다 — 컨트롤러·executor에 문자열 하드코딩 금지.
- 정확한 버킷 이름·retention 정책은 배포 설정 (설계 고정 아님).

### 3.4 조회 API

```text
GET /solves/{solveKey}          → status.json 내용 (+ STALE 판정)
GET /solves/{solveKey}/result   → result.json (DONE 아니면 404/409)
```

와이어 경로·필드명은 호출 시스템과 협의해 확정한다 (Domain §11.2와 동일 원칙).

### 3.5 저장 인터페이스

```java
// app/storage — S3 SDK는 이 구현 뒤에만 존재
public interface SolveStore {
    void putInput(SolveKey key, byte[] body);
    void putStatus(SolveKey key, SolveStatus status);
    void putResult(SolveKey key, byte[] resultJson);
    Optional<SolveStatus> getStatus(SolveKey key);
    Optional<byte[]> getInput(SolveKey key);
    Optional<byte[]> getResult(SolveKey key);
}
```

- 구현 2개: `S3SolveStore`(운영), `LocalSolveStore`(인메모리 또는 로컬 디렉터리 — 테스트·로컬 실행).
- `solver-core`는 이 인터페이스도 모른다 (저장은 전적으로 app의 일).

## 4. Spring Boot 사용 규칙 — 최소한만 (확정)

라인: **Spring Boot 4.1.x** (Java 17–26, OSS 지원 2027-07-31까지). 선택 근거·patch 정책은
[Stage 0 §4.4](implementation/stage-00-cleanup-and-skeleton.md).

| 항목 | 규칙 |
|---|---|
| 스타터 | `spring-boot-starter-web` (+ actuator health 정도). **data·jpa·redis·cloud 스타터 금지** |
| JSON | **Jackson 3** 기본 (`tools.jackson`, 매퍼 `JsonMapper`). starter-json이 제공. **Jackson 2**(`ObjectMapper`·`com.fasterxml.jackson`·`spring-boot-jackson2`) **금지** |
| DI | `app` 모듈 조립에만 사용. `solver-core`·`solver-profile`은 Spring 어노테이션 없는 평범한 Java |
| executor | Spring `@Async` 또는 직접 만든 `ExecutorService` 중 단순한 쪽. 분산 스케줄러 금지 |
| AWS | `software.amazon.awssdk:s3` 하나만, `storage` 패키지 안에서만 |
| 설정 | `application.yml`: 버킷·동시 실행 수·탐색 예산 기본값(시간·step·idle 한도)·seed 등 운영 설정만. 점수·제약 로직 넣기 금지 |

## 5. 배포 (ECS Fargate)

```text
mvn package → app 모듈의 실행 가능 jar → Docker 이미지 1개
ECS Fargate 서비스 1개 · 태스크 1개(기본) · ALB 또는 내부 엔드포인트
권한: 해당 버킷 S3 read/write (태스크 롤)
설정: 환경변수/파라미터로 버킷 이름·프로파일 등 주입
로그: stdout → CloudWatch Logs
```

- CPU/메모리 사이징, 오토스케일, VPC 상세는 배포 시 결정 (설계 고정 아님).
- solve 시간 한도 < ALB idle timeout 문제 없음 — 접수는 즉시 200이고 풀이는 HTTP 밖이므로.

## 6. 로컬 개발·테스트

| 수준 | 환경 |
|---|---|
| 단위·통합 테스트 (기본) | `LocalSolveStore` fake — Docker 불필요, 빠름 |
| 로컬 앱 실행 | `local` Spring profile → `LocalSolveStore`(로컬 디렉터리) 로 전체 흐름 실행 |
| 배포 전 e2e (선택) | LocalStack S3 컨테이너에 `S3SolveStore`를 붙여 실제 SDK 경로 확인 |

- LocalStack은 **선택 도구**다. 일상 개발·CI는 fake로 충분하다 (2026-08-09 확정).

## 7. 현재 코드와의 차이 (마이그레이션 메모)

| 현재 (placeholder) | 목표 |
|---|---|
| 단일 pom, `com.ronext.optimizer`, 수제 HTTP 서버 | 3모듈, `com.ronext.rpdptw`, Spring Boot |
| pom에 GCP 의존성 (Cloud Storage·Workflows) | 제거 → `awssdk:s3`만 |
| `gcp/`, `.serverless/`, 빈 모듈 디렉터리 잔재 | 삭제 |
| `AlnsBatchEngine` (합성 데모) | solver-core의 실제 ALNS로 대체. 데모 코드는 완료 근거가 아님 |
| 루트 README의 Lambda/Step Functions 서술 | ECS Fargate 단일 서비스로 갱신 |

정리 순서·단계는 [Implementation Plan](implementation-plan.md)이 정한다.

## 8. 하지 말 것 (요약)

- 컨트롤러·executor에 S3 SDK 직접 호출 (→ `storage`만)
- `solver-core`에 Spring/Jackson/AWS import (→ 컴파일이 막지만, 의존성 추가로 뚫지 말 것)
- app에서 Jackson 2 API·`spring-boot-jackson2` 사용 (→ Boot 4.1 기본은 Jackson 3, §4)
- 고객 전용 라이브러리를 `solver-core` pom에 추가 (→ `solver-profile`에만)
- `verify`에서 `solve` 내부 참조 (→ ArchUnit이 막음)
- core/solve에 `if (customerId == …)` (→ profile)
- `Problem`에 `Profile`·탐색 예산 담기 (→ 둘 다 인자로 전달, §2.2·§2.3)
- 여러 축을 가중합·Big-M으로 한 숫자에 뭉개기 (→ `long[]` 사전식, Domain §8.3)
- 고객마다 별도 `Problem`·별도 adapter 만들기 (→ canonical에 optional 필드, Domain §2.1)
- 접수 HTTP 안에서 ALNS 완주 대기
- RDB·Redis·SQS·Step Functions 도입 (필요해지면 설계 변경으로 결정)
- 결과 JSON을 재검증 없이 저장
