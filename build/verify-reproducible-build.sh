#!/bin/sh
set -eu

phase00_script_path=$0
case "$phase00_script_path" in
    */*) ;;
    *) phase00_script_path=$(command -v "$phase00_script_path") ;;
esac
phase00_script_directory=${phase00_script_path%/*}
default_repository=$(CDPATH= cd -- "$phase00_script_directory/.." && pwd)
. "$default_repository/build/lib/fail-closed-gates.sh"
repository=${PHASE00_REPRO_REPOSITORY:-"$default_repository"}
maven_repository=${PHASE00_MAVEN_REPO:-"$repository/target/phase-00-m2"}
evidence_directory=${PHASE00_EVIDENCE_DIR:-"$repository/target/phase-00-evidence/E-P00-BUILD"}
reproducibility_report="$evidence_directory/reproducible-build.txt"
if [ -f "$reproducibility_report" ] && [ ! -L "$reproducibility_report" ]; then
    : >"$reproducibility_report"
fi
gate_make_temporary_directory 'mktemp(reproducible-build)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-repro.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM
if [ -e "$reproducibility_report" ] || [ -L "$reproducibility_report" ]; then
    if [ -f "$reproducibility_report" ] && [ ! -L "$reproducibility_report" ]; then
        : >"$reproducibility_report"
    fi
    gate_capture_checked 'rm(stale-reproducible-build-report)' \
        "$temporary/stale-report-remove.out" \
        "$temporary/stale-report-remove.err" \
        rm -f -- "$reproducibility_report"
fi

if [ ! -d "$maven_repository" ]; then
    printf 'offline Maven repository is missing: %s\n' "$maven_repository" >&2
    exit 1
fi

mkdir -p "$temporary/source" "$temporary/work-a" "$temporary/work-b" \
    "$temporary/report-stage"

source_roots='pom.xml .mvn mvnw mvnw.cmd .sdkmanrc rpdptw build legacy gcp Dockerfile .dockerignore README.md docs/implementation/master-realization-plan.md'
for source_root in $source_roots; do
    test -e "$repository/$source_root"
done

source_root_inventory() {
    prefix=$1
    gate_stage_find "$repository" "$prefix.find.raw" "$prefix.find.err" \
        $source_roots \
        ! -path '*/target' ! -path '*/target/*' -print
    gate_sort_file "$prefix.find.raw" "$prefix.paths" "$prefix.sort.err"
    mkdir -p "$prefix.scratch"
    gate_classify_paths "$repository" "$prefix.paths" \
        "$prefix.types.tsv" "$prefix.files.txt" "$prefix.scratch"
    gate_require_no_match \
        'reproducibility source snapshot symlink predicate' \
        "$prefix.symlink.matches" "$prefix.symlink.detector.err" \
        grep '^L	' "$prefix.types.tsv"
    gate_require_no_match \
        'reproducibility source snapshot non-regular predicate' \
        "$prefix.other.matches" "$prefix.other.detector.err" \
        grep '^O	' "$prefix.types.tsv"
    gate_hash_file_list "$repository" "$prefix.files.txt" \
        "$prefix.files.tsv" "$prefix.scratch"
}

# Source traversal must complete before tar sees any path. This makes a partial
# fake-find result, permission error, symlink, or special file fail closed.
source_root_inventory "$temporary/repository-source"

(
    cd "$repository"
    tar -cf "$temporary/source.tar" \
        --exclude='*/target' \
        --exclude='*/target/*' \
        $source_roots
)
tar -xf "$temporary/source.tar" -C "$temporary/work-a"
tar -xf "$temporary/source.tar" -C "$temporary/work-b"

source_manifest() {
    workspace=$1
    output=$2
    prefix=$3
    gate_inventory_tree "$workspace" "$prefix"
    gate_capture_checked "awk(reproducible-source-manifest:$workspace)" \
        "$output" "$prefix.source-manifest.err" \
        awk -F '	' 'BEGIN { OFS="\t" }
        $1 != "." { print $1, $3, $4 }' \
        "$GATE_TREE_FILE_MANIFEST"
    test -s "$output"
}

source_manifest "$temporary/work-a" "$temporary/source-a.tsv" \
    "$temporary/work-a-tree"
source_manifest "$temporary/work-b" "$temporary/source-b.tsv" \
    "$temporary/work-b-tree"
gate_compare_files 'reproducibility source snapshot A versus B' \
    "$temporary/source-a.tsv" "$temporary/source-b.tsv" \
    "$temporary/source.cmp.out" "$temporary/source.cmp.err"
gate_sha256_file "$temporary/source-a.tsv" "$temporary/source-manifest"
source_digest=$GATE_SHA256

build_workspace() {
    workspace=$1
    log=$2
    set +e
    (
        cd "$workspace"
        ./mvnw -B -ntp -Dstyle.color=never -o \
            -Dmaven.repo.local="$maven_repository" clean package
    ) >"$log.stdout" 2>"$log.stderr"
    exit_code=$?
    set -e
    if [ "$exit_code" -ne 0 ]; then
        printf 'reproducibility workspace build failed (%s): %s\n' \
            "$exit_code" "$workspace" >&2
        gate_replay_text_file "$log.stderr" stderr
        printf 'tool=mvnw(reproducible-workspace:%s) result=NOT_EVALUATED exit=%s\n' \
            "$workspace" "$exit_code" >&2
        exit "$exit_code"
    fi
    : >"$log"
    gate_replay_text_file "$log.stdout" >>"$log"
    gate_replay_text_file "$log.stderr" >>"$log"
}

build_workspace "$temporary/work-a" "$temporary/build-a.log"
build_workspace "$temporary/work-b" "$temporary/build-b.log"

artifact_manifest() {
    workspace=$1
    output=$2
    prefix=$3
    gate_stage_find "$workspace" "$prefix.find.raw" "$prefix.find.err" \
        rpdptw build legacy -type f -path '*/target/*.jar' \
        ! -name '*-sources.jar' ! -name '*-javadoc.jar' -print
    gate_sort_file "$prefix.find.raw" "$prefix.paths" "$prefix.sort.err"
    mkdir -p "$prefix.scratch"
    gate_classify_paths "$workspace" "$prefix.paths" \
        "$prefix.types.tsv" "$prefix.files.txt" "$prefix.scratch"
    gate_hash_file_list "$workspace" "$prefix.files.txt" \
        "$prefix.files.tsv" "$prefix.scratch"
    gate_capture_checked "awk(reproducible-artifact-manifest:$workspace)" \
        "$output" "$prefix.artifact-manifest.err" \
        awk -F '	' 'BEGIN { OFS="\t" } { print $1, $3, $4 }' \
        "$prefix.files.tsv"
}

artifact_manifest "$temporary/work-a" "$temporary/artifacts-a.tsv" \
    "$temporary/artifacts-a"
artifact_manifest "$temporary/work-b" "$temporary/artifacts-b.tsv" \
    "$temporary/artifacts-b"
test -s "$temporary/artifacts-a.tsv"
gate_compare_files 'reproducibility artifact snapshot A versus B' \
    "$temporary/artifacts-a.tsv" "$temporary/artifacts-b.tsv" \
    "$temporary/artifacts.cmp.out" "$temporary/artifacts.cmp.err"

gate_capture_checked 'awk(reproducible-artifact-count)' \
    "$temporary/artifact-count.txt" "$temporary/artifact-count.err" \
    awk 'END { print NR + 0 }' "$temporary/artifacts-a.tsv"
IFS= read -r artifact_count <"$temporary/artifact-count.txt" || :
gate_sha256_file "$temporary/artifacts-a.tsv" \
    "$temporary/artifact-manifest"
artifact_manifest_digest=$GATE_SHA256

cp "$temporary/source-a.tsv" \
    "$temporary/report-stage/reproducible-source-manifest.tsv"
cp "$temporary/artifacts-a.tsv" \
    "$temporary/report-stage/reproducible-artifact-manifest.tsv"
cp "$temporary/build-a.log" \
    "$temporary/report-stage/reproducible-build-a.log"
cp "$temporary/build-b.log" \
    "$temporary/report-stage/reproducible-build-b.log"

{
    printf '%s\n' 'PHASE00_REPRODUCIBLE_BUILD=PASS'
    printf 'SOURCE_MANIFEST_SHA256=%s\n' "$source_digest"
    printf 'ARTIFACT_COUNT=%s\n' "$artifact_count"
    printf 'ARTIFACT_MANIFEST_SHA256=%s\n' "$artifact_manifest_digest"
    printf 'COMMAND_ENV_MAVEN_SKIP_RC=%s\n' "${MAVEN_SKIP_RC-<unset>}"
    printf '%s\n' 'WORKSPACE_A_EXIT_CODE=0'
    printf '%s\n' 'WORKSPACE_B_EXIT_CODE=0'
    printf '%s\n' 'SOURCE_TRAVERSAL_STATUS=PASS'
    printf '%s\n' 'ARTIFACT_TRAVERSAL_STATUS=PASS'
} >"$temporary/report-stage/reproducible-build.txt"

# No PASS artifact is visible until all traversal, build, hashing, and equality
# gates above have completed.
mkdir -p "$evidence_directory"
cp "$temporary/report-stage/reproducible-source-manifest.tsv" \
    "$evidence_directory/reproducible-source-manifest.tsv"
cp "$temporary/report-stage/reproducible-artifact-manifest.tsv" \
    "$evidence_directory/reproducible-artifact-manifest.tsv"
cp "$temporary/report-stage/reproducible-build-a.log" \
    "$evidence_directory/reproducible-build-a.log"
cp "$temporary/report-stage/reproducible-build-b.log" \
    "$evidence_directory/reproducible-build-b.log"
report_publish_stage="$evidence_directory/.reproducible-build.txt.$$"
gate_capture_checked 'cp(stage-reproducible-build-report)' \
    "$temporary/report-stage-copy.out" \
    "$temporary/report-stage-copy.err" \
    cp "$temporary/report-stage/reproducible-build.txt" \
        "$report_publish_stage"
gate_capture_checked 'mv(publish-reproducible-build-report)' \
    "$temporary/report-publish.out" \
    "$temporary/report-publish.err" \
    mv "$report_publish_stage" "$evidence_directory/reproducible-build.txt"

gate_replay_text_file "$evidence_directory/reproducible-build.txt"
