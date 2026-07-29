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
gate_make_temporary_directory 'mktemp(git-whitespace-semantics)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-git-whitespace.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM
fixture="$temporary/repository"
mkdir -p "$fixture"
git -C "$fixture" init -q
git -C "$fixture" config user.name phase00-test
git -C "$fixture" config user.email phase00-test@example.invalid
printf '%s\n' clean >"$fixture/tracked.txt"
git -C "$fixture" add tracked.txt
git -C "$fixture" commit -qm initial

: >"$temporary/results.tsv"

run_check() {
    case_id=$1
    expected_exit=$2
    expected_result=$3
    mode=$4
    shift
    shift
    shift
    shift

    set +e
    (
        gate_git_diff_check "$mode" "git($case_id)" \
            "$temporary/$case_id.out" "$temporary/$case_id.err" "$@"
    ) >"$temporary/$case_id.log" 2>&1
    actual_exit=$?
    set -e
    test "$actual_exit" -eq "$expected_exit"
    case "$expected_result" in
        CLEAN)
            test ! -s "$temporary/$case_id.log"
            ;;
        EVALUATED_FAILURE|NOT_EVALUATED)
            gate_text_file_contains "$temporary/$case_id.log" \
                "result=$expected_result exit=$expected_exit"
            ;;
    esac
    printf '%s\t%s\t%s\t%s\n' \
        "$case_id" "$expected_exit" "$actual_exit" "$expected_result" \
        >>"$temporary/results.tsv"
}

run_check clean-success 0 CLEAN tracked \
    git -C "$fixture" diff --check -- tracked.txt

printf '%s \n' tracked-whitespace >"$fixture/tracked.txt"
run_check tracked-whitespace 2 EVALUATED_FAILURE tracked \
    git -C "$fixture" diff --check -- tracked.txt
git -C "$fixture" restore tracked.txt

printf '%s \n' untracked-whitespace >"$fixture/untracked-whitespace.txt"
run_check untracked-whitespace-no-index 3 EVALUATED_FAILURE no-index \
    git -C "$fixture" diff --no-index --check \
        /dev/null "$fixture/untracked-whitespace.txt"

printf '%s\n' ordinary-content >"$fixture/untracked-clean.txt"
run_check ordinary-content-no-index 0 CLEAN no-index \
    git -C "$fixture" diff --no-index --check \
        /dev/null "$fixture/untracked-clean.txt"

run_check malformed-invocation 129 NOT_EVALUATED tracked \
    git -C "$fixture" diff --definitely-not-a-real-option --check

mkdir -p "$temporary/fake-git"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'printf "%s\n" FAKE_GIT_SUCCESS'
    printf '%s\n' 'printf "%s\n" INJECTED_GIT_FAILURE >&2'
    printf '%s\n' 'exit 41'
} >"$temporary/fake-git/git"
chmod +x "$temporary/fake-git/git"
run_check fake-success-nonzero 41 NOT_EVALUATED tracked \
    env PATH="$temporary/fake-git:$PATH" git diff --check

gate_capture_checked 'awk(git-whitespace-case-count)' \
    "$temporary/count.out" "$temporary/count.err" \
    awk 'END { print NR + 0 }' "$temporary/results.tsv"
IFS= read -r case_count <"$temporary/count.out" || :
test "$case_count" -eq 6

{
    printf '%s\n' 'PHASE00_GIT_WHITESPACE_SEMANTICS_SELF_TEST=PASS'
    printf '%s\n' 'CASE_COUNT=6'
    printf '%s\n' 'TRACKED_WHITESPACE_RAW_EXIT=2'
    printf '%s\n' 'NO_INDEX_ORDINARY_DIFFERENCE_RAW_EXIT=1'
    printf '%s\n' 'NO_INDEX_WHITESPACE_RAW_EXIT=3'
    printf '%s\n' 'EXECUTION_ERROR_CASE_COUNT=2'
    printf '%s\n' 'FALSE_GREEN_COUNT=0'
    printf '%s\n' 'case-id	expected-exit	actual-exit	result'
    gate_replay_text_file "$temporary/results.tsv"
} >"$temporary/report.txt"

if [ -n "$report" ]; then
    gate_publish_file_atomic 'git-whitespace-semantics-report' \
        "$temporary/report.txt" "$report" "$temporary/report-publish"
else
    gate_replay_text_file "$temporary/report.txt"
fi
