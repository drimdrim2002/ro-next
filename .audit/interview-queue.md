# 감사 후속 인터뷰 대기열 (2026-08-12 시작)

치명 4건은 처리 완료. 남은 중 결함 중 **시스템 소유자 결정이 필요한 것**을 순서대로 묻는다.
상태: ⬜ 대기 · 🔷 질문 중 · ✅ 확정·반영 · ⏸ 보류

## A. Stage 1 파싱 계열 (Domain §1~5 — 구현 착수 전 필요)

| # | 상태 | 주제 | 결함 출처 |
|---|---|---|---|
| 1 | ✅ 표기 기준 (`15.0`도 거부) | 소수 판정 기준 — 표기(토큰 소수점) vs 값(비정수) | 분할 2 #4 (C-13) |
| 2 | ✅ 별칭 수용 (둘 다 오면 오류) | `maxDriveDist` vs PDF `maxDriveDistc` — 실제 wire 키 철자 | 분할 2 #6 |
| 3 | ✅ itemId 필수, 부재 시 orderId (prodId 폴백 폐기 — 작성 시 발명이었음) | `itemId` 빈 값 허용 여부 (실물 452건 전부 "" vs PDF Mandatory) | 분할 2 #10 |
| 4 | ✅ 셋 다 무시 (비율 0 아닌 값도 거부 안 함 — 수용된 위험) | `driverRestTimeRatio` 처분 (+ `difficultySortType`·`customerAbbr`) | 분할 2 #7 |
| 5 | ✅ 무시 (Domain §2.1 정본 + 실물 목록 등재) | 미지 wire 필드 일반 정책 (무시/거부) — `DEPOT`·`district`·`continent`·`lssId`·`routes` 등 | 분할 2 #7 |
| 6 | ✅ 와일드카드 (부재·"ALL" = 전 차급, Optional.empty 접기) | 차량 `vehicleFeature="ALL"`(규약 기본값) 처리 — 와일드카드 vs 거부 | 분할 2 #9 |
| 7 | ✅ 검증·거부 (KG/CBM 외 INVALID_INPUT, 부재 = KG·CBM) | 단위 코드(`weightUnitCd`/`volumeUnitCd`) 불일치 시 거부 여부 | 분할 2 #12 |

## B. Domain §6~13 (Stage 3~5 계약)

| # | 상태 | 주제 | 결함 출처 |
|---|---|---|---|
| 8 | ✅ 첫 방문도 +1 (Win 28=28 검산으로 확정) | `stopCount` 첫 방문 +1 여부 | 분할 3 #3 |
| 9 | ✅ 4종 + 검사 순서 (stage-05 §4.3 절차를 정본 승격 — 용량이 시간창보다 먼저) | 미배정 `reason`의 판정 규칙 정본화 | 분할 3 #4 |
| 10 | ✅ 참조형 전환 (§6.2+§6.3+§3.4+§2.6+profile hard 참조) | §10.2 재검증 목록 관리 방식 | C-11 |
| 11 | ✅ 삭제 (stage-04 6곳·stage-05 4곳 정리) | `AlnsResult.bestRouteFacts` — 삭제 vs 소비 정의 | 분할 7 F6 |
| 12 | ✅ 반환 봉투 `ParseResult(PlanInput, OptionalLong)` | `secondsSpentLimit` 운반 경로 시그니처 | 분할 8 §1-2 |
| 13 | ✅ 둘째 관문(7-a) 추가 — 5~7 위반 시 8·9 생략 | stage-05 §3 절차 관문 | 분할 7 F4 |
| 14 | ✅ 인용 교체 — 5곳 전부 Domain §11.1로 | Master §3-⑪ 유령 결정 | C-2 |
| 15 | ✅ 설계 확정 삭제 (E1 본체에 재설계 포인터 — stage-01·03·extra 교정) | `Depot.nodeId` 분류 | C-6 |
| 16 | ✅ "표시하고 남김" 공통화 (README 등재, stage-01 반대 규칙 대체 — 7개 문서 잔존 정리는 기계적 묶음으로) | §9 잔존 질문 정리 규칙 공통화 | C-8 |

## C. 기계적 정정 — ✅ 전부 처리 완료 (2026-08-13, 작업자 A·B 위임 + 검수)

- C-1 master:39·plan:362 접수 깊이 옛 문면 · C-3 §8.3 과잉 단언(발원 Domain) ·
  C-4 "1바퀴에는 복귀가 없다"(발원 Domain §2.5:261) · C-5 trips 유예 상위 잔재 ·
  C-7 verify↛solve 재서술 3곳 · C-9 규약/실물 어휘 구분 · C-10 §11.1 의미 정본 좁히기 ·
  C-12 Stage 0 완료 표시 · Domain §7.1 위반 귀속의 stage-03·05 전파 ·
  stage-08 `maxIterations`→`maxSteps` · §7.2 숫자 예 정정 · §3.3 serviceStartTime §7.1 참조 ·
  §2.4 "공통(대부분 존재)" 분류 분리 · 분할 5 잔여(stage-01 T12 논거·stage-extra 전칭 등)
- 경 41건 — 위 처리 후 일괄

## 처리 이력

- 치명 ①②③④ — 2026-08-12 반영 완료 (Domain §2.4/§3.1/§4/§12 · fixture number 정정 ·
  stage-04 §4.2 빈 해 강등 · 하위 8문서 전파)
- 결정 16건 — 2026-08-12~13 인터뷰로 전부 확정·반영 (위 A·B 표)
- 기계적 정정 — 2026-08-13 완료: 작업자 A(상위 문서 + 분할 1~4 경 결함)·B(stage 문서 +
  분할 5~8 경 결함 + 질문 절 잔존 정리) + 오케스트레이터 후속 4건(CLAUDE.md 흐름 그림 ·
  architecture §3.1 UNSUPPORTED/INVALID 구분 · plan Stage 1 DoD 문구 · stage-06 §5
  "이름 그대로" 교정). 교차 검사 통과 (유령 인용·삭제 필드·옛 문면 잔존 0)
- ⚠ 소유자 확인 권장 1건: 분할 7 F5 판정으로 `SearchBudget.termination` **삭제** —
  종료 사유(TIME_LIMIT 등)가 result.json run 메타에서 빠지고 실행 로그(`AlnsRunStats`)로만
  남는다. 결과에 남기길 원하면 Domain §11.1 개정(예산 예외 편입) 후 stage-05·06·08 복원
