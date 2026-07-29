# Phase 06 사람용 구현 가이드 독립 리뷰

```yaml
document_status: COMPLETE
review_type: INDEPENDENT_HUMAN_IMPLEMENTATION_GUIDE_REVIEW
phase: "06"
review_date: 2026-07-29
timezone: Asia/Seoul
inventory_observed_at: 2026-07-29T01:40:21+09:00
reviewer_role: author와 분리된 independent Phase 06 human-guide reviewer
target: docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md
target_tracked_at_head: false
target_git_hash_object: 5fc02ade5df03dd6e697a2a9feb82c1c09f46b43
target_sha256: fdb8e41a134ac4ec582a72e2eb60342bcaa93e07cbeef2451803c9d2f8c46c4f
target_lines: 1979
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
verdict: CHANGES_REQUIRED
target_changes_required: true
finding_counts:
  critical: 0
  high: 4
  medium: 2
  low: 0
required_correction_findings:
  - HG06-R001
  - HG06-R002
  - HG06-R003
  - HG06-R004
  - HG06-R005
  - HG06-R006
```

## 1. 결론

Target은 Phase 06의 핵심인 request-pair 단위 destroy/repair, changed-route
first-write COW, independent bank, hard/stage guard 선행, completed-step,
namespaced RNG, exact termination, independent replay와 Phase 07 verifier 분리를
상당히 충실하게 설명한다. 신규 CVRPTW 구현자를 위한 primer, 사람 checkpoint,
last-safe-point, evidence DAG와 Phase 13/14 gate도 유용하다.

그러나 사람이 이 가이드를 실제 구현·판정 기준으로 사용할 수 있으려면 다음 여섯
건을 교정해야 한다.

1. Canonical Master와 `docs/README.md`가 지목한 Domain/Architecture와 Target이
   “Final”로 고정한 날짜 문서가 서로 다르며, Target은 이 source-authority 충돌을
   드러내지 않는다.
2. Canonical Phase 06의 exact test method 89개 중 23개가 Target의 required
   method 후보 표에서 빠져 false-green이 가능하다.
3. 현재 solver POM과 parent build에는 JUnit test dependency와 Failsafe 실행이
   닫혀 있지 않고, `-pl rpdptw/solver` selected command는 clean repository에서
   upstream artifact를 얻는 절차가 없다.
4. `AlnsStepExecutor`는 state publisher를 갖지 않는 return-value API인데
   pseudocode는 atomic publication을 수행한다고 쓰며, `accepted`도 정의하지
   않은 채 사용한다. 가장 중요한 interruption linearization을 구현 가능한
   contract로 만들지 못한다.
5. HEAD snapshot과 계속 바뀌는 live working tree를 분리하려는 의도는 좋지만,
   현재 wrapper/evidence/progress와 inventory command가 이미 stale하거나
   multi-module tree를 누락한다.
6. Phase 06→07 handoff는 solver-independent versioned schema를 요구하면서 그
   schema의 module owner와 생성/mapping 경계를 정하지 않아 dependency rule과
   exit integration을 동시에 만족할 수 없다.

Target 수정이 필요한 finding이 있으므로 verdict는 `CHANGES_REQUIRED`다. 이는
Phase 06 구현 실패 판정이 아니다. 실제 Phase 06은 여전히 predecessor
acceptance와 cross-Phase contract가 없어 시작 전 `BLOCKED`다.

## 2. 검토 source, fingerprint와 line inventory

Target이 선언한 HEAD Git blob 값은 모두 재현되었다. 다만 source 선택 자체에
`HG06-R001`의 authority 충돌이 있고, progress와 POM은 HEAD 이후 live working
tree에서 바뀌었으므로 별도 identity로 기록했다.

| Source | 직접 대조한 범위 | Fingerprint / lines |
|---|---|---|
| `docs/master-design.md` | §1~§4, §10~§17 | HEAD blob `b507a5e7ba0b7e76475bc2d755493e814f4d053a`; SHA-256 `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd`; 1,648줄 |
| `docs/domain-design.md` | §1~§3, §7~§11, §16~§18 | HEAD blob `ace117c380466b733994a1fbb2a95d31e41b3959`; SHA-256 `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73`; 1,607줄 |
| `docs/architecture-design.md` | §1~§3, §5~§6 | HEAD blob `81495ff448d0e618ab3563e8ff80614fb1028acf`; SHA-256 `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201`; 1,469줄 |
| `docs/architecture-domain-implementation-design.md` | §1~§3, §9~§10, §19~§25 | HEAD blob `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0`; SHA-256 `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571`; 3,822줄 |
| `docs/master-design-open-questions.md` | 전체와 exact `Q-*` rows | HEAD blob `3fff4c583a54f02dea667e78c8e5187d65ec0e18`; SHA-256 `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b`; 87줄 |
| `docs/implementation/master-realization-plan.md` | Phase 05~07, §8~§15 | HEAD blob `d7f6be4fff0089204fbdb52f731b2348407f36eb`; SHA-256 `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d`; 943줄 |
| `docs/implementation/README.md` | §0~§7 | HEAD blob `8a9cb4a29685a2540bd605c3ac63bb459052b2a1`; SHA-256 `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358`; 240줄 |
| `docs/implementation/execution-progress-and-results.md` | §1~§10 | HEAD blob `250aa90ae568a6b32ec905fa5ee456d430ff72cf`; live Git object `a6933848a0ac004d56bd4d23fb31d4084531784a`; live SHA-256 `27240b1a623d5becbdeaf415c2f483f53db39cbd65344487475423ec0f70bf45`; 418줄 |
| Canonical Phase 06 | 전체 §1~§17 | HEAD blob `984b6978981fffa662bcf4cf5b4f8a14c2287c09`; SHA-256 `aec051d91795011c8a589926e14058f8f4943f2cdda873b97e2e4bf613ab6d08`; 1,671줄 |
| Canonical Phase 06 review | 전체 §1~§8 | HEAD blob `c68ed93aec2dee9f376096c70e9a8c0b1fa66480`; SHA-256 `a33e998bfc9d28215cc2087d289fdb6980298b60d51793a0e817b77febbaf4ab`; 244줄 |
| 인접 human Phase 05 | source, ownership, handoff, test/evidence | SHA-256 `be3487a7bffd2b4d8fa884f7e1a0b5e0e63f6534751f4c465331f5463cd0df4d`; 2,057줄 |
| Target human Phase 06 | 전체 | Git object `5fc02ade5df03dd6e697a2a9feb82c1c09f46b43`; SHA-256 `fdb8e41a134ac4ec582a72e2eb60342bcaa93e07cbeef2451803c9d2f8c46c4f`; 1,979줄 |
| 인접 human Phase 07 | source, candidate projection, Phase 06 handoff | SHA-256 `43b996382bd2b54e7fbb7b7ab218d20c50d76dc8fe535ec859937e7138a3d05d`; 1,878줄 |
| Live root `pom.xml` | parent/plugin/dependency policy | Git object `1dc675ba17b7f2202f34a22131f152cc2868b075`; SHA-256 `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`; 237줄 |
| Live `rpdptw/solver/pom.xml` | solver compile/test dependencies | Git object `facf6c32ea130d7506a3bf4567ecede27de194e7`; SHA-256 `ac67af2ac73ab6a5d9f9418f4a5b54ed538c775fbad75a627f348885e3b0ad2e`; 21줄 |

Target이 인용한 날짜 문서의 HEAD blob
`0a02ba4c77a402455e3d80b76969dca28831b1e6`와
`d51339e251dee1e032e711144dc63d6d07d7323b`도 정확히 재현했다. Hash가
틀린 것이 아니라 어느 문서가 canonical인지 충돌하는 것이 finding이다.

`docs/2026-07-26-master-design.md`와 연구·세션 문서는 역사 cross-check로만
취급했다. 실제 의미 판정은 사용자 지정 canonical 5문서와 그 내부 authority를
우선했다.

## 3. HEAD와 live inventory

### 3.1 HEAD baseline

HEAD `7cc890ee1d0805df5ae14b633127fade4f978639`에는 다음이 추적돼 있다.

- 단일 root `pom.xml`
- root `src/main/java` 6개와 `src/test/java` 1개
- Maven wrapper와 target multi-module reactor 없음
- Phase 06 production type/test/evidence 없음

이는 Target의 고정 Git baseline hash와 일치한다.

### 3.2 Live working tree

검토 시각의 미커밋 working tree에는 HEAD와 별도로 다음이 관찰됐다.

- `./mvnw`, `.mvn/`, root parent/aggregator와 총 13개 `pom.xml`
- `rpdptw/{core,solver,verification,application,capabilities,profile-catalog}`
- `build/{test-fixtures,architecture-rules}`와 `legacy/gcp-placeholder`
- target namespace Java 23개가 있으나 모두 현재 Phase 00
  `package-info.java` skeleton
- Phase 00 architecture/test-fixture test source 9개
- Phase 06 production/test Java는 `package-info.java`를 제외하면 0개
- Live progress의 Phase 00은
  `IMPLEMENTED_PENDING_INDEPENDENT_REVIEW`, evidence bundle 제출,
  independent implementation review `IN_PROGRESS`, acceptance receipt
  `NOT_PRODUCED`

따라서 live reactor/evidence 제출은 Phase 00 acceptance가 아니고 Phase 06
entry도 열지 않는다. 반대로 Target의 “wrapper 없음”, “Phase 00 evidence 없음”도
더 이상 live 사실이 아니다.

## 4. 검토 방법과 severity

1. Target 1,979줄을 줄 번호와 함께 전수 읽었다.
2. Canonical 5문서, implementation plan/README/live progress, 원본 Phase 06과
   원본 review를 대조했다.
3. Human Phase 05/07에서 producer/consumer artifact, module owner, lifecycle과
   handoff shape를 교차 검사했다.
4. HEAD Git blob과 live filesystem/POM/progress를 분리했다.
5. Canonical Phase 06 §11.1의 exact method 열과 Target §11.2의 method 열을
   기계적으로 비교했다.
6. Proposed Java interface와 pseudocode의 compile/ownership/publication
   closure를 Java 25/Maven 관점에서 검사했다.
7. Local Markdown file/fragment, heading/fence, whitespace와 declared fingerprint를
   정적으로 검사했다.
8. Maven test는 실행하지 않았다. Phase 06 구현이 없고 다른 작업의 미커밋
   Phase 00 tree에 `target/` 산출물을 만들 이유가 없기 때문이다.

| Severity | 기준 |
|---|---|
| `CRITICAL` | 권위·상태·보안·publication을 즉시 잘못 확정해 회복 곤란한 결과를 허용 |
| `HIGH` | 핵심 source, state transaction, build/test gate 또는 acceptance를 잘못 이끎 |
| `MEDIUM` | inventory/API/handoff가 불완전해 사람이 임의 결정을 해야 하거나 재현성을 약화 |
| `LOW` | 의미를 직접 바꾸지는 않지만 링크·용어·metadata 신뢰를 약화 |

## 5. Finding summary

| ID | Severity | 요약 | Target 수정 |
|---|---|---|---|
| `HG06-R001` | `HIGH` | Canonical Domain/Architecture path가 Master/docs 지도와 Target 사이에서 충돌 | `YES` |
| `HG06-R002` | `HIGH` | Canonical exact method 89개 중 23개 누락 | `YES` |
| `HG06-R003` | `HIGH` | Solver test dependency/Failsafe/upstream build가 닫히지 않아 future command가 실행 불가 | `YES` |
| `HG06-R004` | `HIGH` | Atomic publication을 수행할 owner/API가 없고 pseudocode의 `accepted`가 미정의 | `YES` |
| `HG06-R005` | `MEDIUM` | Live inventory와 progress가 stale하고 inventory 명령이 multi-module tree를 누락 | `YES` |
| `HG06-R006` | `MEDIUM` | Phase 06→07 solver-independent handoff schema owner/mapping이 미결정 | `YES` |

## 6. Findings

### HG06-R001 — Canonical Domain/Architecture source가 서로 다른 경로로 고정돼 있다

- **Severity:** `HIGH`
- **Finding:** Target metadata lines 24~25, 읽기 순서 lines 202~208,
  fingerprint 표 lines 245~246과 traceability 표는
  `docs/2026-07-26-domain-design.md`와
  `docs/2026-07-26-architecture-design.md`를 “Final” source로 고정한다. 그러나
  Canonical Master §1.4는 `domain-design.md`와 `architecture-design.md`를 자신의
  상세 문서로 직접 지목하고, `docs/README.md` lines 7~10, 18~24, 38~40도 같은
  두 파일을 현재 최상위 entry와 hierarchy로 선언한다. 날짜 문서 자신은 반대로
  plain 문서를 supersede한다고 쓰므로 실제 source set에 해결되지 않은 양방향
  authority drift가 있다.
- **사람에게 미치는 영향:** 구현자는 어느 module/package, state artifact,
  runtime identity와 test/evidence 배치를 따라야 하는지 두 문서군 사이에서
  임의 선택하게 된다. Target의 source freeze와 fingerprint가 green이어도 잘못된
  source set을 정확히 fingerprint한 false assurance가 된다.
- **Target 위치:** metadata lines 22~34; [§3.1](../phases/phase-06-human-implementation-guide.md#31-충돌-해소-순서);
  [§3.2](../phases/phase-06-human-implementation-guide.md#32-처음-구현하는-사람의-정확한-읽기-순서);
  [§3.3](../phases/phase-06-human-implementation-guide.md#33-검증-가능한-source-fingerprint);
  §16 traceability의 Domain/Architecture rows.
- **Source section:** Canonical Master §1.4 lines 118~131;
  `docs/README.md` lines 3~12, 18~45; 날짜 Domain metadata lines 1~15;
  날짜 Architecture metadata lines 1~15.
- **Root cause:** Implementation 문서와 human-guide README가 날짜 문서를
  “Final”로 부르는 기존 표현을, 더 높은 Master와 현재 docs 지도에 대한
  semantic-diff/approval record 없이 복제했다.
- **Required correction:** Target 단독으로 한쪽을 조용히 선택하지 않는다.
  먼저 `SOURCE_AUTHORITY_CONFLICT — BLOCKED`를 WP-06.0 entry gate에 추가하고,
  Master/Domain/Architecture owner가 canonical path와 supersedes 관계를 승인하게
  한다. 현재 Master/docs 지도를 따르기로 승인되면 metadata, 읽기 순서,
  fingerprint와 traceability를 plain Domain/Architecture blob으로 일괄 교체한다.
  날짜 문서를 유지하기로 승인되면 Master/docs 지도와 같은 변경 단위의 authority
  record를 요구한다. 두 source set의 Phase 06 semantic/package/test diff도
  evidence에 포함한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Target만 고치고 implementation README, human-guide README,
  원본 Phase/review의 “Final” link를 남기면 다음 Phase가 다시 다른 source set을
  소비한다. 이 finding의 closure는 cross-document owner approval가 필요하다.

### HG06-R002 — Canonical exact test matrix 23개가 Target 판정 기준에서 빠졌다

- **Severity:** `HIGH`
- **Finding:** Canonical Phase 06 §11.1에는 exact test method 89개가 있지만
  Target §11.2에는 66개만 있다. Target §11.5는 자신의 “required method”를 fresh
  report와 대조하라고 하므로 아래 23개 누락을 스스로 검출하지 못한다.

  ```text
  cancelDiscardLeavesParentUnchanged()
  interruptedStepAdvancesNoState()
  hardInfeasibleCandidateNeverReachesAcceptance()
  worstRemovalOnlyProposesAndNeverMutates()
  legacyReplayPresetMatchesVersionedDifferentialTrace()
  noFeasibleInsertionPreservesHardFeasibleSnapshot()
  doesNotReimplementPropagationOrComparator()
  sameSnapshotOutcomeAndConfigProduceSameNextFingerprint()
  updatePeriodUsesCompletedStepsOnly()
  saUsesAcceptancePurposeStreamOnly()
  temperatureAdvancesOnceAfterCompletedRejectedStep()
  sameStateCandidateAndDrawProduceSameDecision()
  unusedAcceptanceDrawDoesNotShiftNextStepOperators()
  detectsSeedDerivationVersionMismatch()
  doesNotTreatClockUnavailableAsFailedCandidate()
  cancelDuringEveryStepBoundaryDiscardsDraft()
  platformTimeoutIsNotAlgorithmCompletion()
  malformedProposalFailsClosedWithoutInternalRetryLoop()
  unorderedSourceCollectionsDoNotChangeReplay()
  detectsChangedOperatorVersionOrTiePolicy()
  detectsSameCandidateDifferentTraceDigest()
  solverModuleHasNoTenantAuthorizationOrProviderCredentialLogic()
  searchDoesNotImplementPropagationTravelOrFinalization()
  ```

- **사람에게 미치는 영향:** Cancel/platform timeout, hard-infeasible acceptance
  leakage, no-feasible repair, adaptive/temperature boundary, seed/schema drift,
  unordered collection nondeterminism, trace/operator corruption와 semantic owner
  duplication이 있어도 Target 표 기준 evidence는 green일 수 있다. 특히
  Target §11.4가 fault/corruption/reproducibility/security/architecture를 모두
  필수라고 쓰는 것과 실제 required inventory가 모순된다.
- **Target 위치:** [§11.2](../phases/phase-06-human-implementation-guide.md#112-핵심-test-class와-exact-method-후보)
  lines 1522~1594; [§11.3](../phases/phase-06-human-implementation-guide.md#113-red--green-순서);
  [§11.5](../phases/phase-06-human-implementation-guide.md#115-false-green-방지)
  lines 1634~1657; exit checklist lines 1788~1822.
- **Source section:** Canonical Phase 06 §11.1 lines 1194~1288; §11.2~§11.3;
  original review F-P06-002, F-P06-007~011.
- **Root cause:** “핵심 후보”로 교육용 표를 줄이면서 canonical acceptance
  inventory와 Target의 fail-closed required-method manifest를 분리하지 않았다.
- **Required correction:** 23개 method의 fixture/oracle/pass criterion을 Target
  표와 해당 WP에 복원하거나, canonical matrix를 그대로 normative manifest로
  참조하고 Target 표는 단지 학습 subset이라고 명시한다. 후자를 택하면 Phase exit
  때 canonical method inventory와 실제 report의 차집합이 0인지 검사하는
  versioned manifest/digest 절차가 필요하다. 각 omitted corruption/fault test에는
  seeded faulty double가 실제 red가 되는 sensitivity evidence도 남긴다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Method 이름만 추가하고 expected를 production output으로
  생성하거나 oracle가 production helper를 import하면 89개여도 false-green이다.

### HG06-R003 — Maven/POM과 selected verification command가 clean 환경에서 닫히지 않았다

- **Severity:** `HIGH`
- **Finding:** Target tree lines 684~727은 solver `pom.xml`을 포함하지만 어느 WP도
  test dependency, test-fixture artifact 또는 Failsafe configuration을 추가·확인하지
  않는다. Live `rpdptw/solver/pom.xml` lines 14~20은 compile `rpdptw-core`
  dependency 하나뿐이며 JUnit test dependency가 없다. Root POM은 Surefire만
  선언하고 `failIfNoTests=false`를 사용하며 Maven Failsafe plugin 선언이 없다.
  따라서 Target lines 1411~1413의 `-Dit.test=AlnsWorkerRunIT ... clean verify`는
  현재 `*IT`를 실행할 lifecycle이 없다. 또한 lines 1269~1273 등의
  `-pl rpdptw/solver` command는 clean isolated local repository에서 `rpdptw-core`
  artifact를 reactor로 함께 만들지 않으며, Target은 `-am`을 금지하면서도
  upstream bootstrap을 제공하지 않는다.
- **사람에게 미치는 영향:** Test source가 JUnit import에서 compile되지 않거나,
  IT가 아예 실행되지 않거나, 개발자 local Maven repository에 우연히 설치된
  upstream artifact에 따라 pass/fail이 달라진다. `clean verify` exit 0과 실제
  Phase 06 test 실행을 혼동할 수 있다.
- **Target 위치:** proposed tree lines 684~727; WP future commands lines
  1269~1273, 1304~1308, 1337~1341, 1371~1375, 1406~1414, 1444~1448,
  1480~1492; [§11.5](../phases/phase-06-human-implementation-guide.md#115-false-green-방지)
  lines 1634~1657.
- **Source section:** Canonical Architecture §5.1~§5.2는 `*IT`를 Failsafe로
  분리하고 parent가 Surefire/Failsafe를 중앙화하도록 요구한다; Canonical Phase 06
  §13.1; original review F-P06-002; live root/solver POM inventory.
- **Root cause:** Test 부재 false-green을 막으려고 selected module과
  `failIfNoSpecifiedTests=true`만 강조하면서 Maven reactor dependency resolution,
  JUnit classpath와 Failsafe lifecycle을 별도 실행 계약으로 닫지 않았다.
- **Required correction:** WP-06.0에 accepted effective-POM gate를 추가한다.
  최소한 solver의 JUnit test dependency, 필요한 approved test-fixture test-jar,
  pinned Failsafe plugin과 `*IT` include, Surefire/Failsafe
  `failIfNoSpecifiedTests` 동작을 effective POM으로 검증한다. Clean isolated Maven
  repository에서 upstream을 재현 가능하게 build한 뒤 target report를 fail-closed
  검증하는 command를 제시한다. `-am`을 사용할 경우 upstream nonmatching test만
  허용하고 solver의 exact class/method XML 부재는 별도 scanner가 반드시 실패하게
  하거나, 명시적 dependency bootstrap과 target-only test를 두 단계로 분리한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Phase 00 review가 Failsafe/JUnit을 나중에 보완할 수 있지만,
  Phase 06 guide가 accepted effective POM identity를 pin하지 않으면 local
  installation/stale report 의존이 재발한다.

### HG06-R004 — Single publication linearization이 proposed Java API로 구현되지 않는다

- **Severity:** `HIGH`
- **Finding:** Target lines 803~840의 `AlnsStepExecutor.execute(before, ...)`는
  immutable `AlnsStepResult`를 반환할 뿐 state holder, expected version,
  publisher 또는 CAS port가 없다. 그런데 state machine lines 1048~1055와
  pseudocode lines 1141~1143은 executor가 `nextState`를 “atomically publish”한
  뒤 반환한다고 하고, lines 1155~1156은 probe와 publication 사이를
  linearization boundary로 규정한다. 어떤 Java operation이 유일한 publication인지
  정의되지 않았다. 같은 pseudocode는 `decision`만 정의한 뒤 lines 1122에서
  존재하지 않는 `accepted` 변수를 사용한다. File tree에는 `AlnsEngine`이 있지만
  run signature/state owner도 제시되지 않는다.
- **사람에게 미치는 영향:** 구현자는 pure function의 method return을 publication으로
  볼지, executor 내부 `AtomicReference`, engine-owned state 또는 외부 store write를
  추가할지 임의로 결정한다. 잘못된 선택은 candidate/progress/learning의 부분
  commit, 중복 publish, publication 직후 cancel의 step 유실 또는 hidden mutable
  state를 만든다. Undefined `accepted`는 guard-failed reject와 accepted
  non-improving의 current update를 다르게 구현하게 할 수 있다.
- **Target 위치:** [§8.3](../phases/phase-06-human-implementation-guide.md#83-proposed-state-types)
  lines 803~844; [§9.1](../phases/phase-06-human-implementation-guide.md#91-completed-step-state-machine);
  [§9.2](../phases/phase-06-human-implementation-guide.md#92-alns-step-pseudocode)
  lines 1079~1156; WP-06.3 lines 1317~1349.
- **Source section:** Canonical Master §11.4와 §13.1;
  Canonical Phase 06 §7.2, §8.1~§8.2; original review F-P06-006,
  F-P06-010, F-P06-011.
- **Root cause:** 원본 review의 “aggregate를 한 번 publish” 교정을
  state-machine 문장으로 옮겼지만, publication owner와 Java memory/state-store
  boundary를 skeletal contract에 연결하지 않았다.
- **Required correction:** Cross-Phase API review에서 둘 중 하나를 명시적으로
  선택한다.

  1. `AlnsStepExecutor`는 완전한 pure transition이며 publication은
     `AlnsEngine`의 단일 owner loop가 `Completed.after`를 현재 state slot에
     한 번 교체하는 operation이다.
  2. Executor가 publication을 소유한다면 expected-state identity/version과
     one-shot publisher/CAS result, duplicate/conflict/failure outcome을 signature에
     포함한다.

  어느 경우든 final probe, publication operation, return/next-boundary signal의
  happens-before/linearization 의미와 fault matrix를 명시하고
  `accepted`를 `decision.isAccepted()` 같은 defined relation으로 바꾼다.
  `AlnsEngine.run`/run outcome/termination의 최소 compile-closed signature와
  publication count oracle도 추가한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** In-memory atomic reference가 맞더라도 향후 Phase 08/10
  durable commit과 같은 publication으로 합치면 ownership이 다시 흐려진다.
  Solver-local state publication과 application/storage commit을 용어와 type으로
  분리해야 한다.

### HG06-R005 — Snapshot/live inventory와 재현 명령이 현재 checkout을 충분히 설명하지 못한다

- **Severity:** `MEDIUM`
- **Finding:** Target은 commit snapshot과 final working tree를 나누려 하지만
  lines 54~56은 “현재 checkout에 `rpdptw-solver` module이 없다”고 하고,
  lines 439와 451~454는 wrapper와 Phase 00 evidence가 없다고 쓴다. 현재 live
  tree에는 solver module과 executable wrapper가 있고, scheduler progress는
  Phase 00 evidence bundle 제출과 independent review 진행을 기록한다. 또한
  lines 420의 `find src/main/java src/test/java`는 root legacy 경로만 찾고
  `rpdptw/**`, `build/**`, `legacy/**`의 multi-module Java를 전부 누락한다.
  Target이 구현자에게 확인하라고 한 progress 범위도 §8까지여서 live execution
  registry §10.1을 직접 지목하지 않는다. Lines 457~464의 “현재 inventory
  fingerprint”는 실제로 HEAD legacy file SHA-256이다.
- **사람에게 미치는 영향:** 구현자는 live reactor가 없다고 오판하거나, 반대로
  scaffold/evidence 제출을 accepted Phase 00으로 오인할 수 있다. Multi-module
  source/POM을 보지 못해 POM closure와 Phase 06 source 0건을 정확히 판정하지
  못한다.
- **Target 위치:** opening note lines 54~58; [§5.1](../phases/phase-06-human-implementation-guide.md#51-재현-가능한-read-only-inventory-명령);
  [§5.2](../phases/phase-06-human-implementation-guide.md#52-존재placeholder부재-구분);
  §5.3 lines 466~486; reading order line 224.
- **Source section:** Live progress metadata lines 11~18와 §10.1 lines
  372~418; live root/child POM and Java inventory; Master Realization Plan의
  status/evidence 분리 규칙.
- **Root cause:** 작성 중 관찰한 여러 live 순간을 “final/current”로 봉인했고,
  단일-module 시절 inventory command를 reactor 전환 뒤 갱신하지 않았다.
- **Required correction:** §5를 `HEAD snapshot`, `observed live drift at
  <timestamp/status digest>`, `implementation-entry re-snapshot procedure`로
  분리한다. HEAD SHA 표는 “HEAD legacy fingerprint”로 이름을 바꾼다.
  `rg --files` 또는 multi-root `find`로 모든 POM, wrapper, `rpdptw/build/legacy`
  main/test Java와 Phase 06 non-`package-info` count를 검사한다. Live progress
  §10.1의 evidence/review/receipt를 읽되 receipt가 없으므로 `BLOCKED`를 유지한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Shared working tree는 correction 중에도 변할 수 있다.
  문서의 live 표를 영구 사실로 보지 말고 implementation entry에서 timestamp,
  HEAD, status/POM/progress identity를 다시 봉인해야 한다.

### HG06-R006 — Phase 06→07 handoff schema의 module owner와 mapping 경계가 없다

- **Severity:** `MEDIUM`
- **Finding:** Target lines 667~675와 763~766은 Phase 07이 import할 수 있는
  solver-independent `CommittedCandidate` projection을 요구하고 solver의
  verification dependency를 금지한다. 그러나 proposed tree에는 core-owned
  candidate projection, versioned artifact schema, mapper 또는 compatibility
  contract가 없고, WP-06.7이 막연히 “handoff projection”을 만든다고만 한다.
  Human Phase 07은 `CandidateSnapshot`을 verification-side proposed request로
  제시한다. Phase 06 solver가 verification type을 생성하면 dependency rule을
  깨고, Phase 07이 solver `CommittedCandidate`를 받으면 verifier independence를
  깨며, Phase 08 mapper로 미루면 Phase 06 exit handoff의 owner가 빈다.
- **사람에게 미치는 영향:** 구현자는 같은 route/bank/authority data를 서로 다른
  type/encoding으로 중복 정의하거나 solver internal serialization을 노출할 수
  있다. Content digest, unknown field/version, canonical order와 exceptional
  termination mapping이 양쪽에서 달라져 Phase 07이 accepted Phase 06 artifact를
  읽지 못할 수 있다.
- **Target 위치:** producer/consumer lines 160~166;
  [§7.4](../phases/phase-06-human-implementation-guide.md#74-4단계--통합과-handoff)
  lines 665~676; [§8.2](../phases/phase-06-human-implementation-guide.md#82-dependency-direction)
  lines 733~766; WP-06.7 lines 1458~1500; §15.2 lines 1877~1905.
- **Source section:** Canonical Architecture의 solver→core와
  verification→core/no-solver DAG; Canonical Phase 06 §7.1/§16.2;
  original review F-P06-005; Canonical/Human Phase 07 candidate input와
  no-solver dependency sections.
- **Root cause:** “Public/wire schema 최종 승인은 non-scope”라는 올바른 제한과
  Phase 간에 당장 필요한 internal immutable handoff contract의 owner 결정을
  함께 미뤘다.
- **Required correction:** WP-06.0에 `CROSS-PHASE HANDOFF SCHEMA GATE`를
  추가한다. Architecture + Phase 06/07 owner가 다음 중 한 경계를 승인해야 한다.

  - core-owned immutable candidate vocabulary/projection을 solver와 verifier가
    각각 생성/소비하거나,
  - Java module dependency와 무관한 versioned immutable artifact schema를
    Phase 06이 생성하고 Phase 07이 독립 decode한다.

  Owner, package/artifact path, canonical order/encoding version, content digest,
  unknown/missing field rejection, declared-claim 비권위성, termination/last-safe
  mapping과 no-verifier-field test를 exact하게 연결한다. Phase 06/07 compatibility
  test는 양쪽 internal class를 공유하지 않는 독립 fixture/decoder로 검증한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Schema를 core에 둔다고 solver cache/trace/verification status까지
  core로 끌어올리면 stable semantic module이 search/publication 세부에 오염된다.
  최소 immutable candidate vocabulary만 공유해야 한다.

## 7. 축별 판정과 no-finding 근거

| 검토 축 | 판정 | 근거 / residual |
|---|---|---|
| 원문 불변조건과 Phase 경계 | `PASS` | Pair atomicity, route-bank XOR, no-alias COW, hard/stage guard, completed-step, no verifier pull-forward를 보존 |
| 신규 독자의 배경·이유 이해 | `PASS` | CVRPTW 대비 RPDPTW, delivery-only, current/best, COW lifecycle와 학습 순서가 구체적 |
| WP 실행·판정성 | `FAIL` | 각 WP shape는 좋지만 `HG06-R003`, `HG06-R004`로 실제 command/state boundary가 닫히지 않음 |
| Java/Maven/proposed API 실제성 | `FAIL` | `HG06-R003`, `HG06-R004`, `HG06-R006` |
| 과도한 완성 코드/추상성 | `PASS WITH FINDING` | Copy-paste 완성 코드를 주지 않는 점은 적절하나 필요한 publication/handoff owner까지 생략 |
| Entry/exit/evidence/rollback/failure | `PASS` | AND gate, last safe point, rollback, exact exceptional status와 immutable evidence DAG가 명확 |
| Security/observability | `PASS` | Raw external ID/PII/secret 배제, semantic trace와 observation metadata 분리 |
| Reproducibility | `PASS WITH FINDING` | Strong envelope, purpose RNG, replay/corruption 원칙은 적절하나 test 23개 누락 |
| Hidden default | `PASS` | `Q-BENCH-02`, legacy/test/experiment/official 값을 구분하고 omitted config를 bind error로 둠 |
| Phase 13/14 gate | `PASS` | 14A receipt+별도 `C-17` 승인 전 Phase 13 금지, 14A/14B와 production authority 분리 |
| 인접 ownership/lifecycle/state | `PASS WITH FINDING` | Phase 05 seed/COW와 Phase 07 verifier 분리는 맞지만 handoff schema owner는 `HG06-R006` |
| 링크/GFM/fingerprint/traceability | `PASS WITH FINDING` | Local Markdown link 45개와 fragment 29개는 유효, declared HEAD blob mismatch 0; source path authority는 `HG06-R001` |
| 사람 승인과 안전 지점 | `PASS` | CP-0~7, owner/config/oracle/evidence approval와 rollback point가 있음 |
| 문서/실제 구현 혼동 | `PASS WITH FINDING` | `NOT_STARTED/NOT_ACCEPTED/NOT_PRODUCED`를 반복하지만 live Phase 00 inventory는 `HG06-R005` |
| Fixture/builder/oracle/red→green | `PASS WITH FINDING` | Full-copy/seed/canonicalizer 독립 원칙은 좋으나 canonical exact test 23개와 Maven lifecycle이 빠짐 |
| Test category/pass/false-green | `FAIL` | Category 표는 포괄적이지만 required manifest와 build lifecycle이 `HG06-R002`, `HG06-R003`으로 축약/미폐쇄 |

No-finding으로 판정한 핵심 의미에는 다음이 포함된다.

- Guard 실패 candidate가 `current`/`stageBest`/`solveBest`를 바꾸지 않는다.
- Rejected feasible completed step과 invalid/interrupted non-advance를 구분한다.
- Same content의 세 slot handle을 pairwise distinct하게 만든다.
- Wall clock은 quality/tie/seed input이 아니며 exceptional termination을 정상
  `MAX_STEPS_REACHED`로 바꾸지 않는다.
- Phase 07 `PASS`, final outcome와 publication eligibility를 Phase 06이 만들지 않는다.
- Phase 13/MIP/OR-Tools를 Phase 06 source/dependency에 선반영하지 않는다.
- Pre-review evidence → independent review → post-review acceptance receipt의
  단방향 DAG를 유지한다.

## 8. Target correction과 구분되는 실제 implementation blocker

다음은 Target이 이미 대체로 정확히 기록한 실제 blocker다. 이 review가 문서
교정 finding과 섞어 임의 해소하지 않는다.

- Phase 00은 implementation/evidence 제출이 있어도 independent review와
  acceptance receipt가 끝나지 않아 아직 accepted predecessor가 아니다.
- Phase 03, 04, 05 accepted implementation/evidence/review/receipt가 없다.
- Central pair-removal editor의 module/API/test/evidence owner가 Phase 05/06/
  Architecture 사이에서 미승인이다.
- Full-solution evaluator, business equality와 context tie seam이 Phase 03~06
  사이에서 미승인이다.
- Phase 06 explicit algorithm/acceptance/termination/seed/canonicalization config와
  scheduler implementer/reviewer identity가 승인되지 않았다.
- Phase 06 production/test Java와 `E-P06-COW`, `E-P06-ALNS`,
  `E-P06-REPLAY`는 없다.
- `Q-BENCH-02` official 수치, Phase 14A corpus/protocol/criteria/receipt,
  `C-17` approvals와 Phase 14B production authority는 여전히
  OPEN/GATED/NOT_GRANTED다.
- `Q-VAR-01`과 multi-trip/rotation은 deferred다.

Target correction은 위 구현 blocker를 “해결됨”으로 바꾸면 안 된다. Source,
Maven, publication과 handoff contract를 먼저 정확히 표현하고, 실제 scheduler가
accepted evidence를 확인한 뒤에만 implementation entry를 열어야 한다.

## 9. 정적 검사

Target과 이 Output만 검사하고 다른 문서·코드·POM/test/deployment는 수정하지
않았다.

| 검사 | 결과 | 비고 |
|---|---|---|
| Target H1/section | `PASS` | H1 1개, §1~§17 존재 |
| Target fence parity | `PASS` | Fence marker 76개 |
| Target trailing whitespace/tab/CRLF | `PASS` | 각각 0 |
| Target EOF newline | `PASS` | LF |
| Target local Markdown links | `PASS` | 45개, missing file 0 |
| Target GFM/explicit fragments | `PASS` | 29개, missing fragment 0 |
| Target declared HEAD Git blob | `PASS` | mismatch 0 |
| Canonical exact method diff | `FAIL` | Canonical 89, Target 66, missing 23 |
| Scoped Target diff | `PASS` | Target은 reviewer가 수정하지 않음 |
| Output whitespace | `PASS` | trailing whitespace/tab/CRLF 0, EOF LF |
| Scoped `git diff --check` | `PASS` | whitespace diagnostic 0 |
| Output-only scope | `PASS` | 새 review 파일 하나만 작성 |

Untracked 파일은 일반 `git diff --check -- <path>`에 나타나지 않으므로 Output에는
untracked-aware no-index whitespace 검사도 적용했다. No-index raw exit `1`은
새 파일 차이 자체이며 whitespace diagnostic은 0이다.

## 10. 최종 verdict

`CHANGES_REQUIRED`

Target은 Phase 06의 의미·안전 gate를 잘 설명하지만 source authority, exact
test inventory, Maven lifecycle, atomic publication과 Phase 07 handoff가 사람이
구현하고 독립 판정할 정도로 닫혀 있지 않다. 여섯 finding 모두 Target correction이
필요하다. Reviewer는 Target, README/progress, 다른 문서, 코드, POM, test,
deployment를 수정하지 않았다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=4 MEDIUM=2 LOW=0
REQUIRED_CORRECTION_FINDINGS: HG06-R001,HG06-R002,HG06-R003,HG06-R004,HG06-R005,HG06-R006

## Correction 01 읽기 전용 재검증

```yaml
recheck_observed_at: 2026-07-29T02:46:25+09:00
timezone: Asia/Seoul
recheck_round: 01
recheck_scope: HG06-R001~HG06-R006 correction closure only
review_before_append_sha256: 68b46956abddf2acf13e18ef179d59c4370e05b26528a4a1fd062df8c682d832
review_before_append_lines: 531
target_sha256_rechecked: 719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3
target_git_hash_object_rechecked: 45bee760233551432e9106f6b560bace653b4c8a
target_lines_rechecked: 2400
correction_report_sha256_rechecked: cbb22a9cb51401f728c038338089ae075079bc1cf433d4d34f84b22ef0d2b21c
correction_report_git_hash_object_rechecked: a760dfac244d2a8a4ae2a6d83ebf398a2ae6106e
correction_report_lines_rechecked: 317
head: 7cc890ee1d0805df5ae14b633127fade4f978639
branch: codex-implementation
```

### 재검증 방법과 읽은 source

Correction report의 `ADDRESSED_FINDINGS` 자기주장을 판정 근거로 사용하지 않았다.
고정한 Target을 줄 번호로 다시 읽고, 원 finding의 root cause와 required correction을
각각 canonical/original/adjacent/live source에 역대조했다. 범위는 여섯 finding과
그 correction이 만든 직접 regression으로 제한했으며 Phase 06 전체를 새로
리뷰하지 않았다.

| 읽은 파일 | 재검증 SHA-256 | 줄 수 | 사용 범위 |
|---|---|---:|---|
| `docs/README.md` | `5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569` | 67 | Current plain source map |
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | 1,648 | Canonical Master §1~4, §10~17 |
| `docs/domain-design.md` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | 1,607 | Current Domain, COW/state |
| `docs/architecture-design.md` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | 1,469 | Current module/POM/test DAG |
| `docs/2026-07-26-domain-design.md` | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | 1,886 | Dated implementation input |
| `docs/2026-07-26-architecture-design.md` | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | 1,019 | Dated module/package input |
| `docs/architecture-domain-implementation-design.md` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | 3,822 | Integrated Phase 05~07 경계 |
| `docs/master-design-open-questions.md` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | 87 | `Q-ALG-*`, `Q-BENCH-02`, `Q-VAR-01` |
| `docs/implementation/README.md` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | 240 | Implementation source authority |
| `docs/implementation/master-realization-plan.md` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | 943 | Phase 05~07, evidence/gate |
| `docs/implementation/execution-progress-and-results.md` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | 470 | Live scheduler/receipt 상태 |
| `docs/implementation/phases/phase-05-pair-insertion-initial-portfolio.md` | `9df624f8ba93d8de69f3f60e8cf3d7f74f7ac0e9c14fc7d99514bf6154595533` | 1,649 | Canonical producer handoff |
| `docs/implementation/phases/phase-06-cow-alns-reproducibility.md` | `aec051d91795011c8a589926e14058f8f4943f2cdda873b97e2e4bf613ab6d08` | 1,671 | Canonical Phase 06 전체 |
| `docs/implementation/reviews/phase-06-review.md` | `a33e998bfc9d28215cc2087d289fdb6980298b60d51793a0e817b77febbaf4ab` | 244 | 원 Phase 06 finding/test gate |
| `docs/implementation/phases/phase-07-independent-verification-final-result.md` | `1b0d7d92d961c44605307a5a66bd659ac54b4cb0832d275aa10c8bfb855de727` | 1,666 | Canonical consumer/no-solver 경계 |
| `docs/implementation/human-guides/phases/phase-05-human-implementation-guide.md` | `6a0cc49c39a8ad95181c9a0f04580b50b5a84a66e85558f8656829ca0fca788c` | 2,317 | 인접 seed/ownership/handoff |
| `docs/implementation/human-guides/phases/phase-06-human-implementation-guide.md` | `719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3` | 2,400 | Corrected Target 전체 |
| `docs/implementation/human-guides/phases/phase-07-human-implementation-guide.md` | `9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd` | 2,163 | 인접 schema/decoder/no-solver gate |
| `docs/implementation/human-guides/corrections/phase-06-correction-01.md` | `cbb22a9cb51401f728c038338089ae075079bc1cf433d4d34f84b22ef0d2b21c` | 317 | 교정 주장과 declared hash |
| `pom.xml` | `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c` | 237 | Live parent/Surefire policy |
| `rpdptw/solver/pom.xml` | `ac67af2ac73ab6a5d9f9418f4a5b54ed538c775fbad75a627f348885e3b0ad2e` | 21 | Live solver dependency closure |
| `.mvn/wrapper/maven-wrapper.properties` | `7613cd8a2f64216deb6c8f8443f2b339f2305d9b30fc3a4ea3088820a4788871` | 4 | Live wrapper identity |

Live inventory는 Target의 timestamped correction observation과 다시 분리했다.
재검증 시점에는 13개 POM, executable wrapper, `rpdptw/build/legacy` 아래
`package-info.java` 24개, 전체 `*Test.java`/`*IT.java` 15개가 있었으나 solver의
non-`package-info` production/test는 여전히 0개다. Progress는
`PHASE_00_FIX_02_IN_PROGRESS`, Phase 00 acceptance receipt는 `NOT_PRODUCED`,
전체 accepted implementation은 `0/15`다. Target §5의 과거 correction observation
23/14와 달라졌지만 Target이 이를 영구 current 사실로 쓰지 않고 implementation-entry
re-snapshot을 요구하므로 아래 `HG06-R005` 판정에 그 drift 자체를 failure로 세지
않았다.

### Finding별 closure 판정

#### HG06-R001 — `RESOLVED`

- **확인한 Target anchor:** Lines 179~216은 plain current map과 dated
  implementation input의 상충 선언을 양쪽 모두 적고
  `SOURCE_AUTHORITY_CONFLICT — BLOCKED`와
  `SOURCE_AUTHORITY_DECISION_RECEIPT`의 owner/path/supersedes/diff/WP/evidence
  필드를 요구한다. Lines 224~331은 두 source set의 읽기 순서와 fingerprint
  identity를 분리한다. Lines 591, 611, 1435~1476, 2161과 2347은 같은 gate를
  entry/WP/exit/traceability에 연결한다.
- **Source evidence:** Canonical Master §1.4와 `docs/README.md`는 plain pair를
  current detail로 지목하고, Implementation README §3와 Plan §2는 dated pair를
  고정한다. Target은 어느 쪽도 몰래 우선하지 않는다. Target의 HEAD blob 15개도
  실제 `HEAD:path`와 mismatch 0이었다.
- **Root cause closure:** 날짜 문서를 semantic diff/owner approval 없이 “Final”로
  복제한 원 root cause는 명시적 conflict/receipt gate로 닫혔다.
- **남은 risk:** Cross-document owner가 실제 canonical pair를 승인한 것은 아니다.
  Receipt 전 production/evidence가 계속 `BLOCKED`인 것은 finding 미교정이 아니라
  의도한 implementation blocker다.

#### HG06-R002 — `RESOLVED`

- **확인한 Target anchor:** Lines 1771~1866은 원 class 순서의 exact method를 모두
  싣고, lines 1868~1911은
  `PHASE06-CANONICAL-REQUIRED-METHODS-V1`, count 89, UTF-8/LF hash와
  class→WP→evidence mapping을 고정한다. Lines 1899~1905와 1951~2015는 fresh
  Surefire/Failsafe/architecture XML의 missing/extra/duplicate 및
  failed/error/skipped/stale/zero-test를 fail-closed한다. Lines 2034~2040,
  2192와 2349도 같은 manifest를 evidence/exit/traceability에 연결한다.
- **Source evidence:** Canonical Phase 06 §11.1의 `Class#method()` 89행과 Target
  89행을 표 순서로 normalize해 비교했다. 양쪽 count는 89, missing/extra/duplicate는
  모두 0이고 SHA-256은
  `1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86`로
  동일했다.
- **Root cause closure:** 교육 subset을 normative required inventory처럼 사용해
  23개를 놓친 원 false-green 경로가 제거됐다. Fault/corruption method의
  sensitivity receipt도 lines 1907~1911에 있다.
- **남은 risk:** 실제 solver test/XML은 0개다. Manifest 정의가 닫혔다는 것과
  구현/evidence가 존재한다는 것은 다르다.

#### HG06-R003 — `RESOLVED`

- **확인한 Target anchor:** Lines 510~549와 1951~1965는 live solver의 JUnit/
  test-fixture 부재, parent `failIfNoTests=false`, Failsafe execution 부재를
  `EFFECTIVE_POM_CONTRACT — BLOCKED`로 판정한다. Lines 1966~1996은 fresh
  `PHASE06_M2`, wrapper-only execution, upstream `-am clean install`,
  target-only Surefire/Failsafe, architecture bootstrap와 final root
  `clean verify` 순서를 제공한다. Lines 1431~1454는 이 절차를 모든 WP의
  선행 receipt로 만든다. Selected Surefire에는
  `surefire.failIfNoSpecifiedTests=true`, selected IT에는
  `failsafe.failIfNoSpecifiedTests=true`, 모든 실행에는 `clean`과 같은 isolated
  repository가 연결돼 있다.
- **Source evidence:** Current Architecture §5~§7/§18~§19의 parent/plugin/test
  lifecycle, dated Architecture §2/§5~§6, Canonical Phase 06 §13.1과 live POM을
  대조했다. Live root POM에는 여전히 Surefire 3.5.4
  `failIfNoTests=false`만 있고 Failsafe가 없으며, solver POM은 compile core
  dependency 하나뿐이다. Target은 이를 해결됐다고 주장하지 않는다.
- **Root cause closure:** Selected filter만 제시했던 원 guide와 달리 accepted
  effective-POM, JUnit/test-fixture, Failsafe lifecycle, reactor bootstrap,
  zero-test와 stale XML을 한 실행 계약으로 닫았다.
- **남은 risk:** 실제 POM correction과 Maven execution/receipt는 아직 없다.
  이 read-only recheck는 Phase 06 source/test가 0이고 POM 변경이 범위 밖이므로
  Maven test를 실행하지 않았다.

#### HG06-R004 — `OPEN`

- **확인한 Target anchor와 닫힌 부분:** Lines 864~1027은 executor를 unpublished
  pure derivation으로 한정하고 `AlnsEngine`, `AlnsStateSlot`,
  `StateIdentity`, `StatePublishResult`, `AlnsRunOutcome`을 추가했다. Lines
  1010~1022와 1213~1375는 final probe, one-shot publish, no partial visibility,
  no retry와 `decision.isAccepted()`를 명시한다. Lines 1548~1587은 publication
  count와 pre/post interruption fault matrix를 WP-06.3에 연결한다.
- **남아 있는 정확한 root cause:** 제안 API와 pseudocode가 아직 하나의
  compile/semantic contract로 닫히지 않는다.
  1. `AlnsStepExecutor`는 lines 901~907에서 `execute(...)`를 선언하지만
     pseudocode lines 1274와 1342는 `deriveStep(...)`을 정의·호출한다.
  2. `AlnsStepResult.Completed`는 lines 916~920에서
     `(outcome, after, evidence)`만 선언하지만 line 1334는 존재하지 않는
     `unpublished=true` component를 만들고 line 1356은 필요한 outcome/evidence
     없이 같은 `Completed`를 published 결과처럼 다시 만든다.
  3. `AlnsEngine.run(...)`은 lines 950~1007에서 `AlnsRunOutcome`을 반환하지만
     lines 1340~1360의 `runOneStep`은 `AlnsStepResult`처럼 반환한다. 두 경계의
     관계와 exact return type이 없다.
  4. `StatePublishResult.Conflict(actualIdentity)`는 expected `before`와 다른 visible
     identity를 뜻하는데 line 1360은 여전히
     `Failed(lastCommitted=before, cause=publication)`를 반환한다. Conflict가
     실제 가능하다면 `before`는 last committed state가 아니고, 단일-owner 규칙상
     불가능한 invariant defect라면 API/owner exposure와 fault outcome이 그렇게
     말해야 한다.
- **사람에게 미치는 영향:** 구현자는 method/result type을 임의로 고치고 unpublished
  derivation과 published completion을 같은 variant로 혼용하거나, conflict에서
  stale state를 last-safe point로 보고할 수 있다. 이는 원 finding의
  publication/interruption linearization과 failure oracle을 다시 불명확하게 한다.
- **Source evidence:** Canonical Master §11.4/§13.1, Canonical Phase 06
  §7.2/§8.1~§8.2, 원 review `F-P06-006/010/011`과 본 review의 original
  `HG06-R004` required correction은 minimum compile-closed engine/run outcome,
  unique publication operation과 truthful conflict/failure outcome을 요구한다.
- **Required correction:** Target에서 executor method 이름과 signature를 하나로
  통일하고, unpublished derivation result와 published engine step/run outcome을
  서로 다른 명시적 type/variant로 연결한다. `AlnsEngine.run`과 `runOneStep`의
  exact return/termination mapping도 같은 skeletal contract로 맞춘다. Conflict를
  허용하면 actual visible state/identity를 원자적으로 읽어 실제
  `lastCommitted`를 반환하고, exclusive single-owner 아래 불가능한 defect라면 slot
  exposure와 outcome/test가 그 invariant failure를 명시해야 한다. No retry,
  unchanged-by-this-call, publication-count와 pre/post signal oracle은 유지한다.
- **Target 수정 필요 여부:** `YES`
- **남은 risk:** Concrete memory primitive와 cross-Phase approval는 그 뒤에도
  implementation gate로 남는다. 이번 판정은 새 broad finding이 아니라 원
  `HG06-R004` closure 미완료다.

#### HG06-R005 — `RESOLVED`

- **확인한 Target anchor:** Lines 467~504는 multi-root `rg --files` 명령과
  `HEAD snapshot`/human-review observation/correction observation/
  implementation-entry re-snapshot을 분리한다. Lines 506~528은
  존재·placeholder·부재와 timestamped POM/wrapper/progress byte identity를
  acceptance에서 분리한다. Lines 530~549는 live build success/stale XML을
  Phase 06 evidence로 재사용하지 못하게 한다.
- **Source evidence:** 재검증 live inventory는 13 POM, wrapper present,
  package-info 24, 전체 test 15, solver Phase 06 production/test 0이었다.
  Progress hash는 `9361ae89...bf08c`이고 Phase 00은 Fix 02 진행 중이며 receipt가
  없다. Target의 23/14와 다른 이 변화는 correction report도 shared-worktree
  drift로 별도 기록했고, Target은 entry에서 다시 봉인하라고 요구한다.
- **Root cause closure:** 서로 다른 live 순간을 하나의 영구 “현재”로 합치고
  root `src/**`만 보던 원 명령 문제는 닫혔다.
- **남은 risk:** Shared worktree는 계속 변한다. 다음 구현자는 반드시 새 timestamp,
  status/POM/progress hash와 count를 다시 고정해야 한다.

#### HG06-R006 — `RESOLVED`

- **확인한 Target anchor:** Lines 858~862는 solver internal/Phase 07 proposed
  type 양방향 import를 금지한다. Lines 2257~2278은
  `PHASE06_07_HANDOFF_SCHEMA_DECISION — BLOCKED`와 no-default를 두고, lines
  2280~2305는 core-owned minimal vocabulary와 neutral versioned artifact 두
  대안, owner/module/package, 양쪽 mapper, version/order/encoding/digest,
  required/optional, unknown/missing/mismatch와 compatibility/corruption test를
  field group별로 닫는다. Lines 620, 1453~1454, 2066, 2196~2198와 2369는
  entry/WP/evidence/exit/traceability에 같은 receipt를 연결한다.
- **Source evidence:** Canonical Architecture의 solver→core,
  verification→core/no-solver DAG와 Canonical Phase 06 §7.1/§16.2를 보존한다.
  Current human Phase 07 lines 407, 426~431, 790, 824~838과 2143도 같은
  two-option/no-default gate, independent decoder와 solver import 0을 요구한다.
- **Root cause closure:** Public/wire schema 최종 승인을 미루면서 당장 필요한
  internal handoff owner까지 비워 둔 원 문제가 명시적 cross-Phase decision
  receipt로 닫혔다.
- **남은 risk:** 실제 owner/path와 mapper/decoder compatibility evidence는 아직
  승인·구현되지 않았다. Receipt 전 Phase 07 handoff가 `BLOCKED`인 것이 정확하다.

### 정적·manifest 재검증

| 검사 | 결과 | 근거 |
|---|---|---|
| Target hash/line | `PASS` | SHA-256 `719ec2dc...fc3d3`, Git object `45bee760...c8a`, 2,400줄 |
| Correction report hash/line | `PASS` | SHA-256 `cbb22a9c...b21c`, Git object `a760dfac...106e`, 317줄 |
| H1/heading/duplicate anchor | `PASS` | H1 1개, heading 76개, duplicate generated anchor 0 |
| Fence | `PASS` | Fence marker 80개, unclosed fence 0 |
| Local Markdown/GFM fragment | `PASS` | Local link 63개, fragment 37개, missing file/heading/explicit `<a id>` 0 |
| Declared HEAD fingerprint | `PASS` | Target §3.3의 Git blob 15개, `HEAD:path` mismatch 0 |
| Canonical method manifest | `PASS` | Original 89 = Target 89; missing/extra/duplicate 0; exact SHA-256 `1146dc4e...c86` |
| Target whitespace/EOF | `PASS` | trailing whitespace/tab/CR 0, EOF LF |
| Live manifest precondition | `BLOCKED AS EXPECTED` | Solver Phase 06 source/test/XML 0; accepted effective-POM/receipt 없음 |
| Maven execution | `NOT RUN` | Read-only scope, Phase 06 test 0, actual POM gate 미충족 |
| Scoped normal `git diff --check` | `PASS` | 이 review path whitespace diagnostic 0 |
| Untracked-aware no-index `--check` | `PASS` | Raw exit 1은 append/new-file content 차이이며 whitespace diagnostic 0 |
| Write scope | `PASS` | 자기 review 끝의 이 절만 append; Target/correction/source/POM/code/test 미수정 |

Correction 01은 `HG06-R001`, `R002`, `R003`, `R005`, `R006`의 문서 root cause를
닫았다. 그 밖의 original invariant, hidden-default, Phase 13/14 gate,
security/observability, replay oracle와 adjacent ownership은 이번 좁은 범위에서
새 regression을 만들지 않았다. 그러나 `HG06-R004`의 proposed Java API와
pseudocode가 여전히 동일한 실행 contract를 표현하지 않으므로 Target correction은
한 차례 더 필요하다. 실제 Phase 06 구현/acceptance는 이 verdict와 별개로 계속
`BLOCKED/NOT_STARTED/NOT_PRODUCED/NOT_ACCEPTED`다.

RECHECK_ROUND: 01
RECHECK_VERDICT: FURTHER_CORRECTION_REQUIRED
RESOLVED_FINDINGS: HG06-R001,HG06-R002,HG06-R003,HG06-R005,HG06-R006
OPEN_FINDINGS: HG06-R004
TARGET_HASH_RECHECKED: 719ec2dc7fae721315966c8f3fa0e41ab0022a4fbce8b2a0765c4c36fa7fc3d3
CORRECTION_REPORT_HASH_RECHECKED: cbb22a9cb51401f728c038338089ae075079bc1cf433d4d34f84b22ef0d2b21c

## Correction 02 읽기 전용 재검증

```yaml
recheck_observed_at: 2026-07-29T03:04:09+09:00
timezone: Asia/Seoul
recheck_round: 02
recheck_scope: HG06-R004 closure only
review_before_append_sha256: 988dae6f61a07d0b3d3216c3cc402987e850cd166759cebaba795c1aed888598
review_before_append_lines: 775
target_sha256_rechecked: df232eebc269601f5a9964a5856a27c731c6fa23229aea5c9b318c899fe6df23
target_git_hash_object_rechecked: 39c647b9146eee2c3340ead62f29bcb8835e0d31
target_lines_rechecked: 2567
correction_report_sha256_rechecked: f42da3798ae2a5c19055b673969619c1e1b88ebbdd9e319f784385e0415620ed
correction_report_git_hash_object_rechecked: 8efc7604bff6cf32239f9cfbccf1224198886aa3
correction_report_lines_rechecked: 233
head: 7cc890ee1d0805df5ae14b633127fade4f978639
branch: codex-implementation
```

### 재검증 source와 방법

Correction 02의 `ADDRESSED_FINDINGS`와 consistency 표를 그대로 승인하지 않았다.
Target의 변경 전 Git object
`45bee760233551432e9106f6b560bace653b4c8a`와 변경 후 object
`39c647b9146eee2c3340ead62f29bcb8835e0d31`의 blob diff를 직접 읽고, 현재 Target
§8.3/§9.1~§9.2/WP-06.3/test/evidence/exit/traceability를 다음 source에
역대조했다.

| Source | SHA-256 | 직접 대조한 요구 |
|---|---|---|
| `docs/master-design.md` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | §11.4 aggregate completed step, §13.1 incomplete non-advance/exact termination |
| `docs/implementation/phases/phase-06-cow-alns-reproducibility.md` | `aec051d91795011c8a589926e14058f8f4943f2cdda873b97e2e4bf613ab6d08` | §7.2/§8.1~§8.2 state result, final probe, one publication |
| `docs/implementation/reviews/phase-06-review.md` | `a33e998bfc9d28215cc2087d289fdb6980298b60d51793a0e817b77febbaf4ab` | `F-P06-006`, `F-P06-010`, `F-P06-011` |
| `docs/implementation/human-guides/corrections/phase-06-correction-01.md` | `cbb22a9cb51401f728c038338089ae075079bc1cf433d4d34f84b22ef0d2b21c` | Round 01의 owner/publication 교정 의도 |
| 이 review의 Correction 01 recheck | append 전 SHA-256 `988dae6f61a07d0b3d3216c3cc402987e850cd166759cebaba795c1aed888598` | `HG06-R004 — OPEN`의 네 정확한 미폐쇄 원인과 required correction |
| `docs/implementation/human-guides/corrections/phase-06-correction-02.md` | `f42da3798ae2a5c19055b673969619c1e1b88ebbdd9e319f784385e0415620ed` | 교정 주장 검증 대상, authority 아님 |

### HG06-R004 closure 판정 — `RESOLVED`

#### 1. Executor signature와 unpublished derivation

- **Target anchor:** Lines 901~949.
- `AlnsStepExecutor`의 유일한 method는
  `execute(AlnsSearchState, AlnsExecutionConfig, StepRandomStreams,
  InterruptionProbe) → AlnsStepDerivation`이다.
- `AlnsStepDerivation.Completed`는 정확히
  `(IterationOutcome outcome, AlnsSearchState unpublishedAfter,
  StepEvidence evidence)` 세 component를 가진다.
- Target 전역 symbol 검사에서 stale `AlnsStepResult`, `deriveStep(...)`,
  `unpublished=true`는 각각 0건이다. Executor는 state slot/publisher를 소유하지
  않고 unpublished immutable value만 만든다.

#### 2. Unpublished derivation과 published step/run outcome 분리

- **Target anchor:** Lines 951~1055.
- `AlnsStepDerivation.Completed`, `AlnsEngineStepOutcome.Published`,
  `AlnsRunOutcome.Normal/Exceptional/Failed`는 서로 다른 sealed hierarchy와
  constructor다. Inheritance/cast/same-constructor reuse를 금지한다.
- `VisibleAlnsState(state, identity, publicationCount)`와
  `AlnsStateSlot.readVisible()`가 visible state/identity/count를 하나의 atomic
  snapshot으로 묶는다.
- Pseudocode의 derivation `Completed` constructor는 lines 1415~1416에서 선언과
  같은 세 component를 한 번만 만들고, slot `Published` 뒤 lines 1452~1453에서
  별도 engine `Published(outcome, committed, evidence)`를 만든다. 생략된
  component나 published `Completed` 재사용이 없다.

#### 3. `runOneStep` ↔ `run`과 termination mapping

- **Target anchor:** Lines 955~1048, 1084~1098, 1422~1502.
- Interface는
  `runOneStep(...) → AlnsEngineStepOutcome`과
  `run(...) → AlnsRunOutcome`을 명시한다.
- Derivation invalid/failed는 step/run `Failed`, interruption/final pre-publish
  signal은 exact `Exceptional`, slot publish 성공만 step `Published`가 된다.
  Requested completed count에 정확히 도달한 경우만 Phase 06 run
  `Normal(MAX_STEPS_REACHED, visible)`가 된다.
- Post-publication signal은 이미 published step을 유지하고 다음 run boundary에서
  exact exceptional termination으로 fold된다. Phase 10 소유
  `NO_STRICT_IMPROVEMENT`/`MAX_ROUNDS_REACHED`를 Phase 06이 생성하지 않는다.
- 모든 pseudocode branch는 선언된 accessor/component를 제공하며 derivation
  object를 engine/run outcome으로 직접 반환하지 않는다.

#### 4. Atomic conflict와 truthful `lastCommitted`

- **Target anchor:** Lines 972~1103, 1455~1469.
- `StatePublishResult.Conflict(actualVisible)`와
  `Failed(actualVisible, failure)`는 결과 linearization 시점의 atomic
  state/identity/count snapshot을 반환한다.
- Conflict는 valid single-owner 실행의 정상 경쟁이 아니라
  `PUBLICATION_OWNERSHIP_INVARIANT_VIOLATION`으로 fail-closed하며, engine
  `Failed`와 run `Failed`가 stale expected `before`가 아닌 `actualVisible`을
  `lastCommitted`로 전달한다.
- 다른 unauthorized publisher가 이미 invariant를 깨뜨렸다면 actual count가
  `before`와 다를 수 있음을 명시하므로 “total count unchanged”라는 거짓
  oracle을 유지하지 않는다. Target 전역에서 stale
  `lastCommitted=before` 표현은 0건이다.

#### 5. No-retry, publication-count와 pre/post-signal oracle

- **Target anchor:** Lines 1057~1098, 1291~1346, 1439~1501,
  WP-06.3 lines 1695~1744.
- Publish success는 entire aggregate visibility와
  `before.publicationCount + 1`을 요구한다. Conflict/failure는 derived state를
  이 call로 publish하지 않고 call 귀속 successful-publication delta가 0이며
  내부 retry가 없다.
- Final pre-publication signal은 `before` atomic snapshot을 보존하고 publication
  call 0으로 exceptional 종료한다. Post-publication signal은 completed step/count를
  보존하고 다음 run boundary의 exceptional termination이 된다.
- `acceptedImprovementPublishesOneImmutableNextState()`의 fixture/pass criterion,
  `interruptionRaceUsesPublicationAsSingleLinearizationPoint()`,
  WP-06.3 expected, CP-3, `E-P06-ALNS`, exit checklist와
  `REQ-SINGLE-PUBLISH`가 동일한 type/constructor/count/conflict/signal oracle을
  참조한다. 기존 canonical method 이름을 추가·삭제·변경하지 않았다.

#### Root cause closure와 residual risk

Round 01의 네 미폐쇄 원인인 method-name 불일치, 존재하지 않는
`unpublished` component/incomplete constructor, step/run 반환형 부재, conflict의
stale `before` 보고가 모두 제거됐다. 구현자가 이 경계에서 새 type/constructor나
last-safe 의미를 발명할 필요가 없으므로 `HG06-R004`는 문서 root cause 수준에서
`RESOLVED`다.

Concrete `AtomicReference`/VarHandle/lock 선택, publish-capability composition root,
실제 Java compile/test/evidence와 cross-Phase API 승인은 여전히
`PROPOSED INTERNAL, REVIEW REQUIRED`/entry blocker다. 이는 Target이 구현 완료를
주장하지 않고 보존한 residual implementation gate이며 `HG06-R004`를 다시 여는
문서 결함이 아니다. Correction 02 직접 변경 범위에서 별도 NEW finding은 없다.

### 정적·보존 검사

| 검사 | 결과 | 근거 |
|---|---|---|
| Target/report hash와 line | `PASS` | Target `df232eeb...6df23`/2,567줄; Correction 02 `f42da379...20ed`/233줄 |
| Correction 01→02 blob scope | `PASS` | R004 type/owner/pseudocode/WP/test/evidence/exit/traceability diff로 한정 |
| Declaration/pseudocode stale symbol | `PASS` | `AlnsStepResult`, `deriveStep(...)`, `unpublished=true`, stale `lastCommitted=before` 모두 0 |
| Canonical exact method manifest | `PASS` | Canonical 89 = Target 89; missing/extra/duplicate 0; SHA-256 `1146dc4ee53b08d4059e80dfee40602a40919a5918c6792e7eceea4634b07c86` |
| Target declared HEAD fingerprints | `PASS` | Git blob 15개, `HEAD:path` mismatch 0 |
| Target local link/GFM fragment | `PASS` | Link 63개, fragment 37개, missing file/fragment 0 |
| Correction report local link/GFM fragment | `PASS` | Link 9개, fragment 3개, missing file/fragment 0 |
| H1/heading/duplicate anchor | `PASS` | Target 1/76/0, Correction report 1/13/0 |
| Fence/whitespace/EOF | `PASS` | Target fence 80, report fence 4; unclosed 0; trailing whitespace/tab/CR 0; EOF LF |
| Maven/Java execution | `NOT RUN` | Read-only document recheck, Phase 06 source/test/evidence 0과 accepted effective-POM 부재 |
| Scoped normal `git diff --check` | `PASS` | Review path whitespace diagnostic 0 |
| Untracked-aware no-index `--check` | `PASS` | Raw exit 1은 untracked content 차이이며 whitespace diagnostic 0 |
| Write scope | `PASS` | 이 review 끝에 Correction 02 절만 append; Target/report/source/code/POM/test 미수정 |

Correction 01에서 이미 `RESOLVED`로 판정한 `HG06-R001`, `R002`, `R003`,
`R005`, `R006`의 상태는 변경하지 않았다. Round 02 범위인 `HG06-R004`도
`RESOLVED`되어 이 review의 required correction finding은 누적 기준 모두 닫혔다.
이는 Phase 06 implementation/acceptance가 열렸다는 뜻이 아니며 실제 상태는 계속
`BLOCKED/NOT_STARTED/NOT_PRODUCED/NOT_ACCEPTED`다.

RECHECK_ROUND: 02
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: HG06-R004
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: df232eebc269601f5a9964a5856a27c731c6fa23229aea5c9b318c899fe6df23
CORRECTION_REPORT_HASH_RECHECKED: f42da3798ae2a5c19055b673969619c1e1b88ebbdd9e319f784385e0415620ed
