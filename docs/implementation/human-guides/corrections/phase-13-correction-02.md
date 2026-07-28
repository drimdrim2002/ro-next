# Phase 13 사람용 구현 가이드 correction 02

```yaml
phase: "13"
correction_round: "02"
correction_type: SCHEDULER_FINAL_GLOBAL_LINK_CORRECTION
correction_status: COMPLETE
source_thread_id: 019fa957-eadc-74d1-ae44-6d2957482856
source_finding: HG13-SFV-001
target: docs/implementation/human-guides/phases/phase-13-human-implementation-guide.md
implementation_gate: C17_GATE_CLOSED
phase14a_acceptance_receipt: NOT_PRODUCED
code_pom_test_scheduler_change: NONE
stage_commit_push_worktree: NOT_PERFORMED
edit_scope:
  - docs/implementation/human-guides/phases/phase-13-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-13-correction-02.md
```

## 1. Finding과 root cause

Scheduler final global link check finding `HG13-SFV-001`은
[Target guide](../phases/phase-13-human-implementation-guide.md) §3.3의 source
fragment 두 개가 실제 heading으로 resolve되지 않는 문제다.

Root cause는 두 source heading의 em dash(`—`)를 GFM fragment로 옮길 때 em
dash만 제거한 뒤 남는 좌우 공백 두 개를 hyphen 하나로 축약한 것이다. GFM
heading slug에서는 em dash가 제거되어도 그 양쪽 공백은 각각 hyphen으로
치환되므로 경계에는 `--`가 남아야 한다.

## 2. Exact correction과 source heading

Link label, 읽기 순서, section 의미와 source 문서는 그대로 두고 destination
fragment만 다음과 같이 교정했다.

1. Integrated Design

   - Source heading:
     [`## 17. Phase 13 — Optional route pool과 route selection`](../../../architecture-domain-implementation-design.md#17-phase-13--optional-route-pool과-route-selection)
   - Old:
     `../../../architecture-domain-implementation-design.md#17-phase-13-optional-route-pool과-route-selection`
   - New:
     `../../../architecture-domain-implementation-design.md#17-phase-13--optional-route-pool과-route-selection`
   - Derivation: `17.`의 period와 em dash를 제거하고 Latin 문자를 lowercase로
     만든다. `13 — Optional`의 좌우 공백은 각각 hyphen이 되어
     `17-phase-13--optional-route-pool과-route-selection`이 된다.

2. Master Realization Plan

   - Source heading:
     [`### Phase 13 — Optional hybrid`](../../master-realization-plan.md#phase-13--optional-hybrid)
   - Old:
     `../../master-realization-plan.md#phase-13-optional-hybrid`
   - New:
     `../../master-realization-plan.md#phase-13--optional-hybrid`
   - Derivation: em dash를 제거하고 Latin 문자를 lowercase로 만든다.
     `13 — Optional`의 좌우 공백은 각각 hyphen이 되어
     `phase-13--optional-hybrid`가 된다.

## 3. Hash와 scope

| 대상 | SHA-256 |
|---|---|
| Target before | `68c35d77638d440513ec55bd025599bb6d34a7e7c429dacdfe362ceb3d5feba1` |
| Target after | `f5a755808104da13cd51f34235f37aafa8348c4ca094ea3061593203af88db07` |
| [Correction 01](phase-13-correction-01.md) read-only snapshot | `ad8b67844a26e5246b3e3c4f2ac7008cd351f6dfa54b2cd4f052c0864d19ebc0` |
| [Phase 13 human review](../reviews/phase-13-review.md) read-only snapshot | `75dc1dbfa9ffa5d1eb49d200566a3bf8ced05b7f2faf93834a3a2bfcfa24ed17` |

Target의 semantic diff는 위 두 URL fragment의 single hyphen을 double hyphen으로
바꾼 것뿐이다. Correction 02 report 한 개를 새로 만들었으며 correction 01,
review, scheduler, core/original 문서, Java, POM과 test는 수정하지 않았다.

## 4. Authority와 gate 보존

이 교정은 broken source navigation만 복원한다. Link label, section 의미,
authority 순서, ownership, acceptance 조건, evidence 요구, 본문과 gate 상태는
바꾸지 않았다.

- Phase 13은 계속 `C17_GATE_CLOSED`, `GATED_NOT_STARTED`,
  `NOT_PRODUCED`, `NOT_ACCEPTED`다.
- Phase 14A acceptance receipt를 만들거나 Phase 14B
  signing/trust/action-time/production authority를 바꾸지 않았다.
- 정적 link PASS는 implementation, Maven/native integration, evidence,
  acceptance 또는 production readiness를 뜻하지 않는다.

## 5. Verification

| 검사 | 결과 | 근거 |
|---|---|---|
| `HG13-SFV-001` exact links | `PASS` | Old fragment 0, corrected fragment 각 1 |
| Whole human-guides local link/fragment | `PASS` | 기존 finding의 broken 두 건이 0으로 감소하고 검사 대상 broken 0 |
| Target/correction 01/review links | `PASS` | 상대 파일과 heading fragment resolve |
| Target semantic scope | `PASS` | Before/after 비교에서 URL fragment 두 곳만 변경 |
| Correction 01/review immutability | `PASS` | SHA-256 snapshot 불변 |
| Heading/fence/whitespace/EOF | `PASS` | H1/heading 구조, fence parity, trailing whitespace/tab/CRLF 0, LF EOF |
| Scoped/untracked diff | `PASS` | 허용된 target 수정과 correction 02 신규 파일만 이번 교정 소유 |
| Stage/commit/push/worktree | `NOT_PERFORMED` | 사용자 금지 준수 |

## 6. Residual

`HG13-SFV-001`에 대해 deferred link finding은 없다. Phase 13 implementation,
gate, evidence와 acceptance residual은
[Correction 01 §7](phase-13-correction-01.md#7-residual-blocker와-handoff)에
기록된 상태 그대로이며 이 link-only correction의 범위 밖이다. 공유 checkout의
기존 modified/deleted/untracked 작업은 보존했으며 accepted baseline으로
해석하지 않았다.

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: HG13-SFV-001
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 68c35d77638d440513ec55bd025599bb6d34a7e7c429dacdfe362ceb3d5feba1
TARGET_HASH_AFTER: f5a755808104da13cd51f34235f37aafa8348c4ca094ea3061593203af88db07
