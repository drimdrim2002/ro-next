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
gate_make_temporary_directory 'mktemp(source-scan)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-source-scan.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

scan_no_match() {
    scan_name=$1
    pattern=$2
    shift
    shift

    for required_root in "$@"; do
        if [ ! -e "$repository/$required_root" ]; then
            printf 'required source scan root is missing: scan=%s root=%s\n' \
                "$scan_name" "$required_root" >&2
            return 2
        fi
    done

    set +e
    (
        cd "$repository"
        rg -n "$pattern" "$@"
    ) >"$temporary/$scan_name.matches.txt" \
        2>"$temporary/$scan_name.detector-error.txt"
    detector_exit=$?
    set -e

    printf 'SCAN_NAME=%s\n' "$scan_name"
    printf '%s\n' 'DETECTOR=rg'
    printf 'DETECTOR_EXIT_CODE=%s\n' "$detector_exit"
    case "$detector_exit" in
        0)
            gate_capture_checked "awk(source-scan-match-count:$scan_name)" \
                "$temporary/$scan_name.match-count.txt" \
                "$temporary/$scan_name.match-count.err" \
                awk 'END { print NR + 0 }' \
                "$temporary/$scan_name.matches.txt"
            IFS= read -r match_count \
                <"$temporary/$scan_name.match-count.txt" || :
            printf '%s\n' 'DETECTOR_EXECUTION_STATUS=PASS'
            printf '%s\n' 'MATCH_RESULT=MATCH'
            printf 'MATCH_COUNT=%s\n' "$match_count"
            printf '%s\n' 'SOURCE_SCAN_GATE=FAIL'
            return 1
            ;;
        1)
            printf '%s\n' 'DETECTOR_EXECUTION_STATUS=PASS'
            printf '%s\n' 'MATCH_RESULT=NO_MATCH'
            printf '%s\n' 'MATCH_COUNT=0'
            printf '%s\n' 'SOURCE_SCAN_GATE=PASS'
            return 0
            ;;
        *)
            printf '%s\n' 'DETECTOR_EXECUTION_STATUS=FAIL'
            printf '%s\n' 'MATCH_RESULT=NOT_EVALUATED'
            printf '%s\n' 'SOURCE_SCAN_GATE=FAIL'
            gate_report_not_evaluated "rg(source-scan:$scan_name)" \
                "$detector_exit" \
                "$temporary/$scan_name.detector-error.txt"
            return "$detector_exit"
            ;;
    esac
}

run_scan() {
    case "$1" in
        provider)
            scan_no_match \
                'provider' \
                'com\.google|software\.amazon\.awssdk|com\.azure|io\.kubernetes|gurobi' \
                rpdptw
            ;;
        ortools)
            scan_no_match \
                'ortools' \
                'com\.google\.ortools|route-selection-ortools-cpsat' \
                pom.xml rpdptw adapters
            ;;
        *)
            printf 'unknown Phase 00 source scan: %s\n' "$1" >&2
            return 2
            ;;
    esac
}

if [ "${1:-}" = '--self-test' ]; then
    if [ "$#" -ne 2 ]; then
        printf 'usage: %s --self-test <report-path>\n' "$0" >&2
        exit 2
    fi
    report=$2
    report_stage="$temporary/source-scan-fault-self-test.txt"
    if [ -e "$report" ] || [ -L "$report" ]; then
        if [ -f "$report" ] && [ ! -L "$report" ]; then
            : >"$report"
        fi
        gate_capture_checked 'rm(stale-source-scan-self-test-report)' \
            "$temporary/stale-report-remove.out" \
            "$temporary/stale-report-remove.err" \
            rm -f -- "$report"
    fi

    mkdir -p "$temporary/rg-exit-2-shim"
    {
        printf '%s\n' '#!/bin/sh'
        printf '%s\n' 'exit 2'
    } >"$temporary/rg-exit-2-shim/rg"
    chmod +x "$temporary/rg-exit-2-shim/rg"

    set +e
    PATH="$temporary/rg-exit-2-shim:$PATH" \
        "$repository/build/verify-phase-00-source-scans.sh" --scan provider \
        >"$temporary/rg-exit-2-scan.txt" 2>&1
    injected_exit=$?
    set -e
    test "$injected_exit" -eq 2
    gate_require_match \
        'source scan injected detector exit record' \
        "$temporary/rg-exit-record.matches" \
        "$temporary/rg-exit-record.detector.err" \
        grep -Fx 'DETECTOR_EXIT_CODE=2' "$temporary/rg-exit-2-scan.txt"
    gate_require_match \
        'source scan injected execution status' \
        "$temporary/rg-status.matches" \
        "$temporary/rg-status.detector.err" \
        grep -Fx 'DETECTOR_EXECUTION_STATUS=FAIL' \
        "$temporary/rg-exit-2-scan.txt"
    gate_require_match \
        'source scan injected not-evaluated result' \
        "$temporary/rg-result.matches" \
        "$temporary/rg-result.detector.err" \
        grep -Fx 'MATCH_RESULT=NOT_EVALUATED' \
        "$temporary/rg-exit-2-scan.txt"
    gate_require_no_match \
        'source detector-error PASS-report absence' \
        "$temporary/rg-pass.matches" \
        "$temporary/rg-pass.detector.err" \
        grep -F 'PHASE00_SOURCE_SCANS=PASS' \
        "$temporary/rg-exit-2-scan.txt"

    {
        printf '%s\n' 'PHASE00_SOURCE_SCAN_FAULT_SELF_TEST=PASS'
        printf '%s\n' 'FAULT_TEST_NAME=rgExitTwoIsExecutionFailure'
        printf '%s\n' 'INJECTED_SCAN=provider'
        printf '%s\n' 'INJECTED_DETECTOR_EXIT_CODE=2'
        printf 'SCAN_COMMAND_EXIT_CODE=%s\n' "$injected_exit"
        printf '%s\n' 'MATCH_RESULT=NOT_EVALUATED'
        printf '%s\n' 'PASS_REPORT_PRESENT=NO'
        printf '%s\n' 'AUTOMATED_SOURCE_SCAN_FAULT_TEST_COUNT=1'
    } >"$report_stage"
    gate_publish_file_atomic 'source-scan-fault-self-test-report' \
        "$report_stage" "$report" "$temporary/report-publish"
    exit 0
fi

if [ "${1:-}" = '--scan' ]; then
    if [ "$#" -ne 2 ]; then
        printf 'usage: %s --scan <provider|ortools>\n' "$0" >&2
        exit 2
    fi
    run_scan "$2"
    printf 'PHASE00_SOURCE_SCAN_%s=PASS\n' "$2"
    exit 0
fi

if [ "$#" -ne 0 ]; then
    printf 'usage: %s [--scan <provider|ortools>|--self-test <report-path>]\n' \
        "$0" >&2
    exit 2
fi

run_scan provider
run_scan ortools
printf '%s\n' 'PHASE00_SOURCE_SCANS=PASS'
