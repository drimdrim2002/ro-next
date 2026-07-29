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

gate_inventory_receipt_failure() {
    gate_inventory_receipt_reason=$1
    gate_inventory_receipt_exit=${2:-1}
    printf 'tool=gate-tool-inventory-receipt-consumer result=NOT_EVALUATED exit=%s reason=%s\n' \
        "$gate_inventory_receipt_exit" "$gate_inventory_receipt_reason" >&2
    return "$gate_inventory_receipt_exit"
}

gate_inventory_consume_receipt() {
    gate_inventory_report_path=$1
    gate_inventory_receipt_path=$2
    gate_inventory_expected_token=$3
    gate_inventory_producer_exit=$4

    case "$gate_inventory_expected_token" in
        ''|*[!A-Za-z0-9._-]*)
            gate_inventory_receipt_failure INVALID_EXPECTED_TOKEN 64
            return
            ;;
    esac
    case "$gate_inventory_producer_exit" in
        0) ;;
        *[!0-9]*|'')
            gate_inventory_receipt_failure INVALID_PRODUCER_EXIT 64
            return
            ;;
        *)
            gate_inventory_receipt_failure PRODUCER_EXIT_NONZERO 1
            return
            ;;
    esac
    if [ ! -f "$gate_inventory_receipt_path" ] ||
        [ -L "$gate_inventory_receipt_path" ]; then
        gate_inventory_receipt_failure RECEIPT_MISSING_OR_NOT_REGULAR 1
        return
    fi
    if [ ! -f "$gate_inventory_report_path" ] ||
        [ -L "$gate_inventory_report_path" ]; then
        gate_inventory_receipt_failure REPORT_MISSING_OR_NOT_REGULAR 1
        return
    fi

    gate_inventory_receipt_marker=
    gate_inventory_receipt_token=
    gate_inventory_receipt_report_sha256=
    gate_inventory_receipt_result=
    gate_inventory_receipt_recorded_exit=
    gate_inventory_receipt_marker_seen=0
    gate_inventory_receipt_token_seen=0
    gate_inventory_receipt_report_sha256_seen=0
    gate_inventory_receipt_result_seen=0
    gate_inventory_receipt_recorded_exit_seen=0
    gate_inventory_receipt_order=
    gate_inventory_receipt_line_count=0
    while IFS='=' read -r gate_inventory_receipt_key \
        gate_inventory_receipt_value ||
        [ -n "$gate_inventory_receipt_key" ] ||
        [ -n "$gate_inventory_receipt_value" ]; do
        gate_inventory_receipt_line_count=$((gate_inventory_receipt_line_count + 1))
        if [ -n "$gate_inventory_receipt_order" ]; then
            gate_inventory_receipt_order=\
"$gate_inventory_receipt_order|$gate_inventory_receipt_key"
        else
            gate_inventory_receipt_order=$gate_inventory_receipt_key
        fi
        case "$gate_inventory_receipt_key" in
            PHASE00_GATE_TOOL_INVENTORY_RECEIPT)
                test "$gate_inventory_receipt_marker_seen" -eq 0 ||
                    gate_inventory_receipt_failure DUPLICATE_RECEIPT_MARKER 1 ||
                    return
                gate_inventory_receipt_marker_seen=1
                gate_inventory_receipt_marker=$gate_inventory_receipt_value
                ;;
            INVOCATION_TOKEN)
                test "$gate_inventory_receipt_token_seen" -eq 0 ||
                    gate_inventory_receipt_failure DUPLICATE_TOKEN 1 ||
                    return
                gate_inventory_receipt_token_seen=1
                gate_inventory_receipt_token=$gate_inventory_receipt_value
                ;;
            REPORT_SHA256)
                test "$gate_inventory_receipt_report_sha256_seen" -eq 0 ||
                    gate_inventory_receipt_failure \
                        DUPLICATE_REPORT_SHA256 1 ||
                    return
                gate_inventory_receipt_report_sha256_seen=1
                gate_inventory_receipt_report_sha256=\
$gate_inventory_receipt_value
                ;;
            PRODUCER_RESULT)
                test "$gate_inventory_receipt_result_seen" -eq 0 ||
                    gate_inventory_receipt_failure DUPLICATE_RESULT 1 ||
                    return
                gate_inventory_receipt_result_seen=1
                gate_inventory_receipt_result=$gate_inventory_receipt_value
                ;;
            PRODUCER_EXIT_CODE)
                test "$gate_inventory_receipt_recorded_exit_seen" -eq 0 ||
                    gate_inventory_receipt_failure DUPLICATE_RECORDED_EXIT 1 ||
                    return
                gate_inventory_receipt_recorded_exit_seen=1
                gate_inventory_receipt_recorded_exit=\
$gate_inventory_receipt_value
                ;;
            *)
                gate_inventory_receipt_failure UNKNOWN_RECEIPT_FIELD 1
                return
                ;;
        esac
    done <"$gate_inventory_receipt_path"

    test "$gate_inventory_receipt_marker_seen" -eq 1 ||
        gate_inventory_receipt_failure RECEIPT_MARKER_MISSING 1 ||
        return
    test "$gate_inventory_receipt_token_seen" -eq 1 ||
        gate_inventory_receipt_failure TOKEN_MISSING 1 ||
        return
    test "$gate_inventory_receipt_report_sha256_seen" -eq 1 ||
        gate_inventory_receipt_failure REPORT_SHA256_MISSING 1 ||
        return
    test "$gate_inventory_receipt_result_seen" -eq 1 ||
        gate_inventory_receipt_failure PRODUCER_RESULT_MISSING 1 ||
        return
    test "$gate_inventory_receipt_recorded_exit_seen" -eq 1 ||
        gate_inventory_receipt_failure RECORDED_EXIT_MISSING 1 ||
        return
    test "$gate_inventory_receipt_line_count" -eq 5 ||
        gate_inventory_receipt_failure RECEIPT_FIELD_COUNT 1 ||
        return
    test "$gate_inventory_receipt_order" = \
        'PHASE00_GATE_TOOL_INVENTORY_RECEIPT|INVOCATION_TOKEN|REPORT_SHA256|PRODUCER_RESULT|PRODUCER_EXIT_CODE' ||
        gate_inventory_receipt_failure RECEIPT_FIELD_ORDER 1 ||
        return
    test "$gate_inventory_receipt_marker" = PASS ||
        gate_inventory_receipt_failure RECEIPT_NOT_PASS 1 ||
        return
    test "$gate_inventory_receipt_token" = "$gate_inventory_expected_token" ||
        gate_inventory_receipt_failure TOKEN_MISMATCH 1 ||
        return
    test "$gate_inventory_receipt_result" = PASS ||
        gate_inventory_receipt_failure PRODUCER_RESULT_NOT_PASS 1 ||
        return
    test "$gate_inventory_receipt_recorded_exit" = 0 ||
        gate_inventory_receipt_failure RECORDED_EXIT_NOT_ZERO 1 ||
        return
    case "$gate_inventory_receipt_report_sha256" in
        *[!0-9a-f]*|'')
            gate_inventory_receipt_failure REPORT_SHA256_INVALID 1
            return
            ;;
    esac
    test "${#gate_inventory_receipt_report_sha256}" -eq 64 ||
        gate_inventory_receipt_failure REPORT_SHA256_INVALID 1 ||
        return

    if gate_make_temporary_directory \
        'mktemp(gate-tool-inventory-receipt-consumer)' \
        "${TMPDIR:-/tmp}/ro-next-phase00-tool-receipt-consumer.XXXXXX"; then
        gate_inventory_consumer_temporary=$GATE_TEMPORARY_DIRECTORY
    else
        gate_inventory_consumer_temporary_exit=$?
        gate_inventory_receipt_failure \
            REPORT_HASH_SCRATCH_UNAVAILABLE \
            "$gate_inventory_consumer_temporary_exit"
        return
    fi
    if gate_sha256_file "$gate_inventory_report_path" \
        "$gate_inventory_consumer_temporary/report"; then
        gate_inventory_current_report_sha256=$GATE_SHA256
    else
        gate_inventory_report_hash_exit=$?
        command rm -rf -- "$gate_inventory_consumer_temporary" \
            >/dev/null 2>&1 || :
        gate_inventory_receipt_failure \
            REPORT_HASH_NOT_EVALUATED "$gate_inventory_report_hash_exit"
        return
    fi
    if command rm -rf -- "$gate_inventory_consumer_temporary" \
        >/dev/null 2>&1; then
        :
    else
        gate_inventory_consumer_cleanup_exit=$?
        gate_inventory_receipt_failure REPORT_HASH_SCRATCH_CLEANUP_FAILED \
            "$gate_inventory_consumer_cleanup_exit"
        return
    fi
    test "$gate_inventory_current_report_sha256" = \
        "$gate_inventory_receipt_report_sha256" ||
        gate_inventory_receipt_failure REPORT_SHA256_MISMATCH 1 ||
        return
}

gate_inventory_validate_report() {
    gate_inventory_report_path=$1
    if [ ! -f "$gate_inventory_report_path" ] ||
        [ -L "$gate_inventory_report_path" ]; then
        gate_inventory_receipt_failure REPORT_MISSING_OR_NOT_REGULAR 1
        return
    fi

    gate_inventory_report_pass_count=0
    gate_inventory_report_inventory_count=0
    gate_inventory_report_actual_count=0
    gate_inventory_report_conditional_count=0
    gate_inventory_report_launcher_count=0
    while IFS= read -r gate_inventory_report_line ||
        [ -n "$gate_inventory_report_line" ]; do
        case "$gate_inventory_report_line" in
            PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS)
                gate_inventory_report_pass_count=\
$((gate_inventory_report_pass_count + 1))
                ;;
            INVENTORY_ROW_COUNT=54)
                gate_inventory_report_inventory_count=\
$((gate_inventory_report_inventory_count + 1))
                ;;
            WRAPPER_LAUNCHER_ACTUAL_CLASS_COUNT=13)
                gate_inventory_report_actual_count=\
$((gate_inventory_report_actual_count + 1))
                ;;
            WRAPPER_LAUNCHER_CONDITIONAL_CLASS_COUNT=14)
                gate_inventory_report_conditional_count=\
$((gate_inventory_report_conditional_count + 1))
                ;;
            PINNED_LAUNCHER_CONTRACT_ROW_COUNT=19)
                gate_inventory_report_launcher_count=\
$((gate_inventory_report_launcher_count + 1))
                ;;
        esac
    done <"$gate_inventory_report_path"

    test "$gate_inventory_report_pass_count" -eq 1 ||
        gate_inventory_receipt_failure REPORT_PASS_MARKER_COUNT 1 ||
        return
    test "$gate_inventory_report_inventory_count" -eq 1 ||
        gate_inventory_receipt_failure REPORT_INVENTORY_COUNT 1 ||
        return
    test "$gate_inventory_report_actual_count" -eq 1 ||
        gate_inventory_receipt_failure REPORT_ACTUAL_COUNT 1 ||
        return
    test "$gate_inventory_report_conditional_count" -eq 1 ||
        gate_inventory_receipt_failure REPORT_CONDITIONAL_COUNT 1 ||
        return
    test "$gate_inventory_report_launcher_count" -eq 1 ||
        gate_inventory_receipt_failure REPORT_LAUNCHER_COUNT 1 ||
        return
}

if [ "${1:-}" = --consume ]; then
    test "$#" -eq 5 ||
        gate_inventory_receipt_failure EXPECTED_REPORT_RECEIPT_TOKEN_EXIT 64
    gate_inventory_consume_receipt "$2" "$3" "$4" "$5"
    gate_inventory_validate_report "$2"
    exit $?
fi

if [ "$#" -ne 3 ]; then
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=EXPECTED_REPORT_RECEIPT_TOKEN' \
        >&2
    exit 64
fi

requested_report=$1
requested_receipt=$2
invocation_token=$3
case "$invocation_token" in
    ''|*[!A-Za-z0-9._-]*)
        printf '%s\n' \
            'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=INVALID_TOKEN' \
            >&2
        exit 64
        ;;
esac
case "$requested_report" in
    */*)
        requested_report_parent=${requested_report%/*}
        report_basename=${requested_report##*/}
        ;;
    *)
        requested_report_parent=.
        report_basename=$requested_report
        ;;
esac
case "$report_basename" in
    ''|.|..)
        printf '%s\n' \
            'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=INVALID_REPORT_BASENAME' \
            >&2
        exit 64
        ;;
esac
caller_directory=$(pwd -P)
case "$requested_report_parent" in
    /*) requested_report_parent_path=$requested_report_parent ;;
    *) requested_report_parent_path=$caller_directory/$requested_report_parent ;;
esac
if report_parent_physical=$(
    CDPATH= cd -P -- "$requested_report_parent" && pwd -P
); then
    :
else
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=REPORT_PARENT_UNRESOLVED' \
        >&2
    exit 64
fi
report_canonical=$report_parent_physical/$report_basename

case "$requested_receipt" in
    */*)
        requested_receipt_parent=${requested_receipt%/*}
        receipt_basename=${requested_receipt##*/}
        ;;
    *)
        requested_receipt_parent=.
        receipt_basename=$requested_receipt
        ;;
esac
case "$receipt_basename" in
    ''|.|..)
        printf '%s\n' \
            'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=INVALID_RECEIPT_BASENAME' \
            >&2
        exit 64
        ;;
esac
if receipt_parent_physical=$(
    CDPATH= cd -P -- "$requested_receipt_parent" && pwd -P
); then
    :
else
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=RECEIPT_PARENT_UNRESOLVED' \
        >&2
    exit 64
fi
if [ ! "$report_parent_physical" -ef "$receipt_parent_physical" ]; then
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=REPORT_RECEIPT_PARENT_MISMATCH' \
        >&2
    exit 64
fi
receipt_canonical=$receipt_parent_physical/$receipt_basename
if [ "$report_canonical" = "$receipt_canonical" ] ||
    { { [ -e "$requested_report" ] || [ -L "$requested_report" ]; } &&
      { [ -e "$requested_receipt" ] || [ -L "$requested_receipt" ]; } &&
      [ "$requested_report" -ef "$requested_receipt" ]; }; then
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=REPORT_RECEIPT_ALIAS' \
        >&2
    exit 64
fi

gate_inventory_wrapper_protected_alias() {
    gate_inventory_wrapper_output=$1
    gate_inventory_wrapper_output_canonical=$2
    gate_inventory_wrapper_output_label=$3
    shift
    shift
    shift
    gate_inventory_wrapper_protected=$1
    if [ ! -e "$gate_inventory_wrapper_protected" ] &&
        [ ! -L "$gate_inventory_wrapper_protected" ]; then
        return 0
    fi
    gate_inventory_wrapper_protected_parent=\
${gate_inventory_wrapper_protected%/*}
    gate_inventory_wrapper_protected_basename=\
${gate_inventory_wrapper_protected##*/}
    gate_inventory_wrapper_protected_parent_physical=$(
        CDPATH= cd -P -- "$gate_inventory_wrapper_protected_parent" &&
            pwd -P
    )
    gate_inventory_wrapper_protected_canonical=\
"$gate_inventory_wrapper_protected_parent_physical/$gate_inventory_wrapper_protected_basename"
    if [ "$gate_inventory_wrapper_output_canonical" = \
        "$gate_inventory_wrapper_protected_canonical" ]; then
        printf 'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 output=%s reason=CANONICAL_PROTECTED_ALIAS path=%s\n' \
            "$gate_inventory_wrapper_output_label" \
            "$gate_inventory_wrapper_protected" >&2
        exit 64
    fi
    if { [ -e "$gate_inventory_wrapper_output" ] ||
        [ -L "$gate_inventory_wrapper_output" ]; } &&
        [ "$gate_inventory_wrapper_output" -ef \
            "$gate_inventory_wrapper_protected" ]; then
        printf 'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 output=%s reason=PHYSICAL_PROTECTED_ALIAS path=%s\n' \
            "$gate_inventory_wrapper_output_label" \
            "$gate_inventory_wrapper_protected" >&2
        exit 64
    fi
}

inventory="$repository/build/phase-00-gate-tool-inventory.tsv"
source_manifest="$repository/build/phase-00-gate-source-manifest.tsv"
for protected in \
    "$inventory" \
    "$repository/build/maven-3.9.14-launcher-contract.tsv" \
    "$repository/.mvn/wrapper/maven-wrapper.sh.sha256" \
    "$repository/.mvn/wrapper/maven-launcher-3.9.14.sha256" \
    "$repository/.mvn/wrapper/maven-wrapper.properties" \
    "$source_manifest" \
    "$repository/mvnw" \
    "$repository/target/phase-00-evidence/evidence-manifest.tsv" \
    "$repository/target/phase-00-evidence/evidence-manifest.sha256" \
    "$repository/target/phase-00-evidence/pre-review-evidence-manifest.tsv" \
    "$repository/target/phase-00-evidence/pre-review-evidence-manifest.sha256"
do
    gate_inventory_wrapper_protected_alias \
        "$requested_report" "$report_canonical" report "$protected"
    gate_inventory_wrapper_protected_alias \
        "$requested_receipt" "$receipt_canonical" receipt "$protected"
done
if [ -f "$source_manifest" ]; then
    while IFS='	' read -r protected_source protected_digest; do
        test -n "$protected_source" || continue
        gate_inventory_wrapper_protected_alias \
            "$requested_report" "$report_canonical" report \
            "$repository/$protected_source"
        gate_inventory_wrapper_protected_alias \
            "$requested_receipt" "$receipt_canonical" receipt \
            "$repository/$protected_source"
    done <"$source_manifest"
fi
if [ -d "$requested_report" ]; then
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=REPORT_IS_DIRECTORY' \
        >&2
    exit 64
fi
if [ -d "$requested_receipt" ]; then
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=64 reason=RECEIPT_IS_DIRECTORY' \
        >&2
    exit 64
fi

CDPATH= cd -P -- "$requested_report_parent"
report=./$report_basename
receipt=./$receipt_basename

gate_make_temporary_directory 'mktemp(gate-tool-inventory-wrapper)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-tool-inventory-wrapper.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
pass_report_stage=
pass_receipt_stage=
wrapper_complete=0

gate_inventory_wrapper_exit() {
    gate_inventory_wrapper_status=$?
    trap - EXIT HUP INT TERM
    set +e
    if [ -n "$pass_report_stage" ] &&
        { [ -e "$pass_report_stage" ] || [ -L "$pass_report_stage" ]; }; then
        command rm -f -- "$pass_report_stage" >/dev/null 2>&1 || :
    fi
    if [ -n "$pass_receipt_stage" ] &&
        { [ -e "$pass_receipt_stage" ] || [ -L "$pass_receipt_stage" ]; }; then
        command rm -f -- "$pass_receipt_stage" >/dev/null 2>&1 || :
    fi
    if [ -n "${temporary:-}" ] && [ -d "$temporary" ]; then
        command rm -rf -- "$temporary" >/dev/null 2>&1 || :
    fi
    if [ "$gate_inventory_wrapper_status" -ne 0 ]; then
        printf 'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=%s\n' \
            "$gate_inventory_wrapper_status" >&2
    fi
    exit "$gate_inventory_wrapper_status"
}

gate_inventory_wrapper_signal() {
    gate_inventory_wrapper_signal_name=$1
    gate_inventory_wrapper_signal_exit=$2
    printf 'tool=signal(%s:gate-tool-inventory-wrapper) result=NOT_EVALUATED exit=%s\n' \
        "$gate_inventory_wrapper_signal_name" \
        "$gate_inventory_wrapper_signal_exit" >&2
    exit "$gate_inventory_wrapper_signal_exit"
}

trap gate_inventory_wrapper_exit EXIT
trap 'gate_inventory_wrapper_signal HUP 129' HUP
trap 'gate_inventory_wrapper_signal INT 130' INT
trap 'gate_inventory_wrapper_signal TERM 143' TERM

{
    printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=NOT_EVALUATED'
    printf '%s\n' 'INVOCATION_RESULT=NOT_EVALUATED'
} >"$temporary/final-report-not-evaluated.txt"
gate_publish_file_atomic 'gate-tool-inventory-wrapper-invalidation' \
    "$temporary/final-report-not-evaluated.txt" "$report" \
    "$temporary/final-report-invalidation"

set +e
"$repository/build/verify-gate-tool-inventory.sh" \
    "$temporary/deterministic-report.txt" \
    "$temporary/invocation-receipt.txt" \
    "$invocation_token" \
    >"$temporary/verifier.stdout" 2>"$temporary/verifier.stderr"
producer_exit=$?
set -e
if [ "$producer_exit" -ne 0 ]; then
    gate_replay_text_file "$temporary/verifier.stderr" stderr
    gate_inventory_consume_receipt \
        "$temporary/deterministic-report.txt" \
        "$temporary/invocation-receipt.txt" \
        "$invocation_token" "$producer_exit" || :
    exit "$producer_exit"
fi
gate_inventory_consume_receipt \
    "$temporary/deterministic-report.txt" \
    "$temporary/invocation-receipt.txt" \
    "$invocation_token" "$producer_exit"
gate_inventory_validate_report "$temporary/deterministic-report.txt"

test "$requested_report_parent_path" -ef . || {
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=1 reason=REPORT_PARENT_REPLACED' \
        >&2
    exit 1
}
pass_report_stage=./.$report_basename.pass.$$
pass_receipt_stage=./.$receipt_basename.pass.$$
gate_capture_checked 'cp(stage:gate-tool-inventory-wrapper-pass)' \
    "$temporary/pass-stage-copy.out" "$temporary/pass-stage-copy.err" \
    cp "$temporary/deterministic-report.txt" "$pass_report_stage"
gate_capture_checked 'cp(stage:gate-tool-inventory-wrapper-receipt)' \
    "$temporary/receipt-stage-copy.out" \
    "$temporary/receipt-stage-copy.err" \
    cp "$temporary/invocation-receipt.txt" "$pass_receipt_stage"

if command rm -rf -- "$temporary" >/dev/null 2>&1; then
    temporary=
else
    cleanup_exit=$?
    printf 'tool=rm(cleanup:gate-tool-inventory-wrapper) result=NOT_EVALUATED exit=%s\n' \
        "$cleanup_exit" >&2
    exit "$cleanup_exit"
fi
test "$requested_report_parent_path" -ef . || {
    printf '%s\n' \
        'tool=gate-tool-inventory-wrapper result=NOT_EVALUATED exit=1 reason=REPORT_PARENT_REPLACED' \
        >&2
    exit 1
}
if mv "$pass_report_stage" "$report"; then
    pass_report_stage=
else
    publish_exit=$?
    printf 'tool=mv(publish:gate-tool-inventory-wrapper-pass) result=NOT_EVALUATED exit=%s\n' \
        "$publish_exit" >&2
    exit "$publish_exit"
fi
if mv "$pass_receipt_stage" "$receipt"; then
    pass_receipt_stage=
else
    publish_exit=$?
    printf 'tool=mv(publish:gate-tool-inventory-wrapper-receipt) result=NOT_EVALUATED exit=%s\n' \
        "$publish_exit" >&2
    exit "$publish_exit"
fi
wrapper_complete=1
