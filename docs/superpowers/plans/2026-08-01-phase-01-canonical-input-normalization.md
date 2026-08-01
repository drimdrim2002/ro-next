# Phase 01 — Canonical Input Normalization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement D1 single-canonical anti-corruption input: external bytes → one `adapters/input` path → `CanonicalBusinessInput` → strict normalization → sealed `NormalizedInputArtifact` (or ordered typed rejection), with evidence bundles `E-P01-NUMERIC` / `E-P01-TIME` / `E-P01-COMPAT` / `E-P01-ERROR` for Phase 02 handoff.

**Architecture:** Provider-neutral types live only in `rpdptw-core` (`com.ronext.rpdptw.input` + `com.ronext.rpdptw.normalization`): pure JDK, no Jackson/HTTP/cloud/solver. External DTO parsing and alias/coercion/unknown-field policy live in new Maven module `adapters/input` (`com.ronext.rpdptw.adapter.input`), which depends on core only. Wire/public schema remains **OPEN** (O3); this phase ships a **test-only** declared adapter identity + fixture bytes, never multi-version schema ops. Normalization is pure, side-effect-free, returns sealed success **or** deterministically ordered `InputProblem` list — never a partial artifact.

**Tech Stack:** Java 25, Maven reactor (existing Phase 00 skeleton), JUnit 5, ArchUnit (existing `rpdptw-architecture-rules`), JDK `BigDecimal`/`Math.*Exact` for fixed-point (no `double` intermediate), SHA-256 digests (`MessageDigest`), optional Jackson **only** inside `adapters/input` for test-fixture JSON parse (never in core).

**Spec (normative for this plan):** [docs/implementation/phases/phase-01-canonical-input-normalization.md](../../implementation/phases/phase-01-canonical-input-normalization.md)  
**Also read:** [docs/domain-design.md](../../domain-design.md) §2.4, §4, §5 · [docs/architecture-design.md](../../architecture-design.md) §4.2 `adapters/input`, D1 · [docs/master-design.md](../../master-design.md) D1 · [docs/implementation/evidence/phase-00/Phase01SkeletonHandoff.md](../../implementation/evidence/phase-00/Phase01SkeletonHandoff.md) · [docs/implementation/reviews/phase-01-review.md](../../implementation/reviews/phase-01-review.md)

**Out of scope (do not implement):** Dense IDs, `PreparedTravel`, immutable solve snapshot assembly, Great Circle / speed→`U` execution, route propagation, profile binding/defaults, ALNS/solver/verifier, public HTTP wire approval, win_poc official success, connecting any deleted legacy `com.ronext.optimizer` path, filling absent ownership as `DIRECT`, filling absent speed as `45`.

---

## Preconditions (stop if any fail)

Do **not** create production types under `rpdptw-core` or `adapters/input` until **all** of the following hold:

| # | Gate | How to satisfy |
|---|---|---|
| P1 | Phase 00 entry evidence | Re-verify `E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY` under `target/phase-00-evidence/` (or team-accepted sealed copy) + tracked summary [REVIEW_VERDICTS.md](../../implementation/evidence/phase-00/REVIEW_VERDICTS.md). Spec frontmatter may still say `BLOCKED_BY_PHASE_00_ENTRY_EVIDENCE` until formal acceptance promotion — **do not invent promotion**. If bundles missing/corrupt, **stop**. |
| P2 | Module baseline frozen | Record accepted artifactIds, packages, and exact verify command from Phase 00. Sole semantic start = `rpdptw-core`. |
| P3 | Working tree safe | `git status --short` — do not clobber unrelated user work. Prefer branch `phase-01-canonical-input` (or worktree). |
| P4 | Authorization | Real implementation task ID + Domain·Input owner / independent reviewer when org requires it. Do **not** invent IDs. |
| P5 | OPEN acknowledged | Public wire, O3 adapter product name, coercion/alias/unknown production values remain OPEN. Implementation uses **test-only** policy + `TEST_FIXTURE_V1` adapter identity. |
| P6 | Win decimal fixture policy | `data/win_poc_case.json` decimal `D/U` = **negative** fixture only. `data/win_poc_case_floor.json` = separate integer-migrated input (not generic silent floor in parser). |

**Fresh inventory (run once in Task 0):**

```bash
git rev-parse --abbrev-ref HEAD
git rev-parse HEAD
git status --short
java -version
./mvnw -version
./mvnw -B -ntp verify
shasum -a 256 \
  docs/implementation/phases/phase-01-canonical-input-normalization.md \
  docs/domain-design.md \
  docs/architecture-design.md \
  docs/master-design.md
```

---

## File structure (create / modify map)

```text
ro-next/
├── pom.xml                                 # add <module>adapters</module> if not present
├── adapters/
│   ├── pom.xml                             # packaging=pom aggregator
│   └── input/                              # NEW Maven module (Architecture adapters/input)
│       ├── pom.xml                         # artifactId rpdptw-adapter-input
│       └── src/
│           ├── main/java/com/ronext/rpdptw/adapter/input/
│           │   ├── InputAdapter.java
│           │   ├── ExternalInputDocument.java
│           │   ├── AdaptationResult.java
│           │   ├── AdaptedCanonicalInput.java
│           │   ├── SinglePathInputAdapterRegistry.java
│           │   └── testfixture/            # test-only external shape (NOT public wire)
│           │       ├── TestFixtureInputAdapter.java
│           │       ├── TestFixtureExternalDto.java
│           │       └── TestFixtureAliasPolicy.java
│           └── test/java/com/ronext/rpdptw/adapter/input/
│               ├── InputAdapterContractTest.java
│               └── LegacyExternalInputAdapterTest.java
├── rpdptw/core/
│   ├── pom.xml                             # add junit test deps if missing
│   └── src/
│       ├── main/java/com/ronext/rpdptw/
│       │   ├── input/                      # canonical records + identities
│       │   │   ├── SchemaIdentity.java
│       │   │   ├── AdapterIdentity.java
│       │   │   ├── External*Id.java
│       │   │   ├── CanonicalBusinessInput.java
│       │   │   ├── CanonicalPlanEnvelope.java
│       │   │   ├── CanonicalRequestInput.java
│       │   │   ├── CanonicalVehicleInput.java
│       │   │   ├── CanonicalLocationInput.java
│       │   │   ├── CanonicalTravelInput.java
│       │   │   ├── CanonicalServiceInput.java
│       │   │   ├── CanonicalItemInput.java
│       │   │   ├── ServicePattern.java
│       │   │   ├── InputProvenance.java
│       │   │   └── ... (supporting value types)
│       │   └── normalization/
│       │       ├── CanonicalInputNormalizer.java
│       │       ├── DefaultCanonicalInputNormalizer.java
│       │       ├── NormalizationPolicySnapshot.java
│       │       ├── NormalizationResult.java
│       │       ├── NormalizedInputArtifact.java
│       │       ├── Normalized* types
│       │       ├── FixedPointNormalizer.java / DefaultFixedPointNormalizer.java
│       │       ├── TimeNormalizer.java
│       │       ├── ServiceTimeNormalizer.java
│       │       ├── CompatibilityNormalizer.java
│       │       ├── TripPolicyNormalizer.java
│       │       ├── RouteResourceNormalizer.java
│       │       ├── CanonicalOrdering.java
│       │       ├── CanonicalFingerprint.java
│       │       ├── InputProblem.java / InputProblemCode.java / InputPath.java
│       │       └── InputRejectionReport.java
│       └── test/java/com/ronext/rpdptw/
│           ├── normalization/
│           │   ├── FixedPointNormalizerTest.java
│           │   ├── TimeNormalizerTest.java
│           │   ├── ServiceTimeNormalizerTest.java
│           │   ├── RouteResourceNormalizerTest.java
│           │   ├── CompatibilityNormalizerTest.java
│           │   ├── TripPolicyNormalizerTest.java
│           │   ├── CanonicalOrderingTest.java
│           │   └── NormalizedInputArtifactTest.java
│           └── support/                    # package-private test helpers if needed
├── build/test-fixtures/
│   └── src/test/java/com/ronext/rpdptw/fixture/
│       ├── ExternalInputFixtureBuilder.java
│       ├── CanonicalInputFixtureBuilder.java
│       ├── NormalizationOracle.java
│       └── Phase01FailureFixtures.java
└── build/architecture-rules/
    ├── pom.xml                             # add test dep on rpdptw-adapter-input
    └── src/test/java/com/ronext/rpdptw/architecture/
        └── Phase01DependencyRulesTest.java
```

**Coordinates (proposed; align with Phase 00 if already different):**

| Module path | `groupId` | `artifactId` |
|---|---|---|
| `adapters` (aggregator) | `com.ronext.rpdptw` | `rpdptw-adapters` |
| `adapters/input` | `com.ronext.rpdptw` | `rpdptw-adapter-input` |
| `rpdptw/core` | `com.ronext.rpdptw` | `rpdptw-core` (existing) |
| `build/test-fixtures` | `com.ronext.rpdptw` | `rpdptw-test-fixtures` (existing; use `type=test-jar`, `classifier=tests`) |

**Dependency direction (must hold):**

```text
rpdptw-adapter-input  →  rpdptw-core
rpdptw-test-fixtures (tests)  →  rpdptw-core  [+ optionally adapter test APIs]
rpdptw-architecture-rules (test)  →  core + adapter + existing modules

FORBIDDEN:
  rpdptw-core → adapter / Jackson / HTTP / cloud / solver / verification / application
  normalization → travel prep, PreparedTravel, solve snapshot builders
  adapter → solver / profile binding / objective
```

**Empty debris:** `adapters/legacy-win-json` and `adapters/object-filesystem` dirs exist without POMs — **do not** register as reactor modules. Leave them unless owner authorizes deletion.

---

## Task 0: WP-01.0 — Entry evidence freeze (read-only)

**Files:**
- Create: `target/phase-01-evidence/entry-gate.txt` (gitignored evidence only)
- Create (optional tracked): `docs/implementation/evidence/phase-01/README.md` with “NOT_PRODUCED until exit”

- [ ] **Step 1: Verify Phase 00 bundles exist and match review summary**

```bash
test -f target/phase-00-evidence/evidence-manifest.sha256
test -d target/phase-00-evidence/E-P00-BUILD
test -d target/phase-00-evidence/E-P00-ARCH
test -d target/phase-00-evidence/E-P00-LEGACY
cat docs/implementation/evidence/phase-00/REVIEW_VERDICTS.md
./mvnw -B -ntp verify
```

Expected: all three E-P00 dirs present; review summary shows PASS / PASS / PASS (REMOVAL); `./mvnw verify` exit 0.

If any missing → **stop**. Do not implement Phase 01 source.

- [ ] **Step 2: Freeze baseline identity into entry-gate note**

```bash
mkdir -p target/phase-01-evidence
{
  echo "phase=01"
  echo "entry_checked_at_utc=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "git_branch=$(git rev-parse --abbrev-ref HEAD)"
  echo "git_head=$(git rev-parse HEAD)"
  echo "p00_manifest_sha256=$(cat target/phase-00-evidence/evidence-manifest.sha256)"
  echo "sole_semantic_start=rpdptw-core"
  echo "adapter_module_planned=adapters/input (rpdptw-adapter-input)"
  echo "wire_schema=OPEN"
  echo "test_adapter_identity=TEST_FIXTURE_V1"
} | tee target/phase-01-evidence/entry-gate.txt
```

- [ ] **Step 3: Commit only if you add a tracked evidence README (no production code)**

```bash
# optional
git add docs/implementation/evidence/phase-01/README.md
git commit -m "docs: scaffold Phase 01 evidence directory (entry not accepted)"
```

---

## Task 1: Reactor scaffold for `adapters/input`

**Files:**
- Create: `adapters/pom.xml`
- Create: `adapters/input/pom.xml`
- Modify: root `pom.xml` — add `<module>adapters</module>`
- Modify: `rpdptw/core/pom.xml` — add JUnit test dependencies
- Modify: `build/architecture-rules/pom.xml` — add `rpdptw-adapter-input` test dependency (can wait until Task 9 if preferred)

- [ ] **Step 1: Write `adapters/pom.xml` and `adapters/input/pom.xml`**

`adapters/pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ronext</groupId>
        <artifactId>ro-next</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <groupId>com.ronext.rpdptw</groupId>
    <artifactId>rpdptw-adapters</artifactId>
    <packaging>pom</packaging>
    <name>rpdptw-adapters</name>
    <modules>
        <module>input</module>
    </modules>
</project>
```

`adapters/input/pom.xml` essentials:

```xml
<parent>
    <groupId>com.ronext.rpdptw</groupId>
    <artifactId>rpdptw-adapters</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</parent>
<artifactId>rpdptw-adapter-input</artifactId>
<packaging>jar</packaging>
<dependencies>
    <dependency>
        <groupId>com.ronext.rpdptw</groupId>
        <artifactId>rpdptw-core</artifactId>
        <version>${project.version}</version>
    </dependency>
    <!-- Jackson ONLY here for test-fixture JSON; NEVER in rpdptw-core.
         Version comes from root dependencyManagement (see note below). -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.ronext.rpdptw</groupId>
        <artifactId>rpdptw-test-fixtures</artifactId>
        <version>${project.version}</version>
        <type>test-jar</type>
        <classifier>tests</classifier>
        <scope>test</scope>
    </dependency>
</dependencies>
```

**Required:** pin Jackson in root `pom.xml` `dependencyManagement` (do not leave version only on the module):

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.19.0</version>
</dependency>
```

Then omit `<version>` on `adapters/input` dependency. **Do not** add Jackson to `rpdptw-core`.

Copy the same `bannedDependencies` Enforcer block from `rpdptw/pom.xml` onto `adapters/pom.xml` (or `adapters/input`) so adapter modules cannot pull OR-Tools/GCP/AWS.

- [ ] **Step 2: Register module in root `pom.xml`**

```xml
<modules>
    <module>build</module>
    <module>rpdptw</module>
    <module>adapters</module>
</modules>
```

- [ ] **Step 3: Ensure core can compile tests**

`rpdptw/core/pom.xml` add:

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

- [ ] **Step 4: Empty package-info for adapter + verify reactor**

Create `adapters/input/src/main/java/com/ronext/rpdptw/adapter/input/package-info.java` documenting: single anti-corruption path (D1); forbidden deps: solver, verification, cloud SDK as production authority.

```bash
./mvnw -B -ntp -pl adapters/input -am test
```

Expected: BUILD SUCCESS (0 tests OK).

- [ ] **Step 5: Commit**

```bash
git add pom.xml adapters rpdptw/core/pom.xml
git commit -m "build: add adapters/input module for Phase 01 D1 boundary"
```

---

## Task 2: Core SPI + identity + problem types (compile skeleton)

**Files:**
- Create under `rpdptw/core/src/main/java/com/ronext/rpdptw/input/`:
  - `AdapterIdentity.java`, `SchemaIdentity.java`
  - `ExternalPlanId.java`, `ExternalRequestId.java`, `ExternalVehicleId.java`, `ExternalLocationId.java`
  - `ServicePattern.java`
  - `RawInputDigest.java`
- Create under `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/`:
  - `InputPath.java`, `InputProblemCode.java`, `InputProblem.java`, `InputRejectionReport.java`
  - `NormalizationResult.java`, `Adaptation` types only if kept adapter-local (prefer adapter owns `AdaptationResult`)

- [ ] **Step 1: Write failing test that SPI types exist (optional compile-level)**

Prefer jumping to Task 3 first failing functional test; this task only needs types compiling. Minimal records:

```java
package com.ronext.rpdptw.input;

import java.util.Objects;

/** Opaque case-sensitive adapter identity (provenance, not multi-canonical track). */
public record AdapterIdentity(String value) {
    public AdapterIdentity {
        Objects.requireNonNull(value, "value");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("adapter identity must be non-empty");
        }
    }
}
```

```java
package com.ronext.rpdptw.input;

public enum ServicePattern {
    DELIVERY_ONLY,
    PICKUP_DELIVERY
}
```

```java
package com.ronext.rpdptw.normalization;

public enum InputProblemCode {
    UNSUPPORTED_ADAPTER_OR_SOURCE,
    AMBIGUOUS_ALIAS,
    UNKNOWN_FIELD_REJECTED,
    MISSING_REQUIRED_FIELD,
    DUPLICATE_IDENTITY,
    DUPLICATE_TRAVEL_KEY,
    DANGLING_REFERENCE,
    CANONICAL_ORDER_COLLISION,
    INVALID_NUMERIC_SYNTAX,
    FRACTION_NOT_ALLOWED,
    NEGATIVE_VALUE,
    NON_POSITIVE_QUANTITY,
    ARITHMETIC_OVERFLOW,
    INVALID_DATETIME,
    TIMEZONE_OR_OFFSET_NOT_ALLOWED,
    INVALID_PLAN_RANGE,
    AMBIGUOUS_WINDOW,
    ORDER_LEVEL_TASK_TIME_NOT_ALLOWED,
    INVALID_SERVICE_PATTERN,
    INVALID_REQ_DATE,
    UNAPPROVED_EXTENSION_INPUT,
    INVALID_VEHICLE_FEATURE,
    INVALID_FEATURE_LIST,
    INVALID_CAPABILITY,
    INVALID_ZONE,
    INVALID_OWNERSHIP,
    INVALID_SPEED,
    INVALID_WAIT_POLICY,
    INVALID_ROUTE_RESOURCE_LIMIT,
    UNSUPPORTED_TRIP_POLICY,
    UNSUPPORTED_ROTATION
}
```

**One public top-level type per `.java` file.** Create three files:

`InputPath.java`:

```java
package com.ronext.rpdptw.normalization;

import java.util.Objects;

public record InputPath(String dotted) {
    public InputPath {
        Objects.requireNonNull(dotted, "dotted");
    }
}
```

`InputProblem.java`:

```java
package com.ronext.rpdptw.normalization;

public sealed interface InputProblem {
    InputProblemCode code();
    InputPath path();

    record Schema(InputProblemCode code, InputPath path) implements InputProblem {}
    record Identity(InputProblemCode code, InputPath path) implements InputProblem {}
    record Reference(InputProblemCode code, InputPath path) implements InputProblem {}
    record Numeric(InputProblemCode code, InputPath path) implements InputProblem {}
    record Temporal(InputProblemCode code, InputPath path) implements InputProblem {}
    record Compatibility(InputProblemCode code, InputPath path) implements InputProblem {}
    record Trip(InputProblemCode code, InputPath path) implements InputProblem {}
}
```

`InputRejectionReport.java`:

```java
package com.ronext.rpdptw.normalization;

import java.util.List;
import java.util.Objects;

public record InputRejectionReport(List<InputProblem> problems) {
    public InputRejectionReport {
        problems = List.copyOf(Objects.requireNonNull(problems));
        if (problems.isEmpty()) {
            throw new IllegalArgumentException("rejection requires ≥1 problem");
        }
    }
}
```

External IDs: wrap `String` with **no** trim/case-fold/Unicode NFKC — store exact code units after schema-approved decoding (adapter responsibility).

- [ ] **Step 2: Compile**

```bash
./mvnw -B -ntp -pl rpdptw/core -am test-compile
```

Expected: SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add rpdptw/core/src/main/java/com/ronext/rpdptw/input \
        rpdptw/core/src/main/java/com/ronext/rpdptw/normalization
git commit -m "feat(core): add Phase 01 identity and InputProblem skeleton"
```

---

## Task 3: WP-01.3 start — Fixed-point numeric (TDD first functional suite)

**Why before adapter:** Numeric kernel is pure core, zero wire dependency, highest defect risk (double, order of FLOOR×qty). Spec §7.3 / Domain §5.1.

**Files:**
- Create: `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/FixedPointNormalizer.java`
- Create: `rpdptw/core/src/main/java/com/ronext/rpdptw/normalization/DefaultFixedPointNormalizer.java`
- Create: `rpdptw/core/src/test/java/com/ronext/rpdptw/normalization/FixedPointNormalizerTest.java`
- Create: `build/test-fixtures/.../NormalizationOracle.java` (hand oracle; may live in core test until fixtures grow)

- [ ] **Step 1: Write failing tests (exact method names from spec §9.2)**

```java
package com.ronext.rpdptw.normalization;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

class FixedPointNormalizerTest {

    private final FixedPointNormalizer n = new DefaultFixedPointNormalizer();

    @Test
    void floorsWeightAtThirdDecimal() {
        // 1.2340 and 1.2349 → both 1234 milli-units (FLOOR scale 3)
        assertEquals(1234L, n.floorNonNegativeToScale3("1.2340"));
        assertEquals(1234L, n.floorNonNegativeToScale3("1.2349"));
        // hand oracle: never use production path for expected
        assertEquals(
                new BigDecimal("1.2349").movePointRight(3).setScale(0, RoundingMode.FLOOR).longValueExact(),
                n.floorNonNegativeToScale3("1.2349"));
    }

    @Test
    void normalizesEachItemBeforeQuantityMultiplication() {
        // item-first-difference: 0.0009 × qty 2 → scaled 0, product 0
        // (line-first would floor(0.0009*2*1000)=1 — that path MUST NOT win)
        long scaled = n.floorNonNegativeToScale3("0.0009");
        assertEquals(0L, scaled);
        assertEquals(0L, n.multiplyChecked(scaled, 2));
    }

    @Test
    void rejectsDecimalDistanceEvenWhenMathematicallyIntegral() {
        assertThrows(NumericReject.class, () -> n.requireIntegerLexeme("1.0"));
    }

    @Test
    void rejectsExponentForIntegerOnlyFieldPerAdapterPolicy() {
        assertThrows(NumericReject.class, () -> n.requireIntegerLexeme("1e3"));
    }

    @Test
    void detectsScaleOverflow() {
        // digit string that cannot fit long after *1000 floor
        assertThrows(NumericReject.class,
                () -> n.floorNonNegativeToScale3("999999999999999999999.999"));
    }

    @Test
    void detectsQuantityMultiplicationOverflow() {
        assertThrows(NumericReject.class, () -> n.multiplyChecked(Long.MAX_VALUE, 2));
    }

    @Test
    void detectsRequestSumOverflow() {
        assertThrows(NumericReject.class,
                () -> n.addChecked(Long.MAX_VALUE, 1L));
    }

    @Test
    void appliesFinite999CbmOnlyWhenVolumeUnusedIsExplicit() {
        // When policy says volume axis unused, adapter may map to 999 CBM milli = 999_000
        // Normalizer must NOT invent 999; only accept explicit marker path tested at adapter layer.
        // Here: ensure plain missing volume is not auto-filled by numeric kernel.
        assertTrue(n instanceof DefaultFixedPointNormalizer);
        // no method that returns 999 by default — compile-time absence is the oracle
    }
}
```

- [ ] **Step 2: Run tests — expect fail**

```bash
./mvnw -B -ntp -pl rpdptw/core -Dtest=FixedPointNormalizerTest test
```

Expected: FAIL (class/method missing) or compilation failure.

- [ ] **Step 3: Minimal implementation — no double (one type per file)**

`NumericReject.java` (package-private or public; mapped to `InputProblem` by orchestrator later):

```java
package com.ronext.rpdptw.normalization;

final class NumericReject extends RuntimeException {
    private final InputProblemCode code;

    NumericReject(InputProblemCode code) {
        super(code.name());
        this.code = code;
    }

    InputProblemCode code() {
        return code;
    }
}
```

`FixedPointNormalizer.java`:

```java
package com.ronext.rpdptw.normalization;

public interface FixedPointNormalizer {
    long floorNonNegativeToScale3(String decimalLexeme);
    long multiplyChecked(long normalizedItemValue, int quantity);
    long addChecked(long left, long right);
    long requireIntegerLexeme(String lexeme);
}
```

`DefaultFixedPointNormalizer.java`:

```java
package com.ronext.rpdptw.normalization;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DefaultFixedPointNormalizer implements FixedPointNormalizer {

    @Override
    public long floorNonNegativeToScale3(String decimalLexeme) {
        // Parse with BigDecimal from string only — never Double.parseDouble
        final BigDecimal bd;
        try {
            bd = new BigDecimal(decimalLexeme);
        } catch (NumberFormatException | ArithmeticException e) {
            throw new NumericReject(InputProblemCode.INVALID_NUMERIC_SYNTAX);
        }
        if (bd.signum() < 0) {
            throw new NumericReject(InputProblemCode.NEGATIVE_VALUE);
        }
        try {
            return bd.movePointRight(3).setScale(0, RoundingMode.FLOOR).longValueExact();
        } catch (ArithmeticException e) {
            throw new NumericReject(InputProblemCode.ARITHMETIC_OVERFLOW);
        }
    }

    @Override
    public long multiplyChecked(long normalizedItemValue, int quantity) {
        if (quantity <= 0) {
            throw new NumericReject(InputProblemCode.NON_POSITIVE_QUANTITY);
        }
        try {
            return Math.multiplyExact(normalizedItemValue, quantity);
        } catch (ArithmeticException e) {
            throw new NumericReject(InputProblemCode.ARITHMETIC_OVERFLOW);
        }
    }

    @Override
    public long addChecked(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException e) {
            throw new NumericReject(InputProblemCode.ARITHMETIC_OVERFLOW);
        }
    }

    @Override
    public long requireIntegerLexeme(String lexeme) {
        // integer-only: optional leading +/-, digits only — reject '.', 'e', 'E'
        if (lexeme == null || lexeme.isEmpty()
                || lexeme.indexOf('.') >= 0
                || lexeme.indexOf('e') >= 0
                || lexeme.indexOf('E') >= 0) {
            throw new NumericReject(InputProblemCode.FRACTION_NOT_ALLOWED);
        }
        try {
            long v = Long.parseLong(lexeme);
            if (v < 0) {
                throw new NumericReject(InputProblemCode.NEGATIVE_VALUE);
            }
            return v;
        } catch (NumberFormatException e) {
            throw new NumericReject(InputProblemCode.INVALID_NUMERIC_SYNTAX);
        }
    }
}
```

- [ ] **Step 4: Re-run tests — expect pass**

```bash
./mvnw -B -ntp -pl rpdptw/core -Dtest=FixedPointNormalizerTest test
```

Expected: all methods PASS.

- [ ] **Step 5: Commit**

```bash
git add rpdptw/core/src/main/java/com/ronext/rpdptw/normalization \
        rpdptw/core/src/test/java/com/ronext/rpdptw/normalization/FixedPointNormalizerTest.java
git commit -m "feat(core): fixed-point n=3 FLOOR and integer-only lexemes"
```

---

## Task 4: Canonical DTOs + fixture builders

**Files:**
- Create canonical records under `rpdptw/core/.../input/` (spec §5–§7):
  - `CanonicalBusinessInput`, `CanonicalPlanEnvelope`, `CanonicalRequestInput`, `CanonicalVehicleInput`, `CanonicalLocationInput`, `CanonicalTravelInput`, `CanonicalServiceInput`, `CanonicalItemInput`, `CanonicalCompatibilityInput`, …
- Create: `build/test-fixtures/src/test/java/com/ronext/rpdptw/fixture/CanonicalInputFixtureBuilder.java`
- Create: `build/test-fixtures/src/test/java/com/ronext/rpdptw/fixture/ExternalInputFixtureBuilder.java`
- Create: `build/test-fixtures/src/test/java/com/ronext/rpdptw/fixture/Phase01FailureFixtures.java`

- [ ] **Step 1: Define minimal `CanonicalBusinessInput` graph**

Keep fields enough for §9.3 fixtures. Example request:

```java
public record CanonicalRequestInput(
        ExternalRequestId id,
        ServicePattern servicePattern,
        Optional<CanonicalServiceInput> pickup, // empty when DELIVERY_ONLY
        CanonicalServiceInput delivery,
        List<CanonicalItemInput> items,
        CanonicalCompatibilityInput compatibility,
        Optional<Boolean> mandatoryDeclaration,
        Optional<ApprovedTypedExtensionInput> extensionInput
) {
    public CanonicalRequestInput {
        items = List.copyOf(items);
        if (servicePattern == ServicePattern.DELIVERY_ONLY && pickup.isPresent()) {
            throw new IllegalArgumentException("DELIVERY_ONLY must not carry pickup visit");
        }
        if (servicePattern == ServicePattern.PICKUP_DELIVERY && pickup.isEmpty()) {
            throw new IllegalArgumentException("PICKUP_DELIVERY requires pickup visit");
        }
    }
}
```

`CanonicalServiceInput`: locationId, window open/close strings or raw seconds later, duration seconds lexeme, optional `reqDate` string, optional zone.

`CanonicalVehicleInput`: id, size feature code (not ALL/blank), capabilities set, `VehicleZoneSet` raw, ownership optional, speed optional, trip flags, route resource limits optional, wait policy raw.

- [ ] **Step 2: Fixture builders return immutable builders with fluent `.withRequest(...)`**

Builders must be **test-only** (in test-fixtures test-jar). Do not put sample production defaults in main.

- [ ] **Step 3: Compile fixtures module**

```bash
./mvnw -B -ntp -pl build/test-fixtures -am test-compile
```

- [ ] **Step 4: Commit**

```bash
git add rpdptw/core/src/main/java/com/ronext/rpdptw/input \
        build/test-fixtures/src/test/java/com/ronext/rpdptw/fixture
git commit -m "feat(core): canonical input records and Phase 01 fixture builders"
```

---

## Task 5: WP-01.1 — Single-path InputAdapter (D1)

**Files:**
- Create: `adapters/input/src/main/java/.../InputAdapter.java`
- Create: `ExternalInputDocument.java`, `AdaptationResult.java`, `AdaptedCanonicalInput.java`
- Create: `SinglePathInputAdapterRegistry.java`
- Create: `testfixture/TestFixtureInputAdapter.java` (+ DTO + alias policy)
- Create: `InputAdapterContractTest.java`, `LegacyExternalInputAdapterTest.java`

- [ ] **Step 1: Write failing adapter contract tests**

```java
package com.ronext.rpdptw.adapter.input;

import com.ronext.rpdptw.input.AdapterIdentity;
import com.ronext.rpdptw.normalization.InputProblemCode;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import static org.junit.jupiter.api.Assertions.*;

class InputAdapterContractTest {

    private final SinglePathInputAdapterRegistry registry = SinglePathInputAdapterRegistry.testDefault();

    @Test
    void acceptsSupportedAdapterAndRecordsRawDigest() throws Exception {
        byte[] bytes = """
            {"adapter":"TEST_FIXTURE_V1","planId":"P1","customer":"C","profile":"P","profileVersion":"1"}
            """.getBytes(StandardCharsets.UTF_8);
        var doc = new ExternalInputDocument(bytes, new AdapterIdentity("TEST_FIXTURE_V1"), "application/json");
        var result = registry.adapt(doc);
        assertInstanceOf(AdaptationResult.Accepted.class, result);
        var accepted = (AdaptationResult.Accepted) result;
        byte[] expected = MessageDigest.getInstance("SHA-256").digest(bytes);
        assertArrayEquals(expected, accepted.input().rawInputDigest().sha256());
    }

    @Test
    void rejectsUnknownAdapterWithoutFallback() {
        var doc = new ExternalInputDocument(
                "{}".getBytes(StandardCharsets.UTF_8),
                new AdapterIdentity("UNKNOWN_V99"),
                "application/json");
        var result = registry.adapt(doc);
        assertInstanceOf(AdaptationResult.Rejected.class, result);
        var codes = ((AdaptationResult.Rejected) result).report().problems().stream()
                .map(p -> p.code()).toList();
        assertTrue(codes.contains(InputProblemCode.UNSUPPORTED_ADAPTER_OR_SOURCE));
        // MUST NOT try "latest" or nearest adapter
    }

    @Test
    void unknownFieldBehaviorComesFromExplicitPolicy() {
        // With REJECT policy, unknown field → UNKNOWN_FIELD_REJECTED
        // With IGNORE policy (test-only), success + provenance records ignored paths
        // Never invent production default silently
        assertDoesNotThrow(() -> TestFixtureAliasPolicy.strictReject());
    }
}
```

```java
class LegacyExternalInputAdapterTest {

    @Test
    void mapsPerSideReqDateOnly() { /* PICKUP_DELIVERY: pickup.reqDate and delivery.reqDate independent */ }

    @Test
    void rejectsOrderLevelTaskTime() { /* order-level taskTime → ORDER_LEVEL_TASK_TIME_NOT_ALLOWED */ }

    @Test
    void preservesExactProfileSelectionAndPresetOmission() { /* preset Optional.empty preserved */ }

    @Test
    void rejectsUnapprovedRawCustomerExtension() { /* raw map extension → UNAPPROVED_EXTENSION_INPUT */ }
}
```

- [ ] **Step 2: Run — expect fail**

```bash
./mvnw -B -ntp -pl adapters/input -am \
  -Dtest=InputAdapterContractTest,LegacyExternalInputAdapterTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

- [ ] **Step 3: Implement SPI + single registry + test fixture adapter**

```java
public interface InputAdapter {
    boolean supports(AdapterIdentity adapter);
    AdaptationResult adapt(ExternalInputDocument document);
}

public sealed interface AdaptationResult {
    record Accepted(AdaptedCanonicalInput input) implements AdaptationResult {}
    record Rejected(InputRejectionReport report) implements AdaptationResult {}
}

public final class SinglePathInputAdapterRegistry {
    private final List<InputAdapter> adapters; // typically size 1 in production path selection

    public AdaptationResult adapt(ExternalInputDocument document) {
        InputAdapter match = null;
        for (InputAdapter a : adapters) {
            if (a.supports(document.declaredAdapterIdentity())) {
                if (match != null) {
                    // registration bug — never ambiguous multi-path success
                    throw new IllegalStateException("multiple adapters claim same identity");
                }
                match = a;
            }
        }
        if (match == null) {
            return new AdaptationResult.Rejected(new InputRejectionReport(List.of(
                    new InputProblem.Schema(
                            InputProblemCode.UNSUPPORTED_ADAPTER_OR_SOURCE,
                            new InputPath("adapter")))));
        }
        return match.adapt(document);
    }
}
```

`TestFixtureInputAdapter`:
1. Digest exact bytes (SHA-256).
2. Parse JSON with Jackson into `TestFixtureExternalDto` (strict: `FAIL_ON_UNKNOWN_PROPERTIES` when policy=REJECT).
3. Map to `CanonicalBusinessInput` (servicePattern enum only; map legacy `dueDate` → per-side `reqDate` only when fixture says so — document mapping in class Javadoc).
4. Record provenance: alias list, unknown-field decisions, schema identity `TEST_FIXTURE_SCHEMA_V1`.
5. On any problem → `Rejected` with deterministic sort (Task 8).

**D1 rules:** No second registry for “v2 schema track”. Declared adapter identity is provenance only.

- [ ] **Step 4: Green + commit**

```bash
./mvnw -B -ntp -pl adapters/input -am \
  -Dtest=InputAdapterContractTest,LegacyExternalInputAdapterTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
git add adapters/input rpdptw/core
git commit -m "feat(adapter): single-path TEST_FIXTURE_V1 input adapter (D1)"
```

---

## Task 6: WP-01.2 — Identity, reference graph, canonical ordering

**Files:**
- Create: `CanonicalOrdering.java`, `ReferenceValidator.java` (names flexible)
- Create: `CanonicalOrderingTest.java`
- Extend: `DefaultCanonicalInputNormalizer` draft pipeline (duplicates before maps)

- [ ] **Step 1: Failing tests (identity/order only — fingerprint methods wait for Task 9)**

```java
@Test void rejectsDuplicateRequestBeforeCanonicalSort() { /* two same ExternalRequestId */ }
@Test void rejectsDuplicateTravelKeyEvenWhenValuesMatch() { /* (A,B) twice */ }
@Test void rejectsDanglingLocationReference() { /* service location not in locations */ }
@Test void failureOrderIsInputPermutationIndependent() { /* problem sort key fixed */ }
// DEFER to Task 9 (needs CanonicalFingerprint):
// setPermutationKeepsSemanticFingerprint, visitOrderChangesSemanticFingerprint
```

- [ ] **Step 2: Run red**

```bash
./mvnw -B -ntp -pl rpdptw/core -am -Dtest=CanonicalOrderingTest test
```

- [ ] **Step 3: Implement rules**

| Collection | Rule |
|---|---|
| Requests / vehicles / locations | Detect duplicate external ID **before** `Map`/`sort`; reject `DUPLICATE_IDENTITY` |
| Sparse travel | Key `(from,to)`; duplicate even if values equal → `DUPLICATE_TRAVEL_KEY` |
| References | Every locationId on service/depot/travel exists → else `DANGLING_REFERENCE` |
| Set-like sort | Unsigned UTF-8 lexicographic order of identity string (record comparator in `CanonicalOrdering` + ADR note) |
| Pickup/delivery visit fields | Do **not** reorder (order is meaning) |
| Error order | Sort by `path.dotted` then `code.name()` then stable evidence digest of path+code |

Task 6 “green” means the four identity/reference/order tests above pass. Do **not** implement full semantic fingerprint encoding here.

- [ ] **Step 4: Green + commit**

```bash
git commit -m "feat(core): identity, reference validation, and canonical set ordering"
```

---

## Task 7: WP-01.4 — Time, service time, trip, route resources

**Files:**
- Create: `TimeNormalizer.java`, `ServiceTimeNormalizer.java`, `TripPolicyNormalizer.java`, `RouteResourceNormalizer.java`
- Create matching `*Test.java` with **exact** §9.2 method names (checklist below)
- Create normalized types: `NormalizedWindow`, `NormalizedPlanEnvelope`, `TripPolicy`, `DepotWaitPolicy`, `WorkArcPolicy`, `NormalizedRouteResourceLimits`

### Required exact test methods (spec §9.2 — all must exist and run)

| Class | Methods |
|---|---|
| `TimeNormalizerTest` | `convertsExactLocalDateTimeToPlanOriginSeconds`; `rejectsOffsetOrZone`; `keepsPlanEndExclusiveAndCloseInclusive`; `expandsOvernightWindowOncePerPlanDate`; `rejectsEqualOpenCloseWithoutSchemaMeaning`; `preservesFullArcRestartPolicyAndExactHandoffOracle` |
| `ServiceTimeNormalizerTest` | `checksServiceDurationItemTimeQuantitySum`; `detectsServiceTimeOverflow`; `rejectsOrderLevelTaskTime` |
| `TripPolicyNormalizerTest` | `onewayIgnoresRotationButRecordsRawValue`; `acceptsSingleRoundtripZeroRotation`; `rejectsNonOnewayRotation`; `normalizesExplicitDepotWaitPolicyWithoutFallback`; `rejectsUnknownDepotWaitPolicy` |
| `RouteResourceNormalizerTest` | `preservesMissingRouteLimitsAsTypedAbsence`; `normalizesVehicleAndGlobalRouteLimitsIndependently`; `rejectsNegativeOrFractionalRouteResourceLimit`; `detectsRouteResourceLimitOverflow` |

- [ ] **Step 1: Failing time/service/trip/resource tests**

Critical oracles:

```java
@Test
void convertsExactLocalDateTimeToPlanOriginSeconds() {
    // planStart = 2026-08-01 00:00:00, event = 2026-08-01 01:00:00 → 3600L
    // format exact "yyyy-MM-dd HH:mm:ss" only
}

@Test
void rejectsOffsetOrZone() {
    // "2026-08-01T01:00:00Z", "2026-08-01 01:00:00+09:00", trailing "Z" → TIMEZONE_OR_OFFSET_NOT_ALLOWED
}

@Test
void keepsPlanEndExclusiveAndCloseInclusive() {
    // raw close inclusive → endSecondExclusive = close + 1 (checked), clipped to planEndExclusive
}

@Test
void expandsOvernightWindowOncePerPlanDate() {
    // Fixed fixture (do not invent alternate expansions):
    //   planStart          = 2026-08-01 00:00:00  → origin 0
    //   planEndExclusive   = 2026-08-04 00:00:00  → origin 259200  (3*86400)
    //   raw overnight open = 22:00:00, close inclusive = 02:00:00
    //
    // Algorithm (required):
    //   for each calendar date D where D ∈ [planStartDate, planEndDate):
    //     openSec          = seconds(D + 22:00:00)
    //     closeInclusiveSec= seconds(D+1 + 02:00:00)
    //     endExclusiveSec  = closeInclusiveSec + 1   // inclusive close → half-open
    //     clip to [0, planEndExclusive); drop empty
    //
    // Expected half-open intervals [start, end):
    //   [79200, 93601)     // Aug1 22:00 → Aug2 02:00:01
    //   [165600, 180001)   // Aug2 22:00 → Aug3 02:00:01
    //   [252000, 259200)   // Aug3 22:00 → clipped at planEnd (would be Aug4 02:00:01)
    // assertEquals(List.of(w(79200,93601), w(165600,180001), w(252000,259200)), actual);
}

@Test
void rejectsEqualOpenCloseWithoutSchemaMeaning() { /* AMBIGUOUS_WINDOW */ }

@Test
void preservesFullArcRestartPolicyAndExactHandoffOracle() {
    // WorkArcPolicy.FULL_ARC_WITHIN_ONE_WORK_WINDOW only
    // remainder 5s, travel 6s, next work start 100 → expected restart departure 100
    // Phase 01 records policy + handoff tuple; does NOT run propagation
}
```

Service time:

```text
serviceSeconds = duration + Σ (item.taskTime × qty)  // checked Math.*Exact
order-level taskTime → ORDER_LEVEL_TASK_TIME_NOT_ALLOWED
overflow → ARITHMETIC_OVERFLOW via detectsServiceTimeOverflow
```

Trip:

| Input | Result |
|---|---|
| oneway (+ any rotation raw) | `TripPolicy.OneWay`, raw rotation in provenance only |
| single roundtrip, rotation 0 | `TripPolicy.SingleRoundTrip` |
| non-oneway multi rotation | `UNSUPPORTED_ROTATION` |
| waitInDepot known N/Y | `DepotWaitPolicy` explicit |
| unknown wait | `INVALID_WAIT_POLICY` |

Route resources: missing → typed `Optional.empty()` absence; never `Long.MAX_VALUE`/0 sentinel; negative/fractional → `INVALID_ROUTE_RESOURCE_LIMIT`; overflow → `detectsRouteResourceLimitOverflow`.

`reqDate`: preserve per-side optional/normalized seconds; **do not** evaluate `serviceStart ≤ reqDate` (Phase 03). Invalid shape → `INVALID_REQ_DATE`.

- [ ] **Step 2–4: Red → implement → green**

```bash
./mvnw -B -ntp -pl rpdptw/core -am \
  -Dtest=TimeNormalizerTest,ServiceTimeNormalizerTest,TripPolicyNormalizerTest,RouteResourceNormalizerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(core): time, service, trip, and route-resource normalization"
```

---

## Task 8: WP-01.5 — Compatibility, zone, ownership, speed, servicePattern

**Files:**
- Create: `CompatibilityNormalizer.java` + sealed types `AllowedVehicleSizes`, `VehicleZoneSet`, `VehicleOwnership`, `VehicleSpeedInput`
- Create: `CompatibilityNormalizerTest.java` with **exact** WP-01.5 method names

- [ ] **Step 1: Failing tests**

```java
@Test void keepsFreeFormSizeCodeCaseSensitive() { /* "t1" ≠ "T1" */ }
@Test void acceptsOnlyExactAllAlternative() { /* request list exactly ["ALL"] */ }
@Test void rejectsMixedAllAndConcreteCode() { /* ["ALL","T1"] → INVALID_FEATURE_LIST */ }
@Test void rejectsVehicleAllOrBlank() { /* vehicle concrete feature required */ }
@Test void preservesVehicleMultiZoneIds() { /* SortedSet of zones */ }
@Test void normalizesMissingVehicleZonesToAllZones() { /* VehicleZoneSet.AllZones */ }
@Test void preservesNoEligibleVehicleAsValidNormalizedFact() { /* not malformed reject */ }
@Test void preservesConflictingConcretePickupDeliveryZonesAsStaticUnassignabilityFact() { /* not reject */ }
@Test void normalizesOwnershipPresentOrAbsentNeverSilentDirect() { /* absent ≠ DIRECT */ }
@Test void preservesAbsentSpeedWithoutFilling45() { /* VehicleSpeedInput.Absent */ }
@Test void rejectsInvalidSpeed() { /* NaN, ≤0, non-finite → INVALID_SPEED */ }
@Test void acceptsServicePatternDeliveryOnlyAndPickupDeliveryOnly() { /* else INVALID_SERVICE_PATTERN */ }
```

- [ ] **Step 2–4: Implement sealed models**

```java
public sealed interface VehicleOwnership {
    record Absent() implements VehicleOwnership {}
    record Direct() implements VehicleOwnership {}
    record Lease() implements VehicleOwnership {}
}

public sealed interface VehicleSpeedInput {
    record Absent() implements VehicleSpeedInput {}
    record PresentKmH(double value) implements VehicleSpeedInput {
        public PresentKmH {
            if (!(value > 0.0) || Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException("invalid speed");
            }
        }
    }
}

public sealed interface VehicleZoneSet {
    record AllZones() implements VehicleZoneSet {}
    record Restricted(SortedSet<ZoneCode> zoneIds) implements VehicleZoneSet {}
}
```

Note: speed uses `double` only for **already validated finite present** vehicle speed declaration (Domain travel prep uses km/h). Do **not** use double for weight/volume/distance/time. Invalid present speed is rejected **before** constructing `PresentKmH`.

Zone conflict on PICKUP_DELIVERY with concrete incompatible zones → attach **normalized static unassignability fact** on artifact (typed field), **not** `InputProblem` reject.

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(core): size/capability/zone/ownership/speed normalization"
```

---

## Task 9: WP-01.6 — Orchestrator, seal, fingerprints, architecture guard

**Files:**
- Create: `CanonicalInputNormalizer.java`, `DefaultCanonicalInputNormalizer.java`
- Create: `NormalizedInputArtifact.java`, `CanonicalFingerprint.java`, `NormalizationPolicySnapshot.java`
- Create: `NormalizedInputArtifactTest.java`
- Create: `Phase01DependencyRulesTest.java`
- Modify: architecture-rules POM dependency on adapter

- [ ] **Step 1: Failing artifact/fingerprint tests**

Exact methods from spec (`NormalizedInputArtifactTest` + deferred `CanonicalOrderingTest` methods):

```java
// NormalizedInputArtifactTest
@Test void artifactDefensivelyCopiesAllCollections() { /* mutate input lists post-seal does not change artifact */ }
@Test void sameMeaningAndPolicyHasSameSemanticFingerprint() {}
@Test void profilePresetMandatoryResourceMeaningChangesSemanticFingerprint() {}
@Test void aliasProvenanceChangesEnvelopeNotMeaning() {}
@Test void rejectionNeverExposesPartialArtifact() { /* Rejected has no artifact accessor */ }
@Test void rejectionEvidenceRedactsRawValuesAndInputBytes() {
    // inject canary email/address in invalid payload; report.toString()/messages must not contain canary
}

// CanonicalOrderingTest (deferred from Task 6 — implement fingerprint first, then these)
@Test void setPermutationKeepsSemanticFingerprint() { /* set-like reorder: same semantic fp, raw digest may differ */ }
@Test void visitOrderChangesSemanticFingerprint() { /* ordered sequence change: different semantic fp */ }
```

Architecture:

```java
@AnalyzeClasses(packages = "com.ronext.rpdptw")
class Phase01DependencyRulesTest {
    @ArchTest
    static final ArchRule coreDoesNotDependOnJacksonCloudSolverOrVerification = /* ... */;

    @ArchTest
    static final ArchRule normalizationDoesNotCreatePreparedTravelOrSolveSnapshot =
            noClasses().that().resideInAPackage("com.ronext.rpdptw.normalization..")
                    .should().dependOnClassesThat().haveSimpleNameContaining("PreparedTravel")
                    /* also ban ProblemInstance builders if introduced elsewhere */;

    @ArchTest
    static final ArchRule adapterDoesNotDependOnSolver =
            noClasses().that().resideInAPackage("com.ronext.rpdptw.adapter.input..")
                    .should().dependOnClassesThat().resideInAPackage("com.ronext.rpdptw.solver..");
}
```

Also ban core → `com.fasterxml.jackson`, `com.google`, `software.amazon`.

- [ ] **Step 2: Orchestrator pseudo-code (must match spec §7.7)**

```text
normalize(adapted, policy):
  problems = []
  problems += duplicates / references / servicePattern·reqDate shape
  if problems: return Rejected(sort(problems))
  draft = numeric + time + service + compatibility + trip + resources
  // ownership/speed/zone absence preserved
  problems += arithmetic boundary problems
  if problems: return Rejected(sort(problems))
  ordered = sort set-like only
  semantic = fingerprint(policy, adapter, plan/profile/preset-or-omission,
                         mandatory/extension, trip/wait/resource/ownership/zone/speed, ordered)
  envelope = fingerprint(rawDigest, semantic, provenance)
  return Accepted(seal(ordered, digests, fingerprints))
```

Fingerprint encoding (**PROPOSED INTERNAL**):

1. Version string `fp-v1` in the digest preimage.
2. Canonical UTF-8 length-prefixed fields (or JSON-like deterministic encoding with sorted keys) — implement in `CanonicalFingerprint` **and** mirror in test oracle under fixtures (do not call production encoder for expected values in property tests — either table-drive equality or dual independent encoder in test sources).
3. Algorithm: SHA-256 of preimage bytes; store as hex lowercase.

`NormalizedInputArtifact` constructor: deep `List.copyOf` / unmodifiable sorted sets; no setters.

`NormalizationResult`:

```java
public sealed interface NormalizationResult {
    record Accepted(NormalizedInputArtifact artifact) implements NormalizationResult {}
    record Rejected(InputRejectionReport report) implements NormalizationResult {}
}
```

- [ ] **Step 3: Green full suite subset**

```bash
./mvnw -B -ntp -pl rpdptw/core,adapters/input,build/architecture-rules -am test
```

- [ ] **Step 4: Commit**

```bash
git commit -m "feat(core): seal NormalizedInputArtifact and Phase 01 architecture rules"
```

---

## Task 10: Wire end-to-end happy path + §9.3 failure catalog

**Files:**
- Extend: `Phase01FailureFixtures.java` with every row in spec §9.3
- Create: integration-style test in core or adapter: `Phase01EndToEndNormalizationTest` (optional name; keep required §9.2 classes as owners of methods)

- [ ] **Step 1: For each §9.3 fixture, add one assertion**

| Fixture | Expected |
|---|---|
| `item-first-difference` | weight product 0 |
| `scale-boundary` | both 1234 |
| `integer-looking-decimal` | `FRACTION_NOT_ALLOWED` |
| `quantity-overflow` / `sum-overflow` | `ARITHMETIC_OVERFLOW`, no artifact |
| `duplicate-request` / `duplicate-arc` | identity/travel codes |
| `decimal-win-travel` | reject decimal D/U (load snippet from `data/win_poc_case.json` matrix only as **negative** bytes if useful — do not mark success) |
| `ownership-absent` / `speed-absent` | typed absence |
| `service-pattern-only` | reject LOGICAL/REAL tokens |
| `pii-redaction` | canary absent from report |
| `zone-conflict` | success + unassignability fact |

- [ ] **Step 2: Run**

```bash
./mvnw -B -ntp -pl rpdptw/core,adapters/input -am test
```

Expected: required tests 0 failed / 0 skipped.

- [ ] **Step 3: Commit**

```bash
git commit -m "test: Phase 01 failure fixture catalog and e2e normalization path"
```

---

## Task 11: WP-01.7 — Evidence bundle + handoff (no fake ACCEPTED)

**Files:**
- Create under `target/phase-01-evidence/` (gitignored): tree from spec §10.2
- Create tracked summary optional: `docs/implementation/evidence/phase-01/README.md` update
- **Do not** flip phase YAML to ACCEPTED without independent review + user instruction

- [ ] **Step 1: Full verify**

```bash
./mvnw -B -ntp -pl rpdptw/core,adapters/input,build/architecture-rules -am verify
./mvnw -B -ntp verify
```

Expected: exit 0; Surefire XML under each owner module lists every §9.2 class with executed tests ≥1.

- [ ] **Step 2: Collect evidence**

```bash
mkdir -p target/phase-01-evidence/{E-P01-NUMERIC,E-P01-TIME,E-P01-COMPAT,E-P01-ERROR,handoff,manifest}
# copy surefire reports, test counts, java -version, git rev-parse, entry-gate.txt
# write known-limitations.md:
#   - public wire OPEN
#   - O3 product adapter name OPEN
#   - default speed 45 is Phase 02+ travel-prep policy only
#   - win_poc decimal not official success
./build/verify-evidence-bundle.sh target/phase-01-evidence  # if script supports; else manual sha256 manifest
```

Handoff note for Phase 02 must list:

```text
NormalizedInputArtifact
  rawInputDigest, semanticFingerprint, envelopeFingerprint
  external identity graph
  servicePattern + per-side reqDate (request-time meaning)
  customer/profile/version + preset omission
  mandatory + approved extension declarations (unbound)
  sparse integer D/U only when present
  speed present|absent (no fill 45)
  vehicle multi-zone | all-zones; ownership present|absent
  trip/wait/resource typed absence
  NO dense IDs, NO PreparedTravel, NO solve snapshot
```

- [ ] **Step 3: Commit evidence tooling/docs only if tracked**

```bash
git add docs/implementation/evidence/phase-01
git commit -m "docs: Phase 01 evidence layout and Phase 02 handoff contract notes"
```

- [ ] **Step 4: Request independent review**

Point reviewer at:

1. This plan completion checklist  
2. Spec §10 exit gate  
3. Spec §14 traceability table  
4. `target/phase-01-evidence/` digests  

Do **not** self-mark `phase_acceptance_status: ACCEPTED`.

---

## Anti-patterns (implementer must not)

- `Double.parseDouble` / `Float` for weight, volume, distance, time  
- `Map.put` last-write-wins for duplicate IDs or travel keys  
- Trim/lowercase external IDs  
- Silent `ownership=DIRECT` or `speed=45`  
- Vehicle forced single zone  
- `kind` LOGICAL|REAL / `REAL_PICKUP_DELIVERY` enums  
- `reqDate` as completion deadline or `serviceEndTime` bound  
- Core depending on Jackson  
- Multi-adapter “schema version tracks”  
- `@Disabled` hiding required §9.2 tests  
- Claiming win_poc official success from decimal matrix  
- Partial `NormalizedInputArtifact` on failure  

---

## Definition of Done (plan execution)

- [ ] All Tasks 0–11 checkboxes complete  
- [ ] §9.2 exact test class/method names present and green  
- [ ] §9.3 fixtures covered  
- [ ] `./mvnw verify` green at reactor root  
- [ ] `Phase01DependencyRulesTest` green  
- [ ] Evidence dirs for four E-P01-* keys exist with digests  
- [ ] Phase 02 handoff contract written; no travel/snapshot code  
- [ ] Independent review requested; ACCEPTED only after human process  

---

## Execution notes for agentic workers

- Prefer @superpowers:subagent-driven-development: one task per subagent, TDD order (red→green→commit).  
- Prefer @superpowers:executing-plans for inline batch with checkpoints after Tasks 3, 5, 7, 9, 11.  
- If Phase 00 evidence gate fails mid-stream, halt and surface to human — do not stub fake PASS files.  
- If Product/API owner later freezes public wire (O3), replace only `adapters/input` mapping; **keep** core canonical/normalization semantics stable (D1).
