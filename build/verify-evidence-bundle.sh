#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    printf 'usage: %s <sealed-evidence-directory>\n' "$0" >&2
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
gate_make_temporary_directory 'mktemp(evidence-bundle-verify)' \
    "${TMPDIR:-/tmp}/ro-next-phase00-evidence-verify.XXXXXX"
temporary=$GATE_TEMPORARY_DIRECTORY
trap 'command rm -rf -- "$temporary" >/dev/null 2>&1 || :' EXIT HUP INT TERM

fail() {
    printf '%s\n' "$1" >&2
    exit 1
}

# Independent traversal 1: no sorting or hashing begins unless find itself
# succeeded. The resulting snapshot covers path, type, mode, byte length, and
# content digest for every entry.
gate_inventory_tree "$bundle" "$temporary/tree-before"
tree_before=$GATE_TREE_DIGEST
tree_before_files=$GATE_TREE_FILE_MANIFEST

test -f "$manifest" || fail 'sealed evidence manifest is missing'
test -f "$manifest_digest" || fail 'sealed evidence manifest digest is missing'
test -s "$manifest" || fail 'sealed evidence manifest is empty'

gate_capture_checked 'wc(lines:evidence-manifest-digest)' \
    "$temporary/digest-line-count.txt" \
    "$temporary/digest-line-count.err" \
    wc -l "$manifest_digest"
gate_capture_checked 'awk(evidence-manifest-digest-line-count)' \
    "$temporary/digest-line-count.value" \
    "$temporary/digest-line-count.parse.err" \
    awk 'NR == 1 { print $1 }' "$temporary/digest-line-count.txt"
IFS= read -r digest_line_count <"$temporary/digest-line-count.value" || :
test "$digest_line_count" -eq 1 \
    || fail 'sealed evidence manifest digest must contain exactly one line'

IFS= read -r expected <"$manifest_digest" || :
case "$expected" in
    *[!0-9a-f]*|'')
        fail 'sealed evidence manifest digest is not canonical lowercase SHA-256'
        ;;
esac
test "${#expected}" -eq 64 \
    || fail 'sealed evidence manifest digest is not canonical lowercase SHA-256'

gate_capture_checked 'awk(evidence-manifest-shape)' \
    "$temporary/manifest-shape.out" "$temporary/manifest-shape.err" \
    awk -F '	' 'NF != 3 { print }' "$manifest"
test ! -s "$temporary/manifest-shape.out" \
    || fail 'sealed evidence manifest contains a malformed TSV row'

previous=
while IFS='	' read -r path bytes digest; do
    case "$path" in
        ''|/*|./*|../*|*/../*|*/..|*//*)
            fail "sealed evidence manifest contains a non-canonical path: $path"
            ;;
        evidence-manifest.tsv|evidence-manifest.sha256)
            fail "sealed evidence manifest contains a seal file as payload: $path"
            ;;
    esac
    case "$bytes" in
        ''|*[!0-9]*)
            fail "sealed evidence manifest contains a non-numeric length: $path"
            ;;
    esac
    case "$digest" in
        ''|*[!0-9a-f]*)
            fail "sealed evidence manifest contains a non-canonical digest: $path"
            ;;
    esac
    test "${#digest}" -eq 64 \
        || fail "sealed evidence manifest contains a non-canonical digest: $path"
    if [ -n "$previous" ] && {
        [ "$path" \< "$previous" ] || [ "$path" = "$previous" ]
    }; then
        fail "sealed evidence manifest paths are not unique stable-order entries: $path"
    fi
    previous=$path
done <"$manifest"

gate_sha256_file "$manifest" "$temporary/actual-manifest"
actual=$GATE_SHA256
test "$expected" = "$actual" \
    || fail 'sealed evidence manifest digest mismatch'

# Derive the exact independently traversed payload set. This loop does not use
# grep, so seal-file exclusion cannot reinterpret detector errors as absence.
: >"$temporary/current.tsv"
while IFS='	' read -r path mode bytes digest; do
    case "$path" in
        evidence-manifest.tsv|evidence-manifest.sha256)
            continue
            ;;
    esac
    printf '%s\t%s\t%s\n' "$path" "$bytes" "$digest" \
        >>"$temporary/current.tsv"
done <"$tree_before_files"

test -s "$temporary/current.tsv" || fail 'evidence bundle payload is empty'
gate_compare_files 'sealed manifest versus independently traversed payload' \
    "$manifest" "$temporary/current.tsv" \
    "$temporary/manifest-payload.cmp.out" \
    "$temporary/manifest-payload.cmp.err"

# Independent traversal 2 is required even after payload equality. A verifier
# PASS is emitted only after content and metadata are unchanged.
gate_inventory_tree "$bundle" "$temporary/tree-after"
tree_after=$GATE_TREE_DIGEST
test "$tree_before" = "$tree_after" \
    || fail 'read-only evidence verification changed the bundle tree'

gate_capture_checked 'awk(evidence-manifest-file-count)' \
    "$temporary/file-count.txt" "$temporary/file-count.err" \
    awk 'END { print NR + 0 }' "$manifest"
IFS= read -r file_count <"$temporary/file-count.txt" || :
printf '%s\n' 'PHASE00_EVIDENCE_BUNDLE=PASS'
printf '%s\n' 'VERIFICATION_MODE=READ_ONLY'
printf '%s\n' 'TREE_DIGEST_SCOPE=PATH_TYPE_MODE_LENGTH_CONTENT'
printf 'FILE_COUNT=%s\n' "$file_count"
printf 'MANIFEST_SHA256=%s\n' "$actual"
printf 'TREE_BEFORE_SHA256=%s\n' "$tree_before"
printf 'TREE_AFTER_SHA256=%s\n' "$tree_after"
