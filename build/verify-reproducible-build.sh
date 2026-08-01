#!/usr/bin/env bash
set -euo pipefail

# Determine repository root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$REPO_ROOT"

echo "=== Verifying Reproducible Build ==="

# 1. Refuse uncommitted dirty state or unverified HEAD
if [ -n "$(git status --porcelain)" ]; then
  echo "ERROR: Repository has uncommitted changes (dirty working tree)." >&2
  git status --short >&2
  exit 1
fi

if ! git rev-parse --verify HEAD >/dev/null 2>&1; then
  echo "ERROR: Cannot verify git HEAD commit." >&2
  exit 1
fi

COMMIT_SHA="$(git rev-parse HEAD)"
echo "Verified HEAD commit: ${COMMIT_SHA}"

# Parse and normalize any arguments (e.g. converting relative -Dmaven.repo.local=... to absolute path)
MVN_ARGS=()
for arg in "$@"; do
  if [[ "$arg" =~ ^-Dmaven\.repo\.local=(.*)$ ]]; then
    repo_path="${BASH_REMATCH[1]}"
    if [[ "$repo_path" != /* ]]; then
      repo_path="$(cd "$REPO_ROOT" && pwd)/$repo_path"
    fi
    MVN_ARGS+=("-Dmaven.repo.local=$repo_path")
  else
    MVN_ARGS+=("$arg")
  fi
done

# 2. Create isolated temporary build workspaces
WORK1=$(mktemp -d "${TMPDIR:-/tmp}/ro-next-repro-1.XXXXXX")
WORK2=$(mktemp -d "${TMPDIR:-/tmp}/ro-next-repro-2.XXXXXX")
HASHES1=$(mktemp "${TMPDIR:-/tmp}/ro-next-hashes-1.XXXXXX")
HASHES2=$(mktemp "${TMPDIR:-/tmp}/ro-next-hashes-2.XXXXXX")

cleanup() {
  rm -rf "$WORK1" "$WORK2" "$HASHES1" "$HASHES2"
}
trap cleanup EXIT

echo "Created Workspace 1: $WORK1"
echo "Created Workspace 2: $WORK2"

# 3. Copy repository source files into both workspaces via git archive
git archive HEAD | tar -x -C "$WORK1"
git archive HEAD | tar -x -C "$WORK2"

chmod +x "$WORK1/mvnw" "$WORK2/mvnw"

# 4. Run Maven package build in both workspaces
echo "=== Executing Build 1 in $WORK1 ==="
(cd "$WORK1" && ./mvnw -B -ntp -Dstyle.color=never package "${MVN_ARGS[@]}")

echo "=== Executing Build 2 in $WORK2 ==="
(cd "$WORK2" && ./mvnw -B -ntp -Dstyle.color=never package "${MVN_ARGS[@]}")

# Helper for computing SHA-256
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

collect_jar_hashes() {
  local work_dir="$1"
  local outfile="$2"
  (
    cd "$work_dir"
    find . -type f -name "*.jar" ! -path "*/.mvn/*" | sort | while read -r jar_file; do
      local hash
      hash=$(compute_sha256 "$jar_file")
      echo "${jar_file#./}  ${hash}"
    done
  ) > "$outfile"
}

# 5. Collect JAR files and compute SHA-256 hashes
collect_jar_hashes "$WORK1" "$HASHES1"
collect_jar_hashes "$WORK2" "$HASHES2"

echo "=== Build 1 Artifact Hashes ==="
cat "$HASHES1"

echo "=== Build 2 Artifact Hashes ==="
cat "$HASHES2"

# 6. Compare SHA-256 lists between the two builds
echo "=== Comparing Artifact Hashes ==="
if diff -u "$HASHES1" "$HASHES2"; then
  echo "SUCCESS: 100% reproducible build confirmed! SHA-256 hashes match perfectly across isolated builds."
  exit 0
else
  echo "ERROR: Reproducible build verification failed! SHA-256 hash mismatch detected." >&2
  exit 1
fi
