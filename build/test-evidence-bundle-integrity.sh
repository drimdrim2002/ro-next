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
if [ -n "$report" ] && [ -f "$report" ] && [ ! -L "$report" ]; then
    : >"$report"
fi
gate_make_temporary_directory 'mktemp(evidence-integrity-test)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-evidence-test.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM
real_find=$(command -v find)
if [ -n "$report" ] && { [ -e "$report" ] || [ -L "$report" ]; }; then
    if [ -f "$report" ] && [ ! -L "$report" ]; then
        : >"$report"
    fi
    gate_capture_checked 'rm(stale-evidence-integrity-self-test-report)' \
        "$temporary/stale-report-remove.out" \
        "$temporary/stale-report-remove.err" \
        rm -f -- "$report"
fi

tree_digest() {
    directory=$1
    digest_id=$2
    set +e
    (
        cd "$directory" || exit 1
        "$real_find" . -print
    ) >"$temporary/$digest_id.find.raw" \
        2>"$temporary/$digest_id.find.err"
    find_exit=$?
    set -e
    if [ "$find_exit" -ne 0 ]; then
        gate_report_not_evaluated "find(integrity-reference-tree:$directory)" \
            "$find_exit" "$temporary/$digest_id.find.err"
        return "$find_exit"
    fi
    gate_sort_file "$temporary/$digest_id.find.raw" \
        "$temporary/$digest_id.paths" "$temporary/$digest_id.sort.err"

    : >"$temporary/$digest_id.snapshot"
    index=0
    while IFS= read -r path; do
        gate_normalize_relative_path "$path"
        relative=$GATE_RELATIVE_PATH
        if [ "$relative" = . ]; then
            full=$directory
        else
            full=$directory/$relative
        fi
        index=$((index + 1))
        prefix="$temporary/$digest_id-entry-$index"
        gate_stat_mode "$full" "$prefix"
        mode=$GATE_MODE
        if [ -L "$full" ]; then
            link_target=$(readlink "$full")
            printf 'L\t%s\t%s\t%s\n' \
                "$relative" "$mode" "$link_target" \
                >>"$temporary/$digest_id.snapshot"
        elif [ -d "$full" ]; then
            printf 'D\t%s\t%s\n' "$relative" "$mode" \
                >>"$temporary/$digest_id.snapshot"
        elif [ -f "$full" ]; then
            gate_file_size "$full" "$prefix"
            size=$GATE_FILE_SIZE
            gate_sha256_file "$full" "$prefix"
            printf 'F\t%s\t%s\t%s\t%s\n' \
                "$relative" "$mode" "$size" "$GATE_SHA256" \
                >>"$temporary/$digest_id.snapshot"
        else
            printf 'O\t%s\t%s\n' "$relative" "$mode" \
                >>"$temporary/$digest_id.snapshot"
        fi
    done <"$temporary/$digest_id.paths"
    gate_sha256_file "$temporary/$digest_id.snapshot" \
        "$temporary/$digest_id-tree"
    printf '%s\n' "$GATE_SHA256"
}

expected_failure() {
    case_id=$1
    directory=$2
    before=$(tree_digest "$directory" "$case_id-before")
    set +e
    "$repository/build/verify-evidence-bundle.sh" "$directory" \
        >"$temporary/$case_id.log" 2>&1
    exit_code=$?
    set -e
    after=$(tree_digest "$directory" "$case_id-after")
    test "$exit_code" -ne 0
    test "$before" = "$after"
    printf '%s\t%s\t%s\t%s\n' "$case_id" "$exit_code" "$before" "$after" \
        >>"$temporary/results.tsv"
}

expected_seal_failure() {
    case_id=$1
    directory=$2
    shift
    shift
    set +e
    "$@" >"$temporary/$case_id.log" 2>&1
    exit_code=$?
    set -e
    test "$exit_code" -ne 0
    test ! -e "$directory/evidence-manifest.tsv"
    test ! -e "$directory/evidence-manifest.sha256"
    printf '%s\t%s\t%s\t%s\n' \
        "$case_id" "$exit_code" 'NO_PUBLIC_SEAL' 'NO_PUBLIC_SEAL' \
        >>"$temporary/results.tsv"
}

mkdir -p "$temporary/sealed/nested"
printf '%s\n' 'phase-00-integrity-fixture' >"$temporary/sealed/payload.txt"
printf '%s\n' 'nested-payload' >"$temporary/sealed/nested/payload.txt"
"$repository/build/seal-evidence-bundle.sh" "$temporary/sealed" \
    >"$temporary/seal.log"

success_before=$(tree_digest "$temporary/sealed" success-before)
"$repository/build/verify-evidence-bundle.sh" "$temporary/sealed" \
    >"$temporary/verify.log"
success_after=$(tree_digest "$temporary/sealed" success-after)
test "$success_before" = "$success_after"
printf '%s\t%s\t%s\t%s\n' \
    'sealed-read-only-success' '0' "$success_before" "$success_after" \
    >"$temporary/results.tsv"

cp -R "$temporary/sealed" "$temporary/manifest-missing"
rm "$temporary/manifest-missing/evidence-manifest.tsv"
expected_failure 'manifest-only-missing' "$temporary/manifest-missing"

cp -R "$temporary/sealed" "$temporary/digest-missing"
rm "$temporary/digest-missing/evidence-manifest.sha256"
expected_failure 'digest-only-missing' "$temporary/digest-missing"

cp -R "$temporary/sealed" "$temporary/both-missing"
rm "$temporary/both-missing/evidence-manifest.tsv"
rm "$temporary/both-missing/evidence-manifest.sha256"
expected_failure 'both-seal-files-missing' "$temporary/both-missing"

cp -R "$temporary/sealed" "$temporary/interrupted-seal"
printf '%s\n' 'partial-digest-write' \
    >"$temporary/interrupted-seal/evidence-manifest.sha256"
expected_failure 'interrupted-seal' "$temporary/interrupted-seal"

cp -R "$temporary/sealed" "$temporary/payload-missing"
rm "$temporary/payload-missing/nested/payload.txt"
expected_failure 'payload-missing' "$temporary/payload-missing"

cp -R "$temporary/sealed" "$temporary/payload-mutated"
printf '%s\n' 'mutation' >>"$temporary/payload-mutated/payload.txt"
expected_failure 'payload-mutation' "$temporary/payload-mutated"

cp -R "$temporary/sealed" "$temporary/symlink"
ln -s payload.txt "$temporary/symlink/forbidden-link"
expected_failure 'symlink' "$temporary/symlink"

cp -R "$temporary/sealed" "$temporary/path-traversal"
sed '1s#^[^\t]*#../outside#' \
    "$temporary/path-traversal/evidence-manifest.tsv" \
    >"$temporary/path-traversal/evidence-manifest.tsv.new"
mv "$temporary/path-traversal/evidence-manifest.tsv.new" \
    "$temporary/path-traversal/evidence-manifest.tsv"
gate_sha256_file \
    "$temporary/path-traversal/evidence-manifest.tsv" \
    "$temporary/path-traversal-manifest"
printf '%s\n' "$GATE_SHA256" \
    >"$temporary/path-traversal/evidence-manifest.sha256"
expected_failure 'path-traversal' "$temporary/path-traversal"

cp -R "$temporary/sealed" "$temporary/hidden-payload-omission"
mkdir -p "$temporary/hidden-payload-omission/hidden"
printf '%s\n' 'payload omitted from the forged seal' \
    >"$temporary/hidden-payload-omission/hidden/payload.txt"
expected_failure 'hidden-payload-omission' \
    "$temporary/hidden-payload-omission"

mkdir -p "$temporary/unreadable-subtree/hidden"
printf '%s\n' 'hidden subtree payload' \
    >"$temporary/unreadable-subtree/hidden/payload.txt"
chmod 000 "$temporary/unreadable-subtree/hidden"
expected_seal_failure 'unreadable-subtree' \
    "$temporary/unreadable-subtree" \
    "$repository/build/seal-evidence-bundle.sh" \
    "$temporary/unreadable-subtree"
chmod 700 "$temporary/unreadable-subtree/hidden"

mkdir -p "$temporary/unreadable-file"
printf '%s\n' 'unreadable payload' \
    >"$temporary/unreadable-file/payload.txt"
chmod 000 "$temporary/unreadable-file/payload.txt"
expected_seal_failure 'unreadable-file' \
    "$temporary/unreadable-file" \
    "$repository/build/seal-evidence-bundle.sh" \
    "$temporary/unreadable-file"
chmod 600 "$temporary/unreadable-file/payload.txt"

mkdir -p "$temporary/find-exit-shim"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' "printf '%s\\n' '.' './payload.txt'"
    printf '%s\n' 'exit "${PHASE00_FAKE_FIND_EXIT:-7}"'
} >"$temporary/find-exit-shim/find"
chmod +x "$temporary/find-exit-shim/find"

mkdir -p "$temporary/forced-find-seal/hidden"
printf '%s\n' 'visible payload' >"$temporary/forced-find-seal/payload.txt"
printf '%s\n' 'hidden partial-traversal payload' \
    >"$temporary/forced-find-seal/hidden/payload.txt"
expected_seal_failure 'forced-find-exit-seal' \
    "$temporary/forced-find-seal" \
    env PHASE00_FAKE_FIND_EXIT=7 \
    PATH="$temporary/find-exit-shim:$PATH" \
    "$repository/build/seal-evidence-bundle.sh" \
    "$temporary/forced-find-seal"

forced_verify_before=$(tree_digest "$temporary/sealed" forced-verify-before)
set +e
PHASE00_FAKE_FIND_EXIT=8 \
    PATH="$temporary/find-exit-shim:$PATH" \
    "$repository/build/verify-evidence-bundle.sh" "$temporary/sealed" \
    >"$temporary/forced-find-exit-verify.log" 2>&1
forced_verify_exit=$?
set -e
forced_verify_after=$(tree_digest "$temporary/sealed" forced-verify-after)
test "$forced_verify_exit" -eq 8
test "$forced_verify_before" = "$forced_verify_after"
gate_require_no_match \
    'forced-find verifier PASS-report absence' \
    "$temporary/forced-find-pass.matches" \
    "$temporary/forced-find-pass.detector.err" \
    grep -F 'PHASE00_EVIDENCE_BUNDLE=PASS' \
    "$temporary/forced-find-exit-verify.log"
printf '%s\t%s\t%s\t%s\n' \
    'forced-find-exit-verify' "$forced_verify_exit" \
    "$forced_verify_before" "$forced_verify_after" \
    >>"$temporary/results.tsv"

gate_capture_checked 'awk(evidence-integrity-case-count)' \
    "$temporary/case-count.txt" "$temporary/case-count.err" \
    awk 'END { print NR + 0 }' "$temporary/results.tsv"
IFS= read -r case_count <"$temporary/case-count.txt" || :
test "$case_count" -eq 14

{
    printf '%s\n' 'PHASE00_EVIDENCE_INTEGRITY_SELF_TEST=PASS'
    printf '%s\n' 'VERIFIER_MODE=READ_ONLY'
    printf 'CASE_COUNT=%s\n' "$case_count"
    printf '%s\n' 'EXPECTED_FAILURE_CASE_COUNT=13'
    printf '%s\n' 'FORCED_FIND_NONZERO_CASE_COUNT=2'
    printf '%s\n' 'UNREADABLE_ENTRY_CASE_COUNT=2'
    printf '%s\n' 'HIDDEN_PAYLOAD_OMISSION_CASE_COUNT=1'
    printf '%s\n' 'TREE_MUTATION_COUNT=0'
    printf '%s\n' 'case-id	exit-code	tree-before-sha256	tree-after-sha256'
    gate_replay_text_file "$temporary/results.tsv"
} >"$temporary/report.txt"

if [ -n "$report" ]; then
    gate_publish_file_atomic 'evidence-integrity-self-test-report' \
        "$temporary/report.txt" "$report" "$temporary/report-publish"
else
    gate_replay_text_file "$temporary/report.txt"
fi
