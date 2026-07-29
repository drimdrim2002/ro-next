#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    printf 'usage: %s <unsealed-evidence-directory>\n' "$0" >&2
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

bundle=$(CDPATH= cd -- "$1" 2>/dev/null && pwd) || {
    printf 'evidence directory does not exist: %s\n' "$1" >&2
    exit 1
}
manifest="$bundle/evidence-manifest.tsv"
manifest_digest="$bundle/evidence-manifest.sha256"
gate_make_temporary_directory 'mktemp(evidence-seal)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-evidence-seal.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
manifest_stage="$bundle/.evidence-manifest.tsv.sealing.$$"
digest_stage="$bundle/.evidence-manifest.sha256.sealing.$$"
remove_incomplete_seal=0
seal_complete=0

cleanup() {
    cleanup_status=$?
    set +e
    command rm -rf -- "$temporary"
    cleanup_temporary_status=$?
    command rm -f -- "$manifest_stage" "$digest_stage"
    cleanup_stage_status=$?
    if [ "$remove_incomplete_seal" -eq 1 ] && [ "$seal_complete" -ne 1 ]; then
        command rm -f -- "$manifest" "$manifest_digest"
        cleanup_public_status=$?
    else
        cleanup_public_status=0
    fi
    if [ "$cleanup_temporary_status" -ne 0 ]; then
        printf 'tool=rm(cleanup-seal-temporary) result=NOT_EVALUATED exit=%s\n' \
            "$cleanup_temporary_status" >&2
    fi
    if [ "$cleanup_stage_status" -ne 0 ]; then
        printf 'tool=rm(cleanup-seal-stage) result=NOT_EVALUATED exit=%s\n' \
            "$cleanup_stage_status" >&2
    fi
    if [ "$cleanup_public_status" -ne 0 ]; then
        printf 'tool=rm(remove-incomplete-public-seal) result=NOT_EVALUATED exit=%s\n' \
            "$cleanup_public_status" >&2
    fi
    exit "$cleanup_status"
}
trap cleanup EXIT HUP INT TERM

if [ -e "$manifest" ] || [ -e "$manifest_digest" ]; then
    printf '%s\n' 'refusing to overwrite an existing evidence seal' >&2
    exit 1
fi

# The first complete traversal is staged and checked before sorting or hashing.
# The helper rejects traversal errors, symlinks, non-regular entries, and
# declared-unreadable files/directories without relying on a pipeline status.
gate_inventory_tree "$bundle" "$temporary/tree-before"
tree_before=$GATE_TREE_DIGEST
test -s "$GATE_TREE_FILE_MANIFEST" || {
    printf '%s\n' 'evidence bundle payload is empty' >&2
    exit 1
}

# Strip the metadata mode column from the internal snapshot manifest. The
# public seal remains canonical path, byte length, and SHA-256.
gate_capture_checked 'awk(public-evidence-manifest-projection)' \
    "$temporary/evidence-manifest.tsv" \
    "$temporary/evidence-manifest.projection.err" \
    awk -F '	' 'BEGIN { OFS="\t" } { print $1, $3, $4 }' \
    "$GATE_TREE_FILE_MANIFEST"
test -s "$temporary/evidence-manifest.tsv"
gate_sha256_file "$temporary/evidence-manifest.tsv" \
    "$temporary/evidence-manifest"
printf '%s\n' "$GATE_SHA256" >"$temporary/evidence-manifest.sha256"

# A second independent traversal closes create/delete/type/content/mode races
# before any seal file is published.
gate_inventory_tree "$bundle" "$temporary/tree-after"
tree_after=$GATE_TREE_DIGEST
test "$tree_before" = "$tree_after" || {
    printf '%s\n' 'evidence bundle changed during traversal or hashing' >&2
    exit 1
}

# Validate the would-be seal in an isolated candidate tree first.
mkdir -p "$temporary/candidate"
gate_capture_checked 'cp(seal-candidate-payload)' \
    "$temporary/candidate-payload-copy.out" \
    "$temporary/candidate-payload-copy.err" \
    cp -R "$bundle/." "$temporary/candidate"
gate_capture_checked 'cp(seal-candidate-manifest)' \
    "$temporary/candidate-manifest-copy.out" \
    "$temporary/candidate-manifest-copy.err" \
    cp "$temporary/evidence-manifest.tsv" \
        "$temporary/candidate/evidence-manifest.tsv"
gate_capture_checked 'cp(seal-candidate-digest)' \
    "$temporary/candidate-digest-copy.out" \
    "$temporary/candidate-digest-copy.err" \
    cp "$temporary/evidence-manifest.sha256" \
        "$temporary/candidate/evidence-manifest.sha256"
gate_capture_checked 'verify-evidence-bundle(seal-candidate)' \
    "$temporary/candidate-verification.txt" \
    "$temporary/candidate-verification.err" \
    "$repository/build/verify-evidence-bundle.sh" "$temporary/candidate"

# Publish only complete candidates. If interruption or final verification
# fails between the two POSIX renames, the trap removes both public seal files.
gate_capture_checked 'cp(stage-public-evidence-manifest)' \
    "$temporary/public-manifest-stage.out" \
    "$temporary/public-manifest-stage.err" \
    cp "$temporary/evidence-manifest.tsv" "$manifest_stage"
gate_capture_checked 'cp(stage-public-evidence-digest)' \
    "$temporary/public-digest-stage.out" \
    "$temporary/public-digest-stage.err" \
    cp "$temporary/evidence-manifest.sha256" "$digest_stage"
remove_incomplete_seal=1
gate_capture_checked 'mv(publish-evidence-manifest)' \
    "$temporary/public-manifest-publish.out" \
    "$temporary/public-manifest-publish.err" \
    mv "$manifest_stage" "$manifest"
gate_capture_checked 'mv(publish-evidence-digest)' \
    "$temporary/public-digest-publish.out" \
    "$temporary/public-digest-publish.err" \
    mv "$digest_stage" "$manifest_digest"

gate_capture_checked 'verify-evidence-bundle(final-seal)' \
    "$temporary/final-verification.txt" \
    "$temporary/final-verification.err" \
    "$repository/build/verify-evidence-bundle.sh" "$bundle"
seal_complete=1
gate_replay_text_file "$temporary/final-verification.txt"
