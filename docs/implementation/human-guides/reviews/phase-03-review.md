# Phase 03 사람용 구현 가이드 독립 리뷰

```yaml
phase: "03"
review_type: INDEPENDENT_HUMAN_IMPLEMENTATION_GUIDE_REVIEW
reviewer_task_nature: author와 분리된 read-only 문서 검토
target: docs/implementation/human-guides/phases/phase-03-human-implementation-guide.md
target_git_hash_object: 6c5d7f1fa3cd3c574194396479dc20ececd131fd
target_sha256: 1611717ddd519cc581fe57cffc384ce6e43f7bd2185904c2aeb1fc68e26222f2
target_lines: 1469
target_tracked_at_head: false
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
inventory_snapshot_at: 2026-07-29T01:31:23+09:00
inventory_status_sha256: 942b08f3a3554dda43e09d0bdb60ac9599d4322b30dfc101ba86f7772d12524b
review_date: 2026-07-29
timezone: Asia/Seoul
verdict: CHANGES_REQUIRED
target_changes_required: true
finding_counts:
  critical: 0
  high: 3
  medium: 1
  low: 1
```

## 1. 결론

Target은 Phase 03의 물리 전파 의미, `Invalid`/`Infeasible` 분리, OPEN/GATED/deferred 보존, Phase 13/14A/14B gate, evidence DAG와 사람 checkpoint를 전반적으로 충실하게 설명한다. 완성 코드를 복사하게 하지 않으면서도 hand fixture, work package, rollback과 handoff를 제공한 점도 적절하다.

그러나 실제 구현자가 그대로 실행·판정하기에는 다음 5건의 교정이 필요하다.

1. HEAD snapshot과 현재 미커밋 reactor drift가 “현재 checkout”이라는 한 표에 섞여 현재 명령·링크·inventory가 틀리다.
2. `propagation`과 `evaluation.api` 사이 package dependency/ownership을 상위 Domain/Architecture와 반대 방향으로 사실상 고정하면서 cross-source conflict와 승인 gate를 드러내지 않는다.
3. Canonical Phase 03의 exact required test 42개 중 5개와 exact boundary row가 빠져 Target 기준만으로 false-green이 가능하다.
4. Proposed Java tree와 skeletal signature가 참조 type의 owner/package를 닫지 않아 compile 가능한 설계 검토 입력이 아니다.
5. 네 곳의 내부 절 번호가 실제 §11.2/§11.5 대신 존재하지 않는 §10.2/§10.5를 가리킨다.

Target 수정이 필요한 finding이 있으므로 최종 verdict는 `CHANGES_REQUIRED`다. 이 verdict는 Phase 03 구현 실패 판정이 아니라 사람용 가이드의 교정 요구다. Target은 본 review에서 수정하지 않았다.

## 2. 검토 source와 fingerprint

Target metadata의 Git blob 값은 `7cc890ee1d0805df5ae14b633127fade4f978639` HEAD에서 모두 재현되었다. 다만 live progress와 POM은 미커밋 변경이 있으므로 HEAD blob과 working-tree blob을 분리했다.

| 입력 | 직접 대조한 section | Fingerprint |
|---|---|---|
| `docs/master-design.md` | §1, §4~§9, §12~§17 | HEAD blob `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| `docs/2026-07-26-domain-design.md` | §1~§3, §7~§10, §17~§18 | HEAD blob `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| `docs/2026-07-26-architecture-design.md` | §1~§2, §5.6, §6 | HEAD blob `d51339e251dee1e032e711144dc63d6d07d7323b` |
| `docs/architecture-domain-implementation-design.md` | §1~§3, Phase 02~04, §19~§25 | HEAD blob `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| `docs/master-design-open-questions.md` | §1~§4와 exact `Q-*` rows | HEAD blob `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| `docs/implementation/master-realization-plan.md` | §1~§4, Phase 02~04, Phase 13~14, §8~§15 | HEAD blob `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| `docs/implementation/README.md` | §0~§7 | HEAD blob `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| `docs/implementation/execution-progress-and-results.md` | §1~§2, §5~§10 | HEAD blob `250aa90ae568a6b32ec905fa5ee456d430ff72cf`; live blob `3e9dcd16621e459e4572a094cb71ab62a3978360` |
| `docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md` | §1~§15 | HEAD blob `74098f7e15cf75bcc443ae009cc475a9b60d63a3` |
| `docs/implementation/reviews/phase-03-review.md` | §1~§7와 ALNS-first addendum | HEAD blob `e473887ffbbb165f18ea6aaa76d6fc3917fc4af2` |
| Target | 전체 1,469줄 | Git hash-object `6c5d7f1fa3cd3c574194396479dc20ececd131fd`; SHA-256 `1611717ddd519cc581fe57cffc384ce6e43f7bd2185904c2aeb1fc68e26222f2` |
| 인접 사람용 Phase 02 | metadata, producer artifact, Phase 03 handoff | SHA-256 `93c53ea2055bea89df4cb70dddc95b66b035f01bc392764d59d1ec86dea87f3a` |
| 인접 사람용 Phase 04 | metadata, Phase 03 input, WP-04.5, Phase 05/07 handoff | SHA-256 `158e9cc7339a8bebb68375f0ad33c792057efe8cc6a4533b2b98025d0bfb6010` |

`docs/2026-07-26-master-design.md`와 `docs/codex/`는 누락·퇴행 cross-check로만 취급했다. 현재 의미나 finding의 authority로 사용하지 않았다.

## 3. Inventory snapshot

### 3.1 HEAD baseline

HEAD `7cc890ee1d0805df5ae14b633127fade4f978639`에는 다음이 추적돼 있다.

- Root `pom.xml` 하나, HEAD blob `f8a411eadd4a5c01d8dd09fdea462738ca63d65f`
- Root `src/main/java` 6개, `src/test/java` 1개
- Maven wrapper와 target reactor module 없음
- Phase 03 production type/test/evidence 없음

이 baseline은 Target §3.2의 source fingerprint와 일치한다.

### 3.2 미커밋 live drift

2026-07-29T01:31:23+09:00의 working tree에는 HEAD와 별도로 다음이 관찰됐다.

- Root `pom.xml`은 `packaging=pom`인 `ro-next-parent` reactor로 수정됐고 live blob은 `1dc675ba17b7f2202f34a22131f152cc2868b075`다.
- `mvnw`, `.mvn/`, `rpdptw/`, `build/`, `legacy/`가 untracked로 존재한다.
- 총 13개 live `pom.xml`과 `rpdptw/core`, `build/test-fixtures`, `build/architecture-rules` 경로가 존재한다.
- Target namespace에는 production 기능이 아니라 23개의 `package-info.java` skeleton이 있고 Phase 03 `RoutePlan`, propagator, evaluation type과 Phase 03 test는 0개다.
- Architecture/test-fixtures에는 Phase 00용 test source 9개가 있다.
- Legacy code는 `legacy/gcp-placeholder`로 이동·확장 중이고 원래 root `src/**` 7개는 working tree에서 삭제 상태다.
- Live progress는 Phase 00 prerequisite remediation을 `IN_PROGRESS`, evidence/receipt를 `NOT_PRODUCED`로 기록한다.

따라서 live reactor의 존재는 Phase 00 또는 Phase 03 acceptance가 아니다. 반대로 “wrapper/module이 현재 없다”는 설명도 더 이상 live 사실이 아니다.

## 4. 검토 방법과 기준

1. Target 1,469줄을 줄 번호와 함께 전수 읽었다.
2. Target이 선언한 source blob을 HEAD에서 재계산하고, 바뀐 progress/POM은 working-tree blob을 별도로 계산했다.
3. Canonical Master → question register → Final Domain → Final Architecture → Integrated design → implementation plan/Phase/review 순서로 의미와 배치를 대조했다.
4. Phase 02/04 canonical 문서와 인접 사람용 가이드에서 producer/consumer identity, lifecycle, failure와 handoff를 교차 검사했다.
5. HEAD tree와 live filesystem/POM/Java inventory를 분리하고, 파일 존재를 acceptance나 evidence로 승격하지 않았다.
6. Canonical Phase 03 §9.3 exact method matrix와 Target §11.3 method matrix를 정렬해 누락을 계산했다.
7. Local Markdown file 63개와 GFM fragment를 검사하고, 본문 내부 절 참조도 별도로 확인했다.
8. Target에 대해서만 trailing whitespace, tab, CRLF, fence parity, EOF newline과 `git diff --check`를 검사했다.
9. 구현·Maven test는 실행하지 않았다. Phase 03 구현 review가 아니고 live tree가 다른 세션의 미커밋 Phase 00 작업 중이기 때문이다.

Severity는 다음처럼 적용했다.

| Severity | 기준 |
|---|---|
| `CRITICAL` | 권위나 안전 상태를 잘못 확정해 즉시 회복 곤란한 구현·publication을 허용 |
| `HIGH` | 핵심 source/dependency/test gate 위반으로 구현 또는 acceptance를 잘못 이끎 |
| `MEDIUM` | API/실행 지시가 불완전해 사람이 임의 결정을 해야 하거나 검증 재현성이 약화 |
| `LOW` | 의미를 바꾸지는 않지만 링크·절 번호·용어 drift로 탐색성과 신뢰를 해침 |

## 5. Finding summary

| ID | Severity | 요약 | Target 수정 |
|---|---|---|---|
| `HG03-R001` | `HIGH` | HEAD baseline을 live checkout으로 표시해 wrapper/reactor/링크/명령 inventory가 틀림 | `YES` |
| `HG03-R002` | `HIGH` | `propagation`↔`evaluation.api` package 방향을 상위 배치 계약과 반대로 고정 | `YES` |
| `HG03-R003` | `HIGH` | Exact required test 5개와 boundary row 누락으로 false-green 가능 | `YES` |
| `HG03-R004` | `MEDIUM` | Skeletal signature가 참조하는 proposed type의 owner/package가 닫히지 않음 | `YES` |
| `HG03-R005` | `LOW` | §10.2/§10.5 내부 참조 4곳이 존재하지 않는 절을 가리킴 | `YES` |

## 6. Findings

### HG03-R001 — HEAD baseline을 live checkout으로 표시해 실행 inventory가 틀리다

- **Severity:** `HIGH`
- **Finding:** Target metadata는 inventory가 commit `7cc890e…` 기준이라고 올바르게 선언하지만, §6은 이를 “현재 checkout”으로 다시 서술한다. 현재 live tree에는 executable `./mvnw`, 13개 POM, `rpdptw/core`, `build/test-fixtures`, `build/architecture-rules`와 Phase 00 skeleton이 존재한다. 반대로 Target이 링크한 root `src/.../AlnsBatchEngine*.java`는 live tree에서 삭제되고 `legacy/gcp-placeholder`로 이동 중이다.
- **사람에게 미치는 영향:** 구현자는 system `mvn`을 현재 권위 명령으로 선택하고, 실제 reactor가 없다고 오판하며, 깨진 링크 두 개를 따라가고, Phase 00 in-progress drift를 관찰하지 못한다. 다른 방향으로는 live skeleton 존재를 accepted predecessor로 오인할 위험도 있다.
- **Target 위치/anchor:** [§6 실제 inventory](../phases/phase-03-human-implementation-guide.md#6-실제-inventory-현재-상태와-목표-상태), 특히 lines 371~425; metadata lines 13~23; inventory link lines 393~403.
- **Source evidence:** `docs/implementation/execution-progress-and-results.md` §5와 live §10.1; live root `pom.xml` `<modules>` lines 14~18; `rpdptw/pom.xml` lines 16~22; `build/pom.xml` lines 15~18; `git status --short`; Master Realization Plan §3의 historical inventory 표기 규칙.
- **Root cause:** 재현 가능한 HEAD snapshot과 계속 변하는 working-tree inventory를 한 상태로 압축했다. Source fingerprints는 snapshot인데 본문 시제와 명령은 live라고 표현했다.
- **Required correction:** §6을 최소 두 표로 분리한다. 첫 표는 `HEAD baseline at 7cc890e…`, 둘째 표는 검토 시각이 있는 `uncommitted live drift`로 표시한다. Live wrapper/reactor/package-info/Phase 00 tests와 progress status를 기록하되 `NOT_ACCEPTED/NOT_PRODUCED`를 유지한다. Root legacy 링크는 snapshot 링크임을 명시하거나 live `legacy/gcp-placeholder` 위치와 분리한다. 명령은 “accepted wrapper”와 “현재 unaccepted live wrapper”를 구분하고 acceptance 전 evidence 명령으로 승격하지 않는다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Live tree는 다른 작업 중 계속 바뀔 수 있다. 시각·commit·working-tree blob/status digest를 함께 기록하고 구현 착수 때 재-snapshot하지 않으면 다시 stale해진다.

### HG03-R002 — Package dependency 방향이 상위 Domain/Architecture와 충돌하지만 승인된 것처럼 제시된다

- **Severity:** `HIGH`
- **Finding:** Target §9.4는 `domain + travel → propagation → evaluation.api → evaluation.runtime`을 제시하고, §9.1/§9.2는 `RouteFacts`를 `propagation`에 두면서 `evaluation.api`의 `HardConstraint`/`MetricContributor`가 이를 소비하게 한다. `DomainFacetProvider`도 승인 시 `propagation` 소유라고 한다. 그러나 Final Domain §3의 package DAG는 `domain/travel/evaluation.api → propagation`과 `propagation/evaluation.api → evaluation.runtime`을 명시하고, Final Architecture §2.3은 fact와 constraint/metric/score/objective/profile SPI를 `evaluation.api`가 소유한다고 배치한다. Integrated design §7.6도 `DomainFacetProvider`를 `evaluation.api` SPI 목록에 둔다.
- **사람에게 미치는 영향:** 구현자는 어느 package가 fact/facet contract를 소유하는지 결정할 수 없고, 한 문서를 따르면 다른 architecture test/consumer import가 깨진다. 잘못 봉인하면 package cycle을 피하려다 상위 owner contract를 무승인 변경하거나, 반대로 실제 cycle을 만들 수 있다.
- **Target 위치/anchor:** [§9.1 Proposed package](../phases/phase-03-human-implementation-guide.md#91-proposed-package와-file-배치) lines 597~638; [§9.2 Skeletal contract](../phases/phase-03-human-implementation-guide.md#92-skeletal-contract-후보) lines 640~698; [§9.4 Dependency direction](../phases/phase-03-human-implementation-guide.md#94-dependency-direction) lines 726~751.
- **Source evidence:** `docs/2026-07-26-domain-design.md` §3 “전체 처리 흐름과 책임 경계”, lines 300~323; `docs/2026-07-26-architecture-design.md` §2.3 “Package 책임”, lines 267~295; `docs/architecture-domain-implementation-design.md` §7.6 “Evaluation SPI”, lines 1354~1370. Canonical Phase review F-P03-010은 반대 방향 교정을 적용했지만 Target 자신의 authority 순서에서 이 review는 Final Domain/Architecture보다 낮고, 상위 배치 변경 승인 record는 없다.
- **Root cause:** Canonical Phase review의 cycle 회피안을 상위 package-owner drift를 함께 해결하지 않은 채 사람용 가이드에 복제했다.
- **Required correction:** 어느 방향이 옳다고 Target 단독으로 확정하지 않는다. 상위 source conflict, 후보 A(상위 배치대로 fact contract를 `evaluation.api`에 두고 propagation이 소비), 후보 B(현재 Phase review대로 propagation-owned fact/facet을 evaluation이 소비), cycle 조건과 consumer 영향을 명시한다. Core/Evaluation + Architecture owner가 owner/package/import/architecture-test를 승인하기 전 `CROSS-PHASE ARCHITECTURE BLOCKER`로 두고, last safe point는 의미 contract와 package 생성 전 API review로 설정한다. 승인 결과는 관련 authority/ADR와 같은 변경 단위로 반영한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Type 이름만 옮기고 fingerprint ownership, Phase 04 provider, Phase 07 재계산과 architecture test를 함께 갱신하지 않으면 의미상 cycle이 남는다.

### HG03-R003 — Canonical exact test matrix가 축약되어 Target 기준 false-green이 가능하다

- **Severity:** `HIGH`
- **Finding:** Canonical Phase 03 §9.3에는 exact method 42개가 있으나 Target §11.3에는 37개만 있다. 다음 5개가 빠졌다.

  - `consumesExpandedRepeatingAndOvernightWindowsWithoutRawTimeReinterpretation()`
  - `increasingAbsentOrPresentResourceLimitIsMonotone()`
  - `rejectsPoisonedRoutePlanFingerprint()`
  - `keepsNeutralMetricsFreeOfPriceAndPreference()`
  - `scoreReadsOnlyMetricSnapshotAndTypedParameters()`

  또한 Target의 `rejectsOneUnitBeyondEveryHardBound()` 판정은 “weight/volume/time/stop/drive 각 row”로 축약돼 canonical의 weight/volume lower+upper, service close, due, plan end, full-arc work end, stop, drive time/distance exact rows를 실행 가능하게 열거하지 않는다. §11.5는 Target §11.3 목록만 fresh report에 있으면 된다고 하므로 누락을 스스로 탐지하지 못한다.
- **사람에게 미치는 영향:** 반복/overnight authority 재해석, resource-limit monotonicity 역전, poisoned route fingerprint, 가격이 섞인 metric, score의 raw-route 접근이 있어도 Target의 required manifest는 green이 될 수 있다. 이는 identity, layer isolation과 false-green 방지 목적을 직접 훼손한다.
- **Target 위치/anchor:** [§11.3 Test class와 method 후보](../phases/phase-03-human-implementation-guide.md#113-test-class와-method-후보) lines 1048~1089; [§11.5 Maven 명령과 판정](../phases/phase-03-human-implementation-guide.md#115-maven-명령과-fail-closed-판정) lines 1156~1174.
- **Source evidence:** `docs/implementation/phases/phase-03-route-propagation-evaluation-kernel.md` §9.3, lines 789~838; 같은 문서 §9.4; `docs/implementation/reviews/phase-03-review.md` F-P03-003과 F-P03-009; Final Domain §17.2 time/service evidence와 §17.5 propagation evidence.
- **Root cause:** “후보” 표를 짧게 만들면서 canonical exact acceptance inventory와 boundary sensitivity를 부분 요약했다.
- **Required correction:** 누락된 5개 method와 builder/oracle/green 판정을 §11.3 및 required manifest에 복원한다. `rejectsOneUnitBeyondEveryHardBound()`의 exact parameterized rows와 expected category/code/call-count를 열거한다. §11.5 pass 판정은 canonical matrix digest 또는 동등한 exact inventory와 Target 표 양쪽의 차집합이 0인지 검사하게 한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** 이름만 복원하고 faulty double/independent oracle가 production helper를 공유하면 sensitivity evidence는 여전히 false-green이다.

### HG03-R004 — Proposed Java tree와 skeletal signature가 type ownership 관점에서 닫히지 않았다

- **Severity:** `MEDIUM`
- **Finding:** §9.2의 `MetricContributor`와 `HardConstraint`는 `MetricDeclaration`, `MetricValue`, `ConstraintKey`, `ConstraintCheck`, `MetricSnapshot`을 참조하지만 §9.1 tree에는 이 파일들이 없고 package/visibility/identity owner도 정해져 있지 않다. 같은 tree에는 `RouteFacts`가 요구하는 fingerprint/value type과 objective vector/tie 관련 contract도 부분적으로만 나타난다. “완성 코드가 아닌 뼈대”라는 주의는 적절하지만, compile closure와 package review에는 부족하다.
- **사람에게 미치는 영향:** 프로젝트를 모르는 구현자는 누락 type을 임의 package에 만들거나 raw `Map`/generic value로 대체할 수 있다. 특히 HG03-R002의 package 방향 conflict를 무의식적으로 닫을 가능성이 크다.
- **Target 위치/anchor:** [§9.1](../phases/phase-03-human-implementation-guide.md#91-proposed-package와-file-배치) lines 597~638; [§9.2](../phases/phase-03-human-implementation-guide.md#92-skeletal-contract-후보) lines 640~698; §7.5 API freeze checkpoint.
- **Source evidence:** Final Architecture §2.3~§2.5의 package/contract owner; Canonical Phase 03 §6과 §7.2 proposed tree/signature; Master §1.2의 “구체 API는 source가 자동 확정하지 않음” 규칙.
- **Root cause:** 완성 코드 복붙을 피하려는 축약이 contract dependency inventory까지 생략했다.
- **Required correction:** 완성 구현은 제공하지 않되, snippet이 의도적으로 비컴파일인지 또는 compile-closed review skeleton인지 명시한다. 후자라면 모든 참조 type에 대해 proposed owner package, visibility, semantic fields, unit/fingerprint 책임과 dependency edge를 표로 추가한다. HG03-R002가 승인되기 전 실제 파일 생성은 계속 금지한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Compile만 되도록 빈 marker type을 추가하면 semantic closure, immutability와 corruption test가 여전히 비어 있을 수 있다.

### HG03-R005 — 실행 지시의 내부 절 번호 네 곳이 잘못됐다

- **Severity:** `LOW`
- **Finding:** Target lines 425, 527, 880, 904는 future command 또는 mixed hand fixture를 각각 `§10.5`, `§10.2`라고 부른다. 실제 heading은 Maven command가 §11.5, hand fixture가 §11.2다. §10에는 WP heading만 있고 §10.2/§10.5 heading은 존재하지 않는다.
- **사람에게 미치는 영향:** 구현자가 명령 precondition과 oracle expected table을 찾지 못하거나, WP 번호를 section 번호로 오해한다.
- **Target 위치/anchor:** §6.3 line 425; §8.2 line 527; WP-03.1 line 880; WP-03.2 line 904.
- **Source evidence:** Target heading inventory의 §10 “Ordered work packages”, §11.2 “필수 hand-calculated fixture”, §11.5 “Maven 명령과 fail-closed 판정”.
- **Root cause:** §10에서 §11로 테스트 장을 이동한 뒤 본문 cross-reference를 함께 갱신하지 않았다.
- **Required correction:** 두 `§10.2`를 `§11.2`로, 두 `§10.5`를 `§11.5`로 바꾼다. 이후 heading-aware 내부 참조 검사를 추가한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Markdown link가 아닌 평문 절 참조는 일반 link checker가 잡지 못하므로 재배치 시 다시 stale할 수 있다.

## 7. 검사별 판정과 residual risk

| 검사 | 판정 | 근거 | Residual risk |
|---:|---|---|---|
| 1. 원문 의미·불변조건·identity/lifecycle·Phase 경계 | `PASS WITH FINDING` | Pair, prefix load, directed travel, full-arc, triage, cache 비권위는 source와 일치. Package owner는 `HG03-R002` | Full-solution evaluator와 tie API는 기존 residual blocker |
| 2. 신규 Java/CVRPTW 구현자 이해 가능성 | `PASS WITH FINDING` | Primer, 큰 그림, producer/consumer, 학습 경로가 충분함 | `HG03-R004`, `HG03-R005` 교정 전에는 API/탐색이 불완전 |
| 3. 명령/WP 실행·판정 가능성 | `PASS WITH FINDING` | 모든 WP에 목적, 사전조건, target, 행동, 검증, 기대, 실패, rollback, handoff가 있음 | `HG03-R001`, `HG03-R005`로 현재 명령과 참조가 부정확 |
| 4. Java/Maven 실제 구조와 proposed contract | `FAIL` | Live reactor는 관찰했으나 Target inventory와 package/type closure가 불일치 | `HG03-R001`, `HG03-R002`, `HG03-R004` |
| 5. 완성 코드 복붙/과도한 추상화 | `PASS WITH FINDING` | Full implementation을 제공하지 않고 pseudocode와 skeleton만 제공 | `HG03-R004`처럼 필요한 ownership까지 생략하면 임의 구현 위험 |
| 6. Entry/exit/evidence/rollback/failure/security/observability/reproducibility | `PASS` | AND gate, WP rollback, evidence DAG, redaction, fingerprint와 repeat/parallel 요구가 있음 | 실제 evidence는 `NOT_PRODUCED` |
| 7. OPEN/GATED/deferred/EXPERIMENT_REQUIRED | `PASS` | `Q-BENCH-02`, `C-17`, `Q-VAR-01`, facet/public API/performance를 숨은 값으로 닫지 않음 | 승인 record 없이 상태를 바꾸면 재검토 필요 |
| 8. Phase 13 C-17/14A와 Phase 14 authority | `PASS` | `00→…→08→14A`, receipt+별도 승인→13, 14B 분리를 정확히 보존 | Corpus/criteria/receipt와 production authority는 여전히 없음 |
| 9. 인접 Phase ownership/dependency/lifecycle | `PASS WITH RESIDUAL` | Phase 02 refs와 Phase 04 handoff가 인접 사람용 가이드와 대체로 일치 | Package owner conflict와 기존 solution/tie blocker는 공동 승인 필요 |
| 10. 링크/GFM/source fingerprint/traceability/용어 | `FAIL` | HEAD blob과 61개 유효 file/fragment link는 통과 | Live 이동으로 file link 2개가 깨졌고 `HG03-R005` 평문 절 참조 4개가 stale |
| 11. 사람 결정·승인과 마지막 안전 지점 | `PASS` | §5.3, §7.5와 각 WP rollback이 승인 전 문서/oracle/API review를 last safe point로 둠 | HG03-R002 architecture conflict도 명시적 checkpoint로 추가해야 함 |
| 12. 문서 완성과 구현 완료/legacy 11-Phase 혼동 | `PASS` | 15 Phase, `0/15`, `NOT_STARTED/NOT_ACCEPTED/NOT_PRODUCED`, receipt 필요를 반복 명시 | Live skeleton을 acceptance로 오인하지 않도록 R001 교정 필요 |
| 13. Fixture/builder/oracle/red→green/false-green/category/pass | `FAIL` | Hand fixture와 oracle 분리는 좋지만 exact required inventory가 축약됨 | `HG03-R003` 교정 전 canonical 결함 5종과 boundary row가 누락 가능 |

## 8. Target 수정과 구분되는 implementation blocker

다음은 Target이 이미 대체로 올바르게 기록한 실제 implementation blocker다. 이 review가 Target 교정 요구와 섞어 닫지 않는다.

- Phase 00은 live remediation code가 있어도 independent review/evidence/acceptance receipt가 없어 `NOT_ACCEPTED`다.
- Phase 01과 Phase 02 accepted artifact/evidence/receipt가 없다.
- Phase 03 production type, test, `E-P03-*`, pre-review manifest와 acceptance receipt가 없다.
- Full-solution evaluator owner/API/identity/invalidation/failure/comparator 계약은 Phase 03~05 공동 blocker다.
- Business objective equality와 solution/insertion context tie boundary는 Phase 03/05/06 공동 blocker다.
- `Q-BENCH-02`, Phase 14A corpus/criteria/receipt, `C-17`, backend/운영 승인과 Phase 14B production authority는 여전히 OPEN/GATED다.

이 항목들은 실제 구현 착수를 막거나 downstream acceptance를 막지만, 존재 자체가 Target의 새로운 결함은 아니다. 반대로 live Phase 00 drift는 구현 완료 증거가 아니다.

## 9. 링크·구조·whitespace 정적 검사

Target 파일만 대상으로 검사했다.

| 검사 | 결과 | 비고 |
|---|---|---|
| H1/필수 주요 section | `PASS` | H1 1개, §1~§17 존재 |
| Fence parity | `PASS` | Fence marker 72개, 짝수 |
| Trailing whitespace | `PASS` | 0줄 |
| Tab | `PASS` | 0줄 |
| CRLF | `PASS` | 0줄 |
| EOF newline | `PASS` | LF |
| Local Markdown links/GFM fragments | `FAIL` | 63개 중 live file target 2개 누락; 나머지 file/fragment 유효 |
| 누락 file link | `FAIL` | Root `src/.../AlnsBatchEngine.java`, `AlnsBatchEngineTest.java`; live 이동 상태 |
| 평문 section reference | `FAIL` | §10.2 2곳, §10.5 2곳 |
| Declared HEAD source fingerprint | `PASS` | 재계산 mismatch 0 |
| `git diff --check -- <target>` | `PASS` | Exit 0. Target이 untracked라 tracked diff만 보는 한계가 있음 |
| Untracked-aware `git diff --no-index --check /dev/null <target>` | `PASS` | Raw exit 1은 파일 차이, whitespace diagnostic 0 |

## 10. 최종 verdict

`CHANGES_REQUIRED`

Target의 핵심 도메인 의미와 gate 보존은 강하지만, 실제 inventory, package-owner conflict, exact test matrix, proposed type closure와 내부 참조를 교정해야 사람이 안전하게 구현·판정할 수 있다. 다섯 finding 모두 Target 수정이 필요하다. Reviewer는 Target, 다른 guide/review/correction, README/progress, canonical 문서, 코드/POM/test/deployment를 수정하지 않았다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=3 MEDIUM=1 LOW=1
REQUIRED_CORRECTION_FINDINGS: HG03-R001,HG03-R002,HG03-R003,HG03-R004,HG03-R005

## Correction 01 읽기 전용 재검증

### 재검증 metadata

```yaml
recheck_round: "01"
recheck_type: ORIGINAL_REVIEWER_READ_ONLY_FOLLOW_UP
rechecked_at: 2026-07-29T02:43:42+09:00
timezone: Asia/Seoul
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
original_review_sha256_before_append: 0184730a8503c825fb02eee55f7391a6c49951835c511922f9a05320528abfcc
original_review_git_hash_object_before_append: ad46c1dde7b7035ab074c464297c56c2675df36a
corrected_target_sha256: f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3
corrected_target_git_hash_object: f8c9db72ad5bc1a6b5c23d658b5d4e6280f96fae
correction_report_sha256: c166e1f064ca3a42ddd71a05b1bcb557e36578fd88f2a0e8a251f2893c73534f
correction_report_git_hash_object: b042303c73db11a039d0723393d6c2e414c8385d
recheck_verdict: FURTHER_CORRECTION_REQUIRED
```

Correction report의 해결 주장을 판정 근거로 사용하지 않고 corrected Target의 실제 본문, canonical source, 인접 handoff와 live filesystem/POM을 다시 대조했다. 원 finding 5건은 모두 `RESOLVED`다. 다만 Target §6 heading 변경이 원 review의 immutable link 하나를 끊은 새 traceability regression `HG03-R006`이 있어 전체 재검증 verdict는 `FURTHER_CORRECTION_REQUIRED`다.

### 읽은 파일과 고정 hash

| 입력 | 재검증 범위 | Hash |
|---|---|---|
| 이 원 review의 append 전 상태 | 전체, 특히 HG03-R001~R005와 정적 검사 | SHA-256 `0184730a8503c825fb02eee55f7391a6c49951835c511922f9a05320528abfcc` |
| Corrected Target | 전체 1,620줄 | SHA-256 `f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3`; Git object `f8c9db72ad5bc1a6b5c23d658b5d4e6280f96fae` |
| Correction 01 report | 전체 152줄 | SHA-256 `c166e1f064ca3a42ddd71a05b1bcb557e36578fd88f2a0e8a251f2893c73534f`; Git object `b042303c73db11a039d0723393d6c2e414c8385d` |
| `docs/master-design.md` | 의미, gate, evaluation, evidence | HEAD blob `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| `docs/domain-design.md` / `docs/architecture-design.md` | 현재 REVIEW package map | HEAD blobs `ace117c380466b733994a1fbb2a95d31e41b3959` / `81495ff448d0e618ab3563e8ff80614fb1028acf` |
| Dated Domain / Architecture | §3 package DAG / §2.3 package owner | HEAD blobs `0a02ba4c77a402455e3d80b76969dca28831b1e6` / `d51339e251dee1e032e711144dc63d6d07d7323b` |
| Integrated design / question register | Phase 03~04 SPI와 OPEN/GATED/deferred | HEAD blobs `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` / `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| Master plan / implementation README | inventory, entry/exit, evidence DAG | HEAD blobs `d7f6be4fff0089204fbdb52f731b2348407f36eb` / `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| Execution progress | §5, §8, live §10.1 | HEAD blob `250aa90ae568a6b32ec905fa5ee456d430ff72cf`; recheck live blob `0419f69199b3140dd44020f78278b1352e6517b8` |
| Canonical Phase 03 / original Phase review | Exact §9.3 matrix, package correction과 residual blockers | HEAD blobs `74098f7e15cf75bcc443ae009cc475a9b60d63a3` / `e473887ffbbb165f18ea6aaa76d6fc3917fc4af2` |
| Canonical Phase 02 / Phase 04 | Producer/consumer handoff | HEAD blobs `8b5f115369ca2189c80de12868fb3a63104d9228` / `e0a68fd442a7db383e4a650e4a561234c323b1fe` |
| 인접 사람용 Phase 02 / Phase 04 | Current producer/consumer guide cross-check | SHA-256 `123405df8823ef901e6a4f6ef8a77e18208965f40c25e3385cbdccc2e9f57ace` / `c5ddc9a469792f8e4e0b6b611e8e0711025e2d3991c66b03f36fc5100dc6504c` |

### Finding별 판정

#### HG03-R001 — RESOLVED

- **확인한 Target anchor:** metadata lines 17~22, §3.2, §5.2, §6.1~6.3, §11.1과 §11.5.
- **직접 확인:** Target은 `7cc890e…` HEAD baseline과 `2026-07-29T02:03:10+09:00` 미커밋 live snapshot을 별도 표로 분리한다. HEAD placeholder는 `git show` 대상이라고 명시하고 live legacy 링크는 실제 `legacy/gcp-placeholder`로 바꿨다. Wrapper/reactor를 `UNCOMMITTED_UNAPPROVED_SNAPSHOT`으로 유지하며 accepted future command, zero-test, Surefire/Failsafe와 source-set precondition을 분리했다.
- **Source evidence:** Master Realization Plan §3, Execution Progress §5와 live §10.1, root `pom.xml`, `rpdptw/core/pom.xml`, `build/test-fixtures/pom.xml`, 현재 filesystem inventory.
- **Root cause closure:** Snapshot authority와 계속 변하는 live state를 같은 “현재 checkout”으로 압축하던 원인이 시점·commit·status digest·관련 blob/count 분리로 닫혔다.
- **남은 risk:** 재검증 시점에는 progress가 Phase 00 Fix 02로 이동했고 live progress blob은 `0419f6…`, architecture/fixture `*Test.java`는 10개다. Target snapshot의 Fix 01/9개/`36afdf…`와 다르지만 §6.2가 이를 timestamped snapshot으로 제한하고 §6.2 마지막 문단이 착수 전 재-snapshot을 요구하므로 finding 재개 사유는 아니다. Phase 03 type/test/evidence는 여전히 0이다.

#### HG03-R002 — RESOLVED

- **확인한 Target anchor:** §3.1 lines 146~170, §7.4~7.5, §9.1~9.4, WP-03.0, §12.1 C0, §15.2와 §16 `REQ-P03-PACKAGE-OWNER`.
- **직접 확인:** Target은 `evaluation.api` owner 후보 A와 propagation owner 후보 B를 각각 acyclic DAG로 제시하고 어느 쪽도 승인된 사실로 고정하지 않는다. Fact/failure/facet family, Phase 04/05/07 consumer 영향, architecture test, owner와 last safe point를 명시하고 승인 전 package/file 생성을 금지한다.
- **Source evidence:** Current Domain §3.1과 Current Architecture §6.1은 `evaluation.api` fact owner 방향, dated Final Domain §3 lines 314~323과 dated Final Architecture §2.3 lines 267~295도 같은 방향이다. Integrated §7.6은 `DomainFacetProvider`를 evaluation SPI로 둔다. Original Phase review F-P03-010은 propagation owner 방향이어서 실제 cross-source conflict가 존재한다.
- **Root cause closure:** 낮은 implementation review의 cycle 회피안을 상위 owner 변경 승인처럼 복제하던 원인이 명시적 `CROSS-PHASE ARCHITECTURE BLOCKER`와 A/B decision record gate로 닫혔다.
- **남은 risk:** 후보 선택 자체는 의도적으로 OPEN이다. Type만 이동하고 fingerprint/provider/verifier/architecture test를 함께 갱신하지 않으면 cycle 또는 duplicate authority가 남는다.

#### HG03-R003 — RESOLVED

- **확인한 Target anchor:** §11.3 lines 1155~1218, §11.5 lines 1298~1306, §12.1 C5, §14.1과 §16 `REQ-P03-EXACT-MANIFEST`.
- **직접 확인:** Canonical Phase 03 §9.3과 Target §11.3의 table-order `Class#method`를 독립 추출한 결과 각각 42개, duplicate 0, 양방향 차집합 0이었다. 둘의 UTF-8/LF manifest SHA-256은 모두 `79379c996379669725e70dd9577bce42d4fb1c5de4f00024d5415de4e75c6e1c`다. 원래 누락된 5개 method가 올바른 class/order에 복원됐다.
- **Boundary/false-green:** `rejectsOneUnitBeyondEveryHardBound()`는 weight/volume lower+upper, service close, completion due, exclusive plan end, full-arc work end, stop, drive time/distance의 11개 row를 category/code/call-count와 함께 열거한다. Fresh report, actual underlying method, parameter row, missing/duplicate/failed/error/skipped 0과 양방향 set equality를 pass 조건으로 둔다.
- **Source evidence:** Canonical Phase 03 §9.3~§9.4, original canonical Phase review F-P03-003/F-P03-009, dated Final Domain §17.2/§17.5.
- **Root cause closure:** 교육용 축약 표를 exact acceptance manifest로 오인하던 원인이 canonical digest와 parameter-row manifest로 닫혔다.
- **남은 risk:** 실제 source/test/report는 `NOT_PRODUCED`다. 이름만 맞고 oracle이 production helper를 공유하거나 11개 row 일부만 실행하면 여전히 실패하도록 evidence review가 판정해야 한다.

#### HG03-R004 — RESOLVED

- **확인한 Target anchor:** §9.1 lines 622~676, §9.2 lines 678~745, §11.1 lines 1077~1097, §11.5 lines 1234~1296.
- **직접 확인:** Snippet은 package/import가 생략된 의도적 non-compile-closed review fragment라고 명시됐다. Tree에 metric/constraint/objective value type이 추가됐고 closure table은 모든 참조 family의 proposed owner/visibility, semantic field, unit/fingerprint와 allowed edge를 제공한다. Owner-sensitive fact/failure/facet은 R002 결정 전 경로를 비워 둔다.
- **Maven/source-set 확인:** Live `build/test-fixtures`의 core dependency는 test scope이고 test-JAR가 attach된다. `rpdptw-core`에는 JUnit test dependency가 없으며 root Surefire `3.5.4`는 `failIfNoTests=false`, Failsafe binding은 없다. Target은 이 실제 상태와 future accepted wiring, `*Test`/`*IT`, zero-test 판정을 분리한다.
- **Source evidence:** Current Architecture §6.1/§8, dated Final Architecture §2.3~§2.5, canonical Phase 03 §6~§7.2, live POM 세 개.
- **Root cause closure:** 복붙 방지를 이유로 contract dependency inventory까지 생략하던 원인이 non-compiling status와 semantic closure table로 닫혔다.
- **남은 risk:** 승인된 compile-closed skeleton은 아직 없다. 빈 marker/raw map으로 compile만 맞추지 않는 별도 API/compile review가 계속 필요하다.

#### HG03-R005 — RESOLVED

- **확인한 Target anchor:** §6.3 line 446, §8.2 line 552, WP-03.1 line 976, WP-03.2 line 1000.
- **직접 확인:** 원래 잘못된 실행 참조 두 `§10.2`는 `§11.2`, 두 `§10.5`는 `§11.5`로 교정됐다. 현재 남은 `§10.2` 한 건은 §3.2가 historical `2026-07-26 Master`의 source section을 가리키는 의도적 참조다.
- **Source evidence:** Corrected Target heading inventory의 §11.2와 §11.5.
- **Root cause closure:** 테스트 장 이동 뒤 갱신되지 않은 네 평문 참조가 정확한 현재 절로 연결됐다.
- **남은 risk:** 평문 section reference는 일반 Markdown link checker가 잡지 못하므로 heading-aware 검사를 계속 유지해야 한다.

### NEW HG03-R006 — LOW — OPEN

- **Finding:** Correction이 Target의 §6 heading을 `실제 inventory (현재 상태와 목표 상태)`에서 `실제 inventory: HEAD baseline, 미커밋 live snapshot과 목표`로 바꾸면서 원 review HG03-R001의 Target anchor `#6-실제-inventory-현재-상태와-목표-상태`가 더 이상 존재하지 않는다.
- **사람에게 미치는 영향:** Correction provenance의 출발점인 원 review에서 R001의 정확한 Target 위치로 이동할 수 없어 immutable review → corrected target 추적성이 한 곳 끊긴다.
- **위치/evidence:** 이 review의 기존 HG03-R001 “Target 위치/anchor”; corrected Target §6 line 393. GFM scanner 결과 Target 73 links/0 broken, correction report 26/0, 이 review 8 links/1 broken이다.
- **Root cause:** Heading을 더 정확하게 바꾸면서 기존 inbound fragment의 호환 anchor를 보존하지 않았고 correction 정적 검사가 Target과 correction report만 검사했다.
- **Required correction:** Corrected Target의 새 §6 heading 바로 앞에 기존 fragment를 보존하는 명시적 alias `<a id="6-실제-inventory-현재-상태와-목표-상태"></a>`를 추가한다. 기존 review 본문이나 finding을 덮어쓰지 않는다. 이후 Target, correction report와 원 review를 함께 link/GFM 재검사한다.
- **Target 수정 필요 여부:** `YES`
- **남은 risk:** 향후 heading rename에서도 inbound review/correction link를 함께 검사하지 않으면 같은 회귀가 반복된다.

### 정적·manifest 재검증

| 검사 | 결과 | 근거 |
|---|---|---|
| Target H1/heading/fence | `PASS` | H1 1개, fence marker 74개로 짝수 |
| Correction report H1/fence | `PASS` | H1 1개, fence marker 2개 |
| Target/correction whitespace | `PASS` | 두 파일 모두 trailing whitespace 0, tab 0, CRLF 0, EOF LF |
| Scoped `git diff --check` | `PASS` | Target/correction 대상 exit 0 |
| Untracked-aware no-index whitespace | `PASS` | 두 파일 모두 raw exit 1은 content difference, diagnostic 0 byte |
| Target local links/GFM | `PASS` | 73개, broken 0 |
| Correction report local links/GFM | `PASS` | 26개, broken 0 |
| 원 review inbound link | `FAIL` | 8개 중 corrected Target §6의 legacy fragment 1개 broken; `HG03-R006` |
| Wrong current section reference | `PASS` | 실행 문맥의 stale §10.2/§10.5 0; historical source §10.2만 1개 |
| Exact method manifest | `PASS` | Canonical 42, Target 42, duplicate/양방향 차집합 0, SHA-256 exact |
| Boundary parameter manifest | `PASS` | 11개 exact row와 category/code/call-count 존재 |
| Maven discovery/zero-test/stale evidence | `PASS` | Live와 future wiring, Surefire/Failsafe, selected-test zero guard, clean/fresh XML 및 run별 sealing 구분 |
| Entry/exit/evidence DAG | `PASS` | Phase 00~02 receipts와 owner decision은 AND gate; M→R→receipt 단방향이며 code/test만으로 완료하지 않음 |
| OPEN/GATED/deferred와 Phase 13/14 | `PASS` | Package owner, solution evaluator/tie, `Q-BENCH-02`, `C-17`, `Q-VAR-01`, 14A/14B authority가 값으로 닫히지 않음 |
| Adjacent handoff/security/failure/rollback | `PASS` | Phase 02 refs, Phase 04 fact/SPI 소비, safe redaction, `Invalid`/`Infeasible`, 각 WP last safe/rollback과 receipt handoff 유지 |
| 구현/test 실행 | `NOT_RUN` | 문서 correction 재검증이며 Phase 03 source/test/evidence가 없음 |

원 finding 5건은 모두 해결됐지만 새 Target 수정 finding 1건이 있으므로 Correction 01 전체는 아직 수용할 수 없다. 이 판정은 Phase 03 구현 완료나 acceptance를 뜻하지 않으며 Target, correction report, 코드/POM/test, README/progress 또는 다른 review는 수정하지 않았다.

RECHECK_ROUND: 01
RECHECK_VERDICT: FURTHER_CORRECTION_REQUIRED
RESOLVED_FINDINGS: HG03-R001,HG03-R002,HG03-R003,HG03-R004,HG03-R005
OPEN_FINDINGS: HG03-R006
TARGET_HASH_RECHECKED: f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3
CORRECTION_REPORT_HASH_RECHECKED: c166e1f064ca3a42ddd71a05b1bcb557e36578fd88f2a0e8a251f2893c73534f

## Correction 02 읽기 전용 재검증

### 재검증 metadata

```yaml
recheck_round: "02"
recheck_type: ORIGINAL_REVIEWER_READ_ONLY_FOLLOW_UP
rechecked_at: 2026-07-29T02:53:45+09:00
timezone: Asia/Seoul
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
review_sha256_before_append: 9ff7f1dbf524601a1ba51daae869af921e2f700967e4a64ec7b3454f1d064755
target_sha256: 45589e13070291de5de46e9f92fcf156d23b424ae25bddcdae9044ef8a9b7926
target_git_hash_object: 205dfb8a7a9c9f2cde4001734ac7a46de7fb0f1a
correction_01_sha256: c166e1f064ca3a42ddd71a05b1bcb557e36578fd88f2a0e8a251f2893c73534f
correction_02_sha256: bf98a3c22fc6792fc6b78e3f7b18375001782d74d1acd969a93ba5bf610a906e
correction_02_git_hash_object: eb70bf2e4ee5f213a450deddfb7b4aa5ef3144f9
recheck_verdict: ACCEPTED
```

Correction 02 report의 자기 판정을 사용하지 않고 Target의 실제 alias bytes, 원 review의 기존 inbound link와 네 문서의 GFM/정적 상태를 직접 재검증했다.

### HG03-R006 — RESOLVED

- **확인한 Target anchor:** corrected Target lines 393~395. `<a id="6-실제-inventory-현재-상태와-목표-상태"></a>`가 새 `## 6. 실제 inventory: HEAD baseline, 미커밋 live snapshot과 목표` 바로 앞에 있다.
- **유일성/GFM 판정:** Alias block은 정확히 1개이고 같은 `id` 중복은 0이다. Fenced code가 아닌 실제 HTML anchor이므로 GFM fragment target으로 인식된다.
- **Inbound link 판정:** 원 review HG03-R001의 기존 `#6-실제-inventory-현재-상태와-목표-상태`가 이 explicit id로 resolve된다. 원 review의 local/inbound Markdown link 8개 전체가 `broken 0`이다.
- **변경 최소성:** 현재 Target에서 정확한 alias block과 뒤의 빈 줄만 in-memory 제거한 SHA-256은 `f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3`로 Correction 01 재검증 당시 Target hash와 정확히 같다. 현재 Target SHA-256은 `45589e13070291de5de46e9f92fcf156d23b424ae25bddcdae9044ef8a9b7926`이다.
- **Root cause closure:** Heading rename 때 legacy inbound fragment를 검사·보존하지 않았던 원인은 compatibility alias와 원 review 포함 GFM 재검사로 닫혔다.
- **남은 risk:** 향후 heading rename에서도 Target만 검사하면 같은 회귀가 생길 수 있으므로 immutable review/correction inbound link를 함께 검사해야 한다. 현재 추가 correction을 요구하는 OPEN finding은 없다.

### Hash와 정적 재검증

| Artifact/검사 | 결과 | 근거 |
|---|---|---|
| Target hash | `PASS` | SHA-256 `45589e13070291de5de46e9f92fcf156d23b424ae25bddcdae9044ef8a9b7926`; Git object `205dfb8a7a9c9f2cde4001734ac7a46de7fb0f1a` |
| Correction 01 hash | `PASS` | SHA-256 `c166e1f064ca3a42ddd71a05b1bcb557e36578fd88f2a0e8a251f2893c73534f`; Correction 01 재검증 이후 불변 |
| Correction 02 hash | `PASS` | SHA-256 `bf98a3c22fc6792fc6b78e3f7b18375001782d74d1acd969a93ba5bf610a906e`; Git object `eb70bf2e4ee5f213a450deddfb7b4aa5ef3144f9` |
| Target link/GFM | `PASS` | Local Markdown link 73개, broken 0; legacy explicit id 유효 |
| Correction 01 link/GFM | `PASS` | 26개, broken 0 |
| Correction 02 link/GFM | `PASS` | 3개, broken 0 |
| 원 review link/GFM | `PASS` | 기존 8개, broken 0; HG03-R006 재현 경로 해결 |
| Heading | `PASS` | Target H1 1/heading 75, Correction 01 H1 1/heading 13, Correction 02 H1 1/heading 7 |
| Fence parity | `PASS` | Target 74, Correction 01 2, Correction 02 4; 모두 짝수 |
| Whitespace/EOF | `PASS` | Target과 Correction 01/02 모두 trailing whitespace 0, tab 0, CRLF 0, EOF LF |
| Scoped `git diff --check` | `PASS` | Target, Correction 01/02와 review 대상 diagnostic 0 |
| Untracked-aware no-index check | `PASS` | Target과 Correction 01/02 모두 raw exit 1은 content difference이고 whitespace diagnostic 0 byte |
| Contract/gate regression | `PASS` | Alias 제거 시 previous Target bytes가 exact 복원되므로 기존 heading, entry/exit, OPEN/GATED/deferred, evidence와 implementation 계약의 다른 byte 변경 0 |
| Implementation/test 실행 | `NOT_RUN` | Fragment compatibility correction의 문서 재검증이며 코드/POM/test는 read-only |

`HG03-R006`은 해결됐다. Correction 02는 legacy fragment traceability 범위에서 `ACCEPTED`이며 Phase 03 구현, evidence 또는 acceptance를 승인하는 판정은 아니다. Target, Correction 01/02, 코드/POM/test, README/progress와 다른 review는 수정하지 않았다.

RECHECK_ROUND: 02
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: HG03-R006
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: 45589e13070291de5de46e9f92fcf156d23b424ae25bddcdae9044ef8a9b7926
CORRECTION_REPORT_HASH_RECHECKED: bf98a3c22fc6792fc6b78e3f7b18375001782d74d1acd969a93ba5bf610a906e
