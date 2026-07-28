# Phase 07 사람용 구현 가이드 교정 보고서 — Round 02

```yaml
phase: "07"
correction_round: "02"
correction_type: HUMAN_IMPLEMENTATION_GUIDE_CORRECTION
status: COMPLETED
target: docs/implementation/human-guides/phases/phase-07-human-implementation-guide.md
review_recheck_input: docs/implementation/human-guides/reviews/phase-07-review.md
previous_correction_input: docs/implementation/human-guides/corrections/phase-07-correction-01.md
original_phase_review_input: docs/implementation/reviews/phase-07-review.md
correction_report: docs/implementation/human-guides/corrections/phase-07-correction-02.md
finding_scope:
  - HG07-R006
addressed_findings: 1
deferred_findings: 0
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
branch_observed: codex-implementation
corrected_at: 2026-07-29T02:54:22+09:00
timezone: Asia/Seoul
owned_files:
  - docs/implementation/human-guides/phases/phase-07-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-07-correction-02.md
read_only_inputs:
  - docs/implementation/human-guides/reviews/phase-07-review.md
  - docs/implementation/human-guides/corrections/phase-07-correction-01.md
  - docs/implementation/reviews/phase-07-review.md
  - repository-local inbound Markdown links
prohibited_actions_observed:
  code_or_pom_or_test_change: false
  stage: false
  commit: false
  push: false
  worktree_operation: false
implementation_or_test_execution: NOT_RUN
target_sha256_before: 9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd
target_sha256_after: d4b5230965aa43bfb8f652c546782e2db42f54971ba649c5b9cf9c6d1f118841
```

## 1. 교정 결과와 소유 범위

[Correction 01 재검증의 `HG07-R006`](../reviews/phase-07-review.md#hg07-r006--122-heading-변경이-원-review의-gfm-trace-link를-끊었다), [correction 01 report](phase-07-correction-01.md), corrected Target §12.2와 [canonical 원 Phase 07 review](../../reviews/phase-07-review.md)를 읽고 입력 hash를 고정했다. Target과 correction 01은 재검증 footer의 SHA-256과 일치했다. 이 교정은 두 owned file만 수정하며 review/recheck, correction 01, canonical/original 문서, README/progress, 다른 guide, code/POM/test/evidence를 변경하지 않았다. Stage, commit, push와 worktree 작업도 수행하지 않았다.

교정 결과는 [Target §12.2의 legacy compatibility anchor](../phases/phase-07-human-implementation-guide.md#122-test-class와-exact-method-후보) 한 곳이다. 현재 heading 바로 앞에 다음 explicit HTML anchor를 추가했다.

```html
<a id="122-test-class와-exact-method-후보"></a>
```

이어 legacy “후보” ID는 기존 문서의 navigation compatibility만 위한 alias이고 현재 normative status는 canonical required 69-method manifest라는 짧은 설명을 추가했다. `### 12.2 Canonical required 69-method manifest` heading과 69-method 계약 본문은 그대로 유지했다.

## 2. HG07-R006 root cause와 변경 anchor

- **Finding:** Correction 01이 §12.2 heading을 exact 69-method 상태에 맞게 바꾸면서 원 human-guide review의 inbound fragment `#122-test-class와-exact-method-후보`가 사라졌다.
- **Root cause:** Heading rename 뒤 target과 correction report의 outbound link만 검사했고, immutable review 및 repository-local 문서가 target으로 보내는 historical inbound fragment를 compatibility set에 포함하지 않았다.
- **변경 anchor:** Target의 `### 12.2 Canonical required 69-method manifest` 바로 앞.
- **Required correction 적용:** Explicit alias `<a id="122-test-class와-exact-method-후보"></a>`와 navigation-only 설명만 추가했다.
- **불변성 확인:** 현재 Target에서 exact 추가 block을 제거하고 기존 heading 한 줄을 복원한 in-memory bytes의 SHA-256이 before hash와 정확히 같다. Heading text, manifest bytes, 계약/gate/본문의 그 밖의 변경은 0이다.

## 3. Hash 기록

| Artifact | Before / frozen read SHA-256 | After / recheck SHA-256 | Git hash-object | Lines | 판정 |
|---|---|---|---|---:|---|
| Target | `9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd` | `d4b5230965aa43bfb8f652c546782e2db42f54971ba649c5b9cf9c6d1f118841` | `471f189cded4efe61ee1ee26ec3db663c37b8922` | 2,167 | Intended compatibility change |
| Human-guide review/recheck | `7b5ff1f2acfcb54635bfc8faca7e532915c7935f62dea2fbea0f7be27faae4ef` | same | `fbf0897fe7d891300d104417fdabf0df146ecc36` | 345 | Read-only |
| Correction 01 | `776c2128e5aa3a012db581386f02ad95fcfb481c6170aa8381a3329d3bbd3764` | same | `c2029e9bc37592ef4333c50fcc533674cfa0f628` | 207 | Read-only |
| Canonical 원 Phase 07 review | `82da52bcd52724c44a5ffd913f3146b7017457ed0a209b85611b80cb8547c78f` | same | `156a994ef66d9c6f51785d96d2b327fe4539712e` | 314 | Read-only |

## 4. Gate와 normative status 보존

Compatibility alias는 navigation만 복원하며 correction 01이 확정한 현재 §12.2 의미와 다음 gate를 바꾸지 않는다.

- Canonical required set은 계속 69개이고 LF SHA-256은 `aff4bae9269b37b09a8df9909cb9e5c8f060e3bb2f1f1cc166c29749e66ad339`다. Legacy ID의 “후보”는 normative subset 또는 status가 아니다.
- Target additional set은 계속 5개이고 LF SHA-256은 `5374ef3ddf0aa9961c6b3974f036ff360f3f8f3ec8f7585134814dc059195502`다. Required 69개를 줄이거나 대체하지 않는다.
- Missing/extra/duplicate/order mismatch와 failed/error/skipped/aborted/stale/zero-discovered fail-closed 계약을 유지한다.
- `Q-BENCH-02`는 `OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01`은 `DEFERRED`다.
- External JSON/media/hash/public compatibility, full-solution evaluator/comparator API와 Phase 06→07 handoff schema는 계속 `OPEN`/`PROPOSED`/`BLOCKED` gate다.
- Phase 13 `C-17`, Phase 14A benchmark acceptance와 Phase 14B production authority는 계속 분리된 `GATED` 상태다.
- Phase 07 implementation/test/evidence와 acceptance는 생성하거나 승격하지 않았다.

## 5. 검증 결과

| 검사 | 결과 | 근거 |
|---|---|---|
| Before hash 재구성 | `PASS` | Exact alias/설명 block 제거와 기존 heading 복원 후 SHA-256이 `9b324867…27dd` |
| Alias 위치/유일성 | `PASS` | §12.2 heading 바로 앞 explicit id 1개, duplicate 0 |
| Heading과 normative manifest | `PASS` | Heading text 동일; required `69/69`, additional `5/5`, 두 digest와 cross-duplicate 0 유지 |
| Target local link/GFM | `PASS` | Local 32, fragment 0, broken 0 |
| Correction 01 local link/GFM | `PASS` | Local 1, fragment 0, broken 0 |
| Human-guide review/recheck local link/GFM | `PASS` | Local 24, fragment 9, broken 0; 기존 §12.2 legacy fragment 복원 |
| Canonical 원 Phase 07 review local link/GFM | `PASS` | Local 16, fragment 5, broken 0 |
| Repository-local target inbound fragments | `PASS` | Fragment 10, broken 0; 기존 review 9개와 correction 02의 새 trace 1개를 포함해 target path로 resolve한 모든 local Markdown inbound fragment 검사 |
| Correction 02 local link/GFM | `PASS` | Local 4, fragment 2, broken 0 |
| Heading/fence | `PASS` | Target 기존 H1/H2/H3/H4 `1/18/63/2`, fence marker 88 및 parity 유지; report hierarchy jump 0, fence closed |
| Whitespace/encoding/EOF | `PASS` | 두 owned file의 trailing whitespace/tab/CRLF/NUL 0, UTF-8 read 성공, EOF LF |
| Scoped tracked diff check | `PASS` | `git diff --check -- <두 owned file>` diagnostic 0; 두 파일은 untracked 상태라 다음 no-index 검사로 보완 |
| Untracked-aware diff check | `PASS` | 각 owned file의 `git diff --no-index --check /dev/null <file>` whitespace diagnostic 0; raw exit 1은 content difference |
| Scoped/untracked write set | `PASS` | Scoped status에서 두 owned file만 `??`; correction 전부터 untracked인 Target과 새 report를 구분하고 비소유 변경 보존 |
| Implementation/test 실행 | `NOT_RUN` | 문서 navigation compatibility correction이며 code/POM/test는 read-only |

Link 검사는 fenced code를 제외하고 local path를 실제 파일에 resolve한 뒤 fragment를 대상 문서의 GFM heading slug 또는 explicit HTML id와 대조했다. 특히 원 human-guide review가 Target으로 보내는 기존 9개와 이 report의 새 trace 1개가 모두 resolve되어 repository-local inbound broken 합계는 0이다.

## 6. 판정

`HG07-R006`의 required correction은 완료됐다. 이 판정은 historical navigation trace 복구만 뜻하며 legacy “후보” wording을 현재 acceptance status로 되살리거나 Phase 07 구현/evidence/acceptance를 승인하지 않는다.

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: HG07-R006
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd
TARGET_HASH_AFTER: d4b5230965aa43bfb8f652c546782e2db42f54971ba649c5b9cf9c6d1f118841
