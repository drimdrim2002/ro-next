#!/usr/bin/env bash
set -euo pipefail

# Determine repository root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

MODE="auto"
EVIDENCE_DIR=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    -g|--generate)
      MODE="generate"
      shift
      ;;
    -v|--verify)
      MODE="verify"
      shift
      ;;
    -h|--help)
      echo "Usage: $0 [--generate|--verify] [EVIDENCE_DIR]"
      echo "Validates or generates evidence bundle manifest and SHA-256 checksums."
      exit 0
      ;;
    *)
      if [ -z "$EVIDENCE_DIR" ]; then
        EVIDENCE_DIR="$1"
      else
        echo "ERROR: Unknown argument: $1" >&2
        exit 1
      fi
      shift
      ;;
  esac
done

if [ -z "$EVIDENCE_DIR" ]; then
  EVIDENCE_DIR="target/phase-00-evidence"
fi

# Ensure absolute or canonical path for checking
if [[ "$EVIDENCE_DIR" != /* ]]; then
  TARGET_PATH="$REPO_ROOT/$EVIDENCE_DIR"
else
  TARGET_PATH="$EVIDENCE_DIR"
fi

if [ ! -d "$TARGET_PATH" ]; then
  echo "ERROR: Evidence directory does not exist: $EVIDENCE_DIR ($TARGET_PATH)" >&2
  exit 1
fi

CANONICAL_DIR="$(cd "$TARGET_PATH" && pwd -P)"

# Helper functions
compute_sha256() {
  local file="$1"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$file" | awk '{print $1}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$file" | awk '{print $1}'
  else
    openssl dgst -sha256 "$file" | awk '{print $2}'
  fi
}

get_byte_length() {
  local file="$1"
  wc -c < "$file" | tr -d '[:space:]'
}

validate_relative_path() {
  local rel_path="$1"
  if [ -z "$rel_path" ]; then
    echo "ERROR: Empty file path encountered in evidence verification." >&2
    return 1
  fi
  if [[ "$rel_path" = /* ]]; then
    echo "ERROR: Absolute path not allowed in evidence bundle: $rel_path" >&2
    return 1
  fi
  if [[ "$rel_path" =~ (\.\./|\.\.$|^\.\.$) ]]; then
    echo "ERROR: Path traversal attempt detected: $rel_path" >&2
    return 1
  fi
  return 0
}

# Symlink rejection check
SYMLINKS="$(find "$CANONICAL_DIR" -type l)"
if [ -n "$SYMLINKS" ]; then
  echo "ERROR: Symlinks are forbidden in evidence bundle:" >&2
  echo "$SYMLINKS" >&2
  exit 1
fi

if [ "$MODE" = "auto" ]; then
  if [ -f "$CANONICAL_DIR/checksums.sha256" ]; then
    MODE="verify"
  else
    MODE="generate"
  fi
fi

if [ "$MODE" = "generate" ]; then
  echo "=== Generating Evidence Bundle Seal in $EVIDENCE_DIR ==="

  MANIFEST_FILE="$CANONICAL_DIR/MANIFEST.md"
  CHECKSUM_FILE="$CANONICAL_DIR/checksums.sha256"

  TIMESTAMP="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

  {
    echo "# Evidence Bundle Manifest"
    echo ""
    echo "- **Generated:** ${TIMESTAMP}"
    echo "- **Target Directory:** \`${EVIDENCE_DIR}\`"
    echo ""
    echo "## File Integrity Table"
    echo ""
    echo "| Relative Path | Size (bytes) | SHA-256 Checksum |"
    echo "|:---|---:|:---|"
  } > "$MANIFEST_FILE"

  (
    cd "$CANONICAL_DIR"
    find . -type f ! -name "checksums.sha256" ! -name "MANIFEST.md" | sort | while read -r file; do
      rel_file="${file#./}"
      validate_relative_path "$rel_file"
      size=$(get_byte_length "$rel_file")
      hash=$(compute_sha256 "$rel_file")
      echo "| \`${rel_file}\` | ${size} | \`${hash}\` |" >> "$MANIFEST_FILE"
    done
  )

  (
    cd "$CANONICAL_DIR"
    find . -type f ! -name "checksums.sha256" | sort | while read -r file; do
      rel_file="${file#./}"
      hash=$(compute_sha256 "$rel_file")
      echo "${hash}  ${rel_file}"
    done
  ) > "$CHECKSUM_FILE"

  echo "SUCCESS: Created MANIFEST.md and checksums.sha256 in $EVIDENCE_DIR."
  exit 0

elif [ "$MODE" = "verify" ]; then
  echo "=== Verifying Evidence Bundle Integrity in $EVIDENCE_DIR ==="

  CHECKSUM_FILE="$CANONICAL_DIR/checksums.sha256"
  MANIFEST_FILE="$CANONICAL_DIR/MANIFEST.md"

  if [ ! -f "$CHECKSUM_FILE" ]; then
    echo "ERROR: Checksum manifest missing ($CHECKSUM_FILE)." >&2
    exit 1
  fi

  CHECKED_COUNT=0

  while read -r expected_hash rel_file; do
    [ -z "$expected_hash" ] && continue

    validate_relative_path "$rel_file"

    target_path="$CANONICAL_DIR/$rel_file"

    if [ ! -f "$target_path" ]; then
      echo "ERROR: Missing evidence file: $rel_file" >&2
      exit 1
    fi

    if [ -L "$target_path" ]; then
      echo "ERROR: Symlink detected for file: $rel_file" >&2
      exit 1
    fi

    actual_hash=$(compute_sha256 "$target_path")
    if [ "$actual_hash" != "$expected_hash" ]; then
      echo "ERROR: SHA-256 checksum mismatch for $rel_file!" >&2
      echo "  Expected: $expected_hash" >&2
      echo "  Actual:   $actual_hash" >&2
      exit 1
    fi

    CHECKED_COUNT=$((CHECKED_COUNT + 1))
  done < "$CHECKSUM_FILE"

  if [ -f "$MANIFEST_FILE" ]; then
    while read -r line; do
      if [[ "$line" =~ ^\|\ \`([^\`]+)\`\ \|\ ([0-9]+)\ \|\ \`([a-f0-9]+)\`\ \| ]]; then
        rel_file="${BASH_REMATCH[1]}"
        expected_size="${BASH_REMATCH[2]}"
        expected_hash="${BASH_REMATCH[3]}"

        target_path="$CANONICAL_DIR/$rel_file"
        if [ ! -f "$target_path" ]; then
          echo "ERROR: File listed in MANIFEST.md missing: $rel_file" >&2
          exit 1
        fi

        actual_size=$(get_byte_length "$target_path")
        if [ "$actual_size" != "$expected_size" ]; then
          echo "ERROR: Byte length mismatch for $rel_file in MANIFEST.md!" >&2
          echo "  Expected: $expected_size bytes" >&2
          echo "  Actual:   $actual_size bytes" >&2
          exit 1
        fi

        actual_hash=$(compute_sha256 "$target_path")
        if [ "$actual_hash" != "$expected_hash" ]; then
          echo "ERROR: SHA-256 checksum mismatch for $rel_file in MANIFEST.md!" >&2
          echo "  Expected: $expected_hash" >&2
          echo "  Actual:   $actual_hash" >&2
          exit 1
        fi
      fi
    done < "$MANIFEST_FILE"
  fi

  # Verify no unlisted extra files exist in CANONICAL_DIR
  (
    cd "$CANONICAL_DIR"
    find . -type f | sort | while read -r file; do
      rel_file="${file#./}"
      if [ "$rel_file" = "checksums.sha256" ]; then
        continue
      fi
      if ! grep -q -E "[[:space:]]${rel_file}$" "$CHECKSUM_FILE"; then
        echo "ERROR: Unlisted extra file found in evidence bundle: $rel_file" >&2
        exit 1
      fi
    done
  )

  echo "SUCCESS: Evidence bundle integrity verified (${CHECKED_COUNT} files verified, 0 mismatches)."
  exit 0
fi
