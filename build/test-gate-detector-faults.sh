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
gate_make_temporary_directory 'mktemp(gate-detector-fault-test)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-gate-faults.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM
real_grep=$(command -v grep)
real_mv=/bin/mv
test -x "$real_mv"
if [ -n "$report" ] && { [ -e "$report" ] || [ -L "$report" ]; }; then
    if [ -f "$report" ] && [ ! -L "$report" ]; then
        : >"$report"
    fi
    gate_capture_checked 'rm(stale-gate-fault-self-test-report)' \
        "$temporary/stale-report-remove.out" \
        "$temporary/stale-report-remove.err" \
        rm -f -- "$report"
fi

make_fault_shim() {
    tool=$1
    exit_code=$2
    shim_directory="$temporary/$tool-fault-shim"
    mkdir -p "$shim_directory"
    {
        printf '%s\n' '#!/bin/sh'
        printf '%s\n' 'if [ -n "${PHASE00_FAKE_SUCCESS_OUTPUT:-}" ]; then'
        printf '%s\n' '    printf "%s\n" "$PHASE00_FAKE_SUCCESS_OUTPUT"'
        printf '%s\n' 'fi'
        printf 'printf "INJECTED_%s_FAILURE exit=%s\\\\n" >&2\n' \
            "$tool" "$exit_code"
        printf 'exit %s\n' "$exit_code"
    } >"$shim_directory/$tool"
    chmod +x "$shim_directory/$tool"
}

make_targeted_fault_shim() {
    tool=$1
    real_tool=$2
    needle=$3
    exit_code=$4
    shim_directory="$temporary/$tool-targeted-fault-shim"
    mkdir -p "$shim_directory"
    {
        printf '%s\n' '#!/bin/sh'
        printf '%s\n' 'for argument in "$@"; do'
        printf '    case "$argument" in *%s*)\n' "$needle"
        printf '%s\n' '        if [ -n "${PHASE00_FAKE_SUCCESS_OUTPUT:-}" ]; then'
        printf '%s\n' '            printf "%s\n" "$PHASE00_FAKE_SUCCESS_OUTPUT"'
        printf '%s\n' '        fi'
        printf '        printf "INJECTED_%s_FAILURE exit=%s\\\\n" >&2\n' \
            "$tool" "$exit_code"
        printf '        exit %s ;;\n' "$exit_code"
        printf '%s\n' '    esac'
        printf '%s\n' 'done'
        printf 'exec %s "$@"\n' "$real_tool"
    } >"$shim_directory/$tool"
    chmod +x "$shim_directory/$tool"
}

mkdir -p "$temporary/grep-fault-shim"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'for argument in "$@"; do'
    printf '%s\n' '    if [ "${PHASE00_GREP_FAULT_EXACT:-0}" -eq 1 ]; then'
    printf '%s\n' '        if [ "$argument" = "$PHASE00_GREP_FAULT_NEEDLE" ]; then'
    printf '%s\n' '            exit "$PHASE00_GREP_FAULT_EXIT"'
    printf '%s\n' '        fi'
    printf '%s\n' '    else'
    printf '%s\n' '        case "$argument" in'
    printf '%s\n' '            *"$PHASE00_GREP_FAULT_NEEDLE"*) exit "$PHASE00_GREP_FAULT_EXIT" ;;'
    printf '%s\n' '        esac'
    printf '%s\n' '    fi'
    printf '%s\n' 'done'
    printf '%s\n' 'exec "$PHASE00_REAL_GREP" "$@"'
} >"$temporary/grep-fault-shim/grep"
chmod +x "$temporary/grep-fault-shim/grep"

: >"$temporary/results.tsv"
run_exact_failure() {
    case_id=$1
    expected_exit=$2
    forbidden_pass_marker=$3
    shift
    shift
    shift

    set +e
    "$@" >"$temporary/$case_id.log" 2>&1
    actual_exit=$?
    set -e
    test "$actual_exit" -eq "$expected_exit"
    if [ "$forbidden_pass_marker" != '-' ]; then
        gate_require_no_match \
            "$case_id PASS-report absence" \
            "$temporary/$case_id.pass.matches" \
            "$temporary/$case_id.pass.detector.err" \
            "$real_grep" -F "$forbidden_pass_marker" \
            "$temporary/$case_id.log"
    fi
    evaluation_result=EVALUATED_FAILURE
    case "$case_id" in
        *-grep-exit-*|*-rg-exit-*|*-find-exit-*|*-jdeps-exit-*|\
        *-shasum-exit-*|*-sort-exit-*|*-awk-exit-*|*-wc-exit-*|\
        *-ls-exit-*|*-cmp-exit-*|*-jar-exit-*|*-chmod-exit-*|\
        *-cp-exit-*|*-date-exit-*|*-env-exit-*|*-git-exit-*|\
        *-java-exit-*|*-ln-exit-*|*-mkdir-exit-*|*-mkfifo-exit-*|\
        *-mktemp-exit-*|*-mv-exit-*|*-readlink-exit-*|*-rm-exit-*|\
        *-sed-exit-*|*-tar-exit-*|*-tr-exit-*|*-unzip-exit-*|\
        *-mvnw-exit-*)
            gate_require_match \
                "$case_id NOT_EVALUATED report" \
                "$temporary/$case_id.not-evaluated.matches" \
                "$temporary/$case_id.not-evaluated.detector.err" \
                "$real_grep" -F 'NOT_EVALUATED' "$temporary/$case_id.log"
            evaluation_result=NOT_EVALUATED
            ;;
    esac
    printf '%s\t%s\t%s\t%s\t%s\n' \
        "$case_id" "$expected_exit" "$actual_exit" \
        "$evaluation_result" 'NO_PASS_REPORT' \
        >>"$temporary/results.tsv"
}

assert_exact_diagnostic() {
    case_id=$1
    diagnostic=$2
    gate_require_match \
        "$case_id exact structured diagnostic" \
        "$temporary/$case_id.exact-diagnostic.matches" \
        "$temporary/$case_id.exact-diagnostic.detector.err" \
        "$real_grep" -F "$diagnostic" "$temporary/$case_id.log"
}

write_valid_pre_review_manifest() {
    output=$1
    {
        printf '%s\n' 'preReviewEvidenceManifest:'
        printf '%s\n' '  phase: "00"'
        printf '%s\n' '  canonicalPhasePlanDigest: "sha256:a"'
        printf '%s\n' '  reviewCriteriaDigest: "sha256:b"'
        printf '%s\n' '  sourceCommitDigest: "sha256:c"'
        printf '%s\n' '  inputArtifactDigests: "sha256:d"'
        printf '%s\n' '  configProfileBuildRuntimeDigests: "sha256:e"'
        printf '%s\n' '  commandEnvironmentToolchainExitCodeRecordDigest: "sha256:f"'
        printf '%s\n' '  testResultAndFixtureDigests: "sha256:0"'
        printf '%s\n' '  requiredEvidenceKeyArtifactDigests: "sha256:1"'
        printf '%s\n' '  architectureDependencySecurityReportDigests: "sha256:2"'
        printf '%s\n' '  openGatedDeferredSnapshotDigest: "sha256:3"'
        printf '%s\n' '  handoffCandidateArtifactDigest: "sha256:4"'
        printf '%s\n' '  rollbackPointDigest: "sha256:5"'
    } >"$output"
}

mkdir -p "$temporary/bundle-symlink"
printf '%s\n' 'payload' >"$temporary/bundle-symlink/payload.txt"
ln -s payload.txt "$temporary/bundle-symlink/forbidden-link"
run_exact_failure \
    'seal-symlink-grep-exit-2' 2 'PHASE00_EVIDENCE_BUNDLE=PASS' \
    env PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='^L' PHASE00_GREP_FAULT_EXIT=2 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/seal-evidence-bundle.sh" \
    "$temporary/bundle-symlink"
test ! -e "$temporary/bundle-symlink/evidence-manifest.tsv"
test ! -e "$temporary/bundle-symlink/evidence-manifest.sha256"

mkdir -p "$temporary/bundle-verify-symlink"
printf '%s\n' 'payload' >"$temporary/bundle-verify-symlink/payload.txt"
"$repository/build/seal-evidence-bundle.sh" \
    "$temporary/bundle-verify-symlink" \
    >"$temporary/bundle-verify-symlink.seal.log"
ln -s payload.txt "$temporary/bundle-verify-symlink/forbidden-link"
run_exact_failure \
    'verify-symlink-grep-exit-3' 3 'PHASE00_EVIDENCE_BUNDLE=PASS' \
    env PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='^L' PHASE00_GREP_FAULT_EXIT=3 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/verify-evidence-bundle.sh" \
    "$temporary/bundle-verify-symlink"

mkdir -p "$temporary/bundle-nonregular"
printf '%s\n' 'payload' >"$temporary/bundle-nonregular/payload.txt"
mkfifo "$temporary/bundle-nonregular/forbidden-fifo"
run_exact_failure \
    'seal-nonregular-grep-exit-3' 3 'PHASE00_EVIDENCE_BUNDLE=PASS' \
    env PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='^O' PHASE00_GREP_FAULT_EXIT=3 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/seal-evidence-bundle.sh" \
    "$temporary/bundle-nonregular"
test ! -e "$temporary/bundle-nonregular/evidence-manifest.tsv"
test ! -e "$temporary/bundle-nonregular/evidence-manifest.sha256"

write_valid_pre_review_manifest "$temporary/pre-review-valid.yaml"
run_exact_failure \
    'pre-review-key-count-grep-exit-2' 2 \
    'PHASE00_PRE_REVIEW_MANIFEST_CONTRACT=PASS' \
    env PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='phase' PHASE00_GREP_FAULT_EXIT=2 \
    PHASE00_GREP_FAULT_EXACT=1 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/verify-pre-review-evidence-manifest.sh" \
    "$temporary/pre-review-valid.yaml"

cp "$temporary/pre-review-valid.yaml" "$temporary/pre-review-malformed.yaml"
printf '%s\n' 'malformed-row' >>"$temporary/pre-review-malformed.yaml"
run_exact_failure \
    'pre-review-malformed-row-grep-exit-2' 2 \
    'PHASE00_PRE_REVIEW_MANIFEST_CONTRACT=PASS' \
    env PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='A-Za-z0-9' PHASE00_GREP_FAULT_EXIT=2 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/verify-pre-review-evidence-manifest.sh" \
    "$temporary/pre-review-malformed.yaml"

sed 's/  phase: "00"/  phase: "00 acceptanceStatus"/' \
    "$temporary/pre-review-valid.yaml" \
    >"$temporary/pre-review-forbidden.yaml"
run_exact_failure \
    'pre-review-forbidden-field-grep-exit-3' 3 \
    'PHASE00_PRE_REVIEW_MANIFEST_CONTRACT=PASS' \
    env PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='acceptanceStatus' \
    PHASE00_GREP_FAULT_EXIT=3 PHASE00_GREP_FAULT_EXACT=1 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/verify-pre-review-evidence-manifest.sh" \
    "$temporary/pre-review-forbidden.yaml"

run_exact_failure \
    'bytecode-jdeps-grep-exit-2' 2 \
    'PHASE00_BYTECODE_BOUNDARIES=PASS' \
    env PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='com[.]google' PHASE00_GREP_FAULT_EXIT=2 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/verify-bytecode-boundaries.sh"

bytecode_repository="$temporary/bytecode-repository"
for module in core solver verification application capabilities profile-catalog; do
    mkdir -p "$bytecode_repository/rpdptw/$module"
    cp -R "$repository/rpdptw/$module/target" \
        "$bytecode_repository/rpdptw/$module/target"
done
mkdir -p "$bytecode_repository/build/test-fixtures"
cp -R "$repository/build/test-fixtures/target" \
    "$bytecode_repository/build/test-fixtures/target"

gate_stage_find "$bytecode_repository/rpdptw/verification/target" \
    "$temporary/verification-jars.raw" "$temporary/verification-jars.err" \
    . -maxdepth 1 -type f -name '*.jar' -print
gate_sort_file "$temporary/verification-jars.raw" \
    "$temporary/verification-jars.sorted" \
    "$temporary/verification-jars.sort.err"
IFS= read -r verification_relative <"$temporary/verification-jars.sorted"
gate_normalize_relative_path "$verification_relative"
verification_jar="$bytecode_repository/rpdptw/verification/target/$GATE_RELATIVE_PATH"
mkdir -p "$temporary/injected/com/ronext/optimizer"
printf '%s\n' 'fault-injected legacy bytecode marker' \
    >"$temporary/injected/com/ronext/optimizer/Injected.class"
(
    cd "$temporary/injected"
    jar uf "$verification_jar" com/ronext/optimizer/Injected.class
)

run_exact_failure \
    'bytecode-injected-legacy-match' 1 \
    'PHASE00_BYTECODE_BOUNDARIES=PASS' \
    env PHASE00_SCAN_REPOSITORY="$bytecode_repository" \
    "$repository/build/verify-bytecode-boundaries.sh"
run_exact_failure \
    'bytecode-injected-legacy-grep-exit-3' 3 \
    'PHASE00_BYTECODE_BOUNDARIES=PASS' \
    env PHASE00_SCAN_REPOSITORY="$bytecode_repository" \
    PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='com/ronext/rpdptw/solver' \
    PHASE00_GREP_FAULT_EXIT=3 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/verify-bytecode-boundaries.sh"

run_exact_failure \
    'bytecode-fixture-main-grep-exit-3' 3 \
    'PHASE00_BYTECODE_BOUNDARIES=PASS' \
    env PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='com/ronext/rpdptw/fixture/' \
    PHASE00_GREP_FAULT_EXIT=3 PHASE00_GREP_FAULT_EXACT=1 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/verify-bytecode-boundaries.sh"

repro_repository="$temporary/repro-source"
mkdir -p \
    "$repro_repository/.mvn" \
    "$repro_repository/rpdptw" \
    "$repro_repository/build" \
    "$repro_repository/legacy" \
    "$repro_repository/gcp" \
    "$repro_repository/docs/implementation" \
    "$temporary/repro-m2"
for source_file in pom.xml mvnw mvnw.cmd .sdkmanrc Dockerfile .dockerignore README.md; do
    printf '%s\n' 'fault fixture' >"$repro_repository/$source_file"
done
chmod +x "$repro_repository/mvnw"
printf '%s\n' 'fault fixture' \
    >"$repro_repository/docs/implementation/master-realization-plan.md"
ln -s ../pom.xml "$repro_repository/.mvn/source-link"

run_exact_failure \
    'repro-source-symlink-match' 1 \
    'PHASE00_REPRODUCIBLE_BUILD=PASS' \
    env PHASE00_REPRO_REPOSITORY="$repro_repository" \
    PHASE00_MAVEN_REPO="$temporary/repro-m2" \
    PHASE00_EVIDENCE_DIR="$temporary/repro-evidence-normal" \
    "$repository/build/verify-reproducible-build.sh"
run_exact_failure \
    'repro-source-symlink-grep-exit-3' 3 \
    'PHASE00_REPRODUCIBLE_BUILD=PASS' \
    env PHASE00_REPRO_REPOSITORY="$repro_repository" \
    PHASE00_MAVEN_REPO="$temporary/repro-m2" \
    PHASE00_EVIDENCE_DIR="$temporary/repro-evidence-fault" \
    PHASE00_REAL_GREP="$real_grep" \
    PHASE00_GREP_FAULT_NEEDLE='^L' PHASE00_GREP_FAULT_EXIT=3 \
    PATH="$temporary/grep-fault-shim:$PATH" \
    "$repository/build/verify-reproducible-build.sh"
test ! -e "$temporary/repro-evidence-fault/reproducible-build.txt"

for tool in \
    jdeps shasum sort awk wc ls find cmp jar \
    chmod cp date env git java ln mkdir mkfifo mktemp mv readlink rm sed tar tr unzip
do
    make_fault_shim "$tool" 9
done
make_fault_shim rg 2
make_fault_shim env 125

printf '%s\n' 'helper input' >"$temporary/helper-input.txt"
mkdir -p "$temporary/helper-tree"
printf '%s\n' 'tree payload' >"$temporary/helper-tree/payload.txt"

for tool in \
    chmod cp date java ln mkdir mkfifo mv readlink rm sed tar tr unzip
do
    case_id="helper-$tool-exit-9"
    run_exact_failure \
        "$case_id" 9 'INJECTED_FALSE_PASS' \
        env PHASE00_FAKE_SUCCESS_OUTPUT='INJECTED_FALSE_PASS' \
        PATH="$temporary/$tool-fault-shim:$PATH" \
        sh -eu -c \
        '. "$1"; "$2" --phase00-fault-probe >"$3"; printf "%s\n" HELPER_PASS' \
        sh "$repository/build/lib/fail-closed-gates.sh" "$tool" \
        "$temporary/$tool.partial-output"
    assert_exact_diagnostic \
        "$case_id" "tool=$tool result=NOT_EVALUATED exit=9"
done

run_exact_failure \
    'helper-env-exit-125' 125 'INJECTED_FALSE_PASS' \
    sh -eu -c \
    'PATH=$2; PHASE00_FAKE_SUCCESS_OUTPUT=INJECTED_FALSE_PASS; export PATH PHASE00_FAKE_SUCCESS_OUTPUT; . "$1"; env --phase00-fault-probe >"$3"; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/env-fault-shim:$PATH" \
    "$temporary/env.partial-output"
assert_exact_diagnostic \
    'helper-env-exit-125' 'tool=env result=NOT_EVALUATED exit=125'

run_exact_failure \
    'helper-mktemp-exit-9' 9 'INJECTED_FALSE_PASS' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='INJECTED_FALSE_PASS' \
    PATH="$temporary/mktemp-fault-shim:$PATH" \
    sh -eu -c \
    '. "$1"; gate_make_temporary_directory "mktemp(helper)" "$2"; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/helper-mktemp.XXXXXX"
assert_exact_diagnostic \
    'helper-mktemp-exit-9' 'tool=mktemp(helper) result=NOT_EVALUATED exit=9'

run_exact_failure \
    'helper-shasum-exit-9' 9 'HELPER_PASS' \
    env PATH="$temporary/shasum-fault-shim:$PATH" \
    sh -eu -c '. "$1"; gate_sha256_file "$2" "$3"; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/helper-input.txt" "$temporary/helper-shasum"

run_exact_failure \
    'helper-sort-exit-9' 9 'HELPER_PASS' \
    env PATH="$temporary/sort-fault-shim:$PATH" \
    sh -eu -c '. "$1"; gate_sort_file "$2" "$3" "$4"; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/helper-input.txt" "$temporary/helper-sort.out" \
    "$temporary/helper-sort.err"

run_exact_failure \
    'helper-awk-exit-9' 9 'HELPER_PASS' \
    env PATH="$temporary/awk-fault-shim:$PATH" \
    sh -eu -c '. "$1"; gate_stat_mode "$2" "$3"; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/helper-input.txt" "$temporary/helper-awk"

run_exact_failure \
    'helper-wc-exit-9' 9 'HELPER_PASS' \
    env PATH="$temporary/wc-fault-shim:$PATH" \
    sh -eu -c '. "$1"; gate_file_size "$2" "$3"; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/helper-input.txt" "$temporary/helper-wc"

run_exact_failure \
    'helper-ls-exit-9' 9 'HELPER_PASS' \
    env PATH="$temporary/ls-fault-shim:$PATH" \
    sh -eu -c '. "$1"; gate_stat_mode "$2" "$3"; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/helper-input.txt" "$temporary/helper-ls"

run_exact_failure \
    'helper-find-exit-9' 9 'HELPER_PASS' \
    env PATH="$temporary/find-fault-shim:$PATH" \
    sh -eu -c '. "$1"; gate_stage_find "$2" "$3" "$4" . -print; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/helper-tree" "$temporary/helper-find.out" \
    "$temporary/helper-find.err"

run_exact_failure \
    'helper-cmp-exit-9' 9 'HELPER_PASS' \
    env PATH="$temporary/cmp-fault-shim:$PATH" \
    sh -eu -c '. "$1"; gate_compare_files helper "$2" "$2" "$3" "$4"; printf "%s\n" HELPER_PASS' \
    sh "$repository/build/lib/fail-closed-gates.sh" \
    "$temporary/helper-input.txt" "$temporary/helper-cmp.out" \
    "$temporary/helper-cmp.err"

run_exact_failure \
    'bytecode-jdeps-exit-9' 9 'PHASE00_BYTECODE_BOUNDARIES=PASS' \
    env PATH="$temporary/jdeps-fault-shim:$PATH" \
    "$repository/build/verify-bytecode-boundaries.sh"

run_exact_failure \
    'bytecode-jar-exit-9' 9 'PHASE00_BYTECODE_BOUNDARIES=PASS' \
    env PATH="$temporary/jar-fault-shim:$PATH" \
    "$repository/build/verify-bytecode-boundaries.sh"

run_exact_failure \
    'source-rg-exit-2' 2 'PHASE00_SOURCE_SCANS=PASS' \
    env PATH="$temporary/rg-fault-shim:$PATH" \
    "$repository/build/verify-phase-00-source-scans.sh" --scan provider

run_exact_failure \
    'actual-toolchain-java-exit-9' 9 'PHASE00_TOOLCHAIN_POLICY=PASS' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='PHASE00_TOOLCHAIN_POLICY=PASS' \
    PATH="$temporary/java-fault-shim:$PATH" \
    "$repository/build/verify-toolchain.sh"
assert_exact_diagnostic \
    'actual-toolchain-java-exit-9' \
    'tool=java result=NOT_EVALUATED exit=9'

legacy_fault_template="$temporary/actual-legacy-template"
mkdir -p "$legacy_fault_template/pre-move" "$legacy_fault_template/post-move"
printf '%s\n' \
    '[INFO] com.ronext:ro-next:jar:0.1.0-SNAPSHOT' \
    '[INFO] +- com.fasterxml.jackson.core:jackson-core:jar:2.17.2:compile' \
    '[INFO] \- com.google.cloud:google-cloud-storage:jar:2.42.0:compile' \
    >"$legacy_fault_template/pre-move/dependency-tree-verbose.txt"
printf '%s\n' \
    '[INFO] com.ronext:legacy-gcp-placeholder:jar:0.1.0-SNAPSHOT' \
    '[INFO] +- com.fasterxml.jackson.core:jackson-core:jar:2.17.2:compile' \
    '[INFO] \- com.google.cloud:google-cloud-storage:jar:2.42.0:compile' \
    >"$legacy_fault_template/post-move/dependency-tree-verbose.txt"
printf '%s\n' \
    '[WARNING] ro-next-0.1.0-SNAPSHOT.jar overlap fixture' \
    >"$legacy_fault_template/pre-move/shade-collision-warnings.txt"
printf '%s\n' \
    '[WARNING] legacy-gcp-placeholder-0.1.0-SNAPSHOT.jar overlap fixture' \
    >"$legacy_fault_template/post-move/shade-collision-warnings.txt"
printf '%s\n' 'META-INF/services/example.Service' \
    >"$legacy_fault_template/pre-move/selected-service-resource-inventory.txt"
printf '%s\n' 'META-INF/services/example.Service' \
    >"$legacy_fault_template/post-move/selected-service-resource-inventory.txt"
printf '%s\n' 'Main-Class: com.ronext.optimizer.Main' \
    >"$legacy_fault_template/pre-move/shaded-manifest.mf"
printf '%s\n' 'Main-Class: com.ronext.optimizer.Main' \
    >"$legacy_fault_template/post-move/shaded-manifest.mf"
printf '%s\n' \
    '[INFO] +- com.fasterxml.jackson.core:jackson-core:jar:2.17.2:compile' \
    >"$legacy_fault_template/pre-move/jackson-conflict-inventory.txt"
printf '%s\n' \
    '[INFO] +- com.fasterxml.jackson.core:jackson-core:jar:2.17.2:compile' \
    >"$legacy_fault_template/post-move/jackson-conflict-inventory.txt"
printf '%s\n' 'TESTS=1' \
    >"$legacy_fault_template/pre-move/baseline-identity-and-test-summary.txt"
printf '%s\n' \
    'com.ronext.optimizer.adapter.in.http.LegacyOptimizationContractCharacterizationTest#readsTheListedGenerationWhenCandidateIsOverwrittenAfterListing' \
    >"$legacy_fault_template/post-move/golden-test-list.txt"
printf '%s\n' 'LEGACY_GOLDEN_TESTS=15' >"$temporary/test-summary.txt"

for tool in sed tr; do
    legacy_fault_fixture="$temporary/actual-legacy-$tool"
    cp -R "$legacy_fault_template" "$legacy_fault_fixture"
    printf '%s\n' 'LEGACY_PRE_POST_COMPARISON=PASS' \
        >"$legacy_fault_fixture/legacy-pre-post-comparison.txt"
    case_id="actual-legacy-$tool-exit-9"
    run_exact_failure \
        "$case_id" 9 'LEGACY_PRE_POST_COMPARISON=PASS' \
        env PHASE00_FAKE_SUCCESS_OUTPUT='LEGACY_PRE_POST_COMPARISON=PASS' \
        PATH="$temporary/$tool-fault-shim:$PATH" \
        "$repository/build/compare-legacy-baseline.sh" \
        "$legacy_fault_fixture"
    assert_exact_diagnostic \
        "$case_id" "tool=$tool result=NOT_EVALUATED exit=9"
    test ! -e "$legacy_fault_fixture/legacy-pre-post-comparison.txt"
done

legacy_rm_fixture="$temporary/actual-legacy-rm"
cp -R "$legacy_fault_template" "$legacy_rm_fixture"
printf '%s\n' 'LEGACY_PRE_POST_COMPARISON=PASS' \
    >"$legacy_rm_fixture/legacy-pre-post-comparison.txt"
run_exact_failure \
    'actual-legacy-stale-rm-exit-9' 9 'LEGACY_PRE_POST_COMPARISON=PASS' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='LEGACY_PRE_POST_COMPARISON=PASS' \
    PATH="$temporary/rm-fault-shim:$PATH" \
    "$repository/build/compare-legacy-baseline.sh" "$legacy_rm_fixture"
assert_exact_diagnostic \
    'actual-legacy-stale-rm-exit-9' 'tool=rm result=NOT_EVALUATED exit=9'
test ! -s "$legacy_rm_fixture/legacy-pre-post-comparison.txt"

legacy_mktemp_fixture="$temporary/actual-legacy-mktemp"
cp -R "$legacy_fault_template" "$legacy_mktemp_fixture"
printf '%s\n' 'LEGACY_PRE_POST_COMPARISON=PASS' \
    >"$legacy_mktemp_fixture/legacy-pre-post-comparison.txt"
run_exact_failure \
    'actual-legacy-stale-mktemp-exit-9' 9 \
    'LEGACY_PRE_POST_COMPARISON=PASS' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='LEGACY_PRE_POST_COMPARISON=PASS' \
    PATH="$temporary/mktemp-fault-shim:$PATH" \
    "$repository/build/compare-legacy-baseline.sh" "$legacy_mktemp_fixture"
assert_exact_diagnostic \
    'actual-legacy-stale-mktemp-exit-9' \
    'tool=mktemp(legacy-comparator) result=NOT_EVALUATED exit=9'
test ! -s "$legacy_mktemp_fixture/legacy-pre-post-comparison.txt"

baseline_git_fixture="$temporary/actual-baseline-git"
mkdir -p "$baseline_git_fixture/pre-move"
printf '%s\n' 'BASELINE_COMMAND_EXIT_CODE=0' \
    >"$baseline_git_fixture/pre-move/baseline-identity-and-test-summary.txt"
run_exact_failure \
    'actual-baseline-git-exit-9' 9 'BASELINE_COMMAND_EXIT_CODE=0' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='BASELINE_COMMAND_EXIT_CODE=0' \
    PHASE00_MAVEN_REPO="$repository/target/phase-00-m2" \
    PATH="$temporary/git-fault-shim:$PATH" \
    "$repository/build/capture-pre-move-legacy-baseline.sh" \
    "$baseline_git_fixture"
assert_exact_diagnostic \
    'actual-baseline-git-exit-9' \
    'tool=git(pre-move-baseline-head) result=NOT_EVALUATED exit=9'
test ! -s "$baseline_git_fixture/pre-move/baseline-identity-and-test-summary.txt"

baseline_fault_repository="$temporary/baseline-fault-repository"
baseline_fault_source="$temporary/baseline-fault-source"
baseline_fault_m2="$temporary/baseline-fault-m2"
baseline_fault_jar="$temporary/baseline-fault-app.jar"
mkdir -p \
    "$baseline_fault_repository/build/lib" \
    "$baseline_fault_source/gcp" \
    "$baseline_fault_source/src/main/java/com/ronext/optimizer" \
    "$baseline_fault_source/src/test/java/com/ronext/optimizer" \
    "$baseline_fault_m2" \
    "$temporary/baseline-fault-jar/META-INF/services" \
    "$temporary/baseline-git-success-shim"
cp "$repository/build/capture-pre-move-legacy-baseline.sh" \
    "$baseline_fault_repository/build/capture-pre-move-legacy-baseline.sh"
cp "$repository/build/lib/fail-closed-gates.sh" \
    "$baseline_fault_repository/build/lib/fail-closed-gates.sh"
printf '%s\n' '<project />' >"$baseline_fault_source/pom.xml"
printf '%s\n' 'FROM scratch' >"$baseline_fault_source/Dockerfile"
printf '%s\n' 'fixture' >"$baseline_fault_source/gcp/fixture.txt"
printf '%s\n' 'package com.ronext.optimizer; final class Fixture {}' \
    >"$baseline_fault_source/src/main/java/com/ronext/optimizer/Fixture.java"
printf '%s\n' 'package com.ronext.optimizer; final class FixtureTest {}' \
    >"$baseline_fault_source/src/test/java/com/ronext/optimizer/FixtureTest.java"
printf '%s\n' 'com.ronext.optimizer.Fixture' \
    >"$temporary/baseline-fault-jar/META-INF/services/example.Service"
printf '%s\n' \
    'Manifest-Version: 1.0' \
    'Main-Class: com.ronext.optimizer.Main' \
    >"$temporary/baseline-fault-manifest.mf"
jar cfm "$baseline_fault_jar" "$temporary/baseline-fault-manifest.mf" \
    -C "$temporary/baseline-fault-jar" .
{
    printf '%s\n' '#!/bin/sh' 'set -eu'
    printf '%s\n' 'source_root=' 'previous='
    printf '%s\n' 'for argument in "$@"; do'
    printf '%s\n' '    if [ "$previous" = "-f" ]; then source_root=${argument%/*}; fi'
    printf '%s\n' '    previous=$argument'
    printf '%s\n' 'done'
    printf '%s\n' 'case " $* " in'
    printf '%s\n' '    *" test "*)'
    printf '%s\n' '        mkdir -p "$source_root/target/surefire-reports"'
    printf '%s\n' '        printf "%s\n" "<?xml version=\"1.0\"?>" "<testsuite tests=\"1\" failures=\"0\" errors=\"0\" skipped=\"0\">" >"$source_root/target/surefire-reports/TEST-fixture.xml"'
    printf '%s\n' '        ;;'
    printf '%s\n' '    *" dependency:tree "*)'
    printf '%s\n' '        printf "%s\n" "[INFO] com.ronext:ro-next:jar:0.1.0-SNAPSHOT" "[INFO] +- com.fasterxml.jackson.core:jackson-core:jar:2.17.2:compile" "[INFO] \- com.fasterxml.jackson.core:jackson-core:jar:2.16.0:compile (omitted for conflict with 2.17.2)"'
    printf '%s\n' '        ;;'
    printf '%s\n' '    *" package "*)'
    printf '%s\n' '        mkdir -p "$source_root/target"'
    printf '%s\n' '        /bin/cp "$PHASE00_FAKE_BASELINE_JAR" "$source_root/target/ro-next-0.1.0-SNAPSHOT-app.jar"'
    printf '%s\n' '        printf "%s\n" "[WARNING] ro-next-0.1.0-SNAPSHOT.jar overlap fixture"'
    printf '%s\n' '        ;;'
    printf '%s\n' '    *) printf "unexpected fake baseline command: %s\n" "$*" >&2; exit 97 ;;'
    printf '%s\n' 'esac'
} >"$baseline_fault_repository/mvnw"
chmod +x "$baseline_fault_repository/mvnw"
{
    printf '%s\n' '#!/bin/sh' 'set -eu'
    printf '%s\n' 'case " $* " in'
    printf '%s\n' '    *" rev-parse HEAD "*) printf "%s\n" 1111111111111111111111111111111111111111 ;;'
    printf '%s\n' '    *" archive --format=tar "*) /usr/bin/tar -cf - -C "$PHASE00_FAKE_BASELINE_SOURCE" . ;;'
    printf '%s\n' '    *) printf "unexpected fake baseline git command: %s\n" "$*" >&2; exit 97 ;;'
    printf '%s\n' 'esac'
} >"$temporary/baseline-git-success-shim/git"
chmod +x "$temporary/baseline-git-success-shim/git"

baseline_tar_fixture="$temporary/actual-baseline-tar"
mkdir -p "$baseline_tar_fixture/pre-move"
printf '%s\n' 'BASELINE_COMMAND_EXIT_CODE=0' \
    >"$baseline_tar_fixture/pre-move/baseline-identity-and-test-summary.txt"
run_exact_failure \
    'actual-baseline-tar-exit-9' 9 'BASELINE_COMMAND_EXIT_CODE=0' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='BASELINE_COMMAND_EXIT_CODE=0' \
    PHASE00_MAVEN_REPO="$baseline_fault_m2" \
    PHASE00_LEGACY_BASELINE_COMMIT=1111111111111111111111111111111111111111 \
    PHASE00_FAKE_BASELINE_SOURCE="$baseline_fault_source" \
    PATH="$temporary/baseline-git-success-shim:$temporary/tar-fault-shim:$PATH" \
    "$baseline_fault_repository/build/capture-pre-move-legacy-baseline.sh" \
    "$baseline_tar_fixture"
assert_exact_diagnostic \
    'actual-baseline-tar-exit-9' 'tool=tar result=NOT_EVALUATED exit=9'
test ! -s "$baseline_tar_fixture/pre-move/baseline-identity-and-test-summary.txt"

baseline_unzip_fixture="$temporary/actual-baseline-unzip"
mkdir -p "$baseline_unzip_fixture/pre-move"
printf '%s\n' 'BASELINE_COMMAND_EXIT_CODE=0' \
    >"$baseline_unzip_fixture/pre-move/baseline-identity-and-test-summary.txt"
run_exact_failure \
    'actual-baseline-unzip-exit-9' 9 'BASELINE_COMMAND_EXIT_CODE=0' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='BASELINE_COMMAND_EXIT_CODE=0' \
    PHASE00_MAVEN_REPO="$baseline_fault_m2" \
    PHASE00_LEGACY_BASELINE_COMMIT=1111111111111111111111111111111111111111 \
    PHASE00_FAKE_BASELINE_SOURCE="$baseline_fault_source" \
    PHASE00_FAKE_BASELINE_JAR="$baseline_fault_jar" \
    PATH="$temporary/baseline-git-success-shim:$temporary/unzip-fault-shim:$PATH" \
    "$baseline_fault_repository/build/capture-pre-move-legacy-baseline.sh" \
    "$baseline_unzip_fixture"
assert_exact_diagnostic \
    'actual-baseline-unzip-exit-9' \
    'tool=unzip result=NOT_EVALUATED exit=9'
test ! -s "$baseline_unzip_fixture/pre-move/baseline-identity-and-test-summary.txt"

real_cp=/bin/cp
make_targeted_fault_shim cp "$real_cp" files.txt 9
redaction_cp_fixture="$temporary/actual-redaction-cp"
mkdir -p "$redaction_cp_fixture"
printf '%s\n' 'redacted evidence' >"$redaction_cp_fixture/safe.txt"
run_exact_failure \
    'actual-redaction-cp-exit-9' 9 'PHASE00_EVIDENCE_REDACTION=PASS' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='PHASE00_EVIDENCE_REDACTION=PASS' \
    PATH="$temporary/cp-targeted-fault-shim:$PATH" \
    "$repository/build/verify-evidence-redaction.sh" \
    "$redaction_cp_fixture"
assert_exact_diagnostic \
    'actual-redaction-cp-exit-9' 'tool=cp result=NOT_EVALUATED exit=9'

seal_mv_fixture="$temporary/actual-seal-mv"
mkdir -p "$seal_mv_fixture"
printf '%s\n' 'payload' >"$seal_mv_fixture/payload.txt"
run_exact_failure \
    'actual-seal-publication-mv-exit-9' 9 \
    'PHASE00_EVIDENCE_BUNDLE=PASS' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='PHASE00_EVIDENCE_BUNDLE=PASS' \
    PATH="$temporary/mv-fault-shim:$PATH" \
    "$repository/build/seal-evidence-bundle.sh" "$seal_mv_fixture"
assert_exact_diagnostic \
    'actual-seal-publication-mv-exit-9' \
    'tool=mv result=NOT_EVALUATED exit=9'
test ! -e "$seal_mv_fixture/evidence-manifest.tsv"
test ! -e "$seal_mv_fixture/evidence-manifest.sha256"

make_targeted_fault_shim mv "$real_mv" evidence-manifest.sha256 9
seal_second_mv_fixture="$temporary/actual-seal-second-mv"
mkdir -p "$seal_second_mv_fixture"
printf '%s\n' 'payload' >"$seal_second_mv_fixture/payload.txt"
run_exact_failure \
    'actual-seal-second-publication-mv-exit-9' 9 \
    'PHASE00_EVIDENCE_BUNDLE=PASS' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='PHASE00_EVIDENCE_BUNDLE=PASS' \
    PATH="$temporary/mv-targeted-fault-shim:$PATH" \
    "$repository/build/seal-evidence-bundle.sh" \
    "$seal_second_mv_fixture"
assert_exact_diagnostic \
    'actual-seal-second-publication-mv-exit-9' \
    'tool=mv result=NOT_EVALUATED exit=9'
test ! -e "$seal_second_mv_fixture/evidence-manifest.tsv"
test ! -e "$seal_second_mv_fixture/evidence-manifest.sha256"

generator_repository="$temporary/generator-repository"
mkdir -p "$generator_repository/build/lib" \
    "$generator_repository/target/phase-00-evidence" \
    "$temporary/generator-git-success-shim"
cp "$repository/build/generate-phase-00-evidence.sh" \
    "$generator_repository/build/generate-phase-00-evidence.sh"
cp "$repository/build/lib/fail-closed-gates.sh" \
    "$generator_repository/build/lib/fail-closed-gates.sh"
printf '%s\n' 'OLD_PHASE00_BUNDLE=PASS' \
    >"$generator_repository/target/phase-00-evidence/old-pass.txt"
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'case " $* " in'
    printf '%s\n' '    *" rev-parse HEAD "*) printf "%s\n" 1111111111111111111111111111111111111111 ;;'
    printf '%s\n' '    *" branch --show-current "*) printf "%s\n" fault-probe ;;'
    printf '%s\n' '    *) printf "%s\n" "unexpected git probe: $*" >&2; exit 97 ;;'
    printf '%s\n' 'esac'
} >"$temporary/generator-git-success-shim/git"
chmod +x "$temporary/generator-git-success-shim/git"
run_exact_failure \
    'actual-generator-date-exit-9' 9 'PHASE00_EVIDENCE_BUNDLE=PASS' \
    env PHASE00_FAKE_SUCCESS_OUTPUT='PHASE00_EVIDENCE_BUNDLE=PASS' \
    PATH="$temporary/generator-git-success-shim:$temporary/date-fault-shim:$PATH" \
    "$generator_repository/build/generate-phase-00-evidence.sh"
assert_exact_diagnostic \
    'actual-generator-date-exit-9' 'tool=date result=NOT_EVALUATED exit=9'
test ! -e "$generator_repository/target/phase-00-evidence"

repro_mvnw_repository="$temporary/repro-mvnw-source"
mkdir -p \
    "$repro_mvnw_repository/.mvn" \
    "$repro_mvnw_repository/rpdptw" \
    "$repro_mvnw_repository/build" \
    "$repro_mvnw_repository/legacy" \
    "$repro_mvnw_repository/gcp" \
    "$repro_mvnw_repository/docs/implementation" \
    "$temporary/repro-mvnw-m2" \
    "$temporary/repro-mvnw-evidence"
for source_file in pom.xml mvnw mvnw.cmd .sdkmanrc Dockerfile .dockerignore README.md; do
    printf '%s\n' 'fault fixture' >"$repro_mvnw_repository/$source_file"
done
{
    printf '%s\n' '#!/bin/sh'
    printf '%s\n' 'printf "%s\n" PHASE00_REPRODUCIBLE_BUILD=PASS'
    printf '%s\n' 'printf "%s\n" INJECTED_MVNW_FAILURE >&2'
    printf '%s\n' 'exit 9'
} >"$repro_mvnw_repository/mvnw"
chmod +x "$repro_mvnw_repository/mvnw"
printf '%s\n' 'fault fixture' \
    >"$repro_mvnw_repository/docs/implementation/master-realization-plan.md"
printf '%s\n' 'PHASE00_REPRODUCIBLE_BUILD=PASS' \
    >"$temporary/repro-mvnw-evidence/reproducible-build.txt"
run_exact_failure \
    'actual-repro-mvnw-exit-9' 9 'PHASE00_REPRODUCIBLE_BUILD=PASS' \
    env PHASE00_REPRO_REPOSITORY="$repro_mvnw_repository" \
    PHASE00_MAVEN_REPO="$temporary/repro-mvnw-m2" \
    PHASE00_EVIDENCE_DIR="$temporary/repro-mvnw-evidence" \
    "$repository/build/verify-reproducible-build.sh"
gate_require_match \
    'actual reproducible mvnw exact tool label' \
    "$temporary/actual-repro-mvnw.tool.matches" \
    "$temporary/actual-repro-mvnw.tool.detector.err" \
    "$real_grep" -F 'tool=mvnw(reproducible-workspace:' \
    "$temporary/actual-repro-mvnw-exit-9.log"
assert_exact_diagnostic \
    'actual-repro-mvnw-exit-9' 'result=NOT_EVALUATED exit=9'
test ! -s "$temporary/repro-mvnw-evidence/reproducible-build.txt"

gate_require_no_match \
    'eliminated tail external call inventory' \
    "$temporary/eliminated-external.matches" \
    "$temporary/eliminated-external.detector.err" \
    rg -n '^[[:space:]]*tail[[:space:]]' \
    "$repository"/build/*.sh "$repository"/build/lib/*.sh "$repository/mvnw"

gate_capture_checked 'awk(gate-detector-fault-case-count)' \
    "$temporary/case-count.txt" "$temporary/case-count.err" \
    awk 'END { print NR + 0 }' "$temporary/results.tsv"
IFS= read -r case_count <"$temporary/case-count.txt" || :
test "$case_count" -eq 51

{
    printf '%s\n' 'PHASE00_GATE_DETECTOR_FAULT_SELF_TEST=PASS'
    printf 'CASE_COUNT=%s\n' "$case_count"
    printf '%s\n' 'ACTUAL_INVENTORY_CLASS_COUNT=30'
    printf '%s\n' 'REVIEW05_NEW_FAULT_CASE_COUNT=28'
    printf '%s\n' 'STALE_PASS_INVALIDATION_CASE_COUNT=9'
    printf '%s\n' 'ACTUAL_CALL_PATH_CASE_COUNT=12'
    printf '%s\n' 'FAKE_SUCCESS_OUTPUT_THEN_NONZERO_CASE_COUNT=28'
    printf '%s\n' 'GREP_EXIT_2_CASE_COUNT=4'
    printf '%s\n' 'GREP_EXIT_3_CASE_COUNT=6'
    printf '%s\n' 'RG_EXIT_2_CASE_COUNT=1'
    printf '%s\n' 'FIND_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'JDEPS_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'SHASUM_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'SORT_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'AWK_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'WC_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'LS_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'CMP_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'JAR_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'CP_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'DATE_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'ENV_LAUNCHER_EXIT_125_CASE_COUNT=1'
    printf '%s\n' 'GIT_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'JAVA_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'MKTEMP_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'MV_EXIT_9_CASE_COUNT=3'
    printf '%s\n' 'FIRST_SECOND_SEAL_PUBLICATION_FAULT_CASE_COUNT=2'
    printf '%s\n' 'RM_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'SED_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'TAR_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'TR_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'UNZIP_EXIT_9_CASE_COUNT=2'
    printf '%s\n' 'MVNW_EXIT_9_CASE_COUNT=1'
    printf '%s\n' 'NOT_EVALUATED_CASE_COUNT=49'
    printf '%s\n' 'NORMAL_DETECTOR_ATTACK_MATCH_CASE_COUNT=2'
    printf '%s\n' 'FALSE_GREEN_COUNT=0'
    printf '%s\n' 'case-id	expected-exit	actual-exit	evaluation-result	pass-report'
    gate_replay_text_file "$temporary/results.tsv"
} >"$temporary/report.txt"

if [ -n "$report" ]; then
    gate_publish_file_atomic 'gate-fault-self-test-report' \
        "$temporary/report.txt" "$report" "$temporary/report-publish"
else
    gate_replay_text_file "$temporary/report.txt"
fi
