# Phase 00 사람용 구현 가이드 correction 01

```yaml
phase: "00"
correction_round: "01"
correction_kind: HUMAN_GUIDE_FINDING_CORRECTION
correction_status: COMPLETE_PENDING_INDEPENDENT_REVIEW
correction_date: 2026-07-29
correction_timezone: Asia/Seoul
target: ../phases/phase-00-human-implementation-guide.md
review_input: ../reviews/phase-00-review.md
scope:
  allowed:
    - docs/implementation/human-guides/phases/phase-00-human-implementation-guide.md
    - docs/implementation/human-guides/corrections/phase-00-correction-01.md
  implementation_changed: false
  review_input_changed: false
target_sha256_before: bf6a35990f0554ffee7206e19f18213b601164ba11830938944c298c063d5c16
target_sha256_after: c1d18c0f118f840c272c1737da678dc83769ed13ceca02227ec525edf4bd2f7c
review_sha256_before: 63f733a5b6f61e41ea3befa4c8340dbd43f9755f4dc6fbb9a17377543e99d332
review_sha256_after: 63f733a5b6f61e41ea3befa4c8340dbd43f9755f4dc6fbb9a17377543e99d332
addressed_findings:
  - P00-HG-R-001
  - P00-HG-R-002
  - P00-HG-R-003
  - P00-HG-R-004
  - P00-HG-R-005
  - P00-HG-R-006
deferred_findings: []
```

이 report는 [독립 review](../reviews/phase-00-review.md)의 target 수정 `YES` finding 6개를
[Phase 00 사람용 guide](../phases/phase-00-human-implementation-guide.md)에 교정한 기록이다.
구현 acceptance, scheduler status 변경 또는 Phase 01 entry authorization이 아니다.

## 1. 입력, authority 역할과 교정 원칙

### 1.1 직접 사용한 입력

| 입력 | 사용한 역할 | SHA-256 |
|---|---|---|
| [Master Design](../../../master-design.md) | 현재 invariant, Phase 13/14 gate와 reproducibility authority | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` |
| [현재 Domain 지도](../../../domain-design.md) | 최신 `REVIEW` domain 책임/package 상위 지도 | `3add42ca7116d9a38f465e71d6e36034a977888d6b3975a5e56d28e9b9e3ff73` |
| [현재 Architecture 지도](../../../architecture-design.md) | 최신 `REVIEW` 전체-product Maven/package 추천 지도 | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` |
| [2026-07-26 Domain 고정 입력](../../../2026-07-26-domain-design.md) | 사용자가 고정한 dated domain source | `1b56cf8b508755f9a61c6aa5bf447e8ff2d4cae0695fc797c185c453919cdbac` |
| [2026-07-26 Architecture 고정 입력](../../../2026-07-26-architecture-design.md) | 사용자가 고정한 dated Maven/package/customer source | `1162d7c22bdd506836d699ac38ea7a95ff06d7d45de34107676db4e537a049ed` |
| [통합 구현 설계](../../../architecture-domain-implementation-design.md) | Concrete implementation tree, DAG, profile/catalog와 ambient rule | `883af86062254e7b6984a0716e102bc25be614ef6096bc451e45b45486f11571` |
| [Open-question register](../../../master-design-open-questions.md) | OPEN/GATED/DEFERRED와 owner 상태 | `b16bd877065d70919991e17031b8be8186acb40c53c39652acd8212a294d126b` |
| [Implementation README](../../README.md) | Canonical Phase/review index와 handoff baseline | `6454238185af7b7c420f468adf42609a0ec045d6c70c16cc7601f0342fa74358` |
| [Master Realization Plan](../../master-realization-plan.md) | Evidence DAG, immutable manifest와 `target/` 금지 | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` |
| [Phase 00 원본](../../phases/phase-00-build-architecture-skeleton.md) | Reviewed Phase 00 tree, WP, package/customer/ambient oracle | `0ad01e21a94ac543486a53c0ed0a256b4137e673bbae1be4d959ebc46dcefd27` |
| [Phase 00 원본 review](../../reviews/phase-00-review.md) | `P00-R-001`~`006` correction과 residual blocker | `db4f1f8c4d178c99d82597085155a1fbfac223659944f3b092e66ae5b7f6fed8` |
| [공식 execution progress](../../execution-progress-and-results.md) | 교정 시점 live scheduler state | `24d61971ad268992d3c0d76b4e06b1dfb1d9125935467ae02574260cc41a3f1d` |
| [Human-guide review](../reviews/phase-00-review.md) | 이 round의 finding/source/root cause/required correction | `63f733a5b6f61e41ea3befa4c8340dbd43f9755f4dc6fbb9a17377543e99d332` |

현재 상위 Domain/Architecture는 최신 전체-product `REVIEW` 지도, dated 문서는 사용자 고정
입력, implementation 문서는 Phase 00의 실행 가능한 safe subset과 evidence baseline으로
구분했다. Current Architecture의 profile별 module 추천과 reviewed implementation baseline의
`rpdptw-capabilities` + 단일 `rpdptw-profile-catalog` 배치는 서로 다르다. 이 차이를 숨기거나
임의 해결하지 않고 H3 승인 및 accepted Phase 00 coordinate/package manifest 전에는
concrete 이름을 `PROPOSED`로 유지했다.

### 1.2 Live inventory 경계

교정 직전 관측값은 `2026-07-29T02:02:30+09:00`, branch
`codex-implementation`, `HEAD 7cc890ee1d0805df5ae14b633127fade4f978639`, tree
`a63fa3b93ca206298ac6d467e02e1f3503cee30e`다.

| Fingerprint | SHA-256 | 의미 |
|---|---|---|
| `git status --porcelain=v1 -z --untracked-files=all` | `7087b4b7ce5505dc6e8c2774f39af185b859c9651d0d9541ba88b6dd5ff54f84` | Path/state snapshot |
| `git diff --binary HEAD` | `500c4ef4e09165f685d0b446b97b4a5352e863dd1c08c6de82b89a1aab212836` | Tracked dirty bytes |
| Sorted untracked `path + SHA-256` manifest | `a02ef644b8ecb791a2663811ee1151305cb7f18d5f2bc7aaf0ffc5699e9be2fa` | Untracked bytes |
| Phase 00 implementation non-`target` inventory | `66e6529a5eb5276f01d137718b3950c18eb89664da5dc000563b04e94f3c0292` | POM/wrapper/build/rpdptw/legacy candidate bytes |

이 값은 **UNCOMMITTED / UNAPPROVED correction-pre-edit snapshot**이다. Target/report 교정으로
whole-worktree digest가 달라지는 것은 정상이고, 어느 digest도 acceptance evidence가 아니다.
Live candidate는 POM 13개, main Java 33개, test Java 16개와 wrapper를 가졌지만 공식 상태는
`CHANGES_REQUIRED_FIX_01_IN_PROGRESS`, 기존 bundle은 defective/superseded,
acceptance는 `REJECTED_PENDING_FIX_01_REGENERATION`이다.

Shared checkout의 implementation task는 교정과 동시에 계속 진행됐다. Validation 중 같은
non-`target` implementation manifest가 `62182ed1...05d33`, 이어
`2026-07-29T02:11:07+09:00`에 `f394c28e...b9516`으로 다시 변했다. 이 correction은 그
파일을 수정하지 않았으며, 변화 자체가 timestamp 없는 “CURRENT”를 acceptance identity로
사용할 수 없다는 근거다.

## 2. Finding별 교정

### P00-HG-R-001 — Reviewed package/file skeleton

**변경 위치:** Target metadata/§2.1~§2.2, §6.2, §9.1, WP00-3, §14.4, §15.1, §16.

**변경:** `build/pom.xml`, `rpdptw/pom.xml`, `legacy/pom.xml` aggregator를 추가하고 core의
`input/domain/normalization/travel/propagation/evaluation.{api,runtime,insertion}`, solver의
`portfolio/search/state/termination`, verification/result, application
`port.in/port.out/service/execution`, 단수 `com.ronext.rpdptw.capability`를 exact
`package-info.java` inventory로 교정했다. Java TEST-ONLY 후보에는 실제 Java 25
compilation-unit package/import를 보강했다. `-pl ... -am`은 선택 reactor subset이지 root
acceptance가 아니며 test-fixture producer green과 consumer test-jar wiring을 구분했다.

**이유와 source:** Dated Architecture §2.1/§2.3, 통합 구현 설계 §3.2~§3.6, Phase 00 원본
§6/§6.1과 review finding `P00-HG-R-001`. Current 상위 Architecture와의 profile module
차이는 H3/accepted manifest로 남겼다.

### P00-HG-R-002 — Endpoint/failure-stage legacy oracle

**변경 위치:** Target §5.1, WP00-1, §11.4, §12.1 H0, §14.2, §16.

**변경:** `HEAD` pre-move source는 `git show <commit>:<path>`로 읽고 live moved source와
분리했다. Public API와 worker를 method/path/JSON parse/parsed validation/provider 단계로
나눈 matrix를 추가했다. `HEAD` source branch상 API malformed JSON은 redacted `500`,
parsed-invalid request는 `400`, API fallback은 `404`, worker non-POST는 `405`, missing
result는 `202`다. Expected status/content-type/body bytes는 pre-move 실제 capture 뒤에만
golden으로 확정하게 했다. 더 나은 HTTP semantics는 별도 승인 변경으로 분리했다.

**이유와 source:** `HEAD`의 `OptimizationApiController`/`OptimizationWorkerController`,
Phase 00 원본 WP-00-1, 원본 review `P00-R-005`, human review `P00-HG-R-002`.

### P00-HG-R-003 — Clean-safe evidence와 corruption-resistant seal

**변경 위치:** Target WP00-0, WP00-2, WP00-5, §11.4~§11.5, §12.2, §14.5, §18.

**변경:** `target/phase-00-evidence`를 disposable staging으로 제한하고 acceptance direct
reference를 금지했다. 승인된 evidence root는 `clean` 영향 밖의
content-addressed/digest-protected location이며 위치/retention/ACL은 OPEN/사람 승인으로
남겼다. Dirty implementation은 commit 또는 content-addressed source archive로 먼저
봉인한다. Seal은 nested leaf의 canonical relative path, byte length와 SHA-256을 stable
order로 기록하고 nested omission, symlink, missing file, path collision, same
path/different bytes, one-byte mutation과 self-reference를 non-zero로 거부한다.

**이유와 source:** Master Realization Plan §9.1/§9.4, Phase 00 원본 WP-00-0/2/5와 §12.2,
원본 review `P00-R-003`, human review `P00-HG-R-003`.

### P00-HG-R-004 — Customer executable rule과 identity data 경계

**변경 위치:** Target §9.3, WP00-4, §11.4, §12.1 H3/H4, §14.4/§14.6, §16.

**변경:** Generic core/solver/verification과 승인된 application 범위의 customer-name
conditional/executable implementation/package를 금지했다. Profile catalog와 승인된 adapter
authorization은 exact customer/profile/version과 authorization identity data를 허용하되
arbitrary executable rule/solver branch를 금지했다. Approved token/location manifest,
AST/bytecode/package rule, conditional/string/switch negative fixture와 positive catalog
fixture를 요구했다.

**이유와 source:** Dated Architecture §2.6~§2.7, 통합 구현 설계 §3.4/§3.6, Phase 00
원본 WP-00-4 step 8, 원본 review `P00-R-004`, human review `P00-HG-R-004`.

### P00-HG-R-005 — HEAD baseline과 unapproved live drift 분리

**변경 위치:** Target metadata/서문, §2.1~§2.2, §5.1~§5.2, §6.1, §17~§18.

**변경:** `HEAD` commit/tree baseline과 correction-time live drift를 별도 표와 digest로
기록했다. 깨진 root `src/**` link 5개를 제거하고 baseline은 `git show`, live는 inventory가
발견한 `legacy/gcp-placeholder/**` link로 읽게 했다. Progress fingerprint mismatch 때 같은
task owner에게 합류하거나 overlap이면 STOP하는 scheduler 판정 절차를 추가했다.
`NOT_STARTED`는 `HEAD` baseline 과거 상태로만 남기고 live
`CHANGES_REQUIRED_FIX_01_IN_PROGRESS`/rejected acceptance와 혼합하지 않았다.

**이유와 source:** Live progress §10.1, 원본 review `P00-R-006`, human review
`P00-HG-R-005`, correction-time Git/POM/Java inventory.

### P00-HG-R-006 — Core ambient nondeterminism guard

**변경 위치:** Target §9.3, WP00-4, §11.4, §12.1 H3/H4, §14.4/§14.6, §16.

**변경:** Core의 environment/system-property semantic read, system/ambient clock,
unseeded/global random, mutable static registry를 각각 rule과 known-negative fixture로
추가했다. Expected rule ID와 offending symbol을 assert하며 injected clock/random/config는
후속 owner의 명시적 contract일 때만 허용하고 Phase 00 fake production default를 금지했다.
Surefire/Failsafe naming/binding, fresh report, non-zero expected test count를 exit oracle에
추가했다.

**이유와 source:** Phase 00 원본 §3.2 item 11/§8.5, Master §13.2, 통합 구현 설계 §4.3/§19.1,
human review `P00-HG-R-006`.

## 3. 보존한 gate, blocker와 last safe point

- RPDPTW pair atomicity, route/bank XOR, delivery-only physical-visit 의미와 두 verifier
  독립성을 바꾸지 않았다.
- Phase 00은 build/package/architecture/legacy characterization만 소유한다. Phase 01
  canonical type, Phase 07 verifier 구현, provider/deployment와 production code를 만들지 않았다.
- `Q-BENCH-02`는 **OPEN / EXPERIMENT_REQUIRED**, `Q-VAR-01`은 **DEFERRED**다.
- Phase 13은 유효한 Phase 14A `ALNS_BENCHMARK_ACCEPTANCE_RECEIPT`와 `C-17` 승인 전
  **GATED**다. Phase 14B는 Phase 11/14A evidence, official values와 명시적 production
  authority 전 **GATED**다.
- Public API/schema, exact ArchUnit/plugin version, Jackson convergence, Shade collision,
  archive timestamp, H3 package manifest와 evidence store/retention/ACL은 사람 승인 전
  PROPOSED/OPEN이다.
- H0~H5와 각 승인 전 마지막 안전 지점, broad reset/clean 금지, unrelated work 보존을
  유지했다.
- `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`, 독립 implementation review와 post-review
  receipt가 모두 accepted되기 전 Phase 01은 blocked다.

## 4. 검증 결과

| 검사 | 결과 |
|---|---|
| Target/review/report non-empty와 SHA-256 | PASS |
| Review hash before/after 불변 | PASS; `63f733...d332` |
| Target hash before/after 변화 | PASS; `bf6a35...c16` → `c1d18c...f7c` |
| Relative link target | PASS |
| Relative fragment/GFM anchor | PASS; fragment link 0개 |
| Heading hierarchy | PASS; fenced code comment 제외 skip 0 |
| Code fence | PASS; target 36개, report 2개, 모두 closed |
| Trailing whitespace | PASS; match 0 |
| EOF newline/CR | PASS; 두 파일 LF newline, CR 0 |
| Required finding ID와 target traceability | PASS; `P00-HG-R-001`~`006` |
| Scoped `git diff --check` | PASS; output 0 |
| Untracked-file `git diff --no-index --check` 보조 검사 | PASS; whitespace diagnostic 0, added-file exit `1` expected |
| Unrelated tracked diff fingerprint | PASS/preserved; `500c4ef4e09165f685d0b446b97b4a5352e863dd1c08c6de82b89a1aab212836` |
| Concurrent implementation inventory | OBSERVED, NOT ACCEPTED; `66e652...` → `62182e...` → `f394c2...` |
| 변경 scope | PASS; target guide와 이 correction report만 의도적으로 수정/생성, stage/commit 없음 |

## 5. Residual risk

1. Current Architecture의 profile별 module과 reviewed implementation baseline의
   capabilities/profile-catalog 배치 차이는 H3가 아직 승인하지 않았다. Accepted manifest 전
   concrete 이름은 PROPOSED다.
2. Evidence store의 실제 위치, retention과 ACL은 Build·Quality/Release owner 결정 전
   OPEN이다. `target/`은 acceptance source가 될 수 없다.
3. Legacy matrix의 source-derived status branch는 pre-move exact response-byte capture를
   대신하지 않는다. Field initialization/test seam이 timing과 failure mapping을 바꾸는지
   이동 전후 golden으로 확인해야 한다.
4. Customer synonym, reflection과 dynamic service loading은 정적 scan으로 완전 증명되지
   않는다. Coverage manifest와 독립 change review가 계속 필요하다.
5. Live drift digest는 correction-pre-edit snapshot이다. Shared checkout이 계속 변할 수
   있으므로 implementation owner가 immutable source를 봉인하기 전 acceptance에 사용하지 않는다.
6. Official implementation fix 01과 evidence regeneration/review 02는 이 문서 correction의
   범위 밖이며 현재도 Phase 01 blocker다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: P00-HG-R-001,P00-HG-R-002,P00-HG-R-003,P00-HG-R-004,P00-HG-R-005,P00-HG-R-006
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: bf6a35990f0554ffee7206e19f18213b601164ba11830938944c298c063d5c16
TARGET_HASH_AFTER: c1d18c0f118f840c272c1737da678dc83769ed13ceca02227ec525edf4bd2f7c
