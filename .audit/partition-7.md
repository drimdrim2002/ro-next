# 분할 7 감사 보고서 — stage-04 · stage-05 (초점: 재검증의 탐색 독립성)

감사일: 2026-08-11~12 · 감사자: doc-audit 분할 7

## 0. 필수 선행 보고

### 0.1 대상 파일 실제 줄 수 (`wc -l`)

```
$ wc -l docs/implementation/stage-04-initial-solution-and-alns.md \
        docs/implementation/stage-05-verification-and-result.md
     438 docs/implementation/stage-04-initial-solution-and-alns.md
     492 docs/implementation/stage-05-verification-and-result.md
     930 total
```

### 0.2 기계 검사 5종 — 명령과 건수

| 검사 | 명령 | 건수 | 판정 결과 |
|---|---|---|---|
| a. 링크 | `grep -noE '\[[^]]*\]\(([^)]+)\)' stage-04*.md stage-05*.md` | 6건 (stage-04 2 · stage-05 4) | 대상 파일(`../domain-design.md`, `../implementation-plan.md`) 전부 실재 — 끊어진 링크 0 |
| b. 전칭·부정어 | `grep -nE '전부\|모두\|하나도\|0건\|없다\|유일\|항상\|절대\|언제나'` | stage-04 27줄 · stage-05 34줄 | 전 건 판정 §1.1 — 결함 2건 (F1·F2), 나머지 Domain·fixture 실측과 일치 |
| c. 상태어 | `grep -nE '해소\|확정\|종결\|보류\|잠정\|미정\|TODO\|재검토'` | stage-04 9줄 · stage-05 18줄 | 전 건 판정 §1.2 — 결함 1건 (F7), 나머지 현재 상태와 일치 |
| d. 참조 | `grep -oE '§[0-9.]+\|Stage [0-9]\|위 표\|아래 주석\|이미 [^ ]*는'` | stage-04 207건 · stage-05 326건 ("위 표" 2건은 "단**위 표**현"의 오탐 — 실제 서술형 '위 표' 참조 0) | Domain·Master 앵커 전수 검증 (§1.3) — 끊어진 참조 1건 (F3, `Master §3-⑪` 4회). 타 Stage 앵커는 분할 경계로 미확인 목록화 (§3) |
| e. fixture | `python3` — `data/win_poc_case_floor.json` 키·건수 재계산 (아래 0.2.1) | 문서 주장 10건 대조 | 전부 일치 — 결함 0 |

#### 0.2.1 fixture 대조 상세 (검사 e)

실측: orders **452** · vehicles **31** · distanceMatrix **205,209 = 453²** · 장소 452+차고 1 = **453** ·
차고 1건 `locTcd=START_CENTER`(endDepot 없음) · 차고 창 `00:00:00~23:59:59`(전일창) ·
주문 시간창은 단일 `openTime`/`closeTime` 문자열(창 1개, 목록형 0건) · pickup 관련 키 없음(전건 DELIVERY_ONLY 형태) ·
`options.Termination.secondsSpentLimit` 존재 · `options.waitInDepot` 존재 ·
vehicle 키 7개(`vehicleId·vehicleFeature·maxWeight·maxVolume·workStartTime·workEndTime·speed`).

→ stage-04 T11(장소 453·주문 452·차량 31·이동표 453² 전 쌍), stage-05 E22(창 1개·차고 전일창·endDepot 없음),
stage-05 Q4("현 규약 전건 DELIVERY_ONLY"), stage-04 §3.3(wire에 시간 한도만 존재) **전부 fixture와 일치**.

### 0.3 정독 범위

- stage-04-initial-solution-and-alns.md: **1~438행 전부** (frontmatter 포함)
- stage-05-verification-and-result.md: **1~492행 전부** (frontmatter 포함)
- 대조용 상위 문서(전문 정독 아님, 해당 절만): Domain §1.3~1.4·§2.5.1·§2.6·§3.2·§5·§6·§7.1·§7.3~7.5·§8·§9·§10·§11·§12,
  Master §2·§3·§5·§6. 타 Stage 문서(00~03·06~08)·Architecture·Plan은 분할 경계로 열지 않음
  (예외: D2 전칭 주장 검증을 위한 `secondsSpentLimit` 단일 키워드 grep 1회 — §1.1 참조).

---

## 1. 기계 검사 목록별 판정

### 1.1 전칭·부정어 (61줄) 판정 요약

| 주장 (대표 행) | 판정 |
|---|---|
| stage-04:34 "탐색 예산은 AlnsConfig가 전부 소유 · Problem에는 없다" | 일치 — Domain §2.5.1 MUST NOT, §5 표 실재 |
| stage-04:35-36 / stage-05:41 "재검증이 도달할 수 없고 ArchUnit이 강제" | **결함 F2** |
| stage-04:64 "pickup만 빼는 연산은 없다" | 일치 — Domain §1.4-1 |
| stage-04:69·127 "0건 삽입·전부 bank도 유효" | 일치 — Domain §9.1 |
| stage-04:205·380 "timeLimitSec 항상 존재·E13 불가능" | 문서 내 정합 (§3.3 필수 필드 선언과 일치) |
| stage-04:212-213 "종료 조건은 유효한 답을 바꾸지 않는다 · 재검증은 보지 않는다" | 일치 — Domain §2.5.1·§10.2 |
| stage-04:263 "초기해는 실패할 수 없다" | **결함 F1** |
| stage-04:341 "best 갱신 항상 strict" | 문서 내 정합 (§4.5·E8) |
| stage-04:437 Q1 "적용 범위를 어느 문서도 정의하지 않았다" | 확인됨 — `grep -rn secondsSpentLimit docs/`(현행 문서 9건) 결과 어느 곳도 초기해·동결·재검증 포함 여부를 정의하지 않음. Domain §2.5.1은 "탐색을 언제 멈출지"까지만 |
| stage-05:44 "시그니처에 Stage 4 타입이 하나도 없어" | 일치 — verify 인자는 Problem·Profile·Map·Set·Evaluation·long[] 뿐 |
| stage-05:45 "Stage 4 의존은 T8 하나뿐" | 일치 — §4 ResultAssembler는 Pass+RunStamp만 사용, T5는 Stage 3 Evaluator 사용 |
| stage-05:56·98 "유일한 발행 규칙" | 일치 — Master §3 결정 6·§5 |
| stage-05:227 "replay가 표 밖의 이동값을 얻을 수 없다 (전 쌍 완비)" | fixture 일치 (205,209 = 453² 전 쌍). Stage 2 §4 절차 5 자체는 미확인(분할 6) |
| stage-05:264·379-380 "항상 DONE · FAILED 생산 경로 없음 · verified 항상 true" | 일치 — Domain §11.1 (2026-08-11 명시) |
| stage-05:301 "예시 목록 그대로 — 추가하지 않음" | 일치 — Domain §11.1 사유 4개 동일 |
| 그 외 (표 머리글 "전부 ~디렉터리", 서술 반복 등) | 규칙 주장 아님 또는 상기 판정에 포섭 — 결함 없음 |

### 1.2 상태어 (27줄) 판정 요약

| 항목 | 판정 |
|---|---|
| stage-04:19 / stage-05:20·198·457·477 "D4 확정" | 일치 — Domain §3.2 전개 규칙·§7.1 차고 창 실재 |
| stage-05:195·417 "시간창 close 확정 해석" | 일치 — Domain §7.1 MUST 문장 실재 |
| stage-05:490 Q3 "해소 (2026-08-11)" | 일치 — Domain §11.1 "FAILED는 열거 호환용… (2026-08-11 명시)" 실재 |
| stage-04:438 Q2 "부분 해소" vs stage-05:473 "Stage 4 Q2 해소" | **결함 F7** (상태 불일치) |
| 나머지 ("확정 해", "확정한다" 등 용어·미래 시제) | 상태 표기 아님 — 결함 없음 |

### 1.3 참조 판정 요약 (Domain·Master 앵커)

검증 완료(실재·내용 일치): Domain §1.3·§1.4·§2.5.1·§2.6·§3.2·§3.4·§5·§6.2~6.5·§7.1(절차 0~8)·
§7.3(`departure`·`interWorkWindowRestTime`·항등식)·§8.1(층 ③·④)·§8.3·§8.4·§9.1~9.3·§10.1~10.3·
§11.1(run 항목·경로 시각 2종 2026-08-11 추가·사유 4개)·§11.2·§12, Master §2-③·§3-⑥·§5·§6.
**불일치 1건: `Master §3-⑪` — Master §3 결정 표는 10행뿐이다 (결함 F3).**
Master §5를 "2단 verifier 폐기" 근거로 든 공동 인용(stage-05:472)은 약하나 주 근거 Domain §10.2에 폐기 문장이 실재해 결함으로 세지 않음.

---

## 2. 결함 목록 (심각도 순)

### [D1] docs/implementation/stage-04-initial-solution-and-alns.md:262-264
문제 — §4.2 절차 2가 "초기해는 경로 단위 전파를 통과한 삽입만 했으므로 (Evaluator Infeasible이) 실패할 수 없다"며 실패 시 `IllegalStateException`(= 버그)으로 규정하지만, 이 전칭 주장은 **profile hard 제약을 빠뜨렸다** — 초기해 후보 검증(§4.3)은 전파+호환 필터뿐이고, profile hard는 같은 문서 N7이 인정하듯 "Evaluator만" 본다.
근거 — stage-04:359 N7 "호환성·profile hard·해 전체 집계는 `Evaluator`만 하므로" + §3.4 `InitialSolutionBuilder.build(Problem)`은 profile을 받지도 않음. Domain §8.4는 "추가 hard 제약"을 정식 확장 지점으로 정의(844행), Domain §12의 '구조 결함' 분류는 pair 분리·XOR·캐시≠재계산뿐(963행) — profile hard 위반 초기해는 구조 결함이 아니다. 같은 상황을 루프 안 draft는 "폐기"(§4.2-e, E10)로 처리해 비대칭. 오독 시나리오: 그대로 구현하면 hard 제약이 있는 profile 고객에서 초기해가 첫 평가에서 Infeasible → 예외 → executor FAILED.
증상 — hard 제약 profile을 쓰는 고객(§8.4 확장 지점 2)의 요청이 유효한 해(최소한 빈 해)가 존재해도 **전건 초기해 단계에서 FAILED**. default profile·fixture에서는 잠복해 테스트로 안 잡힌다. stage-05 E15("항상-false profile hard … 탐색도 못 만든 해")의 전제도 실제로는 crash로 실현돼 어긋난다.
권고 — 문구 분리 + 설계 보완: "실패할 수 없다"의 주어를 '구조·전파 위반'으로 한정하고, 초기해의 Evaluator Infeasible은 예외가 아니라 폐기·후퇴(예: 위반 경로 해체 후 bank 또는 빈 해로 후퇴) 경로로 정의하거나 `InitialSolutionBuilder`가 profile을 받아 삽입 시 정식 평가로 검증하게 개정.

### [D2] docs/implementation/stage-04-initial-solution-and-alns.md:34-36 · stage-05-verification-and-result.md:40-41
문제 — "재검증이 탐색 예산에 **도달할 수 없고** `verify ↛ solve` **ArchUnit 규칙이 그것을 강제한다**"는 두 문서 공통 단언이 거짓 인과다 — ArchUnit은 **타입 참조**만 막고, 예산 **값**은 stage-05 자신의 설계(§4.1 `SolveResult.SearchBudget`·`RunStamp`)가 verify 패키지 소유 record로 들여온다 (Domain §11.1이 run 메타 기록을 요구하므로 들여와야만 한다).
근거 — stage-05:279-281 `SearchBudget(timeLimitSec, maxSteps, idleSteps, idleSec, seed, …)`와 stage-05:305-307 `RunStamp(… SolveResult.SearchBudget searchBudget)`가 verify 패키지에 정의됨. 실제 보장 장치는 `SolutionVerifier.verify` 시그니처에 예산 인자가 없다는 것(stage-05:121-126)과 구현 규율뿐. 오독 시나리오: 리뷰어가 ArchUnit green을 "예산 무관 검증"의 증거로 믿고, 누군가 verify 검사 로직에 `RunStamp`를 인자로 추가해도(컴파일·ArchUnit 모두 통과) 독립성 훼손을 못 잡는다.
증상 — 재검증 독립성의 방어선이 실제보다 강하게 인식됨. CLAUDE.md가 경고하는 "green이 곧 경계 준수의 근거는 아니다"와 같은 함정을 문서가 직접 심는다.
권고 — 문구 교정: "ArchUnit이 `AlnsConfig` 타입 참조를 막고, **값의 차단은 `SolutionVerifier.verify` 시그니처(예산 인자 없음)가 담당한다** — `SearchBudget` 값은 결과 기록용으로만 verify에 들어온다(§4.1)"로 두 문서 모두 분리 서술.

### [D4] docs/implementation/stage-04-initial-solution-and-alns.md:90·172 · stage-05-verification-and-result.md:473·476
문제 — "추적 장치 금지"의 근거로 4회 인용된 `Master §3-⑪`이 존재하지 않는다 — Master §3 핵심 결정 표는 10행에서 끝난다.
근거 — `grep -cE '^\| [0-9]+ \|' docs/master-design.md` → **10**. `grep -rn '§3-⑪' docs --include='*.md'`(현행) → stage-04 2건·stage-05 2건·stage-08 1건(타 분할). 잔존 근거는 Domain §11.1 마지막 불릿("fingerprint·algorithm lineage 등 추적 장치는 … run 메타로 대체 (확정)", 945행)에 실재.
증상 — 인용을 따라간 리뷰어가 결정 ⑪을 찾지 못해 "결정이 삭제됐나, 규칙이 아직 유효한가"를 다시 묻게 된다. 규칙 자체는 Domain §11.1로 살아 있어 구현은 틀리지 않는다.
권고 — 실측 후 교정: 4곳 인용을 `Domain §11.1`(또는 실재하는 Master 앵커)로 바꾸거나, Master §3에 해당 결정이 있어야 한다면 상위 문서 개정. stage-08의 동일 인용은 분할 8로 이관.

### [D1] docs/implementation/stage-05-verification-and-result.md:211-249 (§3 재검증 절차)
문제 — 절차 5~7(재전파·호환·profile hard)에서 위반이 나왔을 때 절차 8·9(metric·score 재계산 대조)를 실행하는지 생략하는지 정의가 없다 — 구조 위반에는 관문(절차 4)이 있는데 물리 위반 뒤에는 관문이 없다.
근거 — 절차 8 "recomputed = Evaluation.aggregate(**재계산 facts 전부**, …)": `RouteReplay.Outcome.Violated`(stage-05:180-182)는 facts를 담지 않으므로 위반 경로가 있으면 '전부'가 부분집합이 된다. "번호 순서대로 … 위반은 던지지 않고 수집한다"(207-208행)를 문자대로 따르면 부분 facts로 집계 → reported와 필연적 불일치 → 진짜 위반에 **가짜 SCORE_MISMATCH가 추가 수집**된다. 오독 시나리오: FAILED 원인 기록(§12)에 hard 위반과 점수 불일치가 함께 남아 "탐색이 점수도 조작했다"로 오진.
증상 — PASS/FAIL 판정 자체는 안 바뀌나(이미 Fail 확정) FAILED 원인 진단이 오염되고, 구현자마다 8·9 실행 여부가 갈려 T1~T3 오염 테스트의 기대 위반 목록이 구현별로 달라진다.
권고 — 문구 보완: 절차 7 뒤에 "5~7 위반이 하나라도 있으면 8·9를 생략하고 Fail" 관문 한 줄 추가 (절차 4와 동형).

### [D3] docs/implementation/stage-05-verification-and-result.md:279-281·473 (SearchBudget.termination)
문제 — `SearchBudget`이 `String termination`을 포함하는데, 이 값의 출처는 `AlnsRunStats.termination`(stage-04 §3.2)이라 같은 record의 주석 "**AlnsConfig의 예산 부분만** 결과용으로 옮긴 값"(278행), §9의 "run 메타에 `AlnsRunStats` 포함 안 함 — **stats는 로그·실험용으로만**"(473행), Domain §11.1 run 항목(예산 = §2.5.1의 시간·step·idle·seed — termination 없음)과 세 겹으로 모순된다.
근거 — Domain §2.5.1 표(330-333행)의 예산 값 목록에 termination 없음. Domain §11.1(921-925행) run 항목에도 없음. stage-04:160-165 `AlnsRunStats(… Termination termination)` — termination은 실행 통계다.
증상 — 구현자가 "termination을 넣는 게 맞나, stats 배제 원칙 위반인가"를 다시 묻게 되고, RunStamp를 채우는 Stage 6 구현이 어느 선언을 따를지 갈린다.
권고 — 문구 분리: termination을 포함시키는 결정이면 "예산 + 종료 사유 1개(예외 편입, Domain §11.1 개정 필요)"로 명시하고 §9 행·record 주석을 함께 교정; 아니면 필드 삭제.

### [D3] docs/implementation/stage-04-initial-solution-and-alns.md:53·151-158 (AlnsResult.bestRouteFacts)
문제 — `AlnsResult.bestRouteFacts`는 두 문서 어디에도 소비자가 없는 선제 필드이고, 서두 그림(53행)은 그것이 "→ Stage 5 재검증"으로 흘러가는 것처럼 그려 stage-05와 어긋난다.
근거 — stage-05:143-145 "탐색의 `bestRouteFacts`는 verify가 **받지 않는다**", stage-05:340 "조립에 쓰지 않는다"(N4), stage-05:471 "경로별 대조 | 안 함". Master §6(114-118행) "그 전에 미리 필드를 만들어 두지 않는다 … 이 원칙을 전 항목에 적용했다 (2026-08-11)" — 나중에 넣는 비용이 지금과 같은 순수 add-only 필드다. 오독 시나리오: 그림만 본 Stage 6 구현자가 bestRouteFacts를 검증 입력으로 배선하려다 verify 시그니처와 충돌.
증상 — 소비처 없는 필드가 "왜 있고 어디로 가는가"를 다시 묻게 만든다. (stage-06이 로그 등으로 소비하는지는 분할 8 확인 필요 — 소비처가 있으면 그림 수정만 남는다.)
권고 — 삭제(필드·그림의 RouteFacts 표기) 또는 소비처를 한 줄 명시 — Master §6 원칙과의 정합을 분할 B에서 최종 판정.

### [D5] docs/implementation/stage-04-initial-solution-and-alns.md:438 (§9 Q2)
문제 — stage-04 Q2는 "부분 해소 … Stage 5·6에서 결정"으로 미결처럼 남아 있는데, stage-05:473이 이미 "Stage 4 Q2 해소: stats는 로그·실험용으로만"으로 종결을 선언했다.
근거 — 두 행 인용 그대로. stage-04는 2026-08-11 개정(revisions 22행)까지 거쳤는데 Q2를 갱신하지 않았다.
증상 — stage-04만 읽는 구현자가 닫힌 결정을 다시 고민한다.
권고 — stage-04 Q2를 "해소 (2026-08-11, Stage 5 §9)"로 갱신 (F5의 termination 모순 해소와 함께).

### [D1] docs/implementation/stage-05-verification-and-result.md:192-193 (§2.3 공유 목록)
문제 — "공유하는 것은 … **`profile`의** 값 타입·공식(`RouteFacts.routeOperationalTimeSec()` 등)뿐"이라고 썼는데 `RouteFacts`는 profile이 아니라 core `eval` 소유다 — 같은 문서 N1(374행)은 "core `eval`의 값 타입과 공식 (Stage 3 N1이 이 목적으로 배치)"으로 옳게 서술한다.
근거 — 두 행 인용 대조. 오독 시나리오: §2.3만 읽으면 값 타입·공식이 profile 모듈 소유로 보여 공유 허용 경계(무엇은 공유하고 무엇은 이중 구현하는가)를 잘못 그린다.
증상 — 독립 구현 경계 판단 시 혼란 — N1까지 읽으면 해소되므로 경미.
권고 — 문구 교정: "`Problem`의 동결 사실·인자로 받은 profile 인스턴스·core `eval`의 값 타입과 공식"으로 N1과 통일.

### [D1·미확인] docs/implementation/stage-05-verification-and-result.md:317-318·312-314 (§4.1 toWallClock)
문제 — 시각 역변환을 "`timeBase.toWallClock`"으로 지정하면서 `ResultAssembler.assemble(problem, profile, pass, stamp)` 인자에 `TimeBase`가 없다 — timeBase가 `Problem`에서 나오는지가 문서 안에 명시돼 있지 않다.
근거 — assemble 시그니처(312-314행)와 317행 인용. `Problem`이 timeBase를 노출하는지는 Stage 2 §2.3(분할 6 담당)에 있어 이 분할에서는 **미확인**.
증상 — Stage 2가 노출하지 않는다면 조립이 컴파일 불가 — 노출한다면 결함 아님(접근 경로 한 줄 누락).
권고 — 분할 6·B에서 Stage 2 §2.3와 대조 후, "problem.timeBase().toWallClock"처럼 출처를 한 줄 명시.

---

## 3. 분할 경계로 미확인 — 통합(B) 이관 목록

이 분할에서 실재·내용을 검증하지 못한 참조 (지침상 타 분할 파일 열람 금지):

- Stage 3 앵커: §2·§3.2~3.5·§4.2~4.4·§9 Q1~Q3·N1·N2·N3·N5·N6·N7·N8·E27·E30·E31·E36·E40 (stage-04·05 합계 20여 회 인용 — `RouteFacts.departureSec`·`endDepotArrivalSec` 필드명 실재 여부 포함)
- Stage 2: §2.3(Problem 조회 메서드 — F9와 직결)·§4 절차 5·T12·E19
- Stage 1: §2.2·§3(`toWallClock`)·§7 / Stage 0: §3.1·§4.2·§4.4·§6(`DoNotIncludeTests`) / Stage Extra: E1·E3
- Architecture: §2·§2.1·§3.2·§3.3 / Plan: §1 편입(2026-08-11)·§2.1 D2·D4·Stage 4 "규모" 문장·Stage 5 순서 개정
- stage-06의 `AlnsResult.bestRouteFacts` 소비 여부 (F6 최종 판정 조건)
- stage-08:297의 `Master §3-⑪` 동일 끊어진 인용 (F3과 같은 결함 — 분할 8 관할)

상위 문서 개정 후보로 관찰된 것 (내 대상 파일의 결함은 아님): Domain §10.2 검사 목록에 호환성(§3.4)이
명시돼 있지 않다 — stage-05 N5가 이미 지적하고 Master §2-③으로 보완 중. Domain 담당 분할(2·3)에서 §10.2 개정 검토 권고.

---

## 4. 초점 판정 — 재검증은 탐색과 독립 구현 가능한가

가능하다 — 진입 시그니처(Stage 4 타입 0)·`RouteReplay`의 근거가 Stage 4 문서가 아니라 Domain §7.1(절차 0~8 완결 서술, 실재 확인)이고, 공유 허용 목록(N1)이 사실/해석을 옳게 갈랐다. 단 독립성의 **서술된 강제 장치가 과장돼 있고(F2)**, 초기해 예외 규정(F1)·부분 facts 관문 부재(F4)·termination 모순(F5)이 탐색↔재검증 경계 위에서 발견됐다.

## 5. 심각도 요약

| 심각도 | 건수 | 대표 |
|---|---|---|
| 치명 (구현이 틀림) | 1 | F1 stage-04 §4.2 — 초기해 "실패할 수 없다" 단언이 profile hard 누락 → 예외·FAILED |
| 중 (재작업 유발) | 5 | F2 "ArchUnit이 예산 차단을 강제" 거짓 인과 · F3 `Master §3-⑪` 부재 · F4 §3 관문 부재 · F5 termination 모순 · F6 bestRouteFacts 소비자 없음 |
| 경 (읽기 불편) | 3 | F7 Q2 상태 불일치 · F8 공유 목록 오귀속 · F9 toWallClock 출처 미명시(미확인) |
