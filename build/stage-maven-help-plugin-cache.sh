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

if [ "$#" -ne 3 ]; then
    printf 'usage: %s <candidate-repository> <controlled-snapshot> <provenance-output>\n' \
        "$0" >&2
    exit 2
fi

candidate=$1
snapshot=$2
provenance_output=$3
allowlist="$repository/build/maven-help-plugin-3.5.1-cache.sha256"
gate_make_temporary_directory 'mktemp(help-plugin-cache-stage)' \
    "${TMPDIR:-/tmp}/ro-next-help-cache-stage.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

case "$candidate" in
    /*) ;;
    *)
        printf 'candidate Maven repository must be absolute\n' >&2
        exit 1
        ;;
esac
case "$snapshot" in
    /*) ;;
    *)
        printf 'controlled Maven snapshot must be absolute\n' >&2
        exit 1
        ;;
esac
test -d "$candidate"
test ! -e "$snapshot"
test -f "$allowlist"
mkdir -p "$snapshot"

: >"$temporary/actual-cache.sha256"
artifact_count=0
while IFS='	' read -r relative expected_digest; do
    case "$relative" in
        ''|\#*) continue ;;
        /*|./*|../*|*/../*|*/..|*//*)
            printf 'non-canonical controlled cache path: %s\n' "$relative" >&2
            exit 1
            ;;
    esac
    case "$expected_digest" in
        *[!0-9a-f]*|'')
            printf 'invalid controlled cache digest: %s\n' "$relative" >&2
            exit 1
            ;;
    esac
    test "${#expected_digest}" -eq 64

    source_file="$candidate/$relative"
    test -f "$source_file"
    test ! -L "$source_file"
    artifact_count=$((artifact_count + 1))
    prefix="$temporary/artifact-$artifact_count"
    gate_assert_declared_readable "$source_file" F "$prefix"
    gate_sha256_file "$source_file" "$prefix"
    if [ "$GATE_SHA256" != "$expected_digest" ]; then
        printf 'controlled cache digest mismatch: %s\n' "$relative" >&2
        exit 1
    fi

    destination_file="$snapshot/$relative"
    destination_parent=${destination_file%/*}
    mkdir -p "$destination_parent"
    gate_capture_checked "cp(help-plugin-cache:$relative)" \
        "$prefix.copy.out" "$prefix.copy.err" \
        cp "$source_file" "$destination_file"
    printf '%s\t%s\n' "$relative" "$expected_digest" \
        >>"$temporary/actual-cache.sha256"
done <"$allowlist"

test "$artifact_count" -eq 50
test -f "$snapshot/org/apache/maven/plugins/maven-help-plugin/3.5.1/maven-help-plugin-3.5.1.jar"
test -f "$snapshot/org/apache/maven/plugins/maven-help-plugin/3.5.1/maven-help-plugin-3.5.1.pom"

gate_sha256_file "$allowlist" "$temporary/allowlist"
allowlist_digest=$GATE_SHA256
gate_sha256_file "$temporary/actual-cache.sha256" "$temporary/snapshot"
snapshot_digest=$GATE_SHA256

{
    printf '%s\n' 'CACHE_PURPOSE=OFFICIAL_MAVEN_HELP_EFFECTIVE_POM'
    printf '%s\n' 'PLUGIN_COORDINATE=org.apache.maven.plugins:maven-help-plugin:3.5.1'
    printf '%s\n' 'UPSTREAM_REPOSITORY=https://repo.maven.apache.org/maven2'
    printf 'CACHE_SOURCE_PROVENANCE=%s\n' \
        "${PHASE00_HELP_PLUGIN_CACHE_PROVENANCE:-CONTROLLED_LOCAL_CACHE}"
    printf '%s\n' 'CACHE_STAGE_MODE=ALLOWLISTED_SHA256_VERIFIED_COPY'
    printf 'CACHE_ARTIFACT_COUNT=%s\n' "$artifact_count"
    printf 'CACHE_ALLOWLIST_SHA256=%s\n' "$allowlist_digest"
    printf 'CACHE_SNAPSHOT_SHA256=%s\n' "$snapshot_digest"
    printf '%s\n' 'NETWORK_ACCESS=NOT_USED_DURING_STAGING'
} >"$provenance_output"

gate_replay_text_file "$provenance_output"
