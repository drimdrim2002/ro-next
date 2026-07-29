# Phase 08 사람용 구현 가이드 독립 리뷰

```yaml
review_status: COMPLETE
review_type: INDEPENDENT_HUMAN_IMPLEMENTATION_GUIDE_REVIEW
reviewed_at: 2026-07-29T01:46:17+09:00
live_inventory_revalidated_at: 2026-07-29T01:49:00+09:00
review_scope: docs/implementation/human-guides/phases/phase-08-human-implementation-guide.md
output_scope: docs/implementation/human-guides/reviews/phase-08-review.md
reviewer_relation: target 작성 작업과 다른 독립 작업
head_commit: 7cc890ee1d0805df5ae14b633127fade4f978639
branch_observed: codex-implementation
target_status_at_review: untracked pre-existing file
target_lines_before_review: 2599
target_sha256_before_review: 2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5
target_git_blob_before_review: 2c7dd10a6b8f5a5fb04618cd525b0a4882ba6223
target_modified_by_reviewer: false
implementation_status_observed: BLOCKED_NOT_IMPLEMENTED
phase_acceptance_status_observed: NOT_ACCEPTED
phase08_evidence_status_observed: NOT_PRODUCED
phase00_live_status_observed: CHANGES_REQUIRED_FIX_01_IN_PROGRESS
phase00_acceptance_status_observed: NOT_ACCEPTED
verdict: CHANGES_REQUIRED
target_changes_required: true
finding_counts:
  critical: 0
  high: 2
  medium: 3
  low: 0
required_correction_findings:
  - F-HG-P08-001
  - F-HG-P08-002
  - F-HG-P08-003
  - F-HG-P08-004
  - F-HG-P08-005
```

## 1. 결론

대상은 Phase 08의 의미, 선행 gate, provider-neutral 경계, both-gate publication,
hidden default 금지, security·failure·observability·reproducibility, evidence DAG와
Phase 13/14 권한 분리를 폭넓게 설명한다. 문서와 실제 구현을 혼동하지 않고 현재
Phase 08을 `BLOCKED_NOT_IMPLEMENTED / NOT_ACCEPTED / NOT_PRODUCED`로 둔 판정도
맞다.

그러나 사람이 이 가이드만 따라 실제 구현을 시작하기에는 필수 교정이 남아 있다.

1. 현재 설계 문서 지도가 최상위 Domain/Architecture로 지정한
   `docs/domain-design.md`, `docs/architecture-design.md`를 읽지 않고 날짜 고정
   구문서를 권위 source로 사용한다.
2. `LocalCliMain`은 `apps/cli`에, 실제 composition root는 그 하위 consumer인
   `distributions/local`에 두어 선언된 compile DAG만으로 executable bootstrap을
   만들 수 없다.
3. 정확한 Phase 07 Java 계약은 top-level 2-variant + nested 2-variant인데 일부
   pseudocode·WP·test 문구는 이를 직접 3-variant switch처럼 지시한다.
4. reusable contract test artifact와 owner-POM selected test의 clean-checkout
   dependency 경로가 닫히지 않았다.
5. 최종 live inventory hash와 Phase 00 상태가 review 시점의 실제 checkout보다
   뒤처졌다.

첫 두 항목은 source authority와 mandatory local runtime의 실행 구조를 훼손하므로
`HIGH`다. 나머지 세 항목은 현재 Phase 08의 기존 entry blocker와 별개인 문서
실행성·재현성 결함이며 `MEDIUM`이다. 따라서 verdict는
`CHANGES_REQUIRED`, target 수정은 `YES`다.

## 2. Review 기준과 fingerprint

### 2.1 Target과 직접 기준 문서

| 문서 | 사용 범위 | 줄 수 | Git blob 또는 live hash | SHA-256 |
|---|---|---:|---|---|
| Target guide | 전체 §1~§17 | 2,599 | live Git object `2c7dd10a6b8f5a5fb04618cd525b0a4882ba6223` | `2fcb57b24104d91e9e24b07c2debf431c3bff594b222e554bfb3e3a7fd4e0ec5` |
| Canonical Master | §1, §4, §10, §13~17 | 1,648 | HEAD/live `b507a5e7ba0b7e76475bc2d755493e814f4d053a` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` |
| Current Domain Design | §1, §3, §10, §13~18 | 1,607 | HEAD/live `ace117c380466b733994a1fbb2a95d31e41b3959` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` |
| Current Architecture Design | §5~7, §10, §12, §17~19 | 1,469 | HEAD/live `81495ff448d0e618ab3563e8ff80614fb1028acf` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` |
| Integrated Design | §3, §12~13, §19~25 | 3,822 | HEAD/live `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` |
| Open Questions | exact Q rows와 gate 상태 | 87 | HEAD/live `3fff4c583a54f02dea667e78c8e5187d65ec0e18` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` |
| Master Realization Plan | authority, Phase 07~10, evidence/DoD | 943 | HEAD/live `d7f6be4fff0089204fbdb52f731b2348407f36eb` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` |
| Implementation README | 15 Phase DAG와 status authority | 240 | HEAD/live `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` |
| Execution Progress | §2, §5, live §10 | 439 | HEAD `250aa90ae568a6b32ec905fa5ee456d430ff72cf`; live `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d` | live `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` |
| Canonical Phase 08 | 전체 | 2,563 | HEAD/live `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` | `15dbcab53fc00fc4d3062c5bdb0eb1f072fd902bfbb59b322859f30b090a5f0f` |
| Canonical Phase 08 review | 전체 F-P08-001~013 | 460 | HEAD/live `7983d3b09e2f4c45b8cd132e3627c053c9e3e8ce` | `95d0b005d6218af7c2e5e8d720419c6fd781d0f97f9abddff2fd0132b52264f7` |
| Phase 07 human guide | producer contract/handoff | 1,878 | live `f5e4d82c1a0119ba9dfb56031ae470a2a897cfba` | `43b996382bd2b54e7fbb7b7ab218d20c50d76dc8fe535ec859937e7138a3d05d` |
| Phase 09 human guide | storage consumer boundary | 2,079 | live `1c31c4747362317c7f4ec8fb63c5f57627019769` | `67fb810d94171181309eb3fa898bbfee6572895267d93790d38a144522aa9c54` |

날짜 고정 `docs/2026-07-26-domain-design.md`와
`docs/2026-07-26-architecture-design.md`도 대상이 실제로 인용하므로 대조했지만,
현재 `docs/README.md`가 가리키는 최상위 Domain/Architecture를 대신하는 권위
source로 취급하지 않았다. Historical 문서는 설명의 provenance로만 사용했다.

### 2.2 HEAD와 live implementation inventory

검토 시점의 HEAD는 `7cc890ee1d0805df5ae14b633127fade4f978639`이고 tracked
baseline은 root 단일 JAR, main/test Java `6/1`, GCP/Jackson placeholder다.

Live checkout은 동시 Phase 00 작업 때문에 다음 상태다.

- Root `pom.xml` live Git object:
  `1dc675ba17b7f2202f34a22131f152cc2868b075`
- Root reactor: `rpdptw`, `build`, `legacy`
- `rpdptw/application` main Java: concrete type 0, `package-info.java` 4
- 전체 `rpdptw/**` main Java: `package-info.java` 23, production type 0
- 전체 `rpdptw/**` test Java: 0
- `build/**/src/test/java` Java: 9
- `adapters/legacy-win-json`, `adapters/object-filesystem`, `apps/cli`: 빈 디렉터리
- `distributions`, `build/port-contract-tests`: 부재
- Phase 08 `E-P08-*`: 부재
- Phase 00: `CHANGES_REQUIRED_FIX_01_IN_PROGRESS / NOT_ACCEPTED`
- Phase 00 review: `COMPLETED_CHANGES_REQUIRED`, 5 major findings
- Phase 00 evidence: 기존 bundle SHA-256
  `acdac94dc2071744320b154ab88c2ab1ac1087858d9eabc39fe63e78759501ee`는
  `REJECTED_PENDING_FIX_01_REGENERATION`
- Phase 08: 계속 `BLOCKED_NOT_IMPLEMENTED / NOT_ACCEPTED`

이 live Phase 00 산출물은 Phase 08 acceptance가 아니며 본 review는 POM, Java, test,
deployment, evidence와 progress를 수정하지 않았다.

## 3. 방법

1. Target 2,599줄을 처음부터 끝까지 읽고 metadata, primer, scope, proposed Java,
   test plan, WP-08.0~7, command/evidence, exit/rollback/handoff/traceability를
   section별로 대조했다.
2. Canonical 5문서와 implementation master plan/README/progress를 읽고 현재
   authority, Phase 08 경계, Phase 13/14 gate와 상태 변경 권한을 고정했다.
3. 원본 Phase 08과 독립 review의 F-P08-001~013이 target에 손실 없이 전달됐는지
   확인했다.
4. Phase 07 exact sealed hierarchy와 Phase 09 consumer blocker를 대조해 인접
   lifecycle/state/ownership을 검사했다.
5. Phase 07/09 human guide와 비교해 신규 독자 설명, fixture/oracle, red→green,
   false-green, category/pass 기준의 일관성을 확인했다.
6. HEAD tree와 live POM/source/test/evidence inventory를 분리하고 구체 Java type,
   Maven test artifact, CLI/bootstrap dependency가 실제로 가능한지 정적 검사했다.
7. 상대 파일 링크, GFM fragment, fence, heading, whitespace와 scoped diff를
   검사했다. 현재 Phase 08 type/test가 없고 entry gate가 닫혀 있으므로 future
   Maven command를 성공 evidence로 실행하지 않았다.

## 4. Summary

| 영역 | 판정 | 근거 |
|---|---|---|
| 원문 불변조건·Phase 경계 | PASS | Provider-neutral application, both-gate, no normal payload on failure, no Phase 09/10/11 선구현을 명시 |
| 신규 독자 이해 | PASS_WITH_FINDINGS | Primer와 lifecycle은 좋으나 current authority와 executable composition 설명이 충돌 |
| WP 실행·판정성 | CHANGES_REQUIRED | WP별 precondition/action/failure/rollback은 좋으나 bootstrap/test dependency 경로가 닫히지 않음 |
| Java/Maven proposed API 실제성 | CHANGES_REQUIRED | Phase 07 nested switch와 CLI composition/Test JAR 경로 교정 필요 |
| 코드 과다·추상성 | PASS | 코드가 proposed skeleton임을 반복 표시하고 open contract를 억지로 확정하지 않음 |
| Entry/exit/evidence/rollback | PASS | AND gate, immutable evidence DAG, stop/resume/last-safe와 legacy rollback이 명시됨 |
| Failure/security/observability/reproducibility | PASS | Failure taxonomy, access blocker, symlink/limit/redaction, cancel/deadline/retry/clock 분리가 충분함 |
| Hidden default·Phase 13/14 gate | PASS | `Q-BENCH-02`, C-17, Phase 14A/14B와 production authority를 정확히 차단 |
| 인접 ownership/lifecycle/state | PASS_WITH_FINDINGS | 의미는 보존하지만 Phase 07 Java branch 구조를 일부 평탄화 |
| 링크/GFM/fingerprint/traceability | CHANGES_REQUIRED | 링크는 정상이나 canonical Domain/Architecture fingerprint 누락과 live hash stale |
| 사람 승인/안전 지점 | PASS | owner/checkpoint/stop/rollback과 scheduler-only 상태 권한이 명확함 |
| 문서/실제 구현 구분 | PASS | placeholder/scaffold/future command/evidence 부재를 구현 완료와 분리 |
| Test fixture/builder/oracle/red→green/false-green/category/pass | PASS_WITH_FINDINGS | 개념 coverage는 충분하나 reusable contract artifact와 clean-checkout command가 미완결 |

## 5. Findings

### F-HG-P08-001 — 현재 canonical Domain/Architecture 대신 날짜 고정 구문서를 권위 source로 사용한다

- **Severity:** `HIGH`
- **Finding:** Target metadata와 source fingerprint 표는
  `docs/2026-07-26-domain-design.md`,
  `docs/2026-07-26-architecture-design.md`를 각각 `Final Domain`,
  `Final Architecture`로 읽게 하고 현재
  `docs/domain-design.md`, `docs/architecture-design.md`를 전혀 고정하지 않는다.
  읽기 순서와 traceability도 구문서의 section 번호를 사용한다.
- **사람 영향:** 신규 구현자는 현재 Domain의 state/result/error 계약과 현재
  Architecture의 module/app composition, local runtime, port, security/test 절을
  읽지 않고 오래된 section 번호를 따라간다. 이 때문에 target의
  `distributions/local`/`object-filesystem` 분할 같은 새 제안과 현재
  `apps/cli` composition-root 계약의 충돌을 발견하지 못하고, source 변경 impact
  review도 잘못된 두 blob에 대해서만 수행한다.
- **Target 위치:** metadata 33~38줄, source hierarchy 211~238줄, 읽기 순서
  261~275줄, WP 근거 1542/1623/1979줄, traceability 2572~2591줄.
- **Source section:** `docs/README.md` “현재 최상위 진입점”, “권장 읽기 순서”,
  “문서 계층과 규범 지위”; current Domain §1/§3/§10/§13~18; current
  Architecture §5~7/§10/§12/§17~19.
- **Root cause:** Canonical Phase 08과 human-guide README가 사용한 historical
  implementation hierarchy를 그대로 복제하고, reviewer 요청의 canonical 5문서 및
  현재 `docs/README.md`와 재조정하지 않았다.
- **Required correction:** Target metadata/source 표/읽기 순서/근거/traceability에
  current `domain-design.md` blob `ace117...`과 current
  `architecture-design.md` blob `81495f...`을 넣고 실제 관련 section으로 다시
  매핑한다. 날짜 고정 두 문서는 historical/supplemental input으로 낮춘다.
  Current와 historical 사이의 module/runtime/result 차이가 Phase 08 requirement,
  WP, tests, evidence에 주는 semantic impact를 명시한다.
- **Target 수정:** `YES`
- **구현 blocker 구분:** `DOCUMENTATION AUTHORITY BLOCKER`. 이미 기록된 Phase 00~07
  acceptance/cross-Phase blocker와 별개이며, source receipt가 교정되기 전 Phase 08
  구현 기준을 freeze하면 안 된다.
- **Residual risk:** Upstream implementation plan도 historical hierarchy를 포함하므로
  target만 수정한 뒤에도 owner가 source 충돌을 한 번 승인해야 한다.

### F-HG-P08-002 — CLI entrypoint와 composition root의 선언된 dependency 방향이 bootstrap을 막는다

- **Severity:** `HIGH`
- **Finding:** Target tree는 executable 이름인 `LocalCliMain`을 `apps/cli`에 두고
  concrete capability/profile/filesystem을 조립하는 `LocalCompositionRoot`와
  `LocalRuntimeBootstrap`을 `distributions/local`에 둔다. 동시에 compile DAG는
  `distributions/local → apps/cli`만 허용한다. 따라서 `LocalCliMain`은 자신보다
  downstream인 composition root를 import할 수 없고, 문서에는 distribution 쪽
  executable main, injected runner SPI 또는 ServiceLoader bootstrap도 없다.
- **사람 영향:** 그대로 구현하면 `apps/cli → distributions/local` 역의존/cycle을
  만들거나, `main()`이 application/filesystem/profile을 직접 조립해 선언된
  composition ownership을 우회한다. 어느 경우든 mandatory local CLI와 real E2E를
  정상 packaging할 수 없다.
- **Target 위치:** change tree 758~775줄, compile dependency 790~821줄, 책임표
  832~833줄, WP-08.4 1778~1803줄.
- **Source section:** current Architecture §6.6은 app module을 composition root로
  지정하고 `apps/cli`를 local/offline entrypoint로 둔다. Integrated §12.4는 local
  reference flow를, Canonical Phase 08 §5는 target과 같은 proposed split을 제시하므로
  둘 사이의 명시적 resolution이 필요하다.
- **Root cause:** CLI parser/presenter module, executable distribution module과
  composition root를 서로 다른 층으로 나누면서 실제 `main()` 호출 방향을 설계하지
  않았다.
- **Required correction:** 둘 중 하나를 authority/ADR와 함께 고정한다.
  (A) current Architecture처럼 `apps/cli`를 composition root로 두고 필요한 local
  adapter/profile dependency를 app assembly에 둔다. 또는 (B)
  `apps/cli`를 parser/runner/presenter library로 만들고 executable `main`과
  bootstrap/composition을 `distributions/local`로 옮긴다. 선택한 DAG를 compile
  architecture test, `java -jar`/launcher smoke test와 WP-08.4/6 command에 연결한다.
- **Target 수정:** `YES`
- **구현 blocker 구분:** `IMPLEMENTATION-START BLOCKER`. Phase 08 entry가 열려도 이
  bootstrap 방향을 결정하기 전 mandatory CLI/local E2E를 구현하면 안 된다.
- **Residual risk:** Module 수와 packaging은 proposed이므로 Phase 00/Architecture
  owner 승인 없이 public compatibility로 고정하면 안 된다.

### F-HG-P08-003 — Phase 07의 nested sealed hierarchy를 직접 3-variant switch처럼 지시한다

- **Severity:** `MEDIUM`
- **Finding:** Target §4.5와 §16.1은 정확히
  `Phase07Output.Publishable | Phase07Output.Rejected` 아래
  `FinalResultRejection.VerificationRejected | GateIncomplete` 구조를 보여 준다.
  그러나 §9.8 pseudocode, WP-08.2 action, signature test 이름과 exit checklist는
  `Publishable`, `VerificationRejected`, `GateIncomplete`를 top-level에서 직접
  exhaustive switch하는 것처럼 쓴다.
- **사람 영향:** Java 25 sealed switch를 그대로 작성하면 upstream
  `Phase07Output`과 compile되지 않거나, `Rejected`를 generic mapping한 뒤 nested
  field coverage를 reflection/catch-all로 우회할 수 있다. 이는
  `GateIncomplete` disposition 합성 방지와 exact publish-call oracle을 약화한다.
- **Target 위치:** 1128~1181줄, 1197줄, 1234~1238줄, 1378/1410줄,
  1608~1615줄, 2163줄, 2379/2395줄.
- **Source section:** Actual Phase 07 §3.3, §7.7, §8.4의
  `Phase07Output`/`FinalResultRejection` Java 계약; Target 자체 §4.5와 §16.1.
- **Root cause:** 의미상 세 경우와 Java top-level sealed variant 수를 같은 표현으로
  축약했다.
- **Required correction:** Pseudocode와 WP를 명시적 nested exhaustive switch로
  바꾼다. 첫 switch는 `Publishable`/`Rejected`, 둘째 switch는
  `VerificationRejected`/`GateIncomplete`여야 한다. Test 이름도
  `topLevelOutputAndNestedRejectionAreExhaustive()`처럼 hierarchy를 검증하고,
  unknown top-level/nested subtype 또는 field omission이 compile/test에서
  fail-closed함을 판정한다.
- **Target 수정:** `YES`
- **구현 blocker 구분:** `IMPLEMENTATION CORRECTNESS BLOCKER`. Upstream API가 아직
  unaccepted라는 기존 blocker와 별개로, accepted API가 이 shape라면 mapping 전에
  교정해야 한다.
- **Residual risk:** Upstream 이름은 proposed이므로 최종 accepted type에 맞춰
  명칭은 바뀔 수 있지만 2단계 exhaustiveness 의미는 보존해야 한다.

### F-HG-P08-004 — Reusable contract test와 selected-test command의 Maven dependency path가 닫히지 않았다

- **Severity:** `MEDIUM`
- **Finding:** Target은 `build/port-contract-tests/src/test/java`의 abstract contract를
  filesystem adapter의 concrete contract test에서 재사용한다고 지시하지만,
  그 test classes를 consumer에 제공할 `test-jar`/classifier와 test-scope dependency
  DAG를 제시하지 않는다. 또한 모든 selected command는 owner child POM을 `-f`로
  단독 실행한다. `rpdptw/application` 같은 child POM은 sibling core/solver/verification
  artifact가 exact source state로 local repository에 설치돼 있지 않으면 clean
  checkout에서 resolve되지 않는다. “먼저 full dependency build”는 `verify`인지
  `install`인지와 isolated local repository를 정의하지 않아 이 gap을 닫지 않는다.
- **사람 영향:** Abstract contract는 compile되지 않거나 복사되어 oracle drift가
  생기고, selected test는 test discovery 이전 dependency resolution에서 실패하거나
  개발자 local repository의 오래된 sibling artifact를 사용한다. 이 경우
  false-green 방지 규칙이 있어도 evidence가 같은 source state를 증명하지 못한다.
- **Target 위치:** 689~706줄, compile DAG 790~819줄, false-green 1391~1402줄,
  WP command 1474~1486/1556~1568/1637~1644/1732~1744/1824~1841/
  1911~1923/1994~2005줄, command matrix 2157~2173줄.
- **Source section:** Current Architecture §18.4/§19.1의 abstract compatibility
  suite와 `-pl ... -am verify`; live `build/test-fixtures/pom.xml`의
  `maven-jar-plugin:test-jar`; live architecture rules의 consumer
  `type=test-jar`, `classifier=tests` 검증.
- **Root cause:** Canonical Phase 08 review F-P08-009의 “표준 source set” 교정만
  전달하고 cross-module test artifact publication/consumption과 clean-checkout
  dependency resolution을 끝까지 설계하지 않았다.
- **Required correction:** `port-contract-tests`가 executable-only인지 reusable
  Test JAR인지 결정한다. Reusable이면 live fixture convention과 같이
  `test-jar`/classifier/test scope, reactor order와 no-production-leak architecture
  test를 명시한다. Selected evidence에는 isolated local repository에서 exact
  source sibling을 먼저 `install -DskipTests`한 뒤 owner POM을 실행하거나, root
  reactor `-pl/-am` 실행 + owner report exact-manifest validator로 zero-test를
  fail-closed하는 완전한 명령을 제시한다. WP-08.4에서 distribution에 지정한
  `LocalInputLimitTest`/`LocalWorkspaceSecurityTest`가 adapter test와 별도
  integration class인지도 exact path로 구분한다.
- **Target 수정:** `YES`
- **구현 blocker 구분:** `EVIDENCE/REPRODUCIBILITY BLOCKER`. Production port 설계
  자체보다 tests/evidence acceptance를 막는다.
- **Residual risk:** Test JAR은 production compile graph에 유입되면 안 되므로
  bytecode/dependency report가 필요하다.

### F-HG-P08-005 — Live inventory fingerprint와 Phase 00 상태가 review 시점에 stale하다

- **Severity:** `MEDIUM`
- **Finding:** Target은 progress live Git object를 `3e9d...`, root POM live
  object를 `8aeca...`로 기록하고 Phase 00을 `IN_PROGRESS`,
  evidence `NOT_PRODUCED`라고 설명한다. Final live revalidation 값은 progress
  `36afdfd...`, root POM `1dc675b...`, Phase 00
  `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`다. 첫 implementation evidence bundle은
  independent review에서 reject되어 fix 뒤 재생성 대기이며 acceptance receipt는
  없다.
- **사람 영향:** Phase 08 entry가 여전히 닫혀 있다는 최종 결론은 같지만, 구현자는
  이미 생성된 Phase 00 evidence/review task를 부재로 오인하고 entry receipt의
  현재 owner/last-safe/restart 단계를 잘못 기록한다. “최종 재검사” hash도 재현되지
  않는다.
- **Target 위치:** metadata 21~22/41줄, live source note 247줄, live POM
  431~454줄, current command 설명 2122~2139줄.
- **Source section:** live `docs/implementation/execution-progress-and-results.md`
  metadata와 §5 Phase 00 row, §10.1 actual implementation registry; live root
  `pom.xml`과 source/test/evidence inventory.
- **Root cause:** Concurrent Phase 00 reviewer/scheduler가 target 작성 뒤 상태와
  bytes를 전진시켰고 final inventory receipt를 다시 봉인하지 않았다.
- **Required correction:** Target의 observed-at timestamp와 live Git objects,
  Phase 00 status/evidence/review/acceptance를 재스냅샷한다. HEAD baseline과 live
  drift는 계속 분리하고, Phase 00 `NOT_ACCEPTED`이므로 Phase 08은 여전히
  blocked라는 판정은 유지한다. 빠르게 변하는 live hash는 acceptance criterion이
  아니라 provenance라고 명시한다.
- **Target 수정:** `YES`
- **구현 blocker 구분:** `DOCUMENTATION INVENTORY BLOCKER`; Phase 08 구현의 본질적
  blocker는 아니며 Phase 00 acceptance 부재라는 기존 gate는 계속 유효하다.
- **Residual risk:** Shared checkout이 계속 변하므로 correction 직후에도 scheduler
  receipt와 revalidation timestamp를 함께 기록해야 한다.

## 6. No-finding 근거

다음 영역은 finding을 발행하지 않았다.

### 6.1 원문 불변조건과 Phase 경계

- Target §2.3/§6.2/§14~16은 Phase 08을 application/local reference로 제한하고
  object-common/S3, durable coordinator, AWS deployment와 production cutover를
  선구현하지 않는다.
- Candidate PASS와 result PASS를 모두 요구하고 rejection/incomplete에는 정상
  route/outcome/payload를 두지 않는다.
- Phase 13은 Phase 14A receipt와 C-17 승인 뒤 optional이며 Phase 08 success로
  자동 열리지 않는다고 반복 명시한다.
- `Q-BENCH-02`, `Q-VAR-01`, official/production 값을 hidden default로 채우지 않는다.

### 6.2 신규 독자, scope와 사람 승인

- CVRPTW와 RPDPTW request/pair 차이, stable partition, identity와 artifact/state/
  observation lifecycle을 예와 표로 설명한다.
- `actual`, `placeholder`, `future target`, `OPEN/blocker`를 분리하고 proposed Java가
  현재 API가 아니라고 명시한다.
- WP마다 precondition, action, 근거, 금지 shortcut, expected/failure,
  rollback/last-safe/handoff/checkpoint가 있다.
- Security/Storage/Coordinator/Platform/Product/Algorithm/Operations/reviewer/scheduler
  승인을 분리하며 implementer self-approval을 금지한다.

### 6.3 Failure, security, observability와 reproducibility

- Missing/denied/corrupt/stale/indeterminate를 generic failure로 축소하지 않는
  operation별 carrier blocker를 보존한다.
- Tenant claim과 authorization proof를 분리하고 backend call 전 fail-closed,
  traversal/symlink/input limit/redaction test를 요구한다.
- Cancellation intent/observation/actual termination, algorithm budget/watchdog/
  platform timeout과 last-safe point를 분리한다.
- Clock/path/process/event order를 semantic fingerprint에서 제외하고 fresh/same
  workspace rerun을 요구한다.

### 6.4 Test 전략과 evidence

- Fixture/builder, independent oracle, corruption/fault/race/security/E2E를
  production helper와 분리한다.
- Red→green 순서, selected test zero-test 차단, exact discovered report 비교,
  missing/failure/error/skipped/aborted 0을 명시한다.
- Stub Phase 07과 real E2E, in-memory와 filesystem evidence를 혼동하지 않는다.
- `E-P08-PORT`, `E-P08-LOCAL-E2E`, `E-P08-IDEMPOTENCY`와
  pre-review manifest → independent review → acceptance receipt의 단방향 DAG가
  명확하다.

## 7. Static inspection 결과

| 검사 | 명령/방법 | 결과 |
|---|---|---|
| Target line/hash | `wc -l`, `shasum -a 256`, `git hash-object` | `2599`, SHA-256 `2fcb57...`, Git object `2c7dd1...` |
| Source HEAD/live | `git rev-parse HEAD:<path>`, `git hash-object <path>` | canonical source는 HEAD=live; progress만 HEAD `250aa...` / live `36af...` |
| Inventory | `git ls-tree`, `find`, `rg --files`, POM/source inspection | Phase 08 concrete source/test/evidence 0; Phase 00 fix 01 진행 중 |
| Relative file links | 24개 Markdown link의 resolved path 검사 | missing file 0 |
| GFM fragments | 4개 fragment를 target heading slug와 대조 | broken fragment 0 |
| Fence/heading | code fence parity와 fence 밖 heading scan | fence closed; headings 170 |
| Target whitespace | trailing blank/tab scan | 위반 0 |
| Phase gate scan | `Q-BENCH-02`, C-17, Phase 13/14A/14B, ACCEPTED/production 문맥 검사 | hidden default/gate bypass 0 |
| Maven 실행 | 실행하지 않음 | Phase 08 type/test 부재와 closed entry를 실제 PASS로 오인하지 않음 |

## 8. Verdict와 correction gate

Target은 구현 blocker를 숨기지 않고 교육·안전·evidence 구조도 강하다. 하지만
current canonical source receipt, executable CLI composition, exact Phase 07 sealed
shape와 reproducible Maven test path는 사람이 구현을 시작하기 전에 교정해야 한다.
Correction 후 독립 follow-up은 다섯 finding을 닫고 target hash/line/link/GFM 및
live inventory를 다시 확인해야 한다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=2 MEDIUM=3 LOW=0
REQUIRED_CORRECTION_FINDINGS: F-HG-P08-001, F-HG-P08-002, F-HG-P08-003, F-HG-P08-004, F-HG-P08-005

## Correction 01 읽기 전용 재검증

```yaml
recheck_round: "01"
rechecked_at: 2026-07-29T02:44:18+09:00
recheck_type: ORIGINAL_REVIEWER_READ_ONLY_FOLLOW_UP
head_commit_observed: 7cc890ee1d0805df5ae14b633127fade4f978639
target_lines_rechecked: 2833
target_sha256_rechecked: ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326
target_git_object_rechecked: b3572fee2c687534cc41a581cb18f6f45e5328c6
correction_report_lines_rechecked: 236
correction_report_sha256_rechecked: 14646cfc9b97215af37ee4afb76a259018dfc8a751835445b4b002ea7fd6aa15
correction_report_git_object_rechecked: 986bda9c8986da43ab7af365a8a6232f28bb65db
review_prefix_lines_before_append: 404
review_prefix_sha256_before_append: 761a69a40ced5b2d7cff5d785b80db21da3e13a433b39ec44278ad78aa4d8a84
target_or_correction_report_modified_by_reviewer: false
code_pom_test_readme_progress_other_review_modified_by_reviewer: false
git_stage_commit_push_worktree_status: NOT_PERFORMED
recheck_verdict: FURTHER_CORRECTION_REQUIRED
resolved_findings:
  - F-HG-P08-002
  - F-HG-P08-003
  - F-HG-P08-004
  - F-HG-P08-005
open_findings:
  - F-HG-P08-001
new_findings: []
```

### 재검증 범위와 방법

이 절은 새 broad review가 아니라 원 finding 다섯 건의 root cause와 required
correction만 다시 판정한다. Correction report의 `COMPLETE` 자기주장은 판정 근거로
사용하지 않고, 고정한 target bytes에서 각 anchor를 직접 읽어 current canonical,
Actual Phase 07/09, live Maven convention과 inventory에 대조했다.

1. Target, correction report와 원 review prefix의 line count, SHA-256과 Git object를
   먼저 고정했다.
2. F-HG-P08-001~005의 원 root cause/required correction을 target의 교정된
   source receipt, module/DAG, sealed dispatch, Test JAR/명령, timestamped inventory
   anchor에 각각 대조했다.
3. Current Architecture의 실제 heading과 날짜 고정 Architecture의 heading을 별도로
   대조해 절 번호가 어느 문서를 가리키는지 확인했다.
4. Relative link/GFM fragment, fence/heading, whitespace/NUL/EOF와 selected-test
   command manifest를 정적으로 검사했다.
5. Phase 08 concrete type/test/evidence가 없고 Phase 00이 여전히
   `NOT_ACCEPTED`이므로 future Maven 명령을 실행 결과나 PASS evidence로 만들지 않았다.

### 읽은 파일과 hash receipt

직접 재검증 입력:

| 파일 | 줄 | SHA-256 | Git object |
|---|---:|---|---|
| 이 review의 append 전 prefix | 404 | `761a69a40ced5b2d7cff5d785b80db21da3e13a433b39ec44278ad78aa4d8a84` | `51374cb0930a25516e3c752631f317faefee2057` |
| Corrected target | 2,833 | `ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326` | `b3572fee2c687534cc41a581cb18f6f45e5328c6` |
| Correction report 01 | 236 | `14646cfc9b97215af37ee4afb76a259018dfc8a751835445b4b002ea7fd6aa15` | `986bda9c8986da43ab7af365a8a6232f28bb65db` |

Current canonical/path-map 입력:

| 파일 | 줄 | SHA-256 | Git object |
|---|---:|---|---|
| `docs/README.md` | 67 | `5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569` | `13f1b3b2dea038b8e0b466c138f5f59299413125` |
| `docs/master-design.md` | 1,648 | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| `docs/domain-design.md` | 1,607 | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` | `ace117c380466b733994a1fbb2a95d31e41b3959` |
| `docs/architecture-design.md` | 1,469 | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| `docs/architecture-domain-implementation-design.md` | 3,822 | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| `docs/master-design-open-questions.md` | 87 | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |

Historical/implementation/original/adjacent 입력:

| 파일 | 줄 | SHA-256 | Git object |
|---|---:|---|---|
| `docs/2026-07-26-domain-design.md` | 1,886 | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| `docs/2026-07-26-architecture-design.md` | 1,019 | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| Implementation README | 240 | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| Master Realization Plan | 943 | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| Execution Progress | 470 | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` | live `0419f69199b3140dd44020f78278b1352e6517b8` |
| Actual Phase 07 | 1,666 | `1b0d7d92d961c44605307a5a66bd659ac54b4cb0832d275aa10c8bfb855de727` | `1d4ce46f4c2400a5ea0dce3b8b11bc5ba0fed115` |
| Phase 07 review | 314 | `82da52bcd52724c44a5ffd913f3146b7017457ed0a209b85611b80cb8547c78f` | `156a994ef66d9c6f51785d96d2b327fe4539712e` |
| Phase 07 human guide | 2,163 | `9b324867f94e0bfef5e3954ecdc9a57f4978d63072f2e51d15413b4d13a627dd` | `24d30f09b972a58657f07c640c98940011880cf1` |
| Canonical Phase 08 | 2,563 | `15dbcab53fc00fc4d3062c5bdb0eb1f072fd902bfbb59b322859f30b090a5f0f` | `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` |
| Canonical Phase 08 review | 460 | `95d0b005d6218af7c2e5e8d720419c6fd781d0f97f9abddff2fd0132b52264f7` | `7983d3b09e2f4c45b8cd132e3627c053c9e3e8ce` |
| Actual Phase 09 | 1,967 | `ac08d10a63f7d0bd86a74fa61c1d220b0619a9c16c7d50fe0017cfe9afc8bb3b` | `99a5b0df5531a65964272f423cc0ccca4d4f1430` |
| Phase 09 review | 423 | `e49a441d0ccedb9e13b5b5c0f2614bef710dc68a1d587a3c65a5e430bf7a0b5d` | `786a7bd3a396c7010204e3940c5d2f031c07af48` |
| Phase 09 human guide | 2,727 | `09b32c3a821b6ad68ee4aa5a0322d01af07ee08390e3650b80c8de8339b46038` | `f4cbd4b9e85823686ed1c82014dbb84f475a659b` |

Live Maven/inventory 입력:

| 파일 | 줄 | SHA-256 | Git object |
|---|---:|---|---|
| Root `pom.xml` | 237 | `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c` | live `1dc675ba17b7f2202f34a22131f152cc2868b075` |
| `build/test-fixtures/pom.xml` | 49 | `65a9fae30174778ade49302b2e8525419955792437498a7dd5be6649888aad71` | live `e033f9cda6de908a25431b6dfce5b0b0d8cd3ac1` |
| `build/architecture-rules/pom.xml` | 83 | `3f4ff694dc616651c739ef50b3b746ff11c23d1e9f23e3f5680d52eb6d5fbc14` | live `cb14c68cd79c3db9636d0cd08b92016352dd1d3e` |

### Finding별 판정

#### F-HG-P08-001 — OPEN

- **원 severity:** `HIGH`
- **확인한 target anchor:** §3.1~§3.3의 current source table/읽기 순서와
  current-vs-dated impact는 258~329줄에서 교정됐다. 그러나 §17 traceability
  2807, 2810~2813, 2815, 2817~2822, 2825줄의 13개 requirement는 여전히
  `Architecture §3.1`, `§3.6/§5.2`, `§5.5`, `§5.3`, `§5.1~5.2`,
  `§3.2`, `§3.4`, `§3.3~3.6` 같은 날짜 고정 Architecture 절 번호를
  `Current`/`Dated` 표기 없이 사용한다.
- **Source evidence:** Current Architecture에서 flow는 §4.2, identity/idempotency는
  §11.3/§16.3/§17.2, artifact는 §16.2, observability/retry/failure/security는
  §17.1~§17.3, local runtime은 §10, Phase 10 seam은 §11~§12다. 반면 위 target
  번호와 제목은 `docs/2026-07-26-architecture-design.md`의 Runtime §3과
  운영 §5에 정확히 대응한다. Current Architecture에는 `§3.6`, `§5.3`,
  `§5.5` 자체가 없다.
- **남은 root cause:** Source table만 current hierarchy로 바꾸고 requirement-level
  traceability의 historical section mapping을 끝까지 재작성하지 않았다. 따라서 원
  required correction의 “traceability를 current actual relevant section으로 다시
  매핑”이 부분 완료에 그쳤다.
- **Required correction:** §17의 위 13개 행을 실제 Current Architecture heading으로
  재매핑하고 source label을 `Current Architecture`로 명시한다. 날짜 고정 절을
  보조 provenance로 유지하려면 각 행에서 `Dated Architecture
  (HISTORICAL_IMPLEMENTATION_BASELINE_ONLY)`로 명시하고 current authority citation을
  함께 둔다. 그 뒤 source→requirement→WP→test/evidence semantic impact를 다시
  검사한다.
- **Target 수정 필요:** `YES` — 이 recheck에서는 수정하지 않았다.
- **남은 risk:** 신규 구현자가 source table에서는 current 문서를 읽고 실제 requirement
  추적 단계에서는 존재하지 않거나 의미가 다른 current 절을 찾게 되어 authority
  receipt와 review 재현성이 갈라진다. 이는 원 `DOCUMENTATION AUTHORITY BLOCKER`다.

#### F-HG-P08-002 — RESOLVED

- **확인한 target anchor:** §8.1 765~769줄이 current Architecture를 우선하고,
  847~868줄이 `LocalCliMain`, bootstrap, composition을 모두 `apps/cli`에 둔다.
  870~882줄은 `distributions/local`과 별도 filesystem module split을 새 승인 전
  금지하며, 884~940줄과 972~974줄은 `apps/cli → application/adapters/profile`
  compile DAG, same-module main/bootstrap과 launcher oracle을 닫는다.
- **Source evidence:** Current Architecture §6.6 460~474줄은 app이 composition
  root이고 local JSON/filesystem 구현은 `adapters/common` package라고 정하며,
  §7.1 548~594줄은 `apps/* → selected adapter/profile/application` 방향을 허용한다.
- **Root cause closure:** Parser/main과 downstream composition root를 분리해 호출
  방향이 없던 구조를 제거했다. WP-08.4/6/7 및
  `cliOwnsExecutableCompositionRootAndHasNoReverseDependency()`와 launcher smoke가
  같은 선택을 검증한다.
- **남은 risk:** 실제 `apps/cli` POM/JAR/type은 아직 0이며 exact public signature와
  packaging은 Phase 00/Architecture/API owner 승인 뒤의 future evidence다. 이는
  문서 correction 미완료가 아니라 기존 implementation-start gate다.

#### F-HG-P08-003 — RESOLVED

- **확인한 target anchor:** §4.5 442~469줄, §9.8 1269~1333줄,
  `Phase07OutputStubBuilder` 1347줄, signature test 1385~1387줄,
  WP-08.0/1 1628/1710줄, exit 2613/2629줄과 handoff 2684~2688줄이 모두
  top-level `Publishable | Rejected`와 nested
  `VerificationRejected | GateIncomplete`를 분리한다. 두 switch 모두
  default/catch-all/reflection을 금지한다.
- **Source evidence:** Actual Phase 07 948~977줄의 Java contract가 정확히
  `Phase07Output.Publishable | Phase07Output.Rejected`와
  `FinalResultRejection.VerificationRejected | GateIncomplete`다.
- **Root cause closure:** 의미상 세 결과를 Java top-level 세 subtype처럼 축약하던
  pseudocode/WP/test 문구가 명시적 two-level exhaustive dispatch로 바뀌었다.
- **남은 risk:** Actual Phase 07은 아직 unaccepted라 최종 이름은 바뀔 수 있다.
  Accepted artifact에서도 2단계 exhaustiveness와 field/no-payload 의미를 다시
  compile 검증해야 한다.

#### F-HG-P08-004 — RESOLVED

- **확인한 target anchor:** §8.1 784~790줄과 §8.2 942~960줄은
  `build/port-contract-tests`를 attached `tests` classifier Test JAR owner로,
  `adapters/common`을 concrete behavior-test owner로 고정하고
  `type=test-jar/classifier=tests/scope=test`와 production leakage 0을 요구한다.
  §10.5 1548~1569줄은 stale/zero/skip/duplicate와 Failsafe 미구성 false-green을
  차단한다. §12.1 2361~2384줄은 explicit empty isolated repository에서
  `-pl owner -am clean install -DskipTests` 뒤 같은 repository의 owner
  `-f ... clean test`를 수행하고 Test JAR/dependency tree/Surefire XML/expected
  class-method manifest를 봉인한다. 2402~2407줄은 이 protocol을 모든 selected
  command의 공통 선행조건으로 만든다.
- **Source evidence:** Live `build/test-fixtures/pom.xml` 34~45줄은
  `maven-jar-plugin:test-jar`를 attach하고, live
  `build/architecture-rules/pom.xml` 51~58줄은
  `type=test-jar/classifier=tests/scope=test`로 소비한다. Current Architecture
  §18.4/§19.1은 reusable compatibility suite와 reactor dependency closure를
  요구한다.
- **Manifest/discovery evidence:** Target의 `-Dtest=` selected command 15개를
  정적 파싱한 결과 15개 모두 owner `-f`, 같은 `PHASE08_M2`,
  `-Dsurefire.failIfNoSpecifiedTests=true`, fresh `clean test|verify`와 본문에
  선언된 exact class name을 가졌다. Adapter unit test와 CLI integration test도
  `LocalInputLimitTest`/`LocalWorkspaceSecurityTest` 대
  `LocalCliInputLimitIntegrationTest`/`LocalCliWorkspaceSecurityIntegrationTest`로
  exact path가 분리됐다.
- **Root cause closure:** Reusable abstract source의 publication/consumption과
  clean-checkout sibling resolution이 모두 명시적 Maven dependency path를 가진다.
- **남은 risk:** Proposed Test JAR/module/test는 아직 존재하지 않는다. 실제 evidence는
  accepted Phase 00 reactor에서 attached artifact와 production graph leakage 0,
  fresh report manifest를 다시 증명해야 한다.

#### F-HG-P08-005 — RESOLVED

- **확인한 target anchor:** Metadata 21줄, §3.2 290~298줄, §5.1~§5.3
  492~572줄과 §12.1 2320~2340줄이 HEAD baseline, original authoring
  observation, correction timestamp snapshot과 accepted evidence를 분리한다.
  Progress/root POM snapshot objects
  `0419f69199b3140dd44020f78278b1352e6517b8` /
  `1dc675ba17b7f2202f34a22131f152cc2868b075`는 recheck live bytes와 일치한다.
- **Source evidence:** Execution Progress 377~400줄은
  `REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS`,
  independent review 02 `COMPLETED_CHANGES_REQUIRED`,
  evidence `REJECTED_PENDING_FIX_02_REGENERATION`, acceptance receipt
  `NOT_PRODUCED`다. Scoped reactor POM은 13개, `rpdptw` main Java는
  package-info 23/concrete 0, application은 package-info 4/concrete 0,
  `rpdptw` test와 Phase 08 named test/evidence는 0이고
  `distributions`, `build/port-contract-tests`는 없다.
- **Root cause closure:** Stale authoring state를 final authority처럼 쓰지 않고
  correction 시각의 receipt를 provenance로 고정했으며, rejected Phase 00 bundle을
  accepted entry로 승격하지 않았다.
- **남은 risk:** Recheck 시점의 `build/**/src/test/java`는 12개,
  `@Test` 28개로 correction snapshot 표의 11개/25개 이후 다시 drift했다.
  이는 target이 명시한 mutable live observation 위험이며 Phase 08 acceptance는
  여전히 0이다. 구현 시작 시 source/test/evidence manifest와 scheduler acceptance
  receipt를 새 시각/hash로 다시 봉인해야 한다.

Correction이 만든 별도 `NEW` finding은 발견하지 않았다. F-HG-P08-001의 미교정
traceability는 원 finding 범위이며 새 finding으로 중복 발행하지 않는다.

### 정적 재검증

| 검사 | 방법 | 결과 |
|---|---|---|
| Target 고정 | `wc -l`, `shasum -a 256`, `git hash-object` | `2,833`; SHA-256/Git object가 이 절 metadata와 일치 |
| Relative link/GFM fragment | Target Markdown local link 28개를 상대 path와 GitHub heading slug에 대조 | Missing 0, broken fragment 0 |
| Fence/heading | Fence 밖 heading transition과 backtick fence parity | Fence marker 142, 모두 닫힘; heading 170, level jump 0 |
| Whitespace/NUL/EOF | Trailing space/tab, NUL, final LF 검사 | Target 위반 0 |
| Selected-test manifest | 15개 `-Dtest=` block의 owner/M2/fail-if-none/clean phase/class 선언 정적 파싱 | Invalid 0 |
| Maven discovery/stale evidence | Surefire 3.5.4, Failsafe 미구성, Test JAR owner/consumer POM과 §10.5/§12.1 대조 | Zero/skip/stale 차단 문구와 paired dependency path 확인 |
| Maven/Java 실행 | 실행하지 않음 | Phase 08 target type/test 부재와 Phase 00 `NOT_ACCEPTED`; future command를 actual PASS로 오인하지 않음 |
| Scope | Target/correction/code/POM/test/README/progress/다른 review read-only, 이 review 끝에만 append | 허용 범위 외 수정 0 |

F-HG-P08-002~005는 원 required correction 기준으로 닫혔다. 그러나
F-HG-P08-001의 requirement-level current canonical mapping이 남아 있으므로
Correction 01 전체를 accept할 수 없다. Target 작성자는 위 13개 traceability 행만
current Architecture의 실제 절로 재매핑한 뒤 다음 read-only recheck를 요청해야 한다.

RECHECK_ROUND: 01
RECHECK_VERDICT: FURTHER_CORRECTION_REQUIRED
RESOLVED_FINDINGS: F-HG-P08-002, F-HG-P08-003, F-HG-P08-004, F-HG-P08-005
OPEN_FINDINGS: F-HG-P08-001
TARGET_HASH_RECHECKED: ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326
CORRECTION_REPORT_HASH_RECHECKED: 14646cfc9b97215af37ee4afb76a259018dfc8a751835445b4b002ea7fd6aa15

## Correction 02 읽기 전용 재검증

```yaml
recheck_round: "02"
rechecked_at: 2026-07-29T02:56:14+09:00
recheck_type: ORIGINAL_REVIEWER_READ_ONLY_FOLLOW_UP
head_commit_observed: 7cc890ee1d0805df5ae14b633127fade4f978639
review_prefix_lines_before_append: 650
review_prefix_sha256_before_append: 4c56750d8157995a8abfd104dbfd3b39e1c9dae5bd30d10076036ed4b6a9a79b
target_lines_rechecked: 2833
target_sha256_rechecked: 624efc9f8619dc3e239c65967ee114750712a9770e90969859f431cafe64d982
target_git_object_rechecked: 6be82ebb56af3cc637ab127802ad3d9f345f11f7
correction_02_report_lines_rechecked: 140
correction_02_report_sha256_rechecked: 8d9d354659f3e8fc9a2d67279a8f2fc545cc102c9e456592b3b52b11281df15a
correction_02_report_git_object_rechecked: bb4f319f3c99805c2e4fc6ff6532bee0e262f9ed
target_or_correction_report_modified_by_reviewer: false
other_files_modified_by_reviewer: false
git_stage_commit_push_worktree_status: NOT_PERFORMED
recheck_verdict: ACCEPTED
resolved_findings:
  - F-HG-P08-001
open_findings: []
new_findings: []
```

### 입력 고정과 방법

Correction 02의 `COMPLETE` 자기판정은 근거로 사용하지 않았다. Round 01 target
before Git object를 직접 복원해 corrected target과 line/field 단위로 비교하고,
13개 source cell 각각을 Current Architecture의 실제 heading과 본문 의미에
대조했다.

| 입력 | 줄 | SHA-256 | Git object/역할 |
|---|---:|---|---|
| 이 review의 Round 02 append 전 prefix | 650 | `4c56750d8157995a8abfd104dbfd3b39e1c9dae5bd30d10076036ed4b6a9a79b` | `3429f5591bd54a7cec6abfddcff0707ccd42ee8b` |
| Target before Correction 02 | 2,833 | `ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326` | `b3572fee2c687534cc41a581cb18f6f45e5328c6` |
| Corrected target | 2,833 | `624efc9f8619dc3e239c65967ee114750712a9770e90969859f431cafe64d982` | `6be82ebb56af3cc637ab127802ad3d9f345f11f7` |
| Correction 02 report | 140 | `8d9d354659f3e8fc9a2d67279a8f2fc545cc102c9e456592b3b52b11281df15a` | `bb4f319f3c99805c2e4fc6ff6532bee0e262f9ed` |
| Current Architecture | 1,469 | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | `81495ff448d0e618ab3563e8ff80614fb1028acf`; current authority |
| Dated Architecture | 1,019 | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` | `d51339e251dee1e032e711144dc63d6d07d7323b`; historical-only |

검증은 다음 순서로 수행했다.

1. Before/after target 전체를 비교해 변경 line과 table column을 고정했다.
2. 13개 requirement의 Current Architecture 절 번호와 heading 존재를 확인했다.
3. 해당 절 본문이 requirement의 의미를 직접 지지하는지, 다른 보존 source가
   세부 의미를 계속 닫는지 확인했다.
4. Requirement text, WP, exact test/evidence와 gate가 누락·변경되지 않았는지
   byte 단위로 비교했다.
5. Target/report link, GFM fragment, fence/heading, whitespace/NUL/EOF와 scoped
   diff check를 실행했다.

### F-HG-P08-001 — RESOLVED

#### 13개 Current Architecture semantic mapping

| Requirement / target anchor | 교정된 Current Architecture source | 직접 확인한 semantic evidence |
|---|---|---|
| `REQ-P08-FLOW` / 2807 | §4.2 `Main flow` | Canonical input부터 candidate/result verifier, publication CAS와 retrieval까지 한 흐름이다. |
| `REQ-P08-IDENTITY` / 2810 | §11.3 `Logical identity와 idempotency`, §16.3 `Provenance`, §17.2 `Retry/failure matrix` | Submission/Solve/WorkerRun/Attempt identity, lineage와 retry identity를 함께 고정한다. |
| `REQ-P08-AUTHZ` / 2811 | §17.3 `Security and data boundary` | Tenant access scope, 최소 권한과 profile/preset 권한 검증을 둔다. Non-ambient 세부 계약은 보존된 Integrated §20/review source가 계속 닫는다. |
| `REQ-P08-FAILURE` / 2812 | §12.1 `Port catalog`, §17.2 `Retry/failure matrix` | Artifact/state port와 operation failure의 retry owner/identity/result 가능성을 구분한다. Lossless carrier 세부는 Integrated §21/review source가 보존됐다. |
| `REQ-P08-ARTIFACT` / 2813 | §12.1 `Port catalog`, §16.2 `Artifact contract` | Immutable put/get/verify, digest-before-deserialize, create-once와 publication-index CAS를 직접 규정한다. |
| `REQ-P08-LOCAL` / 2815 | §10.1~§10.3 `Local/single-run runtime` | Same use case/verifier를 쓰는 reference runtime, explicit workspace, same-process dispatch와 local publication을 규정한다. |
| `REQ-P08-REPRO` / 2817 | §10.1~§10.3 `Local/single-run runtime`, §17.1 `Structured observability` | Deterministic rerun을 기준으로 하고 thread count, elapsed와 completion order를 semantic input에서 제외한다. |
| `REQ-P08-CANCEL` / 2818 | §12.1 `Port catalog`, §17.2 `Retry/failure matrix` | Cancellation intent의 기록·조회·전파와 idempotent intent를 정상 완료와 분리한다. Last-safe 의미는 보존된 Master §13.1이 계속 지지한다. |
| `REQ-P08-DEADLINE` / 2819 | §10.3 `Single-run과 local multi-worker`, §11.1 `Logical state machine`, §17.2 `Retry/failure matrix` | Local timeout을 정상 step 종료로 바꾸지 않고 watchdog/resource/platform timeout을 exceptional state/failure로 분리한다. |
| `REQ-P08-RETRY` / 2820 | §11.3 `Logical identity와 idempotency`, §17.2 `Retry/failure matrix` | 같은 logical worker에서 attempt만 바뀌는 규칙과 failure별 retry identity를 고정한다. |
| `REQ-P08-SECURITY` / 2821 | §17.3 `Security and data boundary` | Tenant scope, 최소 권한, secret/raw address/PII redaction을 규정한다. Path/symlink/limit 세부는 보존된 Integrated §20이 계속 지지한다. |
| `REQ-P08-OBS` / 2822 | §17.1 `Structured observability` | Correlation/telemetry field를 정의하고 elapsed/completion order가 quality·seed·replay fingerprint 입력이 아님을 명시한다. |
| `REQ-P08-P10` / 2825 | §11.1~§11.3 `Distributed multi-round runtime`, §12.1~§12.2 `Provider-neutral logical ports` | Logical state/flow/identity와 provider-neutral dispatch/cancel/state seam을 제공하며 coordinator 구현 ownership은 Plan Phase 10에 보존한다. |

13개 모두 Current Architecture에 실제로 존재하는 절/heading이며 requirement 의미와
관련된다. 한 Current section만으로 모든 세부를 과장하지 않고 Master, Integrated,
review, Plan source가 갖는 더 강한 last-safe, access, lossless failure, filesystem
security와 Phase 10 ownership 의미도 그대로 남겼다.

#### Dated provenance와 authority

- Metadata 45~46줄은 날짜 고정 Domain/Architecture를
  `HISTORICAL_IMPLEMENTATION_BASELINE_ONLY`로 표시한다.
- §3.1 226~247줄은 current path/canonical source 뒤 마지막 historical
  cross-check로만 날짜 고정 문서를 읽고 current authority가 아니라고 명시한다.
- §3.2 source table 267줄도 Dated Architecture를 historical drift cross-check로만
  둔다.
- 교정된 13개 source cell에는 unqualified `Architecture §...` 또는 Dated
  Architecture를 current requirement authority로 쓰는 행이 0개다.

따라서 dated provenance는 삭제되어 출처가 사라진 것이 아니라, current semantic
authority와 분리된 보조 provenance로 명시적으로 남았다.

#### Requirement/WP/test/evidence/gate 보존

Before Git object와 corrected target의 전체 diff는 정확히 13줄
`2807, 2810~2813, 2815, 2817~2822, 2825`뿐이다.

| 비교 항목 | 결과 | 근거 |
|---|---|---|
| Requirement ID/text | `13/13 UNCHANGED` | 각 행 첫 번째 cell byte 동일 |
| Source | `13/13 CORRECTED` | 각 행 두 번째 cell만 old/unqualified Architecture에서 Current Architecture로 변경 |
| WP | `13/13 UNCHANGED` | Decision/08.x 범위와 순서 byte 동일 |
| Exact test/evidence | `13/13 UNCHANGED` | Test method/suite/evidence/receipt cell byte 동일 |
| 다른 requirement/gate | `UNCHANGED` | Target의 그 밖의 line 변경 0; `Q-BENCH-02 OPEN`, `C-17 GATED`, `Q-VAR-01 DEFERRED`, Phase 09/10 및 scheduler acceptance gate 보존 |

Source mapping만 바뀌었으므로 새 field, API, state, failure, provider, default,
WP, test 또는 acceptance authority가 생기지 않았고 기존 mapping의 누락도 없다.
F-HG-P08-001의 원 root cause와 required correction은 닫혔다. 남은 것은 실제
구현 시작 전 source receipt/owner 승인을 다시 확인해야 한다는 기존 residual
process risk이며 추가 target correction 사유가 아니다.

### 정적 재검증

| 검사 | 결과 |
|---|---|
| Target hash/line | 2,833줄; SHA-256 `624efc9f...4d982`, Git object `6be82ebb...11f7` 일치 |
| Correction 02 hash/line | 140줄; SHA-256 `8d9d3546...df15a`, Git object `bb4f319f...f9ed` 일치 |
| Before→after semantic diff | Changed line 13, changed column은 Source 13/13, 다른 cell/line 변화 0 |
| Target links/GFM | Local link 28, missing 0, broken fragment 0 |
| Correction report links/GFM | Local link 4, missing 0, broken fragment 0 |
| Fence/heading | Target fence 142/heading 170, report fence 2/heading 6; unclosed fence와 level jump 0 |
| Whitespace/NUL/EOF | Target/report trailing whitespace, NUL 0; final LF 존재 |
| Scoped `git diff --check` | Target와 Correction 02 모두 whitespace diagnostic 0 |
| Maven/Java/test 실행 | 실행하지 않음; 이번 correction은 source traceability-only이며 구현/PASS evidence를 만들지 않음 |
| Scope | Target/report 및 비소유 파일 수정 0; 이 review 끝에 Round 02 절만 append |

Correction 01에서 이미 닫힌 F-HG-P08-002~005와 이번에 닫힌 F-HG-P08-001을
합쳐 원 review의 required correction finding은 모두 해결됐다. Correction 02가 만든
별도 regression이나 `NEW` finding은 발견하지 않았다.

RECHECK_ROUND: 02
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: F-HG-P08-001
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: 624efc9f8619dc3e239c65967ee114750712a9770e90969859f431cafe64d982
CORRECTION_REPORT_HASH_RECHECKED: 8d9d354659f3e8fc9a2d67279a8f2fc545cc102c9e456592b3b52b11281df15a
