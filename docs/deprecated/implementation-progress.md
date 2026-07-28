---
document_role: RPDPTW 구현 계획 문서 작성 현황과 실제 구현 phase 상태를 분리해 관리하는 authoritative progress tracker
status: ACTIVE
last_updated: 2026-07-24
repository: /Users/brown/workspace/ro-next
phase_status_vocabulary:
  - NOT_STARTED
  - IN_PROGRESS
  - BLOCKED
  - DONE
source_documents:
  - path: docs/codex/implementation-plan.md
    sha256: d4450fd8d69e79cea36c75f41eac65c79f1eb4e339a327def0592b7f4966d14a
  - path: docs/codex/phases/phase-00-baseline-and-build-architecture.md
    sha256: 12722696b430f693f2c4db9df760ed6acb6a463eb49cfa398927936426d01e52
  - path: docs/codex/phases/phase-01-input-domain-and-travel.md
    sha256: a2f57d1bbfd519a97feec4f7811e1600e19efe201200cbfd14929c19a3c453b3
  - path: docs/codex/phases/phase-02-propagation-evaluation-and-profiles.md
    sha256: 8ec2cb5b8178d4afe4d82fc7a7a35e274729aeca1fd6687f5f974b942df26ae1
  - path: docs/codex/phases/phase-03-atomic-pair-and-initial-portfolio.md
    sha256: bb60081ebeaa858fcafb660238ac78abfaba4c311517394574b5ce95ca42aa15
  - path: docs/codex/phases/phase-04-cow-alns-and-reproducibility.md
    sha256: 1d85b79f8d8b8f30eb209fecee778f22bd0e3b70e67de2635c0892d7b4d8632b
  - path: docs/codex/phases/phase-05-verification-finalization-and-publication.md
    sha256: a60d695d89f98b9b7b11b93531e1a8abffcc1b4a3652e4da5c1a612f688d20d8
  - path: docs/codex/phases/phase-06-local-application-and-logical-ports.md
    sha256: 9d78da948c78e7744374b4e0cdc09a6f84f18ffdeb9ffd5235211db9004a7fce
  - path: docs/codex/phases/phase-07-logical-multi-round-coordinator.md
    sha256: a68a4d9131e37e8f84b2197214d29b7edea36244f1fce7fff3be05e5989bb12d
  - path: docs/codex/phases/phase-08-compatibility-migration-and-cutover.md
    sha256: 2a9f14143f91fcf34cd95cf3e3160ac6653c8f01b7b875a177650dc59febb222
  - path: docs/codex/phases/phase-09-official-win-poc-workflow.md
    sha256: 7bbe015a3efe2ed8bc9c392b3a2f5ae76e927e64f30de1178c673983a01645bd
  - path: docs/codex/phases/phase-10-cow-profiling-decision.md
    sha256: 565a2846450972855af7c29597b7a3396ff255a95a5d0f571f49825a99c6066e
  - path: docs/master-design.md
    sha256: 5e6a7901c2065fb58273853a233c556fa7d873732a4e3f104c6df15ad6d45f9c
  - path: docs/architecture-design.md
    sha256: 161b08e8875834698d3bd73b4bd11fcb3077bc4786afd47ac0be36358958b212
  - path: docs/domain-design.md
    sha256: 3a98d34b4967900faa5c4f1ac93f0b9c2168bfa8d018efd362114e9e557f98e2
  - path: docs/master-design-open-questions.md
    sha256: 3d6bc496b8df98a10534338828dd7e845b50ea967afa884642403405e613c088
---

# RPDPTW 구현 진행 현황

## 1. 문서 역할과 판정 원칙

이 문서는 [전체 구현 계획](implementation-plan.md), 11개 phase 실행 명세, [Master Design](../master-design.md), [Architecture Design](../architecture-design.md), [Domain Design](../domain-design.md), [질문 등록부](../master-design-open-questions.md)와 현재 repository를 대조해 **문서 작성 현황**과 **실제 코드 구현 현황**을 분리 관리하는 단일 진행 현황판이다. 설계 의미, public API, wire schema, provider, 수치 또는 변환 정책을 새로 승인하는 문서가 아니다.

상단 `status: ACTIVE`는 이 tracker의 관리 상태다. 구현 phase 상태에는 [전체 구현 계획 §11.2](implementation-plan.md)의 다음 네 값만 사용한다.

| 구현 phase 상태 | 의미 |
|---|---|
| `NOT_STARTED` | Entry gate가 아직 평가되지 않았거나 production/test 작업을 시작하지 않음 |
| `IN_PROGRESS` | Entry gate를 통과하고 범위 내 test, implementation, evidence를 실제 작성 중 |
| `BLOCKED` | 명시된 외부 결정·승인·fixture 또는 선행 DONE evidence가 없어 안전하게 진행할 수 없음 |
| `DONE` | 아래 DONE AND gate를 모두 충족 |

`DONE`은 다음 아홉 조건의 AND다.

1. Entry gate와 모든 선행 phase가 충족됐다.
2. Phase 문서의 구현 범위와 deliverable이 실제 repository에 존재한다.
3. 각 요구에 red → green evidence가 있다.
4. Targeted test, module `verify`, 필요한 reactor와 architecture 회귀가 통과했다.
5. Exit evidence와 evidence bundle digest가 있다.
6. 불변조건, 금지 dependency/value와 scope exclusion 위반이 0이다.
7. Blocker가 남지 않았고 중단 조건이 발생하지 않았다.
8. Rollback과 다음 phase handoff가 재현 가능하다.
9. 단순 source/test/mock/demo 존재가 아니라 downstream consumer가 artifact를 실제 검증해 소비한다.

따라서 계획 또는 phase 문서 파일의 존재, 문서 작성 완료, 중앙 검증 통과, skeleton·test·mock·demo의 존재는 구현 `DONE`이 아니다. 문서 작성률과 구현 완료율을 섞은 퍼센트도 만들지 않는다.

## 2. 문서 작성 현황

기존 12개 계획 문서는 작성 완료 및 중앙 검증 통과 상태다. 다만 이는 문서 품질 판정이며, 상위 설계와 계획상 제안 API의 `REVIEW` 지위를 `APPROVED`로 바꾸지 않는다. Phase 문서의 원문 `document_status` 라벨이 `COMPLETE`, `REVIEW_READY` 등이어도 구현 상태 또는 외부 계약 승인으로 해석하지 않는다.

| 문서 | 작성 상태 | 검증 상태 | 원문 작성 라벨 | 권위·승인 상태 |
|---|---|---|---|---|
| [전체 구현 계획](implementation-plan.md) | 작성 완료 | 중앙 검증 통과 | `status: REVIEW` | `REVIEW`; 구현 완료 보고가 아님 |
| [AR-0 phase](phases/phase-00-baseline-and-build-architecture.md) | 작성 완료 | 중앙 검증 통과 | `document_status: REVIEW` | `REVIEW` |
| [AR-1 phase](phases/phase-01-input-domain-and-travel.md) | 작성 완료 | 중앙 검증 통과 | `document_status: REVIEW` | `REVIEW` |
| [AR-2 phase](phases/phase-02-propagation-evaluation-and-profiles.md) | 작성 완료 | 중앙 검증 통과 | 원문 field 없음 | `REVIEW` |
| [AR-3 phase](phases/phase-03-atomic-pair-and-initial-portfolio.md) | 작성 완료 | 중앙 검증 통과 | `document_status: REVIEW` | `REVIEW` |
| [AR-4 phase](phases/phase-04-cow-alns-and-reproducibility.md) | 작성 완료 | 중앙 검증 통과 | `document_status: REVIEW_DRAFT` | `REVIEW` |
| [AR-5 phase](phases/phase-05-verification-finalization-and-publication.md) | 작성 완료 | 중앙 검증 통과 | `document_status: REVIEW_DRAFT` | `REVIEW` |
| [AR-6 phase](phases/phase-06-local-application-and-logical-ports.md) | 작성 완료 | 중앙 검증 통과 | `document_status: COMPLETE` | `REVIEW`; `COMPLETE`는 작성 라벨 |
| [AR-7 phase](phases/phase-07-logical-multi-round-coordinator.md) | 작성 완료 | 중앙 검증 통과 | `document_status: REVIEW_READY` | `REVIEW` |
| [AR-8 phase](phases/phase-08-compatibility-migration-and-cutover.md) | 작성 완료 | 중앙 검증 통과 | `document_status: READY_FOR_REVIEW` | `REVIEW` |
| [AR-9 phase](phases/phase-09-official-win-poc-workflow.md) | 작성 완료 | 중앙 검증 통과 | 원문 field 없음 | `REVIEW` |
| [AR-10 phase](phases/phase-10-cow-profiling-decision.md) | 작성 완료 | 중앙 검증 통과 | `document_status: REVIEW` | `REVIEW` |
| 이 progress 문서 | 생성 완료 | 본 세션 정적 검증 통과 | `status: ACTIVE` | 진행 현황에만 authoritative |

## 3. 현재 repository baseline

기준 시점은 2026-07-24다.

| 항목 | 현재 관찰 |
|---|---|
| Git | Branch `codex/domain-design`, HEAD `523c23e2e13410885b16e974efe40ffe598106ee`, dirty working tree. 기존 변경과 기존 문서는 사용자·다른 세션 소유다. |
| Build | Root `pom.xml` 하나인 단일 `jar`/shaded application. Java release 25와 Maven `[3.9.14,)` enforcer는 있으나 목표 parent/aggregator reactor는 없다. Root POM SHA-256은 `f61cab65190c44c5aba08b8c413397d5fe8ba8835f57de1d79deb6b705454cd6`다. |
| Source/test | `com.ronext.optimizer` 아래 production Java 6개와 `AlnsBatchEngineTest` 1개가 있다. `rpdptw`, `adapters`, `apps`, `build`, `legacy/current-app` 목표 module/source는 아직 없다. |
| Legacy integration | Google Cloud SDK가 root classpath에 직접 있고 `gcp/` build/workflow 자료가 있다. 이는 characterization 대상이지 목표 provider authority가 아니다. |
| Win fixture | `data/win_poc_case.json` SHA-256은 `ea003bac326ebdbbb5f49595388767ed223c03539fd6579b96f3acbedce6b7d7`다. 현재 decimal `D/U`는 official integer meter/second 계약에 비준수다. 변환본을 추측해 만들지 않는다. |
| 격리 baseline | Phase-00 관리 관찰: Amazon Corretto Java `25.0.3`, Maven `3.9.14`, 기존 `mvn test` PASS, 기존 `mvn verify` PASS. 이는 legacy baseline이지 AR-0 구현 evidence가 아니다. |
| 공유 target 주의 | 여러 세션의 병렬 Maven 실행으로 생긴 shade JAR replace 등 공유 `target/` 충돌은 격리 재현 전 baseline 결함에서 제외한다. 이 tracker 작성 세션은 Maven을 재실행하지 않았다. |
| 구현 evidence | `target/codex-evidence/<phase-id>/<evidence-id>/` bundle, 목표 구현 코드·테스트, phase DONE digest가 현재 없다. |

현재 금지되는 완료 주장은 다음과 같다.

- 문서가 존재하거나 중앙 검증을 통과했다는 이유로 구현을 시작·완료했다고 표시하는 주장
- 기존 legacy `test`/`verify` PASS를 AR-0 또는 후속 phase DONE evidence로 재사용하는 주장
- Skeleton, compile 성공, 단일 targeted test, mock/demo 또는 test-only 수치를 downstream 소비 evidence로 대체하는 주장
- 공유 `target/` 충돌을 repository 결함 또는 phase blocker로 확정하는 주장
- 승인되지 않은 Earth radius/library default, 공식 worker/step/round/watchdog 수치, provider 또는 external wire 변환을 채운 뒤 완료하는 주장
- Candidate/result verifier 중 하나만 통과하거나 일부 worker만 성공한 결과를 publishable/official로 부르는 주장
- 현재 decimal Win fixture를 반올림·절삭·문자열 변환해 compliant official fixture라고 부르는 주장

## 4. 구현 phase 현황

Phase 문서 top metadata와 대조한 canonical 현재 판정은 아래와 같다. 구현 코드·테스트·evidence bundle이 없으므로 `DONE` 또는 `IN_PROGRESS`를 만들지 않는다.

| Phase / RM | 실행 문서 | 현재 구현 상태 | Entry gate | 현재 blocker | 최근 evidence | 다음 안전 작업 |
|---|---|---|---|---|---|---|
| `AR-0 / RM-0` | [Baseline/build architecture](phases/phase-00-baseline-and-build-architecture.md) | `NOT_STARTED` — metadata 일치 | 선행 phase 없음. 사용자 변경 보존, Git status/hash와 toolchain baseline을 먼저 캡처 | 없음; 현재 첫 안전 착수점 | Phase 명세 중앙 검증과 격리 legacy test/verify PASS만 있음. AR-0 bundle은 없음 | 별도 구현 세션에서 baseline을 다시 고정하고 legacy characterization test와 reactor architecture test를 production/POM보다 먼저 작성 |
| `AR-1 / RM-1` | [Input/domain/travel](phases/phase-01-input-domain-and-travel.md) | `BLOCKED` — metadata 일치 | `AR-0 DONE` | `DEP-00`, `AUTH-GC-01`; Great Circle production exit authority 없음 | Phase 명세 중앙 검증만 있음. 구현 및 bundle 없음 | AR-0 DONE 뒤 test-first로 versioned policy hook·missing-policy rejection을 시작하되, phase 완료 전 exact Great Circle authority 확보 |
| `AR-2 / RM-2` | [Propagation/evaluation/profiles](phases/phase-02-propagation-evaluation-and-profiles.md) | `BLOCKED` — metadata 일치 | `AR-1 DONE`과 problem/travel fingerprint handoff | `DEP-01` | Phase 명세 중앙 검증만 있음. 구현 및 bundle 없음 | AR-1 DONE bundle을 검증한 뒤 hand-calculated propagation/profile/comparator test부터 작성 |
| `AR-3 / RM-3` | [Atomic pair/initial portfolio](phases/phase-03-atomic-pair-and-initial-portfolio.md) | `BLOCKED` — metadata 일치 | `AR-2 DONE` | `DEP-02` | Phase 명세 중앙 검증과 `CommittedCandidate` owner 정합화만 있음. 구현 및 bundle 없음 | AR-2 DONE 뒤 side-effect-free pair evaluator와 rollback/isolation test부터 작성 |
| `AR-4 / RM-4` | [COW ALNS/reproducibility](phases/phase-04-cow-alns-and-reproducibility.md) | `BLOCKED` — metadata 일치 | `AR-0`~`AR-3 DONE` | `DEP-03` 및 transitive predecessor evidence 부재 | Phase 명세 중앙 검증, `solver.state.CommittedCandidate`와 AR-2 `evaluation.api.StageGuard` 직접 소비 정합화만 있음 | AR-3 DONE 뒤 COW reject/fault/completed-step test를 먼저 작성하고 AR-2 StageGuard를 중복 없이 직접 소비 |
| `AR-5 / RM-5` | [Verification/finalization/publication](phases/phase-05-verification-finalization-and-publication.md) | `BLOCKED` — metadata 일치 | `AR-0`~`AR-4 DONE`, AR-1 problem/travel과 AR-2 evaluation authority | `DEP-04`; 실제 candidate와 중립 verifier input 부재 | Phase 명세 중앙 검증만 있음. 두 verifier, both-pass와 bundle 없음 | AR-4 DONE 뒤 corruption test와 publication block test를 먼저 작성하고 actual candidate의 solver-free projection을 검증 |
| `AR-6 / RM-8-local` | [Local application/logical ports](phases/phase-06-local-application-and-logical-ports.md) | `BLOCKED` — metadata 일치 | Skeleton 착수는 `AR-0 DONE`; phase exit는 `AR-5 DONE` | `DEP-00`, `DEP-05` | Phase 명세 중앙 검증만 있음. Port/local code와 both-pass E2E 없음 | AR-0 DONE 뒤 port/fake/local artifact-state contract test scaffold 가능. DONE은 AR-5 both-pass 실제 경로까지 보류 |
| `AR-7 / RM-6-logical` | [Logical multi-round coordinator](phases/phase-07-logical-multi-round-coordinator.md) | `BLOCKED` — metadata 일치 | Scaffold는 `AR-0 DONE`; exit는 `AR-5`, `AR-6 DONE` | `DEP-00`, `DEP-05`, `DEP-06` | Phase 명세 중앙 검증만 있음. Coordinator/fake dispatcher와 completeness evidence 없음 | AR-0 DONE 뒤 identity/state-machine/fake coordinator test scaffold 가능. DONE은 AR-5/6 actual verified publication 뒤 보류 |
| `AR-8 / RM-8-cutover` | [Compatibility migration/cutover](phases/phase-08-compatibility-migration-and-cutover.md) | `BLOCKED` — metadata 일치 | `AR-5`, `AR-6`, `AR-7 DONE` | `DEP-07`, `AUTH-WIRE-01`; 승인되지 않은 external break/cutover 금지 | Phase 명세 중앙 검증만 있음. Compatibility/shadow/rollback evidence 없음 | 선행 DONE 뒤 legacy characterization 및 versioned adapter test부터 시작하고 unresolved wire 차이는 승인 전 cutover하지 않음 |
| `AR-9 / RM-6-official` | [Official Win PoC](phases/phase-09-official-win-poc-workflow.md) | `BLOCKED` — metadata 일치 | `AR-8 DONE`, `Q-BENCH-02` 공식 수치 승인, compliant integer fixture/digest | `DEP-08`, `AUTH-BENCH-01`, `AUTH-FIXTURE-01` | Phase 명세 중앙 검증만 있음. Official manifest/baseline/challenger evidence 없음 | 현재는 blocker/approval/fixture만 검토. 수치나 fixture를 추측한 test-local official 구현 금지 |
| `AR-10 / RM-7` | [COW profiling decision](phases/phase-10-cow-profiling-decision.md) | `BLOCKED` — metadata 일치 | `AR-4`, `AR-5 DONE`과 representative both-pass normal case | `DEP-04`, `DEP-05`; representative verified case 부재 | Phase 명세 중앙 검증만 있음. Profiling/raw report/equivalence bundle 없음 | AR-4/5 DONE 뒤 원본 변환 없는 representative case를 선택하고 profiling OFF/ON semantic-equivalence test부터 작성 |

### 4.1 현재 상태 집계

| 상태 | 수량 |
|---|---:|
| `DONE` | 0 |
| `IN_PROGRESS` | 0 |
| `BLOCKED` | 10 |
| `NOT_STARTED` | 1 |
| 합계 | 11 |

이 집계는 구현 phase만 센다. 계획 문서 12개와 이 tracker의 작성 완료를 구현 수량 또는 구현 퍼센트에 더하지 않는다.

## 5. Dependency와 critical path

```text
AR-0
├── AR-1 → AR-2 → AR-3 → AR-4 → AR-5 → AR-6 → AR-7 → AR-8 → AR-9
│                                  └──────────────→ AR-10
├── AR-6 port/local skeleton after AR-0
└── AR-7 coordinator scaffold after AR-0
```

Publishable local solver의 core critical path는 `AR-0 → AR-1 → AR-2 → AR-3 → AR-4 → AR-5 → AR-6`이다. Compatibility cutover는 `AR-7 → AR-8`, official Win PoC는 외부 gate가 닫힌 뒤 `AR-9`까지 이어진다. `AR-10`은 `AR-4`와 `AR-5` 뒤의 profiling decision branch이며 `AR-9` DONE은 필수가 아니다.

`AR-0`이 현재 첫 안전 착수점이다. `AR-0 DONE` 뒤 `AR-6` port/local skeleton과 `AR-7` coordinator scaffold는 core critical path와 병행할 수 있다. 그러나 어느 작업도 `AR-5` candidate verifier와 result-integrity verifier의 both-pass publication 경로를 우회해 phase `DONE`이 될 수 없다.

## 6. Blocker 등록부

Tracker ID는 상태 추적용이며 새 설계 결정 ID나 승인 record가 아니다. Provider, 수치, 변환, 외부 계약을 추측해 blocker를 닫지 않는다.

### 6.1 Dependency gate

| ID | Owner / authority | 영향 phase | 마지막 안전 지점 | 재개 조건 |
|---|---|---|---|---|
| `DEP-00` | AR-0 구현 세션과 AR-0 DONE gate reviewer | AR-1, AR-6/7 scaffold 및 transitive 후속 | 현재 legacy tree, AR-0 명세, 격리 baseline | AR-0 red→green, reactor/architecture regression, evidence bundle digest와 handoff가 모두 DONE |
| `DEP-01` | AR-1 구현 세션과 AR-1 DONE gate reviewer | AR-2, AR-3~5 transitive | AR-1 계획상 API와 test builder 명세 | Approved Great Circle policy를 포함한 immutable problem/complete travel DONE evidence와 fingerprints |
| `DEP-02` | AR-2 구현 세션과 AR-2 DONE gate reviewer | AR-3 및 transitive 후속 | AR-2 전파/평가/profile 명세 | Propagation, `evaluation.api.StageGuard`, exact comparator, immutable BoundProfile DONE evidence |
| `DEP-03` | AR-3 구현 세션과 AR-3 DONE gate reviewer | AR-4 및 transitive 후속 | `solver.state.CommittedCandidate` 정합화된 문서 계약 | Atomic pair, portfolio, candidate isolation과 downstream-consumable DONE evidence |
| `DEP-04` | AR-4 구현 세션과 AR-4 DONE gate reviewer | AR-5, AR-10 및 transitive 후속 | COW/reproducibility 명세 | Actual committed candidate, normal termination, cache-free equality, reproducibility DONE evidence |
| `DEP-05` | AR-5 구현 세션과 AR-5 DONE gate reviewer | AR-6 exit, AR-7~8, AR-10 | 두 독립 verifier와 publication gate 명세 | Candidate/result verifier 독립 PASS, PublishableResult, corruption suite와 both-pass DONE evidence |
| `DEP-06` | AR-6 구현 세션과 AR-6 DONE gate reviewer | AR-7, AR-8 | Provider-neutral port/fake 명세 | Local artifact/state/dispatch/CAS와 both-pass E2E DONE evidence |
| `DEP-07` | AR-7 구현 세션과 AR-7 DONE gate reviewer | AR-8 | Logical identity/state-machine/fake 명세 | Declared completeness, stable fan-in, retry/incomplete rejection과 verified publication DONE evidence |
| `DEP-08` | AR-8 구현 세션과 AR-8 DONE gate reviewer | AR-9 | Legacy characterization과 logical cutover 명세 | Approved compatibility matrix, shadow/rollback 및 logical cutover DONE evidence |

### 6.2 External / authority blocker

| ID | Owner / authority | 영향 phase | 마지막 안전 지점 | 재개 조건 |
|---|---|---|---|---|
| `AUTH-GC-01` | Input/matrix contract authority 또는 승인 ADR owner | AR-1 exit와 transitive 후속 | Versioned distance-policy interface, explicit test double, missing-policy typed rejection | Exact Great Circle 식, 상수/Earth radius, rounding input과 implementation version을 명시한 채택 외부 계약 또는 승인 ADR |
| `AUTH-BENCH-01` | Benchmark·Quality authority (`Q-BENCH-02`) | AR-9 official manifest/baseline | Explicit `TEST_ONLY` logical fan-out/fan-in; official default 없음 | Calibration corpus/report와 `screenMaxSteps`, phase-2 worker 수·`phase2MaxSteps`·`maxRounds`, watchdog의 명시적 승인 및 digest |
| `AUTH-FIXTURE-01` | Input/matrix 및 benchmark fixture authority | AR-9 official run | 현재 fixture는 read-only negative rejection fixture | Integer meter/second `D/U` compliant fixture와 승인 digest 또는 명시적 계약 변경; 임의 변환 금지 |
| `AUTH-WIRE-01` | External API/product authority와 Architecture review | AR-8 intentional break 및 logical cutover | Legacy characterization, versioned adapter, shadow difference를 unresolved로 분류 | External wire break별 승인 record, versioned compatibility matrix, rollback/cutover authority |
| `AUTH-INFRA-01` | Application·Platform·Product authority (`Q-INFRA-01`) | Deferred `AR-11/RM-9`; AR-0~10은 logical boundary만 유지 | Provider-neutral ports와 local/fake runtime | Workload, security/access/retention/audit, retry/recovery, performance/cost evidence와 별도 scope 승인 |
| `AUTH-VAR-01` | Product·Domain·Algorithm authority (`Q-VAR-01`) | Deferred `AR-11/RM-9`; active core에 선반영 금지 | 현재 atomic pair, fixed terminal, bank/matrix 계약 | 대상 variant·시점·fixture, hand result와 core-impact feasibility에 대한 별도 승인 |

`Q-INFRA-01`과 `Q-VAR-01`은 현재 active phase의 실패가 아니라 deferred scope gate다. Logical boundary를 지키는 한 AR-0~AR-10 진행을 막지 않으며, 반대로 active 구현이 이를 묵시적으로 해결하거나 활성화할 수 없다.

## 7. Evidence bundle과 상태 변경 절차

각 구현 phase는 `target/codex-evidence/<phase-id>/<evidence-id>/`를 생성하고 CI artifact로 보존한다. `target/`을 source control에 넣을 필요는 없지만 이 tracker와 해당 phase handoff는 evidence ID, content digest, commit/tree와 artifact digest를 참조해야 한다.

최소 bundle은 `evidence.json`, `commands.log`, `red/`, `green/`, `regression/`, `fingerprints/`, `faults/`, `reproducibility/`, `diff/`, `handoff.md`를 가진다. `benchmark/`는 AR-9/AR-10에서만 사용한다. Bundle 경로 문자열이나 비어 있는 디렉터리만으로 evidence가 되지 않는다.

Phase 상태는 다음 절차로만 바꾼다.

1. Source design/plan/phase hash, Git status/tree, 사용자 변경과 선행 evidence ID/digest를 캡처한다.
2. Entry gate를 평가한다. Gate가 열리지 않았으면 blocker ID, owner/authority, 마지막 안전 지점과 재개 조건을 기록하고 `BLOCKED`를 유지한다.
3. Gate를 통과한 뒤 production보다 먼저 failing test를 실제 작성·실행하고 의도한 red report를 보존할 때만 `IN_PROGRESS`로 바꾼다.
4. Targeted green 뒤 module/reactor/architecture/fault/reproducibility 등 해당 phase의 전체 exit 검증을 실행한다.
5. DONE AND gate 아홉 항목, blocker 0건, evidence digest와 downstream 소비를 확인한 뒤에만 `DONE`으로 바꾼다.
6. 해당 phase top metadata, 이 표, 집계, blocker 등록부와 append-only 작업 기록을 같은 reviewed 변경 단위에서 갱신한다. Source가 바뀌면 SHA-256도 다시 계산한다.
7. 기존 DONE evidence의 무효화가 발견되면 과거 기록을 고치지 말고 correction log를 추가한 뒤 원인에 따라 `BLOCKED` 또는 `IN_PROGRESS`로 명시적으로 내린다.

## 8. Update checklist

진행 현황을 갱신하는 세션은 다음을 모두 확인한다.

- [ ] 현재 working tree의 사용자·다른 세션 변경을 보존하고 시작/종료 `git status --short --branch`를 남겼다.
- [ ] 16개 source document SHA-256을 실제 파일에서 다시 계산하고 metadata drift를 판정했다.
- [ ] 각 phase top metadata의 `phase`, `rm_mapping`, 구현 `status`를 tracker와 대조했다.
- [ ] 구현 status는 `NOT_STARTED`, `IN_PROGRESS`, `BLOCKED`, `DONE` 중 하나만 썼다.
- [ ] `IN_PROGRESS`에는 열린 entry gate와 실제 red evidence가, `DONE`에는 아홉 조건의 AND evidence가 있다.
- [ ] Evidence ref가 `target/codex-evidence/<phase-id>/<evidence-id>/`와 digest를 함께 가리킨다.
- [ ] Blocker마다 ID, owner/authority, 영향 phase, 마지막 안전 지점과 재개 조건이 있다.
- [ ] `DONE`/`IN_PROGRESS`/`BLOCKED`/`NOT_STARTED` 집계 합이 11인지 확인했다.
- [ ] 문서 작성 현황을 구현 상태 또는 혼합 퍼센트로 계산하지 않았다.
- [ ] Relative link, H1/heading, code-fence 짝, trailing whitespace와 source hash 검사를 통과했다.
- [ ] `git diff --check`와 신규 파일 whitespace 검사를 통과했다.
- [ ] 변경 범위가 승인된 파일에만 있고 broad restore/reset/clean을 실행하지 않았다.
- [ ] 아래 작업 기록에 새 행만 append했다.

## 9. 작업 기록

이 표는 append-only다. 기존 행의 설명·날짜·판정을 덮어쓰지 않는다. 정정이 필요하면 새 sequence로 원행을 참조하는 correction을 추가한다.

| Seq | 기준일 | 작업 / 판정 | Evidence와 비고 |
|---:|---|---|---|
| 1 | 2026-07-24 | 상위 [전체 구현 계획](implementation-plan.md) 생성 | AR-0~AR-10, RM mapping, DAG, TDD/evidence와 DONE gate 정의 |
| 2 | 2026-07-24 | 11개 phase 문서를 각 별도 세션에서 생성 | 문서 작성 완료와 실제 구현 상태를 분리 |
| 3 | 2026-07-24 | Phase-00 격리 baseline 관찰 | Amazon Corretto Java `25.0.3`, Maven `3.9.14`, 기존 `test` PASS, 기존 `verify` PASS |
| 4 | 2026-07-24 | 병렬 Maven의 공유 `target/` 충돌을 baseline 결함에서 제외 | 동시 shade JAR replace 등은 같은 tree의 격리 재현 전 결함/blocker evidence가 아님 |
| 5 | 2026-07-24 | 중앙 교차 검토에서 `CommittedCandidate` canonical owner 통일 | `com.ronext.rpdptw.solver.state`를 AR-3~AR-6 공통 owner로 사용; portfolio alias/duplicate 금지 |
| 6 | 2026-07-24 | AR-4 StageGuard 중복 제거 | AR-4가 AR-2 `com.ronext.rpdptw.evaluation.api.StageGuard`를 직접 소비; solver-local wrapper/alias 금지 |
| 7 | 2026-07-24 | Phase 구현 상태 정규화 | AR-0 `NOT_STARTED`, AR-1~AR-10 `BLOCKED`; 문서 작성 label과 구현 상태 분리 |
| 8 | 2026-07-24 | 기존 12개 계획 문서 중앙 정적 검증 통과 | 상대 링크, code fence, source hash, `git diff --check` 통과; 설계/phase 권위는 계속 `REVIEW` |
| 9 | 2026-07-24 | 이 authoritative progress tracker 생성 | 상태 집계 `DONE 0`, `IN_PROGRESS 0`, `BLOCKED 10`, `NOT_STARTED 1`; 구현·승인·POM 변경 없음 |

## 10. 다음 작업

다음 작업은 이 tracker 세션이 아니라 **별도 AR-0 구현 세션**에서 [AR-0 실행 명세](phases/phase-00-baseline-and-build-architecture.md)에 따라 수행한다.

1. 구현 전에 현재 사용자 변경을 그대로 보존하고 `git status --short --branch`, HEAD/tree, root POM, source/test, fixture와 source design hash를 캡처한다. 기존 dirty 변경을 reset/restore/clean하거나 덮어쓰지 않는다.
2. Phase-00 격리 baseline의 Java/Maven 및 legacy test/verify 결과를 출발점으로 기록하되 AR-0 DONE evidence로 재사용하지 않는다.
3. Production source나 POM을 바꾸기 전에 `LegacyAlnsBatchEngineCharacterizationTest`와 reactor/dependency/package architecture test를 먼저 작성하고 의도한 red를 보존한다.
4. 최소 reactor/legacy migration 구현 뒤 targeted green, module/reactor `verify`, architecture와 rollback evidence를 격리 상태에서 생성한다.
5. `target/codex-evidence/AR-0/<evidence-id>/` bundle과 digest, 변경 전후 artifact/main-class, 다음 phase handoff가 완결된 뒤에만 AR-0을 `DONE`으로 바꾼다.
6. AR-1은 AR-0 DONE 뒤 시작할 수 있지만, 누락 `D` production path의 Great Circle exact 함수·상수·rounding input·implementation version authority는 AR-1 완료 전에 반드시 닫아야 한다. 승인되지 않은 Earth radius나 library default를 선택하지 않는다.
