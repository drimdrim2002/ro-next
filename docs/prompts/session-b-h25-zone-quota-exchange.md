# 세션 B — H25 greedy + 교환 (`stage-04-h25-zone-quota-exchange` 구현 + 비교 실측)

브랜치 `stage-04-alns`, **세션 A의 커밋**(H23 기권 제거 — `stage-04-zone-quota-allocation-scaling` 구현) 이후에서 시작한다.
`git log --oneline -3`으로 그 커밋이 있는지 먼저 확인하고, 없으면 멈추고 알린다.

## 할 일

`docs/implementation/stage-04-h25-zone-quota-exchange.md`(설계안, 사용자 결정 Q1~Q5 확정됨)를 구현하고
§8 비교 프로토콜을 실측한다.

읽는 순서: `CLAUDE.md` → 위 설계 문서 전체 → `docs/implementation/stage-04-zone-quota-allocation-scaling.md` §3
(공유하는 `value`·`Allocation.truncated`) → `docs/implementation/stage-04-initial-solution-heuristics.md`
§3.2 SPI·§4.1~§4.4·§5 H3/H23 의사코드·§7·§8 →
`solver-core/src/main/java/com/ronext/rpdptw/solve/`의 `ZoneQuotaAllocation.java`·
`ZoneQuotaBalancedFillConstruction.java`·`VehicleZoneFillConstruction.java`·`InitialSolutionBuilder.java` 현행.

## 순서 (각 단계에 확인 명령)

1. **계약 개정 먼저** — 설계 문서 §9 개정 목록 전부(stage-04-heuristics·survey §2.5/§5·stage-04-alns·
   domain-design §9.3·implementation-plan·`CLAUDE.md`·notes 2편·README의 "24개 → 25개" 포함).
   설계 문서 `status` 갱신.
   확인: `grep -rn "24개" docs CLAUDE.md` 결과가 개정 목록이 남기기로 한 곳뿐이다.
2. **추출** — `ZoneQuotaBalancedFillConstruction.fill(problem, profile, allocation)` 동작 무변경 추출
   (`value()`는 세션 A가 이미 뽑았다 — 그대로 쓴다).
   확인: `mvn test -pl solver-core -Dtest='ZoneQuota*'`와 `mvn test -pl app -Dtest=WinPocFixtureTest` 그대로 통과.
3. **구현** — `ZoneQuotaExchangeFillConstruction` 신규 1파일: §4.1 준비 → §4.2 greedy 덮개(라운드 로빈·trim) →
   §4.3 교환 best-improvement(MOVE·SWAP, 동률은 순회 순서, `MAX_SCANS = 50`) → §4.4 `fill` 호출.
   `Demand.cached`·프론티어 나열은 부르지 않는다(X30). `allocate(problem, valueTrace, ScanRule)` 오버로드는
   비교 전용. `InitialSolutionBuilder.defaults()` 끝에 25번째로 추가.
   확인: `mvn test -pl solver-core -Dtest=ZoneQuotaExchangeFillConstructionTest`
4. **테스트** — §7대로 T53~T55(solver-core), T56 = app `WinPocFixtureTest`에 H3·H25·H23 나란히 출력
   (`H25 ≤ H3` 단언은 아직 넣지 않는다 — Q4).
5. **판정**: 루트 `mvn verify` 통과. 기존 H23·H24 결과 무변경(T52).
6. **실측** — §8 프로토콜 재료 ①~④를 전부 돌린다:
   ① 실물 fixture ② `zoneId` 제거 변형 ③ T25 합성 변형(차량 전부 다른 유형) ④ `ScanRule` BEST vs FIRST.
   기법마다 (미배정, 차량, 거리, 운행시간, 소요)와 배정 값 V·스캔 수를 표로.
   결과를 설계 문서 §8에 "실측 결과" 절로 채우고, §8 결정 표의 어느 행에 해당하는지 적는다.
   ④에서 품질이 같으면 FIRST로 바꾸고 BEST와 오버로드를 지운다(Q2) — 바꿨으면 §4.3도 고친다.
7. **커밋** — 계약 개정·구현·실측 결과를 한 커밋에(실측으로 코드가 바뀌었으면 그것까지). 한국어 메시지.

## 규칙

- 설계 문서·계약에 없는 선택은 하지 않는다. **H3는 수정하지 않는다**(비교 대상). H23의 DP·2단계 로직은 수정하지
  않는다 — 추출만.
- §8 결정 표의 결론(H25 잔류·폐기·기권 대체)은 **사용자가 정한다** — 실측 결과와 해당 행을 보고하고 멈춘다.
  "H25 ≤ H3"이 나와도 이 세션에서 코드를 지우지 않는다.
- 테스트가 깨진 채로 완료라고 하지 않는다.

## 끝날 때 보고할 것

- 커밋 해시 · 바뀐 파일 목록
- T53~T56 결과
- §8 실측 표(①~④)
- 해당하는 결정 표 행과 그 근거
- BEST/FIRST 결정
- 설계 문서와 달리 구현한 것(있다면 이유와 함께)
