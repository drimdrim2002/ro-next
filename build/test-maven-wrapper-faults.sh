#!/bin/sh
set -eu

phase00_script_path=$0
case "$phase00_script_path" in
    */*) ;;
    *) phase00_script_path=$(command -v "$phase00_script_path") ;;
esac
phase00_script_directory=${phase00_script_path%/*}
repository=$(CDPATH= cd -- "$phase00_script_directory/.." && pwd)
. "$repository/build/lib/fail-closed-gates.sh"

report=${1:-}
gate_make_temporary_directory 'mktemp(maven-wrapper-faults)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-maven-wrapper.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM
maven_user_home=${MAVEN_USER_HOME:-"${HOME}/.m2"}
private_driver="$repository/build/test-support/maven-private-test-driver.sh"
maven_home="$maven_user_home/wrapper/dists/apache-maven-3.9.14/1ed81e32"
official_maven="$maven_home/bin/mvn"

MAVEN_SKIP_RC=1 "$repository/mvnw" --version \
    >"$temporary/bootstrap.out" 2>"$temporary/bootstrap.err"
test -x "$official_maven"

make_fault_shim() {
    tool=$1
    exit_code=$2
    shim="$temporary/$tool-shim"
    mkdir -p "$shim"
    {
        printf '%s\n' '#!/bin/sh'
        printf 'printf "SUCCESS_SHAPED_%s_OUTPUT\\\\n"\n' "$tool"
        printf 'printf "INJECTED_%s_FAILURE exit=%s\\\\n" >&2\n' \
            "$tool" "$exit_code"
        printf 'exit %s\n' "$exit_code"
    } >"$shim/$tool"
    chmod +x "$shim/$tool"
}

: >"$temporary/results.tsv"
run_fault() {
    case_id=$1
    tool_label=$2
    shift
    shift
    set +e
    (
        cd "$repository"
        "$@"
    ) >"$temporary/$case_id.log" 2>&1
    actual_exit=$?
    set -e
    test "$actual_exit" -eq 41
    gate_text_file_contains "$temporary/$case_id.log" \
        "tool=$tool_label result=NOT_EVALUATED exit=41"
    if gate_text_file_contains "$temporary/$case_id.log" \
        'Apache Maven 3.9.14'; then
        printf 'Maven ran after prerequisite failure: %s\n' "$case_id" >&2
        exit 1
    fi
    printf '%s\t%s\t%s\t%s\n' \
        "$case_id" "$tool_label" "$actual_exit" NOT_EVALUATED \
        >>"$temporary/results.tsv"
}

for tool in uname dirname basename tr sh shasum; do
    make_fault_shim "$tool" 41
    label=$tool
    [ "$tool" != shasum ] || label='shasum(maven-launcher-digest)'
    run_fault "actual-$tool-exit-41" "$label" \
        env PATH="$temporary/$tool-shim:$PATH" ./mvnw --version
done

mkdir -p "$temporary/java-home/bin"
make_fault_shim java 41
cp "$temporary/java-shim/java" "$temporary/java-home/bin/java"
run_fault actual-java-exit-41 java \
    env JAVA_HOME="$temporary/java-home" ./mvnw --version

mkdir -p "$temporary/mavenrc-syntax-home"
printf '%s\n' ')' >"$temporary/mavenrc-syntax-home/.mavenrc"
set +e
(
    cd "$repository"
    env HOME="$temporary/mavenrc-syntax-home" \
        MAVEN_USER_HOME="$maven_user_home" MAVEN_SKIP_RC= \
        ./mvnw --version
) >"$temporary/actual-mavenrc-source-syntax-error.log" 2>&1
mavenrc_source_exit=$?
set -e
test "$mavenrc_source_exit" -eq 2
gate_text_file_contains \
    "$temporary/actual-mavenrc-source-syntax-error.log" \
    'tool=mavenrc-source result=NOT_EVALUATED exit=2'
if gate_text_file_contains \
    "$temporary/actual-mavenrc-source-syntax-error.log" \
    'Apache Maven 3.9.14'; then
    printf '%s\n' 'Maven ran after mavenrc source syntax failure' >&2
    exit 1
fi
printf '%s\t%s\t%s\t%s\n' \
    actual-mavenrc-source-syntax-error mavenrc-source \
    "$mavenrc_source_exit" NOT_EVALUATED >>"$temporary/results.tsv"

make_fault_shim dynamic-maven-executable 41
run_fault actual-dynamic-maven-executable-exit-41 dynamic-maven-executable \
    "$private_driver" dynamic-executable \
    "$temporary/dynamic-maven-executable-shim/dynamic-maven-executable" \
    --version

conditional_tools='
cat
cygpath
curl
expr
javac
ls
mkdir
mktemp
mv
rm
sha256sum
tar
unzip
wget
'
conditional_count=0
for tool in $conditional_tools; do
    conditional_count=$((conditional_count + 1))
    make_fault_shim "$tool" 41
    run_fault "conditional-$tool-exit-41" "$tool" \
        env PATH="$temporary/$tool-shim:$PATH" \
        "$private_driver" conditional-tool "$tool"
done
test "$conditional_count" -eq 14

mkdir -p "$temporary/missing-wrapper"
set +e
(
    cd "$repository"
    "$private_driver" preflight-launcher \
        "$official_maven" "$temporary/missing-wrapper"
) >"$temporary/actual-controlled-maven-launcher-missing.log" 2>&1
controlled_launcher_exit=$?
set -e
test "$controlled_launcher_exit" -eq 126
gate_text_file_contains \
    "$temporary/actual-controlled-maven-launcher-missing.log" \
    'tool=controlled-maven-launcher result=NOT_EVALUATED exit=126'
printf '%s\t%s\t%s\t%s\n' \
    actual-controlled-maven-launcher-missing controlled-maven-launcher \
    "$controlled_launcher_exit" NOT_EVALUATED >>"$temporary/results.tsv"

mkdir -p "$temporary/missing-core-wrapper/build/lib"
cp "$repository/build/lib/maven-launcher-fail-closed.sh" \
    "$temporary/missing-core-wrapper/build/lib/maven-launcher-fail-closed.sh"
set +e
(
    cd "$repository"
    "$private_driver" preflight-launcher \
        "$official_maven" "$temporary/missing-core-wrapper"
) >"$temporary/actual-controlled-maven-launcher-core-missing.log" 2>&1
controlled_launcher_core_exit=$?
set -e
test "$controlled_launcher_core_exit" -eq 126
gate_text_file_contains \
    "$temporary/actual-controlled-maven-launcher-core-missing.log" \
    'tool=controlled-maven-launcher-core result=NOT_EVALUATED exit=126'
printf '%s\t%s\t%s\t%s\n' \
    actual-controlled-maven-launcher-core-missing \
    controlled-maven-launcher-core \
    "$controlled_launcher_core_exit" NOT_EVALUATED >>"$temporary/results.tsv"

mkdir -p "$temporary/classworlds-missing-home/boot"
set +e
(
    cd "$repository"
    "$private_driver" launcher \
        "$temporary/classworlds-missing-home" \
        "$temporary/no-usr-local.mavenrc" "$temporary/no-etc.mavenrc" \
        --version
) >"$temporary/actual-classworlds-missing.log" 2>&1
classworlds_missing_exit=$?
set -e
test "$classworlds_missing_exit" -eq 1
gate_text_file_contains "$temporary/actual-classworlds-missing.log" \
    'tool=maven-classworlds-inventory result=NOT_EVALUATED exit=1'
printf '%s\t%s\t%s\t%s\n' \
    actual-classworlds-missing maven-classworlds-inventory \
    "$classworlds_missing_exit" NOT_EVALUATED >>"$temporary/results.tsv"

mkdir -p "$temporary/classworlds-multiple-home/boot"
: >"$temporary/classworlds-multiple-home/boot/plexus-classworlds-a.jar"
: >"$temporary/classworlds-multiple-home/boot/plexus-classworlds-b.jar"
set +e
(
    cd "$repository"
    "$private_driver" launcher \
        "$temporary/classworlds-multiple-home" \
        "$temporary/no-usr-local.mavenrc" "$temporary/no-etc.mavenrc" \
        --version
) >"$temporary/actual-classworlds-multiple.log" 2>&1
classworlds_multiple_exit=$?
set -e
test "$classworlds_multiple_exit" -eq 1
gate_text_file_contains "$temporary/actual-classworlds-multiple.log" \
    'tool=maven-classworlds-inventory result=NOT_EVALUATED exit=1'
printf '%s\t%s\t%s\t%s\n' \
    actual-classworlds-multiple maven-classworlds-inventory \
    "$classworlds_multiple_exit" NOT_EVALUATED >>"$temporary/results.tsv"

mkdir -p "$temporary/maven-config-missing-home/boot"
: >"$temporary/maven-config-missing-home/boot/plexus-classworlds-2.9.0.jar"
set +e
(
    cd "$repository"
    "$private_driver" launcher \
        "$temporary/maven-config-missing-home" \
        "$temporary/no-usr-local.mavenrc" "$temporary/no-etc.mavenrc" \
        --version
) >"$temporary/actual-maven-launcher-config-missing.log" 2>&1
maven_config_missing_exit=$?
set -e
test "$maven_config_missing_exit" -eq 1
gate_text_file_contains "$temporary/actual-maven-launcher-config-missing.log" \
    'tool=maven-launcher-config result=NOT_EVALUATED exit=1'
printf '%s\t%s\t%s\t%s\n' \
    actual-maven-launcher-config-missing maven-launcher-config \
    "$maven_config_missing_exit" NOT_EVALUATED >>"$temporary/results.tsv"

mkdir -p "$temporary/public-home" "$temporary/public-syntax-home"
printf '%s\n' ')' >"$temporary/public-syntax-home/.mavenrc"
{
    printf '%s\n' '#!/bin/sh' 'exit 0'
} >"$temporary/exit-zero-launcher"
chmod +x "$temporary/exit-zero-launcher"
{
    printf '%s\n' \
        'MAVEN_OPTS=--phase00-review08-invalid-system-rc-option' \
        'export MAVEN_OPTS'
} >"$temporary/mutating-system.mavenrc"
mkdir -p "$temporary/public-fake-maven-home"

capture_public() {
    public_prefix=$1
    public_home=$2
    shift
    shift
    set +e
    (
        cd "$repository"
        env HOME="$public_home" MAVEN_USER_HOME="$maven_user_home" \
            MAVEN_SKIP_RC= "$@" ./mvnw --version
    ) >"$public_prefix.stdout" 2>"$public_prefix.stderr"
    PUBLIC_CAPTURE_EXIT=$?
    set -e
}

: >"$temporary/public-attacks.tsv"
capture_public "$temporary/public-baseline" "$temporary/public-home"
public_baseline_exit=$PUBLIC_CAPTURE_EXIT
test "$public_baseline_exit" -eq 0

assert_public_baseline() {
    public_case_id=$1
    shift
    capture_public "$temporary/$public_case_id" "$temporary/public-home" "$@"
    test "$PUBLIC_CAPTURE_EXIT" -eq "$public_baseline_exit"
    gate_compare_files "$public_case_id stdout baseline" \
        "$temporary/public-baseline.stdout" \
        "$temporary/$public_case_id.stdout" \
        "$temporary/$public_case_id.stdout.cmp" \
        "$temporary/$public_case_id.stdout.cmp.err"
    gate_compare_files "$public_case_id stderr baseline" \
        "$temporary/public-baseline.stderr" \
        "$temporary/$public_case_id.stderr" \
        "$temporary/$public_case_id.stderr.cmp" \
        "$temporary/$public_case_id.stderr.cmp.err"
    printf '%s\t%s\t%s\n' \
        "$public_case_id" "$PUBLIC_CAPTURE_EXIT" BASELINE_UNCHANGED \
        >>"$temporary/public-attacks.tsv"
}

assert_public_baseline public-test-mode-alone \
    PHASE00_MVNW_TEST_MODE=1
assert_public_baseline public-conditional-tool-alone \
    PHASE00_MVNW_TEST_CONDITIONAL_TOOL=uname
assert_public_baseline public-maven-executable-alone \
    PHASE00_MVNW_TEST_MAVEN_EXECUTABLE="$temporary/exit-zero-launcher"
assert_public_baseline public-controlled-launcher-alone \
    PHASE00_MVNW_TEST_CONTROLLED_LAUNCHER="$temporary/exit-zero-launcher"
assert_public_baseline public-maven-home-alone \
    PHASE00_MVNW_TEST_MAVEN_HOME="$temporary/public-fake-maven-home"
assert_public_baseline public-usr-local-rc-alone \
    PHASE00_MVNW_TEST_USR_LOCAL_MAVENRC="$temporary/mutating-system.mavenrc"
assert_public_baseline public-etc-rc-alone \
    PHASE00_MVNW_TEST_ETC_MAVENRC="$temporary/mutating-system.mavenrc"

assert_public_baseline public-mode-conditional-tool-combination \
    PHASE00_MVNW_TEST_MODE=1 \
    PHASE00_MVNW_TEST_CONDITIONAL_TOOL=uname
assert_public_baseline public-mode-maven-executable-combination \
    PHASE00_MVNW_TEST_MODE=1 \
    PHASE00_MVNW_TEST_MAVEN_EXECUTABLE="$temporary/exit-zero-launcher"
assert_public_baseline public-mode-controlled-launcher-combination \
    PHASE00_MVNW_TEST_MODE=1 \
    PHASE00_MVNW_TEST_CONTROLLED_LAUNCHER="$temporary/exit-zero-launcher"
assert_public_baseline public-mode-maven-home-combination \
    PHASE00_MVNW_TEST_MODE=1 \
    PHASE00_MVNW_TEST_MAVEN_HOME="$temporary/public-fake-maven-home"
assert_public_baseline public-mode-usr-local-rc-combination \
    PHASE00_MVNW_TEST_MODE=1 \
    PHASE00_MVNW_TEST_USR_LOCAL_MAVENRC="$temporary/mutating-system.mavenrc"
assert_public_baseline public-mode-etc-rc-combination \
    PHASE00_MVNW_TEST_MODE=1 \
    PHASE00_MVNW_TEST_ETC_MAVENRC="$temporary/mutating-system.mavenrc"

capture_public "$temporary/public-syntax-baseline" \
    "$temporary/public-syntax-home"
public_syntax_baseline_exit=$PUBLIC_CAPTURE_EXIT
test "$public_syntax_baseline_exit" -eq 2
gate_text_file_contains "$temporary/public-syntax-baseline.stderr" \
    'tool=mavenrc-source result=NOT_EVALUATED exit=2'
capture_public "$temporary/public-all-overrides-syntax" \
    "$temporary/public-syntax-home" \
    PHASE00_MVNW_TEST_MODE=1 \
    PHASE00_MVNW_TEST_CONDITIONAL_TOOL=uname \
    PHASE00_MVNW_TEST_MAVEN_EXECUTABLE="$temporary/exit-zero-launcher" \
    PHASE00_MVNW_TEST_CONTROLLED_LAUNCHER="$temporary/exit-zero-launcher" \
    PHASE00_MVNW_TEST_MAVEN_HOME="$temporary/public-fake-maven-home" \
    PHASE00_MVNW_TEST_USR_LOCAL_MAVENRC="$temporary/mutating-system.mavenrc" \
    PHASE00_MVNW_TEST_ETC_MAVENRC="$temporary/mutating-system.mavenrc"
test "$PUBLIC_CAPTURE_EXIT" -eq "$public_syntax_baseline_exit"
gate_compare_files 'all overrides syntax stdout baseline' \
    "$temporary/public-syntax-baseline.stdout" \
    "$temporary/public-all-overrides-syntax.stdout" \
    "$temporary/public-all-overrides-syntax.stdout.cmp" \
    "$temporary/public-all-overrides-syntax.stdout.cmp.err"
gate_compare_files 'all overrides syntax stderr baseline' \
    "$temporary/public-syntax-baseline.stderr" \
    "$temporary/public-all-overrides-syntax.stderr" \
    "$temporary/public-all-overrides-syntax.stderr.cmp" \
    "$temporary/public-all-overrides-syntax.stderr.cmp.err"
printf '%s\t%s\t%s\n' \
    public-all-overrides-syntax-error "$PUBLIC_CAPTURE_EXIT" \
    SYNTAX_ERROR_DIAGNOSTIC_AND_EXIT_UNCHANGED \
    >>"$temporary/public-attacks.tsv"

gate_require_no_match 'public Maven production source test override' \
    "$temporary/public-source-override.scan" \
    "$temporary/public-source-override.scan.err" \
    grep -n 'PHASE00_MVNW_TEST_' \
    "$repository/mvnw" \
    "$repository/build/lib/maven-wrapper-fail-closed.sh" \
    "$repository/build/lib/maven-launcher-fail-closed.sh" \
    "$repository/build/lib/maven-launcher-core.sh"

gate_capture_checked 'awk(public-attack-case-count)' \
    "$temporary/public-attack-count.out" \
    "$temporary/public-attack-count.err" \
    awk 'END { print NR + 0 }' "$temporary/public-attacks.tsv"
IFS= read -r public_attack_count \
    <"$temporary/public-attack-count.out" || :
test "$public_attack_count" -eq 14

gate_capture_checked 'awk(maven-wrapper-fault-case-count)' \
    "$temporary/count.out" "$temporary/count.err" \
    awk 'END { print NR + 0 }' "$temporary/results.tsv"
IFS= read -r case_count <"$temporary/count.out" || :
test "$case_count" -eq 28

{
    printf '%s\n' 'PHASE00_MAVEN_WRAPPER_FAULT_SELF_TEST=PASS'
    printf '%s\n' 'CASE_COUNT=28'
    printf '%s\n' 'ACTUAL_PATH_FAULT_CASE_COUNT=14'
    printf '%s\n' 'ACTUAL_PREREQUISITE_CLASS_COUNT=13'
    printf '%s\n' 'CONDITIONAL_PATH_FAULT_CASE_COUNT=14'
    printf '%s\n' 'MAVENRC_SOURCE_ERROR_CASE_COUNT=1'
    printf '%s\n' 'PRIVATE_TEST_DRIVER_FAULT_CASE_COUNT=20'
    printf 'PUBLIC_TEST_OVERRIDE_ATTACK_COUNT=%s\n' "$public_attack_count"
    printf '%s\n' 'PUBLIC_PRODUCTION_TEST_OVERRIDE_REFERENCE_COUNT=0'
    printf '%s\n' 'PUBLIC_PRIVATE_DRIVER_SELECTION_PATH=NONE'
    printf '%s\n' 'FAKE_SUCCESS_OUTPUT_THEN_NONZERO_CASE_COUNT=22'
    printf '%s\n' 'EXACT_EXIT_41_CASE_COUNT=22'
    printf '%s\n' 'DOWNSTREAM_MAVEN_EXECUTION_COUNT=0'
    printf '%s\n' 'FALSE_GREEN_COUNT=0'
    printf '%s\n' \
        'case-id	tool-label	actual-exit	result'
    gate_replay_text_file "$temporary/results.tsv"
    printf '%s\n' \
        'public-attack-id	actual-exit	oracle'
    gate_replay_text_file "$temporary/public-attacks.tsv"
} >"$temporary/report.txt"

if [ -n "$report" ]; then
    gate_publish_file_atomic 'maven-wrapper-fault-report' \
        "$temporary/report.txt" "$report" "$temporary/report-publish"
else
    gate_replay_text_file "$temporary/report.txt"
fi
