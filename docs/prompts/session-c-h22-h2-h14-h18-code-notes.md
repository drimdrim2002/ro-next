# 세션 C — H22·H2·H14·H18 코드 해설 노트 4편 (subagent 4개 병렬)

브랜치 `stage-04-alns`. 이 세션은 오케스트레이터다 — 문서를 직접 쓰지 않고 **subagent 4개를 병렬로** 띄워
각각 한 편씩 쓰게 하고, 결과를 검수해 보고한다. 코드는 한 줄도 고치지 않는다(읽기만).

## 왜 쓰나

초기해 병렬 포트폴리오로 남기기로 한 5개 중 H23은 이미 코드 해설 노트가 있다
(`docs/notes/zone-quota-balanced-fill-construction.md` · `docs/notes/zone-quota-allocation-dp.md`).
나머지 4개(H22·H2·H14·H18)에는 없다. 같은 수준의 해설을 4편 만든다.

선정 근거와 쉬운 말 설명의 **출발점**은 `docs/notes/initial-solution-heuristics-h1-h24.md`의
**§4.2 축과 대표 · §6 포트폴리오 4개 예시 설명**(§6.0 비용의 정의 · §6.1 H22 · §6.2 H2 · §6.3 H14 ·
§6.4 H18 · §6.5 힐베르트 순서)이다. 새 문서는 **§6을 코드 수준으로 확장**하는 것이지 §6을 대체하지 않는다.

## 산출물

| subagent | 문서 | 대상 코드 |
|---|---|---|
| 1 | `docs/notes/squeaky-wheel-sequential-construction.md` | `SqueakyWheelSequentialConstruction.java`(94줄) + 공유하는 `DeadlineSequentialConstruction.construct` |
| 2 | `docs/notes/urgency-regret3-construction.md` | `UrgencyRegret3Construction.java`(105줄) |
| 3 | `docs/notes/vehicle-fill-remaining-regret-construction.md` | `VehicleFillRemainingRegretConstruction.java`(129줄) |
| 4 | `docs/notes/hilbert-split-construction.md` | `HilbertSplitConstruction.java`(92줄) + `GiantTourSplit.java`(113줄) |

전부 `solver-core/src/main/java/com/ronext/rpdptw/solve/` 아래. 네 편 모두 `InsertionSearch.java`(480줄)의
`candidates`·`apply`·`Candidate.byCost`를 공유하므로 각 문서가 **자기 기법이 쓰는 만큼만** 설명한다.

## 각 subagent에게 줄 지시 (공통 — 문서마다 그대로 전달)

### 형식

`docs/notes/zone-quota-balanced-fill-construction.md`를 **형식의 본보기로 삼는다**(내용을 베끼는 게 아니라
구성·머리말·각주 방식을 맞춘다). 절 구성:

- 머리말 인용 블록 — 성격(코드 해설 노트, 비규범) · 규범 문서 링크 · 대상 독자 · 대상 파일(줄 수·기준일)
- §0 한 줄 요약 + 이름 뜯어보기 표
- §1 프로젝트 안에서의 위치 — 포트폴리오의 한 칸 · `ConstructionHeuristic` SPI · 다루는 자료형
- §2 기대는 부품 — `InsertionSearch`(그 기법이 쓰는 메서드만) 등
- §3 알고리즘 전체 흐름 — 의사코드 또는 단계 목록
- §4 단계별 상세 — **실제 코드 인용 + 줄 번호**, 각 줄이 무엇을 계산하는지
- §5 결정성 · 종료 · 복잡도 — 규범 문서 §5 복잡도 표의 해당 행을 인용하고 풀이
- §6 코드를 읽으며 눈여겨볼 점
- §7 경계 상황 — 규범 §7의 X번호 중 이 기법 것
- §8 테스트와 실측 — 이 기법의 T번호와 §1 표의 실측값
- 용어 각주

### 내용 규칙 (어기면 다시 쓴다)

1. **배경 지식 0을 가정한다.** CVRPTW·regret·seed·bank·profile·오라클·사전식 같은 말은 처음 나올 때 각주로 푼다.
2. **모호한 비유 금지.** "삐걱대는 바퀴처럼", "마치 …인 것처럼" 같은 표현으로 설명을 끝내지 않는다.
   이름의 유래를 한 줄 적는 것은 되지만, **설명의 본체는 수치와 절차**여야 한다.
3. **수치로 말한다.** "빠르다/느리다" 대신 "77 ms / 519 ms", "많다" 대신 "452건 중 47건". 예시를 들 때는
   주문 5~8건·차 2~3대 규모의 **구체적인 숫자 표**를 만들어 단계마다 값이 어떻게 변하는지 보인다
   (`initial-solution-heuristics-h1-h24.md` §6의 예시 표가 그 수준의 최소선이다 — 문서는 그보다 더 상세해야 한다).
4. **코드에서 어떻게 구했는지가 본체다.** 각 계산이 **어느 파일 어느 줄**에서 나오는지 밝히고, 비교자·정렬 키·
   동률 규칙·상수를 코드 그대로 인용한다. 상수는 값과 함께 "재량 상수이고 Stage 8이 조정한다"를 밝힌다.
5. **추측과 사실을 구분한다.** 코드에서 확인한 것은 단정하고, 확인 못 한 것은 "전제:" 또는 "가설:"로 표시한다.
   예: H18의 실물 165건 원인은 §6.4의 **가설**이지 측정된 사실이 아니다.
6. **규범과 충돌하면 규범이 이긴다** — `docs/implementation/stage-04-initial-solution-heuristics.md` §5의
   해당 항목(H2 514행 · H14 768행 · H18 850행 · H22 925행 부근)·§4.1~§4.3·§5 복잡도 표·§7 X·§8 T를 먼저 읽는다.
7. 한국어. 기존 용어 표기(`Request`·pair·`Problem`·`Solution`·bank·profile·기권·미배정)를 그대로 쓴다.
   같은 개념에 새 이름을 붙이지 않는다.
8. **코드는 고치지 않는다.** 읽다가 결함으로 보이는 것을 찾으면 문서에 적지 말고 **보고에만** 적는다.

### 반드시 확인해 넣을 것 (기법별)

- **H22** — `ROUNDS = 5`가 코드 상수라는 것 · 1라운드가 H4와 같은 실행임을 코드로 보이기
  (`DeadlineSequentialConstruction.construct(problem, profile, order, Candidate.byCost(), ID)` 공유) ·
  `blame`의 +2/+1 규칙과 중앙값 계산 · 불변점 조기 종료(X18) · 라운드마다 `Evaluator.evaluate`로 정식 평가해
  best를 고른다는 것 · T37 · 실측 47건/399 ms. 한 라운드의 비용을 방문 수로 계산해 보일 것
  (차 한 대가 L개 방문 중이면 자리 L+1곳 × 자리마다 전파 O(L) → 그 차에 O(L²)).
- **H2** — 시간창 폭 4분위 등급을 실제로 어떻게 나누는지(코드의 분위 계산) · regret3가
  `cands[1].deltaDriveDistMeter - cands[0]…` + `cands[2] - cands[0]`의 합이라는 것 · 선택 키
  `(등급 ASC, 후보 수 ASC, regret3 DESC, RequestId ASC)` · 후보 수를 3에서 자르는 이유(코드 주석) ·
  H1과의 차이 · 실측 60건/644 ms.
- **H14** — 차량 순서(정차 한도·적재량 DESC) · "남은 차량 대비 regret"의 식과 `noAlt` 최우선(X19) ·
  차를 닫는 조건 · T31 · 실측 56건/519 ms. ②(뒤차가 혼자 싣는 최선 비용)를 어떻게 계산하는지 코드로.
- **H18** — `hilbertOrder`의 4단계(대표 좌표 → 경계 상자 정규화 → `hilbertIndex` → 정렬)와 `ORDER`(16차) ·
  `hilbertIndex`의 루프가 사분면을 내려가며 회전시키는 부분을 **비유 없이** 좌표 변환으로 설명 ·
  `GiantTourSplit`의 DP(상태·전이·역추적)와 조각 안 순서가 순열 그대로라는 것 · `appendPair`로 pair가 붙는 것 ·
  조각→차량이 적재량 DESC라는 것 · T34("같은 순열에서 Split ≤ next-fit") · 실측 165건/175 ms ·
  실물에서 약한 이유는 **가설**임을 명시.

### 끝날 때 보고할 것

문서 경로 · 절 구성 · 코드에서 새로 확인한 사실 중 §6 설명과 **다른 것**(있으면 반드시) ·
결함으로 보이는 것(문서에는 안 적고 여기에만) · 각주로 푼 용어 목록.

## 오케스트레이터가 할 일

1. 위 "공통 지시 + 기법별 항목"을 담아 subagent 4개를 **한 번에 병렬로** 띄운다(서로 다른 파일을 쓰므로 충돌 없음).
2. 넷이 다 끝나면 각 문서를 직접 읽어 확인한다:
   - 모호한 비유가 남아 있지 않은가 · 수치 예시 표가 있는가 · 줄 번호 인용이 실제 코드와 맞는가
   - 네 문서가 `InsertionSearch`의 같은 부분을 설명할 때 용어가 서로 어긋나지 않는가
   - §1 표의 실측값(H22 47/399 ms · H2 60/644 ms · H14 56/519 ms · H18 165/175 ms)과 일치하는가
3. `docs/notes/initial-solution-heuristics-h1-h24.md` §6 각 절 끝에 새 문서 링크를 한 줄씩 단다
   (§6.1 → squeaky-wheel …, §6.2 → urgency-regret3 …, §6.3 → vehicle-fill-remaining-regret …,
   §6.4·§6.5 → hilbert-split …). frontmatter `revisions`에 한 줄.
4. 커밋 — `docs(notes): H22·H2·H14·H18 코드 해설 노트 4편` 형식, 한국어.
5. 나에게 보고: 문서 4개 경로 · 각 문서에서 코드로 확인한 새 사실 · subagent가 보고한 결함 후보.

## 주의

- 세션 A(`stage-04-zone-quota-allocation-scaling` 구현)가 같은 작업 트리에서 돌고 있을 수 있다.
  시작할 때 `git status --short`로 확인하고, 미커밋 변경이 있으면 **그 파일들은 건드리지 않는다**
  (이 작업이 만지는 것은 `docs/notes/`의 새 파일 4개 + `initial-solution-heuristics-h1-h24.md`뿐이다).
  `initial-solution-heuristics-h1-h24.md`가 이미 수정 상태면 링크 추가만 하고 다른 줄은 손대지 않는다.
- subagent에게 코드 수정 권한이 필요 없다 — 문서 쓰기와 코드 읽기뿐이다.
