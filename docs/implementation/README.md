# RPDPTW 구현 문서 지도

```yaml
document_set_status: DOCUMENTATION_COMPLETE_WITH_RESIDUAL_BLOCKERS
baseline_date: 2026-07-28
source_authority: USER_LOCKED_FOR_THIS_DOCUMENT_SET
phase_count: 15
phase_documents: 15
phase_reviews: 15
implementation_accepted_phases: 0
execution_success_fixture: data/win_poc_case_floor.json
execution_success_status: NOT_RUN
```

## 0. 이 구현 작업의 최종 성공 기준

이 문서 세트에 따라 수행하는 현재 구현 작업의 성공은
[win_poc_case_floor.json](../../data/win_poc_case_floor.json)을 실제 solver로 실행하고,
candidate verifier와 result-integrity verifier가 모두 `PASS`한 결과를 생성하여
사용자에게 보여주는 것이다.

세부 AND gate, 결과 필드와 재현 조건은
[Master Realization Plan §11.3](master-realization-plan.md#113-사용자-고정-실행-성공-dod)을
따른다. 현재 상태는 fixture migration만 완료된 `NOT_RUN`이며 solver 구현 성공을
주장하지 않는다.

## 1. 읽기 순서

1. [Canonical Master](../master-design.md)에서 시스템 의미, 확정 결정, 완료 정의와 gate를 읽는다.
2. [Final Domain Design](../2026-07-26-domain-design.md)에서 normalization, travel, pair, propagation, evaluation과 result 계약을 읽는다.
3. [Final Architecture Design](../2026-07-26-architecture-design.md)에서 Maven/module/package, port, verifier와 backend 경계를 읽는다.
4. [Implementation integrated design](../architecture-domain-implementation-design.md)에서 15 Phase, capability/profile, no-DB object storage, AWS/reference와 provider substitution을 읽는다.
5. [Master Design open questions](../master-design-open-questions.md)에서 `Q-*` exact 상태와 approval/restart 조건을 확인한다.
6. [Master Realization Plan](master-realization-plan.md)에서 current inventory, DAG, Phase별 실행·검증과 DoD를 확인한다.
7. [Execution Progress and Results](execution-progress-and-results.md)에서 총괄 스케줄러가 관리하는 task/status/evidence/result를 확인한다.
8. 실제 작업 시 해당 Phase 상세 문서와 review 문서를 함께 읽는다. Phase 00~14 상세 문서와 독립 review는 모두 actual이다.

[2026-07-26 Master Design — SUPERSEDED](../2026-07-26-master-design.md)는 역사 cross-check에만 사용한다. `docs/codex/*`는 2026-07-24 역사/참고 자료이며 이 문서 세트에서 복사·수정·삭제하거나 현재 authority로 사용하지 않는다.

## 2. 문서 지도

| 문서 | 소유하는 내용 | 소유하지 않는 내용 |
|---|---|---|
| [master-realization-plan.md](master-realization-plan.md) | Current inventory, target structure, Phase 00~14 DAG/critical path, phase contract, test/evidence/DoD, gate와 traceability | 구현 완료 상태의 일상 갱신 |
| [execution-progress-and-results.md](execution-progress-and-results.md) | 문서 workflow, scheduler task registry, phase status, result, review와 remaining issue | Source 설계 의미 변경 |
| [README.md](README.md) | 읽기 순서, source authority, canonical phase/review link index와 naming rule | Phase 상세 구현 계약 |
| `phases/phase-00-*.md`~`phase-14-*.md` | 해당 Phase의 구체 실행 unit, command, change scope, test와 handoff | 다른 Phase의 status 또는 전체 source authority 변경 |
| `reviews/phase-00-review.md`~`phase-14-review.md` | 해당 Phase entry/exit/evidence의 독립 review verdict | 구현자 self-claim만으로 status 승격 |

## 3. Source authority

이 구현 문서 세트의 입력 권위는 사용자 선언으로 고정되었다. Source metadata의 `REVIEW`는 provenance로 보존하지만 문서 작성을 중단하지 않는다. 구현 완료·cutover에는 여전히 실제 phase review와 evidence가 필요하다.

충돌 순서는 다음과 같다.

```text
사용자 선언
→ canonical docs/master-design.md
→ 질문 등록부의 exact Q-* 상태
→ final domain meaning
→ final architecture placement
→ integrated design의 15 Phase/no-DB/AWS 구조
→ superseded master의 historical cross-check
```

중요한 최신 해소:

- `Q-INFRA-01`은 `RESOLVED`이며 target/reference는 AWS S3 + Step Functions + Lambda다.
- 질문 상태는 `RESOLVED 26`, `OPEN — EXPERIMENT_REQUIRED 1`, `DEFERRED 1`이다.
- AWS 선택은 실제 구현·배포·production cutover 승인이 아니다.
- `win_poc_case_floor.json`은 사용자 승인 `D/U FLOOR` migration을 거친 이 구현 작업의 최종 실행 fixture다.
- `Q-BENCH-02` 공식 수치와 production authority는 별도 Phase 14 production gate로 남는다.
- Route pool/MIP는 `C-17 GATED TARGET`이며 Phase 13 별도 승인 전 시작·기본 활성화하지 않는다.
- `Q-VAR-01`은 `DEFERRED`이며 restart evidence 전 질문·활성화하지 않는다.

## 4. Canonical Phase와 review index

상태 표기:

- **actual**: 파일이 실제 존재함.
- **reviewed**: 독립 review 문서가 존재하고 대상 문서에 안전 교정이 반영됨. 구현 완료를 뜻하지 않음.
- **gated**: entry approval 전 작업 시작 금지.
- **deferred**: restart condition 전 질문·활성화 금지.

현재 Phase/review 링크는 모두 **actual/reviewed**다. Review verdict와 구현 entry gate는 [Execution Progress and Results](execution-progress-and-results.md)의 registry를 따른다.

| Phase | 구현 주제 | 상세 문서 | Review |
|---:|---|---|---|
| 00 | Build와 architecture 뼈대 | [actual: phase-00-build-architecture-skeleton.md](phases/phase-00-build-architecture-skeleton.md) | [reviewed: phase-00-review.md](reviews/phase-00-review.md) |
| 01 | 내부 표준 입력과 정규화 | [actual: phase-01-canonical-input-normalization.md](phases/phase-01-canonical-input-normalization.md) | [reviewed: phase-01-review.md](reviews/phase-01-review.md) |
| 02 | 이동 자료 준비와 immutable problem | [actual: phase-02-prepared-travel-immutable-problem.md](phases/phase-02-prepared-travel-immutable-problem.md) | [reviewed: phase-02-review.md](reviews/phase-02-review.md) |
| 03 | 경로 전파 계산과 평가 | [actual: phase-03-route-propagation-evaluation-kernel.md](phases/phase-03-route-propagation-evaluation-kernel.md) | [reviewed: phase-03-review.md](reviews/phase-03-review.md) |
| 04 | 재사용 기능과 고객 profile | [actual: phase-04-capabilities-customer-profiles.md](phases/phase-04-capabilities-customer-profiles.md) | [reviewed: phase-04-review.md](reviews/phase-04-review.md) |
| 05 | Pickup-delivery pair, 삽입과 초기 후보군 | [actual: phase-05-pair-insertion-initial-portfolio.md](phases/phase-05-pair-insertion-initial-portfolio.md) | [reviewed: phase-05-review.md](reviews/phase-05-review.md) |
| 06 | 복사 후 변경 방식의 ALNS | [actual: phase-06-cow-alns-reproducibility.md](phases/phase-06-cow-alns-reproducibility.md) | [reviewed: phase-06-review.md](reviews/phase-06-review.md) |
| 07 | 독립 검증과 최종 결과 | [actual: phase-07-independent-verification-final-result.md](phases/phase-07-independent-verification-final-result.md) | [reviewed: phase-07-review.md](reviews/phase-07-review.md) |
| 08 | Application interface와 local 실행 | [actual: phase-08-application-ports-local-runtime.md](phases/phase-08-application-ports-local-runtime.md) | [reviewed: phase-08-review.md](reviews/phase-08-review.md) |
| 09 | DB 없는 object storage | [actual: phase-09-object-storage-no-database.md](phases/phase-09-object-storage-no-database.md) | [reviewed: phase-09-review.md](reviews/phase-09-review.md) |
| 10 | 여러 round를 조정하는 coordinator | [actual: phase-10-provider-neutral-coordinator.md](phases/phase-10-provider-neutral-coordinator.md) | [reviewed: phase-10-review.md](reviews/phase-10-review.md) |
| 11 | AWS reference distribution | [actual: phase-11-aws-reference-distribution.md](phases/phase-11-aws-reference-distribution.md) | [reviewed: phase-11-review.md](reviews/phase-11-review.md) |
| 12 | Provider substitution | [actual/gated: phase-12-provider-substitution.md](phases/phase-12-provider-substitution.md) | [reviewed/gated: phase-12-review.md](reviews/phase-12-review.md) |
| 13 | Optional hybrid — `C-17 gated` | [actual/gated: phase-13-optional-hybrid-route-selection.md](phases/phase-13-optional-hybrid-route-selection.md) | [reviewed/gated: phase-13-review.md](reviews/phase-13-review.md) |
| 14 | Official calibration/cutover — authority gated | [actual/gated: phase-14-official-calibration-cutover.md](phases/phase-14-official-calibration-cutover.md) | [reviewed/gated: phase-14-review.md](reviews/phase-14-review.md) |

## 5. Filename 규칙

상세 문서:

```text
docs/implementation/phases/phase-00-build-architecture-skeleton.md
...
docs/implementation/phases/phase-14-official-calibration-cutover.md
```

Review 문서:

```text
docs/implementation/reviews/phase-00-review.md
...
docs/implementation/reviews/phase-14-review.md
```

규칙:

1. Phase 번호는 두 자리 `00`~`14`를 사용한다.
2. 상세 slug는 §4 표에 적힌 canonical 이름을 그대로 사용한다.
3. Review filename은 `phase-NN-review.md`로 고정한다.
4. 같은 Phase의 `-v2`, `-final`, 날짜 복제 파일을 만들지 않는다. Version/status는 문서 metadata와 git history로 관리한다.
5. 상세/review 파일은 모두 actual이며 filename은 바꾸지 않는다.
6. 새 file을 만들기 전 [Execution Progress and Results](execution-progress-and-results.md)의 scheduler task ID와 entry gate를 확인한다.

## 6. Phase 작업 순서

일반 AWS ALNS-only critical path:

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 09 → 10 → 11 → 14
```

조건부 branch:

```text
10 → 12  # 승인된 provider substitution
06 + 07 + C-17 approval → 13  # optional hybrid
13 → 14  # official hybrid manifest를 선택한 경우에만
```

각 Phase 작업자는 다음 순서로 읽는다.

1. 이 README의 authority와 canonical filename.
2. [Master Realization Plan](master-realization-plan.md)의 해당 Phase 계약.
3. 해당 actual Phase 상세 문서.
4. 관련 source authority section.
5. [Execution Progress and Results](execution-progress-and-results.md)의 scheduler task/entry 상태.
6. 구현과 evidence bundle.
7. 해당 Phase review 문서와 scheduler verdict.

## 7. Progress와 review 규칙

- 문서 작성률과 구현 완료율을 섞지 않는다.
- Source/test/deployment file 존재는 Phase completion이 아니다.
- 구현 Phase는 exit evidence와 review가 모두 통과해야 `ACCEPTED`다.
- 구현자와 reviewer는 evidence/verdict를 제출하고, authoritative task registry/status/result summary는 총괄 스케줄러만 갱신한다.
- Scheduler task ID를 전달받지 못했으면 `TBD`로 남기며 임의 ID를 만들지 않는다.
- OPEN/EXPERIMENT_REQUIRED/GATED/deferred를 임의 수치·default·완료 상태로 바꾸지 않는다.
- 모든 Phase/review 상대 링크는 target과 fragment anchor를 정적으로 검사한다.
