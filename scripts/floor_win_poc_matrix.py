#!/usr/bin/env python3
"""Floor WIN PoC distance-matrix distance/time values to integer units."""

from __future__ import annotations

import argparse
import json
from decimal import Decimal, InvalidOperation, ROUND_FLOOR
from pathlib import Path
from typing import Any


MATRIX_FIELDS = {
    "D": "distance in meters",
    "U": "duration in seconds",
}


def floor_nonnegative(value: Any, *, field: str, row_index: int) -> int:
    """Parse an exact JSON number/string and return its mathematical floor."""
    if isinstance(value, bool) or not isinstance(value, (int, float, str)):
        raise ValueError(
            f"distanceMatrix[{row_index}].{field} must be a number or numeric string"
        )

    try:
        decimal_value = Decimal(str(value))
    except InvalidOperation as error:
        raise ValueError(
            f"distanceMatrix[{row_index}].{field} is not a valid decimal: {value!r}"
        ) from error

    if not decimal_value.is_finite() or decimal_value < 0:
        raise ValueError(
            f"distanceMatrix[{row_index}].{field} must be finite and nonnegative"
        )

    return int(decimal_value.to_integral_value(rounding=ROUND_FLOOR))


def transform(document: dict[str, Any]) -> tuple[dict[str, Any], dict[str, int]]:
    matrix = document.get("distanceMatrix")
    if not isinstance(matrix, list):
        raise ValueError("root.distanceMatrix must be an array")

    changed = {field: 0 for field in MATRIX_FIELDS}
    for row_index, row in enumerate(matrix):
        if not isinstance(row, dict):
            raise ValueError(f"distanceMatrix[{row_index}] must be an object")

        for field in MATRIX_FIELDS:
            if field not in row:
                raise ValueError(f"distanceMatrix[{row_index}].{field} is missing")
            floored = floor_nonnegative(row[field], field=field, row_index=row_index)
            if Decimal(str(row[field])) != Decimal(floored):
                changed[field] += 1
            row[field] = floored

    return document, changed


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Floor distanceMatrix.D (meters) and distanceMatrix.U (seconds), "
            "leaving all other fields unchanged."
        )
    )
    parser.add_argument("input", type=Path, help="source WIN PoC JSON file")
    parser.add_argument("output", type=Path, help="destination JSON file")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.input.resolve() == args.output.resolve():
        raise ValueError("input and output paths must be different")

    with args.input.open("r", encoding="utf-8") as source:
        document = json.load(source)
    if not isinstance(document, dict):
        raise ValueError("the JSON root must be an object")

    transformed, changed = transform(document)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8") as destination:
        json.dump(
            transformed,
            destination,
            ensure_ascii=False,
            separators=(",", ":"),
        )
        destination.write("\n")

    print(
        f"wrote {args.output}: "
        f"{len(transformed['distanceMatrix'])} matrix rows, "
        f"D floored={changed['D']}, U floored={changed['U']}"
    )


if __name__ == "__main__":
    main()
