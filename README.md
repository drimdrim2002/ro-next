# ro-next

배차 최적화(RPDPTW) 서비스. 규약 JSON을 접수해 ALNS로 배차안을 만들고, 독립 재검증을 통과한
결과만 S3에 저장하는 **단일 Spring Boot 서비스** (AWS ECS Fargate)다.

## 설계 문서

- **진입점:** [`docs/README.md`](docs/README.md)
- 현행 4문서: [Master](docs/master-design.md) · [Domain](docs/domain-design.md) ·
  [Architecture](docs/architecture-design.md) · [Implementation Plan](docs/implementation-plan.md)
  (2026-08-09 확정 · 2026-08-10 3계층 개정)
- 과거 설계는 전부 [`docs/deprecated/`](docs/deprecated/) — 효력 없음

## 기술 기준 (확정 설계)

- Java 25 (`.sdkmanrc`로 고정) · Maven 3.9+
- 모듈 3개 (3계층): `solver-core`(순수 Java, 의존성 0) + `solver-profile`(고객별 정책) +
  `app`(**Spring Boot 4.1**, ECS 배포 단위) · JSON은 **Jackson 3** (`JsonMapper`)
- 저장: **Amazon S3만** (RDB·Redis 없음)
- 배포: **AWS ECS Fargate** 단일 서비스 (Lambda·Step Functions 사용하지 않음)

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
mvn verify
```

## 현재 코드 상태 (주의)

현 `src/`의 `AlnsBatchEngine` 등은 배포 흐름 확인용 placeholder이며 pom에는 과거 GCP 실험
의존성이 남아 있다. 확정 설계와 다르며, [Implementation Plan Stage 0](docs/implementation-plan.md)에서
정리·재구성한다. placeholder 동작은 솔버 완성의 근거가 아니다.
