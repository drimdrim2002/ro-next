# 세션 02 — RPDPTW 용어 표준화

## 확정 사항

- 이 프로젝트의 확장 문제 모델 공식 약어는 **`RPDPTW`**로 통일한다.
- 공식 영문 풀이는 **Rich Pickup and Delivery Problem with Time Windows**이다.
- 기존 마스터 설계의 `RPDPDTW`는 공식 영문 명칭에서 유도되지 않는 잘못된 표기다. `Rich(R) + Pickup(P) + Delivery(D) + Problem(P) + Time(T) + Windows(W)`의 순서에 따라 `RPDPTW`가 맞다.
- 이 결정은 문서와 향후 구현의 명명 기준을 정하는 것이다. 이 세션에서는 코드, 패키지 또는 기존 설계 문서를 변경하지 않는다.
- 학술 문제인 **PDPTW**와 프로젝트의 확장 표준 모델인 **RPDPTW**는 구분한다.
  - `PDPTW`: 일반적인 Pickup and Delivery Problem with Time Windows 및 Ropke–Pisinger 논문의 대상 문제
  - `RPDPTW`: PDPTW에 업무용 용량·근무·지역·호환성·고객사별 정책 등의 rich 제약을 포함하는 이 프로젝트의 표준 모델
- 따라서 `PDPTW`를 모두 `RPDPTW`로 일괄 치환하지 않는다. 문맥상 프로젝트 표준 모델을 가리킬 때만 `RPDPTW`를 사용한다.

## 변경 대상

현재 문서 기준 조사 결과는 다음과 같다.

| 대상 | 현재 상태 | 향후 반영 |
|---|---|---|
| `docs/master-design.md` | 대문자 `RPDPDTW` 25곳 | 모두 `RPDPTW`로 변경 |
| `docs/master-design.md` | 소문자 패키지 표기 `rpdpdtw` 9곳 | 모두 `rpdptw`로 변경 |
| `docs/master-design.md` | 타입명 `RpdpdtwSolution`, `RpdpdtwBenchmarkRunner` 2곳 | `RpdptwSolution`, `RpdptwBenchmarkRunner`로 변경 |
| `docs/domain-design.md` | `RPDPTW`와 `rpdptw` 사용 | 이미 표준에 맞으므로 유지 |
| `docs/arranged/01_problem_definition.md` | 학술 문제군 `PDPTW` 사용 | 의미가 다르므로 유지 |
| `docs/arranged/07_papers_and_benchmarks.md` | Ropke–Pisinger 논문 대상 `PDPTW` 사용 | 원 논문의 문제명을 가리키므로 유지 |
| `docs/orgin/alns_pdptw_paper_summary_ko.md` 및 파일명 | 원 논문의 `PDPTW` 사용 | 유지하며 파일명도 변경하지 않음 |

`master-design.md`의 변경 범위는 다음과 같이 분류된다. 줄 번호는 세션 조사 시점의 문서 기준이다.

- 제목·메타데이터·정의: 1, 4, 26, 28, 43, 48, 52, 70행
- 목표·구조·변환 흐름: 99, 121, 154, 174, 187, 224, 270, 348, 366행
- 업무 변환·로드맵·결론: 1033, 1035, 1039, 2011, 2126, 2160, 2216, 2234행
- 패키지 표기: 280, 394, 397, 424, 436, 448, 455, 472, 489행
- 타입명 예시: 517, 1557행

`docs` 아래 Markdown 문서 전체에서 두 설계 문서를 제외하면 `RPDPDTW`, `RPDPTW`, `rpdpdtw`, `rpdptw` 표기는 발견되지 않았다. 즉 현재 확인 가능한 변경 영향은 두 설계 문서 안의 목표 명명에 한정된다.

## 명명 규칙

| 사용 위치 | 규칙 | 예시 |
|---|---|---|
| 한국어·영어 본문, 표, 다이어그램 | 약어 전체를 대문자로 표기 | `RPDPTW 표준 모델`, `RPDPTW ProblemInstance` |
| 최초 정의 | 약어와 공식 영문 풀이를 함께 표기 | `RPDPTW (Rich Pickup and Delivery Problem with Time Windows)` |
| 이후 본문 | 약어만 사용 가능 | `RPDPTW 엔진` |
| Java 패키지명 및 디렉터리명 | 전체 소문자 `rpdptw` | `...solver.rpdptw`, `rpdptw.domain` |
| Java 타입명 | Java의 PascalCase 관례에 따라 `Rpdptw` 접두어 사용 | `RpdptwSolution`, `RpdptwBenchmarkRunner` |
| 설정 키·JSON 필드가 필요한 경우 | 기존 프로젝트의 lowerCamelCase 관례 적용 | `rpdptwConfig` |
| 로그·메트릭·저장 경로의 식별자가 필요한 경우 | 해당 플랫폼의 규칙을 따르되 철자 배열은 `rpdptw` 유지 | `rpdptw_solver_*`, `rpdptw/...` |
| 일반 학술 문제 또는 논문 인용 | 원래 약어 `PDPTW` 유지 | `ALNS for PDPTW` |

추가 규칙은 다음과 같다.

- `RPDPDTW`, `rpdpdtw`, `Rpdpdtw`는 신규 문서와 향후 구현에서 금지 표기로 취급한다.
- `RPDPTW`와 `PDPTW`는 서로의 별칭으로 취급하지 않는다. 상위·하위 문제 관계를 설명할 때는 “RPDPTW의 기반 문제인 PDPTW”처럼 의미를 명시한다.
- 클래스명 안에서 약어 전체를 대문자로 유지한 `RPDPTWSolution`보다는 프로젝트의 기존 Java 명명 방식에 맞춘 `RpdptwSolution`을 사용한다.
- 패키지명 변경은 이 문서가 정하는 **향후 목표 구조**다. 이 세션에서 실제 소스 패키지를 이동하거나 호환 코드를 만들지 않는다.

## 마스터 설계 반영안

마스터 설계 개정 시 다음 순서로 반영한다.

1. 문서 제목과 문서 성격의 `RPDPDTW`를 `RPDPTW`로 바꾼다.
2. 용어 정의 절을 다음 의미로 수정한다.

   > RPDPTW는 Rich Pickup and Delivery Problem with Time Windows의 약자이며, 이 문서에서는 PDPTW를 기반으로 현실 업무 제약을 포함하도록 확장한 프로젝트 표준 문제 모델을 뜻한다.

3. 본문·표·다이어그램에서 프로젝트 표준 모델을 가리키는 `RPDPDTW`를 모두 `RPDPTW`로 바꾼다.
4. 목표 패키지 표기를 `com.sds.cello.ronext.solver.rpdptw`와 `rpdptw.*`로 바꾼다. 단, GCP 기준 아키텍처 재작성 과정에서 최상위 패키지 자체가 변경되면 그 결정에 따르고 마지막 세그먼트만 `rpdptw` 규칙을 유지한다.
5. 타입명 예시는 `RpdptwSolution`, `RpdptwBenchmarkRunner`로 바꾼다.
6. `PDPTW` 논문과 벤치마크를 설명하는 부분은 원래 약어를 유지하고, 프로젝트 표준 모델과 혼동될 수 있는 첫 등장에 두 용어의 관계를 한 문장으로 설명한다.
7. 개정 후 문서 검사에서 금지 표기인 `RPDPDTW|rpdpdtw|Rpdpdtw`가 0건인지 확인한다. 동시에 `PDPTW`가 학술 문제 문맥에서 보존되었는지 수동 검토한다.

## 남은 질문

1. 과거 AWS/ECS 환경의 외부 계약에 `RPDPDTW` 또는 `rpdpdtw`가 이미 노출된 적이 있는가? 예를 들어 JSON type 값, 작업 이름, S3 경로, 로그·메트릭 이름, 대시보드 필터가 이에 해당한다. 현재 저장소 문서와 소스에서는 확인되지 않았지만, 외부 운영 자산에 존재한다면 GCP 전환 설계에 구 표기 호환 또는 명시적 마이그레이션 항목을 추가해야 한다.
2. 고객이나 외부 시스템에 공개되는 명칭도 `RPDPTW`로 노출할 것인가, 아니면 이 약어는 내부 기술 모델명으로만 사용하고 외부 API에는 중립적인 업무 용어를 사용할 것인가? 이 결정은 내부 문서 표준화를 막지 않지만 향후 API·결과 포맷의 명명에 영향을 준다.
