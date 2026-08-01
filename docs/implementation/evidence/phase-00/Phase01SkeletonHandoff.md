# Phase 01 Skeleton Handoff Manifest

## Overview
This document specifies the formal handoff contract and architecture constraints established in Phase 00 for consumption by Phase 01 (Domain Model Implementation).

---

## Key Handoff Directives

### 1. Semantic Production Entry Point
- **`rpdptw-core`** (`com.ronext.rpdptw:rpdptw-core`) is the **ONLY** semantic production start module for Phase 01 domain modeling.
- All core domain models, value objects, domain events, and pure business logic MUST originate in `rpdptw-core`.
- `rpdptw-core` is strictly pure Java (JDK standard library only) with zero external compile-scope dependencies.

### 2. No Premature Domain Types Created in Phase 00
- Zero `CanonicalInput`, route plan, vehicle, distance matrix, or customer domain model types were implemented in Phase 00.
- Phase 01 starts from a clean, unpolluted architecture skeleton with no legacy domain assumptions pre-baked.

### 3. Test-Fixtures Consumer Coordinates
- Shared test utilities and fixtures are published via `rpdptw-test-fixtures`.
- Modules consuming test fixtures in Phase 01 MUST declare dependency using test classifier:
  ```xml
  <dependency>
      <groupId>com.ronext.rpdptw</groupId>
      <artifactId>rpdptw-test-fixtures</artifactId>
      <version>${project.version}</version>
      <type>test-jar</type>
      <scope>test</scope>
  </dependency>
  ```
- Test utilities MUST NOT leak into main compile or runtime scopes (enforced by `TestScopeLeakageArchitectureTest`).

---

## Open Items Status (All OPEN)

| Item ID | Description | Status | Target Phase |
| :--- | :--- | :---: | :---: |
| **C-17** | Customer configuration dynamic loading & security boundary scope rules | **OPEN** | Phase 01 / Phase 03 |
| **O1** | OptaPlanner vs Timefold solver engine abstraction boundary decision | **OPEN** | Phase 02 |
| **Q-BENCH-02** | Solver benchmarking harness baseline dataset & metric collection specification | **OPEN** | Phase 02 |

---

## Non-Acceptance & Acceptance Criteria
> [!IMPORTANT]
> **Implementation Non-Acceptance Statement:**
> Phase 00 implementation is **NOT ACCEPTED** until independent review of all evidence bundles (`E-P00-BUILD`, `E-P00-ARCH`, `E-P00-LEGACY`) and formal sign-off by the lead architecture reviewer.
