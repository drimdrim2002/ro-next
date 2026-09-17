---
name: java-clean-code
description: Java를 이 저장소 관습과 클린코드 원칙으로 수정한다. Use when editing .java files, adding or changing tests, refactoring Java in solver-core, solver-profile, or app, writing records or classes, or /java-clean-code.
---

# Java 클린코드

`.java`를 만들거나 고칠 때 이 파일을 연다. 작업 방식(최소·수술·주변 스타일)은 `AGENTS.md`가 권위다.

**먼저** 같은 디렉터리의 `references/repo-java.md`를 읽는다. 이 저장소 관습이 일반 원칙보다 앞선다.

## 원칙

책을 옮기지 않는다. 아래만 적용한다.

- **이름.** 하는 일을 드러낸다. 설계 문서 용어(`Request`, `Problem`, `bank`, `Profile`)를 바꾸지 않는다. 같은 개념에 새 이름을 붙이지 않는다.
- **작게.** 함수는 한 가지. 클래스는 한 가지 이유. 200줄이 50줄이면 다시 쓴다. 사용처가 하나인데 인터페이스·전략·팩토리를 두지 않는다.
- **부수효과.** 명령과 조회를 섞지 않는다. `Problem`·이동표를 탐색이 고치지 않는다. 불변 값이면 새 값을 돌려준다.
- **주석.** 왜(설계 절, 함정)만. 무엇을 하는지는 이름이 말한다. 구현 과정을 남기지 않는다. 주석으로 문제를 가리지 않는다.
- **오류.** 불변식이 막는 상태를 방어 코드로 다루지 않는다. 일어날 수 없는 분기를 만들지 않는다. 입력 거부는 기존 `InputException.Kind`를 쓴다.
- **테스트가 문서다.** 깨진 테스트를 고치지 않은 채 완료하지 않는다. 테스트 이름은 행동을 말한다 (`detectsXorViolations`). 프로덕션과 같은 패키지에 두어 패키지-private을 검사한다.
- **맞춘다.** 인접 파일의 들여쓰기·import 순서·주석 밀도·record vs class를 따른다. 취향으로 포맷·타입힌트·주석을 덧씌우지 않는다.

## 하지 않는 것

요청에 없는 계층, 설정, 일반화, "나중에 쓸" 확장점. Uncle Bob·Effective Java 목차 전체를 이 레포에 다시 적용하지 않는다.
