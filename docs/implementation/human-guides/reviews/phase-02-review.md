# Phase 02 사람용 구현 가이드 독립 review

## 1. Metadata

| 항목 | 값 |
|---|---|
| Phase | `02` — Prepared travel과 immutable problem |
| Reviewer task 성격 | 작성자 세션과 분리된 새 작업에서 수행한 독립 문서 review. 구현·수정·승인 대행이 아니라 사람용 가이드의 정확성·실행 가능성 판정 |
| Target | `docs/implementation/human-guides/phases/phase-02-human-implementation-guide.md` |
| Target line 수 | `1,271` |
| Target SHA-256 | `93c53ea2055bea89df4cb70dddc95b66b035f01bc392764d59d1ec86dea87f3a` |
| Target working-tree Git blob | `cd2357922066d0508b1146e36734d0d431e9b31a` |
| Target 조사 기준 commit 표기 | `7cc890ee1d0805df5ae14b633127fade4f978639` |
| Review 기준시각 | `2026-07-29T01:27:34+09:00` (`Asia/Seoul`) |
| Review 범위 | Target 의미·교육성·실행성·Java/Maven 정합성·gate/evidence/test/link/whitespace |
| 변경 범위 | 이 review 파일 하나만 생성. Target, README/progress, 다른 guide/review/correction, canonical 문서, code/POM/test/deployment는 수정하지 않음 |
| 최종 verdict | `CHANGES_REQUIRED` |

Fingerprint는 review 시점의 working bytes에 대한 `git hash-object` 값이다. 이는 source authority를 대신하지 않으며, 아래의 HEAD baseline과 미커밋 live drift를 구분하기 위한 재현 정보다.

### 1.1 Reviewed source와 fingerprint

| Source | 직접 대조한 section | Working-tree Git blob |
|---|---|---|
| `docs/master-design.md` | §1.5, §2, §4.1~§4.7, §5~§8, §13~§17 | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` |
| `docs/2026-07-26-domain-design.md` | §1~§7, §16~§18, 특히 §2.2~§2.4·§6·§7·§17.4 | `0a02ba4c77a402455e3d80b76969dca28831b1e6` |
| `docs/2026-07-26-architecture-design.md` | §1~§2, §5.5~§5.6, §6 | `d51339e251dee1e032e711144dc63d6d07d7323b` |
| `docs/architecture-domain-implementation-design.md` | §1~§3, §5~§7, §19~§25 | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` |
| `docs/master-design-open-questions.md` | §1~§4, `Q-NUM-*`, `Q-MTX-*`, `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` |
| `docs/implementation/master-realization-plan.md` | §1~§9, Phase 01~03, §10~§15 | `d7f6be4fff0089204fbdb52f731b2348407f36eb` |
| `docs/implementation/README.md` | §0~§7 | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` |
| `docs/implementation/execution-progress-and-results.md` | §2, §5, §6.3, §8~§10 | `3e9dcd16621e459e4572a094cb71ab62a3978360` |
| `docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md` | 전체, 특히 §3~§14 | `8b5f115369ca2189c80de12868fb3a63104d9228` |
| `docs/implementation/reviews/phase-02-review.md` | 전체, 특히 F-P02-01~06와 blocker ledger | `1b5dcbc0a10d1cd2c3b060a6fe874b736fe3fd65` |
| 인접 human guide `phase-01-human-implementation-guide.md` | Producer artifact, Phase 02 handoff, value type와 implementation state | `326c99bbc3446fd6fda3f8ab52345fd34d69556c` |
| Target human guide `phase-02-human-implementation-guide.md` | 전체 | `cd2357922066d0508b1146e36734d0d431e9b31a` |
| 인접 human guide `phase-03-human-implementation-guide.md` | Phase 02 entry/handoff, delivery-only, consumer equality | `6c5d7f1fa3cd3c574194396479dc20ececd131fd` |
| `docs/implementation/human-guides/README.md` | Authority, 15 Phase index, review link | `af0ef4982cf7d84ad74c6d94082d053a17bc350f` |
| `docs/implementation/human-guides/execution-progress-and-results.md` | Phase 02 guide/review task 상태 | `dd10d40c35dffb00b9926985cf66697e45625fea` |
| Live `pom.xml` | Packaging, modules, dependency/plugin policy | `1dc675ba17b7f2202f34a22131f152cc2868b075` |
| `.sdkmanrc` | Java/Maven version | `62506cdf7dbd884fce313f6bcc1bb65adc5c3b9d` |

`docs/2026-07-26-master-design.md`와 `docs/codex/`는 historical cross-check로만 취급했고 현재 결정을 덮는 근거로 사용하지 않았다.

### 1.2 Repository inventory snapshot

#### HEAD baseline

- Branch: `codex-implementation`
- HEAD: `7cc890ee1d0805df5ae14b633127fade4f978639`
- HEAD 시각/제목: `2026-07-29 00:19:42 +0900`, `deprecated`
- HEAD tree: root `pom.xml` 1개, `src/main/java` 6개, `src/test/java` 1개
- 의미: Target §5.1이 설명한 single-JAR placeholder inventory와 일치한다.

#### 미커밋 live drift

- Root `pom.xml`은 이미 `packaging=pom` parent/aggregator이며 `rpdptw`, `build`, `legacy`를 module로 선언한다.
- `pom.xml`은 총 15개다. `rpdptw` 아래 core/solver/verification/application/capabilities/profile-catalog module, `build` 아래 architecture-rules/test-fixtures, `legacy/gcp-placeholder`가 실제로 존재한다.
- `rpdptw` target namespace의 main Java 23개는 모두 `package-info.java`이며 Phase 01/02 production type은 0개다.
- `build`에는 architecture/test-fixture test Java 9개와 검증 script가 존재하고, `legacy`에는 main/test Java 15개가 존재한다.
- 기존 root `src/**` 6/1 파일은 live tree에서 삭제 상태이고 legacy module 아래로 이동·확장된 내용이 untracked 상태다.
- `docs/implementation/execution-progress-and-results.md` §10.1은 Phase 00을 `PREREQUISITE_REMEDIATION_IN_PROGRESS / NOT_ACCEPTED`, Phase 02를 `BLOCKED_BY_ENTRY_GATES / NOT_ACCEPTED`로 기록한다.
- `human-guides/` 전체는 untracked다. 이 review는 그 밖의 사용자 소유 변경을 보존했다.

Live reactor와 package skeleton의 존재는 Phase 00 acceptance가 아니다. 반대로 그 상태를 “module 없음”으로 계속 설명해도 안 된다. 현재 정직한 판정은 “미커밋 Phase 00 remediation artifact는 존재하지만 evidence/review/receipt가 없어 unaccepted”다.

## 2. Review 방법과 기준

1. Target 전체 1,271행을 section별로 읽고 canonical Phase 02와 독립 review correction을 역추적했다.
2. Canonical Master, Final Domain/Architecture, Integrated Design, question register, realization plan, implementation README/progress를 exact heading 단위로 대조했다.
3. Phase 01과 Phase 03의 canonical/human handoff를 양방향으로 대조해 producer/consumer ownership, identity, lifecycle과 state를 확인했다.
4. `git ls-tree HEAD`, `git status --short --untracked-files=all`, live POM/module/package inventory를 비교해 HEAD baseline과 live drift를 분리했다.
5. Proposed Java type/signature를 Phase 01 value type, Maven module DAG, 현재 POM의 test-fixture edge와 비교했다.
6. 각 WP의 사전조건, 예상 artifact, 행동, command, pass 판정, failure 해석, rollback, handoff가 실제로 닫히는지 확인했다.
7. Test fixture/builder/oracle, red→green, sensitivity, false-green과 exact Surefire method 판정을 확인했다.
8. OPEN/GATED/deferred, Phase 13의 Phase 14A + `C-17` gate와 Phase 14B production authority가 숨은 기본값으로 닫히지 않는지 확인했다.
9. Target의 local Markdown link 62개와 fragment 38개를 실제 path/heading/explicit anchor에 대조했다.
10. Target만 대상으로 heading/fence/중복 anchor/trailing whitespace/tab/NUL 및 `git diff --check`를 검사했다.

Severity 기준:

- `CRITICAL`: 안전·authority·publication을 즉시 우회하거나 구현 결과를 근본적으로 오염시키는 결함
- `HIGH`: 구현자가 잘못된 구조/identity/명령을 따라 중대한 재작업·false acceptance·불변조건 위반을 만들 수 있는 결함
- `MEDIUM`: 중요한 boundary/test/type 계약이 빠져 valid/invalid 판정 또는 재현성이 흔들리는 결함
- `LOW`: 의미 변경 가능성은 낮지만 문서 정확성·추적성을 떨어뜨리는 결함

## 3. Finding summary

| ID | Severity | 요약 | Target 수정 |
|---|---|---|---|
| `HG-P02-R01` | HIGH | HEAD snapshot을 live current inventory처럼 제시해 실제 Phase 00 remediation과 source drift를 누락 | YES |
| `HG-P02-R02` | HIGH | Delivery-only logical pickup이 physical-location node인 것처럼 읽히고 OPEN representation을 사실상 닫음 | YES |
| `HG-P02-R03` | HIGH | WP Maven/test-fixture/evidence command가 실제 reactor에서 재현 가능한 판정으로 닫히지 않음 | YES |
| `HG-P02-R04` | MEDIUM | Missing-coordinate rejection과 non-self zero 허용이 필수 test/exit/traceability에서 빠짐 | YES |
| `HG-P02-R05` | MEDIUM | `SolverRequest`가 Phase 01의 weight/volume unit type을 raw `long`으로 약화 | YES |

Counts: `CRITICAL=0`, `HIGH=3`, `MEDIUM=2`, `LOW=0`.

## 4. Findings

### HG-P02-R01 — HIGH — Current inventory와 source fingerprint가 live checkout을 반영하지 않는다

- **Finding:** Target은 조사 commit의 single-project 상태를 현재 실제 checkout처럼 반복한다. 그러나 review 시점 live tree에는 root aggregator, 15개 POM, target module/package skeleton, build/architecture tests와 legacy boundary가 이미 존재한다. Phase 00은 여전히 unaccepted지만 “reactor/module 없음”, “main 6/test 1”, “현재 root placeholder test 1건”이라는 문장은 현재 사실이 아니다. Target이 기록한 `execution-progress-and-results.md` blob `250aa...`와 root POM blob `f8a411...`도 각각 현재 `3e9dcd...`, `1dc675...`로 drift했다.
- **사람에게 미치는 영향:** 구현자는 진행 중인 사용자 소유 Phase 00 artifact를 보지 못하고 같은 module/tree를 다시 만들거나, 실제로 존재하는 module command를 “지금 실패해야 하는 future-red”로 오판하거나, active remediation을 안전한 rollback 대상으로 잘못 취급할 수 있다. 반대로 unaccepted skeleton을 accepted Phase 00으로 오인할 위험도 있다.
- **Target 위치/anchor:** metadata §1.1 lines 42, 47; §4.2 lines 218~242; §5.1 lines 258~280; §9 서문 line 762; WP-02.6 lines 920~929; §10.4 lines 1017~1022.
- **Source evidence:**
  - `docs/implementation/execution-progress-and-results.md` §5 “구현 task registry”와 §10.1 “Phase 00 prerequisite remediation”
  - Live root `pom.xml` `<modules>`와 `rpdptw/pom.xml` `<modules>`
  - `rpdptw/core/src/main/java/com/ronext/rpdptw/{domain,travel}/package-info.java`
  - HEAD `7cc890...`의 `pom.xml`, `src/main/java`, `src/test/java`
  - Target §1.1 자체 규칙: fingerprint가 달라지면 관련 section과 requirement/test/evidence 영향을 다시 review
- **Root cause:** 작성 시점 HEAD snapshot과 이후 같은 checkout의 미커밋 Phase 00 구현 drift를 한 inventory 축으로 다뤘고, review 시작 전 source blob drift 규칙을 재적용하지 않았다.
- **Required correction:** Target metadata와 §4~§5를 `HEAD baseline`과 `live uncommitted drift` 두 표로 나눈다. Live reactor/module/package/build-test 존재와 Phase 00 `IN_PROGRESS / NOT_ACCEPTED`를 함께 기록한다. “없다”를 “skeleton은 존재하지만 Phase 01/02 production type/evidence/receipt는 없다”로 고친다. Current POM/progress fingerprints와 read-only inventory command 결과를 갱신하고, implementation 시작 시 재-snapshot하는 stop rule을 둔다. Live artifact를 수정·삭제하거나 Phase 00 acceptance로 승격하지 않는다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** Phase 00 작업은 review 뒤에도 변할 수 있다. 구현 시작 직전 exact accepted receipt와 current inventory를 다시 확인해야 하며, 이 review의 live snapshot 자체도 acceptance authority가 아니다.

### HG-P02-R02 — HIGH — Delivery-only logical pickup의 물리 identity를 잘못 가르칠 수 있다

- **Finding:** Target §3.1의 `L7` 예시는 “delivery-only logical pickup owner”를 `PickupNode(P17) → PhysicalLocationId(L7)` 그림과 같은 문맥에 둔다. §3.3은 모든 request가 정확한 pickup/delivery node reference를 갖는다고 고정하고, §8.6의 `SolverRequest`는 service pattern과 무관하게 필수 `pickupNodeId`를 요구한다. WP-02.5의 `request pair/node kind → node→location` 순서도 logical pickup 제외를 명시하지 않는다. 이는 이후 §3.2의 “logical pickup은 travel/stop/service visit을 만들지 않는다”와 내부적으로 충돌하며, `P-02`의 prefix-node 대 explicit-initial-load 표현을 사실상 한 방향으로 닫는다.
- **사람에게 미치는 영향:** 신규 구현자가 delivery-only request마다 depot physical pickup node/location을 생성해 location count와 `M²`를 오염시키거나, 가짜 travel/stop/service/window/zone을 만들 수 있다. Phase 03의 initial load와 stop/resource oracle도 잘못된다.
- **Target 위치/anchor:** §3.1 lines 145~153; §3.2 lines 155~160; §3.3 lines 172~184; §8.6 lines 661~699; §8.7 lines 724~751; WP-02.5 line 888.
- **Source evidence:**
  - `docs/master-design.md` §5.2 “Service meaning”: logical pickup은 travel, stop, depot revisit을 만들지 않으며 내부 표현은 `P-02` 잠정
  - `docs/2026-07-26-domain-design.md` §2.3 “Delivery-only와 real pickup-delivery”: `PickupSemantics.LogicalInitialLoad` 대 `PhysicalService`, 가짜 depot visit 금지
  - `docs/implementation/phases/phase-01-canonical-input-normalization.md` §7.2: `PickupInput.LogicalInitialLoad`는 location/travel/stop/depot service node를 만들지 않음
  - `docs/architecture-domain-implementation-design.md` §6.4 “Dense identity와 immutable problem”: delivery-only logical pickup은 travel/stop/customer service node를 만들지 않음
  - 인접 `phase-03-human-implementation-guide.md` §4.4: logical pickup은 visit가 아니고 initial-load ownership에만 참여
- **Root cause:** “request pair identity는 유지한다”와 “physical solver node/visit을 만든다”를 구분하지 않았고, 상위 문서에 남은 representation OPEN을 mandatory node signature로 축약했다.
- **Required correction:** `L7` 예시에서 delivery-only logical pickup을 physical-location mapping 그림에서 제거하고 real pickup/delivery/terminal만 그린다. Proposed model은 `PickupSemantics.LogicalInitialLoad`와 `PhysicalService(SolverNodeId)`처럼 두 의미를 분리하거나, logical identity를 유지하더라도 physical node/location/travel/visit collection에서 제외됨을 명시한다. “모든 solver node의 location mapping”은 실제 physical service/terminal node에만 적용된다는 해석을 닫고, logical pickup이 `M`, `M²`, stop, service, zone, travel call count를 늘리지 않는 contract/property test를 추가한다. 구체 내부 표현은 `PROPOSED/OPEN`으로 유지한다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** Canonical 자료에도 항상 `pickupNodeId`를 보이는 skeletal 예가 있어 표현 drift 가능성이 남는다. Phase 01~03 owner와 Architecture owner가 하나의 internal contract를 승인하기 전 public API로 고정하면 안 된다.

### HG-P02-R03 — HIGH — WP command와 test-fixture dependency가 재현 가능한 pass 판정으로 닫히지 않는다

- **Finding:** WP-02.1~5의 targeted Maven command는 canonical Phase 02의 `-am`을 빼고 `rpdptw/core`만 실행한다. Target은 동시에 `build/test-fixtures`의 builder/oracle/corruption helper를 예상하지만, live `build/test-fixtures/pom.xml`은 이미 `rpdptw-core`를 test-scope로 의존한다. 따라서 core test가 그 fixture artifact를 다시 의존하면 cycle이고, 의존하지 않으면 Target이 제시한 fixture가 core command에서 compile/run되지 않는다. 또한 WP-02.0 command는 실제 ADR/approval/evidence plan이 아니라 canonical Phase 문서의 non-empty/whitespace만 검사한다. WP-02.6은 “exact required method manifest”를 요구하지만 required method 집합과 XML 판정 command를 정의하지 않아 class 하나 또는 stale report로도 사람이 잘못 판정할 수 있다.
- **사람에게 미치는 영향:** Fresh checkout에서는 sibling artifact 미해결 또는 cycle로 실패하고, 개발자 로컬 Maven repository에 stale artifact가 있으면 잘못 통과할 수 있다. Required method 누락·duplicate·skipped를 문장으로만 금지해 false-green을 자동 차단하지 못한다. WP-02.0은 gate evidence가 없어도 항상 green에 가까운 명령이다.
- **Target 위치/anchor:** 목표 tree lines 286~313; WP-02.0 lines 764~780; WP-02.1 command lines 798~803; WP-02.2 lines 821~825; WP-02.3 lines 843~847; WP-02.4 lines 873~877; WP-02.5 lines 897~901; WP-02.6 lines 903~929; §10.2~§10.4 lines 947~1026.
- **Source evidence:**
  - `docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md` §9 WP-02-1~5: canonical targeted command는 `mvn -pl rpdptw/core -am ...`
  - `docs/implementation/master-realization-plan.md` §8.2: positive/negative/boundary/oracle/architecture/fault/corruption/reproducibility별 완료 판정
  - 같은 plan §9.1: exact command, toolchain, exit code, test result digest와 required evidence key를 pre-review manifest에 봉인
  - Live `build/test-fixtures/pom.xml` dependencies: test-scope `rpdptw-core`
  - Live `rpdptw/core/pom.xml`: 현재 Phase 02 test dependency 없음
  - Live root `pom.xml`: Surefire의 module-level `failIfNoTests=false`
- **Root cause:** Canonical future command와 proposed tree를 실제 reactor/test dependency 방향에 맞춰 통합하지 않았고, “사람이 나중에 exact manifest를 확인한다”는 서술을 executable oracle로 바꾸지 않았다.
- **Required correction:** Phase 00 acceptance 뒤 사용할 exact wrapper/Maven invocation과 reactor edge를 먼저 확정한다. Core unit fixture는 `rpdptw/core/src/test`에 두고 cross-module fixture는 downstream consumer만 쓰거나, cycle 없는 별도 pure fixture edge를 Architecture review로 승인한다. 단순히 current 양방향 dependency를 만들면 안 된다. 필요한 command에는 승인된 reactor 관계에 맞는 `-am`/module list와 fresh `clean` 범위를 포함한다. WP-02.0은 task/receipt/policy/ADR/evidence-plan의 실제 경로와 digest를 검사해야 한다. Required class/method를 machine-readable manifest로 고정하고 fresh Surefire XML에서 tests/failures/errors/skipped 및 missing/duplicate method를 계산하는 command를 제공한다. Class가 없거나 0건이면 non-zero여야 한다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** Phase 00가 아직 accepted가 아니므로 최종 command와 fixture edge는 지금 확정할 수 없다. 그 전에는 future command를 실행 성공 evidence로 사용할 수 없고, Target 수정 뒤에도 Phase 00 receipt를 entry에서 다시 검증해야 한다.

### HG-P02-R04 — MEDIUM — 두 필수 travel boundary가 required test에서 빠졌다

- **Finding:** Target pseudocode는 missing `D`에서 coordinate를 요구하지만 test table, exit checklist와 traceability에는 `rejectsMissingDistanceWhenCoordinateIsAbsent()`가 없다. 또한 canonical Phase 02가 명시한 “non-self zero는 co-located 의미일 수 있으므로 상위 계약 없이 양수를 강제하지 않는다”가 Target 설명·value contract·test에서 빠졌다. 현재 method 목록은 “대표 후보”라서 이 두 동작이 없어도 exact required manifest를 구성할 수 있다.
- **사람에게 미치는 영향:** 구현자가 좌표 없는 missing distance를 `0`, reverse, cache 또는 임의 function으로 보완하거나 늦은 runtime failure로 넘길 수 있다. 반대로 서로 다른 logical location ID의 valid zero distance/time을 invalid로 거부할 수 있다. 양쪽 모두 generic coverage test만으로는 false-green이 가능하다.
- **Target 위치/anchor:** §3.4 lines 188~199; §8.4 lines 576~613; pseudocode lines 724~758; WP-02.3 lines 827~855; §10.2 lines 949~995; exit checklist lines 1132~1144; traceability lines 1238~1244.
- **Source evidence:**
  - `docs/master-design.md` §8 item 6: missing `D`인데 필요한 coordinate가 없으면 solve 전 input error
  - `docs/2026-07-26-domain-design.md` §6 “Travel preparation” lines 625~632: coordinate required, no library default
  - `docs/implementation/phases/phase-02-prepared-travel-immutable-problem.md` §7.2: non-self zero 허용, 음수/overflow만 거부
  - 같은 Phase 02 §14 `REQ-P02-D-GEN`: exact test `rejectsMissingDistanceWhenCoordinateIsAbsent`
- **Root cause:** Happy-path generation과 rounding sensitivity에 집중하면서 prerequisite absence와 co-located boundary를 required oracle 목록에 올리지 않았다.
- **Required correction:** `rejectsMissingDistanceWhenCoordinateIsAbsent()`를 required method manifest, WP-02.3, exit checklist와 traceability에 추가하고 generator/cache/reverse 호출 0 및 no-partial-publication을 oracle로 둔다. 별도 non-self co-located fixture로 zero `D/U`가 상위 source contract상 valid할 때 보존되는지, negative만 reject되는지 검사한다. “대표 후보”와 “exit-required” method를 구분해 후자를 완전한 목록으로 만든다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** 승인된 Great Circle function/reference vector가 아직 없어 numeric success path는 계속 blocked다. Missing-coordinate rejection과 zero boundary는 임의 Earth model 없이도 먼저 red/green으로 검증할 수 있다.

### HG-P02-R05 — MEDIUM — Phase 01의 unit-bearing demand type이 raw long으로 약화된다

- **Finding:** Target §8.6의 `SolverRequest`는 `long demandWeight`, `long demandVolume`을 사용한다. 인접 Phase 01 contract는 `MilliKilograms`와 `MilliCubicMeters`로 scale 3/FLOOR를 봉인하고 Phase 02가 이를 재해석하지 않도록 한다. 이름 없는 raw `long`은 unit swap, raw kg/CBM 재주입 또는 Phase 02 재정규화를 compile-time에 막지 못한다.
- **사람에게 미치는 영향:** Java 구현자는 ID에는 type-specific wrapper를 쓰면서도 weight/volume은 같은 primitive로 섞어 capacity/reference를 잘못 비교할 수 있다. Phase 01의 item-first/scale/rounding provenance가 ProblemInstance 경계에서 희석된다.
- **Target 위치/anchor:** §8.6 lines 663~672; WP-02.5 lines 881~895; exit checklist lines 1141~1144.
- **Source evidence:**
  - `docs/master-design.md` §7.2: weight/volume `n=3/FLOOR`, 같은 차원 demand/capacity의 unit/scale 일치
  - `docs/implementation/phases/phase-01-canonical-input-normalization.md` §7.3 “Numeric value”: `MilliKilograms`, `MilliCubicMeters`
  - 인접 `phase-01-human-implementation-guide.md` §10.4 “Value object 후보”: 같은 두 type
  - `docs/2026-07-26-architecture-design.md` §2.4 및 Integrated Design §4.3 package rule: immutable domain value와 typed boundary
- **Root cause:** Final Domain의 설명용 raw-long pseudo record를 복사하면서 Phase 01이 이미 제안한 unit-bearing handoff type과 연결하지 않았다.
- **Required correction:** `SolverRequest`와 vehicle capacity가 Phase 01의 동일 unit-bearing immutable type을 재사용하거나 의미상 동등한 reviewed type을 사용하도록 고친다. 이름이 아직 proposed라면 최소 `demandWeightMilliKg`/`demandVolumeMilliCbm`과 exact type mapping을 명시한다. Phase 02가 scale/rounding을 다시 수행하지 않는 compile/contract test와 weight↔volume swap 방지 test를 추가한다.
- **Target 수정 필요 여부:** **YES**
- **Residual risk:** 최종 Java type 이름과 visibility는 여전히 proposed다. 다만 unit/scale/rounding identity는 OPEN이 아니므로 type 이름 변경이 primitive 혼용의 근거가 될 수 없다.

## 5. 검사 축별 판정과 residual risk

| 검사 | 판정과 근거 | Residual risk |
|---|---|---|
| 1. 원문 의미·불변조건·identity/lifecycle·Phase 경계 | `HG-P02-R02`, `R04`, `R05` 수정 필요. 나머지 directed key, totality, no-fallback, immutable freeze, route-state non-scope는 Target §2~§8과 Master §4~§8에 일치 | Delivery-only representation과 missing-coordinate/zero boundary 수정 전 구현 drift 가능 |
| 2. 신규 Java/CVRPTW 구현자 교육성 | 큰 그림·용어·producer/consumer·학습 단계는 충분하다. 다만 `R01`, `R02`, `R05`가 실제 checkout과 domain mental model을 왜곡 | Proposed 이름을 public/accepted API로 오인할 수 있어 checkpoint 유지 필요 |
| 3. 명령/WP 실행·판정 가능성 | 각 WP에 목적/사전조건/행동/기대/failure/rollback/handoff가 있는 점은 좋지만 `R03` 때문에 command와 exact pass oracle이 닫히지 않음 | Phase 00 acceptance 전 최종 module command 자체는 조건부 |
| 4. Java/Maven 구조·signature/dependency | Package/provider isolation은 맞다. Live reactor를 읽으면 `R01`, test-fixture edge는 `R03`, unit type은 `R05` 수정 필요 | 현재 target namespace는 package-info뿐이며 API evidence 0 |
| 5. 완성 코드 복붙 대 추상성 | Skeletal snippet이며 완성 구현을 가장하지 않는다. 설명·선택 근거·anti-pattern이 있어 과도한 복붙은 아님 | Skeleton이 많은 만큼 OPEN signature를 그대로 public화할 위험 |
| 6. Entry/exit/evidence/rollback/failure/security/observability/reproducibility | Target §4, §9~§14가 gate, all-or-nothing, redaction, safe aggregate, evidence DAG, last-safe-point를 명시해 PASS | 실제 artifact/evidence/review/receipt는 전부 미생성 |
| 7. OPEN/GATED/deferred/EXPERIMENT_REQUIRED | Target §6.3~§6.4와 §13.4가 Great Circle, fingerprint, performance, `Q-BENCH-02`, `Q-VAR-01`을 hidden default로 닫지 않아 PASS | Owner approval 전 production green 금지 지속 |
| 8. Phase 13/14 우회 | Target §2.2, §6.3, §12, §15가 Phase 14A receipt + `C-17`, Phase 14B official/production authority를 분리해 PASS | Optional branch 문서 존재가 dependency 추가 권한이 아님 |
| 9. 인접 Phase ownership/dependency/lifecycle/state | Phase 01 단일 immutable source와 Phase 03 read-only consumer/no fallback은 일치. Delivery-only representation만 `R02` | Phase 01/02/03 exact internal type contract는 아직 승인 전 |
| 10. 상대 링크/GFM fragment/fingerprint/traceability/용어 | Link 62/fragment 38 모두 실제 target/anchor 존재. `R01`의 stale working fingerprint만 수정 필요 | Renderer별 Unicode anchor 차이는 local checker를 계속 사용 |
| 11. 사람 결정·승인과 마지막 안전 지점 | Target §4.3, §6.4, §11.1이 결정권자·stop/resume·last safe point를 구분해 PASS | Scheduler/owner/task와 approvals는 아직 없음 |
| 12. 문서 완료와 구현 완료·legacy 11-phase 혼동 | Metadata와 §13.4가 `NOT_STARTED/NOT_ACCEPTED`, 15 Phase, scheduler-only transition을 명시. Legacy 11-phase 복사 없음 | Live Phase 00 skeleton 존재를 acceptance로 오인하지 않아야 함 |
| 13. Test fixture/builder/oracle/red→green/false-green/category/pass | Independent oracle, sensitivity, category 표는 강점. `R03` exact execution과 `R04` required boundary 누락 수정 필요 | 공식 snapshot/benchmark oracle은 별도 authority 전 사용할 수 없음 |

## 6. Target와 무관하게 남은 implementation blockers

다음은 Target correction finding과 별개인 실제 구현 blocker다. 이 review가 해제하거나 승인하지 않는다.

| Blocker | 현재 상태 | Last safe point | Restart condition |
|---|---|---|---|
| Phase 00 reactor/architecture acceptance | Live remediation artifact는 있으나 evidence/review/receipt 없음 | HEAD baseline + 보존된 live worktree | `E-P00-*`, independent review, acceptance receipt, scheduler transition |
| Phase 01 accepted handoff | Production type/test/evidence 없음 | Read-only source와 Phase 00 accepted artifact | `E-P01-*`, immutable `NormalizedInputArtifact`, accepted receipt |
| Great Circle exact function/version | 미승인 | Provided-complete/test-only contract review | Earth model/precision/reference vectors와 owner approval |
| Typed source/generation policy | 미승인 | Test-only hand oracle | Allowlist/priority/declared absence/no-fallback approval |
| Phase 02 implementation/evidence | Production type 0, `E-P02-*` 0 | 이 guide/review와 accepted predecessor | Entry gate + code/test/evidence + independent review/receipt |

`win_poc_case_floor.json`, current target JAR, documentation review 또는 root build 성공은 위 blocker를 대신하지 않는다.

## 7. 링크·구조·whitespace 정적 검사

Target 파일 하나만 대상으로 검사했다.

| 검사 | 결과 | Evidence |
|---|---|---|
| Relative Markdown path | PASS | 검사 62개, missing 0 |
| GFM fragment/explicit anchor | PASS | 검사 38개, missing 0 |
| Fence | PASS | ` ``` ` line 58개로 짝수 |
| Non-fenced heading structure | PASS | heading 65개, level jump 0, duplicate base slug 0 |
| Trailing whitespace | PASS | `[[:blank:]]+$` match 0 |
| Tab | PASS | match 0 |
| NUL | PASS | 0 byte |
| `git diff --check -- <target>` | PASS | exit `0` |
| Untracked-content 보완 검사 | PASS | `git diff --no-index --check /dev/null <target>`의 whitespace diagnostic output 0. Exit `1`은 새 파일 전체가 diff이기 때문이며 whitespace error가 아님 |

Standard `git diff --check -- <target>`는 Target이 untracked여서 content를 열거하지 않는다. 그래서 no-index `--check` 출력과 직접 trailing-whitespace 검사를 함께 사용했다.

## 8. 최종 verdict

`CHANGES_REQUIRED`

Target 수정이 필요한 finding이 5개다. 특히 live inventory를 HEAD snapshot과 분리하고, delivery-only logical pickup의 non-physical 의미를 바로잡고, Maven/test-fixture/evidence command를 실제 reactor에서 재현 가능한 판정으로 닫기 전에는 사람용 구현 가이드를 accept할 수 없다.

이 review는 Target 문서의 품질 판정일 뿐 Phase 02 구현, evidence, 독립 implementation review 또는 acceptance receipt가 아니다. Target은 수정하지 않았다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=3 MEDIUM=2 LOW=0
REQUIRED_CORRECTION_FINDINGS: HG-P02-R01,HG-P02-R02,HG-P02-R03,HG-P02-R04,HG-P02-R05

## Correction 01 읽기 전용 재검증

### 재검증 metadata와 고정 입력

| 항목 | 값 |
|---|---|
| Recheck round | `01` |
| Recheck 시각 | `2026-07-29T02:45:35+09:00` (`Asia/Seoul`) |
| Reviewer task 성격 | 원 review 세션의 correction follow-up. 새 broad review나 implementation review가 아님 |
| 변경 허용 범위 | 이 review 파일 끝에 본 절 append만 허용 |
| Target | [phase-02-human-implementation-guide.md](../phases/phase-02-human-implementation-guide.md), 1,596행 |
| Target SHA-256 / Git blob | `123405df8823ef901e6a4f6ef8a77e18208965f40c25e3385cbdccc2e9f57ace` / `da1937c5bc600144789838e7636f7fda794e7878` |
| Correction report | [phase-02-correction-01.md](../corrections/phase-02-correction-01.md), 295행 |
| Correction report SHA-256 / Git blob | `cb032c16bcc2bf67c58ff0d036caab0dc84d4900502a6335a12c5dc4cb87d269` / `1489e2016cf4edca43a835ae9580a9b59392b23a` |
| Original review pre-append SHA-256 / Git blob | `dcf7de5ee30442b7c16e29dc60896d9b3b31de27afa1cebdc2c7a8cd3f083a72` / `c804f3c8e2ad9ec30166b7aca32af6abebaf5627` |
| HEAD | `7cc890ee1d0805df5ae14b633127fade4f978639` |

Correction report의 `5/5 ADDRESSED` 자기주장은 판정 근거로 사용하지 않았다. 위 target
bytes에서 각 original finding의 root cause와 required correction을 다시 추적했다.

읽은 의미·Phase·인접 문서의 SHA-256은 다음과 같다.

| Source | SHA-256 |
|---|---|
| [Master](../../../master-design.md) | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` |
| [Current Domain map](../../../domain-design.md) | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` |
| [Current Architecture map](../../../architecture-design.md) | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` |
| [2026-07-26 Domain](../../../2026-07-26-domain-design.md) | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` |
| [2026-07-26 Architecture](../../../2026-07-26-architecture-design.md) | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` |
| [Integrated design](../../../architecture-domain-implementation-design.md) | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` |
| [Open questions](../../../master-design-open-questions.md) | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` |
| [Master realization plan](../../master-realization-plan.md) | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` |
| [Implementation README](../../README.md) | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` |
| [Execution progress](../../execution-progress-and-results.md) | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c` |
| [Canonical Phase 01](../../phases/phase-01-canonical-input-normalization.md) | `67e078a058753335ae823bbec815b3628f212d4593dfce9ee0db39cc02000324` |
| [Canonical Phase 02](../../phases/phase-02-prepared-travel-immutable-problem.md) | `51d8491a701f88b218609ea32e9ec714710ff88291760c1dc63bfc27bba9f686` |
| [Canonical Phase 02 review](../../reviews/phase-02-review.md) | `7c25976b530bf42f470a01ccccd81e6732ffb577eebd9a0aef6cafeb19b343f5` |
| [Canonical Phase 03](../../phases/phase-03-route-propagation-evaluation-kernel.md) | `46945bd0b4c2d65d43ea078d5562e048133986983d11c5b62ecfb8a9f3bd7b54` |
| [Adjacent human Phase 01](../phases/phase-01-human-implementation-guide.md) | `142d4d70cc3e8dd3deee65762fc41f435e082a5a8266e6a2fbd4a816847c299e` |
| [Adjacent human Phase 03](../phases/phase-03-human-implementation-guide.md) | `f23c052b58974425217969f8be13b4561b0fe751ca1a9899a62cb6605356c3b3` |

Historical 자료는 cross-check로만 취급했고 current semantic/acceptance authority로 승격하지
않았다.

### Live inventory 재확인

Recheck 시점 live tree는 POM 13개, RPDPTW main Java 23개 전부
`package-info.java`, build test Java 12개, legacy Java 15개, core test Java 0개다.
Root/core/test-fixtures/architecture-rules POM SHA-256은 각각
`ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`,
`d8b6634ca1a38c1486550e0935058490f96694efd895fc566bbb5b8fa4baad69`,
`65a9fae30174778ade49302b2e8525419955792437498a7dd5be6649888aad71`,
`3f4ff694dc616651c739ef50b3b746ff11c23d1e9f23e3f5680d52eb6d5fbc14`다.
POM, RPDPTW/build/legacy/root Java와 progress 64개 파일을 `sorted path + NUL +
file SHA-256 + NUL`로 결합한 inventory manifest SHA-256은
`cca2c2b3b794d27f8acd427db25eb758d57c95119ffca6039747073cf6eb992c`다.

Target §5.1.2의 `2026-07-29T02:03:39+09:00` snapshot 뒤 progress blob은
`36afdf...`에서 `0419f69199b3140dd44020f78278b1352e6517b8`로 바뀌었고, Phase 00은
현재 `REVIEW_02_CHANGES_REQUIRED_FIX_02_IN_PROGRESS / NOT_ACCEPTED`다. Build test도
snapshot의 11개에서 12개로 바뀌었다. 이는 target §4.2 line 261과 §5.1 line 309가
명시한 재-snapshot stop rule을 실제로 발동시키는 live drift이며, target snapshot이나 이
recheck가 Phase 00 acceptance authority라는 뜻이 아니다.

### Finding별 재판정

#### HG-P02-R01 — RESOLVED

- **확인한 target anchor:** §1 metadata/§1.1 lines 11~60, §4.2 lines 239~263,
  §5.1 lines 279~309, §9 line 831, WP-02.0과 WP-02.6.
- **Source evidence:** Execution progress §5 “구현 task registry”와 §10.1
  “Phase 00 prerequisite remediation”; live root/reactor POM; HEAD
  `7cc890...`.
- **판정 근거:** HEAD single-JAR baseline과 timestamp가 있는 uncommitted live
  snapshot을 분리했고, skeleton 존재와 `NOT_ACCEPTED`, Phase 02 type/test/evidence 0을
  동시에 기록했다. Drift 시 구현을 중지하고 accepted receipt의 source snapshot과
  재대조하도록 했으며 live artifact 삭제·rollback·accept 권한도 부여하지 않았다.
- **남은 risk:** 위와 같이 recheck 시점에 이미 새 drift가 있다. 실제 구현자는 target의
  옛 snapshot을 사용하지 말고 Phase 00 accepted receipt가 생긴 뒤 다시 봉인해야 한다.

#### HG-P02-R02 — OPEN

- **닫힌 부분:** §3.1~§3.3 lines 155~198은
  `LogicalInitialLoad`를 physical location 그림에서 제거하고 pair ownership만 남겼다.
  §8.6 lines 708~758은 logical/physical pickup semantics를 분리하며, WP-02.5,
  `keepsLogicalInitialLoadOutOfPhysicalNodeLocationTravelAndVisitSets`, §13.2와 §15는
  node/location/travel/stop/service, `M`, `M²`와 generation call count 증가 0을
  연결한다.
- **남은 root cause:** Original finding의 required correction은 logical pickup 자체가
  **zone visit도 만들지 않음**을 contract/property test에 포함하도록 요구했다. Master
  §5.3 “Vehicle size와 capability”도 “Delivery-only의 depot 논리 pickup은 zone 방문이
  아니다”라고 명시한다. 그러나 corrected target에는 `zone`/`구역`이 한 번도 나타나지
  않으며, 기존 required method와 evidence가 이 결함을 판정하지 않는다. Physical
  identity 오해는 대부분 교정됐지만 required correction 전체는 닫히지 않았다.
- **Required correction:** §3 의미/invariant, §6 결정 상태, §8.6 freeze contract,
  WP-02.5, exit-required table/PSV, §13.2와 §15에 “logical pickup 자체는 zone visit이나
  zone-resource membership을 만들지 않는다”를 추가한다. Delivery node와 real physical
  pickup의 정상 zone 사실은 계속 보존하면서, delivery-only request 수를 바꿔도 logical
  pickup에서 새 zone visit이 생기지 않는 독립 oracle을 exact required method로 고정한다.
  Target은 이 recheck에서 수정하지 않았다.
- **남은 risk:** Prefix token 대 explicit initial-load state와 Java visibility는 계속
  `P-02 PROPOSED/OPEN`이다. 비물리·비-zone-visit 의미와 내부 표현 승인을 섞으면 안 된다.

#### HG-P02-R03 — OPEN

- **닫힌 부분:** §5.1~§5.2는 실제
  `build/test-fixtures --test→ rpdptw-core` 방향과 core-local helper ownership을 맞췄다.
  WP-02.0은 실제 path/digest 없으면 실패하고, WP-02.1~5는 accepted wrapper,
  `-am clean`, zero/specified-test strict option을 사용한다. §10.2 table과 PSV는
  58/58, unique 58, missing/extra/duplicate 0이며 checker Python 문법도 유효하다.
  Checker는 report/testcase 0, failure/error/skipped와 required method
  missing/duplicate를 non-zero로 만든다. Current POM에 Failsafe execution이 없다는
  discovery 제한도 정확히 기록했다.
- **남은 root cause:** WP-02.6 lines 1038~1055는 core-only
  `-pl rpdptw/core -am clean verify` 뒤에도 “각 clean run 직후 §10.2의 checker”를
  실행하라고 한다. 그러나 single PSV lines 1144~1203에는 core 53개뿐 아니라
  `build/architecture-rules` required method 5개도 들어 있고, checker는 manifest의 모든
  `(module, engine)` report directory를 무조건 요구한다. Core-only 첫 run은
  `build/architecture-rules/target/surefire-reports`를 만들지 않으므로 checker가
  `no fresh XML reports`로 반드시 실패한다. Run-local clean lifecycle과 full-exit
  manifest를 한 판정 단위로 합친 것이 원 command/evidence root cause를 완전히 닫지 못한
  이유다.
- **Source evidence:** Current Architecture §19.1 “Reactor build order”, Master
  Realization Plan §8.2 “필수 test 종류”와 §9.1 “Pre-review evidence manifest”,
  canonical Phase 02 WP-02-1~6/§10, live core/test-fixtures/architecture-rules POM.
- **Required correction:** Core run에는 core 53개만 담은 reviewed run-scoped manifest
  또는 명시적 module filter를 사용하고, architecture `-am`/full root run에는 58개
  full-exit manifest를 사용하도록 command와 checker interface를 실행 가능하게 맞춘다.
  각 run의 report freshness, zero-test/failure/skipped/missing/duplicate 판정과 immutable
  run identity는 유지하며, final exit에서는 full 58개를 약화하지 않는다. Target은 이
  recheck에서 수정하지 않았다.
- **남은 risk:** Phase 00이 아직 accepted가 아니므로 wrapper/DAG는 계속 future
  conditional이다. Maven/Java test는 scope와 entry gate상 실행하지 않았고, 문서의
  checker syntax 검증은 실제 test execution evidence가 아니다.

#### HG-P02-R04 — RESOLVED

- **확인한 target anchor:** §3.4 lines 202~218, §8.7, WP-02.3 lines 929~970,
  §10.2 lines 1090~1093와 PSV lines 1157~1160, §13.2 lines 1448~1452, §15의
  `REQ-P02-D-GEN`/non-self-zero traceability.
- **Source evidence:** Master §8 “Directed distance/time matrix 계약”, 2026-07-26
  Domain §6 “Travel preparation”, canonical Phase 02 §7.2 “Absence semantics”와
  §14 `REQ-P02-D-GEN`.
- **판정 근거:** Missing coordinate는 generator/cache/reverse 호출과 partial publication
  없이 reject되고, valid provided non-self zero 보존과 negative rejection이 separate
  exit-required method, WP subset, sensitivity, exit와 evidence에 연결됐다.
- **남은 risk:** Approved Great Circle function/version/reference vector가 없으므로
  generated-distance numeric green과 Phase exit는 계속 blocked다. Boundary subset
  해결이 그 authority를 대신하지 않는다.

#### HG-P02-R05 — RESOLVED

- **확인한 target anchor:** §6.3 line 409, §8.6 lines 708~758, WP-02.5
  lines 999~1020, §10.2 lines 1121~1136와 PSV lines 1188~1203, §11.2 line 1371,
  §13.2 line 1456, §15 unit traceability.
- **Source evidence:** Master §7.2 “Fixed-point와 checked arithmetic”, canonical
  Phase 01 §7.3 “Numeric value”, 2026-07-26 Architecture §2.4 “먼저 알아야 할
  immutable artifact”, adjacent human Phase 01의 value object/handoff 계약.
- **판정 근거:** `SolverRequest` 후보가 `MilliKilograms`와 `MilliCubicMeters`를
  사용하고 vehicle capacity에도 같은 차원의 type을 요구한다. Phase 02 renormalization
  0, exact value 보존, primitive/unit swap 방지와 compiled-signature test가
  WP/PSV/evidence/exit에 연결됐다.
- **남은 risk:** Exact Java type 이름과 visibility는 proposed이며 current production
  type/test는 0개다. Reviewed equivalent mapping 없이 raw primitive로 바꾸거나 문서
  contract를 구현 완료로 오인할 수 없다.

### 정적 link/GFM/fence/whitespace/manifest 재검증

| 검사 | 결과 | Evidence/해석 |
|---|---|---|
| Target relative links/GFM fragments | `PASS` | Local links 84개, fragments 55개, missing 0 |
| Correction report relative links/GFM fragments | `PASS` | Local links 28개, fragments 15개, missing 0 |
| Non-fenced heading structure | `PASS` | Target heading 67개, correction heading 14개; level jump 0, duplicate base slug 0 |
| Fence | `PASS` | Target fence line 64개로 짝수, correction report 0개 |
| Target whitespace | `PASS` | Trailing whitespace/tab/NUL 0, newline EOF |
| Correction report whitespace | `PASS` | Trailing whitespace/tab/NUL 0, newline EOF |
| Table ↔ PSV | `PASS` | 58/58, unique 58, missing/extra/duplicate 0 |
| Embedded XML checker syntax | `PASS` | Python compile 성공 |
| Maven discovery semantics | `PARTIAL` | Surefire-only/current Failsafe 미구성 설명은 정확하나 `HG-P02-R03`의 run-scoped manifest 순서 결함 존재 |
| `git diff --check -- <target>` | `PASS` | Exit `0`; target은 untracked라 no-index/direct 검사로 보완 |
| `git diff --no-index --check /dev/null <target>` | `PASS` | Whitespace diagnostic 0; exit `1`은 전체 새 content diff라 expected |
| 구현/test 실행 | `NOT_RUN_BY_DESIGN` | Entry gate 미충족, Phase 02 production/test 0, live Phase 00 remediation은 다른 owner scope |

### Recheck verdict

`FURTHER_CORRECTION_REQUIRED`

`HG-P02-R01`, `R04`, `R05`는 original root cause와 required correction이 현재 target에서
닫혔다. `HG-P02-R02`는 logical pickup의 zone-visit 제외가 빠졌고, `HG-P02-R03`은 첫
core-only clean run과 full 58-method checker가 함께 성공할 수 없어 계속 target 수정이
필요하다. 이 판정은 문서 correction의 재검증이며 Phase 02 구현·evidence·acceptance나
Phase 00 live artifact의 승인/rollback이 아니다. Target, correction report, POM, Java,
test, README/progress와 다른 review는 수정하지 않았다.

RECHECK_ROUND: 01
RECHECK_VERDICT: FURTHER_CORRECTION_REQUIRED
RESOLVED_FINDINGS: HG-P02-R01,HG-P02-R04,HG-P02-R05
OPEN_FINDINGS: HG-P02-R02,HG-P02-R03
TARGET_HASH_RECHECKED: 123405df8823ef901e6a4f6ef8a77e18208965f40c25e3385cbdccc2e9f57ace
CORRECTION_REPORT_HASH_RECHECKED: cb032c16bcc2bf67c58ff0d036caab0dc84d4900502a6335a12c5dc4cb87d269

## Correction 02 읽기 전용 재검증

### 재검증 metadata와 범위

| 항목 | 값 |
|---|---|
| Recheck round | `02` |
| Recheck 시각 | `2026-07-29T03:02:00+09:00` (`Asia/Seoul`) |
| Task 성격 | Correction 01 recheck에서 OPEN이던 `HG-P02-R02`, `HG-P02-R03`만 재검증한 원 reviewer follow-up |
| Target | [phase-02-human-implementation-guide.md](../phases/phase-02-human-implementation-guide.md), 1,803행 |
| Target SHA-256 / Git blob | `eabd5e029772bbaefa39073835dcb205f19fef09af9252642c442a00c172052e` / `eaa8a02026c17db085344bfb8e2a007624921d9b` |
| Correction report | [phase-02-correction-02.md](../corrections/phase-02-correction-02.md), 207행 |
| Correction report SHA-256 / Git blob | `4a19f9c31d69f5f5a00656fecef20114d72f1621f0c9fd2f2101ea4906fb61c7` / `e1cb6f8cdcac3909cb2d43fd21a3b5447cf4f297` |
| Review pre-append SHA-256 / Git blob | `722fa749ec10b66ac7850e414c2536339aa8aa709f1a980c9f9c6c059e5d8193` / `85b1e78e11ca5b8b8bbbfc9ebc989ff406d1cb64` |
| HEAD | `7cc890ee1d0805df5ae14b633127fade4f978639` |
| 변경 범위 | 이 review 끝의 본 절 append만 수행. Target/report/POM/Java/test/README/progress는 read-only |

Correction report의 `ADDRESSED`와 synthetic-control 표는 자기주장으로만 취급했다. Target의
실제 문장, exact table/PSV, embedded checker code와 live Maven DAG를 직접 대조했다.
주요 source SHA-256은 Master
`e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd`,
Current Architecture
`fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201`,
Master Realization Plan
`940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d`,
canonical Phase 02
`51d8491a701f88b218609ea32e9ec714710ff88291760c1dc63bfc27bba9f686`다.

### HG-P02-R02 — RESOLVED

- **확인한 target anchor:** §3.2~§3.3 lines 172~195, §6.3~§6.4 lines
  411/429, §8.6 lines 751/760~761, WP-02.5 lines 1010/1014, §10.2 table
  line 1179와 oracle 설명 lines 1202~1209, PSV line 1255, evidence lines
  1565~1566, exit lines 1660~1661, traceability line 1770.
- **Source evidence:** Master §5.2 “Service meaning”와 §5.3 “Vehicle size와
  capability”. 특히 delivery-only depot logical pickup은 zone 방문이 아니라는
  line 529와 대조했다.
- **Closure 판정:** Logical pickup 자체가 zone fact를 읽거나 합성하지 않고 zone
  visit/zone-resource membership에 원소를 추가하지 않는다는 계약이 identity,
  freeze, WP, evidence, exit와 traceability에 모두 연결됐다. 반대 방향으로 delivery
  node와 real physical pickup의 정상 `zoneId`/zone-resource fact를 보존하고 `ALL`로
  덮어쓰지 않는 계약도 명시됐다.
- **Exact independent oracle:** Required method
  `keepsLogicalInitialLoadOutOfPhysicalAndZoneResourceSets`는 table과 PSV에 각각
  정확히 한 번 존재한다. Physical collection expected set과 zone expected set을
  서로 재사용하지 않고, delivery-only request-count 대조 fixture에서 logical-pickup
  zone-visit/membership delta를 각각 exact `0`으로, delivery/real-pickup zone fact를
  별도 expected physical fact set과 exact equality로 판정하도록 닫았다.
- **남은 risk:** 실제 Java/test는 아직 없고 prefix token 대 explicit initial-load
  representation과 visibility는 계속 `P-02 PROPOSED/OPEN`이다. 이 residual은
  비-zone-visit 의미나 정상 physical zone fact 보존을 다시 열지 않는다.

### HG-P02-R03 — RESOLVED

- **확인한 target anchor:** WP-02.6 lines 1027~1113, §10.2 full PSV
  lines 1196~1271, checker lines 1273~1473, negative controls lines
  1482~1492, evidence lines 1576~1582, exit lines 1671~1672와 traceability
  line 1781.
- **Maven/DAG 대조:** Current Architecture §19.1과 live POM에서 core가
  test-fixtures/architecture-rules보다 앞서고, architecture-rules가 core와
  test-fixtures test-jar를 downstream test scope로 소비한다. Core run은
  `-pl rpdptw/core -am`, architecture run은
  `-pl build/architecture-rules -am`, final run은 root `clean verify`로 구분돼
  correction 01의 첫-run 불가능 순서가 제거됐다.
- **Manifest 관계:** Table과 PSV는 58/58, unique 58, missing/extra/duplicate 0이다.
  Module count는 `rpdptw/core=53`, `build/architecture-rules=5`이고
  `core53 ⊂ full58`, 차집합이 exact architecture 5임을 checker가 먼저 강제한다.
- **Run-scoped 판정:** `--run-kind core`는 core 53개만 선택하며,
  `architecture`와 `root`는 full 58개와 exact equality여야 한다. Core report만
  있는 상태에서 full run kind를 사용하면 architecture report 부재로 non-zero이고,
  final root evidence에 core filter를 재사용할 수 없다.
- **Freshness와 pass oracle:** Run timestamp보다 오래된 XML, report 0,
  testcase 0, suite/testcase failure/error/skipped, required missing/duplicate와
  manifest duplicate는 모두 non-zero code path로 연결된다. Embedded checker
  192행은 독립 compile 검사에 통과했다.
- **Run identity와 final exit:** Run ID 형식/존재 여부를 검사하고 existing evidence
  directory overwrite를 거부한다. Manifest, selected report, toolchain과 source
  digest를 temporary directory에 모은 뒤 rename하며 `run.json`에 run kind,
  reviewed command ID, required count와 digest를 기록한다. Final exit는
  `runKind=root`, `phase02-root-clean-verify-v1`, `requiredCount=58`, unique immutable
  run identity를 요구하고 evidence/exit checklist가 이를 다시 확인한다.
- **남은 risk:** Checker와 PSV는 future implementation artifact이며 실제 Maven/Java
  test evidence가 아니다. 구현 review에서는 실제 committed checker/PSV digest,
  wrapper exit/log capture와 copied report digest가 봉인된 run identity와 일치하는지
  다시 확인해야 한다. Phase 00/01 entry acceptance가 없으므로 production
  implementation은 계속 blocked다.

### 정적·범위 재검증

| 검사 | 결과 | Evidence |
|---|---|---|
| Required table ↔ PSV | `PASS` | 58/58, unique 58, core 53, architecture 5, missing/extra/duplicate 0 |
| Zone required method | `PASS` | Table/PSV 동일 이름 각 1회, independent physical/zone oracle 설명 존재 |
| Checker syntax/code path | `PASS` | Python compile 성공; 53/58 selection, freshness, zero/non-pass/missing/duplicate, run identity와 final-root path 확인 |
| Target link/GFM | `PASS` | Local link 85, fragment 56, missing 0 |
| Correction report link/GFM | `PASS` | Local link 13, fragment 5, missing 0 |
| Heading/fence | `PASS` | Target 67 headings/64 fence lines, report 16 headings/0 fence; level jump·duplicate slug·open fence 0 |
| Whitespace | `PASS` | Target/report trailing whitespace, tab, NUL 0; newline EOF |
| Scoped `git diff --check` | `PASS` | Target/report/review exit `0`; untracked no-index `--check` whitespace diagnostic 0 |
| Maven/Java implementation test | `NOT_RUN_BY_DESIGN` | Entry gate 미충족, Phase 02 implementation/test 0, code/POM/test는 read-only scope |
| New correction regression | `NONE FOUND IN SCOPE` | R02/R03 closure 범위에서 새 target correction finding 없음 |

### Correction 02 recheck verdict

`ACCEPTED`

이번 round의 두 OPEN finding은 required correction이 target의 exact anchor와 executable
future contract에서 모두 닫혔다. Correction 01에서 RESOLVED된 R01/R04/R05는 다시
열지 않았으며 cumulative OPEN finding은 없다. 이 verdict는 사람용 guide correction
02의 문서 재검증 결과일 뿐 Phase 02 구현·evidence·acceptance receipt 또는 scheduler
transition이 아니다. Target과 correction report는 수정하지 않았다.

RECHECK_ROUND: 02
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: HG-P02-R02,HG-P02-R03
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: eabd5e029772bbaefa39073835dcb205f19fef09af9252642c442a00c172052e
CORRECTION_REPORT_HASH_RECHECKED: 4a19f9c31d69f5f5a00656fecef20114d72f1621f0c9fd2f2101ea4906fb61c7
