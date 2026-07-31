---
title: RPDPTW 설계 권위 재확립 — Phase A 인터뷰 정리
status: PHASE_A_COMPLETE
version: 1.0
date: 2026-07-30
owner: 사용자 + deep-interview 세션
purpose: >
  설계 이해 합의와 의도 어긋남만 고정한다.
  Phase B(설계 3종 재작성)의 유일한 권위 입력이다.
authority: >
  다음 세션에서 master / domain / architecture 재작성 시
  이 문서만 규범 입력으로 사용한다.
  explainer·2026-07-26 시리즈·implementation 문서는
  참고 자료일 뿐 이 문서와 충돌하면 이 문서가 이긴다.
out_of_scope_this_document:
  - master-design / domain-design / architecture-design 본문
  - 구현 코드·phase 실행
  - 구 문서 SUPERSEDED 일괄 정리 (Phase C)
phase_c: normative-historical-input

---

<!-- phase-c-authority-banner -->
> **Normative historical input (Phase A)** — not current Master/Domain/Architecture body. Current design: [docs/README.md](../README.md).


# RPDPTW 설계 권위 재확립 — Phase A 인터뷰 정리

## 0. 이 문서의 지위 (필독)

| 항목 | 내용 |
|---|---|
| **Phase** | A — 컨텍스트 절약, **결정·합의만 고정** |
| **이번 산출** | 이 인터뷰 정리 **1개만** |
| **하지 않은 것** | 설계 3종 본문 작성, 구현, 전 문서 링크 일괄 수정 |
| **다음 단계** | Phase B — 이 문서만 입력으로 설계 재작성 |
| **권위 규칙** | Phase B에서 **이 파일이 유일 규범 입력**. 기존 explainer/dated design과 충돌 시 **이 문서 우선** |

### Phase 계획

| Phase | 목표 | 산출 |
|---|---|---|
| **A (완료 대상: 이 문서)** | 핵심 축 합의 + 의도 어긋남 표 | 본 인터뷰 정리 |
| **B** | 설계 재작성 | `YYYY-MM-DD-master-design.md` → domain → architecture 순 |
| **C (선택)** | 권위 링크·SUPERSEDED·implementation README 정합 | 구 문서 상태 정리 |

Phase B 권장 순서: **Master 단독 → 검수 → Domain → 검수 → Architecture**.  
한 세션에 3종을 동시에 쓰지 않는다.

---

## 1. 세션 목적과 완료 기준 (Phase A)

### 1.1 목적

fable/codex가 정리한 Master / Domain / Architecture 및 explainer를 바탕으로  
사용자 이해를 검증하고, **문서 권위 재확립에 필요한 합의만** 남긴다.

### 1.2 Phase A 완료 기준

1. 핵심 축(목표, pair, 파이프라인, 문서 경계, gate, ALNS-first, C-17 gated 등) 합의  
2. 의도 어긋남 표 확정  
3. 본 인터뷰 정리 작성 + “다음 세션 유일 입력” 명시  

설계 3종 본문은 Phase A 완료 조건이 **아니다**.

---

## 2. 세션 운영 결정

| 축 | 결정 |
|---|---|
| 목표 유형 | **이해 검증 + 권위 재확립** (구현 착수 아님) |
| 설계 범위 | master / domain / architecture **3종 통합·교정** (본문은 Phase B) |
| 구현 문서 | phase plan·realization plan은 **이번 재작성 범위 밖** (참조만) |
| 결정 변경 한도 | 기존 설계 결정은 유지. **의도 어긋남으로 확인된 항목만** 재확정. 에이전트가 임의 신규 결정 금지 |
| 언어 | 최종 규범 문서는 **한국어**. 식별자·용어(`Request`, `C-17`, `RM-*` 등)는 영어 유지 |
| 진행 방식 | 구체 불만 목록 없이, 핵심 축 제시 → 맞음/다름/모름 |

---

## 3. 핵심 축 합의 (모두 사용자 확인: 맞음)

아래 문장은 Phase B Master 초안의 **의미 기준**이다.

### A1. 한 문장 목표

시스템은 단일 고정 입력 계약의 RPDPTW 입력을 읽어 immutable solve snapshot으로 문제 정의를 동결하고,  
모든 `Request`의 pickup/delivery pair와 hard constraint를 보존하는 해를 ALNS로 생성·개선하며,  
선택적으로(별도 승인 시에만) MIP 재조합을 적용한 뒤,  
**독립 verifier 두 단계를 통과한** `ASSIGNED` / `UNASSIGNED` partition과 provenance만 발행한다.

### A2. 입력 계약 → §4 D1 확정 의도 참고

- **단일 고정 입력 계약** (multi-version 스키마 운영 없음)  
- 단위(거리·시간 등)는 version으로 바꾸지 않고 **전역 고정**  
- 고객 차이 = **profile** ± optional 필드  
- 레거시 외부 포맷이 있으면 **adapter 하나**로 canonical 변환 (version 체계와 별개)

### A3. pair 불변조건

1. 배정·mutation 원자 단위는 고객 한 점이 아니라 **`Request`(pair)**  
2. 같은 Request의 pickup·delivery는 **같은 vehicle route**, **pickup 선행**  
3. partial pair / cross-vehicle pair는 품질 문제가 아니라 **구조 결함**  
4. 탐색 중 미배정(`SearchRequestBank`)과 최종 `UNASSIGNED`를 **섞지 않음**

### A4. delivery-only

- CVRPTW형 delivery-only도 **같은 RPDPTW core**에서 처리  
- logical pickup은 pair 소유권에만 참여, **실제 정차·travel·service visit을 만들지 않음**  
- 가짜 depot visit으로 logical pickup을 흉내 내지 않음  

### A5. end-to-end 파이프라인

```text
외부 입력
  → (필요 시) adapter → 단일 고정 canonical 계약
  → 정규화
  → travel 준비
  → profile 결합
  → immutable solve snapshot          ← 문제 정의 동결
  → initial portfolio (≤8)
  → phase-1 screen ALNS
  → phase-2 ALNS
  → [optional gated] HybridPhase (route pool + MIP)
  → candidate solution verifier
  → finalization / audit
  → result-integrity verifier
  → publishable result
```

- snapshot **이후** 탐색은 해(candidate)만 변경. 문제·travel·profile 의미를 탐색이 고치지 않음  
- verifier PASS 전 결과는 발행 권위가 없음  

### A6. 문서 경계

| 문서 | 소유 |
|---|---|
| **Master** | 목표, 범위, 완료 정의, 핵심 결정, e2e 흐름, roadmap/gate, 검증 evidence 개요 |
| **Domain** | 값·수식·정규화·travel·전파·평가·결과의 정확한 의미와 acceptance |
| **Architecture** | Maven module, package, port/SPI, runtime, adapter 배치, dependency 금지선 |

한 문서가 다른 문서의 의미를 단독으로 바꾸지 않는다.

### A7. 완료 / gate

- class 존재, API 응답, 문서 작성 완료, 단일 fixture 점수 향상 ≠ 완료  
- 완료는 **gate + evidence**의 AND  
- 목적별 추가 gate: 일반 발행 / official Win 비교 / application cutover / hybrid 활성화는 서로 다름  

### A8. ALNS-first, C-17 gated

- 기본 구현·benchmark 경로 = **ALNS**  
- route pool / MIP = **C-17 GATED TARGET**  
- 별도 승인·evidence 전 **구현 착수 및 production 기본 활성화 금지**  

### A9. current vs target

- 현재 placeholder(`AlnsBatchEngine` 등)·orchestration demo·구현 문서 세트 작성 완료는  
  **목표 솔버 완료 evidence가 아님**  
- “현재 repo 동작”과 “승인된 목표 계약”을 용어·문서·진행률에서 분리  

### A10. 클라우드 / compute → §4 D2 확정 의도 참고

- object storage + durable orchestration + worker compute의 **논리 역할 분리**는 유지 가능  
- **compute 구현 후보는 Lambda 또는 ECS이며, 둘 중 무엇인지는 미결정**  
- 클라우드 선택 ≠ 알고리즘 완료 ≠ production cutover 승인  
- provider SDK는 adapter/deployment 경계에만  

### A11. 고객 확장

- 공통 route state / propagation / ALNS core에 고객 이름 분기 금지  
- constraint, metric, score, comparator, `SolvePlan` 등 **profile 조합**으로 격리  

### A12. Phase A 산출 범위

- 이번 세션(Phase A) 규범 산출 = **본 인터뷰 정리만**  
- 설계 3종 본문 = Phase B  

---

## 4. 의도 어긋남 / 교정 표 (Phase B 반영 필수)

기존 문서·explainer 표현과 달리, **사용자 의도로 확정된** 항목이다.

| ID | 주제 | 기존 문서·해설 뉘앙스 | **확정 의도 (권위)** | Phase B 반영 위치 |
|---|---|---|---|---|
| **D1** | 입력 versioning | `versioned input`, multi-판 schema/adapter 강조 | **단일 고정 입력 계약**. multi-version 스키마 운영 없음. 단위 전역 고정( version으로 분/초 등 변경 금지). 고객 차이=profile±optional. 레거시=adapter 하나 | Master 목표·입력 절; Domain 입력 계약; Architecture adapter |
| **D2** | Worker/API compute | AWS Lambda를 단정하는 표현 존재 | **Lambda 또는 ECS, 미결정**. 문서에 후보로 쓰고 확정하지 말 것 | Master runtime 개요; Architecture runtime |

### 의도 어긋남이 아닌 것 (오해 정리로 끝)

| 주제 | 정리 |
|---|---|
| `immutable solve snapshot` | 탐색 전 문제·travel·profile 동결. 이해 합의 |
| `독립 verifier` | candidate + result-integrity 2단계, 솔버 cache와 분리. 이해 합의 |
| pair / bank / UNASSIGNED | A3 합의 |
| ALNS-first / C-17 | A8 합의 |

---

## 5. 용어 합의 (Phase A에서 풀어 쓴 것)

| 용어 | 합의된 의미 |
|---|---|
| **단일 고정 입력 계약** | 하나의 canonical 입력 의미·단위. multi-version 병행 운영 없음 |
| **immutable solve snapshot** | 풀이 시작 직전 동결된 문제 정의 묶음(정규화 문제 + travel + profile 등). 이후 탐색은 해만 변경 |
| **독립 verifier** | 솔버 incremental cache를 진실로 쓰지 않는 재검사. ① 해 ② 발행 payload |
| **Request / pair** | pickup+delivery 원자 운송 의무. mutation·feasibility·partition의 단위 |
| **SearchRequestBank** | 탐색 중 미배정 membership. 최종 UNASSIGNED·실패 사유 저장소가 아님 |
| **profile** | 고객·설정별 평가·제약·계획 조립. core의 고객명 분기 대체 |
| **GATED / C-17** | 상세 설계는 가능하나, 명시 승인·evidence 전 구현·기본 활성화 금지 |
| **placeholder / current** | 데모·합성 엔진·미검증 경로. target 완료로 승격 금지 |

---

## 6. Phase B — 새 설계 3종에 넣을 것 / 빼 것

파일명 예 (작성일 prefix):

- `docs/YYYY-MM-DD-master-design.md`  
- `docs/YYYY-MM-DD-domain-design.md`  
- `docs/YYYY-MM-DD-architecture-design.md`  

### 6.1 세 문서 공통

**넣을 것**

- §3 핵심 축 합의 문장  
- §4 의도 교정 D1, D2  
- current vs target 분리  
- ALNS-first, C-17 gated  
- 문서 상호 경계 (A6)  
- “이 문서는 Phase A 인터뷰 정리(`2026-07-30-design-interview-phase-a.md`)를 규범 입력으로 한다” front-matter  

**빼거나 약화할 것**

- multi-version 입력 스키마를 **필수 전제**처럼 쓰는 서술 (D1)  
- compute = Lambda **확정** 서술 (D2)  
- placeholder/현재 코드를 완료 evidence로 읽는 서술  
- Phase A에서 합의하지 않은 **새 수치·새 제품 확정** (round 수, MIP budget, production sizing 등)  

**가져오되 “잠정/상속”으로 표시할 것**

- 기존 C-\* / P-\* / Q-\* / RM-\* 중 Phase A에서 다시 열지 않은 항목  
- 상세 수식·acceptance·module tree — Domain/Architecture 본문에서 기존 REVIEW 문서를 **상속 후보**로 재구성.  
  **D1·D2와 충돌하는 문장만 교정.** 나머지는 “기존 유지(미재심)”로 명시 가능  

### 6.2 Master에 넣을 것 (Phase B 1순위)

- 한 문장 목표, 범위/비범위  
- 완료 AND gate 개요  
- 핵심 결정 등록부 요약 (D1·D2 반영 후)  
- e2e 흐름 (A5)  
- ALNS baseline vs gated hybrid  
- 검증·publication·benchmark 개요  
- roadmap/gate 개요 (세부 phase 문서는 링크만)  
- migration / current vs target  

### 6.3 Domain에 넣을 것 (Master 검수 후)

- Request/pair, delivery-only, bank, propagation, evaluation, result 의미  
- **단일 고정 입력 계약** 필드·단위·정규화 (version multi-track 제거/비범위)  
- acceptance 방향 (기존 상세는 상속·정리)  

### 6.4 Architecture에 넣을 것 (Domain 검수 후)

- module/package DAG, dependency 금지  
- port/adapter, profile 확장 seam  
- local/worker/distributed 논리 runtime  
- compute: **Lambda | ECS 미결정**  
- verifier 격리, optional MIP backend 격리  
- AWS 등은 reference/후보, production 승인 아님  

### 6.5 Phase B에서 하지 말 것

- implementation `phases/*` 재작성  
- 코드 구현  
- 열려 있는 실험 수치 확정 (`Q-BENCH-02` 등)  
- C-17 활성화  

---

## 7. 열린 질문 (Phase A에서 닫지 않음)

| ID | 질문 | 상태 | 비고 |
|---|---|---|---|
| **O1** | Worker/API compute를 Lambda로 할지 ECS로 할지 | **OPEN** | D2. Phase B 문서에는 후보로만 |
| **O2** | 단일 고정 계약의 구체 필드 목록·optional 확장 규칙 문서화 깊이 | OPEN (Domain Phase B) | D1 후속. 의미는 “한 계약”으로 확정 |
| **O3** | 레거시 Win JSON adapter의 공식 이름·범위 | OPEN | “adapter 하나” 원칙만 확정 |
| **O4** | 기존 C-\*/Q-\* 전부의 재승인 여부 | **재승인 안 함**이 Phase A 원칙 | 충돌 시에만 D\*로 승격 |
| **O5** | Phase C 수행 여부·시점 | OPEN | 구 `master-design.md` 부재, SUPERSEDED, README 링크 |
| **O6** | 기존 상세 수식·module tree를 Phase B에서 얼마나 축약할지 | OPEN | B 세션 시작 시 Master 분량부터 결정 |

---

## 8. 기존 문서 지형 (참고, 비권위)

Phase B가 **다시 읽지 않기 위한** 지형도. 충돌 시 §0·§3·§4가 이김.

| 자료 | 관찰 |
|---|---|
| `docs/2026-07-26-master-design.md` | SUPERSEDED 초안. 본문 가치는 있으나 권위 깨짐 |
| `docs/2026-07-26-domain-design.md` | REVIEW 상세 후보 |
| `docs/2026-07-26-architecture-design.md` | REVIEW 상세 후보 |
| `docs/*-explained*.md` | 학습용. 규범 아님. versioned 설명 등은 D1과 충돌 가능 |
| `docs/architecture-design.md` | 별도 장문 REVIEW. dated architecture와 병존 |
| `docs/master-design.md` | 다수 문서 링크 대상이나 **루트에 없음** (deprecated에 역사본) |
| `docs/implementation/*` | 문서 세트 작성 완료, 구현 accepted 0/15, ALNS-first. Phase A 재작성 범위 밖 |
| 코드 | placeholder 성격 경로 존재. target 완료 아님 |

---

## 9. Phase B 시작 체크리스트 (다음 세션용)

1. **입력:** 이 파일만 규범으로 연다. explainer 전체 재독 금지(필요 시 사용자가 지정한 절만).  
2. **출력:** `YYYY-MM-DD-master-design.md` 초안 (한국어).  
3. **필수 반영:** D1, D2, A1–A12, current≠target, ALNS-first, C-17 gated.  
4. **금지:** 새 수치·제품 확정, MIP 기본 활성화, multi-version 입력 체계 재도입.  
5. **검수:** 사용자 승인 후에만 Domain 세션 착수.  

---

## 10. 결정 요약 (한 페이지)

### 프로세스

- Phase A = 합의 패킷(본 문서). Phase B = Master→Domain→Architecture. Phase C = 링크/SUPERSEDED 선택.

### 의도 교정 (문서에 반드시 반영)

1. **D1** 단일 고정 입력 계약. multi-version 없음. 단위 고정.  
2. **D2** compute = Lambda **또는** ECS, **미결정**.  

### 의미 합의 (유지)

- pair 원자성, snapshot 동결, verifier 2단, profile 격리, ALNS-first, C-17 gated, placeholder≠완료.  

### 다음 액션

- [ ] Phase B: Master 재작성 (이 문서만 입력)  
- [ ] 사용자 Master 검수  
- [ ] Domain → Architecture  
- [ ] (선택) Phase C 권위 정리  

---

*문서 끝. Phase A 인터뷰 정리.*
