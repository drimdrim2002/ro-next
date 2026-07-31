---
title: Phase B Architecture §4 검수 핸드오프
status: ARCHIVED
date: 2026-07-31
purpose: >
  새 세션에서 Architecture §4 (Module / package DAG · dependency 금지선)
  deep-interview 검수만 이어갈 때 사용한다.
work_document: docs/2026-07-31-architecture-design.md
work_document_version: "1.2"
normative_inputs:
  - docs/2026-07-30-design-interview-phase-a.md
  - docs/2026-07-30-master-design.md
  - docs/2026-07-31-domain-design.md
inheritance_candidate: docs/2026-07-26-architecture-design.md
resume_from: "§4 Module / package DAG · dependency 금지선"
do_not_restart: architecture full draft rewrite
phase_c: path-and-status-only

---

<!-- phase-c-authority-banner -->
> **ARCHIVED (Phase C)** — session handoff/prompt only. Not design authority. See [docs/README.md](../README.md).


# Phase B Architecture — §4 검수 핸드오프 (새 세션용)

아래 본문을 새 에이전트 세션에 붙여 넣거나 `@` 로 첨부해 사용한다.

---

# 역할

설계 문서 작성 에이전트. **구현·코드 변경·phase 실행·커밋(요청 전) 금지.**  
**Architecture 본문만** 교정. Master/Domain 의미 변경 금지.  
초안 **전면 재작성 금지.** `docs/2026-07-31-architecture-design.md` 를 읽고 **§4부터 검수**.

---

# 규범 입력 (충돌 시 이 순서)

1. `docs/2026-07-30-design-interview-phase-a.md`
2. `docs/2026-07-30-master-design.md` (**APPROVED**)
3. `docs/2026-07-31-domain-design.md` (**APPROVED**)

상속 후보(필요 시만): `docs/2026-07-26-architecture-design.md`  
→ D1·D2·Domain 교정·아래 **합의 표**와 충돌 문장 **채택 금지**.

---

# 작업 문서

| 항목 | 값 |
|---|---|
| 파일 | **`docs/2026-07-31-architecture-design.md`** |
| status | **REVIEW** |
| version | **1.2** (검수 중 갱신 가능) |
| §4 위치 | `## 4. Module / package DAG · dependency 금지선` |

---

# 이미 합의 (MUST 유지 — 재질문으로 번복하지 말 것)

| 주제 | 합의 |
|---|---|
| **D1** | 단일 canonical. multi-version 없음. adapter 하나. 단위 전역 고정 |
| **D2 / O1** | compute = Lambda **또는** ECS, **미결정**. 당장 결정 불필요 |
| **저장** | **DB 없음**. **Redis 포기**. **S3 only** (결과·중간·artifact·run 상태) |
| **로컬 통합** | **LocalStack S3**. 단위 테스트 인메모리 fake 허용 |
| **orchestration** | 각본=`rpdptw-application`+port / 엔진=OPEN / 바이트=S3 |
| **A6 §3** | 논리 runtime **의미**는 Master §5.2. Arch §3은 **배치만** (옵션1 축소 **완료**) |
| **C-17** | ALNS-first. hybrid optional. config **default off**. true ≠ 승인 우회 |
| **A9** | placeholder / LocalStack 기동 ≠ 솔버 완료 evidence |
| **A11** | profile SPI. core 고객명 분기 금지 |
| **스타일** | 한국어 본문. `English (쉬운 한국어)`. 발표 가능하도록 **풀어 설명** |

---

# 절 진행 상태

| 절 | 상태 |
|---|---|
| §1–2 | 초안. 깊은 인터뷰 없음 |
| **§3** | **검수·축소 완료** — 다시 철학 장문으로 되돌리지 말 것 |
| **§4** | **이번 세션 주 목표** — 초안만 있음, deep-interview |
| §5–15 | 초안. §4 이후 절 단위 |

---

# 이번 세션 목표: §4 검수

## §4 초안 구성 (현재)

- **4.1** Maven target tree (`rpdptw-core/solver/verification/application`, `adapters/s3`, profiles, apps, …)
- **4.2** Module DAG (`verification` ↛ `solver` 핵심)
- **4.3** Dependency 금지선 (cloud/OR-Tools/JDBC/Redis/S3 SDK 위치)
- **4.4** Package 책임 요약 (`com.ronext.rpdptw`)
- **4.5** Build enforcement

## §4 검수 시 확인 축 (에이전트가 설명할 것)

1. **module 쪼개기 이유** — 왜 core / solver / verification / application 분리인가 (발표용으로 풀기)
2. **금지선 한 줄** — `verification` ↛ `solver` (independent verifier)
3. **S3 SDK·LocalStack** — adapters 만, core 침투 금지 (합의와 정합)
4. **C-17 module** — optional `route-selection-*`, default build 오염 금지
5. **profiles** — core only 의존, search internal 금지
6. **O6** — class 전수 vs 경계 중심 깊이 (확정 수치·새 모듈 남발 금지)
7. Domain 의미 재정의가 package 표에 섞였는지

## 진행 방식 (필수)

1. 작업 문서 §4 + 본 핸드오프 합의 표 읽기 (explainer 전체 재독 금지)
2. 첫 응답: 상태 한 줄 + **§4 검수 시작** 확인  
   예: “Architecture v1.2 REVIEW. §3 축소 완료. **§4 Module DAG 부터 deep-interview.**”
3. **deep-interview**
   - 한 절 또는 한 하위절씩 (4.1 → 4.2 → … 권장)
   - 형식: **현재 이해 / 막힌 결정 / 추천 답안 / 질문**
   - 설명: 쉽고 구체적, `English (쉬운 한국어)`
4. 합의는 **문서에 즉시 반영** 후 다음 하위절
5. §4 끝나면 사용자에게 §5 진행 여부 확인
6. **커밋은 사용자 요청 시에만**

## 금지

- Architecture 초안 전면 재작성
- §3을 다시 장문으로 부풀리기 (배치 표 유지)
- Redis/DB 재도입, multi-version, Lambda 단정, C-17 기본 ON
- Domain 수식·의미 변경, Master/Domain 본문 재작성
- 구현 코드, implementation phases 재작성
- 실험 수치(round/worker/maxSteps) 확정
- 합의 없는 새 Maven 모듈 남발

## 상속 후보 사용 시

`2026-07-26-architecture-design.md` 의 module tree는 **참고**.  
채택 전 **S3 only · no Redis · no DB · verification↛solver · C-17 optional** 과 대조.

---

# 시작 체크리스트 (새 세션 첫 툴 사용 전)

- [ ] `docs/2026-07-31-architecture-design.md` front-matter (v1.2, storage, local, section_3_policy)
- [ ] §3이 “짧게/배치만”인지 확인 (철학 장문 복귀 금지)
- [ ] §4.1–4.5 읽기
- [ ] 사용자에게 §4 진행 확인 후 **4.1부터** 풀어 설명 + 질문 하나

**지금 §4.1 Maven target tree 부터 검수를 시작하라.**

---

## 새 세션 붙여넣기 한 줄 예시

```text
@docs/2026-07-31-architecture-phase-b-s4-handoff.md
@docs/2026-07-31-architecture-design.md
위 핸드오프대로 Architecture §4 Module DAG deep-interview 진행.
```

---

*핸드오프 끝. §4 검수 전용.*
