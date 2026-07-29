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

inventory="$repository/build/phase-00-gate-tool-inventory.tsv"
launcher_contract="$repository/build/maven-3.9.14-launcher-contract.tsv"
wrapper_pin="$repository/.mvn/wrapper/maven-wrapper.sh.sha256"
launcher_pin="$repository/.mvn/wrapper/maven-launcher-3.9.14.sha256"
wrapper_properties="$repository/.mvn/wrapper/maven-wrapper.properties"
source_manifest="$repository/build/phase-00-gate-source-manifest.tsv"

report=${1:-}
receipt=${2:-}
invocation_token=${3:-}

gate_inventory_usage_failure() {
    printf '%s\n' \
        'tool=gate-tool-inventory-invocation-contract result=NOT_EVALUATED exit=64 reason=EXPECTED_REPORT_RECEIPT_TOKEN' \
        >&2
    exit 64
}

test "$#" -eq 3 || gate_inventory_usage_failure
case "$invocation_token" in
    ''|*[!A-Za-z0-9._-]*)
        printf '%s\n' \
            'tool=gate-tool-inventory-invocation-contract result=NOT_EVALUATED exit=64 reason=INVALID_TOKEN' \
            >&2
        exit 64
        ;;
esac

gate_inventory_reject_output() {
    gate_inventory_reject_label=$1
    gate_inventory_reject_reason=$2
    gate_inventory_reject_path=$3
    printf 'tool=gate-tool-inventory-output-guard result=NOT_EVALUATED exit=64 output=%s reason=%s path=%s\n' \
        "$gate_inventory_reject_label" "$gate_inventory_reject_reason" \
        "$gate_inventory_reject_path" >&2
    exit 64
}

gate_inventory_compare_protected() {
    gate_inventory_output=$1
    gate_inventory_output_canonical=$2
    gate_inventory_output_label=$3
    gate_inventory_protected=$4

    if [ ! -e "$gate_inventory_protected" ] &&
        [ ! -L "$gate_inventory_protected" ]; then
        return 0
    fi
    case "$gate_inventory_protected" in
        */*)
            gate_inventory_protected_parent=${gate_inventory_protected%/*}
            gate_inventory_protected_basename=${gate_inventory_protected##*/}
            ;;
        *)
            gate_inventory_protected_parent=.
            gate_inventory_protected_basename=$gate_inventory_protected
            ;;
    esac
    if gate_inventory_protected_parent_physical=$(
        CDPATH= cd -P -- "$gate_inventory_protected_parent" && pwd -P
    ); then
        :
    else
        gate_inventory_reject_output "$gate_inventory_output_label" \
            PROTECTED_PARENT_UNRESOLVED "$gate_inventory_protected"
    fi
    gate_inventory_protected_canonical=\
"$gate_inventory_protected_parent_physical/$gate_inventory_protected_basename"
    if [ "$gate_inventory_output_canonical" = \
        "$gate_inventory_protected_canonical" ]; then
        gate_inventory_reject_output "$gate_inventory_output_label" \
            CANONICAL_PROTECTED_ALIAS "$gate_inventory_protected"
    fi
    if { [ -e "$gate_inventory_output" ] || [ -L "$gate_inventory_output" ]; } &&
        [ "$gate_inventory_output" -ef "$gate_inventory_protected" ]; then
        gate_inventory_reject_output "$gate_inventory_output_label" \
            PHYSICAL_PROTECTED_ALIAS "$gate_inventory_protected"
    fi
}

gate_inventory_guard_output() {
    gate_inventory_guard_path=$1
    gate_inventory_guard_label=$2
    case "$gate_inventory_guard_path" in
        */*)
            gate_inventory_guard_parent=${gate_inventory_guard_path%/*}
            gate_inventory_guard_basename=${gate_inventory_guard_path##*/}
            ;;
        *)
            gate_inventory_guard_parent=.
            gate_inventory_guard_basename=$gate_inventory_guard_path
            ;;
    esac
    case "$gate_inventory_guard_basename" in
        ''|.|..)
            gate_inventory_reject_output "$gate_inventory_guard_label" \
                INVALID_BASENAME "$gate_inventory_guard_path"
            ;;
    esac
    if gate_inventory_guard_parent_physical=$(
        CDPATH= cd -P -- "$gate_inventory_guard_parent" && pwd -P
    ); then
        :
    else
        gate_inventory_reject_output "$gate_inventory_guard_label" \
            OUTPUT_PARENT_UNRESOLVED "$gate_inventory_guard_path"
    fi
    gate_inventory_guard_canonical=\
"$gate_inventory_guard_parent_physical/$gate_inventory_guard_basename"
    if [ -d "$gate_inventory_guard_path" ]; then
        gate_inventory_reject_output "$gate_inventory_guard_label" \
            OUTPUT_IS_DIRECTORY "$gate_inventory_guard_path"
    fi
    if { [ -e "$gate_inventory_guard_path" ] ||
        [ -L "$gate_inventory_guard_path" ]; } &&
        [ ! -f "$gate_inventory_guard_path" ] &&
        [ ! -L "$gate_inventory_guard_path" ]; then
        gate_inventory_reject_output "$gate_inventory_guard_label" \
            OUTPUT_IS_NOT_REGULAR_OR_LINK "$gate_inventory_guard_path"
    fi

    for gate_inventory_static_protected in \
        "$inventory" "$launcher_contract" "$wrapper_pin" "$launcher_pin" \
        "$wrapper_properties" "$source_manifest" "$repository/mvnw" \
        "$repository/target/phase-00-evidence/evidence-manifest.tsv" \
        "$repository/target/phase-00-evidence/evidence-manifest.sha256" \
        "$repository/target/phase-00-evidence/pre-review-evidence-manifest.tsv" \
        "$repository/target/phase-00-evidence/pre-review-evidence-manifest.sha256"
    do
        gate_inventory_compare_protected "$gate_inventory_guard_path" \
            "$gate_inventory_guard_canonical" "$gate_inventory_guard_label" \
            "$gate_inventory_static_protected"
    done
    if [ -f "$source_manifest" ]; then
        while IFS='	' read -r gate_inventory_source_path \
            gate_inventory_source_digest; do
            test -n "$gate_inventory_source_path" || continue
            gate_inventory_compare_protected "$gate_inventory_guard_path" \
                "$gate_inventory_guard_canonical" \
                "$gate_inventory_guard_label" \
                "$repository/$gate_inventory_source_path"
        done <"$source_manifest"
    fi
    GATE_INVENTORY_OUTPUT_CANONICAL=$gate_inventory_guard_canonical
}

gate_inventory_guard_output "$report" report
report_canonical=$GATE_INVENTORY_OUTPUT_CANONICAL
gate_inventory_guard_output "$receipt" receipt
receipt_canonical=$GATE_INVENTORY_OUTPUT_CANONICAL
if [ "$report_canonical" = "$receipt_canonical" ] ||
    { { [ -e "$report" ] || [ -L "$report" ]; } &&
      { [ -e "$receipt" ] || [ -L "$receipt" ]; } &&
      [ "$report" -ef "$receipt" ]; }; then
    gate_inventory_reject_output receipt REPORT_RECEIPT_ALIAS "$receipt"
fi

gate_make_temporary_directory 'mktemp(gate-tool-inventory)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-tool-inventory.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
gate_inventory_contract_active=0

gate_inventory_write_failure_state() {
    gate_inventory_failure_exit=$1
    {
        printf '%s\n' \
            'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=NOT_EVALUATED'
        printf '%s\n' 'INVOCATION_RESULT=NOT_EVALUATED'
    } >"$temporary/report-not-evaluated.txt"
    {
        printf '%s\n' \
            'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=NOT_EVALUATED'
        printf 'INVOCATION_TOKEN=%s\n' "$invocation_token"
        printf '%s\n' 'REPORT_SHA256=NOT_EVALUATED'
        printf '%s\n' 'PRODUCER_RESULT=NOT_EVALUATED'
        printf 'PRODUCER_EXIT_CODE=%s\n' "$gate_inventory_failure_exit"
    } >"$temporary/receipt-not-evaluated.txt"
}

gate_inventory_exit() {
    gate_inventory_primary_exit=$?
    trap - EXIT HUP INT TERM
    set +e
    if [ "$gate_inventory_primary_exit" -ne 0 ] &&
        [ "$gate_inventory_contract_active" -eq 1 ] &&
        [ -d "$temporary" ]; then
        gate_inventory_write_failure_state "$gate_inventory_primary_exit"
        gate_publish_file_atomic 'gate-tool-inventory-failure-report' \
            "$temporary/report-not-evaluated.txt" "$report" \
            "$temporary/failure-report-publish" >/dev/null 2>&1 || :
        gate_publish_file_atomic 'gate-tool-inventory-failure-receipt' \
            "$temporary/receipt-not-evaluated.txt" "$receipt" \
            "$temporary/failure-receipt-publish" >/dev/null 2>&1 || :
    fi
    gate_inventory_cleanup_exit=0
    if [ -d "$temporary" ]; then
        if command rm -rf -- "$temporary" >/dev/null 2>&1; then
            :
        else
            gate_inventory_cleanup_exit=$?
            printf 'tool=rm(cleanup:gate-tool-inventory) result=NOT_EVALUATED exit=%s\n' \
                "$gate_inventory_cleanup_exit" >&2
        fi
    fi
    if [ "$gate_inventory_cleanup_exit" -ne 0 ] &&
        [ "$gate_inventory_primary_exit" -eq 0 ]; then
        gate_inventory_primary_exit=$gate_inventory_cleanup_exit
        if [ "$gate_inventory_contract_active" -eq 1 ] &&
            [ -d "$temporary" ]; then
            gate_inventory_write_failure_state "$gate_inventory_primary_exit"
            gate_publish_file_atomic \
                'gate-tool-inventory-cleanup-failure-report' \
                "$temporary/report-not-evaluated.txt" "$report" \
                "$temporary/cleanup-failure-report-publish" \
                >/dev/null 2>&1 || :
            gate_publish_file_atomic \
                'gate-tool-inventory-cleanup-failure-receipt' \
                "$temporary/receipt-not-evaluated.txt" "$receipt" \
                "$temporary/cleanup-failure-receipt-publish" \
                >/dev/null 2>&1 || :
        fi
    fi
    if [ "$gate_inventory_primary_exit" -ne 0 ]; then
        printf 'tool=gate-tool-inventory-verifier result=NOT_EVALUATED exit=%s token=%s\n' \
            "$gate_inventory_primary_exit" "$invocation_token" >&2
    fi
    exit "$gate_inventory_primary_exit"
}

gate_inventory_signal() {
    gate_inventory_signal_name=$1
    gate_inventory_signal_exit=$2
    printf 'tool=signal(%s:gate-tool-inventory) result=NOT_EVALUATED exit=%s\n' \
        "$gate_inventory_signal_name" "$gate_inventory_signal_exit" >&2
    exit "$gate_inventory_signal_exit"
}

trap gate_inventory_exit EXIT
trap 'gate_inventory_signal HUP 129' HUP
trap 'gate_inventory_signal INT 130' INT
trap 'gate_inventory_signal TERM 143' TERM

gate_inventory_write_failure_state PENDING
gate_inventory_contract_active=1
gate_publish_file_atomic 'gate-tool-inventory-initial-report' \
    "$temporary/report-not-evaluated.txt" "$report" \
    "$temporary/initial-report-publish"
gate_publish_file_atomic 'gate-tool-inventory-initial-receipt' \
    "$temporary/receipt-not-evaluated.txt" "$receipt" \
    "$temporary/initial-receipt-publish"

for required in \
    "$inventory" "$launcher_contract" "$wrapper_pin" "$launcher_pin" \
    "$wrapper_properties" "$source_manifest"
do
    test -f "$required" && test -s "$required"
done

IFS=' ' read -r expected_wrapper_digest expected_wrapper_name <"$wrapper_pin"
test "$expected_wrapper_name" = mvnw
gate_sha256_file "$repository/mvnw" "$temporary/wrapper"
test "$GATE_SHA256" = "$expected_wrapper_digest"

IFS=' ' read -r expected_launcher_digest expected_launcher_name <"$launcher_pin"
test "$expected_launcher_name" = bin/mvn
test "$expected_launcher_digest" = \
    f9381d0cb98abaaf9592dae421eddc497e84ed9bfb723b84c111d1350863c3a2
gate_capture_checked 'awk(maven-launcher-asset-pin-count)' \
    "$temporary/launcher-asset-pin-count" \
    "$temporary/launcher-asset-pin-count.err" \
    awk 'END { print NR + 0 }' "$launcher_pin"
IFS= read -r launcher_asset_pin_count \
    <"$temporary/launcher-asset-pin-count" || :
test "$launcher_asset_pin_count" -eq 3
gate_require_match 'Maven launcher m2.conf pin' \
    "$temporary/m2-conf-pin.match" "$temporary/m2-conf-pin.err" \
    grep -Fx \
    'e336769bf93a902baa7a3e827ba55e4cef7de4af2ed1a9541a4261094d748ba9  bin/m2.conf' \
    "$launcher_pin"
gate_require_match 'Maven Classworlds pin' \
    "$temporary/classworlds-pin.match" "$temporary/classworlds-pin.err" \
    grep -Fx \
    '1ad3292cd563381e3fd632f3fded1988f9e9b2be7a9f3db63ff4c4cedba13fa5  boot/plexus-classworlds-2.9.0.jar' \
    "$launcher_pin"

wrapper_version=
distribution_type=
distribution_url=
distribution_digest=
while IFS='=' read -r key value; do
    case "$key" in
        wrapperVersion) wrapper_version=$value ;;
        distributionType) distribution_type=$value ;;
        distributionUrl) distribution_url=$value ;;
        distributionSha256Sum) distribution_digest=$value ;;
    esac
done <"$wrapper_properties"
test "$wrapper_version" = 3.3.4
test "$distribution_type" = only-script
test "$distribution_url" = \
    https://archive.apache.org/dist/maven/maven-3/3.9.14/binaries/apache-maven-3.9.14-bin.zip
test "$distribution_digest" = \
    55fadd669532a3205d5db95f490bf13971d8b0843526f407f29db0e61f074ab3

actual_tools='
basename
controlled-maven-launcher
controlled-maven-launcher-core
dirname
dynamic-maven-executable
java
maven-classworlds-inventory
maven-launcher-config
mavenrc-source
sh
shasum
tr
uname
'
conditional_tools='
cat
curl
cygpath
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

actual_count=0
conditional_count=0
for tool in $actual_tools; do
    actual_count=$((actual_count + 1))
    gate_capture_checked "awk(inventory-actual:$tool)" \
        "$temporary/$tool.actual-count" "$temporary/$tool.actual-count.err" \
        awk -F '	' -v tool="$tool" \
        '$1 == tool && $2 ~ /ACTUAL|DYNAMIC/ { count++ } END { print count + 0 }' \
        "$inventory"
    IFS= read -r count <"$temporary/$tool.actual-count" || :
    test "$count" -eq 1
done
for tool in $conditional_tools; do
    conditional_count=$((conditional_count + 1))
    gate_capture_checked "awk(inventory-conditional:$tool)" \
        "$temporary/$tool.conditional-count" \
        "$temporary/$tool.conditional-count.err" \
        awk -F '	' -v tool="$tool" \
        '$1 == tool && $2 ~ /CONDITIONAL/ { count++ } END { print count + 0 }' \
        "$inventory"
    IFS= read -r count <"$temporary/$tool.conditional-count" || :
    test "$count" -eq 1
done
test "$actual_count" -eq 13
test "$conditional_count" -eq 14

gate_capture_checked 'awk(inventory-row-count)' \
    "$temporary/inventory-row-count" "$temporary/inventory-row-count.err" \
    awk 'NR > 1 { count++ } END { print count + 0 }' "$inventory"
IFS= read -r inventory_row_count <"$temporary/inventory-row-count" || :
test "$inventory_row_count" -eq 54

gate_capture_checked 'awk(launcher-contract-row-count)' \
    "$temporary/launcher-row-count" "$temporary/launcher-row-count.err" \
    awk 'NR > 1 { count++ } END { print count + 0 }' "$launcher_contract"
IFS= read -r launcher_row_count <"$temporary/launcher-row-count" || :
test "$launcher_row_count" -eq 19

for contract_tool in \
    sh controlled-maven-launcher controlled-maven-launcher-core \
    uname dirname java \
    dynamic-maven-executable maven-classworlds-inventory \
    maven-launcher-config mavenrc-source MAVEN_BASEDIR \
    ls expr cygpath tr
do
    gate_capture_checked "awk(launcher-contract:$contract_tool)" \
        "$temporary/$contract_tool.launcher-count" \
        "$temporary/$contract_tool.launcher-count.err" \
        awk -F '	' -v tool="$contract_tool" \
        '$2 == tool { count++ } END { print count + 0 }' "$launcher_contract"
    IFS= read -r count <"$temporary/$contract_tool.launcher-count" || :
    test "$count" -eq 1
done

# The manifest is a complete immutable snapshot of every Phase 00 shell source.
# Adding or changing any external command without intentionally refreshing the
# inventory, its fault case, and this source snapshot makes this verifier fail.
: >"$temporary/actual-source-manifest.tsv"
while IFS='	' read -r path expected_digest; do
    test -n "$path"
    test -n "$expected_digest"
    test -f "$repository/$path"
    gate_sha256_file "$repository/$path" "$temporary/source"
    test "$GATE_SHA256" = "$expected_digest"
    printf '%s\t%s\n' "$path" "$GATE_SHA256" \
        >>"$temporary/actual-source-manifest.tsv"
done <"$source_manifest"
gate_compare_files 'gate source snapshot' \
    "$source_manifest" "$temporary/actual-source-manifest.tsv" \
    "$temporary/source-manifest.cmp.out" \
    "$temporary/source-manifest.cmp.err"

gate_stage_find "$repository/build" \
    "$temporary/shell-files.raw" "$temporary/shell-files.err" \
    . -type f -name '*.sh' -print
gate_sort_file "$temporary/shell-files.raw" \
    "$temporary/shell-files.sorted" "$temporary/shell-files.sort.err"
{
    printf '%s\n' mvnw
    while IFS= read -r path; do
        gate_normalize_relative_path "$path"
        printf 'build/%s\n' "$GATE_RELATIVE_PATH"
    done <"$temporary/shell-files.sorted"
} >"$temporary/expected-source-paths.txt"
gate_capture_checked 'awk(source-manifest-paths)' \
    "$temporary/manifest-source-paths.txt" \
    "$temporary/manifest-source-paths.err" \
    awk -F '	' '{ print $1 }' "$source_manifest"
gate_compare_files 'complete gate shell source path inventory' \
    "$temporary/expected-source-paths.txt" \
    "$temporary/manifest-source-paths.txt" \
    "$temporary/source-paths.cmp.out" "$temporary/source-paths.cmp.err"

gate_require_no_match 'public Maven production source test override' \
    "$temporary/public-source-override.scan" \
    "$temporary/public-source-override.scan.err" \
    grep -n 'PHASE00_MVNW_TEST_' \
    "$repository/mvnw" \
    "$repository/build/lib/maven-wrapper-fail-closed.sh" \
    "$repository/build/lib/maven-launcher-fail-closed.sh" \
    "$repository/build/lib/maven-launcher-core.sh"
gate_require_match 'private Maven test driver is test-only referenced' \
    "$temporary/private-driver-reference.scan" \
    "$temporary/private-driver-reference.scan.err" \
    grep -F 'build/test-support/maven-private-test-driver.sh' \
    "$repository/build/test-maven-launcher-parity.sh" \
    "$repository/build/test-maven-wrapper-faults.sh"

gate_capture_checked 'cp(unclassified-command-mutation-fixture)' \
    "$temporary/mutation-copy.out" "$temporary/mutation-copy.err" \
    cp "$repository/mvnw" "$temporary/mvnw-unclassified-command"
printf '%s\n' 'phase00_unclassified_external --fault-probe' \
    >>"$temporary/mvnw-unclassified-command"
gate_sha256_file "$temporary/mvnw-unclassified-command" \
    "$temporary/mutated-wrapper"
test "$GATE_SHA256" != "$expected_wrapper_digest"

{
    printf '%s\n' 'PHASE00_RECURSIVE_GATE_TOOL_INVENTORY=PASS'
    printf 'INVENTORY_ROW_COUNT=%s\n' "$inventory_row_count"
    printf 'WRAPPER_LAUNCHER_ACTUAL_CLASS_COUNT=%s\n' "$actual_count"
    printf 'WRAPPER_LAUNCHER_CONDITIONAL_CLASS_COUNT=%s\n' "$conditional_count"
    printf 'PINNED_LAUNCHER_CONTRACT_ROW_COUNT=%s\n' "$launcher_row_count"
    printf 'WRAPPER_SHA256=%s\n' "$expected_wrapper_digest"
    printf 'MAVEN_3_9_14_LAUNCHER_SHA256=%s\n' "$expected_launcher_digest"
    printf '%s\n' \
        'MAVEN_3_9_14_M2_CONF_SHA256=e336769bf93a902baa7a3e827ba55e4cef7de4af2ed1a9541a4261094d748ba9'
    printf '%s\n' \
        'MAVEN_3_9_14_CLASSWORLDS_SHA256=1ad3292cd563381e3fd632f3fded1988f9e9b2be7a9f3db63ff4c4cedba13fa5'
    printf '%s\n' \
        'MAVEN_DISTRIBUTION_SHA256=55fadd669532a3205d5db95f490bf13971d8b0843526f407f29db0e61f074ab3'
    printf '%s\n' 'STATIC_SOURCE_SNAPSHOT=COMPLETE'
    printf '%s\n' 'PUBLIC_PRODUCTION_TEST_OVERRIDE_REFERENCE_COUNT=0'
    printf '%s\n' 'PRIVATE_TEST_DRIVER_PUBLIC_SELECTION_PATH=NONE'
    printf '%s\n' 'UNCLASSIFIED_COMMAND_MUTATION_CASE_COUNT=1'
    printf '%s\n' 'UNCLASSIFIED_EXTERNAL_COMMAND_COUNT=0'
    printf '%s\n' 'UNTESTED_WRAPPER_LAUNCHER_CLASS_COUNT=0'
} >"$temporary/report.txt"

gate_sha256_file "$temporary/report.txt" "$temporary/final-report"
final_report_sha256=$GATE_SHA256

if [ -n "$report" ]; then
    gate_publish_file_atomic 'gate-tool-inventory-report' \
        "$temporary/report.txt" "$report" "$temporary/report-publish"
else
    gate_replay_text_file "$temporary/report.txt"
fi

{
    printf '%s\n' 'PHASE00_GATE_TOOL_INVENTORY_RECEIPT=PASS'
    printf 'INVOCATION_TOKEN=%s\n' "$invocation_token"
    printf 'REPORT_SHA256=%s\n' "$final_report_sha256"
    printf '%s\n' 'PRODUCER_RESULT=PASS'
    printf '%s\n' 'PRODUCER_EXIT_CODE=0'
} >"$temporary/receipt.txt"
gate_publish_file_atomic 'gate-tool-inventory-receipt' \
    "$temporary/receipt.txt" "$receipt" "$temporary/receipt-publish"
