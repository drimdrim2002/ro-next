---
title: RO-Next Master Design
status: CONFIRMED
date: 2026-08-09
supersedes: docs/deprecated/2026-07-31-phase-b-master-design.md
related:
  - docs/domain-design.md
  - docs/architecture-design.md
  - docs/implementation-plan.md
revisions:
  - 2026-08-09 최초 확정
  - 2026-08-11 §6 — "미리 필드를 만들어 두지 않는다"를 차량별 `multiRotation` 한 항목에서
    **전 항목 원칙으로 승격**하고, 유예 항목 등재부(Stage Extra)로의 포인터 추가.
    차량별 `multiRotation` 문단이 근거로 삼던 "이미 `trips`가 쓰는 방식" 참조도 제거했다
    (차량별 `trips`가 같이 유예돼 존재하지 않는 선례를 가리키게 됐다). 범위(§4)는 불변
  - 2026-08-13 전수 감사 기계적 정정 — §2-① "형식 검증"을 "형식·정규화 검증"으로
    (2026-08-11 접수 깊이 확정 정합 — 정본은 Architecture §3.1) · §3 결정 #10 "의존성 0"에
    compile scope 명시 · §6 차량별 multiRotation 문단의 덮어쓰기 규칙 참조에 유예 상태 명시 ·
    §7 "과거 문서는 전부 deprecated/에" 문구를 아카이브 표 참조로 교정
---

# RO-Next Master Design

배차 최적화(RPDPTW) 서비스의 목표·범위·핵심 결정을 정의하는 최상위 문서다.
2026-08-09 재설계 인터뷰의 결과이며, 이전 문서들의 결정 등록부(A\*/D\*/O\*)·gate 체계는 폐기하고
**현재 유효한 내용만** 담는다.

## 1. 무엇을 만드는가 (한 문장)

> 호출 시스템이 규약 JSON으로 보낸 배차 요청을 접수하고,
> ALNS 탐색으로 "차량별 방문 경로 + 미배정 주문" 배차안을 만든 뒤,
> **별도 검증 코드가 처음부터 다시 계산해 규칙 위반이 없음을 확인한 결과만** 결과 JSON으로 저장하는
> **단일 Spring Boot 서비스** (AWS ECS Fargate, 저장은 S3만).

## 2. 한 건의 요청이 흐르는 전체 그림

이 그림이 시스템 전체다. 각 단계의 정확한 의미는 [Domain Design](domain-design.md),
코드·인프라 배치는 [Architecture Design](architecture-design.md)이 정의한다.

```text
① 접수 (동기 — HTTP 한 요청)
   호출 시스템 → POST (규약 JSON: 주문·차량·거리표·옵션)
   → 형식·정규화 검증 → S3에 입력 저장 → 200 + solveKey 응답
   (여기서 배차 계산을 기다리지 않는다. 잘못된 입력이면 4xx, S3 저장 없음)

② 풀이 (비동기 — 같은 앱 내부 executor)
   입력 → canonical 변환 → 정규화 → 이동표 준비 → Problem 동결
   → 초기해 생성 → ALNS 반복 개선 → 최선 Solution

③ 재검증 (발행 전 안전장치)
   별도 검증 코드가 최선 Solution을 캐시 없이 처음부터 재계산
   → pair·용량·시간창 등 hard 규칙 전부 확인
   → 실패 시 결과를 내보내지 않고 FAILED 처리 (탐색 코드 버그로 취급)

④ 결과 저장
   검증 통과한 Solution → 결과 JSON (차량별 경로 + 미배정 주문&사유 + 지표)
   → S3에 저장, 상태 DONE

⑤ 조회
   호출 시스템이 solveKey로 GET 상태/결과 API 호출 (또는 S3 직접 읽기)
```

## 3. 핵심 결정

| # | 결정 | 내용 |
|---|---|---|
| 1 | **입력 계약 하나** | 솔버가 이해하는 canonical(정본) 입력 의미는 하나. 버전별 스키마 병행 운영 없음. 외부 규약([RO Input Json Spec](../data/ro_input_json_spec.pdf))과의 차이는 **adapter 하나**가 변환 |
| 2 | **pair가 원자 단위** | 배정·이동의 단위는 개별 지점이 아니라 `Request`(pickup+delivery 짝). 짝이 갈라지면 품질 문제가 아니라 **구조 결함** |
| 3 | **delivery-only도 같은 core** | 배송만 있는 주문(현 규약 전부)도 같은 RPDPTW core가 처리. 가짜 픽업 방문을 만들지 않음 |
| 4 | **Problem 동결 후 탐색** | 풀이 시작 시 문제(주문·차량·이동표·규칙 설정)를 `Problem`으로 동결. 탐색은 `Solution`(경로·미배정)만 변경 |
| 5 | **ALNS이 기본 탐색** | 초기해 생성 → ALNS(destroy/repair) 개선. MIP 재조합은 §6 향후 옵션 |
| 6 | **재검증 1회** | 결과 저장 직전, 탐색 코드와 분리된 검증 코드가 최종 배차안 전체를 재계산. **재검증을 통과하지 못한 배차안은 결과로 저장하지 않는다** — 이것이 유일한 발행 규칙 |
| 7 | **고객 차이는 profile** | 점수·제약의 고객별 차이는 core 코드 분기(`if (customerId…)`)가 아니라 profile 구현 교체로. 연결은 코드 레지스트리(`customerId → profile` 맵), 미등록 고객은 default |
| 8 | **저장은 S3만** | 입력·진행 상태·결과 전부 S3 객체. RDB·Redis 사용하지 않음 |
| 9 | **단일 Spring Boot 서비스** | 접수 API와 풀이 executor가 한 앱. AWS **ECS Fargate** 배포. Lambda·Step Functions·SQS 사용하지 않음 |
| 10 | **모듈 3개** | `solver-core`(순수 Java, compile 의존 0 — test scope 제외) + `solver-profile`(고객 정책·고객 전용 라이브러리) + `app`(Spring Boot). 의존은 `app → solver-profile → solver-core` 한 방향. 경계는 컴파일 의존 + ArchUnit 테스트로 강제 (Architecture §2) |

## 4. 범위

**한다**

- 규약 JSON 접수 → canonical 변환 → 정규화 → 이동표 준비 → `Problem` 동결
- 초기해 + ALNS 탐색, hard 제약(용량·시간창·pair·구역 등) 준수
- 발행 전 독립 재검증 1회
- 결과 JSON: 차량별 경로 / 미배정 주문+사유 / 요약 지표 / 간단한 실행 메타
- 고객별 점수·제약 차이의 profile 격리
- ECS Fargate 단일 서비스 배포, 상태·결과 조회 API

**하지 않는다 (현재 범위 밖)**

- multi-trip (차고 재방문 회차, `multiRotation`이 `2` 이상 또는 `-1`) — 입력 거부.
  `multiRotation`은 **바퀴 수**이므로 `0`(미설정)·`1` = 1바퀴는 지원 범위 안이다 (Domain §2.5)
- 본격 multi-depot 최적화 (차량별 시작/도착 차고 지정까지만)
- MIP 재조합 (§6)
- 입력 스키마 다중 버전 운영
- 탐색 파라미터(반복 수·초기해 개수 등)의 문서 확정 — 구현·실험 재량

## 5. 완료와 검증 기준

- **제품 규칙 (유일)**: 재검증(§2-③)을 통과하지 못한 배차안은 결과로 저장하지 않는다.
- **구현 완료 기준**: 각 구현 단계의 DoD([Implementation Plan](implementation-plan.md))를 따른다 — 테스트 통과, 실제 fixture 실행 결과 같은 보통의 기준이다.
- **1차 성공 기준**: [win_poc_case_floor.json](../data/win_poc_case_floor.json)을 실제 솔버로 풀어
  재검증 통과 + 결과 JSON 생성. 이후 기존 엔진(Win) 결과와 지표 비교.

이전 문서의 "gate + evidence" 완료 판정 체계는 폐기했다.

## 6. 향후 옵션 (지금 하지 않음)

**MIP 재조합** — ALNS가 만든 경로 조각들을 모아 수리최적화(MIP/CP-SAT)로 "경로 고르기"를 다시 푸는
고급 기능. 효과가 필요하다고 판단될 때 별도 설계로 검토한다. 지금은 설계·구현·모듈 모두 범위 밖이며,
파이프라인에 자리를 예약해 두지 않는다.

**차량별 `multiRotation`** — 지금은 통과하는 값이 `{0, 1}`뿐이라 모든 차량이 1바퀴여서
차량마다 다를 수가 없다. 그러나 "차량마다 회전 수를 다르게 주고 싶다"는 요구가 있으므로,
**multi-trip(`2` 이상)을 여는 그 시점에 함께 다룬다** — 차량 값이 `options` 기본값을 덮어쓰는
규칙을 적용하면 된다 (그 규칙 자체도 지금은 유예 상태다 — Domain §2.5 유예 주석과
Stage Extra E1이 복원 지점이다). 그 전에 미리 필드를 만들어 두지 않는다.

**이 원칙을 전 항목에 적용했다 (2026-08-11).** 위 문단의 "그 전에 미리 필드를 만들어 두지
않는다"는 차량별 `multiRotation`에만 적용되던 규칙이었는데, Stage 1 검토에서 같은 성격의
선제 구현이 더 있는 것이 확인돼(wire에 없는 차량별 `trips`·치수 축·차량 소유 구분) 전부
유예했다. 판단 기준은 "지금 안 쓰는가"가 아니라 **"나중에 넣는 비용이 지금 넣는 비용과
같은가"**다 — 같으면(순수 add-only) 미루고, 다르면(타입 모양이 바뀌거나 여러 Stage를 동시에
고쳐야 하면) 지금 만든다. 다일 시간창·정수 단위 체계가 후자라 그대로 남았다.
지운 항목·근거·트리거·되살릴 지점은
[Stage Extra](implementation/stage-extra-deferred-features.md) 등재부에 있고,
작업 목록 등재는 [Plan §1 말미](implementation-plan.md)다.

그 외: 규약의 PICKUP_DELIVERY 확장(canonical은 이미 지원, wire 규약 협의 필요), multi-depot 확대,
분산 병렬 탐색(여러 워커 경쟁)은 필요해질 때 각각 별도 결정으로 연다.

## 7. 문서 지도

| 문서 | 소유 내용 |
|---|---|
| **Master (본 문서)** | 목표·전체 흐름·핵심 결정·범위·완료 기준 |
| [Domain Design](domain-design.md) | 입력·정규화·이동표·Problem/Solution·전파·평가·ALNS·재검증·결과 JSON의 **정확한 의미와 규칙** |
| [Architecture Design](architecture-design.md) | 모듈·패키지·경계 규칙, 앱 구조(API·executor·S3), ECS 배포, 로컬 환경 |
| [Implementation Plan](implementation-plan.md) | 구현 단계·순서·단계별 완료 기준, 기존 코드 정리 |

읽는 순서: Master → Domain → Architecture → Implementation Plan.
과거 문서는 [docs/deprecated/](deprecated/) 등 아카이브에 있으며(전체 목록은
[docs/README.md](README.md)의 아카이브 표) 참고용일 뿐 효력이 없다.

## 8. 용어 최소 사전

| 용어 | 뜻 |
|---|---|
| **규약 (spec)** | 호출 시스템과 이미 공유된 입력 JSON 형식. [data/ro_input_json_spec.pdf](../data/ro_input_json_spec.pdf) |
| **canonical (정본)** | 솔버 내부가 이해하는 유일한 입력 의미. adapter가 규약 → canonical로 변환 |
| **`Request`** | 운송 의무 한 건. pickup+delivery 짝(pair). 현 규약의 주문(order)은 delivery만 있는 Request |
| **`Problem`** | 풀이 시작 시 동결된 문제 묶음(주문·차량·이동표·규칙 설정). 이후 절대 변경되지 않음 |
| **`Solution`** | 배차안: 차량별 방문 순서 + 미배정 목록. 탐색이 바꾸는 유일한 대상 |
| **ALNS** | 해를 조금 부수고(destroy) 다시 넣으며(repair) 반복 개선하는 탐색 방법 |
| **재검증 (verify)** | 결과 저장 직전, 별도 코드가 배차안을 캐시 없이 처음부터 재계산해 규칙 준수를 확인 |
| **결과 JSON** | 호출 시스템에 내보내는 최종 산출: 차량별 경로 + 미배정+사유 + 지표 |
| **profile** | 고객별 점수·제약 구성 묶음. core는 profile만 보고 고객 이름을 모름 |
| **solveKey** | 접수 시 발급되는 S3 key prefix. 이후 상태·결과 조회의 손잡이 |
