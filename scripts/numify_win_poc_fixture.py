#!/usr/bin/env python3
"""win_poc_case_floor.json의 수치 필드를 JSON number로 정정한다.

Domain §3.1 (2026-08-12 확정): 수치 필드의 JSON 인코딩은 number다 — 문자열 수치는
INVALID_INPUT. 실물 wire가 수치 대부분을 문자열로 보내 실행 fixture가 규약을 어기고
있었으므로, 이 스크립트가 아래 허용 목록의 필드만 number로 바꾼다.

- 정수 표기("45")는 int로, 소수점 표기("26.2"·"2100.0")는 float로 — 값은 바꾸지 않는다.
- 시각·날짜·ID·코드 문자열은 건드리지 않는다. 이동표 C(코드 문자열)도 그대로.
- 원본 win_poc_case.json은 수신 원형 보존 — 이 스크립트의 대상이 아니다.

실행: python3 scripts/numify_win_poc_fixture.py   (저장소 루트에서)
"""

import json
import re
import sys
from collections import Counter
from pathlib import Path

PATH = Path("data/win_poc_case_floor.json")

# 문맥별 변환 대상 필드 (Domain §3.1의 수치 필드)
DEPOT_FIELDS = {"longitude", "latitude", "taskTime"}
VEHICLE_FIELDS = {"maxWeight", "maxVolume", "speed"}
ORDER_FIELDS = {"latitude", "longitude", "duration"}
ITEM_FIELDS = {"qty", "weight", "volume", "taskTime"}
OPTION_KEYS = {
    "multiRotation",
    "driverRestTimeRatio",
    "Optimizer.VehicleMaxStopCount",
    "Optimizer.DefaultSpeed",
    "Termination.secondsSpentLimit",
}
MATRIX_FIELDS = {"D", "U"}  # 이미 number지만 문자열이 섞여 있으면 함께 정정

INT_RE = re.compile(r"-?\d+")

converted = Counter()


def to_number(value, label):
    if isinstance(value, (int, float)):
        return value
    if not isinstance(value, str):
        raise TypeError(f"{label}: 예상 밖 타입 {type(value).__name__}")
    text = value.strip()
    number = int(text) if INT_RE.fullmatch(text) else float(text)
    if float(number) != float(text):  # 값 보존 검산
        raise ValueError(f"{label}: 값 훼손 {value!r} -> {number!r}")
    converted[label] += 1
    return number


def convert_fields(obj, fields, ctx):
    for key in fields:
        if key in obj:
            obj[key] = to_number(obj[key], f"{ctx}.{key}")


def main():
    raw = PATH.read_text(encoding="utf-8")
    data = json.loads(raw)

    for depot in data["depot"]:
        convert_fields(depot, DEPOT_FIELDS, "depot")
    for vehicle in data["vehicles"]:
        convert_fields(vehicle, VEHICLE_FIELDS, "vehicle")
    for order in data["orders"]:
        convert_fields(order, ORDER_FIELDS, "order")
        for item in order.get("items", []):
            convert_fields(item, ITEM_FIELDS, "item")
    convert_fields(data["options"], OPTION_KEYS, "options")
    for row in data["distanceMatrix"]:
        convert_fields(row, MATRIX_FIELDS, "matrix")

    # 규모 불변 검산
    assert len(data["orders"]) == 452 and len(data["vehicles"]) == 31
    assert len(data["distanceMatrix"]) == 205_209 and len(data["depot"]) == 1

    PATH.write_text(
        json.dumps(data, ensure_ascii=False, separators=(", ", ": ")) + "\n",
        encoding="utf-8",
    )

    for label in sorted(converted):
        print(f"{label:28s} {converted[label]:5d}")
    print(f"{'total':28s} {sum(converted.values()):5d}")


if __name__ == "__main__":
    sys.exit(main())
