# Phase 08 사람용 구현 가이드 교정 보고서 — Round 02

```yaml
correction_status: COMPLETE
correction_round: "02"
corrected_at: 2026-07-29T02:50:53+09:00
validated_at: 2026-07-29T02:53:29+09:00
head_commit_observed: 7cc890ee1d0805df5ae14b633127fade4f978639
branch_observed: codex-implementation
target: docs/implementation/human-guides/phases/phase-08-human-implementation-guide.md
review_input: docs/implementation/human-guides/reviews/phase-08-review.md
previous_correction_input: docs/implementation/human-guides/corrections/phase-08-correction-01.md
correction_report: docs/implementation/human-guides/corrections/phase-08-correction-02.md
correction_owner: RPDPTW Phase 08 human-guide correction 02 owner
architecture_authority: docs/architecture-design.md
historical_architecture_input: docs/2026-07-26-architecture-design.md
status_authority: total scheduler
allowed_write_scope:
  - docs/implementation/human-guides/phases/phase-08-human-implementation-guide.md
  - docs/implementation/human-guides/corrections/phase-08-correction-02.md
code_pom_test_evidence_status_modified: false
review_or_previous_correction_modified: false
git_stage_commit_push_worktree_status: NOT_PERFORMED
owned_git_status: UNTRACKED_TARGET_AND_NEW_CORRECTION_REPORT
unrelated_concurrent_drift_observed: true
target_sha256_before: ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326
target_sha256_after: 624efc9f8619dc3e239c65967ee114750712a9770e90969859f431cafe64d982
target_git_object_before: b3572fee2c687534cc41a581cb18f6f45e5328c6
target_git_object_after: 6be82ebb56af3cc637ab127802ad3d9f345f11f7
addressed_findings:
  - F-HG-P08-001
deferred_findings: []
```

## 1. Finding, root cause와 교정 결론

[Phase 08 사람용 가이드](../phases/phase-08-human-implementation-guide.md)의
Correction 01 재검증에서 `F-HG-P08-001`만 `OPEN`으로 남았다. §3.1~§3.3은 current
source hierarchy와 current-vs-dated impact를 이미 교정했지만, §17의 13개
requirement 행은 날짜 고정 Architecture §3/§5 번호를 `Current` 또는 `Dated`
label 없이 계속 사용했다.

Root cause는 source table을 current hierarchy로 바꾼 뒤 requirement-level
traceability의 historical section mapping을 끝까지 재작성하지 않은 것이다. 이 때문에
독자는 [Current Architecture](../../../architecture-design.md)에서 존재하지 않거나
다른 의미인 §3.6/§5.3/§5.5를 찾게 되고, source receipt와 requirement 추적 결과가
갈라졌다.

이번 교정은 [재검증 finding](../reviews/phase-08-review.md)의 정확한 13개 행만 실제
Current Architecture heading으로 다시 매핑했다. 모든 source label은
`Current Architecture`로 명시했다. 날짜 고정 문서는
[Dated Architecture](../../../2026-07-26-architecture-design.md)
`(HISTORICAL_IMPLEMENTATION_BASELINE_ONLY)`로만 읽었고 target source cell에는
provenance가 필요하지 않아 남기지 않았다. Requirement 의미, owner, decision/gate,
WP와 exact test/evidence는 변경하지 않았다.

## 2. 고정한 입력과 before/after hash

SHA-256과 Git object는 교정 시작/완료 bytes를 기준으로 한다. Source와 review의
`Before = After`는 이 교정 scope에서 수정하지 않았다는 뜻이다.

| File/role | Lines | Before SHA-256 / Git object | After SHA-256 / Git object | 판정 |
|---|---:|---|---|---|
| Target guide | 2,833 | `ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326` / `b3572fee2c687534cc41a581cb18f6f45e5328c6` | `624efc9f8619dc3e239c65967ee114750712a9770e90969859f431cafe64d982` / `6be82ebb56af3cc637ab127802ad3d9f345f11f7` | 13개 Source cell corrected |
| Human-guide review/revalidation | 650 | `4c56750d8157995a8abfd104dbfd3b39e1c9dae5bd30d10076036ed4b6a9a79b` / `3429f5591bd54a7cec6abfddcff0707ccd42ee8b` | same | Read-only correction input |
| Correction 01 | 236 | `14646cfc9b97215af37ee4afb76a259018dfc8a751835445b4b002ea7fd6aa15` / `986bda9c8986da43ab7af365a8a6232f28bb65db` | same | Read-only prior correction |
| Current Architecture | 1,469 | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` / `81495ff448d0e618ab3563e8ff80614fb1028acf` | same | Current authority |
| Dated Architecture | 1,019 | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` / `d51339e251dee1e032e711144dc63d6d07d7323b` | same | `HISTORICAL_IMPLEMENTATION_BASELINE_ONLY` |

Target §3.1~§3.3과 §17, review의 원 finding/Correction 01 재검증, Current
Architecture §4.2/§10~12/§16~18, Dated Architecture §3/§5, Correction 01의
source/gate/validation receipt를 직접 대조했다.

## 3. 13개 old → current mapping과 source evidence

표의 target anchor는 교정 후에도 동일한 §17 requirement row와 line이다.
`Old Architecture`는 모두 Dated Architecture
`(HISTORICAL_IMPLEMENTATION_BASELINE_ONLY)` 절 번호다. 표에 쓰지 않은 Master,
Integrated, review, Plan source는 target에서 그대로 보존했다.

| Requirement / target anchor | Old Architecture citation | Current Architecture citation | Current source evidence |
|---|---|---|---|
| `REQ-P08-FLOW`, §17:2807 | §3.1 | §4.2 `Main flow` | Submission부터 canonical input, solve, 두 verifier, publication CAS와 retrieval까지 한 흐름을 고정한다. |
| `REQ-P08-IDENTITY`, §17:2810 | §3.6/§5.2 | §11.3 `Logical identity와 idempotency`; §16.3 `Provenance`; §17.2 `Retry/failure matrix` | Submission/Solve/WorkerRun/Attempt identity, versioned lineage와 retry identity를 함께 고정한다. |
| `REQ-P08-AUTHZ`, §17:2811 | §5.5 | §17.3 `Security and data boundary` | Tenant access scope, least privilege, profile/preset 선택 권한과 secret/PII redaction을 명시한다. |
| `REQ-P08-FAILURE`, §17:2812 | §5.3 | §12.1 `Port catalog`; §17.2 `Retry/failure matrix` | Artifact/state port 책임과 transient/integrity/CAS failure의 owner·retry·정상 결과 가능성을 분리한다. |
| `REQ-P08-ARTIFACT`, §17:2813 | §5.1~§5.2 | §12.1 `Port catalog`; §16.2 `Artifact contract` | Immutable put/get/verify port와 create-once, digest-before-deserialize, CAS publication index를 고정한다. |
| `REQ-P08-LOCAL`, §17:2815 | §3.2 | §10.1~§10.3 `Local/single-run runtime` | Local을 same-use-case/same-verification reference execution으로 두고 explicit workspace, same-process dispatch와 local publication을 규정한다. |
| `REQ-P08-REPRO`, §17:2817 | §3.2 | §10.1~§10.3 `Local/single-run runtime`; §17.1 `Structured observability` | Deterministic rerun을 기준으로 삼고 thread pool, elapsed time과 completion order를 result/quality의 hidden input에서 제외한다. |
| `REQ-P08-CANCEL`, §17:2818 | §3.6 | §12.1 `Port catalog`; §17.2 `Retry/failure matrix` | Cancellation intent의 기록·조회·전파와 idempotent intent를 실제 정상 완료와 분리한다. Exact last-safe 의미는 보존된 Master §13.1이 계속 권위다. |
| `REQ-P08-DEADLINE`, §17:2819 | §3.4/§5.3 | §10.3 `Single-run과 local multi-worker`; §11.1 `Logical state machine`; §17.2 `Retry/failure matrix` | Local wall-clock을 정상 step 종료로 바꾸지 않고 watchdog/resource/platform timeout을 별도 exceptional state/failure로 둔다. |
| `REQ-P08-RETRY`, §17:2820 | §3.6/§5.3 | §11.3 `Logical identity와 idempotency`; §17.2 `Retry/failure matrix` | 같은 logical worker identity에서 허용되는 attempt 변화와 failure별 retry owner/identity를 고정한다. |
| `REQ-P08-SECURITY`, §17:2821 | §5.5 | §17.3 `Security and data boundary` | Tenant scope, 최소 권한, secret/raw address/PII redaction과 artifact identity/encryption 분리를 고정한다. Path/symlink/limit 상세는 보존된 Integrated §20이 계속 권위다. |
| `REQ-P08-OBS`, §17:2822 | §5.5 | §17.1 `Structured observability` | Correlation/telemetry field를 정의하면서 elapsed time과 completion order가 quality/seed/replay fingerprint 입력이 아님을 명시한다. |
| `REQ-P08-P10`, §17:2825 | §3.3~§3.6 | §11.1~§11.3 `Distributed multi-round runtime`; §12.1~§12.2 `Provider-neutral logical ports` | Logical state/flow/identity와 provider-neutral dispatch/cancel/state seam만 고정하고 Phase 10 coordinator 구현은 보존된 Plan Phase 10에 남긴다. |

## 4. Semantic impact, ownership와 gate 보존 검증

Correction 전후 §17 row를 field별로 비교했다.

| 비교 항목 | 결과 | 확인 내용 |
|---|---|---|
| Requirement identity/text | `UNCHANGED` | 위 13개 `REQ-P08-*` ID와 요구 문구가 byte-for-byte 동일하다. |
| Source authority | `CORRECTED` | Unqualified historical Architecture 번호를 실제 `Current Architecture` section/heading으로 교체했다. |
| Other source/provenance | `UNCHANGED` | Master, Integrated, review, Actual Phase, Plan 참조를 제거하거나 승격하지 않았다. |
| WP mapping | `UNCHANGED` | 13개 행의 Decision/08.x 범위와 순서를 그대로 보존했다. |
| Exact test/evidence | `UNCHANGED` | Test 이름, suite, receipt와 `E-P08-*` 의미를 그대로 보존했다. |
| Owner/gate | `UNCHANGED` | Architecture owner, Phase 07/09/10 owner, implementation/test owner와 scheduler-only status authority를 바꾸지 않았다. |
| Contract/state/failure | `NO_NEW_SEMANTICS` | 새 field/port/state/failure/CLI/provider/public schema/default를 만들지 않았다. |

Correction 01의 다른 계약과 blocker도 그대로다. `Q-BENCH-02`는
`OPEN — EXPERIMENT_REQUIRED`, `C-17`/Phase 13은 Phase 14A benchmark acceptance와
별도 승인 전 `GATED`, `Q-VAR-01`은 `DEFERRED`다. Phase 14A benchmark qualification과
Phase 14B production authority는 분리되며 Phase 08 local pass가 어느 권한도 자동
부여하지 않는다. Phase 07/09 accepted-contract gate, Phase 10 coordinator handoff,
authorization/storage failure/publication 결정과 scheduler-only acceptance 권한을
수정하지 않았다.

## 5. Validation receipt

| 검사 | 방법 | 결과 |
|---|---|---|
| Target before/after | `shasum -a 256`, `git hash-object`, `wc -l` | `PASS`; 2,833줄 유지, metadata/footer hash와 일치 |
| 13개 mapping completeness | Exact requirement ID와 source label scan | `PASS`; 13/13 `Current Architecture`, 대상 old/unqualified Architecture citation 0 |
| Semantic field preservation | Before object↔after target의 Markdown table field 비교 | `PASS`; Source 외 Requirement/WP/Exact test-evidence 변화 0 |
| Source heading/evidence | Current/Dated Architecture heading과 해당 본문 정적 대조 | `PASS`; current heading 존재, dated 번호는 historical로만 기록 |
| Relative links/GFM fragments | Target/report의 local Markdown link resolve와 GitHub heading slug 검사 | `PASS`; missing file/broken fragment 0 |
| Heading hierarchy | Fence 밖 H1~H6 transition 검사 | `PASS`; jump 0 |
| Fences | Line-anchored fence parity | `PASS`; unclosed fence 0 |
| Whitespace/NUL/EOF | Trailing whitespace/tab, NUL, final LF 검사 | `PASS`; 위반 0 |
| Scoped diff | Before Git object↔target와 `/dev/null`↔Correction 02 `--no-index --check` | `PASS`; 허용된 두 문서만 |
| Untracked/scoped preservation | Exact owned-path `git status --short --untracked-files=all`와 human-guide 비소유 파일 aggregate snapshot | `PASS_WITH_CONCURRENT_DRIFT`; target/report만 exact owned untracked path로 확인. 비소유 aggregate는 시작 `9aabac523040dfe3d6751578c94cd27109cd3034db0120fae3c25825f61121cf` 뒤 검증 중 `1c3287939f4b6685ead013c4c53de4ea6e18e8dc8a3e846721933da98fe52c58`, `16b65dc66d4f232ab529965e2051bcab7638a93144d1266e27f50c5c1b3b0f33`으로 concurrent drift. 비소유 파일을 수정·복원하지 않음 |
| Code/POM/test/evidence | 실행/수정하지 않음 | Phase 08 구현·evidence·acceptance 상태를 만들거나 승격하지 않음 |
| Git operation | stage/commit/push/worktree command 미실행 | `PASS` |

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: F-HG-P08-001
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326
TARGET_HASH_AFTER: 624efc9f8619dc3e239c65967ee114750712a9770e90969859f431cafe64d982
