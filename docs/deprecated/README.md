# deprecated — 설계 문서 archive

이 폴더는 **current design authority가 아니다.**  
정본 진입점은 [../README.md](../README.md) 와 APPROVED 3문서다.

| 문서 | 역할 |
|---|---|
| [../master-design.md](../master-design.md) | Master **APPROVED** |
| [../domain-design.md](../domain-design.md) | Domain **APPROVED** |
| [../architecture-design.md](../architecture-design.md) | Architecture **APPROVED** |

## 이 폴더에 있는 것

| 범주 | 예시 | status |
|---|---|---|
| Phase A 규범 입력 | [2026-07-30-design-interview-phase-a.md](2026-07-30-design-interview-phase-a.md) | `PHASE_A_COMPLETE` (역사 규범 입력; 삭제 금지) |
| 2026-07-26 Final 시리즈 | `2026-07-26-*-design.md` | `SUPERSEDED` → 정본 3문서 |
| 구 undated 설계 | `master-design.md`, `domain-design.md`, `architecture-design.md` 등 | `SUPERSEDED` |
| 구 질문 등록부 | [master-design-open-questions.md](master-design-open-questions.md) | `SUPERSEDED` (단일 대체 등록부 없음; OPEN은 Master §4.4 등) |
| 구 구현 통합 설계 | [architecture-domain-implementation-design.md](architecture-domain-implementation-design.md) | `SUPERSEDED` → `../implementation/` |
| Phase B 핸드오프·프롬프트 | `2026-07-31-architecture-*.md` | `ARCHIVED` |
| 구 implementation phases | [phases/](phases/) | archive |

## 규칙

1. 본문을 현재 결정의 authority로 인용하지 않는다.
2. 삭제하지 않는다 (이력·implementation frozen fingerprint 경로 유지).
3. 링크 수정이 필요하면 상대경로만 고치고 의미를 재승인하지 않는다.
4. Phase C는 path remap + status banner 범위다. implementation 세트의 Phase B rebase는 별 작업이다.

## 세션·연구 자료 (이 폴더 밖)

- [../master-design-sessions/](../master-design-sessions/) — review input
- [../arranged/](../arranged/) · [../orgin/](../orgin/) — 연구·원본 (비규범)
