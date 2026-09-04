---
title: Stage 4 — ALNS (상세 구현 설계)
stage: 4
date: 2026-08-10
plan: ../implementation-plan.md
sources:
  - ../domain-design.md (§9 ALNS, §6 Solution·XOR·trial, §8.3 비교, §12 오류 분류)
  - ../architecture-design.md (§2 모듈·패키지, §3.2 시간 한도)
  - stage-00-cleanup-and-skeleton.md (§3.1 이름 기준, §4.2 test 의존 고정)
  - stage-01-canonical-input-normalization.md (§2.2 DeliveryPolicy — 탐색 예산은 여기 없음)
  - stage-02-travel-and-problem-freeze.md (§2.3 Problem.compatibleVehicles)
  - stage-03-solution-propagation-evaluation.md (§2 Solution·StructureCheck, §3 RoutePropagator, §4 Evaluator·비교 규약)
revisions:
  - 2026-09-02 초기해 포트폴리오 확장 인지 — heuristics 문서의 확장 14개 편입(8 → 22개,
    [survey](stage-04-initial-solution-heuristics-survey.md))으로 개수 언급만 갱신.
    이 문서의 ALNS 계약은 무변경 (초기해는 진입점일 뿐)
  - 2026-09-02 **4-ALNS 구현 직전 정합** — (1) 초기해 개수 22 → **24개**(기본 8 + 확장 14 +
    실물 맞춤 2, [survey §2.5](stage-04-initial-solution-heuristics-survey.md))로 본문 8곳 갱신,
    §7 말미 초기해 테스트 범위 T13–T37 → **T13–T44**. (2) 문서가 정하지 않았던 것을 확정:
    `enum Termination`은 별도 파일이 아니라 **`AlnsRunStats` 안에 중첩**(선례
    `ConstructionOutcome.Status`) · `AdaptiveWeights.Outcome`도 중첩 · `RepairOperator.repair`가
    **`Profile`을 인자로 받는다** (§4.3-②의 profile hard 검증이 `InsertionSearch`에 profile을
    요구하는데 종전 시그니처에 빠져 있었다 — 2026-09-02 §4.3 개정의 누락) ·
    `RegretInsertion`은 `InsertionSearch.Cache`를 쓴다 (N4 대조는 heuristics T13b가 이미 담당,
    수락 권위는 무관) · 적응 가중치 갱신은 segment 평균(score ÷ 사용 횟수). (3) E14·T5의
    결정성 범위를 명시 — 벽시계 의존 요소(p(t) 감쇠·시간 종료)를 끈 조건에서의 결정성이다.
    (4) T12 둘째 케이스는 §3.2 오버로드에 Infeasible 손 조립 해를 넣는 것으로 —
    경로 단위 `HardConstraint`로는 빈 해를 거부할 수 없어 E16b가 SPI로 재현 불가하다.
    (5) T1 fixture를 실측으로 확정 — 순수 배송 소형 문제는 포트폴리오가 이미 최적에 닿는
    경우가 절반이라 PD 2건을 섞은 12건 문제로 (§7 T1 행)
  - 2026-09-04 **실물 fixture 실측 정합** (검토 세션 산출, 구현 전 문서 개정) —
    실물 `data/win_poc_case_floor.json`을 H23 초기해 위에서 돌려 얻은 실측으로 네 곳을 고쳤다.
    (1) **§4.3에 "이미 Infeasible인 기존 경로는 후보에서 제외"**: 실물 이동표는 D·U를 쌍마다
    그대로 받아 쓰므로 삼각부등식·대칭이 성립하지 않고(표본 150곳 331만 삼중쌍 중 거리 위반
    111,381·중앙값 864 m, 시간 위반 65,506·중앙값 163 s), 방문 하나를 빼면 남은 경로가
    Infeasible이 될 수 있다. 종전 문면은 그 경우를 버그로 보아 `AlnsSolver`가 실물에서
    **첫 반복에 예외로 죽었다**(단독 제거 452건 중 1건, 20초 루프 999회 중 8회). 폐기는
    기존 E10이 이미 규정하므로 새 edge case도 destroy 연산자 변경도 필요 없다 (N9).
    (2) **q를 비율에서 절대 개수로** — `minDestroyFraction`·`maxDestroyFraction` 삭제,
    `minDestroyCount` 5·`maxDestroyCount` 20. 종전 기본값은 452건에서 q 45~60이 되는데
    부피 여유가 4.2%뿐이라 draft의 98%가 미배정 상태로 끝났다. (3) **§4.5 acceptance에 축 가드
    한 줄 추가** — 앞 두 축(미배정·차량 수)이 동률일 때만 확률 수락. 감쇠 스케줄·필드·기본값
    0.05는 **그대로 둔다**. N1이 스칼라 Δ를 금지해 확률이 평평하므로 "거리만 조금 나쁨"과
    "미배정 증가"가 같은 확률로 통과하고, 후자를 받으면 사전식 1순위 때문에 best를 이길 수 없는
    영역에 갇힌다. (4) **`StringRemoval` 신설** (destroy 3종). 600초·seed 3개 실측:
    현행 3,499,570 m(마지막 개선 30.4초) → 개정안 3,393,528 m(반복 6배, 마지막 개선 499.8초).
    기존 엔진(Win) 3,545,031 m 대비 −4.3%. 기각·보류 내역은 §4.4 말미
  - 2026-08-10 최초 작성
  - 2026-08-10 탐색 예산을 `AlnsConfig`가 소유 (Domain §2.5.1) — idle 종료 조건 추가,
    profile 인자화, 비교를 `Scores.compare`로
  - 2026-08-10 §7에 규모 테스트 T11 추가 (Plan Stage 4 규모 DoD 대응 — Stage 2 T12와 같은
    합성 문제로 ALNS 1회, 시간 한도 안의 반복 수 기록). 다른 설계 무변경
  - 2026-08-10 D4 확정 반영 — §4.1 삽입 순서의 정렬 키를 "delivery의 **마지막** 창 close"로
    명시 (시간창이 `List<TimeWindow>`가 됨, Domain §3.2). 창 1개 입력에서는 종전과 같은 값이라
    연산자·acceptance·테스트는 무변경
  - 2026-08-11 정리 — §7 말미 문구를 Plan §1 DoD 편입으로 갱신. 설계 무변경
  - 2026-08-12 전수 감사 치명 결함 수선 — §4.2 초기해 처리 분리: StructureCheck 위반만
    `IllegalStateException`(버그)이고, Evaluator Infeasible(profile hard)은 **빈 해로 강등**한다.
    "초기해는 실패할 수 없다" 단언 폐기 — 빌더의 후보 검증(§4.3)은 경로 단위 전파뿐이라
    profile hard(§8.4)를 모른다. §5 N8 · §6 E16/E16b · §7 T12 추가, E10 문구 보완
  - 2026-08-12 감사 후속 인터뷰 정합 — `AlnsResult.bestRouteFacts` **삭제** (실측: stage-05
    수령 거부·stage-06 무소비 — 소비자 없는 선제 필드, Master §6 원칙 이행). 서두 그림·
    §1 파일 표·§2 record·§3.2 주석·§4.2 f/종료 정리
  - 2026-08-13 감사 후속 인터뷰 정합 — 존재하지 않는 `Master §3-⑪` 인용 2곳(§1 표·§3.2)을
    실제 근거인 **Domain §11.1**로 교체 (추적 장치 폐기 결정의 실소재)
  - 2026-08-13 감사 결함 정정 (분할 7 F2·F7, C-7·C-8) — 서두 "ArchUnit이 예산 도달 불가를
    강제" 거짓 인과 교정(ArchUnit은 타입 참조 차단, 값의 차단은 verify 시그니처 —
    Stage 5 §2와 정합) · §9 Q2를 해소로 갱신(stats는 run 메타에 안 실린다 — Domain §11.1,
    Stage 5 §9)하고 §3.2·§8의 같은 잔재 문구 동기. 설계 무변경
  - 2026-08-14 Domain `PICKUP_ONLY` — 삽입 위치: pickup NodeId를 0..n (DELIVERY_ONLY와
    대칭). 삽입 순서 키: delivery 창이 없으면 pickup의 마지막 창 close
  - 2026-09-02 초기해를 **결정적 construction 8개 포트폴리오**로 대체 —
    상세는 [stage-04-initial-solution-heuristics.md](stage-04-initial-solution-heuristics.md).
    §3.4 `build(Problem, Profile)` · §4.1 포트폴리오 요약 · §4.3 후보 검증에 profile hard 추가 ·
    §4.2-2 Infeasible 강등 폐기(예외로 승격) · N5·N8 개정 · E16 폐기(E16b 존치) ·
    T1·T12 개정 · §9 **Q1 해소**(시간 한도는 ALNS 루프에만)
  - 2026-09-02 **4-초기해 / 4-ALNS 분리 구현** 반영 (Plan §2.2) — 서두에 소유 범위 명시 ·
    §3.2에 `solve(problem, profile, initial)` 오버로드 추가(초기해 없이 루프 단독 검증) ·
    §4.2-2가 인자 초기해를 받도록 · §7 말미에 완료 기준의 경계 명시. 설계 무변경
  - 2026-09-02 **파일명 변경** `stage-04-initial-solution-and-alns.md` → `stage-04-alns.md`.
    4-초기해 분리로 이 문서가 초기해를 더는 소유하지 않아 옛 이름이 사실과 어긋났다.
    frontmatter `title`·H1·서두 문장도 함께 정정. 참조 10건을 전부 갱신했고,
    날짜가 박힌 과거 감사 기록(`.audit/partition-7.md`)은 **당시 사실이므로 고치지 않았다**
---

# Stage 4 — ALNS

solver-core의 `solve` 패키지에 ALNS 탐색(destroy/repair·acceptance·종료)을 만든다.
초기해는 [4-초기해](stage-04-initial-solution-heuristics.md)가 소유하고, 여기서는 그 결과를
받아 쓰기만 한다. 주 근거: [Domain §9](../domain-design.md). Stage 3이 확정한
`Solution`·`Route`·`StructureCheck`·`RoutePropagator`·`Evaluator`·`EvaluationResult`·
`Evaluation`·비교 규약(`Scores.compare < 0 ⟺ 더 좋음`)과 Stage 2·3의
`Problem`(`freeze(Plan)`·`compatibleVehicles()`·`nodeRef()`)을 그대로 잇는다 —
같은 개념에 새 이름을 짓지 않는다.

**탐색 예산은 `AlnsConfig`가 전부 소유한다** (Domain §2.5.1). `Problem`에는 시간·step·idle
한도가 없다. 재검증(Stage 5)이 이 값들을 판정에 쓰지 못하게 막는 장치는 두 겹이되 역할이
다르다 — `verify ↛ solve` ArchUnit 규칙(Architecture §2.1)은 `AlnsConfig` 등 **타입 참조**를
차단할 뿐이고, 예산 **값**의 차단은 `SolutionVerifier.verify` 시그니처에 예산 인자가 없다는
것(Stage 5 §2.1)이 담당한다 (예산 값 자체는 결과 기록용 `SearchBudget`으로 verify 패키지에
존재한다 — Stage 5 §4.1. ArchUnit green이 곧 "예산 무관 검증"의 증거는 아니다).

**이 문서는 `4-ALNS`를 소유한다 (2026-09-02).** Stage 4는 **따로 만들고 따로 끝내는 두 단계**다 —
`4-초기해`([stage-04-initial-solution-heuristics.md](stage-04-initial-solution-heuristics.md))가
green이 된 뒤 이 문서를 시작한다 (Plan §2.2). 다만 **의존은 한 방향이고 약하다**:
초기해 쪽은 ALNS 타입을 하나도 쓰지 않고, 이쪽은 §3.2의 오버로드로 초기해를 **인자로 받을 수**
있어 손 조립 해만으로 단독 검증된다. 순서를 바꿔도 막히지 않는다는 뜻이지, 권장 순서가
바뀌는 것은 아니다.

**DoD** ([Plan Stage 4 — 4-ALNS](../implementation-plan.md)): 소형 fixture에서 초기해 대비 개선
확인 · pair·XOR 불변식이 탐색 중 유지되는 property 테스트 (예: 랜덤 스텝 N회 후 구조 검사).
4-초기해의 DoD는 이 문서가 판정하지 않는다.

핵심 구도 — Domain §9.1의 한 스텝을 Stage 3 타입 위에 그대로 올린다:

```text
[Problem (동결)]  ──InitialSolutionBuilder(24개 포트폴리오)──▶  [initial Solution] = current = best
                                                      │
      ┌────────── 반복 (시간·step·idle 한도까지, Domain §12) ─────────────┐
      │ current ─destroy(pair 단위)─▶ draft ─repair(pair 삽입)─▶ draft'  │
      │ draft' ─StructureCheck─▶ 위반 있으면 예외 (구조 결함 = 버그)        │
      │        ─Evaluator(정식 평가)─▶ Infeasible이면 폐기                 │
      │        ─acceptance(Scores.compare)─▶ current/best 갱신            │
      └──────────────────────────────────────────────────────────────────┘
                                                      ▼
              [AlnsResult: best + Evaluation(③) + score(④)]  → Stage 5 재검증
```

---

## 1. 문서가 고정하는 것 / 구현·실험 재량 (Domain §9.3)

이 경계를 문서 전체에서 유지한다. 아래 "고정"을 어기면 결함이고, "재량"은 기본값 제안일 뿐이다.

| 구분 | 내용 | 근거 |
|---|---|---|
| **고정** | destroy/repair는 **pair 단위** — `RequestId`로만 넣고 뺀다. pickup만 빼는 연산은 없다 | §1.4·§9.1 MUST |
| **고정** | 뺀 Request는 bank에 정확히 한 번. 매 trial 후 XOR 성립 | §6.3·§9.1 |
| **고정** | 순서: 구조 검사 → 정식 평가 → acceptance. **수락·최종 비교의 권위는 정식 평가(`Evaluator`)뿐** — 근사·shortlist 점수로 수락 금지 | §6.5·§9.2 MUST NOT |
| **고정** | trial은 복사본(draft)에서. `current`/`best`는 별개 확정 해 — 별칭 공유 금지 | §6.5 |
| **고정** | 새 경로 시작은 실제 미사용 `VehicleId` 소비 (가짜 차량 카운트 금지) | §9.1 |
| **고정** | repair가 일부만 넣거나 0건 넣어도 정상 시도. 전부 bank인 해도 유효 | §9.1 |
| **고정** | 종료 조건 도달(시간·step·idle) = 정상 종료 — 그 시점 best를 반환하고 재검증으로 넘긴다 | §12·Master §2 |
| **고정** | 탐색은 `Problem`·이동표·profile을 수정하지 않는다 (읽기 전용) | §5 MUST |
| **고정** | 탐색 예산은 `AlnsConfig`에만 있다. `Problem`에서 예산을 읽는 코드 금지 | §2.5.1 MUST NOT |
| 재량 | 초기해 휴리스틱·개수(본 문서: **결정적 construction 24개 포트폴리오** — [상세](stage-04-initial-solution-heuristics.md)) | §9.3 |
| 재량 | 연산자 목록(본 문서: destroy **3** + repair 2)·q 범위·적응 가중치 | §9.3 |
| 재량 | acceptance 세부(동점·worse 수락 확률)·예산 기본값·시드 정책. 단 **축 가드(§4.5)는 재량이 아니라 N1의 귀결**이다 — 스칼라 Δ가 없으면 확률만으로는 열화의 크기를 구분할 수 없다 | §9.3 |
| 재량 | 삽입 후보 shortlist·증분 계산 (도입 시 노트 N4의 대조 테스트 필수) | §9.2·§6.4 |

---

## 2. 파일/클래스 목록

전부 `solver-core/src/main/java/com/ronext/rpdptw/solve/` (Architecture §2 — "solve:
Solution·전파·평가·ALNS", Stage 0 §3.1). 하위 패키지를 만들지 않는다.

| 파일 | 책임 한 줄 | 근거 |
|---|---|---|
| `solve/AlnsSolver.java` | ALNS 본체: 반복 루프·acceptance·종료·best 관리 | Domain §9 |
| `solve/AlnsConfig.java` | **탐색 예산**(시간·step·idle 한도·seed) + 알고리즘 튜닝 (§3.3) | Domain §2.5.1·§9.3 |
| `solve/AlnsResult.java` | 탐색 산출: best + `Evaluation`(③) + `long[] score`(④) + 통계 | Domain §9·§10.2 |
| `solve/AlnsRunStats.java` | 간단한 실행 통계 (반복·수락·경과·종료 사유) — 추적 장치 아님. `enum Termination`은 이 record 안에 중첩 (별도 파일 없음, 2026-09-02) | Domain §11.1 |
| `solve/InitialSolutionBuilder.java` | 초기해 포트폴리오 실행기 — 결정적 construction 24개를 돌려 정식 평가로 best 1개 선택. **파일·기법 상세는 [stage-04-initial-solution-heuristics.md](stage-04-initial-solution-heuristics.md)** | Domain §9.3 재량 |
| `solve/DestroyOperator.java` | destroy SPI: pair 단위로 빼서 bank로 | Domain §9.1 |
| `solve/RepairOperator.java` | repair SPI: bank의 Request를 pair 삽입 | Domain §9.1 |
| `solve/RandomRemoval.java` | 배정된 Request 중 무작위 q개 제거 | 재량 기본 연산자 |
| `solve/RouteRemoval.java` | 무작위 경로 하나를 통째로 제거 (차량 축 탐색용). 차량 여유가 없는 입력에서는 같은 경로가 다시 조립돼 동점이 많다 — PD·합성 문제용으로 존치 (2026-09-04 실측) | 재량 기본 연산자 |
| `solve/StringRemoval.java` | seed 근처 경로들에서 **연속 구간**(한 경로당 ≤ 10)을 잘라 q개까지 제거 — 도로를 따라 이어진 토막을 빼고 다시 잇는다 | 재량 기본 연산자 (2026-09-04) |
| `solve/GreedyInsertion.java` | 후보 중 최소 비용 위치에 순차 삽입 | 재량 기본 연산자 |
| `solve/RegretInsertion.java` | regret-2: 차선과의 격차가 큰 Request부터 삽입 | 재량 기본 연산자 |
| `solve/AdaptiveWeights.java` | 연산자 룰렛 선택 + segment 가중치 갱신. `enum Outcome` 중첩 | 재량 (ALNS 적응층) |
| `solve/InsertionSearch.java` | §4.3 삽입 후보 탐색·검증·비용의 구현체 — repair 연산자와 초기해 기법들이 공유 (노트 N5). **정의는 [heuristics 문서 §3.3](stage-04-initial-solution-heuristics.md)** | Domain §9.1 |

Stage 3 산출물(`Solution`·`StructureCheck`·`Evaluator` 등)과 `Problem`·`Profile`·`Scores`는
수정하지 않는다. 테스트 파일은 §7.

---

## 3. 시그니처

전체 구현 본문은 쓰지 않는다 — 여기 시그니처가 계약이다.
난수는 JDK `java.util.random.RandomGenerator`만 쓴다 (solver-core 외부 의존 0, Architecture §2.1).

### 3.1 연산자 SPI (Domain §9.1)

```java
public interface DestroyOperator {
    String id();
    /**
     * current에서 Request들을 pair 단위로 빼 bank에 넣은 새 Solution을 반환한다.
     * removeCount는 목표치(힌트)다 — 연산자는 pair 단위를 지키는 한 덜 뺄 수 있다.
     * 반환 해는 구조 검사를 통과해야 한다 (빈 visits 경로 금지 → 경로째 제거, §6 E4).
     */
    Solution destroy(Problem problem, Solution current, int removeCount, RandomGenerator rng);
}

public interface RepairOperator {
    String id();
    /**
     * bank의 Request들을 (이전부터 bank였던 것 포함) 가능한 만큼 pair 삽입한 새 Solution.
     * 0건 삽입도 정상 반환이다 (Domain §9.1). 반환 해는 구조 검사를 통과해야 한다.
     * profile을 받는다 — §4.3-②의 후보 검증(profile hard)이 InsertionSearch에 profile을
     * 요구하기 때문이다 (2026-09-02). destroy는 profile을 보지 않는다.
     */
    Solution repair(Problem problem, Profile profile, Solution destroyed, RandomGenerator rng);
}
```

- 두 SPI 모두 `Solution`(불변 record)을 받아 **새 인스턴스**를 만든다 — draft 복사 규칙
  (Domain §6.5)이 타입으로 지켜진다. 확정 해를 제자리에서 고칠 방법 자체가 없다.

### 3.2 solver·결과

```java
public final class AlnsSolver {
    public AlnsSolver(AlnsConfig config,
                      List<DestroyOperator> destroyOperators,
                      List<RepairOperator> repairOperators);   // 각각 1개 이상
    public static AlnsSolver withDefaults(AlnsConfig config);  // §2의 기본 연산자 5개
                                                               //   (destroy 3 + repair 2, 2026-09-04)

    /** 초기해 → ALNS 반복 → best 반환. Problem·profile은 읽기만 한다 (Domain §5).
        profile은 인자다 — Problem에 담기지 않으며, 호출자가 재검증에도 같은 인스턴스를
        넘긴다 (Domain §8.4 MUST).
        초기해는 InitialSolutionBuilder.build(problem, profile)로 만든다. */
    public AlnsResult solve(Problem problem, Profile profile);

    /** 초기해를 이미 가진 호출자용. 4-ALNS를 4-초기해와 **따로 구현·검증**하기 위한
        진입점이다 (2026-09-02, Plan §2.2) — 손 조립 해나 빈 해를 넣으면 포트폴리오
        없이도 루프 전체를 시험할 수 있다.
        받은 해는 위와 똑같이 다뤄진다: StructureCheck 위반이면 예외, Evaluator Infeasible이면
        예외(§4.2-2). 예산·acceptance·종료는 전부 동일하다. */
    public AlnsResult solve(Problem problem, Profile profile, Solution initial);
}

public record AlnsResult(
    Solution best,
    Evaluation bestEvaluation,               // 층 ③ — Stage 5 재검증의 대조 대상 (§10.2)
    long[] bestScore,                        // 층 ④ — 〃 (Arrays.equals로 대조)
    Evaluation initialEvaluation,            // DoD "초기해 대비 개선"의 기준값
                                             //   = 포트폴리오 24개 중 최선의 평가 (기준이 올라간다)
    long[] initialScore,                     // 〃 (개선 판정은 score로 — Scores.compare)
    AlnsRunStats stats) {}

public record AlnsRunStats(
    long iterations, long accepted, long infeasibleDiscarded,
    long bestImproved, long elapsedMillis,
    Termination termination) {                  // 어느 조건으로 멈췄는지 (§3.3)

    /** 별도 파일이 아니라 여기 중첩 — 선례 ConstructionOutcome.Status (2026-09-02). */
    public enum Termination { TIME_LIMIT, MAX_STEPS, IDLE_STEPS, IDLE_TIME }
}
```

- `bestEvaluation`·`bestScore`는 마지막에 새로 계산한 값이 아니라 **best 승격 시점의
  정식 평가 결과**를 그대로 보관한 것이다. 재검증(Stage 5)이 "탐색이 보고한 값"(§10.2)으로
  이 둘을 대조한다 — 캐시 ≠ 재계산이면 버그 (§6.4, 테스트 T4). (경로 facts를 운반하던
  `bestRouteFacts`는 2026-08-12 삭제 — verify 수령 거부·앱 무소비로 소비자가 없는 선제
  필드였다, Master §6.)
- `AlnsRunStats`는 로그·실험용 카운터다. 결과 JSON의 run 메타(Domain §11.1)에는 **넣지
  않는다** — run 항목은 배송정책·탐색 예산만이다 (§9 Q2 해소, Stage 5 §9).
  fingerprint·lineage류 추적 장치는 만들지 않는다 (Domain §11.1).

### 3.3 설정 (전부 재량 — Domain §9.3)

```java
public record AlnsConfig(
    // ---- 탐색 예산 (Domain §2.5.1 — Problem에 담기지 않는 값들) ----
    long timeLimitSec,                // 최대 실행 시간. 호출자가 이미 결정해 넘긴다 (아래)
    OptionalLong maxSteps,            // 최대 실행 step 수. 부재 = 시간 한도만
    OptionalLong idleSteps,           // best 미개선이 이만큼 연속되면 종료. 부재 = 미적용
    OptionalLong idleSec,             // best 미개선이 이만큼 지속되면 종료. 부재 = 미적용
    long seed,                        // 시드 정책은 호출자(Stage 6 executor) 몫 — core는 받기만
    // ---- 알고리즘 튜닝 (전부 재량) ----
    int minDestroyCount,              // q 하한 (기본 5)  — 2026-09-04 비율에서 절대 개수로
    int maxDestroyCount,              // q 상한 (기본 20) — 〃
    double worseAcceptStartProbability, // 기본 0.05 — 경과 시간에 비례해 0으로 선형 감소
                                        //   (§4.5의 축 가드와 함께 쓴다. 실측: 0.2·0.5는 더 나쁘다)
    int weightSegmentLength,          // 적응 가중치 segment (기본 100 반복)
    double weightDecay,               // ρ (기본 0.5)
    int rewardNewBest, int rewardImproved, int rewardAcceptedWorse) {  // σ₁·σ₂·σ₃ (기본 5·2·1)

    public static AlnsConfig defaults(long seed, long timeLimitSec);
}
```

**시간 한도의 출처는 core가 모른다.** `timeLimitSec`은 이미 결정된 값으로 들어온다 —
호출자(Stage 6 executor)가 `wire Termination.secondsSpentLimit ▷ application.yml 기본값`
체인을 적용한 결과다. `Problem`을 뒤져 예산을 찾는 코드는 존재하지 않는다 (Domain §2.5.1 MUST NOT).

**종료 조건 4개** — 하나라도 걸리면 종료하고 `AlnsRunStats.termination`에 어느 것인지 남긴다:

| 조건 | 의미 | 부재 시 |
|---|---|---|
| `timeLimitSec` | 경과 시간 초과 | **항상 존재** (필수) |
| `maxSteps` | 반복 횟수 초과 | 미적용 |
| `idleSteps` | best가 이 횟수만큼 연속 미개선 (정체) | 미적용 |
| `idleSec` | best가 이 시간만큼 미개선 (정체) | 미적용 |

- idle 두 축은 **best 갱신 시점**을 기준으로 리셋한다 (수락 시점이 아니다 — worse 수락이
  정체 카운터를 되돌리면 종료가 무한정 밀린다).
- 네 조건 모두 "언제 멈출지"일 뿐 **유효한 답이 무엇인지를 바꾸지 않는다.** 값이 달라지면
  결과가 달라질 수 있지만 무효가 되지는 않으며, 재검증은 이 값들을 보지 않는다 (Domain §10.2).
- 규약(wire)에 있는 것은 시간 한도뿐이다. 나머지 셋은 `application.yml`에서만 온다 (Stage 6).

### 3.4 초기해·적응층

```java
public final class InitialSolutionBuilder {
    /**
     * 결정적 construction 24개(기본 8 + 확장 14 + 실물 맞춤 2)를 우선순위 순으로 실행해 정식 평가(Evaluator)로 best 하나를
     * 고른다. 난수를 쓰지 않으므로 같은 Problem·Profile이면 같은 초기해.
     * 삽입 못 한 Request는 bank에 남는다 (유효한 해, §9.1).
     * profile을 받는다 — 후보 검증이 profile hard까지 보기 때문이다 (§4.3, 2026-09-02).
     * 기법 목록·의사코드·기권 규칙은 stage-04-initial-solution-heuristics.md.
     */
    public static InitialSolutionResult build(Problem problem, Profile profile);
}

public final class AdaptiveWeights {
    public AdaptiveWeights(int operatorCount, double decay, int segmentLength,
                           int rewardNewBest, int rewardImproved, int rewardAcceptedWorse);
    public int select(RandomGenerator rng);                  // 가중치 비례 룰렛
    public void reward(int operatorIndex, Outcome outcome);  // 중첩 enum NEW_BEST/IMPROVED/ACCEPTED_WORSE
    public void endIteration();                              // segment 경계에서 w ← (1−ρ)w + ρ·(score ÷ 사용 횟수)
                                                             //   (그 segment에 미사용이면 w 유지)
}
```

---

## 4. 절차

### 4.1 초기해 생성 (`InitialSolutionBuilder.build`) — 재량 기본

**상세는 [stage-04-initial-solution-heuristics.md](stage-04-initial-solution-heuristics.md).**
여기서는 ALNS가 의존하는 계약만 적는다.

```text
1. 우선순위 순으로 고정된 결정적 construction 24개를 순서대로 실행한다 (난수 없음).
   그 Problem에서 성립하지 않는 기법은 기권하고 건너뛴다 (예: PICKUP_DELIVERY가 있으면
   savings·sweep 계열).
2. 각 결과는 StructureCheck 통과 + Evaluator Feasible이어야 한다 — 아니면 예외(버그).
   §4.3의 후보 검증이 Evaluator가 보는 것 전부를 이미 보기 때문이다.
3. best = Scores.compare 최소. 동률이면 우선순위가 앞선 기법이 이긴다 (결정적).
4. 전원 기권이면 빈 해(전 Request bank)를 반환한다 — 유효한 해다 (§9.1).
5. 초기해에는 시간 상한이 없다. 종료는 시간이 아니라 구조로 보장한다
   (모든 바깥 루프가 매 반복에서 Request 하나를 삽입하거나 제외하므로 ≤ |requests|).
```

- 종전의 "결정적 greedy 1개"는 포트폴리오 중 `deadline-sequential` 하나로 편입됐다 (2026-09-02).
- 여기서 폐기된 채로 남는 것은 **이전 설계의 근사 phase-1 screening 2단계 파이프라인**이다
  (Domain §9.3). 다수 후보를 만들어 **정식 평가로** 고르는 것은 그 구조가 아니다.
- 호환 차량 0대·시간창 불가능 Request가 bank에 남아도 정상이다 — 사유 기록은 Stage 5의 일
  (bank는 ID만, §6.3 MUST NOT).

### 4.2 ALNS 루프 (`AlnsSolver.solve`)

```text
1. 준비    rng = config.seed 기반 RandomGenerator.
          deadline = now + config.timeLimitSec.        // 예산은 이미 결정돼 들어온다 (§3.3)
          lastBestStep = 0, lastBestAt = now.          // idle 카운터 기준점
          weights = destroy·repair 각각 AdaptiveWeights (초기 가중치 균등).
2. 초기해  initial = 인자로 받았으면 그것, 아니면
                    InitialSolutionBuilder.build(problem, profile).best().  // §3.2 오버로드
          StructureCheck 위반 → IllegalStateException (버그 — §12 구조 결함).
          Evaluator Infeasible → IllegalStateException (2026-09-02 개정 — 버그다.
           §4.3의 후보 검증이 전파·호환·profile hard를 전부 보므로 construction 결과가
           Infeasible일 수 없다. 종전의 "빈 해 강등"(E16·N8)은 후보 검증이 profile hard를
           모르던 시절의 규칙이라 폐기).
          전원 기권이어서 빈 해가 왔는데 그마저 Infeasible → IllegalStateException
           (profile 구성 결함 — E16b, 존치).
          current = best = initial. currentEval = bestEval = 그 해의 평가.
          currentScore = bestScore = 그 해의 score.
3. 반복    while (종료 조건 미충족):
        종료 조건 = now ≥ deadline                                    → TIME_LIMIT
                 | maxSteps 존재 ∧ iterations ≥ maxSteps             → MAX_STEPS
                 | idleSteps 존재 ∧ iterations − lastBestStep ≥ 그 값 → IDLE_STEPS
                 | idleSec  존재 ∧ now − lastBestAt ≥ 그 값           → IDLE_TIME
   a. q = clamp(minDestroyCount + rng.nextInt(maxDestroyCount − minDestroyCount + 1), 1, 배정 수).
      **비율이 아니라 절대 개수다** (2026-09-04) — 적재 여유가 작은 입력에서 비율로 뽑으면
      뺀 것을 다시 못 넣어 draft가 늘 미배정으로 끝난다 (§4.4 말미 실측).
      배정 0건이면 destroy 생략 (draft = current — repair만 시도).
   b. destroy = weights로 룰렛 선택 → draft = destroy(problem, current, q, rng).
   c. repair  = weights로 룰렛 선택 → draft' = repair(problem, profile, draft, rng).
   d. StructureCheck.check(problem, draft') 비어 있지 않으면 IllegalStateException —
      연산자 버그를 조용히 버리지 않는다 (노트 N2).
   e. result = Evaluator.evaluate(problem, profile, draft').
      Infeasible → 폐기, infeasibleDiscarded++, 다음 반복 (연산자가 경로 단위로 사전
      검증하므로 드물어야 정상 — §6 E10).
   f. acceptance (§4.5): 수락이면 current = draft', currentEval·currentScore = 평가 결과.
      Scores.compare(score, bestScore) < 0 이면 best = draft', bestEval·bestScore 갱신,
      bestImproved++, lastBestStep = iterations, lastBestAt = now.   // ← idle 리셋은 여기서만
   g. 두 연산자에 결과 보상 기록(NEW_BEST / IMPROVED = 수락 ∧ cmp < 0 ∧ best 아님 /
      ACCEPTED_WORSE = 수락 ∧ cmp > 0. 동점 수락은 보상 없음), weights.endIteration().
4. 종료    AlnsResult(best, bestEval, bestScore, initialEval, initialScore, stats) 반환.
          어느 조건으로 멈췄든 정상 종료다 — 그 시점 best로 재검증 진행 (Domain §12).
          반복 0회(한도가 이미 지남)면 initial이 곧 best다.
```

- **idle 리셋은 best 갱신(f)에서만** 한다. worse 수락에서 리셋하면 정체 종료가 무한정 밀린다.

### 4.3 삽입 후보 탐색 (repair 공통 루틴)

Request 하나를 경로 하나에 넣는 후보 나열과 검증. `GreedyInsertion`·`RegretInsertion`·
`InitialSolutionBuilder`가 공유한다 (노트 N5).

```text
대상 경로:  problem.compatibleVehicles(requestId)에 든 차량의 기존 경로
          + 그중 미사용 차량 하나로 여는 새 경로 (실제 VehicleId 소비 — §9.1.
            미사용 호환 차량이 여럿이면 VehicleId 문자열 순 첫 번째만 후보 — 결정성, 노트 N3).
          단 **삽입 전 상태에서 이미 Infeasible인 기존 경로는 제외**한다 (후보 0개로 취급,
            2026-09-04). destroy가 만들 수 있는 상태이지 버그가 아니다 — 이동표가 삼각부등식을
            지키지 않으면 방문을 빼는 것만으로 뒤 방문이 창을 넘길 수 있다 (노트 N9).
            그 경로를 품은 draft는 §4.2-e의 정식 평가에서 Infeasible로 폐기된다 (E10).
위치:      DELIVERY_ONLY — delivery NodeId를 각 삽입 위치 0..n에.
          PICKUP_ONLY — pickup NodeId를 각 삽입 위치 0..n에 (DELIVERY_ONLY와 대칭).
          PICKUP_DELIVERY — pickup 위치 i ≤ delivery 위치 j 의 모든 (i, j) 쌍.
            픽업 선행이 후보 생성 규칙 자체로 보장된다 (§1.4·§9.1).
검증:      ① 후보 방문 목록으로 RoutePropagator.propagate(problem, vehicleId, visits′) —
             Infeasible이면 후보 탈락. (그 경로의 정확한 물리 계산이지 근사가 아니다 — 노트 N7.)
          ② profile.hardConstraints() 전부 satisfied(problem, facts) — 거짓이면 탈락
             (2026-09-02 추가). HardConstraint가 시그니처상 경로 단위라 이 검증은 완결적이고,
             그래서 §4.1 초기해와 루프의 draft 모두 profile hard로 Infeasible이 될 수 없다.
             해 전체 수준 요구는 이 SPI로 표현 불가 — 점수 축으로 정의한다 (E16b).
비용(재량): (새 경로 여부, ΔdriveDistMeter, ΔrouteOperationalTimeSec) 사전식 —
          기존 경로 우선, 거리 증가 최소. 이 비용은 후보 '고르기'에만 쓴다.
          수락은 §4.4의 정식 평가만이 결정한다 (§9.2 MUST NOT).
```

### 4.4 연산자 5개 (재량 기본값 — 각 한 스텝)

```text
RandomRemoval.destroy(current, q):
  1. 배정된 RequestId 수집 (경로 방문을 problem.nodeRef로 역참조, RequestId 정렬 — 노트 N3).
  2. rng로 q개 선택. 각 Request의 방문 NodeId 전부(=pair)를 경로에서 제거.
  3. 빈 visits가 된 경로는 경로째 제거 (§6 E4). 제거된 RequestId를 bank에 추가.

RouteRemoval.destroy(current, q):
  1. 경로가 없으면 current 그대로. 있으면 rng로 경로 하나 선택 (removeCount 무시 — 힌트).
  2. 그 경로 전체 제거, 소유 Request 전부 bank로. (usedVehicleCount 축을 직접 흔든다 —
     사용 차량 수가 비교 2순위인 default profile에서 유효한 이웃.)

StringRemoval.destroy(current, q):        (2026-09-04 신설)
  1. 배정 Request 중 rng로 seed 하나. 각 경로를 "seed와 최근접 방문의 이동표 거리"로 오름차순
     정렬 (동률은 VehicleId 문자열 순 — 노트 N3).
  2. 가까운 경로부터: 그 경로의 최근접 방문을 중심으로 길이 len = 1 + rng.nextInt(min(10, 남은 q))
     의 **연속 구간**을 잘라 제거 목록에 넣는다. 목록이 q개에 닿으면 중단.
  3. 목록의 Request를 pair 단위로 제거. 빈 visits가 된 경로는 경로째 제거 (§6 E4).

GreedyInsertion.repair(destroyed):
  1. bank 전체를 RequestId 정렬 후 rng로 순서 셔플.
  2. 순서대로: §4.3 후보 탐색 → 최소 비용 후보에 삽입, bank에서 제거. 후보 0개면 남긴다.

RegretInsertion.repair(destroyed):
  1. bank의 각 Request에 대해 §4.3 최선·차선 후보 비용 계산.
  2. regret = 차선 − 최선 (ΔdriveDistMeter 기준, 차선 없으면 +∞ 취급 — 지금 못 넣으면
     기회를 잃는 Request 우선). regret 최대 Request를 최선 위치에 삽입.
  3. 삽입할 때마다 영향 경로의 후보 비용 재계산. bank가 비거나 전원 후보 0개면 종료.
     재계산은 InsertionSearch.Cache(호출마다 새로 만듦)로 — 바뀐 경로만 재전파한다.
     상위 3개 캐시가 최선·차선을 정확히 보존함은 heuristics T13b가 대조한다 (N4).
     후보 선택에만 쓰이고 수락은 §4.2-e의 정식 평가뿐이므로 acceptance 권위와 무관하다.
     regret 동률은 RequestId 문자열 순 (rng 미사용 — 결정적).
```

**연산자 선정의 실측 근거 (2026-09-04).** 실물 fixture(`data/win_poc_case_floor.json` — 주문 452·
차량 31·부피 여유 4.2%·존과 차급이 거의 1:1)를 H23 초기해에서 출발시켜 60초·seed 5개로 골랐고,
정한 조합을 600초·seed 3개로 확인했다. 판정은 전부 `Scores.compare`와 총거리(축 3)다 —
모든 실행에서 미배정 0·차량 31로 앞 두 축이 같기 때문이다. 기준점은 기존 엔진(Win)의
`data/alns_result.csv`를 우리 전파·평가로 재계산한 3,545,031 m이고, 초기해는 4,198,408 m다.

| 600초 · seed 3개 | 거리 평균 (m) | 반복 | 마지막 개선 | Win 대비 |
|---|---:|---:|---:|---:|
| 개정 전 기본값 (destroy 2·q 45~60·축 가드 없음) | 3,499,570 | 12,089 | 30.4초 | −1.3% |
| **개정 후 기본값** (destroy 3·q 5~20·축 가드) | **3,393,528** | 72,603 | 499.8초 | **−4.3%** |

개정 전 설정은 30초 만에 개선이 멈춰 예산의 5%만 쓴다. 개정 후는 반복이 6배이고 종료 직전까지
best가 갱신된다.

**채택.** `StringRemoval` — 60초 단독 비교에서 destroy 중 최선이었다(3,396,071. 무작위 3,795,147 ·
경로통째 3,640,326 · 거리 유사 3,609,932 · 존 내부 3,711,232). 비삼각 이동표에서는 지리적으로
이어진 토막을 통째로 빼고 다시 잇는 형태가 실제 개선으로 이어진다. 무작위 제거는 뺀 것의
77~86%가 원래 차로 되돌아갔다.

**기각·보류** (전부 60초·seed 3~5개 실측, 거리 평균 m):

| 후보 | 결과 | 판정 |
|---|---|---|
| worst removal (제거 절감 큰 순) | 3,931,714 · 반복당 소요 2배 | 기각 |
| Shaw removal (거리 관련도) | 3,609,932, 혼합 시 희석(3,435,534) | 기각 — Stage 8 실험 후보로 기록 |
| Shaw removal (서비스 시각 관련도) | 3,690,385 | 기각 — 거리 기준보다 못하다 |
| zone removal (존 내부 q개) | 3,527,287 | 기각 — string이 존 내부 구간을 이미 포함한다 |
| regret-3 · 수요(부피) 내림차순 삽입 | 혼합에서 3,435,492 vs 현행 3,434,885 | 기각 — 이득 0 |
| 삽입 비용 노이즈 (±20%) | 3,688,358 | 기각 — 분산만 커진다 |
| 삽입 후 2-opt·or-opt (손댄 경로만) | 3,421,162 vs 3,434,885 (0.4%)·반복당 +30% | 보류 — Stage 8 |
| late acceptance (L=50) | 600초 3,386,256 vs 채택안 3,393,528 (0.2%, 범위 겹침) | 보류 — 새 상태(이력 배열)를 요구한다 |

### 4.5 acceptance (비교 부호만 사용 — 노트 N1)

```text
cmp = Scores.compare(draftScore, currentScore)      // < 0 ⟺ draft가 더 좋음
cmp ≤ 0  → 수락 (동점 수락 = 재량 결정 — 정체 구간 표류 허용, Stage 3 E27 인계).
cmp > 0  → **앞 두 축이 동률일 때만** 확률 수락 (축 가드, 2026-09-04):
             draftScore[i] == currentScore[i]  (i = 0 .. min(2, 축 개수) − 1)
             ∧ rng.nextDouble() < p(t)
           축이 1개뿐인 profile이면 그 하나만 본다 — 길이를 가정하지 않는다.
           p(t) = worseAcceptStartProbability × (1 − 경과/한도)  (선형 감쇠, 재량 — 무변경)
best 갱신은 항상 strict: Scores.compare(draftScore, bestScore) < 0 일 때만.
```

- 감쇠에 쓰는 "경과/한도"는 `timeLimitSec` 기준이다 — idle 한도는 감쇠에 쓰지 않는다
  (그 둘을 섞으면 정체 시 수락 확률이 튀어 정체를 더 길게 만든다).
- **축 가드가 왜 필요한가 (N1의 직접 귀결, 2026-09-04).** 고전 SA는 `exp(−Δ/T)`로 "조금 나쁨"과
  "많이 나쁨"을 구분해 후자를 거의 받지 않는다. N1이 그 스칼라 Δ를 금지하므로 이 규칙의 확률은
  **평평하다** — 거리가 1 m 나쁜 draft와 미배정이 늘어난 draft가 같은 확률로 통과한다. 후자를
  수락하면 current가 미배정이 있는 영역으로 옮겨 가고, 사전식 1순위가 미배정이라 그 영역에서
  만들어지는 어떤 draft도 (미배정 0인) best를 이길 수 없어 탐색이 사실상 멈춘다. 사전식 축 순서
  자체를 크기 필터로 쓰면 스칼라 Δ 없이 같은 구분이 생긴다 — score 배열의 축별 비교만 쓰므로
  N1 경계 안이다. 감쇠 스케줄("초반에 많이, 나중에 적게")은 이 개정으로 **비로소 의도대로 작동한다**.
- 실측 (2026-09-04). destroy·q를 고정하고 acceptance만 바꾼 비교다.

  | 60초 · seed 5개 · destroy = 연속 구간 고정 | 거리 평균 (m) | best 갱신 | 마지막 개선 |
  |---|---:|---:|---:|
  | 축 가드 없이 확률 0.05 (개정 전) | 3,533,856 | 79 | 10.7초 |
  | 축 가드 없이 확률 0.1 | 3,512,889 | 74 | 12.0초 |
  | 축 가드 + 확률 0.5 | 3,422,724 | 155 | 54.0초 |
  | 축 가드 + 확률 0.2 | 3,416,885 | 191 | 45.0초 |
  | 확률 0 (worse 수락 안 함) | 3,403,217 | 225 | 58.4초 |

  가드 없이 확률을 올리면 무너진다. 별도로 destroy 5종을 섞어 잰 값에서는 **평평한 확률 0.5가
  3,979,130**이고 seed 5개 중 2개가 60초 동안 초기해를 한 번도 넘지 못했다 — 같은 0.5라도
  가드를 붙이면 3,421,881로 뒤집힌다.

  | 600초 · seed 3개 · 개정 후 기본 조합 | 거리 평균 (m) | 마지막 개선 |
  |---|---:|---:|
  | **축 가드 + 확률 0.05 (채택)** | **3,393,528** | 499.8초 |
  | 축 가드 + 확률 0.2 | 3,400,431 | 513.4초 |
  | 축 가드 + 확률 0.5 | 3,418,617 | 558.2초 |

  가드가 붙으면 **시작 확률은 현행 기본값 0.05가 최선**이라 그 값을 바꾸지 않는다.
- **실측이 뒷받침하는 범위** (정직하게 적어 둔다): 이 fixture는 전 실행에서 차량 수가 31로 고정돼
  축 1이 나빠지는 draft 자체가 없었다(전 차량 사용 상태라 늘어날 수 없다). 따라서 위 수치가 증명한
  것은 **축 0(미배정) 가드의 효과**이고, 축 1까지 보는 것은 같은 논리("앞쪽 축이 나빠지면 되돌아올
  수 없다")를 보수적으로 확장한 것이다. 축 1만 따로 검증한 실측은 없다 — 필요하면 Stage 8에서 뗀다.
- 동점 수락(cmp = 0)은 유지한다. 금지하면 수락률이 10%로 떨어져 정체한다 (20초 3,581,618 vs 3,592,361).

---

## 5. 설계 노트

| # | 내용 |
|---|---|
| N1 | **acceptance에 스칼라 Δ 없음**: 고전 SA의 `exp(−Δ/T)`는 두 해의 점수 차를 한 숫자로 요구한다. 비교는 사전식이고 축을 한 숫자로 뭉개지 않는다(§8.3)가 확정이므로, 기본 acceptance는 비교 **부호**와 감쇠 확률만 쓴다. 다른 규칙(late acceptance 등)으로의 교체는 §9.3 재량 — 단 "정식 평가가 낸 `long[] score`만 입력"이라는 경계는 유지 |
| N2 | **구조 위반은 폐기가 아니라 예외**: 구조 결함은 품질 문제가 아니라 버그다 (§1.4·§6.3·§12). draft를 조용히 버리면 버그가 재검증(Stage 5)까지 숨는다. `IllegalStateException`으로 solve를 중단시키고 executor(Stage 6)가 FAILED로 기록한다 |
| N3 | **같은 seed = 같은 결과**: `Set`·`Map` 순회 순서에 의존하지 않도록 연산자는 후보를 ID 문자열 정렬 후 rng를 적용한다. 시드 정책 자체는 재량(§9.3)이지만, 시드가 주어졌을 때의 결정성은 테스트 안정성(T1·T5)의 전제라 기본 연산자의 계약으로 둔다 |
| N4 | **증분 계산은 아직 없다**: 이 Stage의 평가는 항상 전체 재계산(Stage 3 N7)이고 후보 검증은 경로 단위 전파다. 규모(경로 ~30 × 방문 ~20)에서 충분하다. 증분 캐시·shortlist를 나중에 넣는 것은 재량이나, 도입 시 "캐시 점수 = 전체 재계산 점수" 대조 테스트(§6.4)가 필수다 |
| N5 | **초기해 = 빈 해의 repair**: 대다수 construction이 §4.3 후보 탐색을 서로 다른 결정적 순서로 쓰는 특수 사례다 (Split·path-extension 계열만 자체 전파를 쓴다 — heuristics §5). 삽입 루틴(`InsertionSearch`)을 하나만 구현·검증하면 construction들과 repair 연산자가 함께 그것을 쓴다 (2026-09-02 — 기법이 몇 개가 돼도 이 노트의 취지는 그대로다) |
| N6 | **Stage 5 인계**: 재검증 진입값은 `AlnsResult.best`(분해는 Stage 3 N6 — routes/bank가 곧 domain 타입 분해값)와 `bestEvaluation`(점수 대조 대상, §10.2)이다. verify가 `solve` 타입을 직접 받을 수 없으므로(ArchUnit) 변환 어댑팅은 Stage 5가 정의한다 |
| N7 | **경로 단위 전파는 근사가 아니다**: `RoutePropagator`는 그 경로의 정확한 물리·hard 판정이다(§7). 다만 호환성·profile hard·해 전체 집계는 `Evaluator`만 하므로, 수락 직전의 전체 정식 평가는 생략할 수 없다 (§4.2-e가 항상 돈다) |
| N9 | **제거는 feasibility를 보존하지 않는다** (2026-09-04 실측): 이동표는 입력에 있는 구간의 D·U를 그대로 쓰므로(`TravelMatrix.prepare` — 무보정) 삼각부등식·대칭이 성립하지 않는다. 실물 fixture 표본 150곳 331만 삼중쌍에서 거리 위반 111,381건(중앙값 864 m·최대 25,934 m)·시간 위반 65,506건(중앙값 163 s·최대 999 s)이다. 그래서 방문 하나를 빼면 앞뒤 직행이 경유보다 오래 걸려 뒤 방문이 시간창·reqDate·근무창을 넘길 수 있다 — **"기존 경로는 항상 Feasible"은 불변식이 아니다.** 처리는 §4.3의 후보 제외 + 기존 E10 폐기로 끝난다(destroy 연산자에 복구 로직을 넣지 않는다). 재검증(Stage 5)·벤치 해석(Stage 8)도 이 성질을 전제해야 한다 |
| N10 | **T11(합성 문제)은 해의 품질을 잴 수 없다** (2026-09-04): 그 문제의 이동표는 전 arc가 같은 값(1,000 m·80 s)이라 방문 순서를 어떻게 바꿔도 거리·시간 축이 변하지 않는다. "반복은 도는데 전부 동점"은 버그가 아니라 입력의 성질이다. 품질·개선폭 판정은 실물 fixture(T15)와 Stage 8에서만 한다 |
| N8 | **초기해 Infeasible은 버그다** (2026-09-02 개정): 종전 N8은 "`build(problem)`이 profile을 받지 않으므로 profile hard를 지킬 방법이 없다"를 근거로 Infeasible 초기해를 정상 경로로 두고 빈 해 강등을 규정했다. 그 전제가 사라졌다 — `build(problem, profile)`이 profile을 받고 §4.3-②가 후보 단계에서 hard를 보므로, construction 결과의 Infeasible은 논리적으로 불가능하다. "Stage 8에서 필요가 확인되면"으로 미뤄 뒀던 개선을, 포트폴리오 선택이 정식 평가로 이뤄지는 이상 Infeasible 후보를 만들어 버리는 것이 낭비라 당겨 적용했다. 빈 해 강등(E16)은 폐기, E16b는 존치 |

---

## 6. Edge case 표

| # | 상황 | 처리 | 근거 |
|---|---|---|---|
| E1 | requests 빈 목록 | initial = (routes ∅, bank ∅), 반복은 돌지만 no-op — 즉시라도 best 유효 | §9.1·Stage 2 E19 |
| E2 | vehicles 빈 목록 / 전 Request 호환 0대 | 전부 bank인 초기해로 진행·반환 (유효한 해) | §3.4·§9.1 |
| E3 | q > 배정 수 | 배정 수로 clamp. 배정 0이면 destroy 생략 (§4.2-a) | §9.3 재량 |
| E4 | destroy로 경로의 마지막 Request 제거 | 경로째 제거 — 빈 visits Route는 생성 불가 (Stage 3 E30). 차량은 미사용 풀로 복귀 | §6.2·§8.2 |
| E5 | repair 0건 삽입 | 정상 시도 — draft는 대개 미배정 증가로 패배, acceptance가 판단 | §9.1 |
| E6 | destroy 이전부터 bank였던 Request | repair 대상에 포함 — 매 trial이 재배치 기회 | §6.3 (bank는 ID 집합일 뿐) |
| E7 | PD 삽입 위치 | (i ≤ j) 쌍만 생성 — 픽업 선행이 규칙으로 보장. i = j는 pickup 바로 뒤 delivery. `PICKUP_ONLY`는 pickup 위치 0..n만 (E7b) | §1.4 |
| E7b | PICKUP_ONLY 삽입 위치 | pickup NodeId를 각 위치 0..n. delivery 위치 쌍을 만들지 않는다 | §1.3·§1.4 |
| E8 | 동점 (cmp == 0) | current 교체 수락, best는 불변 (strict <) | §8.3·Stage 3 E27 인계 |
| E9 | 시간 한도가 초기해 생성 중 지남 | 반복 0회, initial = best 반환 — FAILED 아님 | §12 "탐색 중단 = 정상" |
| E10 | draft'가 Evaluator Infeasible | 폐기 + 카운트. 연산자는 경로 전파로 사전 검증하므로 default profile에서 빈발하면 연산자 버그 신호 — profile hard가 있는 고객은 버그 없이도 잦을 수 있다 (N8. 로그로 관찰). **이동표가 삼각부등식을 지키지 않으면 destroy만으로도 발생한다** — default profile에서도 정상이고 버그 신호가 아니다 (N9·§4.3, 2026-09-04 실측 8/999회) | §8.1 (hard 감점 통과 금지) |
| E11 | 한 trial이 deadline을 넘겨 끝남 | 다음 반복 조건에서 종료 — 약간의 초과는 허용 (연산자는 deadline을 모른다) | §12·재량 |
| E11b | idle 한도와 시간 한도가 동시에 걸림 | 검사 순서대로 첫 번째 것을 `termination`에 기록 (TIME_LIMIT 우선). 어느 쪽이든 정상 종료 | §3.3 |
| E12 | 연산자가 비호환 차량에 삽입 | 정상 경로에선 불가능(§4.3 사전 필터). 뚫리면 Evaluator INCOMPATIBLE_VEHICLE → 폐기 | §3.4·Stage 3 §4.2 |
| E13 | 시간 한도 없이 step 한도만 | 불가능 — `timeLimitSec`은 필수 필드다 (호출자가 항상 결정해 넘긴다). 네 조건 전부 검사 | §3.3·§4.2-3 |
| E13b | `idleSteps`·`idleSec` 둘 다 부재 | 정상 — 시간·step 한도만으로 종료 (기존 동작과 동일) | §3.3 |
| E14 | 같은 seed·같은 Problem 재실행 | 동일한 AlnsResult (노트 N3). 단 벽시계 의존 요소 — p(t) 감쇠(§4.5)·TIME_LIMIT/IDLE_TIME 종료 — 는 실행마다 달라질 수 있으므로, 계약은 **`maxSteps`/`idleSteps` 종료 + `worseAcceptStartProbability = 0`** 조건에서의 결정성이다 (2026-09-02) | 재량이되 계약화 |
| E15 | destroy가 removeCount보다 덜 뺌 (RouteRemoval 등) | 허용 — removeCount는 힌트 (§3.1) | §9.3 재량 |
| E16 | 초기해 평가가 Infeasible (profile hard) | **폐기 (2026-09-02).** §4.3-②가 후보 단계에서 profile hard를 보므로 이 상황이 생기지 않는다. 생기면 IllegalStateException (버그 — §4.2-2·N8) | §8.4·§4.3 |
| E16b | 빈 해 평가마저 Infeasible | IllegalStateException — 빈 해를 거부하는 hard(최소 배정량 등)는 이 루프가 탐색할 수 없다(Infeasible엔 score가 없어 acceptance 불능). 그런 요구는 hard가 아니라 점수 축으로 정의한다 — profile 구성 결함으로 즉시 드러낸다 | §8.1·§8.3·N2 |

---

## 7. 테스트 목록 — DoD 1:1 대응

위치: `solver-core/src/test/java/com/ronext/rpdptw/solve/`. 의존은 JUnit만 —
jqwik류 property 라이브러리를 추가하지 않는다 (Stage 0 §4.2가 test 의존을 JUnit·ArchUnit으로
고정). property 테스트는 seed 루프로 손수 만든다. Problem은 Stage 1·2 경로로 손 조립한다
(fixture JSON 파싱은 Stage 6 — Stage 1 §7과 동일 원칙).

**예외는 T15 하나다** (2026-09-04 추가) — 실물 fixture를 읽어야 하므로 `app` 모듈에 두고
T44(`WinPocFixtureTest`)의 **테스트 전용 매핑을 재사용**한다. 그 매핑은 T44·T15 전용이고
정식 adapter는 Stage 6이 만든다 (heuristics 문서 §8 T44와 같은 규칙).

| # | 테스트 | 내용 | 대응 DoD 문장 |
|---|---|---|---|
| T1 | `AlnsSolverTest.improvesOverInitialOnSmallFixture` | 소형 fixture(손 조립 — depot 왕복 차량 2대(무게 20,000)·DELIVERY_ONLY 10건(4시간 창)·PICKUP_DELIVERY 2건, 좌표 유래 이동표, 좌표는 테스트 상수) + 고정 seed + `maxSteps` 상한 → `Scores.compare(bestScore, initialScore) < 0`. **기준값이 포트폴리오 24개 중 최선**이라 이 단언은 엄격하다 — 2026-09-02 실측: 순수 배송 소형 문제(8~16건·차량 2~3대·창 유무) 48건 중 23건은 포트폴리오가 이미 ALNS와 같은 점수(개선 0)라 종전의 "두 클러스터 배송" 설계를 버리고 PD를 섞었다(PD 포함 48건 중 개선 0은 3건). 채택 fixture: 최선 spatiotemporal-cluster [0, 2, 44151, 37307] → ALNS [0, 2, 38764, ·], seed 6개·step 500~1500 전부 개선 | "소형 fixture에서 초기해 대비 개선 확인" |
| T2 | `AlnsInvariantPropertyTest.structureHoldsUnderRandomSteps` | seed ~20개 × 랜덤 연산자 시퀀스 ~200스텝: **destroy 직후와 repair 직후 각각** `StructureCheck.check` 위반 0 단언 + 각 스텝의 배정↔bank 이동이 Request 단위(부분 pair 이동 없음)임을 단언. PICKUP_DELIVERY 포함 문제로 수행. **연산자 목록에 `StringRemoval`을 포함하고, 삼각부등식을 어기는 이동표로도 수행한다** (§4.3의 후보 제외 경로를 밟게 — 2026-09-04) | "pair·XOR 불변식이 탐색 중 유지되는 property 테스트 (랜덤 스텝 N회 후 구조 검사)" |
| T3 | `AlnsSolverTest.stopsAtTimeLimitAndReturnsBest` | 아주 짧은 한도로 solve → 정상 반환·best 존재·`stats.elapsedMillis` 기록·`termination == TIME_LIMIT` (E9 포함: 한도 0 → initial 반환) | (Plan 범위 문장 "시간 한도 종료") |
| T4 | `AlnsSolverTest.reportedEvaluationMatchesFreshEvaluation` | solve 후 `Evaluator.evaluate(problem, profile, result.best())`를 새로 실행 → `bestEvaluation`은 record 동등, `bestScore`는 `Arrays.equals` (§6.4 "캐시 = 재계산", 체크리스트 #7 — Stage 5 대조의 전제) | (Plan 범위 문장 "acceptance" — 수락 권위가 정식 평가임의 증명) |
| T5 | `AlnsSolverTest.sameSeedSameResult` | 같은 seed 두 번 solve → best 해·`bestEvaluation` 동등, `bestScore`는 `Arrays.equals`, `stats.iterations`·`accepted` 동일 (노트 N3). E14의 조건대로 `maxSteps` 종료 + `worseAcceptStartProbability = 0`으로 돌린다. `AlnsResult` 자체를 `equals`로 비교하지 않는다 — 배열 필드 때문 (Stage 3 §4.3 공통 규칙) | (T1·T2의 안정성 전제) |
| T6 | `InsertionOperatorsTest.pairInsertionKeepsPickupFirst` | PD Request 삽입 후보가 전부 i ≤ j (E7), 삽입 결과 경로에서 픽업 선행 | (Plan 범위 문장 "destroy/repair(pair 단위)") |
| T7 | `RemovalOperatorsTest.pairRemovalAndEmptiedRouteDrop` | PD 제거 시 두 방문이 함께 사라짐 · E3 clamp · E4 빈 경로 제거 | 〃 |
| T7b | `InsertionOperatorsTest.infeasibleExistingRouteYieldsNoCandidates` | **2026-09-04 추가.** 삼각부등식을 어기는 손 조립 이동표(t(A,C) > t(A,B) + t(B,C)이고 C의 창 close가 그 사이)로 B를 뺀 경로를 만든 뒤, 그 경로를 대상으로 `InsertionSearch.candidates`를 부른다 → **예외 없이** 그 경로에서 후보 0개. 이어서 그 해를 `Evaluator`에 넣으면 Infeasible이고 `AlnsSolver`가 예외 없이 폐기하는 것까지 (§4.3·E10·N9) | (§4.3 개정의 직접 검증) |
| T7c | `RemovalOperatorsTest.stringRemovalTakesContiguousSegments` | **2026-09-04 추가.** `StringRemoval`이 seed 근처 경로에서 **연속 구간**을 q개까지 pair 단위로 빼고, 빈 경로는 사라지며(E4), 같은 seed면 같은 결과임(N3) | (§4.4 신설 연산자) |
| T8 | `InsertionOperatorsTest.usesOnlyCompatibleRealVehicles` | 비호환 차량 후보 없음(E12) · 새 경로는 실제 미사용 VehicleId(§9.1) · 호환 0대 Request는 bank 유지(E2) | 〃 |
| T9 | `AlnsSolverTest.degenerateProblemsReturnValidResult` | E1(주문 0)·E2(차량 0) → 예외 없이 유효한 AlnsResult | (Plan 범위 문장 "초기해 생성") |
| T10 | `AlnsSolverTest.stopsOnIdleLimits` | 넉넉한 시간 한도 + 작은 `idleSteps`(및 별도 케이스로 `idleSec`) → 한도 훨씬 전에 종료하고 `termination`이 IDLE_STEPS / IDLE_TIME. worse 수락이 일어나도 idle 카운터가 리셋되지 않음을 단언 (§4.2-f) | (본 개정에서 추가된 종료 조건의 직접 검증) |
| T10b | `AlnsSolverTest.worseAcceptanceRequiresEqualLeadingAxes` | **2026-09-04 추가.** `worseAcceptStartProbability = 1.0`으로 두고, 손 조립으로 (a) 미배정이 1건 늘어난 draft와 (b) 미배정·차량 수는 같고 거리만 나쁜 draft를 각각 만들어 → (a)는 **절대 수락되지 않고** (b)는 수락되는 것을 단언 (§4.5 축 가드) | (§4.5 개정의 직접 검증) |
| T11 | `AlnsScaleTest.runsOnFullScaleSyntheticProblem` | **규모 측정.** Stage 2 T12와 **같은 합성 문제**(장소 453·주문 452·차량 31·이동표 453² 전 쌍)로 `AlnsSolver.solve` 1회 → 시간 한도 안에 정상 종료. **시간 한도 안에서 몇 번 반복했는지(`AlnsRunStats`의 반복·수락 수, `elapsedMillis`)를 출력해 기록한다.** 해의 품질·개선폭은 판정하지 않는다 (그건 Stage 8). 실물 JSON은 읽지 않는다 — 입력은 프로그램으로 조립한다. **품질을 판정할 수 없는 이유**: 이 합성 문제의 이동표는 전 arc가 같은 값이라 거리·시간 축이 재배치로 변하지 않는다 (노트 N10, 2026-09-04) | Plan Stage 4 "**규모**" 문장 |
| T15 | `app` 모듈 `WinPocAlnsTest`(T44의 테스트 전용 매핑 재사용) | **2026-09-04 추가 — 실물 fixture 회귀.** `data/win_poc_case_floor.json`을 Problem까지 올리고, H23(`ZoneQuotaBalancedFillConstruction`)의 해를 §3.2 오버로드에 넣어 돌린다 (포트폴리오 24개를 다시 돌리지 않는다 — 그 선택은 T44가 이미 고정했고 CI 시간을 줄인다). 단언은 **예외 없이 정상 종료**(§4.3 개정의 실증 — 개정 전에는 첫 반복에서 죽었다) · `bestScore[0] == 0` · `[1] == 31` · 거리 < 초기해. 반복 수·`infeasibleDiscarded`·거리를 출력해 Stage 8 표의 입력으로 남긴다. **품질 판정선(Win 대비 몇 %)은 두지 않는다** — 그건 Stage 8이고, 시간 한도에 따라 값이 달라진다. 시간 한도는 CI에서 짧게 (초기해가 대부분을 차지하므로 수십 초) | (§4.3·§4.4·§4.5 개정의 통합 검증) |
| T12 | `AlnsSolverTest.profileHardIsHonoredFromConstruction` | **2026-09-02 개정** (종전 `infeasibleInitialFallsBackToEmptySolution`은 E16 폐기와 함께 폐기). 테스트 전용 hard 제약 profile("경로당 방문 1개 초과 금지")로 solve → 초기해가 **Feasible**이고 넣지 못한 Request는 bank에 남는다 · 루프 전체가 예외 없이 진행 · 결과 유효. 별도 케이스: §3.2 오버로드에 **Evaluator Infeasible인 손 조립 초기해**를 넣으면 `IllegalStateException` (§4.2-2). 경로 단위 `HardConstraint`는 경로가 없는 빈 해를 거부할 수 없어 E16b는 SPI로 재현 불가 — 빌더의 E16b 검사는 방어용으로만 남는다 (2026-09-02) | (§4.3-② 후보 검증의 직접 검증) |

T3–T10은 Plan DoD 요약 문장 밖이지만 Plan Stage 4 범위 문장("초기해 생성, destroy/repair(pair 단위),
acceptance, 시간 한도 종료")의 직접 검증이다 — Plan §1의 편입(2026-08-11)에 따라 이 표 전부가
완료 기준이다.

**이 표는 `4-ALNS`의 완료 기준 전부다 (2026-09-02, T7b·T7c·T10b·T15는 2026-09-04 추가).**
초기해 포트폴리오의 테스트(T13–T44, T44는 app 모듈의 실물 fixture 테스트)는
[heuristics 문서 §8](stage-04-initial-solution-heuristics.md)이 소유하며 `4-초기해`에서
먼저 green이 된다. T1·T5·T9·T11·T12는 기본 경로(포트폴리오로 초기해를 만드는 경로)를 쓰고,
나머지는 §3.2 오버로드에 손 조립 초기해(T15는 H23의 해)를 넣어 포트폴리오와 무관하게 돌릴 수 있다.
T11·T15는 규모·실물 실행이라 수십 초 걸린다 — 단일 테스트 실행 시 제외하는 방법은 CLAUDE.md의
명령 절에 있다.

---

## 8. 이 Stage에서 하지 않는 것

| 안 하는 것 | 담당 | 근거 |
|---|---|---|
| 재검증(`verify`)·점수 대조 실행·미배정 사유(`NO_COMPATIBLE_VEHICLE` 등) | Stage 5 | Domain §10–§11 |
| 결과 JSON·run 메타 확정 (stats는 미포함으로 종결 — §9 Q2) | Stage 5·6 | Domain §11, §9 Q2 |
| wire `Termination.secondsSpentLimit` 파싱·executor의 전체 시간 관리·config 주입 | Stage 6 | Architecture §3.2, Stage 1 §2.2 |
| 탐색 파라미터 튜닝·Win 지표 비교 (§3.3 기본값의 실측 조정) | Stage 8 | Plan §1 Stage 8 |
| 증분 평가·삽입 캐시의 실제 도입 | 재량 (필요 시) | Domain §9.2, 노트 N4 |
| 추가 연산자 (Shaw/worst/zone removal, regret-3, 수요순 삽입, 삽입 후 2-opt·or-opt 등) — SPI로 열려 있음 | 필요 시 (Stage 8 실험) | Domain §9.3 · §4.4 말미의 기각·보류 기록 |
| 병렬·분산 탐색, MIP 재조합 | 범위 밖 | Master §4·§6 |
| profile별 탐색 예산 차등 (profile은 hard 제약·score 축만 소유) | 안 함 | Domain §8.4·§2.5.1 |
| property 라이브러리(jqwik 등) 의존 추가 | 안 함 | Stage 0 §4.2 |
| `Solution`·`Evaluator` 등 Stage 3 타입 변경 | 안 함 (그대로 소비) | Stage 3 §2–§4 |
| 초기해 기법 24개의 파일·의사코드·기권 규칙·테스트 | [stage-04-initial-solution-heuristics.md](stage-04-initial-solution-heuristics.md) | 이 문서는 ALNS 본체만 소유 |

---

## 9. 미해결 질문

닫힌 질문은 해소 표시를 달아 남긴다 ([README](README.md) 공통 규칙, 2026-08-13) —
**이 절에 남은 미결은 없다** (Q1이 2026-09-02에 닫혔다).

| # | 질문 | 잠정 처리 |
|---|---|---|
| Q1 | wire `Termination.secondsSpentLimit`의 적용 범위 — ALNS 루프만인지, 초기해·Problem 동결·재검증까지 포함한 전체 풀이인지 어느 문서도 정의하지 않았다 | **해소 (2026-09-02).** `timeLimitSec`은 **ALNS 반복 루프에만** 적용한다 — 초기해에는 시간 상한이 없다. 초기해의 종료는 시간이 아니라 구조가 보장하고(모든 바깥 루프가 ≤ \|requests\|), 시간 컷오프는 같은 입력에 대한 결정성(E14)을 깨므로 채택하지 않는다. 전체 풀이 타임박스(초기해+ALNS+재검증)는 Stage 6 executor 몫이다 (Architecture §3.2와 정합). 본문 반영: §4.2-1 주석·[heuristics 문서 §4.3](stage-04-initial-solution-heuristics.md) |
| Q2 | `AlnsRunStats`(반복·수락 수 등)를 결과 JSON run 메타에 넣을지 | **해소 (2026-08-11, Stage 5 §9).** 넣지 않는다 — Domain §11.1 run 항목은 배송정책·탐색 예산(§2.5.1의 시간·step·idle·seed)만 따로 기록하고, stats(반복·수락 카운터·종료 사유)는 로그·실험용으로만 남는다 (§3.2 주석 동기) |
