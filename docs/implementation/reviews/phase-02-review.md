# Phase 02 independent review

```yaml
document_status: FINAL
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
phase: "02"
phase_name: prepared-travel-immutable-problem
review_date: 2026-07-28
reviewer_role: independent Phase 02 reviewer
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
reviewed_target: docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md
review_scope:
  allowed_changes:
    - docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md
    - docs/implementation/reviews/phase-02-review.md
  adjacent_phases: READ_ONLY
  java_maven_inventory: READ_ONLY
baseline_commit: 3424277c9c74f8151a83be056a07dd4659331beb
document_verdict: PASS_AFTER_APPLIED_CORRECTIONS
phase_acceptance_verdict: BLOCKED_NOT_IMPLEMENTED
implementation_evidence_verdict: NOT_AVAILABLE
scheduler_status_change: NOT_AUTHORIZED
whole_file_reciprocal_hashes: NOT_USED
verification_status: PASS
severity_summary:
  HIGH: 3
  MEDIUM: 3
  LOW: 0
  PASS: 4
```

이 review의 `PASS_AFTER_APPLIED_CORRECTIONS`는 Phase 02 상세 문서가 현재 authority와 phase boundary에 맞는 실행 계획이 되었다는 뜻이다. 실제 Java 구현, test, evidence bundle과 accepted predecessor가 없으므로 Phase 02 자체는 `ACCEPTED`, `READY` 또는 `IN_PROGRESS`가 아니며 현재 verdict는 `BLOCKED_NOT_IMPLEMENTED`다.

## 1. Sources reviewed

Whole-file reciprocal hash는 만들지 않았다. Source commit과 아래 exact section/path를 대조했다.

| Source | 대조 범위 | Review 사용 |
|---|---|---|
| [Implementation README](../README.md) | §1~§7, 특히 §3·§7 | 사용자 고정 authority, `REVIEW` 비중단, planned/actual/status 규칙 |
| [Canonical Master](../../master-design.md) | §1.5, §2.3~§2.4, §4.1~§4.6, §5~§8, §13, §14.1, §15.3~§15.4, §16~§17 | Travel/immutable authority, lifecycle, RM-1/2 boundary, rollback·risk·question status |
| [Question register](../../master-design-open-questions.md) | §1~§4; `Q-NUM-01~03`, `Q-MTX-01~03`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | Exact resolved/open/deferred 상태와 hidden-default 금지 |
| [Final Domain Design](../../2026-07-26-domain-design.md) | §3~§7, §16~§18 | Normalization→travel→immutable model, typed failure, acceptance evidence |
| [Final Architecture Design](../../2026-07-26-architecture-design.md) | §2, §5.2~§5.6, §6 | Java 25/Maven boundary, immutable artifact, provider isolation, security/test |
| [Integrated implementation design](../../architecture-domain-implementation-design.md) | §5~§7, §12, §19~§25, §27 | Phase 01/02/03/08 ownership, provenance, failure/retry, test/corruption/invariants |
| [Master Realization Plan](../master-realization-plan.md) | §2~§4, Phase 01~03, §8~§15 | Current inventory, gates, evidence/DoD, rollback, observability, traceability |
| [Phase 01](../phases/phase-01-canonical-input-normalization.md) | §2.3, §5.1, §6.2, §11.3, §12 | `NormalizedInputArtifact` 단일 handoff와 Phase 02 소유 dense node/travel 경계 |
| [Phase 03](../phases/phase-03-route-propagation-evaluation-kernel.md) | §3~§4, §7, §9, §14.1, §15 | Phase 02 artifact acceptance, no-fallback consumer, equality/corruption expectation |
| Actual checkout | `.sdkmanrc`, root `pom.xml`, `src/main/java`, `src/test/java`, `data/` inventory | Java 25/Maven 3.9.14, 단일 project, main 6/test 1, GCP/Jackson root dependency, target artifact 부재 |
| [SUPERSEDED Master](../../2026-07-26-master-design.md) | metadata와 travel/history 관련 절 | Historical regression cross-check only |

`docs/codex/*`는 historical only로 유지했으며 current authority나 수정 대상으로 사용하지 않았다.

## 2. Verdict

### 2.1 Document verdict

`PASS_AFTER_APPLIED_CORRECTIONS`

아래 6개 finding은 Phase 02 target 문서에 안전하고 명백한 범위로 직접 반영했다. 수정 뒤 문서는:

- Phase 01의 accepted `NormalizedInputArtifact`만 source authority로 소비한다.
- Physical-location directed `M²`, vehicle-resolved time, self `0/0`, integer/rounding/default와 no-fallback을 보존한다.
- Generic Phase 02 evidence와 official travel/benchmark authority를 분리한다.
- Phase 08 application/provider port를 미리 소유하지 않는다.
- Test가 0건이어도 green이 되는 command를 허용하지 않는다.
- Safe aggregate observability, corruption, rollback과 exact handoff를 요구한다.

### 2.2 Phase verdict

`BLOCKED_NOT_IMPLEMENTED`

현재 root는 single Maven project이고 target `rpdptw-core`, `PreparedTravel`, `ProblemInstance`, Phase 02 test/evidence가 없다. Phase 00/01 accepted evidence도 없으며 Great Circle exact function/version과 typed source policy approval도 없다. 따라서 문서 review 통과를 구현/phase acceptance로 승격하지 않는다.

## 3. Findings

### F-P02-01 — HIGH — Generic acceptance가 official travel authority에 잘못 종속됨

- **Finding:** Target은 approved integer fixture 또는 official snapshot이 없으면 Phase 02 acceptance integration과 exit를 막았다. 이는 generic RM-1 계약 구현은 current decimal Win fixture 때문에 막히지 않는다는 canonical Master와 충돌했다.
- **Evidence:** 수정 전 target §4.1, WP-02-6, §10.4, §11.1, §12; [Canonical Master §15.3](../../master-design.md#153-rm-1--versioned-input-immutable-domain과-prepared-travel)은 current Win fixture 비준수가 generic RM-1 구현을 막지 않고 official `RM-6` 사용만 막는다고 명시한다. [Question register §4](../../master-design-open-questions.md#4-남은-gate)도 compliant integer matrix를 official baseline gate로 둔다.
- **Correction:** Generic Phase 02는 `TEST_ONLY_HAND_ORACLE`의 exact integer values와 독립 oracle로 검증하도록 바꾸고, approved fixture/official snapshot은 별도 제공된 경우에만 scope/digest를 기록하는 optional authority integration으로 분리한다.
- **Applied:** **YES.** Target §4.1, WP-02-6, §10.4, §11.1, §11.2와 §12에 반영했다.
- **Residual risk:** Test-only oracle은 official travel 또는 benchmark evidence가 아니다. Current Win decimal `D/U`의 canonical/official 사용은 계속 차단된다.

### F-P02-02 — HIGH — Phase 08 port pull-forward와 detached travel source가 이중 authority를 만듦

- **Finding:** Target은 Phase 02에서 application-owned `TravelSnapshotSourcePort`, provider adapter contract와 detached `TravelSourceBatch`를 정의·시험하려 했다. 동시에 Phase 01 artifact를 유일한 입력이라고 선언해 source authority가 두 갈래였다. Phase 01이 넘기지 않는 solver `node`도 handoff 목록에 포함했다.
- **Evidence:** 수정 전 target §5.2, §6.1, §7.1, §8, WP-02-2, §10.1, §13.1; [Phase 01 §6.2](../phases/phase-01-canonical-input-normalization.md#62-산출물)와 [§11.3](../phases/phase-01-canonical-input-normalization.md#113-phase-02-handoff)은 sparse travel declaration이 sealed된 `NormalizedInputArtifact` 하나를 넘기고 dense node는 Phase 02가 만든다고 정한다. [Integrated §6](../../architecture-domain-implementation-design.md#6-phase-2--travel-preparation과-immutable-probleminstance)은 pure preparation을, [§12](../../architecture-domain-implementation-design.md#12-phase-8--application-ports와-local-reference-runtime)은 application port를 Phase 08에 배치한다.
- **Correction:** Detached batch와 application/provider tree/signature/contract test를 제거하고, accepted Phase 01 artifact 내부 travel declaration만 읽는 `TravelSourceHandoffTest`로 바꿨다. Handoff의 `nodes`를 `request/service declarations`로 고쳤다.
- **Applied:** **YES.** Target §2.2~§2.3, §5.2, §6.1, §7.1, §8, WP-02-2, §10.1, §13.1, §14에 반영했다.
- **Residual risk:** Phase 08 이후 실제 acquisition port를 설계할 때 upstream partial/unavailable과 canonical intentional `Absent`가 다시 섞이지 않도록 별도 adapter contract가 필요하다.

### F-P02-03 — HIGH — Targeted Maven command가 test 0건을 green으로 허용

- **Finding:** 모든 targeted command에 `-Dsurefire.failIfNoSpecifiedTests=false`가 있어 typo, module drift 또는 test 누락 시 0 tests로 성공할 수 있었다.
- **Evidence:** 수정 전 target WP-02-1~5 command; target §10.2와 §11.3은 required tests의 failed/skipped 0뿐 아니라 실제 defect detection을 요구한다. [Master Realization Plan §8](../master-realization-plan.md#8-공통-테스트-전략)과 §9는 exact command/count/exit evidence를 요구한다.
- **Correction:** 모든 Phase 02 targeted command에서 false-pass 옵션을 제거했다. Target module/test가 아직 없으므로 현재 실행하면 실패하는 것이 truthful future-red다.
- **Applied:** **YES.** Target WP-02-1~5 command에 반영했다.
- **Residual risk:** Phase 00 module naming이 달라지면 command는 review와 함께 갱신해야 한다. Test 이름이나 module이 없을 때 command 실패를 우회해서는 안 된다.

### F-P02-04 — MEDIUM — Whole-file source hash가 reciprocal churn과 거짓 안정성을 만듦

- **Finding:** Target metadata와 evidence 절이 canonical 문서 전체 SHA-256을 반복 소유했다. Section과 의미가 변하지 않은 unrelated edit에도 drift가 발생하고, adjacent phase가 이를 서로 복제하면 reciprocal update가 된다.
- **Evidence:** 수정 전 target YAML의 whole-file source-hash map, WP-02-0, evidence metadata와 restart 문장. Review scope는 whole-file reciprocal hash를 금지한다.
- **Correction:** Whole-file hashes를 제거하고 source commit + exact cited section/version/status + requirement/test impact review로 교체했다.
- **Applied:** **YES.** Target metadata, WP-02-0, §11.2, §12와 §14에 반영했다.
- **Residual risk:** Read-only Phase 01/03에는 기존 whole-file hash metadata가 남아 있다. 이 review는 인접 Phase를 수정할 권한이 없으므로 각 Phase review에서 별도 정리해야 한다.

### F-P02-05 — MEDIUM — Traceability anchor가 실제 Domain heading을 가리키지 않음

- **Finding:** Target §14의 Domain 링크가 `#travel`, `#immutable-model`을 사용해 실제 §6/§7 heading anchor와 일치하지 않았다.
- **Evidence:** [Final Domain §6](../../2026-07-26-domain-design.md#6-travel-preparation), [Final Domain §7](../../2026-07-26-domain-design.md#7-immutable-solver-model); 수정 전 target §14.
- **Correction:** Exact heading anchor로 교체하고 Phase 01 handoff/Phase boundary link를 추가했다.
- **Applied:** **YES.** Target §14에 반영했다.
- **Residual risk:** Markdown renderer별 비ASCII anchor 차이는 최종 local link/anchor check로 계속 검증해야 한다.

### F-P02-06 — MEDIUM — Phase-local observability contract가 report type 이름에만 머묾

- **Finding:** Target API에는 `TravelPreparationReport`가 있었지만 어떤 safe aggregate가 필수인지, artifact coverage와 어떻게 대조하는지, 무엇을 log/fingerprint에서 제외하는지 정의하지 않았다.
- **Evidence:** 수정 전 target §7~§8/§10; [Integrated §19.3](../../architecture-domain-implementation-design.md#193-correlation-fields)은 travel/problem fingerprint와 integrity 관측을, [§20](../../architecture-domain-implementation-design.md#20-security와-tenant-boundary)은 raw address/PII/secret 비노출을 요구한다.
- **Correction:** Safe aggregate report의 identity/policy/count/failure fields, high-cardinality/PII 금지, semantic fingerprint 비영향과 independent count test를 추가했다.
- **Applied:** **YES.** Target §7.6, file tree, WP-02-4, §10.1/§10.4, §11.2, §14에 반영했다.
- **Residual risk:** Telemetry backend, duration와 platform attempt correlation은 Phase 08 이후 owner가 구현해야 한다. Core report는 backend를 호출하지 않는다.

## 4. PASS findings

Finding이 없는 검사축도 evidence와 residual risk를 명시한다.

### P-P02-01 — PASS — Travel contract와 numeric/default invariant

- **Finding:** 없음.
- **Evidence:** Target §3.1~§3.2와 §7.2~§7.4는 [Canonical Master §7.2](../../master-design.md#72-fixed-point와-checked-arithmetic), [§8](../../master-design.md#8-directed-distancetime-matrix-계약), `Q-MTX-01~03`의 directed physical-location key, integer `D/U`, self `0/0`, Great Circle `HALF_UP`, vehicle-specific generated `U` `CEILING`, missing-only `45 km/h`, invalid-present rejection과 no reverse/symmetric/lazy fallback을 보존한다.
- **Correction:** 없음.
- **Applied:** N/A.
- **Residual risk:** Great Circle Earth model/function/library/version/reference vector는 authority에 없으므로 target §12 blocker가 해제되기 전 missing-`D` implementation/green/exit은 불가하다.

### P-P02-02 — PASS — Artifact ownership, identity lifecycle, failure/corruption/rollback

- **Finding:** 없음.
- **Evidence:** Target §6.3~§6.5, §7.4~§7.6, §9, §10, §11~§13은 method-local draft 비노출, all-or-nothing freeze, defensive copy, same identity/different bytes rejection, fault/cancel discard, cache non-authority, corruption fixtures, last-safe-point와 restart를 명시한다. 이는 [Canonical Master §4.5~§4.6](../../master-design.md#45-상태와-산출물의-생명주기), [Integrated §21~§23](../../architecture-domain-implementation-design.md#21-failure와-retry-matrix)과 일치한다.
- **Correction:** 없음.
- **Applied:** N/A.
- **Residual risk:** 계획된 type/test가 아직 존재하지 않아 defect detection evidence는 0이다. `target/` 산출물이나 placeholder test를 evidence로 사용할 수 없다.

### P-P02-03 — PASS — Phase 01/03 overlap-gap와 actual inventory truthfulness

- **Finding:** 없음(위 F-P02-02 correction 후).
- **Evidence:** Target §5.1은 root POM 하나, Java 25/Maven 3.9.14, main 6/test 1, GCP/Jackson root dependency와 target module/artifact 부재를 실제 checkout과 일치하게 기록한다. Target §13.1은 Phase 01 accepted artifact만, §13.2는 Phase 03에 immutable problem/travel refs와 equality proof만 넘긴다. [Phase 03 §4](../phases/phase-03-route-propagation-evaluation-kernel.md#4-entry-gate와-확인-방법)도 mismatch/coverage failure 시 propagation 금지를 요구한다.
- **Correction:** Phase 01 actual-document/registry-planned 표현과 handoff의 node ownership을 명확히 했다.
- **Applied:** **YES.**
- **Residual risk:** Phase 00 ADR가 proposed module/package name을 바꾸면 target tree와 commands를 semantic owner 유지 조건으로 갱신해야 한다.

### P-P02-04 — PASS — OPEN/GATED/DEFERRED와 official authority 보존

- **Finding:** 없음.
- **Evidence:** Target §1.2, §2.3, §4.1, §10.4, §11~§12는 `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`, `C-17 GATED TARGET`, `Q-VAR-01 DEFERRED`, current decimal Win fixture와 official travel authority를 production default로 채우지 않는다. `Q-INFRA-01`은 최신 `RESOLVED` AWS target으로 읽되 provider SDK를 core에 넣지 않는다.
- **Correction:** Generic test-only evidence와 official authority를 분리했다.
- **Applied:** **YES.**
- **Residual risk:** Official integer travel snapshot, benchmark values, public API/fingerprint contract, performance thresholds과 optional branches는 각 owner approval/restart 전 계속 비활성이다.

## 5. Active blocker ledger

External authority는 blocker의 owner, last safe point와 restart condition으로만 기록한다.

| Blocker | Owner | Last safe point | Restart condition |
|---|---|---|---|
| Phase 00 accepted module baseline 부재 | Architecture + scheduler | Current root single-project checkout와 reviewed Phase 02 document | `E-P00-BUILD/ARCH` + accepted review + exact module contract |
| Phase 01 accepted artifact 부재 | Phase 01 + scheduler | Read-only canonical sources와 target test design | `E-P01-*`, immutable `NormalizedInputArtifact` identity + accepted review |
| Great Circle function/version 부재 | Input·Matrix + Architecture | Provided-complete/test-only contract review | Function/Earth model/precision/reference vectors + approval |
| Typed source/generation policy 부재 | Input·Matrix + Phase 01 owner | Test-only hand oracle | Source allowlist/priority/declared absence/source identity + approval |
| Scheduler task/implementation owner 미확정 | Scheduler | Review-only state | Exact task ID/owner와 entry evidence 확인 |
| Current Win decimal `D/U` | Input·Matrix + Benchmark | Raw fixture read-only | Compliant integer matrix 또는 explicit contract migration approval |

다음은 generic Phase 02 blocker가 아니다.

- Approved integer fixture/official snapshot 부재: 해당 artifact의 official/integration/benchmark 사용만 막는다.
- `Q-BENCH-02`: Phase 14 official manifest 수치만 막는다.
- `C-17`: Phase 13 route pool/MIP만 막는다.
- `Q-VAR-01`: optional variant 질문/활성화만 막는다.

## 6. Required evidence before any Phase acceptance

현재 다음 evidence는 모두 **NOT PRODUCED**다.

| Evidence | Required minimum | Current |
|---|---|---|
| `E-P02-TRAVEL` | Approved Great Circle/source policy, exact test-only oracle identity, total coverage, safe report, formula/asymmetry/no-fallback/corruption/repro/security results | `NOT_PRODUCED` |
| `E-P02-DENSE-ID` | Canonical mapping/bijection/property/overflow/reference report | `NOT_PRODUCED` |
| `E-P02-PROBLEM` | Problem/travel equality, pair/reference/range/no-alias/corruption, Phase 03 handoff | `NOT_PRODUCED` |

Acceptance에는 exact command/exit code, non-zero test counts, failed/skipped counts, fixture/oracle identity, build/toolchain, source commit + cited section/version/status, limitation, reviewer verdict, artifact digest, rollback과 handoff identity가 모두 필요하다.

## 7. Review verification

Final verification은 다음 두 허용 파일만 대상으로 수행했다.

```text
docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md
docs/implementation/reviews/phase-02-review.md
```

| 검사 | 결과 | Evidence |
|---|---|---|
| 두 파일 non-empty | `PASS` | `test -s` 두 경로 |
| Markdown/YAML fence와 heading structure | `PASS` | Fence count 짝수, heading/link parser error 0 |
| Local path와 heading/explicit anchor | `PASS` | 두 파일의 모든 relative Markdown link target/anchor 누락 0 |
| Trailing whitespace | `PASS` | 두 파일 대상 `[[:blank:]]+$` match 0 |
| Whole-file reciprocal hash | `PASS` | 두 파일에 whole-file hash map과 64-hex digest 0 |
| Test false-pass option | `PASS` | Target active command에서 `failIfNoSpecifiedTests=false` 0 |
| `git diff --check` | `PASS` | 두 허용 경로 대상 exit `0`; untracked content의 `--no-index --check` whitespace output 0 |
| Actual inventory reconfirm | `PASS` | commit `3424277`, Java `25.0.3`, Maven `3.9.14`, main Java `6`, test Java `1` |
| Review write scope | `PASS` | Reviewer가 수정한 source는 target Phase 02 하나이며 새로 만든 파일은 이 review 하나다. Adjacent Phase/Java/Maven은 read-only였다. |

이 review 문서 자체는 구현 test/evidence가 아니며 root `mvn verify`를 Phase 02 green으로 대체하지 않는다. Root build는 target module/test 부재 상태를 phase evidence로 오인하지 않기 위해 이 review에서 실행하지 않았다.
