# RPDPTW 설계 문서 지도

Phase C(O5) 이후 **current design authority** 와 **역사 문서** 를 분리한 진입점이다.  
코드 구현 완료 보고가 아니다.

## 1. Current design authority (정본)

| 순서 | 문서 | status | 역할 |
|---:|---|---|---|
| 1 | [Master Design](master-design.md) | **APPROVED** | 목표·범위·완료·핵심 결정·e2e·roadmap/gate 개요 |
| 2 | [Domain Design](domain-design.md) | **APPROVED** | 값·수식·정규화·travel·전파·평가·해·검증·결과 **의미** |
| 3 | [Architecture Design](architecture-design.md) | **APPROVED** | Maven module/package, port/SPI, runtime, adapter **배치** |

**규범 입력 (역사, 삭제 금지):**  
[Phase A 인터뷰 정리](deprecated/2026-07-30-design-interview-phase-a.md) (`PHASE_A_COMPLETE`)

**충돌 시 우선순위:** APPROVED Master → APPROVED Domain → APPROVED Architecture → 그 외.

### 권장 읽기 순서

```text
docs/README.md
→ master-design.md
→ domain-design.md
→ architecture-design.md
→ (구현 착수 시) implementation/README.md
→ 필요 시 master-design-sessions / arranged / orgin
```

## 2. Implementation document set

| 경로 | 역할 |
|---|---|
| [implementation/README.md](implementation/README.md) | 15 Phase 구현 문서 세트 진입점 |
| [implementation/master-realization-plan.md](implementation/master-realization-plan.md) | Phase DAG·DoD |
| [implementation/execution-progress-and-results.md](implementation/execution-progress-and-results.md) | 진행 현황 |

**중요 (A9 current ≠ target):**  
이 구현 문서 세트는 **2026-07-26 Final Domain/Architecture 계열**을 frozen input으로 작성했다.  
Phase B APPROVED 3문서와의 **의미 재정렬(rebase)은 아직 하지 않았다.**  
Phase C는 경로·SUPERSEDED 표기·권위 지도만 고친다. implementation 본문을 Phase B에 맞춰 재작성하지 않는다.

역사 frozen inputs (SUPERSEDED, 경로만 유효):

- [2026-07-26 Domain](deprecated/2026-07-26-domain-design.md)
- [2026-07-26 Architecture](deprecated/2026-07-26-architecture-design.md)
- [2026-07-26 Master](deprecated/2026-07-26-master-design.md) (historical cross-check only)
- [구 질문 등록부](deprecated/master-design-open-questions.md)
- [구 integrated implementation design](deprecated/architecture-domain-implementation-design.md)

## 3. History / non-normative

| 경로 | 지위 |
|---|---|
| [deprecated/](deprecated/) | `SUPERSEDED` / `ARCHIVED` archive. **current authority 아님** |
| [master-design-sessions/](master-design-sessions/) | 세션 review input · evidence (비규범) |
| [arranged/](arranged/) | 연구 정리 (비규범) |
| [orgin/](orgin/) | 원본 요약 보존 (비규범; 디렉터리 철자 유지) |

구 undated `master-design.md` / `domain-design.md` / `architecture-design.md` 는 **deprecated 안**에만 있으며 current로 링크하지 않는다.

## 4. Phase 로드맵 (설계 권위)

| Phase | 목표 | 상태 |
|---|---|---|
| A | 합의·의도 교정 | **완료** |
| B | Master → Domain → Architecture 재작성·검수 | **완료** (3문서 APPROVED) |
| C | 구 문서 SUPERSEDED·깨진 링크·README 정합 (O5) | **완료** (이 지도 반영) |

구현 phase 실행·코드·production cutover는 별 세션이다.
