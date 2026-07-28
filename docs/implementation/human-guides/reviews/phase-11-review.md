# Phase 11 사람용 구현 가이드 독립 리뷰

```yaml
phase: "11"
review_type: INDEPENDENT_HUMAN_IMPLEMENTATION_GUIDE_REVIEW
reviewer_task_nature: author와 분리된 새 작업의 read-only target 검토
target: docs/implementation/human-guides/phases/phase-11-human-implementation-guide.md
target_git_hash_object: 1204cd4ed501921f5a4c0dc6edd1f8e70f6fa729
target_sha256: b38705bc9289b9b5d1c9159ce20cc7a6b4112c05515dcbcfd3c04ee5182c94ee
target_lines: 1535
target_tracked_at_head: false
head_baseline: 7cc890ee1d0805df5ae14b633127fade4f978639
head_branch: codex-implementation
inventory_snapshot_at: "2026-07-29T01:50:33+0900"
inventory_status_sha256_before_output: 7ff2b5b6c96f7a76c6b2e21e904ed1a7f1cfa3c18f11682221c63a537475a4fb
review_date: 2026-07-29
timezone: Asia/Seoul
verdict: CHANGES_REQUIRED
target_changes_required: true
finding_counts:
  critical: 0
  high: 3
  medium: 4
  low: 0
```

## 1. 결론

Target은 Phase 11을 “solver를 Lambda에 올리는 작업”이 아니라 Phase 09/10의
provider-neutral 의미를 S3, Step Functions와 Lambda에 보존하는 작업으로 정확히
설명한다. 특히 AWS 선택·구현·integration evidence·production authority를 네 축으로
분리하고, exact-key/CAS, declared completeness, same-state cancel/publication fence,
two-gate publication, deadline fail-closed, no-DB, Phase 13/14 gate와 단방향 evidence
DAG를 보존한 점은 좋다. 신규 독자를 위한 RPDPTW primer, 사람 승인, last safe point,
WP별 rollback과 failure 해석도 전반적으로 유용하다.

그러나 실제 구현자가 이 문서만으로 source를 고정하고 test/evidence를 판정하려면
7건의 교정이 필요하다.

1. 현재 repository map과 사용자 지시의 canonical Domain/Architecture 대신 날짜
   문서를 authority로 고정했다.
2. Phase 11이 직접 구현해야 하는 Phase 08/09 port와 storage handoff source가 읽기
   순서·fingerprint에서 빠졌다.
3. Canonical Phase 11의 exact test inventory를 36개 “후보”로 축약하면서 필수
   case manifest와 차집합 판정을 만들지 않았다.
4. `*IT` command가 의존하는 Maven Failsafe/JUnit/profile wiring을 WP와 effective-POM
   gate에서 닫지 않았다.
5. `java` fence의 proposed `final class` 네 개가 method body 없는 declaration을
   포함해 Java 25에서 compile되지 않는다.
6. S3 conditional write의 409/412, versioning, multipart와 tenant credential
   semantics가 구현·test 계약에서 빠졌다.
7. Observability/cost의 “required” schema가 원문보다 추상화돼 사람이 pass 기준을
   새로 발명해야 한다.

따라서 verdict는 `CHANGES_REQUIRED`다. 이는 Phase 11 구현 실패 판정이 아니라
사람용 가이드의 교정 요구다. 실제 Phase 11 구현은 별도로 predecessor acceptance,
cross-Phase contract, AWS ADR/environment와 provider evidence가 없어 계속
`BLOCKED_NOT_IMPLEMENTED`다. Target은 이 리뷰에서 수정하지 않았다.

## 2. 검토 source, fingerprint와 line inventory

### 2.1 Canonical 5문서와 implementation source

사용자 지시의 “canonical 5문서”는 현재 [repository design map](../../../README.md)의
최상위 진입점과 Canonical Master §1.4를 따라 non-dated Master/Domain/Architecture,
integrated design, question register로 판정했다.

| Source | 직접 대조한 범위 | Fingerprint / lines |
|---|---|---|
| [Canonical Master](../../../master-design.md) | §1~§4, §13~§17, 특히 logical port·publication·RM-8·AWS/production 분리 | Git blob `b507a5e7ba0b7e76475bc2d755493e814f4d053a`; SHA-256 `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd`; 1,648줄 |
| [Canonical Domain](../../../domain-design.md) | §1~§3, §9~§16, stable identity·verification·result lifecycle | Git blob `ace117c380466b733994a1fbb2a95d31e41b3959`; SHA-256 `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73`; 1,607줄 |
| [Canonical Architecture](../../../architecture-design.md) | §1~§7, §10~§20, 특히 §12~§18 AWS/port/test boundary | Git blob `81495ff448d0e618ab3563e8ff80614fb1028acf`; SHA-256 `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201`; 1,469줄 |
| [Integrated design](../../../architecture-domain-implementation-design.md) | §1~§3, §12~§16, §19~§28 | Git blob `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0`; SHA-256 `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571`; 3,822줄 |
| [Question register](../../../master-design-open-questions.md) | 전체와 exact `Q-BENCH-02`, `Q-INFRA-01`, `Q-VAR-01` | Git blob `3fff4c583a54f02dea667e78c8e5187d65ec0e18`; SHA-256 `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b`; 87줄 |
| [Implementation README](../../README.md) | §0~§7, authority·ALNS-first DAG·상태 권한 | Git blob `8a9cb4a29685a2540bd605c3ac63bb459052b2a1`; SHA-256 `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358`; 240줄 |
| [Master realization plan](../../master-realization-plan.md) | Phase 08~14와 §8~§15 | Git blob `d7f6be4fff0089204fbdb52f731b2348407f36eb`; SHA-256 `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d`; 943줄 |
| [Execution progress](../../execution-progress-and-results.md) | §1~§10, 특히 live Phase 00/11 상태 | snapshot Git object `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d`; SHA-256 `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d`; 439줄 |

Target이 선언한 날짜 문서의 HEAD Git blob
`0a02ba4c77a402455e3d80b76969dca28831b1e6`와
`d51339e251dee1e032e711144dc63d6d07d7323b` 자체는 재현됐다. 문제는 hash가 아니라
어느 문서군을 canonical authority로 봉인했는가이다. 날짜 문서가 plain 문서를
`supersedes`한다고 적은 반면, 현재 docs 지도와 Canonical Master는 plain 문서를
현재 상세 설계로 가리키므로 이 source governance drift도 finding에 포함했다.

### 2.2 원본 Phase/review와 인접 사람용 guide

| Source | 직접 대조한 범위 | Fingerprint / lines |
|---|---|---|
| [Canonical Phase 11 원본](../../phases/phase-11-aws-reference-distribution.md) | 전체 §1~§19, 특히 §4~§15와 exact test matrix | Git blob `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8`; SHA-256 `73d25b651e22bcca82dfa4431d76b5896de01750eea5d9ee9dc2af29cf488728`; 1,833줄 |
| [원본 Phase 11 review](../../reviews/phase-11-review.md) | F-P11-001~008, applied correction, residual blocker와 검증 | Git blob `74377517a4018da73bc0e0bc8bf033ff7a3b0832`; SHA-256 `52ad3f6803c301c00dd7191fe0e78777d9b7b7e1a54da6d1db3a87dcb35625bc`; 409줄 |
| [Phase 10 사람용 guide](../phases/phase-10-human-implementation-guide.md) | exact action/state, cancellation/publication/deadline handoff | Git object `09f6f24e53167a4e320fc158640c13d1aa0cd29f`; SHA-256 `c1649837bbb057ba3272a1c0964782477d32b764e2354ed2c06a94cf14839290`; 2,074줄 |
| Target | 전체 | Git hash-object `1204cd4ed501921f5a4c0dc6edd1f8e70f6fa729`; SHA-256 `b38705bc9289b9b5d1c9159ce20cc7a6b4112c05515dcbcfd3c04ee5182c94ee`; 1,535줄 |
| [Phase 12 사람용 guide](../phases/phase-12-human-implementation-guide.md) | Phase 11 evidence consumer, adoption/migration authority와 reverse-edge 금지 | Git object `d0557e019ec71fbeb1e931d589945949f7ac0074`; SHA-256 `a0e29edfacc21d8a9f23e68b665db0eb62e862fec1f25d7a8031d0a96c2c6fdf`; 2,784줄 |
| Live root `pom.xml` | parent/plugin/dependency/test lifecycle | Git object `1dc675ba17b7f2202f34a22131f152cc2868b075`; SHA-256 `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`; 237줄 |

기술 실제성은 Oracle Java SE 25 JLS, Maven Failsafe 공식 parameter/lifecycle 문서,
AWS S3 conditional-write와 SDK v2 공식 문서를 보조 primary source로 대조했다.
이는 AWS provider를 새 semantic authority로 사용한 것이 아니라 proposed
Java/Maven/AWS mapping이 실제 platform 동작과 맞는지 확인하기 위한 것이다.

## 3. HEAD와 live inventory

### 3.1 HEAD baseline

HEAD `7cc890ee1d0805df5ae14b633127fade4f978639`에는 다음이 추적돼 있다.

- 단일 root JAR POM, root main Java 6개와 test Java 1개
- Maven wrapper와 target reactor 없음
- synthetic `AlnsBatchEngine`과 GCP-oriented placeholder
- Phase 11 AWS adapter/distribution/IaC/test/evidence 없음

Target의 HEAD baseline과 HEAD source fingerprint는 재현됐다.

### 3.2 검토 시점 live working tree

Target이 `2026-07-29T01:00:44+0900`에 고정한 one-time snapshot 뒤에도 concurrent
Phase 00 작업은 진행됐다. 이 review snapshot에서는 다음을 관찰했다.

- Root는 `packaging=pom` reactor이고 `node_modules`를 제외한 POM은 root 포함
  13개다.
- `./mvnw`, `.mvn/`, `rpdptw/`, `build/`, `legacy/`가 존재한다.
- `rpdptw` main Java 23개는 모두 `package-info.java`이고 production type/test는
  아직 0개다.
- `build`에는 architecture/test-fixture test Java 9개가 있다.
- `adapters/object-s3`, `adapters/workflow-aws-stepfunctions`,
  `adapters/compute-aws-lambda`, `distributions/aws-serverless`,
  `deployment/aws`, `build/port-contract-tests`는 없다.
- Live root POM에는 Surefire 3.5.4만 있고 Maven Failsafe plugin/profile은 없다.
- Scheduler progress는 Phase 00을 `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`, review 01을
  `COMPLETED_CHANGES_REQUIRED`, 기존 evidence bundle을
  `REJECTED_PENDING_FIX_01_REGENERATION`으로 기록한다.
- Phase 11은 계속 `BLOCKED_NOT_IMPLEMENTED`; `E-P11-*`, AWS deploy, post-review
  acceptance receipt와 production authority는 없다.

Target은 자신의 snapshot timestamp와 “구현 재개 때 새 inventory receipt” 정책을
명시했으므로 concurrent drift 자체를 별도 target finding으로 만들지는 않았다.
다만 현재 blocker를 판단할 때 Target의 frozen Phase 00 `IN_PROGRESS` 설명을 live
status로 재사용해서는 안 된다.

## 4. 검토 방법과 severity

1. Target 1,535줄을 line number와 함께 전수 읽었다.
2. Canonical 5문서, implementation README/master plan/live progress, 원본 Phase
   11/review를 source 우선순위에 따라 대조했다.
3. Phase 10/12 사람용 guide와 원본 Phase 08~10/12에서 producer/consumer artifact,
   state/action, storage/publication과 evidence dependency를 양방향 확인했다.
4. HEAD Git tree와 live reactor/POM/Java/test/progress/AWS target path를 분리했다.
5. Canonical Phase 11 §12의 unique exact test method 64개와 Target §11.3의 candidate
   method 36개를 기계적으로 추출·비교했다.
6. Proposed Java fence를 Java SE 25 문법과, future Maven command를 current
   effective build wiring 및 Failsafe lifecycle과 대조했다.
7. S3 `If-Match`/`If-None-Match`, 409/412, policy enforcement와 versioning/multipart
   경계를 AWS official documentation으로 확인했다.
8. Entry/exit/evidence/rollback/failure/security/observability/reproducibility,
   hidden default, Phase 13/14 gate와 사람 승인 안전 지점을 검사했다.
9. Target local link/GFM fragment, heading/fence/whitespace/EOF와 scoped
   `git diff --check`를 정적으로 검사했다.
10. Maven/AWS test는 실행하지 않았다. Phase 11 module/test/profile이 없고 concurrent
    Phase 00 working tree에 새 build artifact를 만드는 것은 이 문서 review의 증거가
    아니기 때문이다.

| Severity | 기준 |
|---|---|
| `CRITICAL` | 권위·publication·security fence를 즉시 우회해 회복 곤란한 정상 결과 corruption을 허용 |
| `HIGH` | 잘못된 canonical source, 필수 test/build gate 누락으로 잘못된 구현 또는 false acceptance를 허용 |
| `MEDIUM` | handoff/API/provider/운영 판정이 닫히지 않아 사람이 임의 판단하거나 재현성이 약화 |
| `LOW` | 의미 영향은 제한적이나 link·용어·metadata 신뢰를 저하 |

## 5. Finding summary

| ID | Severity | Finding | Target 수정 |
|---|---|---|---|
| `HG11-R001` | `HIGH` | Canonical Domain/Architecture 대신 날짜 문서를 authority로 고정 | `YES` |
| `HG11-R002` | `MEDIUM` | 직접 소비하는 Phase 08/09 handoff source와 exact section이 읽기 순서에서 누락 | `YES` |
| `HG11-R003` | `HIGH` | Canonical exact test 64개를 36개 후보로 축약하고 required manifest/차집합 gate 부재 | `YES` |
| `HG11-R004` | `HIGH` | Failsafe/JUnit/profile/effective-POM wiring 없이 `*IT` future command를 제시 | `YES` |
| `HG11-R005` | `MEDIUM` | Proposed `final class` Java skeleton이 method body 없이 compile 불가 | `YES` |
| `HG11-R006` | `MEDIUM` | S3 409/412·versioning·multipart·tenant credential mapping과 test가 불완전 | `YES` |
| `HG11-R007` | `MEDIUM` | Required logs/metrics/alarms/cost vector의 exact manifest가 제거돼 운영 pass 판정 불가 | `YES` |

## 6. Findings

### HG11-R001 — Canonical Domain/Architecture source가 잘못 고정됐다

- **Severity:** `HIGH`
- **Finding:** Target metadata lines 35~36, source table lines 207~208, conflict order와
  reading order는 `docs/2026-07-26-domain-design.md`와
  `docs/2026-07-26-architecture-design.md`를 Final authority로 봉인한다. 그러나
  이번 사용자 지시는 canonical 5문서를 요구하고, 현재 `docs/README.md` lines
  3~12/31~45와 Canonical Master §1.4는 `docs/domain-design.md`와
  `docs/architecture-design.md`를 현재 Domain/Architecture 상세 설계로 직접
  가리킨다. Target은 이 source-set conflict를 표시하거나 semantic diff하지 않았다.
- **사람에게 미치는 영향:** 신규 구현자는 current Architecture §12~§18의 logical
  port/AWS/test 경계와 Domain §13~§16의 publication/evidence 의미 대신 다른
  version·section을 entry receipt에 봉인할 수 있다. Hash가 정확해도 잘못 선택한
  source를 정확히 fingerprint한 false assurance가 된다.
- **Target 위치/anchor:** metadata lines 33~46;
  [§3.1 충돌 해소](../phases/phase-11-human-implementation-guide.md#31-충돌-해소-순서);
  [§3.2 source fingerprint](../phases/phase-11-human-implementation-guide.md#32-검증-가능한-source-fingerprint);
  §3.3 읽기 순서와 §16 traceability.
- **Source section:** User-fixed canonical 5; `docs/README.md` §권장 읽기 순서/문서
  계층; Canonical Master §1.4; Canonical Domain §1/§3/§13~§16; Canonical
  Architecture §1/§12~§20/§22.
- **Root cause:** Implementation README와 원본 Phase 11의 “Final dated design”
  source list를 더 높은 current docs map 및 이번 사용자 지시와 재조정하지 않고
  그대로 복제했다.
- **Required correction:** Target metadata, source table, 읽기 순서, WP 근거와
  traceability를 plain Domain/Architecture current blob과 heading으로 정렬한다.
  단순 hash 교체가 아니라 두 source set의 AWS port/state/publication/test 차이를
  semantic diff하고 영향 WP/test/evidence를 갱신한다. 날짜 문서의 self-declared
  `supersedes`와 current docs map의 충돌은 known source-governance blocker로
  명시하고, 상위 owner가 반대 결정을 내리면 같은 변경 단위에서 source index와
  fingerprint를 다시 승인받는다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Implementation README, human-guide README와 여러 원본
  Phase/review가 여전히 날짜 문서를 “Final”로 부른다. Target correction만으로
  repository 전체 source hierarchy가 정리되지는 않는다.

### HG11-R002 — Phase 08/09의 직접 handoff source가 entry 읽기 순서에서 빠졌다

- **Severity:** `MEDIUM`
- **Finding:** Phase 11은 Phase 08의 outbound port와 Phase 09의
  exact-read/put-if-absent/CAS/publication contract를 직접 구현하지만 Target의
  `source_sections_and_fingerprints`와 §3.2/§3.3은 Phase 10/11/12/14만 직접
  인용한다. §3.4와 WP11-2/3은 “accepted Phase 08/09 contract”를 요구하면서 exact
  source section, current residual blocker와 semantic compatibility 확인 위치를
  제공하지 않는다. 원본 Phase 11 metadata/§1.1은 Phase 08 §7.3~§9.3/§16과
  Phase 09 §7.2/§7.5~§9.6/§15를 직접 source로 열거한다.
- **사람에게 미치는 영향:** 구현자는 Target §9의 `PROPOSED/OPEN` type을 accepted
  Phase 08/09 API로 오인하거나 Phase 09의 distinct publication precondition,
  verified-read/metadata, key encoding과 tenant boundary 변경을 놓칠 수 있다.
  Phase 10만 재독해해서는 storage adapter의 직접 owner 계약을 닫을 수 없다.
- **Target 위치/anchor:** metadata lines 33~46;
  [§2.4 producer/consumer](../phases/phase-11-human-implementation-guide.md#24-producer와-consumer-계약);
  [§3.3 읽기 순서](../phases/phase-11-human-implementation-guide.md#33-구현-전-정확한-읽기-순서);
  §3.4 entry, WP11-2/3, §16 traceability.
- **Source section:** Canonical Phase 11 §1.1 source table, §4 entry, §18.1; actual
  Phase 08 application port/deadline/local handoff; actual Phase 09 storage
  backend/verified read/CAS/publication handoff; original review F-P11-004/007/008.
- **Root cause:** 사람용 guide를 압축하면서 “인접 guide는 Phase 10/12”라는
  문서 인접성과 Phase 11이 실제로 직접 소비하는 semantic predecessor를 혼동했다.
- **Required correction:** Phase 08/09 actual 문서의 stable section, current status와
  blob을 source table/읽기 순서에 추가한다. Entry receipt가 accepted implementation
  evidence identity와 section semantic diff를 검증하게 하고, exact API가 다르면
  proposed Target type을 버리고 owner-approved signature를 단일 채택하게 한다.
  Whole-file reciprocal hash는 acceptance 조건으로 만들지 않는다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Phase 08~10 구현 계약 자체가 아직 accepted되지 않았으므로
  source를 추가해도 실제 adapter 구현 entry는 계속 blocked다.

### HG11-R003 — Required test inventory가 “후보”로 축약돼 false-green이 가능하다

- **Severity:** `HIGH`
- **Finding:** Canonical Phase 11 §12.2~§12.4에서 추출되는 unique exact test
  method는 64개지만 Target §11.3에는 이름을 합치거나 바꾼 candidate method 36개만
  있다. Target §11.5/§14.5는 expected/discovered/passed exact count를 요구하지만
  expected set의 versioned source/digest, canonical→renamed mapping과 차집합
  scanner가 없다. 대표적으로 stale opaque CAS rejection, verified initial put,
  actual S3 conditional-policy/KMS/concurrent-CAS, tenant prefix escape, duplicate
  worker/out-of-order wakeup, missing Phase-1 screen, exact workflow mode trace,
  retry identity, separate cancel intent/stop/terminal, role separation/API deny,
  template reference와 explicit function config cases가 normative gate에서 사라졌다.
- **사람에게 미치는 영향:** Target 표의 class/method만 green이면 AWS mode,
  storage conflict, IAM, duplicate/retry, cancellation 또는 IaC 결함이 남아도
  `E-P11-*` 후보 bundle이 완전하다고 오판할 수 있다. “필수 Integration/Fault/
  Security” category 문장이 실제 required case coverage를 보장하지 않는다.
- **Target 위치/anchor:** [§10.1 command map](../phases/phase-11-human-implementation-guide.md#101-wp별-future-검증-command-map);
  [§11.3 test 후보](../phases/phase-11-human-implementation-guide.md#113-exact-test-classmethod-후보);
  [§11.5 false-green](../phases/phase-11-human-implementation-guide.md#115-false-green-방지);
  §11.6 category와 §14.5 evidence checklist.
- **Source section:** Canonical Phase 11 §12.1~§12.6; original Phase 11 review
  F-P11-001~004/007~008; Canonical Architecture §18.1~§18.4.
- **Root cause:** 교육용 핵심 사례로 표를 줄이면서 canonical acceptance inventory와
  학습 subset을 분리하지 않았고, “exact count”를 구현할 manifest를 문장으로만
  남겼다.
- **Required correction:** 64개 canonical case를 fixture/oracle/pass criterion과
  함께 복원하거나, canonical inventory를 versioned machine-readable required
  manifest로 만들고 Target 36개는 학습 subset이라고 명시한다. 이름을 합친 case는
  canonical source case 각각에 대한 parameterized execution과 report identity를
  mapping한다. Exit에서 required set 대 Surefire/Failsafe XML의
  missing/duplicate/failure/error/skipped/renamed-unmapped 차집합이 모두 0인지
  자동 판정하고, seeded faulty provider/double가 해당 test를 실제 red로 만드는
  sensitivity evidence를 요구한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Method 수만 맞추고 expected를 AWS output으로 golden-update하거나
  oracle가 production mapping helper를 공유하면 false-green은 남는다.

### HG11-R004 — Maven Failsafe와 test dependency/profile wiring이 닫히지 않았다

- **Severity:** `HIGH`
- **Finding:** Target의 WP11-2/4~8 command는 `-Dit.test=...`,
  `-Dfailsafe.failIfNoSpecifiedTests=true`와 `clean verify`를 사용하지만, 예상
  change tree/WP11-1에는 Maven Failsafe plugin, `*IT` include, JUnit dependency,
  shared fixture test-jar, profile activation과 report scanner를 추가·검증하는
  행동이 없다. Live root POM은 Surefire 3.5.4만 선언하고 Failsafe와
  `aws-integration`/`aws-local-it` profile이 없다. Failsafe가 binding되지 않으면
  `-Dit.test` property는 실행 대상을 만들지 않으며 `verify`가 integration test
  0개로 끝날 수 있다. Target의 prose상 report reconciliation은 이를 실행하는
  command/test가 없다.
- **사람에게 미치는 영향:** `*IT` source가 JUnit classpath에서 compile되지 않거나,
  test가 한 건도 실행되지 않거나, 개발자 local repository에 우연히 설치된 upstream
  artifact와 profile에 따라 결과가 달라진다. `clean verify` exit 0을 actual AWS
  contract evidence로 오인할 수 있다.
- **Target 위치/anchor:** [§8.1 change tree](../phases/phase-11-human-implementation-guide.md#81-예상-change-tree);
  WP11-1; [§10.1 command map](../phases/phase-11-human-implementation-guide.md#101-wp별-future-검증-command-map);
  [§11.7 Maven/IaC 명령](../phases/phase-11-human-implementation-guide.md#117-maveniac-명령--현재와-미래를-구분하기);
  §11.5 false-green.
- **Source section:** Canonical Architecture §5.2/§18.1~§18.4; Canonical Phase
  11 §12.6; original review F-P11-003; live root/module POM; Maven official
  [Failsafe integration-test goal](https://maven.apache.org/surefire/maven-failsafe-plugin/integration-test-mojo.html)
  (`failsafe.failIfNoSpecifiedTests`, default includes와 lifecycle).
- **Root cause:** Selected-test false-green을 command-line property와 fresh XML
  문장으로 막으려 했지만 그 property를 소비하는 plugin/effective-POM과 target
  report validator를 구현 범위에 넣지 않았다.
- **Required correction:** WP11-1/Phase 00 handoff에 pinned Failsafe plugin/version,
  integration-test+verify binding, `*IT` include, adapter/distribution별 JUnit 및
  approved fixture dependency, profile activation condition과 skip 금지를 추가한다.
  `help:effective-pom` 또는 동등한 machine check로 실제 binding을 검증한다. Clean
  isolated local repository에서 upstream root install을 먼저 수행한 뒤 target
  module/profile의 Failsafe summary/XML 및 required manifest 차집합을 검사하는
  실행 가능한 command를 제시한다. Profile 미존재·미활성·0-test·summary 부재는
  모두 non-zero여야 한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Phase 00 fix가 이후 parent test lifecycle을 보완할 수 있지만,
  Phase 11 entry가 accepted effective-POM identity를 pin하지 않으면 environment
  의존 false-green이 재발한다.

### HG11-R005 — `java` skeleton의 concrete class declaration이 compile되지 않는다

- **Severity:** `MEDIUM`
- **Finding:** Target §9.1의 `final class S3ObjectStorageBackend`, §9.3의
  `final class StepFunctionsWorkflowExecutionAdapter`,
  `StepFunctionsCoordinatorActionExecutor`, `LambdaWorkerDispatcher`는 method body나
  `abstract` modifier 없이 세미콜론으로 끝나는 method declaration을 가진다.
  Java SE 25에서 non-abstract/non-native method body가 세미콜론인 것은 compile-time
  error이고, final non-abstract class는 추상 method를 가질 수 없다. “skeletal
  contract” 설명은 있지만 fence language가 `java`이고 exact candidate type처럼
  배치돼 있다.
- **사람에게 미치는 영향:** 신규 구현자는 이 골격을 red compile fixture로
  붙여넣거나 interface/class ownership을 잘못 결정한다. Proposed contract review가
  실제 signature compile 검증까지 통과했다고 오해할 수 있다.
- **Target 위치/anchor:** [§9 Proposed/Open Java 계약](../phases/phase-11-human-implementation-guide.md#9-proposedopen-java-계약과-상태-전이),
  특히 lines 742~756, 813~833.
- **Source section:** Canonical Architecture §6.6/§12; Phase 08~10 accepted
  signature owner boundary; Oracle
  [Java SE 25 JLS §8.4.7](https://docs.oracle.com/javase/specs/jls/se25/html/jls-8.html#jls-8.4.7)
  method body와 §8.1.1 abstract/final class 규칙.
- **Root cause:** Interface 수준의 signature와 concrete adapter 후보를 한 Java
  snippet에 합치면서 pseudocode용 body 생략을 유효 Java로 표시했다.
- **Required correction:** Pure port contract는 `interface`로 표시하고 concrete
  adapter는 method body가 있는 compile 가능한 최소 skeleton으로 바꾼다. 구현을
  의도적으로 생략하면 `text`/`pseudocode` fence로 바꾸고 compile 가능한 Java라고
  주장하지 않는다. Sealed event 예시의 permitted subtype도 같은 snippet 안에서
  정의하거나 의도적 생략을 표시한다. Accepted Phase 08~10 signature와 달라질 때
  proposed 이름을 폐기하는 gate는 유지한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** 문법을 고쳐도 exact public API, error hierarchy와 owner는
  predecessor acceptance 전 계속 `PROPOSED/OPEN`이다.

### HG11-R006 — S3 conditional conflict와 versioning/multipart 계약이 충분히 실제적이지 않다

- **Severity:** `MEDIUM`
- **Finding:** Target §9.1은 logical projection과 “S3 412”만 설명하고
  `S3BackendConfig`에서 원본 Phase 11의 `tenantCredentialProviderRef`,
  `versioningRequired`를 제거했다. Test도 stale token/actual concurrent CAS는
  일부 다루지만 `409 ConditionalRequestConflict`, versioned bucket의 current
  version/delete marker, multipart completion과 body replayability를 구분하지
  않는다. AWS S3는 `If-None-Match`/`If-Match` failure로 412뿐 아니라 concurrent
  operation의 409를 반환할 수 있고 operation별 재시도 절차가 다르다. Bucket policy
  conditional enforcement는 multipart/copy에 별도 제약이 있다.
- **사람에게 미치는 영향:** Implementer가 409를 generic transport retry로 처리해
  stale CAS token이나 재사용 불가능한 stream을 그대로 반복하거나, versioning/delete
  marker에 따라 “absent/current” 의미가 바뀌는데도 같은 logical outcome으로
  분류할 수 있다. 보안 owner가 tenant credential scope를 application string
  check로 대체할 여지도 남는다.
- **Target 위치/anchor:** [§6.3 결정 상태](../phases/phase-11-human-implementation-guide.md#63-확정-proposedopen-gated-deferred);
  [§9.1 storage 후보](../phases/phase-11-human-implementation-guide.md#91-storage-backend-후보);
  WP11-2; §11.3 `S3ErrorMapperTest`/`S3ObjectStorageBackendContractIT`;
  §14.3 storage exit.
- **Source section:** Canonical Phase 11 §6.1 lines 481~529, §7.3, §8.1~§8.2,
  §12.2~§12.4; original review F-P11-002; AWS official
  [conditional writes](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes.html),
  [bucket-policy enforcement](https://docs.aws.amazon.com/AmazonS3/latest/userguide/conditional-writes-enforce.html),
  [AWS SDK v2 `PutObjectRequest`](https://docs.aws.amazon.com/java/api/latest/software/amazon/awssdk/services/s3/model/PutObjectRequest.html).
- **Root cause:** Provider-specific 상세를 open ADR로 남기는 과정에서 “무엇을
  결정하지 않는다”와 “actual AWS suite가 반드시 분류해야 할 관측 결과”를 함께
  제거했다.
- **Required correction:** 특정 production choice를 발명하지 말고
  versioning/delete-marker/multipart/copy/stream replay와 tenant credential source를
  explicit ADR input으로 복원한다. Immutable create와 CAS 각각에 412/409/deny/
  timeout outcome, exact reread/revalidate와 allowed retry identity를 표로 만든다.
  Actual AWS test가 original bytes/token/current version, exactly-one winner,
  multipart cleanup과 policy deny를 검증하게 한다. Unsupported 조합은 fallback하지
  않고 entry/config에서 fail-closed한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Exact S3 key encoding, versioning mode, multipart threshold와
  retry 수치는 Phase 09/11 owner 승인 전 계속 open이어야 한다.

### HG11-R007 — Observability와 cost의 required schema가 판정 가능한 형태가 아니다

- **Severity:** `MEDIUM`
- **Finding:** Target WP11-6과 §11.6은 “required signal”, “bounded metrics”,
  “usage vector complete”를 요구하지만 required structured log fields, metric
  names/allowed dimensions, alarm catalog와 cost usage fields를 제시하지 않는다.
  `required-metrics-v1.json` fixture의 owner/source/digest도 없다. 반면 원본 Phase
  11 §10은 exact log field, 13개 metric, forbidden high-cardinality dimensions,
  required alarm과 manifest별 cost vector를 열거한다.
- **사람에게 미치는 영향:** 구현자가 수집하기 쉬운 metric/usage만 fixture에 넣고
  같은 fixture를 oracle로 사용해 self-fulfilling green을 만들 수 있다. Missing
  declared worker, verifier rejection, log exporter failure, retry/duplicate cost가
  관측되지 않아도 “required complete”를 주장할 수 있다.
- **Target 위치/anchor:** WP11-6;
  [§11.1 fixture/builder](../phases/phase-11-human-implementation-guide.md#111-fixture와-builder);
  §11.3 `AwsObservabilityAwsIT`/`AwsCostEvidenceIT`; §11.6 category;
  §12.3 evidence manifest와 §14.4 exit.
- **Source section:** Canonical Phase 11 §10.1~§10.2, §12.1/§12.4, §14~§15;
  Canonical Architecture §17.1~§17.3/§18.4; original review observability/cost
  axis.
- **Root cause:** 코드 과다를 줄이면서 operational field manifest와 독립 owner까지
  추상 명사로 축약했다.
- **Required correction:** 원본 §10의 required log/metric/alarm/cost catalog를
  normative table로 복원하거나 versioned fixture/schema의 exact path, digest,
  owner와 source derivation을 지정한다. Tenant/solve/worker/digest의 metric
  dimension 금지, integrity/security event sampling 금지, failure run 포함과
  expected/discovered field 차집합을 자동 판정한다. Cost cap/threshold는 계속
  `OPEN/OWNER_APPROVAL_REQUIRED`로 두고 field completeness와 affordability
  승인을 분리한다.
- **Target 수정 필요 여부:** `YES`
- **Residual risk:** Exact threshold, retention, budget과 production affordability는
  실제 workload 및 Ops/Security/FinOps 승인 전에는 문서 correction만으로 닫히지
  않는다.

## 7. No-finding 근거와 구현 blocker 구분

### 7.1 추가 finding을 만들지 않은 축

| 검사축 | 판정 | 근거 |
|---|---|---|
| Phase 11 범위와 AWS authority 분리 | `PASS` | AWS selected/implemented/evidenced/production 네 상태를 분리하고 Phase 12/13/14 작업을 당기지 않음 |
| RPDPTW primer와 신규 독자 이해 | `PASS` | Request pair, route-bank XOR, identity/observation, lifecycle와 timeout 종류를 설명 |
| Entry/exit/사람 승인 | `PASS` | 9개 entry AND gate, last safe point, named owner와 stop/resume를 명시 |
| State/action/publication fence | `PASS DOCUMENT PLAN` | Exact pending action, opaque token exclusion, distinct publication precondition와 same-state cancel/publish race 보존 |
| Failure/rollback | `PASS DOCUMENT PLAN` | Missing worker, timeout/OOM/throttle/DLQ/cancel/corruption typed failure와 previous/local rollback을 분리 |
| Security boundary | `PASS CONCEPT / R007 DETAIL GAP` | Role 분리, tenant/KMS/network deny, redaction과 no broad wildcard를 요구; exact 운영 manifest 보완은 R007 |
| Reproducibility | `PASS` | Seed/warm start/work/manifest 고정, new `AttemptId`, provider metadata 제외와 different-digest integrity failure 보존 |
| Evidence DAG | `PASS` | Pre-review manifest `M` → review `R` → receipt `(M,R)` 단방향이고 Phase 12 reverse reference 금지 |
| Hidden default와 Phase 13/14 gate | `PASS` | `Q-BENCH-02 OPEN`, `C-17 GATED`, `Q-VAR-01 DEFERRED`, 14A/14B와 production authority 분리 |
| 문서 대 구현 상태 | `PASS` | Module/POM/package-info/generated `.serverless`/test pass를 Phase 11 구현 evidence로 승격하지 않음 |
| 코드 과다 | `PASS WITH R005` | 완성 adapter 코드를 복사하지 않고 semantic skeleton 중심; 다만 Java fence 문법은 교정 필요 |

### 7.2 가이드 correction과 별개인 실제 구현 blocker

다음은 Target 문서 교정 여부와 무관하게 현재 Phase 11 구현을 막는다.

- Phase 00은 `CHANGES_REQUIRED_FIX_01_IN_PROGRESS`이고 accepted receipt가 없다.
- Phase 06~10 accepted implementation/evidence/handoff가 없다.
- Exact action authorization, distinct publication precondition, same-state
  cancel/publish fence와 durable deadline restart contract가 cross-Phase로 미승인이다.
- Workflow type/invocation, S3 layout/versioning, IAM/KMS/network, quota/retention/cost
  ADR와 isolated AWS account/role/budget/cleanup authority가 없다.
- Actual S3/Step Functions/Lambda/IAM/KMS parity/fault/security/rollback evidence와
  Phase 11 post-review acceptance receipt가 없다.
- Phase 14A receipt와 Phase 14B signed production authority가 없다.

따라서 이 리뷰의 `CHANGES_REQUIRED`를 해결해도 Phase 11 implementation을
`READY`나 `ACCEPTED`로 승격할 수 없다. 반대로 구현 blocker가 있다는 이유로 사람용
가이드의 source/test/Maven 결함을 남겨둘 수도 없다.

## 8. 정적 검사와 범위 검증

| 검사 | 결과 | 관찰 |
|---|---|---|
| Target identity | `PASS` | Git hash-object/SHA-256/lines가 metadata의 `1204cd...` / `b38705...` / 1,535와 일치 |
| Target tracked 상태 | `PASS INVENTORY` | HEAD에 없고 `??` untracked; 구현/acceptance 의미 없음 |
| Target-declared source fingerprints | `PASS HASH / SOURCE-SELECTION FINDING` | 선언한 HEAD Git blob은 재현; canonical source 선택 문제는 HG11-R001 |
| HEAD 대 live inventory | `PASS SEPARATION` | HEAD 6/1 single project와 live 13 POM/23 package-info/9 build tests/Phase 00 fix 상태를 분리 |
| Local Markdown link/fragment | `PASS` | Target local links 17/17, unique 17, missing file/fragment 0 |
| Heading/GFM structure | `PASS` | H1 1, H2 17, H3 66; target title/Phase 번호 일치 |
| Fence | `PASS` | Opening/closing fence marker 78개, parity 정상 |
| Whitespace/EOF | `PASS` | Target trailing whitespace 0, final newline 존재 |
| Canonical test inventory comparison | `FAIL TARGET COVERAGE` | 원본 unique exact method 64 대 Target candidate 36; required mapping/manifest 없음 |
| Java 25 syntax inspection | `FAIL TARGET SKELETON` | `java` fence의 concrete final class method-body 누락 4개 type |
| Maven effective wiring inspection | `FAIL TARGET EXECUTABILITY` | Live Failsafe/profile 0, Target WP의 required POM wiring/scanner 부재 |
| AWS conditional semantics cross-check | `FAIL TARGET COMPLETENESS` | 409/versioning/multipart/credential case가 Target contract/test에서 누락 |
| Scope preservation | `PASS` | Target/README/progress/타 문서/코드/POM/test/deployment 수정 없음; output review만 생성 |
| Stage/commit/push/worktree | `PASS` | 수행하지 않음 |

Maven build, AWS deployment와 provider integration은 실행하지 않았다. 실행 대상
module/profile/test가 없고, 이 리뷰에서 생기는 `target/`이나 cloud resource는
Phase 11 evidence가 아니기 때문이다.

## 9. Verdict

Target은 Phase 11의 핵심 불변조건과 gate를 상당히 잘 보존했지만, canonical source,
direct predecessor handoff, test inventory, Maven lifecycle, Java syntax, actual S3
conflict와 운영 evidence 판정에서 사람이 임의 결정을 해야 한다. 7개 finding을
교정하고 같은 독립 review를 다시 통과하기 전에는 사람용 구현 가이드로 승인할 수
없다.

VERDICT: CHANGES_REQUIRED
TARGET_CHANGES_REQUIRED: YES
FINDING_COUNTS: CRITICAL=0 HIGH=3 MEDIUM=4 LOW=0
REQUIRED_CORRECTION_FINDINGS: HG11-R001, HG11-R002, HG11-R003, HG11-R004, HG11-R005, HG11-R006, HG11-R007

## Correction 01 읽기 전용 재검증

### Recheck metadata, 범위와 방법

| 항목 | 값 |
|---|---|
| Recheck round | `01` |
| Recheck 시각 | `2026-07-29T02:46:26+09:00` (`Asia/Seoul`) |
| 역할 | 원 reviewer의 correction 01 read-only follow-up |
| HEAD / branch | `7cc890ee1d0805df5ae14b633127fade4f978639` / `codex-implementation` |
| 원 review 보존 baseline | 첫 504줄 Git blob `1dfdb093c3c436a9e2b4ec2d2d9874f94b38be41`; SHA-256 `0ffdc4ce70364dae63dca266839a34692c1a821e1620614dec57ba9e9dcd29a6` |
| Corrected target | Git blob `4c2bb22953eea363e9485167fd002d8e3fc2ba8b`; SHA-256 `d3a8733847b2e3f411229db49a944402560ecebff8110c0b035c910415f3f657`; 1,965줄 |
| Correction report | Git blob `fc6d192dee76b271e10d6e6346b74f15b1404b44`; SHA-256 `211cc2558f18a21ac2b08b4d5e2fddab1958a574953d0d7f09da7fd7f152caac`; 342줄 |
| 허용 write | 이 review 끝의 본 절 append만 |
| 금지 범위 준수 | Target/correction report/canonical/README/progress/코드/POM/test/deployment/타 review 수정 없음; stage/commit/push/worktree 없음 |

Correction report의 `7/7 ADDRESSED` 자기주장은 판정 입력으로 사용하지 않았다. 원
finding의 root cause와 required correction을 다시 읽고, corrected target의 실제
anchor를 canonical/current/original/adjacent source와 독립 대조했다. Canonical test
표와 embedded manifest는 기계 추출해 byte·count·class·digest를 비교했고, live
POM/module/profile inventory, Java fence, local link/fragment, GFM heading/fence,
whitespace/EOF를 다시 검사했다.

Maven build와 AWS test/deploy는 실행하지 않았다. Live tree에는 Phase 11 module,
Failsafe/profile/test/resource가 없고 entry도 닫혀 있으므로, 다른 Phase의 green이나
새 `target/` 산출물은 correction 문서의 실행 가능성 또는 Phase 11 evidence가 아니다.

### 읽은 source와 recheck fingerprint

모든 SHA-256은 recheck 시점 file bytes, Git blob은 `git hash-object` 결과다.

| Source | Git blob | SHA-256 / lines |
|---|---|---|
| [Current docs map](../../../README.md) | `13f1b3b2dea038b8e0b466c138f5f59299413125` | `5ece2d41fe5a3c3f5f3d938c0440b4d91b0dcc0a9a055e5e76a739b7d29a8569`; 67 |
| [Canonical Master](../../../master-design.md) | `b507a5e7ba0b7e76475bc2d755493e814f4d053a` | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd`; 1,648 |
| [Canonical Domain](../../../domain-design.md) | `ace117c380466b733994a1fbb2a95d31e41b3959` | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73`; 1,607 |
| [Canonical Architecture](../../../architecture-design.md) | `81495ff448d0e618ab3563e8ff80614fb1028acf` | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201`; 1,469 |
| [Integrated design](../../../architecture-domain-implementation-design.md) | `1199abf2cd52c801ec412bfbcf4729e2b5b29cf0` | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571`; 3,822 |
| [Question register](../../../master-design-open-questions.md) | `3fff4c583a54f02dea667e78c8e5187d65ec0e18` | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b`; 87 |
| [Implementation README](../../README.md) | `8a9cb4a29685a2540bd605c3ac63bb459052b2a1` | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358`; 240 |
| [Master realization plan](../../master-realization-plan.md) | `d7f6be4fff0089204fbdb52f731b2348407f36eb` | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d`; 943 |
| [Execution progress](../../execution-progress-and-results.md) | `0419f69199b3140dd44020f78278b1352e6517b8` | `9361ae89c409adc75684b5bcb18e43558aa08ccc3c33acc5f5f9be849a1bf08c`; 470 |
| [Actual Phase 08](../../phases/phase-08-application-ports-local-runtime.md) | `2aff093a6f2728470a7ccbb22b7e1a1a71f5b963` | `15dbcab53fc00fc4d3062c5bdb0eb1f072fd902bfbb59b322859f30b090a5f0f`; 2,563 |
| [Actual Phase 09](../../phases/phase-09-object-storage-no-database.md) | `99a5b0df5531a65964272f423cc0ccca4d4f1430` | `ac08d10a63f7d0bd86a74fa61c1d220b0619a9c16c7d50fe0017cfe9afc8bb3b`; 1,967 |
| [Actual Phase 10](../../phases/phase-10-provider-neutral-coordinator.md) | `2b909924008df6c6f34d7d4d4399c8fdf6b6830a` | `e7656b800020592731e07a1f321a870fc7bdabb021925ed3b13ebe311b9ba8cd`; 1,685 |
| [Canonical Phase 11](../../phases/phase-11-aws-reference-distribution.md) | `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8` | `73d25b651e22bcca82dfa4431d76b5896de01750eea5d9ee9dc2af29cf488728`; 1,833 |
| [Canonical Phase 11 review](../../reviews/phase-11-review.md) | `74377517a4018da73bc0e0bc8bf033ff7a3b0832` | `52ad3f6803c301c00dd7191fe0e78777d9b7b7e1a54da6d1db3a87dcb35625bc`; 409 |
| [Actual Phase 12](../../phases/phase-12-provider-substitution.md) | `63b9defb25d7771bce590d593db9485367724918` | `5f243b2900afe31801ab1c47b9c2467a6344c42b7b27f49ee5c5b51d1c21de1a`; 1,984 |
| [Actual Phase 14](../../phases/phase-14-official-calibration-cutover.md) | `c7e537726d8a5c3b9ae315cf1a42dc454ecd3d26` | `c8f3b4fd2e48d35547d7e2fe31169a5e0a882730859ac97d9c5f60a165802eae`; 2,192 |
| [Human Phase 08](../phases/phase-08-human-implementation-guide.md) | `b3572fee2c687534cc41a581cb18f6f45e5328c6` | `ab9e6da3d04ebd8c50e3e499c5b1651cd80e8df0734f4d4ae72792fb612d1326`; 2,833 |
| [Human Phase 09](../phases/phase-09-human-implementation-guide.md) | `f4cbd4b9e85823686ed1c82014dbb84f475a659b` | `09b32c3a821b6ad68ee4aa5a0322d01af07ee08390e3650b80c8de8339b46038`; 2,727 |
| [Human Phase 10](../phases/phase-10-human-implementation-guide.md) | `dbbf11dca3bb1dee85c482d590a2e161ffcd77c3` | `be79b5fe2049c739ee2bd0105267f65c08a25feb22539e9b8bddf4beff1d8584`; 2,332 |
| [Human Phase 12](../phases/phase-12-human-implementation-guide.md) | `9ac8346b7bac697796d91c00891ae5083baf08b9` | `4e675d25ab0dbdfaf93a7174ea6d67adaa26ab72bc84d0115e23258adb243ee1`; 3,251 |
| Live root `pom.xml` | `1dc675ba17b7f2202f34a22131f152cc2868b075` | `ec712128e70b60797c571b2034528a1ff4d6166c5aaab3f43c6d7e9838f25c3c`; 237 |

Human Phase 08/09/10/12는 correction snapshot 뒤 live bytes가 바뀌었다. Corrected
target은 이를 acceptance로 사용하지 않고 §3.2에서 one-time snapshot임을 밝히며,
§3.3~§3.4와 WP11-0에서 구현 재개 시 stable section semantic diff와 accepted
implementation/evidence identity를 다시 요구한다. Recheck 시점의 현재 handoff도
Phase 08~10 `CHANGES_REQUIRED/NOT_ACCEPTED`, Phase 11/12 blocked와 단방향
handoff를 유지하므로 이 post-correction drift 자체는 원 finding 재개 사유가 아니다.

보조 기술 source는 원 review와 같은 Oracle Java SE 25 JLS §8.4.7, Maven Failsafe
`integration-test` goal/parameter/default include, AWS S3 conditional-write와
conditional-write bucket-policy/Java SDK 문서다. 이는 provider semantics를
canonical authority로 승격하기 위한 것이 아니라 Java fence, Maven discovery와
409/412/versioning/multipart/copy의 실제성을 확인하기 위한 것이다.

### Finding별 재판정

| ID | Status | 핵심 판정 |
|---|---|---|
| `HG11-R001` | `RESOLVED` | Plain Domain/Architecture authority, dated conflict semantic impact와 owner 재승인 gate가 실제 target에 있음 |
| `HG11-R002` | `RESOLVED` | Phase 08/09 actual/human direct source, section diff, accepted evidence identity와 signature 폐기 gate가 있음 |
| `HG11-R003` | `RESOLVED` | Canonical 64-case manifest bytes, mapping/report multiset, stale/zero/sensitivity fail-closed가 재현됨 |
| `HG11-R004` | `RESOLVED` | Future Failsafe/JUnit/fixture/profile/effective-POM wiring과 isolated/slice/report 실행 순서가 구현 범위에 들어옴 |
| `HG11-R005` | `RESOLVED` | Concrete Java method body와 sealed permitted subtype가 복원되고 skeleton/runtime 구분이 명시됨 |
| `HG11-R006` | `OPEN` | S3 operation 의미는 복원됐지만 새 actual-AWS 필수 scenario의 machine-readable discovery oracle이 없음 |
| `HG11-R007` | `RESOLVED` | Exact log/metric/alarm/cost schema, owner/source/digest, forbidden/freshness set 판정이 복원됨 |

#### HG11-R001 — RESOLVED

- **확인한 target anchor:** metadata lines 35~55; §3.1 lines 193~216; §3.2
  lines 218~245; §3.3 lines 266~285; §16 lines 1912~1943.
- **Source evidence:** Current docs map과 Canonical Master §1.4가 가리키는 plain
  Domain/Architecture blob이 metadata/source table/읽기 순서/traceability에
  일치한다. §3.1은 dated 문서의 self-declared supersession과 stale
  `Q-INFRA-01`를 은폐하지 않고 current source-set의 Domain publication,
  Architecture port/test 차이를 WP/test/evidence 영향으로 연결한다.
- **Root cause closure:** Implementation README의 dated source를 무비판 복제한
  상태가 제거됐다. 상위 owner가 다른 source-set을 선택하면 구현을 중지하고 source
  index/section diff/requirement/WP/test/evidence를 함께 재승인한다.
- **남은 risk:** Repository 전체의 dated/plain source-governance 충돌은 여전히
  외부 residual이다. Target 단독으로 이를 해결했다고 주장하지 않는다.

#### HG11-R002 — RESOLVED

- **확인한 target anchor:** metadata lines 45~55; §2.4 lines 179~189; §3.2
  lines 233~243; §3.3 lines 277~279; §3.4 lines 289~301; WP11-0/2/3 lines
  1160~1226; exit lines 1817~1823; §16 lines 1927~1929.
- **Source evidence:** Actual Phase 08 §7.3~§9.3/§16과 Actual Phase 09
  §7.2/§7.5~§9.6/§14~§15가 source row와 reading order에 직접 들어왔다. Entry는
  section semantic diff, accepted implementation artifact/evidence identity와
  handoff receipt를 모두 요구하고 whole-file reciprocal hash를 acceptance로
  사용하지 않는다.
- **Root cause closure:** “문서상 인접”과 “직접 semantic producer”가 분리됐다.
  Proposed signature가 owner-approved signature와 다르면 후보를 폐기한다.
- **남은 risk:** Phase 08~10 구현/review가 accepted되지 않아 actual adapter entry는
  계속 blocked다. Human guide의 post-correction live drift도 구현 재개 receipt에서
  다시 diff해야 한다.

#### HG11-R003 — RESOLVED

- **확인한 target anchor:** §11.3 lines 1357~1478; §11.5 lines
  1494~1506; §11.7 lines 1628~1700; §12.3 lines 1738~1760; exit lines
  1857~1866.
- **Source evidence:** Canonical Phase 11 §12.2~§12.4 표에서 source order로
  추출한 `Class#method()`는 64 unique/27 class다. Target embedded block도
  64 unique/27 class이고 byte diff 0, block SHA-256은 양쪽 모두
  `98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1`이다.
- **Root cause closure:** Future exact path와 64개 one-to-one mapping에
  fixture digest, independent oracle, pass criterion, report identity와
  `sensitivityFaultId`가 필수다. Expected/discovered/passed `64/64/64`,
  missing/duplicate/failed/error/skipped/renamed-unmapped/unexpected/stale/
  sensitivity-missing 0과 same-run source/profile/effective-POM identity가 AND다.
- **남은 risk:** Production helper/oracle 공유 또는 AWS output golden-update는
  sensitivity review가 실제로 실행되기 전까지 구현 residual이다. 이는 현재
  문서가 green이라고 주장하지 않는다.

#### HG11-R004 — RESOLVED

- **확인한 target anchor:** §8.2.1 lines 779~792; WP11-1 lines
  1178~1192; §10.1 lines 1308~1324; §11.5 lines 1494~1506; §11.7 lines
  1615~1700; exit line 1834.
- **Source evidence:** Parent pin, 각 owner module의 `integration-test`+`verify`,
  explicit `*IT` include, JUnit Platform/approved fixture dependency,
  `aws-local-it`/`aws-integration`, skip 금지, summary XML과 selected-module
  effective-POM machine check가 명시됐다. Same source isolated full install,
  selected no-`-am`, filter-free slice/profile `-am clean verify`, 다음 clean 전
  fresh XML/P11CASE audit 순서도 연결된다.
- **Live inventory:** Root 포함 POM 13개지만 Failsafe reference 0,
  `aws-integration`/`aws-local-it` profile 0, Phase 11 expected path 0이다.
  Target §8.2.1/§11.7도 이를 현재 red로 기록하므로 문서와 실제 구현을 혼동하지 않는다.
- **Root cause closure:** CLI property만 있고 이를 소비하는 plugin/profile/report
  validator가 없던 문서 결함은 닫혔다.
- **남은 risk:** 명령과 module/test 이름은 future proposed contract다. Phase 00
  owner가 실제 effective POM과 dependency-closed discovery를 승인하기 전에는
  실행 가능/green evidence가 아니다.

#### HG11-R005 — RESOLVED

- **확인한 target anchor:** §9 introduction lines 807~809; S3 skeleton lines
  813~883; workflow/worker skeleton lines 952~1041; sealed event lines
  1045~1077.
- **Source evidence:** 네 concrete class의 모든 method가 Java body를 가지며
  의도적 `UnsupportedOperationException` skeleton임을 드러낸다. Java fence 안의
  bodyless concrete method declaration은 0이고, sealed interface의 두 permitted
  record가 같은 snippet에 존재한다. Package, provider SDK client, constructor
  injection과 `@Override` 위치도 실제 class/interface ownership과 정렬된다.
- **Root cause closure:** Interface 수준 signature 생략을 concrete Java declaration로
  표시하던 문법 오류가 제거됐다. Skeleton throw가 남은 distribution은 assembly에서
  금지된다.
- **남은 risk:** Accepted Phase 08~10 type/import/signature가 아직 없으므로 snippet은
  standalone 완료 구현이 아니고 exact API는 계속 `PROPOSED/OPEN`이다. Target이
  이 상태를 명시한다.

#### HG11-R006 — OPEN

- **확인한 target anchor:** §6.3 lines 578~580; §6.4 lines 602~604;
  §9.1/§9.1.1 lines 868~925; WP11-2 lines 1194~1208; §10.1 line 1316;
  canonical manifest lines 1357~1478; evidence line 1748; exit lines
  1838~1842; §16 line 1935.
- **닫힌 부분:** `tenantCredentialProvider`, `versioningRequired`,
  multipart/copy/replay policy가 config/ADR input으로 복원됐다. Immutable
  create/state CAS/publication별 409/412/deny/timeout/response-unknown,
  reread/revalidate, retry identity, current version/delete marker,
  original-byte/pointer preservation, orphan-only cleanup와 provider-native deny가
  명시됐다. Unsupported 조합은 fail-closed이고 exact choice는 계속
  `OPEN/ADR_REQUIRED`다.
- **남은 root cause:** WP11-2와 command의 pass 문장은 actual S3
  `412/409/CAS/versioning/multipart/policy/KMS case 전부 발견·통과`를 요구하지만,
  그 필수 case 집합의 stable ID/count/version/digest/report mapping이 없다.
  §11.3의 machine-readable expected set은 canonical 64개뿐이고 actual
  `S3ObjectStorageBackendAwsIT`는 그중 세 method만 가진다. Current-version
  delete marker, PutObject 409 retry와 multipart 전체 재시작의 차이,
  multipart completion/response loss/orphan cleanup, copy policy,
  replayable/non-replayable body와 tenant credential deny는 별도 required report
  identity가 아니다. 따라서 세 canonical method와 prose checklist만 green이어도
  새 operation matrix의 일부가 미발견인 false-green을 기계적으로 거부할 수 없다.
- **사람 영향:** 구현자가 409/versioning/multipart/copy/replay case 일부를 한
  existing test 안에 넣었다고 수동 주장하거나 아예 빠뜨려도 required 64/64/64
  oracle은 계속 green일 수 있다. 이는 실제 AWS conflict/rollback/security 경계를
  증명했다는 잘못된 acceptance를 허용한다.
- **Required correction:** Canonical 64 manifest는 그대로 보존하되, 별도의
  versioned `phase11-s3-actual-cases-v1` required manifest 또는 동등한 supplementary
  case catalog를 Target에 지정한다. Immutable create/state/publication ×
  409/412/deny/timeout/response-unknown, enabled/suspended/delete-marker current
  state, PutObject 대 CompleteMultipartUpload retry/re-init, multipart
  completion/cleanup, copy-policy 403/501, body replayability와 scoped credential
  deny 각각에 stable case ID, fixture/probe, independent oracle, pass criterion,
  sensitivity fault와 report ID를 부여한다. Same-run Failsafe/P11CASE audit가
  expected/discovered/passed와 missing/duplicate/failure/error/skipped/stale/
  sensitivity-missing를 non-zero fail로 판정하게 하고 evidence/exit에 두 manifest
  digest를 함께 봉인한다. AWS output은 expected semantic oracle가 아니며 exact
  production choice는 계속 owner ADR로 남긴다.
- **Target 수정 필요 여부:** `YES` — 본 recheck에서는 target을 수정하지 않았다.
- **남은 risk:** 위 correction 뒤에도 exact key encoding, versioning mode,
  multipart threshold와 retry 수치는 owner 승인 전 open이어야 한다.

#### HG11-R007 — RESOLVED

- **확인한 target anchor:** change tree lines 731~742; WP11-6 lines
  1260~1274; §11.6.1 lines 1528~1613; evidence line 1754; exit line 1852.
- **Source evidence:** Canonical Phase 11 §10의 structured log field를 event
  scope별 required set으로, 13 metric을 unit/allowed dimension과 함께, alarm
  8종과 usage/cost 8 category를 normative minimum으로 복원했다. Future exact
  schema path, canonical source blob/section, schema version/content digest와
  Ops/Security/FinOps/independent oracle owner가 지정됐다.
- **Root cause closure:** Expected/discovered typed-field 차집합, unexpected,
  duplicate key, wrong unit, forbidden field/dimension, missing failure run,
  run/collection identity와 `completeThrough` freshness가 자동 판정 대상이다.
  Integrity/security/failure alarm sampling과 AWS-output-derived schema
  golden-update도 금지된다.
- **남은 risk:** Collection delay, alarm evaluation period, retention, price,
  workload와 cap은 `OPEN/OWNER_APPROVAL_REQUIRED`다. Field completeness와
  affordability/production gate가 분리돼 있으므로 이는 의도적 residual이다.

### Correction regression NEW finding

#### HG11-N001 — §11.3 heading 변경이 보존된 원 review의 fragment link를 끊었다

- **Severity:** `LOW`
- **Finding:** 원 review line 269의
  `../phases/phase-11-human-implementation-guide.md#113-exact-test-classmethod-후보`
  fragment는 correction이 §11.3 heading을 `Canonical required test manifest와
  report oracle`로 바꾼 뒤 target에 존재하지 않는다. Target과 correction report의
  자체 local link는 모두 유효하지만, correction이 읽은 finding input의 stable
  traceability link 한 개가 회귀했다.
- **사람 영향:** 원 finding의 이전 36-case anchor에서 corrected 64-case anchor로
  바로 이동할 수 없어 review→correction audit trail이 끊긴다.
- **Target 위치/source:** Corrected target §11.3 lines 1357~1359; 원 review line
  269; correction report HG11-R003 lines 152~177.
- **Root cause:** Normative heading을 교체하면서 보존된 review가 참조하는 legacy
  fragment용 explicit compatibility anchor를 남기지 않았다.
- **Required correction:** 원 review를 덮어쓰지 말고 corrected target §11.3 바로
  앞에 stable explicit compatibility anchor
  `113-exact-test-classmethod-후보`를 추가하거나, current heading과 독립적인
  permanent section ID를 두고 legacy ID를 alias한다. 이후 target/correction/original
  review를 함께 local-fragment scan해 missing 0을 확인한다.
- **Target 수정 필요 여부:** `YES` — 본 recheck에서는 target을 수정하지 않았다.
- **Residual risk:** Alias가 있으면 의미 residual은 없다. Legacy “후보” 명칭은
  anchor compatibility일 뿐 현재 normative status가 아님을 주석으로 분명히 한다.

### Gate, 정적 검사와 scope 재검증

| 검사 | 결과 | Recheck evidence |
|---|---|---|
| Target/correction identity | `PASS` | 위 Git blob/SHA-256/lines가 correction report의 target-after와 현재 bytes에 일치 |
| 원 review prefix 불변 | `PASS` | 첫 504줄 Git blob/SHA-256가 correction 전 review와 byte-identical |
| Authority timing/status | `PASS` | AWS target만 resolved; implementation/evidence/deployment/production은 blocked/not produced/not granted |
| Entry/exit/handoff | `PASS` | Entry AND gate, offline-only last-safe point, exit AND, Phase 11→review→receipt→Phase 12/14B 단방향 유지 |
| OPEN/GATED/deferred | `PASS` | `Q-BENCH-02 OPEN`, Phase 12/13/14 gated, `Q-VAR-01 DEFERRED`; scheduler 외 status 승격 금지 |
| Security/failure/rollback | `PASS DOCUMENT / OPEN TEST ORACLE` | Tenant credential/IAM/KMS/deny, typed failure, original bytes/pointer 보존과 orphan-only cleanup 존재; machine discovery gap은 HG11-R006 |
| Canonical required manifest | `PASS` | Source/target 64 unique, 27 class, byte diff 0, SHA-256 `98fa...e0a1`; canonical duplicate 0 |
| Maven discovery/zero/stale | `PASS DOCUMENT / LIVE RED` | Failsafe/profile/Phase 11 path live 0; target이 future wiring, selected no-`-am`, slice `-am clean`, same-run report/sensitivity를 요구 |
| Java fence | `PASS` | Bodyless concrete method declaration 0; required four concrete type와 two permitted subtype 존재 |
| Target local path/fragment | `PASS` | 30/30 valid |
| Correction report local path/fragment | `PASS` | 34/34 valid |
| Preserved review local path/fragment | `FAIL REGRESSION` | 26/27 valid; 원 review line 269 legacy §11.3 fragment 1개 missing — HG11-N001 |
| GFM heading/fence | `PASS` | Target H1 1/headings 87/level jump 0/fence marker 90 closed; correction H1 1/headings 18/level jump 0/fence open 0 |
| Whitespace/EOF | `PASS` | Target/correction/original review prefix trailing whitespace 0, final LF; untracked-aware `git diff --no-index --check` diagnostic 0 |
| Implementation/AWS test | `NOT_RUN_BY_DESIGN` | Missing Phase 11 implementation과 closed entry 때문에 문서 재검증에서 green을 합성하지 않음 |
| Scope/stage/git | `PASS` | 이 append 외 scoped file write 없음; stage/commit/push/worktree 없음 |

Correction 01은 source authority, direct handoff, canonical test inventory, Maven
lifecycle, Java syntax와 operations schema 결함을 실제로 닫았다. 그러나 S3 correction이
추가한 actual-operation 필수 scenario의 기계 판정이 닫히지 않았고, heading 변경으로
원 review link 하나가 회귀했다. 두 target correction을 독립 재검증하기 전에는
recheck를 accepted할 수 없다. 이는 Phase 11 구현 실패 판정이 아니며 actual
implementation은 선행 acceptance와 AWS evidence 부재로 계속 blocked다.

RECHECK_ROUND: 01
RECHECK_VERDICT: FURTHER_CORRECTION_REQUIRED
RESOLVED_FINDINGS: HG11-R001, HG11-R002, HG11-R003, HG11-R004, HG11-R005, HG11-R007
OPEN_FINDINGS: HG11-R006, HG11-N001
TARGET_HASH_RECHECKED: d3a8733847b2e3f411229db49a944402560ecebff8110c0b035c910415f3f657
CORRECTION_REPORT_HASH_RECHECKED: 211cc2558f18a21ac2b08b4d5e2fddab1958a574953d0d7f09da7fd7f152caac

## Correction 02 읽기 전용 재검증

### Round 02 metadata와 방법

| 항목 | 값 |
|---|---|
| Recheck round | `02` |
| Recheck 시각 | `2026-07-29T03:04:11+09:00` (`Asia/Seoul`) |
| Finding scope | `HG11-R006`, `HG11-N001` |
| HEAD / branch | `7cc890ee1d0805df5ae14b633127fade4f978639` / `codex-implementation` |
| Round 01 review baseline | 첫 792줄 Git blob `a19d6cb226bbbb8963632092edd194cea5bedd84`; SHA-256 `3e0193a921cbff47a08d111fd2b83d59f6161938a3519e7a309065c95e7773b0` |
| Corrected target | Git blob `28ee2c245a1cf4f167a98a28ed7ac0cdb8277711`; SHA-256 `4dbcfef5d24328ea94645a2f2bc8b1af27d3f943669ea10a0478aa26cd72b9d9`; 2,386줄 |
| Correction 02 report | Git blob `d3d1c489f5fb996d379cb393f0ae8a1b8a6eed56`; SHA-256 `431716418d76f93ee5f07bc8da0be529d9a60678f6a0c0ff0a07a98d68d1317e`; 291줄 |
| Canonical Phase 11 | Git blob `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8`; SHA-256 `73d25b651e22bcca82dfa4431d76b5896de01750eea5d9ee9dc2af29cf488728`; 1,833줄 |
| 허용 write | 이 review 끝의 Round 02 절 append만 |

Correction report의 `ADDRESSED_FINDINGS`와 자체 `PASS` 표를 closure 근거로 그대로
채택하지 않았다. Target의 embedded canonical block과 supplementary JSON exact
bytes를 독립 추출하고, correction 01 이전 target Git object 및 canonical Phase 11
source와 비교했다. JSON parse 뒤 case ID, 지정 7개 field, operation/observation,
report/sensitivity identity와 partition을 집합으로 검사했다. 이어 same-run auditor,
future command, evidence schema, exit/traceability와 OPEN ADR 문장을 line anchor로
대조하고, preserved review에서 target으로 들어가는 legacy inbound fragment를 포함해
local GFM link를 다시 계산했다.

Phase 11 module/profile/Failsafe/test/AWS resource가 없고 entry가 닫혀 있으므로
Maven/AWS test는 실행하지 않았다. 이 round는 future executable document contract의
판정이며 provider green 또는 implementation acceptance가 아니다.

### Canonical 64와 supplementary 29 독립 검사

#### Canonical manifest 불변성

Canonical Phase 11 §12.2~§12.4에서 추출한 source-order block, correction 01 이전
target Git object `4c2bb22953eea363e9485167fd002d8e3fc2ba8b`의 embedded block과 current
target block은 모두 다음과 일치했다.

| 검사 | 결과 |
|---|---|
| Exact case lines | `64 / 64 / 64` |
| Unique class | `27` |
| Current ↔ correction 01 before | Byte diff `0` |
| Current ↔ canonical source | Byte diff `0` |
| 세 block SHA-256 | `98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1` |

따라서 correction 02가 canonical 64를 supplementary case로 교체하거나 이름을
바꾸지 않았다는 불변조건은 충족한다.

#### Supplementary catalog 구조와 coverage

Target §11.3.1 lines 1485~1888의 marker 사이 JSON을 첫 `{`부터 마지막 `}` 뒤 final
LF까지 읽어 parse했다.

| 검사 | 독립 결과 |
|---|---|
| `catalogVersion` | `phase11-s3-actual-cases-v1` |
| Declared / actual case | `29 / 29` |
| Exact JSON SHA-256 | `279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81` |
| Stable `caseId` | 29 present, unique 29, `P11S3-*` pattern 위반 0 |
| 지정 7개 field | `caseId`, `fixtureRef`, `providerProbe`, `independentOracle`, `passCriterion`, `sensitivityFaultId`, `reportId` 각각 present/non-empty 29 |
| Operation/outcome field | `operation`, `observation` 각각 present/non-empty 29 |
| Unexpected per-case field | 0 |
| Report identity | Unique 29; `reportId == "P11CASE:" + caseId` 위반 0 |
| Sensitivity identity | Present 29, unique 29 |

Coverage partition도 JSON에서 직접 계산했다.

| Required partition | 발견 |
|---|---:|
| `IMMUTABLE_CREATE × {409,412,DENY,TIMEOUT,RESPONSE_UNKNOWN}` | 5 |
| `STATE_CAS × {409,412,DENY,TIMEOUT,RESPONSE_UNKNOWN}` | 5 |
| `PUBLICATION_CAS × {409,412,DENY,TIMEOUT,RESPONSE_UNKNOWN}` | 5 |
| Enabled/suspended/current delete marker | 3 |
| PutObject retry / MPU re-init | 2 |
| Multipart completion/response-unknown/orphan cleanup | 3 |
| Copy policy 403/501 | 2 |
| Replayable/non-replayable body | 2 |
| Cross-tenant/missing-scope credential deny | 2 |
| 합계 | 29 |

각 case의 independent oracle와 pass criterion은 AWS response를 expected semantic
oracle로 쓰지 않고 original bytes/current accepted token·pointer, exact reread,
provider-native deny, orphan-only cleanup과 fail-closed를 요구한다.

#### Two-digest report/evidence 연결

다음 target anchor에서 canonical과 supplementary set을 섞지 않는 독립 AND gate를
확인했다.

- §8.2.1 line 792: 두 expected set의 fresh XML/P11CASE multiset과 두 digest를
  report auditor 입력으로 지정.
- WP11-2/7/8 lines 1203~1208, 1288, 1304: 29-case 실행, same-run identity,
  두 digest/report/sensitivity receipt를 storage/parity/evidence handoff에 연결.
- §10.1 line 1318: actual S3 `29/29/29`와 missing/duplicate/failure/error/skipped/
  stale/sensitivity-missing 0을 command pass 기준으로 지정.
- §11.3.1 lines 1864~1888: canonical `C`와 supplementary `S`를 별도 multiset으로
  정의하고 `64/64/64`, `29/29/29`, 두 digest와 run/profile/effective-POM/
  deployment identity를 AND로 판정.
- §11.7 lines 2099~2118: fresh report auditor command가 canonical/supplementary
  digest를 둘 다 받고 두 report set을 다음 clean 전 검사.
- §12.3 lines 2166~2168: evidence manifest가 두 digest, 두 fresh report set과 두
  sensitivity receipt를 별도 field로 봉인.
- §14.5 lines 2279~2287와 §16 lines 2356/2359: exit와 traceability가
  `64/64/64`와 `29/29/29`, 두 digest를 함께 요구.

Count/digest/report를 한 manifest에서 다른 manifest로 채우는 false-green은 이
구조로 차단된다.

### Finding별 closure 판정

#### HG11-R006 — OPEN

- **닫힌 부분:** Canonical 64 불변, supplementary 29 exact catalog, stable case와
  report ID, 지정 7개 field, operation/observation partition, unique sensitivity ID,
  same-run two-set audit, two-digest evidence/exit 연결은 모두 실제 target에서
  재현됐다. Exact key encoding, versioning mode, multipart threshold/part plan,
  retry count/backoff, credential mode와 production policy도 §6.3, §9.1.1과
  §11.3.1 lines 1886~1888에서 계속 `OPEN/ADR_REQUIRED`이며 AWS/service default로
  닫히지 않았다.
- **남은 root cause:** Correction report lines 155~159는 29개 negative control이
  각 case를 “red로 만든다”고 주장하지만 target의 executable PASS 식 lines
  1872~1879는 `sensitivity-missing = ∅`만 검사한다. Receipt lines 1881~1884도
  sensitivity disposition을 **기록**한다고만 한다. 주입한 `sensitivityFaultId`가
  활성화됐는데 mapped test가 계속 green인 `SURVIVED`/wrong-disposition 집합을
  계산하거나 non-zero로 거부하는 조건이 없다. §11.5/§11.7/§14.5 역시
  `sensitivity-missing`만 열거한다. 즉 29개 sensitivity receipt가 모두 존재하지만
  faulty implementation을 하나도 잡지 못해도 현재 문서식으로는
  `29/29/29`와 `sensitivity-missing=0`을 동시에 만족할 수 있다.
- **사람 영향:** 구현자는 fault ID와 receipt만 생성하고 mutation/provider fault가
  실제 red를 유발했는지 검증하지 않은 채 actual S3 negative coverage를 완전하다고
  봉인할 수 있다. Blind retry, stale-token reuse, delete-marker auto-ABSENT,
  current-object cleanup, CopyObject fallback 또는 ambient credential fallback이
  살아남는 false-green이다.
- **Required correction:** Target §11.3.1의 case schema 또는 receipt schema에
  `expectedSensitivityDisposition=RED`를 고정하고 다음 집합을 명시한다.
  `sensitivitySurvived = {caseId | fault activation receipt는 있으나 mapped case가
  expected RED/non-zero가 아님}`. PASS 식에
  `|activatedSensitivity|=|observedRed|=29`,
  `sensitivity-missing=sensitivity-survived=wrong-sensitivity-mapping=∅`를 추가한다.
  각 receipt는 case ID, unique fault ID, fault-activation evidence, normal PASS와
  sensitivity RED/non-zero disposition을 같은 source/run/profile/deployment 및 두
  manifest digest에 묶어야 한다. `Phase11S3ActualCaseCatalogContractTest`,
  fresh report auditor, §11.5/§11.7, evidence schema와 §14.5 exit가 이 negative
  set을 non-zero로 거부해야 한다.
- **Target 수정 필요 여부:** `YES` — 이 recheck에서는 target을 수정하지 않았다.
- **Residual risk:** 위 document gate가 닫혀도 실제 provider fault injection과
  independent oracle 실행 전 Phase 11 evidence는 계속 `NOT_PRODUCED`다. Production
  choice/value는 계속 owner ADR 범위다.

Correction report의 “29 unique sensitivity fault”와 “각각 red” 설명은 root cause를
인지했다는 근거지만, 사람용 target의 실행·판정 계약에 없는 조건을 대신할 수 없다.

#### HG11-N001 — RESOLVED

- **확인한 target anchor:** explicit
  `<a id="113-exact-test-classmethod-후보"></a>`가 line 1359에 정확히 한 번 있고,
  compatibility-only note line 1360과 current normative §11.3 heading line 1362가
  바로 뒤따른다.
- **Inbound 검사:** Preserved review 47 local link, correction 01 34, correction
  02 9와 target 30을 fenced code 제외 GFM heading slug/explicit id에 대조했다.
  Missing path/fragment는 각 문서와 전체에서 0이며 원 review line 269 legacy
  fragment가 alias에 resolve된다.
- **Root cause closure:** Normative heading 교체 때 immutable inbound link를
  compatibility set에서 빠뜨린 문제가 닫혔다. Alias는 한 개이고 current heading
  slug와 충돌하지 않는다.
- **남은 risk:** 없음. Visible note가 legacy “후보”를 current acceptance status로
  오인하지 못하게 하고 canonical 64와 supplementary 29가 현재 규범임을 유지한다.

### 정적 검사, scope와 verdict

| 검사 | 결과 | Evidence |
|---|---|---|
| Target/report identity | `PASS` | Recheck 시작 blob/SHA-256/lines와 correction 02 after metadata 일치 |
| Round 01 review prefix | `PASS` | 첫 792줄 blob/SHA-256 byte-identical |
| Canonical 64 | `PASS` | Current/prior/canonical 64줄, diff 0, SHA-256 `98fa...e0a1` |
| Supplementary JSON | `PASS STRUCTURE` | Parse, 29/29, exact SHA `279c...1d81`, ID/field/report/partition 누락·중복 0 |
| Supplementary negative audit | `FAIL` | Sensitivity ID/receipt presence는 요구하지만 survived/wrong disposition fail set 없음 — `HG11-R006` |
| Two-digest evidence/exit | `PASS DOCUMENT` | Command/auditor/evidence/exit/traceability가 두 set과 두 digest를 독립 AND |
| Production/Open authority | `PASS` | Exact provider/production choice와 수치가 owner ADR 전 OPEN; implementation/evidence/deployment/production 미승격 |
| Legacy alias/inbound link | `PASS` | Alias exact 1, preserved review legacy link 포함 broken 0 |
| GFM/fence | `PASS` | Target H1 1/headings 88/jump 0/fence marker 98 closed; report H1 1/headings 10/jump 0/fence marker 6 closed |
| Whitespace/EOF | `PASS` | Review prefix/target/report trailing whitespace 0, final LF; scoped/no-index `git diff --check` diagnostic 0 |
| Maven/AWS implementation | `NOT_RUN_BY_DESIGN` | Missing Phase 11 implementation과 closed entry; document gate를 provider evidence로 승격하지 않음 |
| Scope/git action | `PASS` | 이 append 외 file write 없음; target/report read-only; stage/commit/push/worktree 없음 |

Correction 02는 supplementary inventory와 legacy navigation을 실질적으로 고쳤다.
그러나 sensitivity fault의 존재와 fault를 **검출해 red가 되는 것**은 다른 조건이다.
Negative disposition audit가 target의 PASS 식에 들어오기 전에는 `HG11-R006`을 닫을
수 없다. 이는 구현 실패 판정이 아니며 Phase 11은 선행 acceptance와 actual AWS
evidence가 없어 계속 blocked다.

RECHECK_ROUND: 02
RECHECK_VERDICT: FURTHER_CORRECTION_REQUIRED
RESOLVED_FINDINGS: HG11-N001
OPEN_FINDINGS: HG11-R006
TARGET_HASH_RECHECKED: 4dbcfef5d24328ea94645a2f2bc8b1af27d3f943669ea10a0478aa26cd72b9d9
CORRECTION_REPORT_HASH_RECHECKED: 431716418d76f93ee5f07bc8da0be529d9a60678f6a0c0ff0a07a98d68d1317e

## Correction 03 읽기 전용 재검증

### Round 03 metadata와 범위

| 항목 | 값 |
|---|---|
| Recheck round | `03` |
| Recheck 시각 | `2026-07-29T03:15:07+09:00` (`Asia/Seoul`) |
| Finding scope | Round 02에서 OPEN이던 `HG11-R006`의 sensitivity negative-disposition root cause만 |
| HEAD / branch | `7cc890ee1d0805df5ae14b633127fade4f978639` / `codex-implementation` |
| Round 02 review baseline | 첫 990줄 Git blob `37bdae0a25b1461551d44ed11b1ec277dd7801bb`; SHA-256 `1bcdcf4f553297fe492de64a774afcd1999f67194a03f4831365d9a0a0a80dc5` |
| Corrected target | Git blob `782dbaeecb053378ec4941c1d1365998797c7fd4`; SHA-256 `802463c118c4cb3bb22dbc1d3232cc2a1e101caa70f8cb85a3940d31c53b09d4`; 2,470줄 |
| Correction 03 report | Git blob `ddf4e15ef7cc01b29ddfc1989d65a6160cd9ded9`; SHA-256 `cf602588d1907c209c25faeef790b60fd341fb1d9652c2ab6819e42172f2adb9`; 223줄 |
| Canonical Phase 11 | Git blob `14bb2c8f95b61ee0ca683a24c396daaa9e0e40f8`; SHA-256 `73d25b651e22bcca82dfa4431d76b5896de01750eea5d9ee9dc2af29cf488728`; 1,833줄 |
| 허용 write | 이 review 끝의 Round 03 절 append만 |

Correction report의 `ADDRESSED_FINDINGS`와 자체 `PASS` 표를 closure 근거로 그대로
채택하지 않았다. Target의 receipt schema와 audit 식을 직접 읽고 Round 02 required
correction의 각 조건을 exact anchor에서 대조했다. Canonical source §12.2~§12.4의
`same` class 표기를 실제 class로 복원해 current embedded manifest와 비교했고,
supplementary marker 사이 JSON을 parse해 Round 02 target Git object
`28ee2c245a1cf4f167a98a28ed7ac0cdb8277711`의 JSON과 byte 비교했다. 그 뒤
catalog contract, fresh auditor, §11.5, §11.7, evidence schema와 §14.5까지
negative disposition의 fail-closed 전파를 추적했다.

Target과 correction report, canonical, code/POM/test, README/progress와 다른 문서는
읽기 전용으로 유지했다. 이 round는 corrected future document contract의
판정이다. 현재 live tree에는 named Phase 11 adapter/distribution/deployment module,
세 named catalog/provider/auditor test, Failsafe 또는 `aws-local-it`/
`aws-integration` profile이 없으므로 Maven/AWS 실행은 하지 않았다. 문서 closure를
implementation/provider evidence나 Phase acceptance로 승격하지 않는다.

### Canonical 64와 supplementary 29 보존 검사

| 검사 | 독립 결과 |
|---|---|
| Canonical source/current/Round 02 case | `64 / 64 / 64`, unique class `27 / 27 / 27` |
| Canonical byte equality | Source ↔ current diff `0`; Round 02 ↔ current diff `0` |
| Canonical block SHA-256 | 세 block 모두 `98fa3535b983a9cc664bae29c10d8dd6c0e8441541c1118f6adbaedeca46e0a1` |
| Supplementary version/count | `phase11-s3-actual-cases-v1`; declared/actual `29/29` |
| Supplementary stable identity | `caseId`/`sensitivityFaultId`/`reportId` unique `29/29/29`; bad `P11CASE:<caseId>` mapping `0` |
| Supplementary required fields | 지정 7개 field와 `operation`/`observation` missing/non-empty 위반 `0` |
| Supplementary byte equality | Round 02 ↔ current JSON diff `0` |
| Supplementary JSON SHA-256 | `279cbcf4c7042dae9c485893c2b0ef3e0f367944ef834fbc4f2c1a761f291d81` |

JSON에서 operation/outcome partition도 다시 계산했다. Immutable create, state CAS,
publication CAS는 각각 `409/412/DENY/TIMEOUT/RESPONSE_UNKNOWN` 5개씩으로 15개이고,
versioned current-state 3, PutObject retry/MPU re-init 2, multipart completion/cleanup
3, copy policy 2, body replayability 2, scoped credential deny 2로 합계 29다.
Correction 03은 canonical 또는 supplementary catalog bytes를 바꾸지 않고 그
실행 receipt/audit contract만 추가했다.

### HG11-R006 — RESOLVED

#### Root cause closure

Round 02의 root cause는 fault/receipt **존재**만 검사하고 활성 fault가 mapped case를
실제로 `RED`/non-zero로 만들지 못한 `SURVIVED`, 또는 다른 case/fault/report를 붙인
wrong disposition을 계산·거부하지 않아 normal `29/29/29` false-green이
가능하다는 것이었다. 현재 Target은 다음 조건을 모두 실행 계약에 넣었다.

| Required correction | 확인한 Target anchor와 판정 |
|---|---|
| Expected RED literal | §11.3.1 lines 1851~1875가 `expectedSensitivityDisposition = RED`를 provider/environment 값이 아닌 schema literal로 고정한다. `Phase11S3ActualCaseCatalogContractTest`가 누락·완화를 provider run 전에 거부한다. |
| Exactly one receipt와 stable mapping | Lines 1859~1864가 supplementary case마다 exactly one receipt, catalog `caseId`, `P11CASE:<caseId>` report와 catalog unique `sensitivityFaultId`를 요구한다. Lines 1907~1910은 exactly one mapped receipt와 activation evidence를 `activatedSensitivity`의 조건으로 사용한다. |
| 29 activation↔observed-RED bijection | Lines 1911~1914가 normal `PASS`, sensitivity `RED`, non-zero exit인 mapped case만 `observedRed`로 계산하고 lines 1927~1929가 canonical `64/64/64`, supplementary `29/29/29`와 별도로 `|activatedSensitivity|=|observedRed|=29`를 요구한다. 두 set은 catalog `S`의 case identity와 결합되며 missing/wrong mapping도 별도 거부되므로 count만 맞춘 교체가 불가능하다. |
| 세 negative set empty | Lines 1915~1922가 `sensitivity-missing`, activated fault가 expected RED/non-zero가 아닌 `sensitivity-survived`, absent/duplicate/cross-map/catalog·identity 불일치인 `wrong-sensitivity-mapping`을 계산하고 line 1933이 세 set 모두 empty를 PASS 조건으로 둔다. |
| Receipt identity binding | Lines 1861~1883이 case/fault/report, activation evidence+digest, normal/sensitivity disposition을 같은 `sourceDigest`, `phase11RunId`, `effectivePomDigest`, profile, deployment와 canonical/supplementary 두 digest에 결합한다. Lines 1919~1922는 normal/sensitivity observation과 이 identity가 다르면 wrong mapping으로 분류한다. |
| Catalog contract와 lifecycle | §8.2.1 line 792와 §10.1 lines 1316/1318이 literal/mapping, activated/red 29와 세 negative set을 Maven discovery·same-run gate에 연결한다. Contract 누락·완화와 disposition 불일치는 non-zero/red다. |
| §11.5 false-green | Lines 1965~1968이 catalog contract, fresh auditor, identity/two-digest 결합을 함께 요구하고 survived/wrong mapping을 normal `29/29/29`로 상쇄할 수 없다고 명시한다. |
| §11.7 fresh auditor | Lines 2161~2166의 future auditor command가 두 exact digest를 받고, lines 2169~2178이 다음 clean 전 receipt 29개를 다시 읽어 activated/red `29/29`, 세 empty set과 binding을 검사하며 survived/wrong disposition은 독립 non-zero다. |
| Evidence validator | Lines 2230~2243이 두 manifest/report set, receipt 29개의 case/fault/report/activation/disposition/identity와 activated/red count, 세 empty array를 봉인한다. Lines 2258~2262는 count mismatch나 어느 negative array든 non-empty면 non-zero이며 normal report나 receipt 파일 존재로 상쇄할 수 없다고 한다. |
| §14.5 exit | Lines 2361~2365가 canonical/supplementary count·digest, literal catalog contract, activated/red 29, receipt binding과 세 empty set을 모두 exit checklist에 두고 survived 또는 wrong disposition 하나라도 있으면 exit non-zero로 고정한다. |

따라서 activation receipt 29개만 만들고 faulty implementation이 계속 green인 경우
`observedRed` 부족과 `sensitivity-survived`로 실패한다. Fault A의 red를 case B에
재사용하거나 normal/fault 결과를 다른 source/run/profile/deployment/digest에서
조합하면 catalog bijection 또는 `wrong-sensitivity-mapping`으로 실패한다. 이는
Round 02 required correction의 원 root cause를 닫는다.

#### Authority 보존과 residual

§6.3 lines 576~591은 exact Java/API, S3 lifecycle/multipart/retry/credential,
workflow/invocation mode와 production 수치를 계속 `PROPOSED/OPEN`,
`ADR_REQUIRED`, `GATED`로 둔다. §9.1 lines 919~927과 §11.3.1 lines 1941~1944는
versioning mode, multipart threshold/plan, retry 수치, key encoding과 credential을
SDK/service/provider default로 채우거나 fallback하는 것을 금지한다. §14.5 lines
2370~2371도 OPEN/GATED/deferred 보존과 `productionAuthority=false`를 요구한다.
Phase 13 optional gate와 Phase 14 production authority는 열리지 않았다.

남은 risk는 document-contract 외부다. 실제 fault activator와 independent oracle가
각 29 case를 red로 만들고 fresh auditor가 이를 판정한다는 것은 accepted
implementation과 isolated actual-AWS run에서 아직 증명되지 않았다. Entry도 선행
Phase acceptance 부재로 닫혀 있다. 이는 `HG11-R006`을 다시 OPEN으로 두는 target
결함이 아니라 구현/evidence blocker다. 이 좁은 correction 범위에서 새 regression
finding은 발견하지 않았다.

### 정적 검사, scope와 verdict

| 검사 | 결과 | Evidence |
|---|---|---|
| Frozen identity | `PASS` | Target/correction 03/canonical의 시작 hash·line과 correction metadata 일치 |
| Round 02 review prefix | `PASS` | 첫 990줄 blob/SHA-256 byte-identical |
| Manifest/catalog | `PASS` | Canonical 64 exact source/Round 02/current diff 0; supplementary 29 parse·identity·coverage와 Round 02 byte diff 0 |
| Negative audit | `PASS DOCUMENT` | Literal RED, activated↔observedRed `29/29`, missing/survived/wrong mapping empty, receipt identity/two-digest binding과 non-zero propagation 확인 |
| Production/Open authority | `PASS` | Exact choice/value는 ADR 전 OPEN, hidden default/fallback 금지, Phase 13/14 gate와 `productionAuthority=false` 보존 |
| Local GFM link | `PASS` | Target local 30/fragment 23/broken 0; correction 03 local 9/fragment 7/broken 0 |
| Heading/fence | `PASS` | Target H1/H2/H3/H4 `1/17/66/4`, jump 0, fence marker 100 closed; correction H1/H2 `1/8`, jump 0, fence marker 6 closed |
| Whitespace/encoding/EOF | `PASS` | Target/correction 03 trailing whitespace/tab/CRLF/NUL 0, UTF-8 read 성공, EOF LF |
| Maven/AWS implementation | `NOT_RUN_BY_DESIGN` | Named implementation/test/profile 0; future document gate를 provider evidence로 승격하지 않음 |
| Scope/git action | `PASS` | 이 절 append 외 write 없음; target/report와 타 문서·코드 read-only; stage/commit/push/worktree 수행 안 함 |

Correction 03은 `HG11-R006`의 receipt-presence false-green을 literal disposition,
same-run identity binding, activation↔red bijection과 세 negative set의
non-zero gate로 실제 Target에서 닫았다. Target의 Phase 11 구현/evidence 상태는
계속 blocked/not-produced이며 이 recheck는 production authority를 부여하지 않는다.

RECHECK_ROUND: 03
RECHECK_VERDICT: ACCEPTED
RESOLVED_FINDINGS: HG11-R006
OPEN_FINDINGS: NONE
TARGET_HASH_RECHECKED: 802463c118c4cb3bb22dbc1d3232cc2a1e101caa70f8cb85a3940d31c53b09d4
CORRECTION_REPORT_HASH_RECHECKED: cf602588d1907c209c25faeef790b60fd341fb1d9652c2ab6819e42172f2adb9
