# ro-next

CVRPTW 최적화 서비스의 Java 25/GCP 기반 구현 시작점입니다. 설계 문서는 `docs/`에, 실행 가능한 Cloud Run 및 Workflows 기본 구조는 `src/`와 `gcp/`에 있습니다.

## 기술 기준

- Java 25: AWS Corretto `25.0.3-amzn` 런타임 컨테이너
- Maven `3.9.14`
- Cloud Run: 공개 API와 내부 ALNS worker, 모두 scale-to-zero
- Google Cloud Workflows: ALNS batch 병렬화와 최적 후보 선택 오케스트레이션
- Cloud Storage: 입력 URI, 후보, 비동기 최적화 결과

JDK와 Maven은 전역 설정이 아니라 저장소의 `.sdkmanrc`로 고정했습니다.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
mvn verify
```

## 배포

Cloud Run 런타임은 AWS Corretto 25 컨테이너를 사용한다. 전체 배포 순서와 IAM 권한은 [GCP 배포 가이드](gcp/README.md)를 따른다.

API는 `gs://` 입력 URI만 받고 Workflows 실행을 시작한다.

```json
{
  "inputUri": "gs://my-optimization-inputs/instance-001.json",
  "parameters": {
    "parallelRuns": 8,
    "iterationsPerRun": 5000,
    "seed": 42
  }
}
```

최적화 코어의 현재 `AlnsBatchEngine`은 배포 흐름 검증을 위한 대체 구현입니다. 실제 CVRPTW 입력 파싱, 초기해, destroy/repair, local search는 이 GCP 어댑터 밖의 순수 Java 계층에 추가합니다. 상세 구조는 [GCP 아키텍처](docs/08_gcp_architecture.md)에서 확인할 수 있습니다.
