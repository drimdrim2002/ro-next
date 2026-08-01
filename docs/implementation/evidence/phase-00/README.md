# Phase 00 Architecture Skeleton Evidence & Reproducibility Guide

## Overview

Instructions to collect, seal, and verify the Phase 00 Architecture Skeleton
evidence bundle for `ro-next`.

Generated artifacts live under `target/phase-00-evidence/`:

- `target/phase-00-evidence/E-P00-BUILD/`
- `target/phase-00-evidence/E-P00-ARCH/`
- `target/phase-00-evidence/E-P00-LEGACY/` (removal attestation; no legacy module)
- `target/phase-00-evidence/REVIEW_VERDICTS.md` (independent reviewer)

Tracked handoff notes (not the sealed runtime bundle):

- `docs/implementation/evidence/phase-00/Phase01SkeletonHandoff.md`
- this file

---

## Reproducing Evidence Bundles

Run all commands from the repository root on a **clean** git tree (required by
`build/verify-reproducible-build.sh`).

### 1. Collect BUILD Evidence (`E-P00-BUILD`)

```bash
mkdir -p target/phase-00-evidence/E-P00-BUILD/

./mvnw -B -ntp -Dstyle.color=never verify \
  > target/phase-00-evidence/E-P00-BUILD/verify.log 2>&1

./mvnw -B -ntp toolchains:display-discovered-jdk-toolchains \
  > target/phase-00-evidence/E-P00-BUILD/toolchains.txt 2>&1

./mvnw -B -ntp -Dstyle.color=never -Dmaven.repo.local=target/phase-00-m2 dependency:go-offline \
  > target/phase-00-evidence/E-P00-BUILD/go-offline.log 2>&1

./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 verify \
  > target/phase-00-evidence/E-P00-BUILD/offline-verify.log 2>&1

./build/verify-reproducible-build.sh \
  > target/phase-00-evidence/E-P00-BUILD/reproducible-build.log 2>&1

cp .mvn/wrapper/maven-wrapper.properties \
  target/phase-00-evidence/E-P00-BUILD/maven-wrapper.properties
```

Write `E-P00-BUILD/MANIFEST.md` with: commit SHA, toolchain, command/exit table,
and the OPEN items table (C-17 GATED, O1/D2 OPEN, Q-BENCH-02 OPEN — no numeric defaults).

### 2. Collect ARCH Evidence (`E-P00-ARCH`)

```bash
mkdir -p target/phase-00-evidence/E-P00-ARCH/

./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules -am test \
  > target/phase-00-evidence/E-P00-ARCH/archunit-tests.log 2>&1

./build/verify-bytecode-boundaries.sh \
  > target/phase-00-evidence/E-P00-ARCH/bytecode-boundaries.log 2>&1

./build/verify-negative-architecture.sh \
  > target/phase-00-evidence/E-P00-ARCH/negative-arch.log 2>&1

./mvnw -B -ntp dependency:tree \
  -pl rpdptw/core,rpdptw/solver,rpdptw/verification,rpdptw/application,rpdptw/profiles/standard \
  > target/phase-00-evidence/E-P00-ARCH/target-dep-trees.txt 2>&1

cp build/architecture-rules/src/test/resources/customer-identity-allowlist.txt \
  target/phase-00-evidence/E-P00-ARCH/customer-identity-allowlist.txt
```

### 3. Collect LEGACY Evidence (`E-P00-LEGACY`) — removal only

There is **no** `legacy/` module. Record deliberate removal:

```bash
mkdir -p target/phase-00-evidence/E-P00-LEGACY/

# Prove absence of legacy module paths and GCP placeholder sources
{
  echo "=== legacy path absence ==="
  for p in legacy legacy/gcp-placeholder gcp Dockerfile; do
    if [ -e "$p" ]; then
      echo "PRESENT (unexpected): $p"
    else
      echo "ABSENT (expected): $p"
    fi
  done
  echo
  echo "=== reactor modules (root pom) ==="
  rg -n '<module>' pom.xml || true
  echo
  echo "=== no com.ronext.optimizer sources under rpdptw ==="
  if rg -n 'package com\.ronext\.optimizer' rpdptw build 2>/dev/null; then
    echo "UNEXPECTED optimizer package reference in target tree"
  else
    echo "OK: no optimizer package sources under rpdptw/build sources scanned"
  fi
} > target/phase-00-evidence/E-P00-LEGACY/legacy-removal-proof.txt 2>&1
```

Write `E-P00-LEGACY/MANIFEST.md` stating user decision: legacy not retained;
characterization suite not applicable; OPEN table corrected (C-17/O1/Q-BENCH-02).

### 4. Independent reviewer verdicts

After collecting the three bundles, write
`target/phase-00-evidence/REVIEW_VERDICTS.md` with PASS/FAIL/WAIVED per bundle,
timestamp, commit SHA, and residual blockers. Copy a summary into
`docs/implementation/evidence/phase-00/REVIEW_VERDICTS.md` if the review should
be tracked in git (optional; seal always covers the target/ copy).

---

## Sealing and Verifying Evidence

```bash
./build/verify-evidence-bundle.sh target/phase-00-evidence
./build/verify-evidence-bundle.sh --verify target/phase-00-evidence
```

---

## OPEN items (authority copy — use in every MANIFEST)

| Item ID | Status | Meaning |
|---|---|---|
| **C-17** | **GATED** | Hybrid route-selection / OR-Tools backends only after 14A receipt + scope approval |
| **O1 / D2** | **OPEN** | Compute product = Lambda \| ECS; no single-product pin |
| **Q-BENCH-02** | **OPEN** | Official numeric thresholds not fixed in Phase 00 |

Storage is **S3 only**; **no DB / no Redis** (Master A10 · Architecture §3.2). JDBC is out of scope and must not appear as a production dependency of target modules.
