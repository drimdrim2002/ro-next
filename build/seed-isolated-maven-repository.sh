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

if [ "$#" -ne 4 ]; then
    printf 'usage: %s <source-local-repository> <isolated-repository> <help-plugin-snapshot> <provenance-output>\n' \
        "$0" >&2
    exit 2
fi

source_repository=$1
isolated_repository=$2
help_plugin_snapshot=$3
provenance_output=$4

test -d "$source_repository" || {
    printf 'source Maven repository is missing: %s\n' "$source_repository" >&2
    exit 1
}
case "$source_repository" in
    /*) ;;
    *)
        printf 'source Maven repository must be absolute\n' >&2
        exit 1
        ;;
esac
case "$isolated_repository" in
    /*) ;;
    *)
        printf 'isolated Maven repository must be absolute\n' >&2
        exit 1
        ;;
esac
case "$help_plugin_snapshot" in
    /*) ;;
    *)
        printf 'Help Plugin snapshot must be absolute\n' >&2
        exit 1
        ;;
esac
test "$source_repository" != "$isolated_repository" || {
    printf 'source and isolated Maven repositories must differ\n' >&2
    exit 1
}
test ! -e "$isolated_repository" || {
    printf 'isolated Maven repository already exists: %s\n' \
        "$isolated_repository" >&2
    exit 1
}
test -d "$help_plugin_snapshot"
test -f "$help_plugin_snapshot/org/apache/maven/plugins/maven-help-plugin/3.5.1/maven-help-plugin-3.5.1.jar"
test -f "$help_plugin_snapshot/org/apache/maven/plugins/maven-help-plugin/3.5.1/maven-help-plugin-3.5.1.pom"

gate_make_temporary_directory 'mktemp(isolated-maven-seed)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-maven-seed.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM
mkdir -p "$isolated_repository"
gate_capture_checked 'cp(base-isolated-maven-repository)' \
    "$temporary/base-copy.out" "$temporary/base-copy.err" \
    cp -R "$source_repository/." "$isolated_repository"
gate_capture_checked 'cp(official-help-plugin-cache-overlay)' \
    "$temporary/help-copy.out" "$temporary/help-copy.err" \
    cp -R "$help_plugin_snapshot/." "$isolated_repository"

gate_stage_find "$isolated_repository" "$temporary/artifacts.find.raw" \
    "$temporary/artifacts.find.err" \
    . -type f \( -name '*.jar' -o -name '*.pom' \) -print
artifact_count=0
while IFS= read -r artifact_path; do
    test -n "$artifact_path" || continue
    artifact_count=$((artifact_count + 1))
done <"$temporary/artifacts.find.raw"
test "$artifact_count" -gt 0

help_artifact_count=0
gate_stage_find "$help_plugin_snapshot" "$temporary/help.find.raw" \
    "$temporary/help.find.err" \
    . -type f \( -name '*.jar' -o -name '*.pom' \) -print
while IFS= read -r help_artifact_path; do
    test -n "$help_artifact_path" || continue
    help_artifact_count=$((help_artifact_count + 1))
done <"$temporary/help.find.raw"
test "$help_artifact_count" -eq 50

{
    printf '%s\n' 'SEED_MODE=LOCAL_READ_ONLY_CACHE_SNAPSHOT'
    printf '%s\n' 'SEED_NETWORK_ACCESS=NOT_USED'
    printf '%s\n' 'ISOLATED_REPOSITORY_MUTATION_SCOPE=TARGET_DIRECTORY_ONLY'
    printf '%s\n' 'SOURCE_REPOSITORY=LOCAL_CACHE_SNAPSHOT'
    printf '%s\n' 'HELP_PLUGIN_OVERLAY=ALLOWLISTED_SHA256_VERIFIED_SNAPSHOT'
    printf 'HELP_PLUGIN_OVERLAY_ARTIFACT_COUNT=%s\n' "$help_artifact_count"
    printf '%s\n' 'HELP_PLUGIN_COORDINATE=org.apache.maven.plugins:maven-help-plugin:3.5.1'
    printf 'ISOLATED_ARTIFACT_COUNT=%s\n' "$artifact_count"
} >"$provenance_output"

gate_replay_text_file "$provenance_output"
