# ro-next

CVRPTW / RPDPTW 최적화 서비스의 **Java 25 + AWS** 기반 구현 시작점입니다.  
설계 문서는 `docs/`에 있습니다.

## 설계 문서

- **진입점:** [`docs/README.md`](docs/README.md) (플랫폼 선언 포함)
- **정본 (APPROVED):** [`docs/master-design.md`](docs/master-design.md) · [`docs/domain-design.md`](docs/domain-design.md) · [`docs/architecture-design.md`](docs/architecture-design.md)
- 구 설계·질문 등록부·Phase B 핸드오프는 [`docs/deprecated/`](docs/deprecated/) (`SUPERSEDED` / `ARCHIVED`)
- 구현 phase 문서 세트: [`docs/implementation/README.md`](docs/implementation/README.md)

## 기술 기준 (target / reference)

| 축 | 기준 |
|---|---|
| JDK / 빌드 | AWS Corretto `25.0.3-amzn`, Maven `3.9.14` (`.sdkmanrc`) |
| 저장 | **Amazon S3 only** (로컬 통합: LocalStack S3). DB · Redis 없음 |
| durable orchestration | **AWS Step Functions** |
| worker / API compute | **AWS Lambda 또는 ECS** (제품 선택은 OPEN — 한쪽 단정 금지) |

JDK와 Maven은 전역 설정이 아니라 저장소의 `.sdkmanrc`로 고정했습니다.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
mvn verify
```

## current vs target (중요)

| 층 | 상태 |
|---|---|
| **Target platform** | AWS — S3 + Step Functions + (Lambda \| ECS) |
| **현재 tracked 코드** (`src/`, root `pom.xml`) | **GCP legacy placeholder** — Google Cloud Storage + Cloud Workflows + Cloud Run HTTP. 합성 `AlnsBatchEngine` objective |
| **tracked `gcp/`** | legacy 배포 가이드. **target 아님** |
| **구현 acceptance** | 문서 기준 0/15. placeholder ≠ 솔버 완료 (Master A9) |

Target API 형태(목표): `s3://` 입력 URI를 받고 Step Functions 실행을 시작한다.

```json
{
  "inputUri": "s3://my-optimization-inputs/instance-001.json",
  "parameters": {
    "parallelRuns": 8,
    "iterationsPerRun": 5000,
    "seed": 42
  }
}
```

`parameters` 예시 수치(`8`, `5000` 등)는 배포 흐름 설명용이며 official benchmark default가 아닙니다.  
실제 RPDPTW 입력 파싱, 초기해, destroy/repair, local search, 독립 verifier는 인프라 어댑터 밖의 순수 Java 계층에 추가합니다. 상세는 [`docs/implementation/master-realization-plan.md`](docs/implementation/master-realization-plan.md) §3 inventory를 보세요.

