#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    printf 'usage: %s <legacy-evidence-directory>\n' "$0" >&2
    exit 2
fi

phase00_script_path=$0
case "$phase00_script_path" in
    */*) ;;
    *) phase00_script_path=$(command -v "$phase00_script_path") ;;
esac
phase00_script_directory=${phase00_script_path%/*}
repository=$(CDPATH= cd -- "$phase00_script_directory/.." && pwd)
. "$repository/build/lib/fail-closed-gates.sh"
evidence_directory=$1
maven_repository=${PHASE00_MAVEN_REPO:-"$repository/target/phase-00-m2"}
baseline_commit=${PHASE00_LEGACY_BASELINE_COMMIT:-7cc890ee1d0805df5ae14b633127fade4f978639}
baseline_report="$evidence_directory/pre-move/baseline-identity-and-test-summary.txt"
if [ -f "$baseline_report" ] && [ ! -L "$baseline_report" ]; then
    : >"$baseline_report"
fi
gate_make_temporary_directory 'mktemp(pre-move-legacy-baseline)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-legacy-baseline.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM
if [ -e "$baseline_report" ] || [ -L "$baseline_report" ]; then
    if [ -f "$baseline_report" ] && [ ! -L "$baseline_report" ]; then
        : >"$baseline_report"
    fi
    gate_capture_checked 'rm(stale-pre-move-baseline-report)' \
        "$temporary/stale-report-remove.out" \
        "$temporary/stale-report-remove.err" \
        rm -f -- "$baseline_report"
fi

gate_capture_checked 'git(pre-move-baseline-head)' \
    "$temporary/actual-head.txt" "$temporary/actual-head.err" \
    git -C "$repository" rev-parse HEAD
IFS= read -r actual_head <"$temporary/actual-head.txt" || :
test "$actual_head" = "$baseline_commit" || {
    printf 'legacy baseline commit mismatch: expected %s, got %s\n' \
        "$baseline_commit" "$actual_head" >&2
    exit 1
}
test -d "$maven_repository" || {
    printf 'isolated Maven repository is missing: %s\n' "$maven_repository" >&2
    exit 1
}

mkdir -p "$temporary/source" "$evidence_directory/pre-move"
gate_capture_checked 'git(archive-pre-move-baseline)' \
    "$temporary/baseline.tar" "$temporary/baseline-archive.err" \
    git -C "$repository" archive --format=tar "$baseline_commit"
gate_capture_checked 'tar(extract-pre-move-baseline)' \
    "$temporary/baseline-extract.out" "$temporary/baseline-extract.err" \
    tar -xf "$temporary/baseline.tar" -C "$temporary/source"

commands="$evidence_directory/pre-move/command-exit-codes.tsv"
printf '%s\t%s\t%s\n' 'exit-code' 'test-count' 'command' >"$commands"

run_baseline_command() {
    display=$1
    recorded_display="MAVEN_SKIP_RC=${MAVEN_SKIP_RC-<unset>} $display"
    log=$2
    shift
    shift
    set +e
    "$@" >"$log.stdout" 2>"$log.stderr"
    exit_code=$?
    set -e
    printf '%s\t%s\t%s\n' "$exit_code" 'N/A' "$recorded_display" >>"$commands"
    if [ "$exit_code" -ne 0 ]; then
        printf 'pre-move baseline command failed (%s): %s\n' \
            "$exit_code" "$recorded_display" >&2
        gate_replay_text_file "$log.stderr" stderr
        if ! gate_text_file_contains "$log.stderr" ' result='; then
            printf 'tool=command(%s) result=EVALUATED_FAILURE exit=%s\n' \
                "$recorded_display" "$exit_code" >&2
        fi
        exit "$exit_code"
    fi
    : >"$log"
    gate_replay_text_file "$log.stdout" >>"$log"
    gate_replay_text_file "$log.stderr" >>"$log"
}

run_baseline_command \
    'baseline HEAD: ./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=<isolated> test' \
    "$evidence_directory/pre-move/test.log" \
    "$repository/mvnw" -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local="$maven_repository" \
    -f "$temporary/source/pom.xml" test

run_baseline_command \
    'baseline HEAD: ./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=<isolated> dependency:tree -Dverbose' \
    "$evidence_directory/pre-move/dependency-tree-verbose.txt" \
    "$repository/mvnw" -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local="$maven_repository" \
    -f "$temporary/source/pom.xml" dependency:tree -Dverbose

run_baseline_command \
    'baseline HEAD: ./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=<isolated> package' \
    "$evidence_directory/pre-move/package.log" \
    "$repository/mvnw" -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local="$maven_repository" \
    -f "$temporary/source/pom.xml" package

gate_stage_find "$temporary/source" "$temporary/reports.find.raw" \
    "$temporary/reports.find.err" \
    . -type f -path '*/target/surefire-reports/TEST-*.xml' -print
gate_sort_file "$temporary/reports.find.raw" \
    "$temporary/reports.sorted" "$temporary/reports.sort.err"
tests=0
failures=0
errors=0
skipped=0
report_count=0
while IFS= read -r relative_report; do
    gate_normalize_relative_path "$relative_report"
    report="$temporary/source/$GATE_RELATIVE_PATH"
    report_tests=$(sed -n '2s/.* tests="\([0-9][0-9]*\)".*/\1/p' "$report")
    report_failures=$(sed -n '2s/.* failures="\([0-9][0-9]*\)".*/\1/p' "$report")
    report_errors=$(sed -n '2s/.* errors="\([0-9][0-9]*\)".*/\1/p' "$report")
    report_skipped=$(sed -n '2s/.* skipped="\([0-9][0-9]*\)".*/\1/p' "$report")
    test -n "$report_tests"
    test -n "$report_failures"
    test -n "$report_errors"
    test -n "$report_skipped"
    tests=$((tests + report_tests))
    failures=$((failures + report_failures))
    errors=$((errors + report_errors))
    skipped=$((skipped + report_skipped))
    report_count=$((report_count + 1))
done <"$temporary/reports.sorted"
test "$report_count" -gt 0
test "$tests" -eq 1
test "$failures" -eq 0
test "$errors" -eq 0
test "$skipped" -eq 0

baseline_jar="$temporary/source/target/ro-next-0.1.0-SNAPSHOT-app.jar"
test -f "$baseline_jar"
gate_capture_checked 'unzip(pre-move-shaded-manifest)' \
    "$evidence_directory/pre-move/shaded-manifest.mf" \
    "$temporary/pre-move-shaded-manifest.err" \
    unzip -p "$baseline_jar" META-INF/MANIFEST.MF
gate_capture_checked 'jar(list:pre-move-shaded-application)' \
    "$temporary/shaded-entry-inventory.unsorted" \
    "$temporary/shaded-entry-inventory.err" jar tf "$baseline_jar"
gate_sort_file "$temporary/shaded-entry-inventory.unsorted" \
    "$evidence_directory/pre-move/shaded-entry-inventory.txt" \
    "$temporary/shaded-entry-inventory.sort.err"
gate_require_match \
    'pre-move selected service/resource inventory' \
    "$evidence_directory/pre-move/selected-service-resource-inventory.txt" \
    "$temporary/pre-move-resource.detector.err" \
    grep -E \
    'META-INF/services/|META-INF/(LICENSE|NOTICE)|module-info[.]class' \
    "$evidence_directory/pre-move/shaded-entry-inventory.txt"
gate_require_match \
    'pre-move Shade warning inventory' \
    "$evidence_directory/pre-move/shade-collision-warnings.txt" \
    "$temporary/pre-move-shade.detector.err" \
    grep -E '^\[WARNING\]' "$evidence_directory/pre-move/package.log"
gate_require_match \
    'pre-move Jackson conflict inventory' \
    "$evidence_directory/pre-move/jackson-conflict-inventory.txt" \
    "$temporary/pre-move-jackson.detector.err" \
    grep -E 'jackson-(core|annotations|databind).*omitted for conflict' \
    "$evidence_directory/pre-move/dependency-tree-verbose.txt"
test -s "$evidence_directory/pre-move/selected-service-resource-inventory.txt"
test -s "$evidence_directory/pre-move/shade-collision-warnings.txt"
test -s "$evidence_directory/pre-move/jackson-conflict-inventory.txt"

gate_stage_find "$temporary/source" "$temporary/source.find.raw" \
    "$temporary/source.find.err" \
    pom.xml Dockerfile gcp src/main/java/com/ronext/optimizer \
    src/test/java/com/ronext/optimizer -type f -print
gate_sort_file "$temporary/source.find.raw" \
    "$temporary/source.paths" "$temporary/source.sort.err"
mkdir -p "$temporary/source-scratch"
gate_classify_paths "$temporary/source" "$temporary/source.paths" \
    "$temporary/source.types.tsv" "$temporary/source.files.txt" \
    "$temporary/source-scratch"
gate_hash_file_list "$temporary/source" "$temporary/source.files.txt" \
    "$temporary/source.files.tsv" "$temporary/source-scratch"
gate_capture_checked 'awk(pre-move-source-manifest)' \
    "$evidence_directory/pre-move/source-manifest.tsv" \
    "$temporary/source-manifest.err" \
    awk -F '	' 'BEGIN { OFS="\t" } { print $1, $3, $4 }' \
    "$temporary/source.files.tsv"
gate_sha256_file "$evidence_directory/pre-move/source-manifest.tsv" \
    "$temporary/source-manifest"
source_manifest_digest=$GATE_SHA256
gate_sha256_file "$evidence_directory/pre-move/dependency-tree-verbose.txt" \
    "$temporary/dependency-tree"
dependency_tree_digest=$GATE_SHA256
gate_sha256_file "$evidence_directory/pre-move/shade-collision-warnings.txt" \
    "$temporary/shade-warnings"
shade_warning_digest=$GATE_SHA256
gate_sha256_file "$evidence_directory/pre-move/selected-service-resource-inventory.txt" \
    "$temporary/selected-resources"
selected_resource_digest=$GATE_SHA256

{
    printf 'BASELINE_COMMIT=%s\n' "$baseline_commit"
    printf '%s\n' 'BASELINE_REPRODUCTION=SEPARATE_GIT_ARCHIVE_TEMPORARY_WORKSPACE'
    printf '%s\n' 'BASELINE_NETWORK_ACCESS=DISABLED_MAVEN_OFFLINE'
    printf 'SOURCE_MANIFEST_SHA256=%s\n' "$source_manifest_digest"
    printf 'DEPENDENCY_TREE_SHA256=%s\n' "$dependency_tree_digest"
    printf 'SHADE_WARNING_INVENTORY_SHA256=%s\n' "$shade_warning_digest"
    printf 'SELECTED_SERVICE_RESOURCE_INVENTORY_SHA256=%s\n' "$selected_resource_digest"
    printf 'TESTS=%s\n' "$tests"
    printf 'FAILURES=%s\n' "$failures"
    printf 'ERRORS=%s\n' "$errors"
    printf 'SKIPPED=%s\n' "$skipped"
    printf '%s\n' 'TEST_COMMAND_EXIT_CODE=0'
    printf '%s\n' 'PACKAGE_COMMAND_EXIT_CODE=0'
} >"$temporary/baseline-identity-and-test-summary.txt"

gate_capture_checked 'mv(publish-pre-move-baseline-report)' \
    "$temporary/baseline-report-publish.out" \
    "$temporary/baseline-report-publish.err" \
    mv "$temporary/baseline-identity-and-test-summary.txt" "$baseline_report"
gate_replay_text_file "$baseline_report"
