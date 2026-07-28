# RPDPTW 사람용 구현 가이드

> 상태: Phase 00~14 작성·독립 리뷰·필요한 correction·원 리뷰어 재검증 완료 (`ACCEPTED`)
> 대상 독자: Java 기초 지식과 CVRPTW 구현 경험은 있으나 이 저장소의 RPDPTW 도메인·아키텍처 계약을 처음 접하는 구현자
> 실제 구현 상태: **0% — 이 문서 집합을 만드는 작업은 코드를 구현하지 않는다.**

## 1. 목적

이 문서 집합은 `docs/implementation/phases/`의 canonical 구현 설계를 사람이 이해하고 직접 구현할 수 있는 교육형 작업 지시서로 재구성한다. 단순한 작업 목록이나 복사 가능한 완성 코드를 제공하는 대신 다음을 연결한다.

- 제품과 RPDPTW 도메인의 배경
- Phase가 존재하는 이유와 앞뒤 Phase의 계약
- 현재 저장소와 목표 상태의 차이
- 사람이 읽고 판단하고 승인받아야 할 지점
- 순서가 있는 작업 패키지와 검증·실패 해석·rollback
- 실제 구현 Phase의 entry/exit gate와 evidence

이 디렉터리의 문서가 원래 설계의 권위를 대체하지는 않는다. 충돌 시 아래 source authority를 따른다.

## 2. Phase 수량 보정

사용자 요청의 “총 14개의 phase”라는 표현과 달리 현재 repository inventory 및 canonical realization plan에는 `phase-00`부터 `phase-14`까지 **15개 Phase**가 있다.

- 근거: [`../README.md`](../README.md)의 canonical Phase index
- 근거: [`../master-realization-plan.md`](../master-realization-plan.md)의 `Phase 00`~`Phase 14`
- 근거: [`../phases/`](../phases/)에 존재하는 15개 canonical 설계 파일

Phase 00은 build/reactor/module DAG와 architecture entry gate를 세우므로 생략하거나 다른 Phase에 병합할 수 없다. 따라서 이 가이드 집합은 Phase 00~14 모두를 독립된 guide와 독립된 review 대상으로 유지한다.

## 3. Source authority와 결정 상태

최소 권위 입력은 다음과 같다.

1. [`../../master-design.md`](../../master-design.md)
2. [`../../2026-07-26-domain-design.md`](../../2026-07-26-domain-design.md)
3. [`../../2026-07-26-architecture-design.md`](../../2026-07-26-architecture-design.md)
4. [`../../architecture-domain-implementation-design.md`](../../architecture-domain-implementation-design.md)
5. [`../../master-design-open-questions.md`](../../master-design-open-questions.md)
6. [`../README.md`](../README.md)
7. [`../master-realization-plan.md`](../master-realization-plan.md)
8. [`../execution-progress-and-results.md`](../execution-progress-and-results.md)
9. 각 Phase의 [`../phases/`](../phases/) 원본과 [`../reviews/`](../reviews/) 원본

`docs/2026-07-26-master-design.md`와 `docs/codex/`는 historical cross-check 용도이며 현재 권위 문서가 아니다. `OPEN`, `GATED`, `deferred`, `EXPERIMENT_REQUIRED` 상태는 숫자·provider·정책의 숨은 기본값으로 닫지 않는다. 특히 Phase 13 optional activation gate와 Phase 14 official calibration/production authority gate를 문서 편의를 위해 우회하지 않는다.

## 4. 권장 읽기 순서

처음 구현하는 사람은 다음 순서를 따른다.

1. 이 README와 [`execution-progress-and-results.md`](execution-progress-and-results.md)에서 문서 상태와 실제 구현 상태를 분리해 확인한다.
2. `master-design.md`의 빠른 탐색 지도, 구현 흐름, 불변조건, roadmap을 읽는다.
3. domain design의 “CVRPTW 개발자를 위한 RPDPTW 입문”과 architecture design의 module/runtime 경계를 읽는다.
4. 현재 Phase guide의 “시작 전 읽기 순서”와 entry gate를 확인한다.
5. 인접 Phase guide의 producer/consumer artifact를 확인한다.
6. 원래 Phase 설계와 review를 함께 열어 exact gate와 known risk를 대조한다.
7. guide의 학습 경로와 작업 패키지 순서대로 직접 구현하고 evidence를 남긴다.

## 5. Canonical Phase index

| Phase | 사람용 guide | 독립 review | 실제 구현 상태 |
|---|---|---|---|
| 00 | [Build와 architecture skeleton](phases/phase-00-human-implementation-guide.md) | [review](reviews/phase-00-review.md) | NOT_STARTED |
| 01 | [Canonical input와 normalization](phases/phase-01-human-implementation-guide.md) | [review](reviews/phase-01-review.md) | NOT_STARTED |
| 02 | [Prepared travel과 immutable problem](phases/phase-02-human-implementation-guide.md) | [review](reviews/phase-02-review.md) | NOT_STARTED |
| 03 | [Route propagation과 evaluation kernel](phases/phase-03-human-implementation-guide.md) | [review](reviews/phase-03-review.md) | NOT_STARTED |
| 04 | [Capability와 customer profile](phases/phase-04-human-implementation-guide.md) | [review](reviews/phase-04-review.md) | NOT_STARTED |
| 05 | [Pair insertion과 initial portfolio](phases/phase-05-human-implementation-guide.md) | [review](reviews/phase-05-review.md) | NOT_STARTED |
| 06 | [COW ALNS와 reproducibility](phases/phase-06-human-implementation-guide.md) | [review](reviews/phase-06-review.md) | NOT_STARTED |
| 07 | [Independent verification과 final result](phases/phase-07-human-implementation-guide.md) | [review](reviews/phase-07-review.md) | NOT_STARTED |
| 08 | [Application ports와 local runtime](phases/phase-08-human-implementation-guide.md) | [review](reviews/phase-08-review.md) | NOT_STARTED |
| 09 | [Object storage와 no-database architecture](phases/phase-09-human-implementation-guide.md) | [review](reviews/phase-09-review.md) | NOT_STARTED |
| 10 | [Provider-neutral coordinator](phases/phase-10-human-implementation-guide.md) | [review](reviews/phase-10-review.md) | NOT_STARTED |
| 11 | [AWS reference distribution](phases/phase-11-human-implementation-guide.md) | [review](reviews/phase-11-review.md) | NOT_STARTED |
| 12 | [Provider substitution](phases/phase-12-human-implementation-guide.md) | [review](reviews/phase-12-review.md) | NOT_STARTED |
| 13 | [Optional hybrid route selection](phases/phase-13-human-implementation-guide.md) | [review](reviews/phase-13-review.md) | NOT_STARTED |
| 14 | [Official calibration과 cutover](phases/phase-14-human-implementation-guide.md) | [review](reviews/phase-14-review.md) | NOT_STARTED |

모든 guide와 독립 review 링크는 최종 정적 검증에서 존재·non-empty·fragment resolve를 확인했다.

## 6. Guide와 review의 역할

각 guide는 현재 저장소 inventory를 근거로 `존재`, `부재`, `placeholder`, `proposed/open`을 구별한다. Java type이나 method signature 후보는 확정 계약과 제안을 명시적으로 나누며, 그대로 붙여 넣는 완성 구현 대신 skeletal contract와 구현자가 답해야 할 질문을 제공한다.

각 review는 guide 작성 작업과 다른 새 Codex 작업이 수행한다. Reviewer는 target guide를 고치지 않고 finding을 보고한다. Target 변경이 필요한 finding은 별도 correction 작업만 수정하며, 원 reviewer가 read-only follow-up으로 closure를 재검증한다.

## 7. 완료의 두 가지 의미

- **문서 작성 완료율**: guide 작성, 독립 review, 필요한 correction과 재검증이 끝난 비율이다.
- **실제 구현 완료율**: 코드·test·deployment와 Phase exit evidence가 충족된 비율이다.

이번 작업은 전자만 완료하는 문서 작업이다. 문서가 모두 승인되어도 실제 구현은 `0%`이며 Phase exit를 통과한 것으로 표시하지 않는다.
