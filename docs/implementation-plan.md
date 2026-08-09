---
title: RO-Next Implementation Plan
status: CONFIRMED
date: 2026-08-09
supersedes: docs/deprecated/implementation-15phase/ (ARCHIVED)
master: docs/master-design.md
---

# RO-Next Implementation Plan

확정된 설계([Master](master-design.md) · [Domain](domain-design.md) · [Architecture](architecture-design.md))를
코드로 만드는 단계 계획이다. 이전 15-phase 문서 세트(약 27,000줄)는 폐기된 구 설계 기준이라
아카이브했고, 이 문서 하나가 그것을 대체한다.

원칙: 단계마다 **동작하는 테스트**가 완료 기준이다. 문서 작성·클래스 존재는 완료가 아니다.
각 단계의 세부 작업 분해는 구현 세션 재량이며, 이 문서는 순서·경계·완료 기준만 고정한다.

## 0. 최종 성공 기준 (변하지 않는 목표)

> [data/win_poc_case_floor.json](../data/win_poc_case_floor.json) (주문 452건·차량 31대)을
> 앱에 접수 → 실제 ALNS로 풀이 → **재검증 통과** → 결과 JSON 생성.
> 이후 같은 입력의 기존 엔진(Win) 결과와 지표(미배정 수·차량 수·총거리) 비교.

## 1. 단계

### Stage 0 — 정리와 뼈대

| 작업 | 내용 |
|---|---|
| 코드 정리 | GCP 의존성 제거(pom), `gcp/`·`.serverless/`·빈 모듈 잔재(`rpdptw/`·`adapters/`·`apps/`·`build/` 디렉터리)·구 `src/`(placeholder) 삭제 |
| 뼈대 | parent pom + `solver-core`(의존 0) + `app`(Spring Boot 3, starter-web) 2모듈 구성 |
| 경계 테스트 | ArchUnit: `verify.. ↛ solve..` 규칙 (빈 패키지 상태로도 룰 파일 먼저) |
| README | 루트 README를 확정 설계에 맞게 갱신 |

**DoD**: `mvn verify` 통과. `app` 기동 후 health 응답. GCP 의존성 0.

### Stage 1 — canonical 입력과 정규화 (solver-core)

Domain §1–§3. canonical 모델(`Request`·`Vehicle`·`Plan`), 단위 정규화(kg×1000 FLOOR 등),
시간 원점(planStart 기준 초), serviceTime 공식, 호환성 판정, 입력 오류·`UNSUPPORTED_INPUT` 분류.

**DoD**: 단위·경계값(FLOOR, 소수 거부, optional 부재 = 제약 없음) 단위 테스트.
`multiRotation != 0` 거부 테스트.

### Stage 2 — 이동표와 Problem 동결 (solver-core)

Domain §4–§5. `LocationId` 기반 이동표(D/U, 누락 보정: Great Circle·`ceil(D×3.6/speed)`),
`Problem` 생성 시 참조·완전성 검증과 동결.

**DoD**: 이동표 보정 규칙 테스트. Problem 생성 후 불변성(문제 쪽 mutator 부재) 확인.

### Stage 3 — Solution·전파·평가 (solver-core)

Domain §6–§8. 경로/bank 상태와 XOR, 적재 부호 규칙(initialLoad 포함), 전파 루프
(arrival→대기→서비스→`reqDate`→load→hard), 기록 값(§7.3), metric, 사전식 comparator,
default profile + `ProfileRegistry`.

**DoD**: Domain §7.2 숫자 예를 그대로 재현하는 테스트. XOR 위반·hard 위반 검출 테스트.
미등록 customerId → default profile 테스트.

### Stage 4 — 초기해와 ALNS (solver-core)

Domain §9. 초기해 생성(greedy 삽입 등 재량), destroy/repair(pair 단위), acceptance,
시간 한도 종료. 삽입 shortlist 근사는 재량 (수락은 정식 평가만).

**DoD**: 소형 fixture에서 초기해 대비 개선 확인. pair·XOR 불변식이 탐색 중 유지되는
property 테스트 (예: 랜덤 스텝 N회 후 구조 검사).

### Stage 5 — 재검증과 결과 (solver-core)

Domain §10–§11. `verify` 패키지의 독립 재검증(전체 해, 캐시 없이), 결과 모델
(routes/unassigned+reason/metrics/run 메타), 검증된 해 ↔ 결과 일치 테스트.

**DoD**: 일부러 오염시킨 해(짝 분리·용량 초과·점수 불일치)가 전부 FAIL. ArchUnit 규칙 통과.

### Stage 6 — 앱 조립 (app)

Architecture §3. 규약 JSON adapter(win_poc fixture로 검증), `SolveStore`(fake + S3 구현),
접수 API(검증→저장→200+solveKey), `SolveExecutor`(상태 전이·heartbeat·STALE), 조회 API.

**DoD**: fake 저장소로 e2e 통합 테스트 — POST 접수 → DONE까지 → GET 결과.
win_poc_case_floor.json 접수·완주 (**성공 기준 §0 달성 시점**).

### Stage 7 — ECS 배포

Architecture §5. Dockerfile(app jar), ECS Fargate 서비스·태스크 롤(S3), 환경변수 설정,
CloudWatch 로그. 선택: 배포 전 LocalStack e2e 1회.

**DoD**: 배포 환경에서 실제 S3로 §0 시나리오 1회 성공.

### Stage 8 — 벤치마크 비교 (마무리)

win_poc 입력의 Win 결과와 지표 비교(미배정·차량 수·거리·시간), 필요한 만큼 탐색 파라미터 조정.
비교 절차와 수치는 이 단계에서 기록한다 (사전 확정하지 않음).

## 2. 순서와 병행

```text
0 → 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8
            (6의 adapter·storage는 3 이후 병행 가능)
```

솔버(1–5)가 중심이고 앱(6)은 얇다. 한 Stage를 끝내고(테스트 green) 다음으로 간다.

## 3. 하지 않는 것

- MIP 재조합, SQS·Step Functions, multi-trip — 설계 범위 밖 (Master §4·§6)
- 탐색 파라미터의 사전 확정 — Stage 4·8에서 실험으로
- 15-phase 문서의 부활 — 참고가 필요하면 [아카이브](deprecated/implementation-15phase/README.md)를 읽되, 규칙 충돌 시 현행 설계 3문서가 이긴다
