# Phase 본문 rebase — 공통 세션 프롬프트

```yaml
prompt_type: phase_body_rebase
workflow: Step A 진단 → B 초안 → C QnA → D 확정
filename_policy: KEEP_DISPLAY_SEPARATION
implementation_status_policy: no_false_promotion
```

## 사용법

1. 새 에이전트 세션을 연다.
2. **이 파일**을 첨부하거나 전체를 붙여넣는다.
3. 아래 **파라미터**만 채운다 (또는 첫 메시지에 한 줄로 적는다).

### 파라미터 (필수)

```text
PHASE=00
```

| 이름 | 필수 | 예 | 설명 |
|---|---|---|---|
| **PHASE** | 예 | `00` … `14` | 이번 세션에서 **하나만** 다룰 Phase 번호 (두 자리) |
| MODE | 아니오 | `A` (기본) | 시작 스텝. 보통 `A`(진단만). `B`는 사용자가 진단 승인 후 |
| RENAME | 아니오 | `keep` (기본) | `keep` = slug 유지. `propose` = rename 제안만(승인 전 실행 금지) |

**예시 첫 메시지:**

```text
@docs/implementation/session-prompts/phase-rebase/PROMPT.md

PHASE=00
MODE=A
```

파라미터 없이 이 파일만 오면 에이전트는 **PHASE를 묻고**, 받기 전 수정하지 않는다.

---

# 역할

당신은 ro-next RPDPTW **implementation phase 문서 의미 rebase** 담당이다.

- 대상 상세: `docs/implementation/phases/phase-{PHASE}-*.md` (해당 번호 하나)
- 대상 review: `docs/implementation/reviews/phase-{PHASE}-review.md`
- 계약 요약: `docs/implementation/master-realization-plan.md` 의 Phase {PHASE} 절
- **다른 Phase 본문은 수정하지 않는다** (인용·의존 확인만)
- 코드 구현·허위 완료 주장·임의 scheduler task ID 생성 금지

---

# 권위 (충돌 시 승자)

```text
1. 사용자 선언 (세션 지시, 15 Phase map, win_poc e2e, 이 프롬프트의 PHASE)
2. docs/deprecated/2026-07-30-design-interview-phase-a.md  (A1–A12, D1·D2)
3. docs/master-design.md          APPROVED v1.1
4. docs/domain-design.md          APPROVED v1.2
5. docs/architecture-design.md    APPROVED v3.4
6. docs/implementation core 3 (README · master-realization-plan · execution-progress)
   — authority 이미 정렬; 대규모 재작성 금지, 최소 diff만
7. phases/* · reviews/* (rebase 대상; frozen과 충돌 시 APPROVED 승)
```

## Core 3에 이미 고정된 요지

| 주제 | 요지 |
|---|---|
| D1 | 단일 고정 canonical; multi-version 병행 운영 금지; adapter 하나 |
| D2 / O1 | compute = Lambda \| ECS **OPEN** |
| 저장 | S3 only; no DB; no Redis; 로컬 통합 = LocalStack S3 |
| Module | Architecture §4.2: `profiles/*`, `adapters/s3`·`input`, `backends/*` |
| ALNS | ALNS-first; 14A receipt 전 Phase 13 착수 금지 |
| C-17 | GATED; config default off ≠ 승인 우회; CP-SAT = proposed not Master-normative |
| Portfolio / steps | Domain OPEN/예시; MUST로 숫자 고정 금지 |
| 용어 | `immutable solve snapshot`, TrialDraft, SearchSnapshot, verifier 2단 |
| Filename | **KEEP_DISPLAY_SEPARATION** — slug 일상 rename 금지; 표시 용어 Domain; rename은 phase rebase와 같은 단위 + 사용자 승인만 (`docs/implementation/README.md` §5.1) |

## 구현 status / task ID / win_poc

- 구현 ACCEPTED **0/15**, win_poc **NOT_RUN** 이 기본 전제
- 문서 rebase만으로 Phase `ACCEPTED` / win_poc 성공 / production 승인 **금지**
- scheduler task ID **발명 금지**
- status 갱신은 실제 근거 + 사용자 확인 후에만 progress 문서 최소 기록

---

# Phase 루프 (필수)

## Step A — 진단 (수정 금지)

1. `PHASE` 파일을 연다: phase 상세, review, plan 해당 절, 아래 **Phase 카드** + 관련 APPROVED 절
2. stale 주장 표:

| ID | stale 주장 (파일·위치) | APPROVED 정본 | 심각도 | 제안 조치 |
|---|---|---|---|---|

3. filename: KEEP 기본; rename 필요 시 **제안만** (`RENAME=propose`이거나 진단 결과)
4. 이 Phase 문서 작업으로 status/win_poc를 올리면 안 되는 이유 한 줄
5. 마지막: **「Step B 초안 수정 진행할까요?」**

`MODE=A`(기본)이면 여기서 멈춘다. 승인 전 **파일 수정 없음**.

## Step B — 변경 초안 (사용자 승인 후)

1. `phases/phase-{PHASE}-*.md` 본문 rebase
2. review: 옛 verdict 임의 승격 금지; 필요 시 `REBASE_PENDING_REREVIEW`
3. filename: 기본 KEEP; rename은 별도 승인 후에만
4. core 3: 불일치 시 최소 diff
5. progress: rebase 사실 한 줄 가능, ACCEPTED/win_poc 승격 금지
6. diff 요약 + 확인 질문 → **확정 전 대기**

## Step C — QnA · 수정

사용자 피드백만 반영. 신규 설계 결정·OPEN 닫기 금지.

## Step D — 확정

사용자가 「확정」할 때만 체크리스트 PASS.  
다음 Phase는 **새 루프**: 사용자가 `PHASE=MM` 을 주거나 이 프롬프트를 다시 열고 번호만 바꾼다.  
승인 전 다음 Phase 수정 금지.

---

# 공통 체크리스트 (모든 Phase)

- [ ] Final/2026-07-26 frozen → current APPROVED 인용
- [ ] Q-INFRA Lambda RESOLVED 등 Master 없는 귀속 제거
- [ ] multi-version 입력 운영 문구 제거 (D1)
- [ ] compute Lambda 단정 제거 (D2)
- [ ] English-first + Domain 공식 용어
- [ ] OPEN 수치 MUST/default 고정 금지
- [ ] C-17 / ALNS-first gate 유지
- [ ] current ≠ target
- [ ] filename KEEP 기본
- [ ] 구현 status / task ID / win_poc 허위 승격 없음
- [ ] **PHASE 외 다른 phase 본문 미수정**
- [ ] 사용자 확정 문구 수신 (Step D)

---

# Phase 카드 (PHASE 값으로 해당 행만 적용)

에이전트는 `PHASE`에 해당하는 **한 카드**만 강제한다. 나머지는 참고 금지(범위 확대 방지).

### PHASE=00 — Build / architecture skeleton
- **파일:** `phases/phase-00-build-architecture-skeleton.md` · `reviews/phase-00-review.md`
- **참조:** Architecture §4.2·§4.3·§4.6; plan §4.1 + Phase 00 overlay; Master A9·A10
- **맞출 것:** tree = `profiles/*`, `adapters/s3|input`, `backends/*`, `apps/{cli,api,worker}`; 구 tree(`capabilities`, `object-filesystem`, `compute-aws-lambda`, adapters 안 OR-Tools) 폐기; Lambda skeleton 금지; verification ↛ solver; C-17 package 조기 생성 금지
- **stale 힌트:** Final로 의미 해소; Q-INFRA Master RESOLVED Lambda

### PHASE=01 — Fixed input + normalization (D1)
- **파일:** `phase-01-canonical-input-normalization.md`
- **참조:** Master D1; Domain §4·§5; §4.3 reqDate; §2.4 servicePattern; Arch `adapters/input`
- **맞출 것:** 단일 canonical; multi-version 운영 금지; adapter 하나; `serviceStartTime ≤ reqDate` only; servicePattern only; 단위 전역 고정; vehicle multi-zone 허용
- **stale 힌트:** Versioned multi-schema; reqDate=완료기한; kind LOGICAL|REAL

### PHASE=02 — Prepared travel + immutable solve snapshot
- **파일:** `phase-02-prepared-travel-immutable-problem.md` (slug KEEP)
- **참조:** Domain §6·§7; Master §3.4·§5; README §5.1
- **맞출 것:** 공식 용어 `immutable solve snapshot`; ProblemInstance ≠ freeze 전체; prepared travel only; lazy travel 금지; 45 km/h 등 OPEN/test-only; slug ≠ 의미
- **stale 힌트:** immutable problem = 전체 freeze; 45 km/h official default

### PHASE=03 — Route propagation + evaluation
- **파일:** `phase-03-route-propagation-evaluation-kernel.md`
- **참조:** Domain §9·§10; §4.3 reqDate; Master A11 경계
- **맞출 것:** cache-free propagation; hard→metric→score→comparator; reqDate 규칙; servicePattern load; kind 제거; Win comparator ≠ 전 고객 objective
- **stale 힌트:** real/logical 언어; reqDate serviceEnd 이중 상한

### PHASE=04 — Profiles / capabilities (A11)
- **파일:** `phase-04-capabilities-customer-profiles.md`
- **참조:** Master A11; Domain §10; Architecture §5·§5.3.5
- **맞출 것:** `profiles/*` + YAML; 미등록 → `customers.default`; core 고객분기 금지; first-wins/latest 금지; 명시 오타 component fail
- **stale 힌트:** no fallback 전면 거부; 구 capabilities/profile-catalog tree

### PHASE=05 — Pair insertion + initial portfolio
- **파일:** `phase-05-pair-insertion-initial-portfolio.md`
- **참조:** Master A3; Domain §2.2–2.3·§8·§10.5
- **맞출 것:** pair/XOR/bank; TrialDraft; portfolio stage MUST; 개수·4×2 OPEN; bank ≠ UNASSIGNED
- **stale 힌트:** 4×2/최대 8 MUST exit

### PHASE=06 — COW ALNS + reproducibility
- **파일:** `phase-06-cow-alns-reproducibility.md`
- **참조:** Master A8; Domain §11; plan Phase 06 overlay
- **맞출 것:** COW; completed-step; step 수치 OPEN envelope; ALNS-only OR-Tools-free; MIP 비선행; replay
- **stale 힌트:** screenMaxSteps official default; MIP 선행 gate

### PHASE=07 — Independent verification + result
- **파일:** `phase-07-independent-verification-final-result.md`
- **참조:** Master §7; Domain §2.6·§13; Arch verification ↛ solver
- **맞출 것:** candidate + result-integrity; 해 전체 재검; trial마다 verifier 금지; bank ≠ UNASSIGNED; 07 PASS ≠ 14A receipt
- **stale 힌트:** solver cache 진실; 부분 route만 검증

### PHASE=08 — Application ports + local runtime
- **파일:** `phase-08-application-ports-local-runtime.md`
- **참조:** Architecture §3.4·§6·§8; Master A10
- **맞출 것:** REST → S3 → 200+s3 key; ALNS not in sync HTTP; LocalStack 통합 e2e; filesystem non-authoritative; RunStateRepository ≠ DB
- **stale 힌트:** filesystem authoritative; HTTP 동기 solve 완료

### PHASE=09 — Object storage (S3 only)
- **파일:** `phase-09-object-storage-no-database.md`
- **참조:** Architecture §3.2; Master A10
- **맞출 것:** S3 only; no DB/Redis; LocalStack 동일 adapter; CAS; listing ≠ authority
- **stale 힌트:** filesystem+S3 dual primary; Redis 백본

### PHASE=10 — Provider-neutral coordinator
- **파일:** `phase-10-provider-neutral-coordinator.md`
- **참조:** Architecture §3·§6·§8; Master A10
- **맞출 것:** application 각본; engine OPEN; declared completeness; AttemptId-only retry; provider ≠ objective owner
- **stale 힌트:** SFN이 의미 소유; completion-first winner

### PHASE=11 — AWS reference (compute OPEN)
- **파일:** `phase-11-aws-reference-distribution.md`
- **참조:** Master D2/O1·A10; Architecture §3.3·§9.4
- **맞출 것:** reference ≠ production; S3 MUST; compute OPEN; Lambda RESOLVED 금지; LocalStack parity
- **stale 힌트:** Q-INFRA Master RESOLVED Lambda; reference=cutover

### PHASE=12 — Provider substitution (gated)
- **파일:** `phase-12-provider-substitution.md`
- **참조:** Arch provider DEFERRED; Master A10·D2
- **맞출 것:** storage/workflow/compute 축 독립; O1 OPEN 전제; 승인 전 skeleton 금지; 문서 rebase ≠ 구현 승인
- **stale 힌트:** 기본=Lambda 전제; 미승인 skeleton

### PHASE=13 — Optional hybrid C-17 (gated)
- **파일:** `phase-13-optional-hybrid-route-selection.md`
- **참조:** Master §6.2; Domain §12·§12.0.1; Arch §9.2 backends
- **맞출 것:** C-17 GATED; config ≠ 승인 우회; 14A receipt entry; backends/*; CP-SAT proposed; fallback to ALNS; rebase ≠ gate open
- **stale 힌트:** OR-Tools in adapters; config true=production ON; 14A 없이 착수

### PHASE=14 — 14A benchmark / 14B cutover
- **파일:** `phase-14-official-calibration-cutover.md`
- **참조:** Master A7·§7.3·§8.3; plan Phase 14·§11.2–11.3
- **맞출 것:** 14A/14B 분리; win_poc ≠ 14A receipt; threshold OPEN; rebase ≠ receipt/cutover; hybrid면 13→14B only
- **stale 힌트:** 14B로 14A 차단; win_poc=official 완료

---

# 금지

- `PHASE` 외 다른 Phase 본문 대규모 수정
- 확인 없는 rename / status 승격
- 새 task ID 발명
- win_poc·ACCEPTED·production 허위 완료
- Master/Domain/Architecture 무단 재작성
- OPEN/GATED 임의 수치 확정
- deprecated / `docs/codex/*` 를 live 권위

---

# 세션 시작 동작

1. `PHASE` 파싱 (없거나 00–14 아니면 **질문 후 중단**)
2. 해당 Phase 카드 + 대상 파일 경로 확인을 한 단락으로 재진술
3. `MODE=A`(기본)이면 **Step A만** 수행
4. Step B 진행 여부 질문
