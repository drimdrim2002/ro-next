# Phase 00 Architecture Skeleton Evidence & Reproducibility Guide

## Overview
This document provides instructions on how to collect, seal, and verify the complete Phase 00 Architecture Skeleton evidence bundle for `ro-next`.

All generated evidence artifacts are placed under `target/phase-00-evidence/` in three designated bundles:
- `target/phase-00-evidence/E-P00-BUILD/`
- `target/phase-00-evidence/E-P00-ARCH/`
- `target/phase-00-evidence/E-P00-LEGACY/`

---

## Reproducing Evidence Bundles

### 1. Collect BUILD Evidence (`E-P00-BUILD`)
Run the following commands from the repository root:

```bash
# Create evidence directory
mkdir -p target/phase-00-evidence/E-P00-BUILD/

# 1.1 Maven full verification log
./mvnw -B -ntp -Dstyle.color=never verify > target/phase-00-evidence/E-P00-BUILD/verify.log 2>&1

# 1.2 Toolchain discovery report
./mvnw -B -ntp toolchains:display-discovered-jdk-toolchains > target/phase-00-evidence/E-P00-BUILD/toolchains.txt 2>&1

# 1.3 Offline dependency populator
./mvnw -B -ntp -Dstyle.color=never -Dmaven.repo.local=target/phase-00-m2 dependency:go-offline > target/phase-00-evidence/E-P00-BUILD/go-offline.log 2>&1

# 1.4 Strict offline build verification
./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 verify > target/phase-00-evidence/E-P00-BUILD/offline-verify.log 2>&1

# 1.5 Reproducible build verification (dual clean build comparison)
./build/verify-reproducible-build.sh > target/phase-00-evidence/E-P00-BUILD/reproducible-build.log 2>&1

# 1.6 Maven wrapper properties copy
cp .mvn/wrapper/maven-wrapper.properties target/phase-00-evidence/E-P00-BUILD/maven-wrapper.properties
```

---

### 2. Collect ARCH Evidence (`E-P00-ARCH`)
Run the following commands:

```bash
mkdir -p target/phase-00-evidence/E-P00-ARCH/

# 2.1 ArchUnit and core purity rule tests
./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules -am test > target/phase-00-evidence/E-P00-ARCH/archunit-tests.log 2>&1

# 2.2 Bytecode boundary verification across built target JARs
./build/verify-bytecode-boundaries.sh > target/phase-00-evidence/E-P00-ARCH/bytecode-boundaries.log 2>&1

# 2.3 Negative architecture test proof (verifying fail-closed behavior)
./build/verify-negative-architecture.sh > target/phase-00-evidence/E-P00-ARCH/negative-arch.log 2>&1

# 2.4 Target modules dependency tree
./mvnw -B -ntp dependency:tree -pl rpdptw/core,rpdptw/solver,rpdptw/verification,rpdptw/application,rpdptw/profiles/standard > target/phase-00-evidence/E-P00-ARCH/target-dep-trees.txt 2>&1

# 2.5 Customer identity allowlist copy
cp build/architecture-rules/src/test/resources/customer-identity-allowlist.txt target/phase-00-evidence/E-P00-ARCH/customer-identity-allowlist.txt
```

---

### 3. Collect LEGACY Evidence (`E-P00-LEGACY`)
Run the following commands:

```bash
mkdir -p target/phase-00-evidence/E-P00-LEGACY/

# 3.1 Legacy characterization test suite
./mvnw -B -ntp -Dstyle.color=never -pl legacy/gcp-placeholder -am test > target/phase-00-evidence/E-P00-LEGACY/legacy-tests.log 2>&1

# 3.2 Legacy dependency tree
./mvnw -B -ntp dependency:tree -pl legacy/gcp-placeholder > target/phase-00-evidence/E-P00-LEGACY/legacy-dep-tree.txt 2>&1
```

---

## Sealing and Verifying Evidence

### Sealing the Evidence Bundle
Once all logs and manifests are created in `target/phase-00-evidence/`, seal the bundle by generating SHA-256 checksums and the top-level manifest:

```bash
./build/verify-evidence-bundle.sh target/phase-00-evidence
```

### Verifying Bundle Integrity
To verify that no files have been tampered with or modified:

```bash
./build/verify-evidence-bundle.sh --verify target/phase-00-evidence
```

Expected Output:
`SUCCESS: Evidence bundle integrity verified (18 files verified, 0 mismatches).`
