---
title: Stage 7 — ECS 배포 (상세 구현 설계)
stage: 7
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../architecture-design.md (§5 배포, §3.3 S3 배치, §3.5 SolveStore, §6 로컬·LocalStack, §1 한 장 그림, §4 Boot 4.1)
  - ../implementation-plan.md (Stage 7, §0 최종 성공 기준, Stage 0 코드 정리 — 구 Dockerfile 삭제)
  - stage-00-cleanup-and-skeleton.md (§2 C4 구 Dockerfile 삭제, §3.1 이름 기준, §4 POM·enforcer)
  - stage-06-app-assembly.md (§1.1 설정 키, §2.1 S3SolveStore, §3.3 key 조립, §10 Q1)
revisions:
  - 2026-08-10 최초 작성
  - 2026-08-10 Dockerfile 스케치에 solver-profile 모듈 반영 (3모듈 reactor)
---

# Stage 7 — ECS 배포

Stage 6까지 조립된 앱(`ro-next-app`)을 Docker 이미지 하나로 만들어 AWS ECS Fargate에
올린다. 주 근거: [Architecture §5](../architecture-design.md). Stage 0 §2 C4가 구 Dockerfile
(단일 모듈 shade jar 전용)을 삭제했으므로, 본 Stage가 **app 모듈 실행 가능 jar 기준으로
재작성**한다. app·solver-core 코드와 Stage 6 설정 키는 **일절 변경하지 않는다** — 이 Stage의
산출물은 Dockerfile과 배포 정의(태스크 정의·IAM·서비스)뿐이다.

**DoD** ([Plan Stage 7](../implementation-plan.md)): 배포 환경에서 실제 S3로 §0 시나리오
(win_poc_case_floor.json 접수 → 재검증 통과 → 결과 JSON) 1회 성공.
단, §0 fixture는 multiRotation 충돌(Stage 6 §10 Q1) 해소가 선행돼야 접수된다 (§9 Q1).

핵심 구도 — Architecture §5의 한 줄(`mvn package → app jar → 이미지 1개 → Fargate 서비스 1개`)을
절차로 편다:

```text
[mvn verify green (전 Stage DoD)]
      │
[docker build]  ─이미지 1개 (app jar)─▶  [ECR push]
                                            │
                       [태스크 정의: 이미지 + 환경변수(§3.4) + 태스크 롤(§3.2) + awslogs]
                                            │
                       [ECS Fargate 서비스 1개 · 태스크 1개 (기본 — Architecture §5)]
[호출 시스템] ──POST /solves·GET──▶  (ALB 또는 내부 엔드포인트 — §9 Q2)
                                            │ stdout                │ 태스크 롤
                                     [CloudWatch Logs]    [S3: ro-next-solves-{env}]
```

---

## 1. 산출물과 배포 시 결정 사항의 경계

Architecture §5가 "배포 시 결정 (설계 고정 아님)"으로 열어 둔 것은 본 문서도 확정하지 않는다.
스케치의 해당 자리는 전부 `<배포 시 결정>`으로 표기한다.

| 구분 | 항목 |
|---|---|
| **이 Stage가 만드는 것** | 루트 `Dockerfile` 재작성 (§2) · 태스크 정의/IAM 정책/서비스 구성의 스케치와 절차 (§3–§4). 스케치를 JSON 파일로 저장소에 보관할지는 구현 재량 (IaC 도입은 하지 않는다 — §8) |
| **배포 시 결정 (Architecture §5·§3.3 명시)** | CPU/메모리 사이징 · 오토스케일(현재 없음) · VPC/서브넷/보안그룹 상세 · 버킷 정확한 이름(`ro-next-solves-{env}`의 `{env}`)·retention · 계정·리전 |
| **변경하지 않는 것** | app·solver-core 코드, `application.yml` 키(Stage 6 §1.1), `.sdkmanrc`, `.dockerignore` |

---

## 2. Dockerfile 설계

### 2.1 결정과 근거

| # | 결정 | 근거 |
|---|---|---|
| D1 | 루트 `Dockerfile` 1개, 이미지 1개 (app jar만 실행) | Architecture §5 "app 모듈의 실행 가능 jar → Docker 이미지 1개". Stage 0 C4의 재작성 인계 |
| D2 | 멀티스테이지: build(maven) → runtime(corretto). §5의 `mvn package` 절차를 컨테이너 안에서 수행 | 호스트 JDK·`target/` 잔재와 무관한 재현 빌드. 런타임 이미지에 Maven·소스 불포함 |
| D3 | build 베이스 `maven:3.9.14-eclipse-temurin-25` | parent pom enforcer(Java `[25,26)`·Maven `≥3.9.14`, Stage 0 §4.1)를 충족하는 조합. 구 Dockerfile과 동일 계열 |
| D4 | runtime 베이스 `amazoncorretto:25-al2023` | `.sdkmanrc`(java `25.0.3-amzn`)와 같은 Corretto 25 계열 — 로컬·CI·운영 JDK 통일 |
| D5 | 이미지 빌드는 `-DskipTests` | 품질 게이트는 배포 절차 1의 루트 `mvn verify`(전 Stage DoD) — 이중 실행 회피. 이미지 빌드는 패키징만 |
| D6 | `java -XX:MaxRAMPercentage=75 -jar` | 힙을 태스크 메모리에 비례시켜 사이징 미확정(§1)과 정합. 구 Dockerfile 값 승계 |
| D7 | 포트 8080, health는 `GET /actuator/health` | Spring 기본 포트(Stage 0 T3와 동일)·actuator 기본 노출 (Stage 0 §5 — 별도 컨트롤러 없음) |

### 2.2 내용 스케치

```dockerfile
# ---- build: mvn package → app 모듈 실행 가능 jar (Architecture §5, 3모듈) ----
FROM maven:3.9.14-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY solver-core/pom.xml solver-core/pom.xml
COPY solver-profile/pom.xml solver-profile/pom.xml
COPY app/pom.xml app/pom.xml
COPY solver-core/src solver-core/src
COPY solver-profile/src solver-profile/src
COPY app/src app/src
RUN mvn -q -B -DskipTests package

# ---- runtime: jar 하나만 (이미지 1개 — Architecture §5) ----
FROM amazoncorretto:25-al2023
WORKDIR /app
COPY --from=build /workspace/app/target/ro-next-app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
```

- parent `<modules>`가 `solver-core` · `solver-profile` · `app` 이므로 **세 모듈 소스를 모두**
  COPY한다 (하나라도 빠지면 reactor 실패). Stage 0 C4가 지운 구 Dockerfile(단일 `src/`)과 혼동하지 않는다.
- jar 이름은 Stage 0 §3.1·§4.4의 좌표(artifactId `ro-next-app` + spring-boot-maven-plugin
  repackage)에서 나온다. `*.jar` 와일드카드는 버전 승계용이며 `.jar.original`(repackage 부산물)과
  충돌하지 않는다.
- 의존성 캐시 레이어(`dependency:go-offline` 선실행)는 빌드 속도 최적화일 뿐이라 구현 재량 —
  멀티모듈 reactor에서의 동작을 확인한 뒤에만 넣는다.
- 컨테이너 안에 curl 등 도구를 추가하지 않는다 — health 확인은 ALB/외부에서 한다 (§6 E8).

---

## 3. ECS Fargate 구성

### 3.1 태스크 정의 스케치

```jsonc
{
  "family": "ro-next",
  "requiresCompatibilities": ["FARGATE"],
  "networkMode": "awsvpc",                      // Fargate 필수
  "cpu": "<배포 시 결정>",                       // Architecture §5 — 사이징 미고정
  "memory": "<배포 시 결정>",                    //   (참고: ALNS는 CPU 바운드, 동시 실행 기본 1)
  "runtimePlatform": { "operatingSystemFamily": "LINUX",
                       "cpuArchitecture": "<이미지 빌드 플랫폼과 일치 — §6 E1>" },
  "taskRoleArn": "<ro-next 태스크 롤 — §3.2>",
  "executionRoleArn": "<ECS 실행 롤 — §3.3>",
  "containerDefinitions": [{
    "name": "app",
    "image": "<ECR URI>:<tag>",
    "portMappings": [{ "containerPort": 8080 }],
    "environment": [ /* §3.4 표 */ ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": { "awslogs-group": "/ecs/ro-next",
                   "awslogs-region": "<배포 시 결정>",
                   "awslogs-stream-prefix": "app" }        // §3.5
    }
  }]
}
```

### 3.2 태스크 롤 — S3 최소 권한 (Architecture §5 "해당 버킷 S3 read/write")

앱의 S3 사용은 `SolveStore` 6개 메서드가 전부다 (Architecture §3.5, Stage 6 §2.1):
put/get **객체**뿐이며 delete·범위 list를 하지 않는다. key는 전부 `solves/` prefix 아래다
(Architecture §3.3, Stage 6 §3.3). 최소 권한은 다음과 같다:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    { "Sid": "SolveObjects",
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:PutObject"],
      "Resource": "arn:aws:s3:::<버킷>/solves/*" },
    { "Sid": "MissingKeyIs404",
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::<버킷>",
      "Condition": { "StringLike": { "s3:prefix": "solves/*" } } }
  ]
}
```

- **`ListBucket`이 최소 권한에 포함되는 이유**: S3는 `ListBucket` 권한이 없으면 미존재 key의
  GetObject에 404(NoSuchKey) 대신 403을 준다. 그러면 `S3SolveStore.getStatus`의 "부재 →
  `Optional.empty`" 판정이 깨져 조회 404(Architecture §3.4)가 500으로 오동작한다.
  prefix 조건으로 실제 나열 범위는 `solves/*`로 좁힌다.
- 자격 증명은 태스크 롤 하나뿐이다 — `S3Client`는 기본 자격 증명 체인(Fargate 컨테이너
  자격 증명 endpoint)을 그대로 쓰고, 정적 액세스 키를 환경변수로 주입하지 않는다 (MUST NOT).

### 3.3 실행 롤 (execution role)

Architecture §5에는 태스크 롤만 언급되지만, ECS가 이미지 pull(ECR)과 로그 전송(CloudWatch)에
쓰는 **실행 롤**은 Fargate 운영의 필수 메커니즘이다. 관리형 정책
`AmazonECSTaskExecutionRolePolicy` 하나면 충분하다 — 앱 권한(S3)과 섞지 않는다.

### 3.4 환경변수 → Stage 6 설정 키 매핑

이름은 Spring Boot relaxed binding 규칙(대문자, `.` → `_`, `-` 제거)으로 기계적으로 나온다.
값이 필요 없는 키는 **설정하지 않고** `application.yml` 기본값(Stage 6 §1.1)을 쓴다.

| 환경변수 | Stage 6 설정 키 | ECS 값 |
|---|---|---|
| `RONEXT_STORAGE_BUCKET` | `ro-next.storage.bucket` | **필수** — `ro-next-solves-{env}` (이름은 배포 설정, Architecture §3.3) |
| `RONEXT_SOLVE_CONCURRENCY` | `ro-next.solve.concurrency` | 미설정 (기본 1 — Architecture §3.2 "태스크 1개·낮은 동시성") |
| `RONEXT_SOLVE_HEARTBEATINTERVALSEC` | `ro-next.solve.heartbeat-interval-sec` | 미설정 (기본 15) |
| `RONEXT_SOLVE_STALEAFTERSEC` | `ro-next.solve.stale-after-sec` | 미설정 (기본 60) |
| `RONEXT_SOLVE_FALLBACKTIMELIMITSEC` | `ro-next.solve.fallback-time-limit-sec` | 미설정 (기본 60) |
| `RONEXT_SOLVE_SEED` | `ro-next.solve.seed` | 미설정 (기본 0 — 정책 변경은 Stage 8) |
| `SPRING_PROFILES_ACTIVE` | (Spring profile) | **설정하지 않는다** — 미설정(≠`local`) = `S3SolveStore` (Architecture §6, Stage 6 §1.1). `local` 설정은 사고다 (§6 E3) |
| `AWS_REGION` | (AWS SDK 리전) | 버킷 리전 명시 (자동 주입에 의존하지 않는다) |
| — (`RONEXT_STORAGE_LOCALDIR`) | `ro-next.storage.local-dir` | ECS에서 사용 없음 (local profile 전용) |

### 3.5 CloudWatch Logs

- Architecture §5: "로그: stdout → CloudWatch Logs" — awslogs 드라이버가 문자 그대로의 이행이다.
  파일 로깅·별도 에이전트를 만들지 않는다.
- 로그 그룹 `/ecs/ro-next`는 사전 생성한다 (retention은 배포 설정 — §1).
- 앱은 Spring Boot 기본 콘솔 로깅 그대로다 — 로깅 설정 변경은 이 Stage 범위 밖.

### 3.6 서비스

```text
클러스터 1개 (예: ro-next) · 서비스 1개 · desiredCount = 1   (Architecture §5 "태스크 1개(기본)")
launchType FARGATE · awsvpc 네트워크 (서브넷·보안그룹 = 배포 시 결정)
엔드포인트: ALB(대상 그룹 health check = /actuator/health) 또는 내부 엔드포인트 — §9 Q2
오토스케일 정책 없음 (Architecture §3.2 — 태스크 1개 운영이 기본. §8)
```

- solve 시간 한도(floor fixture 600초) < ALB idle timeout 문제는 없다 — 접수는 즉시 200이고
  풀이는 HTTP 밖이다 (Architecture §5 명시).

---

## 4. 배포 절차

**사전 준비 (환경당 1회)**: ① S3 버킷 `ro-next-solves-{env}` ② 태스크 롤·실행 롤 (§3.2·§3.3)
③ 로그 그룹 `/ecs/ro-next` ④ ECR 리포지터리 `ro-next-app` ⑤ ECS 클러스터.

이후 배포는 번호 순서대로 (재배포 = 2~6 반복: 새 tag → 새 revision → 서비스 갱신):

```text
1. 게이트    루트 mvn verify green (전 Stage DoD — 이미지 빌드는 테스트를 건너뛰므로 유일한 게이트).
2. 이미지    docker build --platform <태스크 정의와 일치> -t ro-next-app:<tag> .
            로컬 smoke: docker run -e SPRING_PROFILES_ACTIVE=local -e RONEXT_STORAGE_LOCALDIR=/data
            -p 8080:8080 → GET /actuator/health = UP (§7 V1).
3. ECR push  aws ecr get-login-password | docker login … → docker tag/push <ECR URI>:<tag>.
4. 태스크 정의  §3.1 스케치에 이미지 URI·환경변수를 채워 register-task-definition (새 revision).
5. 서비스    최초: create-service (desiredCount 1, §3.6). 이후: update-service --task-definition <새 revision>.
6. 확인      태스크 RUNNING · /actuator/health UP · CloudWatch 로그 스트림에 기동 로그 (§7 V3·V4).
7. S3 스모크  소형 규약 JSON(multiRotation 0) POST → DONE 폴링 → GET result →
            S3에 input/status/result 3객체 실물 확인 (§7 V5 — 태스크 롤·리전·버킷 결선 검증).
8. DoD      win_poc_case_floor.json으로 §0 시나리오 1회 (§7 V6 — §9 Q1 해소 선행).
```

- 순서 7을 8보다 먼저 두는 이유: 인프라 결선(권한·리전·버킷) 오류를 fixture 충돌(Q1)과
  분리해 확인할 수 있다 — 7은 Q1과 무관하게 언제든 실행 가능하다.

---

## 5. LocalStack e2e (선택 — Architecture §6 유지)

| 항목 | 내용 |
|---|---|
| 위치 | 배포 절차 1과 2 사이의 **수동 1회** (Plan Stage 7 "선택: 배포 전 LocalStack e2e 1회"). 커밋되는 테스트·CI 단계로 만들지 않는다 — 일상 개발·CI는 fake로 충분 (Architecture §6 확정) |
| 범위 | `S3SolveStore`의 **실제 SDK 경로**(put/get, key 조립, 직렬화)만. 소형 입력으로 POST → DONE → GET result 1회 |
| 범위 밖 | IAM 태스크 롤·리전·네트워크 — LocalStack은 권한을 검사하지 않으므로 이것들은 실배포(§4-7)에서만 검증된다. DoD가 "배포 환경에서 실제 S3"인 이유다 |
| 방법 (잠정) | LocalStack S3 컨테이너 기동 + 버킷 생성 후, 앱을 local profile **없이** 실행하며 `AWS_ENDPOINT_URL_S3=http://s3.localhost.localstack.cloud:4566` + 더미 자격 증명 + `RONEXT_STORAGE_BUCKET` 주입. 코드 무변경 — SDK의 환경변수 endpoint 설정(AWS SDK Java 2.21+)을 쓴다. 구현 시점 SDK 버전이 이를 지원하지 않으면 endpoint override 설정 키 추가는 구현 재량 (Stage 6 키 체계 `ro-next.storage.*` 아래) |

---

## 6. Edge case / 운영 주의사항 표

| # | 상황 | 처리·주의 | 근거 |
|---|---|---|---|
| E1 | Apple Silicon에서 빌드한 이미지를 X86_64 태스크에 배포 | 기동 실패 (exec format error). `docker build --platform`과 태스크 정의 `cpuArchitecture`를 일치시킨다 | §3.1·§4-2 |
| E2 | **배포·재시작 시 진행 중 solve 소실** | RUNNING 고착 → heartbeat 노화 → 조회가 STALE 응답 → 호출 측 재접수. 자동 재개 없음 — 설계의 정직한 제약. 배포는 진행 중 solve가 없는 시점에 한다 (ECS stop의 SIGTERM 유예(기본 30초)로는 600초 solve를 못 살린다) | Architecture §3.2, Stage 6 E11 |
| E3 | ECS에 `SPRING_PROFILES_ACTIVE=local`이 설정됨 | `LocalSolveStore` — 결과가 컨테이너 디스크에 쓰여 태스크 종료와 함께 소실. §3.4대로 profile 미설정이 정답 | Architecture §6 |
| E4 | RECEIVED 큐 대기 중 재시작 | RECEIVED 고착 — STALE 판정은 RUNNING만 대상이라 걸리지 않음. 호출 측 자체 타임아웃으로 재접수 (Stage 6 E12의 운영 발현) | Architecture §3.2 문면 |
| E5 | health UP인데 S3 권한·버킷 오류 | actuator 기본 health는 S3를 검사하지 않는다 — UP이어도 접수(putInput)가 500일 수 있다. §4-7 S3 스모크가 실질 검증이다 | Stage 0 §5·§3.2 |
| E6 | 버킷 이름 오설정·미설정 | 첫 접수에서 5xx (기동은 성공) — E5와 같은 방법으로 발견. 기동 시 검증 추가는 하지 않는다 (Stage 6 조립 무변경) | §1 |
| E7 | desiredCount > 1 또는 롤링 배포 중 태스크 2개 공존 | 접수한 태스크가 그 solve를 끝까지 담당 (상태·결과는 S3라 조회는 어느 태스크든 무해). 곧 내려갈 태스크가 접수한 solve는 E2로 소실 → 태스크 1개 운영이 기본 | Architecture §3.2 수평 확장 한계 |
| E8 | 컨테이너 수준 healthCheck 정의 | 하지 않는다 — runtime 이미지에 curl류가 없고, 확인은 ALB 대상 그룹(또는 외부 호출)의 `/actuator/health`로 충분 | §2.2·§3.6 |
| E9 | status.json이 heartbeat마다 덮어써짐 | 정상 동작. 버킷 버저닝을 켜면 heartbeat 횟수만큼 버전이 쌓인다 — 켜지 않는다 (retention·수명 주기는 배포 설정) | Architecture §3.3, Stage 6 §3.2 |
| E10 | 같은 planId 재접수 반복으로 객체 누적 | 설계상 정상 (새 runId, 덮어쓰기 없음). 정리는 버킷 lifecycle(배포 설정)의 몫 — 앱은 delete 권한 자체가 없다 (§3.2) | Architecture §3.3 |
| E11 | 미존재 key GetObject가 403으로 응답 | 태스크 롤에 `ListBucket`(prefix 조건) 누락 신호 — §3.2 정책이 원천 차단 | §3.2 |
| E12 | 빌드 이미지 tag 갱신 (Maven·JDK 버전 변경) | enforcer(Java `[25,26)`·Maven `≥3.9.14`)가 이미지 안에서 빌드를 실패시킨다 — tag은 `.sdkmanrc`와 함께 움직인다 | Stage 0 §4.1 |
| E13 | `.dockerignore`의 `target` 항목은 루트만 매칭 (Docker 문법) | 멀티스테이지 빌드라 호스트 `app/target` 잔재가 이미지에 들어갈 경로가 없음 — 무해. jar를 호스트에서 COPY하는 방식으로 바꾸려면 그때 `.dockerignore` 정비가 선행돼야 한다 | §2.1 D2 |
| E14 | 빌드 컨텍스트에 `data/`(fixture ~25MB)·`docs/` 포함 | 동작 무영향 (COPY 대상 아님). 컨텍스트 축소용 `.dockerignore` 보강은 구현 재량 | — |

---

## 7. 테스트/확인 목록 — DoD 1:1 대응

자동 테스트가 아니라 **배포 확인 절차**다 (이 Stage는 코드가 없다). V6이 DoD 문장 그대로이고,
V1~V5는 그 전제의 단계적 확인이다.

| # | 확인 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| V1 | 이미지 빌드·로컬 smoke | `docker build` 성공 + local profile 컨테이너에서 `/actuator/health` UP (§4-2) | "배포 환경에서 … 성공"의 전제 (이미지 기동) |
| V2 | (선택) LocalStack e2e | §5 — `S3SolveStore` 실 SDK 경로 1회 | Plan "선택: 배포 전 LocalStack e2e 1회" |
| V3 | 서비스 기동 | 태스크 RUNNING 유지(재시작 루프 없음) + health UP (§4-6) | 〃 전제 |
| V4 | CloudWatch 로그 | `/ecs/ro-next` 스트림에 앱 기동·접수 로그 확인 | Plan 범위 문장 "CloudWatch 로그" |
| V5 | 실제 S3 스모크 | 소형 규약 JSON(multiRotation 0) POST → DONE → GET result → S3 3객체(input/status/result) 실물 확인 (§4-7) | "실제 S3로 … 성공"의 인프라 결선 부분 (Q1과 무관하게 실행 가능) |
| V6 | **§0 시나리오** | win_poc_case_floor.json POST → 폴링(한도 600초 — 최대 ~11분, Stage 6 T13과 동일) → DONE → GET result: `verified: true`·metrics·routes 확인 + S3 result.json 실물 | **"배포 환경에서 실제 S3로 §0 시나리오 1회 성공"** (§9 Q1 해소 선행) |

---

## 8. 이 Stage에서 하지 않는 것

| 안 하는 것 | 근거·담당 |
|---|---|
| CPU/메모리 사이징·오토스케일·VPC 상세·버킷 이름·retention의 확정 | Architecture §5·§3.3 "배포 시 결정" — §1 표 |
| 오토스케일 정책·blue/green·multi-region·다중 태스크 운영 설계 | 요구 없음 (Architecture §3.2 — 태스크 1개 기본) |
| IaC(Terraform/CDK/CloudFormation) 도입, CI/CD 파이프라인 구축 | Plan 범위 밖 — 절차는 §4의 수동 CLI 기준. 필요해지면 별도 결정 |
| HTTPS·인증·rate limit·요청 크기 상한 | 내부 서비스 — Stage 6 §9와 동일하게 범위 밖 |
| app·solver-core 코드/설정 키 변경 (S3 endpoint 키 포함 — §5는 env 잠정안) | Stage 6 산출물 그대로 소비 |
| SQS 재개·자동 복구·진행 중 solve의 배포 생존 | Architecture §3.2 정직한 제약 유지 (필요 시 설계 변경으로) |
| 컨테이너 healthCheck·로깅 설정 변경·APM 도입 | §6 E8, §3.5 — 요구 없음 |
| Win 지표 비교·탐색 파라미터 조정 | Stage 8 |
| LocalStack의 상시 테스트·CI 편입 | Architecture §6 — 선택 도구 유지 (§5) |

---

## 9. 미해결 질문

확정 문서로 답이 안 나오는 것만 남긴다. Stage 7 진행은 각 항목의 "잠정 처리"로 한다.

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | **multiRotation fixture 충돌** (Stage 0 §11 Q2 → Stage 6 §10 Q1 인계, 미해소): 두 fixture 모두 `multiRotation: "1"`이라 §0 fixture가 접수(422)에서 거부된다 — **V6(DoD)이 이 결정에 선행 의존**한다. fixture 교정(A)이든 규약 협의·Domain 개정(B)이든 Stage 6 Q1의 두 경로 중 하나가 먼저 닫혀야 한다 | 배포·인프라 검증(V1~V5)은 Q1과 무관하게 전부 진행 가능하도록 절차를 분리했다 (§4-7). V6만 결정 대기 |
| Q2 | **엔드포인트 노출 방식**: Architecture §5는 "ALB 또는 내부 엔드포인트"로 열어 두었고, 호출 시스템의 네트워크 위치(같은 VPC인지, 온프레미스인지)가 미정이라 확정할 수 없다 | DoD 확인은 최소 구성(예: 보안그룹을 확인자 위치로 좁힌 접근)으로 수행. 상시 노출 방식은 호출 시스템과 협의 후 배포 설정으로 확정 (Stage 6 §10 Q4의 wire 협의와 같은 상대) |
| Q3 | **환경 구분·계정·리전**: `ro-next-solves-{env}`의 `{env}` 명명, 배포 계정·리전이 어느 문서에도 없다 | 배포 설정 (Architecture §3.3 문면 그대로). DoD는 단일 환경(예: dev) 1개로 수행하면 충분하다 |
