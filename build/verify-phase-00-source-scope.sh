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
gate_make_temporary_directory 'mktemp(source-scope)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-scope.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

is_allowed_phase_path() {
    case "$1" in
        .dockerignore|Dockerfile|README.md|pom.xml|mvnw|mvnw.cmd|\
        .mvn/*|build/*|legacy/*|rpdptw/*|\
        src/main/java/com/ronext/optimizer/*|\
        src/test/java/com/ronext/optimizer/*)
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

is_preserved_concurrent_path() {
    case "$1" in
        docs/implementation/execution-progress-and-results.md|\
        docs/implementation/master-realization-plan.md|\
        docs/implementation/README.md|\
        docs/implementation/human-guides/*|\
        docs/codex/*)
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

gate_capture_checked 'git(source-scope-status)' \
    "$temporary/status.txt" "$temporary/status.err" \
    git -C "$repository" status --porcelain=v1 --untracked-files=all

allowed_count=0
preserved_count=0
unexpected_count=0
while IFS= read -r status_line; do
    test -n "$status_line" || continue
    path=${status_line#???}
    case "$path" in
        *' -> '*)
            path=${path##* -> }
            ;;
    esac
    if is_allowed_phase_path "$path"; then
        allowed_count=$((allowed_count + 1))
    elif is_preserved_concurrent_path "$path"; then
        preserved_count=$((preserved_count + 1))
    else
        printf '%s\n' "$status_line" >>"$temporary/unexpected.txt"
        unexpected_count=$((unexpected_count + 1))
    fi
done <"$temporary/status.txt"

if [ "$unexpected_count" -ne 0 ]; then
    printf '%s\n' 'unexpected Phase 00 source scope entries:' >&2
    gate_replay_text_file "$temporary/unexpected.txt" stderr
    printf '%s\n' \
        'tool=source-scope-path-classification result=EVALUATED_FAILURE exit=1' >&2
    exit 1
fi

set +e
gate_git_diff_check tracked 'git(diff-check-source-scope)' \
    "$temporary/diff-check.out" "$temporary/diff-check.err" \
    git -C "$repository" diff --check -- \
        .dockerignore Dockerfile README.md pom.xml .mvn build legacy mvnw mvnw.cmd rpdptw \
        src/main/java/com/ronext/optimizer src/test/java/com/ronext/optimizer
diff_check_status=$?
set -e
if [ "$diff_check_status" -ne 0 ]; then
    exit "$diff_check_status"
fi

untracked_count=0
gate_capture_checked 'git(source-scope-untracked-files)' \
    "$temporary/untracked-all.txt" "$temporary/untracked-all.err" \
    git -C "$repository" ls-files --others --exclude-standard
while IFS= read -r path; do
        if ! is_allowed_phase_path "$path"; then
            continue
        fi
        untracked_count=$((untracked_count + 1))
        set +e
        gate_git_diff_check no-index \
            "git(diff-no-index-check:$path)" \
            "$temporary/no-index-check.out" \
            "$temporary/no-index-check.err" \
            git -C "$repository" diff --no-index --check \
                /dev/null "$repository/$path"
        exit_code=$?
        set -e
        if [ "$exit_code" -ne 0 ]; then
            exit "$exit_code"
        fi
        printf '%s\n' "$path" >>"$temporary/untracked-paths.txt"
done <"$temporary/untracked-all.txt"

if [ -f "$temporary/untracked-paths.txt" ]; then
    gate_capture_checked 'awk(untracked-phase-file-count)' \
        "$temporary/untracked-count.txt" "$temporary/untracked-count.err" \
        awk 'END { print NR + 0 }' "$temporary/untracked-paths.txt"
    IFS= read -r untracked_count <"$temporary/untracked-count.txt" || :
else
    untracked_count=0
fi
printf '%s\n' 'PHASE00_SOURCE_SCOPE=PASS'
printf 'ALLOWED_STATUS_ENTRY_COUNT=%s\n' "$allowed_count"
printf 'PRESERVED_CONCURRENT_ENTRY_COUNT=%s\n' "$preserved_count"
printf 'UNEXPECTED_STATUS_ENTRY_COUNT=%s\n' "$unexpected_count"
printf 'UNTRACKED_PHASE_FILE_CHECK_COUNT=%s\n' "$untracked_count"
printf '%s\n' 'WHITESPACE_ERROR_COUNT=0'
printf '%s\n' 'SCHEDULER_AND_HUMAN_GUIDE_DISPOSITION=PRESERVED_EXCLUDED'
