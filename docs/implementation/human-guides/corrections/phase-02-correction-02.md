# Phase 02 사람용 구현 가이드 correction 02

## 1. Metadata와 범위

| 항목 | 값 |
|---|---|
| Correction round | `02` |
| Correction 시각 | `2026-07-29T02:56:27+09:00` (`Asia/Seoul`) |
| 역할 | Correction 01 작성자·원 reviewer와 분리된 Phase 02 correction 02 담당 |
| Target | [phase-02-human-implementation-guide.md](../phases/phase-02-human-implementation-guide.md) |
| Original finding/recheck input | [phase-02-review.md](../reviews/phase-02-review.md), Correction 01 읽기 전용 재검증의 OPEN `HG-P02-R02`, `HG-P02-R03` |
| Prior correction | [phase-02-correction-01.md](phase-02-correction-01.md) |
| 허용 변경 | Target guide와 이 correction report만 |
| 금지 범위 준수 | Java/POM/test, canonical/review/correction01, README/progress, stage/commit/push/worktree를 수정하지 않음 |
| 구현/acceptance 주장 | 없음. 문서 계약과 future command/checker interface만 교정했으며 Phase 02는 `NOT_STARTED / NOT_ACCEPTED` |
| Finding 결과 | `HG-P02-R02`, `HG-P02-R03` addressed; deferred `NONE` |
| Target 상태 | `CORRECTED_ROUND_02 / AWAITING_INDEPENDENT_RECHECK` |

이 correction은 recheck가 명시한 두 남은 root cause와 required correction만 닫는다.
Correction 01에서 해결된 `HG-P02-R01`, `R04`, `R05`를 다시 열거나 target의 live
Phase 00 snapshot을 acceptance authority로 승격하지 않는다.

## 2. 고정 입력과 hash

SHA-256은 file bytes, Git blob은 `git hash-object` 결과다. Target before bytes는
recheck footer의 `TARGET_HASH_RECHECKED`와 직접 재계산 값이 일치했다.

| File | SHA-256 | Git blob/역할 |
|---|---|---|
| Target before | `123405df8823ef901e6a4f6ef8a77e18208965f40c25e3385cbdccc2e9f57ace` | `da1937c5bc600144789838e7636f7fda794e7878`; correction 01 rechecked bytes |
| Target after | `eabd5e029772bbaefa39073835dcb205f19fef09af9252642c442a00c172052e` | `eaa8a02026c17db085344bfb8e2a007624921d9b`; correction 02 bytes |
| Finding/recheck input | `722fa749ec10b66ac7850e414c2536339aa8aa709f1a980c9f9c6c059e5d8193` | `85b1e78e11ca5b8b8bbbfc9ebc989ff406d1cb64`; read-only |
| Correction 01 | `cb032c16bcc2bf67c58ff0d036caab0dc84d4900502a6335a12c5dc4cb87d269` | `1489e2016cf4edca43a835ae9580a9b59392b23a`; read-only |
| [Master](../../../master-design.md) | `e16d82789a77ceb2783ae027c3218c5da9b6c65413fc89cd5cab6771be8098bd` | §5.2~§5.3 service/zone invariant |
| [Current Architecture](../../../architecture-design.md) | `fe918a268d98aebcacde281bcb621a47b56356d6c59405e98966581f10f34201` | §19.1 reactor build order와 `-am` |
| [Canonical Phase 02](../../phases/phase-02-prepared-travel-immutable-problem.md) | `51d8491a701f88b218609ea32e9ec714710ff88291760c1dc63bfc27bba9f686` | WP-02-0~6와 §10 exact tests/oracle |
| [Master Realization Plan](../../master-realization-plan.md) | `940fe8c2156bf0472deafcd450e0ea49f0036ab6b304d6d051f0148a38cd0f5d` | §8.2 test category와 §9.1 pre-review manifest |
| [Canonical Phase 02 review](../../reviews/phase-02-review.md) | `7c25976b530bf42f470a01ccccd81e6732ffb577eebd9a0aef6cafeb19b343f5` | Phase blocker/evidence baseline |

Current Architecture §19.1의 topological order는 core가 먼저이고
test-fixtures/architecture-rules는 downstream이다. Master §5.3은 delivery-only의
depot logical pickup이 zone visit이 아니라고 고정한다. 이 두 source를 correction 01의
physical identity와 executable oracle 계약에 추가 적용했다.

## 3. HG-P02-R02 correction

### 3.1 Finding, root cause와 target anchors

- **Finding:** Correction 01은 logical pickup의 node/location/travel/stop/service 제외는
  닫았지만 zone visit과 zone-resource membership 제외를 전혀 쓰거나 판정하지 않았다.
- **남은 root cause:** Physical collection과 zone-resource collection을 별도 불변조건과
  oracle 축으로 보지 않아 Master §5.3의 zone 의미가 required method/evidence 밖에
  남았다.
- **Target anchors:** §3.2~§3.3, §6.3~§6.4, §8.6 freeze contract, WP-02.5,
  §10.2 exit-required table/PSV/checker, §11.2 evidence, §13.2 exit, §15
  traceability와 §16 self-check.

### 3.2 Applied correction

1. Logical pickup 자체는 zone fact를 읽거나 합성하지 않고 zone visit set 또는
   zone-resource membership에 원소를 추가하지 않는다고 §3 invariant에 고정했다.
2. Delivery node와 real physical pickup node의 정상 `zoneId`/zone-resource fact는
   버리거나 `ALL`로 덮어쓰지 않고 immutable problem에 보존한다고 반대 방향의 정상
   계약도 함께 고정했다.
3. §6에서 비물리·비-zone-visit 의미는 확정이고 prefix token 대 explicit initial-load
   representation과 Java visibility만 계속 `P-02 PROPOSED/OPEN`임을 분리했다.
4. §8.6과 WP-02.5 freeze 순서에 logical pickup physical/zone collection 제외와
   delivery/real-pickup zone fact 보존을 넣었다.
5. 기존 core 53/full 58 수를 유지하기 위해 required method를
   `keepsLogicalInitialLoadOutOfPhysicalAndZoneResourceSets()`로 명확히 확장했다.
   기존 physical collection oracle을 삭제하지 않고, delivery-only request 수만 바꾼
   두 fixture의 zone expected set을 production physical expected set과 독립 계산한다.
6. Logical pickup이 추가한 zone visit과 membership delta는 각각 exact `0`, delivery와
   real physical pickup zone fact set은 independent expected set과 exact equality라는
   판정을 PSV 설명, evidence, exit와 traceability에 연결했다.

### 3.3 Source, gate, verification과 residual

| 축 | 내용 |
|---|---|
| Source | [Master §5.2](../../../master-design.md#52-service-meaning), [Master §5.3](../../../master-design.md#53-vehicle-size와-capability), canonical Phase 02 WP-02.5/§10, correction 01의 physical-identity 계약 |
| Gate | 비물리·비-zone-visit 의미는 확정. Prefix token 대 explicit initial-load state, exact class 이름과 visibility는 Architecture/Phase 01~03 owner 승인 전 `PROPOSED/OPEN` |
| Verification | Target의 지정 anchor와 table/PSV에 exact method 이름이 동일하게 1회씩 있고 full PSV는 여전히 core 53 + architecture 5 = 58이다. Delivery-only count delta, logical zone delta `0/0`, physical zone fact equality가 method 설명/evidence/exit/trace에 모두 존재한다 |
| Residual | 실제 Java type/test는 아직 0개다. 문서 oracle은 implementation green이나 Phase acceptance가 아니며, approved internal representation이 생겨도 비-zone-visit 의미를 바꿀 수 없다 |

## 4. HG-P02-R03 correction

### 4.1 Finding, root cause와 target anchors

- **Finding:** Correction 01의 단일 full 58-row PSV/checker를 core-only clean run 직후
  실행하면 architecture report가 없어서 반드시 실패했다.
- **남은 root cause:** Run-local clean lifecycle과 final-exit manifest를 한 scope로
  합치고 checker에 run kind/module filter가 없었다.
- **Target anchors:** WP-02.6 command sequence, §10.2 full PSV와 exact checker,
  §10.4 false-green, §11.2 evidence, §13.3 exit와 §15 traceability.

### 4.2 Applied correction

1. Full PSV를 single source of truth로 유지하되 exact module counts를
   `rpdptw/core=53`, `build/architecture-rules=5`, total `58`로 고정했다.
2. Checker interface에 `--run-kind core|architecture|root`를 추가했다.
   `core`는 full PSV의 explicit `rpdptw/core` filter 53개만 판정하고,
   `architecture`/`root`는 full 58개만 판정한다.
3. WP-02.6 실행 순서를 다음처럼 분리했다.

   - Core `-pl rpdptw/core -am clean verify` → `run-kind=core`, required `53`.
   - Architecture `-pl build/architecture-rules -am clean verify` →
     `run-kind=architecture`, required `58`.
   - Full root `clean verify` → `run-kind=root`, required `58`.

4. Checker는 full manifest count/module/duplicate와
   `core53 ⊂ full58`, `full58 - core53 = architecture5`를 먼저 검사한다.
   Architecture/root가 full set보다 작으면 non-zero다.
5. 각 selected module/engine에 fresh `TEST-*.xml`이 있어야 하고 run 시작 timestamp보다
   오래된 XML, report/testcase 0, suite/testcase failure/error/skipped, required
   missing/duplicate를 모두 non-zero로 유지했다.
6. `run-id`, run kind, reviewed command ID, manifest/toolchain/source/report digest를
   새 evidence directory에 원자적으로 봉인하고 existing `run-id` overwrite를
   거부한다. Caller는 checker stdout/stderr/exit와 `run.json` digest를 pre-review
   manifest에 연결한다.
7. Final exit는 `runKind=root`, command ID `phase02-root-clean-verify-v1`,
   `requiredCount=58`을 요구한다. Core 53 filter를 final root evidence에 재사용할 수
   없다.

### 4.3 Source, gate, verification과 residual

| 축 | 내용 |
|---|---|
| Source | [Current Architecture §19.1](../../../architecture-design.md#191-reactor-build-order), [Plan §8.2](../../master-realization-plan.md#82-필수-test-종류), [Plan §9.1](../../master-realization-plan.md#91-pre-review-evidence-manifest), canonical Phase 02 WP-02-1~6/§10 |
| Gate | 모든 command/script/PSV는 Phase 00 accepted wrapper/DAG와 Phase 01 receipt 뒤의 future contract다. Current POM은 Failsafe execution이 없으므로 승인 없이 `*IT`를 evidence로 계산하지 않는다 |
| Verification | Embedded checker 192행 compile PASS. Synthetic fresh XML에서 core53, architecture58, root58 positive PASS. Zero/failure/skipped/missing/duplicate/stale/no-architecture/duplicate-manifest/subset-drift/run-id-reuse negative control은 모두 expected non-zero |
| Residual | 실제 Phase 02 source/test/checker file과 accepted evidence directory는 아직 없다. Syntax와 synthetic interface 검증은 Maven/Java test execution 또는 Phase 02 acceptance를 대신하지 않는다 |

## 5. Manifest subset/full 관계와 negative controls

### 5.1 Exact relationship

| Scope/run kind | Maven command 의미 | Selected manifest | Required count | Final-exit 자격 |
|---|---|---|---:|---|
| `core` | Core와 필요한 upstream만 clean verify | Full PSV의 exact `module == rpdptw/core` filter | `53` | 없음; local core run evidence |
| `architecture` | Architecture §19.1 downstream module을 `-am`으로 실행 | Full PSV 전체: core 53 + architecture 5 | `58` | Architecture evidence |
| `root` | Full root clean verify | Full PSV 전체: core 53 + architecture 5 | `58` | `runKind=root`/reviewed command ID/immutable run identity까지 맞을 때만 final exit |

집합 관계는 `core53 ⊂ full58`이고 차집합은 정확히
`architecture5 = full58 - core53`이다. PSV row는 total 58, unique 58,
core 53, architecture 5이며 다른 module/engine은 0이다. R02 zone correction은 기존
logical-pickup method 이름과 oracle을 확장했으므로 total 58을 늘리거나 기존 physical
coverage를 삭제하지 않았다.

### 5.2 Executed synthetic checker controls

| Control | 기대 | 실제 |
|---|---|---|
| Fresh core XML + `run-kind=core` | PASS 53 | `PASS required=53` |
| Fresh core+architecture XML + `run-kind=architecture` | PASS 58 | `PASS required=58` |
| Fresh core+architecture XML + `run-kind=root` | PASS 58 | `PASS required=58` |
| 같은 run ID 재사용 | non-zero | `immutable run identity already exists` |
| Core report만으로 full scope | non-zero | `no fresh XML reports: build/architecture-rules/...` |
| Zero testcase | non-zero | `zero testcases` |
| Required failure | non-zero | `non-zero suite summary`/`non-pass testcase` |
| Required skipped | non-zero | `non-zero suite summary`/`non-pass testcase` |
| Required missing | non-zero | `missing required testcase` |
| Required duplicate | non-zero | `duplicate required testcase (2)` |
| Stale XML | non-zero | `stale XML report` |
| Duplicate PSV row | non-zero | `duplicate manifest rows` |
| Core row 삭제로 52+5 | non-zero | `full manifest must be exact core53+architecture5=58` |

이 표의 XML/PSV는 embedded checker interface의 positive/negative behavior만 확인하기 위해
격리된 임시 directory에서 생성 후 제거했다. Repository Java/POM/test나 existing
`target/` report를 Phase 02 green으로 사용하지 않았다.

## 6. 정적·범위 검증

| 검사 | 결과 | Evidence/해석 |
|---|---|---|
| Finding coverage | `PASS` | R02/R03 각각 finding, root cause, anchors, source, gate, verification, residual을 기록 |
| Target hash | `PASS` | Before `123405...57ace`, after `eabd5e...052e`; recheck input과 direct calculation 일치 |
| Input immutability | `PASS` | Review `722fa7...8193`, correction01 `cb032c...d269`, Master/Architecture/canonical hashes가 correction 전후 동일 |
| Required table↔PSV | `PASS` | Exact method table과 PSV logical-pickup 이름 일치; PSV 58/58 unique, core 53, architecture 5 |
| Checker syntax/behavior | `PASS` | Python compile + positive 53/58/58 + 10개 failure category control |
| Relative local link/GFM fragment | `PASS` | Target `85/56`, report `13/5`, preserved review `18/0`, correction01 `28/15` local links/fragments; missing file/fragment 0 |
| GFM heading/table/fence | `PASS` | Target `67/22/64`, report `16/7/0`, review `29/9/0`, correction01 `14/4/0` heading/table/fence lines; level jump, duplicate base slug, table delimiter mismatch, open fence 모두 0 |
| Whitespace/tab/NUL/EOF | `PASS` | 네 문서 모두 trailing whitespace/tab/NUL 0, newline EOF. Owned target/report도 동일 |
| Scoped diff | `PASS` | Captured before→target와 `/dev/null`→report의 `git diff --no-index --check` whitespace diagnostic 0; exit `1`은 content/new-file diff라 expected |
| Untracked-aware scope | `PASS` | Owned target/report는 `??`; preserved review/correction01도 원래 untracked이며 hash 불변. Target before snapshot과 새 report를 사용해 untracked content diff를 별도 판정 |
| Maven/Java implementation test | `NOT_RUN_BY_DESIGN` | Entry gate 미충족이고 code/POM/test는 금지 범위. Embedded checker만 synthetic validation |
| Stage/commit/push/worktree | `PASS` | 수행하지 않음 |

## 7. Residual risk와 handoff

1. Delivery-only exact prefix/initial-load representation과 Java visibility는 계속
   `P-02 PROPOSED/OPEN`이다. Representation approval은 logical pickup을 zone visit으로
   바꾸는 권한이 아니다.
2. Phase 00/01 acceptance, Great Circle/source policy/fingerprint approval가 없으므로
   Phase 02 implementation은 계속 blocked다.
3. Accepted implementation은 문서의 PSV/checker source를 실제 reviewed file로 만들고,
   exact digest와 failure-sensitivity evidence를 독립 review에 제출해야 한다.
4. Final root run은 full 58개를 요구한다. Core 53개 성공이나 architecture `-am` 성공만
   가지고 scheduler `ACCEPTED`로 전이할 수 없다.
5. 이 correction은 새 independent recheck, implementation evidence, acceptance receipt
   또는 Phase 03 handoff를 대체하지 않는다.

CORRECTION_ROUND: 02
ADDRESSED_FINDINGS: HG-P02-R02, HG-P02-R03
DEFERRED_FINDINGS: NONE
TARGET_HASH_BEFORE: 123405df8823ef901e6a4f6ef8a77e18208965f40c25e3385cbdccc2e9f57ace
TARGET_HASH_AFTER: eabd5e029772bbaefa39073835dcb205f19fef09af9252642c442a00c172052e
