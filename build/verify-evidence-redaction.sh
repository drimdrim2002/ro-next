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
if [ "${1:-}" = '--self-test' ] && [ "$#" -eq 2 ] \
    && [ -f "$2" ] && [ ! -L "$2" ]; then
    : >"$2"
fi
gate_make_temporary_directory 'mktemp(evidence-redaction)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-redaction.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

scan_directory() {
    directory=$1
    : >"$temporary/matches.unsorted.txt"
    gate_inventory_tree "$directory" "$temporary/redaction-tree"
    gate_capture_checked 'cp(redaction-file-list)' \
        "$temporary/redaction-file-list-copy.out" \
        "$temporary/redaction-file-list-copy.err" \
        cp "$GATE_TREE_FILE_LIST" "$temporary/files.txt"

    scan_index=0
    while IFS= read -r relative_file; do
        file="$directory/$relative_file"
        scan_index=$((scan_index + 1))
        set +e
        grep -IEl \
            'PHASE00_CANARY_CREDENTIAL_VALUE|PHASE00_CANARY_PII_VALUE|BEGIN [A-Z ]*PRIVATE KEY|AKIA[0-9A-Z]{16}|Authorization:[[:space:]]*(Bearer|Basic)[[:space:]]+[^[:space:]]+' \
            "$file" >>"$temporary/matches.unsorted.txt" \
            2>"$temporary/detector-error.txt"
        marker_exit=$?
        set -e
        case "$marker_exit" in
            0|1)
                ;;
            *)
                printf '%s' "$file" >"$temporary/file-id-$scan_index.txt"
                gate_sha256_file "$temporary/file-id-$scan_index.txt" \
                    "$temporary/file-id-$scan_index"
                file_id=$GATE_SHA256
                gate_report_not_evaluated \
                    "grep(redaction-marker:file-id-sha256=$file_id)" \
                    "$marker_exit" "$temporary/detector-error.txt"
                return "$marker_exit"
                ;;
        esac

        set +e
        grep -IEil \
            '"(password|secret|token|credential|email|address)"[[:space:]]*:[[:space:]]*"[^"]+"' \
            "$file" >>"$temporary/matches.unsorted.txt" \
            2>"$temporary/detector-error.txt"
        structured_exit=$?
        set -e
        case "$structured_exit" in
            0|1)
                ;;
            *)
                printf '%s' "$file" >"$temporary/file-id-$scan_index.txt"
                gate_sha256_file "$temporary/file-id-$scan_index.txt" \
                    "$temporary/file-id-$scan_index"
                file_id=$GATE_SHA256
                gate_report_not_evaluated \
                    "grep(redaction-structured:file-id-sha256=$file_id)" \
                    "$structured_exit" "$temporary/detector-error.txt"
                return "$structured_exit"
                ;;
        esac
    done <"$temporary/files.txt"

    gate_sort_unique_file "$temporary/matches.unsorted.txt" \
        "$temporary/matches.txt" "$temporary/matches.sort.err"

    gate_capture_checked 'awk(redaction-detection-count)' \
        "$temporary/detection-count.txt" "$temporary/detection-count.err" \
        awk 'END { print NR + 0 }' "$temporary/matches.txt"
    IFS= read -r detection_count <"$temporary/detection-count.txt" || :
    printf 'DETECTION_COUNT=%s\n' "$detection_count"
    if [ "$detection_count" -ne 0 ]; then
        return 1
    fi
    printf '%s\n' 'DETECTOR_EXECUTION_STATUS=PASS'
    printf '%s\n' 'DETECTOR_RESULT=NO_MATCH'
}

if [ "${1:-}" = '--self-test' ]; then
    if [ "$#" -ne 2 ]; then
        printf 'usage: %s --self-test <report-path>\n' "$0" >&2
        exit 2
    fi
    report=$2
    report_stage="$temporary/evidence-redaction-self-test.txt"
    if [ -e "$report" ] || [ -L "$report" ]; then
        if [ -f "$report" ] && [ ! -L "$report" ]; then
            : >"$report"
        fi
        gate_capture_checked 'rm(stale-redaction-self-test-report)' \
            "$temporary/stale-report-remove.out" \
            "$temporary/stale-report-remove.err" \
            rm -f -- "$report"
    fi
    mkdir -p "$temporary/canary"
    printf '%s\n' 'PHASE00_CANARY_CREDENTIAL_VALUE' \
        >"$temporary/canary/credential-marker.txt"
    printf '%s\n' 'PHASE00_CANARY_PII_VALUE' \
        >"$temporary/canary/pii-marker.txt"
    set +e
    "$repository/build/verify-evidence-redaction.sh" "$temporary/canary" \
        >"$temporary/canary-scan.txt" 2>&1
    canary_exit=$?
    set -e
    test "$canary_exit" -ne 0
    gate_require_match \
        'redaction canary detection count' \
        "$temporary/canary-count.matches" \
        "$temporary/canary-count.detector.err" \
        grep -Fx 'DETECTION_COUNT=2' "$temporary/canary-scan.txt"

    mkdir -p "$temporary/clean"
    printf '%s\n' 'redacted evidence fixture' >"$temporary/clean/safe-report.txt"
    "$repository/build/verify-evidence-redaction.sh" "$temporary/clean" \
        >"$temporary/clean-scan.txt" 2>&1
    gate_require_match \
        'redaction clean detection count' \
        "$temporary/clean-count.matches" \
        "$temporary/clean-count.detector.err" \
        grep -Fx 'DETECTION_COUNT=0' "$temporary/clean-scan.txt"
    gate_require_match \
        'redaction clean detector status' \
        "$temporary/clean-status.matches" \
        "$temporary/clean-status.detector.err" \
        grep -Fx 'DETECTOR_EXECUTION_STATUS=PASS' \
        "$temporary/clean-scan.txt"
    gate_require_match \
        'redaction clean no-match result' \
        "$temporary/clean-result.matches" \
        "$temporary/clean-result.detector.err" \
        grep -Fx 'DETECTOR_RESULT=NO_MATCH' "$temporary/clean-scan.txt"
    gate_require_match \
        'redaction clean PASS report' \
        "$temporary/clean-pass.matches" \
        "$temporary/clean-pass.detector.err" \
        grep -Fx 'PHASE00_EVIDENCE_REDACTION=PASS' \
        "$temporary/clean-scan.txt"

    mkdir -p "$temporary/grep-exit-2-shim"
    {
        printf '%s\n' '#!/bin/sh'
        printf '%s\n' 'exit 2'
    } >"$temporary/grep-exit-2-shim/grep"
    chmod +x "$temporary/grep-exit-2-shim/grep"
    set +e
    PATH="$temporary/grep-exit-2-shim:$PATH" \
        "$repository/build/verify-evidence-redaction.sh" "$temporary/clean" \
        >"$temporary/grep-exit-2-scan.txt" 2>&1
    grep_exit_2_scan_exit=$?
    set -e
    test "$grep_exit_2_scan_exit" -eq 2
    gate_require_no_match \
        'redaction detector-error PASS-report absence' \
        "$temporary/grep-error-pass.matches" \
        "$temporary/grep-error-pass.detector.err" \
        grep -F 'PHASE00_EVIDENCE_REDACTION=PASS' \
        "$temporary/grep-exit-2-scan.txt"
    gate_require_match \
        'redaction detector-error NOT_EVALUATED report' \
        "$temporary/grep-error-not-evaluated.matches" \
        "$temporary/grep-error-not-evaluated.detector.err" \
        grep -F 'result=NOT_EVALUATED' \
        "$temporary/grep-exit-2-scan.txt"

    printf '%s' 'PHASE00_CANARY_CREDENTIAL_VALUE' \
        >"$temporary/credential-token.txt"
    gate_sha256_file "$temporary/credential-token.txt" \
        "$temporary/credential-token"
    credential_id=$GATE_SHA256
    printf '%s' 'PHASE00_CANARY_PII_VALUE' \
        >"$temporary/pii-token.txt"
    gate_sha256_file "$temporary/pii-token.txt" "$temporary/pii-token"
    pii_id=$GATE_SHA256
    {
        printf '%s\n' 'PHASE00_KNOWN_MARKER_CANARY_SCANNER=PASS'
        printf 'CANARY_SCAN_EXIT_CODE=%s\n' "$canary_exit"
        printf '%s\n' 'CANARY_DETECTION_COUNT=2'
        printf 'CREDENTIAL_TOKEN_ID_SHA256=%s\n' "$credential_id"
        printf 'PII_TOKEN_ID_SHA256=%s\n' "$pii_id"
        printf '%s\n' 'RAW_CANARY_VALUE_REPORTED=NO'
        printf '%s\n' 'CLEAN_NO_DETECTION_SCAN=PASS'
        printf '%s\n' 'CLEAN_DETECTION_COUNT=0'
        printf '%s\n' 'GREP_EXIT_2_FAULT_INJECTION=PASS'
        printf 'GREP_EXIT_2_SCAN_EXIT_CODE=%s\n' "$grep_exit_2_scan_exit"
        printf '%s\n' 'GREP_EXIT_2_RESULT=NOT_EVALUATED'
        printf '%s\n' 'GREP_EXIT_2_PASS_REPORT_PRESENT=NO'
        printf '%s\n' 'AUTOMATED_REDACTION_FAULT_TEST_COUNT=1'
    } >"$report_stage"
    gate_capture_checked 'mv(publish-redaction-self-test-report)' \
        "$temporary/report-publish.out" \
        "$temporary/report-publish.err" \
        mv "$report_stage" "$report"
    exit 0
fi

if [ "$#" -ne 1 ]; then
    printf 'usage: %s <evidence-directory>\n' "$0" >&2
    exit 2
fi

directory=$(CDPATH= cd -- "$1" 2>/dev/null && pwd) || {
    printf 'evidence directory does not exist: %s\n' "$1" >&2
    exit 1
}
scan_directory "$directory"
printf '%s\n' 'PHASE00_EVIDENCE_REDACTION=PASS'
printf '%s\n' 'CREDENTIAL_SECRET_OR_RAW_PII_MATCH_COUNT=0'
