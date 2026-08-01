#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${ROOT_DIR}"

echo "==> Running negative architecture rule verification (-Pnegative-arch)..."

set +e
OUTPUT=$(./mvnw -B -ntp -Dstyle.color=never -Pnegative-arch -pl build/architecture-rules -am test 2>&1)
EXIT_CODE=$?
set -e

if [ $EXIT_CODE -eq 0 ]; then
    echo "ERROR: Expected negative architecture test to FAIL, but it SUCCEEDED (exit code 0)."
    echo "$OUTPUT"
    exit 1
fi

echo "==> Command failed with non-zero exit code as expected ($EXIT_CODE)."
echo "==> Verifying failure messages for negative architecture rules..."

REQUIRED_PATTERNS=(
    "ProviderAndVendorIsolationArchitectureTest"
    "StableModuleDependencyArchitectureTest"
    "PackageBoundaryArchitectureTest"
    "CustomerIsolationArchitectureTest"
    "CorePuritySourceScanTest"
    "RouteSelectionAbsenceTest"
    "TestScopeLeakageArchitectureTest"
)

MISSING=0
for pattern in "${REQUIRED_PATTERNS[@]}"; do
    if echo "$OUTPUT" | grep -q "$pattern"; then
        echo "  [OK] Found expected failure trace for: $pattern"
    else
        echo "  [FAIL] Missing failure output for: $pattern"
        MISSING=$((MISSING + 1))
    fi
done

if [ $MISSING -gt 0 ]; then
    echo "ERROR: $MISSING negative architecture rule checks were not found in output."
    echo "$OUTPUT"
    exit 1
fi

echo "SUCCESS: Negative architecture profile proved all 7 architecture rules fail closed."
exit 0
