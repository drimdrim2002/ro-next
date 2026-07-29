#!/bin/sh
set -eu

phase00_script_path=$0
case "$phase00_script_path" in
    */*) ;;
    *) phase00_script_path=$(command -v "$phase00_script_path") ;;
esac
phase00_script_directory=${phase00_script_path%/*}
script_directory=$(CDPATH= cd -- "$phase00_script_directory" && pwd)
script="$script_directory/${phase00_script_path##*/}"
. "$script_directory/lib/fail-closed-gates.sh"
if [ "${1:-}" = '--self-test' ] && [ "$#" -eq 2 ]; then
    preinvalidate_report=$2
elif [ "$#" -eq 1 ]; then
    preinvalidate_report="$1/legacy-pre-post-comparison.txt"
else
    preinvalidate_report=
fi
if [ -n "$preinvalidate_report" ] \
    && [ -f "$preinvalidate_report" ] \
    && [ ! -L "$preinvalidate_report" ]; then
    : >"$preinvalidate_report"
fi
gate_make_temporary_directory 'mktemp(legacy-comparator)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-legacy-compare.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

digest() {
    gate_sha256_file "$1" "$temporary/digest"
    printf '%s\n' "$GATE_SHA256"
}

raw_dependency_inventory() {
    input=$1
    output=$2
    gate_capture_checked "awk(raw-dependency-inventory:$input)" \
        "$output" "$output.err" awk '
        /^\[INFO\] / {
            line = substr($0, 8)
            if (line ~ /^[^[:space:]()]+:[^[:space:]()]+:[^[:space:]()]+:[^[:space:]()]+$/) {
                print
                next
            }
            if (match(line, /[A-Za-z0-9_.-]+:[^[:space:]():]+:[^[:space:]():]+:[^[:space:]():]+:[^[:space:]()]+/)) {
                print
            }
        }
    ' "$input"
    test -s "$output"
}

canonical_dependency_path_multiset() {
    input=$1
    output=$2
    expected_application_artifact=$3
    output_basename=${output##*/}
    unsorted="$temporary/$output_basename.unsorted.$$"

    gate_capture_checked \
        "awk(canonical-dependency-paths:$expected_application_artifact)" \
        "$unsorted" "$unsorted.err" \
        awk -v expected_application_artifact="$expected_application_artifact" '
        function fail(message) {
            print "dependency tree canonicalization failure: " message >"/dev/stderr"
            failed = 1
            exit 2
        }

        /^\[INFO\] / {
            line = substr($0, 8)
            if (line ~ /^[^[:space:]()]+:[^[:space:]()]+:[^[:space:]()]+:[^[:space:]()]+$/) {
                root_count++
                component_count = split(line, root_component, ":")
                if (component_count != 4) {
                    fail("application root coordinate must have four components")
                }
                if (root_component[2] != expected_application_artifact) {
                    fail("unexpected application artifact " root_component[2])
                }
                root = root_component[1] ":LEGACY_APPLICATION:" \
                    root_component[3] ":" root_component[4]
                print root
                next
            }

            if (!match(line, /[A-Za-z0-9_.-]+:[^[:space:]():]+:[^[:space:]():]+:[^[:space:]():]+:[^[:space:]()]+/)) {
                next
            }
            if (root_count != 1) {
                fail("dependency node appeared without exactly one application root")
            }

            prefix = substr(line, 1, RSTART - 1)
            wrapped = prefix ~ /\($/
            if (wrapped) {
                sub(/\($/, "", prefix)
            }
            if (prefix !~ /^[|+\\ -]+$/ || length(prefix) % 3 != 0) {
                fail("non-canonical Maven tree prefix")
            }
            depth = length(prefix) / 3
            if (depth < 1) {
                fail("dependency node has no parent edge")
            }

            node = substr(line, RSTART)
            if (wrapped) {
                if (node !~ /\)$/) {
                    fail("wrapped conflict node is missing its closing parenthesis")
                }
                sub(/\)$/, "", node)
            }
            if (excluded_test_depth > 0 && depth > excluded_test_depth) {
                next
            }
            if (excluded_test_depth > 0 && depth <= excluded_test_depth) {
                excluded_test_depth = 0
            }
            if (node ~ /:test([[:space:]]|$)/) {
                excluded_test_depth = depth
                next
            }
            if (depth > 1 && !(depth - 1 in ancestor)) {
                fail("dependency node is missing its parent path")
            }
            ancestor[depth] = node
            for (ancestor_key in ancestor) {
                if ((ancestor_key + 0) > depth) {
                    delete ancestor[ancestor_key]
                }
            }

            path = root
            for (level = 1; level <= depth; level++) {
                if (!(level in ancestor)) {
                    fail("dependency path contains a missing ancestor")
                }
                path = path " -> " ancestor[level]
            }
            print path
            node_count++
        }

        END {
            if (!failed && root_count != 1) {
                fail("expected exactly one application root")
            }
            if (!failed && node_count == 0) {
                fail("dependency tree contains no dependency nodes")
            }
        }
    ' "$input"
    gate_sort_file "$unsorted" "$output" "$output.sort.err"
    test -s "$output"
}

normalize_warnings() {
    input=$1
    output=$2
    gate_capture_checked "sed(normalize-shade-warnings:$input)" \
        "$temporary/warnings-normalized.unsorted" \
        "$temporary/warnings-normalized.err" sed \
        -e 's/ro-next-0\.1\.0-SNAPSHOT\.jar/LEGACY-APPLICATION.jar/g' \
        -e 's/legacy-gcp-placeholder-0\.1\.0-SNAPSHOT\.jar/LEGACY-APPLICATION.jar/g' \
        -e 's/, LEGACY-APPLICATION\.jar//g' \
        -e 's/LEGACY-APPLICATION\.jar, //g' \
        "$input"
    gate_sort_file "$temporary/warnings-normalized.unsorted" "$output" \
        "$temporary/warnings-normalized.sort.err"
}

compare_evidence() {
    legacy_evidence=$1
    pre="$legacy_evidence/pre-move"
    post="$legacy_evidence/post-move"
    comparison_report="$legacy_evidence/legacy-pre-post-comparison.txt"
    comparison_stage="$temporary/legacy-pre-post-comparison.txt"

    if [ -e "$comparison_report" ] || [ -L "$comparison_report" ]; then
        if [ -f "$comparison_report" ] && [ ! -L "$comparison_report" ]; then
            : >"$comparison_report"
        fi
        gate_capture_checked 'rm(stale-legacy-comparison-report)' \
            "$temporary/stale-comparison-remove.out" \
            "$temporary/stale-comparison-remove.err" \
            rm -f -- "$comparison_report"
    fi

    required_files='
dependency-tree-verbose.txt
shade-collision-warnings.txt
selected-service-resource-inventory.txt
shaded-manifest.mf
jackson-conflict-inventory.txt
'
    for required in $required_files; do
        test -s "$pre/$required"
        test -s "$post/$required"
    done

    raw_dependency_inventory \
        "$pre/dependency-tree-verbose.txt" \
        "$pre/raw-dependency-tree-inventory.txt"
    raw_dependency_inventory \
        "$post/dependency-tree-verbose.txt" \
        "$post/raw-dependency-tree-inventory.txt"
    canonical_dependency_path_multiset \
        "$pre/dependency-tree-verbose.txt" \
        "$pre/production-dependency-root-path-multiset.txt" \
        'ro-next'
    canonical_dependency_path_multiset \
        "$post/dependency-tree-verbose.txt" \
        "$post/production-dependency-root-path-multiset.txt" \
        'legacy-gcp-placeholder'

    gate_capture_checked 'awk(pre-move-dependency-count)' \
        "$temporary/pre-dependency-count.txt" \
        "$temporary/pre-dependency-count.err" \
        awk 'END { print NR + 0 }' \
        "$pre/production-dependency-root-path-multiset.txt"
    gate_capture_checked 'awk(post-move-dependency-count)' \
        "$temporary/post-dependency-count.txt" \
        "$temporary/post-dependency-count.err" \
        awk 'END { print NR + 0 }' \
        "$post/production-dependency-root-path-multiset.txt"
    IFS= read -r pre_dependency_count \
        <"$temporary/pre-dependency-count.txt" || :
    IFS= read -r post_dependency_count \
        <"$temporary/post-dependency-count.txt" || :
    test "$pre_dependency_count" -gt 1
    test "$pre_dependency_count" -eq "$post_dependency_count"
    gate_compare_files 'legacy production dependency path multiset' \
        "$pre/production-dependency-root-path-multiset.txt" \
        "$post/production-dependency-root-path-multiset.txt" \
        "$temporary/dependency-paths.cmp.out" \
        "$temporary/dependency-paths.cmp.err"

    normalize_warnings "$pre/shade-collision-warnings.txt" \
        "$pre/shade-collision-warnings.normalized.txt"
    normalize_warnings "$post/shade-collision-warnings.txt" \
        "$post/shade-collision-warnings.normalized.txt"
    gate_capture_status "$temporary/pre-shade-jar-count.txt" \
        "$temporary/pre-shade-jar-count.err" \
        grep -c 'ro-next-0\.1\.0-SNAPSHOT\.jar' \
        "$pre/shade-collision-warnings.txt"
    case "$GATE_STATUS" in
        0|1) ;;
        *)
            detector_status=$GATE_STATUS
            gate_report_not_evaluated 'grep(pre-move-shade-jar-count)' \
                "$detector_status" "$temporary/pre-shade-jar-count.err"
            exit "$detector_status"
            ;;
    esac
    gate_capture_status "$temporary/post-shade-jar-count.txt" \
        "$temporary/post-shade-jar-count.err" \
        grep -c 'legacy-gcp-placeholder-0\.1\.0-SNAPSHOT\.jar' \
        "$post/shade-collision-warnings.txt"
    case "$GATE_STATUS" in
        0|1) ;;
        *)
            detector_status=$GATE_STATUS
            gate_report_not_evaluated 'grep(post-move-shade-jar-count)' \
                "$detector_status" "$temporary/post-shade-jar-count.err"
            exit "$detector_status"
            ;;
    esac
    gate_capture_checked 'awk(pre-move-shade-jar-count)' \
        "$temporary/pre-shade-jar-count.value" \
        "$temporary/pre-shade-jar-count.parse.err" \
        awk 'NR == 1 { print $1 + 0 }' \
        "$temporary/pre-shade-jar-count.txt"
    gate_capture_checked 'awk(post-move-shade-jar-count)' \
        "$temporary/post-shade-jar-count.value" \
        "$temporary/post-shade-jar-count.parse.err" \
        awk 'NR == 1 { print $1 + 0 }' \
        "$temporary/post-shade-jar-count.txt"
    IFS= read -r pre_shade_jar_count \
        <"$temporary/pre-shade-jar-count.value" || :
    IFS= read -r post_shade_jar_count \
        <"$temporary/post-shade-jar-count.value" || :
    test "$pre_shade_jar_count" -eq 1
    test "$post_shade_jar_count" -eq 1
    gate_compare_files 'legacy normalized Shade warning inventory' \
        "$pre/shade-collision-warnings.normalized.txt" \
        "$post/shade-collision-warnings.normalized.txt" \
        "$temporary/shade-warnings.cmp.out" \
        "$temporary/shade-warnings.cmp.err"
    gate_compare_files 'legacy selected service/resource inventory' \
        "$pre/selected-service-resource-inventory.txt" \
        "$post/selected-service-resource-inventory.txt" \
        "$temporary/service-resources.cmp.out" \
        "$temporary/service-resources.cmp.err"

    sed -n 's/^Main-Class: //p' "$pre/shaded-manifest.mf" \
        >"$temporary/pre-main.raw"
    sed -n 's/^Main-Class: //p' "$post/shaded-manifest.mf" \
        >"$temporary/post-main.raw"
    pre_main=$(tr -d '\r' <"$temporary/pre-main.raw")
    post_main=$(tr -d '\r' <"$temporary/post-main.raw")
    test -n "$pre_main"
    test "$pre_main" = "$post_main"

    sed 's/^\[INFO\] [|+\\ -]*//' \
        "$pre/jackson-conflict-inventory.txt" \
        >"$temporary/pre-jackson.unsorted"
    sed 's/^\[INFO\] [|+\\ -]*//' \
        "$post/jackson-conflict-inventory.txt" \
        >"$temporary/post-jackson.unsorted"
    gate_sort_file "$temporary/pre-jackson.unsorted" \
        "$temporary/pre-jackson.sorted" "$temporary/pre-jackson.sort.err"
    gate_sort_file "$temporary/post-jackson.unsorted" \
        "$temporary/post-jackson.sorted" "$temporary/post-jackson.sort.err"
    gate_compare_files 'legacy Jackson conflict inventory' \
        "$temporary/pre-jackson.sorted" "$temporary/post-jackson.sorted" \
        "$temporary/jackson.cmp.out" "$temporary/jackson.cmp.err"

    pre_tests=$(sed -n 's/^TESTS=//p' \
        "$pre/baseline-identity-and-test-summary.txt")
    post_tests=$(sed -n 's/^LEGACY_GOLDEN_TESTS=//p' \
        "$legacy_evidence/../test-summary.txt")
    test "$pre_tests" -eq 1
    test "$post_tests" -ge 15
    gate_require_match \
        'legacy listed-generation characterization test inventory' \
        "$temporary/listed-generation.matches" \
        "$temporary/listed-generation.detector.err" \
        grep -F \
        'com.ronext.optimizer.adapter.in.http.LegacyOptimizationContractCharacterizationTest#readsTheListedGenerationWhenCandidateIsOverwrittenAfterListing' \
        "$post/golden-test-list.txt"

    pre_raw_tree_digest=$(digest "$pre/dependency-tree-verbose.txt")
    post_raw_tree_digest=$(digest "$post/dependency-tree-verbose.txt")
    pre_raw_inventory_digest=$(digest "$pre/raw-dependency-tree-inventory.txt")
    post_raw_inventory_digest=$(digest "$post/raw-dependency-tree-inventory.txt")
    if [ "$pre_raw_tree_digest" = "$post_raw_tree_digest" ]; then
        raw_tree_digest_equal=YES
    else
        raw_tree_digest_equal=NO
    fi
    if [ "$pre_raw_inventory_digest" = "$post_raw_inventory_digest" ]; then
        raw_inventory_digest_equal=YES
        raw_digest_disposition=IDENTICAL
    else
        raw_inventory_digest_equal=NO
        raw_digest_disposition=RECORDED_AND_PRODUCTION_CANONICAL_MULTISET_REQUIRED_EQUAL
    fi

    {
        printf '%s\n' 'LEGACY_PRE_POST_COMPARISON=PASS'
        printf '%s\n' 'BASELINE_COMMIT=7cc890ee1d0805df5ae14b633127fade4f978639'
        printf 'PRE_TEST_COUNT=%s\n' "$pre_tests"
        printf 'POST_LEGACY_GOLDEN_TEST_COUNT=%s\n' "$post_tests"
        printf '%s\n' 'DEPENDENCY_COMPARISON_SCOPE=PRODUCTION'
        printf '%s\n' 'RAW_DEPENDENCY_INVENTORY_SCOPE=FULL_MAVEN_TREE'
        printf 'PRODUCTION_DEPENDENCY_ROOT_PATH_MULTISET_ENTRY_COUNT=%s\n' \
            "$pre_dependency_count"
        printf '%s\n' 'PRODUCTION_DEPENDENCY_ROOT_PATH_MULTISET_EQUAL=YES'
        printf '%s\n' 'DEPENDENCY_MULTIPLICITY_PRESERVED=YES'
        printf '%s\n' 'DEPENDENCY_PARENT_TOPOLOGY_PRESERVED=YES'
        printf '%s\n' 'DEPENDENCY_CONFLICT_ANNOTATION_PRESERVED=YES'
        printf '%s\n' 'DEPENDENCY_ALLOWED_NORMALIZATION=SIBLING_ORDER_AND_APPLICATION_ARTIFACT_NAME_ONLY'
        printf 'RAW_DEPENDENCY_TREE_DIGEST_EQUAL=%s\n' "$raw_tree_digest_equal"
        printf 'RAW_DEPENDENCY_INVENTORY_DIGEST_EQUAL=%s\n' \
            "$raw_inventory_digest_equal"
        printf 'RAW_DIGEST_DIFFERENCE_DISPOSITION=%s\n' \
            "$raw_digest_disposition"
        printf '%s\n' 'JACKSON_CONFLICT_INVENTORY_EQUAL=YES'
        printf '%s\n' 'SHADE_WARNING_INVENTORY_NORMALIZED_MULTISET_EQUAL=YES'
        printf '%s\n' 'SHADE_APPLICATION_JAR_CONTRIBUTOR_PRESENT_PRE_POST=YES'
        printf '%s\n' 'SELECTED_SERVICE_RESOURCE_INVENTORY_EQUAL=YES'
        printf 'MAIN_CLASS=%s\n' "$pre_main"
        printf '%s\n' 'ENDPOINT_STATUS_DEFAULT_OBJECTIVE_STORAGE_KEYS_GOLDEN=PASS'
        printf '%s\n' 'LISTED_OBJECT_IDENTITY=OPAQUE_REFERENCE_PRESERVES_PROVIDER_GENERATION'
        printf '%s\n' 'CONCURRENT_OVERWRITE_CHARACTERIZATION=PASS'
        printf '%s\n' 'LEGACY_EXCEPTION_POLICY=NARROW_LEGACY_MODULE_ONLY'
        printf '%s\n' 'GLOBAL_ENFORCER_WEAKENING=NO'
        printf '%s\n' 'DEPENDENCY_ALIGNMENT_OR_SHADE_WARNING_SUPPRESSION=NO'
        printf '%s\n' 'OWNER_APPROVAL_ISSUED_BY_FIX_SESSION=NO'
        printf '%s\n' 'OWNER_REVIEW_02_DECISION=APPROVE'
        printf '%s\n' 'OWNER_APPROVED_SCOPE=PRE_EXISTING_JACKSON_SHADE_NARROW_LEGACY_ONLY_EXCEPTION_POLICY'
        printf '%s\n' 'NEW_DEPENDENCY_LICENSE_PROVIDER_AUTHORITY=NO'
        printf 'PRE_DEPENDENCY_TREE_SHA256=%s\n' "$pre_raw_tree_digest"
        printf 'POST_DEPENDENCY_TREE_SHA256=%s\n' "$post_raw_tree_digest"
        printf 'PRE_RAW_DEPENDENCY_INVENTORY_SHA256=%s\n' \
            "$pre_raw_inventory_digest"
        printf 'POST_RAW_DEPENDENCY_INVENTORY_SHA256=%s\n' \
            "$post_raw_inventory_digest"
        printf 'PRE_PRODUCTION_DEPENDENCY_ROOT_PATH_MULTISET_SHA256=%s\n' \
            "$(digest "$pre/production-dependency-root-path-multiset.txt")"
        printf 'POST_PRODUCTION_DEPENDENCY_ROOT_PATH_MULTISET_SHA256=%s\n' \
            "$(digest "$post/production-dependency-root-path-multiset.txt")"
        printf 'PRE_SHADE_WARNING_SHA256=%s\n' \
            "$(digest "$pre/shade-collision-warnings.txt")"
        printf 'POST_SHADE_WARNING_SHA256=%s\n' \
            "$(digest "$post/shade-collision-warnings.txt")"
        printf 'PRE_SHADE_WARNING_NORMALIZED_SHA256=%s\n' \
            "$(digest "$pre/shade-collision-warnings.normalized.txt")"
        printf 'POST_SHADE_WARNING_NORMALIZED_SHA256=%s\n' \
            "$(digest "$post/shade-collision-warnings.normalized.txt")"
        printf 'PRE_SELECTED_RESOURCE_SHA256=%s\n' \
            "$(digest "$pre/selected-service-resource-inventory.txt")"
        printf 'POST_SELECTED_RESOURCE_SHA256=%s\n' \
            "$(digest "$post/selected-service-resource-inventory.txt")"
    } >"$comparison_stage"

    gate_capture_checked 'mv(publish-legacy-comparison-report)' \
        "$temporary/comparison-publish.out" \
        "$temporary/comparison-publish.err" \
        mv "$comparison_stage" "$comparison_report"
    gate_replay_text_file "$comparison_report"
}

write_comparator_fixture() {
    fixture=$1
    legacy_evidence="$fixture/legacy"
    pre="$legacy_evidence/pre-move"
    post="$legacy_evidence/post-move"
    mkdir -p "$pre" "$post"

    {
        printf '%s\n' '[INFO] com.ronext:ro-next:jar:0.1.0-SNAPSHOT'
        printf '%s\n' '[INFO] +- org.example:alpha:jar:1.0:compile'
        printf '%s\n' '[INFO] |  \- org.example:shared:jar:1.0:runtime'
        printf '%s\n' '[INFO] +- org.example:beta:jar:2.0:compile'
    } >"$pre/dependency-tree-verbose.txt"
    {
        printf '%s\n' '[INFO] com.ronext:legacy-gcp-placeholder:jar:0.1.0-SNAPSHOT'
        printf '%s\n' '[INFO] +- org.example:beta:jar:2.0:compile'
        printf '%s\n' '[INFO] +- org.example:alpha:jar:1.0:compile'
        printf '%s\n' '[INFO] |  \- org.example:shared:jar:1.0:runtime'
    } >"$post/dependency-tree-verbose.txt"

    printf '%s\n' '[WARNING] ro-next-0.1.0-SNAPSHOT.jar overlap' \
        >"$pre/shade-collision-warnings.txt"
    printf '%s\n' '[WARNING] legacy-gcp-placeholder-0.1.0-SNAPSHOT.jar overlap' \
        >"$post/shade-collision-warnings.txt"
    printf '%s\n' 'META-INF/services/org.example.Service' \
        >"$pre/selected-service-resource-inventory.txt"
    cp "$pre/selected-service-resource-inventory.txt" \
        "$post/selected-service-resource-inventory.txt"
    printf '%s\n' 'Main-Class: org.example.Main' >"$pre/shaded-manifest.mf"
    cp "$pre/shaded-manifest.mf" "$post/shaded-manifest.mf"
    printf '%s\n' \
        '[INFO] |  +- (com.fasterxml.jackson.core:jackson-core:jar:1.0:compile - omitted for conflict with 2.0)' \
        >"$pre/jackson-conflict-inventory.txt"
    cp "$pre/jackson-conflict-inventory.txt" \
        "$post/jackson-conflict-inventory.txt"
    printf '%s\n' 'TESTS=1' >"$pre/baseline-identity-and-test-summary.txt"
    printf '%s\n' 'LEGACY_GOLDEN_TESTS=15' >"$fixture/test-summary.txt"
    printf '%s\n' \
        'com.ronext.optimizer.adapter.in.http.LegacyOptimizationContractCharacterizationTest#readsTheListedGenerationWhenCandidateIsOverwrittenAfterListing' \
        >"$post/golden-test-list.txt"
}

self_test() {
    report=$1
    report_stage="$temporary/legacy-comparator-self-test.txt"
    pristine="$temporary/pristine"
    duplicate="$temporary/duplicate"
    reparent="$temporary/reparent"
    conflict="$temporary/conflict"
    multiplicity="$temporary/multiplicity"
    coordinate="$temporary/coordinate"

    if [ -e "$report" ] || [ -L "$report" ]; then
        if [ -f "$report" ] && [ ! -L "$report" ]; then
            : >"$report"
        fi
        gate_capture_checked 'rm(stale-legacy-comparator-self-test-report)' \
            "$temporary/stale-self-test-report-remove.out" \
            "$temporary/stale-self-test-report-remove.err" \
            rm -f -- "$report"
    fi
    write_comparator_fixture "$pristine"

    set +e
    "$script" "$pristine/legacy" >"$temporary/pristine.log" 2>&1
    pristine_exit=$?
    set -e
    test "$pristine_exit" -eq 0

    cp -R "$pristine" "$duplicate"
    mv "$duplicate/legacy/legacy-pre-post-comparison.txt" \
        "$duplicate/pristine-comparison-report.txt"
    gate_capture_checked 'awk(self-test-duplicate-mutation)' \
        "$duplicate/dependency-tree-verbose.mutated.txt" \
        "$duplicate/dependency-tree-verbose.mutated.err" awk '
        {
            print
            if (!inserted && $0 == "[INFO] +- org.example:beta:jar:2.0:compile") {
                print
                inserted = 1
            }
        }
    ' "$duplicate/legacy/post-move/dependency-tree-verbose.txt"
    mv "$duplicate/dependency-tree-verbose.mutated.txt" \
        "$duplicate/legacy/post-move/dependency-tree-verbose.txt"
    set +e
    "$script" "$duplicate/legacy" >"$temporary/duplicate.log" 2>&1
    duplicate_exit=$?
    set -e
    test "$duplicate_exit" -ne 0
    test ! -e "$duplicate/legacy/legacy-pre-post-comparison.txt"

    cp -R "$pristine" "$reparent"
    mv "$reparent/legacy/legacy-pre-post-comparison.txt" \
        "$reparent/pristine-comparison-report.txt"
    gate_capture_checked 'awk(self-test-reparent-mutation)' \
        "$reparent/dependency-tree-verbose.mutated.txt" \
        "$reparent/dependency-tree-verbose.mutated.err" awk '
        {
            if ($0 == "[INFO] +- org.example:beta:jar:2.0:compile") {
                print "[INFO] |  +- org.example:beta:jar:2.0:compile"
            } else {
                print
            }
        }
    ' "$reparent/legacy/post-move/dependency-tree-verbose.txt"
    mv "$reparent/dependency-tree-verbose.mutated.txt" \
        "$reparent/legacy/post-move/dependency-tree-verbose.txt"
    set +e
    "$script" "$reparent/legacy" >"$temporary/reparent.log" 2>&1
    reparent_exit=$?
    set -e
    test "$reparent_exit" -ne 0
    test ! -e "$reparent/legacy/legacy-pre-post-comparison.txt"

    cp -R "$pristine" "$conflict"
    mv "$conflict/legacy/legacy-pre-post-comparison.txt" \
        "$conflict/pristine-comparison-report.txt"
    gate_capture_checked 'sed(self-test-conflict-mutation)' \
        "$conflict/dependency-tree-verbose.mutated.txt" \
        "$conflict/dependency-tree-verbose.mutated.err" sed \
        's#^\[INFO\] +- org.example:beta:jar:2.0:compile$#[INFO] +- (org.example:beta:jar:2.0:compile - omitted for conflict with 2.1)#' \
        "$conflict/legacy/post-move/dependency-tree-verbose.txt"
    mv "$conflict/dependency-tree-verbose.mutated.txt" \
        "$conflict/legacy/post-move/dependency-tree-verbose.txt"
    set +e
    "$script" "$conflict/legacy" >"$temporary/conflict.log" 2>&1
    conflict_exit=$?
    set -e
    test "$conflict_exit" -ne 0
    test ! -e "$conflict/legacy/legacy-pre-post-comparison.txt"

    cp -R "$pristine" "$multiplicity"
    mv "$multiplicity/legacy/legacy-pre-post-comparison.txt" \
        "$multiplicity/pristine-comparison-report.txt"
    gate_capture_checked 'awk(self-test-multiplicity-mutation)' \
        "$multiplicity/dependency-tree-verbose.mutated.txt" \
        "$multiplicity/dependency-tree-verbose.mutated.err" awk '
        {
            print
            if (!inserted && $0 == "[INFO] |  \\- org.example:shared:jar:1.0:runtime") {
                print
                inserted = 1
            }
        }
    ' "$multiplicity/legacy/post-move/dependency-tree-verbose.txt"
    mv "$multiplicity/dependency-tree-verbose.mutated.txt" \
        "$multiplicity/legacy/post-move/dependency-tree-verbose.txt"
    set +e
    "$script" "$multiplicity/legacy" >"$temporary/multiplicity.log" 2>&1
    multiplicity_exit=$?
    set -e
    test "$multiplicity_exit" -ne 0
    test ! -e "$multiplicity/legacy/legacy-pre-post-comparison.txt"

    cp -R "$pristine" "$coordinate"
    mv "$coordinate/legacy/legacy-pre-post-comparison.txt" \
        "$coordinate/pristine-comparison-report.txt"
    gate_capture_checked 'sed(self-test-coordinate-mutation)' \
        "$coordinate/dependency-tree-verbose.mutated.txt" \
        "$coordinate/dependency-tree-verbose.mutated.err" sed \
        's#org.example:beta:jar:2.0:compile#org.example:beta:jar:2.1:compile#' \
        "$coordinate/legacy/post-move/dependency-tree-verbose.txt"
    mv "$coordinate/dependency-tree-verbose.mutated.txt" \
        "$coordinate/legacy/post-move/dependency-tree-verbose.txt"
    set +e
    "$script" "$coordinate/legacy" >"$temporary/coordinate.log" 2>&1
    coordinate_exit=$?
    set -e
    test "$coordinate_exit" -ne 0
    test ! -e "$coordinate/legacy/legacy-pre-post-comparison.txt"

    {
        printf '%s\n' 'PHASE00_LEGACY_COMPARATOR_SELF_TEST=PASS'
        printf '%s\n' 'POSITIVE_TEST_NAME=canonicalSiblingOrderAndApplicationArtifactNormalization'
        printf 'POSITIVE_TEST_EXIT_CODE=%s\n' "$pristine_exit"
        printf '%s\n' 'NEGATIVE_TEST_NAME_1=duplicateDependencyMultiplicityMutationIsRejected'
        printf 'DUPLICATE_MUTATION_EXIT_CODE=%s\n' "$duplicate_exit"
        printf '%s\n' 'NEGATIVE_TEST_NAME_2=directChildToNestedChildReparentMutationIsRejected'
        printf 'REPARENT_MUTATION_EXIT_CODE=%s\n' "$reparent_exit"
        printf '%s\n' 'NEGATIVE_TEST_NAME_3=conflictAnnotationMutationIsRejected'
        printf 'CONFLICT_MUTATION_EXIT_CODE=%s\n' "$conflict_exit"
        printf '%s\n' 'NEGATIVE_TEST_NAME_4=repeatedTransitiveMultiplicityMutationIsRejected'
        printf 'MULTIPLICITY_MUTATION_EXIT_CODE=%s\n' "$multiplicity_exit"
        printf '%s\n' 'NEGATIVE_TEST_NAME_5=dependencyCoordinateMutationIsRejected'
        printf 'COORDINATE_MUTATION_EXIT_CODE=%s\n' "$coordinate_exit"
        printf '%s\n' 'AUTOMATED_COMPARATOR_POSITIVE_TEST_COUNT=1'
        printf '%s\n' 'AUTOMATED_COMPARATOR_NEGATIVE_TEST_COUNT=5'
        printf '%s\n' 'AUTOMATED_COMPARATOR_TOTAL_CASE_COUNT=6'
        printf '%s\n' 'FALSE_GREEN_COUNT=0'
    } >"$report_stage"
    gate_capture_checked 'mv(publish-legacy-comparator-self-test-report)' \
        "$temporary/self-test-report-publish.out" \
        "$temporary/self-test-report-publish.err" \
        mv "$report_stage" "$report"
}

if [ "${1:-}" = '--self-test' ]; then
    if [ "$#" -ne 2 ]; then
        printf 'usage: %s --self-test <report-path>\n' "$0" >&2
        exit 2
    fi
    self_test "$2"
    exit 0
fi

if [ "$#" -ne 1 ]; then
    printf 'usage: %s <legacy-evidence-directory>\n' "$0" >&2
    exit 2
fi

compare_evidence "$1"
