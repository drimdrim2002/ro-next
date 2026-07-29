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

gate_make_temporary_directory 'mktemp(gate-tool-inventory-receipt-test)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-tool-receipt-test.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

if [ "${1:-}" = --r11-only ]; then
    r11_report="$temporary/r11-report.txt"
    r11_receipt="$temporary/r11-receipt.txt"
    r11_initial_token=r11-initial-token
    r11_current_token=r11-current-token

    "$repository/build/run-verify-gate-tool-inventory.sh" \
        "$r11_report" "$r11_receipt" "$r11_initial_token"
    "$repository/build/run-verify-gate-tool-inventory.sh" \
        --consume "$r11_report" "$r11_receipt" \
        "$r11_initial_token" 0
    gate_sha256_file "$r11_report" "$temporary/r11-report-before"
    r11_report_before=$GATE_SHA256
    gate_sha256_file "$r11_receipt" "$temporary/r11-receipt-before"
    r11_receipt_before=$GATE_SHA256
    r11_pass_before=$(grep -Fxc \
        'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' "$r11_report")

    set +e
    env TMPDIR="$temporary/does-not-exist/child" \
        "$repository/build/run-verify-gate-tool-inventory.sh" \
        "$r11_report" "$r11_receipt" "$r11_current_token" \
        >"$temporary/r11-fault.stdout" 2>"$temporary/r11-fault.stderr"
    r11_fault_exit=$?
    set -e
    test "$r11_fault_exit" -ne 0
    gate_require_match 'R11 missing TMPDIR structured diagnostic' \
        "$temporary/r11-fault.match" "$temporary/r11-fault.match.err" \
        grep -F 'result=NOT_EVALUATED' "$temporary/r11-fault.stderr"

    gate_sha256_file "$r11_report" "$temporary/r11-report-after"
    test "$GATE_SHA256" = "$r11_report_before"
    r11_report_after=$GATE_SHA256
    gate_sha256_file "$r11_receipt" "$temporary/r11-receipt-after"
    test "$GATE_SHA256" = "$r11_receipt_before"
    r11_pass_after=$(grep -Fxc \
        'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' "$r11_report")
    test "$r11_pass_before" -eq 1
    test "$r11_pass_after" -eq 1

    set +e
    "$repository/build/run-verify-gate-tool-inventory.sh" \
        --consume "$r11_report" "$r11_receipt" \
        "$r11_current_token" "$r11_fault_exit" \
        >"$temporary/r11-nonzero-consume.stdout" \
        2>"$temporary/r11-nonzero-consume.stderr"
    r11_nonzero_consume_exit=$?
    "$repository/build/run-verify-gate-tool-inventory.sh" \
        --consume "$r11_report" "$r11_receipt" \
        "$r11_current_token" 0 \
        >"$temporary/r11-stale-consume.stdout" \
        2>"$temporary/r11-stale-consume.stderr"
    r11_stale_consume_exit=$?
    set -e
    test "$r11_nonzero_consume_exit" -ne 0
    test "$r11_stale_consume_exit" -ne 0
    gate_require_match 'R11 nonzero exit binding' \
        "$temporary/r11-nonzero-consume.match" \
        "$temporary/r11-nonzero-consume.match.err" \
        grep -F 'reason=PRODUCER_EXIT_NONZERO' \
        "$temporary/r11-nonzero-consume.stderr"
    gate_require_match 'R11 stale receipt token rejection' \
        "$temporary/r11-stale-consume.match" \
        "$temporary/r11-stale-consume.match.err" \
        grep -F 'reason=TOKEN_MISMATCH' \
        "$temporary/r11-stale-consume.stderr"

    r11_unusable_tmpdir="$temporary/unusable-tmpdir"
    printf '%s\n' 'not-a-directory' >"$r11_unusable_tmpdir"
    set +e
    env TMPDIR="$r11_unusable_tmpdir" \
        "$repository/build/run-verify-gate-tool-inventory.sh" \
        "$r11_report" "$r11_receipt" r11-unusable-tmpdir-token \
        >"$temporary/r11-unusable.stdout" \
        2>"$temporary/r11-unusable.stderr"
    r11_unusable_exit=$?
    set -e
    test "$r11_unusable_exit" -ne 0
    gate_require_match 'R11 unusable TMPDIR structured diagnostic' \
        "$temporary/r11-unusable.match" \
        "$temporary/r11-unusable.match.err" \
        grep -F 'result=NOT_EVALUATED' "$temporary/r11-unusable.stderr"

    mkdir -p "$temporary/r11-mktemp-shim"
    {
        printf '%s\n' '#!/bin/sh'
        printf '%s\n' 'printf "%s\n" INJECTED_R11_MKTEMP_FAILURE >&2'
        printf '%s\n' 'exit 9'
    } >"$temporary/r11-mktemp-shim/mktemp"
    chmod +x "$temporary/r11-mktemp-shim/mktemp"
    set +e
    env PATH="$temporary/r11-mktemp-shim:$PATH" \
        "$repository/build/run-verify-gate-tool-inventory.sh" \
        "$r11_report" "$r11_receipt" r11-mktemp-shim-token \
        >"$temporary/r11-mktemp.stdout" \
        2>"$temporary/r11-mktemp.stderr"
    r11_mktemp_exit=$?
    set -e
    test "$r11_mktemp_exit" -eq 9
    gate_require_match 'R11 mktemp shim structured diagnostic' \
        "$temporary/r11-mktemp.match" "$temporary/r11-mktemp.match.err" \
        grep -F 'result=NOT_EVALUATED' "$temporary/r11-mktemp.stderr"
    set +e
    "$repository/build/run-verify-gate-tool-inventory.sh" \
        --consume "$r11_report" "$r11_receipt" \
        r11-unusable-tmpdir-token "$r11_unusable_exit" \
        >"$temporary/r11-unusable-consume.stdout" \
        2>"$temporary/r11-unusable-consume.stderr"
    r11_unusable_consume_exit=$?
    "$repository/build/run-verify-gate-tool-inventory.sh" \
        --consume "$r11_report" "$r11_receipt" \
        r11-mktemp-shim-token "$r11_mktemp_exit" \
        >"$temporary/r11-mktemp-consume.stdout" \
        2>"$temporary/r11-mktemp-consume.stderr"
    r11_mktemp_consume_exit=$?
    set -e
    test "$r11_unusable_consume_exit" -ne 0
    test "$r11_mktemp_consume_exit" -ne 0
    gate_sha256_file "$r11_report" "$temporary/r11-report-final"
    test "$GATE_SHA256" = "$r11_report_before"
    gate_sha256_file "$r11_receipt" "$temporary/r11-receipt-final"
    test "$GATE_SHA256" = "$r11_receipt_before"

    printf '%s\n' 'PHASE00_R11_F01_REGRESSION=PASS'
    printf 'FAULT_EXIT=%s\n' "$r11_fault_exit"
    printf 'UNUSABLE_TMPDIR_EXIT=%s\n' "$r11_unusable_exit"
    printf 'MKTEMP_SHIM_EXIT=%s\n' "$r11_mktemp_exit"
    printf 'REPORT_SHA_BEFORE=%s\n' "$r11_report_before"
    printf 'REPORT_SHA_AFTER=%s\n' "$r11_report_after"
    printf 'REPORT_PASS_MARKER_COUNT_BEFORE=%s\n' "$r11_pass_before"
    printf 'REPORT_PASS_MARKER_COUNT_AFTER=%s\n' "$r11_pass_after"
    printf '%s\n' 'CURRENT_TOKEN_NONZERO_EXIT_CONSUME=REJECTED'
    printf '%s\n' 'CURRENT_TOKEN_STALE_RECEIPT_CONSUME=REJECTED'
    printf '%s\n' 'UNUSABLE_TMPDIR_CURRENT_TOKEN_CONSUME=REJECTED'
    printf '%s\n' 'MKTEMP_SHIM_CURRENT_TOKEN_CONSUME=REJECTED'
    printf '%s\n' 'AUTHORITATIVE_SOURCE_MUTATION_COUNT=0'
    exit 0
fi

receipt="$temporary/receipt.txt"
consumer_report="$temporary/consumer-report.txt"
{
    printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS'
    printf '%s\n' 'INVENTORY_ROW_COUNT=54'
    printf '%s\n' 'WRAPPER_LAUNCHER_ACTUAL_CLASS_COUNT=13'
    printf '%s\n' 'WRAPPER_LAUNCHER_CONDITIONAL_CLASS_COUNT=14'
    printf '%s\n' 'PINNED_LAUNCHER_CONTRACT_ROW_COUNT=19'
} >"$consumer_report"
gate_sha256_file "$consumer_report" "$temporary/consumer-report"
consumer_report_sha256=$GATE_SHA256
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf '%s\n' 'INVOCATION_TOKEN=current-token'
    printf 'REPORT_SHA256=%s\n' "$consumer_report_sha256"
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$receipt"

"$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$receipt" current-token 0

wrapper_invocation_sequence=0
run_wrapper_current_at() {
    current_wrapper=$1
    current_report=$2
    wrapper_invocation_sequence=$((wrapper_invocation_sequence + 1))
    case "$current_report" in
        */*)
            current_report_parent=${current_report%/*}
            current_report_basename=${current_report##*/}
            ;;
        *)
            current_report_parent=.
            current_report_basename=$current_report
            ;;
    esac
    current_receipt=\
"$current_report_parent/.$current_report_basename.receipt.$$.$wrapper_invocation_sequence"
    current_token=\
"receipt-test-$$-$wrapper_invocation_sequence"

    if "$current_wrapper" \
        "$current_report" "$current_receipt" "$current_token"; then
        current_producer_exit=0
    else
        current_producer_exit=$?
        if "$current_wrapper" \
            --consume "$current_report" "$current_receipt" \
            "$current_token" "$current_producer_exit"; then
            printf '%s\n' \
                'current invocation consumer false-greened a nonzero producer' \
                >&2
            return 0
        fi
        return "$current_producer_exit"
    fi
    "$current_wrapper" \
        --consume "$current_report" "$current_receipt" \
        "$current_token" "$current_producer_exit"
}

run_wrapper_current() {
    run_wrapper_current_at \
        "$repository/build/run-verify-gate-tool-inventory.sh" "$1"
}

run_wrapper_current_with_path() {
    current_path=$1
    current_wrapper=$2
    current_report=$3
    PATH=$current_path run_wrapper_current_at \
        "$current_wrapper" "$current_report"
}

expected_failure() {
    case_id=$1
    shift
    set +e
    "$@" >"$temporary/$case_id.stdout" 2>"$temporary/$case_id.stderr"
    case_exit=$?
    set -e
    test "$case_exit" -ne 0
    gate_require_match "$case_id structured diagnostic" \
        "$temporary/$case_id.match" "$temporary/$case_id.match.err" \
        grep -F 'result=NOT_EVALUATED' "$temporary/$case_id.stderr"
}

assert_not_pass() {
    assert_not_pass_path=$1
    set +e
    grep -Fx 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
        "$assert_not_pass_path" \
        >"$temporary/not-pass.stdout" 2>"$temporary/not-pass.stderr"
    assert_not_pass_exit=$?
    set -e
    test "$assert_not_pass_exit" -eq 1
}

copy_verifier_fixture() {
    fixture_name=$1
    fixture="$temporary/$fixture_name"
    mkdir -p "$fixture/build" "$fixture/.mvn/wrapper"
    while IFS='	' read -r fixture_source fixture_digest; do
        fixture_source_parent=${fixture_source%/*}
        if [ "$fixture_source_parent" != "$fixture_source" ]; then
            mkdir -p "$fixture/$fixture_source_parent"
        fi
        cp "$repository/$fixture_source" "$fixture/$fixture_source"
    done <"$repository/build/phase-00-gate-source-manifest.tsv"
    cp "$repository/build/phase-00-gate-source-manifest.tsv" \
        "$fixture/build/phase-00-gate-source-manifest.tsv"
    cp "$repository/build/phase-00-gate-tool-inventory.tsv" \
        "$fixture/build/phase-00-gate-tool-inventory.tsv"
    cp "$repository/build/maven-3.9.14-launcher-contract.tsv" \
        "$fixture/build/maven-3.9.14-launcher-contract.tsv"
    cp "$repository/.mvn/wrapper/maven-wrapper.sh.sha256" \
        "$fixture/.mvn/wrapper/maven-wrapper.sh.sha256"
    cp "$repository/.mvn/wrapper/maven-launcher-3.9.14.sha256" \
        "$fixture/.mvn/wrapper/maven-launcher-3.9.14.sha256"
    cp "$repository/.mvn/wrapper/maven-wrapper.properties" \
        "$fixture/.mvn/wrapper/maven-wrapper.properties"
    chmod +x \
        "$fixture/build/verify-gate-tool-inventory.sh" \
        "$fixture/build/run-verify-gate-tool-inventory.sh"
    GATE_INVENTORY_FIXTURE=$fixture
}

expected_failure wrong-token \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$receipt" wrong-token 0
expected_failure old-token \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$receipt" old-token 0
expected_failure producer-nonzero \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$receipt" current-token 9

missing_token_receipt="$temporary/missing-token-receipt.txt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf 'REPORT_SHA256=%s\n' "$consumer_report_sha256"
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$missing_token_receipt"
expected_failure missing-token \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$missing_token_receipt" current-token 0

old_receipt="$temporary/old-receipt.txt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf '%s\n' 'INVOCATION_TOKEN=old-invocation-token'
    printf 'REPORT_SHA256=%s\n' "$consumer_report_sha256"
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$old_receipt"
expected_failure old-receipt-replay \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$old_receipt" current-token 0

missing_digest_receipt="$temporary/missing-digest-receipt.txt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf '%s\n' 'INVOCATION_TOKEN=current-token'
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$missing_digest_receipt"
expected_failure missing-report-digest \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$missing_digest_receipt" current-token 0

duplicate_digest_receipt="$temporary/duplicate-digest-receipt.txt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf '%s\n' 'INVOCATION_TOKEN=current-token'
    printf 'REPORT_SHA256=%s\n' "$consumer_report_sha256"
    printf 'REPORT_SHA256=%s\n' "$consumer_report_sha256"
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$duplicate_digest_receipt"
expected_failure duplicate-report-digest \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$duplicate_digest_receipt" current-token 0

invalid_digest_receipt="$temporary/invalid-digest-receipt.txt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf '%s\n' 'INVOCATION_TOKEN=current-token'
    printf '%s\n' 'REPORT_SHA256=not-a-canonical-sha256'
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$invalid_digest_receipt"
expected_failure invalid-report-digest \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$invalid_digest_receipt" current-token 0

wrong_order_receipt="$temporary/wrong-order-receipt.txt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf 'REPORT_SHA256=%s\n' "$consumer_report_sha256"
    printf '%s\n' 'INVOCATION_TOKEN=current-token'
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$wrong_order_receipt"
expected_failure wrong-receipt-field-order \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$wrong_order_receipt" current-token 0

wrong_digest_receipt="$temporary/wrong-digest-receipt.txt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf '%s\n' 'INVOCATION_TOKEN=current-token'
    printf '%s\n' \
        'REPORT_SHA256=0000000000000000000000000000000000000000000000000000000000000000'
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$wrong_digest_receipt"
expected_failure wrong-report-digest \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$wrong_digest_receipt" current-token 0

exchange_a_report="$temporary/exchange-a-report.txt"
exchange_b_report="$temporary/exchange-b-report.txt"
for exchange_pair in A B; do
    case "$exchange_pair" in
        A) exchange_report=$exchange_a_report ;;
        B) exchange_report=$exchange_b_report ;;
    esac
    {
        printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS'
        printf '%s\n' 'INVENTORY_ROW_COUNT=54'
        printf '%s\n' 'WRAPPER_LAUNCHER_ACTUAL_CLASS_COUNT=13'
        printf '%s\n' 'WRAPPER_LAUNCHER_CONDITIONAL_CLASS_COUNT=14'
        printf '%s\n' 'PINNED_LAUNCHER_CONTRACT_ROW_COUNT=19'
        printf 'VALID_REPORT_VARIANT=%s\n' "$exchange_pair"
    } >"$exchange_report"
done
gate_sha256_file "$exchange_a_report" "$temporary/exchange-a-report"
exchange_a_sha256=$GATE_SHA256
gate_sha256_file "$exchange_b_report" "$temporary/exchange-b-report"
exchange_b_sha256=$GATE_SHA256
exchange_a_receipt="$temporary/exchange-a-receipt.txt"
exchange_b_receipt="$temporary/exchange-b-receipt.txt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf '%s\n' 'INVOCATION_TOKEN=exchange-token'
    printf 'REPORT_SHA256=%s\n' "$exchange_a_sha256"
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$exchange_a_receipt"
{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf '%s\n' 'INVOCATION_TOKEN=exchange-token'
    printf 'REPORT_SHA256=%s\n' "$exchange_b_sha256"
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$exchange_b_receipt"
"$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$exchange_a_report" "$exchange_a_receipt" exchange-token 0
"$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$exchange_b_report" "$exchange_b_receipt" exchange-token 0
expected_failure exchange-a-report-b-receipt \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$exchange_a_report" "$exchange_b_receipt" exchange-token 0
expected_failure exchange-b-report-a-receipt \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$exchange_b_report" "$exchange_a_receipt" exchange-token 0

printf '%s\n' 'MARKER_PRESERVING_REPORT_MUTATION=INJECTED' \
    >>"$consumer_report"
expected_failure marker-preserving-report-mutation \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$consumer_report" "$receipt" current-token 0

mkdir -p "$temporary/consumer-shasum-fault-shim"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' \
        'printf "%s\n" INJECTED_CONSUMER_REPORT_HASH_FAILURE >&2'
    printf '%s\n' 'exit 9'
} >"$temporary/consumer-shasum-fault-shim/shasum"
chmod +x "$temporary/consumer-shasum-fault-shim/shasum"
expected_failure consumer-report-hash-failure \
    env PATH="$temporary/consumer-shasum-fault-shim:$PATH" \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$exchange_a_report" "$exchange_a_receipt" exchange-token 0

report="$temporary/report.txt"
run_wrapper_current "$report"
gate_require_match 'current wrapper report PASS' \
    "$temporary/current-report.match" "$temporary/current-report.match.err" \
    grep -Fx 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' "$report"

real_shasum=/usr/bin/shasum
test -x "$real_shasum"
mkdir -p "$temporary/producer-shasum-fault-shim"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'for argument in "$@"; do'
    printf '%s\n' \
        '    case "$argument" in */ro-next-phase00-tool-inventory.*/report.txt) printf "%s\n" INJECTED_PRODUCER_REPORT_HASH_FAILURE >&2; exit 9 ;; esac'
    printf '%s\n' 'done'
    printf 'exec %s "$@"\n' "$real_shasum"
} >"$temporary/producer-shasum-fault-shim/shasum"
chmod +x "$temporary/producer-shasum-fault-shim/shasum"
producer_hash_failure_report="$temporary/producer-hash-failure-report.txt"
expected_failure producer-report-hash-failure \
    run_wrapper_current_with_path \
    "$temporary/producer-shasum-fault-shim:$PATH" \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$producer_hash_failure_report"
assert_not_pass "$producer_hash_failure_report"

inventory="$repository/build/phase-00-gate-tool-inventory.tsv"
gate_sha256_file "$inventory" "$temporary/inventory-before"
inventory_before=$GATE_SHA256

expected_failure exact-source-alias \
    run_wrapper_current "$inventory"
gate_sha256_file "$inventory" "$temporary/inventory-after-exact"
test "$GATE_SHA256" = "$inventory_before"

canonical_alias="$repository/build/../build/phase-00-gate-tool-inventory.tsv"
expected_failure canonical-source-alias \
    run_wrapper_current "$canonical_alias"
gate_sha256_file "$inventory" "$temporary/inventory-after-canonical"
test "$GATE_SHA256" = "$inventory_before"

symlink_alias="$temporary/inventory-symlink"
ln -s "$inventory" "$symlink_alias"
expected_failure symlink-source-alias \
    run_wrapper_current "$symlink_alias"
gate_sha256_file "$inventory" "$temporary/inventory-after-symlink"
test "$GATE_SHA256" = "$inventory_before"

hardlink_alias="$temporary/inventory-hardlink"
ln "$inventory" "$hardlink_alias"
expected_failure hardlink-source-alias \
    run_wrapper_current "$hardlink_alias"
gate_sha256_file "$inventory" "$temporary/inventory-after-hardlink"
test "$GATE_SHA256" = "$inventory_before"

receipt_alias_report=\
"$repository/build/.phase00-receipt-alias-report.$$"
expected_failure exact-receipt-source-alias \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$receipt_alias_report" "$inventory" receipt-exact-token
gate_sha256_file "$inventory" "$temporary/inventory-after-receipt-exact"
test "$GATE_SHA256" = "$inventory_before"
expected_failure canonical-receipt-source-alias \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$receipt_alias_report" "$canonical_alias" receipt-canonical-token
gate_sha256_file "$inventory" "$temporary/inventory-after-receipt-canonical"
test "$GATE_SHA256" = "$inventory_before"
receipt_alias_report="$temporary/receipt-alias-report.txt"
expected_failure symlink-receipt-source-alias \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$receipt_alias_report" "$symlink_alias" receipt-symlink-token
gate_sha256_file "$inventory" "$temporary/inventory-after-receipt-symlink"
test "$GATE_SHA256" = "$inventory_before"
expected_failure hardlink-receipt-source-alias \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$receipt_alias_report" "$hardlink_alias" receipt-hardlink-token
gate_sha256_file "$inventory" "$temporary/inventory-after-receipt-hardlink"
test "$GATE_SHA256" = "$inventory_before"
test ! -e "$repository/build/.phase00-receipt-alias-report.$$"

benign_target="$temporary/benign-target.txt"
printf '%s\n' 'BENIGN_TARGET_UNCHANGED' >"$benign_target"
gate_sha256_file "$benign_target" "$temporary/benign-before"
benign_before=$GATE_SHA256
benign_symlink="$temporary/benign-symlink-report.txt"
ln -s "$benign_target" "$benign_symlink"
run_wrapper_current "$benign_symlink"
gate_sha256_file "$benign_target" "$temporary/benign-after-symlink"
test "$GATE_SHA256" = "$benign_before"
test ! -L "$benign_symlink"

benign_hardlink="$temporary/benign-hardlink-report.txt"
ln "$benign_target" "$benign_hardlink"
run_wrapper_current "$benign_hardlink"
gate_sha256_file "$benign_target" "$temporary/benign-after-hardlink"
test "$GATE_SHA256" = "$benign_before"
gate_sha256_file "$benign_hardlink" "$temporary/benign-report"
test "$GATE_SHA256" != "$benign_before"

benign_receipt_symlink="$temporary/benign-symlink-receipt.txt"
benign_receipt_symlink_report="$temporary/benign-symlink-receipt-report.txt"
ln -s "$benign_target" "$benign_receipt_symlink"
"$repository/build/run-verify-gate-tool-inventory.sh" \
    "$benign_receipt_symlink_report" "$benign_receipt_symlink" \
    benign-receipt-symlink-token
"$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$benign_receipt_symlink_report" "$benign_receipt_symlink" \
    benign-receipt-symlink-token 0
gate_sha256_file "$benign_target" "$temporary/benign-after-receipt-symlink"
test "$GATE_SHA256" = "$benign_before"
test ! -L "$benign_receipt_symlink"

benign_receipt_hardlink="$temporary/benign-hardlink-receipt.txt"
benign_receipt_hardlink_report="$temporary/benign-hardlink-receipt-report.txt"
ln "$benign_target" "$benign_receipt_hardlink"
"$repository/build/run-verify-gate-tool-inventory.sh" \
    "$benign_receipt_hardlink_report" "$benign_receipt_hardlink" \
    benign-receipt-hardlink-token
"$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$benign_receipt_hardlink_report" "$benign_receipt_hardlink" \
    benign-receipt-hardlink-token 0
gate_sha256_file "$benign_target" "$temporary/benign-after-receipt-hardlink"
test "$GATE_SHA256" = "$benign_before"

printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
    >"$temporary/stale-report.txt"
mkdir -p "$temporary/cp-fault-shim"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'printf "%s\n" INJECTED_CP_FAILURE >&2'
    printf '%s\n' 'exit 9'
} >"$temporary/cp-fault-shim/cp"
chmod +x "$temporary/cp-fault-shim/cp"
expected_failure stale-pass-cp-failure \
    run_wrapper_current_with_path \
    "$temporary/cp-fault-shim:$PATH" \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$temporary/stale-report.txt"
gate_sha256_file "$inventory" "$temporary/inventory-after-cp"
test "$GATE_SHA256" = "$inventory_before"

copy_verifier_fixture semantic-failure-repository
semantic_fixture=$GATE_INVENTORY_FIXTURE
semantic_report="$temporary/semantic-stale-report.txt"
run_wrapper_current_at \
    "$semantic_fixture/build/run-verify-gate-tool-inventory.sh" \
    "$semantic_report"
printf '%s\n' \
    'unexpected-tool	GATE_CRITICAL	nonzero=NOT_EVALUATED	fixture	fixture' \
    >>"$semantic_fixture/build/phase-00-gate-tool-inventory.tsv"
gate_sha256_file \
    "$semantic_fixture/build/phase-00-gate-tool-inventory.tsv" \
    "$temporary/semantic-source-before"
semantic_source_before=$GATE_SHA256
expected_failure semantic-stale-pass \
    run_wrapper_current_at \
    "$semantic_fixture/build/run-verify-gate-tool-inventory.sh" \
    "$semantic_report"
assert_not_pass "$semantic_report"
gate_sha256_file \
    "$semantic_fixture/build/phase-00-gate-tool-inventory.tsv" \
    "$temporary/semantic-source-after"
test "$GATE_SHA256" = "$semantic_source_before"

mkdir -p "$temporary/receipt-cp-fault-shim"
real_cp=/bin/cp
test -x "$real_cp"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'for argument in "$@"; do'
    printf '%s\n' \
        '    case "$argument" in *invocation-receipt*) printf "%s\n" INJECTED_RECEIPT_CP_FAILURE >&2; exit 9 ;; esac'
    printf '%s\n' 'done'
    printf 'exec %s "$@"\n' "$real_cp"
} >"$temporary/receipt-cp-fault-shim/cp"
chmod +x "$temporary/receipt-cp-fault-shim/cp"
receipt_cp_report="$temporary/receipt-cp-report.txt"
printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
    >"$receipt_cp_report"
expected_failure receipt-write-cp-failure \
    run_wrapper_current_with_path \
    "$temporary/receipt-cp-fault-shim:$PATH" \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$receipt_cp_report"
assert_not_pass "$receipt_cp_report"

mkdir -p "$temporary/mv-fault-shim"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'printf "%s\n" INJECTED_MV_FAILURE >&2'
    printf '%s\n' 'exit 9'
} >"$temporary/mv-fault-shim/mv"
chmod +x "$temporary/mv-fault-shim/mv"
mv_failure_report="$temporary/mv-failure-report.txt"
printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
    >"$mv_failure_report"
expected_failure invalidation-rename-failure \
    run_wrapper_current_with_path \
    "$temporary/mv-fault-shim:$PATH" \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$mv_failure_report"

mkdir -p "$temporary/final-mv-fault-shim"
real_mv=/bin/mv
test -x "$real_mv"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'for argument in "$@"; do'
    printf '%s\n' \
        '    case "$argument" in *.pass.*) printf "%s\n" INJECTED_FINAL_MV_FAILURE >&2; exit 9 ;; esac'
    printf '%s\n' 'done'
    printf 'exec %s "$@"\n' "$real_mv"
} >"$temporary/final-mv-fault-shim/mv"
chmod +x "$temporary/final-mv-fault-shim/mv"
final_mv_report="$temporary/final-mv-report.txt"
expected_failure final-rename-failure \
    run_wrapper_current_with_path \
    "$temporary/final-mv-fault-shim:$PATH" \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$final_mv_report"
assert_not_pass "$final_mv_report"

mkdir -p "$temporary/final-receipt-mv-fault-shim"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'for argument in "$@"; do'
    printf '%s\n' \
        '    case "$argument" in *.receipt.*.pass.*) printf "%s\n" INJECTED_FINAL_RECEIPT_MV_FAILURE >&2; exit 9 ;; esac'
    printf '%s\n' 'done'
    printf 'exec %s "$@"\n' "$real_mv"
} >"$temporary/final-receipt-mv-fault-shim/mv"
chmod +x "$temporary/final-receipt-mv-fault-shim/mv"
final_receipt_mv_report="$temporary/final-receipt-mv-report.txt"
expected_failure final-receipt-rename-failure \
    run_wrapper_current_with_path \
    "$temporary/final-receipt-mv-fault-shim:$PATH" \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$final_receipt_mv_report"
gate_require_match 'final receipt failure may leave deterministic report only' \
    "$temporary/final-receipt-report.match" \
    "$temporary/final-receipt-report.match.err" \
    grep -Fx 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
    "$final_receipt_mv_report"

permission_parent="$temporary/permission-parent"
mkdir -p "$permission_parent"
permission_report="$permission_parent/report.txt"
printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
    >"$permission_report"
chmod 555 "$permission_parent"
expected_failure report-parent-permission-failure \
    run_wrapper_current "$permission_report"
chmod 755 "$permission_parent"

mkdir -p "$temporary/rm-cleanup-fault-shim"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'printf "%s\n" INJECTED_CLEANUP_RM_FAILURE >&2'
    printf '%s\n' 'exit 9'
} >"$temporary/rm-cleanup-fault-shim/rm"
chmod +x "$temporary/rm-cleanup-fault-shim/rm"
cleanup_failure_report="$temporary/cleanup-failure-report.txt"
expected_failure cleanup-failure \
    run_wrapper_current_with_path \
    "$temporary/rm-cleanup-fault-shim:$PATH" \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    "$cleanup_failure_report"
assert_not_pass "$cleanup_failure_report"

concurrent_report="$temporary/concurrent-report.txt"
concurrent_a_receipt="$temporary/concurrent-a-receipt.txt"
concurrent_b_receipt="$temporary/concurrent-b-receipt.txt"
set +e
"$repository/build/run-verify-gate-tool-inventory.sh" \
    "$concurrent_report" "$concurrent_a_receipt" concurrent-token-a \
    >"$temporary/concurrent-a.stdout" 2>"$temporary/concurrent-a.stderr" &
concurrent_a=$!
"$repository/build/run-verify-gate-tool-inventory.sh" \
    "$concurrent_report" "$concurrent_b_receipt" concurrent-token-b \
    >"$temporary/concurrent-b.stdout" 2>"$temporary/concurrent-b.stderr" &
concurrent_b=$!
wait "$concurrent_a"
concurrent_a_exit=$?
wait "$concurrent_b"
concurrent_b_exit=$?
set -e
test "$concurrent_a_exit" -eq 0
test "$concurrent_b_exit" -eq 0
"$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$concurrent_report" "$concurrent_a_receipt" \
    concurrent-token-a "$concurrent_a_exit"
"$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$concurrent_report" "$concurrent_b_receipt" \
    concurrent-token-b "$concurrent_b_exit"
gate_require_match 'concurrent wrapper report PASS' \
    "$temporary/concurrent.match" "$temporary/concurrent.match.err" \
    grep -Fx 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
    "$concurrent_report"
gate_sha256_file "$inventory" "$temporary/inventory-after-concurrent"
test "$GATE_SHA256" = "$inventory_before"

real_awk=/usr/bin/awk
test -x "$real_awk"
make_blocking_awk() {
    blocking_name=$1
    blocking_directory="$temporary/$blocking_name"
    mkdir -p "$blocking_directory"
    mkfifo \
        "$blocking_directory/ready.fifo" \
        "$blocking_directory/release.fifo"
    {
        printf '%s\n' '#!/bin/sh'
        printf '%s\n' 'if [ ! -e "$PHASE00_BLOCK_STATE" ]; then'
        printf '%s\n' '    : >"$PHASE00_BLOCK_STATE"'
        printf '%s\n' '    printf "%s\n" ready >"$PHASE00_BLOCK_READY"'
        printf '%s\n' \
            '    IFS= read -r phase00_release <"$PHASE00_BLOCK_RELEASE"'
        printf '%s\n' 'fi'
        printf 'exec %s "$@"\n' "$real_awk"
    } >"$blocking_directory/awk"
    chmod +x "$blocking_directory/awk"
    GATE_BLOCKING_DIRECTORY=$blocking_directory
}

make_blocking_awk report-replacement-block
report_block=$GATE_BLOCKING_DIRECTORY
report_replace_target="$temporary/report-replace-target.txt"
printf '%s\n' 'REPORT_REPLACE_TARGET_UNCHANGED' >"$report_replace_target"
gate_sha256_file "$report_replace_target" "$temporary/report-replace-before"
report_replace_before=$GATE_SHA256
report_replace_path="$temporary/report-replace.txt"
set +e
PHASE00_BLOCK_STATE="$report_block/state" \
PHASE00_BLOCK_READY="$report_block/ready.fifo" \
PHASE00_BLOCK_RELEASE="$report_block/release.fifo" \
PATH="$report_block:$PATH" \
    run_wrapper_current \
    "$report_replace_path" \
    >"$temporary/report-replace.stdout" \
    2>"$temporary/report-replace.stderr" &
report_replace_pid=$!
IFS= read -r report_ready <"$report_block/ready.fifo"
test "$report_ready" = ready
mv "$report_replace_path" "$temporary/report-replace-invalidated.txt"
ln -s "$report_replace_target" "$report_replace_path"
printf '%s\n' release >"$report_block/release.fifo"
wait "$report_replace_pid"
report_replace_exit=$?
set -e
test "$report_replace_exit" -eq 0
test ! -L "$report_replace_path"
gate_sha256_file "$report_replace_target" "$temporary/report-replace-after"
test "$GATE_SHA256" = "$report_replace_before"
gate_require_match 'report replacement current PASS' \
    "$temporary/report-replace.match" \
    "$temporary/report-replace.match.err" \
    grep -Fx 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
    "$report_replace_path"

make_blocking_awk parent-replacement-block
parent_block=$GATE_BLOCKING_DIRECTORY
parent_current="$temporary/parent-current"
parent_held="$temporary/parent-held"
mkdir -p "$parent_current"
parent_report="$parent_current/report.txt"
set +e
PHASE00_BLOCK_STATE="$parent_block/state" \
PHASE00_BLOCK_READY="$parent_block/ready.fifo" \
PHASE00_BLOCK_RELEASE="$parent_block/release.fifo" \
PATH="$parent_block:$PATH" \
    run_wrapper_current \
    "$parent_report" \
    >"$temporary/parent-replace.stdout" \
    2>"$temporary/parent-replace.stderr" &
parent_replace_pid=$!
IFS= read -r parent_ready <"$parent_block/ready.fifo"
test "$parent_ready" = ready
mv "$parent_current" "$parent_held"
mkdir -p "$parent_current"
printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS' \
    >"$parent_current/report.txt"
printf '%s\n' release >"$parent_block/release.fifo"
wait "$parent_replace_pid"
parent_replace_exit=$?
set -e
test "$parent_replace_exit" -ne 0
gate_require_match 'parent replacement structured diagnostic' \
    "$temporary/parent-replace.match" \
    "$temporary/parent-replace.match.err" \
    grep -F 'reason=REPORT_PARENT_REPLACED' \
    "$temporary/parent-replace.stderr"
assert_not_pass "$parent_held/report.txt"

make_blocking_awk signal-block
signal_block=$GATE_BLOCKING_DIRECTORY
signal_report="$temporary/signal-report.txt"
signal_receipt="$temporary/signal-receipt.txt"
set +e
env \
    PHASE00_BLOCK_STATE="$signal_block/state" \
    PHASE00_BLOCK_READY="$signal_block/ready.fifo" \
    PHASE00_BLOCK_RELEASE="$signal_block/release.fifo" \
    PATH="$signal_block:$PATH" \
    "$repository/build/verify-gate-tool-inventory.sh" \
    "$signal_report" "$signal_receipt" signal-token \
    >"$temporary/signal.stdout" 2>"$temporary/signal.stderr" &
signal_pid=$!
IFS= read -r signal_ready <"$signal_block/ready.fifo"
test "$signal_ready" = ready
kill -TERM "$signal_pid"
printf '%s\n' release >"$signal_block/release.fifo"
wait "$signal_pid"
signal_exit=$?
set -e
test "$signal_exit" -ne 0
assert_not_pass "$signal_report"
expected_failure signal-receipt-consumer \
    "$repository/build/run-verify-gate-tool-inventory.sh" \
    --consume "$signal_report" "$signal_receipt" \
    signal-token "$signal_exit"

gate_sha256_file "$inventory" "$temporary/inventory-final"
test "$GATE_SHA256" = "$inventory_before"

printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT_TEST=PASS'
printf '%s\n' 'CONSUMER_CORRECT_TOKEN_POSITIVE_COUNT=1'
printf '%s\n' 'CONSUMER_WRONG_OLD_MISSING_TOKEN_NEGATIVE_COUNT=3'
printf '%s\n' 'CONSUMER_NONZERO_EXIT_NEGATIVE_COUNT=1'
printf '%s\n' 'OLD_RECEIPT_REPLAY_NEGATIVE_COUNT=1'
printf '%s\n' 'STRICT_DIGEST_SCHEMA_NEGATIVE_COUNT=4'
printf '%s\n' 'DIGEST_VALUE_MISMATCH_NEGATIVE_COUNT=1'
printf '%s\n' 'DIGEST_BOUND_VALID_PAIR_POSITIVE_COUNT=2'
printf '%s\n' 'REPORT_RECEIPT_EXCHANGE_NEGATIVE_COUNT=2'
printf '%s\n' 'MARKER_PRESERVING_MUTATION_NEGATIVE_COUNT=1'
printf '%s\n' 'REPORT_HASH_TOOL_FAILURE_NEGATIVE_COUNT=2'
printf '%s\n' 'PROTECTED_ALIAS_NEGATIVE_COUNT=8'
printf '%s\n' 'BENIGN_ALIAS_SOURCE_UNCHANGED_COUNT=4'
printf '%s\n' 'STALE_PASS_FAILURE_NEGATIVE_COUNT=1'
printf '%s\n' 'CONCURRENT_INVOCATION_COUNT=2'
printf '%s\n' 'SEMANTIC_FAILURE_NEGATIVE_COUNT=1'
printf '%s\n' 'STAGE_WRITE_PERMISSION_RENAME_FAILURE_COUNT=6'
printf '%s\n' 'CLEANUP_FAILURE_NEGATIVE_COUNT=1'
printf '%s\n' 'REPORT_PATH_REPLACEMENT_SAFE_COUNT=1'
printf '%s\n' 'REPORT_PARENT_REPLACEMENT_NEGATIVE_COUNT=1'
printf '%s\n' 'SIGNAL_FAILURE_NEGATIVE_COUNT=1'
printf '%s\n' 'AUTHORITATIVE_SOURCE_MUTATION_COUNT=0'
