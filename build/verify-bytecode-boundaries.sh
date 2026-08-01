#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${ROOT_DIR}"

echo "==> Verifying bytecode boundaries..."

JAR_FILES=$(find rpdptw -name "rpdptw-*.jar" -not -name "*-tests.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar")

if [ -z "$JAR_FILES" ]; then
    echo "==> Target JARs missing. Building JARs..."
    ./mvnw -B -ntp package -DskipTests
    JAR_FILES=$(find rpdptw -name "rpdptw-*.jar" -not -name "*-tests.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar")
fi

if [ -z "$JAR_FILES" ]; then
    echo "ERROR: No JAR files found in rpdptw/ after build."
    exit 1
fi

FORBIDDEN_PACKAGES=(
    "com/google/cloud"
    "software/amazon/awssdk"
    "com/google/ortools"
    "com/ronext/optimizer"
)

VIOLATIONS=0

for jar in $JAR_FILES; do
    echo "Checking JAR: $jar"
    for forbidden in "${FORBIDDEN_PACKAGES[@]}"; do
        if jar tf "$jar" | grep "$forbidden" >/dev/null; then
            echo "ERROR: Forbidden package path '$forbidden' found in $jar"
            VIOLATIONS=$((VIOLATIONS + 1))
        fi
    done

    if command -v jdeps &> /dev/null; then
        JDEPS_OUTPUT=$(jdeps -verbose:class -s "$jar" 2>/dev/null || true)
        if echo "$JDEPS_OUTPUT" | grep -E "com\.google\.cloud|software\.amazon\.awssdk|com\.google\.ortools|com\.ronext\.optimizer" >/dev/null; then
            echo "ERROR: Forbidden import found in $jar via jdeps"
            VIOLATIONS=$((VIOLATIONS + 1))
        fi
    fi
done

if [ "$VIOLATIONS" -gt 0 ]; then
    echo "FAILED: $VIOLATIONS bytecode boundary violations found."
    exit 1
fi

echo "SUCCESS: Bytecode boundary verification passed."
