# Phase 01 독립 Review — 내부 표준 입력과 정규화

```yaml
document_status: FINAL
review_status: COMPLETE
review_type: INDEPENDENT_PHASE_DOCUMENT_REVIEW
review_date: 2026-07-28
reviewer_role: independent Phase 01 reviewer
target:
  - docs/implementation/phases/phase-01-canonical-input-normalization.md
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_c_note: path remap to docs/deprecated/*; content hashes not recomputed
document_verdict: ACCEPTED_WITH_APPLIED_CORRECTIONS
phase_acceptance_verdict: BLOCKED_NOT_IMPLEMENTED
phase_acceptance_status: NOT_ACCEPTED
implementation_verdict: NOT_REVIEWABLE_NOT_STARTED
implementation_evidence_status: NOT_PRODUCED
execution_gate: BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE
scheduler_status_change: NOT_AUTHORIZED
whole_file_reciprocal_hashes: NOT_USED
finding_count:
  critical: 0
  major: 4
  minor: 1
applied_correction_count: 5
unapplied_finding_count: 0
fake_evidence_detected: false
code_change_reviewed: false
```

## 1. Scope와 verdict

이 review는 [Phase 01 상세 문서](../phases/phase-01-canonical-input-normalization.md)의 구현 가능성, 원문 계약 보존, Phase 경계, test/evidence failure-detection과 실제 checkout 정합성을 독립 검토한다. Java/POM/공용 문서/다른 Phase/status registry는 수정하지 않았다. Phase 00과 Phase 02는 handoff·overlap·gap 확인을 위해 읽기 전용으로만 사용했다.

Verdict는 **`ACCEPTED_WITH_APPLIED_CORRECTIONS`**다. 발견한 5건은 모두 Phase 01 문서 안에서 source 의미를 새로 결정하지 않고 안전하게 반영했다. 현재 Phase 상세 문서 15/15와 review 15/15가 모두 존재하지만 이 verdict는 **구현 승인이나 Phase acceptance가 아니다**. Phase 00 review는 `PASS_WITH_RESIDUAL_BLOCKERS`로 완료됐어도 Phase 00 implementation/evidence/acceptance가 `NOT_STARTED`/`NOT_PRODUCED`/`PLANNED·NOT_ACCEPTED`이고 `E-P00-BUILD/ARCH/LEGACY`도 없으므로 Phase 01 실행은 계속 `BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE`다. Phase 01 자체도 implementation/evidence/acceptance를 `NOT_STARTED`/`NOT_PRODUCED`/`NOT_ACCEPTED`로 유지한다.

Source 문서의 `REVIEW` metadata는 사용자 선언에 따라 이 문서 작업을 중단시키는 blocker로 취급하지 않았다. 반대로 review 문서 작성이나 기존 placeholder의 `mvn test` 성공을 구현 evidence로 승격하지 않았다.

## 2. 직접 대조한 sources

### 2.1 권위 source

| Source | 직접 확인한 핵심 section | Review 사용 |
|---|---|---|
| [Canonical Master](../../2026-07-31-phase-b-master-design.md) | §1~§8, §15~§17 | Authority, completion, input/domain/time/travel, phase gate, risk/trace |
| [Final Domain Design](../../2026-07-26-domain-design.md) | §1~§7, §16~§18, §20~§21 | Plan/request/vehicle, normalization, travel boundary, error/evidence/deferred |
| [Final Architecture Design](../../2026-07-26-architecture-design.md) | §1~§3, §5~§6 | Java 25/Maven placement, port/dependency, security/test/evidence |
| [Integrated implementation design](../../architecture-domain-implementation-design.md) | §1~§6, §19~§25, §27~§30 | 15 Phase split, exact Phase 01 output/gate, provenance/security/failure/test |
| [Question register](../../master-design-open-questions.md) | §1~§5, exact 28 rows | `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1` |
| [Master Realization Plan](../master-realization-plan.md) | §1~§15, 특히 Phase 00~02/§8~§14 | Current inventory, DAG, test/evidence/DoD/rollback/blocker |
| [Implementation map](../README.md) | §1~§7 | User-locked authority, canonical filename, workflow |

Source fingerprint는 Phase 01 metadata에 기록된 7개 current authority file과 모두 일치했다. [SUPERSEDED Master](../../2026-07-26-master-design.md)는 historical cross-check에만 사용했고 `docs/codex/*`는 authority로 사용하지 않았다.

### 2.2 인접 Phase와 실제 checkout

| Input | 직접 확인한 section/state | 판정 |
|---|---|---|
| [Phase 00](../phases/phase-00-build-architecture-skeleton.md) | §12~§17, 특히 §14 blocker와 §15 Phase 01 handoff | 상세/review actual, review `PASS_WITH_RESIDUAL_BLOCKERS`; implementation/evidence/acceptance는 `NOT_STARTED`/`NOT_PRODUCED`/`PLANNED·NOT_ACCEPTED` |
| [Phase 02](../phases/phase-02-prepared-travel-immutable-problem.md) | §1~§14, 특히 §4 entry, §6 artifact, §10 test, §11 gate, §13 handoff | actual document, implementation `NOT_STARTED`; raw input 재해석 금지 |
| [pom.xml](../../../../pom.xml) | compiler release 25, Enforcer, Surefire 3.5.4, single-project dependencies | Target reactor가 아닌 legacy/current placeholder build |
| `src/main/java` | 6개 Java file | HTTP/GCP adapter 5개 + synthetic `AlnsBatchEngine` 1개 |
| `src/test/java` | `AlnsBatchEngineTest` 1개 | Synthetic candidate test; Phase 01 evidence 아님 |
| Local toolchain | Corretto 25.0.3, Maven 3.9.14 | Version 일치만 확인; reproducible Phase 00 evidence 아님 |

Review 중 `mvn test`는 기존 `AlnsBatchEngineTest` 1건이 passed 1, failed/error/skipped 0으로 성공했다. 이는 current placeholder characterization 보조 사실일 뿐 `E-P00-*` 또는 `E-P01-*`가 아니다.

## 3. Severity summary

| Severity | Count | Applied | Remaining |
|---|---:|---:|---:|
| `CRITICAL` | 0 | 0 | 0 |
| `MAJOR` | 4 | 4 | 0 |
| `MINOR` | 1 | 1 | 0 |
| 합계 | 5 | 5 | 0 |

## 4. Findings

### F-P01-001 — Canonical envelope/profile declarations were not closed over the handoff

| Field | Review record |
|---|---|
| Severity | `MAJOR` |
| Finding | Pre-correction Phase 01 output/handoff는 plan identity, exact customer/profile/version, requested objective preset의 present/omitted 상태, optional mandatory와 approved typed extension input을 명시적으로 보존하지 않았다. Phase 04가 raw input을 다시 읽거나 hidden default를 선택할 여지가 있었다. |
| Evidence — authority | Final Domain §4.1은 plan identity와 customer profile key/version, objective preset key 또는 omission을 canonical input 최소 의미로 요구하고 §4.2는 optional mandatory와 customer extension을 요구한다. Integrated §5.2~§5.3도 같은 field set을 Phase 1 output으로 둔다. |
| Evidence — exact target section | Pre-correction Phase 01 §6.2 `NormalizedInputArtifact`, §7.2 `CanonicalRequestInput`, §7.4 `NormalizedPlanEnvelope`, §11.3 Phase 02 handoff와 §14 traceability에 해당 선언이 없었다. |
| Correction | Phase 01 §2.2~§2.3, §3.2, §6.2~§6.4, §7.2/§7.4, WP-01.1/WP-01.6, §9.2~§10.2, §11.3, §12~§15에 typed declaration, omission preservation, semantic fingerprint, failure fixture와 handoff를 추가했다. Profile binding과 preset default 해소는 계속 Phase 04 소유로 남겼다. |
| Applied | `YES` |
| Residual risk | Public field shape/version/authorization은 여전히 `OPEN`이며 Product·API·Data/Profile owner 승인이 필요하다. Phase 02 구현/review는 새 handoff field를 손실 없이 `ProblemInstance` lineage로 전달해야 한다. |

### F-P01-002 — Wait/resource/full-arc semantics lacked executable handoff oracles

| Field | Review record |
|---|---|
| Severity | `MAJOR` |
| Finding | Scope에는 `waitInDepot`가 있었지만 proposed type과 exact positive/negative test가 없었고, vehicle/global `maxStopCnt/maxDriveTime/maxDriveDist`의 typed absence·invalid/overflow test도 없었다. Integrated Phase 1 gate의 full-arc next-window rule은 enum/미래 fixture 언급만 있고 exact owner test/evidence가 없었다. |
| Evidence — authority | Canonical Master §7.3은 `waitInDepot`, route 전체 resource와 absent-not-sentinel을 고정한다. Integrated §5.4/§5.6/§5.9는 trip/wait/resource와 full-arc next-window를 Phase 1 meaning/gate에 포함한다. Realization Plan Phase 01 step 4~5와 §15 `REQ-TIME`도 Phase 01/03 공동 evidence를 요구한다. |
| Evidence — exact target section | Pre-correction Phase 01 §2.2에는 wait만 있었고, §7.4에는 `WorkArcPolicy`/`TripPolicy`만 있었으며, WP-01.4와 §9.2/§9.3에는 wait/resource/full-arc exact failure-detection이 없었다. |
| Correction | §7.4에 proposed `DepotWaitPolicy`와 typed route-resource limits를 추가하고 WP-01.4, §9.2~§9.4, §10.1~§10.2, §11~§15에 wait/resource/full-arc handoff test·fixture·evidence를 추가했다. Full-arc propagation 자체는 여전히 Phase 03 비범위로 보존했다. |
| Applied | `YES` |
| Residual risk | Phase 01은 policy와 exact handoff tuple만 검증한다. 실제 full-arc restart, depot/customer wait와 vehicle/global limit 적용은 Phase 03의 independent hand oracle이 통과해야 닫힌다. |

### F-P01-003 — Targeted Maven commands were not reactor-safe and could also false-pass after a naive fix

| Field | Review record |
|---|---|
| Severity | `MAJOR` |
| Finding | Pre-correction WP-01.1~WP-01.5의 `-Dtest=... -am` 명령은 target test가 없는 prerequisite module에서 Surefire no-match failure를 낼 수 있었다. 단순히 `failIfNoSpecifiedTests=false`만 추가하면 target owner module에서도 오타/0-test가 성공할 수 있어 evidence가 가짜 green이 될 수 있었다. |
| Evidence — actual command | Current Surefire 3.5.4에서 `mvn -Dtest=NoSuchPhase01Test test`는 “No tests matching pattern”으로 exit 1, 같은 명령에 `-Dsurefire.failIfNoSpecifiedTests=false`를 주면 tests 0인 채 exit 0이었다. |
| Evidence — exact target/adjacent section | Pre-correction Phase 01 §8 WP-01.1~WP-01.5 명령에는 reactor no-match 처리가 없었다. Phase 02 §9 WP 명령은 이미 `-Dsurefire.failIfNoSpecifiedTests=false`를 사용해 인접 계획의 의도를 보여준다. |
| Correction | 모든 Phase 01 targeted command에 `-Dsurefire.failIfNoSpecifiedTests=false`를 추가하고 §9.4에 owner module의 모든 exact test class XML report와 method execution record 존재, 0-test/report 누락/name typo gate failure를 추가했다. Full `verify`는 그대로 유지했다. |
| Applied | `YES` |
| Residual risk | Target reactor/module은 아직 존재하지 않아 future exact command 자체는 실행하지 못했다. Phase 00 accepted module path와 Surefire report layout이 확정되면 WP-01.0에서 명령을 재검증해야 한다. |

### F-P01-004 — PII/redaction requirement had no exact failure detector

| Field | Review record |
|---|---|
| Severity | `MAJOR` |
| Finding | Phase 01은 raw PII/value를 message/log에 넣지 않는다고 선언했지만 exact canary fixture/test/report가 없어 구현이 raw input 또는 address/email을 rejection evidence에 흘려도 gate가 잡지 못했다. |
| Evidence — authority | Integrated §20은 raw address/PII와 full input의 log/trace 노출을 금지하고 §22.4는 independent corruption fixtures를 요구한다. Realization Plan §9.2는 PII 저장을 금지하며 §13은 redaction test를 security prevention으로 둔다. |
| Evidence — exact target section | Pre-correction Phase 01 §7.6은 PII 금지를 서술했지만 §9.2/§9.3, §10.2 `E-P01-ERROR`와 §14 traceability에 exact redaction detector가 없었다. |
| Correction | WP-01.6, §9.2 `rejectionEvidenceRedactsRawValuesAndInputBytes`, §9.3 `pii-redaction` canary, §10.1~§10.2, §11/§13/§14/§15에 failure·evidence·trace를 추가했다. |
| Applied | `YES` |
| Residual risk | Phase 01 test는 자체 report/log call만 검증한다. HTTP, storage, cloud telemetry와 operational retention은 Phase 08~11 security/contract/rehearsal에서 다시 검증해야 한다. |

### F-P01-005 — Current inventory conflated Phase 00 document absence with evidence absence

| Field | Review record |
|---|---|
| Severity | `MINOR` |
| Finding | Pre-correction Phase 01 §1/§4는 Phase 00 상세 문서와 review/evidence가 모두 없다고 기록했지만 Phase 00 상세 문서는 실제 존재했다. Entry blocker의 이유가 부정확했다. |
| Evidence — historical snapshot (`APPEARED_DURING_AUTHORING`) | 최초 review 시점에는 Phase 00 file만 non-empty였고 Phase 00 §15가 Phase 01 actual file을 handoff 대상으로 확인했으며 `docs/implementation/reviews/phase-00-review.md`와 `E-P00-*`는 존재하지 않았다. 이후 Phase 00 review가 생성됐으므로 이 문장은 live inventory가 아니라 당시 finding 근거로만 보존한다. |
| Evidence — exact target section | Pre-correction Phase 01 §1, §4.1 `Phase 00 상세/review`, §4.2 Working tree inventory. |
| Correction | Phase 00 상세를 `actual`, review/evidence를 `absent`로 분리하고 file existence가 acceptance가 아님을 §1/§4에 명시했다. |
| Applied | `YES` |
| Residual risk | 현재 Phase 00 review는 존재하지만 `E-P00-*`와 accepted implementation/phase evidence는 여전히 실제 blocker다. 이 finding 수정은 실행 상태를 `READY`나 `ACCEPTED`로 올리지 않는다. |

## 5. Axis별 PASS 근거와 residual risk

| Review axis | Verdict | Evidence | Residual risk |
|---|---|---|---|
| Authority/status/open decisions | `PASS` | User-locked order, current fingerprints, `26/1/1`, `Q-INFRA-01 RESOLVED`, `Q-BENCH-02 OPEN`, `C-17 GATED`, `Q-VAR-01 DEFERRED`를 보존 | Authority source 변경 시 impact review 재실행 |
| Contract/invariant/boundary | `PASS AFTER CORRECTION` | Numeric/time/service/identity/compatibility/trip/travel-source 계약과 no-dense/no-prepared/no-solver 경계가 exact trace를 가짐 | Public wire/schema와 approved extension type은 open |
| Gate/owner/state/identity/lifecycle | `PASS` | Phase 00 evidence blocker, `SEALED/REJECTED`, raw/semantic/envelope identity, no partial artifact, no fake acceptance | Scheduler task ID와 Phase 00 accepted implementation evidence 미지정 |
| Phase 00→01→02 handoff | `PASS AFTER CORRECTION` | Phase 00 actual-document/evidence distinction, Phase 01 complete normalized handoff, Phase 02 raw-reparse 금지 | Phase 02 §13.1의 “nodes”는 Phase 01의 service declaration으로만 해석해야 하며 dense `SolverNodeId`는 Phase 02 owner가 생성 |
| Test/fixture/oracle/failure detection | `PASS AFTER CORRECTION` | Boundary/property/oracle, exact negative fixtures, redaction, target report existence와 failed/skipped 0 | Future target tests와 fault injection은 구현 후 실제 실행 필요 |
| Evidence/provenance/fake evidence | `PASS` | Digest-protected four-key bundle, exact command/exit/count, no console/target/source-file substitution | Evidence bundle 자체는 아직 없음 |
| Actual Java/Maven structure | `PASS` | Single root POM, Java 25.0.3/Maven 3.9.14, main 6/test 1, GCP/Jackson root dependency와 synthetic engine을 정확히 기록 | Target modules/architecture rules가 아직 없음 |
| Rollback/failure/security | `PASS AFTER CORRECTION` | Side-effect-free draft discard, typed reject, last accepted WP rollback, PII canary/redaction | End-to-end transport/storage/cloud security는 later Phase |
| Observability/reproducibility | `PASS` | Stable ordering, checked arithmetic, policy/schema/fingerprint, PII-safe evidence와 no elapsed/random hidden input | Canonical encoding algorithm은 proposed internal ADR 대기 |
| Links/traceability/document status | `PASS` | Canonical relative links, source→requirement→test/evidence table, document vs implementation status 분리 | Global README/progress의 planned labels는 scheduler/public-doc owner 범위라 이 review에서 수정하지 않음 |

## 6. Applied change summary

Phase 01 문서에만 다음을 반영했다.

1. 최초 review 당시 Phase 00 상세 문서 존재와 review/evidence 부재를 분리했다. 이 기록은 `APPEARED_DURING_AUTHORING` historical snapshot이며 live inventory는 현재 15/15 phase·review 상태로 별도 갱신했다.
2. Plan identity, exact customer/profile/version, preset request 또는 omission, mandatory, approved typed extension을 artifact/fingerprint/handoff에 추가했다.
3. Profile binding/default는 Phase 04 소유임을 명시해 hidden default를 막았다.
4. `waitInDepot`, vehicle/global route-resource typed absence와 full-arc handoff oracle을 type/work package/test/evidence에 추가했다.
5. Reactor targeted test 명령을 no-match-safe하게 만들고 owner test report/0-test 방지 gate를 추가했다.
6. PII/raw input canary redaction test, failure fixture, evidence와 traceability를 추가했다.
7. Phase 01 review 링크를 actual canonical review file로 갱신했다.

이 변경은 canonical numeric/time/travel/profile 의미, Phase numbering, source authority, question 상태 또는 public API를 새로 결정하지 않는다.

## 7. Residual blockers와 restart

| Blocker/gate | Owner | 막는 범위 | Last safe point | Restart/해제 조건 |
|---|---|---|---|---|
| Phase 00 accepted evidence | Phase 00 build owner + independent reviewer + scheduler | Phase 01 production source 전체 | 이 상세/review와 test/fixture 설계 | 현재 review verdict만으로 부족; `E-P00-BUILD/ARCH/LEGACY`, Phase 00 phase acceptance와 exact accepted module/build command |
| Public wire/schema/version, alias/unknown policy | Product·API·Data + Domain·Input | Production external adapter/compatibility promise | Adapter SPI와 test-only fixtures | Versioned field/alias/coercion/unknown policy와 negative fixtures 승인 |
| Public profile/preset/mandatory/extension field shape | Product·API·Data + Profile owner | External compatibility/authorization promise | Internal typed declaration + omission contract | Versioned type/omission/authorization 승인 |
| Canonical comparator/encoding | Domain·Architecture | Stable cross-version fingerprint promise | Versioned proposed internal comparator | ADR, replay/migration, collision/framing test 승인 |
| Current decimal Win `D/U` | Input·Matrix + Benchmark | 해당 fixture의 canonical/official use | Negative rejection fixture | Compliant integer matrix 또는 explicit contract migration |
| `Q-BENCH-02` | Benchmark·Quality | Phase 14 official manifest | Phase 01 unaffected | Calibration/measured review/explicit approval |
| `C-17` | Product·Algorithm·Architecture + OR-Tools/Legal/Supply-chain/Security/Operations/Cost | Phase 13 | ALNS-only critical path | Phase 06/07/08 accepted + Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, C-17 scope + OR-Tools version/config/native/OSS-license/SBOM/security/operations/cost/admission/fallback/rollback approval |
| `Q-VAR-01` | Product·Domain·Algorithm | Optional variants | Current pair/single-trip facts | Variant/fixture/core-impact evidence + separate approval |
| Multi-trip/rotation | Product·Domain·Algorithm | Non-single-trip input | Reject unsupported rotation | Trip/reset/depot/resource contract + approval |

외부 권위가 필요한 항목에는 값이나 default를 추가하지 않았고, target 문서의 blocker/owner/last safe/restart만 보강·유지했다.

## 8. Validation

최종 검증은 다음 범위를 대상으로 수행했다.

```text
docs/implementation/phases/phase-01-canonical-input-normalization.md
docs/implementation/reviews/phase-01-review.md
```

| Check | Command/방법 | Result |
|---|---|---|
| Non-empty | 두 파일에 `test -s` | `PASS`, exit 0 |
| Required structure | Phase 문서와 review의 canonical `##` heading `rg`; finding field `rg` | `PASS`; Phase 필수 heading 11개, review 필수 heading 8개, findings 5개 |
| Relative links | 두 파일의 Markdown link를 각 파일 directory 기준으로 resolve하고 target existence 검사 | `PASS`; missing target 0 |
| Trailing whitespace | `rg -n '[[:blank:]]+$'` | `PASS`; match 0 |
| Conflict marker | `rg -n '^(<<<<<<<|=======|>>>>>>>)'` | `PASS`; match 0 |
| Tracked diff whitespace | `git diff --check -- <두 파일>` | `PASS`, exit 0 |
| Untracked-aware whitespace | 각 파일에 `git diff --no-index --check /dev/null <file>` | `PASS`; content difference 때문에 exit 1이지만 whitespace diagnostic 0 |
| Source fingerprints | Current authority 7개와 historical cross-check 1개의 SHA-256 재계산 | `PASS`; Phase 01 metadata와 exact match |
| Actual legacy test | `mvn test` | `PASS`; tests 1, failures 0, errors 0, skipped 0; Phase evidence로 사용하지 않음 |
| Surefire detector characterization | nonexistent `-Dtest`를 flag 없이/있이 실행 | expected exit 1 / exit 0; §9.4 owner-report gate 필요성 확인 |
| Scope | `git status --short --untracked-files=all` + 작업 기록 | 이 작업의 edit는 Phase 01 상세와 이 review뿐이며 Java/POM/다른 Phase/review 수정 없음 |

문서 검증 성공은 Phase 01 구현/evidence 성공을 뜻하지 않는다. Workspace의 `docs/implementation/*`는 기존부터 untracked 상태이므로 일반 `git diff --check`가 이를 검사하지 않는 한계를 no-index check로 보완했다.

## ALNS-first direction revision addendum

Task `019fa901-8776-7f61-b467-a8c6595b970d`에서 C-17 restart 조건에 Phase 06/07/08
accepted evidence와 Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`를 추가한 변경을
검토했다. Phase 01은 ALNS-only 입력 경로를 유지하고 기존 review verdict,
implementation, acceptance와 evidence 상태는 변하지 않는다.
