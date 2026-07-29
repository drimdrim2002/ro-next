#!/bin/sh

# POSIX-sh helpers for gate-critical detectors and filesystem traversal.
# Callers use `set -eu`; every helper preserves detector/traversal exit status
# without relying on pipeline or conditional-command semantics.

gate_replay_text_file() {
    gate_replay_path=$1
    gate_replay_destination=${2:-stdout}

    while IFS= read -r gate_replay_line || [ -n "$gate_replay_line" ]; do
        if [ "$gate_replay_destination" = stderr ]; then
            printf '%s\n' "$gate_replay_line" >&2
        else
            printf '%s\n' "$gate_replay_line"
        fi
    done <"$gate_replay_path"
}

gate_text_file_contains() {
    gate_contains_path=$1
    gate_contains_needle=$2

    while IFS= read -r gate_contains_line || [ -n "$gate_contains_line" ]; do
        case "$gate_contains_line" in
            *"$gate_contains_needle"*) return 0 ;;
        esac
    done <"$gate_contains_path"
    return 1
}

gate_passthrough_external() {
    gate_passthrough_label=$1
    gate_passthrough_command=$2
    shift
    shift

    if command "$gate_passthrough_command" "$@"; then
        return 0
    else
        gate_passthrough_status=$?
        printf 'tool=%s result=NOT_EVALUATED exit=%s\n' \
            "$gate_passthrough_label" "$gate_passthrough_status" >&2
        return "$gate_passthrough_status"
    fi
}

gate_silent_external() {
    gate_silent_label=$1
    gate_silent_command=$2
    shift
    shift

    if command "$gate_silent_command" "$@" >/dev/null; then
        return 0
    else
        gate_silent_status=$?
        printf 'tool=%s result=NOT_EVALUATED exit=%s\n' \
            "$gate_silent_label" "$gate_silent_status" >&2
        return "$gate_silent_status"
    fi
}

# These commands have no semantic non-zero result in Phase 00. Defining thin
# shell front doors makes direct, command-substitution, fallback, and
# redirection call sites preserve the failing executable's exact status and a
# stable NOT_EVALUATED diagnostic. grep/rg/cmp and the few git comparisons are
# intentionally excluded because their exit 1 has an evaluated meaning and is
# handled by the tri-state helpers/call sites below.
awk() { gate_passthrough_external awk awk "$@"; }
basename() { gate_passthrough_external basename basename "$@"; }
cat() { gate_passthrough_external cat cat "$@"; }
chmod() { gate_silent_external chmod chmod "$@"; }
cp() { gate_silent_external cp cp "$@"; }
date() { gate_passthrough_external date date "$@"; }
dirname() { gate_passthrough_external dirname dirname "$@"; }
env() {
    if command env "$@"; then
        return 0
    else
        gate_env_status=$?
        case "$gate_env_status" in
            125|126|127)
                printf 'tool=env result=NOT_EVALUATED exit=%s\n' \
                    "$gate_env_status" >&2
                ;;
        esac
        return "$gate_env_status"
    fi
}
find() { gate_passthrough_external find find "$@"; }
jar() { gate_passthrough_external jar jar "$@"; }
java() { gate_passthrough_external java java "$@"; }
jdeps() { gate_passthrough_external jdeps jdeps "$@"; }
ln() { gate_silent_external ln ln "$@"; }
ls() { gate_passthrough_external ls ls "$@"; }
mkdir() { gate_silent_external mkdir mkdir "$@"; }
mkfifo() { gate_silent_external mkfifo mkfifo "$@"; }
mv() { gate_silent_external mv mv "$@"; }
readlink() { gate_passthrough_external readlink readlink "$@"; }
rm() { gate_silent_external rm rm "$@"; }
sed() { gate_passthrough_external sed sed "$@"; }
shasum() { gate_passthrough_external shasum shasum "$@"; }
sort() { gate_passthrough_external sort sort "$@"; }
tar() { gate_silent_external tar tar "$@"; }
tr() { gate_passthrough_external tr tr "$@"; }
unzip() { gate_passthrough_external unzip unzip "$@"; }
wc() { gate_passthrough_external wc wc "$@"; }

gate_make_temporary_directory() {
    gate_temporary_label=$1
    gate_temporary_template=$2

    if GATE_TEMPORARY_DIRECTORY=$(command mktemp -d "$gate_temporary_template"); then
        test -n "$GATE_TEMPORARY_DIRECTORY"
        return 0
    else
        gate_temporary_status=$?
        printf 'tool=%s result=NOT_EVALUATED exit=%s\n' \
            "$gate_temporary_label" "$gate_temporary_status" >&2
        return "$gate_temporary_status"
    fi
}

gate_publish_file_atomic() {
    gate_publish_label=$1
    gate_publish_source=$2
    gate_publish_target=$3
    gate_publish_prefix=$4
    case "$gate_publish_target" in
        */*) gate_publish_parent=${gate_publish_target%/*} ;;
        *) gate_publish_parent=. ;;
    esac
    gate_publish_basename=${gate_publish_target##*/}
    gate_publish_stage="$gate_publish_parent/.$gate_publish_basename.publishing.$$"

    if [ -e "$gate_publish_stage" ] || [ -L "$gate_publish_stage" ]; then
        gate_capture_checked "rm(stale-publish-stage:$gate_publish_label)" \
            "$gate_publish_prefix.stage-remove.out" \
            "$gate_publish_prefix.stage-remove.err" \
            rm -f -- "$gate_publish_stage"
    fi
    gate_capture_checked "cp(stage:$gate_publish_label)" \
        "$gate_publish_prefix.stage-copy.out" \
        "$gate_publish_prefix.stage-copy.err" \
        cp "$gate_publish_source" "$gate_publish_stage"
    gate_capture_checked "mv(publish:$gate_publish_label)" \
        "$gate_publish_prefix.publish.out" \
        "$gate_publish_prefix.publish.err" \
        mv "$gate_publish_stage" "$gate_publish_target"
}

gate_capture_status() {
    gate_stdout=$1
    gate_stderr=$2
    shift
    shift

    set +e
    "$@" >"$gate_stdout" 2>"$gate_stderr"
    GATE_STATUS=$?
    set -e
}

gate_report_not_evaluated() {
    gate_report_tool=$1
    gate_report_exit=$2
    gate_report_stderr=$3

    if [ -s "$gate_report_stderr" ]; then
        gate_replay_text_file "$gate_report_stderr" stderr
    fi
    printf 'tool=%s result=NOT_EVALUATED exit=%s\n' \
        "$gate_report_tool" "$gate_report_exit" >&2
}

gate_report_evaluated_failure() {
    gate_evaluated_tool=$1
    gate_evaluated_exit=$2
    gate_evaluated_stderr=$3

    if [ -s "$gate_evaluated_stderr" ]; then
        gate_replay_text_file "$gate_evaluated_stderr" stderr
    fi
    printf 'tool=%s result=EVALUATED_FAILURE exit=%s\n' \
        "$gate_evaluated_tool" "$gate_evaluated_exit" >&2
}

gate_git_check_output_has_violation() {
    gate_git_violation_path=$1
    while IFS= read -r gate_git_violation_line ||
        [ -n "$gate_git_violation_line" ]; do
        case "$gate_git_violation_line" in
            *': trailing whitespace.'|\
            *': space before tab in indent.'|\
            *': new blank line at EOF.'|\
            *': blank line at EOF.'|\
            *': leftover conflict marker')
                return 0
                ;;
        esac
    done <"$gate_git_violation_path"
    return 1
}

# Git's --check status is a bit mask in the command shapes used by Phase 00:
# tracked whitespace violations are 2; --no-index adds the ordinary-difference
# bit, so difference+whitespace is 3.  A no-index status 1 is therefore an
# evaluated ordinary content difference, not a whitespace failure.  Known
# status alone is insufficient: recognizable --check diagnostics must also be
# present, preventing arbitrary/fake-success nonzero exits from being promoted
# to semantic results.
gate_git_diff_check() {
    gate_git_check_mode=$1
    gate_git_check_label=$2
    gate_git_check_stdout=$3
    gate_git_check_stderr=$4
    shift
    shift
    shift
    shift

    gate_capture_status "$gate_git_check_stdout" "$gate_git_check_stderr" "$@"
    gate_git_check_status=$GATE_STATUS
    GATE_GIT_DIFF_CHECK_RAW_STATUS=$gate_git_check_status

    case "$gate_git_check_mode:$gate_git_check_status" in
        tracked:0|no-index:0|no-index:1)
            return 0
            ;;
        tracked:2|no-index:2|no-index:3)
            if gate_git_check_output_has_violation "$gate_git_check_stdout" ||
                gate_git_check_output_has_violation "$gate_git_check_stderr"; then
                if [ -s "$gate_git_check_stdout" ]; then
                    gate_replay_text_file "$gate_git_check_stdout" stderr
                fi
                gate_report_evaluated_failure \
                    "$gate_git_check_label" "$gate_git_check_status" \
                    "$gate_git_check_stderr"
                return "$gate_git_check_status"
            fi
            ;;
    esac

    if [ -s "$gate_git_check_stdout" ]; then
        gate_replay_text_file "$gate_git_check_stdout" stderr
    fi
    gate_report_not_evaluated \
        "$gate_git_check_label" "$gate_git_check_status" \
        "$gate_git_check_stderr"
    return "$gate_git_check_status"
}

gate_capture_checked() {
    gate_checked_tool=$1
    gate_checked_stdout=$2
    gate_checked_stderr=$3
    shift
    shift
    shift

    gate_capture_status "$gate_checked_stdout" "$gate_checked_stderr" "$@"
    gate_checked_status=$GATE_STATUS
    if [ "$gate_checked_status" -ne 0 ]; then
        gate_report_not_evaluated \
            "$gate_checked_tool" "$gate_checked_status" "$gate_checked_stderr"
        return "$gate_checked_status"
    fi
}

gate_require_no_match() {
    gate_label=$1
    gate_stdout=$2
    gate_stderr=$3
    shift
    shift
    shift

    gate_capture_status "$gate_stdout" "$gate_stderr" "$@"
    case "$GATE_STATUS" in
        0)
            printf 'gate-critical detector matched forbidden content: %s\n' \
                "$gate_label" >&2
            printf 'tool=%s result=EVALUATED_FAILURE exit=1\n' \
                "$gate_label" >&2
            return 1
            ;;
        1)
            return 0
            ;;
        *)
            gate_detector_status=$GATE_STATUS
            gate_report_not_evaluated \
                "$gate_label" "$gate_detector_status" "$gate_stderr"
            return "$gate_detector_status"
            ;;
    esac
}

gate_require_match() {
    gate_label=$1
    gate_stdout=$2
    gate_stderr=$3
    shift
    shift
    shift

    gate_capture_status "$gate_stdout" "$gate_stderr" "$@"
    case "$GATE_STATUS" in
        0)
            return 0
            ;;
        1)
            printf 'gate-critical detector did not find required content: %s\n' \
                "$gate_label" >&2
            printf 'tool=%s result=EVALUATED_FAILURE exit=1\n' \
                "$gate_label" >&2
            return 1
            ;;
        *)
            gate_detector_status=$GATE_STATUS
            gate_report_not_evaluated \
                "$gate_label" "$gate_detector_status" "$gate_stderr"
            return "$gate_detector_status"
            ;;
    esac
}

gate_stage_find() {
    gate_root=$1
    gate_output=$2
    gate_error=$3
    shift
    shift
    shift

    set +e
    (
        cd "$gate_root" || exit 1
        find "$@"
    ) >"$gate_output" 2>"$gate_error"
    GATE_STATUS=$?
    set -e

    if [ "$GATE_STATUS" -ne 0 ]; then
        gate_find_status=$GATE_STATUS
        gate_report_not_evaluated \
            "find(tree:$gate_root)" "$gate_find_status" "$gate_error"
        return "$gate_find_status"
    fi
}

gate_sort_file() {
    gate_input=$1
    gate_output=$2
    gate_error=$3

    LC_ALL=C gate_capture_checked \
        "sort($gate_input)" "$gate_output" "$gate_error" \
        sort "$gate_input"
}

gate_sort_unique_file() {
    gate_input=$1
    gate_output=$2
    gate_error=$3

    LC_ALL=C gate_capture_checked "sort-unique($gate_input)" \
        "$gate_output" "$gate_error" sort -u "$gate_input"
}

gate_compare_files() {
    gate_compare_label=$1
    gate_compare_left=$2
    gate_compare_right=$3
    gate_compare_stdout=$4
    gate_compare_stderr=$5

    gate_capture_status "$gate_compare_stdout" "$gate_compare_stderr" \
        cmp "$gate_compare_left" "$gate_compare_right"
    gate_compare_status=$GATE_STATUS
    case "$gate_compare_status" in
        0)
            return 0
            ;;
        1)
            if [ -s "$gate_compare_stderr" ]; then
                gate_replay_text_file "$gate_compare_stderr" stderr
            fi
            printf 'gate-critical comparison mismatch: %s\n' \
                "$gate_compare_label" >&2
            printf 'tool=cmp(%s) result=EVALUATED_FAILURE exit=1\n' \
                "$gate_compare_label" >&2
            return 1
            ;;
        *)
            gate_report_not_evaluated \
                "cmp($gate_compare_label)" "$gate_compare_status" \
                "$gate_compare_stderr"
            return "$gate_compare_status"
            ;;
    esac
}

gate_normalize_relative_path() {
    gate_path=$1
    case "$gate_path" in
        .)
            GATE_RELATIVE_PATH=.
            return 0
            ;;
        ./*)
            gate_relative=${gate_path#./}
            ;;
        *)
            gate_relative=$gate_path
            ;;
    esac

    gate_tab=$(printf '\t')
    case "$gate_relative" in
        ''|/*|../*|*/../*|*/..|*//*|*"$gate_tab"*)
            printf 'non-canonical traversal path rejected: %s\n' \
                "$gate_path" >&2
            return 1
            ;;
    esac
    GATE_RELATIVE_PATH=$gate_relative
}

gate_stat_mode() {
    gate_path=$1
    gate_prefix=$2

    LC_ALL=C gate_capture_checked "ls(metadata:$gate_path)" \
        "$gate_prefix.ls.txt" "$gate_prefix.ls.err" \
        ls -ldn "$gate_path"

    gate_capture_checked "awk(metadata-mode:$gate_path)" \
        "$gate_prefix.mode.txt" "$gate_prefix.mode.err" \
        awk 'NR == 1 { print substr($1, 1, 10) }' "$gate_prefix.ls.txt"
    IFS= read -r GATE_MODE <"$gate_prefix.mode.txt" || :
    case "$GATE_MODE" in
        ??????????)
            ;;
        *)
            printf 'filesystem metadata mode is not canonical: %s mode=%s\n' \
                "$gate_path" "$GATE_MODE" >&2
            return 1
            ;;
    esac
}

gate_assert_declared_readable() {
    gate_path=$1
    gate_kind=$2
    gate_prefix=$3

    gate_stat_mode "$gate_path" "$gate_prefix"
    case "$GATE_MODE" in
        ?r????????|????r?????|???????r??)
            ;;
        *)
            printf 'filesystem entry has no declared read permission: %s mode=%s\n' \
                "$gate_path" "$GATE_MODE" >&2
            return 1
            ;;
    esac
    if [ "$gate_kind" = D ]; then
        case "$GATE_MODE" in
            ???x??????|??????x???|?????????x)
                ;;
            *)
                printf 'directory has no declared search permission: %s mode=%s\n' \
                    "$gate_path" "$GATE_MODE" >&2
                return 1
                ;;
        esac
    fi
}

gate_sha256_file() {
    gate_path=$1
    gate_prefix=$2

    gate_capture_checked "shasum(sha256:$gate_path)" \
        "$gate_prefix.sha256.txt" "$gate_prefix.sha256.err" \
        shasum -a 256 "$gate_path"
    gate_capture_checked "awk(sha256-output:$gate_path)" \
        "$gate_prefix.sha256.value" "$gate_prefix.sha256.parse.err" \
        awk 'NR == 1 && NF >= 1 { print $1 } NR > 1 { exit 2 }' \
        "$gate_prefix.sha256.txt"
    IFS= read -r GATE_SHA256 <"$gate_prefix.sha256.value" || :
    case "$GATE_SHA256" in
        *[!0-9a-f]*|'')
            printf 'file hash is not canonical lowercase SHA-256: %s\n' \
                "$gate_path" >&2
            return 1
            ;;
    esac
    if [ "${#GATE_SHA256}" -ne 64 ]; then
        printf 'file hash is not canonical lowercase SHA-256: %s\n' \
            "$gate_path" >&2
        return 1
    fi
}

gate_file_size() {
    gate_path=$1
    gate_prefix=$2

    gate_capture_checked "wc(file-bytes:$gate_path)" \
        "$gate_prefix.bytes.txt" "$gate_prefix.bytes.err" \
        wc -c "$gate_path"
    gate_capture_checked "awk(file-bytes:$gate_path)" \
        "$gate_prefix.bytes.value" "$gate_prefix.bytes.parse.err" \
        awk 'NR == 1 && NF >= 1 && $1 ~ /^[0-9]+$/ { print $1; ok=1 }
             END { if (!ok) exit 2 }' "$gate_prefix.bytes.txt"
    IFS= read -r GATE_FILE_SIZE <"$gate_prefix.bytes.value" || :
}

gate_classify_paths() {
    gate_root=$1
    gate_paths=$2
    gate_types=$3
    gate_files=$4
    gate_scratch=$5

    : >"$gate_types"
    : >"$gate_files"
    gate_index=0
    while IFS= read -r gate_path; do
        gate_normalize_relative_path "$gate_path"
        gate_relative=$GATE_RELATIVE_PATH
        if [ "$gate_relative" = . ]; then
            gate_full=$gate_root
        else
            gate_full=$gate_root/$gate_relative
        fi
        gate_index=$((gate_index + 1))
        gate_entry_prefix="$gate_scratch/entry-$gate_index"

        if [ -L "$gate_full" ]; then
            gate_stat_mode "$gate_full" "$gate_entry_prefix"
            printf 'L\t%s\t%s\n' "$gate_relative" "$GATE_MODE" \
                >>"$gate_types"
        elif [ -d "$gate_full" ]; then
            gate_assert_declared_readable \
                "$gate_full" D "$gate_entry_prefix"
            printf 'D\t%s\t%s\n' "$gate_relative" "$GATE_MODE" \
                >>"$gate_types"
        elif [ -f "$gate_full" ]; then
            gate_assert_declared_readable \
                "$gate_full" F "$gate_entry_prefix"
            printf 'F\t%s\t%s\n' "$gate_relative" "$GATE_MODE" \
                >>"$gate_types"
            printf '%s\n' "$gate_relative" >>"$gate_files"
        else
            gate_stat_mode "$gate_full" "$gate_entry_prefix"
            printf 'O\t%s\t%s\n' "$gate_relative" "$GATE_MODE" \
                >>"$gate_types"
        fi
    done <"$gate_paths"
}

gate_hash_file_list() {
    gate_root=$1
    gate_files=$2
    gate_manifest=$3
    gate_scratch=$4

    : >"$gate_manifest"
    gate_index=0
    while IFS= read -r gate_relative; do
        [ -n "$gate_relative" ] || continue
        gate_normalize_relative_path "$gate_relative"
        gate_relative=$GATE_RELATIVE_PATH
        gate_full=$gate_root/$gate_relative
        if [ -L "$gate_full" ] || [ ! -f "$gate_full" ]; then
            printf 'regular payload changed type during hashing: %s\n' \
                "$gate_relative" >&2
            return 1
        fi
        gate_index=$((gate_index + 1))
        gate_file_prefix="$gate_scratch/file-$gate_index"
        gate_assert_declared_readable \
            "$gate_full" F "$gate_file_prefix"
        gate_mode=$GATE_MODE
        gate_file_size "$gate_full" "$gate_file_prefix"
        gate_size=$GATE_FILE_SIZE
        gate_sha256_file "$gate_full" "$gate_file_prefix"
        printf '%s\t%s\t%s\t%s\n' \
            "$gate_relative" "$gate_mode" "$gate_size" "$GATE_SHA256" \
            >>"$gate_manifest"
    done <"$gate_files"
}

gate_inventory_tree() {
    gti_root=$1
    gti_prefix=$2
    gti_work=${gti_prefix%/*}
    mkdir -p "$gti_work" "$gti_prefix.scratch"

    gate_stage_find "$gti_root" "$gti_prefix.find.raw" \
        "$gti_prefix.find.err" . -print
    gate_sort_file "$gti_prefix.find.raw" "$gti_prefix.paths" \
        "$gti_prefix.sort.err"
    gate_classify_paths "$gti_root" "$gti_prefix.paths" \
        "$gti_prefix.types.tsv" "$gti_prefix.files.txt" \
        "$gti_prefix.scratch"

    gate_require_no_match \
        'filesystem tree symlink predicate' \
        "$gti_prefix.symlink.matches" "$gti_prefix.symlink.detector.err" \
        grep '^L	' "$gti_prefix.types.tsv"
    gate_require_no_match \
        'filesystem tree non-regular predicate' \
        "$gti_prefix.other.matches" "$gti_prefix.other.detector.err" \
        grep '^O	' "$gti_prefix.types.tsv"

    gate_hash_file_list "$gti_root" "$gti_prefix.files.txt" \
        "$gti_prefix.files.tsv" "$gti_prefix.scratch"
    gate_capture_checked "cp(tree-type-snapshot:$gate_root)" \
        "$gti_prefix.snapshot.copy.out" \
        "$gti_prefix.snapshot.copy.err" \
        cp "$gti_prefix.types.tsv" "$gti_prefix.snapshot.tsv"
    printf '%s\n' '--FILE-CONTENT--' >>"$gti_prefix.snapshot.tsv"
    gate_replay_text_file "$gti_prefix.files.tsv" \
        >>"$gti_prefix.snapshot.tsv"
    gate_sha256_file "$gti_prefix.snapshot.tsv" "$gti_prefix.snapshot"

    GATE_TREE_DIGEST=$GATE_SHA256
    GATE_TREE_FILE_MANIFEST=$gti_prefix.files.tsv
    GATE_TREE_FILE_LIST=$gti_prefix.files.txt
    GATE_TREE_TYPE_MANIFEST=$gti_prefix.types.tsv
    GATE_TREE_SNAPSHOT=$gti_prefix.snapshot.tsv
}
