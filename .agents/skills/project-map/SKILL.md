---
name: project-map
description: 모듈·패키지 지도. 어느 파일을 고칠지 모를 때 연다. Use when lost in the repo, asking where a change belongs, navigating packages, "어디를 수정", "어느 모듈", "어디 파일", or /project-map.
---

# 프로젝트 지도

클래스 명단이 아니다. 어디에 손을 댈지 모를 때 연다. **현재·다음 Stage는 여기에 적지 않는다** — `docs/implementation-plan.md`와 `docs/implementation/README.md`.

경계 한 줄은 `AGENTS.md`. 배치의 권위는 `docs/architecture-design.md`.

## 모듈과 패키지

```text
solver-core/     com.ronext.rpdptw
                 domain/          canonical 입력·정규화·배송정책 (Domain §1–4)
                   input/         raw 운반체 + PlanNormalizer
                 problem/         Problem 동결·이동표 (Domain §5)
                 eval/            평가 계약·profile SPI·DefaultProfile·Scores
                 solve/           Solution·전파·초기해·ALNS (Domain §6–9). 하위 패키지 없음
                 verify/          독립 재검증·결과 모델 (Domain §10). solve 참조 금지

solver-profile/  com.ronext.rpdptw.profile
                 ProfileRegistry  customerId → Profile (미등록 = default)
                 고객별 패키지     현재 0

app/             com.ronext.rpdptw.app
                 api/             REST 접수·조회          (패키지만, Stage 6)
                 run/             비동기 executor         (패키지만, Stage 6)
                 input/           규약 JSON ↔ raw 운반체  (패키지만, Stage 6)
                 storage/         S3 키·저장              (패키지만, Stage 6)
                 RoNextApplication + application.yml
```

의존: `app → solver-profile → solver-core`. core 내부: `problem→domain`, `eval→{domain,problem}`, `solve→{domain,problem,eval}`, `verify→{domain,problem,eval}`. `verify ↛ solve`, `solve ↛ verify` (ArchUnit `ArchitectureRulesTest`).

## 이 종류의 변경은 여기

| 하려는 일 | 위치 |
|---|---|
| 입력 의미, 정규화, 단위, 시간창 전개 | `solver-core` `domain` / `domain.input` |
| 이동표, `Problem` 동결 | `problem` |
| hard 제약, score 축, 기본 profile | `eval` |
| 고객별 `Profile` 구현·등록 | `solver-profile` |
| 경로, 삽입, 초기해, ALNS, 탐색 예산 (`AlnsConfig`) | `solve` |
| 재검증, 결과 조립 (`SolveResult`) | `verify`. `Solution`을 받지 않는다 — 호출자가 분해해 넘긴다 |
| HTTP 접수·조회 | `app` `api` |
| 비동기 풀이 실행 | `app` `run` |
| 규약 JSON adapter (canonical 확정은 core 정규화) | `app` `input` |
| solveKey, S3 배치 | `app` `storage` — 키 조립은 여기 한 곳만 |
| 모듈 경계 가드 | `solver-core` `ArchitectureRulesTest` |

빈 `api`·`run`·`input`·`storage`에서 타입이 grep에 안 나오는 것은 아직 안 만든 것이다. 다른 데 있는 게 아니다.

## 어디에 둘지

Architecture §2.3. 유효한 답의 집합을 바꾸면 `Problem` 쪽 (`domain` 정책). 답이 달라질 뿐 무효가 아니면 `solve`의 `AlnsConfig`처럼 `Problem` 밖 — 재검증이 못 본다. 다른 고객에게도 의미 있으면 canonical optional, 그 고객만이면 `solver-profile`.

## 요청 한 건

```text
POST /solves → 규약 JSON·정규화 검증 → S3 input.json → 200 + solveKey   ← 동기 여기까지
             → executor: adapter → 정규화 → Problem 동결 → 초기해 → ALNS
             → 재검증 (캐시 없이 처음부터)
             → PASS: result.json + DONE / FAIL: 저장 없이 FAILED
GET /solves/{solveKey}[/result] → S3 조회
```

S3: `solves/{customerId}/{planId}/{runId}/` = solveKey. 그 아래 `input.json`·`status.json`·`result.json`.

## 없는 경로

`rpdptw/` · `adapters/` · `build/` · `apps/` · `gcp/` · 루트 `src/` · `.serverless/` · `node_modules/` · 루트 `Dockerfile` — Stage 0에서 삭제. 옛 대화에 보이면 지금 없는 것이다. Dockerfile은 Stage 7에서 새로 쓴다.

## 실행 fixture

| 파일 | 역할 |
|---|---|
| `data/win_poc_case_floor.json` | 실행 fixture (주문 452·차량 31). 수치 number |
| `data/win_poc_case.json` | 수신 원형. 수치 문자열. 미접수 |
| `data/ro_input_json_spec.pdf` | 규약 원본 |
| `data/alns_result.csv` | 기존 엔진(Win) 비교 |
| `scripts/floor_win_poc_matrix.py` | 거리표 소수 FLOOR |
| `scripts/numify_win_poc_fixture.py` | 문자열→number (floor fixture) |
