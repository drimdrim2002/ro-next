# RPDPTW 설계 문서 지도

Phase C(O5) 이후 **current design authority** 와 **역사 문서** 를 분리한 진입점이다.  
코드 구현 완료 보고가 아니다.

## 0. Platform target (reference)

| 축 | 규범 | 비고 |
|---|---|---|
| 클라우드 | **AWS** (Google Cloud 경로 유지·확장 아님) | production cutover 승인 ≠ reference 선택 |
| 저장 | **Amazon S3 only** | DB · Redis 없음. 로컬 통합 = **LocalStack S3** |
| durable orchestration | **AWS Step Functions** | 각본은 application/coordinator; Domain 점수 소유 아님 |
| worker / API compute | **Lambda 또는 ECS** | **O1 OPEN** — 한쪽 단정 금지 |
| 현재 tracked 코드 | **RPDPTW multi-module skeleton** (`rpdptw/*`, `build/*`) | Domain/solver not implemented. GCP legacy placeholder **removed** |

상세 배치: [Architecture §3 · §9.4](architecture-design.md).  
구현 inventory: [Master Realization Plan §3](implementation/master-realization-plan.md#3-2026-07-28-current-state-inventory).

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
| [implementation/master-realization-plan.md](implementation/master-realization-plan.md) | Phase DAG·DoD (plan v1.3) |
| [implementation/execution-progress-and-results.md](implementation/execution-progress-and-results.md) | 진행 현황 |

**중요 (A9 current ≠ target · 2026-07-31):**

| 층 | 상태 |
|---|---|
| Core 3 (README / master plan / progress) | **Authority aligned** to APPROVED Master v1.1 · Domain v1.2 · Architecture v3.4 |
| Phase filename slug | **KEEP + 표시 분리** (`implementation/README` §5.1). 예: path `…immutable-problem` ↔ 용어 `immutable solve snapshot` |
| `phases/*` · `reviews/*` 본문 | 여전히 **2026-07-26 frozen 작성 스냅샷**. 의미 rebase **미실시** |
| 구현 acceptance | **0/15**. win_poc **NOT_RUN**. 문서 alignment ≠ 솔버 완료 |

**충돌 시:** current APPROVED 3문서가 `phases/*` frozen 본문보다 이긴다 (D1·D2 포함).  
Phase C는 경로·SUPERSEDED·권위 지도만 고쳤고 완료(O5)다.

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
| [arranged/](arranged/) | 연구 정리 (비규범). `08_gcp_architecture.md` 는 **legacy GCP** 참고 |
| [orgin/](orgin/) | 원본 요약 보존 (비규범; 디렉터리 철자 유지) |
| GCP legacy (`legacy/`, `gcp/`) | **removed** by Phase 00 user decision | not target; not reintroduced |

구 undated `master-design.md` / `domain-design.md` / `architecture-design.md` 는 **deprecated 안**에만 있으며 current로 링크하지 않는다.

## 4. Phase 로드맵 (설계 권위)

| Phase | 목표 | 상태 |
|---|---|---|
| A | 합의·의도 교정 | **완료** |
| B | Master → Domain → Architecture 재작성·검수 | **완료** (3문서 APPROVED) |
| C | 구 문서 SUPERSEDED·깨진 링크·README 정합 (O5) | **완료** (이 지도 반영) |

구현 phase 실행·코드·production cutover는 별 세션이다.
