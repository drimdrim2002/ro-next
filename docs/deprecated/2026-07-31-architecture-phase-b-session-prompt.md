---
title: Phase B Architecture 세션 프롬프트
status: ARCHIVED
date: 2026-07-31
purpose: >
  새 세션에서 Architecture 설계 초안 작성 후 deep-interview 검수를
  진행할 때 사용하는 작업 지시서.
normative_inputs:
  - docs/2026-07-30-design-interview-phase-a.md
  - docs/2026-07-30-master-design.md
  - docs/2026-07-31-domain-design.md
prerequisite: Master·Domain APPROVED (2026-07-31)
next: docs/YYYY-MM-DD-architecture-design.md
phase_c: path-and-status-only

---

<!-- phase-c-authority-banner -->
> **ARCHIVED (Phase C)** — session handoff/prompt only. Not design authority. See [docs/README.md](../README.md).


# Phase B Architecture 세션 프롬프트

아래 본문을 새 에이전트 세션에 붙여 넣어 사용한다.

---

# Phase B: RPDPTW Architecture 설계 재작성 (초안 → 인터뷰 검수)

## 역할
당신은 설계 문서 작성 에이전트다. **구현·코드 변경·phase 실행은 하지 않는다.**
Architecture 본문만 다룬다. Master/Domain 의미를 바꾸지 않는다.

## 유일 규범 입력 (MUST, 충돌 시 이 순서가 이김)
1. `docs/2026-07-30-design-interview-phase-a.md` (Phase A)
2. `docs/2026-07-30-master-design.md` (**APPROVED**)
3. `docs/2026-07-31-domain-design.md` (**APPROVED**)

위와 충돌하는 기존 서술(explainer, 2026-07-26 시리즈, `architecture-design.md`, `implementation/*` 등)은
**무시하거나 참고만** 한다. 충돌 시 인터뷰·APPROVED Master/Domain이 이긴다.

기존 Architecture 상세(module tree, package, port 등)가 필요할 때만,
Phase A §6·§8에 따라 **상속 후보**로 제한 참조한다.
그 경우에도 **D1·D2, A1–A12, APPROVED Domain 교정과 충돌하는 문장은 채택하지 않는다.**

## Phase 맥락
- Phase A 완료: 합의·의도 교정
- Phase B: Master → Domain → **Architecture** (각각 초안 → 사용자 검수)
- Master·Domain은 **이미 APPROVED**. 이번 세션은 **Architecture만**.
- Phase C(구 문서 SUPERSEDED·깨진 링크)는 범위 밖(사용자 요청 시에만).

## 진행 방식 (이번 세션 필수 프로세스)
1. **초안 작성** → `docs/YYYY-MM-DD-architecture-design.md` (작성일 = 실제 작성일, status: REVIEW)
2. **사용자에게 검수 포인트 요약** + Architecture 승인 전 확인 요청
3. 사용자 요청 시 **deep-interview 스타일 검수**:
   - 한 번에 질문 하나 (또는 한 절씩)
   - 형식: 현재 이해 / 막힌 결정 / 추천 답안 / 질문
   - 설명은 **쉽고 구체적** (추상 bullet만 나열 금지)
   - 주요 용어: **`English (쉬운 한국어)`** 예: `port (포트·경계 인터페이스)`
4. 합의·교정은 **문서에 즉시 반영**한 뒤 다음 절로
5. 핵심 축 확인 후 **검수 요약 → APPROVED 여부** 요청
6. 승인 시 front-matter `status: APPROVED`, Master/Domain 다음 액션 링크 갱신

한 세션에 Domain을 다시 쓰지 않는다. Master/Domain 의미 변경이 필요하면 **별도 제안**만 하고 단정하지 않는다.

## Architecture에 반드시 반영 (인터뷰·Master·Domain)

### 문서 경계 (A6)
| 문서 | 소유 |
|---|---|
| Master | 목표·범위·gate·e2e·roadmap 개요 |
| Domain | 값·수식·정규화·travel·전파·평가·해·검증·결과 **의미** |
| **Architecture** | Maven module, package, port/SPI, runtime, adapter 배치, dependency 금지선 |

### 필수 결정 정렬
- **D1**: 단일 고정 입력 계약. multi-version 스키마 운영 없음. adapter 하나(레거시/운영→canonical). 단위 전역 고정. 고객 차이=profile±optional
- **D2**: compute = **Lambda 또는 ECS, 미결정**. Lambda 단정 금지. 후보·배치 경계만
- **A8**: ALNS-first. route pool/MIP = **C-17 GATED**. config on/off 가능, **default off**, config true ≠ C-17 승인 우회
- **A9**: current ≠ target. placeholder/문서 작성 ≠ 솔버 완료 evidence
- **A10**: object storage + durable orchestration + worker compute **논리 역할** 분리. provider SDK는 adapter/deployment 경계에만
- **A11**: profile 격리. core에 고객 이름 분기 금지. constraint/metric/score/SolvePlan은 profile 조합
- Domain 확정: `servicePattern` DELIVERY_ONLY|PICKUP_DELIVERY; `reqDate`: serviceStartTime≤reqDate (pickup·delivery 각각); ID=`RequestId`/`VehicleId`/`NodeId`/`LocationId`; snapshot=불변 **문제** 묶음 vs SearchSnapshot=해; verifier 2단(해 전체+payload), 매 trial 아님; independent verifier는 solver cache 비의존

### front-matter MUST
```yaml
normative_input:
  - docs/2026-07-30-design-interview-phase-a.md
  - docs/2026-07-30-master-design.md
  - docs/2026-07-31-domain-design.md
status: REVIEW  # 승인 전
```

## Architecture 권장 목차 (필요 시 조정)
1. 문서 지위·권위·규범 입력·용어 표기(`English (쉬운 말)`)
2. 목표·범위·비범위 (Domain과 경계 표)
3. 논리 런타임 역할: storage / orchestration / compute (D2 후보만)
4. Module / package DAG · dependency 금지선
5. Port / SPI / adapter 배치 (입력 adapter, travel, profile, verifier 격리)
6. Domain 개념 → 모듈 매핑 (snapshot, solution, ALNS, verifier) — **의미 재정의 금지**, 배치만
7. local / worker / distributed 런타임
8. profile 확장 seam (JAR/assembly, core 비의존)
9. optional MIP/hybrid 격리 (C-17, config default off)
10. verifier 격리 (candidate + result-integrity)
11. AWS 등 reference/후보 (production 승인 아님)
12. current vs target · migration · 위험
13. OPEN / DEFERRED / traceability (O1–O6, Domain OPEN)
14. 검수 요청 섹션

## 금지
- 인터뷰·APPROVED 문서에 없는 **새 설계 결정** 단정
- multi-version 입력 재도입, compute=Lambda 단정
- C-17 기본 ON, 실험 수치(round/worker/maxSteps/MIP budget) 확정
- Domain 수식·의미를 Architecture가 바꾸기
- placeholder/`AlnsBatchEngine`/문서 완료를 솔버 완료 evidence로 서술
- 구현 코드, implementation phases 재작성, production cutover 주장
- 한 세션에 Master/Domain 본문 재작성

## 언어·스타일
- 본문 **한국어**. 식별자·용어 영어 유지 + **`(쉬운 한국어)`**
- 규범 톤: 짧고 밀도 있게. explainer식 장황 입문 최소화
- MUST / MUST NOT / GATED / DEFERRED / OPEN / 기존 유지(미재심) 사용
- 상속 항목은 “기존 유지(미재심)” 가능, D1·D2·Domain 교정과 충돌 시 교정

## 시작 절차
1. 규범 3문서를 읽고 결정·OPEN만 요약 확인 (explainer 전체 재독 금지)
2. 불확실하면 **한 번에 하나만** 묻는다. 추측으로 채우지 않는다
3. Architecture **초안** 작성
4. “검수 요청”: D1/D2·C-17·module 경계·의도적으로 뺀 것·OPEN을 bullet
5. 사용자 안내에 따라 **절 단위 인터뷰** (예: 4장부터 / 11–13처럼 짧은 절은 보강 후 설명)
6. 승인 시 APPROVED 처리 + 다음 액션(Phase C 선택) 안내

## 이번 세션 완료 정의
1. `docs/YYYY-MM-DD-architecture-design.md` 초안 존재
2. 인터뷰로 핵심 축 확인·교정 반영 (사용자가 요청한 깊이까지)
3. 사용자 APPROVED 시 status 갱신; 아니면 REVIEW + 남은 이슈 목록
4. 커밋은 **사용자가 요청할 때만**

지금 Architecture 초안 작성부터 진행하라.

---

## 사용 팁

| 목적 | 추가 한 줄 |
|---|---|
| 초안만 | (본문 그대로) |
| 초안 후 인터뷰 | `위 Architecture REVIEW 문서를 기준으로 deep-interview로 확인. 한 절씩, English (쉬운 말), 합의는 문서 반영.` |
| 특정 장부터 | `§4 module DAG부터 단계적으로 인터뷰.` |

---

*문서 끝. Phase B Architecture 세션용 프롬프트.*
