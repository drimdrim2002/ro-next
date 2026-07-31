---
title: Architecture 문서 구조 재정비 핸드오프
status: READY
date: 2026-07-31
purpose: >
  수시 수정으로 비대해진 docs/2026-07-31-architecture-design.md 를
  기존 합의·내용을 보존한 채 중복 제거·구조 재정비할 때 사용한다.
work_document: docs/2026-07-31-architecture-design.md
do_not_restart: architecture full rewrite with new design decisions
---

# 역할

설계 문서 정리 에이전트. **구현·코드 변경·커밋(요청 전) 금지.**  
작업 대상은 **`docs/2026-07-31-architecture-design.md` 만.**  
Master/Domain 의미·본문 재작성 금지. **새 기능·새 합의 추가 금지.**

목표: 수시 수정으로 **비대해지고 중복·구조가 흐려진 Architecture 문서를, 기존 합의·내용을 보존한 채 재정비**한다.

---

# 규범 입력 (충돌 시 이 순서)

1. `docs/2026-07-30-design-interview-phase-a.md`
2. `docs/2026-07-30-master-design.md` (**APPROVED**)
3. `docs/2026-07-31-domain-design.md` (**APPROVED**)
4. 작업 문서 본문에 이미 적힌 **검수 합의** (아래 MUST 표)

상속 후보: `docs/2026-07-26-architecture-design.md` — 참고만. 위와 충돌 시 채택 금지.

---

# 작업 문서

| 항목 | 값 |
|---|---|
| 파일 | **`docs/2026-07-31-architecture-design.md`** |
| status | **REVIEW** (정리 후에도 REVIEW 유지, 사용자 승인 전 APPROVED 로 올리지 말 것) |
| 최근 버전 | front-matter `version` 확인 후, 정리 완료 시 **patch/minor 한 단계 상향** + status 한 줄에 “구조 재정비” 명시 |

---

# MUST 유지 (정리 과정에서 삭제·번복 금지)

| 주제 | 합의 |
|---|---|
| **D1** | 단일 canonical. multi-version 입력 없음. input adapter 하나. 단위 전역 고정 |
| **D2 / O1** | compute = Lambda **또는** ECS, **미결정**. 한쪽 단정 금지 |
| **저장** | **DB 없음. Redis 포기. S3 only** (결과·중간·artifact·run·접수 input) |
| **로컬** | **LocalStack S3**. 단위 테스트 인메모리 fake 허용 |
| **orchestration** | 각본=`rpdptw-application`+port / 엔진=OPEN / 바이트=S3 |
| **A6 §3** | 논리 runtime **의미**는 Master §5.2. Arch §3은 **배치만** (철학 장문 금지) |
| **C-17 / hybrid** | ALNS-first. hybrid optional. config **default off**. true ≠ 승인 우회. OR-Tools는 **`backends/*`** (adapters 아님) |
| **A9** | placeholder / LocalStack 기동 ≠ 솔버 완료 evidence |
| **A11 / 고객 확장** | profile SPI. core 고객명 분기 금지. **customerId → YAML → BoundProfile**. 미등록 고객·슬롯 공백 → **`customers.default` 자동 merge**. 잘못된 **명시** component 는 fail |
| **YAML** | 형식 **YAML MUST**. `customer-binding.yaml` + `component-catalog.yaml`. 수식 본문·시크릿 YAML 금지 |
| **식별자 인지 부하** | 실행상 필수는 **customerId + BoundProfile**. RequirementId/component id/ProfileKey는 YAML 필드로 취급 (5종 동급 식별자 체계로 다시 부풀리지 말 것) |
| **모듈** | verification ↛ solver. S3 SDK는 adapters. application에 cloud/OR-Tools SDK 금지 |
| **배포** | Maven 모듈 ≠ Lambda/zip 개수. 배포 조립은 apps/* |
| **REST 제공** | **이미 공유된 REST/input 스펙**을 따름 (이 문서가 wire 사전 재작성 안 함). 흐름: 규약 호출 → validation → 문제 없으면 S3에 input 저장(key = plan id + customer id 등 조합) → **HTTP 200 + s3 key**. 호출 측이 s3 key로 진행 확인. **같은 HTTP 요청에서 ALNS 전체 완료 아님**. 탐색·검증은 이후 worker 등 |
| **스타일** | 한국어 본문. 주요 용어 `English (쉬운 한국어)`. 발표·온보딩 가능하도록 **풀어 설명**. 축약 기호 나열만으로 끝내지 말 것 |

---

# 현재 문서 대략 구조 (정리 전 — 읽을 때 참고)

1. 문서 지위·용어  
2. 목표·범위·비범위  
3. 런타임 → module/port 배치 (짧게)  
4. Module DAG · 금지선 (4.1 의미+DAG → 4.2 tree → …)  
5. 고객·업종 확장 (YAML 중심)  
6. Port / SPI / adapter (REST 접수 포함, 쉽게)  
7. Domain → 모듈 매핑  
8. local / worker / distributed  
9. profile seam 한 줄  
10. optional MIP/hybrid  
11. AWS · LocalStack  
12. current vs target · migration  
13. OPEN / DEFERRED · traceability  
14. 검수 요청  
15. 다음 액션  

§4–§6이 검수 과정에서 **가장 많이 늘어남**. 중복·교차 반복이 여기 집중.

---

# 이번 세션 목표: **내용 보존 + 구조 재정비** (신규 설계 금지)

## 할 일

1. 문서 **전체**를 읽고, front-matter · §1–§16(또는 현재 장 번호) 목차를 파악한다.  
2. **중복 맵**을 짧게 만든다 (예: S3 only가 §2/§3/§4/§6에 반복 → 규범 한 곳 + 나머지 1줄 링크).  
3. 장을 **읽기 순서**로 재배치·통합할 수 있다. 권장 골격(필요 시 조정 가능):

   | 권장 장 | 역할 |
   |---|---|
   | 1 | 지위·권위·용어·current≠target |
   | 2 | 목표·범위·비범위 (REST 접수 한 줄 포함) |
   | 3 | 저장·로컬·런타임 배치 (짧게) + **REST 접수→S3 key→이후 worker** 타임라인 |
   | 4 | 모듈 의미·DAG·tree·금지선·배포 단위·build |
   | 5 | 고객 확장: 원칙 · customerId/BoundProfile · YAML 스키마 · default fallback · SPI · 시나리오 요약 |
   | 6 | port / SPI / adapter 용어 + 창구 목록 + REST↔port 매핑 (스토리 짧게) |
   | 7 | Domain→모듈 매핑 표 |
   | 8 | local/worker/distributed |
   | 9 | (선택) profile/hybrid/verifier/AWS를 **짧은 절**로 통합하거나 8 뒤에 압축 |
   | 끝 | OPEN/DEFERRED · 검수 · 다음 액션 · 합의 체크리스트 |

4. 각 장 머리: **이 장이 하는 일 / 안 하는 일** 2–4줄.  
5. 상세는 **한 곳**에만 두고, 다른 장은 `→ §x` 로 보낸다.  
6. 용어 **port / SPI / adapter** 정의는 **한 절에만** 전문 정의. 다른 곳은 한 줄+링크.  
7. YAML 필드 표·default fallback·REST 200 흐름은 **삭제하지 말고** 중복만 제거.  
8. version bump + status 문구 갱신.  
9. 정리 후 **목차 + 합의 보존 체크리스트**를 사용자에게 보고.

## 하지 말 것

- 새 모듈·새 port·새 REST 필드·새 저장소 도입  
- Lambda/ECS 확정, C-17 기본 ON, Redis/DB 재도입  
- Domain/Master 본문 수정  
- “더 멋지게” 하려다 합의 문장 삭제  
- 코드·implementation phases 작성  
- 커밋 (사용자 요청 시에만)  
- explainer 수준 장문 철학 부활 (§3 정책 유지)

## 품질 기준

- 같은 규범이 **3곳 이상 장문으로 반복**되면 안 됨 (1 규범 + 링크)  
- §4와 §6이 서로 모듈 트리를 이중 장문으로 설명하지 말 것  
- §5와 §6이 SPI 정의를 이중 장문으로 하지 말 것  
- 초보 독자가 **REST 접수 → s3 key → worker** 와 **모듈 DAG** 를 각각 한 번에 찾을 수 있을 것  
- 한국어 + `English (쉬운 한국어)` 유지  

---

# 진행 방식

1. 첫 응답: 문서 version/status 확인 + “재정비 시작. 신규 합의 없음” 한 줄.  
2. 읽기 → 중복/구조 이슈 목록 (우선순위) → **사용자 확인 없이 진행해도 됨** (사용자가 이 프롬프트로 위임). 다만 **합의 삭제·의미 변경**이 필요해 보이면 멈추고 질문.  
3. 파일을 직접 편집해 재정비.  
4. 완료 보고:  
   - 전/후 목차  
   - 합친·줄인 중복 목록  
   - MUST 표 항목별 “문서 어디에 남았는지”  
   - 의도적으로 남긴 짧은 반복 (있다면)  
5. 커밋은 사용자 요청 시만.

---

# 시작 체크리스트

- [ ] front-matter (storage, local, section_3_policy, version)  
- [ ] MUST 표와 본문 충돌 없는지  
- [ ] REST 접수 흐름이 한 곳에 명확한지  
- [ ] YAML default fallback 이 한 곳에 명확한지  
- [ ] verification ↛ solver, backends vs adapters 가 §4에 남아 있는지  
- [ ] 사용자 보고용 목차·체크리스트 작성  

**지금 `docs/2026-07-31-architecture-design.md` 를 읽고 구조 재정비를 시작하라.**

---

## 새 세션 붙여넣기 한 줄 예시

```text
@docs/2026-07-31-architecture-reorg-handoff.md
@docs/2026-07-31-architecture-design.md
@docs/2026-07-30-master-design.md
@docs/2026-07-31-domain-design.md
위 핸드오프대로 Architecture 문서 내용 보존 + 중복 제거 + 구조 재정비만 진행.
신규 설계·합의 변경·커밋 금지.
```

---

*핸드오프 끝. 구조 재정비 전용.*
