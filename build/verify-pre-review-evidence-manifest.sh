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

if [ "$#" -ne 1 ]; then
    printf 'usage: %s <pre-review-evidence-manifest.yaml>\n' "$0" >&2
    exit 2
fi

manifest=$1
test -f "$manifest" || {
    printf 'pre-review evidence manifest is missing: %s\n' "$manifest" >&2
    exit 1
}
test -s "$manifest"

expected_keys='
phase
canonicalPhasePlanDigest
reviewCriteriaDigest
sourceCommitDigest
inputArtifactDigests
configProfileBuildRuntimeDigests
commandEnvironmentToolchainExitCodeRecordDigest
testResultAndFixtureDigests
requiredEvidenceKeyArtifactDigests
architectureDependencySecurityReportDigests
openGatedDeferredSnapshotDigest
handoffCandidateArtifactDigest
rollbackPointDigest
'

gate_make_temporary_directory 'mktemp(pre-review-manifest)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-pre-review.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

IFS= read -r first_line <"$manifest" || :
test "$first_line" = 'preReviewEvidenceManifest:'

gate_capture_checked 'sed(pre-review-actual-keys)' \
    "$temporary/actual-keys.txt" "$temporary/actual-keys.err" \
    sed -n '2,$s/^  \([A-Za-z][A-Za-z0-9]*\): .*/\1/p' "$manifest"
printf '%s\n' "$expected_keys" >"$temporary/expected-keys.raw"
gate_capture_checked 'sed(pre-review-expected-keys)' \
    "$temporary/expected-keys.txt" "$temporary/expected-keys.err" \
    sed '/^$/d' "$temporary/expected-keys.raw"
gate_capture_checked 'awk(pre-review-actual-key-count)' \
    "$temporary/actual-count.txt" "$temporary/actual-count.err" \
    awk 'NF { count++ } END { print count + 0 }' \
    "$temporary/actual-keys.txt"
gate_capture_checked 'awk(pre-review-expected-key-count)' \
    "$temporary/expected-count.txt" "$temporary/expected-count.err" \
    awk 'NF { count++ } END { print count + 0 }' \
    "$temporary/expected-keys.txt"
IFS= read -r actual_count <"$temporary/actual-count.txt" || :
IFS= read -r expected_count <"$temporary/expected-count.txt" || :
test "$actual_count" -eq "$expected_count"

for expected_key in $expected_keys; do
    gate_capture_status "$temporary/key-count.txt" \
        "$temporary/key-count.err" \
        grep -Fxc "$expected_key" "$temporary/actual-keys.txt"
    case "$GATE_STATUS" in
        0|1)
            ;;
        *)
            key_detector_status=$GATE_STATUS
            gate_report_not_evaluated \
                "grep(pre-review-key:$expected_key)" \
                "$key_detector_status" "$temporary/key-count.err"
            exit "$key_detector_status"
            ;;
    esac
    gate_capture_checked "awk(pre-review-key-count:$expected_key)" \
        "$temporary/key-count.value" "$temporary/key-count.parse.err" \
        awk 'NR == 1 { print $1 + 0 }' "$temporary/key-count.txt"
    IFS= read -r count <"$temporary/key-count.value" || :
    test "$count" -eq 1 || {
        printf 'missing or duplicate pre-review allowlisted key: %s\n' "$expected_key" >&2
        exit 1
    }
done

gate_capture_checked 'sed(pre-review-manifest-rows)' \
    "$temporary/manifest-rows.txt" "$temporary/manifest-rows.err" \
    sed -n '2,$p' "$manifest"
gate_require_no_match \
    'pre-review manifest canonical-row predicate' \
    "$temporary/non-canonical-row.matches" \
    "$temporary/non-canonical-row.detector.err" \
    grep -Ev '^  [A-Za-z][A-Za-z0-9]*: "[^"]+"$' \
    "$temporary/manifest-rows.txt"

for forbidden in \
    reviewerIdentity reviewerRole reviewVerdict reviewTimestamp \
    reviewReportReference reviewReportDigest independentReviewRef \
    acceptanceStatus acceptanceReceipt acceptanceAuthority acceptanceTimestamp \
    preReviewEvidenceManifestDigest
do
    gate_require_no_match \
        "pre-review forbidden lifecycle field: $forbidden" \
        "$temporary/forbidden.matches" \
        "$temporary/forbidden.detector.err" \
        grep -F "$forbidden" "$manifest"
done

printf '%s\n' 'PHASE00_PRE_REVIEW_MANIFEST_CONTRACT=PASS'
printf 'ALLOWLISTED_FIELD_COUNT=%s\n' "$expected_count"
printf '%s\n' 'FORBIDDEN_REVIEW_OR_ACCEPTANCE_FIELD_COUNT=0'
