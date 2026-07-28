# Phase 13 사람용 구현 가이드 독립 리뷰

```yaml
phase: "13"
review_type: INDEPENDENT_HUMAN_IMPLEMENTATION_GUIDE_REVIEW
reviewer_task_nature: author와 분리된 새 작업의 read-only target 검토
target: docs/implementation/human-guides/phases/phase-13-human-implementation-guide.md
target_git_hash_object: a93295937bc61a79e688f5ae6386d1c6592171ca
target_sha256: 92ed2d46ddd7a74975b8dbc149af49d36eca917aa282cc562058f8fdae7c6f14
target_lines: 1959
target_tracked_at_head: false
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
head_branch: codex-implementation
inventory_snapshot_at: "2026-07-29T01:57:34+0900"
inventory_timezone: Asia/Seoul
inventory_status_sha256_before_output: 7ff2b5b6c96f7a76c6b2e21e904ed1a7f1cfa3c18f11682221c63a537475a4fb
inventory_status_git_blob_before_output: 85277a4a7ef615383ac0665dd295f7fed09ad208
java_observed: Amazon Corretto 25.0.3
maven_observed: Apache Maven 3.9.14
review_date: 2026-07-29
verdict: CHANGES_REQUIRED
target_changes_required: true
finding_counts:
  critical: 0
  high: 4
  medium: 1
  low: 0
required_correction_findings:
  - HG13-R001
  - HG13-R002
  - HG13-R003
  - HG13-R004
  - HG13-R005
implementation_gate_status_observed: C17_GATE_CLOSED_NOT_STARTED_NOT_ACCEPTED
phase14a_acceptance_receipt_observed: NOT_PRODUCED
phase14b_production_authority_observed: NOT_GRANTED
target_modified_by_reviewer: false
```

## 1. 결론

Target은 Phase 13의 가장 위험한 gate 대부분을 명확히 보존한다. 특히
`Phase 06/07/08 accepted → Phase 14A ALNS_BENCHMARK_ACCEPTANCE_RECEIPT →
별도 C-17/backend/license/native/security/operations/cost 승인`이라는 AND gate,
gate-closed no-load/absence semantics, Phase 12의 조건부 bounded evidence,
immutable route pool, exact projection 또는 typed skip, direct Java CP-SAT 격리,
fresh materialization/full evaluation, strictly-better adoption, Phase 07 both-gate,
false-green 방지와 ALNS-only rollback point는 신규 구현자가 따라갈 수 있는 수준으로
설명했다.

그러나 실제 구현 지시와 Phase 14B handoff 기준으로 사용하기 전에는 다음 5건을
교정해야 한다.

1. 사용자 지정 canonical 5문서 대신 날짜형 Domain/Architecture를 authority와
   fingerprint source로 고정했다.
2. Phase 14B가 소비할 signed applicability/action-time trust evidence를
   `Phase13ActivatedHandoff` 내부에 넣어 Phase 13 acceptance와 downstream authority를
   다시 섞었다.
3. Current canonical Domain/Architecture의 hybrid step-budget, phase hierarchy와
   `AlnsWarmStart`/`MipWarmStart` 분리 계약이 WP·test·evidence gate에 없다.
4. Gate-open module/POM/test dependency closure와 현재 architecture guard의 전이
   계획이 없어 제시된 future Maven 절차가 clean checkout에서 닫히지 않는다.
5. 핵심 pseudocode가 session을 열지 않고 사용하며, improvement 경로에서는 이전
   ALNS incumbent mutation을 허용하는 assertion을 둔다.

따라서 verdict는 `CHANGES_REQUIRED`다. 이는 Phase 13 구현 실패 판정이 아니라 사람용
가이드의 교정 요구다. Target은 수정하지 않았다. Phase 13 실제 구현은 Target 교정과
무관하게 Phase 14A receipt, `C-17` 및 전문 owner 승인, predecessor acceptance와
implementation/evidence review가 없어 계속 `GATED_NOT_STARTED_NOT_ACCEPTED`다.

## 2. 검토 source, fingerprint와 line

### 2.1 사용자 지정 canonical 5문서

이번 review는 repository 상위 [문서 지도](../../../README.md)의 현재 top-level
계보와 사용자 지시의 “canonical 5문서”를 적용해 non-dated
Master/Domain/Architecture, integrated design, open-question register를 canonical
5문서로 고정했다.

| Source | 직접 대조한 범위 | Git blob | SHA-256 / lines |
|---|---|---|---|
| [Canonical Master](../../../master-design.md) | §1~§4, §10~§17, 특히 `C-17`, §11.7~§11.10, `RM-9A~C` | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` / 1,648 |
| [Canonical Domain](../../../domain-design.md) | §3, §10.6~§10.9, §12.5~§12.8, §15~§18 | `ace117c380466b733994a1fbb2a95d31e41b3959` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` / 1,607 |
| [Canonical Architecture](../../../architecture-design.md) | §4~§7, §11, §14, §17~§20, §22 | `81495ff448d0e618ab3563e8ff80614fb1028acf` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` / 1,469 |
| [Integrated design](../../../architecture-domain-implementation-design.md) | §2~§3, §16~§22, §25~§29 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` / 3,822 |
| [Question register](../../../master-design-open-questions.md) | §1~§4, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` / 87 |

Target이 `Final Domain/Architecture`로 사용한
`docs/2026-07-26-domain-design.md`와
`docs/2026-07-26-architecture-design.md`도 historical semantic cross-check로
읽었다. 각각 Git blob/SHA-256/line은
`0a02ba4c...b1e6` / `1b56cf8b...dbac` / 1,886,
`d51339e2...3d6b` / `1162d7c2...49ed` / 1,019다.

### 2.2 Implementation plan, 원본 Phase/review와 인접 guide

| Source | 직접 대조한 범위 | Git hash-object / SHA-256 / lines |
|---|---|---|
| [Implementation README](../../README.md) | §0~§7, source authority, 14A→13→14B DAG | `8a9cb4a2...b2a1` / `64542381...358` / 240 |
| [Master realization plan](../../master-realization-plan.md) | Phase 06~08/12~14, §8~§15 | `d7f6be4f...6eb` / `940fe8c2...0f5d` / 943 |
| [Execution progress](../../execution-progress-and-results.md) | §2, §5, §8~§10 | live `36afdfde...f1d` / `24d61971...3f1d` / 439 |
| [원본 Phase 13](../../phases/phase-13-optional-hybrid-route-selection.md) | 전체 §1~§14 | `cb3cd961...16bd` / `ca31cfa5...c504` / 1,976 |
| [원본 Phase 13 review](../../reviews/phase-13-review.md) | F-P13-001~008, blocker, ALNS-first addendum | `691620dc...79d` / `13ec5067...ff3` / 505 |
| [Phase 12 사람용 guide](../phases/phase-12-human-implementation-guide.md) | §16.2~§16.3 bounded handoff | `d0557e01...074` / `a0e29edf...6fdf` / 2,784 |
| Target | 전체 | `a9329593...1ca` / `92ed2d46...6f14` / 1,959 |
| [Phase 14 사람용 guide](../phases/phase-14-human-implementation-guide.md) | §2, §6.4~§6.5, §9.7, WP14-3~5, §14 | `78f1bb2e...e6c` / `27f958b9...1791` / 2,095 |

Implementation README는 날짜형 Domain/Architecture를 `Final` source로 나열하지만,
repository 상위 문서 지도는 non-dated Domain/Architecture를 현재 top-level
문서로 둔다. 이번 사용자 지시는 canonical 5문서를 별도로 지정했으므로 이 conflict를
사용자 지시로 해소했다. Target이나 두 README를 reviewer가 수정하지는 않았다.

## 3. HEAD와 live inventory

### 3.1 HEAD baseline 재현

- Branch `codex-implementation`, HEAD
  `7cc890ee1d0805df5ae14b633127fade4f978639`를 재현했다.
- HEAD에는 POM 1개, main Java 6개, test Java 1개가 있다.
- HEAD의 Phase 13 pool/selection/hybrid/OR-Tools adapter와 `E-P13-*`는 0개다.
- Target metadata의 canonical/implementation source Git blob은 HEAD와 일치했다.
- Target은 HEAD에 tracked되지 않은 사람용 guide다.

### 3.2 Review 시점 live inventory

| 항목 | Live 관찰 | Phase 13 의미 |
|---|---|---|
| POM | 13개 | 미커밋 Phase 00 reactor scaffold; Phase 13 acceptance 아님 |
| Main Java | 33개 | stable `package-info.java` 23개 + legacy executable 10개 |
| Test Java | 14개 | architecture/fixture 9개 + legacy characterization 5개 |
| Stable modules | core/solver/verification/application/capabilities/profile-catalog | semantic Phase 13 구현 0 |
| Maven wrapper | `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` | wrapper 존재와 accepted toolchain evidence는 별개 |
| Phase 13 package | `solver.pool`, `solver.selection`, `solver.hybrid`, optional hybrid application 모두 없음 | `GATED_NOT_STARTED` |
| OR-Tools | production dependency/source 0; denylist/self-test 문자열만 존재 | Backend 구현/승인 0 |
| Phase 13 evidence | non-`target` `E-P13-*` 0 | `NOT_PRODUCED` |
| Java/Maven | Corretto 25.0.3 / Maven 3.9.14 | Target의 Java 25/Maven 전제와 일치 |

Target의 one-time live snapshot 뒤 authoritative progress 파일은 Git blob
`3e9dcd1...`에서 review 시점 `36afdfde...`로 다시 바뀌었다. 현재 Phase 00은
`CHANGES_REQUIRED_FIX_01_IN_PROGRESS / NOT_ACCEPTED`이고 Phase 13은 여전히
`C17_GATE_CLOSED / NOT_ACCEPTED`다. Target이 snapshot 시점을 명시했고 Phase 13
의미가 바뀌지 않았으므로 이 hash drift 자체를 finding으로 세지 않았다.

### 3.3 Live Maven closure에서 확인한 사실

- `rpdptw/solver/pom.xml`, `rpdptw/application/pom.xml`,
  `rpdptw/verification/pom.xml`에는 JUnit 또는 test-fixtures test dependency가 없다.
- `rpdptw/hybrid-application/pom.xml`,
  `adapters/route-selection-ortools-cpsat/pom.xml`과 gated integration profile은 없다.
- owner POM을 `-f`로 단독 실행하면 clean local repository에서 sibling reactor
  artifact를 자동 build하지 않는다.
- 현재
  `ProviderAndVendorIsolationArchitectureTest#defaultReactorAdvertisesNoRouteSelectionCapability`
  는 optional adapter directory 자체의 부재를 assert한다. Gate-open WP가 그
  directory를 만들면 현재 test는 default reactor 오염 여부와 무관하게 실패한다.

Maven test는 실행하지 않았다. Gate가 닫혀 있고 Phase 13 source/test/module이 0개인
상태에서 live concurrent Phase 00 scaffold를 build해도 Phase 13 guide의
implementation evidence가 되지 않으며, 이번 작업의 write 범위도 output 하나로
제한돼 있기 때문이다.

## 4. 검토 방법과 severity

1. Target 1,959줄을 line-numbered로 전수 읽었다.
2. Target Git blob, SHA-256, line, HEAD/source fingerprint를 재계산했다.
3. 사용자 지정 canonical 5문서를 우선하고 날짜형 설계는 historical cross-check로
   분리했다.
4. Implementation master plan/README/progress, 원본 Phase 13과 원본 review,
   Phase 12/14 사람용 guide를 양방향 대조했다.
5. `C-17`, Phase 14A receipt, backend/license/native/security/operations/cost,
   applicability signing과 Phase 14B production authority를 서로 다른 gate로 추적했다.
6. Pair/pool/projection/model/warm-start/materialization/adoption/verification lifecycle과
   failure/rollback/retry/reproducibility를 source section에 대조했다.
7. HEAD와 live POM/source/test/wrapper/module/evidence inventory를 분리했다.
8. Future WP의 Java type, Maven dependency, test discovery와 architecture rule 전이를
   정적으로 검토했다.
9. Local link/GFM fragment, heading/fence, trailing whitespace/tab/NUL/conflict
   marker/EOF와 scoped diff를 검사했다.

| Severity | 기준 |
|---|---|
| `CRITICAL` | 현재 문서가 production/authority를 즉시 우회하거나 회복 곤란한 정상 결과 corruption을 허용 |
| `HIGH` | Canonical 의미·authority·lifecycle·필수 gate 또는 실행 closure 누락으로 잘못된 구현/승격을 허용 |
| `MEDIUM` | 국소 API/pseudocode/명령 모순으로 구현자가 임의 보완하거나 재현성이 약화 |
| `LOW` | 의미 영향은 제한적이나 metadata/link/용어 신뢰를 저하 |

## 5. Finding summary

| ID | Severity | Finding | Target 수정 |
|---|---|---|---|
| `HG13-R001` | `HIGH` | Canonical Domain/Architecture 대신 날짜형 설계를 authority로 고정 | `YES` |
| `HG13-R002` | `HIGH` | Phase 14B signed applicability를 Phase 13 activated handoff 내부에 혼합 | `YES` |
| `HG13-R003` | `HIGH` | Hybrid ALNS step budget·phase hierarchy·warm-start 분리 gate 누락 | `YES` |
| `HG13-R004` | `HIGH` | Gate-open Maven/module/test/architecture-rule closure가 실행 불가능 | `YES` |
| `HG13-R005` | `MEDIUM` | Pseudocode의 unopened session과 이전 incumbent mutation 허용 assertion | `YES` |

## 6. Findings

### HG13-R001 — Canonical Domain/Architecture source가 잘못 고정됐다

- **Severity:** `HIGH`
- **Finding:** Target metadata lines 34~36, §3.1~§3.3, 각 WP의 근거와 §16
  traceability는 날짜형
  `docs/2026-07-26-domain-design.md`와
  `docs/2026-07-26-architecture-design.md`를 `Final Domain/Architecture`로
  authority화한다. 사용자 지정 canonical 5문서와 repository 상위 문서 지도는
  non-dated `docs/domain-design.md`, `docs/architecture-design.md`를 현재
  top-level 상세 설계로 둔다. Target은 implementation README와 상위 지도 사이
  conflict를 드러내거나 current semantic diff를 적용하지 않았다.
- **사람에게 미치는 영향:** 신규 구현자는 current Domain §10.9/§12.5~§12.8과
  Architecture §6.7/§11/§14/§17~§19 대신 다른 section/blob을 entry receipt와
  traceability에 봉인한다. Current hybrid phase hierarchy, ALNS step accounting,
  retry/failure matrix, Maven evidence와 application boundary를 빠뜨려도 source를
  따랐다고 오인할 수 있다.
- **Target 위치:** Metadata lines 31~44; §3.1 lines 234~247; §3.2 lines
  253~273; §3.3 lines 275~302; WP 근거; §16 lines 1912~1934.
- **Source section:** 사용자 지정 canonical 5문서;
  `docs/README.md` 문서 지도/계층; Canonical Domain §10.9, §12.5~§12.8,
  §16.7~§18; Canonical Architecture §6~§7, §11, §14, §17~§20, §22.
- **Root cause:** Implementation README와 원본 Phase 13의 날짜형 `Final` source
  index를 상위 repository map 및 이번 사용자 지시보다 우선해 복제했다.
- **Required correction:** Target metadata, source table, 읽기 순서, 모든 source
  link/WP 근거/traceability를 non-dated canonical Domain/Architecture의 current
  blob과 heading으로 교체한다. 날짜형 설계는 historical cross-check로 표시한다.
  단순 hash 교체로 끝내지 말고 current hybrid hierarchy, work/step accounting,
  failure/retry, observability, Maven evidence 차이를 requirement/WP/test/evidence에
  반영한다. 두 README의 source-index conflict는 known documentation blocker로
  명시한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** 다른 human guide가 계속 날짜형 source를 current authority로
  부를 수 있다. Target 교정 뒤에도 scheduler-owned 문서 지도 정렬은 별도 작업이다.

### HG13-R002 — Phase 14B signing/trust authority가 Phase 13 handoff에 역혼합됐다

- **Severity:** `HIGH`
- **Finding:** Target §12.2는
  `Phase13PreReviewEvidenceManifest → review → acceptance receipt → scheduler
  ACCEPTED → Phase13ActivatedHandoff`라는 Phase 13 evidence DAG를 올바르게 둔다.
  그러나 §15.2의 `Phase13ActivatedHandoff` 본문에는
  `signed applicability + action-time verification`을 직접 포함한다. 바로 다음
  §15.3은 signing trust와 current action verification이 Phase 14B 소유라고
  선언한다. Canonical two-branch contract는
  `Phase13ApplicabilityReceipt.Activated(handoff, signedEnvelope,
  actionTimeVerification)`처럼 accepted Phase 13 handoff와 Phase 14B downstream
  consumption evidence를 분리한다.
- **사람에게 미치는 영향:** 구현자는 Phase 14B `G14-SIGNING-TRUST`가 열리기 전에는
  Phase 13 acceptance/handoff를 만들 수 없다고 해석해 `14B → 13` reverse gate를
  만들거나, 반대로 Phase 13 owner가 signature/trust/action-time `PASS`를 자체
  생산해 Phase 14B authority를 우회할 수 있다. 특히 user가 요구한 Phase 14A/13/14B
  권한 분리가 무너진다.
- **Target 위치:** §6.4 line 704; WP13-8 lines 1442~1471; §12.2 lines
  1647~1670; §15.2 lines 1855~1879; §15.3 lines 1881~1895.
- **Source section:** 원본 Phase 13 §6.5와 §14.3~§14.4;
  원본 Phase 13 review F-P13-008/§8;
  원본 Phase 14 §3.1, §5.3, §9.7;
  Phase 14 사람용 guide §9.7와 WP14-4;
  Master realization plan Phase 14A/14B gate.
- **Root cause:** `Phase13ActivatedHandoff`와 scheduler/Phase 14 control-plane의
  `Phase13ApplicabilityReceipt.Activated` wrapper를 평문 handoff 목록에서 하나로
  합쳤다.
- **Required correction:** `Phase13ActivatedHandoff`에는 exact Phase 13 accepted
  evidence, approved hybrid scope/backend/config, conditional Phase 12 evidence와
  ALNS-only rollback point만 둔다. Signed envelope와 current action-time
  verification은 scheduler/Phase 14B-owned
  `Phase13ApplicabilityReceipt.Activated` wrapper의 sibling field로 분리한다.
  `G14-SIGNING-TRUST`가 Phase 13 entry/acceptance/evidence prerequisite가 아니며,
  Phase 14B가 action마다 current policy로 검증한 뒤에만 소비한다는 dependency
  negative test를 WP13-8/§12/§15/traceability에 정렬한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Signature algorithm/trust roots/revocation/time/freshness policy와
  actual signed receipt는 여전히 `OPEN/GATED_NOT_APPROVED`/`NOT_PRODUCED`다. Target
  교정은 그 authority를 승인하지 않는다.

### HG13-R003 — Hybrid execution의 ALNS step budget과 warm-start lifecycle이 닫히지 않았다

- **Severity:** `HIGH`
- **Finding:** Target은 `MipWarmStart`, route-selection budget과
  `RouteSelectionRunId`를 설명하지만 `AlnsWarmStart`를 별도 type/lifecycle로
  정의하지 않는다. 더 중요하게 current canonical contract의
  `ALNS step < ALNS segment < HybridPhase < WorkerRun < ExecutionRound`,
  `Σ completed ALNS segment steps = phase2MaxSteps`, route selection의
  work/time/admission budget은 ALNS completed-step budget과 별도라는 invariant가
  §4.5, state/pseudocode, WP13-6, test/evidence/DoD에 없다.
- **사람에게 미치는 영향:** 구현자가 CP-SAT work/time을 ALNS step으로 차감하거나,
  여러 inner segment가 `phase2MaxSteps`를 초과/미달해도 worker를 정상 완료로
  표시할 수 있다. `MipWarmStart`를 다음 ALNS segment state로 재사용하거나
  non-adopted selection이 RNG/adaptive continuation을 바꾸면 termination,
  retry identity, benchmark 비교와 reproducibility evidence가 모두 false-green이
  된다.
- **Target 위치:** §4.2 `MipWarmStart` line 388; §4.4 lines 437~486;
  §4.5 lines 490~514; §9.4 lines 956~1016; §9.6~§9.7 lines 1052~1147;
  WP13-6 lines 1364~1395; test table/exit checklist/traceability.
- **Source section:** Canonical Domain §10.6, §12.6~§12.8, §15.3, §16.6~§16.7;
  Canonical Architecture §11.2 lines 897~932, §14 lines 1012~1045,
  §17.1~§17.2; Canonical Master §11.10; Phase 06 handoff contract.
- **Root cause:** 원본 Phase 13의 pool/selector 중심 설명을 압축하면서 outer worker
  step/accounting 계약을 인접 Phase 소유로만 보고 human guide의 integration
  invariant에서 누락했다.
- **Required correction:** 용어집/identity/state에 `AlnsWarmStart`와
  `MipWarmStart`를 별도 추가하고 상호 대입을 금지한다. `HybridPhaseRecord`에
  ALNS requested/completed segment-step 합, selector work/time, admission wait,
  fallback/adoption, next `AlnsWarmStart` fingerprint를 분리한다. Pseudocode와
  WP13-6은 exact segment budgets의 합이 declared `phase2MaxSteps`와 일치하고
  selector work가 그 합에 들어가지 않음을 assert해야 한다. Failure/equal/worse에서는
  next ALNS warm start와 RNG/adaptive policy가 이전 incumbent와 동일하고,
  strictly-better fresh candidate만 새 `AlnsWarmStart`가 되는 unit/property/E2E와
  evidence count를 추가한다. 숫자는 계속 explicit approved config이며 hidden
  official default를 만들지 않는다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** `Q-BENCH-02` 공식 step/worker/round 숫자는 여전히
  `OPEN — EXPERIMENT_REQUIRED`다. 구조적 accounting invariant만 지금 고정할 수 있다.

### HG13-R004 — Future Maven/module/test closure와 architecture guard 전이가 실행 불가능하다

- **Severity:** `HIGH`
- **Finding:** Target은 solver/application/hybrid/adapter tests와 owner-POM 명령을
  제시하지만 change tree/WP가 이를 compile/discover할 POM 변경을 소유하지 않는다.
  Live solver/application/verification POM에는 JUnit/test-fixtures dependency가 없고
  hybrid application/adapter POM 및 gated profile도 없다. `-f` owner POM 명령은
  clean local repository에서 sibling reactor dependency를 자동 build하지 않는다.
  또한 current architecture test는
  `adapters/route-selection-ortools-cpsat` directory 자체가 없어야 한다고 assert한다.
  WP13-4가 승인 뒤 directory를 만들고 WP13-8이 root `clean verify`를 실행하면
  default reactor가 backend를 광고하지 않아도 이 기존 test가 실패한다.
- **사람에게 미치는 영향:** 구현자는 test source를 만들고도 JUnit/test-fixture를
  compile하지 못하거나 ambient local Maven repository 때문에 사람마다 다른 결과를
  얻는다. Root verify를 green으로 만들기 위해 architecture guard를 삭제하거나,
  반대로 optional adapter를 default reactor에 넣어 build만 통과시키는 잘못된
  shortcut을 택할 수 있다. 현재 WP 순서로는 DoD와 default-build invariant를 동시에
  만족할 수 없다.
- **Target 위치:** §5.2 lines 568~594; §8.1~§8.2 lines 800~860;
  WP13-0/1/2/3/4/5/7/8의 future command; §11.3 test list;
  §11.5~§11.7 lines 1572~1628.
- **Source section:** Canonical Architecture §5, §7.3, §18, §19;
  원본 Phase 13 §5.2, §9.8, WP-13.4/13.8;
  원본 review F-P13-006;
  live root/rpdptw/build POM과
  `ProviderAndVendorIsolationArchitectureTest`.
- **Root cause:** Target이 semantic package와 test 이름은 구체화했지만, concurrent
  Phase 00 scaffold의 실제 POM/test topology를 gate-open migration input으로
  변환하지 않았다.
- **Required correction:** 각 WP의 expected change에 owner `src/test`, JUnit,
  test-fixtures test-jar dependency, Surefire report gate, optional hybrid/adapter
  POM, aggregator/profile registration과 OR-Tools isolation rule을 명시한다.
  Clean exact-source prerequisite build/install 또는 approved root
  `-pl/-am` sequence와 owner selected-test sequence를 분리해 ambient local
  repository 의존을 없앤다. Gate-open 시 existing directory-absence assertion은
  “default reactor/profile/dependency/service advertisement/class-load/native-load가
  0”을 검사하도록 versioned transition하고, optional approved profile만 adapter를
  포함하는 positive/negative architecture tests를 둔다. 기존 guard를 그냥 삭제하면
  안 된다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Module/profile 이름과 exact OR-Tools dependency는 C-17 및
  Phase 00/Architecture approval 전까지 proposed다. Correction은 이름을 미리
  승인하는 것이 아니라 필요한 Maven ownership과 검증 순서를 닫아야 한다.

### HG13-R005 — Skeletal pseudocode가 session lifecycle과 rollback 불변조건을 위반한다

- **Severity:** `MEDIUM`
- **Finding:** Target §9.7 lines 1127~1134는 `session`을 선언하거나
  `factory.openSession(...)`으로 열지 않은 채 `session.solve(...)`를 호출하고
  `closeJavaSession()`을 실행한다. 원본 Phase 13 pseudocode에는 nullable session
  초기화와 factory open이 있다. 같은 snippet의 line 1146은
  `fingerprint(alnsIncumbent) == pre unless selector was strictly better`라고 해
  improvement 경로에서는 이전 immutable ALNS incumbent가 변해도 assertion이
  통과하게 한다. Target의 no-alias/immutable rollback 설명과 충돌한다.
- **사람에게 미치는 영향:** 그대로 구현하면 compile/null failure 또는
  open/close/cancellation cleanup 누락이 난다. 더 위험하게는 strictly-better
  candidate를 in-place로 incumbent에 덮어써 accepted ALNS-only rollback point,
  pre-fingerprint와 warm-start lineage를 잃을 수 있다.
- **Target 위치:** §9.7 lines 1127~1147; WP13-4 lines 1303~1329;
  WP13-5/6 rollback lines 1355~1393; §14.4 lines 1800~1809.
- **Source section:** Canonical Domain §12.7~§12.8;
  Canonical Architecture §6.7, §17.2;
  원본 Phase 13 §8.3 lines 1156~1185와 §8.4;
  Master `P-18`.
- **Root cause:** Source pseudocode를 줄이면서 session-open 단계가 빠졌고, source에
  남아 있던 conditional incumbent assertion을 immutable input/새 champion의
  구분 없이 복제했다.
- **Required correction:** `session = null`을 두고 admission/native readiness 뒤
  `factory.openSession(approvedConfig)`을 정확히 한 번 호출하며, `finally`에서
  non-null session의 cancellation/reference cleanup과 Java close를 보장한다.
  이전 `alnsIncumbent` fingerprint는 adoption 여부와 무관하게 항상 `pre`와 같아야
  한다. Strict improvement에서는 별도 fresh committed champion과 next
  `AlnsWarmStart` fingerprint를 assert한다. Better path까지 포함한
  open/solve/close exactly-once, open failure, solve failure, cleanup failure,
  old-incumbent byte identity test를 추가한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Exact session API/cleanup mechanism은 승인된 OR-Tools
  version/platform 뒤 확정되지만 ownership과 immutable rollback 의미는 지금
  고정할 수 있다.

## 7. Target 수정과 구분한 실제 구현 blocker

아래는 Target 교정으로 해소되지 않으며 이번 finding count에도 넣지 않은 실제 구현
blocker다.

| Implementation blocker | Owner/authority | 현재 안전 판정 | Target 수정 |
|---|---|---|---|
| Phase 00 reactor remediation 미승인 | Phase 00 owner + independent reviewer + scheduler | concurrent scaffold만 보존, accepted build로 사용 금지 | `NO` |
| Phase 03~07 full-solution evaluator/comparator gap | Core/Profile/Algorithm/Verification owners | route kernel/immutable routes-bank까지만 안전 | `NO` |
| Phase 06/07/08 accepted evidence 부재 | Algorithm/Verification/Application + reviewers | Phase 13 source/POM/test 생성 금지 | `NO` |
| Phase 14A corpus/protocol/criteria와 receipt 부재 | Benchmark/Quality + independent acceptance owner | `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT=NOT_PRODUCED` | `NO` |
| `C-17` scope/hybrid meaning/route-selection authority 미승인 | Product + Algorithm + Architecture | canonical ALNS-only path | `NO` |
| OR-Tools version/config/native/license/SBOM/security/ops/cost/capacity/fallback/rollback 미승인 | Backend/Architecture/Legal/Supply-chain/Security/Ops/FinOps | OR-Tools-free, backend load 0 | `NO` |
| Phase 13 scheduler task와 exact accepted scope 부재 | Total scheduler | `GATED_NOT_STARTED_NOT_ACCEPTED` | `NO` |
| Signed applicability/trust policy와 actual Skip/Activated receipt 부재 | Scheduler + Security/Release + Phase 14B | Phase 14B action 0; Phase 14A에는 영향 없음 | `NO` |
| Phase 14B official values/provider/production authority 부재 | Phase 14B owners | traffic/pointer/default activation 0 | `NO` |

특히 Phase 14A receipt는 Phase 13의 필요조건일 뿐 `C-17`, backend, license/native,
security, operations, cost 또는 production authority를 부여하지 않는다. Phase 13이
나중에 implementation acceptance를 받아도 Phase 14B의 official manifest, provider,
signing trust, canary/activation action authority는 별도다.

## 8. Finding이 없었던 축과 근거

| Review axis | 판정 | 근거 |
|---|---|---|
| `C-17` implementation gate | `PASS` | §3.4 AND gate와 모든 WP `WAITING`; 승인 전 Java/POM/test/backend 생성 금지 |
| Phase 14A 선행 | `PASS` | receipt를 Phase 13 필요조건으로 두고 충분조건/production authority로 과장하지 않음 |
| Phase 12 ownership | `PASS` | selected substituted runtime에만 bounded accepted evidence 요구; baseline universal predecessor 아님 |
| Gate-closed absence | `PASS` | no request는 scheduler-owned Skip, explicit request는 pre-allocation reject, Phase 13 load/native 0 |
| Pair/route-pool invariant | `PASS` | same-vehicle/pair-complete, completed/full-evaluated, same authority, no-alias, stable merge, pin과 conservative dominance |
| Projection/model | `PASS` | explicit unassigned와 concrete vehicle rows, checked int64, typed non-projectable skip, no hidden Big-M |
| Backend policy | `PASS` | direct Java CP-SAT, `MPSolver` 금지, version/workers/seed/time/gap/platform은 open/gated |
| Status/incumbent boundary | `PASS` | `OPTIMAL`/`FEASIBLE`에서만 selected value, no-incumbent status getter 금지 |
| Materialization/adoption | `PASS EXCEPT R005` | selected ID validation, fresh route/bank, full evaluation, strict improvement와 Phase 07 both-gate가 명시됨 |
| Security/license/ops/cost | `PASS DOCUMENT GATE` | 전문 owner 승인과 evidence 없이는 adapter/traffic/승격 불가; actual approval은 없음 |
| Failure/rollback | `PASS EXCEPT R003/R005` | optional clean fallback/required incomplete, immutable ALNS-only last-safe point, artifact 삭제/overwrite 금지 |
| Evidence DAG | `PASS EXCEPT R002` | pre-review manifest → independent review → post-review receipt, backfill/reverse edge 금지 |
| Hidden defaults | `PASS` | pool cap/budget/cadence/traffic/Q-BENCH-02/Q-VAR-01을 open/deferred로 보존 |
| Test false-green awareness | `PASS EXCEPT R004` | zero selected test, skipped required test, fake/backend/materialization evidence 혼합을 금지 |
| Historical 자료 | `PASS` | superseded Master/legacy/GCP scaffold를 authority 또는 evidence로 승격하지 않음 |

## 9. 정적 검사와 범위 검증

### 9.1 Target 문서 정적 검사

| 검사 | 결과 |
|---|---|
| Target non-empty/hash/line | `PASS`; Git blob `a9329593...1ca`, SHA-256 `92ed2d46...6f14`, 1,959 lines |
| Local Markdown links | 29개 검사, missing file 0 |
| GFM fragments | 12개 검사, missing fragment 0 |
| Headings | 90개, duplicate exact heading 0 |
| Fences | 92개로 even |
| Trailing whitespace | 0 |
| Tab/NUL/conflict marker | 각 0 |
| EOF newline | `PASS` |
| Target source blob metadata | HEAD source 12개 모두 metadata 값과 일치 |
| Target 변경 | 시작/종료 hash와 line 동일; reviewer 수정 0 |

### 9.2 Scope와 git 검사

- Review output
  `docs/implementation/human-guides/reviews/phase-13-review.md`만 생성했다.
- Target, human-guide README/progress, implementation README/progress/master plan,
  인접 guide/review, canonical/historical 문서, Java/POM/test/deployment와
  `target/`을 수정하지 않았다.
- Stage/commit/push/worktree 작업을 하지 않았다.
- Scoped `git diff --check`는 diagnostic 0으로 종료했고, Target/output의
  untracked-aware trailing whitespace/tab/NUL/conflict-marker 검사도 모두 0이었다.
- Maven/build/test는 실행하지 않았으며 existing `target/` 또는 generated report를
  evidence로 읽지 않았다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=4 MEDIUM=1 LOW=0
REQUIRED_CORRECTION_FINDINGS: HG13-R001,HG13-R002,HG13-R003,HG13-R004,HG13-R005

## Correction 01 읽기 전용 재검증

### 재검증 범위, 시각과 고정 artifact

- **재검증 시각:** `2026-07-29T02:43:15+0900` (`Asia/Seoul`, KST)
- **작업 성격:** 새 broad review가 아니라 이 원 review의 correction round 01
  read-only closure 확인이다.
- **HEAD/branch:** `7cc890ee1d0805df5ae14b633127fade4f978639` /
  `codex-implementation`
- **검증 finding:** `HG13-R001`, `HG13-R002`, `HG13-R003`, `HG13-R004`,
  `HG13-R005`
- **쓰기 범위:** 이 review 파일 끝의 본 절만 append했다. Target, correction report,
  canonical/original/adjacent 문서, README/progress, Java/POM/test/deployment를
  수정하지 않았다.
- **실행 범위:** Java/Maven/test/native/deployment는 실행하지 않았다. 현재 Phase 13
  source/module/profile/evidence가 0이고 entry gate가 닫혀 있으므로 기존 build 성공이나
  zero-test 결과는 이 correction의 closure evidence가 아니다.

| Artifact | Recheck SHA-256 | Git hash-object | Lines | 판정 |
|---|---|---|---:|---|
| Append 전 원 review | `be6e880ad7f82fdf7d766183cf25127e0be98037e3c1b59f137dd944f4335278` | `37ab000def272bd6073be6aab543fa2d4edab0c3` | 468 | 기존 finding/본문 불변 확인 |
| Corrected target | `68c35d77638d440513ec55bd025599bb6d34a7e7c429dacdfe362ceb3d5feba1` | `ef7224e3e7654745927f47deab894075924b799b` | 2,390 | 이번 recheck의 고정 target |
| Correction report | `ad8b67844a26e5246b3e3c4f2ac7008cd351f6dfa54b2cd4f052c0864d19ebc0` | `65b03569676ad6635ca42de392f52479bfe1911c` | 417 | 자기주장은 참고만 하고 target을 직접 검증 |

### 읽은 source와 recheck hash

Hash는 recheck 시점의 file bytes에 대한 SHA-256이다. Correction report의 source
요약을 acceptance로 사용하지 않고 아래 current bytes와 target anchor를 직접
대조했다.

| Source | Recheck SHA-256 | Lines | 역할 |
|---|---|---:|---|
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | 1,648 | `C-17`, pool/selection, step budget, rollback |
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | 1,607 | Current pool, 두 warm start, materialization/adoption |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | 1,469 | Current Maven/module/session/step/failure 경계 |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | 3,822 | Canonical 15 Phase와 인접 ownership |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 87 | `Q-BENCH-02`, `Q-VAR-01` 상태 |
| `docs/README.md` | `5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569` | 67 | Current top-level 문서 지도 |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | 240 | ALNS-first Phase DAG와 source-index drift |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | 943 | Phase 13 entry/exit/evidence와 14A/14B |
| `docs/implementation/execution-progress-and-results.md` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | 470 | Current scheduler/live gate |
| `docs/implementation/phases/phase-12-provider-substitution.md` | `5f243b2900afe31801ab1c47b9c2467a6344c42b7b27f49ee5c5b51d1c21de1a` | 1,984 | Conditional substituted-runtime handoff |
| `docs/implementation/phases/phase-13-optional-hybrid-route-selection.md` | `ca31cfa532d179a6e278e2a3124eb5579b8775c39ae3cd13c0ca17ae2b4ac504` | 1,976 | 원본 Phase 13 계약 |
| `docs/implementation/reviews/phase-13-review.md` | `13ec506780f0fa3effb777462ad3e8e6041760538e3eb0ba66400f99d4076ff3` | 505 | 원본 F-P13-001~008 |
| `docs/implementation/phases/phase-14-official-calibration-cutover.md` | `c8f3b4fd2e48d35547d7e2fe31169a5e0a882730859ac97d9c5f60a165802eae` | 2,192 | 14A/14B original authority |
| `docs/implementation/human-guides/phases/phase-12-human-implementation-guide.md` | `4e675d25ab0dbdfaf93a7174ea6d67adaa26ab72bc84d0115e23258adb243ee1` | 3,251 | Current bounded Phase 12 producer |
| `docs/implementation/human-guides/phases/phase-14-human-implementation-guide.md` | `4f4f452e03917c129e9e24121ed5544bbe966c8baefe478cd3a28d72af0e429b` | 2,602 | Current Phase 14B consumer wrapper |
| `docs/implementation/human-guides/README.md` | `ad64de533a4e45984ae30be12c57d969a36efd4dc8453e949ec4efc992f6b18a` | 92 | Known 날짜형 source-index drift |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | 1,886 | Historical cross-check only |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | 1,019 | Historical cross-check only |

### Live inventory와 authority 시점 재확인

- Recheck 시점 live count는 POM 13, main Java 33, test Java 17,
  executable `*Test`/`*IT` 15다. Correction snapshot의 test Java 16/executable 14 뒤
  concurrent Phase 00 test 1개가 더 생겼지만 Phase 13 pool/selection/hybrid/adapter/
  `E-P13-*`는 계속 0이고 production OR-Tools source/dependency도 0이다.
- Root POM SHA-256은
  `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`이며
  Surefire `failIfNoTests=false`, Failsafe execution 없음이 유지된다.
  Solver/application/verification POM SHA-256은 각각
  `ac67af2ac73ab6a5d9f9418f4a5b54ed538c775fbad75a627f348885e3b0ad2e`,
  `5beaef9115c3975b6cc13705bcc88361a4cb1698313a76de4d553f51ceb638c3`,
  `1a066e9bfa77f87ff5986e05e9847d5b5b1f8b30d2188de55e4913f6571e4ce7`이고
  JUnit/test-fixtures consumer closure가 아직 없다.
- Current guard
  `ProviderAndVendorIsolationArchitectureTest.java` SHA-256은
  `97e71eb4b60e77cb54440f9f8cd1da3e7dd8509bb9dd054e3f90bfb65a394b25`이며
  line 26에서 optional adapter directory 부재를 계속 assert한다.
- Current progress는 correction snapshot 뒤 drift했지만 Phase 13은
  `C17_GATE_CLOSED / NOT_ACCEPTED`, implementation/evidence는
  `NOT_STARTED / NOT_PRODUCED`, Phase 14A receipt는 `NOT_PRODUCED`, Phase 14B
  production authority는 `NOT_GRANTED`다. 이 live drift는 target의 명시적 one-time
  inventory snapshot을 accepted baseline으로 바꾸지 않으며 finding closure도
  뒤집지 않는다.

### Finding별 closure 판정

#### HG13-R001 — RESOLVED

- **확인한 target anchor:** Metadata lines 34~49; §3.1 lines 239~269; source table과
  읽기 순서 lines 271~321; current lifecycle/invariant lines 397~575; WP 근거와
  §16 lines 2332~2354.
- **Source evidence:** Current non-dated Domain SHA-256 `3add42ca...ff73`의
  §10.9/§12.5~12.8과 Current non-dated Architecture SHA-256
  `fe918a26...2201`의 §5~7/§11.2/§14/§17~19를 직접 대조했다. Target fingerprint의
  Git blob `ace117c...b3959`와 `81495ff...2acf`도 current files와 일치한다.
- **Closure 근거:** Non-dated Domain/Architecture가 현재 authority와 reading order로
  복원됐고 날짜형 두 문서는 `historical only`로 분리됐다. Current hierarchy,
  step/accounting, 두 warm start, failure/retry, Maven/test 의미가 lifecycle,
  WP/test/evidence/traceability까지 연결됐다. Implementation README와 human-guide
  README의 source-index conflict도 lines 264~269에 known documentation blocker로
  명시됐다.
- **남은 risk:** 두 README의 날짜형 source index는 별도 owner correction 전까지
  남는다. 이는 target correction 누락이 아니라 범위 밖 문서 지도 drift이며 current
  guide는 이를 authority로 사용하지 않는다.
- **판정:** `RESOLVED`

#### HG13-R002 — RESOLVED

- **확인한 target anchor:** §2.5 lines 224~235; §6.4의 producer/consumer approval
  boundary; WP13-0 lines 1395~1440; WP13-8 lines 1704~1749; dependency negative
  tests lines 1849~1850; evidence DAG lines 1986~2016; §15.1~15.3 lines
  2229~2311; traceability lines 2352~2353.
- **Source evidence:** 원본 Phase 13 §6.5의 `Activated(handoff, signedEnvelope,
  actionTimeVerification)` wrapper 의미와 current Phase 14 human guide §9.7
  lines 1539~1582의 producer/consumer 시점 분리를 대조했다.
- **Closure 근거:** `Phase13ActivatedHandoff` lines 2255~2270에는 Phase 13 accepted
  scope/evidence/config/rollback만 있고 signed envelope/action-time field가 없다.
  Lines 2281~2289는 이를 Phase 14B-owned
  `Phase13ApplicabilityReceipt.Activated` wrapper의 sibling input으로 둔다.
  `G14-SIGNING-TRUST`는 Phase 13 entry/implementation/evidence/acceptance의
  predecessor가 아니며 Phase 13 owner도 그 `PASS`를 생산하지 않는다는 negative
  dependency가 WP/test/checklist/broken-contract 증상에 일치한다.
- **남은 risk:** Signature algorithm, trust roots, revocation/freshness/time policy와
  actual signed wrapper는 Phase 14B `OPEN/GATED_NOT_APPROVED`/`NOT_PRODUCED`다.
  Correction acceptance는 이 downstream authority를 승인하지 않는다.
- **판정:** `RESOLVED`

#### HG13-R003 — RESOLVED

- **확인한 target anchor:** 용어/identity/lifecycle/invariant lines 397~575;
  backend contract lines 1149~1154; state/pseudocode lines 1250~1375; WP13-5/6
  lines 1588~1664; negative fixtures lines 1772~1778; tests lines 1834~1843;
  false-green/test applicability lines 1874~1912; manifest lines 2024~2033;
  DoD lines 2185~2193; traceability lines 2345~2346.
- **Source evidence:** Current Domain §12.6~12.8의 별도
  `AlnsWarmStart`/`MipWarmStart`, strict adoption/commit과 Current Architecture
  §11.2 lines 897~932 및 §14 lines 1012~1045의
  `Σ completed ALNS segment steps = phase2MaxSteps`/separate selector budget을
  직접 대조했다.
- **Closure 근거:** 두 warm start의 type/identity/lifecycle과 상호 대입 금지가
  고정됐다. `ALNS step < ALNS segment < HybridPhase < WorkerRun <
  ExecutionRound`, normal completed-step exact sum, selector admission/work/time
  별도 accounting이 pseudocode와 WP에 있으며 under/over, selector 혼입, MIP→ALNS
  혼동, RNG/adaptive drift, old-state alias test와 `E-P13-HYBRID` manifest/DoD에
  연결됐다. Failure/equal/worse는 incumbent와 next ALNS warm-start bytes를
  보존하고 strict-better만 fresh champion에서 새 warm start를 만든다.
- **남은 risk:** `phase2MaxSteps`, segment cadence/수, selector budget,
  reproducibility class와 continuation policy의 실제 값은 계속
  `OPEN/GATED`/`OPEN — EXPERIMENT_REQUIRED`다. 구조적 invariant만 닫혔고 구현이나
  official value는 승인되지 않았다.
- **판정:** `RESOLVED`

#### HG13-R004 — RESOLVED

- **확인한 target anchor:** Live inventory §5.2; change tree §8.1 lines 887~933;
  Maven/test/guard §8.4 lines 977~1019; WP 공통 closure lines 1387~1393;
  WP13-3/4/6/8; exact test/false-green §11.3~11.5; Maven sequence lines
  1914~1967; checklist lines 2200~2204; traceability line 2343.
- **Source evidence:** Current Architecture §5의 optional adapter/module topology,
  line 357의 Failsafe ownership, §18~19 build/evidence boundary와 live root/owner/
  test-fixtures/architecture POM 및 directory-absence guard를 직접 대조했다.
- **Closure 근거:** Test source와 같은 patch의 owner JUnit/test-fixtures/Surefire
  closure, adapter Failsafe `integration-test`/`verify`, selected-test
  `failIfNoSpecifiedTests`, fresh XML class/method count, failures/errors/skipped,
  cold isolated repository의 root `-pl/-am` prerequisite install → owner selected
  test → approved-profile `*IT` → default-negative root verify 순서가 명시됐다.
  Existing guard도 삭제가 아니라 default effective reactor/profile/dependency/
  service/class/native 0과 approved-profile exact-one positive/negative test로
  versioned transition하도록 닫혔다.
- **남은 risk:** Live POM/profile/Failsafe/optional adapter와 guard transition은
  아직 구현되지 않았다. 이는 `C-17`/Phase 14A/owner approval 전 실제 구현 blocker다.
  Gate-open 뒤 target skeleton의 placeholder module/profile/test 이름을 approval
  receipt의 exact 값으로 치환하고 cold build로 실행해야 하며, 이번 document recheck는
  Maven 성공을 주장하지 않는다.
- **판정:** `RESOLVED`

#### HG13-R005 — RESOLVED

- **확인한 target anchor:** Fixed invariant lines 570~575; pseudocode lines
  1261~1383; WP13-3/5/6; lifecycle fixtures lines 1775~1778; exact tests lines
  1824~1829와 1840~1842; anti-pattern lines 2122~2126; DoD lines 2175~2176와
  2191~2194.
- **Source evidence:** Current Domain §12.7~12.8의 fresh materialization,
  old-incumbent preservation과 atomic hybrid commit, Current Architecture §6.7/§17의
  backend session/failure lifecycle, 원본 Phase 13 §8.3의 nullable session/open
  sequence를 대조했다.
- **Closure 근거:** `session = null` 뒤 admission/native readiness, factory open
  1회, solve 1회, non-null session cleanup/close 각각 1회 시도가 순서대로 있다.
  Open/solve/cleanup/close failure는 typed no-mutation path이고 cleanup/close
  contamination은 commit 0이다. Strict-better도 old incumbent bytes를 항상 보존하며
  fresh champion/new `AlnsWarmStart`만 준비한 뒤 single atomic commit한다.
  Normal/open/solve/cleanup/close fault와 better-path old-alias test가 별도로 있다.
- **남은 risk:** Exact Java exception taxonomy, callback cleanup API, atomic persistence와
  concurrency contract는 approved backend/application 설계 전 `PROPOSED/OPEN`이다.
  그러나 원 finding의 unopened session과 old-state mutation 허용은 닫혔다.
- **판정:** `RESOLVED`

### Correction regression과 OPEN finding

Correction이 원 finding 범위에 새 authority, lifecycle, Maven/manifest,
security/rollback 또는 Phase 14B ownership regression을 만들었다는 증거는 찾지
못했다. `NEW` finding은 없다. 따라서 OPEN finding의 별도 root cause/required
correction은 `NONE`이다.

### 정적 link/GFM/fence/whitespace/manifest 재검증

| 검사 | Recheck 결과 |
|---|---|
| Local Markdown link | Target/correction/원 review 3개에서 74개 검사, missing file 0 |
| GFM fragment | 19개 검사, missing fragment 0 |
| Heading | Target 95, correction 20, append 후 review 35; exact duplicate 0 |
| Fence | Target 98, correction 8, append 후 review 2; 모두 even |
| Whitespace/encoding | 세 파일 trailing whitespace/tab/NUL/conflict marker 0, LF EOF |
| Scoped `git diff --check` | 세 허용 경로의 tracked scoped check exit 0; untracked review를 `/dev/null`과 비교한 `--no-index --check` whitespace diagnostic 0 (`exit 1`은 내용 차이 존재 의미) |
| Target source fingerprint | Current canonical 5 Git blob이 target metadata/table과 일치 |
| Unit/contract selected manifest | `surefire.failIfNoSpecifiedTests=true`, expected class/method non-zero와 fresh XML count 대조 요구 |
| Adapter integration manifest | Failsafe `verify`, `failsafe.failIfNoSpecifiedTests=true`, exact `-Dit.test`, fresh Failsafe XML 대조 요구 |
| Stale/zero-test 방지 | Warm local repo, existing `target/`, legacy/root success, console-only, zero-test와 stale report를 evidence로 금지 |
| Evidence manifest | Step/segment/phase/worker/round, selector counters, 두 warm start, session counts, atomic commit, exact command/environment/test counts를 요구 |
| Maven/test 실행 | `NOT_RUN`; gate closed와 Phase 13 source/profile/evidence 0 때문에 문서 정적 closure만 판정 |

### Recheck verdict와 실제 구현 blocker 구분

`ACCEPTED`는 correction 01이 원 human-guide finding 5건의 required correction을
현재 target에서 닫았다는 뜻이다. Phase 13 구현/evidence/acceptance 또는 production
승격은 아니다. Phase 06/07/08 acceptance, Phase 14A
`ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, `C-17`, backend/version/native/license/SBOM/
security/operations/cost/capacity/fallback/rollback 승인, actual Java/POM/test/profile/
adapter/evidence와 independent implementation review는 여전히 없다. Phase 14B
official values, signing/action-time policy, traffic/pointer와 production authority도
별도다. 현재 안전 지점은 계속 accepted ALNS-only build/manifest와 Phase 07
both-gate path이며 Phase 13/native activation은 0이다.

RECHECK_ROUND: 01
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: HG13-R001,HG13-R002,HG13-R003,HG13-R004,HG13-R005
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: 68c35d77638d440513ec55bd025599bb6d34a7e7c429dacdfe362ceb3d5feba1
CORRECTION_REPORT_HASH_RECHECKED: ad8b67844a26e5246b3e3c4f2ac7008cd351f6dfa54b2cd4f052c0864d19ebc0

## Correction 02 읽기 전용 재검증

### 재검증 범위와 고정 artifact

- **재검증 시각:** `2026-07-29T03:23:59+0900` (`Asia/Seoul`, KST)
- **Finding:** Scheduler final global link finding `HG13-SFV-001` (`LOW`)
- **범위:** 두 corrected GFM fragment의 실제 heading resolution, target guide와
  correction 02 report 전체의 상대 링크/GFM fragment, 두 fragment 외 semantic
  diff 부재, Phase 13 optional/`C-17`/Phase 14A gate 보존만 확인했다. Correction
  01 finding을 broad하게 다시 열지 않았다.
- **쓰기 범위:** 기존 review의 앞 700줄은 변경하지 않고 본 절만 끝에 append했다.
  Target guide, correction report, 두 link source와 다른 파일은 read-only로 유지했다.
- **HEAD/branch:** `7cc890ee1d0805df5ae14b633127fade4f978639` /
  `codex-implementation`

| Artifact | Recheck SHA-256 | Git hash-object | Lines |
|---|---|---|---:|
| Append 전 기존 review | `75dc1dbfa9ffa5d1eb49d200566a3bf8ced05b7f2faf93834a3a2bfcfa24ed17` | `ee785dc1d7a2293761ee8e79b13419a2da4f4039` | 700 |
| Corrected target | `f5a755808104da13cd51f34235f37aafa8348c4ca094ea3061593203af88db07` | `4f9a80fe33b71e2e7e8598fefaa65f39bef75dda` | 2,390 |
| Correction 02 report | `c8eb383c709d6559d900b551781595e621e61c004db4d02995a8368fa046553e` | `ae851d74761a309912fc86f5cfe45c6372e07450` | 112 |
| Integrated design link source | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` | 3,822 |
| Master realization plan link source | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | `d7f6be4fff0089204fbdb52f731b2348407f36eb` | 943 |

### HG13-SFV-001 — RESOLVED

- **Root cause 재확인:** Source heading의 em dash `—`는 slug에서 제거되지만 그
  양쪽 공백은 각각 `-`로 치환된다. 기존 fragment는 이 두 공백을 하나의 hyphen으로
  축약해 실제 GFM heading slug와 달랐다.
- **Target anchor 1:** Target line 310의
  `../../../architecture-domain-implementation-design.md#17-phase-13--optional-route-pool과-route-selection`
- **Source evidence 1:** Integrated design line 2984의
  `## 17. Phase 13 — Optional route pool과 route selection`을 GFM 규칙으로
  정규화한 slug는 정확히
  `17-phase-13--optional-route-pool과-route-selection`이다. Corrected fragment는
  1개이고 old single-hyphen fragment는 0개다.
- **Target anchor 2:** Target line 312의
  `../../master-realization-plan.md#phase-13--optional-hybrid`
- **Source evidence 2:** Master realization plan line 565의
  `### Phase 13 — Optional hybrid`를 GFM 규칙으로 정규화한 slug는 정확히
  `phase-13--optional-hybrid`다. Corrected fragment는 1개이고 old
  single-hyphen fragment는 0개다.
- **전체 link evidence:** Target guide와 correction 02 report에서 local relative
  link 45개, GFM fragment 16개를 실제 destination file과 generated heading slug에
  대조했다. Missing file 0, missing fragment 0이다. Correction report 자체의 두
  source-heading link와 Correction 01 residual anchor도 resolve된다.
- **변경 범위 evidence:** Current target에서 corrected 두 fragment를 각각
  Correction 01의 old single-hyphen 값으로만 역치환한 stream의 SHA-256은
  `68c35d77638d440513ec55bd025599bb6d34a7e7c429dacdfe362ceb3d5feba1`이다.
  이는 round 01 recheck에서 고정한 target hash와 byte-for-byte 일치한다. 따라서
  round 01 target에서 round 02 target으로의 변경은 두 URL fragment의
  single-hyphen → double-hyphen 치환 두 곳뿐이고 link label, 읽기 순서와 본문
  의미 변경은 없다.
- **Gate 보존:** Target의 Phase 13 상태는 계속 `GATED_NOT_STARTED`이고
  `C-17`은 optional gated target이다. Phase 06/07/08 acceptance와 Phase 14A
  `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`, 별도 backend/license/security/operations/
  cost 승인이 없으면 구현을 시작하지 않는 AND gate가 유지된다. Master realization
  plan lines 565~579의 optional Phase 13 entry/exit/handoff 의미와도 일치한다.
  Link correction은 implementation/evidence/acceptance, Phase 14B authority 또는
  production activation을 만들지 않았다.
- **Residual risk:** 두 source heading이 향후 바뀌거나 GFM slug 검사기가 em dash
  제거 뒤 인접 공백을 임의 collapse하면 link가 다시 깨질 수 있다. Global link
  검사기는 각 공백을 개별 hyphen으로 처리하고 source heading 변경 시 fragment를
  재검증해야 한다. 현재 target correction은 더 필요하지 않다.
- **Status:** `RESOLVED`

### 정적 검사와 scope

| 검사 | 결과 |
|---|---|
| Exact corrected fragments | 각 1개, old fragment 각 0개 |
| Target/report whole relative links | Local 45, fragment 16, missing file/fragment 0 |
| Heading/fence | Target heading 95/fence 98, correction heading 7/fence 2; duplicate heading 0, fence even |
| Whitespace/encoding | Target/report trailing whitespace, tab, CRLF, NUL, conflict marker 0; LF EOF |
| Semantic scope | 두 fragment 역치환 hash가 round 01 target SHA-256과 exact 일치 |
| Implementation/gate status | 의미 변경 0; `C17_GATE_CLOSED`, Phase 14A receipt `NOT_PRODUCED` 유지 |
| Java/Maven/test | `NOT_RUN`; link-only read-only recheck이며 implementation evidence로 해석하지 않음 |
| Append 후 scope/whitespace | 기존 review 앞 700줄 SHA-256 불변, 세 허용 경로 tracked scoped `git diff --check` exit 0, untracked review `--no-index --check` whitespace diagnostic 0 |

### Verdict

`HG13-SFV-001`의 두 broken GFM fragment는 실제 권위 source heading에 resolve되도록
교정됐고 target/report 전체 link 검사도 통과했다. 두 fragment 외 target 의미
변경은 없으며 기존 Correction 01 closure와 Phase 13/14 authority gate는 보존됐다.
추가 target correction은 필요하지 않다.

RESOLVED_FINDINGS: HG13-SFV-001
RECHECK_ROUND: 02
RECHECK_VERDICT: ACCEPTED
RECHECK_OPEN_FINDINGS: NONE
