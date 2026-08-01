# Phase 00 — Build / Architecture Skeleton Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a multi-module Maven reactor and architecture guards for RPDPTW (`com.ronext.rpdptw`) that match Architecture §4.2, isolate the existing GCP legacy placeholder, and produce `E-P00-BUILD` / `E-P00-ARCH` / `E-P00-LEGACY` evidence — without implementing Domain math, AWS adapters, or C-17 backends.

**Architecture:** Root becomes a business-dependency-free parent/aggregator. Target modules live under `rpdptw/` (`core`, `solver`, `verification`, `application`, `profiles/standard`) plus `build/test-fixtures` and `build/architecture-rules`. Current `com.ronext.optimizer` sources move into `legacy/gcp-placeholder` with characterization tests. Enforcer + ArchUnit enforce Architecture §4.6 forbids. Empty `adapters/*`, `backends/*`, `apps/*`, `deployment/` modules are **not** created in this phase.

**Tech Stack:** Java 25, Maven 3.9.14 (wrapper), JUnit 5, Maven Enforcer, ArchUnit, existing Google Cloud / Jackson deps **only** on legacy module, SHA-256 evidence scripts.

**Spec (normative for this plan):** [docs/implementation/phases/phase-00-build-architecture-skeleton.md](../../implementation/phases/phase-00-build-architecture-skeleton.md) (2026-08-01 semantic rebase)  
**Also read:** [docs/architecture-design.md](../../architecture-design.md) §4.1–§4.6 · [docs/implementation/master-realization-plan.md](../../implementation/master-realization-plan.md) §4.1 + Phase 00 · [docs/implementation/reviews/phase-00-review.md](../../implementation/reviews/phase-00-review.md)

**Out of scope (do not implement):** Domain types (`CanonicalInput`, etc.), ALNS, verifiers, S3/LocalStack adapters, Step Functions, Lambda/ECS modules, OR-Tools/`backends/*`, public API, win_poc run, Phase status ACCEPTED promotion without sealed evidence + independent review.

---

## Preconditions (stop if any fail)

Do **not** start Task 1 code moves until all of the following are true:

| # | Gate | How to satisfy |
|---|---|---|
| P1 | Implementation authorization | Real scheduler/implementation task ID + owner/reviewer (do **not** invent IDs). Record in progress/plan progress notes. |
| P2 | Working from rebased Phase 00 body | Use current `phase-00-build-architecture-skeleton.md` (not Final/capabilities tree). Review may still be `REBASE_PENDING_REREVIEW`. |
| P3 | Legacy Jackson/Shade policy written | One short ADR under `docs/superpowers/plans/adr/` or `docs/implementation/decisions/` with: target modules = strict convergence; legacy = narrow allowlist only; no silent version alignment; no global Enforcer skip. Owner sign-off. |
| P4 | Build pins chosen | Exact versions for ArchUnit + any new lifecycle plugins; exact `project.build.outputTimestamp` value or derivation rule. Record in Task 0. |
| P5 | Clean scope | `git status --short` — do not clobber unrelated user work. Prefer dedicated branch/worktree. |
| P6 | Fresh inventory | Re-run inventory commands (Task 1); do not trust 2026-07-28 historical fingerprints alone. |

**Proposed default pins (Build owner must confirm before POM commit):**

| Item | Proposed pin |
|---|---|
| Maven Wrapper | 3.9.14 + official distribution SHA-256 from Apache release |
| `maven.compiler.release` | 25 |
| Enforcer / compiler / Surefire / Shade | 3.6.1 / 3.14.1 / 3.5.4 / 3.6.1 (current root) |
| ArchUnit | `com.tngtech.archunit:archunit-junit5:1.4.1` (confirm latest compatible at implement time) |
| Target Enforcer | `dependencyConvergence` **and** upper-bound deps policy (no silent newer transitive wins) |
| `project.build.outputTimestamp` | Fixed ISO instant of first implementation commit, e.g. `2026-08-01T00:00:00Z` (ADR-required) |
| JUnit | 5.13.1 (current) |

---

## File structure (create / move map)

```text
ro-next/
├── pom.xml                          # packaging=pom; no Google/Jackson/business deps
├── mvnw, mvnw.cmd, .mvn/wrapper/**
├── .mvn/toolchains.example.xml
├── build/
│   ├── pom.xml
│   ├── test-fixtures/               # test-jar classifier=tests
│   ├── architecture-rules/          # ArchUnit + negative fixtures
│   ├── verify-reproducible-build.sh
│   ├── verify-evidence-bundle.sh
│   ├── verify-bytecode-boundaries.sh   # required (jdeps / transitive provider check)
│   └── verify-negative-architecture.sh
├── rpdptw/
│   ├── pom.xml
│   ├── core/                        # package-info only (no domain classes)
│   ├── solver/
│   ├── verification/
│   ├── application/
│   └── profiles/
│       ├── pom.xml                  # optional aggregator
│       └── standard/                # package-info only
├── legacy/
│   ├── pom.xml
│   └── gcp-placeholder/             # moved from src/** + Google/Jackson + shade
├── src/                             # REMOVE after move (empty or deleted)
├── gcp/**                           # preserve; minimal path-only edits
├── Dockerfile                       # legacy module path only
└── target/phase-00-evidence/        # gitignored evidence output
```

**Coordinates (proposed):**

| Module path | `groupId` | `artifactId` |
|---|---|---|
| root | `com.ronext` | `ro-next` |
| `rpdptw/core` | `com.ronext.rpdptw` | `rpdptw-core` |
| `rpdptw/solver` | `com.ronext.rpdptw` | `rpdptw-solver` |
| `rpdptw/verification` | `com.ronext.rpdptw` | `rpdptw-verification` |
| `rpdptw/application` | `com.ronext.rpdptw` | `rpdptw-application` |
| `rpdptw/profiles/standard` | `com.ronext.rpdptw` | `rpdptw-profiles-standard` |
| `build/test-fixtures` | `com.ronext.rpdptw` | `rpdptw-test-fixtures` |
| `build/architecture-rules` | `com.ronext.rpdptw` | `rpdptw-architecture-rules` |
| `legacy/gcp-placeholder` | `com.ronext.legacy` | `legacy-gcp-placeholder` |

**Forbidden this phase:** `rpdptw-capabilities`, `rpdptw-profile-catalog`, `adapters/*`, `backends/*`, `apps/*`, `compute-aws-lambda`, `adapters/object-s3`, Domain production classes, OR-Tools deps.

---

## Task 0: Record gates and pins

**Files:**
- Create: `docs/superpowers/plans/adr/2026-08-01-phase-00-build-pins-and-legacy-policy.md` (or repo’s decisions folder if preferred)
- Modify: none of production code yet

- [ ] **Step 1: Confirm authorization**

Record real task ID, implementer, reviewer in the ADR header. If missing, **stop** and ask the user.

- [ ] **Step 2: Write legacy dependency policy ADR**

Must state:

1. Target modules: Enforcer `dependencyConvergence` + banned Google/AWS/Azure/OR-Tools/JDBC/Redis groups.
2. Legacy module: may keep current Google Storage / Workflow / Jackson coordinates as inventory; **no** silent upgrade to “fix” Jackson without golden re-proof.
3. Shade collisions: inventory + selected service resources golden; no broad exclusion without review.
4. No `enforcer.skip=true` on root default lifecycle.

- [ ] **Step 3: Write build pin table**

Copy the proposed pin table above; replace any version after Build owner check. Include `outputTimestamp` exact value.

- [ ] **Step 4: Commit ADR only**

```bash
git add docs/superpowers/plans/adr/2026-08-01-phase-00-build-pins-and-legacy-policy.md
git commit -m "docs: pin Phase 00 build and legacy dependency policy"
```

---

## Task 1: WP-00-0 — Baseline inventory (read-only + evidence scaffold)

**Files:**
- Create: `target/phase-00-evidence/inventory/` (gitignored) or `docs/implementation/evidence/phase-00/inventory/` if team prefers tracked empty `.gitkeep` only
- Create: `.gitignore` entries for `target/phase-00-m2/`, `target/phase-00-evidence/` if missing

- [ ] **Step 1: Capture identity**

```bash
git rev-parse --abbrev-ref HEAD
git rev-parse HEAD
git status --short
java -version
mvn -version
```

Expected: Java 25.x, Maven ≥ 3.9.14 (or note gap). Save stdout to `…/inventory/toolchain.txt`.

- [ ] **Step 2: Fingerprint tracked sources and authority docs**

```bash
git ls-files -z | xargs -0 shasum -a 256 | sort > target/phase-00-evidence/inventory/tracked-sha256.txt
shasum -a 256 pom.xml Dockerfile .sdkmanrc 2>/dev/null | tee target/phase-00-evidence/inventory/key-files-sha256.txt
find src/main/java src/test/java gcp -type f 2>/dev/null | sort | xargs shasum -a 256 > target/phase-00-evidence/inventory/legacy-src-sha256.txt
# Spec §1.1 authority set (compare to phase-00 frontmatter fingerprints)
shasum -a 256 \
  docs/deprecated/2026-07-30-design-interview-phase-a.md \
  docs/master-design.md \
  docs/domain-design.md \
  docs/architecture-design.md \
  docs/implementation/master-realization-plan.md \
  docs/implementation/README.md \
  docs/implementation/phases/phase-00-build-architecture-skeleton.md \
  | tee target/phase-00-evidence/inventory/authority-sha256.txt
```

- [ ] **Step 2b: Note abandoned path debris (if any)**

If empty or partial `adapters/`, `apps/`, `rpdptw/capabilities`, `rpdptw/profile-catalog` dirs exist from local experiments, record them in inventory and **do not** treat as target modules. Remove only if authorized and empty of needed work; otherwise leave and ensure reactor `modules` list does not include them.

- [ ] **Step 3: Dependency / shade inventory (current single module)**

```bash
mvn -B -ntp -Dstyle.color=never dependency:tree -Dverbose > target/phase-00-evidence/inventory/dependency-tree-verbose.txt
mvn -B -ntp -Dstyle.color=never verify 2>&1 | tee target/phase-00-evidence/inventory/verify-baseline.txt
```

Expected: verify may pass with 1 test; capture Jackson version conflicts and Shade warnings into `…/inventory/jackson-shade-notes.md` (manual extract from logs).

- [ ] **Step 4: Negative source scans (document absence of target tree)**

```bash
rg -n 'com\.google\.ortools|route-selection-ortools-cpsat' pom.xml src || true
test ! -d rpdptw && test ! -d adapters && test ! -d backends
```

- [ ] **Step 5: Commit inventory notes if tracked; otherwise commit only .gitignore**

```bash
# If inventory is gitignored, commit only policy/gitignore changes
git add .gitignore
git commit -m "chore: ignore Phase 00 evidence and local m2 cache paths"
```

---

## Task 2: WP-00-1a — Move legacy sources into module (no behavior change)

**Files:**
- Create: `legacy/pom.xml`, `legacy/gcp-placeholder/pom.xml`
- Move: `src/main/java/com/ronext/optimizer/**` → `legacy/gcp-placeholder/src/main/java/com/ronext/optimizer/**`
- Move: `src/test/java/com/ronext/optimizer/**` → `legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/**`
- Modify: root `pom.xml` temporarily still builds until Task 3 parent split (or convert root to aggregator in same change set if preferred)
- Modify: `Dockerfile` module path after move

- [ ] **Step 1: Create legacy module POMs with current deps**

`legacy/gcp-placeholder/pom.xml` must include current Google Workflow, GCS, Jackson, JUnit, Shade main class `com.ronext.optimizer.adapter.in.http.OptimizationHttpServer` (verify main class name in existing Shade config / `OptimizationHttpServer`).

Root either:
- still points compiler at moved paths, **or**
- becomes aggregator with `<module>legacy/gcp-placeholder</module>` only (minimal green).

Prefer: root `packaging=pom` + single module legacy first (Task 3 expands).

- [ ] **Step 2: `git mv` sources**

```bash
mkdir -p legacy/gcp-placeholder/src/main/java legacy/gcp-placeholder/src/test/java
git mv src/main/java/com/ronext legacy/gcp-placeholder/src/main/java/com/ronext
git mv src/test/java/com/ronext legacy/gcp-placeholder/src/test/java/com/ronext
```

- [ ] **Step 2b: Retarget Dockerfile (path only)**

Update `Dockerfile` build/copy paths to `legacy/gcp-placeholder` (or reactor module coordinates). **No** semantic image rename to AWS/target. Touch `gcp/**` only if a path reference breaks; no workflow meaning change.

- [ ] **Step 3: Run legacy tests**

```bash
mvn -B -ntp -pl legacy/gcp-placeholder -am test
```

Expected: existing `AlnsBatchEngineTest` PASS (same as baseline).

- [ ] **Step 4: Commit**

```bash
git add legacy pom.xml Dockerfile
git commit -m "refactor: move GCP placeholder into legacy/gcp-placeholder module"
```

---

## Task 3: WP-00-1b — Legacy characterization tests (TDD)

**Files:**
- Create: `legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/application/AlnsBatchEngineCharacterizationTest.java`
- Create: `legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/adapter/in/http/LegacyOptimizationContractCharacterizationTest.java`
- Create: `legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/adapter/in/http/LegacyWorkflowCharacterizationTest.java`
- Create: `legacy/gcp-placeholder/src/test/java/com/ronext/optimizer/adapter/in/http/LegacyShadedArtifactCharacterizationTest.java`
- Modify: production code **only** if package-private test seams required (no HTTP contract / storage key / default change)

**Minimum method coverage (spec WP-00-1 — all required):**

| Class | Methods (exact intent) |
|---|---|
| `AlnsBatchEngineCharacterizationTest` | `sameSeedRunAndIterationsProduceSameSyntheticCandidate`, `inputUriBytesAreNotReadByCurrentPlaceholder` |
| `LegacyOptimizationContractCharacterizationTest` | `exposesCurrentPublicAndInternalPaths`, `appliesCurrentParallelRunIterationAndSeedDefaults`, `storesCandidatesAndResultAtCurrentObjectKeys`, `finalizesFromVisiblePrefixAndRawMinimumObjective`, `returnsCurrentNotFoundMethodAndValidationErrors`, `returnsRunningWhenResultObjectIsMissing`, `returnsCurrentRedactedFailureForStorageWorkflowAndEmptyCandidateFailures` |
| `LegacyWorkflowCharacterizationTest` | `dispatchesDeclaredParallelRangeThenFinalize`, `derivesCurrentWorkerSeedFromBaseSeedAndRunNumber` |
| `LegacyShadedArtifactCharacterizationTest` | `preservesMainClassAndSelectedServiceResources`, `reportsEveryBaselineDependencyConflictAndShadeCollision` |

- [ ] **Step 1: Write failing characterization for synthetic engine**

Read `AlnsBatchEngine.java` and existing `AlnsBatchEngineTest`. Assert concrete values from one recorded run (placeholder behavior, not target ALNS).

```java
package com.ronext.optimizer.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class AlnsBatchEngineCharacterizationTest {

    @Test
    void sameSeedRunAndIterationsProduceSameSyntheticCandidate() {
        // Call current AlnsBatchEngine API exactly as production does.
        // Assert equal objective for identical seed/run/iterations.
    }

    @Test
    void inputUriBytesAreNotReadByCurrentPlaceholder() {
        // Prove engine ignores input URI/bytes if that is current behavior.
    }
}
```

- [ ] **Step 2: Run engine characterization**

```bash
mvn -B -ntp -pl legacy/gcp-placeholder -Dtest=AlnsBatchEngineCharacterizationTest test
```

Expected: iterate until green against **current** behavior.

- [ ] **Step 3: HTTP contract characterization (full golden, not status-only)**

With deterministic fakes (**no GCP credentials**), lock **all** of:

1. Public/internal paths currently exposed (`/optimizations`, `/internal/batches`, `/internal/finalize`, etc. as coded).
2. Current defaults/clamps for parallel run, iteration, seed.
3. Current object key patterns for candidates and results.
4. Finalize selects from **visible prefix listing** + **raw minimum objective** (document as known mismatch vs target).
5. Status/body: validation **400**, path **404**, method **405**.
6. Missing result object → current **RUNNING** / 202-style behavior as implemented.
7. Empty candidates / storage / workflow client failure → current **500** + **redacted** body.

Implement as the seven methods in the table above (one test class is fine).

- [ ] **Step 4: Workflow characterization**

Read `gcp/workflows/optimization.yaml` and worker controller seed derivation. Implement:

- `dispatchesDeclaredParallelRangeThenFinalize` — parallel range then finalize order matches current workflow/controller contract (fake HTTP or pure function on dispatch plan).
- `derivesCurrentWorkerSeedFromBaseSeedAndRunNumber` — exact current seed formula.

- [ ] **Step 5: Shaded artifact characterization**

```bash
mvn -B -ntp -pl legacy/gcp-placeholder -am package
```

`LegacyShadedArtifactCharacterizationTest`: Main-Class manifest, selected `META-INF/services` resources, and inventory of every baseline dependency conflict + Shade collision (assert against Task 1 golden list; **no silent exclusion**).

- [ ] **Step 6: Commit**

```bash
git add legacy/gcp-placeholder/src/test
git commit -m "test: characterize legacy GCP placeholder contracts"
```

---

## Task 4: WP-00-2a — Maven Wrapper + parent POM without business deps

**Files:**
- Create: `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.mvn/wrapper/maven-wrapper.jar` (or jar-less wrapper per official 3.9.14 install)
- Create: `.mvn/toolchains.example.xml`
- Modify: root `pom.xml` → `packaging=pom`, modules: `legacy`, `build` (stubs ok), later `rpdptw`
- Create: `legacy/pom.xml` aggregator if not present
- Create: `build/pom.xml` aggregator (empty modules list ok until Task 5–6)

- [ ] **Step 1: Install wrapper with official checksum**

```bash
# Use Apache Maven 3.9.14 distribution; record SHA-256 from official source into
# .mvn/wrapper/maven-wrapper.properties — do not invent checksums.
mvn -N wrapper:wrapper -Dmaven=3.9.14
# Verify properties pin distributionUrl + distributionSha256Sum
```

- [ ] **Step 2: Strip business deps from root**

Root `pom.xml` must have **zero** `dependencies` for Google/Jackson/business. Move all to `legacy/gcp-placeholder/pom.xml`. Root keeps `pluginManagement` + `dependencyManagement` for JUnit/plugin versions only.

- [ ] **Step 3: Enforcer on root**

Keep `requireJavaVersion [25,26)`, `requireMavenVersion [3.9.14,)`, add `requirePluginVersions`, and for **target** modules (later) `dependencyConvergence`. Legacy exceptions only per Task 0 ADR.

- [ ] **Step 4: Pin Maven Toolchains plugin (WP-00-2)**

In root `pluginManagement` / `plugins`, add **exact-version** `maven-toolchains-plugin` (pin in Task 0 ADR; do not use version ranges). Configure compiler and Surefire to use toolchain JDK matching version `[25,26)`. Vendor is not fixed.

Create path-free `.mvn/toolchains.example.xml` only (no `jdkHome` secrets, no user home paths). Document machine-local toolchains file setup in ADR.

- [ ] **Step 5: Set `project.build.outputTimestamp`**

```xml
<project.build.outputTimestamp>2026-08-01T00:00:00Z</project.build.outputTimestamp>
```

(Use ADR value.)

- [ ] **Step 6: Validate with wrapper + toolchains display**

```bash
./mvnw --version
./mvnw -B -ntp toolchains:display-discovered-jdk-toolchains
./mvnw -B -ntp -Dstyle.color=never -pl legacy/gcp-placeholder -am verify
```

Expected: BUILD SUCCESS; Maven 3.9.14; toolchain display shows a JDK in `[25,26)` (or document how local toolchains file supplies it).

- [ ] **Step 7: Commit**

```bash
git add pom.xml mvnw mvnw.cmd .mvn legacy build
git commit -m "build: add Maven wrapper, toolchains pin, and dependency-free parent POM"
```

---

## Task 5: WP-00-2b — Offline warm-up and reproducible script

**Files:**
- Create: `build/verify-reproducible-build.sh` (executable)
- Create: `build/verify-evidence-bundle.sh` (executable)

- [ ] **Step 1: Offline verify script path**

```bash
./mvnw -B -ntp -Dstyle.color=never -Dmaven.repo.local=target/phase-00-m2 dependency:go-offline
./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 -pl legacy/gcp-placeholder -am verify
```

Expected: SUCCESS offline after warm-up.

- [ ] **Step 2: Implement `verify-reproducible-build.sh`**

Behavior required by spec:

1. Refuse dirty/missing source manifest vs recorded implementation commit or content-addressed archive.
2. Build twice in temp workspaces with same `outputTimestamp`.
3. Compare sorted publishable JAR SHA-256 lists; non-zero on mismatch.

Minimal skeleton:

```bash
#!/usr/bin/env bash
set -euo pipefail
# 1) git rev-parse HEAD + git status --porcelain must match sealed snapshot policy
# 2) two temp dirs, copy source, run ./mvnw -B -ntp -Dstyle.color=never package
# 3) find jars | sort | xargs shasum -a 256 | diff
```

- [ ] **Step 3: Implement `verify-evidence-bundle.sh`**

Canonical relative paths, byte length, SHA-256; reject symlinks, missing files, path collision, one-byte mutation.

- [ ] **Step 4: Commit**

```bash
git add build/verify-reproducible-build.sh build/verify-evidence-bundle.sh
git commit -m "build: add Phase 00 reproducibility and evidence seal scripts"
```

---

## Task 6: WP-00-3 — Target module skeleton (`rpdptw/*`)

**Files:**
- Create: `rpdptw/pom.xml`
- Create: `rpdptw/core/pom.xml` + package-info files under `com.ronext.rpdptw.{input,domain,normalization,travel,propagation,evaluation.api,evaluation.runtime,evaluation.insertion}`
- Create: `rpdptw/solver/pom.xml` + package-info under `…solver.{portfolio,search,state,termination}`
- Create: `rpdptw/verification/pom.xml` + package-info under `…verification.{api,candidate,result}` and `…result.{api,finalization}`
- Create: `rpdptw/application/pom.xml` + package-info under `…application.{port.in,port.out,service,execution}`
- Create: `rpdptw/profiles/pom.xml` (optional aggregator)
- Create: `rpdptw/profiles/standard/pom.xml` + `…profiles.standard/package-info.java`
- Create: `build/test-fixtures/pom.xml` + empty `src/test/java/com/ronext/rpdptw/fixture/package-info.java` (or single marker test class only if required for jar)
- Modify: root `pom.xml` modules list

**package-info.java pattern (every package):**

```java
/**
 * Owner: rpdptw-core.
 * Allowed compile deps: none (core) | core only (profiles) | …
 * Forbidden: cloud SDKs, OR-Tools, solver types (if verification), Domain production types in Phase 00.
 */
package com.ronext.rpdptw.input;
```

- [ ] **Step 1: Add modules with compile graph**

| Module | Dependencies |
|---|---|
| core | (none) |
| solver | core |
| verification | core only |
| profiles-standard | core only |
| application | core, solver, verification |
| test-fixtures | core (`test` packaging: attach `tests` classifier) |

- [ ] **Step 2: Configure test-fixtures test-jar**

In `build/test-fixtures/pom.xml`:

```xml
<plugin>
  <artifactId>maven-jar-plugin</artifactId>
  <executions>
    <execution>
      <id>test-jar</id>
      <goals><goal>test-jar</goal></goals>
    </execution>
  </executions>
</plugin>
```

Document consumer coordinates:

```xml
<dependency>
  <groupId>com.ronext.rpdptw</groupId>
  <artifactId>rpdptw-test-fixtures</artifactId>
  <version>${project.version}</version>
  <type>test-jar</type>
  <classifier>tests</classifier>
  <scope>test</scope>
</dependency>
```

- [ ] **Step 3: Verify skeleton**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl rpdptw/core,rpdptw/solver,rpdptw/verification,rpdptw/application,rpdptw/profiles/standard -am verify
./mvnw -B -ntp -Dstyle.color=never -pl build/test-fixtures -am verify
./mvnw -B -ntp -Dstyle.color=never dependency:tree -pl rpdptw/verification
```

Expected: SUCCESS; verification tree has **no** solver/legacy/Google artifacts.

- [ ] **Step 4: Assert no forbidden modules**

```bash
test ! -d rpdptw/capabilities
test ! -d adapters
test ! -d backends
test ! -d apps
rg -n 'CanonicalInput|ProblemInstance|PreparedTravel' rpdptw && exit 1 || true
```

- [ ] **Step 5: Commit**

```bash
git add rpdptw build/test-fixtures pom.xml
git commit -m "feat: add rpdptw multi-module skeleton and test-fixtures"
```

---

## Task 7: WP-00-4a — Enforcer bans on target modules

**Files:**
- Modify: `rpdptw/pom.xml` (or root `pluginManagement` + rpdptw parent) Enforcer rules
- Create: `build/architecture-rules/pom.xml` (test-only module depending on all stable jars with `test` scope)

- [ ] **Step 1: Banned dependencies rule**

Ban on target modules (not legacy):

- `com.google.cloud:*`, `com.google.ortools:*`
- `software.amazon.awssdk:*`, `com.amazonaws:*`
- `com.azure:*`, `io.kubernetes:*`
- JDBC/Redis groups as applicable (`org.springframework.boot:*-data-jpa`, `redis.clients:jedis`, `org.redisson`, etc. — match Architecture §4.6)

- [ ] **Step 2: Fail a deliberate bad dependency (negative control)**

Temporarily add banned dep to `rpdptw-core` in a branch experiment, run verify, expect FAIL; remove. Document in evidence notes. Prefer a permanent negative fixture module under `build/architecture-rules` that is **not** part of default reactor success path, or ArchUnit self-check (Task 8).

- [ ] **Step 3: Root verify still green**

```bash
./mvnw -B -ntp -Dstyle.color=never verify
```

Expected: SUCCESS; legacy still builds; targets clean.

- [ ] **Step 4: Commit**

```bash
git add pom.xml rpdptw build
git commit -m "build: enforce banned dependencies on rpdptw modules"
```

---

## Task 8: WP-00-4b — ArchUnit + source policy rules (TDD)

**Files:**
- Create: `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/StableModuleDependencyArchitectureTest.java`
- Create: `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/ProviderAndVendorIsolationArchitectureTest.java`
- Create: `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/PackageBoundaryArchitectureTest.java`
- Create: `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/CustomerIsolationArchitectureTest.java`
- Create: `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/CorePuritySourceScanTest.java`
- Create: `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/TestScopeLeakageArchitectureTest.java`
- Create: `build/architecture-rules/src/test/java/com/ronext/rpdptw/architecture/RouteSelectionAbsenceTest.java`
- Create: `build/architecture-rules/src/test/resources/customer-identity-allowlist.txt` (approved tokens; start empty or `standard` only)
- Create: `build/verify-bytecode-boundaries.sh` (jdeps or equivalent; required for transitive provider check)

**Dependency:** architecture-rules `pom.xml` uses ArchUnit JUnit5 (pinned version from Task 0) and `test` dependencies on all `rpdptw-*` jars. Analyze **`com.ronext.rpdptw` only** (never treat legacy as target success).

- [ ] **Step 1: Stable module dependency rules**

```java
@AnalyzeClasses(packages = "com.ronext.rpdptw")
class StableModuleDependencyArchitectureTest {

    @ArchTest
    static final ArchRule verification_does_not_depend_on_solver =
        noClasses()
            .that().resideInAPackage("com.ronext.rpdptw.verification..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.ronext.rpdptw.solver..");

    @ArchTest
    static final ArchRule profiles_do_not_depend_on_solver_app_verification_legacy =
        noClasses()
            .that().resideInAPackage("com.ronext.rpdptw.profiles..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.ronext.rpdptw.solver..",
                "com.ronext.rpdptw.application..",
                "com.ronext.rpdptw.verification..",
                "com.ronext.optimizer.."
            );

    // Also: core has no dependency on other rpdptw modules (package-level as applicable)
}
```

- [ ] **Step 2: Provider / vendor isolation**

Rules: no classes in `com.ronext.rpdptw..` depend on `com.google..`, `software.amazon..`, `com.azure..`, `com.google.ortools..`, `com.ronext.optimizer..`.

- [ ] **Step 3: Package boundary (`.internal`)**

No class outside module X accesses `..internal..` packages of another module (ArchUnit package rule + naming convention documented in package-info).

- [ ] **Step 4: Customer isolation (required — not a stub)**

`CustomerIsolationArchitectureTest` must:

1. Load `customer-identity-allowlist.txt` (approved tokens only).
2. Fail if generic modules (`core`, `solver`, `verification`, `application`) contain packages matching customer-specific segments outside allowlist.
3. Fail on simple customer-name **conditional** patterns in those packages (source scan of production roots under `rpdptw/core|solver|verification|application` for non-allowlisted identity tokens in `if`/`switch`/string equals — document oracle limits; no claim of full semantic completeness).
4. Record coverage limits in evidence (static check ≠ synonym proof).

- [ ] **Step 5: Core purity source scan (spec §3.2 #11)**

`CorePuritySourceScanTest` (JUnit reading files under `rpdptw/core/src/main/java`) fails if production sources match:

- `System.getenv`, `System.getProperty` (unless explicitly none expected),
- `System.currentTimeMillis`, `Instant.now`, `Clock.system`, `LocalDate.now` without injected Clock,
- `new Random(` / `ThreadLocalRandom`,
- mutable `static` fields that act as registries (heuristic: non-final static collections/maps).

With package-info-only core this passes; the **rule must still run** on every verify for Phase 01 handoff.

- [ ] **Step 6: Route-selection / C-17 absence**

`RouteSelectionAbsenceTest`: reactor has no `backends` module; no class/resource advertising route-selection capability; `rg` for `route-selection-ortools-cpsat` and `com.google.ortools` on `rpdptw` + root/target poms is empty.

- [ ] **Step 7: Test-scope leakage**

`TestScopeLeakageArchitectureTest` + Enforcer: fixture artifact not on production compile/runtime of application/core/solver/verification.

- [ ] **Step 8: Bytecode boundaries script**

```bash
./build/verify-bytecode-boundaries.sh
# jdeps on rpdptw-* jars; fail if forbidden package names appear transitively
```

- [ ] **Step 9: Run and wire to root verify**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules -am test
./mvnw -B -ntp -Dstyle.color=never verify
```

Skipped architecture tests must fail the build (`failIfNoTests`, no default skip property).

- [ ] **Step 10: Commit**

```bash
git add build/architecture-rules build/verify-bytecode-boundaries.sh pom.xml rpdptw
git commit -m "test: add ArchUnit, customer, core-purity, and bytecode gates"
```

---

## Task 9: WP-00-4c — Negative fixtures prove **each** rule fires

**Default mechanism (required):** Maven profile `negative-arch`.

**Files:**
- Create: `build/architecture-rules/src/negative-fixtures/java/**` (not on main classpath)
- Modify: `build/architecture-rules/pom.xml` with profile `negative-arch` that:
  1. Adds negative-fixtures as sources,
  2. Runs a dedicated fail-expect script or Surefire config,
  3. **Is not** part of default `verify` success path,
  4. Is invoked explicitly and **must exit non-zero** when fixtures are enabled without the “expect failure” wrapper.

**Wrapper script:** `build/verify-negative-architecture.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail
# Runs ./mvnw -Pnegative-arch … and asserts BUILD FAILURE (grep FAILURE / exit != 0).
# Exit 0 from this wrapper means "rules correctly rejected bad fixtures".
```

**Minimum negative fixtures (each must be proven to fail the matching rule):**

| Fixture intent | Rule under test |
|---|---|
| Class in `verification` imports `solver` type | verification ↛ solver |
| Class in `core` references `software.amazon.awssdk` or `com.google.cloud` (compile-only stub interface name in forbidden package via crafted dependency **or** ArchUnit analyzed synthetic package) | cloud SDK ban |
| Class outside `.internal` accessing other module `.internal` | internal boundary |
| Customer token package `…profiles.acmeevil…` or conditional string not on allowlist in `core` | customer isolation |
| Production module depends on test-fixtures main jar / wrong scope | test leakage |
| Class or resource named route-selection advertisement under `rpdptw` | C-17 absence |
| Synthetic core source calling `System.getenv` under negative-arch | Core purity source scan |

Implementation note: if full compile against real AWS SDK is too heavy, use ArchUnit `ImportOption` + intentionally wrong dependency edges between test helper modules that **are** on the negative profile classpath. Document the exact technique in ADR.

- [ ] **Step 1: Implement profile + first fixture (verification→solver) and wrapper**

```bash
./build/verify-negative-architecture.sh
```

Expected: wrapper exit 0 (inner build failed as required).

- [ ] **Step 2: Add remaining fixtures from table (one commit or one per fixture)**

Each fixture must flip from “missing detection” to “detected” before proceeding.

- [ ] **Step 3: Confirm default `./mvnw verify` still SUCCESS without negative profile**

- [ ] **Step 4: Commit**

```bash
git add build/architecture-rules build/verify-negative-architecture.sh
git commit -m "test: negative-arch profile proves architecture rules fail closed"
```

---

## Task 10: WP-00-5 — Evidence bundles and Phase 01 handoff manifest

**Files:**
- Create: `target/phase-00-evidence/E-P00-BUILD/`, `E-P00-ARCH/`, `E-P00-LEGACY/` (gitignored) + optional tracked `docs/implementation/evidence/phase-00/README.md` describing how to reproduce
- Create: `docs/implementation/evidence/phase-00/Phase01SkeletonHandoff.md` (tracked summary, no secrets)

Each evidence bundle directory must include a `MANIFEST.md` with at least:

- implementation commit SHA + dirty-state note
- toolchain fingerprint (java -version, ./mvnw -version, toolchains display)
- exact commands + exit codes
- last safe rollback point (prior green commit)
- OPEN/GATED/DEFERRED list still preserved (C-17, O1, Q-BENCH-02, …)
- what is **not** claimed (no ACCEPTED, no win_poc)

- [ ] **Step 1: Collect BUILD evidence**

```bash
./mvnw -B -ntp -Dstyle.color=never verify
./mvnw -B -ntp toolchains:display-discovered-jdk-toolchains
./mvnw -B -ntp -Dstyle.color=never -Dmaven.repo.local=target/phase-00-m2 dependency:go-offline
./mvnw -B -ntp -Dstyle.color=never -o -Dmaven.repo.local=target/phase-00-m2 verify
./build/verify-reproducible-build.sh
```

Copy logs + dependency trees + wrapper properties into `E-P00-BUILD/`.

- [ ] **Step 2: Collect ARCH evidence**

```bash
./mvnw -B -ntp -Dstyle.color=never -pl build/architecture-rules -am test
./build/verify-bytecode-boundaries.sh
./build/verify-negative-architecture.sh
./mvnw -B -ntp dependency:tree -pl rpdptw/core,rpdptw/solver,rpdptw/verification,rpdptw/application,rpdptw/profiles/standard
```

Include customer allowlist + coverage limits note.

- [ ] **Step 3: Collect LEGACY evidence**

All four characterization test classes’ Surefire reports + jackson/shade inventory + “legacy ≠ target / known mismatches (prefix listing, raw objective)”.

- [ ] **Step 4: Seal with `verify-evidence-bundle.sh`**

```bash
./build/verify-evidence-bundle.sh target/phase-00-evidence
```

Expected: exit 0; manifest lists every file; mutation of one byte in a sealed file is rejected on re-run.

- [ ] **Step 5: Write handoff manifest**

`Phase01SkeletonHandoff.md` must state:

- `rpdptw-core` is only semantic production start for Phase 01
- No `CanonicalInput` types created
- Test-fixtures consumer coordinates
- Open items still OPEN (C-17, O1, Q-BENCH-02)
- Implementation not ACCEPTED until independent review of evidence

- [ ] **Step 6: Commit tracked docs only**

```bash
git add docs/implementation/evidence/phase-00/Phase01SkeletonHandoff.md
git commit -m "docs: Phase 00 skeleton handoff notes for Phase 01"
```

Do **not** claim `phase_acceptance_status: ACCEPTED` in Phase 00 markdown without reviewer receipt.

---

## Task 11: Exit checklist (human + reviewer)

- [ ] **Step 1: Spec alignment self-check**

| Spec MUST | Verified? |
|---|---|
| Tree = §4.2 profiles, not capabilities | |
| verification ↛ solver | |
| No adapters/backends/apps empty modules | |
| No Lambda-only module name | |
| No Domain placeholder classes | |
| No OR-Tools / C-17 packages | |
| Default verify cloud/OR-Tools-free | |
| Legacy characterized, isolated | |
| Online + offline verify | |
| Evidence sealed | |

- [ ] **Step 2: Request independent exit review**

Reviewer uses evidence digests + this plan completion state. Only then may progress mark Phase 00 implementation toward `ACCEPTED`.

- [ ] **Step 3: Do not start Phase 01 implementation** until Phase 00 is `ACCEPTED` per plan gates (Phase 01 **document** rebase may proceed in parallel).

---

## Commit strategy summary

| Task | Commit theme |
|---|---|
| 0 | docs: policy/pins |
| 1 | chore: evidence ignore / inventory hygiene |
| 2 | refactor: legacy module move |
| 3 | test: legacy characterization |
| 4 | build: wrapper + parent |
| 5 | build: repro/evidence scripts |
| 6 | feat: rpdptw skeleton |
| 7 | build: enforcer bans |
| 8 | test: ArchUnit |
| 9 | test: negative architecture proofs |
| 10 | docs: handoff |

Prefer small commits; never `git reset --hard` user work.

---

## Risk register

| Risk | Mitigation |
|---|---|
| Jackson/Shade “fix” silently changes legacy | Task 0 ADR + characterization goldens |
| Empty JAR / no package-info on some JDKs | Keep package-info; avoid empty marker domain classes |
| ArchUnit analyzes legacy via wrong packages | `@AnalyzeClasses(packages = "com.ronext.rpdptw")` only |
| Test-fixtures leak to main | Enforcer + TestScopeLeakageArchitectureTest |
| Over-building adapters/apps | Explicit Task 6 negative asserts |
| Claiming ACCEPTED without evidence | Task 11 gate |

---

## References for implementers

- Spec WP mapping: Phase 00 § WP-00-0 … WP-00-5  
- Architecture tree: `docs/architecture-design.md` §4.2  
- Plan authority overlay: `docs/implementation/master-realization-plan.md` Phase 00  
- Execution skills: `@superpowers:subagent-driven-development` or `@superpowers:executing-plans`  
- Verification skill before claiming done: `@superpowers:verification-before-completion`
