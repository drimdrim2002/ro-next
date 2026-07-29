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
# Evidence commands are intentionally isolated from ambient system/user mavenrc.
# The public mvnw launcher retains the official Maven 3.9.14 rc semantics.
MAVEN_SKIP_RC=1
export MAVEN_SKIP_RC
destination="$repository/target/phase-00-evidence"
if [ -d "$destination" ]; then
    printf '%s\n' \
        'SUPERSEDED_BY_REVIEW08_FIX_REGENERATION=NOT_ACCEPTANCE_CONSUMABLE' \
        >"$destination/.phase-00-evidence-superseded"
fi
gate_make_temporary_directory 'mktemp(evidence-generation)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-generation.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM
mkdir -p "$repository/target"
superseded_destination="$repository/target/.phase-00-evidence.superseded-review08.$$"
if [ -e "$destination" ] || [ -L "$destination" ]; then
    gate_capture_checked 'mv(quarantine-stale-phase-00-evidence)' \
        "$temporary/stale-evidence-quarantine.out" \
        "$temporary/stale-evidence-quarantine.err" \
        mv "$destination" "$superseded_destination"
fi

bundle="$temporary/phase-00-evidence"
build_evidence="$bundle/E-P00-BUILD"
architecture_evidence="$bundle/E-P00-ARCH"
legacy_evidence="$bundle/E-P00-LEGACY"
commands="$bundle/command-exit-codes.tsv"
maven_repository="$repository/target/phase-00-m2"
help_plugin_snapshot="$temporary/controlled-help-plugin-cache"
workspace_owner_directory=${repository%/workspace/*}
source_maven_repository=${PHASE00_SOURCE_MAVEN_REPO:-"$workspace_owner_directory/.m2/repository"}

mkdir -p \
    "$build_evidence/logs" \
    "$architecture_evidence/logs" \
    "$legacy_evidence/logs" \
    "$legacy_evidence/pre-move" \
    "$legacy_evidence/post-move"
printf '%s\t%s\t%s\n' 'evidence-key' 'exit-code' 'command' >"$commands"

run_command() {
    key=$1
    display=$2
    recorded_display="MAVEN_SKIP_RC=1 $display"
    log=$3
    shift
    shift
    shift

    set +e
    (
        cd "$repository"
        "$@"
    ) >"$log.stdout" 2>"$log.stderr"
    exit_code=$?
    set -e

    printf '%s\t%s\t%s\n' "$key" "$exit_code" "$recorded_display" >>"$commands"
    if [ "$exit_code" -ne 0 ]; then
        printf 'Phase 00 evidence command failed (%s): %s\n' \
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

run_gate_tool_inventory_current_invocation() {
    gate_inventory_report=$1
    gate_inventory_receipt=$2
    gate_inventory_token=$3

    set +e
    ./build/run-verify-gate-tool-inventory.sh \
        "$gate_inventory_report" "$gate_inventory_receipt" \
        "$gate_inventory_token"
    gate_inventory_producer_exit=$?
    set -e
    if [ "$gate_inventory_producer_exit" -ne 0 ]; then
        ./build/run-verify-gate-tool-inventory.sh \
            --consume "$gate_inventory_report" "$gate_inventory_receipt" \
            "$gate_inventory_token" "$gate_inventory_producer_exit" || :
        return "$gate_inventory_producer_exit"
    fi
    ./build/run-verify-gate-tool-inventory.sh \
        --consume "$gate_inventory_report" "$gate_inventory_receipt" \
        "$gate_inventory_token" "$gate_inventory_producer_exit"
    rm -f -- "$gate_inventory_receipt"
}

run_expected_failure() {
    key=$1
    display=$2
    recorded_display="MAVEN_SKIP_RC=1 $display"
    log=$3
    shift
    shift
    shift

    set +e
    (
        cd "$repository"
        "$@"
    ) >"$log.stdout" 2>"$log.stderr"
    exit_code=$?
    set -e

    printf '%s\t%s\t%s\n' "$key" "$exit_code" "$recorded_display" >>"$commands"
    if [ "$exit_code" -eq 0 ]; then
        printf 'Phase 00 negative evidence command false-greened: %s\n' \
            "$recorded_display" >&2
        exit 1
    fi
    : >"$log"
    gate_replay_text_file "$log.stdout" >>"$log"
    gate_replay_text_file "$log.stderr" >>"$log"
}

traversal_index=0
next_traversal_prefix() {
    traversal_index=$((traversal_index + 1))
    GATE_GENERATOR_PREFIX="$temporary/traversal-$traversal_index"
}

manifest_tree() {
    tree_root=$1
    output=$2
    shift
    shift
    next_traversal_prefix
    prefix=$GATE_GENERATOR_PREFIX
    gate_stage_find "$tree_root" "$prefix.find.raw" "$prefix.find.err" \
        "$@" -type f ! -path '*/target/*' -print
    gate_sort_file "$prefix.find.raw" "$prefix.paths" "$prefix.sort.err"
    mkdir -p "$prefix.scratch"
    gate_classify_paths "$tree_root" "$prefix.paths" \
        "$prefix.types.tsv" "$prefix.files.txt" "$prefix.scratch"
    gate_hash_file_list "$tree_root" "$prefix.files.txt" \
        "$prefix.files.tsv" "$prefix.scratch"
    gate_capture_checked "awk(generator-manifest:$tree_root)" \
        "$output" "$prefix.manifest.err" \
        awk -F '	' 'BEGIN { OFS="\t" } { print $1, $3, $4 }' \
        "$prefix.files.tsv"
}

directory_digest() {
    directory=$1
    output=$2
    next_traversal_prefix
    prefix=$GATE_GENERATOR_PREFIX
    gate_inventory_tree "$directory" "$prefix"
    gate_capture_checked "awk(generator-directory-digest:$directory)" \
        "$output" "$prefix.directory-digest.err" \
        awk -F '	' 'BEGIN { OFS="\t" } { print $1, $3, $4 }' \
        "$GATE_TREE_FILE_MANIFEST"
    test -s "$output"
    gate_sha256_file "$output" "$prefix.directory-manifest"
    printf '%s\n' "$GATE_SHA256"
}

copy_test_reports() {
    report_root=$1
    copy_destination=$2
    mkdir -p "$copy_destination"
    next_traversal_prefix
    prefix=$GATE_GENERATOR_PREFIX
    gate_stage_find "$report_root" "$prefix.find.raw" "$prefix.find.err" \
        . -type f -path '*/target/surefire-reports/TEST-*.xml' -print
    gate_sort_file "$prefix.find.raw" "$prefix.paths" "$prefix.sort.err"
    while IFS= read -r relative_report; do
        gate_normalize_relative_path "$relative_report"
        report="$report_root/$GATE_RELATIVE_PATH"
        printf '%s\n' "${report#"$repository"/}" \
            >"$prefix.report-path.txt"
        module=$(sed 's#/target/surefire-reports/.*##; s#/#_#g' \
            "$prefix.report-path.txt")
        report_basename=${report##*/}
        cp "$report" "$copy_destination/${module}_$report_basename"
    done <"$prefix.paths"
}

collect_test_reports() {
    report_root=$1
    output=$2
    next_traversal_prefix
    prefix=$GATE_GENERATOR_PREFIX
    gate_stage_find "$report_root" "$prefix.find.raw" "$prefix.find.err" \
        . -type f -path '*/target/surefire-reports/TEST-*.xml' -print
    gate_sort_file "$prefix.find.raw" "$prefix.paths" "$prefix.sort.err"
    : >"$output"
    while IFS= read -r relative_report; do
        gate_normalize_relative_path "$relative_report"
        printf '%s/%s\n' "$report_root" "$GATE_RELATIVE_PATH" >>"$output"
    done <"$prefix.paths"
    test -s "$output"
}

sum_attribute() {
    attribute=$1
    shift
    total=0
    attribute_index=0
    for report in "$@"; do
        attribute_index=$((attribute_index + 1))
        gate_capture_checked "sed(test-report-$attribute:$attribute_index)" \
            "$temporary/test-report-$attribute-$attribute_index.txt" \
            "$temporary/test-report-$attribute-$attribute_index.err" \
            sed -n '2s/.* '"$attribute"'="\([0-9][0-9]*\)".*/\1/p' \
            "$report"
        IFS= read -r value \
            <"$temporary/test-report-$attribute-$attribute_index.txt" || :
        test -n "$value"
        total=$((total + value))
    done
    printf '%s\n' "$total"
}

gate_capture_checked 'git(evidence-generation-head)' \
    "$temporary/head-commit.txt" "$temporary/head-commit.err" \
    git -C "$repository" rev-parse HEAD
IFS= read -r head_commit <"$temporary/head-commit.txt" || :
gate_capture_checked 'git(evidence-generation-branch)' \
    "$temporary/branch.txt" "$temporary/branch.err" \
    git -C "$repository" branch --show-current
IFS= read -r branch <"$temporary/branch.txt" || :
generated_at=$(date '+%Y-%m-%dT%H:%M:%S%z')

{
    printf '%s\n' 'PHASE=00'
    printf '%s\n' 'ENTRY_GATE=AUTHORIZED_PREREQUISITE_REMEDIATION'
    printf '%s\n' 'SOURCE_SCHEDULER_TASK=019fa960-c727-77f2-9bdd-79d8ca997cfd'
    printf '%s\n' 'IMPLEMENTATION_TASK=019fa969-78fb-7590-902f-dfbf9b881c6b'
    printf '%s\n' 'FIX_01_TASK=019fa99f-c258-7912-ada4-cc13604dce90'
    printf '%s\n' 'FIX_02_SCOPE=R02-F04-01,R02-F04-02,R02-F05-01'
    printf '%s\n' 'FIX_03_TASK=019fa9e7-bbf8-7b51-a763-9bdefc72b56c'
    printf '%s\n' 'FIX_03_SCOPE=R03-F06-01,R03-F06-02'
    printf '%s\n' 'REVIEW_04_TASK=019faa06-27fb-7793-bd90-e4d3be453b56'
    printf '%s\n' 'FIX_04_SCOPE=R04-F07-01,R04-F07-02'
    printf '%s\n' 'FORMAL_REVIEW_07_TASK=019faacf-f769-7380-ab20-b69b610cde73'
    printf '%s\n' \
        'FIX_07_SCOPE=MAVEN_3_9_14_MAVENRC_MAVEN_BASEDIR_LAUNCHER_PARITY'
    printf '%s\n' 'FIX_07_TASK=019faad3-dabe-7ef1-babd-8b41cd45a73d'
    printf '%s\n' 'FORMAL_REVIEW_08_TASK=019faaee-428e-7e92-8a51-72029db4acfc'
    printf '%s\n' \
        'FIX_08_SCOPE=R08-F01_PUBLIC_MVNW_TEST_OVERRIDE_STRUCTURAL_SEPARATION'
    printf '%s\n' \
        'FIX_12_SCOPE=R12R-F01_REPORT_RECEIPT_SHA256_BINDING'
    printf '%s\n' 'FIX_12_REVIEW_TARGET=INDEPENDENT_REVIEW_13'
    printf 'ENTRY_HEAD=%s\n' "$head_commit"
    printf 'ENTRY_BRANCH=%s\n' "$branch"
    printf '%s\n' 'USER_OWNED_UNTRACKED_TREE=docs/implementation/human-guides/'
    printf '%s\n' 'USER_OWNED_UNTRACKED_TREE_DISPOSITION=PRESERVED_AND_EXCLUDED'
    printf '%s\n' 'SCHEDULER_CONCURRENT_EDIT=docs/implementation/execution-progress-and-results.md'
    printf '%s\n' 'SCHEDULER_CONCURRENT_EDIT_DISPOSITION=PRESERVED_AND_EXCLUDED'
    printf '%s\n' 'CONTROLLED_EVIDENCE_COMMAND_ENV=MAVEN_SKIP_RC=1'
    printf '%s\n' 'PUBLIC_MVNW_MAVEN_SKIP_RC_POLICY=OFFICIAL_OPT_IN_ONLY'
    printf 'EVIDENCE_GENERATED_AT=%s\n' "$generated_at"
} >"$bundle/entry-gate.txt"

authority_files='
docs/master-design.md
docs/2026-07-26-domain-design.md
docs/2026-07-26-architecture-design.md
docs/architecture-domain-implementation-design.md
docs/master-design-open-questions.md
docs/implementation/README.md
docs/implementation/master-realization-plan.md
docs/implementation/execution-progress-and-results.md
docs/implementation/phases/phase-00-build-architecture-skeleton.md
docs/implementation/reviews/phase-00-review.md
docs/implementation/phases/phase-01-canonical-input-normalization.md
docs/implementation/reviews/phase-01-review.md
'
: >"$build_evidence/authority-manifest.tsv"
authority_index=0
for authority_file in $authority_files; do
    test -f "$repository/$authority_file"
    authority_index=$((authority_index + 1))
    authority_prefix="$temporary/authority-$authority_index"
    gate_file_size "$repository/$authority_file" "$authority_prefix"
    bytes=$GATE_FILE_SIZE
    gate_sha256_file "$repository/$authority_file" "$authority_prefix"
    digest=$GATE_SHA256
    printf '%s\t%s\t%s\n' "$authority_file" "$bytes" "$digest" >>"$build_evidence/authority-manifest.tsv"
done

{
    printf '%s\n' 'PHASE=00'
    printf '%s\n' 'IMPLEMENTATION_STATE=IMPLEMENTED_PENDING_INDEPENDENT_REVIEW_13'
    printf '%s\n' 'EVIDENCE_LIFECYCLE_STATE=PRE_REVIEW_EVIDENCE_ONLY'
    printf '%s\n' 'PHASE_01_HANDOFF=BLOCKED'
} >"$bundle/implementation-state.txt"

{
    printf '%s\n' \
        'SUPERSEDED_FIX06_BUNDLE_SHA256=36f18f7fa46ea3037bf6643b762f23ce49ced6e4014bcb4eaf9398b008e8920c'
    printf '%s\n' \
        'SUPERSEDED_FIX06_CONSUMPTION_STATUS=REJECTED_NOT_ACCEPTANCE_CONSUMABLE'
    printf '%s\n' \
        'SUPERSESSION_REASON=REVIEW07_MAVEN_3_9_14_NORMAL_LAUNCHER_CONTRACT_DIVERGENCE'
    printf '%s\n' \
        'SUPERSEDED_FIX07_BUNDLE_SHA256=9d5335cb2a1b7e3f619eac99eb1593068d8fea47cb6cb8074b0833ea318ca4ba'
    printf '%s\n' \
        'SUPERSEDED_FIX07_PRE_REVIEW_MANIFEST_SHA256=8f96e060abfc5e4afbf02d414c7280f6743c5be15d41803f032592a6f22b0cd4'
    printf '%s\n' \
        'SUPERSEDED_FIX07_CONSUMPTION_STATUS=REJECTED_NOT_ACCEPTANCE_CONSUMABLE'
    printf '%s\n' \
        'FIX07_SUPERSESSION_REASON=REVIEW08_R08_F01_PUBLIC_TEST_OVERRIDE_BYPASS'
    printf '%s\n' \
        'REPLACEMENT_LIFECYCLE=FIX08_PRE_REVIEW_EVIDENCE_PENDING_INDEPENDENT_REVIEW_09'
} >"$bundle/superseded-evidence.txt"

{
    printf '%s\n' 'Q-BENCH-02=OPEN_EXPERIMENT_REQUIRED'
    printf '%s\n' 'C-17=GATED_NO_ROUTE_POOL_OR_MIP_MODULE_OR_DEPENDENCY'
    printf '%s\n' 'Q-VAR-01=DEFERRED_NO_VARIANT_SKELETON'
    printf '%s\n' 'MULTI_TRIP_ROTATION=DEFERRED'
    printf '%s\n' 'AWS_IMPLEMENTATION_CUTOVER=GATED'
    printf '%s\n' 'PUBLIC_JAVA_API_SCHEMA=OPEN_NOT_APPROVED'
    printf '%s\n' 'ARCHITECTURE_ENGINE=DEPENDENCY_FREE_JDK_SCANNER'
    printf '%s\n' 'ARCHITECTURE_ENGINE_EXTERNAL_LICENSE=N/A'
} >"$bundle/open-gated-deferred.txt"

help_cache_candidate=${PHASE00_HELP_PLUGIN_CACHE:-}
help_cache_provenance=${PHASE00_HELP_PLUGIN_CACHE_PROVENANCE:-}
if [ -z "$help_cache_candidate" ]; then
    for candidate in \
        "$repository/target/phase-00-m2" \
        "$source_maven_repository" \
        /private/tmp/ro-next-review04/m2-help
    do
        if [ -f "$candidate/org/apache/maven/plugins/maven-help-plugin/3.5.1/maven-help-plugin-3.5.1.jar" ] &&
            [ -f "$candidate/org/apache/maven/plugins/maven-help-plugin/3.5.1/maven-help-plugin-3.5.1.pom" ]; then
            help_cache_candidate=$candidate
            case "$candidate" in
                "$repository/target/"*)
                    help_cache_provenance=PRE_CLEAN_CHECKOUT_CACHE_SNAPSHOT
                    ;;
                "$source_maven_repository")
                    help_cache_provenance=LOCAL_MAVEN_CACHE_SNAPSHOT
                    ;;
                *)
                    help_cache_provenance=REVIEW_04_PUBLIC_APACHE_CACHE_SNAPSHOT
                    ;;
            esac
            break
        fi
    done
fi
if [ -z "$help_cache_candidate" ]; then
    help_cache_candidate="$temporary/help-plugin-controlled-warmup"
    mkdir -p "$help_cache_candidate"
    run_command 'E-P00-BUILD' 'controlled Maven Central warm-up: org.apache.maven.plugins:maven-help-plugin:3.5.1' \
        "$build_evidence/logs/help-plugin-controlled-warmup.log" \
        ./mvnw -N -B -ntp -U \
        -s build/maven-central-only-settings.xml \
        -Dmaven.repo.local="$help_cache_candidate" \
        org.apache.maven.plugins:maven-help-plugin:3.5.1:help
    help_cache_provenance=CONTROLLED_EXACT_MAVEN_CENTRAL_WARMUP
fi
run_command 'E-P00-BUILD' './build/stage-maven-help-plugin-cache.sh <candidate-cache> <pre-clean-snapshot> <provenance>' \
    "$build_evidence/logs/help-plugin-cache-stage.log" \
    env PHASE00_HELP_PLUGIN_CACHE_PROVENANCE="$help_cache_provenance" \
    ./build/stage-maven-help-plugin-cache.sh \
    "$help_cache_candidate" "$help_plugin_snapshot" \
    "$build_evidence/help-plugin-cache-provenance.txt"

run_command 'E-P00-BUILD' './mvnw --version' \
    "$build_evidence/logs/wrapper-version.log" ./mvnw --version
run_command 'E-P00-BUILD' './build/verify-toolchain.sh' \
    "$build_evidence/logs/toolchain-policy.log" ./build/verify-toolchain.sh
run_command 'E-P00-BUILD' './mvnw -B -ntp toolchains:display-discovered-jdk-toolchains' \
    "$build_evidence/logs/discovered-toolchains.log" \
    ./mvnw -B -ntp toolchains:display-discovered-jdk-toolchains
run_command 'E-P00-BUILD' './mvnw -B -ntp -Dstyle.color=never validate' \
    "$build_evidence/logs/validate.log" \
    ./mvnw -B -ntp -Dstyle.color=never validate

# This exact clean is also the controlled removal of prior ignored build outputs.
run_command 'E-P00-BUILD' './mvnw -B -ntp -Dstyle.color=never clean verify' \
    "$build_evidence/logs/root-clean-verify.log" \
    ./mvnw -B -ntp -Dstyle.color=never clean verify

run_command 'E-P00-ARCH' './build/test-evidence-bundle-integrity.sh <sealed-report>' \
    "$architecture_evidence/logs/evidence-integrity-self-test-command.log" \
    ./build/test-evidence-bundle-integrity.sh \
    "$architecture_evidence/evidence-verifier-negative-self-test.txt"
run_command 'E-P00-ARCH' './build/test-gate-detector-faults.sh <fault-report>' \
    "$architecture_evidence/logs/gate-detector-fault-self-test-command.log" \
    ./build/test-gate-detector-faults.sh \
    "$architecture_evidence/gate-detector-fault-self-test.txt"
run_command 'E-P00-ARCH' './build/test-maven-wrapper-faults.sh <fault-report>' \
    "$architecture_evidence/logs/maven-wrapper-fault-self-test-command.log" \
    ./build/test-maven-wrapper-faults.sh \
    "$architecture_evidence/maven-wrapper-fault-self-test.txt"
run_command 'E-P00-ARCH' './build/test-maven-launcher-parity.sh <parity-report>' \
    "$architecture_evidence/logs/maven-launcher-parity-command.log" \
    ./build/test-maven-launcher-parity.sh \
    "$architecture_evidence/maven-3.9.14-launcher-parity.txt"
run_command 'E-P00-ARCH' './build/test-git-whitespace-semantics.sh <fault-report>' \
    "$architecture_evidence/logs/git-whitespace-self-test-command.log" \
    ./build/test-git-whitespace-semantics.sh \
    "$architecture_evidence/git-whitespace-semantics-self-test.txt"
run_command 'E-P00-ARCH' './build/test-gate-tool-inventory-receipt.sh' \
    "$architecture_evidence/gate-tool-inventory-receipt-self-test.txt" \
    ./build/test-gate-tool-inventory-receipt.sh
run_command 'E-P00-ARCH' './build/test-gate-tool-inventory-receipt.sh --r11-only' \
    "$architecture_evidence/gate-tool-inventory-r11-f01-regression.txt" \
    ./build/test-gate-tool-inventory-receipt.sh --r11-only
gate_inventory_public_report=\
"$architecture_evidence/recursive-gate-tool-inventory.txt"
gate_inventory_public_receipt=\
"$architecture_evidence/.recursive-gate-tool-inventory.receipt"
gate_inventory_public_token=\
"phase00-generator-${temporary##*.}-$$"
run_command 'E-P00-ARCH' './build/run-verify-gate-tool-inventory.sh <report> <caller-receipt> <fresh-token>; --consume <report> <caller-receipt> <same-token> 0' \
    "$architecture_evidence/logs/recursive-gate-tool-inventory-command.log" \
    run_gate_tool_inventory_current_invocation \
    "$gate_inventory_public_report" "$gate_inventory_public_receipt" \
    "$gate_inventory_public_token"

run_command 'E-P00-LEGACY' './mvnw -B -ntp -Dstyle.color=never -pl legacy/gcp-placeholder -am test' \
    "$legacy_evidence/logs/legacy-test.log" \
    ./mvnw -B -ntp -Dstyle.color=never -pl legacy/gcp-placeholder -am test
run_command 'E-P00-LEGACY' './mvnw -B -ntp -Dstyle.color=never -pl legacy/gcp-placeholder -am package' \
    "$legacy_evidence/logs/legacy-package.log" \
    ./mvnw -B -ntp -Dstyle.color=never -pl legacy/gcp-placeholder -am package

run_command 'E-P00-ARCH' './mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core,rpdptw/solver,rpdptw/verification,rpdptw/application,rpdptw/capabilities,rpdptw/profile-catalog -am verify' \
    "$architecture_evidence/logs/stable-modules-verify.log" \
    ./mvnw -B -ntp -Dstyle.color=never \
    -pl rpdptw/core,rpdptw/solver,rpdptw/verification,rpdptw/application,rpdptw/capabilities,rpdptw/profile-catalog \
    -am verify
run_command 'E-P00-ARCH' './mvnw -B -ntp -Dstyle.color=never -pl build/test-fixtures -am verify' \
    "$architecture_evidence/logs/test-fixtures-verify.log" \
    ./mvnw -B -ntp -Dstyle.color=never -pl build/test-fixtures -am verify
run_command 'E-P00-ARCH' './mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules -am test' \
    "$architecture_evidence/logs/architecture-rules-test.log" \
    ./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules -am test
run_expected_failure 'E-P00-ARCH' './mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules -am -Dtest=NoSuchPhase00ArchitectureTest -Dsurefire.failIfNoSpecifiedTests=true test # expected non-zero' \
    "$architecture_evidence/logs/targeted-test-name-typo-negative.log" \
    ./mvnw -B -ntp -Dstyle.color=never \
    -pl build/architecture-rules -am \
    -Dtest=NoSuchPhase00ArchitectureTest \
    -Dsurefire.failIfNoSpecifiedTests=true test
run_command 'E-P00-ARCH' './build/verify-bytecode-boundaries.sh' \
    "$architecture_evidence/logs/bytecode-boundaries.log" ./build/verify-bytecode-boundaries.sh

run_command 'E-P00-ARCH' './build/verify-phase-00-source-scans.sh --scan provider' \
    "$architecture_evidence/logs/provider-source-scan.log" \
    ./build/verify-phase-00-source-scans.sh --scan provider
run_command 'E-P00-ARCH' './build/verify-phase-00-source-scans.sh --scan ortools' \
    "$architecture_evidence/logs/ortools-source-scan.log" \
    ./build/verify-phase-00-source-scans.sh --scan ortools
run_command 'E-P00-ARCH' './build/verify-phase-00-source-scans.sh --self-test <fault-report>' \
    "$architecture_evidence/logs/source-scan-fault-self-test-command.log" \
    ./build/verify-phase-00-source-scans.sh --self-test \
    "$architecture_evidence/source-scan-fault-self-test.txt"

run_command 'E-P00-BUILD' './build/seed-isolated-maven-repository.sh <local-cache> target/phase-00-m2 <controlled-help-plugin-snapshot> <provenance>' \
    "$build_evidence/logs/isolated-repository-seed.log" \
    ./build/seed-isolated-maven-repository.sh \
    "$source_maven_repository" "$maven_repository" "$help_plugin_snapshot" \
    "$build_evidence/isolated-repository-provenance.txt"
run_command 'E-P00-BUILD' './mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 verify' \
    "$build_evidence/logs/offline-verify.log" \
    ./mvnw -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local=target/phase-00-m2 verify

run_command 'E-P00-BUILD' 'PHASE00_MAVEN_REPO=target/phase-00-m2 ./build/write-effective-pom.sh <official-output> <comparison-report>' \
    "$build_evidence/logs/official-effective-pom.log" \
    env PHASE00_MAVEN_REPO="$maven_repository" \
    ./build/write-effective-pom.sh \
    "$build_evidence/effective-pom.xml" \
    "$build_evidence/effective-pom-comparison.txt"
run_command 'E-P00-ARCH' './mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 dependency:tree -Dverbose' \
    "$architecture_evidence/dependency-tree-reactor.txt" \
    ./mvnw -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local=target/phase-00-m2 dependency:tree -Dverbose
run_command 'E-P00-ARCH' './mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 -pl rpdptw/verification -am dependency:tree' \
    "$architecture_evidence/dependency-tree-verification.txt" \
    ./mvnw -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local=target/phase-00-m2 -pl rpdptw/verification -am dependency:tree
run_command 'E-P00-ARCH' './mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 -pl build/architecture-rules -am dependency:tree' \
    "$architecture_evidence/dependency-tree-test-fixture-consumer.txt" \
    ./mvnw -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local=target/phase-00-m2 -pl build/architecture-rules -am dependency:tree
run_command 'E-P00-LEGACY' './mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 -pl legacy/gcp-placeholder dependency:tree -Dverbose' \
    "$legacy_evidence/post-move/dependency-tree-verbose.txt" \
    ./mvnw -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local=target/phase-00-m2 -pl legacy/gcp-placeholder dependency:tree -Dverbose

mkdir -p "$temporary/offline-cache-missing"
run_expected_failure 'E-P00-BUILD' './mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=<empty-cache> verify # expected non-zero' \
    "$build_evidence/logs/offline-cache-missing-negative.log" \
    ./mvnw -B -ntp -Dstyle.color=never -o \
    -Dmaven.repo.local="$temporary/offline-cache-missing" verify
mkdir -p "$temporary/effective-pom-cache-missing"
run_expected_failure 'E-P00-BUILD' './mvnw -B -ntp -o -Dmaven.repo.local=<empty-cache> org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom # expected non-zero' \
    "$build_evidence/logs/effective-pom-empty-cache-negative.log" \
    ./mvnw -B -ntp -o \
    -Dmaven.repo.local="$temporary/effective-pom-cache-missing" \
    org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom \
    -Doutput="$temporary/effective-pom-cache-missing.xml"

run_command 'E-P00-LEGACY' 'PHASE00_MAVEN_REPO=target/phase-00-m2 ./build/capture-pre-move-legacy-baseline.sh <E-P00-LEGACY>' \
    "$legacy_evidence/logs/pre-move-baseline-command.log" \
    env PHASE00_MAVEN_REPO="$maven_repository" \
    ./build/capture-pre-move-legacy-baseline.sh "$legacy_evidence"

run_command 'E-P00-BUILD' 'PHASE00_MAVEN_REPO=target/phase-00-m2 ./build/verify-reproducible-build.sh' \
    "$build_evidence/logs/reproducible-build-command.log" \
    env PHASE00_MAVEN_REPO="$maven_repository" PHASE00_EVIDENCE_DIR="$build_evidence" \
    ./build/verify-reproducible-build.sh

manifest_tree "$repository" "$build_evidence/implementation-source-manifest.tsv" \
    pom.xml .mvn mvnw mvnw.cmd .sdkmanrc rpdptw build legacy gcp Dockerfile .dockerignore README.md
gate_sha256_file "$build_evidence/implementation-source-manifest.tsv" \
    "$temporary/implementation-source-manifest"
implementation_source_digest=$GATE_SHA256

next_traversal_prefix
isolated_prefix=$GATE_GENERATOR_PREFIX
gate_stage_find "$maven_repository" "$isolated_prefix.find.raw" \
    "$isolated_prefix.find.err" \
    . -type f \( -name '*.jar' -o -name '*.pom' \) -print
gate_sort_file "$isolated_prefix.find.raw" "$isolated_prefix.paths" \
    "$isolated_prefix.sort.err"
mkdir -p "$isolated_prefix.scratch"
gate_classify_paths "$maven_repository" "$isolated_prefix.paths" \
    "$isolated_prefix.types.tsv" "$isolated_prefix.files.txt" \
    "$isolated_prefix.scratch"
gate_hash_file_list "$maven_repository" "$isolated_prefix.files.txt" \
    "$isolated_prefix.files.tsv" "$isolated_prefix.scratch"
gate_capture_checked 'awk(isolated-repository-artifact-manifest)' \
    "$build_evidence/isolated-repository-artifact-manifest.tsv" \
    "$isolated_prefix.artifact-manifest.err" \
    awk -F '	' 'BEGIN { OFS="\t" } { print $1, $3, $4 }' \
    "$isolated_prefix.files.tsv"
gate_sha256_file \
    "$build_evidence/isolated-repository-artifact-manifest.tsv" \
    "$temporary/isolated-repository-manifest"
isolated_repository_digest=$GATE_SHA256
gate_sha256_file "$build_evidence/effective-pom.xml" \
    "$temporary/effective-pom"
effective_pom_digest=$GATE_SHA256

{
    printf 'IMPLEMENTATION_HEAD=%s\n' "$head_commit"
    printf 'IMPLEMENTATION_SOURCE_MANIFEST_SHA256=%s\n' "$implementation_source_digest"
    printf 'EFFECTIVE_POM_SHA256=%s\n' "$effective_pom_digest"
    printf 'ISOLATED_REPOSITORY_MANIFEST_SHA256=%s\n' "$isolated_repository_digest"
    printf '%s\n' 'CONTROLLED_EVIDENCE_MAVEN_SKIP_RC=1'
    printf '%s\n' 'PUBLIC_WRAPPER_MAVENRC_SEMANTICS=APACHE_MAVEN_3_9_14'
    printf '%s\n' 'OUTPUT_TIMESTAMP=2026-07-28T00:00:00Z'
    printf '%s\n' 'REPRODUCIBILITY_ENVELOPE=macOS-aarch64/current-Java-25-runtime'
    printf '%s\n' 'CROSS_OS_BIT_FOR_BIT_CLAIM=NOT_MADE'
} >"$build_evidence/build-fingerprint.txt"

gate_capture_checked 'git(ls-tree:pre-move-legacy-source)' \
    "$temporary/baseline-paths.txt" "$temporary/baseline-paths.err" \
    git -C "$repository" ls-tree -r --name-only "$head_commit" -- \
    pom.xml Dockerfile gcp src/main/java/com/ronext/optimizer \
    src/test/java/com/ronext/optimizer
: >"$legacy_evidence/pre-move-legacy-manifest.tsv"
baseline_index=0
while IFS= read -r baseline_path; do
    test -n "$baseline_path" || continue
    baseline_index=$((baseline_index + 1))
    gate_capture_checked "git(cat-file-size:$baseline_path)" \
        "$temporary/baseline-size-$baseline_index.txt" \
        "$temporary/baseline-size-$baseline_index.err" \
        git -C "$repository" cat-file -s "$head_commit:$baseline_path"
    IFS= read -r bytes <"$temporary/baseline-size-$baseline_index.txt" || :
    gate_capture_checked "git(show:$baseline_path)" \
        "$temporary/baseline-source-file" \
        "$temporary/baseline-source-file.err" \
        git -C "$repository" show "$head_commit:$baseline_path"
    gate_sha256_file "$temporary/baseline-source-file" \
        "$temporary/baseline-source-file"
    digest=$GATE_SHA256
    printf '%s\t%s\t%s\n' "$baseline_path" "$bytes" "$digest" \
        >>"$legacy_evidence/pre-move-legacy-manifest.tsv"
done <"$temporary/baseline-paths.txt"
manifest_tree "$repository" "$legacy_evidence/post-move-legacy-manifest.tsv" \
    legacy/gcp-placeholder/src legacy/pom.xml legacy/gcp-placeholder/pom.xml gcp Dockerfile

legacy_jar="$repository/legacy/gcp-placeholder/target/legacy-gcp-placeholder-0.1.0-SNAPSHOT-app.jar"
test -f "$legacy_jar"
gate_sha256_file "$legacy_jar" "$temporary/post-move-shaded-jar"
legacy_jar_basename=${legacy_jar##*/}
printf '%s  %s\n' "$GATE_SHA256" "$legacy_jar_basename" \
    >"$legacy_evidence/post-move/shaded-jar.sha256"
gate_capture_checked 'unzip(post-move-shaded-manifest)' \
    "$legacy_evidence/post-move/shaded-manifest.mf" \
    "$temporary/post-move-shaded-manifest.err" \
    unzip -p "$legacy_jar" META-INF/MANIFEST.MF
gate_capture_checked 'jar(list:post-move-shaded-application)' \
    "$temporary/post-move-shaded-entries.unsorted" \
    "$temporary/post-move-shaded-entries.err" jar tf "$legacy_jar"
gate_sort_file "$temporary/post-move-shaded-entries.unsorted" \
    "$legacy_evidence/post-move/shaded-entry-inventory.txt" \
    "$temporary/post-move-shaded-entries.sort.err"
gate_require_match \
    'post-move selected service/resource inventory' \
    "$legacy_evidence/post-move/selected-service-resource-inventory.txt" \
    "$temporary/post-move-resource.detector.err" \
    grep -E \
    'META-INF/services/|META-INF/(LICENSE|NOTICE)|module-info[.]class' \
    "$legacy_evidence/post-move/shaded-entry-inventory.txt"
gate_require_match \
    'post-move Shade warning inventory' \
    "$legacy_evidence/post-move/shade-collision-warnings.txt" \
    "$temporary/post-move-shade.detector.err" \
    grep -E '^\[WARNING\]' "$legacy_evidence/logs/legacy-package.log"
gate_require_match \
    'post-move Jackson conflict inventory' \
    "$legacy_evidence/post-move/jackson-conflict-inventory.txt" \
    "$temporary/post-move-jackson.detector.err" \
    grep -E 'jackson-(core|annotations|databind).*omitted for conflict' \
    "$legacy_evidence/post-move/dependency-tree-verbose.txt"
test -s "$legacy_evidence/post-move/selected-service-resource-inventory.txt"
test -s "$legacy_evidence/post-move/shade-collision-warnings.txt"
test -s "$legacy_evidence/post-move/jackson-conflict-inventory.txt"

{
    printf '%s\n' 'CLASSIFICATION=LEGACY_COMPATIBILITY_INVENTORY_NOT_TARGET_AUTHORITY'
    printf '%s\n' 'HTTP_PUBLIC_PATH=POST /optimizations'
    printf '%s\n' 'HTTP_RESULT_PATH=GET /optimizations/{requestId}'
    printf '%s\n' 'HTTP_INTERNAL_BATCH_PATH=POST /internal/batches'
    printf '%s\n' 'HTTP_INTERNAL_FINALIZE_PATH=POST /internal/finalize'
    printf '%s\n' 'HTTP_STATUS_INVALID_OR_MISSING_INPUT=400'
    printf '%s\n' 'HTTP_STATUS_UNSUPPORTED_PATH=404'
    printf '%s\n' 'HTTP_STATUS_PUBLIC_UNSUPPORTED_METHOD=404'
    printf '%s\n' 'HTTP_STATUS_INTERNAL_UNSUPPORTED_METHOD=405'
    printf '%s\n' 'HTTP_STATUS_EMPTY_RESULT_ID=400'
    printf '%s\n' 'HTTP_STATUS_MISSING_RESULT=202'
    printf '%s\n' 'HTTP_STATUS_STORAGE_OR_WORKFLOW_FAILURE=500_REDACTED'
    printf '%s\n' 'DEFAULT_PARALLEL_RUNS=8'
    printf '%s\n' 'DEFAULT_ITERATIONS_PER_RUN=5000'
    printf '%s\n' 'PARALLEL_RUNS_CLAMP=1..20'
    printf '%s\n' 'ITERATIONS_PER_RUN_CLAMP=100..250000'
    printf '%s\n' 'CANDIDATE_KEY=candidates/{requestId}/{runNumber}.json'
    printf '%s\n' 'RESULT_KEY=results/{requestId}.json'
    printf '%s\n' 'FINALIZATION=VISIBLE_PREFIX_LISTING_PLUS_RAW_MINIMUM_OBJECTIVE'
    printf '%s\n' 'WORKFLOW=PARALLEL_RANGE_THEN_FINALIZE'
    printf '%s\n' 'WORKER_SEED=BASE_SEED_PLUS_RUN_NUMBER'
    printf '%s\n' 'SYNTHETIC_OBJECTIVE=PRESERVED_BY_GOLDEN_TEST'
    printf '%s\n' 'KNOWN_TARGET_MISMATCH=PREFIX_LISTING_AND_RAW_OBJECTIVE_ARE_NOT_TARGET_CONTRACT'
    printf '%s\n' 'CLOUD_CREDENTIAL_REQUIRED_FOR_TESTS=NO'
} >"$legacy_evidence/observable-behavior-contract.txt"

{
    printf '%s\n' 'POLICY=EXACT_LEGACY_ONLY_COORDINATES_AND_SHADE_BEHAVIOR_PRESERVED'
    printf '%s\n' 'TARGET_MODULE_STRICT_CONVERGENCE=ENFORCED'
    printf '%s\n' 'LEGACY_GLOBAL_ENFORCER_SKIP=NOT_PRESENT'
    printf '%s\n' 'LEGACY_SILENT_ALIGNMENT_OR_EXCLUSION=NOT_PERFORMED'
    printf '%s\n' 'RESOLVED_GRAPH_AND_COLLISIONS=pre-move/,post-move/,legacy-pre-post-comparison.txt'
    printf '%s\n' 'OWNER_APPROVAL_ISSUED_BY_FIX_SESSION=NO'
    printf '%s\n' 'OWNER_REVIEW_02_DECISION=APPROVE'
    printf '%s\n' 'OWNER_APPROVED_SCOPE=PRE_EXISTING_JACKSON_SHADE_NARROW_LEGACY_ONLY_EXCEPTION_POLICY'
    printf '%s\n' 'NEW_DEPENDENCY_LICENSE_PROVIDER_AUTHORITY=NO'
    printf '%s\n' 'PHASE_ACCEPTANCE_AUTHORITY=NO'
} >"$legacy_evidence/legacy-dependency-policy.txt"

{
    printf '%s\n' 'ALLOWED_COMPILE_DAG:'
    printf '%s\n' 'rpdptw-core -> (none)'
    printf '%s\n' 'rpdptw-solver -> rpdptw-core'
    printf '%s\n' 'rpdptw-verification -> rpdptw-core'
    printf '%s\n' 'rpdptw-application -> rpdptw-core,rpdptw-solver,rpdptw-verification'
    printf '%s\n' 'rpdptw-capabilities -> rpdptw-core'
    printf '%s\n' 'rpdptw-profile-catalog -> rpdptw-core'
    printf '%s\n' 'TEST_ONLY: rpdptw-architecture-rules -> stable modules,rpdptw-test-fixtures:tests'
    printf '%s\n' 'ISOLATED: legacy-gcp-placeholder'
    printf '%s\n' 'REACTOR_CYCLE_COUNT=0'
} >"$architecture_evidence/allowed-module-dag.txt"

{
    printf '%s\n' 'ENGINE=dependency-free JDK source/XML/bytecode scanner'
    printf '%s\n' 'POSITIVE=reactor topology and exact stable dependency edges'
    printf '%s\n' 'POSITIVE=stable provider/cloud/vendor/legacy source and bytecode absence'
    printf '%s\n' 'POSITIVE=verification to solver/search/cache/legacy absence'
    printf '%s\n' 'POSITIVE=cross-module internal access absence'
    printf '%s\n' 'POSITIVE=core environment/clock/global-random/static-mutable absence'
    printf '%s\n' 'POSITIVE=declared customer conditional and capability advertisement absence'
    printf '%s\n' 'POSITIVE=test-fixture classifier/scope and production leakage absence'
    printf '%s\n' 'NEGATIVE=provider reference fixture is rejected'
    printf '%s\n' 'NEGATIVE=ambient clock/random/static mutable fixture is rejected'
    printf '%s\n' 'NEGATIVE=cross-module internal fixture is rejected'
    printf '%s\n' 'NEGATIVE=fixture production-scope fixture is rejected'
    printf '%s\n' 'NEGATIVE=customer if/switch/preset fixture is rejected'
    printf '%s\n' 'NEGATIVE=capability service/class advertisement fixture is rejected'
    printf '%s\n' 'NEGATIVE=evidence seal missing/interrupted/mutation/symlink/path traversal is rejected'
    printf '%s\n' 'NEGATIVE=unreadable subtree/file, hidden payload omission, and forced find exit are rejected'
    printf '%s\n' 'SECURITY=known-marker canary and final bundle redaction scanner'
    printf '%s\n' 'FAULT=grep exit 2 is detector execution failure without PASS report'
    printf '%s\n' 'FAULT=grep exit 3 is detector execution failure without PASS report'
    printf '%s\n' 'FAULT=rg exit 2 is detector execution failure without evaluated match result'
    printf '%s\n' 'FAULT=find non-zero is traversal failure without a partial seal'
    printf '%s\n' 'FAULT=jdeps,shasum,sort,awk,wc,ls,cmp,jar exit 9 preserve exact exit and NOT_EVALUATED without PASS'
    printf '%s\n' 'FAULT=recursive wrapper/launcher inventory covers 13 actual and 14 conditional prerequisite classes'
    printf '%s\n' 'RECEIPT=caller-owned fresh token, producer exit 0, same-token PASS receipt and deterministic report validation are required; receipt/token are excluded from sealed payload'
    printf '%s\n' 'CALL_GRAPH=evidence generator and architecture JUnit invoke run-verify-gate-tool-inventory.sh; raw verifier is wrapper-owned'
    printf '%s\n' 'FAULT=stale PASS, wrong/old/missing token, semantic/external/stage/write/permission/rename/cleanup/signal/alias/concurrency/report-parent replacement are fail-closed'
    printf '%s\n' 'FAULT=missing/unusable TMPDIR and wrapper mktemp shim failures reject current-token consumption even when an older deterministic PASS report remains'
    printf '%s\n' 'FAULT=22 external-command attacks preserve exact exit 41; mavenrc syntax preserves exit 2; controlled-launcher,controlled-core,Classworlds,launcher-config structural attacks preserve 126/1'
    printf '%s\n' 'FAULT=uname,dirname,basename,tr,sh,shasum,java,mavenrc source,dynamic Maven executable,controlled launcher/core,Classworlds inventory,launcher config are fail-closed boundaries'
    printf '%s\n' 'PARITY=18 exact official Maven 3.9.14 stdout/stderr/exit A/B pairs plus 1 JAVA_HOME diagnostic enrichment, 1 rc-source fail-closed exception, 2 private positional system-rc cases and 1 reactor JUnit bridge'
    printf '%s\n' 'ATTACK=14 public PHASE00_MVNW_TEST_* standalone/combination attacks preserve baseline or syntax-error exit 2; production source reference count is zero'
    printf '%s\n' 'FAULT=cat,cygpath,curl,expr,javac,ls,mkdir,mktemp,mv,rm,sha256sum,tar,unzip,wget conditional faults are injected only by the private positional driver'
    printf '%s\n' 'GIT_CHECK=tracked exit 2 and no-index exit 3 whitespace diagnostics are EVALUATED_FAILURE'
    printf '%s\n' 'GIT_CHECK=no-index ordinary difference exit 1 is evaluated clean; malformed/fake-success nonzero is NOT_EVALUATED'
    printf '%s\n' 'ELIMINATED_EXTERNAL=tail has no Phase 00 acceptance-path invocation'
    printf '%s\n' 'NON_GATE=private scratch cleanup rm never replaces the primary exact exit'
    printf '%s\n' 'FAULT=manifest, bytecode, and reproducibility predicates are targeted independently'
    printf '%s\n' 'NEGATIVE=legacy dependency duplicate/reparent/conflict/multiplicity/coordinate mutations are rejected'
    printf '%s\n' 'LIFECYCLE=canonical allowlisted pre-review manifest without review or acceptance metadata'
    printf '%s\n' 'STRUCTURAL_STRING_COMPLETENESS_CLAIM=NOT_MADE'
} >"$architecture_evidence/scanner-coverage-manifest.txt"

gate_capture_checked 'cp(gate-tool-inventory-evidence)' \
    "$temporary/gate-tool-inventory-copy.out" \
    "$temporary/gate-tool-inventory-copy.err" \
    cp "$repository/build/phase-00-gate-tool-inventory.tsv" \
        "$architecture_evidence/gate-tool-inventory.tsv"
gate_capture_checked 'cp(maven-launcher-contract-evidence)' \
    "$temporary/maven-launcher-contract-copy.out" \
    "$temporary/maven-launcher-contract-copy.err" \
    cp "$repository/build/maven-3.9.14-launcher-contract.tsv" \
        "$architecture_evidence/maven-3.9.14-launcher-contract.tsv"

manifest_tree "$repository" "$architecture_evidence/architecture-policy-source-manifest.tsv" \
    build/architecture-rules/src build/architecture-rules/pom.xml \
    build/test-fixtures/src build/test-fixtures/pom.xml \
    build/verify-bytecode-boundaries.sh \
    build/verify-evidence-redaction.sh \
    build/verify-phase-00-source-scans.sh \
    build/test-evidence-bundle-integrity.sh \
    build/test-gate-detector-faults.sh \
    build/test-maven-launcher-parity.sh \
    build/test-maven-wrapper-faults.sh \
    build/test-support/maven-private-test-driver.sh \
    build/test-git-whitespace-semantics.sh \
    build/test-gate-tool-inventory-receipt.sh \
    build/run-verify-gate-tool-inventory.sh \
    build/verify-gate-tool-inventory.sh \
    build/phase-00-gate-tool-inventory.tsv \
    build/phase-00-gate-source-manifest.tsv \
    build/maven-3.9.14-launcher-contract.tsv \
    build/lib/fail-closed-gates.sh \
    build/lib/maven-launcher-fail-closed.sh \
    build/lib/maven-launcher-core.sh \
    build/lib/maven-wrapper-fail-closed.sh \
    build/write-effective-pom.sh \
    build/stage-maven-help-plugin-cache.sh \
    build/maven-help-plugin-3.5.1-cache.sha256 \
    build/seed-isolated-maven-repository.sh \
    build/compare-legacy-baseline.sh \
    rpdptw pom.xml

collect_test_reports "$repository/build/architecture-rules" \
    "$temporary/architecture-reports.txt"
collect_test_reports "$repository/build/test-fixtures" \
    "$temporary/fixture-reports.txt"
collect_test_reports "$repository/legacy/gcp-placeholder" \
    "$temporary/legacy-reports.txt"
next_traversal_prefix
all_reports_prefix=$GATE_GENERATOR_PREFIX
gate_stage_find "$repository" "$all_reports_prefix.find.raw" \
    "$all_reports_prefix.find.err" \
    build legacy -type f \
    -path '*/target/surefire-reports/TEST-*.xml' -print
gate_sort_file "$all_reports_prefix.find.raw" \
    "$all_reports_prefix.paths" "$all_reports_prefix.sort.err"
: >"$temporary/all-reports.txt"
while IFS= read -r relative_report; do
    gate_normalize_relative_path "$relative_report"
    printf '%s/%s\n' "$repository" "$GATE_RELATIVE_PATH" \
        >>"$temporary/all-reports.txt"
done <"$all_reports_prefix.paths"
test -s "$temporary/all-reports.txt"

architecture_reports=$(gate_replay_text_file "$temporary/architecture-reports.txt")
fixture_reports=$(gate_replay_text_file "$temporary/fixture-reports.txt")
legacy_reports=$(gate_replay_text_file "$temporary/legacy-reports.txt")
all_reports=$(gate_replay_text_file "$temporary/all-reports.txt")

test "$architecture_reports"
test "$fixture_reports"
test "$legacy_reports"
architecture_tests=$(sum_attribute tests $architecture_reports)
fixture_tests=$(sum_attribute tests $fixture_reports)
legacy_tests=$(sum_attribute tests $legacy_reports)
total_tests=$(sum_attribute tests $all_reports)
total_failures=$(sum_attribute failures $all_reports)
total_errors=$(sum_attribute errors $all_reports)
total_skipped=$(sum_attribute skipped $all_reports)

test "$architecture_tests" -eq 32
test "$fixture_tests" -eq 1
test "$legacy_tests" -eq 15
test "$total_tests" -eq 48
test "$total_failures" -eq 0
test "$total_errors" -eq 0
test "$total_skipped" -eq 0

copy_test_reports "$repository/build/architecture-rules" "$architecture_evidence/surefire-reports"
copy_test_reports "$repository/build/test-fixtures" "$architecture_evidence/surefire-reports"
copy_test_reports "$repository/legacy/gcp-placeholder" "$legacy_evidence/surefire-reports"

{
    printf 'TOTAL_TESTS=%s\n' "$total_tests"
    printf 'PASSED=%s\n' "$total_tests"
    printf 'FAILURES=%s\n' "$total_failures"
    printf 'ERRORS=%s\n' "$total_errors"
    printf 'SKIPPED=%s\n' "$total_skipped"
    printf 'ARCHITECTURE_RULE_TESTS=%s\n' "$architecture_tests"
    printf 'TEST_FIXTURE_BOUNDARY_TESTS=%s\n' "$fixture_tests"
    printf 'LEGACY_GOLDEN_TESTS=%s\n' "$legacy_tests"
    printf '%s\n' 'ORIGINAL_PHASE_00_BASELINE_TESTS_RETAINED=34'
    printf '%s\n' 'REVIEW_02_EVIDENCE_ORACLE_FAULT_TESTS=3'
    printf '%s\n' 'REVIEW_03_GATE_AND_TRAVERSAL_JUNIT_BRIDGE_TESTS=1'
    printf '%s\n' 'REVIEW_03_TARGETED_GREP_FAULT_CASES=10'
    printf '%s\n' 'REVIEW_03_INTEGRITY_FAILURE_CASES=13'
    printf '%s\n' 'REVIEW_04_GATE_CRITICAL_TOOL_FAULT_CASES=22'
    printf '%s\n' 'REVIEW_04_NOT_EVALUATED_TOOL_FAULT_CASES=20'
    printf '%s\n' 'CURRENT_GATE_DETECTOR_FAULT_CASES=51'
    printf '%s\n' 'CURRENT_NOT_EVALUATED_TOOL_FAULT_CASES=49'
    printf '%s\n' 'REVIEW_05_ACTUAL_CALL_PATH_FAULT_CASES=12'
    printf '%s\n' 'REVIEW_05_STALE_PASS_INVALIDATION_CASES=9'
    printf '%s\n' 'REVIEW_06_WRAPPER_LAUNCHER_FAULT_CASES=26'
    printf '%s\n' 'REVIEW_06_WRAPPER_LAUNCHER_ACTUAL_FAULT_CASES=12'
    printf '%s\n' 'REVIEW_06_WRAPPER_LAUNCHER_ACTUAL_CLASSES=11'
    printf '%s\n' 'REVIEW_06_WRAPPER_LAUNCHER_CONDITIONAL_FAULT_CASES=14'
    printf '%s\n' 'REVIEW_06_WRAPPER_LAUNCHER_EXACT_EXIT_41_CASES=22'
    printf '%s\n' 'REVIEW_06_GIT_WHITESPACE_SEMANTIC_CASES=6'
    printf '%s\n' 'REVIEW_06_RECURSIVE_INVENTORY_ROWS=50'
    printf '%s\n' 'REVIEW_06_JUNIT_BRIDGE_TESTS=3'
    printf '%s\n' 'REVIEW_07_WRAPPER_LAUNCHER_FAULT_CASES=27'
    printf '%s\n' 'REVIEW_07_WRAPPER_LAUNCHER_ACTUAL_FAULT_CASES=13'
    printf '%s\n' 'REVIEW_07_WRAPPER_LAUNCHER_ACTUAL_CLASSES=12'
    printf '%s\n' 'REVIEW_07_WRAPPER_LAUNCHER_CONDITIONAL_FAULT_CASES=14'
    printf '%s\n' 'REVIEW_07_WRAPPER_LAUNCHER_EXACT_EXIT_41_CASES=22'
    printf '%s\n' 'REVIEW_07_MAVEN_LAUNCHER_EXACT_AB_PAIRS=18'
    printf '%s\n' 'REVIEW_07_JAVA_HOME_FAIL_CLOSED_ENRICHMENT_PAIRS=1'
    printf '%s\n' 'REVIEW_07_MAVENRC_SOURCE_FAIL_CLOSED_CASES=1'
    printf '%s\n' 'REVIEW_07_SYSTEM_MAVENRC_CONTROLLED_SEAMS=2'
    printf '%s\n' 'REVIEW_07_RECURSIVE_INVENTORY_ROWS=52'
    printf '%s\n' 'REVIEW_07_LAUNCHER_CONTRACT_ROWS=18'
    printf '%s\n' 'REVIEW_07_JUNIT_BRIDGE_TESTS=1'
    printf '%s\n' 'REVIEW_08_WRAPPER_LAUNCHER_FAULT_CASES=28'
    printf '%s\n' 'REVIEW_08_WRAPPER_LAUNCHER_ACTUAL_FAULT_CASES=14'
    printf '%s\n' 'REVIEW_08_WRAPPER_LAUNCHER_ACTUAL_CLASSES=13'
    printf '%s\n' 'REVIEW_08_WRAPPER_LAUNCHER_CONDITIONAL_FAULT_CASES=14'
    printf '%s\n' 'REVIEW_08_WRAPPER_LAUNCHER_EXACT_EXIT_41_CASES=22'
    printf '%s\n' 'REVIEW_08_PUBLIC_TEST_OVERRIDE_ATTACKS=14'
    printf '%s\n' 'REVIEW_08_PRIVATE_DRIVER_FAULT_CASES=20'
    printf '%s\n' 'REVIEW_08_MAVEN_LAUNCHER_EXACT_AB_PAIRS=18'
    printf '%s\n' 'REVIEW_08_JAVA_HOME_FAIL_CLOSED_ENRICHMENT_PAIRS=1'
    printf '%s\n' 'REVIEW_08_MAVENRC_SOURCE_FAIL_CLOSED_CASES=1'
    printf '%s\n' 'REVIEW_08_PRIVATE_SYSTEM_MAVENRC_CASES=2'
    printf '%s\n' 'REVIEW_08_RECURSIVE_INVENTORY_ROWS=54'
    printf '%s\n' 'REVIEW_08_LAUNCHER_CONTRACT_ROWS=19'
    printf '%s\n' 'REVIEW_08_JUNIT_BRIDGE_TESTS=1'
    printf '%s\n' 'REVIEW_12_STRICT_RECEIPT_DIGEST_SCHEMA_NEGATIVE_CASES=4'
    printf '%s\n' 'REVIEW_12_REPORT_RECEIPT_BINDING_NEGATIVE_CASES=4'
    printf '%s\n' 'REVIEW_12_REPORT_HASH_TOOL_FAILURE_CASES=2'
    printf '%s\n' 'ZERO_TEST_FALSE_GREEN_ALLOWED=NO'
} >"$bundle/test-summary.txt"

gate_require_match \
    'legacy Surefire testcase inventory' \
    "$temporary/legacy-testcases.raw" \
    "$temporary/legacy-testcases.detector.err" \
    grep '<testcase ' $legacy_reports
gate_capture_checked 'sed(legacy-testcase-inventory)' \
    "$temporary/legacy-testcases.normalized" \
    "$temporary/legacy-testcases.normalized.err" \
    sed 's/.* name="\([^"]*\)".* classname="\([^"]*\)".*/\2#\1/' \
    "$temporary/legacy-testcases.raw"
gate_sort_file "$temporary/legacy-testcases.normalized" \
    "$legacy_evidence/post-move/golden-test-list.txt" \
    "$temporary/legacy-testcases.sort.err"

gate_require_match \
    'architecture Surefire testcase inventory' \
    "$temporary/architecture-testcases.raw" \
    "$temporary/architecture-testcases.detector.err" \
    grep '<testcase ' $architecture_reports
gate_capture_checked 'sed(architecture-testcase-inventory)' \
    "$temporary/architecture-testcases.normalized" \
    "$temporary/architecture-testcases.normalized.err" \
    sed 's/.* name="\([^"]*\)".* classname="\([^"]*\)".*/\2#\1/' \
    "$temporary/architecture-testcases.raw"
gate_sort_file "$temporary/architecture-testcases.normalized" \
    "$architecture_evidence/architecture-test-list.txt" \
    "$temporary/architecture-testcases.sort.err"

run_command 'E-P00-LEGACY' './build/compare-legacy-baseline.sh --self-test <fault-report>' \
    "$legacy_evidence/logs/legacy-comparator-self-test-command.log" \
    ./build/compare-legacy-baseline.sh --self-test \
    "$legacy_evidence/legacy-comparator-self-test.txt"
run_command 'E-P00-LEGACY' './build/compare-legacy-baseline.sh <E-P00-LEGACY>' \
    "$legacy_evidence/logs/legacy-pre-post-comparison-command.log" \
    ./build/compare-legacy-baseline.sh "$legacy_evidence"

{
    printf '%s\n' 'HANDOFF=PHASE_01_DOMAIN_INPUT_OWNER'
    printf '%s\n' 'ENTRY_STATUS=BLOCKED_PENDING_INDEPENDENT_PHASE_00_REVIEW'
    printf '%s\n' 'SOLE_SEMANTIC_PRODUCTION_START=rpdptw/core'
    printf '%s\n' 'PACKAGES=com.ronext.rpdptw.input,domain,normalization'
    printf '%s\n' 'FIXTURE_COORDINATE=com.ronext:rpdptw-test-fixtures:0.1.0-SNAPSHOT:tests'
    printf '%s\n' 'FIXTURE_TYPE=test-jar'
    printf '%s\n' 'FIXTURE_CLASSIFIER=tests'
    printf '%s\n' 'FIXTURE_SCOPE=test'
    printf '%s\n' 'PHASE_01_DOMAIN_TYPE_COUNT=0'
    printf '%s\n' 'LEGACY_DEPENDENCY_ALLOWED=NO'
    printf '%s\n' 'PROVIDER_DEPENDENCY_ALLOWED=NO'
    printf '%s\n' 'PUBLIC_TARGET_API_APPROVED=NO'
    printf '%s\n' 'REQUIRED_ENTRY_EVIDENCE=E-P00-BUILD,E-P00-ARCH,E-P00-LEGACY'
} >"$bundle/phase-01-handoff.txt"

{
    printf 'ROLLBACK_HEAD=%s\n' "$head_commit"
    printf 'ROLLBACK_CONTENT_ADDRESSED_SOURCE_MANIFEST_SHA256=%s\n' "$implementation_source_digest"
    printf '%s\n' 'ROLLBACK_METHOD=explicit reviewed patch only'
    printf '%s\n' 'FORBIDDEN_ROLLBACK_METHODS=reset-hard,clean,broad-restore,broad-stash,broad-delete'
    printf '%s\n' 'USER_AND_SCHEDULER_CHANGES=preserve'
} >"$bundle/rollback-point.txt"

gate_capture_checked 'git(evidence-generation-status)' \
    "$bundle/generation-git-status.txt" \
    "$temporary/generation-git-status.err" \
    git -C "$repository" status --short

run_command 'E-P00-BUILD' 'git diff --check' \
    "$build_evidence/logs/git-diff-check.log" \
    git diff --check
run_command 'E-P00-BUILD' './build/verify-phase-00-source-scope.sh' \
    "$build_evidence/source-whitespace-and-scope-report.txt" \
    ./build/verify-phase-00-source-scope.sh

run_command 'E-P00-ARCH' './build/verify-evidence-redaction.sh --self-test <canary-report>' \
    "$architecture_evidence/logs/evidence-redaction-canary-command.log" \
    ./build/verify-evidence-redaction.sh --self-test \
    "$architecture_evidence/evidence-redaction-canary-report.txt"
run_command 'E-P00-ARCH' './build/verify-evidence-redaction.sh <unsealed-bundle>' \
    "$architecture_evidence/evidence-redaction-scan.txt" \
    ./build/verify-evidence-redaction.sh "$bundle"

{
    printf '%s\n' 'COMMAND_ENV=MAVEN_SKIP_RC=1'
    printf '%s\n' 'SEAL_COMMAND=./build/seal-evidence-bundle.sh target/phase-00-evidence'
    printf '%s\n' 'COMMAND=./build/verify-evidence-bundle.sh target/phase-00-evidence'
    printf '%s\n' 'EXPECTED_EXIT_CODE=0'
    printf '%s\n' 'VERIFIER_WRITE_MODE=READ_ONLY'
    printf '%s\n' 'RESULT=PASS_IF_AND_ONLY_IF_SEPARATE_SEAL_AND_READ_ONLY_VERIFY_COMPLETE'
} >"$bundle/bundle-verifier-result.txt"

write_pre_review_manifest() {
    build_digest=$(directory_digest "$build_evidence" "$temporary/build-artifacts.tsv")
    architecture_digest=$(directory_digest "$architecture_evidence" "$temporary/architecture-artifacts.tsv")
    legacy_digest=$(directory_digest "$legacy_evidence" "$temporary/legacy-artifacts.tsv")
    gate_sha256_file \
        "$repository/docs/implementation/phases/phase-00-build-architecture-skeleton.md" \
        "$temporary/phase-plan-digest"
    phase_plan_digest=$GATE_SHA256
    gate_sha256_file \
        "$repository/docs/implementation/reviews/phase-00-review.md" \
        "$temporary/review-criteria-digest"
    review_criteria_digest=$GATE_SHA256
    gate_sha256_file "$build_evidence/authority-manifest.tsv" \
        "$temporary/authority-manifest-digest"
    authority_digest=$GATE_SHA256
    gate_sha256_file "$legacy_evidence/pre-move/source-manifest.tsv" \
        "$temporary/pre-move-source-digest"
    pre_move_source_digest=$GATE_SHA256
    gate_sha256_file "$build_evidence/build-fingerprint.txt" \
        "$temporary/build-fingerprint-digest"
    build_fingerprint_digest=$GATE_SHA256
    gate_sha256_file "$commands" "$temporary/commands-digest"
    command_digest=$GATE_SHA256
    gate_sha256_file "$bundle/test-summary.txt" \
        "$temporary/test-summary-digest"
    test_summary_digest=$GATE_SHA256
    gate_sha256_file "$legacy_evidence/post-move/golden-test-list.txt" \
        "$temporary/legacy-golden-digest"
    legacy_golden_digest=$GATE_SHA256
    gate_sha256_file "$architecture_evidence/evidence-redaction-scan.txt" \
        "$temporary/redaction-digest"
    redaction_digest=$GATE_SHA256
    gate_sha256_file "$bundle/open-gated-deferred.txt" \
        "$temporary/open-gate-digest"
    open_gate_digest=$GATE_SHA256
    gate_sha256_file "$bundle/phase-01-handoff.txt" \
        "$temporary/handoff-digest"
    handoff_digest=$GATE_SHA256
    gate_sha256_file "$bundle/rollback-point.txt" \
        "$temporary/rollback-digest"
    rollback_digest=$GATE_SHA256

    {
        printf '%s\n' 'preReviewEvidenceManifest:'
        printf '%s\n' '  phase: "00"'
        printf '  canonicalPhasePlanDigest: "sha256:%s"\n' "$phase_plan_digest"
        printf '  reviewCriteriaDigest: "sha256:%s"\n' "$review_criteria_digest"
        printf '  sourceCommitDigest: "sha256:%s"\n' "$implementation_source_digest"
        printf '  inputArtifactDigests: "authority=sha256:%s;preMoveSource=sha256:%s;baselineGit=git-sha1:%s"\n' \
            "$authority_digest" "$pre_move_source_digest" "$head_commit"
        printf '  configProfileBuildRuntimeDigests: "build=sha256:%s;openGatedDeferred=sha256:%s"\n' \
            "$build_fingerprint_digest" "$open_gate_digest"
        printf '  commandEnvironmentToolchainExitCodeRecordDigest: "sha256:%s"\n' "$command_digest"
        printf '  testResultAndFixtureDigests: "summary=sha256:%s;legacyGolden=sha256:%s"\n' \
            "$test_summary_digest" "$legacy_golden_digest"
        printf '  requiredEvidenceKeyArtifactDigests: "E-P00-BUILD=sha256:%s;E-P00-ARCH=sha256:%s;E-P00-LEGACY=sha256:%s"\n' \
            "$build_digest" "$architecture_digest" "$legacy_digest"
        printf '  architectureDependencySecurityReportDigests: "architecture=sha256:%s;redaction=sha256:%s"\n' \
            "$architecture_digest" "$redaction_digest"
        printf '  openGatedDeferredSnapshotDigest: "sha256:%s"\n' "$open_gate_digest"
        printf '  handoffCandidateArtifactDigest: "sha256:%s"\n' "$handoff_digest"
        printf '  rollbackPointDigest: "sha256:%s"\n' "$rollback_digest"
    } >"$bundle/pre-review-evidence-manifest.yaml"
}

write_pre_review_manifest
run_command 'E-P00-BUILD' './build/verify-pre-review-evidence-manifest.sh <canonical-manifest>' \
    "$architecture_evidence/pre-review-manifest-contract.txt" \
    ./build/verify-pre-review-evidence-manifest.sh \
    "$bundle/pre-review-evidence-manifest.yaml"

# The command record above is immutable evidence too, so recalculate the
# canonical manifest once and validate the final bytes without changing the
# command record again.
write_pre_review_manifest
gate_capture_checked 'verify-pre-review-manifest(final-bytes)' \
    "$temporary/final-pre-review-manifest-check.txt" \
    "$temporary/final-pre-review-manifest-check.err" \
    "$repository/build/verify-pre-review-evidence-manifest.sh" \
        "$bundle/pre-review-evidence-manifest.yaml"
gate_sha256_file "$bundle/pre-review-evidence-manifest.yaml" \
    "$temporary/final-pre-review-manifest"
printf '%s\n' "$GATE_SHA256" \
    >"$bundle/pre-review-evidence-manifest.sha256"

gate_capture_checked 'verify-evidence-redaction(final-bundle)' \
    "$temporary/final-redaction-check.txt" \
    "$temporary/final-redaction-check.err" \
    "$repository/build/verify-evidence-redaction.sh" "$bundle"
gate_capture_checked 'seal-evidence-bundle(generated-bundle)' \
    "$temporary/bundle-seal.log" \
    "$temporary/bundle-seal.err" \
    "$repository/build/seal-evidence-bundle.sh" "$bundle"
gate_capture_checked 'verify-evidence-bundle(generated-bundle)' \
    "$temporary/bundle-verify.log" \
    "$temporary/bundle-verify.err" \
    "$repository/build/verify-evidence-bundle.sh" "$bundle"

staging="$repository/target/.phase-00-evidence.review08.$$"
gate_capture_checked 'cp(stage-phase-00-evidence-bundle)' \
    "$temporary/public-bundle-stage-copy.out" \
    "$temporary/public-bundle-stage-copy.err" \
    cp -R "$bundle" "$staging"
gate_capture_checked 'verify-evidence-bundle(publication-stage)' \
    "$temporary/staging-verify.log" \
    "$temporary/staging-verify.err" \
    "$repository/build/verify-evidence-bundle.sh" "$staging"
gate_capture_checked 'mv(publish-phase-00-evidence-bundle)' \
    "$temporary/public-bundle-publish.out" \
    "$temporary/public-bundle-publish.err" \
    mv "$staging" "$destination"

gate_capture_checked 'verify-evidence-bundle(published-bundle)' \
    "$temporary/published-bundle-verify.out" \
    "$temporary/published-bundle-verify.err" \
    "$repository/build/verify-evidence-bundle.sh" "$destination"
gate_replay_text_file "$temporary/published-bundle-verify.out"
gate_replay_text_file \
    "$repository/target/phase-00-evidence/test-summary.txt"
gate_replay_text_file \
    "$repository/target/phase-00-evidence/E-P00-BUILD/reproducible-build.txt"
IFS= read -r final_bundle_digest \
    <"$destination/evidence-manifest.sha256" || :
IFS= read -r final_pre_review_digest \
    <"$destination/pre-review-evidence-manifest.sha256" || :
printf 'PHASE00_EVIDENCE_BUNDLE_SHA256=%s\n' \
    "$final_bundle_digest"
printf 'PRE_REVIEW_EVIDENCE_MANIFEST_SHA256=%s\n' \
    "$final_pre_review_digest"
printf 'E_P00_BUILD_SHA256=%s\n' \
    "$(directory_digest "$destination/E-P00-BUILD" \
        "$temporary/final-build-artifacts.tsv")"
printf 'E_P00_ARCH_SHA256=%s\n' \
    "$(directory_digest "$destination/E-P00-ARCH" \
        "$temporary/final-architecture-artifacts.tsv")"
printf 'E_P00_LEGACY_SHA256=%s\n' \
    "$(directory_digest "$destination/E-P00-LEGACY" \
        "$temporary/final-legacy-artifacts.tsv")"
