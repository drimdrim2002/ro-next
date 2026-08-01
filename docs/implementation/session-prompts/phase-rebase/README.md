# Phase 본문 rebase 세션 프롬프트

`phases/*` · `reviews/*` 를 APPROVED 설계에 맞추는 작업을 **Phase 한 개씩** 진행할 때 사용한다.

## 사용법

공통 프롬프트 **하나** + Phase 번호 파라미터.

```text
@docs/implementation/session-prompts/phase-rebase/PROMPT.md

PHASE=00
```

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `PHASE` | 예 | `00` … `14` — 이번 세션에서 다룰 Phase **하나만** |
| `MODE` | 아니오 | 기본 `A` = 진단만. 승인 후 `B`로 수정 |
| `RENAME` | 아니오 | 기본 `keep`. `propose` = rename 제안만 |

### 루프

```text
Step A  진단 (수정 없음)  → 사용자 승인
Step B  변경 초안         → 사용자 확인
Step C  QnA · 수정
Step D  확정              → 다음엔 PHASE=01 등으로 다시 호출
```

### 다음 Phase

같은 세션이든 새 세션이든:

```text
@docs/implementation/session-prompts/phase-rebase/PROMPT.md
PHASE=01
```

## 파일

| 파일 | 역할 |
|---|---|
| [PROMPT.md](PROMPT.md) | **유일한 실행 프롬프트** (권위·루프·Phase 카드 표) |
| [README.md](README.md) | 이 안내 |

## 정책 요약

- Filename: **KEEP + 표시 분리** (`docs/implementation/README.md` §5.1)
- 문서 rebase ≠ 구현 ACCEPTED / win_poc 성공
- task ID 발명 금지

## 권장 순서

```text
00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08
→ 14 (14A 계약) → 09 → 10 → 11 → 12 → 13 → 14 (14B 계약)
```

## 관련 문서

- [../../README.md](../../README.md)
- [../../master-realization-plan.md](../../master-realization-plan.md)
- [../../execution-progress-and-results.md](../../execution-progress-and-results.md)
- [../../../master-design.md](../../../master-design.md)
- [../../../domain-design.md](../../../domain-design.md)
- [../../../architecture-design.md](../../../architecture-design.md)
