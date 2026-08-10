# ro-next

배차 최적화(RPDPTW) 서비스. 규약 JSON을 접수해 ALNS로 배차안을 만들고, 독립 재검증을 통과한
결과만 S3에 저장하는 **단일 Spring Boot 서비스** (AWS ECS Fargate)다.

## 설계 문서

- **진입점:** [`docs/README.md`](docs/README.md)
- 현행 4문서: [Master](docs/master-design.md) · [Domain](docs/domain-design.md) ·
  [Architecture](docs/architecture-design.md) · [Implementation Plan](docs/implementation-plan.md)
  (2026-08-09 확정 · 2026-08-10 3계층·Boot 4.1 개정)
- 구현 Stage 상세: [docs/implementation/README.md](docs/implementation/README.md)
- 과거 설계는 전부 [`docs/deprecated/`](docs/deprecated/) — 효력 없음

## 기술 기준 (확정 설계)

- Java 25 (`.sdkmanrc`로 고정) · Maven 3.9.14+
- 모듈 3개 (3계층): `solver-core`(순수 Java, compile 의존 0) + `solver-profile`(고객 정책) +
  `app`(**Spring Boot 4.1.0**, ECS 배포 단위) · JSON은 **Jackson 3** (`JsonMapper`)
- 저장: **Amazon S3만** (RDB·Redis 없음)
- 배포: **AWS ECS Fargate** 단일 서비스 (Lambda·Step Functions 사용하지 않음)

## 모듈 구조

| 모듈 | 책임 |
|---|---|
| `solver-core` | RPDPTW 도메인·탐색·재검증 (외부 라이브러리 compile 의존 0) |
| `solver-profile` | 고객별 `Profile` 구현 · `ProfileRegistry` |
| `app` | Spring Boot 접수/조회 API · executor · 저장 adapter |

진행 상태: [docs/implementation/README.md](docs/implementation/README.md)

## 빌드 · 기동

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
mvn verify
mvn spring-boot:run -pl app
# 다른 터미널
curl -s localhost:8080/actuator/health
```
