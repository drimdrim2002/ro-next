# 세션 31 — Domain Design 열린 질문 통합

```yaml
status: COMPLETE
version: 1.0-integration
last_updated: 2026-07-24
owner: RPDPTW Domain Design 통합
scope: 세션 29 사용자 결정에 맞춘 Domain Design 재구성과 Master·등록부 정합성 검증
supersedes: Domain Design v1.1의 legacy 상세 가정
related_decisions:
  - 29-open-question-interview.md
  - 30-open-question-integration.md
```

## 1. 목적과 권위

이 세션은 [세션 29 열린 질문 인터뷰](29-open-question-interview.md)의 사용자 답변만을 질문 결정의 권위 있는 근거로 사용하여 [Domain Design](../domain-design.md)을 갱신한 기록이다. [세션 30](30-open-question-integration.md)이 Master, 질문 등록부와 영향 역사 세션을 통합한 뒤에도 Domain Design v1.1에 남아 있던 충돌·잠정 의미를 제거하고, Master의 경계를 구현 전 상세 설계 수준으로 연결했다.

이 작업의 범위는 문서 통합뿐이다.

- 코드, 테스트 코드, build/deploy 파일, fixture, PDF와 `data/`를 수정하지 않는다.
- 사용자 답변을 구체 Java API, package, wire DTO 또는 저장 표현의 승인으로 확대하지 않는다.
- `Q-ALG-01`, `Q-BENCH-02`에 scorer, randomized starts, diverse `K`, light-search budget, round/worker 수, `maxSteps` 또는 watchdog 공식 수치를 만들지 않는다.
- `Q-INFRA-01`, `Q-VAR-01`, multi-trip/rotation을 활성화하지 않는다.
- AWS Step Functions/Lambda를 포함한 특정 infrastructure를 목표 topology로 확정하지 않는다.

## 2. 재구성 판단

Domain Design v1.1은 단일 국소 overlay로 안전하게 교정하기 어려웠다. 같은 예제와 타입 설명이 세션 29 이전 의미를 반복했고, 한 항목만 고치면 뒤쪽 전파·평가·예제가 다시 충돌하는 구조였기 때문이다. 따라서 역사 세션은 보존하고 Domain Design 자체는 v2 review 문서로 일관되게 재구성했다.

| Legacy 의미 | 세션 29 이후 의미 | v2 반영 |
|---|---|---|
| 부동소수와 일괄 scale | 무게·부피 item-first `n=3/FLOOR`; 비용·거리·시간 정수 | Numeric normalization과 checked arithmetic 분리 |
| `reqDate`를 release처럼 해석 | `reqDate/dueDate`는 완료기한 alias | Time normalization과 completion deadline으로 통합 |
| Work window 끝에서 arc를 중단·재개 | 전체 arc가 들어갈 때만 출발, 아니면 다음 work start에서 전체 재시작 | Route propagation 불변조건으로 명시 |
| Node 크기 matrix와 좌표 직접 fallback | Physical location 크기, solve 전 complete preparation, runtime lazy fallback 금지 | Travel preparation 계층 신설 |
| Generic feature와 불명확한 zone | Vehicle 단일 concrete feature, order list, exact `ALL`, size/zone AND | Compatibility facts와 route zone 제약 분리 |
| 항상 start/end terminal과 multi-trip 활성 | Oneway는 마지막 고객 종료, single roundtrip만 현재 범위, multi-trip deferred | Terminal/trip 현재 범위와 resume gate 분리 |
| Scalar penalty 중심 평가 | Hard facts → metrics → scores → lexicographic objective → `SolvePlan` | 고객 preset과 objective availability를 상세화 |
| Search bank와 final 상태 혼합 | Search membership, two-state outcome, diagnostic, audit 분리 | Two-gate finalization과 result integrity 추가 |

## 3. Domain Design v2 반영 내용

### 3.1 Input, numeric와 time

- Canonical input, adapter alias와 normalized solver fact를 분리했다.
- 무게·부피는 exact decimal을 item 단위 `n=3`, 비음수 `FLOOR`로 정규화한 뒤 정수 `qty`를 곱한다.
- 비용·거리·시간의 소수 입력은 거부하고 overflow를 sentinel로 바꾸지 않는다.
- Backend가 timezone을 처리하며 solver는 exact `yyyy-MM-dd HH:mm:ss`를 planning origin 기준 정수 초로 바꾼다.
- Plan `[start,end)`, 포함되는 window close, `START_ONLY` 기본과 profile별 `COMPLETE_WITHIN_WINDOW`, 반복·overnight window를 명시했다.
- `serviceTime = duration + Σ(item.taskTime × qty)`이고 `reqDate/dueDate`는 완료기한이다.

### 3.2 Compatibility, request와 route

- Size, free-form capability와 zone을 서로 다른 typed fact로 유지한다.
- Order의 exact `["ALL"]`만 size wildcard이며 free-form code는 case-sensitive exact match다.
- Missing zone은 `"ALL"`이고 한 route의 `ALL` 제외 concrete zone은 최대 하나다.
- Delivery-only와 실제 pickup-delivery를 한 single-trip route에서 혼합할 수 있으며 pair는 atomicity, same vehicle, exactly once와 precedence를 보존한다.
- Oneway는 `multiRotation`을 무시하고 마지막 고객에서 끝난다. Roundtrip은 `multiRotation=0`인 single trip만 현재 범위다.
- Stop은 physical location 전환 기준, drive resource는 실제 arc의 route-wide 누적이다.

### 3.3 Travel preparation

- Matrix cardinality는 solver node가 아니라 unique physical location `M`을 기준으로 한다.
- Provided integer `D/U`를 보존하고 `C`는 무시하며 diagonal은 `0/0`으로 정규화한다.
- 누락 `D`는 Great Circle을 integer meter `HALF_UP`, 누락 `U`는 vehicle별 `CEILING(D × 3.6 ÷ speed)` second로 생성한다.
- Missing speed만 `45 km/h`를 사용한다.
- Complete coverage, provenance와 fingerprint를 solve 전에 확정하고 solver/verifier runtime lazy fallback을 금지한다.
- 현재 Win fixture의 decimal `D/U`는 새 정수 계약에 비준수이며 official baseline evidence로 승격하지 않는다.

### 3.4 State, evaluation와 result

- Immutable problem/profile/travel snapshot과 mutable COW candidate를 분리했다.
- `Q-ALG-02`는 `RESOLVED — KEEP_COW`이며 apply/undo는 측정 evidence와 별도 승인 전 기본이 아니다.
- Hard feasibility, policy-neutral metrics, score, lexicographic objective와 `SolvePlan`을 분리했다.
- Mandatory unassigned는 사용하는 preset의 최상위 차원이다.
- `DIRECT/LEASE` vehicle assignment는 모두 `ASSIGNED`이고 운영자의 후속 외주·이월은 solver outcome이 아니다.
- Static `PROVEN`을 제외한 final `UNASSIGNED`에 cache-free exhaustive insertion audit를 수행하되 feasible insertion을 찾아도 자동 수정·재탐색하지 않는다.
- Candidate verification과 result-integrity verification의 두 gate를 통과한 결과만 publish/compare할 수 있다.

## 4. 질문 상태와 남은 경계

| 상태 | 수량 | 항목 |
|---|---:|---|
| `RESOLVED` | 24 | 세션 29에서 결정된 24개 질문; `Q-ALG-02`의 `KEEP_COW` 포함 |
| `OPEN — EXPERIMENT_REQUIRED` | 2 | `Q-ALG-01`, `Q-BENCH-02` |
| `DEFERRED` | 2 | `Q-INFRA-01`, `Q-VAR-01` |
| 합계 | 28 | 세션 19 canonical 질문 전체 |

남은 활성 항목은 두 calibration protocol의 실제 evidence와 승인뿐이다. Infrastructure와 optional variant는 계속 deferred이며 질문하거나 활성화하지 않는다. Multi-trip/rotation도 별도 exact 계약과 승인 전에는 현재 범위가 아니다.

## 5. 변경 문서

| 문서 | 변경 |
|---|---|
| [Domain Design](../domain-design.md) | v1.1 legacy 의미를 v2 review 상세 설계로 재구성 |
| [Master Design](../master-design.md) | Domain v2와 세션 31 traceability, 문서 관계와 날짜 갱신 |
| [문서 지도](../README.md) | Domain Design을 세 번째 `REVIEW` 진입점으로 등록 |
| [세션 인덱스](README.md) | 세션 31과 최신 Domain 검토 경로 추가 |
| 이 문서 | 판단, 반영 범위, 상태 수와 validation evidence 기록 |

질문 등록부의 status, decision, evidence/gate는 세션 30에서 이미 세션 29와 정합화되었으므로 의미를 다시 쓰지 않았다. 세션 09~17, 19~24, 28, 29의 역사 본문과 세션 30 overlay도 변경하지 않았다.

## 6. Validation 결과

| 검사 | 결과 |
|---|---|
| 질문 상태 수 | `RESOLVED 24`, `OPEN — EXPERIMENT_REQUIRED 2`, `DEFERRED 2`, 합계 28 |
| Canonical question identity | 세션 19과 등록부의 28개 ID·순서·질문 문장 exact match, difference 0 |
| Domain 필수 결정 | Numeric, time, travel, compatibility, route/trip, COW, objective, outcome/audit와 benchmark 경계 assertion 12개 통과 |
| Legacy 충돌 표현 | Partial-arc resume, node-sized matrix, `timeScalePermille`, `Long.MAX_VALUE` sentinel, generic `vehicleFeatures`, multi-trip 활성 기본 0건 |
| Local link/anchor | 관련 19개 문서에서 217개 검사, 오류 0 |
| Markdown fence | 관련 19개 문서 모두 balanced |
| Trailing whitespace | 0건 |
| `git diff --check` | 오류 0 |
| Edit boundary | 문서 5개만 변경·생성; 코드, test, build/deploy, data/PDF/fixture 수정 0건 |

검사 시점의 핵심 문서 크기와 SHA-256은 다음과 같다. 이 통합 기록은 자체 내용을 포함하므로 self-hash를 기록하지 않는다.

| 문서 | 줄 수 | SHA-256 |
|---|---:|---|
| `docs/domain-design.md` | 1,133 | `ab89f320bc279a84493a0c7316edc72c634f9a62d686d2f5101b004bc919445f` |
| `docs/master-design.md` | 945 | `0418637fdcc198101e9b38462e1302bb81854abeda24d58a236c7cc2623857e4` |
| `docs/master-design-open-questions.md` | 78 | `4e211d27639b24aede4d661f37c7d339a0a05a00eef047e844a9ea29cc8846fc` |

## 7. 결론

Domain Design v2는 Master의 의미를 상세화하지만 계속 `REVIEW`다. 세션 31의 완료는 구현, 테스트 통과, official benchmark 수치, infrastructure, optional variant 또는 multi-trip 승인을 뜻하지 않는다. 충돌이 발견되면 Master §1의 authority와 중앙 질문 등록부의 evidence/gate를 사용해 같은 변경 단위에서 교정한다.
