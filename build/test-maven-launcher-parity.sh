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
gate_make_temporary_directory 'mktemp(maven-launcher-parity)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-maven-parity.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

maven_user_home=${MAVEN_USER_HOME:-"${HOME}/.m2"}
runtime_java_home=${JAVA_HOME:-}
maven_home="$maven_user_home/wrapper/dists/apache-maven-3.9.14/1ed81e32"
official_maven="$maven_home/bin/mvn"
wrapper="$repository/mvnw"
private_driver="$repository/build/test-support/maven-private-test-driver.sh"

# Materialize or reuse the exact distribution selected by the checked-in
# wrapper, but prevent ambient user/system rc from influencing this bootstrap.
MAVEN_SKIP_RC=1 "$wrapper" --version >"$temporary/bootstrap.out" \
    2>"$temporary/bootstrap.err"
test -x "$official_maven"
gate_sha256_file "$official_maven" "$temporary/official-maven"
test "$GATE_SHA256" = \
    f9381d0cb98abaaf9592dae421eddc497e84ed9bfb723b84c111d1350863c3a2

mkdir -p \
    "$temporary/home-empty" \
    "$temporary/home-user" \
    "$temporary/home-source" \
    "$temporary/home-syntax" \
    "$temporary/home-order" \
    "$temporary/work/project/.mvn" \
    "$temporary/work/project/child" \
    "$temporary/work/project-space/.mvn" \
    "$temporary/work/relative-project/.mvn" \
    "$temporary/work/valid-project/.mvn" \
    "$temporary/work/maven-config/.mvn" \
    "$temporary/java-home/bin"

{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' \
        'if [ "$#" -eq 1 ] && [ "$1" = -version ]; then exit 0; fi'
    printf '%s\n' \
        'if [ "$#" -eq 2 ] && [ "$1" = --enable-native-access=ALL-UNNAMED ] && [ "$2" = -version ]; then exit 0; fi'
    printf '%s\n' \
        'printf "RC_TRACE=%s\\n" "${PHASE00_RC_TRACE-}"' \
        'printf "MAVEN_PROJECTBASEDIR=%s\\n" "${MAVEN_PROJECTBASEDIR-}"' \
        'printf "MAVEN_CMD_LINE_ARGS=%s\\n" "${MAVEN_CMD_LINE_ARGS-}"'
    printf '%s\n' 'phase00_parity_index=0'
    printf '%s\n' \
        'for phase00_parity_argument in "$@"; do' \
        '  phase00_parity_index=$((phase00_parity_index + 1))' \
        '  printf "ARG_%s=<%s>\\n" "$phase00_parity_index" "$phase00_parity_argument"' \
        'done'
    printf '%s\n' 'exit "${PHASE00_PARITY_JAVA_EXIT-0}"'
} >"$temporary/java-home/bin/java"
chmod +x "$temporary/java-home/bin/java"

case_home=$temporary/home-empty
case_skip_rc=
case_java_home=$runtime_java_home
case_maven_basedir=
case_maven_opts=
case_maven_debug_opts=
case_maven_args=
case_maven_config=
case_java_exit=0

reset_case_environment() {
    case_home=$temporary/home-empty
    case_skip_rc=
    case_java_home=$runtime_java_home
    case_maven_basedir=
    case_maven_opts=
    case_maven_debug_opts=
    case_maven_args=
    case_maven_config=
    case_java_exit=0
}

capture_launcher() {
    phase00_parity_launcher=$1
    phase00_parity_cwd=$2
    phase00_parity_prefix=$3
    shift
    shift
    shift

    case $- in
        *e*) phase00_parity_had_errexit=1 ;;
        *) phase00_parity_had_errexit=0 ;;
    esac
    set +e
    (
        set +x
        cd "$phase00_parity_cwd"
        env \
            HOME="$case_home" \
            MAVEN_USER_HOME="$maven_user_home" \
            MAVEN_SKIP_RC="$case_skip_rc" \
            JAVA_HOME="$case_java_home" \
            MAVEN_BASEDIR="$case_maven_basedir" \
            MAVEN_OPTS="$case_maven_opts" \
            MAVEN_DEBUG_OPTS="$case_maven_debug_opts" \
            MAVEN_ARGS="$case_maven_args" \
            MAVEN_CONFIG="$case_maven_config" \
            PHASE00_PARITY_JAVA_EXIT="$case_java_exit" \
            "$phase00_parity_launcher" "$@"
    ) >"$phase00_parity_prefix.stdout" \
        2>"$phase00_parity_prefix.stderr"
    PHASE00_PARITY_CAPTURE_EXIT=$?
    if [ "$phase00_parity_had_errexit" -eq 1 ]; then
        set -e
    else
        set +e
    fi
}

capture_private_launcher() {
    phase00_parity_cwd=$1
    phase00_parity_prefix=$2
    phase00_parity_usr_local_rc=$3
    phase00_parity_etc_rc=$4
    shift
    shift
    shift
    shift

    case $- in
        *e*) phase00_parity_had_errexit=1 ;;
        *) phase00_parity_had_errexit=0 ;;
    esac
    set +e
    (
        set +x
        cd "$phase00_parity_cwd"
        env \
            HOME="$case_home" \
            MAVEN_USER_HOME="$maven_user_home" \
            MAVEN_SKIP_RC="$case_skip_rc" \
            JAVA_HOME="$case_java_home" \
            MAVEN_BASEDIR="$case_maven_basedir" \
            MAVEN_OPTS="$case_maven_opts" \
            MAVEN_DEBUG_OPTS="$case_maven_debug_opts" \
            MAVEN_ARGS="$case_maven_args" \
            MAVEN_CONFIG="$case_maven_config" \
            PHASE00_PARITY_JAVA_EXIT="$case_java_exit" \
            "$private_driver" launcher "$maven_home" \
            "$phase00_parity_usr_local_rc" "$phase00_parity_etc_rc" "$@"
    ) >"$phase00_parity_prefix.stdout" \
        2>"$phase00_parity_prefix.stderr"
    PHASE00_PARITY_CAPTURE_EXIT=$?
    if [ "$phase00_parity_had_errexit" -eq 1 ]; then
        set -e
    else
        set +e
    fi
}

: >"$temporary/results.tsv"
exact_pair_count=0
failure_only_pair_count=0
fail_closed_enrichment_pair_count=0
controlled_seam_count=0

compare_exact_pair() {
    phase00_parity_case_id=$1
    phase00_parity_expected_exit=$2
    phase00_parity_cwd=$3
    shift
    shift
    shift

    capture_launcher "$official_maven" "$phase00_parity_cwd" \
        "$temporary/$phase00_parity_case_id.official" "$@"
    phase00_parity_official_exit=$PHASE00_PARITY_CAPTURE_EXIT
    capture_launcher "$wrapper" "$phase00_parity_cwd" \
        "$temporary/$phase00_parity_case_id.wrapper" "$@"
    phase00_parity_wrapper_exit=$PHASE00_PARITY_CAPTURE_EXIT

    test "$phase00_parity_official_exit" -eq "$phase00_parity_expected_exit"
    test "$phase00_parity_wrapper_exit" -eq "$phase00_parity_official_exit"
    if ! gate_compare_files "$phase00_parity_case_id stdout parity" \
        "$temporary/$phase00_parity_case_id.official.stdout" \
        "$temporary/$phase00_parity_case_id.wrapper.stdout" \
        "$temporary/$phase00_parity_case_id.stdout.cmp" \
        "$temporary/$phase00_parity_case_id.stdout.cmp.err"; then
        printf 'official stdout (%s):\n' "$phase00_parity_case_id" >&2
        gate_replay_text_file \
            "$temporary/$phase00_parity_case_id.official.stdout" stderr
        printf 'wrapper stdout (%s):\n' "$phase00_parity_case_id" >&2
        gate_replay_text_file \
            "$temporary/$phase00_parity_case_id.wrapper.stdout" stderr
        exit 1
    fi
    if ! gate_compare_files "$phase00_parity_case_id stderr parity" \
        "$temporary/$phase00_parity_case_id.official.stderr" \
        "$temporary/$phase00_parity_case_id.wrapper.stderr" \
        "$temporary/$phase00_parity_case_id.stderr.cmp" \
        "$temporary/$phase00_parity_case_id.stderr.cmp.err"; then
        printf 'official stderr (%s):\n' "$phase00_parity_case_id" >&2
        gate_replay_text_file \
            "$temporary/$phase00_parity_case_id.official.stderr" stderr
        printf 'wrapper stderr (%s):\n' "$phase00_parity_case_id" >&2
        gate_replay_text_file \
            "$temporary/$phase00_parity_case_id.wrapper.stderr" stderr
        exit 1
    fi
    printf '%s\t%s\t%s\t%s\n' \
        "$phase00_parity_case_id" "$phase00_parity_official_exit" \
        "$phase00_parity_wrapper_exit" EXACT_STDOUT_STDERR_EXIT \
        >>"$temporary/results.tsv"
    exact_pair_count=$((exact_pair_count + 1))
}

compare_source_failure_pair() {
    phase00_parity_case_id=$1
    phase00_parity_cwd=$2
    shift
    shift

    capture_launcher "$official_maven" "$phase00_parity_cwd" \
        "$temporary/$phase00_parity_case_id.official" "$@"
    phase00_parity_official_exit=$PHASE00_PARITY_CAPTURE_EXIT
    capture_launcher "$wrapper" "$phase00_parity_cwd" \
        "$temporary/$phase00_parity_case_id.wrapper" "$@"
    phase00_parity_wrapper_exit=$PHASE00_PARITY_CAPTURE_EXIT

    test "$phase00_parity_official_exit" -eq 0
    test "$phase00_parity_wrapper_exit" -eq 2
    test -s "$temporary/$phase00_parity_case_id.official.stderr"
    test -s "$temporary/$phase00_parity_case_id.wrapper.stderr"
    gate_text_file_contains \
        "$temporary/$phase00_parity_case_id.wrapper.stderr" \
        'tool=mavenrc-source result=NOT_EVALUATED exit=2'
    if gate_text_file_contains \
        "$temporary/$phase00_parity_case_id.wrapper.stdout" \
        'Apache Maven 3.9.14'; then
        printf 'Maven false-green after mavenrc source failure: %s\n' \
            "$phase00_parity_case_id" >&2
        exit 1
    fi
    printf '%s\t%s\t%s\t%s\n' \
        "$phase00_parity_case_id" "$phase00_parity_official_exit" \
        "$phase00_parity_wrapper_exit" SOURCE_ERROR_FAIL_CLOSED_NOT_EVALUATED \
        >>"$temporary/results.tsv"
    failure_only_pair_count=$((failure_only_pair_count + 1))
}

compare_fail_closed_enrichment_pair() {
    phase00_parity_case_id=$1
    phase00_parity_expected_exit=$2
    phase00_parity_expected_label=$3
    phase00_parity_cwd=$4
    shift
    shift
    shift
    shift

    capture_launcher "$official_maven" "$phase00_parity_cwd" \
        "$temporary/$phase00_parity_case_id.official" "$@"
    phase00_parity_official_exit=$PHASE00_PARITY_CAPTURE_EXIT
    capture_launcher "$wrapper" "$phase00_parity_cwd" \
        "$temporary/$phase00_parity_case_id.wrapper" "$@"
    phase00_parity_wrapper_exit=$PHASE00_PARITY_CAPTURE_EXIT

    test "$phase00_parity_official_exit" -eq "$phase00_parity_expected_exit"
    test "$phase00_parity_wrapper_exit" -eq "$phase00_parity_official_exit"
    gate_compare_files "$phase00_parity_case_id stdout parity" \
        "$temporary/$phase00_parity_case_id.official.stdout" \
        "$temporary/$phase00_parity_case_id.wrapper.stdout" \
        "$temporary/$phase00_parity_case_id.stdout.cmp" \
        "$temporary/$phase00_parity_case_id.stdout.cmp.err"
    cp "$temporary/$phase00_parity_case_id.official.stderr" \
        "$temporary/$phase00_parity_case_id.expected-wrapper.stderr"
    printf '%s\n' "$phase00_parity_expected_label" \
        >>"$temporary/$phase00_parity_case_id.expected-wrapper.stderr"
    gate_compare_files "$phase00_parity_case_id fail-closed stderr enrichment" \
        "$temporary/$phase00_parity_case_id.expected-wrapper.stderr" \
        "$temporary/$phase00_parity_case_id.wrapper.stderr" \
        "$temporary/$phase00_parity_case_id.stderr.cmp" \
        "$temporary/$phase00_parity_case_id.stderr.cmp.err"
    printf '%s\t%s\t%s\t%s\n' \
        "$phase00_parity_case_id" "$phase00_parity_official_exit" \
        "$phase00_parity_wrapper_exit" OFFICIAL_STDERR_PLUS_NOT_EVALUATED_LABEL \
        >>"$temporary/results.tsv"
    fail_closed_enrichment_pair_count=$((fail_closed_enrichment_pair_count + 1))
}

reset_case_environment
compare_exact_pair no-user-mavenrc 0 "$repository" --version

{
    printf '%s\n' \
        'MAVEN_OPTS=--phase00-review07-invalid-jvm-option' \
        'export MAVEN_OPTS'
} >"$temporary/home-user/.mavenrc"
reset_case_environment
case_home=$temporary/home-user
compare_exact_pair user-mavenrc-invalid-jvm-marker 1 "$repository" --version

reset_case_environment
case_home=$temporary/home-user
case_skip_rc=1
compare_exact_pair user-mavenrc-skip-rc-one 0 "$repository" --version

reset_case_environment
case_home=$temporary/home-user
case_skip_rc=0
compare_exact_pair user-mavenrc-skip-rc-nonempty 0 "$repository" --version

{
    printf '%s\n' \
        'MAVEN_OPTS=--phase00-review07-invalid-jvm-option' \
        'export MAVEN_OPTS' \
        'false'
} >"$temporary/home-source/.mavenrc"
reset_case_environment
case_home=$temporary/home-source
compare_exact_pair user-mavenrc-nonzero-last-command 1 "$repository" --version

{
    printf '%s\n' ')'
} >"$temporary/home-syntax/.mavenrc"
reset_case_environment
case_home=$temporary/home-syntax
compare_source_failure_pair user-mavenrc-syntax-error "$repository" --version

reset_case_environment
case_java_home=$temporary/no-such-java-home
compare_fail_closed_enrichment_pair java-home-invalid 1 \
    'tool=java result=NOT_EVALUATED exit=1' "$repository" --version

printf '%s\n' '--phase00-review07-invalid-jvm-option' \
    >"$temporary/work/project/.mvn/jvm.config"
: >"$temporary/work/project/pom.xml"
reset_case_environment
case_maven_basedir=$temporary/work/project
compare_exact_pair basedir-absolute-jvm-config 1 "$repository" --version

printf '%s\n' '-Dphase00.valid.basedir=true' \
    >"$temporary/work/valid-project/.mvn/jvm.config"
reset_case_environment
case_maven_basedir=$temporary/work/valid-project
compare_exact_pair basedir-absolute-valid 0 "$repository" --version

reset_case_environment
case_maven_basedir=$temporary/work/does-not-exist
compare_exact_pair basedir-nonexistent-officially-accepted 0 \
    "$repository" --version

printf '%s\n' '--phase00-review07-invalid-jvm-option' \
    >"$temporary/work/relative-project/.mvn/jvm.config"
reset_case_environment
case_maven_basedir=relative-project
compare_exact_pair basedir-relative-jvm-config 1 "$temporary/work" --version

printf '%s\n' 'not-a-directory' >"$temporary/work/basedir-file"
reset_case_environment
case_maven_basedir=$temporary/work/basedir-file
compare_exact_pair basedir-file-officially-accepted 0 "$repository" --version

reset_case_environment
compare_exact_pair basedir-cwd-fallback 1 \
    "$temporary/work/project/child" --version

reset_case_environment
compare_exact_pair basedir-file-switch-directory 1 \
    "$temporary/work" -f project --version

reset_case_environment
compare_exact_pair basedir-file-switch-pom 1 \
    "$temporary/work" -f project/pom.xml --version

reset_case_environment
compare_exact_pair basedir-file-switch-nonexistent 1 \
    "$temporary/work" -f no-such-pom.xml --version

printf '%s\n' '--phase00-review07-invalid-cli-option' \
    >"$temporary/work/maven-config/.mvn/maven.config"
reset_case_environment
case_maven_basedir=$temporary/work/maven-config
compare_exact_pair basedir-maven-config-selection 1 "$repository" --version

printf '%s\n' \
    '-Dphase00.jvm.first=one' \
    '-Dphase00.jvm.second=two' \
    >"$temporary/work/project-space/.mvn/jvm.config"
reset_case_environment
case_java_home=$temporary/java-home
case_maven_basedir=$temporary/work/project-space
case_maven_opts='-Dphase00.maven.opts=three'
case_maven_debug_opts='-Dphase00.maven.debug=four'
case_maven_args='-Dphase00.maven.args=five'
case_maven_config='-Dphase00.maven.config=six'
compare_exact_pair launch-vector-options-config-args-quoting 0 "$repository" \
    '-Dphase00.cli.space=alpha beta' '' '*' --version

{
    printf "JAVA_HOME='%s'\n" "$temporary/java-home"
    printf "MAVEN_BASEDIR='%s'\n" "$temporary/work/project-space"
    printf '%s\n' \
        "MAVEN_OPTS='-Dphase00.rc.maven.opts=seven'" \
        "MAVEN_ARGS='-Dphase00.rc.maven.args=eight'" \
        "MAVEN_CONFIG='-Dphase00.rc.maven.config=nine'" \
        'export JAVA_HOME MAVEN_BASEDIR MAVEN_OPTS MAVEN_ARGS MAVEN_CONFIG'
} >"$temporary/home-order/.mavenrc"
reset_case_environment
case_home=$temporary/home-order
compare_exact_pair user-mavenrc-variable-mutations 0 "$repository" \
    '-Dphase00.cli=ten' --version

reset_case_environment
case_java_home=$temporary/java-home
case_java_exit=37
compare_exact_pair final-java-status-propagation 37 "$repository" --version

{
    printf '%s\n' \
        'PHASE00_RC_TRACE=usr-local' \
        'export PHASE00_RC_TRACE'
} >"$temporary/usr-local.mavenrc"
{
    printf '%s\n' \
        'PHASE00_RC_TRACE="${PHASE00_RC_TRACE-},etc"' \
        'export PHASE00_RC_TRACE'
} >"$temporary/etc.mavenrc"
{
    printf '%s\n' \
        'PHASE00_RC_TRACE="${PHASE00_RC_TRACE-},user"' \
        'export PHASE00_RC_TRACE'
} >"$temporary/home-order/.mavenrc"

reset_case_environment
case_home=$temporary/home-order
case_java_home=$temporary/java-home
capture_private_launcher "$repository" "$temporary/system-order" \
    "$temporary/usr-local.mavenrc" "$temporary/etc.mavenrc" --version
test "$PHASE00_PARITY_CAPTURE_EXIT" -eq 0
gate_require_match 'system mavenrc controlled ordering seam' \
    "$temporary/system-order.match" "$temporary/system-order.match.err" \
    grep -Fx 'RC_TRACE=usr-local,etc,user' "$temporary/system-order.stdout"
printf '%s\t%s\t%s\t%s\n' \
    system-mavenrc-ordering-controlled-seam N/A 0 CONTROLLED_ORDER_SOURCE_SEMANTICS \
    >>"$temporary/results.tsv"
controlled_seam_count=$((controlled_seam_count + 1))

case_skip_rc=1
capture_private_launcher "$repository" "$temporary/system-order-skip" \
    "$temporary/usr-local.mavenrc" "$temporary/etc.mavenrc" --version
test "$PHASE00_PARITY_CAPTURE_EXIT" -eq 0
gate_require_match 'system mavenrc controlled skip seam' \
    "$temporary/system-order-skip.match" \
    "$temporary/system-order-skip.match.err" \
    grep -Fx 'RC_TRACE=' "$temporary/system-order-skip.stdout"
printf '%s\t%s\t%s\t%s\n' \
    system-mavenrc-skip-controlled-seam N/A 0 CONTROLLED_SKIP_SEMANTICS \
    >>"$temporary/results.tsv"
controlled_seam_count=$((controlled_seam_count + 1))

test "$exact_pair_count" -eq 18
test "$failure_only_pair_count" -eq 1
test "$fail_closed_enrichment_pair_count" -eq 1
test "$controlled_seam_count" -eq 2

{
    printf '%s\n' 'PHASE00_MAVEN_3_9_14_LAUNCHER_PARITY=PASS'
    printf '%s\n' \
        'OFFICIAL_MAVEN_SHA256=f9381d0cb98abaaf9592dae421eddc497e84ed9bfb723b84c111d1350863c3a2'
    printf 'EXACT_AB_PAIR_COUNT=%s\n' "$exact_pair_count"
    printf 'SOURCE_FAILURE_PAIR_COUNT=%s\n' "$failure_only_pair_count"
    printf 'FAIL_CLOSED_ENRICHMENT_PAIR_COUNT=%s\n' \
        "$fail_closed_enrichment_pair_count"
    printf 'CONTROLLED_SYSTEM_RC_SEAM_COUNT=%s\n' "$controlled_seam_count"
    printf '%s\n' 'FALSE_GREEN_COUNT=0'
    printf '%s\n' \
        'SYSTEM_RC_ENVIRONMENT_LIMITATION=/usr/local/etc/mavenrc_and_/etc/mavenrc_not_modified'
    printf '%s\n' \
        'SYSTEM_RC_FIXTURE_METHOD=PRIVATE_POSITIONAL_TEST_DRIVER'
    printf '%s\n' \
        'PUBLIC_MVNW_PRIVATE_DRIVER_SELECTION_PATH=NONE'
    printf '%s\n' \
        'EVIDENCE_GENERATION_AMBIENT_RC_POLICY=MAVEN_SKIP_RC=1_explicitly_recorded'
    printf '%s\n' 'case-id	official-exit	wrapper-exit	parity'
    gate_replay_text_file "$temporary/results.tsv"
} >"$temporary/report.txt"

if [ -n "$report" ]; then
    gate_publish_file_atomic 'maven-launcher-parity-report' \
        "$temporary/report.txt" "$report" "$temporary/report-publish"
else
    gate_replay_text_file "$temporary/report.txt"
fi
