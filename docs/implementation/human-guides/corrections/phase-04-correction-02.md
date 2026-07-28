# Phase 04 사람용 구현 가이드 correction 02

```yaml
correction_status: COMPLETE_SELF_VERIFIED
correction_round: "02"
correction_type: HUMAN_GUIDE_RECHECK_CORRECTION
source_thread_id: 019fa957-eadc-74d1-ae44-6d2957482856
corrected_at: "2026-07-29T02:53:15+09:00"
timezone: Asia/Seoul
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
branch_observed: codex-implementation
target: docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md
review: docs/implementation/human-guides/reviews/phase-04-review.md
historical_correction_01: docs/implementation/human-guides/corrections/phase-04-correction-01.md
question_register: docs/master-design-open-questions.md
target_status_after_correction: IMPLEMENTATION_GUIDE_BLOCKED_BY_ENTRY_GATES
implementation_status_after_correction: NOT_STARTED
phase_acceptance_status_after_correction: NOT_ACCEPTED
evidence_status_after_correction: NOT_PRODUCED
addressed_findings:
  - HG-P04-R06
  - HG-P04-C01-N01
deferred_findings: []
owned_files:
  - docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-04-correction-02.md
historical_files_modified: false
code_pom_test_modified: false
stage_commit_push_worktree_performed: false
```

## 1. 결과와 correction 소유 범위

[Correction 01 재검증](../reviews/phase-04-review.md#correction-01-읽기-전용-재검증)의
두 OPEN finding을 모두 교정했다.

1. `HG-P04-R06`: [Target §4.2](../phases/phase-04-human-implementation-guide.md#42-검증-가능한-source-fingerprint)의
   expected-manifest와 live snapshot에서 zsh 특수 배열 `$path`와 충돌하던 `path`
   loop 변수를 `source_file`로 바꿨다. 두 block은 `bash`/`zsh` 공통 subshell
   contract와 explicit exit code를 가지며 live working-byte drift도 exit `43`으로
   fail-closed한다.
2. `HG-P04-C01-N01`: [Correction 01 §5](phase-04-correction-01.md#5-보존한-설계-경계와-마지막-안전-지점)의
   line 287 `Q-CAP-001~003` 주장을 역사 기록에서 지우지 않고 이 report에서
   **INVALID / SUPERSEDED**로 정정했다. Canonical question register와 target에
   존재하는 exact ID/ADR만 preservation authority로 기록했다.

Correction 01과 원 review는 historical read-only다. Scheduler/core/original 문서,
canonical Phase 문서, code, POM과 test도 수정하지 않았다. 이 correction은 Phase 04
구현, test evidence, independent review `PASS` 또는 acceptance receipt를 만들지
않으며 target의 entry gate를 열지 않는다.

## 2. 고정 입력과 hash

| Artifact | Correction 시작 SHA-256 | 최종 SHA-256 | 판정 |
|---|---|---|---|
| Target | `c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c` | `a717d366bd2b54a16f1103b115df8d55f881252d1465fd9bf8cc5df0510ceb2b` | 의도한 R06 수정 |
| 원 review + recheck | `441905d73b9c50eed5beba7880af4c3ca44bf285a27e38e265b769ccb1b278d6` | 동일 | Read-only |
| Correction 01 | `fcf6534f47324af3a1dbacab98c1509e50e78ad4fa35405e8fce721e299f248c` | 동일 | Historical read-only |
| Question register | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 동일 | Canonical read-only |

Target의 최종 Git object는
`d81befc4c30065ff6f2c45c985ba6f818a13df50`이다. Review recheck가 고정한 target
SHA-256 `c5dd…6504c`와 correction-01 SHA-256 `fcf6…48c`는 correction 시작 bytes와
정확히 일치했다. Question register의 Git blob
`3fff4c583a54f02dea667e78c8e5187d65ec0e18`도 target의 expected source manifest와
일치한다.

## 3. HG-P04-R06 — zsh-safe fingerprint와 live-drift fail-closed

- **Anchor:** Target §4.2의 첫 번째 expected `HEAD:<source_file>` TSV block과 두
  번째 exact-path live snapshot block, WP-04.0, §14.1.
- **Root cause:** 두 loop가 lowercase `path`를 사용했다. zsh에서는 `path`가
  `$PATH`와 연동된 특수 배열이므로 첫 `read ... path` 또는 `for path`가 command
  search path를 덮어썼다. 올바른 manifest도 `git: command not found`, exit `41`로
  오판될 수 있었다.
- **Source:** 원 review `HG-P04-R06`, recheck의 `HG-P04-R06 — OPEN`, Master
  Realization Plan §9/§12.1, target §4.1~§4.2의 expected HEAD와 live working-byte
  분리 계약.
- **Correction:** 두 block 모두 `source_file`을 사용하고 `( set -eu; ... )`
  subshell로 실행한다. Expected block은 `0=all match`, `41=missing/unresolvable
  HEAD source`, `42=blob mismatch`다. Live block은 source별 expected/working Git
  blob, scoped status/diff-name과 SHA-256을 출력하고
  `43=live drift`, `44=missing/unreadable live source`로 끝난다.
- **Gate preservation:** Expected `HEAD`는 immutable authority fingerprint이고 live
  bytes는 current observation일 뿐이다. Exit `43`이 official baseline 갱신이나
  acceptance를 허용하지 않는다. Changed heading → requirement → WP/test/evidence
  impact review와 owner rebaseline 전에는 implementation/evidence seal을 중단한다.
- **Verification:** Target의 첫째/둘째 `bash` fence 내용을 직접 추출해 `bash`와
  `zsh`에서 실행했다. Success와 live-drift는 target block을 그대로 실행했고,
  missing/mismatch는 같은 17-row block에서 한 fixture row만 바꿨다.
- **Residual:** 공유 checkout은 snapshot 직후에도 바뀔 수 있다. Target이 요구하는
  implementation start와 pre-review evidence seal의 두 번 검사를 유지하며 두
  snapshot이 다르면 seal을 중단한다.

### 3.1 Shell four-fixture 결과

| Fixture | Exact input | bash | zsh | Evidence / 판정 |
|---|---|---:|---:|---|
| Success | Target expected-manifest 그대로, 17 rows | `0` | `0` | 17/17 expected `HEAD` blob match |
| Missing | 같은 block의 첫 source path만 `docs/__missing_phase04_fixture__.md` | `41` | `41` | `fatal: Needed a single revision`; missing fail-closed |
| Mismatch | 같은 block의 첫 expected blob만 40개의 `0` | `42` | `42` | Existing source의 expected/actual inequality |
| Live drift | Target live snapshot 그대로 | `43` | `43` | 아래 working blob이 expected HEAD blob과 다름 |

Live-drift fixture의 두 shell 공통 핵심 출력:

```text
source_file=docs/implementation/execution-progress-and-results.md
expected_head_blob=250aa90ae568a6b32ec905fa5ee456d430ff72cf
working_tree_blob=9ebfc9825931121bd12f65980653dc25263ab211
 M docs/implementation/execution-progress-and-results.md
docs/implementation/execution-progress-and-results.md
c98cddecc7eb0265b835bfa0ea76dfef2f414369c96276914b2d9ca6d1d8c395  docs/implementation/execution-progress-and-results.md
```

이는 concurrent scheduler progress drift의 관찰 증거일 뿐 Phase 04 source rebaseline,
implementation evidence 또는 acceptance가 아니다.

## 4. HG-P04-C01-N01 — historical audit claim supersession

- **Anchor:** Historical correction-01 line 287과 §5, review recheck의
  `HG-P04-C01-N01 — OPEN`, canonical [question register](../../../master-design-open-questions.md),
  Target §5.1/§7.4/§16/§18.
- **Root cause:** Correction 01이 capability 관련 개념을 canonical 28-question
  register와 대조하지 않고 존재하지 않는 `Q-CAP-001~003` 식별자로 축약했다.
- **Source:** Question register v2.2-review의 exact 28 rows와 상태 집계
  `RESOLVED 26 / OPEN — EXPERIMENT_REQUIRED 1 / DEFERRED 1`; target에 실제 기록된
  question/decision/gate ID와 ADR.
- **Correction:** Correction 01은 historical audit bytes로 보존한다. 그 line 287
  claim만 이 report의 machine-readable record로 `INVALID_SUPERSEDED` 처리한다.
  비존재 ID를 다른 새 ID로 바꾸지 않고 canonical register 집합과 target의 exact
  preservation references를 열거한다.
- **Gate preservation:** `Q-BENCH-02` official 수치는 계속
  `OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01`은 `DEFERRED`, `ADR-003`/`ADR-004`와
  `P-04`는 target의 open/proposed 상태, `C-17`은 `GATED`다. correction audit
  정정은 이 gate를 해결하거나 Phase 04 entry/acceptance를 열지 않는다.
- **Verification:** Register 28개 exact ID와 target의 range-expanded exact Q-ID
  11개를 set 비교했다. Target-minus-register는 0이고, invalid claimed IDs 세 개는
  register와 target 양쪽에서 모두 0이다. Correction 01 SHA-256은 before/after
  동일하다.
- **Residual:** Historical report 본문에는 invalid text가 의도적으로 남으므로
  단독 독자는 오해할 수 있다. 후속 audit/automation은 아래 supersession record를
  함께 소비해야 한다.

### 4.1 Machine-readable supersession과 authoritative preservation list

```yaml
supersession:
  status: INVALID_SUPERSEDED
  scope: HISTORICAL_AUDIT_CLAIM_ONLY
  source:
    file: docs/implementation/human-guides/corrections/phase-04-correction-01.md
    line: 287
    claim: Q-CAP-001..003
    sha256: fcf6534f47324af3a1dbacab98c1509e50e78ad4fa35405e8fce721e299f248c
    file_modified: false
  invalid_claimed_ids:
    - id: Q-CAP-001
      present_in_question_register: false
      present_in_target: false
      owner_evidence: null
      decision_evidence: null
      resume_evidence: null
    - id: Q-CAP-002
      present_in_question_register: false
      present_in_target: false
      owner_evidence: null
      decision_evidence: null
      resume_evidence: null
    - id: Q-CAP-003
      present_in_question_register: false
      present_in_target: false
      owner_evidence: null
      decision_evidence: null
      resume_evidence: null
  replacement_question_ids_created: []
  authoritative_sources:
    question_register:
      file: docs/master-design-open-questions.md
      sha256: b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b
      exact_count: 28
      resolved_ids:
        - Q-ALG-01
        - Q-ALG-02
        - Q-BENCH-01
        - Q-BENCH-03
        - Q-COMP-01
        - Q-COMP-02
        - Q-IN-01
        - Q-IN-02
        - Q-INFRA-01
        - Q-MTX-01
        - Q-MTX-02
        - Q-MTX-03
        - Q-NUM-01
        - Q-NUM-02
        - Q-NUM-03
        - Q-OBJ-01
        - Q-OBJ-02
        - Q-OBJ-03
        - Q-REQ-01
        - Q-REQ-02
        - Q-RES-01
        - Q-RES-02
        - Q-TIME-01
        - Q-TIME-02
        - Q-TIME-03
        - Q-TIME-04
      open_experiment_required_ids:
        - Q-BENCH-02
      deferred_ids:
        - Q-VAR-01
    target_preservation_references:
      file: docs/implementation/human-guides/phases/phase-04-human-implementation-guide.md
      sha256: a717d366bd2b54a16f1103b115df8d55f881252d1465fd9bf8cc5df0510ceb2b
      exact_question_ids:
        resolved:
          - Q-COMP-01
          - Q-COMP-02
          - Q-INFRA-01
          - Q-OBJ-01
          - Q-OBJ-02
          - Q-OBJ-03
          - Q-REQ-01
          - Q-REQ-02
          - Q-TIME-03
        open_experiment_required:
          - Q-BENCH-02
        deferred:
          - Q-VAR-01
      exact_non_question_decision_ids:
        ADR-003: OPEN_REQUIRED
        ADR-004: OPEN
        P-04: PROPOSED_OPEN
        C-17: GATED
        C-03: TRACE_SOURCE_REFERENCE
        C-04: TRACE_SOURCE_REFERENCE
  set_comparison:
    target_question_count: 11
    target_minus_register: []
    register_minus_target:
      - Q-ALG-01
      - Q-ALG-02
      - Q-BENCH-01
      - Q-BENCH-03
      - Q-IN-01
      - Q-IN-02
      - Q-MTX-01
      - Q-MTX-02
      - Q-MTX-03
      - Q-NUM-01
      - Q-NUM-02
      - Q-NUM-03
      - Q-RES-01
      - Q-RES-02
      - Q-TIME-01
      - Q-TIME-02
      - Q-TIME-04
    invalid_claim_minus_register:
      - Q-CAP-001
      - Q-CAP-002
      - Q-CAP-003
    invalid_claim_minus_target:
      - Q-CAP-001
      - Q-CAP-002
      - Q-CAP-003
```

Ranges는 comparison에서 사용하지 않고 모두 exact ID로 확장했다. Register의
`register_minus_target` 17개는 유효하지만 Phase 04 target이 보존 목록에서 직접
참조하지 않는 질문이며 삭제·무효화를 뜻하지 않는다.

## 5. 정적·scope 검증

| 검사 | 결과 | 근거 |
|---|---|---|
| Shell four-fixture | PASS | bash/zsh exit `0/41/42/43`이 contract와 일치 |
| Canonical ID set | PASS | Register 28, target exact subset 11, target-minus-register 0 |
| Invalid ID evidence | PASS | 세 ID 모두 register/target absent, owner/decision/resume `null`, replacement 0 |
| Historical immutability | PASS | Review, correction-01, question register SHA-256 before/after 동일 |
| Target local link/GFM fragment | PASS | Local path/fragment missing 0 |
| Report local link/GFM fragment | PASS | Local path/fragment missing 0 |
| Heading/fence | PASS | H1 각 1개, fence parity와 heading hierarchy 정상 |
| Whitespace/EOF | PASS | Trailing whitespace/tab/CRLF/NUL 0, final LF 정확히 1개 |
| Scoped tracked diff check | PASS | `git diff --check -- <두 owned file>` exit 0 |
| Untracked-aware diff check | PASS | 각 owned file의 `/dev/null` no-index raw exit 1은 content difference, whitespace diagnostic 0 |
| Scoped/untracked status | PASS | 두 owned file만 correction write scope; pre-existing read-only/unrelated changes 보존 |
| Code/POM/test | NOT_RUN | 문서 correction이며 해당 경로를 읽기 전용으로 유지 |

## 6. 최종 판정과 residual risk

두 OPEN documentation finding은 사람용 가이드/correction audit 수준에서
교정됐다. Target은 `bash`와 zsh 모두에서 성공 가능한 expected-manifest를 제공하고,
missing/mismatch/live drift를 서로 다른 non-zero exit로 차단한다. Correction 01의
비존재 ID 주장은 역사 bytes를 바꾸지 않은 채 명시적으로 무효화됐다.

남은 risk는 snapshot 이후 TOCTOU drift, unresolved Phase 00~03 receipts,
cross-Phase evaluator/equality/portfolio authority, ADR/owner approval와 실제
implementation/evidence/review/receipt 부재다. 이는 deferred correction finding이
아니며 target의 `BLOCKED / NOT_STARTED / NOT_PRODUCED / NOT_ACCEPTED` 경계를
그대로 유지한다.

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: HG-P04-R06, HG-P04-C01-N01
SUPERSEDES_CORRECTION_REPORT_CLAIM: correction-01 line 287 Q-CAP-001..003 INVALID
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c
TARGET_HASH_AFTER: a717d366bd2b54a16f1103b115df8d55f881252d1465fd9bf8cc5df0510ceb2b
