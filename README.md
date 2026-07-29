# ro-next

RPDPTW 구현을 위한 Java 25 multi-module skeleton과 기존 GCP placeholder를 함께 보존하는 저장소입니다. Target namespace의 Phase 01 이후 기능은 아직 구현되지 않았고, 실행 가능한 기존 Cloud Run/Workflows 코드는 `legacy/gcp-placeholder`와 `gcp/`에 격리되어 있습니다.

## 기술 기준

- Java 25: AWS Corretto `25.0.3-amzn` 런타임 컨테이너
- Maven `3.9.14`
- Maven Wrapper `3.3.4`: Apache Maven `3.9.14` 배포본과 SHA-256 고정
- Cloud Run: legacy 공개 API와 내부 placeholder worker, 모두 scale-to-zero
- Google Cloud Workflows: ALNS batch 병렬화와 최적 후보 선택 오케스트레이션
- Cloud Storage: 입력 URI, 후보, 비동기 최적화 결과

JDK 선택 예시는 `.sdkmanrc`에 있고 Maven 실행은 checksum-pinned wrapper를 사용합니다.

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk env
./mvnw verify
```

Phase 00 build/architecture gate는 root `./mvnw verify`에서 stable module DAG, provider/vendor/customer/internal/test-fixture 경계와 legacy characterization을 함께 검사합니다. Legacy shaded JAR만 만들려면 다음 명령을 사용합니다.

```bash
./mvnw -pl legacy/gcp-placeholder -am package
```

## 배포

Cloud Run 런타임은 AWS Corretto 25 컨테이너를 사용한다. 전체 배포 순서와 IAM 권한은 [GCP 배포 가이드](gcp/README.md)를 따른다.

Legacy API는 `gs://` 입력 URI만 받고 Workflows 실행을 시작한다. 이 endpoint와 payload는 target public API로 승인된 것이 아니다.

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

Legacy `AlnsBatchEngine`은 배포 흐름 검증을 위한 합성 objective 대체 구현입니다. Target domain/input/solver/verifier 기능의 완료 evidence가 아닙니다. 구현 경계와 순서는 [Phase 00 설계](docs/implementation/phases/phase-00-build-architecture-skeleton.md)를 따릅니다.
