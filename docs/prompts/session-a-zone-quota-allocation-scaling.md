# 세션 A — H23 기권 제거 (`stage-04-zone-quota-allocation-scaling` 구현)

브랜치 `stage-04-alns`, 커밋 `58ce6fe` 이후에서 시작한다.

## 할 일

`docs/implementation/stage-04-zone-quota-allocation-scaling.md`(설계안, 사용자 결정 Q1~Q6 확정됨)를 구현한다.

읽는 순서: `CLAUDE.md` → 위 설계 문서 전체 → `docs/implementation/stage-04-initial-solution-heuristics.md`의
§3.4·§4.3·§4.4·§5 "공통 — 존 배정 DP" 블록·§7 X20~X23·§8 T38~T44 →
`solver-core/src/main/java/com/ronext/rpdptw/solve/ZoneQuotaAllocation.java` 현행.

## 순서 (각 단계에 확인 명령)

1. **계약 개정 먼저** — 설계 문서 §8 "계약 문서 개정 목록"의 행을 전부 반영한다
   (stage-04-heuristics의 frontmatter `revisions`·§3.4·§4.3·§4.4·§5·§7·§8, `CLAUDE.md`, notes §4.2 ①).
   설계 문서 frontmatter `status`를 "계약 반영됨 — 구현 중"으로 바꾼다.
   확인: `git diff --stat`에 위 문서들만 잡힌다.
2. **구현** — 설계 문서 §2~§4 순서대로: ① 프론티어 범위 제한(호환 유형·maxNeed) → ②(a) 성분별 DP ·
   ②(b) 풍부한 차종 제외 → ③ 희소 상태 저장(`Layer` 배열·`Demand.cached`의 `byte[Π]` 삭제) → ④ 총량 폭 제한
   `MAX_TOTAL_STATES = 262_144`. `value(demand, types, s)` 추출, `Allocation.truncated` 추가, H23·H24의
   `abstains`는 `false`. 축소는 항상 적용(Q2). 설계 문서 §3 시그니처 밖의 타입·필드는 만들지 않는다.
   확인: `mvn test -pl solver-core -Dtest='ZoneQuotaAllocation*,ZoneQuota*Fill*'`
3. **테스트** — §7대로 T39 삭제, T45~T51 신설(solver-core), T52 = app `WinPocFixtureTest` 확장
   (score 전체 `[0, 31, 4,198,408, 1,002,069]` 일치). T51은 규모 테스트라 `CLAUDE.md` 규약대로
   `-Dtest='!...'` 제외 목록에 이름을 추가한다.
   확인: `mvn test -pl solver-core -Dtest=ZoneQuotaAllocationTest` · `mvn test -pl app -Dtest=WinPocFixtureTest`
4. **판정**: 루트 `mvn verify` 통과. 실물 fixture에서 H23 결과가 개정 전과 완전히 같아야 한다(T52).
   T25(`InitialSolutionScaleTest`) 합성에서도 같은 답.
5. **커밋** — 한국어 메시지, 계약 개정과 구현을 한 커밋에. 설계 문서 `status`를 "구현 완료(커밋 해시)"로.

## 규칙

- 설계 문서·계약에 없는 선택은 하지 않는다. 막히면 문서를 먼저 찾고, 문서가 정하지 않은 것만 묻는다.
- H23·H24 2단계(존 내부 적재)·leftover pass·1-1 교환은 손대지 않는다. `ConstructionOutcome`·
  `InitialSolutionBuilder` 무변경(Q4). H25(`zone-quota-exchange`)는 이 세션 범위 밖 — 다음 세션이 한다.
- 테스트가 깨진 채로 완료라고 하지 않는다. 실패는 출력과 함께 보고한다.

## 끝날 때 보고할 것

- 커밋 해시 · 바뀐 파일 목록
- T45~T52 결과
- 실물 fixture 전후 score 대조
- T51의 상태 수·소요·근사(`truncated`) 여부
- 설계 문서와 달리 구현한 것(있다면 이유와 함께)
