#!/bin/sh
set -eu

phase00_script_path=$0
case "$phase00_script_path" in
    */*) ;;
    *) phase00_script_path=$(command -v "$phase00_script_path") ;;
esac
phase00_script_directory=${phase00_script_path%/*}
default_repository=$(CDPATH= cd -- "$phase00_script_directory/.." && pwd)
. "$default_repository/build/lib/fail-closed-gates.sh"
repository=${PHASE00_SCAN_REPOSITORY:-"$default_repository"}
gate_make_temporary_directory 'mktemp(bytecode-boundaries)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-jdeps.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

modules='
rpdptw/core
rpdptw/solver
rpdptw/verification
rpdptw/application
rpdptw/capabilities
rpdptw/profile-catalog
'

first_module_jar() {
    module=$1
    output=$2
    gate_stage_find "$repository/$module/target" \
        "$temporary/$output.find.raw" "$temporary/$output.find.err" \
        . -maxdepth 1 -type f -name '*.jar' \
        ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print
    gate_sort_file "$temporary/$output.find.raw" \
        "$temporary/$output.find.sorted" "$temporary/$output.sort.err"
    IFS= read -r relative_jar <"$temporary/$output.find.sorted" || :
    if [ -z "$relative_jar" ]; then
        printf 'missing stable module jar: %s\n' "$module" >&2
        return 1
    fi
    gate_normalize_relative_path "$relative_jar"
    GATE_FIRST_JAR="$repository/$module/target/$GATE_RELATIVE_PATH"
}

jar_count=0
: >"$temporary/jdeps.txt"
for module in $modules; do
    module_id=${module%/*}-${module#*/}
    first_module_jar "$module" "$module_id"
    jar_file=$GATE_FIRST_JAR
    jar_count=$((jar_count + 1))
    printf 'MODULE=%s JAR=%s\n' \
        "$module" "${jar_file#"$repository"/}" >>"$temporary/jdeps.txt"
    gate_capture_checked "jdeps($module)" \
        "$temporary/$module_id.jdeps.out" \
        "$temporary/$module_id.jdeps.err" \
        jdeps --multi-release 25 --ignore-missing-deps -q "$jar_file"
    gate_replay_text_file "$temporary/$module_id.jdeps.out" \
        >>"$temporary/jdeps.txt"
done

test "$jar_count" -eq 6

gate_require_no_match \
    'stable bytecode provider/vendor/legacy dependency predicate' \
    "$temporary/jdeps-forbidden.matches" \
    "$temporary/jdeps-forbidden.detector.err" \
    grep -E \
    'com[.]google|software[.]amazon|com[.]amazonaws|com[.]azure|io[.]kubernetes|gurobi|com[.]ronext[.]optimizer' \
    "$temporary/jdeps.txt"

first_module_jar 'rpdptw/verification' 'verification'
verification_jar=$GATE_FIRST_JAR
gate_capture_checked "jar(list:verification)" \
    "$temporary/verification-jar-entries.txt" \
    "$temporary/verification-jar-entries.err" \
    jar tf "$verification_jar"
gate_require_no_match \
    'verification jar solver/legacy bytecode predicate' \
    "$temporary/verification-forbidden.matches" \
    "$temporary/verification-forbidden.detector.err" \
    grep -E 'com/ronext/rpdptw/solver|com/ronext/optimizer' \
    "$temporary/verification-jar-entries.txt"

fixture_tests="$repository/build/test-fixtures/target/rpdptw-test-fixtures-0.1.0-SNAPSHOT-tests.jar"
fixture_main="$repository/build/test-fixtures/target/rpdptw-test-fixtures-0.1.0-SNAPSHOT.jar"
test -f "$fixture_tests"
test -f "$fixture_main"

gate_capture_checked "jar(list:test-fixture-classifier)" \
    "$temporary/fixture-tests-entries.txt" \
    "$temporary/fixture-tests-entries.err" jar tf "$fixture_tests"
gate_require_match \
    'test fixture classifier bytecode predicate' \
    "$temporary/fixture-tests-required.matches" \
    "$temporary/fixture-tests-required.detector.err" \
    grep -F 'com/ronext/rpdptw/fixture/package-info.class' \
    "$temporary/fixture-tests-entries.txt"

gate_capture_checked "jar(list:test-fixture-main)" \
    "$temporary/fixture-main-entries.txt" \
    "$temporary/fixture-main-entries.err" jar tf "$fixture_main"
gate_require_no_match \
    'test fixture main-artifact leakage predicate' \
    "$temporary/fixture-main-forbidden.matches" \
    "$temporary/fixture-main-forbidden.detector.err" \
    grep -F 'com/ronext/rpdptw/fixture/' \
    "$temporary/fixture-main-entries.txt"

gate_replay_text_file "$temporary/jdeps.txt"
printf '%s\n' 'PHASE00_BYTECODE_BOUNDARIES=PASS'
printf 'STABLE_JAR_COUNT=%s\n' "$jar_count"
printf '%s\n' 'FORBIDDEN_BYTECODE_REFERENCE_COUNT=0'
printf '%s\n' 'VERIFICATION_TO_SOLVER_OR_LEGACY_COUNT=0'
printf '%s\n' 'TEST_FIXTURE_MAIN_LEAK_COUNT=0'
