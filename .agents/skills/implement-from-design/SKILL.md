---
name: implement-from-design
description: Stage 문서(docs/implementation/stage-NN-*.md)를 코드로 옮긴다. 파일 표·시그니처와 1:1로 구현하고 표에 없는 타입을 만들지 않는다. Use when implementing a stage, following a stage doc, T-number tests, DoD, "설계대로 구현", "stage 문서대로", or /implement-from-design.
---

# 설계 문서대로 구현

Stage 문서를 코드로 옮길 때만 이 파일을 연다. 버그 수정·리팩터·설명은 `AGENTS.md`면 충분하다. 작업 방식(추측 금지·최소·수술·검증 가능한 목표)은 `AGENTS.md`가 권위다.

## 시작 전

1. 어느 Stage인지 정한다. 인덱스·상태는 `docs/implementation/README.md`, 단계별 DoD는 `docs/implementation-plan.md`. 여기에 스냅샷을 적지 않는다.
2. 해당 `docs/implementation/stage-NN-*.md`를 **구현 전에** 읽는다.
3. 한 Stage에 문서가 둘이면 소유 범위가 갈라져 있다 (예: `stage-04-initial-solution-heuristics.md` vs `stage-04-alns.md`). 해당 쪽만 읽고, 다른 쪽 타입을 끌어오지 않는다.
4. 의미가 갈리면 `AGENTS.md` 권위 순으로 상위 문서를 연다. 문서가 정한 것을 사용자에게 되묻지 않는다. 문서가 정하지 않은 해석이 둘이면 둘 다 말하고 정한 뒤 시작한다.

## 계약

- Stage 문서의 파일 표·시그니처·절차·테스트 표와 **1:1**. 표·코드 블록에 없는 파일·타입·필드·좌표·의존성을 만들지 않는다.
- 바꿔야 하면 **문서를 먼저** 개정하고 그 문서 frontmatter `revisions`에 한 줄. 구현으로 문서를 덮지 않는다.
- 이전 Stage가 정한 클래스명·패키지·시그니처는 그대로 이어받는다. 같은 개념에 새 이름을 붙이지 않는다.
- 일정 번호를 식별자에 넣지 않는다 (`Stage3Fixtures` 금지). 설계 절 좌표는 가리키는 대상이 있으므로 쓴다 (`section72Problem`).

## 진행

단계를 적고 각각에 확인 명령을 붙인다.

```text
1. [작업] → 확인: mvn test -pl solver-core -Dtest=TravelMatrixTest
2. [작업] → 확인: mvn test -pl solver-core -Dtest=T번호클래스
```

- "검증 추가" → 잘못된 입력의 테스트를 먼저 쓰고 통과시킨다.
- "버그 수정" → 재현 테스트를 먼저 쓰고 통과시킨다.
- 판정은 **루트 `mvn verify`**. 단일 테스트는 `-pl`을 붙인다. 명령·함정은 `AGENTS.md`.
- 테스트가 깨진 채로 완료라고 하지 않는다. 실패는 출력과 함께 보고한다.

## 끝나면

Stage 문서의 T번호 표와 `docs/implementation-plan.md`의 해당 Stage DoD가 통과했는지 본다. 문서에 없는 정리·리팩터·인접 파일 손보기는 요청이 있을 때만 한다.
