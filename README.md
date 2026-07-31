# ro-next

CVRPTW 최적화 서비스의 Java 25/AWS 기반 구현 시작점입니다. 설계 문서는 `docs/`에, 실행 가능한 서버 및 워커 기본 구조는 `src/`에 있습니다.

## 설계 문서

- **진입점:** [`docs/README.md`](docs/README.md)
- **정본 (APPROVED):** [`docs/master-design.md`](docs/master-design.md) · [`docs/domain-design.md`](docs/domain-design.md) · [`docs/architecture-design.md`](docs/architecture-design.md)
- 구 설계·질문 등록부·Phase B 핸드오프는 [`docs/deprecated/`](docs/deprecated/) (`SUPERSEDED` / `ARCHIVED`)
- 구현 phase 문서 세트: [`docs/implementation/README.md`](docs/implementation/README.md)

## 기술 기준

- Java 25: AWS Corretto `25.0.3-amzn` 런타임 컨테이너
- Maven `3.9.14`
- AWS Lambda / ECS: 공개 API 및 내부 ALNS worker
- AWS Step Functions: ALNS batch 병렬화와 최적 후보 선택 오케스트레이션
- Amazon S3: 입력 URI, 후보, 비동기 최적화 결과

JDK와 Maven은 전역 설정이 아니라 저장소의 `.sdkmanrc`로 고정했습니다.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
mvn verify
```

## 배포

컴포넌트 런타임은 AWS Corretto 25 컨테이너를 사용합니다. AWS Step Functions 및 Lambda (또는 ECS) 환경에 맞춰 배포됩니다.

API는 `s3://` 입력 URI를 받고 Step Functions 실행을 시작합니다.

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

최적화 코어의 현재 `AlnsBatchEngine`은 배포 흐름 검증을 위한 대체 구현입니다. 실제 CVRPTW 입력 파싱, 초기해, destroy/repair, local search는 인프라 어댑터 밖의 순수 Java 계층에 추가합니다.

