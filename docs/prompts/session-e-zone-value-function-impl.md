# 세션 E — 존 배정 DP 값 함수 개정 (`stage-04-zone-value-function` 구현 + 비교 실측 + T52 갱신)

브랜치 `stage-05-verify`, **설계안 `docs/implementation/stage-04-zone-value-function.md`가 커밋된 뒤**에서 시작한다.
`git log --oneline -5`에 그 커밋이 있는지, `git status --short`가 `?? docs/prompts/`뿐인지 먼저 확인하고,
아니면 멈추고 알린다.

## 사용자 결정 (설계안 §9)

아래가 이 세션의 입력이다. 기본값은 설계안 §9의 추천이다 — **사용자가 다르게 정했으면 이 표와 설계안 §9를 먼저
고친 뒤 시작한다.** 표와 설계안 §9가 서로 다르면 시작하지 않는다.

| # | 질문 | 결정 |
|---|---|---|
| Q1 | 축 순서 | **(a) `(Σ부족, Σ대수, Σ낭비, Σ결손)`** |
| Q2 | 결손 형태 | **(a) 선형 `max(0, 대수 × 평균단품 − 낭비)`** |
| Q3 | 평균 vs 중앙값 | **(a) 평균 `⌊Σvolume_z / n_z⌋`** — `Demand.count` 하나만 더한다 |
| Q4 | T52 기대값 | **(a) 조건부 갱신** — §5.4의 네 조건이 전부 참일 때만 |
| Q5 | 1단계 읽기 경계 | **(a) `Demand`·유형 용량·`s`만.** 계약 §5 공통 블록에 한 줄로 명시 |
| Q6 | 낭비·결손·부족의 단위 | **(a) 부피 유지 + 전제 명시** |
| Q7 | 잎 예산·절단 후속 순서 | **(a) 이 세션 직후.** 이 세션은 건드리지 않는다 |
| Q8 | H24 결과 변화 | **(a) 기록만** |

시작할 때 설계안 frontmatter `revisions`에 "사용자 결정 반영 — §9 Q1~Q8 확정(…)" 한 줄을 더하고 §9 표에 결정 열을 채운다
(scaling 설계안 §9가 한 방식 그대로).

## 왜 (한 문단)

존 배정 DP의 값 `(Σ부족, Σ낭비, Σ대수)`는 실물 fixture에서 전 존을 덮고 31대를 다 쓰는 배정끼리 **상수**라 209개가
동률이고, DP는 열거 순서로 하나를 고른다. 그중 36개(17%)는 2단계에서 미배정 1~2건을 남긴다. 설계안은 축 순서를
정식 score와 맞추고(`대수`가 `낭비` 앞) 4번째 축 **결손** `Σ_z max(0, 대수_z × 평균단품_z − 낭비_z)`을 더해 동점을
"여유가 존 크기에 맞게 퍼진 배정"으로 깨게 한다. 실물 예측 score는 `[0, 31, 4,194,052, 1,004,144]`(종전 4,198,408)이고,
여유 8.7% 변형에서는 33대 → 30대다. **잎 예산 26만 증상은 이 개정으로 안 사라진다** — 후보 자체가 없는 별개 결함이고
(설계안 §1.4·X39) 다음 세션 몫이다.

## 읽는 순서

1. `CLAUDE.md`
2. `docs/implementation/stage-04-zone-value-function.md` **전체** — 특히 §2.6 채택 권고 · §3 시그니처 · §4 의사코드 ·
   §5.3 비교 프로토콜 · §5.4 T52 조건 · §6 계약 개정 목록 · §7 T59~T61·X35~X39
3. `docs/implementation/stage-04-initial-solution-heuristics.md` §3.4 · §5 "공통 — 존 배정 DP" 블록 · §7 X20~X29 · §8 T38~T58
4. `docs/implementation/stage-04-zone-quota-allocation-scaling.md` §2.1 ① · §3 (`value`의 현 정의 — 이 세션이 대체한다)
5. 코드: `solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaAllocation.java`
   (`value` · `Demand.of` · `solveComponent`의 `candidate` 합산 · `Layer`의 `values` stride) ·
   `app/src/test/java/com/ronext/rpdptw/app/WinPocFixtureTest.java` (T44/T52) ·
   `app/src/test/java/com/ronext/rpdptw/solve/ZoneQuotaAllocationAccess.java` (app에서 package-private 접근하는 수법 —
   실측 probe도 같은 자리에 둔다)

## 순서 (각 단계에 확인 명령)

1. **계약 개정 먼저** — 설계안 §6 목록의 행을 **전부** 반영한다: stage-04-heuristics(frontmatter `revisions`·§3.4·§5 공통
   블록·§5 H23 실측 불릿·§7 X35~X39·§8 T44/T52/T59~T61/재확인 행), scaling 설계안 frontmatter, survey §2.5,
   stage-04-alns §8 실측 단락, `CLAUDE.md`(존 배정 DP 문장 + **테스트 수 문면** solver-core 125 → 128),
   notes 3편(`zone-quota-allocation-dp.md`는 §0 그림·§4 정의·§7 예제 표·§8 동률 표를 4축으로 다시 계산),
   `docs/notes/heuristics/*.md`의 H23 실측 행 4곳, `docs/implementation/README.md` 인덱스 행.
   score를 적는 곳은 일단 **예측치**로 쓰고 "(예측 — 6단계에서 확정)"을 붙인다. 설계안 `status`를
   "계약 반영됨 — 구현 중"으로.
   확인: `git diff --stat`에 문서만 잡힌다 · `grep -rn "4,198,408" docs CLAUDE.md`가 "개정 전" 병기 문맥에만 남는다.
2. **구현** — 설계안 §3·§4 그대로, 그 밖은 손대지 않는다:
   - `value(demand, types, s)` → 길이 4, 순서 `(부족, 대수, 낭비, 결손)`. 부족·낭비 정의 무변경.
     `결손 = 대수 == 0 ? 0 : max(0, 대수 × ⌊totalVolume / count⌋ − 낭비)`, 곱은 `Math.multiplyExact`.
   - `VALUE_AXES = 4` 상수. `Layer.values`(`size * 3`)·`value(index)`·`Entry`·`candidate` 합산 세 항 인라인 → 축 수 루프.
     `compare`·`KEY_ORDER`·절단 정렬·역추적·cursor 소비는 무변경.
   - `Demand`에 `final int count`(존 요청 수). `Demand.of`에서 채운다.
   - **만들지 않는 것**: 예산 주입 오버로드 · `Problem`/`TravelMatrix`를 읽는 항 · 다른 이름의 상수. `frontier`·`maxNeed`·
     `MAX_TOTAL_STATES`·`MAX_FRONTIER_LEAVES`·H23/H24 2단계·leftover pass·1-1 교환은 **한 줄도** 바꾸지 않는다.
   확인: `mvn test -pl solver-core -Dtest='ZoneQuota*' -Dsurefire.failIfNoSpecifiedTests=false`
   (T46 "손 계산 최적"이 깨지면 새 축 순서로 손 계산을 다시 해 기대값을 고친다 — 설계안 §7 재확인 행.
   그 외 T38·T45·T47~T50·T57이 깨지면 구현 결함이다).
3. **테스트** — 설계안 §7대로 `ZoneQuotaAllocationTest`에 T59(대수가 낭비보다 앞) · T60(결손이 동률을 깬다 —
   종전 동률 규칙이 **반대편**을 고르는 입력이어야 한다. 실제 `KEY_ORDER` 순회로 그렇게 되는지 확인하고, 안 되면
   존 이름·유형 용량을 바꿔 맞춘다) · T61(빈 집합·비덮개·부피 0 존의 축 값).
   확인: `mvn test -pl solver-core -Dtest=ZoneQuotaAllocationTest`
4. **실물 1차 확인** — `mvn test -pl app -Dtest=WinPocFixtureTest`. **T52는 이 시점에 깨져야 정상**이다
   (기대값이 아직 종전 값). 실제 score를 기록한다. 예측치 `[0, 31, 4,194,052, 1,004,144]`와 다르면 **여기서 멈추고**
   원인부터 찾는다 — 기대값을 실제 값으로 바꾸는 것은 원인을 안 뒤의 일이다(설계안 §5.4 조건 2).
   진단용 예측 배정(설계 세션 probe): ZONE_29 `[0,0,0,0,2,2]` · ZONE_17 `[1,0,0,3,0,0]` · ZONE_16 `[0,1,1,0,1,0]` ·
   ZONE_24 `[0,0,1,1,0,0]` · ZONE_19 `[3,1,1,0,0,0]` · ZONE_18 `[0,0,1,2,0,0]` (유형 순서 T0 4,900 … T5 17,640).
   DP 값은 `[0, 31, 12,290, 10,262]`, 동률 5개, `truncated == false`.
5. **비교 실측** — 설계안 §5.3 재료 ①~⑤를 **개정 전/후 값 함수로** 각각 돌린다. 개정 전은 `git stash`가 아니라
   probe 안에서 옛 `value`를 재현해 같은 실행에서 비교한다(세션 D가 한 방식 — app 테스트 소스에
   `com.ronext.rpdptw.solve` 패키지 probe 클래스를 두면 package-private API에 닿는다). 재료 만드는 법:
   - ② `zoneId` 제거: canonical `RequestSide.zoneId`를 전부 비운다 (H25 §8 재료 ②와 같게)
   - ③ 차종 31종: 차량 31대 용량을 1 단위씩 깎는다 (T58과 같게)
   - ④ 변형 A~E: 규약 JSON을 `toInput` 전에 고친다 — A `vehicleId == "V024"` 제거 · B/E `orders[].items[].volume`
     ×0.9 / ×1.02 · C `items[].weight` ×1.8 · D `vehicles[0]`(V001) 복사본 `V101`·`V102` 추가
   - ⑤ 동률 집합: 현 값의 동률 경로를 전부 나열(부모·s 목록을 기록하는 DP)해 각각 `fill` → 미배정 분포,
     개정 값의 최소 집합이 그 안에서 전부 미배정 0인가. 실물 209개 × fill ≈ 2분
   기록 열은 §5.3대로(입력 · H23/H24 · 값 함수 전/후 · DP 값 · 동률 수 · `truncated` · 미배정 · 차량 · 거리 · 운행시간 · DP 소요).
   결과를 설계안 §5.1 아래 "구현 후 실측" 절로 채우고 §5.3 결정 표의 **어느 행인지** 적는다.
   probe는 **끝나면 삭제**하고(`app/target/test-classes`의 컴파일 산출물까지 — 남으면 surefire가 돌린다)
   `git status --short`에 probe가 없음을 확인한다.
6. **T52 갱신 판정** — 설계안 §5.4 (a)의 네 조건을 **하나씩** 판정해 적는다:
   (1) 새 score ≤ 종전 (`Scores.compare`) (2) 예측치와 정확히 일치 (3) T44 나머지 단언 전부 통과·`truncated == false`
   (4) §5.1 표 1이 재현됨(변형 A~E 전 행에서 개정 후 ≤ 개정 전).
   **넷 다 참**이면 `WinPocFixtureTest`의 기대값과 1단계에서 "(예측)"으로 적어 둔 문서 수치를 확정한다.
   하나라도 거짓이면 **기대값을 바꾸지 말고** 멈춰서 보고한다 — 설계 기각·재검토는 사용자가 정한다(§5.3 결정 표).
7. **판정**: 루트 `mvn verify` 통과 (solver-core 128 / solver-profile 1 / app 4).
8. **커밋** — 계약 개정·구현·테스트·실측 결과를 한 커밋에, 한국어 메시지. 설계안 `status`를 "구현 완료(해시)"로.

## 규칙

- 설계안·계약에 없는 선택은 하지 않는다. 막히면 문서를 먼저 찾고, 문서가 정하지 않은 것만 묻는다.
  해석이 둘이면 둘 다 적고 멈춘다.
- **범위 밖(설계안 §8)을 건드리지 않는다**: 잎 예산 값·절단 규칙·`truncated` 노출(③은 다음 세션), 1-1 교환 제약, 되먹임,
  부족의 단위, 낭비 축 삭제. 실측 표 2(26만)가 나빠도 이 세션은 고치지 않는다 — 관찰만 §5.1에 남긴다.
- **상수를 넣지 않는다.** 결손의 문턱은 `대수 × 평균단품`이고 배율이 없다. 실측이 더 좋게 나오는 배율을 찾았더라도 넣지
  않는다(설계안 §5.2-1) — 보고에만 적는다.
- 예측치와 다른 score를 "실제 값"으로 그냥 굳히지 않는다(4단계·6단계). 다른 이유를 찾아 설계안에 적는 것이 먼저다.
- 테스트가 깨진 채로 완료라고 하지 않는다. 실패는 출력과 함께 보고한다.
- 번호는 설계안 §7의 T59~T61·X35~X39를 그대로 쓴다. 폐기된 H25 문서는 손대지 않는다.
- 문서·커밋 메시지는 한국어, 용어는 기존 표기(부족/낭비/대수/결손/프론티어/덮개/재검증).

## 끝날 때 보고할 것

- 커밋 해시 · 바뀐 파일 목록
- T59~T61 결과 · T46 손 계산을 고쳤다면 전후 값
- 실물 score 개정 전/후와 DP 값·동률 수·DP 소요 전/후
- §5.3 재료 ①~⑤ 표 — 개정 전/후 나란히. ⑤는 동률 수와 미배정 분포, 개정 값 최소 집합의 성공률
- T52 갱신 여부와 §5.4 조건 (1)~(4) 각각의 판정
- H24의 실물 결과 변화 (Q8 — 기록)
- 설계안과 달리 구현한 것 (있다면 이유와 함께)
- 다음 세션(잎 예산·절단 규칙)에 넘길 관찰 — 26만 표 2 수치, 변형 D의 현행 예산 절단 여부
