# Master Design 분리 검토 세션 인덱스

이 디렉터리는 `master-design.md`의 분리 검토와 후속 통합·review 기록이다. 세션 01~18은 최초 주제별 review input이고, 이후 세션은 통합·검토·사용자 결정·반영 기록이다.

## 작업 원칙

- 각 번호는 별도 검토 세션에서 작성했다.
- 이 단계는 앞으로의 구현을 명확히 하기 위한 **문서 작업**이다.
- 최초 세션 01~18 작성 단계에는 코드와 당시 `master-design.md`, `domain-design.md`를 수정하지 않았다.
- 확정되지 않은 내용은 각 문서의 `남은 질문`에 기록했다.
- 각 문서의 제안은 질문에 답한 뒤 통합 Master Design에 반영한다. 질문이 남은 제안을 곧바로 확정 설계로 간주하지 않는다.

## 18개 세션

| 번호 | 문서 | 담당 주제 |
|---:|---|---|
| 01 | [GCP 운영 아키텍처 전환](01-gcp-architecture.md) | 과거 AWS/ECS 가정을 현재 Cloud Run, Workflows, Cloud Storage 구조로 교체 |
| 02 | [RPDPTW 용어 표준화](02-rpdptw-terminology.md) | `RPDPTW`, `rpdptw`, `Rpdptw` 명명 규칙 |
| 03 | [CVRPTW의 RPDPTW 통합 모델 재검토](03-rpdptw-model-recheck.md) | PDPTW 논문과 CVRPTW 정의를 근거로 delivery-only와 실제 pickup-delivery 구분 |
| 04 | [고객사별 목적함수 정책](04-objective-policy-flexibility.md) | `ScorePolicy`, `Objective`, `SolvePlan`을 통한 고객별 목표 확장 |
| 05 | [차량 크기 유형과 확장 제약](05-vehicle-size-and-constraints.md) | 1톤·3톤·5톤 차량 크기와 별도 장비·업무 제약 분리 |
| 06 | [요청 쌍 불변조건](06-request-pair-invariants.md) | same vehicle, exactly once, precedence, 원자적 destroy/repair 계약 |
| 07 | [종료 조건과 재현성](07-termination-and-reproducibility.md) | step 정상 종료와 watchdog 시간 제한 분리 |
| 08 | [후보 해 복사와 롤백](08-candidate-copy-and-rollback.md) | 초기 copy-on-write와 후속 apply/undo 전략 |
| 09 | [고정소수점·단위 변환](09-fixed-point-policy.md) | 합의된 소수 자릿수 처리, scale, rounding, 입력 검증 |
| 10 | [도메인 한계값과 Constants](10-domain-limits-and-constants.md) | 999 CBM, plan end time, sentinel 제거와 한계값 관리 |
| 11 | [입력 스키마와 시간 계약](11-input-schema-and-time-contract.md) | `ro_input_json_spec.pdf` 전체 검토 및 시간·Plan Option 매핑 |
| 12 | [입력 거리·시간 행렬 계약](12-distance-time-input.md) | PDF와 `win_poc_case.json`의 위치·arc·거리·시간 구조 검토 |
| 13 | [미배정 상태와 진단](13-unassigned-status-and-diagnostics.md) | 탐색용 RequestBank와 최종 상태·사유 분리 |
| 14 | [정책·측정 확장 구조](14-policy-metrics-extension.md) | 고객별 비용에 필요한 중립 측정, 합성 정책, Profile Registry 설계 |
| 15 | [초기해 포트폴리오](15-initial-solution-portfolio.md) | 현재 구현 범위의 포트폴리오와 후속 route pool/MIP 분리 |
| 16 | [WinCommerce PoC 벤치마크](16-win-poc-benchmark.md) | `win_poc_case.json` 기준 지표·비교·회귀 절차 |
| 17 | [선택 변형 문제 적용 가능성](17-optional-variant-feasibility.md) | MDVRP, OVRP, SDVRP feasibility study와 코어 변경 경계 |
| 18 | [설계 문서 거버넌스](18-document-governance.md) | Master, Domain, Algorithm, Deployment 문서 역할과 source-of-truth |

## 후속 통합·결정 세션

| 번호 | 문서 | 역할 |
|---:|---|---|
| 19 | [Master 통합 계획](19-integration-plan.md) | 28개 canonical 질문, C/P/Q baseline과 세션 20~26 경계 |
| 20~24 | [도메인·입력](20-domain-input-draft.md), [정책·목적](21-policy-objective-draft.md), [알고리즘](22-algorithm-draft.md), [결과·benchmark](23-result-benchmark-draft.md), [roadmap](24-roadmap-draft.md) | Master 통합용 상세 review input |
| 26~28 | [Master review](26-master-review.md), [수정 기록](27-review-corrections.md), [독립 재검토](28-master-re-review.md) | Verifier/governance correction과 `READY_FOR_REVIEW` 판정 |
| 29 | [열린 질문 인터뷰](29-open-question-interview.md) | 26개 interview 대상의 권위 있는 사용자 답변 |
| 30 | [열린 질문 통합](30-open-question-integration.md) | Master·질문 등록부·영향 세션 반영과 validation 기록 |
| 31 | [Domain Design 통합](31-domain-design-integration.md) | 세션 29 결정에 맞춘 상세 Domain Design 재구성과 정합성 validation 기록 |

세션 09~16과 20~24의 상단 “세션 30 통합 상태”는 본문의 과거 TBD/임시값이 현재 계약으로 오해되지 않도록 하는 최소 overlay다. 현재 상세 도메인 의미는 Master, 질문 등록부와 세션 31을 통해 갱신된 Domain Design을 함께 검토하되 conflict authority는 Master §1을 따른다.

## 권장 검토 순서

내일 검토할 때는 번호순보다 다음 순서가 의존관계를 이해하기 쉽다.

1. 문서와 시스템 기준: `18 → 02 → 01`
2. 입력 계약과 수치: `11 → 12 → 09 → 10`
3. 문제 모델과 상태: `03 → 05 → 06 → 13 → 17`
4. 고객사 정책과 탐색: `04 → 14 → 07 → 08 → 15`
5. 1차 품질 기준: `16`

## 중요한 교차 검토 지점

- 세션 03의 요청 모델은 세션 06의 불변조건과 함께 확정해야 한다.
- 세션 04의 목적 구조와 세션 14의 측정·정책 구조는 하나의 평가 파이프라인으로 통합해야 한다.
- 세션 09~12는 단위와 입력 계약을 함께 다룬다. 특히 `distanceMatrix.D/U/C`의 의미와 단위는 PDF에 명시되지 않아 질문 답변 전까지 확정하지 않는다.
- 세션 01의 GCP timeout 계층은 세션 07의 watchdog 계약과 함께 정해야 한다.
- 세션 15의 초기해 평가 기준은 세션 16의 PoC 비교 벡터와 일치해야 한다.
- 세션 17의 변형 문제 지원 범위는 세션 03의 표준 요청 모델이 확정된 뒤 결정한다.

## 최초 검토 당시 다음 단계

아래 항목은 세션 01~18 작성 당시의 역사적 계획이다. 현재 상태와 다음 gate는 [질문 등록부](../master-design-open-questions.md), [세션 30](30-open-question-integration.md)과 [세션 31](31-domain-design-integration.md)을 따른다.

1. 각 문서의 `남은 질문`에 답변하거나 명시적으로 보류한다.
2. 질문 답변을 해당 세션 문서에 반영해 `확정 사항`과 `잠정안`을 분리한다.
3. 18개 세션의 확정 내용만 사용해 GCP 기준 `master-design.md`를 새로 구성한다.
4. Master Design과 상세 Domain/Algorithm/Deployment 문서 사이의 중복을 제거하고 추적 링크를 추가한다.
