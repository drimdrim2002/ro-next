# 세션 27 — Master Design review findings correction

```yaml
status: REVIEW
version: 1.0-review
last_updated: 2026-07-23
owner: Master Design correction reviewer
scope: 세션 26의 S26-H-001, S26-M-001, S26-L-001, S26-L-002만 수정하고 검증한 기록
supersedes: null
related_decisions: [C-02, C-15, C-21]
```

## 1. 목적, 입력과 편집 경계

이 세션은 [세션 26 review](26-master-review.md)의 네 finding만 수정한다. Master와 질문 등록부는 계속 `REVIEW`이며 이 문서는 승인, 구현 완료 또는 independent re-review 완료를 주장하지 않는다.

수정 전에 다음 문서를 끝까지 읽었다.

- [Master Design](../master-design.md)
- [Master Design open questions](../master-design-open-questions.md)
- [문서 인덱스](../README.md)
- [세션 26 review](26-master-review.md)
- [세션 19 integration plan](19-integration-plan.md)
- [세션 23 result/benchmark draft](23-result-benchmark-draft.md)
- [세션 18 governance](18-document-governance.md)
- [세션 19–26 scheduler log](19-26-scheduler-log.md)

편집은 다음 네 파일에만 한정했다.

- `docs/master-design.md`
- `docs/master-design-open-questions.md`
- `docs/README.md`
- `docs/master-design-sessions/27-review-corrections.md`

코드, 테스트, build/deploy, `data/**`, PDF, fixture, 세션 01–26, scheduler log와 다른 문서는 수정하지 않았다. 구체 infrastructure topology는 계속 deferred이고 Master는 cloud/product-neutral logical responsibility만 소유한다.

## 2. Finding별 correction

### 2.1 `S26-H-001` — 두 verifier gate의 입력·책임·순서

**변경 위치**

- Master §2.2와 §2.4: 두 gate가 현재 설계 범위와 성공 기준임을 명시
- Master §4.1 lines 165–183: end-to-end 순서
- Master §4.2 lines 189–199: finalization과 status/artifact 책임 및 publication rejection
- Master §10.2 lines 462–483: candidate verification, finalization과 result-integrity verification의 handoff
- Master §13.1: recovery candidate에도 같은 두 gate 적용
- Master §14.1 lines 645–694: verifier별 권위 입력, cache-free 검증 책임과 실패 정책
- Master `RM-5` line 753: 두 gate의 구현 산출물과 exit evidence

**수정 전 문제**

§10.2는 candidate verification 뒤에 outcome/diagnostic 생성과 result-integrity verification이 온다고 했지만, §14.1은 candidate 입력만 열거한 단일 verifier에 아직 존재하지 않는 final outcome, diagnostic confidence, summary와 payload까지 검증하도록 맡겼다. 이 충돌은 candidate verifier를 finalization과 결합하거나 publishable payload 검증을 생략하게 만들 수 있었다.

**수정 후 계약**

정확한 순서는 다음으로 하나로 고정했다.

```text
committed candidate
→ candidate solution verifier
→ final request partition/status/diagnostics
→ post-finalization result-integrity verifier
→ publication
```

Candidate solution verifier는 immutable problem/profile declaration, candidate route/node order, bank와 authoritative normalized directed matrix만 권위 입력으로 받는다. Cache 없이 identity, pair/partition, terminal/service pattern, compatibility, travel, capacity/time/resource, hard constraints와 neutral metrics/score/objective를 검증한다.

Post-finalization result-integrity verifier는 첫 verifier의 `PASS` report와 verified solution, final outcomes, disposition references, diagnostic source/audit evidence, summary와 publishable payload를 권위 입력으로 받는다. Outcome partition, status/reference 일치, confidence ceiling, outcome-derived summary와 payload fingerprint를 cache 없이 검증한다.

두 gate 모두 search cache와 solver summary를 권위 입력으로 거부한다. Bounded search telemetry는 승인된 diagnostic source 범위에서만 evidence가 될 수 있고 confidence를 과장할 수 없다. 어느 gate라도 `FAIL`이거나 미완료이면 정상 route/outcome payload와 benchmark vector publication을 거부한다.

**검증 결과**

- PASS — §4, §10.2와 §14.1의 흐름 문자열이 동일한 순서를 가진다.
- PASS — §14.1의 두 verifier가 서로 다른 완전한 입력 목록을 가진다.
- PASS — candidate gate의 여덟 검증 범주와 post-finalization gate의 partition/confidence/summary/fingerprint 검증이 분리되었다.
- PASS — §4.2, §10.2, §14.1과 `RM-5`가 모두 cache/solver-summary 비권위성과 gate별 publication rejection을 요구한다.
- PASS — candidate failure를 정상 unassignment diagnostic으로 바꾸거나 result-integrity failure를 candidate `PASS`로 덮는 경로가 금지되었다.

**의도적으로 바꾸지 않은 범위**

`C-21`의 candidate 독립 검증 강도, `C-15`의 search/result 분리, `Q-RES-01`의 disposition 최소 정보, `Q-RES-02`의 exhaustive audit 범위와 diagnostic confidence 경계는 바꾸지 않았다. 고객사 정책, 외부 schema, 숫자·시간·matrix 의미와 benchmark 수치는 새로 정하지 않았다.

### 2.2 `S26-M-001` — `REVIEW`와 `APPROVED` authority

**변경 위치**

- Master §1 lines 34–44
- README lines 3–10과 27–38

**수정 전 문제**

Master가 `REVIEW`라고 선언하면서도 충돌 시 자기 자신을 승인된 상세 설계보다 앞세우는 문장이 있어, review proposal이 이미 승인된 기준을 대체할 수 있는 것처럼 읽혔다. 채택된 외부 I/O 계약과 Master 승인 전의 승인된 Decision Record 지위도 명확하지 않았다.

**수정 후 계약**

- `REVIEW` 동안 Master는 conflict resolver가 아닌 review proposal이다.
- 승인된 기준과 충돌하면 finding, 중앙 질문 또는 Decision Record의 incorporation/approval 절차를 사용한다.
- 승인 후 hierarchy는 `채택된 외부 I/O 계약과 승인 Decision Record → APPROVED Master → APPROVED 상세 설계 → review input/session → research/original`이다.
- 최신 사용자 결정은 반영 전에도 적용 범위를 명시한 override지만 관련 문서를 자동 승인하지 않는다. 영향 문서와 필요한 질문/Decision Record에 반영한 뒤 review/approval 절차를 완료해야 한다.

**검증 결과**

- PASS — review-time non-authority와 post-approval hierarchy가 별도 문단으로 분리되었다.
- PASS — 외부 I/O 계약, 승인 Decision Record와 최신 사용자 override의 incorporation 절차가 모두 명시되었다.
- PASS — README가 같은 상태 해석을 entrypoint에서 반복 확인한다.

**의도적으로 바꾸지 않은 범위**

Master와 질문 등록부의 상태를 `APPROVED`로 올리지 않았고 새 Decision Record를 만들지 않았다. 미결정 질문의 답, owner 또는 승인자를 추정하지 않았다.

### 2.3 `S26-L-001` — README entrypoint와 비규범 자료

**변경 위치**

- README lines 1–10: Master와 질문 등록부 entrypoint 및 각각의 `REVIEW` 상태
- README lines 12–25: 권장 읽기 순서와 review evidence 경로
- README lines 27–38: session/arranged/orgin의 비규범 지위
- README lines 40–60: arranged 자료 지도와 GCP 자료의 역사적/deferred 지위

**수정 전 문제**

README는 CVRPTW arranged 자료만 안내하고 새 Master, 중앙 질문 등록부와 상태별 authority를 보여 주지 않았다. Provider-specific GCP 자료가 committed target topology처럼 오해될 수 있었다.

**수정 후 계약**

Master와 질문 등록부를 첫 두 entrypoint로 두고 둘 다 `REVIEW`라고 표시했다. 읽기 순서는 `README → Master → 질문 등록부 → 관련 session → arranged → orgin`으로 정리했다. `master-design-sessions`, `arranged`, `orgin`은 비규범 evidence이며 GCP 자료는 historical/current-state evidence 또는 `Q-INFRA-01`의 deferred infrastructure input이라고 명시했다.

**검증 결과**

- PASS — README의 모든 local file/directory link가 존재한다.
- PASS — README의 Master §1 anchor가 resolve된다.
- PASS — 특정 provider/product/runtime/service/deployment unit을 목표 topology로 승인하는 문장이 없다.

**의도적으로 바꾸지 않은 범위**

Arranged/original 자료를 이동·삭제·개정하지 않았고 물리 infrastructure, deployment design 또는 provider 선택을 만들지 않았다. 기존 repository path `orgin`의 철자도 바꾸지 않았다.

### 2.4 `S26-L-002` — 질문 등록부 metadata와 상태 변경 절차

**변경 위치**

- Question registry metadata lines 3–12
- Question registry usage rules lines 18–23

**수정 전 문제**

질문 등록부 metadata에 governance 공통 필드인 `supersedes`와 `related_decisions`가 없었고, 향후 상태 변경이 승인 기록과 연결되어야 한다는 절차가 명시되지 않았다.

**수정 후 계약**

`supersedes: null`과 `related_decisions: []`를 추가했다. 아직 승인 기록이 없으므로 ID를 발명하지 않았다. 향후 질문 상태 변경은 승인 기록을 링크하고 `related_decisions`와 영향 문서를 갱신한 뒤 incorporation/approval 절차를 완료해야 한다.

**검증 결과**

- PASS — governance의 공통 metadata 이름과 형태가 일치한다.
- PASS — 28개 question row는 byte-for-byte 동일하게 보존되었다.
- PASS — ID set과 상태 합계는 `28 = 26 OPEN + 2 DEFERRED`로 동일하다.

**의도적으로 바꾸지 않은 범위**

질문 ID, 질문의 정확한 문장, 상태, earliest blocked gate, 결정 전 안전 동작, evidence/owner boundary와 Master 반영 절은 바꾸지 않았다. 어떤 질문도 답하거나 닫지 않았다.

## 3. 기계적 검증

### 3.1 질문 보존 proof

수정 전 질문 등록부 전체 SHA-256은 `aedb8b77a4d597a29592f47df258648e4d33d41d5c9a435bb98091e4f447fd33`이었다. 수정 전에 각 question table row를 파싱하여 `ID + TAB + exact question text`를 순서대로 연결한 SHA-256과 전체 question row SHA-256을 별도로 기록했다.

| 검증값 | 수정 전 | 수정 후 | 결과 |
|---|---|---|---|
| Question 수 | 28 | 28 | PASS |
| 상태 | 26 `OPEN`, 2 `DEFERRED` | 26 `OPEN`, 2 `DEFERRED` | PASS |
| ID 순서와 집합 | 세션 19의 28개 | 동일 | PASS |
| `ID + exact question text` SHA-256 | `649e7a3a0c25c5ecc198294792bf225a79f263ca13cfcadec3848767db22eada` | 동일 | PASS |
| 전체 question row SHA-256 | `e9afbaf1ed6338d3fb1ea6437c666c5f605848a727433b74ba7c4f05fbf4508f` | 동일 | PASS |

전체 파일 checksum은 metadata/rule 추가 때문에 의도적으로 달라진다. 질문 보존 판정에는 수정 전후 동일한 parser와 row/text hash를 사용했다.

### 3.2 Decision, provisional과 question traceability

동일한 ID extractor로 수정 후 Master와 등록부를 검사했다.

| 집합 | 기대 | 결과 |
|---|---:|---|
| Master `C-*` unique | `C-01`–`C-22`, 22개 | PASS |
| Master `P-*` unique | `P-01`–`P-14`, 14개 | PASS |
| Master `Q-*` unique | 세션 19의 28개 | PASS |
| Registry `Q-*` unique | 세션 19의 28개 | PASS |
| Master/registry Q set equality | 동일 | PASS |

### 3.3 Link, anchor와 Markdown structure

수정된 네 문서의 Markdown local links를 source-relative path와 target heading/explicit anchor로 검사했다.

| 검사 | 결과 |
|---|---|
| local file/directory link와 anchor | 106개 검사, 오류 0 |
| fenced code block | Master 34, registry 2, README 2, session report 4; 모두 balanced |
| trailing whitespace | 네 파일 모두 0 |

### 3.4 핵심 의미 보존

다음 Master evidence가 correction 뒤에도 그대로 존재함을 line/section 검색으로 확인했다.

| 의미 | evidence | 결과 |
|---|---|---|
| RPDPTW 표준 모델 | §2.1 line 50, `C-01` | PASS |
| vehicle-size `Feature`와 capability 분리 | §3.1 lines 103–104, §5.3 | PASS |
| customer-flexible policy 구조 | §2.1 line 52, §9.1–§9.3 | PASS |
| step 정상 종료 + watchdog | §13.1 line 608, `C-08` | PASS |
| authoritative directed input matrix와 physical location | §8 lines 366–386, `C-13` | PASS |
| 네 policy initial portfolio | §11.2 lines 516–530, `C-16` | PASS |
| route pool/MIP deferred | §11.4 line 557, §16.3 | PASS |
| infrastructure deferred, logical ports only | §4.2 lines 203–205, §16.3 | PASS |

RPDPTW 의미, numeric/time/input matrix policy, customer extension seam, algorithm scope 또는 deferred resume criteria에 새 결정을 추가하지 않았다.

### 3.5 Dirty workspace와 edit-boundary attribution

수정 전 `git status --short --untracked-files=all`은 이미 다음 상태를 포함했다.

- tracked root 문서 삭제 1건
- `data/`의 PDF/fixture
- `docs/domain-design.md`
- Master, question registry, session 01–26와 scheduler logs
- `docs/orgin/...`

따라서 clean-worktree 가정 대신 허용 네 경로를 제외한 전체 파일의 `path + content SHA-256` manifest를 정렬하여 aggregate SHA-256을 비교했다.

| 시점/범위 | 파일 수 | aggregate SHA-256 |
|---|---:|---|
| 수정 전, 허용 네 경로 제외 | 25,288 | `ae8be532c796416143b68343ae975aa51fafb2754679c3a9a9745a1f6bbf9937` |
| 수정 후, 허용 네 경로와 아래 concurrent 파일 제외 | 25,288 | `ae8be532c796416143b68343ae975aa51fafb2754679c3a9a9745a1f6bbf9937` |

세션 시작 뒤 다른 작업자가 `docs/master-design-beginner-guide.md`를 생성했다. Scheduler가 concurrent external change로 식별했으며 SHA-256은 `913e55ca6fd7632de2174e454e3baebd18c59a26eacae390602c7eb358bccba6`이다. 이 세션은 그 파일을 읽기 전용 checksum 확인 외에는 건드리지 않았고, 동일한 pre/post corpus 비교를 위해 위 aggregate의 post 측에서 명시적으로 제외했다.

`docs/master-design-sessions/19-26-scheduler-log.md`의 SHA-256은 수정 전후 `e29c5d3c213fb659709be5961a428149ea83641ddec2c61934b02ce788c967b6`로 동일하다. 세션 26 review도 `b1bef2412bf7e56c1d01ebe768599d4bc9126f29339e13f657ad58c2d5b9f95c`로 동일하다.

## 4. 결과와 남은 경계

네 finding의 지정 correction과 문서 수준 검증은 통과했다. Master와 질문 등록부는 계속 `REVIEW`이며, 이 결과는 세션 26 verdict를 스스로 `READY`로 바꾸지 않는다. 다음 단계는 수정본에 대한 별도 read-only independent re-review다.

남은 26개 `OPEN` 질문과 2개 `DEFERRED` 질문은 그대로다. 특히 numeric/rounding/time/matrix/input meaning, customer objective/disposition, algorithm defaults, Win PoC formula/budget, provider topology와 optional variants를 이 세션에서 결정하지 않았다.
