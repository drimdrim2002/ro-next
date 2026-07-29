# Phase 02 사람용 구현 가이드 correction 01

## 1. Metadata

| 항목 | 값 |
|---|---|
| Correction round | `01` |
| Correction 시각 | `2026-07-29T02:12:00+09:00` (`Asia/Seoul`) |
| 역할 분리 | 작성자·reviewer와 분리된 새 correction 세션 |
| Target | [phase-02-human-implementation-guide.md](../phases/phase-02-human-implementation-guide.md) |
| Finding input | [phase-02-review.md](../reviews/phase-02-review.md) |
| 허용 변경 | Target guide와 이 correction report만 |
| 금지 범위 준수 | Review, README/progress, 인접 guide/review/correction, canonical 문서, Java/POM/test/deployment를 수정하지 않음 |
| 구현/acceptance 주장 | 없음. Phase 02는 `BLOCKED_BY_ENTRY_GATES / NOT_ACCEPTED` |
| Finding 결과 | `5/5 ADDRESSED`, deferred `NONE` |
| Target 문서 상태 | `CORRECTED_ROUND_01 / AWAITING_INDEPENDENT_REVIEW` |

이 correction은 사람용 가이드의 계약과 판정 절차만 고친다. Live Phase 00 artifact를
승인·rollback·삭제하거나 scheduler registry를 바꾸지 않으며 Phase 02 Java 구현,
test execution, evidence 또는 acceptance receipt를 만들지 않는다.

## 2. Hash와 correction baseline

SHA-256은 file bytes, Git blob은 `git hash-object` 결과다.

| File | SHA-256 before | SHA-256 after | Git blob before | Git blob after |
|---|---|---|---|---|
| Finding input review | `dcf7de5ee30442b7c16e29dc60896d9b3b31de27afa1cebdc2c7a8cd3f083a72` | `dcf7de5ee30442b7c16e29dc60896d9b3b31de27afa1cebdc2c7a8cd3f083a72` | `c804f3c8e2ad9ec30166b7aca32af6abebaf5627` | `c804f3c8e2ad9ec30166b7aca32af6abebaf5627` |
| Target guide | `93c53ea2055bea89df4cb70dddc95b66b035f01bc392764d59d1ec86dea87f3a` | `123405df8823ef901e6a4f6ef8a77e18208965f40c25e3385cbdccc2e9f57ace` | `cd2357922066d0508b1146e36734d0d431e9b31a` | `da1937c5bc600144789838e7636f7fda794e7878` |

Target before hash는 review metadata의 reviewed bytes와 직접 재계산 결과가 일치했다.
Review는 correction input으로만 읽었고 bytes가 바뀌지 않았다.

### 2.1 Authority와 시점 구분

- 현재 [Master](../../../master-design.md), [Domain map](../../../domain-design.md),
  [Architecture map](../../../architecture-design.md)은 최신 상위 의미·gate·module
  지도다. 모두 `REVIEW`이며 구현 acceptance authority가 아니다.
- 사용자 고정 입력인 [2026-07-26 Domain](../../../2026-07-26-domain-design.md)과
  [2026-07-26 Architecture](../../../2026-07-26-architecture-design.md)는 Phase 02의
  detailed domain/Java baseline으로 보존했다.
- [Implementation README](../../README.md), [Master Realization Plan](../../master-realization-plan.md),
  canonical [Phase 02](../../phases/phase-02-prepared-travel-immutable-problem.md)와
  [original review](../../reviews/phase-02-review.md)는 Phase별 entry/exit/evidence
  baseline으로 사용했다.
- Live checkout은 implementation inventory일 뿐 source/acceptance authority가 아니다.
  명시적 replacement 없이 semantic 선택이 필요한 충돌은 `OPEN`/사람 승인/last safe
  point로 남겼다.

### 2.2 Live inventory correction snapshot

Target §5.1.2에 다음 미커밋·미승인 snapshot을 기록했다.

| 항목 | 값 |
|---|---|
| Snapshot 시각 | `2026-07-29T02:03:39+09:00` |
| HEAD baseline | `7cc890ee1d0805df5ae14b633127fade4f978639`; POM 1, main Java 6, test Java 1 |
| Live root POM blob | `1dc675ba17b7f2202f34a22131f152cc2868b075` |
| Live progress blob | `36afdfde57610b8ec4a31f1f4d9b18f786d6bf1d` |
| Live core POM blob | `827e3cd913c16da3cf0011b964f803b33d5db8ef` |
| Live test-fixtures POM blob | `e033f9cda6de908a25431b6dfce5b0b0d8cd3ac1` |
| Inventory manifest SHA-256 | `c3ad0ffb4e85a30d99e1606411a36e0d4ebd16582e827437d08c6a591098fdc9` |
| Live counts | POM 13; RPDPTW main Java 23 전부 `package-info.java`; build test Java 11; legacy Java 15 |
| Status 해석 | Phase 00 `CHANGES_REQUIRED_FIX_01_IN_PROGRESS / NOT_ACCEPTED`; Phase 02 `BLOCKED_BY_ENTRY_GATES / NOT_ACCEPTED` |

이 snapshot은 review 시점 inventory와도 drift했으므로 target에 재-snapshot stop rule을
추가했다. Live artifact 존재를 Phase 00 acceptance로 올리지 않았고 HEAD single-JAR
baseline과 합치지 않았다.

## 3. Finding별 correction

### HG-P02-R01 — HEAD baseline과 live drift 분리

**변경 위치**

- Target §1.1: current Domain/Architecture map과 사람용 review를 source table에 추가하고
  root POM/progress fingerprint를 correction 시점 bytes로 갱신했다.
- Target §4.1~§4.2: 현재 상위 지도와 dated baseline의 읽기 순서, Phase 00
  `IN_PROGRESS / NOT_ACCEPTED`, 재-snapshot stop rule을 추가했다.
- Target §5.1.1~§5.1.2: committed HEAD와 live uncommitted snapshot을 별도 표로
  나누고 timestamp/blob/inventory digest, POM/Java/test counts와 fixture DAG를 기록했다.
- Target §9 서문, WP-02.0, WP-02.6, §10.4, §12·§16: “module 없음/placeholder 1건”
  표현을 live skeleton/Phase 00·legacy test/Phase 02 test 0건 구분으로 바꿨다.

**이유**

구현자가 active Phase 00 remediation을 중복 생성·삭제하거나 unaccepted skeleton을
accepted module로 오인하는 양쪽 위험을 막기 위해서다.

**Source 근거**

- [Execution progress §5와 §10.1](../../execution-progress-and-results.md)
- Live root/core/build/test-fixtures POM과 package/test inventory
- HEAD `7cc890ee1d0805df5ae14b633127fade4f978639`
- Current Architecture §2/§19와 target의 fingerprint drift rule

**보존 gate와 residual risk**

Phase 00 evidence/review/receipt와 scheduler transition은 계속 필수다. Live worktree는
correction 뒤에도 변할 수 있으므로 구현 직전 새 timestamp/digest가 accepted receipt의
source snapshot과 일치하지 않으면 중지한다.

### HG-P02-R02 — Delivery-only logical pickup의 비물리 의미

**변경 위치**

- Target §3.1 그림에서 logical pickup을 `PhysicalLocationId(L7)` mapping에서 제거하고
  `LogicalInitialLoad`의 no-node/no-location/no-travel 의미를 별도 표시했다.
- Target §3.2~§3.3과 §6.3~§6.4에 pair ownership과 physical collection을 분리하고
  prefix token 대 explicit initial-load 표현을 `P-02 PROPOSED/OPEN`으로 유지했다.
- Target §8.6~§8.7에 실제 Java 문법의 sealed proposed pickup semantics를 두고
  logical/physical validation 순서를 분리했다.
- Target WP-02.5, §10.2, §11.2, §13.2와 §15에 `M`/`M²`/travel call/stop/service
  증가 0 contract와 exact test/evidence를 추가했다.

**이유**

Delivery-only request마다 가짜 depot pickup node/location/visit을 만들어 travel,
stop, service, zone과 Phase 03 initial load를 오염시키는 구현을 차단하기 위해서다.

**Source 근거**

- [Master §5.2](../../../master-design.md#52-service-meaning)
- [2026-07-26 Domain §2.3](../../../2026-07-26-domain-design.md#23-delivery-only와-real-pickup-delivery)
- [Phase 01 §7.2](../../phases/phase-01-canonical-input-normalization.md#72-canonical-request와-service-pattern)
- [Integrated §6.4](../../../architecture-domain-implementation-design.md#64-dense-identity와-immutable-problem)

**보존 gate와 residual risk**

비물리 의미는 확정이지만 exact internal representation, 이름과 visibility는 Architecture와
Phase 01~03 owner 승인 전 public API가 아니다. Canonical 자료의 skeletal
`pickupNodeId` 예를 그대로 public signature로 복사하지 않는 stop rule을 유지했다.

### HG-P02-R03 — Reactor/fixture/command와 executable pass oracle

**변경 위치**

- Target §5.1.2와 §5.2: live `build/test-fixtures --test→ rpdptw-core` edge를 기록하고
  core-local helper는 core `src/test`, cross-module fixture는 downstream consumer가
  소유하도록 분리했다.
- Target WP-02.0: task, Phase 00/01 receipt, policy, ADR와 evidence plan의 actual
  path/digest를 검사하는 fail-closed command로 교체했다.
- Target WP-02.1~5: accepted `./mvnw`, `-am`, `clean`, strict specified/zero-test
  옵션을 사용하도록 바꾸고 current와 future 실행 조건을 분리했다.
- Target WP-02.6: core → downstream architecture → root 순서, Surefire/Failsafe
  discovery 조건과 run별 fresh report 보존을 명시했다.
- Target §10.2: complete exit-required method 58개를 table과 PSV에 동일하게 고정하고,
  report 0개/test 0건/failure/error/skipped/missing/duplicate를 non-zero로 만드는
  fresh XML checker를 제공했다.
- Target §10.4, §11.2, §13.3과 §15: stale report/local repository/cycle/fixture
  leakage false-green을 exit/evidence/traceability에 연결했다.

**이유**

Core가 downstream fixture test-jar를 역의존해 cycle을 만들거나 로컬 repository의
stale artifact로 통과하는 일을 막고, Maven exit `0`과 Phase 02 required method
실행 성공을 구별하기 위해서다.

**Source 근거**

- [Canonical Phase 02 WP-02-1~6와 §10](../../phases/phase-02-prepared-travel-immutable-problem.md)
- [Current Architecture §19.1](../../../architecture-design.md#191-reactor-build-order)
- [Master Realization Plan §8.2](../../master-realization-plan.md#82-필수-test-종류)와
  [§9.1](../../master-realization-plan.md#91-pre-review-evidence-manifest)
- Live root/core/build/test-fixtures/architecture-rules POM

**보존 gate와 residual risk**

Phase 00 acceptance 전 wrapper/DAG는 authority가 아니며 current POM에는 Failsafe execution이
없다. `*IT`를 추가하려면 plugin/version/module owner를 먼저 승인해야 한다. Report checker는
구현 때 source file과 runtime/toolchain digest로 봉인하고 실제 failure sensitivity를 독립
review해야 한다.

### HG-P02-R04 — Missing coordinate와 non-self zero boundary

**변경 위치**

- Target §3.4와 §7.2: valid co-located non-self zero 보존과 negative/overflow rejection을
  분리했다.
- Target §8.7: missing coordinate에서 generator/cache/reverse lookup 전에 reject하는
  pseudocode와 no-partial-publication을 추가했다.
- Target WP-02.3: Great Circle approval 전 검증 가능한 boundary subset과 승인 뒤 numeric
  success path를 분리하고 sensitivity oracle을 추가했다.
- Target §10.2: `rejectsMissingDistanceWhenCoordinateIsAbsent`,
  `preservesValidNonSelfZeroDistanceAndTimeForCoLocatedLocations`,
  `rejectsNegativeProvidedDistanceOrTime`을 exit-required manifest에 넣었다.
- Target §13.2와 §15: exit checklist와 source→test/evidence traceability를 보강했다.

**이유**

Missing coordinate를 zero/reverse/cache/generator로 보완하는 결함과 valid zero를
positive-only validation으로 거부하는 반대 결함을 모두 false-green 없이 잡기 위해서다.

**Source 근거**

- [Master §8](../../../master-design.md#8-directed-distancetime-matrix-계약)
- [2026-07-26 Domain §6](../../../2026-07-26-domain-design.md#6-travel-preparation)
- [Canonical Phase 02 §7.2](../../phases/phase-02-prepared-travel-immutable-problem.md#72-absence-semantics)
- [Canonical Phase 02 §14 `REQ-P02-D-GEN`](../../phases/phase-02-prepared-travel-immutable-problem.md#14-source--requirement--test-traceability)

**보존 gate와 residual risk**

Missing-coordinate rejection과 provided zero boundary는 임의 Earth model 없이 검증할 수
있지만 generated distance numeric green과 Phase exit는 approved function/version/reference
vector 전 계속 blocked다.

### HG-P02-R05 — Unit-bearing demand type 보존

**변경 위치**

- Target §6.3과 §8.6: raw `long demandWeight/demandVolume`을 Phase 01
  `MilliKilograms`/`MilliCubicMeters` 재사용 후보로 교체하고 vehicle capacity에도 같은
  차원 type을 요구했다.
- Target §8.7과 WP-02.5: Phase 02 renormalization 0과 pickup semantics/reference freeze
  순서를 명시했다.
- Target §10.2: value-preservation test와 compiled signature의 distinct unit/no-raw-long
  architecture test를 required manifest에 추가했다.
- Target §11.2, §13.2와 §15: evidence, exit checklist와 traceability에 unit identity와
  weight↔volume swap 방지를 연결했다.

**이유**

Phase 01의 scale `3`/`FLOOR` provenance가 problem boundary에서 primitive로 약해져
raw kg/CBM 재주입, 재반올림 또는 weight-volume swap이 가능한 API가 되는 것을 막기
위해서다.

**Source 근거**

- [Master §7.2](../../../master-design.md#72-fixed-point와-checked-arithmetic)
- [Phase 01 §7.3](../../phases/phase-01-canonical-input-normalization.md#73-numeric-value)
- [2026-07-26 Architecture §2.4](../../../2026-07-26-architecture-design.md#24-먼저-알아야-할-immutable-artifact)
- [Integrated §4.3 package rule](../../../architecture-domain-implementation-design.md#43-package-규칙)

**보존 gate와 residual risk**

Unit/scale/rounding identity는 OPEN이 아니지만 exact Java type 이름과 visibility는
`PROPOSED`다. 이름 변경은 reviewed equivalent mapping으로만 허용하며 primitive 혼용의
근거가 되지 않는다.

## 4. 보존한 invariant, boundary와 gate

- Directed physical-location key, integer provided `D/U`, `C` non-authority, self
  `0/0`, provided priority, Great Circle `HALF_UP`, generated time `CEILING`, missing-only
  `45`, asymmetry와 no lazy/reverse/symmetric/provider fallback을 유지했다.
- Phase 01 immutable artifact 단일 source, Phase 02 all-or-nothing freeze, no mutable
  alias, exact problem/travel fingerprint binding과 Phase 03/07 read-only equality를
  유지했다.
- Route propagation/state/evaluation/search/verifier/provider/public API는 Phase 02
  non-scope이며 완성 코드로 당기지 않았다.
- Pre-review manifest → independent review → post-review acceptance receipt →
  scheduler transition의 evidence DAG, rollback과 last-safe-point를 유지했다.
- Great Circle exact function, typed source policy, fingerprint encoding, official
  snapshot, public API와 performance sizing은 승인 전 `OPEN/BLOCKER`다.
- `Q-BENCH-02 OPEN — EXPERIMENT_REQUIRED`, `Q-VAR-01 DEFERRED`, multi-trip deferred,
  Phase 13의 Phase 14A acceptance receipt + `C-17` gate와 Phase 14B production authority를
  그대로 보존했다.

## 5. 검증 결과

| 검사 | 결과 | Evidence/해석 |
|---|---|---|
| Finding coverage | `PASS` | `HG-P02-R01~R05` 각각 target section, source, test/evidence와 연결 |
| Review immutability | `PASS` | Review SHA-256 before/after 동일 |
| Target hash | `PASS` | Before/after SHA-256와 Git blob 기록 |
| Required method table↔PSV | `PASS` | 58/58, unique 58, missing/extra/duplicate 0 |
| XML checker syntax | `PASS` | Embedded Python compile check 성공 |
| Relative Markdown path | `PASS` | Target+report 재검사에서 missing 0 |
| GFM fragment/explicit anchor | `PASS` | Target+report 재검사에서 missing 0 |
| Heading structure | `PASS` | Level jump와 duplicate base slug 0 |
| Fence | `PASS` | 두 파일 모두 짝수 |
| Trailing whitespace/tab/NUL/EOF | `PASS` | Match 0, NUL 0, 각 file newline EOF |
| Scoped `git diff --check` | `PASS` | 허용 두 path exit `0` |
| Untracked-aware whitespace | `PASS` | 각 file `git diff --no-index --check /dev/null <file>` whitespace diagnostic 0; 새 file/content이므로 exit `1` 자체는 expected |
| Maven/Java implementation test | `NOT_RUN_BY_DESIGN` | Phase 00/01 receipt와 Phase 02 type/test가 없고 live Phase 00 remediation은 다른 owner scope. Root/legacy green을 Phase 02 evidence로 사용하지 않음 |
| Write scope | `PASS` | Target guide와 correction report만 변경; stage/commit/push 없음 |

## 6. Residual risk와 다음 판정

1. Live Phase 00 worktree와 progress는 계속 변할 수 있다. 구현 시작 시 accepted receipt가
   가리키는 source snapshot으로 target §4.2/§5.1을 재검증해야 한다.
2. Phase 00/01 acceptance, Great Circle/source policy/fingerprint approval와 actual
   evidence plan이 없으므로 Phase 02 production implementation은 계속 blocked다.
3. Delivery-only exact internal representation과 Java visibility는 owner review 전
   public API로 고정할 수 없다.
4. Failsafe는 current POM에 구성되지 않았다. Required `*IT`를 file 이름만으로 추가하면
   실행 evidence가 아니다.
5. XML checker는 구현 시 actual committed PSV/checker runtime과 failure-sensitivity
   evidence를 봉인해야 하며 이 correction의 syntax check가 test execution을 대신하지 않는다.
6. 이 correction은 새 independent review와 scheduler acceptance를 대체하지 않는다.

CORRECTION_ROUND: 01
ADDRESSED_FINDINGS: HG-P02-R01,HG-P02-R02,HG-P02-R03,HG-P02-R04,HG-P02-R05
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 93c53ea2055bea89df4cb70dddc95b66b035f01bc392764d59d1ec86dea87f3a
TARGET_HASH_AFTER: 123405df8823ef901e6a4f6ef8a77e18208965f40c25e3385cbdccc2e9f57ace
