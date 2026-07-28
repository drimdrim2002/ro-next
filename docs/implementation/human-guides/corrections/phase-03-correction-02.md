# Phase 03 사람용 구현 가이드 correction 02

```yaml
phase: "03"
correction_round: "02"
correction_type: INDEPENDENT_HUMAN_GUIDE_CORRECTION
source_review: docs/implementation/human-guides/reviews/phase-03-review.md
source_correction_01: docs/implementation/human-guides/corrections/phase-03-correction-01.md
target: docs/implementation/human-guides/phases/phase-03-human-implementation-guide.md
review_recheck_verdict_input: FURTHER_CORRECTION_REQUIRED
finding_scope:
  - HG03-R006
addressed_findings: 1
deferred_findings: 0
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
corrected_at: 2026-07-29T02:46:30+09:00
timezone: Asia/Seoul
implementation_or_test_execution: NOT_RUN
owner_role: RPDPTW Phase 03 human-guide correction 02 writer
owned_files:
  - docs/implementation/human-guides/phases/phase-03-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-03-correction-02.md
read_only_inputs:
  - docs/implementation/human-guides/reviews/phase-03-review.md
  - docs/implementation/human-guides/corrections/phase-03-correction-01.md
  - scheduler README/progress, other guides, canonical/original documents, code, POM, tests
```

## 1. 교정 결과와 소유 범위

[원 review의 Correction 01 재검증과 HG03-R006](../reviews/phase-03-review.md#new-hg03-r006--low--open), [correction 01 report](phase-03-correction-01.md), corrected Target 전체를 읽고 기록된 before hash와 현재 파일을 대조했다. Target과 correction 01은 재검증에 고정된 SHA-256과 일치했다. 이 correction은 두 owned file만 수정하며 원 review, correction 01, scheduler README/progress, 다른 guide, canonical/original 문서와 code/POM/test를 변경하지 않는다. Stage, commit, push와 worktree 작업도 수행하지 않았다.

교정 결과는 [Target의 legacy §6 alias](../phases/phase-03-human-implementation-guide.md#6-실제-inventory-현재-상태와-목표-상태) 한 곳이다. 새 §6 heading 바로 앞에 다음 GFM-compatible explicit HTML anchor를 추가했다.

```html
<a id="6-실제-inventory-현재-상태와-목표-상태"></a>
```

기존 §6 heading, 계약, gate와 본문은 변경하지 않았다.

## 2. HG03-R006 root cause와 변경 anchor

- **Finding:** Correction 01이 §6 heading을 더 정확한 문구로 바꾸면서 원 review의 inbound fragment `#6-실제-inventory-현재-상태와-목표-상태`가 사라졌다.
- **Root cause:** Heading rename 시 새 heading의 링크만 검사하고 immutable 원 review와 correction provenance가 가리키는 legacy fragment의 호환 anchor를 보존·재검사하지 않았다.
- **변경 anchor:** Target의 `## 6. 실제 inventory: HEAD baseline, 미커밋 live snapshot과 목표` 바로 앞.
- **Required correction 적용:** 명시적 alias `<a id="6-실제-inventory-현재-상태와-목표-상태"></a>`만 추가했다.
- **불변성 확인:** 현재 Target에서 이 alias 블록만 제거해 SHA-256을 다시 계산하면 before hash와 정확히 같으므로 기존 heading/계약/gate/본문의 다른 byte 변경은 0이다.

## 3. Hash 기록

| Artifact | Before / read-only hash | After hash | 판정 |
|---|---|---|---|
| Target SHA-256 | `f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3` | `45589e13070291de5de46e9f92fcf156d23b424ae25bddcdae9044ef8a9b7926` | Alias만 추가 |
| Target Git hash-object | `f8c9db72ad5bc1a6b5c23d658b5d4e6280f96fae` | `205dfb8a7a9c9f2cde4001734ac7a46de7fb0f1a` | Commit/stage 없는 content identity |
| Original review SHA-256 | `9ff7f1dbf524601a1ba51daae869af921e2f700967e4a64ec7b3454f1d064755` | 동일 | Read-only |
| Original review Git hash-object | `685f29812a38e2e575b0f128302f7bd07c69fe20` | 동일 | Read-only |
| Correction 01 SHA-256 | `c166e1f064ca3a42ddd71a05b1bcb557e36578fd88f2a0e8a251f2893c73534f` | 동일 | Read-only |
| Correction 01 Git hash-object | `b042303c73db11a039d0723393d6c2e414c8385d` | 동일 | Read-only |

## 4. 보존한 계약과 gate

Alias는 탐색 호환성만 복원하며 correction 01이 보존한 다음 gate와 계약을 바꾸지 않는다.

- Phase 00~02 accepted receipt와 exact handoff fingerprint 전 entry gate는 `BLOCKED`다.
- Live reactor/wrapper는 `UNCOMMITTED_UNAPPROVED_SNAPSHOT`이며 Phase 03 source/evidence authority가 아니다.
- Fact/failure/facet package owner는 `CROSS-PHASE ARCHITECTURE BLOCKER`이고 승인된 후보 DAG/ADR 전 package/file을 만들지 않는다.
- Full-solution evaluator와 business equality/context tie boundary는 cross-Phase blocker로 남는다.
- Exact 42-method manifest와 11 hard-bound row, fresh report와 zero-test/stale-report fail-closed 판정을 유지한다.
- Immutable evidence `M → R → receipt` DAG와 independent review/post-review receipt 요구를 유지한다.
- `Q-BENCH-02`는 `OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01`은 `DEFERRED`다.
- Phase 13은 14A receipt와 별도 `C-17` 승인 전 `GATED`이며 14A와 14B authority를 합치지 않는다.
- Phase 03 code/test/evidence와 acceptance는 계속 `NOT_PRODUCED` / `NOT_ACCEPTED`다.

## 5. 검증 결과

| 검사 | 결과 | 근거 |
|---|---|---|
| Before hash 재구성 | `PASS` | 현재 Target에서 정확한 alias 블록만 제거한 in-memory bytes의 SHA-256이 `f23c052b…c3b3` |
| Required alias 위치/유일성 | `PASS` | 새 §6 heading 바로 앞 1개, id 중복 0 |
| 기존 heading/계약/gate/본문 | `PASS` | Alias 제거 재구성 hash가 before Target과 동일 |
| Target local links/GFM fragments | `PASS` | Markdown local link 73개, broken 0; legacy explicit id 인식 |
| Correction 01 local links/GFM fragments | `PASS` | Markdown local link 26개, broken 0 |
| Original review local links/GFM fragments | `PASS` | Markdown local link 8개, broken 0; 이전 broken legacy §6 fragment 복원 |
| Correction 02 local links/GFM fragments | `PASS` | Markdown local link 3개, broken 0 |
| Heading/fence | `PASS` | Target H1 1개와 기존 heading 구조 유지, fence marker 74개; report H1 1개, fence marker 4개; 모두 parity 정상 |
| Whitespace/EOF | `PASS` | 두 owned file 모두 trailing whitespace 0, tab 0, CRLF 0, EOF LF |
| Scoped tracked diff check | `PASS` | `git diff --check -- <두 owned file>` exit 0 |
| Untracked-aware whitespace check | `PASS` | 각 owned file의 `git diff --no-index --check /dev/null <file>` raw exit 1은 content difference, whitespace diagnostic 0 |
| Scoped/untracked-aware write scope | `PASS` | 두 owned file만 대상으로 status/diff를 검사했고 unrelated working-tree 변경은 보존 |
| Implementation/test 실행 | `NOT_RUN` | 문서 fragment compatibility correction이며 code/POM/test는 read-only |

세 문서의 모든 local Markdown link는 file target을 실제 경로로 resolve하고 fragment를 대상 문서의 non-fenced GFM heading slug 또는 explicit HTML id와 대조했다. 특히 원 review에서 유일하게 깨졌던 legacy §6 inbound fragment가 alias에 resolve되어 broken 합계는 0이다.

## 6. 판정

`HG03-R006`의 required correction은 완료됐다. 이 판정은 legacy fragment traceability 복구만 뜻하며 Phase 03 구현, evidence, independent implementation review 또는 acceptance를 뜻하지 않는다.

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: HG03-R006
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3
TARGET_HASH_AFTER: 45589e13070291de5de46e9f92fcf156d23b424ae25bddcdae9044ef8a9b7926
