# Phase 01 Skeleton Handoff Manifest

## Overview

This document specifies the formal handoff contract and architecture constraints
established in Phase 00 for consumption by Phase 01 (canonical input + normalization).

---

## Key Handoff Directives

### 1. Semantic Production Entry Point

- **`rpdptw-core`** (`com.ronext.rpdptw:rpdptw-core`) is the **ONLY** semantic
  production start module for Phase 01 domain modeling.
- All core domain models, value objects, and pure business logic MUST originate
  in `rpdptw-core`.
- `rpdptw-core` is strictly pure Java (JDK standard library only) with zero
  external compile-scope dependencies.

### 2. No Premature Domain Types Created in Phase 00

- Zero `CanonicalInput`, `ProblemInstance`, `PreparedTravel`, route plan,
  vehicle, distance matrix, or customer domain model types were implemented in
  Phase 00.
- Phase 01 starts from a clean architecture skeleton with no legacy domain
  assumptions pre-baked.

### 3. Test-Fixtures Consumer Coordinates

- Shared test utilities and fixtures are published via `rpdptw-test-fixtures`.
- Modules consuming test fixtures in Phase 01 MUST declare the dependency with
  the tests classifier:

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

- Test utilities MUST NOT leak into main compile or runtime scopes (enforced by
  `TestScopeLeakageArchitectureTest`).

### 4. Legacy GCP Placeholder — Removed

- The `legacy/gcp-placeholder` module, `gcp/` deployment inventory, and legacy
  Dockerfile were **deleted by user decision** (not preserved or re-characterized).
- Target modules must not reintroduce `com.ronext.optimizer` or GCP SDKs
  (enforced by ArchUnit + bytecode gates + bannedDependencies).
- E-P00-LEGACY records removal, not golden behavior preservation.

---

## Open Items Status (Master / Domain / Architecture authority)

| Item ID | Description | Status | Phase 00 rule |
| :--- | :--- | :---: | :--- |
| **C-17** | Optional hybrid route-selection (OR-Tools / CP-SAT proposed; `backends/*` only) | **GATED** | Do not implement or advertise before 14A receipt + separate scope approval. Config default off ≠ gate open. |
| **O1 / D2** | Worker / API compute product = **Lambda \| ECS** | **OPEN** | Do not pin either product in module names, skeleton, or production defaults. Reference platform uses AWS S3 + Step Functions; compute choice remains open. |
| **Q-BENCH-02** | Official step / worker / round / watchdog numeric thresholds | **OPEN** | Phase 00 MUST NOT fix these as POM/test/production defaults. Test-only fixtures only if clearly labeled. |

---

## Non-Acceptance & Acceptance Criteria

> [!IMPORTANT]
> **Implementation Non-Acceptance Statement:**
> Phase 00 implementation is **NOT ACCEPTED** until independent review of evidence
> bundles (`E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`) and formal sign-off by the
> architecture reviewer. See `REVIEW_VERDICTS.md` when present under this directory
> or under `target/phase-00-evidence/`.
